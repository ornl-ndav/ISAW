/* 
 * File: EV_IntegrateUtils.java
 *
 * Copyright (C) 2012, Dennis Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author: dennis $
 *  $Date: 2012/11/09 15:40:55 $            
 *  $Revision: 1.2 $
 */
package EventTools.Integrate;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import Jama.*;

import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.PeakDisplayInfo;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.PeaksDisplayPanel;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.PeakArrayPanels;

import java.io.*;
import java.util.*;
import gov.anl.ipns.Util.File.*;
import EventTools.EventList.*;
import EventTools.Histogram.*;
import Operators.TOF_SCD.*;
import gov.anl.ipns.MathTools.Geometry.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.instruments.*;

public class EV_IntegrateUtils 
{

/**
 * Make a list with lists of events for individual detector module IDs, from
 * a array of events with packed values for Q,id,row,col,tof and lamda, 
 * as returned by SNS_Tof_to_Q_map.MapEventsTo_Q_ID_Row_Col(). 
 *
 * @param max_id       The maximum detector module ID in the list of events.
 * @param ev_info_list Array of floats containing  Q,id,row,col,tof and lamda
 *                     values in sequence, for each event.
 */
  public static Vector<Vector<EventInfo>> SplitEventsByID( int     max_id,
                                                         float[] ev_info_list )
  {
     Vector<Vector<EventInfo>> list = new Vector<Vector<EventInfo>>();
     for ( int i = 0; i <= max_id; i++ )
       list.add( new Vector<EventInfo>() );

     int id = 0;
     for ( int i = 0; i < ev_info_list.length; i+= 8 ) 
     {
       id = Math.round( ev_info_list[ i + 3 ] );
       if ( id < max_id )
         list.elementAt(id).add( new EventInfo( ev_info_list, i ) );
     }
     return list;
  }


/**
 *  Create a Hashtable where each entry is a Vector<EventInfo> objects
 *  corresponding to one hkl.  An event is considered to coorespond to the
 *  hkl with h,k,l values found by rounding the fractional hkl values 
 *  obtained from UB_inverse * gonio_inverse * Q.  The hash key is the 
 *  String formed by  h+","+k+","+l.  Events with hkl = 0,0,0 are omitted.
 *  The initial event list should only have events from ONE detector!  NOTE:
 *  Due to the possibly tilted parallelepipeds of the reciprocal lattice
 *  the event lists obtained from this method should not be used directly
 *  for getting integrated intensities.  Rather, they will just provide a
 *  more limited search list for obtaining the mins and maxes of row, col 
 *  and Q (or tof) for each hkl, as well as finding which hkl's might be
 *  in this detector.  The list of events for a peak in the Hashtable produced
 *  is intended to be passed into GetPeakEventList() to find a detector 
 *  row,col and |Q| aligned box containing the peak that CAN be used to 
 *  find relevant events for the peak.
 *
 *  @param UB_inverse     The transform that maps the Q vector in crystal
 *                        coordinates to H,K,L
 *  @param gonio_inverse  Inverse goniometer rotation, used to rotate the
 *                        Q vector for the peak into crystal coordinates.
 *  @param ev_list        List of events for one detector
 *
 *  @return a Hashtable containing Vectors of EventInfo objects, with one
 *          Vector for each h,k,l that is present in this detector.
 */
  public static Hashtable SplitEventsByHKL( Tran3D   UB_inverse,
                                            Tran3D   gonio_inverse,
                                            Vector<EventInfo> ev_list )
  {
    Tran3D to_hkl = new Tran3D( UB_inverse );
    to_hkl.multiply_by( gonio_inverse );
    Hashtable h_table = new Hashtable();
    int h = 0;
    int k = 0;
    int l = 0;
    String key;
    for ( int i = 0; i < ev_list.size(); i++ )
    {
      EventInfo info = ev_list.elementAt(i);
      Vector3D q_vec = info.VecQ_over_2PI();
      Vector3D hkl = new Vector3D();
      to_hkl.apply_to( q_vec, hkl );
      h = Math.round( hkl.getX() );
      k = Math.round( hkl.getY() );
      l = Math.round( hkl.getZ() );
      if ( h != 0 || k != 0 || l != 0 )        // valid hkl
      {
        key = "" + h + "," + k + "," + l;
        Vector<EventInfo> hkl_ev_list = (Vector<EventInfo>)h_table.get(key);
        if ( hkl_ev_list == null )
        {
          hkl_ev_list = new Vector<EventInfo>();
          hkl_ev_list.add( info );
          h_table.put( key, hkl_ev_list );
        }
        else
          hkl_ev_list.add( info );
      }
    }
    return h_table;
  }


/**
 *  Get a list of events in a detector row, column and |Q| aligned block, 
 *  corresponding to the specified peak, considering the specified tolerance.
 *  First, the preliminary event list for the specified peak is scanned 
 *  to find the min & max of row, col and |Q| for all events whose hkl is 
 *  within the specified tolerance of that hkl.  Then ALL the events for 
 *  the detector are scanned to find events that are within that range of 
 *  row, col and |Q| values.  Events passing this requirement are added to 
 *  the list of EventInfo objects included in the PeakEventList object that
 *  is returned by this method.  
 *  NOTE: The tolerance on hkl should be no larger than 1/2.
 *
 *  @param peak             A Peak_new object with information about this
 *                          peak, including h,k,l and the sample orientation
 *  @param tolerance        Tolerance on h,k,l to use when determining the
 *                          range of row,col,|Q| for events to be associated
 *                          with this peak (i.e. be included in the 
 *                          PeakEventList that is returned).
 *  @param UB_inverse       The matrix that maps Q/(2PI) to HKL
 *  @param ev_list_for_hkl  List of events that are closer to this peak's hkl
 *                          than to any other hkl.  These events are the only
 *                          events checked when scanning for events within
 *                          the specified tolerance on hkl.  
 *  @param ev_list_for_det  List of ALL events for this detector module.  This
 *                          list is used to obtain the final row, col, |Q| 
 *                          aligned list of events that is returned in the
 *                          PeakEventList object.
 *  @return  A PeakEventList object containing the specified peak object and
 *           the list of events associated with this peak, in a row,col,|Q|
 *           aligned region around the peak.  If fewer than 5 events are in 
 *           the region this returns null.
 */
  public static PeakEventList GetPeakEventList_Q_Aligned( 
                                           Peak_new          peak,
                                           float             tolerance,
                                           Tran3D            UB_inverse,
                                           Vector<EventInfo> ev_list_for_hkl,
                                           Vector<EventInfo> ev_list_for_det )
  {
    Vector3D target_hkl = new Vector3D( Math.round(peak.h()),
                                        Math.round(peak.k()),
                                        Math.round(peak.l()) );
    SampleOrientation samp_or = peak.getSampleOrientation();
    Tran3D gonio_inverse = samp_or.getGoniometerRotationInverse();

    //
    // first, find the row, col and |Q| extent of the peak, based on a subset
    // of the event list for the specified hkl ONLY.
    //
    Tran3D to_hkl = new Tran3D( UB_inverse );
    to_hkl.multiply_by( gonio_inverse );

    int   min_row   =  10000;
    int   max_row   = -10000;
    int   min_col   =  10000;
    int   max_col   = -10000;
    float min_mag_Q =  10000;
    float max_mag_Q = -10000;
    Vector3D q_vec;
    Vector3D hkl = new Vector3D();

    int event_count = 0; 

    for ( int i_count = 0; i_count < ev_list_for_det.size(); i_count++ )
    {
      EventInfo info = ev_list_for_det.elementAt(i_count);

      q_vec = info.VecQ_over_2PI();
      to_hkl.apply_to( q_vec, hkl );

      hkl.subtract( target_hkl );
      if ( hkl.length() < tolerance )
      {
        event_count++;
        int   row   = info.Row();
        int   col   = info.Col();
        float mag_Q = info.MagQ_over_2PI();
        if ( row < min_row )
          min_row = row;
        if ( row > max_row )
          max_row = row;
        if ( col < min_col )
          min_col = col;
        if ( col > max_col )
          max_col = col;
        if ( mag_Q > max_mag_Q )
          max_mag_Q = mag_Q;
        if ( mag_Q < min_mag_Q )
          min_mag_Q = mag_Q;
      }
    }

    if ( event_count < 5 )
      return null;

    // Adjust the row and column range to be centered on the peak's row 
    // and column.
    float row_radius =  (max_row - min_row + 1f) / 2;
    max_row = (int)Math.ceil ( peak.y() + row_radius );    
    min_row = (int)Math.floor( peak.y() - row_radius );    

    float col_radius =  (max_col - min_col + 1f) / 2;
    max_col = (int)Math.ceil ( peak.x() + col_radius );
    min_col = (int)Math.floor( peak.x() - col_radius );

    // Expand range down to make room for the "tail" toward smaller Q values.
    float q_range = max_mag_Q - min_mag_Q;
    max_mag_Q += q_range/5;
    min_mag_Q -= 2*q_range/5;

    //
    // Now scan through all events in the list of events for the whole 
    // detector, and only keep those that are within the specified ranges
    // of row, col and mag_Q.
    //
    Vector<EventInfo> ev_list = new Vector<EventInfo>();
    for ( int i_count = 0; i_count < ev_list_for_det.size(); i_count++ )
    {
      EventInfo info = ev_list_for_det.elementAt(i_count);
      int row     = info.Row();
      int col     = info.Col();
      if ( row   >= min_row    &&  row   <= max_row  &&
           col   >= min_col    &&  col   <= max_col   )
      {
         float mag_Q = info.MagQ_over_2PI();
         if ( mag_Q >= min_mag_Q  &&  mag_Q <= max_mag_Q   )
           ev_list.add( info );
      }
    }

    PeakEventList result = new PeakEventList( peak, ev_list,
                                              min_row, max_row,
                                              min_col, max_col,
                                              min_mag_Q, max_mag_Q );
    return result;
  }


/**
 *  Get a list of events in a detector row, column and TOF aligned block, 
 *  corresponding to the specified peak, considering the specified tolerance.
 *  First, the preliminary event list for the specified peak is scanned 
 *  to find the min & max of row, col and TOF for all events whose hkl is 
 *  within the specified tolerance of that hkl.  Then ALL the events for 
 *  the detector are scanned to find events that are within that range of 
 *  row, col and TOF values.  Events passing this requirement are added to 
 *  the list of EventInfo objects included in the PeakEventList object that
 *  is returned by this method.  
 *  NOTE: The tolerance on hkl should be no larger than 1/2.
 *
 *  @param peak             A Peak_new object with information about this
 *                          peak, including h,k,l and the sample orientation
 *  @param tolerance        Tolerance on h,k,l to use when determining the
 *                          range of row,col,|Q| for events to be associated
 *                          with this peak (i.e. be included in the 
 *                          PeakEventList that is returned).
 *  @param UB_inverse       The matrix that maps Q/(2PI) to HKL
 *  @param ev_list_for_hkl  List of events that are closer to this peak's hkl
 *                          than to any other hkl.  These events are the only
 *                          events checked when scanning for events within
 *                          the specified tolerance on hkl.  
 *  @param ev_list_for_det  List of ALL events for this detector module.  This
 *                          list is used to obtain the final row, col, |Q| 
 *                          aligned list of events that is returned in the
 *                          PeakEventList object.
 *  @return  A PeakEventList object containing the specified peak object and
 *           the list of events associated with this peak, in a row,col,|Q|
 *           aligned region around the peak.  If fewer than 5 events are in 
 *           the region this returns null.
 */
  public static PeakEventList GetPeakEventList_TOF_Aligned(
                                           Peak_new          peak,
                                           float             tolerance,
                                           Tran3D            UB_inverse,
                                           Vector<EventInfo> ev_list_for_hkl,
                                           Vector<EventInfo> ev_list_for_det )
  {
    Vector3D target_hkl = new Vector3D( Math.round(peak.h()),
                                        Math.round(peak.k()),
                                        Math.round(peak.l()) );
    SampleOrientation samp_or = peak.getSampleOrientation();
    Tran3D gonio_inverse = samp_or.getGoniometerRotationInverse();

    //
    // first, find the row, col and |Q| extent of the peak, based on a subset
    // of the event list for the specified hkl ONLY.
    //
    Tran3D to_hkl = new Tran3D( UB_inverse );
    to_hkl.multiply_by( gonio_inverse );

    int   min_row   =  10000;
    int   max_row   = -10000;
    int   min_col   =  10000;
    int   max_col   = -10000;
    float min_mag_Q =  10000;
    float max_mag_Q = -10000;
    float min_tof   =  (float)1.0e20;
    float max_tof   = -1;
    Vector3D q_vec;
    Vector3D hkl = new Vector3D();

    int event_count = 0;

    for ( int i_count = 0; i_count < ev_list_for_det.size(); i_count++ )
    {
      EventInfo info = ev_list_for_det.elementAt(i_count);

      q_vec = info.VecQ_over_2PI();
      to_hkl.apply_to( q_vec, hkl );

      hkl.subtract( target_hkl );
      if ( hkl.length() < tolerance )
      {
        event_count++;
        int   row   = info.Row();
        int   col   = info.Col();
        float mag_Q = info.MagQ_over_2PI();
        float tof   = info.Tof();
        if ( row < min_row )
          min_row = row;
        if ( row > max_row )
          max_row = row;
        if ( col < min_col )
          min_col = col;
        if ( col > max_col )
          max_col = col;
        if ( mag_Q > max_mag_Q )
          max_mag_Q = mag_Q;
        if ( mag_Q < min_mag_Q )
          min_mag_Q = mag_Q;
        if ( tof > max_tof )
          max_tof = tof;
        if ( tof < min_tof )
          min_tof = tof;
      }
    }

    if ( event_count < 5 )
      return null;

    // Adjust the row and column range to be centered on the peak's row 
    // and column.
    float row_radius =  (max_row - min_row + 1f) / 2;
    max_row = (int)Math.ceil ( peak.y() + row_radius );
    min_row = (int)Math.floor( peak.y() - row_radius );

    float col_radius =  (max_col - min_col + 1f) / 2;
    max_col = (int)Math.ceil ( peak.x() + col_radius );
    min_col = (int)Math.floor( peak.x() - col_radius );

    // Expand range down to make room for the "tail" toward smaller Q values.
    float q_range = max_mag_Q - min_mag_Q;
    max_mag_Q +=   q_range/5;
    min_mag_Q -= 2*q_range/5;

    // Expand TOF range upward to make room for the "tail".
    float tof_range = max_tof - min_tof;
    min_tof -=   tof_range/5;
    max_tof += 2*tof_range/5;

    //
    // Now scan through all events in the list of events for the whole 
    // detector, and only keep those that are within the specified ranges
    // of row, col and mag_Q.
    //
    Vector<EventInfo> ev_list = new Vector<EventInfo>();
    for ( int i_count = 0; i_count < ev_list_for_det.size(); i_count++ )
    {
      EventInfo info = ev_list_for_det.elementAt(i_count);
      int row     = info.Row();
      int col     = info.Col();
      if ( row   >= min_row    &&  row   <= max_row  &&
           col   >= min_col    &&  col   <= max_col   )
      {
         float tof = info.Tof();
         if ( tof >= min_tof  &&  tof <= max_tof )
           ev_list.add( info );
      }
    }

    PeakEventList result = new PeakEventList( peak, ev_list,
                                              min_row, max_row,
                                              min_col, max_col,
                                              min_mag_Q, max_mag_Q );
    return result;
  }


/**
 *  Get I and sigI by integrating the data in the histo_array using 25 slices,
 *  centered on the peak.  This assumes that the histogram array came from 
 *  the PeakEventList.get*Histogram() method, using 25 slices to cover the 
 *  full range of data in the |Q|/tof direction.  The total counts on the
 *  first 5 and last 5 disks provide the background estimate and the total
 *  counts in the middle three slices provide the peak + background estimate. 
 */
  public static float[] SumIntegrateHistogram_25( float[][][] histo_array )
  {
    int n_pages = histo_array.length;
    float[] sums = new float[ n_pages ];
    for ( int page = 0; page < n_pages; page++ )
    {
      sums[page] = 0;
      float[][] arr = histo_array[page];
      for ( int row = 0; row < histo_array[0].length; row++ )
        for ( int col = 0; col < histo_array[0][0].length; col++ )
          sums[page] += arr[row][col];

//      System.out.println("Page = " + page + "   Sum = " + sums[page] );
    }

// HACK: Currently assumes 25 pages in histogram

    float back     = 0;
    int   back_vol = 0;
    for ( int page = 0; page < 5; page++ )
    {
      back += sums[page];
      back += sums[ n_pages - 1 - page ];
      back_vol += 2;
    }

    float raw_signal = 0;
    int   raw_vol    = 0;
    for ( int page = 5; page < n_pages-5; page++ )
    {
      raw_signal += sums[page];
      raw_vol++;
    }

    float ratio  = (float)raw_vol/(float)back_vol;
    float signal = raw_signal - ratio * back;

    float sigma_signal = (float)Math.sqrt( raw_signal + ratio * ratio * back );

    float[] result = { signal, sigma_signal };
    return result;
  } 


/**
 *  Get I and sigI by integrating the data in the histo_array using 5 disks,
 *  centered on the peak.  The total counts on the first and last disk 
 *  provide the background estimate and the total counts in the middle three
 *  disks provide the peak + background estimate. 
 *  
 * @param pev_list   The peak event list with the peak events to integrate
 * @param radius     The radius of the disks on the detector face that will
 *                   be used
 */
  public static float[] CylinderIntegrate_5( PeakEventList pev_list,
                                             float         radius    )
  {
    int n_pages = 5;
    Histogram3D peak_histo = 
                       pev_list.getCenteredCircleHistogram( radius, n_pages );

    float[][][] histo_array = peak_histo.getHistogramArray();

    float[] sums = new float[ n_pages ];
    for ( int page = 0; page < n_pages; page++ )
    {
      sums[page] = 0;
      float[][] arr = histo_array[page];
      for ( int row = 0; row < histo_array[0].length; row++ )
        for ( int col = 0; col < histo_array[0][0].length; col++ )
          sums[page] += arr[row][col];
    }

    float back     = sums[0] + sums[4];
    int   back_vol = 2;

    float raw_signal = sums[1] + sums[2] + sums[3];
    int   raw_vol    = 3;

    float ratio  = (float)raw_vol/(float)back_vol;
    float signal = raw_signal - ratio * back;

    float sigma_signal = (float)Math.sqrt( raw_signal + ratio * ratio * back );

    float[] result = { signal, sigma_signal };
    return result;
  }


/**
 *  Find the total counts inside and outside of a circle of the specified
 *  radius, centered on the specified row and column.  Also find the number
 *  of pixels inside and outside of the circle. 
 *
 *  @param page       2-D array containing one "page" of a 3D histogram
 *  @param radius     The radius (in row, col number) of the circle that
 *                    contains the peak.
 *  @param center_row The row number at the center of the peak.
 *  @param center_col The column number at the center of the peak.
 *
 *  @return an array of floats with four entries: the peak counts, the number
 *          of peak pixels, the background counts and the number of background
 *          pixels.
 */
  public static float[] IntegrateSlice( float[][] page,   float radius,
                                        float center_row, float center_col )
  {
    float radius_2 = radius * radius;
    float peak     = 0;
    float back     = 0;
    float peak_vol = 0;   
    float back_vol = 0;   
    for ( int row = 0; row < page.length; row++ )
      for ( int col = 0; col < page[0].length; col++ )
      {
        float d_row = row - center_row;
        float d_col = col - center_col;
        float d_sqrd = d_row * d_row + d_col * d_col;
        if ( d_sqrd <= radius_2 )
        {
          peak     += page[row][col];
          peak_vol += 1;
        }
        else
        {
          back     += page[row][col];
          back_vol += 1;
        }
      } 
      
     float[] result = { peak, peak_vol, back, back_vol };
     return result;
  }


  public static float[] IntegrateSlices( PeakEventList pev_list,
                                         float         radius    )
  {
    int n_pages = 7;
//  float[] radii_scale = {  .4f,  .6f,  .8f, 1.0f,  .8f,  .6f,  .4f }; // cone in
    float[] radii_scale = { 1.6f, 1.4f, 1.2f, 1.0f, 1.2f, 1.4f, 1.6f }; // cone out 
//  float[] radii_scale = { 1, 1, 1, 1, 1, 1, 1 };                      // cylinder

    boolean debug = false;
/*
    Peak_new peak = pev_list.getPeak();
    if ( Math.round(peak.h()) == 17  &&
         Math.round(peak.k()) == 10  &&
         Math.round(peak.l()) == 15  )
      debug = false;
*/

    Histogram3D peak_histo = pev_list.getFullHistogram( n_pages );
    float[][][] histo_array = peak_histo.getHistogramArray();

                                         // bound the radius to be at most
                                         // half the number of rows and columns
    int n_rows = histo_array[0].length;
    int n_cols = histo_array[0][0].length;
    if ( radius >= n_rows/2 )
      radius = n_rows/2;
    if ( radius >= n_cols/2 )
      radius = n_cols/2;

    float center_row = pev_list.getCenterRow() - pev_list.getMinRow();
    float center_col = pev_list.getCenterCol() - pev_list.getMinCol();
    float back       = 0;
    float back_vol   = 0;
    float raw_signal = 0;
    float raw_vol    = 0;
    for ( int page = 0; page < n_pages; page++ )
    {
      float[][] arr = histo_array[page];
      float[] slice_info = IntegrateSlice( arr, radius*radii_scale[page], 
                                           center_row, center_col );
      raw_signal += slice_info[0];
      raw_vol    += slice_info[1];
      back       += slice_info[2];
      back_vol   += slice_info[3];
    }

    float ratio  = (float)raw_vol/(float)back_vol;
    float signal = raw_signal - ratio * back;

    float sigma_signal = (float)Math.sqrt( raw_signal + ratio * ratio * back );

    float[] result = { signal, sigma_signal };

    if ( debug )
    {
      System.out.println( "Num Rows       = " + histo_array[0].length );
      System.out.println( "Num Cols       = " + histo_array[0][0].length );
      System.out.println( "pev min_row    = " + pev_list.getMinRow() );
      System.out.println( "pev max_row    = " + pev_list.getMaxRow() );
      System.out.println( "pev center row = " + pev_list.getCenterRow() );
      System.out.println( "pev min_col    = " + pev_list.getMinCol() );
      System.out.println( "pev max_col    = " + pev_list.getMaxCol() );
      System.out.println( "pev center col = " + pev_list.getCenterCol() );
      System.out.println( "center_row     = " + center_row );
      System.out.println( "center_col     = " + center_col );
      System.out.println( "raw signal     = " + raw_signal );
      System.out.println( "raw vol        = " + raw_vol );
      System.out.println( "back           = " + back );
      System.out.println( "back vol       = " + back_vol );
      System.out.println( "ratio          = " + ratio );
      System.out.println( "signal         = " + signal );
      System.out.println( "sigma_signal   = " + sigma_signal );
    }
    return result;
  }


/**
 * Calculate some basic statistics for a list of values stored
 * in increasing order.  Currently, the average and variance are
 * calculated and returned as the first two entries in the
 * returned array.
 */
  public static float[] Stats( float[] vals, float fraction )
  {
    if ( vals.length <= 0 )
      return new float[2];

    if ( fraction > 1 )
      fraction = 1;
    else if ( fraction < 0 )
      fraction = 0;

    int N = (int)(vals.length * fraction);
    if ( N < 1 )
      N = 1;

    float sum    = 0;
    float sum_sq = 0;
    float val;
    for ( int i = 0; i < N; i++ )
    {
      val     = vals[i];
      sum    += val;
      sum_sq += val * val;
    }

    float avg      = sum / N;
    float variance = sum_sq/N - avg * avg;
    float[] result = { avg, variance };
    return result;
  }


/**
 * Calculate some basic statistics for all initial sublists for 
 * a list of values stored in increasing order.  Currently, 
 * the average of the first k+1 values is returned in 
 * result[0][k] and variance of the first k+1 values is returned in
 * result[1][k].
 */
  public static float[][] SublistStats( float[] vals )
  {
    if ( vals.length <= 0 )
      return new float[2][0];

    float[][] result = new float[2][vals.length];

    float sum    = 0;
    float sum_sq = 0;
    float val;
    float avg;
    float variance;
    for ( int i = 0; i < vals.length; i++ )
    {
      val     = vals[i];
      sum    += val;
      sum_sq += val * val;
      avg      = sum / vals.length;
      variance = sum_sq/vals.length - avg * avg;
      result[0][i] = avg;
      result[1][i] = variance;
    }

    return result;
  }


  public static float[] IntegrateRegion( PeakEventList pev_list,
                                         int border, 
                                         int num_rows, int num_cols, 
                                         int num_slices,
                                         boolean use_Q )
  {
    Histogram3D peak_histo = pev_list.getBoundedRebinnedHistogram( 
                                             border, 
                                             num_rows, num_cols, num_slices,
                                             use_Q );

    float[][][] histo_array = peak_histo.getHistogramArray();
    int n_rows = histo_array[0].length;
    int n_cols = histo_array[0][0].length;
    int n_bins = num_slices * n_rows * n_cols;
    float[] vals = new float[ n_bins ];

    int index = 0;
    for ( int page = 0; page < num_slices; page++ )
    {
      float[][] arr = histo_array[page];
      for ( int row = 0; row < n_rows; row++ )
      {
        float[] row_arr = arr[row];
         for ( int col = 0; col < n_cols; col++ )
           vals[index++] = row_arr[col];
      }
    }

    float[] points = new float[12];
    float power_2 = 0.5f;
    for ( int i = 0; i < points.length-1; i++ )
    {
      points[i] = 1 - power_2;
      power_2 /= 2;
    }
    points[points.length-1] = 1;

    Arrays.sort( vals );
    System.out.printf("n_rows, cols = %4d %4d %7d\n", n_rows, n_cols, n_bins );

    float[] threshold = new float[points.length];
    float[] average   = new float[points.length];
    float[] variance  = new float[points.length];

    float[][] sublist_stats = SublistStats(vals);

    for ( int i = 0; i < points.length; i++ )
      System.out.printf("%8.6f ", points[i] );
    System.out.println();

    for ( int i = 0; i < points.length; i++ )
    {
      index = (int)(points[i]*(n_bins-1));
      threshold[i] = vals[ index ];
      average[i]   = sublist_stats[0][index];
      variance[i]  = sublist_stats[1][index];
    }

    for ( int i = 0; i < points.length; i++ )
      System.out.printf("%8.0f ", threshold[i] );
    System.out.println();

    for ( int i = 0; i < points.length; i++ )
      System.out.printf("%8.3f ", average[i] );
    System.out.println();

    for ( int i = 0; i < points.length; i++ )
      System.out.printf("%8.3f ", variance[i] );
    System.out.println();
                                            // Work BACKWARDS through list
                                            // of values to find the last
                                            // one where variance > avg
    int k = sublist_stats[0].length - 1;       
    while ( k >= 0 && 
           (sublist_stats[1][k] > sublist_stats[0][k]) )
      k--;

    k++;
 
    float cutoff = 0;
    if ( k >= vals.length )
    {
      cutoff = vals[vals.length-1];
      k = vals.length-1;
    }
    else
      cutoff = vals[k];

    System.out.println("Cutoff = " + cutoff + " at position " + k );

    float signal     = 0;
    float signal_vol = 0;
    float back       = 0;
    float back_vol   = 0;

    for ( int i = 0; i < k; i++ )
    {
      back += vals[i];
      back_vol += 1;
    }

    for ( int i = k; i < vals.length; i++ )
    {
      signal += vals[i];
      signal_vol += 1;
    }

    float ratio = signal_vol/back_vol;
    float intI  = signal - ratio * back;
    float sigI = (float)Math.sqrt( signal + ratio * ratio * back );

    float[] result = { intI, sigI };
    return result;
  }


/**
 * Integrate an edge or corner peak.  NOTE: The peak center should be set
 * to the MAX rather than to the center of mass, before calling this 
 * method.  If the peak is an edge peak, the sums will be formed using only
 * the half of the peak that is away from the edge, and the results will be
 * doubled.  If the peak is a corner peak, the sums will be formed using 
 * only the quarter of the peak that is interior to the detector, and the
 * result will be multiplied by four.
 *
 * @param pev_list   The peak event list with the peak events to integrate
 * @param radius     The radius of the disks on the detector face that will
 *                   be used
 */
  public static float[] CylinderIntegrateEdge_5( PeakEventList pev_list,
                                                 float         radius,
                                                 PeakEventList.PeakType type )
  {
    int n_pages = 5;
    Histogram3D peak_histo =
                       pev_list.getCenteredCircleHistogram( radius, n_pages );

    float[][][] histo_array = peak_histo.getHistogramArray();
 
    int n_rows = histo_array[0].length;
    int n_cols = histo_array[0][0].length;

    boolean odd_num_rows = (n_rows % 2 == 1 );
    boolean odd_num_cols = (n_cols % 2 == 1 );

    float[] ll_sums = new float[ n_pages ];
    float[] lr_sums = new float[ n_pages ];
    float[] ul_sums = new float[ n_pages ];
    float[] ur_sums = new float[ n_pages ];
    float[] sums    = new float[ n_pages ];

    for ( int page = 0; page < n_pages; page++ )
    {
      float[][] arr = histo_array[page];
                                               // sum over lower left quadrant
      ll_sums[page] = 0;
      for ( int row = 0; row < n_rows/2; row++ )
        for ( int col = 0; col < n_cols/2; col++ )
          ll_sums[page] += arr[row][col];
       
      if ( odd_num_rows )                      // use half of center row
        for ( int col = 0; col < n_cols/2; col++ )
          ll_sums[page] += arr[n_rows/2][col] / 2;

      if ( odd_num_cols )                      // use half of center col 
        for ( int row = 0; row < n_rows/2; row++ )
          ll_sums[page] += arr[row][n_cols/2] / 2;

      if ( odd_num_rows && odd_num_cols )      // use 1/4 of center pixel
        ll_sums[page] += arr[n_rows/2][n_cols/2] / 4;

                                               // sum over lower right quadrant
      lr_sums[page] = 0;
      for ( int row = 0; row < n_rows/2; row++ )
        for ( int col = n_cols/2; col < n_cols; col++ )
          lr_sums[page] += arr[row][col];

      if ( odd_num_rows )                      // use half of center row
        for ( int col = n_cols/2; col < n_cols; col++ )
          lr_sums[page] += arr[n_rows/2][col] / 2;

      if ( odd_num_cols )                      // remove half of center col 
        for ( int row = 0; row < n_rows/2; row++ )
          lr_sums[page] -= arr[row][n_cols/2] / 2;

      if ( odd_num_rows && odd_num_cols )      // remove 1/4 of center pixel
        lr_sums[page] -= arr[n_rows/2][n_cols/2] / 4;

                                                // sum over upper left quadrant
      ul_sums[page] = 0;
      for ( int row = n_rows/2; row < n_rows; row++ )
        for ( int col = 0; col < n_cols/2; col++ )
          ul_sums[page] += arr[row][col];

      if ( odd_num_rows )                      //remove half of center row
        for ( int col = 0; col < n_cols/2; col++ )
          ul_sums[page] -= arr[n_rows/2][col] / 2;

      if ( odd_num_cols )                      // use half of center col 
        for ( int row = n_rows/2; row < n_rows; row++ )
          ul_sums[page] += arr[row][n_cols/2] / 2;

      if ( odd_num_rows && odd_num_cols )      // remove 1/4 of center pixel
        ul_sums[page] -= arr[n_rows/2][n_cols/2] / 4;

                                              // sum over upper right quadrant
      ur_sums[page] = 0;
      for ( int row = n_rows/2; row < n_rows; row++ )
        for ( int col = n_cols/2; col < n_cols; col++ )
          ur_sums[page] += arr[row][col];
        
      if ( odd_num_rows )                      //remove half of center row
        for ( int col = n_cols/2; col < n_cols; col++ )
          ur_sums[page] -= arr[n_rows/2][col] / 2;

      if ( odd_num_cols )                      // remove half of center col 
        for ( int row = n_rows/2; row < n_rows; row++ )
          ur_sums[page] -= arr[row][n_cols/2] / 2;

      if ( odd_num_rows && odd_num_cols )      // use 1/4 of center pixel
        ur_sums[page] += arr[n_rows/2][n_cols/2] / 4;

/*                                               // sum over all quadrants
      sums[page] = 0;
      for ( int row = 0; row < histo_array[0].length; row++ )
        for ( int col = 0; col < histo_array[0][0].length; col++ )
          sums[page] += arr[row][col];

      System.out.println("PAGE = " + page );
      System.out.println("ll, lr, ul, ur, total = " + 
                          ll_sums[page] + ", " +
                          lr_sums[page] + ", " +
                          ul_sums[page] + ", " +
                          ur_sums[page] + ", " +
                          sums[page] );
*/
    }

    for ( int page = 0; page < n_pages; page++ )
    {
      if ( type == PeakEventList.PeakType.INTERIOR )
        sums[page] = ul_sums[page] + ur_sums[page] +
                     ll_sums[page] + lr_sums[page];

      else if ( type == PeakEventList.PeakType.LEFT_EDGE )
        sums[page] = 2 * ( ul_sums[page] +
                           ll_sums[page] );

      else if ( type == PeakEventList.PeakType.RIGHT_EDGE )
        sums[page] = 2 * (            ur_sums[page] +
                                      lr_sums[page] );

      else if ( type == PeakEventList.PeakType.BOTTOM_EDGE )
        sums[page] = 2 * (
                           ll_sums[page] + lr_sums[page] );

      else if ( type == PeakEventList.PeakType.TOP_EDGE )
        sums[page] = 2 * ( ul_sums[page] + ur_sums[page] );

      else if ( type == PeakEventList.PeakType.TOP_LEFT )
        sums[page] = 4 * ( ul_sums[page] );

      else if ( type == PeakEventList.PeakType.TOP_RIGHT )
        sums[page] = 4 * (                 ur_sums[page] );

      else if ( type == PeakEventList.PeakType.BOTTOM_LEFT )
        sums[page] = 4 * (
                           ll_sums[page] );

      else if ( type == PeakEventList.PeakType.BOTTOM_RIGHT )
        sums[page] = 4 * ( 
                                     lr_sums[page] );
    }

    float back     = sums[0] + sums[4];
    int   back_vol = 2;

    float raw_signal = sums[1] + sums[2] + sums[3];
    int   raw_vol    = 3;

    float ratio  = (float)raw_vol/(float)back_vol;
    float signal = raw_signal - ratio * back;

    float sigma_signal = (float)Math.sqrt( raw_signal + ratio * ratio * back );

    float[] result = { signal, sigma_signal };
    return result;
  }

/**
 * Integrate and edge or corner peak.  NOTE: The peak center should be set
 * to the MAX rather than to the center of mass, before calling this 
 * method.  If the peak is an edge peak, the sums will be formed using only
 * the half of the peak that is away from the edge, and the results will be
 * doubled.  If the peak is a corner peak, the sums will be formed using 
 * only the quarter of the peak that is interior to the detector, and the
 * result will be multiplied by four.
 *
 * @param pev_list   The peak event list with the peak events to integrate
 * @param radius     The radius of the disks on the detector face that will
 *                   be used
 */
  public static float[] CylinderIntegrateRefEdge_5( 
                                          PeakEventList pev_list,
                                          float         radius,
                                          int           border,
                                          PeakEventList.PeakType type )
  {
                                     // center row, col in detector coordinates
    int center_row = (int)pev_list.getCenterRow();
    int center_col = (int)pev_list.getCenterCol();
    int n_pages    = 5;

    Histogram3D peak_histo =
                       pev_list.getCenteredCircleHistogram( radius, n_pages );

    float[][][] histo_array = peak_histo.getHistogramArray();
    int n_rows  = histo_array[0].length;
    int n_cols  = histo_array[0][0].length;

    int min_row = (int)peak_histo.yEdgeBinner().axisMin();
    int min_col = (int)peak_histo.xEdgeBinner().axisMin();
    int max_row = (int)peak_histo.yEdgeBinner().axisMax();
    int max_col = (int)peak_histo.xEdgeBinner().axisMax();
/*
    System.out.println("Min, Max Row = " + min_row + ", " + max_row );
    System.out.println("Min, Max Col = " + min_col + ", " + max_col );
    System.out.println("Center Col, Row = " + center_col + ", " + center_row );
    System.out.println("ORIGINAL PAGE.....");

    for ( int row = n_rows-1; row >= 0; row-- )
    {
      float arr[] = histo_array[2][row];
      for (int col = 0; col < n_cols; col++ )
        System.out.printf( "%3d ", Math.round(arr[col]) );
      System.out.println();
    }
*/
                                       // Reflect across left edge, if needed
    if ( type == PeakEventList.PeakType.LEFT_EDGE   ||
         type == PeakEventList.PeakType.BOTTOM_LEFT ||
         type == PeakEventList.PeakType.TOP_LEFT  )
    {
      int last_bad = n_cols/2 - (center_col - border);
      if ( last_bad > n_cols/2 )
        last_bad = n_cols/2;
      for ( int page = 0; page < n_pages; page++ )
      {
        for ( int row = 0; row < n_rows; row++ )
        {                                           // copy data from other
          float[] arr = histo_array[page][row];     // side to missing positons
          for ( int col = 0; col <= last_bad; col++ )   
            arr[col] = arr[n_cols - 1 - col];
        }  
      }
    }
                                       // Reflect across right edge, if needed
    if ( type == PeakEventList.PeakType.RIGHT_EDGE   ||
         type == PeakEventList.PeakType.BOTTOM_RIGHT ||
         type == PeakEventList.PeakType.TOP_RIGHT  )
    {
      int first_bad = 256 - border - center_col + (n_cols+1) / 2;
      if ( first_bad <= (n_cols+1)/2 )
        first_bad = (n_cols+1)/2 + 1;
      for ( int page = 0; page < n_pages; page++ )
      {
        for ( int row = 0; row < n_rows; row++ )
        {                                           // copy data from other
          float[] arr = histo_array[page][row];     // side to missing positons
          for ( int col = first_bad; col < n_cols; col++ )   
            arr[col] = arr[n_cols - 1 - col];
        } 
      }
    }

                                    // Reflect across bottom edge, if needed
    if ( type == PeakEventList.PeakType.BOTTOM_EDGE  ||
         type == PeakEventList.PeakType.BOTTOM_LEFT  ||
         type == PeakEventList.PeakType.BOTTOM_RIGHT  )
    {
      int last_bad = n_rows/2 - (center_row - border);
      if ( last_bad > n_rows/2 )
        last_bad = n_rows/2;
      for ( int page = 0; page < n_pages; page++ )
      {
        float[][] arr = histo_array[page];
        for ( int col = 0; col < n_cols; col++ )
        {                                           // copy data from other
                                                    // side to missing positons
          for ( int row = 0; row <= last_bad; row++ )   
            arr[row][col] = arr[n_rows - 1 - row][col];
        } 
      }
    }
     
                                      // Reflect across top edge, if needed
    if ( type == PeakEventList.PeakType.TOP_EDGE  ||
         type == PeakEventList.PeakType.TOP_LEFT  ||
         type == PeakEventList.PeakType.TOP_RIGHT  )
    { 
      int first_bad = 256 - border - center_row + (n_rows+1) / 2;
      if ( first_bad <= (n_rows+1)/2 )
        first_bad = (n_rows+1)/2 + 1;
      for ( int page = 0; page < n_pages; page++ )
      { 
        float[][] arr = histo_array[page];
        for ( int col = 0; col < n_cols; col++ )
        {                                           // copy data from other
                                                    // side to missing positons
          for ( int row = first_bad; row < n_rows; row++ ) 
            arr[row][col] = arr[n_rows - 1 - row][col];
        }
      }
    }
/*
    System.out.println("FIXED PAGE.....");
    for ( int row = n_rows-1; row >= 0; row-- )
    {
      float arr[] = histo_array[2][row];
      for (int col = 0; col < n_cols; col++ )
        System.out.printf( "%3d ", Math.round(arr[col]) );
      System.out.println();
    }
    System.out.println();
*/
    float[] sums = new float[ n_pages ];
    for ( int page = 0; page < n_pages; page++ )
    {
      sums[page] = 0;
      float[][] arr = histo_array[page];
      for ( int row = 0; row < histo_array[0].length; row++ )
        for ( int col = 0; col < histo_array[0][0].length; col++ )
          sums[page] += arr[row][col];
    }

    float back     = sums[0] + sums[4];
    int   back_vol = 2;

    float raw_signal = sums[1] + sums[2] + sums[3];
    int   raw_vol    = 3;

    float ratio  = (float)raw_vol/(float)back_vol;
    float signal = raw_signal - ratio * back;

    float sigma_signal = (float)Math.sqrt( raw_signal + ratio * ratio * back );

    float[] result = { signal, sigma_signal };
    return result;
  }


/**
 *  Given an array of 3D events, uniformly distributed about the origin,
 *  create the 3x3 matrix X * X_transpose and use JAMA to create the 
 *  EigenvalueDecomposition of that 3x3 matrix.  The eigenvectors of
 *  that matrix, which form the principal axes of the set of events,
 *  can be obtained from the EigenvalueDecomposition.
 *
 *  @param  ev_arr   List of events, centered around 0,0,0
 *  @param  radius   Maxium distance from 0,0,0 of any event that
 *                   will be considered.  To reduce the effect of
 *                   background events in determining the principal
 *                   axes, the radius should not be larger than the
 *                   main peak region.
 */
  public static EigenvalueDecomposition GetEigenDecomp( Vector3D[] ev_arr,
                                                        float      radius )
  {
    double[][] A_arr = new double[3][3];
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
      {
        double sum = 0;
        for ( int i = 0; i < ev_arr.length; i++ )
        {
          if ( ev_arr[i].length() <= radius )
          {
            float[] v = ev_arr[i].get();
            sum += v[row] * v[col];
          }
        }
        A_arr[row][col] = sum;
      }
/*
    System.out.println( "XX_transpose array:" );
    for ( int row = 0; row < 3; row++ )
      System.out.printf( "%12.6f    %12.6f   %12.6f\n",
                         A_arr[row][0], A_arr[row][1], A_arr[row][2] );
*/
    Matrix A = new Matrix( A_arr );
    return new EigenvalueDecomposition( A );
  }


/**
 *  Calculate the standard deviation of the given list of 3D events in
 *  the direction of the specified vector.  Only events that are within
 *  the specified radius of 0,0,0 will be considered.
 *  @param  ev_arr      List of 3D events centered at 0,0,0
 *  @param  direction   The direction vector on which the 3D events 
 *                      will be projected.
 *  @param  radius      Maximun size of event vectors that will be used
 *                      in calculating the standard deviation. 
 */
  public static float sigma( Vector3D[] ev_arr,
                             Vector3D   direction,
                             float      radius )
  {
    float sum    = 0;
    float sum_sq = 0;
    float dot_prod;
    int   count = 0;
    for ( int i = 0; i < ev_arr.length; i++ )
    {
      if ( ev_arr[i].length() <= radius )
      {
        dot_prod = ev_arr[i].dot( direction );
        sum += dot_prod;
        sum_sq += dot_prod * dot_prod;
        count++;
      }
    }
    float ave   = 0;
    float stdev = 0;
    if ( count > 0 )
    {
      ave = sum / count;
      stdev = (float)Math.sqrt( sum_sq/count - ave*ave );
    }
/*
    System.out.println( "Ave    = " + ave );
    System.out.println( "StdDev = " + stdev );
*/
    return stdev;
  }


/**
 * Calculate the number of events in an ellipsoid centered at 0,0,0 with 
 * the three specified axes and the three specified sizes in the direction 
 * of those axes.  Only events that are within the specified radius of the
 * origin will be counted.
 *
 * @param  ev_arr      List of 3D events centered at 0,0,0
 * @param  directions  List of 3 orthonormal directions for the axes of
 *                     the ellipsoid.
 * @param  sizes       List of three values a,b,c giving half the length
 *                     of the three axes of the ellisoid.
 * @return Then number of events that are in or on the specified ellipsoid.
 */
  public static int NumInEllipsoid( Vector3D[] ev_arr,
                                    Vector3D[] directions,
                                    float[]    sizes )
  {
    int   count = 0;
    float comp;
    float sum;
    for ( int i = 0; i < ev_arr.length; i++ )
    {
      sum = 0;
      for ( int k = 0; k < 3; k++ )
      {
        comp = ev_arr[i].dot( directions[k] ) / sizes[k];
        sum += comp * comp;
      }
      if ( sum <= 1 )
        count++;
    }

    return count;
  }

/**
 * Integrate the specified events by counting the number of events in
 * an ellipsoid with the specifed axes, and sizes, and subtracting a
 * background estimate based on a larger ellipsoidal shell.
 *
 * @param  directions  List of 3 orthonormal directions giving the axes of
 *                     the ellipsoid.
 * @param  sigmas      Estimates of the standard deviations in the directions
 *                     of the axes of the ellipsoid.  Unless constrained
 *                     by the radius, the ellipsoid for the peak region will
 *                     have axis half lengths, a, b, c equal to 3 times the
 *                     corresponding sigma.  The background region is the
 *                     ellipsoidal shell between the peak region and an
 *                     ellipsoid with axis half lenghts equal to 4 times the
 *                     corresponding sigma.  If 4 times the largest sigma is
 *                     more than the specified radius, the scale factor 3 and
 *                     4 will be scaled back so that the background ellipsoid
 *                     is contained within a sphere of the specified radius.
 * @param  radius      The maximum distance from the origin for an
 *                     event to be counted.  This should be roughly twice
 *                     the radius of the peak region.
 * @return an array with two values, the net integrated intensity, intI and
 *         sigI, the estimated standard deviation for intI.
 */
  public static float[] Integrate( Vector3D[] ev_arr,
                                   Vector3D[] directions,
                                   float[]    sigmas,
                                   float      radius )
  {
    float r1 = 3;          // rk is the multiple of the sigma value that
    float r2 = 3;          // will be used for the half-axis size of an 
    float r3 = 4;          // ellipse.  r1 is for the peak, r2 is for
                           // the inner surface of the background shell
                           // and r3 is for the outer surface of the
                           // background shell.

    float max_sigma = sigmas[0];
    for ( int i = 1; i < 3; i++ )
      if ( sigmas[i] > max_sigma )
        max_sigma = sigmas[i];

    if ( r3 * max_sigma > radius )
    {
      r3 = radius/max_sigma;
      r2 = r3 * 0.75f;
      r1 = r2;
      System.out.println("Reduced sizes to " + r1 + ", " + r2 + ", " + r3);
    }

    float[] new_sigmas = new float[3];

    for (int i = 0; i < 3; i++ )
      new_sigmas[i] = r1*sigmas[i];
    float peak_w_back = NumInEllipsoid( ev_arr, directions, new_sigmas );

    for (int i = 0; i < 3; i++ )
      new_sigmas[i] = r2*sigmas[i];
    float back1 = NumInEllipsoid( ev_arr, directions, new_sigmas );

    for (int i = 0; i < 3; i++ )
      new_sigmas[i] = r3*sigmas[i];
    float back2 = NumInEllipsoid( ev_arr, directions, new_sigmas );

    float backgrd = back2 - back1;

    float ratio = (float)(Math.pow(r1,3) / (Math.pow(r3,3) - Math.pow(r2,3)));

    float intI = peak_w_back - ratio * backgrd;
    float sigI = (float)Math.sqrt( peak_w_back + ratio * ratio * backgrd );

    float[] result = { intI, sigI };

    System.out.println( "backgrd = " + backgrd + "  ratio = " + ratio +
                        "   peak_w_back = " + peak_w_back );

    return result;
  }


  public static float[] EllipseIntegrate( PeakEventList pevl, float radius )
  {
    Vector3D[] ev_arr = pevl.GetZeroCenteredEventQs( radius );

    EigenvalueDecomposition EV_Decomp = GetEigenDecomp( ev_arr, radius/2 );

    Matrix V = EV_Decomp.getV();
    double[][] V_arr = V.getArray();

    float[] sigmas        = new float[3];
    Vector3D[] directions = new Vector3D[3];
    for ( int k = 0; k < 3; k++ )
    {
      Vector3D direction = new Vector3D( (float)(V_arr[0][k]),
                                         (float)(V_arr[1][k]),
                                         (float)(V_arr[2][k])  );
      float stdev = sigma( ev_arr, direction, radius*0.75f );
      sigmas[k]      = stdev;
      directions[k] = direction;
      System.out.println("\nFor Direction " + k + " stdev = " + stdev );
    }

    return Integrate( ev_arr, directions, sigmas, radius );
  }


/**
 * Get a PeakDisplayInfo object for the specified peak.
 *
 * @param pev_list   The PeakEventList object containing most information
 *                   about this peak.
 * @param radius     The radius of a disk on the detector surface for which 
 *                   data will be included in the PeakDisplayInfo object
 * @param seq_num    (Possibly) new sequence number to use in the label for 
 *                   the peak.
 */
  public static PeakDisplayInfo GetPeakDisplayInfo( PeakEventList pev_list, 
                                                    float         radius,
                                                    int           seq_num )
  {
     Histogram3D peak_histo = pev_list.getCenteredCircleHistogram( radius, 5 );
     float[][][] histo_array = peak_histo.getHistogramArray();

     // flip the rows to fix the row readout values

     int n_pages = histo_array.length;
     for ( int page = 0; page < n_pages; page++ )
     {
       float[][] array_2D = histo_array[page];
       int n_rows  = array_2D.length;
       int n_cols  = array_2D[0].length;
       float[] temp;
       for ( int row = 0; row < n_rows/2; row++ )
       {
         temp = array_2D[row];
         array_2D[row] = array_2D[n_rows-row-1];
         array_2D[n_rows-row-1] = temp;
       }
     }

     int min_row = (int)peak_histo.yEdgeBinner().axisMin();
     int min_col = (int)peak_histo.xEdgeBinner().axisMin();

     Peak_new peak = pev_list.getPeak();
     String key = "" + Math.round( peak.h() ) + "," +
                       Math.round( peak.k() ) + "," +
                       Math.round( peak.l() );

     String title = "" + seq_num + ": " + key;
     PeakDisplayInfo peak_info = new PeakDisplayInfo( title, histo_array,
                                                   min_row, min_col, 0, true );
     return peak_info;
  }


/**
 *  Display images of peaks from multiple detectors from one run.
 *
 *  @param run_num              The run number for the peaks
 *  @param id_with_peak_infos   Vector containing two entries for
 *                              each detector.  The first entry is the
 *                              detector ID.  The second entry is a Vector
 *                              of PeakDisplayInfo objects.
 */
  public static void ShowPeakImages( int run_num, Vector id_with_peak_infos )
  {
     PeakArrayPanels peaks_display = new PeakArrayPanels(
                                  "Integrated Peaks For Run " + run_num );
     
     Vector<PeakDisplayInfo> peak_infos = null;

     for ( int i = 0; i < id_with_peak_infos.size(); i+=2 )
     {
       int id = (Integer)id_with_peak_infos.elementAt(i);
       peak_infos = (Vector<PeakDisplayInfo>)id_with_peak_infos.elementAt(i+1);

       PeakDisplayInfo[] infos_for_det = new PeakDisplayInfo[peak_infos.size()];
       for ( int k = 0; k < infos_for_det.length; k++ )
          infos_for_det[k] = peak_infos.elementAt(k);

       PeaksDisplayPanel ppanel = new PeaksDisplayPanel( infos_for_det );
       peaks_display.addPanel( ppanel, "", ""+id );
     }

     peaks_display.display( "Detector", "ID" );
  }

}

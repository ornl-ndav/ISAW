package EventTools.Integrate;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

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
 *  Get a list of events in a detector row and column aligned block, that
 *  correspond to the specified peak, considering the specified tolerance.
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
 *           aligned region around the peak.
 */
  public static PeakEventList GetPeakEventList( 
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
    for ( int i_count = 0; i_count < ev_list_for_det.size(); i_count++ )
    {
      EventInfo info = ev_list_for_det.elementAt(i_count);

      q_vec = info.VecQ_over_2PI();
      to_hkl.apply_to( q_vec, hkl );

      hkl.subtract( target_hkl );
      if ( hkl.length() < tolerance )
      {
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

    // HACK: Expand range down to make room for the "tail" toward
    // smaller Q values.
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
 *  Get I and sigI by integrating the data in the histo_array.  The 
 *  histo_array is assume to ONLY contain events that should be included
 *  when finding the total integrated intensity and background.
 */
  public static float[] Integrate( float[][][] histo_array )
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

}

/* 
 * File: PeakEventList.java
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
 *  $Date: 2012/09/07 15:40:55 $            
 *  $Revision: 1.2 $
 */

package EventTools.Integrate;

import java.util.*;

import EventTools.EventList.*;
import EventTools.Histogram.*;

import gov.anl.ipns.MathTools.Geometry.*;

import DataSetTools.operator.Generic.TOF_SCD.Peak_new;


/**
 *  This class manages a Peak_new object and a list of EventInfo objects
 *  corresponding to that peak.
 */
public class PeakEventList 
{
  private Peak_new     peak    = null;
  private EventInfo[]  ev_list = null;
  private int          min_row;
  private int          max_row;
  private int          min_col;
  private int          max_col;
  private float        min_mag_Q;
  private float        max_mag_Q;
  private float        center_row;
  private float        center_col;


/**
 * Construct an PeakEventList object from a peak, list of events and range
 * of row, col and mag_Q in the list.
 *
 * @param peak        Peak_new object with basic data about the peak.
 * @param events      List of events that are in a rectangular block around
 *                    the peak in row, col, |Q| space.
 * @param min_row     Smallest row number of any event in ev_list.
 * @param max_row     Largest  row number of any event in ev_list.
 * @param min_col     Smallest column number of any event in ev_list.
 * @param max_col     Largest  column number of any event in ev_list.
 * @param min_mag_Q   Smallest |Q| of any event in ev_list.
 * @param max_mag_Q   Largest  |Q| of any event in ev_list.
 */
  public PeakEventList( Peak_new          peak,
                        Vector<EventInfo> events,
                        int               min_row,
                        int               max_row,
                        int               min_col,
                        int               max_col,
                        float             min_mag_Q,
                        float             max_mag_Q  )
  {
    this.peak      = peak;
    this.min_row   = min_row;
    this.max_row   = max_row;
    this.min_col   = min_col;
    this.max_col   = max_col;
    this.min_mag_Q = min_mag_Q;
    this.max_mag_Q = max_mag_Q;
    this.center_row = (int)peak.y()-1;      // initialize with the row and col
    this.center_col = (int)peak.x()-1;      // from the peak object

    ev_list = new EventInfo[ events.size() ];
    for ( int i = 0; i < ev_list.length; i++ )
      ev_list[i] = events.elementAt(i);
  }


/**
 *  Get a reference to the complete list of EventInfo objects for this
 *  peak.
 *
 *  @return  a reference to the list of EventInfo objects
 */
  public EventInfo[] getEventArray()
  {
    return ev_list;
  }

/**
 *  Get a reference to the Peak_new object for this peak.
 *
 *  @return a reference to the Peak_new object.
 */
  public Peak_new getPeak()
  {
    return peak;
  }

/**
 * Get the min row number for events associated with this peak.
 */
  public int getMinRow()
  {
    return min_row;
  }


/**
 * Get the max row number for events associated with this peak.
 */
  public int getMaxRow()
  {
    return max_row;
  }


/**
 * Get the current center row number on the detector for this peak.
 * Initially this will be the "y" value from the peak object, but it
 * may be updated using methods like: SetCenterRowColToCenterOfMass
 * or SetCenterRowColToMax.
 *
 * @return the currently set center row number.
 */
  public float getCenterRow()
  {
    return center_row;
  }

/**
 * Get the min column number for events associated with this peak.
 */
  public int getMinCol()
  {
    return min_col;
  }


/**
 * Get the max column number for events associated with this peak.
 */
  public int getMaxCol()
  {
    return max_col;
  }


/**
 * Get the current center column number on the detector for this peak.
 * Initially this will be the "x" value from the peak object, but it
 * may be updated using methods like: SetCenterRowColToCenterOfMass
 * or SetCenterRowColToMax.
 *
 * @return the currently set center column number.
 */
  public float getCenterCol()
  {
    return center_col;
  }

/**
 * Get the minimum magnitude of Q for events associated with this peak
 */
  public float getMinMagQ()
  {
    return min_mag_Q;
  }


/**
 * Get the maximum magnitude of Q for events associated with this peak
 */
  public float getMaxMagQ()
  {
    return max_mag_Q;
  }

/**
 * Get a 3D histogram of events for this peak, using a circle of the
 * specified radius (in row and column number) and spiltting the Q-range
 * for this peak into the specified number of steps.
 *
 * @param rc_radius    Radius of a disk on the detector face containing
 *                     the detector pixels whose events will be included
 *                     in the histogram.
 * @param num_q_steps  Number of steps in |Q| in the histogram
 */
  public Histogram3D getCenteredCircleHistogram( float rc_radius,
                                                 int num_q_steps )
  {
    // Try adjusting the radius to keep a roughly constant solid angle
    // for the circle!  THIS DID NOT SEEM TO HELP !!!
    //
    // float det_dist = peak.getGrid().position(center_row,center_col).length();
    // float scale = det_dist/0.425f;
    // rc_radius *= scale;

    EventInfo[] selected_events = new EventInfo[ ev_list.length ];
    int num_selected = 0;
    float rc_radius_2 = rc_radius * rc_radius;
    float d_row,
          d_col;
    for ( int i = 0; i < ev_list.length; i++ )
    {
      d_row = ev_list[i].Row() - center_row;
      d_col = ev_list[i].Col() - center_col;
      if ( d_row*d_row + d_col*d_col <= rc_radius_2 )
      {
        selected_events[ num_selected ] = ev_list[i];
        num_selected++;
      }
    }
                                              // quick hack... need to make
                                              // proper centered histogram from
                                              // selected events 

    EventInfo[] saved_list = ev_list;         // save list the put the 
                                              // selected events in ev_list
    ev_list = new EventInfo[ num_selected ];
    for ( int i = 0; i < num_selected; i++ )
      ev_list[i] = selected_events[i];  

    Histogram3D result = getFullHistogram( num_q_steps );

    ev_list = saved_list;                     // restore ev_list

    return result;   
  }


/**
 * Get a 3D histogram of events for this peak, covering the full range
 * of rows and columns and |Q| for the events associated with this peak.
 *
 * @param num_q_steps  Number of steps in |Q| in the histogram
 */
  public Histogram3D getFullHistogram( int num_q_steps )
  {
    float[] xyz_vals = new float[ 3 * ev_list.length ];
    int index = 0;
    for ( int i = 0; i < ev_list.length; i++ )
    {
      xyz_vals[index++] = ev_list[i].Col();            // col
      xyz_vals[index++] = ev_list[i].Row();            // row
      xyz_vals[index++] = ev_list[i].MagQ_over_2PI();
    }

    FloatArrayEventList3D ev_list_3D = 
                           new FloatArrayEventList3D( null, xyz_vals );

    Vector3D x_bin_dir = new Vector3D( 1, 0, 0 );
    Vector3D y_bin_dir = new Vector3D( 0, 1, 0 );
    Vector3D z_bin_dir = new Vector3D( 0, 0, 1 );

    UniformEventBinner x_bin = new UniformEventBinner( min_col, max_col+1,
                                                       max_col+1 - min_col );
    UniformEventBinner y_bin = new UniformEventBinner( min_row, max_row+1,
                                                       max_row+1 - min_row );
    UniformEventBinner z_bin = new UniformEventBinner( min_mag_Q, max_mag_Q, 
                                                       num_q_steps );
    ProjectionBinner3D x_vec_bin = new ProjectionBinner3D( x_bin, x_bin_dir );
    ProjectionBinner3D y_vec_bin = new ProjectionBinner3D( y_bin, y_bin_dir );
    ProjectionBinner3D z_vec_bin = new ProjectionBinner3D( z_bin, z_bin_dir );

    Histogram3D peak_histo = new Histogram3D(x_vec_bin, y_vec_bin, z_vec_bin);
    peak_histo.addEvents( ev_list_3D, false );

    return peak_histo;
  }


/*
 *  Set a new center row and column position from the center of mass 
 *  of the peak.  A histogram from a disk of pixels centered on the current 
 *  center coordinates with 3 slices in the |Q| direction is obtained, and
 *  the center of mass of the center slice is used for calculating the 
 *  the new row and column coordinates of the peak center.
 *
 *  @param rc_radius  The radius (in row and column number) of a disk of
 *                    pixels on the detector face that will be used to
 *                    determine the center of mass.
 */
  public void SetCenterRowColToCenterOfMass( float rc_radius )
  {
    Histogram3D sum_histo =  getCenteredCircleHistogram( rc_radius, 3 );
    float[][] sum_image = sum_histo.getHistogramArray()[1];

    int n_rows = sum_image.length;
    int n_cols = sum_image[0].length;

    float total_mass = 0;
    float x_mass     = 0;
    float y_mass     = 0;
    float counts;
    for ( int col = 0; col < n_cols; col++ )
      for ( int row = 0; row < n_rows; row++ )
      {
        counts = sum_image[row][col];
        total_mass += counts;
        x_mass     += col * counts;
        y_mass     += row * counts;
      }
    center_col = x_mass/total_mass + min_col;
    center_row = y_mass/total_mass + min_row;

//  System.out.println("Center col = " + center_col + " min_col = " + min_col );
//  System.out.println("Center row = " + center_row + " min_row = " + min_row );
  }



/*
 *  Do projections of the sum image in the col and row directions
 *  to find the peak center.  A histogram from a disk of pixels centered on 
 *  the current center coordinates with 3 slices in the |Q| direction is 
 *  obtained, and the positions of the maximum values in the projections
 *  in the row and column directions is used for the new coordinates of 
 *  the peak center.
 *
 *  @param rc_radius  The radius (in row and column number) of a disk of
 *                    pixels on the detector face that will be used to
 *                    determine the center of mass.
 */
  public void SetCenterRowColToMax( float rc_radius )
  {
    Histogram3D sum_histo =  getCenteredCircleHistogram( rc_radius, 3 );
    float[][] sum_image = sum_histo.getHistogramArray()[1];

    int n_rows = sum_image.length;
    int n_cols = sum_image[0].length;

    float[] col_sum = new float[ n_cols ];
    for ( int col = 0; col < n_cols; col++ )
    {
      float sum = 0;
      for ( int row = 0; row < n_rows; row++ )
        sum += sum_image[row][col];
      col_sum[col] = sum;
    }
    center_col = MaxPosition( col_sum ) + min_col;

    float[] row_sum = new float[ n_rows ];
    for ( int row = 0; row < n_rows; row++ )
    {
      float sum = 0;
      for ( int col = 0; col < n_cols; col++ )
        sum += sum_image[row][col];
      row_sum[row] = sum;
    }
    center_row = MaxPosition( row_sum ) + min_row;

//  System.out.println("Center col = " + center_col + " min_col = " + min_col );
//  System.out.println("Center row = " + center_row + " min_row = " + min_row );
  }

/**
 *  Find the first position in the array where the maximum value occurs.
 */
  private int MaxPosition( float[] array )
  {
    int   max_index = 0;
    float max = array[0];

    for ( int i = 1; i < array.length; i++ )
      if ( array[i] > max )
      {
        max_index = i;
        max = array[i];
      } 
    return max_index;
  }

}
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
  public enum PeakType { INTERIOR, 
                         LEFT_EDGE, RIGHT_EDGE, BOTTOM_EDGE, TOP_EDGE,
                         BOTTOM_LEFT, BOTTOM_RIGHT, TOP_LEFT, TOP_RIGHT };

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
 * Get the PeakType for this peak, based on the specified minimum allowed
 * distance to an edge and the specified border width of bad pixels.
 *
 * @param min_dist   The minimum allowed distance between the peak center and 
 *                   the border of the detector, for a peak to be considered
 *                   an interior peak, in pixels.
 * @param border     The width of the border of bad or dead pixels on each 
 *                   edge of the detecor.
 *
 * @return a PeakType code indicating the position of this peak relative to
 *         the edges and corners.
 */
  public PeakType GetPeakType( float min_dist, int border )
  {
    int n_rows =  peak.getGrid().num_rows(); 
    int n_cols =  peak.getGrid().num_cols(); 

    boolean near_left   = false;
    boolean near_right  = false;
    boolean near_top    = false;
    boolean near_bottom = false;
                                                // assume rows and columns
                                                // are numbered starting at 1
    if ( center_col - 1 < border + min_dist )
      near_left = true;

    if ( center_col > n_cols - border - min_dist )
      near_right = true;

    if ( center_row - 1 < border + min_dist )
      near_bottom = true;

    if ( center_row > n_rows - border - min_dist )
      near_top = true;
                                                  // check corners
    if ( near_bottom && near_left )
      return PeakType.BOTTOM_LEFT;

    if ( near_bottom && near_right )
      return PeakType.BOTTOM_RIGHT;

    if ( near_top && near_left  )
      return PeakType.TOP_LEFT;

    if ( near_top && near_right )
      return PeakType.TOP_RIGHT;
                                                  // check edges
    if ( near_left )
      return PeakType.LEFT_EDGE;

    if ( near_right )
      return PeakType.RIGHT_EDGE;

    if ( near_top  )
      return PeakType.TOP_EDGE;

    if ( near_bottom )
      return PeakType.BOTTOM_EDGE;
                                                  // if we got here it must
                                                  // be an interior peak
    return PeakType.INTERIOR;       
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

    int min_x = (int)Math.floor(center_col - rc_radius);
    int max_x = (int)Math.ceil (center_col + rc_radius);
    int n_x   = max_x - min_x;

    int min_y = (int)Math.floor(center_row - rc_radius);
    int max_y = (int)Math.ceil (center_row + rc_radius);
    int n_y   = max_y - min_y;


    Histogram3D peak_histo = getEmptyHistogram( min_x, max_x, n_x,
                                                min_y, max_y, n_y,
                                                min_mag_Q, max_mag_Q,
                                                num_q_steps );

    FloatArrayEventList3D ev_list_3D = getColRowMagQList( selected_events,
                                                          num_selected );
    
    if ( ev_list_3D != null )
      peak_histo.addEvents( ev_list_3D, false );

    return peak_histo;
  }


/**
 * Get a 3D histogram of events for this peak, using a square with the
 * specified size (in row and column number) and splitting the Q-range
 * for this peak into the specified number of steps.
 *
 * @param size         Width and height of the square on the detector face,
 *                     centered on the peaks for which events falling in
 *                     that square will be included in the histogram.
 * @param num_q_steps  Number of steps in |Q| in the histogram
 */
  public Histogram3D getCenteredSquareHistogram( float size,
                                                 int num_q_steps )
  {
    EventInfo[] selected_events = new EventInfo[ ev_list.length ];
    int num_selected = 0;
    float size_by_2 = size/2;
    float d_row,
          d_col;
    for ( int i = 0; i < ev_list.length; i++ )
    {
      d_row = Math.abs(ev_list[i].Row() - center_row);
      d_col = Math.abs(ev_list[i].Col() - center_col);
      if ( d_row <= size_by_2 && d_col <= size_by_2 )
      {
        selected_events[ num_selected ] = ev_list[i];
        num_selected++;
      }
    }

    int min_x = (int)Math.floor(center_col - size/2);
    int max_x = (int)Math.ceil (center_col + size/2);
    int n_x   = max_x - min_x;

    int min_y = (int)Math.floor(center_row - size/2);
    int max_y = (int)Math.ceil (center_row + size/2);
    int n_y   = max_y - min_y;

    Histogram3D peak_histo = getEmptyHistogram( min_x, max_x, n_x,
                                                min_y, max_y, n_y,
                                                min_mag_Q, max_mag_Q,
                                                num_q_steps );

    FloatArrayEventList3D ev_list_3D = getColRowMagQList( selected_events,
                                                          num_selected );

    if ( ev_list_3D != null )
      peak_histo.addEvents( ev_list_3D, false );

    return peak_histo;
  }



/**
 * Get a 3D histogram of events for this peak, covering the full range
 * of rows and columns and |Q| for the events associated with this peak.
 *
 * @param num_q_steps  Number of steps in |Q| in the histogram
 */
  public Histogram3D getFullHistogram( int num_q_steps )
  {
    Histogram3D peak_histo = getEmptyHistogram( min_col, max_col+1,
                                                max_col+1 - min_col,
                                                min_row, max_row+1,
                                                max_row+1 - min_row,
                                                min_mag_Q, max_mag_Q, 
                                                num_q_steps           );

    FloatArrayEventList3D ev_list_3D = getColRowMagQList( ev_list, 
                                                          ev_list.length );

    if ( ev_list_3D != null )
      peak_histo.addEvents( ev_list_3D, false );

    return peak_histo;
  }


/*
 *  Set a new center row and column position from the center of mass 
 *  of the peak.  A histogram from a disk of pixels centered on the current 
 *  center coordinates with 3 slices in the |Q| direction is obtained, and
 *  the center of mass of the center slice is used for calculating the 
 *  the new row and column coordinates of the peak center.  NOTE: The
 *  center of mass is only changed if there are at least 5 counts in the
 *  region.
 *
 *  @param rc_radius  The radius (in row and column number) of a disk of
 *                    pixels on the detector face that will be used to
 *                    determine the center of mass.
 */
  public void setCenterRowColToCenterOfMass( float rc_radius )
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
                                            // only change center if enough
                                            // counts are present
    if ( x_mass > 0 && y_mass > 0 && total_mass > 4 )
    {
      center_col = x_mass/total_mass + (float)sum_histo.xEdgeBinner().axisMin();
      center_row = y_mass/total_mass + (float)sum_histo.yEdgeBinner().axisMin();
    }

//  System.out.println("Center col = " + center_col + " min_col = " + min_col );
//  System.out.println("Center row = " + center_row + " min_row = " + min_row );
  }



/*
 *  Do projections of the sum image in the col and row directions
 *  to find the peak center.  A histogram from a disk of pixels centered on 
 *  the current center coordinates with 3 slices in the |Q| direction is 
 *  obtained, and the positions of the maximum values in the projections
 *  in the row and column directions is used for the new coordinates of 
 *  the peak center.  NOTE: A coordinate of the center is only changed if 
 *  the maximum value of the projection occurs after position 0 in the 
 *  projected array.  This will avoid moving the center if there are no counts
 *  in the projection.
 *
 *  @param rc_radius  The radius (in row and column number) of a disk of
 *                    pixels on the detector face that will be used to
 *                    determine the center of mass.
 */
  public void setCenterRowColToMax( float rc_radius )
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
   
    int max_pos = MaxPosition( col_sum );

                              // only change center if max is after position 0
    if ( max_pos > 0 )
      center_col = max_pos + (float)sum_histo.xEdgeBinner().axisMin();

    float[] row_sum = new float[ n_rows ];
    for ( int row = 0; row < n_rows; row++ )
    {
      float sum = 0;
      for ( int col = 0; col < n_cols; col++ )
        sum += sum_image[row][col];
      row_sum[row] = sum;
    }
                              // only change center if max is after position 0
    max_pos = MaxPosition( row_sum );
    if ( max_pos > 0 )
      center_row = max_pos + (float)sum_histo.yEdgeBinner().axisMin();

//  System.out.println("Center col = " + center_col + " min_col = " + min_col );
//  System.out.println("Center row = " + center_row + " min_row = " + min_row );
  }


/*
 *  Get an estimate of the standard deviation of the peak from the peak
 *  center.  NOTE: This assumes that the peak's center has already been
 *  set to center of mass.  The standard deviation of the difference in 
 *  row number and the difference in column number from the center is 
 *  calculated for events that are within a square of the specified size
 *  centered on the peak.  This method then returns the square root of the
 *  sum of the squares of the standard deviations in the x and y directions.
 *
 *  @param size       The width and height in row and column number of a
 *                    square of pixels on the detector face that will 
 *                    be used to estimate the standard deviation.
 */
  public float getStandardDeviation( float size )
  {
    int   count = 0;
    float size_by_2 = size/2;
    float d_row,
          d_col;
    float sum_sq_row = 0;
    float sum_sq_col = 0;
    for ( int i = 0; i < ev_list.length; i++ )
    {
      d_row = Math.abs(ev_list[i].Row() - center_row);
      d_col = Math.abs(ev_list[i].Col() - center_col);
      if ( d_row <= size_by_2 && d_col <= size_by_2 )
      {
        sum_sq_row += d_row * d_row;
        sum_sq_col += d_col * d_col;
        count ++;
      }
    }

    float stdev = 0;
    if ( count > 1 )
      stdev = (float)Math.sqrt(( sum_sq_row + sum_sq_col ) / (count - 1));

    return stdev; 
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


/**
 * Get the events in this peak, in the form of a FloatArrayEventList3D
 * object, with components col, row and |Q|/2PI. 
 *
 * @param  list   A partially filled array of EventInfo objects
 * @param  num    The number of EventInfo objects in the list
 *
 * @return a FloatArrayEventlist3D containing col, row, |Q| info about
 *         each of the events in the current event list.  If the list
 *         is empty, this will return null.
 */
  private FloatArrayEventList3D getColRowMagQList( EventInfo[] list, int num )
  {
    if ( num == 0 || list == null || list.length == 0 )
      return null;

    float[] xyz_vals = new float[ 3 * num ];
    int index = 0;
    for ( int i = 0; i < num; i++ )
    {
      xyz_vals[index++] = list[i].Col();            // col
      xyz_vals[index++] = list[i].Row();            // row
      xyz_vals[index++] = list[i].MagQ_over_2PI();
    }

    FloatArrayEventList3D ev_list_3D = 
                           new FloatArrayEventList3D( null, xyz_vals );

    return ev_list_3D;
  }


/**
 *  Get an empty Histogram3D covering the specified ranges in x, y, z, 
 *  with the specified number of steps in each of those directions.
 *
 *  @return an empty Histogram3D object covering the required range.
 */
  private Histogram3D getEmptyHistogram( float min_x, float max_x, int n_x,
                                         float min_y, float max_y, int n_y,
                                         float min_z, float max_z, int n_z )
  {
    Vector3D x_bin_dir = new Vector3D( 1, 0, 0 );
    Vector3D y_bin_dir = new Vector3D( 0, 1, 0 );
    Vector3D z_bin_dir = new Vector3D( 0, 0, 1 );

    UniformEventBinner x_bin = new UniformEventBinner( min_x, max_x, n_x );
    UniformEventBinner y_bin = new UniformEventBinner( min_y, max_y, n_y );
    UniformEventBinner z_bin = new UniformEventBinner( min_z, max_z, n_z );

    ProjectionBinner3D x_vec_bin = new ProjectionBinner3D( x_bin, x_bin_dir );
    ProjectionBinner3D y_vec_bin = new ProjectionBinner3D( y_bin, y_bin_dir );
    ProjectionBinner3D z_vec_bin = new ProjectionBinner3D( z_bin, z_bin_dir );

    Histogram3D peak_histo = new Histogram3D(x_vec_bin, y_vec_bin, z_vec_bin);
    return peak_histo;
  }

}

/* 
 * File: Histogram3D.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.Histogram;

import java.util.*;

import EventTools.EventList.IEventList3D;
import EventTools.Histogram.Operators.BinEvents;
import EventTools.Histogram.Operators.ClearPages;
import EventTools.Histogram.Operators.ScanHistogram3D;
import EventTools.Histogram.Operators.GetEventLists;
import gov.anl.ipns.Operator.Threads.*;

/**
 *   This class represents a 3D histogram.  Currently only a minimal set of
 * operations is provided, but those operations have been tuned and can be
 * run in multiple threads to take advantage of multi-core processors.
 * In most cases, up to four threads on a can be used with reasonable 
 * on a system with four or more cores.  Use of more than four threads will
 * probably not be very helpful.    
 *   The bins of the 3D histogram are determined by three IEventBinner
 * objects. One binner maps between the "X" coordinate and columns of the 
 * underlying 3D array, one maps between "Y" and row numbers and the third
 * maps between "Z" and page number.  
 *   The histogram also records basic information such as the min and max
 * value, and sum of all values in the histogram.
 */
public class Histogram3D
{
  private float  max;
  private float  min;
  private double sum;

  private IEventBinner x_binner,
                       y_binner,
                       z_binner;

  private float[][][] histogram;
  private int         n_threads  = 4;
  private int         n_segments = 4;
  private int         max_time   = 60000;

  private int[] page_1 = new int[n_segments];
  private int[] page_2 = new int[n_segments];


  /**
   * Construct a Histogram3D object covering an axis aligned rectangular
   * region of 3-dimensional real space.  The extent and number of sub-
   * divisions in the x, y and z directions are determined independently
   * by the three IEventBinner parameters.
   * 
   * @param x_binner  IEventBinner that determines the range of X-values
   *                  covered by this histogram, and the subintervals
   *                  used for the histogram.
   * @param y_binner  IEventBinner that determines the range of Y-values
   *                  covered by this histogram, and the subintervals
   *                  used for the histogram.
   * @param z_binner  IEventBinner that determines the range of Z-values
   *                  covered by this histogram, and the subintervals
   *                  used for the histogram.
   */
  public Histogram3D( IEventBinner x_binner, 
                      IEventBinner y_binner,
                      IEventBinner z_binner )
  {
    this.x_binner = x_binner;
    this.y_binner = y_binner;
    this.z_binner = z_binner;

    histogram = new float[z_binner.numBins()]
                         [y_binner.numBins()]
                         [x_binner.numBins()];

    max = Float.NEGATIVE_INFINITY;
    min = Float.POSITIVE_INFINITY;
    sum = 0;
  }

 
  /**
   * Get the maximum value of any bin in the histogram.
   * @return  The max bin value.
   */
  public double maxVal()
  {
    return max;
  }

  
  /**
   * Get the minimum value of any bin in the histogram.
   * @return  The min bin value.
   */
  public double minVal()
  {
    return min;
  }

  
  /**
   * Get the total of all bins in the histogram.
   * @return  The total value.
   */
  public double total()
  {
    return sum;
  }

  
  /**
   *  Set all bins of the histogram to 0.
   */
  public void clear()
  {
    SplitPages();

    Vector  ops = new Vector();
    for ( int i = 0; i < n_segments; i++ )
      ops.add( new ClearPages( histogram, page_1[i], page_2[i] ) );

    ParallelExecutor pe = new ParallelExecutor( ops, n_threads, max_time );
    pe.runOperators();

    min = 0;
    max = 0;
    sum = 0;
  }  


  /**
   * Add all events from the specified IEventList3D to corresponding
   * bins in this histogram.
   * 
   * @param events  The list of events to be added to this histogram.
   * @return  The total event count that was added.  NOTE: currently,
   *          the event code of an event is used to record a number of
   *          simultaneous events.  Consequently, the total returned
   *          may be different from the length of the event list.
   */
  public double addEvents( IEventList3D events )
  {
    SplitPages();

    Vector  ops = new Vector();
    for ( int i = 0; i < n_segments; i++ )
      ops.add( new BinEvents( histogram, min, max, page_1[i], page_2[i], 
                              x_binner, y_binner, z_binner,  events));

    ParallelExecutor pe = new ParallelExecutor( ops, n_threads, max_time );
    Vector results = pe.runOperators();

    double old_sum = sum;
    extract_scan_info( results, old_sum );

    return sum - old_sum;
  }


  /**
   * Get event lists from bins of this histogram with values in intervals
   * determined by the specified IEventBinner.  Since the event lists are
   * obtained by multiple threads working on different sections of the 
   * underlying array, the event lists are returned in many small sections.
   * Specifically, a Vector of Vectors is returned, with one entry in the
   * top level Vector, for each segment of the array that was processed
   * separately.  The "inner" Vectors, corresponding to one segment of the
   * array, contains multiple IEventList3D objects, one for each bin of the
   * specified binner, and one for 3D histogram bins with values larger than
   * the maximum binner value.  Specifically, if the specified binner splits 
   * the interval [10,100) into 9 uniform bins, then 10 lists of events will 
   * be returned.  The first list of events will have x,y,z values at bin 
   * centers, for bins in the segment with counts in the interval [10,20).
   * The event codes will be 0 for these events. The second list of events 
   * will have x,y,z values at bin centers, for bins with counts in the 
   * interval [20,30), etc.  The event codes will be 1 for these events.
   * Finally, 10th list will have the x,y,z values at bin centers, for bins
   * with counts greater than 100.  If there were no bins with counts in a 
   * particular interval, in a particular segment of the array, then that 
   * IEventList3D will be NULL. 
   *   For example, if the binner had 9 bins and the array was split 
   * into 4 segments, then there could be as many as 40 IEventList3D 
   * objects.  The top level vector would have 4 entries, one for each 
   * segment that was processed.  Each of these 4 entries would be a
   * Vector with 10 entries.  Each of those 10 entries would either be 
   * an IEventList3D, or null.  
   *   
   * @param binner  The IEventBinner specifying the histogram levels
   *                of bins that should be returned.
   *                
   * @return  a Vector of Vectors containing IEventList3D object.
   */
  public Vector getEventLists( IEventBinner binner )
  {
    SplitPages();

    Vector  ops = new Vector();
    for ( int i = 0; i < n_segments; i++ )
      ops.add( new GetEventLists( histogram, page_1[i], page_2[i],
                                  x_binner, y_binner, z_binner, binner ) );

    ParallelExecutor pe = new ParallelExecutor( ops, n_threads, max_time );
    Vector results = pe.runOperators();
    return results;
  }

  
  static float[][] one_row = new float[1024][1024];
  
  public float[][] getRow( int row )
  {
    int num_pages = z_binner.numBins();
    int num_cols  = x_binner.numBins();
//    float[][] one_row = new float[num_cols][num_pages];
    for ( int page = 0; page < num_pages; page++ )
       for ( int col = 0; col < num_cols; col++ )
          one_row[col][page] = histogram[page][row][col];
 
    return one_row;
  }


  public float[][] getPage( int page )
  {
    return histogram[page];
  }

  
  
  /**
   * Scan across ALL bins of this histogram and set the min, max and total
   * value information.  NOTE: This method should NOT generally have to be
   * used, since methods that change the histogram entries update the min,
   * max and total value information.
   */
  public void scanHistogram()
  {
    SplitPages();

    Vector  ops = new Vector();
    for ( int i = 0; i < n_segments; i++ )
      ops.add( new ScanHistogram3D( histogram, page_1[i], page_2[i] ) );

    ParallelExecutor pe = new ParallelExecutor( ops, n_threads, max_time );
    Vector results = pe.runOperators();

    extract_scan_info( results, 0 );
  }


  /**
   * Update the min, max and total value information from the result 
   * Vector returned from executing the BinEvents and ScanHistogram3D 
   * operators in multiple threads.
   *  
   * @param results  A vector containing three Doubles, a sum of events
   *                 and min and max bin values
   * @param old_sum  The total sum from the histogram, before executing
   *                 the scan or bin operators.  This should be passed in
   *                 as zero when ScanHistogram is used, to properly 
   *                 get the new total sum.  It should be passed in as
   *                 the old_sum when adding extra events to a non-zero
   *                 histogram using BinEvents.
   */
  private void extract_scan_info( Vector results, double old_sum )
  {
    double   new_sum = old_sum;
    Object   one_result;
    double[] answers = new double[3];
    for ( int i = 0; i < results.size(); i++ )
    {
      one_result = results.elementAt(i);
      if ( one_result instanceof Vector )
        if ( ((Vector)one_result).size() == 3 )
        {
          Vector result_v = (Vector)one_result;
          for ( int k = 0; k < 3; k++ )
            if ( result_v.elementAt(k) instanceof Double )
              answers[k] = (Double)result_v.elementAt(k);
            else
              throw new ExecFailException( FailState.NOT_DONE, result_v );

          new_sum += answers[0];

          if ( answers[1] < min )
            min = (float)answers[1];

          if ( answers[2] > max )
            max = (float)answers[2];
        }
        else
          throw new ExecFailException( FailState.NOT_DONE, null );
    }

    sum = new_sum;
  }

  
  /**
   * Calculate how to split the array into roughly equal sized segments
   * based on the number of segments requested and the number of pages
   * in the histogram.  NOTE: the number of segments is forced to be 
   * less than or equal to the number of pages.
   */
  private void SplitPages()
  {
    if ( n_segments > histogram.length )
      n_segments = histogram.length;
    
    page_1 = new int[n_segments];
    page_2 = new int[n_segments];

    int   step = histogram.length / n_segments;
    for ( int i = 0; i < n_segments; i++ )
    {
      page_1[i] = i*step;
      page_2[i] = (i+1) * step - 1;
    }
    page_2[n_segments-1] = histogram.length - 1;
  }

}

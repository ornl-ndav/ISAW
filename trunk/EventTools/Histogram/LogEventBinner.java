/* 
 * File: LogEventBinner.java
 *
 * Copyright (C) 2010, Dennis Mikkelson
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
 *  $Author: eu7 $
 *  $Date: 2009-07-22 15:20:00 -0500 (Wed, 22 Jul 2009) $            
 *  $Revision: 19795 $
 */

package EventTools.Histogram;

import java.util.*;

/**
 * This class splits an interval [a,b) into a set of contiguous bins [ai,bi)
 * with bin boundaries forming a geometric progression.  Methods are provided
 * to map between a numeric value in [a,b) and the index, i, of the bin 
 * containing that value, and from an index i to the end points or center 
 * of the bin.  The actual value of the right hand end point will be adjusted
 * to be the smallest possible bin boundary that is greater than or equal to
 * the value of "b" specified in the constructor.
 */
public class LogEventBinner implements IEventBinner
{

  private double min;
  private double min_inv;
  private double max;
  private int    num_bins;

  private double ratio;
  private double log_ratio_inv;

  private double eps = 1 + 1e-15;

  /*
   * Construct a LogEventBinner for the specified interval and 
   * first step size.
   * 
   * @param min        The left hand end point of the interval [a,b) on which
   *                   events can occur.  This must be a positive value.
   * @param max        The right hand end point of the interval [a,b); this
   *                   MUST BE STRICTLY GREATER THAN the largest value to be 
   *                   mapped to a valid bin index.  Since the specified max
   *                   may not exactly fall on a bin, the actual max will
   *                   be the last bin that is greater than or equal to the
   *                   specified max.
   * @param first_step The length of the first interval.  This determines
   *                   the ratio dx/x=first_step/first_x.
   */
  public LogEventBinner( double min, double max, double first_step )
  {
    if ( min >= max )
      throw new IllegalArgumentException( "min >= max " + min + ">=" + max );

    if ( first_step <= 0 )
      throw new IllegalArgumentException( "first step <= 0 " + first_step );

    if ( min <= 0 )
      throw new IllegalArgumentException( "min <= 0 " + min );

    this.min     = min;
    this.min_inv = 1.0/min;

    ratio         = 1 + first_step/min;
    log_ratio_inv = 1.0/Math.log( ratio );

    num_bins = (int)( Math.log( eps * max * min_inv ) * log_ratio_inv );
    this.max = min * Math.pow( ratio, num_bins+1 );
  }

  @Override
  public double axisMin()
  {
    return min;
  }

  @Override
  public double axisMax()
  {
    return max;
  }

  @Override
  public int numBins()
  {
    return num_bins;
  }

  @Override
  public int index( double val )
  {
    if ( val < min )
      return -1;

    return (int)( Math.log( eps * val * min_inv) * log_ratio_inv );
                               // NOTE: val is increased by a very small factor
                               //       that index(minVal(i)) == i for all
                               //       of the bins.
  }

  @Override
  public double centerVal( int index )
  {
    return min * Math.pow( ratio, index+0.5 );
  }

  @Override
  public double minVal( int index )
  {
    return min * Math.pow( ratio, index );
  }

  @Override
  public double maxVal( int index )
  {
    return min * Math.pow( ratio, index+1 );
  }

  @Override
  public double Val( double fractional_index )
  {
    return min * Math.pow( ratio, fractional_index );
  }

  /**
   * Get a string specifying the min, max and number of steps used.
   */
  public String toString()
  {
    return String.format( "[ %7.2f, %7.2f ) : %4d ", min, max, num_bins );
  }

  
  /**
   * Basic test program to verify that the binner behaves correctly.
   * 
   * @param args  NOT USED
   */
  public static void main( String args[] )
  {
    double min = 1000;
    double max = 32000;
    double first_step = 0.2;
    IEventBinner binner = new LogEventBinner( min, max, first_step );

    System.out.println("Log Binner: " + binner );

    double left,
           right, 
           center,
           center_2;
    int    left_i,
           right_i,
           center_i,
           center_2_i;

    for ( int i = 0; i < 40; i++ )
    {
      left     = binner.minVal(i);
      center   = binner.centerVal(i);
      center_2 = binner.Val(i+0.5);
      right    = binner.maxVal(i);

      left_i     = binner.index( left );
      center_i   = binner.index( center );
      center_2_i = binner.index( center_2 );
      right_i    = binner.index( right );
      System.out.printf("%5d  %8.2f  %8.2f  %8.2f  %8.2f  %5d  %5d  %5d  %5d\n",
                        i, left, center, center_2, right,
                        left_i, center_i, center_2_i, right_i );
    }

    int error_count = 0;
    for ( int i = 0; i < binner.numBins(); i++ )
    {
      left   = binner.minVal(i);
      left_i = binner.index( left );
      if ( left_i != i )
      {
        System.out.printf("Error at: %5d  %8.2f  %5d\n", i, left, left_i );
        error_count++;
      }
    }
    System.out.println("Bin boundary errors = " + error_count );

    int NUM_TESTS = 1000000;
    double[] test_x = new double[ NUM_TESTS ];
    Random ran = new Random();
    for ( int i = 0; i < NUM_TESTS; i++ )
      test_x[i] = min + ran.nextDouble()*(max - min);

    long start = System.nanoTime();
    int sum = 0;
    for ( int i = 0; i < NUM_TESTS; i++ )
      sum += binner.index( test_x[i] ); 

    double time = (System.nanoTime() - start)/1E6;
    System.out.println("LogEventBinner, time for " + NUM_TESTS +
                       " index() was " + time + " ms." );
    System.out.println("sum = " + sum );
  }
}

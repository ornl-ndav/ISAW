/* 
 * File: UniformEventBinner.java
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

/**
 * This class splits a specified interval [a,b) into a specified number of equal
 * size, contiguous bins [ai,bi). Methods are provided to map between a numeric
 * value in [a,b) and the index, i, of the bin containing that value, and from
 * an index i to the end points or center of the bin.
 */
public class UniformEventBinner implements IEventBinner
{

  private double min;
  private double max;
  private int    num_bins;

  private double to_val_scale;
  private double to_val_shift;

  private double to_index_scale;
  private double to_index_shift;

  /*
   * Construct a UniformEventBinner for the specified interval: [min,max) and
   * specified number of contiguous subintervals.
   * 
   * @param min      The left hand end point of the interval [a,b) on which
   *                 events can occur.
   * @param max      The right hand end point of the interval [a,b); this
   *                 MUST BE STRICTLY GREATER THAN the largest value to be 
   *                 mapped to a valid bin index.
   * @param num_bins The number of contiguous, equal length, subintervals
   *                 [ai,bi) that subdivide the interval [a,b)
   *                   
   *  NOTE: num_bins must be >= 1
   * and min must be < max, or IllegalArgumentException
   */
  public UniformEventBinner( double min, double max, int num_bins )
  {
    if ( num_bins < 1 )
      throw new IllegalArgumentException( "num_bins < 1 " + num_bins );
    if ( min >= max )
      throw new IllegalArgumentException( "min >= max " + min + ">=" + max );

    this.min = min;
    this.max = max;
    this.num_bins = num_bins;

    to_val_scale = (max - min) / num_bins;
    to_val_shift = min + 0.5 * to_val_scale;

    to_index_scale = 1 / to_val_scale;
    to_index_shift = -min * to_index_scale;
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

    int index = (int) (to_index_shift + val * to_index_scale);

    return index;
  }

  @Override
  public double centerVal( int index )
  {
    return to_val_shift + index * to_val_scale;
  }

  @Override
  public double minVal( int index )
  {
    return min + index * to_val_scale;
  }

  @Override
  public double maxVal( int index )
  {
    return min + (index + 1) * to_val_scale;
  }

  @Override
  public double Val( double fractional_index )
  {
    return min + fractional_index * to_val_scale; 
  }

  
  /**
   * Get a string specifying the min, max and number of steps used.
   */
  public String toString()
  {
    return String.format( "[ %7.2f, %7.2f ) : %4d ", min, max, num_bins );
  }

  
  /**
   * Basic test program to verify that the binner behaves correctly 
   * for values inside [min,max) and outside [min,max).
   * 
   * @param args  NOT USED
   */
  public static void main( String args[] )
  {
    IEventBinner binner = new UniformEventBinner( -5, 5, 10 );
    for ( int i = -1; i < 11; i++ )
      System.out.println( i + " " + binner.centerVal( i ) );

    double val;
    double eps = 1e-14;
    int index;

    for ( int i = -6; i < 7; i++ )
    {
      val = i - eps;
      index = binner.index( val );
      System.out.printf( "%18.14f %3d", val, index );
      System.out.printf(
                         " in[ %18.14f, %18.14f )\n",
                         binner.minVal( index ),
                         binner.maxVal( index ) );
      System.out.println();

      val = val + eps;
      index = binner.index( val );
      System.out.printf( "%18.14f %3d", val, index );
      System.out.printf(
                         " in[ %18.14f, %18.14f )\n",
                         binner.minVal( index ),
                         binner.maxVal( index ) );

      val = val + eps;
      index = binner.index( val );
      System.out.printf( "%18.14f %3d", val, index );
      System.out.printf(
                         " in[ %18.14f, %18.14f )\n",
                         binner.minVal( index ),
                         binner.maxVal( index ) );
    }

  }
}

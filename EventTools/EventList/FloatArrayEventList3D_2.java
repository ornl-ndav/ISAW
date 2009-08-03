/* 
 * File: FloatArrayEventList3D_2.java
 *
 * Copyright (C) 2009, Dennis Mikkelson
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

package EventTools.EventList;

import EventTools.Histogram.IEventBinner;
import EventTools.Histogram.UniformEventBinner;

/**
 * This class uses arrays of floats to record the x,y,z
 * coordinates of a list of events, and an array of floats
 * to record the corresponding weights for the events.
 */
public class FloatArrayEventList3D_2 implements IEventList3D 
{
  private float[] weights = null;
  private float[] xyz_vals;

  private IEventBinner x_extent = null;
  private IEventBinner y_extent = null;
  private IEventBinner z_extent = null;

  /**
   * Construct an event list using the specified arrays of weights and
   * x,y,z coordinates.  The xyz_vals array must be three times longer
   * than the array of weights, if the array of weights is non-null.
   * The xyz_vals array must be non-empty.
   *  
   * @param xyz_vals  Array of x-coordinates for the events.
   * @param weights   Array of weights for the events. May be null.
   */
  public FloatArrayEventList3D_2( float[] weights,
                                  float[] xyz_vals )
  {
    if ( xyz_vals == null )
      throw new IllegalArgumentException( "array null" );

    int num_events = xyz_vals.length / 3;
    if ( num_events <= 0 )
      throw new IllegalArgumentException( "zero length weight array" );

    this.weights  = weights;
    this.xyz_vals = xyz_vals;
  }


  @Override
  public int numEntries()
  {
    return xyz_vals.length / 3;
  }


  @Override
  public float eventWeight( int i )
  {
    if ( weights != null )
      return weights[i];
    return 0;
  }


  @Override
  public float[] eventWeights()
  {
    return weights;
  }


  @Override
  public void setEventWeights( float[] weights )
  {
    this.weights = weights;
  }


  @Override
  public void eventVals( int i, double[] values )
  {
    int index = 3*i;
    values[0] = xyz_vals[ index     ];
    values[1] = xyz_vals[ index + 1 ];
    values[2] = xyz_vals[ index + 2 ];
  }


  @Override
  public float[] eventVals()
  {
    return xyz_vals;
  }


  @Override
  public double eventX( int i )
  {
    return xyz_vals[3*i];
  }


  @Override
  public double eventY( int i )
  {
    return xyz_vals[3*i+1];
  }


  @Override
  public double eventZ( int i )
  {
    return xyz_vals[3*i+2];
  }


  @Override
  public IEventBinner xExtent()
  {
    if ( x_extent == null )
      x_extent = min_max( 0 );
        
    return x_extent;
  }

  
  @Override
  public IEventBinner yExtent()
  {
    if ( y_extent == null )
      y_extent = min_max( 1 );
        
    return y_extent;
  }

  
  @Override
  public IEventBinner zExtent()
  {
    if ( z_extent == null )
      z_extent = min_max( 2 );
      
    return z_extent;
  }

  
  /**
   *  Return a string giving the number of entries, and the
   *  x,y,z extents of the events.
   */
  public String toString()
  {
    return String.format( "Num: %6d ", numEntries() ) +
           "XRange: " + xExtent() +
           "YRange: " + xExtent() +
           "ZRange: " + xExtent();
  }


  /**
   * Construct an event binner that spans the extent of the
   * x, y or z values stored in the specified array.
   *  
   * @param offset  Must be 0, 1 or 2 to select x, y or z
   *                respectively.
   *             
   * @return An event binner whose min and max specify a half
   * open interval, [min,max) that contains the values in the
   * array.
   */
  private IEventBinner min_max( int offset )
  {
    float min = xyz_vals[offset];
    float max = xyz_vals[offset];
    float val;

    for ( int i = 3+offset; i < xyz_vals.length/3; i += 3 )
    {
      val = xyz_vals[i];
      if ( val < min )
        min = val;
      else if ( val > max )
        max = val; 
    }

    // Since we are using half-open intervals, we need to move the right
    // hand end point to be slightly above the largest value actually 
    // present.
    float old_max = max;
    float eps = 1e-30f;
    while ( max == old_max )
    {
      max = old_max + eps;
      eps *= 10; 
    }

    return new UniformEventBinner( min, max, 1);
  }

}

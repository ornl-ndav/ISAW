/* 
 * File: FloatArrayEventList3D.java
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

package EventTools.EventList;

import java.util.*;
import EventTools.Histogram.IEventBinner;
import EventTools.Histogram.UniformEventBinner;

/**
 * This class uses arrays of floats to record the x,y,z
 * coordinates of a list of events, and an array of floats 
 * to record the corresponding weights for the events.
 */
public class FloatArrayEventList3D implements IEventList3D 
{
  private float[] weights;
  private float[] x_vals;
  private float[] y_vals;
  private float[] z_vals;

  private IEventBinner x_extent = null;
  private IEventBinner y_extent = null;
  private IEventBinner z_extent = null;

  /**
   * Construct an event list using the specified arrays of weights and
   * x,y,z coordinates.  All of the array parameters must have the same
   * length and must be non-empty.
   * 
   * @param weights Array of weights for the events.
   * @param x_vals  Array of x-coordinates for the events.
   * @param y_vals  Array of y-coordinates for the events.
   * @param z_vals  Array of z-coordinates for the events.
   */
  public FloatArrayEventList3D( float[] weights,
                                float[] x_vals,
                                float[] y_vals,
                                float[] z_vals  ) 
  {
    if ( weights == null || x_vals == null || y_vals == null || z_vals == null )
      throw new IllegalArgumentException( "array null" );

    int num_events = x_vals.length;
    if ( num_events <= 0 )
      throw new IllegalArgumentException( "zero length weights array" );

    if ( x_vals.length != num_events ||
         y_vals.length != num_events ||
         z_vals.length != num_events  )
      throw new IllegalArgumentException( "wrong length of ?_vals array" );

    this.weights = weights;
    this.x_vals  = x_vals;
    this.y_vals  = y_vals;
    this.z_vals  = z_vals;
  }


  @Override
  public int numEntries()
  {
    return x_vals.length;
  }


  @Override
  public float eventWeight( int i )
  {
    return weights[i];
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
    values[0] = x_vals[i];
    values[1] = y_vals[i];
    values[2] = z_vals[i];
  }


  @Override
  public float[] eventVals()
  {
    float[] vals = new float[ 3*x_vals.length ];
    int index = 0;
    for ( int i = 0; i < x_vals.length; i++ )
    {
      vals[index++] = x_vals[i];
      vals[index++] = y_vals[i];
      vals[index++] = z_vals[i];
    }
    return vals;
  }


  @Override
  public double eventX( int i )
  {
    return x_vals[i];
  }


  @Override
  public double eventY( int i )
  {
    return y_vals[i];
  }


  @Override
  public double eventZ( int i )
  {
    return z_vals[i];
  }


  @Override
  public IEventBinner xExtent()
  {
    if ( x_extent == null )
      x_extent = min_max( x_vals );
        
    return x_extent;
  }

  
  @Override
  public IEventBinner yExtent()
  {
    if ( y_extent == null )
      y_extent = min_max( y_vals );
        
    return y_extent;
  }

  
  @Override
  public IEventBinner zExtent()
  {
    if ( z_extent == null )
      z_extent = min_max( z_vals );
      
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
   * values stored in the specified array.
   *  
   * @param vals Array of values that is scanned to find the 
   *             min and max.
   *             
   * @return An event binner whose min and max specify a half
   * open interval, [min,max) that contains the values in the
   * array.
   */
  private IEventBinner min_max( float[] vals )
  {
    float min = vals[0];
    float max = vals[0];
    float val;

    for ( int i = 1; i < vals.length; i++ )
    {
      val = vals[i];
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
 

  /**
   *  Merge all of the event lists in the Vector into a new 
   *  event list.
   *
   *  @param lists  Vector of IEventList3D objects
   *  
   *  @return a new FloatArrayEventList3D containing all of the events
   *          in the lists of events.
   */
  public static FloatArrayEventList3D merge( Vector<IEventList3D> lists )
  {
     IEventList3D list;
     int total_size = 0;
     for ( int i = 0; i < lists.size(); i++ )
     {
       list = lists.elementAt(i);
       total_size += list.numEntries();
     }

     float[] all_x       = new float[total_size];
     float[] all_y       = new float[total_size];
     float[] all_z       = new float[total_size];
     float[] all_weights = new float[total_size];
     int index = 0;
     for ( int i = 0; i < lists.size(); i++ )
     {
       list = lists.elementAt(i);
       int length = list.numEntries();
       if ( length > 0 )
         for ( int k = 0; k < length; k++ )
         {
           all_x[index] = (float)list.eventX(k);
           all_y[index] = (float)list.eventY(k);
           all_z[index] = (float)list.eventZ(k);
           all_weights[index] = list.eventWeight(k);
           index++;        
         }
     }

     return new FloatArrayEventList3D( all_weights, all_x, all_y, all_z );
  }

}

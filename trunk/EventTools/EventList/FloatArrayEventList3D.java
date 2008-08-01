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
 * coordinates of a list of events, and an array of ints
 * to record the corresponding integer codes for the events.
 */
public class FloatArrayEventList3D implements IEventList3D 
{
  private int[]   codes;
  private float[] x_vals;
  private float[] y_vals;
  private float[] z_vals;

  private IEventBinner x_extent = null;
  private IEventBinner y_extent = null;
  private IEventBinner z_extent = null;

  /**
   * Construct an event list using the specified arrays of codes and
   * x,y,z coordinates.  All of the array parameters must have the same
   * length and must be non-empty.
   * 
   * @param codes   Array of integer codes for the events.
   * @param x_vals  Array of x-coordinates for the events.
   * @param y_vals  Array of y-coordinates for the events.
   * @param z_vals  Array of z-coordinates for the events.
   */
  public FloatArrayEventList3D( int[]   codes,
                                float[] x_vals,
                                float[] y_vals,
                                float[] z_vals  ) 
  {
    if ( codes == null || x_vals == null || y_vals == null || z_vals == null )
      throw new IllegalArgumentException( "array null" );

    int num_events = codes.length;
    if ( num_events <= 0 )
      throw new IllegalArgumentException( "zero length codes array" );

    if ( x_vals.length != num_events ||
         y_vals.length != num_events ||
         z_vals.length != num_events  )
      throw new IllegalArgumentException( "wrong length of ?_vals array" );

    this.codes  = codes;
    this.x_vals = x_vals;
    this.y_vals = y_vals;
    this.z_vals = z_vals;
  }


  public int getNumEntries()
  {
    return codes.length;
  }


  public int getEventCode( int i )
  {
    return codes[i];
  }


  public double getEventX( int i )
  {
    return x_vals[i];
  }


  public double getEventY( int i )
  {
    return y_vals[i];
  }


  public double getEventZ( int i )
  {
    return z_vals[i];
  }


  public void getEventVals( int i, double[] values )
  {
    values[0] = x_vals[i];
    values[1] = y_vals[i];
    values[2] = z_vals[i];
  }


  public float[][] getEventArrays()
  {
    float[][] result = new float[3][];
    result[0] = x_vals;
    result[1] = y_vals;
    result[2] = z_vals;
    return result;
  }

  
  public int[] getCodeArray()
  {
    return codes;
  }


  public IEventBinner getXExtent()
  {
    if ( x_extent == null )
      x_extent = min_max( x_vals );
        
    return x_extent;
  }

  
  public IEventBinner getYExtent()
  {
    if ( y_extent == null )
      y_extent = min_max( y_vals );
        
    return y_extent;
  }

  
  public IEventBinner getZExtent()
  {
    if ( z_extent == null )
      z_extent = min_max( z_vals );
      
    return z_extent;
  }


  public String toString()
  {
    return String.format( "Num: %6d ", getNumEntries() ) +
           "XRange: " + getXExtent() +
           "YRange: " + getXExtent() +
           "ZRange: " + getXExtent();
  }


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
       total_size += list.getNumEntries();
     }

     float[] all_x     = new float[total_size];
     float[] all_y     = new float[total_size];
     float[] all_z     = new float[total_size];
     int[]   all_codes = new int[total_size];
     int index = 0;
     for ( int i = 0; i < lists.size(); i++ )
     {
       list = lists.elementAt(i);
       int length = list.getNumEntries();
       if ( length > 0 )
         for ( int k = 0; k < length; k++ )
         {
           all_x[index] = (float)list.getEventX(k);
           all_y[index] = (float)list.getEventY(k);
           all_z[index] = (float)list.getEventZ(k);
           all_codes[index] = list.getEventCode(k);
           index++;        
         }
     }

     return new FloatArrayEventList3D( all_codes, all_x, all_y, all_z );
  }

}

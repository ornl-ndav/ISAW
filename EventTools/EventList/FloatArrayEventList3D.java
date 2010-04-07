/* 
 * File: FloatArrayEventList3D.java
 *
 * Copyright (C) 2008-2010, Dennis Mikkelson
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

import java.util.Vector;
import EventTools.Histogram.IEventBinner;
import EventTools.Histogram.UniformEventBinner;

/**
 * This class uses arrays of floats to record the x,y,z
 * coordinates of a list of events, and an array of floats
 * to record the corresponding weights for the events.
 */
public class FloatArrayEventList3D implements IEventList3D 
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
   * NOTE: This constructor just records references to the arrays passed
   *       in as parameters.  Calling code MUST copy the arrays if the
   *       arrays will be reused in the calling code.
   *  
   * @param xyz_vals  Array of interleaved xyz-coordinates for the events.
   * @param weights   Array of weights for the events. May be null.
   */
  public FloatArrayEventList3D( float[] weights,
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


  /**
   * Construct an event list using the specified arrays of weights and
   * x,y,z coordinates.  If the array of weights is non-null, then the
   * arrays of x, y and z values must also be non-empty.
   *  
   * @param x_vals    Array of x-coordinates for the events.
   * @param y_vals    Array of y-coordinates for the events.
   * @param z_vals    Array of z-coordinates for the events.
   * @param weights   Array of weights for the events. May be null.
   *
   * @deprecated  This constructor is much less efficient thatn the 
   *              constructor that accepts a list of interleaved xyz
   *              coordinates.
   */
  public FloatArrayEventList3D( float[] weights,
                                float[] x_vals,
                                float[] y_vals,
                                float[] z_vals )
  {
    if ( x_vals == null )
      throw new IllegalArgumentException( "x array null" );

    if ( y_vals == null )
      throw new IllegalArgumentException( "y array null" );

    if ( z_vals == null )
      throw new IllegalArgumentException( "z array null" );

    if ( x_vals.length != y_vals.length ||
         y_vals.length != z_vals.length ||
         z_vals.length != x_vals.length )
      throw new IllegalArgumentException( "x,y,z arrays of differnt lengths" );

    int num_events = x_vals.length;
    if ( num_events <= 0 )
      throw new IllegalArgumentException( "zero length weight array" );

    this.weights = weights;
    xyz_vals = new float[ num_events * 3 ];
    
    int index = 0;
    for ( int i = 0; i < num_events; i++ )
    {
      xyz_vals[index++] = x_vals[i];
      xyz_vals[index++] = y_vals[i];
      xyz_vals[index++] = z_vals[i];
    }
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
   *  Get a new event list, consisting of only those events in the current
   *  list that are within the specified radius of the specified point.
   *
   *  @param point  The xyz coordinates of the center point of the sphere
   *                that should be kept.
   *  @param radius The radius of the sphere of events that should be kept.
   *
   *  @return a new FloatArrayEventList3D object that only contains the
   *          events of the current list that are close to the specified point
   *          OR null if there are no events close to the point.
   */
  public FloatArrayEventList3D getLocalEvents( float[] point, float radius )
  {
    int   count = 0;
    float x     = point[0];
    float y     = point[1];
    float z     = point[2];
    float radius_squared = radius * radius;
    float num_events = xyz_vals.length/3;
    float d_squared;
    float ev_x,
          ev_y,
          ev_z;
                                             // to avoid extra allocate/free
                                             // first scan list and count
    int index = 0;                           // number of nearby points
    for ( int i = 0; i < num_events; i++ )
    {
      ev_x = xyz_vals[ index++ ];
      ev_y = xyz_vals[ index++ ];
      ev_z = xyz_vals[ index++ ];
      d_squared = (x-ev_x)*(x-ev_x) + (y-ev_y)*(y-ev_y) +(z-ev_z)*(z-ev_z);
      if ( d_squared < radius_squared )
        count++;
    }

    if ( count == 0 )
      return null;

    float[] sub_weights = new float[count];
    float[] sub_xyz     = new float[3*count];
    index = 0;
    count = 0;
    for ( int i = 0; i < num_events; i++ )
    {
      ev_x = xyz_vals[ index++ ];
      ev_y = xyz_vals[ index++ ];
      ev_z = xyz_vals[ index++ ];
      d_squared = (x-ev_x)*(x-ev_x) + (y-ev_y)*(y-ev_y) +(z-ev_z)*(z-ev_z);
      if ( d_squared < radius_squared )
      {
        sub_weights[ count ] = weights[i];
        sub_xyz[ 3*count     ] = xyz_vals[ 3*i     ];
        sub_xyz[ 3*count + 1 ] = xyz_vals[ 3*i + 1 ];
        sub_xyz[ 3*count + 2 ] = xyz_vals[ 3*i + 2 ];
        count++;
      }
    }

    return new FloatArrayEventList3D( sub_weights, sub_xyz );
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

     float[] all_xyz     = new float[total_size * 3];
     float[] all_weights = new float[total_size    ];

     int index     = 0;
     int xyz_index = 0;
     for ( int i = 0; i < lists.size(); i++ )
     {
       list = lists.elementAt(i);
       int length = list.numEntries();
       if ( length > 0 )
         for ( int k = 0; k < length; k++ )
         {
           xyz_index = 3 * index;
           all_xyz[xyz_index++] = (float)list.eventX(k);
           all_xyz[xyz_index++] = (float)list.eventY(k);
           all_xyz[xyz_index  ] = (float)list.eventZ(k);
           all_weights[index++] = list.eventWeight(k);
         }
     }

     return new FloatArrayEventList3D( all_weights, all_xyz );
  }

}

/* 
 * File: UniformProjectionBinner3D.java
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

import gov.anl.ipns.MathTools.Geometry.Vector3D;

/**
 * Extends UniformEventBinner to bin events based on the
 * dot product with a specified direction vector.
 * The vector is assumed to start at the origin.
 */
public class UniformProjectionBinner3D extends    UniformEventBinner 
                                       implements IProjectionBinner3D
{
  private float vec_x, vec_y, vec_z;
  
  public UniformProjectionBinner3D( double   min, 
                                    double   max, 
                                    int      num_bins,
                                    Vector3D direction )
  {
    super( min, max, num_bins );
  
    if ( direction == null )
      throw new IllegalArgumentException("NULL direction ");
    
    Vector3D temp = new Vector3D( direction );    // make sure we have a
    temp.normalize();                             // unit vector, but don't
                                                  // change the parameter
    vec_x = temp.getX();
    vec_y = temp.getY();
    vec_z = temp.getZ();
  }
  
  @Override
  public Vector3D centerVec( int index )
  {
    Vector3D center = new Vector3D( vec_x, vec_y, vec_z );
    center.multiply( (float)centerVal(index) );
    return center;
  }

  @Override
  public void centerPoint( int index, float[] coords )
  {
    if ( coords == null ) 
         throw new IllegalArgumentException("Array coords is null");
    if ( coords.length < 3 ) 
         throw new IllegalArgumentException("Array coords length < 3 (" +
                                             coords.length + ")" );

    float scale = (float)centerVal(index);
    coords[0] = scale * vec_x;
    coords[1] = scale * vec_y;
    coords[2] = scale * vec_z;
  }

  @Override
  public Vector3D directionVec()
  {
    return new Vector3D( vec_x, vec_y, vec_z );
  }

  @Override
  public int index( float x, float y, float z )
  {
    float val = x * vec_x + y * vec_y + z * vec_z;
    return index( val );
  }

  @Override
  public int index( Vector3D vec )
  {
    float val = vec.getX() * vec_x + vec.getY() * vec_y + vec.getZ() * vec_z;
    return index( val );
  }


  /**
   * Convenience method to calculate the center point of a specified bin
   * in a 3D histogram.  The x_index, determines a vector in the 
   * x_binner's direction, the y_index determines a vector in the 
   * y_binner's direction, and the z_index determines a vector in the 
   * z_binner's direction.  The array coords[] is filled with the 
   * coordinates of the sum of these three vectors. 
   *
   * @param  x_index   The index of the bin in the direction of the x_binner
   * @param  y_index   The index of the bin in the direction of the y_binner
   * @param  z_index   The index of the bin in the direction of the z_binner
   * @param  x_binner  Binner for the first basis direction, "x".
   * @param  y_binner  Binner for the second basis direction, "y".
   * @param  z_binner  Binner for the third basis direction, "z".
   * @param  coords    Array with at least positions, into which the sum
   *                   of x*x_vec + y*y_vec + z*zvec will be stored.  The
   *                   vectors, x_vec, y_vec and z_vec are the unit direction
   *                   vectors of the x,z and z binners.  The values, x, y, z
   *                   are distances along the direction vectors chosen so
   *                   that the vector sum gives the center point of the bin
   *                   in a 3D histogram, with the specified indices and the
   *                   the specified binners.
   */
  public static void centerPoint( int                        x_index,
                                  int                        y_index,
                                  int                        z_index,
                                  UniformProjectionBinner3D  x_binner,
                                  UniformProjectionBinner3D  y_binner,
                                  UniformProjectionBinner3D  z_binner,
                                  float                      coords[] )
  {
    x_binner.centerPoint( x_index, coords );

    float[] temp = new float[3];
    y_binner.centerPoint( y_index, temp );
    coords[0] += temp[0];
    coords[1] += temp[1];
    coords[2] += temp[2];
 
    z_binner.centerPoint( z_index, temp );
    coords[0] += temp[0];
    coords[1] += temp[1];
    coords[2] += temp[2];
  }

}

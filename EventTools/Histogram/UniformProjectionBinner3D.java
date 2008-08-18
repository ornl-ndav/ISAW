/* 
 * File: UniformVecEventBinner.java
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

}

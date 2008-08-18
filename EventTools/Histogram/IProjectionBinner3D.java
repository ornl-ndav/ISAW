/* 
 * File: IVecEventBinner.java
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
 * Extends IEventBinner to bin events based on the
 * dot product with a specified direction vector.
 * The vector is assumed to start at the origin.
 */
public interface IProjectionBinner3D extends IEventBinner
{
  /**
   * Get a Vector3D object giving the direction of
   * the vector for this binner.
   * @return The direction vector for this binner.
   */
  Vector3D directionVec();
  
  /**
   * Find the dot product of the vector with the specified
   * components and this event binner's direction vector,
   * then get the index, based on the value of that
   * dot product.
   */
  int index( float x, float y, float z );
  
  /**
   * Find the dot product of the specified vector 
   * and this event binner's direction vector,
   * then get the index, based on the value of that
   * dot product.
   */
  int index( Vector3D vec );  
  
  /**
   * Get a Vector3D object giving the center of the  
   * bin with the specified index.
   * 
   * @param index  The index of the bin
   * 
   * @return A Vector3D object giving the center of the 
   *         specified bin.
   */
  Vector3D centerVec( int index );
  
}

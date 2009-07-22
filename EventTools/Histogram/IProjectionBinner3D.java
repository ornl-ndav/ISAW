/* 
 * File: IProjectionBinner3D.java
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
   * Get a Vector3D object giving the "lower" endpoint of the interval
   * along the direction vector, with the specified index.
   * 
   * @param index  The index of the bin
   * 
   * @return A Vector3D object giving the "lower" endpoint of the 
   *         specified interval along the direction vector.
   */
  Vector3D minVec( int index );

  /**
   * Get a Vector3D object giving the center of the interval
   * along the direction vector, with the specified index.
   * 
   * @param index  The index of the bin
   * 
   * @return A Vector3D object giving the center point of the 
   *         specified interval along the direction vector.
   */
  Vector3D centerVec( int index );

  /**
   * Get a Vector3D object giving the "upper" endpoint of the interval
   * along the direction vector, with the specified index.
   * 
   * @param index  The index of the bin
   * 
   * @return A Vector3D object giving the "upper" endpoint of the 
   *         specified interval along the direction vector.
   */
  Vector3D maxVec( int index );


  /**
   * Get a Vector3D object that is at the specified fractional position
   * along the subinterval corresponding to the specified fractional index.
   * NOTE: The returned value will only be valid if the index is at least 
   * zero and less than the number of bins.
   * 
   * @param fraction_index The fractional index of a point part way 
   *                       through a bin.  If i is the floor and
   *                       x the fractional part above the floor, the
   *                       the point in ith subinterval that that is
   *                       "x" of the way from the minVec to the
   *                       maxVec in that interval will be returned.
   * 
   * @return The point part way along the interval specified by the given
   *         fractional index.
   */
  Vector3D Vec( double fractional_index );


  /**
   * Get the coordinates of the center of the interval along
   * the direction vectorx, with the specified index.
   *
   * @param  index   The index of the interval whose center coordinates are
   *                 needed.
   * @param  coords  Array of floats, of length at least 3, in which 
   *                 the x, y and z coordinates of the interval center will be
   *                 returned in the first three positions. 
   * @throws an IllegalArgumentException, if the array coords is null or
   *         has length less than three.
   */
  void centerPoint( int index, float[] coords );

}

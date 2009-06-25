/* 
 * File: ProjectionBinner3D.java
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
 * A ProjectionBinner3D bins events based on the dot product of the event
 * coordinates with of a unit vector in a specified direction.  The vector 
 * is assumed to start at the origin.  
 */
public class ProjectionBinner3D implements IProjectionBinner3D
{
  private float        vec_x,      // components of UNIT vector giving the
                       vec_y,      // direction vector for this 
                       vec_z;      // projection binner

  private IEventBinner binner1D;   // One-dimensional binner that determines
                                   // bin positions along the direction
                                   // vector

  /**
   *  Construct a ProjectionBinner3D object using the specified vector
   *  direction and one-dimensional event binner.  
   *
   *  @param binner    The one-dimensional binner that will map between
   *                   positions along the vector and indicies.
   *  @param direction The direction vector for this binner.  This vector
   *                   is normalized to be of length 1, before it is
   *                   used to calculate the indices corresponding to
   *                   event coordinates.
   */
  public ProjectionBinner3D( IEventBinner binner,
                             Vector3D     direction )
  {
    if ( direction == null )
      throw new IllegalArgumentException("NULL direction ");
    else if ( direction.length() == 0 )
      throw new IllegalArgumentException("ZERO length direction vector");

    binner1D = binner; 

    Vector3D temp = new Vector3D( direction );    // make sure we have a
    temp.normalize();                             // unit vector, but don't
                                                  // change the parameter
    vec_x = temp.getX();
    vec_y = temp.getY();
    vec_z = temp.getZ();
  }


  @Override
  public double axisMin()
  {
    return binner1D.axisMin();
  }


  @Override
  public double axisMax()
  {
    return binner1D.axisMax();
  }


  @Override
  public int numBins()
  {
    return binner1D.numBins();
  }


  @Override
  public int index( double val )
  {
    return binner1D.index( val );
  }


  @Override
  public double centerVal( int index )
  {
    return binner1D.centerVal( index );
  }


  @Override
  public double minVal( int index )
  {
    return binner1D.minVal( index );
  }


  @Override
  public double maxVal( int index )
  {
    return binner1D.maxVal( index );
  }


  @Override
  public Vector3D minVec( int index )
  {
    Vector3D min = new Vector3D( vec_x, vec_y, vec_z );
    min.multiply( (float)binner1D.minVal(index) );
    return min;
  }


  @Override
  public Vector3D centerVec( int index )
  {
    Vector3D center = new Vector3D( vec_x, vec_y, vec_z );
    center.multiply( (float)binner1D.centerVal(index) );
    return center;
  }


  @Override
  public Vector3D maxVec( int index )
  {
    Vector3D max = new Vector3D( vec_x, vec_y, vec_z );
    max.multiply( (float)binner1D.maxVal(index) );
    return max;
  }


  @Override
  public void centerPoint( int index, float[] coords )
  {
    if ( coords == null )
         throw new IllegalArgumentException("Array coords is null");
    if ( coords.length < 3 )
         throw new IllegalArgumentException("Array coords length < 3 (" +
                                             coords.length + ")" );

    float scale = (float)binner1D.centerVal(index);
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
    return binner1D.index( val );
  }


  @Override
  public int index( Vector3D vec )
  {
    float val = vec.getX() * vec_x + vec.getY() * vec_y + vec.getZ() * vec_z;
    return binner1D.index( val );
  }


  /**
   * Convenience method to calculate the center point of a specified bin
   * in a 3D histogram.  The x_index, determines a vector in the 
   * x_binner's direction, the y_index determines a vector in the 
   * y_binner's direction, and the z_index determines a vector in the 
   * z_binner's direction.  The array coords[] is filled with the 
   * coordinates of the sum of these three vectors. 
   * NOTE: If three binners with ortho-normal directions are used to
   * for form the histogram, then the same three binners can be used
   * in this method to reconstruct the bin centers.  HOWEVER, if the
   * three binners don't have ortho-normal direction vectors, then a 
   * different set of binners are needed to reconstruct the bin center.
   * Specifically, if x_dir, y_dir and z_dir are the original three binners
   * used to form the histogram, then three new binners, with direction
   * vectors that are in the directions of the dual basis:
   *     x_dir* = y_dir X z_dir 
   *     y_dir* = z_dir X x_dir
   *     z_dir* = x_dir X y_dir
   * MUST be passed in to this method, instead of the original 3 binners.
   * In addition to changes to the direction vectors, the new binners
   * also have different length scales.  The convenience method,
   * getDualBasis() will construct the set of 3 Projection3D binners
   * that are necessary to reconstruct bin centers, from three indices.
   *
   * @param  x_index   The index of the bin in the direction of the x_binner
   * @param  y_index   The index of the bin in the direction of the y_binner
   * @param  z_index   The index of the bin in the direction of the z_binner
   * @param  x_binner  Binner for the first basis direction, "x".
   * @param  y_binner  Binner for the second basis direction, "y".
   * @param  z_binner  Binner for the third basis direction, "z".
   * @param  coords    Array with at least 3 positions, into which the sum
   *                   of x*x_vec + y*y_vec + z*zvec will be stored.  The
   *                   vectors, x_vec, y_vec and z_vec are the unit direction
   *                   vectors of the x,z and z binners.  The values, x, y, z
   *                   are distances along the direction vectors chosen so
   *                   that the vector sum gives the center point of the bin
   *                   in a 3D histogram, with the specified indices and the
   *                   the specified binners.
   */
  public static void centerPoint( int                  x_index,
                                  int                  y_index,
                                  int                  z_index,
                                  IProjectionBinner3D  x_binner,
                                  IProjectionBinner3D  y_binner,
                                  IProjectionBinner3D  z_binner,
                                  float                coords[] )
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


  /**
   * Method to construct three new projection binners, so that
   * 3D events can be mapped to three indices (ix, iy, iz)
   * that determine the correct 3D histogram bin by projecting
   * on these three "dual" binners.  These "dual" binners
   * work even if the original three binners did not use
   * orthogonal directions.  The initial set of 3D binners
   * may be thought of as dividing 3D space into a collection of
   * of parallelepipeds, with the direction vectors along the edges
   * of the parallelepipeds and the sizes of the edges determined by
   * the bins of the corresponding 3D binner.  However, if the three
   * initial 3D binners do not use orthogonal vectors, then they
   * cannot be used directly to determine indices into a 3D array
   * of values for counts in the parallelepipeds.  To do this, a
   * new set of 3D binners is needed with binner[0] perpendicular
   * to the plane determined by y_binner and z_binner; binner[1]
   * perpendicular to the plane determined by z_binner and x_binner,
   * and binner[2] perpendicular to the plane determined by
   * x_binner and y_binner.
   *
   * @param  x_binner  Binner in direction of the first parallelegram
   *                   edge.  For binning events in Q-space this would 
   *                   usually be a*.
   *
   * @param  y_binner  Binner in direction of the second parallelegram
   *                   edge.  For binning events in Q-space this would 
   *                   usually be b*.
   *
   * @param  z_binner  Binner in direction of the second parallelegram
   *                   edge.  For binning events in Q-space this would 
   *                   usually be c*.
   *
   * @return An array of three binners that can be used to place events
   *         in the correct 3D histogram bin corresponding to a 
   *         parallelepiped in 3D.
   */

  public static IProjectionBinner3D[] getDualBinners(
                                      IProjectionBinner3D x_binner,
                                      IProjectionBinner3D y_binner,
                                      IProjectionBinner3D z_binner  )
  {
    IProjectionBinner3D[] result = new IProjectionBinner3D[3];

    result[0] = getDualBinner( x_binner, y_binner, z_binner );
    result[1] = getDualBinner( y_binner, z_binner, x_binner );
    result[2] = getDualBinner( z_binner, x_binner, y_binner );

    return result;
  }

  
  private static IProjectionBinner3D getDualBinner(
                                      IProjectionBinner3D binner_1,
                                      IProjectionBinner3D binner_2,
                                      IProjectionBinner3D binner_3  )
  {
    Vector3D u_star = new Vector3D();
    u_star.cross( binner_2.directionVec(), binner_3.directionVec() );
    u_star.normalize();
    double scale_factor = 1.0/u_star.dot(binner_1.directionVec());

    if ( scale_factor < 0 )          // if u and u* are in opposition 
    {                                // directions, reflect to be basically
      u_star.multiply(-1);           // in the same direction
      scale_factor = -scale_factor;
    }

    double min = scale_factor * binner_1.axisMin();
    double max = scale_factor * binner_1.axisMax();
    int num_bins = binner_1.numBins();

    IEventBinner u_star_binner = new UniformEventBinner( min, max, num_bins );
    return new ProjectionBinner3D( u_star_binner, u_star );
  }


}

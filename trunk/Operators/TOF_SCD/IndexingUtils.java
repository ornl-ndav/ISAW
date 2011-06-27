/* File: IndexingUtils.java 
 *
 * Copyright (C) 2011, Dennis Mikkelson
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

package Operators.TOF_SCD;

import java.util.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.MathTools.*;

public class IndexingUtils
{

/** 
    STATIC method BestFit_UB: Calculates the matrix that most nearly indexes 
    the specified q_vectors, given the lattice parameters.  
    The sum of the squares of the residual errors is returned.
  
    @param  UB                  3x3 matrix that will be set to the UB matrix
    @param  q_vectors           Vector of new Vector3D objects that contains 
                                the list of q_vectors that are to be indexed
                                NOTE: There must be at least 3 q_vectors.
    @param  required_tolerance  The maximum allowed deviation of Miller indices
                                from integer values for a peak to be indexed.
    @param  a                   First unit cell edge length in Angstroms.  
    @param  b                   Second unit cell edge length in Angstroms.  
    @param  c                   Third unit cell edge length in Angstroms.  
    @param  alpha               First unit cell angle in degrees.
    @param  beta                second unit cell angle in degrees.
    @param  gamma               third unit cell angle in degrees.

    @return  This will return the sum of the squares of the residual errors.
  
    @throws  IllegalArgumentException if there are not at least 3 q vectors.
   
    @throws  std::runtime_error    exception if the UB matrix can't be found.
                                   This will happen if the q_vectors do not
                                   determine all three directions of the unit
                                   cell, or if they cannot be indexed within
                                   the required tolerance.
*/
static float BestFit_UB( Tran3D   UB,
                         Vector   q_vectors,
                         float    required_tolerance,
                         float a, float b, float c,
                         float alpha, float beta, float gamma)
{
  if ( q_vectors.size() < 3 )
  {
   throw new
     IllegalArgumentException("Three or more indexed peaks needed to find UB");
  }

                           // Make a hemisphere of possible direction for
                           // plane normals for the reciprocal space planes
                           // with normals in the direction of "a" in unit cell
  int num_steps   = 180;
  Vector dir_list = MakeHemisphereDirections(num_steps);

  float plane_distance = 1/a;

                           // First select the best fitting direction vector
                           // for a_dir from the hemisphere of possiblities.
  Vector3D a_dir = new Vector3D();
  int num_indexed = SelectDirection( a_dir,
                                     q_vectors,
                                     dir_list,
                                     plane_distance,
                                     required_tolerance );

  a_dir.multiply( a );     // Adjust the length of a_dir so a_dir "dot" q is 
                           // an integer if q is on the family of planes 
                           // perpendicular to a_dir in reciprocal space.
                           // Next, get the sub-list of q_vectors that are
                           // indexed in this direction, along with the indices.
  float[]  fit_error = { 0 };
  Vector index_vals = new Vector();
  Vector indexed_qs = new Vector();
  num_indexed = GetIndexedPeaks_1D( q_vectors,
                                    a_dir,
                                    required_tolerance,
                                    index_vals,
                                    indexed_qs,
                                    fit_error  );

                            // Use the 1D indices and qs to optimize the 
                            // plane normal, a_dir.
  fit_error[0] = BestFit_Direction( a_dir, index_vals, indexed_qs );

                            // Now do a similar process for the planes with
                            // normals in the direction of "b" in the unit cell
                            // EXCEPT, choose only from the circle of vectors
                            // that form the correct angle (gamma) with the
                            // previously found a_dir vector.
  num_steps = 10000;
  float angle_degrees = gamma;
  Vector directions = MakeCircleDirections( num_steps, a_dir, angle_degrees );
  Vector3D b_dir = new Vector3D();
  plane_distance = 1/b;
  num_indexed = SelectDirection( b_dir,
                                 q_vectors,
                                 directions,
                                 plane_distance,
                                 required_tolerance );


  b_dir.multiply( b );
  num_indexed = GetIndexedPeaks_1D( q_vectors,
                                    b_dir,
                                    required_tolerance,
                                    index_vals,
                                    indexed_qs,
                                    fit_error  );

  fit_error[0] = BestFit_Direction( b_dir, index_vals, indexed_qs );


  // Now calculate the third direction, for plane normals in the c direction,
  // using the results in UBMatriximplementationnotes.pdf, pg 3, Andre Savici.
  // Get the components of c_dir relative to an orthonormal basis with the 
  // first basis vector in the direction of a_dir and the second basis vector
  // in the (a_dir, b_dir) plane. 

  double cos_alpha = Math.cos(Math.PI/180.0 * alpha);
  double cos_beta  = Math.cos(Math.PI/180.0 * beta);
  double cos_gamma = Math.cos(Math.PI/180.0 * gamma);
  double sin_gamma = Math.sin(Math.PI/180.0 * gamma);

  double c1 = c * cos_beta;
  double c2 = c * ( cos_alpha - cos_gamma * cos_beta )/sin_gamma;
  double V  =  Math.sqrt( 1 - cos_alpha * cos_alpha
                            - cos_beta  * cos_beta
                            - cos_gamma * cos_gamma
                        + 2 * cos_alpha * cos_beta * cos_gamma );
  double c3 = c * V / sin_gamma;

  Vector3D basis_1 = new Vector3D( a_dir );
  basis_1.normalize();

  Vector3D basis_3 = new Vector3D(a_dir);
  basis_3.cross(b_dir);
  basis_3.normalize();

  Vector3D basis_2 = new Vector3D( basis_3 );
  basis_2.cross(basis_1);
  basis_2.normalize();

  basis_1.multiply( (float)c1 );
  basis_2.multiply( (float)c2 );
  basis_3.multiply( (float)c3 );
  Vector3D c_dir = new Vector3D( basis_1 );
  c_dir.add( basis_2 );
  c_dir.add( basis_3 );


                            // Optimize the c_dir vector as before

  num_indexed = GetIndexedPeaks_1D( q_vectors,
                                    c_dir,
                                    required_tolerance,
                                    index_vals,
                                    indexed_qs,
                                    fit_error  );

  fit_error[0] = BestFit_Direction( c_dir, index_vals, indexed_qs );


                            // Now, using the plane normals for all three
                            // families of planes, get a consistent indexing
                            // discarding any peaks that are not indexed in 
                            // all three directions.
  Vector miller_ind = new Vector();
  num_indexed = GetIndexedPeaks_3D( q_vectors,
                                    a_dir, b_dir, c_dir,
                                    required_tolerance,
                                    miller_ind,
                                    indexed_qs,
                                    fit_error );

                            // Finally, use the indexed peaks to get an 
                            // optimized UB that matches the indexing
                           
  fit_error[0] = BestFit_UB( UB, miller_ind, indexed_qs );

  return fit_error[0];
}


/** 
    STATIC method BestFit_UB: Calculates the matrix that most nearly maps
    the specified hkl_vectors to the specified q_vectors.  The calculated
    UB minimizes the sum squared differences between UB*(h,k,l) and the
    corresponding (qx,qy,qz) for all of the specified hkl and Q vectors.
    The sum of the squares of the residual errors is returned.
  
    @param  UB           3x3 matrix that will be set to the UB matrix
    @param  hkl_vectors  Vector of new Vector3D objects that contains the 
                         list of hkl values
    @param  q_vectors    Vector of new Vector3D objects that contains the list
                         of q_vectors that are indexed by the corresponding hkl
                         vectors.
    NOTE: The number of hkl_vectors and q_vectors must be the same, and must
          be at least 3.
  
    @return  This will return the sum of the squares of the residual errors.
  
    @throws  IllegalArgumentException if there are not at least 3
                                      hkl and q vectors, or if the numbers of
                                      hkl and q vectors are not the same.
   
    @throws  std::runtime_error    exception if the QR factorization fails or
                                   the UB matrix can't be calculated or if 
                                   UB is a singular matrix.
*/  
static float BestFit_UB( Tran3D UB,
                         Vector hkl_vectors, 
                         Vector q_vectors )
{
  if ( hkl_vectors.size() < 3 ) 
  {
   throw new 
     IllegalArgumentException("Three or more indexed peaks needed to find UB");
  }

  if ( hkl_vectors.size() != q_vectors.size() )
  {
   throw new 
     IllegalArgumentException("Number of hkl_vectors != number of q_vectors");
  }

  float sum_sq_error = 0;
                                      // Make the H-transpose matrix from the
                                      // hkl vectors and form QR factorization
  double[][] H_transpose = new double[ hkl_vectors.size() ][ 3 ];
  for ( int i = 0; i < hkl_vectors.size(); i++ )
  {
    Vector3D hkl = (Vector3D)( hkl_vectors.elementAt(i) );
    H_transpose[i][0] = hkl.getX();
    H_transpose[i][1] = hkl.getY();
    H_transpose[i][2] = hkl.getZ();
  }

  double[][] Q = LinearAlgebra.QR_factorization( H_transpose );

  float[][] UB_array = new float[3][3];

  double[] b = new double[ q_vectors.size() ];
  for ( int row = 0; row < 3; row++ )
  {
    for ( int i = 0; i < q_vectors.size(); i++ )
    {
      Vector3D q = (Vector3D)( q_vectors.elementAt(i) );
      b[i] = q.get()[row];
    }

    float error = (float)LinearAlgebra.QR_solve( H_transpose, Q, b );

    for ( int col = 0; col < 3; col++ )
      UB_array[row][col] = (float)b[col];

    sum_sq_error += error*error;
  }

  UB.set( UB_array );

  return sum_sq_error;
}

/** 
    STATIC method BestFit_Direction: Calculates the vector for which the
    dot product of the the vector with each of the specified Qxyz vectors 
    is most nearly the corresponding integer index.  The calculated best_vec
    minimizes the sum squared differences between best_vec dot (qx,qy,z) 
    and the corresponding index for all of the specified Q vectors and 
    indices.  The sum of the squares of the residual errors is returned.
    NOTE: This method is similar the BestFit_UB method, but this method only
          optimizes the plane normal in one direction.  Also, this optimizes
          the mapping from (qx,qy,qz) to one index (Q to index), while the 
          BestFit_UB method optimizes the mapping from three (h,k,l) to
          (qx,qy,qz) (3 indices to Q).
  
    @param  best_vec     new Vector3D vector that will be set to a vector whose 
                         direction most nearly corresponds to the plane
                         normal direction and whose magnitude is d.  The 
                         corresponding plane spacing in reciprocal space 
                         is 1/d.
    @param  index_values Vector of ints that contains the list of indices 
    @param  q_vectors    Vector of new Vector3D objects that contains the list  
                         of q_vectors that are indexed in one direction by the 
                         corresponding index values.
    NOTE: The number of index_values and q_vectors must be the same, and must
          be at least 3.
  
    @return  This will return the sum of the squares of the residual errors.
  
    @throws  IllegalArgumentException if there are not at least 3
                                   indices and q vectors, or if the numbers of
                                   indices and q vectors are not the same.
   
    @throws  std::runtime_error    exception if the QR factorization fails or
                                   the best direction can't be calculated.
*/

static float BestFit_Direction( Vector3D best_vec,
                                Vector   index_values,
                                Vector   q_vectors )
{
  if ( index_values.size() < 3 )
   throw new IllegalArgumentException("Three or more indexed values needed");

  if ( index_values.size() != q_vectors.size() )
   throw new 
    IllegalArgumentException( "Number of index_values != number of q_vectors");

                                     // Make the H-transpose matrix from the
                                     // q vectors and form QR factorization
  double[][] H_transpose = new double[ q_vectors.size() ][ 3 ];
  for ( int i = 0; i < q_vectors.size(); i++ )
  {
    Vector3D q = (Vector3D)( q_vectors.elementAt(i) );
    H_transpose[i][0] = q.getX();
    H_transpose[i][1] = q.getY();
    H_transpose[i][2] = q.getZ();
  }

  double[][] Q = LinearAlgebra.QR_factorization( H_transpose );

  double[] b = new double[ index_values.size() ];
  for ( int i = 0; i < index_values.size(); i++ )
    b[i] = (Integer)( index_values.elementAt(i) ); 

  float error = (float)LinearAlgebra.QR_solve( H_transpose, Q, b );

  best_vec.set( (float)b[0], (float)b[1], (float)b[2] );

  return error*error;
}


/**
  Check whether or not the components of the specified vector are within
  the specified tolerance of integer values, other than (0,0,0).
  @param hkl        A new Vector3D object containing what may be valid Miller indices
                    for a peak.
  @param tolerance  The maximum acceptable deviation from integer values for
                    the Miller indices.
  @return true if all components of the vector are within the tolerance of
               integer values (h,k,l) and (h,k,l) is NOT (0,0,0)
 */

static boolean ValidIndex( Vector3D hkl, float tolerance )
{
  boolean valid_index = false;

  int h,k,l;
                                        // since C++ lacks a round() we need
                                        // to do it ourselves!
  h = Math.round( hkl.getX() );
  k = Math.round( hkl.getY() );
  l = Math.round( hkl.getZ() );

  if ( h != 0 || k != 0 || l != 0 )   // check if indexed, but not as (0,0,0)
  {
    if ( (Math.abs( hkl.getX() - h ) <= tolerance) &&
         (Math.abs( hkl.getY() - k ) <= tolerance) &&
         (Math.abs( hkl.getZ() - l ) <= tolerance) )
      valid_index = true;
  }

  return valid_index;
}


/**
   Calculate the number of Q vectors that are mapped to integer h,k,l 
   values by UB.  Each of the Miller indexes, h, k and l must be within
   the specified tolerance of an integer, in order to count the peak
   as indexed.  Also, if (h,k,l) = (0,0,0) the peak will NOT be counted
   as indexed, since (0,0,0) is not a valid index of any peak.
  
   @param UB           A 3x3 matrix of doubles holding the UB matrix
   @param q_vectors    Vector of new Vector3D objects that contains the list of 
                       q_vectors that are indexed by the corresponding hkl
                       vectors.
   @param tolerance    The maximum allowed distance between each component
                       of UB*Q and the nearest integer value, required to
                       to count the peak as indexed by UB.
   @return A non-negative integer giving the number of peaks indexed by UB. 
 */
static int NumberIndexed( Tran3D UB,
                          Vector q_vectors,
                          float tolerance )
{
  float determinant = UB.determinant();
  if ( Math.abs( determinant ) < 1.0e-5 )
    throw new IllegalArgumentException("UB is singular (det < 1e-5)");

  Tran3D UB_inverse = new Tran3D( UB );
  UB_inverse.invert();

  Vector3D hkl = new Vector3D();
  int count = 0;
  for ( int i = 0; i < q_vectors.size(); i++ )
  {
    UB_inverse.apply_to( (Vector3D)(q_vectors.elementAt(i)), hkl );
    if ( ValidIndex( hkl, tolerance ) )
      count++;
  }

  return count;
}


/**
  Given one plane normal direction for a family of parallel planes in 
  reciprocal space, find the peaks that lie on these planes to within the 
  specified tolerance.  The direction is specified as a vector with length 
  "a" if the plane spacing in reciprocal space is 1/a.  In that way, the 
  dot product of a peak Qxyz with the direction vector will be an integer 
  if the peak lies on one of the planes.   

  @param q_vectors           List of new Vector3D peaks in reciprocal space
  @param direction           Direction vector in the direction of the 
                             normal vector for a family of parallel planes
                             in reciprocal space.  The length of this vector 
                             must be the reciprocal of the plane spacing.
  @param required_tolerance  The maximum allowed error (as a faction of
                             the corresponding Miller index) for a peak
                             q_vector to be counted as indexed.
  @param index_vals          List of the one-dimensional Miller indices peaks
                             that were indexed in the specified direction.
  @param indexed_qs          List of Qxyz value for the peaks that were
                             indexed indexed in the specified direction.
  @param fit_error           The sum of the squares of the distances from
                             integer values for the projections of the 
                             indexed q_vectors on the specified direction.

  @return The number of q_vectors that are indexed to within the specified
          tolerance, in the specified direction.

 */
static int GetIndexedPeaks_1D( Vector    q_vectors,
                               Vector3D  direction,
                               float     required_tolerance,
                               Vector    index_vals,
                               Vector    indexed_qs,
                               float[]   fit_error )
{
  int    nearest_int;
  float  proj_value;
  float  error;
  int    num_indexed = 0;
  index_vals.clear();
  indexed_qs.clear();
  fit_error[0] = 0;

  for ( int q_num = 0; q_num < q_vectors.size(); q_num++ )
  {
    proj_value = direction.dot( (Vector3D)( q_vectors.elementAt(q_num) ) );
    nearest_int = Math.round( proj_value );
    error = Math.abs( proj_value - nearest_int );
    if ( error < required_tolerance )
    {
      fit_error[0] += error * error;
      indexed_qs.add( q_vectors.elementAt(q_num) );
      index_vals.add( nearest_int );
      num_indexed++;
    }
  }

  return num_indexed;
}


/**
  Given three plane normal directions for three families of parallel planes in 
  reciprocal space, find the peaks that lie on these planes to within the 
  specified tolerance.  The three directions are specified as vectors with
  lengths that are the reciprocals of the corresponding plane spacings.  In
  that way, the dot product of a peak Qxyz with one of the direction vectors
  will be an integer if the peak lies on one of the planes corresponding to
  that direction.  If the three directions are properly chosen to correspond
  to the unit cell edges, then the resulting indices will be proper Miller
  indices for the peaks.  This method is similar to GetIndexedPeaks_3D, but
  checks three directions simultaneously and requires that the peak lies
  on all three families of planes simultaneously and does NOT index as (0,0,0).

  @param q_vectors           List of new Vector3D peaks in reciprocal space
  @param direction_1         Direction vector in the direction of the normal
                             vector for the first family of parallel planes.
  @param direction_2         Direction vector in the direction of the normal
                             vector for the second family of parallel planes.
  @param direction_3         Direction vector in the direction of the normal
                             vector for the third family of parallel planes.
  @param required_tolerance  The maximum allowed error (as a faction of
                             the corresponding Miller index) for a peak
                             q_vector to be counted as indexed.
  @param index_vals          List of the Miller indices (h,k,l) of peaks
                             that were indexed in all specified directions.
  @param indexed_qs          List of Qxyz value for the peaks that were
                             indexed indexed in all specified directions.
  @param fit_error           The sum of the squares of the distances from
                             integer values for the projections of the 
                             indexed q_vectors on the specified directions.

  @return The number of q_vectors that are indexed to within the specified
          tolerance, in the specified direction.

 */
static int GetIndexedPeaks_3D( Vector    q_vectors,
                               Vector3D  direction_1,
                               Vector3D  direction_2,
                               Vector3D  direction_3,
                               float     required_tolerance,
                               Vector    miller_indices,
                               Vector    indexed_qs,
                               float[]   fit_error )
{
    float    projected_h;
    float    projected_k;
    float    projected_l;
    float    h_error;
    float    k_error;
    float    l_error;
    int      h_int;
    int      k_int;
    int      l_int;
    Vector3D hkl = new Vector3D();
    int      num_indexed = 0;

    miller_indices.clear();
    indexed_qs.clear();
    fit_error[0] = 0;

    for ( int q_num = 0; q_num < q_vectors.size(); q_num++ )
    {
      Vector3D vec = (Vector3D)q_vectors.elementAt(q_num);
      projected_h = direction_1.dot( vec );
      projected_k = direction_2.dot( vec );
      projected_l = direction_3.dot( vec );

      hkl.set( projected_h, projected_k, projected_l );

      if ( ValidIndex( hkl, required_tolerance ) )
      {
        h_int = Math.round( projected_h );
        k_int = Math.round( projected_k );
        l_int = Math.round( projected_l );

        h_error = Math.abs( projected_h - h_int );
        k_error = Math.abs( projected_k - k_int );
        l_error = Math.abs( projected_l - l_int );

        fit_error[0] += h_error*h_error + k_error*k_error + l_error*l_error;

        indexed_qs.add( q_vectors.elementAt(q_num) );

        Vector3D miller_ind = new Vector3D( h_int, k_int, l_int );
        miller_indices.add( miller_ind );

        num_indexed++;
      }
    }

  return num_indexed;
}


/**
  Make a list of directions, approximately uniformly distributed over a
  hemisphere, with the angular separation between direction vectors 
  approximately 90 degrees/n_steps.
  NOTE: This method provides a list of possible directions for plane 
        normals for reciprocal lattice planes.  This facilitates a 
        brute force search for lattice planes with a specific spacing
        between planes.  This will be used for finding the UB matrix, 
        given the lattice parameters.
  @param n_steps   The number of subdivisions in latitude in the upper
                   hemisphere.
  @return A Vector containing directions distributed over the hemisphere
          with y-coordinate at least zero.
 */
static Vector MakeHemisphereDirections( int n_steps )
{
  if ( n_steps <= 0 )
  {
    throw new IllegalArgumentException("n_steps must be greater than 0");
  }

  Vector direction_list = new Vector();

  double angle_step = Math.PI / (2*n_steps);

  for ( double phi = 0; phi <= (1.0001)*Math.PI/2; phi += angle_step )
  {
    double r = Math.sin(phi);

    int n_theta = (int)( 2 * Math.PI * r / angle_step + 0.5 );
     
    double theta_step;
      
    if ( n_theta == 0 )                     // n = ( 0, 1, 0 ).  Just
      theta_step = 2 * Math.PI + 1;         // use one vector at the pole

    else
      theta_step = 2 * Math.PI / n_theta;
      
    double last_theta = 2 * Math.PI - theta_step / 2;

                                            // use half the equator to avoid
                                            // vectors that are the negatives
                                            // of other vectors in the list.
    if ( Math.abs(phi - Math.PI/2) < angle_step/2 )
      last_theta = Math.PI - theta_step/2; 

    for ( double theta = 0; theta < last_theta; theta += theta_step )
    {
      Vector3D direction = new Vector3D( (float)(r*Math.cos(theta)), 
                                         (float)(Math.cos(phi)), 
                                         (float)(r*Math.sin(theta)) );
      direction_list.add( direction );
    }
  }

  return direction_list;
}


/**
  Make a list of directions, uniformly distributed around a circle, all of
  which form the specified angle with the specified axis. 

  @param n_steps   The number of vectors to generate around the circle. 

  @retrun A Vector containing direction vectors forming the same angle
          with the axis.
 */
static Vector MakeCircleDirections( int      n_steps,
                                    Vector3D axis,
                                    float    angle_degrees )
{
  axis = new Vector3D( axis );  // make copy of axis, so we don't change it!

  if ( n_steps <= 0 )
  {
    throw new IllegalArgumentException("n_steps must be greater than 0");
  }
                               // first get a vector perpendicular to axis
  float max_component = Math.abs( axis.getX() );
  float min_component = Math.abs( axis.getX() );
  int min_index = 0;
  for ( int i = 1; i < 3; i++ )
  {
    if ( Math.abs( axis.get()[i] ) < min_component )
    {
      min_component = Math.abs( axis.get()[i] );
      min_index = i;
    }
    if ( Math.abs( axis.get()[i] ) > max_component )
      max_component = Math.abs( axis.get()[i] );
  }

  if ( max_component == 0 )
    throw new IllegalArgumentException("Axis vector must be non-zero!");

  Vector3D second_vec = new Vector3D( 0, 0, 0 );
  if ( min_index == 0 )
    second_vec.set( 1, 0, 0 );
  else if ( min_index == 1 )
    second_vec.set( 0, 1, 0 );
  else if ( min_index == 2 )
    second_vec.set( 0, 0, 1 );

  axis.normalize();
  Vector3D perp_vec = new Vector3D( second_vec );
  perp_vec.cross( axis );
  perp_vec.normalize();

                                // next get a vector that is the specified 
                                // number of degrees away from the axis
  Tran3D rotation = new Tran3D();
  rotation.setRotation( angle_degrees, perp_vec );
  Vector3D vector_at_angle = new Vector3D();
  rotation.apply_to( axis, vector_at_angle ); 
  vector_at_angle.normalize();

                                // finally, form the circle of directions 
                                // consisting of vectors that are at the 
                                // specified angle from the original axis
  float angle_step = 360.0f / n_steps;
  Vector directions = new Vector();
  for ( int i = 0; i < n_steps; i++ )
  {
    rotation.setRotation( i * angle_step, axis ); 
    Vector3D vec = new Vector3D( vector_at_angle );
    rotation.apply_to( vector_at_angle, vec );
    directions.add( vec );
  }

  return directions;
}


/**
  Choose the direction vector that most nearly corresponds to a family of
  planes in the list of q_vectors, with spacing equal to the specified
  plane_spacing.  The direction is chosen from the specified direction_list.

  @param  best_direction      This will be set to the direction that minimizes
                              the sum squared distances of projections of peaks
                              from integer multiples of the specified plane
                              spacing.
  @param  q_vectors           List of peak positions, specified according to
                              the convention that |q| = 1/d.  (i.e. Q/2PI)
  @param  direction_list      List of possible directions for plane normals.
                              Initially, this will be a long list of possible
                              directions from MakeHemisphereDirections().
  @param  plane_spacing       The required spacing between planes in reciprocal
                              space.
  @param  required_tolerance  The maximum deviation of the component of a
                              peak Qxyz in the direction of the best_direction
                              vector for that peak to count as being indexed. 
                              NOTE: The tolerance is specified in terms of
                              Miller Index.  That is, the distance between 
                              adjacent planes is effectively normalized to one
                              for measuring the error in the computed index.
  @return The number of peaks that lie within the specified tolerance of the
          family of planes with normal direction = best_direction and with 
          spacing given by plane_spacing.
 */
static int SelectDirection( Vector3D best_direction,
                            Vector   q_vectors,
                            Vector   direction_list,
                            float    plane_spacing,
                            float    required_tolerance )
{
    float  dot_product;
    int    nearest_int;
    float  error;
    float  sum_sq_error;
    float  min_sum_sq_error = 1.0e30f;

    for ( int dir_num = 0; dir_num < direction_list.size(); dir_num++ )
    {
      sum_sq_error = 0;
      Vector3D direction = (Vector3D)(direction_list.elementAt( dir_num ));
      direction.multiply( 1/plane_spacing );
      for ( int q_num = 0; q_num < q_vectors.size(); q_num++ )
      {
        dot_product = direction.dot( (Vector3D)(q_vectors.elementAt( q_num )));
        nearest_int = Math.round( dot_product );
        error = Math.abs( dot_product - nearest_int );
        sum_sq_error += error * error;
      }

      if ( sum_sq_error < min_sum_sq_error )
      {
        min_sum_sq_error = sum_sq_error;
        best_direction.set( direction );
      }
    }

    float proj_value  = 0;
    int    num_indexed = 0;
    for ( int q_num = 0; q_num < q_vectors.size(); q_num++ )
    {
      proj_value = best_direction.dot((Vector3D)(q_vectors.elementAt( q_num )));
      nearest_int = Math.round( proj_value );
      error = Math.abs( proj_value - nearest_int );
      if ( error < required_tolerance )
        num_indexed++;
    }

  best_direction.normalize();

  return num_indexed;
}

}

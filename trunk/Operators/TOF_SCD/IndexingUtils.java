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
import java.io.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.MathTools.*;

public class IndexingUtils
{

private static float angle( Vector3D v1, Vector3D v2 )
{
  v1 = new Vector3D( v1 );
  v2 = new Vector3D( v2 );

  v1.normalize();
  v2.normalize();

  float dot = v1.dot(v2);
  if ( dot > 1 )             // trap rounding errors
    return 0;
  else if ( dot < -1 )
    return 180;

  float angle_rad = (float)Math.acos( dot );

  return (float)(angle_rad * 180 / Math.PI);
}



/** 
    STATIC method BestFit_UB_1: First attempt at calculating the matrix that most 
    nearly indexes the specified q_vectors, given the lattice parameters.  
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
public static float BestFit_UB_1( Tran3D   UB,
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

  System.out.println("\nSelected a_dir = " + a_dir + 
                     " magnitude = "+a_dir.length());
  for ( int i = 0; i < 5; i++ )
  {
  num_indexed = GetIndexedPeaks_1D( q_vectors,
                                    a_dir,
                                    required_tolerance,
                                    index_vals,
                                    indexed_qs,
                                    fit_error  );

                            // Use the 1D indices and qs to optimize the 
                            // plane normal, a_dir.
  fit_error[0] = BestFit_Direction( a_dir, index_vals, indexed_qs );
  System.out.println("Best a_dir = " + a_dir + 
                     " magnitude = " + a_dir.length() +
                     " num indexed = " + num_indexed  +
                     " fit_error = " + fit_error[0] );
  }
                            // Now do a similar process for the planes with
                            // normals in the direction of "b" in the unit cell
                            // EXCEPT, choose only from the circle of vectors
                            // that form the correct angle (gamma) with the
                            // previously found a_dir vector.
  float angle_step = 90.0f /(2*num_steps);
  num_steps *= 10;

  float angle = gamma - 0.5f; 
  Vector directions = new Vector();
  while ( angle <= gamma + 0.5 )
  {
    Vector extra_dir = MakeCircleDirections( num_steps, a_dir, angle );
    for ( int i = 0; i < extra_dir.size(); i++ )
      directions.add( extra_dir.elementAt(i) );
    angle += angle_step; 
  }


  Vector3D b_dir = new Vector3D();
  plane_distance = 1/b;
  num_indexed = SelectDirection( b_dir,
                                 q_vectors,
                                 directions,
                                 plane_distance,
                                 required_tolerance );


  b_dir.multiply( b );
  System.out.println("\nSelected b_dir = " + b_dir + 
                     " magnitude = "+b_dir.length());
  System.out.println("Gamma (selected) = " + angle(a_dir,b_dir) );
  for ( int i = 0; i < 5; i++ )
  {
  num_indexed = GetIndexedPeaks_1D( q_vectors,
                                    b_dir,
                                    required_tolerance,
                                    index_vals,
                                    indexed_qs,
                                    fit_error  );

  fit_error[0] = BestFit_Direction( b_dir, index_vals, indexed_qs );
  System.out.println("Best b_dir = " + b_dir + 
                     " magnitude = " + b_dir.length() +
                     " num indexed = " + num_indexed  +
                     " fit_error = " + fit_error[0] );
  }

  System.out.println("Gamma (fitted) = " + angle(a_dir,b_dir) );

  // Now calculate the third direction, for plane normals in the c direction,
  // using the results in UBMatriximplementationnotes.pdf, pg 3, Andre Savici.
  // Get the components of c_dir relative to an orthonormal basis with the 
  // first basis vector in the direction of a_dir and the second basis vector
  // in the (a_dir, b_dir) plane. 

angle_step = 1.0f/8.0f;
Vector c_directions = new Vector();
for ( int i = -4; i <= 4; i++ )
for ( int j = -4; j <= 4; j++ )
{
  double cos_alpha = Math.cos(Math.PI/180.0 * ( alpha + i * angle_step ));
  double cos_beta  = Math.cos(Math.PI/180.0 * ( beta + j * angle_step ));
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

  c_dir.normalize();
  c_directions.add( c_dir );
  c_dir.multiply(-1);
  c_directions.add( c_dir );
}

  Vector3D c_dir = new Vector3D();
  plane_distance = 1/c;
  num_indexed = SelectDirection( c_dir,
                                 q_vectors,
                                 c_directions,
                                 plane_distance,
                                 required_tolerance );
  c_dir.multiply(c);
  System.out.println("Alpha (selected) = " + angle(c_dir,b_dir) );
  System.out.println("Beta  (selected) = " + angle(c_dir,a_dir) );
                            // Optimize the c_dir vector as before

  System.out.println("\nSelected c_dir = " + c_dir + 
                     " magnitude = "+c_dir.length());
  for ( int i = 0; i < 7; i++ )
  {
  num_indexed = GetIndexedPeaks_1D( q_vectors,
                                    c_dir,
                                    required_tolerance,
                                    index_vals,
                                    indexed_qs,
                                    fit_error  );

  fit_error[0] = BestFit_Direction( c_dir, index_vals, indexed_qs );
  System.out.println("Best c_dir = " + c_dir + 
                     " magnitude = " + c_dir.length() +
                     " num indexed = " + num_indexed +
                     " fit_error = " + fit_error[0] );
  }
  System.out.println("Alpha (fitted  ) = " + angle(c_dir,b_dir) );
  System.out.println("Beta  (fitted  ) = " + angle(c_dir,a_dir) );

  System.out.println();

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
  System.out.println("Final Version Fit Error = " + fit_error[0] +
                     " Number indexed = " + num_indexed );
  System.out.println("Alpha (final) = " + angle(c_dir,b_dir) );
  System.out.println("Beta  (final) = " + angle(c_dir,a_dir) );
  System.out.println("Gamma (final) = " + angle(a_dir,b_dir) );

  return fit_error[0];
}


/** 
    STATIC method BestFit_UB_2: Second attempt at calculating the matrix that most 
    nearly indexes the specified q_vectors, given the lattice parameters.  
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
public static float BestFit_UB_2( Tran3D   UB,
                                Vector   q_vectors,
                                float    required_tolerance,
                                float a, float b, float c,
                                float alpha, float beta, float gamma)
{
  float degrees_per_step = 0.25f;

  Vector<Vector3D> a_dir_list = new Vector<Vector3D>(); 
  Vector<Vector3D> b_dir_list = new Vector<Vector3D>(); 
  Vector<Vector3D> c_dir_list = new Vector<Vector3D>(); 
  float required_fraction = 0.6f;
  float angle_tolerance   = 0.05f;
  float edge_tolerance    = 0.2f;
  GetPossibleDirectonLists( q_vectors, a_dir_list, b_dir_list, c_dir_list,
                            a, b, c, edge_tolerance,
                            required_tolerance, required_fraction,
                            angle_tolerance, degrees_per_step );


  degrees_per_step = 2;
  Vector3D a_dir = new Vector3D();
  Vector3D b_dir = new Vector3D();
  Vector3D c_dir = new Vector3D();


  float error =  SelectDirections( a_dir, b_dir, c_dir,
                                   a, b, c,
                                   alpha, beta, gamma,
                                   q_vectors,
                                   degrees_per_step );

  System.out.println("Initial error = " + error );
  System.out.println("Alpha (selected) = " + angle(c_dir,b_dir) );
  System.out.println("Beta  (selected) = " + angle(c_dir,a_dir) );
  System.out.println("Gamma (selected) = " + angle(a_dir,b_dir) );

  Vector miller_ind = new Vector();
  Vector indexed_qs = new Vector();
  float[] fit_error = new float[1];
  int num_indexed = GetIndexedPeaks_3D( q_vectors,
                                        a_dir, b_dir, c_dir,
                                        required_tolerance,
                                        miller_ind,
                                        indexed_qs,
                                        fit_error );
 

  fit_error[0] = BestFit_UB( UB, miller_ind, indexed_qs );
  System.out.println("Final Version Fit Error = " + fit_error[0] +
                     " Number indexed = " + num_indexed );
  System.out.println("Alpha (final) = " + angle(c_dir,b_dir) );
  System.out.println("Beta  (final) = " + angle(c_dir,a_dir) );
  System.out.println("Gamma (final) = " + angle(a_dir,b_dir) );

  return fit_error[0];
}



/** 
    STATIC method BestFit_UB: Current attempt at calculating the matrix that 
    most nearly indexes the specified q_vectors, given the lattice parameters.  
    The sum of the squares of the residual errors is returned.
  
    @param  UB                  3x3 matrix that will be set to the UB matrix
    @param  q_vectors           Vector of new Vector3D objects that contains 
                                the list of q_vectors that are to be indexed
                                NOTE: There must be at least 2 linearly 
                                independent q_vectors.  If there are only 2
                                q_vectors, no least squares optimization of
                                the UB matrix will be done.
    @param  required_tolerance  The maximum allowed deviation of Miller indices
                                from integer values for a peak to be indexed.
    @param  a                   First unit cell edge length in Angstroms.  
    @param  b                   Second unit cell edge length in Angstroms.  
    @param  c                   Third unit cell edge length in Angstroms.  
    @param  alpha               First unit cell angle in degrees.
    @param  beta                second unit cell angle in degrees.
    @param  gamma               third unit cell angle in degrees.

    @return  This will return the sum of the squares of the residual errors.
  
    @throws  IllegalArgumentException if there are not at least 2 q vectors.
   
    @throws  std::runtime_error    exception if the UB matrix can't be found.
*/
public static float BestFit_UB( Tran3D            UB,
                                Vector<Vector3D>  q_vectors,
                                float             required_tolerance,
                                float a, float b, float c,
                                float alpha, float beta, float gamma)
{
  if ( q_vectors.size() < 3 )
    throw new IllegalArgumentException("Need at least 2 q_vectors to find UB");

                                    // First, sort the peaks in order of 
                                    // increasing |Q| so that we can try to
                                    // index the low |Q| peaks first.
  q_vectors = SortOnVectorMagnitude( q_vectors );

  int num_initial = 16;
  if ( num_initial > q_vectors.size() )
    num_initial = q_vectors.size();
 
  Vector<Vector3D> some_qs = new Vector<Vector3D>();
  for ( int i = 0; i < num_initial; i++ ) 
    some_qs.add( q_vectors.elementAt(i) );

  float degrees_per_step = 2.0f;

  float error = ScanFor_UB( UB,
                            a, b, c, alpha, beta, gamma,
                            some_qs,
                            degrees_per_step,
                            required_tolerance );

  float[] fit_error = new float[1];
  int     num_indexed;
  Vector miller_ind = new Vector();
  Vector indexed_qs = new Vector();
                                     // If we have enough q vectors, try to
                                     // optimize the directions (ie. UB matrix)
  if ( some_qs.size() >= 3 )
  {
    try
    {
      num_indexed = GetIndexedPeaks( some_qs, UB, required_tolerance,
                                     miller_ind, indexed_qs, fit_error );

      fit_error[0] = BestFit_UB( UB, miller_ind, indexed_qs );
    }
    catch ( Exception ex )
    {
      System.out.println("Could not refine initial UB using only " 
                         + some_qs.size() );
    }
  }
                                     // now gradually bring in the remainging
                                     // peaks and re-optimize the UB to index
                                     // them as well
  int     count = 0;
  while ( num_initial < q_vectors.size() )
  {
    count++;
    num_initial *= 1.5;
    if ( num_initial >= q_vectors.size() ) 
      num_initial = q_vectors.size();

    for ( int i = some_qs.size(); i < num_initial; i++ )
      some_qs.add( q_vectors.elementAt(i) );

    num_indexed = GetIndexedPeaks( some_qs, UB, required_tolerance,
                                   miller_ind, indexed_qs, fit_error );

    fit_error[0] = BestFit_UB( UB, miller_ind, indexed_qs );
  }


  if ( q_vectors.size() >= 3 )    // do one more refinement using all peaks
  {
    try
    {
      num_indexed = GetIndexedPeaks( q_vectors, UB, required_tolerance,
                                     miller_ind, indexed_qs, fit_error );
      fit_error[0] = BestFit_UB( UB, miller_ind, indexed_qs );
    }
    catch ( Exception ex )
    {
      System.out.println("Could not refine initial UB using specified " +
                         "q_vectors " + some_qs.size() );
    }
  }

  return fit_error[0];
}


/**
 * Sort the specified list of Vector3D objects in order of increasing magnitude
 * and return a new Vector containing references to the original vectors, but in
 * order of increasing magnitude.
 *
 * @ param Vector of Vector3D obejcts that are to be sorted
 * @ return A new Vector containing references to the orginanl Vector3D objects, but
 *          in increasing order.
 */
public static Vector<Vector3D> 
         SortOnVectorMagnitude( Vector<Vector3D> q_vectors )
{
  Vector3D list[] = new Vector3D[ q_vectors.size() ];
  for ( int i = 0; i < list.length; i++ )
    list[i] = q_vectors.elementAt(i);

  boolean decreasing = false;
  Arrays.sort( list, new Vector3D_MagnitudeComparator(decreasing) );

  Vector<Vector3D> sorted_qs = new Vector<Vector3D>( list.length );
  for ( int i = 0; i < list.length; i++ )
    sorted_qs.add( list[i] );

  return sorted_qs;
}


   private static class Vector3D_MagnitudeComparator implements Comparator
   {
     boolean decreasing;

     /**
      *  Construct a comparator to sort a list of peaks in increasing or
      *  decreasing order based on |Q|.
      *
      *  @param  decreasing  Set true to sort from largest to smallest;
      *                      set false to sort from smallest to largest.
      */
     public Vector3D_MagnitudeComparator( boolean decreasing )
     {
       this.decreasing = decreasing;
     }

     /**
       *  Compare two IPeakQ objects based on the magnitude of their Q value.
       *
       *  @param  peak_1   The first  peak
       *  @param  peak_2   The second peak 
       *
       *  @return A positive integer if peak_1's run number is greater than
       */
       public int compare( Object peak_1, Object peak_2 )
       {
         float[] q1  = ((Vector3D)peak_1).get();
         float[] q2  = ((Vector3D)peak_2).get();

         float mag_q1 = q1[0]*q1[0] + q1[1]*q1[1] + q1[2]*q1[2];
         float mag_q2 = q2[0]*q2[0] + q2[1]*q2[1] + q2[2]*q2[2];

         if ( decreasing )
         {
           if ( mag_q1 < mag_q2 )
             return 1;
           else if  ( mag_q1 > mag_q2 )
             return -1;
         }
         else
         {
           if ( mag_q1 < mag_q2 )
             return -1;
           else if  ( mag_q1 > mag_q2 )
             return 1;
         }

         return 0;
       }
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
public static float BestFit_UB( Tran3D UB,
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

public static float BestFit_Direction( Vector3D best_vec,
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

public static boolean ValidIndex( Vector3D hkl, float tolerance )
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
public static int NumberIndexed( Tran3D UB,
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
public static int GetIndexedPeaks_1D( Vector    q_vectors,
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
public static int GetIndexedPeaks_3D( Vector    q_vectors,
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
  Given a list of peak positions and a UB matrix, get the list of Miller
  indices and corresponding peak positions for the peaks that are indexed
  to within a specified tolerance, by the UB matrix.

  @param q_vectors           List of positions of peaks in reciprocal space
  @param UB                  The UB matrix that will be used to index the
                             peaks.
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
          tolerance, by the specified UB matrix. 
 */
public static int GetIndexedPeaks( Vector    q_vectors,
                                   Tran3D    UB,
                                   float     required_tolerance,
                                   Vector    miller_indices,
                                   Vector    indexed_qs,
                                   float[]   fit_error )
{
    float    error;
    int      num_indexed = 0;

    miller_indices.clear();
    indexed_qs.clear();
    fit_error[0] = 0;

    Tran3D UB_inverse = new Tran3D( UB );
    UB_inverse.invert();

    Vector3D hkl_vec = new Vector3D();
    for ( int q_num = 0; q_num < q_vectors.size(); q_num++ )
    {
      Vector3D q_vec = (Vector3D)q_vectors.elementAt(q_num);

      UB_inverse.apply_to( q_vec, hkl_vec );

      if ( ValidIndex( hkl_vec, required_tolerance ) )
      {
        float[] hkl_arr = hkl_vec.get();
        for ( int i = 0; i < 3; i++ )
        {
          error = hkl_arr[i] - Math.round(hkl_arr[i]);
          fit_error[0] += error * error;
        }
        indexed_qs.add( q_vectors.elementAt(q_num) );

        Vector3D miller_ind = new Vector3D( Math.round(hkl_vec.getX()),
                                            Math.round(hkl_vec.getY()),
                                            Math.round(hkl_vec.getZ()) );
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
public static Vector MakeHemisphereDirections( int n_steps )
{
  if ( n_steps <= 0 )
  {
    throw new IllegalArgumentException("n_steps must be greater than 0 " +
                                        n_steps );
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
public static Vector MakeCircleDirections( int      n_steps,
                                           Vector3D axis,
                                           float    angle_degrees )
{
  if ( n_steps <= 0 )
  {
    throw new IllegalArgumentException("n_steps must be greater than 0 " +
                                       "in make circle directions " + n_steps );
  }

  axis = new Vector3D( axis );  // make copy of axis, so we don't change it!

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
  rotation.setRotation( angle_step, axis ); 
  Vector directions = new Vector( n_steps );
  for ( int i = 0; i < n_steps; i++ )
  {
    Vector3D vec = new Vector3D( vector_at_angle );
    directions.add( vec );
    rotation.apply_to( vector_at_angle, vector_at_angle );
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
public static int SelectDirection( Vector3D best_direction,
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
      direction = new Vector3D( direction );    // get a copy so we don't
                                                // mess up the original list
      direction.multiply( 1/plane_spacing );
      for ( int q_num = 0; q_num < q_vectors.size(); q_num++ )
      {
        dot_product = direction.dot( (Vector3D)(q_vectors.elementAt( q_num )));
        nearest_int = Math.round( dot_product );
        error = Math.abs( dot_product - nearest_int );
        sum_sq_error += error * error;
      }

      if ( sum_sq_error < min_sum_sq_error + 1.0e-50 )
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


public static float SelectDirections( Vector3D a_dir,
                                      Vector3D b_dir,
                                      Vector3D c_dir,
                                      float a,
                                      float b,
                                      float c,
                                      float alpha,
                                      float beta,
                                      float gamma,
                                      Vector   q_vectors,
                                      float    degrees_per_step )
{
  long start_time = System.nanoTime();

  int num_a_steps = (int)Math.round( 90.0 / degrees_per_step );
  double gamma_radians = gamma * Math.PI / 180.0;

  int num_b_steps = (int)Math.round(4*Math.sin( gamma_radians ) * num_a_steps);

  System.out.println("a, b, c = " + a + ", " + b + ", " + c );
  System.out.println("alpha, beta, gamma = " + 
                       alpha + ", " + beta + ", " + gamma );
  System.out.println("num_a_steps = " + num_a_steps );
  System.out.println("num_b_steps = " + num_b_steps );

  Vector a_dir_list = MakeHemisphereDirections( num_a_steps );

  Vector b_dir_list;

  Vector3D a_dir_temp;
  Vector3D b_dir_temp;
  Vector3D c_dir_temp;

  float min_error = 1e20f;
  float error;
  float dot_prod;
  int   nearest_int;
  Vector3D q_vec = new Vector3D();

  for ( int a_dir_num = 0; a_dir_num < a_dir_list.size(); a_dir_num++ )
  {
    a_dir_temp = (Vector3D)a_dir_list.elementAt( a_dir_num );
    a_dir_temp = new Vector3D( a_dir_temp );
    a_dir_temp.multiply( a );

    b_dir_list = MakeCircleDirections( num_b_steps, a_dir_temp, gamma );

    for ( int b_dir_num = 0; b_dir_num < b_dir_list.size(); b_dir_num++ )
    {
      float sum_sq_error = 0;
      b_dir_temp = (Vector3D)( b_dir_list.elementAt(b_dir_num) );
      b_dir_temp = new Vector3D( b_dir_temp );
      b_dir_temp.multiply( b );
      c_dir_temp = Make_c_dir( a_dir_temp, b_dir_temp, 
                               c, alpha, beta, gamma );

      for ( int q_num = 0; q_num < q_vectors.size(); q_num++ )
      {
        q_vec = (Vector3D)(q_vectors.elementAt( q_num ));
        dot_prod = a_dir_temp.dot( q_vec );
        nearest_int = Math.round( dot_prod );
        error = dot_prod - nearest_int;
        sum_sq_error += error * error;

        dot_prod = b_dir_temp.dot( q_vec );
        nearest_int = Math.round( dot_prod );
        error = dot_prod - nearest_int;
        sum_sq_error += error * error;

        dot_prod = c_dir_temp.dot( q_vec );
        nearest_int = Math.round( dot_prod );
        error = dot_prod - nearest_int;
        sum_sq_error += error * error;
      }

      if ( sum_sq_error < min_error )
      {
        min_error = sum_sq_error;
        a_dir.set( a_dir_temp );
        b_dir.set( b_dir_temp );
        c_dir.set( c_dir_temp );
      }
    }
  }

  long end_time = System.nanoTime();
  System.out.println("ELAPSED TIME = " + (end_time-start_time)/1.0e9 );
  return min_error;
}


/**
 *  The method calls the version of ScanFor_UB that takes three lattice
 *  direction vectors, a_dir, b_dir and c_dir and a required tolerance.
 *  This method uses the three direction vectors to determine the UB 
 *  matrix.  It should be most useful if number of peaks is on the order 
 *  of 10-20, and most of the peaks belong to the same crystallite.
 */
public static float ScanFor_UB( Tran3D   UB,
                                float    a,     float b,    float c,
                                float    alpha, float beta, float gamma,
                                Vector   q_vectors,
                                float    degrees_per_step,
                                float    required_tolerance )
{
  Vector3D a_dir = new Vector3D();
  Vector3D b_dir = new Vector3D();
  Vector3D c_dir = new Vector3D();

  float error = ScanFor_UB( a_dir, b_dir, c_dir,
                            a, b, c, alpha, beta, gamma,
                            q_vectors,
                            degrees_per_step,
                            required_tolerance );

  float[][] UB_inv_arr = { { a_dir.getX(), a_dir.getY(), a_dir.getZ(), 0 },
                           { b_dir.getX(), b_dir.getY(), b_dir.getZ(), 0 },
                           { c_dir.getX(), c_dir.getY(), c_dir.getZ(), 0 },
                           { 0,            0,            0,            1 } };
  UB.set( UB_inv_arr );
  UB.invert();

  return error;
}



/**
 *  The method very simply scans across all possible directions and 
 *  orientations for the  directions of the a, b and c vectors that will 
 *  minimize the sum-squared deviations from integer values of the 
 *  projections of the peaks on the a, b and c directions.  This method 
 *  will always return precisely one set of a, b and c vectors that minimize
 *  the sum-squared error.  If several directions and orientations produce 
 *  the same  minimum, this will return the first one that was encountered 
 *  during the search through directions.  NOTE: This is an expensive 
 *  calculation if the resolution of the vectors searched is less than 
 *  around 2 degrees per step tested.  This method should be most useful 
 *  if there are a small number of peaks, roughly 2-10 AND all peaks belong 
 *  to the same crystallite.
 */
public static float ScanFor_UB( Vector3D a_dir,
                                Vector3D b_dir,
                                Vector3D c_dir,
                                float    a,
                                float    b,
                                float    c,
                                float    alpha,
                                float    beta,
                                float    gamma,
                                Vector   q_vectors,
                                float    degrees_per_step )
{
  long start_time = System.nanoTime();

  int num_a_steps = (int)Math.round( 90.0 / degrees_per_step );
  double gamma_radians = gamma * Math.PI / 180.0;

  int num_b_steps = (int)Math.round(4*Math.sin( gamma_radians ) * num_a_steps);

  System.out.println("a, b, c = " + a + ", " + b + ", " + c );
  System.out.println("alpha, beta, gamma = " +
                       alpha + ", " + beta + ", " + gamma );
  System.out.println("num_a_steps = " + num_a_steps );
  System.out.println("num_b_steps = " + num_b_steps );

  Vector a_dir_list = MakeHemisphereDirections( num_a_steps );

  Vector b_dir_list;

  Vector3D a_dir_temp;
  Vector3D b_dir_temp;
  Vector3D c_dir_temp;

  float min_error = 1e20f;
  float error;
  float dot_prod;
  int   nearest_int;
  Vector3D q_vec = new Vector3D();

  for ( int a_dir_num = 0; a_dir_num < a_dir_list.size(); a_dir_num++ )
  {
    a_dir_temp = (Vector3D)a_dir_list.elementAt( a_dir_num );
    a_dir_temp = new Vector3D( a_dir_temp );
    a_dir_temp.multiply( a );

    b_dir_list = MakeCircleDirections( num_b_steps, a_dir_temp, gamma );

    for ( int b_dir_num = 0; b_dir_num < b_dir_list.size(); b_dir_num++ )
    {
      float sum_sq_error = 0;
      b_dir_temp = (Vector3D)( b_dir_list.elementAt(b_dir_num) );
      b_dir_temp = new Vector3D( b_dir_temp );
      b_dir_temp.multiply( b );
      c_dir_temp = Make_c_dir( a_dir_temp, b_dir_temp,
                               c, alpha, beta, gamma );

      for ( int q_num = 0; q_num < q_vectors.size(); q_num++ )
      {
        q_vec = (Vector3D)(q_vectors.elementAt( q_num ));
        dot_prod = a_dir_temp.dot( q_vec );
        nearest_int = Math.round( dot_prod );
        error = dot_prod - nearest_int;
        sum_sq_error += error * error;

        dot_prod = b_dir_temp.dot( q_vec );
        nearest_int = Math.round( dot_prod );
        error = dot_prod - nearest_int;
        sum_sq_error += error * error;

        dot_prod = c_dir_temp.dot( q_vec );
        nearest_int = Math.round( dot_prod );
        error = dot_prod - nearest_int;
        sum_sq_error += error * error;
      }

      if ( sum_sq_error < min_error )
      {
        min_error = sum_sq_error;
        a_dir.set( a_dir_temp );
        b_dir.set( b_dir_temp );
        c_dir.set( c_dir_temp );
      }
    }
  }
  long end_time = System.nanoTime();
  System.out.println("ELAPSED TIME = " + (end_time-start_time)/1.0e9 );
  return min_error;
}


/**
 *  The method uses two passes to scan across all possible directions and 
 *  orientations for the direction and orientation that best fits the
 *  specified list of peaks.  On the first pass, those only those sets of 
 *  directions that index the most peaks are kept.  On the second pass, 
 *  the directions that minimize the sum-squared deviations from integer 
 *  indices are selected from that smaller set of directions.  This method
 *  should be most useful if number of peaks is on the order of 10-20, 
 *  and most of the peaks belong to the same crystallite.
 */
public static float ScanFor_UB( Vector3D a_dir,
                                Vector3D b_dir,
                                Vector3D c_dir,
                                float    a,
                                float    b,
                                float    c,
                                float    alpha,
                                float    beta,
                                float    gamma,
                                Vector   q_vectors,
                                float    degrees_per_step,
                                float    required_tolerance )
{
  long start_time = System.nanoTime();

  int num_a_steps = (int)Math.round( 90.0 / degrees_per_step );
  double gamma_radians = gamma * Math.PI / 180.0;

  int num_b_steps = (int)Math.round(4*Math.sin( gamma_radians ) * num_a_steps);

  System.out.println("a, b, c = " + a + ", " + b + ", " + c );
  System.out.println("alpha, beta, gamma = " +
                       alpha + ", " + beta + ", " + gamma );
  System.out.println("num_a_steps = " + num_a_steps );
  System.out.println("num_b_steps = " + num_b_steps );

  Vector a_dir_list = MakeHemisphereDirections( num_a_steps );

  Vector b_dir_list;

  Vector3D a_dir_temp;
  Vector3D b_dir_temp;
  Vector3D c_dir_temp;

  float error;
  float dot_prod;
  int   nearest_int;
  int   max_indexed = 0;
  Vector3D q_vec = new Vector3D();
                                              // first select those directions
                                              // that index the most peaks
  Vector<Vector3D> selected_a_dirs = new Vector<Vector3D>();
  Vector<Vector3D> selected_b_dirs = new Vector<Vector3D>();
  Vector<Vector3D> selected_c_dirs = new Vector<Vector3D>();
                                                             
  for ( int a_dir_num = 0; a_dir_num < a_dir_list.size(); a_dir_num++ )
  {
    a_dir_temp = (Vector3D)a_dir_list.elementAt( a_dir_num );
    a_dir_temp = new Vector3D( a_dir_temp );
    a_dir_temp.multiply( a );

    b_dir_list = MakeCircleDirections( num_b_steps, a_dir_temp, gamma );

    for ( int b_dir_num = 0; b_dir_num < b_dir_list.size(); b_dir_num++ )
    {
      b_dir_temp = (Vector3D)( b_dir_list.elementAt(b_dir_num) );
      b_dir_temp = new Vector3D( b_dir_temp );
      b_dir_temp.multiply( b );
      c_dir_temp = Make_c_dir( a_dir_temp, b_dir_temp,
                               c, alpha, beta, gamma );

      int num_indexed = 0;
      for ( int q_num = 0; q_num < q_vectors.size(); q_num++ )
      {
        boolean indexes_peak = true;
        q_vec = (Vector3D)(q_vectors.elementAt( q_num ));
        dot_prod = a_dir_temp.dot( q_vec );
        nearest_int = Math.round( dot_prod );
        error = Math.abs( dot_prod - nearest_int );
        if ( error > required_tolerance )
          indexes_peak = false;
        else
        {
          dot_prod = b_dir_temp.dot( q_vec );
          nearest_int = Math.round( dot_prod );
          error = Math.abs( dot_prod - nearest_int );
          if ( error > required_tolerance )
            indexes_peak = false;
          else
          {
            dot_prod = c_dir_temp.dot( q_vec );
            nearest_int = Math.round( dot_prod );
            error = Math.abs( dot_prod - nearest_int );
            if ( error > required_tolerance )
              indexes_peak = false;
          }
        }
        if ( indexes_peak )
          num_indexed++;
      }

      if ( num_indexed > max_indexed )     // only keep those directions that
      {                                    // index the max number of peaks
        selected_a_dirs.clear();
        selected_b_dirs.clear();
        selected_c_dirs.clear();
        max_indexed = num_indexed;
      }
      if ( num_indexed == max_indexed )
      {
        selected_a_dirs.add( a_dir_temp );
        selected_b_dirs.add( b_dir_temp );
        selected_c_dirs.add( c_dir_temp );
      }
    }
  }

  float min_error = 1e20f;
  for ( int dir_num = 0; dir_num < selected_a_dirs.size(); dir_num++ )
  {
    a_dir_temp = (Vector3D)selected_a_dirs.elementAt( dir_num );
    b_dir_temp = (Vector3D)selected_b_dirs.elementAt( dir_num );
    c_dir_temp = (Vector3D)selected_c_dirs.elementAt( dir_num );

      float sum_sq_error = 0;
      for ( int q_num = 0; q_num < q_vectors.size(); q_num++ )
      {
        q_vec = (Vector3D)(q_vectors.elementAt( q_num ));
        dot_prod = a_dir_temp.dot( q_vec );
        nearest_int = Math.round( dot_prod );
        error = dot_prod - nearest_int;
        sum_sq_error += error * error;

        dot_prod = b_dir_temp.dot( q_vec );
        nearest_int = Math.round( dot_prod );
        error = dot_prod - nearest_int;
        sum_sq_error += error * error;

        dot_prod = c_dir_temp.dot( q_vec );
        nearest_int = Math.round( dot_prod );
        error = dot_prod - nearest_int;
        sum_sq_error += error * error;
      }

      if ( sum_sq_error < min_error )
      {
        min_error = sum_sq_error;
        a_dir.set( a_dir_temp );
        b_dir.set( b_dir_temp );
        c_dir.set( c_dir_temp );
      }
  }

  long end_time = System.nanoTime();
  System.out.println("ELAPSED TIME = " + (end_time-start_time)/1.0e9 );
  return min_error;
}


/**
 *  Calculate the vector in the direction of "c" given two vectors a_dir
 *  and b_dir in the directions of "a" and "b", with lengths a and b.
 *  The length "c" must be specified, along with the unit cell angles,
 *  alpha, beta, gamma.
 */
private static Vector3D Make_c_dir( Vector3D a_dir, Vector3D b_dir,
                                    float c,
                                    float alpha, float beta, float gamma )
{
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

  Vector3D basis_1 = new Vector3D(a_dir);
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

  return c_dir;
}


/**
 */
private static float GetPossibleDirectonLists( Vector<Vector3D> q_vectors,
                                               Vector<Vector3D> a_dir_list,
                                               Vector<Vector3D> b_dir_list,
                                               Vector<Vector3D> c_dir_list,
                                               float  a, 
                                               float  b,
                                               float  c,
                                               float  edge_tolerance,
                                               float  required_tolerance,
                                               float  required_fraction,
                                               float  angle_tolerance,
                                               float  degrees_per_step )
{
  System.out.println("===================================================");
  System.out.println("required_tolerance = " + required_tolerance );
  System.out.println("required_fraction  = " + required_fraction );
  System.out.println("angle_tolerance    = " + angle_tolerance );
  System.out.println("degrees_per_step   = " + degrees_per_step );
  long start = System.nanoTime();

  int n_steps = Math.round( 90 / degrees_per_step );
  Vector sphere_dirs = MakeHemisphereDirections( n_steps );
  float error = 0;

  System.out.println("Number of directions  = " + sphere_dirs.size() );

  error = GetPossibleDirectionList( q_vectors, sphere_dirs, a_dir_list, 
                                    required_tolerance, required_fraction,
                                    angle_tolerance,
                                    a, edge_tolerance );
  error += GetPossibleDirectionList( q_vectors, sphere_dirs, b_dir_list, 
                                     required_tolerance, required_fraction,
                                     angle_tolerance,
                                     b, edge_tolerance );
  error += GetPossibleDirectionList( q_vectors, sphere_dirs, c_dir_list, 
                                     required_tolerance, required_fraction,
                                     angle_tolerance,
                                     c, edge_tolerance );

  System.out.println("total error   = " + error );
  long end = System.nanoTime();
  System.out.println("Total time = " + (end-start)/1.0e9f ); 
  return error;
}


/**
 * @param required_tolerance  Max error in Miller index allowed for a peak
 *                            to count as indexed.
 * @param required_fraction   Fraction of all listed peak q_vectors that must
 *                            be indexed in a direction, for that direction to
 *                            be examined as a possible plane normal direction.
 */
private static float GetPossibleDirectionList( Vector<Vector3D> q_vectors,
                                               Vector<Vector3D> sphere_dirs,
                                               Vector<Vector3D> edge_list,
                                               float  required_tolerance,
                                               float  required_fraction,
                                               float  angle_tolerance,
                                               float  length,
                                               float  length_tolerance )
{
  int required_number = (int)( required_fraction * q_vectors.size() );
  int num_indexed;
  int max_indexed = 0;


  Vector3D dir_vec = new Vector3D( );
  Vector<Vector3D> index_vals = new Vector<Vector3D>();
  Vector<Vector3D> indexed_qs = new Vector<Vector3D>();
  float[] fit_error = new float[1];
  float   min_error = 1e20f;

  Vector<Vector3D> temp_list = new Vector<Vector3D>();

  for ( int i = 0; i < sphere_dirs.size(); i++ )
  {
    dir_vec.set( sphere_dirs.elementAt(i) );
    dir_vec.multiply( length );

    num_indexed = GetIndexedPeaks_1D( q_vectors,
                                      dir_vec,
                                      required_tolerance,
                                      index_vals,
                                      indexed_qs,
                                      fit_error  );
    if ( num_indexed >= required_number )
    {
      fit_error[0] = BestFit_Direction( dir_vec, index_vals, indexed_qs );

      num_indexed = GetIndexedPeaks_1D( q_vectors,
                                        dir_vec,
                                        required_tolerance,
                                        index_vals,
                                        indexed_qs,
                                        fit_error  );

      fit_error[0] = BestFit_Direction( dir_vec, index_vals, indexed_qs );

      float vec_length = dir_vec.length();
      if ( Math.abs( length - vec_length ) < length_tolerance )
      {
        int j = 0;
        boolean new_dir = true;
        while ( new_dir && j < temp_list.size() )
        {
          if ( vec_length == 0 )
            new_dir = false;
          else
          {
            float angle = angle( dir_vec, temp_list.elementAt(j) );
            if ( angle < angle_tolerance/4 || angle > 180 - angle_tolerance/4 )
              new_dir = false; 
          }
          j++;
        }
        if ( new_dir )
        {
          temp_list.add( new Vector3D(dir_vec) );

          if ( num_indexed > max_indexed )
            max_indexed = num_indexed;

          if ( fit_error[0] < min_error )
            min_error = fit_error[0];
        }
      }
    }
  }

  System.out.println("ON FIRST PASS, NUMBER OF VECTORS = " + temp_list.size());
  System.out.println("Max_indexed = " + max_indexed );
  System.out.println("min_error = " + min_error );
  System.out.println("length = " + length );
  System.out.println("length tolerance = " + length_tolerance );
  for ( int i = 0; i < temp_list.size(); i++ )
    System.out.println("i = " + i + " vector = " + temp_list.elementAt(i) +
                       " LENGTH = " + temp_list.elementAt(i).length());

  for ( int i = 0; i < temp_list.size(); i++ )
    for ( int j = i+1; j < temp_list.size(); j++ )
    {
      Vector3D v1 = temp_list.elementAt( i );
      Vector3D v2 = temp_list.elementAt( j );
      System.out.println("v1 = " + i +
                         " v2 = " + j +  
                         " angle = " + angle(v1,v2) );
    }


  for ( int i = 0; i < temp_list.size(); i++ )
  {
    dir_vec.set( temp_list.elementAt(i) );

    num_indexed = GetIndexedPeaks_1D( q_vectors,
                                      dir_vec,
                                      required_tolerance,
                                      index_vals,
                                      indexed_qs,
                                      fit_error  );
    if ( num_indexed >= 0.7 * max_indexed && fit_error[0] < 5 * min_error )
    {
      fit_error[0] = BestFit_Direction( dir_vec, index_vals, indexed_qs );

      num_indexed = GetIndexedPeaks_1D( q_vectors,
                                        dir_vec,
                                        required_tolerance,
                                        index_vals,
                                        indexed_qs,
                                        fit_error  );

      fit_error[0] = BestFit_Direction( dir_vec, index_vals, indexed_qs );
      boolean new_dir = true;
      int j = 0;
      while ( new_dir && j < edge_list.size() )
      {
        if ( angle( dir_vec, edge_list.elementAt(j) ) < angle_tolerance )
          new_dir = false;
        j++;
      }
      if ( new_dir )
        edge_list.add( new Vector3D( dir_vec ) );
    }
  }

  System.out.println("ON SECOND PASS, NUMBER OF VECTORS = " + edge_list.size());
  System.out.println("Max_indexed = " + max_indexed );
  System.out.println("min_error = " + min_error );
  for ( int i = 0; i < edge_list.size(); i++ )
    System.out.println("i = " + i + " vector = " + edge_list.elementAt(i) +
                       " LENGTH = " + edge_list.elementAt(i).length());

  for ( int i = 0; i < edge_list.size(); i++ )
    for ( int j = i+1; j < edge_list.size(); j++ )
    {
      Vector3D v1 = edge_list.elementAt( i );
      Vector3D v2 = edge_list.elementAt( j );
      System.out.println("v1 = " + i +
                         " v2 = " + j + 
                         " angle = " + angle(v1,v2) );
    }

  return min_error;
}


public static void LoadHKL_Q( String           file_name, 
                              Vector<Vector3D> hkls, 
                              Vector<Vector3D> q_vectors ) throws Exception
{
  hkls.clear();
  q_vectors.clear();

  Scanner sc = new Scanner( new File(file_name) );
  int num_peaks = sc.nextInt();
  for ( int i = 0; i < num_peaks; i++ )
  {
    int h = sc.nextInt();
    int k = sc.nextInt();
    int l = sc.nextInt();
    float qx = sc.nextFloat();
    float qy = sc.nextFloat();
    float qz = sc.nextFloat();
    if ( h != 0 || k != 0 || l != 0 )
    {
      hkls.add( new Vector3D( h, k, l ) );
      q_vectors.add( new Vector3D( qx, qy, qz ) );
    }
  }
}


private static void test_ScanForUB( Vector<Vector3D> q_vectors,
                                    float            degrees_per_step )
{
  Vector3D a_dir = new Vector3D();
  Vector3D b_dir = new Vector3D();
  Vector3D c_dir = new Vector3D();

    float[] lat_par = { 3.85f, 3.85f, 3.85f, 90, 90, 90 };
//  float[] lat_par = { 5.45f, 5.45f, 5.45f, 90, 90, 90 };
//  float[] lat_par = { 4.91f, 4.91f, 5.40f, 90, 90, 120 };
//  float[] lat_par = { 73.3f, 73.3f, 99.6f, 90, 90, 120 };
//  float[] lat_par = { 72f, 72f, 100f, 90, 90, 120 };
//  float[] lat_par = { 73.3f, 127.1f, 99.6f, 90, 90, 90 };
//  float[] lat_par = { 73.3f, 127.1f, 99.6f, 90, 90, 90 };

  float a = lat_par[0];
  float b = lat_par[1];
  float c = lat_par[2];
  float alpha = lat_par[3];
  float beta  = lat_par[4];
  float gamma = lat_par[5];

  float fit_error = ScanFor_UB( a_dir, b_dir, c_dir, 
                                a, b, c, alpha, beta, gamma,
                                q_vectors, degrees_per_step );

  System.out.println("Resulting error " + fit_error );
  System.out.println("a_dir = " + a_dir );
  System.out.println("b_dir = " + b_dir );
  System.out.println("c_dir = " + c_dir );
  System.out.println("a = " + a_dir.length() );
  System.out.println("b = " + b_dir.length() );
  System.out.println("c = " + c_dir.length() );
  System.out.println("alpha = " + angle( b_dir, c_dir ) );
  System.out.println("beta  = " + angle( c_dir, a_dir ) );
  System.out.println("gamma = " + angle( a_dir, b_dir ) );
  for ( int i = 0; i < q_vectors.size(); i++ )
  {
    System.out.print("h, k, l = " + a_dir.dot( q_vectors.elementAt(i)));
    System.out.print(", " + b_dir.dot( q_vectors.elementAt(i)));
    System.out.println(", " + c_dir.dot( q_vectors.elementAt(i)));
  }
}

public static void main( String args[] ) throws Exception
{
  Vector<Vector3D> hkls      = new Vector<Vector3D>();
  Vector<Vector3D> q_vectors = new Vector<Vector3D>();
  LoadHKL_Q( args[0], hkls, q_vectors );
/*
  for ( int i = 0; i < hkls.size(); i++ )
    System.out.println( hkls.elementAt(i) + "  " + q_vectors.elementAt(i) );
*/

  Vector3D dir_vec = new Vector3D();

  Vector<Integer> index_vals = new Vector<Integer>();

  float fit_error;

  try
  {

  for ( int dim = 0; dim < 3; dim++ )
  {
    for ( int i = 0; i < hkls.size(); i++ )
      index_vals.add( Math.round(hkls.elementAt(i).get()[dim]) );

    fit_error = BestFit_Direction( dir_vec, index_vals, q_vectors );

    System.out.println("Fit error = " + fit_error );
    System.out.println("Sigma = " + Math.sqrt(fit_error) / hkls.size() );
    System.out.println("Direction = " + dir_vec );

    float length = dir_vec.length();
    System.out.println("Length    = " + length );

    index_vals.clear();
  }


  Tran3D UB = new Tran3D();
  fit_error = BestFit_UB( UB, hkls, q_vectors );
  System.out.println("fit_error in UB = " + fit_error ); 
//  System.out.println("UB = \n" + UB ); 
  UB.invert();
  System.out.println("UB inverse = \n" + UB ); 

  } 
  catch ( Exception ex )
  {
    System.out.println("EXCEPTION... NOT ENOUGH DIRECTIONS TO DO BEST FIT");
  }

/*
  float degrees_per_step = 0.5f;
  test_ScanForUB( q_vectors, degrees_per_step ); 
*/

  float degrees_per_step = 0.5f;
  int n_steps = Math.round( 90 / degrees_per_step );
  Vector sphere_dirs = MakeHemisphereDirections( n_steps );
  System.out.println("Checking " + sphere_dirs.size() + " Sphere Directions");
  Vector<Vector3D> edge_list = new Vector<Vector3D>();
  float required_tolerance = 0.12f;
  float required_fraction = 0.80f;
  float angle_tolerance = 0.5f;
  float edge_length = Float.parseFloat( args[1] );
  float edge_tolerance = 0.2f;
  fit_error =  GetPossibleDirectionList( q_vectors,
                                         sphere_dirs,
                                         edge_list,
                                         required_tolerance,
                                         required_fraction,
                                         angle_tolerance,
                                         edge_length,
                                         edge_tolerance );

  for ( int i = 0; i < edge_list.size(); i++ )
    System.out.println( edge_list.elementAt(i) );
}

} // end of class

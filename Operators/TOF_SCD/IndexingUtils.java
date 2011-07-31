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

import DataSetTools.components.ui.Peaks.subs;
import DataSetTools.operator.Generic.TOF_SCD.IPeak;


public class IndexingUtils
{

public static float angle( Vector3D v1, Vector3D v2 )
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
    STATIC method Find_UB_1: First attempt at calculating the matrix that 
    most nearly indexes the specified q_vectors, given the lattice parameters.  
    The sum of the squares of the residual errors is returned.
  
    @param  UB                  3x3 matrix that will be set to the UB matrix
    @param  q_vectors           Vector of new Vector3D objects that contains 
                                the list of q_vectors that are to be indexed
                                NOTE: There must be at least 3 q_vectors.
    @param  a                   First unit cell edge length in Angstroms.  
    @param  b                   Second unit cell edge length in Angstroms.  
    @param  c                   Third unit cell edge length in Angstroms.  
    @param  alpha               First unit cell angle in degrees.
    @param  beta                second unit cell angle in degrees.
    @param  gamma               third unit cell angle in degrees.
    @param  required_tolerance  The maximum allowed deviation of Miller indices
                                from integer values for a peak to be indexed.

    @return  This will return the sum of the squares of the residual errors.
  
    @throws  IllegalArgumentException if there are not at least 3 q vectors.
   
    @throws  std::runtime_error    exception if the UB matrix can't be found.
                                   This will happen if the q_vectors do not
                                   determine all three directions of the unit
                                   cell, or if they cannot be indexed within
                                   the required tolerance.
*/
public static float Find_UB_1( Tran3D   UB,
                                Vector   q_vectors,
                                float a, float b, float c,
                                float alpha, float beta, float gamma,
                                float    required_tolerance )
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

//  System.out.println("\nSelected a_dir = " + a_dir + 
//                     " magnitude = "+a_dir.length());
  for ( int i = 0; i < 5; i++ )
  {
  num_indexed = GetIndexedPeaks_1D( a_dir,
                                    q_vectors,
                                    required_tolerance,
                                    index_vals,
                                    indexed_qs,
                                    fit_error  );

                            // Use the 1D indices and qs to optimize the 
                            // plane normal, a_dir.
  fit_error[0] = Optimize_Direction_3D( a_dir, index_vals, indexed_qs );
/*  System.out.println("Best a_dir = " + a_dir + 
                     " magnitude = " + a_dir.length() +
                     " num indexed = " + num_indexed  +
                     " fit_error = " + fit_error[0] );
*/
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
/*
  System.out.println("\nSelected b_dir = " + b_dir + 
                     " magnitude = "+b_dir.length());
  System.out.println("Gamma (selected) = " + angle(a_dir,b_dir) );
*/
  for ( int i = 0; i < 5; i++ )
  {
  num_indexed = GetIndexedPeaks_1D( b_dir,
                                    q_vectors,
                                    required_tolerance,
                                    index_vals,
                                    indexed_qs,
                                    fit_error  );

  fit_error[0] = Optimize_Direction_3D( b_dir, index_vals, indexed_qs );
/*
  System.out.println("Best b_dir = " + b_dir + 
                     " magnitude = " + b_dir.length() +
                     " num indexed = " + num_indexed  +
                     " fit_error = " + fit_error[0] );
*/
  }

//  System.out.println("Gamma (fitted) = " + angle(a_dir,b_dir) );

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
/*
  System.out.println("Alpha (selected) = " + angle(c_dir,b_dir) );
  System.out.println("Beta  (selected) = " + angle(c_dir,a_dir) );
                            // Optimize the c_dir vector as before

  System.out.println("\nSelected c_dir = " + c_dir + 
                     " magnitude = "+c_dir.length());
*/
  for ( int i = 0; i < 7; i++ )
  {
  num_indexed = GetIndexedPeaks_1D( c_dir,
                                    q_vectors,
                                    required_tolerance,
                                    index_vals,
                                    indexed_qs,
                                    fit_error  );

  fit_error[0] = Optimize_Direction_3D( c_dir, index_vals, indexed_qs );
/*
  System.out.println("Best c_dir = " + c_dir + 
                     " magnitude = " + c_dir.length() +
                     " num indexed = " + num_indexed +
                     " fit_error = " + fit_error[0] );
*/
  }
/*
  System.out.println("Alpha (fitted  ) = " + angle(c_dir,b_dir) );
  System.out.println("Beta  (fitted  ) = " + angle(c_dir,a_dir) );

  System.out.println();
*/
                            // Now, using the plane normals for all three
                            // families of planes, get a consistent indexing
                            // discarding any peaks that are not indexed in 
                            // all three directions.
  Vector miller_ind = new Vector();
  num_indexed = GetIndexedPeaks_3D( a_dir, b_dir, c_dir,
                                    q_vectors,
                                    required_tolerance,
                                    miller_ind,
                                    indexed_qs,
                                    fit_error );

                            // Finally, use the indexed peaks to get an 
                            // optimized UB that matches the indexing
                           
  fit_error[0] = Optimize_UB_3D( UB, miller_ind, indexed_qs );
/*
  System.out.println("Final Version Fit Error = " + fit_error[0] +
                     " Number indexed = " + num_indexed );
  System.out.println("Alpha (final) = " + angle(c_dir,b_dir) );
  System.out.println("Beta  (final) = " + angle(c_dir,a_dir) );
  System.out.println("Gamma (final) = " + angle(a_dir,b_dir) );
*/
  return fit_error[0];
}


/** 
    STATIC method FInd_UB_2: Second attempt at calculating the matrix that
    most nearly indexes the specified q_vectors, given the lattice parameters.  
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
public static float Find_UB_2( Tran3D   UB,
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
/*
  System.out.println("Initial error = " + error );
  System.out.println("Alpha (selected) = " + angle(c_dir,b_dir) );
  System.out.println("Beta  (selected) = " + angle(c_dir,a_dir) );
  System.out.println("Gamma (selected) = " + angle(a_dir,b_dir) );
*/
  Vector miller_ind = new Vector();
  Vector indexed_qs = new Vector();
  float[] fit_error = new float[1];
  int num_indexed = GetIndexedPeaks_3D( a_dir, b_dir, c_dir,
                                        q_vectors,
                                        required_tolerance,
                                        miller_ind,
                                        indexed_qs,
                                        fit_error );
 

  fit_error[0] = Optimize_UB_3D( UB, miller_ind, indexed_qs );
/*
  System.out.println("Final Version Fit Error = " + fit_error[0] +
                     " Number indexed = " + num_indexed );
  System.out.println("Alpha (final) = " + angle(c_dir,b_dir) );
  System.out.println("Beta  (final) = " + angle(c_dir,a_dir) );
  System.out.println("Gamma (final) = " + angle(a_dir,b_dir) );
*/

  return fit_error[0];
}


/** 
    STATIC method Find_UB: Current attempt at calculating the matrix that 
    most nearly indexes the specified q_vectors, given the lattice parameters.  
    The sum of the squares of the residual errors is returned.
  
    @param  UB                  3x3 matrix that will be set to the UB matrix
    @param  q_vectors           Vector of new Vector3D objects that contains 
                                the list of q_vectors that are to be indexed
                                NOTE: There must be at least 2 linearly 
                                independent q_vectors.  If there are only 2
                                q_vectors, no least squares optimization of
                                the UB matrix will be done.
    @param  a                   First unit cell edge length in Angstroms.  
    @param  b                   Second unit cell edge length in Angstroms.  
    @param  c                   Third unit cell edge length in Angstroms.  
    @param  alpha               First unit cell angle in degrees.
    @param  beta                second unit cell angle in degrees.
    @param  gamma               third unit cell angle in degrees.
    @param  required_tolerance  The maximum allowed deviation of Miller indices
                                from integer values for a peak to be indexed.
    @param  base_index          The sequence number of the peak that should 
                                be used as the central peak.  On the first
                                scan for a UB matrix that fits the data,
                                the remaining peaks in the list of q_vectors 
                                will be shifted by -base_peak, where base_peak
                                is the q_vector with the specified base index.
                                If fewer than 4 peaks are specified in the
                                q_vectors list, this parameter is ignored.
                                If this parameter is -1, and there are at least
                                four peaks in the q_vector list, then a base
                                index will be calculated internally.  In most
                                cases, it should suffice to set this to -1.
    @param  num_initial         The number of low |Q| peaks that are used
                                when scanning for an initial indexing.
    @param  degrees_per_step    The number of degrees between directions that
                                are checked while scanning for an initial 
                                indexing of the peaks with lowest |Q|.

    @return  This will return the sum of the squares of the residual errors.
  
    @throws  IllegalArgumentException if there are not at least 2 q vectors.
   
    @throws  std::runtime_error    exception if the UB matrix can't be found.
*/
public static float Find_UB( Tran3D             UB,
                             Vector<Vector3D>   q_vectors,
                             float a,     float b,    float c,
                             float alpha, float beta, float gamma,
                             float              required_tolerance,
                             int                base_index,
                             int                num_initial,
                             float              degrees_per_step )
{
  if ( q_vectors.size() < 2 )
    throw new IllegalArgumentException("Need at least 2 q_vectors to find UB");

  Vector<Vector3D> original_qs = new Vector<Vector3D>(q_vectors.size());
  for ( int i = 0; i < q_vectors.size(); i++ )
    original_qs.add( new Vector3D( q_vectors.elementAt(i) ) );

  if ( q_vectors.size() > 5 )       // shift to be centered on peak (we lose
                                    // one peak that way.
  {
    Vector<Vector3D> shifted_qs = new Vector<Vector3D>();
    int mid_ind = q_vectors.size()/3;
                                    // either do an initial sort and use
                                    // default base peak, or use the index
                                    // specified by the input parameter
    if ( base_index < 0 || base_index > q_vectors.size() )  
      q_vectors = SortOnVectorMagnitude( q_vectors ); 
    else  
      mid_ind = base_index; 

    Vector3D mid_vec = q_vectors.elementAt( mid_ind );
    for ( int i = 0; i < q_vectors.size(); i++ )
    {
      if ( i != mid_ind )
      {
        Vector3D shifted_vec = new Vector3D( q_vectors.elementAt(i) );
        shifted_vec.subtract( mid_vec );
        shifted_qs.add( shifted_vec );
      }
    }
    q_vectors = shifted_qs; 
  }
                                    // now order the possibly modified
                                    // q_vectors to be increasing in magnitude
  q_vectors = SortOnVectorMagnitude( q_vectors );

  if ( num_initial > q_vectors.size() )
    num_initial = q_vectors.size();
 
  Vector<Vector3D> some_qs = new Vector<Vector3D>();
  for ( int i = 0; i < num_initial; i++ ) 
    some_qs.add( q_vectors.elementAt(i) );

  ScanFor_UB( UB, some_qs,
              a, b, c, alpha, beta, gamma,
              degrees_per_step,
              required_tolerance );

  float[] fit_error = new float[1];
  int     num_indexed;
  Vector miller_ind = new Vector();
  Vector indexed_qs = new Vector();

                                     // now gradually bring in the remaining
                                     // peaks and re-optimize the UB to index
                                     // them as well
  int     count = 0;
  while ( num_initial < q_vectors.size() )
  {
    count++;
    num_initial = Math.round(1.5f * num_initial + 3);
                                             // add 3, in case we started with
                                             // a very small number of peaks!
    if ( num_initial >= q_vectors.size() ) 
      num_initial = q_vectors.size();

    for ( int i = some_qs.size(); i < num_initial; i++ )
      some_qs.add( q_vectors.elementAt(i) );

    Tran3D temp_UB = new Tran3D( UB );
    num_indexed = GetIndexedPeaks( UB, some_qs, required_tolerance,
                                   miller_ind, indexed_qs, fit_error );

//    fit_error[0] = Optimize_UB_3D( temp_UB, miller_ind, indexed_qs );
    fit_error[0] = Optimize_UB_4D( temp_UB, miller_ind, indexed_qs );

    if ( !Float.isNaN( fit_error[0] ) )
      UB.set( temp_UB );
  }

//  System.out.println("Finished growing set of peaks...");

  if ( original_qs.size() >= 5 )    // try one last refinement using all peaks
  {
    Tran3D temp_UB = new Tran3D( UB );
    num_indexed = GetIndexedPeaks( UB, original_qs, required_tolerance,
                                   miller_ind, indexed_qs, fit_error );
//    fit_error[0] = Optimize_UB_3D( temp_UB, miller_ind, indexed_qs );
    fit_error[0] = Optimize_UB_4D( temp_UB, miller_ind, indexed_qs );
    if ( !Float.isNaN( fit_error[0] ) )
      UB.set( temp_UB );
  }
//  System.out.println("Finished finished refining all peaks ...");

                                 // Regardless of how we got the UB, find the
                                 // sum-squared errors for the indexing in 
                                 // HKL space.
  num_indexed = GetIndexedPeaks( UB, q_vectors, required_tolerance,
                                 miller_ind, indexed_qs, fit_error ); 
/*
  System.out.println("Num indexed, shifted = " + num_indexed );
  int num_to_print = Math.min( 5, indexed_qs.size() );
  for ( int i = 0; i < num_to_print; i++ )
    System.out.println("SHIFTED q = " + indexed_qs.elementAt(i) +
                       "hkl = " + miller_ind.elementAt(i ) );
*/
                                 // now, get rid of shift and see how well 
                                 // it works.
  float[][] UB_array = UB.get();
  for ( int i = 0; i < 3; i++ )
  {
    UB_array[3][i] = 0;
    UB_array[i][3] = 0;
  }
  UB_array[3][3] = 1;
  UB.set( UB_array );
  num_indexed = GetIndexedPeaks( UB, original_qs, required_tolerance,
                                 miller_ind, indexed_qs, fit_error );
/*
  System.out.println("Num indexed, raw = " + num_indexed );
  num_to_print = Math.min( 5, indexed_qs.size() );
  for ( int i = 0; i < num_to_print; i++ )
    System.out.println("RAW q = " + indexed_qs.elementAt(i) +
                       "hkl = " + miller_ind.elementAt(i ) );
*/
  return fit_error[0];
}


/** 
    STATIC method Find_UB: This method will attempt to calculate the matrix 
    that most nearly indexes the specified q_vectors, given a range of 
    possible unit cell edge lengths.  

    The sum of the squares of the residual errors is returned.
  
    @param  UB                  3x3 matrix that will be set to the UB matrix
    @param  q_vectors           Vector of new Vector3D objects that contains 
                                the list of q_vectors that are to be indexed
                                NOTE: There must be at least 3 linearly 
                                independent q_vectors.  If there are only 3
                                q_vectors, no least squares optimization of
                                the UB matrix will be done.
    @param  min_d               Lower bound on shortest unit cell edge length.
                                This does not have to be specified exactly but
                                must be strictly less than the smallest edge
                                length, in Angstroms.
    @param  max_d               Upper bound on longest unit cell edge length.
                                This does not have to be specified exactly but
                                must be strictly more than the longest edge
                                length in angstroms.
    @param  required_tolerance  The maximum allowed deviation of Miller indices
                                from integer values for a peak to be indexed.
    @param  base_index          The sequence number of the peak that should 
                                be used as the central peak.  On the first
                                scan for directions representing plane normals
                                in reciprocal space, the peaks in the list of 
                                q_vectors will be shifted by -base_peak, where
                                base_peak is the q_vector with the specified
                                base index.
                                If fewer than 4 peaks are specified in the
                                q_vectors list, this parameter is ignored.
                                If this parameter is -1, and there are at least
                                four peaks in the q_vector list, then a base
                                index will be calculated internally.  In most
                                cases, it should suffice to set this to -1.
    @param  num_initial         The number of low |Q| peaks that are used
                                when scanning for an initial indexing.
    @param  degrees_per_step    The number of degrees between directions that
                                are checked while scanning for an initial 
                                indexing of the peaks with lowest |Q|.

    @return  This will return the sum of the squares of the residual errors.
  
    @throws  IllegalArgumentException if there are not at least 3 q vectors.
   
    @throws  std::runtime_error    exception if the UB matrix can't be found.
*/

public static float Find_UB( Tran3D             UB,
                             Vector<Vector3D>   q_vectors,
                             float              min_d,
                             float              max_d,
                             float              required_tolerance,
                             int                base_index,
                             int                num_initial,
                             float              degrees_per_step )
{
  if ( q_vectors.size() < 3 )
    throw new IllegalArgumentException("Need at least 3 q_vectors to find UB");

  Vector<Vector3D> original_qs = new Vector<Vector3D>(q_vectors.size());
  for ( int i = 0; i < q_vectors.size(); i++ )
    original_qs.add( new Vector3D( q_vectors.elementAt(i) ) );

                                    // First, sort the peaks in order of 
                                    // increasing |Q| so that we can try to
                                    // index the low |Q| peaks first.
  if ( q_vectors.size() > 5 )       // shift to be centered on peak (we lose
                                    // one peak that way.
  {
    Vector<Vector3D> shifted_qs = new Vector<Vector3D>();
    int mid_ind = q_vectors.size()/2;
                                    // either do an initial sort and use
                                    // default base peak, or use the index
                                    // specified by the input parameter
    if ( base_index < 0 || base_index > q_vectors.size() )
      q_vectors = SortOnVectorMagnitude( q_vectors );
    else 
      mid_ind = base_index;

    Vector3D mid_vec = q_vectors.elementAt( mid_ind );
    for ( int i = 0; i < q_vectors.size(); i++ )
    {
      if ( i != mid_ind )
      {
        Vector3D shifted_vec = new Vector3D( q_vectors.elementAt(i) );
        shifted_vec.subtract( mid_vec );
        shifted_qs.add( shifted_vec );
      }
    }
    q_vectors = shifted_qs;
  }
                                    // now order the possibly modified
                                    // q_vectors to be increasing in magnitude
  q_vectors = SortOnVectorMagnitude( q_vectors );

  if ( num_initial > q_vectors.size() )
    num_initial = q_vectors.size();

  Vector<Vector3D> some_qs = new Vector<Vector3D>();
  for ( int i = 0; i < num_initial; i++ )
    some_qs.add( q_vectors.elementAt(i) );

  Vector<Vector3D> directions = new Vector<Vector3D>();
  ScanFor_Directions( directions,
                      some_qs,
                      min_d, max_d,
                      degrees_per_step,
                      required_tolerance );

  directions = SortOnVectorMagnitude( directions );
/*
  for ( int i = 0; i < directions.size(); i++ )
  {
    System.out.printf("i = %2d  Length = %7.4f  dir = ",
                       i, directions.elementAt(i).length() );
    System.out.println( directions.elementAt(i) );
  }
*/

  Vector3D a_dir = directions.elementAt( 0 );
//  System.out.println("A_DIR = " + a_dir + "   length = " + a_dir.length() );


  float min_deg = (float)((180/Math.PI) * Math.atan(2*min_d/max_d) );
//  System.out.println("MIN DEG = " + min_deg );

  float epsilon = 5;                    //  tolerance on right angle (degrees)
  Vector3D b_dir  = null;
  boolean b_found = false;
  int index = 1;
  while ( !b_found && index < directions.size() ) 
  {
    Vector3D vec = directions.elementAt(index);
    float    gamma = angle( a_dir, vec );
    if ( gamma >= min_deg && (180 - gamma) >= min_deg )
    {
      b_dir = new Vector3D( vec );
      if ( gamma > 90 + epsilon )       // try for Nigli cell with angles <= 90
        b_dir.multiply( -1 );
      b_found = true;
      directions.remove( vec );
//    System.out.println("index = " + index );
//    System.out.println("B_DIR = " + b_dir + "   length = " + b_dir.length());
//    System.out.println("GAMMA = " + angle(a_dir, b_dir) );
    }

    else
      index++;
  }

  Vector3D c_dir  = null;
  boolean c_found = false;

  Vector3D perp = new Vector3D();
  perp.cross( a_dir, b_dir );
  perp.normalize();

  while ( !c_found && index < directions.size() )
  {
    Vector3D vec = directions.elementAt(index);
    float perp_ang = angle( perp, vec );
    if ( perp_ang < 80 ) 
    {
      c_dir = new Vector3D( vec );
      c_found = true;
    }
    else 
    {
      c_dir = new Vector3D( vec );
      c_dir.multiply(-1);
      perp_ang = angle( perp, c_dir );
      if ( perp_ang < 80 ) 
      {
        c_found = true;
      }
    }

    if ( ! c_found ) 
      index++;
  }


  if ( c_found )
  {
/*
     System.out.println("C_DIR = " + c_dir + "   length = " + c_dir.length());
     System.out.println("ALPHA = " + angle(b_dir, c_dir) );
     System.out.println("BETA  = " + angle(c_dir, a_dir) );
     System.out.println("GAMMA = " + angle(a_dir, b_dir) );
*/
  }
  else
  {
    System.out.println("ERROR NO C DIRECTION FOUND!!!!!!!!!!");
    System.out.println("DEFAULTING TO LAST C_DIR !!!!!!!!!");
    c_dir = directions.elementAt( directions.size()-1 );
  }

  System.out.println("MIN D = " + min_d );
  System.out.println("MAX D = " + max_d );
  System.out.println(" a = " + a_dir.length() );
  System.out.println(" b = " + b_dir.length() );
  System.out.println(" c = " + c_dir.length() );
  System.out.println(" alpha = " + angle( b_dir, c_dir ) );
  System.out.println(" beta  = " + angle( c_dir, a_dir ) );
  System.out.println(" gamma = " + angle( a_dir, b_dir ) );

  float[][] UB_inv_arr = { { a_dir.getX(), a_dir.getY(), a_dir.getZ(), 0 },
                           { b_dir.getX(), b_dir.getY(), b_dir.getZ(), 0 },
                           { c_dir.getX(), c_dir.getY(), c_dir.getZ(), 0 },
                           { 0,            0,            0,            1 } };

  UB.set( UB_inv_arr );
/*
  System.out.println("********** UB inverse should be = ");
  System.out.println( UB );
*/
  UB.invert();
//  System.out.println("UB = " + UB );
                                     // now gradually bring in the remaining
                                     // peaks and re-optimize the UB to index
                                     // them as well

  Vector<Vector3D> miller_ind = new Vector<Vector3D>();
  Vector<Vector3D> indexed_qs = new Vector<Vector3D>();
  Vector           index_vals = new Vector();

  float[] fit_error = new float[1];

  int     num_indexed = 0;
/*
  num_indexed = GetIndexedPeaks_1D( a_dir, some_qs, required_tolerance,
                        index_vals, indexed_qs, fit_error  );
  System.out.println("***** USING A_DIR ONLY, NUM INDEXED = " + num_indexed );
  for ( int i = 0; i < index_vals.size(); i++ )
    System.out.println("Q = " + indexed_qs.elementAt(i) +
                       " H = " + index_vals.elementAt(i) );

  num_indexed = GetIndexedPeaks_1D( b_dir, some_qs, required_tolerance,
                        index_vals, indexed_qs, fit_error  );
  System.out.println("***** USING B_DIR ONLY, NUM INDEXED = " + num_indexed );
  for ( int i = 0; i < index_vals.size(); i++ )
    System.out.println("Q = " + indexed_qs.elementAt(i) +
                       " K = " + index_vals.elementAt(i) );

  num_indexed = GetIndexedPeaks_1D( c_dir, some_qs, required_tolerance,
                        index_vals, indexed_qs, fit_error  );
  System.out.println("***** USING C_DIR ONLY, NUM INDEXED = " + num_indexed );
  for ( int i = 0; i < index_vals.size(); i++ )
    System.out.println("Q = " + indexed_qs.elementAt(i) +
                       " L = " + index_vals.elementAt(i) );

  num_indexed = GetIndexedPeaks( UB, some_qs, required_tolerance,
                                 miller_ind, indexed_qs, fit_error );

  System.out.println("***** AFTER FORMING UB, NUM INDEXED = " + num_indexed );
  for ( int i = 0; i < indexed_qs.size(); i++ )
    System.out.println("Q = " + indexed_qs.elementAt(i) +
                       " HKL = " + miller_ind.elementAt(i) );
*/

  int     count = 0;
  while ( num_initial < q_vectors.size() )
  {
    count++;
    num_initial = Math.round(1.5f * num_initial + 3);
                                             // add 3, in case we started with
                                             // a very small number of peaks!
    if ( num_initial >= q_vectors.size() )
      num_initial = q_vectors.size();

    for ( int i = some_qs.size(); i < num_initial; i++ )
      some_qs.add( q_vectors.elementAt(i) );

    Tran3D temp_UB = new Tran3D( UB );
    num_indexed = GetIndexedPeaks( UB, some_qs, required_tolerance,
                                   miller_ind, indexed_qs, fit_error );
/*
    System.out.println("Before Optimize_UB_3D Indexed " + num_indexed + 
                       " of " + num_initial );
*/
    fit_error[0] = Optimize_UB_3D( temp_UB, miller_ind, indexed_qs );
//    System.out.println("After Optimize_UB_3D Indexed " + num_indexed );

    if ( !Float.isNaN( fit_error[0] ) )
    {
      UB.set( temp_UB );
      System.out.println("Indexed " + num_indexed + " of " + num_initial );
    }
    else
      System.out.println("Optimize_UB_3D FAILED WITH "+ num_initial +" peaks");
  }

  if ( original_qs.size() >= 5 )   // try one last refinement using all peaks
  {
    Tran3D temp_UB = new Tran3D( UB );
    num_indexed = GetIndexedPeaks( UB, original_qs, required_tolerance,
                                   miller_ind, indexed_qs, fit_error );
    fit_error[0] = Optimize_UB_3D( temp_UB, miller_ind, indexed_qs );
    if ( !Float.isNaN( fit_error[0] ) )
      UB.set( temp_UB );
  }

//  System.out.println("Original UB = " + UB );
                                 // Regardless of how we got the UB, find the
                                 // sum-squared errors for the indexing in
                                 // HKL space.
  num_indexed = GetIndexedPeaks( UB, q_vectors, required_tolerance,
                                 miller_ind, indexed_qs, fit_error );
//  System.out.println("Indexed " + num_indexed + 
//                     " average ^2 error = " + fit_error[0]/num_indexed );

                                 // now, get rid of shift and see how well 
                                 // it works.
  float[][] UB_array = UB.get();
  for ( int i = 0; i < 3; i++ )
  {
    UB_array[3][i] = 0;
    UB_array[i][3] = 0;
  }
  UB_array[3][3] = 1;
  UB.set( UB_array );
  num_indexed = GetIndexedPeaks( UB, original_qs, required_tolerance,
                                 miller_ind, indexed_qs, fit_error );
  try
  {
  float[][] floatUB_4 = UB.get();
  float[][] floatUB = new float[3][3];
  for ( int i = 0; i < 3; i++ )
    for ( int j = 0; j < 3; j++ )
      floatUB[i][j] = floatUB_4[i][j];
  floatUB = subs.Nigglify( floatUB );
  UB.set( floatUB );
  }
  catch ( Exception ex )
  {
    System.out.println("Exception in subs.Nigglify " );
    System.out.println(ex);
    ex.printStackTrace();
  } 

/*
  System.out.println("After subs.Nigglify UB = " + UB ); 

  num_indexed = GetIndexedPeaks( UB, original_qs, required_tolerance,
                                 miller_ind, indexed_qs, fit_error );
  System.out.println("Indexed " + num_indexed +
                     " average ^2 error = " + fit_error[0]/num_indexed );
*/
  return fit_error[0];
}


/**
 * Sort the specified list of Vector3D objects in order of increasing magnitude
 * and return a new Vector containing references to the original vectors, but in
 * order of increasing magnitude.
 *
 * @param  Vector of Vector3D obejcts that are to be sorted
 * @return A new Vector containing references to the orginal Vector3D 
 *          objects, but in increasing order.
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
    STATIC method Optimize_UB: Calculates the matrix that most nearly maps
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
public static float Optimize_UB_3D( Tran3D UB,
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


public static float Optimize_UB_4D( Tran3D UB,
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
  double[][] H_transpose = new double[ hkl_vectors.size() ][ 4 ];
  for ( int i = 0; i < hkl_vectors.size(); i++ )
  {
    Vector3D hkl = (Vector3D)( hkl_vectors.elementAt(i) );
    H_transpose[i][0] = hkl.getX();
    H_transpose[i][1] = hkl.getY();
    H_transpose[i][2] = hkl.getZ();
    H_transpose[i][3] = 1;
  }

  double[][] Q = LinearAlgebra.QR_factorization( H_transpose );

  float[][] augmented_UB_array = new float[4][4];

  double[] b = new double[ q_vectors.size() ];
  for ( int row = 0; row < 3; row++ )
  {
    for ( int i = 0; i < q_vectors.size(); i++ )
    {
      Vector3D q = (Vector3D)( q_vectors.elementAt(i) );
      b[i] = q.get()[row];
    }

    float error = (float)LinearAlgebra.QR_solve( H_transpose, Q, b );

    for ( int col = 0; col < 4; col++ )
      augmented_UB_array[row][col] = (float)b[col];

    sum_sq_error += error*error;
  }

  for ( int col = 0; col < 3; col++ )        // set last row to 0,0,0,1
    augmented_UB_array[3][col] = 0;
  augmented_UB_array[3][3] = 1;   
/*
  System.out.println("Shift = " + augmented_UB_array[0][0] +
                           "  " + augmented_UB_array[0][1] +
                           "  " + augmented_UB_array[0][2] );
*/
  UB.set( augmented_UB_array );

  return sum_sq_error;
}


/** 
    STATIC method Optimize_Direction_3D: Calculates the vector for which the
    dot product of the the vector with each of the specified Qxyz vectors 
    is most nearly the corresponding integer index.  The calculated best_vec
    minimizes the sum squared differences between best_vec dot (qx,qy,z) 
    and the corresponding index for all of the specified Q vectors and 
    indices.  The sum of the squares of the residual errors is returned.
    NOTE: This method is similar the Optimize_UB method, but this method only
          optimizes the plane normal in one direction.  Also, this optimizes
          the mapping from (qx,qy,qz) to one index (Q to index), while the 
          Optimize_UB method optimizes the mapping from three (h,k,l) to
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

public static float Optimize_Direction_3D( Vector3D best_vec,
                                           Vector   index_values,
                                           Vector   q_vectors )
{
  if ( index_values.size() < 3 )
   throw new IllegalArgumentException("Three or more index values needed");

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


public static float Optimize_Direction_4D( Vector3D best_vec,
                                           Vector   index_values,
                                           Vector   q_vectors )
{
  if ( index_values.size() < 3 )
   throw new IllegalArgumentException("Three or more index values needed");

  if ( index_values.size() != q_vectors.size() )
   throw new
    IllegalArgumentException( "Number of index_values != number of q_vectors");

                                     // Make the H-transpose matrix from the
                                     // q vectors and form QR factorization
  double[][] H_transpose = new double[ q_vectors.size() ][ 4 ];
  for ( int i = 0; i < q_vectors.size(); i++ )
  {
    Vector3D q = (Vector3D)( q_vectors.elementAt(i) );
    H_transpose[i][0] = q.getX();
    H_transpose[i][1] = q.getY();
    H_transpose[i][2] = q.getZ();
    H_transpose[i][3] = 1;
  }

  double[][] Q = LinearAlgebra.QR_factorization( H_transpose );

  double[] b = new double[ index_values.size() ];
  for ( int i = 0; i < index_values.size(); i++ )
    b[i] = (Integer)( index_values.elementAt(i) );

  float error = (float)LinearAlgebra.QR_solve( H_transpose, Q, b );
/*
  if ( b.length > 3 )
    System.out.println( "SHIFT = " + b[3] + " in Optimize_Direction_4D" );
*/ 
  best_vec.set( (float)b[0], (float)b[1], (float)b[2] );

  return error*error;
}



/**
  Check whether or not the components of the specified vector are within
  the specified tolerance of integer values, other than (0,0,0).
  @param hkl        A new Vector3D object containing what may be valid Miller
                    indices for a peak.
  @param tolerance  The maximum acceptable deviation from integer values for
                    the Miller indices.
  @return true if all components of the vector are within the tolerance of
               integer values (h,k,l) and (h,k,l) is NOT (0,0,0)
 */

public static boolean ValidIndex( Vector3D hkl, float tolerance )
{
  boolean valid_index = false;

  int h,k,l;

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

  @param direction           Direction vector in the direction of the 
                             normal vector for a family of parallel planes
                             in reciprocal space.  The length of this vector 
                             must be the reciprocal of the plane spacing.
  @param q_vectors           List of new Vector3D peaks in reciprocal space
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
                             This is a measure of the error in HKL space.

  @return The number of q_vectors that are indexed to within the specified
          tolerance, in the specified direction.

 */
public static int GetIndexedPeaks_1D( Vector3D  direction,
                                      Vector    q_vectors,
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

  @param direction_1         Direction vector in the direction of the normal
                             vector for the first family of parallel planes.
  @param direction_2         Direction vector in the direction of the normal
                             vector for the second family of parallel planes.
  @param direction_3         Direction vector in the direction of the normal
                             vector for the third family of parallel planes.
  @param q_vectors           List of new Vector3D peaks in reciprocal space
  @param required_tolerance  The maximum allowed error (as a faction of
                             the corresponding Miller index) for a peak
                             q_vector to be counted as indexed.
  @param miller_indices      List of the Miller indices (h,k,l) of peaks
                             that were indexed in all specified directions.
  @param indexed_qs          List of Qxyz value for the peaks that were
                             indexed indexed in all specified directions.
  @param fit_error           The sum of the squares of the distances from
                             integer values for the projections of the 
                             indexed q_vectors on the specified directions.
                             This is a measure of the error in HKL space.

  @return The number of q_vectors that are indexed to within the specified
          tolerance, in the specified direction.

 */
public static int GetIndexedPeaks_3D( Vector3D  direction_1,
                                      Vector3D  direction_2,
                                      Vector3D  direction_3,
                                      Vector    q_vectors,
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
  @param miller_indices      List of the Miller indices (h,k,l) of peaks
                             that were indexed in all specified directions.
  @param indexed_qs          List of Qxyz value for the peaks that were
                             indexed indexed in all specified directions.
  @param fit_error           The sum of the squares of the distances from
                             integer values for the UB*Q for the specified
                             UB matrix and the specified q_vectors.
                             This is a measure of the error in HKL space.

  @return The number of q_vectors that are indexed to within the specified
          tolerance, by the specified UB matrix. 
 */
public static int GetIndexedPeaks( Tran3D    UB,
                                   Vector    q_vectors,
                                   float     required_tolerance,
                                   Vector    miller_indices,
                                   Vector    indexed_qs,
                                   float[]   fit_error )
{
    float    error;
    int      num_indexed = 0;
    Vector3D hkl_vec = new Vector3D();

    miller_indices.clear();
    indexed_qs.clear();
    fit_error[0] = 0;

    Tran3D UB_inverse = new Tran3D( UB );
    UB_inverse.invert();

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

  @param n_steps        The number of vectors to generate around the circle. 
  @param axis           The axis perpendicular to the circle, through its 
                        center.
  @param angle_degrees  The angle between the axis vector and vectors from 
                        the origin to points on the circle.

  @return A Vector containing direction vectors forming the same angle
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
//  long start_time = System.nanoTime();

  int num_a_steps = (int)Math.round( 90.0 / degrees_per_step );
  double gamma_radians = gamma * Math.PI / 180.0;

  int num_b_steps = (int)Math.round(4*Math.sin( gamma_radians ) * num_a_steps);
/*
  System.out.println("a, b, c = " + a + ", " + b + ", " + c );
  System.out.println("alpha, beta, gamma = " + 
                       alpha + ", " + beta + ", " + gamma );
  System.out.println("num_a_steps = " + num_a_steps );
  System.out.println("num_b_steps = " + num_b_steps );
*/
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
/*
  long end_time = System.nanoTime();
  System.out.println("ELAPSED TIME = " + (end_time-start_time)/1.0e9 );
*/
  return min_error;
}


/**
 *  The method uses two passes to scan across all possible directions and 
 *  orientations to find the direction and orientation for the unit cell
 *  that best fits the specified list of peaks.  
 *  On the first pass, only those sets of directions that index the 
 *  most peaks are kept.  On the second pass, the directions that minimize 
 *  the sum-squared deviations from integer indices are selected from that 
 *  smaller set of directions.  This method should be most useful if number 
 *  of peaks is on the order of 10-20, and most of the peaks belong to the 
 *  same crystallite.
 *  @param UB                 This will be set to the UB matrix that best
 *                            indexes the supplied list of q_vectors.
 *  @param q_vectors          List of locations of peaks in "Q".
 *  @param a                  Lattice parameter "a".
 *  @param b                  Lattice parameter "b".
 *  @param c                  Lattice parameter "c".
 *  @param alpha              Lattice parameter alpha.
 *  @param beta               Lattice parameter beta.
 *  @param gamma              Lattice parameter gamma.
 *  @param degrees_per_step   The number of degrees per step used when 
 *                            scanning through all possible directions and
 *                            orientations for the unit cell. NOTE: The
 *                            work required rises very rapidly as the number
 *                            of degrees per step decreases. A value of 1
 *                            degree leads to about 10 seconds of compute time.
 *                            while a value of 2 only requires a bit more than
 *                            1 sec.  The required time is O(n^3) where 
 *                            n = 1/degrees_per_step.
 *                             
 *  @param required_tolerance The maximum distance from an integer that the
 *                            calculated h,k,l values can have if a peak 
 *                            is to be considered indexed.
 */

public static float ScanFor_UB( Tran3D   UB,
                                Vector   q_vectors,
                                float    a,     float b,    float c,
                                float    alpha, float beta, float gamma,
                                float    degrees_per_step,
                                float    required_tolerance )
{
  Vector3D a_dir = new Vector3D();
  Vector3D b_dir = new Vector3D();
  Vector3D c_dir = new Vector3D();

  float error = ScanFor_UB( a_dir, b_dir, c_dir,
                            q_vectors,
                            a, b, c, alpha, beta, gamma,
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
                                Vector   q_vectors,
                                float    a,
                                float    b,
                                float    c,
                                float    alpha,
                                float    beta,
                                float    gamma,
                                float    degrees_per_step )
{
//  long start_time = System.nanoTime();

  int num_a_steps = (int)Math.round( 90.0 / degrees_per_step );
  double gamma_radians = gamma * Math.PI / 180.0;

  int num_b_steps = (int)Math.round(4*Math.sin( gamma_radians ) * num_a_steps);
/*
  System.out.println("a, b, c = " + a + ", " + b + ", " + c );
  System.out.println("alpha, beta, gamma = " +
                       alpha + ", " + beta + ", " + gamma );
  System.out.println("num_a_steps = " + num_a_steps );
  System.out.println("num_b_steps = " + num_b_steps );
*/
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
/*
  long end_time = System.nanoTime();
  System.out.println("ELAPSED TIME = " + (end_time-start_time)/1.0e9 );
*/
  return min_error;
}


/**
 *  The method uses two passes to scan across all possible directions and 
 *  orientations to find the direction and orientation for the unit cell
 *  that best fits the specified list of peaks.  
 *  On the first pass, only those sets of directions that index the 
 *  most peaks are kept.  On the second pass, the directions that minimize 
 *  the sum-squared deviations from integer indices are selected from that 
 *  smaller set of directions.  This method should be most useful if number 
 *  of peaks is on the order of 10-20, and most of the peaks belong to the 
 *  same crystallite.
 *  @param a_dir              This will be set to a vector corresponding to
 *                            side "a" in the rotated unit cell. 
 *  @param b_dir              This will be set to a vector corresponding to
 *                            side "b" in the rotated unit cell. 
 *  @param c_dir              This will be set to a vector corresponding to
 *                            side "a" in the rotated unit cell. 
 *  @param q_vectors          List of locations of peaks in "Q".
 *  @param a                  Lattice parameter "a".
 *  @param b                  Lattice parameter "b".
 *  @param c                  Lattice parameter "c".
 *  @param alpha              Lattice parameter alpha.
 *  @param beta               Lattice parameter beta.
 *  @param gamma              Lattice parameter gamma.
 *  @param degrees_per_step   The number of degrees per step used when 
 *                            scanning through all possible directions and
 *                            orientations for the unit cell. NOTE: The
 *                            work required rises very rapidly as the number
 *                            of degrees per step decreases. A value of 1
 *                            degree leads to about 10 seconds of compute time.
 *                            while a value of 2 only requires a bit more than
 *                            1 sec.  The required time is O(n^3) where 
 *                            n = 1/degrees_per_step.
 *                             
 *  @param required_tolerance The maximum distance from an integer that the
 *                            calculated h,k,l values can have if a peak 
 *                            is to be considered indexed.
 */
public static float ScanFor_UB( Vector3D a_dir,
                                Vector3D b_dir,
                                Vector3D c_dir,
                                Vector   q_vectors,
                                float    a,
                                float    b,
                                float    c,
                                float    alpha,
                                float    beta,
                                float    gamma,
                                float    degrees_per_step,
                                float    required_tolerance )
{
// long start_time = System.nanoTime();

  int num_a_steps = (int)Math.round( 90.0 / degrees_per_step );
  double gamma_radians = gamma * Math.PI / 180.0;

  int num_b_steps = (int)Math.round(4*Math.sin( gamma_radians ) * num_a_steps);
/*
  System.out.println("a, b, c = " + a + ", " + b + ", " + c );
  System.out.println("alpha, beta, gamma = " +
                       alpha + ", " + beta + ", " + gamma );
  System.out.println("num_a_steps = " + num_a_steps );
  System.out.println("num_b_steps = " + num_b_steps );
*/
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
                                          // now, for each such direction, find
                                          // the one that indexes closes to
                                          // integer values
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
/*
  long end_time = System.nanoTime();
  System.out.println("ELAPSED TIME = " + (end_time-start_time)/1.0e9 );
*/
  return min_error;
}


public static void ScanFor_Directions( Vector<Vector3D> directions,
                                Vector   q_vectors,
                                float    d_min,
                                float    d_max,
                                float    degrees_per_step,
                                float    required_tolerance )
{
//  long start_time = System.nanoTime();

  int num_steps = (int)Math.round( 90.0 / degrees_per_step );
/*
  System.out.println("num_steps = " + num_steps );
*/
  Vector<Vector3D> full_list = MakeHemisphereDirections( num_steps );

  float    error;
  float[]  fit_error = new float[1];
  float    dot_prod;
  int      nearest_int;
  int      max_indexed = 0;
  Vector3D q_vec = new Vector3D();

  float delta_d = 0.1f;
  int n_steps = Math.round( 1 +(d_max - d_min)/delta_d );
                                              // first select those directions
                                              // that index the most peaks,
                                              // scaled to right length, only
                                              // keeping the longest one for
                                              // each direction
  Vector<Vector3D> selected_dirs = new Vector<Vector3D>();
  Vector3D dir_temp = new Vector3D();

  for ( int dir_num = 0; dir_num < full_list.size(); dir_num++ )
  {
    Vector3D current_dir = full_list.elementAt( dir_num );

    for ( int step = 0; step <= n_steps; step++ )
    {
      dir_temp.set( current_dir );
//      dir_temp.multiply( d_max - step * delta_d );    // decreasing size
      dir_temp.multiply( d_min + step * delta_d );    // increasing size
      
      int num_indexed = 0;
      for ( int q_num = 0; q_num < q_vectors.size(); q_num++ )
      {
        boolean indexes_peak = true;
        q_vec = (Vector3D)(q_vectors.elementAt( q_num ));
        dot_prod = dir_temp.dot( q_vec );
        nearest_int = Math.round( dot_prod );
        error = Math.abs( dot_prod - nearest_int );
        if ( error <= required_tolerance )
          num_indexed++;
      }

      if ( num_indexed > max_indexed )     // only keep those directions that
      {                                    // index the max number of peaks
        selected_dirs.clear();
        max_indexed = num_indexed;
      }
      if ( num_indexed >= max_indexed )
      {
        selected_dirs.add( new Vector3D( dir_temp ) );
      }
    }
  }

/*
  System.out.println("After stage 1, number of directions = " + 
                      selected_dirs.size() );
  System.out.println("After stage 1, max_indexed = " + max_indexed );

  for ( int i = 0; i < selected_dirs.size(); i++ )
    System.out.println(" i = " + i +
                       " Length = " + selected_dirs.elementAt(i).length() +
                       " Vector = " + selected_dirs.elementAt(i) );
*/
                                          // now, for each such direction,
                                          // optimize it and discard duplicates 
  Vector<Vector3D> index_vals = new Vector<Vector3D>();
  Vector<Vector3D> indexed_qs = new Vector<Vector3D>();
  directions.clear();
  for ( int dir_num = 0; dir_num < selected_dirs.size(); dir_num++ )
  {
    Vector3D current_dir = selected_dirs.elementAt( dir_num );

    int num_indexed = GetIndexedPeaks_1D( current_dir,
                        q_vectors,
                        required_tolerance,
                        index_vals,
                        indexed_qs,
                        fit_error  );

    Optimize_Direction_3D( current_dir, index_vals, indexed_qs );
/*
    System.out.println(" dir_num = " + dir_num +
                       " Length = " + current_dir.length() +
                       " Vector = " + current_dir );
*/
    float length = current_dir.length();
    if ( length >= d_min && length <= d_max )   // only keep if within range
    {
      boolean duplicate = false;
      for ( int i = 0; i < directions.size(); i++ )
      {
        dir_temp = directions.elementAt(i);
                                                // discard duplicate
        if ( Math.abs(current_dir.getX() - dir_temp.getX())<0.001f && 
             Math.abs(current_dir.getY() - dir_temp.getY())<0.001f && 
             Math.abs(current_dir.getZ() - dir_temp.getZ())<0.001f )
          duplicate = true; 
                                                // discard minus duplicate
        if ( Math.abs(current_dir.getX() + dir_temp.getX())<0.001f &&
             Math.abs(current_dir.getY() + dir_temp.getY())<0.001f &&
             Math.abs(current_dir.getZ() + dir_temp.getZ())<0.001f )
          duplicate = true;
      }
      if (!duplicate)
        directions.add( current_dir );
    }

  }

/*
  long end_time = System.nanoTime();
  System.out.println("ELAPSED TIME = " + (end_time-start_time)/1.0e9 );

  System.out.println("Number of directions = " + directions.size() );
  for ( int dir_num = 0; dir_num < directions.size(); dir_num++ )
  {
    dir_temp = directions.elementAt( dir_num );

    int num_indexed = GetIndexedPeaks_1D( dir_temp,
                                          q_vectors,
                                          required_tolerance,
                                          index_vals,
                                          indexed_qs,
                                          fit_error  );

    System.out.println(" dir_num = " + dir_num +
                       " Length = " + dir_temp.length() +
                       " Vector = " + dir_temp + 
                       " Num Indexed = " + num_indexed );
  }

  System.out.println("NUMBER OF Q_VECTORS = " + q_vectors.size() );
*/
}



/**
 *  For a rotated unit cell, calculate the vector in the direction of edge
 *  "c" given two vectors a_dir and b_dir in the directions of edges "a" 
 *  and "b", with lengths a and b, and the cell angles.
 *  @param  a_dir   Vector3D object with length "a" in the direction of the  
 *                  rotated cell edge "a"
 *  @param  b_dir   Vector3D object with length "b" in the direction of the 
 *                  rotated cell edge "b"
 *  @param  c       The length of the third cell edge, c.
 *  @param  alpha   angle between edges b and c. 
 *  @param  beta    angle between edges c and a. 
 *  @param  gamma   angle between edges a and b. 
 *
 *  @return A new Vector3D object with length "c", in the direction of the 
 *          third rotated unit cell edge, "c".
 */
public static Vector3D Make_c_dir( Vector3D a_dir, Vector3D b_dir,
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

    num_indexed = GetIndexedPeaks_1D( dir_vec,
                                      q_vectors,
                                      required_tolerance,
                                      index_vals,
                                      indexed_qs,
                                      fit_error  );
    if ( num_indexed >= required_number )
    {
      fit_error[0] = Optimize_Direction_3D( dir_vec, index_vals, indexed_qs );

      num_indexed = GetIndexedPeaks_1D( dir_vec,
                                        q_vectors,
                                        required_tolerance,
                                        index_vals,
                                        indexed_qs,
                                        fit_error  );

      fit_error[0] = Optimize_Direction_3D( dir_vec, index_vals, indexed_qs );

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

    num_indexed = GetIndexedPeaks_1D( dir_vec,
                                      q_vectors,
                                      required_tolerance,
                                      index_vals,
                                      indexed_qs,
                                      fit_error  );
    if ( num_indexed >= 0.7 * max_indexed && fit_error[0] < 5 * min_error )
    {
      fit_error[0] = Optimize_Direction_3D( dir_vec, index_vals, indexed_qs );

      num_indexed = GetIndexedPeaks_1D( dir_vec,
                                        q_vectors,
                                        required_tolerance,
                                        index_vals,
                                        indexed_qs,
                                        fit_error  );

      fit_error[0] = Optimize_Direction_3D( dir_vec, index_vals, indexed_qs );
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

/*
  System.out.println("ON SECOND PASS, NUMBER OF VECTORS = " + edge_list.size());
  System.out.println("Max_indexed = " + max_indexed );
  System.out.println("min_error = " + min_error );
*/

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
                                q_vectors,
                                a, b, c, alpha, beta, gamma,
                                degrees_per_step );

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

    fit_error = Optimize_Direction_3D( dir_vec, index_vals, q_vectors );
/*
    System.out.println("Fit error = " + fit_error );
    System.out.println("Sigma = " + Math.sqrt(fit_error) / hkls.size() );
    System.out.println("Direction = " + dir_vec );
*/
    float length = dir_vec.length();
    System.out.println("Length    = " + length );

    index_vals.clear();
  }


  Tran3D UB = new Tran3D();
  fit_error = Optimize_UB_3D( UB, hkls, q_vectors );
//  System.out.println("fit_error in UB = " + fit_error ); 
//  System.out.println("UB = \n" + UB ); 
  UB.invert();
//  System.out.println("UB inverse = \n" + UB ); 

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

public static  Vector<Vector3D>  getPeakQVals( Vector Peaks)
{
   Vector<Vector3D > Res= new Vector<Vector3D>();
   for( int i=0; i< Peaks.size( ); i++)
   {
      IPeak peak =(IPeak) Peaks.elementAt( i );
      Vector3D V = new Vector3D( peak.getUnrotQ( ));
      Res.add( V );
   }
   
   return Res;
}

public static Tran3D Convert2Tran3D( Vector UB)
{
   if( UB== null || (UB.size()!=3 && UB.size()!=9))
      throw new IllegalArgumentException("UB Matrix is the wrong size ");
   float[] R = new float[16];
   Arrays.fill( R, 0f);
   R[15] = 1;
   for( int r =0; r<3;r++)
      for( int c=0; c<3;c++)
      {
         if( UB.size()==9)
            R[r*3+c]= ((Number)UB.elementAt( r*3+c )).floatValue();
         else
            R[r*3+c] = ((Number)(((Vector)UB.elementAt( r )).elementAt(c))).floatValue( );
      }
   
   return new Tran3D(R);
}

public static Vector Convert2Vector( Tran3D UB)
{
    if( UB== null )
       throw new IllegalArgumentException("UB Matrix is null ");
    
    float[][] vals = UB.get( );
    Vector Res = new Vector();
    for( int r=0; r<3;r++)
    {  Vector row = new Vector();
       for( int c=0; c<3;c++)
          row.add( vals[r][c] );
       Res.add(row);
    }
    return Res;
}


public static float[][] Convert2floatArrayArray( Tran3D UB)
{
    if( UB== null )
       throw new IllegalArgumentException("UB Matrix is null ");
    
    float[][] vals =UB.get( );
    float[][] Res = new float[3][3];

    for( int r=0; r<3;r++)
    { 
       for( int c=0; c<3;c++)
         Res[r][c]= vals[r][c] ;
       
    }
    return Res;
}
} // end of class

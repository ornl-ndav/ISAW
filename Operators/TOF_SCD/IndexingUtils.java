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

import jnt.FFT.*;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.MathTools.*;

import DataSetTools.components.ui.Peaks.subs;
import DataSetTools.operator.Generic.TOF_SCD.IPeak;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;

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
                                    // one peak that way.)
  {
    Vector<Vector3D> shifted_qs = new Vector<Vector3D>();
    int mid_ind = q_vectors.size()/3;
                                    // either do an initial sort and use
                                    // default mid index, or use the mid index
                                    // specified by the base_peak parameter
    if ( base_index < 0 || base_index >= q_vectors.size() )  
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

    try
    {
      Tran3D temp_UB = new Tran3D( UB );
      num_indexed = GetIndexedPeaks( UB, some_qs, required_tolerance,
                                     miller_ind, indexed_qs, fit_error );

      fit_error[0] = Optimize_UB_4D( temp_UB, miller_ind, indexed_qs );

      if ( !Float.isNaN( fit_error[0] ) )
        UB.set( temp_UB );
    }
    catch ( Exception ex )
    {
      System.out.println( "Optimize failed with " + num_initial +" peaks " +
                          "continuing ... " );
    }
  }

//  System.out.println("Finished growing set of peaks...");

  if ( original_qs.size() >= 5 )    // try one last refinement using all 
  {                                 // original peaks
    try
    {
      Tran3D temp_UB = new Tran3D( UB );
      num_indexed = GetIndexedPeaks( UB, original_qs, required_tolerance,
                                     miller_ind, indexed_qs, fit_error );
//    fit_error[0] = Optimize_UB_3D( temp_UB, miller_ind, indexed_qs );
      fit_error[0] = Optimize_UB_4D( temp_UB, miller_ind, indexed_qs );
      if ( !Float.isNaN( fit_error[0] ) )
        UB.set( temp_UB );
    }
    catch ( Exception ex )
    {
      System.out.println( "Optimize failed using all original peaks, "+
                          "continuing... " );
    }
  }
//  System.out.println("Finished finished refining all peaks ...");

                                 // Regardless of how we got the UB, find the
                                 // sum-squared errors for the indexing in 
                                 // HKL space.
  num_indexed = GetIndexedPeaks( UB, original_qs, required_tolerance,
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
  that most nearly indexes the specified q_vectors, given only a range of 
  possible unit cell edge lengths.  

     The resolution of the search through possible orientations is specified
  by the degrees_per_step parameter.  Approximately 1-3 degrees_per_step is
  usually adequate.  NOTE: This is an expensive calculation which takes 
  approximately 1 second using 1 degree_per_step.  However, the execution 
  time is O(n^3) so decreasing the resolution to 0.5 degree per step will take 
  about 8 seconds, etc.  It should not be necessary to decrease this value 
  below 1 degree per step, and users will have to be VERY patient, if it is
  decreased much below 1 degree per step.
    The number of peaks used to obtain an initial indexing is specified by
  the "num_initial" parameter.  Good values for this are typically around 
  15-25.  The specified q_vectors must correspond to a single crystal.  If
  several crystallites are present or there are other sources of "noise" 
  leading to invalid peaks, this method will not work well.  The method that
  uses lattice parameters may be better in such cases.  Alternatively, adjust
  the list of specified q_vectors so it does not include noise peaks or peaks
  from more than one crystal, by increasing the threshold for what counts
  as a peak, or by other methods.

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
                                If fewer than 6 peaks are specified in the
                                q_vectors list, this parameter is ignored.
                                If this parameter is -1, and there are at least
                                six peaks in the q_vector list, then a base
                                index will be calculated internally.  In most
                                cases, it should suffice to set this to -1.
    @param  num_initial         The number of low |Q| peaks that are used
                                when scanning for an initial indexing.
    @param  degrees_per_step    The number of degrees between directions that
                                are checked while scanning for an initial 
                                indexing of the peaks with lowest |Q|.

    @return  This will return the sum of the squares of the residual errors.
  
    @throws  IllegalArgumentException if there are not at least 3 q vectors,
                                      of if this routine fails to find a UB.
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
/*
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
*/

  if ( num_initial > q_vectors.size() )
    num_initial = q_vectors.size();

  Vector<Vector3D> some_qs = new Vector<Vector3D>();
  for ( int i = 0; i < num_initial; i++ )
    some_qs.add( q_vectors.elementAt(i) );
/*
  System.out.println("Some Qs = " + some_qs.size() );
  for ( int i = 0; i < some_qs.size(); i++ )
    System.out.println("" + some_qs.elementAt(i) +
                       "  length = " + some_qs.elementAt(i).length() );
*/
  Vector<Vector3D> directions = new Vector<Vector3D>();

  int max_indexed = 0;
  max_indexed = ScanFor_Directions( directions,
                                    some_qs,
                                    min_d, max_d,
                                    required_tolerance,
                                    degrees_per_step );
//  }

  System.out.println("####### AFTER SCAN, MAX_INDEXED = " + max_indexed );
  System.out.println("####### Found " + directions.size() + " directions");

  directions = SortOnVectorMagnitude( directions );

  if ( max_indexed == 0 )
    throw new IllegalArgumentException("Could not find any a,b,c to index Qs");

  if ( directions.size() < 3 )
    throw new IllegalArgumentException("Could not find enough a,b,c vectors");

  float[] fit_error   = new float[1];
  Vector<Integer>  index_vals = new Vector<Integer>();
  Vector<Vector3D> indexed_qs = new Vector<Vector3D>();

  int     num_indexed = 0;
  int     num_sets    = 0;
  int     set_used    = -1;
  float   min_error   = Float.POSITIVE_INFINITY;

  max_indexed = GetIndexedPeaks_1D( directions.elementAt(0),
                                    q_vectors,
                                    required_tolerance,
                                    index_vals,
                                    indexed_qs,
                                    fit_error );

  Tran3D temp_UB = new Tran3D();
  float min_deg  = 40;        
  for ( int i = 0; i < directions.size()-2; i++ )
  {
    if ( FormUB_From_abc_Vectors( temp_UB, directions, i, min_deg ) )
    {
      num_sets++;
      num_indexed = ExpandSetOfIndexedPeaks( temp_UB, 
                                             q_vectors, 
                                             required_tolerance,
                                             num_initial,
                                             fit_error );
//#########
      if ( num_indexed > 0.25*max_indexed && fit_error[0] < min_error )
      {
        UB.set( temp_UB );
        min_error = fit_error[0];
        set_used = i;
      }
    }
  }

  if ( set_used == -1 )
    throw new IllegalArgumentException("Failed to find a UB matrix");
  else
    System.out.println("Used Set " + set_used + " of " + num_sets );

  Vector<Vector3D> miller_ind = new Vector<Vector3D>();

  if ( original_qs.size() >= 5 ) // try one last refinement using original peaks
  {
    temp_UB = new Tran3D( UB );
    num_indexed = GetIndexedPeaks( UB, original_qs, required_tolerance,
                                   miller_ind, indexed_qs, fit_error );
    try
    {
      fit_error[0] = Optimize_UB_3D( temp_UB, miller_ind, indexed_qs );
      if ( !Float.isNaN( fit_error[0] ) )
        UB.set( temp_UB );
    }
    catch (Exception ex)
    {
      System.out.println("Failed to improve UB one last time");
    }
  }

  System.out.print("Before Nigglify: ");
  ShowLatticeParameters( UB );

  Tran3D  Niggli_UB = new Tran3D(UB);
  boolean niggli_ok = MakeNiggliUB( UB, Niggli_UB );

  if ( niggli_ok )
  {
    UB.set( Niggli_UB );
    System.out.print("After  Nigglify: ");
    ShowLatticeParameters( UB );
  }

  return fit_error[0];
}


/**
    STATIC method FindUB_UsingFFT: This method will attempt to calculate the 
  matrix that most nearly indexes the specified q_vectors, given only a range 
  of possible unit cell edge lengths, by examining the FFTs of the projections
  of the Q-vectors on various directions.

     The resolution of the search through possible orientations is specified
  by the degrees_per_step parameter.  Approximately 1-3 degrees_per_step is
  usually adequate.  NOTE: This is an expensive calculation which takes
  approximately 1 second using 1 degree_per_step.  However, the execution
  time is O(n^3) so decreasing the resolution to 0.5 degree per step will take
  about 8 seconds, etc.  It should not be necessary to decrease this value
  below 1 degree per step, and users will have to be VERY patient, if it is
  decreased much below 1 degree per step.

    @param  UB                  3x3 matrix that will be set to the UB matrix
    @param  q_vectors           Vector of new Vector3D objects that contains
                                the list of q_vectors that are to be indexed
                                NOTE: There must be at least 4 q_vectors and it
                                really should have at least 10 or more 
                                peaks for this to work quite consistently.

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
    @param  degrees_per_step    The number of degrees between directions that
                                are checked while scanning for an initial
                                indexing of the peaks with lowest |Q|.

    @return  This will return the sum of the squares of the residual errors.

    @throws  IllegalArgumentException if there are not at least 4 q vectors,
                                      of if this routine fails to find a UB.
*/
public static float FindUB_UsingFFT( Tran3D             UB,
                                     Vector<Vector3D>   q_vectors,
                                     float              min_d,
                                     float              max_d,
                                     float              required_tolerance,
                                     float              degrees_per_step ) 
{
  if ( q_vectors.size() < 4 )
    throw new IllegalArgumentException("Need at least 4 q_vectors to find UB");

  if ( min_d >= max_d || min_d <= 0 )
    throw new IllegalArgumentException("Need 0 < min_d < max_d");

  if ( required_tolerance <= 0 )
    throw new IllegalArgumentException("required_tolerance must be positive");
  
  if ( degrees_per_step <= 0 )
    throw new IllegalArgumentException("degrees_per_step must be positive");

  Vector<Vector3D> directions = new Vector<Vector3D>();

  // Use a somewhat higher tolerance when finding individual directions
  // since it is easier to index one direction individually compared to
  // indexing three directions simultaneously.
  int max_indexed = FFTScanFor_Directions( directions, q_vectors,
                                           min_d, max_d,
                                           0.75f * required_tolerance,
                                           degrees_per_step );

  System.out.println("####### AFTER FFT SCAN, MAX_INDEXED = " + max_indexed );
  System.out.println("####### Found " + directions.size() + " directions");

  if ( max_indexed == 0 )
    throw new IllegalArgumentException("Could not find any a,b,c to index Qs");

  if ( directions.size() < 3 )
    throw new IllegalArgumentException("Could not find enough a,b,c vectors");

  directions = SortOnVectorMagnitude( directions );

  Tran3D temp_UB = new Tran3D();
  float  min_vol = min_d * min_d * min_d / 4.0f;
  if ( !FormUB_From_abc_Vectors( temp_UB, directions, q_vectors,
                                 required_tolerance, min_vol ) )
    throw new IllegalArgumentException("Could NOT form UB matrix from abc's");
  else
     UB.set( temp_UB );

  float[] fit_error = new float[1];

  if ( q_vectors.size() >= 5 )    // refine the matrix several times
  {
    Vector<Vector3D> indexed_qs = new Vector<Vector3D>( q_vectors.size() );
    Vector<Vector3D> miller_ind = new Vector<Vector3D>();
    for ( int i = 0; i < 4; i++ )
    {
      try
      {
        int num_indexed = GetIndexedPeaks( UB, q_vectors, required_tolerance,
                                           miller_ind, indexed_qs, fit_error );
        fit_error[0] = Optimize_UB_3D( temp_UB, miller_ind, indexed_qs );
        if ( !Float.isNaN( fit_error[0] ) )
          UB.set( temp_UB );
      }
      catch (Exception ex)
      {
        System.out.println("Failed to improve UB");
      }
    }
  }
 
  System.out.print("Before Nigglify: ");
  ShowLatticeParameters( UB );

  if ( !CheckUB( UB ) )
  {
    System.out.println("CheckUB says UB_IS_INVALID" );
    System.out.println("UB = ");
    System.out.println( UB );
    System.out.println();
    throw new IllegalArgumentException("CheckUB says UB_IS_INVALID");
  }
  
                                      // now call Nigglify, to get shortest abc
  Tran3D  Niggli_UB = new Tran3D(UB);
  boolean niggli_ok = MakeNiggliUB( UB, Niggli_UB );

  if ( niggli_ok )
  {
    UB.set( Niggli_UB );
    System.out.print("Niggli Reduced Cell: ");
    ShowLatticeParameters( UB );
  }

  return fit_error[0];
}


/**
 *  Form a UB matrix from the given list of possible directions, using the
 *  three directions that correspond to a unit cell with the smallest volume
 *  (greater than or equal to the specified minimum volume) that indexes at
 *  least 80% of the maximum number of peaks indexed by any set of three 
 *  distinct vectors chosen from the list.
 *
 *  @param UB            The calculated UB matrix will be returned in this 
 *                       parameter
 *  @param directions    List of possible vectors for a, b, c.  This list MUST
 *                       be sorted in order of increasing magnitude.
 *  @param q_vectors     The list of q_vectors that should be indexed 
 *  @param req_tolerance The required tolerance on h,k,l to consider a peak
 *                       to be indexed.
 *  @param min_vol       The smallest possible unit cell volume.
 *
 *  @return true if a UB matrix was set, and false if it not possible to
 *          choose a,b,c (i.e. UB) from the list of directions, starting
 *          with the specified a_index.
 */

private static boolean FormUB_From_abc_Vectors( Tran3D           UB,
                                                Vector<Vector3D> directions,
                                                Vector<Vector3D> q_vectors,
                                                float            req_tolerance,
                                                float            min_vol )
{
   int      max_indexed = 0;
   float    vol;
   float    alpha = 0, 
            beta  = 0, 
            gamma = 0;
   Vector3D a_dir = null, 
            b_dir = null, 
            c_dir = null,
            a_temp = null,
            b_temp = null, 
            c_temp = null;
   Vector3D acrossb = new Vector3D();

   float[] ave_2_error  = new float[1];

   for ( int i = 0; i < directions.size()-2; i++ )
   {
     a_temp = directions.elementAt( i );
     for ( int j = i+1; j < directions.size()-1; j++ )
     {
       b_temp = directions.elementAt( j );
       acrossb.cross( a_temp, b_temp );
       for ( int k = j+1; k < directions.size(); k++ )
       {
         c_temp = directions.elementAt( k );
         vol = Math.abs( acrossb.dot( c_temp ) );
         if ( vol > min_vol )
         {
           int n_tol = NumberIndexed_3D( a_temp, b_temp, c_temp, 
                                         q_vectors, req_tolerance, 
                                         ave_2_error );

           // Requiring 20% more indexed with longer edge lengths, favors 
           // the smaller unit cells.
           if ( n_tol > 1.20 * max_indexed ) 
           {
             max_indexed = n_tol;
             a_dir = a_temp;
             b_dir = b_temp;
             c_dir = c_temp;
             alpha = angle( b_temp, c_temp );
             beta  = angle( c_temp, a_temp );
             gamma = angle( a_temp, b_temp );
           }
         }
       }
     }
  }

  if ( a_dir == null )
  {
    System.out.println("********** NO abc FOUND **********");
    return false;
  }
                                   // now build the UB matrix from a,b,c       
                                   //
  if ( ! isRightHanded( a_dir, b_dir, c_dir ) )
    c_dir.multiply( -1 );

  float[][] UB_inv_arr = { { a_dir.getX(), a_dir.getY(), a_dir.getZ(), 0 },
                           { b_dir.getX(), b_dir.getY(), b_dir.getZ(), 0 },
                           { c_dir.getX(), c_dir.getY(), c_dir.getZ(), 0 },
                           { 0,            0,            0,            1 } };

  UB.set( UB_inv_arr );
  UB.invert();

  System.out.printf("Built UB from %8.3f  %8.3f  %8.3f  %8.2f  %8.2f  %8.2f\n",
                    a_dir.length(), b_dir.length(), c_dir.length(),
                    alpha, beta, gamma );
  return true;
}




/**
 *  Form a UB matrix from the given list of possible directions, using the
 *  direction at the specified index for the "a" direction.  The "b" and "c"
 *  directions are chosen so that 
 *   1) |a| < |b| < |c|, 
 *   2) the angle between the a, b, c, vectors is at least the minimum 
 *      angle specified
 *   3) c is not in the same plane as a and b.
 *
 *  @param UB           The calculated UB matrix will be returned in this 
 *                      parameter
 *  @param directions   List of possible vectors for a, b, c.  This list MUST
 *                      be sorted in order of increasing magnitude.
 *  @param a_index      The index to use for the a vector.  The b and c 
 *                      vectors will be choosen from LATER positions in the
 *                      directions list.
 *  @param min_deg      Minimum allowable angle between real space unit cell
 *                      edges.
 *
 *  @return true if a UB matrix was set, and false if it not possible to
 *          choose a,b,c (i.e. UB) from the list of directions, starting
 *          with the specified a_index.
 */
private static boolean FormUB_From_abc_Vectors( Tran3D           UB, 
                                                Vector<Vector3D> directions, 
                                                int              a_index, 
                                                float            min_deg  )
{
  int index = a_index;
  Vector3D a_dir = directions.elementAt( index );
  index++;

  float epsilon = 5;                    //  tolerance on right angle (degrees)
  Vector3D b_dir  = null;
  boolean b_found = false;
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
    }
    index++;
  }

  if ( ! b_found )
    return false;

  Vector3D c_dir  = new Vector3D();
  boolean c_found = false;

  Vector3D perp = new Vector3D();
  perp.cross( a_dir, b_dir );
  perp.normalize();
//  System.out.println("Perp vector = " + perp );
  float perp_ang;
  float alpha;
  float beta;

  while ( !c_found && index < directions.size() )
  {
    Vector3D vec = directions.elementAt(index);
    int factor = 1;                      
    while ( !c_found && factor >= -1 )    // try c in + or - direction
    {
      c_dir.set( vec );
      c_dir.multiply( factor );
      perp_ang = angle( perp, c_dir );
      alpha    = angle( b_dir, c_dir );
      beta     = angle( a_dir, c_dir );
                                       // keep a,b,c right handed by choosing
                                       // c in general directiion of a X b 
      if ( perp_ang < 90 - epsilon                      &&  
           alpha >= min_deg && (180 - alpha) >= min_deg &&
           beta  >= min_deg && (180 - beta ) >= min_deg  )
      {
        c_found = true;
      }
      factor -= 2;
    }

    if ( ! c_found )
      index++;
  }
/*
  float dot     = 0;
  float max_dot = 0;
  for ( int i = index; i < directions.size(); i++ )// find direction most nearly
  {                                                // perpendicular to a,b plane
    Vector3D vec  = directions.elementAt(i);
    Vector3D temp = new Vector3D( vec );
    temp.normalize();
    dot = Math.abs( perp.dot( temp ) );
    if ( dot > max_dot )
    {
      c_dir.set( vec );
      max_dot = dot;
      c_found = true;
      System.out.println("Setting as c_dir, i, max_dot = " + i + 
                         ", " + max_dot);
    }
  }

  perp_ang = angle( perp, c_dir );
  if ( perp_ang > 90 )                 // keep a,b,c right handed by choosing
  {                                    // c in general directiion of a X b 
    perp_ang = 180 - perp_ang;
    c_dir.multiply( -1.0f );
  }
*/

/*
  alpha = angle( b_dir, c_dir );
  beta  = angle( a_dir, c_dir );

  if ( perp_ang < 90 - epsilon                      &&  
       alpha >= min_deg && (180 - alpha) >= min_deg &&
       beta  >= min_deg && (180 - beta ) >= min_deg  )
  {
    c_found = true;
  }
*/
  if ( ! c_found )
    return false;
/*
  System.out.println("MIN D = " + min_d );
  System.out.println("MAX D = " + max_d );
  System.out.println("Trying UB formed from: ");
  System.out.println(" a = " + a_dir.length() );
  System.out.println(" b = " + b_dir.length() );
  System.out.println(" c = " + c_dir.length() );
  System.out.println(" alpha = " + angle( b_dir, c_dir ) );
  System.out.println(" beta  = " + angle( c_dir, a_dir ) );
  System.out.println(" gamma = " + angle( a_dir, b_dir ) );
*/
                                   // now build the UB matrix from a,b,c       

  float[][] UB_inv_arr = { { a_dir.getX(), a_dir.getY(), a_dir.getZ(), 0 },
                           { b_dir.getX(), b_dir.getY(), b_dir.getZ(), 0 },
                           { c_dir.getX(), c_dir.getY(), c_dir.getZ(), 0 },
                           { 0,            0,            0,            1 } };

  UB.set( UB_inv_arr );
  UB.invert();

  return true;
}


/**
 * Gradually use more of the specified peaks and re-optimize the UB to index
 * them.
 * @param  UB                  The initial UB matrix that should be refined to 
 *                             index more of the peaks.
 * @param  q_vectors           The list of peaks, preferably in order of 
 *                             increasing magnitude.
 * @param  required_tolerance  The tolerance for considering a peak to be
 *                             indexed.
 * @param  num_initial         The number of peaks that should be used 
 *                             intially when refining UB.
 * @param  average_error       The average fit error is returned in position
 *                             zero of this array, if the number of peaks
 *                             indexed is more than 0;
 * @return The number of peaks indexed by the final refined UB.  If this is
 *         zero, the indexing failed.
 */
public static int ExpandSetOfIndexedPeaks( Tran3D           UB, 
                                           Vector<Vector3D> q_vectors, 
                                           float            required_tolerance,
                                           int              num_initial,
                                           float[]          average_error )
{
  if ( q_vectors.size() < 3 ) 
    throw new IllegalArgumentException("Need at least 3 q_vectors");

  if ( num_initial < 3 )
    num_initial = 3;
 
  Vector<Vector3D> miller_ind = new Vector<Vector3D>();
  Vector<Vector3D> indexed_qs = new Vector<Vector3D>();
  Vector<Vector3D> some_qs    = new Vector<Vector3D>();

  float[] fit_error = new float[1];

  Tran3D  temp_UB = new Tran3D();
  int     num_indexed = 0;
  boolean done = false;
  int     repeat_counter = 0;    // we do several extra refinements with all
                                 // peaks to get the final result.
  while ( !done )
  {
    if ( num_initial > q_vectors.size() )
    {
      num_initial = q_vectors.size();
      repeat_counter++;
      if ( repeat_counter > 3 )
        done = true;
    }

    for ( int i = some_qs.size(); i < num_initial; i++ )
      some_qs.add( q_vectors.elementAt(i) );

    temp_UB.set( UB );
    num_indexed = GetIndexedPeaks( UB, some_qs, required_tolerance,
                                   miller_ind, indexed_qs, fit_error );
    try
    {
      fit_error[0] = Optimize_UB_3D( temp_UB, miller_ind, indexed_qs );

      if ( !Float.isNaN( fit_error[0] ) )
      {
        UB.set( temp_UB );
//      System.out.println("Indexed " + num_indexed + " of " + num_initial );
      }
      else
      {
        System.out.println("Optimize_UB_3D Failed with "+num_initial+" peaks");
        return 0;
      }
     }
     catch ( Exception ex )
     {
       return 0;
       // failed to improve with these peaks, so continue with more peaks
       // if possible 
     }

    num_initial = Math.round(1.5f * num_initial + 2);
                                  // include more peaks the next time through.
                                  // add 2, in case we started with few peaks
  }

  num_indexed = GetIndexedPeaks( UB, some_qs, required_tolerance,
                                 miller_ind, indexed_qs, fit_error );
/*
  average_error[0] = Float.POSITIVE_INFINITY;
  if ( num_indexed > 0 )
  {
    average_error[0] = fit_error[0] / num_indexed;
    System.out.println("Indexed " + num_indexed + 
                       " average fit_error = " + average_error[0] );
  }
*/
  return num_indexed;
}


/**
 *  Try to find a UB that is equivalent to the original UB, but corresponds
 * to a Niggli reduced cell with the smallest sum of edge lengths and 
 * with angles most different from 90 degrees.
 *
 * @param UB      The original UB 
 * @param newUB   Returns the newUB
 *
 * @return True if a possibly constructive change was made and newUB has been
 * set to a new matrix.  It returns false if no constructive change was found
 * and newUB is just set to the original UB.
 */
public static boolean MakeNiggliUB( Tran3D UB, Tran3D newUB )
{
   Vector3D a = new Vector3D();
   Vector3D b = new Vector3D();
   Vector3D c = new Vector3D();

   boolean ok = getABC( UB, a, b, c );
   if ( !ok )
   {
     System.out.println("ERROR COULD NOT GET a,b,c From UB" );
     System.out.println( UB );
     newUB.set( UB );
     return false;
   }

   Vector3D v1 = new Vector3D();
   Vector3D v2 = new Vector3D();
   Vector3D v3 = new Vector3D();
                                  // first make a list of linear combinations
                                  // of vectors a,b,c with coefficient up to 5
   Vector<Vector3D> directions = new Vector<Vector3D>();
   int N_coeff = 5;
   for ( int i = -N_coeff; i <= N_coeff; i++ )
     for ( int j = -N_coeff; j <= N_coeff; j++ )
       for ( int k = -N_coeff; k <= N_coeff; k++ )
         if ( i != 0 || j != 0 || k != 0 )
         {
            v1.set(a); 
            v2.set(b); 
            v3.set(c); 
            v1.multiply( i );
            v2.multiply( j );
            v3.multiply( k );
            v1.add(v2);
            v1.add(v3);
            Vector3D sum = new Vector3D( v1 );
            directions.add( sum );
         }
                                // next sort the list of linear combinations
                                // in order of increasing length
  directions = SortOnVectorMagnitude( directions );

                                // next form a list of possible UB matrices
                                // using sides from the list of linear 
                                // combinations, using shorter directions first.
                                // Keep trying more until 25 UBs are found.
                                // Only keep UBs corresponding to cells with
                                // at least a minimum cell volume
  Vector<Tran3D> UB_list = new Vector<Tran3D>();

  int num_needed = 25;
  int max_to_try = 5;
  while ( UB_list.size() < num_needed && max_to_try < directions.size() )
  {
    max_to_try *= 2;
    int num_to_try = Math.min( max_to_try, directions.size() );

    Vector3D acrossb = new Vector3D();
    float vol     = 0;
    float min_vol = .1f;      // what should this be? 0.1 works OK, but...?
    for ( int i = 0; i < num_to_try-2; i++ )
    {
      a.set( directions.elementAt(i) );
      for ( int j = i+1; j < num_to_try-1; j++ )
      {
        b.set( directions.elementAt(j) );
        acrossb.cross(a,b);
        for ( int k = j+1; k < num_to_try; k++ )
        {
          c.set( directions.elementAt(k) );
          vol = acrossb.dot( c );
          if ( vol > min_vol && hasNiggliAngles(a,b,c) )
          {
            Tran3D new_tran = new Tran3D();
            getUB( new_tran, a, b, c );
            UB_list.add( new_tran );
          }
        }
      }
    }    
  }
                                // if no valid UBs could be formed, return
                                // false and the original UB
  if ( UB_list.size() <= 0 )
  {
    newUB.set( UB );
    return false;
  }
                                // now sort the UB's in order of increasing
                                // total side length |a|+|b|+|c|
  UB_list = SortOn_abc_Magnitude( UB_list );

/*
  int num_to_print = Math.min( 10, UB_list.size() );
  System.out.println("First at most 10 possible Niggli UBs are");
  for ( int i = 0; i < num_to_print; i++ )
    ShowLatticeParameters( UB_list.elementAt(i) );
*/
                                // keep only those UB's with total side length
                                // within .1% of the first one.  This can't
                                // be much larger or "bad" UBs are made for
                                // some tests with 5% noise
  float length_tol = 0.001f;

  Vector<Tran3D> short_list = new Vector<Tran3D>();
  short_list.add( UB_list.elementAt(0) );
  getABC( short_list.elementAt(0), a, b, c );
  float total_length = a.length() + b.length() + c.length();
  boolean got_short_list = false;
  int i = 1;
  while ( i < UB_list.size() && !got_short_list )
  {
    Tran3D nextUB = UB_list.elementAt(i);
    getABC( nextUB, v1, v2, v3 );
    float next_length = v1.length() + v2.length() + v3.length();
    if ( Math.abs(next_length - total_length)/total_length < length_tol )
      short_list.add( UB_list.elementAt(i) );
    else
      got_short_list = true; 
    i++;
  }

   UB_list = short_list;       // now only use this shorter list

                                // sort the UB_list in decreasing order of total
                                // difference of angles from 90 degrees and
                                // return the one with largest difference.
  UB_list = SortOn_abc_DiffFrom90( UB_list );

  newUB.set( UB_list.elementAt(0) );

  return true;
}


/**
 *  Given a UB corresponding to the three shortest a,b,c vectors ( a Brugger
 *  cell ) form a newUB corresponding to a,b,c that satisfy the Niggli cell 
 *  conditions.
 *
 * @param UB      The original UB 
 * @param newUB   Returns the newUB
 *
 * @return true if newUB was set to the UB corresponding to a Niggli cell.
 */
public static boolean ChooseNiggliUB( Tran3D UB, Tran3D newUB )
{
                                         // INCOMPLETE
  newUB.set( UB );

  Tran3D tempUB = new Tran3D( UB );
  tempUB.invert();

  float[][] abc = tempUB.get();
  Vector3D a = new Vector3D( abc[0][0], abc[0][1], abc[0][2] );
  Vector3D b = new Vector3D( abc[1][0], abc[1][1], abc[1][2] );
  Vector3D c = new Vector3D( abc[2][0], abc[2][1], abc[2][2] );

                                              // first, put the three sides
                                              // in order
  Vector<Vector3D> directions = new Vector<Vector3D>();
  directions.add(a);
  directions.add(b);
  directions.add(c);
  directions = SortOnVectorMagnitude( directions );
  a.set( directions.elementAt(0) );
  b.set( directions.elementAt(1) );
  c.set( directions.elementAt(2) );
                                             // the make them right handed
  if ( ! isRightHanded(a,b,c) )
    c.multiply(-1);

  Vector3D[][] abcs = new Vector3D[4][3];    // each "row" is a set of a,b,c
  
  abcs[0][0] = a;
  abcs[0][1] = b;
  abcs[0][2] = c;

  int[][] signs = { {  1,  1,  1 },
                    { -1, -1,  1 },
                    {  1, -1, -1 },
                    { -1,  1, -1 } };

  for ( int row = 1; row < signs.length; row++ )
    for ( int col = 0; col < 3; col++ )
    {
      abcs[row][col] = new Vector3D( abcs[0][col] );
      abcs[row][col].multiply( signs[row][col] );
    }
/*
  for ( int row = 0; row < signs.length; row++ )
    ShowLatticeParameters( abcs[row][0], abcs[row][1], abcs[row][2] );
*/
  for ( int row = 0; row < signs.length; row++ )
    if ( hasNiggliAngles( abcs[row][0], abcs[row][1], abcs[row][2] ) )
    {
      for ( int i = 0; i < 3; i++ )
        abc[ i ] = abcs[row][i].get();
      newUB.set( abc ); 
      newUB.invert();
      return true;
    }

  return false;
}


/**
 *  Given a UB corresponding to the three shortest a,b,c vectors ( a Brugger
 *  cell ), not necessarily strictly in order of increasing side length,
 *  form a newUB corresponding to a,b,c for which all angles, alpha, beta,
 *  gamma are >= 90 degrees, or all are < 90 degrees. 
 *
 * @param UB      The original UB 
 * @param newUB   Returns the newUB
 *
 * @return true if newUB was set to the UB corresponding to a cell with
 *         angles that satisfy the Niggi conditions.
 */
public static boolean ChooseUB_WithNiggliAngles( Tran3D UB, Tran3D newUB )
{
                                         // INCOMPLETE
  newUB.set( UB );

  Vector3D a = new Vector3D();
  Vector3D b = new Vector3D();
  Vector3D c = new Vector3D();
  getABC( UB, a, b, c );
                                             // make them right handed
  if ( ! isRightHanded(a,b,c) )
    c.multiply(-1);

  Vector3D[][] abcs = new Vector3D[4][3];    // each "row" is a set of a,b,c
 
  abcs[0][0] = a;
  abcs[0][1] = b;
  abcs[0][2] = c;

  int[][] signs = { {  1,  1,  1 },
                    { -1, -1,  1 },
                    {  1, -1, -1 },
                    { -1,  1, -1 } };

  for ( int row = 1; row < signs.length; row++ )
    for ( int col = 0; col < 3; col++ )
    {
      abcs[row][col] = new Vector3D( abcs[0][col] );
      abcs[row][col].multiply( signs[row][col] );
    }

  for ( int row = 0; row < abcs.length; row++ )
    if ( hasNiggliAngles( abcs[row][0], abcs[row][1], abcs[row][2] ) )
    {
      getUB( newUB, a, b, c );
      return true;
    }

  return false;
}


/**
 * Get the UB matrix corresponding to the real space edge vectors a,b,c,
 *
 * @return true if the calculation succeeded and UB is set to the UB matrix 
 *              corresponding to a, b, c.  Return false otherwise.
 */
public static boolean getUB( Tran3D UB, Vector3D a, Vector3D b, Vector3D c )
{
  float[][] abc = new float[3][];
  abc[0] = a.get();
  abc[1] = b.get();
  abc[2] = c.get();
 
  Tran3D tempUB = new Tran3D( abc );
  try
  {
    tempUB.invert();
  }
  catch ( Exception ex )
  {
    return false;
  }

  UB.set( tempUB );
  return true;
}


public static float getDiffFrom90_Sum( Tran3D UB )
{
  Vector3D a = new Vector3D();
  Vector3D b = new Vector3D();
  Vector3D c = new Vector3D();

  if ( !getABC( UB, a, b, c ) )
    return -1;

  float alpha = angle( b, c );
  float beta  = angle( c, a );
  float gamma = angle( a, b );

  float sum = Math.abs( alpha - 90f ) +  
              Math.abs( beta  - 90f ) +  
              Math.abs( gamma - 90f );

  return sum;
}


/**
 * Get the real space edge vectors a,b,c corresponding to the given UB.
 *
 * @return true if the calculation succeeded and a, b and c are set to
 *         the corresponding real space vectors.  Return false otherwise.
 */
public static boolean getABC( Tran3D UB, Vector3D a, Vector3D b, Vector3D c )
{
  Tran3D tempUB = new Tran3D( UB );
  try
  {
    tempUB.invert();
  }
  catch ( Exception ex )
  {
    return false;
  }

  float[][] abc = tempUB.get();
  a.set( abc[0][0], abc[0][1], abc[0][2] );
  b.set( abc[1][0], abc[1][1], abc[1][2] );
  c.set( abc[2][0], abc[2][1], abc[2][2] );

  return true;
}


public static boolean hasNiggliAngles( Vector3D a, Vector3D b, Vector3D c )
{
  float alpha = angle( b, c ); 
  float beta  = angle( c, a ); 
  float gamma = angle( a, b ); 
  float eps   = 0.01f;          // Some tolerance needed or sometimes a Niggli
                                // cell is missed in the tests.

  if ( alpha < 90+eps && beta < 90+eps && gamma < 90+eps )
    return true;

  if ( alpha >= 90-eps && beta >= 90-eps && gamma >= 90-eps )
    return true;

  return false;
}


public static boolean RuthNigglify( Tran3D UB )
{
  try
  {
    float[][] floatUB_4 = UB.get();
    float[][] floatUB = new float[3][3];
    for ( int i = 0; i < 3; i++ )
      for ( int j = 0; j < 3; j++ )
        floatUB[i][j] = floatUB_4[i][j];
    floatUB = subs.OLD_Nigglify( floatUB );
    UB.set( floatUB );
  }
  catch ( Exception ex )
  {
    System.out.println("Exception in subs.Nigglify " );
    System.out.println(ex);
    ex.printStackTrace();
    return false;
  }
  return true;
}


public static boolean NewNigglify( Tran3D UB )
{
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
    return false;
  }
  return true;
}


/**
 * Alter the vectors a, b, c if needed so that a is the shortest, b the
 * next shortest and c the longest of the three vectors, a, b, c.
 */
static public void sort_abc( Vector3D a_vec, Vector3D b_vec, Vector3D c_vec )
{
  Vector3D temp;
  Vector3D a = new Vector3D( a_vec );
  Vector3D b = new Vector3D( b_vec );
  Vector3D c = new Vector3D( c_vec );

  int shortest = 0;
  Vector3D[] list = { a, b, c };                // get shortest one position 0
  for ( int i = 1; i < 3; i++ )
    if ( list[i].length() < list[0].length() )
      shortest = i;

  if ( shortest != 0 )
  {
    temp           = list[0];
    list[0]        = list[shortest];
    list[shortest] = temp;
  }

  if ( list[1].length() > list[2].length() )  // get longest one in position 2
  {
    temp = list[1];
    list[1] = list[2];
    list[2] = temp;
  }

  a_vec.set( list[0] ); 
  b_vec.set( list[1] ); 
  c_vec.set( list[2] ); 
}


/**
 * Sort the specified list of Vector3D objects in order of increasing magnitude
 * and return a new Vector containing references to the original vectors, but in
 * order of increasing magnitude.
 *
 * @param  q_vectors  a Vector of Vector3D obejcts that are to be sorted
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
      *  Construct a comparator to sort a list of Vector3Ds in increasing or
      *  decreasing order based on |vector|.
      *
      *  @param  decreasing  Set true to sort from largest to smallest;
      *                      set false to sort from smallest to largest.
      */
     public Vector3D_MagnitudeComparator( boolean decreasing )
     {
       this.decreasing = decreasing;
     }

     /**
       *  Compare two Vector3D objects based on their magnitude.
       *
       *  @param  peak_1   The first  peak
       *  @param  peak_2   The second peak 
       *
       *  @return A positive integer if peak_1's magnitude is greater than 
       *          peak_2's magnitude
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
 * Sort the specified list of UB matrices in order of increasing total length
 * of the corresponding real space a, b, c vectors and return a new Vector 
 * containing references to the original matrices, but in order of increasing 
 * |a|+|b|+|c|..
 *
 * @param  UB_matrices  a Vector of Tran3D objects that are to be sorted
 * @return A new Vector containing references to the orginal Tran3D 
 *          objects, but in increasing order.
 */
public static Vector<Tran3D>
         SortOn_abc_Magnitude( Vector<Tran3D> UB_matrices )
{
  Tran3D list[] = new Tran3D[ UB_matrices.size() ];
  for ( int i = 0; i < list.length; i++ )
    list[i] = UB_matrices.elementAt(i);

  boolean decreasing = false;
  Arrays.sort( list, new Tran3D_ABC_Comparator(decreasing) );

  Vector<Tran3D> sorted_UBs = new Vector<Tran3D>( list.length );
  for ( int i = 0; i < list.length; i++ )
    sorted_UBs.add( list[i] );

  return sorted_UBs;
}


   private static class Tran3D_ABC_Comparator implements Comparator
   {
     boolean decreasing;

     /**
      *  Construct a comparator to sort a list of Tran3Ds in increasing or
      *  decreasing order based on |a|+|b|+|c|.
      *
      *  @param  decreasing  Set true to sort from largest to smallest;
      *                      set false to sort from smallest to largest.
      */
     public Tran3D_ABC_Comparator( boolean decreasing )
     {
       this.decreasing = decreasing;
     }

     /**
       *  Compare two Tran3Ds based on the sum |a|+|b|+|c|.
       *
       *  @param  UB_1   The first  UB
       *  @param  UB_2   The second UB 
       *
       *  @return A positive integer if UB_1's |a|+|b|+|c| is greater than 
       *          UB_2's |a|+|b|+|c|. 
       */
       public int compare( Object UB_1, Object UB_2 )
       {
         Vector3D a1 = new Vector3D();
         Vector3D b1 = new Vector3D();
         Vector3D c1 = new Vector3D();

         Vector3D a2 = new Vector3D();
         Vector3D b2 = new Vector3D();
         Vector3D c2 = new Vector3D();

         getABC( (Tran3D)UB_1, a1, b1, c1 );
         getABC( (Tran3D)UB_2, a2, b2, c2 );

         float sum_1 = a1.length() + b1.length() + c1.length();
         float sum_2 = a2.length() + b2.length() + c2.length();

         if ( decreasing )
         {
           if ( sum_1 < sum_2 )
             return 1;
           else if  ( sum_1 > sum_2 )
             return -1;
         }
         else
         {
           if ( sum_1 < sum_2 )
             return -1;
           else if  ( sum_1 > sum_2 )
             return 1;
         }

         return 0;
       }
   }


/**
 * Sort the specified list of UB matrices in order of increasing total length
 * of the corresponding real space a, b, c vectors and return a new Vector 
 * containing references to the original matrices, but in order of increasing 
 * |a|+|b|+|c|..
 *
 * @param  UB_matrices  a Vector of Tran3D objects that are to be sorted
 * @return A new Vector containing references to the orginal Tran3D 
 *          objects, but in increasing order.
 */
public static Vector<Tran3D> SortOn_abc_DiffFrom90( Vector<Tran3D> UB_matrices )
{
  Tran3D list[] = new Tran3D[ UB_matrices.size() ];
  for ( int i = 0; i < list.length; i++ )
    list[i] = UB_matrices.elementAt(i);

  boolean decreasing = true;
  Arrays.sort( list, new Tran3D_DiffFrom90_Comparator(decreasing) );

  Vector<Tran3D> sorted_UBs = new Vector<Tran3D>( list.length );
  for ( int i = 0; i < list.length; i++ )
    sorted_UBs.add( list[i] );

  return sorted_UBs;
}


   private static class Tran3D_DiffFrom90_Comparator implements Comparator
   {
     boolean decreasing;

     /**
      *  Construct a comparator to sort a list of Tran3Ds in increasing or
      *  decreasing order based on |alpha-90| + |beta-90| + |gamma-90|.
      *
      *  @param  decreasing  Set true to sort from largest to smallest;
      *                      set false to sort from smallest to largest.
      */
     public Tran3D_DiffFrom90_Comparator( boolean decreasing )
     {
       this.decreasing = decreasing;
     }

     /**
       *  Compare two Tran3Ds based on the sum |a|+|b|+|c|.
       *
       *  @param  UB_1   The first  UB
       *  @param  UB_2   The second UB 
       *
       *  @return A positive integer if UB_1's |a|+|b|+|c| is greater than 
       *          UB_2's |a|+|b|+|c|. 
       */
       public int compare( Object UB_1, Object UB_2 )
       {
         float sum_1 = getDiffFrom90_Sum( (Tran3D)UB_1 );
         float sum_2 = getDiffFrom90_Sum( (Tran3D)UB_2 );

         if ( decreasing )
         {
           if ( sum_1 < sum_2 )
             return 1;
           else if  ( sum_1 > sum_2 )
             return -1;
         }
         else
         {
           if ( sum_1 < sum_2 )
             return -1;
           else if  ( sum_1 > sum_2 )
             return 1;
         }

         return 0;
       }
   }


/**
 * Optimize the specified UB matrix by repeatedly using it to index 
 * the given peaks within the specified tolerance, then solving
 * the resulting over-determined system of equations to obtain 
 * a new UB matrix.
 *
 * @param UB        This must be initially set to a UB matrix that indexes
 *                  the given peaks fairly well.  On return, if the
 *                  optimization was successful, it will be set to an
 *                  optimized version of the original UB.
 * @param q_vectors List of q_vectors that should be indexed by the 
 *                  given UB.
 * @param tolerance Maximum allowed offset in h,k,l for a peak to count
 *                  as indexed.
 * @return The remaining error in the indexing of the peaks, or zero if
 *         the optimization failed.
 */
public static float Optimize_UB( Tran3D           UB, 
                                 Vector<Vector3D> q_vectors, 
                                 float            tolerance )
{
   Tran3D temp_UB = new Tran3D( UB );

   float[] fit_error = new float[1];
   Vector<Vector3D> miller_ind  = new Vector<Vector3D>();
   Vector<Vector3D> indexed_qs  = new Vector<Vector3D>();
   for ( int i = 0; i < 5; i++ )
   {
     int num_indexed = GetIndexedPeaks( UB, q_vectors, tolerance,
                                    miller_ind, indexed_qs, fit_error );
     try
     {
       fit_error[0] = Optimize_UB_3D( temp_UB, miller_ind, indexed_qs );

       if ( !Float.isNaN( fit_error[0] ) )
       {
         UB.set( temp_UB );
       }
       else
       {
//       System.out.println("Optimize_UB_3D Failed with "+q_vectors.size()+" peaks");
         return 0;
       }
     }
     catch ( Exception ex )
     {
       return 0;
        // failed to improve with these peaks, so continue with more peaks
        // if possible 
     }
  }
  return fit_error[0];
}


/**
 * Optimize the specified UB matrix by repeatedly using it to index 
 * the given peaks within the specified tolerance, then solving
 * the resulting over-determined system of equations to obtain 
 * a new UB matrix, using gradually expanding spherical shell regions
 * in Q.
 * @param UB        This must be initially set to a UB matrix that indexes
 *                  the given peaks fairly well.  On return, if the
 *                  optimization was successful, it will be set to an
 *                  optimized version of the original UB.
 * @param q_vectors List of q_vectors that should be indexed by the 
 *                  given UB.
 * @param tolerance Maximum allowed offset in h,k,l for a peak to count
 *                  as indexed.
 * @return The remaining error in the indexing of the peaks, or zero if
 *         the optimization failed.
 */
public static float Optimize_UB_ByGrowingShells( Tran3D           UB,
                                                 Vector<Vector3D> q_vectors, 
                                                 float            tolerance )
{
   q_vectors = SortOnVectorMagnitude( q_vectors );
   Tran3D temp_UB = new Tran3D( UB );

   float[] fit_error = new float[1];
   Vector<Vector3D> miller_ind  = new Vector<Vector3D>();
   Vector<Vector3D> indexed_qs  = new Vector<Vector3D>();

   int first_index = 0;
   int last_index;

   for ( int count = 0; count < 5; count++ )
   {
     int num_indexed = GetIndexedPeaks( UB, q_vectors, tolerance,
                                        miller_ind, indexed_qs, fit_error );

     if ( num_indexed < 5 )        // not enough indexed peaks to optimize
     {
       System.out.println("Too few peaks indexed to optimize:" + num_indexed );
       return 0;
     }

     int num_to_use = num_indexed/4;
     if ( num_to_use < 5 )
       num_to_use = 5;

     Vector<Vector3D> used_miller_ind  = new Vector<Vector3D>();
     Vector<Vector3D> used_indexed_qs  = new Vector<Vector3D>();

     if ( first_index + num_to_use >= num_indexed )
       first_index = num_indexed - num_to_use;

     last_index  = first_index + num_to_use - 1;

     for ( int i = first_index; i <= last_index; i++ )
     {
       used_miller_ind.add( miller_ind.elementAt(i) );
       used_indexed_qs.add( indexed_qs.elementAt(i) );
     }

     try
     {
       fit_error[0] = Optimize_UB_3D(temp_UB, used_miller_ind, used_indexed_qs);

       if ( !Float.isNaN( fit_error[0] ) )
       {
         UB.set( temp_UB );
       }
       else
       {
//       System.out.println("Optimize_UB_3D Failed with "
//                           + q_vectors.size() + " peaks");
         return 0;
       }
     }
     catch ( Exception ex )
     {
       return 0;
        // failed to improve with these peaks, so continue with more peaks
        // if possible 
     }
  }
  return fit_error[0];
}



/** 
    STATIC method Optimize_UB: Calculates the matrix that most nearly maps
    the specified hkl_vectors to the specified q_vectors.  The calculated
    UB minimizes the sum squared differences between UB*(h,k,l) and the
    corresponding (qx,qy,qz) for all of the specified hkl and Q vectors.
    The sum of the squares of the residual errors is returned.  This method is
    used to optimize the UB matrix once an initial indexing has been found.
  
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
                         is 1/d.  This will only be changed 
                         if the returned float value is NOT NaN.
    @param  index_values Vector of ints that contains the list of indices 
    @param  q_vectors    Vector of new Vector3D objects that contains the list  
                         of q_vectors that are indexed in one direction by the 
                         corresponding index values.
    NOTE: The number of index_values and q_vectors must be the same, and must
          be at least 3.
  
    @return  This will return the sum of the squares of the residual errors,
             or Float.NaN if the optimization failed.
  
    @throws  IllegalArgumentException if there are not at least 3
                                   indices and q vectors, or if the numbers of
                                   indices and q vectors are not the same.
   
    @throws  IllegalArgumentException if the QR factorization fails or
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

  float error = Float.NaN;
  try
  {
    double[][] Q = LinearAlgebra.QR_factorization( H_transpose );

    double[] b = new double[ index_values.size() ];
    for ( int i = 0; i < index_values.size(); i++ )
      b[i] = (Integer)( index_values.elementAt(i) ); 

    error = (float)LinearAlgebra.QR_solve( H_transpose, Q, b );

                                      // only change vector if solve worked
    if ( ! Float.isNaN(error) )           
    {
      Vector3D temp_vec = new Vector3D((float)b[0], (float)b[1], (float)b[2]);
      if ( temp_vec.length() == 0 )
      {
        System.out.println("Optimized Direction Was (0,0,0)" );
        error = Float.NaN;
      }
      else
        best_vec.set( temp_vec );
    }
  }
  catch ( Exception ex )
  {
    // if optimization fails, just don't change anything
  }
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
   Calculate the number of Q vectors for which the dot product with the 
   specified direction vector is an integer to within the specified
   tolerance.  This give the number of peaks that would be indexed in the
   given direction, if it were used as one of the unit cell edge vectors,
   a,b,c.
  
   @param direction    A Vector3D representing a possible unit cell edge
                       vector, a, b, c.
   @param q_vectors    Vector of Vector3D objects that contains the list of 
                       q_vectors that are indexed by the corresponding hkl
                       vectors.
   @param tolerance    The maximum allowed distance to an integer from the dot
                       products of peaks with the specified direction.

   @return A non-negative integer giving the number of peaks indexed in one
           direction, by the specified vector. 
 */
public static int NumberIndexed_1D( Vector3D         direction,
                                    Vector<Vector3D> q_vectors,
                                    float            tolerance )
{
  if ( direction.length() == 0 )
    return 0;

  float proj_value;
  float error;
  int   nearest_int;
  int   count = 0;
  for ( int i = 0; i < q_vectors.size(); i++ )
  {
    proj_value  = direction.dot( q_vectors.elementAt(i) );
    nearest_int = Math.round( proj_value );
    error = Math.abs( proj_value - nearest_int );
    if ( error < tolerance )
      count++;
  }
  return count;
}


/**
   Calculate the number of Q vectors for which the dot product with three
   specified direction vectors is an integer triple, NOT equal to (0,0,0) to
   within the specified tolerance.  This give the number of peaks that would 
   be indexed by the UB matrix formed from the specified those three real 
   space unit cell edge vectors.
   NOTE: This method assumes that the three edge vectors are linearly 
         independent and could be used to form a valid UB matrix.
  
   @param a_dir        A Vector3D representing unit cell edge vector a
   @param b_dir        A Vector3D representing unit cell edge vector b
   @param c_dir        A Vector3D representing unit cell edge vector c
   @param q_vectors    Vector of Vector3D objects that contains the list of 
                       q_vectors that are indexed by the corresponding hkl
                       vectors.
   @param tolerance    The maximum allowed distance to an integer from the dot
                       products of peaks with the specified direction.
   @param ave_2_error  The average sum_squared_error for the indexed peaks
                       is returned in the zeroth position of this array, if
                       any peaks were indexed.

   @return A non-negative integer giving the number of peaks simultaneously
           indexed in all three directions by the specified direction vectors. 
 */
public static int NumberIndexed_3D( Vector3D         a_dir,
                                    Vector3D         b_dir,
                                    Vector3D         c_dir,
                                    Vector<Vector3D> q_vectors,
                                    float            tolerance,
                                    float[]          ave_2_error )
{
  if ( a_dir.length() == 0 || b_dir.length() == 0 || c_dir.length() == 0 )
    return 0;

  Vector3D hkl_vec = new Vector3D();
  int     count    = 0;
  float[] hkl      = new float[3];
  float   error_2  = 0;
  float   dh, dk, dl;

  for ( int i = 0; i < q_vectors.size(); i++ )
  {
    hkl[0] = a_dir.dot( q_vectors.elementAt(i) );
    hkl[1] = b_dir.dot( q_vectors.elementAt(i) );
    hkl[2] = c_dir.dot( q_vectors.elementAt(i) );
    hkl_vec.set( hkl );
    if ( ValidIndex( hkl_vec, tolerance ) )
    {
      count++;
      dh = hkl[0] - Math.round( hkl[0] );
      dk = hkl[1] - Math.round( hkl[1] );
      dl = hkl[2] - Math.round( hkl[2] );
      error_2 += dh * dh + dk * dk + dl * dl;    
    }
  }

  if ( count > 0 )
    ave_2_error[0] = error_2 / count;
  else
    ave_2_error[0] = Float.POSITIVE_INFINITY;

  return count;
}


/**
   Calculate the number of Q vectors that are mapped to integer h,k,l 
   values by UB.  Each of the Miller indexes, h, k and l must be within
   the specified tolerance of an integer, in order to count the peak
   as indexed.  Also, if (h,k,l) = (0,0,0) the peak will NOT be counted
   as indexed, since (0,0,0) is not a valid index of any peak.
  
   @param UB           A 3x3 matrix of doubles holding the UB matrix.
                       The UB matrix must not be singular.
   @param q_vectors    Vector of Vector3D objects that contains the list of 
                       q_vectors that are indexed by the corresponding hkl
                       vectors.
   @param tolerance    The maximum allowed distance between each component
                       of UB^-1*Q and the nearest integer value, required to
                       to count the peak as indexed by UB.
   @return A non-negative integer giving the number of peaks indexed by UB. 
   @throws IllegalArgumentException if the UB matrix appears to be singular.
 */
public static int NumberIndexed( Tran3D UB,
                                 Vector q_vectors,
                                 float tolerance )
{
  if ( !CheckUB(UB) )
    throw new IllegalArgumentException("UB not valid orientation matrix ");

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
   Calculate the estimated standard deviation between miller indices that 
   would be assigned using UB-inverse and the fractional miller indices
   (UB-inverse*Q) for peaks that index with valid Miller indices within the
   specified tolerance.
  
   @param UB           A 3x3 matrix of doubles holding the UB matrix.
                       The UB matrix must not be singular.
   @param q_vectors    Vector of Vector3D objects that contains the list of 
                       q_vectors that are indexed by the corresponding hkl
                       vectors.
   @param tolerance    The maximum allowed distance between each component
                       of UB^-1*Q and the nearest integer value, required to
                       to count the peak as indexed by UB.
   @return The  
   @throws IllegalArgumentException if the UB matrix appears to be singular.
 */
public static float IndexingStdDev( Tran3D UB,
                                    Vector q_vectors,
                                    float tolerance )
{
  if ( !CheckUB(UB) )
    throw new IllegalArgumentException("UB not valid orientation matrix ");

  Tran3D UB_inverse = new Tran3D( UB );
  UB_inverse.invert();

  Vector3D hkl = new Vector3D();
  int   count = 0;
  float sum_sq_err = 0;
  float err;
  for ( int i = 0; i < q_vectors.size(); i++ )
  {
    UB_inverse.apply_to( (Vector3D)(q_vectors.elementAt(i)), hkl );
    if ( ValidIndex( hkl, tolerance ) )
    {
      count++;
      err = Math.round(hkl.getX()) - hkl.getX();
      sum_sq_err += err*err;
      err = Math.round(hkl.getY()) - hkl.getY();
      sum_sq_err += err*err;
      err = Math.round(hkl.getZ()) - hkl.getZ();
      sum_sq_err += err*err;
    }
  }

  float std_dev = 0;
  if ( count > 0 )
    std_dev = (float)Math.sqrt( sum_sq_err / (3 * count) );

  return std_dev;
}


/**
  Check whether or not the specified matrix is reasonable for an orientation
  matrix.  In particular, check that it is without any nan or infinite values
  and that its determinant is within a reasonable range, for an 
  orientation matrix.

  @param UB  A Tran3D object holding the UB matrix

  @return true if this could be a valid UB matrix. 
 */

public static boolean CheckUB( Tran3D UB )
{
  float[][] f_vals = UB.get();

  for ( int row = 0; row < 3; row++ )
    for ( int col = 0; col < 3; col++ )
    {
      if ( Float.isNaN( f_vals[row][col]) )
        return false;

      if ( Float.isInfinite( f_vals[row][col]) )
        return false;
    }
                                         // use double precision for
                                         // calculating the determinant
  double[][] val  = new double[3][3];
  for ( int row = 0; row < 3; row++ )
    for ( int col = 0; col < 3; col++ )
       val[row][col] = f_vals[row][col];

  double det =   val[0][0] * ( val[1][1] * val[2][2] - val[1][2] * val[2][1] )
               - val[0][1] * ( val[1][0] * val[2][2] - val[1][2] * val[2][0] )
               + val[0][2] * ( val[1][0] * val[2][1] - val[1][1] * val[2][0] );

  double abs_det = Math.abs(det);
  if ( abs_det > 10 || abs_det < 1e-12 ) // UB not found correctly
    return false;

  return true;
}

private static void ShowAllUB_INFO( Tran3D           UB,
                                    Vector<Vector3D> q_vectors,
                                    float            tolerance )
{

  Vector3D a = new Vector3D();
  Vector3D b = new Vector3D();
  Vector3D c = new Vector3D();
  getABC( UB, a, b, c );

  float alpha = angle(b,c);
  float beta  = angle(c,a);
  float gamma = angle(a,b);

  float tot_length = a.length() + b.length() + c.length();
  double tot_diff  = Math.abs( 90.0f - alpha ) + 
                     Math.abs( 90.0f - beta  ) +
                     Math.abs( 90.0f - gamma );

  alpha *= (float)( Math.PI/180.0);
  beta  *= (float)( Math.PI/180.0);
  gamma *= (float)( Math.PI/180.0);

  double tot_cos = Math.abs( Math.cos(alpha) ) +
                   Math.abs( Math.cos(beta)  ) +
                   Math.abs( Math.cos(gamma) );

  double tot_prod = Math.abs( Math.cos(alpha) ) * 
                    Math.abs( Math.cos(beta)  ) *
                    Math.abs( Math.cos(gamma) );

  int   num_indexed = NumberIndexed( UB, q_vectors, tolerance );
  float error       = IndexingStdDev( UB, q_vectors, tolerance );

  float[][]  ub_arr = UB.get();
  double[][] ub_mat = new double[3][3];
  for ( int i = 0; i < 3; i++ )
    for ( int j = 0; j < 3; j++ )
      ub_mat[i][j] = ub_arr[i][j];

//  System.out.println("UB = " + UB );
  double[] l_par = lattice_calc.LatticeParamsOfUB( ub_mat );
  if ( l_par != null )
  {
    System.out.printf("%8.4f %8.4f %8.4f  %8.4f %8.4f %8.4f  %8.4f",
        l_par[0], l_par[1], l_par[2], l_par[3], l_par[4], l_par[5], l_par[6] );

    System.out.print(" RH = " + isRightHanded( UB ) );

    System.out.printf( "   %8.4f  %8.4f  %8.6f  %8.6f  %4d  %9.7f\n",
             tot_length, tot_diff, tot_cos, tot_prod, num_indexed, error );
  }
  else
    System.out.println("l_par NULL when showing lattice parameters for matrix:"
                       + UB );
}


public static float[] getLatticeParameters( Tran3D UB )
{
  Vector3D a = new Vector3D();
  Vector3D b = new Vector3D();
  Vector3D c = new Vector3D();
  getABC( UB, a, b, c );

  Vector3D cross = new Vector3D();
  cross.cross( a, b );
  float vol = cross.dot( c );

  float[] lat_par = { a.length(), b.length(), c.length(),
                      angle(b,c), angle(c,a), angle(a,b),
                      vol };
  return lat_par;
 }


public static void ShowLatticeParameters( Tran3D UB )
{
  float[][]  ub_arr = UB.get();
  double[][] ub_mat = new double[3][3];
  for ( int i = 0; i < 3; i++ )
    for ( int j = 0; j < 3; j++ )
      ub_mat[i][j] = ub_arr[i][j];

//  System.out.println("UB = " + UB );
  double[] l_par = lattice_calc.LatticeParamsOfUB( ub_mat );
  if ( l_par != null )
  {
    System.out.printf("%8.4f %8.4f %8.4f  %8.4f %8.4f %8.4f  %8.4f",
        l_par[0], l_par[1], l_par[2], l_par[3], l_par[4], l_par[5], l_par[6] );

    System.out.println(" RH = " + isRightHanded( UB ) );
  }
  else
    System.out.println("l_par NULL when showing lattice parameters for matrix:"
                       + UB );
}


private static void ShowLatticeParametersDiffer( Tran3D UB_1, Tran3D UB_2 )
{
  Vector3D a = new Vector3D();
  Vector3D b = new Vector3D();
  Vector3D c = new Vector3D();
  getABC( UB_1, a, b, c );
  float[] LatPar_1 = { a.length(), b.length(), c.length(),
                       angle(b,c), angle(c,a), angle(a,b) };

  getABC( UB_2, a, b, c );
  float[] LatPar_2 = { a.length(), b.length(), c.length(),
                       angle(b,c), angle(c,a), angle(a,b) };

  for ( int i = 0; i < LatPar_1.length; i++ )
    if ( Math.abs((LatPar_1[i] - LatPar_2[i]) / LatPar_1[i]) > .01 )
    {
      System.out.println("RESULT: LAT_PAR_DIFFERENT");
      return;
    }
}



private static void ShowLatticeParameters( Vector3D a, Vector3D b, Vector3D c )
{
  float[] l_par = new float[7];
  l_par[0] = a.length();
  l_par[1] = b.length();
  l_par[2] = c.length();
  l_par[3] = angle(b,c);
  l_par[4] = angle(c,a);
  l_par[5] = angle(a,b);
  
  Vector3D acrossb = new Vector3D();
  acrossb.cross( a, b );

  l_par[6] = Math.abs( acrossb.dot( c ) );

  System.out.printf("Nigg:           "+
                    " %8.4f %8.4f %8.4f  %8.4f %8.4f %8.4f  %8.4f",
        l_par[0], l_par[1], l_par[2], l_par[3], l_par[4], l_par[5], l_par[6] );

  System.out.println(" RH = " + isRightHanded( a, b, c ) );
}


private static boolean isRightHanded( Vector3D a, Vector3D b, Vector3D c )
{
  Vector3D acrossb = new Vector3D();
  acrossb.cross( a, b );
  if ( acrossb.dot( c ) > 0 )
    return true;
  else
    return false;
}


public static boolean isRightHanded( Tran3D UB )
{
  Tran3D tempUB = new Tran3D( UB );
  tempUB.invert();

  float[][] abc = tempUB.get();
  Vector3D a = new Vector3D( abc[0][0], abc[0][1], abc[0][2] );
  Vector3D b = new Vector3D( abc[1][0], abc[1][1], abc[1][2] );
  Vector3D c = new Vector3D( abc[2][0], abc[2][1], abc[2][2] );

  return isRightHanded( a, b, c );
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

  if ( direction.length() == 0 )    // special case, zero vector will NOT
    return 0;                       // index any peaks, even though dot product
                                    // with Q vectors is always an integer!

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
                             that were indexed within the specified tolerance.
  @param indexed_qs          List of Qxyz value for the peaks that were
                             indexed within the specified tolerance.
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
  Given a list of peak objects, get the list of Miller indices and 
  corresponding peak positions for the peaks that currently have
  valid Miller indices to within the specified tolerance.

  @param peaks               List of peak objects 
  @param required_tolerance  The maximum allowed error (as a faction of
                             the corresponding Miller index) for a peak
                             q_vector to be counted as indexed.
  @param miller_indices      List of the Miller indices (h,k,l) of peaks
                             that were indexed within the specified tolerance.
  @param indexed_qs          List of Unrotated Qxyz values for the peaks that
                             were indexed within the specified tolerance..

  @return The number peaks that are indexed to within the specified tolerance.
 */
public static int GetIndexedPeaks( Vector<Peak_new> peaks, 
                                   float            required_tolerance,
                                   Vector           miller_indices,
                                   Vector           indexed_qs )
{
    float    error;
    int      num_indexed = 0;

    miller_indices.clear();
    indexed_qs.clear();
    Peak_new peak;
    Vector3D hkl = new Vector3D();
    for ( int peak_num = 0; peak_num < peaks.size(); peak_num++ )
    {
      peak = peaks.elementAt( peak_num ); 
      hkl.set( peak.h(), peak.k(), peak.l() );

      if ( ValidIndex( hkl, required_tolerance ) )
      {
        indexed_qs.add( new Vector3D( peak.getUnrotQ() ) );

        Vector3D miller_ind = new Vector3D( Math.round(hkl.getX()),
                                            Math.round(hkl.getY()),
                                            Math.round(hkl.getZ()) );
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
 *  will always return precisely one UB matrix for which the a,b,c minimize
 *  the sum-squared error.  If several directions and orientations produce 
 *  the same  minimum, this will return the first one that was encountered 
 *  during the search through directions.  NOTE: This is an expensive 
 *  calculation if the resolution of the vectors searched is less than 
 *  around 2 degrees per step tested.  This method should be most useful 
 *  if there are a small number of peaks, roughly 2-10 AND all peaks belong 
 *  to the same crystallite.
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
 */

public static float ScanFor_UB( Tran3D   UB,
                                Vector   q_vectors,
                                float    a,     float b,    float c,
                                float    alpha, float beta, float gamma,
                                float    degrees_per_step )
{
  Vector3D a_dir = new Vector3D();
  Vector3D b_dir = new Vector3D();
  Vector3D c_dir = new Vector3D();

  float error = ScanFor_UB( a_dir, b_dir, c_dir,
                            q_vectors,
                            a, b, c, alpha, beta, gamma,
                            degrees_per_step );

  float[][] UB_inv_arr = { { a_dir.getX(), a_dir.getY(), a_dir.getZ(), 0 },
                           { b_dir.getX(), b_dir.getY(), b_dir.getZ(), 0 },
                           { c_dir.getX(), c_dir.getY(), c_dir.getZ(), 0 },
                           { 0,            0,            0,            1 } };
  UB.set( UB_inv_arr );
  UB.invert();

  return error;
}




/**
 *  This method very simply scans across all possible directions and 
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


/**
 *  Fill an array with the magnitude of the FFT of the 
 *  projections of the specified q_vectors on the specified direction.
 *  The largest value in the magnitude FFT that occurs at index 5 or more
 *  is returned as the value of the function.
 *
 *  @param FFT           The object containing pre-calculated values needed to
 *                       find the FFT of a sequence with the same number of 
 *                       entries as the projections array.
 *  @param q_vectors     The list of Q vectors to project on the specified 
 *                       direction.
 *  @param current_dir   The direction the Q vectors will be projected on.
 *  @param projections   Array to hold the projections of the Q vectors.  This
 *                       must be long enough so that all projected values map
 *                       map to a valid index, after they are multiplied by the
 *                       index_factor.
 *  @param index_factor  Factor that when multiplied by a projected Q vector 
 *                       will give a valid index into the projections array.
 *  @param magnitude_fft Array that will be filled out with the magnitude of
 *                       the FFT of the projections.
 *  @return The largest value in the magnitude_fft, that is stored in position
 *                      5 or more.
 */
static float GetMagFFT( RealFloatFFT_Radix2 FFT, 
                        Vector<Vector3D>    q_vectors,
                        Vector3D            current_dir, 
                        float[]             projections, 
                        float               index_factor,
                        float[]             magnitude_fft )
{
    Arrays.fill( projections, 0.0f );
                                                      // project onto direction
    Vector3D q_vec;
    float    dot_prod;
    int      index;
    int      n = projections.length;
    for ( int q_num = 0; q_num < q_vectors.size(); q_num++ )
    {
      q_vec = (Vector3D)(q_vectors.elementAt( q_num ));
      dot_prod = current_dir.dot( q_vec );
      index = (int)Math.abs((index_factor * dot_prod));
      if ( index < n )
        projections[ index ] += 1;
      else
        projections[ n-1 ] += 1;    // this should not happen except due to 
                                    // round off error
    }
                                                      // get the |FFT|
    FFT.transform( projections, 0, 1 );
    for ( int i = 1; i < magnitude_fft.length; i++ )
      magnitude_fft[i] =
        (float) Math.sqrt( projections[i]   * projections[i] +
                           projections[n-i] * projections[n-i] );

    magnitude_fft[0] = Math.abs( projections[0] );

    int   dc_end      = 5;        // we need a better estimate of this
    float max_mag_fft = 0f;
    for ( int i = dc_end; i < magnitude_fft.length; i++ )
      if ( magnitude_fft[i] > max_mag_fft )
        max_mag_fft = magnitude_fft[i];

   return max_mag_fft;
}


/**
 * Scan the FFT array for the first maximum that exceeds
 * the specified threshold and is beyond the initial DC term/interval.
 * @param magnitude_fft   The array containing the magnitude of the 
 *                        FFT values.
 * @param threshold       The required threshold for the first peak.  This
 *                        must be positive.
 * @return The centroid (index) where the first maximum occurs, or -1
 *         if no point in the FFT (beyond the DC term) equals or exceeds 
 *         the required threshold.
 */
static float GetFirstMaxIndex( float[] magnitude_fft, float threshold )
{
                                     // find first local min below threshold
  int     N = magnitude_fft.length;
  int     i = 2;
  boolean found_min = false;
  while ( i < N-1 && !found_min )
  {
    float val = magnitude_fft[i];
    if ( val <  threshold          &&
         val <= magnitude_fft[i-1] && 
         val <= magnitude_fft[i+1]  )
      found_min = true;
    i++;
  }
  
  if ( !found_min )
    return -1;
                                     // find next local max above threshold
  boolean found_max = false;
  while ( i < N-1 && !found_max )
  {
    float val = magnitude_fft[i];
    if ( val >= threshold          &&     
         val >= magnitude_fft[i-1] &&
         val >= magnitude_fft[i+1]  )
      found_max = true;
    else
      i++;
  }

  if ( found_max )                  // find centroid of peak in FFT using
  {                                 // one or two bins on either side of max
    float sum   = 0;
    float w_sum = 0;

    int offset = 2; 
    if ( i == N-2 )
      offset = 1;

    for ( int j = i-offset; j <= i+offset; j++ )
    {
      sum   += j * magnitude_fft[j];
      w_sum += magnitude_fft[j];
    }
    return sum / w_sum;
  }
  else
    return -1;
}


/**
   Get list of possible edge vectors for the real space unit cell.  This list
   will consist of vectors, V, for which V dot Q is essentially an integer for
   the most Q vectors.  The difference between V dot Q and an integer must be
   less than the required tolerance for it to count as an integer.  This
   method uses the FFT of projections to find the "interesting" directions.
    @param  directions          Vector that will be filled with the directions
                                that may correspond to unit cell edges.
    @param  q_vectors           Vector of new Vector3D objects that contains 
                                the list of q_vectors that are to be indexed.
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
    @param  degrees_per_step    The number of degrees between directions that
                                are checked while scanning for an initial 
                                indexing of the peaks with lowest |Q|.
    @return The number of peaks indexed by each of the directions.
 */

public static int FFTScanFor_Directions( Vector<Vector3D> directions,
                                         Vector   q_vectors,
                                         float    min_d,
                                         float    max_d,
                                         float    required_tolerance,
                                         float    degrees_per_step )
{
  final    int N_FFT_STEPS = 512;
  float    error;
  float[]  fit_error = new float[1];
  int      max_indexed = 0;
  Vector3D q_vec = new Vector3D();
                           // first, make hemisphere of possible directions 
                           // with specified resolution.
  int num_steps = (int)Math.round( 90.0 / degrees_per_step );
  Vector<Vector3D> full_list = MakeHemisphereDirections( num_steps );

                           // find the maximum magnitude of Q to set range
  float mag_Q;             // needed for FFT
  float max_mag_Q = 0;
  for ( int q_num = 1; q_num < q_vectors.size(); q_num++ )
  {
    q_vec = (Vector3D)(q_vectors.elementAt( q_num ));
    mag_Q = q_vec.length();
    if ( mag_Q > max_mag_Q )
      max_mag_Q = mag_Q;
  }
  max_mag_Q *= 1.1f;      // allow for a little "headroom" for FFT range

                          // apply the FFT to each of the directions, and
                          // keep track of their maximum magnitude past DC
  float   max_mag_fft;
  float[] max_fft_val   = new float[ full_list.size() ];

  float[] projections   = new float[ N_FFT_STEPS ];
  float[] magnitude_fft = new float[ N_FFT_STEPS/2 ];

  float index_factor = N_FFT_STEPS / max_mag_Q;     // maps |proj Q| to index 

  RealFloatFFT_Radix2 FFT = new RealFloatFFT_Radix2( N_FFT_STEPS );
  for ( int dir_num = 0; dir_num < full_list.size(); dir_num++ )
  {
    Vector3D current_dir = full_list.elementAt( dir_num );
    max_mag_fft = GetMagFFT( FFT, q_vectors, current_dir,
                             projections,
                             index_factor,
                             magnitude_fft );

    max_fft_val[ dir_num ] = max_mag_fft; 
  }
                          // find the directions with the 500 largest
                          // fft values, and place them in temp_dirs vector
  int N_TO_TRY = 500;
 
  float[] max_fft_copy  = new float[ full_list.size() ];
  System.arraycopy( max_fft_val, 0, max_fft_copy, 0, max_fft_copy.length );
  Arrays.sort( max_fft_copy );

  int index = max_fft_copy.length - 1;
  max_mag_fft = max_fft_copy[ index ];

  float threshold = max_mag_fft;
  while ( ( index > max_fft_copy.length - N_TO_TRY ) && 
            threshold >= max_mag_fft / 2)
  {
    index--;
    threshold = max_fft_copy[ index ];
  }

  Vector<Vector3D> temp_dirs = new Vector<Vector3D>();
  for ( int i = 0; i < max_fft_val.length; i++ )
    if ( max_fft_val[i] >= threshold )
    {
      temp_dirs.add( full_list.elementAt( i ) );
    }
                                  // now scan through temp_dirs and use the
                                  // FFT to find the cell edge length that
                                  // corresponds to the max_mag_fft.  Only keep
                                  // directions with length nearly in bounds
  Vector3D temp;
  Vector<Vector3D> temp_dirs_2 = new Vector<Vector3D>( temp_dirs.size() );

  for ( int i = 0; i < temp_dirs.size(); i++ )
  {
    max_mag_fft = GetMagFFT( FFT, q_vectors, temp_dirs.elementAt(i),
                             projections,
                             index_factor,
                             magnitude_fft );

    float position = GetFirstMaxIndex( magnitude_fft, threshold );
    if ( position > 0 )
    {
      float q_val = max_mag_Q / position;
      float d_val = 1 / q_val;
      if ( d_val >= 0.8 * min_d && d_val <= 1.2 * max_d )
      {
        temp = temp_dirs.elementAt(i);
        temp.multiply( d_val );
        temp_dirs_2.add( temp );
      }
    }
  }
                                   // look at how many peaks were indexed
                                   // for each of the initial directions
  max_indexed = 0;
  int      num_indexed;
  Vector3D current_dir = new Vector3D();
  for ( int dir_num = 0; dir_num < temp_dirs_2.size(); dir_num++ )
  {
    current_dir = temp_dirs_2.elementAt( dir_num );
    num_indexed = NumberIndexed_1D( current_dir, q_vectors, required_tolerance);
    if ( num_indexed > max_indexed )
      max_indexed = num_indexed;
  }
                                    // only keep original directions that index
                                    // at least 50% of max num indexed
  temp_dirs.clear();
  for ( int dir_num = 0; dir_num < temp_dirs_2.size(); dir_num++ )
  {
    current_dir = temp_dirs_2.elementAt( dir_num );
    num_indexed = NumberIndexed_1D( current_dir, q_vectors, required_tolerance);
    if ( num_indexed >= 0.50 * max_indexed )
      temp_dirs.add( current_dir );
  }
                                   // refine directions and again find the 
                                   // max number indexed, for the optimized
                                   // directions
  max_indexed = 0;
  Vector<Vector3D> index_vals = new Vector<Vector3D>();
  Vector<Vector3D> indexed_qs = new Vector<Vector3D>();
  for ( int dir_num = 0; dir_num < temp_dirs.size(); dir_num++ )
  {
    current_dir = temp_dirs.elementAt( dir_num );

    num_indexed = GetIndexedPeaks_1D( current_dir,
                                      q_vectors,
                                      required_tolerance,
                                      index_vals,
                                      indexed_qs,
                                      fit_error  );
    try
    {
      int     count  = 0;
      boolean failed = false;
      while ( count < 5 && !failed )     // 5 iterations should be enough for
      {                                  // the optimization to stabilize
        num_indexed = 0;
        error = Optimize_Direction_3D( current_dir, index_vals, indexed_qs );
 
        if ( Float.isNaN(error) )
          failed = true;
        else
        {
          num_indexed = GetIndexedPeaks_1D( current_dir,
                                            q_vectors,
                                            required_tolerance,
                                            index_vals,
                                            indexed_qs,
                                            fit_error  ); 
          if ( num_indexed > max_indexed )
            max_indexed = num_indexed;

          count++;
        }
      }
    }
    catch ( Exception ex )
    { 
      // System.out.print  ("Failed to optimize direction # " + dir_num );
      // don't continue to refine if the direction fails to optimize properly
    }
  }

                                      // discard ones with length out of bounds
  temp_dirs_2.clear();
  for ( int i = 0; i < temp_dirs.size(); i++ )
  {
    current_dir = temp_dirs.elementAt( i );
    float length = current_dir.length();
    if ( length >= min_d && length <= max_d )
      temp_dirs_2.add( current_dir );
  }
                                    // only keep directions that index at 
                                    // least 75% of the max number of peaks
  temp_dirs.clear();
  for ( int dir_num = 0; dir_num < temp_dirs_2.size(); dir_num++ )
  {
    current_dir = temp_dirs_2.elementAt( dir_num );
    num_indexed = NumberIndexed_1D(current_dir, q_vectors, required_tolerance);
    if ( num_indexed > max_indexed * 0.75 )
      temp_dirs.add( current_dir );
  }

  temp_dirs = SortOnVectorMagnitude( temp_dirs );

                                      // discard duplicates.  The tolerances
                                      // do not seem to be critical, but a 
                                      // looser tolerance discards more vectors
                                      // and improves performance.  Too loose
                                      // must eventually cause missed directions
/*
  float ang_tol =  20;                // 20 degree tolerance for angles is
  float len_tol = .35f;               // equiv to 35% tolerance for lengths
*/
  float ang_tol =  10;                // 10 degree tolerance for angles is
  float len_tol = .17f;               // equiv to 17% tolerance for lengths
/*
  float ang_tol =  5;                 // 5 degree tolerance for angles is
  float len_tol = .087f;              // equiv to 8.7% tolerance for lengths

  float ang_tol =  2;                 // 2 degree tolerance for angles is 
  float len_tol = .035f;              // equiv to 3.5% tolerance for lengths

  float ang_tol =  1;                 // 1 degree tolerance for angles is
  float len_tol = .017f;              // equiv to 1.7% tolerance for lengths

  float ang_tol =  .5f;               // 1/2 degree tolerance for angles is
  float len_tol = .01f;               // equiv .87% tolerance for lengths
*/
//  System.out.println("Test ang_tol = " + ang_tol + " len_tol = " + len_tol);

  temp_dirs = DiscardDuplicates( temp_dirs,
                                 q_vectors,
                                 required_tolerance,
                                 len_tol,
                                 ang_tol );

                                      // recalculate the max number indexed
                                      // and put list into directions vector
  directions.clear();
  for ( int i = 0; i < temp_dirs.size(); i++ )
  {
    current_dir = temp_dirs.elementAt( i );
    directions.add( current_dir );
  }
  
  return max_indexed;
}


/**
 *  Construct a sublist of the specified list of a,b,c directions, by removing
 *  all directions that seem to be duplicates.  If several directions all have
 *  the same length (within the specified length tolerance) and have the
 *  same direction (within the specified angle tolerange) then only one of 
 *  those directions will be recorded in the sublist.  The one that indexes
 *  the most peaks, within the specified tolerance will be kept.
 *
 *  @param  dirs                List of possible a,b,c directions, sorted in
 *                              order of increasing length.  This list will be
 *                              cleared by this method.
 *  @param  q_vectors           List of q_vectors that should be indexed
 *  @param  required_tolerance  The tolerance for indexing
 *  @param  len_tol             The tolerance on the relative difference in 
 *                              length for two directions to be considered
 *                              equal.  Eg. if relative differences must be
 *                              less than 5% for two lengths to be considered
 *                              the same, pass in .05 for the len_tol.
 *  @param  ang_tol             The tolerance for the difference in directions,
 *                              specified in degrees.
 *
 *  @return a new list without duplicated directions.
 */
private static Vector<Vector3D> DiscardDuplicates( Vector<Vector3D> dirs,
                                                   Vector<Vector3D> q_vectors,
                                                   float    required_tolerance,
                                                   float    len_tol,
                                                   float    ang_tol )
{
/*
  System.out.println("Before discarding duplicates, list size = " + 
                      dirs.size() );
*/
  Vector<Vector3D> new_list = new Vector<Vector3D>();
  Vector<Vector3D> temp     = new Vector<Vector3D>();
  Vector3D current_dir,
           next_dir;
  Vector3D zero_vec = new Vector3D(0,0,0);
  float    current_length,
           next_length;
  float    angle,
           len_diff;
  boolean  new_dir;
  int      dir_num = 0;
  int      check_index;
  while ( dir_num < dirs.size() )
  {                                          // put sequence of similar vectors
    current_dir = dirs.elementAt( dir_num ); // in sub-list temp.
    dir_num++;
    current_length = current_dir.length();

    if ( current_length > 0 )                 // skip any zero vectors
    {
      temp.clear();
      temp.add( current_dir );
      new_dir = false;
      check_index = dir_num;
      while ( check_index < dirs.size() && !new_dir )
      {
        next_dir = dirs.elementAt( check_index );
        next_length = next_dir.length();
        if ( next_length > 0 )
        {
          len_diff = Math.abs( next_dir.length() - current_length );
          if ( ( len_diff/current_length ) < len_tol )  // continue scan
          { 
            angle = angle( current_dir, next_dir );
            if ( (angle < ang_tol) || (angle > (180-ang_tol)) )
            {
              temp.add( next_dir );
              dirs.set( check_index, zero_vec );  // mark off this direction 
            }                                     // since it was duplicate

            check_index++;                    // keep checking all vectors with 
          }                                   // essentially the same length   
          else
            new_dir = true;                   // we only know we have a new
                                              // direction if the length is 
                                              // different, since list is 
        }                                     // sorted by length !
        else
          check_index++;                     // just move on!
      }
                                             // now scan through temp list to
      int max_indexed = 0;                   // find the one that indexes most
      int num_indexed;
      int max_i = -1;

      for ( int i = 0; i < temp.size(); i++ )
      {
        num_indexed = NumberIndexed_1D( temp.elementAt(i),
                                        q_vectors,
                                        required_tolerance );
        if ( num_indexed > max_indexed )
        {
          max_indexed = num_indexed;
          max_i = i;
        }
      }

      if ( max_indexed > 0 )               // don't bother to add any direction
      {                                    // that doesn't index anything
        new_list.add( temp.elementAt( max_i ) );
      }
    }
  }

  dirs.clear();
/*  System.out.println("After  discarding duplicates, list size = " + 
                      new_list.size() );
*/
  return new_list;
}


/**
   Get list of possible edge vectors for the real space unit cell.  This list
   will consist of vectors, V, for which V dot Q is essentially an integer for
   the most Q vectors.  The difference between V dot Q and an integer must be
   less than the required tolerance for it to count as an integer.
    @param  directions          Vector that will be filled with the directions
                                that may correspond to unit cell edges.
    @param  q_vectors           Vector of new Vector3D objects that contains 
                                the list of q_vectors that are to be indexed.
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
    @param  degrees_per_step    The number of degrees between directions that
                                are checked while scanning for an initial 
                                indexing of the peaks with lowest |Q|.
    @return The number of peaks indexed by each of the directions.
 */

public static int  ScanFor_Directions( Vector<Vector3D> directions,
                                       Vector   q_vectors,
                                       float    min_d,
                                       float    max_d,
                                       float    required_tolerance,
                                       float    degrees_per_step )
{
  float    error;
  float[]  fit_error = new float[1];
  float    dot_prod;
  int      nearest_int;
  int      max_indexed = 0;
  Vector3D q_vec = new Vector3D();
                           // first, make hemisphere of possible directions 
                           // with specified resolution.
  int num_steps = (int)Math.round( 90.0 / degrees_per_step );
  Vector<Vector3D> full_list = MakeHemisphereDirections( num_steps );

                           // Now, look for possible real-space unit cell edges
                           // by checking for vectors with length between 
                           // min_d and max_d that would index the most peaks,
                           // in some direction, keeping the shortest vector
                           // for each direction where the max peaks are indexed
  float delta_d = 0.1f;
  int n_steps = Math.round( 1 +(max_d - min_d)/delta_d );

  Vector<Vector3D> selected_dirs = new Vector<Vector3D>();
  Vector3D dir_temp = new Vector3D();

  for ( int dir_num = 0; dir_num < full_list.size(); dir_num++ )
  {
    Vector3D current_dir = full_list.elementAt( dir_num );

    for ( int step = 0; step <= n_steps; step++ )
    {
      dir_temp.set( current_dir );
      dir_temp.multiply( min_d + step * delta_d );    // increasing size
      
      int num_indexed = 0;
      for ( int q_num = 0; q_num < q_vectors.size(); q_num++ )
      {
        q_vec = (Vector3D)(q_vectors.elementAt( q_num ));
        dot_prod = dir_temp.dot( q_vec );
        nearest_int = Math.round( dot_prod );
        error = Math.abs( dot_prod - nearest_int );
        if ( error <= required_tolerance )
          num_indexed++;
      }

//#### if ( num_indexed > max_indexed + 1 ) // discard previous directions if 
//#### if ( num_indexed > max_indexed + 2 ) // discard previous directions if 
      if ( num_indexed > max_indexed )     // discard previous directions if 
      {                                    // we find a direction that indexes 
        selected_dirs.clear();             // significantly more peaks
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
                           // Now, optimize each direction and discard possible
                           // unit cell edges that are duplicates, putting the
                           // new smaller list in the vector "directions"
  Vector<Vector3D> index_vals = new Vector<Vector3D>();
  Vector<Vector3D> indexed_qs = new Vector<Vector3D>();
  directions.clear();
  Vector3D current_dir = new Vector3D();
  Vector3D diff        = new Vector3D();
  for ( int dir_num = 0; dir_num < selected_dirs.size(); dir_num++ )
  {
    current_dir = selected_dirs.elementAt( dir_num );

    GetIndexedPeaks_1D( current_dir, 
                        q_vectors,
                        required_tolerance,
                        index_vals, 
                        indexed_qs,
                        fit_error  );

    Optimize_Direction_3D( current_dir, index_vals, indexed_qs );

    float length = current_dir.length();
    if ( length >= min_d && length <= max_d )   // only keep if within range
    {
      boolean duplicate = false;
      for ( int i = 0; i < directions.size(); i++ )
      {
        dir_temp = directions.elementAt(i);
        diff.set( current_dir );
        diff.subtract( dir_temp );
                                                // discard same direction  
        if ( diff.length() < 0.001f )
          duplicate = true; 
                                                // discard opposite direction 
        else 
        {
          diff.set( current_dir );
          diff.add( dir_temp );
          if ( diff.length() < 0.001f )
            duplicate = true;
        }
      }
      if (!duplicate)
        directions.add( current_dir );
    }
  }

  return max_indexed;

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

public static  Vector<Vector3D>  getPeakQVals( Vector Peaks )
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


public static Tran3D Convert2Tran3D( Vector UB )
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
            R[r*3+c] = 
            ((Number)(((Vector)UB.elementAt( r )).elementAt(c))).floatValue( );
      }
   
   return new Tran3D(R);
}

public static Vector Convert2Vector( Tran3D UB )
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


public static float[][] Convert2floatArrayArray( Tran3D UB )
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

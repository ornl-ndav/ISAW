/* File: FindGonAnglesFromPHI_Rotation.java 
 *
 * Copyright (C) 2012, Dennis Mikkelson
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
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.instruments.*;

public class FindGonAnglesFromPHI_Rotation
{

/**
 * Find the angle (in degrees) needed to rotate v1 to v2 around the given
 * axis.
 *
 * @param v1   A vector that should be rotated about the axis to position v2.
 * @param v2   The final rotated position of v1.
 * @param axis The axis of a rotation that rotates v1 to v2.
 *
 * @return  The angle of rotation about the specified axis that will map 
 *          v1 to v2.
 */
  public static float PhiFromAxis( Vector3D axis, Vector3D v1, Vector3D v2 )
  {
    Vector3D u_axis = new Vector3D( axis );

                                  // Get local v1 with only the component of
                                  // v1 in plane perpendicular to the axis 
    v1 = new Vector3D( v1 );      // don't change the v1 that was passed in
    v1.normalize();
    u_axis.normalize();
    float comp = u_axis.dot(v1);
    u_axis.multiply( comp );
    v1.subtract( u_axis );
    v1.normalize();

                                  // Get local v2 with only the component of
                                  // v2 in plane perpendicular to the axis 
    v2 = new Vector3D( v2 );      // don't change the v1 that was passed in
    v2.normalize();
    u_axis.normalize();
    comp = u_axis.dot(v2);
    u_axis.multiply( comp );
    v2.subtract( u_axis );
    v2.normalize();
    
    Vector3D cross = new Vector3D();
    cross.cross( v1, v2 );
    float angle = (float)(Math.asin(cross.length()) * 180/Math.PI);

    if ( cross.dot(axis) < 0 )
      angle = -angle;

    return angle;
  }


/*
 *  Find a vector perpendicular to any axis that could be used to rotate 
 *  vector v1 to vector v2.  The locus of all possible axes of such a rotation
 *  is a plane that bisects v1 and v2 and contains the cross product of v1 
 *  and v2.
 *
 *  @param v1   A vector that should be rotated about an axis to position v2.
 *  @param v2   The final rotated position of v1.
 *
 *  @return The normal vector to the plane of possible axes of rotations that
 *          rotate v1 to v2.
 */
  public static Vector3D FindNormalToAxisVec( Vector3D v1, Vector3D v2 )
  {
    v1 = new Vector3D( v1 );
    v2 = new Vector3D( v2 );

    v1.normalize();
    v2.normalize();

    Vector3D sum = new Vector3D( v1 );
    sum.add( v2 );
    Vector3D perp_vec = new Vector3D();
    perp_vec.cross( v1, v2 );
    perp_vec.cross( perp_vec, sum );
    perp_vec.normalize();

    return perp_vec;
  }


/*
 *  Given TWO non-colinear vectors that are each normal to a plane of 
 *  possible axis vectors, find the axis vector.  This is calculated as
 *  the normalized cross product of n1 and n2.
 *
 *  @param  n1    A vector that is normal to one plane of possible axis
 *                vectors.
 *  @param  n2    A vector that is normal to another plane of possible axis
 *                vectors.
 *
 *  @return A vector that is along the line of intersection of the two
 *          planes of possible axis vectors. 
 */
  public static Vector3D FindAxisVecFromNormals( Vector3D n1, Vector3D n2 )
  {
    Vector3D axis = new Vector3D();
    axis.cross( n1, n2 );
    axis.normalize();
    return axis;
  }


/**
 *  Get the reciprocal lattice basis vectors a*, b*, c* from the columns
 *  of the UB matrix.
 *
 *  @param UB   The UB matrix
 *
 *  @return An array with three vectors, the columns of UB
 */
  public static Vector3D[] ABC_star_FromUB( Tran3D UB )
  {
    Vector3D[] abc = new Vector3D[3];
     
    float[][] UB_arr = UB.get();

    abc[0] = new Vector3D( UB_arr[0][0],UB_arr[1][0],UB_arr[2][0] );
    abc[1] = new Vector3D( UB_arr[0][1],UB_arr[1][1],UB_arr[2][1] );
    abc[2] = new Vector3D( UB_arr[0][2],UB_arr[1][2],UB_arr[2][2] );

    return abc;
  }  


/**
 *  Get the OMEGA angle of the goniometer from the PHI axis vector.  The
 *  zero OMEGA position is assumed to be with the CHI circle of the goniometer
 *  perpendicular to the beam direction.
 */
  public static float OmegaFromPhiAxis( Vector3D axis )
  {
    float angle = (float)(Math.atan2(axis.getY(), axis.getX()) * 180/Math.PI);
    angle = angle + 90; 
    return angle;
  }


/**
 *  Get the CHI angle of the goniometer from the PHI axis vector.  The
 *  zero CHI position is assumed to be with the PHI axis vertical upward.
 */
  public static float ChiFromPhiAxis( Vector3D axis, float omega )
  {
    Tran3D omega_inv          = new Tran3D();
    Vector3D up_dir           = new Vector3D( 0, 0, 1 );
    Vector3D rotated_phi_axis = new Vector3D();
    omega_inv.setRotation( -omega, up_dir );
    omega_inv.apply_to( axis, rotated_phi_axis );
    float angle = (float)(Math.atan2( rotated_phi_axis.getZ(), 
                                      rotated_phi_axis.getY() ) * 180/Math.PI);
    float chi = angle + 270;
    while ( chi > 180 )
      chi = chi - 360;
    while ( chi < -180 )
      chi = chi + 180;

    return chi;
  }

  
/**
 *  Given two UB matrices, corresponding to measurements with only a change in
 *  Phi, find the Chi and Omega angles and the change in Phi between the two
 *  measurements.  NOTE: This will only work if the UBs index the two lists 
 *  of peaks a consistent way.
 *
 *  @param UB1                UB for the first list of peaks.
 *  @param UB2                UB for the second list of peaks.
 *  @param delta_phi_positive Set true if the Phi angle changed by a positive
 *                            amount between measuring the first and second
 *                            set of peaks.  This is necessary to avoid a
 *                            second possible solution that rotates by -Phi
 *                            around an axis pointing in the opposite
 *                            direction.
 * @return a Vector of floats containing Phi, Chi and Omega, in that order.
 */
  public static Vector<Float> FindGonAnglesFromUBs( Tran3D  UB1, 
                                                    Tran3D  UB2,
                                                    boolean delta_phi_positive )
  {
    float   phi_sum   = 0;
    float   chi_sum   = 0;
    float   omega_sum = 0;
    Vector3D[] abc1 = ABC_star_FromUB( UB1 );
    Vector3D[] abc2 = ABC_star_FromUB( UB2 );
                           // NOTE: vector abc1[i] must map to vector abc2[i] 
                           //       for i = 0, 1, 2, so we can find three 
                           //       estimates for the Phi axis, one from each 
                           //       pair of vectors.  We also get three sets
                           //       of three estimates of Phi and three
                           //       estimates of Chi and Omega, 
    Vector3D[] axis_normals = new Vector3D[3];
    for ( int i = 0; i < 3; i++ )
      axis_normals[i] = FindNormalToAxisVec( abc1[i], abc2[i] );

    Vector3D[] phi_axes     = new Vector3D[3];
    float[]    phi_angles   = new float[3];
    float[]    omega_angles = new float[3];
    float[]    chi_angles   = new float[3];
    for ( int i = 0; i < 3; i++ )
    {
      phi_axes[i]=FindAxisVecFromNormals(axis_normals[i],axis_normals[(i+1)%3]);

      float   phi;

      phi = PhiFromAxis(phi_axes[i],abc1[0],abc2[0]);
      if (  delta_phi_positive && phi <= 0 ||
           !delta_phi_positive && phi >= 0  )
        phi_axes[i].multiply(-1);
                                    // For this calculated Phi axis, set
      phi_sum = 0;                  // phi to average of rotation angles for
      for ( int k = 0; k < 3; k++ ) // all three vectors, a, b, c if sign ok
      {
        phi = PhiFromAxis(phi_axes[i],abc1[k],abc2[k]);

        if (  delta_phi_positive && phi <= 0 ||
             !delta_phi_positive && phi >= 0  )
          throw new IllegalArgumentException("Inconsistent signs on phi");

        phi_sum += phi;
      }
                                     // Take phi as the average of angles that
                                     // rotate abc1[i] to abc2[i].  Then find 
                                     // Omega and Chi from the current Phi
                                     // axis.
      phi_angles[i]   = phi_sum/3;
      omega_angles[i] = OmegaFromPhiAxis( phi_axes[i] );
      chi_angles[i]   = ChiFromPhiAxis( phi_axes[i], omega_angles[i] ); 
    }
                                     // Now average the Phi, Chi and Omega
                                     // estimates from each of the calculations
                                     // of the phi axes.
    phi_sum   = 0;
    chi_sum   = 0;
    omega_sum = 0;
    for ( int i = 0; i < 3; i++ )
    {
      phi_sum   += phi_angles[i];
      chi_sum   += chi_angles[i];
      omega_sum += omega_angles[i];
    }

    Vector<Float> results = new Vector<Float>();    
    results.add( phi_sum/3 );
    results.add( chi_sum/3 );
    results.add( omega_sum/3 );

    System.out.printf("\nPHI = %7.2f   CHI = %7.2f   OMEGA = %7.2f\n",
           results.elementAt(0), results.elementAt(1), results.elementAt(2) ); 

    return results;
  }


/**
 *  Given two lists of indexed peaks, measured with only a change in PHI,
 *  find the Chi and Omega angles and the change in Phi between the two
 *  measurements.  NOTE: This will only work if the two lists of peaks have
 *  already been indexed in a consistent way.
 *
 *  @param peaks_1            First list of indexed peaks.
 *  @param peaks_2            Second list of indexed peaks.
 *  @param delta_phi_positive Set true if the Phi angle changed by a positive
 *                            amount between measuring the first and second
 *                            set of peaks.  This is necessary to avoid a
 *                            second possible solution that rotates by -Phi
 *                            around an axis pointing in the opposite
 *                            direction.
 * @return a Vector of floats containing Phi, Chi and Omega, in that order.
 */
  public static Vector<Float> FindGonAnglesFromIndexedPeaks(
                                          Vector<Peak_new> peaks_1,
                                          Vector<Peak_new> peaks_2,
                                          boolean          delta_phi_positive )
  {
    Tran3D UB1 = PeaksFileUtils.FindUB_FromIndexing( peaks_1, 0.12f );
    Tran3D UB2 = PeaksFileUtils.FindUB_FromIndexing( peaks_2, 0.12f );

    return FindGonAnglesFromUBs( UB1, UB2, delta_phi_positive );
  }


/**
 *  Given the UB for a first set of peaks, and a rotation angle PHI applied
 *  to the sample to measure a second set of peaks, find the corresponding
 *  UB that will index the second set of peaks in a consistent way.  This
 *  method generates a large number of possible rotation axes and rotates
 *  UB1 by the specified phi angle about each of these possible axes.  It
 *  keeps the rotation that indexes the most peaks, and sets an initial
 *  UB2 to the rotation times UB1.  It then optimizes this UB2, and returns
 *  the optimized version.
 *
 *  @param UB1       UB matrix for a first set of peaks.
 *  @param phi       Change in Phi angle between measuring the first set
 *                   of peaks and the second set of peaks.  ONLY PHI should
 *                   change between these two measurements.
 *  @param peaks_2   Second set of peaks.
 *  @param tolerance Threshold on h,k,l that determines whether or not 
 *                   peaks are indexed.
 */
  public static Tran3D FindUB2_FromUB1_AndPhi( Tran3D           UB1,
                                               float            phi,
                                               Vector<Peak_new> peaks_2,
                                               float            tolerance )
  {
    Vector<Vector3D> q_vecs = new Vector<Vector3D>();
    for ( int i = 0; i < peaks_2.size(); i++ )
      q_vecs.add( new Vector3D( peaks_2.elementAt(i).getQ() ) );

    Vector<Vector3D> axes = IndexingUtils.MakeHemisphereDirections( 90 );
    int num_indexed    = 0;
    int max_indexed    = 0;
    Vector3D axis      = null;
    Tran3D   rotatedUB = new Tran3D();
    for ( int i = 0; i < axes.size(); i++ )
    {
      rotatedUB.setRotation( phi, axes.elementAt(i) );
      rotatedUB.multiply_by( UB1 );
      num_indexed = IndexingUtils.NumberIndexed(rotatedUB, q_vecs, tolerance); 
      if ( num_indexed > max_indexed )
      {
        max_indexed = num_indexed;
        axis = new Vector3D( axes.elementAt(i));
      }

      axes.elementAt(i).multiply(-1);
      rotatedUB.setRotation( phi, axes.elementAt(i) );
      rotatedUB.multiply_by( UB1 );
      num_indexed = IndexingUtils.NumberIndexed(rotatedUB, q_vecs, tolerance);
      if ( num_indexed > max_indexed )
      {
        max_indexed = num_indexed;
        axis = new Vector3D( axes.elementAt(i));
      }
    }

    Tran3D UB2 = null;
    Tran3D UB2_inv;
    rotatedUB.setRotation( phi, axis );
    rotatedUB.multiply_by( UB1 );
    UB2 = new Tran3D( rotatedUB );

    for ( int count = 0; count < 10; count++ )
    {
      Vector<Vector3D> hkls = new Vector<Vector3D>();
      q_vecs.clear();
      UB2_inv = new Tran3D( UB2 );
      UB2_inv.invert();
      for ( int i = 0; i < peaks_2.size(); i++ )
      {
        Peak_new peak = peaks_2.elementAt(i);
        peak.sethkl( 0, 0, 0 );
        Vector3D q   = new Vector3D( peak.getQ() );
        Vector3D hkl = new Vector3D();
        UB2_inv.apply_to( q, hkl );
        if ( IndexingUtils.ValidIndex( hkl, tolerance ) )
        {
          hkl.set( Math.round(hkl.getX()), 
                   Math.round(hkl.getY()), 
                   Math.round(hkl.getZ()) );
          peaks_2.elementAt(i).sethkl( hkl.getX(), hkl.getY(), hkl.getZ() ); 
          q_vecs.add( q );
          hkls.add( hkl );
        }
      }
      IndexingUtils.Optimize_UB_3D( UB2, hkls, q_vecs );
    }

    return UB2;
  }


/**
 *  Find a Niggli reduced cell that will best index the specified peaks,
 *  using the FFT indexing routine from IndexingUtils, and NO goniometer
 *  rotation.
 *
 *  @param peaks      Vector of peaks to be indexed.
 *  @param min_d      A number strictly less than the shortest edge of the
 *                    real space Niggli reduced cell.
 *  @param max_d      A number strictly more than the longest edge of the
 *                    real space Niggli reduced cell.
 *  @param tolerance  Tolerance on fractional Miller indexes for a peak to
 *                    be considered indexed.
 *  @param angle_step Approximate angle between successive directions used
 *                    in the FFT indexing routine.
 *  @return A Tran3D holding the UB matrix found by the FFT indexing routine.
 */
  public static Tran3D FindUB_RAW( Vector<Peak_new> peaks,
                                   float            min_d,
                                   float            max_d,
                                   float            tolerance,
                                   float            angle_step )
  {
    if ( peaks.size() < 4 )
      return null;

    Vector<Vector3D> q_vectors = new Vector<Vector3D>();
    for ( int i = 0; i < peaks.size(); i++ )
      q_vectors.add( new Vector3D( peaks.elementAt(i).getQ() ) );

    Tran3D UB_tran = new Tran3D();
    IndexingUtils.FindUB_UsingFFT( UB_tran,
                                   q_vectors,
                                   min_d, max_d,
                                   tolerance,
                                   angle_step );
    return UB_tran;
  }


/**
 *  Estimate the goniometer angles from two sets of peaks, that were 
 *  measured at the same Chi and Omega angles, but different Phi angles.
 *  NOTE: This will occasionally fail by finding other rotations that index
 *  a large number of peaks that are symmetry equivalent to the correctly
 *  rotated peaks.  It should be possible to avoid this problem by choosing
 *  a different PHI rotation. 
 *  This method first finds a UB corresponding to the first set of
 *  peaks using the FFT based method.  It then tests a large number of
 *  rotations by the specified Phi angle, about different axes, applied to UB,
 *  to find the one that best indexes the second set of peaks.  That second
 *  UB is then optimized.  Finally, given the two UB matrices, the axis and
 *  and angle of rotation that maps the a*, b* and c* vectors for the first
 *  UB to the a*, b* and c* vectors for the second UB are calculated, and
 *  the corresponding Phi, Chi and Omega angles are returned.
 *  @param peaks_1    First set of peaks
 *  @param peaks_2    Second set of peaks
 *  @param phi        The amount the Phi angle was changed between measuring
 *                    the first set of peaks and the second set of peaks.
 *  @param min_d      Lower bound on real space primitive unit cell edges. 
 *  @param max_d      Upper bound on real space primitive unit cell edges.
 *  @param tolerance  Tolerance on h,k,l for determining which peaks are 
 *                    indexed.
 *
 *  @return a Vector with three floats, the estimated Phi, Chi and Omega values.
 */
  public static Vector<Float> GoniometerAnglesFromPhiRotation( 
                                                 Vector<Peak_new> peaks_1,
                                                 Vector<Peak_new> peaks_2,
                                                 float            phi,
                                                 float            min_d,
                                                 float            max_d,
                                                 float            tolerance )
  { 
    float angle_step = 1;           // 1 degree steps in possible directions 

    Tran3D UB1 = FindUB_RAW( peaks_1, min_d, max_d, tolerance, angle_step );

    Tran3D UB2 = FindUB2_FromUB1_AndPhi( UB1, phi, peaks_2, tolerance );

    boolean positive_delta_phi = true;
    if ( phi < 0 )
      positive_delta_phi = false;

    return FindGonAnglesFromUBs( UB1, UB2, positive_delta_phi );
  }


/**
 *  Estimate the goniometer angles from two peaks files, that were 
 *  measured at the same Chi and Omega angles, but different Phi angles.
 *  NOTE: This will occasionally fail by finding other rotations that index
 *  a large number of peaks that are symmetry equivalent to the correctly
 *  rotated peaks.  It should be possible to avoid this problem by choosing
 *  a different PHI rotation. 
 *  This method first finds a UB corresponding to the first set of
 *  peaks using the FFT based method.  It then tests a large number of
 *  rotations by the specified Phi angle, about different axes, applied to UB,
 *  to find the one that best indexes the second set of peaks.  That second
 *  UB is then optimized.  Finally, given the two UB matrices, the axis and
 *  and angle of rotation that maps the a*, b* and c* vectors for the first
 *  UB to the a*, b* and c* vectors for the second UB are calculated, and
 *  the corresponding Phi, Chi and Omega angles are returned.
 *  @param file_1     Fully qualified name of the first peaks file. 
 *  @param file_2     Fully qualified name of the second peaks file.
 *  @param phi        The amount the Phi angle was changed between measuring
 *                    the first set of peaks and the second set of peaks.
 *  @param min_d      Lower bound on real space primitive unit cell edges. 
 *  @param max_d      Upper bound on real space primitive unit cell edges.
 *  @param tolerance  Tolerance on h,k,l for determining which peaks are 
 *                    indexed.
 *
 *  @return a Vector with three floats, the estimated Phi, Chi and Omega values.
 */
  public static Vector<Float> GoniometerAnglesFromPhiRotation(
                                                          String  file_1,
                                                          String  file_2,
                                                          float   phi,
                                                          float   min_d,
                                                          float   max_d,
                                                          float   tolerance )
                throws Exception
  {
    Vector<Peak_new> peaks_1 = Peak_new_IO.ReadPeaks_new( file_1 );
    Vector<Peak_new> peaks_2 = Peak_new_IO.ReadPeaks_new( file_2 );

    return GoniometerAnglesFromPhiRotation( peaks_1, peaks_2, phi,
                                            min_d, max_d, tolerance );
  }


/**
 * Crude tests of the methods in this class.
 */
  public static void main( String args[] ) throws Exception
  {

    boolean delta_phi_positive = true;

    String dir = "/usr2/SNAP_FIXED_OMEGA/RESULTS/";
    String run_1 = args[0];
    String run_2 = args[1];
    String file_1 = dir + "/" + run_1 + "_Niggli.integrate";
    String file_2 = dir + "/" + run_2 + "_Niggli.integrate";
/*
    String dir ="/usr2/TOPAZ_SAPPHIRE_JUNE_2012/ISAW_EV_RAW/";
    String file_1 = dir + "/" + "5637_Niggli_Consistent.integrate";
    String file_2 = dir + "/" + "5643_Niggli_Consistent.integrate";

    String dir ="/usr2/TOPAZ_SAPPHIRE_JUNE_2012/NEW_MANTID_SCRIPT/TEST_5/FOUND";
    String file_1 = dir + "/" + "5637_Niggli.integrate";
    String file_2 = dir + "/" + "5643_Niggli.integrate";
*/
    Vector<Peak_new> peaks_1 = Peak_new_IO.ReadPeaks_new( file_1 );
    Vector<Peak_new> peaks_2 = Peak_new_IO.ReadPeaks_new( file_2 );
    System.out.println("Loaded " + peaks_1.size() + " and " + peaks_2.size());

    float phi       = 15;
    float min_d     =  4.0f;
    float max_d     =  8.0f;
    float tolerance = 0.12f;

    Vector<Float> angles = null;
    System.out.println("\nUSING PRE-CALCULATED INDEXES GIVES");
    angles = FindGonAnglesFromIndexedPeaks(peaks_1,peaks_2,delta_phi_positive);
    System.out.println("\nANGLES = " + angles.elementAt(0) +
                       ", " + angles.elementAt(1) +
                       ", " + angles.elementAt(2) + "\n");


    System.out.println("\nUSING UBs FROM PRE-CALCULATED INDEXES GIVES");
    Tran3D UB1 = PeaksFileUtils.FindUB_FromIndexing( peaks_1, 0.12f );
    Tran3D UB2 = PeaksFileUtils.FindUB_FromIndexing( peaks_2, 0.12f );
    angles = FindGonAnglesFromUBs( UB1, UB2, delta_phi_positive );
    System.out.println("\nANGLES = " + angles.elementAt(0) +
                       ", " + angles.elementAt(1) +
                       ", " + angles.elementAt(2) + "\n");

    System.out.println("\nUSING UB_2 FROM SEARCH OF ROTATIONS ON PEAKS");
    angles = GoniometerAnglesFromPhiRotation( peaks_1, peaks_2,
                                              phi,
                                              min_d, max_d,
                                              tolerance );
    System.out.println("\nANGLES = " + angles.elementAt(0) +
                       ", " + angles.elementAt(1) +
                       ", " + angles.elementAt(2) + "\n");

    System.out.println("\nUSING UB_2 FROM SEARCH OF ROTATIONS ON PEAKS FILES");
    angles = GoniometerAnglesFromPhiRotation( file_1, file_2,
                                              phi,
                                              min_d, max_d,
                                              tolerance );

    System.out.println("\nANGLES = " + angles.elementAt(0) +
                       ", " + angles.elementAt(1) +
                       ", " + angles.elementAt(2) + "\n");

  }

}


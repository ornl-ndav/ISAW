/* 
 * File: ARCS_Index_Calc.java
 *
 * Copyright (C) 2010, Dennis Mikkelson
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

import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Geometry.*;
import DataSetTools.operator.Generic.TOF_SCD.*;

/**
 * Get a UB matrix and additional information required for orienting a
 * single crystal on ARCS, given the lattice parameters, a list of peaks
 * and the initial PSI angle and estimates of hkl's for the U and V vectors 
 * in the horizonatl plane.  The required information is returned in the
 * first four positions of a Vector.
 *
 * @param lattice_params      Array of doubles containing the lattice
 *                            parameters, a, b, c, alpha, beta and gamma
 *                            in that order.
 * @param peaks               Vector of Peak_new objects containing measured 
 *                            peaks.  These are not assumed to
 *                            be indexed yet.
 * @param psi_deg             The estimated PSI angle in degrees.
 * @param u_hkl               The HKL values for a peak, U, that is essentially
 *                            in the horizontal plane, at an angle of PSI
 *                            from the beam direction.
 * @param v_hkl               The HKL values for a second peak, V, that is 
 *                            essentially in the horizontal plane, with
 *                            V chosen so that the vector U X V  points
 *                            upward.
 * @param required_tolerance  The tolerance required to consider a peak
 *                            to be indexed.
 * @param initial_number      The number of low-Q peaks that should be 
 *                            indexed first, before starting to increase the
 *                            size of the region in which peaks are indexed.
 *                            This should typically be a small value roughly
 *                            from 5-20.
 * @param required_fraction   The fraction of the initial number of low_Q
 *                            peaks that should be indexed before starting to 
 *                            increase the size of the region in which peaks 
 *                            are indexed.  This shouid be typically between
 *                            0.3 and 1.  The initial number and required
 *                            fraction may be adjusted to require their
 *                            product to be at least 4.
 * @return a Vector containing the required information.  Position 0 has
 * the calculated UB matrix in the form written by ISAW to a .mat file.
 * Position 1 has the new PSI angle, corresponding to the calculated UB.
 * Position 2 has the new values for the HKLs of the projected vector U.
 * Position 3 has the new values for the HKLs of the projected vector V.
 */

public class ARCS_Index_Calc
{

  public static Vector index( double[]    lattice_params, 
                              Vector      peaks,
                              double      psi_deg,
                              Vector3D_d  u_hkl,
                              Vector3D_d  v_hkl,
                              double      required_tolerance,
                              int         initial_number,
                              double      required_fraction )
                     throws IOException
  {
                                         // first get the materials matrix, B
     double[][] A_matrix = lattice_calc.A_matrix( lattice_params );
     double[][] B        = LinearAlgebra.getInverse( A_matrix );   
     for ( int i = 0; i < 3; i++ )
       for ( int j = 0; j < 3; j++ )
         B[i][j] *= 2*Math.PI; 
                                       // next, find rotation U1 to rotate 
                                       // B*u_hkl to a vector at angle psi_deg
                                       // from the beam direction, in the 
                                       // horizontal plane.
     Tran3D_d   B_tran = new Tran3D_d( B );
     Vector3D_d u_vec  = new Vector3D_d();

     B_tran.apply_to( u_hkl, u_vec );
     u_vec.normalize();
                                        // u_target is the vector that the
                                        // specified peak should rotate to
                                        // in the horizontal plane
     double psi_rad = Math.PI * psi_deg / 180;
     Vector3D_d u_target = new Vector3D_d ( Math.cos( psi_rad ), 
                                            Math.sin( psi_rad ), 0 );
     Tran3D_d U1 = new Tran3D_d();

     Vector3D_d axis = new Vector3D_d();
     axis.cross( u_vec, u_target );
     if ( axis.length() != 0 )
       axis.normalize();
     else                                   // u_vec and u_target co-linear so 
       axis = new Vector3D_d( 0, 0, 1 );    // both must be in horiz. plane
    
     double dot_prod  = u_vec.dot( u_target ); 
     double rot_angle = Math.acos( dot_prod );
     double rot_deg   = rot_angle * 180 / Math.PI;

     U1.setRotation( rot_deg, axis );

     Tran3D_d U1B = new Tran3D_d();
     U1B.multiply_by( U1 );
     U1B.multiply_by( B_tran );
                                        // Now find rotation U2 about the
                                        // rotated_u axis, that will place
                                        // v in the horizontal plane with
                                        // u X v vertically upward
     Vector3D_d axis_2 = new Vector3D_d();
     U1B.apply_to( u_hkl, axis_2 );
     axis_2.normalize();

     Vector3D_d v_vec = new Vector3D_d();
     U1B.apply_to( v_hkl, v_vec );
                                        // project rotated v unto plane 
                                        // perpendicular to axis_2     
     Vector3D_d comp_1 = new Vector3D_d( axis_2 );
     comp_1.multiply( v_vec.dot(axis_2) );
     v_vec.subtract( comp_1 );
     v_vec.normalize();

     Vector3D_d vert_vec = new Vector3D_d( 0, 0, 1 ); 
     double vert_comp = vert_vec.dot( v_vec );

     Vector3D_d horiz_vec = new Vector3D_d();
     horiz_vec.cross( vert_vec, axis_2 );
     horiz_vec.normalize();
     double horiz_comp = horiz_vec.dot( v_vec );

     double phi_deg = (180/Math.PI)*Math.atan2( vert_comp, horiz_comp );
     Tran3D_d U2 = new Tran3D_d();
     U2.setRotation( -phi_deg, axis_2 );

     Tran3D_d UB = new Tran3D_d();
     UB.multiply_by( U2 );
     UB.multiply_by( U1 );
     UB.multiply_by( B_tran );

     double[][] UB_inv_mat = new double[3][3];
     double[][] temp = UB.get();
     for ( int i = 0; i < 3; i++ )
       for ( int j = 0; j < 3; j++ )
         UB_inv_mat[i][j] = temp[i][j];
     LinearAlgebra.invert( UB_inv_mat );

     IndexPeaks_Calc.SortPeaksMagQ( peaks, false );
//   Peak_new_IO.WritePeaks_new( "temp_0.peaks", peaks, false );
     Vector some_peaks = new Vector();

     initial_number = Math.min( initial_number, peaks.size() );
     for ( int i = 0; i < initial_number; i++ )
       some_peaks.add( peaks.elementAt(i) );

     double tol = required_tolerance;
     double frac = required_fraction;
     if ( tol < 0.01 )
       tol = 0.01;

     int num_indexed = 0;
     while ( num_indexed < initial_number * frac && tol < 0.4 )
     {
       IndexPeaks_Calc.Index( some_peaks, UB_inv_mat, tol );
       num_indexed = IndexPeaks_Calc.NumIndexed( some_peaks, UB_inv_mat, tol );

//     if ( num_indexed > 3 )
//       IndexPeaks_Calc.OptimizeUB(some_peaks, UB_inv_mat, UB_inv_mat, tol);
//     System.out.println("Number indexed = " + num_indexed );

       if ( num_indexed < initial_number * frac )
         tol = tol * 1.2;
     }
//     System.out.println("Tolerance = " + tol );

     IndexPeaks_Calc.OptimizeUB( some_peaks, UB_inv_mat, UB_inv_mat, tol );

     IndexPeaks_Calc.Index( peaks, UB_inv_mat, tol );
     num_indexed = IndexPeaks_Calc.NumIndexed( peaks, UB_inv_mat, tol );
     System.out.println("Number indexed after FIRST LOOP = " + num_indexed );
//   Peak_new_IO.WritePeaks_new( "temp_2.peaks", peaks, false );
     
     int number = (int)(initial_number * 1.5);
     if ( number < 10 )
       number = 10;
     tol = required_tolerance;
     while ( number < peaks.size() ) // && num_indexed < .8 * peaks.size() )
     { 
       some_peaks.clear();
       for ( int i = 0; i < number; i++ )
         some_peaks.add( peaks.elementAt(i) );

       IndexPeaks_Calc.OptimizeUB(some_peaks, UB_inv_mat, UB_inv_mat, tol);

       IndexPeaks_Calc.Index( peaks, UB_inv_mat, tol );
       num_indexed = IndexPeaks_Calc.NumIndexed( peaks, UB_inv_mat, tol );
//     System.out.println("Number indexed in SECOND = " + num_indexed );

       number = (int)(number * 1.3);
     }

//   Peak_new_IO.WritePeaks_new( "temp_3.peaks", peaks, false );

     Tran3D_d newUB = new Tran3D_d( UB_inv_mat );
     newUB.invert();

     Vector3D_d u_proj_hkl = projected_HKL_info( u_hkl, newUB );
     Vector3D_d v_proj_hkl = projected_HKL_info( v_hkl, newUB );

     double new_psi = getPSI( u_hkl, newUB );

     double[][] UB_mat = newUB.get();
     float[][] newUB_mat = new float[3][3];
     for ( int i = 0; i < 3; i++ )
       for ( int j = 0; j < 3; j++ )
         newUB_mat[i][j] = (float)(UB_mat[j][i] / (2*Math.PI));

     Vector results = new Vector();
     results.add( newUB_mat );
     results.add( new_psi );
     results.add( u_proj_hkl );
     results.add( v_proj_hkl );

     return results;
  }


  public static Vector3D_d projected_HKL_info( Vector3D_d hkl, Tran3D_d UB )
  {
    Tran3D_d  UB_inv = new Tran3D_d( UB );
    UB_inv.invert();

    Vector3D_d exact_qxyz = new Vector3D_d();
    UB.apply_to( hkl, exact_qxyz );

    Vector3D_d projected_qxyz = new Vector3D_d( exact_qxyz.getX(),
                                                exact_qxyz.getY(), 0 );

    Vector3D_d projected_hkl = new Vector3D_d();
    UB_inv.apply_to( projected_qxyz, projected_hkl );

    return projected_hkl;
  }


  public static double getPSI( Vector3D_d hkl, Tran3D_d UB )
  {
    Vector3D_d qxyz = new Vector3D_d();
    UB.apply_to( hkl, qxyz );

    Vector3D_d projected_qxyz = new Vector3D_d( qxyz.getX(), qxyz.getY(), 0 );

    double psi = Math.atan2( projected_qxyz.getY(), projected_qxyz.getX() );
    double psi_deg = psi * 180 / Math.PI;

    return psi_deg;
  }


  public static void main( String args[] ) throws Exception
  {
    String peaks_file = "/usr2/ARCS_SCD_6/ARCS_6449.peaks";
//  String peaks_file = "/usr2/ARCS_SCD_5/ARCS_3948.peaks";
//  String peaks_file = "/usr2/ARCS_SCD_5/ARCS_4513.peaks";

    double[] lattice_params = { 3.805, 3.805, 6.28, 90, 90, 90 };
//  double[] lattice_params = { 4.48, 4.48, 4.48, 90, 90, 90 };
//  double[] lattice_params = { 5.45, 5.45, 5.45, 90, 120, 120 };
    Vector   peaks = Peak_new_IO.ReadPeaks_new( peaks_file );

//  Vector3D_d u_hkl = new Vector3D_d( 1, 0, 0 );
    Vector3D_d u_hkl = new Vector3D_d( 0, 0, -1 );    // det 65
    Vector3D_d v_hkl = new Vector3D_d( 1, 0, -3 );    // det 46

//  Vector3D_d u_hkl = new Vector3D_d(  0, -1, -1 );
//  Vector3D_d v_hkl = new Vector3D_d(  1, -2, -2 );

//  Vector3D_d u_hkl = new Vector3D_d(  0,  -2,  0 );
//  Vector3D_d v_hkl = new Vector3D_d( -2,  -1,  2 );

//  double psi_deg = 20;
//  double psi_deg = 101.446;
    double psi_deg = 95; 
//  double psi_deg = 127;
//  double psi_deg = 105.5;

    int    num = 10;
    double tol = 0.10;
    double required_fraction = 0.5;

    Vector results = index( lattice_params, 
                            peaks, 
                            psi_deg, 
                            u_hkl, 
                            v_hkl, 
                            tol, 
                            num, 
                            required_fraction );

     float[][] UB          = (float[][]) results.elementAt(0);
     double psi            = (Double)    results.elementAt(1);
     Vector3D_d u_proj_hkl = (Vector3D_d)results.elementAt(2);
     Vector3D_d v_proj_hkl = (Vector3D_d)results.elementAt(3);

     System.out.println("UB = ");
     LinearAlgebra.print( UB );
     System.out.printf("PSI = %8.4f\n", psi);
     System.out.printf( "U Projected HKL  = %6.3f  %6.3f  %6.3f\n",
                u_proj_hkl.getX(), u_proj_hkl.getY(), u_proj_hkl.getZ() );
     System.out.printf( "V Projected HKL  = %6.3f  %6.3f  %6.3f\n",
                v_proj_hkl.getX(), v_proj_hkl.getY(), v_proj_hkl.getZ() );
  }

}

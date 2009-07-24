/* 
 * File: SCD_OrientationErrorF.java
 *
 * Copyright (C) 2009, Dennis Mikkelson
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
 * NOTE: This class is a simplified version of the Peak.java class 
 *       written by Peter Peterson.
 *
 *  Last Modified:
 * 
 *  $Author: eu7 $
 *  $Date: 2008-11-20 10:12:35 -0600 (Thu, 20 Nov 2008) $            
 *  $Revision: 19434 $
 */

package DataSetTools.trial;

import java.io.*;
import java.util.*;

import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Functions.*;
import gov.anl.ipns.MathTools.Geometry.*;

import DataSetTools.instruments.*;
import DataSetTools.operator.Generic.TOF_SCD.*;


public class SCD_OrientationErrorF extends    OneVarParameterizedFunction
                                   implements Serializable
{
  private int          eval_count = 0;
  private double[][]   peaks_q_vec; 
  private double[][]   U = new double[3][3];
  private double[][]   B;
  private double[][]   UBinverse;


  public SCD_OrientationErrorF( Vector<IPeakQ> peaks, 
                                double[] lattice_params,
                                double[] params,
                                String[] param_names )
  {
     super( "SCD_OrientationErrorF", params, param_names );
                                                 // get list of unrotated Qs
                                                 // from the peaks
     peaks_q_vec = new double[peaks.size()][3];
     float[] temp;
     for ( int index = 0; index < peaks.size(); index++ )
     {
       temp = peaks.elementAt(index).getUnrotQ();
       for ( int i = 0; i < 3; i++ )
         peaks_q_vec[index][i] = temp[i] * 2 * Math.PI; 
     }
                                                 // calculate an initial
                                                 // B matrix from lattice
                                                 // params
     double[][] A_matrix = lattice_calc.A_matrix( lattice_params );
//     System.out.println("A_matrix = " );
//     LinearAlgebra.print( A_matrix );

     B = LinearAlgebra.getInverse( A_matrix );
//     System.out.println("B = " );
//     LinearAlgebra.print( B );

//     double lat_par[] = lattice_calc.LatticeParamsOfUB( B );
//     lat_params[6] *= 8 * Math.PI * Math.PI * Math.PI;
//     System.out.println("Re-calculated Lattice Parameters");
//     LinearAlgebra.print( lat_par );

     for ( int i = 0; i < 3; i++ )
       for ( int j = 0; j < 3; j++ )
         B[i][j] *= 2*Math.PI;
//     System.out.println("2 * PI * B = " );
//     LinearAlgebra.print( B );
                                                 // calculate initial U matrix
                                                 // with NO rotation
     
     setParameters( parameters );
  }


  /**
   *  array of parameters containing euler rotation angles for the 
   *  calculated Q vectors.   
   *  parameters[0] = phi;
   *  parameters[1] = chi;
   *  parameters[2] = omega;
   */
  public void setParameters( double parameters[] )
  {
    super.setParameters( parameters );
    SampleOrientation_d orientation = new SNS_SampleOrientation_d 
                                             ( parameters[0],
                                               parameters[1],
                                               parameters[2] );
    Tran3D_d rotation = orientation.getGoniometerRotation();
    double[][] tran = rotation.get();
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
        U[row][col] = tran[row][col];

    UBinverse = LinearAlgebra.mult( U, B );
    LinearAlgebra.invert( UBinverse );

    eval_count++;
/*
    if ( eval_count % 100 == 0 )
    {
      String message = "After " + eval_count + " steps... params are";
      ShowProgress( message, System.out );
      System.out.println("U matrix is " );
      LinearAlgebra.print( U );
    }
*/

  }
 

  public double getValue( double x )
  {
    int       index = (int)Math.round(x);
    double    sum   = 0;
    double    diff;                        // set to distance to nearest
                                           // integer for h, then k and l
    double[]  q_vec = new double[3];
    double[]  hkl;

    for ( int i = 0; i < 3; i++ )
      q_vec[i] = peaks_q_vec[index][i];

    hkl = LinearAlgebra.mult( UBinverse, q_vec );
      
    for ( int i = 0; i < 3; i++ )                // find the sum of the squared
    {                                            // distances to nearest integer
      int floor = (int)Math.floor( hkl[i] );     // for h, k and l.
      diff = hkl[i] - floor;
      if ( diff > 0.5 )
        diff = 1 - diff;

      sum += diff * diff;
    }

//    return Math.sqrt(sum);
      return sum;
//    return sum*sum;
  }


  public void show_hkl( int index )
  {
    double[]  q_vec = new double[3];
    double[]  hkl;
    for ( int i = 0; i < 3; i++ )
      q_vec[i] = peaks_q_vec[index][i];

    hkl = LinearAlgebra.mult( UBinverse, q_vec );

    System.out.printf("index = %5d   %7.4f  %7.4f  %7.4f\n",
                       index, hkl[0], hkl[1], hkl[2] ); 
  }


  public double[][] getUBinverse()
  {
    return UBinverse;
  }


  public static void show_hkls( Vector<IPeakQ> peaks, double[][] UBinverse )
  {
    System.out.println("HKL Values of peaks");
    float q_vecf[] = new float[3];
    double q_vec[] = new double[3];
    double hkl[]   = new double[3];
    for ( int index = 0; index < peaks.size(); index++ )
    {
      q_vecf = peaks.elementAt(index).getUnrotQ();
      for ( int i = 0; i < 3; i++ )
        q_vec[i] = q_vecf[i] * 2 * Math.PI;

      hkl = LinearAlgebra.mult( UBinverse, q_vec );
      System.out.printf("index = %5d   %5.2f  %5.2f  %5.2f\n",
                         index, hkl[0], hkl[1], hkl[2] ); 
    }
  }


  public static double[][] get_hkls( Vector<IPeakQ> peaks, 
                                    double[][] UBinverse )
  {
    float  q_vecf[] = new float[3];
    double q_vec[]  = new double[3];
    double hkls[][] = new double[peaks.size()][3];
    for ( int index = 0; index < peaks.size(); index++ )
    {
      q_vecf = peaks.elementAt(index).getUnrotQ();
      for ( int i = 0; i < 3; i++ )
        q_vec[i] = q_vecf[i] * 2 * Math.PI;

      hkls[index] = LinearAlgebra.mult( UBinverse, q_vec );
    }

    return hkls;
  }


  public void ShowProgress( String message, PrintStream out )
  {
    if ( out == null )
      return;

    out.println();
    out.println("==================================================");
    out.println(message);
    out.println("==================================================");
    out.println( "Number of evaluations = " + eval_count );

    double err;
    double sum_sq_err = 0;
    for ( int i = 0; i < peaks_q_vec.length; i++ )
    {
      err = getValue( i );
      sum_sq_err += err * err;
    }

    out.println( "Sum Squared Error = " + sum_sq_err );
  }


 /* -------------------------------------------------------------------------
  *
  * MAIN  ( Basic main program for testing purposes only. )
  *
  */
  public static void main(String[] args) throws IOException
  {
    // Lattice Parameters 
    double lattice_params[] = new double[6];

/* For Oxalic Acid  (NOT EXACT)
*/
    lattice_params[0] = 6.094;
    lattice_params[2] = 3.601;
    lattice_params[1] = 11.915;
    lattice_params[3] = 90;
    lattice_params[5] = 90;
    lattice_params[4] = 103.2;
//  String all_filename =
//      "/usr2/SNAP_2/OXALIC_SUBSAMPLED/oxalic_subsampled_large_peaks.peaks";

    String all_filename =
        "/usr2/SNAP_2/OXALIC_SUBSAMPLED/oxalic_subsampled_248-257.peaks";

    String filename =
        "/usr2/SNAP_2/OXALIC_SUBSAMPLED/oxalic_subsampled_temp.peaks";

/* For Quartz
    lattice_params[0] = 4.9138;
    lattice_params[2] = 4.9138;
    lattice_params[1] = 5.4051;
    lattice_params[3] = 90;
    lattice_params[5] = 90;
    lattice_params[4] = 120;
*/
//  String filename = "/usr2/SNAP_2/QUARTZ/quartz_test_235_1_half_det.peaks";
//  String filename = "/usr2/SNAP_2/QUARTZ/quartz_test_235_1_det.peaks";
//  String filename = "/usr2/SNAP_2/QUARTZ/quartz_test_235_2_det.peaks";
//  String filename = "/usr2/SNAP_2/QUARTZ/quartz_test_235_3_det.peaks";
//  String filename = "/usr2/SNAP_2/QUARTZ/quartz_test_235_5_det.peaks";
//  String filename = "/usr2/SNAP_2/QUARTZ/quartz_test_235.peaks";
//  String filename = "/usr2/SNAP_2/QUARTZ/quartz_test_235-238.peaks";

    Vector<Peak_new> peak_news = Peak_new_IO.ReadPeaks_new( filename );
    Vector<IPeakQ> peaks = new Vector<IPeakQ>();
    for ( int i = 0; i < peak_news.size(); i++ )
      peaks.add( peak_news.elementAt(i) );

    String[] param_names = { "phi", "chi", "omega" };
    double[] params      = { 40, 30, 60 };


    double                 best_chi_sqr = Double.POSITIVE_INFINITY;
    SCD_OrientationErrorF  best_error_f = null;
    MarquardtArrayFitter   best_fitter  = null;

    SCD_OrientationErrorF  error_f = null;

    Random random = new Random();
    MarquardtArrayFitter fitter = null;
    for ( int count = 0; count < 200; count++ )
    {
      params[0] = 180 * random.nextDouble();
      params[1] =  90 * random.nextDouble();
      params[2] = 360 * random.nextDouble();
      error_f = new SCD_OrientationErrorF( peaks, 
                                           lattice_params,
                                           params,
                                           param_names );

//    String message = "Before fit... params are";
//    error_f.ShowProgress( message, System.out );

      double z_vals[] = new double[ peaks.size() ];
      double sigmas[] = new double[ peaks.size() ];
      double x_index[] = new double[ peaks.size() ];
      for ( int i = 0; i < peaks.size(); i++ )
      {
        z_vals[i] = 0;
        sigmas[i] = 1.0;
/*
        sigmas[i] = Math.sqrt( peaks.elementAt(i).ipkobs() );
        if ( sigmas[i] <= 0 )
          sigmas[i] = 1;
*/
        x_index[i]  = i;
      }
                                           // build the data fitter and display
                                           // the results.
      fitter = new MarquardtArrayFitter( error_f, 
                                         x_index, 
                                         z_vals, 
                                         sigmas, 
                                         1.0e-15, 
                                         1000 );

      double chi_sqr = fitter.getChiSqr();

      if ( chi_sqr < best_chi_sqr ) 
      {
        best_chi_sqr = chi_sqr;
        best_error_f = error_f;
        best_fitter  = fitter;
      }
     
      if ( count % 100 == 10 )
      {
        System.out.println( "RANDOM DIRECTION NUMBER " + count );
        System.out.println( "ChiSqr = " + chi_sqr );
        System.out.println( "BestChiSqr = " + best_chi_sqr ); 
      }
    }

    System.out.println( fitter.getResultsString() );

    System.out.println("BEST CHI SQ = " + best_chi_sqr );
    for ( int i = 0; i < peaks.size(); i++ )
      best_error_f.show_hkl( i );

    double[][] UBinverse = best_error_f.getUBinverse(); 
/*
    Vector<IPeakQ> all_peaks = Peak_new_IO.ReadPeaks_new( all_filename );
    show_hkls( all_peaks, UBinverse );
*/
    double[][]  indexing = best_error_f.get_hkls( peaks, UBinverse );
    System.out.println("*** Using get_hkls, indexing is: ");
    for ( int row = 0; row < indexing.length; row++ )
    {  
      System.out.printf( "%4d", row );
      for ( int col = 0; col < 3; col++ )
        System.out.printf( " %5.2f ", indexing[row][col] );
      System.out.println();
    }
    

  }

}

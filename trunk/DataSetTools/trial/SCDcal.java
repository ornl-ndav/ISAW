/*
 * File:  SCDcal.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2003/07/16 22:31:54  dennis
 *  Initial form of SCD calibration program.  Currently only works
 *  with one detector and one run, but most code needed for working
 *  with multiple detectors and multiple runs is in place.
 *
 */

package  DataSetTools.trial;

import java.io.*;
import java.util.*;
import DataSetTools.math.*;
import DataSetTools.util.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.functions.*;

/**
 * This class implements a parameterized "function" that calculates the
 * difference between the theoretical and observed peak positions in "Q"
 * for SCD, for various values of the instrument parameters.
 * This is used with the Marquardt fitting code to calibrate SCD.
 */

public class SCDcal extends    OneVarParameterizedFunction
                    implements Serializable
{
  public static final int N_PARAMS    = 7;
  public static final int L1_INDEX    = 0;
  public static final int T0_INDEX    = 1;
  public static final int DET_W_INDEX = 2;
  public static final int DET_H_INDEX = 3;
  public static final int DET_X_INDEX = 4;
  public static final int DET_Y_INDEX = 5;
  public static final int DET_Z_INDEX = 6;
                                                  // OLD DETECTOR
  public static final int    N_ROWS   = 85;
  public static final int    N_COLS   = 85;
  public static final double DET_SIZE = 0.25;
  public static final double DET_D    = 0.32;
  public static final double DET_A    = -90.0;
  public static final double L1       = 9.378;
  public static final double DET_Z    = 0.0;

  int    n_peaks;
  int    run[]; 
  int    id[];
  double hkl[][];      // list of hkl triples for ith point in ith row
  double tof[];
  int    row[];
  int    col[];

  double U_observed[][];
  double B_observed[][];
  double B_theoretical[][];

  double qxyz_observed[][];
  double qxyz_theoretical[][];

  UniformGrid_d grid;
  int  eval_count = 0;

  /**
   *  Construct a function defined on the grid of (x,y) values specified, 
   *  using the parameters and parameter names specified.  The grid points
   *  are numbered in a sequence and the point's index in the sequence is 
   *  used as the one variable.
   */
   public SCDcal( int    run[],         
                  int    det_id[],
                  double hkl[][],
                  double tof[], int row[], int col[],
                  double params[], String param_names[] )
   {
     super( "SCDcal", params, param_names );

     n_peaks = run.length;
     this.run = run;
     this.id =  det_id;
     this.hkl = hkl;
     this.tof = tof;
     this.row = row;
     this.col = col;

     // Lattice Parameters for Quartz
     double a = 4.9138;
     double b = 4.9138;
     double c = 5.4051;
     double alpha = 90;
     double beta  = 90;
     double gamma = 120;

     B_theoretical = lattice_calc.A_matrix( a, b, c, alpha, beta, gamma );     
     System.out.println("Material Matrix = " );
     LinearAlgebra.print( B_theoretical );
     B_theoretical = LinearAlgebra.getInverse( B_theoretical ); 

     System.out.println("B_theoretical = " );
     LinearAlgebra.print( B_theoretical );

     double lat_par[] = lattice_calc.LatticeParamsOfUB( B_theoretical );
     System.out.println("Lattice Parameters");
     LinearAlgebra.print( lat_par );

     for ( int i = 0; i < 3; i++ )
       for ( int j = 0; j < 3; j++ )
         B_theoretical[i][j] *= 2*Math.PI;
     System.out.println("Final B_theoretical = " );
     LinearAlgebra.print( B_theoretical );

     qxyz_theoretical = new double[n_peaks][3];
     qxyz_observed    = new double[n_peaks][3];
     setParameters(params);
   }

  /**
   *  Evaluate the value of this model function at the specified index.
   *
   *  @param  x  the index into the list of points at which the model is to be 
   *             evaluated. 
   *
   *  @return the value of the model function, at the specified point 
   */
  public double getValue( double x )
  {
    int    index = (int)Math.round(x);
    double sum   = 0;
    double diff;

    for ( int i = 0; i < 3; i++ )
    {
      diff = qxyz_observed[index][i] - qxyz_theoretical[index][i]; 
      sum += diff * diff;
    }
    return Math.sqrt(sum); 
  }


  /**
   *  Set the new values for the list of parameters for this function.
   *  Set up the new position of the data grid.
   *
   *  @param  parameters  Array containing values to copy into the list of
   *                      parameter values for this function.
   */
  public void setParameters( double parameters[] )
  {
    super.setParameters( parameters );
   
    int id = 1;                                   // one detector for now

    double x = parameters[ DET_X_INDEX ];
    double y = parameters[ DET_Y_INDEX ];
    double z = parameters[ DET_Z_INDEX ];
    double w = parameters[ DET_W_INDEX ];
    double h = parameters[ DET_H_INDEX ];

    Vector3D_d center = new Vector3D_d( x, y, z );
    Vector3D_d y_vec  = new Vector3D_d( 0, 0, 1 ); // For now assume the 
                                                   // detector is vertical
    Vector3D_d x_vec  = new Vector3D_d();
    x_vec.cross( center, y_vec );
    x_vec.normalize();
    grid = new UniformGrid_d( id, "m", center, x_vec, y_vec, w, h, 0.001, 
                              N_ROWS, N_COLS );

//    System.out.println("GRID = " + grid );
    find_qxyz_observed();
    find_U_and_B_observed();
    find_qxyz_theoretical();
  } 

  /**
   *  Calculate the rotation matrix from the list of hkl and observed qxyzs.
   *  NOTE: the observed qxyzs must have been previously calculated.
   */
  private double find_U_and_B_observed()
  {
    double UB[][] = new double[3][3];
    double my_hkl[][]  = copy( hkl );
    double my_qxyz[][] = copy( qxyz_observed );
/*
    System.out.println("UB size = " + UB.length +" X " + UB[0].length );
    System.out.println("my_hkl size = " + my_hkl.length +
                                  " X " + my_hkl[0].length );
    System.out.println("my_qxyz size = " + my_qxyz.length +
                                   " X " + my_qxyz[0].length );
    for ( int i = 0; i < 5; i++ )
    {
      System.out.println( "" + hkl[i][0] + 
                        ", " + hkl[i][1] + 
                        ", " + hkl[i][2] +
                        ", " + my_qxyz[i][0] +
                        ", " + my_qxyz[i][1] +
                        ", " + my_qxyz[i][2] );
                    
    }
    System.out.println("Error in mapping hkl to qxyz = " + error );
*/
    double error = LinearAlgebra.BestFitMatrix( UB, my_hkl, my_qxyz );
    U_observed = lattice_calc.getU( UB );
    B_observed = lattice_calc.getB( UB );

    if ( eval_count % 1000 == 0 )
    {
      System.out.println( ""+eval_count/1000 );
      for ( int k = 0; k < 3; k++ )
        for ( int j = 0; j < 3; j++ )
          UB[k][j] /= (2*Math.PI);
      double cell_params[] = lattice_calc.LatticeParamsOfUB( UB );
      LinearAlgebra.print( cell_params );
      double index[] = new double[n_peaks];
      for ( int i = 0; i < index.length; i++ )
        index[i] = i; 
      double vals[] = getValues( index );
      double s_dev = 0;
      for ( int i = 0; i < vals.length; i++ )
        s_dev += vals[i] * vals[i];
      s_dev = Math.sqrt( s_dev/vals.length );
      System.out.println("1 standard dev error distance in Q = " + s_dev );
    }
    eval_count++;
    return error;
  }

  /**
   *  Calculate the list of observed qxyz values, using the measured tof,
   *  row and col values with the current values of the instrument parameters. 
   */
  private void find_qxyz_observed()
  {
    double l1 = parameters[L1_INDEX];
    double t0 = parameters[T0_INDEX];
    
    DetectorPosition_d position;
    Position3D_d qxyz;
    double       coords[];
    for ( int peak = 0; peak < n_peaks; peak++ )
    {
      position = new DetectorPosition_d( grid.position( row[peak], col[peak] ));
      qxyz     = tof_calc_d.DiffractometerVecQ(position, l1, tof[peak] - t0);  
      coords = qxyz.getCartesianCoords();
      qxyz_observed[peak][0] = coords[0];  
      qxyz_observed[peak][1] = coords[1];  
      qxyz_observed[peak][2] = coords[2];  
    }
  }
  

  /**
   *  Calculate the list of theoretical qxyz values, using the theoretical
   *  "B" matrix and the current observed rotation "U_observed".  These 
   *  are calculated as  UB * hkl = qxyz.  NOTE: U_observed must have been
   *  previously calculated.
   */
  private void find_qxyz_theoretical()
  {
    double UB[][] = LinearAlgebra.mult( U_observed, B_theoretical );
    for ( int peak = 0; peak < n_peaks; peak++ )
      mult_vector( UB, hkl[peak], qxyz_theoretical[peak] );
  }


  /**
   *  Multiply the matrix A[][] times the vector x[], storing result in b[]
   */
  private void mult_vector( double A[][], double x[], double b[] )
  {
    for ( int row = 0; row < x.length; row++ )
    {
      b[row] = 0.0;
      for ( int col = 0; col < x.length; col++ )
        b[row] += A[row][col] * x[col];
    }
  }

  /**
   *  Make a copy of a two-dimensional array of doubles
   */
  private double[][] copy( double A[][] )
  {
    double M[][] = new double[ A.length ][ A[0].length ];
    for ( int i = 0; i < A.length; i++ )
      for ( int j = 0; j < A[0].length; j++ )
        M[i][j] = A[i][j];
    return M;
  }


 /* -------------------------------------------------------------------------
  *
  * MAIN  ( Basic main program for testing purposes only. )
  *
  */
    public static void main(String[] args)
    {
      boolean det_info_read = false;
      int    n_peaks = 0;
      int    line_type;
      int    run[]    = null;
      int    det[]    = null;
      double det_a[]  = null;
      double det_d[]  = null;
      double chi[]    = null;
      double phi[]    = null;
      double omega[]  = null;
      double hkl[][]  = null;
      double tof[]    = null;
      int    row[]    = null; 
      int    col[]    = null;
      double counts[] = null;
      try
      {
        TextFileReader tfr = new TextFileReader( args[0] );
        n_peaks = tfr.read_int();
        run    = new int[n_peaks];
        det    = new int[n_peaks];
        det_a  = new double[n_peaks];
        det_d  = new double[n_peaks];
        chi    = new double[n_peaks];
        phi    = new double[n_peaks];
        omega  = new double[n_peaks];
        det    = new int[n_peaks];
        hkl    = new double[n_peaks][3];
        tof    = new double[n_peaks];
        row    = new int[n_peaks];
        col    = new int[n_peaks];
        counts = new double[n_peaks];
        for ( int i = 0; i < n_peaks; i++ )
        {
          line_type = tfr.read_int();
          System.out.println("Read type : " + line_type );
          if ( line_type == 0 )      // READ HEADER INFO
          {
            tfr.read_line();         // end of line 0

            line_type = tfr.read_int();
            System.out.println("Read type : " + line_type );
            run[i]   = tfr.read_int();
            det[i]   = tfr.read_int(); 
            det_a[i] = tfr.read_double();
            tfr.read_double();        // IGNORE ANGLE 2
            det_d[i] = tfr.read_double();
            chi[i]   = tfr.read_double();
            phi[i]   = tfr.read_double();
            omega[i] = tfr.read_double();
            tfr.read_line();          // end of line 1

            line_type = tfr.read_int();
            System.out.println("Read type : " + line_type );
            tfr.read_line();          // end of line 2A

            line_type = tfr.read_int();
            System.out.println("Read type : " + line_type );
            det_info_read = true;
          }
          if ( !det_info_read )
          {
            run[i]   = run[i-1];
            det[i]   = det[i-1];
            det_a[i] = det_a[i-1];
            det_d[i] = det_d[i-1];
            chi[i]   = chi[i-1];
            phi[i]   = phi[i-1];
            omega[i] = omega[i-1];
          }
          tfr.read_int();             // IGNORE SEQN
          hkl[i][0] = Math.round(tfr.read_double()); 
          hkl[i][1] = Math.round(tfr.read_double()); 
          hkl[i][2] = Math.round(tfr.read_double()); 
          col[i]    = tfr.read_int();
          row[i]    = tfr.read_int();
          tof[i]    = tfr.read_double();
          counts[i] = tfr.read_double();
          tfr.read_line();            // end of line 3
          det_info_read = false; 
        }
      }
      catch ( IOException e )
      {
        System.out.println(e);
      }
                                                   // set up the list of 
                                                   // parameters and names
      String parameter_names[]       = new String[N_PARAMS];
      parameter_names[ L1_INDEX ]    = "L1";
      parameter_names[ T0_INDEX ]    = "T0";
      parameter_names[ DET_W_INDEX ] = "Det Width";
      parameter_names[ DET_H_INDEX ] = "Det Height";
      parameter_names[ DET_X_INDEX ] = "Det center x";
      parameter_names[ DET_Y_INDEX ] = "Det center y";
      parameter_names[ DET_Z_INDEX ] = "Det center z";

      double parameters[] = new double[N_PARAMS];
      parameters[ L1_INDEX ]    = L1;
      parameters[ T0_INDEX ]    = 0.0;
      parameters[ DET_W_INDEX ] = DET_SIZE;
      parameters[ DET_H_INDEX ] = DET_SIZE;
      parameters[ DET_X_INDEX ] = DET_D * Math.cos( DET_A * Math.PI/180 );
      parameters[ DET_Y_INDEX ] = DET_D * Math.sin( DET_A * Math.PI/180 );
      parameters[ DET_Z_INDEX ] = 0.0;
                                                     // build the one variable
                                                     // function
      OneVarParameterizedFunction 
                        error_f = new SCDcal( run, 
                                              det,
                                              hkl,
                                              tof, row, col,
                                              parameters, parameter_names ); 

                                                     // build the array of 
                                                     // function values with  
                                                     // noise and "fake" sigmas
      double z_vals[] = new double[ n_peaks ]; 
      double sigmas[] = new double[ n_peaks ]; 
      double index[]  = new double[ n_peaks ];
      for ( int i = 0; i < n_peaks; i++ )
      {
        z_vals[i] = 0;
//      sigmas[i] = 1.0/Math.sqrt( counts[i] );
//      sigmas[i] = Math.sqrt( counts[i] );
        sigmas[i] = 1.0;
        index[i]  = i;
      }

      double vals[]   = error_f.getValues( index );
      System.out.println("error_f evaluated at all points = ");
      LinearAlgebra.print( vals );
      for ( int k = 0; k < parameters.length; k++ )
      {
        System.out.println("Derivatives with respect to " + parameter_names[k]);
        double derivs[] = error_f.get_dFdai(index,k);
        LinearAlgebra.print( derivs );
      }

//      System.exit(0);
                                           // build the data fitter and display 
                                           // the results.
      MarquardtArrayFitter fitter = 
        new MarquardtArrayFitter(error_f, index, z_vals, sigmas, 1.0e-10, 500);

      double p_sigmas[];
      double p_sigmas_2[];
      double coefs[];
      String names[];
      p_sigmas = fitter.getParameterSigmas();
      p_sigmas_2 = fitter.getParameterSigmas_2();
      coefs = error_f.getParameters();
      names = error_f.getParameterNames();
      for ( int i = 0; i < error_f.numParameters(); i++ )
        System.out.println(names[i] + " = " + coefs[i] +
                           " +- " + p_sigmas[i] +
                           " +- " + p_sigmas_2[i] );
    }
}

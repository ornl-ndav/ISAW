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
 *  Revision 1.5  2003/07/29 21:29:35  dennis
 *  Now uses named constants for indices to all parameters that
 *  can be optimized.  Added detector distance parameter, but either
 *  this, or the detector width and height should be kept fixed.
 *
 *  Revision 1.4  2003/07/29 19:51:22  dennis
 *  Constructor now accepts a list of boolean flags to indicate
 *  which of the parameters are actually used in the optimization.
 *
 *  Revision 1.3  2003/07/29 16:52:03  dennis
 *  Added more parameters, now fits initial path (L1), tof offset and
 *  scale (T0, A), sample shift (SX, SY, SZ), and Width, Height,
 *  x&y offset, and detector orientation for each detector.
 *
 */

package  DataSetTools.trial;

import java.io.*;
import java.util.*;
import DataSetTools.math.*;
import DataSetTools.util.*;
import DataSetTools.instruments.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.functions.*;

/**
 * This class implements a parameterized "function" that calculates the
 * difference between the theoretical and observed peak positions in "Q"
 * for SCD, for various values of the instrument parameters.
 * This is used with the Marquardt fitting code to calibrate SCD.
 */

public class SCDcal   extends    OneVarParameterizedFunction
                      implements Serializable
{
  public static final int L1_INDEX    = 0;
  public static final int T0_INDEX    = 1;
  public static final int A_INDEX     = 2;
  public static final int SX_INDEX    = 3;
  public static final int SY_INDEX    = 4;
  public static final int SZ_INDEX    = 5;
  public static final int DET_BASE_INDEX   = 6;
  public static final int N_DET_PARAMS     = 8; // number of params per detector

  public static final int DET_WIDTH_INDEX  = 0; // relative to the start of the 
  public static final int DET_HEIGHT_INDEX = 1; // parameters for a detector
  public static final int DET_X_OFF_INDEX  = 2;
  public static final int DET_Y_OFF_INDEX  = 3;
  public static final int DET_D_INDEX      = 4;
  public static final int DET_PHI_INDEX    = 5;
  public static final int DET_CHI_INDEX    = 6;
  public static final int DET_OMEGA_INDEX  = 7;

  private int    n_peaks;
  private int    run[]; 
  private int    id[];
  private double hkl[][];      // list of hkl triples for ith point in ith row
  private double tof[];
  private int    row[];
  private int    col[];

  private double U_observed[][];
  private double B_observed[][];
  private double B_theoretical[][];

  private double qxyz_observed[][];
  private double qxyz_theoretical[][];

  private UniformGrid_d grid;
  private int  eval_count = 0;

  private Hashtable gon_rotation_inverse;
  private Hashtable gon_rotation;
  private Hashtable grids;
  private Vector    nominal_position;

  private double    all_parameters[];   // full list of all possible params
  private String    all_parameter_names[];

  private int       all_p_index[];      // index into full list of all params,
                                        // for each used parameter
  private int       used_p_index[];     // index into list of params used, 
                                        // for each possible parameter
  /**
   *  Construct a function defined on the grid of (x,y) values specified, 
   *  using the parameters and parameter names specified.  The grid points
   *  are numbered in a sequence and the point's index in the sequence is 
   *  used as the one variable.
   */
   public SCDcal( 
                  int                 run[],         
                  int                 det_id[],
                  Hashtable           grids,
                  SampleOrientation_d orientation[],
                  double              hkl[][],
                  double              tof[], 
                  int                 row[], 
                  int                 col[],
                  double              params[], 
                  String              param_names[],
                  int                 n_used,
                  boolean             is_used[],
                  double              lattice_params[] )
   {
                                   // we only want to use some of the possible
                                   // parameter, but the super constructor must
                                   // be called first, so we have to pass in
                                   // empty arrays and then change the values
     super( "SCDcal", new double[n_used], new String[n_used] );

     int used_index = 0;                        // set up copies of the 
     for ( int i = 0; i < params.length; i++ )  // sub lists that are actually
       if ( is_used[i] )                        // used for the optimization
       {
         parameters[used_index]      = params[i];
         parameter_names[used_index] = param_names[i];
         used_index++;
       }

     if ( used_index != n_used )
     {
       throw new InstantiationError(
                  "ERROR: number of parameters used " + used_index + 
                  " doesn't match number specified "  + n_used ); 
     }
                                                 // make tables of indices to
     used_p_index = new int[params.length];      // switch between full list 
     all_p_index  = new int[n_used];             // of parameters and sub lists 
     used_index = 0;
     for ( int i = 0; i < params.length; i++ )
       if ( is_used[i] )
       {
         used_p_index[i] = used_index;
         all_p_index[used_index] = i;
         used_index++;
       }
       else
         used_p_index[i] = -1;                  // invalid index, since not used

                               // record references to these arrays and change
                               // the values as the optimization proceeds
     all_parameters = params;
     all_parameter_names = param_names;

     n_peaks = run.length;
     this.run = copy( run );
     this.id =  copy( det_id );
     this.grids = grids;
     this.hkl = copy( hkl );
     this.tof = copy( tof );
     this.row = copy( row );
     this.col = copy( col );       // to be really thorough, we should probably
                                   // also copy the grids Hashtable and the
                                   // orientation array, however, the order of
                                   // the values in the grids hashtable is
                                   // critical, and the copy may disturb it.

     // Lattice Parameters for Quartz
     B_theoretical = lattice_calc.A_matrix( lattice_params );     
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

     gon_rotation_inverse = new Hashtable();
     gon_rotation         = new Hashtable();
     for ( int i = 0; i < run.length; i++ )
     {
       Integer key = new Integer( run[i] );
       Object  val = gon_rotation_inverse.get( key );
       if ( val == null )
       { 
         Tran3D_d tran = orientation[i].getGoniometerRotationInverse();
         double rotation[][] = new double[3][3];
         for ( int k = 0; k < 3; k++ )
           for ( int j = 0; j < 3; j++ )
             rotation[k][j] = tran.get()[k][j]; 
         gon_rotation_inverse.put( key, rotation );

         tran = orientation[i].getGoniometerRotation();
         rotation = new double[3][3];
         for ( int k = 0; k < 3; k++ )
           for ( int j = 0; j < 3; j++ )
             rotation[k][j] = tran.get()[k][j];
         gon_rotation.put( key, rotation );
       }
     }

     nominal_position = new Vector();
     Enumeration e = grids.elements();
     while ( e.hasMoreElements() )
       nominal_position.add( ((UniformGrid_d)e.nextElement()).position() ); 

     qxyz_theoretical = new double[n_peaks][3];
     qxyz_observed    = new double[n_peaks][3];

     setParameters(parameters);   // This may seem circular, but it forces the
                                  // local version to set up initial values
                                  // for qxyz_theoretical, observed, UB, etd.
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

    // copy the parameters that are being used for optimization into to master
    // list of parameters and then actually use all values from the master list.

    for ( int i = 0; i < parameters.length; i++ )
      all_parameters[ all_p_index[i] ] = parameters[i];

    int det_count = 0;
    int index     = DET_BASE_INDEX;
    Enumeration e = grids.elements();
    while ( e.hasMoreElements() )
    {
      index = DET_BASE_INDEX + det_count * N_DET_PARAMS;

      UniformGrid_d grid = (UniformGrid_d)e.nextElement();
      Vector3D_d nom_pos = (Vector3D_d)nominal_position.elementAt(det_count);
      double width  = all_parameters[ index + DET_WIDTH_INDEX  ];
      double height = all_parameters[ index + DET_HEIGHT_INDEX ];
      double x_off  = all_parameters[ index + DET_X_OFF_INDEX  ];
      double y_off  = all_parameters[ index + DET_Y_OFF_INDEX  ];
      double det_d  = all_parameters[ index + DET_D_INDEX      ];
      double phi    = all_parameters[ index + DET_PHI_INDEX    ];
      double chi    = all_parameters[ index + DET_CHI_INDEX    ];
      double omega  = all_parameters[ index + DET_OMEGA_INDEX  ];

      grid.setWidth( width );
      grid.setHeight( height );
    
      Vector3D_d center       = new Vector3D_d( nom_pos );
      Vector3D_d minus_z_vec  = new Vector3D_d( center );
      minus_z_vec.normalize();
      Vector3D_d y_vec  = new Vector3D_d( 0, 0, 1 );
      Vector3D_d x_vec  = new Vector3D_d();
      x_vec.cross( minus_z_vec, y_vec );

      Vector3D_d x_shift = new Vector3D_d( x_vec );
      Vector3D_d y_shift = new Vector3D_d( y_vec );
      x_shift.multiply( x_off );
      y_shift.multiply( y_off );
      center.normalize();
      center.multiply( det_d );
      center.add( x_shift ); 
      center.add( y_shift ); 
      grid.setCenter( center );

      Tran3D_d euler_rotation = tof_calc_d.makeEulerRotation(phi, chi, omega);
      euler_rotation.apply_to( x_vec, x_vec );
      euler_rotation.apply_to( y_vec, y_vec );
      grid.setOrientation( x_vec, y_vec );

      det_count++;
    }
   
    find_qxyz_observed();
    find_U_and_B_observed();
    find_qxyz_theoretical();

    show_progress( B_observed, false );
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

    double error = LinearAlgebra.BestFitMatrix( UB, my_hkl, my_qxyz );
    U_observed = lattice_calc.getU( UB );
    B_observed = lattice_calc.getB( UB );

    return error;
  }

  /**
   *  Calculate the list of observed qxyz values, using the measured tof,
   *  row and col values with the current values of the instrument parameters. 
   */
  private void find_qxyz_observed()
  {
    double l1 = all_parameters[L1_INDEX];
    double t0 = all_parameters[T0_INDEX];
    double a  = all_parameters[A_INDEX];
    double sx = all_parameters[SX_INDEX];
    double sy = all_parameters[SY_INDEX];
    double sz = all_parameters[SZ_INDEX];

    DetectorPosition_d pixel_position;
    Vector3D_d         pixel_vec;
    Vector3D_d         sample_shift;
    double             sample_shift_array[] = {-sx, -sy, -sz};
    double             rotation[][] = null;
    double             rotation_inv[][] = null;
    Position3D_d qxyz;
    double       coords[] = new double[3];
    for ( int peak = 0; peak < n_peaks; peak++ )
    {
      rotation = (double[][])gon_rotation.get( new Integer( run[peak] ) );
      mult_vector( rotation, sample_shift_array, coords );
      sample_shift = new Vector3D_d( coords );

      UniformGrid_d grid = (UniformGrid_d)grids.get( new Integer( id[peak] ) );
      pixel_vec = grid.position( row[peak], col[peak] ); 
      pixel_vec.add( sample_shift );
      pixel_position = new DetectorPosition_d( pixel_vec );

      qxyz = tof_calc_d.DiffractometerVecQ(pixel_position, l1, a*tof[peak]+t0); 
      coords = qxyz.getCartesianCoords();
      rotation_inv = (double[][])
                     gon_rotation_inverse.get( new Integer( run[peak] ) );
      mult_vector( rotation_inv, coords, qxyz_observed[peak] );
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
   *  Make a copy of a one-dimensional array of doubles
   */
  private double[] copy( double A[] )
  {
    double M[] = new double[ A.length ];
    for ( int i = 0; i < A.length; i++ )
      M[i] = A[i];
    return M;
  }


  /**
   *  Make a copy of a one-dimensional array of ints
   */
  private int[] copy( int A[] )
  {
    int M[] = new int[ A.length ];
    for ( int i = 0; i < A.length; i++ )
      M[i] = A[i];
    return M;
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

  /**
   *
   */
  private void show_progress( double B[][], boolean show )
  {
    if ( eval_count % 1000 == 0 || show )
    {                                  // first show observed cell parameters
                                       // for current stage of calibration
      double my_B[][] = copy ( B );
      System.out.println( ""+eval_count/1000 );
      for ( int k = 0; k < 3; k++ )
        for ( int j = 0; j < 3; j++ )
          my_B[k][j] /= (2*Math.PI);

      double cell_params[] = lattice_calc.LatticeParamsOfUB( my_B );
      System.out.println("==================================================");
      System.out.println("Observed cell parameters now are.... "); 
      LinearAlgebra.print( cell_params );
                                      
                                       // then show the standard deviation of
                                       // the error in q, from all zeros
      double index[] = new double[n_peaks];
      for ( int i = 0; i < index.length; i++ )
        index[i] = i;

      double vals[] = getValues( index );
      double s_dev = 0;
      for ( int i = 0; i < vals.length; i++ )
        s_dev += vals[i] * vals[i];

      s_dev = Math.sqrt( s_dev/vals.length );

      System.out.println();
      System.out.println("1 standard dev error distance in Q = " + s_dev );

                                       // finally, dump out the current
                                       // parameter estimates
      int det_count = 0;
      System.out.println();
      System.out.println("Instrument & Sample parameters: " );
      for ( int i = 0; i < all_parameters.length; i ++ )
      {
        if ( (i - DET_BASE_INDEX) % N_DET_PARAMS == 0 )
        {
          System.out.println();
          System.out.println();
          System.out.println("Parameters for Detector " + det_count );
          det_count++;
        }
        if ( used_p_index[i] >= 0 )
          System.out.print( " " + Format.real(all_parameters[i], 12, 5 ) );
      }
      System.out.println();
    }
    eval_count++;
  }


  static private void show_parameter( String name, 
                                      double value, 
                                      double sig1  )
  {
    System.out.print( Format.string(name,17)  );
    System.out.print( Format.real(value,20,9) + "  +-" );
    System.out.print( Format.real(sig1, 20,9) );
    System.out.println();
  }

 /* -------------------------------------------------------------------------
  *
  * MAIN  ( Basic main program for testing purposes only. )
  *
  */
    public static void main(String[] args)
    {
     // Lattice Parameters for Quartz
      double lattice_params[] = new double[6];
      lattice_params[0] = 4.9138;
      lattice_params[1] = 4.9138;
      lattice_params[2] = 5.4051;
      lattice_params[3] = 90;
      lattice_params[4] = 90;
      lattice_params[5] = 120;


      final int N_ROWS      = 100;       // Detector parameters, used for 
      final int N_COLS      = 100;       // setting up DataGrids must come
      final double DET_SIZE = 0.15;      // from somewhere.
      final double L1       = 9.378;

      boolean det_info_read = false;
      int    n_peaks = 0;
      int    line_type;
      int    run[]    = null;
      int    det[]    = null;
      double det_a;
      double det_d;
      double chi;
      double phi;
      double omega;
      double hkl[][]  = null;
      double tof[]    = null;
      int    row[]    = null; 
      int    col[]    = null;
      double counts[] = null;
      SampleOrientation_d orientation[]  = null;
      DetectorPosition_d  det_position[] = null;
      Hashtable grids = new Hashtable();
      try
      {
        TextFileReader tfr = new TextFileReader( args[0] );
        n_peaks = tfr.read_int();
        run    = new int[n_peaks];
        det    = new int[n_peaks];
        det    = new int[n_peaks];
        hkl    = new double[n_peaks][3];
        tof    = new double[n_peaks];
        row    = new int[n_peaks];
        col    = new int[n_peaks];
        counts = new double[n_peaks];
        orientation  = new SampleOrientation_d[n_peaks];
        det_position = new DetectorPosition_d[n_peaks];

        for ( int i = 0; i < n_peaks; i++ )
        {
          line_type = tfr.read_int();
          if ( line_type == 0 )      // READ HEADER INFO
          {
            tfr.read_line();         // end of line 0

            line_type = tfr.read_int();
            run[i]   = tfr.read_int();
            det[i]   = tfr.read_int(); 
            det_a = tfr.read_double();
            tfr.read_double();        // IGNORE ANGLE 2
            det_d = tfr.read_double();
            det_position[i] = new DetectorPosition_d();
            det_position[i].setSphericalCoords( det_d, 
                                                det_a * Math.PI/180,
                                                0.0 ); 
            chi   = tfr.read_double();
            phi   = tfr.read_double();
            omega = tfr.read_double();
            orientation[i] = new IPNS_SCD_SampleOrientation_d((float)phi, 
                                                            (float)chi, 
                                                            (float)omega);
            tfr.read_line();          // end of line 1

            line_type = tfr.read_int();
            tfr.read_line();          // end of line 2A

            line_type = tfr.read_int();
            det_info_read = true;
            Integer key = new Integer( det[i] );
            if ( grids.get(key) == null )            // new detector, so add it
            {
              Vector3D_d center = new Vector3D_d( 
                                       det_d *  Math.cos(det_a * Math.PI/180),
                                       det_d *  Math.sin(det_a * Math.PI/180),
                                       0  );
              Vector3D_d y_vec = new Vector3D_d( 0, 0, 1 ); 
              Vector3D_d x_vec = new Vector3D_d(); 
              x_vec.cross( center, y_vec );
              x_vec.normalize();
              UniformGrid_d grid = new UniformGrid_d
                                   ( det[i], "m", 
                                     center, x_vec, y_vec, 
                                     DET_SIZE, DET_SIZE, 0.001, 
                                     N_ROWS, N_COLS );
              grids.put( key, grid );
              System.out.println("grid = " + grid );
            }
          }
          if ( !det_info_read )
          {
            run[i]   = run[i-1];
            det[i]   = det[i-1];
            det_position[i] = det_position[i-1];
            orientation[i]  = orientation[i-1];
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

      int n_params = DET_BASE_INDEX + N_DET_PARAMS * grids.size();
      System.out.println("NUMBER OF PARAMETERS = " + n_params );

      String parameter_names[]     = new String[n_params];
      parameter_names[ L1_INDEX ]  = "L1";
      parameter_names[ T0_INDEX ]  = "T0";
      parameter_names[ A_INDEX  ]  = "A(tof=A*t+T0)";
      parameter_names[ SX_INDEX ]  = "SX";
      parameter_names[ SY_INDEX ]  = "SY";
      parameter_names[ SZ_INDEX ]  = "SZ";

      double parameters[] = new double[n_params];
      parameters[ L1_INDEX ]  = L1;
      parameters[ T0_INDEX ]  = 0.0;
      parameters[ A_INDEX  ]  = 1.0;
      parameters[ SX_INDEX ]  = 0.0;
      parameters[ SY_INDEX ]  = 0.0;
      parameters[ SZ_INDEX ]  = 0.0;

      int index     = DET_BASE_INDEX;
      int det_count = 0;
      Enumeration e = grids.elements();
      while ( e.hasMoreElements() )
      {
        index = DET_BASE_INDEX + det_count * N_DET_PARAMS;

        UniformGrid_d grid = (UniformGrid_d)e.nextElement();
        int id = grid.ID();
        parameter_names[index + DET_WIDTH_INDEX ] = "Det " + id + "    Width";
        parameters     [index + DET_WIDTH_INDEX ] = grid.width();

        parameter_names[index + DET_HEIGHT_INDEX] = "Det " + id + "   Height";
        parameters     [index + DET_HEIGHT_INDEX] = grid.height();

        parameter_names[index + DET_X_OFF_INDEX ] = "Det " + id + " x_offset";
        parameters     [index + DET_X_OFF_INDEX ] = 0;

        parameter_names[index + DET_Y_OFF_INDEX ] = "Det " + id + " y_offset";
        parameters     [index + DET_Y_OFF_INDEX ] = 0;

        parameter_names[index + DET_D_INDEX     ] = "Det " + id + " distance";
        parameters     [index + DET_D_INDEX     ] = grid.position().length();

        parameter_names[index + DET_PHI_INDEX   ] = "Det " + id + "      phi";
        parameters     [index + DET_PHI_INDEX   ] = 0;

        parameter_names[index + DET_CHI_INDEX   ] = "Det " + id + "      chi";
        parameters     [index + DET_CHI_INDEX   ] = 0;

        parameter_names[index + DET_OMEGA_INDEX ] = "Det " + id + "    omega";
        parameters     [index + DET_OMEGA_INDEX ] = 0;

        det_count++;
      } 

      boolean is_used[] = new boolean[n_params];
      for ( int i = 0; i < n_params; i++ )
        is_used[i] = true;
                              // Insert code at this point to mark some params.
                              // First turn off any params shared by all dets.
    //  is_used[ A_INDEX ] = false;
                              // then turn off some params for all detectors.
      for ( int i = 0; i < det_count; i++ )   
      {
        index = DET_BASE_INDEX + i * N_DET_PARAMS;
        is_used[ index + DET_D_INDEX      ] = false;
    //  is_used[ index + DET_WIDTH_INDEX  ] = false;
    //  is_used[ index + DET_HEIGHT_INDEX ] = false;
      }
                              // now count the number that were used
      int n_used = 0;
      for ( int i = 0; i < n_params; i++ )
        if ( is_used[i] )
          n_used++;
                                                     // build the one variable
                                                     // function
      SCDcal error_f = new SCDcal( run, 
                                   det,
                                   grids,
                                   orientation,
                                   hkl,
                                   tof, row, col,
                                   parameters, parameter_names,
                                   n_used, is_used,
                                   lattice_params ); 

                                                     // build the array of 
                                                     // function values with  
                                                     // noise and "fake" sigmas
      double z_vals[] = new double[ n_peaks ]; 
      double sigmas[] = new double[ n_peaks ]; 
      double x_index[] = new double[ n_peaks ];
      for ( int i = 0; i < n_peaks; i++ )
      {
        z_vals[i] = 0;
//      sigmas[i] = Math.sqrt( counts[i] );
        sigmas[i] = 1.0;
        x_index[i]  = i;
      }
/*
      double vals[]   = error_f.getValues( x_index );
      System.out.println("error_f evaluated at all points = ");
      LinearAlgebra.print( vals );
      for ( int k = 0; k < parameters.length; k++ )
      {
        System.out.println("Derivatives with respect to " + parameter_names[k]);
        double derivs[] = error_f.get_dFdai(x_index,k);
        LinearAlgebra.print( derivs );
      }
*/
//      System.exit(0);
                                           // build the data fitter and display 
                                           // the results.
      MarquardtArrayFitter fitter = 
      new MarquardtArrayFitter(error_f, x_index, z_vals, sigmas, 1.0e-16, 100);

      double p_sigmas[];
      double coefs[];
      String names[];
      p_sigmas = fitter.getParameterSigmas();
      coefs = error_f.getParameters();
      names = error_f.getParameterNames();
      for ( int i = 0; i < error_f.numParameters(); i++ )
        show_parameter( names[i], coefs[i], p_sigmas[i] );

      System.out.println();
      System.out.println("RESULTS -----------------------------------------"); 
      System.out.println("observed U matrix:");
      LinearAlgebra.print( error_f.U_observed );
      System.out.println("observed B matrix:");
      LinearAlgebra.print( error_f.B_observed );
      System.out.println("observed UB matrix:");
      double UB[][] = LinearAlgebra.mult( error_f.U_observed, 
                                          error_f.B_observed );
      LinearAlgebra.print( UB );

      System.out.println("Transpose of observed UB/(2PI)");
      for ( int i = 0; i < 3; i++ )
        for ( int j = 0; j < 3; j++ )
          UB[i][j] /= (2*Math.PI);
      UB = LinearAlgebra.getTranspose( UB );
      LinearAlgebra.print( UB );

      System.out.println();
      error_f.show_progress( error_f.B_observed, true );

      det_count = 0;
      e = grids.elements();
      while ( e.hasMoreElements())
      {
        UniformGrid_d grid = (UniformGrid_d)e.nextElement();
        System.out.println();
        System.out.println("USING DETECTOR: " + grid.ID());
        System.out.println();

        System.out.print( Format.real( 100*parameters[L1_INDEX], 9, 3 ) );
        System.out.print( Format.real( parameters[T0_INDEX], 8, 3 ) );
                                                 
        index = DET_BASE_INDEX + det_count * N_DET_PARAMS;
                                                       // width and height are
                                                       // from outer edge to 
                                                       // outer edge.
        double x2cm = 100*parameters[ index + DET_WIDTH_INDEX ]  / N_COLS; 
        double y2cm = 100*parameters[ index + DET_HEIGHT_INDEX ] / N_ROWS;  

                                                //this is to edge of detector
                                                //not center of first pixel
        double xLeft  = 100*parameters[index + DET_X_OFF_INDEX] 
                        - x2cm*N_COLS/2.0;

        double yLower = 100*parameters[index + DET_Y_OFF_INDEX]
                        - y2cm*N_ROWS/2.0;

        phi   = parameters[ index + DET_PHI_INDEX ];
        chi   = parameters[ index + DET_CHI_INDEX ];
        omega = parameters[ index + DET_OMEGA_INDEX ];
        det_count++;

        System.out.print( Format.real( x2cm, 11, 6 ) );
        System.out.print( Format.real( y2cm, 11, 6 ) );
        System.out.print( Format.real( xLeft,  11, 6 ) );
        System.out.print( Format.real( yLower, 11, 6 ) );
        System.out.println();      
        System.out.print( Format.real( phi, 11, 6 ) );
        System.out.print( Format.real( chi, 11, 6 ) );
        System.out.print( Format.real( omega, 11, 6 ) );
        System.out.println();      
      } 
    }
}

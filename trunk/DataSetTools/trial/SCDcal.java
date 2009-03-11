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
 *  Revision 1.19  2007/06/07 20:24:03  dennis
 *  The ShowProgress method now has an extra parameter that provides a
 *  String message that is printed before the progress info.
 *  Added check for null stream before printing in ShowOldCalibraionInfo
 *  method.
 *
 *  Revision 1.18  2006/01/16 04:23:05  dennis
 *  Added parameter to constructor so that the calibration process
 *  can apply to both the LANSCE and IPNS SCD.
 *
 *  Revision 1.17  2004/07/26 21:47:10  dennis
 *  Changed name of PeakData to PeakData_d since PeakData_d uses
 *  double precision.
 *
 *  Revision 1.16  2004/05/03 16:27:02  dennis
 *  Removed unused local variables and two private methods that are
 *  no longer used.
 *
 *  Revision 1.15  2004/04/15 15:02:41  dennis
 *  Refined the calculation of the shifted detector center so it will
 *  also work for detectors not centered on the 'scattering plane'.
 *  Added methods getAllGrids() and getStandardDeviationInQ() to
 *  provide information to the top level SCDcalib operator for
 *  logging purposes.
 *
 *  Revision 1.14  2004/04/02 17:47:51  dennis
 *  Added method getAllGridIDs() to get the list of IDs used in the
 *  calibration.  Modified getMeasuredPeakPositions() and
 *  getTheoreticalPeakPositions() to include the detector ID as
 *  a parameters and return the list of positions with NULL for
 *  data that did not correspond to the specified detector ID.
 *
 *  Revision 1.13  2004/04/02 15:29:21  dennis
 *  Added method getMeasuredPeakPositions() to return the measured
 *  peaks (row,col,tof) values used in the calibration.
 *  Added method getTheoreticalPeakPositions() to return the theoretical
 *  (row,col,tof) values calculated using the new calibrated instrument
 *  parameters.
 *
 *  Revision 1.12  2004/04/01 20:59:00  dennis
 *  Added log file PrintStream to constructor.  Modified methods
 *  ShowProgress() and ShowOldCalibrationInfo() to print to log file.
 *  Made some improvements to form of messages printed.
 *
 *  Revision 1.11  2004/03/15 06:10:54  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.10  2004/03/15 03:28:44  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.9  2003/08/04 15:51:14  dennis
 *  Temporarily made U_observed[][], B_observed[][] and B_theoretical[][]
 *  public, for use by the SCDcalib operator that is in another package.
 *
 *  Revision 1.8  2003/08/01 13:29:55  dennis
 *  Reordered quartz lattice parameters to match order in peaks
 *  file produced by "Blind".  Altered which parameters are actually
 *  used in the optimization.
 *
 *  Revision 1.7  2003/07/31 14:15:58  dennis
 *  Switched to use a vector of PeakData objects, rather than
 *  parallel arrays of values.
 *
 *  Revision 1.6  2003/07/29 22:34:21  dennis
 *  Made row[] and col[] values for peaks double instead of integer,
 *  in preparation to work with centroided peaks.
 *  Added method ShowOldCalibrationInfo() to print out the calibration
 *  information currently used by the SCD software, for each detector.
 *  Made ShowProgress method public.
 *  Main program now displays results using fitter.getResultsString()
 *  convenience method.
 *
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

import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Functions.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Numeric.*;

import java.io.*;
import java.util.*;
import DataSetTools.math.*;
import DataSetTools.dataset.*;
import DataSetTools.instruments.*;

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

  private PrintStream log_file = null;

  private Vector peaks_vector;
  private int    n_peaks;
  private int    run[]; 
  private int    id[];
  private double hkl[][];      // list of hkl triples for ith point in ith row
  private double tof[];
  private double row[];
  private double col[];

  public double U_observed[][];
  public double B_observed[][];
  public double B_theoretical[][];
  public double A_matrix[][];

  private double qxyz_observed[][];
  private double qxyz_theoretical[][];

  private int  eval_count = 0;

  private Hashtable gon_rotation_inverse;
  private Hashtable gon_rotation;
  private Hashtable grids;
  private Vector3D_d[]  nominal_position;
  private Vector3D_d[]  nominal_base_vec;
  private Vector3D_d[]  nominal_up_vec;
  private double    standard_dev_in_Q = Double.MAX_VALUE;    

  private double    all_parameters[];   // full list of all possible params
  private String    all_parameter_names[];

  private int       all_p_index[];      // index into full list of all params,
                                        // for each used parameter
  private int       used_p_index[];     // index into list of params used, 
                                        // for each possible parameter

  private String    instrument_type;    // PeakData_d.SNS_SCD,
                                        // PeakData_d.IPNS_SCD or
                                        // PeakData_d.LANSCE_SCD
  /**
   *  Construct a function defined on the grid of (x,y) values specified, 
   *  using the parameters and parameter names specified.  The grid points
   *  are numbered in a sequence and the point's index in the sequence is 
   *  used as the one variable.
   */
   public SCDcal( String      instrument_type,
                  Vector      peaks_vector,
                  Hashtable   grids,
                  double      params[], 
                  String      param_names[],
                  int         n_used,
                  boolean     is_used[],
                  double      lattice_params[],
                  PrintStream log_file )
   {
                                   // we only want to use some of the possible
                                   // parameter, but the super constructor must
                                   // be called first, so we have to pass in
                                   // empty arrays and then change the values
     super( "SCDcal", new double[n_used], new String[n_used] );
     this.log_file = log_file;

     this.instrument_type = instrument_type;
     this.peaks_vector = peaks_vector;

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

     n_peaks = peaks_vector.size();
     run = new int[n_peaks];
     id =  new int[n_peaks];
     hkl = new double[n_peaks][3];
     row = new double[n_peaks];
     col = new double[n_peaks];      
     tof = new double[n_peaks];
     for ( int i = 0; i < n_peaks; i++ )
     {
       PeakData_d peak = (PeakData_d)peaks_vector.elementAt(i);
       run[i]    = peak.run_num;
       id [i]    = peak.grid.ID();
       hkl[i][0] = Math.round(peak.h);
       hkl[i][1] = Math.round(peak.k);
       hkl[i][2] = Math.round(peak.l);
       row[i]    = peak.row;
       col[i]    = peak.col;
       tof[i]    = peak.tof;
     }
     this.grids = grids;
                                   // to be really thorough, we should probably
                                   // also copy the grids Hashtable however, 
                                   // the order of the values in the grids
                                   // hashtable is critical, and the copy 
                                   // may disturb it.

     A_matrix = lattice_calc.A_matrix( lattice_params );
     System.out.println("A_matrix = " );
     LinearAlgebra.print( A_matrix );

     B_theoretical = LinearAlgebra.getInverse( A_matrix ); 
     System.out.println("B_theoretical = " );
     LinearAlgebra.print( B_theoretical );

     double lat_par[] = lattice_calc.LatticeParamsOfUB( B_theoretical );
     System.out.println("Lattice Parameters");
     LinearAlgebra.print( lat_par );

     for ( int i = 0; i < 3; i++ )
       for ( int j = 0; j < 3; j++ )
         B_theoretical[i][j] *= 2*Math.PI;
     System.out.println("2 * PI * B_theoretical = " );
     LinearAlgebra.print( B_theoretical );

                                    // find all of the distinct runs, and 
                                    // save the goniometer rotation and its
                                    // inverse in hashtables, using the
                                    // run number as the key.
     gon_rotation_inverse = new Hashtable();
     gon_rotation         = new Hashtable();
     for ( int i = 0; i < run.length; i++ )
     {
       PeakData_d peak = (PeakData_d)peaks_vector.elementAt(i); 
       Integer key = new Integer( run[i] );
       Object  val = gon_rotation_inverse.get( key );
       if ( val == null )
       { 
         Tran3D_d tran = peak.orientation.getGoniometerRotationInverse();
         double rotation[][] = new double[3][3];
         for ( int k = 0; k < 3; k++ )
           for ( int j = 0; j < 3; j++ )
             rotation[k][j] = tran.get()[k][j]; 
         gon_rotation_inverse.put( key, rotation );

         tran = peak.orientation.getGoniometerRotation();
         rotation = new double[3][3];
         for ( int k = 0; k < 3; k++ )
           for ( int j = 0; j < 3; j++ )
             rotation[k][j] = tran.get()[k][j];
         gon_rotation.put( key, rotation );
       }
     }

     nominal_position = new Vector3D_d[ grids.size() ];
     nominal_base_vec = new Vector3D_d[ grids.size() ];
     nominal_up_vec   = new Vector3D_d[ grids.size() ];
     int index = 0;
     Enumeration e = grids.elements();
     while ( e.hasMoreElements() )
     { 
       UniformGrid_d grid = (UniformGrid_d)(e.nextElement());
       nominal_position[index] = grid.position();
       nominal_base_vec[index] = grid.x_vec();
       nominal_up_vec[index]   = grid.y_vec();
       index++;
     }

     System.out.println("GRIDS position, base and up vectors are:");
     for ( int i = 0; i < nominal_position.length; i++ )
     {
       System.out.println(":::::::::::::::: " + i + " ::::::::::::::::::::" );
       System.out.println( nominal_position[i] );
       System.out.println( nominal_base_vec[i] );
       System.out.println( nominal_up_vec[i] );
     }

     qxyz_theoretical = new double[n_peaks][3];
     qxyz_observed    = new double[n_peaks][3];

     setParameters(parameters);   // This may seem circular, but it forces the
                                  // local version to set up initial values
                                  // for qxyz_theoretical, observed, UB, etd.

     /*
     for ( int i = 0; i < n_peaks; i++ )
     {
       System.out.println(""+i);
       LinearAlgebra.print( hkl[i] );
       LinearAlgebra.print( qxyz_observed[i] );
       LinearAlgebra.print( qxyz_theoretical[i] );
     }
     */
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
      Vector3D_d nom_pos      = nominal_position[det_count];
      Vector3D_d nom_base_vec = nominal_base_vec[det_count];
      Vector3D_d nom_up_vec   = nominal_up_vec[det_count];
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
/*
      // NOTE: using this first block, the x and y offsets are interpreted 
      //       as follows:  The y offset is in the vertical direction.  The
      //       x offset is in a horizontal direction perpendicular to the 
      //       line from the detector center to the sample.  This must be 
      //       changed if the detector is not in the horizontal plane.
      //       Offsets are measured from the nominal position of the detector.
      //
      Vector3D_d center      = new Vector3D_d( nom_pos );
      Vector3D_d minus_z_vec = new Vector3D_d( center );
      minus_z_vec.normalize();
      Vector3D_d y_vec = new Vector3D_d( 0, 0, 1 );
      Vector3D_d x_vec = new Vector3D_d();
      x_vec.cross( minus_z_vec, y_vec );
      x_vec.normalize();

      Vector3D_d x_shift = new Vector3D_d( x_vec );
      Vector3D_d y_shift = new Vector3D_d( y_vec );
      x_shift.multiply( x_off );
      y_shift.multiply( y_off );
      center.normalize();
      center.multiply( det_d );
      center.add( x_shift ); 
      center.add( y_shift ); 
      grid.setCenter( center );
*/
/*
      // NOTE: using this second block, the x and y offsets are interpreted 
      //       as follows:  The x offset is in a horizontal plane perpendicular
      //       to the line from the nominal center to the sample.  The
      //       y offset is in a direction perpendicular to the x offset 
      //       direction and perpendicular to the line from the detector 
      //       center to the sample.  This version should work provided the
      //       nominal detector center is not directly above or below the 
      //       sample.  HOWEVER, this is only appropriate if the detector face
      //       is perpendicular to the line from the center of the detector
      //       to the sample.  THIS DOES NOT WORK PROPERLY FOR THE SNAP
      //       instrument at the SNS!
      //       Offsets are measured from the nominal position of the detector.
      //
      Vector3D_d center      = new Vector3D_d( nom_pos );
      Vector3D_d minus_z_vec = new Vector3D_d( center );
      minus_z_vec.normalize();
      Vector3D_d vert_vec = new Vector3D_d( 0, 0, 1 );
      Vector3D_d x_vec  = new Vector3D_d();
      x_vec.cross( minus_z_vec, vert_vec );
      Vector3D_d y_vec = new Vector3D_d();
      y_vec.cross( x_vec, minus_z_vec );
      x_vec.normalize();
      y_vec.normalize();

      Vector3D_d x_shift = new Vector3D_d( x_vec );
      Vector3D_d y_shift = new Vector3D_d( y_vec );
      x_shift.multiply( x_off );
      y_shift.multiply( y_off );
      center.normalize();
      center.multiply( det_d );
      center.add( x_shift ); 
      center.add( y_shift ); 
      grid.setCenter( center );

      if ( phi != 0 || chi != 0 || omega != 0 )
      {
        Tran3D_d euler_rotation = tof_calc_d.makeEulerRotation(phi, chi, omega);
        euler_rotation.apply_to( x_vec, x_vec );
        euler_rotation.apply_to( y_vec, y_vec );
        grid.setOrientation( x_vec, y_vec );
      }
*/
      // NOTE: This third block should work in most cases.  det_d, x,y 
      //       offsets, and Euler angles are interpreted as explained below.
      //       The x,y offsets and Euler angles are applied to the nominal
      //       position and orientation obtained from the initial detector 
      //       grid that is passed in to the constructor.  
      //       det_d is the length of the line from the sample to the shifted
      //       grid center.  In general the grid's base(x_vec) and up(y_vec)
      //       vectors will NOT be perpendicular to this line, so the x and
      //       y offsets can't be interpreted as shifts in these directions,
      //       To avoid this problem, a new vector, x_perp, is constructed
      //       so that it is in the same general direction as the grid's
      //       x_vec, and is in the same plane as x_vec and the line from 
      //       the grid center to the the sample, but is perpendicular to
      //       the line from the center to the sample.  Similarly, a new
      //       vector y_perp is constructed to be in the same general direction
      //       as the grid's y_vec, but is perpendicular to the line from
      //       the sample to the grid center.  x_perp and y_perp are based
      //       on the grid's initial position.
      //       x offset is an offset in the direction of x_perp.
      //       y offset is an offset in the direction of y_perp.
      //       AFTER shifting the center according to the x and y offset
      //       values, the center is scaled to have length det_d.  This 
      //       will change the x and y offsets slightly.
      //
      Vector3D_d center      = new Vector3D_d( nom_pos );
      Vector3D_d minus_z_vec = new Vector3D_d( center );
      minus_z_vec.normalize();

      Vector3D_d x_perp = new Vector3D_d( nom_base_vec );
      double axial_comp = x_perp.dot( minus_z_vec );
      Vector3D_d delta  = new Vector3D_d( minus_z_vec );
      delta.multiply( -axial_comp );
      x_perp.add( delta );
      x_perp.normalize();
      x_perp.multiply( x_off );

      Vector3D_d y_perp = new Vector3D_d( nom_up_vec );
      axial_comp = y_perp.dot( minus_z_vec );
      delta  = new Vector3D_d( minus_z_vec );
      delta.multiply( -axial_comp );
      y_perp.add( delta );
      y_perp.normalize();
      y_perp.multiply( y_off );

      center.add( x_perp );
      center.add( y_perp );
      center.normalize();
      center.multiply( det_d );
      grid.setCenter( center );

      if ( phi != 0 || chi != 0 || omega != 0 )
      {
        Vector3D_d new_base = new Vector3D_d( nom_base_vec ); 
        Vector3D_d new_up   = new Vector3D_d( nom_up_vec ); 
        Tran3D_d euler_rotation = tof_calc_d.makeEulerRotation(phi,chi,omega);
        euler_rotation.apply_to( new_base, new_base );
        euler_rotation.apply_to( new_up, new_up );
        grid.setOrientation( new_base, new_up );
      }

      det_count++;
    }
   
    find_qxyz_observed();
    find_U_and_B_observed();
    find_qxyz_theoretical();

    eval_count++;
    if ( eval_count % 500 == 0 )
    {
      String message = "After " + eval_count + " steps... params are";
      ShowProgress( message, System.out );
      ShowProgress( message, log_file );
    }
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
   *  Get an array listing all of the grid IDs (i.e. detector IDs) 
   *
   *  @return  an array of the detector IDs.
   */
  public int[] getAllGridIDs()
  {
     Object grid_objects[] = grids.values().toArray();
     int ids[] = new int[ grid_objects.length ];

     for ( int i = 0; i < ids.length; i++ )
       ids[i] = ((UniformGrid_d)grid_objects[i]).ID();

     Arrays.sort( ids );
     
     return ids;
  }


  /** 
   *  Get an array listing all of the data grids.
   *
   *  @return  an array of the detector double precision data grids.
   */
  public UniformGrid_d[] getAllGrids()
  {
    int[] ids = getAllGridIDs();
    UniformGrid_d all_grids[] = new UniformGrid_d[ ids.length ]; 

    for ( int i = 0; i < ids.length; i++ )
      all_grids[i] = (UniformGrid_d)(grids.get( ids[i] ));

    return all_grids;
  }



  /**
   *  Get a full length list of triples: row, col, time-of-flight for 
   *  the measured peaks, for the specified detetector ID, many of which
   *  are NULL!  
   *  NOTE: MANY OF THE POSITIONS IN THIS ARRAY WILL BE NULL, SINCE A NULL
   *        TRIPLE IS ENTERED IF THE PEAK CAME FROM THE OTHER DETECTOR!!
   *
   *  @param det_id  The id of the detector for which the peaks are returned
   */ 
  public float[][] getMeasuredPeakPositions( int det_id )
  {
     float positions[][] = new float[n_peaks][3];
     for ( int i = 0; i < n_peaks; i++ )
     {
       PeakData_d peak = (PeakData_d)peaks_vector.elementAt(i);
       if ( det_id != id[i] )
         positions[i] = null;
       else
       {
         positions[i][0] = (float)peak.row;
         positions[i][1] = (float)peak.col;
         positions[i][2] = (float)peak.tof;
       }
     }
     return positions;
  }


  /**
   *  Get a full length list of triples: row, col, time-of-flight for 
   *  the theoretically expected peaks, for the specified detetector ID,
   *  many of which are NULL!  
   *  NOTE: MANY OF THE POSITIONS IN THIS ARRAY WILL BE NULL, SINCE A NULL
   *        TRIPLE IS ENTERED IF THE PEAK CAME FROM THE OTHER DETECTOR, OR
   *        IF THE PEAK WOULD MISS THE DETECTOR, AFTER CALIBRATION ADJUSTMENTS
   *        ARE MADE!!!
   *
   *  @param det_id  The id of the detector for which the peaks are returned
   */
  public float[][] getTheoreticalPeakPositions( int det_id )
  {
     double l1 = all_parameters[L1_INDEX];
     double t0 = all_parameters[T0_INDEX];

     float positions[][] = new float[n_peaks][3];
     for ( int i = 0; i < n_peaks; i++ )
     {
       PeakData_d peak = (PeakData_d)peaks_vector.elementAt(i);
       if ( det_id != id[i] )
         positions[i] = null;
       else
       {
         UniformGrid_d grid_d = (UniformGrid_d)grids.get( new Integer(id[i]) );
         UniformGrid grid = new UniformGrid( grid_d, false ); 

         float phi   = (float)peak.orientation.getPhi();
         float chi   = (float)peak.orientation.getChi();
         float omega = (float)peak.orientation.getOmega();

         SampleOrientation   orientation_f = null;
         SampleOrientation_d orientation_d = peak.orientation;

         if ( orientation_d instanceof LANSCE_SCD_SampleOrientation_d )
           orientation_f = new LANSCE_SCD_SampleOrientation( phi, chi, omega );

         else  if ( orientation_d instanceof IPNS_SCD_SampleOrientation_d )
           orientation_f = new IPNS_SCD_SampleOrientation( phi, chi, omega );

         else
           orientation_f = new SNS_SampleOrientation( phi, chi, omega );

         VecQMapper mapper = new VecQMapper( grid, 
                                            (float)l1, 
                                            (float)t0, 
                                             orientation_f );

         Vector3D q_vec = new Vector3D( (float)qxyz_theoretical[i][0],
                                        (float)qxyz_theoretical[i][1],
                                        (float)qxyz_theoretical[i][2] );
         positions[i] = mapper.QtoRowColTOF( q_vec );
       }
     }
     return positions;
  }


  /**
   *  Get the standard deviation in Q value that was last calculated.
   *
   *  @return  An estimate for the standard deviation in Q calculated as
   *            Math.sqrt( sum_sq_errors/number_of_peaks ) 
   */
  public double getStandardDeviationInQ()
  {
    return standard_dev_in_Q;
  }

  /**
   *  Show the progress of the calibration calculation by printing the
   *  observed lattice parameters, standard deviation in the current 
   *  function values (i.e. differences between Q theoretical and
   *  Q observed) and the current parameter estimates.
   */
  public void ShowProgress()
  {
    ShowProgress( "", System.out );
  }
 

  /**
   *  Show the progress of the calibration calculation by printing the
   *  observed lattice parameters, standard deviation in the current 
   *  function values (i.e. differences between Q theoretical and
   *  Q observed) and the current parameter estimates.
   *
   *  @param message  A message String to print before the progress info
   *  @param out      The PrintStream to which the message and info is
   *                  to be sent.
   */
  public void ShowProgress( String message, PrintStream out )
  {                           
    if ( out == null )
      return;

    out.println();
    out.println("==================================================");
    out.println(message);
    out.println("==================================================");
    out.println( "Number of evaluations = " + eval_count );

                                       // first show observed cell parameters
                                       // for current stage of calibration
    double my_B[][] = copy ( B_observed );
    for ( int k = 0; k < 3; k++ )
      for ( int j = 0; j < 3; j++ )
        my_B[k][j] /= (2*Math.PI);

    double cell_params[] = lattice_calc.LatticeParamsOfUB( my_B );
    out.println("Observed cell parameters now are.... "); 
    for ( int i = 0; i < 3; i++ )
      out.print( " " + Format.real( cell_params[i], 10, 6 ) );
    for ( int i = 3; i < 6; i++ )
      out.print( " " + Format.real( cell_params[i], 10, 5 ) );
    out.println( " " + Format.real( cell_params[6], 10, 5 ) );
                                      
                                       // then show the standard deviation of
                                       // the error in q, from all zeros
    double index[] = new double[n_peaks];
    for ( int i = 0; i < index.length; i++ )
      index[i] = i;

    double vals[] = getValues( index );
    double sum_sq_errors = 0;
    for ( int i = 0; i < vals.length; i++ )
      sum_sq_errors += vals[i] * vals[i];

    standard_dev_in_Q = Math.sqrt( sum_sq_errors/vals.length );

    out.println();
    out.println("One standard dev error distance in Q = " + standard_dev_in_Q );

                                       // finally, dump out the current
                                       // parameter estimates
    int det_count = 0;
    out.println();
    out.println("Instrument & Sample parameters: " );
    for ( int i = 0; i < all_parameters.length; i ++ )
    {
      if ( (i - DET_BASE_INDEX) % N_DET_PARAMS == 0 )
      {
        out.println();
        out.println();
        out.println("Parameters for Detector " + det_count );
        det_count++;
      }
      if ( used_p_index[i] >= 0 )
        out.print( " " + Format.real(all_parameters[i], 12, 8 ) );
    }
    out.println();
  }

  
  /**
   *  For each detector, show only those calibration values that are currently
   *  used in the SCD software, in the form that they are used as of 7/29/2003.
   *  Other parameters are ont displayed.
   */
  public void ShowOldCalibrationInfo()
  {
     ShowOldCalibrationInfo( System.out );
  }


  /**
   *  For each detector, show only those calibration values that are currently
   *  used in the SCD software, in the form that they are used as of 7/29/2003.
   *  Other parameters are not displayed.
   */
  public void ShowOldCalibrationInfo( PrintStream out )
  {
    if ( out == null )
      return;

    out.println();
    out.println("===========================================================");
    out.println("BASIC Calibrated Values " );
    int det_count = 0;
    Enumeration e = grids.elements();
    while ( e.hasMoreElements())
    {
      UniformGrid_d grid = (UniformGrid_d)e.nextElement();
      out.println();
      out.println("DETECTOR: " + grid.ID());
      out.println();

      out.print( Format.real( 100 * all_parameters[L1_INDEX], 9, 3 ) );
      out.print( Format.real( all_parameters[T0_INDEX], 8, 3 ) );

      int index = DET_BASE_INDEX + det_count * N_DET_PARAMS;
                                                       // width and height are
                                                       // from outer edge to
                                                       // outer edge.
      double x2cm = 100 * 
                    all_parameters[ index + DET_WIDTH_INDEX ] / grid.num_cols();
      double y2cm = 100 * 
                    all_parameters[ index + DET_HEIGHT_INDEX] / grid.num_rows();

                                                //this is to edge of detector
                                                //not center of first pixel
      double xLeft  = 100 * all_parameters[index + DET_X_OFF_INDEX]
                        - x2cm * grid.num_cols() / 2.0;

      double yLower = 100 * all_parameters[index + DET_Y_OFF_INDEX]
                      - y2cm * grid.num_rows() / 2.0;

      out.print( Format.real( x2cm, 11, 6 ) );
      out.print( Format.real( y2cm, 11, 6 ) );
      out.print( Format.real( xLeft,  11, 6 ) );
      out.print( Format.real( yLower, 11, 6 ) );
      out.println();
/*    
      double phi   = all_parameters[ index + DET_PHI_INDEX ];
      double chi   = all_parameters[ index + DET_CHI_INDEX ];
      double omega = all_parameters[ index + DET_OMEGA_INDEX ];

      out.print("Detector phi, chi, omega = " );
      out.print( Format.real( phi, 11, 6 ) );
      out.print( Format.real( chi, 11, 6 ) );
      out.print( Format.real( omega, 11, 6 ) );
      out.println();
*/
      det_count++;
    }
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
                                                    // load the vector of peaks
      Vector peaks = PeakData_d.ReadPeaks( args[0], null, PeakData_d.SNS_SCD );
      PeakData_d peak = (PeakData_d)peaks.elementAt(0);
      double l1 = peak.l1; 

      Hashtable grids = new Hashtable();
      for ( int i = 0; i < peaks.size(); i++ )
      {
        UniformGrid_d grid = ((PeakData_d)peaks.elementAt(i)).grid;  
        Integer key = new Integer( grid.ID() );
        if ( grids.get(key) == null )            // new detector, so add it
        {
          grids.put( key, grid );
          System.out.println("grid = " + grid );
        }
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
      parameters[ L1_INDEX ]  = l1;
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
   // is_used[ L1_INDEX ] = false;
   // is_used[ T0_INDEX ] = false;
      is_used[ A_INDEX  ] = false;
      is_used[ SX_INDEX ] = false;
      is_used[ SY_INDEX ] = false;
      is_used[ SZ_INDEX ] = false;
                              // then turn off some params for all detectors.
      for ( int i = 0; i < det_count; i++ )   
      {
        index = DET_BASE_INDEX + i * N_DET_PARAMS;

    //  is_used[ index + DET_WIDTH_INDEX  ] = false;
    //  is_used[ index + DET_HEIGHT_INDEX ] = false;
    //  is_used[ index + DET_X_OFF_INDEX  ] = false;
    //  is_used[ index + DET_Y_OFF_INDEX  ] = false;
        is_used[ index + DET_D_INDEX      ] = false;
        is_used[ index + DET_PHI_INDEX    ] = false;
        is_used[ index + DET_CHI_INDEX    ] = false;
        is_used[ index + DET_OMEGA_INDEX  ] = false;
      }
                              // now count the number that were used
      int n_used = 0;
      for ( int i = 0; i < n_params; i++ )
        if ( is_used[i] )
          n_used++;
                                                     // build the one variable
                                                     // function
      SCDcal error_f = new SCDcal( PeakData_d.SNS_SCD,
                                   peaks, 
                                   grids,
                                   parameters, 
                                   parameter_names,
                                   n_used, 
                                   is_used,
                                   lattice_params,
                                   null ); 

      for ( int i = 0; i < parameters.length; i++ )
      {
        if ( is_used[i] )
          System.out.print("  ");
        else
          System.out.print("* ");
        System.out.println( parameter_names[i] +" = " + parameters[i] ); 
      }
      String message = "Before fit... params are";
      error_f.ShowProgress( message, System.out );
      error_f.ShowProgress( message, error_f.log_file );
                                                // build the arrays of x values
                                                // target function values 
                                                // (z_vals) and "fake"
      double z_vals[] = new double[ peaks.size() ]; 
      double sigmas[] = new double[ peaks.size() ]; 
      double x_index[] = new double[ peaks.size() ];
      for ( int i = 0; i < peaks.size(); i++ )
      {
        z_vals[i] = 0;
//      sigmas[i] = Math.sqrt( counts[i] );
        sigmas[i] = 1.0;
        x_index[i]  = i;
      }
                                           // build the data fitter and display 
                                           // the results.
      MarquardtArrayFitter fitter = 
      new MarquardtArrayFitter(error_f, x_index, z_vals, sigmas, 1.0e-16, 2000);

      System.out.println( fitter.getResultsString() );

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

      message = "After fit... params are";
      error_f.ShowProgress( message, System.out );
      error_f.ShowProgress( message, error_f.log_file );

      error_f.ShowOldCalibrationInfo();
    }
}

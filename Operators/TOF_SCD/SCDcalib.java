/*
 * File:  SCDcalib.java
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
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2003/08/04 15:56:21  dennis
 *  Initial Version of SCD calibration operator.
 *
 */
package Operators.TOF_SCD;

import DataSetTools.math.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import DataSetTools.util.*;
import DataSetTools.trial.*;
import DataSetTools.functions.*;
import DataSetTools.dataset.*;

import java.io.*;
import java.util.*;

import IPNS.Runfile.*;


/**
 */
public class SCDcalib extends GenericTOF_SCD 
{

  private static final String  TITLE = "SCD Calibration";

  /* ------------------------ Default constructor ------------------------- */
  /**
   *  Creates operator with title "SCD Calibration" and a default
   *  list of parameters.
   */
  public SCDcalib()
  {
    super( TITLE );
  }

  /* --------------------------- getCommand ------------------------------- */
  /**
   * Get the name of this operator to use in scripts
   *
   * @return  "SCDcalib", the command used to invoke this
   *           operator in Scripts
   */
  public String getCommand()
  {
    return "SCDcalib";
  }

  /* ----------------------- setDefaultParameters ------------------------- */
  /**
   * Sets default values for the parameters.  This must match the data types
   * of the parameters.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();

    LoadFilePG lfpg = new LoadFilePG( "Peaks file", null );
    lfpg.setFilter( new PeaksFilter(  ) );

    addParameter( lfpg );

    LoadFilePG rfpg = new LoadFilePG( "Run file", null );
    rfpg.setFilter( new RunfileFilter() );

    addParameter( rfpg );

    addParameter( new Parameter("lattice 'a'", new Float(4.9138f)) );
    addParameter( new Parameter("lattice 'a'", new Float(5.4051f)) );
    addParameter( new Parameter("lattice 'c'", new Float(4.9138f)) );
    addParameter( new Parameter("lattice 'alpha'", new Float(90)) );
    addParameter( new Parameter("lattice 'beta'",  new Float(120)) );
    addParameter( new Parameter("lattice 'gamma'", new Float(90)) );

    addParameter( new Parameter("max steps", new Integer(500)) );
    addParameter( new Parameter("tolerance exponent", new Integer(-12)) );

    addParameter( new Parameter("Refine L1",  new Boolean(true)) ); 
    addParameter( new Parameter("Refine t0",  new Boolean(true)) ); 
    addParameter( new Parameter("Refine 'A'(tof=At+t0)", new Boolean(false)) ); 
    addParameter( new Parameter("Refine sample shift", new Boolean(false)) );

    addParameter( new Parameter("Refine det width",  new Boolean(true)) ); 
    addParameter( new Parameter("Refine det height", new Boolean(true)) ); 
    addParameter( new Parameter("Refine det x_offset", new Boolean(true)) ); 
    addParameter( new Parameter("Refine det y_offset", new Boolean(true)) ); 
    addParameter( new Parameter("Refine det distance", new Boolean(false)) ); 
    addParameter( new Parameter("Refine det rotation", new Boolean(false)) ); 
  }



  /**
   * Uses the current values of the parameters to generate a result.
   */
  public Object getResult(  ) 
  {
    String peaksfile = getParameter(0).getValue().toString();
    String runfile   = getParameter(1).getValue().toString();

    float  lat_params[] = new float[6];
    for ( int i = 0; i < 6; i++ )
      lat_params[i] = ((Float)(getParameter(i+2).getValue())).floatValue(); 

    int max_steps = ((Integer)(getParameter(8).getValue())).intValue();
    int tol_exp   = ((Integer)(getParameter(9).getValue())).intValue();

    boolean use_L1    = ((Boolean)(getParameter(10).getValue())).booleanValue();
    boolean use_t0    = ((Boolean)(getParameter(11).getValue())).booleanValue();
    boolean use_A     = ((Boolean)(getParameter(12).getValue())).booleanValue();
    boolean use_ssh   = ((Boolean)(getParameter(13).getValue())).booleanValue();
    boolean use_width = ((Boolean)(getParameter(14).getValue())).booleanValue();
    boolean use_height =((Boolean)(getParameter(15).getValue())).booleanValue();
    boolean use_xoff  = ((Boolean)(getParameter(16).getValue())).booleanValue();
    boolean use_yoff  = ((Boolean)(getParameter(17).getValue())).booleanValue();
    boolean use_dist  = ((Boolean)(getParameter(18).getValue())).booleanValue();
    boolean use_rot   = ((Boolean)(getParameter(19).getValue())).booleanValue();


    double lattice_params[] = new double[6];
    for ( int i = 0; i < 6; i++ )
      lattice_params[i] = lat_params[i];
                                                    // load the vector of peaks
    Vector peaks = PeakData.ReadPeaks( peaksfile, runfile ); 

    PeakData peak = (PeakData)peaks.elementAt(0);
    double l1 = peak.l1;

    Hashtable grids = new Hashtable();
    for ( int i = 0; i < peaks.size(); i++ )
    {
      UniformGrid_d grid = ((PeakData)peaks.elementAt(i)).grid;
      Integer key = new Integer( grid.ID() );
      if ( grids.get(key) == null )            // new detector, so add it
      {
        grids.put( key, grid );
        System.out.println("grid = " + grid );
      }
    }
                                                   // set up the list of
                                                   // parameters and names
    int DET_BASE_INDEX   = SCDcal.DET_BASE_INDEX;
    int N_DET_PARAMS     = SCDcal.N_DET_PARAMS;
    int L1_INDEX         = SCDcal.L1_INDEX;
    int T0_INDEX         = SCDcal.T0_INDEX;
    int A_INDEX          = SCDcal.A_INDEX;
    int SX_INDEX         = SCDcal.SX_INDEX;
    int SY_INDEX         = SCDcal.SY_INDEX;
    int SZ_INDEX         = SCDcal.SZ_INDEX;
    int DET_WIDTH_INDEX  = SCDcal.DET_WIDTH_INDEX;
    int DET_HEIGHT_INDEX = SCDcal.DET_HEIGHT_INDEX;
    int DET_X_OFF_INDEX  = SCDcal.DET_X_OFF_INDEX;
    int DET_Y_OFF_INDEX  = SCDcal.DET_Y_OFF_INDEX;
    int DET_D_INDEX      = SCDcal.DET_D_INDEX;
    int DET_PHI_INDEX    = SCDcal.DET_PHI_INDEX;
    int DET_CHI_INDEX    = SCDcal.DET_CHI_INDEX;
    int DET_OMEGA_INDEX  = SCDcal.DET_OMEGA_INDEX;

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
    is_used[ L1_INDEX ] = use_L1;
    is_used[ T0_INDEX ] = use_t0;
    is_used[ A_INDEX  ] = use_A;
    is_used[ SX_INDEX ] = use_ssh;
    is_used[ SY_INDEX ] = use_ssh;
    is_used[ SZ_INDEX ] = use_ssh;
                              // then turn off some params for all detectors.
    for ( int i = 0; i < det_count; i++ )
    {
      index = DET_BASE_INDEX + i * N_DET_PARAMS;
      is_used[ index + DET_WIDTH_INDEX  ] = use_width;
      is_used[ index + DET_HEIGHT_INDEX ] = use_height;
      is_used[ index + DET_X_OFF_INDEX  ] = use_xoff;
      is_used[ index + DET_Y_OFF_INDEX  ] = use_yoff;
      is_used[ index + DET_D_INDEX      ] = use_dist;
      is_used[ index + DET_PHI_INDEX    ] = use_rot;
      is_used[ index + DET_CHI_INDEX    ] = use_rot;
      is_used[ index + DET_OMEGA_INDEX  ] = use_rot;
    }

                              // now count the number that were used
    int n_used = 0;
    for ( int i = 0; i < n_params; i++ )
      if ( is_used[i] )
        n_used++;
                                                     // build the one variable
                                                     // function
    SCDcal error_f = new SCDcal( peaks,
                                 grids,
                                 parameters, parameter_names,
                                 n_used, is_used,
                                 lattice_params );

    for ( int i = 0; i < parameters.length; i++ )
    {
      if ( is_used[i] )
        System.out.print("  ");
      else
        System.out.print("* ");
      System.out.println( parameter_names[i] +" = " + parameters[i] );
    }
    System.out.println("Before fit... params are");
    error_f.ShowProgress();
                                                // build the arrays of x values
                                                // target function values
                                                // (z_vals) and "fake"
    double z_vals[] = new double[ peaks.size() ];
    double sigmas[] = new double[ peaks.size() ];
    double x_index[] = new double[ peaks.size() ];
    for ( int i = 0; i < peaks.size(); i++ )
    {
      z_vals[i] = 0;
//    sigmas[i] = Math.sqrt( counts[i] );
      sigmas[i] = 1.0;
      x_index[i]  = i;
    }
                                           // build the data fitter and display
                                           // the results.
    double tolerance = Math.pow( 10, tol_exp ); 
    MarquardtArrayFitter fitter = 
       new MarquardtArrayFitter( error_f, 
                                 x_index, z_vals, sigmas, 
                                 tolerance, max_steps);

    System.out.println( fitter.getResultsString() );
    System.out.println();
    System.out.println("RESULTS -----------------------------------------");
    System.out.println("observed U matrix:");
    LinearAlgebra.print( error_f.U_observed );
    System.out.println("observed B matrix:");
    LinearAlgebra.print( error_f.B_observed );
    System.out.println("observed UB matrix:");
    double UB[][] = LinearAlgebra.mult( error_f.U_observed, error_f.B_observed);
    LinearAlgebra.print( UB );

    System.out.println("Transpose of observed UB/(2PI)");
    for ( int i = 0; i < 3; i++ )
      for ( int j = 0; j < 3; j++ )
        UB[i][j] /= (2*Math.PI);
    UB = LinearAlgebra.getTranspose( UB );
    LinearAlgebra.print( UB );

    System.out.println();
    error_f.ShowProgress();

    error_f.ShowOldCalibrationInfo();

    float results[] = new float[ parameters.length ];
    for ( int i = 0; i < parameters.length; i++ )
      results[i] = (float)parameters[i];

    Vector result_vector = new Vector();
    result_vector.addElement( results );
    result_vector.addElement( parameter_names );

    return result_vector;
  }

}

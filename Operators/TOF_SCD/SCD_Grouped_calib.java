/*
 * File:  SCD_Grouped_calib.java
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 * 
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */
package Operators.TOF_SCD;

import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.operator.*;
import DataSetTools.trial.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;

import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Functions.*;
import gov.anl.ipns.Parameters.ArrayPG;
import gov.anl.ipns.Parameters.BooleanPG;
import gov.anl.ipns.Parameters.LoadFilePG;
import gov.anl.ipns.Parameters.BooleanEnablePG;
import gov.anl.ipns.Parameters.DataDirPG;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Sys.*;

import java.util.*;
import java.io.*;
import IPNS.Runfile.*;


/**
 *  This operator uses data from a known crystal sample to calibrate various
 *  instrument parameters for a single crystal neutron diffraction instrument
 *  such as the SCD at IPNS.  A file listing the measured peak positions
 *  (the peaks file) and one of the runfiles that was used to produce the
 *  peaks file are needed, as well as the lattice parameters for the known
 *  sample.  The calibration codes calculate where peaks should occur in
 *  Q-space (theoretical positions) based on the lattice parameters, as well 
 *  as where the measured peaks (observed peaks) occur in Q-space and then
 *  adjusts the instrument parameters so that the observed peaks match the
 *  theoretical peaks as closely as possible.  An implementation of the 
 *  Marquardt-Levenberg optimization algorithm is used.  The user can specify
 *  a maximum number of steps and tolerance to control when the algorithm
 *  terminates.  The user can also choose which instrument parameters are 
 *  adjusted.  Since the instrument parameters are related, it is NOT possible
 *  to optimize the fit for all of them simultaneously.  For example, if
 *  the detector distance is allowed to vary, the detector width and height
 *  should be kept fixed.
 */
public class SCD_Grouped_calib extends GenericTOF_SCD 
{
  private static final String  TITLE = "SCD Grouped Calibration";
  public  static final String  RESULTS_FILE_NAME = "/SCD_Grouped_calib.results";

  /* ------------------------ Default constructor ------------------------- */
  /**
   *  Creates operator with title "SCD Grouped Calibration" and a default
   *  list of parameters.
   */
  public SCD_Grouped_calib()
  {
    super( TITLE );
  }

  /* --------------------------- constructor ------------------------------ */
  /**
   *  Creates operator with title "SCD Grouped Calibration" and the 
   *  specified list of parameters.  The getResult method must still be 
   *  used to execute the operator.
   *
   *  @param  peaksfile   SCD peaks file containing the observed peaks. 
   *  @param  det_groups  String indicating how the detectors should be
   *                      grouped, in a form like "[2:4][5:7][8:10]"
   *  @param  a           Lattice parameter, 'a b c' must correspond to the
   *                      order of the axes in the peaksfile. 
   *  @param  b           Lattice parameter, 'a b c' must correspond to the
   *                      order of the axes in the peaksfile. 
   *  @param  c           Lattice parameter, 'a b c' must correspond to the
   *                      order of the axes in the peaksfile. 
   *  @param  alpha       Lattice parameter, 'alpha beta gamma' angles must
   *                      correspond to the order of the axes in the peaks 
   *                      file.
   *  @param  beta        Lattice parameter, 'alpha beta gamma' angles must
   *                      correspond to the order of the axes in the peaks 
   *                      file.
   *  @param  gamma       Lattice parameter, 'alpha beta gamma' angles must
   *                      correspond to the order of the axes in the peaks 
   *                      file.
   *  @param  max_steps   The maximum number of iteration steps for the 
   *                      optimization.                    
   *  @param  tol_exp     The exponent for the tolerance.  The iteration stops
   *                      when the normalized relative change in the parameters
   *                      is less than 10^tol_exp.  Values of -10 to -16 
   *                      should work.
   *  @param use_L1       Flag indicating whether or not the value of L1 
   *                      should be allowed to vary from it's nominal value.
   *  @param use_t0       Flag indicating whether or not the value of t0 
   *                      should be allowed to vary from it's nominal value.
   *  @param use_A        Flag indicating whether or not the value of A 
   *                      in the wavelength dependent time-of-flight shift
   *                      equation calibrated_tof=A*tof+t0, should be allowed 
   *                      to vary from it's nominal value of 1.
   *  @param use_ssh      Flag indicating whether or not the sample position
   *                      should be shifted from the center of the goniometer.
   *                      The shift values SX, SY, SZ are shifts in the
   *                      laboratory X,Y,Z coordinate system, when the
   *                      goniometer angles are all set to 0. 
   *  @param use_width    Flag indicating whether or not the width of the 
   *                      detectors should be allowed to vary from their
   *                      nominal values.
   *  @param use_height   Flag indicating whether or not the height of the 
   *                      detectors should be allowed to vary from their
   *                      nominal values.
   *  @param use_xoff     Flag indicating whether or not the detectors can
   *                      be offset in the x direction in the xy coordinate
   *                      system on the face of the detector, staying in the
   *                      original plane of the detector.
   *  @param use_yoff     Flag indicating whether or not the detectors can
   *                      be offset in the y direction in the xy coordinate
   *                      system on the face of the detector.
   *  @param use_dist     Flag indicating whether or not the distance from 
   *                      the detector to the sample can be varied, for each
   *                      detector.
   *  @param use_rot      Flag indicating whether or not an arbitrary rotation
   *                      of each detector should be allowed, specified by
   *                      Euler angles in the laboratory coordinate system.
   *  @param read_params  Flag indicating whether or not to read initial values
   *                      for the parameters from a file.
   *  @param param_file   File from which the initial values would be read.
   */
  public SCD_Grouped_calib( String   peaksfile,
                            String   det_groups,
                            float    a,
                            float    b,
                            float    c,
                            float    alpha,
                            float    beta,
                            float    gamma,
                            int      max_steps,
                            int      tol_exp,
                            boolean  use_L1,
                            boolean  use_t0,
                            boolean  use_A,
                            boolean  use_ssh,
                            boolean  use_width,
                            boolean  use_height,
                            boolean  use_xoff,
                            boolean  use_yoff,
                            boolean  use_dist,
                            boolean  use_rot,
                            boolean  read_params,
                            String   param_file )
  {
    this();
    parameters = new Vector();

    LoadFilePG peaksfilePG = new LoadFilePG( peaksfile, null );
    peaksfilePG.setFilter( new PeaksFilter() );
    addParameter( peaksfilePG );

    ArrayPG group_pg = new ArrayPG(
                      "Detector Groups(as list of lists) eg.[[2:4][5:7][8:10]]",
                       det_groups );
    addParameter( group_pg );

    addParameter( new Parameter("lattice 'a'", new Float(a)) );
    addParameter( new Parameter("lattice 'a'", new Float(b)) );
    addParameter( new Parameter("lattice 'c'", new Float(c)) );
    addParameter( new Parameter("lattice 'alpha'", new Float(alpha)) );
    addParameter( new Parameter("lattice 'beta'",  new Float(beta)) );
    addParameter( new Parameter("lattice 'gamma'", new Float(gamma)) );

    addParameter( new Parameter("max steps", new Integer(max_steps)) );
    addParameter( new Parameter("tolerance exponent", new Integer(tol_exp)) );

    addParameter( new Parameter("Refine L1",  new Boolean(use_L1)) );
    addParameter( new Parameter("Refine t0",  new Boolean(use_t0)) );
    addParameter( new Parameter("Refine 'A'(tof=At+t0)", new Boolean(use_A)) );
    addParameter( new Parameter("Refine sample shift", new Boolean(use_ssh)) );

    addParameter( new Parameter("Refine det width",  new Boolean(use_width)) );
    addParameter( new Parameter("Refine det height", new Boolean(use_height)) );
    addParameter( new Parameter("Refine det x_offset", new Boolean(use_xoff)) );
    addParameter( new Parameter("Refine det y_offset", new Boolean(use_yoff)) );
    addParameter( new Parameter("Refine det distance", new Boolean(use_dist)) );
    addParameter( new Parameter("Refine det rotation", new Boolean(use_rot)) );

    addParameter( make_initial_file_pg(false) );

    addParameter( new LoadFilePG("Initial Values File", param_file) );

    addParameter( new DataDirPG( 
                            "Output Dir for SCD_Grouped_calib.results & .log",
                             System.getProperty("user.home")) ); 
  }


  /* --------------------------- getCommand ------------------------------- */
  /**
   * Get the name of this operator to use in scripts
   *
   * @return  "SCD_Grouped_calib", the command used to invoke this
   *           operator in Scripts
   */
  public String getCommand()
  {
    return "SCD_Grouped_calib";
  }


  /* ------------------------ getDocumentation ---------------------------- */
  /**
   *  Get the documentation to be displayed by the help system.
   */
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    Res.append("@overview This operator uses data from a known crystal ");
    Res.append(" sample to calibrate various instrument parameters ");
    Res.append(" for a single crystal neutron diffraction instrument ");
    Res.append(" such as SNAP at the SNS .  A file listing the measured ");
    Res.append(" peak positions (an SNS format peaks file) is needed, as ");
    Res.append(" well as the lattice parameters for the known sample. ");
    Res.append(" The detectors can be arranged in groups.  During the ");
    Res.append(" optimization, detectors in a group will be moved ");
    Res.append(" together as though rigidly attached to each other. ");
    Res.append(" This allows treating each detector panel for the SNAP ");
    Res.append(" instrument as a rigid assembly.  ");
    Res.append(" NOTE: This operator automatically writes two files ");
    Res.append(" by default to the user's home directory. ");
    Res.append(" The first file, SCD_Grouped_calib.results, ");
    Res.append(" contains the results of the calibration, including the ");
    Res.append(" internal form of all supported parameters, the  ");
    Res.append(" calibrated L1, t0 and detector position information ");
    Res.append(" in the form used by ISAW's SNS SCD codes.  This file ");
    Res.append(" can also be read by the operator, to get initial values ");
    Res.append(" for the parameters, instead of using values from ");
    Res.append(" the NeXus file.  After running this at least once, you ");
    Res.append(" can check the option to Load Initial Values From File. ");
    Res.append(" By doing this, it is possible to resume refining the ");
    Res.append(" parameters from where they were after the operator ");
    Res.append(" terminated.  The second file that is automatically ");
    Res.append(" produced is SCD_Grouped_calib.log.  This log file ");
    Res.append(" contains the ");
    Res.append(" initial values used for this run, some progress reports ");
    Res.append(" on the optimization, written while it's in progress, lists ");
    Res.append(" of theoretical versus measured row, col and tof values for ");
    Res.append(" all detectors, the resulting parameters in");
    Res.append(" internal form, and final calibration information both ");
    Res.append(" in the new SNS form and in the form needed for the ");
    Res.append(" old IPNS SCD instrument. ");

    Res.append("@algorithm The calibration codes calculate where peaks ");
    Res.append(" should occur in Q-space (theoretical positions) based ");
    Res.append(" on the lattice parameters, as well as where the ");
    Res.append(" measured peaks (observed peaks) occur in Q-space and ");
    Res.append(" then adjusts the instrument parameters so that the ");
    Res.append(" observed peaks match the theoretical peaks as closely ");
    Res.append("  as possible.  An implementation of the Marquardt ");
    Res.append(" optimization algorithm is used.  The user can specify ");
    Res.append(" a maximum number of steps and tolerance to control ");
    Res.append(" when the algorithm terminates.  The user can also ");
    Res.append(" choose which instrument parameters are adjusted. ");
    Res.append(" Since the instrument parameters are related, ");
    Res.append(" it may not be possible to optimize the fit for all ");
    Res.append(" of them simultaneously.  For example, if the detector ");
    Res.append(" distance is allowed to vary, it may be best to keep " );
    Res.append(" the detector width and height fixed. ");

    Res.append("@param  peaksfile - SCD peaks file containing the ");
    Res.append(" observed peaks.");
    
    Res.append("@param  det_groups - String specifying how the detectors ");
    Res.append(" should be grouped during the optimization. ");
    Res.append(" The positions and orientations of the detectors in a group ");
    Res.append(" are all changed together, as though the detectors were ");
    Res.append(" rigidly attached to each other.");
    Res.append(" The groups must be specified as a list of lists of ");
    Res.append(" detector IDs.  Square brackets indicate a list, and ");
    Res.append(" detector IDs are spcified in comma separated lists. ");
    Res.append(" A range of consecutive integers can be specified using " );
    Res.append(" a colon between the first and last integer.  For example: " );
    Res.append(" [[2:5][6,9,10:20]].");
    Res.append(" NOTE: The groups are checked for validity before ");
    Res.append(" they are used.  Specifically, any detectors that are ");
    Res.append(" not present in the peaks file are removed from groups. ");
    Res.append(" Any duplicate detector IDs are also removed.  ");
    Res.append(" Any detector that is NOT listed in a group will not be ");
    Res.append(" adjusted during the calibration.  ");
    Res.append(" If individual detectors are to be adjusted, they must ");
    Res.append(" be put in a group with one member.");

    Res.append("@param  a - Lattice parameter, 'a b c' must correspond ");
    Res.append(" to the order of the axes in the peaksfile.");

    Res.append("@param  b - Lattice parameter, 'a b c' must correspond ");
    Res.append(" to the order of the axes in the peaksfile.");

    Res.append("@param  c - Lattice parameter, 'a b c' must correspond ");
    Res.append(" to the order of the axes in the peaksfile.");

    Res.append("@param  alpha - Lattice parameter, 'alpha beta gamma' ");
    Res.append(" angles must correspond to the order of the angles in the ");
    Res.append(" peaks file");

    Res.append("@param  beta - Lattice parameter, 'alpha beta gamma' ");
    Res.append(" angles must correspond to the order of the angles in the ");
    Res.append(" peaks file");

    Res.append("@param  gamma - Lattice parameter, 'alpha beta gamma' ");
    Res.append(" angles must correspond to the order of the angles in the ");
    Res.append(" peaks file");

    Res.append("@param  max_steps - The maximum number of iteration ");
    Res.append(" steps for the optimization. ");

    Res.append("@param  tol_exp - The exponent for the tolerance.  ");
    Res.append(" The iteration stops when the normalized relative ");
    Res.append(" change in the parameters is less than 10^tol_exp.");
    Res.append(" Values of -10 to -16 should work.");

    Res.append("@param use_L1 - Flag indicating whether or not the ");
    Res.append(" value of L1 should be allowed to vary from it's ");
    Res.append(" nominal value.");

    Res.append("@param use_t0 - Flag indicating whether or not the ");
    Res.append(" value of t0 should be allowed to vary from it's ");
    Res.append(" nominal value.");

    Res.append("@param use_A - Flag indicating whether or not the ");
    Res.append(" value of A in the wavelength dependent time-of-flight " );
    Res.append(" shift equation (calibrated_tof=A*tof+t0), should be allowed ");
    Res.append(" to vary from it's nominal value of 1.");

    Res.append("@param use_ssh - Flag indicating whether or not the ");
    Res.append(" sample position should be shifted from the center of the ");
    Res.append(" goniometer. The shift values SX, SY, SZ are shifts in ");
    Res.append(" the laboratory X,Y,Z coordinate system, when the ");
    Res.append(" goniometer angles are all set to 0.");

    Res.append("@param use_width - Flag indicating whether or not the ");
    Res.append(" width of the detectors should be allowed to vary from ");
    Res.append(" their nominal values. ");

    Res.append("@param use_height - Flag indicating whether or not the ");
    Res.append(" height of the detectors should be allowed to vary from ");
    Res.append(" their nominal values. ");

    Res.append("@param use_xoff - Flag indicating whether or not the ");
    Res.append(" detectors can be offset in the x direction in the ");
    Res.append(" xy coordinate system on the face of the detector, staying ");
    Res.append(" in the original plane of the detector. ");

    Res.append("@param use_yoff - Flag indicating whether or not the ");
    Res.append(" detectors can be offset in the y direction in the ");
    Res.append(" xy coordinate system on the face of the detector, staying ");
    Res.append(" in the original plane of the detector. ");

    Res.append("@param use_dist - Flag indicating whether or not the  ");
    Res.append(" distance from the detector to the sample can be varied, ");
    Res.append(" for each detector.");

    Res.append("@param use_rot - Flag indicating whether or not an ");
    Res.append(" arbitrary rotation of each detector should be allowed, ");
    Res.append(" specified by Euler angles in the laboratory ");
    Res.append(" coordinate system. ");

    Res.append("@param read_params - Flag indicating whether or not ");
    Res.append(" to read initial values for the parameters from a file. ");

    Res.append("@param param_file - File from which the initial values ");
    Res.append(" would be read.  This file should have the parameters listed ");
    Res.append(" in EXACTLY the same form as in the file SCDcal.results.");
    Res.append(" NOTE: If the read_params box is checked, and the default ");
    Res.append(" file name is used, then the last SCDcal.results file ");
    Res.append(" that was written will be read and those parameter values ");
    Res.append(" will be used as a starting point for the next optimization. ");
    Res.append(" This is a convenient way to refine iteratively on different ");
    Res.append(" sets of parameters.");

    Res.append(" @param data_dir - Directory where the .resuls and .log ");
    Res.append(" will be written.  This defaults to the user's home ");
    Res.append(" directory.  ");

    Res.append("@return A String indicating that the operation was ");
    Res.append(" completed.  ");
/*
    Res.append("@return A vector containing entries giving the ");
    Res.append(" values of the instrument parameters followed by the ");
    Res.append(" names of the instrument paramters.  Most of the results ");
    Res.append(" are currently displayed on the system console. ");
*/
    return Res.toString();
  }


  /* ----------------------- setDefaultParameters ------------------------- */
  /**
   * Sets default values for the parameters.  This must match the data types
   * of the parameters.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();

    LoadFilePG peaksfilePG = new LoadFilePG( "Peaks file", null );
    peaksfilePG.setFilter( new PeaksFilter(  ) );
    addParameter( peaksfilePG );

    ArrayPG group_pg = new ArrayPG(
                     "Detector Groups(as list of lists) eg.[[2:4][5:7][8:10]]",
                     "[[2:10]]" );
    addParameter( group_pg );

    addParameter( new Parameter("lattice 'a'", new Float(4.9138f)) );
    addParameter( new Parameter("lattice 'b'", new Float(4.9138f)) );
    addParameter( new Parameter("lattice 'c'", new Float(5.4051f)) );
    addParameter( new Parameter("lattice 'alpha'", new Float(90)) );
    addParameter( new Parameter("lattice 'beta'",  new Float(90)) );
    addParameter( new Parameter("lattice 'gamma'", new Float(120)) );

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

    String home_dir = System.getProperty( "user.home" );
    addParameter( make_initial_file_pg(false) );
    addParameter( new LoadFilePG("Initial Values File",
                                  home_dir + RESULTS_FILE_NAME) ); 

    addParameter( new DataDirPG(
                           "Output Dir for SCD_Grouped_calib.results & .log",
                            home_dir )); 
  }

  /**
   *  Make the BooleanEnablePG parameter GUI that controls whether or not
   *  initial values are read from a file.
   *
   *  @param  state   boolean indicating whether or not the PG should be
   *                  initialized to true.
   *
   *  @return  A BooleanEnablePG to control whether or not initial values
   *           are read from a file.
   */
  private BooleanEnablePG make_initial_file_pg( boolean state )
  {
    Vector vals = new Vector();
    vals.add( new Boolean(false) );
    vals.add( new Integer(1) );
    vals.add( new Integer(0) );
    return new BooleanEnablePG( "Load Initial Values From File", vals );
  }

 
  /**
   * Uses the current values of the parameters to calcuate calibrated values
   * for the instrument parameters.
   *
   * @return A vector containing the calibrated values of all of the parameters
   *           that could have been allowed to vary, followed by the names
   *           of the parameters.
   */
  public Object getResult() 
  {
    String peaksfile   = getParameter(0).getValue().toString();
    Vector groups_vec = (Vector) getParameter(1).getValue();

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

    boolean read_params = 
                       ((Boolean)(getParameter(20).getValue())).booleanValue();
    String  param_file  = getParameter(21).getValue().toString(); 
    String  output_dir  = getParameter(22).getValue().toString(); 

                                                      // open the log file
    String      logname   = "SCD_Grouped_calib.log";
    PrintStream log_print = null;
    try
    {
      File log_file       = new File( output_dir.trim() + "/" + logname );
      OutputStream log_os = new FileOutputStream( log_file );
      log_print = new PrintStream( log_os );
    }
    catch ( Exception e )
    {
      SharedMessages.addmsg("WARNING: Couldn't open log file in " + output_dir);
      SharedMessages.addmsg("         Only writing log to console terminal...");
      log_print = null;
    }

    double lattice_params[] = new double[6];
    for ( int i = 0; i < 6; i++ )
      lattice_params[i] = lat_params[i];

    String instrument_type = PeakData_d.SNS_SCD;
    Vector peaks = PeakData_d.ReadPeaks(peaksfile, null, instrument_type); 

    if ( peaks == null || peaks.size() <= 0 )
      return new ErrorString("Failed to read " + peaksfile );

    
    System.out.println( "Read " + peaks.size() + " peaks from file.");
    PeakData_d peak = (PeakData_d)peaks.elementAt(0);
    double l1 = peak.l1;  

    Hashtable grids = new Hashtable();         // find all grids used by peaks
    for ( int i = 0; i < peaks.size(); i++ )
    {
      UniformGrid_d grid = ((PeakData_d)peaks.elementAt(i)).grid;
      Integer key = new Integer( grid.ID() );
      if ( grids.get(key) == null )            // new detector, so add it
      {
        grids.put( key, grid );
        System.out.println("Found detector :" + grid.ID() );
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

    UniformGrid_d[] grid_arr = SCDcal_util.getAllGrids( grids );
    for ( int det_count = 0; det_count < grid_arr.length; det_count++ )
    {
      index = DET_BASE_INDEX + det_count * N_DET_PARAMS;

      UniformGrid_d grid = grid_arr[ det_count ];
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
    }

    if ( read_params )
      SCDcal_util.ReadParams( param_file, parameter_names, parameters );

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
    for ( int i = 0; i < grid_arr.length; i++ )
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

    int[][] groups = SCDcal_util.formGroups( groups_vec, grids, is_used );

                              // now count the number that were used
    int n_used = 0;
    for ( int i = 0; i < n_params; i++ )
      if ( is_used[i] )
        n_used++;
                                                     // build the one variable
                                                     // function
    SCD_Grouped_cal error_f = new SCD_Grouped_cal( peaks,
                                                   grids,
                                                   groups,
                                                   parameters, 
                                                   parameter_names,
                                                   n_used, 
                                                   is_used,
                                                   lattice_params,
                                                   log_print );

                                                      // show initial values
    SCDcal_util.ShowInitalValues( 
                          System.out, parameter_names, parameters, is_used );

    SCDcal_util.ShowInitalValues( 
                          log_print, parameter_names, parameters, is_used );

    String message = "Before fit... params are";
    error_f.ShowProgress( message, System.out );
    error_f.ShowProgress( message, log_print  );
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
                                 tolerance, max_steps );

    if ( log_print != null )
    {
      log_print.println( "==================================================");
      log_print.println( "RESULT OF FIT:");
      log_print.println( "==================================================");
      log_print.println( "PARAMETERS ACTUALLY USED FOR CALIBRATION:");
      log_print.println( fitter.getResultsString() );
    }
    System.out.println( "==================================================");
    System.out.println( "RESULT OF FIT:");
    System.out.println( "==================================================");
    System.out.println( "PARAMETERS ACTUALLY USED FOR CALIBRATION:");
    System.out.println( fitter.getResultsString() );

    System.out.println( "==================================================");
    System.out.println( "MATRICES:");
    System.out.println( "==================================================");
    System.out.println("observed U matrix:");
    LinearAlgebra.print( error_f.getU_observed() );

    System.out.println("observed B matrix:");
    LinearAlgebra.print( error_f.getB_observed() );

    System.out.println("observed UB matrix:");
    double UB[][] = LinearAlgebra.mult( error_f.getU_observed(), 
                                        error_f.getB_observed() );
    LinearAlgebra.print( UB );

    System.out.println("Transpose of observed UB/(2PI)");
    for ( int i = 0; i < 3; i++ )
      for ( int j = 0; j < 3; j++ )
        UB[i][j] /= (2*Math.PI);
    UB = LinearAlgebra.getTranspose( UB );
    LinearAlgebra.print( UB );
    System.out.println();
                                             // log & show row, col, tof errors
    int id_list[] = SCDcal_util.getAllGridIDs( grids );
    for ( int count = 0; count < id_list.length; count++ )
    {
      int det_id = id_list[count]; 

      float meas_pos[][] = error_f.getMeasuredPeakPositions( det_id );
      float theo_pos[][] = error_f.getTheoreticalPeakPositions( det_id );

      floatPoint2D row_pairs[] = SCDcal_util.getPairs( 0, theo_pos, meas_pos );
      floatPoint2D col_pairs[] = SCDcal_util.getPairs( 1, theo_pos, meas_pos );
      floatPoint2D tof_pairs[] = SCDcal_util.getPairs( 2, theo_pos, meas_pos );

      if ( log_print != null )
      {
        log_print.println("");
        log_print.println("=================================================");
        log_print.println("Detector Row Number Comparison, ID " + det_id );
        log_print.println("=================================================");
        log_print.println("Theoetical     Measured");
        for ( int i = 0; i < row_pairs.length; i++ )
          log_print.println( Format.real( row_pairs[i].x, 10, 5 ) + "   " +
                             Format.real( row_pairs[i].y, 13, 5 )    );

        log_print.println("Detector Column Number Comparison, ID " + det_id );
        log_print.println("Theoetical     Measured");
        for ( int i = 0; i < col_pairs.length; i++ )
          log_print.println( Format.real( col_pairs[i].x, 10, 5 ) + "   " +
                             Format.real( col_pairs[i].y, 13, 5 )    );

        log_print.println("Detector Time-of-flight Comparison, ID " + det_id );
        log_print.print("NOTE: The calculated T0 shift should be added to ");
        log_print.println("the measured value ");
        log_print.println("Theoetical     Measured");
        for ( int i = 0; i < tof_pairs.length; i++ )
          log_print.println( Format.real( tof_pairs[i].x, 10, 3 ) + "   " +
                             Format.real( tof_pairs[i].y, 13, 3 )    );
      }

      SCDcal_util.MakeDisplay( "Theoretical vs Measured Row, ID " + det_id, 
                               "Row", "Number", row_pairs );
      SCDcal_util.MakeDisplay( "Theoretical vs Measured Column, ID " + det_id, 
                               "Column", "Number", col_pairs );
      SCDcal_util.MakeDisplay( "Theoretical vs Measured TOF, ID " + det_id, 
                               "Time", "us", tof_pairs );
    }
                                                   // record the results file
    String resultname = RESULTS_FILE_NAME;
    PrintStream result_print = null;
    try
    {
      File result_file       = new File( output_dir + resultname );
      OutputStream result_os = new FileOutputStream( result_file );
      result_print = new PrintStream( result_os );
    }
    catch ( Exception results_exeception )
    {
      SharedMessages.addmsg("WARNING: Couldn't write results file: " +
                             output_dir + resultname );
      result_print = null;
    }

    message = "CALIBRATION INFORMATION:";
    error_f.ShowProgress( message, System.out );
    SCDcal_util.showGroups( System.out, groups );


    if ( result_print != null )
    {
      SCDcal_util.WriteAllParams( result_print, parameter_names, parameters );
      error_f.ShowProgress( message, result_print );
      SCDcal_util.showGroups( result_print, groups );
      result_print.close();
    }

    if ( log_print != null )
    {
      error_f.ShowOldCalibrationInfo( log_print );
      SCDcal_util.WriteAllParams( log_print, parameter_names, parameters );
      SCDcal_util.ShowIfUsed( log_print, parameter_names, is_used );
      error_f.ShowProgress( message, log_print  );
      SCDcal_util.showGroups( log_print, groups );
      log_print.close();
    }

/*
    float results[] = new float[ parameters.length ];
    for ( int i = 0; i < parameters.length; i++ )
      results[i] = (float)parameters[i];

    Vector result_vector = new Vector();
    result_vector.addElement( results );
    result_vector.addElement( parameter_names );

    return result_vector;
*/
    return "Complete";
  }

}

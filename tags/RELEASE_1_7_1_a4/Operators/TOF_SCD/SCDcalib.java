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
 *  Revision 1.9  2004/04/15 15:09:16  dennis
 *  Added methods WriteAllParams() and ReadParams() to write and
 *  read the state of all possible instrument parameters.  This
 *  allows the parameters calculated by one run of the optimization
 *  to be used as the starting values for another run (presumably
 *  refining a different set of parameters).  The parameters are
 *  written to the file SCDcalib.results by default.  This file
 *  also contains the local basis vectors for the detectors, in
 *  case the detector orientation was optimized.
 *  Added more information to the SCDcalib.log file.
 *  Added operators parameters to choose whether to start with default
 *  instrument parameter values, or to read them from SCDcalib.results.
 *  Updated documentation to reflect the new features.
 *
 *  Revision 1.8  2004/04/02 17:50:05  dennis
 *  Now displays and logs comparisons between measured and theoretical
 *  values for row, col and tof, for each of the detectors used in
 *  the calibration, organized by detector.
 *
 *  Revision 1.7  2004/04/02 15:31:26  dennis
 *  Added code to log and display views of scatter plots showing the
 *  comparison between  measured and theoretical row, col and tof
 *  values.
 *
 *  Revision 1.6  2004/04/01 21:02:47  dennis
 *  Opens default log file, SCDcalib.log, in current directory
 *  and calls methods on the error function being minimized to
 *  write information on the progress of the computation, as
 *  well as the final results to the log file.
 *
 *  Revision 1.5  2004/03/31 21:35:40  dennis
 *  Added check for empty list of peaks (with hkl's) read from file.
 *
 *  Revision 1.4  2004/03/15 19:36:55  dennis
 *  Removed unused imports after factoring out view components,
 *  math and utilities.
 *
 *  Revision 1.3  2004/03/15 03:37:03  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.2  2003/08/05 23:03:25  dennis
 *  Added constructor to allow calling the operator from Java.
 *  Added javadocs and getDocumentation() method.
 *
 *  Revision 1.1  2003/08/04 15:56:21  dennis
 *  Initial Version of SCD calibration operator.
 *
 */
package Operators.TOF_SCD;

import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import DataSetTools.trial.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;

import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Functions.*;
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
public class SCDcalib extends GenericTOF_SCD 
{
  private static final String  TITLE = "SCD Calibration";
  public  static final String  DEFAULT_PARAMETERS_FILE = "SCDcalib.results";

  /* ------------------------ Default constructor ------------------------- */
  /**
   *  Creates operator with title "SCD Calibration" and a default
   *  list of parameters.
   */
  public SCDcalib()
  {
    super( TITLE );
  }

  /* --------------------------- constructor ------------------------------ */
  /**
   *  Creates operator with title "SCD Calibration" and the specified list
   *  of parameters.  The getResult method must still be used to execute 
   *  the operator.
   *
   *  @param  peaksfile   SCD peaks file containing the observed peaks. 
   *  @param  runfile     Runfile for one of the runs that produced the
   *                      peaks file (we assume all runs use the same 
   *                      time bins and detector positions).
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
  public SCDcalib( String   peaksfile,
                   String   runfile,
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

    LoadFilePG lfpg = new LoadFilePG( peaksfile, null );
    lfpg.setFilter( new PeaksFilter() );
    addParameter( lfpg );

    LoadFilePG rfpg = new LoadFilePG( runfile, null );
    rfpg.setFilter( new RunfileFilter() );
    addParameter( rfpg );

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

    addParameter( new Parameter("LOAD INITIAL VALUES FROM FILE", 
                  new Boolean( read_params ) ));
    addParameter( new LoadFilePG("ALL PARAMETERS FILE", param_file) );
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
    Res.append(" such as the SCD at IPNS.  A file listing the measured ");
    Res.append(" peak positions (the peaks file) and one of the runfiles ");
    Res.append(" that was used to produce the peaks file are needed, as ");
    Res.append(" well as the lattice parameters for the known sample. ");
    Res.append(" NOTE: This operator automatically writes two files to ");
    Res.append(" the current directory.  The first file, SCDcalib.results, ");
    Res.append(" contains the results of the calibration, including the ");
    Res.append(" internal form of all supported parameters, the local ");
    Res.append(" coordinate basis vectors for the detectors, and the ");
    Res.append(" basic calibration values currently (4/15/04) used by ");
    Res.append(" the SCD reduction codes, in the 'usual' form.  This file ");
    Res.append(" can also be read by the operator, to get initial values ");
    Res.append(" for the parameters, instead of using nominal values from ");
    Res.append(" the runfile.  The second file that is automatically ");
    Res.append(" produced is SCDcalib.log.  This log file contains the ");
    Res.append(" initial values used for this run, some progress reports ");
    Res.append(" on the optimization, written while it's in progress, lists ");
    Res.append(" of theoretical versus measured row, col and tof values for ");
    Res.append(" all detectors, and the resulting parameters in");
    Res.append(" internal form.");

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
    Res.append(" it is NOT possible to optimize the fit for all ");
    Res.append(" of them simultaneously.  For example, if the detector ");
    Res.append(" distance is allowed to vary, the detector width and ");
    Res.append(" height should be kept fixed. ");

    Res.append("@param  peaksfile - SCD peaks file containing the ");
    Res.append(" observed peaks.");

    Res.append("@param  runfile - Runfile for one of the runs that ");
    Res.append(" produced the peaks file (we assume all runs use the same ");
    Res.append(" time bins and detector positions).");

    Res.append("@param  a - Lattice parameter, 'a b c' must correspond ");
    Res.append(" to the order of the axes in the peaksfile.");

    Res.append("@param  b - Lattice parameter, 'a b c' must correspond ");
    Res.append(" to the order of the axes in the peaksfile.");

    Res.append("@param  c - Lattice parameter, 'a b c' must correspond ");
    Res.append(" to the order of the axes in the peaksfile.");

    Res.append("@param  alpha - Lattice parameter, 'alpha beta gamma' ");
    Res.append(" angles must correspond to the order of the axes in the ");
    Res.append(" peaks file");

    Res.append("@param  beta - Lattice parameter, 'alpha beta gamma' ");
    Res.append(" angles must correspond to the order of the axes in the ");
    Res.append(" peaks file");

    Res.append("@param  gamma - Lattice parameter, 'alpha beta gamma' ");
    Res.append(" angles must correspond to the order of the axes in the ");
    Res.append(" peaks file");

    Res.append("@param  max_steps - The maximum number of iteration ");
    Res.append(" steps for the optimization. ");

    Res.append("@param  tol_exp - he exponent for the tolerance. ");
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

    Res.append("@return A vector containing entries giving the ");
    Res.append(" values of the instrument parameters followed by the ");
    Res.append(" names of the instrument paramters.  Most of the results ");
    Res.append(" are currently displayed on the system console. ");

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

    LoadFilePG lfpg = new LoadFilePG( "Peaks file", null );
    lfpg.setFilter( new PeaksFilter(  ) );

    addParameter( lfpg );

    LoadFilePG rfpg = new LoadFilePG( "Run file", null );
    rfpg.setFilter( new RunfileFilter() );

    addParameter( rfpg );

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

    addParameter( new Parameter("LOAD INITIAL VALUES FROM FILE", 
                  new Boolean(false) ));
    addParameter( new LoadFilePG("ALL PARAMETERS FILE",
                                  DEFAULT_PARAMETERS_FILE) ); 
  }

 
  /*
   *  Extract list of (theory,measured) pairs, in a list of float point 2D
   *  objects.  If k = 0, this will the the row numbers.  If k = 1, this
   *  will be the column numbers.  If k = 2, this will be the times-of-flight
   */
  private floatPoint2D[] getPairs( int k, float theory[][], float measured[][] )
  {
     if ( k < 0 || k > 2 )
     {
       System.out.println("ERROR: invalid index in SCDcalib.getPairs() " + k );
       return null;
     }
                                                    // Build a vector keeping
                                                    // only non-null points
     Vector pairs_vector = new Vector( measured.length );
     for ( int i = 0; i < measured.length; i++ )
       if ( theory[i] != null && measured[i] != null )
         pairs_vector.add( new floatPoint2D( theory[i][k], measured[i][k] ) );

     floatPoint2D pairs[] = new floatPoint2D[ pairs_vector.size() ];
     for ( int i = 0; i < pairs.length; i++ )
       pairs[i] = (floatPoint2D)pairs_vector.elementAt( i );

     arrayUtil.SortOnX( pairs );
     return pairs;
  }


  private void MakeDisplay( String          title, 
                            String          label, 
                            String          units, 
                            floatPoint2D[]  pairs )
  {
    DataSetFactory ds_factory = new DataSetFactory( title, 
                                                    units, 
                                                   "Theoretical " + label, 
                                                    units, 
                                                   "Measured " + label );

                                           // one entry measured vs theoretical
    DataSet ds = ds_factory.getDataSet();
    Vector unique = new Vector( pairs.length );
    unique.add( pairs[0] );
    for ( int i = 1; i < pairs.length; i++ )
      if ( pairs[i].x > pairs[i-1].x )             // ok to add if increasing
        unique.add( pairs[i] );

    float x[] = new float[ unique.size() ];
    float y[] = new float[ unique.size() ];
    for ( int i = 0; i < x.length; i++ )
    {
      floatPoint2D point = (floatPoint2D)unique.elementAt( i );
      x[i] = point.x;
      y[i] = point.y;
    }
    XScale x_scale = new VariableXScale( x );
    Data d = Data.getInstance( x_scale, y, 1 ); 
    ds.addData_entry( d );

                                           // make second entry with y = x 
    for ( int i = 0; i < y.length; i++ )
      y[i] = x[i];
    d = Data.getInstance( x_scale, y, 2 );
    ds.addData_entry( d );

    ds.setSelectFlag( 0, true );
    ds.setSelectFlag( 1, true );

    new ViewManager( ds, IViewManager.SELECTED_GRAPHS );
  }


  /**
   *  Write all of the parameters names and values to the specified stream
   *
   *  @param  out      The print stream to write to
   *  @param  names    The list of parameter names
   *  @param  values   The list of parameter values
   *  @param  is_used  The list of flags indicating whether or not the 
   *                   parameter is refined.
   */
  private void ShowInitalValues( PrintStream out,
                                 String      names[],
                                 double      values[],
                                 boolean     is_used[]  )
  {
    out.println("------------- Initial Parameter Values ---------------");
    for ( int i = 0; i < values.length; i++ )
    {
      if ( is_used[i] )
        out.print("  ");
      else
        out.print("* ");
      out.println( names[i] +" = " + values[i] );
    }
  }
 
  /**
   *  Write all of the parameters names and values to the specified stream
   *
   *  @param  out    The print stream to write to
   *  @param  names  The list of parameter names
   *  @param  values The list of parameter values
   *  @param  s_dev  One standard deviation error distance in Q
   *  @param  grids  Array of the data grids
   */
  private void WriteAllParams( PrintStream    out, 
                               String         names[], 
                               double         values[],
                               double         s_dev,
                               UniformGrid_d  grids[]    )
  {
    if ( out == null )
    {
      SharedMessages.addmsg("WARNING: Results file doesn't exist" );
      return;
    }

    int max_label_length = 0;
    for ( int i = 0; i < names.length; i++ )
      if ( max_label_length < names[i].length() )
        max_label_length = names[i].length();

    out.println("#");
    out.println("# ALL POSSIBLE CALIBRATION PARAMETERS " ); 
    out.println("# " + (new Date()).toString() );
    out.println("# Lengths in meters");
    out.println("# Times in microseconds");
    out.println("# Angles in degrees");
    out.println("#");
    out.println("# One standard deviation error distance in Q = " + s_dev );
    out.println("#");
    for ( int i = 0; i < grids.length; i++ )
    {
      out.println("# Orientation of Detector " + grids[i].ID() + "-----------");
      double comp[] = grids[i].x_vec().get();
      out.println("# local x_vector:  ( " +
                           Format.real(comp[0], 10, 6, false) + ", " +
                           Format.real(comp[1], 10, 6, false) + ", " +
                           Format.real(comp[2], 10, 6, false) + ") " 
                           );

      comp = grids[i].y_vec().get();
      out.println("# local y_vector:  ( " +
                           Format.real(comp[0], 10, 6, false) + ", " +
                           Format.real(comp[1], 10, 6, false) + ", " +
                           Format.real(comp[2], 10, 6, false) + ") "
                           );

      comp = grids[i].z_vec().get();
      out.println("# detector normal: ( " + 
                           Format.real(comp[0], 10, 6, false) + ", " +
                           Format.real(comp[1], 10, 6, false) + ", " +
                           Format.real(comp[2], 10, 6, false) + ") "
                           );
      out.println("#");
    }
    out.println("#");

    for ( int i = 0; i < values.length; i++ )
    {
      out.print( names[i] + ": " );
      int pad = max_label_length - names[i].length(); 
      for (int space = 0; space < pad; space ++ )
        out.print(" ");
      if ( values[i] >= 0 )
        out.println ( " " + values[i] );
      else
        out.println ( values[i] );
    }
  }


  /**
   *  Read parameter values from a file. 
   *
   *  @param  filename   The name of the file to read from. 
   *  @param  names      The list of parameter names
   *  @param  values     The list of parameter values
   */
  private void ReadParams( String filename, String names[], double values[])
  {
    FileReader fr = null;
    BufferedReader br = null;
    System.out.println("Trying to read parameters: " + filename );
    try
    {
      fr = new FileReader( filename );
      if ( fr == null )
        System.out.println("ERROR: couldn't make FileReader " + filename );
      else
        System.out.println("SUCCESS: made FileReader " + filename );

      br = new BufferedReader( fr );
      String line;
                                                // skip leading comment lines
      line = br.readLine(); 
      System.out.println("READ -> " + line );
      while ( line != null && line.startsWith("#") )
      {
        line = br.readLine(); 
        System.out.println("READ -> " + line );
      }

      String name;
      String val_string;
      double value;
      int    colon_index;
      while ( line != null )
      {
        colon_index = line.indexOf( ":" );
        if ( colon_index > 0 )                // try to find the parameter name
        {
          name = line.substring( 0, colon_index );
          name.trim();
          for ( int i = 0; i < names.length; i++ )
            if ( name.equalsIgnoreCase( names[i] ) )
            {
               val_string = line.substring( colon_index + 1 ); 
               val_string.trim();
               value = Double.parseDouble( val_string );      
               values[i] = value;
            }
        }
        line = br.readLine();
        System.out.println("READ -> " + line );
      } 
      br.close();
      fr.close();
    }
    catch ( Exception e )
    {
      SharedMessages.addmsg("WARNING: Problem reading parameter from file: " );
      SharedMessages.addmsg(" " + filename );
      e.printStackTrace();
    }
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
    String peaksfile  = getParameter(0).getValue().toString();
    String runfile    = getParameter(1).getValue().toString();
    String logname    = "SCDcalib.log";

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

                                                      // open the log file
    PrintStream log_print = null;
    try
    {
      File log_file       = new File( logname );
      OutputStream log_os = new FileOutputStream( log_file );
      log_print = new PrintStream( log_os );
    }
    catch ( Exception e )
    {
      SharedMessages.addmsg("WARNING: Couldn't open log file: " + logname );
    }

    double lattice_params[] = new double[6];
    for ( int i = 0; i < 6; i++ )
      lattice_params[i] = lat_params[i];
                                                    // load the vector of peaks
    Vector peaks = PeakData.ReadPeaks( peaksfile, runfile ); 

    if ( peaks == null || peaks.size() <= 0 )
    {
      return new ErrorString("Failed to read " + peaksfile + " or " + runfile);
    }

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

    if ( read_params )
      ReadParams( param_file, parameter_names, parameters );

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
                                 parameters, 
                                 parameter_names,
                                 n_used, 
                                 is_used,
                                 lattice_params,
                                 log_print );

                                                      // show initial values
    ShowInitalValues( System.out, parameter_names, parameters, is_used );
    ShowInitalValues( log_print, parameter_names, parameters, is_used );

    System.out.println("Before fit... params are");
    log_print.println("Before fit... params are");
    error_f.ShowProgress( System.out );
    error_f.ShowProgress( log_print  );
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

    log_print.println( "==================================================");
    log_print.println( "RESULT OF FIT:");
    log_print.println( fitter.getResultsString() );
    log_print.println( "==================================================");
    System.out.println( "==================================================");
    System.out.println( "RESULT OF FIT:");
    System.out.println( fitter.getResultsString() );
    System.out.println( "==================================================");

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
    error_f.ShowProgress( System.out );
    error_f.ShowProgress( log_print  );

    error_f.ShowOldCalibrationInfo( System.out );
    error_f.ShowOldCalibrationInfo( log_print  );

                                             // log & show row, col, tof errors
    int id_list[] = error_f.getAllGridIDs();
    for ( int count = 0; count < id_list.length; count++ )
    {
      int det_id = id_list[count]; 

      float meas_pos[][] = error_f.getMeasuredPeakPositions( det_id );
      float theo_pos[][] = error_f.getTheoreticalPeakPositions( det_id );

      floatPoint2D row_pairs[] = getPairs( 0, theo_pos, meas_pos );
      floatPoint2D col_pairs[] = getPairs( 1, theo_pos, meas_pos );
      floatPoint2D tof_pairs[] = getPairs( 2, theo_pos, meas_pos );

      log_print.println("Detector Row Number Comparison, ID " + det_id );
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
      log_print.print ("NOTE: The calculated T0 shift should be added to the ");
      log_print.println("measured value ");
      log_print.println("Theoetical     Measured");
      for ( int i = 0; i < tof_pairs.length; i++ )
        log_print.println( Format.real( tof_pairs[i].x, 10, 3 ) + "   " +
                           Format.real( tof_pairs[i].y, 13, 3 )    );


      MakeDisplay( "Theoretical vs Measured Row, ID " + det_id, 
                   "Row", "Number", row_pairs );
      MakeDisplay( "Theoretical vs Measured Column, ID " + det_id, 
                   "Column", "Number", col_pairs );
      MakeDisplay( "Theoretical vs Measured TOF, ID " + det_id, 
                   "Time", "us", tof_pairs );
    }
                                                   // record the results file
    String resultname = DEFAULT_PARAMETERS_FILE;
    PrintStream result_print = null;
    try
    {
      File result_file       = new File( resultname );
      OutputStream result_os = new FileOutputStream( result_file );
      result_print = new PrintStream( result_os );
    }
    catch ( Exception results_exeception )
    {
      SharedMessages.addmsg("WARNING: Couldn't open results file: "
                            + resultname );
    }

    UniformGrid_d grid_arr[] = error_f.getAllGrids();
    double s_dev = error_f.getStandardDeviationInQ();
    WriteAllParams( log_print, parameter_names, parameters, s_dev, grid_arr );
    WriteAllParams( result_print, parameter_names, parameters, s_dev, grid_arr);
    WriteAllParams( System.out, parameter_names, parameters, s_dev, grid_arr );
    error_f.ShowOldCalibrationInfo( result_print  );
    result_print.close();
    log_print.close();

    float results[] = new float[ parameters.length ];
    for ( int i = 0; i < parameters.length; i++ )
      results[i] = (float)parameters[i];

    Vector result_vector = new Vector();
    result_vector.addElement( results );
    result_vector.addElement( parameter_names );

    return result_vector;
  }

}

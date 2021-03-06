/* 
 * File: SNS_Tof_to_Q_map.java
 *
 * Copyright (C) 2009,2010 Dennis Mikkelson
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

package EventTools.EventList;

import java.util.*;
import java.io.*;

import gov.anl.ipns.MathTools.Geometry.*;

import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.dataset.*;
import DataSetTools.math.*;
import DataSetTools.trial.*;
import DataSetTools.util.SharedData;
import DataSetTools.instruments.*;
import EventTools.Histogram.IEventBinner;
import EventTools.Histogram.LogEventBinner;
import EventTools.Histogram.UniformEventBinner;

/** 
 *  This class constructs the time-of-flight to vector Q mapping information
 *  needed to efficiently map raw SNS event data into reciprocal space.
 *  The information can be constructed from an ISAW .DetCal file, or from
 *  the detector position information at the start of a peaks file for the 
 *  instrument.  NOTE: The order and size of the detectors listed in the 
 *  .DetCal or .Peaks file MUST be the same as in the *neutron_event.dat
 *  file and ids must be stored by column in order of increasing row number,
 *  so that the pixel ids match.
 *    The MapEventToQ() method does the actual mapping from (tof,id) to vector
 *  Q. The result is a list of events in Q along with a corresponding list
 *  of weight factors.
 *
 *  Following A.J.Schultz's anvred, the weight factors should be:
 * 
 *    sin^2(theta) / (lamda^4 * spec * eff * trans)
 *
 *  where theta = scattering_angle/2
 *        lamda = wavelength (in angstroms?)
 *        spec  = incident spectrum correction
 *        eff   = pixel efficiency
 *        trans = absorption correction
 *  
 *  The quantity:
 *
 *    sin^2(theta) / eff 
 *
 *  depends only on the pixel and can be pre-calculated 
 *  for each pixel.  It could be saved in array pix_weight[].
 *  For now, pix_weight[] is calculated by the method:
 *  BuildPixWeights() and just holds the sin^2(theta) values.
 *
 *  The wavelength dependent portion of the correction is saved in
 *  the array lamda_weight[].
 *  The time-of-flight is converted to wave length by multiplying
 *  by tof_to_lamda[id], then (int)STEPS_PER_ANGSTROM * lamda
 *  gives an index into the table lamda_weight[].
 *
 *  The lamda_weight[] array contains values like:
 *
 *      1/(lamda^power * spec(lamda))
 *   
 *  which are pre-calculated for each lamda.  These values are
 *  saved in the array lamda_weight[].  The optimal value to use
 *  for the power should be determined when a good incident spectrum
 *  has been determined.  Currently, power=3 when used with an 
 *  incident spectrum and power=2.4 when used without an incident
 *  spectrum.
 *
 *  The pixel efficiency and absorption correction are NOT CURRENTLY USED.
 *  The absorption correction, trans, depends on both lamda and the pixel,
 *  Which is a fairly expensive calulation when done for each event.
 */

public class SNS_Tof_to_Q_map 
{
  public static final float  ANGST_PER_US_PER_M = 
                                        (float)tof_calc.ANGST_PER_US_PER_M;

                                         // These determine the extent and
                                         // resolution of the boolean table
                                         // that filters the regions of Q to map
  public static final double ABSOLUTE_MAX_Q = 100;
  public static final int    NUM_USE_Q_BINS = 100000;

  public static final float radtodeg_half = 180.f/(float)Math.PI/2.f;

  public static final float[][] pc = new float[][] {
       {0.9369f, 0.9490f, 0.9778f, 1.0083f, 1.0295f, 1.0389f, 1.0392f, 1.0338f,
        1.0261f, 1.0180f, 1.0107f, 1.0046f, 0.9997f, 0.9957f, 0.9929f, 0.9909f,
        0.9896f, 0.9888f, 0.9886f},
       {2.1217f, 2.0149f, 1.7559f, 1.4739f, 1.2669f, 1.1606f, 1.1382f, 1.1724f,
        1.2328f, 1.3032f, 1.3706f, 1.4300f, 1.4804f, 1.5213f, 1.5524f, 1.5755f,
        1.5913f, 1.6005f, 1.6033f},
       {-0.1304f, 0.0423f, 0.4664f, 0.9427f, 1.3112f, 1.5201f, 1.5844f, 1.5411f,
        1.4370f, 1.2998f, 1.1543f, 1.0131f, 0.8820f, 0.7670f, 0.6712f, 0.5951f,
        0.5398f, 0.5063f, 0.4955f},
       {1.1717f, 1.0872f, 0.8715f, 0.6068f, 0.3643f, 0.1757f, 0.0446f, -0.0375f,
       -0.0853f, -0.1088f, -0.1176f, -0.1177f, -0.1123f, -0.1051f, -0.0978f,
       -0.0914f, -0.0868f, -0.0840f, -0.0833f}};

  private final float MAX_WAVELENGTH = 50.0f;    // max in lamda_weight table

  private final float STEPS_PER_ANGSTROM = 100;  // resolution of lamda table 

  private final int   NUM_WAVELENGTHS = 
                             Math.round( MAX_WAVELENGTH * STEPS_PER_ANGSTROM );

  private boolean     debug = false;

  private IDataGrid[]  grid_arr;         // list of actual grids
  private IDataGrid[]  all_grids;        // list of possible grids, indexed by
                                         // grid ID.  NULL if there is no grid
                                         // with that ID.
  private VecQMapper[] inverse_mapper;

  private float        L1;               // L1 in meters.

  private float        t0;               // t0 shift in 100ns units

  private float[]      QUxyz;            // Array giving a unit vector in the 
                                         // direction of "Q" corresponding to
                                         // each DAS pixel ID.  The 3D vector
                                         // Q value is obtained by multiplying
                                         // a unit vector time |Q|

  private float[]      tof_to_MagQ;      // Array giving conversion factor from
                                         // time of flight to |Q| for each
                                         // DAS pixel ID. 
                                         // |Q| is tof_to_MagQ[id] / tof

  private float[]      tof_to_lamda;     // Array giving conversion factor from
                                         // time of flight to wavelength for
                                         // each DAS pixel ID.
                                         // lamda is tof_to_lamda[id] * tof/10

  private float[]      recipLaSinTa;     // Array giving time-focusing values
                                         // for each DAS pixel ID.  Contains 
                                         // 1/(La*Sin(Ta)) values where La
                                         // is the actual total path length and
                                         // Ta is the actual theta value.  This
                                         // table is used for time-focusing
                                         // events to a "virtual" path length
                                         // Lv and "virtual" angle Tv.

  private BankInfo[]    bank_info;       // Array of BankInfo objects with 
                                         // entries ONLY for the banks that
                                         // are present.  The information on
                                         // on the bank with ID = k, might NOT
                                         // be stored in position k of the list

  private BankInfo[]   all_bank_infos;   // list of BankInfo objects for each
                                         // possible detector banks.  If there
                                         // is not an actual detector for a
                                         // position in this list, that entry
                                         // is null.  The information on the 
                                         // bank with ID = k WILL BE stored
                                         // in position k of this list.

  private int[]        bank_num;         // Array giving the SNS bank number
                                         // for each DAS pixel ID.  This is
                                         // used for forming a default
                                         // collection of histograms from 
                                         // the raw events 

  private float[]      das_to_row;       // row and column in terms of the DAS
  private float[]      das_to_col;       // pixel ID.

  private int[]        das_to_nex_id;    // Array giving the mapping from a
                                         // DAS pixel id to a NeXus pixel ID.
                                         // NOTE: For some instruments this is
                                         //       the identity mapping

  private int[]        nex_to_das_id;    // Array giving the mapping from a
                                         // NeXus pixel id to a DAS pixel ID.
                                         // -1 is used to mark a missing ID.
                                         // NOTE: For some instruments this is
                                         //       the identity mapping

  private float[]      lamda_weight;     // 1/(lamda^power*spec(lamda)) indexed
                                         // by STEPS_PER_ANGSTROM * lamda

  private float[]      two_theta_map;    // 2*theta(pix_id)

  private float[]      pix_weight;       // sin^2(theta(pix_id)) / eff(pix_id)

  private boolean[]    use_id;           // Array of flags used to mask off
                                         // certain detector pixels.  The
                                         // DAS pixel ID is the index into this
                                         // array

  private boolean[]    use_q;            // Array of flags used to mask of
                                         // certain intervals of |Q|

  private IEventBinner use_q_binner;     // Binner to find index of a |Q| 
                                         // value in the use_q array.

  private float        max_q_to_map;     // Discard all events with Q more 
                                         // than this value

  private float        min_q_to_map;     // Discard all events with Q less 
                                         // than this value

                                         // The following arrays and binners
                                         // are used to discard events in or
                                         // near specific peaks

  private int          num_peaks_discarded;

  private float[]      peak_Qx_min;      // Array listing the extent of each
  private float[]      peak_Qx_max;      // peak in the x direction

  private float[]      peak_Qy_min;      // Array listing the extent of each
  private float[]      peak_Qy_max;      // peak in the y direction

  private float[]      peak_Qz_min;      // Array listing the extent of each
  private float[]      peak_Qz_max;      // peak in the z direction

  private int          max_grid_ID = 0;

  private String            instrument_name = "NO_NAME";
  private int               run_num         = 0;
  private float             monitor_count   = 100000;
  private SampleOrientation orientation     = new SNS_SampleOrientation(0,0,0);
  private float power_th;
  private float radius;
  private float smu;
  private float amu;


  /**
   *  Construct the mapping from (tof,id) to Qxyz, d and time-focused 
   *  spectra from the information at the start of a .peaks file or .DetCal 
   *  file, an SNS bank file, an SNS mapping file and an incident spectrum 
   *  file.  NOTE: There MUST be an entry for each detector in the instrument,
   *  so a .peaks file may not work, if some detectors are missing. 
   *  The instrument name is required, and will be used to find default
   *  det cal, bank, map and incident spectrum files, if these files are
   *  not specified.
   *  An exception will be thrown if specified files are missing or can't 
   *  be read and no default files are found.  If the files are specified,
   *  the files must be consistent and describe the same instrument 
   *  configuration as was in place when the neutron event file(s) that 
   *  will be mapped were written.  The initial spectrum file is optional.
   *
   *  @param  instrument_name   The name of the instrument, such as "TOPAZ".
   *
   *  @param  det_cal_filename  The name of the .DetCal or .peaks file with 
   *                            position information about EVERY detector and
   *                            L1 and t0 values.
   *
   *  @param  bank_filename     The name of the _bank_ XML file containing the 
   *                            SNS detector bank information.
   *
   *  @param  map_filename      The name of the _TS_ binary file containing 
   *                            DAS pixel ID to NeXus pixel ID mapping.
   *
   *  @param  spectrum_filename Name of file containing incident spectrum
   *                            for this instrument.  The spectrum file
   *                            must be an ASCII file giving values for the
   *                            incident spectrum.  The file must be the same
   *                            form as InstrumentInfo/SNS/SNAP_Spectrum.dat
   *                            If no spectrum file is available, pass in null.
   *                            The SNAP spectrum file will be used by default
   *                            for SNAP.
   */
  public SNS_Tof_to_Q_map( String instrument_name,
                           String det_cal_filename,
                           String bank_filename,
                           String map_filename,
                           String spectrum_filename )
         throws IOException
  {
    InitFromSNS_Maps( instrument_name, 
                      det_cal_filename, 
                      bank_filename,
                      map_filename,
                      spectrum_filename );

     this.power_th = 3.0f;
     this.radius = 0.0f;
     this.smu    = 0.0f;
     this.amu    = 0.0f;
  }


  /**
   *  Construct the mapping from (tof,id) to Qxyz, d and time-focused 
   *  spectra from the information at the start of a .peaks file or .DetCal 
   *  file, an SNS bank file, an SNS mapping file and an incident spectrum 
   *  file.  NOTE: There MUST be an entry for each detector in the instrument,
   *  so a .peaks file may not work, if some detectors are missing. 
   *  The instrument name is required, and will be used to find default
   *  det cal, bank, map and incident spectrum files, if these files are
   *  not specified.
   *  An exception will be thrown if specified files are missing or can't 
   *  be read and no default files are found.  If the files are specified,
   *  the files must be consistent and describe the same instrument 
   *  configuration as was in place when the neutron event file(s) that 
   *  will be mapped were written.  The initial spectrum file is optional.
   *
   *  A spherical absorption correction will be made if the radius is 
   *  specified to be greater than 0.  The absorption correction the
   *  same correction as currently done in the ANVRED program, based on 
   *  the paper: C. Q. Dwiggins, jr., Acta Cryst. a31, 395 (1975).
   *
   *  @param  instrument_name   The name of the instrument, such as "TOPAZ".
   *
   *  @param  det_cal_filename  The name of the .DetCal or .peaks file with 
   *                            position information about EVERY detector and
   *                            L1 and t0 values.
   *
   *  @param  bank_filename     The name of the _bank_ XML file containing the 
   *                            SNS detector bank information.
   *
   *  @param  map_filename      The name of the _TS_ binary file containing 
   *                            DAS pixel ID to NeXus pixel ID mapping.
   *
   *  @param  spectrum_filename Name of file containing incident spectrum
   *                            for this instrument.  The spectrum file
   *                            must be an ASCII file giving values for the
   *                            incident spectrum.  The file must be the same
   *                            form as InstrumentInfo/SNS/SNAP_Spectrum.dat
   *                            If no spectrum file is available, pass in null.
   *                            The SNAP spectrum file will be used by default
   *                            for SNAP.
   *  @param  power_th          Power of lamda in BuildLamdaWeights
   *  @param  radius            Radius of the sample in centimeters
   *  @param  smu               Linear scattering coefficient.
   *  @param  amu               Linear absorption coefficient at 1.8 Angstroms.
   */
  public SNS_Tof_to_Q_map( String instrument_name,
                           String det_cal_filename,
                           String bank_filename,
                           String map_filename,
                           String spectrum_filename, 
                           float  power_th,
                           float  radius, 
                           float  smu, 
                           float  amu)
         throws IOException
  {
     this.power_th = power_th;
     InitFromSNS_Maps( instrument_name, 
                      det_cal_filename, 
                      bank_filename,
                      map_filename,
                      spectrum_filename );

     this.radius = radius;
     this.smu    = smu;
     this.amu    = amu;
  }


  /**
   *  Construct the mapping from (tof,id) to Qxyz, d and time-focused 
   *  spectra from the information at the start of a .peaks file or .DetCal 
   *  file, and an incident spectrum file.  NOTE: There MUST be an
   *  entry for each detector in the instrument, so a .peaks file may not 
   *  work, if some detectors are missing.
   *
   *  @param  det_cal_filename  The name of the .DetCal or .peaks file with 
   *                            position information about EVERY detector and
   *                            L1 and t0 values.
   *
   *  @param  spectrum_filename Name of file containing incident spectrum
   *                            for this instrument.  The spectrum file
   *                            must be an ASCII file giving values for the
   *                            incident spectrum.  The file must be the same
   *                            form as InstrumentInfo/SNS/SNAP_Spectrum.dat
   *                            If no spectrum file is available, pass in null.
   *                            The SNAP spectrum file will be used by default
   *                            for SNAP.
   *
   *  @param  instrument_name  Name of the instrument, used to determine
   *                           pixel orderings for event file.
   */
  public SNS_Tof_to_Q_map( String det_cal_filename, 
                           String spectrum_filename,
                           String instrument_name )  
         throws IOException
  {
     InitFromSNS_Maps( instrument_name, det_cal_filename, 
                       null, null, spectrum_filename );
     this.power_th = 3.0f;
     this.radius = 0.0f;
     this.smu = 0.0f;
     this.amu = 0.0f;
  }


  /**
   *  This method initialized the tables using information from 
   *  a .DetCal file, an SNS "bank" file and an SNS "mapping" file.
   *  This should work for SNS diffractometers and spectrometers, provided
   *  the required files are available.
   */
  private void InitFromSNS_Maps( String instrument_name,
                                 String det_cal_filename,
                                 String bank_filename,
                                 String map_filename,
                                 String spectrum_filename )
         throws IOException
  {
    String default_dir = SharedData.getProperty("ISAW_HOME","") +
                         "/InstrumentInfo/SNS/" + instrument_name + "/";
    try
    {
      FileUtil.CheckFile ( det_cal_filename );
    }
    catch ( Exception ex )
    {
      det_cal_filename = default_dir + instrument_name + ".DetCal";
      FileUtil.CheckFile ( det_cal_filename );
    }

    try
    {
      FileUtil.CheckFile ( bank_filename );
    }
    catch ( Exception ex )
    {
      bank_filename = default_dir + instrument_name + "_bank.xml";
      FileUtil.CheckFile ( bank_filename );
    }

    try
    {
      FileUtil.CheckFile ( map_filename );
    }
    catch ( Exception ex )
    {
      map_filename = default_dir + instrument_name + "_TS.dat";
      FileUtil.CheckFile ( map_filename );
    }

    this.instrument_name = instrument_name;

    das_to_nex_id = FileUtil.LoadIntFile( map_filename );

    Vector file_info = FileUtil.LoadDetCal( det_cal_filename );
    grid_arr = (IDataGrid[])(file_info.elementAt(0));

    L1 = (Float)(file_info.elementAt(1));
    t0 = 10*(Float)(file_info.elementAt(2));

    bank_info = FileUtil.LoadBankInfo( bank_filename );
    
    System.out.println("--------------------------------------------");
    System.out.println("Initializing SNS_Tof_to_Q_map.java using....");
    System.out.println("DetCal File  : " + det_cal_filename );
    System.out.println("Bank File    : " + bank_filename );
    System.out.println("Mapping File : " + map_filename );
    System.out.println("--------------------------------------------");

    if ( debug )
    {
      System.out.println("Mapping File has " + das_to_nex_id.length + " IDs");
      for ( int i = 0; i < 10; i++ )
        System.out.println("i = " + i + ",  map[i] = " + das_to_nex_id[i] );
      System.out.println("DetCal  File has " + grid_arr.length + " Grids");
      System.out.println("Bank    File has " + bank_info.length+ " Banks");
    }

    BuildMaps( das_to_nex_id, grid_arr, bank_info );

    SampleOrientation orient = new SNS_SampleOrientation( 0, 0, 0 );
    inverse_mapper = new VecQMapper[ grid_arr.length ];
    for ( int i = 0; i < grid_arr.length; i++ )
      inverse_mapper[i] = new VecQMapper( grid_arr[i], L1, t0/10, orient );

    if ( spectrum_filename == null || spectrum_filename.trim().length() == 0 )
      spectrum_filename = null;

    BuildLamdaWeights( spectrum_filename );

    use_q_binner = new UniformEventBinner( 0, ABSOLUTE_MAX_Q, NUM_USE_Q_BINS );
    use_q        = new boolean[ NUM_USE_Q_BINS ];
    for ( int i = 0; i < NUM_USE_Q_BINS; i++ )
      use_q[i] = true;

    min_q_to_map = 0;                         // default values, to map all Qs
    max_q_to_map = (float)ABSOLUTE_MAX_Q; 

    num_peaks_discarded = 0;
/*
    int[] mask_ids = { 1, 2, 3,      6, 7, 8, 9, 10, 11, 12, 13, 14 };
    int[] grid_4   = { 4 };
    int[] grid_5   = { 5 };
    int[] col_ids = { 20,21,22,23,24,25, 100,101,102,103,104,105 };
    int[] row_ids = { 20,21,22,23,24,25, 50,51,52,53,54,55 };
    maskOffDetectors( mask_ids );
    maskOffDetectorColumns( grid_4, col_ids );
    maskOffDetectorRows   ( grid_5, row_ids );
*/
  }


 /**
  * Get the L1 value (in meters) read from the .DetCal file.
  *
  * @return the L1 value converted to meters, read from the .DetCal file
  */
  public float getL1()
  {
    return L1;
  }


 /**
  * Get the T_zero shift value (in microseconds)  read from the .DetCal file.
  *
  * @return the T_zero shift value as read from the .DetCal file
  */
  public float getT0()
  {
    return t0/10;
  }


 /**
  * Get the detector grid with the specified bank ID.
  *
  * @param det_id  The grid ID of detector grid that is requested.
  *
  * @return A reference to the IDataGrid object with the detector information
  *         if there is one for the specified bank_id, or null if not.
  */
  public IDataGrid getIDataGrid( int det_id )
  {
    boolean   found = false;                    // find the IDataGrid with
    IDataGrid grid  = null;                     // the correct ID
    int i = 0;
    while ( !found && i < grid_arr.length )
    {
      grid = grid_arr[i];
      if ( grid.ID() == det_id )
        found = true;
      else
        i++;
    }

    return grid; 
  }


 /**
  *  Get a list of all of the detector grid (aka bank or module) IDs used
  *  by this mapper.
  *
  *  @return Array of ints containing the list of the grid IDs used by 
  *          this mapper.
  */
  public int[] getGridIDs()
  {
    int[] grid_ids = new int[ grid_arr.length ];

    for ( int i = 0; i < grid_arr.length; i++ )
      grid_ids[i] = grid_arr[i].ID();

    return grid_ids;
  }


 /**
  * Set the "q filter" to discard events with |Q| greater than the
  * specified max_q.
  *
  * @param max_q  The maximum magnitude that the Q value of an event can
  *               have to be mapped to Q.  Events with a larger |Q| will 
  *               just be ignored.
  */
  public void setMaxQ( float max_q )
  {
    if ( max_q <= 0 || max_q >= ABSOLUTE_MAX_Q )      // fix invalid max_q
      max_q = (float)ABSOLUTE_MAX_Q;

    max_q_to_map = max_q;  
  }


 /**
  * Get the current value of maximum |Q| that this mapper is set to map.
  *
  * @return  The maximum magnitude that the Q value of an event can
  *          have to be mapped to Q. 
  */
  public float getMaxQ()
  {
    return max_q_to_map;
  }



 /**
  * Set the "q filter" to discard events with |Q| less than the
  * specified min_q.
  *
  * @param min_q  The minimum magnitude that the Q value of an event can
  *               have to be mapped to Q.  Events with a smaller |Q| will 
  *               just be ignored.
  */
  public void setMinQ( float min_q )
  {
    if ( min_q <= 0 || min_q >= ABSOLUTE_MAX_Q )      // fix invalid min_q
      min_q = 0;

    min_q_to_map = min_q;
  }


 /**
  * Get the current value of minimum |Q| that this mapper is set to map.
  *
  * @return  The minimum magnitude that the Q value of an event can
  *          have to be mapped to Q. 
  */
  public float getMinQ()
  {
    return min_q_to_map;
  }


 /**
  * Specify what intervals of |Q| should be omitted.  Any events with
  * |Q| inside of these intervals will, or will not be mapped, depending 
  * on the value of the omit_flag.  NOTE: This will
  * set the entire list of flags indicating what |Q| value should be used,
  * so it will overwrite any previously set flags.
  *
  * @param endpoints  Array of floats specifying the endpoints a0,b0,a1,b2,...
  *                   of the intervals [a0,b0], [a1,b1], etc. that should be
  *                   omitted (or kept) when mapping events to Q.  The values 
  *                   must be ordered so that ai<=bi.  If this is not satisfied
  *                   for a pair of entries, that pair is ignored. 
  *                   NOTE: This method makes an array of
  *                   boolean flags indicating whether a |Q| should be used 
  *                   or not.  The resolution of this table is currently set
  *                   to 0.001 inverse Angstroms, so the endpoint values are
  *                   only used to that precision.
  *
  * @param omit_flag  boolean flag indicating whether events with |Q| values
  *                   in the specified intervals should be omitted or kept.
  *                   If omit_flag is TRUE, Qs in the specified intervals
  *                   will be omitted, and all other Qs will be kept.  
  *                   If omit_flag is FALSE, Qs in the specified intervals
  *                   will be kept and all other Qs will be omitted.  
  */
  public void setQ_Filter( float[] endpoints, boolean omit_flag )
  {
    if ( endpoints == null )
      throw new IllegalArgumentException("Array of endpoints is null!");
      
    if ( endpoints.length < 2 )
      throw new IllegalArgumentException("Need at least two endpoints, got " +
                                          endpoints.length );

    for ( int i = 0; i < NUM_USE_Q_BINS; i++ )      // initialize all flags to 
      use_q[i] = omit_flag;                         // the default state.

    boolean use_flag = !omit_flag;
    for ( int i = 0; i < endpoints.length - 1; i += 2 )
    {
      float a = endpoints[ i   ];
      float b = endpoints[ i+1 ];
      int a_index = use_q_binner.index( a );
      int b_index = use_q_binner.index( b );
      if ( a <= b && a_index < NUM_USE_Q_BINS && b_index >= 0 )
      {
        if ( a_index < 0 )
          a_index = 0;
        if ( b_index >= NUM_USE_Q_BINS )
          b_index = NUM_USE_Q_BINS - 1;
        for ( int k = a_index; k <= b_index; k++ )  // mark specified |Q|s
          use_q[k] = use_flag;                      // to be omitted or kept
      }
    }
                                                    // as a sanity check, see
    boolean some_enabled = false;                   // if any Qs would be kept
    int index = 0;
    while ( !some_enabled && index < NUM_USE_Q_BINS )
      if ( use_q[ index ] )
        some_enabled = true;
      else
        index++;

    if ( !some_enabled )                            // invalid set of intervals
      for ( int k = 0; k < NUM_USE_Q_BINS; k++ )    // so reset all |Q|s to be 
        use_q[k] = true;                            // used
  }


 /**
  * Specify what NeXus IDs should actually be used.  Any event with NeXus ID
  * greater than flags.length or for which flags[das_id] is false will
  * NOT be processed on future calls to MapEventsToQ().  The index das_id
  * is the pixel ID used by the Data Acquisition System that corresponds to
  * the NeXus ID used to index the array of boolean values.  NOTE: This will
  * set the entire list of ids to be used, so it will overwrite any
  * previously values set by setDAS_ID_Filter() or by setNeXus_ID_Filter().
  *
  * @param flags  Array of boolean values specifying which NeXus IDs should
  *               be used when mapping events to Q.  In most cases,
  *               the list of boolean values should have an entry for
  *               each possible NeXus id.
  */
  public void setNeXus_ID_Filter( boolean[] flags )
  {
    if ( flags == null )
      throw new IllegalArgumentException("Array of flags is null!");

    for ( int i = 0; i < use_id.length; i++ )
      use_id[i] = false;

    int das_id;
    int last_id = Math.min( flags.length, nex_to_das_id.length ) - 1;
    for ( int i = 0; i <= last_id; i++ )
    {
      das_id = nex_to_das_id[ i ];
      if ( das_id >= 0 && das_id < use_id.length )
        use_id[das_id] = flags[i];
    }
  }


 /**
  * Specify what DAS IDs should actually be used.  Any event with das_id
  * greater than flags.length or for which flags[das_id] is false will
  * NOT be processed on future calls to MapEventsToQ(). NOTE: This will
  * set the entire list of ids to be used, so it will overwrite any
  * previously set values specifying which IDs to use.
  *
  * @param flags   Array of boolean values specifying which IDs should
  *                be used when mapping events to Q.  In most cases,
  *                the list of boolean values should have an entry for
  *                each possible DAS id.
  */
  public void setDAS_ID_Filter( boolean[] flags )
  {
    if ( flags == null )
      throw new IllegalArgumentException("Array of flags is null!");

    int last_id = Math.min( flags.length, use_id.length ) - 1;

    for ( int i = last_id; i < use_id.length; i++ )
      use_id[i] = false;

    for ( int i = 0; i <= last_id; i++ )
      use_id[i] = flags[i];
  }


  /**
   *  Reset all use_id flags to true so that no events will be discarded
   *  based on their pixel ID.
   */
  public void clear_ID_Filter()
  {
    for ( int i = 0; i < use_id.length; i++ )
      use_id[i] = true;
  }


 /**
  *  Mask off all pixels in specified detectors.  The use_id flags for
  *  all pixels in all the detector will be set to false, without changing 
  *  the flag values for other detectors pixels.  If a grid number is 
  *  not a valid module ID, this method will have no effect.
  *
  *  @param grid_ids List of IDs for entire detectors that should be
  *                  omitted from the data.
  */
  public void maskOffDetectors( int[] grid_ids )
  {
    if ( grid_ids == null )
      return;

    for ( int k = 0; k < grid_ids.length; k++ )
    {
      int id = grid_ids[k];
      if ( all_bank_infos[ id ] != null )     
      {
        int first_nexus_id = all_bank_infos[ id ].first_NeXus_id();
        int last_nexus_id  = all_bank_infos[ id ].last_NeXus_id();

        for ( int i = first_nexus_id; i <= last_nexus_id; i++ )
          use_id[ nex_to_das_id[i] ] = false;
      }
    }
  }


 /**
  *  Mask off all pixels in specified detector columns.  The use_id flags for
  *  all pixels in the specified columns of the specified detectors will be 
  *  set to false, without changing other flag values.
  *  If a grid number or column number is not valid, this method will have 
  *  no effect for that detector or column.  NOTE: SNS detectors are arranged
  *  in column major order!
  *
  *  @param grid_ids List of IDs for detectors from which the specified
  *                  columns should be omitted from the data.
  *
  *  @param col_ids  The list of column_ids that should be omitted from the
  *                  specified detectors.  NOTE: column ids are assumed to
  *                  start at ONE and increase up to and including the 
  *                  number of columns in the detector.

  */
  public void maskOffDetectorColumns( int[] grid_ids, int[] col_ids )
  {
    if ( grid_ids == null || col_ids == null )
      return;

    for ( int k = 0; k < grid_ids.length; k++ )
    {
      int id = grid_ids[k];

      if ( all_bank_infos[ id ] != null )
      {
        int first_nexus_id = all_bank_infos[ id ].first_NeXus_id();
        int last_nexus_id  = all_bank_infos[ id ].last_NeXus_id();
        int n_rows         = all_bank_infos[ id ].num_rows();
        int n_cols         = all_bank_infos[ id ].num_cols();
        int col_start,
            col_num,
            col_end;

        for ( int j = 0; j < col_ids.length; j++ )
        {
          col_num = col_ids[j];
          if ( col_num >= 1 && col_num <= n_cols )
          {
            col_start = first_nexus_id + (col_num -  1) * n_rows;
            col_end   = col_start + n_rows - 1;
            for ( int i = col_start; i <= col_end; i++ )
              use_id[ nex_to_das_id[i] ] = false;
          }
        }
      }
    }
  }


 /**
  *  Mask off all pixels in specified detector rows.  The use_id flags for
  *  all pixels in the specified rows of the specified detectors will be 
  *  set to false, without changing other flag values.
  *  If a grid number or row number is not valid, this method will have 
  *  no effect for that detector or row.  NOTE: SNS detectors are arranged
  *  in column major order!
  *
  *  @param grid_ids List of IDs for detectors from which the specified
  *                  columns should be omitted from the data.
  *
  *  @param row_ids  The list of row_ids that should be omitted from the
  *                  specified detectors.  NOTE: row_ids are assumed to
  *                  start at ONE and increase to and including the number 
  *                  of rows in the detector.
  */
  public void maskOffDetectorRows( int[] grid_ids, int[] row_ids )
  {
    if ( grid_ids == null || row_ids == null )
      return;

    for ( int k = 0; k < grid_ids.length; k++ )
    {
      int id = grid_ids[k];

      if ( all_bank_infos[ id ] != null )
      {
        int first_nexus_id = all_bank_infos[ id ].first_NeXus_id();
        int last_nexus_id  = all_bank_infos[ id ].last_NeXus_id();
        int n_rows         = all_bank_infos[ id ].num_rows();
        int n_cols         = all_bank_infos[ id ].num_cols();
        int row_start,
            row_end;

        for ( int j = 0; j < row_ids.length; j++ )
        {  
          int row_num = row_ids[j];
          if ( row_num >= 1 && row_num <= n_rows )
          {
            row_start = first_nexus_id + row_num - 1;
            row_end   = row_start + (n_cols-1) * n_rows;
            for ( int i = row_start; i <= row_end; i+= n_rows )
              use_id[ nex_to_das_id[i] ] = false;
          }
        }
      }
    }
  }


  /**
   *  Set the list of peak regions for which events will be discarded.
   *  
   *  @param peak_info  Two-dimensional array of floats, with one row
   *                    for each peak.  Each row must contain six values,
   *                    qx, qy, qz, delta_qx, delta_qy and delta_qz in 
   *                    that order.
   */
  public boolean setDiscardedPeaksList( float[][] peak_info )
  {
    peak_Qx_min = new float[ peak_info.length ]; 
    peak_Qx_max = new float[ peak_info.length ]; 
    peak_Qy_min = new float[ peak_info.length ]; 
    peak_Qy_max = new float[ peak_info.length ]; 
    peak_Qz_min = new float[ peak_info.length ]; 
    peak_Qz_max = new float[ peak_info.length ]; 

    for ( int i = 0; i < peak_info.length; i++ )
    {
      peak_Qx_min[i] = peak_info[i][0] - peak_info[i][3];
      peak_Qx_max[i] = peak_info[i][0] + peak_info[i][3];

      peak_Qy_min[i] = peak_info[i][1] - peak_info[i][4];
      peak_Qy_max[i] = peak_info[i][1] + peak_info[i][4];

      peak_Qz_min[i] = peak_info[i][2] - peak_info[i][5];
      peak_Qz_max[i] = peak_info[i][2] + peak_info[i][5];
    }
     
    num_peaks_discarded = peak_info.length;
    System.out.println("**** Discarding " + num_peaks_discarded );
/*
    for ( int i = 0; i < num_peaks_discarded; i++ )
      System.out.printf( "%f6.2  %f6.2  %f6.2  %f6.2  %f6.2  %f6.2\n",
                          peak_info[i][0], peak_info[i][1], peak_info[i][2],
                          peak_info[i][3], peak_info[i][4], peak_info[i][5] );
*/
    return true;
  }


  /**
   *  Reset the list of peak regions to discard to be empty.
   */
  public void clearDiscardedPeaksList()
  {
    num_peaks_discarded = 0;
    peak_Qx_min = null;
    peak_Qx_max = null;
    peak_Qy_min = null;
    peak_Qy_max = null;
    peak_Qz_min = null;
    peak_Qz_max = null;
  }


  /**
   *  Check whether an event with the specified pixel id and |Q| is in
   *  one of the peaks to be discarded.
   *
   *  @param  id    The pixel id where the event occurred.
   *  @param  magQ  The |Q| for the event.
   *
   *  @return true if the event is in a discarded peak region and false 
   *          otherwise.
   */
  private boolean EventInDiscardedPeak( int id, float magQ ) 
  {
    if ( num_peaks_discarded <= 0 )
      return false;

    int id_offset = 3*id;
    float qx = magQ * QUxyz[id_offset++];
    float qy = magQ * QUxyz[id_offset++];
    float qz = magQ * QUxyz[id_offset  ];
                                           // check all peaks.  If event is in
                                           // small box around any one peak, 
                                           // return true to discard the event 
    for ( int i = 0; i < num_peaks_discarded; i++ )
      if ( peak_Qx_min[i] < qx && qx < peak_Qx_max[i] &&
           peak_Qy_min[i] < qy && qy < peak_Qy_max[i] &&
           peak_Qz_min[i] < qz && qz < peak_Qz_max[i] )
        return true;

    return false;
  }


 /**
  *  Set the parameters that control the absorption correction calculation.
  *  If any of the parameters are negative, the corresponding value will be 
  *  set to 0.
  *
  *  @param  power_th          Power of lamda in BuildLamdaWeights
  *  @param  radius  Radius of the sample in centimeters
  *  @param  smu     Linear scattering coefficient at 1.8 Angstroms.
  *  @param  amu     Linear absorption coefficient at 1.8 Angstroms.
  */
  public void setAbsorptionParameters( float  power_th, 
                                       float  radius, 
                                       float  smu, 
                                       float  amu )
  {
     this.power_th = power_th;
     this.radius = radius;
     this.smu    = smu;
     this.amu    = amu;
  }


 /**
  *  Get a double array containing a dspace mapping file corresponding to
  *  the current instrument.
  *
  *  @return an array of doubles containing the diffractometer constants
  *
  */
  public double[] getDspaceMapArray()
  {
    double[]  dspace_map = new double[ tof_to_MagQ.length ];
    for ( int i = 0; i < dspace_map.length; i++ )
      dspace_map[i] = 2 * Math.PI / tof_to_MagQ[i];

    return dspace_map;
  }


  /**
   *  Map the specified sub-list of time-of-flight events to a packed
   *  array of events in reciprocal space, listing Qx,Qy,Qz for each event,
   *  interleaved in one array.  The weight field will be set to 0 for
   *  events with invalid ids.  For events with valid ids, the weight
   *  field is set to the product of a weight based on the pixel id, and
   *  a weight based on the neutron wavelength corresponding that event.
   *  NOTE: Since the resulting Qxyz values are packed in a one-dimensional
   *        array, at most (2^31-1)/12 = 178.9 million events can be processed
   *        in one batch by this method.
   *
   *  @param event_list  List of (tof,id) specifying detected neutrons.
   *
   *  @param first       The index of the first event to map to Q
   *
   *  @param num_to_map  The number of events to map to Q
   *
   *  @return FloatArrayEventList3D object containing an array of floats 
   *          with (Qx,Qy,Qz) for each event, and the list of weights for
   *          each event.
   */
  public FloatArrayEventList3D MapEventsToQ( ITofEventList event_list,
                                             long  first,
                                             long  num_to_map )
  {
     int num_mapped = CheckAndGetNumToMap( event_list, first, num_to_map );

     int     id;
     int     id_offset;
     int     index;
     int     mapped_index;
     float   tof_chan;
     float   magQ;
     float   qx,qy,qz;
     int     minus_id_count = 0;
     int     large_id_count = 0;
     float   lamda;
     int     lamda_index;
     float   transinv = 1.0f;
                                                 // get the sublist of all of
                                                 // the events we need to map
     int[] my_events = event_list.rawEvents( first, num_mapped );
/*
     int[] used_ids = new int[ QUxyz.length ];   // this is 3 times larger
                                                 // than should be needed.
*/
     int ev_index = 0;
     mapped_index = 0;
                                                 // First scan for how many
                                                 // events pass the filters
                                                 // so we only need to  
                                                 // allocate new arrays once 
     int ok_counter  = 0;
     int use_q_index = 0;
/*
     int neg_count = 0;
     for ( int i = 0; i < num_mapped; i+=2 )
       if ( my_events[i] < 0 )
         neg_count++;
     System.out.println("Negative time occurred " + neg_count + " times");
*/
     for ( int i = 0; i < num_mapped; i++ )
     {
       tof_chan = my_events[ ev_index++ ] + t0; 
       id       = my_events[ ev_index++ ]; 
       if ( id >= 0 && id < tof_to_MagQ.length )
       {
         if ( use_id[ id ] && tof_chan > 0 )
         {
           magQ = tof_to_MagQ[id]/tof_chan;
           use_q_index = use_q_binner.index( magQ );
           if ( magQ >= min_q_to_map         &&
                magQ <= max_q_to_map         &&
                use_q_index >= 0             && 
                use_q_index < NUM_USE_Q_BINS && 
                use_q[ use_q_index ]  )
           {                 
             if ( num_peaks_discarded <= 0  ||
                 !EventInDiscardedPeak( id, magQ ) )
               ok_counter++;
           }
         }
       }
     }
                                                  // Now allocate right-size
                                                  // arrays and process events
     float[] Qxyz    = new float[ 3 * ok_counter ];
     float[] weights = new float[ ok_counter ];

     ev_index = 0;                                // start over at start of
                                                  // the part we're mapping
     for ( int i = 0; i < num_mapped; i++ )
     {
       tof_chan = my_events[ ev_index++ ] + t0; 
       id       = my_events[ ev_index++ ]; 
/*
       if ( id > 0 && id < used_ids.length )
         used_ids[id]++;
*/
       if ( id < 0 )
         minus_id_count++;

       else if ( id >= tof_to_MagQ.length )
         large_id_count++;

       else if ( use_id[ id ] && tof_chan > 0 )
       {
         magQ = tof_to_MagQ[id]/tof_chan;
         use_q_index = use_q_binner.index( magQ );
         if ( magQ >= min_q_to_map         &&
              magQ <= max_q_to_map         &&
              use_q_index >= 0             && 
              use_q_index < NUM_USE_Q_BINS && 
              use_q[ use_q_index ]  )
         {
           if ( num_peaks_discarded <= 0  ||
               !EventInDiscardedPeak( id, magQ ) )
           {
             id_offset = 3*id;
             qx = magQ * QUxyz[id_offset++];
             qy = magQ * QUxyz[id_offset++];
             qz = magQ * QUxyz[id_offset  ];

             index = mapped_index * 3;
             Qxyz[index++] = qx;
             Qxyz[index++] = qy;
             Qxyz[index  ] = qz;

                                       // NOTE: If the following calculations
                                       //       are modified, the method
                                       //       getEventWeight must be updated.
             lamda = tof_chan/10.0f * tof_to_lamda[id];
             lamda_index = (int)( STEPS_PER_ANGSTROM * lamda );

             if ( lamda_index < 0 )
               lamda_index = 0;
             if ( lamda_index >= lamda_weight.length )
               lamda_index = lamda_weight.length - 1;

             if ( radius > 0 )
             {
               transinv = absor_sphere(two_theta_map[id], lamda);
               weights[mapped_index] =
                     pix_weight[id] * lamda_weight[ lamda_index ] * transinv;
             }
             else
               weights[mapped_index] =
                       pix_weight[id] * lamda_weight[ lamda_index ];

             mapped_index++;
           }
         }
       }

     }
/*
     int[] bank_nums_used = new int[500];
     for ( int i = 0; i < used_ids.length; i++ )
     {
      if ( used_ids[i] > 100 )
        bank_nums_used[ i/1250 ]++;          // Test for PG3, to check which
     }                                       // banks were used

     for ( int i = 0; i < bank_nums_used.length; i++ )
     {
      if ( bank_nums_used[i] > 0 )
        System.out.println( "" + i + "      " + bank_nums_used[i] );
     }
*/
     return new FloatArrayEventList3D( weights, Qxyz );
  }


  /**
   * Map the specified sub-list of time-of-flight events to a packed array
   * of floats containing eight values for each event.  The eight values are 
   * qx, qy, qz, gridID, row, column, time-of-flight and wavelength for that
   * event.
   *
   *  NOTE: Since eight values for each event are packed in a one-dimensional
   *        array, at most (2^31-1)/32 = 67.1 million events can be processed
   *        in one batch by this method.
   *
   *  @param event_list  List of (tof,id) specifying detected neutrons.
   *
   *  @param first       The index of the first event to process
   *
   *  @param num_to_map  The number of events to process 
   *
   *  @return an array of floats containing eight values 
   *          (Qx,Qy,Qz,gridID,row,col,tof,lamda) for each event, interleaved
   *          in the array.
   */

  public float[]  MapEventsTo_Q_ID_Row_Col( ITofEventList event_list,
                                             long  first,
                                             long  num_to_map )
  {
     int num_mapped = CheckAndGetNumToMap( event_list, first, num_to_map );

     int     id;
     int     id_offset;
     int     index;
     int     mapped_index;
     float   tof_chan;
     float   tof;
     float   magQ;
     float   qx,qy,qz;
     int     minus_id_count = 0;
     int     large_id_count = 0;
     float   lamda;
     int     lamda_index;
     float   transinv = 1.0f;
                                                 // get the sublist of all of
                                                 // the events we need to map
     int[] my_events = event_list.rawEvents( first, num_mapped );
     int ev_index = 0;
     mapped_index = 0;
                                                 // First scan for how many
                                                 // events pass the filters
                                                 // so we only need to  
                                                 // allocate new arrays once 
     int ok_counter  = 0;
     int use_q_index = 0;
     for ( int i = 0; i < num_mapped; i++ )
     {
       tof_chan = my_events[ ev_index++ ] + t0;
       id       = my_events[ ev_index++ ];
       if ( id >= 0 && id < tof_to_MagQ.length )
       {
         if ( use_id[ id ] && tof_chan > 0 )
         {
           magQ = tof_to_MagQ[id]/tof_chan;
           use_q_index = use_q_binner.index( magQ );
           if ( magQ >= min_q_to_map         &&
                magQ <= max_q_to_map         &&
                use_q_index >= 0             &&
                use_q_index < NUM_USE_Q_BINS &&
                use_q[ use_q_index ]  )
           {
             if ( num_peaks_discarded <= 0  ||
                 !EventInDiscardedPeak( id, magQ ) )
               ok_counter++;
           }
         }
       }
     }
                                                  // Now allocate right-size
                                                  // arrays and process events
     float[] result = new float[ 8 * ok_counter ];

     ev_index = 0;                                // start over at start of
                                                  // the part we're mapping
     for ( int i = 0; i < num_mapped; i++ )
     {
       tof_chan = my_events[ ev_index++ ] + t0;
       tof      = tof_chan/10.0f;

       id       = my_events[ ev_index++ ];
       if ( id < 0 )
         minus_id_count++;

       else if ( id >= tof_to_MagQ.length )
         large_id_count++;

       else if ( use_id[ id ] && tof_chan > 0 )
       {
         magQ = tof_to_MagQ[id]/tof_chan;
         use_q_index = use_q_binner.index( magQ );
         if ( magQ >= min_q_to_map         &&
              magQ <= max_q_to_map         &&
              use_q_index >= 0             &&
              use_q_index < NUM_USE_Q_BINS &&
              use_q[ use_q_index ]  )
         {
           if ( num_peaks_discarded <= 0  ||
               !EventInDiscardedPeak( id, magQ ) )
           {
             id_offset = 3*id;
             qx = magQ * QUxyz[id_offset++];
             qy = magQ * QUxyz[id_offset++];
             qz = magQ * QUxyz[id_offset  ];

             index = mapped_index * 8;
             result[index++] = qx;
             result[index++] = qy;
             result[index++] = qz;
             result[index++] = bank_num[id];
             result[index++] = das_to_row[id];
             result[index++] = das_to_col[id];
             result[index++] = tof;
             result[index  ] = tof * tof_to_lamda[id];

             mapped_index++;
           }
         }
       }

     }
     return result; 
  }




 /**
  *  Get the weight factor that would be used for an event occuring 
  *  at the specified wavelength, with the specified two_theta value.
  *
  *  @param  lamda      The wavelength of an event.
  *  @param  two_theta  The scattering angle of the event.
  *
  *  @return The weight factor for the specified position and wavelength.
  */
  public float getEventWeight( float lamda, float two_theta )
  {
    int   lamda_index = (int)( STEPS_PER_ANGSTROM * lamda );

    if ( lamda_index < 0 )
      lamda_index = 0;
    else if ( lamda_index >= lamda_weight.length )
      lamda_index = lamda_weight.length - 1;

    float lamda_w     = lamda_weight[ lamda_index ];

    float transinv = 1;
    if ( radius > 0 )
       transinv = absor_sphere(two_theta, lamda);

    float sin_theta = (float)Math.sin( two_theta/2 );
    float pix_weight = sin_theta * sin_theta;
    
    float event_weight = pix_weight * lamda_w * transinv;

    return event_weight;
  }


  /**
   *  Map the specified sub-list of time-of-flight events to a packed 
   *  array of events in reciprocal space, listing Qx,Qy,Qz for each event, 
   *  interleaved in one array.  NOTE: due to array size limitations in Java,
   *  at most (2^31-1)/3 = 715.8 million events can be processed in one
   *  batch by this method. 
   *
   *  @param tofs        List of integer time-of-flight values, giving the 
   *                     number of 100ns clock ticks since t0 for this event.
   *
   *  @param ids         List of detector pixel ids corresponding to the listed
   *                     tofs.
   *
   *  @param first       The index of the first event to map to Q
   *
   *  @param num_to_map  The number of events to map to Q
   *
   *  @return an array of floats containing values (Qx,Qy,Qz) for each 
   *          event, interleaved in the array. 
   *
   *  @deprecated The method using an interleaved array of (tof,id) pairs
   *              is more efficient than this method, and should be used
   *              whenever possible.
   */
  @Deprecated
  public FloatArrayEventList3D MapEventsToQ( int[] tofs, 
                                             int[] ids,
                                             int   first,
                                             int   num_to_map )
  {
     if ( tofs == null )
       throw new IllegalArgumentException( "Time-of-flight array is null" );

     if ( ids == null )
       throw new IllegalArgumentException( "Pixel id array is null" );

     if ( tofs.length != ids.length )
       throw new IllegalArgumentException("TOF array length " + tofs.length +
                                         " != id array length " + ids.length);

     if ( tofs.length > Integer.MAX_VALUE/12 )
       throw new IllegalArgumentException("TOF array length " + tofs.length +
                                         " exceeds " + Integer.MAX_VALUE/12 );

     if ( first < 0 || first >= tofs.length )
       throw new IllegalArgumentException("First index: " + first +
                                         " < 0 or >= " + tofs.length );

     if ( tofs.length == 0 )
     {
       float[] empty_Qxyz = new float[0];
       return new FloatArrayEventList3D( null, empty_Qxyz );
     }

     if ( first == 0 )
     {
       ITofEventList raw_events = new TofEventList( tofs, ids );
       return MapEventsToQ( raw_events, 0, num_to_map );
     }
     else              // construct TofEventList from part of tofs[] and ids[] 
     {
       int[] new_tofs = new int[ num_to_map ];
       int[] new_ids  = new int[ num_to_map ];

       System.arraycopy( tofs, first, new_tofs, 0, num_to_map );
       System.arraycopy( ids,  first, new_ids,  0, num_to_map );

       ITofEventList raw_events = new TofEventList( new_tofs, new_ids );
       return MapEventsToQ( raw_events, 0, num_to_map );
     }
  }


  /**
   *  Map the specified time-of-flight events to a packed array of events
   *  in reciprocal space, listing Qx,Qy,Qz for each event, interleaved
   *  in one array.  NOTE: due to array size limitations in Java,
   *  at most (2^31-1)/3 = 715.8 million events can be processed in one
   *  batch by this method. 
   *
   *  @param tofs  List of integer time-of-flight values, giving the number
   *               of 100ns clock ticks since t0 for this event.
   *
   *  @param ids   List of detector pixel ids corresponding to the listed
   *               tofs.
   *
   *  @return an array of floats containing values (Qx,Qy,Qz) for each 
   *          event, interleaved in the array. 
   *
   *  @deprecated The method using an interleaved array of (tof,id) pairs
   *              is more efficient than this method, and should be used
   *              whenever possible.
   */
  @Deprecated
  public FloatArrayEventList3D MapEventsToQ( int[] tofs, int[] ids)
  {
    if ( tofs == null || ids == null )
      throw new IllegalArgumentException( "Time-of-flight array is null" );

    int first = 0;
    int num_to_map = tofs.length;
    return MapEventsToQ( tofs, ids, first, num_to_map );
  }


  /**
   *  Map the specified sub-list of time-of-flight events to a 
   *  list of "d-spacing" histograms.
   *
   *  @param event_list  List of (tof,id) specifying detected neutrons.
   *
   *  @param first       The index of the first event to map to d
   *
   *  @param num_to_map  The number of events to map to d
   *
   *  @param binner      The IEventBinner object that defines the bin
   *                     boundaries for the histogram bins
   *                     
   *  @param d_map       List of diffractometer constants.  If this is 
   *                     null, the instrument geometry will determine the
   *                     mapping from time-of-flight to d.
   *
   *  @return A two dimensional array of integers.  The kth row of this 
   *          array contains the histogram values for detector bank k.
   *          If detector bank k does not exist, that row will be null.
   */
  public int[][] Make_d_Histograms( ITofEventList event_list,
                                    long          first,
                                    long          num_to_map,
                                    IEventBinner  binner,
                                    double[]      d_map )
  {
    int num_mapped = CheckAndGetNumToMap( event_list, first, num_to_map );

    boolean  use_d_map = true;
    if (d_map == null || d_map.length < tof_to_MagQ.length )
      use_d_map = false;

    float  tof_chan;
    int    id;
    double two_pi = Math.PI * 2;
    double d_value;

    int    ev_index = 0;                      // index into event array
    int    index;                             // index into histogram bin
    int    num_bins = binner.numBins();
    int    grid_id;

    int[]   my_events = event_list.rawEvents( first, num_mapped );
    int[][] histogram = getEmptyIntHistogram( binner );

    for ( int i = 0; i < num_mapped; i++ )
    {
      tof_chan = my_events[ ev_index++ ] + t0;
      id       = my_events[ ev_index++ ];

      if ( id >= 0 && id < tof_to_MagQ.length )
      {
        if ( use_d_map )
          d_value = tof_chan * d_map[ id ];
        else
          d_value = two_pi * tof_chan / tof_to_MagQ[id];

        index   = binner.index( d_value );

        if ( index >= 0 && index < num_bins )
        {
          grid_id = bank_num[ id ];
          if ( histogram[ grid_id ] != null )
            histogram[ grid_id ][ index ]++;
        }
      }
    }

    return histogram;
  }


  /**
   *  Map the specified sub-list of time-of-flight events to a list
   *  of "d-spacing" "ghost" histograms.  A reference to arrays containing
   *  the ghost ids and weights must be passed in to this array as a
   *  parameter.  These arrays can be read from a file, using the method:
   *  FileUtil.LoadGhostMapFile()
   *
   *  @param event_list    List of (tof,id) specifying detected neutrons.
   *
   *  @param first         The index of the first event to map to d
   *
   *  @param num_to_map    The number of events to map to d
   *
   *  @param binner        The IEventBinner object that defines the bin
   *                       boundaries for the histogram bins
   *
   *  @param d_map         List of diffractometer constants.  If this is 
   *                       null, the instrument geometry will determine the
   *                       mapping from time-of-flight to d.
   *                     
   *  @param ghost_ids     Two dimensional array of DAS ids.  The kth row
   *                       specifies the list of DAS ids affected by an
   *                       event in row k.
   *
   *  @param ghost_weights Two dimensional array of doubles.  The kth row
   *                       specifies the list of fractional weights added
   *                       to the affected bins of the ghost histograms.
   *
   *  @return A two dimensional array of floats.  The kth row of this 
   *          array contains the histogram values for detector bank k.
   *          If detector bank k does not exist, that row will be null.
   */
  public float[][] Make_d_Histograms( ITofEventList event_list,
                                      long          first,
                                      long          num_to_map,
                                      IEventBinner  binner,
                                      double[]      d_map,
                                      int[][]       ghost_ids,
                                      double[][]    ghost_weights )
  {
    int num_mapped = CheckAndGetNumToMap( event_list, first, num_to_map );

    boolean  use_d_map = true;
    if ( d_map == null || d_map.length < tof_to_MagQ.length )
    {
      use_d_map = false;
      d_map = null;                           // just in case someone passed
    }                                         // in an invalid d_map
    float    tof_chan;
    int      event_id;                        // the DAS id of actual event
    int      num_ghosts = ghost_ids[0].length;
    int      id;                              // the DAS id of "ghost" events
    int[]    cur_ids;                         // current array of "ghost" ids
    double[] cur_ws;                          // current array of "weights"
    double   two_pi = Math.PI * 2;
    double   d_value;

    int      ev_index = 0;                    // index into event array
    int      index;                           // index into histogram bin
    int      num_bins = binner.numBins();
    int      grid_id;

    int[]      my_events = event_list.rawEvents( first, num_mapped );
    double[][] histogram = getEmptyDoubleHistogram( binner );

    for ( int i = 0; i < num_mapped; i++ )
    {
      tof_chan = my_events[ ev_index++ ] + t0;
      event_id = my_events[ ev_index++ ];

      cur_ids = ghost_ids[ event_id ];       // just point to current DAS id
      cur_ws  = ghost_weights[ event_id ];   // info to simplify array indexing

      for ( int ghost_num = 0; ghost_num < num_ghosts; ghost_num++ )
      {
        id = cur_ids[ ghost_num ];
        if ( id >= 0 && id < tof_to_MagQ.length )
        {
          if ( use_d_map )
            d_value = tof_chan * d_map[ id ];
          else
            d_value = two_pi * tof_chan / tof_to_MagQ[id];

          index = binner.index( d_value );

          if ( index >= 0 && index < num_bins )
          {
            grid_id = bank_num[ id ];
            if ( histogram[ grid_id ] != null )
              histogram[ grid_id ][ index ] += cur_ws[ ghost_num ];
          }
        }
      }
    }

    if ( binner instanceof LogEventBinner )     // higher order correction is
    {                                           // only set up for log scale
      for ( int row = 0; row < histogram.length; row++ )
        if ( histogram[row] != null )
          DoHigherOrderCorrection( row, 
                                   histogram[row], 
                                   binner, 
                                   ghost_weights,
                                   d_map );
    }
                                             // now copy histogram to float[][]
    float[][] f_histogram = getEmptyFloatHistogram( binner );
    for ( int row = 0; row < histogram.length; row++ )
      if ( histogram[row] != null )
        for ( int col = 0; col < histogram[row].length; col++ )
           f_histogram[row][col] = (float)(histogram[row][col]);

    return f_histogram;
  }


  /**
   *  Adjust ghost histogram for higher order correction terms.
   *  (This was adapted from Jason Hodge Python code.)
   */
  private void DoHigherOrderCorrection( int          grid_id, 
                                        double[]     ghosts, 
                                        IEventBinner binner,
                                        double[][]   ghost_weights,
                                        double[]     d_map )
  {
    int     first_id = 0;                 // ID of first pixel in detector
    int     last_id  = 0;
    int     left_id  = 0;                 // ID of pixel at left detector edge
    int     right_id = 0;                 // ID of pixel at right detector edge
    int     j        = 0;

    if ( all_bank_infos[ grid_id ] == null )
    {
      System.out.println( "Higher order ghost correction failed.  " +
                          "Did not find module # " + grid_id );
      return;
    }

    first_id = all_bank_infos[ grid_id ].first_NeXus_id();
    last_id  = all_bank_infos[ grid_id ].last_NeXus_id();

    left_id  = first_id + 3;               // This is only for rows of 7 LPSDs
    right_id = last_id - 3;                // arranged horizontally, like PG3

                                           // the rval for the detector is
                                           // stored in ghost table, offset
                                           // by 1249, from first ID.
    double rval = ghost_weights[ first_id + 1249 ][0];

    if ( rval == 0 )                              // NO higher order correction
    {
//    System.out.println("rval == 0 for grid ID " + grid_id );
      return;
    }

    int    n_bins = binner.numBins();
    double d_mid  = binner.centerVal( n_bins/2 );
    int    mid_id = (left_id + right_id) / 2;

    double mid_tof = 0;
    double d1      = 0;
    double d2      = 0;

    if ( d_map != null )
    {
      mid_tof = d_mid / d_map[ mid_id ];
      d1      = mid_tof * d_map[ left_id ];
      d2      = mid_tof * d_map[ right_id ];
    }
    else
    {
      mid_tof = d_mid * tof_to_MagQ[ mid_id ] / (2 * Math.PI);
      d1      = 2 * Math.PI * mid_tof / tof_to_MagQ[ left_id ];
      d2      = 2 * Math.PI * mid_tof / tof_to_MagQ[ right_id ];
    }

    int steps = Math.abs(binner.index( d2 ) - binner.index( d1 ));

//  System.out.printf("ID = %3d  left_id = %6d  right_id = %6d  steps = %6d\n",
//                     grid_id, left_id, right_id, steps );

    double zcts = 0;
    for ( int i = 0; i < ghosts.length; i++ )
      zcts += ghosts[i];

    double[] fohist = new double[ ghosts.length ];
    double[] sohist = new double[ ghosts.length ];
    double[] tohist = new double[ ghosts.length ];
                                                      // get smoothed version
    for ( int i = 0; i < ghosts.length; i++ )         // of ghost histogram
      fohist[i] = ghosts[i];

    for ( int i = 1; i < ghosts.length-1; i++ )
    {
      fohist[i] += 0.333 * ( fohist[i-1] + fohist[i+1] );
      fohist[i] /= 1.666;
    }

    double focts = 0;
    for ( int i = 0; i < ghosts.length; i++ )
      focts += fohist[i];
                                                     // make approximate ghost
                                                     // peak profile, milli
    double[] milli = milli_2( steps, rval );
                                                     // make second order hist
                                                     // by convolution
    for ( int k = steps; k < ghosts.length - steps; k++ )
      for ( int i = 0; i < 2 * steps; i++ )
        sohist[ k - steps + i ] += fohist[ k ] * milli[ i ];

                                                     // make third order hist
                                                     // by convolution again
    for ( int k = steps; k < ghosts.length - steps; k++ )
      for ( int i = 0; i < 2 * steps; i++ )
        tohist[ k - steps + i ] += sohist[ k ] * milli[ i ];
    
    double r1 = rval;
    double r2 = rval * rval;
    double r3 = rval * rval * rval;
    double r2inf = r1 / ( 1 + r1 );
    double r2g = r2inf - r1 + r2;

    for ( int i = 0; i < ghosts.length; i++ )
      ghosts[i] = fohist[i] - sohist[i] + ( r2g / r3 ) * tohist[i]; 

    for ( int i = 0; i < ghosts.length; i++ )
      if ( ghosts[i] < 0 )
        ghosts[i] = 0;
  }


  /**
   *  Calculate simple approximation to a single ghost peak, first version
   *  (This was adapted from Jason Hodge Python code.)
   */
  private double[] milli_1( int steps, double rval )
  {
                                                     // make approximate ghost
                                                     // peak profile, milli
    double[] milli = new double[ 2 * steps + 1 ];
                                                     // first make big triangle
    int bigt  = (int)(0.85 * steps);
    for ( int k = -bigt; k <= bigt; k++ )
      milli[ steps + k ] = (bigt - Math.abs(k)) / (double)(bigt * bigt);

                                                     // add in left triangle
    int    Lt   = (int)( 0.10 * steps );
    int    Loff = (int)( 0.29 * steps );
    double LtA  = 1.0 / ( Lt * Lt );
    for ( int k = -Lt; k <= Lt; k++ )
      milli[ steps + k + Loff ] += 0.20 * LtA * (Lt - Math.abs(k));

                                                     // add in right triangle
    for ( int k = -Lt; k <= Lt; k++ )
      milli[ steps + k - Loff ] += 0.20 * LtA * (Lt - Math.abs(k));

    double sum_milli = 0;
    for ( int k = 0; k < milli.length; k++ )
      sum_milli += milli[k];

    double scale = rval / sum_milli;
    for ( int k = 0; k < milli.length; k++ )         // nomalize to rval
      milli[k] *= scale;

    return milli;
  }


  /**
   *  Calculate simple approximation to a single ghost peak, second version
   *  (This was adapted from Jason Hodge Python code.)
   */
  private double[] milli_2( int steps, double rval )
  {
                                                     // make approximate ghost
                                                     // peak profile, milli
    double[] milli = new double[ 2 * steps + 1 ];
                                                     // first make big triangle
    int bigt  = (int)(0.85 * steps);
    double bigtA = 1.0 / (bigt * bigt);
    for ( int k = 0; k <= bigt; k++ )
      milli[ steps + k ] = bigtA * (bigt - Math.abs(k));

    double cdo = (int)(0.05*steps);                  // central drop out
    for ( int i = steps; i < steps + cdo; i++ )
       milli[i] = 0.0;

    int    Rgt   = (int)(0.10*steps);                // right ghost triangle
    int    Rgoff = (int)(0.29*steps);
    double RgtA  = 1.0 / (double)(Rgt * Rgt);

    for ( int k = -Rgt; k <= Rgt; k++ )
      milli[ steps + k + Rgoff ] += 0.60 * RgtA * (Rgt - Math.abs(k));

    int    Cgt   = (int)(0.05 * steps);                // close ghost triangle
    int    Cgoff = (int)(0.08 * steps);             
    double CgtA  = 1.0 / (double)(Cgt * Cgt);

    for ( int k = -Cgt; k <= Cgt; k++ )
      milli[steps + k + Cgoff] += 0.15*CgtA*(Cgt - Math.abs(k));

    for ( int k = 1; k < steps; k++ )      //  create left side by mirror image
      milli[steps-k] = milli[steps+k];

    double sum_milli = 0;
    for ( int k = 0; k < milli.length; k++ )
      sum_milli += milli[k];

    double scale = rval / sum_milli;
    for ( int k = 0; k < milli.length; k++ )         // nomalize to rval
      milli[k] *= scale;

    return milli;
  }


  /**
   *  Map the specified sub-list of time-of-flight events to a list of
   *  time-focused  histograms.
   *
   *  @param event_list  List of (tof,id) specifying detected neutrons.
   *
   *  @param first       The index of the first event to be histogrammed
   *
   *  @param num_to_map  The number of events to map to be histogrammed
   *
   *  @param binner      The IEventBinner object that defines the bin
   *                     boundaries for the histogram bins
   *
   *  @param angle_deg   The "virtual" scattering angle, two theta, 
   *                     (in degrees) to which the data should be focused
   *
   *  @param final_L_m   The final flight path length (in meters) to which
   *                     the data should be focused
   *                     
   *  @return A two dimensional array of integers.  The kth row of this 
   *          array contains the histogram values for detector bank k.
   *          If detector bank k does not exist, that row will be null.
   */
  public int[][] Make_Time_Focused_Histograms( ITofEventList event_list,
                                               long          first,
                                               long          num_to_map,
                                               IEventBinner  binner,
                                               float         angle_deg,
                                               float         final_L_m )
  {
    int num_mapped = CheckAndGetNumToMap( event_list, first, num_to_map );

    if ( final_L_m <= 0 )
      throw new IllegalArgumentException( "Final flight path must be > 0 " +
                                           final_L_m );
    float  tof_chan;
    int    id;
    float  focused_tof;
    float  scale = (float)
                   ((L1+final_L_m) * Math.sin(angle_deg * Math.PI/360) / 10);
                                              // since SNS event TOF values are
                                              // in 100 ns units, we need to
                                              // divide by 10 to get micro-secs

    int    ev_index = 0;                      // index into event array
    int    index;                             // index into histogram bin
    int    num_bins = binner.numBins();
    int    grid_id;

    int[]   my_events = event_list.rawEvents( first, num_mapped );
    int[][] histogram = getEmptyIntHistogram( binner );

    for ( int i = 0; i < num_mapped; i++ )
    {
      tof_chan = my_events[ ev_index++ ] + t0;
      id       = my_events[ ev_index++ ];

      if ( id >= 0 && id < recipLaSinTa.length )
      {
        focused_tof = tof_chan * scale * recipLaSinTa[id];
        index       = binner.index( focused_tof );
        if ( index >= 0 && index < num_bins )
        {
          grid_id = bank_num[ id ];
          if ( histogram[ grid_id ] != null )
            histogram[ grid_id ][ index ]++;
        }
      }
    }

    return histogram;
  }


  /**
   *  Map the specified sub-list of time-of-flight events to a list of
   *  time-focused "ghost" histograms.  A reference to arrays containing
   *  the ghost ids and weights must be passed in to this array as a 
   *  parameter.  These arrays can be read from a file, using the method:
   *  FileUtil.LoadGhostMapFile()
   *
   *  @param event_list    List of (tof,id) specifying detected neutrons.
   *
   *  @param first         The index of the first event to be histogrammed
   *
   *  @param num_to_map    The number of events to map to be histogrammed
   *
   *  @param binner        The IEventBinner object that defines the bin
   *                       boundaries for the histogram bins
   *
   *  @param angle_deg     The "virtual" scattering angle, two theta, 
   *                       (in degrees) to which the data should be focused
   *
   *  @param final_L_m     The final flight path length (in meters) to which
   *                       the data should be focused
   *
   *  @param ghost_ids     Two dimensional array of DAS ids.  The kth row
   *                       specifies the list of DAS ids affected by an
   *                       event in row k.
   *                     
   *  @param ghost_weights Two dimensional array of doubles.  The kth row
   *                       specifies the list of fractional weights added
   *                       to the affected bins of the ghost histograms.
   *                     
   *  @return A two dimensional array of floats.  The kth row of this 
   *          array contains the histogram values for detector bank k.
   *          If detector bank k does not exist, that row will be null.
   */
  public float[][] Make_Time_Focused_Histograms( ITofEventList event_list,
                                                 long          first,
                                                 long          num_to_map,
                                                 IEventBinner  binner,
                                                 float         angle_deg,
                                                 float         final_L_m,
                                                 int[][]       ghost_ids,
                                                 double[][]    ghost_weights )
  {
    int num_mapped = CheckAndGetNumToMap( event_list, first, num_to_map );

    if ( final_L_m <= 0 )
      throw new IllegalArgumentException( "Final flight path must be > 0 " +
                                           final_L_m );
    float    tof_chan;
    int      event_id;                        // the DAS id of actual event
    int      num_ghosts = ghost_ids[0].length;
    int      id;                              // the DAS id of "ghost" events
    int[]    cur_ids;                         // current array of "ghost" ids
    double[] cur_ws;                          // current array of "weights"
    float    focused_tof;
    float    scale = (float)
                   ((L1+final_L_m) * Math.sin(angle_deg * Math.PI/360) / 10);
                                              // since SNS event TOF values are
                                              // in 100 ns units, we need to
                                              // divide by 10 to get micro-secs

    int    ev_index = 0;                      // index into my event array
    int    index;                             // index into histogram bin
    int    num_bins = binner.numBins();
    int    grid_id;

    int[]      my_events = event_list.rawEvents( first, num_mapped );
    double[][] histogram = getEmptyDoubleHistogram( binner );

    for ( int i = 0; i < num_mapped; i++ )
    {
      tof_chan = my_events[ ev_index++ ] + t0;
      event_id = my_events[ ev_index++ ];

      cur_ids = ghost_ids[ event_id ];       // just point to current DAS id
      cur_ws  = ghost_weights[ event_id ];   // info to simplify array indexing

      for ( int ghost_num = 0; ghost_num < num_ghosts; ghost_num++ )
      {
        id = cur_ids[ ghost_num ];
        if ( id > 0 && id < recipLaSinTa.length )
        {
          focused_tof = tof_chan * scale * recipLaSinTa[id];
          index       = binner.index( focused_tof );
          if ( index >= 0 && index < num_bins )
          {
            grid_id = bank_num[ id ];
            if ( histogram[ grid_id ] != null )
              histogram[ grid_id ][ index ] += cur_ws[ ghost_num ];
          }
        }
      }
    }
                                             // now copy histogram to float[][]
    float[][] f_histogram = getEmptyFloatHistogram( binner );
    for ( int row = 0; row < histogram.length; row++ )
      if ( histogram[row] != null )
        for ( int col = 0; col < histogram[row].length; col++ )
           f_histogram[row][col] = (float)(histogram[row][col]);

    return f_histogram;
  }


  /**
   *  Map the specified sub-list of time-of-flight events to a 
   *  list of |Q| histograms, one histogram for each bank.
   *
   *  @param event_list  List of (tof,id) specifying detected neutrons.
   *
   *  @param first       The index of the first event to map to |Q|
   *
   *  @param num_to_map  The number of events to map to |Q| 
   *
   *  @param binner      The IEventBinner object that defines the bin
   *                     boundaries for the histogram bins
   *                     
   *  @return A two dimensional array of integers.  The kth row of this 
   *          array contains the histogram values for detector bank k.
   *          If detector bank k does not exist, that row will be null.
   */
  public int[][] Make_q_Histograms( ITofEventList event_list,
                                    long          first,
                                    long          num_to_map,
                                    IEventBinner  binner  )
  {
    int num_mapped = CheckAndGetNumToMap( event_list, first, num_to_map );

    float  tof_chan;
    int    id;
    double q_value;

    int ev_index = 0;                         // index into event array
    int    index;                             // index into histogram bin
    int    num_bins = binner.numBins();
    int    grid_id;

    int[]   my_events = event_list.rawEvents( first, num_mapped );
    int[][] histogram = getEmptyIntHistogram( binner );

    for ( int i = 0; i < num_mapped; i++ )
    {
      tof_chan = my_events[ ev_index++ ] + t0;
      id       = my_events[ ev_index++ ];

      if ( id >= 0 && id < tof_to_MagQ.length && tof_chan > 0 )
      {
        q_value = tof_to_MagQ[id] / tof_chan;
        index   = binner.index( q_value );
        if ( index >= 0 && index < num_bins )
        {
          grid_id = bank_num[ id ];
          if ( histogram[ grid_id ] != null )
            histogram[ grid_id ][ index ]++;
        }
      }
    }

    return histogram;
  }


  /**
   *  Map the specified sub-list of time-of-flight events to a 
   *  list of wavelength histograms, one histogram for each bank.
   *
   *  @param event_list  List of (tof,id) specifying detected neutrons.
   *
   *  @param first       The index of the first event to map to wavelength 
   *
   *  @param num_to_map  The number of events to map to wavelength 
   *
   *  @param binner      The IEventBinner object that defines the bin
   *                     boundaries for the histogram bins
   *                     
   *  @return A two dimensional array of integers.  The kth row of this 
   *          array contains the histogram values for detector bank k.
   *          If detector bank k does not exist, that row will be null.
   */
  public int[][] Make_wl_Histograms( ITofEventList event_list,
                                     long          first,
                                     long          num_to_map,
                                     IEventBinner  binner  )
  {
    int num_mapped = CheckAndGetNumToMap( event_list, first, num_to_map );

    float  tof_chan;
    int    id;
    double wl_value;

    int    ev_index = 0;                      // index into event array
    int    index;                             // index into histogram bin
    int    num_bins = binner.numBins();
    int    grid_id;

    int[]   my_events = event_list.rawEvents( first, num_mapped );
    int[][] histogram = getEmptyIntHistogram( binner );

    for ( int i = 0; i < num_mapped; i++ )
    {
      tof_chan = my_events[ ev_index++ ] + t0;
      id       = my_events[ ev_index++ ];

      if ( id >= 0 && id < tof_to_lamda.length )
      {
        wl_value = tof_to_lamda[id] * tof_chan / 10.0f;
        index    = binner.index( wl_value );
        if ( index >= 0 && index < num_bins )
        {
          grid_id = bank_num[ id ];
          if ( histogram[ grid_id ] != null )
            histogram[ grid_id ][ index ]++;
        }
      }
    }

    return histogram;
  }


  /**
   *  Map the specified sub-list of time-of-flight events to a 
   *  list of RAW time-of-flight histograms, one histogram for each bank.
   *  NOTE: The times-of-flight are NOT focused in this method.  The 
   *        returned histograms will be essentially like the histograms
   *        returned by Make_ws_Histograms, except the x-axis units will
   *        be time-of-flight in microseconds, rather than wavelength in
   *        Angstroms.
   *
   *  @param event_list  List of (tof,id) specifying detected neutrons.
   *
   *  @param first       The index of the first event to map to RAW time-
   *                     of-flight. 
   *
   *  @param num_to_map  The number of events to map to RAW time-of-flight 
   *
   *  @param binner      The IEventBinner object that defines the bin
   *                     boundaries for the histogram bins
   *                     
   *  @return A two dimensional array of integers.  The kth row of this 
   *          array contains the histogram values for detector bank k.
   *          If detector bank k does not exist, that row will be null.
   */
  public int[][] Make_RAW_TOF_Histograms( ITofEventList event_list,
                                          long          first,
                                          long          num_to_map,
                                          IEventBinner  binner  )
  {
    int num_mapped = CheckAndGetNumToMap( event_list, first, num_to_map );

    float  tof_chan;
    float  tof_value;
    int    id;

    int    ev_index = 0;                      // index into event array
    int    index;                             // index into histogram bin
    int    num_bins = binner.numBins();
    int    grid_id;

    int[]   my_events = event_list.rawEvents( first, num_mapped );
    int[][] histogram = getEmptyIntHistogram( binner );

    for ( int i = 0; i < num_mapped; i++ )
    {
      tof_chan = my_events[ ev_index++ ] + t0;
      id       = my_events[ ev_index++ ];

      if ( id >= 0 )
      {
        tof_value = tof_chan / 10.0f;
        index     = binner.index( tof_value );
        if ( index >= 0 && index < num_bins )
        {
          grid_id = bank_num[ id ];
          if ( histogram[ grid_id ] != null )
            histogram[ grid_id ][ index ]++;
        }
      }
    }

    return histogram;
  }


  /**
   *  Map the specified sub-list of time-of-flight events to a list of
   *  time-focused  histograms, one histogram for each bank, focused to
   *  the center of the bank.
   *
   *  @param event_list  List of (tof,id) specifying detected neutrons.
   *
   *  @param first       The index of the first event to be histogrammed
   *
   *  @param num_to_map  The number of events to map to be histogrammed
   *
   *  @param binner      The IEventBinner object that defines the bin
   *                     boundaries for the histogram bins
   *
   *  @return A two dimensional array of integers.  The kth row of this 
   *          array contains the histogram values for detector bank k.
   *          If detector bank k does not exist, that row will be null.
   */
  public int[][] Make_Time_Focused_Histograms( ITofEventList event_list,
                                               long          first,
                                               long          num_to_map,
                                               IEventBinner  binner     )
  {
    int num_mapped = CheckAndGetNumToMap( event_list, first, num_to_map );

    float   tof_chan;
    int     id;
    float   focused_tof;
    float   scale;
    float[] scale_factors = new float[ all_grids.length ];

    for ( int gid = 0; gid < all_grids.length; gid++ )
    {
      if ( all_grids[gid] == null )
        scale_factors[gid] = 0;
      else
      {
        Vector3D pos = all_grids[gid].position();
        float final_L_m = pos.length();
        float two_theta_rad = (float)( Math.acos( pos.getX() / final_L_m ) );
        float theta_rad = two_theta_rad / 2;
        scale_factors[gid] = (float)((L1+final_L_m) * Math.sin(theta_rad) / 10);
                                              // since SNS event TOF values are
                                              // in 100 ns units, we need to
                                              // divide by 10 to get micro-secs
      } 
    }

    int    ev_index = 0;                      // index into event array
    int    index;                             // index into histogram bin
    int    num_bins = binner.numBins();
    int    grid_id;

    int[]   my_events = event_list.rawEvents( first, num_mapped );
    int[][] histogram = getEmptyIntHistogram( binner );

    for ( int i = 0; i < num_mapped; i++ )
    {
      tof_chan = my_events[ ev_index++ ] + t0;
      id       = my_events[ ev_index++ ];

      if ( id >= 0 && id < recipLaSinTa.length )
      {
        grid_id = bank_num[ id ];
        if ( histogram[ grid_id ] != null )
        {
          scale   = scale_factors[ grid_id ];
          if ( scale > 0 )
          {
            focused_tof = tof_chan * scale * recipLaSinTa[id];
            index       = binner.index( focused_tof );
            if ( index >= 0 && index < num_bins )
              histogram[ grid_id ][ index ]++;
          }
        }
      }
    }

    return histogram;
  }


  /**
   *  Map a specified qx,qy,qz back to a detectors row, col, tof and ID, if
   *  possible.  NOTE: Currently this does not use the sample orientation
   *  information, but assumes that the sample orientation angles are all 
   *  zero.  
   * 
   *  @param   qx  The x component of the q vector
   *  @param   qy  The y component of the q vector
   *  @param   qz  The z component of the q vector
   *
   *  @return an array of four floats containing the fractional row, column, 
   *          time-of-flight and detector ID correspondingg the specified
   *          Q vector.  If the Q vector does not match any pixel on any
   *          of the detectors handled by this object, NULL is returned.
   */
  public float[] QtoRowColTOF_ID( float qx, float qy, float qz )
  {
    Vector3D q_vec = new Vector3D( qx, qy, qz );

    q_vec.multiply( (float)(2*Math.PI)  );

    for ( int k = 0; k < inverse_mapper.length; k++ )
    {
      float[] recalc = inverse_mapper[k].QtoRowColTOF( q_vec );
      if ( recalc != null )
      {
//      recalc[2] = recalc[2] - t0/10;      // switch from "theoretical" tof to
                                            // measured tof, in microseconds?
        float[] result = { recalc[0], recalc[1], recalc[2], grid_arr[k].ID() };
        return result;
      }
    }
    return null;
  }


  /**
   *  Construct a Peak_new object from specified qx,qy,qz components if
   *  possible.  If the specified qx,qy,qz values do not map to one of
   *  the detectors held by this object, then null is returned.
   *
   *  NOTES:
   *
   *  1. Currently this does not use the sample orientation
   *     information, but assumes that the sample orientation 
   *     angles are all zero.  
   *  2. The h,k,l, seqnum, ipkobs and Facility fields are NOT set
   *     by this method.
   *  3. Code using this should check that the result is not null     
   * 
   *  @param   qx        The x component of the q vector
   *  @param   qy        The y component of the q vector
   *  @param   qz        The z component of the q vector
   *  @param   run_info  Array of floats containing the run_number,
   *                     phi, chi and omega, in that order.
   *
   *  @return A Peak_new object corresponding to the specified qx,qy,qz,
   *          or null if the specified qx,qy,qz don't map back to any
   *          detector.
   */
  public Peak_new GetPeak( float qx, float qy, float qz, float[] run_info )
  {
    float[] row_col_tof_ID = QtoRowColTOF_ID( qx, qy, qz );
    
    if ( row_col_tof_ID == null )
      return null;

    int det_id = (int)row_col_tof_ID[3];

    IDataGrid grid = getIDataGrid( det_id );
     
    int run_number = 0;
    SampleOrientation sample_orientation = new SNS_SampleOrientation(0,0,0);

    if ( run_info != null && run_info.length == 4 )
    {
      run_number = (int)run_info[0];
      sample_orientation = new SNS_SampleOrientation( run_info[1],
                                                      run_info[2],
                                                      run_info[2] );
    }

    Peak_new peak = new Peak_new( run_number,
                                  monitor_count,
                                  row_col_tof_ID[1],
                                  row_col_tof_ID[0],
                                  row_col_tof_ID[2]/10, // use tof to make a
                                                        // psuedo-channel num
                                                        // since the events 
                                                        // aren't histogrammed
                                  grid,
                                  sample_orientation,
                                  row_col_tof_ID[2],
                                  L1,
                                  t0 / 10  );             // Convert t0 shift 
                                                          // to microseconds 
    peak.setInstrument( instrument_name );
    
    return peak;
  }


  /**
   * Check that the specified event list and range of events is valid,
   * and throw an IllegalArgumentException if the event list or range is not
   * valid.  The number of events to process is returned as the value of this 
   * method.  The returned number of events will fit in one array if
   * no exception is thrown.
   */
  private int CheckAndGetNumToMap( ITofEventList event_list,
                                   long          first,
                                   long          num_to_map )
  {
     if ( event_list == null )
       throw new IllegalArgumentException( "event_list is null" );

     long num_events = event_list.numEntries();

     if ( first < 0 || first >= num_events )
       throw new IllegalArgumentException("First index: " + first +
                 " < 0 or >= number of events in list: " + num_events );

     long last = first + num_to_map - 1;
     if ( last >= num_events )
       last = num_events - 1;

     num_to_map = last - first + 1;
     if ( num_to_map > ITofEventList.MAX_LIST_SIZE )
       throw new IllegalArgumentException( "TOO many events in sublist. " +
             num_to_map + " exceeds " + ITofEventList.MAX_LIST_SIZE );
/*
     System.out.println("Maximum number of events = " +
                                                 ITofEventList.MAX_LIST_SIZE );
     System.out.println("Number of events in list = " + num_events );
     System.out.println("Number of events to map  = " + num_to_map );
     System.out.println("First event number       = " + first );
*/
     return (int)num_to_map;
  }


  /**
   *  Get a two dimensional "ragged array" to hold an integer valued
   *  histogram.  Row k will be an array of ints to hold the histogram 
   *  for bank ID k.  If bank k is empty, row k is null.
   *
   *  @param binner the binner that determines the histogram size and 
   *                bin boundaries.
   *
   *  @return emtpy two-dimensional array of ints, with null rows for
   *          for rows corresponding to missing detector banks.
   */
  private int[][] getEmptyIntHistogram( IEventBinner binner )
  {
    int[][] result = new int[ max_grid_ID + 1 ][];

    for ( int i = 0; i < result.length; i++ )
      result[i] = null;

    int num_bins = binner.numBins();
    for ( int  i = 0; i < grid_arr.length; i++ )
    {
      int grid_ID = grid_arr[i].ID();
      result[ grid_ID ] = new int[ num_bins ];
    }

    return result;
  }


  /**
   *  Get a two dimensional "ragged array" to hold a float valued
   *  histogram.  Row k will be an array of floats to hold the histogram 
   *  for bank ID k.  If bank k is empty, row k is null.
   *
   *  @param binner the binner that determines the histogram size and 
   *                bin boundaries.
   *
   *  @return emtpy two-dimensional array of floats, with null rows for
   *          for rows corresponding to missing detector banks.
   */
  private float[][] getEmptyFloatHistogram( IEventBinner binner )
  {
    float[][] result = new float[ max_grid_ID + 1 ][];

    for ( int i = 0; i < result.length; i++ )
      result[i] = null;

    int num_bins = binner.numBins();
    for ( int  i = 0; i < grid_arr.length; i++ )
    {
      int grid_ID = grid_arr[i].ID();
      result[ grid_ID ] = new float[ num_bins ];
    }

    return result;
  }


  /**
   *  Get a two dimensional "ragged array" to hold a double valued
   *  histogram.  Row k will be an array of doubles to hold the histogram 
   *  for bank ID k.  If bank k is empty, row k is null.
   *
   *  @param binner the binner that determines the histogram size and 
   *                bin boundaries.
   *
   *  @return emtpy two-dimensional array of doubles, with null rows for
   *          for rows corresponding to missing detector banks.
   */
  private double[][] getEmptyDoubleHistogram( IEventBinner binner )
  {
    double[][] result = new double[ max_grid_ID + 1 ][];

    for ( int i = 0; i < result.length; i++ )
      result[i] = null;

    int num_bins = binner.numBins();
    for ( int  i = 0; i < grid_arr.length; i++ )
    {
      int grid_ID = grid_arr[i].ID();
      result[ grid_ID ] = new double[ num_bins ];
    }

    return result;
  }


  /**
   *  Build the list of weights corresponding to different wavelengths.
   *  Although the spectrum file need not have a fixed number of
   *  points, it MUST have the spectrum recorded as a histogram with one
   *  more bin boundary than the number of bins.
   *    The entries in the table produced are:
   *
   *     1/( lamda^power * spec(lamda) )
   *
   *  Where power was chosen to give a relatively uniform intensity display
   *  in 3D.  The power is currently 3 if an incident spectrum is present
   *  and 2.4 if no incident spectrum is used.
   */
  private void BuildLamdaWeights( String spectrum_file_name )
  {
                                             // Theoretically correct value 3.0;
                                             // if we have an incident spectrum
//  float power_ns = 2.4f;                   // lower power needed to find
                                             // peaks in ARCS data with no
                                             // incident spectrum
    float   lamda; 

    boolean use_incident_spectrum = true;
    float power = power_th;
  
    lamda_weight = GetSpectrumWeights( spectrum_file_name );

    if ( lamda_weight == null )              // loading spectrum failed so use
    {                                        // array of 1's
      use_incident_spectrum = false;
//    power = power_ns;                      // This is commented out, so we
                                             // don't override user specified 
                                             // value.
      lamda_weight = new float[ NUM_WAVELENGTHS ];
      for ( int i = 0; i < lamda_weight.length; i++ )
        lamda_weight[i] = 1;
    }
    
    for ( int i = 0; i < lamda_weight.length; i++ )
    {
      lamda = i / STEPS_PER_ANGSTROM;
      lamda_weight[i] *= (float)(1/Math.pow(lamda,power));
    }

    if ( use_incident_spectrum )
      System.out.println("Built weights using incident spectrum from " +
                          spectrum_file_name + " with wl power " + power );
    else
      System.out.println("Built APPROXIMATE weights with wl power " + power );
  }


  /**
   *  Build the list of weights corresponding to different wavelengths,
   *  based solely on the incident spectrum.  The power of lamda is added
   *  by the BuildLamdaWeights() method.
   *  The spectrum values are first normalized so that the MAXIMUM value is
   *  1 and the minimum value is 0.1.
   *
   *  @return the array of weights for the incident spectrum, if the the
   *          spectrum file could be loaded, or null, if the spectrum
   *          file could not be loaded.
   */
  private float[] GetSpectrumWeights( String spectrum_file_name )
  {
    float MIN_SPECTRUM_VALUE = 0.1f;

    try
    {
      FileUtil.CheckFile( spectrum_file_name );
    }
    catch ( Exception ex )
    {
      return null;
    }

    float[] spec_val   = null;
    float[] spec_lamda = null;
                                                      // now load the file
    int num_bins = 0;
    try
    {
      FileReader     f_in        = new FileReader( spectrum_file_name );
      BufferedReader buff_reader = new BufferedReader( f_in );
      Scanner        sc          = new Scanner( buff_reader );

      for ( int i = 0; i < 6; i++ )                   // skip info lines
        sc.nextLine();

      String num_y_line = sc.nextLine().trim();

      int blank_index = num_y_line.lastIndexOf(" ");

      num_y_line = num_y_line.substring(blank_index);
      num_y_line = num_y_line.trim();

      Integer NUM_BINS = new Integer( num_y_line );
      num_bins = NUM_BINS;
      if ( num_bins <= 0 )
      {
        System.out.println("NEGATIVE NUMBER OF BINS IN " + spectrum_file_name);
        return null;
      }

      spec_lamda = new float[ num_bins+1 ];
      spec_val   = new float[ num_bins ];
      for ( int i = 0; i < num_bins; i++ )
      {
        spec_lamda[i] = sc.nextFloat();
        spec_val[i]   = (float)sc.nextFloat();
      }
    }
    catch ( Exception ex )
    {
      System.out.println("FAILED TO LOAD SPECTRUM: " + spectrum_file_name );
      System.out.println("EXCEPTION = " + ex + "\n" +
                         "NOT WEIGHTING BY INCIDENT SPECTRUM!" );
      return null;
    }
                                                      // normalize spectrum
    float max = spec_val[0];
    for ( int i = 0; i < num_bins; i++ )
      if ( spec_val[i] > max )
        max = spec_val[i];

    if ( max <= 0 )
    {
      System.out.println("ERROR: Spectrum file had no positive entries");
      return null;
    }
                                                  // set so max value == 1
    for ( int i = 0; i < num_bins; i++ )
      spec_val[i] /= max;

    for ( int i = 0; i < num_bins; i++ )          // clamp to be > 0
      if ( spec_val[i] < MIN_SPECTRUM_VALUE )
        spec_val[i] = MIN_SPECTRUM_VALUE;
                                                  // now adjust weights based
                                                  // on incident spectru,
    int   index;
    float val;
    float lamda;
    float lamda_min  = spec_lamda[0];
    float lamda_max  = spec_lamda[num_bins - 2];
    float spec_first = spec_val[0];
    float spec_last  = spec_val[num_bins - 1];
    
    lamda_weight = new float[ NUM_WAVELENGTHS ];
    for ( int i = 0; i < lamda_weight.length; i++ )
    {
      lamda = i / STEPS_PER_ANGSTROM;
      if ( lamda <= lamda_min )
        val = spec_first;
      else if ( lamda >= lamda_max )
        val = spec_last;
      else
      {
        index = Arrays.binarySearch( spec_lamda, lamda );
        if ( index < 0 )
          index = -index - 1;
        if ( index > num_bins - 1 )
          index = num_bins - 1;
        val = spec_val[index];
      }
      lamda_weight[i] = 1 / val;
    }

    return lamda_weight;
  }


 /**
  *       subroutine to calculate a spherical absorption correction
  *       and tbar. based on values in:
  *
  *       c. w. dwiggins, jr., acta cryst. a31, 395 (1975).
  *
  *       in this paper, a is the transmission and a* = 1/a is
  *       the absorption correction.
  *
  *       input are the smu (scattering) and amu (absorption at 1.8 ang.)
  *       linear absorption coefficients, the radius r of the sample
  *       the theta angle and wavelength.
  *       the absorption (absn) and tbar are returned.
  *
  *       a. j. schultz, june, 2008
  */
  private float absor_sphere(float twoth, float wl)
  {
    int i;
    float mu, mur;         //mu is the linear absorption coefficient,
                            //r is the radius of the spherical sample.
    float theta,astar1,astar2,frac,astar;
//  float trans;
//  float tbar;

//  For each of the 19 theta values in dwiggins (theta = 0.0 to 90.0
//  in steps of 5.0 deg.), the astar values vs.mur were fit to a third
//  order polynomial in excel. these values are given in the static array
//  pc[][]

    mu = smu + (amu/1.8f)*wl;

    mur = mu*radius;

    theta = twoth*radtodeg_half;

//  using the polymial coefficients, calulate astar (= 1/transmission) at
//  theta values below and above the actual theta value.

    i = (int)(theta/5.f);
    astar1 = pc[0][i] + mur * (pc[1][i] + mur * (pc[2][i] + pc[3][i] * mur));

    i = i+1;
    astar2 = pc[0][i] + mur * (pc[1][i] + mur * (pc[2][i] + pc[3][i] * mur));

//  do a linear interpolation between theta values.

    frac = (theta%5.f)/5.f;

    astar = astar1*(1-frac) + astar2*frac;       // astar is the correction
//  trans = 1.f/astar;                           // trans is the transmission
                                                 // trans = exp(-mu*tbar)

//  calculate tbar as defined by coppens.
//  tbar = -(float)Math.log(trans)/mu;

    return astar;
  }


  /**
   *  Build the following tables, indexed by the DAS ID:
   *  tof_to_lamda[],
   *  tof_to_MagQ[],
   *  QUxyz[],
   *  recipLaSinTa[],
   *  pix_weight[],
   *  nex_to_das_id[],
   *  use_id[],
   *  bank_num[],
   *  das_to_row[],
   *  das_to_col[]
   *  all_bank_infos[],
   *
   *  This version requires the information from .DetCal, mapping and bank
   *  files for SNS instruemnts.
   */
  private void BuildMaps( int[]       das_to_nex_id, 
                          IDataGrid[] datagrid_arr,
                          BankInfo[]  bank_info ) 
  {
                            // make table of IDataGrids indexed by the grid ID 
                            // missing grids are marked with a null

                            // First find the largest possible ID by scanning
                            // through the information from the .DetCal and
                            // the Bank.xml files
    int val;
    int max_grid_id = 0;     
    for ( int i = 0; i < datagrid_arr.length; i++ )
    {
      val = datagrid_arr[i].ID();
      if ( val > max_grid_id )
        max_grid_id = val;
    }

    for ( int i = 0; i < bank_info.length; i++ )
    {
      val = bank_info[i].ID();
      if ( val > max_grid_id )
        max_grid_id = val;
    }

    max_grid_ID = max_grid_id;                // TODO: replace max_grid_id
                                              //       by max_grid_ID and
                                              //       add method to find it.

    System.out.println("**** max_grid_id = " + max_grid_ID );
    all_grids      = new IDataGrid[ max_grid_id + 1 ];
    all_bank_infos = new BankInfo [ max_grid_id + 1 ]; 
    for ( int i = 0; i < all_grids.length; i++ )
    {
      all_grids[i]      = null;
      all_bank_infos[i] = null;
    }
                                             // Get complete arrays of possible
                                             // grids and bank_infos, leaving
                                             // null for missing items
    IDataGrid grid;
    for ( int i = 0; i < datagrid_arr.length; i++ )
    {
      grid = datagrid_arr[i];
      if ( grid != null )
      {
        int id = grid.ID();
        if ( id >= 0 && id < all_grids.length )
          all_grids[ id ] = grid;
      }
    }

    for ( int k = 0; k < bank_info.length; k++ )
    {
      BankInfo info = bank_info[k];
      if ( info != null )
      {
        int id = info.ID();
        if ( id >= 0 && id < all_bank_infos.length )
          all_bank_infos[ id ] = info;
      }
    }
                                               // Now discard any grids that
                                               // aren't listed in both the
                                               // banking file and .DetCal
    for ( int i = 0; i <= max_grid_id; i++ )
    {
      if ( all_grids[i] != null && all_bank_infos[i] == null )
      {
        all_grids[i] = null;
        System.out.println("WARNING: Missing ID " + i + " from bank.xml file");
      }
      else if ( all_bank_infos[i] != null && all_grids[i] == null )
      {
        all_bank_infos[i] = null;
        System.out.println("WARNING: Missing ID " + i + " from .DetCal file");
      }
    }
                                               // make tables to map NeXus ID
                                               // to gridID, row and column
                                               // missing entries marked by -1
    int pix_count  = das_to_nex_id.length;
    int max_nex_id = 0; 
    for ( int i = 0; i < das_to_nex_id.length; i++ )
    {
      val = das_to_nex_id[i];
      if ( val > max_nex_id )
        max_nex_id = val;
    }

    int[] nex_to_gridID = new int[ max_nex_id + 1 ];
    int[] nex_to_row    = new int[ max_nex_id + 1 ];
    int[] nex_to_col    = new int[ max_nex_id + 1 ];

    Arrays.fill( nex_to_gridID, -1 );
    Arrays.fill( nex_to_row,    -1 );
    Arrays.fill( nex_to_col,    -1 );
                                              // now use bank info to fill out
                                              // nex_to_* arrays....
    int grid_id;
    int x_size,
        y_size,
        first_id,
        last_id,
        id;
    for ( int k = 0; k < bank_info.length; k++ )
    {
      int bank_id = bank_info[k].ID();
      
      if ( all_bank_infos[bank_id] != null )
      {
        grid_id  = all_bank_infos[bank_id].ID();
        x_size   = all_bank_infos[bank_id].num_cols();
        y_size   = all_bank_infos[bank_id].num_rows();
        first_id = all_bank_infos[bank_id].first_NeXus_id();
        last_id  = all_bank_infos[bank_id].last_NeXus_id();

        grid = all_grids[ grid_id ];
        if ( y_size != grid.num_rows() || x_size != grid.num_cols() )
          System.out.println("ERROR: Grid size wrong for " + grid_id + 
              ", " +  y_size + " != " + grid.num_rows() + " OR " + 
              " "  +  x_size + " != " + grid.num_cols() ); 
        else
        {
          id = first_id;
          for ( int col = 1; col <= x_size; col++ )     // NOTE: In ISAW the 
            for ( int row = 1; row <= y_size; row++ )   // IDataGrid rows and
            {                                           // columns start at 1
              nex_to_gridID[ id ] = grid_id;
              nex_to_row   [ id ] = row;
              nex_to_col   [ id ] = col;
              id++;
            } 
          if ( id - 1 != last_id )
            System.out.println("Didn't end with last id: " + (id-1) + 
                               " != " + last_id );
        }
      }  
    }
                               // now that the temporary tables are built
                               // proceed to build the maps
    two_theta_map = new float  [ pix_count ];  
    tof_to_lamda  = new float  [ pix_count ];  
    tof_to_MagQ   = new float  [ pix_count ]; 
    QUxyz         = new float  [ 3*pix_count ];
    recipLaSinTa  = new float  [ pix_count ];
    pix_weight    = new float  [ pix_count ]; 
    use_id        = new boolean[ pix_count ];
    bank_num      = new int    [ pix_count ]; 
    das_to_row    = new float  [ pix_count ];
    das_to_col    = new float  [ pix_count ];

    float     part = (float)(10 * 4 * Math.PI / tof_calc.ANGST_PER_US_PER_M);
                                                   // partial constant
    double    two_theta;
    float     sin_theta;
    float     L2;
    Vector3D  pix_pos;
    Vector3D  unit_qvec;
    float[]   coords;
    int       grid_ID;
    int       index;
    int       nex_i;
    int       row,
              col;

    for ( int das_i = 0; das_i < pix_count; das_i++ )
    {
      nex_i   = das_to_nex_id[ das_i ];

      grid_ID = nex_to_gridID[ nex_i ];
      row     = nex_to_row   [ nex_i ];
      col     = nex_to_col   [ nex_i ];

      if ( grid_ID >= 0 )
      {
        bank_num     [ das_i ] = grid_ID;
        das_to_row   [ das_i ] = row;
        das_to_col   [ das_i ] = col;

        grid       = all_grids[ grid_ID ];

        pix_pos    = grid.position( row, col );
        L2         = pix_pos.length();

        coords     = pix_pos.get();
        coords[0] -= L2;                        // internally using IPNS
                                                // coordinates
        unit_qvec = new Vector3D(coords);
        unit_qvec.normalize();

        index = das_i * 3;
        QUxyz[ index     ] = unit_qvec.getX();
        QUxyz[ index + 1 ] = unit_qvec.getY();
        QUxyz[ index + 2 ] = unit_qvec.getZ();

        two_theta = Math.acos( pix_pos.getX() / L2 );
        sin_theta = (float)Math.sin(two_theta/2);

        two_theta_map [das_i] = (float)two_theta;

        tof_to_MagQ   [das_i] = part * (L1 + L2) * sin_theta;

        tof_to_lamda  [das_i] = ANGST_PER_US_PER_M /(L1 + L2);

        recipLaSinTa  [das_i] = 1/( (L1 + L2) * sin_theta );

        pix_weight    [das_i] = sin_theta * sin_theta;

        use_id        [das_i] = true;            // initially assume all pixels
                                                 // will be used.
      }
    }
                                                 // now make the reverse table
                                                 // that maps NeXus pixel IDs
                                                 // to DAS pixels IDs
    nex_to_das_id = new int[ das_to_nex_id.length ];

    for ( int i = 0; i < nex_to_das_id.length; i++ )
      nex_to_das_id[i] = -1;                     // mark unused IDs with -1

    for ( int i = 0; i < das_to_nex_id.length; i++ )
      nex_to_das_id[ das_to_nex_id[i] ] = i;     // record tranlation from 
                                                 // NeXus ID to DAS ID for the
                                                 // ones that are mapped.
  }


  /**
   *  Basic tests during develepment.
   */
  public static void main( String args[] ) throws Exception
  {
    String inst_name = "SNAP";
    String info_dir = "/home/dennis/SNS_ISAW/ISAW_ALL/InstrumentInfo/SNS/"
                      + inst_name + "/";
    String det_file  = info_dir + inst_name + ".DetCal";
//  String map_file  = info_dir + inst_name + "_TS.dat";
//  String bank_file = info_dir + inst_name + "_bank.xml";
//  String ev_file   = "/usr2/DEMO/ARCS_1250_neutron_event.dat";
//  String ev_file   = "/usr2/DEMO/SNAP_240_neutron_event.dat";
    String ev_file   = "/usr2/DEMO/SNAP_767_neutron_event.dat";

    long start = System.nanoTime();
    SNS_Tof_to_Q_map mapper = new  SNS_Tof_to_Q_map( det_file, null, inst_name);
    double time = (System.nanoTime()-start)/1e6;
    System.out.printf("Time to make mapper = %5.2f ms\n", time );

    ITofEventList loader = new SNS_TofEventList( ev_file );
    
    double  min_d      =  0.2;
    double  max_d      =  10;
    double  first_step = .0002;
    IEventBinner d_binner = new LogEventBinner( min_d, max_d, first_step );
    System.out.println("Number of bins in binner = " + d_binner.numBins() );

    start = System.nanoTime();
    int[][] histogram = mapper.Make_d_Histograms( loader, 
                                                  0, 
                                                  (int)loader.numEntries(), 
                                                  d_binner,
                                                  null );
    time = (System.nanoTime()-start)/1e6;
    System.out.printf("Time to make d histogram = %5.2f ms\n", time );

    for ( int i = 0; i < histogram.length; i++ )
      if ( histogram[i] == null )
        System.out.println( "histogram " + i + " null" );
      else
        System.out.println( "histogram " + i + " length "+histogram[i].length);

/*
    float sum;
    for ( int i = 0; i < histogram[14].length; i++ )
    {
      sum = 0;
      for ( int id = 10; id <= 18; id++ )
        sum += histogram[id][i];
      System.out.printf( "%6.5f %3.2f\n", d_binner.minVal(i), sum );
    }
*/

    float angle_deg = 90;
    float final_L_m = .4956f; 

    float min_tof   = 0;
    float max_tof   = 17000;
    int   num_tof   = 1700;
    IEventBinner tof_binner = new UniformEventBinner(min_tof,max_tof,num_tof);
    start = System.nanoTime();
    histogram = mapper.Make_Time_Focused_Histograms( loader, 
                                                      0,
                                                      (int)loader.numEntries(),
                                                      tof_binner,
                                                      angle_deg,
                                                      final_L_m   );
    time = (System.nanoTime()-start)/1e6;
    System.out.println("Number of bins in binner = " + tof_binner.numBins() );
    System.out.printf("Time to make focused_tof histogram = %5.2f ms\n", time);
/*
    for ( int i = 0; i < histogram[14].length; i++ )
      System.out.printf("%6.5f %3.2f\n", tof_binner.minVal(i), 
                                         (float)histogram[14][i]);
*/

   start = System.nanoTime();
   mapper = new SNS_Tof_to_Q_map( "SNAP", null, null, null, null );
   time = (System.nanoTime()-start)/1e6;
   System.out.printf("Time to make new mapper = %5.2f ms\n", time);

   System.out.println("L1 = " + mapper.getL1() );
   System.out.println("t0 = " + mapper.getT0() );
  }

} 

/*
 * File:  RunfileRetriever.java
 *
 * Copyright (C) 1999-2001, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.76  2003/08/05 14:44:51  dennis
 *  Added temporary fix for SAND area detector positions.  The
 *  effective positions of the pixels are now set from the area
 *  detector DataGrid, not from the IPNS Runfile segment positions.
 *
 *  Revision 1.75  2003/07/21 22:32:54  dennis
 *  Added temporary fix to put M1 monitor upstream, for SAD instruments.
 *
 *  Revision 1.74  2003/07/01 22:21:03  dennis
 *  Fixed problem with VERY slow reading of GLAD data by replacing
 *  call to setData_entries( ds ) for each DataGrid with a single
 *  call to UniformGrid.setDataEntriesInAllGrids( ds ).  The original
 *  code was O(N*N) while the new version is O(N).  Since N is about
 *  20000 for GLAD, the old version was not practical.
 *
 *  Revision 1.73  2003/05/24 21:12:07  dennis
 *  Now adds list of references to Data blocks to the UniformGrid object
 *  for each detector.
 *
 *  Revision 1.72  2003/04/14 14:34:47  dennis
 *  Now gets the size of detectors from the IPNS.Runfile.Runfile methods:
 *  DetectorWidth(id), DetectorLength(id) and DetectorDepth(id)
 *
 *  Revision 1.71  2003/03/20 22:48:13  dennis
 *  Calculate OMEGA using raw angle instead of focused angle.
 *
 *  Revision 1.70  2003/03/17 23:25:58  dennis
 *  Fixed bug introduced with new DataGrid concept.  Now (again)
 *  properly interprets Runfile.RawFlightPath() as sample to
 *  detector distance.
 *
 *  Revision 1.69  2003/03/11 21:51:16  dennis
 *  Supports both rotations by DetRot1() (tilts detector towards the sample)
 *  and DetRot2() (rotates detector about normal to detector surface)
 *
 *  Revision 1.68  2003/03/10 22:20:41  dennis
 *  Now supports some non-vertical orientation of LPSDs.  Specifically,
 *  the rotation specified by DetRot2 in IPNS runfiles is now handled.
 *
 *  Revision 1.67  2003/03/10 20:57:48  dennis
 *  Calculation of solid angle now uses segment length and width values
 *  from the IPNS package, rather than the whole detector length and
 *  width values.
 *
 *  Revision 1.66  2003/02/18 20:25:24  dennis
 *  Switched to add one SampleOrientation attribute instead of separate
 *  phi, chi and omega values.
 *
 *  Revision 1.65  2003/02/17 22:24:12  pfpeterson
 *  Updated deprecated method calls to what is now used.
 *
 *  Revision 1.64  2003/02/17 18:43:13  dennis
 *  Fixed bug (file with grouped data could not be loaded).
 *  Also, now keep list of detector data grids, not detector data grid
 *  attributes, since detector info is not stored directly as a DataSet
 *  attribute, but only referred to from pixel attributes.
 *
 *  Revision 1.63  2003/02/14 20:51:52  dennis
 *  Removed commented out SegmentInfo code.
 *  Now uses trimToSize() method on an AttributeList before
 *  adding the AttributeList to a DataSet or Data block.
 *
 *  Revision 1.62  2003/02/12 20:03:10  dennis
 *  Switched to use PixelInfoList instead of SegmentInfoList
 *
 *  Revision 1.61  2003/02/10 16:06:20  dennis
 *  Commented out debug print
 *
 *  Revision 1.60  2003/02/10 15:21:51  dennis
 *  Now adds detector grid to DataSet attributes and PixelList
 *  to Data block attributes, for SCD and SAD instruments only.
 *  Eventually, the SegmentInfoList should be removed for these
 *  instruments.
 *
 *  Revision 1.59  2003/01/15 20:54:26  dennis
 *  Changed to use SegmentInfo, SegInfoListAttribute, etc.
 *
 *  Revision 1.58  2002/12/20 20:25:51  pfpeterson
 *  Added user name to AttributeList of DataSet.
 *
 *  Revision 1.57  2002/11/27 23:23:16  pfpeterson
 *  standardized header
 *
 *  Revision 1.56  2002/11/15 00:06:27  dennis
 *  RunfileRetriever now adds shared copies of the attributes:
 *      RUN_NUM
 *      NUMBER_OF_PULSES
 *      INITIAL_PATH
 *      SAMPLE_CHI
 *      SAMPLE_PHI
 *      SAMPLE_OMEGA
 *      EFFICIENCY_FACTOR (nominal value = 1)
 *      DETECTOR_CEN_ANGLE
 *      DETECTOR_CEN_DIST
 *  to the attribute lists for the Data blocks or DataSet to conserve
 *  memory.
 *
 *  Revision 1.55  2002/10/24 15:38:16  dennis
 *  Now calculates the total count rather than calling Runfile.Get1DSum().
 *  This avoids re-reading the spectrum from the file a second time for the
 *  purpose of calculating the total counts and saves 35% of the time it takes
 *  to load an SCD file.
 *
 *  Revision 1.54  2002/10/12 22:41:25  hammonds
 *  Fix import path to get DC5.
 *
 *  Revision 1.53  2002/10/12 03:42:33  hammonds
 *  changed location for getting LENGTH&WIDTH for detector elements from 
 *  IPNS.Runfile.Runfile to IPNS.Calib.DC5.
 *
 *  Revision 1.52  2002/10/03 15:50:51  dennis
 *  Replace call to Data.setSqrtErrors() to Data.setSqrtErrors(true)
 *
 *  Revision 1.51  2002/08/05 14:35:10  hammonds
 *  Commented out code that rotates SCDs detector angle to -90 degrees.
 *
 *  Revision 1.50  2002/08/01 22:44:15  dennis
 *  Monitor operators are now added using the DataSetFactory's
 *  addMonitorOperators() method
 *
 *  Revision 1.49  2002/07/31 21:52:41  dennis
 *  Fixed bug that caused the first monitor Data block to not
 *  be loaded.
 *
 *  Revision 1.48  2002/07/31 16:38:36  dennis
 *  Corrects SCD detector position to -90 degrees.
 *  Added methods to get portions of histograms:
 *     getDataSet( int data_set_num, int ids[] )
 *     getAvailableIDs( int data_set_num )
 *
 *  Revision 1.47  2002/07/24 14:59:30  dennis
 *  TIME_FIELD_TYPE attribute no longer added
 *  OMEGA attribute only added for TOF_DIFFRACTOMETER
 *
 *  Revision 1.46  2002/07/10 20:07:42  dennis
 *  Now uses XScale.getInstance() to get a uniform XScale if possible.
 *
 *  Revision 1.45  2002/06/28 20:49:46  dennis
 *  Now adds MonitorTofToWavelength operator
 *
 *  Revision 1.44  2002/06/03 22:33:34  dennis
 *  tof_data_calc.NewEnergyInData(,) is now used to adjust the spectra
 *  for a TOF_DG_Spectrometer, to the incident energy calculated from the
 *  beam monitors.
 *
 *  Revision 1.43  2002/04/24 19:07:40  pfpeterson
 *  Updated to add the raw detector center distance and angle
 *  attributes when the instrument is of the SCD type.
 *
 *  Revision 1.42  2002/03/18 21:16:56  dennis
 *  If the calculated incident energy is invalid, just use the nominal
 *  incident energy.
 *
 *  Revision 1.41  2002/03/13 16:14:44  dennis
 *  Converted to new abstract Data class.
 *
 *  Revision 1.40  2002/02/22 20:36:25  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.39  2002/02/04 23:02:28  dennis
 *  Added attributes to SCD Data and DataSets giving the sample orientation
 *  "Sample Chi", "Sample Phi" and "Sample Omega"
 *
 *  Revision 1.38  2002/01/22 20:31:25  dennis
 *  Temporarily added code for HRMECS runs 3099 & 3100 where
 *  HRMECS was run as a diffractometer.
 *
 *  Revision 1.37  2001/12/21 17:26:47  dennis
 *  Did complete change to using detector segments instead of detector ids.
 *
 *  Revision 1.36  2001/12/14 22:13:12  dennis
 *  Refined calculations for DG Spectrometers.  It now calculates the
 *  energy from the monitor peaks in the constructor and stores the
 *  calculated value as the ENERGY_IN attribute.  It also uses the
 *  calculated value of ENERGY_IN to calculate the time of flight
 *  from the source to the sample.  The calculated and nominal
 *  source to sample TOFs are also stored as attributes.
 *
 */
package DataSetTools.retriever;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.operator.DataSet.Special.*;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;
import DataSetTools.instruments.InstrumentType;
import DataSetTools.instruments.*;
import IPNS.Runfile.*;
import IPNS.Calib.*;
import DataSetTools.math.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;
import java.util.*;
import java.io.*;

/**
 *  This class opens an IPNS Runfile and extracts DataSets corresponding to
 *  monitors and sample histograms.
 */
public class RunfileRetriever extends    Retriever 
                              implements Serializable
{
  int     num_data_sets;    // the number of distinct DataSets in this runfile
                            // monitors and detectors from different histograms
                            // are placed in separate DataSets. 
  int[]   data_set_type;    // the type of a specific DataSet number in this
                            // runfile.
  int[]   histogram;        // the histogram number corresponding to a specific
                            // DataSet number in this runfile.
  Runfile run_file;

  private int instrument_type = InstrumentType.UNKNOWN;

  private float calculated_E_in = 0;     // incident energy calculated from
                                         // the beam monitors, for direct
                                         // geometry spectrometers.  

                                         // The following attributes are shared
                                         // by all items in this runfile.
  private IntListAttribute run_num_attr      = null;
  private FloatAttribute   num_pulses_attr   = null;
  private FloatAttribute   initial_path_attr = null;
  private SampleOrientationAttribute 
                           scd_sample_orientation_attr = null;
  private FloatAttribute   nominal_eff_attr 
                            = new FloatAttribute(Attribute.EFFICIENCY_FACTOR,1);
  private Hashtable        det_cen_dist_attrs  = new Hashtable();
  private Hashtable        det_cen_angle_attrs = new Hashtable();
  private Hashtable        det_data_grids      = new Hashtable();

/**
 *  Construct a runfile retriever for a specific file.
 *
 *  @param data_source_name  The fully qualified file name for the runfile.
 */
  public RunfileRetriever(String data_source_name) 
  {
    super(data_source_name);

    int       first_id, 
              last_id;
    int       num_histograms;
    boolean   has_monitors;
    boolean   has_detectors;
    boolean   has_pulse_height;
    String    file_name     = StringUtil.setFileSeparator( data_source_name );

    file_name = FilenameUtil.fixCase( file_name );
    if ( file_name == null )
    {
      System.out.println("ERROR: file " + file_name + 
                          " not found in RunfileRetriever");
      run_file = null;
      return;
    }

    try
    {
      run_file         = new Runfile( file_name );
      run_file.LeaveOpen();
      instrument_type  = run_file.InstrumentType();
      if ( instrument_type == InstrumentType.UNKNOWN )
        instrument_type = InstrumentType.getIPNSInstrumentType( file_name );

      // patch for runs where HRMECS was run as a diffractometer
      int run_num = run_file.RunNumber();
      if ( run_num == 3099 || run_num == 3100 )
      {
        String inst_name = InstrumentType.getIPNSInstrumentName( file_name );
        if ( inst_name.equalsIgnoreCase( "HRCS" ) )
          instrument_type = IPNS.Runfile.InstrumentType.TOF_DIFFRACTOMETER;
      }

      num_histograms   = run_file.NumOfHistograms();
      data_set_type    = new int[ 3 * num_histograms ];
      histogram        = new int[ 3 * num_histograms ];
      num_data_sets    = 0;
      for ( int hist = 1; hist <= num_histograms; hist++ )
      {
        if ( run_file.IsHistogramGrouped( hist ) )
        {
          first_id = run_file.MinSubgroupID( hist );
          last_id  = run_file.MaxSubgroupID( hist );
          has_monitors     = false;
          has_detectors    = false;
          has_pulse_height = false;
          for ( int group_id = first_id; group_id < last_id; group_id++ )
          {
            if ( run_file.IsSubgroupBeamMonitor(group_id) )
              has_monitors = true;
            else if ( run_file.IsPulseHeight(group_id) )
              has_pulse_height = true;
            else
              has_detectors = true;
          }

          if ( has_monitors )
          { 
            data_set_type[ num_data_sets ] = MONITOR_DATA_SET;
            histogram[ num_data_sets ]     = hist;
            num_data_sets++;
            if ( instrument_type == InstrumentType.TOF_DG_SPECTROMETER )
              calculated_E_in = CalculateEIn();
          }
          if ( has_pulse_height )
          { 
            data_set_type[ num_data_sets ] = PULSE_HEIGHT_DATA_SET;
            histogram[ num_data_sets ]     = hist;
            num_data_sets++;
          }
          if ( has_detectors )
          {
            data_set_type[ num_data_sets ] = HISTOGRAM_DATA_SET;
            histogram[ num_data_sets ]     = hist;
            num_data_sets++;
          }

        }
      }
      run_file.Close();
    }
    catch( Exception e ) 
    {
      run_file = null;
      System.out.println("Exception in RunfileRetriever constructor");
      System.out.println("Exception is " +  e ); 
      e.printStackTrace();
    }
  }

/**
 *  Get the number of distinct DataSets contained in this runfile. 
 *  The monitors are placed into one DataSet.  Any sample histograms are 
 *  placed into separate DataSets.  
 *   
 *  @return the number of distinct DataSets in this runfile.
 */  
  public int numDataSets()
  { 
    if ( run_file == null )
      return 0;
 
    return num_data_sets;
  }

/**
 *  Get the type code of a particular DataSet in this runfile. 
 *  The type codes include:
 *
 *     Retriever.INVALID_DATA_SET
 *     Retriever.MONITOR_DATA_SET
 *     Retriever.HISTOGRAM_DATA_SET
 *     Retriever.PULSE_HEIGHT_DATA_SET
 *
 *  @param  data_set_num  The number of the DataSet in this runfile whose
 *                        type code is needed.  data_set_num must be between
 *                        0 and numDataSets()-1
 *
 *  @return the type code for the specified DataSet.
 */ 
  public int getType( int data_set_num )
  {
    if ( run_file == null )
       return INVALID_DATA_SET;

    if ( data_set_num >= 0 && data_set_num < num_data_sets )
      return data_set_type[ data_set_num ];
    else
      return INVALID_DATA_SET;
  }

/**
 *  Get the histogram number of a particular DataSet in this runfile. 
 *  
 *  @param  data_set_num  The number of the DataSet in this runfile whose
 *                        histogram number is needed.  data_set_num must be 
 *                        between 0 and numDataSets()-1
 *
 *  @return the histogram number for the specified DataSet.
 */ 
  public int getHistogramNum( int data_set_num )
  {
    if ( run_file == null )
      return INVALID_DATA_SET;

    if ( data_set_num >= 0 && data_set_num < num_data_sets )
      return histogram[ data_set_num ];
    else
      return INVALID_DATA_SET;
  }


/**
 *  Get the first DataSet in this runfile that has the specified type, 
 *  histogram, monitor or pulse height.  If no DataSet in the runfile has 
 *  the specified type this returns null.
 *
 *  @param type  The type of the DataSet to retrieve from the runfile.
 *                 Retriever.HISTOGRAM_DATA_SET 
                   Retriever.MONITOR_DATA_SET
                   Retriever.PULSE_HEIGHT_DATA_SET
 *
 *  @return  The first DataSet in the with the specified type, or null if
 *           no such DataSet exists in the runfile.
 */
 public DataSet getFirstDataSet( int type )
 {
   if ( run_file == null )
     return null;

   if ( type != Retriever.HISTOGRAM_DATA_SET  &&
        type != Retriever.MONITOR_DATA_SET    &&
        type != Retriever.PULSE_HEIGHT_DATA_SET  )
     return null;

   for ( int i = 0; i < num_data_sets; i++ )
     if ( type == getType( i ) )
       return getDataSet( i );

   return null;
 }

/**
 *  Get the specified DataSet from this runfile.
 * 
 *  @param  data_set_num  The number of the DataSet in this runfile 
 *                        that is to be read from the runfile.  data_set_num
 *                        must be between 0 and numDataSets()-1
 *
 *  @return the requested DataSet, if data_set_num specifies a valid DataSet,
 *          .
 */
  public DataSet getDataSet( int data_set_num )
  {
    if ( run_file == null )
      return null;

//    System.out.println("======= getting dataset for >>>" + data_source_name );

    if ( data_set_num < 0 || data_set_num >= num_data_sets )
      return null;

    int ids[] = getAvailableIDs( data_set_num );

    return getDataSet( data_set_num, instrument_type, ids );
  }


/**
 *  Get a DataSet from the runfile containing only the the specified group 
 *  IDs from within the specified DataSet.  
 *  NOTE: The list of group IDs must be in increasing order.
 * 
 *  @param  data_set_num  The number of the DataSet in this runfile 
 *                        that is to be read from the runfile.  data_set_num
 *                        must be between 0 and numDataSets()-1
 *
 *  @param  ids           The list of group IDs from the specified DataSet
 *                        that are to be read from the runfile and returned 
 *                        in the DataSet, in increasing order.
 *
 *  @return a DataSet containing only the specified groups, if the data_set_num
 *          and ID list specify a non-empty set of Data blocks, or null
 *          otherwise.
 */
  public DataSet getDataSet( int data_set_num, int ids[] )
  {
    if ( run_file == null )
      return null;

//    System.out.println("======= getting dataset for >>>" + data_source_name );

    if ( data_set_num < 0              || 
         data_set_num >= num_data_sets ||
         ids          == null          || 
         ids.length   == 0             )
      return null;

    return getDataSet( data_set_num, instrument_type, ids );
  }


/**
 *  Get the list of available IDs in the specified DataSet.
 *
 *  @param  data_set_num  The number of the DataSet in this runfile.
 *
 *  @return An array of IDs that are available in this DataSet, in increasing
 *          order.
 */
  public int[] getAvailableIDs( int data_set_num )
  {
    if ( data_set_num < 0 || data_set_num >= num_data_sets )
      return new int[0];

    int histogram_num = histogram[ data_set_num ];  
    int first_id = run_file.MinSubgroupID( histogram_num );
    int last_id  = run_file.MaxSubgroupID( histogram_num );

    int ids[] = new int[ last_id - first_id + 1 ];
    int id = first_id;
    int     ds_type = getType( data_set_num );
    boolean monitor_group;
    int     n_used = 0;
    for ( int i = 0; i < ids.length; i++ )
    { 
      monitor_group = run_file.IsSubgroupBeamMonitor(id);
      if (  monitor_group && ds_type == MONITOR_DATA_SET  ||
           !monitor_group && ds_type != MONITOR_DATA_SET  )
      {
        ids[n_used] = id;
        n_used++; 
      }
      id++;
    }

    int available_ids[] = new int[ n_used ];
    System.arraycopy( ids, 0, available_ids, 0, n_used );
    return available_ids;
  }


/**
 *  Calculate the incident energy from the monitor spectra for a direct
 *  geometry spectrometer.  In this case, this method is called from the
 *  constructor, after the runfile has been opened and the instrument type
 *  has been deterimined. 
 */
private float CalculateEIn()
{
  int first_id = run_file.MinSubgroupID( 1 );
  int last_id  = run_file.MaxSubgroupID( 1 );
                                                // if not enough monitors, just
                                                // return the nominal value.
  if ( last_id < 0 || first_id < 0 || last_id < first_id + 1 )               
    return (float)run_file.EnergyIn();

  int  n_monitors = 0;
  Data d[] = new Data[2];                       // use first two monitors to
                                                // calculate the energy
  try
  {
    int group_id = first_id;
    while ( group_id <= last_id && n_monitors < 2 )
    {
      if ( run_file.IsSubgroupBeamMonitor(group_id) )
      {
        float bin_boundaries[] = run_file.TimeChannelBoundaries(group_id);
        float raw_spectrum[]   = run_file.Get1DSpectrum( group_id );
        XScale x_scale = new VariableXScale( bin_boundaries );
        d[n_monitors] = Data.getInstance( x_scale, raw_spectrum, n_monitors ); 

        DetectorPosition det_position = new DetectorPosition();

        Segment group_segments[] = run_file.SegsInSubgroup( group_id );
        float seg_angle  = (float)run_file.RawDetectorAngle(group_segments[0]);
              seg_angle *= (float)(Math.PI / 180.0);
        float seg_height = (float)run_file.RawDetectorHeight(group_segments[0]);
        float seg_path   = (float)run_file.RawFlightPath( group_segments[0] );
              seg_path   = Math.abs( seg_path );

        det_position.setCylindricalCoords( seg_path, seg_angle, seg_height );

        Attribute pos_attr = new DetPosAttribute( Attribute.DETECTOR_POS, 
                                                  det_position );
        d[n_monitors].setAttribute( pos_attr );
        n_monitors++;
      }
      group_id++;
    }
  }
  catch ( IOException e )
  {
    System.out.println( "ERROR: no monitor data in runfile " + run_file );
    return (float)run_file.EnergyIn();
  }

  float e_in = tof_data_calc.EnergyFromMonitorData( d[0], d[1] );

                                                       // use values from file
                                                       // if calculated value
                                                       // is invalid
  if ( Float.isNaN(e_in) || Float.isInfinite(e_in) || e_in <= 0 ) 
  {
    System.out.println("ERROR:Invalid calculated EnergyIn in RunfileRetriever");
    return (float)run_file.EnergyIn(); 
  }
  else
    return e_in;
}


/**
 *  Get the specified DataSet from this runfile, based on the type of the
 *  data in the runfile
 *
 *  @param  data_set_num     The number of the DataSet in this runfile
 *                           that is to be read from the runfile.  data_set_num
 *                           must be between 0 and numDataSets()-1
 *
 *  @param  instrument_type  Specifies the type of instrument for this runfile.
 *                           Currently only:
 *                               InstrumentType.TOF_DIFFRACTOMETER
 *                               InstrumentType.TOF_DG_SPECTROMETER
 *                           are supported.
 *  @param  ids              The list of segment IDs from the specified DataSet
 *                           that are to be read from the runfile and returned 
 *                           in the DataSet, in increasing order.
 *
 *  @return the requested DataSet.
 */
  private DataSet getDataSet( int data_set_num, 
                              int instrument_type,
                              int ids[] )
  {
    int               num_times = 0;
    XScale            x_scale = new UniformXScale(0,1,2);
    float[]           raw_spectrum;
    int               group_id;
    Data              spectrum; 
    int               histogram_num;
    boolean           is_monitor      = false;
    boolean           is_histogram    = false;
    boolean           is_pulse_height = false;
    int               first_id, last_id;
    float[]           bin_boundaries = null;
    float             source_to_sample_tof;
    DataSet           data_set = null;
    String            title;
    String            ds_type;

    if ( run_file == null )
      return null;

    histogram_num = histogram[ data_set_num ];  

    title = InstrumentType.getBaseFileName( data_source_name );
    if ( getType( data_set_num ) == MONITOR_DATA_SET )
      {
        is_monitor = true;
        title = "M" + histogram_num + "_" + title; 
        ds_type = Attribute.MONITOR_DATA;
      }
    else if ( getType( data_set_num ) == PULSE_HEIGHT_DATA_SET )
      {
        is_pulse_height = true;
        title = "P" + histogram_num + "_" + title;
        ds_type = Attribute.PULSE_HEIGHT_DATA;
      }
    else
      {
        is_histogram = true;    
        title = "H" + histogram_num + "_" + title; 
        ds_type = Attribute.SAMPLE_DATA;
      }
 
    try
    {                                       // Construct the empty DataSet
                                            // with reasonable defaults

     DataSetFactory ds_factory = new DataSetFactory( title );
//     System.out.println("instrument_type = " + instrument_type );
     if ( is_monitor || is_pulse_height )
       data_set = ds_factory.getDataSet();  // just generic operations

     else                                   // get data_set with ops for the
                                            // current instrument
       data_set = ds_factory.getTofDataSet( instrument_type );  

                                            // Add some special operators
                                            // for monitors 
     if ( is_monitor )
       DataSetFactory.addMonitorOperators( data_set, instrument_type );

                                            // Fix the label and units for
     if ( is_pulse_height )                 // Pulse height spectra
     {
       data_set.setX_label( "P.H. Channel" );
       data_set.setX_units( "Channel" );
     }

     data_set.addLog_entry( "Loaded " + title );
     AddDataSetAttributes( data_source_name, ds_type, data_set );

     run_file.LeaveOpen();

     int last_tf_type = Integer.MAX_VALUE;  // keep track of the previous time
     int tf_type;                           // type so we only create new 
                                            // XScales when needed.  

     first_id = run_file.MinSubgroupID( histogram_num );
     last_id  = run_file.MaxSubgroupID( histogram_num );

     for ( group_id = first_id; group_id <= last_id; group_id++ )
     {
      if ( Arrays.binarySearch( ids, group_id ) >= 0 )// skip if not in the list
      {
       Segment group_segments[] = run_file.SegsInSubgroup( group_id );

       if ( group_segments.length > 0 )  // only deal with non-trivial groups
                                         // and then pick out the groups of the
                                         // correct type, in case there are 
                                         // several types in this histogram
       if ( is_monitor      &&  run_file.IsSubgroupBeamMonitor(group_id) ||
            is_pulse_height &&  run_file.IsPulseHeight(group_id)         ||
            is_histogram                               && 
             !run_file.IsSubgroupBeamMonitor(group_id) &&
             !run_file.IsPulseHeight(group_id)                            )
       {
         tf_type = run_file.TimeFieldType(group_id);
         if ( tf_type != last_tf_type )      // only get the times if it's a
                                             // new time field type
         {                               
           bin_boundaries = run_file.TimeChannelBoundaries(group_id);
           num_times      = bin_boundaries.length;
           last_tf_type   = tf_type;
 
           if ( is_pulse_height )         // change bin bounds to channel number
             for ( int chan=0; chan<num_times; chan ++ )
               bin_boundaries[chan] = chan;

           // change times to sample to detector TOF for spectrometers 
           // and groups that are NOT beam monitors 
           // if there is valid source_to_sample information available
           else if ( instrument_type == InstrumentType.TOF_DG_SPECTROMETER &&
                   !is_monitor                                           )
           {
             source_to_sample_tof = 
                        (float)run_file.SourceToSampleTime();

             if ( !Float.isInfinite(source_to_sample_tof) )
               for ( int i = 0; i < bin_boundaries.length; i++ )
                 bin_boundaries[i] -= source_to_sample_tof;
           }
           x_scale = XScale.getInstance( bin_boundaries );
         }

         if ( num_times > 1 )
         {
          raw_spectrum = run_file.Get1DSpectrum( group_id );
          if ( raw_spectrum.length >= 1 )
          {
            spectrum = Data.getInstance( x_scale, raw_spectrum, group_id );
            spectrum.setSqrtErrors( true );

            // Add the relevant attributes ----------------------------------
            AddSpectrumAttributes( run_file,
                                   instrument_type,
                                   histogram_num,
                                   group_id,
                                   group_segments,
                                   tf_type,
                                   spectrum      );

            // Now add the spectrum to the DataSet -------------------------
            data_set.addData_entry( spectrum );
          }
         }
       }
      }
    }
                           // now add references to the Data blocks to each
                           // of the Data grids used.                 
    UniformGrid.setDataEntriesInAllGrids( data_set );

    run_file.Close(); 

    } 
    catch( Exception e )
    {
      System.out.println("Exception in RunfileRetriever.getDataSet()" );
      System.out.println("Exception is " +  e );
      e.printStackTrace();
    }

    if ( instrument_type == InstrumentType.TOF_DG_SPECTROMETER &&
         ds_type         == Attribute.SAMPLE_DATA  )              //adjust e_in
      for ( int i = 0; i < data_set.getNum_entries(); i++ )
      {
        Data d = data_set.getData_entry( i );
        d = tof_data_calc.NewEnergyInData( (TabulatedData)d, calculated_E_in );
        data_set.replaceData_entry( d, i );
      } 

                                     // make sure that detector 1 is upstream
    if ( instrument_type == InstrumentType.TOF_SAD && is_monitor )
    {
      System.out.println("TEMPORARY FIX FOR SAND MONITOR 1 .....");
      Data d = data_set.getData_entry(0);
      DetectorPosition position = 
         (DetectorPosition)d.getAttributeValue(Attribute.DETECTOR_POS);
      float coords[] = position.getCartesianCoords();
      if ( coords[0] > 0 )
        coords[0] *= -1;

      position.setCartesianCoords( coords[0], coords[1], coords[2] ); 
      d.setAttribute( new DetPosAttribute( Attribute.DETECTOR_POS, position ));
    }

                                     // make effective positions match grid 
    if ( instrument_type == InstrumentType.TOF_SAD && is_histogram )
    {
      System.out.println("TEMPORARY FIX FOR SAND AREA DETECTOR .....");
      ids = Grid_util.getAreaGridIDs( data_set );
      UniformGrid grid;
      for ( int i = 0; i < ids.length; i++ )
      {
        int det_id = ids[i];
        grid = (UniformGrid)Grid_util.getAreaGrid( data_set, det_id );
        Grid_util.setEffectivePositions( data_set, det_id );
      }
    }

    return data_set;
  }


/**
 *  Add the DataSet attributes to the specified DataSet.
 *
 *  @param  file_name  The file name for this DataSet.
 *  @param  ds_type    The type of this DataSet, "MONITOR_DATA", "SAMPLE_DATA",
 *                     etc.
 *  @param  ds         The DataSet to which the attributes are added.
 */
  private void AddDataSetAttributes( String   file_name,
                                     String   ds_type,
                                     DataSet  ds        )
  {
    IntAttribute      int_attr;
    StringAttribute   str_attr;
    IntListAttribute  int_list_attr;
    AttributeList     attr_list = ds.getAttributeList();

    // Fully qualified file name...
    str_attr = new StringAttribute( Attribute.FILE_NAME, file_name );
    attr_list.setAttribute( str_attr );

    // Instrument Name ........
    str_attr = new StringAttribute( Attribute.INST_NAME, 
                         InstrumentType.getIPNSInstrumentName( file_name ) );
    attr_list.setAttribute( str_attr );

    // Instrument Type ........
    int list[] = new int[1];
    list[0] = instrument_type;
    int_list_attr = new IntListAttribute( Attribute.INST_TYPE, list );
    attr_list.setAttribute( int_list_attr );

    // DataSet Type ........
    str_attr = new StringAttribute( Attribute.DS_TYPE, ds_type );
    attr_list.setAttribute( str_attr );

    // Run Title ........
    String title = run_file.RunTitle();
    title = title.trim();
    str_attr = new StringAttribute( Attribute.RUN_TITLE, title );
    attr_list.setAttribute( str_attr );

    // Run Number ...........
    Add_RunNumber( attr_list );

    // End Date  ........
    str_attr = new StringAttribute( Attribute.END_DATE, run_file.EndDate() );
    attr_list.setAttribute( str_attr );

    // End Time  ........
    str_attr = new StringAttribute( Attribute.END_TIME, run_file.EndTime() );
    attr_list.setAttribute( str_attr );
    
    // Number of Pulses  ........
    Add_NumberOfPulses( attr_list );

    // SCD sample orientation, Sample Chi, Sample Phi, Sample Omega
    if ( instrument_type == InstrumentType.TOF_SCD )
        AddSCD_SampleOrientation( attr_list );

    // User Name
    str_attr = new StringAttribute( Attribute.USER, run_file.UserName() );
    attr_list.setAttribute( str_attr );

    attr_list.trimToSize();
    ds.setAttributeList( attr_list );
  }



/**
 *  Add the Spectrum attributes to the specified Data block.
 *
 *  @param  run_file         The Runfile we're reading from 
 *  @param  instrument_type  The file name for this DataSet
 *  @param  histogram_num    The histogram number for this group
 *  @param  group_id         The group_id for this group
 *  @param  group_segments   The list of Segments that belong to this group
 *  @param  tf_type          The time field type for this group 
 *  @param  spectrum         The Data block to which the attributes are added  
 *
 */
  private void AddSpectrumAttributes( Runfile runfile,
                                      int     instrument_type,
                                      int     histogram_num,
                                      int     group_id,
                                      Segment group_segments[], 
                                      int     tf_type,
                                      Data    spectrum )
  {
    StringAttribute   str_attr;
    FloatAttribute    float_attr;
    IntAttribute      int_attr;
    DetPosAttribute   pos_attr;
    IntListAttribute  int_list_attr;
    DetectorPosition  position = new DetectorPosition();
    float             final_path;
    float             angle;
    float             height;
    AttributeList     attr_list = spectrum.getAttributeList();

    // Run Number ...........
    Add_RunNumber( attr_list );

    // Time field type
    // int_attr = new IntAttribute( Attribute.TIME_FIELD_TYPE, tf_type );
    // attr_list.setAttribute( int_attr );

    // SCD sample orientation, Sample Chi, Sample Phi, Sample Omega
    if ( instrument_type == InstrumentType.TOF_SCD )
      AddSCD_SampleOrientation( attr_list );

    // Detector and Segment ID lists ..........
    int det_ids[] = new int[ group_segments.length ];
    int seg_ids[] = new int[ group_segments.length ];
    for ( int i = 0; i < group_segments.length; i++ )
    {
      det_ids[i] = group_segments[i].DetID();
      seg_ids[i] = group_segments[i].SegID();
    }
    int_list_attr = new IntListAttribute( Attribute.DETECTOR_IDS,
                                          det_ids );
    attr_list.setAttribute( int_list_attr );

    int_list_attr = new IntListAttribute( "Segment IDs",
                                           seg_ids );
    attr_list.setAttribute( int_list_attr );

    // Initial flight path ............
    Add_InitialPath( attr_list );

    // Initial energy ...........
    if ( instrument_type == InstrumentType.TOF_DG_SPECTROMETER )
    {
      float_attr =new FloatAttribute(Attribute.NOMINAL_ENERGY_IN,
                                     (float)run_file.EnergyIn() );
      attr_list.setAttribute( float_attr );

      float_attr =new FloatAttribute(Attribute.ENERGY_IN,
                                     (float)run_file.EnergyIn() );
      attr_list.setAttribute( float_attr );

      float_attr =new FloatAttribute(Attribute.NOMINAL_SOURCE_TO_SAMPLE_TOF,
                                     (float)run_file.SourceToSampleTime() );
      attr_list.setAttribute( float_attr );

      float_attr =new FloatAttribute(Attribute.SOURCE_TO_SAMPLE_TOF,
                                     (float)run_file.SourceToSampleTime() );
      attr_list.setAttribute( float_attr );
    }

    // Final energy ...........
    if ( instrument_type == InstrumentType.TOF_IDG_SPECTROMETER )
    {
      float_attr =new FloatAttribute(Attribute.ENERGY_OUT,
                                     (float)run_file.EnergyOut() );
      attr_list.setAttribute( float_attr );
    }

    // Detector position ..........
    //
    // For now, form weighted average of raw detector angles and heights,
    // but use the effective (not raw) flight path length, for 
    // DG_SPECTROMETERS

    float solid_angles[] = GrpSolidAngles( group_id );
    if ( instrument_type == InstrumentType.TOF_DG_SPECTROMETER )
    {
      angle      = getAverageAngle( group_segments, 
                                    histogram_num, 
                                    solid_angles,
                                    true  );   
      angle      *= (float)(Math.PI / 180.0);
  
      height     = getAverageHeight( group_segments, 
                                     histogram_num,
                                     solid_angles, 
                                     true );

      final_path = getAverageFlightPath( group_segments, 
                                         histogram_num, 
                                         solid_angles,
                                         false );
    }
    else           //  Just get the values from the runfile where possible
    {              //  don't weight by solid angles

      angle  = (float)run_file.DetectorAngle(group_segments[0], histogram_num);
      angle *= (float)(Math.PI / 180.0);

      height     = getAverageHeight( group_segments, histogram_num, false );
      final_path = getAverageFlightPath(group_segments, histogram_num, false);
    }

    // We should probably use the following to form weighted average of 
    // effective detector angles, heights and flight path lengths, for all 
    // other instruments, but for now, it doesn't work since the detector
    // type of LPSD's is set to 0 by the Runfile package.  Consequently,
    // the solid angles are all 0 and so the angle, height and final_path
    // also turn out to be 0 for such instruments. 
    // else
    // {
    //  angle      = getAverageAngle( group_segments,
    //                                histogram_num,
    //                                solid_angles,
    //                                false );
    //  angle      *= (float)(Math.PI / 180.0);
    //
    //  height     = getAverageHeight( group_segments, 
    //                                 histogram_num, 
    //                                 solid_angles, 
    //                                 false );
    //
    //  final_path = getAverageFlightPath( group_segments,
    //                                     histogram_num,
    //                                     solid_angles,
    //                                     false ); 
    // }

    float r = 0;                                      // patch for error with
    if ( final_path * final_path < height * height )  // group 294 in some files
    {
      System.out.println("ERROR: in RunfileRetriever final_path < height");
      System.out.println("       for group ID = " + group_id );
      System.out.println("       final_path   = " + final_path );
      System.out.println("       height       = " + height );
      System.out.println("       Now using r = final_path as default.");
      r = final_path;
    }
    else
      r  = (float)Math.sqrt( final_path * final_path - height * height );
    position.setCylindricalCoords( r, angle, height );

    // Show effective position
//    float sphere_coords[] = position.getSphericalCoords();
//    System.out.println("Group = " + group_id +
//                       " R = " + sphere_coords[0] +
//                       " Theta = " + sphere_coords[1]*180/3.14159265f +
//                       " Phi = " + sphere_coords[2]*180/3.14159265f );


    pos_attr = new DetPosAttribute( Attribute.DETECTOR_POS, position );
    attr_list.setAttribute( pos_attr );

    // Omega
    if ( instrument_type == InstrumentType.TOF_DIFFRACTOMETER )
    {
      float raw_angle = 0f;
      if ( group_segments.length >= 0 )
        raw_angle = (float)run_file.RawDetectorAngle( group_segments[0] );

      float omega = tof_calc.Omega( raw_angle );
      FloatAttribute omega_attr = new FloatAttribute( Attribute.OMEGA, omega );
      attr_list.setAttribute( omega_attr );
    }

    // Raw Detector Angle ...........
    float_attr = new FloatAttribute(Attribute.RAW_ANGLE,
                      (float)run_file.RawDetectorAngle( group_segments[0]) );
    attr_list.setAttribute( float_attr );

    // "Raw distance"......
    if ( instrument_type == InstrumentType.TOF_DG_SPECTROMETER &&
         group_segments.length > 0                               )
    {
      float raw_distance = 0;                // needed for TOF_DG_SPECTROMETER
      for ( int i = 0; i < group_segments.length; i++ )
        raw_distance += (float)run_file.RawFlightPath( group_segments[i] );
      float_attr = new FloatAttribute( Attribute.RAW_DISTANCE,
                                       raw_distance/group_segments.length );
      attr_list.setAttribute( float_attr );
    }

    // Delta 2 theta ( range of scattering angles covered ), assuming 1" tube
    float det_width_factor = 1.45530928f;  // detector width in meters times
                                           // 180/PI to convert from radians
                                           // to degrees.
    float delta_2theta = det_width_factor/final_path;
    float_attr =new FloatAttribute(Attribute.DELTA_2THETA, delta_2theta  );
    attr_list.setAttribute( float_attr );

    // PixelInfo ....
    if ( group_segments.length > 0 )
    {
      short      row;
      short      col;
      int        seg_id;
      int        det_id;
      IDataGrid  data_grid;
      Integer    key;
      IPixelInfo    list[] = new IPixelInfo[group_segments.length]; 
      PixelInfoList pil;
      for ( int i = 0; i < group_segments.length; i++ )
      {
        Add_DetectorDataGrid( group_segments[i].DetID() );

        det_id = group_segments[i].DetID();
        seg_id = group_segments[i].SegID();
        row = (short)group_segments[i].Row();
        col = (short)group_segments[i].Column();

        key = new Integer(det_id);
        data_grid = (IDataGrid)det_data_grids.get( key );
        list[i] = new DetectorPixelInfo( seg_id, row, col, data_grid );   
      }
      pil = new PixelInfoList(list);
      attr_list.setAttribute( 
                  new PixelInfoListAttribute(Attribute.PIXEL_INFO_LIST, pil) );
    }

    // SegmentInfo ....

    if ( group_segments.length > 0 )
      if(instrument_type==InstrumentType.TOF_SCD)
         Add_DetectorCenterPosition(attr_list,group_segments[0].DetID());

    if ( group_segments.length > 0 )           // add the SegInfoListAttribute
    {
                                               // add the crate, input & slot
                                               // info for the first segment
                                               // in the group
      int crates[] = new int[ group_segments.length ];
      int slots[]  = new int[ group_segments.length ];
      int inputs[] = new int[ group_segments.length ];
      for ( int id = 0; id < group_segments.length; id++ )
      {
        Segment seg = group_segments[id];        
        crates[id] = runfile.CrateNum(seg); 
        slots[id]  = runfile.SlotNum(seg); 
        inputs[id] = runfile.InputNum(seg); 
      }
      attr_list.setAttribute( new IntListAttribute( "Crate", crates ) );
      attr_list.setAttribute( new IntListAttribute( "Slot",  slots  ) );
      attr_list.setAttribute( new IntListAttribute( "Input", inputs ) );
    }

    // Solid angle
    float_attr =new FloatAttribute(Attribute.SOLID_ANGLE,
                                   GrpSolidAngle( group_id ) );
    attr_list.setAttribute( float_attr );

    // Efficiency Factor 
    attr_list.setAttribute( nominal_eff_attr );

    // Number of pulses...... 
    Add_NumberOfPulses( attr_list );

    // Total Counts  ........
    try{
      float total = 0;
      float counts[] = spectrum.getY_values();
      if ( counts != null )
        for ( int i = 0; i < counts.length; i++ )
          total += counts[i]; 
      float_attr = new FloatAttribute( Attribute.TOTAL_COUNT, total );
      attr_list.setAttribute( float_attr );
    }
    catch(Exception e)
    {
      System.out.println("Exception in RunfileRetriever.AddSpectrumAttributes");
      System.out.println("Exception is " + e );
      e.printStackTrace();
    };

    attr_list.trimToSize();
    spectrum.setAttributeList( attr_list );
  }


  /**
   *  Add "shared" number of pulses attribute to the specified Attribute list
   */
  private void Add_NumberOfPulses( AttributeList attr_list )
  {
    if ( num_pulses_attr == null )
      num_pulses_attr =new FloatAttribute(Attribute.NUMBER_OF_PULSES,
                                          (float)run_file.NumOfPulses());
    attr_list.setAttribute( num_pulses_attr );
  }


  /**
   *  Add "shared" initial path attribute to the specified Attribute list
   */
  private void Add_InitialPath( AttributeList attr_list )
  {
    if ( initial_path_attr == null )
      initial_path_attr =new FloatAttribute(Attribute.INITIAL_PATH,
                                           (float)run_file.SourceToSample());
    attr_list.setAttribute( initial_path_attr );
  }


  /**
   *  Add "shared" run number attribute to the specified Attribute list
   */
  private void Add_RunNumber( AttributeList attr_list )
  { 
    if ( run_num_attr == null )
    {
      int list[] = new int[1];
      list[0] =  run_file.RunNumber();
      run_num_attr =new IntListAttribute(Attribute.RUN_NUM, list );
    }
    attr_list.setAttribute( run_num_attr );
  }


  /**
   *  Add "shared" SampleOrientation attribute for SCD instruments. 
   *
   *  @param  attr_list  The list of attributes to which the orientation
   *                     attribute is added.  
   */
  private void AddSCD_SampleOrientation( AttributeList attr_list )
  {
    if ( scd_sample_orientation_attr == null )
    {
      SampleOrientation so = new IPNS_SCD_SampleOrientation(
                                      (float)run_file.Phi(),
                                      (float)run_file.Chi(), 
                                      (float)run_file.Omega() );
      scd_sample_orientation_attr = new SampleOrientationAttribute(
                                        Attribute.SAMPLE_ORIENTATION, so );
    }
    attr_list.setAttribute( scd_sample_orientation_attr );
  }

  /**
   * Add raw Detector (not segment) angle and secondary flight path to
   * the list of attributes.  
   *
   * @param attr_list The list of attributes to which the detector
   *                  attributes are added.
   * @param id        Detector number.
   */
    private void Add_DetectorCenterPosition( AttributeList attr_list, int id )
    {
                             // to allow sharing the attributes, we keep them 
                             // in a hash table, keyed by a string form of 
                             // the detector id.  This will work for an 
                             // arbitrary number of detectors.
       Integer key = new Integer(id);
       FloatAttribute float_attr = 
                      (FloatAttribute)det_cen_dist_attrs.get( key );
       if ( float_attr == null )
       {
         float_attr = new FloatAttribute( Attribute.DETECTOR_CEN_DISTANCE,
                                         (float)run_file.RawFlightPath(id));
         det_cen_dist_attrs.put( key, float_attr );
       }
       attr_list.setAttribute( float_attr );

       float_attr = (FloatAttribute)det_cen_angle_attrs.get( key );
       if ( float_attr == null )
       {
         float_attr = new FloatAttribute( Attribute.DETECTOR_CEN_ANGLE,
                                         (float)run_file.RawDetectorAngle(id));
         det_cen_angle_attrs.put( key, float_attr );
       } 
       attr_list.setAttribute( float_attr );
    }


  /**
   *  Add Detector data grid information to a list of all detectors.
   *
   * @param id        Detector number.
   */
    private void Add_DetectorDataGrid( int id )
    {
                             // to allow sharing the data grids, we keep them
                             // in a hash table, keyed by a string form of
                             // the detector id.  This will work for an
                             // arbitrary number of detectors.
       Integer key = new Integer(id);

       IDataGrid data_grid = (IDataGrid)det_data_grids.get( key );
       if ( data_grid == null )   // add new detector data grid to hashtable
       {
         float det_angle  = (float)run_file.RawDetectorAngle(id);
         float final_path = (float)run_file.RawFlightPath(id);
         float det_height = (float)run_file.RawDetectorHeight(id);


         float r = 0;                                 // patch for error with
                                                      // group 294 in some files
         if ( final_path * final_path < det_height * det_height )  
         {
           System.out.println("ERROR: in RunfileRetriever " + 
                              "final_path < det_height");
           System.out.println("       for detector ID = " + id );
           System.out.println("       final_path   = " + final_path );
           System.out.println("       det_height   = " + det_height );
           System.out.println("       Now using r = final_path as default.");
           r = final_path;
         }
         else
           r  = (float)Math.sqrt( final_path * final_path - 
                                  det_height * det_height );

         det_angle = (float)(det_angle * Math.PI/180);
         DetectorPosition det_pos = new DetectorPosition();
         det_pos.setCylindricalCoords( r, det_angle, det_height );
         Vector3D det_cen = new Vector3D( det_pos );
 
                             // construct orthonormal coordinate system for
                             // the detector, with z_vec normal to the surface
                             // of the detector at the center of the detector,
                             // x_vec in the direction of the detector rows and
                             // y_vec in the direction of the detector columns.
                             // That is, as a point moves in the y-direction
                             // the row numbers change.  This coordinate 
                             // system is first established with y_vec vertical
                             // and the detector rotated about a vertical axis
                             // through it's center, so that normal (z_vec)
                             // points towards the sample (or towards a point
                             // directly above or below the sample).
                             // Subsequently, the detector may be tilted about
                             // it's x-axis as specified by DetRot1, and/or 
                             // rotated about it's z-axis as specified by 
                             // DetRot2.
                                         // assume y_vec in detector coords 
                                         // is initially vertical in lab coords
         Vector3D y_vec;
         y_vec = new Vector3D( 0, 0, 1 );
                                         // temporarily make z_vec to be a
                                         // vector pointing from center
                                         // of the detector back to origin
         Vector3D z_vec   = new Vector3D( det_cen ); 
         z_vec.multiply(-1);
                                         // make the x_vec in detector coords
                                         // so we have a right hand system
         Vector3D x_vec   = new Vector3D();
         x_vec.cross( y_vec, z_vec );
         x_vec.normalize();
                                        // now make z_vec a unit normal 
                                        // perpendicular to x and y
         z_vec.cross( x_vec, y_vec );

         if ( run_file.DetRot1( id ) != 0 )        // tip the whole detector
         {                                         // towards the sample
           float angle = run_file.DetRot1( id );
           Tran3D rot_mat = new Tran3D();
           rot_mat.setRotation( angle, x_vec );
           rot_mat.apply_to( y_vec, y_vec );
           rot_mat.apply_to( z_vec, z_vec );
         }

         if ( run_file.DetRot2( id ) != 0 )        // rotate whole detector
         {                                         // about axis perpendicular
           float angle = run_file.DetRot2( id );   // to "face" of detector
           Tran3D rot_mat = new Tran3D();
           rot_mat.setRotation( angle, z_vec );
           rot_mat.apply_to( x_vec, x_vec );
           rot_mat.apply_to( y_vec, y_vec );
         }

         int n_rows = run_file.NumSegs1( id );     // NumSegs1 gives n_rows
         int n_cols = run_file.NumSegs2( id );     // NumSegs2 gives n_cols 

         int det_type = run_file.DetectorType( id );
         float width  = run_file.DetectorWidth(id)/100;
         float height = run_file.DetectorLength(id)/100;
         float depth  = run_file.DetectorDepth(id)/100;


         data_grid = new UniformGrid( id, "m",
                                      det_cen, x_vec, y_vec,
                                      width, height, depth,
                                      n_rows, n_cols );

   //    System.out.println("Built Uniform Grid for detector # " + data_grid );

         det_data_grids.put( key, data_grid );
       }
    }




 /**
  *  get the average flight path for the detectors that are in the specified
  *  group.  This routine should probably be put in the IPNS Runfile package.
  *
  *  @param  segs     Array of detector segments for this group
  *  @param  hist_num The histogram number for which the flight path is to
  *                   be found.
  *  @param  raw      If true, get the raw flight path, otherwise get the
  *                   effective flight path.
  *
  *  @return the averages of the flight paths of the detectors in this group
  */
  private float getAverageFlightPath(Segment segs[], int hist_num, boolean raw)
  {
    float values[] = new float[ segs.length ];
    try
    {
      for ( int i = 0; i < segs.length; i++ )
        if ( raw )
          values[i] = (float)run_file.RawFlightPath( segs[i] );
        else
          values[i] = (float)run_file.FlightPath( segs[i], hist_num );
    }
    catch ( Exception e )
    {
      System.out.println("Exception in RunfileRetriever.getAverageFlightPath:");
      System.out.println( "Exception is " + e );
      e.printStackTrace();
    }

    return arrayUtil.SignedAbsSum( values ) / values.length;
  }


 /**
  *  get the WEIGHTED average flight path for the detectors that are in the 
  *  specified group, weighted by the solid angle of the detectors.  
  *  This routine should probably be put in the IPNS Runfile package.
  *
  *  @param  segs     Array of detector segments for this group
  *  @param  hist_num The histogram number for which the flight path is to
  *                   be found.
  *  @param  solid_angles  Array of detector solid angles for this group
  *  @param  raw      If true, get the raw flight path, otherwise get the
  *                   effective flight path.
  *
  *  @return the averages of the flight paths of the detectors in this group
  */
  private float getAverageFlightPath( Segment  segs[], 
                                      int      hist_num,
                                      float    solid_angles[],
                                      boolean  raw )
  {
    float values[] = new float[ segs.length ];
    float tot_solid_ang = 0;
    try
    {
      for ( int i = 0; i < segs.length; i++ )
      {
        if ( raw )
          values[i] = (float)( solid_angles[i] * 
                               run_file.RawFlightPath( segs[i]) );
        else
          values[i] = (float)( solid_angles[i] * 
                               run_file.FlightPath( segs[i], hist_num) );
        tot_solid_ang += solid_angles[i];
      }
    }
    catch ( Exception e )
    {
      System.out.println("Exception in RunfileRetriever.getAverageFlightPath:");
      System.out.println( "Exception is " + e );
      e.printStackTrace();
    }

    return arrayUtil.SignedAbsSum( values ) / tot_solid_ang;
  }


 /**
  *  get the average "z" position for the detectors that are in the specified
  *  group.  This routine should probably be put in the IPNS Runfile package.
  *
  *  @param  segs     Array of detector segments for this group
  *  @param  hist_num The histogram number for which the average "z" position
  *                   is to be found.
  *  @param  raw      If true, get the raw "z" value, otherwise get the
  *                   effective "z" value.
  *
  *  @return the averages of the "z" positions of the detectors in this group
  */ 
  private float getAverageHeight( Segment segs[], 
                                  int     hist_num, 
                                  boolean raw )
  {
    float values[] = new float[ segs.length ];
    try
    {
      for ( int i = 0; i < segs.length; i++ )
        if ( raw )
          values[i] = (float)run_file.RawDetectorHeight( segs[i] );
        else
          values[i] = (float)run_file.DetectorHeight( segs[i], hist_num );
    }
    catch ( Exception e )
    {
      System.out.println( "Exception in RunfileRetriever.getAverageHeight:" );
      System.out.println( "Exception is " + e );
      e.printStackTrace();
    }

    return arrayUtil.SignedAbsSum( values ) / segs.length;   
  }


 /**
  *  get the WEIGHTED average "z" position for the detectors that are in 
  *  the specified group, weighted by the solid angle of the detectors.  
  *  This routine should probably be put in the IPNS Runfile package.
  *
  *  @param  segs          Array of detector segments for this group
  *  @param  hist_num      The histogram number for which the average "z" 
  *                        position is to be found.
  *  @param  solid_angles  Array of detector solid angles for this group
  *  @param  raw           If true, get the raw "z" value, otherwise get the
  *                        effective "z" value.
  *  
  *  @return the averages of the "z" positions of the detectors in this group
  */
  private float getAverageHeight( Segment segs[], 
                                  int     hist_num,
                                  float   solid_angles[], 
                                  boolean raw )
  {
    float values[] = new float[ segs.length ];
    float tot_solid_ang = 0;
    try
    {
      for ( int i = 0; i < segs.length; i++ )
      {
        if ( raw )
          values[i] = (float)( solid_angles[i] * 
                               run_file.RawDetectorHeight( segs[i] ));
        else
          values[i] = (float)( solid_angles[i] * 
                               run_file.DetectorHeight( segs[i], hist_num ));
        tot_solid_ang += solid_angles[i];
      }
    }
    catch ( Exception e )
    {
      System.out.println( "Exception in RunfileRetriever.getAverageHeight:" );
      System.out.println( "Exception is " + e );
      e.printStackTrace();
    }

    return arrayUtil.SignedAbsSum( values ) / tot_solid_ang;
  }



 /**
  *  get the average horizontal angle for the detectors that are in the 
  *  specified group.  This routine should probably be put in the IPNS 
  *  Runfile package.
  *
  *  @param  segs          Array of detector segments for this group
  *  @param  hist_num      The histogram number for which the average angle
  *                        is to be found
  *  @param  raw           If true, get the raw angle, otherwise get the
  *                        effective angle.
  *
  *  @return the averages of the horizontal angles of the detectors in this 
  *          group
  */
  private float getAverageAngle( Segment segs[], int hist_num, boolean raw )
  {
    float total = 0;
    try
    {
      for ( int i = 0; i < segs.length; i++ )
        if ( raw )
          total += run_file.RawDetectorAngle( segs[i] );
        else
          total += run_file.DetectorAngle( segs[i], hist_num );
    }
    catch ( Exception e )
    {
      System.out.println( "Exception in RunfileRetriever.getAverageAngle:" );
      System.out.println( "Exception is " + e );
      e.printStackTrace();
    }

    return total / segs.length;  
  }



 /**
  *  get the WEIGHTED average horizontal angle for the detectors that are in 
  *  the specified group, weighted by the solid angle of the detectors.  
  *  This routine should probably be put in the IPNS Runfile package.
  *
  *  @param  segs          Array of detector segments for this group
  *  @param  hist_num      The histogram number for which the average angle
  *                        is to be found
  *  @param  solid_angles  Array of detector solid angles for this group
  *  @param  raw           If true, get the raw angle, otherwise get the
  *                        effective angle.
  *
  *  @return the averages of the horizontal angles of the detectors in this
  *          group
  */
  private float getAverageAngle( Segment  segs[],  
                                 int      hist_num, 
                                 float    solid_angles[],
                                 boolean  raw )
  {
    float total = 0;
    float sum   = 0;
    try
    {
      for ( int i = 0; i < segs.length; i++ )
      {
        if ( raw )
          total += solid_angles[i] * run_file.RawDetectorAngle( segs[i] );
        else
          total += solid_angles[i] * run_file.DetectorAngle(segs[i], hist_num);
        sum   += solid_angles[i];
      }
    }
    catch ( Exception e )
    {
      System.out.println( "Exception in RunfileRetriever.getAverageAngle:" );
      System.out.println( "Exception is " + e );
      e.printStackTrace();
    }

    return total / sum;
  }


 /**
  *
  *  Calculate the total solid angle for the specified group of segments 
  * 
  *  @param  group_id  The id of the group of detectors whose solid angle is
  *                    to be computed.
  *
  *  @return The total solid angle 
  */
  private float GrpSolidAngle( int group_id )
  {
    Segment segs[] = run_file.SegsInSubgroup( group_id );

    float solid_angle = 0;

    for ( int seg_count = 0; seg_count < segs.length; seg_count++ )
      solid_angle += SegmentSolidAngle( segs[seg_count] );

    return solid_angle; 
  }


 /**
  *
  *  Calculate array of solid angles for the list of segements in a group
  *
  *  @param  group_id  The id of the group for which the list of  
  *                    solid angles is to be computed. 
  *
  *  @return The list of solid angles subtended by the segments in the 
  *          specified group.
  */
  private float[] GrpSolidAngles( int group_id )
  {
    Segment segs[]       = run_file.SegsInSubgroup( group_id );
    float solid_angles[] = new float[ segs.length ];

    for ( int seg_count = 0; seg_count < segs.length; seg_count++ )
      solid_angles[ seg_count ] = SegmentSolidAngle( segs[seg_count] );

    return solid_angles;
  }


 /**
  *
  *  Calculate the solid angle for the specified single segment 
  *
  *  @param  seg  The segment whose solid angle is to be computed.
  *
  *  @return The solid angle subtended by the segment.
  */
  private float SegmentSolidAngle( Segment seg )
  {
    float solid_angle = 0;
    float area,
          length,
          width,
          raw_dist,
          nom_radius,
          nom_height,
          nom_dist;
                                          // calculate using detector info      
/*
    int type = run_file.DetectorType( seg );
    length = DC5.LENGTH[ type ] / 100;   // convert cm to m
//    width  = DC5.WIDTH[ type ] / 100;    // convert cm to m
    width = .0254f;                        // assume 1" outside diameter to
                                             // match Chun's results
*/

    length = seg.Length()/100;           // calculate using info about segment
    width  = seg.Width()/100;            // convert cm to m

    nom_radius = (float) run_file.RawFlightPath( seg );
    nom_height = (float) run_file.RawDetectorHeight( seg );
    nom_dist   = (float) Math.sqrt( nom_radius * nom_radius +
                                      nom_height * nom_height );

                                        // compensate for effective path length
    raw_dist = (float) Math.sqrt( nom_dist * nom_dist -
                                  length * length / 12.0 );

    solid_angle += length*width / (raw_dist * raw_dist);
//    System.out.println("Det ID = " + seg.detID() +
//                       " type  = " + type + 
//                       " nom_dist = " + nom_dist +
//                       " raw_dist = " + raw_dist +
//                       " length = " + length +
//                       " width = " + width  +
//                       " solid ang = " + solid_angle );

    return solid_angle;
  }



/**
 *  Show Group Detector Info
 */
  private void ShowGroupSegmentInfo( int group_id, int hist )
  {
    Segment segs[] = run_file.SegsInSubgroup( group_id );
    int type; 
    Segment seg;
    float area,
          length,
          width,
          raw_dist,
          nom_radius,
          nom_height,
          nom_dist = 0,
          solid_angle,
          total_solid_angle = 0;

    try
    {
      System.out.println("---------- GROUP ID " + group_id + " ----------");
      for ( int i = 0; i < segs.length; i++ )
      {
        seg = segs[i];
        type = run_file.DetectorType( seg );
        length = DC5.LENGTH[ type ] / 100;   // convert cm to m
        width  = DC5.WIDTH[ type ] / 100;    // convert cm to m

        nom_radius = (float) run_file.RawFlightPath( seg );
        nom_height = (float) run_file.RawDetectorHeight( seg );
        nom_dist   = (float) Math.sqrt( nom_radius * nom_radius +
                                        nom_height * nom_height );

        raw_dist = (float) Math.sqrt( nom_dist * nom_dist -
                                    length * length / 12.0 );
        solid_angle = length*width / (raw_dist * raw_dist);

        System.out.println("ID = " + segs[i].SegID() +
           "  RawAng= " + (float)run_file.RawDetectorAngle( seg ) +
           "  EffAng= " + (float)run_file.DetectorAngle( seg, hist ) +
           "  RawHt= " + (float)nom_height +
           "  EffHt= " + (float)run_file.DetectorHeight( seg, hist ) +
           "  Pth= " + (float)nom_radius +
           "  EffPth= " + (float)run_file.FlightPath( seg, hist ) +
           "  RawD= " + raw_dist +
           "  SAng= " + solid_angle );
        total_solid_angle += solid_angle;
      }
      System.out.println("Total solid angle = " + total_solid_angle );

      System.out.println("Runfile's Group Effective Values:" );
      System.out.println(
           "Ang= " + (float)run_file.DetectorAngle( segs[0], hist ) +
           "  Ht= " +(float)run_file.DetectorHeight( segs[0], hist ) +
           "  Pth= " + (float)run_file.FlightPath( segs[0], hist ) ); 

      // Show position using RAW data and WEIGHTED average
      float solid_angs[] = GrpSolidAngles( group_id );
      DetectorPosition position = new DetectorPosition();

      float angle  = getAverageAngle( segs, hist, solid_angs, true );
      angle       *= (float)(Math.PI / 180.0);
      float height = getAverageHeight( segs, hist, solid_angs, true );
      float path   = getAverageFlightPath( segs, hist, solid_angs, true );
      float r      = (float)Math.sqrt( path * path - height * height );
      position.setCylindricalCoords( r, angle, height );

      float sphere_coords[] = position.getSphericalCoords();
      System.out.println( "Raw Position, weighted by solid angle ............");
      System.out.println( " R = " + sphere_coords[0] +
                          " Theta = " + sphere_coords[1]*180/3.14159265f +
                          " Phi = " + sphere_coords[2]*180/3.14159265f );

      // Show effective position using RAW data and simple average
      angle  = getAverageAngle( segs, hist, true );
      angle *= (float)(Math.PI / 180.0);
      height = getAverageHeight( segs, hist, true );
      path   = getAverageFlightPath( segs, hist, true );
      r      = (float)Math.sqrt( path * path - height * height );
      position.setCylindricalCoords( r, angle, height );

      sphere_coords = position.getSphericalCoords();
      System.out.println( "Raw Position, averaged ..................");
      System.out.println( " R = " + sphere_coords[0] +
                          " Theta = " + sphere_coords[1]*180/3.14159265f +
                          " Phi = " + sphere_coords[2]*180/3.14159265f );


      // Show position using EFFECTIVE data and WEIGHTED average
      angle  = getAverageAngle( segs, hist, solid_angs, false );
      angle *= (float)(Math.PI / 180.0);
      height = getAverageHeight( segs, hist, solid_angs, false );
      path   = getAverageFlightPath( segs, hist, solid_angs, false );
      r      = (float)Math.sqrt( path * path - height * height );
      position.setCylindricalCoords( r, angle, height );

      sphere_coords = position.getSphericalCoords();
      System.out.println( "Eff Position, weighted by solid angle ...........");
      System.out.println( " R = " + sphere_coords[0] +
                          " Theta = " + sphere_coords[1]*180/3.14159265f +
                          " Phi = " + sphere_coords[2]*180/3.14159265f );

      // Show position using EFFECTIVE data and simple average
      angle  = getAverageAngle( segs, hist, false );
      angle *= (float)(Math.PI / 180.0);
      height = getAverageHeight( segs, hist, false );
      path   = getAverageFlightPath( segs, hist, false );
      r      = (float)Math.sqrt( path * path - height * height );
      position.setCylindricalCoords( r, angle, height );

      sphere_coords = position.getSphericalCoords();
      System.out.println( "Eff Position, averaged ..................");
      System.out.println( " R = " + sphere_coords[0] +
                          " Theta = " + sphere_coords[1]*180/3.14159265f +
                          " Phi = " + sphere_coords[2]*180/3.14159265f );
    }
    catch ( Exception e )
    {
      System.out.println( 
                 "Exception in RunfileRetriever.ShowGroupSegmentInfo:" );
      System.out.println( "Exception is " + e );
      e.printStackTrace();
    }
  }

  public static void main(String[] args)
  {
    String file_names[] = { "/usr/local/ARGONNE_DATA/wrchen03/INS/sand19878.run" };
//    String file_names[] = { "/usr/local/ARGONNE_DATA/glad0816.run" };
//    String file_names[] = { "/home/dennis/glad7197.run" };
//    String file_names[] = { "/usr/local/ARGONNE_DATA/SCD_QUARTZ/SCD06496.RUN" };
/*    String file_names[] = { "/usr/local/ARGONNE_DATA/SCD_MnFl/SCD07940.RUN", 
                            "/usr/local/ARGONNE_DATA/SCD_MnFl/SCD07941.RUN",
                            "/usr/local/ARGONNE_DATA/SCD_MnFl/SCD07942.RUN",
                            "/usr/local/ARGONNE_DATA/SCD_MnFl/SCD07943.RUN",
                            "/usr/local/ARGONNE_DATA/SCD_MnFl/SCD07944.RUN",
                            "/usr/local/ARGONNE_DATA/SCD_MnFl/SCD07945.RUN" };
*/
    System.out.println("Start test program"); 

    RunfileRetriever rr;
    DataSet ds[] = new DataSet[ file_names.length ]; 
//    for ( int i = 0; i < 1; i++ )
    for ( int i = 0; i < file_names.length; i++ )
    {
      System.out.println("Loading: " + file_names[i] +"....");
      rr = new RunfileRetriever( file_names[i] ); 
      ds[i] = rr.getFirstDataSet(Retriever.HISTOGRAM_DATA_SET);
      rr = null;
      System.out.println("Data loaded for data set # " + i + "--------------------" );

      System.out.println("Trying GC...");
      System.gc();
    }

    int index = 154;
    System.out.println("Looking at group with index = " + index );
    Data d = ds[0].getData_entry(index);
    float y[] = d.getY_values();
    float sum = 0;
    for ( int i = 0; i < y.length; i++ )
    {
      sum += y[i];
      System.out.println("i, y = " + i + ", " + y[i] );
    }
    System.out.println("TOTAL = " + sum );
 
    System.out.println("Trying GC...");
    System.gc();

    try   { Thread.currentThread().sleep(5000); } 
    catch (InterruptedException ie) { }

    System.out.println("Trying GC...");
    System.gc();
/*
    System.out.println("Displaying Image...");
    ViewManager vm = new ViewManager( ds[0], IViewManager.IMAGE ); 
    try   { Thread.currentThread().sleep(5000); }                
    catch (InterruptedException ie) { }
*/
    System.out.println("End test program"); 
//    System.exit(1);
  }

}

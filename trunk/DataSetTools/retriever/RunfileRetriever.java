/*
 * File:  RunfileRetriever.java
 *
 * Copyright (C) 1999, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.33  2001/08/16 01:18:23  dennis
 *  Now sets the ID for the detector element using Segment.DetID() method.
 *
 *  Revision 1.32  2001/08/13 15:06:43  dennis
 *  Added MonitorTofToEnergy conversion operator for beam monitors.
 *
 *  Revision 1.31  2001/07/30 18:48:09  dennis
 *  Now adds DS_TYPE attribute to the DataSets.
 *
 *  Revision 1.30  2001/07/27 15:53:52  dennis
 *  Now also tries to get the instrument type from the file name, if
 *  the Runfile package returns type unknown.
 *
 *  Revision 1.29  2001/07/25 18:15:27  dennis
 *  Now trims the title from the runfile.
 *
 *  Revision 1.28  2001/07/10 20:24:12  dennis
 *  Now adds "Omega" attribute to each spectrum.
 *
 *  Revision 1.27  2001/07/10 19:36:24  dennis
 *  Now imports DataSetTools/instruments/DetectorInfo
 *
 *  Revision 1.26  2001/07/10 18:59:16  dennis
 *  First attempt at using the new detector "segment"
 *  methods from the IPNS/Runfile package.
 *
 *  Revision 1.25  2001/07/04 15:23:58  dennis
 *  Calculation of average heights and angles now uses the method
 *  arrayUtil.SignedAbsSum().
 *
 *  Revision 1.24  2001/06/28 14:09:25  dennis
 *  Now traps for height > final_path, which led to detector positions
 *  with NaN values.
 *
 *  Revision 1.23  2001/04/25 21:57:57  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.22  2001/03/01 20:48:02  dennis
 *  Now gets the instrument type using the method InstrumentType()
 *  from the IPNS.Runfile.Runfile class, rather than parsing the
 *  file name.
 *
 *  Revision 1.21  2001/02/22 21:00:46  dennis
 *  Improved error checking in getDataSet() method.
 *
 *  Revision 1.20  2001/01/31 14:23:48  dennis
 *  Now uses CVS version numbers.
 *
 *  Revision 1.19  2001/01/29 21:13:17  dennis
 *  Simplified logic for adding operators for
 *  DG Spectrometers and Pulse Height Spectra.
 *  Also, now uses CVS revision numbers.
 *
 *  Revision 1.17  2000/10/10 19:58:42  dennis
 *  Now gets pulse height spectrum as a special type of DataSet.
 *  Fixed bug that didn't correct runfile name upper/lower case
 *  properly.
 *
 *  Revision 1.15  2000/08/03 21:40:15  dennis
 *  Now calls FilenameUtil.fixCase()
 *
 *  Revision 1.14  2000/08/01 19:06:07  dennis
 *  Now sets sqrt(counts) errors on initial spectrum data
 *
 *  Revision 1.13  2000/07/31 20:49:05  dennis
 *  Now calculates effective positions weighted by solid angles ONLY for
 *  direct geometry spectrometers.  For all other instruments, just use
 *  Runfile routine for angle and calculate un-weighted average for
 *  the detector height and flight path length.
 *
 *  Revision 1.12  2000/07/28 18:42:57  dennis
 *  Added call to fixSeparator() for the file name.
 *
 *  Revision 1.11  2000/07/26 19:12:41  dennis
 *  Compute detector positions for direct geometry spectrometers based on the
 *  raw detector angles and heights, but effective flight path.
 *  Compute detector postions for all other instruments based on effecitve
 *  angles, heights and flight paths.
 *
 *  Revision 1.10  2000/07/25 16:37:31  dennis
 *  Added NUMBER_OF_PULSES attribute to each spectrum
 *
 *  Revision 1.9  2000/07/24 15:50:12  dennis
 *  Added MonitorPeakArea to monitor DataSet
 *
 *  Revision 1.8  2000/07/21 20:18:02  dennis
 *  Added tests for null Runfile object which occurs if the file is not opened
 *  successfully.
 *
 *  Revision 1.7  2000/07/18 19:54:28  dennis
 *  Added flexible routines for calculating average positions from either RAW
 *  or EFFECTIVE values and optionally weighting the values by the corresponding
 *  solid angles.
 *
 *  Revision 1.6  2000/07/17 20:54:19  dennis
 *  Temporarily added EnergyFromMonitorDS() operator to monitor DataSets
 *
 *  Revision 1.5  2000/07/13 14:23:43  dennis
 *  Changed TOTAL_COUNTS to TOTAL_COUNT to remove redundant attribute name
 *
 *  Revision 1.4  2000/07/11 21:18:35  dennis
 *  Added private method to show information about the detectors in a group
 *
 *  Revision 1.3  2000/07/10 22:49:46  dennis
 *  Now Uses CVS 
 *
 *  Revision 1.22  2000/06/14 14:55:42  dennis
 *  Added getFirstDataSet(type) to return the first monitor or first
 *  histogram DataSet in the runfile.
 *
 *  Revision 1.21  2000/06/14 13:57:43  dennis
 *  Changed to return histogram of raw counts rather than counts/microsecond
 *
 *  Revision 1.20  2000/06/14 13:56:13  dennis
 *  Added rudimentary support for IDGSpectrometer and Reflectometer
 *
 *  Revision 1.19  2000/06/01 18:41:08  dennis
 *  Changed separtor to "_" rather than blank in DataSet title.
 *  Added solid angle, delta 2 theta and efficiency factor.
 *
 *  Revision 1.18  2000/05/18 21:26:47  dennis
 *  Added the fully qualified file name as a DataSet attribute and
 *  added the time field type as a Data block attribute.
 *
 *  Revision 1.17  2000/05/12 21:39:39  dennis
 *  Now only creates Data blocks that share references to time field types,
 *  if possible
 *
 *  Revision 1.16  2000/05/11 16:19:12  dennis
 *  added RCS logging
 *
 *  Dennis Mikkelson, 3/23/2000  Added code to all catch{} blocks to print
 *                               info about the exception.
 *                               Fixed bug in getting the total counts...
 *                                 ( must pass group_id, NOT a detector id
 *                                    to Get1DSum( ) )
 *                               Added documentation for all routines
 * 
 *  Alok Chatterjee, Fall 1999.  Added number of pulses and total counts
 *                               information.
 */
package DataSetTools.retriever;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.InstrumentType;
import DataSetTools.instruments.DetectorInfo;
import IPNS.Runfile.*;
import DataSetTools.math.*;
import DataSetTools.util.*;
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

  private static int instrument_type = InstrumentType.UNKNOWN;

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
    String    file_name     = StringUtil.fixSeparator( data_source_name );

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
      instrument_type  = run_file.InstrumentType();
      if ( instrument_type == InstrumentType.UNKNOWN )
        instrument_type = InstrumentType.getIPNSInstrumentType( file_name );
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
 *  @return the requested DataSet.
 */
  public DataSet getDataSet( int data_set_num )
  {
    if ( run_file == null )
      return null;

//    System.out.println("======= getting dataset for >>>" + data_source_name );

    if ( data_set_num < 0 || data_set_num >= num_data_sets )
      return null;

    return getDataSet( data_set_num, instrument_type );
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
 *
 *  @return the requested DataSet.
 */
  private DataSet getDataSet( int data_set_num, int instrument_type )
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
    {
                                            // Construct the empty DataSet
                                            // with reasonable defaults

     DataSetFactory ds_factory = new DataSetFactory( title );
//     System.out.println("instrument_type = " + instrument_type );
     if ( is_monitor )
     {
       data_set = ds_factory.getDataSet();
       data_set.addOperator( new MonitorTofToEnergy() );
     }
     else if ( is_pulse_height )
       data_set = ds_factory.getDataSet();  // just generic operations

     else                                   // get data_set with ops for the
                                            // current instrument
       data_set = ds_factory.getTofDataSet( instrument_type );  

                                            // Adjust the empty DataSet based
                                            // on the current situation 
     if ( is_monitor )
     {
       data_set.addOperator( new EnergyFromMonitorDS() );
       data_set.addOperator( new MonitorPeakArea() );
     }
     else if ( is_pulse_height )
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
      int     group_members[]  = run_file.IdsInSubgroup( group_id );
      Segment group_segments[] = run_file.SegsInSubgroup( group_id );
      if ( group_members.length != group_segments.length )
        System.out.println("ERROR: Wrong number of segments");

      if ( group_members.length > 0 )   // only deal with non-trivial groups
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

          if ( is_pulse_height )          // change bin bounds to channel number
            for ( int chan=0; chan<num_times; chan ++ )
              bin_boundaries[chan] = chan;

            // change times to sample to detector TOF for spectrometers 
            // and groups that are NOT beam monitors 
            // if there is valid source_to_sample information available
          else if ( instrument_type == InstrumentType.TOF_DG_SPECTROMETER &&
                   !is_monitor                                           )
           {
             source_to_sample_tof = (float)run_file.SourceToSampleTime();
             if ( !Float.isInfinite(source_to_sample_tof) )
               for ( int i = 0; i < bin_boundaries.length; i++ )
                 bin_boundaries[i] -= source_to_sample_tof;
           }
/*  ####
           System.out.println("bin boundaries for group ID " + group_id );
           for ( int ii = 0; ii < bin_boundaries.length; ii++ )
             System.out.print(" " + bin_boundaries[ii] );
           System.out.println();
*/
           x_scale = new VariableXScale( bin_boundaries );
        }

        if ( num_times > 1 )
        {
          raw_spectrum = run_file.Get1DSpectrum( group_id );
          if ( raw_spectrum.length >= 1 )
          {
            spectrum = new Data( x_scale, raw_spectrum, group_id );
            spectrum.setSqrtErrors();

            // Add the relevant attributes ----------------------------------
            AddSpectrumAttributes( instrument_type,
                                   histogram_num,
                                   group_id,
                                   group_members, 
                                   group_segments,
                                   tf_type,
                                   spectrum      );
 
            // Now add the spectrum to the DataSet -------------------------
            data_set.addData_entry( spectrum );
          }
        }
      }
    }

    run_file.Close(); 

    } 
    catch( Exception e )
    {
      System.out.println("Exception in RunfileRetriever.getDataSet()" );
      System.out.println("Exception is " +  e );
      e.printStackTrace();
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
    list[0] =  run_file.RunNumber();
    int_list_attr =new IntListAttribute(Attribute.RUN_NUM, list );
    attr_list.setAttribute( int_list_attr );

    // End Date  ........
    str_attr = new StringAttribute( Attribute.END_DATE, run_file.EndDate() );
    attr_list.setAttribute( str_attr );

    // End Time  ........
    str_attr = new StringAttribute( Attribute.END_TIME, run_file.EndTime() );
    attr_list.setAttribute( str_attr );
    
    // Number of Pulses  ........
    int_attr = new IntAttribute( Attribute.NUMBER_OF_PULSES, 
                                 run_file.NumOfPulses() );
    attr_list.setAttribute( int_attr );
    
    ds.setAttributeList( attr_list );
  }



/**
 *  Add the Spectrum attributes to the specified Data block.
 *
 *  @param  instrument_type  The file name for this DataSet
 *  @param  histogram_num    The histogram number for this group
 *  @param  group_id         The group_id for this group
 *  @param  group_members    The list of Detectors that belong to this group
 *  @param  spectrum         The Data block to which the attributes are added  
 *
 */
  private void AddSpectrumAttributes( int     instrument_type,
                                      int     histogram_num,
                                      int     group_id,
                                      int     group_members[], 
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
    int list[] = new int[1]; 
    list[0] =  run_file.RunNumber();
    int_list_attr =new IntListAttribute(Attribute.RUN_NUM, list );
    attr_list.setAttribute( int_list_attr );

    // Time field type
    int_attr = new IntAttribute( Attribute.TIME_FIELD_TYPE, tf_type );
    attr_list.setAttribute( int_attr );

    // Detector ID list ..........
    int_list_attr = new IntListAttribute( Attribute.DETECTOR_IDS,
                                            group_members );
    attr_list.setAttribute( int_list_attr );

    // Initial flight path ............
    float_attr = new FloatAttribute( Attribute.INITIAL_PATH,
                                     (float)run_file.SourceToSample() );
    attr_list.setAttribute( float_attr );

    // Initial energy ...........
    if ( instrument_type == InstrumentType.TOF_DG_SPECTROMETER )
    {
      float_attr =new FloatAttribute(Attribute.NOMINAL_ENERGY_IN,
                                     (float)run_file.EnergyIn() );
      attr_list.setAttribute( float_attr );

      float_attr =new FloatAttribute(Attribute.ENERGY_IN,
                                     (float)run_file.EnergyIn() );
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
      angle      = getAverageAngle( group_members, 
                                    histogram_num, 
                                    solid_angles,
                                    true  );   
      angle      *= (float)(Math.PI / 180.0);
  
      height     = getAverageHeight( group_members, solid_angles, true );

      final_path = getAverageFlightPath( group_members, 
                                         histogram_num, 
                                         solid_angles,
                                         false );
    }
    else           //  Just get the values from the runfile where possible
    {              //  don't weight by solid angles

      angle = (float)run_file.DetectorAngle(group_members[0], histogram_num);
      angle      *= (float)(Math.PI / 180.0);

      height     = getAverageHeight( group_members, false );
      final_path = getAverageFlightPath(group_members, histogram_num, false);
    }


    // We should probably use the following to form weighted average of 
    // effective detector angles, heights and flight path lengths, for all 
    // other instruments, but for now, it doesn't work since the detector
    // type of LPSD's is set to 0 by the Runfile package.  Consequently,
    // the solid angles are all 0 and so the angle, height and final_path
    // also turn out to be 0 for such instruments. 
    // else
    // {
    //  angle      = getAverageAngle( group_members,
    //                                histogram_num,
    //                                solid_angles,
    //                                false );
    //  angle      *= (float)(Math.PI / 180.0);
    //
    //  height     = getAverageHeight( group_members, solid_angles, false );
    //
    //  final_path = getAverageFlightPath( group_members,
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
    float omega = tof_calc.Omega( angle * 180 / 3.14159264f );
    FloatAttribute omega_attr = new FloatAttribute( Attribute.OMEGA, omega );
    attr_list.setAttribute( omega_attr );

    // Raw Detector Angle ...........
    float_attr =new FloatAttribute(Attribute.RAW_ANGLE,
                      (float)run_file.RawDetectorAngle( group_members[0]) );
    attr_list.setAttribute( float_attr );

    // Delta 2 theta ( range of scattering angles covered ), assuming 1" tube
    float det_width_factor = 1.45530928f;  // detector width in meters times
                                           // 180/PI to convert from radians
                                           // to degrees.
    float delta_2theta = det_width_factor/final_path;
    float_attr =new FloatAttribute(Attribute.DELTA_2THETA, delta_2theta  );
    attr_list.setAttribute( float_attr );

    // DetectorInfo ....

    DetectorInfo det_info_list[] = new DetectorInfo[ group_members.length ];
    for ( int id = 0; id < group_members.length; id++ )
    {
      DetectorPosition det_position = new DetectorPosition();
      float seg_angle  = (float)run_file.RawDetectorAngle( group_segments[id] );
            seg_angle *= (float)(Math.PI / 180.0);
      float seg_height = (float)run_file.RawDetectorHeight( group_segments[id]);
      float seg_path   = (float)run_file.RawFlightPath( group_segments[id] );
      float rho = 0;                                    // patch for error with
                                                        // group 294, some files
      if ( seg_path * seg_path < seg_height * seg_height )  
        rho = seg_path;
      else
        rho  = (float)Math.sqrt(seg_path * seg_path - seg_height * seg_height);
      
      det_position.setCylindricalCoords( rho, seg_angle, seg_height );

      DetectorInfo det_info = new DetectorInfo( group_segments[id].DetID(), 
                                              group_members[id], 
                                              group_segments[id].Row(),
                                              group_segments[id].Column(),
                                              det_position,
                                              group_segments[id].Length(),
                                              group_segments[id].Width(),
                                              group_segments[id].Depth(),
                                              group_segments[id].Efficiency() );
      det_info_list[id] = det_info;
    }
    if ( group_members.length > 0 )
    {
      DetInfoListAttribute det_info_attr = new DetInfoListAttribute( 
                             Attribute.DETECTOR_INFO_LIST, det_info_list );
      attr_list.setAttribute( det_info_attr );
    }

    // Solid angle
    //float_attr =new FloatAttribute(Attribute.SOLID_ANGLE,
    //                               run_file.SolidAngle( group_id ) );
    float_attr =new FloatAttribute(Attribute.SOLID_ANGLE,
                                   GrpSolidAngle( group_id ) );
    attr_list.setAttribute( float_attr );
    // Efficiency Factor 
    float_attr =new FloatAttribute(Attribute.EFFICIENCY_FACTOR, 1 );
    attr_list.setAttribute( float_attr );

    // Number of pulses...... 
    int_attr = new IntAttribute( Attribute.NUMBER_OF_PULSES, 
                                     run_file.NumOfPulses( ));
    attr_list.setAttribute( int_attr );


    // Total Counts  ........
    try{
    float_attr = new FloatAttribute( Attribute.TOTAL_COUNT, 
                               (float)run_file.Get1DSum( group_id ));
    attr_list.setAttribute( float_attr );
    }
    catch(Exception e)
    {
      System.out.println("Exception in RunfileRetriever.AddSpectrumAttributes");
      System.out.println("Exception is " + e );
      e.printStackTrace();
    };

    spectrum.setAttributeList( attr_list );
  }


 /**
  *  get the average flight path for the detectors that are in the specified
  *  group.  This routine should probably be put in the IPNS Runfile package.
  *
  *  @param  ids      Array of detector ids for this group
  *  @param  hist_num The histogram number for which the flight path is to
  *                   be found.
  *  @param  raw      If true, get the raw flight path, otherwise get the
  *                   effective flight path.
  *
  *  @return the averages of the flight paths of the detectors in this group
  */
  private float getAverageFlightPath( int ids[], int hist_num, boolean raw )
  {
    float values[] = new float[ ids.length ];
    try
    {
      for ( int i = 0; i < ids.length; i++ )
        if ( raw )
          values[i] = (float)run_file.RawFlightPath( ids[i] );
        else
          values[i] = (float)run_file.FlightPath( ids[i], hist_num );
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
  *  @param  ids      Array of detector ids for this group
  *  @param  hist_num The histogram number for which the flight path is to
  *                   be found.
  *  @param  solid_angles  Array of detector solid angles for this group
  *  @param  raw      If true, get the raw flight path, otherwise get the
  *                   effective flight path.
  *
  *  @return the averages of the flight paths of the detectors in this group
  */
  private float getAverageFlightPath( int      ids[], 
                                      int      hist_num,
                                      float    solid_angles[],
                                      boolean  raw )
  {
    float values[] = new float[ ids.length ];
    float tot_solid_ang = 0;
    try
    {
      for ( int i = 0; i < ids.length; i++ )
      {
        if ( raw )
          values[i] = (float)( solid_angles[i] * 
                               run_file.RawFlightPath( ids[i]) );
        else
          values[i] = (float)( solid_angles[i] * 
                               run_file.FlightPath( ids[i], hist_num) );
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
  *  @param  ids      Array of detector ids for this group
  *  @param  raw      If true, get the raw "z" value, otherwise get the
  *                   effective "z" value.
  *
  *  @return the averages of the "z" positions of the detectors in this group
  */ 
  private float getAverageHeight( int ids[], boolean raw )
  {
    float values[] = new float[ ids.length ];
    try
    {
      for ( int i = 0; i < ids.length; i++ )
        if ( raw )
          values[i] = (float)run_file.RawDetectorHeight( ids[i] );
        else
          values[i] = (float)run_file.DetectorHeight( ids[i] );
    }
    catch ( Exception e )
    {
      System.out.println( "Exception in RunfileRetriever.getAverageHeight:" );
      System.out.println( "Exception is " + e );
      e.printStackTrace();
    }

    return arrayUtil.SignedAbsSum( values ) / ids.length;   
  }


 /**
  *  get the WEIGHTED average "z" position for the detectors that are in 
  *  the specified group, weighted by the solid angle of the detectors.  
  *  This routine should probably be put in the IPNS Runfile package.
  *
  *  @param  ids           Array of detector ids for this group
  *  @param  solid_angles  Array of detector solid angles for this group
  *  @param  raw           If true, get the raw "z" value, otherwise get the
  *                        effective "z" value.
  *  
  *  @return the averages of the "z" positions of the detectors in this group
  */
  private float getAverageHeight(int ids[], float solid_angles[], boolean raw)
  {
    float values[] = new float[ ids.length ];
    float tot_solid_ang = 0;
    try
    {
      for ( int i = 0; i < ids.length; i++ )
      {
        if ( raw )
          values[i] = (float)( solid_angles[i] * 
                               run_file.RawDetectorHeight( ids[i] ));
        else
          values[i] = (float)( solid_angles[i] * 
                               run_file.DetectorHeight( ids[i] ));
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
  *  @param  ids   Array of detector ids for this group
  *  @param  hist_num      The histogram number for which the average angle
  *                        is to be found
  *  @param  raw           If true, get the raw angle, otherwise get the
  *                        effective angle.
  *
  *  @return the averages of the horizontal angles of the detectors in this 
  *          group
  */
  private float getAverageAngle( int ids[], int hist_num, boolean raw )
  {
    float total = 0;
    try
    {
      for ( int i = 0; i < ids.length; i++ )
        if ( raw )
          total += run_file.RawDetectorAngle( ids[i] );
        else
          total += run_file.DetectorAngle( ids[i], hist_num );
    }
    catch ( Exception e )
    {
      System.out.println( "Exception in RunfileRetriever.getAverageAngle:" );
      System.out.println( "Exception is " + e );
      e.printStackTrace();
    }

    return total / ids.length;  
  }



 /**
  *  get the WEIGHTED average horizontal angle for the detectors that are in 
  *  the specified group, weighted by the solid angle of the detectors.  
  *  This routine should probably be put in the IPNS Runfile package.
  *
  *  @param  ids           Array of detector ids for this group
  *  @param  hist_num      The histogram number for which the average angle
  *                        is to be found
  *  @param  solid_angles  Array of detector solid angles for this group
  *  @param  raw           If true, get the raw angle, otherwise get the
  *                        effective angle.
  *
  *  @return the averages of the horizontal angles of the detectors in this
  *          group
  */
  private float getAverageAngle( int      ids[],  
                                 int      hist_num, 
                                 float    solid_angles[],
                                 boolean  raw )
  {
    float total = 0;
    float sum   = 0;
    try
    {
      for ( int i = 0; i < ids.length; i++ )
      {
        if ( raw )
          total += solid_angles[i] * run_file.RawDetectorAngle( ids[i] );
        else
          total += solid_angles[i] * run_file.DetectorAngle( ids[i], hist_num );
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
  *  Calculate the total solid angle for the specified group of detectors
  * 
  *  @param  group_id  The id of the group of detectors whose solid angle is
  *                    to be computed.
  *
  *  @return The total solid angle 
  */
  private float GrpSolidAngle( int group_id )
  {
    int ids[] = run_file.IdsInSubgroup( group_id );

    float solid_angle = 0;
    int   id;

    for ( int det_count = 0; det_count < ids.length; det_count++ )
    {
      id     = ids[ det_count ];
      solid_angle += DetSolidAngle( id );
    }

    return solid_angle; 
  }


 /**
  *
  *  Calculate array of solid angles for the list of detectors in a group
  *
  *  @param  group_id  The id of the group for which the list of detector
  *                    whose solid angle is to be computed.  *
  *
  *  @return The list of solid angles subtended by the detectors in the 
  *          specified group.
  */
  private float[] GrpSolidAngles( int group_id )
  {
    int   ids[]          = run_file.IdsInSubgroup( group_id );
    float solid_angles[] = new float[ ids.length ];

    int   id;
    for ( int det_count = 0; det_count < ids.length; det_count++ )
    {
      id     = ids[ det_count ];
      solid_angles[ det_count ] = DetSolidAngle( id );
    }

    return solid_angles;
  }


 /**
  *
  *  Calculate the solid angle for the specified single detector
  *
  *  @param  det_id  The id of the detector whose solid angle is to be computed.
  *
  *  @return The solid angle subtended by the detector.
  */
  private float DetSolidAngle( int det_id )
  {
    float solid_angle = 0;
    int   type;
    float area,
          length,
          width,
          raw_dist,
          nom_radius,
          nom_height,
          nom_dist;

    type   = run_file.DetectorType( det_id );
    length = Runfile.LENGTH[ type ] / 100;   // convert cm to m
//    width  = Runfile.WIDTH[ type ] / 100;    // convert cm to m
    width = .0254f;                        // assume 1" outside diameter to
                                             // match Chun's results

    nom_radius = (float) run_file.RawFlightPath( det_id );
    nom_height = (float) run_file.RawDetectorHeight( det_id );
    nom_dist   = (float) Math.sqrt( nom_radius * nom_radius +
                                      nom_height * nom_height );

    raw_dist = (float) Math.sqrt( nom_dist * nom_dist -
                                  length * length / 12.0 );
    solid_angle += length*width / (raw_dist * raw_dist);
//    System.out.println("Det ID = " + det_id +
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
  private void ShowGroupDetectorInfo( int group_id, int hist )
  {
    int ids[] = run_file.IdsInSubgroup( group_id );
    int type, 
        id;
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
      for ( int i = 0; i < ids.length; i++ )
      {
        id = ids[i];
        type = run_file.DetectorType( id );
        length = Runfile.LENGTH[ type ] / 100;   // convert cm to m
        width  = Runfile.WIDTH[ type ] / 100;    // convert cm to m

        nom_radius = (float) run_file.RawFlightPath( id );
        nom_height = (float) run_file.RawDetectorHeight( id );
        nom_dist   = (float) Math.sqrt( nom_radius * nom_radius +
                                        nom_height * nom_height );

        raw_dist = (float) Math.sqrt( nom_dist * nom_dist -
                                    length * length / 12.0 );
        solid_angle = length*width / (raw_dist * raw_dist);

        System.out.println("ID = " + ids[i] +
           "  RawAng= " + (float)run_file.RawDetectorAngle( ids[i] ) +
           "  EffAng= " + (float)run_file.DetectorAngle( ids[i], hist ) +
           "  RawHt= " + (float)nom_height +
           "  EffHt= " + (float)run_file.DetectorHeight( id ) +
           "  Pth= " + (float)nom_radius +
           "  EffPth= " + (float)run_file.FlightPath( id, hist ) +
           "  RawD= " + raw_dist +
           "  SAng= " + solid_angle );
        total_solid_angle += solid_angle;
      }
      System.out.println("Total solid angle = " + total_solid_angle );

      System.out.println("Runfile's Group Effective Values:" );
      System.out.println(
           "Ang= " + (float)run_file.DetectorAngle( ids[0], hist ) +
           "  Ht= " +(float)run_file.DetectorHeight( ids[0] ) +
           "  Pth= " + (float)run_file.FlightPath( ids[0], hist ) ); 

      // Show position using RAW data and WEIGHTED average
      float solid_angs[] = GrpSolidAngles( group_id );
      DetectorPosition position = new DetectorPosition();

      float angle  = getAverageAngle( ids, hist, solid_angs, true );
      angle       *= (float)(Math.PI / 180.0);
      float height = getAverageHeight( ids, solid_angs, true );
      float path   = getAverageFlightPath( ids, hist, solid_angs, true );
      float r      = (float)Math.sqrt( path * path - height * height );
      position.setCylindricalCoords( r, angle, height );

      float sphere_coords[] = position.getSphericalCoords();
      System.out.println( "Raw Position, weighted by solid angle ............");
      System.out.println( " R = " + sphere_coords[0] +
                          " Theta = " + sphere_coords[1]*180/3.14159265f +
                          " Phi = " + sphere_coords[2]*180/3.14159265f );

      // Show effective position using RAW data and simple average
      angle  = getAverageAngle( ids, hist, true );
      angle *= (float)(Math.PI / 180.0);
      height = getAverageHeight( ids, true );
      path   = getAverageFlightPath( ids, hist, true );
      r      = (float)Math.sqrt( path * path - height * height );
      position.setCylindricalCoords( r, angle, height );

      sphere_coords = position.getSphericalCoords();
      System.out.println( "Raw Position, averaged ..................");
      System.out.println( " R = " + sphere_coords[0] +
                          " Theta = " + sphere_coords[1]*180/3.14159265f +
                          " Phi = " + sphere_coords[2]*180/3.14159265f );


      // Show position using EFFECTIVE data and WEIGHTED average
      angle  = getAverageAngle( ids, hist, solid_angs, false );
      angle *= (float)(Math.PI / 180.0);
      height = getAverageHeight( ids, solid_angs, false );
      path   = getAverageFlightPath( ids, hist, solid_angs, false );
      r      = (float)Math.sqrt( path * path - height * height );
      position.setCylindricalCoords( r, angle, height );

      sphere_coords = position.getSphericalCoords();
      System.out.println( "Eff Position, weighted by solid angle ...........");
      System.out.println( " R = " + sphere_coords[0] +
                          " Theta = " + sphere_coords[1]*180/3.14159265f +
                          " Phi = " + sphere_coords[2]*180/3.14159265f );

      // Show position using EFFECTIVE data and simple average
      angle  = getAverageAngle( ids, hist, false );
      angle *= (float)(Math.PI / 180.0);
      height = getAverageHeight( ids, false );
      path   = getAverageFlightPath( ids, hist, false );
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
                 "Exception in RunfileRetriever.ShowGroupDetectorInfo:" );
      System.out.println( "Exception is " + e );
      e.printStackTrace();
    }
  }


  public static void main(String[] args)
  {
    System.out.println("No test program"); 
  }

}

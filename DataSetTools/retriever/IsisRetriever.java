/*
 * File:  IsisRetriever.java
 *
 * Copyright (C) 2004, Dennis Mikkelson
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
 *  Revision 1.4  2005/01/10 15:55:06  dennis
 *  Removed empty statement.
 *
 *  Revision 1.3  2004/07/14 16:51:20  dennis
 *  Temporarily added some restritions to the ID ranges to avoid problems
 *  with missing time regimes, etc.
 *
 *  Revision 1.2  2004/07/08 16:05:33  dennis
 *  Removed some "workarounds" since underlying ISIS Rawfile class has
 *  been fixed.  Specifically, LeaveOpen() no longer hangs the system,
 *  the first histogram is now histogram 1 and methods to get the
 *  min and max monitor ID have been implemented.
 *
 *  Revision 1.1  2004/07/02 16:54:44  dennis
 *  Initial version of data retriever for ISIS files.  This version is
 *  basically working for SXDII files, though the monitor position
 *  is not yet properly read.
 *
 *
 */

package DataSetTools.retriever;

import DataSetTools.dataset.*;
import DataSetTools.instruments.InstrumentType;
import DataSetTools.instruments.*;
import DataSetTools.viewer.*;
import ISIS.Rawfile.*;
import DataSetTools.math.*;
import DataSetTools.util.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.Sys.*;

import java.util.*;
import java.io.*;

/**
 *  This class opens an ISIS raw file and extracts DataSets corresponding to
 *  monitors and the sample histogram.
 */
public class IsisRetriever extends    Retriever
                           implements Serializable
{
  private int     num_data_sets = 0;  // The number of distinct DataSets.
                                      // This should be two.  The monitor 
                                      // DataSet and the sample histogram.

  private int[]   data_set_type = null;   
 
  private int[]   histogram = null;

  private Rawfile raw_file;

  private int     instrument_type = InstrumentType.UNKNOWN;

  private IntListAttribute run_num_attr      = null;
  private FloatAttribute   num_pulses_attr   = null;
  private FloatAttribute   initial_path_attr = null;
  private SampleOrientationAttribute
                           scd_sample_orientation_attr = null;

  private IDataGrid grids[] = null;

  int               first_spec_id[] = null;

/**
 *  construct an ISIS raw file retriever for a specific file.
 *
 *  @param data_source_name  The fully qualified file name for the raw file.
 */
  public IsisRetriever(String data_source_name)
  {
    super(data_source_name);

    int       first_id,
              last_id;

    boolean   has_monitors  = false;
    boolean   has_detectors = false;

    String    file_name     = StringUtil.setFileSeparator( data_source_name );

    file_name = FilenameUtil.fixCase( file_name );
    if ( file_name == null )
    {
      System.out.println("ERROR: file " + file_name +
                          " not found in IsisRetriever");
      raw_file = null;
      return;
    }

    try
    {
      raw_file = new Rawfile( file_name );
      raw_file.LeaveOpen();
      instrument_type = raw_file.InstrumentType();
      String name = raw_file.InstrumentName().trim();
      System.out.println("Instrument Type = " + instrument_type );
      System.out.println("Instrument Name = " + name );
      if ( instrument_type == InstrumentType.TOF_SCD  && name.equals( "SXD" ) )
        SetUpGridInfo( new SXD_Grids() );
      else
        System.out.println("NO DATA GRIDS AVAILABLE FOR " +
                            raw_file.InstrumentName()     );
      num_data_sets = 0;

      // for now just assume it has monitors
      has_monitors = true;
      num_data_sets++;

      for ( int i = 0; i < 46000; i++ )
        if ( raw_file.IsSubgroupBeamMonitor(i) )
          System.out.println("MONITOR # : " + i );
/*
      int monitor_ids[] = raw_file.MonitorDetNums();
      if ( monitor_ids != null && monitor_ids.length > 0 )
      {
        has_monitors = true;
        num_data_sets++;
      }
*/
      System.out.println("Looking for DataSets");
      int hist = 1;                               // only histogram is 0 #### 1

      first_id = raw_file.MinSubgroupID( hist );
      last_id  = raw_file.MaxSubgroupID( hist );
      System.out.println("Back from MinSubgroupID, first_id = " + first_id);
      System.out.println("Back from MaxSubgroupID, last_id = " + last_id );

      first_id = 2;
      last_id = 45057;
      System.out.println("Set ids to " + first_id + " to " + last_id );

      if ( last_id > first_id )
      {
        has_detectors = true;
        num_data_sets++;
      }
      System.out.println("After step 1, num_data_sets = " + num_data_sets);

      if ( num_data_sets > 0 )
      {
        data_set_type = new int[ num_data_sets ];
        histogram     = new int[ num_data_sets ];

        int index = 0;
        if ( has_monitors )
        {
          data_set_type[ index ] = MONITOR_DATA_SET;
          histogram[ index ] = hist;
          index++;
        }

        if ( has_detectors )
        {
          data_set_type[ index ] = HISTOGRAM_DATA_SET;
          histogram[ index ] = hist;
        }
      }

      System.out.println("After step 2, num_data_sets = " + num_data_sets);
      raw_file.Close();
    }
    catch( Exception e )
    {
      raw_file = null;
      System.out.println("Exception in IsisRetriever constructor");
      System.out.println("Exception is " +  e );
      e.printStackTrace();
    }
  }


/**
 *  Get the number of distinct DataSets contained in this raw file. 
 *  The monitors are placed into one DataSet.  Any sample histograms are 
 *  placed into separate DataSets.  
 *   
 *  @return the number of distinct DataSets in this raw file.
 */
  public int numDataSets()
  {
    if ( raw_file == null )
      return 0;

    return num_data_sets;
  }


/**
 *  Get the type code of a particular DataSet in this raw file. 
 *  The type codes include:
 *
 *     Retriever.INVALID_DATA_SET
 *     Retriever.MONITOR_DATA_SET
 *     Retriever.HISTOGRAM_DATA_SET
 *     Retriever.PULSE_HEIGHT_DATA_SET
 *
 *  @param  data_set_num  The number of the DataSet in this raw file whose
 *                        type code is needed.  data_set_num must be between
 *                        0 and numDataSets()-1
 *
 *  @return the type code for the specified DataSet.
 */
  public int getType( int data_set_num )
  {
    if ( raw_file == null )
       return INVALID_DATA_SET;

    if ( data_set_num >= 0 && data_set_num < num_data_sets )
      return data_set_type[ data_set_num ];
    else
      return INVALID_DATA_SET;
  }


/**
 *  Get the histogram number of a particular DataSet in this raw file. 
 *  
 *  @param  data_set_num  The number of the DataSet in this raw file whose
 *                        histogram number is needed.  data_set_num must be 
 *                        between 0 and numDataSets()-1
 *
 *  @return the histogram number for the specified DataSet.
 */
  public int getHistogramNum( int data_set_num )
  {
    if ( raw_file == null )
      return INVALID_DATA_SET;

    if ( data_set_num >= 0 && data_set_num < num_data_sets )
      return histogram[ data_set_num ];
    else
      return INVALID_DATA_SET;
  }

 
/**
 *  Get the first DataSet in this raw file that has the specified type, 
 *  histogram, monitor or pulse height.  If no DataSet in the raw file has 
 *  the specified type this returns null.
 *
 *  @param type  The type of the DataSet to retrieve from the raw file.
 *                 Retriever.HISTOGRAM_DATA_SET 
 *                 Retriever.MONITOR_DATA_SET
 *                 Retriever.PULSE_HEIGHT_DATA_SET
 *
 *  @return  The first DataSet in the with the specified type, or null if
 *           no such DataSet exists in the raw file.
 */
 public DataSet getFirstDataSet( int type )
 {
   if ( raw_file == null )
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
 *  Get the specified DataSet from this raw file.
 * 
 *  @param  data_set_num  The number of the DataSet in this raw file 
 *                        that is to be read from the raw file.  data_set_num
 *                        must be between 0 and numDataSets()-1
 *
 *  @return the requested DataSet, if data_set_num specifies a valid DataSet,
 *          .
 */
  public DataSet getDataSet( int data_set_num )
  {
    if ( raw_file == null )
      return null;

    if ( data_set_num < 0 || data_set_num >= num_data_sets )
      return null;

//  int ids[] = getAvailableIDs( data_set_num );
    int ids[] = IntList.ToArray( "2:45057" );

    return getDataSet( data_set_num, instrument_type, ids );
  }


/**
 *  Get a DataSet from the raw file containing only the the specified group 
 *  IDs from within the specified DataSet.  
 *  NOTE: The list of group IDs must be in increasing order.
 * 
 *  @param  data_set_num  The number of the DataSet in this raw file 
 *                        that is to be read from the raw file.  data_set_num
 *                        must be between 0 and numDataSets()-1
 *
 *  @param  ids           The list of group IDs from the specified DataSet
 *                        that are to be read from the raw file and returned 
 *                        in the DataSet, in increasing order.
 *
 *  @return a DataSet containing only the specified groups, if the data_set_num
 *          and ID list specify a non-empty set of Data blocks, or null
 *          otherwise.
 */
  public DataSet getDataSet( int data_set_num, int ids[] )
  {
    if ( raw_file == null )
      return null;

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
 *  @param  data_set_num  The number of the DataSet in this raw file.
 *
 *  @return An array of IDs that are available in this DataSet, in increasing
 *          order.
 */
  public int[] getAvailableIDs( int data_set_num )
  {
    if ( data_set_num < 0 || data_set_num >= num_data_sets )
      return new int[0];

    int histogram_num = histogram[ data_set_num ];
    int first_id,
        last_id;

    if ( data_set_type[ data_set_num ] == HISTOGRAM_DATA_SET )
    {
      first_id = raw_file.MinSubgroupID( histogram_num );
      last_id  = raw_file.MaxSubgroupID( histogram_num );
//    first_id = 2;
//    last_id  = 45057;
    }
    else if ( data_set_type[ data_set_num ] == MONITOR_DATA_SET )
    { 
      first_id = raw_file.MinMonitorID(); 
      last_id  = raw_file.MaxMonitorID(); 
//    first_id = 45101;   // for SXDII
//    last_id  = 45104;   // for SXDII
    } 
    else
      return new int[0]; 

    int ids[] = new int[ last_id - first_id + 1 ];
    int id = first_id;
    int n_used = 0;
    for ( int i = 0; i < ids.length; i++ )
    {
      if ( raw_file.TimeFieldType(id) >= 0 )  // ### returns -1
      {                                       // ### if id bad
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
 *  Get the specified DataSet from this raw file, based on the type of the
 *  data in the raw file
 *
 *  @param  data_set_num     The number of the DataSet in this raw file
 *                           that is to be read from the raw file.  data_set_num
 *                           must be between 0 and numDataSets()-1
 *
 *  @param  instrument_type  Specifies the type of instrument for this raw file.
 *                           Currently only:
 *                               InstrumentType.TOF_DIFFRACTOMETER
 *                               InstrumentType.TOF_DG_SPECTROMETER
 *                           are supported.
 *  @param  ids              The list of segment IDs from the specified DataSet
 *                           that are to be read from the raw file and returned 
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
    int               group_id = -2;
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

    System.out.println("Start of getDataSet " + IntList.ToString(ids) );

    if ( raw_file == null )
      return null;

    histogram_num = histogram[ data_set_num ];

    title = InstrumentType.getBaseFileName( data_source_name );
    if ( getType( data_set_num ) == MONITOR_DATA_SET )
    {
      is_monitor = true;
      title = "M" + histogram_num + "_" + title;
      ds_type = Attribute.MONITOR_DATA;
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
     if ( is_monitor || is_pulse_height )
       data_set = ds_factory.getDataSet();  // just generic operations

     else                                   // get data_set with ops for the
                                            // current instrument
       data_set = ds_factory.getTofDataSet( instrument_type );

                                            // Add some special operators
                                            // for monitors 
     if ( is_monitor )
       DataSetFactory.addMonitorOperators( data_set, instrument_type );

     data_set.addLog_entry( "Loaded " + title );
     AddDataSetAttributes( data_source_name, ds_type, data_set );

     raw_file.LeaveOpen();

     int last_tf_type = Integer.MAX_VALUE;  // keep track of the previous time
     int tf_type;                           // type so we only create new 
                                            // XScales when needed.  

     first_id = raw_file.MinSubgroupID( histogram_num );
     last_id  = raw_file.MaxSubgroupID( histogram_num );
     last_id  = 45104; // ##### fix this...
     System.out.println("Starting loop from " + first_id + " to " + last_id ); 
     for ( group_id = first_id; group_id <= last_id; group_id++ )
     {
      if ( Arrays.binarySearch( ids, group_id ) >= 0 )// skip if not in the list
      {
       if ( is_monitor    &&   raw_file.IsSubgroupBeamMonitor(group_id) ||
            is_histogram  &&  !raw_file.IsSubgroupBeamMonitor(group_id) )
       {
         tf_type = raw_file.TimeFieldType(group_id);
         if ( tf_type != last_tf_type )      // only get the times if it's a
                                             // new time field type
         {
           System.out.println("group id, tft, hist = " + group_id + 
                              ", " + tf_type + 
                              ", " + histogram_num );
           bin_boundaries = raw_file.TimeChannelBoundaries(group_id);
           num_times      = bin_boundaries.length;
           System.out.println("num_times = " + num_times );
           last_tf_type   = tf_type;

           x_scale = XScale.getInstance( bin_boundaries );
         }

         if ( num_times > 1 )
         {
          raw_spectrum = raw_file.Get1DSpectrum( group_id );
          if ( raw_spectrum.length >= 1 )
          {
            spectrum = Data.getInstance( x_scale, raw_spectrum, group_id );
            spectrum.setSqrtErrors( true );

            // Add the relevant attributes ----------------------------------
            AddSpectrumAttributes( instrument_type,
                                   histogram_num,
                                   group_id,
                                   spectrum      );
            data_set.addData_entry( spectrum );
          }
         }
       }
      }
    }
                           // now add references to the Data blocks to each
                           // of the Data grids used.                 
    UniformGrid.setDataEntriesInAllGrids( data_set );

    raw_file.Close();

    }
    catch( Exception e )
    {
      System.out.println("Exception in IsisRetriever.getDataSet()" );
      System.out.println("group_id = " + group_id );
      System.out.println("Exception is " +  e );
      e.printStackTrace();
    }

                                     // make effective positions match grid 
    ids = Grid_util.getAreaGridIDs( data_set );
    for ( int i = 0; i < ids.length; i++ )
    {
      int det_id = ids[i];
      Grid_util.setEffectivePositions( data_set, det_id );
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
    String title = raw_file.RunTitle();
    title = title.trim();
    str_attr = new StringAttribute( Attribute.RUN_TITLE, title );
    attr_list.setAttribute( str_attr );

    // Run Number ...........
    AddShared_RunNumber( attr_list );

    // End Date  ........
    str_attr = new StringAttribute( Attribute.END_DATE, raw_file.EndDate() );
    attr_list.setAttribute( str_attr );

    // End Time  ........
    str_attr = new StringAttribute( Attribute.END_TIME, raw_file.EndTime() );
    attr_list.setAttribute( str_attr );

    // End Time  ........
    str_attr = new StringAttribute( Attribute.END_TIME, raw_file.EndTime() );
    attr_list.setAttribute( str_attr );

    // Number of Pulses  ........
    AddShared_NumberOfPulses( attr_list );

    // SCD sample orientation, Sample Chi, Sample Phi, Sample Omega
    if ( instrument_type == InstrumentType.TOF_SCD )
      AddShared_SCD_SampleOrientation( attr_list );

    // User Name
    str_attr = new StringAttribute( Attribute.USER, raw_file.UserName() );
    attr_list.setAttribute( str_attr );

    attr_list.trimToSize();
    ds.setAttributeList( attr_list );
  }

/**
 *  Add the Spectrum attributes to the specified Data block.
 *
 *  @param  instrument_type  The file name for this DataSet
 *  @param  histogram_num    The histogram number for this group
 *  @param  group_id         The group_id for this group
 *  @param  spectrum         The Data block to which the attributes are added  
 *
 */
  private void AddSpectrumAttributes( int     instrument_type,
                                      int     histogram_num,
                                      int     group_id,
                                      Data    spectrum )
  {
    FloatAttribute    float_attr;
    AttributeList     attr_list = spectrum.getAttributeList();

    // Run Number .......
    // Initial Path ......
    // Total Counts ......
    if ( add_run_attrs )
    {
      AddShared_RunNumber( attr_list );
      AddShared_InitialPath( attr_list );
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
        System.out.println(
                        "Exception in RunfileRetriever.AddSpectrumAttributes");
        System.out.println("Exception is " + e );
        e.printStackTrace();
      }
    }

    // Pixel Info List.....
    if ( add_vis_attrs )
      Add_PixelInfo( attr_list, group_id );

    // Number of pulses......
    // SCD sample orientation....
    // SCD Detector Center & Distance .....
    if ( add_scd_attrs )
    {
      AddShared_NumberOfPulses( attr_list );
      if ( instrument_type == InstrumentType.TOF_SCD )
         AddShared_SCD_SampleOrientation( attr_list );
    }

    // Detector and Segment ID lists ..........
    // Crate, Slot and Input
    if ( add_diagnostic_attrs )
    {
//      Add_CrateSlotInput(  attr_list, group_segments, raw_file );
      Add_DetAndSegID( attr_list, group_id );
    }

    attr_list.trimToSize();
    spectrum.setAttributeList( attr_list );
  }


  /**
   *  Add "shared" number of pulses attribute to the specified Attribute list
   *
   *  @ param   attr_list       The attribute list to which the
   *                            attribute is added
   */
  private void AddShared_NumberOfPulses( AttributeList attr_list )
  {
    if ( num_pulses_attr == null )
      num_pulses_attr =new FloatAttribute(Attribute.NUMBER_OF_PULSES,
                                          (float)raw_file.NumOfPulses());
    attr_list.setAttribute( num_pulses_attr );
  }


  /**
   *  Add "shared" initial path attribute to the specified Attribute list
   *
   *  @ param   attr_list       The attribute list to which the
   *                            attribute is added
   */
  private void AddShared_InitialPath( AttributeList attr_list )
  {
    if ( initial_path_attr == null )
      initial_path_attr =new FloatAttribute(Attribute.INITIAL_PATH,
                                           (float)raw_file.SourceToSample());
    attr_list.setAttribute( initial_path_attr );
  }


  /**
   *  Add "shared" run number attribute to the specified Attribute list
   *
   *  @ param   attr_list       The attribute list to which the
   *                            attribute is added
   */
  private void AddShared_RunNumber( AttributeList attr_list )
  {
    if ( run_num_attr == null )
    {
      int list[] = new int[1];
      list[0] =  raw_file.RunNumber();
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
  private void AddShared_SCD_SampleOrientation( AttributeList attr_list )
  {
    if ( scd_sample_orientation_attr == null )
    {
      SampleOrientation so = new IPNS_SCD_SampleOrientation(
                                      (float)raw_file.Phi(),
                                      (float)raw_file.Chi(),
                                      (float)raw_file.Omega() );
      scd_sample_orientation_attr = new SampleOrientationAttribute(
                                        Attribute.SAMPLE_ORIENTATION, so );
    }
    attr_list.setAttribute( scd_sample_orientation_attr );
  }


/**
 *  Set up grid info using the specifed source for the information.
 */
  private void SetUpGridInfo( IInstrument_Grid_Info info )
  {
    grids         = new IDataGrid[ info.numGrids() ]; 
    first_spec_id = new int [ info.numGrids() ]; 

    for ( int i = 0; i < grids.length; i++ )
    {
      grids[i]         = info.getGridAtIndex( i );
      first_spec_id[i] = info.getFirstSpectrumID(i); 
    }
  }

  /**
   *  Add the detector ID and segment ID for an ISIS file.
   *
   *  @ param   attr_list       The attribute list to which the
   *                            attribute is added
   *  @ param   group_id        The detector segment ids for this group
   */
  private void Add_DetAndSegID( AttributeList attr_list, int group_id )
  {
    int det_ids[] = new int[ 1 ];
    int seg_ids[] = new int[ 1 ];
//    det_ids[0] = DetID( group_id );
    det_ids[0] = (group_id - 2) / 4096;   // ##### for SXD only
    seg_ids[0] = group_id;

    Attribute int_list_attr = new IntListAttribute( Attribute.DETECTOR_IDS,
                                          det_ids );
    attr_list.setAttribute( int_list_attr );

    int_list_attr = new IntListAttribute( Attribute.SEGMENT_IDS, seg_ids );
    attr_list.setAttribute( int_list_attr );
  }

  /**
   *  Add the pixel info list attributes, sharing a data grid object
   *  if possible. 
   *
   *  @ param   attr_list   The attribute list to which the attribute is added
   *  @ param   group_id    The id of the detector segment
   */
  private void Add_PixelInfo( AttributeList attr_list, int group_id )
  {
    short      row;
    short      col;
    int        det_id;
    IDataGrid  data_grid;

    if ( grids == null || first_spec_id == null )
    {
      System.out.println("No Grid info available for group " + group_id );
      return;
    }

    if ( raw_file.IsSubgroupBeamMonitor( group_id ) )
    {
      System.out.println("No Grid info available for MONITOR " + group_id );
      return;
    }

//    det_id = raw_file.DetID( group_id );
    det_id = (group_id - 2) / 4096 + 1;              // SXD ONLY ########
    int index = det_id - 1;

    data_grid = grids[ index ];

    int offset = group_id - first_spec_id[index]; 
    row = (short)( 1 + offset / data_grid.num_cols() );
    col = (short)( 1 + offset % data_grid.num_cols() );

    IPixelInfo  list[] = new IPixelInfo[1];
    list[0] = new DetectorPixelInfo( group_id, row, col, data_grid );

    PixelInfoList pil = new PixelInfoList(list);
    attr_list.setAttribute(
                new PixelInfoListAttribute(Attribute.PIXEL_INFO_LIST, pil) );
  }


/**
 *  Main program for testing purposes
 *
 *  @param args  First argment should be the fully qualified file name of
 *               of an ISIS file to load.
 */
  public static void main( String args[] )
  {
    String file_name;
    if ( args.length < 1 )
      file_name = "/home/dennis/ISIS_SXD/SXD17210.RAW";
    else
      file_name = args[0];

    System.out.println("Loading ISIS file: " + file_name );

    IsisRetriever rr = new IsisRetriever( file_name );
    System.out.println("Num DataSets = " + rr.numDataSets() );
    for ( int i = 0; i < rr.numDataSets(); i++ )
    {
      System.out.println("Type = " + rr.getType(i) );
      System.out.println("Available IDs : " +  
                          IntList.ToString( rr.getAvailableIDs(i) ) );  
    }
/*
   DataSet mon_ds = rr.getDataSet(0);
   System.out.println("Number in Monitor DataSet = " + mon_ds.getNum_entries());
   new ViewManager( mon_ds, IViewManager.IMAGE );
*/

   int ids[] = IntList.ToArray( "2:28672" );
//   int ids[] = IntList.ToArray( "2:45057" );   // #####

   DataSet hist_ds = rr.getDataSet( 1, ids );  // #####
//   DataSet hist_ds = rr.getDataSet( 1 );
//   DataSet hist_ds = rr.getFirstDataSet( Retriever.HISTOGRAM_DATA_SET );

   System.out.println("Number in Histogram DataSet: "+hist_ds.getNum_entries());
   new ViewManager( hist_ds, IViewManager.IMAGE );

   System.out.println("Done with main");
  }

}

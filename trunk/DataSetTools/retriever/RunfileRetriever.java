package DataSetTools.retriever;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.*;
import IPNS.Runfile.*;
import DataSetTools.math.*;
import java.io.*;

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

  public RunfileRetriever(String data_source_name) 
  {
    super(data_source_name);

    int       first_id, 
              last_id;
    int       num_histograms;
    boolean   has_monitors;
    boolean   has_detectors;

    try
    {
      run_file         = new Runfile( data_source_name );
      num_histograms   = run_file.NumOfHistograms();
      data_set_type    = new int[ 2 * num_histograms ];
      histogram        = new int[ 2 * num_histograms ];
      num_data_sets    = 0;
      for ( int hist = 1; hist <= num_histograms; hist++ )
      {
        if ( run_file.IsHistogramGrouped( hist ) )
        {
          first_id = run_file.MinSubgroupID( hist );
          last_id  = run_file.MaxSubgroupID( hist );
          has_monitors = false;
          has_detectors = false;
          for ( int group_id = first_id; group_id < last_id; group_id++ )
          {
            if ( run_file.IsSubgroupBeamMonitor(group_id) )
              has_monitors = true;
            else
              has_detectors = true;
          }

          if ( has_monitors )
          { 
            data_set_type[ num_data_sets ] = MONITOR_DATA_SET;
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
    catch( Exception e ) { System.out.println( e ); }
  }

  public int numDataSets()
  { 
    return num_data_sets;
  }

  public int getType( int data_set_num )
  {
    if ( data_set_num >= 0 && data_set_num < num_data_sets )
      return data_set_type[ data_set_num ];
    else
      return INVALID_DATA_SET;
  }

  public int getHistogramNum( int data_set_num )
  {
    if ( data_set_num >= 0 && data_set_num < num_data_sets )
      return histogram[ data_set_num ];
    else
      return INVALID_DATA_SET;
  }

  public DataSet getDataSet( int data_set_num )
  {
    System.out.println("======= getting dataset for >>>" + data_source_name );
    int instrument_type;

    if ( data_set_num >= num_data_sets )
      return null;

    instrument_type = InstrumentType.getIPNSInstrumentType( data_source_name );
    return getDataSet( data_set_num, instrument_type );
  }

  private DataSet getDataSet( int data_set_num, int instrument_type )
  {
    int               num_times;
    XScale            x_scale;
    float[]           raw_spectrum;
    int               group_id;
    int               det_id;
    Data              spectrum; 
    int               histogram_num;
    boolean           monitor;
    int               first_id, last_id;
    float[]           bin_boundaries;
    float             source_to_sample_tof;
    DataSet           data_set = null;
    String            title;

    histogram_num = histogram[ data_set_num ];  

    title = InstrumentType.getBaseFileName( data_source_name );
    if ( getType( data_set_num ) == MONITOR_DATA_SET )
      {
        monitor = true;
        title = "M" + histogram_num + " " + title; 
      }
    else
      {
        monitor = false;    
        title = "H" + histogram_num + " " + title; 
      }
 
    try
    {
     first_id = run_file.MinSubgroupID( histogram_num );
     last_id  = run_file.MaxSubgroupID( histogram_num );

     DataSetFactory ds_factory;
     if ( monitor )
       ds_factory = new DataSetFactory( title, 
                                       "Time(us)",
                                       "Time-of-flight",
                                       "Counts",
                                       "Scattering Intensity" );
     else if ( instrument_type == InstrumentType.TOF_DIFFRACTOMETER )
       ds_factory = new DiffractometerTofDataSetFactory( title );
     else if ( instrument_type == InstrumentType.TOF_DG_SPECTROMETER )
       ds_factory = new SpectrometerTofDataSetFactory( title );
     else
       ds_factory = new DataSetFactory( title );

     data_set   = ds_factory.getDataSet();
     data_set.addLog_entry( "Loaded " + title );
     AddDataSetAttributes( data_source_name, data_set );

     run_file.LeaveOpen();
     for ( group_id = first_id; group_id <= last_id; group_id++ )
     {
      int[] group_members = run_file.IdsInSubgroup( group_id );

      if ( group_members.length > 0 )   // only deal with non-trivial groups
      if ( monitor && run_file.IsSubgroupBeamMonitor(group_id) ||
           !monitor && !run_file.IsSubgroupBeamMonitor(group_id) )
      {
        det_id = group_members[0];    // representative det_id;

        bin_boundaries = run_file.TimeChannelBoundaries(det_id, histogram_num);
        num_times = bin_boundaries.length;

        if ( num_times > 1 )
        {
          // raw_spectrum = run_file.Get1DSpectrum( det_id, histogram_num );
           raw_spectrum = run_file.Get1DSpectrum( group_id );

          if ( raw_spectrum.length >= 1 )
          {
            // change times to sample to detector TOF for spectrometers 
            // and groups that are NOT beam monitors ------------------------ 
            source_to_sample_tof = (float)run_file.SourceToSampleTime();
            if (instrument_type==InstrumentType.TOF_DG_SPECTROMETER && !monitor)
              for ( int i = 0; i < bin_boundaries.length; i++ )
                 bin_boundaries[i] -= source_to_sample_tof;

            x_scale = new VariableXScale( bin_boundaries );
            spectrum = new Data( x_scale, raw_spectrum, group_id );

            // Add the relevant attributes ----------------------------------
            AddSpectrumAttributes( instrument_type,
                                   histogram_num,
                                   group_id,
                                   group_members, 
                                   spectrum      );
 
            // Now add the spectrum to the DataSet -------------------------
            data_set.addData_entry( spectrum );
          }
        }
      }
    }

    run_file.Close(); 

    } catch( Exception e )
      {
         System.out.println( e );
      }

    return data_set;
  }



  private void AddDataSetAttributes( String   file_name,
                                     DataSet  ds        )
  {
    IntAttribute      int_attr;
    StringAttribute   str_attr;
    IntListAttribute  int_list_attr;
    AttributeList     attr_list = ds.getAttributeList();

    // Instrument Name ........
    str_attr = new StringAttribute( Attribute.INST_NAME, 
                         InstrumentType.getIPNSInstrumentName( file_name ) );
    attr_list.setAttribute( str_attr );

    // Instrument Type ........
    int list[] = new int[1];
    list[0] = InstrumentType.getIPNSInstrumentType( file_name );
    int_list_attr = new IntListAttribute( Attribute.INST_TYPE, list );
    attr_list.setAttribute( int_list_attr );

    // Run Title ........
    str_attr = new StringAttribute( Attribute.RUN_TITLE, run_file.RunTitle() );
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
    int_attr = new IntAttribute( Attribute.NUMBER_OF_PULSES, run_file.NumOfPulses() );
    attr_list.setAttribute( int_attr );
    

    ds.setAttributeList( attr_list );
  }




  private void AddSpectrumAttributes( int     instrument_type,
                                      int     histogram_num,
                                      int     group_id,
                                      int     group_members[], 
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
    angle      = (float)run_file.DetectorAngle(group_members[0], histogram_num);
    angle      *= (float)(Math.PI / 180.0);
    height     = getAverageHeight( group_members );
    final_path = (float)run_file.FlightPath( group_members[0], histogram_num );
    float r    = (float)Math.sqrt( final_path * final_path - height * height );
    position.setCylindricalCoords( r, angle, height );

    pos_attr = new DetPosAttribute( Attribute.DETECTOR_POS, position );
    attr_list.setAttribute( pos_attr );

    // Raw Detector Angle ...........
    float_attr =new FloatAttribute(Attribute.RAW_ANGLE,
                      (float)run_file.RawDetectorAngle( group_members[0]) );
    attr_list.setAttribute( float_attr );
    
    // Total Counts  ........
    int_attr = new IntAttribute( Attribute.TOTAL_COUNTS, 5555555555 );
    attr_list.setAttribute( int_attr );


    spectrum.setAttributeList( attr_list );
  }



  // ##### could be put into class Runfile
  private float getAverageHeight( int ids[] )
  {
    float total = 0;
    try
    {
    for ( int i = 0; i < ids.length; i++ )
      total += run_file.DetectorHeight( ids[i] );
    }
    catch ( Exception e )
    {
      System.out.println( e );
    }

    return total / ids.length;   
  }


  public static void main(String[] args)
  {
    System.out.println("No test program"); 
  }

}

/*
 * @(#)NexusRetriever.java
 *
 * Programmer: Dennis Mikkelson
 *
 * $Log$
 * Revision 1.1  2001/03/28 22:28:01  dennis
 * First attempt at retriever for NeXus data
 *
 *
 */
package DataSetTools.retriever;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.InstrumentType;
import DataSetTools.nexus.*;
import neutron.nexus.*;
import java.util.Hashtable;
import java.util.Enumeration;
import DataSetTools.math.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;
import java.io.*;

/**
 *  This class opens a Nexus file and extracts DataSets.  If the instrument
 *  type can not be handled properly, only one DataSet will be returned 
 *  and it will only include the generic operators.
 */
public class NexusRetriever extends    Retriever 
                            implements Serializable
{
  boolean file_exists     = false;
  int     instrument_type = 0;
  int     num_data_sets;    // the number of distinct DataSets in this file 
  int[]   data_set_type;    // the type of a specific DataSet number in this
                            // runfile.

/**
 *  Construct a nexus retriever for a specific file.
 *
 *  @param data_source_name  The fully qualified file name for the runfile.
 */
  public NexusRetriever(String data_source_name) 
  {
    super(data_source_name);

    try 
    {
      NexusFile nexus_file = new NexusFile( data_source_name, 
                                            NexusFile.NXACC_READ );
      instrument_type = InstrumentType.UNKNOWN;
      num_data_sets = 1;
      data_set_type = new int[1];
      data_set_type[0] = HISTOGRAM_DATA_SET;
                                               // temporarily get attributes 
                                               // since the finalize hangs
                                               // if nothing is done before
                                               // finalizing.
      nexus_file = null;
      pause( 500 );
      System.gc();
      pause( 500 );
      file_exists = true;
    }
    catch ( Throwable e )
    {
      System.out.println("EXCEPTION in NexusRetriever constructor" + e);
      file_exists = false;
      num_data_sets = 0;
    }

    if ( file_exists )                        // now see if it's a type we
    {                                         // know how to handle
      NexusFile nexus_file = null; 
      try
      {
        nexus_file = new NexusFile( data_source_name, NexusFile.NXACC_READ );
        nexus_file.opengroup( "Histogram1", "NXentry" );
        String analysis = NexusUtils.getString( nexus_file, "analysis" );
        if ( analysis.equalsIgnoreCase( "TOFNDGS" ) )
        {
          instrument_type = InstrumentType.TOF_DG_SPECTROMETER;
          num_data_sets = 2;
          data_set_type = new int[2];
          data_set_type[0] = MONITOR_DATA_SET;
          data_set_type[1] = HISTOGRAM_DATA_SET;
        }
      }
      catch ( Throwable e )
      {
        // no Histogram1 or analysis entries, so it's not a spectrometer
      }
      nexus_file = null;
      pause( 500 );
      System.gc();
      pause( 500 );
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
    if ( !file_exists )
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
    if ( !file_exists )
       return INVALID_DATA_SET;

    if ( data_set_num >= 0 && data_set_num < num_data_sets )
      return data_set_type[ data_set_num ];
    else
      return INVALID_DATA_SET;
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
    System.out.println("getDataSet # " + data_set_num );

    if ( !file_exists )
      return null;

    if ( data_set_num < 0 || data_set_num >= num_data_sets )
      return null;

    
    DataSet ds = null;

    if ( instrument_type == InstrumentType.TOF_DG_SPECTROMETER )  
    {
      if ( data_set_num == 0 )
        ds = getSpectrometerMonitors();
      else
        ds = getSpectrometerHistogram();
    }
    else
      ds = getGenericDataSet();

    return ds;
  }


  private Data getMonitorData( NexusFile nexus_file, int id )
  {
     float y_vals[] = NexusUtils.getFloatArray1( nexus_file, "data" );
     float x_vals[] = NexusUtils.getFloatArray1( nexus_file, "time_of_flight");
  
     if ( x_vals == null || y_vals == null )
       return null;
 
     XScale x_scale = new VariableXScale( x_vals );
     Data   d       = new Data( x_scale, y_vals, id );
      
     float dist[]   = NexusUtils.getFloatArray1( nexus_file, "distance");
     float m_dist[] = NexusUtils.getFloatArray1( nexus_file, 
                                                "moderator_distance");

     if ( dist == null && m_dist == null )
       return d;

     AttributeList attr_list = new AttributeList();
     if ( dist != null )
       attr_list.addAttribute( new FloatAttribute( "distance", dist[0] ));

     if ( m_dist != null )
       attr_list.addAttribute( new FloatAttribute( "moderator_distance", 
                                                     m_dist[0]          ));
     if ( dist != null )
     {
       DetectorPosition position = new DetectorPosition();
       position.setCartesianCoords( dist[0], 0, 0 );
       attr_list.addAttribute( new DetPosAttribute( Attribute.DETECTOR_POS, 
                                                    position               ));
     }

     d.setAttributeList( attr_list );

     return d;
  }


  private AttributeList getBasicSpectrmeterAttributes( NexusFile nexus_file )
  {
    AttributeList attr_list = new AttributeList();

    try
    {
      nexus_file.opengroup( "sample", "NXsample" );
        float mod_dist[] = NexusUtils.getFloatArray1( nexus_file,
                                                      "moderator_distance" );
        Attribute initial_path = new FloatAttribute( Attribute.INITIAL_PATH,
                                                     mod_dist[0] );
        attr_list.setAttribute( initial_path );
      nexus_file.closegroup();

      nexus_file.opengroup( "LRMECS", "NXinstrument" );
        nexus_file.opengroup( "source", "NXsource" );
          float pulses[]=NexusUtils.getFloatArray1( nexus_file,"proton_pulses");
          Attribute n_pulses = new IntAttribute( Attribute.NUMBER_OF_PULSES,
                                                (int)pulses[0] );
          attr_list.setAttribute( n_pulses );
        nexus_file.closegroup();

        nexus_file.opengroup( "monochromator", "NXchopper" );
          float nom_e_in[] = NexusUtils.getFloatArray1( nexus_file,
                                                       "incident_energy");
          Attribute n_e_in = new FloatAttribute( Attribute.NOMINAL_ENERGY_IN,
                                                nom_e_in[0] );
          attr_list.setAttribute( n_e_in );
          Attribute e_in=new FloatAttribute( Attribute.ENERGY_IN, nom_e_in[0] );
          attr_list.setAttribute( e_in );
        nexus_file.closegroup();
      nexus_file.closegroup();
    }
    catch ( Throwable e )
    {
      System.out.println(
               "EXCEPTION in NexusRetriever.getBasicSpectrometerAttributes() "
               + e);
    }

    return attr_list;
  }


  private Attribute[] getSpectrometerDetPositions( NexusFile nexus_file )
  {
    Attribute        list[] = new Attribute[0];
    DetectorPosition position;
    Attribute        attr;

    try
    {
      nexus_file.opengroup( "LRMECS", "NXinstrument" );
        nexus_file.opengroup( "detector_bank", "NXdetector" );
          float phi[]  = NexusUtils.getFloatArray1( nexus_file,"phi");
          float dist[] = NexusUtils.getFloatArray1( nexus_file,"distance");
          float z      = 0;
          list = new Attribute[ phi.length ];

          for ( int i = 0; i < phi.length; i++ )
          {
            phi[i] = (float)(phi[i] * Math.PI / 180);    // convert to radians!
            position = new DetectorPosition();
            position.setCylindricalCoords( dist[i], phi[i], z );
            attr = new DetPosAttribute( Attribute.DETECTOR_POS, position );
            list[i] = attr;
          }
        nexus_file.closegroup();
      nexus_file.closegroup();
    }
    catch ( Throwable e )
    {
      System.out.println(
                "EXCEPTION in NexusRetriever.getSpectrometerDetPositions() "
               + e);
    }

    return list;
  }



 private DataSet getSpectrometerMonitors()
 {
    NexusFile nexus_file = null;
    DataSet   ds         = null;

    System.out.println("getMonitorData for " + data_source_name );
    try
    {
      nexus_file = new NexusFile( data_source_name, NexusFile.NXACC_READ );

      AttributeList global_attr_list = NexusUtils.getAttributes( nexus_file );
      nexus_file.opengroup( "Histogram1", "NXentry" );

      String title = NexusUtils.getString( nexus_file, "title" );
      if ( title == null )
        title = "M1_Monitors";
      else
        title = "M1_Monitors: " + title;

      DataSetFactory ds_factory = new DataSetFactory( title );
      ds = ds_factory.getDataSet();
      ds.setAttributeList( global_attr_list );
      ds.addOperator( new EnergyFromMonitorDS() );
      ds.addOperator( new MonitorPeakArea() );
                                                         // add monitor data
      nexus_file.opengroup( "monitor1", "NXmonitor" );
      Data d = getMonitorData( nexus_file, 1 );
      ds.addData_entry( d );
      nexus_file.closegroup();

      nexus_file.opengroup( "monitor2", "NXmonitor" );
      d = getMonitorData( nexus_file, 2 );
      ds.addData_entry( d );
      nexus_file.closegroup();
                                                        // add more attributes

      AttributeList basic_attrs = getBasicSpectrmeterAttributes( nexus_file );
      for ( int i = 0; i < ds.getNum_entries(); i++ )
      {
        d = ds.getData_entry( i );
        for ( int k = 0; k < basic_attrs.getNum_attributes(); k++ )
          d.setAttribute( basic_attrs.getAttribute(k) );
      } 
    }
    catch ( Throwable e )
    {
      System.out.println(
               "EXCEPTION in NexusRetriever.getSpectrometerMonitors() "+ e); 
      ds = null;
    }

    nexus_file = null;                               // close file and return
    pause( 500 );
    System.gc();
    pause( 500 );
    return ds;
 }


 private DataSet getSpectrometerHistogram()
 {
    NexusFile nexus_file = null;
    DataSet   ds         = null;

    System.out.println("getSpectrometerData for " + data_source_name );
    try
    {
      nexus_file = new NexusFile( data_source_name, NexusFile.NXACC_READ );

      AttributeList global_attr_list = NexusUtils.getAttributes( nexus_file );
      nexus_file.opengroup( "Histogram1", "NXentry" );

      String title = NexusUtils.getString( nexus_file, "title" );
      if ( title == null )
        title = "H1_Untitled";
      else
        title = "H1_" + title;

      AttributeList basic_attrs = getBasicSpectrmeterAttributes( nexus_file );
      Attribute det_positions[] = getSpectrometerDetPositions( nexus_file );

      DataSetFactory ds_factory = new DataSetFactory( title );
      ds = ds_factory.getTofDataSet( instrument_type );
      ds.setAttributeList( global_attr_list );

      nexus_file.opengroup( "data", "NXdata" );

      float x_vals[] = NexusUtils.getFloatArray1( nexus_file, "time_of_flight");
      float y_vals[][] = NexusUtils.getFloatArray2( nexus_file, "data" );
      if ( x_vals == null || y_vals == null )
        return null;

      FloatAttribute l0_attr = (FloatAttribute)basic_attrs.getAttribute( 
                                                Attribute.INITIAL_PATH );
      FloatAttribute e0_attr = (FloatAttribute)basic_attrs.getAttribute(
                                                Attribute.ENERGY_IN );
/*
                                            // attempt to adjust times to the
                                            // sample to detector times
      float l0 = l0_attr.getFloatValue();
      float e0 = e0_attr.getFloatValue();
  //    e0 = 11.1469f;                      // calculated from monitor data
                                            // for lrcs3000
      System.out.println( "l0, e0 = " + l0 + ", " + e0 );
      float source_to_sample_tof = tof_calc.TOFofEnergy( l0, e0 );
      System.out.println( "source_to_sample_tof = " + source_to_sample_tof );
      for ( int i = 0; i < x_vals.length; i++ )
        x_vals[i] -= source_to_sample_tof;
*/
      XScale x_scale = new VariableXScale( x_vals );
      for ( int row = 0; row < y_vals.length; row++ )
      {
        Data   d       = new Data( x_scale, y_vals[row], row );
        d.setAttribute( det_positions[ row ] );
        for ( int k = 0; k < basic_attrs.getNum_attributes(); k++ )
          d.setAttribute( basic_attrs.getAttribute( k ) );

        ds.addData_entry( d );
      }
    }
    catch ( Throwable e )
    {
      System.out.println(
               "EXCEPTION in NexusRetriever.getSpectrometerHistogram() "+ e);
      ds = null;
    }

    nexus_file = null;                     // close file and return
    pause( 500 );
    System.gc();
    pause( 500 );
    return ds;
 }






 private DataSet getGenericDataSet()
 {
    NexusFile nexus_file = null;
    DataSet   ds         = null;
    String    title      = "Untitled";
    String    temp_title = null;

    System.out.println("getGenericData for " + data_source_name );
    try
    {
      nexus_file = new NexusFile( data_source_name, NexusFile.NXACC_READ ); 

      AttributeList global_attr_list = NexusUtils.getAttributes( nexus_file );

      String      first_entry = null;
      Hashtable   h     = nexus_file.groupdir();    // search for first NXentry
      Enumeration e     = h.keys();
      boolean     found = false;
      while( e.hasMoreElements() && !found )
      {
         String vname = (String)e.nextElement();
         String vclass = (String)h.get(vname);
         if ( vclass.equals( "NXentry" ) )
         {
           first_entry = vname;    
           found = true;
         }
      }

      if ( first_entry == null )                   // close file and return
      {
        nexus_file = null;
        pause( 500 );
        System.gc();
        pause( 500 );
        return null;
      }

      System.out.println("Found first NXentry as " + first_entry );
      nexus_file.opengroup( first_entry, "NXentry" );
      temp_title = NexusUtils.getString( nexus_file, "title" );
      if ( temp_title != null )
        title = temp_title;

                                                    // search for first NXdata
      String  data_group = null;                
      h     = nexus_file.groupdir(); 
      e     = h.keys();
      found = false;
      while( e.hasMoreElements() && !found )
      {
         String g_name = (String)e.nextElement();
         String g_class = (String)h.get(g_name);
         if ( g_class.equals( "NXdata" ) )
         {
           data_group = g_name;
           found = true;
         }
      }
      

      System.out.println("Found first NXdata as " + data_group );
      nexus_file.opengroup( data_group, "NXdata" );
      temp_title = NexusUtils.getString( nexus_file, "title" );
      if ( temp_title != null )
        title = temp_title;
                                                      // search for SDS with
      String      signal_data = null;                 // signal == 1 
      h     = nexus_file.groupdir();  
      e     = h.keys();
      found = false;
      while( e.hasMoreElements() && !found )
      {
         String d_name = (String)e.nextElement();
         String d_class = (String)h.get(d_name);
         if ( d_class.equals( "SDS" ) )
         {
           nexus_file.opendata( d_name );
           AttributeList attr_list = NexusUtils.getAttributes( nexus_file );
           Attribute attr = attr_list.getAttribute( "signal" );
           attr = attr_list.getAttribute( "signal" );

     //    Checking the value fails since some files don't use int for signal
     //    if ( attr != null && ((Integer)attr.getValue()).intValue() == 1 )
           if ( attr != null )
           {
             signal_data = d_name;
             found = true;
           }
         }
      }

     float my_data[][] = null;
     System.out.println("Found signal on " + signal_data );
     System.out.println("Data rank = " +NexusUtils.getDataRank(nexus_file) );  
     System.out.println("Data type = " +NexusUtils.getDataType(nexus_file));

     if ( NexusUtils.getDataRank(nexus_file) == 1 )
     {
       int data_type = NexusUtils.getDataType(nexus_file);
       if ( data_type == NexusFile.NX_FLOAT32 )
       {
         float array[] = (float[])NexusUtils.getData( nexus_file );
         my_data = new float[1][ array.length ];
         for ( int i = 0; i < array.length; i++ )
           my_data[0][i] = array[i];
       }

       else if ( data_type == NexusFile.NX_INT32   ||
                 data_type == NexusFile.NX_UINT32   )
       {
         int array[] = (int[])NexusUtils.getData( nexus_file );
         my_data = new float[1][ array.length ];
         for ( int i = 0; i < array.length; i++ )
           my_data[0][i] = array[i];
       }
     }

     else if ( NexusUtils.getDataRank(nexus_file) == 2 )
     {
       int data_type = NexusUtils.getDataType(nexus_file);
       if ( data_type == NexusFile.NX_FLOAT32 )
         my_data = (float[][])NexusUtils.getData( nexus_file );

       else if ( data_type == NexusFile.NX_INT32   ||
                 data_type == NexusFile.NX_UINT32   )
       {
         int array[][] = (int[][])NexusUtils.getData( nexus_file );
         my_data = new float[ array.length ][ array[0].length ];
         for ( int row = 0; row < array.length; row++ )
           for ( int col = 0; col < array[0].length; col++ )
             my_data[row][col] = array[row][col]; 
       }
     }

     if ( my_data != null )
     {
       DataSetFactory ds_factory = new DataSetFactory(title);
       ds = ds_factory.getDataSet();
       ds.setX_label( "Channel" );
       ds.setX_units( "bin #" );
       ds.setY_label( "Intensity" );
       ds.setY_units( "counts" );

       XScale x_scale = new UniformXScale( 0, 
                                           my_data[0].length, 
                                           my_data[0].length + 1 );

       for ( int row = 0; row < my_data.length; row++ )
         ds.addData_entry( new Data( x_scale, my_data[row], row ));

       ds.setAttributeList( global_attr_list );
       ds.addLog_entry( "Loaded " + data_source_name );
     }

     nexus_file.closedata();
     nexus_file.closegroup();
     nexus_file.closegroup();

    }
    catch ( Throwable e )
    {
      System.out.println("EXCEPTION in NexusRetriever.getGenericDataSet() "+ e);
    }

    nexus_file = null;                     // close file and return
    pause( 500 );
    System.gc();
    pause( 500 );
    return ds;
 }

public static void pause( int ms )
{
  try
  {
    Thread.sleep(ms);
  }
  catch ( Exception e )
  {
    System.out.println("EXCEPTION sleeping in pause");
  }
}

  public static void main(String[] args)
  {
    String path         = "/IPNShome/dennis/ISAW/NDS_DATA/";
    String file_names[] = { "amortest000161999.hdf",
                            "amortest000171999.hdf",
                            "dmc012201998.hdf",
                            "focus003101999.hdf",
                            "hrpt007101999.hdf",
                            "lrcs3000.nxs",
                            "sans030101999.hdf",
                            "trics00151999.hdf" };

    for ( int f_num = 0; f_num < file_names.length; f_num++ )
    {
      String file_name = path + file_names[ f_num ];
      NexusRetriever nr = new NexusRetriever( file_name );
      for ( int i = 0; i <  nr.numDataSets(); i++ )
      {
        DataSet ds = nr.getDataSet( i );
        if ( ds != null )
        {
          System.out.println("Showing DataSet : " + ds + " from " + file_name );
          ViewManager vm = new ViewManager( ds, ViewManager.IMAGE );
        }
        else
          System.out.println("Failed to get DataSet # " + i );
      }
      nr = null;
      pause( 500 );
      System.gc();
      pause( 500 );
    }


  }

}

/*
 * File: LiveDataServer.java
 *
 * Copyright (C) 2001, Ruth Mikkelson
 *                     Alok Chatterjee,
 *                     Dennis Mikkelson
 *                     John Hammonds
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
 *  Revision 1.34  2003/02/24 21:01:18  dennis
 *  Removed un-needed debug print.
 *
 *  Revision 1.33  2003/02/24 20:41:59  dennis
 *  Put debug print in 'if ( debug...' statement.
 *
 *  Revision 1.32  2003/02/24 20:37:05  dennis
 *  Now consistently replies to status requests by sending a String
 *  that begins with "Status:".
 *
 *  Revision 1.31  2003/02/24 13:39:19  dennis
 *  Switched to use CommandObject instead of compound command Strings.
 *
 *  Revision 1.30  2003/02/22 23:12:46  dennis
 *  Now only recalculates the compressed version of the DataSet when
 *  the DataSet is requested, in case either 1 minute has elapsed, or
 *  in case more than half of the spectra have been updated by the
 *  DAS.
 *
 *  Revision 1.29  2003/02/21 13:14:56  dennis
 *  Calls Reset() method before sending a DataSet, to avoid having to
 *  clone the DataSet first.
 *
 *  Revision 1.28  2002/11/30 22:18:52  dennis
 *  Now keeps a list of references to all Data blocks indexed by the
 *  segment id.  This allows immediately updating the correct Data block
 *  when a new UDP packet is received, without having to search through
 *  all DataSets to find the correct Data block.  This reduces the
 *  computational load on the server.
 *
 *  Revision 1.27  2002/11/29 22:50:36  dennis
 *  Now uses InstrumentType.formIPNSFileName() to form proper file name
 *  based on instrument name and run number from UDP packet.
 *
 *  Revision 1.26  2002/11/27 23:27:59  pfpeterson
 *  standardized header
 *
 *  Revision 1.25  2002/06/03 14:12:50  dennis
 *  The server now sets the error arrays to null.  This saves the time to
 *  transmit the error arrays.  Also, since the error arrays were set on
 *  the basis of the empty runfile, the error values would have been zero
 *  anyway.
 *
 *  Revision 1.24  2001/12/10 22:05:24  dennis
 *  Now will pad run numbers with leading zeros, to length 4 when
 *  forming the file name string.
 *
 */
package NetComm;

import java.lang.*;
import java.net.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;
import DataSetTools.instruments.*;

/**
 *  LiveDataServer that receives UDP packets containing spectra from a DAS,
 *  forms a DataSet and sends the DataSet as an Object via TCP to client 
 *  programs.
 *
 *  @see DataSetTools.retriever.LiveDataRetriever
 *  @see IUDPUser
 *  @see UDPReceive
 *  @see ITCPUser
 *  @see TCPComm
 *  @see ThreadedTCPComm
 *  @see TCPServiceInit 
 */

public class LiveDataServer extends    DataSetServer
                            implements IUDPUser
{
  public  static final int MAGIC_NUMBER         = 483719513;
  public  static final int DEFAULT_DAS_UDP_PORT = 6080;

  private static final int DELAY_COUNT        = 3000;
  private static final int OLD_THRESHOLD      = 10000;  // ms until we call the
                                                        // data old;
  private static final int START_READ_ACCESS  = 0;
  private static final int STOP_READ_ACCESS   = 1;
  private static final int START_WRITE_ACCESS = 2;
  private static final int STOP_WRITE_ACCESS  = 3;

  private  int     delay_counter   = 0;             // only issue DATA_CHANGED
                                                    // messages when we have
                                                    // processed a specified
                                                    // number of pulses

  private  String  status           = RemoteDataRetriever.NO_DATA_SETS_STRING;
  private  long    last_time_ms     = 0;
  private  String  last_time        = "";
  private  String  instrument_name  = null;
  private  int     run_number       = -1;
  private  int     current_udp_port = DEFAULT_DAS_UDP_PORT;

                                                    // maintain compressed 
                                                    // copies of DataSets and
                                                    // info on time and number
                                                    // of updated spectra 
  CompressedDataSet comp_ds[] = new CompressedDataSet[0];
  long    comp_time_ms[] = new long[0];
  int     num_updated[]  = new int[0];
  boolean new_comp_ds    = true;                    // flag indicating whether
                                                    // or not the compressed 
                                                    // DataSet is new
  
  DataSet data_set[]     = new DataSet[0];          // current DataSets
  int     ds_type[]      = new int[0];              // current DataSet types
  int     spec_buffer[]  = new int[ 16384 ];        // buffer for one part of
                                                    // on spectrum

  int     max_seg_id  = 0;                  // size of table of all Data blocks
  int     ds_index[]  = new int[0];         // list of indices into data_set[]
                                            // array, indexed by group id
  Data    data_list[] = null;               // table of references to all Data
                                            // blocks.

  /* ---------------------------- Constructor -------------------------- */
  /**
   *  Construct a FileDataSetServer with an empty list of DataSets.
   */
   public LiveDataServer()
   {
     super();
     setServerName( "LiveDataServer" );
     setLogFilename( "LiveDataServerLog.txt" );
     last_time_ms = System.currentTimeMillis();
     last_time    = DateUtil.default_string();
   }

  /* -------------------------- setUDPport --------------------------- */
  /**
   *  Set the UDP port number to use when the server is started.  To be 
   *  effective this must be called before starting the server and the 
   *  specified port number must be positive.
   *
   *  @param  port  The port number to used when the server is started.
   */
  public void setUDPport( int port )
  {
     if ( port <= 0 )
       current_udp_port = DEFAULT_DAS_UDP_PORT;
     else
       current_udp_port = port;
  }


  /* ---------------------------- getUDPport --------------------------- */
  /**
   *  Get the UPD port used for this run of the server.
   *
   public String getUDPport()
   {
     return current_udp_port;
   }


  /* ------------------------- parseArgs ----------------------------- */
  /**
   *  Parse a list of command line arguments to extract values for the
   *  the data directories.  The only command supported at this level
   *  is -U.
   *
   *  @param args  Array of strings from the command line, containing
   *               command characters and arguments.
   */
   public void parseArgs( String args[] )
   {
     super.parseArgs( args );

     if ( StringUtil.commandPresent("-h", args ) ||
          StringUtil.commandPresent("-H", args )  )
       showLiveDataServerUsage();

     String command = StringUtil.getCommand( 1, "U", args );
     if ( command.length() > 0 )
     {
       try
       {
         int value = Integer.parseInt(command);
         setUDPport( value );
       }
       catch ( NumberFormatException e )
       {
       }
     }
   }


  /* ----------------------- showLiveDataServerUsage ----------------------- */
  /**
   *  Print list of supported commands.
   */
   public void showLiveDataServerUsage()
   {
     System.out.println("  -U<port>         Set UDP port to use for data");
     System.out.println("                   from the DAS");
   }


  /* ---------------------------- start_UDP ---------------------------- */
  /**
   *  Start the UDP listener running, listening for UDP packets on the last
   *  port that was specified using setUDPport, or the default port if
   *  none was specified.
   */
   public void start_UDP()
   {
    System.out.println("Starting UDP receiver on port " + current_udp_port );
    UDPReceive udp_comm;
    udp_comm = new UDPReceive( current_udp_port, this );
    udp_comm.setPriority(Thread.MAX_PRIORITY);
    udp_comm.start();
   }

  /* -------------------------- InitializeDataSets ---------------------- */
  /**
   *  Load the histogram and monitor DataSet structure for the specified
   *  instrument and run number from the runfile then zero out the monitor 
   *  and histogram DataSets.
   *
   *  @param  new_instrument   String giving the abbreviated instrument name
   *                           such as HRCS.
   *  @param  new_run_number   integer run number used to form the file name.
   *
   */
  private void InitializeDataSets( String new_instrument, int new_run_number )
  {
    instrument_name  = new_instrument.toLowerCase();
    run_number       = new_run_number;

    String file_name = InstrumentType.formIPNSFileName( new_instrument,
                                                        new_run_number );
    System.out.println("FileName: " + file_name );

    Retriever rr = get_retriever( file_name );

    if ( rr != null )
    {
      max_seg_id = 0;
      int num_data_sets = rr.numDataSets();
      if ( num_data_sets > 0 )
      {
        data_set     = new DataSet[ num_data_sets ];
        ds_type      = new int[ num_data_sets ];
        comp_ds      = new CompressedDataSet[ num_data_sets ];
        comp_time_ms = new long[ num_data_sets ];
        num_updated  = new int[ num_data_sets ];
        for ( int i = 0; i < num_data_sets; i++ )
        {
          ds_type[i] = rr.getType( i ); 
          data_set[i] = rr.getDataSet( i ); 
          SetToZero( data_set[i] );

          comp_ds[i]      = null;
          comp_time_ms[i] = System.currentTimeMillis();
          num_updated[i]  = 0;
        }
        data_name = data_set[0].getTitle();

        if ( max_seg_id <= 0 )
        {
          data_list = null;
          return;
        }                                 // now build list of references to 
                                          // the Data blocks, based on seg id
                                          // NOTE: seg_id's start at 1
        data_list = new Data[max_seg_id + 1]; 
        ds_index  = new int[max_seg_id + 1]; 
        Data d;
        int  id;
        for ( int i = 0; i < num_data_sets; i++ )
        { 
          for ( int k = 0; k < data_set[i].getNum_entries(); k++ )
          {
            d = data_set[i].getData_entry(k);
            id = d.getGroup_ID(); 
            data_list[id] = d;
            ds_index[id] = i;
          }
        }
        return;
      }
    }

    System.out.println("ERROR: Invalid runfile: " + file_name );
    data_set = new DataSet[0];
    ds_type  = new int[0];
    data_name = "DEFAULT_DATA_NAME";

    rr = null;
  }

  /* ---------------------------- ProcessData -------------------------- */
  /**
   *  Method to process data from the UDP port.  This is the method needed 
   *  to implement the IUDPUser interface.  It is called whenever UDP data
   *  is received from the DAS.  This method unpacks the instrument name,
   *  run number, group ID, integer spectrum data, etc. from the byte buffer.
   *  If the instrument or run number has changed, it loads the initial 
   *  monitor and histogram DataSets from the runfile.  Finally, it calls 
   *  a method to pack the new spectrum information into the DataSets.
   *  
   *  @param  buffer  byte buffer[] containing the data from the UDP packet
   *                  sent by the DAS.
   *
   *  @param  length  the length of the buffer that is used.
   */
  public void ProcessData( byte buffer[], int length )
  { 
    last_time_ms = System.currentTimeMillis();
    last_time = DateUtil.default_string(); 
    status    = RemoteDataRetriever.DATA_LIVE_STRING + last_time;

    String new_instrument_name;
    int    new_run_number;
                                               // make sure the buffer is long
                                               // enough to hold some data
    if ( length < 24 )
    {
      System.out.println("UDP packet with < 24 bytes, ignored");
      return;
    }

    int start = 0;
    int magic,
        id,
        first_channel,
        num_channels;
                                                // make sure the buffer starts
                                                // with the magic number 
    magic = ByteConvert.toInt( buffer, start );
    start += 4; 
    if ( magic != MAGIC_NUMBER )
    {
      System.out.println("UDP packet with wrong magic number, ignored");
      return;
    }

    int n_chars = buffer[ start ];            // get the instrument name length
    start++;                                  // instrument name String
    new_instrument_name = new String( buffer, start, n_chars );
    start += n_chars; 
                                              // unpack the new run number
    new_run_number = ByteConvert.toInt( buffer, start );
    start += 4;

    if ( new_run_number <= 0 )                // DAS going away, keep old data
    {
      status = RemoteDataRetriever.DAS_OFFLINE_STRING + last_time;
      return;
    }

    if ( new_run_number != run_number      ||
         instrument_name == null           ||
         !instrument_name.equalsIgnoreCase( new_instrument_name ) )
      InitializeDataSets( new_instrument_name, new_run_number );

                                              // unpack the group ID
    id = ByteConvert.toInt( buffer, start );
    start += 4;
                                              // unpack the first channel index
    first_channel = ByteConvert.toInt( buffer, start );
    start += 4;
                                              // unpack the number of channels
    num_channels = ByteConvert.toInt( buffer, start );
    start += 4;

    if ( length < 24 + 4 * num_channels )
    {
      System.out.println("UDP packet too short, ignored");
      return;
    }
                                              // unpack the int spectrum values
    for ( int i = 0; i < num_channels; i++ )
    {
      spec_buffer[i] = ByteConvert.toInt( buffer, start );
      start += 4;
    }

    if ( !RecordData( id, first_channel, num_channels, spec_buffer ) )
      System.out.println("Error: couldn't record id " + id ); 

    delay_counter++;                         // only notify observers after
    if ( delay_counter >= DELAY_COUNT )      // DELAY_COUNT spectra have been
    {                                        // processed.
      for ( int i = 0; i < data_set.length; i++ )
        data_set[i].notifyIObservers( IObserver.DATA_CHANGED );
      delay_counter = 0;
    }
  }

  
  /* -------------------------- ProcessCommand -------------------------- */
  /**
   *  Method to process commands from a TCP client.  
   *
   *  @param  command    The command object sent by the client.
   *
   *  @param  tcp_io     The TCP communications object to which the response
   *                     must be sent.
   *
   *  @return True if the command was processed and no further action is 
   *          needed. Returns false if the command was not handled, and
   *          so some response must still be sent back to the client.
   */
  synchronized public boolean ProcessCommand( CommandObject   command,
                                              ThreadedTCPComm tcp_io   )
  {
    if ( debug_server )
      System.out.println("Received request " + command );

    try
    {
      if (  command.getCommand() == CommandObject.GET_DS_TYPES )
      {
        int types[] = new int[ ds_type.length ];
        for ( int i = 0; i < types.length; i++ )
          types[i] = ds_type[i];
        tcp_io.Reset();                              // must reset since we
        tcp_io.Send( types );                        // changed the contents
        return true;
      }

      else if ( command.getCommand() == CommandObject.GET_DS_NAME )
      {
        int index = ((GetDataCommand)command).getDataSetNumber();

        if ( index >= 0 && index < ds_type.length )   // valid DataSet index
          tcp_io.Send( data_set[index].getTitle() );
        else
          tcp_io.Send( "DEFAULT_DATA_NAME" ); 
 
        return true;
      }

      else if ( command.getCommand() == CommandObject.GET_DS )
      {
        int index = ((GetDataCommand)command).getDataSetNumber();

        if ( index >= 0 && index < ds_type.length )   // valid DataSet index
        {                    
          DataSet source_ds = data_set[ index ];

          if ( source_ds != null )                    // remove observers
          {                                           // before sending
            Date date = new Date( System.currentTimeMillis() );
            source_ds.addLog_entry( "Live Data as of: " + date );
            source_ds.deleteIObservers(); 
            CompressedDataSet comp_ds;

            float min_tof = ((GetDataCommand)command).getMin_x();
            float max_tof = ((GetDataCommand)command).getMax_x();
            int   rebin   = ((GetDataCommand)command).getRebin_factor();
            String id_str = ((GetDataCommand)command).getGroup_ids();
            if ( ( min_tof <  max_tof  &&  min_tof > 0 )     || 
                   rebin   != 1                              ||
                   !id_str.equals(CommandObject.ALL_IDS)     ) 
            {
              comp_ds = getSubsetDS( index, id_str, min_tof, max_tof, rebin );
            }
            else                                      // send full DataSet
            {
              comp_ds = getCompressedDS(index);
              if ( new_comp_ds )                      // don't reset if we are
                tcp_io.Reset();                       // just sending the same
                                                      // compressed ds object
            }
            tcp_io.Send( comp_ds );
          }
          else                                       
            tcp_io.Send( DataSet.EMPTY_DATA_SET );
        }

        else                                          
          tcp_io.Send( DataSet.EMPTY_DATA_SET );

        return true;
      }

      else if ( command.getCommand() == CommandObject.GET_STATUS )
      {
         String reply = status;

         if ( status.startsWith(RemoteDataRetriever.NO_DATA_SETS_STRING) )
           reply = status + DateUtil.default_string();

         else if ( System.currentTimeMillis() - last_time_ms > OLD_THRESHOLD )
           reply = RemoteDataRetriever.DATA_OLD_STRING + last_time;

         tcp_io.Send( TCPComm.STATUS + reply );
         if ( debug_server )
           System.out.println("LDS replied with: "+ TCPComm.STATUS + reply); 

         return true;
      }
   
      else
      {
        if ( debug_server )
          System.out.println("CALLING super.ProcessCommand() for " + command );
        return false;
      }
    }
    catch ( Exception e )
    {
      System.out.println("Error: LiveDataServer command: " + command);
      System.out.println("Error: couldn't send data "+e );
      return false;
    }  
  }


 /*-------------------------------------------------------------------------
  *
  *  PRIVATE METHODS
  *
  * 
  */

  /* ----------------------------- SetToZero ---------------------------- */
  /**
   *  Zero out all of the spectra in the specified DataSet.  Also, set the
   *  errors to null, to save communication time and find the largest segment
   *  id in this DataSet, so that a table of Data blocks can be built.
   *
   *  @param  ds   The DataSet whose entries are to be zeroed out.
   */
  private void SetToZero( DataSet ds )
  {
    float         y[];
    int           seg_id;
    TabulatedData d;

    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      d = (TabulatedData)(ds.getData_entry(i));
      d.setErrors(null);
      y = d.getY_values();
      for ( int j = 0; j < y.length; j++ )
        y[j] = 0;

      seg_id = d.getGroup_ID();
      if ( seg_id > max_seg_id )
        max_seg_id = seg_id; 
    }
  }

  /* --------------------------- RecordData ------------------------------ */
  /**
   *  Record the new part of a spectrum in the appropriate Data block.
   *
   *  @param  id            The group ID of the spectrum to record
   *  @param  first_channel The first channel index in the partial spectrum
   *  @param  num_channels  The number of channels in the partial spectrum
   *  @param  spec_buffer   Buffer containing the partial spectrum data
   */
  private boolean RecordData( int     id,
                              int     first_channel,
                              int     num_channels,
                              int     spec_buffer[]  )
  {
    if ( data_list == null )                           // no list of Data blocks
      return false;

    Data d = data_list[id];
    if ( d == null )                                   // no such spectrum
      return false;

    if ( first_channel < 0 || num_channels < 0 )       // nonsense, so bail out
      return false;
                                                       // get the Y_values
    float y[] = d.getY_values();
    if ( first_channel >= y.length )                   // now make sure that
      return false;                                    // the indices are in
                                                       // range
    int last_channel = first_channel+num_channels - 1;
    if ( last_channel >= y.length )
      last_channel = y.length - 1; 
                                                       // actually set the new
                                                       // Y_values 
    for (int i = first_channel; i <= last_channel; i++)
      y[i] = spec_buffer[ i-first_channel ];
                                                      // set the time attribute 
    Attribute attrib = new StringAttribute( Attribute.UPDATE_TIME, 
                                            DateUtil.default_string()  );
    d.setAttribute( attrib );

    num_updated[ ds_index[id] ]++;                    // count another updated
                                                      // spectrum for this ds.
    return true;
  }


 /* -------------------------- getCompressedDS ---------------------------- */
 /**
  *  Get a compressed form of the specified DataSet.  If there is no compressed
  *  version available, make a new one.  Also, if the compressed version is
  *  more than a minute old, or if half of it's spectra have been updated,
  *  make a new one.
  */
  private CompressedDataSet getCompressedDS( int index )
  {
    if ( index < 0 || index > comp_ds.length )
      return null;
                                 // we need to make a new compressed DataSet if
                                 // there is none, or if the compressed DataSet
                                 // is too far out of date.
    if ( comp_ds[index] == null                                        ||
         num_updated[index]*2       > data_set[index].getNum_entries() ||
         System.currentTimeMillis() > comp_time_ms[index] + 60000      ) 
    {                                        // record the time and reset count
      num_updated[index]  = 0;
      comp_time_ms[index] = System.currentTimeMillis();
      comp_ds[index] = new CompressedDataSet( data_set[index] );
      new_comp_ds = true;
    }
    else
      new_comp_ds = false;

    return comp_ds[index];
  }

  private CompressedDataSet getSubsetDS( int    index, 
                                         String id_str, 
                                         float  min_tof, 
                                         float  max_tof, 
                                         int    rebin )
  {
    DataSet ds =  data_set[index];
    
    if ( ds == null || ds.getNum_entries() == 0 )
      return new CompressedDataSet( DataSet.EMPTY_DATA_SET );
 
    DataSet new_ds = ds.empty_clone();
    int ids[] = new int[0];
    if( !id_str.equals( CommandObject.ALL_IDS ) )
      ids = IntList.ToArray( id_str );
    else
    {
      int first_id = ds.getData_entry(0).getGroup_ID();
      int last_id  = ds.getData_entry( ds.getNum_entries()-1 ).getGroup_ID();
      ids = new int[ last_id - first_id + 1 ];
      for ( int i = 0; i < ids.length; i++ )
        ids[i] = first_id + i;
    }

    boolean must_rebin = false;
    XScale  x_scale = ds.getData_entry(0).getX_scale();
    if ( min_tof < max_tof && min_tof > 0 )
    { 
      must_rebin = true;
      x_scale = x_scale.restrict( new ClosedInterval(min_tof,max_tof) );
    }

    if ( rebin > 1 && rebin <= x_scale.getNum_x() )
    {
      must_rebin = true;
      float bins[] = x_scale.getXs();
      float new_bins[] = new float[ bins.length/rebin ];
      for ( int i = 0; i < new_bins.length; i++ )
        new_bins[i] = bins[i*rebin];
      x_scale = new VariableXScale( new_bins );
    }

    for ( int i = 0; i < ids.length; i++ )
      if ( ids[i] >= 0 && ids[i] < ds_index.length )
        if ( ds_index[ids[i]] == index )                // ids[i] is in ds
        {
          Data d = data_list[ids[i]];
          if ( must_rebin )
          {
            d = (Data)d.clone();
            d.resample( x_scale, Data.SMOOTH_NONE );
          }
          new_ds.addData_entry( d );
        }

    return new CompressedDataSet( new_ds );
  }

  /* ------------------------------ main --------------------------------- */

  public static void main(String args[])
  {
    System.out.println();

    LiveDataServer server= new LiveDataServer();
    server.parseArgs( args );

    if ( StringUtil.commandPresent( "-h", args )  ||
         StringUtil.commandPresent( "-h", args )  )
      System.exit(1);

    System.out.println("Starting " + server.getServerName() + " on " + 
                        DateUtil.default_string()  );
    System.out.println("Log File " + server.getLogFilename() );
    System.out.println("Using DataDirectories ");
    server.showDataDirectories();

    server.start_UDP( );
    server.startTCP();
  }
}

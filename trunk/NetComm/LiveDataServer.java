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

  private static final int DELAY_COUNT        = 300;
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

  DataSet data_set[]    = new DataSet[0];           // current DataSets
  int     ds_type[]     = new int[0];               // current DataSet types
  int     spec_buffer[] = new int[ 16384 ];         // buffer for one part of
                                                    // on spectrum

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
                                                         // pad run_number with
                                                         // leading 0's to at 
                                                         // least length 4.
    String run_number_string = new String();
    run_number_string = "" + run_number;
    while ( run_number_string.length() < 4 )
       run_number_string = "0" + run_number_string;

    String file_name = instrument_name + run_number_string + ".run"; 

    System.out.println( "FileName: " + file_name );
    Retriever rr = get_retriever( file_name );

    if ( rr != null )
    {
      int num_data_sets = rr.numDataSets();
      if ( num_data_sets > 0 )
      {
        data_set = new DataSet[ num_data_sets ];
        ds_type = new int[ num_data_sets ];
        for ( int i = 0; i < num_data_sets; i++ )
        {
          ds_type[i] = rr.getType( i ); 
          data_set[i] = rr.getDataSet( i ); 
          SetToZero( data_set[i] );
        }
        data_name = data_set[0].getTitle();
        return;
      }
    }

    System.out.println("ERROR: Invalid runfile: " + file_name );
    data_set = new DataSet[0];
    ds_type  = new int[0];
    data_name = "NONE";

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
                                              // record the spectrum values in
                                              // the first DataSet possible
    int ds_num = 0;
    while ( (ds_num < data_set.length) &&
            !RecordData( data_set[ds_num], id, 
                         first_channel, num_channels, spec_buffer) )
      ds_num++;

    delay_counter++;                         // only notify observers after
    if ( delay_counter >= DELAY_COUNT )      // DELAY_COUNT pulses have been
    {                                        // processed.
      for ( int i = 0; i < data_set.length; i++ )
        data_set[i].notifyIObservers( IObserver.DATA_CHANGED );
      delay_counter = 0;
    }
  }

 /* --------------------------- ProcessCommand --------------------------- */
 /**
  *  Method to process commands from a TCP client.  Derived classes should
  *  override this method with methods that process the command properly
  *  and return an appropriate object through the tcp_io object.
  *
  *  @param  command    The command string sent by the client.
  *
  *  @param  tcp_io     The TCP communications object to which the response
  *                     must be sent.
  */
  synchronized public void ProcessCommand( String          command,
                                           ThreadedTCPComm tcp_io   )
  {
    if ( debug_server )
      System.out.println("Received request " + command );

    try
    {
      if (  command.startsWith( COMMAND_GET_DS_TYPES ) )
      {
        int types[] = new int[ ds_type.length ];
        for ( int i = 0; i < types.length; i++ )
          types[i] = ds_type[i];
        tcp_io.Send( types );
        return;
      }

      else if ( command.startsWith( COMMAND_GET_DS ) )
      {
        int index = extractIntParameter( command );

        if ( index >= 0 && index < ds_type.length )   // valid DataSet index
        {                                             // so get a copy of a
                                                      // snapshot of the ds
          DataSet source_ds = data_set[ index ];
          DataSet ds        = (DataSet)(source_ds.clone());

          if ( ds != null )                           // remove observers
          {                                           // before sending
            Date date = new Date( System.currentTimeMillis() );
            ds.addLog_entry( "Live Data as of: " + date );
            ds.deleteIObservers(); 
            tcp_io.Send( ds  );
          }
          else                                       
            tcp_io.Send( DataSet.EMPTY_DATA_SET.clone() );
        }

        else                                          
          tcp_io.Send( DataSet.EMPTY_DATA_SET.clone() );

        return;
      }

      else if ( command.startsWith( COMMAND_GET_STATUS ) )
      {
         if ( status.startsWith( RemoteDataRetriever.DAS_OFFLINE_STRING ) )
         {
           tcp_io.Send( status );
           return;
         }

         if ( status.startsWith( RemoteDataRetriever.NO_DATA_SETS_STRING ) )
         {
           tcp_io.Send( status + DateUtil.default_string() );
           return;
         }

         long time_ms = System.currentTimeMillis();
         if ( time_ms - last_time_ms > OLD_THRESHOLD )
           tcp_io.Send( RemoteDataRetriever.DATA_OLD_STRING + last_time );
         else
           tcp_io.Send( status );

         return;
      }
   
      else
      {
        if ( debug_server )
          System.out.println("CALLING super.ProcessCommand() for " + command );
        super.ProcessCommand( command, tcp_io );
      }
    }
    catch ( Exception e )
    {
      System.out.println("Error: LiveDataServer command: " + command);
      System.out.println("Error: couldn't send data "+e );
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
   *  errors to null, to save communication time.
   *
   *  @param  ds   The DataSet whose entries are to be zeroed out.
   */
  private void SetToZero( DataSet ds )
  {
    float         y[];
    TabulatedData d;

    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      d = (TabulatedData)(ds.getData_entry(i));
      d.setErrors(null);
      y = d.getY_values();
      for ( int j = 0; j < y.length; j++ )
        y[j] = 0;
    }
  }

  /* --------------------------- RecordData ------------------------------ */
  /**
   *  Record part of a spectrum in the specified DataSet.
   *
   *  @param  ds            The DataSet in which to record the partial spectrum
   *  @param  id            The group ID of the spectrum to record
   *  @param  first_channel The first channel index in the partial spectrum
   *  @param  num_channels  The number of channels in the partial spectrum
   *  @param  spec_buffer   Buffer containing the partial spectrum data
   */
  private boolean RecordData( DataSet ds,
                              int     id,
                              int     first_channel,
                              int     num_channels,
                              int     spec_buffer[]  )
  {
    Data d = ds.getData_entry_with_id( id );           // get the Data block
    if ( d == null )
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

    return true;
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

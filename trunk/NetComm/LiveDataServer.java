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
 *  Revision 1.15  2001/08/09 21:47:26  dennis
 *  Uses start_TCP() method to start the TCPServer.
 *
 *  Revision 1.14  2001/08/09 15:42:27  dennis
 *  Now checks if the retriever is null.
 *  Put some debug prints in "if (debug server)" blocks
 *
 *  Revision 1.13  2001/08/07 21:30:59  dennis
 *    Removed command to get DS_TYPE and get NUM_DS... now only uses
 *  get DS_TYPES, for the whole list of types.  This is simpler,
 *  reduces the number of requests needed and allows LiveDataServer
 *  and FileDataServer to handle the same requests.
 *
 *  Revision 1.12  2001/08/03 21:32:00  dennis
 *  Now derives from DataSetServer/TCPServer, to provide user name,
 *  password and logging features.
 *
 *  Revision 1.11  2001/06/08 16:13:24  dennis
 *  Change PORT to DEFAULT_PORT and now allow specifying
 *  different ports.
 *
 *  Revision 1.10  2001/06/06 21:05:09  dennis
 *  Now sends DataSet.EMPTY_DATA_SET clone if the requested
 *  DataSet is null, or doesn't exist.
 *
 *  Revision 1.9  2001/04/23 19:44:14  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.8  2001/03/02 17:07:14  dennis
 *  Added TestAndSetAccessFlag method that may in the future be used
 *  to prevent access to the DataSets while the runfile is being
 *  changed.
 *
 *  Revision 1.7  2001/02/22 20:56:48  dennis
 *    Now handles all of the DataSets from a runfile.  It no longer assumes
 *  that DataSet 0 is the monitor DataSet and that there is only one
 *  histogram DataSet.
 *    Now it handles commands to get the number of DataSets, get the type of
 *  a specific DataSet and get a specific DataSet.
 *
 *  Revision 1.6  2001/02/16 22:04:38  dennis
 *  Added array bounds checking on the information from the DAS to
 *  avoid problems if the DAS sends bad channel numbers.
 *  Also, made instance variables private, and only notify
 *  any observers of the dataset once every 300 pulses.
 *
 *  Revision 1.5  2001/02/15 22:10:24  dennis
 *  Now makes clones of the monitor and histogram DataSets before sending
 *  them.  This was needed since otherwise the DataSets would not actually
 *  be sent if they were requested a second time.
 *
 *  Revision 1.4  2001/02/06 22:06:41  dennis
 *  The directory where the current runfile will be found
 *  can now be specified on the command line.  If it is
 *  not specified, the current directory will be used.
 *
 *  Revision 1.3  2001/02/02 20:58:23  dennis
 *  Now gets the instrument name and run number from each UDP packet sent
 *  by the DAS.  If the instrument name or run number changes, the
 *  Histogram and Monitor DataSets will be re-initialized from the
 *  corresponding runfile.
 *
 *  Revision 1.2  2001/01/31 14:21:27  dennis
 *  Fixed error in javadoc comment.
 *
 *  Revision 1.1  2001/01/30 23:27:24  dennis
 *  Initial version, network communications for ISAW.
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

  private static final int START_READ_ACCESS  = 0;
  private static final int STOP_READ_ACCESS   = 1;
  private static final int START_WRITE_ACCESS = 2;
  private static final int STOP_WRITE_ACCESS  = 3;

  private  int     delay_counter   = 0;             // only issue DATA_CHANGED
                                                    // messages when we have
                                                    // processed a specified
                                                    // number of pulses
  private  String  instrument_name  = null;
  private  int     run_number       = -1;
  private  int     current_udp_port = DEFAULT_DAS_UDP_PORT;

  private  int read_count = 0;             // This is used to avoid switching 
                                           // to a new runfile at the
                                           // same time that data from the 
                                           // current run is being transmitted.
                                           // See: TestAndSetReadCount()

  DataSet data_set[] = new DataSet[0];              // current DataSets
  int     ds_type[]  = new int[0];                  // current DataSet types

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
   }

  /* ---------------------------- start_UDP ---------------------------- */
  /**
   *  Start the server listening for UDP packets on the specified port.
   *
   *  @param  udp_port  The udp port to listen to for data from the DAS.
   */
   public void start_UDP( int udp_port )
   {
                                         // Start the UPD receiver to listen
                                         // for data from the DAS
    if ( udp_port < 0 )
      udp_port = current_udp_port;

    System.out.println("Starting UDP receiver on port " + udp_port );
    UDPReceive udp_comm;
    udp_comm = new UDPReceive( udp_port, this );
    udp_comm.setPriority(Thread.MAX_PRIORITY);
    udp_comm.start();

    current_udp_port = udp_port;
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
    String file_name = instrument_name + run_number + ".run"; 

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
            ds.deleteIObservers(); 
            tcp_io.Send( ds  );
          }
          else                                       
            tcp_io.Send( DataSet.EMPTY_DATA_SET.clone() );
        }

        else                                          
          tcp_io.Send( DataSet.EMPTY_DATA_SET.clone() );
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

  /* -------------------------- TestAndSetAccessFlag ---------------------- */

  synchronized private boolean TestAndSetAccessFlag( int access ) 
  {
    if ( access == START_READ_ACCESS )           // we want to start reading 
    {
      if ( read_count < 0 )
        return false;                            // blocked
      else
      {
        read_count++;                            // ok, count another reader
        return true;
      }
    }

    else if ( access == STOP_READ_ACCESS )       // we want to stop reading  
    {
      if ( read_count <= 0 )
      {
        System.out.println("ERROR: stoping read in TestAndSetAccessFlag, " +
                           "but read_count <= 0: read_count =" + read_count );
        return false;
      } 
      else
      {
        read_count--;                             // ok, count one less reader
        return true;
      }
    }

    else if ( access == START_WRITE_ACCESS )      // we want to start writing   
    {
      if ( read_count > 0 )
        return false;                             // blocked
      else
      {
        read_count = -1000;                       // ok, lock out any readers
        return true;
      }
    }

    else if ( access == STOP_WRITE_ACCESS )
    {
      if ( read_count >= 0 )
      {
        System.out.println("ERROR: stoping write in TestAndSetAccessFlag, " + 
                           "but read_count >= 0 read_count =" + read_count );
        return false;
      } 
      else
      {
        read_count = 0;                            // ok, allow readers now
        return true;
      }
    }

    else
    {
      System.out.println("ERROR: Invalid command in TestAndSetAccessFlag");
      return false;
    }
  }



  /**
   *  Zero out all of the spectra in the specified DataSet
   *
   *  @param  ds   The DataSet whose entries are to be zeroed out.
   */
  private void SetToZero( DataSet ds )
  {
    float y[];

    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      y = ds.getData_entry(i).getY_values();
      for ( int j = 0; j < y.length; j++ )
        y[j] = 0;
    }
  }


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
    Date date = new Date( System.currentTimeMillis() );
    Attribute attrib = new StringAttribute( Attribute.UPDATE_TIME, 
                                            date.toString()         );
    d.setAttribute( attrib );

    return true;
  }


  /* ------------------------------ main --------------------------------- */

  public static void main(String args[])
  {
    System.out.println("Live Data Server:");
    System.out.println("You may specify up to three parameters on the ");
    System.out.println("command line.  You can omit trailing parameters, but ");
    System.out.println("not leading parameters.  The possible parameters are:");
    System.out.println(" 1) The data directory from which to read the runfile");
    System.out.println(" 2) The UDP port for the server use for the DAS" );
    System.out.println(" 3) The TCP port for the server use for clients" );
    String dataDirectory = null;
    Date date = new Date( System.currentTimeMillis() );
    System.out.println("Date = " + date );

    LiveDataServer server= new LiveDataServer();
                     
    if ( args.length != 0 ) {
	dataDirectory = args[0];
	System.out.println( "Data directory is: " + dataDirectory );
    }
    else {
	dataDirectory = new String("");
	System.out.println("No Directory was specified.  Data directory "
			   + " will be current directory" );
    }
    server.addDataDirectory( dataDirectory );


                                         // Start the UPD receiver to listen
                                         // for data from the DAS
    if ( args.length >=2 )
    {
      int udp_port = Integer.parseInt( args[1] );
      server.start_UDP( udp_port );
    }
    else
      server.start_UDP( -1 );

                                         // Start the TCP server to listen 
                                         // for clients requesting data
    if ( args.length >=3 )
    {
      int tcp_port = Integer.parseInt( args[2] );
      server.start_TCP( tcp_port );
    }
    else
      server.start_TCP( -1 );

//  We could pop up viewers to see each new spectrum as it is received, but
//  currently the viewers DON'T handle partial updates very well... most
//  of the time is wasted redrawing everything.
//
//  ViewManager mon_vm  = new ViewManager( server.mon_ds, IViewManager.IMAGE );
//  ViewManager hist_vm = new ViewManager( server.hist_ds, IViewManager.IMAGE );
  }
}

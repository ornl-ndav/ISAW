/*
 * @(#)LiveDataServer.java
 *
 *  Prototype for a LiveDataServer to run on an IPNS instrument computer. 
 *
 *  Programmers: Ruth Mikkelson,
 *               Alok Chatterjee,
 *               Dennis Mikkelson
 *               John Hammonds
 *
 *  $Log$
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

public class LiveDataServer implements IUDPUser,
                                       ITCPUser
{
  public static final String COMMAND_GET_DS      = "COMMAND:GET_DATA_SET ";
  public static final String COMMAND_GET_DS_TYPE = "COMMAND:GET_DATA_SET_TYPE ";
  public static final String COMMAND_GET_NUM_DS  = "COMMAND:GET_NUM_DATA_SETS";

  public  static final int MAGIC_NUMBER       = 483719513;
  public  static final int SERVER_PORT_NUMBER = 6088;

  private static final int DELAY_COUNT        = 300;

  private static final int START_READ_ACCESS  = 0;
  private static final int STOP_READ_ACCESS   = 1;
  private static final int START_WRITE_ACCESS = 2;
  private static final int STOP_WRITE_ACCESS  = 3;

  private  int     delay_counter   = 0;             // only issue DATA_CHANGED
                                                    // messages when we have
                                                    // processed a specified
                                                    // number of pulses
  private  String  directory_name  = null;
  private  String  file_name       = null;
  private  String  instrument_name = null;
  private  int     run_number      = -1;

  private  int read_count = 0;             // This is used to avoid switching 
                                           // to a new runfile at the
                                           // same time that data from the 
                                           // current run is being transmitted.
                                           // See: TestAndSetReadCount()

  DataSet data_set[] = new DataSet[0];              // current DataSets
  int     ds_type[]  = new int[0];                  // current DataSet types

  int     spec_buffer[] = new int[ 16384 ];         // buffer for one part of
                                                    // on spectrum

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
    instrument_name = new_instrument.toLowerCase();
    run_number      = new_run_number;
    file_name       = directory_name + instrument_name + run_number + ".run"; 

    System.out.println( "FileName: " + file_name );
    RunfileRetriever rr = new RunfileRetriever( file_name );

    int num_data_sets = rr.numDataSets();
    data_set = new DataSet[ num_data_sets ];
    ds_type = new int[ num_data_sets ];
    for ( int i = 0; i < num_data_sets; i++ )
    {
      ds_type[i] = rr.getType( i ); 
      data_set[i] = rr.getDataSet( i ); 
      SetToZero( data_set[i] );
    }

    rr = null;
  }

  /**
   *  Method to set the data directory.  If a directory is specified on the 
   *  command line it is used as the data directory.  If not, the current
   *  directory is used.
   *
   *  @ param String dataDirectory
   */
  public void SetDataDirectory( String dataDirectory )
    {
	directory_name = dataDirectory;
    }

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

 /**
  *  Method to process data from a TCP client.  This is the method needed
  *  to implement the ITCPUser interface.  It is called whenever an Object
  *  is received from a TCP client.  The Object should consist of a String
  *  command, requesting the monitor or histogram DataSet.
  *
  *  @param  buffer  byte buffer[] containing the data from the UDP packet
  *                  sent by the DAS.
  *
  *  @param  length  the length of the buffer that is used.
  */
  public void ProcessData( Object data_obj, ThreadedTCPComm tcp_io )
  {
    if ( data_obj instanceof String )
    {
      String command = (String)data_obj; 
      System.out.println("Received request " + command );
      try
      {
        if (  command.equalsIgnoreCase( COMMAND_GET_NUM_DS ) )
        {
          System.out.println("Processing GET NUM DS " + command );
          tcp_io.Send( new Integer( data_set.length ) );
        }

        else if ( command.startsWith( COMMAND_GET_DS_TYPE ) )
        {
          System.out.println("Processing GET DS TYPE " + command );
          int index = extractIntParameter( command );

          if ( index >= 0 && index < ds_type.length )
            tcp_io.Send( new Integer( ds_type[ index ] ) );
          else
            tcp_io.Send( new Integer( Retriever.INVALID_DATA_SET ) );
        }

        else if ( command.startsWith( COMMAND_GET_DS ) )
        {
          System.out.println("Processing GET DS " + command );
          int index = extractIntParameter( command );
          if ( index >= 0 && index < ds_type.length )   //valid DataSet index
          {
            DataSet ds = (DataSet)(data_set[ index ].clone());
            if ( ds != null )     // must remove observers before sending
            {
              System.out.println("Trying to send " + ds );
              ds.deleteIObservers(); 
              tcp_io.Send( ds  );
              System.out.println("Finished sending " + ds );
            }
          }
          else                                         // bad index, return
          {
            tcp_io.Send( DataSet.EMPTY_DATA_SET.clone() );
          }
        }
      }
      catch ( Exception e )
      {
        System.out.println("Error: LiveDataServer command: " + command);
        System.out.println("Error: couldn't send data "+e );
      }  
    }
  }


 /*-------------------------------------------------------------------------
  *
  *  PRIVATE METHODS
  *
  * 
  */

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
   *  Extract the first integer value occuring in a command string.
   *
   *  @param command  A command string containing an integer parameter following
   *                  a space ' ' character. 
   */
  private int extractIntParameter( String command )
  {
    int first_space = command.indexOf( " " );       // extract string following 
                                                    // the first space, if
                                                    // possible
    if ( first_space < 0 )
      return -1;
    
    command = command.substring( first_space + 1 );
    command.trim();

    int next_space = command.indexOf( " " );
    String int_string = " ";
    if ( next_space < 0 )
      int_string = command;
    else
      int_string = command.substring( 0, next_space );

    int parameter = (Integer.valueOf( int_string )).intValue();
    return parameter;
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
    server.SetDataDirectory( dataDirectory );
                                         // Start the UPD receiver to listen
                                         // for data from the DAS
    System.out.println("Starting UDP receiver...");
    UDPReceive udp_comm;
    udp_comm = new UDPReceive( DASOutputTest.DAS_UDP_PORT, server );
    udp_comm.setPriority(Thread.MAX_PRIORITY);
    udp_comm.start();
    System.out.println("UDP receiver started.");
    System.out.println();
                                         // Start the TCP server to listen 
                                         // for clients requesting data
    System.out.println("Starting TCP server...");
    TCPServiceInit TCPinit = new TCPServiceInit( server, SERVER_PORT_NUMBER);
    TCPinit.start();
    System.out.println("TCP server started.");
    System.out.println();
   
//  We could pop up viewers to see each new spectrum as it is received, but
//  currently the viewers DON'T handle partial updates very well... most
//  of the time is wasted redrawing everything.
//
//  ViewManager mon_vm  = new ViewManager( server.mon_ds, IViewManager.IMAGE );
//  ViewManager hist_vm = new ViewManager( server.hist_ds, IViewManager.IMAGE );
  }
}

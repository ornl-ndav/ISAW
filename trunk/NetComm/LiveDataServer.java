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
  public  static final int MAGIC_NUMBER       = 483719513;
  public  static final int SERVER_PORT_NUMBER = 6088;
  private static final int DELAY_COUNT        = 300;

  private  int     delay_counter   = 0;             // only issue DATA_CHANGED
                                                    // messages when we have
                                                    // processed a specified
                                                    // number of pulses
  private  String  directory_name  = null;
  private  String  file_name       = null;
  private  String  instrument_name = null;
  private  int     run_number      = -1;

  DataSet mon_ds;                                   // current monitor DataSet
  DataSet hist_ds;                                  // current histogram DataSet

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
    mon_ds  = rr.getDataSet(0);
    hist_ds = rr.getDataSet(1);
    rr = null;
    SetToZero( mon_ds );
    SetToZero( hist_ds );
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
                                              // the monitor or histogram
                                              // DataSet. 
    if ( !RecordData( mon_ds, id, first_channel, num_channels, spec_buffer ) )
      RecordData( hist_ds, id, first_channel, num_channels, spec_buffer );

    delay_counter++;                         // only notify observers after
    if ( delay_counter >= DELAY_COUNT )      // DELAY_COUNT pulses have been
    {                                        // processed.
      mon_ds.notifyIObservers( IObserver.DATA_CHANGED );
      hist_ds.notifyIObservers( IObserver.DATA_CHANGED );
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
      System.out.println("Received request " + data_obj );
      String command = (String)data_obj; 
      {
        try
        {
          DataSet ds = null;

          if ( command.equalsIgnoreCase( "GET MONITORS" ) )
            ds = (DataSet)(mon_ds.clone());
          else if ( command.equalsIgnoreCase( "GET HISTOGRAM" ) ) 
            ds = (DataSet)(hist_ds.clone());

          if ( ds != null )     // must remove observers before sending
          {
            IObserverList observer_list = ds.getIObserverList();
            ds.deleteIObservers(); 
            System.out.println("Trying to send " + ds );
            tcp_io.Send( ds  );
            System.out.println("Finished sending " + ds );

//          Data d = ds.getData_entry(0);         // for debug purposes only
//          System.out.println( "For entry " + 0 + " Time = " +
//                      (String)(d.getAttributeValue(Attribute.UPDATE_TIME)));

//          ds.setIObserverList( observer_list ); // needed if ds is not a  
                                                  // clone of mon_ds or hist_ds
          }
        }
        catch ( Exception e )
        {
          System.out.println("Error: couldn't send data "+e );
        }  
      }
    }
  }

  /**
   *  Zero out all of the spectra in the specified DataSet
   *
   *  @param  ds   The DataSet whose entries are to be zeroed out.
   */
  public void SetToZero( DataSet ds )
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

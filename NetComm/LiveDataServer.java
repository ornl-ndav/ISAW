/*
 * @(#)LiveDataServer.java
 *
 *  Prototype for a LiveDataServer to run on an IPNS instrument computer. 
 *
 *  Programmers: Ruth Mikkelson,
 *               Alok Chatterjee,
 *               Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.2  2001/01/31 14:21:27  dennis
 *  Fixed error in javadoc comment.
 *
 *  Revision 1.1  2001/01/30 23:27:24  dennis
 *  Initial version, network communications for ISAW.
 *
 *
 */
package NetComm;

import java.lang.*;
import java.net.*;
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
  public static final int MAGIC_NUMBER       = 483719513;
  public static final int SERVER_PORT_NUMBER = 6088;

  DataSet mon_ds;                                   // current monitor DataSet
  DataSet hist_ds;                                  // current histogram DataSet

  int     spec_buffer[] = new int[ 16384 ];         // buffer for one part of
                                                    // on spectrum


  /**
   *  Method to process data from the UDP port.  This is the method needed 
   *  to implement the IUDPUser interface.  It is called whenever UDP data
   *  is received from the DAS.  This method unpacks the group ID, integer
   *  spectrum data, etc. from the byte buffer and calls a method to pack
   *  that information into the current DataSet.
   *  
   *  @param  buffer  byte buffer[] containing the data from the UDP packet
   *                  sent by the DAS.
   *
   *  @param  length  the length of the buffer that is used.
   */
  public void ProcessData( byte buffer[], int length )
  { 
                                               // make sure the buffer is long
                                               // enough to hold some data
    if ( length < 16 )
    {
      System.out.println("UDP packet with < 16 bytes, ignored");
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
                                              // unpack the group ID
    id = ByteConvert.toInt( buffer, start );
    start += 4;
                                              // unpack the first channel index
    first_channel = ByteConvert.toInt( buffer, start );
    start += 4;
                                              // unpack the number of channels
    num_channels = ByteConvert.toInt( buffer, start );
    start += 4;

    if ( length < 16 + 4 * num_channels )
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
          DataSet ds            = null;

          if ( command.equalsIgnoreCase( "GET MONITORS" ) )
            ds = mon_ds;
          else if ( command.equalsIgnoreCase( "GET HISTOGRAM" ) ) 
            ds = hist_ds;

          if ( ds != null )     // must remove observers before sending
          {
            IObserverList observer_list = ds.getIObserverList();
            ds.deleteIObservers(); 
            System.out.println("Trying to send " + ds );
            tcp_io.Send( ds  );
            System.out.println("Finished sending " + ds );
            ds.setIObserverList( observer_list ); 
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
    Data d = ds.getData_entry_with_id( id );
    if ( d == null )
      return false;

    float y[] = d.getY_values();
    for ( int i = first_channel; i < first_channel+num_channels; i++ )
      y[i] = spec_buffer[ i-first_channel ];

    ds.notifyIObservers( IObserver.DATA_CHANGED );
    return true;
  }


  /* ------------------------------ main --------------------------------- */

  public static void main(String args[])
  {

    LiveDataServer server= new LiveDataServer();
                     
    if ( args.length <= 0 )
    {
      System.out.println("ERROR: you must specifiy a runfile to use for");
      System.out.println("       testing.  This runfile must be the same");
      System.out.println("       as will be used when starting the ");
      System.out.println("       DASOutputTest." );
      System.out.println("       Try 'java DASOutputTest hrcs2447.run'");

      System.exit(1);
    }
                                         // Load the DataSet structure from
                                         // the runfile, and zero out the
                                         // monitor and histogram data
    RunfileRetriever rr = new RunfileRetriever( args[0] );
    server.mon_ds  = rr.getDataSet(0);
    server.hist_ds = rr.getDataSet(1);
    rr = null;
    server.SetToZero( server.mon_ds );
    server.SetToZero( server.hist_ds );
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

/*
 * @(#)LiveDataRetriever.java      
 *
 * Programmers: Dennis Mikkelson,
 *              Alok Chatterjee
 *
 *  $Log$
 *  Revision 1.6  2001/02/22 21:03:32  dennis
 *  Now gets all of the DataSets from the LiveDataServer.
 *  Also, changed methods for getting the number and types of DataSets
 *  to get that information from the LiveDataServer, rather than
 *  assuming that there were just two DataSets, a monitor and histogram
 *  DataSet.
 *
 *  Revision 1.5  2001/02/15 23:23:01  dennis
 *  Added finalize() method and made Exit() private.
 *  Now finalize() calls Exit().
 *
 *  Revision 1.4  2001/02/09 14:19:06  dennis
 *  Changed CURRENT_TIME attribute to UPDATE_TIME.
 *
 *  Revision 1.3  2001/02/02 20:53:51  dennis
 *  Added loop to display the CURRENT_TIME attribute for testing.
 *
 *  Revision 1.2  2001/01/31 14:25:19  dennis
 *  Added Exit() method to break the TCP connection to the remote
 *  server and free up the TCP socket.
 *
 *  Revision 1.1  2001/01/29 21:14:10  dennis
 *  Initial version of retriever for "live" data.
 *
 *
 */
package DataSetTools.retriever;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import java.io.*;
import java.net.*;
import NetComm.*;

/**
 *  This class opens a TCP connection to a LiveDataServer object running
 *  on a remote machine and extracts the DataSets corresponding to
 *  monitors and sample histograms.
 */
public class LiveDataRetriever extends    Retriever 
                               implements Serializable
{
  public static final int TIMEOUT_MS = 60000;

  TCPComm tcp_io = null;

/**
 *  Construct a LiveDataRetriever to get data from a LiveDataServer running
 *  a specified instrument computer.
 *
 *  @param data_source_name  The host name for the instrument computer.  It
 *                           is assumed that a LiveDataServer is running on
 *                           that computer on the required port. 
 */
  public LiveDataRetriever(String data_source_name) 
  {
    super(data_source_name);

    try
    {
      Socket sock = new Socket( data_source_name, 
                                LiveDataServer.SERVER_PORT_NUMBER );
      tcp_io      = new TCPComm( sock, TIMEOUT_MS );
    }
    catch( Exception e ) 
    {
      System.out.println("Exception in LiveDataRetriever constructor");
      System.out.println("Exception is " +  e ); 
    }
  }

/**
 *  Get the number of distinct DataSets from this LiveDataServer. 
 *  The monitors are placed into one DataSet.  Any sample histograms are 
 *  placed into separate DataSets.  
 *   
 *  @return the number of distinct DataSets in this runfile.
 */  
  public int numDataSets()
  { 
    Object obj = getObjectFromServer( LiveDataServer.COMMAND_GET_NUM_DS );

    if ( obj != null && obj instanceof Integer )
    {
      int num = ((Integer)obj).intValue();
      if ( num >= 0 )
        return num;
    }

    return 0;                                   // somethings wrong, so no
                                                // DataSets are available
  }

/**
 * Get the type of the specified data set from the current data source.
 * The type is an integer flag that indicates whether the data set contains
 * monitor data or data from other detectors.
 */

  public int getType( int data_set_num )
  {
    Object obj = getObjectFromServer( LiveDataServer.COMMAND_GET_DS_TYPE + 
                                      data_set_num );

    if ( obj != null && obj instanceof Integer )
    {
      int num = ((Integer)obj).intValue();
      return num;
    }

    return Retriever.INVALID_DATA_SET;          // somethings wrong, so 
                                                // DataSet type is invalid
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
    System.out.println( "For ds #" + data_set_num + " type = " + 
                         getType( data_set_num ) );

    Object obj = getObjectFromServer( 
                           LiveDataServer.COMMAND_GET_DS + data_set_num );
    if ( obj != null && obj instanceof DataSet )
      return (DataSet)obj;

    return (DataSet)(DataSet.EMPTY_DATA_SET.clone());
  }

/**
 *  Break the connection with the LiveDataServer. 
 *
 */
  private void Exit()
  {
    try
    {
      tcp_io.Send( new TCPCommExitClass() );
    }
    catch ( Exception e )
    {
      System.out.println( "Exception in LiveDataRetriever.Exit():" + e );
    }
  }


/**
 *
 */
 private Object getObjectFromServer( String command )
 {
    System.out.println("LiveDataRetriever sending command:" + command );
    boolean request_sent = false;

    try
    {
      System.out.println( "Command sent: " + command );
      tcp_io.Send( command );
      request_sent = true;
    }
    catch ( Exception e )
    {
      System.out.println("EXCEPTION in LiveDataRetriever:" + e );
      System.out.println("while sending command: " + command );
    }

    if ( request_sent )
    {
      try
      {
        Object obj = null;
        obj = tcp_io.Receive();
        System.out.println( "Got " + obj );

        System.out.println("LiveDataRetriever finished command:" + command );
        return obj;
      }
      catch ( Exception e )
      {
        System.out.println("EXCEPTION in LiveDataRetriever:" + e );
        System.out.println("while receiving response to command: " + command );
      }
    }

    System.out.println("LiveDataRetriever failed command:" + command );
    return null;
  }


/**
 *  Finalize method to make sure that the TCP connection is closed if this
 *  LiveDataRetriever object is no longer being used.
 */
  protected void finalize() throws IOException
  {
     System.out.println("Retriever finalization");
     Exit();
  }


/* -------------------------------- main --------------------------------- */

  public static void main( String args[] )
  {
//  String server_name =  "mscs138.mscs.uwstout.edu";
    String server_name =  "dmikk.mscs.uwstout.edu";
//  String server_name =  "mandrake.pns.anl.gov";

    System.out.println("====================================================");
    System.out.println("Making retriever to get DataSets from:" + server_name);
    System.out.println("====================================================");
    LiveDataRetriever retriever = new LiveDataRetriever( server_name );

    DataSet     monitor_ds = retriever.getDataSet(0);
    ViewManager monitor_vm = new ViewManager( monitor_ds, IViewManager.IMAGE);

    DataSet     hist_ds = retriever.getDataSet(1);
    ViewManager hist_vm = new ViewManager( hist_ds, IViewManager.IMAGE );

//    retriever.Exit();

  // to verify that the time attribute has been set, uncomment this loop
/*  for ( int i = 0; i < hist_ds.getNum_entries(); i++ )
    {
      Data d = hist_ds.getData_entry(i);
      System.out.println( "For entry " + i + " Time = " + 
                        (String)(d.getAttributeValue(Attribute.UPDATE_TIME)));
     }
*/
    
  }
}

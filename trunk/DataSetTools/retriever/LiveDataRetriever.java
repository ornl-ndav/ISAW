/*
 * @(#)LiveDataRetriever.java      
 *
 * Programmer: Dennis Mikkelson
 *
 *  $Log$
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
 *  This class opens an IPNS Runfile and extracts DataSets corresponding to
 *  monitors and sample histograms.
 */
public class LiveDataRetriever extends    Retriever 
                               implements Serializable
{
  public static final int TIMEOUT_MS       = 60000;

  int     num_data_sets=0;  // the number of distinct DataSets for this server 
                            // monitors and detectors from different histograms
                            // are placed in separate DataSets. 
  TCPComm tcp_io = null;

/**
 *  Construct a LiveDataRetriever for a specific instrument computer.
 *
 *  @param data_source_name  The host name for the instrument computer.  It
 *                           is assumed that a LiveDataServer is running on
 *                           that computer on the desired port. 
 */
  public LiveDataRetriever(String data_source_name) 
  {
    super(data_source_name);

    try
    {
      Socket sock = new Socket( data_source_name, 
                                LiveDataServer.SERVER_PORT_NUMBER );
      tcp_io      = new TCPComm( sock, TIMEOUT_MS );

      num_data_sets = 2;
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
    return num_data_sets;
  }

/**
 * Get the type of the specified data set from the current data source.
 * The type is an integer flag that indicates whether the data set contains
 * monitor data or data from other detectors.
 */

  public int getType( int data_set_num )
  {
    if ( data_set_num == 0 )
      return Retriever.MONITOR_DATA_SET;
    else if ( data_set_num == 1 )
      return Retriever.HISTOGRAM_DATA_SET;
    else
      return Retriever.INVALID_DATA_SET;
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
    if ( data_set_num < 0 || data_set_num > 1 )
      return null;

    boolean request_sent = false;

    try
    {
      if ( data_set_num == 0 )
        tcp_io.Send( new String("GET MONITORS") );
      else
        tcp_io.Send( new String("GET HISTOGRAM") );
      request_sent = true;
    }
    catch ( Exception e )
    {
      System.out.println("EXCEPTION asking for DataSet " + e );
    }
    
    if ( request_sent )
    {
      try
      { 
        Object obj = tcp_io.Receive();
        System.out.println( "Got " + obj );

        DataSet ds = (DataSet)obj;
        return ds;
      }
      catch ( Exception e )
      {
        System.out.println("EXCEPTION receiving DataSet " + e );
      }
    }

    return null;
  }

/**
 *  Break the connection with the LiveDataServer. 
 *
 */
  public void Exit()
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

    DataSet          monitor_ds = retriever.getDataSet(0);
    ViewManager monitor_vm   = new ViewManager( monitor_ds, IViewManager.IMAGE);

    DataSet          hist_ds    = retriever.getDataSet(1);
    ViewManager histogram_vm = new ViewManager( hist_ds, IViewManager.IMAGE );

    retriever.Exit();

/*  // to verify that the time attribute has been set, uncomment this loop
    for ( int i = 0; i < hist_ds.getNum_entries(); i++ )
    {
      Data d = hist_ds.getData_entry(i);
      System.out.println( "For entry " + i + " Time = " + 
                        (String)(d.getAttributeValue(Attribute.CURRENT_TIME)));
     }
*/
    
  }
}

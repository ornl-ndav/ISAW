/*
 * File:  LiveDataRetriever.java      
 *
 * Copyright (C) 2001, Dennis Mikkelson,
 *                     Alok Chatterjee
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
 *  Revision 1.10  2001/06/11 17:58:43  dennis
 *  Made Exit() and MakeConnection() public so that they
 *  can be called from the LiveDataManager.  This was
 *  needed since the connection had be restarted to avoid
 *  a memory leak.
 *
 *  Revision 1.9  2001/06/08 16:15:18  dennis
 *  Now allows the port number to use to be specified after
 *  the dns name with a colon separator.
 *  eg. dmikk.mscs.uwstout.edu:6089
 *
 *  Revision 1.8  2001/06/07 16:39:30  dennis
 *  Now supports reconnection to the LiveDataServer, in case
 *  the connection to the server is lost, or could not be
 *  made when the retriever was constructed.  Added method
 *  isConnected() to report on the status of the connection.
 *  If the connection is lost, this will automatically try
 *  to reconnect whenever some information is requested
 *  from the server.
 *
 *  Revision 1.7  2001/04/25 21:57:49  dennis
 *  Added copyright and GPL info at the start of the file.
 *
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

/* ----------------------------- Constructor ----------------------------- */
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

    MakeConnection();
  }


/* ----------------------------- numDataSets ---------------------------- */
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

/* ------------------------------ getType ------------------------------- */
/**
 * Get the type of the specified data set from the current data source.
 * The type is an integer flag that indicates whether the data set contains
 * monitor data or data from other detectors.
 *
 * @param  data_set_num   Specifies which DataSet's type is requested.
 *
 * @return The type of the specified DataSet, or the flag 
 *         Retriever.INVALID_DATA_SET
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


/* ---------------------------- getDataSet ----------------------------- */
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
    Object obj = getObjectFromServer( 
                           LiveDataServer.COMMAND_GET_DS + data_set_num );
    if ( obj != null && obj instanceof DataSet )
      return (DataSet)obj;

    return (DataSet)(DataSet.EMPTY_DATA_SET.clone());
  }


/* ---------------------------- isConnected ----------------------------- */
/**
 *  Check to see if the connection to the remote server has been made and
 *  is still operating;
 *
 *  @return Returns true if the connection to the remote server is still
 *          operating.
 */
  public boolean isConnected()
  {
    if ( tcp_io == null )
      return false;
    
    return true;
  }


/* --------------------------- MakeConnection ---------------------------- */
/**
 *  Connect with the remote live data server.
 */
  public boolean MakeConnection()
  {
    int    port;
    String remote_machine;

    int colon_index = data_source_name.indexOf(':');
    if ( colon_index < 0 )
    {
      remote_machine = data_source_name;
      port = LiveDataServer.DEFAULT_SERVER_PORT_NUMBER;
    }
    else
    {
      remote_machine = data_source_name.substring( 0, colon_index );
      String port_name = data_source_name.substring( colon_index+1 );
      port_name.trim();
      port = Integer.parseInt( port_name );
    }

    try
    {
      Socket sock = new Socket( remote_machine, port );
      tcp_io      = new TCPComm( sock, TIMEOUT_MS );
      return true;
    }
    catch( Exception e )
    {
      tcp_io = null;
      System.out.println( "LiveDataRetriever CONNECTION TO " +
                           remote_machine + " FAILED ON PORT " + port );
      System.out.println( "Exception is " +  e );
      return false;
    }
  }


/* -------------------------------- Exit --------------------------------- */
/**
 *  Break the connection with the LiveDataServer. 
 */
  public void Exit()
  {
    if ( tcp_io != null )
      try
      {
        tcp_io.Send( new TCPCommExitClass() );
      }
      catch ( Exception e )
      {
        System.out.println( "Exception in LiveDataRetriever.Exit():" + e );
      }
  }


/* ------------------------- getObjectFromServer ------------------------- */
/**
 *  Send command to server and get it's response as an object.
 *
 *  @param command   The command string to send to the server.  Appropriate
 *                   commands are give as strings in LiveDataServer.java
 *
 *  @return The object that was requested from the server, or null if the
 *          the server was not running, or could not provide the requested
 *          object.
 *
 */
 synchronized private Object getObjectFromServer( String command )
 {
    if ( tcp_io == null )
      MakeConnection();

    if ( tcp_io == null )
    {
      System.out.println("LiveDataRetriever can't send command:" + command );
      return null;
    }

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
      tcp_io = null;                               // the connection is gone
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

/* ------------------------------------------------------------------------
 *
 *  PROTECTED METHODS
 *
 */

/* ------------------------------ finalize ---------------------------- */
/**
 *  Finalize method to make sure that the TCP connection is closed if this
 *  LiveDataRetriever object is no longer being used.
 */
  protected void finalize() throws IOException
  {
     System.out.println("Retriever finalization");
     Exit();
  }


/* ------------------------------------------------------------------------
 *
 *  MAIN PROGRAM FOR TESTING PURPOSES ONLY 
 *
 */

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

/*
 * File:  RemoteDataRetriever.java      
 *
 * Copyright (C) 2001, Dennis Mikkelson,
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
 *  Revision 1.16  2004/03/15 06:10:51  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.15  2003/05/28 20:49:23  pfpeterson
 *  Changed System.getProperty to SharedData.getProperty
 *
 *  Revision 1.14  2003/03/07 22:47:55  dennis
 *  Made convenience methods for getting particular commands
 *  into static methods.
 *  Added support for GET_DS_ID_RANGE and GET_DS_X_RANGE
 *
 *  Revision 1.13  2003/03/05 20:46:49  dennis
 *  Changed default attribute mode to FULL_ATTRIBUTES in the utility
 *  methods that build Command objects.
 *  Added method to make a GetDataCommand object, taking all options
 *  as parameters.
 *
 *  Revision 1.12  2003/03/04 20:27:52  dennis
 *  Now resets "server_alive" flag to false if the connection to the
 *  server is lost.
 *
 *  Revision 1.11  2003/02/24 21:09:43  dennis
 *  Moved STATUS string from TCPComm to TCPServer
 *
 *  Revision 1.10  2003/02/24 20:47:40  dennis
 *  Now checks that reply to status request starts with "Status:"
 *
 *  Revision 1.9  2003/02/24 13:42:31  dennis
 *  Switched to use CommandObject instead of compound String command.
 *
 *  Revision 1.8  2002/11/27 23:23:16  pfpeterson
 *  standardized header
 *
 */
package DataSetTools.retriever;

import DataSetTools.dataset.*;
import DataSetTools.util.SharedData;
import java.io.*;
import java.net.*;
import NetComm.*;

/**
 *  This class is a base class for retrievers that get data from a 
 *  NetComm.DataServer using a TCP connection.  It takes care of 
 *  the connection and communication with the remote server. 
 */
abstract public class RemoteDataRetriever extends    Retriever 
                                          implements Serializable
{
  public static final int TIMEOUT_MS           = 60000;
  public static final int BAD_FILE_NAME        = -1;
  public static final int BAD_USER_NAME        = -2;
  public static final int BAD_PASSWORD         = -3;
  public static final int SERVER_DOWN          = -4;
  public static final int WRONG_SERVER_TYPE    = -5;
  public static final int DATA_OLD             = -6;
  public static final int DATA_LIVE            = -7;
  public static final int DAS_OFFLINE          = -8;
  public static final int NOT_CONNECTED        = -9;

  public static final String SERVER_OK_STRING         = "Server OK";
  public static final String SERVER_ERROR_STRING      = "Server Error: ";
  public static final String NO_DATA_SETS_STRING      = "No DataSets: ";
  public static final String BAD_FILE_NAME_STRING     = "File Not Found";
  public static final String BAD_USER_NAME_STRING     = "User Not Found";
  public static final String BAD_PASSWORD_STRING      = "Wrong Password";
  public static final String SERVER_DOWN_STRING       = "Server Down";
  public static final String WRONG_SERVER_TYPE_STRING = "Wrong Server Type";
  public static final String DATA_OLD_STRING          = "Data OLD: ";
  public static final String DATA_LIVE_STRING         = "Data Live: ";
  public static final String DAS_OFFLINE_STRING       = "DAS STOPPED: ";
  public static final String NOT_CONNECTED_STRING     = "NOT Connected";

  private static final String error_messages[] = { NO_DATA_SETS_STRING,
                                                   BAD_FILE_NAME_STRING,
                                                   BAD_USER_NAME_STRING,
                                                   BAD_PASSWORD_STRING,
                                                   SERVER_DOWN_STRING,
                                                   WRONG_SERVER_TYPE_STRING,
                                                   DATA_OLD_STRING,
                                                   DATA_LIVE_STRING,
                                                   DAS_OFFLINE_STRING,
                                                   NOT_CONNECTED_STRING };
  TCPComm tcp_io = null;

  protected boolean  debug_remote = false;
  protected boolean  server_alive = false;
  protected String   password     = "RemoteDataRetriever";
  protected String   file_name    = "";

  private   String remote_machine = "";
  private   int    port           = DataSetServer.DEFAULT_SERVER_TCP_PORT;
  private   String user_name      = "RemoteDataRetriever";

/* ----------------------------- Constructor ----------------------------- */
/**
 *  Construct a RemoteDataRetriever to get data from a DataSetServer
 *
 *  @param data_source_name  String containing the host name, port, password
 *                           and file_name for the server.
 */
  public RemoteDataRetriever( String data_source_name ) 
  {
    super(data_source_name);

    dataSource source = new dataSource( data_source_name );
    remote_machine = source.getMachine();
    user_name      = source.getUserName();
    password       = source.getPassWord();
    file_name      = source.getFileName();
    if ( source.getPort() > 0 )
      port           = source.getPort();

    MakeConnection();
  }

/* ----------------------------- error_message -------------------------- */
/**
 *  Return the error String associated with a given error flag, as returned
 *  by the numDataSets() method.  
 */
 public static String error_message( int error_flag )
 {
   if ( error_flag > 0 )
     return SERVER_OK_STRING;

   if ( error_flag <= 0 && error_flag > -error_messages.length )
     return error_messages[ -error_flag ];

   return ( SERVER_ERROR_STRING + error_flag );
 }
  

/* ------------------------------- status ------------------------------- */
/**
 *  Get a status message for the current retriever state.  By default, this
 *  just returns a string message corresponding to an error code obtained
 *  from the numDataSets method.  
 */
 public String status()
 {
   return error_message( numDataSets() );
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
    try
    {
      Socket sock = new Socket( remote_machine, port );
      tcp_io      = new TCPComm( sock, TIMEOUT_MS );

      Object obj = getObjectFromServer( getStatus() );

      if ( obj == null || !(obj instanceof String) )
      {
        if ( obj == null )
          server_alive = false;
        return false; 
      }
      else
        server_alive = true;

      String answer = (String)obj;
      if ( answer.startsWith( TCPServer.STATUS ) )
      {
        server_alive = true;
        return true;
      }
      else
      {
        if ( debug_remote )
          System.out.println("ERROR: status request failed for " + 
                              data_source_name );
        return false;
      }
    }
    catch( Exception e )
    {
      tcp_io = null;
      if ( debug_remote )
      {
        System.out.println( "RemoteDataRetriever CONNECTION TO " +
                             remote_machine + " FAILED ON PORT " + port );
        System.out.println( "Exception is " +  e );
      }
      server_alive = false;
      return false;
    }
  }


/* -------------------------------- Exit --------------------------------- */
/**
 *  Break the connection with the DataSetServer. 
 */
  public void Exit()
  {
    if ( tcp_io != null )
    {
      try
      {
        tcp_io.Send( new TCPCommExitClass() );
      }
      catch ( Exception e )
      {
        if ( debug_remote )
          System.out.println( "Exception in RemoteDataRetriever.Exit():" + e );
      }

      tcp_io = null;
    }
    server_alive = false;
  }


/* ------------------------- getObjectFromServer ------------------------- */
/**
 *  Send command to server and get it's response as an object.
 *
 *  @param command   The CommandObject to send to the server. 
 *
 *  @return The object that was requested from the server, or null if the
 *          the server was not running, or could not provide the requested
 *          object.
 */
 synchronized protected Object getObjectFromServer( CommandObject command )
 {
    if ( debug_remote )
      System.out.println("getObjectFromServer called with " + command );

    if ( tcp_io == null )
      MakeConnection();

    if ( tcp_io == null )
    {
      if ( debug_remote )
      {
        System.out.println("RemoteDataRetriever can't send command:" + command);
        System.out.println("TCP Connection is null to " + data_source_name );
      }
      return null;
    }

    if ( debug_remote )
      System.out.println("RemoteDataRetriever sending command:" + command );

    boolean request_sent = false;

    try
    {
      if ( debug_remote )
        System.out.println( "Command sent: " + command );
      tcp_io.Send( command );
      request_sent = true;
    }
    catch ( Exception e )
    {
      if ( debug_remote )
      {
        System.out.println("EXCEPTION in RemoteDataRetriever:" + e );
        System.out.println("while sending command: " + command );
      }
      tcp_io = null;                               // the connection is gone
    }

    if ( request_sent )
    {
      try
      {
        Object obj = null;
        obj = tcp_io.Receive();

        if ( debug_remote )
        {
          System.out.println("RemoteDataRetriever finished command:"+command );
          System.out.println("...got back object " + obj );
        }
        return obj;
      }
      catch ( Exception e )
      {
        System.out.println("EXCEPTION in RemoteDataRetriever:" + e );
        System.out.println("while receiving response to command: " + command );
      }
    }

    if ( debug_remote )
      System.out.println("RemoteDataRetriever failed command:" + command );
    return null;
  }

/* ------------------------------------------------------------------------
 *
 *  PROTECTED METHODS
 *
 */

/**
 *  Get a CommandObject configured with the GET_STATUS command
 *
 *  @return a CommandObject requesting the status of the server.
 */
  static public CommandObject getStatus()
  {
    String user_name = SharedData.getProperty("user.name");    
    String password  = "dummy password";
    return new CommandObject( CommandObject.GET_STATUS, user_name, password );
  }


/**
 *  Get a GetDataCommand object configured with the GET_DS_TYPES command.
 *  If the server is the LiveDataServer, the file name is ignored.
 *
 *  @param  file_name  String containing the fully qualified name of the
 *                     file. 
 *
 *  @return a GetDataCommand requesting the list of DataSet types from 
 *          a remote data server.
 */
  static public GetDataCommand getDS_Types( String file_name )
  {
    String user_name = SharedData.getProperty("user.name");
    String password  = "dummy password";
    return new GetDataCommand( CommandObject.GET_DS_TYPES,      
                              user_name, password, 
                              file_name, 
                              0, 
                              CommandObject.ALL_IDS,
                              0,0, 
                              1, 
                              Attribute.FULL_ATTRIBUTES );
  }

/**
 *  Get a GetDataCommand object configured with the GET_DS_NAME command.  
 *  If the server is the LiveDataServer, the file name is ignored. 
 *
 *  @param  file_name  String containing the fully qualified name of the
 *                     file. 
 *
 *  @param  ds_num  the number of the DataSet whose name is requested.
 * 
 *  @return a GetDataCommand requesting the name of a DataSet from 
 *          a remote data server.  
 */ 
  static public GetDataCommand getDS_Name( String file_name, int ds_num )
  {
    String user_name = SharedData.getProperty("user.name");
    String password  = "dummy password";
    return new GetDataCommand( CommandObject.GET_DS_NAME,      
                              user_name, password, 
                              file_name, 
                              ds_num, 
                              CommandObject.ALL_IDS,
                              0,0, 
                              1, 
                              Attribute.FULL_ATTRIBUTES );
  }


/**
 *  Get a GetDataCommand object configured with the GET_DS_ID_RANGE command.
 *  If the server is the LiveDataServer, the file name is ignored.
 *
 *  @param  file_name  String containing the fully qualified name of the
 *                     file.
 *
 *  @param  ds_num  the number of the DataSet whose ID range is requested.
 *
 *  @return a GetDataCommand requesting the range of IDs of a DataSet from
 *          a remote data server.
 */
  static public GetDataCommand getDS_ID_Range( String file_name, int ds_num )
  {
    String user_name = SharedData.getProperty("user.name");
    String password  = "dummy password";
    return new GetDataCommand( CommandObject.GET_DS_ID_RANGE,
                               user_name, password,
                               file_name,
                               ds_num,
                               CommandObject.ALL_IDS,
                               0,0,
                               1,
                               Attribute.FULL_ATTRIBUTES );
  }


/**
 *  Get a GetDataCommand object configured with the GET_DS_X_RANGE command.
 *  If the server is the LiveDataServer, the file name is ignored.
 *
 *  @param  file_name  String containing the fully qualified name of the
 *                     file.
 *
 *  @param  ds_num  the number of the DataSet whose X range is requested.
 *
 *  @return a GetDataCommand requesting the range of X-values of a DataSet from
 *          a remote data server.
 */
  static public GetDataCommand getDS_X_Range( String file_name, int ds_num )
  {
    String user_name = SharedData.getProperty("user.name");
    String password  = "dummy password";
    return new GetDataCommand( CommandObject.GET_DS_X_RANGE,
                               user_name, password,
                               file_name,
                               ds_num,
                               CommandObject.ALL_IDS,
                               0,0,
                               1,
                               Attribute.FULL_ATTRIBUTES );
  }


/**
 *  Get a GetDataCommand object configured to get an entire DataSet.  If the
 *  server is a LiveDataServer, the file name is ignored.
 *
 *  @param  file_name  String containing the fully qualified name of the
 *                     file. 
 *
 *  @param  ds_num  the number of the DataSet requested.
 * 
 *  @return a GetDataCommand requesting a complete DataSet from       
 *          a remote data server.
 */ 
  static public GetDataCommand getDS( String file_name, int ds_num )
  {
    String user_name = SharedData.getProperty("user.name");
    String password  = "dummy password";
    return new GetDataCommand( CommandObject.GET_DS, 
                               user_name, password,
                               file_name,
                               ds_num,
                               CommandObject.ALL_IDS,
                               0,0, 
                               1, 
                               Attribute.FULL_ATTRIBUTES );
  }


/**
 *  Get a GetDataCommand object configured to get a specified portion of the 
 *  specified DataSet.  If the server is a LiveDataServer, the file name is
 *  ignored.
 *
 *  @param  file_name     String containing the fully qualified name of the
 *                        file.
 *  @param  ds_num        The number of the DataSet to be returned.
 *  @param  group_ids     String listing the required group IDs.
 *  @param  min_x         The start of the interval of x values for which
 *                        the data is needed.
 *  @param  max_x         The end of the interval of x values for which
 *                        the data is needed.
 *  @param  rebin_factor  Integer giving the number of adjacent bins that
 *                        should be combined.
 *  @param  attr_mode     Integer code for number of attributes requested,
 *                        one of:
 *                           Attribute.NO_ATTRIBUTES
 *                           Attribute.ANALYSIS_ATTRIBUTES
 *                           Attribute.FULL_ATTRIBUTES
 *
 *  @return a GetDataCommand requesting a portion of a DataSet from       
 *          a remote data server.
 */
  static public GetDataCommand getDataSet( String file_name,
                                           int    ds_num,
                                           String group_ids,
                                           float  min_x,
                                           float  max_x,
                                           int    rebin_factor,
                                           int    attr_mode      )
  {
    String user_name = SharedData.getProperty("user.name");
    String password  = "dummy password";
    return new GetDataCommand( CommandObject.GET_DS,
                               user_name, password,
                               file_name,
                               ds_num,
                               group_ids,
                               min_x, max_x,
                               rebin_factor,
                               attr_mode );
  }


/* ------------------------------ finalize ---------------------------- */
/**
 *  Finalize method to make sure that the TCP connection is closed if this
 *  RemoteDataRetriever object is no longer being used.
 */
  protected void finalize() throws IOException
  {
     if ( debug_remote )
       System.out.println("Retriever finalization");

     if ( isConnected() )
       Exit();
  }

}

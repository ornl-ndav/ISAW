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
 *  Revision 1.7  2001/08/14 19:16:30  dennis
 *  Changed words used for some error messages.
 *
 *  Revision 1.6  2001/08/14 15:12:38  dennis
 *  Added status messages for LiveDataServer status.
 *
 *  Revision 1.5  2001/08/13 23:29:59  dennis
 *  Now has separate error messages for bad user name and bad password.
 *  Added String form for error messages, and method error_message()
 *  to convert from error code to String form.
 *  Added implementation of a status() method.
 *
 *  Revision 1.4  2001/08/10 19:50:55  dennis
 *  Switched to new name for the default tcp server port.
 *
 *  Revision 1.3  2001/08/09 15:24:06  dennis
 *  Put debug prints in "if (debug_retriever)" blocks.
 *
 *  Revision 1.2  2001/08/07 21:34:00  dennis
 *  Added WRONG_SERVER_TYPE error code.
 *
 *  Revision 1.1  2001/08/03 21:39:36  dennis
 *  Base class for retrievers that get DataSets from a
 *  NetComm.DataServer.  Manages connections and communication
 *  with the server.
 *
 *
 */
package DataSetTools.retriever;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import java.io.*;
import java.net.*;
import java.util.*;
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

  protected boolean  server_alive = false;
  protected boolean  user_ok      = false;
  protected boolean  password_ok  = false;
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
    server_alive = false;
    user_ok      = false;
    password_ok  = false;

    try
    {
      Socket sock = new Socket( remote_machine, port );
      tcp_io      = new TCPComm( sock, TIMEOUT_MS );

      Object obj = 
               getObjectFromServer( DataSetServer.COMMAND_USER_IS + user_name );

      if ( obj == null || !(obj instanceof String) )
        return false; 
      else
        server_alive = true;

      String answer = (String)obj;
      if ( answer.equals( DataSetServer.ANSWER_OK ) )
        user_ok = true;
      else
      {
        System.out.println("ERROR: user name not accepted by server " +
                            data_source_name );
        return false;
      }

      answer = (String)
           getObjectFromServer( DataSetServer.COMMAND_PASSWORD_IS + password );
      if ( answer.equals( DataSetServer.ANSWER_OK ) )
        password_ok = true;
      else
      {
        System.out.println("ERROR: password not accepted by server " +
                            data_source_name );
        return false;
      }

      return true;
    }
    catch( Exception e )
    {
      tcp_io = null;
      if ( debug_retriever )
      {
        System.out.println( "RemoteDataRetriever CONNECTION TO " +
                             remote_machine + " FAILED ON PORT " + port );
        System.out.println( "Exception is " +  e );
      }
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
        if ( debug_retriever )
          System.out.println( "Exception in RemoteDataRetriever.Exit():" + e );
      }

      tcp_io = null;
    }
    server_alive = false;
    user_ok      = false;
    password_ok  = false;
  }


/* ------------------------- getObjectFromServer ------------------------- */
/**
 *  Send command to server and get it's response as an object.
 *
 *  @param command   The command string to send to the server.  Appropriate
 *                   commands are given as strings in NetComm.DataSetServer
 *
 *  @return The object that was requested from the server, or null if the
 *          the server was not running, or could not provide the requested
 *          object.
 *
 */
 synchronized protected Object getObjectFromServer( String command )
 {
    if ( debug_retriever )
      System.out.println("getObjectFromServer called with " + command );

    if ( tcp_io == null )
      MakeConnection();

    if ( tcp_io == null )
    {
      if ( debug_retriever )
      {
        System.out.println("RemoteDataRetriever can't send command:" + command);
        System.out.println("TCP Connection is null to " + data_source_name );
      }
      return null;
    }

    if ( debug_retriever )
      System.out.println("RemoteDataRetriever sending command:" + command );

    boolean request_sent = false;

    try
    {
      if ( debug_retriever )
        System.out.println( "Command sent: " + command );
      tcp_io.Send( command );
      request_sent = true;
    }
    catch ( Exception e )
    {
      if ( debug_retriever )
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
        if ( debug_retriever )
          System.out.println( "Got " + obj );

        if ( debug_retriever )
          System.out.println("RemoteDataRetriever finished command:"+command );
        return obj;
      }
      catch ( Exception e )
      {
        System.out.println("EXCEPTION in RemoteDataRetriever:" + e );
        System.out.println("while receiving response to command: " + command );
      }
    }

    if ( debug_retriever )
      System.out.println("RemoteDataRetriever failed command:" + command );
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
 *  RemoteDataRetriever object is no longer being used.
 */
  protected void finalize() throws IOException
  {
     if ( debug_retriever )
       System.out.println("Retriever finalization");

     if ( isConnected() )
       Exit();
  }

}

/*
 * File: TCPClient.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 *  Revision 1.1  2003/10/21 21:20:29  dennis
 *  Base class for clients that communicate with TCPServers.
 *
 */
package NetComm;

import java.io.*;
import java.util.*;
import java.net.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;
import DataSetTools.operator.*;

/**
 *  This is the base class for classes that communicate with a TCPServer.
 */
public class TCPClient
{
  protected  TCPComm  tcp_io       = null;
  protected  int      port         = 6091;
  protected  String   host         = "127.0.0.1";
  protected  boolean  server_alive = false;
  protected  boolean  debug_remote = false;

  
  /* -------------------------------- setHost --------------------------- */
  /**
   *  Set the host on which the contact the TCPServer. 
   */
   public void setHost( String host )
   {
     this.host = host;
   }


  /* -------------------------------- getHost --------------------------- */
  /**
   *  Get the host which is being used for the TCPServer. 
   *
   *  @return host  The name of the host being used.
   */
   public String getHost()
   {
      return( host );
   }


  /* -------------------------------- setPort --------------------------- */
  /**
   *  Set the port to be used to communicate with the TCPServer
   */
   public void setPort( int port )
   {
      this.port = port;
   }


  /* -------------------------------- getPort --------------------------- */
  /**
   *  Get the port being used to communicate with the TCPServer
   *
   *  @return the port number being used.
   */
   public int getPort()
   {
      return( port );
   }

  /* --------------------------- MakeConnection ---------------------------- */
  /**
   *  Connect with the TCPServer.
   */
   public boolean MakeConnection()
   {
     try
     {
       Socket sock = new Socket( host, port );
       tcp_io      = new TCPComm( sock, RemoteDataRetriever.TIMEOUT_MS );
 
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
                               host + " ON PORT " + port );
         return false;
       }
     }
     catch( Exception e )
     {
       tcp_io = null;
       if ( debug_remote )
       {
         System.out.println( "TCPClient CONNECTION TO " +
                              host + " FAILED ON PORT " + port );
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
          System.out.println( "Exception in TCPClient.Exit():" + e );
       }

       tcp_io = null;
     }
     server_alive = false;
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


  /* ------------------------------------------------------------------------
   *
   * PROTECTED METHODS
   *  
   */

  /* ------------------------------ finalize ---------------------------- */
  /**
   *  Finalize method to make sure that the TCP connection is closed if this
   *  TCPClient object is no longer being used.
   */
   protected void finalize() throws IOException
   {
     if ( debug_remote )
       System.out.println("TCPClient finalization");

     if ( isConnected() )
       Exit();
   }


  /* ------------------------ showClientUsage -------------------------- */
  /**
   *  Print list of supported commands.
   */
   protected static void showClientUsage()
   {
     System.out.println("  -h,-H            Help");
     System.out.println("  -T<port>         Set TCP port to use");
     System.out.println("  -S<name>         server host name");
   }


  /* ------------------------- parseArgs ----------------------------- */
  /**
   *  Parse a list of command line arguments to extract values for the
   *  server name, log file name and TCP port.  This will typically be
   *  used instead of separately calling the methods to set these individually,
   *  if the values are specified on a command line.  Commands supported at
   *  this level are -L, -S, -T to specify the Logfile name, Server name and
   *  TCP port respectively.  Commands to control usernames and passwords
   *  may be added to this level later.
   *
   *  @param args  Array of strings from the command line, containing
   *               command characters and arguments.
   */
   protected void parseArgs( String args[] )
   {
     if ( StringUtil.commandPresent("-h", args ) ||
          StringUtil.commandPresent("-H", args )  )
     {
       showClientUsage();
       System.exit(1);
     }

     String new_host = StringUtil.getCommand( 1, "-S", args );
     if ( new_host.length() > 0 )
       setHost( new_host );

     String command = StringUtil.getCommand( 1, "-T", args );
     if ( command.length() > 0 )
     {
       try
       {
         int value = Integer.parseInt(command);
         setPort( value );
       }
       catch ( NumberFormatException e )
       {
          System.out.println("Failed to set port number, using " + port );
       }
     }
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
        System.out.println("TCPClient can't send command:" + command);
        System.out.println("TCPClient Connection is null to " + host +
                           " on port " + port );
      }
      return null;
    }

    if ( debug_remote )
      System.out.println("TCPClient sending command:" + command );

    boolean request_sent = false;
    try
    {
      if ( debug_remote )
        System.out.println( "TCPClient Command sent: " + command );
      tcp_io.Send( command );
      request_sent = true;
    }
    catch ( Exception e )
    {
      if ( debug_remote )
      {
        System.out.println("EXCEPTION in TCPClient:" + e );
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
          System.out.println("TCPClient finished command:"+command );
          System.out.println("...got back object " + obj );
        }
        return obj;
      }
      catch ( Exception e )
      {
        System.out.println("EXCEPTION in TCPClient:" + e );
        System.out.println("while receiving response to command: " + command );
      }
    }

    if ( debug_remote )
      System.out.println("TCPClient failed command:" + command );
    return null;
  }
 

  /* ------------------------------ getStatus -------------------------- */
  /**
   *  Get a CommandObject configured with the GET_STATUS command
   *
   *  @return a CommandObject requesting the status of the server.
   */
   static protected CommandObject getStatus()
   {
     String user_name = SharedData.getProperty("user.name");
     String password  = "dummy password";
     return new CommandObject( CommandObject.GET_STATUS, user_name, password );
   }


  /* ------------------------------ main ------------------------------- */
  /**
   *  Main program to run client in stand alone mode for testing purposes.
   */
   public static void main( String args[] )
   {
      TCPClient client = new TCPClient();

      client.parseArgs( args );

      client.MakeConnection();
      if ( client.isConnected() )
        System.out.println("Connection OK");
      else
        System.out.println("Connection failed to " + client.getHost() +
                           " on port " + client.getPort() );
   }

}


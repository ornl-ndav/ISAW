/*
 * File: TCPServer.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 *  Revision 1.9  2002/11/27 23:27:59  pfpeterson
 *  standardized header
 *
 */
package NetComm;

import java.io.*;
import java.util.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;

/**
 *  This is a base class for servers that receive TCP requests from clients
 *  and responds.  It logs requests from distinct clients/users, checks
 *  passwords (not fully implemented) and passes requests on to the 
 *  ProcessCommand() method.  The ProcessCommand() method must be implemented
 *  in a derived class to provide the needed functionality.
 *
 *  @see ITCPUser
 *  @see TCPComm
 *  @see ThreadedTCPComm
 *  @see TCPServiceInit 
 */

public class TCPServer implements ITCPUser
{
  public static final String COMMAND_GET_STATUS   = "COMMAND:GET_STATUS ";
  public static final String COMMAND_PASSWORD_IS  = "COMMAND:PASSWORD_IS ";
  public static final String COMMAND_USER_IS      = "COMMAND:USER_IS ";
  public static final String COMMAND_GET_DATA_NAME= "COMMAND:GET_DATA_NAME ";
  public static final String ANSWER_OK            = "OK";
  public static final String ANSWER_NOT_OK        = "Not_OK";

  public static final String UNKNOWN_USER         = "UNKNOWN";
  public static final String FAIL_STRING          = "WRITE FAIL";
  public static final String EXIT_STRING          = "EXIT";
  public static final String INVALID_STRING       = "Bad user_name or password";
  public static final String INVALID_COMMAND      = "Invalid Command";

  public static final int    DEFAULT_SERVER_TCP_PORT = 6088;
  public static final String DEFAULT_PASSWORD        = "IPNS";

  public static boolean debug_server = false;

                                            // NOTE: password & user checking
                                            //       is not yet impelemented.
  private    boolean password_ok = false;
  private    boolean user_ok     = false;
  private    String  user_name   = UNKNOWN_USER;

  private    Hashtable log;
  private    String    log_filename     = "TCPServerLog.txt";
  private    String    server_name      = "TCPServer";
  private    int       current_tcp_port = DEFAULT_SERVER_TCP_PORT;
  private    String    start_time       = "";
  protected  String    data_name        = "NONE";   // identifier for last
                                                    // data processed.   

  /* ---------------------------- Constructor -------------------------- */
  /**
   *  Construct a TCPServer.
   */
   public TCPServer()
   {
     log = new Hashtable();
     start_time = DateUtil.default_string();
   }

  /* --------------------------- setServerName ------------------------- */
  /**
   *  Set name for this server, to use in the log file.
   *
   *  @param  name  The new name of this server that will be  entered in 
   *                the log file. 
   */
   public void setServerName( String name )
   {
     if ( name.length() > 0 )
       server_name = name;
   }

  /* --------------------------- getServerName ------------------------- */
  /**
   *  Get name for this server that will be used in the log file.
   *
   */
   public String getServerName()
   {
     return server_name;
   }


  /* --------------------------- setLogFilename ------------------------- */
  /**
   *  Set name of the log file for this server.
   *
   *  @param  name  The new name of the log file for this server.
   */
   public void setLogFilename( String name )
   {
     if ( name.length() > 0 )
       log_filename = name;
   }


  /* --------------------------- getLogFilename ------------------------- */
  /**
   *  Get name of the log file used for this run of the server.
   *
   */
   public String getLogFilename()
   {
     return log_filename;
   }


  /* ---------------------------- setTCPport --------------------------- */
  /**
   *  Set the TCP port number to use when the server is started.  To be 
   *  effective this must be called before starting the server and the 
   *  specified port number must be positive.
   *
   *  @param  port  The port number to used when the server is started.
   */
  public void setTCPport( int port )
  {
     if ( port <= 0 )
       current_tcp_port = DEFAULT_SERVER_TCP_PORT;
     else
       current_tcp_port = port;
  }


  /* ---------------------------- getTCPport --------------------------- */
  /**
   *  Get the TCP port used for this run of the server.
   *
   public String getTCPport()
   {
     return current_tcp_port;
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
   public void parseArgs( String args[] )
   {
     if ( StringUtil.commandPresent("-h", args ) ||
          StringUtil.commandPresent("-H", args )  )
       showTCPServerUsage();

     setLogFilename( StringUtil.getCommand( 1, "-L", args ) );
     setServerName( StringUtil.getCommand( 1, "-S", args ) );

     String command = StringUtil.getCommand( 1, "-T", args );
     if ( command.length() > 0 )
     {
       try
       {
         int value = Integer.parseInt(command);
         setTCPport( value ); 
       }
       catch ( NumberFormatException e )
       {
       }
     }
   } 


  /* ------------------------ showTCPServerUsage -------------------------- */
  /**
   *  Print list of supported commands.
   */  
   public static void showTCPServerUsage()
   {
     System.out.println("  -h,-H            Help");
     System.out.println("  -T<port>         Set TCP port to use");
     System.out.println("  -L<file name>    Set name of log file");
     System.out.println("  -S<name>         Set name for server in log file");
   }


  /* ----------------------------- startTCP --------------------------- */
  /**
   *  Start the TCP server running, listening for clients on the last
   *  port that was specified using setTCPport, or the default port if
   *  none was specified.
   */
   public void startTCP( )
   {
     System.out.println("Starting TCP server on port " + current_tcp_port );
     TCPServiceInit TCPinit;
     TCPinit = new TCPServiceInit( this, current_tcp_port );
     TCPinit.start();
   }


  /* -------------------------- ProcessCommand -------------------------- */
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
      try
      {                                                // handle GET_STATUS, but
                                                       // allow derived classes
                                                       // to override it.
        if ( command.startsWith( COMMAND_GET_STATUS) )
        {
          if ( !user_ok )
            tcp_io.Send( RemoteDataRetriever.BAD_USER_NAME_STRING );

          else if ( !password_ok )
            tcp_io.Send( RemoteDataRetriever.BAD_PASSWORD_STRING );

          else 
            tcp_io.Send( RemoteDataRetriever.SERVER_OK_STRING );
        } 
        else 
          tcp_io.Send( ANSWER_NOT_OK );      // no operation was carried out
      }
      catch ( Exception e )
      {
        System.out.println("Error: TCPServer command: " + command);
        System.out.println("Error: couldn't send data "+e );
      }
   }


 /* --------------------------- ProcessData ------------------------------ */
 /**
  *  Method to process data from a TCP client.  This is the method needed
  *  to implement the ITCPUser interface.  It is called whenever an Object
  *  is received from a TCP client.  The Object should consist of a String
  *  command, requiring a response from the server.
  *
  *  @param  data_obj   The object sent by the client.  This must be a
  *                     string in order to be processed by this method.
  *
  *  @param  tcp_io     The TCP communications object to which the response
  *                     must be sent.
  */
  synchronized public void ProcessData(Object data_obj, ThreadedTCPComm tcp_io)
  {

    if ( data_obj instanceof String )
    {
      String command = (String)data_obj; 
      if ( debug_server )
        System.out.println("Received request " + command );
      try
      {
        if ( command.startsWith( COMMAND_USER_IS ))  
        {
          MakeLogEntry( command, tcp_io.getInetAddressString(), false );
          user_name = getArgument( command );
          user_ok = true;
          tcp_io.Send( ANSWER_OK );
          return;
        } 

        else if ( command.startsWith( COMMAND_PASSWORD_IS ))
        { 
                                             // don't put password in log file
          MakeLogEntry( COMMAND_PASSWORD_IS,  
                        tcp_io.getInetAddressString(),  
                        false );
                                             // stub, replace with method
                                             // to check password for validity
          String password = getArgument( command );
          boolean valid_password = true;     
          if ( valid_password )             
          {
            tcp_io.Send( ANSWER_OK );
            password_ok = true;
          }
          else
          {
            tcp_io.Send( ANSWER_NOT_OK );
            password_ok = false;
          }
          return;
        }

        else if ( command.startsWith( COMMAND_GET_DATA_NAME ))
        {
          MakeLogEntry( command, tcp_io.getInetAddressString(), false );
          if ( user_ok && password_ok )
            tcp_io.Send( data_name );
          else
            tcp_io.Send( ANSWER_NOT_OK );          
          return;
        }


        if ( user_ok && password_ok )
        {
          MakeLogEntry( command, tcp_io.getInetAddressString(), false );
          ProcessCommand( command, tcp_io );
        }

        else                                           // break connection
        {
          if ( !user_ok )
            System.out.println("ERROR: user name not valid in TCPServer");

          if ( !password_ok )
            System.out.println("ERROR: password not valid in TCPServer");

          reset_user_info();
          tcp_io.Send( new TCPCommExitClass() );
          MakeLogEntry( INVALID_STRING + " for " + command,
                        tcp_io.getInetAddressString(), 
                        true );
        }

      }
      catch ( Exception e )
      {
        System.out.println("Error: TCPServer command: " + command);
        System.out.println("Error: couldn't send data " + e ); 
        System.out.println( "Exception is " + e );
        e.printStackTrace();

        MakeLogEntry( FAIL_STRING + " for " + command,
                      tcp_io.getInetAddressString(), 
                      true );
        reset_user_info();
      }  
    }

    else if ( data_obj instanceof TCPCommExitClass )
    {
      if ( debug_server )
        System.out.println("Exit received in TCPServer");

      MakeLogEntry( EXIT_STRING, tcp_io.getInetAddressString(), true);
    }

    else
      MakeLogEntry( INVALID_COMMAND, tcp_io.getInetAddressString(), true );

  }


  /* ---------------------------- getArgument --------------------------- */
  /**
   *  Get the argument String from a command string, assuming that the 
   *  command name is the first sequence of non-blank characters in the
   *  command and that the argument string follows the first blank character.
   *
   *  @param command  String containg a command name followed by a blank and
   *                  a string arugument to the command.
   */
  public String getArgument( String command )
  {
    String argument = "";

    int space_index = command.indexOf(' ');
    if ( space_index > 0 )
    {
      argument = command.substring( space_index + 1 );
      argument = argument.trim();
    }

    return argument;
  }


  /* ----------------------------------------------------------------------
   *
   *  PRIVATE METHODS
   *
   */

  /* ---------------------------- MakeLogEntry --------------------------- */
  /**
   *  Make an entry in the log for the specified inet address for the 
   *  current user and date.
   */
  private void MakeLogEntry( String  command,
                             String  inet_address, 
                             boolean write_file )
  {
    String log_key  = user_name + "@" + inet_address;
    int    requests = 1;

    LogEntry log_entry = (LogEntry)log.get( log_key );
    if ( log_entry == null )                   // new entries always are logged
      write_file = true;
    else
      requests = log_entry.getRequests() + 1; 

    log.put( log_key, new LogEntry( command, requests ) );
    if ( write_file )
      WriteLogFile();
  }

  /* ---------------------------- WriteLogFile --------------------------- */
  /**
   *  Write the list of log entries to a file. 
   */
  private void WriteLogFile()
  {
     try                                                 // write to file 
     {
       File f = new File( log_filename );
       FileOutputStream file_stream = new FileOutputStream( f );
       OutputStreamWriter file_writer = new OutputStreamWriter( file_stream );
       file_writer.write( "------------------------------------------------\n");
       file_writer.write( server_name+" since "+start_time+"\n");
       file_writer.write( "------------------------------------------------\n");
       Enumeration e = log.keys();
       while ( e.hasMoreElements() )
       {
         String key = (String)e.nextElement();
         String value = ((LogEntry)log.get( key )).toString();
         file_writer.write(key + "\n" + value + "\n");
       }
       file_writer.flush();
       file_writer.close();
     }
     catch ( Exception exception )                        // write to console
     {
       Enumeration e = log.keys();
       while ( e.hasMoreElements() )
       {
         String key = (String)e.nextElement();
         String value = ((LogEntry)log.get( key )).toString();
         System.out.println( key + "\n" + value );
       }
     }
  }


  /* --------------------------- reset_user_info ------------------------ */
  /**
   *  Reset the user_name to "", and reset user and password ok flags to false.
   */
  private void reset_user_info()
  {
    user_name   = UNKNOWN_USER;
    user_ok     = false;
    password_ok = false;
  }

  /* -------------------------------------------------------------------------
   *
   *  PRIVATE CLASSES
   *
   */

  private class LogEntry
  {
    String date;
    String command;
    int    total_requests;

    public LogEntry( String command, int requests )
    {
      this.date      = DateUtil.default_string();
      this.command   = command;
      total_requests = requests;
    }

    public String getDate()
    {
      return date;
    }

    public String getCommand()
    {
      return command;
    }

    public int getRequests()
    {
      return total_requests;
    }

    public String toString()
    {
      String s  = "  Last Active:    " + date + "\n";
             s += "  Last Command:   " + command + "\n";
             s += "  Total Requests: " + total_requests + "\n";
      return s;
    }
  }


  /* ------------------------------ main --------------------------------- */

  public static void main(String args[])
  {
    System.out.println("TCPServer starting:");

    TCPServer server= new TCPServer();

    server.parseArgs( args );                  // get options from command
/*
                                               // set hard coded values 
    server.setServerName( "TestServer" );
    server.setLogFilename( "TestLog.txt" );
    server.setTCPport( 6091 );
*/
    server.startTCP();
  }
}

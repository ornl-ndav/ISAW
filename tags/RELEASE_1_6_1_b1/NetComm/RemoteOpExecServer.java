/*
 * File: RemoteOpExecServer.java
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
 *  Revision 1.1  2003/10/21 21:23:25  dennis
 *  Server to execute operators sent to it from clients
 *  on remote systems.
 *
 */
package NetComm;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;
import DataSetTools.operator.*;

/**
 *  This class implements a server to execute operators sent to it as part
 *  of a command from client machines.  The server will either store the 
 *  result of the operator in a file, and return a status message, or it 
 *  will directly return the result of invoking the operator, depending on 
 *  the command that was received.
 */

public class RemoteOpExecServer extends TCPServer 
{

  /* ---------------------------- Constructor -------------------------- */
  /**
   *  Construct a RemoteOpExecServer.
   */
   public RemoteOpExecServer()
   {
     super();
     setServerName( "RemoteOpExecServer" );
     setLogFilename( "RemoteOpExecLog.txt" );
     // debug_server = true;
   }


  /* -------------------------- ProcessCommand -------------------------- */
  /**
   *  Method to process commands from a TCP client.  
   * *  @param  command    The command object sent by the client.
   *
   *  @param  tcp_io     The TCP communications object to which the response
   *                     must be sent.
   *
   *  @return True if the command was processed and no further action is
   *          needed. Returns false if the command was not handled, and
   *          so some response must still be sent back to the client.
   */
   synchronized public boolean ProcessCommand( CommandObject   command,
                                               ThreadedTCPComm tcp_io   )
   {
      if ( debug_server )
        System.out.println("RemoteOpExecServer ProcessCommand called:"+command);
      try
      {
        if ( debug_server )
            System.out.println("Processing " + command );

        if (  command.getCommand() == CommandObject.RETURN_RESULT )
        {
          Operator op = ((ExecuteOpCommand)command).getOperator();
          Object result = op.getResult();
          tcp_io.Reset();
          tcp_io.Send( result );

          if ( debug_server )
          {
            System.out.println("returned result of " + op.getTitle() );
            System.out.println("result class = " + result.getClass() );
          }

          result = null; 
          System.gc();
        }

        else if ( command.getCommand() == CommandObject.RESULT_IN_FILE )
        { 
          Operator op = ((ExecuteOpCommand)command).getOperator();
          Object result = op.getResult();
          if ( result instanceof ErrorString )
            tcp_io.Send( result );
          else
          {
            String filename = ((ExecuteOpCommand)command).getFilename();
            if ( filename == null || filename.length() == 0 )
              filename = op.getTitle();

            String dir_name = ((ExecuteOpCommand)command).getDir_name();
            if ( dir_name != null && dir_name.length() > 0 )
              filename = dir_name + "/" + filename;

            if ( result instanceof String       ||
                 result instanceof StringBuffer  )
              WriteStringToFile( filename, result );
            else
              WriteObjectToFile( filename, result );
 
            tcp_io.Send( "SAVED TO:" + filename ); 
          }            
          result = null;
          System.gc();
        }
  
        else if ( command.getCommand() == CommandObject.GET_STATUS )
          tcp_io.Send(TCPServer.STATUS + TCPServer.ANSWER_OK);
 
        else
          return false;
      }
      catch ( Exception e )
      {
        System.out.println("In RemoteOpExecServer, exception is " + e );
        e.printStackTrace();
        System.out.println("Error: FileDataServer command: " + command);
        System.out.println("Error: couldn't send data "+e );
        return false;
      }  

    return true;
  }


  /* ------------------------ WriteStringToFile ------------------------- */
  /** 
   *  Write the specified String or StringBuffer object to a file.
   *
   *  @param  filename   The name of the file
   *  @param  result     A String or StringBuffer to be dumped to the file
   */
  public static void WriteStringToFile( String filename, Object result )
                     throws Exception
  {
    FileWriter  file   = new FileWriter( filename );
    PrintWriter writer = new PrintWriter( file );

    if ( result instanceof StringBuffer )
      writer.print( ((StringBuffer)result).toString() );
    else
      writer.print( ((String)result).toString() );

    writer.close();       
  }

  
  /* ------------------------ WriteObjectToFile ------------------------- */
  /** 
   *  Write the specified object to a file as a GZIP compressed object
   *  stream, for compatibility with ISAW .isd files, in the case that
   *  we are saving DataSets.  
   *
   *  @param  filename   The name of the file
   *  @param  result     An object to be dumped to the file.
   */
  public static void WriteObjectToFile( String filename, Object result )
                     throws Exception
  {
    FileOutputStream   file_os = new FileOutputStream( filename );
    GZIPOutputStream   gz_os   = new GZIPOutputStream( file_os );
    ObjectOutputStream obj_os  = new ObjectOutputStream( gz_os );
  
    obj_os.writeObject( result );
    obj_os.close();          
  }


  /* ------------------------------ main --------------------------------- */

  public static void main(String args[])
  {
    RemoteOpExecServer server = new RemoteOpExecServer();
    server.parseArgs( args );

    if ( StringUtil.commandPresent( "-h", args )  ||
         StringUtil.commandPresent( "-H", args )  )
      System.exit(1);

    Date date = new Date( System.currentTimeMillis() );
    System.out.println("Starting " + server.getServerName() + " on " + date );
    System.out.println("Log File " + server.getLogFilename() );

    server.startTCP();
  }

}

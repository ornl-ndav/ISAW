/*
 * File: FileDataSetServer.java
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
 *  Revision 1.1  2001/08/03 21:33:00  dennis
 *  Server for .run and .nxs files.
 *
 *
 */
package NetComm;

import java.io.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;

/**
 *  This is a base class for servers that receive requests for DataSets and 
 *  sends them to clients on a network.
 *
 *  @see ITCPUser
 *  @see TCPComm
 *  @see ThreadedTCPComm
 *  @see TCPServiceInit 
 */

public class FileDataSetServer extends DataSetServer 
{

  /* ---------------------------- Constructor -------------------------- */
  /**
   *  Construct a FileDataSetServer with an empty list of DataSets.
   */
   public FileDataSetServer()
   {
     super();
     setServerName( "FileDataSetServer" );
     setLogFilename( "FileServerLog.txt" );
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
      System.out.println("FileDataSetServer ProcessCommand called:"+command);
      try
      {
        if ( command.startsWith( COMMAND_GET_DS_TYPES ) )
        {
          System.out.println("Processing " + command );
          String file_name = getArgument( command );
          System.out.println("file_name =" + file_name );
          Retriever r = get_retriever( file_name );
          if ( r == null )
            tcp_io.Send( new Integer(-1) );
          else
          {
            int n_ds = r.numDataSets();
            System.out.println("n_ds = " + n_ds );
            int types[] = new int[ n_ds ];
            for ( int i = 0; i < n_ds; i++ )
              types[i] = r.getType(i);
            tcp_io.Send( types );
            r = null; 
            System.gc();
          }
        }

        else if ( command.startsWith( COMMAND_GET_DS ) )
        { 
                         // COMMAND_GET_DS command has the form:
                         // COMMAND_GET_DS  <file_name>  <DataSet index>

          System.out.println("Processing GET DS " + command );

          String argument = getArgument( command );
          int index = extractIntParameter( argument );

          int space = argument.indexOf(' ');
          String file_name = argument.substring( 0, space ).trim();
          Retriever r = get_retriever( file_name );
          if ( r == null )
            tcp_io.Send( DataSet.EMPTY_DATA_SET.clone() );
          else 
          { 
            DataSet ds = r.getDataSet( index );

            if ( ds != null )                           // remove observers
            {                                           // before sending
              System.out.println("Trying to send " + ds );
              ds.deleteIObservers(); 
              tcp_io.Send( ds  );
              System.out.println("Finished sending " + ds );
            }
            else                                       
              tcp_io.Send( DataSet.EMPTY_DATA_SET.clone() );
            r = null; 
            System.gc();
          }
        }
  
        else
        {
           System.out.println( "Command not recognized in FileDataServer");
           System.out.println( "ProcessCommand() --> " + command );
           tcp_io.Send( ANSWER_NOT_OK );
        }
      }
      catch ( Exception e )
      {
        System.out.println("Error: FileDataSetServer command: " + command);
        System.out.println("Error: couldn't send data "+e );
      }  
   
  }


  /* ------------------------------ main --------------------------------- */

  public static void main(String args[])
  {
    System.out.println("FileDataSetServer starting:");

    FileDataSetServer server= new FileDataSetServer();
    server.setServerName( "Test FileDataSetServer" );
    server.setLogFilename( "Test_FileDataSetServerLog.txt" );

    server.addDataDirectory( "/home/dennis/ARGONNE_DATA/" );

                                         // Start the DataSetServer to listen
                                         // for clients requesting data
    System.out.println("Starting TCP server...");
    TCPServiceInit TCPinit;
    TCPinit = new TCPServiceInit( server, DEFAULT_SERVER_PORT_NUMBER );

    TCPinit.start();
    System.out.println("TCP server started.");
    System.out.println();
  }
}

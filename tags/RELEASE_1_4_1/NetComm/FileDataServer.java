/*
 * File: FileDataServer.java
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
 *  Revision 1.3  2002/11/27 23:27:59  pfpeterson
 *  standardized header
 *
 */
package NetComm;

import java.io.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;

/**
 *  This is a base class for servers that receive requests for DataSets and 
 *  sends them to clients on a network.
 *
 *  @see ITCPUser
 *  @see TCPComm
 *  @see ThreadedTCPComm
 *  @see TCPServiceInit 
 */

public class FileDataServer extends DataSetServer 
{

  /* ---------------------------- Constructor -------------------------- */
  /**
   *  Construct a FileDataServer with an empty list of DataSets.
   */
   public FileDataServer()
   {
     super();
     setServerName( "FileDataServer" );
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
      if ( debug_server )
        System.out.println("FileDataServer ProcessCommand called:"+command);
      try
      {
        if ( command.startsWith( COMMAND_GET_DS_TYPES ) )
        {
          if ( debug_server )
            System.out.println("Processing " + command );

          String file_name = getArgument( command );
          Retriever r = get_retriever( file_name );
          if ( r == null )
            tcp_io.Send( new Integer(-1) );
          else
          {
            int n_ds = r.numDataSets();
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
              if ( debug_server )
                System.out.println("Trying to send " + ds );
              ds.deleteIObservers(); 
              tcp_io.Send( ds  );
              data_name = ds.getTitle();
            }
            else                                       
              tcp_io.Send( DataSet.EMPTY_DATA_SET.clone() );
            r = null; 
            System.gc();
          }
        }
  
        else
           super.ProcessCommand( command, tcp_io );
      }
      catch ( Exception e )
      {
        System.out.println("Error: FileDataServer command: " + command);
        System.out.println("Error: couldn't send data "+e );
      }  
   
  }


  /* ------------------------------ main --------------------------------- */

  public static void main(String args[])
  {
    FileDataServer server= new FileDataServer();
    server.parseArgs( args );

    if ( StringUtil.commandPresent( "-h", args )  ||
         StringUtil.commandPresent( "-h", args )  )
      System.exit(1);

    Date date = new Date( System.currentTimeMillis() );
    System.out.println("Starting " + server.getServerName() + " on " + date );
    System.out.println("Log File " + server.getLogFilename() );
    System.out.println("Using DataDirectories ");
    server.showDataDirectories();

    server.startTCP();
  }
}

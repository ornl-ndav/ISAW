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
 *  Revision 1.11  2004/03/15 03:35:22  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.10  2004/03/01 18:45:22  dennis
 *  Now sends: RemoteDataRetriever.BAD_FILE_NAME in case the requested
 *  file does not exist.
 *
 *  Revision 1.9  2003/10/21 21:16:42  dennis
 *  Fixed some errors with javadoc comments and other comments.
 *  Removed redundant debug print.
 *
 *  Revision 1.8  2003/10/20 22:11:21  dennis
 *  Server will now halt of started with either -h or -H to
 *  request help on usage.
 *
 *  Revision 1.7  2003/02/24 21:09:14  dennis
 *  Moved STATUS string from TCPComm to TCPServer
 *
 *  Revision 1.6  2003/02/24 20:37:05  dennis
 *  Now consistently replies to status requests by sending a String
 *  that begins with "Status:".
 *
 *  Revision 1.5  2003/02/24 13:39:19  dennis
 *  Switched to use CommandObject instead of compound command Strings.
 *
 *  Revision 1.4  2003/02/21 13:17:35  dennis
 *  Removed un-needed clone() operations before sending empty data set.
 *  Calls Reset() before sending the array of DataSet types.
 *
 *  Revision 1.3  2002/11/27 23:27:59  pfpeterson
 *  standardized header
 *
 */
package NetComm;

import gov.anl.ipns.Util.Sys.*;

import java.io.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;

/**
 *  This class implements a remote file server for files that can be 
 *  read by ISAW.  The server receives requests for the number and type 
 *  of DataSets in a file, or for particular DataSets in the file and 
 *  sends the requested information to clients.
 */

public class FileDataServer extends DataSetServer 
{

  /* ---------------------------- Constructor -------------------------- */
  /**
   *  Construct a FileDataServer with an empty list of directories.  
   *  The addDataDirectory() method from the DataSetServer class must be
   *  called to build the list of directories that will be searched when 
   *  a particular file is requested. 
   */
   public FileDataServer()
   {
     super();
     setServerName( "FileDataServer" );
     setLogFilename( "FileServerLog.txt" );
   }


  /* -------------------------- ProcessCommand -------------------------- */
  /**
   *  Method to process commands from a TCP client.  
   *
   *  @param  command    The command object sent by the client.
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
        System.out.println("FileDataServer ProcessCommand called:"+command);

      try
      {
        if (  command.getCommand() == CommandObject.GET_DS_TYPES )
        {
          String file_name = ((GetDataCommand)command).getFilename();
          Retriever r = get_retriever( file_name );
          if ( r == null )
            tcp_io.Send( new Integer( RemoteDataRetriever.BAD_FILE_NAME ) );
          else
          {
            int n_ds = r.numDataSets();
            int types[] = new int[ n_ds ];
            for ( int i = 0; i < n_ds; i++ )
              types[i] = r.getType(i);
            tcp_io.Reset();
            tcp_io.Send( types );
            r = null; 
            System.gc();
          }
        }

        else if ( command.getCommand() == CommandObject.GET_DS )
        { 
          int index = ((GetDataCommand)command).getDataSetNumber();
          String file_name = ((GetDataCommand)command).getFilename();
          Retriever r = get_retriever( file_name );
          if ( r == null )
            tcp_io.Send( DataSet.EMPTY_DATA_SET );
          else 
          { 
            DataSet ds = r.getDataSet( index );

            if ( ds != null )                           // remove observers
            {                                           // before sending
              if ( debug_server )
                System.out.println("Trying to send " + ds );
              ds.deleteIObservers(); 
              data_name = ds.getTitle();
              tcp_io.Send( ds );
            }
            else                                       
              tcp_io.Send( DataSet.EMPTY_DATA_SET );
            r = null; 
            System.gc();
          }
        }
  
        else if ( command.getCommand() == CommandObject.GET_STATUS )
          tcp_io.Send(TCPServer.STATUS + TCPServer.ANSWER_OK);
 
        else
          return false;
      }
      catch ( Exception e )
      {
        System.out.println("Error: FileDataServer command: " + command);
        System.out.println("Error: couldn't send data "+e );
        return false;
      }  

    return true;
  }


  /* ------------------------------ main --------------------------------- */

  public static void main(String args[])
  {
    FileDataServer server= new FileDataServer();
    server.parseArgs( args );

    if ( StringUtil.commandPresent( "-h", args )  ||
         StringUtil.commandPresent( "-H", args )  )
      System.exit(1);

    Date date = new Date( System.currentTimeMillis() );
    System.out.println("Starting " + server.getServerName() + " on " + date );
    System.out.println("Log File " + server.getLogFilename() );
    System.out.println("Using DataDirectories ");
    server.showDataDirectories();

    server.startTCP();
  }
}

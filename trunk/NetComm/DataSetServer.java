/*
 * File: DataSetServer.java
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
 *  Revision 1.1  2001/08/03 21:27:16  dennis
 *  Base class for TCP servers that process requests for DataSets.
 *  Maintains a list of directories and defines some commands.
 *  Extends TCPServer.
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
 *  sends them to clients on a network.  It adds some commands, utility 
 *  methods and a list of data directories that are used by both 
 *  LiveDataServers and FileDataServers
 *
 *  @see LiveDataServer 
 *  @see FileDataServer 
 *  @see TCPServer 
 */

public class DataSetServer extends TCPServer 
{
  public static final String COMMAND_GET_DS      = "COMMAMD:GET_DATA_SET ";
  public static final String COMMAND_GET_DS_TYPE = "COMMAND:GET_DATA_SET_TYPE ";
  public static final String COMMAND_GET_DS_TYPES= "COMMAND:GET_DS_TYPES ";
  public static final String COMMAND_GET_NUM_DS  = "COMMAND:GET_NUM_DATA_SETS ";

  protected  Vector  directory_names = null;

  /* ---------------------------- Constructor -------------------------- */
  /**
   *  Construct a DataSetServer with an empty list of DataSets.
   */
   public DataSetServer()
   {
     super();
     directory_names = new Vector();
   }


  /* ------------------------- addDataDirectory ------------------------ */
  /**
   *  Method to add a data directory to the list of directories that will be
   *  searched for a requested file.  If a directory is specified on the 
   *  command line it is used as the data directory.  If not, the current
   *  directory is used.
   *
   *  @ param String dataDirectory
   */
  public void addDataDirectory( String data_directory )
  {
    for ( int i = 0; i < directory_names.size(); i++ )
      if ( data_directory.equals( (String)directory_names.elementAt(i) ) )
        return;
 
    directory_names.add( data_directory ); 
  }


 /*-------------------------------------------------------------------------
  *
  *  PROTECTED METHODS
  * 
  */

  /* -------------------------- extractIntParameter ----------------------- */
  /**
   *  Extract the first integer value occuring in a command string.
   *
   *  @param command  A command string containing an integer parameter following
   *                  a space ' ' character. 
   */
  protected int extractIntParameter( String command )
  {
    int first_space = command.indexOf( " " );       // extract string following 
                                                    // the first space, if
                                                    // possible
    if ( first_space < 0 )
      return -1;
    
    command = command.substring( first_space + 1 );
    command.trim();

    int next_space = command.indexOf( " " );
    String int_string = " ";
    if ( next_space < 0 )
      int_string = command;
    else
      int_string = command.substring( 0, next_space );

    int parameter = (Integer.valueOf( int_string )).intValue();
    return parameter;
  }


 /* ---------------------------- get_retriever --------------------------- */
 /**
  *  Get a Retriver for the specified file, currently IPNS runfiles and
  *  NeXus files are supported.  
  *
  *  @param file_name  The base name of the file, relative to one of the
  *                    directories available in the list of directories.
  *
  *  @return Return a retriever for the specified file, if the file exists
  *          and is one of the supported types of files.  Return null
  *          otherwise.
  */

  synchronized protected Retriever get_retriever( String file_name )
  {
    String full_name = find_file( file_name );
    Retriever retriever = null;

    if ( full_name == null )
      return null;
 
    String temp = full_name.toUpperCase();
    if ( temp.endsWith( "RUN" ) )
      retriever = new RunfileRetriever( full_name );

    else if ( temp.endsWith("NXS") || temp.endsWith("HDF") )
      retriever = new NexusRetriever( full_name );

    return retriever;
  }

 /*-------------------------------------------------------------------------
  *
  *  PRIVATE METHODS
  *
  */

 /* ------------------------------ find_file ---------------------------- */
 /**
  *  Find the specified file in the list of available directories.  The first
  *  occurence is used and the fully qualified file name is returned as the
  *  value of this function.  If the file is not found, null is returned.
  */  
  synchronized private String find_file( String file_name )
  {
    int     dir_num = 0;
    String  full_name;

    while ( dir_num < directory_names.size() )
    {
      full_name  = (String)directory_names.elementAt( dir_num );
      full_name += file_name;

      File file = new File ( full_name );

      if ( file.exists() )
        return full_name;
      else
        dir_num++;   
    }

    return null;
  }


  /* ------------------------------ main --------------------------------- */

  public static void main(String args[])
  {

    System.out.println("DataSetServer starting:");

    DataSetServer server= new DataSetServer();
    server.setServerName( "TestDataSetServer" );
    server.setLogFilename( "TestDataSetServerLog.txt" );
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

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
 *  Revision 1.6  2001/09/20 17:37:55  dennis
 *  Fixed @see javadoc comment
 *
 *  Revision 1.5  2001/08/10 19:43:22  dennis
 *  Added methods to parse command line arguments and show usage.
 *  Also added methods to get and show data directories being used.
 *  Added code to add a directory separator at the end of a
 *  directory name, if it was not there.
 *
 *  Revision 1.4  2001/08/09 21:46:48  dennis
 *  Uses start_TCP() method to start the TCPServer.
 *
 *  Revision 1.3  2001/08/09 15:43:35  dennis
 *  Added command GET_NAME.
 *
 *  Revision 1.2  2001/08/07 21:27:25  dennis
 *    Removed command to get DS_TYPE and get NUM_DS... now only uses
 *  get DS_TYPES, for the whole list of types.  This is simpler,
 *  reduces the number of requests needed and allows LiveDataServer
 *  and FileDataServer to handle the same requests.
 *    extractIntParameter() now treats the last item in a command
 *  string as an int parameter.
 *    find_file() now tries upper and lower case versions of the
 *  file name as well as the original file specified.
 *
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
import DataSetTools.util.*;

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
  public static final String COMMAND_GET_DS_TYPES= "COMMAND:GET_DS_TYPES ";
  public static final String COMMAND_GET_NAME    = "COMMAND:GET_NAME ";

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
    String fixed = data_directory.trim();

   if ( !fixed.endsWith( File.separator ))
     fixed += File.separator;

    for ( int i = 0; i < directory_names.size(); i++ )
      if ( fixed.equals( (String)directory_names.elementAt(i) ) )
        return;
 
    directory_names.add( fixed ); 
  }


  /* ------------------------ getDataDirectory --------------------------- */
  /**
   *  Get the specified data directory from the list of data directories;
   *
   *  @return  The specified entry in the data directory list or an empty
   *           string if the entry does not exist.
   */
  public String getDataDirectory( int i )
  {
    if ( i < 0 || i > directory_names.size()-1 )
      return "";

    return (String)directory_names.elementAt(i);
  }


  /* ------------------------ showDataDirectories ------------------------ */
  /**
   *  Show the list of data directories currently being used.
   */
  public void showDataDirectories()
  {
    for ( int i = 0; i < directory_names.size(); i++ )
      System.out.println("   " + (String)directory_names.elementAt(i) );
  }


  /* ------------------------- parseArgs ----------------------------- */
  /**
   *  Parse a list of command line arguments to extract values for the
   *  the data directories.  The only command supported at this level
   *  is -D.
   *
   *  @param args  Array of strings from the command line, containing
   *               command characters and arguments.
   */
   public void parseArgs( String args[] )
   {
     super.parseArgs( args );

     if ( StringUtil.commandPresent("-h", args ) ||
          StringUtil.commandPresent("-H", args )  )
       showDataSetServerUsage();

     int count = 1;
     String dir_name = StringUtil.getCommand( count, "-D", args );
     while ( dir_name.length() > 0 )
     {
       addDataDirectory( dir_name ); 
       count++;
       dir_name = StringUtil.getCommand( count, "-D", args );
     }
   }

  /* ----------------------- showDataSetServerUsage ----------------------- */
  /**
   *  Print list of supported commands.
   */ 
   public void showDataSetServerUsage()
   {
    System.out.println("  -D<dir name>     Add a directory to list of");
    System.out.println("                   directories to search for data.");
    System.out.println("                   To add multiple directories, use");
    System.out.println("                   -D multiple times, once for each");
    System.out.println("                   additional directory.");
   }


 /*-------------------------------------------------------------------------
  *
  *  PROTECTED METHODS
  * 
  */

  /* -------------------------- extractIntParameter ----------------------- */
  /**
   *  Extract the last integer value occuring in a command string.
   *
   *  @param command  A command string ending with a space and an integer 
   *                  parameter.
   */
  protected int extractIntParameter( String command )
  {
    int last_space = command.lastIndexOf( " " );   
 
    if ( last_space < 0 )
      return -1;
    
    String int_string = command.substring( last_space + 1 );
    int_string.trim();

    int parameter;
    try
    {
      parameter = (Integer.valueOf( int_string )).intValue();
    }
    catch ( NumberFormatException e )
    {
      parameter = -1;
    }
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
    String  dir_name;
    File    file;

    while ( dir_num < directory_names.size() )
    {
      dir_name  = (String)directory_names.elementAt( dir_num );

      full_name = dir_name + file_name;                  // try it as specified
      file = new File ( full_name );
      if ( file.exists() )
        return full_name;

      full_name = dir_name + file_name.toUpperCase();    // try it UPPER CASE
      file = new File ( full_name );
      if ( file.exists() )
        return full_name;

      full_name = dir_name + file_name.toLowerCase();    // try it lower case
      file = new File ( full_name );
      if ( file.exists() )
        return full_name;

      dir_num++;   
    }

    return null;
  }


  /* ------------------------------ main --------------------------------- */

  public static void main(String args[])
  {

    System.out.println("DataSetServer starting:");

    DataSetServer server= new DataSetServer();
    server.parseArgs( args );
    server.startTCP();
  }
}

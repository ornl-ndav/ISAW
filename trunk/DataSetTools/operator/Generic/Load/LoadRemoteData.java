/*
 * File:  LoadRemotejava
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
 *  Revision 1.6  2003/05/28 20:58:08  pfpeterson
 *  Changed System.getProperty to SharedData.getProperty
 *
 *  Revision 1.5  2003/01/13 17:28:58  dennis
 *  Added getDocumentation() method. (Chris Bouzek)
 *
 *  Revision 1.4  2002/11/27 23:21:16  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/09/19 16:05:35  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.2  2002/04/08 14:08:46  dennis
 *  Changed default port to 6089
 *
 *  Revision 1.1  2002/02/22 20:57:57  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.Generic.Load;

import java.io.*;
import java.util.*;
import DataSetTools.util.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import NetComm.*;
import DataSetTools.operator.Parameter;
import DataSetTools.parameter.*;

/**
 * Operator to load all of the DataSets from a remote data source.
 * For example, a snapshot of the monitor and histogram data can be
 * obtained from a LiveDataServer, or the contents of a runfile can be
 * obtained from a FileDataServer,
 */

public class LoadRemoteData extends    GenericLoad
                            implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this constructor
   * is used, meaningful values for the parameters should be set before
   * calling getResult().
   */
   public LoadRemoteData( )
   {
     super( "Load Remote Data" );
   }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for with the specified parameter values so
   *  that the operation can be invoked immediately by calling getResult().
   *
   *  @param  host        The name or internet address of the remote machine.
   *  @param  port        The port number to use for the connection.
   *  @param  user_name   The user name for the remote machine.
   *  @param  password    The password for the remote machine.
   *  @param  file_name   The name of the file to be loaded (eg: hrcs1797.run).
   *  @param  server_type The type of the server, one of:
   *                          LIVE_DATA_SERVER  = "Live Data";
   *                          ISAW_FILE_SERVER  = "ISAW File Server";
   *                          NDS_FILE_SERVER   = "NDS File Server";
   */
   public LoadRemoteData( String           host,
                          int              port,
                          String           user_name,
                          String           password,
                          String           file_name,
                          ServerTypeString server_type )
   {
      super( "Load Remote Data" );

      IParameter parameter = getParameter(0);
      parameter.setValue( host );

      parameter = getParameter(1);
      parameter.setValue( new Integer(port) );

      parameter = getParameter(2);
      parameter.setValue( user_name );

      parameter = getParameter(3);
      parameter.setValue( password );

      parameter = getParameter(4);
      parameter.setValue( file_name );

      parameter = getParameter(5);
      parameter.setValue( new ServerTypeString( server_type.toString() ) );
   }

  /* -------------------------- setDefaultParameters ----------------------- */
  /**
   *  Set the parameters to default values.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Host name", new String("") );
    addParameter( parameter );

    int port = TCPServer.DEFAULT_SERVER_TCP_PORT;
    parameter = new Parameter("Port (e.g. 6089)", new Integer(port) );
    addParameter( parameter );

    String user_name = SharedData.getProperty( "user.name" );
    parameter = new Parameter("User name", user_name );
    addParameter( parameter );

    String password = TCPServer.DEFAULT_PASSWORD;
    parameter = new Parameter("Password", password );
    addParameter( parameter );

    parameter = new Parameter("File Name (e.g. hrcs1797.run)",
                 new String("") );
    addParameter( parameter );

    parameter = new Parameter("Server Type", new ServerTypeString() );
    addParameter( parameter );
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: in this case,
   *         LoadRemote
   *
   */
   public String getCommand()
   {
     return "LoadRemote";
   }

  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator loads all of the DataSets from a ");
    s.append("remote data source.\n");
    s.append("@assumptions The server type must be one of the three listed ");
    s.append("below in the server_type parameter description.  The ");
    s.append("specified file also must exist on the server.  Furthermore, ");
    s.append("the specified server type must match the actual server type.\n");
    s.append("@algorithm First this operator uses the host name, port ");
    s.append("number, username, password, and file name to create a data ");
    s.append("source name.\n");
    s.append("Next it tries to open a connection to the server.  In doing ");
    s.append("so, it checks to see whether the server is down, whether the ");
    s.append("specified server type matches the actual server type, and ");
    s.append("whether the specified file exists on the server.\n");
    s.append("Finally it retrieves the DataSets from the server and places ");
    s.append("them in an array.\n");
    s.append("@param host The name or internet address of the remote ");
    s.append("machine.\n");
    s.append("@param port The port number to use for the connection.\n");
    s.append("@param user_name The user name for the remote machine.\n");
    s.append("@param password The password for the remote machine.\n");
    s.append("@param file_name The name of the file to be loaded.  ");
    s.append("(e.g.: hrcs1797.run).\n");
    s.append("@param server_type The type of the server.  Use one of the ");
    s.append("following:\n");
    s.append("LIVE_DATA_SERVER = \"Live Data\"\n");
    s.append("ISAW_FILE_SERVER = \"ISAW File Server\"\n");
    s.append("NDS_FILE_SERVER  = \"NDS File Server\"\n");
    s.append("@return An array of DataSets is returned as a Java Object.\n");
    s.append("@error Returns an error if the specifed server type does not ");
    s.append("match one of the types listed in the server_type parameter ");
    s.append("description.\n");
    s.append("@error Returns an error if the file is not found on the ");
    s.append("server.\n");
    s.append("@error Returns an error if the server is down.\n");
    s.append("@error Returns an error if the specified server type does not ");
    s.append("match the actual server type.\n");
    return s.toString();
  }

  /* ----------------------------- getResult ---------------------------- */
  /**
   *  Connects to the specified remote server and retrieves the DataSets
   *  on it.
   *
   *  @return  An array of DataSets is returned as a Java Object.
   */
   public Object getResult()
   {
     String  host      = (String)getParameter(0).getValue();
     int     port      = ((Integer)getParameter(1).getValue()).intValue();
     String  user_name = (String)getParameter(2).getValue();
     String  password  = (String)getParameter(3).getValue();
     String  file_name = (String)getParameter(4).getValue();

     Object obj         = getParameter(5).getValue();
     String server_type = ((ServerTypeString)obj).toString();

     user_name = user_name.trim();
     if ( user_name.length() <= 0 )
       user_name = SharedData.getProperty( "user.name" );

     String  data_source_name = host      + ";" +
                                port      + ";" +
                                user_name + ";" +
                                password  + ";" +
                                file_name;

     Retriever r;
     if ( server_type.equals( ServerTypeString.LIVE_DATA_SERVER ) )
       r = new LiveDataRetriever( data_source_name );
     else if  ( server_type.equals( ServerTypeString.ISAW_FILE_SERVER ) )
       r = new RemoteFileRetriever( data_source_name );
     else if  ( server_type.equals( ServerTypeString.NDS_FILE_SERVER ) )
       r = new ndsRetriever( data_source_name );
     else
       return new ErrorString( "ERROR: Unsupported Server Type" );

     int n_ds = RemoteDataRetriever.SERVER_DOWN;
     if ( r != null )
       n_ds = r.numDataSets();

     if ( n_ds == RemoteDataRetriever.BAD_FILE_NAME )
       return new ErrorString( "File " + file_name + " NOT FOUND" );

     else if ( n_ds == RemoteDataRetriever.SERVER_DOWN )
       return new ErrorString( "Can't connect to " + host + " on " + port );

     else if ( n_ds == RemoteDataRetriever.WRONG_SERVER_TYPE )
       return new ErrorString( "Wrong server type: " + server_type +
                               " on " + host + " port " + port );
     else if ( n_ds <= 0 )
       return new ErrorString( RemoteDataRetriever.error_message( n_ds ) );

     else
     {
       DataSet ds[] = new DataSet[ n_ds ];
       for ( int i = 0; i < ds.length; i++ )
         ds[i] = r.getDataSet(i);
       return ds;
     }
   }


   /* -------------------------------- main ------------------------------ */
   /*
    * main program for test purposes only
    */

    public static void main( String[] args )
    {
      //simple main just to test documentation and
      //default getResult() method

      LoadRemoteData op;
      StringBuffer m = new StringBuffer();

      op = new LoadRemoteData();

      Object result = op.getResult();
      m.append("\nThe results of calling getResult() for ");
      m.append("a default instance of LoadRemoteData are:\n\n");
      m.append(result.toString());
      m.append("\n\nThe results of calling getDocumentation() for ");
      m.append("LoadRemoteData are:\n\n");
      m.append(op.getDocumentation());

      System.out.print(m.toString());
    }
}

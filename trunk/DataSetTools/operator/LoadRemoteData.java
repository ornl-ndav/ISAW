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
 *  Revision 1.2  2001/08/07 21:37:29  dennis
 *  Added support for ndsRetriver.
 *
 *  Revision 1.1  2001/08/07 15:58:25  dennis
 *  Operator for loading data from remote source such as a
 *  LiveDataServer, etc.
 *
 */

package DataSetTools.operator;

import java.io.*;
import java.util.*;
import DataSetTools.util.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import NetComm.*;

/**
 * Operator to load all of the DataSets from a remote data source.  
 * For example, a snapshot of the monitor and histogram data can be 
 * obtained from a LiveDataServer, or the contents of a runfile and be
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
   *  @param  file_name   The name of the file to be loaded, eg: hrcs1797.run.
   *  @param  server_type The type of the server, one of:
   *                          LIVE_DATA_SERVER  = "Live Data";
   *                          ISAW_FILE_SERVER  = "ISAW File Server";
   *                          NDS_FILE_SERVER   = "NDS File Server";
   */
   public LoadRemoteData( String           host,
                          int              port,
                          String           file_name,
                          ServerTypeString server_type ) 
   {
      super( "Load Remote Data" );

      Parameter parameter = getParameter(0);
      parameter.setValue( host );

      parameter = getParameter(1);
      parameter.setValue( new Integer(port) );

      parameter = getParameter(2);
      parameter.setValue( file_name );

      parameter = getParameter(3);
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

    int port = TCPServer.DEFAULT_SERVER_PORT_NUMBER;
    parameter = new Parameter("Port (eg. 6088)", new Integer(port) );
    addParameter( parameter );

    parameter = new Parameter("File Name(hrcs1797.run)", new String("") );
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



  /* ----------------------------- getResult ---------------------------- */
  /**
   * @return  An array of DataSets is returned as a Java Object.
   */
   public Object getResult()
   {
     String  host = (String)getParameter(0).getValue();
     int     port = ((Integer)getParameter(1).getValue()).intValue();
     String  file_name   = (String)getParameter(2).getValue();

     Object obj = getParameter(3).getValue();
     ServerTypeString sts = (ServerTypeString)obj;
     String server_type = sts.toString();
//     String  server_type = (String)
//                 ((ServerTypeString)getParameter(3).getValue()).toString();

     String user_name = System.getProperty( "user.name" );
     String password  = "IPNS";

     String  data_source_name = host      + ";" +
                                port      + ";" +
                                user_name + ";" +
                                password  + ";" +
                                file_name;

     System.out.println("In LoadRemote operator, name = " + data_source_name); 
     file_name = file_name.trim();

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
     
     else if ( n_ds == RemoteDataRetriever.BAD_USER_OR_PASSWORD )
       return new ErrorString( "Bad user name or password" );
       
     else if ( n_ds == RemoteDataRetriever.SERVER_DOWN )
       return new ErrorString( "Can't connect to " + host + " on " + port );
       
     else if ( n_ds == RemoteDataRetriever.WRONG_SERVER_TYPE )
       return new ErrorString( "Wrong sever type: " + server_type +
                               " on " + host + " port " + port );
       
     else if ( n_ds <= 0 )
       return new ErrorString( "No DataSets available" );

     else
     {
       System.out.println("Getting " + n_ds + " DataSets");
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

   public static void main(String[] args)
   {
   } 
} 

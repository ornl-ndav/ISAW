/*
 * File: RemoteOpExecClient.java
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
 *  Revision 1.3  2003/10/31 18:15:44  dennis
 *  Added a few checks for null in the main (test) program.
 *
 *  Revision 1.2  2003/10/22 21:24:30  dennis
 *  Changed parameter name so javadocs build cleanly using
 *  jdk 1.4.2
 *
 *  Revision 1.1  2003/10/21 21:21:56  dennis
 *  Client to send operator to RemoteOpExecServer to
 *  request remote execution of operator on a server.
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
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.operator.Generic.Load.*;
import Operators.*;

/**
 *  This class provides access to a RemoteOpExecServer.  The client must 
 *  use the same port as was specified when starting the server.
 */
public class RemoteOpExecClient extends TCPClient
{

  /** 
   *  Execute the specified operator on the RemoteOpExecServer.
   *
   *  @param  op   The operator to be executed on the remote system.
   *  
   *  @return the result of executing the operator's getResult() method
   */
  public Object getResult( Operator op )
  {
     String username = SharedData.getProperty("user.name");
     String password = "dummypassword";
     ExecuteOpCommand command = 
                      new ExecuteOpCommand( CommandObject.RETURN_RESULT,
                                            username,
                                            password,
                                            op,
                                            "",
                                            "" );

    Object result = getObjectFromServer( command );
    return result;
  }


  /** 
   *  Execute the specified operator on the RemoteOpExecServer, and save
   *  the result in the specified file.
   *
   *  @param  op         The operator to be executed on the remote system.
   *  @param  file_name  The name of the file where the result should be
   *                     saved.
   *  @param  dir_name   The name of the directory where the file should
   *                     be written.
   *  
   *  @return status message
   */
  public String saveResultInFile( Operator op, 
                                  String file_name, 
                                  String dir_name )
  {
     String username = SharedData.getProperty("user.name");
     String password = "dummypassword";
     ExecuteOpCommand command = 
                      new ExecuteOpCommand( CommandObject.RESULT_IN_FILE,
                                            username,
                                            password,
                                            op,
                                            file_name,
                                            dir_name );

    Object result = getObjectFromServer( command );
    if ( result != null )
      return result.toString();
    else
      return "ERROR: status message from server was null";
  } 
  

  /* ------------------------------ main ------------------------------- */
  /**
   *  Main program to run client in stand alone mode for testing purposes.
   */
   public static void main( String args[] )
   {
      RemoteOpExecClient client = new RemoteOpExecClient();

      client.parseArgs( args );        // get server and port from command line

      client.MakeConnection();         // connect and make sure we're ok
      if ( client.isConnected() )
        System.out.println("Connection OK");
      else
      {
        System.out.println("Connection failed to " + client.getHost() +
                           " on port " + client.getPort() );
        System.exit(1);
      }
                                           // now test a couple of operators
                                           // starting with the hello operator
      Operator op = new HelloOperator(); 
      Object result = client.getResult(op);
      if ( result != null )
        System.out.println("Result of HelloOperator is " + result.toString() );
      else
        System.out.println("Got back null from HelloOperator");

                                           // Ysquared operator on empty DataSet
      op = new Ysquared();
      result = client.getResult(op);
      if ( result != null )
        System.out.println("Result of Ysquared is " + result.toString() );
      else
        System.out.println("Got back null from Ysquared operator");

                                           // Load a DataSet (edit this to 
                                           // specify a DataSet on the server)
      String runfile = "/usr/local/ARGONNE_DATA/hrcs1797.run";
      op = new LoadOneHistogramDS( runfile, 1, "" ); 
      result = client.getResult(op);
      if ( result instanceof DataSet )
      {
        System.out.println("Result of LoadOneHistogramDS is a DataSet...");
        ViewManager vm = new ViewManager( (DataSet)result, ViewManager.IMAGE );
      }
      else
        System.out.println("LoadOneHistogramDS returned " + result.toString());

                                           // now test executing and saving 
                                           // the result remotely
      op = new HelloOperator(); 
      result = client.saveResultInFile( op, "HelloResult.txt", "");
      System.out.println( "Result of executing and saving result " +
                          " of HelloOperator is " + result.toString() );

      op = new LoadOneHistogramDS( runfile, 1, "" );
      result = client.saveResultInFile( op, "LoadResult.isd", "");
      System.out.println( "Result of executing and saving result " +
                          " of LoadOneHistogramDS is " + result.toString() );

   }

}


/*
 * File:  RemoteFileRetriever.java      
 *
 * Copyright (C) 2001, Dennis Mikkelson,
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
 *  Revision 1.1  2001/08/03 21:41:48  dennis
 *  Retriever for files from remote machine.
 *
 *
 */
package DataSetTools.retriever;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import java.io.*;
import java.net.*;
import java.util.*;
import NetComm.*;

/**
 *  This class is a base class for retrievers that get data from a 
 *  NetComm.DataServer using a TCP connection.
 */
public class RemoteFileRetriever extends    RemoteDataRetriever 
                                 implements Serializable
{
 
  DataSet data_set[]   = null;
  int     ds_type[]    = null;
 

/* ----------------------------- Constructor ----------------------------- */
/**
 *  Construct a RemoteFileRetriever to get data from a DataSetServer
 *
 *  @param data_source_name  String containing the host name, port, password
 *                           and file_name for the server.
 */
  public RemoteFileRetriever( String data_source_name ) 
  {
    super(data_source_name);
    password = "RemoteFileRetriever";

    if ( isConnected() )             // load up the DataSet type array and
    {                                // make room for the DataSets.

      Object obj = getObjectFromServer( DataSetServer.COMMAND_GET_DS_TYPES +
                                        file_name );

      if ( obj instanceof int[] )    
      {
        ds_type = (int[]) obj;

        System.out.print("Got array of ints = ");
        for ( int i = 0; i < ds_type.length; i++ )
          System.out.print(" " + i );
        System.out.println();

        data_set = new DataSet[ ds_type.length ];
        for ( int i = 0; i < data_set.length; i++ )
          data_set[i] = null;
      }
      else
        System.out.println("Didn't get int[]");
    }
  }


/* ----------------------------- numDataSets ---------------------------- */
/**
 *  Get the number of distinct DataSets from this DataSetServer. 
 *  The monitors are placed into one DataSet.  Any sample histograms are 
 *  placed into separate DataSets.  
 *   
 *  @return If the connection was made and the specified file was valid,
 *          return the number of distinct DataSets in this file.  If the
 *          connection to the server was not made, return -1.  If the 
 *          connection was made, but the file was not valid, return 0;
 */  
  public int numDataSets()
  { 
    if ( ds_type == null )
    {
      if ( server_alive && user_pass_ok )
        return BAD_FILE_NAME;
      
      if ( server_alive )
        return BAD_USER_OR_PASSWORD;

      return SERVER_DOWN;
    }
    else
      return ds_type.length;
  }


/* ------------------------------ getType ------------------------------- */
/**
 * Get the type of the specified data set from the current data source.
 * The type is an integer flag that indicates whether the data set contains
 * monitor data or data from other detectors.
 *
 * @param  data_set_num   Specifies which DataSet's type is requested.
 *
 * @return The type of the specified DataSet, or the flag 
 *         Retriever.INVALID_DATA_SET
 */

  public int getType( int data_set_num )
  {
    if ( ds_type      == null || 
         data_set_num <  0    || 
         data_set_num >= ds_type.length )
      return Retriever.INVALID_DATA_SET;            // something is wrong, so 
                                                    // DataSet type is invalid
    else
      return ds_type[ data_set_num ];
  }


/* ---------------------------- getDataSet ----------------------------- */
/**
 *  Get the specified DataSet from this runfile.
 * 
 *  @param  data_set_num  The number of the DataSet in this runfile 
 *                        that is to be read from the runfile.  data_set_num
 *                        must be between 0 and numDataSets()-1
 *
 *  @return the requested DataSet.
 */
  public DataSet getDataSet( int data_set_num )
  {
    if ( data_set     == null ||                
         data_set_num <  0    || 
         data_set_num >= data_set.length )
      return null;

    if ( data_set[ data_set_num ] != null )      // use cached copy if possible
      return data_set[ data_set_num ];

    if ( !isConnected() )
      MakeConnection();

    Object obj = getObjectFromServer( DataSetServer.COMMAND_GET_DS + 
                                      file_name + " " +
                                      data_set_num );
    if ( obj != null && obj instanceof DataSet )
    {
      DataSet ds = (DataSet)obj;
      data_set[ data_set_num ] = ds;
      return ds;
    }
    else
      System.out.println("Failed to get DataSet " + 
                          data_set_num + " from " + file_name );

    return null;
  }


/* ------------------------------------------------------------------------
 *
 *  MAIN PROGRAM FOR TESTING PURPOSES ONLY 
 *
 */

/* -------------------------------- main --------------------------------- */

  public static void main( String args[] )
  {
//  String server_name =  "mscs138.mscs.uwstout.edu";
//    String server_name =  "dmikk.mscs.uwstout.edu";
    String server_name =  "mandrake.pns.anl.gov";

    System.out.println("====================================================");
    System.out.println("Making retriever to get DataSets from:" + server_name);
    System.out.println("====================================================");
    RemoteFileRetriever retriever = new RemoteFileRetriever( 
              server_name+";;;junk_pass;lrcs3000.nxs" );

    DataSet monitor_ds = retriever.getDataSet(0);
    if ( monitor_ds != null )
    {
      System.out.println("Got DataSet " + monitor_ds );
      ViewManager monitor_vm = new ViewManager( monitor_ds, IViewManager.IMAGE);
    }
    else
      System.out.println("ERROR: no monitor data set");

    DataSet hist_ds = retriever.getDataSet(1);
    if ( hist_ds != null )
    {
      System.out.println("Got DataSet " + hist_ds );
      ViewManager hist_vm = new ViewManager( hist_ds, IViewManager.IMAGE );
    }
    else
      System.out.println("ERROR: no histogram data set");

    monitor_ds = retriever.getDataSet(0);
    if ( monitor_ds != null )
    {
      System.out.println("Got DataSet " + monitor_ds );
      ViewManager monitor_vm = new ViewManager( monitor_ds, IViewManager.IMAGE);
    }
    else
      System.out.println("ERROR: no monitor data set");
  }

}

/*
 * File:  LiveDataRetriever.java      
 *
 * Copyright (C) 2001, Dennis Mikkelson, Alok Chatterjee
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
 *  Revision 1.22  2002/11/27 23:23:16  pfpeterson
 *  standardized header
 *
 *  Revision 1.21  2002/10/03 15:50:50  dennis
 *  Replace call to Data.setSqrtErrors() to Data.setSqrtErrors(true)
 *
 *  Revision 1.20  2002/06/03 14:23:00  dennis
 *  Now sets the errors to sqrt(counts) when recieving a DataSet, since the
 *  errors are not set by the LiveDataServer.
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
 *  This class opens a TCP connection to a LiveDataServer object running
 *  on a remote machine and extracts the DataSets corresponding to
 *  monitors and sample histograms.
 */
public class LiveDataRetriever extends    RemoteDataRetriever 
                               implements Serializable
{
  public static final int TIMEOUT_MS = 60000;

  TCPComm tcp_io = null;

/* ----------------------------- Constructor ----------------------------- */
/**
 *  Construct a LiveDataRetriever to get data from a LiveDataServer running
 *  a specified instrument computer.
 *
 *  @param data_source_name  The host name for the instrument computer.  It
 *                           is assumed that a LiveDataServer is running on
 *                           that computer on the required port. 
 */
  public LiveDataRetriever(String data_source_name) 
  {
    super(data_source_name);

    MakeConnection();
  }


/* ----------------------------- numDataSets ---------------------------- */
/**
 *  Get the number of distinct DataSets from this LiveDataServer. 
 *  The monitors are placed into one DataSet.  Any sample histograms are 
 *  placed into separate DataSets.  
 *   
 *  @return the number of distinct DataSets on this LiveDataServer.
 */  
  public int numDataSets()
  {
              // include file_name for compatibilty with RemoteFileRetriever

    Object obj = getObjectFromServer( DataSetServer.COMMAND_GET_DS_TYPES +
                                      file_name );

    if ( obj != null && obj instanceof int[] )
    {
      int types[] = (int[])obj;
      return types.length; 
    }

    if ( server_alive && user_ok && password_ok )
      return WRONG_SERVER_TYPE;

    if ( server_alive && user_ok )
      return BAD_PASSWORD;

    if ( server_alive )
      return BAD_USER_NAME;

    return SERVER_DOWN;
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
              // include file_name for compatibilty with RemoteFileRetriever

    Object obj = getObjectFromServer( DataSetServer.COMMAND_GET_DS_TYPES +
                                      file_name );

    if ( obj != null && obj instanceof int[] )
    {
      int types[] = (int[])obj;
      if ( data_set_num < 0 || data_set_num >= types.length )
        return Retriever.INVALID_DATA_SET;
      else
        return types[data_set_num];
    }

    return INVALID_DATA_SET;
  }


/* ---------------------------- getDataName ----------------------------- */
/**
 *  Get the name of current data on the LiveDataServer.
 *
 *  @return String containing the name of the Data.
 */
  public String getDataName()
  {
              // include file_name for compatibilty with RemoteFileRetriever

    Object obj = getObjectFromServer( DataSetServer.COMMAND_GET_DATA_NAME +
                                      file_name );

    if ( obj != null && obj instanceof String )
    {
      String name = (String)obj;
      return name;
    }

    return new String("NONE");
  }


/* ---------------------------- getDataSet ----------------------------- */
/**
 *  Get the specified DataSet from the LiveDataServer.
 * 
 *  @param  data_set_num  The number of the DataSet on the LiveDataServer 
 *                        that is to be obtained.  data_set_num
 *                        must be between 0 and numDataSets()-1
 *
 *  @return the requested DataSet.
 */
  public DataSet getDataSet( int data_set_num )
  {
    Object obj = getObjectFromServer( DataSetServer.COMMAND_GET_DS +
                                      file_name + " " +
                                      data_set_num );

    if ( obj != null && obj instanceof DataSet )
    {
      DataSet ds = (DataSet)obj;
      TabulatedData d;
                                          // fix the total counts attributes
                                          // and error arrays
      for ( int i = 0; i < ds.getNum_entries(); i++ )
      {
        d = (TabulatedData)ds.getData_entry(i);
        d.setSqrtErrors( true ); 
        float y[] = d.getY_values();
        float total = 0;
        for ( int j = 0; j < y.length; j++ )
          total+= y[j];
        d.setAttribute( new FloatAttribute( Attribute.TOTAL_COUNT, total ) ); 
      }
      return ds;
    }

    return null;
  }


/* ------------------------------- status ------------------------------- */
/**
 *  Get a status message for the current retriever state.  By default, this
 *  just returns a string message corresponding to an error code obtained
 *  from the numDataSets method.
 */
 public String status()
 {
   Object obj = getObjectFromServer( TCPServer.COMMAND_GET_STATUS + file_name );

   if ( obj != null && obj instanceof String )
     return (String)obj;

   if ( !isConnected() )
     return NOT_CONNECTED_STRING;

   return SERVER_ERROR_STRING; 
 }


/* ------------------------------------------------------------------------
 *
 *  PROTECTED METHODS
 *
 */

/* ------------------------------ finalize ---------------------------- */
/**
 *  Finalize method to make sure that the TCP connection is closed if this
 *  LiveDataRetriever object is no longer being used.
 */
  protected void finalize() throws IOException
  {
     Exit();
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
    String server_name =  "dmikk.mscs.uwstout.edu";
//  String server_name =  "mandrake.pns.anl.gov";

    System.out.println("====================================================");
    System.out.println("Making retriever to get DataSets from:" + server_name);
    System.out.println("====================================================");
    LiveDataRetriever retriever = new LiveDataRetriever( server_name );

    DataSet     monitor_ds = retriever.getDataSet(0);
    ViewManager monitor_vm = new ViewManager( monitor_ds, IViewManager.IMAGE);

    DataSet     hist_ds = retriever.getDataSet(1);
    ViewManager hist_vm = new ViewManager( hist_ds, IViewManager.IMAGE );

//    retriever.Exit();

  // to verify that the time attribute has been set, uncomment this loop
/*  for ( int i = 0; i < hist_ds.getNum_entries(); i++ )
    {
      Data d = hist_ds.getData_entry(i);
      System.out.println( "For entry " + i + " Time = " + 
                        (String)(d.getAttributeValue(Attribute.UPDATE_TIME)));
     }
*/
    
  }
}

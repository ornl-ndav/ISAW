/*
 * File:  LiveDataManager.java      
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
 *  Revision 1.18  2003/03/04 20:37:40  dennis
 *  Now keeps less state information and goes to the server
 *  for simple requests.
 *  Added method isGettingDS() that is checked by the LiveDataMonitor,
 *  so that additional requests for large DataSets are rejected
 *  while the LiveDataManager is busy getting a DataSet.
 *  Added method getDataSetName() that is used by the LiveDataMonitor
 *  to produce the list of available DataSets, without having to
 *  first load the DataSets from the server.
 *
 *  Revision 1.17  2002/11/27 23:23:16  pfpeterson
 *  standardized header
 *
 *  Revision 1.16  2002/04/18 21:29:05  dennis
 *  Added private method getValidDataSet(ds) that will return a clone
 *  of the EMPTY_DATA_SET if ds is null, or return ds otherwise.  This
 *  is used to ensure that even if the LiveDataRetriever returns a null
 *  for a DataSet, we only save valid DataSets.
 *
 */
package DataSetTools.retriever;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import java.awt.event.*;
import NetComm.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;

/**
 *  This class constructs a LiveDataRetriever object for a specified source
 *  and periodically gets an updated version of the data from the 
 *  LiveDataRetriever.
 *
 *  @see NetComm.LiveDataServer
 *  @see LiveDataRetriever
 *  @see DataSetTools.components.ui.LiveDataMonitor
 */
public class LiveDataManager extends    Thread 
                             implements Serializable
{
  public static final int    MIN_DELAY   = 10;       // minimum delay in seconds
  public static final int    MAX_DELAY   = 600;      // maximum delay in seconds
  public static final String DATA_CHANGED   = "Data Changed ";

  private Vector            listeners   = null;        
  private LiveDataRetriever retriever   = null;
  private DataSet           data_sets[] = new DataSet[0];
  private boolean           ignore[]    = new boolean[0];
                                                      // flags to mark which
                                                      // data sets won't be
                                                      // automatically updated
  private int               getting_ds  = -1;

  private int               time_ms     = 3*MIN_DELAY*1000;
  private int               error_flag  = RemoteDataRetriever.NOT_CONNECTED;

  private String last_data_name = "NONE";


/* ----------------------------- Constructor ----------------------------- */
/**
 *  Construct a LiveDataManager to get data from a LiveDataServer running
 *  on a specified instrument computer.
 *
 *  @param data_source_name  The host name for the instrument computer.  It
 *                           is assumed that a LiveDataServer is running on
 *                           that computer on the required port. 
 */
  public LiveDataManager(String data_source_name) 
  {
    retriever = new LiveDataRetriever( data_source_name );
    listeners = new Vector();
    SetUpLocalCopies();
    this.start();
  }

/* ----------------------------- numDataSets ----------------------------- */
/**
 *  Get the number of distinct DataSets from the current data source. 
 *  The monitors are placed into one DataSet.  Any sample histograms are 
 *  placed into separate DataSets.  
 *   
 *  @return the number of distinct DataSets in this runfile.
 */  
  public int numDataSets()
  { 
    if ( error_flag < 0 )
      return error_flag;

    return retriever.numDataSets();
  }

/* -------------------------------- getType ------------------------------ */
/**
 * Get the type of the specified data set from the current data source.
 * The type is an integer flag that indicates whether the data set contains
 * monitor data or data from other detectors.
 *
 *  @param  data_set_num  The number of the DataSet to be returned.
 *
 *  @return the type of the specified DataSet.
 */

  public int getType( int data_set_num )
  {
    return retriever.getType( data_set_num );
  }


/* ------------------------------ getDataSet ----------------------------- */
/**
 *  Get the specified DataSet from the current data source.
 * 
 *  @param  ds_num  The number of the DataSet to be returned.
 *    
 *  @return the requested DataSet.
 */
  public DataSet getDataSet( int ds_num )
  {
    if ( ds_num < 0 )
      return null;

    if ( ds_num >= data_sets.length  &&
         ds_num <  numDataSets() )
      SetUpLocalCopies();

    if ( ds_num >= 0 && ds_num < data_sets.length )      // valid ds_num, so 
    {                                                    // work with copy
      if ( data_sets[ ds_num ] == null || 
          !data_sets[ ds_num ].getTitle().equals(getDataSetName(ds_num) ) )
      {
        UpdateDataSetNow( ds_num );                       // bad local copy
      }
      return data_sets[ds_num];          
    }
    else 
      return null;

  }


/* ------------------------------ getDataSet ----------------------------- */
/**
 *  Get the specified portion of the specified DataSet from the current 
 *  data source.
 *
 *  @param  data_set_num  The number of the DataSet to be returned.
 *
 *  @return the requested DataSet.
 */
  public DataSet getDataSet( int    data_set_num, 
                             String group_ids,
                             float  min_x,
                             float  max_x,
                             int    rebin_factor,
                             int    attr_mode      )
  {
    return getDataSet( data_set_num );
  }


/* ---------------------------- getDataSetName ----------------------------- */
/**
 *  Get the name of the specified DataSet from the current data source.
 *
 *  @param  data_set_num  The number of the DataSet whose name is 
 *                        to be returned.
 *
 *  @return the name of the specified DataSet.
 */
  public String getDataSetName( int data_set_num )
  {
    return retriever.getDataSetName( data_set_num );
  }


/* --------------------------- setUpdateInterval -------------------------- */
/**
 *  Set the time interval after which the DataSets will be updated with data
 *  from the current data source.
 *
 *  @param  seconds  The number of seconds between automatic updates of the
 *                   DataSets.  This must be between the values given by
 *                   MIN_DELAY and MAX_DELAY.
 */
 public void setUpdateInterval( double seconds )
 {
   if ( seconds <= MIN_DELAY )
     time_ms = MIN_DELAY * 1000;
   else if ( seconds >= MAX_DELAY )
     time_ms = MAX_DELAY * 1000;
   else  
     time_ms = (int)( 1000 * seconds );
 }


/* --------------------------- getUpdateInterval -------------------------- */
/**
 *  Set the time interval after which the DataSets will be updated with data
 *  from the current data source.
 *
 *  @return  The number of seconds between automatic updates of the DataSets.
 */
 public double getUpdateInterval( )
 {
   return time_ms/1000.0;
 }


/* -------------------------- setUpdateIgnoreFlag ------------------------- */
/**
 *  Select whether or not the specified DataSet should be ignored during
 *  automatic updates from the current data source.
 *
 *  @param  data_set_num  The number of the DataSet whose ignore flag is
 *                        being set.
 *  @param  ignore        Flag value... use "true" if the DataSet should
 *                        not be automatically updated and use "false"
 *                        otherwise. 
 */
 public void setUpdateIgnoreFlag( int data_set_num, boolean ignore )
 {
   if ( data_set_num >= 0 && data_set_num < data_sets.length )
     this.ignore[ data_set_num ] = ignore;
 }


/* -------------------------- getUpdateIgnoreFlag ------------------------- */
/**
 *  Get the current state of the update ignore flag.
 *
 *  @param  data_set_num  The number of the DataSet whose ignore flag is
 *                        requested.
 *
 *  @return  the update ignore flag for the specified DataSet.
 */
 public boolean getUpdateIgnoreFlag( int data_set_num )
 {
   if ( data_set_num >= 0 && data_set_num < data_sets.length )
     return this.ignore[ data_set_num ];
 
   else
     return true;
 }


/* ----------------------------- isGettingDS ----------------------------- */
/**
 *  Find out if the communications channel is currently busy getting a 
 *  DataSet.
 */
 public boolean isGettingDS()
 {
   if ( getting_ds >= 0 )
     return true;
   else
     return false;
 }


/* ---------------------------- UpdateDataSetNow -------------------------- */
/**
 *  Request an immediate update of the specified DataSet.
 *
 *  @param  data_set_num  The number of the DataSet that is to be updated
 *                        immediately.
 */
 synchronized public void UpdateDataSetNow( int data_set_num )
 {
   if ( data_set_num < 0 )
     return;

   if ( data_set_num > data_sets.length - 1 )
     SetUpLocalCopies();

   getting_ds = data_set_num; // if a thread starts to get a  DataSet, set flag
                              // so the run() method doesn't queue up another
                              // request for a DataSet.               

   DataSet temp_ds = retriever.getDataSet( data_set_num );

   if ( temp_ds == data_sets[data_set_num] )
     System.out.println("ERROR!!!! same data set" );

   if ( temp_ds != null )
   { 
     if ( data_sets[data_set_num] == null )             // save new DataSet
       data_sets[data_set_num] = temp_ds;
     else                                               // or copy to current DS
       data_sets[data_set_num].shallowCopy( temp_ds );  // copy notifies the
                                                        // DataSet's observers
   }

   getting_ds = -1;                                      // reset the flag
 }

 /* ------------------------ addActionListener -------------------------- */
 /**
  *  Add an ActionListener for this LiveDataManager.  Whenever a DataSet with
  *  a new title is obtained from the LiveDataServer, an action event will
  *  be sent to the listeners, indicating that the DataSet has been changed. 
  *
  *  @param listener  An ActionListener whose ActionPerformed() method is
  *                   to be called when a DataSet with a new title is received.
  */
  public void addActionListener( ActionListener listener )
  {
    for ( int i = 0; i < listeners.size(); i++ )       // don't add it if it's
      if ( listeners.elementAt(i).equals( listener ) ) // already there
        return;

    listeners.add( listener );
  }


 /* ------------------------ removeActionListener ------------------------ */
 /**
  *  Remove the specified ActionListener from this LiveDataManager.  If
  *  the specified ActionListener is not in the list of ActionListeners for
  *  for this LiveDatamanager this method has no effect.
  *
  *  @param listener  The ActionListener to be removed.
  */
  public void removeActionListener( ActionListener listener )
  {
    listeners.remove( listener );
  }


/* -------------------------- send_message ------------------------------- */
/**
 *  Send a message to all of the action listeners for this LiveDataManager
 */
 public void send_message( String message )
 {
   for ( int i = 0; i < listeners.size(); i++ )
   {
     ActionListener listener = (ActionListener)listeners.elementAt(i);
     listener.actionPerformed( new ActionEvent( this, 0, message ) );
   }
 }


/* -------------------------------- run --------------------------------- */
/**
 *  The run method for this LiveDataManager.  This will sleep and 
 *  periodically check for new data from the LiveDataRetriever.
 */
 public void run()
 {
   while ( true )
   {
     try
     {
       for ( int step = 0; step < 20; step++ )      // Do 20 separate sleeps,
         sleep( time_ms / 20 );                     // so a long sleep will
                                                    // end sooner if time_ms
                                                    // is altered. 

       if ( getting_ds < 0 )                        // don't try talking to the
                                                    // server if it's busy.
       {
         send_message( retriever.status() );        // notify LiveDataMonitor
                                                    // what current status is

         int n_ds = retriever.numDataSets();        // if number of DataSets
         if (  n_ds != data_sets.length )           // changed, fix our lists
           SetUpLocalCopies();                               

         else if ( n_ds != error_flag )             // if error status changed
         {                                          // notify LiveDataMonitor
           error_flag = n_ds;
           send_message(  DATA_CHANGED + "Run 1: " );
         }

         String name = retriever.getDataSetName(0);   // if the run changed,
         if ( !name.equals( last_data_name ) )        // notify LiveDataMonitor
         {
           last_data_name = name;
           send_message(  DATA_CHANGED + "Run 2: " + name );
         }

         for ( int i = 0; i < data_sets.length; i++ )    // update the DataSets
         {
           if ( !ignore[i] )                             // we are interested in
             UpdateDataSetNow( i );
         }
       } 
     }
     catch ( Exception e )
     {
       System.out.println("Exception in LiveDataManager.run() is:" + e );
       e.printStackTrace();
     }
   }
 }


/* -------------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

/* --------------------------- SetUpLocalCopies -------------------------- */
/*
 *  Construct or resize the local copies of the DataSets, together with
 *  the types and "ignore" flags for the DataSets on the remote server.
 */

  synchronized private void SetUpLocalCopies()
  {
    if ( retriever == null )
    {
      error_flag = RemoteDataRetriever.NOT_CONNECTED;
      send_message( DATA_CHANGED + "SetUpLocalCopies 1: " );
      return;
    }

    int num_ds = retriever.numDataSets();

    if ( error_flag != num_ds )
    {
      error_flag = num_ds; 
      send_message(  DATA_CHANGED + "SetUpLocalCopies 2: " );
    }

    if ( num_ds < 0 )                          // error flag... lost the server
      return;                                  // so just return, leaving local
                                               // copies unchanged. 

    int num_to_save = Math.min( num_ds, data_sets.length );

    if ( num_ds != data_sets.length )           // we must resize our lists
    {
      DataSet new_data_sets[] = new DataSet[ num_ds ];
      boolean new_ignore[]    = new boolean[ num_ds ];

      for ( int i = 0; i < num_to_save; i++ )           // save what we can
      {                                                 // of the old ones
        new_data_sets[i] = data_sets[i];
        new_ignore[i]    = ignore[i];
      }
      data_sets = new_data_sets;
      ignore    = new_ignore;
                                                        // initialize new ones
      for ( int i = num_to_save; i < num_ds; i++ )
      {
        data_sets[i] = null;
        ignore[i]    = true;
      }

      send_message(  DATA_CHANGED + "SetUpLocalCopies 3: " );
    }
  }

  /*
   *  Get a valid DataSet that will be the specified DataSet if it is non-null
   *  and will be a copy of the EMPTY_DATASET if the specified DataSet is null 
   */
  private DataSet getValidDataSet( DataSet ds )
  {
    if ( ds == null )
      return (DataSet)DataSet.EMPTY_DATA_SET.clone();

    return ds; 
  }

/* -------------------------------- main --------------------------------- */
  
  public static void main( String args[] )
  {
//  String server_name =  "mscs138.mscs.uwstout.edu";
    String server_name =  "dmikk.mscs.uwstout.edu";
//  String server_name =  "mandrake.pns.anl.gov";

    System.out.println("====================================================");
    System.out.println("Making manager to get DataSets from:" + server_name);
    System.out.println("====================================================");
    LiveDataManager manager = new LiveDataManager( server_name );

    DataSet     monitor_ds = manager.getDataSet(0);
    ViewManager monitor_vm = new ViewManager( monitor_ds, IViewManager.IMAGE);

    DataSet     hist_ds = manager.getDataSet(1);
    ViewManager hist_vm = new ViewManager( hist_ds, IViewManager.IMAGE );

  }
}

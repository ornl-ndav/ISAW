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
 *  Revision 1.14  2001/08/14 15:11:14  dennis
 *  Now periodically gets the server status and sends it to the
 *  LiveDataMonitor.
 *
 *  Revision 1.13  2001/08/10 19:52:16  dennis
 *  Added test for data_sets[data_set_num] == null in
 *  UpdateDataSetNow() method.
 *
 *  Revision 1.12  2001/08/09 15:24:06  dennis
 *  Put debug prints in "if (debug_retriever)" blocks.
 *
 *  Revision 1.11  2001/08/08 14:01:40  dennis
 *  Improved handling of list of ActionListeners and sending messages
 *  to ActionListeners.
 *  First stage integration of new error messages.
 *
 *  Revision 1.10  2001/08/07 21:36:33  dennis
 *  Added error_flag and error codes.
 *
 *  Revision 1.9  2001/06/11 18:00:31  dennis
 *  Now calls Exit() and MakeConnection() on the retriever
 *  every time a data set is obtained to avoid a memory
 *  leak.
 *
 *  Revision 1.8  2001/06/08 22:05:14  dennis
 *  UpdateDataSetNow() now checks for an invalid data_set_num and
 *  refreshes the local state is the data_set_num is too large.
 *
 *  Revision 1.7  2001/06/07 16:45:09  dennis
 *  Now periodically checks for a change in the number of DataSets
 *  available and reinitializes its local data if this changes.
 *
 *  Revision 1.6  2001/06/06 21:22:59  dennis
 *  Now uses ActionEvents to notify listeners that the Runfile
 *  has been changed.
 *
 *  Revision 1.5  2001/04/25 21:57:47  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.4  2001/03/01 23:22:23  dennis
 *  Now uses a private method SetUpLocalCopies() to initialize its
 *  local copies of the DataSets and associated info.  This will make
 *  it easier to re-initialize these items when the DAS changes to
 *  a new run.
 *
 *  Revision 1.3  2001/02/20 23:02:50  dennis
 *  Added constants for MIN_DELAY and MAX_DELAY.  Also, made
 *  minor improvement to documentation.
 *
 *  Revision 1.2  2001/02/16 16:39:01  dennis
 *  Do 20 small sleeps, to allow a changed sleep time to
 *  take effect faster.  Added some @see comments.
 *
 *  Revision 1.1  2001/02/15 23:25:23  dennis
 *  Class to control periodic updates to live data sets
 *  by periodically getting new data from a LiveDataServer.
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
  public static final int    NO_DATA_MANAGER = -10;
  public static final int    NO_CONNECTION   = -11;

  public static final int    MIN_DELAY   = 10;       // minimum delay in seconds
  public static final int    MAX_DELAY   = 600;      // maximum delay in seconds
  public static final String DATA_CHANGED   = "Data Changed ";

  private Vector            listeners   = null;        
  private LiveDataRetriever retriever   = null;
  private DataSet           data_sets[] = new DataSet[0];
  private int               ds_type[]   = new int[0];
  private boolean           ignore[]    = new boolean[0];
                                                      // flags to mark which
                                                      // data sets won't be
                                                      // automatically updated
  private int               time_ms     = 3*MIN_DELAY*1000;
  private int               error_flag  = NO_CONNECTION; 

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

    return data_sets.length;
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
    if ( data_set_num >= 0 && data_set_num < data_sets.length )
      return ds_type[ data_set_num ];
    else
      return Retriever.INVALID_DATA_SET;
  }


/* ------------------------------ getDataSet ----------------------------- */
/**
 *  Get the specified DataSet from the current data source.
 * 
 *  @param  data_set_num  The number of the DataSet to be returned.
 *    
 *  @return the requested DataSet.
 */
  public DataSet getDataSet( int data_set_num )
  {
    if ( data_set_num >= 0 && data_set_num < data_sets.length )
      return data_sets[ data_set_num ];
    else
      return null;
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


   DataSet temp_ds = retriever.getDataSet( data_set_num );
   retriever.Exit();
   retriever.MakeConnection();

   if ( temp_ds == data_sets[data_set_num] )
     System.out.println("ERROR!!!! same data set" );

   if ( temp_ds != null )
   {                                             // check for change of DataSet
                                                 // and re-initialize if needed

     if ( data_sets[data_set_num] == null   ||
         !temp_ds.getTitle().equals( data_sets[data_set_num].getTitle() ) )
       SetUpLocalCopies();
     else
       data_sets[data_set_num].copy( temp_ds );  // copy notifies the observers
                                                 // of the DataSets 
   }
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
 *  Send a message to all of the action listeners for this panel
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
       send_message( retriever.status() );

       int n_ds = retriever.numDataSets();
       if (  n_ds != data_sets.length ) 
         SetUpLocalCopies();                               

       else if ( n_ds != error_flag )
       {
         error_flag = n_ds;
         send_message(  DATA_CHANGED + "Run 1: " );
       }

       boolean new_runfile = false;
       String name = retriever.getDataName();
       if ( !name.equals( last_data_name ) )
       {
         last_data_name = name;
         new_runfile = true; 
       }

       for ( int i = 0; i < data_sets.length; i++ )
         if ( !ignore[i] || new_runfile )
           UpdateDataSetNow( i );
     
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
      error_flag = NO_CONNECTION;
      send_message( DATA_CHANGED + "SetUpLocalCopies 1: " );
    }

    else
    {
      int num_ds      = retriever.numDataSets();

      if ( error_flag != num_ds )
      {
        error_flag = num_ds; 
        send_message(  DATA_CHANGED + "SetUpLocalCopies 2: " );
      }

      if ( num_ds < 0 )    
        num_ds = 0;

      int num_to_save = Math.min( num_ds, data_sets.length );

      if ( num_ds != data_sets.length )           // we must resize our lists
      {
        DataSet new_data_sets[] = new DataSet[ num_ds ];
        int     new_ds_type[]   = new int    [ num_ds ];
        boolean new_ignore[]    = new boolean[ num_ds ];

        for ( int i = 0; i < num_to_save; i++ )           // save what we can
        {                                                 // of the old ones
          new_data_sets[i] = data_sets[i];

          DataSet temp_ds  = retriever.getDataSet(i);
          new_data_sets[i].copy( temp_ds );              // copy notifies any
                                                         // observers of the ds
          new_ignore[i]    = ignore[i];
        }
        data_sets = new_data_sets;
        ds_type   = new_ds_type;
        ignore    = new_ignore;
                                                          // initialize new ones
        for ( int i = num_to_save; i < num_ds; i++ )
        {
          ds_type[i]   = retriever.getType(i);
          data_sets[i] = retriever.getDataSet(i);
          ignore[i]    = true;
        }
        send_message(  DATA_CHANGED + "SetUpLocalCopies 3: " );
      }

      else if ( num_ds > 0 )
      {                                                 // refresh our existing 
        for ( int i = 0; i < num_ds; i++ )              // lists
        {
          ds_type[i]   = retriever.getType(i);

          DataSet temp_ds  = retriever.getDataSet(i);
          data_sets[i].copy( temp_ds );                  // copy notifies any
                                                         // observers of the ds
        }
        send_message(  DATA_CHANGED + "SetUpLocalCopies 4: " );
      }
    }
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

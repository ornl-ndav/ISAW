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
 *  Revision 1.23  2004/02/17 19:08:05  dennis
 *  Added method to calculate the total counts after getting all
 *  or part of a DataSet.  This was previously done in the
 *  getDataSet() method of the LiveDataRetriever class.  However,
 *  since the system was revised to get partial DataSets, the
 *  method to get the full data set was bypassed and the TotalCount
 *  attribute was not being set.
 *
 *  Revision 1.22  2003/10/16 17:21:24  dennis
 *  Fixed javadocs to build cleanly with jdk 1.4.2
 *
 *  Revision 1.21  2003/03/14 20:47:07  dennis
 *  The UpdateDataSetNow() method now verifies that it actually
 *  got a DataSet when one was requested.
 *  If the DataManager's run method terminates, the TCP connection
 *  is now exited.
 *
 *  Revision 1.20  2003/03/10 06:05:00  dennis
 *  Now records the last command that was used to load the local
 *  copy of a DataSet and also the next command that should be
 *  used when an update is requested for the DataSet.  Added
 *  method setNextUpdateCommand()
 *  Methods getDataSet(i) and UpdateDataSet(i) now just call the
 *  versions that take a command as a parameter, after forming
 *  the appropriate command.
 *  getDataSet(command) just returns the local copy if there is
 *  one, unless the command for getting it has changed, or the
 *  run has changed (i.e. new data name).
 *
 *  Revision 1.19  2003/03/07 22:51:15  dennis
 *  Added methods to GET_DS_ID_RANGE and GET_DS_X_RANGE
 *  Added method to stop the thread "cleanly" by setting a
 *  flag that causes the run method to eventually terminate.
 *  Added method to remove all ActionListeners.
 *  Added methods to get/updateDataSets that take a command
 *  object as a parameter.
 *
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

  private Vector            listeners      = null;        
  private LiveDataRetriever retriever      = null;
  private DataSet           data_sets[]    = new DataSet[0];
  private GetDataCommand    next_command[] = new GetDataCommand[0];
                                                      // holds the next command
                                                      // that should be used for
                                                      // a DataSet update
  private GetDataCommand    last_command[] = new GetDataCommand[0];
                                                      // track last command used
                                                      // to get a DataSet so 
                                                      // auto update can use it
  private boolean           ignore[]        = new boolean[0];
                                                      // flags to mark which
                                                      // data sets won't be
                                                      // automatically updated
  private int               getting_ds  = -1;

  private int               time_ms     = 3*MIN_DELAY*1000;
  private int               error_flag  = RemoteDataRetriever.NOT_CONNECTED;

  private String last_data_name = "NONE";

  private boolean keep_running  = true;


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
 *  Get a copy of the current DataSet.  IF a new command has been requested,
 *  use it.  If no new command request is listed, use the last command.
 *  If there is no last command, just get the full DataSet.
 *  IF possible, just return the current copy stored in the LiveDataManager.
 *
 *  @param  ds_num  The number of the DataSet to be returned.
 *    
 *  @return the requested DataSet.
 */
  public DataSet getDataSet( int ds_num )
  {
    if ( ds_num < 0 )
      return null;

   GetDataCommand command = null;

   if ( ds_num >= 0                  &&        // try to use the next command
        ds_num < next_command.length )         // or reuse the last command     
   { 
     if( next_command[ds_num] != null  )
       command = next_command[ds_num];
     else if ( last_command[ ds_num ] != null )
       command = last_command[ds_num];
   }

   if ( command == null )                      // if all else fails get the 
     command = getDefaultCommand( ds_num );    // full DataSet

    return getDataSet( command );
  }


/* ------------------------------ getDataSet ----------------------------- */
/**
 *  Get the specified portion of the specified DataSet from the current
 *  data source.  If the command matches the last command used to get this
 *  DataSet, just return a reference to the local copy.
 *
 *  @param  command  The GetDataCommand object specifying the portion of the
 *                   DataSet to get.
 *
 *  @return the requested DataSet.
 */
  public DataSet getDataSet( GetDataCommand command )
  {
    int ds_num = command.getDataSetNumber();

    if ( ds_num < 0 )
      return null;

    if ( ds_num >= data_sets.length  &&
         ds_num <  numDataSets() )
      SetUpLocalCopies();

    if ( ds_num >= 0 && ds_num < data_sets.length )      // valid ds_num, so
    {                                                    // work with copy, if
                                                         // possible
      if ( data_sets[ ds_num ] == null               ||
          (last_command[ ds_num ] != null && 
          !command.equals( last_command[ds_num] ))   ||
          !data_sets[ ds_num ].getTitle().equals(getDataSetName(ds_num) ) )
      {
        UpdateDataSetNow( command );    // if no local copy, or doesn't match 
      }                                 // command, or doesn't match title
      return data_sets[ds_num];   
    }
    else
      return null;
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


/* ---------------------------- getDataSetIDRange ------------------------- */
/**
 *  Get the range of IDs of the specified DataSet from the current data source.
 *
 *  @param  data_set_num  The number of the DataSet whose ID range is
 *                        to be returned.
 *
 *  @return  String containing the range of IDs of the specified DataSet.
 */
  public String getDataSetIDRange( int data_set_num )
  {
    CommandObject command = 
                     RemoteDataRetriever.getDS_ID_Range( "", data_set_num );

    String range = (String)retriever.getObjectFromServer( command );
    
    if ( range != null )
      return range;
    else
      return "";
  }


/* ---------------------------- getDataSetXRange ------------------------- */
/**
 *  Get the range of X-value of the specified DataSet from the current
 *  data source.
 *
 *  @param  data_set_num  The number of the DataSet whose X range is
 *                        to be returned.
 *
 *  @return  ClosedInterval with the range of Xs of the specified DataSet.
 */
  public ClosedInterval getDataSetXRange( int data_set_num )
  {
    CommandObject command =
                     RemoteDataRetriever.getDS_X_Range( "", data_set_num );

    Object obj = retriever.getObjectFromServer( command );

    if ( obj != null && obj instanceof ClosedInterval )
      return (ClosedInterval)obj;
    else 
      return new ClosedInterval(0,0);
  }


/* --------------------------- getDefaultCommand -------------------------- */
/**
 *  Get a GetDataCommand object configured with the full list of available
 *  IDs and complete XRange for getting the full DataSet from server.
 *
 *  @param  ds_num        The number of the DataSet whose X range is
 *                        to be returned.
 *
 *  @return a GetDataCommand object with values suitable for getting the 
 *          full DataSet with Analysis attributes.
 */

  public GetDataCommand getDefaultCommand( int ds_num )
  {
    String ds_name   = getDataSetName( ds_num );
    String id_string = getDataSetIDRange( ds_num );
    ClosedInterval x_range = getDataSetXRange( ds_num );

    return RemoteDataRetriever.getDataSet( ds_name,
                                           ds_num,
                                           id_string,
                                           x_range.getStart_x(),
                                           x_range.getEnd_x(),
                                           1,
                                           Attribute.ANALYSIS_ATTRIBUTES );
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


/* ------------------------- setNextUpdateCommand ------------------------- */
/**
 *  Set the command to use the next time a DataSet is updated.
 *
 *  @param  command  the new command
 */
 public void setNextUpdateCommand( GetDataCommand command )
 {
   if ( command.getCommand() == CommandObject.GET_DS )
   {
     int ds_num = command.getDataSetNumber();
     if ( ds_num >= 0 && ds_num < next_command.length )
       next_command[ ds_num ] = command; 
   }
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
 *  Request an immediate update of the specified DataSet using the command
 *  stored in the next_command list, or the default command to get the 
 *  full DataSet, if no next_command has been defined.
 *
 *  @param  ds_num  The number of the DataSet that is to be updated
 *                  immediately.
 */
 synchronized public void UpdateDataSetNow( int ds_num )
 {
   GetDataCommand command = null;

   if ( ds_num >= 0                  &&        // try to use the next command
        ds_num < next_command.length )         // or reuse the last command
   {
     if( next_command[ds_num] != null  )
       command = next_command[ds_num];
     else if ( last_command[ ds_num ] != null )
       command = last_command[ds_num];
   }

   if ( command == null )                      // if all else fails get the
     command = getDefaultCommand( ds_num );    // full DataSet

   UpdateDataSetNow( command );
 }


/* ---------------------------- UpdateDataSetNow -------------------------- */
/**
 *  Request an immediate update of the specified DataSet using the specified
 *  command to get the desired part of the DataSet.
 *
 *  @param  command  The command specifying the number and portion of the 
 *                   DataSet that is to be updated immediately.
 */
 synchronized public void UpdateDataSetNow( GetDataCommand command )
 {
   int data_set_num = command.getDataSetNumber();
   if ( data_set_num < 0 )
     return;

   if ( data_set_num > data_sets.length - 1 )
     SetUpLocalCopies();

   getting_ds = data_set_num; // if a thread starts to get a  DataSet, set flag
                              // so the run() method doesn't queue up another
                              // request for a DataSet.

   Object  obj = retriever.getObjectFromServer( command );

   if ( !(obj instanceof DataSet) )                    // lost connection 
   {
     send_message( retriever.status() );
     return;
   }

   DataSet temp_ds = (DataSet)obj;

   if ( temp_ds == data_sets[data_set_num] )
     System.out.println("ERROR!!!! same data set" );

   if ( temp_ds != null )
   {
     last_command[ data_set_num ] = command;            // save the command.
     next_command[ data_set_num ] = command;

     fixTotalCounts( temp_ds );

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


 /* --------------------- removeAllActionListeners ------------------------ */
 /**
  *  Remove all ActionListeners from this LiveDataManager. 
  */
  public void removeAllActionListeners()
  {
    listeners.removeAllElements();
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


/* --------------------------- stop_eventually --------------------------- */
/**
 *  Set the keep_running flag to false so that at the next loop through
 *  the "infinite" loop in the run method, the run method will exit.
 */
 public void stop_eventually()
 {
   keep_running = false;
 }


/* -------------------------------- run --------------------------------- */
/**
 *  The run method for this LiveDataManager.  This will sleep and 
 *  periodically check for new data from the LiveDataRetriever.
 */
 public void run()
 {
   while ( keep_running )
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
           SetUpLocalCopies();
           send_message(  DATA_CHANGED + "Run 2: " + name );
         }

         for ( int i = 0; i < data_sets.length; i++ )    // update the DataSets
         {
           if ( !ignore[i] )                             // we are interested in
           {
             UpdateDataSetNow( i );
           }
         }
       } 
     }
     catch ( Exception e )
     {
       System.out.println("Exception in LiveDataManager.run() is:" + e );
       e.printStackTrace();
     }
   }

   retriever.Exit();          // shut down the tcp connection when we're done
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
      DataSet new_data_sets[]       = new DataSet[ num_ds ];
      GetDataCommand new_last_cmd[] = new GetDataCommand[ num_ds ];
      GetDataCommand new_next_cmd[] = new GetDataCommand[ num_ds ];
      boolean new_ignore[]          = new boolean[ num_ds ];

      for ( int i = 0; i < num_to_save; i++ )           // save what we can
      {                                                 // of the old ones
        new_data_sets[i] = data_sets[i];
        new_last_cmd[i]  = null;                        // reset the commands
        new_next_cmd[i]  = getDefaultCommand(i);        // since the DataSet
        new_ignore[i]    = ignore[i];                   // changed
      }
      data_sets    = new_data_sets;
      last_command = new_last_cmd;
      next_command = new_next_cmd;
      ignore       = new_ignore;
                                                        // initialize new ones
      for ( int i = num_to_save; i < num_ds; i++ )
      {
        data_sets[i]    = null;
        last_command[i] = null;                         // reset the commands
        next_command[i] = getDefaultCommand(i);
        ignore[i]       = true;
      }

      send_message(  DATA_CHANGED + "SetUpLocalCopies 3: " );
    }
  }

/* --------------------------- getValidDataSet -------------------------- */
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

/* --------------------------- fixTotalCounts -------------------------- */
/*
 * fix the total counts attributes and error arrays
 *
 */
  private void fixTotalCounts( DataSet ds )
  {
     if ( ds == null )
       return;

     float y[];
     float total;
     Data  d;
     for ( int i = 0; i < ds.getNum_entries(); i++ )
     {
       d = (TabulatedData)ds.getData_entry(i);
       if ( d != null )
       { 
         d.setSqrtErrors( true ); 
         y = d.getY_values();
         total = 0;
         for ( int j = 0; j < y.length; j++ )
           total+= y[j];
         d.setAttribute( new FloatAttribute( Attribute.TOTAL_COUNT, total ) );
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

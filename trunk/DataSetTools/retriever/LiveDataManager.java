/*
 * @(#)LiveDataManager.java      
 *
 * Programmer: Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.2  2001/02/16 16:39:01  dennis
 *  Do 20 small sleeps, to allow a changed sleep time to
 *  take effect faster.  Added some @see comments.
 *
 *  Revision 1.1  2001/02/15 23:25:23  dennis
 *  Class to control periodic updates to live data sets
 *  by periodically getting new data from a LiveDataServer.
 *
 *
 */
package DataSetTools.retriever;

import java.io.*;
import java.net.*;
import java.lang.*;
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
  LiveDataRetriever retriever   = null;
  DataSet           data_sets[] = new DataSet[0];
  boolean           ignore[]    = new boolean[0];     // flags to mark which
                                                      // data sets won't be
                                                      // automatically updated
  int               time_ms     = 20000;

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
    if ( retriever != null )
    {
      data_sets = new DataSet[ retriever.numDataSets() ];
      ignore    = new boolean[ retriever.numDataSets() ];

      for ( int i = 0; i < data_sets.length; i++ )
      {
        data_sets[i] = retriever.getDataSet(i);
        ignore[i]    = false;
      }
    }
    this.start();
  }

/**
 *  Get the number of distinct DataSets from this LiveDataServer. 
 *  The monitors are placed into one DataSet.  Any sample histograms are 
 *  placed into separate DataSets.  
 *   
 *  @return the number of distinct DataSets in this runfile.
 */  
  public int numDataSets()
  { 
    return data_sets.length;
  }

/**
 * Get the type of the specified data set from the current data source.
 * The type is an integer flag that indicates whether the data set contains
 * monitor data or data from other detectors.
 */

  public int getType( int data_set_num )
  {
    if ( retriever != null )
      return retriever.getType( data_set_num );
    else
      return 0;
  }


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
    if ( data_set_num >= 0 && data_set_num < data_sets.length )
      return data_sets[ data_set_num ];
    else
      return null;
  }


/**
 *  Set the time interval after which the DataSets will be updated with data
 *  from the current data source.
 *
 *  @param  seconds  The number of seconds between automatic updates of the
 *                   DataSets.  This must be at least 10 seconds.
 */
 public void setUpdateInterval( double seconds )
 {
   if ( seconds >= 10 )
     time_ms = (int)( 1000 * seconds );
 }


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


/**
 *  Request an immediate update of the specified DataSet.
 *
 *  @param  data_set_num  The number of the DataSet that is to be updated
 *                        immediately.
 */
 synchronized public void UpdateDataSetNow( int data_set_num )
 {
   DataSet temp_ds = retriever.getDataSet( data_set_num );

   if ( temp_ds == data_sets[data_set_num] )
     System.out.println("ERROR!!!! same data set" );

   if ( temp_ds != null )
     data_sets[data_set_num].copy( temp_ds );    // copy notifies the observers
 }


/**
 *  This will sleep and periodically get new data from the LiveDataRetriever.
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

       for ( int i = 0; i < data_sets.length; i++ )
         if ( !ignore[i] )
           UpdateDataSetNow( i );
     }
     catch ( Exception e )
     {
       System.out.println("Exception in LiveDataManager.run() is:" + e );
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

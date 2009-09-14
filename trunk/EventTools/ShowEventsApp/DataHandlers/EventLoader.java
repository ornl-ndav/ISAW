/* 
 * File: EventLoader.java
 *
 * Copyright (C) 2009, Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author: eu7 $
 *  $Date: 2009-08-29 20:00:20 -0500 (Sat, 29 Aug 2009) $            
 *  $Revision: 19972 $
 */

package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;

import gov.anl.ipns.Operator.IOperator;
import gov.anl.ipns.Operator.Threads.ParallelExecutor;
import gov.anl.ipns.Operator.Threads.ExecFailException;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.EventList.SNS_TofEventList;
import EventTools.EventList.TofEventList;
import EventTools.EventList.EventSegmentLoadOp;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.LoadEventsCmd;
import EventTools.ShowEventsApp.Command.Util;

public class EventLoader implements IReceiveMessage
{
  private MessageCenter    message_center;


  public EventLoader( MessageCenter message_center )
  {
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.LOAD_FILE_DATA );
  }


  public boolean receive( Message message )
  {
/*
    long   start;
    double run_time;
*/
    if ( message.getName().equals(Commands.LOAD_FILE_DATA) )
    {
      LoadEventsCmd cmd = (LoadEventsCmd)message.getValue();
      
      String event_file_name = cmd.getEventFile();

//      start = System.nanoTime();
      try
      {
        LoadEvents( event_file_name, 
                    cmd.getFirstEvent(),
                    cmd.getEventsToLoad(),
                    cmd.getEventsToShow(),
                    cmd.getNumThreads()  ); 
      }
      catch ( Exception ex )
      {
        Util.sendError( "ERROR: Failed to Load File :" +
                        event_file_name + "\n" + ex );
        return false;
      }
/*
      run_time = (System.nanoTime() - start)/1.0e6;
      String load_str = String.format( "Loaded file in %5.1f ms\n", run_time );
      Util.sendInfo( load_str );
*/
      return false;
    }

    return false;
  }
  

  synchronized private void LoadEvents( String event_file_name, 
                                        long   first, 
                                        long   num_to_load,
                                        long   num_to_show,
                                        int    num_threads  )
  {
/*
    System.out.println("FIRST = " + first );
    System.out.println("NUM_TO_LOAD = " + num_to_load );
    System.out.println("NUM_TO_SHOW = " + num_to_show );
    System.out.println("NUM_THREADS = " + num_threads );
*/
    SNS_TofEventList check_file = new SNS_TofEventList( event_file_name );
    long num_available = check_file.numEntries();
    check_file = null;

    if ( num_available <= 0 )
    {
      System.out.println("ERROR: No events in, or can't open " + 
                          event_file_name );
      return;
    }

    long num_remaining = num_available - first;
    if ( num_to_load > num_remaining )     // can't load more than remain 
      num_to_load = num_remaining;

    if ( num_to_show > num_to_load )       // can't show more than are loaded
      num_to_show = num_to_load; 

    long MAX_SEG_SIZE = 5000000;
    long seg_size = num_to_load / num_threads;
    if ( seg_size > MAX_SEG_SIZE )
      seg_size = MAX_SEG_SIZE;

    Util.sendInfo( "Loading " + event_file_name );
    Util.sendInfo( "Please WAIT... " );

    boolean done       = false;
    long    num_loaded = 0;
    Vector<IOperator> ops = new Vector<IOperator>();
    while ( !done )
    {
      ops.clear();
      int n_threads = 0;
      while ( n_threads < num_threads && num_loaded < num_to_load )
      {
        if ( num_to_load - num_loaded < seg_size )
          seg_size = num_to_load - num_loaded;

//      Util.sendInfo( "Loading " + seg_size + " starting with " + first );
        System.out.println("FIRST = " + first + " SEG_SIZE = " + seg_size );
        ops.add( new EventSegmentLoadOp( event_file_name, first, seg_size ) );
        first      += seg_size;
        num_loaded += seg_size;
        n_threads++;     
      }

      long start_time = System.nanoTime();

      Object results;
      try
      {
        ParallelExecutor exec = new ParallelExecutor( ops, n_threads, 600000 );
        results = exec.runOperators();
      }
      catch ( ExecFailException fail_exception )
      {
        results = fail_exception.getPartialResults();
        System.out.println("ExecFailException while loading events: " +
                            fail_exception.getFailureStatus() );
        Util.sendError( "Failed to load events from " + event_file_name );

        Util.sendError( fail_exception.getFailureStatus().toString() );
        return;
      }
      long run_time = System.nanoTime() - start_time;
      System.out.printf("LOADED %d EVENTS IN %5.1f ms\n" ,
                         num_loaded, (run_time/1.0e6) );

      int num_segs = ((Vector)results).size();
      int[][] tofs = new int[num_segs][];
      int[][] ids  = new int[num_segs][];
      for ( int i = 0; i < num_segs; i++ )
      {
        Vector array_vec = (Vector)((Vector)results).elementAt(i);
        tofs[i] = (int[])array_vec.elementAt(0);
        ids[i]  = (int[])array_vec.elementAt(1);
      }

      for ( int i = 0; i < num_segs; i++ )
      {
        TofEventList tof_evl = new TofEventList( tofs[i], ids[i] );
        Message map_to_Q_cmd = new Message( Commands.MAP_EVENTS_TO_Q,
                                            tof_evl,
                                            false,
                                            true );
        message_center.send( map_to_Q_cmd );
      }

      if ( num_loaded >= num_to_load )
        done = true;
    }
  }

}

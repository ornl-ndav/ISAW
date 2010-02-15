/* 
 * File: EventSegmentLoadOp.java
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
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.EventList;

import java.util.*;
import gov.anl.ipns.Operator.*;
import gov.anl.ipns.Operator.Threads.*;

/**
 *  This class represents a parallel IOperator to load a portion of a list
 *  of events.  The operator can be run in parallel using threads, since
 *  the list of events should be in the same address space as the calling
 *  code.
 */
public class EventSegmentLoadOp implements IOperator
{
  private String filename;
  private long   first;
  private long   num;


 /**
  *  Create an operator to load the specified number of events from the
  *  specified file, starting with the specified first event.  If more events
  *  are requested than are present in the file, then only as many events
  *  as are present will be loaded.
  *
  *  @param filename   The name of the SNS *neutron_event.dat file containing
  *                    the list of event ids and times-of-flight.
  *  @param first      The index of the first event to read in the file.
  *  @param num        The number of events to read, starting with the
  *                    specified first event.   
  */
  public EventSegmentLoadOp( String filename, long first, long num )
  {
     this.filename = filename;
     this.first    = first;
     this.num      = num;
  }


 /**
  * Get the requested portion of the event file, in an SNS_TofEventList
  * object.
  *
  * @return a new SNS_TofEventList object containing the requested events.
  */
  public Object getResult()
  {
     SNS_TofEventList eventlist = new SNS_TofEventList( filename );

     int[] events = eventlist.rawEvents( first, num );

     return new TofEventList( events, events.length/2, false );
  }


  public static void main( String args[] )
  {
     int n_threads = 4;

//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_238_neutron_event.dat";
     String file_name = "/usr2/ARCS_SCD_2/EVENTS/ARCS_1250_neutron_event.dat";
//   String file_name = "/usr2/ARCS_SCD_3/EVENTS/ARCS_1853_neutron_event.dat";
//   String file_name = "/usr2/ARCS_SCD/ARCS_419_neutron_event.dat";

     SNS_TofEventList event_list = new SNS_TofEventList( file_name );
     long num_entries = event_list.numEntries();

     long seg_size = num_entries/n_threads;
     long first = 0;
     Vector<IOperator> ops = new Vector<IOperator>();
     for ( int i = 0; i < n_threads-1; i++ )
     {
       ops.add( new EventSegmentLoadOp( file_name, first, seg_size ) );
       first += seg_size;
     }

     seg_size = num_entries - first;     // adjust for the last load segment
     ops.add( new EventSegmentLoadOp( file_name, first, seg_size ) );

     long start_time = System.nanoTime();

     Vector results;
     try
     {
       ParallelExecutor exec = new ParallelExecutor( ops, n_threads, 60000 );
       results = (Vector)exec.runOperators();
     }
     catch ( ExecFailException fail_exception )
     {
       results = (Vector)fail_exception.getPartialResults();
       System.out.println("ExecFailException: " +
                           fail_exception.getFailureStatus() );
     }
     long run_time = System.nanoTime() - start_time;
     System.out.printf("PARALLEL LOAD IN %5.1f \n" , (run_time/1.0e6) );

     seg_size = num_entries/n_threads;
     first = 0;
     for ( int i = 0; i < n_threads; i++ )
     {
       ITofEventList ev_list = (ITofEventList)results.elementAt(i);
       long num_events = ev_list.numEntries();
       int[] raw_ev = ev_list.rawEvents(0,num_events);
       System.out.println("Values from segment " + i + " --------------");
       for ( int j = 0; j < 10; j++ )
         System.out.printf( "%6d    %8d    %8d\n", 
                            j+first, raw_ev[2*j], raw_ev[2*j+1] );
       first += seg_size;
     }
  }

} 

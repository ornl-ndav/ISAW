/* 
 * File: Test_2_SNS_Events.java
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
import java.io.*;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Operator.*;
import gov.anl.ipns.Operator.Threads.*;

import EventTools.Histogram.*;
import EventTools.Viewers.*;

public class Test_2_SNS_Events
{

  public static void main( String args[] ) throws IOException
  {
     int n_threads = 16;

//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_238_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_239_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_240_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_248_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_252_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_253_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_254_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_259_neutron_event.dat";
//   String file_name = "/usr2/SNAP_3/EVENTS/SNAP_427_neutron_event.dat";
//   String file_name = "/usr2/SNAP_4/EVENTS/SNAP_734_neutron_event.dat";
//   String file_name = "/usr2/SNAP_4/EVENTS/SNAP_735_neutron_event.dat";
//   String file_name = "/usr2/SNAP_4/EVENTS/SNAP_736_neutron_event.dat";
//   String file_name = "/usr2/SNAP_4/EVENTS/SNAP_737_neutron_event.dat";
//   String file_name = "/usr2/SNAP_4/EVENTS/SNAP_738_neutron_event.dat";

     String file_name = "/usr2/ARCS_SCD/EVENTS/ARCS_419_neutron_event.dat";
//   String file_name = "/usr2/ARCS_SCD_2/EVENTS/ARCS_1250_neutron_event.dat";
//   String file_name = "/usr2/ARCS_SCD_3/EVENTS/ARCS_1853_neutron_event.dat";

//   String file_name = "/usr2/SEQUOIA/EVENTS/SEQ_328_neutron_event.dat";

     String instrument = null;
     String det_file   = null;

     if ( file_name.indexOf( "ARCS") >= 0 )
     {
       instrument = SNS_Tof_to_Q_map.ARCS;
       det_file   = "/usr2/ARCS_SCD/ARCS_419.grids";
     }
     else if  ( file_name.indexOf( "SNAP") >= 0 )
     {
       instrument = SNS_Tof_to_Q_map.SNAP;
       det_file   = "/usr2/SNS_SCD_TEST_3/SNAP_1_Panel.DetCal";
     }
     else if  ( file_name.indexOf( "SEQ") >= 0 )
     {
       instrument = SNS_Tof_to_Q_map.SEQ;
       det_file   = "/usr2/SEQUOIA/SEQ_328.grids";
     }

     long start_time = System.nanoTime();

     SNS_Tof_to_Q_map mapper = new SNS_Tof_to_Q_map( det_file, instrument );

     long run_time = System.nanoTime() - start_time;
     System.out.printf("MADE To Q map in %5.1f ms\n" , (run_time/1.0e6) );

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

     start_time = System.nanoTime();

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
     }
     run_time = System.nanoTime() - start_time;
     System.out.printf("LOADED %d EVENTS IN %5.1f ms\n" , 
                        num_entries, (run_time/1.0e6) );

     start_time = System.nanoTime();

     first = 0;
     int[][] tofs = new int[n_threads][];
     int[][] ids  = new int[n_threads][];
     for ( int i = 0; i < n_threads; i++ )
     {
       Vector array_vec = (Vector)((Vector)results).elementAt(i);
       tofs[i] = (int[])array_vec.elementAt(0);
       ids[i]  = (int[])array_vec.elementAt(1);
     }

     Vector<IOperator> toQ_ops = new Vector<IOperator>();
     for ( int i = 0; i < n_threads; i++ )
       toQ_ops.add( new MapEventsToQ_Op( tofs[i], ids[i], mapper ) );

     start_time = System.nanoTime();

     try
     {
       ParallelExecutor exec = 
                           new ParallelExecutor( toQ_ops, n_threads, 600000 );
       results = exec.runOperators();
     }
     catch ( ExecFailException fail_exception )
     {
       results = fail_exception.getPartialResults();
       System.out.println("ExecFailException while converting to Q: " +
                           fail_exception.getFailureStatus() );
     }
     run_time = System.nanoTime() - start_time;
     System.out.printf("PARALLEL CONVERTED %d EVENTS TO Q IN %5.1f ms\n" ,
                        num_entries, (run_time/1.0e6) );

     IEventList3D[] event_lists = new IEventList3D[n_threads];

     for ( int i = 0; i < n_threads; i++ )
     {
       float[] list = (float[])(((Vector)results).elementAt(i));
       float[] weights = new float[ list.length/3 ];
       for ( int k = 0; k < weights.length; k++ )
         weights[k] = 1;
       event_lists[i] = new FloatArrayEventList3D_2( weights, list ); 
     }

     // Now make histogram
     start_time = System.nanoTime();
     int NUM_BINS = 512;

     Vector3D xVec = new Vector3D(1,0,0);
     Vector3D yVec = new Vector3D(0,1,0);
     Vector3D zVec = new Vector3D(0,0,1);

     IEventBinner x_bin1D = new UniformEventBinner( -25.6f,  0,    NUM_BINS );
     IEventBinner y_bin1D = new UniformEventBinner( -12.8f, 12.8f, NUM_BINS );
     IEventBinner z_bin1D = new UniformEventBinner( -12.8f, 12.8f, NUM_BINS );

     ProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, xVec);
     ProjectionBinner3D y_binner = new ProjectionBinner3D(y_bin1D, yVec);
     ProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, zVec);

     IProjectionBinner3D[] dual_binners =
          ProjectionBinner3D.getDualBinners( x_binner, y_binner, z_binner );

     Histogram3D histogram = new Histogram3D( dual_binners[0], 
                                              dual_binners[1], 
                                              dual_binners[2]);
     run_time = System.nanoTime() - start_time;
     System.out.println("Time(ms) to allocate histogram = " + run_time/1.e6);

     start_time = System.nanoTime();
     for ( int i = 0; i < n_threads; i++ )
       histogram.addEvents( event_lists[i] );
     run_time = System.nanoTime() - start_time;
     System.out.println("Time(ms) to add events to histogram = " +
                         run_time / 1.e6);

     float min  = (float)histogram.minVal();
     float max  = (float)histogram.maxVal();

     System.out.println("Histogram min   = " + min );
     System.out.println("Histogram max   = " + max );
     System.out.println("Histogram total = " + histogram.total() );

     SlicedEventsViewer my_viewer = new SlicedEventsViewer( histogram,
                                                            file_name );

     int MAX_EVENTS = 1000000;
     int n_lists = event_lists.length;

     int n_events = 0;
     int i = 0; 
     while ( n_events < MAX_EVENTS && i < n_lists )
     {
       my_viewer.add_events( event_lists[i], true, 0, 0, 0 );
       n_events += event_lists[i].numEntries();
       i++;
     }

     my_viewer.setOrthographicView( false );
     my_viewer.updateDisplay();

  }

}

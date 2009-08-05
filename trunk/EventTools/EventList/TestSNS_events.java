/* 
 * File: TestSNS_events.java
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
import java.awt.*;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Operator.*;
import gov.anl.ipns.Operator.Threads.*;

import DataSetTools.operator.Generic.TOF_SCD.FindPeaksViaSort;
import DataSetTools.operator.Generic.TOF_SCD.BasicPeakInfo;
import DataSetTools.operator.Generic.TOF_SCD.IPeakQ;
import DataSetTools.operator.Generic.TOF_SCD.PeakQ;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new_IO;

import Operators.TOF_SCD.IndexPeaks_Calc;

import EventTools.Histogram.*;
import EventTools.Viewers.*;

import SSG_Tools.SSG_Nodes.SimpleShapes.*;


public class TestSNS_events
{

  public static Histogram3D DefaultHistogram( int num_bins )
  { 
    // Just make default histogram aligned with coord axes.
    long start_time = System.nanoTime();

    Vector3D xVec = new Vector3D(1,0,0);
    Vector3D yVec = new Vector3D(0,1,0);
    Vector3D zVec = new Vector3D(0,0,1);
/*
    IEventBinner x_bin1D = new UniformEventBinner( -25.6f,  0,    num_bins );
//  IEventBinner y_bin1D = new UniformEventBinner( -12.8f, 12.8f, num_bins );
    IEventBinner y_bin1D = new UniformEventBinner( -25.6f,  0,    num_bins );
    IEventBinner z_bin1D = new UniformEventBinner( -12.8f, 12.8f, num_bins );
*/
    IEventBinner x_bin1D = new UniformEventBinner( -16.0f,  0,    num_bins );
    IEventBinner y_bin1D = new UniformEventBinner( -16.0f,  0,    num_bins );
    IEventBinner z_bin1D = new UniformEventBinner( - 8.0f, 8.0f, num_bins );


    ProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, xVec);
    ProjectionBinner3D y_binner = new ProjectionBinner3D(y_bin1D, yVec);
    ProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, zVec);

    Histogram3D histogram = new Histogram3D( x_binner, 
                                             y_binner,
                                             z_binner );
    long run_time = System.nanoTime() - start_time;
    System.out.println("Time(ms) to allocate default histogram = " + run_time/1.e6);

    return histogram;
  }


  public static Histogram3D BuildHistogram( String mat_file, int num_bins )
                            throws IOException
  {
    if ( mat_file == null || !( new File(mat_file).exists() ) )
      return DefaultHistogram( num_bins );

     // Now make histogram
     long start_time = System.nanoTime();

     FileReader     f_in        = new FileReader( mat_file );
     BufferedReader buff_reader = new BufferedReader( f_in );
     Scanner        sc          = new Scanner( buff_reader );

     float[] temp = new float[3];     // each row in mat file is basis vector

     for ( int i = 0; i < 3; i++ )
       temp[i] = sc.nextFloat();
     Vector3D a_star = new Vector3D( temp );

     for ( int i = 0; i < 3; i++ )
       temp[i] = sc.nextFloat();
     Vector3D b_star = new Vector3D( temp );

     for ( int i = 0; i < 3; i++ )
       temp[i] = sc.nextFloat();
     Vector3D c_star = new Vector3D( temp );

     System.out.printf("a = %5.3f,  b = %5.3f, c = %5.3f\n",
                       1/a_star.length(),
                       1/b_star.length(),
                       1/c_star.length() );

     float a = (float)(2 * Math.PI * a_star.length() );
     float b = (float)(2 * Math.PI * b_star.length() );
     float c = (float)(2 * Math.PI * c_star.length() );

     a_star.normalize();
     b_star.normalize();
     c_star.normalize();

     System.out.println("BuildHistogram ........ " );
     System.out.println("a_star: " + a_star );
     System.out.println("b_star: " + b_star );
     System.out.println("c_star: " + c_star );

     float h_val = 0;
     float k_val = 0;
     float l_val = 0;
     float extent_factor = 10;
 
     boolean ARCS_419 = false;
     boolean SNAP_240 = true;
     if ( ARCS_419 )
     {
       h_val = -4;
       k_val =  0;
       l_val =  8;
       extent_factor = 18;
     }
     else if ( SNAP_240 )
     {
       h_val =  0;
       k_val =  0;
       l_val =  0;
       extent_factor = 10f;
     }

     float a_center = h_val * a;
     float b_center = k_val * b;
     float c_center = l_val * c;

     float h_extent = extent_factor * a;
     float k_extent = extent_factor * b;
     float l_extent = extent_factor * c;

     IEventBinner a_bin1D = new UniformEventBinner( a_center - h_extent, 
                                                    a_center + h_extent, 
                                                    num_bins );

     IEventBinner b_bin1D = new UniformEventBinner( b_center - k_extent, 
                                                    b_center + k_extent, 
                                                    num_bins );

     IEventBinner c_bin1D = new UniformEventBinner( c_center - l_extent, 
                                                    c_center + l_extent, 
                                                    num_bins );
     
     ProjectionBinner3D x_edge_binner = null;
     ProjectionBinner3D y_edge_binner = null;
     ProjectionBinner3D z_edge_binner = null; 
     int order = 2;
     if ( order == 1 )
     {
       x_edge_binner = new ProjectionBinner3D(a_bin1D, a_star);
       y_edge_binner = new ProjectionBinner3D(b_bin1D, b_star);
       z_edge_binner = new ProjectionBinner3D(c_bin1D, c_star);     }
     else if ( order == 2 )
     {
       z_edge_binner = new ProjectionBinner3D(a_bin1D, a_star);
       x_edge_binner = new ProjectionBinner3D(b_bin1D, b_star);
       y_edge_binner = new ProjectionBinner3D(c_bin1D, c_star);
     }
     else
     {
       y_edge_binner = new ProjectionBinner3D(a_bin1D, a_star);
       z_edge_binner = new ProjectionBinner3D(b_bin1D, b_star);
       x_edge_binner = new ProjectionBinner3D(c_bin1D, c_star);
     }

     Histogram3D histogram = new Histogram3D( x_edge_binner,
                                              y_edge_binner,
                                              z_edge_binner);
     long run_time = System.nanoTime() - start_time;
     System.out.println("Time(ms) to allocate skewed histogram = " + run_time/1.e6);

     return histogram;
  }


  public static void main( String args[] ) throws IOException
  {
     int n_threads = 6;

//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_238_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_239_neutron_event.dat";
     String file_name = "/usr2/SNAP_2/EVENTS/SNAP_240_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_248_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_249_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_250_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_251_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_252_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_253_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_254_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_255_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_256_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_257_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_258_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_259_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_260_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_261_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_262_neutron_event.dat";
//   String file_name = "/usr2/SNAP_2/EVENTS/SNAP_263_neutron_event.dat";
//   String file_name = "/usr2/SNAP_3/EVENTS/SNAP_427_neutron_event.dat";
//   String file_name = "/usr2/SNAP_4/EVENTS/SNAP_730_neutron_event.dat";
//   String file_name = "/usr2/SNAP_4/EVENTS/SNAP_732_neutron_event.dat";
//   String file_name = "/usr2/SNAP_4/EVENTS/SNAP_734_neutron_event.dat";
//   String file_name = "/usr2/SNAP_4/EVENTS/SNAP_735_neutron_event.dat";
//   String file_name = "/usr2/SNAP_4/EVENTS/SNAP_736_neutron_event.dat";
//   String file_name = "/usr2/SNAP_4/EVENTS/SNAP_737_neutron_event.dat";
//   String file_name = "/usr2/SNAP_4/EVENTS/SNAP_738_neutron_event.dat";
//   String file_name = "/usr2/SNAP_5/EVENTS/SNAP_875_neutron_event.dat";

//   String file_name = "/usr2/ARCS_SCD/EVENTS/ARCS_419_neutron_event.dat";
//   String file_name = "/usr2/ARCS_SCD_2/EVENTS/ARCS_1250_neutron_event.dat";
//   String file_name = "/usr2/ARCS_SCD_3/EVENTS/ARCS_1853_neutron_event.dat";

//   String file_name = "/usr2/SEQUOIA/EVENTS/SEQ_328_neutron_event.dat";

     String instrument = null;
     String det_file   = null;
     String mat_file   = null;

     if ( file_name.indexOf( "ARCS") >= 0 )
     {
       instrument = SNS_Tof_to_Q_map.ARCS;
       det_file   = "/usr2/ARCS_SCD/ARCS_419.grids";
       if ( file_name.indexOf( "419") >= 0 ) 
         mat_file = "/usr2/ARCS_SCD/si_419_ls.mat";
     }
     else if  ( file_name.indexOf( "SNAP") >= 0 )
     {
       instrument = SNS_Tof_to_Q_map.SNAP;
       det_file   = "/usr2/SNS_SCD_TEST_3/SNAP_1_Panel.DetCal";
       if ( file_name.indexOf("240") >= 0 )
         mat_file = 
              "/usr2/SNAP_2/QUARTZ_NOT_ROTATED/quartz_240_NOT_ROTATED_ls.mat"; 
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
       event_lists[i] = (IEventList3D)(((Vector)results).elementAt(i));


     int NUM_BINS = 512;
//   Histogram3D histogram = BuildHistogram( mat_file, NUM_BINS );
     Histogram3D histogram = DefaultHistogram( NUM_BINS );

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

     int MAX_EVENTS = 5000000;
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

// /*
     float[][][] histogram_array = new float[NUM_BINS][][];
     for ( int page = 0; page < NUM_BINS; page++ )
       histogram_array[page] = histogram.pageSlice( page );

     int[] val_histogram = new int[10000];
     
     int[] row_list = new int[NUM_BINS];
     int[] col_list = new int[NUM_BINS];
     for ( int k = 0; k < NUM_BINS; k++ )
     {
       row_list[k] = k + 1;
       col_list[k] = k + 1;
     }

     StringBuffer log = new StringBuffer();
     BasicPeakInfo[] peaks = FindPeaksViaSort.getPeaks( histogram_array,
                                                        true,
                                                        120,
                                                        0,
                                                        row_list,
                                                        col_list,
                                                        0,
                                                        NUM_BINS-1,
                                                        val_histogram,
                                                        log );

    String        log_file = "/home/dennis/Test_" + NUM_BINS + ".log"; 
    FileWriter     writer  = new FileWriter( log_file );
    BufferedWriter output  = new BufferedWriter( writer );
    output.write( log.toString() );
    output.close();

    IProjectionBinner3D x_binner = histogram.xEdgeBinner();
    IProjectionBinner3D y_binner = histogram.yEdgeBinner();
    IProjectionBinner3D z_binner = histogram.zEdgeBinner();

    Vector3D   zero    = new Vector3D();
    Vector3D[] verts   = new Vector3D[ peaks.length ];
    int        counter = 0;
    for ( int k = 0; k < verts.length; k++ )
    {
      if ( peaks[k].isValid() )
      {
        counter++;

        float col  = peaks[k].getColMean();
        float row  = peaks[k].getRowMean();
        float page = peaks[k].getChanCenter();

        Vector3D point = x_binner.Vec( page );
        Vector3D temp  = y_binner.Vec( col );
        point.add( temp );
        temp = z_binner.Vec( row );
        point.add( temp );
        verts[k] = point; 
      }
      else
        verts[k] = zero;
    }
    System.out.println("Marked " + counter + " seemingly valid peaks");
    my_viewer.addMarkers( verts, 6, Polymarker.BOX, Color.WHITE );
    my_viewer.updateDisplay();
    System.out.println("DONE>>>>>>>>>>>>>>>>>>");

    Vector<IPeakQ> q_peaks = new Vector<IPeakQ>();
    for ( int k = 0; k < verts.length; k++ )
    {
      if ( verts[k] != zero )
      {
        float qx = verts[k].getX();
        float qy = verts[k].getY();
        float qz = verts[k].getZ();
        float ipk_f = histogram.valueAt( qx, qy, qz );
        qx = (float)(qx / (2 * Math.PI)) ;
        qy = (float)(qy / (2 * Math.PI)) ;
        qz = (float)(qz / (2 * Math.PI)) ;
        q_peaks.add( new PeakQ( qx, qy, qz, (int)ipk_f ) );
      } 
    }

/*
    System.out.println("BEFORE INDEXING PEAKS" );
    for  ( int k = 0; k < q_peaks.size(); k++ )
      System.out.println( q_peaks.elementAt(k) );
*/

/*  // QUARTZ
*/
    IndexPeaks_Calc.IndexPeaksWithOptimizer( q_peaks,
                                             4.915f, 4.915f, 5.4f,
                                             90, 90, 120 );

    // OXALIC ACID  [6.094,3.601,11.915,90.0,103.2,90.0]
/*  
    IndexPeaks_Calc.IndexPeaksWithOptimizer( q_peaks,
                                             6.094f,3.601f,11.915f,
                                             90, 103.2f, 90 );
*/
    System.out.println("AFTER INDEXING PEAKS" );
/*
    for  ( int k = 0; k < q_peaks.size(); k++ )
      System.out.println( q_peaks.elementAt(k) );
*/
    Vector<Peak_new> new_peaks = new Vector<Peak_new>();
    for  ( int k = 0; k < q_peaks.size(); k++ )
    {
      IPeakQ q_peak = q_peaks.elementAt(k);
      float[] qxyz = q_peak.getUnrotQ(); 
      Peak_new peak = mapper.GetPeak( qxyz[0], qxyz[1], qxyz[2] );
      peak.setFacility( "SNS" );
      peak.sethkl( q_peak.h(), q_peak.k(), q_peak.l() );
      peak.seqnum( k );
      peak.ipkobs( q_peak.ipkobs() );
      new_peaks.add( peak );
    }
    
    System.out.println("New Peaks ---------------------------");
    for  ( int k = 0; k < new_peaks.size(); k++ )
      System.out.println( new_peaks.elementAt(k).toString() );
    
    String peaks_file = "/home/dennis/EventPeaksFile.peaks";
    Peak_new_IO.WritePeaks_new( peaks_file, new_peaks, false ); 
/*    
    start_time = System.nanoTime();
    histogram.clear();
    run_time = System.nanoTime() - start_time;
    System.out.println("Time(ms) to clear histogram = " + run_time/1.e6);
*/

// */
  }

}

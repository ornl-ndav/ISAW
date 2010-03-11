/* 
 * File: MapEventsToQ_Op.java
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

import gov.anl.ipns.Operator.*;

/**
 *  This class represents a parallel IOperator that maps a list of events
 *  to reciprocal space.  The operator can be run in parallel using threads.
 */
public class MapEventsToQ_Op implements IOperator
{
  private ITofEventList    tof_events;
  private int              first;
  private int              num_to_map;
  private SNS_Tof_to_Q_map mapper;


 /**
  *  Create an operator to map all of the events defined by the 
  *  tof_events object, to Q.
  *
  *  @param  tof_events  The ITofEventList of TOF events to map to Q
  *  @param  mapper      The SNS_Tof_to_Q_map object that contains the 
  *                      information and method to map the specified 
  *                      events to Q
  */
  public MapEventsToQ_Op( ITofEventList tof_events, 
                          SNS_Tof_to_Q_map mapper )
  {
     this.tof_events = tof_events;
     this.first      = 0;
     this.num_to_map = (int)tof_events.numEntries();
     this.mapper     = mapper;
  }


 /**
  *  Create an operator to map a sub list of the specified events
  *  defined by the tof_events object, to Q.
  *
  *  @param  tof_events  The ITofEventList of TOF events to map to Q
  *  @param  first      The index of the first event to map to Q
  *  @param  num_to_map The number of events to map to Q
  *  @param  mapper     The SNS_Tof_to_Q_map object that contains the 
  *                     information and method to map the specified
  *                      events to Q
  */
  public MapEventsToQ_Op( ITofEventList    tof_events,
                          int              first,
                          int              num_to_map,
                          SNS_Tof_to_Q_map mapper )
  {
     this.tof_events = tof_events;
     this.first      = first;
     this.num_to_map = num_to_map;
     this.mapper     = mapper;
  }


  /**
   *  Run the specified SNS_Tof_to_Q_map mapper and return the resulting
   *  event list with Qx,Qy,Qz values and weights.
   */
  public Object getResult()
  {
     return mapper.MapEventsToQ( tof_events, first, num_to_map); 
  }

  /**
   *  Basic test program for map operator
   */
  public static void main( String args[] ) throws Exception
  {
    String file_name = "/usr2/DEMO/SNAP_240_neutron_event.dat";

    SNS_TofEventList loader = new SNS_TofEventList( file_name );

    long tot_num = loader.numEntries();

    System.out.println("Number of events in file = " + tot_num );

    int[] list = loader.rawEvents( 2000000, 2000000 );

    System.out.println("Number of events loaded =  " + list.length/2 );

    ITofEventList ev_list = new TofEventList( list, 2000000, false );

    String det_cal_file = 
      "/home/dennis/SNS_ISAW/ISAW_ALL/InstrumentInfo/SNS/SNAP/SNAP.DetCal";                          
    String inst_name = "SNAP";
    SNS_Tof_to_Q_map mapper = new SNS_Tof_to_Q_map(det_cal_file,null,inst_name);

    int num_mapped = 1000000;
    int first      = 1000000;
    IOperator op = new MapEventsToQ_Op( ev_list, first, num_mapped, mapper ); 
    Object result = op.getResult();    

    FloatArrayEventList3D q_list = (FloatArrayEventList3D)result; 
    float[] qxyz = q_list.eventVals();

    System.out.println("Mapped " + num_mapped + " starting at " + first );
    System.out.println("First 4 = ");
    for ( int i = 0; i < 4; i++ )
      System.out.printf(" %5.2f  %5.2f  %5.2f\n", 
                          qxyz[3*i], qxyz[3*i+1], qxyz[3*i+2] );
    System.out.println("Last 4 = ");
    for ( int i = num_mapped-4; i < num_mapped; i++ )
      System.out.printf(" %5.2f  %5.2f  %5.2f\n", 
                          qxyz[3*i], qxyz[3*i+1], qxyz[3*i+2] );
  }

} 


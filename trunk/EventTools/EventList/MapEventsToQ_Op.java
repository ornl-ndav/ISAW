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
  private int[]            tofs;
  private int[]            ids;
  private int              first;
  private int              num_to_map;
  private SNS_Tof_to_Q_map mapper;


 /**
  *  Create an operator to map all of the events defined by the tofs and ids
  *  arrays, to Q.
  *
  *  @param  tofs    The list of times-of-flight for the events
  *  @param  ids     The list of pixel ids for the events
  *  @param  mapper  The SNS_Tof_to_Q_map object that contains the 
  *                  information and method to map the specified events to Q
  */
  public MapEventsToQ_Op( int[] tofs, int[] ids, SNS_Tof_to_Q_map mapper )
  {
     this.tofs       = tofs;
     this.ids        = ids;
     this.first      = 0;
     this.num_to_map = tofs.length;
     this.mapper     = mapper;
  }


 /**
  *  Create an operator to map the specified events in the tofs and ids
  *  arrays, to Q.
  *
  *  @param  tofs       The list of times-of-flight for the events
  *  @param  ids        The list of pixel ids for the events
  *  @param  first      The index of the first event to map to Q
  *  @param  num_to_map The number of events to map to Q
  *  @param  mapper     The SNS_Tof_to_Q_map object that contains the 
  *                     information and method to map the specified
  *                      events to Q
  */
  public MapEventsToQ_Op( int[] tofs, 
                          int[] ids, 
                          int   first,
                          int   num_to_map,
                          SNS_Tof_to_Q_map mapper )
  {
     this.tofs       = tofs;
     this.ids        = ids;
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
     return mapper.MapEventsToQ( tofs, ids, first, num_to_map ); 
  }

} 


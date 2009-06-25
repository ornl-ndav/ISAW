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

import java.util.*;
import gov.anl.ipns.Operator.*;
import gov.anl.ipns.Operator.Threads.*;

/**
 *  This class represents a parallel IOperator that maps a list of events
 *  to reciprocal space.  The operator can be run in parallel using threads.
 */
public class MapEventsToQ_Op implements IOperator
{
  private int[]            tofs;
  private int[]            ids;
  private SNS_Tof_to_Q_map mapper;

 /**
  *  Create an operator to map the specified events to Q.
  *
  *  @param  tofs    The list of times-of-flight for the events
  *  @param  ids     The list of pixel ids for the events
  *  @param  mapper  The SNS_Tof_to_Q_map object that contains the 
  *                  information and method to map the events to Q
  */
  public MapEventsToQ_Op( int[] tofs, int[] ids, SNS_Tof_to_Q_map mapper )
  {
     this.tofs   = tofs;
     this.ids    = ids;
     this.mapper = mapper;
  }


  /**
   *  Run the specified SNS_Tof_to_Q_map mapper and return the resulting
   *  float array with Qx,Qy,Qz values interleaved.
   */
  public Object getResult()
  {
     return mapper.BuildPackedQxyz(tofs, ids ); 
  }

} 


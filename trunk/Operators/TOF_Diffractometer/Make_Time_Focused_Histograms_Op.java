/* 
 * File: Make_Time_Focused_Histograms_Op.java
 *
 * Copyright (C) 2010, Dennis Mikkelson
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
 *  $Date$ `           
 *  $Rev$
 */

package Operators.TOF_Diffractometer;

import gov.anl.ipns.Operator.IOperator;
import EventTools.EventList.ITofEventList;
import EventTools.EventList.SNS_Tof_to_Q_map;
import EventTools.Histogram.IEventBinner;


/**
 *  This class is an Operator wrapper around the Make_Time_Focused_Histograms
 *  method for use by multiple threads for loading events.
 */
public class Make_Time_Focused_Histograms_Op implements IOperator
{
  private SNS_Tof_to_Q_map  mapper;
  private ITofEventList     ev_list;
  private long              first_event;
  private long              num_events;
  private IEventBinner      binner;
  private float             angle_deg;
  private float             final_L_m;
  private int[][]           ghost_ids;
  private double[][]        ghost_weights;


  /**
   *  Construct a Make_Time_Focused_Histograms_Op operator to load the
   *  specified set of events.
   */
  public Make_Time_Focused_Histograms_Op( SNS_Tof_to_Q_map  mapper,
                                          ITofEventList     ev_list,
                                          long              first_event,
                                          long              num_events,
                                          IEventBinner      binner,
                                          float             angle_deg,
                                          float             final_L_m,
                                          int[][]           ghost_ids,
                                          double[][]        ghost_weights )
  {
    this.mapper        = mapper;
    this.ev_list       = ev_list;
    this.first_event   = first_event;
    this.num_events    = num_events;
    this.binner        = binner;
    this.angle_deg     = angle_deg;
    this.final_L_m     = final_L_m;
    this.ghost_ids     = ghost_ids;
    this.ghost_weights = ghost_weights;
  }


  /**
   *  Return a float[][] of d-histogram data corresponding to the specified
   *  set of events from the specified file.
   */
  public Object getResult()
  {
    float[][] histograms = null;

    if ( ghost_ids != null )
      histograms = mapper.Make_Time_Focused_Histograms( ev_list,
                                             first_event,
                                             num_events,
                                             binner,
                                             angle_deg,
                                             final_L_m,
                                             ghost_ids,
                                             ghost_weights);
    else
    {
      int[][] int_histograms = mapper.Make_Time_Focused_Histograms( ev_list,
                                                         first_event,
                                                         num_events,
                                                         binner,
                                                         angle_deg,
                                                         final_L_m );
      histograms = Util.ConvertTo2DfloatArray( int_histograms );
    }

    return histograms;
  }

}

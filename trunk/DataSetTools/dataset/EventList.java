/*
 * File: EventList.java
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2002/06/28 20:52:20  dennis
 *  Improved check for valid index when building histogram.
 *
 *  Revision 1.1  2002/06/19 22:54:06  dennis
 *  Prototype classes for event data.
 *
 */

package  DataSetTools.dataset;

import DataSetTools.dataset.*;
import DataSetTools.util.*;

public class EventList implements IEventList
{
  double start_time;     // offset for calculating time-of-flight 
  double tick_width;     // minimum time between events 
  int    time[];         // list of times as number of clock pulses
  int    count[];        // list of counts at the corresponding times


  public EventList( double  start_time, 
                    double  tick_width, 
                    int     time[], 
                    int     count[] )
  {
    this.start_time = start_time;
    this.tick_width = tick_width;
    this.time       = time;
    this.count      = count;
  } 


  public float[] histogram( XScale x_scale, int smooth_flag )
  {
    float t;
    int   index;
    float x[] = x_scale.getXs();
    float y[] = new float[ x.length-1 ];
                                                   // this can be done more
                                                   // efficiently, for now just 
    for ( int i = 0; i < time.length; i++ )        // place each event in the
    {                                              // right histogram bin
      t = (float)(start_time + time[i] * tick_width);
      index = arrayUtil.get_index_of( t, x );
      if ( index >= 0 && index < y.length )
        y[index] += count[i];
    } 

    return y;
  }
}

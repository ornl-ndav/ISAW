/* 
 * File: BinEvents.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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
package EventTools.Histogram.Operators;

import java.util.*;

import EventTools.EventList.IEventList3D;
import EventTools.Histogram.IEventBinner;
import gov.anl.ipns.Operator.*;

/**
 * This IOperator adds a list of events to appropriate bins in the specified 
 * range of "pages" in the specified 3D array.  Several objects of this 
 * class, covering different ranges of "pages" in the 3D array are run
 * in separate threads by a method in the Histogram3D class, to add
 * the events to a Histogram3D object.  
 */
public class BinEvents implements IOperator
{
  private float[][][] histogram;
  private int         first_page,
                      last_page;
  private float       min,
                      max;

  private IEventList3D events;
  private IEventBinner x_binner;
  private IEventBinner y_binner;
  private IEventBinner z_binner;


  /**
   * Construct an operator to bin the specified list of events, into a 
   * portion of the specified histogram array, using the specified 
   * IEventBinner objects to determine the page,row and column where 
   * each event belongs.  NOTE: The histogram3D object keeps track of the
   * current max and min values in the histogram bins.  The current values
   * of the max and min are passed in to this object as parameters.  As the
   * events are binned, the min and max values are updated and are returned
   * by the getResult() method. 
   *  
   * @param histogram  The 3D array into which the events are binned.
   * @param min        The current min count in any bin
   * @param max        The current max count in any bin
   * @param first_page The first page of the portion of the 3D histogram 
   *                   that this operator will use.
   * @param last_page  The last page of the portion of the 3D histogram 
   *                   that this operator will use.
   * @param x_binner   The IEventBinner that determines which column of the
   *                   3D array corresponds to an event's X-coordinate.
   * @param y_binner   The IEventBinner that determines which row of the
   *                   3D array corresponds to an event's Y-coordinate.
   * @param z_binner   The IEventBinner that determines which page of the
   *                   3D array corresponds to an event's Z-coordinate.
   * @param events     The list of events to be categorized and added to the
   *                   histogram array.  NOTE: currently the "code" associated
   *                   with the event is assumed to be a count of multiple
   *                   events occurring at the specified x,y,z.
   */
  public BinEvents( float[][][]       histogram, 
                    float             min,
                    float             max,
                    int               first_page, 
                    int               last_page,
                    IEventBinner      x_binner,
                    IEventBinner      y_binner,
                    IEventBinner      z_binner,
                    IEventList3D events    )
  {
    this.histogram  = histogram;
    this.first_page = first_page;
    this.last_page  = last_page;

    this.x_binner = x_binner;
    this.y_binner = y_binner;
    this.z_binner = z_binner;

    this.events = events;
  }


  /**
   * Step through the list of events passed in to the constructor and add
   * them to the corresponding bins of the histogram.  The sum of all events
   * added and the updated min and max values are returned as three Doubles in
   * that order in the returned Vector.
   * 
   * @return a Vector containing three Doubles, the sum of the events added 
   * and the updated min and max values in that order.
   */
  public Object getResult()
  {
    double[] ev_xyz = new double[3];
    float   val;
    float   count;
    double  sum = 0;
    int     x_index,
            y_index,
            z_index;

    int     num_x_bins = x_binner.getNumBins();
    int     num_y_bins = y_binner.getNumBins();
    
    int num_events = events.getNumEntries();

    for ( int i = 0; i <  num_events; i++ )
    {
      /*  NOTE: This second form of the calculation executes faster on 
       *        a 2-processor XEON (32 bit Linux).
       */
       events.getEventVals( i, ev_xyz );
       z_index = z_binner.getIndex( ev_xyz[2] );

       if ( z_index >= first_page && z_index <= last_page )
       {
         x_index = x_binner.getIndex( ev_xyz[0] );
         y_index = y_binner.getIndex( ev_xyz[1] );

         if ( x_index >= 0 && x_index < num_x_bins &&
              y_index >= 0 && y_index < num_y_bins  )
         {
           count = events.getEventCode( i );
           val = histogram[z_index][y_index][x_index];
           val += count;
           histogram[z_index][y_index][x_index] = val;

           if ( val > max )
             max = val;
           if ( val < min )
             min = val;

           sum += count;
         }
       }
       
      /*  NOTE: This second form of the calculation executes faster on 
       *        a 4-core Opteron (64 bit Linux).
       *        
       val     = events.getEventZ( i );
       z_index = z_binner.getIndex( val );
       
       if ( z_index >= first_page && z_index <= last_page )
       {
         val     = events.getEventY( i );
         y_index = y_binner.getIndex( val );

         val     = events.getEventX( i );
         x_index = x_binner.getIndex( val );
         
         if ( x_index >= 0 && x_index < num_x_bins &&
              y_index >= 0 && y_index < num_y_bins  )
         {
           count = events.getEventCode( i );
           
           val = histogram[z_index][y_index][x_index];
           val += count;
           histogram[z_index][y_index][x_index] = val;
           
           if ( val > max )
             max = val;
           if ( val < min )
             min = val;

           sum += count;
         }
       }
      */
    }

    Vector results = new Vector(3);
    results.add( new Double( sum ) );
    results.add( new Double( min ) );
    results.add( new Double( max ) );
    return results;
  }

}

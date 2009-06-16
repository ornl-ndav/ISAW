/* 
 * File: ITofEventList.java
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

/**
 * This interface specifies methods that must be implemented by objects that
 * represent a list of events at specific times-of-flight in particular 
 * detectors. 
 */
public interface ITofEventList 
{
  /**
   * Get the total number of events available in this event list.  
   * 
   * @return  an integer given the total number of entries in this event list.
   */
  long numEntries();

  
  /** 
   * Get an array of times-of-flight for a  subset of the list of events, 
   * starting with the specified first event number.
   *
   * @param   first_event  The index of the first event whose time-of-flight
   *                       should be returned.
   * 
   * @param   num_events   The number of events whose times-of-flight
   *                       should be returned.
   *
   * @return an array of integers giving the times-of-flight for the
   *         specified sublist of events. 
   */
  int[] eventTof( long first_event, long num_events ); 

  
  /** 
   * Get an array of pixel IDs for a  subset of the list of events, 
   * starting with the specified first event number.
   *
   * @param   first_event  The index of the first event whose pixel ID 
   *                       should be returned.
   * 
   * @param   num_events   The number of events whose pixel IDs 
   *                       should be returned.
   *
   * @return an array of integers giving the pixel IDs for the
   *         specified sublist of events. 
   */
  int[] eventPixelID( long first_event, long num_events );

  
}

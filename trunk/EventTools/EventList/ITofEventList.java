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
   *  The maximun number of entries in one event list.  This limit is roughly 
   *  250 million, and is due to the limits on array indexing for the 
   *  byte array representation used by some implementing classes.
   */
  static final long MAX_LIST_SIZE = ((long)Integer.MAX_VALUE) / 8;


  /**
   * Get the total number of events available in this event list.  
   * 
   * @return  an integer given the total number of entries in this event list.
   */
  long numEntries();


  /** 
   * Get a reference to an array of events, represented as interleaved 
   * integers, starting with the TOF and ID of the first event, followed 
   * by the TOF and ID of the next event, etc.  
   * NOTE: It is most efficient to call this using first_event set to 0 and
   *       the number of events equal to the total number of events stored
   *       in this event list.  In that case this method just returns a 
   *       reference to the internal array of integers, and the calling 
   *       code should NOT alter the values in the array that is returned.
   * NOTE: The array returned may be longer than needed to store the events.
   *       The array should only be used to access events numbered from 0
   *       to numEntries()-1.
   *
   * @param   first_event  The event number of the first event to be returned
   *                       in the array.
   * 
   * @param   num_events   The number of events that should be returned.
   *                       If too many events are requested, fewer events
   *                       will actually be returned.
   *
   * @return A reference to an internal interleaved array of integers 
   *         holding the times-of-flight and pixel IDs for the requested 
   *         sublist of events.  The TOF for for the i_th event past the 
   *         specified first_event is stored in position 2i and the pixel
   *         ID for that event is stored in position 2i+1.
   */
  int[] rawEvents( long first_event, long num_events );

  
  /** 
   * Get a new array containing times-of-flight for the specified subset of 
   * the list of events, starting with the specified first event number.
   * This method is provided for conveniece only, calling rawEvents() will
   * be much more efficient.
   *
   * @param   first_event  The index of the first event whose time-of-flight
   *                       should be returned.
   * 
   * @param   num_events   The number of events whose times-of-flight
   *                       should be returned.
   *                       If too many events are requested, fewer events
   *                       will actually be returned.
   *
   * @return a new array of integers giving the times-of-flight for the
   *         specified sublist of events. 
   */
  int[] eventTof( long first_event, long num_events ); 

  
  /** 
   * Get a new array containing Pixel IDs for the specified subset of 
   * the list of events, starting with the specified first event number.
   * This method is provided for conveniece only, calling rawEvents() will
   * be much more efficient.
   *
   * @param   first_event  The index of the first event whose pixel ID 
   *                       should be returned.
   * 
   * @param   num_events   The number of events whose pixel IDs 
   *                       should be returned.
   *                       If too many events are requested, fewer events
   *                       will actually be returned.
   *
   * @return a new array of integers giving the pixel IDs for the
   *         specified sublist of events. 
   */
  int[] eventPixelID( long first_event, long num_events );

  
}

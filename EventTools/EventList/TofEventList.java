/* 
 * File: TofEventList.java
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
 * This class represents a list of SNS time-of-flight events as two
 * parallel arrays of ints, one for the times-of-flight and one for
 * the pixel IDs.
 */
public class TofEventList implements ITofEventList
{
  int[] events = null;   // list of events stored interleaved in this array.
                         // This array MAY be just a reference to the array
                         // of events that was passed in when this was 
                         // constructed.

  /**
   *  Construct a TofEventList object from separate arrays of tofs and ids.
   *  NOTE: This constructor is MUCH less efficient that the constructor that
   *  takes one array containg the TOFs and IDs interleaved.
   *  
   *  @param tofs  Array of times-of-flight for this event list.
   *  @param ids   Array of pixel ids for this event list.
   *
   */
  public TofEventList( int[] tofs, int[] ids )
  {
    if ( tofs == null || ids == null )
      throw new IllegalArgumentException(
                           "null array passed to TofEventList constructor 1");

    int num_entries = Math.min( tofs.length, ids.length );

    events = new int[ 2 * num_entries ];
    int index = 0;
    for ( int i = 0; i < num_entries; i++ )
    {
      events[index++] = tofs[i]; 
      events[index++] = ids[i]; 
    }
  }
  

  /**
   *  Construct a TofEventList object from an array of interleaved tofs and
   *  ids.
   *  
   *  @param raw_events  Array with interleaved times-of-flight and ids
   *                     for this event list.
   *  @param make_copy   If true, the raw_event array will be copied into
   *                     a new internal array; If false, a reference to the
   *                     raw_events array will be kept, instead of making a
   *                     copy. 
   */
  public TofEventList( int[] raw_events, boolean make_copy )
  {
    if ( raw_events == null )
      throw new IllegalArgumentException( 
                           "null array passed to TofEventList constructor 2");

    if ( make_copy )
    {
      events = new int[ raw_events.length ]; 
      System.arraycopy( raw_events, 0, events, 0, raw_events.length );
    }
    else
      events = raw_events;
  }


  @Override
  public long numEntries()
  {
    if ( events != null )
      return events.length / 2;

    return 0;
  }


  @Override
  public int[] rawEvents( long first_event, long num_events )
  {
    long num_available = numEntries();
    if ( num_available <= 0 || num_available > MAX_LIST_SIZE )
      return null;

    if ( num_events  <= 0 ||
         first_event <  0 ||
         first_event >= num_available )
      return null;

    long num_possible = num_available - first_event;
    if ( num_events > num_possible )
      num_events = num_possible;

    if ( first_event == 0 && num_events == num_available )
      return events;

    int[] raw_events = new int[ (int)num_events ];
    System.arraycopy(events, (int)first_event, raw_events, 0, (int)num_events);
    return raw_events;
  }


  
  @Override
  public int[] eventTof( long first_event, long num_events )
  {
    long num_available = numEntries();
    if ( num_available <= 0 || num_available > MAX_LIST_SIZE )
      return null;

    if ( num_events  <= 0 ||
         first_event <  0 ||
         first_event >= num_available )
      return null;

    long num_possible = num_available - first_event;
    if ( num_events > num_possible )
      num_events = num_possible;

    int[] tof_array = new int[ (int)num_events ]; 
    
    for ( int i = 0; i < num_events; i++ ) 
      tof_array[i] = events[ 2*i ];
    return tof_array;
  }


  @Override
  public int[] eventPixelID( long first_event, long num_events )
  {
    long num_available = numEntries();
    if ( num_available <= 0 || num_available > Integer.MAX_VALUE )
      return null;

    if ( num_events  <= 0 ||
         first_event <  0 ||
         first_event >= num_available )
      return null;

    long num_possible = num_available - first_event;
    if ( num_events > num_possible )
      num_events = num_possible;

    int[] ids_array = new int[ (int)num_events ];

    for ( int i = 0; i < num_events; i++ )
      ids_array[i] = events[ 2*i + 1 ];

    return ids_array;
  }


}

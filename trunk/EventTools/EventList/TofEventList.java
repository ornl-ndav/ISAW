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

  int num_entries = 0;   // NOTE: This may NOT be the same as the length of
                         //       the interleaved events array.  The events 
                         //       array might be a reference to an external
                         //       buffer array that is only partially filled,
                         //       starting in position 0.

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

    int num_given = Math.min( tofs.length, ids.length );

    if ( num_given > MAX_LIST_SIZE )
      throw new IllegalArgumentException(
            "Number of events passed to TofEventList constructor 2, " +
             num_given + " must not exceed " + MAX_LIST_SIZE );

    num_entries = num_given;

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
   *
   *  @param num_events  The number of events stored in the initial portion
   *                     of this array.
   *
   *  @param make_copy   If true, the raw_event array will be copied into
   *                     a new internal array; If false, a reference to the
   *                     raw_events array will be kept, instead of making a
   *                     copy. 
   */
  public TofEventList( int[] raw_events, int num_events, boolean make_copy )
  {
    if ( raw_events == null )
      throw new IllegalArgumentException( 
                           "null array passed to TofEventList constructor 2");

    if ( num_events < 0 )
      throw new IllegalArgumentException( 
            "Negative number of events passed to TofEventList constructor 2");

    if ( num_events > MAX_LIST_SIZE )
      throw new IllegalArgumentException( 
            "Number of events passed to TofEventList constructor 2, " +
             num_events + " must not exceed " + MAX_LIST_SIZE );

    if ( make_copy )
    {
      events = new int[ 2*num_events ]; 
      System.arraycopy( raw_events, 0, events, 0, 2*num_events );
    }
    else
      events = raw_events;

    num_entries = num_events;   // record the number of events to use from
                                     // the interleaved event array.
  }


  @Override
  public long numEntries()
  {
    return this.num_entries;
  }


  @Override
  public int[] rawEvents( long first_event, long num_events )
  {
    int num_to_get = adjust_num_events( first_event, num_events );

    if ( first_event == 0 && num_to_get == num_entries )
      return events;

    int[] raw_events = new int[ num_to_get ];
    System.arraycopy( events, 2*(int)first_event, raw_events, 0, 2*num_to_get );
    return raw_events;
  }

  
  @Override
  public int[] eventTof( long first_event, long num_events )
  {
    int num_to_get = adjust_num_events( first_event, num_events );

    int[] tof_array = new int[ num_to_get ]; 
    
    for ( int i = 0; i < num_to_get; i++ ) 
      tof_array[i] = events[ 2*i ];
    return tof_array;
  }


  @Override
  public int[] eventPixelID( long first_event, long num_events )
  {
    int num_to_get = adjust_num_events( first_event, num_events );

    int[] ids_array = new int[ num_to_get ];

    for ( int i = 0; i < num_to_get; i++ )
      ids_array[i] = events[ 2*i + 1 ];

    return ids_array;
  }

  /**
   *  Adjust the number of events to return based on the number of
   *  events available and the maximum number that can be returned.
   *  If 0 is returned by this method, the number of events requested
   *  was not valid, so no events can be returned.
   *
   *  @param num_events  The number of events requested.
   *
   *  @return An integer giving the number of events that can actually
   *          be returned.  This is less than or equal to the number
   *          requested.
   */
  private int adjust_num_events( long first_event, long num_events )
  {
    if ( num_entries <= 0 )
      return 0;

    if ( num_events  <= 0 ||
         first_event <  0 ||
         first_event >= num_entries )
      return 0;

    long num_possible = num_entries - first_event;
    if ( num_events > num_possible )
      num_events = num_possible;

    return (int)num_events;
  }
}

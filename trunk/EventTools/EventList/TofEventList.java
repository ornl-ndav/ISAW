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
  int[] tofs = null;
  int[] ids  = null;


  /**
   *  Record a reference to the specifed arrays of tofs and ids.
   *  
   *  @param tofs  Array of times-of-flight for this event list.
   *  @param ids   Array of pixel ids for this event list.
   *
   */
  public TofEventList( int[] tofs, int[] ids )
  {
    this.tofs = tofs;
    this.ids  = ids;
  }


  @Override
  public long numEntries()
  {
    if ( tofs != null )
      return tofs.length;

    return 0;
  }

  
  @Override
  public int[] eventTof( long first_event, long num_events )
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

    if ( first_event == 0 && num_events == num_available )
      return tofs;

    int[] tof_array = new int[ (int)num_events ]; 
    System.arraycopy( tofs, (int)first_event, tof_array, 0, (int)num_events );

    System.out.println("COPIED tofs ARRAY *******************************");
    
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

    if ( first_event == 0 && num_events == num_available )
      return ids;

    int[] ids_array = new int[ (int)num_events ];
    System.arraycopy( ids, (int)first_event, ids_array, 0, (int)num_events );

    System.out.println("COPIED ids ARRAY *******************************");

    return ids_array;
  }


}

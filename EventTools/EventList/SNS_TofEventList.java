/* 
 * File: SNS_TofEventList.java
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

import java.io.*;

/**
 * This class represents a list of events at specific times-of-flight in 
 * particular detectors. 
 */
public class SNS_TofEventList implements ITofEventList
{
  /**
   *  Size of byte buffer used when getting a sequence of bytes from
   *  the disk.  NOTE: The size must be a multiple of 8.
   */ 
  public static final int BUFFER_SIZE = 65536;

  private String filename;      // name of the SNS raw event data file
  private long   file_size;  
  private long   first_event;   // index of the first event that was loaded
  private long   num_events;    // number of events that were loaded
  private int[]  events;        // list of interleaved TOF and pixel ID values

  /**
   * Construct an SNS_TofEventList wrapper around the specified file,
   * if possible.
   *
   * @param filename  The fully qualified name of the underlying SNS event
   *                  file.
   * @throws IllegalArgumentException if the file doesn't exist, or 
   *         the file length is not evenly divisible by 8. 
   */
  public SNS_TofEventList( String filename )
  {
    File ev_file = new File( filename );
    if ( !ev_file.exists() )
      throw new IllegalArgumentException( filename + " does not exist.");

    this.filename = filename;
    first_event   = 0;
    num_events    = 0;
    events        = new int[0];

    file_size = ev_file.length();
    if ( file_size % 8 != 0 )
      throw new IllegalArgumentException( filename + " is not an event file.");
  }


  @Override
  public long numEntries()
  {
    return file_size/8;
  }

  @Override
  public int[] rawEvents( long first_event, long num_events )
  {
    if ( this.first_event == first_event &&         // use loaded tof array
         this.num_events  == num_events   )
      return events;
                                                    // load requested events
    CheckEventRange( first_event, num_events );
    LoadEvents( this.first_event, this.num_events );
    return events;
  }

  
  @Override
  public int[] eventTof( long first_event, long num_events )
  {
    if ( this.first_event != first_event ||         // must load a different
         this.num_events  != num_events   )         // segment
      rawEvents( first_event, num_events );

    num_events = this.num_events;                   // loading may have lowered
                                                    // num_events;
    int[] tofs = new int[ (int)num_events ];
    for ( int i = 0; i < num_events; i++ )
      tofs[i] = events[ 2*i ];

    return tofs; 
  }


  @Override
  public int[] eventPixelID( long first_event, long num_events )
  {
    if ( this.first_event != first_event ||         // must load a different
         this.num_events  != num_events   )         // segment
      rawEvents( first_event, num_events );

    num_events = this.num_events;                   // loading may have lowered
                                                    // num_events;
    int[] ids = new int[ (int)num_events ];
    for ( int i = 0; i < num_events; i++ )
      ids[i] = events[ 2*i + 1 ];

    return ids;
  }

  /**
   *  Reset this object's state to no events read and zero length arrays
   *  so that this class no longer keeps references to potentially large
   *  arrays of times-of-flight, or pixel_IDs.
   */
  public void free_storage()
  {
    this.first_event = 0;
    this.num_events  = 0;

    events = new int[0];
  }


  /**
   *  Actually load the specified range of events into the packed event
   *  array, events[].  NOTE: CheckEventRange() must be called before 
   *  calling this method, to make sure that the first_event and number of
   *  events is valid.
   *
   *  @param first_event  The index of the first event to load from the file.
   *  @param num_events   The number of successive events to load from the
   *                      file.
   */
  private void LoadEvents( long first_event, long num_events )
  {
    try
    {
//    System.out.println("Loading file " + filename );
//    System.out.println("first event = " + first_event + 
//                       " num_events = " + num_events );
      RandomAccessFile r_file = new RandomAccessFile( filename, "r" );

      events = new int[2 * (int)num_events];
//    System.out.println( "Array sizes = " + tofs.length );

      r_file.seek( 8*first_event );    // move to first requested event in
                                       // the file an read the requested 
                                       // number of events in segments
      long num_loaded = 0;
      long num_left;
      long seg_size;
      long bytes_read;
      byte[] buffer = new byte[ BUFFER_SIZE ];
      while ( num_loaded < num_events )
      {
        num_left   = num_events - num_loaded;
        bytes_read = r_file.read( buffer );
        seg_size   = Math.min( BUFFER_SIZE, 8*num_left );
        seg_size   = Math.min( seg_size, bytes_read );
/*      System.out.println("\nfirst_event = " + first_event +
                           "\nnum_events  = " + num_events +
                           "\nnum_loaded  = " + num_loaded +
                           "\nnum_left    = " + num_left +
                           "\nseg_size    = " + seg_size +
                           "\nbytes_read  = " + bytes_read );
*/
        if ( bytes_read > 0 )
        {
          UnpackBuffer( buffer, seg_size, num_loaded );
          num_loaded += bytes_read/8;
        }
        else
        {
          System.out.println("ERROR: Unexpected end of event file: " +
                              filename + 
                             " after reading " + num_loaded +
                             " out of " + num_events +
                             " events." );
          num_loaded = num_events;
        }
      }
//    System.out.println("num_loaded = " + num_loaded );
      r_file.close();
    }
    catch ( IOException ex )
    {
      throw new IllegalArgumentException("Failed to load events from file " +
                                          filename +
                                         " requested " + num_events +
                                         " starting at " + first_event + 
                                         " from file with " + numEntries() +
                                         " events." );
    }
  }


  /**
   *  Get the integer values stored in the input file buffer and put the
   *  values into the proper positions in the event[] array.  
   *
   *  @param buffer      The array of bytes as read in one segment from the
   *                     event data file.
   *  @param bytes_read  The number of bytes that were read in from the file
   *                     and are to be extracted and placed in the events[]
   *                     array.
   *  @param num_loaded  The number of events that have already been loaded. 
   *                     This provides the position where the first tof and
   *                     pixel id from the buffer should be stored.
   */
  private void UnpackBuffer( byte[] buffer, long bytes_read, long num_loaded )
  {
    int index = (int)(2*num_loaded);
    for ( int i = 0; i < bytes_read; i += 8 )
    {
      events[ index++ ] = getValue_32( buffer, i   );
      events[ index++ ] = getValue_32( buffer, i+4 );       
    } 
  }


  /**
   * Decode the integer value stored in a sequence of 
   * four bytes in the buffer.  The four bytes determining
   * the Integer value are stored in the file and buffer in the 
   * sequence: b0, b1, b2, b3, with the lowest order byte, b0, first
   * and the the highest order byte, b3, last.
   * 
   * @param i  The index of the first byte in the buffer
   *                    
   * @return The integer value represented by four successive bytes from
   *         the file. 
   */
  public static int getValue_32( byte[] buffer, int i )
  {
    int val = 0;

    i += 3;
                                   // NOTE: When the signed byte is
    val |= buffer[ i-- ] & 0xFF;   // converted to int, it is sign
    val <<= 8;                     // extended, so the $0xFF is
                                   // needed.
    val |= buffer[ i-- ] & 0xFF;
    val <<= 8;

    val |= buffer[ i-- ] & 0xFF;
    val <<= 8;

    val |= buffer[ i ] & 0xFF;
    return val;
   }


  /**
   *  Check that the requested event range is valid and adjust the number
   *  of events if needed.  After calling this method the fields 
   *  this.first_event and this.num_events should be properly set to load
   *  data from the file, if no exception is thrown.
   *
   * @param   first_event  The index of the first event whose pixel ID 
   *                       should be returned.
   * 
   * @param   num_events   The number of events whose pixel IDs 
   *                       should be returned.
   *
   */
  private void CheckEventRange( long first_event, long num_events ) 
               throws IllegalArgumentException
  {
    if ( first_event < 0  ||
         first_event >= numEntries() )
      throw new IllegalArgumentException( "First event " + first_event + 
                                          " invalid, not between 0 and " +
                                          (numEntries() - 1) );
    this.first_event = first_event;

    if ( num_events < 0  )
      throw new IllegalArgumentException( "Num events " + num_events +
                                          " invalid, must be at least 0" +
                                          " and no more than " +
                                          Integer.MAX_VALUE  );

    long requested_num = Math.min( num_events, numEntries() - first_event );
    if ( requested_num > MAX_LIST_SIZE )
      throw new IllegalArgumentException("Num events " + num_events +
                                         " invalid, limited to the maximum " +
                                         " array size " + MAX_LIST_SIZE );
    this.num_events = requested_num;
  }


  /**
   *  main program providing basic test for this class
   */
  public static void main(String[] args)
  {
//  String file_name = "/usr2/SNAP_2/EVENTS/SNAP_238_neutron_event.dat";
    String file_name = "/usr2/ARCS_SCD_2/EVENTS/ARCS_1250_neutron_event.dat";
//  String file_name = "/usr2/ARCS_SCD_3/EVENTS/ARCS_1853_neutron_event.dat";
//  String file_name = "/usr2/ARCS_SCD/ARCS_419_neutron_event.dat";
    SNS_TofEventList event_list = new SNS_TofEventList( file_name );

    long FIRST       =  0;
    long NUM_TO_LOAD = 10;
    long num_entries = event_list.numEntries();

    System.out.println("Number of events = " + num_entries );

    boolean long_test = true;
    if ( long_test )
      NUM_TO_LOAD = num_entries;

    long start = System.nanoTime();
    int[] tof_list = event_list.eventTof( FIRST, NUM_TO_LOAD );
    int[] id_list  = event_list.eventPixelID( FIRST, NUM_TO_LOAD );
    long end = System.nanoTime();
    
    System.out.printf("Time to load = %5.1f ms\n", (end - start)/1.0e6 );

    if ( !long_test )
      for ( int i = 0; i < tof_list.length; i++ )
        System.out.printf( "%6d    %8d   %8d\n", i, tof_list[i], id_list[i] );
    else
      for ( int i = 0; i < 3; i++ )
        System.out.printf( "%6d    %8d   %8d\n", i, tof_list[i], id_list[i] );
  }

}

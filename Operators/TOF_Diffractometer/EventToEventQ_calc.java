/* 
 * File:  EventToEventQ_calc.java
 *
 * Copyright (C) 2011, Dennis Mikkelson
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

package Operators.TOF_Diffractometer;

import java.io.*;
import gov.anl.ipns.Util.File.*;
import EventTools.EventList.*;



public class EventToEventQ_calc
{

  public static final String ASCII         = "ASCII";
  public static final String LITTLE_ENDIAN = "BINARY_LITTLE";
  public static final String BIG_ENDIAN    = "BINARY_BIG";

  public static final int DEFAULT_SEG_SIZE = 64 * SNS_TofEventList.BUFFER_SIZE;

   /**
    * This method will load an SNS raw event file, map those events to
    * reciprocal space, and then write the corresponding vector values
    * to a file.  The file can be in one of three formats, binary little
    * endian, binary big endian, or 3-column ASCII.  The values are written
    * in groups of 3 floats.  The first float is the component of Q in the
    * direction of the beam.  (This will always be negative.)  The second
    * float is the component of Q in the horizontal plane, perpendicular
    * to the beam.  The third float is the component of Q in the vertically
    * upward direction.  In all cases the magnitude of Q is taken to be 
    * 2*PI/d.
    *
    * The coordinates are listed in the ASCII file in exponential format.
    * NOTE: When writing the ASCII file, this method takes a long time to
    *       run and requires approximately 1-2 minutes for each 10
    *       million events.
    *
    * @param event_filename    The name of the file with events
    * @param DetCal_filename   The name of the file with the detector
    *                          calibrations
    * @param bank_filename     The name of the file with bank and pixelID(nex)
    *                          info
    * @param mapping_filename  The name of the file that maps DAS pixel_id's
    *                          to NeXus pixel_id's
    * @param first_event       The first Event to load
    * @param num_events        The number of events to load
    * @param out_filename      The name of the ASCII file that will be written
    * @param file_type         String specifying which type of file to write
    *                          "ASCII", 
    *                          "Binary_Little_Endian(PC)",
    *                          "Binary_Big_Endian(Java)"
    */

  public static void EventToEventQ( String event_filename,
                                    String DetCal_filename,
                                    String bank_filename,
                                    String mapping_filename,
                                    float  first_event,
                                    float  num_events,
                                    String output_filename,
                                    String file_type  )
                     throws Exception
  {
    file_type = file_type.toUpperCase();
    String instrument = FileIO.getSNSInstrumentName( event_filename );

    SNS_Tof_to_Q_map mapper = new SNS_Tof_to_Q_map( instrument,
                                                    DetCal_filename,
                                                    bank_filename,
                                                    mapping_filename,
                                                    null );

     SNS_TofEventList tof_evl = new SNS_TofEventList( event_filename );

     long first = (long)first_event;
     long num   = (long)num_events;

     if ( first >= tof_evl.numEntries() )
      throw new IllegalArgumentException("first event " + first_event +
                 " exceeds number of events in file " + tof_evl.numEntries());

     FileOutputStream     fos = new FileOutputStream( output_filename );
     BufferedOutputStream out = new BufferedOutputStream( fos );

                                                       // keep events in range
     long last = first + num - 1;
     if ( last >= tof_evl.numEntries() )
       last =  tof_evl.numEntries() - 1;

     long num_to_load  = last - first + 1;
     long seg_size     = DEFAULT_SEG_SIZE;
     long num_segments = num_to_load / seg_size + 1;
     long num_loaded   = 0;
     while ( num_loaded < num_to_load )
     {
       if ( first + seg_size - 1 > last )
         seg_size = last - first + 1;

       FloatArrayEventList3D q_list = 
                              mapper.MapEventsToQ(tof_evl, first, seg_size);
       
       System.out.println("  First = " + first + 
                          "  seg_size = " + seg_size +
                          "  q_list: " + q_list.numEntries() );

       if ( file_type.startsWith( ASCII ) )
         WriteAsASCII( q_list, out );
       else if ( file_type.startsWith( LITTLE_ENDIAN ) )
         WriteLittleEndian( q_list, out );
       else if ( file_type.startsWith( BIG_ENDIAN ) )
         WriteBigEndian( q_list, out );
       else
         throw new 
            IllegalArgumentException("File type not supported "+file_type);

       num_loaded += seg_size;
       first      += seg_size;
     }
     out.close();
     System.out.println("LOADED " + num_loaded );
  }


  private static void WriteLittleEndian( FloatArrayEventList3D q_list,
                                         BufferedOutputStream  out )
                      throws Exception
  {
    float[] xyz_vals = q_list.eventVals();
    byte[]  buffer   = new byte[ 4 * xyz_vals.length ];
    for ( int i = 0; i < xyz_vals.length; i++ )
      FileUtil.setFloat_32( xyz_vals[i], buffer, 4*i );
    out.write( buffer );
  }


  private static void WriteBigEndian( FloatArrayEventList3D q_list,
                                      BufferedOutputStream  out )
                      throws Exception
  {
    float[] xyz_vals = q_list.eventVals();
    byte[]  buffer   = new byte[ 4 * xyz_vals.length ];
    int     index    = 0;
    int     int_bytes;
    for ( int i = 0; i < xyz_vals.length; i++ )
    {
      int_bytes = Float.floatToRawIntBits( xyz_vals[i] );
      buffer[ index++ ] =(byte)(  int_bytes >> 24 );
      buffer[ index++ ] =(byte)( (int_bytes << 8) >> 24 );
      buffer[ index++ ] =(byte)( (int_bytes << 16) >> 24 );
      buffer[ index++ ] =(byte)( (int_bytes << 24) >> 24 );
    }
    out.write( buffer );
  }


  private static void WriteAsASCII( FloatArrayEventList3D q_list,
                                    BufferedOutputStream  out ) 
                      throws Exception
  {
     int num_cores = Runtime.getRuntime().availableProcessors();
     System.out.println("USING " + num_cores + " threads");
     Thread[] threads = new Thread[num_cores];

     String[] string_list = new String[ q_list.numEntries() ];

     if ( string_list.length < 1000 * num_cores )   // use single thread
       for ( int i = 0; i < q_list.numEntries(); i++ )
         string_list[i] = String.format( "%13.6e %13.6e %13.6e\n",
                      q_list.eventX(i), q_list.eventY(i), q_list.eventZ(i) );
     else
     {
       int start = 0;
       int n_per_thread = string_list.length / num_cores;
       for ( int th = 0; th < num_cores - 1; th++ )
       {
         threads[th] = new Formatter(start,n_per_thread,string_list,q_list);
         start += n_per_thread;
       }
       n_per_thread = string_list.length - start;
       threads[num_cores-1] =
                        new Formatter(start,n_per_thread,string_list,q_list);

       for ( int th = 0; th < num_cores; th++ )
         threads[th].start();

       for ( int th = 0; th < num_cores; th++ )
         threads[th].join();
     }

     for ( int i = 0; i < q_list.numEntries(); i++ )
       out.write( string_list[i].getBytes() );
  }


  /**
   *  Simple main program for testing
   */
  public static void main( String args[] ) throws Exception
  {
    String ev_file  = args[0];
    String out_file = args[1];
//    EventToEventQ( ev_file, "", "", "", 0, 1.0e10f, out_file, ASCII );
//    EventToEventQ( ev_file, "", "", "", 0, 1.0e10f, out_file, LITTLE_ENDIAN );
    EventToEventQ( ev_file, "", "", "", 0, 1.0e10f, out_file, BIG_ENDIAN );
  }

}

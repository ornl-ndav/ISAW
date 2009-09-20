/* 
 * File: SocketServerText.java
 *
 * Copyright (C) 2009, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$:
 *  $Date$:            
 *  $Rev$:
 */
package EventTools.ShowEventsApp.DataHandlers;

import java.util.*;
import java.net.SocketException;
import java.net.UnknownHostException;

import EventTools.EventList.SNS_TofEventList;
import NetComm.UDPSend;


/**
 * Sends UDP packets similar to the packet structure at the SNS. The
 * data comes from an Event file.
 * 
 * @author Ruth
 *
 */
public class SocketServerTest extends UDPSend
{
   /**
    *  Limit on the maximum number of events to send for each 1/60 second
    *  pulse.
    */
   public static final int MAX_PER_PULSE = 83333;   // 5 million events/sec

   /**
    * Constructor. The data is sent to port 8002 on the specified host.
    *
    * @param destination_node  The machine to which the UDP packets of events
    *                          are sent.
    *
    * @throws UnknownHostException
    * @throws SocketException
    */
   public SocketServerTest( String destination_node )
                            throws UnknownHostException , SocketException
   {
      super( destination_node, 8002 );
   }

   public void runTest( String EventFileName, int events_per_pulse )
   {
      Random ran_gen = new Random();

      SNS_TofEventList eventList = new SNS_TofEventList( EventFileName );
      long totNevents = eventList.numEntries();

      if ( totNevents > 8000000 )                   // do at most 8 million 
        totNevents = 8000000;                       // events for testing

      int[] all_tofs = eventList.eventTof( 0L, totNevents );
      int[] all_ids  = eventList.eventPixelID( 0L, totNevents );
      int[] ids, tof;

      int NPacketsSent = 0;

      if ( events_per_pulse > MAX_PER_PULSE )       // limit rate to about 5 
        events_per_pulse = MAX_PER_PULSE;           // million per second

      int firstEvent = 0;
      Vector<byte[]> packets  = new Vector<byte[]>(10);

      long last_time = System.currentTimeMillis();
      long curr_time;
      int  elapsed_time;

      while ( firstEvent < totNevents )  
      {                        
                                                    // Determine number of 
                                                    // events for this pulse
         int  n_to_send = (int)
                 ( events_per_pulse * (0.2 * ran_gen.nextDouble() + 0.8) );

                                                    // Don't try to send more
                                                    // events than remain  
         if ( firstEvent + n_to_send > totNevents )
           n_to_send = (int)(totNevents - firstEvent);

         packets.clear();
         while ( n_to_send > 0 )
         {                                          // determine number to
           int packet_size = n_to_send;             // send in this packet

           if ( packet_size > 2000 )                // can't do more than 2000
             packet_size = 2000;
                                                    // now build a packet of
           ids = new int[ packet_size ];            // the correct size
           tof = new int[ packet_size ];

           System.arraycopy( all_tofs, firstEvent, tof, 0, packet_size );
           System.arraycopy( all_ids,  firstEvent, ids, 0, packet_size );

                                                    // adjust the number to
                                                    // send and firstEvent
           n_to_send   = n_to_send - packet_size;
           firstEvent += packet_size;
                                                    // push header and data 
                                                    // into one byte array
           byte[] packet = new byte[ 24 + ids.length * 8 ];
           java.util.Arrays.fill( packet , 0 , 24 , (byte) 0 );
           int L = ids.length;
           packet[ 20 ] = LoByte( L );
           packet[ 21 ] = HiByte( L );

           int start = 24;
           for( int i = 0 ; i < ids.length ; i++ )
           {
              assign( tof[ i ] , packet , start );
              assign( ids[ i ] , packet , start + 4 );

              start += 8;
           }                                   
                                                     // save the byte array
           packets.add( packet );
         }

         try                                         // now try to actually 
         {                                           // send each packet
            for ( int i = 0; i < packets.size(); i++ )
            {
              byte[] packet = packets.elementAt(i);

              // System.out.write( packet );
              send( packet , packet.length );
              NPacketsSent++ ;

              if( NPacketsSent % 200 == 0 )
                 System.out.println( "sent packets =" + NPacketsSent );
            }
                                                   // send about 60 times/sec
            curr_time = System.currentTimeMillis();
            elapsed_time = (int)(curr_time - last_time);
            last_time = curr_time;
            if ( elapsed_time < 16 )
              Thread.sleep( 16 - elapsed_time );
            else
              Thread.sleep(1);                    // give the system a break
         }
         catch( Exception s )
         {
            s.printStackTrace();
            System.exit( 0 );
         }
      }
   }
   

   /**
    * Returns the hi(most significant) byte for the given number. Takes into 
    * account of negative numbers
    * @param offset  The number
    * @return   The most significant byte 
    * NOTE: The integer must be less than 2 bytes.
    */
   public static byte HiByte( int offset)
   {
      int N = 0;
      if(offset < 0 )
        N= 1;
     return (byte)(offset/256 - N);
   }

   
   /**
    * Returns the low(least significant) byte for the given number. Takes into 
    * account of negative numbers
    * @param offset  The number
    * @return   The least significant byte 
    * 
    */
   public static byte LoByte( int offset)
   {
        return (byte)(offset%256);
   }

   
   private void assign( int val, byte[] B, int start)
   {
      int TVal = val/(256*256);
      int LVal = val %(256*256);
      B[start]= LoByte(LVal);
      B[start+1]= HiByte(LVal);
      B[start+2]= LoByte(TVal);
      B[start+3]= HiByte(TVal);
   }


   /**
    * Test program
    * @param args
    *     args[0]  The name of the Event file
    *     args[1]  The maximum number of events to send for each pulse
    *              at approximately 1/60 sec per pulse 
    *     args[2]  The node name or IP address to which the event UDP packets
    *              will be sent.
    */
   public static void main( String[] args )
   {
     if( args == null || args.length < 1)
     {
       System.out.println("You must at least specify an event file to use");
       System.out.println("as the source of events sent to IsawEV.");

        System.exit(0);
     }
     
     int NEvents = 450;
     
     if ( args.length > 1)
        NEvents = Integer.parseInt( args[1] );

     String destination_node = "localhost";
     if (args.length > 2 )
       destination_node = args[2];
     
     try
      {
         SocketServerTest ss = new SocketServerTest( destination_node );
         ss.runTest( args[ 0 ] , NEvents );
      }
      catch( Exception s )
      {
         s.printStackTrace();
         System.exit( 0 );
      }
   }

}

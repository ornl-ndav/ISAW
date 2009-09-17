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


   int NPacketsSent =0;
   /**
    * Constructor. The data is sent to port 8002 on the localhost
    * @throws UnknownHostException
    * @throws SocketException
    */
   public SocketServerTest( )throws UnknownHostException , SocketException
   {
      super( "localhost" , 8002 );
   }
   public void runTest( String EventFileName, long NEvents )
           
   {

      
      SNS_TofEventList eventList = new SNS_TofEventList( EventFileName );
      if( NEvents < 80 )
         NEvents = 80;
      // if( NEvents >900)
      NEvents = 900;
      int[] ids , tof;
      long totNevents = eventList.numEntries();

      for( long firstEvent = 0 ; firstEvent < totNevents ; firstEvent += NEvents )
      {
         ids = eventList.eventPixelID( firstEvent , NEvents );
         tof = eventList.eventTof( firstEvent , NEvents );

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

         try
         {
            // System.out.write( packet );
            send( packet , packet.length );
            NPacketsSent++ ;
            if( NPacketsSent % 200 == 0 )
               System.out.println( "sent packets =" + NPacketsSent );
            if( ids.length < NEvents )
               System.exit( 0 );
            Thread.sleep(30 );
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
    *     args[1]  The number of events in each packet.(Not implemented)
    */
   public static void main( String[] args )
   {
     if( args == null || args.length < 1)
        System.exit(0);
     
     int NEvents = 0;
     
     if( args.length > 1)
        NEvents = Integer.parseInt( args[1] );
     
     try
      {
         SocketServerTest ss = new SocketServerTest();
         ss.runTest( args[ 0 ] , NEvents );
      }
      catch( Exception s )
      {
         s.printStackTrace();
         System.exit( 0 );
      }

   }

}

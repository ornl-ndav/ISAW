/* 
 * File: SocketEventLoader.java
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


import EventTools.EventList.TofEventList;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.SetNewInstrumentCmd;
import MessageTools.Message;
import MessageTools.MessageCenter;
import MessageTools.TimedTrigger;
import NetComm.IUDPUser;
import NetComm.UDPReceive;

/**
 * Class that loads event data sent via UDP sockets and sends the information
 * using messages to listeners.
 * 
 * @author Ruth
 * 
 */
public class SocketEventLoader extends UDPReceive
{



   public static int BUFF_SIZE     = 3000;

   public static int SEND_MINIMUM  = 2000;

   public static int SEND_MIN_TIME = 1500; // ms
   
   public static thisIUDPUser  user;
   public thisIUDPUser User;
   


   /**
    * Constructor
    * 
    * @param port
    *           The port listening for these UDP packets
    * @param message_center
    *           The message center to send messages to
    * @param Instrument
    *           The Instrument producing the UDP packets, or null
    */
   public SocketEventLoader( int port, MessageCenter message_center,
            String Instrument )
   {

      super( port , getUDPUser( message_center , Instrument ) );
      User = user;
   }
   
   
   private static thisIUDPUser getUDPUser( MessageCenter message_center , String Instrument )
   {
      user =new thisIUDPUser( message_center , Instrument );
      return user;
   }

   public void setPause( boolean doPause)
   {
      User.setPause( doPause);
   }

   /**
    * Test program using port 8002 and the SNAP instrument
    * 
    * @param args
    */
   public static void main( String[] args )
   {

      MessageCenter message_center = new MessageCenter( "TTest" );
      new TimedTrigger( message_center , 30 );
      SocketEventLoader ev = new SocketEventLoader( 8002 , message_center ,
               "SNAP" );
      ev.start();
   }
}


/**
 * Class for the user to process the UDP packets
 * 
 * @author Ruth
 * 
 */
class thisIUDPUser implements IUDPUser
{



   MessageCenter      message_center;

   int[]              tofBuff   = new int[ SocketEventLoader.BUFF_SIZE ];

   int[]              idBuff    = new int[ SocketEventLoader.BUFF_SIZE ];

   int                Buffstart = 0;

   long               timeStamp = - 1;
   
   boolean           pause = false;

   private static int Nshown    = 0;

   private static int NReceived = 0;


   /**
    * Constructor
    * 
    * @param message_center
    *           The message center to receive messages
    * @param Instrument
    *           The intrument. If non null an INIT_NEW_INSTRUMENT message will
    *           be sent.
    * 
    */
   public thisIUDPUser( MessageCenter message_center, String Instrument )
   {

      this.message_center = message_center;
      if( Instrument != null )
         message_center
                  .send( new Message( Commands.INIT_NEW_INSTRUMENT ,
                           new SetNewInstrumentCmd( Instrument , null , null ) ,
                           false ) );
      ( new timerThread( this ) ).start();
   }

   public void setPause( boolean doPause)
   {
      pause = doPause;
   }

   /**
    * Processes one data packet
    * 
    * @param data
    *           the data packet
    * @param length
    *           The number of bytes in the data
    */
   public void ProcessData( byte[] data , int length )
   {

      if( pause)
      {
         Buffstart =0;
         return;
      }
      Nshown = 0;
      if( length < 28 || data == null || data.length < length )
         return;

      if( length > 23 )
         if( data[ 0 ] == (byte) 0 && data[ 1 ] == (byte) ( 2 )
                  && data[ 2 ] == (byte) 0 && data[ 3 ] == (byte) 0 )
            if( data[ 27 ] > 0 && ( ( data[ 27 ] & 0x80 ) > 0 ) )
               return;

      NReceived++ ;

      int NEvents = Cvrt2Int( data , 20 );

      int[] ids = new int[ NEvents ];
      int[] tofs = new int[ NEvents ];
      int start = 24;
      // start=0; NEvents = length/2 with Dennis' interpretation
      if( Buffstart + NEvents >= SocketEventLoader.BUFF_SIZE )
      {
         // System.out.print( "PrcessDat1" );
         SendMessage( NEvents );
      }
      for( int i = Buffstart ; i < Buffstart + NEvents ; i++ )
      {
         tofBuff[ i ] = Cvrt2Int( data , start );
         idBuff[ i ] = Cvrt2Int( data , start + 4 );

         if( Nshown < 0 && NReceived % 20 == 0 )
         {
            System.out.println( String.format( "%8x,%8x,%2x,%2x,%2x,%2x" ,
                     tofs[ i ] , ids[ i ] , data[ start ] , data[ start + 1 ] ,
                     data[ start + 2 ] , data[ start + 3 ] ) );
            Nshown++ ;
         }
         start += 8;
      }

      Buffstart += NEvents;

      if( Buffstart > SocketEventLoader.SEND_MINIMUM )
      {
         // System.out.print( "PrcessDat2" );
         SendMessage( 0 );
      }

      // if( NReceived % 200 == 0 )
      // System.out.println( "Received packets =" + NReceived );
   }


   // Sends a message if enough info has been buffered or enough time has
   // passed
   protected synchronized void SendMessage( int NEvents )
   {

      long currTime = System.currentTimeMillis();

      if( ( Buffstart < SocketEventLoader.SEND_MINIMUM && Buffstart + NEvents < SocketEventLoader.BUFF_SIZE ) )
         if( currTime - timeStamp < SocketEventLoader.SEND_MIN_TIME )
            return;

      if( Buffstart == 0 )
         return;

      int[] tofs = new int[ Buffstart ];
      int[] ids = new int[ Buffstart ];

      System.arraycopy( tofBuff , 0 , tofs , 0 , Buffstart );
      System.arraycopy( idBuff , 0 , ids , 0 , Buffstart );
      Buffstart = 0;
     
      message_center.send( new Message( Commands.MAP_EVENTS_TO_Q ,
               new TofEventList( tofs , ids ) , false , true ) );

      timeStamp = System.currentTimeMillis();
      if( NEvents >= SocketEventLoader.BUFF_SIZE)
      {
         tofBuff= new int[ NEvents+10];
         idBuff =new int[ NEvents+10];
         SocketEventLoader.BUFF_SIZE = NEvents+10;
      }
   }


   private int Cvrt2Int( byte[] B , int start )
   {

      int NEvents = 0;

      for( int i = start + 3 ; i >= start ; i-- )
      {
         NEvents |= B[ i ] & 0xFF;
         if( i > start )
            NEvents <<= 8;

      }
      return NEvents;
   }

}


/**
 * Used to send messages after a given period of time has elapsed
 * 
 * @author Ruth
 * 
 */
class timerThread extends Thread
{



   thisIUDPUser user;


   public timerThread( thisIUDPUser user )
   {

      this.user = user;
   }


   public void run()
   {

      try
      {
         if( user == null )
            return;
         while( 3 == 3 )
         {
            Thread.sleep( SocketEventLoader.SEND_MIN_TIME );          
            user.SendMessage( 0 );
         }
      }
      catch( Exception s )
      {

      }
   }
}

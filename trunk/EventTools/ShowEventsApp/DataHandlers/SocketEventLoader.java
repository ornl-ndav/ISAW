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


import java.io.*;

import EventTools.EventList.FileUtil;
import EventTools.EventList.TofEventList;
import EventTools.ShowEventsApp.Command.Commands;
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
public class SocketEventLoader implements IUDPUser
{
   public static int BUFF_SIZE     = 200000;

   public static int SEND_MINIMUM  = BUFF_SIZE - 4000;

   public static int UDP_BUFFER_SIZE = 655360;   // enough buffer space for
                                                 // 10 max size UDP packets

   public static int SEND_MIN_TIME = 2000; // ms      
   UDPReceive         udpReceiver;
   
   boolean            udpReceiverStarted;
   
   Object             buffer_lock      = new Object();//for synchronization

   MessageCenter      message_center;

   int[]              sendBuff ;   
   
   int[][]            BuffPool         = new int[10]
                                              [ SocketEventLoader.BUFF_SIZE ];

   int                currentBuffPage  = 0;

   int                Buffstart        = 0;

   long               timeStamp        = - 1;
   
   boolean            pause            = false;

   boolean            sendProtOnCharge = false;
   
   boolean            sendNEvents      = false;


   //------------------- debug info -------------------


   public static int  debug = 0;

   int                NReceived = 0;

   int                total_received = 0;
   
   double             TotalProtonsOnTarget =0;
   
   FileOutputStream   fsave;   

   int                TotalEventDataSent2IsawEV  =   0;
   
   int[]              SavedEventsBuff;              



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
   public SocketEventLoader( int           port, 
                             MessageCenter message_center,
                             String        Instrument )
   {
      init( message_center, Instrument);
      
      udpReceiver = new UDPReceive( port, this );

      try
      {
        udpReceiver.setReceiveBufferSize( UDP_BUFFER_SIZE );
      }
      catch ( Exception ex )
      {
        System.out.println("EXCEPTION setting UDP receive buffer size");
      }

      udpReceiverStarted = false;
   }

   
   /**
    * Constructor
    * 
    * @param message_center   The message center to receive messages
    *           
    * @param Instrument       The intrument. If non null an 
    *                         INIT_NEW_INSTRUMENT message will be sent.
    */
   public void init( MessageCenter message_center, 
                     String        Instrument)
   {
      this.message_center = message_center;
          
      if ( SocketEventLoader.debug == 5 )
      {
         try
         {
            fsave = new FileOutputStream( "C:/Users/ruth/eventDat.dat" );

         } catch( Exception s )
         {
            fsave = null;
         }
         
         SavedEventsBuff = new int[ 20000000 ];
         
      } else
      {
         fsave = null;
         SavedEventsBuff = null;
      }
   
      
     Object Scaling = System.getProperty( "ScaleWith" );
      
     if( Scaling != null && Scaling.equals( "Protons on target" ))
         
         sendProtOnCharge = true;
      
      
      else if( Scaling != null && Scaling.equals( "NumEvents" ))
         
         sendNEvents = true;
     
     
     sendBuff = BuffPool[ currentBuffPage ];
     
     ( new timerThread( this ) ).start();
   }
  

   public void start()
   {
      if( udpReceiverStarted)
         return;
      udpReceiverStarted = true;
      udpReceiver.start();
   }

   
   public void interrupt()
   {
      udpReceiver.interrupt( );
   }

  
   public void close()
   {
     udpReceiver.close();
   }
   
 
   /**
    * Test program using port 8002 and the SNAP instrument
    * 
    * @param args
    */
   public static void main( String[] args )
   {
      int port = 8002;
      
      if( args!= null && args.length > 0)
           port = Integer.parseInt( args[0]);
      
      MessageCenter message_center = new MessageCenter( "TTest" );
      
      new TimedTrigger( message_center , 30 );
      
      SocketEventLoader ev = new SocketEventLoader( port , message_center ,
               "SNAP" );
      
      SocketEventLoader.debug = 20;
      
      ev.start();
   }


   public void setPause( boolean doPause)
   {
      pause = doPause;
   }
   
   /**
    * Resets the counter for the total protons on target.
    */
   public void resetAccumulator()
   {
      TotalProtonsOnTarget =0;
      
      NReceived =0;      
      
      Buffstart =0;
      
      total_received = 0;
      
      TotalEventDataSent2IsawEV = 0;
   }


   /**
    * Processes one data packet
    * 
    * @param data
    *           the data packet
    * @param length
    *           The number of bytes in the data
    */
   public void ProcessData(byte[] data, int length)
   {

      synchronized( buffer_lock )
      {
         if ( pause )
         {
            Buffstart = 0;
            return;
         }

         if ( data == null || data.length < length )
            return;

         if ( NReceived < 100 && SocketEventLoader.debug == 1 )
         {
            System.out.print( "data packet length=" + data.length + ","
                  + length );
            
            System.out.println( String.format( "%02x%02x%02x%02x" , data[0] ,
                  data[1] , data[2] , data[3] ) );
         }

         if ( ( NReceived + 1 ) % 200 < 0 )
         {
            String firstData = String.format(
                  "%02x%02x%02x%02x %02x%02x%02x%02x %02x%02x%02x%02x" ,
                  data[48] , data[49] , data[50] , data[51] , data[52] ,
                  data[53] , data[54] , data[55] , data[0] , data[1] , data[2] ,
                  data[3] );
            System.out.println( "received packets =" + NReceived + ",data="
                  + firstData );
            
         }

         if ( isCommandPacket( data , length ) )
         {
            ProcessCommandPacket( data , length );
            return;
            
         } else
            
            System.out.println( "Stray Packet" );

         return;
      }
   }     

   
   
   private boolean isCommandPacket( byte[] data, int length)
   {
      if( length <44)
         return false;
      
      if( data[4]!=0 ||data[5]!=2 ||data[6]!=0 ||data[4]!=0 )
         return false;
      
      return true;
   }
   
   
     private void ProcessDataPacket( byte[] data, int length )
     {
      NReceived++ ;
  
      int NEvents = Cvrt2Int( data , 8 )- Cvrt2Int( data , 12 );
      NEvents = NEvents/8;//8 bytes per event packet
      
      if( NEvents <=0  )
         return;
      
      int start = 24 + Cvrt2Int( data , 12);
      
      if( SocketEventLoader.debug == 2 )
      { 
         System.out.print("NEvents="+NEvents+", first data=");
         System.out.print( String.format( "%02x%02x%02x%02x " , 
               data[start+0],data[start+1],data[start+2],data[start+3] ) );
         System.out.print( String.format( "%02x%02x%02x%02x " , 
           data[start+4],data[start+5],data[start+6],data[start+7] ) );
         System.out.println( String.format( "%02x%02x%02x%02x " , 
                 data[0],data[1],data[2],data[3] ) );
         System.out.print( String.format( "%02x%02x%02x%02x " , 
           data[4],data[5],data[6],data[7] ) );
          System.out.println( );
      }
         
      total_received += NEvents;
     

      if( Buffstart + NEvents >= SocketEventLoader.BUFF_SIZE )
      {

         SendMessage( NEvents );
      }
      

      if( Buffstart + 2*NEvents >= SocketEventLoader.BUFF_SIZE )
      {

         SendMessage( NEvents );
      }
      
      for( int i = Buffstart ; i < Buffstart + 2*NEvents ; i++)
      {
         sendBuff[i]= Cvrt2Int( data, start );
         start +=4;
      }

      if( SocketEventLoader.debug==2)
        System.out.println("first float Data="
              +sendBuff[Buffstart  ]+","+ sendBuff[Buffstart+1]+","
              +sendBuff[Buffstart+2]+","+ sendBuff[Buffstart+3]);

      if( total_received < 0)
      {
         for( int i=0; i< Math.min( 2*total_received ,80 );i++)
            System.out.print(sendBuff[i]+",");
         
          System.out.println("");
      }
    
      Buffstart += 2*NEvents;

      if( Buffstart > SocketEventLoader.SEND_MINIMUM )
      {
         
         SendMessage( 0 );
      }
    }


     private void ProcessCommandPacket( byte[] data, int length)
     {
        ProcessDataPacket( data, length );
        int nPulseIDs = Cvrt2Int( data, 12 ) / 24;
        
        double protonsThisPacket =0;
        
        for( int i=0; i<nPulseIDs; i++)
           protonsThisPacket += FileUtil.getDouble_64(  data, 40 +i*24 );
        
        TotalProtonsOnTarget +=protonsThisPacket;
        
        if( SocketEventLoader.debug ==20)
               System.out.println("   prot on targ="+ protonsThisPacket);
        
        if(  sendProtOnCharge && protonsThisPacket !=0 )
              message_center.send( new Message( Commands.SCALE_FACTOR, 
                              (float)(protonsThisPacket), false, false));
        if( !sendNEvents)
           return;
        
        int NEvents = Cvrt2Int( data , 8 )- Cvrt2Int( data , 12 );
        NEvents = NEvents/8;//8 bytes per event packet
        
        message_center.send( new Message( Commands.SCALE_FACTOR, 
              (float)(NEvents), false, false));
        
     }
     
   
  
   // Sends a message if enough info has been buffered or enough time has
   // passed
   protected void SendMessage(int NEvents)
   {
      synchronized( buffer_lock )
      {
         long currTime = System.currentTimeMillis( );

         if ( Buffstart < SocketEventLoader.SEND_MINIMUM  &&
              Buffstart + 2 * NEvents < SocketEventLoader.BUFF_SIZE  &&
              currTime - timeStamp < SocketEventLoader.SEND_MIN_TIME )
            return;

         if ( Buffstart == 0 )
         {
            if ( SocketEventLoader.debug != 0 )
            {
               System.out.println( "Total events received=" + total_received
            
                     + "total sent =" + ( TotalEventDataSent2IsawEV / 2.0 ) );
               System.out.println("Total protons on target="+
                                   TotalProtonsOnTarget);
            }
         
            if ( fsave != null && TotalEventDataSent2IsawEV > 300000
                  && SocketEventLoader.debug == 5 )
               try
               {
                  System.out.println( "Start writing file. Don't exit" );
                  for( int i = 0 ; i < TotalEventDataSent2IsawEV ; i += 2 )
                  {
                     fsave.write( String.format( "%5d %5d \n" ,
                           SavedEventsBuff[i] , SavedEventsBuff[i + 1] )
                           .getBytes( ) );
                     if ( ( i + 1 ) % 10000 == 0 )
                        fsave.flush( );
                  }
                  fsave.close( );
                  System.out.println( "Finished writing file. You can exit" );
                  fsave = null;
               } catch( Exception s )
               {
                  fsave = null;
               }

            return;
         }

         TofEventList raw_events = 
                          new TofEventList( sendBuff, Buffstart / 2, false );
 
         if(SocketEventLoader.debug ==20)
            for( int i=0; i< Buffstart; i+=2)
            {
               System.out.print( "("+sendBuff[i] +","+sendBuff[i+1]+")   ");
               if( i%20 ==0)
                  System.out.println( );
            }
            
         message_center.send( 
             new Message( Commands.MAP_EVENTS_TO_Q, raw_events, false, true ) );

         if( SocketEventLoader.debug == 5)
            System.arraycopy( sendBuff, 0, 
                              SavedEventsBuff, TotalEventDataSent2IsawEV, 
                              Buffstart );

         TotalEventDataSent2IsawEV += Buffstart;
         Buffstart = 0;
         
         //--------------------------Will check first & 2nd entry if neg
         //                           can reuse
         currentBuffPage++;
         currentBuffPage = currentBuffPage % 10;
         sendBuff = BuffPool[ currentBuffPage ];
         //------------------------------
         timeStamp = System.currentTimeMillis( );
      }
   }

   private int Cvrt2Int(byte[] B, int start)
   {

      /*int NEvents = 0;

      for( int i = start + 3 ; i >= start ; i-- )
      {
         NEvents |= B[ i ] & 0xFF;
         if( i > start )
            NEvents <<= 8;
      }
      
      return NEvents;
      */
      return FileUtil.getInt_32( B , start );
   }

   // }

   /**
    * Used to send messages after a given period of time has elapsed
    * 
    * @author Ruth
    * 
    */
   class timerThread extends Thread
   {

      SocketEventLoader user;

      public timerThread(SocketEventLoader user)
      {

         this.user = user;
      }

      
      public void run()
      {

         try
         {
            if ( user == null )
               return;
            
            while( true )
            {
               Thread.sleep( SocketEventLoader.SEND_MIN_TIME );
               user.SendMessage( 0 );
            }
         } catch( Exception s )
         {
            System.out.println( "This catch block should not have been empty" );
            System.out.println( s );
            s.printStackTrace( );
         }
      }
   }
}

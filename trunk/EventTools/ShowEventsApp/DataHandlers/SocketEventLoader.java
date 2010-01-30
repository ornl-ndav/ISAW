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


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

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
   public static int BUFF_SIZE     = 10000;

   public static int SEND_MINIMUM  = BUFF_SIZE - 4000;

   public static int SEND_MIN_TIME = 2000; // ms
   
   public static int START_CMD_INDX_TARTG_PROTO = 40;
   public static int NUM_CMD_TARTG_PROTO = 8;
   public static int HEADER_PACKET_2POS = 6;
   
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
            String Instrument, String detInfFile, String IncidSpectraFile )
   {
      super( port , getUDPUser( message_center , Instrument, detInfFile,IncidSpectraFile ) );
      User = user;
      try
      {
      START_CMD_INDX_TARTG_PROTO = 
         Integer.parseInt( System.getProperty( "Command Packet Index Protons On Target","40"  ));
      }catch( Exception s)
      {
         START_CMD_INDX_TARTG_PROTO = 40;
      }
      
      HEADER_PACKET_2POS = DataSetTools.util.SharedData.getintProperty( 
            "Header_Packet_2_Position" , "6" );
   }
   

   public SocketEventLoader( int port, MessageCenter message_center,
            String Instrument)
   {
      this( port, message_center, Instrument, null, null);
   }
   
   
   private static thisIUDPUser getUDPUser( MessageCenter message_center , 
                                           String Instrument, 
                                           String detInfFile, 
                                           String IncidSpectraFile )
   {
      user =new thisIUDPUser( message_center , Instrument,detInfFile ,IncidSpectraFile );
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
   Object             buffer_lock = new Object();

   MessageCenter      message_center;

   int[]              tofBuff   = new int[ SocketEventLoader.BUFF_SIZE ];

   int[]              idBuff    = new int[ SocketEventLoader.BUFF_SIZE ];

   int                Buffstart = 0;

   long               timeStamp = - 1;
   
   boolean           pause = false;

   private        int Nshown    = 0;

   private        int NReceived = 0;

   private        int total_received = 0;
   
   private      double TotalProtonsOnTarget =0;
   
   private      boolean SendScale =
                      System.getProperty( "ScaleWith","" ).toUpperCase( )
                                  .equals( "PROTONS ON TARGET" ) ;

   /**
    * Constructor
    * 
    * @param message_center
    *           The message center to receive messages
    * @param Instrument
    *           The intrument. If non null an INIT_NEW_INSTRUMENT message will
    *           be sent.
    * @param detector_file_name
    *           The name of the file with the detector information or null for
    *           the default based on the instrument name.
    * @param incident_spectra_filename
    *           The name of the file with the incident specta or null for
    *           default
    * 
    */
   public thisIUDPUser( MessageCenter message_center, String Instrument, 
                         String detector_file_name,
                         String incident_spectra_filename )
   {
      this.message_center = message_center;
     
      if( Instrument != null )
         message_center
                  .send( new Message( Commands.INIT_NEW_INSTRUMENT ,
                           new SetNewInstrumentCmd( Instrument , 
                                   querieFile(detector_file_name) ,
                                   querieFile( incident_spectra_filename)) ,
                           false ) );
      ( new timerThread( this ) ).start();
    
   }

   private String querieFile( String fileName)
   {
      if( fileName == null || fileName.trim().length()<1)
         return null;
     if( !(new java.io.File( fileName)).exists())
        return null;
     return fileName;
   }

   public thisIUDPUser( MessageCenter message_center, String Instrument )
   {
      this(message_center, Instrument, null, null);
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
     
     synchronized (buffer_lock)
     {
      if ( pause )
      {
         Buffstart =0;
         return;
      }

      Nshown = 0;
         if ( data == null || data.length < length )
            return;
         if ( NReceived < 0 )
         {
            String S = "";
            for( int i = 0 ; i < Math.min( length , 24 ) ; i++ )
               S += String.format( "%02x," , data[i] );
            System.out.println( "---" + length + "||" + S );
         }
         if ( isCommandPacket( data, length ) )
         {
            ProcessCommandPacket( data, length );
            return;
         }else
            ProcessDataPacket( data, length );
         return;
     }
   }
     
   private boolean isCommandPacket( byte[] data, int length)
   {
      if( length <44)
         return false;
      if( data[4]!=0 ||data[5]!=0 ||data[6]!=6 ||data[4]!=0 )
         return false;
      return true;
   }
   
   
     private void ProcessDataPacket( byte[] data, int length )
     {

      NReceived++ ;
      if( length <52)
         return;
      if( data[6]!=2|| data[5]!=0 ||data[7]!=0)
         return;
      int NEvents = Cvrt2Int( data , 8)- Cvrt2Int( data , 12);
	  NEvents = length/8;
	  if( NEvents <=0  )
	     return;
	 
      total_received += NEvents;

      int[] ids = new int[ NEvents ];
      int[] tofs = new int[ NEvents ];
      int start = 48;
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

         if( Nshown < 0 )
         {
            System.out.println( String.format( "%08x,%08x," ,
                     tofBuff[ i ] , idBuff[ i ]  ));
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
    }
      // if( NReceived % 200 == 0 )
      // System.out.println( "Received packets =" + NReceived );
 

     private void ProcessCommandPacket( byte[] data, int length)
     {
                 
        TotalProtonsOnTarget += Cvrt2dbl( data, 40);
       
        if(  SendScale && TotalProtonsOnTarget !=0 )
              message_center.send( new Message( Commands.SCALE_FACTOR, 
                                      (float)( 1f/TotalProtonsOnTarget), true, true));
     }
     
     
  // First version
   private void ProcessCommandPacket0( byte[] data, int length)
   {
      
      if(  SocketEventLoader.START_CMD_INDX_TARTG_PROTO <20 )
         return;

      int commandPacket = SocketEventLoader.HEADER_PACKET_2POS %4;
      
      if( data[4+commandPacket] != (byte)2)
         return;
      
      for( int i= 1; i<4; i++ )
      {
        if( data[4+ (commandPacket+i) % 4] !=0) 
           return;
      }
     if( Cvrt2Int(data,16) <= 0)
         return;
          
      TotalProtonsOnTarget += Cvrt2dbl( data, SocketEventLoader.START_CMD_INDX_TARTG_PROTO);
      if(  SendScale && TotalProtonsOnTarget !=0 )
            message_center.send( new Message( Commands.SCALE_FACTOR, 
                                     1f/TotalProtonsOnTarget, true, true));
   }
   
   private double Cvrt2dbl( byte[] data , int start)
   {
      if( data == null || start <0|| data.length < start+8)
         return 0.;
      try
      {
         ByteArrayInputStream bStream = new ByteArrayInputStream(data,start,8);
         DataInputStream dStream = new DataInputStream( bStream );
         double x= dStream.readDouble( );
         
         return x;
      }catch(Exception s)
      {
         System.out.println("Socket Loader error="+s);
         return 0;
      }
      
      
   }
   // Sends a message if enough info has been buffered or enough time has
   // passed
   protected void SendMessage( int NEvents )
   {
     synchronized ( buffer_lock )
     {
      long currTime = System.currentTimeMillis();

      if(  Buffstart < SocketEventLoader.SEND_MINIMUM            && 
           Buffstart + NEvents < SocketEventLoader.BUFF_SIZE     &&
           currTime - timeStamp < SocketEventLoader.SEND_MIN_TIME )
          return;
 
      if( Buffstart == 0 )
      {
//       System.out.println("NO EVENTS NOW, Total sent = " + total_received );
         return;
      }
      
//    System.out.println("Total number of events sent = " + total_received );

      int[] tofs = new int[ Buffstart ];
      int[] ids = new int[ Buffstart ];

      System.arraycopy( tofBuff , 0 , tofs , 0 , Buffstart );
      System.arraycopy( idBuff , 0 , ids , 0 , Buffstart );
      Buffstart = 0;
     
      message_center.send( new Message( Commands.MAP_EVENTS_TO_Q ,
               new TofEventList( tofs , ids ) , false , true ) );

      timeStamp = System.currentTimeMillis();
     //Testing only--> message_center.send( new Message( Commands.SCALE_FACTOR, 1f/total_received, true, true));
/*
      if( NEvents >= SocketEventLoader.BUFF_SIZE)
      {
         tofBuff= new int[ NEvents+10 ];
         idBuff = new int[ NEvents+10 ];
         SocketEventLoader.BUFF_SIZE = NEvents+10;
      }
*/  }
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
         while( true )
         {
            Thread.sleep( SocketEventLoader.SEND_MIN_TIME );          
//          System.out.println("Timer calling SendMessage(0)");
            user.SendMessage( 0 );
         }
      }
      catch( Exception s )
      {
        System.out.println("This catch block should not have been empty");
        System.out.println( s );
        s.printStackTrace();
      }
   }
}

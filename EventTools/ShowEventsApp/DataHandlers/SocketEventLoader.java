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
import java.io.*;

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
public class SocketEventLoader
{
   public static int BUFF_SIZE     = 200000;

   public static int SEND_MINIMUM  = BUFF_SIZE - 4000;

   public static int UDP_BUFFER_SIZE = 655360;   // enough buffer space for
                                                 // 10 max size UDP packets

   public static int SEND_MIN_TIME = 2000; // ms
   
   //deprecated by new format
   public static int START_CMD_INDX_TARTG_PROTO = 40;
   public static int NUM_CMD_TARTG_PROTO = 8;
   public static int HEADER_PACKET_2POS = 6; 
   
   public thisIUDPUser User;
   private UDPReceive  udpReceiver;
   private boolean udpReceiverStarted;
   
   public static int debug = 0;


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
                             String        Instrument, 
                             String        detInfFile,
                             String        IncidSpectraFile, 
                             String        bankFile,
                             String        ID_MapFile )
   {
      User = new thisIUDPUser( message_center, 
                               Instrument,
                               detInfFile,
                               IncidSpectraFile,
                               bankFile,
                               ID_MapFile );
      udpReceiver = new UDPReceive( port, User );

      try
      {
        udpReceiver.setReceiveBufferSize( UDP_BUFFER_SIZE );
      }
      catch ( Exception ex )
      {
        System.out.println("EXCEPTION setting UDP receive buffer size");
      }

      udpReceiverStarted = false;
      try
      {
        START_CMD_INDX_TARTG_PROTO = 
          Integer.parseInt( 
            System.getProperty("Command Packet Index Protons On Target","40"));
      }
      catch( Exception s)
      {
         START_CMD_INDX_TARTG_PROTO = 40;
      }
      
      HEADER_PACKET_2POS = DataSetTools.util.SharedData.getintProperty( 
            "Header_Packet_2_Position" , "6" );
   }
   

   public SocketEventLoader( int port, MessageCenter message_center,
            String Instrument)
   {
      this( port, message_center, Instrument, null, null,null, null);
   }
   
  
   public void setPause( boolean doPause)
   {
      User.setPause( doPause);
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

   
   /**
    * Resets the counter for the total protons on target.
    */
   public void resetAccumulator()
   {
      User.resetAccumulator( );
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

   //deprecated for non interlaced form of event messages
   int[]              tofBuff   = new int[0];// SocketEventLoader.BUFF_SIZE ];
   
   //deprecated for non interlaced form of event messages
   int[]              idBuff    =new int[0];// SocketEventLoader.BUFF_SIZE ];

   int[]              sendBuff ;   
   int[][]            BuffPool  = new int[10][ SocketEventLoader.BUFF_SIZE ];
   int                currentBuffPage = 0;

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
   
   private     FileOutputStream   fsave;
   

   private int TotalEventDataSent2IsawEV  =   0;// debugging aids
   private int[] SavedEventsBuff;              //debugging aids
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
   public thisIUDPUser( MessageCenter message_center, 
                        String        Instrument, 
                        String        detector_file_name,
                        String        incident_spectra_filename,
                        String        bankFile,
                        String        ID_MapFile )
   {
      this.message_center = message_center;
     
      if( Instrument != null )
         message_center
                  .send( new Message( Commands.INIT_NEW_INSTRUMENT ,
                           new SetNewInstrumentCmd( Instrument , 
                                   querieFile(detector_file_name) ,
                                   querieFile( incident_spectra_filename),
                                   querieFile( bankFile),
                                   querieFile( ID_MapFile) ) ,
                           false ) );
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
      
      sendBuff = BuffPool[ currentBuffPage ];
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
      this(message_center, Instrument, null, null,null,null);
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

         if ( NReceived < 100 && SocketEventLoader.debug==1 )
         {
            System.out.print( "data packet length="+data.length+","+length);
           //SocketServerTest.showwpacket( data ,65, "packet in" );
            System.out.println( String.format("%02x%02x%02x%02x",
                        data[0],data[1],data[2],data[3]));
         }

         if( (NReceived+1) % 200 < 0 )
         {
            String firstData = String.format( 
                  "%02x%02x%02x%02x %02x%02x%02x%02x %02x%02x%02x%02x" , 
                  data[48],data[49],data[50],data[51],
                  data[52],data[53],data[54],data[55],
                  data[0], data[1], data[2], data[3]);
            System.out.println( "received packets =" + NReceived +
                                ",data=" + firstData );
            //  SocketServerTest.showwpacket( data, 65, 
            //                             "received packets =" + NReceived );
         }

         if ( isCommandPacket( data, length ) )
         {
            ProcessCommandPacket( data, length );
            return;
         }
         else
           System.out.println("Stray Packet");

         return;
     }
   }
     

   private boolean isCommandPacket( byte[] data, int length)
   {
      if( length <44)
         return false;
      if( data[4]!=0 ||data[5]!=0 ||data[6]!=2 ||data[4]!=0 )
         return false;
      return true;
   }

   
     private void ProcessDataPacket0( byte[] data, int length )
     {
      NReceived++ ;
      
      if( length <52)
         return;
      if( data[6]!=2|| data[5]!=0 ||data[7]!=0)
         return;
  
      int NEvents = Cvrt2Int( data , 8)- Cvrt2Int( data , 12);
	  NEvents = NEvents/8;//8 bytes per event packet
	  
	  if( NEvents <=0  )
	     return;
	 
      total_received += NEvents;
    
      //int[] ids = new int[ NEvents ];
     // int[] tofs = new int[NEvents ];
      int start = 24 + Cvrt2Int( data , 12);
      // start=0; NEvents = length/2 with Dennis' interpretation
      if( Buffstart + 2*NEvents >= SocketEventLoader.BUFF_SIZE )
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
     
      // start=0; NEvents = length/2 with Dennis' interpretation
      if( Buffstart + NEvents >= SocketEventLoader.BUFF_SIZE )
      {
         // System.out.print( "PrcessDat1" );
         SendMessage( NEvents );
      }
      
      // start=0; NEvents = length/2 with Dennis' interpretation
      if( Buffstart + 2*NEvents >= SocketEventLoader.BUFF_SIZE )
      {
         // System.out.print( "PrcessDat1" );
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
         // System.out.print( "PrcessDat2" );
         SendMessage( 0 );
      }
    }


     private void ProcessCommandPacket( byte[] data, int length)
     {
        ProcessDataPacket( data, length );
        int nPulseIDs = Cvrt2Int( data, 12 ) / 24;
        double protonsThisPacket =0;
        for( int i=0; i<nPulseIDs; i++)
           protonsThisPacket += Cvrt2dbl( data, 40 +i*24 );
        
        TotalProtonsOnTarget +=protonsThisPacket;
       
        if(  SendScale && protonsThisPacket !=0 )
              message_center.send( new Message( Commands.SCALE_FACTOR, 
                              (float)(protonsThisPacket), false, false));
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
          
      TotalProtonsOnTarget += Cvrt2dbl( data, 
                                 SocketEventLoader.START_CMD_INDX_TARTG_PROTO);
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

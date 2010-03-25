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
 *  $Author:$
 *  $Date:$            
 *  $Rev:$
 */
package EventTools.ShowEventsApp.DataHandlers;

import gov.anl.ipns.Util.File.RobustFileFilter;

import java.util.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import java.awt.Color;
import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;

import EventTools.EventList.SNS_TofEventList;
import NetComm.*;


/**
 * Sends UDP packets similar to the packet structure at the SNS. The
 * data comes from an Event file.
 * 
 * @author Ruth
 *
 */
public class SocketServerTest 
{
   /**
    *  Limit on the maximum number of events to send for each 1/60 second
    *  pulse.
    */
   public static final int MAX_PER_PULSE = 200000;   // 200,000 events/pulse
                                                     // is about max for 1Gb 
                                                     // network connection

   public static final int UDP_BUFFER_SIZE = 655360;
   
  
   public static int debug = 0;
   int nTimes =0;
   UDPSend udpSend;

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
        this( destination_node, 8002);
      }

   /**
    * Constructor. The data is sent to port 8002 on the specified host.
    *
    * @param destination_node  The machine to which the UDP packets of events
    *                          are sent.
    *
    * @throws UnknownHostException
    * @throws SocketException
    */
   public SocketServerTest( String destination_node, int portNum )
                            throws UnknownHostException , SocketException
   {
      udpSend = new UDPSend( destination_node, portNum );

      try
      {
         udpSend.setSendBufferSize( UDP_BUFFER_SIZE );
      }
      catch ( Exception ex )
      {
        System.out.println("EXCEPTION setting UDP receive buffer size");
      }


      /*try
      {
      START_CMD_INDX_TARTG_PROTO = 
         Integer.parseInt( System.getProperty( 
                           "Command Packet Index Protons On Target","40"));
      }catch( Exception s)
      {
         START_CMD_INDX_TARTG_PROTO = 40;
      }
      */
   }

   public void runTest( String EventFileName, int events_per_pulse )
   {
      Random ran_gen = new Random();
      int TotalEventsSent =0;
      
      SNS_TofEventList eventList = new SNS_TofEventList( EventFileName );
      long totNevents = eventList.numEntries();

      if ( totNevents > 8000000 )                   // do at most 8 million 
        totNevents = 8000000;                       // events for testing

      int[] all_tofs = eventList.eventTof( 0L, totNevents );
      int[] all_ids  = eventList.eventPixelID( 0L, totNevents );
  
      int[] ids, tof;
      
      if( debug ==1)
         System.out.println(
               String.format("tof = %4d,%4d,%4d,%4d, ids = %4d,%4d,%4d,%4d",
               all_tofs[0], all_tofs[1], all_tofs[2], all_tofs[3],
               all_ids[0],  all_ids[1],  all_ids[2],  all_ids[3])  );
      
      int NPacketsSent = 0;

      if ( events_per_pulse > MAX_PER_PULSE )       // limit rate  
        events_per_pulse = MAX_PER_PULSE; 

      int firstEvent = 0;
      Vector<byte[]> packets  = new Vector<byte[]>(10);

      long last_time = System.currentTimeMillis();
      long curr_time;
      int  elapsed_time;

      while ( firstEvent < totNevents )    // While more pulses
      {                        
         int NbytesSentInPacket =0;
                                                    // Determine number of 
                                                    // events for this pulse
         int  n_to_send = (int)
                 ( events_per_pulse * (0.2 * ran_gen.nextDouble() + 0.8) );

                                                    // Don't try to send more
                                                    // events than remain  
         if ( firstEvent + n_to_send > totNevents )
           n_to_send = (int)(totNevents - firstEvent);

         packets.clear();
         while ( n_to_send > 0 )      // split events for this pulse 
                                      // across multiple packets
         {                                          // determine number to
           int packet_size = n_to_send;             // send in this packet

           if ( packet_size > 2000 )                // can't do more than 2000
             packet_size = 2000;
                                                    // now build a packet of
           ids = new int[ packet_size ];            // the correct size
           tof = new int[ packet_size ];

           System.arraycopy( all_tofs, firstEvent, tof, 0, packet_size );
           System.arraycopy( all_ids,  firstEvent, ids, 0, packet_size );
           
           NbytesSentInPacket += packet_size;
                                                    // adjust the number to
                                                    // send and firstEvent
           n_to_send   = n_to_send - packet_size;
           firstEvent += packet_size;
                                                    // push header and data 
                                                    // into one byte array
          
           byte[] packet = new byte[ ids.length * 8 ];
           
           int start = 0;
           
           if( debug ==2 && packets.size()<1 && ids.length >2)
              System.out.println("float vals="+tof[0]+","+ids[0]+","
                    +tof[1]+","+ids[1]);
           
           for( int i = 0 ; i < ids.length ; i++ )
           {
              assign( tof[ i ] , packet , start );
              assign( ids[ i ] , packet , start + 4 );
             
              start += 8;
           }                                   
                                               // save the byte array
           packets.add( packet );
           TotalEventsSent += ids.length;
         }

         try 
         {  
            for( int i=0; i< packets.size( ); i++)
            { 
              byte[] packet1 = MakeCommandPacket( NbytesSentInPacket, 
                                                  packets.elementAt( i ) );
          
              assign(NPacketsSent+1, packet1,0);
              udpSend.send( packet1 , packet1.length );
 
              NPacketsSent++ ;
              
              if( NPacketsSent <10 && debug == 1)
                 showwpacket(packet1,packet1.length,"Whole packet");
             
              if( NPacketsSent % 193 < 0  )
              {
                 String firstData ="";
                 if(debug == 4)
                    firstData =",data="+ String.format( 
                    "%02x%02x%02x%02x %02x%02x%02x%02x %02x%02x%02x%02x" , 
                       packet1[48],packet1[49],packet1[50],packet1[51],
                       packet1[52],packet1[53],packet1[54],packet1[55],
                       packet1[0], packet1[1], packet1[2], packet1[3]);
                // packet1[0]=(byte)( NPacketsSent/200);
                // showwpacket( packet1,65,"sent packets =" + NPacketsSent);

                 System.out.println("sent packets =" + NPacketsSent+firstData );
                 System.out.println(" packet length =" + packet1.length );
                 System.out.println(" events in packet =" + 
                                                      (packet1.length-48)/8  );
                 System.out.println(" total events sent =" + TotalEventsSent );
              }
            }

            curr_time = System.currentTimeMillis();
            elapsed_time = (int)(curr_time - last_time);
            last_time = curr_time;

            if ( elapsed_time < 16 )
              Thread.sleep( 16 - elapsed_time );
            else
              Thread.sleep(0,1000);                // give the system a break
         }
         catch( Exception s )
         {
            s.printStackTrace();
            System.exit( 0 );
         }
      }
      
      System.out.println("Total events sent= "+ TotalEventsSent);
      if ( debug == 10 )
      {
         String dump_file = "C:/Users/ruth/event2.dat";
         try
         {
            System.out.println( "Saving to file, totNEvents=" + totNevents );
            FileOutputStream fout = new FileOutputStream( dump_file );
            for( int i = 0 ; i < totNevents ; i++ )
               fout.write( String.format( "%5d %5d \n" , all_tofs[i] ,
                     all_ids[i] ).getBytes( ) );
            fout.close( );
         } catch( Exception s )
         {
            System.out.println( "Exception dumping events to " + dump_file );
         }
         System.out.println( "File throught" );
      }
   }


   public static void showwpacket( byte[] packet,int length, String message)
   {
    System.out.println( message);
    int k=0;
    if( packet == null || packet.length < 1)
       return;
    if( length <=0)
       length = packet.length;
    length = Math.min( length , packet.length );
    
    for( int i=0; i+4<length;i+=4)
    {
       if( i%32 == 0 )
          System.out.println( );
       System.out.print( String.format( "%02x%02x%02x%02x " , 
                             packet[i],packet[i+1],packet[i+2],packet[i+3] ) );
       k=i+4;
    }

    for(int i=k; i < packet.length ;i++)
       System.out.print(  String.format( "%2x" , packet[i] ));
    System.out.println("");
    try
    {
      System.in.read( );
    }
    catch(Exception ss)
    {
      System.out.println("Exception reading from System.in");    
    }
   }


   //deprecated
   private byte[] MakeEventPacket( byte[] eventData)
   {
     byte[] Result = new byte[6*4+6*4+eventData.length];
     Arrays.fill( Result , (byte)0 );
     Result[6]= (byte)2;
     assign( eventData.length+24, Result,8);
     assign( 24, Result, 12);
     System.arraycopy( eventData , 0 , Result , 48 , eventData.length );
     return Result;
   }

   
   private byte[] MakeCommandPacket( int N2Bsent,byte[] eventData)
   {
      if( eventData == null)
         return null;

      int eventLength = eventData.length;
      
      byte[] Res = new byte[48 + eventLength];
      Arrays.fill( Res , (byte)0 );
      
      Res[5] = (byte)2;      
      assign(24,Res,12);
      
      assign( 24+eventLength, Res,8);
      
      if( debug == 2 )
         System.out.print( "NEvents="+eventLength/8+", first data=");
      try
      {
         ByteArrayOutputStream bStream = new ByteArrayOutputStream(10);
         DataOutputStream dStream = new DataOutputStream( bStream);
         dStream.writeDouble( (double ) eventLength);
         byte[] res = bStream.toByteArray( );
         
         if( res.length < 8)
            return null;
         for( int i = 0; i < 4 ; i++ )
         {
            byte save = res[i];
            res[i] =res[7-i];
            res[7-i] = save;
         }
         System.arraycopy( res,0,Res,40,8);
         
         int start = 48;
         
         if( eventLength > 0)
           
            {
               byte[] eventList = eventData;
               System.arraycopy(eventList , 0 , Res , start , eventList.length);
               start += eventList.length;
               if ( debug == 2  )
               {
                  System.out.print( String.format( "%02x%02x%02x%02x " ,
                        eventList[0] , eventList[1] , eventList[2] ,
                        eventList[3] ) );
                  System.out.print( String.format( "%02x%02x%02x%02x " ,
                        eventList[4] , eventList[5] , eventList[6] ,
                        eventList[7] ) );
                  System.out.print( String.format( "%02x%02x%02x%02x " ,
                        eventList[8] , eventList[9] , eventList[10] ,
                        eventList[11] ) );
                  System.out.print( String.format( "%02x%02x%02x%02x " ,
                        eventList[12] , eventList[13] , eventList[14] ,
                        eventList[15] ) );
                  System.out.println( );
               }
            }
         
      }catch(Exception s)
      {
         System.out.println( );
         return null;
      }
      return Res;
   }

   
   //old command packet ere 2/1/2010
  /* private byte[] MakeCommandPacket0( int N2Bsent)
   {
      byte[] Res = new byte[SocketEventLoader.START_CMD_INDX_TARTG_PROTO+32];
      Arrays.fill( Res , (byte)0 );
      Res[5] = (byte)2;
      
      assign(N2Bsent/8,Res,12);//woops packets 
      assign(N2Bsent, Res,16);
      Res[23]=(byte)0x80;
      try
      {
         ByteArrayOutputStream bStream = new ByteArrayOutputStream(10);
         DataOutputStream dStream = new DataOutputStream( bStream);
         dStream.writeDouble( (double ) N2Bsent);
         byte[] res = bStream.toByteArray( );
         if( res.length < 8)
            return null;
         System.arraycopy( res,0,Res,START_CMD_INDX_TARTG_PROTO,8);
      }catch(Exception s)
      {
         return null;
      }
      
      //assign(N2Bsent,Res,SocketEventLoader.START_CMD_INDX_TARTG_PROTO);
      
      return Res;
   }
*/

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
    *              will be sent. If "" will be local host
    *     args[3]  Port Num
    */
   public static void main( String[] args )
   {
      int port = 8002;
      
      if( args.length >=4)
         try
      {   
            port = Integer.parseInt(  args[3].trim() );
            if( port <=0)
               port = 8002;
      }catch( Exception sx)
      {
         port = 8002;
      }
         
      String destination_node ="";
     if( args == null || args.length < 1)
     {
//       DataSetTools.util.SharedData sd = new DataSetTools.util.SharedData();
       JFileChooser jfc = new JFileChooser( 
                                   System.getProperty( "Data_Directory","" ));
       jfc.setDialogType( JFileChooser.CUSTOM_DIALOG );
       RobustFileFilter filter = new RobustFileFilter();
       filter.addExtension( "dat" );
       filter.setDescription( "Raw Event File" );
       jfc.setFileFilter( filter );
       
       JPanel panel = new JPanel();
       BoxLayout blay = new BoxLayout( panel,BoxLayout.Y_AXIS);
       panel.setLayout( blay );
       
       jfc.setBorder(  new TitledBorder( new LineBorder( Color.black,2),
                "EVENT FILE NAME", TitledBorder.CENTER, TitledBorder.TOP ) );
       
       panel.add(jfc);
       
       JPanel panel1 = new JPanel();
       panel1.setLayout(  new GridLayout( 3,2) );
       JTextField  TextNEvents = new JTextField("450");
       JTextField TextIP = new JTextField("");
       
       panel1.add( new JLabel("Max Number of events per pulse"));
       panel1.add( TextNEvents );
       
       panel1.add( new JLabel("Blank or the recipient node name or IP"+
                      " address "));
       panel1.add( TextIP );
       JTextField Port = new JTextField( ""+port);
       panel1.add(  new JLabel("Port Number") );
       panel1.add( Port );
       panel1.setBorder(  new TitledBorder( new LineBorder( Color.black,2),
                "OTHER PARAMETERS(opt)", TitledBorder.CENTER,TitledBorder.TOP));
       panel.add(  panel1);
      
       
       String filename = null;
       JOptionPane jopt = new JOptionPane();
       int Res =jopt.showConfirmDialog( null, panel, "Inputs to Test program",
                                        JOptionPane.OK_CANCEL_OPTION );
     
       
       if( Res == JOptionPane.OK_OPTION && jfc.getSelectedFile()!= null)
       {
           filename = jfc.getSelectedFile().getPath(); 
           System.out.println("filename = "+filename);
       } 
       else
       {
          System.out.println(" Need to specify an Event file");
          System.out.println(
                  " In addition the number of events to send for each pulse");
          System.out.println(" and the name or IP address to send packets to");
          System.exit( 0 );
       }
       
       int MaxEvents = -1;
       try
       {
          MaxEvents = Integer.parseInt(  TextNEvents.getText().trim() );
       }
       catch(Exception s1)
       {
          MaxEvents =-1;
       }
       
       String IP = TextIP.getText();
       int n=3;
       
       if( IP == null || IP.trim().length() < 1)
          IP= "localhost";
       
       try
       {
          port = Integer.parseInt( Port.getText( ).trim());
          if( port <=0)
             port = 8002;
          
       }catch( Exception sss)
       {
          port = 8002;
       }
       
       if( n < 3 )
       {
         if( MaxEvents <0)
           MaxEvents =450;
       }
       else if( MaxEvents < 0)
          n = 1;
       
       args= new String[n + 1];
       args[0]= filename;
       if( n>1)
          args[1] = ""+MaxEvents;
       if( n>2)
          args[2] = IP;
       if( n > 3)
          args[3] =""+port;
     }
    
     int NEvents = 450;
     
     if ( args.length > 1)
        NEvents = Integer.parseInt( args[1] );

     destination_node = "localhost";
     
     if (args.length > 2 )
       destination_node = args[2];     
  
     try
     {
       SocketServerTest ss = new SocketServerTest( destination_node, port );
    
       ss.runTest( args[ 0 ] , NEvents );
     }
     catch( Exception s )
     {
       s.printStackTrace();
       System.exit( 0 );
     }
   }

}

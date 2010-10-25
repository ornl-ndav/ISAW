/* 
 * File: SNS_TofEventListNX.java
 *
 * Copyright (C) 2010, Ruth Mikkelson
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
package EventTools.EventList;

import gov.anl.ipns.Util.Sys.StringUtil;

import java.util.Arrays;

import NexIO.NexApi.*;
import NexIO.Util.*;
import NexIO.*;

/**
 * Retrieves 
 * @author ruth
 *
 */
public class SNS_TofEventListNX implements ITofEventList
{

   long[] eventsInPulse;
   String[] NXeventNames;
   int NumInitialEvents;
   int pulse1,pulse2;
   NexNode  topNode, EntryNode;
   public static String EVENT_INDEX ="event_index";
   public static String EVENT_DATA = "NXevent_data";
   public static String PIX_IDS ="event_pixel_id";
   public static String TOF_FIELD ="event_time_of_flight";
   
   /**
    * Constructor
    * @param filename The name of the event NeXus file
    * 
    * NOTE: Throws a lot of IllegalArgument exceptions if the file does not
    *  appear correct
    */
   public SNS_TofEventListNX( String filename) 
   {
      NumInitialEvents = pulse1 = pulse2 = -1;
      eventsInPulse = new long[20000];
      Arrays.fill( eventsInPulse , 0 );
      NXeventNames = new String[20000];
      Init(filename);
   }
  
   /**
    * Sets the number of initial events to load from all the NXdata_events first
    * IsawEV only displays first n events. The rest are used for coloring, peaks, etc.
    * 
    * @param n  The number of start events to load from all NXevent data's  or -1.
    * 
    */
   public void setInitialNEvents( int n)
   {
      NumInitialEvents = n;
   }
   
   /**
    * Loads the pixelID's from all NXevent_data from pulse1 containing
    * the first event to pulse2(exclusive) which contains event firstEvent
    * + numEvents.  
    */
   @Override
   public int[] eventPixelID(long firstEvent, long numEvents)
   {
      return eventInfo(firstEvent,numEvents, PIX_IDS, null,false);
   }
   
   private int[]  eventInfo( long firstEvent, long numEvents, String Field,int[] left,boolean convert)
   {
    //TODO: implement NumInitialEvents. Just do 2 rounds.
      //Use if NumInitialEvents >0 && numEvents > numInitialEvents
      numEvents = Math.min(  numEvents , numEntries()-firstEvent );
      if( NXeventNames== null || numEvents <=0 || EntryNode == null)
         return null;
      
      int[] pulseRange = CalcPulseRange(firstEvent, numEvents, eventsInPulse);
      if( pulseRange[0] < 0)
         pulseRange[0]=0;
      if( pulseRange[1]<0)
         pulseRange[1]= (int)numEvents;
      
      int k=0;
      int[] Res = new int[ (int)numEvents];
      
      int startDet =0;
      if( left != null && left.length==2)
         {
           startDet = left[0];
           k= left[1];
         }
    
      
      int i;
      for(  i=0; i< NXeventNames.length && k < numEvents; i++)
      {
         NxNode child = EntryNode.getChildNode( NXeventNames[(startDet+i)% NXeventNames.length ]);
         float[] DetField1 =NexUtils.getFloatArrayFieldValue( child,Field);
         int[] DetEventIndx =NexUtils.getIntArrayFieldValue( child,EVENT_INDEX);
       
         if( DetField1 != null && DetEventIndx != null &&DetField1.length>= pulseRange[0]
                   && DetEventIndx.length >= eventsInPulse.length)
         {
            if( convert)
            {
               String units = ConvertDataTypes.StringValue( 
                     child.getChildNode( Field ).getAttrValue( "units") );
               
               if( units != null && Field.indexOf( "time" )>=0)//time units assumed
               {
                  float mult =ConvertDataTypes.getUnitMultiplier( units , "us" )*10;
                  //Rest assumes raw tof is in 100 nanoseconds
                  for( int j=0;j<DetField1.length;j++)
                     DetField1[j]=(int)(mult*DetField1[j]);
               }
            }
            int[] DetField = new int[DetField1.length];
            for( int j=0; j< DetField.length; j++)
               DetField[j] =(int)(.5+DetField1[j]);
            
            int start = DetEventIndx[pulseRange[0]];
            if( i==0 && left != null && left.length==2 && left[1] >=0)
               start = left[1];
            
            long end = this.numEntries();
            if( pulseRange[1] >=0 && pulseRange[1]<end)
               end = DetEventIndx[pulseRange[1]];
            if( end <0)
               end = DetField.length;
           // System.out.println(NXeventNames[i]+","+start+","+end);
            System.arraycopy( DetField , start , Res , k ,
                       Math.min((int) numEvents-k , (int)(end-start)));
            
           
            
            k += Math.min( (int)numEvents-k , (int)(end-start));
            if( left != null && left.length==2)
               left[1]=k;
         }
         
      }
      if( left !=null)
      {
         left[0]=i;
         
      }
      //TODO: fix when initial events is implemented
      if( Res.length == k || left != null)
         return Res;
      
      int[] Res1 = new int[k];
      System.arraycopy( Res,0,Res1,0,k);
      return Res1;
      
      
      

   }

   
   /**
    * Loads the time_of_flights's from all NXevent_data from pulse1 containing
    * the first event to pulse2(exclusive) which contains event firstEvent
    * + numEvents.  
    */
   @Override
   public int[] eventTof(long firstEvent, long numEvents)
   {

      return eventInfo( firstEvent,numEvents, TOF_FIELD,null,true);

   }

   @Override
   public long numEntries()
   {

      if( eventsInPulse != null || eventsInPulse.length < 1)
         return eventsInPulse[eventsInPulse.length-1];
      
      return 0;
   }

   
   /**
    * Loads the events from all NXevent_data from pulse1 containing
    * the first event to pulse2(exclusive) which contains event firstEvent
    * + numEvents.  
    */
   @Override
   public int[] rawEvents(long firstEvent, long numEvents)
   {  
      numEvents = Math.min(  numEvents , numEntries()-firstEvent );
       if( NXeventNames== null || numEvents <=0 || EntryNode == null)
         return null;

      
      int[]data = eventTof(firstEvent,numEvents);
      if( data== null)
         return null;
      int[] Res = new int[2*(int)data.length];
      for( int i=0; i<data.length;i++)
         Res[2*i]=data[i];
      
      data = eventPixelID( firstEvent, numEvents);
      if( data ==  null || data.length*2 != Res.length)
         return null;
      for( int i=0; i<data.length;i++)
         Res[2*i+1]=data[i];
      
      
      return Res;
      
   }
   
   private void Init( String filename)
   {
      try
      {
      topNode = new NexNode(filename);
      EntryNode = null;
      if( topNode == null)
         {
            eventsInPulse = new long[1];
            eventsInPulse[0] = 0;
            NXeventNames = null;
            return;
         }
      for( int i=0; i < topNode.getNChildNodes( ) && EntryNode == null;i++)
      {
        NxNode child = topNode.getChildNode( i );
        if( child.getNodeClass( ).equals( "NXentry"))
           EntryNode = (NexNode)child;
      }
      
      if( EntryNode == null)
      {

         eventsInPulse = new long[1];
         eventsInPulse[0] = 0;
         NXeventNames = null;
         return;
      }
      int N=0;
      int Npulses = 0;
      for( int i=0; i < EntryNode.getNChildNodes( ); i++)
      {
         NxNode child = EntryNode.getChildNode( i );
         if( child.getNodeClass( ).equals( "NXevent_data" ))
         {
            NXeventNames[N]=child.getNodeName( );
            N++;
            NxNode ev_pulse = child.getChildNode( EVENT_INDEX );
            if( ev_pulse != null)
            {
                 int[] D = ConvertDataTypes.intArrayValue( ev_pulse.getNodeValue( ) );
                 if( D != null)
                 {
                    Npulses = Math.max(  Npulses , D.length );
                     eventsInPulse = expand(eventsInPulse, D);
                 }
                 NxNode nd =child.getChildNode( TOF_FIELD );
                 int[] dim = nd.getDimension();
                 int last = 0;
                 if( D != null)
                    for( int j=0;j< D.length;j++)
                    {  
                       if( D[j]>= 0)
                          last = D[j];
                       else
                          last= dim[0];
                       eventsInPulse[j]+=last;
                    }
            }
         }
      }
      
      if( Npulses >0)
      {
         
         while(Npulses>0&&  eventsInPulse[Npulses-1]< 0)
            Npulses--;
         long[] Sav = new long[Npulses];
         System.arraycopy( eventsInPulse , 0 , Sav , 0 , Npulses );
         eventsInPulse = Sav;
      }
      
      }catch(Throwable s)
      {
         throw new IllegalArgumentException( s.toString());
      }
      
      if( eventsInPulse == null || eventsInPulse.length < 1 ||
            NXeventNames==null || NXeventNames.length < 1 )
         throw new IllegalArgumentException( "No Data ");
      
      if( topNode == null || EntryNode == null)
         throw new IllegalArgumentException( "No Data ");
   }
   
   private long[] expand( long[] BigIntArray, int[] NewIntArray)
   {

      if ( BigIntArray == null )
         if ( NewIntArray == null )
            return null;
         else
         {
            long[] Res = new long[ NewIntArray.length ];
            Arrays.fill( Res , 0 );
            return Res;
         }
      
      if( NewIntArray == null || NewIntArray.length <= BigIntArray.length)
         return BigIntArray;
      long[] Res = new long[NewIntArray.length];
      Arrays.fill(Res,0);
      System.arraycopy( BigIntArray,0,Res,0,BigIntArray.length);
      return Res;
      
   }
   
   // returns 2 ints, first is first pulse, and last is 2nd pulse. If -1 all pulses are used.
   private int[] CalcPulseRange( long firstEvent, long  NumEvents, long[] eventsInPulse)
   {
      int[] Res = new int[2];
      Res[0]=Res[1] = -1;
      if( eventsInPulse == null || NumEvents <=0)
         return Res;
      
      Res[0] = Arrays.binarySearch( eventsInPulse , firstEvent );
      Res[1] = Arrays.binarySearch(  eventsInPulse , firstEvent+NumEvents );
      if( Res[0] <0)
         Res[0]=-Res[0]-1;
      if( Res[1] < 0)
         Res[1] = -Res[1]-1;
      return Res;
      
   }
   
   public static void main( String[] args)
   {
      String filename = "C:/ISAW/Sampleruns/SNS/EventNXs/PG3_732_event.nxs";
      SNS_TofEventListNX events = new SNS_TofEventListNX( filename);
     // System.out.println( StringUtil.toString( events.eventPixelID( 0 , 200 ),true));
      int nevents = (int)events.numEntries( );
      int[] X =events.eventTof( nevents-200 , 1200 );
      System.out.println("#,nevents="+X.length+","+nevents);
      System.out.println( StringUtil.toString( X,true));
      //System.out.println( StringUtil.toString( events.rawEvents( 0 , 200 ),true));
   }
}

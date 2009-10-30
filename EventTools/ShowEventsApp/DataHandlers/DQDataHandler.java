/* 
 * File: DQDataHandler.java
 *
 * Copyright (C) 2009 Dennis Mikkelson
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
 *  $Author: eu7 $
 *  $Date: 2009-08-13 00:27:33 -0500 (Thu, 13 Aug 2009) $            
 *  $Revision: 19830 $
 */


package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;

import gov.anl.ipns.Util.Numeric.ClosedInterval;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import DataSetTools.dataset.*;
import DataSetTools.util.SharedData;
import Operators.Generic.Save.SaveASCII_calc;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.SetNewInstrumentCmd;
import EventTools.ShowEventsApp.Command.Util;
import EventTools.EventList.IEventList3D;
import MessageTools.*;

/**
 *  This class accumulates histograms in d-spacing and reciprocal space (Q)
 *  for events that have been weighted based on geometric corrections 
 *  (and possibly correction for the incident spectrum).  It passes these
 *  histograms on to the D and Q View handler classes.
 */
public class DQDataHandler implements IReceiveMessage
{
   public static final int   NUM_BINS = 10000;
   public static final float MAX_Q = 20;
   public static final float MAX_D = 10;
 
   private MessageCenter messageCenter;
   private MessageCenter viewMessageCenter;
   
   boolean normalizeQ,    // if true send normalized Q data
           normalizeD;    // if true send normalized D data
   float scale_factor;
   
   String incidentSpectraFileName;
   float[] Wl_list;       //incident spectrum values
   float minWL,maxWL, MM; // parameters to find correct entry in the list
   int nWL;               // The number of wavelengths in Wl_list
   
   private float[][] d_values = new float[2][NUM_BINS+1];
   private float[][] q_values = new float[2][NUM_BINS+1]; 
   private float[] dn_values = new float[NUM_BINS+1];
   private float[] qn_values = new float[NUM_BINS+1]; 
   float[][] Q_values = new float[2][], //will hold the info sent
             D_values = new float[2][];


  /**
   *  Construct a DQDataHandler to get and receive messages from the
   *  specified MessageCenter.
   *
   *  @param  messageCenter     The message center to listen to for comands
   *  @param  viewMessageCenter The message center to which the D and Q 
   *                            arrays will be sent.
   */
   public DQDataHandler( MessageCenter messageCenter,
                         MessageCenter viewMessageCenter )
   {
      this.messageCenter = messageCenter;
      this.viewMessageCenter = viewMessageCenter;

      this.messageCenter.addReceiver(this, Commands.ADD_EVENTS_TO_HISTOGRAMS);

      this.messageCenter.addReceiver(this, Commands.INIT_DQ);

      this.messageCenter.addReceiver(this, Commands.GET_D_VALUES);
      this.messageCenter.addReceiver(this, Commands.GET_Q_VALUES);

      this.messageCenter.addReceiver(this, Commands.SAVE_Q_VALUES);
      this.messageCenter.addReceiver(this, Commands.SAVE_D_VALUES);

      this.messageCenter.addReceiver(this, Commands.SCALE_FACTOR);
      this.viewMessageCenter.addReceiver(this, Commands.NORMALIZE_QD_GRAPHS);
   
      scale_factor = -1;
      incidentSpectraFileName = null;
      normalizeQ = normalizeD = false;
      Q_values[1] = q_values[1];
      D_values[1] = d_values[1];
      setXs();
      clearYs();
   }
   

  /**
   *  Clear the list of Y values for the D and Q histograms.
   */
   synchronized private void clearYs()
   {
     for ( int i = 0; i <= NUM_BINS; i++ )
       d_values[1][i] = 0;

     for ( int i = 0; i <= NUM_BINS; i++ )
       q_values[1][i] = 0;
     
     java.util.Arrays.fill( dn_values , 0f );
     java.util.Arrays.fill( qn_values , 0f );
     
   }

   public void setUpIncidentSpectrum( String filename, String InstrumentName)
   {
      if( filename== null || filename.length() < 1 ||
               !( new java.io.File( filename)).exists())
      {
        
        filename = SharedData.getProperty( "ISAW_HOME" ,"" ).trim();
        if( !filename.endsWith( "/" ) || !filename.endsWith( "\\" ))
           filename +="/";
        filename += "InstrumentInfo/SNS/"+InstrumentName+"_Spectrum.dat";
        if( !( new java.io.File(filename).exists()))
        {

          Wl_list = null;
          minWL= maxWL=Float.NaN;
          nWL = 0;
          return; 
        }
        }
      DataSet DS = Util.ReadDSFile( filename );
      if( DS == null || DS.getNum_entries()<1)
      {
         Wl_list = null;
         minWL= maxWL=Float.NaN;
         nWL = 0;
        return; 
      }
      Data D = DS.getData_entry( 0 );
      XScale xscl = D.getX_scale();
      float start = xscl.getStart_x();
      float end = xscl.getEnd_x();
      int nxs = xscl.getNum_x();
      if( end <= start || nxs <=0 || start < 0 || end <=.1f)
      {
         Wl_list = null;
         minWL= maxWL=Float.NaN;
         nWL = 0;
        return; 
      }

      nWL =(int) ( (end-start)/end/.02f +.5f);
      nWL = Math.max(  nWL, xscl.getNum_x() );
      xscl =new UniformXScale(start, end,nWL);
      D.resample(xscl  , 0);
      float[] ys=   D.getY_values();
      int i=0;
      for( i=0; i < ys.length && ys[i] <=0 ;i++)
      {}
      if( i == ys.length)
      {
         Wl_list = null;
         minWL= maxWL=Float.NaN;
         nWL = 0;
        return; 
      }
      int j=0;
      for( j=ys.length-1; j>=0 && ys[j] <=0 ; j--){}

      if( j < 0 || (j==0 && ys[j]<=0) || ((j-i+1)<12))
      {
         Wl_list = null;
         minWL= maxWL=Float.NaN;
         nWL = 0;
        return; 
      }
      Wl_list = new float[ j-i+1];
      System.arraycopy( ys,i , Wl_list ,0,j-i+1 );
      
      minWL= xscl.getX( i );
      if( j+1 < xscl.getNum_x())
         maxWL =xscl.getX( j+1 );
      else
         maxWL = xscl.getEnd_x();
      
      nWL = j-i+1;
      MM = nWL/(maxWL-minWL);
      
      
         
   }

  /**
   *  Set up the array of X-values for the D and Q histograms.
   */
   synchronized private void setXs()
   {
     for ( int i = 0; i <= NUM_BINS; i++ )
       d_values[0][i] = i * MAX_D / NUM_BINS;

     for ( int i = 0; i <= NUM_BINS; i++ )
       q_values[0][i] = i * MAX_Q / NUM_BINS;
     
      Q_values[0]= q_values[0];
      D_values[0] = d_values[0];
   }


  /**
   * Add the specified list of events to the current histograms for
   * d-spacing and Q, using the weight of each event.
   *
   * @param events  The IEventList3D object containing the events.
   *                This MUST have the weights set.
   */
   synchronized private void AddEvents( IEventList3D events )
   {
     float xyz[] = events.eventVals();
//   float weights[] = events.eventWeights();
     int   n_events = events.numEntries();
     int   index = 0;
     int   bin_num;
     float mag_q,
           d_val;
     float x, y, z;
     float[] q_arr = q_values[1];
     float[] d_arr = d_values[1];

     for ( int i = 0; i < n_events; i++ )
     {
       x = xyz[index++];
       y = xyz[index++];
       z = xyz[index++];
       float Qsq =x*x + y*y + z*z;
       mag_q = (float)Math.sqrt( Qsq );
       d_val = (float)(2 * Math.PI / mag_q);
       
       //------ calculate weight for normalized values
       float wl = 2*d_val*(float)Math.sqrt( .5+2*x*x/(Qsq*Qsq) );
       float weight =0;
       if( wl >= minWL && wl < maxWL && wl >0)
          weight = Wl_list[(int)( (wl-minWL)*MM)];
       if( weight !=0)
          weight = 1/weight;
       else if( Wl_list == null)
          weight = 1;
       //  --------------------------------
       
       bin_num = (int)(NUM_BINS * mag_q/MAX_Q); 
       if ( bin_num <= NUM_BINS )
         {
          q_arr[bin_num] += 1; //weights[i];
          qn_values[bin_num] +=weight;
         }

       if ( mag_q > 0 )
       {
         
         bin_num = (int)(NUM_BINS * d_val/MAX_D);
         if ( bin_num <= NUM_BINS )
           {
            d_arr[bin_num] += 1;  //weights[i];
            dn_values[ bin_num] +=weight;
           }
       }
     }
/*
     System.out.println("*********** Processed events " + n_events );
     System.out.println("*********** Q VALUES ARE : " );
     for ( int i = 0; i <= NUM_BINS; i++ )
       System.out.printf("x = %5.2f  y = %5.2f \n", 
                          q_values[0][i], q_values[1][i] );
*/
     for ( int i = 0; i < 2; i++ )           // set values at low Q and D
     {                                       // to zero.  TODO: why was this
       q_arr[i] = 0;                         // needed when dealing with
       d_arr[i] = 0;                         // simulated TOPAZ data?
     }
   }


   /**
    * Convenience method to send a message to the VIEW message center
    * used by this class.
    *
    * @param command  The command name (i.e. queue) for this message.
    * @param value    The value object for this message.
    */
   private void sendViewMessage(String command, Object value)
   {
      Message message = new Message(command, value, true, true);
      
      viewMessageCenter.send(message);
   }
   
   
   /**
    *  Process messages: ADD_EVENTS_TO_HISTOGRAMS, CLEAR_DQ, GET_D_VALUES
    *  and GET_Q_VALUES.  
    *
    *  @param message The message containing the command to be 
    *                 carried out. 
    */
   public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.ADD_EVENTS_TO_HISTOGRAMS))
      {
        Object obj = message.getValue();

        if ( obj == null || !(obj instanceof IEventList3D) )
        {
          Util.sendError( "NULL or Empty EventList in DQHandler");
          return false;
        }

        AddEvents( (IEventList3D)obj );
        float[][] q1_vals = Scale( Q_values, normalizeQ);
        float[][] d1_vals = Scale( D_values, normalizeD);
        synchronized( q_values )
        {
          sendViewMessage(Commands.SET_Q_VALUES, q1_vals );
          sendViewMessage(Commands.SET_D_VALUES, d1_vals );
        }
      }
      
      if (message.getName().equals(Commands.INIT_DQ))
      {
        clearYs();
        SetNewInstrumentCmd cmd =(SetNewInstrumentCmd) message.getValue();
        scale_factor = cmd.getScaleFactor();
        incidentSpectraFileName = (String)cmd.getIncidentSpectrumFileName();
        setUpIncidentSpectrum( incidentSpectraFileName, cmd.getInstrumentName() );
        Message init_dq_done = new Message( Commands.INIT_DQ_DONE,
                                            null,
                                            true,
                                            true );
        messageCenter.send( init_dq_done );

      }
      
      if (message.getName().equals(Commands.GET_D_VALUES))
      {
        sendViewMessage(Commands.SET_D_VALUES, Scale(D_values, normalizeD) );
        return true;
      }
      
      if (message.getName().equals(Commands.GET_Q_VALUES))
      {
         
         sendViewMessage(Commands.SET_Q_VALUES, Scale(Q_values, normalizeQ) );
         return true;
      }
      
      if( message.getName().equals(Commands.SAVE_D_VALUES))
      {
         DataSet D = MakeDataSet( D_values,"D Graph","Angstrom");
         String fileName = (String)message.getValue();
         return SaveDataSetASCII(D , fileName);
      }
      
      if( message.getName().equals(Commands.SAVE_Q_VALUES))
      {
         DataSet D = MakeDataSet( Q_values,"Q Graph", "Inv Angstrom");
         String fileName = (String)message.getValue();
         return SaveDataSetASCII(D , fileName);
      }
      
      if( message.getName().equals(  Commands.SCALE_FACTOR ))
      {
         scale_factor =((Float) message.getValue()).floatValue();
      }
      
      if( message.getName().equals(  Commands.NORMALIZE_QD_GRAPHS ))
      {
         Vector res = (Vector)( message.getValue());
         boolean show = ((Boolean)res.firstElement()).booleanValue();
         String D_Q   =((String)res.lastElement()).toString();
         if(  D_Q =="Q")
            if( show == normalizeQ )
               return false;
             else
             {
               normalizeQ = show;
               if( show)
                  Q_values[1] = qn_values;
               else
                  Q_values[1] = q_values[1];
             }
         if(  D_Q =="D")
            if( show == normalizeD )
               return false;
            else 
            {
               normalizeD = show;
               if( show)
                  D_values[1] = dn_values;
               else
                  D_values[1] =d_values[1];
            }
         if( D_Q =="D")
            sendViewMessage(Commands.SET_D_VALUES, Scale(D_values, normalizeD) );
         else if( D_Q =="Q")
            sendViewMessage(Commands.SET_Q_VALUES, Scale(Q_values, normalizeQ) );
         
         
      }
      
      return false;
   }
   
      
   private boolean SaveDataSetASCII( DataSet D, String fileName)
   {
      UniformXScale sc = D.getXRange();
      ClosedInterval intv = D.getYRange();
      String fmt= getCFormat( sc.getStart_x(), sc.getEnd_x(),sc.getNum_x());
      fmt += " "+getCFormat( intv.getStart_x(), 
                             intv.getEnd_x(), 
                             2*sc.getNum_x());
      try{ SaveASCII_calc. SaveASCII( D, false,fmt, fileName);
      }catch( Exception ss)
      {
         return false;
      }
      return true;
   }


   // attempts to have 6 digits showing and each entry from start to end 
   // in nSteps.  shows a different String
   public static String getCFormat( float start, float end, int nSteps)
   {
      if( start > end)
      {
         float save = start;
         start = end;
         end = save;
         
      }else if( start == end && start == 0)
         return "%6.1f";
      
      if( nSteps <= 0)
         nSteps = 1;
      
      //Extra digit for (-)
      int x = 0;
      if( start < 0 || end < 0)
         x = 1;
      
      int nDigits2Left= (int)( Math.log10( Math.max( Math.abs(start) , 
                                           Math.abs( end ) ))) +1;
      if( nDigits2Left < 0)
         nDigits2Left = 0;
      
      int nDigits2Right =0;
      
      if( start < end )
      {
         double dd= Math.log10( ( end - start)/nSteps );
         if( dd < 0)
            nDigits2Right = -(int)Math.floor(dd) +1;
      }
      
      if( nDigits2Left > 6)
          if(nDigits2Right == 0)
             return "%"+(nDigits2Left+x)+".0f";
          else 
             return "%"+(nDigits2Left+x)+"."+nDigits2Right+"f";
      else
      {
         nDigits2Right =6-nDigits2Left-x;
         return "%"+(nDigits2Left+x)+"."+nDigits2Right+"f";
      }
   }
   
   
   private DataSet MakeDataSet( float[][] vals, String Title,String xUnits)
   {
      DataSet D = new DataSet( );
      D.setTitle( Title );
      D.setX_units( xUnits);
      Data Db = new FunctionTable( new VariableXScale(vals[0]), vals[1],1);
      D.addData_entry( Db );
      Db.setSelected( true );
      return D;
   }

   private float[][] Scale( float[][] qvals, boolean normalize)
   {
      if(  qvals == null)
         return qvals;
      if( scale_factor < 0 || !normalize)
         return qvals;
      
      float[][] Res = new float[2][qvals[1].length];
      Res[0] = qvals[0];
      for( int i=0; i < qvals[1].length; i++)
      {
         float m=1;
         if( scale_factor > 0)
            m= scale_factor;
         
         Res[1][i] =m* qvals[1][i];           
         
      }
      return Res;
      
      
      
   }
   
  
   public static void main( String[] args)
   {
      System.out.println( 
               DQDataHandler.getCFormat( Float.parseFloat( args[0] ),
                                         Float.parseFloat( args[1]), 1000 ));
   }

}

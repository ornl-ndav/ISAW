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

import EventTools.ShowEventsApp.Command.Commands;
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
   private float[][] d_values = new float[2][NUM_BINS+1];
   private float[][] q_values = new float[2][NUM_BINS+1]; 


  /**
   *  Construct a DQDataHandler to get and receive messages from the
   *  specified MessageCenter.
   *
   *  @param  messageCenter  The messge center to listen to for comands
   *                         and to which the D and Q arrays will be sent.
   */
   public DQDataHandler(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      this.messageCenter.addReceiver(this, Commands.ADD_EVENTS_TO_VIEW);
      this.messageCenter.addReceiver(this, Commands.CLEAR_DQ);
      this.messageCenter.addReceiver(this, Commands.GET_D_VALUES);
      this.messageCenter.addReceiver(this, Commands.GET_Q_VALUES);

      setXs();
      clearYs();
   }
   

  /**
   *  Clear the list of Y values for the D and Q histograms.
   */
   private void clearYs()
   {
     for ( int i = 0; i <= NUM_BINS; i++ )
       d_values[1][i] = 0;

     for ( int i = 0; i <= NUM_BINS; i++ )
       q_values[1][i] = 0;
   }


  /**
   *  Set up the array of X-values for the D and Q histograms.
   */
   private void setXs()
   {
     for ( int i = 0; i <= NUM_BINS; i++ )
       d_values[0][i] = i * MAX_D / NUM_BINS;

     for ( int i = 0; i <= NUM_BINS; i++ )
       q_values[0][i] = i * MAX_Q / NUM_BINS;
   }


  /**
   * Add the specified list of events to the current histograms for
   * d-spacing and Q, using the weight of each event.
   *
   * @param events  The IEventList3D object containing the events.
   *                This MUST have the weights set.
   */
   private void AddEvents( IEventList3D events )
   {
     float xyz[] = events.eventVals();
     float weights[] = events.eventWeights();
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
       mag_q = (float)Math.sqrt( x*x + y*y + z*z );

       bin_num = (int)(NUM_BINS * mag_q/MAX_Q); 
       if ( bin_num <= NUM_BINS )
         q_arr[bin_num] += weights[i];

       if ( mag_q > 0 )
       {
         d_val = (float)(2 * Math.PI / mag_q);
         bin_num = (int)(NUM_BINS * d_val/MAX_D);
         if ( bin_num <= NUM_BINS )
           d_arr[bin_num] += weights[i];
       }
     }
/*
     System.out.println("*********** Processed events " + n_events );
     System.out.println("*********** Q VALUES ARE : " );
     for ( int i = 0; i <= NUM_BINS; i++ )
       System.out.printf("x = %5.2f  y = %5.2f \n", 
                          q_values[0][i], q_values[1][i] );
*/
   }


   /**
    * Convenience method to send a message to the message center
    * used by this class.
    *
    * @param command  The command name (i.e. queue) for this message.
    * @param value    The value object for this message.
    */
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command, value, true);
      
      messageCenter.receive(message);
   }
   
   
   /**
    *  Process messages: ADD_EVENTS_TO_VIEW, CLEAR_DQ, GET_D_VALUES
    *  and GET_Q_VALUES.  
    *
    *  @param message The message containing the command to be 
    *                 carried out. 
    */
   public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.ADD_EVENTS_TO_VIEW))
      {
        Object obj = message.getValue();

        if ( obj == null || !(obj instanceof IEventList3D) )
          Util.sendError( messageCenter, 
                         "NULL or Empty EventList in DQHandler");
        AddEvents( (IEventList3D)obj );
      }
      
      if (message.getName().equals(Commands.CLEAR_DQ))
      {
        clearYs();
      }
      
      if (message.getName().equals(Commands.GET_D_VALUES))
      {
        sendMessage(Commands.SET_D_VALUES, d_values );
        return true;
      }
      
      if (message.getName().equals(Commands.GET_Q_VALUES))
      {
         sendMessage(Commands.SET_Q_VALUES, q_values );
         return true;
      }
      
      return false;
   }
}

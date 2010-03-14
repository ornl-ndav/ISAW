/* 
 * File: UpDateTrigger.java
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
 *  $Author$:
 *  $Date$:            
 *  $Rev$:
 */
package EventTools.ShowEventsApp.DataHandlers;

import EventTools.ShowEventsApp.Command.Commands;
import MessageTools.*;

/**
 * This class sends UPDATE messages to a message center whenever it
 * receives an UPDATE message.  As a result the message center 
 * ALWAYS has another UPDATE message to send out each time it processes
 * messages.  Consequently, this forces UPDATE messages to be sent out
 * at the rate at which the message center processes messages.
 * In most cases, this should only be used with a message center that
 * processes messages quite slowly, such as the "view_messages_center"
 * for IsawEV, which is only sends messages several seconds.
 * @author ruth
 */
public class UpDateTrigger implements IReceiveMessage
{
   MessageCenter messageCenter;

   /**
    * Constructor
    * 
    * @param messageCenter The message center that is to receive the UPDATE 
    *                      command. This class also receives the command
    *                      back, which causes another UPDATE command to be 
    *                      given. 
    *            
    * NOTE: This assumes that the message center is already being triggered 
    *       to processes all received messages at a steady rate.
    */
   
   public UpDateTrigger( MessageCenter messageCenter )
   {
      this.messageCenter = messageCenter;
      messageCenter.addReceiver( this, Commands.UPDATE );
      messageCenter.send( new Message( Commands.UPDATE, null, true) );
   }
   
   @Override
   public boolean receive(Message message)
   {
      messageCenter.send( new Message( Commands.UPDATE, null, true) );
      return false;
   }


/**
 * Test program 
 * 
 * @param args  no arguments are used
 */
   public static void main(String[] args)
   {
     MessageCenter messageCenter = new MessageCenter("XXX");
     UpDateTrigger trig = new UpDateTrigger( messageCenter);
     TimedTrigger t = new TimedTrigger( messageCenter, 2500);
   }
}

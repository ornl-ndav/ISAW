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
 * This class creates update messages to a message center at a rate 
 * determined by the message sender. The message sender should be 
 * triggered to send out its messages every so often
 * @author ruth
 *
 */
public class UpDateTrigger implements IReceiveMessage
{
   MessageCenter messageCenter;

   /**
    * Constructor
    * 
    * @param messageCenter The message center that is to receive
    *            the UPDATE command. This also receives the command
    *            back, which will cause another UDATE command to be 
    *            given. 
    *            
    * NOTE: The message center should be triggered to send out all
    *       messages every so often
    */
   
   public UpDateTrigger( MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      messageCenter.addReceiver( this, Commands.UPDATE  );
      messageCenter.send(  new Message( Commands.UPDATE, null, true) );
      
      
   }
   
   
   @Override
   public boolean receive(Message message)
   {
      
      messageCenter.send(  new Message( Commands.UPDATE, null, true) );
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

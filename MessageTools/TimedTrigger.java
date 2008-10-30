/*
 * File:  TimedTrigger.java
 *
 * Copyright (C) 2006 Dennis Mikkelson
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2007/08/14 00:09:01  dennis
 *  Added MessageTools files from UW-Stout repository.
 *
 *  Revision 1.1  2006/10/30 00:55:35  dennis
 *  Initial version of simple "trigger" class to trigger the
 *  processing of messages, by a MessageCenter.
 *
 */

package MessageTools;

import java.awt.event.*;
import javax.swing.*;


/**
 *  This class will send PROCESS_MESSAGES message to a MessageCenter, at
 *  regular time intervals, to trigger the processing of messages by the
 *  MessageCenter.  Without such a request to process messages, the message
 *  center will just continue to receive messages, but never process them.
 */
public class TimedTrigger 
{
  private  MessageCenter  message_center;
  private  Timer          timer;

  /* ------------------------ constructor ------------------------------ */
  /**
   *  Make a TimedTrigger object to trigger the processing of messages
   *  at the specified time interval, for the specified MessageCenter.
   *
   *  @param  center     The message center handling messages for this 
   *                     application
   *  @param  time_in_ms The time between frames.
   */
  public TimedTrigger( MessageCenter center, 
                       int           time_in_ms )
  {
    message_center = center;

    timer = new Timer( time_in_ms, new TimerListener() );
    timer.start();
  }


  /* ------------------------- TimerListener --------------------------- */
  /**
   *  When the timer fires, the actionPerformed method in this internal
   *  class will trigger message delivery by the message center.
   */
  private class TimerListener implements ActionListener
  {
     public void actionPerformed( ActionEvent e )
     {
        message_center.receive( MessageCenter.PROCESS_MESSAGES ); 
     }
  }
  
} 
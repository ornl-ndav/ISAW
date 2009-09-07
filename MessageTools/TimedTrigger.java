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
 *  $Author$
 *  $Date$            
 *  $Revision$
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
  private  boolean        call_dispatch;

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

    call_dispatch = false;
    Thread dispatch_thread = new CallDispatchThread();
    dispatch_thread.start(); 
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
        if ( !call_dispatch )            // if we're not already working on 
          call_dispatch = true;          // it, trip the call_dispatch flag
     }                                   // to start processing messages
  }


  /* ----------------------- CallDispatchThread ------------------------ */
  /**
   *  This class is the Thread that actually calls dispatchMessages().  
   *  We run this in a separate thread to avoid tying up the AWT Event
   *  thread.  This thread will run as long as the application is running.
   *  Whenever the timer trips the "call_dispatch" flag, this thread
   *  thread calls the dispatchMessages() method on the MessageCenter. 
   */
  protected class CallDispatchThread extends Thread
  {
    public void run()
    {
      while ( true )                       // keep looping forever
      {
        if ( call_dispatch )
        {
          try
          {
            message_center.dispatchMessages();
          }
          catch ( Throwable ex )
          {
            System.out.println("Exception processing messages : " + ex );
            ex.printStackTrace();
          }
          finally
          {
            call_dispatch = false;
          }
        }
        try
        {
          Thread.sleep(30);
        }
        catch ( Exception ex )
        {
          System.out.println("Exception sleeping in CallDispatchThread");
        }
      }
    }
  }
  
} 

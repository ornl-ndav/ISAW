/*
 * File:  UpdateManager.java
 *
 * Copyright (C) 2005-2009 Dennis Mikkelson
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
 *  This class coordinates the processing of messages and updating of 
 *  objects at regular intervals.  A timer triggers the message center 
 *  to process messages, then the Update methods of all the listed Objects
 *  are called, to redraw or carry out what ever action is needed after
 *  a list of messages is processed.
 */
public class UpdateManager 
{
  private  MessageCenter  message_center;
  private  IUpdate[]      updateables = null;
  private  Timer          timer;
  private  boolean        call_dispatch;

  /* ------------------------ constructor ------------------------------ */
  /**
   *  Make an UpdateManager for the specified MessageCenter and set of 
   *  panels.
   *
   *  @param  center     The message center handling messages for this 
   *                     application
   *  @param  objs       Array of IUpdate() objects with Update() methods 
   *                     that are to be called after messages are delivered.
   *  @param  time_in_ms The time between frames.
   *
   *  @throws IllegalArgumentException if some object in the list is null 
   *          or does not implement IUpdate()
   */
  public UpdateManager( MessageCenter center, 
                        IUpdate       objs[],
                        int           time_in_ms )  
                        throws IllegalArgumentException
  {
    message_center = center;
                                          // set up the list of Updateables
    if ( objs != null )
    {
      updateables = new IUpdate[ objs.length ];
      for ( int i = 0; i < objs.length; i++ )
        if ( objs[i] != null )
          updateables[i] = objs[i];
        else
          throw 
            new IllegalArgumentException("null Object or NOT IUpdate:" + i );
    }

    timer = new Timer( time_in_ms, new TimerListener() );
    timer.start();

    call_dispatch = false;
    Thread dispatch_thread = new CallDispatchThread();
    dispatch_thread.start();
  }


  /* ------------------------- TimerListener --------------------------- */
  /**
   *  When the timer fires, the actionPerformed method in this internal
   *  class will trigger message delivery by the message center, and 
   *  update any Updateables, if some messages were delivered in the
   *  call dispatch thread and and returned true. 
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
   *  If dispatchMessages() returns true, then the Update() methods will
   *  be called for all IUpdate objects passed to the constructor.
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
            boolean changed = message_center.dispatchMessages();
            if ( changed && updateables != null )
              for ( int i = 0; i < updateables.length; i++ )
                updateables[i].Update();
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

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
  }


  /* ------------------------- TimerListener --------------------------- */
  /**
   *  When the timer fires, the actionPerformed method in this internal
   *  class will trigger message delivery by the message center, and 
   *  redraw all JoglPanels, if some messages were actually delivered. 
   */
  private class TimerListener implements ActionListener
  {
     public void actionPerformed( ActionEvent e )
     {
       if ( message_center.dispatchMessages() )
       {
         if ( updateables != null )
           for ( int i = 0; i < updateables.length; i++ )
             updateables[i].Update();
       }
     }
  }
  
} 

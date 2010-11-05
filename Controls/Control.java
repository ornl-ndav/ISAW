/*
 * File:  Control.java
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
 *  $Log: Control.java,v $
 *  Revision 1.3  2009/10/14 18:40:56  dennis
 *  Updated to use new version of MessageTools from ISAW.
 *
 *  Revision 1.2  2006/10/31 03:50:33  dennis
 *  Now if the title is passed in null, a titled border will NOT
 *  be used.
 *
 *  Revision 1.1  2006/10/30 05:02:18  dennis
 *  Abstract base class for control objects that send messages
 *  via a message center.
 *
 */

package Controls;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import MessageTools.*;


/**
 *  This is the abstract base class for control objects that send messages
 *  to a message center, the the control value is changed.  By default, the 
 *  messages that are sent have the replace flag set true, so any previously 
 *  sent messages in that queue will be replaced by the new message.  This 
 *  default behavior can be changed using the setReplaceMode() method.
 *  The control is in a JPanel, with a titled border.
 */
public abstract class Control extends JPanel
{
  private MessageCenter message_center;  
  private Object        queue_name;
  private boolean       replace_flag;


  /* ---------------------------- constructor ---------------------------- */
  /**
   *  Construct a control object that will send messages to the specified
   *  queue on the specified message center. 
   *
   *  @param  message_center The message center where the messages are sent
   *  @param  queue_name     The name of the queue where the messages are sent
   *  @param  title          The title appearing on the border of the control.
   *                         If the title is null, no border will be used.
   */
  public Control( MessageCenter message_center,
                  Object        queue_name,
                  String        title        )
  {
    this.message_center = message_center;
    this.queue_name     = queue_name;

    replace_flag = true;

    if ( title != null )
      setBorder( new TitledBorder(title) );

    setLayout( new GridLayout(1,1) );
  }

  
  /* ---------------------------- getValue() ---------------------------- */
  /**
   *  Get the current value of this control 
   *
   *  @return  the current value of the control 
   */
  public abstract Object getValue();


  /* ------------------------- setReplaceMode ---------------------------- */
  /**
   *  Set the replace flag to true if the earlier messages sent to the 
   *  queue replace all previous messages.  Set the replace flag to false
   *  if previous values are to be kept in the message queue and processed.
   *  The default value for the replace flag is true.  
   *
   *  @param  replace_flag  Controls whether or not messages from this control 
   *                        replace previous messages in the message queue.
   */ 
  public void setReplaceMode( boolean replace_flag )
  {
    this.replace_flag = replace_flag;
  }


  /* ------------------------- getReplaceMode ---------------------------- */
  /**
   *  Get the current value of the replace flag.
   *
   *  @return The boolean value of the replace flag.
   */
  public boolean getReplaceMode()
  {
    return replace_flag;
  }


  /* --------------------------- sendMessage ----------------------------- */
  /**
   *  Trigger this control to send it's message using its current value.
   */
  public void sendMessage()
  {
     Message message = new Message( queue_name,
                                    getValue(),
                                    replace_flag,
                                    false );
     message_center.send( message );
  }


}

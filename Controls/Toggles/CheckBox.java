/*
 * File:  CheckBox.java
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
 *  $Log: CheckBox.java,v $
 *  Revision 1.3  2009/10/14 18:40:56  dennis
 *  Updated to use new version of MessageTools from ISAW.
 *
 *  Revision 1.2  2007/08/26 21:07:21  dennis
 *  Added serialVersionUID = 1
 *
 *  Revision 1.1  2006/10/31 03:53:31  dennis
 *  Initial version of CheckBox control.
 *
 */

package Controls.Toggles;

import java.awt.event.*;
import javax.swing.*;

import MessageTools.*;
import Controls.*;


/**
 *  A CheckBox sends messages containing a true or false value,
 *  whenever the selection is changed.  The message goes to a specified
 *  queue on a specified message center.  By default, the messages that are 
 *  sent have the replace flag set true, so any previously sent messages
 *  on that queue will be replaced by the new message.  This default behavior
 *  can be changed using the setReplaceMode() method.
 */
public class CheckBox extends Control 
{
  private JCheckBox check_box;
  private static final long serialVersionUID = 1;


  /* ---------------------------- constructor ---------------------------- */
  /**
   *  Construct a CheckBox object that will send messages to the specified
   *  queue on the specified message center.  The value of the message will
   *  be the Boolean value true or false, depending on the CheckBox state.
   *
   *  @param  message_center   The message center where the values are sent
   *  @param  queue_name       The name of the queue where the values are sent
   *  @param  title            The title appearing on the CheckBox 
   *  @param  initial_value    The intial value, true (i.e. selected) or 
   *                           false  (i.e. unselected) to use for the 
   *                           CheckBox.
   */
  public CheckBox( MessageCenter message_center,
                   Object        queue_name,
                   String        title,
                   boolean       initial_value )
  {
    super( message_center, queue_name, null );

    check_box = new JCheckBox( title, initial_value );

    check_box.addActionListener( new Listener() );

    add( check_box );
  }

  
  /* ---------------------------- getValue() ---------------------------- */
  /**
   *  Get the current selection state from the JCheckBox 
   *
   *  @return  Boolean value true, if the CheckBox is selected and false
   *           otherwise. 
   */
  public Object getValue()
  {
     return new Boolean( check_box.isSelected() );
  }


  /* ------------------------- ActionListener ---------------------------- */
  /**
   *  This class listens to the JCheckBox and sends messages to the message
   *  center, when the value is changed.
   */
  private class Listener implements ActionListener
  {
    public void actionPerformed( ActionEvent e )
    {
      sendMessage();
    }
  }


  /* ----------------------------- main ----------------------------------- */
  /**
   *  Main program for basic testing purposes.
   */
  public static void main( String args[] )
  {
    String QUEUE_NAME = "TestCheckBoxQueue";

    MessageCenter message_center = new MessageCenter("Test Center");
    TestReceiver  receiver = new TestReceiver( "Test Receiver" );

    message_center.addReceiver( receiver, QUEUE_NAME );

    CheckBox control = 
                 new CheckBox( message_center, QUEUE_NAME, "Set On/Off", true );

    control.sendMessage();
    message_center.dispatchMessages();

    new UpdateManager( message_center, null, 100 );

    JFrame frame = new JFrame( "Choice Test" );
    frame.setSize(200,87);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( control );
    frame.setVisible( true );

    new UpdateManager( message_center, null, 1000 );
  }

}

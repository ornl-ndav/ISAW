/*
 * File:  ComboBox.java
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
 *  $Log: ComboBox.java,v $
 *  Revision 1.5  2009/10/14 18:40:56  dennis
 *  Updated to use new version of MessageTools from ISAW.
 *
 *  Revision 1.4  2007/10/14 17:21:57  dennis
 *  Generalized to allow ComboBox to use a list of Objects instead
 *  of just a list of Strings.
 *
 *  Revision 1.3  2007/08/26 21:07:21  dennis
 *  Added serialVersionUID = 1
 *
 *  Revision 1.2  2007/08/25 04:12:27  dennis
 *  Removed redundant cast.
 *
 *  Revision 1.1  2006/10/30 05:11:00  dennis
 *  Initial version of control that sends messages to a message
 *  center, using the current value from a JComboBox.
 *
 */

package Controls.Choosers;

import java.awt.event.*;
import javax.swing.*;

import MessageTools.*;
import Controls.*;


/**
 *  A ComboBox sends messages containing the new value from a JComboBox,
 *  whenever the selection is changed.  The message goes to a specified
 *  queue on a specified message center.  By default, the messages that are 
 *  sent have the replace flag set true, so any previously sent messages
 *  on that queue will be replaced by the new message.  This default behavior
 *  can be changed using the setReplaceMode() method.
 */
public class ComboBox extends Control 
{
  private static final long serialVersionUID = 1;

  private JComboBox combo_box;


  /* ---------------------------- constructor ---------------------------- */
  /**
   *  Construct a ComboBox object that will send messages to the specified
   *  queue on the specified message center.  The value of the message will
   *  be the item that is currently selected in the ComboBox.
   *
   *  @param  message_center   The message center where the values are sent
   *  @param  queue_name       The name of the queue where the values are sent
   *  @param  new_title        The title appearing on the border of the control 
   *  @param  items            The list of choice items for the ComboBox
   *  @param  initial_choice   The index of the choice that will be used
   *                           as the initial value in the ComboBox
   */
  public ComboBox( MessageCenter message_center,
                   Object        queue_name,
                   String        new_title,
                   Object[]      items,
                   int           initial_choice )
  {
    super( message_center, queue_name, new_title );

    combo_box = new JComboBox( items );
    combo_box.setSelectedIndex( initial_choice );

    combo_box.addActionListener( new Listener() );

    add( combo_box );
  }

  
  /* ---------------------------- getValue() ---------------------------- */
  /**
   *  Get the current selection from the ComboBox 
   *
   *  @return  the currently selected String
   */
  public Object getValue()
  {
     return combo_box.getSelectedItem();
  }


  /* ------------------------- ActionListener ---------------------------- */
  /**
   *  This class listens to the JComboBox and sends messages to the message
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
    String QUEUE_NAME = "TestChoiceQueue";

    MessageCenter message_center = new MessageCenter("Test Center");
    TestReceiver  receiver = new TestReceiver( "Test Receiver" );

    message_center.addReceiver( receiver, QUEUE_NAME );

    String[] strings = { "Red", "Green", "Blue" };
    ComboBox control = 
                 new ComboBox( message_center, QUEUE_NAME, "Test Choice",
                               strings, 1 );

    control.sendMessage();
    message_center.dispatchMessages();

    new UpdateManager( message_center, null, 500 );

    JFrame frame = new JFrame( "Choice Test" );
    frame.setSize(200,87);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( control );
    frame.setVisible( true );

    new UpdateManager( message_center, null, 1000 );
  }

}

/*
 * File:  Slider.java
 *
 * Copyright (C) 2005 Dennis Mikkelson
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
 *  $Log: Slider.java,v $
 *  Revision 1.7  2009/10/14 18:40:56  dennis
 *  Updated to use new version of MessageTools from ISAW.
 *
 *  Revision 1.6  2007/08/26 21:07:21  dennis
 *  Added serialVersionUID = 1
 *
 *  Revision 1.5  2006/10/30 05:05:13  dennis
 *  Simplified by factoring out common methods and placing them
 *  in the abstract base class, Contorls.Control.
 *
 *  Revision 1.4  2006/10/24 04:33:51  dennis
 *  Added sendMessage() method to trigger sending the message based
 *  on the current value form the JSlider.
 *
 *  Revision 1.3  2006/07/20 19:59:01  dennis
 *  Replaced deprecated method frame.show() with setVisible(true)
 *
 *  Revision 1.2  2005/11/30 19:38:36  dennis
 *  Added method to set the replace flag.
 *  Added javadocs.
 */

package Controls.Valuators;

import javax.swing.*;
import javax.swing.event.*;

import Controls.*;
import MessageTools.*;


/**
 *  A Slider object sends messages containing the new value on a JSlider,
 *  whenever the JSlider value is changed.  The message goes to a specified
 *  queue on a specified message center.  By default, the messages that are 
 *  sent have the replace flag set true, so any previously sent messages
 *  on that queue will be replaced by the new message.  This default behavior
 *  can be changed using the setReplaceMode() method.
 */
public class Slider extends Control 
{
  private static final long serialVersionUID = 1;

  private float    min,
                   max,
                   initial;
  private int      num_steps;
  private JSlider  slider;


  /* ---------------------------- constructor ---------------------------- */
  /**
   *  Construct a slider object that will send messages to the specified
   *  queue on the specified message center.  The value of the message will
   *  be between the specified min and max values, and is determined by a 
   *  JSlider object.  
   *
   *  @param  message_center   The message center where the values are sent
   *  @param  queue_name       The name of the queue where the values are sent
   *  @param  title            The title appearing on the JSlider
   *  @param  new_min          The minumum value that will be sent
   *  @param  new_max          The maximum value that will be sent
   *  @param  new_initial      The initial value 
   *  @param  new_num_steps    The resolution to be used for the JSlider
   */
  public Slider( MessageCenter message_center,
                 Object        queue_name,
                 String        title,
                 float         new_min, 
                 float         new_max, 
                 float         new_initial,
                 int           new_num_steps )
  {
    super( message_center, queue_name, title );

    min       = new_min;
    max       = new_max;
    initial   = new_initial;
    num_steps = new_num_steps;

    if ( initial < min )
      initial = min;
    if ( initial > max )
      initial = max;

    slider = new JSlider();

    slider.setMinimum( 0 );
    slider.setMaximum( num_steps );
    slider.setValue( (int)( num_steps * (initial - min)/(max - min) ) );
    slider.addChangeListener( new Listener() );
    add( slider );
  }

  
  /* ---------------------------- getValue() ---------------------------- */
  /**
   *  Get the current value of the slider
   *
   *  @return  the current value of the slider
   */
  public Object getValue()
  {
     return new Float(min + (max-min) * slider.getValue()/num_steps);
  }


  /* ------------------------- ChangeListener ---------------------------- */
  /**
   *  This class listens to the JSlider and sends messages to the message
   *  center, when the value is changed.
   */
  private class Listener implements ChangeListener
  {
    public void stateChanged( ChangeEvent e )
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
    String        QUEUE_NAME = "TestSliderQueue";

    MessageCenter message_center = new MessageCenter("Test Center");
    TestReceiver  receiver = new TestReceiver( "Test Receiver" );
    message_center.addReceiver( receiver, QUEUE_NAME );

    Slider slider = 
                 new Slider( message_center, QUEUE_NAME, "Test Slider",
                             1, 5, 2, 100 );

    slider.sendMessage();
    message_center.dispatchMessages();

    new UpdateManager( message_center, null, 500 );

    JFrame frame = new JFrame( "Slider Test" );
    frame.setSize(200,87);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( slider );
    frame.setVisible( true );
  }

}

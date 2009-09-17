/* 
 * File: displayColorEditor.java
 *
 * Copyright (C) 2009, Paul Fischer
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0800276 and by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.ShowEventsApp.Controls;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import gov.anl.ipns.ViewTools.Components.ViewControls.ColorScaleControl.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

import MessageTools.*;

import EventTools.ShowEventsApp.Command.*;

/**
 * Contains ColorEditPanel and handles sending and receiving
 * of messages as well as creation of the ColorEditPanel.
 */
public class displayColorEditor implements IReceiveMessage
{
  private JFrame          cEditPanel;
  private ColorEditPanel  colorEditPanel;
  private Rectangle       bounds          = new Rectangle(100,100,400,500);
  private MessageCenter   message_center;
  private MessageCenter   view_message_center;
  private String          command;
  private Object          ColorEdPanMonitor = new Object();
  
  /**
   * Creates colorEditPanel as well as stores the message center and 
   * the command name to be used when sending messages.
   * 
   * @param inMessage_Center Message Center to be used.
   * @param inCommand Command name to be used to send messages.
   * @param min The minimum data value which is used on the 
   *        ColorEditPanel to build the color image and color table.
   * @param max The maximum data value which is used on the 
   *        ColorEditPanel to build the color image and color table.
   * @param addListener The option whether or not to add an 
   *        actionListener to the ColorEditPanel
   */
  public displayColorEditor(MessageCenter inMessage_Center,
                  MessageCenter view_message_center,
                  String inCommand, int min, int max, 
                  boolean addListener)
  {
    message_center = inMessage_Center;
    this.view_message_center = view_message_center;
    command = inCommand;
    
    colorEditPanel = new ColorEditPanel(min, max, false, false);
    
    byte[] table = new byte[256];
    for(int i = 0; i < table.length-2; i++)
       table[i] = (byte)(i/2);
    
    colorEditPanel.
       setControlValue(new ColorScaleInfo(min, max, 1, "Heat 1", 
                                        true, 127, table, true));
    if (addListener)
       colorEditPanel.addActionListener( new ColorListener() );

    view_message_center.addReceiver( this, Commands.SET_HISTOGRAM_MAX );
  }

  /**
   * Listens for SET_HISTOGRAM_MAX message and when received
   * sets the new min and max values on the ColorEditPanel
   */
  public boolean receive( Message message )
  {
   // System.out.println("***displayColorEditor in thread "
   //                    + Thread.currentThread());

    if ( message.getName().equals(Commands.SET_HISTOGRAM_MAX) )
    {
      Object obj = message.getValue();

      if ( obj == null || ! ( obj instanceof Float ) )
        return false;

      Float max = (Float)obj;
      if ( max <= 0 )
        return false;

      float min = max/50;
      if ( min < 1 ) 
        min = 1;

       if ( max < min + 9 )
         max = min + 9;
       
       DecimalFormat dc = new DecimalFormat("############.#");
       min = Float.parseFloat(dc.format(min));
       max = Float.parseFloat(dc.format(max));

      //System.out.println("Setting Color range to " + min + " to " + max );
       synchronized(ColorEdPanMonitor)
       {
      colorEditPanel.setControlValue( 0.0001f, colorEditPanel.MINSET ); 
      colorEditPanel.setControlValue( max, colorEditPanel.MAXSET ); 
      colorEditPanel.setControlValue( min, colorEditPanel.MINSET ); 
       }
      sendMessage(Commands.SET_COLOR_SCALE, getColorScaleInfo()); 
    }

    return false;

  }

  /*private displayColorEditor(MessageCenter inMessage_Center,
      String inCommand)
  {
    message_center = inMessage_Center;
    command = inCommand;
  }

  public float getDataMin()
  {
     return colorEditPanel.getMin();
  }
  
  public float getDataMax()
  {
     return colorEditPanel.getMax();
  }*/
    
  /**
   * Gets the ColorScaleInfo object from the ColorEditPanel
   * and returns it.
   * 
   * @return ColorScaleInfo object.
   */
  private ColorScaleInfo getColorScaleInfo()
  {
     return (ColorScaleInfo)colorEditPanel.getControlValue();
  }   
  
  /**
   * Returns the colorEditPanel.
   * 
   * @return JPanel
   */
  public JPanel getColorPanel()
  {
     return colorEditPanel;
  }
  
  /*public void setColorScale(float min, float max)
  {
     byte[] table = new byte[256];
      for(int i = 0; i < table.length-2; i++)
         table[i] = (byte)(i/2);
      
     colorEditPanel.
      setControlValue(new ColorScaleInfo(Math.round(min), Math.round(max),
                                       1, "Heat 1", true, 127, table, true));
  }*/
  
  /**
   * Listens to the ColorEditPanel and sends out a message with 
   * a command name that is passed into displayColorEditor and a value
   * or ColorScaleInfo.
   */
  private class ColorListener implements ActionListener
  {
    public void actionPerformed (ActionEvent ae)
    {      
      /*if( ae.getActionCommand().equals(ColorEditPanel.cancelMessage))
      {
         if (cEditPanel != null)
            cEditPanel.dispose();
      }
      
      if( ae.getActionCommand().equals(ColorEditPanel.doneMessage))
      {
        sendMessage(command, getColorScaleInfo());
        if (cEditPanel != null)
           cEditPanel.dispose();
      }*/
      
      if (ae.getActionCommand().equals(ColorEditPanel.updateMessage))
      {
        sendMessage(command, getColorScaleInfo());
      }
    }
  }
  
  /**
   * Sends a message to the message center
   * 
   * @param command Command Name for others to listen to.
   * @param value Object to send to the listener.
   */
  private void sendMessage(String command, Object value)
  {
     Message message = new Message( command,
                                    value,
                                    true ,true);
     
     message_center.send( message );
  }
  
  /**
   * Mainly for testing purposes.
   * Creates a JFrame to put the ColorEditPanel onto
   * so that the class can be tested.
   */
  public void createColorEditor()
  {
    cEditPanel = new JFrame("Color Editor");
    cEditPanel.setBounds(bounds);
    cEditPanel.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    colorEditPanel.addActionListener( new ColorListener() );
    cEditPanel.getContentPane().add(colorEditPanel);
    cEditPanel.setVisible(true);
  }
  
  public static void main(String[] args) 
  {
    MessageCenter messageC = new MessageCenter("ColorEditor Center");
    MessageCenter messageV= new MessageCenter("SlowColorEditor Center");
    TestReceiver tr = new TestReceiver("Test ColorEditor");
    messageC.addReceiver(tr, Commands.SET_COLOR_SCALE);
    
    displayColorEditor display = new displayColorEditor(messageC, messageV,
                    Commands.SET_COLOR_SCALE, 15, 1000, false);
    display.createColorEditor();
    
    new UpdateManager(messageC, null, 100);
  }

}

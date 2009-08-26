/* 
 * File: drawingOptions.java
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

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

/**
 * Creates a panel giving the user options for 
 * how things are drawn on the screen such as
 * whether or not to draw points above the max data
 * value and below the min data value.
 * Also wether to show the axis on the screen,
 * use an alpha value for the points, the point
 * size and to view in orthographic or not.
 */
public class drawingOptions extends JPanel
{
   public static final long serialVersionUID = 1L;
   private MessageCenter    messageCenter;
   private JCheckBox        filterAbove;
   private JCheckBox        filterBelow;
   private JCheckBox        showAxis;
   private JCheckBox        useAlpha;
   private JTextField       pointSize;
   private JTextField       alphaValue;
   private JCheckBox        orthographic;
   
   /**
    * Builds the panel as well as stores the message center.
    * 
    * @param messageCenter
    */
   public drawingOptions(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      this.setBorder(new TitledBorder("Display Options"));
      this.setLayout(new GridLayout(7,1));
      
      this.add(buildOrtho());
      this.add(buildMin());
      this.add(buildMax());
      this.add( buildAxes());
      this.add(buildPointSize());
      this.add(builduseAlpha());
      
      JButton apply = new JButton("Apply");
      apply.addActionListener(new displayListener());
      
      this.add(apply);
   }
   
   /**
    * Builds the option panel for orthographic view.
    * 
    * @return JPanel
    */
   private JPanel buildOrtho()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,1));
      
      orthographic = new JCheckBox("Orthographic View");
      
      panel.add(orthographic);

      return panel;
   }
   
   /**
    * Builds the option panel for filtering
    * above the max data value.
    * 
    * @return JPanel
    */
   private JPanel buildMax()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,1));
      
      filterAbove = new JCheckBox("Filter Above Max");
      
      panel.add(filterAbove);
      
      return panel;
   }
   
   /**
    * Builds the option panel for filtering
    * below the min data value.
    * 
    * @return JPanel
    */
   private JPanel buildMin()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,1));

      filterBelow = new JCheckBox("Filter Below Min");
      filterBelow.setSelected(true);
      
      panel.add(filterBelow);
      
      return panel;
   }
   
   /**
    * Builds the option panel for showing the axis.
    * 
    * @return JPanel
    */
   private JPanel buildAxes()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,1));

      showAxis = new JCheckBox("Show Axis");
      showAxis.setSelected(true);
      
      panel.add(showAxis);
      
      return panel;
   }
   
   /**
    * Builds the option panel for the size of the 
    * points to be drawn.
    * 
    * @return JPanel
    */
   private JPanel buildPointSize()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,1));

      JLabel label = new JLabel("Point Size");
      String defaultSize = "3";
      pointSize = new JTextField(defaultSize);
      pointSize.setHorizontalAlignment(JTextField.RIGHT);

      panel.add(label);
      panel.add(pointSize);
      
      return panel;
   }
   
   /**
    * Builds the option panel for using an alpha value
    * by entering a number from 0.0 - 1.0.
    * 
    * @return JPanel
    */
   private JPanel builduseAlpha()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));

      useAlpha = new JCheckBox("Use Alpha");
      alphaValue = new JTextField();
      alphaValue.setHorizontalAlignment(JTextField.RIGHT);
      alphaValue.setText("1.0");
      
      panel.add(useAlpha);
      panel.add(alphaValue);
      
      return panel;
   }
   
   /**
    * Sends a message to the message center
    * 
    * @param command Command Name for others to listen to.
    * @param value Object to send to the listener.
    */
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command, value, true);
      
      messageCenter.receive(message);
   }
   
   /**
    * Checks to make sure the values for point size
    * and alpha value are entered correctly.
    * 
    * @return true if everything is valid otherwise false.
    */
   private boolean valid()
   {
      if(pointSize.getText().equals(""))
      {
         String error = "You have not specified the Point size!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         Float.parseFloat(pointSize.getText());
      }
      catch (NumberFormatException nfe)
      {
         String error = "Point size must be a float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      if(alphaValue.getText().equals(""))
      {
         String error = "You have not specified the alpha value!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      float num;
      
      try
      {
         num = Float.parseFloat(alphaValue.getText());
      }
      catch (NumberFormatException nfe)
      {
         String error = "Alpha value must be a float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      if (num > 1.0f || num < 0.0f)
      {
         String error = "Alpha value must be between 0.0 and 1.0!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      return true;
   }
   
   /**
    * Listens to the apply button and then checks to make 
    * sure all information entered is correct and then sends
    * a message of SET_DRAWING_OPTIONS and of type
    * DrawingOptionsCmd.
    */
   private class displayListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         if (valid())
         {
            DrawingOptionsCmd filters = 
               new DrawingOptionsCmd(orthographic.isSelected(),
                           filterBelow.isSelected(),
                           filterAbove.isSelected(),
                           showAxis.isSelected(),
                           Float.parseFloat(pointSize.getText()),
                           useAlpha.isSelected(),
                           Float.parseFloat(alphaValue.getText()));
            
            sendMessage(Commands.SET_DRAWING_OPTIONS, filters);
         }
      }
   }

   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Testing MessageCenter");
      TestReceiver tc = new TestReceiver("FilePanel TestingMessages");
      mc.addReceiver(tc, Commands.SET_DRAWING_OPTIONS);
      
      drawingOptions draw = new drawingOptions(mc);

      JFrame View = new JFrame( "Test File Panel" );
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setBounds(10,10, 300, 275);
      View.setVisible(true);
      
      View.add(draw);
     
      new UpdateManager(mc, null, 100);
   }
}

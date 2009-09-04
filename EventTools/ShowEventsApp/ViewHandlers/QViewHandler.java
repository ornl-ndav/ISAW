/* 
 * File: QViewHandler.java
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
 *  $Author: fischerp $
 *  $Date: 2009-08-25 11:02:13 -0500 (Tue, 25 Aug 2009) $            
 *  $Revision: 19931 $
 */

package EventTools.ShowEventsApp.ViewHandlers;

import java.awt.GridLayout;

import javax.swing.*;

import EventTools.ShowEventsApp.Command.Commands;
import gov.anl.ipns.ViewTools.Components.OneD.FunctionViewComponent;
import MessageTools.*;

/**
 * Builds and Displays a graph of q. Updates
 * automatically when data is loaded and displayed on the screen.
 */
public class QViewHandler implements IReceiveMessage
{
   private MessageCenter messageCenter;
   private JFrame        qDisplayFrame;
   private JPanel        graphPanel;
   private String        Title = "Magnitude Q";
   private String        x_units = "Inv(" + '\u00c5' + ")";
   private String        y_units = "weighted";
   private String        x_label = "Q";
   private String        y_label = "Intensity";
   
   /**
    * Sets the message center for the DViewHandler but does
    * not display or create anything else.  The class relies mainly
    * on the message center.  It will display the jframe when it
    * receives SHOW_Q_GRAPH message.
    * 
    * @param messageCenter
    */
   public QViewHandler(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      this.messageCenter.addReceiver(this, Commands.SHOW_Q_GRAPH);
      this.messageCenter.addReceiver(this, Commands.HIDE_Q_GRAPH);
      this.messageCenter.addReceiver(this, Commands.SET_Q_VALUES);
   }
   
   /**
    * Creates a new JFrame to display the graph every time
    * it is called.  Will display a graph if its been built
    * or will display a placeholder saying no data loaded.
    */
   private void displayQFrame()
   {
      qDisplayFrame = new JFrame("Q View");
      qDisplayFrame.setLayout(new GridLayout(1,1));
      qDisplayFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      qDisplayFrame.setBounds(0, 0, 1000, 300);
      qDisplayFrame.setVisible(true);
      
      if (graphPanel != null)
         qDisplayFrame.add(graphPanel);
      else
        qDisplayFrame.add(placeholderPanel());
      
      qDisplayFrame.repaint();
   }
   
   /**
    * Placeholder to put in the frame if no data is loaded.
    * 
    * @return Panel
    */
   private JPanel placeholderPanel()
   {
      JPanel placeholderpanel = new JPanel();
      placeholderpanel.setLayout(new GridLayout(1,1));
      
      JLabel label = new JLabel("No Data Loaded!");
      label.setHorizontalAlignment(JLabel.CENTER);
      
      placeholderpanel.add(label);
      
      return placeholderpanel;
   }
   
   /**
    * Takes the data and creates an instance of
    * FunctionViewComponent and adds it to the graphPanel
    * and then to the frame if the frame has been created.
    * This allows for the graph to be updated while the frame 
    * is displayed.
    * 
    * @param xyValues X,Y values of the data for the graph.
    */
   private void setPanelInformation(float[][] xyValues)
   {
      float[] x_values = xyValues[0];
      float[] y_values = xyValues[1];
      float[] errors = null;

      if(qDisplayFrame != null)
         qDisplayFrame.getContentPane().removeAll();  
      
      graphPanel = 
         FunctionViewComponent.ShowGraphWithAxes(x_values, y_values, errors, 
               Title, x_units, y_units, x_label, y_label);
      
      if(qDisplayFrame != null)
         qDisplayFrame.add(graphPanel);
   }
   
   /**
    * Send a message to the messagecenter
    * 
    * @param command
    * @param value
    */
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command, value, true);
      
      messageCenter.receive(message);
   }

   /**
    * Receive messages to display the frame, hide the frame,
    * get the xy values, and set the values/create the graph.
    */
   public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.SHOW_Q_GRAPH))
      {
         displayQFrame();
         
         return true;
      }
      
      if (message.getName().equals(Commands.HIDE_Q_GRAPH))
      {
         qDisplayFrame.dispose();
         
         return true;
      }
      
      if (message.getName().equals(Commands.SET_Q_VALUES))
      {
         setPanelInformation(((float[][])message.getValue()));
         
         if(qDisplayFrame != null)
            qDisplayFrame.validate();
         
         return true;
      }
      return false;
   }
}

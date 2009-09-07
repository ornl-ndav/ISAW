/* 
 * File: sliceControl.java
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
package EventTools.ShowEventsApp.Controls.HistogramControls;

import java.awt.event.*;
import java.awt.GridLayout;
import javax.swing.*;

import gov.anl.ipns.MathTools.Geometry.SlicePlane3D;
import gov.anl.ipns.ViewTools.UI.*;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

/**
 * Slice control panel that creates a
 * SliceSelectorSplitPaneUI to be used to set the 
 * orientation of the Histogram which will display the slice
 * in the correct way.
 */
public class sliceControl extends JPanel
                          implements IReceiveMessage
{
   private static final long  serialVersionUID  = 1L;
   private SliceSelectorSplitPaneUI scUI;
   private MessageCenter   message_center;
   
   /**
    * Constructor that creates the SliceSelectorSplitPaneUI
    * and adds it to itself.
    * 
    * @param mc MessageCenter to be used.
    */
   public sliceControl(MessageCenter mc)
   {
      message_center = mc;
      message_center.addReceiver(this, Commands.SET_ORIENTATION_MATRIX);
      
      scUI = new SliceSelectorSplitPaneUI(ISlicePlaneSelector.QXYZ_MODE);
      scUI.addActionListener(new Action());
      this.setLayout(new GridLayout(1,1));
      this.add(scUI);
   }
   
   /**
    * Set the plane on the SliceSelectorSplitPaneUI
    * 
    * @param plane The plane to set the control to.
    */
   public void setPlane(SlicePlane3D plane)
   {
      scUI.setPlane(plane);
   }
   
   /*public void showSliceControl()
   {
      sliceDisplay.setVisible(true);
   }*/
   
   /**
    * Send a message to the message center.
    */
   private void sendMessage(String command, Object value)
   {
      Message message = new Message( command,
                                     value,
                                     true );
      
      message_center.send( message );
   }
   
   /**
    * ActionListener that listens to SliceSelectorSplitPaneUI
    * and sends a message of tyep SlicePlaneInformationCmd.
    */
   private class Action implements ActionListener
   {
      public void actionPerformed(ActionEvent arg0)
      {
         String command = arg0.getActionCommand();
         
         SlicePlaneInformationCmd spi = 
            new SlicePlaneInformationCmd(scUI.getSliceMode(),
                                      scUI.getPlane(),
                                      scUI.getPlane().getNormal(),
                                      scUI.getStepSize(),
                                      scUI.getSliceWidth(),
                                      scUI.getSliceHeight(),
                                      scUI.getSliceThickness());

         sendMessage(command, spi);
      }
   }
   
   /**
    * Waiting for SET_ORIENTATION_MATRIX message so that it can
    * update the SliceSelectorSplitPaneUI with the correct information.
    */
   public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.SET_ORIENTATION_MATRIX))
      {
         
         return true;
      }
      return false;
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("sliceControl MessageCenter");
      TestReceiver tr = new TestReceiver("Test");

      mc.addReceiver(tr, Commands.SET_ORIENTATION_MATRIX);
      
      sliceControl sc = new sliceControl(mc);

      JFrame View = new JFrame("Slice Control");
      View.setBounds(625,210,225,425);
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setVisible(true);
      
      View.add(sc);
    
      new UpdateManager( mc, null, 1000 );
   }
}

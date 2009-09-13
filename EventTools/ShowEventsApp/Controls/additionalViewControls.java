/* 
 * File: additionalViewControls.java
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
 *  $Date: 2009-08-25 11:05:06 -0500 (Tue, 25 Aug 2009) $            
 *  $Revision: 19932 $
 */

package EventTools.ShowEventsApp.Controls;

import java.awt.GridLayout;
import java.awt.event.*;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.DisplaySliceCmd;
import EventTools.ShowEventsApp.Command.DisplaySliceCmd.moveSlice;
import EventTools.ShowEventsApp.Controls.HistogramControls.FrameController;
import MessageTools.*;

/**
 * Builds the panel to control and display other views such as the
 * Q and d-spacing graphs and the slice controls.
 */
public class additionalViewControls extends JPanel
{
   public static final long  serialVersionUID = 1L;
   private MessageCenter     messageCenter;
   private MessageCenter     viewMessageCenter;
   private FrameController   frame_control;
   private JCheckBox         showQGraph;
   private JCheckBox         showDGraph;
   private JCheckBox         showImageOne;
   private JCheckBox         showImageTwo;
   private JCheckBox         showImageThree;
   private JCheckBox         sliceOneCbx;
   private JCheckBox         sliceTwoCbx;
   private JCheckBox         sliceThreeCbx;
   private JRadioButton      moveSliceOne;
   private JRadioButton      moveSliceTwo;
   private JRadioButton      moveSliceThree;
   
   /**
    * Builds the panel and adds the items to itself
    * as well as stores the message center.
    * 
    * @param messageCenter
    */
   public additionalViewControls( MessageCenter messageCenter,
                                  MessageCenter viewMessageCenter )
   {
      this.messageCenter     = messageCenter;
      this.viewMessageCenter = viewMessageCenter;
      this.setBorder(new TitledBorder("Additional View Options"));
      this.setLayout(new GridLayout(1,1));
      
      Box box = new Box( BoxLayout.Y_AXIS );
      box.add(buildGraphOptions());
      box.add(buildSliceOptions());
      
      this.add(box);
   }
   
   /**
    * Build two checkboxs to display Q graph or d-spacing graph.
    * 
    * @return JPanel
    */
   private JPanel buildGraphOptions()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));
      panel.setBorder(new TitledBorder("Graph Views"));
      
      showQGraph = new JCheckBox("Show Q Graph");
      showQGraph.addActionListener(new graphListener());
      showDGraph = new JCheckBox("Show D Graph");
      showDGraph.addActionListener(new graphListener());
      
      panel.add(showQGraph);
      panel.add(showDGraph);
      
      return panel;
   }
   
   /**
    * Creates a box container with the slice options.
    * 
    * @return Box containing the slice display options
    *       as well as the slice move options.
    */
   private Box buildSliceOptions()
   {
      Box box = new Box( BoxLayout.Y_AXIS );
      box.setBorder(new TitledBorder("Slice Views/Controls"));
      box.add(buildSliceDisplayOptions());
      box.add(buildSliceMoveOptions());
      
      return box;
   }
   
   /**
    * Creates the options for showing the different slices and
    * also their respective images.
    * 
    * @return JPanel
    */
   private JPanel buildSliceDisplayOptions()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(2,3));
      
      showImageOne = new JCheckBox("Show Image X");
      showImageOne.addActionListener(new sliceListener());
      
      showImageTwo = new JCheckBox("Show Image Y");
      showImageTwo.addActionListener(new sliceListener());
      
      showImageThree = new JCheckBox("Show Image Z");
      showImageThree.addActionListener(new sliceListener());
      
      sliceOneCbx = new JCheckBox("Show 3D Slice X");
      sliceOneCbx.addActionListener(new sliceListener());
      
      sliceTwoCbx = new JCheckBox("Show 3D Slice Y");
      sliceTwoCbx.addActionListener(new sliceListener());
      
      sliceThreeCbx = new JCheckBox("Show 3D Slice Z");
      sliceThreeCbx.addActionListener(new sliceListener());

      panel.add(showImageOne);
      panel.add(showImageTwo);
      panel.add(showImageThree);
      
      panel.add(sliceOneCbx);
      panel.add(sliceTwoCbx);
      panel.add(sliceThreeCbx);
      
      return panel;
   }
   
   /**
    * Creates the option to select which slice the
    * frame controller will actually move when the user
    * selects to increment/decrement the slice.
    * 
    * @return JPanel
    */
   private JPanel buildSliceMoveOptions()
   {
      JPanel outerPanel = new JPanel();
      outerPanel.setLayout(new GridLayout(1,1));
      outerPanel.setBorder(new TitledBorder("Slice Shift"));
      
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,3));
      ButtonGroup group = new ButtonGroup();

      moveSliceOne = new JRadioButton("Move X");
      moveSliceTwo = new JRadioButton("Move Y");
      moveSliceThree = new JRadioButton("Move Z");
      
      group.add(moveSliceOne);
      group.add(moveSliceTwo);
      group.add(moveSliceThree);
      
      panel.add(moveSliceOne);
      panel.add(moveSliceTwo);
      panel.add(moveSliceThree);
      
      frame_control  = new FrameController();
      frame_control.addActionListener(new sliceListener());
      
      Box box = new Box( BoxLayout.Y_AXIS );
      box.add(panel);
      box.add(frame_control);

      outerPanel.add(box);
      
      return outerPanel;
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
      
      messageCenter.send(message);
   }
   

   /**
    * Sends a message to the view message center
    * 
    * @param command Command Name for others to listen to.
    * @param value Object to send to the listener.
    */
   private void sendViewMessage(String command, Object value)
   {
      Message message = new Message(command, value, true);

      viewMessageCenter.send(message);
   }


   /**
    * Listens to the slice display selection checkboxs as well
    * as to the frame controller.  When they change it
    * sends a message of SET_SLICE_1 and of type
    * DisplaySliceCmd.
    */
   private class sliceListener implements ActionListener
   {
      public void actionPerformed(ActionEvent ae)
      {
         moveSlice move = null;
         
         if (moveSliceOne.isSelected())
            move = moveSlice.X;
         
         if (moveSliceTwo.isSelected())
            move = moveSlice.Y;
         
         if (moveSliceThree.isSelected())
            move = moveSlice.Z;
         
         DisplaySliceCmd displaySlice = 
            new DisplaySliceCmd(showImageOne.isSelected(),
                                showImageTwo.isSelected(),
                                showImageThree.isSelected(),
                                sliceOneCbx.isSelected(),
                                sliceTwoCbx.isSelected(),
                                sliceThreeCbx.isSelected(),
                                move, frame_control.getFrameNumber());
         
         sendMessage(Commands.SET_SLICE_1, displaySlice);
      }
   }
   
   /**
    * Listens to the two options for Q/d-spacing graphs
    * and sends a message of either SHOW_Q/D_GRAPH or 
    * HIDE_Q/D_GRAPH.
    */
   private class graphListener implements ActionListener
   {
      public void actionPerformed(ActionEvent ae)
      {
         String command = null;
         JCheckBox tmpBox = ((JCheckBox)(ae.getSource()));
         
         if (tmpBox.equals(showQGraph))
         {
            if (tmpBox.isSelected())
               command = Commands.SHOW_Q_GRAPH;
            else
               command = Commands.HIDE_Q_GRAPH;
         }
            
         if (tmpBox.equals(showDGraph))
         {
            if (tmpBox.isSelected())
               command = Commands.SHOW_D_GRAPH;
            else
               command = Commands.HIDE_D_GRAPH;
         }
         
         sendViewMessage(command, null);
      }
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc  = new MessageCenter("Testing MessageCenter");
      MessageCenter vmc = new MessageCenter("Testing ViewMessageCenter");
      TestReceiver tc = new TestReceiver("Slice Panel TestingMessages");
      mc.addReceiver(tc, Commands.SET_SLICE_1);
      mc.addReceiver(tc, Commands.SHOW_D_GRAPH);
      mc.addReceiver(tc, Commands.SHOW_Q_GRAPH);
      vmc.addReceiver(tc, Commands.HIDE_D_GRAPH);
      vmc.addReceiver(tc, Commands.HIDE_Q_GRAPH);
      
      additionalViewControls sdc = new additionalViewControls(mc, vmc);
      
      JFrame View = new JFrame( "Test Slice Panel" );
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setBounds(10,10, 400, 375);
      View.setVisible(true);
      
      View.add(sdc);
      new UpdateManager(mc, null, 100);
      new UpdateManager(vmc, null, 100);
   }
}

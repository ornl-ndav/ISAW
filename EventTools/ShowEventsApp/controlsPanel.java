/* 
 * File: controlsPanel.java
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

package EventTools.ShowEventsApp;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.Color;
import java.awt.event.*;
import java.awt.GridLayout;

import MessageTools.*;

import EventTools.ShowEventsApp.Command.*;
import EventTools.ShowEventsApp.Controls.*;
import EventTools.ShowEventsApp.Controls.Peaks.*;
import EventTools.ShowEventsApp.Controls.HistogramControls.*;

/**
 * This class creates a pre determined set of buttons for a
 * control panel as well as the actual JPanel.  When the 
 * button is pressed, it sends a message of CHANGE_PANEL
 * along with the appropriate JPanel.
 */
public class controlsPanel extends JPanel
{
   public static final long       serialVersionUID = 1L;
   private MessageCenter          messageCenter;
   private MessageCenter          viewMessageCenter;
   
   private JButton                loadFileBtn;
   private JButton                findPeaksBtn;
   private JButton                filterPeaksBtn;
   private JButton                indexPeaksBtn;
   private JButton                integrateBtn;
   
   private JButton                selectedPoint;
   private JButton                orientationBtn;
   private JButton                peakInfoBtn;
   private JButton                colorScaleBtn;
   private JButton                planeBtn;
   private JButton                additionalViewsBtn;
   private JButton                drawOptions;
   
   private filePanel              filepanel;
   private displayColorEditor     colorEditPanel;
   private peakOptionsPanel       peakPanel;
   private indexPeaksPanel        indexPeakPanel;
   private positionInfoPanel      positionPanel;
   private peaksStatPanel         peakInfoPanel;
   private sliceControl           slicePanel;
   private additionalViewControls sliceControlsPanel;
   private drawingOptions         drawoptions;
   
// private final Color background_color = new Color( 221, 232, 243 );
   private final Color background_color = new Color( 187, 209, 230 );

   /**
    * Builds the side panel in the splitpane for all the controls.
    * Also builds the corresponding panels that are passed in the 
    * message system to the other panel to be displayed.
    * 
    * @param messageCenter
    */
   public controlsPanel( MessageCenter messageCenter,
                         MessageCenter viewMessageCenter )
   {
      this.messageCenter     = messageCenter;
      this.viewMessageCenter = viewMessageCenter;
      buildPanels();
      
      this.setLayout(new GridLayout(2,1));
      this.add(buildOperationsPanel());
      this.add(buildControlsPanel());
   }
   
   /**
    * Builds all the panels to be displayed on the display side
    * of the splitpane.  This class uses these panels to pass
    * along with the message system where they displayPanel
    * class then just displays the panel it was sent.
    */
   private void buildPanels()
   {
      filepanel = new filePanel(messageCenter);
      peakPanel = new peakOptionsPanel(messageCenter);
      indexPeakPanel = new indexPeaksPanel(messageCenter);
      positionPanel = new positionInfoPanel(messageCenter);
      peakInfoPanel = new peaksStatPanel( messageCenter);
     
      slicePanel = new sliceControl(messageCenter);
      sliceControlsPanel = new additionalViewControls( messageCenter,
                                                       viewMessageCenter );
      drawoptions = new drawingOptions(messageCenter);
      
      //new DViewHandler( messageCenter );
      //new QViewHandler( messageCenter );
      
      colorEditPanel = new displayColorEditor(messageCenter,viewMessageCenter,
            Commands.SET_COLOR_SCALE, 15, 1000, true);
   }
   
   /**
    * Builds the first set of buttons which are all operations
    * to be done such as load an event file, find peaks, etc.
    * 
    * @return Panel containing a button list of operations.
    */
   private JPanel buildOperationsPanel()
   {
      JPanel panel = new JPanel();
      panel.setBorder(new TitledBorder("Operations"));
      panel.setLayout(new GridLayout(5,1));
      
      loadFileBtn = new JButton("Load Data");
      loadFileBtn.setBackground( background_color );
      loadFileBtn.addActionListener(new buttonListener());
      
      findPeaksBtn = new JButton("Find Peaks");
      findPeaksBtn.setBackground( background_color );
      findPeaksBtn.addActionListener(new buttonListener());
      
      filterPeaksBtn = new JButton("Filter Peaks");
      filterPeaksBtn.setBackground( background_color );
      filterPeaksBtn.addActionListener(new buttonListener());
      filterPeaksBtn.setEnabled(false);
      
      indexPeaksBtn = new JButton("Index Peaks");
      indexPeaksBtn.setBackground( background_color );
      indexPeaksBtn.addActionListener(new buttonListener());
      
      integrateBtn = new JButton("Integrate");
      integrateBtn.setBackground( background_color );
      integrateBtn.addActionListener(new buttonListener());
      integrateBtn.setEnabled(false);
      
      panel.add(loadFileBtn);
      panel.add(findPeaksBtn);
      panel.add(filterPeaksBtn);
      panel.add(indexPeaksBtn);
      panel.add(integrateBtn);
      
      return panel;
   }
   
   /**
    * Builds the second set of buttons which are all information
    * buttons or controls such as color of data or drawing options.
    * 
    * @return Panel containing a button list of controls/info.
    */
   private JPanel buildControlsPanel()
   {
      int npanels =6;//Change when add or delete a button
      JPanel panel = new JPanel();
      panel.setBorder(new TitledBorder("Controls/Info"));
      panel.setLayout(new GridLayout(npanels,1));
      
      selectedPoint = new JButton("Selected Point");
      selectedPoint.setBackground( background_color );
      selectedPoint.addActionListener(new buttonListener());
      
      orientationBtn = new JButton("Orientation Info.");
      orientationBtn.setBackground( background_color );
      orientationBtn.addActionListener(new buttonListener());
      
      peakInfoBtn = new JButton("Peaks Info.");
      peakInfoBtn.setBackground( background_color );
      peakInfoBtn.addActionListener(new buttonListener());
      
      colorScaleBtn = new JButton("Color Scale");
      colorScaleBtn.setBackground( background_color );
      colorScaleBtn.addActionListener(new buttonListener());
      
      planeBtn = new JButton("Histogram Orientation");
      planeBtn.setBackground( background_color );
      planeBtn.addActionListener(new buttonListener());
      
      additionalViewsBtn = new JButton("Additional Views");
      additionalViewsBtn.setBackground( background_color );
      additionalViewsBtn.addActionListener(new buttonListener());
      
      drawOptions = new JButton("Draw Options");
      drawOptions.setBackground(background_color);
      drawOptions.addActionListener(new buttonListener());
      
      panel.add(selectedPoint);
     // panel.add(orientationBtn);
      panel.add( peakInfoBtn );
      panel.add(colorScaleBtn);
      panel.add(planeBtn);
      panel.add(additionalViewsBtn);
      panel.add(drawOptions);
      
      return panel;
   }
   
   /**
    * Listens to all of the buttons and sends a message of
    * CHANGE_PANEL along with the corresponding panel.
    * 
    * @author fischerp
    */
   private class buttonListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         JPanel value = null;
         
         if (e.getSource().equals(loadFileBtn))
            value = filepanel.getPanel();
         
         if (e.getSource().equals(findPeaksBtn))
            value = peakPanel;
         
         if (e.getSource().equals(filterPeaksBtn))
            value = notImplementedPanel();
         
         if (e.getSource().equals(indexPeaksBtn))
            value = indexPeakPanel;
         
         if (e.getSource().equals(integrateBtn))
         {   
            value = notImplementedPanel();
            //Test();
         }
         
         if (e.getSource().equals(selectedPoint))
            value = positionPanel;

         if (e.getSource().equals(orientationBtn))
            value = notImplementedPanel();

         if (e.getSource().equals(peakInfoBtn))
            value = peakInfoPanel;
         
         if (e.getSource().equals(additionalViewsBtn))
            value = sliceControlsPanel;
         
         if (e.getSource().equals(colorScaleBtn))
            value = colorEditPanel.getColorPanel();
         
         if (e.getSource().equals(planeBtn))
            value = slicePanel;
         
         if (e.getSource().equals(drawOptions))
            value = drawoptions;
         
         sendMessage(Commands.CHANGE_PANEL, value);
      }
   }
   
   //Creates message to test new modules
   /*private void Test()
   {
      String filename = "C:\\ISAW\\SampleRuns\\SNS\\Snap\\QuartzRunsFixed\\quartz.peaks";
      Vector< Peak_new > peaks = null;
      try
      {
         peaks = Peak_new_IO.ReadPeaks_new( filename );
      }
      catch( Exception s )
      {
        return;
      }
      MessageCenter msgC = messageCenter;
    
      Message mmm = new Message( Commands.SET_PEAK_NEW_LIST , peaks , false );
      msgC.receive( mmm );
      msgC.receive( MessageCenter.PROCESS_MESSAGES );
   }*/
   
   /**
    * Placeholder for unimplemented features.
    * 
    * @return Panel
    */
   private JPanel notImplementedPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,1));
      
      JLabel label = new JLabel("Not Yet Implemented");
      label.setHorizontalAlignment(JLabel.CENTER);
      
      panel.add(label);
      
      return panel;
   }
   
   /**
    * Send message to MessageCenter.
    * 
    * @param command
    * @param value
    */
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command,
                                    value,
                                    true);
      
      messageCenter.send(message);
   }
   
   /**
    * Main method to test and make sure displays 
    * and sends messages correctly
    * 
    * @param args
    */
   public static void main(String[] args)
   {
      MessageCenter mc  = new MessageCenter("Testing MessageCenter");
      MessageCenter vmc = new MessageCenter("Testing ViewMessageCenter");
      TestReceiver tc = new TestReceiver("controlsPanel TestingMessages");
      mc.addReceiver(tc, Commands.CHANGE_PANEL);
      
      controlsPanel op = new controlsPanel(mc, vmc);
      
      JFrame View = new JFrame( "Test Controls Panel" );
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setBounds(10,10, 300, 275);
      View.setVisible(true);
      
      View.add(op);

      new TimedTrigger(mc, 100);
      new TimedTrigger(vmc, 100);
   }
}

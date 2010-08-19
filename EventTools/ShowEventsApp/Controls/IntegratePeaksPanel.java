/* 
 * File: IntegratePeaksPanel.java
 *
 * Copyright (C) 2010, Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */


package EventTools.ShowEventsApp.Controls;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.*;

import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.IntegratePeaksCmd;
import EventTools.ShowEventsApp.DataHandlers.QuickIntegrateHandler;
import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import gov.anl.ipns.ViewTools.UI.FontUtil;


public class IntegratePeaksPanel extends JPanel implements IReceiveMessage
{
   private MessageCenter message_center;
                                            // Sphere integrate controls
   private JTextField   radius_txf      = new JTextField("0.2");
   private JRadioButton found_peaks_ckb = new JRadioButton("Current Peaks");
   private JRadioButton all_peaks_ckb   = 
                                 new JRadioButton("All Predicted Positions");

   private JButton      sphere_stats_button = new JButton("Show Statistics");
   private JButton      set_in_peaks_button = new JButton("Update Peaks List");

                                            // Histogram integrate controls

   private final String NO_HISTOGRAM    = "NO HISTOGRAM";
   private final String HISTOGRAM_READY = "READY TO ADD EVENTS";
   private final String EVENTS_ADDED_TO_HISTOGRAM = "EVENTS ADDED TO HISTOGRAM";
   private String[] step_list = { "  2 Steps per Miller Index",
                                  "  3 Steps per Miller Index",
                                  "  4 Steps per Miller Index",
                                  "  5 Steps per Miller Index",
                                  "  6 Steps per Miller Index",
                                  "  7 Steps per Miller Index",
                                  "  8 Steps per Miller Index",
                                  "  9 Steps per Miller Index",
                                  " 10 Steps per Miller Index",
                                  " 11 Steps per Miller Index",
                                  " 12 Steps per Miller Index",
                                  " 13 Steps per Miller Index",
                                  " 14 Steps per Miller Index",
                                  " 15 Steps per Miller Index",
                                  " 16 Steps per Miller Index",
                                  " 17 Steps per Miller Index",
                                  " 18 Steps per Miller Index",
                                  " 19 Steps per Miller Index",
                                  " 20 Steps per Miller Index",
                                  " 21 Steps per Miller Index",
                                  " 22 Steps per Miller Index",
                                  " 23 Steps per Miller Index",
                                  " 24 Steps per Miller Index",
                                  " 25 Steps per Miller Index" };
   private JComboBox   steps_selector = new JComboBox( step_list );
   private JTextField  hist_mem_txf   = new JTextField("0");
   private JButton init_hist_button   = new JButton("Initialize Histogram");
   private JButton free_hist_button   = new JButton("Free Histogram Memory");
   private JTextField hist_status_txf = new JTextField( NO_HISTOGRAM );
   private JButton hist_stats_button  = new JButton("Show Statistics");
   private JButton to_peaks_button    = new JButton("Update Peaks List");

                                            // Integration Stats Controls
   private final float LEVEL_0 = QuickIntegrateHandler.LEVEL_0;
   private final float LEVEL_1 = QuickIntegrateHandler.LEVEL_1;
   private final float LEVEL_2 = QuickIntegrateHandler.LEVEL_2;
   private final float LEVEL_3 = QuickIntegrateHandler.LEVEL_3;
   private final float LEVEL_4 = QuickIntegrateHandler.LEVEL_4;

   private JTextField  num_level_0_txf = new JTextField( "" );
   private JTextField  num_level_1_txf = new JTextField( "" );
   private JTextField  num_level_2_txf = new JTextField( "" );
   private JTextField  num_level_3_txf = new JTextField( "" );
   private JTextField  num_level_4_txf = new JTextField( "" );


   public IntegratePeaksPanel( MessageCenter message_center )
   {
      super();
      this.message_center = message_center;
      message_center.addReceiver( this, 
                                  Commands.SET_INTEGRATED_INTENSITY_STATS );
      message_center.addReceiver( this, Commands.SET_HISTOGRAM_SPACE_MB );
      message_center.addReceiver( this, Commands.INTEGRATE_HISTOGRAM_READY );
      message_center.addReceiver( this, Commands.INTEGRATE_HISTOGRAM_FREED );
      message_center.addReceiver( this, 
                                  Commands.ADDED_EVENTS_TO_INTEGRATE_HISTOGRAM);

      setBorder( new TitledBorder("Quick Integrate Options") );
      setLayout( new GridLayout( 2, 1 ) );

      JTabbedPane tabbed_pane = new JTabbedPane();
      tabbed_pane.addTab("Sphere Method", 
                          buildSphereIntegratePanel() );
      tabbed_pane.addTab("Aligned Histogram Method",
                          buildHistogramIntegratePanel() );
      add( tabbed_pane );
      add( buildStatsPanel() );
   }


   private JPanel buildSphereIntegratePanel()
   {
      JPanel sphere_panel = new JPanel();

      sphere_panel.setLayout( new GridLayout( 5, 2 ) );

      sphere_panel.add( new JLabel(" Sphere Radius " + FontUtil.INV_ANGSTROM));
      sphere_panel.add( radius_txf );
        radius_txf.setHorizontalAlignment( JTextField.RIGHT );

      sphere_panel.add( found_peaks_ckb );
      sphere_panel.add( all_peaks_ckb );
      found_peaks_ckb.setSelected(true);
      ButtonGroup group = new ButtonGroup();
      group.add( found_peaks_ckb );
      group.add( all_peaks_ckb ); 

      sphere_panel.add( sphere_stats_button );
      sphere_panel.add( set_in_peaks_button );

      for ( int i = 0; i < 4; i++ )                  // add filler panels
        sphere_panel.add( new JPanel() );

      SphereButtonListener listener = new SphereButtonListener();
      sphere_stats_button.addActionListener( listener );
      set_in_peaks_button.addActionListener( listener );

      return sphere_panel;
   }


   private JPanel buildHistogramIntegratePanel()
   {
      JPanel hist_panel = new JPanel();

      hist_panel.setLayout( new GridLayout( 5, 2 ) );

      hist_panel.add( new JLabel("Histogram Resolution") );
      hist_panel.add( steps_selector );
        steps_selector.addItemListener( new StepsListener() );
        steps_selector.setSelectedIndex(3);

      hist_panel.add( new JLabel("Required Histogram Size(MB)") );
      hist_panel.add( hist_mem_txf );
        hist_mem_txf.setHorizontalAlignment( JTextField.RIGHT );
        hist_mem_txf.setEditable( false );

      hist_panel.add( new JLabel("Histogram Status") );
      hist_panel.add( hist_status_txf );
        hist_status_txf.setHorizontalAlignment( JTextField.RIGHT );
        hist_status_txf.setEditable( false );

      hist_panel.add( init_hist_button );
      hist_panel.add( free_hist_button );

      hist_panel.add( hist_stats_button );
      hist_panel.add( to_peaks_button );

      HistButtonListener listener = new HistButtonListener();
      init_hist_button.addActionListener( listener );
      free_hist_button.addActionListener( listener );
      hist_stats_button.addActionListener( listener );
      to_peaks_button.addActionListener( listener );

      return hist_panel;
   }


   private JPanel buildStatsPanel()
   {
      JPanel stats_panel = new JPanel();
      stats_panel.setBorder(new TitledBorder("Integration Statistics"));
      stats_panel.setLayout( new GridLayout(5,2) );

      stats_panel.add( new JLabel("Number of Peaks    >=  " + LEVEL_0) );
      stats_panel.add( num_level_0_txf );

      stats_panel.add( new JLabel("Number with I/sigI >=  " + LEVEL_1) );
      stats_panel.add( num_level_1_txf );

      stats_panel.add( new JLabel("Number with I/sigI >=  " + LEVEL_2 ) );
      stats_panel.add( num_level_2_txf );

      stats_panel.add( new JLabel("Number with I/sigI >=  " + LEVEL_3 ) );
      stats_panel.add( num_level_3_txf );

      stats_panel.add( new JLabel("Number with I/sigI >= " + LEVEL_4 ) );
      stats_panel.add( num_level_4_txf );

      for ( int i = 0; i < 5; i++ )
      {
        Component comp = stats_panel.getComponent( 2 * i + 1 );
        if ( comp instanceof JTextField )
        {
          JTextField txf = (JTextField)comp;
          txf.setHorizontalAlignment( JTextField.RIGHT );
          txf.setEditable( false );
        }
      }

      return stats_panel;
   }

   @Override
   public boolean receive( Message message )
   {
     if ( message == null ) 
       return true;

     if ( message.getName().equals(Commands.SET_INTEGRATED_INTENSITY_STATS) )
     {
       Object obj = message.getValue();
       if ( obj != null && obj instanceof int[] )
       {
         int[] stats = (int[])obj;
         JTextField[] text_fields = { num_level_0_txf,
                                      num_level_1_txf,
                                      num_level_2_txf,
                                      num_level_3_txf,
                                      num_level_4_txf };
         for ( int i = 0; i < text_fields.length; i++ )
           if ( i < stats.length )
             text_fields[i].setText( "" + stats[i] );
           else
             text_fields[i].setText( "0" );
       }
       else
         System.out.println("ERROR: wrong value type in IntegratePeaksPanel " +
                             Commands.SET_INTEGRATED_INTENSITY_STATS );
     }

     else if ( message.getName().equals(Commands.SET_HISTOGRAM_SPACE_MB) )
     {
       Object obj = message.getValue();
       if ( obj != null && obj instanceof Float )
         hist_mem_txf.setText( String.format( "%5.1f", (Float)obj ) );
       else
         System.out.println("ERROR: wrong value type in IntegratePeaksPanel " +
                             Commands.SET_INTEGRATED_INTENSITY_STATS );
     }

     else if ( message.getName().equals(Commands.INTEGRATE_HISTOGRAM_READY))
       hist_status_txf.setText( HISTOGRAM_READY );

     else if ( message.getName().equals(Commands.INTEGRATE_HISTOGRAM_FREED))
       hist_status_txf.setText( NO_HISTOGRAM );

     else if ( message.getName().equals(
                                Commands.ADDED_EVENTS_TO_INTEGRATE_HISTOGRAM))
       hist_status_txf.setText( EVENTS_ADDED_TO_HISTOGRAM );

     return false;
   }


   /**
    * Sends a message to the message center
    * 
    * @param name    Message queue name.
    * @param value   Object to send as the message value.
    */
   private void sendMessage( String name, Object value )
   {
      Message message = new Message( name, 
                                     value,
                                     true,
                                     true );
      message_center.send( message );
   }


   private float getValueFromTextField( JTextField txf )
   {
     float value = 0;
     try
     {
       value = Float.parseFloat( txf.getText().trim() );
     }
     catch( Exception ex )
     {
       JOptionPane.showMessageDialog( null,
                                      "Invalid Number " + txf.getText(),
                                     "Error",
                                     JOptionPane.ERROR_MESSAGE);
     }
     return value;
   }


   private class HistButtonListener implements ActionListener
   {
     public void actionPerformed( ActionEvent a_event )
     {
       if ( a_event.getSource() == init_hist_button )
         sendMessage( Commands.INIT_INTEGRATE_HISTOGRAM, null );

       else if ( a_event.getSource() == free_hist_button )
       {
         sendMessage( Commands.FREE_INTEGRATE_HISTOGRAM, null );
         hist_status_txf.setText( NO_HISTOGRAM );         
       }
       else if ( a_event.getSource() == hist_stats_button )
         sendMessage( Commands.SCAN_INTEGRATED_INTENSITIES, null );

       else if ( a_event.getSource() == to_peaks_button )
         sendMessage( Commands.MAKE_INTEGRATED_PEAK_Q_LIST, null );
     }
   }


   private class SphereButtonListener implements ActionListener
   {
     public void actionPerformed( ActionEvent a_event )
     {
       float   sphere_radius      = getValueFromTextField( radius_txf );
       boolean current_peaks_only = found_peaks_ckb.isSelected();

       boolean record_as_peaks_list = false;
       if ( a_event.getSource() == set_in_peaks_button )
         record_as_peaks_list = true;

       IntegratePeaksCmd cmd = new IntegratePeaksCmd( null, 
                                                      sphere_radius,
                                                      current_peaks_only,
                                                      record_as_peaks_list );
       sendMessage( Commands.GET_PEAKS_TO_SPHERE_INTEGRATE, cmd );
     }
   }


   private class StepsListener implements ItemListener
   {
     public void itemStateChanged( ItemEvent item_event )
     {
       if ( item_event.getStateChange() == ItemEvent.SELECTED )
       {
         int steps = 2 + steps_selector.getSelectedIndex();
         sendMessage( Commands.SET_STEPS_PER_MILLER_INDEX, (Integer)steps );
         hist_status_txf.setText( NO_HISTOGRAM );         
       }
     }
   }
   
}

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
import EventTools.ShowEventsApp.DataHandlers.QuickIntegrateHandler;
import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;


public class IntegratePeaksPanel extends JPanel implements IReceiveMessage
{
   private final float LEVEL_0 = QuickIntegrateHandler.LEVEL_0;
   private final float LEVEL_1 = QuickIntegrateHandler.LEVEL_1;
   private final float LEVEL_2 = QuickIntegrateHandler.LEVEL_2;
   private final float LEVEL_3 = QuickIntegrateHandler.LEVEL_3;
   private final float LEVEL_4 = QuickIntegrateHandler.LEVEL_4;

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

   private MessageCenter message_center;

   private JTextField  run_num_txf     = new JTextField("0");
   private JTextField  phi_txf         = new JTextField("0");
   private JTextField  chi_txf         = new JTextField("0");
   private JTextField  omega_txf       = new JTextField("0");
   private JTextField  max_magQ_txf    = new JTextField("0");
   private JTextField  hist_mem_txf    = new JTextField("0");
   private JTextField  num_level_0_txf = new JTextField( "" ); 
   private JTextField  num_level_1_txf = new JTextField( "" ); 
   private JTextField  num_level_2_txf = new JTextField( "" ); 
   private JTextField  num_level_3_txf = new JTextField( "" ); 
   private JTextField  num_level_4_txf = new JTextField( "" ); 

   private JComboBox   steps_selector  = new JComboBox( step_list );

   private JButton scan_button   = new JButton("Scan Integrate Histogram");
   private JButton clear_button  = new JButton("Clear Int. Intensities");
   private JButton to_peaks_button = new JButton("Set In Peaks List");

   public IntegratePeaksPanel( MessageCenter message_center )
   {
      super();
      this.message_center = message_center;
      message_center.addReceiver( this, 
                                  Commands.SET_INTEGRATED_INTENSITY_STATS );
      message_center.addReceiver( this, Commands.SET_HISTOGRAM_SPACE_MB );

      ButtonListener listener = new ButtonListener();

      scan_button.addActionListener( listener );
      clear_button.addActionListener( listener );
      to_peaks_button.addActionListener( listener );

      setLayout( new GridLayout( 13, 2 ) );
     
      setBorder(new TitledBorder("Quick Integrate Options"));

      add( new JLabel("Run Number") ); 
      add( run_num_txf );

      add( new JLabel("Phi") ); 
      add( phi_txf );

      add( new JLabel("Chi") ); 
      add( chi_txf );

      add( new JLabel("Omega") ); 
      add( omega_txf );

      add( new JLabel("Max |Q| to Integrate") );
      add( max_magQ_txf ); 

      add( clear_button );
      add( steps_selector );
      steps_selector.addItemListener( new StepsListener() );
      steps_selector.setSelectedIndex(3);

      add( new JLabel("Histogram Space ( MB )") );
      add( hist_mem_txf );

      add( scan_button );
      add( to_peaks_button );

      add( new JLabel("Number of Peaks >= " + LEVEL_0) ); 
      add( num_level_0_txf );

      add( new JLabel("Number with I/sigI >= " + LEVEL_1) ); 
      add( num_level_1_txf );

      add( new JLabel("Number with I/sigI >= " + LEVEL_2 ) ); 
      add( num_level_2_txf );

      add( new JLabel("Number with I/sigI >= " + LEVEL_3 ) );
      add( num_level_3_txf );

      add( new JLabel("Number with I/sigI >= " + LEVEL_4 ) );
      add( num_level_4_txf );
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


   private class ButtonListener implements ActionListener
   {
     public void actionPerformed( ActionEvent a_event )
     {
       if ( a_event.getSource() == scan_button )
         sendMessage( Commands.COUNT_INTEGRATED_INTENSITIES, null );

       else if ( a_event.getSource() == clear_button )
         sendMessage( Commands.CLEAR_INTEGRATED_INTENSITIES, null );

       else if ( a_event.getSource() == to_peaks_button )
         sendMessage( Commands.SET_INT_I_IN_PEAKS_LIST,
                      null );
     }
   }


   private class StepsListener implements ItemListener
   {
     public void itemStateChanged( ItemEvent item_event )
     {
       if ( item_event.getStateChange() == ItemEvent.SELECTED )
       {
         int steps = 2 + steps_selector.getSelectedIndex();
         System.out.println("Should now use " + steps );
         sendMessage( Commands.SET_STEPS_PER_MILLER_INDEX, (Integer)steps );
       }
     }
   }
   
}

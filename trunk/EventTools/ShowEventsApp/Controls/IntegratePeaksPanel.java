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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.*;

import EventTools.ShowEventsApp.Command.Commands;
import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;


public class IntegratePeaksPanel extends JPanel implements IReceiveMessage
{
   MessageCenter message_center;

   JTextField  run_num_txf        = new JTextField("0");
   JTextField  phi_txf            = new JTextField("0");
   JTextField  chi_txf            = new JTextField("0");
   JTextField  omega_txf          = new JTextField("0");
   JTextField  num_positive_txf   = new JTextField("0"); 
   JTextField  num_good_isigi_txf = new JTextField("0"); 

   JTextField  file_name_txf      = new JTextField(
                                       "IsawEV_Quick.integrate");

   JButton scan_button   = new JButton("Scan Integrate Histogram");
   JButton clear_button  = new JButton("Clear Integ Intensities");
   JButton write_button  = new JButton("Write .integrate File");



   public IntegratePeaksPanel( MessageCenter message_center )
   {
      super();
      this.message_center = message_center;
      message_center.addReceiver( this, 
                                  Commands.SET_INTEGRATED_INTENSITY_STATS );

      ControlListener listener = new ControlListener();

      scan_button.addActionListener( listener );
      clear_button.addActionListener( listener );
      write_button.addActionListener( listener );

      setLayout( new GridLayout( 8 , 2 ) );
     
      setBorder(new TitledBorder("Quick Integrate Options"));

      add( new JLabel("Run Number") ); 
      add( run_num_txf );

      add( new JLabel("Phi") ); 
      add( phi_txf );

      add( new JLabel("Chi") ); 
      add( chi_txf );

      add( new JLabel("Omega") ); 
      add( omega_txf );

      add( new JLabel("Number of Peaks > 0") ); 
      add( num_positive_txf );

      add( new JLabel("Number with I/sigI >= 3") ); 
      add( num_good_isigi_txf );

      add( scan_button );
      add( clear_button );
  
      add( write_button );
      add( file_name_txf );
   }


   @Override
   public boolean receive( Message message )
   {
     if ( message == null ) 
       return true;

     if ( message.getName().equals(Commands.SET_INTEGRATED_INTENSITY_STATS) )
     {
       Object obj = message.getValue();
       if ( obj instanceof int[] )
       {
         int[] stats = (int[])obj;
         num_positive_txf.setText( "" + stats[0] );
         num_good_isigi_txf.setText("" + stats[1] ); 
       }
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


   private class ControlListener implements ActionListener
   {
     public void actionPerformed( ActionEvent a_event )
     {
       if ( a_event.getSource() == scan_button )
         sendMessage( Commands.COUNT_INTEGRATED_INTENSITIES, null );

       else if ( a_event.getSource() == clear_button )
         sendMessage( Commands.CLEAR_INTEGRATED_INTENSITIES, null );

       else if ( a_event.getSource() == write_button )
         sendMessage( Commands.WRITE_INTEGRATED_INTENSITIES,
                      file_name_txf.getText() );
     }
   }
   
}

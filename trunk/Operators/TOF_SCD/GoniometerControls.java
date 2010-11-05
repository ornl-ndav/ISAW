/* 
 * File: GoniometerControls.java 
 *
 * Copyright (C) 2009, Dennis Mikkelson
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

package Operators.TOF_SCD;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import MessageTools.*;
import SSG_Tools.Viewers.Controls.*;
import Controls.Valuators.*;
import gov.anl.ipns.ViewTools.UI.*;

/**
 *  This class constructs controls that allow the user to specify a 
 *  set of goniometer angles PHI, CHI, OMEGA.  It is designed to work
 *  with the GoniomterControlledPeaks class.
 */
public class GoniometerControls extends    JPanel
                                implements IReceiveMessage
{
  private static final long serialVersionUID = 1;
  private static final String ANGLE_LABEL = FontUtil.PHI_UC + ", " +
                                            FontUtil.CHI_UC + ", " +
                                            FontUtil.OMEGA_UC + " = ";
  float phi   = 0;
  float chi   = 0;
  float omega = 0;
  
  JTextField values_txf = null;

  /* -------------------------- constructor ------------------------------- */
  /**
   *  Construct a set of controls that will send messages for the specified
   *  set of peaks through the specified message center.
   *
   *  @param  message_center   The MessageCenter that handles messages for
   *                           the sets of peaks.
   *  @param  peaks_name       The name of the set of peaks to be controlled.
   */
  public GoniometerControls( MessageCenter   message_center,
                             String          peaks_name )
  {
    setBorder( new TitledBorder(peaks_name) );
    setLayout( new GridLayout(4,1) );

    String phi_queue = peaks_name + " PHI";
    String chi_queue = peaks_name + " CHI";
    String omega_queue = peaks_name + " OMEGA";

    add( new Slider( message_center, 
                     phi_queue,
                     "PHI",
                     -180, 180, 0, 3600 ) );
    add( new Slider( message_center,
                     chi_queue,
                     "CHI",
                     -180, 180, 0, 3600 ) );
    add( new Slider( message_center,
                     omega_queue,
                     "OMEGA",
                     -180, 180, 0, 3600 ) );

    values_txf = new JTextField( ANGLE_LABEL + "   0.00    0.00    0.00");
    values_txf.setEditable( false );
    add( values_txf );

    message_center.addReceiver( this, phi_queue );
    message_center.addReceiver( this, chi_queue );
    message_center.addReceiver( this, omega_queue );
  }

  /* --------------------------- receive ------------------------------- */
  /**
   *  Accept and process a command message for this robot.
   *
   *  @param message  The command message to be processed.
   *
   *  @return  Return true if the message was processed and false if there
   *           was something wrong with the message, that prevented it from
   *           being processed.
   */
  public boolean receive( Message message )
  {
    String command = (String)message.getName();

    float angle = (Float)message.getValue();
    if ( command.endsWith(ControlledPeaks.PHI) )
      phi = angle;

    else if ( command.endsWith(ControlledPeaks.CHI) )
      chi = angle;

    else if ( command.endsWith(ControlledPeaks.OMEGA) )
      omega = angle;

    String values = String.format("  %7.2f  %7.2f  %7.2f", phi, chi, omega );
    values_txf.setText( ANGLE_LABEL + values );

    return true;
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Basic program for testing.
   */
  public static void main( String args[] )
  {
    MessageCenter message_center = new MessageCenter("Test Center");
    TestReceiver  receiver = new TestReceiver( "Test Receiver" );
    message_center.addReceiver( receiver, "Test Peaks PHI" );
    message_center.addReceiver( receiver, "Test Peaks CHI" );
    message_center.addReceiver( receiver, "Test Peaks OMEGA" );

    GoniometerControls controls = 
                        new GoniometerControls( message_center, "Test Peaks" );

    JFrame frame = new JFrame( "Goniometer Controls Test" );
    frame.setSize(300,300);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( controls );
    frame.setVisible( true );

    int FRAME_TIME = 30;
    new UpdateManager( message_center, null, FRAME_TIME );
  }
}


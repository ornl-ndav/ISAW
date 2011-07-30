/* 
 * File: AutoIndexingPanel.java
 *
 * Copyright (C) 2011, Dennis Mikkelson
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
 *  $Author: eu7 $
 *  $Date: 2010-04-19 15:37:10 -0500 (Mon, 19 Apr 2010) $            
 *  $Revision: 20664 $
 */

package EventTools.ShowEventsApp.Controls.Peaks;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import gov.anl.ipns.MathTools.Geometry.Vector3D_d;
import MessageTools.*;

import EventTools.ShowEventsApp.Command.*;


public class AutoIndexingPanel extends JPanel
{
  private static final String[] keys = { "d_min", 
                                         "d_max",
                                         "angle_step",
                                         "base_index",
                                         "num_initial" };

  private static final String[] labels = { " Min Unit Cell Edge",
                                           " Max Unit Cell Edge",
                                           " Angle Step (degrees)", 
                                           " Base Peak Seq Number",
                                           " Number to Index Initially" };

  private static final String[] defaults = { "3", "15",
                                             "1", "-1", "15" };
  private Hashtable jtextf;


  /**
   *  Build the panel displaying parameters needed by the new auto index
   *  with params method, and keep the Text Fields so that the parameters 
   *  can be placed in a command to carry out that indexing method.
   */
  public AutoIndexingPanel()
  {
    jtextf           = new Hashtable( keys.length );
    Hashtable jlabel = new Hashtable( keys.length );

    for ( int i = 0; i < keys.length; i++ )
    {
      JLabel     label      = new JLabel( labels[i] );
      JTextField text_field = new JTextField( defaults[i] );

      text_field.setHorizontalAlignment(JTextField.RIGHT);

      jlabel.put( keys[i], label );
      jtextf.put( keys[i], text_field );
    }

    this.setLayout(new GridLayout(keys.length, 2));
    for ( int i = 0; i < keys.length; i++ )    
    {
      add( (JLabel)jlabel.get(keys[i])    );
      add( (JTextField)jtextf.get(keys[i]) );
    }

    this.setBorder(new TitledBorder("Auto Index in Range"));
  }


  /**
   * Use the current values from the text fields of this panel, and the
   * specified tolerance to build an IndexARCS_PeaksCmd and send that
   * command message to the specified message_center.
   *
   * @param message_center  The Message Center that should receive the command
   * @param tolerance       The tolerance value to use when indexing peaks
   */
  public void DoAutoIndexing( MessageCenter message_center, float tolerance )
  {
    Hashtable hash = new Hashtable( keys.length );
                                                // do quick check of input 
    for ( int i = 0; i < keys.length; i++ )
    {
      try
      {
        JTextField tf = (JTextField)jtextf.get( keys[i] );
        float value = Float.parseFloat( tf.getText() );
        hash.put( keys[i], value );
      }
      catch (NumberFormatException e)
      {
        String error = labels[i] + " must be a number";
        JOptionPane.showMessageDialog( null, error, "Invalid Input",
                                       JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
                                           // Note: sequence numbers start at 1
    IndexPeaksAutoCmd cmd = new IndexPeaksAutoCmd( 
                                               (Float)hash.get("d_min"),
                                               (Float)hash.get("d_max"),
                                               (Float)hash.get("angle_step"),
                                    Math.round((Float)hash.get("base_index"))-1,
                                    Math.round((Float)hash.get("num_initial")),
                                                tolerance );

    Message message = new Message( Commands.INDEX_PEAKS_AUTO, cmd, true );

    message_center.send( message );
  }
}

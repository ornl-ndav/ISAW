/* 
 * File: ARCS_IndexPanel.java
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

package EventTools.ShowEventsApp.Controls.Peaks;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import gov.anl.ipns.MathTools.Geometry.Vector3D_d;
import MessageTools.*;

import EventTools.ShowEventsApp.Command.*;


public class ARCS_IndexPanel extends JPanel
{
  private static final String[] keys = { "a", "b", "c", 
                                         "alpha", "beta", "gamma",
                                         "init_num", "req_frac", "psi",
                                         "u_h", "u_k", "u_l",
                                         "v_h", "v_k", "v_l" };

  private static final String[] labels = { " a", " b", " c",
                                           " alpha", " beta", " gamma",
                                           " Pass 1 Number to Index", 
                                           " Pass 1 Required Fraction", 
                                           " PSI angle",
                                           "    h", "    k", "    l",
                                           "    h", "    k", "    l" };

  private static final String[] defaults = { "3.805", "3.805", "6.28",
                                             "90", "90", "90",
                                             "12", ".6", "5",
                                             "0", "0", "0",
                                             "0", "0", "0" };
  private Hashtable jtextf;


  /**
   *  Build the panel displaying parameters needed by the ARCS index
   *  method, and keep the Text Fields so that the parameters can be
   *  placed in a command to carry out that indexing method.
   */
  public ARCS_IndexPanel()
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
                                           // everything up to u and v vectors
                                           // goes into the upper panel
    JPanel sub_panel = new JPanel();
    sub_panel.setLayout(new GridLayout(9, 2));
    int i = 0;                           
    while ( ! keys[i].equals( "u_h" ) )    
    {
      sub_panel.add( (JLabel)jlabel.get(keys[i])    );
      sub_panel.add( (JTextField)jtextf.get(keys[i]) );
      i++;
    }
                                           // u vector goes into separate panel 
    JPanel u_hkl_panel = new JPanel();
    u_hkl_panel.setBorder(new TitledBorder("U vector"));
    u_hkl_panel.setLayout( new GridLayout(1,6) );
    while ( ! keys[i].equals( "v_h" ) )
    {
      u_hkl_panel.add( (JLabel)    jlabel.get(keys[i])    );
      u_hkl_panel.add( (JTextField)jtextf.get(keys[i]) );
      i++;
    }
                                           // v vector goes into separate panel
    JPanel v_hkl_panel = new JPanel();
    v_hkl_panel.setBorder(new TitledBorder("V vector"));
    v_hkl_panel.setLayout( new GridLayout(1,6) );
    while ( i < keys.length )
    {
      v_hkl_panel.add( (JLabel)    jlabel.get(keys[i])    );
      v_hkl_panel.add( (JTextField)jtextf.get(keys[i]) );
      i++;
    }
                                           // now put the panels together
    JPanel sub_panel_2 = new JPanel();
    sub_panel_2.setLayout( new GridLayout(2,1) );
    sub_panel_2.add(u_hkl_panel);
    sub_panel_2.add(v_hkl_panel);

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.add( sub_panel );
    this.add( sub_panel_2 );
  }


  /**
   * Use the current values from the text fields of this panel, and the
   * specified tolerance to build an IndexARCS_PeaksCmd and send that
   * command message to the specified message_center.
   *
   * @param message_center  The Message Center that should receive the command
   * @param tolerance       The tolerance value to use when indexing peaks
   */
  public void DoARCS_Indexing( MessageCenter message_center, float tolerance )
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
                                                // build and send the command
    Vector3D_d u_vec = new Vector3D_d( (Float)hash.get("u_h"), 
                                       (Float)hash.get("u_k"),
                                       (Float)hash.get("u_l") );

    Vector3D_d v_vec = new Vector3D_d( (Float)hash.get("v_h"), 
                                       (Float)hash.get("v_k"),
                                       (Float)hash.get("v_l") );

    IndexARCS_PeaksCmd cmd = new IndexARCS_PeaksCmd( 
                                       (Float)hash.get("a"), 
                                       (Float)hash.get("b"),
                                       (Float)hash.get("c"),
                                       (Float)hash.get("alpha"),
                                       (Float)hash.get("beta"),
                                       (Float)hash.get("gamma"),
                                       (Float)hash.get("psi"),
                                       u_vec,
                                       v_vec,
                                       tolerance,
                                       Math.round((Float)hash.get("init_num")),
                                       (Float)hash.get("req_frac") );

    Message message = new Message( Commands.INDEX_PEAKS_ARCS, cmd, true );
    message_center.send( message );
  }

}

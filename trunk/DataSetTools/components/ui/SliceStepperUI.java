/*
 * File:  SliceStepperUI.java
 *
 * Copyright (C) 2004, Dennis Mikkelson
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
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2004/01/26 23:54:32  dennis
 * Initial version of user interface for stepping a
 * rectangular slab, forward and backward in 3D.
 *
 */

package DataSetTools.components.ui;

import DataSetTools.util.*;
import java.awt.*;
import java.awt.event.*;
//import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.*;

/**
 *  This class provides a user interface for specifying the size and
 *  resolution of an image of a rectangular slab in 3D.
 */

public class SliceStepperUI extends    ActiveJPanel
                            implements Serializable 
{
  public static final String VALUE_CHANGED = "Value Changed";

  private TextValueUI  step_ui;

  /*-------------------------- default constructor ----------------------- */
  /**
   *  Construct a SliceStepperUI with default values.
   */
  public SliceStepperUI( String title )
  {
    step_ui      = new TextValueUI( "Step Size ", 0.02f );

    JButton backward_button = new JButton( "<" );
    JButton forward_button = new JButton( ">" );
    JPanel  button_panel = new JPanel();
    button_panel.setLayout( new GridLayout( 1, 2 ) );
    button_panel.add( backward_button );
    button_panel.add( forward_button );

    TitledBorder border =
                 new TitledBorder(LineBorder.createBlackLineBorder(), title );
    border.setTitleFont( FontUtil.BORDER_FONT );
    setBorder( border );

    setLayout( new GridLayout(2,1) );
    add( step_ui );
    add( button_panel );

    ValueListener value_listener = new ValueListener();
    step_ui.addActionListener( value_listener );
    backward_button.addActionListener( value_listener );
    forward_button.addActionListener( value_listener );
  }

  /* ------------------------- getStepSize --------------------------- */
  /**
   *  Get the currently selected step size.
   *
   *  @return the currently selected steps/unit.
   */
  public float getStepSize()
  {
    return step_ui.getValue();
  }

  /* ----------------------------- toString ------------------------------ */
  /**
   *  Return a string form of this plane.
   */
  public String toString()
  {
    return step_ui.getLabel() + ": " + step_ui.getValue();
  }

  /* -----------------------------------------------------------------------
   *
   *  PRIVATE CLASSES
   *
   */
  /* ------------------------ ValueListener ------------------------------ */
  /*
   *  Listen for a new value.
   */ 
  private class ValueListener implements ActionListener
  {
    public void actionPerformed( ActionEvent e )
    {
      System.out.println("Value changed");
      send_message( VALUE_CHANGED );
    }
  }

  /* ------------------------------ main --------------------------------- */
  /**
   *  Main program providing basic functionality test.
   */
  public static void main( String args[] )
  {
    JFrame  f = new JFrame("Test for SliceStepperUI");
    f.setBounds( 0, 0, 200, 100 ); 

    final SliceStepperUI test = new SliceStepperUI("Move Slice");

    test.addActionListener( new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        System.out.println("New Values ----------------------------" );
        System.out.println("" + test.getStepSize() );
        System.out.println("--------------------------------------" );
      }
    });

    f.getContentPane().setLayout( new GridLayout(1,1) );
    f.getContentPane().add( test );
    f.setVisible(true);
  }

}

/* 
 * File: TransformHKL_Panel.java
 *
 * Copyright (C) 2012, Dennis Mikkelson
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
 *  $$
 *  $$            
 *  $$
 */

package EventTools.ShowEventsApp.Controls;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;

import gov.anl.ipns.MathTools.Geometry.Tran3D;
import MessageTools.*;

import EventTools.ShowEventsApp.Command.*;

/**
 *  This class implements a JPanel where the user can specify a 
 *  transformation matrix, M, that will be applied to the HKL
 *  values of indexed peaks.  The UB matrix will be modified
 *  to be UB*M-inverse.
 */
public class TransformHKL_Panel extends JPanel
{
  private MessageCenter message_center;
  private JButton apply_button;
  private JButton reset_button;


  private static final String[][] defaults = { { "1.0", "0.0", "0.0" },
                                               { "0.0", "1.0", "0.0" },
                                               { "0.0", "0.0", "1.0" } };

  private static JTextField[][] matrix_textf= new JTextField[3][3];

  /**
   *  Build the panel displaying parameters for adjusting the 
   *  HKL values in a list of peaks, using a 3x3 transformation.
   *  @param message_center  The Message Center that should receive the command
   */
  public TransformHKL_Panel( MessageCenter message_center )
  {
    this.message_center = message_center;

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.setBorder(new TitledBorder("Select Transform Matrix"));
                                             
                                           // add panel with explanatory text
    JPanel explain_panel = new JPanel();
    String[] explain = {"   The matrix, M, entered below will multiply",
                        "the current h,k,l values. The UB matrix will",
                        "be changed to UB * M-inverse, so the q-vectors",
                        "are not changed.",
                        "",
                        "   Press 'Apply' to change UB and re-index the",
                        "peaks.",
                        "",
                        "NOTE: 'Reset' just resets the form, for entering",
                        "a new matrix M, and does NOT un-do previously",
                        "applied changes." };
    JLabel[] exp_label = new JLabel[ explain.length ]; 
    explain_panel.setLayout( new GridLayout( 2 + explain.length, 1 ) );
    explain_panel.add( new JLabel("") );
    for ( int i = 0; i < explain.length; i++ )
    {
      exp_label[i] = new JLabel( explain[i], SwingConstants.LEFT );
      explain_panel.add( exp_label[i] );
    }
    explain_panel.add( new JLabel("") );
    this.add( explain_panel );
                                           // add panel for matrix entry
    JPanel matrix_panel = new JPanel();
    matrix_panel.setBorder(new TitledBorder("Transform Matrix, M"));
    matrix_panel.setLayout( new GridLayout(3,3) );

    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
    {
      JTextField text_field = new JTextField( defaults[row][col] );
      text_field.setHorizontalAlignment(JTextField.CENTER);
      matrix_textf[row][col] = text_field;
      matrix_panel.add( text_field );
    }
    this.add( matrix_panel );
                                            // add filler panel
    JPanel filler_panel = new JPanel();
    this.add( filler_panel );
    filler_panel.setPreferredSize( new Dimension(0,300) );

                                            // add panel with control buttons
    apply_button = new JButton("Apply");
    reset_button = new JButton("Reset");
  
    apply_button.addActionListener( new ApplyButtonListener() );
    reset_button.addActionListener( new ResetButtonListener() );

    JPanel button_panel = new JPanel();
    button_panel.setLayout( new GridLayout(1,2) );
    button_panel.add( apply_button );
    button_panel.add( reset_button );
    
    this.add( button_panel );
  }


  /**
   * Reset input matrix to the identity matrix.
   */
  private void Reset()
  {
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
        matrix_textf[row][col].setText( defaults[row][col] );
  }
  

  /**
   * Use the current values from the text fields of this panel
   * TranformPeaksCmd and send that command message.
   */
  private void TransformHKLs()
  {
                                                // do quick check of input 
    double[][] transform = new double[3][3];
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
      {
        try
        {
          double value = Double.parseDouble( matrix_textf[row][col].getText() );
          transform[row][col] = value;
        }
        catch (NumberFormatException e)
        {
          String error = "M( " + (row+1) + ", " + 
                                    (col+1) + " ) must be a number";

          JOptionPane.showMessageDialog( null, error, "Invalid Input",
                                         JOptionPane.ERROR_MESSAGE);
          return;
        }
      }

    Message message = new Message( Commands.TRANSFORM_HKL, transform, true );
    message_center.send( message );
  }

 /**
  * This class handles 'Reset' button presses 
  */
   class ResetButtonListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
        Reset();
      }
   }

 /**
  * This class handles 'Apply' button presses 
  */
   class ApplyButtonListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
        TransformHKLs();
      }
   }

}

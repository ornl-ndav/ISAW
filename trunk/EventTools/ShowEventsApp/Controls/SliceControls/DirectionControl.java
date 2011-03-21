/* 
 * File: DirectionControl.java
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
 *  $Author: $
 *  $Date: $            
 *  $Revision:$
 */

package EventTools.ShowEventsApp.Controls.SliceControls;

import java.awt.*;
import javax.swing.*;

import gov.anl.ipns.ViewTools.UI.Vector3D_UI;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

/**
 *  This class implements a simple control for specifying the direction
 *  and binning for an "edge" of a 3D histogram.  The control consists of
 *  a JPanel with text fields for entering the edge direction, step size
 *  and number of steps in the specified direction.
 */
public class DirectionControl extends JPanel
{
  private String       title;
  private Vector3D_UI  VectorUI;
  private JTextField   StepSizeTF;
  private JTextField   NumStepsTF;


  /**
   * Construct a DirectionControl object with the specified title and
   * and initial value for the the direction vector.
   * @param  title        The title that will be displayed for the 
   *                      vector
   * @param  initial_vec  The initial value of the vector for this control
   */
  public DirectionControl( String title, Vector3D initial_vec )
  {
    this.title = title;

    VectorUI   = new Vector3D_UI( "", initial_vec );
    StepSizeTF = new JTextField("0.04");
    NumStepsTF = new JTextField("250");

    VectorUI.setHorizontalAlignment( JTextField.RIGHT );
    StepSizeTF.setHorizontalAlignment( JTextField.RIGHT );
    NumStepsTF.setHorizontalAlignment( JTextField.RIGHT );

    setLayout( new GridLayout(3,2) );

    add( new JLabel(title) );
    add( VectorUI );

    add( new JLabel("Step Size") );
    add( StepSizeTF );

    add( new JLabel("Number of Bins") );    
    add( NumStepsTF );
  }


  /**
   *  Get the title string that was specified when this control was 
   *  constructed.
   */
  public String getTitle()
  {
    return title;
  }


  /**
   *  Get the Vector3D object that was entered by the user.
   */
  public Vector3D getDirection()
  {
    return VectorUI.getVector();
  }


  /**
   *  Get the value entered for the step size.  If a valid value is not
   *  present, return 0;
   */
  public double getStepSize()
  {
    double value = 0.0;
    try
    {
      value = Double.parseDouble( StepSizeTF.getText().trim() ); 
    }
    catch ( Exception ex )
    {
      System.out.println("ERROR parsing step size in " + title );
    }
    return value;
  }


  /**
   *  Get the value entered for the number of steps.  If a valid value is not
   *  present, return 0;
   */
  public int getNumSteps()
  {
    int value = 0;
    try
    {
      value = Integer.parseInt( NumStepsTF.getText().trim() );
    }
    catch ( Exception ex )
    {
      System.out.println("ERROR parsing num steps " + title );
    }

    if ( value < 0 )     // negative number of steps not allowed
      value = 0;

    return value;
  }


  /**
   *  Main method for basic testing purposes only. 
   */
  public static void main( String args[] )
  {
    JFrame test_frame = new JFrame("Direction Control");
    test_frame.setBounds( 0, 0, 300, 200 );
    test_frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
    
    Vector3D initial_vec = new Vector3D( 1, 0, 0 );
    DirectionControl control = new DirectionControl( "Direction ( 'slice' )",
                                                      initial_vec );
    test_frame.add( control );
    test_frame.setVisible( true );

    System.out.println( control.getDirection() );
    System.out.println( control.getStepSize() );
    System.out.println( control.getNumSteps() );
  }


}

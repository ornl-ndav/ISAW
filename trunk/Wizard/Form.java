/*
 * File:  Form.java
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2002/02/27 17:29:20  dennis
 * Form base class that defines one logical step in a sequence
 * of steps controlled by a Wizard.  Derived classes will actually
 * provide meaningful operations.
 *
 *
 */

package Wizard;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;
import DataSetTools.components.ParametersGUI.*;

/**
 *  The Form class is controls one operation of the sequence of operations
 *  managed by a Wizard.  The Form produces a panel containing the title
 *  of the form, up to three lists of parameters for the form and an execute
 *  button.  This form class by itself does not do anything productive.
 *  Derived classes should override the execute() method to actually do
 *  some operations involving the parameters.  The execute() method is
 *  responsible for using the input parameters and producing new values for
 *  the result parameters. If special layouts and parameter interfaces are 
 *  needed, the MakeGUI() method can also be overridden in sub classes. 
 *
 *  @see Wizard
 *  @see WizardParameter
 *  @see Examples.MathWizard 
 *  @see Examples.AdderExampleForm
 */

public class Form implements Serializable
{
  protected  String    title;
  protected  String    help_message = "Help not available for this form ";

  protected  Wizard    wizard;              // the Wizard using this form
  protected  JPanel    panel;               // panel that the Wizard will draw
  protected  JButton   execute_button;      // button that invokes execute()
                                            // method.  Must be added to panel
                                            // by makeGUI() method, in any
                                            // derived class that overrides the
                                            // makeGUI() method
  protected  boolean   completed;           // set by execute, if done ok

  protected  String    const_params[];
  protected  String    editable_params[];
  protected  String    result_params[];

  /**
   *  Construct a form with the given title and parameter names to work with 
   *  the specified Wizard.  The actual parameters are obtained from the
   *  wizard, using the names provided to the constructor.
   *
   *  @param  title           The title to show on this form
   *  @param  const_params    The names of the parameters used in this 
   *                          calculation that the user should NOT alter
   *  @param  editable_parms  The names of the parameters used in this 
   *                          calculation that should be input by the user
   *  @param  result_params   The names of the parameters that will be 
   *                          calculated by this form
   *  @param  wizard          The Wizard that holds the master list of 
   *                          parameters 
   */
  public Form( String title,
               String const_params[],
               String editable_params[],
               String result_params[],
               Wizard wizard )
  {
    this.title           = title;              
    this.const_params    = const_params;
    this.editable_params = editable_params;
    this.result_params   = result_params;
    this.wizard          = wizard;

    completed      = false;
    panel          = new JPanel();
    execute_button = new JButton("Execute");
    execute_button.addActionListener( new ExecuteListener() );

    makeGUI();
  } 

  /**
   *  This method makes the GUI for the Form.  If a derived class overrides
   *  this method, it must build it's own user interface in the current
   *  JPanel, panel, since that is what is returned to the Wizard to show the
   *  form.  Also, this method is NOT just called at construction time, but
   *  is called each time the Form is shown by the Wizard.  This guarantees 
   *  that the paramter values will be properly displayed using their current 
   *  values.  In addition to displaying interfaces for the parameters,
   *  this method should add the execute_button to the panel, so that the
   *  user can trigger the calculation.
   */
  protected void makeGUI()
  {
    Box box = new Box( BoxLayout.Y_AXIS );
    panel.setLayout( new GridLayout( 1, 1 ) );    
    panel.removeAll();
    panel.add(box); 

    JLabel title_label = new JLabel( title, SwingConstants.CENTER );
    JPanel title_panel = new JPanel();
    title_panel.setLayout( new GridLayout(1,1) );
    title_panel.add( title_label );
    box.add( title_panel );

    JPanel sub_panel = build_param_panel("CONSTANT PARAMETERS", const_params);
    if ( sub_panel != null )
      box.add( sub_panel ); 

    sub_panel = build_param_panel("USER SPECIFIED PARAMETERS", editable_params);
    if ( sub_panel != null )
      box.add( sub_panel ); 

    sub_panel = build_param_panel("RESULTS", result_params);
    if ( sub_panel != null )
      box.add( sub_panel ); 

    JPanel button_panel = new JPanel();
    button_panel.add( execute_button );
    box.add(button_panel);
  }

  /**
   *  This builds the portions of the default form panel that contain a
   *  list of parameters inside of a panel with a titled border.  It is
   *  used to build up to three portions, corresponding to the constant,
   *  user-specified and result parameters.
   *
   *  @param  title  The title to put on the border
   *  @param  params The names of the parameter to include in this sub-panel
   */
  protected JPanel build_param_panel( String title, String params[] )
  {
    if ( params == null || params.length <= 0 )
      return null;

    JPanel       sub_panel = new JPanel();
    TitledBorder border;
    border = new TitledBorder(LineBorder.createBlackLineBorder(), title);
    border.setTitleFont( FontUtil.BORDER_FONT );
    sub_panel.setBorder( border );
    sub_panel.setLayout( new GridLayout( params.length, 1 ) );
    for ( int i = 0; i < params.length; i++ )
    {
      WizardParameter param = wizard.getParameter( params[i] );
      sub_panel.add( param.getGUISegment() );
    }
    return sub_panel;
  }

  /**
   *  Get the panel to display in the Wizard when this form is displayed.
   *  This will typically only be called by the Wizard controlling this form.
   *
   *  @returns The panel to display for this form.
   */
  public JPanel getPanel()
  {
    return panel;
  }

  /**
   *  Save the state of this form (NOT IMPLEMENTED YET)
   */
  public void save()
  {
    Wizard.status_display.append(title + " State save() Not Implemented\n");
  }

  /**
   *  Load the state of this form (NOT IMPLEMENTED YET)
   */
  public boolean load()
  {
    Wizard.status_display.append(title + " State load() Not Implemented\n");
    return false;
  }

  /**
   *  This rebuilds the GUI for this form in the panel and makes it visible.
   *  This will be called by the Wizard after adding the panel to it's 
   *  form display area.
   */
  public void show()
  {
    makeGUI();
    panel.validate();
    panel.setVisible(true);
  }

  /**
   *  This is called when the Wizard is no longer displaying the current 
   *  form.  Currently this just sets the panel to be invisible.  Eventually,
   *  additional parameters and logic could be added so that if the Wizard
   *  is advancing to the next form and the current form has NOT been 
   *  completed, then this will call execute to try to execute the form. 
   *
   *  @return true or false to indicate whether or not the form was completed
   *          properly.
   */
  public boolean hide()
  {
    panel.setVisible(false);
    return true;         
  }

  /**
   *  Specify the help message to use for this form.
   *
   *  @param  help_message  This will typically be a multi-line string
   *                        describing the operation carried out by this form.
   */
  public void setHelpMessage( String help_message )
  {
     this.help_message = help_message;
  }

  /**
   *  Get the help message for this form. 
   *
   *  @return  help_message  The help message to display for this form.
   */
  public String getHelpMessage()
  {
     return help_message;
  }

  /**
   *  Carry out the operation controlled by this form.  This should set the
   *  completed flag to true if the operation was successful.  This class
   *  will be overridden in derived classes.
   *
   *  @return true if the operation was successfully carried out.
   */
  public boolean execute()
  {
    Wizard.status_display.append(title + " execute() Not Implemented\n");
    // completed = .....
    return true;
  }

  /**
   *  Check whether or not this form has been successfully completed.
   *  (NOT IMPLEMENTED YET)
   * 
   *  @return true if the form's operation has been successfully carried out
   *               and none of the parameters have been subsequently altered 
   */
  public boolean done()
  {
    Wizard.status_display.append(title + " done() Not Implemented\n");
    return true;
    // check completed flag and
    // for each required parameter, check if still valid
    // for each result parameter, check if now set to valid value 
  }

  /**
   *  Internal class to handle the execute_button.
   */ 
  private class ExecuteListener implements ActionListener
  {
    public void actionPerformed( ActionEvent event )
    {
      execute(); 
    }
  }

  /**
   *  main program for testing purposes
   */
  public static void main( String args[] )
  {
    System.out.println("Form Main");
  }
}

/*
 * File:  WizardParameter.java
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
 * Revision 1.1  2002/05/28 20:36:01  pfpeterson
 * Moved files
 *
 * Revision 1.3  2002/04/11 22:32:48  pfpeterson
 * Added some comments for the javadocs.
 *
 * Revision 1.2  2002/03/12 16:09:46  pfpeterson
 * Now automatically disable constant and result parameters.
 *
 * Revision 1.1  2002/02/27 17:27:03  dennis
 * Subclass of Parameter that includes extra information and GUI
 * components for use in Wizards.
 *
 *
 */

package DataSetTools.wizard;

import java.awt.*;
import java.beans.*;
import javax.swing.*;
import DataSetTools.operator.*;
import DataSetTools.components.ParametersGUI.*;

/**
 *  This class bundles a DataSetTools.operator.Parameter object together with
 *  a panel that contains a GUI for displaying and entering a parameter value
 *  and a check_box to indicate whether or not this parameter's value has been
 *  set.
 */
public class WizardParameter extends Parameter
{
  private JParameterGUI  param_gui;
  private JCheckBox      check_box;
  private JPanel         extended_gui;
  private boolean        enabled;


  /**
   *  Construct a new WizardParmeter using the specified prompt string,
   *  object value and state of the value.
   */
  public WizardParameter( String prompt, Object value, boolean value_set )
  {
    super( prompt, value );
    param_gui = JParameterGUI.getInstance( this );
    check_box = new JCheckBox( "", value_set );
    check_box.setEnabled( false );
    extended_gui = new JPanel();
    enabled=true;
    MakeGUIPanel(); 
  }

  /**
   *  Check to see if this parameter's value has been marked as "set"
   *
   *  @return true if this parameter's value has been marked as set.
   */
  public boolean isSet()
  {
    return check_box.isSelected();
  }

  /**
   *  Mark this parameter's value as not being set. 
   */
  public void unSet()
  {
    check_box.setSelected( false );
  }

  /**
   *  Get the value currently recorded in the parameter GUI and set that
   *  as the value of the parameter.  Mark the value as set.
   *
   *  @return the value object from the parameter's GUI.
   */
  public Object getNewValue()
  {
    Object val = param_gui.getParameter().getValue();
    setValue( val );
    return val;
  }

  /**
   *  Record this object as the new value for this parameter and create
   *  a new parameter GUI for this parameter.  Mark the value as set.
   *
   *  @param object  The new value object for this parameter.  NOTE: This
   *                 object should be of the same class as the original
   *                 value object, though the classes are not checked at this
   *                 time.
   */
  public void setValue( Object object )
  {
    super.setValue( object );
    check_box.setSelected( true );
    param_gui = JParameterGUI.getInstance( this );
    this.setEnabled(enabled);
    MakeGUIPanel();
  }

  /**
   *  Get the current parameter GUI for this parameter.  NOTE: The parameter
   *  GUI is changed each time the value is set, so the value returned may 
   *  not still be the parameter GUI for this parameter at some later time.
   *
   *  @return the parameter GUI currently used to display this parameter.
   */ 
  public JParameterGUI getJParameterGUI()
  {
    return param_gui;
  }
 
  /** 
   *  Set a new parameter GUI for this parameter.  
   *
   *  @param  param_gui  A new parameter GUI object to use for this
   *                     parameter.  The specified param_gui should have
   *                     been constructed with reference to the current
   *                     parameter.
   */
  public void setJParameterGUI( JParameterGUI param_gui )
  {
    this.param_gui = param_gui;
    MakeGUIPanel();
  }

  /**
   *  Get the current GUI segment, containing the parameter GUI and the
   *  checkbox, for this parameter.
   *
   *  @return a panel containing the paramter GUI and checkbox for this
   *          parameter.
   */
  public JPanel getGUISegment()
  {
    return extended_gui;
  }

    /**
     * enable or disable the gui (editable/noneditable)
     */
    public void setEnabled(boolean en){
        this.enabled=en;
        param_gui.setEnabled(en);
    }

    /**
     * find out if the gui is editable
     */
    public boolean getEnabled(){
        return this.enabled;
    }

  /**
   *  Build the default GUI, containing the parameter GUI and checkbox,
   *  for this parameter.
   */
  private void MakeGUIPanel()
  {
    extended_gui.removeAll();
    Box box = new Box( BoxLayout.X_AXIS );
    box.add( param_gui.getGUISegment() );
    box.add( check_box );
    extended_gui.setLayout( new GridLayout(1,1) );
    extended_gui.add( box );
    extended_gui.validate();
    /* param_gui.addPropertyChangeListener(JParameterGUI.VALUE,
       new PropertyChangeListener(){
       public void propertyChange(PropertyChangeEvent ev){
       System.out.println("(WP)value: "+ev.getOldValue()+"->"
       +ev.getNewValue());
       }
       }); */
  }

}

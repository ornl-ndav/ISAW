/*
 * File:  Form.java
 *
 * Copyright (C) 2003 Chris Bouzek
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
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.19  2003/06/19 16:17:21  bouzekc
 * Changed the validateParameterGUIs method to work only with
 * variable parameters.
 *
 * Revision 1.18  2003/06/18 22:47:04  bouzekc
 * Added method to automatically validate nearly all
 * ParameterGUIs.
 *
 * Revision 1.17  2003/06/18 19:48:23  bouzekc
 * Instantiated getResult() to provide initialization of
 * PropertyChanger values and provide a hook for superclass
 * parameter checking.
 *
 * Revision 1.16  2003/06/17 20:26:53  bouzekc
 * Updated documentation.
 *
 * Revision 1.15  2003/06/16 23:05:26  bouzekc
 * Now implements PropertyChanger.
 *
 * Revision 1.14  2003/06/09 22:09:08  bouzekc
 * Added constructor for setting HAS_CONSTANTS on
 * initialization, and removed the method which previously
 * set them.
 *
 * Revision 1.13  2003/06/09 14:50:44  bouzekc
 * Changed errorOut() to return ErrorStrings rather than
 * Booleans.
 *
 * Revision 1.12  2003/06/05 22:19:42  bouzekc
 * Added code so that subclasses can more easily write
 * error messages related to incorrect parameters.
 * Added code to allow selective setting of constant
 * parameters by external classes.
 *
 * Revision 1.11  2003/05/16 15:30:48  pfpeterson
 * Removed a redundant call to setDefaultParameters() immediately after
 * super(String).
 *
 * Revision 1.10  2003/04/24 18:55:32  pfpeterson
 * Added functionality to save Wizards plus code cleanup. (Chris Bouzek)
 *
 * Revision 1.9  2003/04/02 14:54:49  pfpeterson
 * Major reworking to reflect that Form now subclasses Operator. (Chris Bouzek)
 *
 * Revision 1.8  2003/03/19 15:01:30  pfpeterson
 * Also registers property change events from the parameters with
 * the wizard as well. (Chris Bouzek)
 *
 * Revision 1.7  2003/02/26 21:45:20  pfpeterson
 * Changed setCompleted(false) to invalidate() and removed the completed
 * variable b/c it's value isn't reliable.
 *
 * Revision 1.6  2003/02/26 17:20:03  rmikk
 * Writes message to DataSetTools.util.SharedData.status_pane
 *
 * Revision 1.5  2002/11/27 23:26:33  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/08/05 19:12:01  pfpeterson
 * Switched the default layout of the parameters in a form to be grid.
 *
 * Revision 1.3  2002/06/11 14:56:17  pfpeterson
 * Small updates to documentation.
 *
 * Revision 1.2  2002/06/06 16:15:44  pfpeterson
 * Now use new parameters.
 *
 * Revision 1.1  2002/05/28 20:35:59  pfpeterson
 * Moved files
 *
 * Revision 1.3  2002/04/11 22:34:35  pfpeterson
 * Big changes including:
 *   - new GUI (layout works better)
 *   - filled out done() method
 *   - if setCompleted(false) invalidate results in this form.
 *
 * Revision 1.2  2002/03/12 16:09:44  pfpeterson
 * Now automatically disable constant and result parameters.
 *
 * Revision 1.1  2002/02/27 17:29:20  dennis
 * Form base class that defines one logical step in a sequence
 * of steps controlled by a Wizard.  Derived classes will actually
 * provide meaningful operations.
 *
 *
 */

package DataSetTools.wizard;

import java.io.Serializable;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.Color;
import java.awt.GridLayout;
import java.beans.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import DataSetTools.util.*;
import DataSetTools.components.ParametersGUI.PropChangeProgressBar;
import DataSetTools.dataset.DataSet;
import java.io.File;
import java.util.Vector;

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
 *  @see Wizard.MathWizard 
 *  @see Wizard.AdderExampleForm
 */

/**
 *  Note that Forms are set up by default as standalone Forms, or the first Form
 *  in a Wizard.  To set a Form to have constant parameters that rely on 
 *  values obtained from previous Forms, set the HAS_CONSTANTS variable to 
 *  true by using the appropriate constructor.
 */
public abstract class Form extends Operator implements Serializable,
                                                       PropertyChanger{
  private final boolean DEBUG=false;

  protected JPanel    panel;               // panel that the Wizard will draw
  
  private static final String CONS_FRAME_HEAD = "CONSTANT PARAMETERS";
  private static final String VAR_FRAME_HEAD  = "USER SPECIFIED PARAMETERS";
  private static final String RES_FRAME_HEAD  = "RESULTS";
  public static final String[] PARAM_NAMES =
                             {CONS_FRAME_HEAD, VAR_FRAME_HEAD, RES_FRAME_HEAD};

  public static final int CONST_PARAM  = 0;
  public static final int VAR_PARAM    = 1;
  public static final int RESULT_PARAM = 2;

  private int[][]  param_ref = null;

  //used for standalone or first Forms.  Default is standalone.
  protected boolean HAS_CONSTANTS = false; 

  protected PropertyChangeSupport propBind;

  //used for the progress bars
  protected float newPercent, oldPercent, increment;

  /**
   *  Construct a form with the given title to work with 
   *  the specified Wizard.  
   *
   *  @param  title           The title to show on this form
   *
   */
  public Form( String title )
  {
    super(title);
    panel = null;
    this.param_ref=null;
    propBind = new PropertyChangeSupport(this);
  } 

  /**
   *  Construct a form with the given title to work with 
   *  the specified Wizard.  This constructor also allows
   *  for setting whether or not this Form has constant 
   *  parameters.
   *
   *  @param  title           The title to show on this form
   *
   */
  public Form( String title, boolean hasConstantParams )
  {
    this(title);
    this.HAS_CONSTANTS = hasConstantParams;
  } 

  /* ---------------------------- addParameter ---------------------------- */
  /**
   * Add the reference for the specified parameter to the list of parameters 
   * for this operation object.  
   *
   *  @param   iparam   The new IParameterGUI to be added to the list
   *                    of parameters for this object.
   */
  protected void addParameter(IParameterGUI iparam)
  {
    parameters.addElement(iparam);
  }

  /* ---------------------------- setParameter --------------------------- */
  /**
   * Set the parameter at the specified index in the list of parameters
   * for this Form.  The parameter that is set MUST have the same type
   * of value object as that was originally placed in the list of parameters
   * using the addParameter() method.  
   *
   *  @param  index    The index in the list of parameters of the parameter
   *                   that is to be set.  "index" must be between 0 and the
   *                   number of parameters - 1.
   *
   *  @return  Returns true if the parameter was properly set, and returns 
   *           false otherwise.  Specifically, it returns false if either
   *           the given index is invalid, or the specified parameter
   *           has a different data type than the parameter at the given
   *           index.
   */
  public boolean setParameter(IParameterGUI iparam, int index)
  {
    return super.setParameter(iparam, index);
  }

  public boolean setParameter(IParameter iparam, int index)
  {
    if( iparam instanceof IParameterGUI )
      return this.setParameter((IParameterGUI)iparam,index);
    else
      return false;
  }

  /**
   * This method takes care of the setting up the gui to build in.
   *
   * @param container where all of the gui components will be packed
   * into.
   */
  protected final void prepGUI(java.awt.Container container){
    if(panel==null) panel=new JPanel();
    panel.removeAll();
    panel.add(container);
  }

  /**
   *  This method makes the GUI for the Form.  If a derived class
   *  overrides this method, it must build it's own user interface in
   *  the current JPanel, panel, since that is what is returned to the
   *  Wizard to show the form.  Also, this method is NOT just called
   *  at construction time, but is called each time the Form is shown
   *  by the Wizard.  This guarantees that the parameter values will be
   *  properly displayed using their current values.
   *  @see Form#prepGUI(java.awt.Container) prepGUI
   *  @see Form#enableParameters() enableParameters
   */
  protected void makeGUI(){
      if(DEBUG)System.out.println("IN makeGUI of "+this.getCommand());
      Box box = new Box( BoxLayout.Y_AXIS );
      if(DEBUG) box.setBackground(Color.red);
      prepGUI(box);
      JPanel sub_panel;
      for(int i = 0; i < param_ref.length; i++)
      {
        if(param_ref[i]!=null && param_ref[i].length > 0){
          // build the sub_panels
          sub_panel = build_param_panel(PARAM_NAMES[i], param_ref[i]);
          if ( sub_panel != null )
            box.add( sub_panel );
        }
      }

      this.enableParameters();
  }

  /**
   * Sets the enable/disable state of the parameters according to the
   * types declared using {@link #setParamTypes(int[],int[],int[])
   * setParamTypes}.
   */
  protected final void enableParameters(){
      IParameterGUI param=null;
      boolean enable=false;

      for(int i = 0; i < param_ref.length; i++)
      {
        if(param_ref[i]!=null && param_ref[i].length > 0){
          enable=(i==VAR_PARAM); // only editable_params should be enabled
          if(DEBUG) System.out.print(PARAM_NAMES[i]+"("+enable+")");
          for( int j=0 ; j<param_ref[i].length ; j++ ){
            ((IParameterGUI)getParameter(param_ref[i][j])).setEnabled(enable);
            if(DEBUG) System.out.print(param_ref[i][j]+" ");
          }
          if(DEBUG)System.out.println();
        }
      }
    
  }

  /**
   * Method to set the parameter types. If you don't want one set then
   * pass in null or a zero length array.
   */
  protected final void setParamTypes(int[] constant, int[] variable,
                                                                 int[] result){
    if(constant==null || constant.length<=0)
      constant=null;
    if(variable==null || variable.length<=0)
      variable=null;
    if(result==null || result.length<=0)
      result=null;

    param_ref=new int[][]{constant,variable,result};
  }

  /**
   * Returns the array of indices for the different types of parameters
   */
  protected final int[] getParamType(int type){
    if(type==CONST_PARAM)
      return param_ref[CONST_PARAM];
    else if(type==VAR_PARAM)
      return param_ref[VAR_PARAM];
    else if(type==RESULT_PARAM)
      return param_ref[RESULT_PARAM];
    else
      throw new IndexOutOfBoundsException("Invalid type specified");
  }

  /**
   *  Returns the array of indices for the variable parameters.
   */
  public final int[] getVarParamIndices()
  {
    return this.getParamType(VAR_PARAM);
  }

  /**
   *  This builds the portions of the default form panel that contain a
   *  list of parameters inside of a panel with a titled border.  It is
   *  used to build up to three portions, corresponding to the constant,
   *  user-specified and result parameters.
   *
   *  @param  title  The title to put on the border
   *  @param  num    The reference to the particular parameters (i.e 
   *                 editable, result, or constant) within the 
   *                 parameters Vector.
   */
  protected final JPanel build_param_panel( String title, int num[])
  {
    if( getNum_parameters()<=0 ) return null;

    JPanel       sub_panel = new JPanel();
    TitledBorder border;
    border = new TitledBorder(LineBorder.createBlackLineBorder(), title);
    //border.setTitleFont( FontUtil.BORDER_FONT );
    sub_panel.setBorder( border );
    sub_panel.setLayout( new GridLayout( num.length, 1 ) );
    for ( int i = 0; i < num.length; i++ ){
        IParameterGUI param = (IParameterGUI)getParameter(num[i]);
        param.init();
        sub_panel.add( param.getGUIPanel() );
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
    if(panel==null) panel=new JPanel();

    return panel;
  }

  /**
   *
   *  This is called when displaying or hiding the current form.  While
   *  this currently only sets the form to be visible or not, additional
   *  functionality could be added so that it will ensure that the 
   *  Form's execute() method is called before advancing to the next 
   *  Form.  Replaces show() and hide().
   *
   *  @param boolean show true when you want the form to show, false
   *                      when you do not.
   */
  public void setVisible(boolean show)
  {
    if(show){
      this.makeGUI();
      panel.validate();
      panel.setVisible(show);
    }else{
      if(this.panel!=null)
        panel.setVisible(show);
    }
  }

  /**
   *  Check whether or not this form has been successfully completed.
   * 
   *  @return true if the form's operation has been successfully carried out
   *               and none of the parameters have been subsequently altered 
   */
  public boolean done(){
      int areSet=0;
      int totalParam=getNum_parameters();

      if(totalParam<=0) return false;

      for(int i = 0; i < totalParam; i++)
        if( ((IParameterGUI)this.getParameter(i)).getValid() )
          areSet++;

      return (areSet==totalParam);
  }

  /**
   *  Sets the valid state of all result parameters to false.
   */
  public void invalidate(){
    if(DEBUG)
      System.out.println("invalidate");
    if(this.getNum_parameters()<=0) return;

    int[] result_indices =this.getParamType(RESULT_PARAM);
    if( result_indices==null || result_indices.length<=0) return;

    for( int i=0 ; i<result_indices.length ; i++ )
      ((IParameterGUI)getParameter(result_indices[i])).setValid(false);
  }
   
  /**
   *  Method to add PropertyChangeListeners to the Form's list
   *  of parameters.
   */
  public void addParameterPropertyChangeListener(PropertyChangeListener w) 
  {
    IParameterGUI param;

    if(this.getNum_parameters()<=0) return;
    int[] var_indices =this.getParamType(VAR_PARAM);
    if( var_indices==null || var_indices.length<=0) return;

    for( int i=0 ; i<var_indices.length ; i++ )
    {
      param = (IParameterGUI)this.getParameter(var_indices[i]); 
      if(param instanceof PropertyChanger){
        ((PropertyChanger)param)
          .addPropertyChangeListener(IParameter.VALUE,w);
      }
    }
  }

  /**
   *  Convenience method for subclassed Forms to return an
   *  "invalid" message to the Wizard, and output an appropriate
   *  error message to the user.
   *
   *  @param  errmessage           The error message that you want the user
   *                               to see.
   *
   *  @return                      A new ErrorString containing the error
   *                               message.
   */
  protected Object errorOut(Object errmessage)
  {
   String message;
   if( errmessage instanceof String )
   {
     message = "FORM ERROR: " + errmessage;
     SharedData.addmsg(message);
   }
   else
   {
     message = "FORM ERROR: " + errmessage.toString();
     SharedData.addmsg(message);
   }
   return new ErrorString(message);
  }

  /**
   *  Convenience method for subclassed Forms to return an
   *  "invalid" message to the Wizard, and output an appropriate
   *  error message to the user as well as invalidating a parameter.
   *
   *  @param  param                The IParameterGUI to set invalid.
   *
   *  @param  errmessage           The error message that you want the user
   *                               to see.
   *
   *  @return                      A new ErrorString containing the error
   *                               message.
   */
  protected Object errorOut(IParameterGUI param, Object errmessage)
  {
   param.setValid(false);
   return this.errorOut(errmessage);
  }

  /**
   *  Overridden to some functionality for child Forms.
   *
   *  @return     The result of validateParameterGUIs(), which is either
   *              Boolean.TRUE or an ErrorString, depending on the whether the
   *              parameters successfully validated or not, respectively.
   */
  public Object getResult()
  {
    //for progress bars
    newPercent = oldPercent = increment = 0;
    //not created yet
    return this.validateParameterGUIs();
  }

  /**
   *  Convenience method for checking variable parameters.  Although it 
   *  can be overwritten to provide a more customized approach to validating 
   *  parameters, this should not usually be necessary, as the 
   *  recommended approach is to retrieve all parameters, validate
   *  them using this method, then perform any special validations
   *  directly in the child class.
   *  
   *  @return     Either Boolean.TRUE or an ErrorString, depending on the 
   *              whether the parameters successfully validated or not, 
   *              respectively.
   */
  protected Object validateParameterGUIs()
  {
    IParameterGUI ipg;
    Object obj;

    if(this.getNum_parameters() <= 0) 
      return new ErrorString("No parameters to check");
    int[] var_indices = this.getParamType(VAR_PARAM);
    if( var_indices == null || var_indices.length <= 0) 
      return new ErrorString("No variable parameters to check");

    for( int i = 0; i < var_indices.length; i++ ){
      ipg = (IParameterGUI)this.getParameter(var_indices[i]); 
      if(ipg.getValid() == true)
        continue;

      if(ipg instanceof DataSetPG){
        obj = ipg.getValue();
        if(obj != null && obj != DataSet.EMPTY_DATA_SET)
          ipg.setValid(true);
        else
          return errorOut(ipg, 
            "Parameter " + ipg.getName() + " is invalid.");
      }
      else if(ipg instanceof BrowsePG){ 
        if(new File(ipg.getValue().toString()).exists())
          ipg.setValid(true);
        else
          return errorOut(ipg, 
            "Parameter " + ipg.getName() + " is invalid.");
      }
      else if(ipg instanceof ArrayPG){
        Vector v = (Vector)(ipg.getValue());
        if(v == null || v.isEmpty())
          ipg.setValid(false);
        else{  //assume it is valid, then test that assumption
          ipg.setValid(true);
          for(int k = 0; k < v.size(); k++){
            if( !(new File( v.elementAt(k).toString() ).exists()) ){
              ipg.setValid(false);
              break;
            }
          }
        }
      }
      else if(ipg instanceof BooleanPG)
        ipg.setValid(true);
      else if(ipg instanceof StringEntryPG){
        //need to check input against the StringFilterer
        StringFilterer sf = 
          ((StringEntryPG)ipg).getStringFilter();
        if(sf.isOkay(0, ipg.getValue().toString(), ""))
          ipg.setValid(true);
        else
          return errorOut(ipg, 
            "Parameter " + ipg.getName() + " is invalid.");
      }

      //for the remainder of the parameters, we will set them false.
      //HashPG can't really be checked - if the value is not found, the
      //value is set to a blank.  This is the default behavior of the JComboBox
      //that HashPG's HashEntry is built on.
      //ChooserPG's really can't be checked - they have a built in
      //mechanism to add non-existing values.
      //VectorPG does not have a clean way to determine its validity.
      //To be safe we are going to let the subclasses work with these 
      //parameters to validate them, but we will not return an error.
      else
        ipg.setValid(false);
    }
    return Boolean.TRUE;
  }

  /* -------------------- PropertyChanger methods --------------------------*/

  /**
   *  Adds the property change listener pcl to this Form's 
   *  PropertyChangeSupport propBind variable.
   */
  public void addPropertyChangeListener(String property, 
                                        PropertyChangeListener pcl)
  {
    if(propBind != null)
      propBind.addPropertyChangeListener(property, pcl);
  }

  /**
   *  Adds the property change listener pcl to this Form's 
   *  PropertyChangeSupport propBind variable.
   */
  public void addPropertyChangeListener(PropertyChangeListener pcl)
  {
    if(propBind != null)
      propBind.addPropertyChangeListener(pcl);
  }

  /**
   *  Removes the property change listener pcl from this Form's 
   *  PropertyChangeSupport propBind variable.
   */
  public void removePropertyChangeListener(PropertyChangeListener pcl)
  {
    if(propBind != null)
      propBind.removePropertyChangeListener(pcl);
  }

  /**
   *  Utility method to fire property change events.
   */
  protected void fireValueChangeEvent(int oldValue, int newValue)
  {
    if( propBind != null && oldValue != newValue )
    propBind.firePropertyChange(PropChangeProgressBar.VALUE, 
                                     oldValue, newValue);                     
  }
}

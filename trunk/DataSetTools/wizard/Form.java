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
import DataSetTools.util.PropertyChanger;

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

public abstract class Form extends Operator implements Serializable{
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
  /**
   *  Construct a form with the given title to work with 
   *  the specified Wizard.  Note that the integer
   *  arrays const_params, editable_params, and result_params
   *  are used to provide references within the parameter Vector
   *  so that the Form can properly call makeGUI().  If a subclass
   *  overwrites this method, it must follow the same protocol.
   *
   *  @param  title           The title to show on this form
   *
   */
  public Form( String title )
  {
    super(title);
    panel = null;
    this.param_ref=null;
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
}

/*
 * File:  OperatorForm.java
 *
 * Copyright (C) 2003, Christopher M. Bouzek
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
 *           Christopher M. Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 */

package DataSetTools.wizard;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import DataSetTools.util.*;
import DataSetTools.components.ParametersGUI.*;

/**
  * The OperatorForm class is an extension of Form designed to work 
  * with an Operator Object.  Although a Form is an Operator, 
  * by creating an OperatorForm, it becomes easier to implement
  * many of the methods.
 */

public class OperatorForm extends Form implements Serializable, HiddenOperator{
  protected Operator form_op;
  protected IParameterGUI result_param;

  /**
   *  Construct an OperatorForm with the given title.  
   *
   *  @param  title           The title to show on this form
   *
   */
  public OperatorForm()
  {
    super("Operator Form");   
    this.result_param=null;
    this.setDefaultParameters();
  } 

  /**
   *  Construct an OperatorForm with the given title and Operator.
   *  This allows the use of that Operator for the getResult()
   *  method.  
   *
   *  @param  title           The title to show on this form
   *
   */
  public OperatorForm( Operator op)
  {
    super(op.getTitle());
    form_op = op;
    this.setDefaultParameters();
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor
   *
   */
   public String getCommand()
   {
     return form_op.getCommand();
   }

  /* ----------------------------- getResult ---------------------------- */
  /**
   * Returns the object that is the result of applying this operation.
   * This should be called after setting the appropriate parameters.
   * Derived classes will override this method with code that will
   * carry out the required operation.
   *
   * @return  The result of carrying out this operation is returned as a Java
   *          Object.
   */ 
   public Object getResult()
   {
     Object result=form_op.getResult();
     
     if(result instanceof ErrorString){
       this.result_param.setValue(null);
       SharedData.addmsg("ERROR: " + result);
       return Boolean.FALSE;
     }
     
     /*if we have a String result and it has a '.' in it, it is probably
       a file.*/
     if(result instanceof String && 
        ( ((String)result).indexOf('.') ) >= 0){
       String indexedString = (String)result;
       
       //assume that we have a loadable/viewable file name
       result_param = new BrowsePG("Result", indexedString, true);
     }
     else  //something else we can't handle as a file
       this.result_param.setValue(result);
       
     SharedData.addmsg("Success!\n" + result.toString());

     //validate the parameters...if we got this far, assume
     //that our parameters were OK.
     for(int i = 0; i < getNum_parameters(); i++ )
       ((IParameterGUI)getParameter(i)).setValid(true);
     return result;
   } 

  /* -------------------------- setDefaultParameters ----------------------- */
  /**
   *  Set the parameters to default values using the form_op Operator.
   */
   public void setDefaultParameters()
   {
     // can't do anything without an operator
     if(form_op==null) return;

     // set the result parameter
     result_param=new StringPG("Result",null,false);

     //set the variable parameters length and indices
     int num_params, var_indices[];
     num_params = form_op.getNum_parameters();
     var_indices = new int[num_params];   

     // initialize the variable indices and tell parameters to show valid
     for(int i = 0; i < num_params; i++){
       var_indices[i] = i; // create index
       ((IParameterGUI)this.getParameter(i)).setDrawValid(true);
     }

     //set the parameter types so we can build the GUI
     super.setParamTypes(null, var_indices, new int[]{num_params});
   }

   /* ---------------------------- addParameter ---------------------------- */
   /**
    * Add the specified parameter to the list of parameters for this Form.  
    * This method will typically be called by the constructor for the
    * derived class.
    *
    *  @param   parameter   The new IParameteterGUI to be added to this Form.
    */
   protected void addParameter( IParameter parameter )
   {
       //form_op.addParameter(parameter);
   }

  /* ---------------------------- getNum_parameters ------------------------ */
  /**
   * Gets the number of parameters for this Form.
   *
   *  @return  Returns the number of parameters that this operator has.
   */
  public int getNum_parameters()
  {
    if(form_op!=null)
      return form_op.getNum_parameters()+1;
    else
      return 0;
  }

  /* ----------------------------- getParameter -------------------------- */
  /**
   * Get the parameter at the specified index from the list of parameters
   * for this Form.  Note: This returns a reference to the specified
   * parameter.  Consequently the value of the parameter can be altered.
   *
   *  @param  index    The index in the list of parameters of the parameter
   *                   that is to be returned.  "index" must be between 0 and 
   *                   the number of parameters - 1.
   *
   *  @return  Returns the parameters at the specified position in the list
   *           of parameters for this object.  If the index is invalid,
   *           this returns null.
   */
  public IParameter getParameter( int index )
  {
    if(index<form_op.getNum_parameters())
      return form_op.getParameter(index);
    else
      return result_param;
  }

  /* ---------------------------- setParameter --------------------------- */
  /**
   * Set the parameter at the specified index in the list of parameters
   * for this Form.  The parameter that is set MUST have the same type
   * of value object as that was originally placed in the list of parameters
   * using the addParameter() method.  Typically, the "GUI" will get a parameter
   * from the operator, change its value and then set the parameter back at
   * the same index.
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
  public boolean setParameter( IParameter parameter, int index )
  {
    if(index<0 || index>=this.getNum_parameters()) return false;

    if(index<form_op.getNum_parameters()){
      return form_op.setParameter(parameter, index);
    }else{
      try{
        result_param=(IParameterGUI)parameter;
        return true;
      }catch(ClassCastException e){
        return false;
      }
    }
  } 
}




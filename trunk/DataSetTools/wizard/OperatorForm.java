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
 *
 * $Log$
 * Revision 1.12  2003/07/02 15:35:32  bouzekc
 * Fixed javadoc error.
 *
 * Revision 1.11  2003/06/27 22:23:42  bouzekc
 * Reformatted for consistency.
 *
 * Revision 1.10  2003/06/27 22:23:06  bouzekc
 * Added log header and class javadoc comments.
 *
 */
package DataSetTools.wizard;

import Command.ParameterClassList;

import DataSetTools.components.ParametersGUI.*;

import DataSetTools.operator.*;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;


/**
 * The OperatorForm class is an extension of Form designed to work
 * with an Operator Object.  Although a Form is an Operator,
 * by creating an OperatorForm, it becomes easier to implement
 * many of the methods.  In addition, an OperatorForm has the advantage of
 * automated parameter linking, so it can "return" more than one value.
 */
public class OperatorForm extends Form implements Serializable, HiddenOperator {
  private static ParameterClassList PL = null;
  protected Operator form_op;
  protected IParameterGUI result_param;
  private int[] constIndices;

  /**
   *  Construct an OperatorForm with the given title.
   *
   *  @param  title           The title to show on this form
   *
   */
  public OperatorForm(  ) {
    super( "Operator Form" );
    this.result_param = null;
    this.setDefaultParameters(  );
  }

  /**
   *  Construct an OperatorForm with the given Operator.
   *  This allows the use of that Operator for the getResult()
   *  method.
   *
   *  @param  op             The Operator to use for this form
   *
   */
  public OperatorForm( Operator op ) {
    super( op.getTitle(  ) );
    form_op = op;
    this.setDefaultParameters(  );
  }

  /**
   *  Construct an OperatorForm with the given Operator and
   *  result parameter type.
   *  This allows the use of that Operator for the getResult()
   *  method.
   *
   *  @param  op              The Operator to use for this form
   *
   *  @param  type            The IParameterGUI type of the result
   *                          parameter.  e.g. for a LoadFilePG,
   *                          use "LoadFile"
   *
   *  @param  name            The name of the result parameter.
   *                          e.g. "log file"
   *
   */
  public OperatorForm( Operator op, String type, String name ) {
    this( op );
    this.setParamClass( type );
    result_param.setName( name );
  }

  /**
   *  Construct an OperatorForm with the given Operator and
   *  result parameter type.
   *  This allows the use of that Operator for the getResult()
   *  method.  In addition, this constructor allows setting of the constant
   *  parameters.
   *
   *  @param  op              The Operator to use for this form
   *
   *  @param  type            The IParameterGUI type of the result
   *                          parameter.  e.g. for a LoadFilePG,
   *                          use "LoadFile"
   *
   *  @param  name            The name of the result parameter.
   *                          e.g. "log file"
   *
   *  @param indices          The array of indices that represent constant
   *                          parameters for this Form.
   *
   *
   */
  public OperatorForm( Operator op, String type, String name, int[] indices ) {
    this( op );
    this.setParamClass( type );
    result_param.setName( name );
    super.HAS_CONSTANTS   = true;
    this.constIndices     = indices;
    this.setDefaultParameters(  );
  }

  /* ---------------------------- getCommand ------------------------------- */

  /**
   * @return  the command name to be used with script processor
   *
   */
  public String getCommand(  ) {
    return form_op.getCommand(  );
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
  public Object getResult(  ) {
    Object result = form_op.getResult(  );

    //Operator failed - exit out
    if( result instanceof ErrorString ) {
      this.result_param.setValue( null );

      return errorOut( result.toString(  ) );
    }

    this.result_param.setValue( result );

    if( result != null ) {
      SharedData.addmsg( "Success! " + result.toString(  ) );
    } else {
      SharedData.addmsg( "Success!" );
    }

    //validate the parameters...if we got this far, assume
    //that our parameters were OK.
    for( int i = 0; i < getNum_parameters(  ); i++ ) {
      ( ( IParameterGUI )getParameter( i ) ).setValid( true );
    }

    return result;
  }

  /* -------------------------- setDefaultParameters ----------------------- */

  /**
   *  Set the parameters to default values using the form_op Operator.
   */
  public void setDefaultParameters(  ) {
    // can't do anything without an operator
    if( form_op == null ) {
      return;
    }

    Vector temp = null;  //may need temporary index storage

    // set the result parameter
    result_param = new StringPG( "Result", null, false );

    //set the variable parameters length and indices
    int num_params;

    //set the variable parameters length and indices
    int[] var_indices;

    num_params = form_op.getNum_parameters(  );

    boolean isConstant = false;

    if( HAS_CONSTANTS ) {
      var_indices = new int[num_params - constIndices.length];
    } else {
      var_indices = new int[num_params];
    }

    // initialize the variable indices and tell parameters to show valid
    for( int i = 0; i < num_params; i++ ) {
      if( HAS_CONSTANTS ) {
        if( temp == null ) {
          temp = new Vector( var_indices.length, 2 );
        }

        isConstant = false;

        for( int j = 0; j < constIndices.length; j++ ) {
          if( i == constIndices[j] ) {  //found a constant parameter index
            isConstant = true;

            break;
          }
        }

        if( !isConstant ) {  //non-constant parameter
          temp.add( new Integer( i ) );
        }
      }
    }

    if( HAS_CONSTANTS ) {
      //create an Integer array and copy it over to the variable indices
      Object[] tempIndices = temp.toArray(  );

      for( int i = 0; i < var_indices.length; i++ ) {
        var_indices[i] = ( ( Integer )tempIndices[i] ).intValue(  );
      }
    } else {  //no constant parameters

      for( int i = 0; i < num_params; i++ ) {
        var_indices[i] = i;
      }
    }

    //set the parameters to be drawn
    for( int i = 0; i < num_params; i++ ) {
      ( ( IParameterGUI )this.getParameter( i ) ).setDrawValid( true );
    }

    /*set the parameter types so we can build the GUI
       the result parameter is one after the last variable parameter
       and so we'll set it to num_params.*/
    if( HAS_CONSTANTS ) {
      super.setParamTypes( constIndices, var_indices, new int[]{ num_params } );
    } else {
      super.setParamTypes( null, var_indices, new int[]{ num_params } );
    }
  }

  /**
   *  Method to allow OperatorForms to set their constant parameters indices.
   *
   *  @param  indices     Array of integers indicating which parameters should
   *                      be constant.
   */
  public void setConstantParamIndices( int[] indices ) {
    super.HAS_CONSTANTS   = true;
    this.constIndices     = indices;
  }

  /* ---------------------------- getNum_parameters ------------------------ */

  /**
   * Gets the number of parameters for this Form.
   *
   *  @return  Returns the number of parameters that this operator has.
   */
  public int getNum_parameters(  ) {
    if( form_op != null ) {
      return form_op.getNum_parameters(  ) + 1;
    } else {
      return 0;
    }
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
  public IParameter getParameter( int index ) {
    if( index < form_op.getNum_parameters(  ) ) {
      return form_op.getParameter( index );
    } else {
      return result_param;
    }
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
  public boolean setParameter( IParameter parameter, int index ) {
    if( ( index < 0 ) || ( index >= this.getNum_parameters(  ) ) ) {
      return false;
    }

    if( index < form_op.getNum_parameters(  ) ) {
      return form_op.setParameter( parameter, index );
    } else {
      try {
        result_param = ( IParameterGUI )parameter;

        return true;
      } catch( ClassCastException e ) {
        return false;
      }
    }
  }

  /**
   *  Used to set the class type of the result parameter by
   *  passing in the type (as determined by getType()) of the
   *  IParameterGUI.
   */
  private void setParamClass( String type ) {
    try {
      if( PL == null ) {
        PL = new ParameterClassList(  );
      }

      result_param = ( IParameterGUI )( PL.getInstance( type ) );
    } catch( ClassCastException cce ) {
      SharedData.addmsg( 
        "ERROR: You must pass an IParameterGUI (not a IParameter)to a Form." );
    }
  }
}

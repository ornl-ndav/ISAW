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
 * Revision 1.30  2003/10/29 02:48:59  bouzekc
 * Redesign.  Now uses its own parameter list, with the result parameter as the
 * parameter.  Removed several methods that became obsolete with this change.
 *
 * Revision 1.29  2003/10/15 03:38:05  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.28  2003/10/11 20:46:08  bouzekc
 * Fixed bug where the result parameter did not properly validate.
 *
 * Revision 1.27  2003/09/11 21:21:30  bouzekc
 * Updated to work with new Form class.
 *
 * Revision 1.26  2003/07/09 23:06:26  bouzekc
 * Now uses validateParameterGUIs() in getResult().
 *
 * Revision 1.25  2003/07/09 22:24:28  bouzekc
 * Now overrides getTitle().
 *
 * Revision 1.24  2003/07/09 21:19:56  bouzekc
 * Added getDocumentation() to return the internal Operators
 * documentation.
 *
 * Revision 1.23  2003/07/07 20:35:14  bouzekc
 * Now implicitly sets HAS_CONSTANTS by way of
 * setConstantParamIndices().  setConstantParamIndices() no
 * longer allows resetting of the constant parameter indices.
 *
 * Revision 1.22  2003/07/03 14:12:02  bouzekc
 * Added all missing javadoc comments and formatted existing
 * comments.
 *
 * Revision 1.21  2003/07/02 22:53:39  bouzekc
 * Sorted methods according to access rights.
 *
 * Revision 1.20  2003/07/02 20:09:48  bouzekc
 * Removed unused import statement.
 *
 * Revision 1.19  2003/07/02 18:27:44  bouzekc
 * Removed unused imports.
 *
 * Revision 1.18  2003/07/02 17:05:10  bouzekc
 * Fixed bug where if setDefaultParameters() was called after
 * setting the result parameter name and type, the result
 * parameter return to a StringPG with a name of "Result."
 *
 * Revision 1.17  2003/07/02 16:26:46  bouzekc
 * Returned const_indices to private status.
 *
 * Revision 1.16  2003/07/02 16:21:02  bouzekc
 * Made const_indices and setParamClass protected so
 * ScriptForm can use them.
 *
 * Revision 1.15  2003/07/02 16:07:54  bouzekc
 * Removed unnecessary references to "this".
 *
 * Revision 1.14  2003/07/02 16:00:30  bouzekc
 * Fixed javadoc error, added constructor for creating an
 * OperatorForm with a given title.
 *
 * Revision 1.13  2003/07/02 15:37:38  bouzekc
 * No longer implements Serializable.
 *
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

import DataSetTools.operator.HiddenOperator;
import DataSetTools.operator.Operator;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import java.util.Vector;


/**
 * The OperatorForm class is an extension of Form designed to work with an
 * Operator Object.  Although a Form is an Operator, by creating an
 * OperatorForm, it becomes easier to implement many of the methods.  In
 * addition, an OperatorForm has the advantage of automated parameter linking,
 * so it can "return" more than one value.
 */
public class OperatorForm extends Form implements HiddenOperator {
  //~ Static fields/initializers ***********************************************

  private static ParameterClassList PL = null;

  //~ Instance fields **********************************************************

  protected Operator form_op;
  protected IParameterGUI result_param;
  private int[] constIndices;

  //~ Constructors *************************************************************

  /**
   * Construct an OperatorForm with the title "Operator Form".
   */
  public OperatorForm(  ) {
    super( "Operator Form" );
    result_param = null;
    setDefaultParameters(  );
  }

  /**
   * Construct an OperatorForm with the given title.
   *
   * @param title The title to show on this form
   */
  public OperatorForm( String title ) {
    super( title );
    result_param = null;
    setDefaultParameters(  );
  }

  /**
   * Construct an OperatorForm with the given Operator. This allows the use of
   * that Operator for the getResult() method.
   *
   * @param op The Operator to use for this form
   */
  public OperatorForm( Operator op ) {
    super( op.getTitle(  ) );
    form_op = op;
    setDefaultParameters(  );
  }

  /**
   * Construct an OperatorForm with the given Operator and result parameter
   * type. This allows the use of that Operator for the getResult() method.
   *
   * @param op The Operator to use for this form
   * @param type The IParameterGUI type of the result parameter.  e.g. for a
   *        LoadFilePG, use "LoadFile"
   * @param name The name of the result parameter. e.g. "log file"
   */
  public OperatorForm( Operator op, String type, String name ) {
    this( op );
    setParamClass( type );
    result_param.setName( name );
    setDefaultParameters(  );
  }

  /**
   * Construct an OperatorForm with the given Operator and result parameter
   * type. This allows the use of that Operator for the getResult() method. In
   * addition, this constructor allows setting of the constant parameters.
   *
   * @param op The Operator to use for this form
   * @param type The IParameterGUI type of the result parameter.  e.g. for a
   *        LoadFilePG, use "LoadFile"
   * @param name The name of the result parameter. e.g. "log file"
   * @param indices The array of indices that represent constant parameters for
   *        this Form.
   */
  public OperatorForm( Operator op, String type, String name, int[] indices ) {
    this( op );
    setParamClass( type );
    result_param.setName( name );
    setConstantParamIndices( indices );
    setDefaultParameters(  );
  }

  //~ Methods ******************************************************************

  /**
   * @return the command name to be used with script processor
   */
  public String getCommand(  ) {
    return form_op.getCommand(  );
  }

  /**
   * Method to allow OperatorForms to set their constant parameters indices.
   * Does not allow resetting once the constant parameter indices have been
   * set.
   *
   * @param indices Array of integers indicating which parameters should be
   *        constant.
   */
  public void setConstantParamIndices( int[] indices ) {
    if( constIndices == null ) {
      HAS_CONSTANTS   = true;
      constIndices    = indices;
    }
  }

  /**
   * Set the parameters to default values using the form_op Operator.
   */
  public void setDefaultParameters(  ) {
    // can't do anything without an operator
    if( form_op == null ) {
      return;
    }

    Vector temp = null;  //may need temporary index storage

    // set the result parameter
    if( result_param == null ) {
      result_param = new StringPG( "Result", null, false );
    }

    //acquire the number of internal operator parameters
    int numOpParams;
    numOpParams = form_op.getNum_parameters(  );

    //set the variable parameters length and indices
    int[] var_indices;

    //if we are told that we have constant parameters, determine the indices of
    //the parameters that are variable.  Otherwise, consider them all variable.
    if( HAS_CONSTANTS ) {
      var_indices = new int[numOpParams - constIndices.length];
    } else {
      var_indices = new int[numOpParams];
    }

    boolean curParamIsConstant = false;

    // initialize the variable indices and tell parameters to show valid
    for( int i = 0; i < numOpParams; i++ ) {
      //if we have constant parameters, set up the storage for them
      if( HAS_CONSTANTS ) {
        if( temp == null ) {
          temp = new Vector( var_indices.length, 2 );
        }

        //reset the flag that tells us if we have a constant parameter
        curParamIsConstant = false;

        //see if the current parameter index is in our constant index list
        for( int j = 0; j < constIndices.length; j++ ) {
          if( i == constIndices[j] ) {  //found a constant parameter index
            curParamIsConstant = true;

            break;
          }
        }

        //not a constant parameter, so add its index to the variable indices
        //list
        if( !curParamIsConstant ) {
          temp.add( new Integer( i ) );
        }
      }
    }

    //load all of the variable parameter indices we found into the variable
    //indices list
    if( HAS_CONSTANTS ) {
      //create an Integer array and copy it over to the variable indices
      Object[] tempIndices = temp.toArray(  );

      for( int i = 0; i < var_indices.length; i++ ) {
        var_indices[i] = ( ( Integer )tempIndices[i] ).intValue(  );
      }
    } else {  //no constant parameters

      for( int i = 0; i < numOpParams; i++ ) {
        var_indices[i] = i;
      }
    }

    //reference all of the internal Operator parameters to this Operator's
    //parameters and add the result parameter
    clearParametersVector(  );

    for( int pNum = 0; pNum < numOpParams; pNum++ ) {
      addParameter( form_op.getParameter( pNum ) );
    }
    addParameter( result_param );

    //draw all of the internal Operator parameters and our result parameter
    for( int i = 0; i < getNum_parameters(  ); i++ ) {
      ( ( IParameterGUI )this.getParameter( i ) ).setDrawValid( true );
    }

    /*set the parameter types so we can build the GUI
       the result parameter is one after the last variable parameter
       and so we'll set it to numOpParams.*/
    if( HAS_CONSTANTS ) {
      setParamTypes( constIndices, var_indices, new int[]{ numOpParams } );
    } else {
      setParamTypes( null, var_indices, new int[]{ numOpParams } );
    }
  }

  /**
   * @return the documentation for this OperatorForm in standard javadoc and
   *         HTML formatting.
   */
  public String getDocumentation(  ) {
    if( form_op != null ) {
      return form_op.getDocumentation(  );
    } else {
      return Operator.DEFAULT_DOCS;
    }
  }

  /**
   * Returns the object that is the result of applying this operation. This
   * should be called after setting the appropriate parameters.
   *
   * @return The result of carrying out this operation is returned as a Java
   *         Object.
   */
  public Object getResult(  ) {
    if( form_op == null ) {
      return null;
    }

    //return early if we can't validate the parameters
    Object allValid = validateSelf(  );

    if( allValid instanceof ErrorString ) {
      return errorOut( allValid.toString(  ) );
    } else if( allValid instanceof Boolean ) {
      if( ( ( Boolean )allValid ).booleanValue(  ) != true ) {
        return errorOut( "Parameters are invalid." );
      }
    }

    //set the value for the internal Operator's parameters to the values that
    //we currently have for this OperatorForm.  Don't include the result
    //parameter
    for( int pNum = 0; pNum < ( getNum_parameters(  ) - 1 ); pNum++ ) {
      form_op.getParameter( pNum )
             .setValue( getParameter( pNum ).getValue(  ) );
    }

    Object result = form_op.getResult(  );

    //Operator failed - exit out
    if( result instanceof ErrorString ) {
      result_param.setValue( null );

      return errorOut( result.toString(  ) );
    }
    result_param.setValue( result );
    result_param.validateSelf(  );

    if( result != null ) {
      SharedData.addmsg( "Success! " + result.toString(  ) );
    } else {
      SharedData.addmsg( "Success!" );
    }

    return result;
  }

  /**
   * Used to set the class type of the result parameter by passing in the type
   * (as determined by getType()) of the IParameterGUI.
   *
   * @param type The type of the IParameterGUI.
   */
  protected void setParamClass( String type ) {
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

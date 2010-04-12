/*
 * File:  DataBlockScalarAdd.java
 *             
 * Copyright (C) 2005, Dennis Mikkelson
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *  $Log$
 *  Revision 1.3  2006/07/10 21:28:23  dennis
 *  Removed unused imports, after refactoring the PG concept.
 *
 *  Revision 1.2  2006/07/10 16:25:57  dennis
 *  Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 *  Revision 1.1  2006/02/02 22:57:07  dennis
 *  Operators to add, subtract, mulitiply and divide a scalar
 *  with one specified Data block in a DataSet.
 *
 */

package DataSetTools.operator.DataSet.Math.Scalar;

import gov.anl.ipns.Parameters.*;
import gov.anl.ipns.Parameters.IParameter;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.viewer.*;
import  DataSetTools.dataset.*;
import  DataSetTools.operator.*;
import  DataSetTools.operator.DataSet.DSOpsImplementation;

/**
  *  Add a constant value to a specified Data block in a DataSet. 
  */

public class DataBlockScalarAdd extends    ScalarOp 
                                implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public DataBlockScalarAdd( )
  {
    super( "Add a Scalar to one Data Block" );
  }

 
  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  value       The value to be added to each point in  
   *                      the specified Data block 
   *  @param  index       The index of the Data block for which the specified
   *                      value will be added to each point in the Data
   *                      block.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the value is just added to the 
   *                      specified Data block of the DataSet "in place".
   */

  public DataBlockScalarAdd( DataSet ds, 
                             float   value, 
                             int     index, 
                             boolean make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s)

    IParameter parameter = getParameter( 0 );
    parameter.setValue( new Float( value ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Integer( index ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on 
  }


  /* ---------------------------- getDocumentation -------------------------- */
 
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This operator adds a constant value to a ");
    Res.append("Data block in a DataSet.  ");
    Res.append("When the operation is successful and a ");
    Res.append("new DataSet is created, a reference to this new DataSet is ");
    Res.append("returned. If a new DataSet is NOT created, the result is ");
    Res.append("stored in the current DataSet and a String indicating the ");
    Res.append("operation was carried out successfully is returned.  ");
    Res.append("If an error occurs and the operation is NOT successful, an ");
    Res.append("ErrorString is returned.");
     
    Res.append("@algorithm If make a new DataSet is selected, construct a new");
    Res.append("DataSet with the same title, units and operations as the ");
    Res.append("current DataSet, add the constant value to each value of the ");
    Res.append("specified Data block and replace it in the new DataSet. ");
    Res.append("If make a new DataSet is not selected, the operation is ");
    Res.append("done in place in the original DataSet.");
    
    Res.append("@param ds - the current DataSet on which the operator will be");
    Res.append(" performed.");
    Res.append("@param value - the value to add to each point in the ");
    Res.append("specified Data block of the current DataSet.");
    Res.append("@param index - the index of the Data block in the DataSet ");
    Res.append("to which the constant value will be added.  ");
    Res.append("@param make_new_ds - a boolean value which determines if a ");
    Res.append("new DataSet is created or not.");
    
    Res.append("@return Returns a new DataSet, an ErrorString or a String.  ");
    Res.append("If \"create a new DataSet\" is selected and operation is ");
    Res.append("successful, a reference to a new DataSet will be returned. ");
    Res.append("If the operation is successful without creating a new ");
    Res.append("DataSet, a String indicating success will be returned. ");
    Res.append("If the operation is not successful, an ErrorString will be ");
    Res.append("returned.");

    Res.append(
       "@error \"ERROR: unsupported operation in DoDataBlockScalarOp\"");
    Res.append("@error \"ERROR: invalid index in Data block scalar Add\"");
    
    return Res.toString();
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   *         in this case Add
   */
   public String getCommand()
   {
     return "Add";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    FloatPG parameter1 = new FloatPG( "Scalar to Add", new Float(0.0) );
    addParameter( parameter1 );

    IntegerPG parameter2 = new IntegerPG( "Index of Data block", new Integer(0) );
    addParameter( parameter2 );

    BooleanPG parameter3 = new BooleanPG( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter3 );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**
   * @return returns a DataSet, an ErrorString or a String.
   * The return object may be a new DataSet containing the current DataSet 
   * values with the constant value added to one Data block, 
   * if "Create a new DataSet" is selected.  If a new DataSet is not 
   * created a String will be returned indicating successful completion
   * of the operation, if no error occurs, or an ErrorString will be 
   * returned, if an error does occur.
   */
  public Object getResult()
  {
    return DSOpsImplementation.DoDataBlockScalarOp( this );
  }


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataBlockScalarAdd Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    DataBlockScalarAdd new_op = new DataBlockScalarAdd( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


  /* --------------------------- main ----------------------------------- */
  /*
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
    DataSet ds = DataSetFactory.getTestDataSet();
    new ViewManager(ds, ViewManager.IMAGE); 

    Operator op = new DataBlockScalarAdd( ds, 100, 0, true );
    DataSet new_ds = (DataSet)op.getResult();
    new ViewManager(new_ds, ViewManager.IMAGE); 
    
    System.out.println(op.getDocumentation() + "\n");
    System.out.println(op.getResult().toString());
  }

}

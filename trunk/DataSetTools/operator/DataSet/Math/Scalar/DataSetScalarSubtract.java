/*
 * File:  DataSetScalarSubtract.java 
 *             
 * Copyright (C) 1999, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * 
 *  $Log$
 *  Revision 1.5  2003/10/16 00:09:50  dennis
 *  Fixed javadocs to build cleanly with jdk 1.4.2
 *
 *  Revision 1.4  2002/11/27 23:19:03  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/11/12 23:40:22  dennis
 *  Added getDocumentation() and main() methods.  Added documentation to
 *  getResult() method.  ( Modified by Shannon Hintzman )
 *
 *  Revision 1.2  2002/09/19 16:02:36  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 21:03:18  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Math.Scalar;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.viewer.*;
import  DataSetTools.dataset.*;
import  DataSetTools.operator.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.operator.DataSet.DSOpsImplementation;
import  DataSetTools.parameter.*;

/**
  * This operator subtracts a constant from the values of all data objects in a 
  * data set.
  */

public class DataSetScalarSubtract extends    ScalarOp 
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

  public DataSetScalarSubtract( )
  {
    super( "Subtract a Scalar" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  value       The value to subtract from each point in each Data
   *                      block in ds
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the value is just subtracted from 
   *                      the Data blocks of the DataSet.
   */

  public DataSetScalarSubtract( DataSet ds, float value, boolean make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s)

    IParameter parameter = getParameter( 0 );
    parameter.setValue( new Float( value) );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getDocumentation ------------------------ */
 
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This operator subtracts a constant value from all ");
    Res.append("data objects in a DataSet. When the operation is successful ");
    Res.append("and a new DataSet is created, a reference to this new DataSet");
    Res.append(" is returned. If a new DataSet is NOT created, the result is ");
    Res.append("stored in the current DataSet and a reference to the current ");
    Res.append("DataSet is returned. If the operation is NOT successful, an ");
    Res.append("error string is returned.");
     
    Res.append("@algorithm If make a new DataSet is selected, construct a new");
    Res.append(" DataSet with the same title, units and operations as the ");
    Res.append("current DataSet, subtract the constant value from each value ");
    Res.append("of the current DataSet and store in a new DataSet. If it is ");
    Res.append("not selected, the constant value will be subtracted from each");
    Res.append(" value of the current DataSet and replace the value in the ");
    Res.append("current DataSet.");
    
    Res.append("@param ds - the current DataSet on which the operator will be");
    Res.append(" performed.");
    Res.append("@param value - the value to subtract from each point in each ");
    Res.append("data block of the current DataSet.");
    Res.append("@param make_new_ds - a boolean value which determines if a ");
    Res.append("new DataSet is created or not.");
    
    Res.append("@return returns a new DataSet or an ErrorString.");
    Res.append("If \"create a new DataSet\" is selected and operation is ");
    Res.append("successful, a reference to a new DataSet will be returned. If");
    Res.append(" the operation is successful without creating a new DataSet, ");
    Res.append("a reference to the current DataSet will be returned. If the ");
    Res.append("operation is not successful, an ErrorString will be returned.");
    
    Res.append("@error \"ERROR: unsupported operation in DoDSScalarOp\"");
    
    return Res.toString();
  }

 /* ---------------------------- getCommand ------------------------------- */
 /**
  * @return	the command name to be used with script processor: in this case,
  *		 Sub
  */
   public String getCommand()
   {
     return "Sub";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Scalar to Subtract", new Float(0.0) );
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**
   * @return returns a DataSet or an Error String
   * The return object may be a new DataSet containing the current DataSet 
   * values with the constant value subtracted if "Create a new DataSet" is 
   * selected, the current data set with the constant value subtracted if 
   * "Create a new DataSet" was not selected, or an ErrorString if the 
   * operation was invalid ("Error: unsupported operation in DoDSScalarOp").
   */
  public Object getResult()
  {
    return DSOpsImplementation.DoDSScalarOp( this );
  }


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetScalarSubtract Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    DataSetScalarSubtract new_op = new DataSetScalarSubtract( );
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
    ViewManager viewer = new ViewManager(ds, ViewManager.IMAGE); 

    Operator op = new DataSetScalarSubtract( ds, 100, true );
    DataSet new_ds = (DataSet)op.getResult();
    ViewManager new_viewer = new ViewManager(new_ds, ViewManager.IMAGE); 
    
    System.out.println(op.getDocumentation());
    System.out.println(op.getResult().toString());
  }

}


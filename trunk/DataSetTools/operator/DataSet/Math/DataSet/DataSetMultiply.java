/*
 * File:  DataSetMultiply.java
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
 *  $Log$
 *  Revision 1.4  2002/11/12 23:28:54  dennis
 *  Added getDocumentation and main methods.  Added JavaDoc comments for the
 *  getResult() method.  (modified by: Tyler Stelzer)
 *
 *  Revision 1.3  2002/09/19 16:02:17  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.2  2002/07/17 20:31:39  dennis
 *  Fixed form of comment
 *
 *  Revision 1.1  2002/02/22 21:02:56  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.8  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.7  2001/04/26 19:06:06  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.6  2000/11/10 22:41:34  dennis
 *     Introduced additional abstract classes to better categorize the operators.
 *  Existing operators were modified to be derived from one of the new abstract
 *  classes.  The abstract base class hierarchy is now:
 *
 *   Operator
 *
 *    -GenericOperator
 *       --GenericLoad
 *       --GenericBatch
 *
 *    -DataSetOperator
 *      --DS_EditList
 *      --DS_Math
 *         ---ScalarOp
 *         ---DataSetOp
 *         ---AnalyzeOp
 *      --DS_Attribute
 *      --DS_Conversion
 *         ---XAxisConversionOp
 *         ---YAxisConversionOp
 *         ---XYAxesConversionOp
 *      --DS_Special
 *
 *     To allow for automatic generation of hierarchial menus, each new operator
 *  should fall into one of these categories, or a new category should be
 *  constructed within this hierarchy for the new operator.
 *
 *  Revision 1.5  2000/10/03 22:13:10  dennis
 *
 *  Now uses the constant empty DataSet, DataSet.EMPTY_DATA_SET,
 *   as a place holder for the DataSet parameter.
 *
 *  Revision 1.4  2000/07/10 22:35:54  dennis
 *  Now Using CVS 
 *
 *  Revision 1.6  2000/06/14 21:42:40  dennis
 *  getResult() method now calls DSOpsImplementation.DoDSBinaryOp( this )
 *  so that add, subtract, multiply and divide can all share the same
 *  basic implemention.
 *
 *  Revision 1.5  2000/06/09 16:12:35  dennis
 *  Added getCommand() method to return the abbreviated command string for
 *  this operator
 *
 *  Revision 1.4  2000/05/16 15:36:34  dennis
 *  Fixed clone() method to also copy the parameter values from
 *  the current operator.
 *
 *  Revision 1.3  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 *
 *  99/08/16   Added constructor to allow
 *             calling operator directly
 */

package DataSetTools.operator.DataSet.Math.DataSet;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.operator.DataSet.DSOpsImplementation;
import  DataSetTools.parameter.*;
import  DataSetTools.viewer.*;
import  DataSetTools.operator.*;
/**
  *  Multiply the corresponding Data "blocks" of the parameter DataSet times 
  *  the Data "blocks" of the current DataSet.
  */

public class DataSetMultiply extends  DataSetOp 
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

  public DataSetMultiply( )
  {
    super( "Multiply by a DataSet" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds              The DataSet to which the operation is applied
   *  @parm   ds_to_multiply  The DataSet to multiply DataSet ds by
   *  @param  make_new_ds     Flag that determines whether a new DataSet is
   *                          constructed, or the Data blocks of the second
   *                          DataSet are just multiplied times the Data blocks
   *                          of the first DataSet.
   */

  public DataSetMultiply( DataSet  ds,
                          DataSet  ds_to_multiply,
                          boolean  make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( ds_to_multiply );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: 
   *            in this case Mult
   */
   public String getCommand()
   {
     return "Mult";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "DataSet to Multiply by",
                                          DataSet.EMPTY_DATA_SET );
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**
  * @return returns a new DataSet or an Error String.
  *      If "create a new DataSet" is selected and operation is successful, a
  *      reference to a new DataSet will be returned.  If it is successful with out
  *      creating a new DataSet, a reference to the current DataSet will be returned.  If
  *      the operation is not successful, an error string will be returned.  Possible errors
  *      include "unsupported operation", "DataSets have different units", and 
  *      "no compatible Data blocks to combine in.
  *
  */
  public Object getResult()
  {
    return DSOpsImplementation.DoDSBinaryOp( this );
  }
  
  
  public String getDocumentation(){
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This operator multiplies two DataSets together.");
     Res.append(" When the operation is successful and a new DataSet is");
     Res.append(" created, a reference to this new DataSet is returned.");
     Res.append(" If a new DataSet is NOT created, the result is stored in");
     Res.append(" the current DataSet and a reference to the current DataSet");
     Res.append(" is returned. If the operation is NOT successful, an error");
     Res.append(" string is returned.");
     
    Res.append("@algorithm Construct a new DataSet with the same title, units");
     Res.append(" and operations as the current DataSet. Multiply the values");
     Res.append(" of each DataSet.  If make a new DataSet is selected, the");
     Res.append(" new values will be stored in a new DataSet.  If it is not");
     Res.append(" selected, the new values will be stored in the current");
     Res.append(" DataSet.");
    
    Res.append("@param ds - the current DataSet on which the operator will be");
    Res.append(" performed.");
    Res.append("@param ds_to_add - the DataSet to multiply the current");
    Res.append(" DataSet by.");
    Res.append("@param make_new_ds - a boolean value which determines if a");
     Res.append(" new DataSet is created or not.");
    
    Res.append("@return returns a new DataSet or an Error String.");
     Res.append(" If \"create a new DataSet\" is selected and operation is");
     Res.append(" successful, a reference to a new DataSet will be returned.");
     Res.append(" If it is successful without creating a new DataSet, a");
     Res.append(" reference to the current DataSet will bereturned. If the");
     Res.append(" operation is not successful, an error string will be"); 
     Res.append(" returned");
     
    Res.append("@error Unsupported operation");
    Res.append("@error DataSets have different units");
    Res.append("@error No compatible Data blocks");
     
     return Res.toString();
  }

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetMultiply Operator.  The list of parameters
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  { 
    DataSetMultiply new_op    = new DataSetMultiply( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
  
  
  public static void main(String []args)
  {
    DataSet ds1 = DataSetFactory.getTestDataSet();
    DataSet ds2 = DataSetFactory.getTestDataSet();
    ViewManager viewer = new ViewManager(ds1, ViewManager.IMAGE);
    
    Operator op = new DataSetMultiply(ds1, ds2, true);
    DataSet new_ds = (DataSet)op.getResult();
    ViewManager new_viewer = new ViewManager(new_ds, ViewManager.IMAGE);
    
    String documentation = op.getDocumentation();
    System.out.println(documentation);
    System.out.println("\n" + op.getResult().toString());
  }
}

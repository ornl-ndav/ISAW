/*
 * File:  DataSetDivide.java 
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
 *  Revision 1.4  2002/11/19 22:11:30  dennis
 *  Added getDocumentation() method, main test program.  Also,
 *  now checks that units match before dividing. (Chris Bouzek)
 *
 *  Revision 1.3  2002/09/19 16:02:15  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.2  2002/07/17 20:31:39  dennis
 *  Fixed form of comment
 *
 *  Revision 1.1  2002/02/22 21:02:55  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.6  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.5  2001/04/26 19:05:46  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.4  2000/11/10 22:41:34  dennis
 *    Introduced additional abstract classes to better categorize the operators.
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
 *  Revision 1.3  2000/10/03 22:13:10  dennis
 *
 *  Now uses the constant empty DataSet, DataSet.EMPTY_DATA_SET,
 *   as a place holder for the DataSet parameter.
 *
 *  Revision 1.2  2000/07/10 22:35:51  dennis
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
 * 
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
  *  Divide the corresponding Data "blocks" of the parameter DataSet into
  *  the Data "blocks" of the current DataSet.
  */

public class DataSetDivide extends  DataSetOp 
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
  public DataSetDivide( )
  {
    super( "Divide by a DataSet" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /** 
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *  
   *  @param  ds            The DataSet to which the operation is applied
   *  @parm   ds_to_divide  The DataSet to be divided into DataSet ds.
   *  @param  make_new_ds   Flag that determines whether a new DataSet is
   *                        constructed, or the Data blocks of the second
   *                        DataSet are just divided into the Data blocks of the
   *                        first DataSet.
   */

  public DataSetDivide( DataSet    ds,
                        DataSet    ds_to_divide,
                        boolean    make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( ds_to_divide );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processors: 
   *            in this case Div
   */
   public String getCommand()
   {
     return "Div";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "DataSet to Divide",
                                          DataSet.EMPTY_DATA_SET );
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }

  /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator divides this DataSet by another.");
    s.append("@assumptions The units on the two DataSets are compatible.");
    s.append("@algorithm Uses the binary divide from DSOpsImplementation.");
    s.append("@param The DataSet for the operation.");
    s.append("@param The DataSet with which to divide by.");
    s.append("@param A boolean value of true if you want a new DataSet to be ");
    s.append("created, or false if you want the operation performed on the ");
    s.append("original DataSet.");
    s.append("@return The DataSet which is the result of dividing the ");
    s.append("first DataSet by the second");
    s.append("@return An error if the units of the two DataSets do not match.");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   *  @return    The DataSet which is the result of dividing one DataSet
   *             by a second DataSet.  
   */  
  public Object getResult()
  {
    // get the parameters which determine whether a new DataSet should be made
    // and the DataSet to subtract.
    Boolean make_new = (Boolean)this.getParameter(1).getValue();
    DataSet ds_divided_by = (DataSet)this.getParameter(0).getValue();
    DataSet current_ds = this.getDataSet();

    //if units do not match
    if( !current_ds.getX_units().equals(ds_divided_by.getX_units()) || 
	!current_ds.getY_units().equals(ds_divided_by.getY_units()) )
	return new 
               ErrorString("ERROR: The units on the DataSets do not match.");

    if( !make_new.booleanValue() )
	current_ds.getOp_log().addEntry("Divided " + current_ds.toString()
					  + " by " + ds_divided_by + ".");
    //if the units do match
    return DSOpsImplementation.DoDSBinaryOp( this );
  }


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetDivide Operator.  The list of parameters
   * and the reference to the DataSet to which it applies are also copied.
   */
  public Object clone()
  {
    DataSetDivide new_op    = new DataSetDivide( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

  /* ---------------------------- main --------------------------------- */
  /**
   *  Main method for testing purposes.
   */

  public static void main( String[] args )
  {
    DataSet ds1 = DataSetFactory.getTestDataSet(); //create the first test DataSet
    DataSet ds2 = DataSetFactory.getTestDataSet(); //create the second test DataSet
    ViewManager viewer = new ViewManager(ds1, ViewManager.IMAGE);
    Operator op = new DataSetDivide( ds1, ds2, true );
    DataSet new_ds = (DataSet)op.getResult();
    ViewManager new_viewer = new ViewManager(new_ds, ViewManager.IMAGE);
  }//main()
}

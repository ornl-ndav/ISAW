/*
 * File:  DataSetSort.java 
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
 *  Revision 1.8  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.7  2001/04/26 19:07:45  dennis
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
 *  Revision 1.5  2000/11/07 15:55:02  dennis
 *  Replace "group" by "Group" in operator title.
 *
 *  Revision 1.4  2000/07/10 22:36:00  dennis
 *  Now Using CVS 
 *
 *  Revision 1.9  2000/06/09 16:12:35  dennis
 *  Added getCommand() method to return the abbreviated command string for
 *  this operator
 *
 *  Revision 1.8  2000/06/08 15:25:59  dennis
 *  Changed type casting of attribute names from (SpecialString) to
 *  (AttributeNameString).
 *
 *  Revision 1.7  2000/05/16 15:36:34  dennis
 *  Fixed clone() method to also copy the parameter values from
 *  the current operator.
 *
 *  Revision 1.6  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 *
 *  99/08/16   Added constructor to allow
 *             calling operator directly
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.util.*;
import  DataSetTools.dataset.*;

/**
  * This operator sorts a DataSet based on an attribute of the Data entries.
  */

public class DataSetSort  extends    DS_EditList 
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

  public DataSetSort( )
  {
    super( "Sort on ONE Group attribute" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  attr_name   The name of that attribute to be used for the
   *                      sort criterion
   *  @param  increasing  Flag that indicates whether the sort should put
   *                      the Data blocks in increasing or decreasing order
   *                      based on the specified attribute
   *  @param  make_new_ds Flag that determines whether the sort creates a
   *                      new DataSet and returns the new DataSet as a value,
   *                      or just does the sort "in place" and just returns
   *                      a message indicating the sort was done.
   */

  public DataSetSort( DataSet   ds,
                      String    attr_name,
                      boolean   increasing,
                      boolean   make_new_ds   )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new AttributeNameString(attr_name) );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( increasing ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, Sort
   */
   public String getCommand()
   {
     return "Sort";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Group Attribute to Sort on",
                               new AttributeNameString(Attribute.RAW_ANGLE) );
    addParameter( parameter );

    parameter = new Parameter("Sort in Increasing Order?", new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter("Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  // get the parameters

    String attr_name = 
              ((AttributeNameString)getParameter(0).getValue()).toString();
    boolean increasing  = ((Boolean)getParameter(1).getValue()).booleanValue();
    boolean make_new_ds = ((Boolean)getParameter(2).getValue()).booleanValue();

    DataSet ds     = this.getDataSet();

    DataSet new_ds = ds;             // set new_ds to either a reference to ds
    if ( make_new_ds )               // or a clone of ds
      new_ds = (DataSet)ds.clone();


    if ( new_ds.Sort(attr_name, increasing) )
    {                                          // if sort worked ok, return
                                               // new_ds, or message string
      new_ds.addLog_entry( "Sorted by " + attr_name );
      if ( make_new_ds )                           
        return new_ds;
      else
      {
        new_ds.notifyIObservers( IObserver.DATA_REORDERED );
        return new String("DataSet sorted");
      }                           
    }
    else
      {
        ErrorString message = new ErrorString(
                           "ERROR: Sort failed... no attribute: " + attr_name );
        System.out.println( message );
        return message;
      }
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetSort Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    DataSetSort new_op = new DataSetSort( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}

/*
 * File:  DataSetMerge.java 
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
 * This operator merges two DataSets by putting a copy of all spectra from
 * both DataSets into a new DataSet.  This will only be done if the X and Y
 * units match for the two DataSets being merged.
 *
 *  $Log$
 *  Revision 1.2  2002/09/19 16:01:08  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 21:01:49  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.10  2001/11/09 18:46:55  dennis
 *  Now returns error string if the units on the DataSets being merged
 *  don't match.
 *
 *  Revision 1.9  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.8  2001/04/26 19:05:48  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.7  2000/11/10 22:41:34  dennis
 *     Introduced additional abstract classes to better categorize 
 *     the operators.
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
 *  Revision 1.6  2000/10/03 22:13:10  dennis
 *
 *  Now uses the constant empty DataSet, DataSet.EMPTY_DATA_SET,
 *   as a place holder for the DataSet parameter.
 *
 *  Revision 1.5  2000/09/11 23:05:54  dennis
 *  Added blank space in log message.
 *
 *  Revision 1.4  2000/07/10 22:35:52  dennis
 *  Now Using CVS 
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

package DataSetTools.operator.DataSet.EditList;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
  * This operator creates a new DataSet by combining the Data blocks from the 
  * current DataSet with the Data blocks of a specified DataSet.  This can 
  * only be done if the two DataSets have the same X and Y units.  If a 
  * DataSet with N Data blocks is merged with a DataSet with M Data blocks,
  * the new DataSet will have N+M Data blocks.
  */

public class DataSetMerge extends    DS_EditList 
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

  public DataSetMerge( )
  {
    super( "Merge with a DataSet" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /** 
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *  
   *  @param  ds          The DataSet to which the operation is applied
   *  @parm   ds_to_merge The DataSet to merge with DataSet ds.
   */

  public DataSetMerge( DataSet             ds,
                       DataSet             ds_to_merge )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( ds_to_merge );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: 
   *            in this case Merge
   */
   public String getCommand()
   {
     return "Merge";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "DataSet to Merge",
                                          DataSet.EMPTY_DATA_SET ); 
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

                                     // Get the second DataSet from the 
                                     // parameter list and merge Data objects 
                                     // with Data objects from the current
                                     // DataSet.
  public Object getResult()
  {                                  // get the DataSet to merge with 
    DataSet ds_to_merge = (DataSet)(getParameter(0).getValue());

                                     // get the current data set
    DataSet ds = this.getDataSet();

    if ( !ds.SameUnits(ds_to_merge)) // DataSets are NOT COMPATIBLE TO COMBINE
      return new ErrorString("Error DataSets have different units " + 
                  ds.getX_units() + " != " + ds_to_merge.getX_units() + " or " +
                  ds.getY_units() + " != " + ds_to_merge.getY_units() ); 
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = ds.empty_clone(); 
    new_ds.addLog_entry( "Merged with " + ds_to_merge );
    new_ds.CombineAttributeList( ds_to_merge );

                                           // do the operation
    Data data, 
         new_data; 
                                           // put in Data blocks from the 
                                           // current data set
    int num_data = ds.getNum_entries();
    for ( int i = 0; i < num_data; i++ )
    {
      data     = ds.getData_entry( i );    // get reference to the data entry
      new_data = (Data)data.clone( );      // clone it and add it to the new 
      new_ds.addData_entry( new_data );    // DataSet  
    }
                                           // put in Data blocks from the 
                                           // second data set
    num_data = ds_to_merge.getNum_entries();
    for ( int i = 0; i < num_data; i++ )
    {
      data     = ds_to_merge.getData_entry( i );   // get reference to the data
      new_data = (Data)data.clone( );              // clone it and add it to 
      new_ds.addData_entry( new_data );            // the new DataSet
    }

    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetMerge Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    DataSetMerge new_op = new DataSetMerge( );
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
    DataSetMerge op = new DataSetMerge();

    String list[] = op.getCategoryList();
    System.out.println( "Categories are: " );
    for ( int i = 0; i < list.length; i++ )
      System.out.println( list[i] );
  }

}

/*
 * File:  SumCurrentlySelected.java 
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
 * $Log$
 * Revision 1.1  2002/02/22 21:03:00  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.6  2001/08/16 19:16:04  dennis
 * Now calls DSImplementationOps.AddDataBlocks to form the sum
 * and to form an average DetectorPosition, weighted by
 * the solid angles ( if present ).
 *
 * Revision 1.5  2001/06/01 21:18:00  rmikk
 * Improved documentation for getCommand() method
 *
 * Revision 1.4  2001/04/26 19:11:48  dennis
 * Added copyright and GPL info at the start of the file.
 *
 * Revision 1.3  2000/11/10 22:41:34  dennis
 *    Introduced additional abstract classes to better categorize the operators.
 * Existing operators were modified to be derived from one of the new abstract
 * classes.  The abstract base class hierarchy is now:
 *
 *  Operator
 *
 *   -GenericOperator
 *      --GenericLoad
 *      --GenericBatch
 *
 *   -DataSetOperator
 *     --DS_EditList
 *     --DS_Math
 *        ---ScalarOp
 *        ---DataSetOp
 *        ---AnalyzeOp
 *     --DS_Attribute
 *     --DS_Conversion
 *        ---XAxisConversionOp
 *        ---YAxisConversionOp
 *        ---XYAxesConversionOp
 *     --DS_Special
 *
 *    To allow for automatic generation of hierarchial menus, each new operator
 * should fall into one of these categories, or a new category should be
 * constructed within this hierarchy for the new operator.
 *
 * Revision 1.2  2000/11/07 15:41:59  dennis
 * Replaced "Data blocks" with "Groups" in operator title.
 *
 * Revision 1.1  2000/07/10 22:36:25  dennis
 * Now Using CVS 
 *
 * Revision 1.6  2000/06/14 22:17:51  dennis
 * Placed make_new_ds parameter last in list of parameters for consistency
 * with the other operators.
 *
 * Revision 1.5  2000/06/09 16:12:35  dennis
 * Added getCommand() method to return the abbreviated command string for
 * this operator
 *
 * Revision 1.4  2000/05/25 19:06:14  dennis
 * fixed problem with documentation format
 *
 *  Revision 1.3  2000/05/16 15:36:34  dennis
 *  Fixed clone() method to also copy the parameter values from
 *  the current operator.
 * 
 *  Revision 1.2  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 *
 */

package DataSetTools.operator.DataSet.Math.DataSet;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.operator.DataSet.DSOpsImplementation;

/**
  *  Replace Data blocks with the sum of the Data blocks.  Data blocks that 
  *  are marked as selected, or, alternatively, that are not marked 
  *  as selected, are summed and replaced depending on the paramters. 
  */

public class SumCurrentlySelected  extends    DataSetOp 
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

  public SumCurrentlySelected( )
  {
    super( "Sum currently selected Groups" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *
   *  @param  status      Flag that determines whether the selected or
   *                      un-selected Data blocks are summed.
   *                      If status==true, the selected blocks are summed.
   *                      If status==false, the un-selected blocks are summed.
   *
   *  @param  make_new_ds Flag that determines whether removing the Data
   *                      blocks makes a new DataSet and returns the new 
   *                      DataSet as a value, or just sums and removes the 
   *                      Data blocks from the current DataSet and returns a 
   *                      message indicating that the sum operation was
   *                      done.
   */

  public SumCurrentlySelected( DataSet  ds,
                               boolean  status,
                               boolean  make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Boolean( status ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( make_new_ds ) );


    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   *         in this case, SumSel
   */
   public String getCommand()
   {
     return "SumSel";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Sum selected (or un-selected) groups?",
                                         new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(true) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  // get the parameters
    boolean status      = ((Boolean)getParameter(0).getValue()).booleanValue();
    boolean make_new_ds = ((Boolean)getParameter(1).getValue()).booleanValue();

    DataSet ds      = this.getDataSet();
    DataSet temp_ds = (DataSet)ds.empty_clone();

    Data    data  = null;
    int num_data = ds.getNum_entries();
    int ids[] = new int[num_data];
    int n_summed = 0;
    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );
      if ( data.isSelected() == status )  
      {                                     
        ids[n_summed] = data.getGroup_ID();    // record the Data blocks that
        n_summed++;                            // are to be summed 

        temp_ds.addData_entry( data );
      }
    }

    if ( temp_ds.getNum_entries() <= 0 )
    {
      ErrorString message = new ErrorString(
                         "ERROR: No Data blocks satisfy the condition" );
      System.out.println( message );
      return message;
    }
      
    SpecialString result = DSOpsImplementation.AddDataBlocks( temp_ds );
    if ( result != null )
      return result;

    String log_message;
    int list[] = new int[n_summed];
    for ( int i = 0; i < n_summed; i++)
      list[i] = ids[i];
    arrayUtil.sort( list );
    
    if ( status )
      log_message = "Summed selected Data blocks: "+IntList.ToString(list);
    else
      log_message = "Summed un-selected Data blocks: "+IntList.ToString(list);

    if ( make_new_ds )                           
    {
      temp_ds.addLog_entry( log_message );
      return temp_ds;
    }
    else
    {
      ds.addLog_entry( log_message );
      ds.removeSelected( status );
      ds.addData_entry( temp_ds.getData_entry(0) );
      ds.clearSelections();

      ds.notifyIObservers( IObserver.DATA_DELETED );
      return new String("Specified Data blocks SUMMED");
    }                           
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SumCurrentlySelected Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    SumCurrentlySelected new_op = new SumCurrentlySelected( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}

/*
 * File:  DataSetDivide_1.java 
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 *  Revision 1.1  2002/07/17 20:28:39  dennis
 *  Mathematical operation between one Data block of a second
 *  DataSet and all Data blocks of the current DataSet.
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
  *  Divide one Data "block" of a second DataSet into all Data "blocks"
  *  of the current DataSet.  The resulting Data blocks are placed in a  
  *  new DataSet provided the parameter "make_new_ds" is true, otherwise
  *  the Data blocks of the current DataSet are altered.
  *
  *  The tile of this operator is "DataSet divide by one Data block".
  *
  *  The command name for this operator is "Div".
  *
  *  @see DataSetTools.operator.DataSet.DataSetOperator
  *  @see DataSetTools.operator.Operator
  */

public class DataSetDivide_1 extends    DataSetOp 
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

  public DataSetDivide_1( )
  {
    super( "DataSet divide by one Data block" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  ds_2        The DataSet containing the Data block to be 
   *                      divided into all Data blocks of DataSet ds.
   *  @param  id          The id of the Data block to use from DataSet ds_2.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the specified Data block from the 
   *                      second DataSet is just divided into the Data 
   *                      blocks of the current DataSet.
   */

  public DataSetDivide_1( DataSet    ds,
                          DataSet    ds_2,
                          int        id,
                          boolean    make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( ds_2 );

    parameter = getParameter( 1 );
    parameter.setValue( new Integer( id ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return  the command name to be used with script processor: 
   *          in this case Div 
   */
   public String getCommand()
   {
     return "Div";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "DataSet to Divide", 
                                          DataSet.EMPTY_DATA_SET );
    addParameter( parameter );

    parameter = new Parameter( "ID of Group to Divide", new Integer(5) ); 
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {       
    return DSOpsImplementation.DoDSOneDataBlockOp( this );
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetDivide_1 Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    DataSetDivide_1 new_op = new DataSetDivide_1( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}

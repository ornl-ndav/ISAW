/*
 * File:  GetDataAttribute.java 
 *             
 * Copyright (C) 2000, Ruth Mikkelson,
 *                     Dennis Mikkelson
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
 *  Revision 1.2  2002/09/19 16:00:04  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 21:00:06  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.6  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.5  2001/04/26 19:09:46  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.4  2000/11/17 23:44:29  dennis
 *  Minor documentation fix.
 *
 *  Revision 1.3  2000/11/10 22:41:34  dennis
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
 */

package DataSetTools.operator.DataSet.Attribute;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
  *  Allows the user to get attributes from the Data blocks in a DataSet
  *
  *  @see DS_Attribute 
  */

public class GetDataAttribute extends    DS_Attribute 
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

  public GetDataAttribute( )
  {
    super( "Get Data Attribute" );
  }

  
/* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied.
   *  @param  index       The index of the data block whose attribute is to 
   *                      be set.
   *  @parm   Attrib      The Attribute to be set.
   * 
   */

  public GetDataAttribute  ( DataSet               ds,
                             Integer               index,  
                             AttributeNameString   Attrib
                           )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    IParameter parameter ;

    parameter = getParameter( 0 );
    parameter.setValue( index);

    parameter= getParameter( 1 );
    parameter.setValue( Attrib);   

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, GetAttr
   */
   public String getCommand()
   {
     return "GetAttr";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters
    Parameter parameter;
    
    parameter = new Parameter( "Data block index?", new Integer( 0 ));
    addParameter( parameter);

    parameter = new Parameter( "Attribute?", new AttributeNameString("") );
    addParameter( parameter );
  }

  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  { 
     Attribute A;
     DataSet ds = getDataSet();

     int index = ((Integer) (getParameter(0).getValue())).intValue();
     String S  = ((AttributeNameString)(getParameter(1).getValue())).toString();

     Data D    = ds.getData_entry(index);
     if( D == null) 
       return new ErrorString(" Improper index ");
     
     Object O = D.getAttributeValue( S );
     if ( O == null)
       return new ErrorString(" Attribute "+ S + " is not in the List" );

     return O;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current GetDataAttribute Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    GetDataAttribute new_op = new GetDataAttribute( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}

/*
 * File:  IntegrateGroup.java  
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
 * Revision 1.1  2002/02/22 21:02:31  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.7  2001/06/01 21:18:00  rmikk
 * Improved documentation for getCommand() method
 *
 * Revision 1.6  2001/04/26 19:09:58  dennis
 * Added copyright and GPL info at the start of the file.
 *
 * Revision 1.5  2000/12/07 22:39:34  dennis
 * Trivial simplification.
 *
 * Revision 1.4  2000/11/10 22:41:34  dennis
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
 * Revision 1.3  2000/11/07 15:49:06  dennis
 * Replaced group with Group in operator title.
 *
 * Revision 1.2  2000/08/02 20:17:58  dennis
 * Changed to use TrapIntegrate() for function data instead of just using
 * IntegrateHistogram for histogram data
 *
 * Revision 1.1  2000/07/10 22:36:10  dennis
 * Now Using CVS 
 *
 * Revision 1.2  2000/06/09 16:12:35  dennis
 * Added getCommand() method to return the abbreviated command string for
 * this operator
 *
 * Revision 1.1  2000/06/09 14:58:19  dennis
 * Initial revision
 *
 * Revision 1.5  2000/06/01 21:43:26  dennis
 * fixed error in documentation.
 *
 * Revision 1.4  2000/05/16 15:36:34  dennis
 * Fixed clone() method to also copy the parameter values from
 * the current operator.
 *
 * Revision 1.3  2000/05/11 16:41:28  dennis
 * Added RCS logging
 *
 * 2000/06/09  Renamed from just Integrate
 *
 * 99/08/16    Added constructor to allow
 *             calling operator directly
 */

package DataSetTools.operator.DataSet.Math.Analyze;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;
import  DataSetTools.operator.Parameter;

/**
  *  This operator calculates the integral of the data values of one Data 
  *  block.  The Group ID of the Data block to be integrated is specified by 
  *  the parameter "Group ID".  The interval [a,b] over which the integration 
  *  is done is specified by the two endpoints a, b where it is assumed that
  *  a < b.  This operator just produces a numerical result that is displayed 
  *  in the operator dialog box.
  */

public class  IntegrateGroup  extends    AnalyzeOp 
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

  public IntegrateGroup( )
  {
    super( "Integrate a Group" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  group_id    The group_id of the Data block that is to be
   *                      integrated
   *  @param  a           The left hand endpoint of the interval [a, b] over
   *                      which each Data block is integrated
   *  @param  b           The righ hand endpoint of the interval [a, b] over
   *                      which each Data block is integrated
   */

  public IntegrateGroup( DataSet      ds,
                         int          group_id,
                         float        a,
                         float        b  )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    Parameter parameter = getParameter(0);
    parameter.setValue( new Integer( group_id ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( a ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float( b ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, IntegGrp
   */
   public String getCommand()
   {
     return "IntegGrp";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Group ID to Integrate",new Integer(0));
    addParameter( parameter );

    parameter = new Parameter("Left end point (a)", new Float(0));
    addParameter( parameter );

    parameter = new Parameter("Right end point (b)", new Float(0));
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  // get the parameters

    int group_id = ( (Integer)(getParameter(0).getValue()) ).intValue();

    float a = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float b = ( (Float)(getParameter(2).getValue()) ).floatValue();

                                     // get the current data set and do the 
                                     // operation
    DataSet ds = this.getDataSet();

    Data data = ds.getData_entry_with_id( group_id );
    if ( data == null )
    {
      ErrorString message = new ErrorString( 
                           "ERROR: no data entry with the group_ID "+group_id );
      System.out.println( message );
      return message;
    }

    float x_vals[] = data.getX_scale().getXs();
    float y_vals[] = data.getY_values();

    float result;

    if ( x_vals.length == y_vals.length + 1 )  // histogram
      result = NumericalAnalysis.IntegrateHistogram( x_vals, y_vals, a, b );
    else                                       // tabulated function
      result = NumericalAnalysis.TrapIntegrate( x_vals, y_vals, a, b );

    return new Float( result );  
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current IntegrateGroup Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    IntegrateGroup new_op = new IntegrateGroup( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}

/*
 * File:  CalculateMomentOfGroup.java 
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
 *  Revision 1.3  2002/09/19 16:01:51  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.2  2002/03/05 19:25:42  pfpeterson
 *  Updated @see references in javadocs.
 *
 *  Revision 1.1  2002/02/22 21:02:26  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.5  2001/05/31 20:18:55  rmikk
 *  Added javadocs for the getCommand method
 *
 *  Revision 1.4  2001/04/26 19:04:25  dennis
 *  Added copyright and GPL info at the start of the file.
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
 *  Revision 1.2  2000/08/02 20:19:27  dennis
 *  Changed to use TrapMoment() for function data instead of just using
 *  HistogramMoment() for histogram data
 *
 *  Revision 1.1  2000/07/10 22:35:47  dennis
 *  Now Using CVS 
 *
 *  Revision 1.1  2000/06/09 16:12:01  dennis
 *  Initial revision
 *
 *  Revision 1.6  2000/05/16 15:36:34  dennis
 *  Fixed clone() method to also copy the parameter values from
 *  the current operator.
 *
 *  Revision 1.5  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 *
 *  2000/06/09  Renamed CalculateMoment to
 *               CalculateMomentOfGroup
 *
 *  99/08/16    Added constructor to allow 
 *              calling operator directly
 */

package DataSetTools.operator.DataSet.Math.Analyze;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;
import  DataSetTools.operator.*;
import  DataSetTools.parameter.*;

/**
  *  Calculate the specified moment of the selected Data block over the  
  *  sppecified inteval. This operator calculates the integral of the data 
  *  values times a power of x.  The Group ID of the data block to be
  *  integrated is specified by the parameter "Group ID".  The power of x is 
  *  specified by the parameter "Moment".  The interval [a,b] over which the 
  *  integration is done is specified by the two endpoints a, b where it is 
  *  assumed that a < b.
  *
  *  @see DataSetTools.operator.DataSet.DataSetOperator
  *  @see Operator
  */

public class  CalculateMomentOfGroup  extends    AnalyzeOp 
                                      implements Serializable
{
  /* ----------------------- DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public CalculateMomentOfGroup( )
  {
    super( "Calculate Moment of Group" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct a CalculateMomentOfGroup operator for a specified DataSet and 
   *  with the specified parameter values so that the operation can be 
   *  invoked immediately by calling getResult()
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  group_id    The id of the group for which the moment is to be
   *                      calculated 
   *                      which the moment is to be calculated.
   *  @param  a           The left hand endpoint of the interval [a, b] over
   *                      which the moment is to be calculated.
   *  @param  b           The right hand endpoint of the interval [a, b] over
   *                      which the moment is to be calculated.
   *                      from the data set.
   *  @param  center      The center point for the moment calculation.
   *  @parm   moment      The moment to be calculated.
   */

  public CalculateMomentOfGroup( DataSet  ds,
                                 int      group_id,
                                 float    a,
                                 float    b,
                                 float    center,
                                 int      moment   )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( new Integer( group_id ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( a ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float( b ) );

    parameter = getParameter( 3 );
    parameter.setValue( new Float( center ) );

    parameter = getParameter( 4 );
    parameter.setValue( new Integer( moment ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case MomGrp
   */
   public String getCommand()
   {
     return "MomGrp";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Group ID",new Integer(0));
    addParameter( parameter );

    parameter = new Parameter("Left end point (a)", new Float(0));
    addParameter( parameter );

    parameter = new Parameter("Right end point (b)", new Float(0));
    addParameter( parameter );

    parameter = new Parameter("Center point for moment calculation",
                               new Float(0));
    addParameter( parameter );

    parameter = new Parameter("Moment ( 1, 2, 3 ... )", new Integer(1));
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  // get the parameters

    int group_ID = ( (Integer)(getParameter(0).getValue()) ).intValue();
    int moment   = ( (Integer)(getParameter(4).getValue()) ).intValue();

    float a      = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float b      = ( (Float)(getParameter(2).getValue()) ).floatValue();
    float center = ( (Float)(getParameter(3).getValue()) ).floatValue();

                                     // get the current data set and do the 
                                     // operation
    DataSet ds = this.getDataSet();

    Data data = ds.getData_entry_with_id( group_ID );
    if ( data == null )
    {
      ErrorString message = new ErrorString( 
                          "ERROR: no data entry with the group_ID "+group_ID );
      System.out.println( message );
      return message;
    }
    else
    {
      float x_vals[] = data.getX_scale().getXs();
      float y_vals[] = data.getY_values();

      float result = 0;
      if ( x_vals.length == y_vals.length + 1 )   // histogram
         result = NumericalAnalysis.HistogramMoment( x_vals, y_vals, 
                                                     a,      b, 
                                                     center,
                                                     moment     );

      else                                        // function
         result = NumericalAnalysis.TrapMoment( x_vals, y_vals, 
                                                a,      b, 
                                                center,
                                                moment     );
     
      return new Float( result );
    }
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current CalculateMomentOfGroup Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    CalculateMomentOfGroup new_op = new CalculateMomentOfGroup( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}

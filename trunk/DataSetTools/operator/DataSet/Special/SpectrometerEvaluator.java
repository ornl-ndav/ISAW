/*
 * File:  SpectrometerEvaluator.java 
 *
 * Copyright (C) 2000, Dongfeng Chen,
 *                     Alok Chatterjee,
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
 *  Revision 1.1  2002/02/22 21:03:44  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.8  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.7  2001/04/26 19:10:52  dennis
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
 */

package  DataSetTools.operator.DataSet.Special;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.retriever.*;
import  ChopTools.*;
import  DataSetTools.operator.Parameter;

/**
 * This operator removes Data blocks that seem to come from defective detectors
 * in a Chopper Spectrometer such as HRMCS & LRMCS at IPNS. 
 */ 

public class SpectrometerEvaluator extends    DS_Special 
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

  public SpectrometerEvaluator( )
  {
    super( "Remove Bad Detectors Data" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  monitor_ds  The DataSet containing the monitors for this 
   *                      histogram
   *  @param  up_level    Upper cut off for the detector quality measure
   *  @param  up_level    Lower cut off for the detector quality measure
   */

  public SpectrometerEvaluator( DataSet    ds,
                                DataSet    monitor_ds,
                                float      up_level, 
                                float      low_level  )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( monitor_ds );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( up_level ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float( low_level ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: in this case, Eval
   */
   public String getCommand()
   {
     return "Eval";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter;

    parameter = new Parameter( "Monitor DataSet",
                                DataSet.EMPTY_DATA_SET );
    addParameter( parameter );
   
    parameter = new Parameter( "Uplevel", new Float(50.0) );
    addParameter( parameter );
   
    parameter = new Parameter( "Lowlevel", new Float(1.0) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();
    DataSet new_ds = ds.empty_clone();
    
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Evaluator works" );
    
    DataSet monitor_ds    =    (DataSet)(getParameter(0).getValue());
    
//    System.out.println("The H1 and M1 are : "+ds+" and"+  monitor_ds );
    
    if ( !ds.SameUnits( monitor_ds ) )    // units don't match so return error 
      {
        ErrorString message = new ErrorString(
                           "ERROR: Monitor DataSet has different units" );
        System.out.println( message );
        return message;
      }

    if ( !ds.getX_units().equalsIgnoreCase("Time(us)")  ||
         !ds.getY_units().equalsIgnoreCase("Counts") )      // wrong units, so
      {
        ErrorString message = new ErrorString(
                           "ERROR: DataSet units not Time(us) & Counts " );
        System.out.println( message );
        return message;
      }
  
    float uplevel = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float lowlevel = ( (Float)(getParameter(2).getValue()) ).floatValue();
    
    new_ds  = chop_evaluation.evaluator(ds, monitor_ds, uplevel, lowlevel );
    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SpectrometerEvaluator Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */

  public Object clone()
  {
    SpectrometerEvaluator new_op = new SpectrometerEvaluator( );
                                                // copy the data set associated
                                                // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}

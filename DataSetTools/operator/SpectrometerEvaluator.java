/*
 * @(#)SpectrometerEvaluator.java   0.2  99/08/16  Dongfeng Chen 
 *                                                 Alok Chatterjee
 *                                                 Dennis Mikkelson
 *
 *                                 99/08/16   Added constructor to allow
 *                                            calling operator directly
 *
 * This operator removes Data blocks that seem to come from defective detectors
 * in a Chopper Spectrometer such as HRMCS & LRMCS at IPNS. 
 */

package  DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.retriever.*;
import  ChopTools.*;


public class SpectrometerEvaluator extends    DataSetOperator 
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
   * Returns the abbreviated command string for this operator.
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

    parameter = new Parameter( "Monitors",
                              new DataSet("Monitor DataSet", "Empty DataSet") );
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
   * Get a copy of the current SpectrometerEvaluato Operator.  The list of
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

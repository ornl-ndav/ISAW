/*
 * @(#)SpectrometerNomalizer.java   0.2  99/07/23  Dongfeng Chen, 
 *                                                 Dennis Mikkelson
 *
 *                                 99/08/16   Added constructor to allow
 *                                            calling operator directly
 *             
 *
 *
 *  $Log$
 *  Revision 1.6  2001/04/26 19:11:16  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.5  2000/11/10 22:41:34  dennis
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
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  ChopTools.*;
import  DataSetTools.util.*;

/**
  * This operator normalizes all data objects in a DataSet to a monitor.
  * Specifically, it divides a data set by a the integrated peak 
  * obtained from Monitor 1
  */

public class SpectrometerNormalizer extends    DS_Special
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

  public SpectrometerNormalizer( )
  {
    super( "Scale Using Monitor1 DataSet" );
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
   *  @param  adjuster    Sets the scale for the normalized spectrum 
   */

  public SpectrometerNormalizer( DataSet    ds,
                                 float      adjuster,
                                 DataSet    monitor_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float( adjuster) );

    parameter = getParameter( 1 );
    parameter.setValue( monitor_ds );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "Norm";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Adjuster", new Float(1000000.0) );
    addParameter( parameter );
   
    parameter = new Parameter( "Monitor DataSet",
                                DataSet.EMPTY_DATA_SET );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

                                     // The concrete operation extracts the
                                     // current value of the scale factor
                                     // parameter and returns the result of 
                                     // dividing by that scale factor.
  public Object getResult()
  {                                  // get the scale factor parameter 
    
     DataSet ds            = this.getDataSet(); 
     DataSet monitor_ds    = (DataSet)(getParameter(1).getValue());

     if ( !ds.SameUnits( monitor_ds )  )    // units don't match so something
      {                                     // is wrong
        ErrorString message = new ErrorString(
                           "ERROR: monitor_ds has different units" );
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

                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
     DataSet new_ds = ds.empty_clone(); 
     new_ds.addLog_entry( "Normalize Using " + monitor_ds );
     float nomalizer=chop_calibraton.intergratedPeak1Intensity(monitor_ds);
     System.out.println("Nomalizer is:"+ nomalizer);
                                            // now do the normalization 
     
     float scale = nomalizer/( (Float)(getParameter(0).getValue()) ).floatValue();
     System.out.println("Scale is:"+ scale);
     
                                     // get the current data set

    if ( scale != 0 )                // do the operation if possible
    {                                // otherwise return empty data set 
      int num_data = ds.getNum_entries();
      Data data,
           new_data;
      for ( int i = 0; i < num_data; i++ )
      {
        data = ds.getData_entry( i );       // get reference to the data entry
        new_data = data.divide( scale );    // divide by scale factor, assuming
                                            // 0 error in the scale factor.
        new_ds.addData_entry( new_data );      
      }
    }

    return new_ds;
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SpectrometerNomalizer Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SpectrometerNormalizer new_op    = new SpectrometerNormalizer( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}

/*
 * @(#)DataSetScalarDivide.java   0.2  99/06/07   Dennis Mikkelson
 *                                     99/08/16   Added constructor to allow
 *                                                calling operator directly
 *             
 * This operator divides all data objects in a data set by a scalar value.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;

/**
  *  Divide a data set by a constant scalar value 
  */

public class DataSetScalarDivide extends    DataSetOperator 
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

  public DataSetScalarDivide( )
  {
    super( "Divide by Scalar" );

    Parameter parameter = new Parameter( "Divisor", new Float(1.0) );
    addParameter( parameter );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */

  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @parm   value       The value to divide into to each point in each Data
   *                      block in ds
   */

  public DataSetScalarDivide( DataSet ds, float value )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s)

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float( value) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getResult ------------------------------- */

                                     // The concrete operation extracts the
                                     // current value of the scale factor
                                     // parameter and returns the result of 
                                     // dividing by that scale factor.
  public Object getResult()
  {                                  // get the scale factor parameter 
    float scale = ( (Float)(getParameter(0).getValue()) ).floatValue();

                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = ds.empty_clone(); 
    new_ds.addLog_entry( "Divided by " + scale );

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
   * Get a copy of the current DataSetScalarDivide Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    DataSetScalarDivide new_op    = new DataSetScalarDivide( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }



}

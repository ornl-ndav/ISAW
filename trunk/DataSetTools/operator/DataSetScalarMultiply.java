/*
 * @(#)DataSetScalarMultiply.java   0.1  99/06/02   Dennis Mikkelson
 *             
 * This operator multiplies all data objects in a data set by a scalar value.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;

/**
  *  Scale a data set by a constant scale factor
  */

public class DataSetScalarMultiply extends    DataSetOperator 
                                   implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public DataSetScalarMultiply( )
  {
    super( "Multiply by Scalar" );

    Parameter parameter = new Parameter( "Scale Factor", new Float(1.0) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

                                     // The concrete operation extracts the
                                     // current value of the scale factor
                                     // parameter and returns the result of 
                                     // scaling.
  public Object getResult()
  {                                  // get the scale factor parameter 
    float scale = ( (Float)(getParameter(0).getValue()) ).floatValue();

                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = (DataSet)ds.empty_clone(); 
    new_ds.addLog_entry( "Multiplied by " + scale );

                                            // do the operation
    int num_data = ds.getNum_entries();
    Data data,
         new_data;
    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );        // get reference to the data entry
      new_data = data.multiply( scale );   // scale it, assuming 0 error in 
                                           // the scale factor.
      new_ds.addData_entry( new_data );      
    }

    return new_ds;
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetScalarMultiply Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    DataSetScalarMultiply new_op = new DataSetScalarMultiply( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }


}

/*
 * @(#)DataSetScalarAdd.java   0.1  99/06/07   Dennis Mikkelson
 *             
 * This operator adds a constant to the values of all data objects in a 
 * data set.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;

/**
  *  Add a constant value to all data objects in a data set. 
  */

public class DataSetScalarAdd extends    DataSetOperator 
                              implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public DataSetScalarAdd( )
  {
    super( "Add a Scalar" );

    Parameter parameter = new Parameter( "Scalar to Add", new Float(0.0) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

                                     // The concrete operation extracts the
                                     // current value of the scalar to add 
                                     // and returns the result of adding it
                                     // to each point in each data block.
  public Object getResult()
  {
                                     // get the scalar to add 
    float shift = ( (Float)(getParameter(0).getValue()) ).floatValue();

                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = (DataSet)ds.empty_clone(); 
    new_ds.addLog_entry( "Added " + shift );

                                            // do the operation
    int num_data = ds.getNum_entries();
    Data data,
         new_data;
    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );        // get reference to the data entry
      new_data = data.add( shift );        // add it, assuming 0 error in 
                                           // the scale factor.
      new_ds.addData_entry( new_data );      
    }

    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetScalarAdd Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    DataSetScalarAdd new_op    = new DataSetScalarAdd( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }


}

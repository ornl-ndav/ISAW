/*
 * @(#)DataSetMultiply.java   0.1  99/07/15   Dennis Mikkelson
 *             
 * This operator multiplies two DataSets by multiplying the corresponding 
 * Data "blocks" in the DataSets.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  *  Multiply the corresponding Data "blocks" of the parameter DataSet times 
  *  the Data "blocks" of the current DataSet.
  */

public class DataSetMultiply extends  DataSetOperator 
                                      implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public DataSetMultiply( )
  {
    super( "Multiply by a DataSet" );

    Parameter parameter = new Parameter( 
                             "DataSet to Multiply by", 
                             new DataSet("DataSetToMultiplyBy",
                                         "Empty DataSet")  );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

                                     // Get the second DataSet from the 
                                     // parameter list and multiply  
                                     // corresponding Data objects by Data 
                                     // objects in the current DataSet.
  public Object getResult()
  {                                  // get the DataSet to multiply 
    DataSet ds_to_multiply = (DataSet)(getParameter(0).getValue());

                                     // get the current data set
    DataSet ds = this.getDataSet();

    if ( !ds.SameUnits( ds_to_multiply ) )  // DataSets are NOT COMPATIBLE TO 
      {                                     // COMBINE
        ErrorString message = new ErrorString(
                           "ERROR: DataSets have different units" );
        System.out.println( message );
        return message;
      }
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = (DataSet)ds.empty_clone(); 
    new_ds.addLog_entry( "Multiplied by " + ds_to_multiply );

                                            // do the operation
    int num_data = ds.getNum_entries();
    Data data,
         multiply_data,
         new_data;
    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );        // get reference to the data entry

      multiply_data = ds_to_multiply.getData_entry_with_id( data.getGroup_ID());
 
      if ( multiply_data != null )         // there is a corresponding entry
      {                                    // to try to subtract 
        new_data = data.multiply( multiply_data );  
        if ( new_data != null )            // if they could be multiplied 
          new_ds.addData_entry( new_data );      
      }
    }
    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetMultiply Operator.  The list of parameters
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  { 
    DataSetMultiply new_op    = new DataSetMultiply( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }


}

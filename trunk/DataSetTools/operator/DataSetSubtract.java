/*
 * @(#)DataSetSubtract.java   0.1  99/07/15   Dennis Mikkelson
 *             
 * This operator subtracts two DataSets by subtracting the corresponding 
 * Data "blocks" in the DataSets.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  *  Subtract the corresponding Data "blocks" of the parameter DataSet from
  *  the Data "blocks" of the current DataSet.
  */

public class DataSetSubtract extends  DataSetOperator 
                                      implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public DataSetSubtract( )
  {
    super( "Subtract a DataSet" );

    Parameter parameter = new Parameter( 
                             "DataSet to Subtract", 
                             new DataSet("DataSetToSubtract","Empty DataSet"));
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

                                     // Get the second DataSet from the 
                                     // parameter list and subtract 
                                     // corresponding Data objects from Data 
                                     // objects in the current DataSet.
  public Object getResult()
  {                                  // get the DataSet to subtract 
    DataSet ds_to_subtract = (DataSet)(getParameter(0).getValue());

                                     // get the current data set
    DataSet ds = this.getDataSet();

    if ( !ds.SameUnits( ds_to_subtract ) )  // DataSets are NOT COMPATIBLE TO 
                                            // COMBINE
      {
        ErrorString message = new ErrorString(
                           "ERROR: DataSets have different units" );
        System.out.println( message );
        return message;
      }

                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = (DataSet)ds.empty_clone(); 
    new_ds.addLog_entry( "Subtracted " + ds_to_subtract );

                                            // do the operation
    int num_data = ds.getNum_entries();
    Data data,
         subtract_data,
         new_data;
    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );        // get reference to the data entry

      subtract_data = ds_to_subtract.getData_entry_with_id( data.getGroup_ID());
 
      if ( subtract_data != null )         // there is a corresponding entry
      {                                    // to try to subtract 
        new_data = data.subtract( subtract_data );  
        if ( new_data != null )            // if they could be subtracted 
          new_ds.addData_entry( new_data );      
      }
    }
    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetSubtract Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    DataSetSubtract new_op = new DataSetSubtract( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }



}

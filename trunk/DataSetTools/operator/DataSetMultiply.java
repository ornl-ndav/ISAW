/*
 * @(#)DataSetMultiply.java   0.2  99/07/15   Dennis Mikkelson
 *                                 99/08/16   Added constructor to allow
 *                                            calling operator directly
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
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public DataSetMultiply( )
  {
    super( "Multiply by a DataSet" );

    Parameter parameter = new Parameter( 
                             "DataSet to Multiply by", 
                             new DataSet("DataSetToMultiplyBy",
                                         "Empty DataSet")  );
    addParameter( parameter );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds              The DataSet to which the operation is applied
   *  @parm   ds_to_multiply  The DataSet to multiply DataSet ds by
   */

  public DataSetMultiply( DataSet             ds,
                          DataSet             ds_to_multiply )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( ds_to_multiply );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
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
    DataSet new_ds = ds.empty_clone(); 
    new_ds.addLog_entry( "Multiplied by " + ds_to_multiply );
    new_ds.CombineAttributeList( ds_to_multiply );

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

/*
 * @(#)DataSetDivide.java   0.2  99/07/15   Dennis Mikkelson
 *                               99/08/16   Added constructor to allow
 *                                          calling operator directly
 *             
 * This operator divides two DataSets by dividing the corresponding 
 * Data "blocks" in the DataSets.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  *  Divide the corresponding Data "blocks" of the parameter DataSet from
  *  the Data "blocks" of the current DataSet.
  */

public class DataSetDivide extends  DataSetOperator 
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
  public DataSetDivide( )
  {
    super( "Divide by a DataSet" );

    Parameter parameter = new Parameter( 
                             "DataSet to Divide", 
                             new DataSet("DataSetToDivide","Empty DataSet"));
    addParameter( parameter );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /** 
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *  
   *  @param  ds            The DataSet to which the operation is applied
   *  @parm   ds_to_divide  The DataSet to be divided into DataSet ds.
   */

  public DataSetDivide( DataSet             ds,
                        DataSet             ds_to_divide )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( ds_to_divide );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getResult ------------------------------- */

                                     // Get the second DataSet from the 
                                     // parameter list and divide Data objects
                                     // in the current DataSet by corresponding
                                     // Data objects in the second DataSet.
  public Object getResult()
  {                                  // get the DataSet to divide by 
    DataSet ds_divisor = (DataSet)(getParameter(0).getValue());

                                     // get the current data set
    DataSet ds = this.getDataSet();

    if ( !ds.SameUnits( ds_divisor ) )    // DataSets are NOT COMPATIBLE TO 
      {                                   // COMBINE
        ErrorString message = new ErrorString(
                           "ERROR: DataSets have different units" );
        System.out.println( message );
        return message;
      }
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = ds.empty_clone(); 
    new_ds.addLog_entry( "Divided by " + ds_divisor );
    new_ds.CombineAttributeList( ds_divisor );

                                            // do the operation
    int num_data = ds.getNum_entries();
    Data data,
         divisor_data,
         new_data;
    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );        // get reference to the data entry

      divisor_data = ds_divisor.getData_entry_with_id( data.getGroup_ID() );
 
      if ( divisor_data != null )         // there is a corresponding entry
      {                                    // to try to divide 
        new_data = data.divide( divisor_data );  
        if ( new_data != null )            // if they could be divided 
          new_ds.addData_entry( new_data );      
      }
    }
    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetDivide Operator.  The list of parameters
   * and the reference to the DataSet to which it applies are also copied.
   */
  public Object clone()
  {
    DataSetDivide new_op    = new DataSetDivide( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }


}

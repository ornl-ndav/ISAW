/*
 * @(#)DataSetAdd.java   0.2  99/07/13   Dennis Mikkelson
 *                            99/08/16   Added constructor to allow
 *                                       calling operator directly
 *             
 * This operator adds two DataSets by adding the corresponding Data "blocks" in
 * the DataSets.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  *  Add corresponding Data "blocks" from the current DataSet to Data "blocks"
  *  from a second DataSet to form a new DataSet. This operator adds two 
  *  DataSets by adding the corresponding Data blocks in the DataSets to form 
  *  a new DataSet.  A Data block in the current DataSet will be added to the 
  *  first Data block in the second DataSet with the same Group ID provided
  *  that they have the same units, the same number of data values and extend 
  *  over the same X-interval.
  */

public class DataSetAdd extends    DataSetOperator 
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

  public DataSetAdd( )
  {
    super( "Add a DataSet" );

    Parameter parameter = new Parameter( "DataSet to Add", 
                              new DataSet("DataSetToAdd", "Empty DataSet") );
    addParameter( parameter );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @parm   ds_to_add   The DataSet to be added to DataSet ds.
   */

  public DataSetAdd( DataSet    ds,
                     DataSet    ds_to_add )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( ds_to_add );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getResult ------------------------------- */

                                     // Get the second DataSet from the 
                                     // parameter list and add corresponding
                                     // Data objects to Data objects from the
                                     // current DataSet.
  public Object getResult()
  {                                  // get the DataSet to add 
    DataSet ds_to_add = (DataSet)(getParameter(0).getValue());

                                     // get the current data set
    DataSet ds = this.getDataSet();

    System.out.println( "ds        = " + ds );
    System.out.println( "ds_to_add = " + ds_to_add );

    if ( !ds.SameUnits( ds_to_add ) )// DataSets are NOT COMPATIBLE TO COMBINE
      {
        ErrorString message = new ErrorString(
                           "ERROR: DataSets have different units" );
        System.out.println( message );
        return message;
      }
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = ds.empty_clone(); 
    new_ds.addLog_entry( "Added " + ds_to_add );
    new_ds.CombineAttributeList( ds_to_add );
                                            // do the operation
    int num_data = ds.getNum_entries();
    Data data,
         add_data,
         new_data;
    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );        // get reference to the data entry

      add_data = ds_to_add.getData_entry_with_id( data.getGroup_ID() );
 
      if ( add_data != null )              // there is a corresponding entry
      {                                    // to try to add
        new_data = data.add( add_data );  
        if ( new_data != null )            // if they could be added
          new_ds.addData_entry( new_data );      
      }
    }
    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetAdd Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    DataSetAdd new_op    = new DataSetAdd( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }

}

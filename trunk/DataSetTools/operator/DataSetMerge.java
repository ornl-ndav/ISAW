/*
 * @(#)DataSetMerge.java   0.2  99/07/20   Dennis Mikkelson
 *                              99/08/16   Added constructor to allow
 *                                         calling operator directly
 *             
 * This operator merges two DataSets by putting a copy of all spectra from
 * both DataSets into a new DataSet.  This will only be done if the X and Y
 * units match for the two DataSets being merged.
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.6  2000/10/03 22:13:10  dennis
 *  Now uses the constant empty DataSet, DataSet.EMPTY_DATA_SET,
 *   as a place holder for the DataSet parameter.
 *
 *  Revision 1.5  2000/09/11 23:05:54  dennis
 *  Added blank space in log message.
 *
 *  Revision 1.4  2000/07/10 22:35:52  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.5  2000/06/09 16:12:35  dennis
 *  Added getCommand() method to return the abbreviated command string for
 *  this operator
 *
 *  Revision 1.4  2000/05/16 15:36:34  dennis
 *  Fixed clone() method to also copy the parameter values from
 *  the current operator.
 *
 *  Revision 1.3  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 *
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;

/**
  * This operator creates a new DataSet by combining the Data blocks from the 
  * current DataSet with the Data blocks of a specified DataSet.  This can 
  * only be done if the two DataSets have the same X and Y units.  If a 
  * DataSet with N Data blocks is merged with a DataSet with M Data blocks,
  * the new DataSet will have N+M Data blocks.
  */

public class DataSetMerge extends    DataSetOperator 
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

  public DataSetMerge( )
  {
    super( "Merge with a DataSet" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /** 
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *  
   *  @param  ds          The DataSet to which the operation is applied
   *  @parm   ds_to_merge The DataSet to merge with DataSet ds.
   */

  public DataSetMerge( DataSet             ds,
                       DataSet             ds_to_merge )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( ds_to_merge );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "Merge";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "DataSet to Merge",
                                          DataSet.EMPTY_DATA_SET ); 
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

                                     // Get the second DataSet from the 
                                     // parameter list and merge Data objects 
                                     // with Data objects from the current
                                     // DataSet.
  public Object getResult()
  {                                  // get the DataSet to merge with 
    DataSet ds_to_merge = (DataSet)(getParameter(0).getValue());

                                     // get the current data set
    DataSet ds = this.getDataSet();

    if ( !ds.SameUnits(ds_to_merge)) // DataSets are NOT COMPATIBLE TO COMBINE
      return null;
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = ds.empty_clone(); 
    new_ds.addLog_entry( "Merged with " + ds_to_merge );
    new_ds.CombineAttributeList( ds_to_merge );

                                           // do the operation
    Data data, 
         new_data; 
                                           // put in Data blocks from the 
                                           // current data set
    int num_data = ds.getNum_entries();
    for ( int i = 0; i < num_data; i++ )
    {
      data     = ds.getData_entry( i );    // get reference to the data entry
      new_data = (Data)data.clone( );      // clone it and add it to the new 
      new_ds.addData_entry( new_data );    // DataSet  
    }
                                           // put in Data blocks from the 
                                           // second data set
    num_data = ds_to_merge.getNum_entries();
    for ( int i = 0; i < num_data; i++ )
    {
      data     = ds_to_merge.getData_entry( i );   // get reference to the data
      new_data = (Data)data.clone( );              // clone it and add it to 
      new_ds.addData_entry( new_data );            // the new DataSet
    }

    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetMerge Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    DataSetMerge new_op    = new DataSetMerge( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}

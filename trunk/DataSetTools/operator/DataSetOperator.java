/*
 * @(#)DataSetOperator.java     0.1  99/06/04  Dennis Mikkelson
 *
 */

package DataSetTools.operator;

import java.util.Vector;
import java.io.*;
import DataSetTools.dataset.*;

/**
 * Root class for operators that operate on a DataSet object. Derived clases
 * must provide a constructor that sets up an appropriate list of operations
 * and implement the method "getResult()" to provide the result of carrying out
 * the operation.
 */

abstract public class DataSetOperator extends Operator implements Serializable
{
   private DataSet data_set;

   protected DataSetOperator( String title )
   {
      super( title );
      this.data_set = null;
   } 

  /**
   * Set the "this" DataSet for the operator.  That is, a DataSetOperator is
   * an operator that belongs to the set of operations that can be performed
   * on a DataSet.  The DataSet that the operator operates on is set by calling
   * this method when the operator is added to the list of operators for a
   * DataSet.
   */
  public void setDataSet( DataSet data_set )
  {
    this.data_set = data_set; 
  }

  /**
   * Get the "this" DataSet for the operator.  This is the data set on which
   * the operation is to be performed.
   */
  public DataSet getDataSet()
  {
    return data_set;
  }


  /**
   * Get a copy of the current DataSetOperator.  The list of parameters
   * and the reference to the DataSet to which it applies is copied.  This
   * method must be implemented in classes derived from DataSetOperator.
   */
  abstract public Object clone();

} 

/*
 * @(#)DataSetOperator.java     0.1  99/06/04  Dennis Mikkelson
 *
 */

package DataSetTools.operator;

import java.util.Vector;
import java.io.*;
import DataSetTools.dataset.*;

/**
 *   Root class for operators that operate on a DataSet object.  This class 
 * extends the generic Operator class by adding a DataSet member and methods 
 * to get/set the DataSet member. 
 *
 *   A DataSetOperator is typically used in one of two ways.  First, the 
 * operator might be added to the list of operators that work on a particular
 * DataSet.  In this case, the DataSet associated with the operator is set
 * by the DataSet's addOperator method.  If the default constructor for the
 * specific DataSetOperator is used, any additional parameters required
 * must be set later.  In particular, a GUI can get the operator from the 
 * DataSet and have the user specify values for any remaining parameters.
 * After the parameter values are set the getResult() method of the operator
 * is used to carry out the operation.
 *
 *   Alternatively, an operator can be applied directly if the so-called
 * "full constructor" is used.  A full constructor includes the DataSet
 * that is associated with the operator, as well as values for all other 
 * parameters needed by the operator.  In this case, the operation can be 
 * carried out immediately by calling getResult().
 *
 * @see Operator
 * @see DataSetAdd
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


  /**
   *  Trace the finalization of objects
   */
/*
  protected void finalize() throws IOException
  {
    System.out.println( "finalize Operator" );
  }
*/
} 

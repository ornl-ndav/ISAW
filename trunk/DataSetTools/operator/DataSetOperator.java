/*
 * @(#)DataSetOperator.java     0.1  99/06/04  Dennis Mikkelson
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.4  2000/11/10 22:41:34  dennis
 *     Introduced additional abstract classes to better categorize the operators.
 *  Existing operators were modified to be derived from one of the new abstract
 *  classes.  The abstract base class hierarchy is now:
 *
 *   Operator
 *
 *    -GenericOperator
 *       --GenericLoad
 *       --GenericBatch
 *
 *    -DataSetOperator
 *      --DS_EditList
 *      --DS_Math
 *         ---ScalarOp
 *         ---DataSetOp
 *         ---AnalyzeOp
 *      --DS_Attribute
 *      --DS_Conversion
 *         ---XAxisConversionOp
 *         ---YAxisConversionOp
 *         ---XYAxesConversionOp
 *      --DS_Special
 *
 *     To allow for automatic generation of hierarchial menus, each new operator
 *  should fall into one of these categories, or a new category should be
 *  constructed within this hierarchy for the new operator.
 *
 *  Revision 1.3  2000/07/10 22:35:55  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.7  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 *
 *
 */

package DataSetTools.operator;

import java.util.Vector;
import java.io.*;
import DataSetTools.dataset.*;

/**
 *   Root class for operators that operate on a DataSet object.  This class 
 * extends the Operator class by adding a DataSet member and methods 
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
   *
   *  @param  data_set  the DataSet that this operator will operate on when
   *                    the getResult() method is called.
   */
  public void setDataSet( DataSet data_set )
  {
    this.data_set = data_set; 
  }

  /**
   * Get the "this" DataSet for the operator.  This is the data set on which
   * the operation is to be performed.
   *
   * @return  a reference to the DataSet this operator acts on.
   */
  public DataSet getDataSet()
  {
    return data_set;
  }


  /* -------------------------- getCategory -------------------------------- */
  /**
   * Get the category of this operator
   *
   * @return  A String specifying the category of this operator.  This is
   *          actually the category of the abstract base class from which
   *          the current operator is directly derived.
   */
  public String getCategory()
  {
    return DATA_SET_OPERATOR;
  }

  /* ------------------------ getCategoryList ------------------------------ */
  /**
   * Get an array of strings listing the operator category names of base
   * classes for this operator.  The first entry in the array is the string:
   *
   *      Operator.OPERATOR
   *
   * The last entry is the category of the last abstract base class that is
   * is a base class for the current operator.
   * 
   * @return  A list of Strings specifying the category names of the abstract
   * base classes from which this operator is derived.
   */
  public String[] getCategoryList()
  {
    String partial_list[] = super.getCategoryList();  // get list of ancestor
                                                      // categories and put 
                                                      // them in a new larger
                                                      // list.
    return AppendCategory( DATA_SET_OPERATOR, partial_list );
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

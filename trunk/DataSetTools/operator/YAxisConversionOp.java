/*
 * @(#)YAxisConversionOp.java     1.0  2000/11/08  Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.1  2000/11/10 23:10:34  dennis
 *  New abstract base class for operators.  Allows automatic
 *  generation of hierarchical menus using operator categories.
 *
 *
 */

package DataSetTools.operator;

import java.io.*;

/**
 * Base class for DataSetOperators that convert the Y axis to different units.
 *
 * @see Conversion 
 * @see TrueAngle 
 *
 */

abstract public class YAxisConversionOp extends    DS_Conversion 
                                        implements Serializable
{
   protected YAxisConversionOp( String title )
   {
      super( title );
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
    return Y_AXIS_CONVERSION;
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
    return AppendCategory( Y_AXIS_CONVERSION, partial_list );
  }

} 

/*
 * @(#)XAxisConversionOp.java   0.1  2000/04/20   Dennis Mikkelson
 *
 * $Log$
 * Revision 1.1  2000/11/10 23:10:31  dennis
 * New abstract base class for operators.  Allows automatic
 * generation of hierarchical menus using operator categories.
 *
 *
 * Revision 1.1  2000/07/10 22:36:28  dennis
 * July 10, 2000 version... many changes
 *
 * Revision 1.7  2000/05/16 15:35:04  dennis
 * fixed error in documentation due to DOS text format.
 *
 * Revision 1.6  2000/05/11 16:41:28  dennis
 * Added RCS logging
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;

/**
  * This abstract class is the base class for DataSetOperators that convert the
  * X axis to different units.
  *
  *  @see DS_Conversion
  *  @see DiffractomterTofToD
  */

abstract public class XAxisConversionOp extends    DS_Conversion 
                                        implements Serializable
{
  public XAxisConversionOp( String title )
  {
    super( title );
    Parameter parameter;
  }

  /* -------------------------- getCategory -------------------------------- */
  /**
   * Get the category of this DataSet operator
   *
   * @return  A String specifying the category of this operator.
   */
  public String getCategory()
  {
    return X_AXIS_CONVERSION;
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
    return AppendCategory( X_AXIS_CONVERSION, partial_list );
  }


  /* -------------------------- new_X_label ---------------------------- */
  /**
   * Get string label for converted x values.
   *
   *  @return  String describing the x label and units for converted x values.
   */
  abstract public String new_X_label();



  /* -------------------------- convert_X_Value ---------------------------- */
  /**
   * Evaluate the axis conversion function at one point only.
   *
   *  @param  x    the x-value where the axis conversion function is to be
   *               evaluated.
   *
   *  @param  i    the index of the Data block for which the axis conversion
   *               function is to be evaluated.
   *
   *  @return  the value of the axis conversion function at the specified x.
   */
  abstract public float convert_X_Value( float x, int i );



  /* --------------------------- getXRange() ------------------------------- */
  /**
   *  Get the range of converted X-Values for the entire DataSet, if a
   *  DataSet has been assigned to this operator.
   *
   *  @return  An XScale containing the range of converted X-Values for this 
   *           operator and it's DataSet.  If this operator has not been 
   *           associated with a DataSet, or if the DataSet is empty, this
   *           method returns null.
   */
  public UniformXScale getXRange()
  {
    DataSet ds = this.getDataSet();
    if ( ds == null )
      return null;

    int n_data = ds.getNum_entries();
    if ( n_data <= 0 )
      return null; 

    Data   d     = ds.getData_entry(0);
    XScale scale = d.getX_scale();

    float start = convert_X_Value( scale.getStart_x(), 0 ); 
    float end   = convert_X_Value( scale.getEnd_x(), 0 ); 
    float x1, 
          x2;
 
    boolean scale_reversed = false;
    if ( start >  end )
      scale_reversed = true;
    
    for ( int i = 1; i < n_data; i++ )
    {
      d     = ds.getData_entry(i);
      scale = d.getX_scale();

      x1 = convert_X_Value( scale.getStart_x(), i ); 
      x2 = convert_X_Value( scale.getEnd_x(), i ); 

      if ( !scale_reversed )
      {
        if ( x1 < start )
          start = x1;
        else if ( x2 > end )
          end = x2;
      } 
      else
      {
        if ( x1 > start )
          start = x1;
        else if ( x2 < end )
          end = x2;
      } 
    }

    if ( !scale_reversed )
      return new UniformXScale( start, end, 2 );
    else
      return new UniformXScale( end, start, 2 );
  }
}

/*
 * File:  XAxisConversionOp.java
 *
 * Copyright (C) 2000, Dennis Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.4  2003/06/16 19:02:27  pfpeterson
 * Removed old code and updated to work with new getCategoryList() code
 * in base operator class.
 *
 * Revision 1.3  2002/11/27 23:17:04  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/07/31 16:06:59  dennis
 * Implements IDataPointInfo and provides "wrapper" function that
 * call the numeric calculation methods of derived conversion ops.
 *
 * Revision 1.1  2002/02/22 21:01:00  pfpeterson
 * Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Conversion.XAxis;

import  java.io.*;
import  java.text.*;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.operator.DataSet.Conversion.DS_Conversion;

/**
  * This abstract class is the base class for DataSetOperators that convert the
  * X axis to different units.
  *
  *  @see DS_Conversion
  *  @see DiffractometerTofToD 
  */

abstract public class XAxisConversionOp extends    DS_Conversion 
                                        implements IDataPointInfo,
                                                   Serializable
{
  private static String[] categoryList=null;
  public XAxisConversionOp( String title )
  {
    super( title );
    Parameter parameter;
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
    if(categoryList==null)
      categoryList=createCategoryList();

    return categoryList;
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


  /* ------------------------- PointInfoLabel --------------------------- */
  /**
   * Get string label for information about a point on the xaxis.
   *
   *  @param  x    the x-value for which the axis label is to be obtained.
   *  @param  i    the index of the Data block that will be used for obtaining
   *               the label.
   *
   *  @return  String describing the information provided by X_Info().
   */
  public String PointInfoLabel( float x, int i )
  {
    return new_X_label();
  }


  /* ----------------------------- PointInfo ----------------------------- */
  /**
   * Get the information for the specified point and Data block.
   *
   *  @param  x    the x-value for which the axis information is to be obtained.   *  @param  i    the index of the Data block that will be used for obtaining
   *               the information about the x axis.
   *
   *  @return  information for the x axis at the specified x.
   */
  public String PointInfo( float x, int i )
  {
    float value = convert_X_Value( x, i );
    NumberFormat f = NumberFormat.getInstance();
    return f.format( value );
  }


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

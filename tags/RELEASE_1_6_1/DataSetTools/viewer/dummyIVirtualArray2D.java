
/*
 * File:  dummyIVirtualArray2D.java
 *
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.3  2004/02/16 05:25:04  millermi
 * - Added methods getErrors(), setErrors(), setSquareRootErrors(),
 *   and getErrorValue() which allow an array of errors to be
 *   associated with the data in that array.
 * - ******THE METHODS ABOVE STILL NEED TO HAVE A MEANINGFUL
 *   IMPLEMENTATION WRITTEN FOR THEM.******
 *
 * Revision 1.2  2003/12/18 22:46:06  millermi
 * - This file was involved in generalizing AxisInfo2D to
 *   AxisInfo. This change was made so that the AxisInfo
 *   class can be used for more than just 2D axes.
 *
 * Revision 1.1  2003/10/27 15:08:05  rmikk
 * Initial Checkin
 *
 */


package DataSetTools.viewer;
import DataSetTools.components.View.*;


public class dummyIVirtualArray2D implements IVirtualArray2D{

  float[][] Data ={ {1f,3f,5f,7f},
                    {2f,4f,6f,8f},
                    {0f,1f,2f,3f},
                    {6f,5f,4f,3f}
                   };
  public dummyIVirtualArray2D(){
  }
  public int getDimension(){
     return 2;
  }
 /**
   * Returns the attributes of the data array in a AxisInfo wrapper.
   * This method will take in an integer to determine which axis
   * info is being retrieved for.
   */
   public AxisInfo getAxisInfo( int axis ){
        return null;
   }
   
  /**
   * Sets the attributes of the data array within a AxisInfo wrapper.
   * This method will take in an integer to determine which axis
   * info is being altered.
   */
   public void setAxisInfo( int axis, float min, float max,
                              String label, String units, boolean islinear ){
   }
   
  /**
   * Sets the attributes of the data array within a AxisInfo wrapper.
   * This method will take in an integer to determine which axis
   * info is being altered.
   */
   public void setAxisInfo( int axis, AxisInfo info ){
   }
     
  /**
   * This method will return the title assigned to the data. 
   */
   public String getTitle(){
     return "xxx";
   }
   
  /**
   * This method will assign a title to the data. 
   */
   public void setTitle( String title ){
    ;
   }
  
  /*
   ***************************************************************************
   * The following methods must include implementation to prevent
   * the user from exceeding the initial array size determined
   * at creation of the array. If an M x N array is specified,
   * the parameters must not exceed (M-1,N-1). 
   ***************************************************************************
   */
   
  /**
   * Get values for a portion or all of a row.
   * The "from" and "to" values must be direct array reference, i.e.
   * because the array positions start at zero, not one, this must be
   * accounted for. If the array passed in exceeds the bounds of the array, 
   * get values for array elements and ignore extra values.
   */
   public float[] getRowValues( int row_number, int from, int to ){
         return null;
   }
   
  /**
   * Set values for a portion or all of a row.
   * The "from" and "to" values must be direct array reference, i.e.
   * because the array positions start at zero, not one, this must be
   * accounted for. If the array passed in exceeds the bounds of the array, 
   * set values for array elements and ignore extra values.
   */
   public void setRowValues( float[] values, int row_number, int start ){
      return;
   }
   
  /**
   * Get values for a portion or all of a column.
   * The "from" and "to" values must be direct array reference, i.e.
   * because the array positions start at zero, not one, this must be
   * accounted for. If the array passed in exceeds the bounds of the array, 
   * get values for array elements and ignore extra values.
   */
   public float[] getColumnValues( int column_number, int from, int to ){
     return null;
   }
   
  /**
   * Set values for a portion or all of a column.
   * The "from" and "to" values must be direct array reference, i.e.
   * because the array positions start at zero, not one, this must be
   * accounted for. If the array passed in exceeds the bounds of the array, 
   * set values for array elements and ignore extra values.
   */
   public void setColumnValues( float[] values, int column_number, int start ){
     return;
   }
   
  /**
   * Get value for a single array element.
   */
   public float getDataValue( int row_number, int column_number ){
      return 0.0f;
   }
   
  /**
   * Set value for a single array element.
   */
   public void setDataValue( int row_number, int column_number, float value ){
     return;
   }
      
  /**
   * Set all values in the array to a value. This method will usually
   * serve to "initialize" or zero out the array. 
   */
   public void setAllValues( float value ){return;
   }
   
  /**
   * Returns the values in the specified region.
   * The vertical dimensions of the region are specified by starting 
   * at first row and ending at the last row. The horizontal dimensions 
   * are determined by the first column and last column. 
   */ 
   public float[][] getRegionValues( int first_row, int last_row,
                                     int first_column, int last_column ){
        return null;
   }
  /**  
   * Sets values for a specified rectangular region. This method takes 
   * in a 2D array that is already organized into rows and columns
   * corresponding to a portion of the virtual array that will be altered.
   */
   public void setRegionValues( float[][] values, 
                                int row_number,
				int column_number ){
         return ;
   }
				
  /**
   * Returns number of rows in the array.
   */
   public int getNumRows(){ return 4;
   }

  /**
   * Returns number of columns in the array.
   */   
   public int getNumColumns(){return 4;
   }
  
 /**
  * Set the error values that correspond to the data. The dimensions of the
  * error values array should match the dimensions of the data array. Zeroes
  * will be used to fill undersized error arrays. Values that are in an array
  * that exceeds the data array will be ignored.
  *
  *  @param  error_values The array of error values corresponding to the data.
  *  @return true if data array dimensions match the error array dimensions.
  */
  public boolean setErrors( float[][] error_values )
  {/*
    errors_set = true;
    // by setting these values, do not use the calculated
    // square-root errors
    setSquareRootErrors( false );
    
    // Check to see if error values array is same size as data array.
    // If so, reference the array passed in.
    if( error_values.length == getNumRows() &&
        error_values[0].length == getNumColumns() )
    {
      errorArray = error_values;
      return true;
    }
    // If dimensions are not equal, copy values that are valid into an array
    // the same size as the data.
    else
    {
      errorArray = new float[getNumRows()][getNumColumns()];
      // If error_values is too large, the extra values are ignored
      // by these "for" loops. If too small, the zeroes are inserted.
      for( int row = 0; row < getNumRows(); row++ )
      {
        for( int col = 0; col < getNumColumns(); col++ )
	{
	  if( row >= error_values.length || col >= error_values[0].length )
	  {
	    errorArray[row][col] = 0;
	  }
	  else
	  {
	    errorArray[row][col] = error_values[row][col];
	  }
	}
      }
      return false;
    }*/
    return false; // remove if uncommenting code above.
  }
  
 /**
  * Get the error values corresponding to the data. setSquareRootErrors(true)
  * or setErrors(array) must be called to have meaningful values returned.
  * By default, null will be returned. If square-root values are
  * desired and the data value is negative, the square-root of the positive
  * value will be returned. If setErrors() was called, then the error array
  * passed in will be returned (this array will be always have the same
  * dimensions as the data, it will be modified if the dimensions are
  * different).
  *
  *  @return error values of the data.
  */
  public float[][] getErrors()
  {/*
    // if setSquareRootErrors(true) was called
    if( use_sqrt )
    {
      float[][] sqrt_errors = new float[getNumRows()][getNumColumns()];
      for( int row = 0; row < getNumRows(); row++ )
      {
        for( int col = 0; col < getNumColumns(); col++ )
        {
          sqrt_errors[row][col] = (float)
	            Math.sqrt( (double)Math.abs( getDataValue(row,col) ) );
        }
      }
      return sqrt_errors;
    }
    // if the errors were set using the setErrors() method
    if( errors_set )
      return errorArray;
    // if neither use_sqrt nor errors_set, return null.*/
    return null;
  }
  
 /**
  * Use this method to specify whether to use error values that were passed
  * into the setErrors() method or to use the square-root of the data value.
  *
  *  @param  use_sqrt_errs If true, use square-root.
  *                        If false, use set error values if they exist.
  */
  public void setSquareRootErrors( boolean use_sqrt_errs )
  {
    //use_sqrt = use_sqrt_errs;
  }
 
 /**
  * Get an error value for a given row and column. Returns Float.NaN if
  * row or column are invalid.
  *
  *  @param  row Row number.
  *  @param  column Column number.
  *  @return error value for data at [row,column]. If row or column is invalid,
  *          or if setSquareRootErrors() or setErrors is not called,
  *          Float.NaN is returned.
  */
  public float getErrorValue( int row, int column )
  {/*
    // make sure row/column are valid values.
    if( row >= getNumRows() || column >= getNumColumns() )
      return Float.NaN;
    // return sqrt error value if specified.
    if( use_sqrt )
      return (float)Math.sqrt( (double)Math.abs( getDataValue(row,column) ) );
    // if the errors were set using the setErrors() method, return them
    if( errors_set )
      return errorArray[row][column];
    // if neither use_sqrt or errors_set, then return NaN*/
    return Float.NaN;
  }
 


}

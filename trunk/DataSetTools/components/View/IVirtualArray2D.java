/*
 * File: IVirtualArray2D.java
 *
 * Copyright (C) 2003, Mike Miller
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
 * Primary   Mike Miller <millermi@uwstout.edu>
 * Contact:  Student Developer, University of Wisconsin-Stout
 *           
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.4  2003/10/22 20:15:08  millermi
 *  - Removed methods now defined by IVirtualArray.
 *  - Added java docs where needed.
 *
 *  Revision 1.3  2003/08/07 15:56:14  dennis
 *  - Added method setAxisInfoVA() with alternate parameters.
 *    Since getAxisInfoVA() returns an AxisInfo2D object, this
 *    new method takes in an AxisInfo2D object.
 *    (Mike Miller)
 *
 *  Revision 1.2  2003/05/16 15:01:54  dennis
 *  Minor fix to java doc comments and added acknowledgement of NSF funding.
 *
 */
 
package DataSetTools.components.View;

import DataSetTools.components.View.TwoD.*;

/**
 * This interface is implemented by classes that can produce a "logical"
 * 2-D array of floats and is used to pass data to viewers and view components.
 * Along with the data, some data attributes are kept in the virtual 
 * array.  An IVirtualArray2D has the same logical format as a typical 2D array.
 * Below is an example of an M x N virtual array.
 *
 * | (0,0)    (0,1)   (0,2)  ...  (0,N-1)  |
 * | (1,0)    (1,1)   (1,2)  ...  (1,N-1)  |
 * | (2,0)    (2,1)   (2,2)  ...  (2,N-1)  |
 * |  ...   ...   ...  ...   ...           |
 * | (M-1,0) (M-1,1) (M-1,2) ... (M-1,N-1) |
 *
 * All references to rows and columns are interpretted
 * to mean row number and column number where an
 * M x N array has M rows, and N columns. The row numbers
 * start at zero and go to M-1 and the column numbers
 * start at zero and go to N-1. 
 *
 *  @see DataSetTools.components.View.AxisInfo2D
 */

public interface IVirtualArray2D extends IVirtualArray
{
 /**
  * Returns the attributes of the data array in a AxisInfo2D wrapper.
  * This method will take in a boolean value to determine for which axis
  * info is being retrieved for.    true = X axis, false = Y axis.
  *
  *  @param  isX - Use AxisInfo2D.XAXIS (true) or AxisInfo2D.YAXIS (false). 
  *  @return the axis info for the axis specified.
  */
  public AxisInfo2D getAxisInfoVA( boolean isX );
  
 /**
  * Sets the attributes of the data array within a AxisInfo2D wrapper.
  * This method will take in a boolean value to determine for which axis
  * info is being altered.	    true = X axis, false = Y axis.
  *
  *  @param  isX - Use AxisInfo2D.XAXIS (true) or AxisInfo2D.YAXIS (false).
  *  @param  min - Minimum value for this axis.
  *  @param  max - Maximum value for this axis.
  *  @param  label - label associated with the axis.
  *  @param  units - units associated with the values for this axis.
  *  @param  islinear - is axis linear (true) or logarithmic (false)
  */
  public void setAxisInfoVA( boolean isX, float min, float max,
			     String label, String units, boolean islinear ); 
  
 /**
  * Sets the attributes of the data array within a AxisInfo2D wrapper.
  * This method will take in a boolean value to determine for which axis
  * info is being altered.	    true = X axis, false = Y axis.
  * 
  *  @param  isX - Use AxisInfo2D.XAXIS (true) or AxisInfo2D.YAXIS (false).
  *  @param  info - The axis info object associated with the axis specified.
  */
  public void setAxisInfoVA( boolean isX, AxisInfo2D info );
  
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
  *
  *  @param  row   the row number being altered
  *  @param  from  the column number of first element to be altered
  *  @param  to    the column number of the last element to be altered
  *  @return If row, from, and to are valid, an array of floats containing
  *	     the specified section of the row is returned.
  *	     If row, from, or to are invalid, an empty 1-D array is returned.
  */
  public float[] getRowValues( int row, int from, int to );
  
 /**
  * Set values for a portion or all of a row.
  * The "from" and "to" values must be direct array reference, i.e.
  * because the array positions start at zero, not one, this must be
  * accounted for. If the array passed in exceeds the bounds of the array, 
  * set values for array elements and ignore extra values.
  *
  *  @param values  array of elements to be put into the row
  *  @param row     row number of desired row
  *  @param start   what column number to start at
  */
  public void setRowValues( float[] values, int row, int start );
  
 /**
  * Get values for a portion or all of a column.
  * The "from" and "to" values must be direct array reference, i.e.
  * because the array positions start at zero, not one, this must be
  * accounted for. If the array passed in exceeds the bounds of the array, 
  * get values for array elements and ignore extra values.
  *
  *  @param  column  column number of desired column
  *  @param  from    the row number of first element to be altered
  *  @param  to      the row number of the last element to be altered
  *  @return If column, from, and to are valid, an array of floats containing
  *	     the specified section of the row is returned.
  *	     If row, from, or to are invalid, an empty 1-D array is returned.
  */
  public float[] getColumnValues( int column, int from, int to );
  
 /**
  * Set values for a portion or all of a column.
  * The "from" and "to" values must be direct array reference, i.e.
  * because the array positions start at zero, not one, this must be
  * accounted for. If the array passed in exceeds the bounds of the array, 
  * set values for array elements and ignore extra values.
  *
  *  @param values  array of elements to be put into the column
  *  @param column  column number of desired column
  *  @param start   what row number to start at
  */
  public void setColumnValues( float[] values, int column, int start );
  
 /**
  * Get value for a single array element.
  *
  *  @param  row     row number of element
  *  @param  column  column number of element
  *  @return If element is found, the float value for that element is returned.
  *	     If element is not found, zero is returned.
  */ 
  public float getDataValue( int row, int column );
  
 /**
  * Set value for a single array element.
  *
  *  @param  row     row number of element
  *  @param  column  column number of element
  *  @param  value   value that element will be set to
  */
  public void setDataValue( int row, int column, float value );
  
 /**
  * Returns the values in the specified region.
  * The vertical dimensions of the region are specified by starting 
  * at first row and ending at the last row. The horizontal dimensions 
  * are determined by the first column and last column. 
  *
  *  @param  row_start  first row of the region
  *  @param  row_stop	last row of the region
  *  @param  col_start  first column of the region
  *  @param  col_stop	last column of the region
  *  @return If a portion of the array is specified, a 2-D array copy of 
  *	     this portion will be returned. 
  *	     If all of the array is specified, a reference to the actual array
  *	     will be returned.
  */
  public float[][] getRegionValues( int row_start, int row_stop,
				    int col_start, int col_stop );
 /**  
  * Sets values for a specified rectangular region. This method takes 
  * in a 2D array that is already organized into rows and columns
  * corresponding to a portion of the virtual array that will be altered.
  *
  *  @param  values	2-D array of float values 
  *  @param  row_start  first row of the region being altered
  *  @param  col_start  first column of the region being altered
  */
  public void setRegionValues( float[][] values, 
			       int row_start,
        		       int col_start );
        		       
 /**
  * Returns number of rows in the array.
  *
  *  @return This returns the number of rows in the array. 
  */ 
  public int getNumRows();

 /**
  * Returns number of columns in the array.
  *
  *  @return This returns the number of columns in the array. 
  */
  public int getNumColumns();
     
}

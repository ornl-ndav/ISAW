
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
 


}

/*
 * File: RowColTimeVirtualArray.java
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
 * Revision 1.18  2005/07/19 19:03:16  rmikk
 * Fixed a spelling error
 *
 * Revision 1.17  2005/07/08 13:24:20  rmikk
 * Eliminated a possible null pointer exception
 *
 * Revision 1.16  2004/09/15 22:03:52  millermi
 * - Updated LINEAR, TRU_LOG, and PSEUDO_LOG setting for AxisInfo class.
 *   Adding a second log required the boolean parameter to be changed
 *   to an int. These changes may affect any ObjectState saved configurations
 *   made prior to this version.
 *
 * Revision 1.15  2004/07/29 13:36:42  rmikk
 * Fixed the names for the keys of some more of the ViewerState variables
 *
 * Revision 1.14  2004/06/04 15:10:59  rmikk
 * Eliminated a few unused variables
 * Added code so LargeJTable repositions itself to the pointedAt point.
 *
 * Revision 1.13  2004/05/06 17:35:45  rmikk
 * Added a setTime Method 
 * Added an argument to the Selected2D constructor
 * Access to the super.setTime is now through the SetTime method
 *
 * Revision 1.12  2004/03/15 19:34:00  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.11  2004/03/15 03:29:02  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.10  2004/02/16 05:25:04  millermi
 * - Added methods getErrors(), setErrors(), setSquareRootErrors(),
 *   and getErrorValue() which allow an array of errors to be
 *   associated with the data in that array.
 * - ******THE METHODS ABOVE STILL NEED TO HAVE A MEANINGFUL
 *   IMPLEMENTATION WRITTEN FOR THEM.******
 *
 * Revision 1.9  2004/01/24 22:41:15  bouzekc
 * Removed/commented out unused imports/variables.
 *
 * Revision 1.8  2003/12/18 22:46:06  millermi
 * - This file was involved in generalizing AxisInfo2D to
 *   AxisInfo. This change was made so that the AxisInfo
 *   class can be used for more than just 2D axes.
 *
 * Revision 1.7  2003/12/15 00:33:52  rmikk
 * Now notifies the observers of a DataSet when the selected groups have changed
 *
 * Revision 1.6  2003/12/11 22:08:53  rmikk
 * Added a kill command to remove orphaned windows
 *
 * Revision 1.5  2003/11/09 22:28:45  rmikk
 * Fixed an error so that the region selected in the table view is correct
 *
 * Revision 1.4  2003/11/06 21:26:10  rmikk
 * Implemented method to select data sets via the table.  In
 * this form only data sets are selected and not time intervals.
 *
 * Revision 1.3  2003/11/06 20:01:57  rmikk
 * Eliminated warning messages by removing paths from
 *   ViewMenuItems
 * Now throws and Illegal argument exception if the DataSet
 *    does not have enough information to make a table
 *
 * Revision 1.2  2003/10/28 19:57:58  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.1  2003/10/27 15:11:32  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.viewer.Table;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.Menu.*;
import gov.anl.ipns.ViewTools.UI.*;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import DataSetTools.dataset.*;
import java.io.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;
import DataSetTools.components.ui.*;
import java.awt.*;

/**
*    This class produces array information for row and column values at a
*    particular time slice.  Controls are available to change the time slice,
*    change the detector, ranges of rows and columns to consider, and ranges of
*    times to consider.
*/
public class RowColTimeVirtualArray extends 
                          DataSetTools.viewer.Table.Time_Slice_TableModel 
                       implements IArrayMaker_DataSet, IVirtualArray2D,doesColumns{
  DataSet DS;
  String Title;
  JCheckBoxMenuItem jmErr=null; 
  JCheckBoxMenuItem jmInd=null;
  public AnimationController acontrol= null ;
  XScaleChooserUI XScl= null;
  public float[] xvals1;
  public int TimeIndex = -1;
  public JPanel JRowColPanel= null;
  public XScale x_scale= null;
  ViewerState state;

  /**
  *    Constructor for this Virtual Array
  *  @param DS  The DataSet with the values in row and column
  *  @param time   The time Slice to show
  *  @param showErrors  If true, Errors will be displayed in addition to the
  *                       yvalues
  *  @param showInd If true, the index of the group with the given row and column
  *           will be displayed.
  *  @param  state This stores state information for this viewer. Some state info is
  *  <table><tr> <td>Name</td><td>Value Data Type and subrange info </td></tr>
  *  <tr> <td>TableTS_TimeInd</td><td>Integer from 0 to the number of time channels</td></tr>
  *  <tr> <td>TABLE_TS_NXSTEPS</td><td>Positive Integer. # of time steps</td></tr>
  *  <tr> <td>TABLE_TS_MAX_TIME</td><td>Float, max TOF or Xvalue</td></tr>
  *  <tr> <td>TABLE_TS_MIN_TIME</td><td>Float, min TOF or Xvalue</td></tr>
  *  <tr> <td>TableTimeSliceMaxCol</td><td>Integer-last col to look at</td></tr>
  *  <tr> <td>TableTimeSliceMinCol</td><td>Integer-first col to look at</td></tr>
  *  <tr> <td>TableTS_MaxRow</td><td>Integer-last row to look at</td></tr>
  *  <tr> <td>TableTS_MinRow</td><td>Integer-first row to look at</td></tr>
  *  <tr> <td>TableTS_Detector Num</td><td>-1 or DetectorNumber to view</td></tr>
  *  <tr> <td>TABLE_TS_MAX_TIME</td><td>Last time to view</td></tr>
  *  <tr> <td>TABLE_TS_MIN_TIME</td><td>First time to view</td></tr>
  *  <tr> <td>TableTS_ShowError</td><td>Show Errors</td></tr>
  *  <tr> <td>TableTS_ShowIndex</td><td>Show Indices</td></tr>
  *<  tr> <td>TableTS</td><td>"" to reset all states with defaults</td></tr>
  *</table>
  */
  public RowColTimeVirtualArray(DataSet DS, float time, boolean showErrors, 
     boolean showInd, ViewerState state) throws IllegalArgumentException
    {
     super( DS, time, false,false);
     this.DS = DS;
     Title = DS.getTitle();
     this.state = state;
     initState();
    }

  public int getDimension(){
     return 2;
  }

  //---------------- IVirtualArray2D Methods----------------
  /**
   * Returns the attributes of the data array in a AxisInfo wrapper.
   * This method will take in an integer to determine which axis
   * info is being retrieved for.
   */
  public AxisInfo getAxisInfo( int axis )
    {
     return null;
    }
   


  /**
   * Sets the attributes of the data array within a AxisInfo wrapper.
   * This method will take in an integer to determine which axis
   * info is being altered.
   */
  public void setAxisInfo( int axis, float min, float max,
                           String label, String units, int scale )
    {
    }


  /**
   * Sets the attributes of the data array within a AxisInfo wrapper.
   * This method will take in an integer to determine which axis
   * info is being altered.
   */
  public void setAxisInfo( int axis, AxisInfo info )
    {
    }
   


  /**
   * This method will return the title assigned to the data. 
   */
  public String getTitle()
    { 
     return Title;
    }
   

  /**
   * This method will assign a title to the data. 
   */
  public void setTitle( String title )
    {
     Title = title;
    }
 

   
  /**
   * Get values for a portion or all of a row.
   * The "from" and "to" values must be direct array reference, i.e.
   * because the array positions start at zero, not one, this must be
   * accounted for. If the array passed in exceeds the bounds of the array, 
   * get values for array elements and ignore extra values.
   */
  public float[] getRowValues( int row_number, int from, int to )
    {
     row_number =AdjustRowCol( row_number, getNumRows());
     from =AdjustRowCol(from, getNumColumns());
     to =AdjustRowCol(to, getNumColumns());
     if( from > to) 
        return new float[0];
     float[] Res = new float[ to-from +1];
     for( int i = from; i <= to; i++)
        Res[ i - from] = getDataValue( row_number, i);

     return Res;
    }



  /**
   * Set values for a portion or all of a row.
   * The "from" and "to" values must be direct array reference, i.e.
   * because the array positions start at zero, not one, this must be
   * accounted for. If the array passed in exceeds the bounds of the array, 
   * set values for array elements and ignore extra values.
   */
  public void setRowValues( float[] values, int row_number, int start )
    {
     return;
    }


   
  /**
   * Get values for a portion or all of a column.
   * The "from" and "to" values must be direct array reference, i.e.
   * because the array positions start at zero, not one, this must be
   * accounted for. If the array passed in exceeds the bounds of the array, 
   * get values for array elements and ignore extra values.
   */
  public float[] getColumnValues( int column_number, int from, int to )
    {
     if( column_number < 0)
        return new float[0];
     if( column_number >= getNumColumns()) 
        return new float[0];
     if( from <0) 
        from =0;
     if( to < 0) 
        to =0;
     if( from >= getNumRows()) 
        from = getNumRows()-1;
     if( to >= getNumRows()) 
        to = getNumRows()-1;
     if( from > to) 
         return new float[0];
     float[] Res = new float[ to-from +1];
     for( int i = from; i <= to; i++)
        Res[ i - from] = getDataValue(  i,column_number);
     return Res;
    }
  

 
  /**
   * Set values for a portion or all of a column.
   * The "from" and "to" values must be direct array reference, i.e.
   * because the array positions start at zero, not one, this must be
   * accounted for. If the array passed in exceeds the bounds of the array, 
   * set values for array elements and ignore extra values.
   */
  public void setColumnValues( float[] values, int column_number, int start )
    {
     return;
    }


  /**
   * Get value for a single array element.
   */
  public float getDataValue( int row_number, int column_number )
    { 
          
     if( row_number < 0) 
        return Float.NaN;
     if( column_number < 0) 
        return Float.NaN;
     if( row_number >= getNumRows())
        return Float.NaN;
     if( column_number >= getNumColumns()) 
        return Float.NaN;
     try{
        return (new Float( getValueAt(row_number, column_number ).toString()))
                  .floatValue();
     }catch( Exception ss){
        return Float.NaN;
     }
    }
   


  /**
   * Set value for a single array element.
   */
  public void setDataValue( int row_number, int column_number, float value )
    {
     return;
    }
      


  /**
   * Set all values in the array to a value. This method will usually
   * serve to "initialize" or zero out the array. 
   */
  public void setAllValues( float value )
    {
     return;
    }
  

  // Checks the bound of the number row to be between 0 and maxRows-1
  private int AdjustRowCol( int row, int maxRows)
    {
     if( row < 0) 
        return 0;
     if( row >= maxRows) 
        return maxRows - 1;
     return row;
    }



  /**
   * Returns the values in the specified region.
   * The vertical dimensions of the region are specified by starting 
   * at first row and ending at the last row. The horizontal dimensions 
   * are determined by the first column and last column. 
   */ 
  public float[][] getRegionValues( int first_row, int last_row,
                                    int first_column, int last_column )
    {
     first_row = AdjustRowCol( first_row, getNumRows());
     last_row = AdjustRowCol( last_row, getNumRows());
     first_column = AdjustRowCol( first_column, getNumColumns());
     last_column = AdjustRowCol( last_column, getNumColumns());
     if( first_row >last_row) first_row = last_row;
     if( first_column >last_column) first_column = last_column;
     
     float[][]Res = new float[last_row-first_row+1][last_column-first_column+1];
    
     for( int i = first_row; i<=last_row; i++)
        Res[i] = getRowValues( i, first_column,last_column);
     return Res;

    }



  /**  
   * Sets values for a specified rectangular region. This method takes 
   * in a 2D array that is already organized into rows and columns
   * corresponding to a portion of the virtual array that will be altered.
   */
  public void setRegionValues( float[][] values, 
                                int row_number,
				int column_number )
    {
     return;
    }


				
  /**
   * Returns number of rows in the array.
   */
  public int getNumRows()
    {
     return getRowCount();
    }



  /**
   * Returns number of columns in the array.
   */   
  public int getNumColumns()
    {
     return getColumnCount() ;
    }
      


  //---------------- End IVirtualArray2D Methods----------------


  //---------------- IArrayMaker Methods----------------

  /**
   * Return controls needed by the component.
   */ 
  public JComponent[] getSharedControls()
    {
     JComponent[] jcomps = getControls();
     addDataChangeListener( this );
     JComponent[] Res = new JComponent[ jcomps.length +4];
     JRowColPanel = new JPanel( new GridLayout(2,1));
    
     TextRangeUI  tr=  new  TextRangeUI ("Row Range",
                                          0.0f+state.get_int(ViewerState.TABLE_TS_ROWMIN),
                                          0.0f+state.get_int(ViewerState.TABLE_TS_ROWMAX));
                        
     
     tr.addActionListener( new MyRangeActionListener(1));
     Res[0] = tr;
     tr= new TextRangeUI ("Col Range", 0.0f+state.get_int(ViewerState.TABLE_TS_COLMIN),
                                       0.0f+state.get_int(ViewerState.TABLE_TS_COLMAX));
                        
     
     tr.addActionListener( new MyRangeActionListener(2));
     Res[1] = tr;
     
        
     if( jcomps != null) if(jcomps.length > 0)
        for( int i=0; i< jcomps.length; i++)
           Res[2+i]=( jcomps[i]);
     
   
     
     XScl= new XScaleChooserUI("XScale", DS.getX_units(),
                             state.get_float(ViewerState.TABLE_TS_TIMEMIN),
                             state.get_float(ViewerState.TABLE_TS_TIMEMAX),
                             state.get_int(ViewerState.TABLE_TS_NSTEPS));
     
     Res[2+jcomps.length] = XScl;
     x_scale = XScl.getXScale();
     if( x_scale == null)
       x_scale = DS.getData_entry(0).getX_scale();
     XScl.addActionListener( new MyXScaleActionListener());

    //Animation Controller
     acontrol = new AnimationController();
     
       xvals1 =(calcXvals());
   
     acontrol.setFrame_values( xvals1);
     acontrol.setBorderTitle("X vals");
     acontrol.setTextLabel(" X("+DS.getX_units()+")");
     TimeIndex = state.get_int( ViewerState.TABLE_TS_CHAN);
     if( TimeIndex < 0)
        TimeIndex = 0;
     else if( TimeIndex >= xvals1.length)
        TimeIndex = x_scale.getNum_x()-1;
     acontrol.setFrameValue( xvals1[TimeIndex]);//<========Array out of bounds 0
     acontrol.addActionListener( new MyAnimationListener());
    
     Res[3+jcomps.length] =( acontrol);
   
     return Res;
    }



  /**
   * To be continued...
   */   
  public JComponent[] getPrivateControls()
    {
     return new JComponent[0];
    }



  /**
   * Return view menu items needed by the component.
   */   
  public ViewMenuItem[] getSharedMenuItems( )
    {
     ViewMenuItem[] Res;
     Res = new ViewMenuItem[4];
     //if(  JMenuName.equals( "Options"))
       {
        
        jmErr = new JCheckBoxMenuItem("Show Errors");
        jmErr.addActionListener( new CheckBoxListener());
        jmErr.setSelected( state.get_boolean(ViewerState.TABLE_TS_ERR));
        jmInd = new JCheckBoxMenuItem("Show Indices");
        jmInd.addActionListener( new CheckBoxListener());
        jmInd.setSelected( state.get_boolean(ViewerState.TABLE_TS_IND));
        Res[0] = new ViewMenuItem(jmErr);
        Res[1] = new ViewMenuItem(jmInd);
        
       }
     //if( JMenuName.equals( "File"))
       {
        
        JMenuItem item = new JMenuItem( "Save DataSet to File");
        SaveDataSetActionListener DSActList =new SaveDataSetActionListener( DS);
        item.addActionListener(DSActList);
        Res[2] = new ViewMenuItem( item);
        JMenuItem sv= new JMenuItem( "Save Table to a File");
        sv.addActionListener( new MyActionListener());
        Res[3] =new ViewMenuItem(sv);
        return Res;
       }


     
    }
   

   public void kill(){
   }
  /**
   * To be continued...
   */
  public ViewMenuItem[] getPrivateMenuItems()
    {
     return new ViewMenuItem[0];
    }
   String[] paths ={"Options","Options","File","File"};
   public String[] getSharedMenuItemPath(){
     return paths;
   }
   public String[] getPrivateMenuItemPath( ){
       return null;
   }
  

  Vector Listeners = new Vector();
  /**
   * Add a listener to this view component. A listener will be notified
   * when a selected point or region changes on the view component.
   * The action command for these events are given in the public static 
   *  variables at the top.  All other events will be sent to the
   *  IVirtualArray
   */
  public void addActionListener( ActionListener act_listener )
    {
     Listeners.addElement( act_listener);
    }
   


  /**
   * Remove a specified listener from this view component.
   */ 
  public void removeActionListener( ActionListener act_listener )
    {
     Listeners.removeElement( act_listener);
    }



  /**
   * Remove all listeners from this view component.
   */ 
  public void removeAllActionListeners()
    {
     Listeners.removeAllElements();
    }


  // notifies all ActionListeners of an event
  private void notifyActionListeners( String command)
    {
     for( int i = 0; i < Listeners.size(); i++)
        ((ActionListener)Listeners.elementAt(i)).actionPerformed(
              new ActionEvent(this,ActionEvent.ACTION_PERFORMED,command));

    }


  public IVirtualArray getArray(){
      return (IVirtualArray2D)this;
  } 
   //-------------- IArrayMaker_DataSet Methods -----------------------------

  /**
  *    Get the DataSet Group corresponding to the given Selected Data
  *    @param  Info Should be a SelectedData2D Object
  */
  public int getGroupIndex( ISelectedData Info)
    {
     if( Info instanceof SelectedData2D)
       {
        SelectedData2D Info2D =(SelectedData2D)Info;
        int Gr = super.getGroup( Info2D.getRow(), Info2D.getCol());
        return Gr;

       }
     return -1;
    }


  
  /**
  *    Returns the time corresponding to the given Selected Data
  *    @param  Info Should be a SelectedData2D Object
  */
  public float getTime( ISelectedData Info)
    {
     if( Info instanceof SelectedData2D)
       {
        SelectedData2D Info2D =(SelectedData2D)Info;
        return getTime( Info2D.getRow(), Info2D.getCol());

       }
     return -1;

    }


  public void SelectRegion( ISelectedRegion region){
     if( region instanceof SelectedRegion2D){
        SelectedRegion2D Region = (SelectedRegion2D)region;
        if( Region.rows == null)
           return;
        if( Region.cols == null)
           return;
        for( int i = 0; i < Region.rows.length; i++)
          for( int j = 0; j< Region.cols.length; j++){
             int Group = getGroup(Region.rows[ i], Region.cols[j]);
             DS.setSelectFlag( Group, true);

         }

        DS.notifyIObservers( IObserver.SELECTION_CHANGED);
     }
  }
  /**
   *  This method sets the time, notifies listeners and advances the animation
   *  control
   * @param time  the new time
   * NOTE: This is currently called only when a pointed at event occurs
   */
  public void setTime( float time){
  	super.setTime(time);
  	acontrol.setFrameValue( time);
	notifyActionListeners( IArrayMaker.DATA_CHANGED);
  	
  }
  /**
   *  Invokes super.setTime only. For internal use. setTime notifies listeners
   * and moves the time animation control
   */
  public void SetTime( float time){
  	 super.setTime( time);
  	
  }
  
  /**
  *    Returns the selected data corresponding to the give PointedAt
  *    condition
  *    @param PointedAtGroupIndex   The index in the DataSet of the group
  *             that is being pointed at
  *    @param PointedAtTime The time in question when the pointing takes
  *          place
  *    @return  a SelectedData2D containing the row and column corresponding
  *              to the selected condition
  */
  public ISelectedData getSelectedData( int PointedAtGroupIndex,
                                         float PointedAtTime)
    {
     return new SelectedData2D( getRow(PointedAtGroupIndex,PointedAtTime),
                                getCol(PointedAtGroupIndex,PointedAtTime),
                                PointedAtTime);
    }


  /**
  *    Called when this is an ActionListener.  Currently it is invoked only
  *    when data is changed by the superclass
  */
  public void actionPerformed( ActionEvent evt)
    {
     if( evt.getActionCommand().equals("DataChange"))
       {
        state.set_int( ViewerState.TABLE_TS_DETNUM, getDetNum());
        notifyActionListeners( IArrayMaker.DATA_CHANGED);
        notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);

       }
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
    return false; // remove if uncommenting code above!!!
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


  //---------------- IVirtualArray Methods----------------

  // Attempts to create a float[] of xvals satisfying as many constraints as
  // possible
  private float[] calcXvals()
    {
     int[] u = DS.getSelectedIndices();
     boolean useAll=false;
     if( u== null)
        useAll = true;
     else if( u.length < 1)
        useAll = true;
     int a = 0;
     if( !useAll )
        a = u[0];
     float[] new_xvals;
   
     Data  D = DS.getData_entry(a);

     if( x_scale != null)
        new_xvals = x_scale.getXs();
     else
       {
        x_scale  =  D.getX_scale();
        new_xvals = D.getX_values(); 
       }
    
     int n=1;
     if( D instanceof FunctionTable)
       n=0;
     else if( D instanceof FunctionModel)
       n=0;
     int first = Arrays.binarySearch( new_xvals, state.get_float(ViewerState.TABLE_TS_TIMEMIN));
     int end =Arrays.binarySearch( new_xvals, state.get_float(ViewerState.TABLE_TS_TIMEMAX));
     if( first < 0) 
        first = -first-1;
     if( end < 0) 
        end = -end -1;
     if( first >= end){
       state.set_float( ViewerState.TABLE_TS_TIMEMIN, x_scale.getStart_x());
       state.set_float(ViewerState.TABLE_TS_TIMEMAX, x_scale.getEnd_x());
       return x_scale.getXs();
     }
     if( end >= new_xvals.length) 
        end = new_xvals.length -1;
     float[] Hxvals = new float[ end - first +1 -n];
    
     for( int i = 0; i< Hxvals.length; i++)
          Hxvals[i] = (new_xvals[first+i]+new_xvals[first+i+n])/2.0f;
     return Hxvals;
    
    }
 


  // converts time in getPointedAtX to index in time slice array
  private int getPointedAtXindex()
    {
     float X = DS.getPointedAtX();
     if( Float.isNaN( X))
        return 0;
     if( xvals1== null)
        return 0;
     int index = java.util.Arrays.binarySearch( xvals1, X);
     if( index < 0)
         index =-index-1;
     if( index <=0)
        return 0;
     if( index >= xvals1.length -1) 
        return xvals1.length -1;
     if( (xvals1[index] -X) <= (X-xvals1[index-1] ))
        return index;
     else
        return index -1;
       
    }



  //-----------------Control Listeners --------------------------

  /** 
  *  Listener for events on the show error and show indices Option menu item buttons
  */
    //Moved to IVirtual Array
  private class CheckBoxListener  implements ActionListener
    {
     public void actionPerformed( ActionEvent evt)
       {
        setErrInd(jmErr.getState(),jmInd.getState());
        //set state here 
        state.set_boolean(ViewerState.TABLE_TS_ERR,jmErr.getState());  
        state.set_boolean(ViewerState.TABLE_TS_IND ,jmInd.getState()); 
        notifyActionListeners( IArrayMaker.DATA_CHANGED);
        notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);

       }
    }//CheckBoxListener


  private class MyActionListener implements ActionListener
    {
     String filename = null;

     /** 
     *  Displays a JFileChooser box to save the table and then writes header information
     *  and the data to the file
     */  
     public void actionPerformed( ActionEvent evt )
       {
        JFileChooser jf ;
        if( filename == null )  
           jf = new JFileChooser();
        else 
           jf = new JFileChooser( filename );
        FileOutputStream fout = null;
        if( !( jf.showSaveDialog( null ) == JFileChooser.CANCEL_OPTION ) )
           try
             {
              filename = jf.getSelectedFile().toString();
              File ff = new File( filename );
              fout = new FileOutputStream( ff );       
               
              StringBuffer S =new StringBuffer( 8192); 
              // Header Stuff
              S.append( "#Data Set");
              S .append( DS.toString());
              S.append("\n");
              S.append("#Selected Groups\n");

      
              String SS = "NO SELECTED INDICES";
              int[] SelInd = DS.getSelectedIndices() ;
              if( SelInd != null ) if( SelInd.length > 0 )
                 SS = StringUtil.toString( SelInd );

              S.append( "#     ");
              S.append(DS.toString());
              S.append(":");
              S.append( SS );

              S.append("\n");
              S.append( "#Operations\n" );
              S.append( "#     ");
              S.append(DS.toString() );
              S.append(":");
              OperationLog oplog = DS.getOp_log();

              if( oplog != null )
                 for( int j = 0; j < oplog.numEntries(); j++ )
                   {
                    S.append(oplog.getEntryAt( j ));
                    if( j + 1 < oplog.numEntries() )
                       S.append( "\n");  
                   }
              S.append("\n");
        
              fout.write(S.toString().getBytes());
              S.setLength(0);
              SaveFileInfo( fout);
               
             }
           catch( Exception ss )
             {
              DataSetTools.util.SharedData.addmsg( "Cannot Save " + 
                         ss.getClass()+":"+ss );
             }
       }
    }//MyActionListener( redo)
 
  public void SaveFileInfo( FileOutputStream fout)
   {
     StringBuffer S = new StringBuffer( 3000);
     float saveTime = Time;
     //int saveChan = TimeIndex;
     
     float MaxTime = state.get_float(ViewerState.TABLE_TS_TIMEMAX);
     float MinTime = state.get_float(ViewerState.TABLE_TS_TIMEMIN);
     int MinRow = state.get_int(ViewerState.TABLE_TS_ROWMIN);
     int MaxRow = state.get_int(ViewerState.TABLE_TS_ROWMAX);  
     int MinCol = state.get_int(ViewerState.TABLE_TS_COLMIN);
     int MaxCol = state.get_int(ViewerState.TABLE_TS_COLMAX); 
     int timeIndStart = Arrays.binarySearch( x_scale.getXs(), MinTime);
     int timeIndEnd = Arrays.binarySearch( x_scale.getXs(), MaxTime);
     if( timeIndStart < 0) timeIndStart = -timeIndStart -2;
     if( timeIndEnd < 0) timeIndEnd = -timeIndEnd -2;
     if( timeIndStart < 0) timeIndStart = 0;
     if( timeIndEnd < 0) timeIndEnd = 0;
     
    
     try{
     for( int i = timeIndStart; i <= timeIndEnd; i++ ){
        S.append("\n\nTime="+xvals1[i]+"\n\t");
        
        SetTime(xvals1[i]);
        for( int j= MinCol; j<=MaxCol; j++)
          S.append("Col "+j+"\t");
        S.append("\n");
        for( int row = MinRow; row <=MaxRow; row++){
          S.append("Row "+row+"\t");
          for( int j= MinCol; j<=MaxCol; j++){
              S.append(getValueAt( row-MinRow,j-MinCol)+"\t");
          }
          S.append("\n");
          if( S.length()>2400){
             fout.write( S.toString().getBytes());
             S.setLength(0); 
          }
        
        }
     }
     
     if( S.length()>0)
       fout.write( S.toString().getBytes());
     fout.close();
    }
    catch( Exception ss)
      { SharedData.addmsg("IO error="+ss.toString());}
    SetTime(saveTime);
   }
  

  /** 
   * Puts in initial values for the state variables if they have not been
   * initialized.  Also sets up any information that must be initialized using
   * these state values.
   */
  public void initState()
    {
     if( state == null)
        state = new ViewerState();

     if(( state.get_String( ViewerState.TABLE_TS).equals("")))
       {
        //String S =state.get_String( ViewerState.TABLE_TS);
    
        state.set_String( ViewerState.TABLE_TS, "OK");
     
        //DataSet ds = DS;
        state.set_int( ViewerState.TABLE_TS_ROWMIN , 1);
        state.set_int( ViewerState.TABLE_TS_COLMIN , 1);
        state.set_int( ViewerState.TABLE_TS_ROWMAX , getRowCount());
        state.set_int( ViewerState.TABLE_TS_COLMAX , getColumnCount()); 
        state.set_int( ViewerState.TABLE_TS_CHAN , getPointedAtXindex( ));
        XScale xscl1 = DS.getData_entry(0).getX_scale();
        state.set_float(ViewerState.TABLE_TS_TIMEMIN, xscl1.getStart_x());
        state.set_float(ViewerState.TABLE_TS_TIMEMAX ,xscl1.getEnd_x());
        state.set_int(ViewerState.TABLE_TS_NSTEPS , 0);
        state.set_boolean(ViewerState.TABLE_TS_ERR,false);  
        state.set_boolean(ViewerState.TABLE_TS_IND ,false); 
     
       }
     x_scale = null;
     if( xvals1 == null)
       {
        xvals1 = ( calcXvals());
       }
     //x_scale = new VariableXScale( xvals1);
     //setXScale( x_scale);
     
     setErrInd(state.get_boolean( ViewerState.TABLE_TS_ERR),
                state.get_boolean( ViewerState.TABLE_TS_IND));
     setRowRange(state.get_int(ViewerState.TABLE_TS_ROWMIN),
                 state.get_int(ViewerState.TABLE_TS_ROWMAX));
     setColRange(state.get_int(ViewerState.TABLE_TS_COLMIN),
                 state.get_int(ViewerState.TABLE_TS_COLMAX));
     setTimeRange(state.get_float(ViewerState.TABLE_TS_TIMEMIN),
                 state.get_float(ViewerState.TABLE_TS_TIMEMAX));
     
     
     TimeIndex = state.get_int( ViewerState.TABLE_TS_CHAN);
     if( TimeIndex < 0)
        TimeIndex = 0;
     else if( TimeIndex >= x_scale.getNum_x())
        TimeIndex = x_scale.getNum_x()-1;
     SetTime( x_scale.getX( TimeIndex));
     
     int DetNum = state.get_int( ViewerState.TABLE_TS_DETNUM);
     if( DetNum >= 0)
       setDetNum( DetNum);
    }


  // Listens for the change in the range of rows and/or columns
  //  to be viewed
  class MyRangeActionListener implements ActionListener
    {
     int ID;
     
     public MyRangeActionListener( int id)
       {
        ID=id;
       }
     public void actionPerformed( ActionEvent evt)
       {
        TextRangeUI tri = (TextRangeUI)(evt.getSource());
        int min,max;
        min = (int)( tri.getMin());
        max = (int)( tri.getMax());
        
        //int n2 = getColumnCount();
        if(ID==1)
          {
           setRowRange( min,max);
           state.set_int( ViewerState.TABLE_TS_ROWMIN, min);
           state.set_int(ViewerState.TABLE_TS_ROWMAX, max);
          }
        else
          {
           setColRange( min,max);
           state.set_int(ViewerState.TABLE_TS_COLMIN, min);
           state.set_int(ViewerState.TABLE_TS_COLMAX, max);
          }
         
        notifyActionListeners( IArrayMaker.DATA_CHANGED);
       }
    }


  // Listens for changes in the XScaleChooserUI and sets state variables
  // etc in response to these changes.
  class MyXScaleActionListener implements ActionListener
    {

     public void actionPerformed( ActionEvent evt)
       { 
        x_scale= XScl.getXScale();
       
        if(x_scale == null)
           x_scale = DS.getData_entry(0).getX_scale();
        
        setXScale(x_scale );
       
       
        //float[] xx = x_scale.getXs();
        xvals1 = calcXvals();
        acontrol.setFrame_values(xvals1);
        float X = DS.getPointedAtX();
        if( Float.isNaN(X))
           acontrol.setFrameNumber( 0);
        else
           acontrol.setFrameValue(  X );
        SetTime( acontrol.getFrameValue());
        state.set_int( ViewerState.TABLE_TS_CHAN , getPointedAtXindex( ));
        state.set_float(ViewerState.TABLE_TS_TIMEMIN, xvals1[0]);
        state.set_float(ViewerState.TABLE_TS_TIMEMAX ,xvals1[ xvals1.length - 1 ]);
     
        state.set_int(ViewerState.TABLE_TS_NSTEPS , x_scale.getNum_x());
   
        notifyActionListeners( IArrayMaker.DATA_CHANGED);
       }
    }


 
  // Listens for changes in the animation control( changing time slice)
  // Updates state and other variables then notifies everyone that the
  // data has changed
  class MyAnimationListener implements ActionListener
    {
     public void actionPerformed( ActionEvent evt)
       { 
        if( state == null)
           return;
        if( acontrol == null)
           return;
        //only notify if different
        int FrameNumber = state.get_int(ViewerState.TABLE_TS_CHAN);
        
        if( FrameNumber == acontrol.getFrameNumber())
           return;
           
        state.set_int( ViewerState.TABLE_TS_CHAN, acontrol.getFrameNumber());
         
        SetTime( xvals1[acontrol.getFrameNumber()]);
        
        notifyActionListeners( IArrayMaker.DATA_CHANGED);
        if( acontrol.getFrameNumber() != FrameNumber) 
          {
           DS.setPointedAtX(xvals1[acontrol.getFrameNumber()]);
           //notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);
           DS.notifyIObservers(gov.anl.ipns.Util.Messaging.IObserver.POINTED_AT_CHANGED);
          }
       }
    }

  
}//RowColTimeVirtualArray
    


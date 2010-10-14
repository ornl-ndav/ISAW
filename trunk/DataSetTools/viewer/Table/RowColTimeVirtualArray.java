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
 * $Log: RowColTimeVirtualArray.java,v $
 * Revision 1.22  2008/02/15 20:59:20  rmikk
 * Eliminated problem when VariableXScales and Uniformare given only one value.
 *
 * Revision 1.21  2007/06/14 22:06:23  rmikk
 * Eliminated duplicate entries in the action listeners
 * Do not notify that data has changed if the new time is not in a different bin
 *  than the previous time
 *
 * Revision 1.20  2007/06/05 20:22:21  rmikk
 * Filled out the axisInfo method so this could be used with the new View
 *    components
 * Added a ReverseY directions.  Must be set to use the new view components
 * Added the slice integration routines here
 *
 * Revision 1.19  2005/11/11 21:11:15  rmikk
 * Fixed the array out of bounds error
 *
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
import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.Numeric.floatPoint2D;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.Menu.*;
import gov.anl.ipns.ViewTools.UI.*;
import gov.anl.ipns.ViewTools.Components.Region.*;
import gov.anl.ipns.ViewTools.Panels.Transforms.*;

import java.util.*;
import java.awt.event.*;

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.instruments.SampleOrientation;
import DataSetTools.math.tof_calc;

import java.io.*;

import DataSetTools.trial.*;
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
                       implements IArrayMaker_DataSet, IVirtualArray2D,doesColumns,
                       IhasMarkers{
  /**
    * 
    */
   private static final long serialVersionUID = 1L;
//DataSet DS;
  String Title;
  JCheckBoxMenuItem jmErr=null; 
  JCheckBoxMenuItem jmInd=null;
  public AnimationController acontrol= null ;
  XScaleChooserUI XScl= null;
  public float[] xvals1;
  public int TimeIndex = -1;
  public JPanel JRowColPanel= null;
  //public XScale x_scale= null;
  public ViewerState state;
  public boolean ReverseY;

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
     if( axis == AxisInfo.X_AXIS){
        return new AxisInfo( state.get_int(ViewerState.TABLE_TS_COLMIN )-.5f , 
                 state.get_int( ViewerState.TABLE_TS_COLMAX )+.5f ,
                          "Column", "",AxisInfo.LINEAR);
     }else if( axis == AxisInfo.Y_AXIS){
        //if( !ReverseY )
         return new AxisInfo( state.get_int(ViewerState.TABLE_TS_ROWMIN )-.5f , 
                    state.get_int( ViewerState.TABLE_TS_ROWMAX )+.5f,
                             "Row", "",AxisInfo.LINEAR);
        

       // return new AxisInfo( state.get_int(ViewerState.TABLE_TS_ROWMAX )+.5f , 
       //          state.get_int( ViewerState.TABLE_TS_ROWMIN )-.5f,
       //                   "Row", "",AxisInfo.LINEAR);
     }else if( axis == AxisInfo.Z_AXIS){
        return new AxisInfo( 0f, DS.getYRange().getEnd_x(),"Intensities", "Counts",
                   AxisInfo.LINEAR);
     }
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
     
     if( ReverseY ){
        int r = tMaxrow - row_number; 
        row_number = r - tMinrow;
     }
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
     
        
     if(jcomps.length > 0)
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
    
        
        jmErr = new JCheckBoxMenuItem("Show Errors");
        jmErr.addActionListener( new CheckBoxListener());
        jmErr.setSelected( state.get_boolean(ViewerState.TABLE_TS_ERR));
        jmInd = new JCheckBoxMenuItem("Show Indices");
        jmInd.addActionListener( new CheckBoxListener());
        jmInd.setSelected( state.get_boolean(ViewerState.TABLE_TS_IND));
        Res[0] = new ViewMenuItem(ViewMenuItem.PUT_IN_EDIT, jmErr );
        Res[1] = new ViewMenuItem( ViewMenuItem.PUT_IN_EDIT, jmInd);
        
     
        
        JMenuItem item = new JMenuItem( "Save DataSet to File");
        SaveDataSetActionListener DSActList =new SaveDataSetActionListener( DS);
        item.addActionListener(DSActList);
        Res[2] = new ViewMenuItem(ViewMenuItem.PUT_IN_FILE, item);
        JMenuItem sv= new JMenuItem( "Save Table to a File");
        sv.addActionListener( new MyActionListener());
        Res[3] =new ViewMenuItem(ViewMenuItem.PUT_IN_FILE, sv);
        return Res;
      
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
     if(! Listeners.contains(act_listener))
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

   /**
    * Gives the total number of rows in the whole detector
    * @return the total number of rows in the whole detector
    */
  public int getTotalNumRows(){
     return num_rows;
  }
  
  public IVirtualArray getArray(){
      return this;
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
        int row = Info2D.getRow()-tMinrow;
        //if( ReverseY){
        //   row= tMaxrow -Info2D.getRow();
        //}
        int col = Info2D.getCol()-tMincol;
       
        
        int Gr = super.getGroup( row,col);
       
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
     if( region instanceof SelectedRegion2D)
     {
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
   *  This methods shows the result of integrating the last selected Box 
   *  region.  The result displayed gives the sum of the cells - background
   *  and the error assuming a Poisson disribution in the cells. The 
   *  background is the shell just outside of the Box region in question
   *   
   * @param viewArray   The 2D virtual array with the intensities to be used 
   * 
   * @param SelectedRegions An array of selected regions.  Only the last
   *                 Box region that was selected will be integrated
   *                 
   * @param Region2Array  Converts the coordinates of Region to array
   *                coordinates.
   */
  public static void ShowIntegrateStats( IVirtualArray2D viewArray, 
                                         Region[]        SelectedRegions,
                                         CoordTransform   Region2Array){
     if( viewArray == null){
        JOptionPane.showMessageDialog( null, "No array of values to integrate");
        return;
     }
     
     if( SelectedRegions == null || SelectedRegions.length < 1 ){
        JOptionPane.showMessageDialog( null, "No regions to integrate");
        return;
     }
     
     if( Region2Array == null){
        JOptionPane.showMessageDialog( null, "Cannot find region in the array to integrate");
        return;
     }
     
     
     Region Box = null;
     for( int i= SelectedRegions.length -1; i >= 0  && Box == null ; i--){
        if( (SelectedRegions[i] instanceof BoxRegion) || 
                 (SelectedRegions[i] instanceof TableRegion))
           Box = SelectedRegions[i];
     }
     
     
     if( Box == null){
        JOptionPane.showMessageDialog( null, "No Box region selected");
        return;
     }
     float[] Results = new float[2];
     CoordBounds BoxLimits = Box.getRegionBounds( Region2Array );
     if( Box instanceof TableRegion){
        BoxLimits.setBounds( BoxLimits.getX1(), BoxLimits.getY1(),
                 BoxLimits.getX2(), BoxLimits.getY2() );
     }
     //NOTE Top Bound NOT included in BoxLimits
     Results = GetIntegrateStats( viewArray,FirstInt(BoxLimits.getX1(),true,true), 
                                 FirstInt(BoxLimits.getX2(),false,false),
                                 FirstInt(BoxLimits.getY1(),true, true),
                                 FirstInt(BoxLimits.getY2(),false, false));
     String S="Intensity=" + Results[0] +"\n";
     
     S+= "(Poisson)Error = " + Results[1];
     S+="\n Intensity/error="+( Results[0] / Results[1] );
     JOptionPane.showMessageDialog( null, S);
  }
  
  private static int FirstInt( float F, boolean moreThan, boolean orEqual)
  {
     int Res = (int)Math.floor( F );
     if( Res == F && orEqual)
        return Res;
     if( Res <= F && moreThan)
        return Res+1;
     if( Res >= F && !moreThan)
        return Res -1;
     
     return Res;
     
     
  }
  /**
   * Calculates the sum of the intensities - background for the array elements
   * from row1,col1 to row2,col2.  The background is calculated from the
   * shell just outside of the given box.
   * 
   * @param viewArray  The array of intensities
   * @param col1       The first column n the region of interest
   * @param col2       The last column(incl) n the region of interest
   * @param row1       The first row n the region of interest
   * @param row2       The last row(incl) in the region of interest
   * @return      An array with 2 elements.  The first is the intensities(-
   *              background) and the second is the error(assuming Poisson)
   */
  public static float[] GetIntegrateStats( IVirtualArray2D viewArray, int col1,
               int col2, int row1, int row2){
     int Col1 = Math.min( col1, col2);
     int Col2 = Math.max( col1, col2);
     int Row1= Math.min( row1 , row2);
     int Row2= Math.max( row1 , row2);
     float SumSel = 0;
     float SumBorder =0;
     int nSel =0;
     int nBorder = 0;
     
     // Find sum of selected region
     for( int row = Row1; row <= Row2; row++)
        for( int col = Col1; col <= Col2; col++ ){
           if( row >=0  && row < viewArray.getNumRows())
              if( col >=0  && col < viewArray.getNumColumns()){
                 SumSel += viewArray.getDataValue( row, col);
                 nSel++;
              }
                 
        }
     
     //Find sum of top and bottom borders
     for( int col= Col1-1; col <= Col2+1; col++){
        if( Row1-1 >=0)
           if( col >=0 && col < viewArray.getNumColumns()){
           SumBorder += viewArray.getDataValue( Row1-1, col);
           nBorder ++;
        }
        if( Row2 + 1 < viewArray.getNumRows()) 
           if( col >=0 && col < viewArray.getNumColumns()){
              SumBorder += viewArray.getDataValue( Row2 + 1, col);
              nBorder ++;
        }
     }
     
     //Now find the sum on left and right
     for( int row = Row1 ; row <= Row2 ; row++ ) {
         if( Col1 - 1 >= 0 ) {
            SumBorder += viewArray.getDataValue( row , Col1 - 1 );
            nBorder++ ;
         }

         if( Col2 + 1 < viewArray.getNumColumns() ) {
            SumBorder += viewArray.getDataValue( row , Col2 + 1 );
            nBorder++ ;
         }
      }
     float[] Result = new float[2];
     Result[0] =(SumSel - (nSel * SumBorder/nBorder));
     
     double p_over_b = (0.0 + nSel)/nBorder;
     Result[1] =(float)java.lang.Math.sqrt( SumSel + p_over_b*p_over_b*SumBorder);
    
     return Result;
  }
  
  
  /**
   *  This method sets the time, notifies listeners and advances the animation
   *  control
   * @param time  the new time
   * NOTE: This is currently called only when a pointed at event occurs
   */
  public void setTime( float time){
   float Time = super.getTime( 1,1) ;
   if( Float.isNaN( time ) ||  Float.isNaN( Time ))
      return;
   if( x_scale == null)
      return;
   if( x_scale.getI( time ) == x_scale.getI( Time ))
      return;
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


  //------------------------implements IhasMarkers------------
  
  public floatPoint2D[] getMarkers1()
{
   int indx = x_scale.getI_GLB(  Time );
   
   if( indx <0 || indx >= x_scale.getEnd_x())
      return null;
   
   float minTime = x_scale.getX( indx );
   float maxTime = x_scale.getX( indx+1 );
   
   IDataGrid grid = Grid_util.getAreaGrid( DS , DetNum );
   if( grid == null)
      return null;
   if( grid instanceof RowColGrid)
   {
      grid = RowColGrid.GetDataGrid( (RowColGrid)grid , .01f );
   }
   SampleOrientation sampOrient = AttrUtil.getSampleOrientation( DS );
   
   float[][] OrientMat = null;
   try
   {
      OrientMat =  AttrUtil.getOrientMatrix( DS );
   }catch(Exception s){
      
   
      return null;
   }
   
   if( sampOrient == null || OrientMat == null)
     return null;
     

   float initialPath = AttrUtil.getInitialPath( DS );
   float[][] invOrient = LinearAlgebra.getInverse( OrientMat);
   invOrient =Mult(  invOrient , 
                sampOrient.getGoniometerRotationInverse().get() );
   
   if( invOrient == null)
      return null; 
    
   float[][][] hklMinMax = getHKLMinMax( grid, minTime,maxTime,initialPath,invOrient);
   int[][] hklList = calcMarkers( hklMinMax[0], hklMinMax[1]);
   Vector<int[]> data = new Vector<int[]>();
   if( hklList != null)
      for( int i=0; i < hklList.length; i++)
      {
         float[] hklListF= { (float)hklList[i][0],(float)hklList[i][1],(float)hklList[i][2]};
         float[] Qs =  LinearAlgebra.mult( OrientMat , hklListF );
         
         
         //float[] rcT = Xlate.QtoRowColTOF( new Vector3D(Qs));//,sampOrient, grid, initialPath );
         float[] rcT =Q2RCT( new Vector3D(Qs), grid,  initialPath,
                                          sampOrient);
         
         
         if( rcT != null && rcT[2]<=maxTime && rcT[2]>=minTime )
         {
            int[] D = new int[2];
            D[0] =grid.num_cols()+1-(int)(rcT[0]+.5);
            D[1] =(int)(rcT[1]+.5);
            data.add( D );               
         }
      }
   if( data.size()<1)
      return null;
   
   floatPoint2D[] pts = new floatPoint2D[data.size()]; 
   for( int i=0;i< data.size(); i++)
   {
      int[] point = data.elementAt( i );
      pts[i]= new floatPoint2D( point[1],point[0]);
   }
   
   return pts;
     
}
  
 //invOrient includes sample orientation
private  float[][][]  getHKLMinMax( IDataGrid grid, float minTime,float maxTime,float initialPath,
         float[][]invOrient)
{
   float minDist,maxDist;
   
   Vector3D Pt = new Vector3D( grid.z_vec( ));
   Pt.multiply( grid.position( ).dot( grid.z_vec( ) ) );
   minDist = Float.POSITIVE_INFINITY;
   maxDist = Float.NEGATIVE_INFINITY;
   int row =0; int col =0;
   for( int i=0; i < 4;i++)
   {
     float d = Pt.distance( grid.position(row*grid.num_rows( )+1,col*grid.num_cols( )+1) );
     col++;
     if( col >1)
     {
        row++;
        col=0;
     }
     if( d < minDist)
        minDist = d;
     if( d > maxDist)
        maxDist =d;
   }
   row=0; col=0;
   for( int i=0; i<4;i++)
   {
      Vector3D P1 = grid.position(row*grid.num_rows( )+1,col*grid.num_cols( )+1) ;
      col++;
      if( col >1)
      {
         row++;
         col=0;
      }
      Vector3D P2=grid.position(row*grid.num_rows( )+1,col*grid.num_cols( )+1) ;
      Vector3D diff = new Vector3D(P2);
      diff.subtract(P1);
      float k = (Pt.dot( diff )-P1.dot( diff ))/(diff.dot( diff ));
      if( k >=0 || k <=1)
      {
         Vector3D Pt2 = new Vector3D(P1);
         diff.multiply( k );
         Pt2.add( diff );
         float d= Pt.distance(Pt2);
         if( d < minDist)
            minDist = d;
      }
   }
   
   float[][][] Res = new float[2][4][3];//2 min-max, 4 sides, 3 hval kval lval
   row=0;col=0;
   for( int i=0; i< 4; i++)
   {
      Vector3D pt = grid.position(row*grid.num_rows( )+1,col*grid.num_cols( )+1);
      Vector3D Q = tof_calc.DiffractometerVecQ( pt , initialPath , minTime );
      Q.multiply( .5f/(float)Math.PI*maxDist/pt.length() );
      float[] Q3 = new float[3];
      System.arraycopy( Q.get(),0,Q3,0,3);
      Res[0][i]=LinearAlgebra.mult( invOrient ,Q3);
      Q.multiply(minTime/maxTime*minDist/maxDist);
      Q3 = new float[3];
      System.arraycopy( Q.get(),0,Q3,0,3);
      Res[1][i]=LinearAlgebra.mult( invOrient ,Q3 );
      col++;
      if( col >1)
      {
         row++;
         col=0;
      }
      
   }
   return Res;
}
 


  /**
   * Will produce marks where Peaks should occur. 
   * (non-Javadoc)
   * @see DataSetTools.viewer.IhasMarkers#getMarkers()
   */
  @Override
  public floatPoint2D[] getMarkers()
  {
    int indx = x_scale.getI_GLB(  Time );
    
    if( indx <0 || indx >= x_scale.getEnd_x())
       return null;
    
    float minTime = x_scale.getX( indx );
    float maxTime = x_scale.getX( indx+1 );
    
    IDataGrid grid = Grid_util.getAreaGrid( DS , DetNum );
    if( grid == null)
       return null;
    if( grid instanceof RowColGrid)
    {
       grid = RowColGrid.GetDataGrid( (RowColGrid)grid , .01f );
    }
    SampleOrientation sampOrient = AttrUtil.getSampleOrientation( DS );
    
    float[][] OrientMat = null;
    try
    {
       OrientMat =  AttrUtil.getOrientMatrix( DS );
    }catch(Exception s){
       
    }
    
    if( sampOrient == null || OrientMat == null)
      return null;
    VecQToTOF Xlate = new VecQToTOF( DS, grid );
    float initialPath = AttrUtil.getInitialPath( DS );
    float[][] invOrient = LinearAlgebra.getInverse( OrientMat);
    invOrient =Mult(  invOrient , 
                 sampOrient.getGoniometerRotationInverse().get() );
    
    if( invOrient == null)
       return null; 
      float[][] Phkl= new float[8][3];
      Phkl[0] = LinearAlgebra.mult( invOrient , tof_calc
               .DiffractometerVecQ( new DetectorPosition(grid.position( 1 , 1 )) , initialPath ,
                        minTime ).getCartesianCoords() );
      Phkl[1]  = LinearAlgebra.mult( invOrient , tof_calc
               .DiffractometerVecQ( new DetectorPosition(grid.position( 1 , 1 )) , initialPath ,
                        maxTime ).getCartesianCoords() );
      Phkl[2]  = LinearAlgebra.mult( invOrient , tof_calc
               .DiffractometerVecQ( new DetectorPosition(grid.position( 1 , grid.num_cols() ) ),
                        initialPath , minTime ).getCartesianCoords() );
      Phkl[3]  = LinearAlgebra.mult( invOrient , tof_calc
               .DiffractometerVecQ( new DetectorPosition(grid.position( 1 , grid.num_cols() ) ),
                        initialPath , maxTime ).getCartesianCoords() );
      Phkl[4]  = LinearAlgebra.mult( invOrient , tof_calc
               .DiffractometerVecQ( new DetectorPosition(grid.position( grid.num_rows() , 1 )) , initialPath ,
                        minTime ).getCartesianCoords() );
      Phkl[5]  = LinearAlgebra.mult( invOrient , tof_calc
               .DiffractometerVecQ( new DetectorPosition(grid.position( grid.num_rows() , 1 )) , initialPath ,
                        maxTime ).getCartesianCoords() );
      Phkl[6]  = LinearAlgebra.mult( invOrient , tof_calc
               .DiffractometerVecQ( new DetectorPosition(grid.position( grid.num_rows() , grid.num_cols() )) ,
                        initialPath , minTime ).getCartesianCoords() );
      Phkl[7]  = LinearAlgebra.mult( invOrient , tof_calc
               .DiffractometerVecQ( new DetectorPosition(grid.position( grid.num_rows() , grid.num_cols() )) ,
                        initialPath , maxTime ).getCartesianCoords() );
     float[] Minhkl =new float[3];
     float[] Maxhkl = new float[3];
     for( int j=0; j<3;j++)
     {
        Minhkl[j] = Maxhkl[j]=Phkl[0][j];
     }
     for( int i=1;i<8;i++)
        for(int j=0; j< 3; j++)
        {
           if(Phkl[i][j] < Minhkl[j])
              Minhkl[j]= Phkl[i][j];
           if(Phkl[i][j] > Maxhkl[j])
              Maxhkl[j]= Phkl[i][j];
          
        }
     Vector<int[]> data = new Vector<int[]>();
     float[] hkl= new float[3];
     float twoPI = (float)(2*Math.PI);
     int minh=  (int)Math.floor(Minhkl[0]/twoPI);
     int maxh = (int)(Math.floor( Maxhkl[0]/twoPI)+1);
     int mink=  (int)(Math.floor(Minhkl[1]/twoPI));
     int maxk = (int)(Math.floor( Maxhkl[1]/twoPI)+1);
     int minl=  (int)(Math.floor(Minhkl[2]/twoPI));
     int maxl = (int)(Math.floor( Maxhkl[2]/twoPI)+1);
     
     for( int h= minh; h <=maxh; h++)
        for( int k= mink; k <=maxk; k++)
           for( int l= minl; l <=maxl; l++)
           {
              hkl[0]=(float)(h*(Math.PI*2));hkl[1]=(float)(k*(Math.PI*2));
              hkl[2]=(float)(l*(Math.PI*2));
              float[] Qs =  LinearAlgebra.mult( OrientMat , hkl );
              
              
              //float[] rcT = Xlate.QtoRowColTOF( new Vector3D(Qs));//,sampOrient, grid, initialPath );
              float[] rcT =Q2RCT( new Vector3D(Qs), grid,  initialPath,
                                               sampOrient);
              
              
              if( rcT != null && rcT[2]<=maxTime && rcT[2]>=minTime )
              {
                 int[] D = new int[2];
                 D[0] =grid.num_cols()+1-(int)(rcT[0]+.5);
                 D[1] =(int)(rcT[1]+.5);
                 data.add( D );               
              }
           }
    
     if( data.size()<1)
        return null;
     
     floatPoint2D[] pts = new floatPoint2D[data.size()]; 
     for( int i=0;i< data.size(); i++)
     {
        int[] point = data.elementAt( i );
        pts[i]= new floatPoint2D( point[1],point[0]);
     }
     
    // floatPoint2D[] pts1 = getMarkers1();
   /*  float[][] bott= new float[4][];
     float[][] top= new float[4][];
     Phkl = LinearAlgebra.mult( Phkl , 1f/2/(float)Math.PI );  
     for( int i=0; i< 4;i++)
     {
        bott[i]=Phkl[2*i];
        top[i] = Phkl[2*i+1];
     }
   */  
    // System.out.println("hkl's="+gov.anl.ipns.Util.Sys.StringUtil.toString( calcMarkers(bott, top) ));
     
     return pts;
    
  }
  //Q with 2pi, i.e.  dQ for Q= mv
  private float[] Q2RCT( Vector3D Qs, IDataGrid grid, float initialPath,
                                     SampleOrientation sampOrient)
  {
      Vector3D Q = new Vector3D();
      sampOrient.getGoniometerRotation( ).apply_to( Qs , Q );
     if( Q.getX() > 0)
        return null;
     float MQ = Q.length( );
     float Ang0= (float)(Math.PI-Math.abs( Math.acos( Q.getX()/MQ ))); 
     float Ang = (float)(Math.PI - 2*Ang0);
                                       //Scat Ang in Q,beam vec plane
     float mv = (float) Math.sqrt(  MQ*MQ/2/(1-Math.cos(Ang)) );
     float[] Dir = new float[3];
     Dir[0]= Q.getX()+mv;
     Dir[1] = Q.getY( );
     Dir[2] = Q.getZ( );
     Vector3D DirV = new Vector3D( Dir );
     float D = grid.position().dot( grid.z_vec( ) );
     float k = D/DirV.dot( grid.z_vec() );
     DirV.multiply(k);
     Vector3D Pt = new Vector3D(DirV);
     DirV.subtract( grid.position() );
     float x = DirV.dot( grid.x_vec() );
     float y = DirV.dot(  grid.y_vec( ) );
     float row = grid.row(x,y);
     float col = grid.col(  x , y );
     if( row < .5 || col < .5 || Float.isNaN( row )|| Float.isNaN( col ))
        return null;
     if( col > grid.num_cols( )+.5 || row > grid.num_rows( )+.5)
        return null;
     
     float t = tof_calc.TOFofDiffractometerQ( Ang ,initialPath+Pt.length() ,MQ );
     
     //float t =(float)((initialPath+DirV.length())/ mv*tof_calc.MN_KG ); 
     float[] Res = new float[3];
     Res[0]= row;
     Res[1] = col;
     Res[2] = t;
     return Res;
     
     
     
  }

  /**
   * Calculates the cells( row and col) that contain integer hkl values( all 3)
   * If there is an integer hkl between two shells 
   * @param hkl1
   * @param hkl2
   * @param rc
   * @param tolerance  for corners to be integer 
   * @return
   */
  private int[][] calcMarkers( float[][] hkl1,float[][] hkl2)
  {
    
     try
     {

    float[][][] HKL = new float[2][4][4];
    HKL[0]= hkl1;
    HKL[1] = hkl2;
    float minh =Float.POSITIVE_INFINITY;
    float maxh = Float.NEGATIVE_INFINITY;
    for( int j=0;j<1;j++)
    for( int i=0; i<4;i++)
      
    {
       if( HKL[j][i][0] < minh)
       {
          minh =  HKL[j][i][0];
        
       }
       if( HKL[j][i][0] > maxh)
          maxh = HKL[j][i][0];
    }
    Vector<int[]> hkls = new Vector<int[]>();
    for(int h = (int)Math.floor(minh+.8); h<= maxh+.2; h++)
    {
      Vector<float[]> hklpt1 = new Vector<float[]>();
      for( int  s=0; s<4; s++)
      {
         float alpha = calcAlpha( h,hkl1[s][0], hkl1[(s+1)%4][0] );
         if( alpha >=0 && alpha <=1)
            hklpt1.addElement( Ptt(alpha,hkl1[s], hkl1[(s+1)%4]));
         if( alpha == 0 && hkl1[s][0]== hkl1[(s+1)%4][0])
            hklpt1.addElement( Ptt(1f,hkl1[s], hkl1[(s+1)%4]));
         alpha = calcAlpha( h,hkl2[s][0], hkl2[(s+1)%4][0] );
         if( alpha >=0 && alpha <=1)
            hklpt1.addElement( Ptt(alpha,hkl2[s], hkl2[(s+1)%4]));
         if( alpha == 0 && hkl2[s][0]== hkl2[(s+1)%4][0])
            hklpt1.addElement( Ptt(1f,hkl2[s], hkl2[(s+1)%4]));
         
         alpha = calcAlpha( h,hkl1[s][0],hkl2[s][0] );
         if( alpha >=0 && alpha <=1)
            hklpt1.addElement( Ptt(alpha,hkl1[s],hkl2[s] ));
         if( alpha == 0 && hkl1[s][0]==hkl2[s][0])
            hklpt1.addElement( Ptt(1f,hkl1[s],hkl2[s] ));        
         
      }
       
      float[][] Hklpt1 = EliminateRepeats( hklpt1);

      float maxk = Float.NEGATIVE_INFINITY;
      float mink = Float.POSITIVE_INFINITY;
      if( Hklpt1 != null)
      for( int s1 =0; s1< Hklpt1.length; s1++)
      {
         if( Hklpt1[s1][1] < mink)
            mink = Hklpt1[s1][1];
         if( Hklpt1[s1][1]>maxk)
            maxk = Hklpt1[s1][1];
      }
      hklpt1 = new Vector<float[]>();
      if( Hklpt1 != null && Hklpt1.length > 0 )
         for( int k=(int)Math.floor( .8+mink);
         k <=(int)Math.floor( .2+maxk); k++)
      {
        if( Hklpt1.length ==1)
        {

           if( Math.abs( Hklpt1[0][2] -Math.floor( Hklpt1[0][2]+.1))<.08  &&
                 Math.abs( Hklpt1[0][1] -Math.floor( Hklpt1[0][1]+.1))<.08)
              hkls.add(  new int[]{h,(int)Math.floor( Hklpt1[0][1]+.1),
                         (int)Math.floor( Hklpt1[0][2]+.1)} );
          
        }
        else 
        {
           for( int s1=0; s1 < Hklpt1.length -1; s1++)
            for( int s2 = s1+1; s2 < Hklpt1.length ; s2++)
          
           {
              float alpha1 = calcAlpha( k,Hklpt1[s1][1], Hklpt1[s2][1] );
              if( alpha1 >=0 && alpha1 <=1)
                  hklpt1.add(  Ptt( alpha1, Hklpt1[s1], Hklpt1[s2] ) );
              if( alpha1 ==0 &&Hklpt1[s1][1]== Hklpt1[s2][1] )
                 hklpt1.add(  Ptt( 1f, Hklpt1[s1], Hklpt1[s2] ) );
           }
         float[][]Hklpt2 = EliminateRepeats( hklpt1);
         if( Hklpt2 != null)
         if( Hklpt2.length ==1)
         {
            if( Math.abs( Hklpt2[0][2] -Math.floor( Hklpt2[0][2]+.1))<.08)
               hkls.add(  new int[]{h,k,(int)Math.floor( Hklpt2[0][2]+.1)} );
         }else
         for( int s3=0 ; s3 < Hklpt2.length -1; s3++)
            for( int s4 = s3+1; s4 < Hklpt2.length ; s4++)
               for( int l= (int)Math.floor( .8+ Math.min( Hklpt2[s3][2],Hklpt2[s4][2]) );
                     l <=.2+ Math.min( Hklpt2[s3][2] ,Hklpt2[s4][2]); l++ )
                    hkls.add(  new int[]{h,k,l} );
      }
      } 
    }
    
    return hkls.toArray( new int[0][3]);
   
     }catch(Throwable tt)
     {
        tt.printStackTrace( );
        return null;
     }
     
  }
  
  private float[][] EliminateRepeats( Vector<float[]> hklpts)
  {
     float[][] Res = hklpts.toArray( new float[0][0]);
    
     
    
   for( int i=0; i< Res.length ; i++)
        for( int j=i+1; j < Res.length; j++)
           if( Res[i] != null & Res[j] != null)
           {  boolean nullify= true;
              for( int indx =0; indx <3 && nullify; indx++)
              {
                 if( Math.floor( Res[i][indx]-.2 ) !=Math.floor( Res[j][indx]-.2 ))
                    nullify = false;
                 else if( Math.floor( Res[i][indx]+.2 ) !=Math.floor( Res[j][indx]+.2 ))
                    nullify = false;
              }
              if( nullify)
                 Res[j] = null;
           }
     int k=0;
     for( int i=0; i < Res.length; i++)
     {
        if( Res[i] == null)
           k++;
        else
          Res[i-k] =Res[i];
     }
    int N = Res.length -k;
    float[][] RRes = new float[N][3];
    System.arraycopy( Res , 0 , RRes , 0 , N );
    return RRes;
  
  }
  
  class floatArrayComparator implements Comparator<float[]>
  {

   @Override
   public int compare(float[] o1, float[] o2)
   {

      if(o1 == null)
         if( o2 ==null)
            return 0;
         else
            return -1;
      if( o2 == null)
         return 1;
      for( int i=0; i< Math.min( o1.length,o2.length  ); i++)
         if( o1[i] < o2[i])
            return -1;
         else if( o1[i] > o2[i])
            return 1;
      
      if( o1.length == o2.length)
         return 0;

      if( o1.length <o2.length)
         return -1;
      return 1;
     
   }

  
     
  }
  
  //Assumes all h values are the same
  private void CalcMarkers2D( int N,float[][]hkl,float[][]qq, Vector<float[]>Qs)
  {
     if( hkl == null || qq == null || hkl.length <N || qq.length <N)
        return;
     float kmin = Float.POSITIVE_INFINITY;
     float kmax = Float.NEGATIVE_INFINITY;
     for( int i=0; i< N; i++)
     {
        if( hkl[i].length !=3)
           return;
        if( hkl[i][1] < kmin)
           kmin =hkl[i][1];
        if( hkl[i][1] > kmax)
           kmax =hkl[i][1];
        
     }
     
     for( int k= (int)(kmin-.1); k < kmax+.1; k++)
       for( int side =0;side <3; side++)
          for( int side1 =1; side1 < 4-side; side1++)
          {
             float alpha1 = calcAlpha( k, hkl[side][1],hkl[side+1][1]);
             if( alpha1 >=0 && alpha1 <=1)
             {
                float[]hkl1 = Ptt( alpha1,hkl[side],hkl[side+1]);
                float[]qq1 = Ptt(alpha1,qq[side],qq[side+1]);
                float alpha2 = calcAlpha( k, hkl[side1][1],hkl[side1+1][1]);
                if( alpha2 >=0 && alpha2 <=1)
                {
                   float[] hkl2 = Ptt(alpha2,hkl[side1],hkl[side1+1]);
                   float[] qq2 = Ptt(alpha2,qq[side1],qq[side1+1]);
                   for( int l = (int)(Math.min( hkl2[2] , hkl1[2] )-.1);
                       l < (int)(Math.max( hkl2[2] , hkl1[2] )+.1); l++)
                       {
                          float alpha3 = calcAlpha(l, hkl1[2], hkl2[2]);
                          if( alpha3 >=0 && alpha3 <=1)
                             Qs.addElement( Ptt(alpha3,qq1,qq2));
                       }
                   
                }
             }
          }

  }
  private boolean AdjacentSides( int i, int j)
  {
     if( i-j == 1 || i-j == -1)
        return true;
     if( (i==0 && j==3) || (i==3 && j==0) )
        return true;
     return false;
  }
  private void Xchange( float[] shell, float[] side,float[][]hkl,  float[][]qq, int i, int j )
  {
     float sh = shell[i];
     float sd  = side[i];
     float[] hh = hkl[i];
     float[] qr =qq[i];
     shell[i]= shell[j];
     side[i]= side[j];
     hkl[i]=hkl[j];
     qq[i] = qq[j];
     shell[j]= sh;
     side[j]= sd;
     hkl[j]=hh;
     qq[j]= qr;
  }
  
  private static float[] Ptt( float alpha, float[] hkl1, float[]hkl2)
  {
     float[] Res = new float[3];
     for( int i=0; i< 3;i++)
        Res[i] = hkl1[i]+(hkl2[i]-hkl1[i])*alpha;
     return Res;
  }
  

  
  private static float calcAlpha( int d, float start, float end)
  {
     if( start == end)
        return 0;
     return (d-start)/(end-start);
  }
  
  
  private float[][] Mult( float[][]mat1,float[][] subMat2)
  {
     float[][] Res =new float[3][3];
     Arrays.fill( Res[0] , 0f );
     Arrays.fill( Res[1] , 0f );
     Arrays.fill( Res[2] , 0f );
     
     for( int row=0; row< 3; row++)
       for( int col=0; col < 3; col++)
          for(int srow=0; srow < mat1[row].length; srow++)
          Res[row][col] += mat1[row][srow] * subMat2[srow][col];
     
     return Res;
     
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
     if( u != null  && !useAll )
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
     String fname = null;

     /** 
     *  Displays a JFileChooser box to save the table and then writes header information
     *  and the data to the file
     */  
     public void actionPerformed( ActionEvent evt )
       {
        JFileChooser jf ;
        if( fname == null )  
           jf = new JFileChooser();
        else 
           jf = new JFileChooser( fname );
        FileOutputStream fout = null;
        if( !( jf.showSaveDialog( null ) == JFileChooser.CANCEL_OPTION ) )
           try
             {
              fname = jf.getSelectedFile().toString();
              File ff = new File( fname );
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
     
     //float MaxTime = state.get_float(ViewerState.TABLE_TS_TIMEMAX);
     //float MinTime = state.get_float(ViewerState.TABLE_TS_TIMEMIN);
     int MinRow = state.get_int(ViewerState.TABLE_TS_ROWMIN);
     //int MaxRow = state.get_int(ViewerState.TABLE_TS_ROWMAX);  
     int MinCol = state.get_int(ViewerState.TABLE_TS_COLMIN);
     //int MaxCol = state.get_int(ViewerState.TABLE_TS_COLMAX); 
  
     try{
     for( int i = 0; i < xvals1.length; i++ ){
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
     if(xvals1 == null || xvals1.length< 1)
        xvals1 = new float[]{0f,1f};
     else if( xvals1.length < 2){
        float x = xvals1[0];
        xvals1= new float[]{x,x+1};
     }
        
        
     x_scale = new VariableXScale( xvals1);
     setXScale( x_scale);
     
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
     
     int DetectorNum = state.get_int( ViewerState.TABLE_TS_DETNUM);
     if( DetectorNum >= 0)
       setDetNum( DetectorNum );
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
    


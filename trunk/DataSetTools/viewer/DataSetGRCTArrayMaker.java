
/*
 * File:  DataSetGRCTArrayMaker.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2004/05/17 13:55:08  rmikk
 * Initial Checkin
 *
 */
package DataSetTools.viewer;
import DataSetTools.dataset.*;
import java.util.*;
import java.awt.event.*;
import gov.anl.ipns.ViewTools.UI.*;
import DataSetTools.components.ui.*;
import DataSetTools.viewer.*;
import gov.anl.ipns.ViewTools.*;
import javax.swing.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.*;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.Menu.*;
import java.awt.*;
import gov.anl.ipns.Util.Sys.*;
import DataSetTools.viewer.Table.*;
import gov.anl.ipns.ViewTools.Components.Transparency.*;


/**
  *  This class is an array producer for Gridded data sets.  Row, column, time
  *  grid and DataSet are the choices for dimensions.  Any one dimension can represent
  *  a display row and any other one can represent a display column.  The other 
  *  dimensions( if more than 1) have animation controllers to change their values
  */
public class DataSetGRCTArrayMaker  implements IArrayMaker_DataSet, IVirtualArray2D
                                            {

   DataSet[] DataSets;
   ViewerState  state;
   IHandler[] Handler = new IHandler[5];
   float[] pixel_min;//4 is DS Num,3-GridNum, 2-row,1-col, 0 is time indicies
   float[] pixel_max;// index in given dimension
   int[] GridNums;
   float MinTime,MaxTime;
   int NtimeChan = 0;// Number of channels over all, 0 use channels
   int NrowChan = 0;// Number of row channels
   int NColChan = 0;// Number of col channels
   int NDssChan  = 0;// Number of data set channels
   int NGridChan = 0;// Number of grid Channels
   int MaxChannels, MaxRows,MaxCols;
   int[] Permutation ;// Permutation[0] index in pixel
   AnimationController[] ACS;
   XScaleChooserUI[] Xscales;
   int NstepDims = 5;
//----------------- GUI Elements-----------------
   JButton Dimension;
   //MyJPanel  DimGUI;
   ViewControl[] AnimXscls;
   
   public DataSetGRCTArrayMaker( DataSet[] DSS, ViewerState state){
      this.state = state;
      DataSets = DSS;
      init();
   }
   public DataSetGRCTArrayMaker( DataSet DS, ViewerState state){
     this.state = state;
     DataSets = new DataSet[1];
     DataSets[0] = DS;
     init();
   }
   private void init(){
     pixel_min = new float[5];
     pixel_max = new float[5];
     Arrays.fill(pixel_min,0f);
     Arrays.fill(pixel_max,1f);
     pixel_min[1]++;
     pixel_min[2]++;
     pixel_max[1]++;
     pixel_max[2]++;
     Permutation = new int[5];
     for( int i=0; i< 5; i++)
        Permutation[i]= i;
     //----------- Get ranges of rows,cols,etc in the set of data sets-----
     Vector V = new Vector();
     MaxChannels = -1; MaxRows=-1; MaxCols = -1;
     MinTime = Float.POSITIVE_INFINITY;
     MaxTime = Float.NEGATIVE_INFINITY;
     
     for( int i=0; i< DataSets.length; i++){
        int[] grid= NexIO.Write.NxWriteData.getAreaGrids(DataSets[i]);
        if( grid != null)
          for( int k = 0 ; k < grid.length; k++){
            if( V.indexOf(new Integer(grid[k]))<0){
              V.addElement( new Integer( grid[k]));
            }
           IDataGrid G =NexIO.Write.NxWriteData.getAreaGrid(DataSets[i],grid[k]);
           if( G.num_rows() >MaxRows) 
               MaxRows=  G.num_rows();
           if( G.num_cols() > MaxCols)
               MaxCols = G.num_cols();
           XScale D = G.getData_entry( 1,1).getX_scale();
           if( D.getNum_x() > MaxChannels)
             MaxChannels = D.getNum_x();
           if( D.getStart_x()< MinTime)
               MinTime = D.getStart_x();
           if( D.getEnd_x()> MaxTime)
               MaxTime= D.getEnd_x();
      
          }
     }
        GridNums = new int[V.size()];
        for( int i = 0; i < V.size(); i++)
          GridNums[i]= ((Integer)(V.elementAt(i))).intValue();
        java.util.Arrays.sort(GridNums);        
        
      // Eliminate cases so do not step thru dimensions of length 2

      ACS = new AnimationController[5];
      Xscales = new XScaleChooserUI[5];
     if( DataSets.length <2){
       NstepDims--;
       pixel_min[4] = 0;
       pixel_max[4] =1;
       ACS[4]= null;
       Xscales[4]= null;      
     }else{
       ACS[4] = new AnimationController();
       Xscales[4]= new XScaleChooserUI("DataSet","index",(float)0, (float)DataSets.length,0);
       Xscales[4].addActionListener( new DataSetXsclActionListener());
       float[] xvals = new float[DataSets.length];
       for( int i=0; i<xvals.length; i++)
          xvals[i]=(float)i;
       ACS[4].setFrame_values( xvals);
       ACS[4].setBorderTitle("DataSet");
       ACS[4].setTextLabel("");
       ACS[4].addActionListener( new DataSetAnimActionListener());

     }
    
     if( GridNums.length < 2){
       Permutation[4]=3;
       Permutation[3] = 4;
       NstepDims--;
       pixel_min[3] = 0;
       pixel_max[3] =1;
       ACS[3]=null;
       Xscales[3]=null;
     }else{
       ACS[3] = new AnimationController();
       Xscales[3]= new XScaleChooserUI("GridNum","index",(float)0, (float)GridNums.length,0);
       Xscales[3].addActionListener( new GridXsclActionListener());
       float[] xvals = new float[ GridNums.length ];
       for( int i=0; i < xvals.length; i++)
          xvals[i]=(float)GridNums[i];
       ACS[3].setFrame_values( xvals);
       ACS[3].setBorderTitle("Grid");
       ACS[3].setTextLabel("ID");
       ACS[3].addActionListener( new GridAnimActionListener());

     }
     
     if( MaxCols < 2){
       int save = Permutation[NstepDims-1];
       Permutation[NstepDims-1] = 2;
       Permutation[2]=save;
       NstepDims--;
       pixel_min[2] = 1;
       pixel_max[2] =2;
       ACS[2]=null;
       Xscales[2]=null;
     }else{
       ACS[2] = new AnimationController();
       Xscales[2]= new XScaleChooserUI("Column","",(float)1, (float)MaxCols,0);
       Xscales[2].addActionListener( new ColXsclActionListener());
       float[] xvals = new float[MaxCols];
       for( int i=0; i<xvals.length; i++)
          xvals[i]=(float)(i+1);
       ACS[2].setFrame_values( xvals);
       ACS[2].setBorderTitle("Column");
       ACS[2].setTextLabel("");
       ACS[2].addActionListener( new ColAnimActionListener());

     }
     if( MaxRows < 2){
       int save = Permutation[NstepDims-1];
       Permutation[NstepDims-1] = 1;
       Permutation[1]=save;
       NstepDims--;
       pixel_min[1] = 1;
       pixel_max[1] =2;
       ACS[1]=null;
       Xscales[1]=null;
     }else{

       ACS[1] = new AnimationController();
       Xscales[1]= new XScaleChooserUI("Row","",(float)1, (float)MaxRows,0);
       Xscales[1].addActionListener( new RowXsclActionListener());
       float[] xvals = new float[MaxRows];
       for( int i=0; i<xvals.length; i++)
          xvals[i]=(float)(i+1);
       ACS[1].setFrame_values( xvals);
       ACS[1].setBorderTitle("Row");
       ACS[1].setTextLabel("");
       ACS[1].addActionListener( new RowAnimActionListener());
     }
    if( MaxChannels < 2){
       int save = Permutation[NstepDims-1];
       Permutation[NstepDims-1] = 0;
       Permutation[0]=save;
       NstepDims--;
       pixel_min[0] = 0;
       pixel_max[0] =1;
       ACS[0]=null;
       Xscales[0]=null;
     }else{
       ACS[0] = new AnimationController();
       Xscales[0]= new XScaleChooserUI("Time","us",MinTime, MaxTime,0);
       Xscales[0].addActionListener( new TimeXsclActionListener());
       float[] xvals = new float[20];
       for( int i=0; i<xvals.length; i++)
          xvals[i]= MinTime+i*(MaxTime-MinTime)/20.f;
       ACS[0].setFrame_values( xvals);
       ACS[0].setBorderTitle("Time");
       ACS[0].setTextLabel("");
       ACS[0].addActionListener( new TimeAnimActionListener());
    }
    Handler[0] = new TimeHandler( MinTime,MaxTime,MaxChannels,0);
    Handler[1] = new RowHandler( MaxRows,0);
    Handler[2] = new ColHandler( MaxCols,0);
    Handler[3] = new GridHandler( GridNums, 0);
    Handler[4] = new DataSetHandler( DataSets, 0);

   for( int i=0; i < 2 ; i++)
      SetEnabled(ACS[Permutation[i]], false);
  //Set up Controls;
   Dimension = new JButton("Dimensions");
   Dimension.addActionListener( new ButtonListener());
   
   AnimXscls= new ViewControl[ 1+2*NstepDims];
   AnimXscls[0]= new ViewControlMaker(Dimension);
   for( int i=0; i<NstepDims;i++){
      AnimXscls[1+2*i]= new ViewControlMaker( ACS[Permutation[i]]);
      AnimXscls[2+2*i]= new ViewControlMaker(Xscales[Permutation[i]]);
   }
   SetEnabled(ACS[Permutation[0]],false);
   SetEnabled(ACS[Permutation[1]],false);
   
   }//init


//----------------------- IArrayMaker Methods ---------------------
  /**
   * Return controls needed by the component.
   */ 
   public JComponent[] getSharedControls(){
      return AnimXscls;
   }
   public JComponent[] getPrivateControls(){
     return new JComponent[0];
   }

  /**
   * Return view menu items needed by the component.
   */   
   public ViewMenuItem[] getSharedMenuItems( ){
     return new ViewMenuItem[0];
   }
   public ViewMenuItem[] getPrivateMenuItems( ){
     return new ViewMenuItem[0];
   }
  
   public String[] getSharedMenuItemPath( ){
     return new String[0];
   }
   public String[] getPrivateMenuItemPath( ){
     return new String[0];
   }
  Vector actionListeners = new Vector();
  /**
  *    Adds an ActionListener to this VirtualArray. See above for
  *    action events that will be sent to the listeners
  */
  public void addActionListener( ActionListener listener){
     if( listener == null)
       return;
     if( actionListeners.indexOf(listener) >=0)
        return;
     actionListeners.add(listener);
  }
  void notifyListeners( String reason){
    Object src = this;
    for( int i=0; i< NstepDims; i++)
       if( i < 2)// NOT steppable cause in display
          SetEnabled(ACS[Permutation[i]],false);
       else 
          SetEnabled(ACS[Permutation[i]],true);
    
    ActionEvent evt = new ActionEvent( src,ActionEvent.ACTION_PERFORMED, reason,0);
    for( int i=0;i < actionListeners.size();i++)
      ((ActionListener)(actionListeners.elementAt(i))).actionPerformed( evt);
    

  }
  private void SetEnabled( Container Cont,boolean status){
     Cont.setEnabled( status);
     for(int i=0;i < Cont.getComponentCount();i++){
        Component comp = Cont.getComponent( i);
        if( comp instanceof Container)
           SetEnabled((Container)comp,status);
        else
           comp.setEnabled(status);
     }

  }
  /**
   * Remove a specified listener from this view component.
   */ 
   public void removeActionListener( ActionListener act_listener ){
      actionListeners.remove(act_listener);
   }
  
  /**
   * Remove all listeners from this view component.
   */ 
   public void removeAllActionListeners(){
     actionListeners= new Vector();
   }

  /**
  *    Invoked whenever there is an action event on and instance of
  *    a class which is being listened for.  Also, anyone can invoke the
  *    method.  See above the action commands that must be supported
  */
   public void actionPerformed( ActionEvent evt){
  }

  public IVirtualArray getArray(){
     return this;
  }

  /**
    * Used to dispose of orphan windows and other resources when the
    * parent is removed from display
    */
  public void kill(){
  }

//------------------ IArrayMaker_DataSet Methods-----------------------
  public int getGroupIndex( ISelectedData Info){
    return -1;
  }

  /**
  *    Returns the time corresponding to the given Selected Data
  *    @param  Info  Should be a SelectedData2D Object
  */
  public float getTime( ISelectedData Info){
     return Float.NaN;
  }


  /**
  *    Returns the selected data corresponding to the give PointedAt
  *    condition
  *    @param PointedAtGroupIndex   The index in the DataSet of the group
  *             that is being pointed at
  *    @param PointedAtTime The time in question when the pointing takes
  *          place
  *    @return  a SelectedData2D containing the row,column and time corresponding
  *              to the selected condition
  */
  public ISelectedData getSelectedData( int PointedAtGroupIndex,
                                         float PointedAtTime){
                                         	
    return null;
  }
  
  public void setTime( float time){
  }
  
  public void SelectRegion( ISelectedRegion region){
  }


 /**
  * Sets the attributes of the data array within a AxisInfo wrapper.
  * This method will take in an integer to determine which axis
  * info is being altered.
  *
  *  @param  axis Use AxisInfo.X_AXIS (0) or AxisInfo.Y_AXIS (1).
  *  @param  min Minimum value for this axis.
  *  @param  max Maximum value for this axis.
  *  @param  label Label associated with the axis.
  *  @param  units Units associated with the values for this axis.
  *  @param  islinear Is axis linear (true) or logarithmic (false)
  */
  public void setAxisInfo( int axis, float min, float max,
			   String label, String units, boolean islinear ){
   }

//------------------------------ IVirtualArray2D Methods ----------------
  
 /**
  * Sets the attributes of the data array within a AxisInfo wrapper.
  * This method will take in an integer to determine which axis
  * info is being altered.
  * 
  *  @param  axis Use AxisInfo.X_AXIS (0) or AxisInfo.Y_AXIS (1).
  *  @param  info The axis info object associated with the axis specified.
  */
  public void setAxisInfo( int axis, AxisInfo info ){
  }
  
 /*
  ***************************************************************************
  * The following methods must include implementation to prevent
  * the user from exceeding the initial array size determined
  * at creation of the array. If an M x N array is specified,
  * the parameters must not exceed (M-1,N-1). 
  ***************************************************************************
  */
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
  public float[] getRowValues( int row_number, int from, int to ){

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
  *
  *  @param values  array of elements to be put into the row
  *  @param row     row number of desired row
  *  @param start   what column number to start at
  */
  public void setRowValues( float[] values, int row, int start ){
  }
  
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
  public float[] getColumnValues( int column_number, int from, int to ){

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
  *
  *  @param values  array of elements to be put into the column
  *  @param column  column number of desired column
  *  @param start   what row number to start at
  */
  public void setColumnValues( float[] values, int column, int start ){
  }
  
 /**
  * Get value for a single array element.
  *
  *  @param  row     row number of element
  *  @param  column  column number of element
  *  @return If element is found, the float value for that element is returned.
  *	     If element is not found, zero is returned.
  */ 
  public float getDataValue( int row, int column ){
     int f= Permutation[0];
     int n = Permutation[1];
     pixel_min[f] = Handler[f].getMin( row);
     pixel_max[f] = Handler[f].getMax( row);
     pixel_min[n] = Handler[n].getMin( column);
     pixel_max[n] = Handler[n].getMax( column);
     return Handler[4].getValue(DataSets,pixel_min,pixel_max); //pixel min to pixel max
     
  }
  
 /**
  * Set value for a single array element.
  *
  *  @param  row     row number of element
  *  @param  column  column number of element
  *  @param  value   value that element will be set to
  */
  public void setDataValue( int row, int column, float value ){
  }
  
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
  public float[][] getRegionValues( int first_row, int last_row,
				    int first_column, int last_column ){

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
  *
  *  @param  values	2-D array of float values 
  *  @param  row_start  first row of the region being altered
  *  @param  col_start  first column of the region being altered
  */
  public void setRegionValues( float[][] values, 
			       int row_start,
        		       int col_start ){
  }
        		       
 /**
  * Returns number of rows in the array.
  *
  *  @return This returns the number of rows in the array. 
  */ 
  public int getNumRows(){
    int f= Permutation[0];
    return Handler[f].getNSteps();
  }

 /**
  * Returns number of columns in the array.
  *
  *  @return This returns the number of columns in the array. 
  */
  public int getNumColumns(){
    int f= Permutation[1];
    return Handler[f].getNSteps();
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
  public boolean setErrors( float[][] error_values ){
     return true;
  }
  
 /**
  * Get the error values corresponding to the data. If no error values have
  * been set, the square-root of the data value will be returned.
  *
  *  @return error values of the data.
  */
  public float[][] getErrors(){
     return null;
  }
  
 /**
  * Use this method to specify whether to use error values that were passed
  * into the setErrors() method or to use the square-root of the data value.
  *
  *  @param  use_sqrt If true, use square-root.
  *                   If false, use set error values if they exist.
  */
  public void setSquareRootErrors( boolean use_sqrt ){
  }
 
 /**
  * Get an error value for a given row and column. Returns Float.NaN if
  * row or column are invalid.
  *
  *  @param  row Row number.
  *  @param  column Column number.
  *  @return error value for data at [row,column]. If row or column is invalid,
  *          Float.NaN is returned.
  */
  public float getErrorValue( int row, int column ){
    return 0.0f;
  }

//------------- IVirtualArray Methods-----------------------------
     
 /**
  * This method will return the title assigned to the data. 
  *
  *  @return title assigned to the data.
  */
  public String getTitle(){
    return "Title";
  }
  
 /**
  * This method will assign a title to the data. 
  *
  *  @param  title - title describing the data
  */
  public void setTitle( String title ){
  }
      
 /**
  * Set all values in the array to a value. This method will usually
  * serve to "initialize" or zero out the array. 
  *
  *  @param  value - single value used to set all other values in the array
  */
  public void setAllValues( float value ){
  }
 
 /**
  * Gets the dimension of the VirtualArray. For example, IVirtualArray1D = 1,
  * IVirtualArray2D = 2.
  *
  *  @return dimension of VirtualArray. This value is an primative integer
  *          not a Dimension.
  */
  public int getDimension(){
     return 2;
  }
  
 /**
  * Get detailed information about this axis.
  *
  *  @param  axis The integer code for the axis, starting at 0.
  *  @return The axis info for the axis specified.
  *  @see    gov.anl.ipns.ViewTools.Components.AxisInfo
  */
  public AxisInfo getAxisInfo( int axiscode ){
    if( axiscode ==AxisInfo.X_AXIS){
      int f = Permutation[0];
      return getAxisInfo( f,true);
    }else if( axiscode == AxisInfo.Y_AXIS){
      return getAxisInfo(Permutation[1],true);
    }else 
      return null;
  }
 private AxisInfo getAxisInfo( int dim, boolean b){
      int f=dim;
      if( dim==0)//time
         return new AxisInfo( MinTime,MaxTime, DataSets[0].getX_label(),
             DataSets[0].getX_units(), true);
      else if( f==1)//row
        
         return new AxisInfo((float)1,(float)MaxRows, "Row","",true);
      else if( f==2)//col
          return new AxisInfo((float)1,(float)MaxCols, "Col","",true);
      else if( f==3)//grid
         return new AxisInfo((float)0,(float)GridNums.length, "Grid","",true);
      else if( f==4)//row
          return new AxisInfo((float)0,(float)DataSets.length, "DataSet","",true);
      else
         return null;
  
 }
  


   

//================================ Handlers and Listeners =============
public void setGrids( DataSet DS){
   Grids = null;
   Grids = new IDataGrid[ GridNums.length];
   for( int i=0; i< Grids.length; i++){
      Grids[i] = NexIO.Write.NxWriteData.getAreaGrid(DS, GridNums[i]);

   }
}

interface IHandler{
   public float getMin( int i);
   public float getMax( int i);
   public int getNSteps();
   public float getValue(DataSet[] DSS, float[] minInds, float[] maxInds);

}
IDataGrid[] Grids= null;
int DSnum = -1;
class DataSetHandler implements IHandler{
  DataSet[] DSS;
  int Nslices;
  float D;
  public DataSetHandler( DataSet[] DSS, int Nslices){
    this.DSS = DSS;
    this.Nslices = Nslices;
    if( Nslices >= DSS.length)
        this.Nslices = DSS.length;
    if( Nslices >0)
       D = DSS.length/(float)Nslices;
    else 
       D = -1;
  }
   public float getMin( int i){
     if( Nslices ==0)return i;
     return i*D;
   }
   public float getMax( int i){
        if(Nslices <= 0) return i+1;
        return i*D +D;
   }

   public int getNSteps(){
      if( Nslices <=0)
        return DSS.length;
      return Nslices;
   }
   public float getValue(DataSet[] DSS, float[] minInds, float[] maxInds){
   float[] savMin=new float[5],savMax=new float[5];
   System.arraycopy( minInds,0,savMin,0,5);
   System.arraycopy( maxInds,0,savMax,0,5);
   float Res=0;
   float first = Float.NaN;
   for( int i=(int) minInds[4]; i< (int)maxInds[4]; i++){
       if( i != DSnum){
         setGrids( DSS[i]);
         DSnum = i;
       }
      savMin[4]=i;
      savMax[4] = i+1;
      DataSet DS = DSS[i];
      Res += Handler[3].getValue( DSS, savMin, savMax);
      if(Float.isNaN(first))
        first = Res;
   }
   Res -= first*(minInds[4] -(int)minInds[4]);
   if( maxInds[4] == (int)maxInds[4]) return Res;
   savMin[4]=(int)maxInds[4];
   savMax[4] =1+savMin[4];
   if( savMax[4] !=DSnum)
       setGrids(DSS[(int)savMax[4]]);
   first = Handler[3].getValue(DSS,savMin,savMax);
   Res += (maxInds[4] -(int)maxInds[4])*first;
   return Res;
  
    
   }

}
class GridHandler implements IHandler{
  int[] GridNums;
  int Nslices;
  float D;
  public GridHandler( int[] GridNums, int Nslices){
    this.GridNums = GridNums;
    this.Nslices = Nslices;
    if( Nslices > GridNums.length)
      Nslices =GridNums.length;
    if( Nslices >0)
       D = GridNums.length/(float)Nslices;
    else 
       D = -1;
  }
   public float getMin( int i){
      if( Nslices <=0)return i;
      return i*D;
   }
   public float getMax( int i){
      if( Nslices <=0)return i+1;
      return i*D+1;
   }
  
   public int getNSteps(){
     if( Nslices <=0)
       return GridNums.length;
     return Nslices;

   }
   public float getValue( DataSet[] DSS,float[] minInds, float[] maxInds){
     DataSet DS = DSS[(int)minInds[4]];
     float[] savMin=new float[5],savMax=new float[5];
     System.arraycopy( minInds,0,savMin,0,5);
     System.arraycopy( maxInds,0,savMax,0,5);
     float Res=0;
     float first = Float.NaN;
     for( int i=(int) minInds[3]; i< (int)maxInds[3]; i++){
      savMin[3]=i;
      savMax[3] = i+1;
      if( Grids[i] != null){
        Res += Handler[2].getValue( DSS, savMin, savMax);
        if(Float.isNaN(first))
          first = Res;
      }
   }
   Res -= first*(minInds[3] -(int)minInds[3]);
   if( maxInds[3] == (int)maxInds[3]) return Res;
   savMin[3]= (int)maxInds[3];
   savMax[3] =1+savMin[3];
   if(Grids[(int)savMin[3]]!= null){
     first = Handler[2].getValue(DSS,savMin,savMax);
     Res += (maxInds[3] -(int)maxInds[3])*first;
   }
   return Res;
  
        

     }
   

}
class ColHandler implements IHandler{
   int MaxCols,
       Nslices;
  float D;
   public ColHandler( int MaxCols, int Nslices){
     this.MaxCols = MaxCols;
     this.Nslices = Nslices;
     if( Nslices >= MaxCols)
        this.Nslices = MaxCols;
    if( Nslices >0)
       D = MaxCols/(float)Nslices;
    else 
       D = -1;
   }
   public float getMin( int i){
      if( Nslices <=0)return i+1;
      return i*D+1;
      
   }
   public float getMax( int i){
         if( Nslices <=0)return i+2;
         return i*D+1+D;
   }

   public int getNSteps(){
     if( Nslices <=0)
       return MaxCols;
     return Nslices;

    }
   public float getValue(DataSet[] DSS, float[] minInds, float[] maxInds){
     DataSet DS = DSS[(int)minInds[4]];
     int gridNum = GridNums[(int)minInds[3]];
     float[] savMin=new float[5],savMax=new float[5];
     System.arraycopy( minInds,0,savMin,0,5);
     System.arraycopy( maxInds,0,savMax,0,5);
     float Res=0;
     float first = Float.NaN;
     for( int i=(int) minInds[2]; i< (int)maxInds[2]; i++){
      savMin[2]=i;
      savMax[2] = i+1;
    
      Res += Handler[1].getValue( DSS, savMin, savMax);
      if(Float.isNaN(first))
         first = Res;
    
   }
   Res -= first*(minInds[2] -(int)minInds[2]);
   if( maxInds[2] == (int)maxInds[2]) return Res;
   savMin[2]=(int)maxInds[2];
   savMax[2] =1+savMin[2];
   first = Handler[1].getValue(DSS,savMin,savMax);
   Res += (maxInds[2] -(int)maxInds[2])*first;
   
   return Res;
  
     
   }


}
class RowHandler implements IHandler{
   int MaxRows,
       Nslices;
  float D;
   public RowHandler( int MaxRows, int Nslices){
     this.MaxRows = MaxRows;
     this.Nslices = Nslices;
      if(Nslices >= MaxRows)
        Nslices = MaxRows;
    if( Nslices >0)
       D = MaxRows/(float)Nslices;
    else 
       D = -1;
   }

   public float getMin( int i){
      if( Nslices <=0)return i+1;
      return i*D+1;
   }
  public  float getMax( int i){
      if( Nslices <=0)return i+2;
      return i*D+1+D;
   }

   public int getNSteps(){
     if( Nslices <=0)
       return MaxRows;
     return Nslices;

   }
   public float getValue(DataSet[] DSS, float[] minInds, float[] maxInds){

     DataSet DS = DSS[(int)minInds[4]];
     int gridNum = GridNums[(int)minInds[3]];
     int col =(int)minInds[2];
     float[] savMin=new float[5],savMax=new float[5];
     System.arraycopy( minInds,0,savMin,0,5);
     System.arraycopy( maxInds,0,savMax,0,5);
     float Res=0;
     float first = Float.NaN;
     for( int i=(int) minInds[1]; i< (int)maxInds[1]; i++){
      savMin[1]=i;
      savMax[1] = i+1;
    
      Res += Handler[0].getValue( DSS, savMin, savMax);
      if(Float.isNaN(first))
         first = Res;
    
   }
   Res -= first*(minInds[1] -(int)minInds[1]);
   if( maxInds[1] == (int)maxInds[1]) return Res;
   savMin[1]=(int)maxInds[1];
   savMax[1] =1+savMin[1];
   first = Handler[0].getValue(DSS,savMin,savMax);
   Res += (maxInds[1] -(int)maxInds[1])*first;
   
   return Res;
   }

}
class TimeHandler implements IHandler{
   float MinTime,
         MaxTime;
   int MaxChannels,
        Nslices;

  float D;
   public TimeHandler( float MinTime, float MaxTime, int MaxChannels,int Nslices){
     this.MinTime = MinTime;
     this.MaxTime = MaxTime;
     this.MaxChannels = MaxChannels;
     this.Nslices = Nslices;
     if( Nslices < 0)
          Nslices = 0;
     
    if( Nslices >0)
       D = (MaxTime-MinTime)/(float)Nslices;
    else 
       D = -1;
   }
   public float getMin( int i){
      if( Nslices <=0) return i;
      return i*D;
   }
  
   public int getNSteps(){
      if( Nslices <=0)
         return MaxChannels;
      return Nslices;
   }
   public float getMax( int i){
     if( Nslices <=0)return i+1;
     return i*D+D;
    }
   public float getValue(DataSet[] DSS, float[] minInds, float[] maxInds){

     DataSet DS = DSS[(int)minInds[4]];
     int gridNum = GridNums[(int)minInds[3]];
     int col =(int)minInds[2];
     int row = (int)minInds[1];
     IDataGrid grid = Grids[(int)minInds[3]];
     Data D = grid.getData_entry( row,col);
     float Ind_min = minInds[0];
     float Ind_max = maxInds[0];
     XScale xscl= D.getX_scale();
     if( Nslices != 0){// indicies are times->indicies
       Ind_min = GetI(xscl, Ind_min);
       Ind_max = GetI(xscl, Ind_max);
        
     }
     float Res=0;
     float first = Float.NaN;
     float[] yvalues =D.getY_values();
     for( int i=(int) Ind_min; (i< (int)Ind_max)&&(i<yvalues.length); i++){
       Res += yvalues[i];
       
      
      if(Float.isNaN(first))
         first = Res;
    
   }
   Res -= first*(minInds[1] -(int)minInds[1]);
   if( Ind_max == (int)Ind_max) return Res;
   first = yvalues[(int)Ind_max];
   Res += (maxInds[1] -(int)maxInds[1])*first;
   
   return Res;
   }

}

float GetI( XScale xscl, float time){
   int i = xscl.getI(time);
   float time1 = xscl.getX(i-1);
   float time2 = xscl.getX(i);
   if(Float.isNaN(time1))
      return 0;
   if( Float.isNaN(time2))
      return (float)i;
   return i-1+ (time-time1)/(time2-time1);



}
//---------------------Action Listeners------------------------------
// --- Get range of data sets going in the handlers ------------
class DataSetXsclActionListener implements ActionListener{
  int MaxDSS;
  public DataSetXsclActionListener(){
    MaxDSS = DataSets.length;
  }
  public void actionPerformed( ActionEvent evt){
   XScale xscl=  Xscales[4].getXScale();
   if( xscl == null)
      Handler[4]= new DataSetHandler( DataSets,0);
   else
      Handler[4] = new DataSetHandler( DataSets, xscl.getNum_x());
   notifyListeners( IArrayMaker.DATA_CHANGED);

  }
}

class DataSetAnimActionListener implements ActionListener{

  public void actionPerformed( ActionEvent evt){
     int r = ACS[4].getFrameNumber();
     pixel_min[4] = Handler[4].getMin( r);
     pixel_max[4] = Handler[4].getMax( r );
     notifyListeners( IArrayMaker.DATA_CHANGED);
  }
}

class GridXsclActionListener implements ActionListener{
  int MaxGrids;
  public GridXsclActionListener(){
    MaxGrids = GridNums.length;
  }
  public void actionPerformed( ActionEvent evt){
   XScale xscl=  Xscales[3].getXScale();
   if( xscl == null)
      Handler[3]= new GridHandler( GridNums,0);
   else
      Handler[3] = new GridHandler( GridNums, xscl.getNum_x());
   notifyListeners( IArrayMaker.DATA_CHANGED);

  }
  
}

class GridAnimActionListener implements ActionListener{

  public void actionPerformed( ActionEvent evt){

     int r = ACS[3].getFrameNumber();
     pixel_min[3] = Handler[3].getMin( r);
     pixel_max[3] = Handler[3].getMax( r );
     notifyListeners( IArrayMaker.DATA_CHANGED);
  }
}
class ColXsclActionListener implements ActionListener{
  int MaxCols;
  public ColXsclActionListener(){
    this.MaxCols = MaxCols;
  }
  public void actionPerformed( ActionEvent evt){
   XScale xscl=  Xscales[2].getXScale();
   if( xscl == null)
      Handler[2]= new ColHandler( MaxCols,0);
   else
      Handler[2] = new ColHandler( MaxCols, xscl.getNum_x());
   notifyListeners( IArrayMaker.DATA_CHANGED);

  }
    
  
}

class ColAnimActionListener implements ActionListener{

  public void actionPerformed( ActionEvent evt){
     int r = ACS[2].getFrameNumber();
     pixel_min[2] = Handler[2].getMin( r);
     pixel_max[2] = Handler[2].getMax( r );
     notifyListeners( IArrayMaker.DATA_CHANGED);
  }
}

class RowXsclActionListener implements ActionListener{
  int MaxRows;
  public RowXsclActionListener(){
    this.MaxRows = MaxRows;
  }

  public void actionPerformed( ActionEvent evt){
   XScale xscl=  Xscales[1].getXScale();
   if( xscl == null)
      Handler[1]= new RowHandler( MaxRows,0);
   else
      Handler[1] = new RowHandler( MaxRows, xscl.getNum_x());
   notifyListeners( IArrayMaker.DATA_CHANGED);

  }
}


class RowAnimActionListener implements ActionListener{

  public void actionPerformed( ActionEvent evt){
     int r = ACS[1].getFrameNumber();
     pixel_min[1] = Handler[1].getMin( r);
     pixel_max[1] = Handler[1].getMax( r );
     notifyListeners( IArrayMaker.DATA_CHANGED);
     
  }
}


class TimeXsclActionListener implements ActionListener{
  float MinTime,MaxTime;
  int MaxChannels;
  public TimeXsclActionListener(){
    this.MinTime = MinTime;
    this.MaxTime = MaxTime;
    this.MaxChannels = MaxChannels;
  }
  public void actionPerformed( ActionEvent evt){
   XScale xscl=  Xscales[0].getXScale();
   if( xscl == null)
      Handler[0]= new TimeHandler( MinTime,MaxTime,MaxChannels,0);
   else
      Handler[0] = new TimeHandler( MinTime,MaxTime,MaxChannels, xscl.getNum_x());
   notifyListeners( IArrayMaker.DATA_CHANGED);

  }
 }


class TimeAnimActionListener implements ActionListener{

  public void actionPerformed( ActionEvent evt){
     int r = ACS[0].getFrameNumber();
     pixel_min[0] = Handler[0].getMin( r);
     pixel_max[0] = Handler[0].getMax( r );
     notifyListeners( IArrayMaker.DATA_CHANGED);
  }
}
FinishJFrame Frm = null;
class ButtonListener implements ActionListener{
   
   public void actionPerformed( ActionEvent evt){
   	  
      if( Frm == null){
        Frm = new FinishJFrame("Dimension order");
        Frm.addWindowListener( new FrmWindowListener( ));
        Frm.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE);
        Frm.setSize( 600,400);
        Frm.getContentPane().add( new MyJPanel( NstepDims,Permutation));
        Frm.show();
      }
      
      
   }
   public void ClearFrame(){
      Frm=null;
   }
}
class FrmWindowListener extends WindowAdapter{
	
  public  void windowClosed(WindowEvent e){
  	  
    Frm = null;
  }

}
class MyJPanel extends JPanel{
   int NstepDims;
   int[] Permutation;
   JList Coord1, Coord2;
   String[] DimNames ={"Time","Row","Col","Grid","DataSet"};
   public MyJPanel( int NstepDims, int[] Permutation){
     super( new GridLayout( 1,3));
     this.NstepDims = NstepDims;
     this.Permutation =Permutation;
     ListElement[] S = new ListElement[NstepDims];
     
     for( int i=0; i < NstepDims ; i++)
       S[i]= new ListElement(DimNames[Permutation[i]], Permutation[i]);
     
     Coord1 = new JList(S);
     Coord2 = new JList(S);
     Coord1.setBorder(BorderFactory.createTitledBorder(
                          BorderFactory.createLoweredBevelBorder(),
                          "Horizontal axis"
                                                      ) 
                    );
     Coord2.setBorder(BorderFactory.createTitledBorder(
                          BorderFactory.createLoweredBevelBorder(),
                          "Vertical axis"
                                                      ) 
                    );
     add(Coord1);
     add(Coord2);
     JButton but = new JButton("Submit");
     but.addActionListener( new SubmitButtonListener(this));
     add(but);
   }


  public void update(){
     ListElement x = (ListElement)Coord1.getSelectedValue();
     ListElement y = (ListElement)Coord2.getSelectedValue();
     if( (x== null) ||(y== null)){
       JOptionPane.showMessageDialog( null, "Select an item in BOTH lists");
       return;

     }
    if( x==y){
       JOptionPane.showMessageDialog( null, "Select an diffent items from the two lists");
       return;
    }

   int xind = x.getValue();
   int yind = y.getValue();
   swap( Permutation, 0, xind);
   swap(Permutation, 1, yind); 
   notifyListeners( IArrayMaker.DATA_CHANGED);

  }
  
private void swap( int[] P, int indx, int value){
   int j=-1;
   for( int i=0; (i< P.length) &&(j<0); i++)
       if(P[i]== value)
           j=i;
    if( j<0)
       return;
    P[j]= P[indx];
    P[indx]=value; 
}
}//MyJPanel
class ListElement{
  int value;
  String name;
  public ListElement( String name, int value){
     this.value = value;
     this.name = name;
  }
  public String toString(){
     return name;
  }
  public int getValue(){
     return value;
  }
}
class SubmitButtonListener implements ActionListener{
  MyJPanel pan;

  public SubmitButtonListener( MyJPanel pan){
    this.pan = pan;
  }
  public void actionPerformed( ActionEvent evt){
      pan.update();


  }
}

public static void main( String[] args){
   JFrame fr = new JFrame("Test");

   fr.setSize( 900,500);
   fr.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE);
   DataSet[] DSS = null;
   try{
      DSS = Command.ScriptUtil.load( args[0]);
   }catch(Exception s){
     System.exit(0);
   }
   DataSet DS = DSS[DSS.length-1];
   fr.getContentPane().add( new DataSetViewerMaker1(DS,null,
                 new DataSetGRCTArrayMaker( DS, null),
                 //new LargeJTableViewComponent(null,new dummyIVirtualArray2D())
                 new gov.anl.ipns.ViewTools.Components.TwoD.ImageViewComponent( new dummyIVirtualArray2D())
                ));
   fr.show();

}
}//DataSetGRCTArrayMaker

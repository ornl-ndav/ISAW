/*
 * File:  DataBlockSelector.java 
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.16  2004/10/09 14:10:51  rmikk
 * Added some bounds checking code to eliminate run time errors
 *
 * Revision 1.15  2004/09/15 22:03:52  millermi
 * - Updated LINEAR, TRU_LOG, and PSEUDO_LOG setting for AxisInfo class.
 *   Adding a second log required the boolean parameter to be changed
 *   to an int. These changes may affect any ObjectState saved configurations
 *   made prior to this version.
 *
 * Revision 1.14  2004/05/14 15:05:27  rmikk
 * Removed unused variables
 *
 * Revision 1.13  2004/05/06 17:33:36  rmikk
 * Added a setTime Method to the interface
 * Added an argument to the Selected2D constructor
 *
 * Revision 1.12  2004/03/15 19:33:59  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.11  2004/03/15 03:29:01  dennis
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
 * Revision 1.9  2004/02/07 20:13:50  rmikk
 * Fixed Error when initial fields are not present
 *
 * Revision 1.8  2003/12/30 13:07:27  rmikk
 * Added two new choices to a submenu of the select menu item to add the new
 *   attributes: row, col, Detector, slot, input and crate
 *
 * Revision 1.7  2003/12/18 22:46:06  millermi
 * - This file was involved in generalizing AxisInfo2D to
 *   AxisInfo. This change was made so that the AxisInfo
 *   class can be used for more than just 2D axes.
 *
 * Revision 1.6  2003/12/18 13:05:49  rmikk
 * Notifies observers of the data set that the selected data blocks have changed.
 *   This makes this view more interactive with the other views
 *
 * Revision 1.5  2003/12/14 18:41:58  rmikk
 * The Scattering angle column is now in degrees
 *
 * Revision 1.4  2003/12/12 19:48:07  rmikk
 * Removed javadoc error
 * Increased the side of the Field selector window
 *
 * Revision 1.3  2003/12/11 22:09:04  rmikk
 * Added a kill command to remove orphaned windows
 *
 * Revision 1.2  2003/12/11 19:40:22  rmikk
 * Starts with GroupID, Scat Ang, and Tot Count columns
 * The control buttons have more descriptive names and
 *    now occupy the whole space in the Panel
 * A ClearAll( selected) Menu item is not under Select
 * There now is only one Add window no matter how many
 *     times the Add button is pressed
 *
 * Revision 1.1  2003/12/04 20:47:07  rmikk
 * Initial Checkin
 *
 */
package DataSetTools.viewer.Table;


import DataSetTools.viewer.*;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.Menu.*;

import java.awt.event.*;
import javax.swing.*; 
import javax.swing.event.*;
import java.util.*;
import DataSetTools.dataset.*;
import java.awt.*;
import java.lang.reflect.*;
import DataSetTools.operator.DataSet.Attribute.*;

/**
  *  This class is the "ArrayMaker" part of a DataSetViewer that can be used
  *  to sort and select Data Blocks from a DataSet.
  */ 
public class DataBlockSelector implements IArrayMaker_DataSet {

    DataSet DS;
    Vector Fields;      //The names of the Fields in the vies
    TableArray tbArray; // IVirtualArray2D that returns values
    ViewerState state;
    Integer[] GroupSort;

    static final String SpFieldNames1 = "DetPos x;DetPos y;DetPos z;DetPos r;DetPos theta;";
    static final String SpFieldNames = SpFieldNames1 + "DetPos rho;DetPos phi;Group Index;Scat Ang;";


    /**
      *  Constructor
      *  @param DS  The data set to be viewed
      *  @param state  the Viewer state. This is not implemented yet.
      */
    public DataBlockSelector(DataSet DS,  ViewerState state) {
        this.DS = DS;
        Fields = new Vector();
        Fields.addElement("Group ID");
        FieldNames = GetFieldNames( state);
        for (int i = 0; i < FieldNames.length; i++)
            if (Fields.indexOf(FieldNames[i]) < 0)
                Fields.addElement(FieldNames[i]);
        tbArray = new TableArray(DS, Fields);
        GroupSort = new Integer[ DS.getNum_entries()];
        for (int i = 0; i < GroupSort.length; i++)
            GroupSort[i] = new Integer(i);
        
    }
    String[] FieldNames ={"Scat Ang","Total Count"};
    private String[] GetFieldNames( ViewerState state){

       return FieldNames;
    }
  
    //---------------------- IArrayMakerDataSet Methods-----------------------

    int selectedColumn = -1;

    /**
      *  Returns the index of the Group corresponding to the given Info
      *  @param  Info  the Information on what was selected.  Only 
      *                SelectedData2S Info objects are processed
      *  @return  the index of the Group corresponding to the given Info
      */
    public int getGroupIndex(ISelectedData Info) {
        if (Info instanceof SelectedData2D) {
            int row = ((SelectedData2D) Info).getRow();

            selectedColumn = ((SelectedData2D) Info).getCol();
            float GroupID = tbArray.getDataValue(row, 0);

            if (!Float.isNaN(GroupID)) {
                return DS.getIndex_of_data(DS.getData_entry_with_id((int) GroupID));
            } else return -1;

        } else
            return -1;
     
    }

    /**
      *  Returns the time of the selected information.
      *
      *  @param  Info  the Information on what was selected. This is ignored
      *                 since time is not part of this view
      *  @return  the time corresponding to the 1st DataBlocks 1st time channel
      */
    public float getTime(ISelectedData Info) {
        return DS.getData_entry(0).getX_scale().getXs()[0];
    }


    /**
      * Returns the pointed at information in the Selected2D format
      * @param PointedAtGroupIndex  the index of the pointed at group
      * @param PointedAtTime  the time being pointed at( not considered in this
      *             view)
      */
    public ISelectedData getSelectedData(int PointedAtGroupIndex,
        float PointedAtTime) {

        Data D = DS.getData_entry(PointedAtGroupIndex);
        int GroupID = D.getGroup_ID();       

        if (D != null) {
            for (int i = 0; i < tbArray.getNumRows(); i++) {
                if (tbArray.getDataValue(i, 0) == GroupID)
                    return new SelectedData2D(i, 0,PointedAtTime);
            }
          
        }
        return new SelectedData2D(-1, -1, Float.NaN);
    }


    /**
      *  Sets all data blocks in the given region as selected
      * @param region the selected region. Only SelectedRegion2D info is
      *           processed
      */
    public void SelectRegion(ISelectedRegion region) {

        if (region instanceof SelectedRegion2D) {
            int[] rows = ((SelectedRegion2D) region).rows; 

            if (rows == null)
                return;
            for (int i = 0; i < rows.length; i++) {
                int GroupID = (int) tbArray.getDataValue(rows[i], 0);
                Data D = DS.getData_entry_with_id(GroupID);

                if (D != null) {
                    int indx = DS.getIndex_of_data(D);

                    DS.setSelectFlag(indx, true);
                }
            }
           
        DS.notifyIObservers( IObserver.SELECTION_CHANGED);

        }
    }
   
   /**
    *  Sets the time from an external source, like pointed at
    *  Does nothing
    */
   public void setTime( float time){
   	
   	
   }

    //-------------------- IArrayMaker Methods --------------------------------
 

    /**
     * Return controls needed by the component. These will include an add button,
     * a delete button, and a sort button.
     */ 
    public JComponent[] getSharedControls() {

        JComponent[] Res = new JComponent[3];
        JButton add = new JButton("Add Field");

        add.setToolTipText("Add a new Field");
        JButton del = new JButton("Delete Field");

        del.setToolTipText("Delete the last pointed at(clicked) column");
        JButton sort = new JButton("Sort on Field");

        sort.setToolTipText("Sort using the last pointed at(clicked) column");
        add.addActionListener(new AddActionListener());
        del.addActionListener(new DeleteActionListener());
        sort.addActionListener(new SortActionListener());
        Res[0] = PutInJPanel(add);
        Res[1] = PutInJPanel(del);
        Res[2] = PutInJPanel(sort);
        return Res;
    }

    private JPanel PutInJPanel( JComponent comp){
       JPanel jp = new JPanel( new GridLayout( 1,1));
       jp.add(comp);
       return jp;
    }
    /**
     * Presently returns no Controls
     */   
    public JComponent[] getPrivateControls() {
        return new JComponent[0];
    }



    /**
     * Presently returns NO MenuItems
     */   
    public ViewMenuItem[] getSharedMenuItems() {
        JMenuItem jmi = new JMenuItem( "Clear All");
        ViewMenuItem item = new ViewMenuItem(jmi);
        jmi.addActionListener( new myClearActionListener());
        
        ViewMenuItem[] view_menu= new ViewMenuItem[3];
        view_menu[0] = item;

        NewAttrAdd addAttr = new NewAttrAdd();
        jmi = new JMenuItem( "Row,Col,Detector");
        item = new ViewMenuItem(jmi);
        item.addActionListener( addAttr);
        view_menu[1] = item;


        jmi = new JMenuItem( "Crate,Slot,Intput");
        item = new ViewMenuItem(jmi);
        item.addActionListener( addAttr);
        view_menu[2] = item;
        
        return view_menu;
    }

   
    /**
     * resently returns NO MenuItems..
     */
    public ViewMenuItem[] getPrivateMenuItems() {
        return new ViewMenuItem[0];
    }
  

    /**
      * Returns null because there are no MenuItems
      */
    public String[] getSharedMenuItemPath() {
        String[] Res = new String[3];
        Res[0] = "Select";
        Res[1] ="Select.New Attribute";
        Res[2] ="Select.New Attribute";
        return Res;
    }



    /**
      * Returns null because there are no MenuItems
      */  
    public String[] getPrivateMenuItemPath() {

        return null;

    }


    Vector listeners = new Vector();

    /**
     *    Adds an ActionListener to this VirtualArray. The "DataChanged" action
     *    event is the only one that will be sent to listeners.
     */
    public void addActionListener(ActionListener listener) {
        if (listener == null) return;
        if (listeners.indexOf(listener) < 0)
            listeners.addElement(listener);
    }



    /**
     * Remove a specified listener from the listener list.
     */ 
    public void removeActionListener(ActionListener act_listener) {
        listeners.removeElement(act_listener);
    }
  


    /**
     * Remove all listeners from the listener listt.
     */ 
    public void removeAllActionListeners() {
        listeners.clear();
    }

    //-------------- fires an action event -------------------------
    private void fireActionEvent(String ActionCommand) {
        ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ActionCommand);

        for (int i = 0; i < listeners.size(); i++) {
            ((ActionListener) (listeners.elementAt(i))).actionPerformed(evt);
        }
    }



    /**
     *    Invoked whenever there is an action event on and instance of
     *    a class which is being listened for.  Also, anyone can invoke the
     *    method.  See above the action commands that must be supported
     */
    public void actionPerformed(ActionEvent evt) {}



    /** 
      *  Returns the Virtual Array instantiated by this Array maker
      */
    public IVirtualArray getArray() {
        return tbArray;
    }
    
   public void kill(){

      if( jf != null)
         jf.dispose();
   }

    /**
      *  The IVirutalArray2D class created by this Array Maker
      */
    class TableArray implements IVirtualArray2D, doesColumns {
        DataSet DS;
        Vector Fields;
        public table_view.Gen_TableModel TabModel; 
        DefaultListModel listModel;
        table_view tbView;
        int[] Groups;

        /**
          *  Constructor
          *  @param DS  The data set to be viewed
          *  @param Fields the list of fields to be viewed.  The Group ID field is
          *    always the first field.
          */
        public TableArray(DataSet DS, Vector Fields) {
            this.DS = DS;
            this.Fields =new Vector();
            DataSet[] DSS = new DataSet[1];

            DSS[0] = DS;
            tbView = new table_view(DSS);
      
            listModel = new DefaultListModel();
            for (int i = 0; i < Fields.size(); i++){
                table_view.FieldInfo fieldinf =(tbView.getFieldInfo(DS, 
                             (String) (Fields.elementAt(i))));
                if( fieldinf != null){
                      listModel.addElement( fieldinf);
                      this.Fields.addElement( Fields.elementAt(i));
 
                }else
                   System.out.println("Field not set "+Fields.elementAt(i));
                   
                
            }
            Groups = new int[ DS.getNum_entries()];
            for (int i = 0; i < DS.getNum_entries(); i++) 
                Groups[i] = i;
            TabModel = tbView.getGenTableModel(DS, listModel, "HGT,F", Groups);
        
        }

        //--------------- doesColumns --------------------
        /**
          *  Returns the full name for a column
          *  @param column  the column whose name is desired
          */
        public String getColumnName(int column) {
            //return TabModel.getColumnName( column );
            if ((column < 0) || (column >= Fields.size())) return "Col " + column;
            return (String) (Fields.elementAt(column));

        }

        //------------------ IVirtualArray2D Methods --------------------------
        /**
          *  Returns 2
          */
        public int getDimension() {
            return 2;
        }

        /** 
          *  Not implemented.
          *  @param the Title for this Virtual Array
          */
        public void setTitle(String Title) {}

        /** 
          *  Returns "Data Block Selectro"
          */
        public String getTitle() {
            return "Data Block Selector";
        }

        /**
         * Gets the attributes of the data array within a AxisInfo wrapper.
         * This method will take in an integer to determine which axis
         * info is being returned.
         *
         *  @param  axis Use AxisInfo integer codes.
         *  @return the axis info for the axis specified.
         */
        public AxisInfo getAxisInfo( int axis ) {
            return null;
        }
  
 
       /**
         * Sets the attributes of the data array within a AxisInfo wrapper.
         * This method will take in an integer to determine which axis
         * info is being altered.
         *
         *  @param  axis Use AxisInfo integer codes.
         *  @param  min Minimum value for this axis.
         *  @param  max Maximum value for this axis.
         *  @param  label label associated with the axis.
         *  @param  units units associated with the values for this axis.
         *  @param  scale Is axis linear or logarithmic
         */
        public void setAxisInfo(int axis, float min, float max,
            String label, String units, int scale) {} 
  
        /**
         * Sets the attributes of the data array within a AxisInfo wrapper.
         * This method will take in an integer to determine which axis
         * info is being altered.
         *
         *  @param  axis Use AxisInfo.X_AXIS (0) or AxisInfo.Y_AXIS (1).
         *  @param  info - The axis info object associated with the axis specified.
         */
        public void setAxisInfo(int axis, AxisInfo info) {}
  
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
         * 	     If row, from, or to are invalid, an empty 1-D array is returned.
         */
        public float[] getRowValues(int row, int from, int to) {

            if (from > to)
                return null;
            if ((from < 0) || (to < 0)) return null;
     
            float[] Res = new float[to - from + 1];

            for (int i = from; i <= to; i++)
                try {
                    Res[i - from] = getDataValue(row, i);
                } catch (Exception s) {
                    Res[i - from] = Float.NaN;
                }
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
        public void setRowValues(float[] values, int row, int start) {}
  
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
        public float[] getColumnValues(int column, int from, int to) {

            if (from > to)
                return null;

            if ((from < 0) || (to < 0)) 
                return null;
            float[] Res = new float[to - from + 1];

            for (int i = from; i <= to; i++)
                try {
                    Res[i - from] = getDataValue(i, column);
                } catch (Exception s) {
                    Res[i - from] = Float.NaN;
                }
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
        public void setColumnValues(float[] values, int column, int start) {}
  

        /**
         * Get value for a single array element.
         *
         *  @param  row     row number of element
         *  @param  column  column number of element
         *  @return If element is found, the float value for that element is returned.
         *	     If element is not found, zero is returned.
         */ 
        public float getDataValue(int row, int column) {

            if (row < 0) 
                return Float.NaN;
            if (row >= GroupSort.length) 
                return Float.NaN;
            row = GroupSort[row].intValue();
            try {
                float f =(new Float(TabModel.getValueAt(row, column).toString().trim())).floatValue();
                if( Fields.elementAt(column).equals("Scat Ang")) f = (float)(f*180/Math.PI);
                return f;
            } catch (Exception s) {
                return Float.NaN;
            }
        }
  

        /**
         * Set value for a single array element.
         *
         *  @param  row     row number of element
         *  @param  column  column number of element
         *  @param  value   value that element will be set to
         */
        public void setDataValue(int row, int column, float value) {}
  
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
        public float[][] getRegionValues(int row_start, int row_stop,
            int col_start, int col_stop) {

            if (row_start > row_stop)
                return null;
            if ((row_start < 0) || (row_stop < 0))
                return null;
            float[][] Res = new float[ row_stop - row_start + 1][5];

            for (int r = row_start; r <= row_stop; r++)
                Res[r - row_start] = getRowValues(r, col_start, col_stop);
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
        public void setRegionValues(float[][] values, 
            int row_start,
            int col_start) {}

        		       
        /**
         * Returns number of rows in the array.
         *
         *  @return This returns the number of rows in the array. 
         */ 
        public int getNumRows() {
            return TabModel.getRowCount();
        }


        /**
         * Returns number of columns in the array.
         *
         *  @return This returns the number of columns in the array. 
         */
        public int getNumColumns() {
            return TabModel.getColumnCount();
        }
    
        public void setAllValues(float f) {} 
  
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
         return false; // remove if uncommenting code above
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
       *			If false, use set error values if they exist.
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
       *	  or if setSquareRootErrors() or setErrors is not called,
       *	  Float.NaN is returned.
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

        //--------------------------------- Internal Methods -----------------
        /** 
         * The invoker of this method MUST report a DataChanged event
         */
        public void addField(String Field) {

            if (Field == null) 
                return;
            if (Fields.indexOf(Field) >= 0) 
                return;
            table_view.FieldInfo fieldinf = tbView.getFieldInfo(DS, Field);

            if (fieldinf == null) 
                return;
            listModel.addElement(fieldinf);
            Fields.addElement(Field);
            TabModel = tbView.getGenTableModel(DS, listModel, "HGT,F", Groups);
        }

        public void removeField(int col) {

            if (col < 1) 
                return; 
            if (col >= Fields.size()) 
                return;
            listModel.remove(col);
            Fields.removeElementAt(col);
            TabModel = tbView.getGenTableModel(DS, listModel, "HGT,F", Groups);
        }

    }//class TableArray


    class myWindowAdapter extends WindowAdapter{
       public void windowClosed(WindowEvent e){
          opened = false;
          jf = null;
       }

    }


    boolean opened = false;
    JFrame jf = null;
    class AddActionListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            if( opened)
               return;
            jf = new JFrame("Field Names");
            jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            jf.addWindowListener( new myWindowAdapter());
            opened = true;
            jf.setSize(200, 300);
            //------------------ Set up Field Choices -----------------
            DefaultListModel listmod = new DefaultListModel();
            Data Db = DS.getData_entry(0);

            for (int i = 0; i < Db.getNum_attributes(); i++) {

                Object O = Db.getAttributeValue(i);
                if ((O instanceof Number) )
                    listmod.addElement(Db.getAttribute(i).getName());
            }

            int j = 0;

            for (int i = SpFieldNames.indexOf(";"); i >= 0; 
                                           i = SpFieldNames.indexOf(";", j)) {
                listmod.addElement(SpFieldNames.substring(j, i));
                j = i + 1; 
            }

            JList list = new JList(listmod);

            jf.getContentPane().add(new JScrollPane(list));
            jf.show();
            list.addListSelectionListener(new myListSelectionListener());

        }
    }//AddActionListener


    class DeleteActionListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {

            tbArray.removeField(selectedColumn);
            fireActionEvent("DataChanged");

        }
    }



    class SortActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if( selectedColumn < 0) return;
            if (selectedColumn >= tbArray.getNumColumns()) return ;
            Arrays.sort(GroupSort, new comparre());
            fireActionEvent("DataChanged");
        }
    }


    //--------- Catches Field list selection events----------
    class myListSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
         
            String S = (String) ((JList) (e.getSource())).getSelectedValue();

            tbArray.addField(S.trim());
            fireActionEvent("DataChanged");

        }
    }

    /**
      *  Test program
      */
    public static void main(String args[]) {
        DataSet[] DS = null;

        try {
            DS = Command.ScriptUtil.load("C:/ISAW/SampleRuns/GPPD12358.run");
        } catch (Exception ss) {
            System.out.println("Error reading file - " + ss);
            System.exit(0);
        }
        DataSet Ds = DS[DS.length - 1];
        DataBlockSelector dbSel = new DataBlockSelector(Ds, null);
        LargeJTableViewComponent tab = new LargeJTableViewComponent(null,
                (IVirtualArray2D) dbSel.getArray());
        DataSetViewerMaker1 viewer = new DataSetViewerMaker1(Ds, null, dbSel, tab);
        JFrame jf = new JFrame("Data Block Selector");

        jf.setSize(400, 500);
        jf.getContentPane().add(viewer);
        JMenuBar Men = viewer.getMenuBar();

        jf.setJMenuBar(Men);
        jf.show();

    }//main
    class comparre implements Comparator {
        public int compare(Object o1,
            Object o2) {
            if (selectedColumn < 0) return 0;
            if (selectedColumn >= tbArray.getNumColumns()) return 0;
            if (!(o1 instanceof Integer)) return showError("o1 not INT");;
            if (!(o2 instanceof Integer)) return showError("o2 not INT");
            int row1 = ((Integer) o1).intValue();
            int row2 = ((Integer) o2).intValue();

            if (row1 < 0) return showError("row1 neg");;
            if (row2 < 0) return showError("row2 neg");;
            if (row1 >= GroupSort.length) return showError("row1 too large");
            if (row2 >= GroupSort.length) return showError("row2 too large");;
            float v1, v2;

            try {
                v1 = new Float(tbArray.TabModel.getValueAt(row1, selectedColumn).toString()).floatValue();
            } catch (Exception s) {
                v1 = Float.NaN;
            }
            try {
                v2 = new Float(tbArray.TabModel.getValueAt(row2, selectedColumn).toString()).floatValue();
            } catch (Exception s) {
                v2 = Float.NaN;
            }

            if (v1 == v2) return 0;
            if (Float.isNaN(v1)) return -1;
            if (Float.isNaN(v2)) return 1;

            if (v1 < v2) return -1;
            if (v1 > v2) return 1;
            return 0;
 
        }

        public boolean equals(Object obj) {

            return this.getClass() == obj.getClass();
        }

        public int showError(String message) {
            System.out.println("in Comparator message=" + message);
            return 0;
       }

    }
  class myClearActionListener implements ActionListener{
     public void actionPerformed( ActionEvent evt){
        DS.clearSelections();
        DS.notifyIObservers( IObserver.SELECTION_CHANGED);

     }

  }

  class NewAttrAdd implements ActionListener{
     public void actionPerformed( ActionEvent evt){
        if( evt.getActionCommand().indexOf("Row,Col") >= 0){
           
           for( int i = 0; i< DS.getNum_entries(); i++){

             Object O = (new GetPixelInfo_op( DS, i)).getResult();
             if( O instanceof Vector){
                
                Data db = DS.getData_entry(i);
                Vector R = (Vector)O;
                db.setAttribute( new IntAttribute( "Row", ((Integer)R.elementAt(1)).intValue()));
                db.setAttribute( new IntAttribute( "Col", ((Integer)R.elementAt(0)).intValue()));
                db.setAttribute( new IntAttribute( "Detector", ((Integer)R.elementAt(2)).intValue()));


             } 

           }

        }else{
          String[] slott = {Attribute.SLOT, Attribute.CRATE,Attribute.INPUT};
          
          for( int i = 0; i< DS.getNum_entries(); i++){
            Data db = DS.getData_entry(i);
            for( int j=0;j<3;j++){
              Object O = db.getAttributeValue( slott[j]);
              if( O instanceof int[]){
                 int c = Array.getInt( O,0);
                 db.setAttribute( new IntAttribute( "_"+slott[j],c));
              }


            }
          } 

     }
     }//action performed
  }//NewAttrAdd
}

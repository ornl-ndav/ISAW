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
import java.awt.event.*;
import DataSetTools.components.View.*;
import DataSetTools.components.View.Menu.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import DataSetTools.dataset.*;
import java.awt.*;
/**
  *  This class is the "ArrayMaker" part of a DataSetViewer that can be used
  *  to sort and select Data Blocks from a DataSet.
  */ 
public class DataBlockSelector implements IArrayMaker_DataSet {

    DataSet DS;
    Vector Fields;
    TableArray tbArray;
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
        int row = -1, col = 0;

        if (D != null) {
            for (int i = 0; i < tbArray.getNumRows(); i++) {
                if (tbArray.getDataValue(i, 0) == GroupID)
                    return new SelectedData2D(i, 0);
            }
          
        }
        return new SelectedData2D(-1, -1);
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
        }
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
        
        ViewMenuItem[] view_menu= new ViewMenuItem[1];
        view_menu[0] = item;
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
        String[] Res = new String[1];
        Res[0] = "Select";
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
            this.Fields = Fields;
            DataSet[] DSS = new DataSet[1];

            DSS[0] = DS;
            tbView = new table_view(DSS);
      
            listModel = new DefaultListModel();
            for (int i = 0; i < Fields.size(); i++){
                table_view.FieldInfo fieldinf =(tbView.getFieldInfo(DS, 
                             (String) (Fields.elementAt(i))));
                if( fieldinf != null)
                      listModel.addElement( fieldinf);
                else
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
          *  @param the Title for this Virtual Arrau
          */
        public void setTitle(String Title) {}

        /** 
          *  Returns "Data Block Selectro"
          */
        public String getTitle() {
            return "Data Block Selector";
        }

        /**
         * Returns the attributes of the data array in a AxisInfo2D wrapper.
         * This method will take in a boolean value to determine for which axis
         * info is being retrieved for.    true = X axis, false = Y axis.
         *
         *  @param  isX - Use AxisInfo2D.XAXIS (true) or AxisInfo2D.YAXIS (false). 
         *  @return the axis info for the axis specified.
         */
        public AxisInfo2D getAxisInfoVA(boolean isX) {
            return null;
        }
  
 
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
        public void setAxisInfoVA(boolean isX, float min, float max,
            String label, String units, boolean islinear) {} 
  
        /**
         * Sets the attributes of the data array within a AxisInfo2D wrapper.
         * This method will take in a boolean value to determine for which axis
         * info is being altered.	    true = X axis, false = Y axis.
         * 
         *  @param  isX - Use AxisInfo2D.XAXIS (true) or AxisInfo2D.YAXIS (false).
         *  @param  info - The axis info object associated with the axis specified.
         */
        public void setAxisInfoVA(boolean isX, AxisInfo2D info) {}
  
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

     }

  }
}

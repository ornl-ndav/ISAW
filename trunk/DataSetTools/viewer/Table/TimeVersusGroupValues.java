
/*
 * File:  TimeVersusGroupValues.java
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
 * Modified:
 *
 * $Log$
 * Revision 1.5  2003/11/06 21:26:43  rmikk
 * Added a method for handling selected rows and columns.
 *   Not implemented yet
 *
 * Revision 1.4  2003/11/06 20:05:25  rmikk
 * Eliminated null pointer exceptions when the state was
 *    initially null
 * Eliminated warnings by removing paths from the
 *    ViewMenuItems
 *
 * Revision 1.3  2003/10/28 19:58:13  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.2  2003/10/28 16:20:03  rmikk
 * It now deals with an initial null state correctly
 *
 * Revision 1.1  2003/10/27 15:12:34  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.viewer.Table;
import DataSetTools.dataset.*;
import javax.swing.*;
import DataSetTools.viewer.*;
import DataSetTools.components.View.Menu.*;
import java.util.*;
import java.awt.event.*;
import DataSetTools.components.View.*;
import java.io.*;
import DataSetTools.util.*;



/**
*    This class is the produces array values that show the values for each
*    selected group
*/
public class TimeVersusGroupValues extends DS_XY_TableModel
                    implements IVirtualArray2D, IArrayMaker_DataSet,doesColumns
    {
     DataSet DS;
     String Title;
     JCheckBoxMenuItem jmErr=null; 
     JCheckBoxMenuItem jmInd=null;
     //XScaleChooserUI XScl= null;
     public float[] xvals1;
     public int TimeIndex = -1;
     JCheckBox ShowAll;
     public XScale x_scale= null;
  
     int[] SelectedIndices , AllIndices;
     ViewerState state;
     public TimeVersusGroupValues(DataSet DS, int Groups[], boolean showErrors, 
                                   boolean showInd, ViewerState state)
       {
        super( DS, Groups, showErrors,showErrors);
        this.DS = DS;
        Title = DS.getTitle();
        this.state = state;
        if( state == null)
          this.state = new ViewerState();
        initState();
        //Check state 
        state = this.state;
        if( (Groups == null) || (Groups.length < 1))
          {
           Groups = new int[1];
           setGroups( Groups);
          }
        
     
        AllIndices = new int[ DS.getNum_entries() ];
        for( int i = 0; i < DS.getNum_entries() ; i++ )
            AllIndices[i]=i;
        if( this.state.get_boolean(ViewerState.TIMEVSGROUPTABLE_SHOWALL))
           setGroups( AllIndices);
        setErrInd( this.state.get_boolean(ViewerState.TIMEVSGROUPTABLE_SHOWERR),
              this.state.get_boolean(ViewerState.TIMEVSGROUPTABLE_SHOWIND));
     
       }



  /**
  *    Returns the state variable
  */
  public ViewerState getState()
    {
     return state;
    }




  /**
  *    Initializes state values if not initialized. Also sets up variables and
  *    conditions dictated by the state variables
  */
  public void initState()
    {
     
     if( !state.get_boolean( ViewerState.TIMEVSGROUPTABLE)){   
          state.set_boolean( ViewerState.TIMEVSGROUPTABLE_SHOWALL, false);
          state.set_boolean( ViewerState.TIMEVSGROUPTABLE, true);  
          state.set_boolean( ViewerState.TIMEVSGROUPTABLE_SHOWERR, false);  
          state.set_boolean( ViewerState.TIMEVSGROUPTABLE_SHOWIND, false);
     }
    }
  
  //---------------- IVirtualArray2D Methods----------------


  public int getDimension(){
     return 2;
  }

  /**
   * Returns the attributes of the data array in a AxisInfo2D wrapper.
   * This method will take in a boolean value to determine for which axis
   * info is being retrieved for.    true = X axis, false = Y axis.
   */
  public AxisInfo2D getAxisInfoVA( boolean isX )
   {
     return null;
   }
   

  /**
   * Sets the attributes of the data array within a AxisInfo2D wrapper.
   * This method will take in a boolean value to determine for which axis
   * info is being altered.          true = X axis, false = Y axis.
   */
  public void setAxisInfoVA( boolean isX, float min, float max,
                              String label, String units, boolean islinear )
    {
    }


  /**
   * Sets the attributes of the data array within a AxisInfo2D wrapper.
   * This method will take in a boolean value to determine for which axis
   * info is being altered.          true = X axis, false = Y axis.
   */
  public void setAxisInfoVA( boolean isX, AxisInfo2D info )
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
     try
       {
        return (new Float( getValueAt(row_number, column_number ).toString()))
                .floatValue();
       }
     catch( Exception ss)
       {
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



  private int AdjustRowCol( int row, int maxRows)
    {
     if( row < 0) 
        return 0;
     if( row >= maxRows) 
        return maxRows ;
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
    { System.out.println("first/lastrow,first/lastcol="+first_row+","+last_row+","+
               first_column+","+last_column);
     first_row = AdjustRowCol( first_row, getNumRows());
     last_row = AdjustRowCol( last_row, getNumRows());
     first_column = AdjustRowCol( first_column, getNumColumns());
     last_column = AdjustRowCol( last_column, getNumColumns());
     
    
     if( first_row >= last_row) first_row = last_row;
     if( first_column >= last_column) first_column = last_column;
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
     int V = getColumnCount();
     return getColumnCount() ;
     
    }
      

//---------------- End IVirtualArray2D Methods----------------


//---------------- IVirtualArray Methods----------------



  /**
   * Return controls needed by the component.
   */ 
  public JComponent[] getSharedControls()
    {
    
     JComponent[] Res = new JComponent[ 1];
    
    
     ShowAll = new JCheckBox( "Show All Groups" );
     ShowAll.addActionListener( new MActionListener( ) );
     if( state.get_boolean(ViewerState.TIMEVSGROUPTABLE_SHOWALL))
        ShowAll.setSelected(true);
     else 
        ShowAll.setSelected(false);
     Res[0] = ShowAll; 
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
        jmErr.setSelected( state.get_boolean( ViewerState.TIMEVSGROUPTABLE_SHOWERR));
        jmInd = new JCheckBoxMenuItem("Show Indicies");
        jmInd.addActionListener( new CheckBoxListener());
        jmInd.setSelected( state.get_boolean( ViewerState.TIMEVSGROUPTABLE_SHOWIND));
        Res[0] = new ViewMenuItem(jmErr);
        Res[1] = new ViewMenuItem(jmInd);
        
       }
     //if( JMenuName.equals( "File"))
       {
     
        
        JMenuItem item = new JMenuItem( "Save DataSet to File");
        SaveDataSetActionListener DSActList =new SaveDataSetActionListener( DS);
        item.addActionListener(DSActList);
        Res[2] = new ViewMenuItem("File", item);
        JMenuItem sv= new JMenuItem( "Save Table to a File");
        sv.addActionListener( new SaveFileListener());
        Res[3] =new ViewMenuItem(sv);
        return Res;
       }


     
    }
   
   String[] paths = {"Options","Options","File","File"};
   public String[] getSharedMenuItemPath(){
      return paths;

   }
   public String[] getPrivateMenuItemPath( ){
     return null;
   } 



  /**
   * To be continued...
   */
  public ViewMenuItem[] getPrivateMenuItems()
    {
     return new ViewMenuItem[0];
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


  // Notify all Action Listeners of the given event
  private void notifyActionListeners( String command)
    {
     for( int i = 0; i < Listeners.size(); i++)
       ((ActionListener)Listeners.elementAt(i)).actionPerformed(
           new ActionEvent(this,ActionEvent.ACTION_PERFORMED,command));

    }

 public IVirtualArray getArray(){
    return (IVirtualArray2D)this;
 }

  //--------------- IArrayMake_DataSet Methods
  
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
     int row = getRow(PointedAtGroupIndex,PointedAtTime);
     int col = getCol(PointedAtGroupIndex,PointedAtTime);
     SelectedData2D X= new SelectedData2D(row ,col );
     return X;
    }


 public void SelectRegion( ISelectedRegion region){

    if( region instanceof SelectedRegion2D){
       SelectedRegion2D Region = (SelectedRegion2D) region;
       


    }
  }
  //-------------------------- Event Listeners ---------------------------------
  

 // Class to listen for selections for showing errors and/or indicies
 private class CheckBoxListener  implements ActionListener
    {
     public void actionPerformed( ActionEvent evt)
       {
        setErrInd(jmErr.getState(),jmInd.getState());
        //set state here 
        state.set_boolean( ViewerState.TIMEVSGROUPTABLE_SHOWERR,jmErr.getState());
        state.set_boolean( ViewerState.TIMEVSGROUPTABLE_SHOWIND,jmInd.getState());

        notifyActionListeners( IArrayMaker.DATA_CHANGED);
        notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);

       }
    }//CheckBoxListener


   /** Listens for selection/deselection of the Show All indices checkbox
  */
  class MActionListener implements ActionListener
    {
     
     public void actionPerformed( ActionEvent evt )
       {
        if( ShowAll.isSelected())
           setGroups( AllIndices);
        else if( DS.getNumSelected() > 0)
           setGroups( DS.getSelectedIndices());
        else
          {
           Groups = new int[1];
           Groups[0] = 0;
           setGroups( Groups);
          }
        state.set_boolean( ViewerState.TIMEVSGROUPTABLE_SHOWALL, ShowAll.isSelected());
        
        notifyActionListeners( IArrayMaker.DATA_CHANGED );
         
       }
    }

  class SaveFileListener implements ActionListener{

     public void actionPerformed( ActionEvent evt){
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

     }//actionPerformed
     public void SaveFileInfo( FileOutputStream fout)
       {
        StringBuffer S = new StringBuffer(3000);
        S.append("X\t");
        for( int i = 1; i <getColumnCount(); i++)
           S.append( getColumnName(i) +"\t");
        S.append( "\n");
        try{
        for( int row = 0; row < getRowCount(); row++)
          {
           for( int col = 0; col < getColumnCount() ; col++)
             {
              Object F = getValueAt( row , col );
              S.append( F + "\t" );
             
             }
           S.append("\n");
           if( S.length() > 2400)
             {
              fout.write( S.toString().getBytes());
              S.setLength(0);
             }

          }
        if( S.length() > 0)
          {
           fout.write( S.toString().getBytes());
           S.setLength(0);
          }
        fout.close();
        }
        catch( Exception s)
          {
           SharedData.addmsg("IO Error="+s);
          }
  
     }//SaveFileInfo

  }//SaveFileListener

}

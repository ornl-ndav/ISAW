/*
 * File:  LargeJTableViewComponent.java 
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 *  $Log$
 *  Revision 1.19  2006/07/27 00:32:38  dennis
 *  Moved ExcelAdapter to package ExtTools
 *
 *  Revision 1.18  2005/05/25 19:37:51  dennis
 *  Replaced direct call to .show() method for window,
 *  since .show() is deprecated in java 1.5.
 *  Now calls WindowShower.show() to create a runnable
 *  that is run from the Swing thread and sets the
 *  visibility of the window true.
 *
 *  Revision 1.17  2005/05/13 13:12:05  rmikk
 *  Fixed error with selecting cells in the table.
 *
 *  Revision 1.16  2005/01/10 15:47:32  dennis
 *  Removed unused imports.
 *
 *  Revision 1.15  2004/11/11 19:50:44  millermi
 *  - No longer implements IAxisAddible since AxisOverlay2D is not used.
 *
 *  Revision 1.14  2004/08/23 21:12:16  rmikk
 *  Eliminated a null pointer exception in Instrument Table view
 *
 *  Revision 1.13  2004/08/04 22:18:23  rmikk
 *  Started to implement IAxisAddible
 *  Fixed the ViewMenuItems to include a path
 *  Eliminated some unused code
 *  Implemented the getPointedAt and setPointedAT to work like the ImageViewComponent
 *
 *  Revision 1.12  2004/05/26 18:37:56  rmikk
 *  Removed unused variables
 *
 *  Revision 1.11  2004/05/17 13:52:45  rmikk
 *  Added IViewComponent2D methods
 *  Changed to implement IViewComponent2D
 *
 *  Revision 1.10  2004/05/06 17:34:04  rmikk
 *  
 *  Added an argument to the Selected2D constructor
 *
 *  Revision 1.9  2004/03/19 20:30:04  millermi
 *  - Added special LOG characters so log messages will now be
 *    recorded in the file.
 *
 */

package DataSetTools.viewer.Table;
//import DataSetTools.dataset.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.StringFilter.*;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.TwoD.*;
import gov.anl.ipns.ViewTools.Components.Menu.*;
import gov.anl.ipns.ViewTools.Components.Region.*;
import gov.anl.ipns.ViewTools.UI.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.ViewControl;
import gov.anl.ipns.ViewTools.Components.ViewControls.ViewControlMaker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import DataSetTools.viewer.*;
import javax.swing.table.*;
import java.util.*;
import java.beans.*;
import DataSetTools.components.ParametersGUI.*;
import ExtTools.ExcelAdapter;

/** This class can be used as a Stub for all the special TableViewers in the old
*   format.  It creates a DataSetViewer with one table at the left and one Xconversions
*   table at the right.  The Options Show Errors and Show Indices are implemented along with
*   the connection between the table and the Xconversions operator.  The POINTED_AT message
*   is handled here
*/
public class LargeJTableViewComponent  extends JPanel implements IViewComponent2D,
   DataSetViewerMethods//, IAxisAddible
 {
  IntTableModel table_model;
  IVirtualArray2D Array; 
  protected ViewerState state;
  
  public XJTable jtb;
  
  JScrollPane JscrlPane;
  
  JMenuItem  JMi, 
             JCp ;
  
  ObjectState Ostate = null;
  ExcelAdapter EA;
  SaveDataSetActionListener SaveDS;
  StringEntry StEnt = null; 
  MAction myAction ;
  ComponentInputMap inp_map;
  ActionMap act_map;
  AxisInfo  XAxis, YAxis;
  public LargeJTableViewComponent( ViewerState state1, IVirtualArray2D Array)
    {super( new GridLayout(1,1));
     state = state1;
     if( state == null)
       {state = new ViewerState();
        
        }
     
     initState( state );
     Ostate = new ObjectState();
     Ostate.insert("ViewerState", state);
    
  
   //myAction.addPropertyChangeListener( new TableKeyListener( this));
     this.Array=Array;
     if( Array == null)
        this.Array = new dummyIVirtualArray2D();
     XAxis = this.Array.getAxisInfo( 0);
     YAxis = this.Array.getAxisInfo( 1);
     Array = this.Array;
     table_model = new IntTableModel( this.Array );
     jtb = null;
     SetUpNewJtb();
      
         
    }
  
 // setState() and getState() are required by IPreserveState interface   
 /**
  * This method will set the current state variables of the object to state
  * variables wrapped in the ObjectState passed in.
  *
  *  @param  new_state
  */
  public void setObjectState( ObjectState new_state )
  {
    Ostate = new_state;
    ViewerState st = (ViewerState)new_state.get("ViewerState");
    if( st != null){
       state= st;
  
       
    SetUpNewJtb();
       //Now repaint with these
    
       
    }
  }
 
 /**
  * This method will get the current values of the state variables for this
  * object. These variables will be wrapped in an ObjectState.
  *
  *  @param  isDefault Should selective state be returned, that used to store
  *                    user preferences common from project to project?
  *  @return if true, the default state containing user preferences,
  *          if false, the entire state, suitable for project specific saves.
  */ 
  public ObjectState getObjectState( boolean isDefault )
  {
    if( isDefault){
      ObjectState defState = new ObjectState();
      defState.insert("ViewerState", state);
      return defState;
    }
 
    return Ostate;
  }
 
   private void SetUpNewJtb(){
      removeAll();
      jtb = new XJTable( table_model);
      jtb.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
      jtb.setColumnSelectionAllowed( true );
      jtb.setSelectionBackground( Color.lightGray);
      jtb.removeEditor();
      jtb.addMouseListener( new MyMouseListener());
     
      inp_map = new ComponentInputMap( jtb);
      inp_map.put( KeyStroke.getKeyStroke( KeyEvent.VK_UP,0), "UP");
      inp_map.put( KeyStroke.getKeyStroke( KeyEvent.VK_DOWN,0), "DOWN");
      inp_map.put( KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT,0), "RIGHT");
      inp_map.put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT,0), "LEFT");
      
      act_map = new ActionMap();
      myAction = new MAction("UP");
      act_map.put( "UP",new MAction("UP"));
      act_map.put(  "DOWN",new MAction("DOWN"));
      act_map.put(  "RIGHT",new MAction("RIGHT"));
      act_map.put(  "LEFT",new MAction("LEFT"));
//    jtb.getSelectionModel().addListSelectionListener( new TableKeyListener( this ));
      
       myAction.setEnabled( true);

       jtb.setActionMap( act_map);
       jtb.setInputMap( JComponent.WHEN_FOCUSED, inp_map);

      JscrlPane = new JScrollPane(jtb);
      add( JscrlPane );
      

      EA = new ExcelAdapter( jtb);
       
      setLayout( new GridLayout(1,1));
      Rectangle R = getBounds();
      if( R.width > 0)
         JscrlPane.setSize( R.width-2,R.height-2);
      JscrlPane.validate();
      JscrlPane.repaint();
    
   }
  
   class IntegrateListener implements ActionListener
    {

     private float floatValue( Object O)
      {

       if( O instanceof Number)
         return ((Number)O).floatValue();

       else if( O instanceof String)
        try{
         return ( new Float( (String)O)).floatValue();
           }
        catch( Exception ss){}
       return 0.0f;
       }

     /** "Integrates" the selected cells, removing background (level =
     *   average of bordering cells.  Also, error estimates are given
     */
     public void actionPerformed( ActionEvent evt)
      {
        int[] rows = jtb.getSelectedRows();
        int[] cols = jtb.getSelectedColumns();
        Arrays.sort( rows);
        Arrays.sort( cols) ;
        int nrows = getNlist( rows);
        int ncols = getNlist( cols);
        int firstRow = getFirstinList( rows);
        int firstCol = getFirstinList( cols);
        
        rows= cols = null;
        if( nrows <= 0)
          return;
        if( ncols <= 0)
          return;
        float SumSel = 0.0f, SumBorder = 0.0f;
        int nSel =0, nBord = 0;
        //Selected Area
        float value;

        for( int row = firstRow; row < firstRow + nrows; row++)
          for( int col = firstCol; col < firstCol + ncols; col++)
            {
             value= floatValue(table_model.getValueAt( row, col));
             SumSel += value;
             nSel++;
            }
       //Top and Bottom Borders
        for(int col = firstCol-1; col < firstCol + ncols + 1; col++)
           if( (col >=0) && (col < table_model.getColumnCount()))
             { if( firstRow - 1 >=0)
                 {
                  value= floatValue(table_model.getValueAt( firstRow - 1, col));
                  SumBorder +=value;
                  nBord++;
                  }
               if( firstRow + nrows < table_model.getRowCount())
                 {
                  value = floatValue(table_model.getValueAt( firstRow +nrows, col));
                  SumBorder += value;
                  nBord++;
                  }
              }
         //Right and Left Borders
         for(int  row = firstRow; row < firstRow + nrows; row++)
           {
             if( firstCol -1 >= 0)
              {
                value = floatValue(table_model.getValueAt( row, firstCol - 1));
                SumBorder += value;
                nBord++;
              }   

            if( firstCol + ncols < table_model.getColumnCount() )
              {
               value = floatValue(table_model.getValueAt( row, firstCol +ncols));
               SumBorder += value;
               nBord++;
              }

            }
       
        float Intensity =(SumSel - (nSel * SumBorder/nBord));
        String S="Intensity=" + Intensity +"\n";
        double p_over_b = (0.0 + nSel)/nBord;
        double sigI =java.lang.Math.sqrt( SumSel + p_over_b*p_over_b*SumBorder);
        S+= "(Poisson)Error = " + sigI;
        S+="\n Intensity/error="+(Intensity/sigI);

          
       JOptionPane.showMessageDialog( null, S);

       }//actionPerformed

     //Gets number of consecutive #'s in list starting from first in list
    //Does NOT assume list is increasing
     private int getNlist( int[] list)
       {
        if( list == null) 
           return 0;
        if( list.length <1)
           return 0;
        int F = getFirstinList( list);
        if( F < 0) 
           return 0;
        int Res = 1;
        boolean done =  Res >= list.length;
        
        while(!done)
          if( list[ Res ] == F + Res)
             {
              Res++;
              done = Res >= list.length;
              
              }
          else
             done = true;
              
        return Res;
       }

      private int getFirstinList( int[] list)
       {
         if( list== null)
           return -1;
         if( list.length < 1)
           return -1;
         return list[0];
       }


     }//class IntegrateListener

  


  /** Sets the data set, table model, Xconversion table,and the JTable,and validates
  */
  //Eliminated.  The DataSetVieweMaker will Create a whole new ViewComponent
  //   if the DataSet is changed
 /* public void setDataSet( DataSet ds)
   { this.ds = ds;
    
     super.setDataSet( ds);
     SaveDS.setDataSet( ds);
     boolean serr,sind;
     serr = jmErr.getState();
     sind = jmInd.getState(); 
     table_model = fixTableModel( state, table_model, serr,sind);
     
     InfoTableHolder.remove( InfoTable.getTable());
     InfoTable = new DataSetXConversionsTable (ds );
     InfoTableHolder.add(InfoTable.getTable());
     jtb.setModel( table_model);
     validate();
   }
 */

 /** Initializes the state variables for the show error/index options<P>
  * Subclasses can add extra  state information and initializations before or after this.<P>
  * NOTE: All variables in a subclass should be initialized here.
  */
  public void initState( ViewerState state)
    {
    
     }



   

   /** LargeJTableViewComponent Takes care of the POINTED_AT_CHANGED reason and locates the
   *   Scroll pane to the correct position.  Subclasses should take care of 
   *   all other reasons and also call this method if the POINTED_AT groups
   *   and channel change
   */
  public void setPointedAt( ISelectedData Info)
     {if( Info instanceof SelectedData2D )
      { 
        SelectedData2D Info2D = (SelectedData2D)Info;
        int row = Info2D.getRow();
        int col = Info2D.getCol();
        if( row <  0)
          return;
        if( col < 0)
          return;
        if( row == jtb.getSelectedRow())
          if( col == jtb.getSelectedColumn())
            return;
        if( row >= jtb.getRowCount())
           return;
        if( col >= jtb.getColumnCount())
           return;

        Rectangle R = jtb.getCellRect( row,col,false);
        Rectangle Rscr = JscrlPane.getViewport().getViewRect();//getViewRect(); NG
        int width = Rscr.width;
        int height = Rscr.height;
      
        Rectangle RR = R;
        RR.y = R.y - height/2;
        RR.x = R.x -width/2;
        Rectangle RTL = jtb.getCellRect( 0, 0, false);
        Rectangle RBR = jtb.getCellRect( jtb.getRowCount() -1,jtb.getColumnCount()-1, false);
        
        if( RR.x < RTL.x)
           RR.x = RTL.x;
        else if( RR.x > RBR.x)
           RR.x = RBR.x;


        if( RR.y < RTL.y)
           RR.y = RTL.y;
        else if( RR.y > RBR.y)
           RR.y = RBR.y;

/*        if( col> jtb.getColumnCount() -ncols/2)
           RR.x = jtb.getCellRect(row,java.lang.Math.min(0,col-ncols/2) ,false).x;
        else if( col-ncols/2 <=0)
           RR.x = jtb.getCellRect( row, 0,false).x;
        if( row >= jtb.getRowCount() -nrows/2)//means there is a blank below. min # rows
           RR.y = jtb.getCellRect(java.lang.Math.min(0,row-nrows/2) , col,false).y;
       
        else if( row -nrows/2 < 0)
           RR.y =jtb.getCellRect( 0,col,false).y;
*/

        JscrlPane.getViewport().setViewPosition( new Point( RR.x, RR.y));
        
        jtb.setColumnSelectionInterval( col,col);
        jtb.setRowSelectionInterval(row,row);
        
      }
     
     }


   public ViewerState getState()
     {return state;
     }
//-------------IViewComponent2D Methods -----------
	public void setPointedAt( floatPoint2D fpt ){
              
           if( Array.getNumColumns() <=0)
              return ;
           if( Array.getNumRows() <=0)
               return ;
           int col = (int)(Array.getNumColumns()*(fpt.x - XAxis.getMin())/(XAxis.getMax()-XAxis.getMin()));
           
           int row = (int)(Array.getNumRows()*(fpt.y - YAxis.getMax())/(YAxis.getMin()-YAxis.getMax()));
           setPointedAt( new SelectedData2D( row, col, 0f));


             
	}
 
	 /**
		* This method is a notification to the view component that the selected
		* point has changed.
		*
		*  @return The current point as a floatPoint2D.
		*/ 
		public floatPoint2D getPointedAt(){
                    if( Array.getNumColumns() <=0)
                       return null;
                    if( Array.getNumRows() <=0)
                       return null;
	            SelectedData2D dat = (SelectedData2D)IgetPointedAt();
                    int row = dat.getRow();
                    int col = dat.getCol();
                    float x =  XAxis.getMin()+(XAxis.getMax()-XAxis.getMin())*(col+.5f)/Array.getNumColumns();
                    float y =  YAxis.getMax()+(YAxis.getMin()-YAxis.getMax())*(row+.5f)/Array.getNumRows();
                    return new floatPoint2D( x,y);



		}

	 /**
		* Given an array of points, a selection overlay can be created.
		*
		*  @param  rgn - array of regions
		*/ 
		public void setSelectedRegions( Region[] rgn ){
		}
 
	 /**
		* Retrieve array of regions generated by the selection overlay.
		*
		*  @return The selected regions.
		*/
		public Region[] getSelectedRegions(){
		return null;
		}
  
	 /**
		* This method is invoked to notify the view component when the IVirtualArray
		* of data has changed. 
		*
		*  @param  v2D - virtual array of data
		*/ 
		public void dataChanged(IVirtualArray2D v2D){
			setData( v2D);
                        XAxis = v2D.getAxisInfo(0);
                        YAxis = v2D.getAxisInfo(1);
		}
		
//-------------END IViewComponent2D Methods---------------

  /** 
  *  Listener for events on the show error and show indices Option menu item buttons
  */
    
 /* private class CheckBoxListener  implements ActionListener
    {
     public void actionPerformed( ActionEvent evt)
       { 
        table_model=fixTableModel( state, table_model, jmErr.getState(),jmInd.getState());
        jtb.setModel(table_model );
        jtb.invalidate();
        //setPointedAt( ISelectedData Info)
        
       }
    }
*/

   /**
   * Action Listener for the MenuItems to Select all and copy select
   * in the JFrame containing the JTable
   */
   private class MyJTableListener implements ActionListener
     {
      JTable JTb;
      ExcelAdapter EA;
      IntTableModel DTM;
      LargeJTableViewComponent viewComp;
      public MyJTableListener( JTable JTb, IntTableModel DTM, ExcelAdapter EA,
              LargeJTableViewComponent viewComp)
        {
         this.JTb = JTb;
         this.DTM = DTM;
         this.EA = EA;
         this.viewComp = viewComp;
        }


      public void actionPerformed( ActionEvent e )
        {
         JMenuItem targ = ( JMenuItem )e.getSource();

         if( targ.equals( JMi ) )
           {viewComp.jtb.selectAll();
           // JTb.setRowSelectionInterval( 0, DTM.getRowCount() - 1 );
            //JTb.setColumnSelectionInterval( 0, DTM.getColumnCount() - 1 );
           }
         else if( targ.equals( JCp ) )
           {
            viewComp.EA.actionPerformed( new ActionEvent( 
                         viewComp.jtb, 0, "Copy" ) );
           }
        }
     }

   /** 
   *    Listener for mouse events in the table. These events cause POINTED_AT_CHANGED events
   */
   class MyMouseListener  extends MouseAdapter
     {
      public void mouseClicked(MouseEvent e)
        { 
          //int row = jtb.getSelectedRow();
          //int col = jtb.getSelectedColumn();
          notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);
          if( jtb.getInputMap() ==  inp_map){}
          else{ 
            jtb.setInputMap( JComponent.WHEN_FOCUSED, inp_map);
            jtb.setActionMap( act_map);
          }
          
         }
      }
  /** Selects the groups that were highlighted
  */
  class SelectActionListener implements ActionListener
    {
     public void actionPerformed( ActionEvent evt)
      {
       int[] cols = jtb.getSelectedColumns();
       int[] rows = jtb.getSelectedRows();

       if( (cols == null) || ( rows == null))
         return;
       if( (cols.length < 1) ||(rows.length < 1))
         return;
       notifyActionListeners( IViewComponent.SELECTED_CHANGED);

     }
    }//class SelectActionListener

  //------------------ IViewComponent Methods ----------------
  /**
    *  Returns the list of Shared ViewMenuItems. Currently items to Select All,
    *  Copy Selected, Integrate Selected regions, and Select menu items are
    *  returned.  The Select menu item should set the corresponding data sets and
    *  and times as selected.
    */
   public ViewMenuItem[] getMenuItems(){
       ViewMenuItem[] Res;
       
      
     
     //if(  JMenuName.equals( "Edit"))
     {
      Res = new ViewMenuItem[4];
      //JMenu jSprSht= new JMenu( "SpreadSheet");
     
      JMi = new JMenuItem( "Select All" );
     
      JCp = new JMenuItem( "Copy Sel" );
      JMenuItem calc= new JMenuItem( "Integrate");
      
      calc.addActionListener( new IntegrateListener() );
      JMi.addActionListener( new MyJTableListener( jtb, table_model, EA ,this) );
      JCp.addActionListener( new MyJTableListener( jtb, table_model, EA ,this) );
      Res[0] = new ViewMenuItem("Edit.SpreadSheet",JMi);
      Res[1] = new ViewMenuItem("Edit.SpreadSheet",JCp);
      Res[2] = new ViewMenuItem("Edit.SpreadSheet",calc);
      JMi.setToolTipText("Select All entries in the table");
      JCp.setToolTipText("Copy the selected items in the table to the Clipboard");
      calc.setToolTipText("Integrate selected region minus background"+
            "(the boundary cells)"); 
     
     }
     // if(  JMenuName.equals(  "Select"))
       {
     
        JMenuItem Sel1 = new JMenuItem( "Rectangle");
        Sel1.addActionListener( new SelectActionListener());
        Sel1.setToolTipText("Selects in application items highlighted in table");
        Res[3] = new ViewMenuItem("Select", Sel1);
        return Res;
      }
   
   }

   String[] paths ={"Edit.SpreadSheet","Edit.SpreadSheet","Edit.SpreadSheet",
                    "Select"};
   public String[] getSharedMenuItemPath(){
     return paths;
   }
   public String[] getPrivateMenuItemPath( ){
     return null;
   }

   /** 
   *    Test program for this module. It requires one argument, the filename of a data set
   *    to display as a Time_Slice_TableModel
   */
   public static void main( String args[])
     {if( args == null)
        System.exit(0);
      if( args.length<1)
        System.exit(0);
      //DataSet[] DS = (new IsawGUI.Util()).loadRunfile( args[0]);
      //Time_Slice_TableModel tbm = new Time_Slice_TableModel(DS[1],4317.0f,false,false);
      JFrame jf = new JFrame("Test");
      //LargeJTableViewComponent stab = new LargeJTableViewComponent( DS[1], null,tbm);
      //jf.getContentPane().add( stab);
      jf.setSize( 400,400);
      WindowShower.show(jf);

     }// main
//----------------------- IViewComp Methods ----------------------------
 public void setData( IVirtualArray Data) throws IllegalArgumentException{
     if( !(Data instanceof IVirtualArray2D))
         throw new IllegalArgumentException("Improper Data For this Table viewer");
     table_model =  new IntTableModel( (IVirtualArray2D)Data);
     Array =(IVirtualArray2D)Data;
     SetUpNewJtb();
    /* jtb.setModel( table_model);
     jtb.revalidate();
     repaint();
    */
   }

   /**
   * Retrieve the jpanel that this component constructs. 
   */
   public JPanel getDisplayPanel(){
     return this;
   }
  
  /**
   * Return controls needed by the component. Currently only a Format
   * Control is returned.
   */ 
   public ViewControl[] getControls(){
     ViewControl[] Res = new ViewControl[1];
     
     StEnt = new StringEntry("",6, new FormatFilter());
     StEnt.addActionListener( new FormatActionListener());
     JPanel JP = new JPanel( new GridLayout( 1,2));
     JP.add( new JLabel("Format"));
     JP.add( StEnt);
     StEnt.setToolTipText("Fortran Formats like F5.3,I4,E8.2");
     Res[0] = new ViewControlMaker(JP);
     return Res;
     
   }
   
  public void kill(){
  }

  //--------------------- Communication Methods ----------------

   /**
   * To be continued...
   */ 
   public void dataChanged(){
       
       //int V =Array.getNumColumns(); 
       if( 
           (Array.getNumColumns() != jtb.getColumnCount()) ||
           (Array.getNumRows() != jtb.getRowCount()) )
              SetUpNewJtb(); 
       
       jtb.validate();
       jtb.repaint();
   }
   Vector Listeners = new Vector();
  /**
   * Add a listener to this view component. A listener will be notified
   * when a selected point or region changes on the view component.
   * The action command for these events are given in the public static 
   *  variables at the top.  All other events will be sent to the
   *  IVirtualArray
   */
   public void addActionListener( ActionListener act_listener ){
     Listeners.addElement( act_listener);
   }
   
  /**
   * Remove a specified listener from this view component.
   */ 
   public void removeActionListener( ActionListener act_listener ){
     Listeners.removeElement( act_listener);
   }
  /**
   * Remove all listeners from this view component.
   */ 
   public void removeAllActionListeners(){
     Listeners.removeAllElements();
   }

   private void notifyActionListeners( String command){
    for( int i = 0; i < Listeners.size(); i++)
      ((ActionListener)Listeners.elementAt(i)).actionPerformed(
          new ActionEvent(this,ActionEvent.ACTION_PERFORMED,command));

   }

   public ISelectedRegion IgetSelectedRegion(){
     
     SelectedRegion2D reg = new SelectedRegion2D(  jtb.getSelectedRows(),
                  jtb.getSelectedColumns()); 
     return reg;
   }
   public ISelectedData IgetPointedAt(){
     return new SelectedData2D( jtb.getSelectedRow(), jtb.getSelectedColumn(),
          Float.NaN);
   }
//--------------- End IViewComponent Methods ------------------------------
//----------------- Start IAxisAddible Methods -----------------------------
  public AxisInfo getAxisInformation( int axiscode){
     return Array.getAxisInfo( axiscode);
  }

  public String getTitle(){
     return Array.getTitle();
  }

  public int getPrecision(){
     return 0;
  } 

  public java.awt.Font getFont(){

     return  FontUtil.MONO_FONT;
  } 

  public Rectangle getRegionInfo(){
    return getBounds();
  }
//----------------- End IAxisAddible Methods -----------------------------
 class IntTableModel  extends AbstractTableModel {//implements ITableModel NO
   IVirtualArray2D Array;
   public IntTableModel( IVirtualArray2D Array){
     this.Array = Array;
   }

  public void setNewArray( IVirtualArray2D Array){
    this.Array = Array;
  }
  public int getRowCount(){
    return Array.getNumRows();
   }
  public int getColumnCount(){
    return Array.getNumColumns();
   }
  public String getColumnName(int columnIndex){

   if( Array instanceof doesColumns)
      return ((doesColumns)Array).getColumnName( columnIndex );
   else
      return "Col"+columnIndex;
  }
  public Object getValueAt(int row, int column){
    try{
      Float F = new Float( Array.getDataValue( row, column));
      if( F.isNaN())
         return "";
      if( Format == null)
        return F;
      return Formatt( F , Format.charAt(0), FormatFilter.getWidth(Format),
                           FormatFilter.getDecimal( Format) );
    }catch( Exception s){
      return null;
    }
   }

  String Format = null; 
  public void setFormat( String FortranFormatSpecifier){
    this.Format = FortranFormatSpecifier;
    
  }
 }//IntTableModel

  public String Formatt( Float F, char mode, int width, int decimal){
      if( "EeIiFf".indexOf( mode) < 0)
         return "";
      mode = Character.toUpperCase( mode);
      double d = F.doubleValue();
      if( mode == 'I')
        return Format.integer( d, width);
      else if( mode == 'F')
        return ShowReal( d, width, decimal);
      else
        return Format.doubleExp( d, width);

  }
  String ShowReal( double d, int width, int decimal){
    char[] pattern = new char[width+1];
    if( decimal > width) decimal = width;
    Arrays.fill( pattern,0, width-decimal, '#'); 
    pattern[width-decimal]= '.';
    if( width-decimal >0)
      pattern[width-decimal-1]='0';
    Arrays.fill( pattern, width-decimal+1,pattern.length,'0');
    java.text.DecimalFormat df = new java.text.DecimalFormat( new String( pattern));
    
    String X= df.format(d,new StringBuffer(0),new java.text.FieldPosition(0)).toString();
   
    return Format.string(X, width,true);

  }
  private class FormatActionListener implements ActionListener{
     public void actionPerformed(ActionEvent evt){
       if( StEnt != null)
          table_model.setFormat( StEnt.getText());
      
     }

  }
  
  private class TableKeyListener implements PropertyChangeListener{
   LargeJTableViewComponent comp;
   public TableKeyListener( LargeJTableViewComponent comp){
     this.comp = comp;
   }
   public void propertyChange(PropertyChangeEvent evt){

     notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);
     /*int code = e.getKeyCode();
     if( code == KeyEvent.VK_RIGHT)
          notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);
     else if( code == KeyEvent.VK_KP_RIGHT)
          notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);
     else if( code == KeyEvent.VK_LEFT)
          notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);
     else if( code == KeyEvent.VK_KP_LEFT)
          notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);
     else if( code == KeyEvent.VK_DOWN)
          notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);
     else if( code == KeyEvent.VK_KP_DOWN)
          notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);
     else if( code == KeyEvent.VK_UP)
          notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);
     else if( code == KeyEvent.VK_KP_UP)
          notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);
    */
   }
  }//TableKeyListener

  private class MAction extends AbstractAction{
    String S;
    public MAction( String S){
      super();
      this.S = S;
    }
 
    public void actionPerformed( ActionEvent evt){
       int row = jtb.getSelectedRow();
       int col = jtb.getSelectedColumn();
       if( S.equals("UP")){
          if( row > 0)
             jtb.changeSelection(row-1,col,false,false);
       }
       else if( S.equals("DOWN")){
          if( row + 1 < jtb.getRowCount())
             jtb.changeSelection(row+1,col,false,false);
       }
       else if( S.equals("LEFT")){
          if( col > 0)
           jtb.changeSelection(row,col-1,false,false);
       }
       else if( S.equals("RIGHT")){
          if( col +1 < jtb.getColumnCount())
           jtb.changeSelection(row,col+1,false,false);
           }
       else
          return;
      notifyActionListeners( IViewComponent.POINTED_AT_CHANGED);
           
    }//actionPerforme
    }//MAction
  
   }


/*
 * File:  TimeSliceView.java 
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
 * $Log$
 * Revision 1.6  2002/11/27 23:25:37  pfpeterson
 * standardized header
 *
 * Revision 1.5  2002/10/07 14:48:45  rmikk
 * Sets the pointed at Time internally to the dataset's
 *   PointedAtX at several places, so it is in line with
 *   the other views
 *
 * Revision 1.4  2002/08/21 15:49:14  rmikk
 * -If pointedAtX is NaN(undefined) defaults to 0th frame
 *  -Initialized with pointed at Time and group
 *
 * Revision 1.3  2002/07/26 22:04:10  rmikk
 * Place more information in the state variable and got it
 *   working when returning to this view.
 * Added an XScaleChooser control to the control panel
 *
 * Revision 1.2  2002/07/25 21:01:12  rmikk
 *  The times now reflect if the Data is Histogram or Function
 *    Data.
 *
 * Revision 1.1  2002/07/24 20:04:26  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.viewer.Table;


import javax.swing.*;
import javax.swing.table.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.components.ui.*;
import java.awt.*;
import java.awt.event.*;
import DataSetTools.util.*;


/** The DataSetViewer that gives the TimeSlice view( vs row and column) of data
*   in table form.  The super class STableView constructs the DataSetView with a table,
*   conversions table, Menubar items for Showing errors and/or indices, and Saving the table.
*/
public class TimeSliceView  extends STableView
 {public AnimationController acontrol ;
  XScaleChooserUI XScl;
  public float[] xvals1;
  public int TimeIndex;
  public JPanel JRowColPanel;
  public XScale x_scale;
  /** Standard constructor for DataSetViewers.<P>
  * NOTE: The data should come from an area detector
  */
  public TimeSliceView( DataSet DS, ViewerState state1)
   {super( DS,state1, new Time_Slice_TableModel(DS, 0.0f, false, false));
    TimeIndex = state.get_int("TableTS_TimeInd");
    
     
     if(xvals1 == null)
        {
         xvals1 = calcXvals();
         }
     ((Time_Slice_TableModel)table_model).setTime( xvals1[TimeIndex]);
     jtb.invalidate();
     super.redraw( IObserver.POINTED_AT_CHANGED );    
   }

   /** Sets the xvals- the times for the slicing
   */
   public void setXvals(float[] Xvals)
    { this.xvals1 = Xvals;
     
    }

   /** Called by the super class to initialize new variables in this module
   */
   public void initState( ViewerState st)
    {super.initState(state);
     if(!( state.get_String( ViewerState.TABLE_TS).equals("")))
       {
        return;
       }
     String S =state.get_String( ViewerState.TABLE_TS);
    
     state.set_String( ViewerState.TABLE_TS, "OK");
    
     DataSet ds = getDataSet();
     state.set_int( "TableTS_MinRow" , 1);
     state.set_int( "TableTS_MinCol" , 1);
     state.set_int( "TableTS_MaxRow" , table_model.getRowCount());
     state.set_int( "TableTS_MaxCol" , table_model.getColumnCount());
     x_scale = null;
     if( xvals1 == null)
       { setXvals( calcXvals());
        
        }
    state.set_int( "TableTS_TimeInd" , getPointedAtXindex( ));
    state.set_float("TABLE_TS_MIN_TIME", xvals1[0]);
    state.set_float("TABLE_TS_MAX_TIME" ,xvals1[ xvals1.length - 1 ]);
    state.set_int("TABLE_TS_NXSTEPS" , 0);
     
     
     }
  
   //Will use data block 0 for XRange unless TableTS_nbins > 0
   // then a uniform XScale with nbins will be used

   private float[] calcXvals()
    {int[] u = getDataSet().getSelectedIndices();
    boolean useAll=false;
    if( u== null)
      useAll = true;
    else if( u.length < 1)
      useAll = true;
    int a = 0;
    if( !useAll )
      a = u[0];
    float[] new_xvals;
   
    Data  D = getDataSet().getData_entry(a);

    if( x_scale != null)
       new_xvals = x_scale.getXs();
    else
      {
       new_xvals = D.getX_values(); 
      }
    
      //table_view.MergeXvals( a,getDataSet(), null, useAll, getDataSet().getSelectedIndices());
    if( D instanceof FunctionTable)
      return new_xvals;
    if( D instanceof FunctionModel)
      return new_xvals;
     
    float[] Hxvals = new float[ new_xvals.length -1];
    for( int i = 0; i< Hxvals.length; i++)
       Hxvals[i] = (new_xvals[i]+new_xvals[i+1])/2.0f;
    return Hxvals;
    
    
    }
  public ViewerState getState()
    { 
      return state;
    }


   /** Called by super's constructor or super.initAftMenuItems to fix up the TableViewModel 
   */
   public TableViewModel fixTableModel( ViewerState state , TableViewModel table_model, 
                                  boolean showerrors,boolean showIndices)
    {  
        if( getDataSet() == null)
           DataSetTools.util.SharedData.addmsg("DataSet is null in fix table");
       int i =getPointedAtXindex( );//state.get_int("TableTS_TimeInd");
       state.set_int( "TableTS_TimeInd" ,i );
       if( i<0)
          i=0;
       
       //if( i >= xvals1.length)
       //   i= xvals1.length -1;
       Time_Slice_TableModel ttt= new Time_Slice_TableModel(getDataSet(),
                       xvals1[i], 
                       showerrors, showIndices);
       ttt.setRowRange( state.get_int( "TableTS_MinRow"), state.get_int("TableTS_MaxRow"));
       ttt.setColRange( state.get_int( "TableTS_MinCol"), state.get_int("TableTS_MaxCol"));
       
      
       return ttt;
    }

   /** Called by super's constructor or super.initAftMenuItems to addComponent above 
    * the Xconversions panel
   */
   public void AddComponentsAboveInfo( JPanel EastPanel)
    {
    if( JRowColPanel == null)
        JRowColPanel = new JPanel( new GridLayout(2,1));
    else
        JRowColPanel.removeAll();
        TextRangeUI  tr=  new  TextRangeUI ("Row Range",
                                          0.0f+state.get_int("TableTS_MinRow"),
                                          0.0f+state.get_int("TableTS_MaxRow"));
                        
     JRowColPanel.add(tr);
     tr.addActionListener( new MyRangeActionListener(1, this));
     tr= new TextRangeUI ("Col Range", 0.0f+state.get_int("TableTS_MinCol"),
                                       0.0f+state.get_int("TableTS_MaxCol"));
                        
     JRowColPanel.add( tr);
     tr.addActionListener( new MyRangeActionListener(2, this));
     JRowColPanel.setBorder( BorderFactory.createTitledBorder(
                                BorderFactory.createLoweredBevelBorder() ,"Ranges") );
     EastPanel.add( JRowColPanel);
  // XSCale Chooser
     XScl= new XScaleChooserUI("XScale", getDataSet().getX_units(),
                             state.get_float("TABLE_TS_MIN_TIME"),
                             state.get_float("TABLE_TS_MAX_TIME"),
                             state.get_int("TABLE_TS_NXSTEPS"));
     
     EastPanel.add( XScl);
     x_scale = XScl.getXScale();
     XScl.addActionListener( new MyXScaleActionListener());

  //Animation Controller
     acontrol = new AnimationController();
     
       setXvals(calcXvals());
   
     acontrol.setFrame_values( xvals1);
     acontrol.setBorderTitle("X vals");
     acontrol.setTextLabel(" X("+getDataSet().getX_units()+")");
     acontrol.setFrameValue( getDataSet().getPointedAtX());
     acontrol.addActionListener( new MyAnimationListener());
    
     EastPanel.add( acontrol);
    
    }

   /** Sets a new Data Set for this viewer
   */
   public void setDataSet( DataSet ds)
     {super.setDataSet( ds);
      /*if( JRowColPanel != null)
          {JRowColPanel.removeAll();
           setXvals(calcXvals());
           AddComponentsAboveInfo( jEast);
          }
       
      */ setXvals(calcXvals());
         removeAll();
         initAftMenuItems();
         validate();
      
      
      }
     
     // converts time in getPointedAtX to index in time slice array
     private int getPointedAtXindex()
     {float X = getDataSet().getPointedAtX();
      if( Float.isNaN( X))
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
  /** Redraws the appropriate parts of the view in response to changes
  */
  public void redraw( String reason)
    {
     if( reason == IObserver.POINTED_AT_CHANGED )
       { 
        float x = getDataSet().getPointedAtX();
        int indexX = getPointedAtXindex();
        int index =getDataSet().getPointedAtIndex();
        if( acontrol.getFrameNumber() != indexX)
          {state.set_int("TableTS_TimeInd", indexX);
           acontrol.setFrameNumber( indexX );
          }
        
       }
     super.redraw(reason);
    }

   /** Test program for this module.  It needsone argument, the filename
   */
   public static void main( String args[])
    {
      if( args == null)
        System.exit(0);
      if( args.length<1)
        System.exit(0);
      DataSet[] DS = (new IsawGUI.Util()).loadRunfile( args[0]);
      Time_Slice_TableModel tbm = new Time_Slice_TableModel(DS[1],4317.0f,false,false);
  
      for( int i=0; i<DS[1].getNum_entries(); i++)
         DS[1].setSelectFlag(i,true);
      new ViewManager( DS[1], "x,Row vs Col y");

    }
  class MyRangeActionListener implements ActionListener
    {int ID;
     TimeSliceView tv;
     public MyRangeActionListener( int id, TimeSliceView tv)
       {ID=id;
        this.tv = tv;
       }
     public void actionPerformed( ActionEvent evt)
       {TextRangeUI tri = (TextRangeUI)(evt.getSource());
        int min,max;
         min = (int)( tri.getMin());
         max = (int)( tri.getMax());
        
        int n2 = table_model.getColumnCount();
        if(ID==1)
          { ((Time_Slice_TableModel)table_model).setRowRange( min,max);
            state.set_int( "TableTS_MinRow", min);
            state.set_int("TableTS_MaxRow", max);
           }
        else
           {((Time_Slice_TableModel)table_model).setColRange( min,max);
               state.set_int("TableTS_MinCol", min);
            state.set_int("TableTS_MaxCol", max);
            }
         
         tv.removeAll();
         initAftMenuItems();
         tv.validate();
        /*int n1 = table_model.getColumnCount();
        TableColumnModel tcm= jtb.getColumnModel();
        for( int i = n2; i< n1;i--)
          { TableColumn tc = tcm.getColumn( i);
            jtb.removeColumn( tc);
            tcm= jtb.getColumnModel();
           }
        jtb.setModel( table_model );
        jtb.invalidate();
        
       
             
        */
        /*TableHolder.removeAll();
        
        TableHolder.add( new JScrollPane( jtb));
        
        jtb.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
        jtb.setColumnSelectionAllowed( true );
        
        jtb.removeEditor();
        
        //TableHolder.invalidate();
        
        //jtb.repaint();
        tv.invalidate();
       */
        
         
         
        }
     }
 class MyAnimationListener implements ActionListener
   {
     public void actionPerformed( ActionEvent evt)
       { 
         if( state == null)
           return;
         if( acontrol == null)
            return;
         //only notify if different
         int FrameNumber = state.get_int("TableTS_TimeInd");
        
        
           
         state.set_int( "TableTS_TimeInd", acontrol.getFrameNumber());
         
         ((Time_Slice_TableModel)table_model).setTime( xvals1[acontrol.getFrameNumber()]);
        
         jtb.validate();
         jtb.repaint();
         if( acontrol.getFrameNumber() != FrameNumber) 
           {getDataSet().setPointedAtX(xvals1[acontrol.getFrameNumber()]);
            getDataSet().notifyIObservers( IObserver.POINTED_AT_CHANGED );
           }
        
       }

    }
 public class MyXScaleActionListener implements ActionListener
   {

    public void actionPerformed( ActionEvent evt)
      { x_scale= XScl.getXScale();
       if(x_scale == null)
         x_scale = getDataSet().getData_entry(0).getX_scale();
       ((Time_Slice_TableModel)table_model).setXScale(x_scale );
       
       
       //float[] xx = x_scale.getXs();
       xvals1 = calcXvals();
       
       acontrol.setFrame_values( xvals1);
       float X = getDataSet().getPointedAtX();
       if( Float.isNaN(X))
         acontrol.setFrameNumber( 0);
       else
          acontrol.setFrameValue(  X );
       state.set_int( "TableTS_TimeInd" , getPointedAtXindex( ));
       state.set_float("TABLE_TS_MIN_TIME", xvals1[0]);
       state.set_float("TABLE_TS_MAX_TIME" ,xvals1[ xvals1.length - 1 ]);
       state.set_int("TABLE_TS_NXSTEPS" , x_scale.getNum_x());
       
       }
    }
  }

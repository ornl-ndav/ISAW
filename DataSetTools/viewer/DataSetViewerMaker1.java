/*
 * File: DataSetViewerMaker1.java
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
 *  $Log$
 *  Revision 1.4  2003/12/04 20:45:29  rmikk
 *  Added some input checking
 *
 *  Revision 1.3  2003/11/06 21:27:26  rmikk
 *  Now can handle selected Regions for specifying
 *    data sets and time ranges to be selected
 *
 *  Revision 1.2  2003/11/06 19:56:18  rmikk
 *  Changed the proportion on the split pane so the control
 *    panel takes less area
 *
 *  Revision 1.1  2003/10/27 15:09:43  rmikk
 *
 *  Initial Checkin
 *
 *  Revision 1.4  2003/08/08 15:48:24  dennis
 *  Added GPL copyright information and $Log$
 *  Added GPL copyright information and Revision 1.4  2003/12/04 20:45:29  rmikk
 *  Added GPL copyright information and Added some input checking
 *  Added GPL copyright information and
 *  Added GPL copyright information and Revision 1.3  2003/11/06 21:27:26  rmikk
 *  Added GPL copyright information and Now can handle selected Regions for specifying
 *  Added GPL copyright information and   data sets and time ranges to be selected
 *  Added GPL copyright information and
 *  Added GPL copyright information and Revision 1.2  2003/11/06 19:56:18  rmikk
 *  Added GPL copyright information and Changed the proportion on the split pane so the control
 *  Added GPL copyright information and   panel takes less area
 *  Added GPL copyright information and
 *  Added GPL copyright information and Revision 1.1  2003/10/27 15:09:43  rmikk
 *  Added GPL copyright information and
 *  Added GPL copyright information and Initial Checkin
 *  Added GPL copyright information and to record CVS
 *  login messages.
 *
 */

package DataSetTools.viewer;

import javax.swing.*;
import DataSetTools.dataset.*;
import DataSetTools.components.View.*;
import DataSetTools.components.View.Menu.*;
import DataSetTools.components.View.OneD.*;
import DataSetTools.components.View.TwoD.*;
import java.awt.event.*;
import java.awt.*;
import DataSetTools.viewer.*;
import DataSetTools.components.containers.*;
import DataSetTools.viewer.*;
import DataSetTools.viewer.Table.*;
import Command.*;
import DataSetTools.components.ui.*;
import DataSetTools.util.*;
import java.util.*;


/**
*    This class creates DataSetViewers given an IViewComponent and an
*    IVirtualArray. Although, it can be used standalone, it is best to
*    subclass the class so that the constructor requires only a DataSet and
*    a ViewerState.  In this form, it can be used by the ViewManager.
*    
*    This class just places the IVirtualComponent in the left part of a SplitPane,
*    get the Controls and adds then to the Right part of the split pane, and maintains
*    the DataSetXConversionsTable.
*/
public class DataSetViewerMaker1  extends DataSetViewer
  {
   DataSet ds;
   ViewerState state;
   IArrayMaker_DataSet viewArray;
   IViewComponent viewComp;
   DataSetData update_array;
   DataSetXConversionsTable  Conversions;

   /** 
   *   Constructor
   *   @param  ds  the DataSet that is to be viewed
   *   @param  state  The viewer State
   *   @param viewArray  the IVirtualArray(produces array values when needed)
   *   @param viewComp   the IVirtualComponent that displays the DataSet in the form
   *                     supplied by a compatible IVirtualArray
   */

  public DataSetViewerMaker1( DataSet             ds, 
                              ViewerState         state, 
                              IArrayMaker_DataSet       viewArray, 
                              IViewComponent      viewComp )
    {
     super( ds, state);
     this.viewArray = viewArray;
     this.viewComp = viewComp;
     this.ds = ds;
     this.state = state;
     if( !(viewComp instanceof DataSetViewerMethods)){
       SharedData.addmsg("The view component is missing the DataSetViewerMethods");
       return;
     }
        
     try{
       ((DataSetViewerMethods)viewComp).setData( viewArray.getArray());
     }catch(Exception ss){
          SharedData.addmsg(ss.toString());
          return;
     }
     JPanel East = new JPanel( new GridLayout( 1,1));
     
     BoxLayout blayout = new BoxLayout( East,BoxLayout.Y_AXIS);
     
     East.setLayout( blayout);
      JComponent[] ArrayScontrols =viewArray.getSharedControls();
     if( ArrayScontrols != null)
       for( int i=0; i< ArrayScontrols.length; i++)
         East.add( ArrayScontrols[i]);

     JComponent[] Arraycontrols =viewArray.getPrivateControls();
     if( Arraycontrols != null)
       for( int i=0; i< Arraycontrols.length; i++)
         East.add( Arraycontrols[i]);
      
     JComponent[] Compcontrols = viewComp.getSharedControls();
     if( Compcontrols != null)
       for( int i=0; i< Compcontrols.length; i++)
         East.add( Compcontrols[i]);    
     JComponent[] CompPcontrols = viewComp.getPrivateControls();
     if( CompPcontrols != null)
       for( int i=0; i< CompPcontrols.length; i++)
         East.add( CompPcontrols[i]);  
     
     ViewMenuItem[] MenItem1 = viewComp.getSharedMenuItems();
     ViewMenuItem[] MenItem2 = viewArray.getSharedMenuItems();
     
     PrintComponentActionListener.setUpMenuItem( getMenuBar(), this);
     SetUpMenuBar( getMenuBar(), MenItem1,((DataSetViewerMethods)viewComp).getSharedMenuItemPath());

     SetUpMenuBar( getMenuBar(), MenItem2, viewArray.getSharedMenuItemPath());

     Conversions = new DataSetXConversionsTable( ds);
     East.add( Conversions.getTable());
     East.add( Box.createRigidArea(new Dimension(30,500)) ); 
     
     viewArray.addActionListener( new ArrayActionListener());
     
     viewComp.addActionListener( new CompActionListener());
     setLayout( new GridLayout( 1,1));
     add( new SplitPaneWithState(JSplitPane.HORIZONTAL_SPLIT,
                  viewComp.getDisplayPanel(), East, .80f));

     invalidate();
    }

  /**
  *    Causes everything to be repainted
  */
  public void Repaint()
    {
     repaint();
    }

  private void SetUpMenuBar( JMenuBar bar, ViewMenuItem[] items, String[] paths){
     if( items == null)
       return;
     if( bar == null)
       return;
     for( int i = 0; i< items.length; i++){
        String path = paths[i];
        if( path != null)
          if( path.length() > 1){
            int p1= 0;
            int p = path.indexOf('.');
            if( p < 0) 
               p = path.length();
            JMenu jm = getSubMenu( bar, path.substring( p1,p));
            p1 = p+1;
            while( p1 < path.length()){
               p = path.indexOf('.',p1);
               if( p < 0) 
                 p = path.length();
               JMenu jm1 = getSubMenu( jm, path.substring(p1,p));
               p1 = p+1;
               jm = jm1;
             }
            jm.add( items[i].getItem());
            
          }
        }//for


     }
   private JMenu getSubMenu( JMenuBar jm, String path){
      if( jm == null)
         return null;
      if( path == null)
         return null;
      for( int i = 0; i < jm.getMenuCount(); i++)
        if( jm.getMenu(i).getText().equals( path))
             return jm.getMenu(i);
      JMenu Men = new JMenu(path);
      jm.add( Men);
      return Men;
   }

  
   private JMenu getSubMenu( JMenu jm, String path){
      
      if( jm == null)
         return null;
      if( path == null)
         return null;
      for( int i = 0; i < jm.getItemCount(); i++){
        if( jm.getItem(i) != null)
          if( jm.getItem(i).getText().equals(path))
            if( jm.getItem(i) instanceof JMenu)
               return (JMenu)jm.getItem(i);
      }
      JMenu Men = new JMenu(path);
      jm.add( Men);
      return Men;

   }
 
  Vector InternalPointedAts = new Vector();

  /**
  *   Causes the display to be redrawn and also the data changed to reflect a 
  *   POINTED_AT_CHANGED event
  */
  public void redraw( String reason)
    {
     if ( !validDataSet() )
        return;

     if( reason.equals( "SELECTION CHANGED" ) )
       { 
        //update_array = new DataSetData( getDataSet() );
        // viewComp.dataChanged(update_array);
        // viewComp.getGraphJPanel().repaint();
       }
     else if( reason.equals( "POINTED AT CHANGED" )) 
       { 
        int Group = ds.getPointedAtIndex();
        float time = ds.getPointedAtX();
        if( !eliminate( Group,time, InternalPointedAts))
           ((DataSetViewerMethods)viewComp).setPointedAt( 
                      viewArray.getSelectedData( Group,time));
        Conversions.showConversions( time, Group);                          
            
       };
    }


  // To make sure internal events are not dealt with like external events,
  // all external notifications are saved( values are saved). If an event 
  // with the same values return, this method returns false and eliminates 
  // those values from the saved set.
  private  boolean eliminate( int Group, float time, Vector V)
    {
     for( int i = 0; i < V.size(); i++)
       {
        Vector Pt = (Vector)(V.elementAt(i));
        if( Pt.firstElement() instanceof Integer)
           if( ((Integer)(Pt.firstElement())).intValue() == Group)
              if( Pt.lastElement() instanceof Float)
                 if( ((Float)(Pt.lastElement())).floatValue() == time)
                   {
                    V.remove(i);
                    return true;
                   }
                
       }
     return false;

    }


 
  // Listens for action events coming from the IVirtualArray
  class ArrayActionListener  implements ActionListener
    {
     public void actionPerformed( ActionEvent evt)
       {
        viewComp.dataChanged();
        Repaint();
       }
    }

  //Listens for events coming from the IViewComponent
  class CompActionListener implements ActionListener
    {
     public void actionPerformed( ActionEvent evt)
       {
        if( evt.getActionCommand().equals(IViewComponent.POINTED_AT_CHANGED) )
          {
           SelectedData2D X = (SelectedData2D)(((DataSetViewerMethods)viewComp).
                                IgetPointedAt());
           int Group = viewArray.getGroupIndex( X);

           float Time = viewArray.getTime( X);
           if( Group < 0) return;
           if( Float.isNaN(Time)) return;
           Conversions.showConversions( Time, Group);
           Vector V = new Vector();
           V.addElement( new Integer( Group));
           V.addElement( new Float( Time));
           InternalPointedAts.addElement( V);
           ds.setPointedAtX( Time);
           ds.setPointedAtIndex( Group);
           ds.notifyIObservers( IObserver.POINTED_AT_CHANGED);
          }
         else if( evt.getActionCommand().equals(IViewComponent.SELECTED_CHANGED)){
             ISelectedRegion selRegion = ((DataSetViewerMethods)viewComp).IgetSelectedRegion();
             viewArray.SelectRegion( selRegion);
         }
         
       }
    }
 


  public static void main( String args[])
    {
     DataSet[] DSS = null;
     try
       {
        DSS = ScriptUtil.load( "C:/Isaw/SampleRuns/SCD06496.RUN");
       }
     catch( Exception ss)
       {
        System.exit(0);
       }
     DataSet DS = DSS[DSS.length-1];
     JFrame jf = new JFrame("Test");
     jf.setSize( 500,500);
     DataSetViewer dsv = new DataSetViewerMaker1(DS,null,
                       new RowColTimeVirtualArray( DS,1000f,false,false,null),
                       new LargeJTableViewComponent( null, null));
     jf.getContentPane().add(dsv );

     jf.setJMenuBar( dsv.getMenuBar());
   
     jf.show();
     jf.invalidate();

    }
  }//DataSetViewerMaker1

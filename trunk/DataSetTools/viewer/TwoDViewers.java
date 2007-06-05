/*
 * File: TwoDViewers.java
 *
 * Copyright (C) 2007, Ruth Mikkelson 
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
 *  Revision 1.1  2007/06/05 20:15:32  rmikk
 *  Initial checkin for a replacement for the contour view and the Counts xy view
 * *
 */ 

package DataSetTools.viewer;

import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.TwoD.*;
import gov.anl.ipns.ViewTools.Components.TwoD.Contour.*;
import gov.anl.ipns.ViewTools.Components.Menu.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.ViewControl;
import gov.anl.ipns.ViewTools.UI.*;

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.components.View.*;
import java.awt.event.*;
import java.awt.*;
import DataSetTools.viewer.Table.*;
import Command.*;
import DataSetTools.components.ui.*;
import DataSetTools.util.*;
import java.util.*;
import javax.swing.event.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.ViewTools.Components.Region.*;
import gov.anl.ipns.ViewTools.Panels.Transforms.*;


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
public class TwoDViewers  extends DataSetViewer   {
   DataSet ds;
   ViewerState state;
   IArrayMaker_DataSet viewArray;
   IViewComponent2D viewComp;
   DataSetData update_array;
   DataSetXConversionsTable  Conversions;
   public float ImagePortion = .8f;
   JPanel ControlHolder;
   JPanel ViewHolder;
   JCheckBoxMenuItem ImageView;
   JCheckBoxMenuItem TableView ;
   JCheckBoxMenuItem ContourView ;
   String currentViewType ;
   public boolean notifyComp =true;
   public boolean notifyArray = true;
   /** 
   *   Constructor
   *   @param  ds  the DataSet that is to be viewed
   *   @param  state  The viewer State
   *   @param viewArray  the IVirtualArray(produces array values when needed)
   *   @param viewComp   the IVirtualComponent that displays the DataSet in the form
   *                     supplied by a compatible IVirtualArray
   */

  public TwoDViewers( DataSet             ds, 
                      ViewerState         state)
    {
     super( ds, state);
     setLayout( new GridLayout(1,1));
     this.viewArray =  new RowColTimeVirtualArray( ds, 
              ds.getData_entry(0).getX_scale().getStart_x(),
              false, false, state);
     ((RowColTimeVirtualArray)viewArray).ReverseY = true;
     if( state == null){
         ((RowColTimeVirtualArray)viewArray).initState();;
         state =((RowColTimeVirtualArray)viewArray).state; 
     }
     this.viewComp = new ImageViewComponent((IVirtualArray2D) viewArray.getArray());
     this.ds = ds;
     this.state = state;
     Ostate= new ObjectState();
     Ostate.insert("Data",state);
     Ostate.insert("ViewType", "Image");
     currentViewType ="Image";
     /*if( !(viewComp instanceof DataSetViewerMethods)){
       SharedData.addmsg("The view component is missing the DataSetViewerMethods");
       return;
     }*/
        
     try{
        viewComp.dataChanged( (IVirtualArray2D)viewArray.getArray());
     }catch(Exception ss){
          SharedData.addmsg(ss.toString());
          return;
     }
     ControlHolder = new JPanel( new GridLayout(1,1));
     ViewHolder = new JPanel( new GridLayout(1,1));
     setUpControls( ControlHolder, viewArray, viewComp);
     setUpViews( ViewHolder, viewArray, viewComp);
     
     
     
     
     ViewMenuItem[] MenItem2 = viewArray.getSharedMenuItems();
     
     PrintComponentActionListener.setUpMenuItem( getMenuBar(), this);
     JMenu edt = FindTopJMenu(getMenuBar(),"Edit");
     JMenuItem integrate = new JMenuItem( "Integrate");
     integrate.addActionListener( new IntegrateActionListener( this ));
     edt.add( integrate );
     getMenuBar().add( edt);
     SetUpMenuBar( getMenuBar(), MenItem2, viewArray.getSharedMenuItemPath());
     setUpViewComponentMenus(viewComp);
     
     viewArray.addActionListener( new ArrayActionListener());
    
     viewComp.addActionListener( new CompActionListener());
     
     if( state != null){
        float f = state.get_float("ControlPanelWidth(%)");
        if( !Float.isNaN(f))
        ImagePortion = 1-f/100f;
     }
    
   
     add( new SplitPaneWithState(JSplitPane.HORIZONTAL_SPLIT,
                  ViewHolder, ControlHolder, ImagePortion));
     SetUpViewChoices();
     invalidate();
     doLayout();
     validate();
     repaint();
     invalidate();
     addAncestorListener( new myAncestorListener());
    }
  
  private JMenu FindTopJMenu(JMenuBar jmenBar,String header){
     if( jmenBar == null)
        return null;
     for( int i=0; i< jmenBar.getMenuCount(); i++)
        if( jmenBar.getMenu( i ).getText().equals( header))
           return jmenBar.getMenu(i);
     
    JMenu newMenu = new JMenu( header);
    jmenBar.add( newMenu);
    return newMenu;
    }
  
  
  private void SetUpViewChoices(){
     //Find options menu 
     JMenuBar bar = getMenuBar();
     int i;
     for( i=0; (i< bar.getMenuCount())&& !bar.getMenu(i).getText().equals("Options"); i++)
     {  }
     if( i >= bar.getMenuCount()){
       bar.add( new JMenu("Options")); 
     }
     JMenu jmenu = bar.getMenu( i );
     ImageView = new JCheckBoxMenuItem( "Image");
     TableView = new JCheckBoxMenuItem( "Table");
     ContourView = new JCheckBoxMenuItem( "Contour");
     ButtonGroup group = new ButtonGroup();
     group.add( ImageView);
     group.add( TableView);
     group.add(ContourView);
     jmenu.add( ImageView);
     jmenu.add( TableView);
     jmenu.add(ContourView);
      ImageView.addActionListener( new ViewTypeActionListener( this ) );
      TableView.addActionListener(new ViewTypeActionListener( this ) );
     ContourView.addActionListener(new ViewTypeActionListener( this ));
     ImageView.setSelected( true );
     
    
     
  }
  private void setUpControls( JPanel ControlHolder, IArrayMaker_DataSet viewArray,
                           IViewComponent2D viewComp){
     JPanel EastHolder = new JPanel( new GridLayout( 1,1));
     JComponent PanView = null;
     JPanel East = new JPanel();
     BoxLayout blayout = new BoxLayout( East,BoxLayout.Y_AXIS);
     
     East.setLayout( blayout);
     JComponent[] ArrayScontrols =viewArray.getSharedControls();
     if( ArrayScontrols != null)
       for( int i=0; i< ArrayScontrols.length; i++)
        if(ArrayScontrols[i] instanceof AnimationController)
           East.add( ArrayScontrols[i]);
     JTabbedPane jtab= new JTabbedPane();
     JPanel ArrayControls = null;
     JPanel OverLayControls = null;
     JPanel OthCompControls = null;
     Conversions = new DataSetXConversionsTable( ds);
     if( ArrayScontrols != null )
     for( int i=0; i< ArrayScontrols.length; i++)
        if( !(ArrayScontrols[i] instanceof AnimationController)){
           if( ArrayControls == null){
              ArrayControls = new JPanel();
              BoxLayout blayout1 = new BoxLayout( ArrayControls,BoxLayout.Y_AXIS);
              ArrayControls.setLayout( blayout1 );
           }
           ArrayControls.add( ArrayScontrols[i]);   
        }
     if( ArrayControls != null){
        ArrayControls.add( Box.createVerticalGlue());
        jtab.add( "Data", ArrayControls);
     }
     jtab.add( "Conversions", Conversions.getTable());
     JComponent[] Arraycontrols =viewComp.getControls();
     if( Arraycontrols != null){
      for( int i=0; i < Arraycontrols.length; i++)
          if( Arraycontrols[i] instanceof ViewControl)
             if( ((ViewControl)Arraycontrols[i]).getTitle().endsWith("Overlay")){
                if( OverLayControls == null){
                   OverLayControls = new JPanel();
                   BoxLayout blayout2 = new BoxLayout( OverLayControls,BoxLayout.Y_AXIS);
                   OverLayControls.setLayout( blayout2 );
                   
                }
                OverLayControls.add( Arraycontrols[i]);
                   
             }
      if( OverLayControls != null){
         OverLayControls.add(Box.createVerticalGlue());
         jtab.add("Overlays", OverLayControls);
      }
      
      for( int i=0; i< Arraycontrols.length; i++)
         if( Arraycontrols[i] instanceof ViewControl)
            if( !((ViewControl)Arraycontrols[i]).getTitle().endsWith("Overlay"))
               if(!( Arraycontrols[i] instanceof 
                           gov.anl.ipns.ViewTools.Components.ViewControls.PanViewControl)){
                  if( OthCompControls == null){
                     OthCompControls = new JPanel();
                     BoxLayout blayout3= new BoxLayout( OthCompControls, BoxLayout.Y_AXIS);
                     OthCompControls.setLayout( blayout3);
                  }
                  OthCompControls.add( Arraycontrols[i]);
               }else
                 PanView = Arraycontrols[i]; 
     } 
     if( OthCompControls != null){
        OthCompControls.add(Box.createVerticalGlue());
        jtab.add("Other view Info", OthCompControls);
     }
     
    East.add(jtab);
    if( PanView != null  && !currentViewType.equals("Table"))
       East.add( PanView);
      
   
     

    
   
     East.add( Box.createVerticalGlue()); 
     EastHolder.add(East);
     ControlHolder.add( EastHolder);
     
  }
  
  private void setUpViews( JPanel ViewHolder, IArrayMaker_DataSet viewArray,
           IViewComponent2D viewComp){
     ViewHolder.add( viewComp.getDisplayPanel());
     
  }
  private void removeViewComponentMenus( IViewComponent2D viewComp){
     
     ViewMenuItem[] MenItem1 = viewComp.getMenuItems();
     if( MenItem1 == null)
        return;
     JMenuBar bar = getMenuBar();
     JMenu item = null;
     int p1 =-1;
     for( int i=0; i< MenItem1.length; i++){
        String path = MenItem1[i].getPath();
        if( path != null){
           int p0 = p1+1;
           p1 = path.indexOf(".",p0);
           while( p1 >=0){
              item = InsertInMenu(bar,item, path.substring(p0,p1) );
              p0=p1+1;
              p1 = path.indexOf(".",p0);
              
           }
           if( item != null) if( p0 >=0){
              item.remove( MenItem1[i].getItem());
           }
        }
     }
     
     // Now remove all JMenu's with No JMenuItems
     
        JMenuBar jm = getMenuBar();
       
        for( int i= jm.getMenuCount()-1; i>=0; i--){
           JMenu jmenu = jm.getMenu(i);
           RemoveAllSingletons( jmenu);
           if( jmenu.getItemCount() < 1){
              jm.remove( i );
             
           }
        }
 
  }
  
  private void RemoveAllSingletons( JMenu jmenu){
     if( jmenu == null)
        return;
     for( int i = jmenu.getItemCount()-1; i>=0; i--){
        JMenuItem jmi = jmenu.getItem( i );
        if( jmi instanceof JMenu){
           RemoveAllSingletons( (JMenu)jmi);
           if( ((JMenu)jmi).getItemCount() <1)
              jmenu.remove( i);
        }
        
     }
  }
  private void setUpViewComponentMenus( IViewComponent2D viewComp){
    
     
    ViewMenuItem[] MenItem1 = viewComp.getMenuItems();
    if( MenItem1 == null)
       return;
    JMenuBar bar = getMenuBar();
    JMenu item = null;
    for( int i=0; i< MenItem1.length; i++){
       String path = MenItem1[i].getPath();

       int p1 =-1;
       item = null;
       if( path != null){
          int p0 = p1+1;
          p1 = path.indexOf(".",p0);
          if( p1 < 0)
             p1 = path.length();
          while( p0 < path.length() ){
             item = InsertInMenu(bar,item, path.substring(p0,p1) );
             p0=p1+1;
             p1 = path.indexOf(".",p0);
             if( p1 < 0)
                p1 = path.length();
             
          }
          if( item != null) if( p0 >=0){
             item.add( MenItem1[i].getItem());
          }
       }
    }
 }
    
     
     
 
 

 private JMenu InsertInMenu( JMenuBar bar, JMenu item, String path){
     if( item == null){
        for( int i=0; i< bar.getMenuCount(); i++)
           if( bar.getMenu( i ).getText().equals( path))
              return bar.getMenu(i);
         JMenu mainMenu = new JMenu(path); 
         bar.add( mainMenu);
         return mainMenu;
     }else  {
        for( int i=0; i< item.getItemCount(); i++)
           if( item.getItem( i).getText().equals( path ))
              return (JMenu)item.getItem( i );
        JMenu subMenu = new JMenu( path );
        item.add( subMenu);
        return subMenu;
     }
     
 }
 //Re initializes whole thing when view changes
  private void reInit( IViewComponent2D oldViewType, String newViewType){
     
     if( viewComp != null){
          if(! Ostate.reset("View"+currentViewType,viewComp.getObjectState( false)))
             Ostate.insert("View"+currentViewType,viewComp.getObjectState( false));
          viewComp.kill();
     }
     
     
     if( newViewType.equals("Image")){
        viewComp= new ImageViewComponent( (IVirtualArray2D)viewArray.getArray());
        if( Ostate.get("View"+"Image") != null &&!( Ostate.get("ViewTable") instanceof String)) 
              viewComp.setObjectState( (ObjectState)Ostate.get("View"+"Image"));
        ImageView.setSelected( true);
        currentViewType = "Image";
     }else  if( newViewType.equals("Table")){
        viewComp = new TableViewComponent( (IVirtualArray2D)viewArray.getArray());
        if( (Ostate.get("View"+"Table") != null)&&!( Ostate.get("ViewTable") instanceof String))
             viewComp.setObjectState( (ObjectState)Ostate.get("View"+"Table"));
        TableView.setSelected( true); 
        currentViewType ="Table";
     }else if( newViewType.equals("Contour")){
        viewComp= new ContourViewComponent((IVirtualArray2D)viewArray.getArray());
        if( Ostate.get("View"+"Contour") != null &&!( Ostate.get("ViewTable") instanceof String))
              viewComp.setObjectState( (ObjectState)Ostate.get("View"+"Contour"));
        ContourView.setSelected( true);
        currentViewType = "Contour";
     }else
        return;
     viewComp.addActionListener( new CompActionListener());
     ControlHolder.removeAll();
     ViewHolder.removeAll();
     ControlHolder.setLayout( new GridLayout(1,1));
     ViewHolder.setLayout( new GridLayout(1,1));
     removeViewComponentMenus( oldViewType);
     setUpViewComponentMenus( viewComp);
     setUpControls( ControlHolder, viewArray, viewComp);
     setUpViews( ViewHolder, viewArray, viewComp);
     invalidate();
     repaint();
     doLayout();
     validate();
     invalidate();
     repaint();
     
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
        String path=null;
        if( paths != null)
           path = paths[i];
        else
           path = items[i].getPath();
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
        if( !eliminate( Group,time, InternalPointedAts)){
           if(!Float.isNaN(time))
               viewArray.setTime( time);
           SelectedData2D X =(SelectedData2D) ((RowColTimeVirtualArray)viewArray).getSelectedData( Group, time);
           int nrows = ((RowColTimeVirtualArray)viewArray).getTotalNumRows();
           
           
           floatPoint2D X2D = new floatPoint2D(1f+ X.getCol(), 1f+ nrows - 1- X.getRow());
           notifyComp =false;
           viewComp.setPointedAt( X2D);
        }
        Conversions.showConversions( time, Group);                          
            
       }
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
       // if( viewComp instanceof ImageViewComponent)
           viewComp.dataChanged( (IVirtualArray2D)(viewArray.getArray()) );
           ViewHolder.removeAll();
       // else;
           ViewHolder.add( viewComp.getDisplayPanel());
           invalidate();
           repaint();
           doLayout();
           validate();
           invalidate();
           repaint();
         
		//    viewComp.dataChanged();
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
           if( ! notifyComp){
              notifyComp = true;
              return;
           }
           floatPoint2D X1 = viewComp.getPointedAt();
          // X1.y = ((RowColTimeVirtualArray)viewArray).getTotalNumRows() - X1.y ;
           SelectedData2D X = new SelectedData2D( (int)(X1.y+.5)-1, (int)(X1.x+.5)-1, 
                      ((RowColTimeVirtualArray)viewArray).getTime(1,1));
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
            //What to do with selected???  No default
           /*  Region[] Regs = viewComp.getSelectedRegions();
             SelectedRegion2D selRegion=null ;
             CoordTransform transform = viewComp.getWorldToArrayTransform();
             Vector pts = new Vector();
             if( Regs != null)
                for( int i=0; i < Regs.length; i++){
                   java.awt.Point[] p = Regs[i].getSelectedPoints( transform );
                   if( p != null)
                   for( int k=0; k< p.length; k++)
                       if( !pts.contains( p[k] ))
                            pts.addElement(p[k]);
                }
             int[] xs = new int[ pts.size()];
             int[] ys = new int[ pts.size() ];
             for( int i=0; i< pts.size(); i++){
                java.awt.Point point =(java.awt.Point)pts.elementAt( i );
                xs[i] = ( point.x -1);
                ys[i] = point.y-1;
             }
             selRegion= new SelectedRegion2D( xs, ys);
             ((RowColTimeVirtualArray)viewArray).SelectRegion( selRegion);
          */
         }
         
       }
    }
  
  class myAncestorListener implements AncestorListener{
     public void ancestorAdded(AncestorEvent event){
     }
    
     public void ancestorRemoved(AncestorEvent event){
       
       viewComp.kill();
       viewArray.kill();
     }
     public void ancestorMoved(AncestorEvent event){

     }
     
  }

  class ViewTypeActionListener implements ActionListener{
     TwoDViewers D2;
     public ViewTypeActionListener( TwoDViewers DSView){
        D2= DSView;
     }
     
     public void actionPerformed( ActionEvent evt){
        String button = evt.getActionCommand();
        if( button.equals( "Image")){
          
        }else  if( button.equals( "Table")){
          
           
        }else  if( button.equals( "Contour")){
       
           
        }else
           return;
        D2.reInit( D2.viewComp, button);
     }
  }
  
  class IntegrateActionListener implements ActionListener{
     TwoDViewers viewer;
     public IntegrateActionListener( TwoDViewers viewer){
        this.viewer = viewer;
     }
     
     public void actionPerformed( ActionEvent evt){
        RowColTimeVirtualArray.ShowIntegrateStats( 
                 (IVirtualArray2D)viewer.viewArray.getArray(),
                 viewer.viewComp.getSelectedRegions(),
                 viewer.viewComp.getWorldToArrayTransform());
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
     FinishJFrame jf = new FinishJFrame("Test");
     jf.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE);
     jf.setSize( 500,500);
     TwoDViewers dsv = new TwoDViewers(DS , null);
     jf.getContentPane().add(dsv );

     jf.setJMenuBar( dsv.getMenuBar());
     jf.invalidate();
     WindowShower.show(jf);
    
 /*    int n =3;
     while( n >=0)
       try{
        int c;
        for( c =System.in.read();c <32; c =System.in.read())
        {}
        String S =""+(char)c;
        for( c =System.in.read();c >32; c =System.in.read())
        { S +=(char)c;}
        n= (new Integer(S.trim())).intValue();
        DS.setPointedAtIndex( n );
        DS.setPointedAtX( DS.getData_entry(0).getX_scale().getStart_x());
        dsv.redraw("POINTED AT CHANGED"); 
     
     }catch(Exception s){
        System.exit(0);
     }
*/
    }
  
  //-------------------- IPreserveState Methods & Variables-----------------
   ObjectState Ostate= null;
   
   public void setObjectState( ObjectState new_state){
     Ostate = new_state;
     
          
     state = (ViewerState)Ostate.get("Data");
         
     viewComp.setObjectState( 
                 (ObjectState)Ostate.get("View"+currentViewType));
     currentViewType = Ostate.get("ViewType").toString();
          
          
      
   }
   public ObjectState getObjectState( boolean is_default){
       if( Ostate == null)
          Ostate = new ObjectState();
       
       ObjectState state = new ObjectState();
       if( !is_default)
          state = Ostate;
       
       ViewerState st = new ViewerState();
       if( !is_default)
          st = this.state;
       
       state.insert("Data", st);
                          
     
       state.insert("View"+currentViewType ,viewComp.getObjectState 
                   (is_default));
       if( !is_default)
           state.insert("ViewType", currentViewType);
       else
          state.insert("ViewType","Image");
       return state;
       
   }
   
  }//DataSetViewerMaker1

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
 *  Revision 1.10  2007/10/09 01:08:48  rmikk
 *  Added another tab for the Contour View
 *
 *  Revision 1.9  2007/10/05 13:32:28  rmikk
 *  Fixed javadoc errors
 *
 *  Revision 1.8  2007/07/26 20:19:18  rmikk
 *  Renamed Show Tag Frame in Menu's to Pointed at Graph View to parallel the
 *      Pointed at Table View
 *  Fixed the menu's so fewer option are shown and all the view component
 *       menu items are shown just once.
 *  When tag Frame is closed, now adjust the other two parallel variables
 *
 *  Revision 1.7  2007/07/13 18:08:58  dennis
 *  Added getDisplayComponent() method to return just the data display
 *  panel without any controls.
 *
 *  Revision 1.6  2007/06/20 16:50:20  rmikk
 *  The color no longer autoscales and the aspect ratio is set in the ImageView
 *
 *  Revision 1.5  2007/06/19 15:24:36  rmikk
 *  -Used dataChanged(){no args} when time changes. This retains zooms but
 *         not the position of the table.
 *  -Introduced methods to update object states, etc., in one place
 *  -Added code to position the table. On animation the user MUST select the
 *     point in the upper left point in the table
 *
 *  Revision 1.4  2007/06/14 22:02:59  rmikk
 *  Eliminated some unnecessary layouts
 *  Add the PanView to the Table view
 *
 *  Revision 1.3  2007/06/13 16:30:49  rmikk
 *  Add the component listener to the JFrame sooner so the tag frames follow the
 *     JFrame when the JFrame moves.
 *
 *  Revision 1.2  2007/06/12 22:02:35  rmikk
 *  Incorporated the GraphTagFrame to view slices of the data
 *
 *  Revision 1.1  2007/06/05 20:15:32  rmikk
 *  Initial checkin for a replacement for the contour view and the Counts xy view
 * *
 */

package DataSetTools.viewer;

import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.TwoD.*;
import gov.anl.ipns.ViewTools.Panels.Image.*;
import gov.anl.ipns.ViewTools.Components.TwoD.Contour.*;
import gov.anl.ipns.ViewTools.Components.Menu.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.*;
import gov.anl.ipns.ViewTools.UI.*;
import gov.anl.ipns.ViewTools.Panels.*;
import javax.swing.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.*;
import DataSetTools.dataset.*;
import DataSetTools.components.View.*;
import java.awt.event.*;
import java.awt.*;
import DataSetTools.viewer.Table.*;
import Command.*;
import DataSetTools.components.ui.*;
import DataSetTools.operator.DataSet.Attribute.GetPixelInfo_op;
import DataSetTools.util.*;
import java.util.*;
import javax.swing.event.*;
import gov.anl.ipns.Util.Numeric.*;
// import gov.anl.ipns.ViewTools.Components.Region.*;
import gov.anl.ipns.ViewTools.Panels.Transforms.*;
import gov.anl.ipns.ViewTools.Components.Transparency.*;
import gov.anl.ipns.ViewTools.Panels.Table.*;

// import gov.anl.ipns.ViewTools.Panels.Table.*;

/**
 * This class creates DataSetViewers given an IViewComponent and an
 * IVirtualArray. Although, it can be used standalone, it is best to subclass
 * the class so that the constructor requires only a DataSet and a ViewerState.
 * In this form, it can be used by the ViewManager.
 * 
 * This class just places the IVirtualComponent in the left part of a SplitPane,
 * get the Controls and adds then to the Right part of the split pane, and
 * maintains the DataSetXConversionsTable.
 */
public class TwoDViewers extends DataSetViewer {

   DataSet                     ds;
   ViewerState                 state;
   IArrayMaker_DataSet         viewArray;
   IViewComponent2D            viewComp;
   DataSetData                 update_array;
   DataSetXConversionsTable    Conversions;
   public float                ImagePortion = .8f;
   JPanel                      ControlHolder;
   JPanel                      ViewHolder;
   JCheckBoxMenuItem           ImageView;
   JCheckBoxMenuItem           TableView;
   JCheckBoxMenuItem           ContourView;
   String                      currentViewType;

   public boolean              notifyComp   = true;
   public boolean              notifyArray  = true;

   JCheckBoxMenuItem           ShowTag;
   boolean                     showtag;
   
   private static final String SHOW_TAG     = "Pointed At Graph View";
   JPanel DisplayPanel;
   Point TableTopLeft;
   
   ClosedInterval YRange;


   /**
    * This DataSet viewer cycles between 3 forms of the viewers, the image,
    * table and contour ViewComponents
    * 
    * @param ds
    *           the DataSet that is to be viewed
    * @param state
    *           The viewer State
    *  
    */

   public TwoDViewers( DataSet ds, ViewerState state ) {

      super( ds , state );
      setLayout( new GridLayout( 1 , 1 ) );
      this.viewArray = new RowColTimeVirtualArray( ds , ds.getData_entry( 0 )
               .getX_scale().getStart_x() , false , false , state );

      ( (RowColTimeVirtualArray) viewArray ).ReverseY = true;

      if( state == null ) {
         ( (RowColTimeVirtualArray) viewArray ).initState();
         
         state = ( (RowColTimeVirtualArray) viewArray ).state;
      }

      YRange = ds.getYRange();
    
      this.viewComp = new ImageViewComponent( (IVirtualArray2D) viewArray
               .getArray() );
      this.ds = ds;
      this.state = state;

      Ostate = new ObjectState();
      Ostate.insert( "Data" , state );
      Ostate.insert( "ViewType" , "Image" );
      currentViewType = "Image";
      TableTopLeft = new Point( 1,1);
      ObjectState ViewState = (ObjectState)viewComp.getObjectState( true );
      floatPoint2D Range = (floatPoint2D)ViewState.get( ImageViewComponent.LOG_SCALE_SLIDER+
                     "."+ControlSlider.RANGE);
      if( Range != null)
         
          ViewState.reset( ImageViewComponent.LOG_SCALE_SLIDER+"."+
                       ControlSlider.SLIDER_VALUE, (Range.x + Range.y)/2f);
      viewComp.setObjectState( ViewState );
      Ostate.insert("ViewImage", viewComp.getObjectState( true ));
      

      try {
         viewComp.dataChanged( (IVirtualArray2D) viewArray.getArray() );
      }
      catch( Exception ss ) {
         SharedData.addmsg( ss.toString() );
         return;
      }

      ControlHolder = new JPanel( new GridLayout( 1 , 1 ) );
      ViewHolder = new JPanel( new GridLayout( 1 , 1 ) );

      setUpControls( ControlHolder , viewArray , viewComp );
      setUpViews( ViewHolder , viewArray , viewComp );

      ViewMenuItem[] MenItem2 = viewArray.getSharedMenuItems();

      PrintComponentActionListener.setUpMenuItem( getMenuBar() , this );
      JMenu edt = FindTopJMenu( getMenuBar() , "Edit" );
      FindTopJMenu( getMenuBar() , "View" );
      FindTopJMenu( getMenuBar() , "Options" );
      FindTopJMenu( getMenuBar() , "Help" );


      JMenuItem integrate = new JMenuItem( "Integrate" );
      integrate.addActionListener( new IntegrateActionListener( this ) );
      edt.add( integrate );

      SetUpMenuBar( getMenuBar() , MenItem2 , viewArray.getSharedMenuItemPath() );
      setUpViewComponentMenus( viewComp );

      viewArray.addActionListener( new ArrayActionListener() );

      viewComp.addActionListener( new CompActionListener() );

      if( state != null ) {
         float f = state.get_float( "ControlPanelWidth(%)" );
         if( ! Float.isNaN( f ) ) ImagePortion = 1 - f / 100f;
      }

      add( new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT , ViewHolder ,
               ControlHolder , ImagePortion ) );
     
      SetUpViewChoices();
      invalidate();
      doLayout();
      validate();
      repaint();
      invalidate();
      addAncestorListener( new myAncestorListener() );

   }


   private JMenu FindTopJMenu( JMenuBar jmenBar , String header ) {

      if( jmenBar == null ) return null;
      
      for( int i = 0 ; i < jmenBar.getMenuCount() ; i++ )
         
         if( jmenBar.getMenu( i ).getText().equals( header ) )
            return jmenBar.getMenu( i );

      JMenu newMenu = new JMenu( header );
      jmenBar.add( newMenu );
      return newMenu;
   }


   private void SetUpViewChoices() {

      // Find options menu
      JMenuBar bar = getMenuBar();
      int i;
      for( i = 0 ; ( i < bar.getMenuCount() )
               && ! bar.getMenu( i ).getText().equals( "Options" ) ; i++ ) {
      }
      if( i >= bar.getMenuCount() ) {
         bar.add( new JMenu( "Options" ) );
      }


      JMenu jmenu = bar.getMenu( i );

      ImageView = new JCheckBoxMenuItem( "Image" );
      TableView = new JCheckBoxMenuItem( "Table" );
      ContourView = new JCheckBoxMenuItem( "Contour" );

      ButtonGroup group = new ButtonGroup();
      group.add( ImageView );
      group.add( TableView );
      group.add( ContourView );

      jmenu.add( new JLabel( "Viewer Modes" ) );
      jmenu.add( ImageView );
      jmenu.add( TableView );
      jmenu.add( ContourView );
      jmenu.add( new JSeparator() );

      ImageView.addActionListener( new ViewTypeActionListener( this ) );
      TableView.addActionListener( new ViewTypeActionListener( this ) );
      ContourView.addActionListener( new ViewTypeActionListener( this ) );
      ImageView.setSelected( true );

      ShowTag = new JCheckBoxMenuItem( SHOW_TAG );
      jmenu.add( ShowTag );
      ShowTag.addActionListener( new ViewTypeActionListener( this ) );
      showtag = false;
      ShowTag.setSelected( false );
      jmenu.add( new JSeparator() );
   }


   private void setUpControls( JPanel ControlHolder ,
            IArrayMaker_DataSet viewArray , IViewComponent2D viewComp ) {

      JPanel EastHolder = new JPanel( new GridLayout( 1 , 1 ) );
      JComponent PanView = null;
      JPanel East = new JPanel();
      BoxLayout blayout = new BoxLayout( East , BoxLayout.Y_AXIS );

      East.setLayout( blayout );

      JComponent[] ArrayScontrols = viewArray.getSharedControls();
      if( ArrayScontrols != null )
         for( int i = 0 ; i < ArrayScontrols.length ; i++ )
            if( ArrayScontrols[ i ] instanceof AnimationController )
               East.add( ArrayScontrols[ i ] );

      JTabbedPane jtab = new JTabbedPane();
      JPanel ArrayControls = null;
      JPanel OverLayControls = null;
      JPanel OthCompControls = null;
      JPanel ContourLevelControls = null;
      Conversions = new DataSetXConversionsTable( ds );

      if( ArrayScontrols != null )
         for( int i = 0 ; i < ArrayScontrols.length ; i++ )
            if( ! ( ArrayScontrols[ i ] instanceof AnimationController ) ) {
               if( ArrayControls == null ) {
                  ArrayControls = new JPanel();
                  BoxLayout blayout1 = new BoxLayout( ArrayControls ,
                           BoxLayout.Y_AXIS );
                  ArrayControls.setLayout( blayout1 );
               }
               ArrayControls.add( ArrayScontrols[ i ] );
            }

      if( ArrayControls != null ) {
         ArrayControls.add( Box.createVerticalGlue() );
         jtab.add( "Data" , ArrayControls );
      }

      jtab.add( "Conversions" , Conversions.getTable() );

      JComponent[] Arraycontrols = viewComp.getControls();

      if( Arraycontrols != null )
         for( int i = 0 ; i < Arraycontrols.length ; i++ )
            if( Arraycontrols[ i ] instanceof ViewControl )
               if( ( (ViewControl) Arraycontrols[ i ] ).getTitle().endsWith(
                        "Overlay" ) ) {
                  if( OverLayControls == null ) {
                     OverLayControls = new JPanel();
                     BoxLayout blayout2 = new BoxLayout( OverLayControls ,
                              BoxLayout.Y_AXIS );
                     OverLayControls.setLayout( blayout2 );

                  }
                  OverLayControls.add( Arraycontrols[ i ] );

               }

      if( OverLayControls != null ) {
         OverLayControls.add( Box.createVerticalGlue() );
         jtab.add( "Overlays" , OverLayControls );
      }

      if( viewComp instanceof ContourViewComponent ) {
         
         JPanel ContourTab = SetUpTab( Arraycontrols ,
                  null , 
                  null , 
                  
                  Add( null ,CompositeContourControl.class ) ,
                  
                  Add( Add( Add( null ,
                    "Labels" ) ,
                    ContourControlHandler.CALCULATE_BUTTON_LABEL ) ,
                    ContourControlHandler.SIGNIFICANT_FIGURES_LABEL ) );
         
         if( ContourTab != null )
            jtab.add( "Contours" , ContourTab );
         
      }

      OthCompControls = SetUpTab( Arraycontrols ,
               Add( Add( null , PanViewControl.class ) ,
                                   CompositeContourControl.class ) ,
                                   
               Add( Add( Add( Add( null , "Overlay" ) ,
               "Labels" ) ,
               ContourControlHandler.CALCULATE_BUTTON_LABEL ) ,
               ContourControlHandler.SIGNIFICANT_FIGURES_LABEL ) ,
               
               (Vector) null , 
               (Vector) null );


     

      if( OthCompControls != null ) {
         
         jtab.add( "Other view Info" , OthCompControls );
      }


      for( int i = 0 ; i < Arraycontrols.length ; i++ )
         if( Arraycontrols[ i ] instanceof PanViewControl )
            PanView = Arraycontrols[ i ];

      East.add( jtab );
      if( PanView != null )// && ! currentViewType.equals( "Table" )
         East.add( PanView );


      East.add( Box.createVerticalGlue() );
      EastHolder.add( East );
      ControlHolder.add( EastHolder );
   }


   private Vector Add( Vector V , Object O ) {

      if( V == null )
         V = new Vector();
      V.addElement( O );
      return V;
   }
   
   /**
    * SetUp a series of controls in the tab pane
    * 
    * @param Arraycontrols  The list of controls
    * 
    * @param OmitClasses   The classes to omit. If null, none will be excluded here
    * @param OmitTitleParts  The titles(parts of titles) to be omitted. If null none
    *                        will be excluded
    * @param IncludeClasses  The classes to include. If null all will be included
    * 
    * @param IncludeNameParts  The titles with these parts will be included.  If null
    *                          all will be considered
    * @return
    */
   private JPanel SetUpTab( JComponent[] Arraycontrols, Vector OmitClasses,
                  Vector OmitTitleParts, Vector IncludeClasses, 
                  Vector IncludeNameParts){
      
      JPanel OthCompControls = null;
      
      for( int i = 0 ; i < Arraycontrols.length ; i++ )
            if( UseThisControl(Arraycontrols[i],OmitClasses,OmitTitleParts,
                     IncludeClasses, IncludeNameParts)){
               
                  if( OthCompControls == null ) {
                     
                     OthCompControls = new JPanel();
                     BoxLayout blayout3 = new BoxLayout( OthCompControls ,
                              BoxLayout.Y_AXIS );
                     OthCompControls.setLayout( blayout3 );
                  }
                  
                  OthCompControls.add( Arraycontrols[ i ] );
               }
      
      if( OthCompControls != null){
         //OthCompControls.add( Box.createVerticalGlue() );
      }
      
      return OthCompControls;
              
   }
   
   // Determines if a Control is to be included 
   
   private boolean UseThisControl( JComponent control , 
                                  Vector OmitClasses ,
                                  Vector OmitTitleParts ,
                                  Vector IncludeClasses ,
                                  Vector IncludeNameParts ) {

      if( control == null )
         return false;

      if( ! ( control instanceof ViewControl ) )
         return false;

      if( OmitClasses != null )
         for( int i = 0 ; i < OmitClasses.size() ; i++ )
            if( control.getClass().equals( OmitClasses.elementAt( i ) ) )
               return false;

      if( OmitTitleParts != null )
         for( int i = 0 ; i < OmitTitleParts.size() ; i++ )
            if( ( (ViewControl) control ).getTitle().indexOf(
                     (String) OmitTitleParts.elementAt( i ) ) >= 0 )
               return false;


      if( IncludeClasses != null )
         for( int i = 0 ; i < IncludeClasses.size() ; i++ )
            if( control.getClass().equals( IncludeClasses.elementAt( i ) ) )
               return true;

      if( IncludeNameParts != null )
         for( int i = 0 ; i < IncludeNameParts.size() ; i++ )
            if( ( (ViewControl) control ).getTitle().indexOf(
                     (String) IncludeNameParts.elementAt( i ) ) >= 0 )
               return true;

      if( IncludeClasses == null && IncludeNameParts == null )
         return true;

      return false;


   }
   

   private void setUpViews( JPanel              ViewHolder , 
                            IArrayMaker_DataSet viewArray ,
                            IViewComponent2D    viewComp ) {

      DisplayPanel = viewComp.getDisplayPanel();
      Set2DObjectState();
      ViewHolder.add( DisplayPanel );
     
      ActiveJPanel JoverlayPanel = MarkOverLayJPanels( DisplayPanel );
      if( JoverlayPanel == null ) {

           return;

      }

      DisplayChangedListener dispList = new DisplayChangedListener( this );


      JoverlayPanel.addActionListener( dispList );
      JoverlayPanel.addComponentListener( dispList );
   }

   
   

   // Only looks for sub elements that are not overlayJPanels but
   // ActiveJPanels
   private ActiveJPanel MarkOverLayJPanels( JComponent jp ) {

      if( jp instanceof ActiveJPanel ) return (ActiveJPanel) jp;
      
      ActiveJPanel jp1 = null;
      for( int i = 0 ; i < jp.getComponentCount() ; i++ ) {
         
         if( jp.getComponent( i ) instanceof JComponent )
            if( ! ( jp.getComponent( i ) instanceof OverlayJPanel ) )
               if( jp.getComponent( i ) instanceof ActiveJPanel ) {
                  jp1 = (ActiveJPanel) jp.getComponent( i );
                  return jp1;

               }
               else {
                  
                  ActiveJPanel Res = MarkOverLayJPanels( (JComponent) ( jp
                           .getComponent( i ) ) );
                  if( Res != null ) return Res;
               }

      }

      return null;


   }


   
   
   private void removeViewComponentMenus( IViewComponent2D viewComp ) {
      
   
      ViewMenuItem[] MenItem1 = viewComp.getMenuItems();
      
      if( MenItem1 == null ) return;
      
      JMenuBar bar = getMenuBar();
      JMenu item = null;
      int p1 = - 1;
      
      for( int i = 0 ; i < MenItem1.length ; i++ ) {
         
         String path = MenItem1[ i ].getPath();
         if( path != null ) {
            
            int p0 = p1 + 1;
            p1 = path.indexOf( "." , p0 );
            if( p1 < 0 )
               p1 =path.length();
            item = null;
            while( p1 >= 0 ) {
               String MenItem = path.substring( p0,p1);
               if( item == null){
                  for( int j=0;j< bar.getMenuCount() && item == null ; j++)
                     if( bar.getMenu( j ).getText().equals( MenItem))
                        item = bar.getMenu( j );
                     
               }else
                  for( int j=0; j< item.getItemCount(); j++)
                     if( item.getItem(j) instanceof JMenu && 
                                 item.getItem(j).getText().equals( MenItem))
                        item = (JMenu)item.getItem(j);
               
                  
               
               p0 = p1 + 1;
               if( p0 < path.length() )
                  p1 = path.indexOf( "." , p0 );
               else
                  p1 = -1;
               if(item == null)
                  p1= -1;
            }
            
            if( item != null ) if( p0 >= 0 ) {
               
               item.remove( MenItem1[ i ].getItem() );
               
            }
            
         }
      }

      // Now remove all JMenu's with No JMenuItems

      JMenuBar jm = getMenuBar();

      for( int i = jm.getMenuCount() - 1 ; i >= 0 ; i-- ) {
         
         JMenu jmenu = jm.getMenu( i );
         RemoveAllSingletons( jmenu );
         
         if( ";File;Edit;View;Options;Help;".indexOf( ";"+jmenu.getText()+";" ) <= 0 )
            if( jmenu.getItemCount() < 1 ) {
               
               jm.remove( i );

            }
      }

   }

   
   

   private void RemoveAllSingletons( JMenu jmenu ) {

      if( jmenu == null ) return;
      
      for( int i = jmenu.getItemCount() - 1 ; i >= 0 ; i-- ) {
         
         JMenuItem jmi = jmenu.getItem( i );
         if( jmi instanceof JMenu ) {
            
            RemoveAllSingletons( (JMenu) jmi );
            if( ( (JMenu) jmi ).getItemCount() < 1 ) jmenu.remove( i );
            
         }

      }
   }


   private void setUpViewComponentMenus( IViewComponent2D viewComp ) {


      ViewMenuItem[] MenItem1 = viewComp.getMenuItems();
      if( MenItem1 == null ) return;
      
      JMenuBar bar = getMenuBar();
      JMenu item = null;
      for( int i = 0 ; i < MenItem1.length ; i++ ) {
         
         String path = MenItem1[ i ].getPath();

         int p1 = - 1;
         item = null;
         if( path != null ) {
            int p0 = p1 + 1;
            p1 = path.indexOf( "." , p0 );
            if( p1 < 0 ) p1 = path.length();
            while( p0 < path.length() ) {
               item = InsertInMenu( bar , item , path.substring( p0 , p1 ) );
               p0 = p1 + 1;
               p1 = path.indexOf( "." , p0 );
               if( p1 < 0 ) p1 = path.length();

            }
            
            if( item != null ) if( p0 >= 0 ) {
               
               item.add( MenItem1[ i ].getItem() );
               
            }
         }
      }
   }


   
   
   private JMenu InsertInMenu( JMenuBar bar , JMenu item , String path ) {

      if( item == null ) {
         
         for( int i = 0 ; i < bar.getMenuCount() ; i++ )
            if( bar.getMenu( i ).getText().equals( path ) )
               return bar.getMenu( i );
         
         JMenu mainMenu = new JMenu( path );
         bar.add( mainMenu );
         return mainMenu;
         
      }
      else {
         
         for( int i = 0 ; i < item.getItemCount() ; i++ )
            if( item.getItem( i ).getText().equals( path ) )
               
               return (JMenu) item.getItem( i );
         
         JMenu subMenu = new JMenu( path );
         item.add( subMenu );
         return subMenu;
         
      }

   }

   
   

   // Re initializes whole thing when view changes
   private void reInit( IViewComponent2D oldViewType , String newViewType ) {

      if( viewComp != null ) {
         
        Update2DObjectState();
        UpdateViewObjectState();
        removeViewComponentMenus( viewComp );
        viewComp.kill();
      
      }


      if( newViewType.equals( "Image" ) ) {
         
         viewComp = new ImageViewComponent( (IVirtualArray2D) viewArray
                  .getArray() );
         
                 
         ImageView.setSelected( true );
         currentViewType = "Image";
         
      }
      else if( newViewType.equals( "Table" ) ) {
         
         viewComp = new TableViewComponent( (IVirtualArray2D) viewArray
                  .getArray() );
         
                 
         TableView.setSelected( true );
         currentViewType = "Table";
         
      }
      else if( newViewType.equals( "Contour" ) ) {
         
         viewComp = new ContourViewComponent( (IVirtualArray2D) viewArray
                  .getArray() );
       
         ContourView.setSelected( true );
         currentViewType = "Contour";
         
      }
      else
         return;
      Ostate.reset("ViewType", currentViewType );
      DisplayPanel = viewComp.getDisplayPanel();
      Set2DObjectState();
      SetViewObjectState();
      viewComp.addActionListener( new CompActionListener() );
      
      ControlHolder.removeAll();
      ViewHolder.removeAll();
      ControlHolder.setLayout( new GridLayout( 1 , 1 ) );
      ViewHolder.setLayout( new GridLayout( 1 , 1 ) );
      
      
      setUpViewComponentMenus( viewComp );
      setUpControls( ControlHolder , viewArray , viewComp );
      setUpViews( ViewHolder , viewArray , viewComp );
      
      invalidate();
      repaint();
      doLayout();
      validate();
      invalidate();
      repaint();

   }


   /**
    * Causes everything to be repainted
    */
   public void Repaint() {

      repaint();
   }


   private void SetUpMenuBar( JMenuBar bar , ViewMenuItem[] items ,
            String[] paths ) {
     
      SetUpMenuBar1( bar, viewArray.getSharedMenuItems()); 
      SetUpMenuBar1( bar, viewComp.getMenuItems());
   }

   private void SetUpMenuBar1( JMenuBar bar , ViewMenuItem[] items){
      
      if( items == null ) return;
      if( bar == null ) return;
      
      for( int i = 0 ; i < items.length ; i++ ) {
         
         String path = null;
         
         path = items[ i ].getPath();
         
         if( path != null ) if( path.length() > 1 ) {
            
            int p1 = 0;
            int p = path.indexOf( '.' );
            if( p < 0 ) p = path.length();
            
            JMenu jm = getSubMenu( bar , path.substring( p1 , p ) );
            p1 = p + 1;
            while( p1 < path.length() ) {
               
               p = path.indexOf( '.' , p1 );
               if( p < 0 ) p = path.length();
               JMenu jm1 = getSubMenu( jm , path.substring( p1 , p ) );
               p1 = p + 1;
               jm = jm1;
               
            }
            
            jm.add( items[ i ].getItem() );

         }
      }// for


   }


   
   
   private JMenu getSubMenu( JMenuBar jm , String path ) {

      if( jm == null ) return null;
      if( path == null ) return null;
      
      for( int i = 0 ; i < jm.getMenuCount() ; i++ )
         
         if( jm.getMenu( i ).getText().equals( path ) ) return jm.getMenu( i );
      
      JMenu Men = new JMenu( path );
      jm.add( Men );
      return Men;
   }


   
   
   private JMenu getSubMenu( JMenu jm , String path ) {

      if( jm == null ) return null;
      if( path == null ) return null;
      
      for( int i = 0 ; i < jm.getItemCount() ; i++ ) {
         
         if( jm.getItem( i ) != null )
            if( jm.getItem( i ).getText().equals( path ) )
               if( jm.getItem( i ) instanceof JMenu )
                  
                  return (JMenu) jm.getItem( i );
         
      }
      
      JMenu Men = new JMenu( path );
      jm.add( Men );
      return Men;

   }

   
   
   Vector InternalPointedAts = new Vector();


   /**
    * Causes the display to be redrawn and also the data changed to reflect a
    * POINTED_AT_CHANGED event
    */
   public void redraw( String reason ) {
      

      if( ! validDataSet() ) return;

      if( reason.equals( "SELECTION CHANGED" ) ) {
         
         // update_array = new DataSetData( getDataSet() );
         // viewComp.dataChanged(update_array);
         // viewComp.getGraphJPanel().repaint();
      }
      else if( reason.equals( "POINTED AT CHANGED" ) ) {
         
         int Group = ds.getPointedAtIndex();
         float time = ds.getPointedAtX();
         if( ! eliminate( Group , time , InternalPointedAts ) ) {
            
            if( ! Float.isNaN( time ) ) 
                   viewArray.setTime( time );
            
            SelectedData2D X = (SelectedData2D) ( (RowColTimeVirtualArray) viewArray )
                     .getSelectedData( Group , time );
            
            int nrows = ( (RowColTimeVirtualArray) viewArray )
                     .getTotalNumRows();


            floatPoint2D X2D = new floatPoint2D( 1f + X.getCol() , 1f + nrows
                     - 1 - X.getRow() );
            notifyComp = false;
            viewComp.setPointedAt( X2D );
            
         }
         
         Conversions.showConversions( time , Group );

      }else if( reason.equals( IObserver.DATA_CHANGED)){
         YRange = ds.getYRange();
         Set2DObjectState();
      }
   }

   
   

   // To make sure internal events are not dealt with like external events,
   // all external notifications are saved( values are saved). If an event
   // with the same values return, this method returns false and eliminates
   // those values from the saved set.
   private boolean eliminate( int Group , float time , Vector V ) {
      

      for( int i = 0 ; i < V.size() ; i++ ) {
         
         Vector Pt = (Vector) ( V.elementAt( i ) );
         if( Pt.firstElement() instanceof Integer )
            if( ( (Integer) ( Pt.firstElement() ) ).intValue() == Group )
               if( Pt.lastElement() instanceof Float )
                  if( ( (Float) ( Pt.lastElement() ) ).floatValue() == time ) {
                     
                     V.remove( i );
                     return true;
                     
                  }

      }
      
      return false;

   }


   // Listens for action events coming from the IVirtualArray
   class ArrayActionListener implements ActionListener {

      public void actionPerformed( ActionEvent evt ) {

         Update2DObjectState();
         viewComp.dataChanged(  );
         Set2DObjectState();//Sets position of the JTable
       
         if( TagFrame != null )
            TagFrame.setNewData( (IVirtualArray2D) ( viewArray.getArray() ) ,
                     (CoordBounds) null );
         DisplayPanel = viewComp.getDisplayPanel(); 
         DisplayPanel.repaint();
         
      }
   }

   
   
   // Listens for events coming from the IViewComponent
   class CompActionListener implements ActionListener {

      public void actionPerformed( ActionEvent evt ) {


         if( evt.getActionCommand().equals( IViewComponent.POINTED_AT_CHANGED )
                  || evt.getActionCommand().equals(
                           IObserver.POINTED_AT_CHANGED ) ) {
            
            if( ! notifyComp ) {
               notifyComp = true;
               return;
            }
            
            floatPoint2D X1 = viewComp.getPointedAt();
            
            SelectedData2D X = new SelectedData2D( (int) ( X1.y + .5 ) - 1 ,
                     (int) ( X1.x + .5 ) - 1 ,
                     ( (RowColTimeVirtualArray) viewArray ).getTime( 1 , 1 ) );
            
            int Group = viewArray.getGroupIndex( X );
            float Time = viewArray.getTime( X );

            if( Group < 0 ) return;
            if( Float.isNaN( Time ) ) return;
            
            Conversions.showConversions( Time , Group );
            
            Vector V = new Vector();   
            V.addElement( new Integer( Group ) );
            V.addElement( new Float( Time ) );
            InternalPointedAts.addElement( V );
            
            ds.setPointedAtX( Time );
            ds.setPointedAtIndex( Group );
            ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
            
         }
         else if( evt.getActionCommand().equals(
                  
                  IViewComponent.SELECTED_CHANGED ) ) {
            
            // What to do with selected??? No default
            /*
             * Region[] Regs = viewComp.getSelectedRegions(); SelectedRegion2D
             * selRegion=null ; CoordTransform transform =
             * viewComp.getWorldToArrayTransform(); Vector pts = new Vector();
             * if( Regs != null) for( int i=0; i < Regs.length; i++){
             * java.awt.Point[] p = Regs[i].getSelectedPoints( transform ); if(
             * p != null) for( int k=0; k< p.length; k++) if( !pts.contains(
             * p[k] )) pts.addElement(p[k]); } int[] xs = new int[ pts.size()];
             * int[] ys = new int[ pts.size() ]; for( int i=0; i< pts.size();
             * i++){ java.awt.Point point =(java.awt.Point)pts.elementAt( i );
             * xs[i] = ( point.x -1); ys[i] = point.y-1; } selRegion= new
             * SelectedRegion2D( xs, ys);
             * ((RowColTimeVirtualArray)viewArray).SelectRegion( selRegion);
             */
            
         }

      }
   }

   
   
   /**
    * Called when any "ancestor" of this TwoDViewers is removed. Can delete
    * resources created by this TwoDViewer( like JFrames, viewComponent and
    *  the array )
    * 
    * @author Ruth
    *
    */
   class myAncestorListener extends WindowAdapter implements AncestorListener {

      public void ancestorAdded( AncestorEvent event ) {

      }

      /**
       * 
       */
      public void ancestorRemoved( AncestorEvent event ) {

         viewComp.kill();
         viewArray.kill();
         if( TagFrame != null ) {
            
            TagFrame.dispose();
            TagFrame = null;
            showtag = false;
            ShowTag.setSelected( false );
         }

      }


      public void ancestorMoved( AncestorEvent event ) {

      }


      public void windowClosing( WindowEvent e ) {

         TagFrame = null;
         showtag = false;
         ShowTag.setSelected( false );
         

      }
   }
   
  
   
   boolean JFrameCompListSet = false;
   private void setJFrameCompListener(){
      if( JFrameCompListSet )
         return;
      
      JFrame jf = GetJFrame( this );
      
      if( jf != null ) {
         
         DisplayChangedListener dispList = new DisplayChangedListener(
                                                              null );
         
         jf.addComponentListener( dispList );
         JFrameCompListSet = true;
      
      }
      
   }
   
   
   /**
    * Handles events associated with changing the View mode( image, table,
    * or Contour) and showing the GraphTagFrame
    * @author Ruth
    *
    */
   class ViewTypeActionListener implements ActionListener {

      TwoDViewers D2;


      public ViewTypeActionListener( TwoDViewers DSView ) {

         D2 = DSView;
      }

  
      
      public void actionPerformed( ActionEvent evt ) {
         
         
         
         String button = evt.getActionCommand();
         
         if( button.equals( "Image" ) ) {

         }
         else if( button.equals( "Table" ) ) {


         }
         else if( button.equals( "Contour" ) ) {


         }
         else if( button.equals( SHOW_TAG ) ) {
            showtag = ! showtag;
            ShowTag.setSelected( showtag );
            if( ! showtag  && TagFrame != null) {
               TagFrame.dispose();
               TagFrame = null;
            }else
               action();
            setJFrameCompListener();
            return;
         }
         
         D2.reInit( D2.viewComp , button );

         if( TagFrame != null ) {
            
            TagFrame.setViewerRowColRange( TwoDViewers.getRCRange( D2
                     .MarkOverLayJPanels( D2 ) , (IVirtualArray2D) ( viewArray
                     .getArray() ) ) );
            
            TagFrame
                     .actionPerformed( new ActionEvent( this , 1 ,
                              "Just get in" ) );
         }

      }
   }

   
   
   
   /**
    * This handles the event to integrate a selected area in a table
    * 
    * @author Ruth
    *
    */
   class IntegrateActionListener implements ActionListener {

      TwoDViewers viewer;


      public IntegrateActionListener( TwoDViewers viewer ) {

         this.viewer = viewer;
      }


      public void actionPerformed( ActionEvent evt ) {

         RowColTimeVirtualArray.ShowIntegrateStats(
                  (IVirtualArray2D) viewer.viewArray.getArray() ,
                  viewer.viewComp.getSelectedRegions() , viewer.viewComp
                           .getWorldToArrayTransform() );
      }

   }


   
   /**
    * Gets the enclosing JFrame for a container
    * @param cont  The container inside a JFrame
    * @return  The JFrame or null
    * NOTE: This container C cannot be further down than 20 levels 
    */
   public static JFrame GetJFrame( Container cont ) {

      if( cont == null ) return null;
      JFrame jf = null;
      
      for( int i = 0 ; i < 20 && jf == null && cont != null ; i++ ) {
         
         cont = cont.getParent();
         if( cont instanceof JFrame ) jf = (JFrame) cont;
         
      }

      return jf;

   }


   /**
    * Get the row, col for the Pointed At element in the data set
    * 
    * @param ds the data set
    * @return A vector whose first element is the col, the second element is the
    *         row, and the last element is the detector number for the pointed
    *         at element in this data set
    */
   public static Vector getPointedAtRowColDet( DataSet ds ) {

      int indx = ds.getPointedAtIndex();
      if( indx < 0 ) return null;
      Object O = ( new GetPixelInfo_op( ds , indx ) ).getResult();
      if( O instanceof Vector ) {
         return (Vector) O;
      }
      else
         return null;
   }


   /**
    * Returns the row,col range that correspond with getDataValues from the
    * VirtualArray2D
    * 
    * @param basePanel
    *           The panel with an image of row/column data
    * @param arr
    *           The VirtualArray2D that gives the corresponding values picture
    *           in the image of the basePanel
    * @return The Coord range of row cols (x-col,y row)
    */
   public static CoordBounds getRCRange( ActiveJPanel basePanel ,
            IVirtualArray2D arr ) {

      CoordBounds RC = null;
      if( basePanel instanceof gov.anl.ipns.ViewTools.Panels.Image.ImageJPanel2 ) {

         gov.anl.ipns.ViewTools.Panels.Image.ImageJPanel2 image = (gov.anl.ipns.ViewTools.Panels.Image.ImageJPanel2) basePanel;

         AxisInfo x_axis = arr.getAxisInfo( AxisInfo.X_AXIS );
         AxisInfo y_axis = arr.getAxisInfo( AxisInfo.Y_AXIS );
         CoordBounds From = new CoordBounds( x_axis.getMin() , y_axis.getMin() ,
                  x_axis.getMax() , y_axis.getMax() );


         CoordBounds To = new CoordBounds( - .5f , - .5f ,
                  arr.getNumColumns() - .5f , arr.getNumRows() - .5f );

         CoordTransform trans = new CoordTransform( From , To );
         CoordBounds zoom = image.getLocalWorldCoords();


         RC = trans.MapTo( zoom );

         RC.setBounds( RC.getX1() , RC.getY2() , RC.getX2() , RC.getY1() );


      }
      else if( basePanel instanceof gov.anl.ipns.ViewTools.Panels.Table.TableJPanel ) {

         gov.anl.ipns.ViewTools.Panels.Table.TableJPanel table = (gov.anl.ipns.ViewTools.Panels.Table.TableJPanel) basePanel;

         Rectangle R = table.getVisibleRectangle();
         RC = new CoordBounds( R.x - .5f , R.y - .5f , R.x + R.width + .5f ,
                  R.y + R.height + .5f );

      }
      else if( basePanel instanceof gov.anl.ipns.ViewTools.Panels.Contour.ContourJPanel ) {

         gov.anl.ipns.ViewTools.Panels.Contour.ContourJPanel contour = (gov.anl.ipns.ViewTools.Panels.Contour.ContourJPanel) basePanel;

         AxisInfo x_axis = arr.getAxisInfo( AxisInfo.X_AXIS );
         AxisInfo y_axis = arr.getAxisInfo( AxisInfo.Y_AXIS );
         CoordBounds From = new CoordBounds( x_axis.getMin() , y_axis.getMin() ,
                  x_axis.getMax() , y_axis.getMax() );

         CoordBounds To = new CoordBounds( - .5f , - .5f ,
                  arr.getNumColumns() - .5f , arr.getNumRows() - .5f );
         CoordTransform trans = new CoordTransform( From , To );
         CoordBounds zoom = contour.getLocalWorldCoords();
         zoom.setBounds( zoom.getX1() , zoom.getY2() , zoom.getX2() , zoom
                  .getY1() );
         RC = trans.MapTo( zoom );

         RC.setBounds( RC.getX1() , RC.getY2() , RC.getX2() , RC.getY1() );


      }

      return RC;


   }


   
   /**
    * Creates the initial TagFrame calculating and setting all the variables
    *
    */
   private void action() {

      if( ! showtag ) return;
      JFrame jf = null;

      int x_offset = 0;
      int y_offset = 0;

      int row = - 1 , col = - 1;


      Vector V = getPointedAtRowColDet( ds );
      if( V == null ) return;
      row = ( (Integer) V.firstElement() ).intValue();
      col = ( (Integer) V.elementAt( 1 ) ).intValue();
      ActiveJPanel basePanel = MarkOverLayJPanels( (JPanel) this );

      if( basePanel.getWidth() <= 0 || basePanel.getHeight() <= 0 ) return;
      Container C = basePanel;


      for( int i = 0 ; i < 25 && jf == null && C != null ; i++ ) {
         
         Container C1 = C.getParent();
         if( C1 == null )
            
            C = C1;
         
         else {
            
            x_offset += C.getX() + C.getInsets().left;
            y_offset += C.getY() + C.getInsets().top;
            C = C1;
            if( C1 instanceof JFrame ) jf = (JFrame) C1;
            
         }
      }
      
      if( jf == null ) return;

      CoordBounds pixCoord = new CoordBounds( x_offset , y_offset , x_offset
               + basePanel.getWidth() , y_offset + basePanel.getHeight() );

      CoordBounds RC = getRCRange( basePanel , (IVirtualArray2D) ( viewArray
               .getArray() ) );

      if( RC == null ) return;

      if( TagFrame == null ) {
         
         TagFrame = new GraphTagFrame( jf , pixCoord , RC ,
                  (IVirtualArray2D) ( viewArray.getArray() ) , (float[]) null ,
                  row , col , - 1 );
         
         TagFrame.addWindowListener( new myAncestorListener() );
         gov.anl.ipns.Util.Sys.WindowShower.show( TagFrame );

      }
      else {
         
         return;

      }


   }

   GraphTagFrame TagFrame = null;

   /**
    *  Handles the events that cause the TagFrame to change 
    * @author Ruth
    *
    */
   class DisplayChangedListener extends ComponentAdapter implements ActionListener {

      
      TwoDViewers    d2View;


      public DisplayChangedListener( TwoDViewers d2View ) {

         
         this.d2View = d2View;
      }


      public void actionPerformed( ActionEvent evt ) {

         if( TagFrame == null ) {
            action();
            return;
         }
         
         if( evt.getActionCommand().equals( IObserver.POINTED_AT_CHANGED )
                  || evt.getActionCommand().equals( CoordJPanel.CURSOR_MOVED ) ) {
            

                        
            Vector V = getPointedAtRowColDet( ds );
            int col = ( (Integer) ( V.firstElement() ) ).intValue();
            int row = ( (Integer) ( V.elementAt( 1 ) ) ).intValue();
            
            float t = ds.getPointedAtX();
            int indx = ds.getPointedAtIndex();
            if( indx < 0 ) return;
            int timeChan = - 1;
            
            Data D = ds.getData_entry( indx );
            XScale xsc = D.getX_scale();
            
            if( ! Float.isNaN( t ) && t > 0 ) timeChan = xsc.getI( t );
            
            row = ( (IVirtualArray2D) ( viewArray.getArray() ) ).getNumRows()
                     - row + 1;
            
            TagFrame.setPointedAtChanged( row , col , timeChan , D
                     .getY_values() );

            return;
            
         }
         
         if( evt.getActionCommand() == IObserver.SELECTION_CHANGED ) return;
         boolean change = false;
         
         if( evt.getActionCommand() == CoordJPanel.ZOOM_IN
                  || evt.getActionCommand() == CoordJPanel.RESET_ZOOM ) {
            
            if( evt.getSource() instanceof ActiveJPanel ) {
               
               CoordBounds RC = getRCRange( (ActiveJPanel) evt.getSource() ,
                        (IVirtualArray2D) ( viewArray.getArray() ) );
               
               if( RC == null ) return;
               TagFrame.setViewerRowColRange( RC );
               return;
               
            }

         }
         
         if( evt.getActionCommand().equals( "STATE_CHANGED" ) ) change = true;

         if( change ) action();
      }


      public void componentResized( ComponentEvent e ) {

         if( TagFrame == null ) {
            
            action();
            return;
            
         }
      
         if( e.getSource() instanceof ActiveJPanel ) {
            
            CoordBounds RC = new CoordBounds();
            JFrame jf = GraphTagFrame.getJFrameWsubPixelBounds( (Container) e
                     .getSource() , RC );
            
            if( jf == null ) return;

            TagFrame.setViewerPixelRange( jf , RC );
         } /*
             * else if( e.getSource() instanceof JFrame){ JFrame jf =
             * (JFrame)(e.getSource()); ActiveJPanel jj =
             * d2View.MarkOverLayJPanels((JComponent) jf.getContentPane());
             * CoordBounds pixCoords = new CoordBounds(); if(jf ==
             * getJFrameWsubPixelBounds( jj , pixCoords)){
             * TagFrame.setViewerPixelRange( (JFrame)e.getSource(), pixCoords); }
             *  }
             */


      }


      public void componentMoved( ComponentEvent e ) {

         if( TagFrame == null ) {
            
            action();
            return;
            
         }
 
         if( ! ( e.getSource() instanceof JFrame ) ) return;
         TagFrame.SetTag2FrameChanged( (JFrame) ( e.getSource() ) );

      }

   }


   /**
    * 
    * Test program for this module
    * 
    * @param args None used
    */
   public static void main( String args[] ) {

      DataSet[] DSS = null;
      try {
         
         DSS = ScriptUtil.load( "C:/Isaw/SampleRuns/SCD06496.RUN" );
         
      }
      catch( Exception ss ) {
         
         System.exit( 0 );
         
      }
      
      
      DataSet DS = DSS[ DSS.length - 1 ];
      FinishJFrame jf = new FinishJFrame( "Test" );
      
      jf.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
      jf.setSize( 500 , 500 );
      
      TwoDViewers dsv = new TwoDViewers( DS , null );
      jf.getContentPane().add( dsv );

      jf.setJMenuBar( dsv.getMenuBar() );
      jf.invalidate();
      WindowShower.show( jf );

      
   }


  /* ------------------------- getDisplayComponent -------------------------- */
  /**
   *  Get the JComponent that contains the image of the data, without
   *  any associated controls or auxillary displays.
   */
   public JComponent getDisplayComponent()
   {
     if ( viewComp != null )
       return viewComp.getDisplayPanel();
     else
       return this;
   }

   
   //-------------------- IPreserveState Methods & Variables-----------------
   ObjectState Ostate = null;


   public void setObjectState( ObjectState new_state ) {

      Ostate = new_state;

      String newState = (String)new_state.get("ViewType");
      if( newState .equals( currentViewType))
         viewComp.setObjectState( (ObjectState)Ostate.get("View"+currentViewType));
      reInit( viewComp, newState);
      
      currentViewType = Ostate.get( "ViewType" ).toString();
      


   }


   public ObjectState getObjectState( boolean is_default ) {

      if( Ostate == null ) 
         Ostate = new ObjectState();

      ObjectState state = new ObjectState();
      
      if( ! is_default ) 
         state = Ostate;

      ViewerState st = new ViewerState();
      
      if( ! is_default ) 
         st = this.state;

      state.reset( "Data" , st );

      state.reset("ViewType", currentViewType);
      
      if(!state.reset( "View" + currentViewType , viewComp
               .getObjectState( is_default ) ))
          state.insert( "View" + currentViewType , viewComp
                  .getObjectState(true ));
      
      ActiveJPanel jp = MarkOverLayJPanels( DisplayPanel );
      
      if( jp == null)
         return  state;
    
      if( ! is_default ){         
         
         state.reset( "ViewType" , currentViewType );                
      
      } else{
         
         state.insert( "ViewType" , "Image" );
         
      }
      return state;

   }
   
  private void Update2DObjectState(){
    Ostate.reset("ViewType" , currentViewType);
    ActiveJPanel jp = MarkOverLayJPanels( DisplayPanel );
    if( !(jp instanceof TableJPanel ))
       return;
    ObjectState O= ((IPreserveState)jp).getObjectState( false);
    Object Or = O.get( "TableJPanel.Viewport_Position");
    if( Or instanceof String)
       TableTopLeft = null;
    else
       TableTopLeft = (Point)Or;
    Rectangle R  = ((TableJPanel)jp).getVisibleRectangle();
    TableTopLeft = new Point( R.x, R.y);
    
  }
  
  
  private void UpdateDataObjectState(){
     if( state == null)
        state = new ViewerState();
     
     Ostate.reset( "Data" , state);

    
     
  }
  
  private void UpdateViewObjectState(){
     if( viewComp instanceof IPreserveState)
        
     if(!Ostate.reset( "View"+currentViewType, 
                 ((IPreserveState)viewComp).getObjectState( false )))
        
        Ostate.insert( "View"+currentViewType, 
                 ((IPreserveState)viewComp).getObjectState( true ));
     
  }
  
  
 
  
  private void SetDataObjectState(){
     
     state = (ViewerState)Ostate.get("Data");
     currentViewType = (String)Ostate.get( "ViewType");
     
     
  }
  
  private void SetViewObjectState(){

     if( viewComp instanceof IPreserveState){
          ObjectState O = (ObjectState)Ostate.get( "View"+currentViewType  ); 
          if( O != null)
             viewComp.setObjectState(O );
     }
  }
  
  private void Set2DObjectState(){
     currentViewType = (String)Ostate.get("ViewType" );
     

     ActiveJPanel jp = MarkOverLayJPanels( DisplayPanel );
     if( jp == null)
        return;
     if( jp instanceof TableJPanel){
     if( TableTopLeft == null)
        return;
     if( !currentViewType.equals( "Table"))
        return;
     

    
     if( !(jp instanceof TableJPanel ))
        return;
     
     ObjectState O= ((IPreserveState)jp).getObjectState( false);
     if( O == null)
        return;
     
     if( !O.reset( "TableJPanel.Viewport_Position", TableTopLeft))
        O.insert( "TableJPanel.Viewport_Position", TableTopLeft);
     
     ((IPreserveState)jp).setObjectState(  O );
     ((TableJPanel)jp).setVisibleLocation( TableTopLeft );
     }else if( jp instanceof ImageJPanel2){
        ImageJPanel2 IPanel = (ImageJPanel2)jp;
        ((ImageViewComponent)viewComp).preserveAspectRatio( true );
        IPanel.enableAutoDataRange( false );
        IPanel.setDataRange( YRange.getStart_x(), YRange.getEnd_x());
     }
     
   }
}


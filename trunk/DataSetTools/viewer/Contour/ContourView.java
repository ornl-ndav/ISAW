/*
 * File: ContourView.java
 *
 * Copyright (C) 2002, Rion Dooley
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
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
 *  Revision 1.38  2003/12/15 00:42:08  rmikk
 *  Eliminated some commented out code
 *  Now redraws when the selected groups of the data set are changed
 *
 *  Revision 1.37  2003/11/25 20:12:03  rmikk
 *  Added a Save Image as a submenu of the File Menu
 *
 *  Revision 1.36  2003/10/30 21:10:26  dennis
 *  Removed unneeded import of gov.noaa.pmel.sgt.demo, which
 *  caused a problem with javadoc 1.4.2_02
 *
 *  Revision 1.35  2003/10/15 03:56:37  bouzekc
 *  Fixed javadoc errors.
 *
 *  Revision 1.34  2003/09/04 15:01:38  rmikk
 *  2nd Detector now works
 *
 *  Revision 1.33  2003/08/28 15:03:27  rmikk
 *  Added code to incorporate Several Area Detectors
 *
 *  Revision 1.32  2003/08/12 14:31:47  rmikk
 *  Added the SaveDataSetActionListener Menu item to save
 *    the current data set
 *
 *  Revision 1.31  2003/07/14 20:31:30  rmikk
 *  -Fixed error when Xscale is rebinned
 *
 *  Revision 1.30  2003/06/18 20:37:10  pfpeterson
 *  Changed calls for NxNodeUtils.Showw(Object) to
 *  DataSetTools.util.StringUtil.toString(Object)
 *
 *  Revision 1.29  2003/05/28 20:55:44  pfpeterson
 *  Changed System.getProperty to SharedData.getProperty
 *
 *  Revision 1.28  2003/05/19 15:18:55  rmikk
 *  -Added an addControl method for subclasses to add
 *      their own specialized controls to the control panel
 *  -Updated code to work for cases where axes are linear
 *    combinations of other base axes.
 *
 *  Revision 1.27  2003/05/12 16:01:41  rmikk
 *  Removed some commented out code.
 *  Eliminated a redraw when a mouse is clicked on the
 *    corresponding view
 *  Fixed code to work with the axes cases( non row-col-time)
 *    interactively
 *
 *  Revision 1.26  2003/05/05 17:50:18  dennis
 *  Made JPlotLayout rpl_ an instance variable, rather than a static
 *  class variable.  This fixes a problem when multiple contour views
 *  were used simultaneously.
 *
 *  Revision 1.25  2003/05/02 19:20:40  dennis
 *  Now uses ClosedInterval.niceGrid() method to calculate values to
 *  use for contour levels.
 *
 *  Revision 1.24  2003/03/19 19:35:50  rmikk
 *  Added a Menu option( Edit/SpreadSheet/Intensity)
 *   on the viewer to calculate the Intensity of the zoomed
 *   in area.
 *
 *  Revision 1.23  2002/11/27 23:24:29  pfpeterson
 *  standardized header
 *
 *  Revision 1.22  2002/11/25 13:51:07  rmikk
 *  The panel containing the SGT ContourPlot now is a CoordJPanel with all 
 *  the cursor controls.  Expanding the split pane with the ContourPlot 
 *  now works.
 *
 *  Revision 1.21  2002/10/18 19:05:34  rmikk
 *  Fixed the notify system to be more robust
 *
 *  Revision 1.20  2002/10/14 19:52:11  rmikk
 *  Converted the private setData method to a protected method
 *    so subclasses can recalculate the data
 *
 *  Revision 1.19  2002/10/07 16:17:32  rmikk
 *  Fixed a missing parenthesis
 *
 *  Revision 1.18  2002/10/07 15:01:24  rmikk
 *  Introduced a constructor that used an IAxesHandler
 *
 *  Revision 1.17  2002/09/25 14:00:19  rmikk
 *  Defined state variable Contour.Intensity to eliminate error
 *    messages
 *
 *  Revision 1.16  2002/08/30 18:08:13  rmikk
 *  -Eliminated reference to a test operator ChgOp in the
 *   main program
 *
 *  Revision 1.15  2002/08/30 15:33:47  rmikk
 *    -Used the Range of Y values(Contour intensities) from ContourData.java
 *    -Fixed some Units indicators
 *
 *  Revision 1.14  2002/08/23 13:51:08  rmikk
 *  -Eliminated some dead code
 *  -Added a color scale ( Need to elongate JFrame to see it
 *     sometimes)
 *  -Fixed the aspect ratio problem
 *
 *  Revision 1.13  2002/08/21 15:42:36  rmikk
 *  -If pointedAtX is NaN(undefined) defaults to 0th frame
 *  - Title of Contour Plot is now the title of the data set
 *  - Contour levels now "rounded" to last two or one differing digits 
 *    base 10. There should be approximately 10 contour levels.
 *
 *  Revision 1.12  2002/08/02 19:34:48  rmikk
 *  main program Converts to Q with 100 bins
 *
 *  Revision 1.11  2002/08/02 13:31:16  rmikk
 *  If the POINTED_AT_X is negative tha action controller
 *    now is initialized to time 0.
 *
 *  Revision 1.10  2002/08/01 13:49:38  rmikk
 *  Sped up the display when the intensity slider was changed.
 *  Implemented hooks to display countours with arbitrary
 *    axes handlers
 *
 *  Revision 1.9  2002/07/30 14:37:10  rmikk
 *  Added the XScaleChooser to the control panel
 *
 *  Revision 1.8  2002/07/24 23:01:06  rmikk
 *  Fixed code so the Contour display moves with outside
 *    POINTED_AT events
 *
 *  Revision 1.7  2002/07/24 15:24:25  rmikk
 *  Fixed so input POINTED_AT events do not send out
 *    POINTED_AT events.
 *
 *  Revision 1.6  2002/07/23 22:09:05  rmikk
 *  Implemented the PointedAt features to integrate with
 *    other viewers.
 *
 *  Revision 1.5  2002/07/15 22:13:33  rmikk
 *  Added an intensity slider
 *
 *  Revision 1.4  2002/07/12 21:14:07  rmikk
 *  Changed documentation to get log messages to record
 *    automatically
 *  Incorporated a log scale for colors
 *  Eliminated some debug prints
 *
 */
package DataSetTools.viewer.Contour;

import gov.noaa.pmel.sgt.swing.JPlotLayout;
import gov.noaa.pmel.sgt.swing.JClassTree;
import gov.noaa.pmel.sgt.swing.prop.GridAttributeDialog;

import gov.noaa.pmel.sgt.dm.SGTData;

import gov.noaa.pmel.sgt.*;

import gov.noaa.pmel.util.*;

import java.awt.*;
import java.awt.print.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.viewer.util.*;
import DataSetTools.components.image.*;
import DataSetTools.components.containers.*;
import DataSetTools.instruments.*;
import DataSetTools.viewer.*;
import DataSetTools.math.*;
import DataSetTools.components.containers.*;
import DataSetTools.components.ThreeD.*;
import DataSetTools.components.ui.*;
import DataSetTools.retriever.*;
import DataSetTools.operator.*;
import gov.noaa.pmel.sgt.dm.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.components.image.*;

/**
 * Example demonstrating how to use <code>JPlotLayout</code>
 * to create a raster-contour plot.
 * 
 * @author Donald Denbo
 * @version $Revision$, $Date$
 * @since 2.0
 */
public class ContourView extends DataSetViewer
{
   JPlotLayout rpl_ = null;
   private GridAttribute gridAttr_ = null;
  // JButton edit_;
   boolean rpldrawn  = false;
   JButton space_ = null;
   AnimationController ac;
   JMenuItem tree_;
   DataSetXConversionsTable dct;
   JPanel ConvTableHolder = null;
   CoordJPanel rpl_Holder = null;
   JScrollPane dctScroll = null;
   XScaleChooserUI Xscl = null;
   JPanel  XsclHolder = null;
   JPanel cdControlHolder = null;
   boolean acChange,XsclChange,XConvChange;
   //JButton print_;
   ContourData cd;
   JSlider time_slider;
   SGTData newData;
   JPane gridKeyPane;
   DataSet data_set;
   int sliderTime_index;
   IAxisHandler  axis1,axis2,axis3;
   
   float[] times;
   int ncolors = 200;
   public int nlevels = 10;
   ViewerState state = null;  
   JLabel time_label = new JLabel( "" );
   // Use the correct one ---------------
   //JPanel main; 
   SplitPaneWithState main = null;
   //JSplitPane main = null;
  // JPanel main= null;
   //---------------------
   JSlider intensity = null;
   JPanel intensitHolder = null;
   int PrevGroup;
   boolean notify = true, stop=false;
   float note_time;
   float note_group; 
   int index_mouse = -1; //used to make sure a mouse click event is not
   float time_mouse =Float.NaN; //sent back to redraw
   protected IAxesHandler Transf;
   MyMouseListener cursors;
   SaveDataSetActionListener SaveDS;
  public ContourView( DataSet ds, ViewerState state1 )
     { this( ds, state1, null,null,null);
      }
  public ContourView( DataSet ds, ViewerState state, IAxesHandler Transf)
     {this( ds, state,Transf.getAxis(0), Transf.getAxis(1), Transf.getAxis(2));
      this.Transf = Transf;
      }
  public ContourView( DataSet ds, ViewerState state1 ,IAxisHandler axis1,
                         IAxisHandler axis2, IAxisHandler axis3)
    {

     super( ds, state1 );  // Records the data_set and current ViewerState
     this.axis1 = axis1;
     this.axis2 = axis2;
     this.axis3 = axis3;
     data_set = ds;
     Transf = null;
     state = state1;
     SaveDS = new SaveDataSetActionListener(ds);
     SaveDS.setUpMenuItem( getMenuBar());
     if( !validDataSet() )
       {
        return;
       }
     sliderTime_index = 0;
     cursors = new MyMouseListener( this );
     PrevGroup = ds.getPointedAtIndex();
     if( state1 == null)
       {state = new ViewerState();
        state.set_int ("Contour.Intensity",50);
         
       }
       
   //Create the Menu bar items
     JMenuBar menu_bar = getMenuBar();
     JMenu jm = menu_bar.getMenu( DataSetViewer.EDIT_MENU_ID );

     tree_ = new JMenuItem( "Graph" );
     jm.add( tree_ );
     tree_.addActionListener( new MyAction() );

     JMenu SprdSheet  = new JMenu( "SpreadSheet");
     jm.add( SprdSheet);
     JMenuItem Intensity = new JMenuItem( "Intensity");
     SprdSheet.add( Intensity);
     Intensity.addActionListener( new IntensityListener() );

     DataSetTools.viewer.PrintComponentActionListener.setUpMenuItem( menu_bar, this );
     
     DataSetTools.viewer.SaveImageActionListener.setUpMenuItem( menu_bar, this);
     jm = menu_bar.getMenu( DataSetViewer.OPTION_MENU_ID );
     jm.add( new ColorScaleMenu( new ColorActionListener() ) );

     JMenuItem jmi;
     JMenu cont_style=new JMenu("Contour Style");
     jm.add( cont_style);
      /*jmi = new JMenuItem("AREA_FILL");
     cont_style.add( jmi);
     jmi.addActionListener( new MyContStyleListener());

     jmi = new JMenuItem("AREA_FILL_CONTOUR");
     cont_style.add( jmi);
     jmi.addActionListener( new MyContStyleListener());
     */
     jmi = new JMenuItem("RASTER_CONTOUR");
     cont_style.add( jmi);
     jmi.addActionListener( new MyContStyleListener());

     jmi = new JMenuItem("CONTOUR");
     cont_style.add( jmi);
     jmi.addActionListener( new MyContStyleListener());

     jmi = new JMenuItem("RASTER");
     cont_style.add( jmi);
     jmi.addActionListener( new MyContStyleListener());

     ac = new AnimationController();

     ac.addActionListener( new MyAction() );
      
      
     boolean b= state.get_boolean(ViewerState.CONTOUR_DATA);
     
     if( !b)
       {setTimeRangeStateValues( getDataSet(),true);
       }
     Xscl = new XScaleChooserUI( getDataSet().getX_label(), getDataSet().getX_units(),
                        state.get_float("ContourTimeMin"), state.get_float("ContourTimeMax"),
                        state.get_int("ContourTimeStep"));

     Xscl.addActionListener( new MyXSCaleActionListener());
     XsclHolder = new JPanel( new GridLayout( 1,1));
     XsclHolder.add( Xscl);
     acChange = true;
     XsclChange = true;
     XConvChange = true;
     setData( data_set, state.get_int( ViewerState.CONTOUR_STYLE) );
     //Add the components to the window
     JPanel jpEast = new JPanel();

     BoxLayout blay = new BoxLayout( jpEast, BoxLayout.Y_AXIS );

     jpEast.setLayout( blay );
      

     intensity = new JSlider( 0, 100);
     intensity.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(),
                        "Intensity") );
     
     intensity.addChangeListener( new MyChange( this ));
     intensityHolder = new JPanel( new GridLayout(1,1));
     intensityHolder.add( intensity);

     
      
     rpl_Holder = new CoordJPanel();
     rpl_Holder.setLayout(new GridLayout(1,1));//MyJPanel( rpl_, Color.white);
     rpl_Holder.add( rpl_);
     rpl_Holder.addActionListener( new CoordJPanelActionListener());
     setLayout(jpEast );
    
      
     setLayout( new GridLayout( 1,1));
     rpl_Holder.addComponentListener( new MyComponentListener() );
     add( main);
     validate();
     rpldrawn=false;
    }

  private  void setTimeRangeStateValues(DataSet ds, boolean setToZero){

       
        UniformXScale xx = ds.getXRange();
        state.set_float("ContourTimeMin", xx.getStart_x());
        state.set_float("ContourTimeMax",xx.getEnd_x());
        if( setToZero)
          state.set_int( "ContourTimeStep" , 0 );
        
        state.set_boolean(ViewerState.CONTOUR_DATA, true);
        state.set_int("Contour.Intensity", 50);

  }

  class MyMouseMotionListener implements MouseMotionListener
  {
   public void mouseDragged(MouseEvent e)
   {
    rpl_Holder.dispatchEvent( e);

    }

   public void mouseMoved(MouseEvent e)
   {
    rpl_Holder.dispatchEvent( e);
    }



  }//MyMouseLotionListener
  JPanel NewBoxLayoutJPanel(){
      JPanel cdControlHolder = new JPanel();
     
     BoxLayout blay = new BoxLayout( cdControlHolder, BoxLayout.Y_AXIS );

     cdControlHolder.setLayout( blay );
     return cdControlHolder; 

  }
  boolean addControls( ContourData cd, JPanel cdControlHolder){
     JComponent[] controls = cd.getControls();
     if( controls == null) return false;
     if( controls.length < 1) return false;
     for( int i = 0; i <  controls.length; i++){
       cdControlHolder.add( controls[i]);
     }
     return true;

  }
  // main splitpane with state, 
  public void setLayout(JPanel jpEast)
    {jpEast.add( ac );
     jpEast.add(XsclHolder);
     jpEast.add(intensityHolder);
     if( cdControlHolder == null)
       cdControlHolder = NewBoxLayoutJPanel();
     else
       cdControlHolder.removeAll();
     
      addControls(  cd, cdControlHolder);
     jpEast.add(cdControlHolder);
     //cd.addDataChangeListener( new DataChangeListener());
     addControl( jpEast);
     jpEast.add( ConvTableHolder );
     jpEast.add( Box.createHorizontalGlue() );
     main = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,  rpl_Holder,  jpEast, .70f);
    }

   class DataChangeListener implements ActionListener{
      public void actionPerformed( ActionEvent evt){
      if( cd == null) return;
      if( times == null) return;
      if( sliderTime_index < 0)
        sliderTime_index =0;
      if( times.length < 1) return;
      
      SimpleGrid newData1 = ( SimpleGrid )( cd.getSGTData( times[sliderTime_index] ) );

      ( ( SimpleGrid )newData ).setZArray( newData1.getZArray() );
      rpl_.draw();

     }
   }
   public  void addControl( JPanel jpanel)
    {

     
     }
   //Change the name of doLayou when changing this
   //Cannot get the column 2 items to draw correctly.
  JPanel acHolder, intensityHolder;
  public void setLayout2( JPanel jpEast)
    {//main = new JPanel();
     main.setLayout( null);
     acHolder = new MyJPanel(  ac ,Color.blue);//new JPanel( new GridLayout( 1,1));//
     intensityHolder =new JPanel( new GridLayout( 1,1));
      
     acHolder.add( ac);
     intensityHolder.add( intensity);
       
    }

  class CoordJPanelActionListener implements ActionListener
    {
      public void actionPerformed( ActionEvent evt )
        { if( rpl_Holder == null) 
            return;
         if( evt.getActionCommand().equals(CoordJPanel.CURSOR_MOVED))
          { Point P = rpl_Holder.getCurrent_pixel_point() ;
            cursors.mouseClicked( new MouseEvent(rpl_, MouseEvent.MOUSE_CLICKED,
                          (long)0,0,P.x,P.y,1,false));
          }
          else if( evt.getActionCommand().equals(CoordJPanel.ZOOM_IN))
          { Rectangle R = rpl_Holder.getZoom_region();
           
            if( R == null )
              return;
            
            gov.noaa.pmel.sgt.Layer L = rpl_.getFirstLayer();
            gov.noaa.pmel.sgt.Graph g = L.getGraph();
        
            if( !( g instanceof CartesianGraph ) )
               return;
            CartesianGraph cg = ( CartesianGraph )g;

            rpl_.setBatch(true);
            double xl = cg.getXPtoU( L.getXDtoP( R.x ) );
            double yl = cg.getYPtoU( L.getYDtoP( R.y ) );
            double xr = cg.getXPtoU( L.getXDtoP( R.x+R.width ) );
            double yr = cg.getYPtoU( L.getYDtoP( R.y +R.height) );
            double y;
            if( yl > yr)
              {y = yl;
               yl = yr;
               yr = y;
              }
          
            try{
              rpl_.setRange( new Domain(new Range2D(xl,xr), new Range2D(yl,yr)));
                }
            catch( Exception sss)
              {System.out.println("could not set the range"+sss);
              }
            Component[] comps = rpl_.getComponents();
            Layer ly;
            for(int i=0; i < comps.length; i++) {
              if(comps[i] instanceof Layer) {
                ly = (Layer)comps[i];
                ((CartesianGraph)ly.getGraph()).setClip(xl, xr,
                                                  yl, yr);
               
              }
             }
           
            rpl_.setBatch(false);
            rpl_.draw();
          }
          else if(evt.getActionCommand().equals(CoordJPanel.RESET_ZOOM))
          { rpl_.resetZoom();
          }

         }
     }//CoordJPanelActionListener()
  public void doLayout2()
    {if( acHolder == null)
       {super.doLayout();
        
        return;
       }
     System.out.println("in doLayout Bounds ="+getBounds());
     Rectangle R = getBounds();
     if( R.width <=0) 
        return;
     if(R.height <=0) 
        return;
     int R1 = (int)(R.width*.7);
     rpl_Holder.setBounds     ( new Rectangle( 0,    0,                (int)(R.width*.7),
                         R.height) );
     acHolder.setBounds       ( new Rectangle(R1+1,   0,                 R.width-R1-2, 
                   (int) (R.height*.2)));
     
   
     rpl_Holder.doLayout();
   

     main.add( rpl_Holder);
     main.add( acHolder);
     rpl_Holder.doLayout();
     acHolder.doLayout();
    }


  //Will attempt to do box layout horizontally. NOPE cannot do this
  public void setLayout3( JPanel jpEast)
    {jpEast.add( ac );
     jpEast.add(intensity);  
     jpEast.add( ConvTableHolder );
     jpEast.add( Box.createHorizontalGlue() );

     BoxLayout bl = new BoxLayout(main, BoxLayout.X_AXIS );
     //main = new JPanel();
     main.setLayout( bl);
     main.add(rpl_Holder);
     main.add( jpEast);
    }


  public ViewerState getState()
    {return state;
    }



  protected void setData( DataSet ds, int GridContourAttribute )
    {  
     if( axis1 == null)
        cd = new ContourData( ds, state );
     else if( Transf == null)
        cd = new ContourData( ds, axis1, axis2, axis3);
     else
       { 
         axis1 = Transf.getAxis( 0 );
         axis2 = Transf.getAxis( 1 );
         axis3 = Transf.getAxis( 2);
         cd = new ContourData( ds, axis1, axis2, axis3);
        }
      cd.addDataChangeListener( new DataChangeListener());
    if( cdControlHolder == null)
      cdControlHolder = NewBoxLayoutJPanel();
    else cdControlHolder.removeAll();
    addControls( cd, cdControlHolder);
    if( XsclHolder == null)
        {
          XsclHolder = new JPanel( new GridLayout( 1,1 ));
          XsclChange = true;
         }
    else if( XsclChange)
        XsclHolder.remove( Xscl);

     
     if( XsclChange)
       { 
        setTimeRangeStateValues( ds, true );// the data has changed
       
        Xscl = new XScaleChooserUI( ds.getX_label(), ds.getX_units(),
                 state.get_float("ContourTimeMin"), state.get_float("ContourTimeMax"),
                  state.get_int("ContourTimeStep"));

        Xscl.addActionListener( new MyXSCaleActionListener());
        XsclHolder.add( Xscl);
      }

     cd.setXScale( Xscl.getXScale() );
     times = cd.getTimeRange();
     
     if( (ac != null) && (acChange  ||  XsclChange) )
       {ac.setFrame_values( times );
        if( Float.isNaN( ds.getPointedAtX()))
          {ac.setFrameNumber( 0 );
               
          }
        else
           ac.setFrameValue( ds.getPointedAtX());
        sliderTime_index = ac.getFrameNumber();
        if( axis3 != null)
           ac.setBorderTitle( axis3.getAxisName());  
          
       }
     if( ConvTableHolder == null )
        ConvTableHolder = new JPanel( new GridLayout( 1, 1 ) );
     else if( XConvChange)
        ConvTableHolder.remove( dctScroll );

 
     
     
     if( XConvChange )
       {
        dct = new DataSetXConversionsTable( getDataSet() );
        if( Xscl.getXScale() != null)
             dct.showConversions(getDataSet().getPointedAtX(), 
                              getDataSet().getPointedAtIndex(), Xscl.getXScale());
        else 
             dct.showConversions(getDataSet().getPointedAtX(), 
                              getDataSet().getPointedAtIndex());
          
        dctScroll = new JScrollPane( dct.getTable() );
        ConvTableHolder.add( dctScroll );
       }

     init( ds, GridContourAttribute );
    }


  /** GridContourAttribute is GridAttribute.RASTER_CONTOUR or GridAttribute.CONTOUR, etc
   *  only creates rpl_ the JPlotLayout
  */
  private void init( DataSet ds, int GridContourAttribute )
    {

     /*
      * Create JPlotLayout and turn batching on.  With batching on the
      * plot will not be updated as components are modified or added to
      * the plot tree.
      */
     if( ac != null )
       {
        if( axis3 == null)
          {ac.setBorderTitle( data_set.getX_label() );
           ac.setTextLabel( data_set.getX_units() + "=" );
          }
      else
        {ac.setBorderTitle( axis3.getAxisName());
         ac.setTextLabel( axis3.getAxisUnits()+"=");
         }
       }
     state.set_int("Contour.Style", GridContourAttribute);
     if( ( main != null ) )
        if( rpl_Holder != null)  
          {
           rpl_Holder.remove( rpl_);
          }
        else 
           DataSetTools.util.SharedData.addmsg("main != null && rpl_Holder is null");
     rpl_ = this.makeGraph( times[sliderTime_index], state );
      
   
     rpl_.addMouseListener( cursors );
 
     /*
      * Turn batching off. JPlotLayout will redraw if it has been
      * modified since batching was turned on.
      */
     //rpl_.setBatch(false);
     if( main != null )
        if( rpl_Holder != null)
          {rpl_Holder.add( rpl_);
           //main.setLeftComponent( rpl_ );
           validate();
          }
      

    }


  public void setDataSet( DataSet ds )
   {
      super.setDataSet( ds );
      data_set = ds;
      SaveDS.setDataSet( ds);
      int GridAt = state.get_int("Contour.Style");
      acChange = XConvChange = XsclChange = true;
      setData( ds, GridAt );
      rpl_.draw();
   }

  class ColorActionListener implements ActionListener
    {
     public void actionPerformed( ActionEvent evt )
      { 
         if( rpl_ == null )
            return;
         if( !rpl_.isDisplayable() )
            return;
         if( gridAttr_ == null )
            return;
         if( state == null )
            state = new ViewerState();
         state.set_String( ViewerState.COLOR_SCALE, evt.getActionCommand() );
         acChange= XsclChange = XConvChange = false;
        
         setData(getDataSet(),state.get_int("Contour.Style"));
         rpl_.draw();
      }
   }

  class MyContStyleListener implements ActionListener
    {
     public void actionPerformed( ActionEvent evt)
       {acChange= XsclChange = XConvChange = false;
      
        if( evt.getActionCommand().equals("AREA_FILL"))
           state.set_int("Contour.Style",GridAttribute.AREA_FILL );
        else if( evt.getActionCommand().equals("AREA_FILL_CONTOUR"))
           state.set_int("Contour.Style",GridAttribute.AREA_FILL_CONTOUR );
        else if( evt.getActionCommand().equals("RASTER_CONTOUR"))
           state.set_int("Contour.Style",GridAttribute.RASTER_CONTOUR );
        else if( evt.getActionCommand().equals("CONTOUR"))
           state.set_int("Contour.Style",GridAttribute.CONTOUR );
        else if( evt.getActionCommand().equals("RASTER"))
           state.set_int("Contour.Style",GridAttribute.RASTER );
        setData( data_set , state.get_int("Contour.Style"));
        rpl_.draw();
       }
    }

 
 /* void edit_actionPerformed( java.awt.event.ActionEvent e )
   {

      
      GridAttributeDialog gad = new GridAttributeDialog();

      gad.setJPane( rpl_ );
      CartesianRenderer rend = ( ( CartesianGraph )rpl_.getFirstLayer().getGraph() ).getRenderer();

      gad.setGridCartesianRenderer( ( GridCartesianRenderer )rend );
      //        gad.setGridAttribute(gridAttr_);
      gad.setVisible( true );
   }
  */

  void tree_actionPerformed( java.awt.event.ActionEvent e )
   {

      /*
       * Create a JClassTree for the JPlotLayout objects
       */
      JClassTree ct = new JClassTree();

      ct.setModal( false );
      ct.setJPane( rpl_ );
      ct.show();
   }


  /*void slider_changePerformed( javax.swing.event.ChangeEvent event )
   {

      
      time_label.setText( "" + times[time_slider.getValue()] );

      if( main == null || rpl_ == null || cd == null )
         return;
      SimpleGrid newData1 = ( SimpleGrid )( cd.getSGTData( times[time_slider.getValue()] ) );

      ( ( SimpleGrid )newData ).setZArray( newData1.getZArray() );
      rpl_.draw();

      
   }
  */
  private String getMainTitle()
    {DataSet ds = getDataSet();
     return ds.getTitle();

    }
  private int getLeadDigPos( double val)
    {if( val == 0)
        return 0;
     if( val < 0)
        val = -val;
     int r = (int)( java.lang.Math.log(val)/java.lang.Math.log(10));
     return r;
       
    }


  public static double[] clevelsFrom( ClosedInterval YRange)
    {
      float min = YRange.getStart_x();
      float max = YRange.getEnd_x();

      if ( (max - min) < 10 )      // just use one contour level at half max
      {
        double values[] = new double[1];
        values[0] = max/2;
        return values;
      }

      // use more levels if we have a large number of counts, use 
      int n_levels = (int)(3 * Math.sqrt( max-min ));
 
      float grid_pts[] = YRange.niceGrid(n_levels);
  
      double values[] = new double[ grid_pts.length ]; 
      for ( int i = 0; i < values.length; i++ )
        values[i] = grid_pts[i];
 
      return values;
    }


  public JPlotLayout makeGraph( float time, ViewerState state )
   {

      /*
       * Here we use a pre-created "Layout" for raster time
       * series to simplify the construction of a plot. The
       * JPlotLayout can plot a single grid with
       * a ColorKey, time series with a LineKey, point collection with a
       * PointCollectionKey, and general X-Y plots with a
       * LineKey. JPlotLayout supports zooming, object selection, and
       * object editing.
       */


      JPlotLayout rpl;
      ContourLevels clevels;

      //cd = new ContourData(data_set);
      newData = cd.getSGTData( time );
      /*
       * Create the layout without a Logo image and with the
       * ColorKey on a separate Pane object.
       */
      rpl = new MJPlotLayout( true, false, false, "ISAW Layout", null, false);
     
      gov.noaa.pmel.util.Dimension2D sz =new Dimension2D( 6.0, 6.0);
      rpl.setLayerSizeP( sz );
        //rpl.setKeyLocationP( new Point2D.Double(0.0,1.0));


      
      int style =state.get_int("Contour.Style");
      if( style != GridAttribute.CONTOUR)
        { 
          rpl.setKeyBoundsP( new Rectangle2D.Double(sz.width/2, 0.0,
                                sz.width-.01, .1));
         }
      
      /*
       * Create a GridAttribute for CONTOUR style.
       */
      //To use a variable scale determined at each time increment use the first
      //Range2D declaration, otherwise, use the second one.
      ClosedInterval cli = cd.getYRange();
      double[] Aclev = clevelsFrom( cli);
      nlevels = Aclev.length;
      Range2D datar = new Range2D( cli.getStart_x(), cli.getEnd_x(),
           ( cli.getEnd_x() - cli.getStart_x() ) / nlevels );

      //Range2D datar = new Range2D( -20f, 45f, 5f );
      clevels = ContourLevels.getDefault(Aclev );
     

      /*
       * Create a ColorMap and change the style to RASTER_CONTOUR.
       */
      IndexedColorMap cmap = createColorMap( datar, state, clevels );
      
 
      if( style != GridAttribute.CONTOUR)
        { gridAttr_ = new GridAttribute( style,cmap);
          if( gridAttr_.isContour())
            gridAttr_.setContourLevels( clevels );
         }
      else
        gridAttr_ = new GridAttribute( clevels );
      
      /*
       * Add the grid to the layout and give a label for
       * the ColorKey.
       */
     
      if( rpl==null)
          DataSetTools.util.SharedData.addmsg("rpl is null");
      rpl.addData( newData, gridAttr_, "Color Scale" );

      /*
       * Change the layout's three title lines.
       
       */
      rpl.setTitles( getMainTitle() , "", "" );

      /*
       * Resize the graph  and place in the "Center" of the frame.
       */
      // rpl.setSize(new Dimension(600, 400));
      /*
       * Resize the key Pane, both the device size and the physical
       * size. Set the size of the key in physical units and place
       * the key pane at the "South" of the frame.
       */
       // rpl.setKeyAlignment(ColorKey.BOTTOM, ColorKey.CENTER);
        
      //rpl.setKeyLayerSizeP(new Dimension2D(6.0, 1.02));
      // rpl.setKeyBoundsP(new Rectangle2D.Double(0.01, 1.01, 5.98, 1.0));
      
      return rpl;
   }


  IndexedColorMap createColorMap( Range2D datar, ViewerState state, ContourLevels clevels )
   {
      
      String ColorMap = IndexColorMaker.HEATED_OBJECT_SCALE;
      String C = null;

      if( state != null )
      {
         C = state.get_String( ViewerState.COLOR_SCALE );

         if( C != null )
            ColorMap = C;
      }
      if( C == null )
      {
         C = SharedData.getProperty( "ColorScale" );
         if( C != null )
            ColorMap = C;
      }

      IndexedColorMap cmap = new IndexedColorMap( 
             IndexColorMaker.getColorTable( ColorMap, ncolors ) );
    
     cmap.setTransform( new logTransform( 0.0, ncolors - 1.0,
               datar.start, datar.end ,state.get_int("Contour.Intensity")) );
      //IndexedColorMap cmap = new IndexedColorMap(IndexColorMaker.getColorTable(ColorMap, 18),clevels);
      return cmap;
   }
  
  class MJPlotLayout extends JPlotLayout
    {
      public MJPlotLayout(boolean x, boolean y, boolean z, String S,java.awt.Image u,boolean zz)
      { super( x,y,z,S,u,zz);
       }

      public void processEvent( AWTEvent evt)
      {
        rpl_Holder.dispatchEvent( evt);
       }

     }

    
   // For testing purposes only
  public static void main2( String[] args )
   {
      ViewerState CONTOUR = new ViewerState();

      CONTOUR.set_String( "CONTOUR", "Contour" );
      DataSet[] data_set;

      data_set = new IsawGUI.Util().loadRunfile( "C:\\ISAW\\SampleRuns\\SCD06496.RUN" );
      new ViewManager( data_set[1], IViewManager.CONTOUR );
      //ContourView contour_view = new ContourView(data_set[1], CONTOUR);

   }

  public static void main3( String[] args)
     { 
      float min=0f,max=223f;
      if( args != null)
        {if( args.length>0)
          min = new Float( args[0]).floatValue();
         if( args.length > 1)
          max = new Float( args[1]).floatValue();
        }
      System.out.println( StringUtil.toString( 
                     ContourView.clevelsFrom(new ClosedInterval( min, max))) );

     }
   /** 
    * Standalone to get Contour views of Qx,Qy,Qz
   * @param  args  args[0] = The filename with the data set
   *               args[1] = OPTIONAL, the histogram to view
   */
  public static void main( String[] args)
   { if( args == null)
       {System.out.println( " Please specify the filename with the data");
        System.exit(0);
        }
      if( args.length <1)
       {System.out.println( " Please specify the filename with the data");
        System.exit(0);
        }
      String filename = args[0];
      DataSet[] data_set;

      data_set = new IsawGUI.Util().loadRunfile( filename );
      int spectra = data_set.length -1;
      if( args.length > 1)
        try
          {
            spectra = ( new Integer( args[1].trim())).intValue();
          }
        catch( Exception ss){}
     DataSet ds = data_set[spectra];
     JFrame jf = new JFrame( "Contour View:"+ ds.toString());

     jf.setSize( 400,600);
     int Choice1 = 0, Choice2 = 1,Choice3 =2;
    
    
     System.out.println("Enter option desired");
     System.out.println("  a) Qx,Qy vs Qz");
     System.out.println("  b) Qx,Qz vs Qy");
     System.out.println("  c) Qy,Qz vs Qx");
     char c=0;
     try{
       while( (c <=32)&&(c!='a') &&(c!='b')&& (c!='c'))
          c = (char)System.in.read();
         }
      catch( Exception u){}
      if( c =='b')
        {Choice2 = 2; Choice3 = 1;}
      else if( c=='c')
        {Choice1 = 1; Choice2 = 2; Choice3 = 0;}
      /*DataSetOperator op = ds.getOperator( "Convert to Q");
      op.setParameter(new Parameter("nbins", new Integer(0)),2);
      Object O = op.getResult();
      if( O instanceof DataSet)
         ds = (DataSet)O;
      else
        {System.out.println( O);
         System.exit(0);
        }
      */
      //Operator op = new Operators.ChgOp( ds);
      //op.getResult();
      QxQyQzAxesHandler Qax = new QxQyQzAxesHandler(ds);
      IAxisHandler Axis1, Axis2, Axis3;
      System.out.println("ds size x_units="+ds.getNum_entries()+ds.getX_units());
      Data D = ds.getData_entry( 960);
      NexIO.NxNodeUtils nd= new NexIO.NxNodeUtils();
      //System.out.println("x="+nd.Showw( D.getX_scale().getXs()));
      //System.out.println("y="+nd.Showw( D.getY_values()));
      if( Choice1 == 0)
        Axis1 = Qax.getQxAxis();
      else if( Choice1 ==1)
        Axis1 = Qax.getQyAxis();
      else
        Axis1 = Qax.getQzAxis();

      if( Choice2 == 0)
        Axis2 = Qax.getQxAxis();
      else if( Choice2 ==1)
        Axis2 = Qax.getQyAxis();
      else
        Axis2 = Qax.getQzAxis();

      if( Choice3 == 0)
        Axis3 = Qax.getQxAxis();
      else if( Choice3 ==1)
        Axis3 = Qax.getQyAxis();
      else
        Axis3 = Qax.getQzAxis();
    System.out.println("============== qx,qy,qz data ===========");
    for( int kk=0;kk<5;kk++)
      System.out.println( Axis1.getValue(0,kk)+","+Axis2.getValue(0,kk)+
              ","+Axis3.getValue(0,kk));
    ViewerState vs = new ViewerState();
    
    ContourView cv = new ContourView( ds, null ,Axis1,Axis2,Axis3);
    jf.getContentPane().setLayout( new GridLayout(1,1));
    jf.getContentPane().add(cv);
    jf.validate();
    jf.show();
         


    } 
  class MyAction implements java.awt.event.ActionListener
   {
      public void actionPerformed( java.awt.event.ActionEvent event )
      {  
         Object obj = event.getSource();

         
         if( obj == tree_ )
            tree_actionPerformed( event );
         //if( obj == print_ )
         //   print_actionPerformed( event );
         if( obj == ac )
         {  
            if( main == null || rpl_ == null || cd == null )
               return;
           
            try
            {  
               int i = new Integer( event.getActionCommand() ).intValue();
              
               //if( sliderTime_index == i)
               //  return;
              
               if( i < 0 ) 
                 i=0;
               else if( i >= times.length )
                  i = times.length -1;;
              
               SimpleGrid newData1 = ( SimpleGrid )( cd.getSGTData( times[i] ) );
              
               if( axis1== null)
               data_set.setPointedAtX( times[i]);
             
              
               sliderTime_index = i;
               if( notify &&(axis1==null))
                 {
                  data_set.notifyIObservers( IObserver.POINTED_AT_CHANGED );
                  notify=false;
                  stop=true;
                  note_time= times[i];
                  note_group = data_set.getPointedAtIndex();
                 }
                  
               else if( axis1== null)
                  notify=true;
                
               ( ( SimpleGrid )newData ).setZArray( newData1.getZArray() );
              
               rpl_.draw();
               rpldrawn = true;
              // dct.showConversions( -1, -1 );
            }
            catch( Exception s )
            {}
         }
      }
   }


  class MyChange implements javax.swing.event.ChangeListener
   {

      JPanel panel;

      public MyChange( JPanel panel )
      {
        this.panel = panel;
      }


      public void stateChanged( javax.swing.event.ChangeEvent event )
      { 
         Object obj = event.getSource();

         if( obj != intensity )
           return;
         state.set_int ("Contour.Intensity",intensity.getValue());
         rpl_Holder.remove( rpl_);
         rpl_ = makeGraph( times[sliderTime_index], state);
         rpl_Holder.add( rpl_);
         rpl_.addMouseListener( cursors );
         
         rpl_Holder.validate();
         rpl_.draw();
            
      }
   }

   /**
    * Print JPlotLayout.  JPlotLayout knows that if keyPane is in
    * a separate JPane to append the keyPane to the plot on the
    * same page.
    */
  /*void actionPerformed( java.awt.event.ActionEvent event )
   {
      Color saveColor;

      PrinterJob printJob = PrinterJob.getPrinterJob();

      printJob.setPrintable( rpl_ );
      printJob.setJobName( "Contour Graph" );
      if( printJob.printDialog() )
      {
         try
         {
            saveColor = rpl_.getBackground();
            //if(!saveColor.equals(Color.white)) {
            //  rpl_.setBackground(Color.white);
            //}
            rpl_.setPageAlign( AbstractPane.TOP,
               AbstractPane.CENTER );
            RepaintManager currentManager = RepaintManager.currentManager( rpl_ );

            currentManager.setDoubleBufferingEnabled( false );
            printJob.print();
            currentManager.setDoubleBufferingEnabled( true );
            //rpl_.setBackground(saveColor);
         }
         catch( PrinterException e )
         {
            System.out.println( "Error printing: " + e );
         }
      }
   }
*/
  public int getPointedAtXindex()
     {float X = getDataSet().getPointedAtX();
      if( Float.isNaN(X))
         return 0;
      if( axis3 != null)//times is now NOT an xscale type array
        { int indx =getDataSet().getPointedAtIndex();
          Data D = getDataSet().getData_entry(indx);
          if( D == null)
            return 0;
          DataSetTools.dataset.XScale xscl = D.getX_scale();
          if( xscl== null)
            return 0;
          else
            {int xindex = xscl.getI(X);
             return xindex;
            }

        }
      int index = java.util.Arrays.binarySearch( times, X);
      if( index < 0)
         index =-index-1;
      if( index <=0)
        return 0;
      if( index >= times.length -1) 
        return times.length -1;
      if( (times[index] -X) <= (X-times[index-1] ))
        return index;
      else
        return index -1;
       
      }
  public void redraw( String reason )
   { 
      if( reason == IObserver.DESTROY )
      {
         setData( getDataSet(), GridAttribute.RASTER_CONTOUR );
         rpl_.draw();

      }
      else if( reason == IObserver.DATA_REORDERED )
      {
         setData( getDataSet(), GridAttribute.RASTER_CONTOUR );
         rpl_.draw();
      }
      else if( reason == IObserver.DATA_DELETED )
      {
         setData( getDataSet(), GridAttribute.RASTER_CONTOUR );
         rpl_.draw();
      }
      else if( reason == IObserver.SELECTION_CHANGED )
      {
         setData( getDataSet(), GridAttribute.RASTER_CONTOUR );
         rpl_.draw();
      }
      else if( reason == IObserver.POINTED_AT_CHANGED )
      { 
        float x = data_set.getPointedAtX();
        int index =data_set.getPointedAtIndex();
        if( index_mouse == index)
          if( time_mouse == x)
            {
              time_mouse = Float.NaN;
              index_mouse = -1;
              return;
             }
        if( stop)
          {if( x == note_time)
            if( note_group==index)
              {stop=false;
               notify=true;
              }
            return;
           }
        int Xindex = getPointedAtXindex();
        
        if( (PrevGroup != index) && notify)
          {notify = true;
           PrevGroup = index;
           if( dct != null )
             if( times != null)
                if( axis3 ==null)
                    if( Xscl.getXScale() != null)
                        dct.showConversions( times[Xindex], index, Xscl.getXScale() );
                    else
                        dct.showConversions( times[Xindex], index );
                else
                    if( Xscl.getXScale() != null)
                       dct.showConversions( x, index, Xscl.getXScale() );
                    else
                       dct.showConversions( x, index);
            
           }
        if( axis3==null)
        if( (Xindex != sliderTime_index) || !rpldrawn) 
          { sliderTime_index = Xindex;
             notify = false;
            ac.setFrameNumber( Xindex );
           
            //ac.stop();
           }
       if(axis3 != null)
         { 
           float t3 = axis3.getValue( index, Xindex);
            notify = false;
            ac.setFrameValue( t3);
            sliderTime_index =ac.getFrameNumber();
            if( dct != null )
             if( times != null)
                if( Xscl.getXScale() != null)
                    dct.showConversions( x, index ,Xscl.getXScale());
                else
                    dct.showConversions( x, index );

          }
       notify = true;
       // if( notify)
          //data_set.notifyIObservers( IObserver.POINTED_AT_CHANGED);  
        }
      else if( reason == IObserver.GROUPS_CHANGED )
      {
         setData( getDataSet(), GridAttribute.RASTER_CONTOUR );
         rpl_.draw();

      }
      else if( reason == IObserver.DATA_CHANGED )
      {
         setData( getDataSet(), GridAttribute.RASTER_CONTOUR );
         rpl_.draw();
      }
      else if( reason == IObserver.ATTRIBUTE_CHANGED )
      {}
      else if( reason == IObserver.FIELD_CHANGED )
      {}
      else if( reason == IObserver.HIDDEN_CHANGED )
      {}
      else if( reason == DataSetViewer.NEW_DATA_SET )
      {
         setData( getDataSet(), GridAttribute.RASTER_CONTOUR );
         rpl_.draw();
      }
      //else  don't redraw
      //redraw();                      
    
   }

  class MyComponentListener extends ComponentAdapter
   {
      public void componentResized( ComponentEvent e )
      { 
         if( rpl_ == null )
            return;

         if( !rpl_.isDisplayable() )
            return;
         Rectangle R = rpl_.getBounds();

         if( R.width <= 0 )
            return;
         if( R.height <= 0 )
            return;

         //main.invalidate();
         if( cursors != null)
           cursors.clearcursors();
         rpl_.draw();

      }


      public void componentShown( ComponentEvent e )
      { 
         //if( rpl_ != null)
         // rpl_.draw();
         
         componentResized( e );
      }


      public void componentHidden( ComponentEvent e )
      { if( cursors != null)
           cursors.clearcursors();
      }



   }


  class MyMouseListener extends MouseAdapter
   {  CrosshairCursor crossHair;
     
      boolean crossHairDrawn, boxDrawn;
      JPanel panel;
      
      public MyMouseListener( JPanel panel)
      { this.panel = panel = null;
        if( panel != null)
          {
            crossHair = new CrosshairCursor( panel );
            crossHairDrawn = false;
            boxDrawn = false;
            //box = new BoxCursor( panel );
           }
        
       }

      public void clearcursors()
      {  if( rpl_Holder != null)
           if( rpl_Holder.isDoingBox())
              rpl_Holder.stop_box( new Point( 0,0), false);
           else if( rpl_Holder.isDoingCrosshair())
              rpl_Holder.stop_crosshair( new Point(0,0));
       //crossHairDrawn = false;
       //crossHair.stop(new Point( 0,0));

      }


      public void handleCrossHairs( MouseEvent e)
      {if( panel == null) 
         return;
       int x = e.getX(),
           y = e.getY();
       
       Component cc = (Component)e.getSource();
       if( e.getSource() instanceof JPane)
       { JPane jp = (JPane) e.getSource();
         
         cc = jp.getParent();
         if( cc == null) return;
         if( !(cc instanceof JPanel)) return;
         x = x +(jp.getBounds().x - panel.getBounds().x );
         y = y +(jp.getBounds().y - panel.getBounds().y );
        
       }

       crossHair.start(  new Point(x,y));
       crossHair.redraw( new Point(x,y));
      

      }




      public void mouseClicked( MouseEvent e )
      { //JPlotLayout jp = (JPlotLayout)(e.getSource());
         handleCrossHairs( e );
         
         gov.noaa.pmel.sgt.Layer L = rpl_.getFirstLayer();
         gov.noaa.pmel.sgt.Graph g = L.getGraph();
        
         if( !( g instanceof CartesianGraph ) )
            return;
         CartesianGraph cg = ( CartesianGraph )g;

        
         double col = cg.getXPtoU( L.getXDtoP( e.getX() ) );
         double row = cg.getYPtoU( L.getYDtoP( e.getY() ) );
         int index = cd.getGroupIndex( row, col );
         float time = cd.getTime( row, col);

         if( index < 0)
           {dct.showConversions( 0.0f, -1 ); 
            return;
           }
        
         if( index >= getDataSet().getNum_entries())
           {dct.showConversions( 0.0f, -1 ); 
            return;
           }
         
         if( Float.isNaN(time))
          { 
            return; 
           } 
        
         index_mouse = index;
         time_mouse = time; 
         data_set.setPointedAtIndex( index);
         
         data_set.setPointedAtX( time);
         
         data_set.notifyIObservers( IObserver.POINTED_AT_CHANGED );
         notify = false;
         stop = false;
         note_time=time;
         note_group=index;
        
         PrevGroup = index;
         //System.out.println(" in MOuse click Xscale="+ Xscl.getXScale().getNum_x() );
         if( Xscl.getXScale() != null)
            dct.showConversions( time, index ,Xscl.getXScale()); 
         else
            dct.showConversions( time, index ); 

      }

   }
  class MyXSCaleActionListener implements ActionListener
   {
    public void actionPerformed( ActionEvent evt)
     {
      XScale xsc= Xscl.getXScale();
      cd.setXScale( xsc);
      if( xsc == null)
        {state.set_int( "ContourTimeStep" , 0 );
        }
      else
        {
         state.set_int( "ContourTimeStep" , xsc.getNum_x() );
         state.set_float("ContourTimeMin", xsc.getStart_x());
         state.set_float("ContourTimeMax",xsc.getEnd_x());
        }
      acChange= true;XsclChange= XConvChange = false;
      setData( getDataSet(), state.get_int("Contour.Style"));
      rpl_.draw();
      }
    }
  class MyJPanel extends JPanel
   {JComponent comp;
    Color colr;
    public MyJPanel( JComponent jc, Color C)
      {super( new GridLayout( 1,1 ));
        comp =jc;
       colr =C;
      }
  
    public void paint1( Graphics g)
      {
        Rectangle R = this.getBounds();
        g.setColor( colr);
 
        g.fillRect(R.x,R.y,R.width,R.height);
        comp.setBounds( new Rectangle( R.x, R.y, R.width,R.height));
       System.out.println("in Paint "+colr+R);
        System.out.println("com"+comp.getBounds()+comp.getClass());
        if( comp instanceof JPlotLayout)
            rpl_.draw(g);
        else
            {comp.paint(g);}
        
       }

    }
  

  public void paint( Graphics g)
  { super.paint( g);
    if( cursors != null)
    {
     cursors.clearcursors();
     }
   }
  public void paint1( Graphics g)
   {System.out.println("In ContourView paint");
     Graphics g1 = g;
     rpl_Holder.paint(g1);
     Rectangle R = acHolder.getBounds();
     g1=g;
     //g1.translate( R.x,R.y);
     System.out.println("CV acHolder.bounds="+acHolder.getBounds());
     System.out.println("CV ac bounds="+ac.getBounds());
     acHolder.paint(g1);

     }

  class MyFocusListener implements FocusListener
   {
    public void focusGained(FocusEvent e)
     { System.out.println("XXX focus gained");
      }

    public void focusLost(FocusEvent e)
     { System.out.println("XXX focus Lost");
      }

   }// MyFocusListener

  class IntensityListener implements ActionListener
   {
    public void actionPerformed( ActionEvent evt )
     {
       if( cd == null)  return;
       Domain D = rpl_.getRange();
       if( D == null)
          return;
       if( D.getXRange() == null) return;
       if( D.getYRange() == null) return;
       //System.out.println("Xrange="+D.getXRange().start+","+D.getXRange().end);
      // System.out.println("Yrange="+D.getYRange().start+","+D.getYRange().end);
       //System.out.println("");
       double[] xArray = ((SGTGrid) newData).getXArray();
       double[] yArray  =((SGTGrid)  newData).getYArray();
       double[] zArray = ((SGTGrid) newData).getZArray();
      
       int xstart = findCoordIndex(D.getXRange().start, xArray, true);
       int xend  =findCoordIndex(D.getXRange().end, xArray, false);
       int ystart = findCoordIndex(D.getYRange().start, yArray, true);
       int yend  =findCoordIndex(D.getYRange().end, yArray, false);

       float SumSel=0.0f, SumBorder= 0.0f;
       int nSel =0, nBord = 0;
       for( int x = xstart; x <= xend; x++)
         for( int y = ystart; y<= yend; y++)
          {
            float value = getValue( yArray.length ,  zArray, x, y);
            if(Float.isNaN(value)){}
            else
               { SumSel+= value;
                 nSel++;
               }

           }
       //Top and Bottom Borders
      for( int x = xstart-1; x <= xend+1; x++)
       if( x >= 0)
          if( x <= xArray.length -1)
           { if( ystart -1 >=0)
               {
                float value = getValue( yArray.length ,  zArray, x, ystart -1);
                if(Float.isNaN(value)){}
                 else
                  { SumBorder+= value;
                   nBord++;
                  }
                }
              if( yend + 1 <= yArray.length -1)
              {
                float value = getValue( yArray.length ,  zArray, x, yend + 1);
                if(Float.isNaN(value)){}
                 else
                  { SumBorder+= value;
                   nBord++;
                  }
              }

           }// if x is in range

       //Right and Left borders
       for( int y = ystart; y <= yend; y++)
         {
           if( xstart-1 >=0)
               {
                float value = getValue( yArray.length ,  zArray, xstart-1, y );
                if(Float.isNaN(value)){}
                 else
                  { SumBorder+= value;
                   nBord++;
                  }
                }
            if( xend+1 <= xArray.length-1)
              {
                float value = getValue( yArray.length ,  zArray, xend + 1, y );
                if(Float.isNaN(value)){}
                 else
                  { SumBorder+= value;
                   nBord++;
                  }
                }

         }  //For for right and left borders

         float Intensity =(SumSel - (nSel * SumBorder/nBord));
        String S="Intensity=" + Intensity +"\n";
        double p_over_b = (0.0 + nSel)/nBord;
        double sigI =java.lang.Math.sqrt( SumSel + p_over_b*p_over_b*SumBorder);
        S+= "(Poisson)Error = " + sigI;
        S+="\n Intensity/error="+(Intensity/sigI);

          
       (new JOptionPane()).showMessageDialog( null, S);

     }
   private float getValue( int axisLength,double[] Zvalues, int x, int y)
    { 
     if( x < 0) return Float.NaN;
     if( y < 0) return Float.NaN;
     if( y >= axisLength) return Float.NaN;
     int index = (x-1)*axisLength + y;
     if( index >= Zvalues.length) return Float.NaN;
     return (float) Zvalues[ index];

    }

   // Gives index that makes sure whole block is in the Range
   private int findCoordIndex( double value, double[] list, boolean leftEndpoint)
    {
      int ind = Arrays.binarySearch( list, value);
      if( ind < 0)
        { ind = -ind -1; //insert before point
          if( !leftEndpoint) 
            ind--;
         
        }
       return ind;


     }


   }//class IntensityListener
}


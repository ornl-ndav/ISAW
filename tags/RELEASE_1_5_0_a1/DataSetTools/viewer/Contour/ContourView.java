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
 *  Revision 1.23  2002/11/27 23:24:29  pfpeterson
 *  standardized header
 *
 *  Revision 1.22  2002/11/25 13:51:07  rmikk
 *  The panel containing the SGT ContourPlot now is a CoordJPanel with all the cursor controls.
 *  Expanding the split pane with the ContourPlot now works.
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
 *  - Contour levels now "rounded" to last two or one differing digits base 10. There should be approximately 10 contour levels.
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


import gov.noaa.pmel.sgt.demo.*;
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
   static JPlotLayout rpl_ = null;
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
   protected IAxesHandler Transf;
   MyMouseListener cursors;
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
     DataSetTools.viewer.PrintComponentActionListener.setUpMenuItem( menu_bar, this );
      
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
       {state.set_int( "ContourTimeStep" , 0 );
        UniformXScale xx = getDataSet().getXRange();
        state.set_float("ContourTimeMin", xx.getStart_x());
        state.set_float("ContourTimeMax",xx.getEnd_x());
        state.set_boolean(ViewerState.CONTOUR_DATA, true);
        state.set_int("Contour.Intensity", 50);
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
     //rpl_.addFocusListener( new MyFocusListener());
     //rpl_.addMouseMotionListener( new MyMouseMotionListener());
     //main.setDividerLocation( .70);
     /*  main = new JPanel();
     GridBagLayout gbl = new GridBagLayout();
     GridBagConstraints gbc = new GridBagConstraints();
     
     main.setLayout( new BorderLayout());
     //gbc.fill = GridBagConstraints.BOTH;
     gbc.weightx=2;
     // gbc.gridx = 0;
    
     // gbl.setConstraints( rpl_Holder, gbc);
     
     //main.add(rpl_, BorderLayout.CENTER);
     gbc.weightx = 1;
     
     //gbc.gridx = 3;
      
     //gbl.setConstraints( jpEast, gbc);
     
     main.add( jpEast, BorderLayout.EAST);
     */
     /* all grid bag layout for all components
     main = new JPanel();
  
     GridBagLayout gbl = new GridBagLayout();
     GridBagConstraints gbc = new GridBagConstraints();
     gbc.weightx =2;
     gbc.gridheight = 6;
     gbl.setConstraints( rpl_Holder, gbc);

     gbc.weightx =1;
     gbc.gridheight = 1;
     gbc.gridwidth =  GridBagConstraints.REMAINDER;
     gbl.setConstraints( ac, gbc);

     gbl.setConstraints( intensity, gbc);

     gbc.gridheight =3;
     gbl.setConstraints( ConvTableHolder, gbc);

     main.add( rpl_Holder);
     main.add( ac);
     main.add( intensity);
     main.add( ConvTableHolder);
     // end grid bag layout for all components
     */  
     setLayout( new GridLayout( 1,1));
     rpl_Holder.addComponentListener( new MyComponentListener() );
     add( main);
     validate();
     rpldrawn=false;
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
  // main splitpane with state, 
  public void setLayout(JPanel jpEast)
    {jpEast.add( ac );
     jpEast.add(XsclHolder);
     jpEast.add(intensityHolder);  
     jpEast.add( ConvTableHolder );
     jpEast.add( Box.createHorizontalGlue() );
     main = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,  rpl_Holder,  jpEast, .70f);
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
     //intensityHolder.setBounds( new Rectangle(R1+1, 1+(int)(R.height*.2),R.width-R1, 
      //                  (int)(R.height*.1)) ));
     // ConvTableHolder.setBounds( new Rectangle(R1+1, 3+(int)(R.height*.3), R.width -R1,
     //                                 R.height-2-(int)(R.height*.3));
   
     rpl_Holder.doLayout();
     //acHolder.doLayout();
     //intensityHolder.doLayout();
     //ConversionTableHolder.doLayout();

     main.add( rpl_Holder);
     main.add( acHolder);
     rpl_Holder.doLayout();
     acHolder.doLayout();
     // System.out.println( "acHolder bounds="+acHolder.getBounds());
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
        cd = new ContourData( ds );
     else
        cd = new ContourData( ds, axis1, axis2, axis3);
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
          
          
       }
     if( ConvTableHolder == null )
        ConvTableHolder = new JPanel( new GridLayout( 1, 1 ) );
     else if( XConvChange)
        ConvTableHolder.remove( dctScroll );

      
     if( XConvChange )
       {
        dct = new DataSetXConversionsTable( getDataSet() );
        dct.showConversions(getDataSet().getPointedAtX(), 
                              getDataSet().getPointedAtIndex());
          
        dctScroll = new JScrollPane( dct.getTable() );
        ConvTableHolder.add( dctScroll );
       }

     if( XsclHolder == null)
        XsclHolder = new JPanel( new GridLayout( 1,1 ));
     else if( XsclChange)
        XsclHolder.remove( Xscl);

     if( XsclChange)
       {
        Xscl = new XScaleChooserUI( getDataSet().getX_label(), getDataSet().getX_units(),
                     state.get_float("ContourTimeMin"), state.get_float("ContourTimeMax"),
                     state.get_int("ContourTimeStep"));

        Xscl.addActionListener( new MyXSCaleActionListener());
        XsclHolder.add( Xscl);
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
          {//main.remove( rpl_Holder);
           //main.remove( rpl_ );
           rpl_Holder.remove( rpl_);
          }
        else 
           System.out.println("main != null && rpl_Holder is null");
     rpl_ = this.makeGraph( times[sliderTime_index], state );
      
     //rpl_.setKeyBoundsP(new Rectangle2D.Double(2.0,2.0,ls.width,1.0 )); 
     //rpl_.setKeyLocationP( new Point2D.Double( 2.0,2.0));

     rpl_.addMouseListener( cursors );
     //gridKeyPane = rpl_.getKeyPane();
     //rpl_.setBatch(true);
     //rpl_.setLayout( new GridLayout( 1,1));
      
     // rpl_.setKeyAlignment(AbstractPane.BOTTOM, AbstractPane.CENTER);
     /*
      * Layout the plot, key, buttons, and slider.
      */

     //main.add(rpl_, BorderLayout.CENTER);


     // gridKeyPane.setLayout( new GridLayout( 1,1));
     // gridKeyPane.setSize(new Dimension(600,100));
     //rpl_.setKeyLayerSizeP(new Dimension2D(6.0, 1.0));
     // rpl_.setKeyBoundsP(new Rectangle2D.Double(0.0, 1.0, 6.0, 1.0));
     //gridKeyPane.setLayout( new GridLayout(1,1));
     //main.add(gridKeyPane, BorderLayout.SOUTH);


     //main.add(jpEast, BorderLayout.EAST);

     //frame.pack();
     //frame.setVisible(true);

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
         setData( data_set, state.get_int("Contour.Style"));
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

  public static double getPow10( int power)
    {return java.lang.Math.pow( 10.0, 0.0+power);
     }
  private int digVal( double v, int digit)
    {
     if( v==0)
        return 0;
     if( v<0)
        v = -v;
     int Ldigit = getLeadDigPos( v);
     if( digit > Ldigit)
        return 0;
     double pow10 = getPow10( digit-1);
     double rerror =1 - getPow10(6+Ldigit-digit);
     double v1 = (int)(v/pow10);
     if( v/pow10 -v1 > rerror) 
        v1=v1+1;
     v1 = v -pow10*v1;
     double v2 = (int)(v1*10.0/pow10);
     rerror =1 - getPow10(6+Ldigit-digit-1);
     if( v1*10.0/pow10 -v2 > rerror)
        v2 = v2+1;
     return (int)v2;
         
    }
  // takes abs value, converts to string. Makes sure there is a decimal point.
  // Attempts to deal with trailing 9's and 0's  and leading 0's
  public static String fixUp( double v)
    {v = java.lang.Math.abs(v);
     String Res = new Double( v).toString().trim();
     if( Res.indexOf('.') < 0)
        Res +='.';
     int pdot = Res.indexOf('.');
     while( (Res.length() >8) && (Res.length()>= pdot)) //too long
       {Res = Res.substring( 0, Res.length()-1);
       }
     if( Res.length() > 8)
       {char[] cc= new char[Res.length()-1-7];
        Arrays.fill(cc, '0');
        Res = Res.substring(0,8)+new String(cc) +".";
       }
     while( Res.charAt(0) =='0')
        Res = Res.substring(1);
     while( Res.charAt(Res.length()-1) == '0')
        Res = Res.substring(0, Res.length() -1);

       // now check for trailing 9's
     int Nnines=0;
     for( int i = Res.length()-1; Res.charAt(i) =='9'; i--)
        Nnines++;
     if( Nnines ==0)
        return Res;
     if( Res.length() < 8)
        return Res;
     Res = Res.substring( 0,Res.length() - Nnines);

     if( Res.charAt(Res.length()-1) !='.')
        return Res.substring( 0, Res.length()-1) + 
               (char)((int)Res.charAt( Res.length()-1)+1);
     String SRes=".";
     for( int i=Res.length()-2; i >=0; i--)
        if( Res.charAt(i) !='9')
           return Res.substring(0,i)+(char)((int)Res.charAt(i) +1) + SRes;
        else
           SRes = '0'+SRes;
     return "1"+SRes;

      }


  public static double[] clevelsFrom( ClosedInterval YRange)
    {double start =(double) YRange.getStart_x();
     double end = (double)YRange.getEnd_x();
     int sgns=1;
     int sgne=1;
     if( start < 0) 
        sgns=-1;
     if( end < 0) 
        sgne = -1;
     String Sstart = fixUp( start);
     String Send = fixUp( end);
     int pDotStart = Sstart.indexOf('.');
     int pDotEnd = Send.indexOf('.');
     // Get digits to line up and have equal length
     char[] cc;
     if( pDotStart < pDotEnd)
       {cc= new char[pDotEnd-pDotStart];
        Arrays.fill(cc,'0');
        Sstart = new String( cc )+Sstart;
       }
     else if( pDotEnd < pDotStart)
       {cc= new char[pDotStart-pDotEnd];
        Arrays.fill(cc,'0');
        Send = new String( cc )+Send;
       }

     if( Sstart.length() < Send.length())
       {cc= new char[Send.length()-Sstart.length()];
        Arrays.fill(cc,'0');
        Sstart +=new String( cc );
       }
     else if( Send.length() < Sstart.length())
       {cc= new char[Sstart.length()- Send.length()];
        Arrays.fill(cc,'0');
        Send += new String(cc);
       }
     //Find the number of leading base 10 digits that match
     int nmatch = 0;
     boolean done= false;
     for( int i=0; (i<Sstart.length())&&(i < Send.length())&& !done; i++)
        if( Sstart.charAt(i) == Send.charAt(i))
           nmatch++;
        else
           done = true;
      //
     char cStart,cEnd;
     double[] Res = null;
     if( nmatch == Sstart.length())
        if( sgns*sgne > 0)
          {Res = new double[1];
           Res[0] = start;
           return Res;
          }
        else
          {if( Sstart.charAt(0) == '.')
              nmatch = 1; // include decimal point
           else
               nmatch =0;
           if( Sstart.length() <= nmatch)
             {Sstart +='0';
              Send +='0';
             }
            
           cStart = Sstart.charAt(nmatch);
           if(cStart == '.')
              nmatch++;
           if( Sstart.length() <= nmatch)
             {Sstart +='0';
              Send +='0';
             }
           cStart = Sstart.charAt(nmatch);
           cEnd =Send.charAt( nmatch);
           
          }
     else // not all digits match
       {cStart = Sstart.charAt(nmatch);
        cEnd = Send.charAt( nmatch);
          
       }
         
     int ncontours = (int)cEnd-(int)'0' - (int)(sgne*sgns)*((int)cStart-(int)'0');
     if( ncontours < 0) 
        ncontours = -ncontours;
     if(sgns <0) 
        ncontours++;
     if( ncontours > 7)
       {Res = new double[ ncontours + 1];
        int pdot = Sstart.indexOf('.');
        double delta = getPow10(pdot - nmatch -1);
        String SstartB= Sstart.substring(0, nmatch + 1);
          
        for( int j=nmatch+1;j<Sstart.length();j++)
           if( Sstart.charAt(j)=='.')
              SstartB +='.';
           else
              SstartB +='0';
        double ss= sgns*new Double( SstartB).doubleValue();
        if( sgns < 0)
           ss -=delta;
            
        for( int j=0; j < ncontours + 1 ;j++)
          {Res[j]= ss+j*delta;
          } 
        return Res;
       }
     else //use 2 digits
       {char cStart2,cEnd2;
        while( Sstart.length() <= nmatch+1)
          {Sstart +='0';
           Send +='0';
          }   
        int kk = 0;
        if( Sstart.charAt(nmatch + 1 + kk) =='.') 
           kk++;
        if( kk>=Sstart.length())
          {Sstart += '0';
           Send   += '0';
          }
        cStart2= Sstart.charAt( nmatch+1+kk);
        cEnd2 =Send.charAt( nmatch+1 +kk);
        ncontours = 10*((int)cEnd -(int)'0')+(int)cEnd2-(int)'0' -
                      (int)(sgns*sgne)*(10*((int)cStart-(int)'0')+(int)cStart2-(int)'0');
        if( ncontours < 0) 
           ncontours = -ncontours;
          
        //if( ncontours >10)
          {int step = ncontours/10;
           if( step <2) 
              step = 1;
           else if( step < 4) 
              step =2;
           else 
              step = 5;
           int pdot = Sstart.indexOf('.');
           double delta = step*getPow10(pdot-nmatch-2);
           ncontours =(int)( ncontours/(float)step +.3)+1;
           Res = new double[ncontours ];
           String SstartB=  Sstart.substring(0, nmatch+kk+1);
           int ichar = (int)cStart2-(int)'0';
           ichar = step * (int)( ichar/(float)step +.45)+(int)'0';
           if(pdot == SstartB.length())
             {SstartB+='.';
              kk++;
             }
           SstartB +=(char)ichar;
           for( int j=nmatch+kk+2; j<Sstart.length();j++)
              if( Sstart.charAt(j)=='.')
                 SstartB +='.';
              else
                 SstartB +='0';
           double jStart = sgns*new Double( SstartB).doubleValue();
           if(sgns < 0)
              jStart -= delta;
           for( int j=0; j< ncontours ; j++)
              Res[j]= jStart+j*delta;
           return Res;
          }    
       }
     //System.out.println("XHere");    
     //return Res;
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

      /*MouseListener[] E = (MouseListener[]) rpl.getListeners(MouseListener.class );
     
      for(int i=0;i<E.length;i++)
         rpl.removeMouseListener( E[i]);

      MouseMotionListener[] E1 = (MouseMotionListener[])rpl.getListeners(MouseMotionListener.class);
      for(int i=0;i<E1.length;i++)
         rpl.removeMouseMotionListener( E1[i]); 

      */
      
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
         C = System.getProperty( "ColorScale" );
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
      System.out.println(
      (new NexIO.NxNodeUtils()).Showw(    
      ContourView.clevelsFrom(new ClosedInterval( min, max)))
                         );

     }
   /** Standalone to get Contour views of Qx,Qy,Qz
   * @param  args[0]  The filename with the data set
   * @param  args[1]  OPTIONAL, the histogram to view
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
              
               data_set.setPointedAtX( times[i]);
             
              
               sliderTime_index = i;
               if( notify)
                 {
                  data_set.notifyIObservers( IObserver.POINTED_AT_CHANGED );
                  notify=false;
                  stop=true;
                  note_time= times[i];
                  note_group = data_set.getPointedAtIndex();
                 }
                  
               else
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
      {}
      else if( reason == IObserver.POINTED_AT_CHANGED )
      { 
        float x = data_set.getPointedAtX();
        int index =data_set.getPointedAtIndex();
        if( stop)
          {if( x == note_time)
            if( note_group==index)
              {stop=false;
               notify=true;
              }
            return;
           }
        int Xindex = getPointedAtXindex();
       

        if( PrevGroup != index)
          {notify = true;
           PrevGroup = index;
           if( dct != null )
             if( times != null)
                dct.showConversions( times[Xindex], index );
            
           }
       
        if( (Xindex != sliderTime_index) || !rpldrawn) 
          { sliderTime_index = Xindex;
             notify = false;
            ac.setFrameNumber( Xindex );
           
            //ac.stop();
           }
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
         System.out.println("in getSource instance of JPane");
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
          
         data_set.setPointedAtIndex( index);
         
         data_set.setPointedAtX( time);
         
         data_set.notifyIObservers( IObserver.POINTED_AT_CHANGED );
         notify = false;
         stop = false;
         note_time=time;
         note_group=index;
        
         PrevGroup = index;
        
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
      acChange= XsclChange =true; XConvChange = false;
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

   }
}


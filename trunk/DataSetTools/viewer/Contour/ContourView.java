/*
 * File:  ContourView.java
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
 * Contact : Rion Dooley <deardooley@anl.gov>
 *           IPNS
 *           Argonne National Lab
 *           Argonne, IL  60439
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 * Modified:
 *
 *  $Log$
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
import gov.noaa.pmel.sgt.dm.*;


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
   JButton edit_;
   JButton space_ = null;
   AnimationController ac;
   JMenuItem tree_;
   DataSetXConversionsTable dct;
   JPanel ConvTableHolder = null;
   JScrollPane dctScroll = null;
   JButton print_;
   ContourData cd;
   JSlider time_slider;
   SGTData newData;
   JPane gridKeyPane;
   DataSet data_set;
   int sliderTime_index;
   //JFrame frame;
   float[] times;
   int ncolors = 200;
   int nlevels = 16;
   ViewerState state = null;  
   JLabel time_label = new JLabel( "" );
   SplitPaneWithState main = null;
   public ContourView( DataSet ds, ViewerState state1 )
   {

      super( ds, state1 );  // Records the data_set and current ViewerState
      // object in the parent class and then
      // sets up the menu bar with items handled by the
      // parent class.
      data_set = ds;
      state = state1;
      if( !validDataSet() )
      {
         return;
      }
      sliderTime_index = 0;
      
      if( state1 == null)
        { state = new ViewerState();
         
        }
      /*
       * Create a new ViewManager to contain the graph.
       */
      JMenuBar menu_bar = getMenuBar();
      JMenu jm = menu_bar.getMenu( DataSetViewer.EDIT_MENU_ID );

      tree_ = new JMenuItem( "Graph" );
      jm.add( tree_ );
      tree_.addActionListener( new MyAction() );
      DataSetTools.viewer.PrintComponentActionListener.setUpMenuItem( menu_bar, this );
      //main.setLayout(new BorderLayout());
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
    
      setData( data_set, state.get_int( ViewerState.CONTOUR_STYLE) );
      JPanel jpEast = new JPanel();

      BoxLayout blay = new BoxLayout( jpEast, BoxLayout.Y_AXIS );

      jpEast.setLayout( blay );
      jpEast.add( ac );

      jpEast.add( ConvTableHolder );
      jpEast.add( Box.createHorizontalGlue() );
      main = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT, rpl_, jpEast, .7f );
      setLayout( new GridLayout( 1, 1 ) );
      add( main );

      main.addComponentListener( new MyComponentListener() );
      main.validate();
   }

  public ViewerState getState()
   {return state;
   }



   private void setData( DataSet ds, int GridContourAttribute )
   {
      cd = new ContourData( ds );
      times = cd.getTimeRange();
      if( ac != null )
         ac.setFrame_values( times );

      if( ConvTableHolder == null )
         ConvTableHolder = new JPanel( new GridLayout( 1, 1 ) );
      else if( dctScroll != null )
         ConvTableHolder.remove( dctScroll );

      dct = new DataSetXConversionsTable( getDataSet() );
      dctScroll = new JScrollPane( dct.getTable() );
      ConvTableHolder.add( dctScroll );
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
         ac.setBorderTitle( data_set.getX_label() );
         ac.setTextLabel( data_set.getX_units() + "=" );
      }
      state.set_int("Contour.Style", GridContourAttribute);
      if( !( rpl_ == null ) )
         if( main != null )
         {
            main.remove( rpl_ );
         }
      rpl_ = this.makeGraph( times[sliderTime_index], state );

      //rpl_.setKeyBoundsP(new Rectangle2D.Double(2.0,2.0,ls.width,1.0 )); 
      //rpl_.setKeyLocationP( new Point2D.Double( 2.0,2.0));

      rpl_.addMouseListener( new MyMouseListener() );
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
         main.setLeftComponent( rpl_ );

   }


   public void setDataSet( DataSet ds )
   {
      super.setDataSet( ds );
      data_set = ds;
      int GridAt = state.get_int("Contour.Style");
     
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

         /* ClosedInterval cli= getDataSet().getYRange();
          Range2D datar = new Range2D(cli.getStart_x(), cli.getEnd_x(), 
          (cli.getEnd_x() - cli.getStart_x()) / nlevels );
          IndexedColorMap ii=createColorMap( datar, state, (ContourLevels)null);
          gridAttr_.setColorMap(ii);
          //will have to redraw whole thing init_rpl, 
          */
         init( data_set, state.get_int("Contour.Style"));
         rpl_.draw();
      }
   }
   class MyContStyleListener implements ActionListener
   {
      public void actionPerformed( ActionEvent evt)
      {if( evt.getActionCommand().equals("AREA_FILL"))
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
   JPanel makeButtonPanel()
   {
      JPanel button = new JPanel();

      button.setLayout( new FlowLayout() );
      //tree_ = new JButton("Tree View");
      MyAction myAction = new MyAction();

      tree_.addActionListener( myAction );
      button.add( tree_ );
      edit_ = new JButton( "Edit GridAttribute" );
      edit_.addActionListener( myAction );
      button.add( edit_ );
      tree_.addActionListener( myAction );
      print_ = new JButton( "Print Contour" );
      print_.addActionListener( myAction );
      button.add( print_ );
      return button;
   }


   JPanel makeSliderPanel()
   {

      JPanel slider = new JPanel();

      slider.setLayout( new FlowLayout() );
      time_slider = new JSlider( 0, times.length - 1 );
      MyChange l = new MyChange();

      time_slider.addChangeListener( l );
      //time_slider.setExtent(100);
      time_slider.setMinorTickSpacing( 1 );
      time_slider.setMinorTickSpacing( 10 );
      time_slider.setValue( 1 );
      time_label.setText( "" + times[time_slider.getValue()] );
      slider.add( time_label );
      slider.add( time_slider );
      return slider;
   }


   void edit_actionPerformed( java.awt.event.ActionEvent e )
   {

      /*
       * Create a GridAttributeDialog and set the renderer.
       */
      GridAttributeDialog gad = new GridAttributeDialog();

      gad.setJPane( rpl_ );
      CartesianRenderer rend = ( ( CartesianGraph )rpl_.getFirstLayer().getGraph() ).getRenderer();

      gad.setGridCartesianRenderer( ( GridCartesianRenderer )rend );
      //        gad.setGridAttribute(gridAttr_);
      gad.setVisible( true );
   }


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


   void slider_changePerformed( javax.swing.event.ChangeEvent event )
   {

      /*
       * Change the time value and redraw the graph with the new data
       */
      time_label.setText( "" + times[time_slider.getValue()] );

      if( main == null || rpl_ == null || cd == null )
         return;
      SimpleGrid newData1 = ( SimpleGrid )( cd.getSGTData( times[time_slider.getValue()] ) );

      ( ( SimpleGrid )newData ).setZArray( newData1.getZArray() );
      rpl_.draw();

      /*main.remove(rpl_);
       rpl_= makeGraph(times[time_slider.getValue()], null);
       rpl_.setBatch(true);
       main.add(rpl_, BorderLayout.CENTER);
       JPane gridKeyPane = rpl_.getKeyPane();
       // gridKeyPane.setSize(new Dimension(600,100));
       main.add(gridKeyPane, BorderLayout.SOUTH);
       //frame.getContentPane().add(main, "Center");    
       rpl_.getKeyPane().draw();
       rpl_.setBatch(false);
       */
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
      rpl = new JPlotLayout( true, false, false, "ISAW Layout", null, true );
      rpl.setEditClasses( true );

      /*
       * Create a GridAttribute for CONTOUR style.
       */
      //To use a variable scale determined at each time increment use the first
      //Range2D declaration, otherwise, use the second one.
      ClosedInterval cli = getDataSet().getYRange();
      Range2D datar = new Range2D( cli.getStart_x(), cli.getEnd_x(),
            ( cli.getEnd_x() - cli.getStart_x() ) / nlevels );

      //Range2D datar = new Range2D( -20f, 45f, 5f );
      clevels = ContourLevels.getDefault( datar );
      gridAttr_ = new GridAttribute( clevels );

      /*
       * Create a ColorMap and change the style to RASTER_CONTOUR.
       */
      IndexedColorMap cmap = createColorMap( datar, state, clevels );

      gridAttr_.setColorMap( cmap );
      gridAttr_.setStyle( state.get_int("Contour.Style"));

      /*
       * Add the grid to the layout and give a label for
       * the ColorKey.
       */
      rpl.addData( newData, gridAttr_, "Area Detector Data" );

      /*
       * Change the layout's three title lines.
       */
      rpl.setTitles( "Raster Contour Graph", "", "" );

      /*
       * Resize the graph  and place in the "Center" of the frame.
       */
      // rpl.setSize(new Dimension(600, 400));
      /*
       * Resize the key Pane, both the device size and the physical
       * size. Set the size of the key in physical units and place
       * the key pane at the "South" of the frame.
       */
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

      IndexedColorMap cmap = new IndexedColorMap( IndexColorMaker.getColorTable( ColorMap, ncolors ) );

      cmap.setTransform( new logTransform( 0.0, ncolors - 1.0,
            datar.start, datar.end ,1.0f) );
      //IndexedColorMap cmap = new IndexedColorMap(IndexColorMaker.getColorTable(ColorMap, 18),clevels);
      return cmap;
   }
  
   // For testing purposes only
   public static void main( String[] args )
   {
      ViewerState CONTOUR = new ViewerState();

      CONTOUR.set_String( "CONTOUR", "Contour" );
      DataSet[] data_set;

      data_set = new IsawGUI.Util().loadRunfile( "C:\\ISAW\\SampleRuns\\SCD06496.RUN" );
      new ViewManager( data_set[1], IViewManager.CONTOUR );
      //ContourView contour_view = new ContourView(data_set[1], CONTOUR);

   }

   class MyAction implements java.awt.event.ActionListener
   {
      public void actionPerformed( java.awt.event.ActionEvent event )
      {
         Object obj = event.getSource();

         if( obj == edit_ )
            edit_actionPerformed( event );
         if( obj == tree_ )
            tree_actionPerformed( event );
         if( obj == print_ )
            print_actionPerformed( event );
         if( obj == ac )
         {
            if( main == null || rpl_ == null || cd == null )
               return;
            try
            {
               int i = new Integer( event.getActionCommand() ).intValue();

               sliderTime_index = i;
               if( i < 0 ) return;
               if( i >= times.length )
                  return;

               SimpleGrid newData1 = ( SimpleGrid )( cd.getSGTData( times[i] ) );

               ( ( SimpleGrid )newData ).setZArray( newData1.getZArray() );
               rpl_.draw();

               dct.showConversions( -1, -1 );
            }
            catch( Exception s )
            {}
         }
      }
   }


   class MyChange implements javax.swing.event.ChangeListener
   {
      public void stateChanged( javax.swing.event.ChangeEvent event )
      {
         Object obj = event.getSource();

         if( obj == time_slider )
            slider_changePerformed( event );
      }
   }

   /**
    * Print JPlotLayout.  JPlotLayout knows that if keyPane is in
    * a separate JPane to append the keyPane to the plot on the
    * same page.
    */
   void print_actionPerformed( java.awt.event.ActionEvent event )
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
      {}
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

         main.invalidate();

         rpl_.draw();

      }


      public void componentShown( ComponentEvent e )
      {
         //if( rpl_ != null)
         // rpl_.draw();
         componentResized( e );
      }


      public void componentHidden( ComponentEvent e )
      {}

   }


   class MyMouseListener extends MouseAdapter
   {
      public void mouseClicked( MouseEvent e )
      { //JPlotLayout jp = (JPlotLayout)(e.getSource());
         gov.noaa.pmel.sgt.Layer L = rpl_.getFirstLayer();
         gov.noaa.pmel.sgt.Graph g = L.getGraph();

         if( !( g instanceof CartesianGraph ) )
            return;
         CartesianGraph cg = ( CartesianGraph )g;

         // System.out.println( L.getXDtoP(e.getX())+","+L.getYDtoP(e.getY()));
         //System.out.println( cg.getXPtoU(L.getXDtoP(e.getX()))+","+
         //                     cg.getYPtoU(L.getYDtoP(e.getY())));
         double col = cg.getXPtoU( L.getXDtoP( e.getX() ) );
         double row = cg.getYPtoU( L.getYDtoP( e.getY() ) );
         int index = cd.getGroupIndex( row, col );
         float time = cd.getTime();

         dct.showConversions( time, index );

      }

   }
}


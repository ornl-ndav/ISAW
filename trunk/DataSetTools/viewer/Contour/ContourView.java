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
   JPanel rpl_Holder = null;
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
   // Use the correct one ---------------
   //JPanel main; 
   SplitPaneWithState main = null;
   //JSplitPane main = null;
  // JPanel main= null;
   //---------------------
   JSlider intensity = null;
   public ContourView( DataSet ds, ViewerState state1 )
   {

      super( ds, state1 );  // Records the data_set and current ViewerState
 
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
       state.set_int ("Contour.Intensity",50);
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
    
      setData( data_set, state.get_int( ViewerState.CONTOUR_STYLE) );

      //Add the components to the window
      JPanel jpEast = new JPanel();

      BoxLayout blay = new BoxLayout( jpEast, BoxLayout.Y_AXIS );

      jpEast.setLayout( blay );
     
   
      intensity = new JSlider( 0, 100);
      intensity.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(),
                        "Intensity") );
     
      intensity.addChangeListener( new MyChange());
      

     
      
      rpl_Holder = new MyJPanel( rpl_, Color.white);
      rpl_Holder.add( rpl_);
      setLayout(jpEast );
     
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
      main.addComponentListener( new MyComponentListener() );
      add( main);
      setLayout( new GridLayout( 1,1));
      validate();

   }
  // main splitpane with state, 
  public void setLayout(JPanel jpEast)
   {jpEast.add( ac );
    jpEast.add(intensity);  
    jpEast.add( ConvTableHolder );
    jpEast.add( Box.createHorizontalGlue() );
    main = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,  rpl_Holder,  jpEast, .70f);
   }
   //Change the name of doLayou when changing this
   //Cannot get the column 2 items to draw correctly.
   JPanel acHolder, intensityHolder;
   public void setLayout2( JPanel jpEast)
     { //main = new JPanel();
       main.setLayout( null);
       acHolder = new MyJPanel(  ac ,Color.blue);//new JPanel( new GridLayout( 1,1));//
       intensityHolder =new JPanel( new GridLayout( 1,1));
      
       acHolder.add( ac);
       intensityHolder.add( intensity);
       
     }
  public void doLayout2()
   {if( acHolder == null)
      { super.doLayout();
        System.out.println("Eliminate ContourView.doLayout");
        return;
       }
    System.out.println("in doLayout Bounds ="+getBounds());
    Rectangle R = getBounds();
    if( R.width <=0) return;
    if(R.height <=0) return;
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
    System.out.println( "acHolder bounds="+acHolder.getBounds());
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
      if( ( main != null ) )
       if( rpl_Holder != null)  
         { //main.remove( rpl_Holder);
            //main.remove( rpl_ );
             rpl_Holder.remove( rpl_);
         }
        else System.out.println("main != null && rpl_Holder is null");
      rpl_ = this.makeGraph( times[sliderTime_index], state );
      
      //rpl_.setKeyBoundsP(new Rectangle2D.Double(2.0,2.0,ls.width,1.0 )); 
      //rpl_.setKeyLocationP( new Point2D.Double( 2.0,2.0));

      rpl_.addMouseListener( new MyMouseListener() );
      //gridKeyPane = rpl_.getKeyPane();
      //rpl_.setBatch(true);
      rpl_.setLayout( new GridLayout( 1,1));
      
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
         {  rpl_Holder.add( rpl_);
           //main.setLeftComponent( rpl_ );
           validate();
          }

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
  
         setData( data_set, state.get_int("Contour.Style"));
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
            datar.start, datar.end ,state.get_int("Contour.Intensity")) );
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
               if( sliderTime_index == i)
                 return;
               
               if( i < 0 ) 
                 i=0;
               else if( i >= times.length )
                  i = times.length -1;;
               sliderTime_index = i;
               SimpleGrid newData1 = ( SimpleGrid )( cd.getSGTData( times[i] ) );
               data_set.setPointedAtX( times[i]);
               data_set.notifyIObservers( IObserver.POINTED_AT_CHANGED );
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

         if( obj != intensity )
           return;
         state.set_int ("Contour.Intensity",intensity.getValue());
         setData( getDataSet(), state.get_int(ViewerState.CONTOUR_STYLE));
         main.repaint();
         rpl_.draw();
        
            
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

   private int getPointedAtXindex()
     {float X = getDataSet().getPointedAtX();
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
        int Xindex = getPointedAtXindex();
        if( Xindex != sliderTime_index) // java.lang.Math.abs(cd.getTime() -x) >.00001)
          { sliderTime_index = Xindex;
            ac.setFrameNumber( Xindex );
            //ac.stop();
           }
           
        dct.showConversions( times[Xindex], index );
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

         rpl_.draw();

      }


      public void componentShown( ComponentEvent e )
      { System.out.println("B");
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
         data_set.setPointedAtIndex( index);
         data_set.setPointedAtX( time);
         
         data_set.notifyIObservers( IObserver.POINTED_AT_CHANGED );

         //dct.showConversions( time, index ); moved to redraw

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
}


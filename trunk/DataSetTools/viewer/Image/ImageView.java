/**
 * File:  ImageView.java
 *
 * Copyright (C) 1999, Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
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
 *  Revision 1.62  2007/09/17 02:55:04  dennis
 *  Updated name of GraphJPanel method setZoom_region() to the new
 *  name setLocalWorldCoords().
 *
 *  Revision 1.61  2007/07/13 20:26:28  dennis
 *  getDisplayComponent() now returns the scrolled pane that
 *  contains the ImageJPanel2, not the ImageJPanel2 itself.
 *  This fixes an obscure bug where the panel was not displayed
 *  in the PreviewDevice.  The same component is part of the
 *  ImageView and placed in a window by the ImageRenderWriter.
 *
 *  Revision 1.60  2007/07/13 16:52:46  dennis
 *  Added getDisplayComponent() method to return just the data display
 *  panel without any controls.
 *
 *  Revision 1.59  2007/04/30 00:20:36  dennis
 *  Adjusted bounds in y-direction to [0,n_rows] to match convention
 *  used in the underlying ImageJPanel2.
 *
 *  Revision 1.58  2006/08/10 17:05:13  dennis
 *  In redraw() method, only redraw horizontal graph in case the
 *  request to redraw came from outside of the ImageView.
 *
 *  Revision 1.57  2005/09/01 02:32:35  dennis
 *  Small fixes to vertical scrolling behavior.
 *  Now preserves size of zoomed in region when vertical scrolling.
 *  Double click to zoom out now zooms out to maximum number of
 *  displayable rows, not full DataSet, when vertical scrolling is on.
 *
 *  Revision 1.56  2005/08/31 21:25:37  dennis
 *  Fixed bug where zoom region would not be properly retained when
 *  the window was resized.
 *
 *  Revision 1.55  2005/08/31 20:02:01  dennis
 *  Fixed bug where ImageView "hung" when rebinning un-transformed
 *  data.  This was bug was introduced when the ImageView class was
 *  revised to handle more than 32767 spectra.
 *
 *  Revision 1.54  2005/08/17 01:24:39  dennis
 *  Size of sort/selection icon now adjusts depending on whether or
 *  not a horizontal scroll bar is shown.  This should complete the
 *  update of the ImageView to handle more than 32767 spectra.
 *
 *  Revision 1.53  2005/08/16 23:07:50  dennis
 *  Icon displaying selection info and sort order now fixed.
 *  A small spacer panel still needs to be added at the bottom
 *  of the sort/selection panel, when the horizontal scroll bar is
 *  enabled, to keep the relationship between the rows of the image
 *  and the rows of the sort/selection panel.
 *
 *  Revision 1.52  2005/08/15 02:00:29  dennis
 *  Now reinitialize everything if DataSet is changed.
 *  Moved border to be around selection & image & vertical scrollbar
 *  to better group selections with image rows.  There are still
 *  problems with the selection image when there are so many spectra
 *  that vertical scrolling is required.
 *
 *  Revision 1.51  2005/08/14 21:39:38  dennis
 *  Now handles arbitrarily many spectra, as needed for SASI, SXD
 *  and the LANSCE SCD.
 *  Improved vertical alignment between graph and image areas.
 *  Some problems still remain with the selection and sort indicators
 *  on the left side of the image area, but it should be serviceable.
 *
 *  Revision 1.50  2005/05/25 20:24:46  dennis
 *  Now calls convenience method WindowShower.show() to show
 *  the window, instead of instantiating a WindowShower object
 *  and adding it to the event queue.
 *
 *  Revision 1.49  2005/03/31 22:34:38  dennis
 *  Fixed option to use colored graphs by moving call to set graph
 *  data before call to set graph color.  Earlier versions of
 *  graphJPanel, allowd setting color and data in either order;
 *  current version requires data to be set first.
 *
 *  Revision 1.48  2004/07/16 18:30:17  dennis
 *  Fixed improper comparison with Float.NaN
 *
 *  Revision 1.47  2004/03/19 17:18:44  dennis
 *  Removed unused variables
 *
 *  Revision 1.46  2004/03/15 06:10:55  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.45  2004/03/15 03:29:01  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.44  2003/12/12 18:12:25  dennis
 *  The "main" test program now uses the WindowShower utility class to
 *  display the ImageView from the Swing event handling thread, instead
 *  of showing it directly.
 *
 *  Revision 1.43  2003/07/09 15:35:06  dennis
 *  The XConversions table for the image now uses the new form of
 *  DataSetXConversionsTable.showConversions() method, which rebins
 *  to a specified XScale.  The conversions table now shows the
 *  correct counts at a pixel, when the viewer has constructed a
 *  rebinned data set.
 *
 *  Revision 1.42  2003/07/05 19:17:09  dennis
 *  Adapted to the new form of the setNamedColorModel() method from the
 *  ImageJPanel class.
 *
 *  Revision 1.41  2003/04/18 15:21:53  dennis
 *  Now explicitly sets vertical scrolling true, so that vertical
 *  scrolling is done when there are more spectra than pixels.
 *
 *  Revision 1.40  2003/02/22 23:14:37  dennis
 *  Added a couple of checks for null DataSet and null Data blocks.
 *
 *  Revision 1.39  2002/12/01 16:13:35  dennis
 *  Now handles case of null Data block by using a row of zeros.
 *
 *  Revision 1.38  2002/11/27 23:24:57  pfpeterson
 *  standardized header
 *
 *  Revision 1.37  2002/10/04 14:38:40  dennis
 *  Simplified calculation of scrolled image position when scrolling
 *  to the "Pointed At" spectrum.
 *
 *  Revision 1.36  2002/08/02 15:33:30  dennis
 *  improved handling of POINTED_AT_CHANGED messages
 *
 *  Revision 1.35  2002/07/29 16:46:53  dennis
 *  ImageView no longer sets "PointedAt" to 0 when drawing it's
 *  default horizontal graph.  DataSet observers are no longer
 *  notified of the default graph drawn by ImageView.
 *
 *  Revision 1.34  2002/07/24 23:21:55  dennis
 *  Now updates X-Conversions table when POINTED_AT_CHANGED
 *  message is received.
 *
 *  Revision 1.33  2002/07/23 18:21:02  dennis
 *  Now sends/processes POINTED_AT_CHANGED messages when the
 *  pointed at "X" value is changed as well as when the
 *  pointed at Data block is changed.
 *  Added method SyncVImageScrollBar to scroll to pointed
 *  at Data block if the image has a vertical scroll bar.
 *
 *  Revision 1.32  2002/07/12 18:39:19  dennis
 *  Now traps for invalid (null) XScale from the x_scale_ui
 *  and uses the result from getXRange() for the DataSet as
 *  the default.
 *
 *  Revision 1.31  2002/06/19 22:34:45  dennis
 *  Fixed problem for DataSets that contain mixtures of
 *  histograms and functions. Now separate XScales are used
 *  for histograms and functions, with the functions evaluated
 *  at the centers of the bins used for the histograms.
 *
 *  Revision 1.30  2002/06/17 22:09:09  dennis
 *  When the image is "zoomed", the graph is now redrawn using the
 *  same x interval as the image.  Also, the x interval for the
 *  graph is no longer reset when the y-scale slider is adjusted.
 *
 *  Revision 1.29  2002/05/30 22:57:16  chatterjee
 *  Added print feature
 *
 *  Revision 1.28  2002/03/13 16:11:36  dennis
 *  Converted to new abstract Data class.
 *
 */
package DataSetTools.viewer.Image;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.viewer.util.*;
import DataSetTools.util.SharedData;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.ViewTools.Panels.Graph.*;
import gov.anl.ipns.ViewTools.Panels.Image.*;
import gov.anl.ipns.ViewTools.Panels.Transforms.*;
import gov.anl.ipns.ViewTools.UI.*;
import gov.anl.ipns.ViewTools.Components.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import DataSetTools.components.ui.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * The image view of a Data Set.
 *
 * @see DataSetTools.dataset.DataSet
 *
 * @version 1.01
 *
 */

public class ImageView extends    DataSetViewer
                       implements Serializable
{ 
  private static final String TRACK_IMAGE_CURSOR = 
                                         "Graph Cursor Tracks Image Cursor";
  private static final String HORIZONTAL_SCROLL  = "Horizontal Scroll";
  private static final String H_GRAPH_REBINNED   = "Graph Rebinned Data";

  public static final String NO_MULTI_PLOT        = "Don't Graph Selected";
  public static final String MULTI_PLOT_OVERLAY   = "Overlaid";
  public static final String MULTI_PLOT_VERTICAL  = "Shift Vertically";
  public static final String MULTI_PLOT_DIAGONAL  = "Shift Diagonally";
  public static final String MULTI_PLOT_COLOR     = "Color Graphs";
  public static final String MULTI_PLOT_HIDDEN_LINES = "Remove Hidden Lines";

  private static final int LEFT_SPACER_SIZE = 8;
  Dimension MIN_TOP_BOX    = new Dimension( LEFT_SPACER_SIZE, 1  );
  Dimension MAX_BOTTOM_BOX = new Dimension( LEFT_SPACER_SIZE, 17 );

                                                   // Image and border
  private ImageJPanel2 image_Jpanel;   
  private ImageJPanel2 selection_image;
  private ImageJPanel  color_scale_image;
  private JScrollPane  image_scroll_pane;
  private JScrollBar   vert_scroll_bar;
  private int          max_num_rows_to_show;
  private int          rows_offset_from_scroll_top;

  private String       current_multi_plot_mode = MULTI_PLOT_DIAGONAL;
  private final int    MAX_PLOTS = 16;

  JPanel image_container    = new JPanel();        // Panels to contain all
  JPanel graph_container    = new JPanel();        // parts associated with
  JPanel graph_right_filler = new JPanel();        // graph and image area
  Box.Filler sel_image_filler   = new Box.Filler( MIN_TOP_BOX, 
                                                  MIN_TOP_BOX, 
                                                  MIN_TOP_BOX );

  private SplitPaneWithState   main_split_pane = null;
  private SplitPaneWithState   left_split_pane = null;

  private XScaleChooserUI      x_scale_ui      = null;

  private JPanel                   image_data_panel  = new JPanel();
  private DataSetXConversionsTable image_table;

  private JPanel                   graph_data_panel  = new JPanel();
  private DataSetXConversionsTable graph_table;

                                              // panel for the horizontal graph
  private GraphJPanel  h_graph      = new GraphJPanel();
  private JScrollPane  hgraph_scroll_pane;

  private JSlider      log_scale_slider = new JSlider();
  private JSlider      hgraph_scale_slider = new JSlider(JSlider.HORIZONTAL,
                                                         0, 1000, 0);
  private ClosedInterval  y_range;
  
  private JCheckBoxMenuItem track_image_cursor_button = null;
  private JCheckBoxMenuItem use_color_button    = null;
  private JCheckBoxMenuItem remove_hidden_lines = null;

  private boolean image_sent_pointed_at = false;

  private boolean debug = false;

/* --------------------------------------------------------------------------
 *
 * CONSTRUCTORS
 */

/* ------------------------------------------------------------------------ */
/**
 *   Construct and ImageView of the specified data_set, starting with the 
 *   specified initial state.
 *
 *   @param  data_set   The DataSet to view.
 *   @param  state      The initial state for the viewer.  If this is null,
 *                      a default state will be used.
 */
public ImageView( DataSet data_set, ViewerState state ) 
{
  super( data_set, state );        

  AddOptionsToMenu();

  if ( !validDataSet() )
    return;
  JMenuBar jmb= getMenuBar();
  gov.anl.ipns.Util.Sys.PrintComponentActionListener.setUpMenuItem( jmb, this );
  init();
  MakeImage( false );
  getState().setZoomRegion( image_Jpanel.getLocalWorldCoords(), data_set );

  DrawDefaultDataBlock();
  UpdateHGraphRange();
}


/* -----------------------------------------------------------------------
 *
 *  PUBLIC METHODS
 *
 */

/* ----------------------------- redraw --------------------------------- */
/**
 * Redraw all or part of the image. The amount that needs to be redrawn is
 * determined by the "reason" parameter.  
 *
 * @param  reason  The reason the redraw is needed.
 *
 */
public void redraw( String reason )
{
  if ( !validDataSet() )
    return;

  if ( reason.equals( IObserver.SELECTION_CHANGED ))
  {
    MakeSelectionImage( true );
    DrawSelectedHGraphs();
  }
  else if ( reason.equals( IObserver.POINTED_AT_CHANGED ))
  {
    if ( image_Jpanel.isDoingBox() )    // don't interrupt the zoom in process
      return;

    float n_data = getDataSet().getNum_entries();

    if ( debug )
    {
      System.out.println("ImageView.redraw(), reason = " + reason );
      System.out.println("PAX  = " + getDataSet().getPointedAtX() );
    }

    if ( n_data > 0 )
    {
      if ( !image_sent_pointed_at )
      {
        floatPoint2D pt = image_Jpanel.getCurrent_WC_point();
        int   index = getDataSet().getPointedAtIndex();
        float x     = getDataSet().getPointedAtX();
        pt.y = index;
        pt.y = pt.y * ( n_data - 1 )/ n_data + 0.5f;
        if ( !Float.isNaN(x)  )
          pt.x = x;

        SyncVImageScrollBar();
        if ( index != DataSet.INVALID_INDEX && !Float.isNaN(x) )
        {
          XScale new_scale = getXConversionScale();
          image_table.showConversions( x, index, new_scale );
        }

        image_Jpanel.set_crosshair_WC( pt );

        floatPoint2D WC_xy = new floatPoint2D(x,0);
        h_graph.set_crosshair_WC( WC_xy );
        h_graph.repaint();

        getState().set_int( ViewerState.POINTED_AT_INDEX, index );
        getState().set_float( ViewerState.POINTED_AT_X, pt.x );
      }
      else
        image_sent_pointed_at = false;   // only skip one POINTED_AT message
    }
  }
  else if ( reason.equals( XScaleChooserUI.N_STEPS_CHANGED ) ||
            reason.equals( XScaleChooserUI.X_RANGE_CHANGED )  )
  {
    MakeImage( true );
    DrawSelectedHGraphs();
    UpdateHGraphRange();
  }
  else          
  {
    init();
    MakeImage( true );
    DrawSelectedHGraphs();
    UpdateHGraphRange();
  }
}

/* ----------------------------- setDataSet ------------------------------- */
/**
 * Specify a different DataSet to be shown by this viewer.
 *
 * @param  ds   The new DataSet to view  
 *
 */

public void setDataSet( DataSet ds )
{
  if ( ds == null )
    return;

  if ( ds.getNum_entries() == 0 )
    return;

  if ( !validDataSet() )
    return;

  setVisible(false);
  super.setDataSet( ds );

  init(); 
  redraw( NEW_DATA_SET );

  max_num_rows_to_show = ds.getNum_entries();
  rows_offset_from_scroll_top = 0;
                                                        // restore the old zoom 
                                                        // region if it's valid 
  CoordBounds zoom_region = getState().getZoomRegion( ds );
  if ( zoom_region != null )
    image_Jpanel.setLocalWorldCoords( zoom_region );
                                                        // syncrhonize the 
                                                        // saved zoom region,
                                                        // with the panel's 
  getState().setZoomRegion( image_Jpanel.getLocalWorldCoords(), ds );

  DrawDefaultDataBlock();
  setVisible(true);
}


/* ------------------------ getXConversionScale -------------------------- */
 /**
  *  Return an XScale specified by the user to be used to control X-Axis 
  *  conversions.
  *
  *  @return  The current values from the number of bins control and the
  *           x range control form the Xscale that is returned, if no valid
  *           XScale is present, the XScale returned by the DataSet's
  *           getXRange() method is used..
  */
 public XScale getXConversionScale()
  {
    XScale x_scale = x_scale_ui.getXScale();
    if ( x_scale == null )                          // if none specified
      x_scale = getDataSet().getXRange();           // use default

    return x_scale;
  }


/* ------------------------- getDisplayComponent -------------------------- */
 /**
  *  Get the JComponent that contains the image of the data, without
  *  any associated controls or auxillary displays.
  */
  public JComponent getDisplayComponent()
  {
    return image_scroll_pane;
  }


/* ----------------------------- main ------------------------------------ */
/*
 *  For testing purposes only
 */
public static void main(String[] args)
{
  DataSet   data_set   = new DataSet("Sample DataSet", "Sample log-info");
  data_set.setX_units( "Test X Units" );
  data_set.setX_label("Text X Label" );
  data_set.setY_units( "Test Y Units" );
  data_set.setY_label("Text Y Label" );

  Data          spectrum;     // data block that will hold a "spectrum"
  float[]       y_values;     // array to hold the "counts" for the spectrum
  UniformXScale x_scale;      // "time channels" for the spectrum

  for ( int id = 1; id < 10; id++ )            // for each id
  {
    x_scale = new UniformXScale( 1, 5, 50 );   // build list of time channels

    y_values = new float[50];                       // build list of counts
    for ( int channel = 0; channel < 50; channel++ )
      y_values[ channel ] = (float)Math.sin( id * channel / 10.0 );

    spectrum = new FunctionTable( x_scale, y_values, id );  
                                                    // put it into a "Data"
                                                    // object and then add
    data_set.addData_entry( spectrum );             // that data object to
                                                    // the data set
  }

  ImageView image_view = new ImageView( data_set, null );
  JFrame f = new JFrame("Test for ImageView.class");
  f.setBounds(0,0,600,400);
  f.setJMenuBar( image_view.getMenuBar() );
  f.getContentPane().add(image_view);

  WindowShower.show( f );
}
   

/* -----------------------------------------------------------------------
 *
 * PRIVATE METHODS
 *
 */ 

private void init()
{

  if ( main_split_pane != null )     // get rid of the old components first
  {
    image_data_panel.removeAll(); 
    graph_data_panel.removeAll(); 
    image_scroll_pane.removeAll(); 
    hgraph_scroll_pane.removeAll(); 
    left_split_pane.removeAll(); 
    main_split_pane.removeAll(); 
    image_container.removeAll();
    graph_container.removeAll();
    removeAll();
  }
  image_Jpanel = new ImageJPanel2();
  image_Jpanel.setNamedColorModel( 
               getState().get_String(ViewerState.COLOR_SCALE), true, true );
                                               // make box to contain both the
                                               // image and selection indicator
  Box sel_image_box = Box.createVerticalBox();
  selection_image = new ImageJPanel2();
  selection_image.setNamedColorModel( IndexColorMaker.GRAY_SCALE, false, true );
  selection_image.enableAutoDataRange( false );
  selection_image.setDataRange( 0, 1 );

  sel_image_box.add( new Box.Filler( MIN_TOP_BOX, MIN_TOP_BOX, MIN_TOP_BOX ));
  sel_image_box.add( selection_image );
  sel_image_filler.changeShape( MIN_TOP_BOX, MIN_TOP_BOX, MIN_TOP_BOX );
  sel_image_box.add( sel_image_filler );

  sel_image_box.setMaximumSize( new Dimension(LEFT_SPACER_SIZE, 3000) );
  sel_image_box.setPreferredSize( new Dimension(LEFT_SPACER_SIZE, 0) );

  vert_scroll_bar = new JScrollBar(); 

  image_scroll_pane = new JScrollPane( image_Jpanel,
                                      JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                                          );

  image_scroll_pane.getHorizontalScrollBar().addAdjustmentListener( 
                                             new ImageHScrollBarListener() );

  image_container.setLayout( new BorderLayout() );
  image_container.add( sel_image_box, BorderLayout.WEST );
  image_container.add( image_scroll_pane, BorderLayout.CENTER );
  image_container.add( vert_scroll_bar, BorderLayout.EAST );

  image_data_panel  = new JPanel();
  image_table = new DataSetXConversionsTable( getDataSet() );

  graph_data_panel  = new JPanel();
  graph_table = new DataSetXConversionsTable( getDataSet() );

  h_graph = new GraphJPanel();
  hgraph_scroll_pane = new JScrollPane( h_graph,
                                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                                          );
  log_scale_slider = new JSlider();
  hgraph_scale_slider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 0);
  int value = (int)(10 * getState().get_float( ViewerState.AUTO_SCALE ));
  hgraph_scale_slider.setValue( value );

  main_split_pane = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
                                          MakeDisplayArea(),
                                          MakeControlArea(),
                                          0.7f );

  String title = getDataSet().toString();
  AttributeList attr_list = getDataSet().getAttributeList();
  Attribute     attr      = attr_list.getAttribute(Attribute.RUN_TITLE);
  if ( attr != null )
   title = attr.getStringValue();

  TitledBorder border = new TitledBorder(
                                    LineBorder.createBlackLineBorder(), title);
  border.setTitleFont( FontUtil.BORDER_FONT );
  main_split_pane.setBorder( border );

  this.setLayout( new GridLayout(1,1) );
  this.add( main_split_pane );

  MakeConnections();                       // Add event handlers

  y_range = getDataSet().getYRange();

  main_split_pane.my_setDividerLocation( 0.7f );
  left_split_pane.my_setDividerLocation( 0.7f );

}

/* ------------------------- AddOptionsToMenu --------------------------- */

private void AddOptionsToMenu()
{
  OptionMenuHandler option_menu_handler = new OptionMenuHandler();
  JMenu option_menu = menu_bar.getMenu( OPTION_MENU_ID );

                                                     // color options
  JMenu color_menu = new ColorScaleMenu( option_menu_handler );
  option_menu.add( color_menu );
                                               // multiplot graph options
  JMenu multiplot_menu = new JMenu( "Graph Selected (" + MAX_PLOTS + " Max)" );

  MultiPlotMenuHandler multi_plot_menu_handler = new MultiPlotMenuHandler();
  option_menu.add( multiplot_menu );

  ButtonGroup group = new ButtonGroup();
  JRadioButtonMenuItem r_button = new JRadioButtonMenuItem( NO_MULTI_PLOT );
  if ( current_multi_plot_mode.equals( NO_MULTI_PLOT ))
    r_button.setSelected(true);
  r_button.addActionListener( multi_plot_menu_handler );
  multiplot_menu.add( r_button );
  group.add( r_button );

  r_button = new JRadioButtonMenuItem( MULTI_PLOT_DIAGONAL );
  if ( current_multi_plot_mode.equals( MULTI_PLOT_DIAGONAL ))
    r_button.setSelected(true);
  r_button.addActionListener( multi_plot_menu_handler );
  multiplot_menu.add( r_button );
  group.add( r_button );

  r_button = new JRadioButtonMenuItem( MULTI_PLOT_VERTICAL );
  if ( current_multi_plot_mode.equals( MULTI_PLOT_VERTICAL ))
    r_button.setSelected(true);
  r_button.addActionListener( multi_plot_menu_handler );
  multiplot_menu.add( r_button );
  group.add( r_button );

  r_button = new JRadioButtonMenuItem( MULTI_PLOT_OVERLAY );
  if ( current_multi_plot_mode.equals( MULTI_PLOT_OVERLAY ))
    r_button.setSelected(true);
  r_button.addActionListener( multi_plot_menu_handler );
  multiplot_menu.add( r_button );
  group.add( r_button );
                                                 // remove_hidden_lines option 
  remove_hidden_lines = new JCheckBoxMenuItem( MULTI_PLOT_HIDDEN_LINES );
  remove_hidden_lines.addActionListener( multi_plot_menu_handler );
  remove_hidden_lines.setState( true );
  multiplot_menu.add( remove_hidden_lines );

                                                   // multiplot color option
  use_color_button = new JCheckBoxMenuItem( MULTI_PLOT_COLOR );
  use_color_button.addActionListener( multi_plot_menu_handler );
  use_color_button.setState( false );
  multiplot_menu.add( use_color_button );

                                                   // cursor tracking option
  track_image_cursor_button = new JCheckBoxMenuItem(TRACK_IMAGE_CURSOR );
  track_image_cursor_button.setState( true );
  option_menu.add( track_image_cursor_button );

                                                    // Horizontal scroll option
  JCheckBoxMenuItem cb_button = new JCheckBoxMenuItem( HORIZONTAL_SCROLL );
  cb_button.setState( getState().get_boolean( ViewerState.H_SCROLL ) );
  cb_button.addActionListener( option_menu_handler );
  option_menu.add( cb_button );

  cb_button = new JCheckBoxMenuItem( H_GRAPH_REBINNED );
  cb_button.setState( getState().get_boolean( ViewerState.REBIN ) );
  cb_button.addActionListener( option_menu_handler );
  option_menu.add( cb_button );

}


/* ------------------------- MakeImage ---------------------------- */
/*
 *  Construct displayed image from the data in the data set.
 *
 */
private void MakeImage( boolean redraw_flag )
{ 
  if ( !validDataSet() )
    return;

  float   image_data[][];
  Data    data_block;

  int num_rows = getDataSet().getNum_entries();
  max_num_rows_to_show = num_rows;
  rows_offset_from_scroll_top = 0;

  UniformXScale x_scale = (UniformXScale)getXConversionScale();
  int num_cols = x_scale.getNum_x();
  if ( num_cols < 1 || num_rows < 1   )      // #### degenerate size of JPanel
    return;

  float x_min = x_scale.getStart_x();
  float x_max = x_scale.getEnd_x();
  float step  = 0;
  if ( num_cols > 1 )
    step = (x_max - x_min) / (num_cols-1);

  UniformXScale histogram_scale = x_scale,           // We will need different
                function_scale  = x_scale;           // XScales for histograms
                                                     // and functions.

  if ( num_cols == 1 )                               // invalid for histograms
    histogram_scale = new UniformXScale(x_min-1,x_max+1,num_cols+1);
  else
  {                                                  // adjust x_scale to hist.
    int num_histograms = 0;                          // or function, which ever
    for ( int i = 0; i < num_rows; i++ )             // is more common.
      if ( getDataSet().getData_entry(i).isHistogram() )
        num_histograms++;

    if ( num_histograms > num_rows/2 )                // derive function_scale
                                                      // using bin centers 
      function_scale  = new UniformXScale(x_min+step/2,x_max-step/2,num_cols-1);
 
    else                                              // derive histogram_scale
                                                      // with given bin centers 
      histogram_scale = new UniformXScale(x_min-step/2,x_max+step/2,num_cols+1);
  }

  image_data = new float[ num_rows ][];

  for ( int i = 0; i < num_rows; i++ )
  {
    data_block = getDataSet().getData_entry(i);
    if ( data_block == null )
      image_data[i] = new float[ num_cols ];         // row of zeros if no Data 
    else if ( data_block.isHistogram() )
      image_data[i] = data_block.getY_values(histogram_scale,IData.SMOOTH_NONE);
    else
      image_data[i] = data_block.getY_values(function_scale,IData.SMOOTH_NONE);
  }
                              // set the log scale and image data, but don't
                              // remake the image yet. It's done when the
                              // component is initially resized.     
  image_Jpanel.changeLogScale( log_scale_slider.getValue(), false );
  VirtualArray2D va2d = new VirtualArray2D(image_data);

  image_Jpanel.setData( va2d, redraw_flag );

                                          // use slightly different coordinate
                                          // system for functions and histograms
  CoordBounds bounds;
  if ( image_data[0].length < num_cols )                 // histogram
    bounds = new CoordBounds( x_min, 0, x_max, num_rows );
  else                  
  {                                                      // function
    float delta_x;
    if ( num_cols > 1 )
      delta_x = (x_max - x_min) / (num_cols - 1);
    else
      delta_x = 0;
    bounds = new CoordBounds(x_min-delta_x/2, 0, x_max+delta_x/2, num_rows);
  }
  image_Jpanel.initializeWorldCoords( bounds );
  
  CoordBounds sel_bounds = new CoordBounds( 0, 0, 1, 1 );
  selection_image.initializeWorldCoords( sel_bounds );

  if ( getState().get_boolean( ViewerState.H_SCROLL ) )
    SetHorizontalScrolling( true );                   // this was needed to
                                                      // switch DataSets after
                                                      // HScroll was enabled   
  ConfigureVerticalScrollBar();
  MakeSelectionImage( redraw_flag );
}


/* --------------------------- MakeSelectionImage -------------------------- */

private void MakeSelectionImage( boolean redraw_flag )
{
  if ( getDataSet() == null )                         // nothing to do....
    return;

  int   num_rows = getDataSet().getNum_entries();
  if ( num_rows == 0 )
    return;

  CoordBounds image_bounds = image_Jpanel.getLocalWorldCoords();
  int first_row = image_Jpanel.ImageRow_of_WC_y( image_bounds.getY1() );
  int last_row  = image_Jpanel.ImageRow_of_WC_y( image_bounds.getY2() );

  int   group_id;
  float sel_image_data[][] = new float[ last_row - first_row + 1 ][ 2 ];
  Data  data_block;
  float max_group_id = getDataSet().getMaxGroupID();

  for ( int i = first_row; i <= last_row; i++ )
  {
    data_block = getDataSet().getData_entry(i);
    if ( data_block == null )                       // something is wrong so
      return;                                       // we can't proceed.

                                                    // col 1 has select flags
    if ( data_block.isSelected() )
      sel_image_data[i-first_row][1] = 1;
    else
      sel_image_data[i-first_row][1] = 0;
                                                    // col 0 has group_id "code"
    group_id = data_block.getGroup_ID();
    if ( max_group_id >= 0 )                        // group -1 maps to 0
      sel_image_data[i-first_row][0] = (group_id+1)/(max_group_id+1);
    else                                           
      sel_image_data[i-first_row][0] = 0; 
  }

  VirtualArray2D sel_image_va2D= new VirtualArray2D(sel_image_data);
  selection_image.setData( sel_image_va2D, redraw_flag );
}


/* ---------------------------- MakeControlArea --------------------------- */
/*
 *  Construct the group of controls for color, logscale, etc. 
 */
private Component MakeControlArea()
{
  Box control_area = new Box( BoxLayout.Y_AXIS );

  if ( getDataSet().getNum_entries() == 0 )
  {
    control_area.add( new JLabel( "ERROR: NO DATA" ) ); 
    return control_area;
  }

  String label = getDataSet().getX_units();         
  UniformXScale x_scale  = getDataSet().getXRange();
  float x_min = x_scale.getStart_x();
  float x_max = x_scale.getEnd_x();
  int n_steps = getDataSet().getMaxXSteps();
  if ( getDataSet().getData_entry(0).isHistogram() )
    n_steps = n_steps - 1;

  x_scale_ui = new XScaleChooserUI( "X Scale", label, x_min, x_max, n_steps );
  control_area.add( x_scale_ui );

                                                  // make a color scale bar
  color_scale_image = new ColorScaleImage();
  color_scale_image.setNamedColorModel( 
                getState().get_String( ViewerState.COLOR_SCALE), true, true );
  control_area.add( color_scale_image );

  log_scale_slider.setPreferredSize( new Dimension(120,50) );
  log_scale_slider.setValue( getState().get_int( ViewerState.BRIGHTNESS) );
  log_scale_slider.setMajorTickSpacing(20);
  log_scale_slider.setMinorTickSpacing(5);
  log_scale_slider.setPaintTicks(true);

  TitledBorder border = new TitledBorder(
                             LineBorder.createBlackLineBorder(),"Brightness");
  border.setTitleFont( FontUtil.BORDER_FONT ); 
  log_scale_slider.setBorder( border ); 

  control_area.add( log_scale_slider );

  hgraph_scale_slider.setPreferredSize( new Dimension(120,50) );
  border = new TitledBorder(LineBorder.createBlackLineBorder(),"Auto-Scale");
  border.setTitleFont( FontUtil.BORDER_FONT ); 
  hgraph_scale_slider.setBorder( border );
  control_area.add( hgraph_scale_slider );

  border = new TitledBorder(LineBorder.createBlackLineBorder(),"Image Data");
  border.setTitleFont( FontUtil.BORDER_FONT ); 
  image_data_panel.setBorder( border ); 
  image_data_panel.setLayout( new GridLayout(1,1) );
  image_data_panel.add( image_table.getTable() );
  control_area.add( image_data_panel );   
   
  border = new TitledBorder(LineBorder.createBlackLineBorder(),"Graph Data");
  border.setTitleFont( FontUtil.BORDER_FONT ); 
  graph_data_panel.setBorder( border );
  graph_data_panel.setLayout( new GridLayout(1,1) );
  graph_data_panel.add( graph_table.getTable() );
  control_area.add( graph_data_panel );      

  JPanel filler = new JPanel();
  filler.setPreferredSize( new Dimension( 120, 2000 ) );
  control_area.add( filler );
 
  return control_area;
}

/* ---------------------------- MakeDisplayArea --------------------------- */
/*
 *  Construct the group of controls for color, logscale, etc. 
 */
private JSplitPane MakeDisplayArea()
{
  left_split_pane = new SplitPaneWithState( JSplitPane.VERTICAL_SPLIT,
                                    MakeImageArea(), 
                                    MakeHorGraphArea(),
                                    0.70f );

//  left_split_pane.setContinuousLayout( false );

  return left_split_pane;
}


/* ------------------------ MakeHorizontalGraphArea ----------------------- */
/*
 * Construct panel for the horizontal graph.
 */

private JComponent MakeHorGraphArea( )
{
  TitledBorder border = new TitledBorder( LineBorder.createBlackLineBorder(),
                                         "HORIZONTAL GRAPH DISPLAY" );
  border.setTitleFont( FontUtil.BORDER_FONT );

  hgraph_scroll_pane.setBorder( border ); 

  JPanel graph_left_filler  = new JPanel();

  graph_left_filler.setPreferredSize( new Dimension(LEFT_SPACER_SIZE, 0 ) );
  graph_right_filler.setPreferredSize( new Dimension( 20, 0 ) );
  graph_right_filler.setVisible( false );

  graph_container.setLayout( new BorderLayout() );
  graph_container.add( graph_left_filler,  BorderLayout.WEST );
  graph_container.add( graph_right_filler, BorderLayout.EAST );
  graph_container.add( hgraph_scroll_pane, BorderLayout.CENTER );
  return graph_container; 
}


/* --------------------------- MakeImageArea ---------------------------- */
/*
 * Construct a panel with a border for the main image area.
 */

private JComponent MakeImageArea( )
{
  String title;

  OperationLog op_log = getDataSet().getOp_log();
  if ( op_log.numEntries() <= 0 )
    title = "IMAGE DISPLAY AREA";
  else
    title = op_log.getEntryAt( op_log.numEntries() - 1 );

  TitledBorder border = new TitledBorder(
                                LineBorder.createBlackLineBorder(), title );
  border.setTitleFont( FontUtil.BORDER_FONT );

  image_container.setBorder( border );

  return image_container;
}


/*--------------------------- MakeConnections ---------------------------- */
/*
 *  Add listeners for events that need to be handled.
 */
private void MakeConnections()
{
   hgraph_scale_slider.addChangeListener( new HGraphScaleEventHandler() );   

   log_scale_slider.addChangeListener( new LogScaleEventHandler() );   

   image_Jpanel.addMouseMotionListener( new ImageMouseMotionAdapter() );
   image_Jpanel.addMouseListener( new ImageMouseAdapter() );
   image_Jpanel.addMouseListener( new ImageZoomMouseHandler() );
   image_Jpanel.addComponentListener( new ImageResizeListener() );

   vert_scroll_bar.addAdjustmentListener( new VerticalScrollListener() );

   h_graph.addMouseMotionListener( new HGraphMouseMotionAdapter() );
   h_graph.addMouseListener( new HGraphMouseAdapter() );

   x_scale_ui.addActionListener( new XScaleListener() );

   SelectionKeyAdapter key_adapter = new SelectionKeyAdapter();
   image_Jpanel.addKeyListener( key_adapter );
   h_graph.addKeyListener( new ConsumeKeyAdapter() );
}


/* ------------------------ SetHorizontalScrolling ------------------------ */
private void SetHorizontalScrolling( boolean state )
{
  getState().set_boolean( ViewerState.H_SCROLL, state );
  left_split_pane.setVisible( false );
  image_Jpanel.setHorizontalScrolling( state );
  if ( state )                                 // position the image scroll bar
  {
    JScrollBar scroll_bar = image_scroll_pane.getHorizontalScrollBar();
    int min = scroll_bar.getMinimum();
    int max = scroll_bar.getMaximum();
    int position = (int) ((max-min)  
                        * getState().get_float(ViewerState.H_SCROLL_POSITION) 
                        + min ); 
    scroll_bar.setValue( position );
  }
  image_scroll_pane.doLayout();
                                         // make the graph have same preferred
                                         // width as the image, so horizontal
                                         // scroll bars line up better.
  Dimension preferred_size = image_Jpanel.getPreferredSize();
  preferred_size.height = 0;
  h_graph.my_setPreferredSize( preferred_size );
  h_graph.setHorizontalScrolling( state );

  if ( state )                            // position the graph scroll bar
  {
    SyncHGraphScrollBar();
    sel_image_filler.changeShape( MAX_BOTTOM_BOX,      // put larger filler at
                                  MAX_BOTTOM_BOX,      // bottom of selection
                                  MAX_BOTTOM_BOX );    // icon
  }
  else
    sel_image_filler.changeShape( MIN_TOP_BOX, MIN_TOP_BOX, MIN_TOP_BOX );

  hgraph_scroll_pane.doLayout();

  left_split_pane.invalidate();
  left_split_pane.doLayout();
  left_split_pane.setVisible( true );    // it was necessary to set the 
                                         // visibilty off then on again to
                                         // have the scroll bars remain on
                                         // (or off) as the DataSet and 
                                         // number of bins are changed.
  ConfigureVerticalScrollBar();
  MakeSelectionImage( true );
}


/* ------------------------ setVerticalViewableRegion -------------------- */

private void setVerticalViewableRegion( int first_row )
{
  CoordBounds bounds = image_Jpanel.getLocalWorldCoords();
  float x1 = bounds.getX1(); 
  float y1 = bounds.getY1(); 
  float x2 = bounds.getX2();
  float y2 = bounds.getY2();
  
  Dimension size = image_Jpanel.getSize();
  y1 = first_row;

  int num_rows_displayed = max_num_rows_to_show;
  if ( num_rows_displayed < 1 )
    num_rows_displayed = 1;

  else if ( num_rows_displayed >= size.height )
    num_rows_displayed = size.height-1;

  y1 += rows_offset_from_scroll_top;
  y2 = y1 + num_rows_displayed;

  image_Jpanel.setLocalWorldCoords( x1, y1, x2, y2 );
  image_Jpanel.RebuildImage();
   
  MakeSelectionImage( true );
}


/* ----------------------- ConfigureVerticalScrollBar -------------------- */

private void ConfigureVerticalScrollBar()
{
  if ( image_Jpanel.isShowing() )
  {
    int window_height = 1;                      // set default height if not
                                                // actually shown yet
    Dimension size = image_Jpanel.getSize(); 
    if ( size.height > 0 )
      window_height = size.height;
     
    DataSet ds = getDataSet();
    if ( ds != null )
    {
      int n_rows = ds.getNum_entries();
      vert_scroll_bar.setMinimum( 0 ); 
      vert_scroll_bar.setMaximum( n_rows ); 
      vert_scroll_bar.setVisibleAmount( window_height );
      if ( n_rows > window_height )
      {
        rows_offset_from_scroll_top = 0;
        max_num_rows_to_show = ds.getNum_entries();
        vert_scroll_bar.setVisible( true );

        Dimension bar_size = vert_scroll_bar.getSize();
        bar_size.height = 0;
        graph_right_filler.setPreferredSize( bar_size );
        graph_right_filler.setVisible( true );
      }
      else
      {
        rows_offset_from_scroll_top = 0;
        max_num_rows_to_show = ds.getNum_entries();

        CoordBounds bounds = image_Jpanel.getLocalWorldCoords();
        float x1 = bounds.getX1();
        float y1 = bounds.getY1();               // initially = 0;
        float x2 = bounds.getX2();
        float y2 = bounds.getY2();               // initially = n_rows - 1;

        image_Jpanel.setLocalWorldCoords( x1, y1, x2, y2 );
        image_Jpanel.RebuildImage();

        vert_scroll_bar.setVisible( false );
        graph_right_filler.setVisible( false );
      }
    }
  }
}


/* -------------------------- UpdateImageReadout ------------------------- */

private void UpdateImageReadout( int row )
{
  floatPoint2D float_pt = image_Jpanel.getCurrent_WC_point();
  XScale new_scale = getXConversionScale();
  image_table.showConversions( float_pt.x, row, new_scale );
}


/* -------------------------- SetGraphCursorFromImage ---------------------- */

private void SetGraphCursorFromImage( )
{
  floatPoint2D float_pt = image_Jpanel.getCurrent_WC_point();
                                               // interpolation is needed to
                                               // get the y value along the 
                                               // graph for non-histogram data

  float_pt.y = h_graph.getY_value( float_pt.x, 0 );

  h_graph.set_crosshair_WC( float_pt );
  UpdateHGraphReadout();
}

  
/* ------------------------ DrawPointedAtHGraph --------------------------- */

private boolean DrawPointedAtHGraph()
{
  int pointed_at_row = getDataSet().getPointedAtIndex();

  if ( pointed_at_row >= 0 )
  {
    if ( remove_hidden_lines.getState() )
      h_graph.setRemoveHiddenLines( true );
    else 
      h_graph.setRemoveHiddenLines( false );

    DrawHGraph( pointed_at_row, 0, true );

    return true;
  }

  return false;
}


/* ------------------------ DrawSelectedHGraphs --------------------------- */

private int DrawSelectedHGraphs()
{
  int num_drawn = 0;

  h_graph.clearData();

  if ( !current_multi_plot_mode.equals( NO_MULTI_PLOT ))
  {
    if ( current_multi_plot_mode.equals( MULTI_PLOT_OVERLAY ))
      h_graph.setMultiplotOffsets( 0, 0 );
    else if ( current_multi_plot_mode.equals( MULTI_PLOT_VERTICAL ))
      h_graph.setMultiplotOffsets( 0, 10 );
    else if ( current_multi_plot_mode.equals( MULTI_PLOT_DIAGONAL ))
      h_graph.setMultiplotOffsets( 10, 10 );

    int pointed_at_row = getDataSet().getPointedAtIndex();

    int draw_count = 0;
    int n_rows = getDataSet().getNum_entries();
    boolean too_many = false;
    int i = n_rows-1;
    while ( !too_many && i >= 0 )       // for (int i = n_rows-1; i >= 0; i--)
    {
      if ( getDataSet().isSelected(i) )
      {
        draw_count++;
        if ( i == pointed_at_row )
          DrawHGraph( i, draw_count, true );
        else
          DrawHGraph( i, draw_count, false );

        num_drawn++;
      } 
      i--;
      if ( draw_count >= MAX_PLOTS )
        too_many = true;
    }
  }

  if ( DrawPointedAtHGraph() )
    num_drawn++;

  return num_drawn;
}


/* -------------------------- DrawDefaultDataBlock ------------------------- */

private void DrawDefaultDataBlock()
{

  if ( DrawSelectedHGraphs() <= 0 )      // try to draw selected graphs... if
  {                                      // none, try to draw the last one
                                         // that was pointed at.

    int   last_pointed_at = getDataSet().getPointedAtIndex();

    if (last_pointed_at >= 0 && last_pointed_at < getDataSet().getNum_entries())
      DrawHGraph( last_pointed_at, 0, true );

    else                                 // if none, try to draw data block 0
    {
      Data data_block = getDataSet().getData_entry(0);

      if ( data_block != null )
        DrawHGraph( 0, 0, true );

      else
        System.out.println("ERROR... no Data blocks in DataSet" );
    }
  }
}


/* ---------------------------- DrawHGraph ----------------------------- */

private void DrawHGraph( int index, int graph_num, boolean pointed_at )
{
  Data  data_block = getDataSet().getData_entry( index );

  float x[];
  float y[];
  if ( getState().get_boolean( ViewerState.REBIN ) )
  {
    XScale x_scale = getXConversionScale();
    x = x_scale.getXs();
    y = data_block.getY_values( x_scale, IData.SMOOTH_NONE );
  }
  else
  {
    x = data_block.getX_scale().getXs();
    y = data_block.getY_values();
  }

  Color color_list[] = { 
                         Color.red, 
                         Color.orange, 
                         Color.yellow,
                         Color.green, 
                         Color.cyan, 
                         Color.magenta,
                         Color.gray,
                         Color.white 
                        };

  h_graph.setData( x, y, graph_num, false );
  CoordBounds graph_bounds = h_graph.getGlobalWorldCoords();
  CoordBounds image_bounds = image_Jpanel.getLocalWorldCoords();
  graph_bounds.setBounds( image_bounds.getX1(), graph_bounds.getY1(),
                          image_bounds.getX2(), graph_bounds.getY2() );

  h_graph.initializeWorldCoords( graph_bounds );
  h_graph.setX_bounds( image_bounds.getX1(), image_bounds.getX2() );

  if ( graph_num == 0 ) 
  {
    String border_label = DS_Util.getData_ID_String( getDataSet(), index );

    TitledBorder border = new TitledBorder( LineBorder.createBlackLineBorder(),
                                          border_label );
    border.setTitleFont( FontUtil.BORDER_FONT );
    if ( data_block.isSelected() )
      border.setTitleColor( Color.blue );
    else
      border.setTitleColor( Color.black );
    hgraph_scroll_pane.setBorder( border );

    h_graph.setColor( Color.black, graph_num, false );
  }
  else                       
  {
    if ( pointed_at )
      h_graph.setColor( Color.black, graph_num, false );
    else 
    {
      if ( use_color_button.getState() )
        h_graph.setColor( color_list[ (graph_num - 1) % color_list.length ],
                          graph_num, 
                          false     );
      else
        h_graph.setColor( Color.blue, graph_num, false );
    }
  }
}


/* -------------------------- UpdateHGraphReadout ------------------------- */

private void UpdateHGraphReadout( )
{
  floatPoint2D float_pt = h_graph.getCurrent_WC_point();
  int row = getDataSet().getPointedAtIndex();
  graph_table.showConversions( float_pt.x, float_pt.y, row );
}


/* -------------------------- SyncHGraphScrollBar ------------------------- */

private void SyncHGraphScrollBar()
{
  JScrollBar hi_bar = image_scroll_pane.getHorizontalScrollBar();
  JScrollBar hg_bar = hgraph_scroll_pane.getHorizontalScrollBar();

  if ( !hi_bar.isVisible() || !hg_bar.isVisible() )
    return;

  float hi_bar_min   = hi_bar.getMinimum();
  float hi_bar_max   = hi_bar.getMaximum();
  float hi_bar_value = hi_bar.getValue();

  float hg_bar_min   = hg_bar.getMinimum();
  float hg_bar_max   = hg_bar.getMaximum();
  float fraction     = 0;
  int   hg_bar_value;

  if ( hi_bar_max == hi_bar_min )
    hg_bar_value = 0;
  else
  {
    fraction = (hi_bar_value - hi_bar_min) / (hi_bar_max - hi_bar_min);
    hg_bar_value = (int)(hg_bar_min + (hg_bar_max - hg_bar_min) * fraction );
  }                      
  hg_bar.setValue( hg_bar_value );

  getState().set_float( ViewerState.H_SCROLL_POSITION, fraction );
}
 

/* -------------------------- SyncVImageScrollBar ------------------------- */

private void SyncVImageScrollBar()
{
  JScrollBar vi_bar = image_scroll_pane.getVerticalScrollBar();

  if ( vi_bar == null || !vi_bar.isVisible() )
    return;

  DataSet ds = getDataSet();
  if ( ds == null )
    return;

  int n_rows = ds.getNum_entries();
  if ( n_rows <= 100 )
    return; 

  int row = ds.getPointedAtIndex();
  if ( row == DataSet.INVALID_INDEX ) 
    return;

  float vi_bar_min  = vi_bar.getMinimum();
  float vi_bar_max  = vi_bar.getMaximum();
  float bar_height  = vi_bar.getSize().height;

  if ( bar_height >= n_rows )      // scrolling not needed
    return;

  float fraction = 0;
  int   vi_bar_value;

  if ( vi_bar_max == vi_bar_min )
    vi_bar_value = 0;
  else
  {
    fraction = (row-bar_height/2) / (float)(n_rows-bar_height);
    if ( fraction < 0 )
      fraction = 0;
    if ( fraction > 1 )
      fraction = 1;
    vi_bar_value = (int)( vi_bar_min +
                         (vi_bar_max - vi_bar_min - bar_height) * fraction );
  }

/*
  System.out.println("In SyncVImageScrollBar");
  System.out.println("n_rows       = " + n_rows );
  System.out.println("row          = " + row );
  System.out.println("fraction     = " + fraction );
  System.out.println("vi_bar_value = " + vi_bar_value );
  System.out.println("vi_bar_min   = " + vi_bar_min );
  System.out.println("vi_bar_max   = " + vi_bar_max );
  System.out.println("Scrollbar size = " + vi_bar.getSize() );
  System.out.println("knob_height    = " + knob_height );
  System.out.println("scroll size    = " + image_scroll_pane.getSize() );
*/  
  //vi_bar.setValue( vi_bar_value );

  getState().set_float( ViewerState.V_SCROLL_POSITION, fraction );
}


/* ------------------------- ProcessImageMouseEvent ------------------------ */

private Point ProcessImageMouseEvent( MouseEvent e, 
                                      int        last_image_row,
                                      int        last_image_col  )
{
  image_Jpanel.requestFocus();
  
  Point        pix_pt = image_Jpanel.getCurrent_pixel_point();
  floatPoint2D pt     = image_Jpanel.getCurrent_WC_point();
  int row = image_Jpanel.ImageRow_of_PixelRow( pix_pt.y );
  int col = image_Jpanel.ImageCol_of_PixelCol( pix_pt.x );
  if ( debug )
  {
    System.out.println("ProcessImageMouseEvent");
    System.out.println("row = " + row);
    System.out.println("col = " + col);
    System.out.println("WC  = " + pt );
  }
                                        // NOTE: Now that we are notifying
                                        //       observers when Y or X changes
                                        //       these checks may be redundant.
                                        //       Leave them here incase we
                                        //       need to go back to notifying
                                        //       for Y changes only due to
                                        //       performance considerations.

  if ( row != last_image_row || col != last_image_col  )
  {
    if ( getDataSet().getPointedAtIndex() != row  ||
         getDataSet().getPointedAtX()     != pt.x ||
         getDataSet().getNum_entries() == 1        )  // only change if needed
    { 
      getDataSet().setPointedAtX( pt.x ); 
      getDataSet().setPointedAtIndex( row );
      getState().set_int( ViewerState.POINTED_AT_INDEX, row );
      getState().set_float( ViewerState.POINTED_AT_X, pt.x );
      if ( debug )
        System.out.println("IMAGE CALLING NOTIFY: " + pt.x);
      image_sent_pointed_at = true;
      getDataSet().notifyIObservers( IObserver.POINTED_AT_CHANGED );
    }
    DrawSelectedHGraphs();
  }

  if ( col != last_image_col || row != last_image_row )
    UpdateImageReadout( row );

  SyncHGraphScrollBar();

  if ( track_image_cursor_button.getState() )
    SetGraphCursorFromImage();

  return new Point( col, row );
}


/* -------------------------- UpdateHGraphRange --------------------------- */

  private void UpdateHGraphRange()
  {
    int value = hgraph_scale_slider.getValue();
    getState().set_float( ViewerState.AUTO_SCALE, value/10.0f );

    if ( value == 0 )
    {
      h_graph.autoY_bounds();
      TitledBorder border = 
           new TitledBorder(LineBorder.createBlackLineBorder(), "Auto-Scale");
      border.setTitleFont( FontUtil.BORDER_FONT );
      hgraph_scale_slider.setBorder( border );
    }
    else
    {
      float y_min = y_range.getStart_x();
      float y_max = y_range.getEnd_x();
      float range = ( y_max - y_min ) * value / 1000.0f;
      h_graph.setY_bounds( y_min, y_min + range );
      TitledBorder border = new TitledBorder(LineBorder.createBlackLineBorder(),
                         "" + value /10.0f+"%(max)" );
      border.setTitleFont( FontUtil.BORDER_FONT );
      hgraph_scale_slider.setBorder( border );
    }
  }



/* -------------------------------------------------------------------------
 *
 *  INTERNAL CLASSES
 *
 */

/* ---------------------------- LogScaleEventHandler ----------------------- */

private class LogScaleEventHandler implements ChangeListener,
                                              Serializable
{
  public void stateChanged(ChangeEvent e)
  {
    JSlider slider = (JSlider)e.getSource();

                              // set image log scale when slider stops moving
    if ( !slider.getValueIsAdjusting() )
    {
      int value = slider.getValue();
      image_Jpanel.changeLogScale( value, true );
      getState().set_int( ViewerState.BRIGHTNESS, value );
    } 
  }
} 


/* ------------------------- HGraphScaleEventHandler ----------------------- */

private class HGraphScaleEventHandler implements ChangeListener,
                                                 Serializable
{
  public void stateChanged(ChangeEvent e)
  {
    UpdateHGraphRange();
  }
}



/* -------------------------- ImageZoomMouseHandler ----------------------- */

private class ImageZoomMouseHandler extends    MouseAdapter
                                    implements Serializable
{
  public void mouseClicked (MouseEvent e)
  {
    if ( e.getClickCount() == 2 )                 // zoom out to full view 
    {
      if ( vert_scroll_bar != null && vert_scroll_bar.isVisible() )
      {
        DataSet ds = getDataSet();
        if ( ds != null )
          max_num_rows_to_show = ds.getNum_entries();
          rows_offset_from_scroll_top = 0;

        int value = vert_scroll_bar.getValue();
        setVerticalViewableRegion( value );
      }
      getState().setZoomRegion( image_Jpanel.getLocalWorldCoords(),
                                getDataSet()  );
      MakeSelectionImage( true );
      DrawSelectedHGraphs();
    }
  }

  public void mouseReleased(MouseEvent e)         // zoom in to sub region 
  {
    CoordBounds local_bounds = image_Jpanel.getLocalWorldCoords();
    getState().setZoomRegion( local_bounds, getDataSet() );

    MakeSelectionImage( true );
    DrawSelectedHGraphs();

    int y1 = (int)local_bounds.getY1();    
    int y2 = (int)local_bounds.getY2();    
    max_num_rows_to_show = Math.abs(y2 - y1) + 1;
                                                  // if we zoom in while 
                                                  // vertical scroll bar is on
                                                  // displayed region must be
                                                  // offset
    if ( vert_scroll_bar.isVisible() )
      rows_offset_from_scroll_top = Math.abs(vert_scroll_bar.getValue()-y1);
  }
}



/* ------------------------ ImageMouseMotionAdapter ------------------------ */

private class ImageMouseMotionAdapter extends    MouseMotionAdapter
                                      implements Serializable
{
   int last_image_row = -1;           // Record the last postion, so we don't
   int last_image_col = -1;           // do more work than needed as the 
                                      // mouse is dragged.

   public void mouseDragged( MouseEvent e )
   {
     Point last_pt = ProcessImageMouseEvent(e, last_image_row, last_image_col);

     last_image_row = last_pt.y;
     last_image_col = last_pt.x;
   }
}


/* --------------------------- ImageMouseAdapter --------------------------- */

private class ImageMouseAdapter extends    MouseAdapter
                                implements Serializable
{
   public void mousePressed( MouseEvent e )
   {
     ProcessImageMouseEvent(e, -1, -1);
   }
}


/* ----------------------- HGraphMouseMotionAdapter ----------------------- */

private class HGraphMouseMotionAdapter extends    MouseMotionAdapter
                                       implements Serializable
{
   public void mouseDragged( MouseEvent e )
   {
     UpdateHGraphReadout();
   }
}


/* -------------------------- HGraphMouseAdapter -------------------------- */

private class HGraphMouseAdapter extends    MouseAdapter
                                 implements Serializable
{
   public void mousePressed( MouseEvent e )
   {
     UpdateHGraphReadout();
   }
}


/* --------------------------- SelectionKeyAdapter ------------------------- */

private class SelectionKeyAdapter extends     KeyAdapter
                                  implements Serializable
{
  public void keyPressed( KeyEvent e )
  {
    Point pix_pt = image_Jpanel.getCurrent_pixel_point();
    int   row    = image_Jpanel.ImageRow_of_PixelRow( pix_pt.y );
    KeySelect.ProcessKeySelection( getDataSet(), row, e );

    e.consume();
  }
}

/* --------------------------- ConsumeKeyAdapter ------------------------- */

private class ConsumeKeyAdapter extends     KeyAdapter
                                implements Serializable
{
  public void keyPressed( KeyEvent e )
  {
    e.consume();
  }
}


/* ------------------------------- finalize ------------------------------ */
  /**
   *  Trace the finalization of objects
   */
/*
  protected void finalize() throws IOException
  {
    System.out.println( "finalize ImageView" );
  }
*/


/* -------------------------- OptionMenuHandler -------------------------- */

  private class OptionMenuHandler implements ActionListener,
                                             Serializable
  {
    public void actionPerformed( ActionEvent e )
    {
      String action = e.getActionCommand();
 
      if ( action.equals( TRACK_IMAGE_CURSOR ) )
        return;

       else if ( action.equals( HORIZONTAL_SCROLL ) )
       {
         boolean state = ((JCheckBoxMenuItem)e.getSource()).getState();
         SetHorizontalScrolling( state );
       }
       else if ( action.equals( H_GRAPH_REBINNED ) )
       {
         boolean state = ((JCheckBoxMenuItem)e.getSource()).getState();
         getState().set_boolean( ViewerState.REBIN, state );
         DrawSelectedHGraphs();
       }
       else
       {
         image_Jpanel.setNamedColorModel( action, true, true );
         color_scale_image.setNamedColorModel( action, true, true );
         getState().set_String( ViewerState.COLOR_SCALE, action );
       }
    }
  }


/* ------------------------ ImageHScrollBarListener ---------------------- */

  private class ImageHScrollBarListener implements AdjustmentListener,
                                                   Serializable
  {
    public void adjustmentValueChanged( AdjustmentEvent e )
    {
        SyncHGraphScrollBar();
    }
  }


/* ------------------------ VerticalScrollListener ---------------------- */

  private class VerticalScrollListener implements AdjustmentListener,
                                                  Serializable
  {
    public void adjustmentValueChanged( AdjustmentEvent e )
    {
       setVerticalViewableRegion( e.getValue() );
    }
  }


/* ------------------------ ImageResizeListener --------------------------- */

  private class ImageResizeListener extends    ComponentAdapter
                                    implements Serializable
  {
     public void componentResized( ComponentEvent e )
     {
       ConfigureVerticalScrollBar();
       MakeSelectionImage( true );
     }
  }


/* ------------------------ XScaleListener ----------------------------- */

  private class XScaleListener implements ActionListener,
                                          Serializable
  {
     public void actionPerformed(ActionEvent e)
     {
       String action  = e.getActionCommand();
       getDataSet().notifyIObservers( action );
     }
  }


/* ------------------------ MultiPlotMenuHandler ------------------------- */

  private class MultiPlotMenuHandler implements ActionListener,
                                                Serializable
  {
    public void actionPerformed( ActionEvent e )
    {
      String action  = e.getActionCommand();
                                                // color or hidden line option
                                                // change
      if ( action.equals( MULTI_PLOT_COLOR )     ||     
           action.equals( MULTI_PLOT_HIDDEN_LINES ))
      {
        DrawSelectedHGraphs();
        return;
      }
                                                 // multiplot mode change 
      if ( !action.equals( current_multi_plot_mode ))
      {
        JRadioButtonMenuItem button = (JRadioButtonMenuItem)e.getSource();
        button.setSelected(true);
        current_multi_plot_mode = action;
        DrawSelectedHGraphs();
      }
    }
  }


}

/**
 * @(#)ImageView.java
 *
 *  Programmer: Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.12  2001/03/02 17:04:45  dennis
 *  Now only restores the zoom region if the DataSet is using the
 *  same units and labels as the previous DataSet.
 *
 *  Revision 1.11  2001/03/01 23:14:58  dennis
 *  Now saves the zoom region and restores it when the
 *  viewer is given a new DataSet.
 *
 *  Revision 1.10  2001/03/01 22:32:10  dennis
 *  Now saves the last pointed at index and will redraw that
 *  Data block ( if possible ) as the default Data block, when
 *  a new DataSet is given to the viewer.
 *
 *  Revision 1.9  2001/02/09 14:20:35  dennis
 *  Added last update time to graph title, if it is present as
 *  a Data attribute.
 *
 *  Revision 1.8  2001/01/29 21:29:30  dennis
 *  Now uses CVS version numbers.
 *
 *  Revision 1.7  2000/12/15 05:09:40  dennis
 *  Now convert histograms to functions before generating the image.
 *  This was needed because if histograms with very different
 *  x-scales are rebinned to form an image, the intensities of the
 *  image may be misleading.
 *
 *  Revision 1.6  2000/11/07 15:24:11  dennis
 *  Added docs on public methods.
 *  Draws crosshair cursor for POINTED_AT_CHANGED message.
 *  Includes ViewerState object in constructor and uses ViewerState for
 *  the color scale and horizontal scrolling state.
 *  Consumes key events, so that the cursor arrow keys don't also scroll
 *  the scrolled panes.  This caused errors in the XOR drawing of the
 *  crosshair cursor on windows.
 *
 *  Revision 1.5  2000/10/10 19:52:07  dennis
 *  "Pointed At" Data block and cursor readouts now update with mouse
 *  click as well as with mouse drag operation.
 *
 *  Revision 1.4  2000/10/03 21:19:42  dennis
 *  Now uses ImageJPanel method to set the color model.
 *
 *  Revision 1.3  2000/08/03 01:49:01  dennis
 *  Now sets the default graph as the "pointed" at Data block and notifies
 *  observers when the viewer starts.  This fixed a bug where the user had to
 *  first move the mouse pointer on the image before moving it on the graph in
 *  order to use the cursor readouts on the graph.
 *
 *  Revision 1.2  2000/08/02 01:51:10  dennis
 *  Now calls Data.ResampleUniformly() instead of Data.ReBin()
 *
 *  Revision 1.1  2000/07/10 23:03:23  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.51  2000/07/03 21:14:22  dennis
 *  modified DrawPointedAtHGraph() to return a boolean indicating whether or
 *  not a graph was drawn.
 *  Modified DrawSelectedHGraphs() to return a count of the number of graphs
 *  drawn.
 *
 *  Added DrawDefaultDataBlock that tries to draw the selected HGraphs or the
 *  pointed at HGraphs.  If none are drawn, it draws Data block 0 if there is
 *  one.
 *
 *  Revision 1.50  2000/06/12 20:05:35  dennis
 *  now implements Seriaizable
 *
 *  Revision 1.49  2000/06/01 18:21:42  dennis
 *  now includes remove hidden lines option.
 *
 *  Revision 1.48  2000/05/24 22:45:07  dennis
 *  Added plotting of multiple [colored] [offset] graphs.
 *
 *  Revision 1.47  2000/05/18 20:58:08  dennis
 *  now draws the graphs of both the most recently selected graph AND the
 *  pointed at graph.
 *
 *  Revision 1.46  2000/05/18 20:05:22  dennis
 *  removed non-working Cursor setting code.
 *  Fixed problme with horizontal scroll bar visibility.
 *
 *  Revision 1.45  2000/05/16 22:25:07  dennis
 *  Added NUM_BINS control and method getXConversionScale()
 *
 *  Revision 1.44  2000/05/11 15:23:28  dennis
 *  added RCS logging
 *
 *
 *
 */
package DataSetTools.viewer.Image;

import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;
import DataSetTools.viewer.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import DataSetTools.components.image.*;
import DataSetTools.components.ui.*;
import DataSetTools.components.containers.*;
import java.text.*;

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

  private static final String NO_MULTI_PLOT        = "Don't Graph Selected";
  private static final String MULTI_PLOT_OVERLAY   = "Overlaid";
  private static final String MULTI_PLOT_VERTICAL  = "Shift Vertically";
  private static final String MULTI_PLOT_DIAGONAL  = "Shift Diagonally";
  private static final String MULTI_PLOT_COLOR     = "Color Graphs";
  private static final String MULTI_PLOT_HIDDEN_LINES = "Remove Hidden Lines";

                                                   // Image and border
  private ImageJPanel  image_Jpanel;   
  private ImageJPanel  selection_image;
  private ImageJPanel  color_scale_image;
  private JScrollPane  image_scroll_pane;
  private String       current_multi_plot_mode = MULTI_PLOT_DIAGONAL;
  private final int    MAX_PLOTS = 16;

  private SplitPaneWithState   main_split_pane = null;
  private SplitPaneWithState   left_split_pane = null;

  private TextRangeUI  x_range_ui;
  private TextValueUI  n_bins_ui;

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
  private float           x_min,
                          x_max;
  
  private JCheckBoxMenuItem track_image_cursor_button = null;
  private JCheckBoxMenuItem use_color_button    = null;
  private JCheckBoxMenuItem remove_hidden_lines = null;

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

  init();
  MakeImage( false );
  getState().setZoomRegion( image_Jpanel.getLocalWorldCoords(), data_set );

  DrawDefaultDataBlock();
}

/* -----------------------------------------------------------------------
 *
 *  PUBLIC METHODS
 *
 */

/* ----------------------------- redraw --------------------------------- */
/**
 * Redraw all or part of the image. The amount that needs to be redrawn is
 * determined by the "reason" parameter.  In addition to the reasons provided
 * by the base class, this also responds to the reason X_RANGE_CHANGED.
 *
 * @param  reason  The reason the redraw is needed.
 *
 */
public void redraw( String reason )
{
  if ( reason == IObserver.SELECTION_CHANGED )
  {
    MakeSelectionImage( true );
    DrawSelectedHGraphs();
  }
  else if ( reason == IObserver.POINTED_AT_CHANGED )
  {
    DrawSelectedHGraphs(); 
    float n_data = getDataSet().getNum_entries();
    if ( n_data > 0 )
    {
      floatPoint2D pt = image_Jpanel.getCurrent_WC_point();
      int  index = getDataSet().getPointedAtIndex();
      pt.y = index;
      pt.y = pt.y * ( n_data - 1 )/ n_data + 0.5f;
      image_Jpanel.set_crosshair_WC( pt );

      getState().setPointedAtIndex( index );
    }
  }
  else
    MakeImage( true );
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
  setVisible(false);
  super.setDataSet( ds );
  init(); 
  redraw( NEW_DATA_SET );
                                                        // restore the old zoom 
                                                        // region if it's valid 
  CoordBounds zoom_region = getState().getZoomRegion( ds );
  if ( zoom_region != null )
    image_Jpanel.setLocalWorldCoords( zoom_region );

  DrawDefaultDataBlock();
  setVisible(true);
}

/* ----------------------------- doLayout ------------------------------- */
/**
 * This method first resets the divider location and the calls the super class
 * doLayout method.  This was an attempt at reducing the amount of "flicker"
 * as internal frames were moved on a DeskTop.
 *
 */

public void doLayout()
{

  if ( main_split_pane != null )
    main_split_pane.my_setDividerLocation( 0.7f );

/* With both calls to setDividerLocation, the viewer "flickers" as it
   is moved in an internal frame.  
  if ( left_split_pane != null )
    left_split_pane.my_setDividerLocation( 0.7f );
*/

  super.doLayout();
}


/* ------------------------ getXConversionScale -------------------------- */
 /**
  *  Return a range of X values specified by the user to be used to
  *  control X-Axis conversions.  
  *
  *  @return  The current values from the number of bins control and the 
  *           x range control form the Xscale that is returned.
  *
  */
 public UniformXScale getXConversionScale()
  {
    int num_bins = (int)n_bins_ui.getValue();
    float x_min = x_range_ui.getMin();
    float x_max = x_range_ui.getMax();

    return ( new UniformXScale( x_min, x_max, num_bins ) );
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

    spectrum = new Data( x_scale, y_values, id );   // put it into a "Data"
                                                    // object and then add
    data_set.addData_entry( spectrum );             // that data object to
                                                    // the data set
  }

  ImageView image_view = new ImageView( data_set, null );
  JFrame f = new JFrame("Test for ImageJPanel.class");
  f.setBounds(0,0,600,400);
  f.setJMenuBar( image_view.getMenuBar() );
  f.getContentPane().add(image_view);
  f.setVisible(true);
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
    removeAll();
  }
  image_Jpanel      = new ImageJPanel();
  image_Jpanel.setNamedColorModel( getState().getColor_scale(), false );
                                               // make box to contain both the
                                               // image and selection indicator
  Box image_area = new Box( BoxLayout.X_AXIS );
  JPanel spacer = new JPanel();
  spacer.setMaximumSize( new Dimension(3, 30000) );
  spacer.setPreferredSize( new Dimension(3, 200) );
  
  JPanel sel_image_container = new JPanel();
  selection_image = new ImageJPanel();
  sel_image_container.setLayout( new GridLayout(1,1) );
  sel_image_container.add( selection_image );

  sel_image_container.setMaximumSize( new Dimension(8, 30000) );
  sel_image_container.setPreferredSize( new Dimension(8, 200) );

  image_area.add( sel_image_container );
  image_area.add( spacer );
  image_area.add( image_Jpanel );

  image_scroll_pane = new JScrollPane( image_area,
                                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                                          );

  image_scroll_pane.getHorizontalScrollBar().addAdjustmentListener( 
                                             new ImageHScrollBarListener() );

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
  JMenu color_menu = new JMenu( "Color Scale..." );
  option_menu.add( color_menu );

  JMenuItem button = new JMenuItem( IndexColorMaker.HEATED_OBJECT_SCALE );
  button.addActionListener( option_menu_handler );
  color_menu.add( button );

  button = new JMenuItem( IndexColorMaker.HEATED_OBJECT_SCALE_2 );
  button.addActionListener( option_menu_handler );
  color_menu.add( button );

  button = new JMenuItem( IndexColorMaker.GRAY_SCALE );
  button.addActionListener( option_menu_handler );
  color_menu.add( button );

  button = new JMenuItem( IndexColorMaker.NEGATIVE_GRAY_SCALE );
  button.addActionListener( option_menu_handler );
  color_menu.add( button );

  button = new JMenuItem( IndexColorMaker.GREEN_YELLOW_SCALE );
  button.addActionListener( option_menu_handler );
  color_menu.add( button );

  button = new JMenuItem( IndexColorMaker.RAINBOW_SCALE );
  button.addActionListener( option_menu_handler );
  color_menu.add( button );

  button = new JMenuItem( IndexColorMaker.OPTIMAL_SCALE );
  button.addActionListener( option_menu_handler );
  color_menu.add( button );

  button = new JMenuItem( IndexColorMaker.MULTI_SCALE );
  button.addActionListener( option_menu_handler );
  color_menu.add( button );

  button = new JMenuItem( IndexColorMaker.SPECTRUM_SCALE );
  button.addActionListener( option_menu_handler );
  color_menu.add( button );
                                               // multiplot graph options
  JMenu multiplot_menu = new JMenu( "Graph Selected (" + MAX_PLOTS + " Max)" );

  MultiPlotMenuHandler multi_plot_menu_handler = new MultiPlotMenuHandler();
  option_menu.add( multiplot_menu );

  ButtonGroup group = new ButtonGroup();
  JRadioButtonMenuItem r_button = new JRadioButtonMenuItem( NO_MULTI_PLOT );
  if ( current_multi_plot_mode == NO_MULTI_PLOT )
    r_button.setSelected(true);
  r_button.addActionListener( multi_plot_menu_handler );
  multiplot_menu.add( r_button );
  group.add( r_button );

  r_button = new JRadioButtonMenuItem( MULTI_PLOT_DIAGONAL );
  if ( current_multi_plot_mode == MULTI_PLOT_DIAGONAL )
    r_button.setSelected(true);
  r_button.addActionListener( multi_plot_menu_handler );
  multiplot_menu.add( r_button );
  group.add( r_button );

  r_button = new JRadioButtonMenuItem( MULTI_PLOT_VERTICAL );
  if ( current_multi_plot_mode == MULTI_PLOT_VERTICAL )
    r_button.setSelected(true);
  r_button.addActionListener( multi_plot_menu_handler );
  multiplot_menu.add( r_button );
  group.add( r_button );

  r_button = new JRadioButtonMenuItem( MULTI_PLOT_OVERLAY );
  if ( current_multi_plot_mode == MULTI_PLOT_OVERLAY )
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
  cb_button.setState( getState().getHorizontal_scrolling() );
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
  float         image_data[][];
  Data          data_block;
  Data          rebinned_data_block;
  int           num_rows = getDataSet().getNum_entries();
  if ( num_rows == 0 )
    return;

//  int num_cols = getDataSet().getMaxXSteps();
  int num_cols = (int)n_bins_ui.getValue();

  float x_min = x_range_ui.getMin();
  float x_max = x_range_ui.getMax();
/*
  UniformXScale x_scale = getDataSet().getXRange();
  if ( x_scale.getEnd_x() == x_scale.getStart_x() ) 
    num_cols = 1;
  else
    num_cols = (int)( num_cols * ( x_max - x_min ) /
                                 ( x_scale.getEnd_x() - x_scale.getStart_x()));
*/
//  System.out.println("XRange is        " + x_scale );
  
//  System.out.println("Making image with" + num_rows + " rows" );
//  System.out.println("num_cols         " + num_cols );
//  System.out.println("on interval [" + x_min + ", " + x_max + "]" );

  if ( num_cols < 2 ||
       num_rows < 1   )    // #### degenerate size of JPanel, so just return
    return;

  UniformXScale x_scale = new UniformXScale( x_min, x_max, num_cols );

  image_data = new float[ num_rows ][];

  for ( int i = 0; i < num_rows; i++ )
  {
    data_block = getDataSet().getData_entry(i);
    rebinned_data_block = (Data)data_block.clone();

    if ( !rebinned_data_block.isFunction() )           // need to treat it as
      rebinned_data_block.ConvertToFunction( false );  // intensity for image
                                                       // display when starting
                                                       // with widely different
                                                       // sizes of x-bins
    rebinned_data_block.ResampleUniformly( x_scale );  

    image_data[i] = rebinned_data_block.getY_values();
  }
                              // set the log scale and image data, but don't
                              // remake the image yet. It's done when the
                              // component is initially resized.     
  image_Jpanel.changeLogScale( log_scale_slider.getValue(), false );
  image_Jpanel.setData( image_data, redraw_flag );

  CoordBounds bounds = new CoordBounds( x_min, 0, x_max, num_rows-1 );
  image_Jpanel.setGlobalWorldCoords( bounds );

  MakeSelectionImage( redraw_flag );

  if ( getState().getHorizontal_scrolling() )
    SetHorizontalScrolling( true );                   // this was needed to
                                                      // switch DataSets after
                                                      // HScroll was enabled   
}


/* --------------------------- MakeSelectionImage -------------------------- */

private void MakeSelectionImage( boolean redraw_flag )
{
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
    if ( data_block.isSelected() )
      sel_image_data[i-first_row][1] = 1;
    else
      sel_image_data[i-first_row][1] = 0;

    group_id = data_block.getGroup_ID();
    if ( max_group_id >= 0 )                              // group -1 maps to 0
      sel_image_data[i-first_row][0] = (group_id+1)/(max_group_id+1);
    else                                           
      sel_image_data[i-first_row][0] = 0; 
  }

  selection_image.setData( sel_image_data, redraw_flag );
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
  x_min = x_scale.getStart_x();
  x_max = x_scale.getEnd_x();
  x_range_ui = new TextRangeUI(label, x_min, x_max );
  x_range_ui.setPreferredSize( new Dimension(120, 50) );
  control_area.add( x_range_ui );

  int num_cols = getDataSet().getMaxXSteps();
  n_bins_ui = new TextValueUI( "Num Bins", num_cols );
  control_area.add( n_bins_ui ); 
                                                  // make a color scale bar
  float color_scale_data[][] = new float[1][255];
  for ( int i = -127; i <= 127; i++ )
    color_scale_data[0][i+127] = i;
  color_scale_image = new ImageJPanel();
  color_scale_image.setData( color_scale_data, false );
  color_scale_image.setNamedColorModel( getState().getColor_scale(), false );
  control_area.add( color_scale_image );

  log_scale_slider.setPreferredSize( new Dimension(120,50) );
  log_scale_slider.setValue(50);
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

  return hgraph_scroll_pane;
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
  image_scroll_pane.setBorder( border );

  return image_scroll_pane;
}


/*--------------------------- MakeConnections ---------------------------- */
/*
 *  Add listeners for events that need to be handled.
 */
private void MakeConnections()
{
   hgraph_scale_slider.addChangeListener( new HGraphScaleEventHandler() );   

   log_scale_slider.addChangeListener( new LogScaleEventHandler() );   
   log_scale_slider.addMouseListener( new LogScaleMouseHandler() );

   image_Jpanel.addMouseMotionListener( new ImageMouseMotionAdapter() );
   image_Jpanel.addMouseListener( new ImageMouseAdapter() );
   image_Jpanel.addMouseListener( new ImageZoomMouseHandler() );
   h_graph.addMouseMotionListener( new HGraphMouseMotionAdapter() );
   h_graph.addMouseListener( new HGraphMouseAdapter() );

   x_range_ui.addActionListener( new X_Range_Listener() );
   n_bins_ui.addActionListener( new NumBins_Listener() );
  

   SelectionKeyAdapter key_adapter = new SelectionKeyAdapter();
   image_Jpanel.addKeyListener( key_adapter );
   h_graph.addKeyListener( new ConsumeKeyAdapter() );
}

/* ------------------------ SetHorizontalScrolling ------------------------ */
private void SetHorizontalScrolling( boolean state )
{
  getState().setHorizontal_scrolling( state );
  left_split_pane.setVisible( false );
  image_Jpanel.SetHorizontalScrolling( state );
  if ( state )                                 // position the image scroll bar
  {
    JScrollBar scroll_bar = image_scroll_pane.getHorizontalScrollBar();
    int min = scroll_bar.getMinimum();
    int max = scroll_bar.getMaximum();
    int position = (int) 
               ((max-min) * getState().getHorizontal_scroll_fraction() + min ); 
    scroll_bar.setValue( position );
  }
  image_scroll_pane.doLayout();

                                         // make the graph have same preferred
                                         // width as the image, so horizontal
                                         // scroll bars line up better.
  Dimension preferred_size = image_Jpanel.getPreferredSize();
  preferred_size.height = 0;
  h_graph.my_setPreferredSize( preferred_size );
  h_graph.SetHorizontalScrolling( state );

  if ( state )                            // position the graph scroll bar
    SyncHGraphScrollBar();

  hgraph_scroll_pane.doLayout();

  left_split_pane.invalidate();
  left_split_pane.doLayout();
  left_split_pane.setVisible( true );    // it was necessary to set the 
                                         // visibilty off then on again to
                                         // have the scroll bars remain on
                                         // (or off) as the DataSet and 
                                         // number of bins are changed.
}



/* -------------------------- UpdateImageReadout ------------------------- */

private void UpdateImageReadout( int row )
{
  floatPoint2D float_pt = image_Jpanel.getCurrent_WC_point();
  image_table.showConversions( float_pt.x, row );
}

/* -------------------------- SetGraphCursorFromImage ---------------------- */

private void SetGraphCursorFromImage( )
{
  Point pix_pt          = image_Jpanel.getCurrent_pixel_point();
  int col = image_Jpanel.ImageCol_of_PixelCol( pix_pt.x );
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
  Data pointed_at_data_block = null;
  int pointed_at_row = getDataSet().getPointedAtIndex();
  if ( pointed_at_row >= 0 )
  {
    pointed_at_data_block = getDataSet().getData_entry( pointed_at_row );

    if ( remove_hidden_lines.getState() )
      h_graph.setRemoveHiddenLines( true );
    else 
      h_graph.setRemoveHiddenLines( false );

    DrawHGraph( pointed_at_data_block, 0, true );

    return true;
  }

  return false;
}


/* ------------------------ DrawSelectedHGraphs --------------------------- */

private int DrawSelectedHGraphs()
{
  int num_drawn = 0;

  h_graph.clearData();

  if ( current_multi_plot_mode != NO_MULTI_PLOT )
  {
    if ( current_multi_plot_mode == MULTI_PLOT_OVERLAY )
      h_graph.setMultiplotOffsets( 0, 0 );
    else if ( current_multi_plot_mode == MULTI_PLOT_VERTICAL )
      h_graph.setMultiplotOffsets( 0, 10 );
    else if ( current_multi_plot_mode == MULTI_PLOT_DIAGONAL )
      h_graph.setMultiplotOffsets( 10, 10 );

    Data pointed_at_data_block = null;
    int pointed_at_row = getDataSet().getPointedAtIndex();
    if ( pointed_at_row >= 0 )
      pointed_at_data_block = getDataSet().getData_entry( pointed_at_row );

    Data data_block = null;
    int draw_count = 0;
    int n_rows = getDataSet().getNum_entries();
    boolean too_many = false;
    int i = n_rows-1;
    while ( !too_many && i >= 0 )       // for (int i = n_rows-1; i >= 0; i--)
    {
      if ( getDataSet().isSelected(i) )
      {
        draw_count++;
        data_block = getDataSet().getData_entry( i );
        if ( data_block == pointed_at_data_block )
          DrawHGraph( data_block, draw_count, true );
        else
          DrawHGraph( data_block, draw_count, false );

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

    int last_pointed_at = getState().getPointedAtIndex();
    Data data_block     = getDataSet().getData_entry( last_pointed_at );
    if ( data_block != null )
    {
      DrawHGraph( data_block, 0, true );
      getDataSet().setPointedAtIndex( last_pointed_at );
      getDataSet().notifyIObservers( IObserver.POINTED_AT_CHANGED );
    }
    else                                 // if none, try to draw data block 0
    {
      data_block = getDataSet().getData_entry(0);
      if ( data_block != null )
      {
        DrawHGraph( data_block, 0, true );
        getDataSet().setPointedAtIndex(0);
        getDataSet().notifyIObservers( IObserver.POINTED_AT_CHANGED );
      }

      else
        System.out.println("ERROR... no Data blocks in DataSet" );
    }
  }
}


/* ---------------------------- DrawHGraph ----------------------------- */

private void DrawHGraph( Data data_block, int graph_num, boolean pointed_at )
{
  float x[] = data_block.getX_scale().getXs();
  float y[] = data_block.getY_values();
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

  if ( graph_num == 0 )
  {
    String border_label = data_block.toString();
    if ( data_block.isSelected() )
      border_label += " (Selected)";

    String update_time = (String)
                         (data_block.getAttributeValue(Attribute.UPDATE_TIME));
    if ( update_time != null )
      border_label = border_label + ", " + update_time;

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
  h_graph.setData( x, y, graph_num, false );
  CoordBounds graph_bounds = h_graph.getGlobalWorldCoords();
  CoordBounds image_bounds = image_Jpanel.getLocalWorldCoords();
  graph_bounds.setBounds( image_bounds.getX1(), graph_bounds.getY1(),
                          image_bounds.getX2(), graph_bounds.getY2() );

  h_graph.setGlobalWorldCoords( graph_bounds );
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

  getState().setHorizontal_scroll_fraction( fraction );
}
 

/* ------------------------- ProcessImageMouseEvent ------------------------ */

private Point ProcessImageMouseEvent( MouseEvent e, 
                                      int        last_image_row,
                                      int        last_image_col  )
{
  Point pix_pt          = image_Jpanel.getCurrent_pixel_point();
  int row = image_Jpanel.ImageRow_of_PixelRow( pix_pt.y );
  int col = image_Jpanel.ImageCol_of_PixelCol( pix_pt.x );

  if ( row != last_image_row )
  {
    if ( getDataSet().getPointedAtIndex() != row )  // only change if needed
    { 
      getDataSet().setPointedAtIndex( row );
      getState().setPointedAtIndex( row );
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
                              // #### NOTE: This should work... in fact, it 
                              //            used to work.  With swing1.1.1beta2
                              //            it does not work.  The kludge,
                              //            "LogScaleMouseHandler" was added
                              //            as a workaround.  When the slider
                              //            is fixed, so that the method to
                              //            getValueIsAdjusting() again works,
                              //            The LogScaleMouseHandler should be
                              //            removed.
    if ( !slider.getValueIsAdjusting() )
      image_Jpanel.changeLogScale( slider.getValue(), true );
  }
} 



/* ---------------------------- LogScaleMouseHandler ----------------------- */
private class LogScaleMouseHandler extends    MouseAdapter
                                   implements Serializable
{
  public void mouseReleased(MouseEvent e)
  {
    JSlider slider = (JSlider)e.getSource();
    image_Jpanel.changeLogScale( slider.getValue(), true );
  }
}

/* ------------------------- HGraphScaleEventHandler ----------------------- */

private class HGraphScaleEventHandler implements ChangeListener,
                                                 Serializable
{
  public void stateChanged(ChangeEvent e)
  {
    JSlider slider = (JSlider)e.getSource();

    if ( slider.getValue() == 0 )
    {
      h_graph.autoY_bounds();
      TitledBorder border = new TitledBorder(LineBorder.createBlackLineBorder(),
                         "Auto-Scale" );
      border.setTitleFont( FontUtil.BORDER_FONT );
      slider.setBorder( border );
    }
    else
    {
      float y_min = y_range.getStart_x();
      float y_max = y_range.getEnd_x();
      float range = ( y_max - y_min ) * slider.getValue() / 1000.0f;
      h_graph.setY_bounds( y_min, y_min + range );
      TitledBorder border = new TitledBorder(LineBorder.createBlackLineBorder(),
                         ""+slider.getValue()/10.0f+"%(max)" );
      border.setTitleFont( FontUtil.BORDER_FONT );
      slider.setBorder( border );
    }
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
      MakeSelectionImage( true );
      getState().setZoomRegion( image_Jpanel.getGlobalWorldCoords(),
                                getDataSet()  );
    }
  }

  public void mouseReleased(MouseEvent e)         // zoom in to sub region 
  {
    MakeSelectionImage( true );
    getState().setZoomRegion( image_Jpanel.getLocalWorldCoords(),
                              getDataSet()  );
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


  /**
   *  Trace the finalization of objects
   */
/*
  protected void finalize() throws IOException
  {
    System.out.println( "finalize ImageView" );
  }
*/

  private class OptionMenuHandler implements ActionListener,
                                             Serializable
  {
    public void actionPerformed( ActionEvent e )
    {
      String action = e.getActionCommand();
 
      if ( action == TRACK_IMAGE_CURSOR )
        return;

       else if ( action == HORIZONTAL_SCROLL )
       {
         boolean state = ((JCheckBoxMenuItem)e.getSource()).getState();
         SetHorizontalScrolling( state );
       }
       else
       {
         image_Jpanel.setNamedColorModel( action, true );
         color_scale_image.setNamedColorModel( action, true );
         getState().setColor_scale( action );
       }
    }
  }


  private class ImageHScrollBarListener implements AdjustmentListener,
                                                   Serializable
  {
    public void adjustmentValueChanged( AdjustmentEvent e )
    {
        SyncHGraphScrollBar();
    }
  }

  private class X_Range_Listener implements ActionListener,
                                            Serializable
  {
     public void actionPerformed(ActionEvent e)
     {
       getDataSet().notifyIObservers( X_RANGE_CHANGED );
     }
  }


  private class NumBins_Listener implements ActionListener,
                                            Serializable
  {
     public void actionPerformed(ActionEvent e)
     {
       getDataSet().notifyIObservers( BINS_CHANGED );
     }
  }


  private class MultiPlotMenuHandler implements ActionListener,
                                                Serializable
  {
    public void actionPerformed( ActionEvent e )
    {
      String action  = e.getActionCommand();

                                                // color or hidden line option
                                                // change
      if ( action == MULTI_PLOT_COLOR     ||     
           action == MULTI_PLOT_HIDDEN_LINES )
      {
        DrawSelectedHGraphs();
        return;
      }
                                                 // multiplot mode change 
      if ( action != current_multi_plot_mode )
      {
        JRadioButtonMenuItem button = (JRadioButtonMenuItem)e.getSource();
        button.setSelected(true);
        current_multi_plot_mode = action;
        DrawSelectedHGraphs();
      }
    }
  }

}

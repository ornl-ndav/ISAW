/**
 * @(#)GraphView.java  
 *
 * Programmer: Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.5  2000/12/07 22:44:00  dennis
 *  Now shows raw data rather than resampling the data uniformly
 *  before drawing the graphs.
 *
 *  Revision 1.4  2000/11/07 15:26:36  dennis
 *  Includes ViewerState object in constructor and uses ViewerState for
 *  horizontal scrolling state.
 *  Consumes key events, so that the cursor arrow keys don't also scroll
 *  the scrolled panes.  This caused errors in the XOR drawing of the
 *  crosshair cursor on windows.
 *
 *  Revision 1.3  2000/10/10 19:54:10  dennis
 *  redraw() due to selection changes now only draws graphs for which the
 *  selection status changed.  This is much more efficient.
 *  Cursor readout and "Pointed At" data block now changes with mouse
 *  click as well as with mouse drag event.
 *
 *  Revision 1.2  2000/08/02 01:50:42  dennis
 *  Now calls Data.ResampleUniformly() instead of Data.ReBin()
 *
 *  Revision 1.1  2000/07/10 23:02:50  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.20  2000/06/12 20:10:03  dennis
 *  now implements Serializable
 *
 *  Revision 1.19  2000/05/18 20:06:20  dennis
 *  added getXConversionScale() method.
 *  Added n_bins_ui control to control the number of bins.
 *
 *  Revision 1.18  2000/05/11 15:23:11  dennis
 *  added RCS logging
 *
 */
package DataSetTools.viewer.Graph;

import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;
import DataSetTools.viewer.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import DataSetTools.components.image.*;
import DataSetTools.components.containers.*;
import DataSetTools.components.ui.*;
import java.text.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * A graph view of a Data Set.
 *
 * @see DataSetTools.dataset.DataSet
 *
 */

public class GraphView extends    DataSetViewer
                       implements Serializable
{ 
  private static final String HORIZONTAL_SCROLL = "Horizontal Scroll";

                                                    // Image and border
  SplitPaneWithState   main_split_pane = null;

  private JPanel       display_controls  = new JPanel();
  private TextRangeUI  x_range_ui;
  private TextValueUI  n_bins_ui;

  private JPanel       graph_data_panel  = new JPanel();
  private DataSetXConversionsTable graph_table;

  private GraphJPanel  h_graph[]        = null;
  private boolean      was_selected[]   = null;
                                              // panel for the horizontal graph
  JPanel viewport = new JPanel();
  int    viewport_preferred_width = 300;
  private JScrollPane  hgraph_scroll_pane = new JScrollPane( viewport, 
                                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                                          );

  private JSlider      hgraph_scale_slider = new JSlider( JSlider.HORIZONTAL,
                                                          0, 1000, 0 );
  ClosedInterval       y_range;

/* --------------------------------------------------------------------------
 *
 * CONSTRUCTORS
 *
 */

/* ------------------------------------------------------------------------ */
public GraphView( DataSet data_set, ViewerState state ) 
{
  super( data_set, state );       
  AddOptionsToMenu();

  init();
  DrawGraphs();
}

/* -----------------------------------------------------------------------
 *
 *  PUBLIC METHODS
 *
 */

public void redraw( String reason )
{
  if ( reason == IObserver.SELECTION_CHANGED )
    DrawSelectedGraphs();

  else if ( reason == IObserver.POINTED_AT_CHANGED )
    DrawPointedAtGraph();

  else
    DrawGraphs();
}


public void setDataSet( DataSet ds )
{
  setVisible(false);
  super.setDataSet( ds );
  init();
  redraw( NEW_DATA_SET );
  setVisible(true);
}


public void doLayout()
{
  if ( main_split_pane != null )
    main_split_pane.my_setDividerLocation( 0.7f );

  super.doLayout();
}

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

  GraphView graph_view = new GraphView( data_set, null );
  JFrame f = new JFrame("Test for GraphView class");
  f.setBounds(0,0,600,400);
  f.setJMenuBar( graph_view.getMenuBar() );
  f.getContentPane().add(graph_view);
  f.setVisible(true);
}
   
/* -----------------------------------------------------------------------
 *
 * PRIVATE METHODS
 *
 */ 

/* -------------------------------- init -------------------------------- */
private void init()
{
  if ( main_split_pane != null )     // get rid of the old components first
  {
    graph_data_panel.removeAll();
    hgraph_scroll_pane.removeAll();
    main_split_pane.removeAll();
    removeAll();
  }

  hgraph_scroll_pane = new JScrollPane( viewport,
                                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                                          );
  hgraph_scroll_pane.getHorizontalScrollBar().addAdjustmentListener(
                                            new HScrollBarListener() );

  main_split_pane = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
                                            MakeHorGraphArea(),
                                            MakeControlArea(),
                                            0.7f );

  String title = getDataSet().toString();
  AttributeList attr_list = getDataSet().getAttributeList();
  Attribute     attr      = attr_list.getAttribute(Attribute.RUN_TITLE); 
  if ( attr != null )
   title = attr.getStringValue();

  TitledBorder border = new TitledBorder(
                                    LineBorder.createBlackLineBorder(),
                                    title );
  border.setTitleFont( FontUtil.BORDER_FONT );
  main_split_pane.setBorder( border );

  this.setLayout( new GridLayout(1,1) );
  this.add( main_split_pane );
  MakeConnections();                       // Add event handlers

  y_range = getDataSet().getYRange();
}

/* ------------------------- AddOptionsToMenu --------------------------- */

private void AddOptionsToMenu()
{
  OptionMenuHandler option_menu_handler = new OptionMenuHandler();
  JMenu option_menu = menu_bar.getMenu( OPTION_MENU_ID );
                                                    // Horizontal scroll option
  JCheckBoxMenuItem cb_button = new JCheckBoxMenuItem( HORIZONTAL_SCROLL );
  cb_button.setState( getState().getHorizontal_scrolling() );
  cb_button.addActionListener( option_menu_handler );
  option_menu.add( cb_button );
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
                                                     // Add the x-range control
  String label = getDataSet().getX_units();
  UniformXScale x_scale  = getDataSet().getXRange();
  float x_min = x_scale.getStart_x();
  float x_max = x_scale.getEnd_x();
  x_range_ui = new TextRangeUI(label, x_min, x_max );
  x_range_ui.setPreferredSize( new Dimension(120, 50) );
  x_range_ui.setFont( FontUtil.LABEL_FONT );
  control_area.add( x_range_ui );

  int num_cols = getDataSet().getMaxXSteps();
  n_bins_ui = new TextValueUI( "Num Bins", num_cols );
  control_area.add( n_bins_ui );
                                                     // Add the y-range slider
  hgraph_scale_slider.setPreferredSize( new Dimension(120,50) );
  TitledBorder border = new TitledBorder(LineBorder.createBlackLineBorder(), 
                                         "Auto-Scale" );
  border.setTitleFont( FontUtil.BORDER_FONT );
  hgraph_scale_slider.setBorder( border );

  control_area.add( hgraph_scale_slider );
  control_area.add( display_controls );      

  graph_table = new DataSetXConversionsTable( getDataSet() );
  border = new TitledBorder(LineBorder.createBlackLineBorder(), "Graph Data" );
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

/* ------------------------ MakeHorizontalGraphArea ----------------------- */
/*
 * Construct panel for the horizontal graph.
 */

private JScrollPane MakeHorGraphArea( )
{
  String title;

  OperationLog op_log = getDataSet().getOp_log();
  if ( op_log.numEntries() <= 0 )
    title = "GRAPH DISPLAY AREA";
  else
    title = op_log.getEntryAt( op_log.numEntries() - 1 );

  TitledBorder border = new TitledBorder( LineBorder.createBlackLineBorder(),
                                          title );
  border.setTitleFont( FontUtil.BORDER_FONT ); 
  hgraph_scroll_pane.setBorder( border ); 

  JScrollBar scroll_bar = hgraph_scroll_pane.getVerticalScrollBar();
  scroll_bar.setUnitIncrement( 25 );
  return hgraph_scroll_pane;
}

/*--------------------------- MakeConnections ---------------------------- */
/*
 *  Add listeners for events that need to be handled.
 */
private void MakeConnections()
{
   hgraph_scale_slider.addChangeListener( new HGraphScaleEventHandler() );   
   x_range_ui.addActionListener( new X_Range_Listener() );
   n_bins_ui.addActionListener( new NumBins_Listener() );
}


/* --------------------- SetHorizontalScrolling ------------------------ */
private void SetHorizontalScrolling( boolean state )
{
  getState().setHorizontal_scrolling( state );
  if ( state )
  {
    viewport_preferred_width = (int)n_bins_ui.getValue();
    JScrollBar scroll_bar = hgraph_scroll_pane.getHorizontalScrollBar();
    int min = scroll_bar.getMinimum();
    int max = scroll_bar.getMaximum();
    int position = (int)
               ((max-min) * getState().getHorizontal_scroll_fraction() + min );
    scroll_bar.setValue( position );
  }
  else
    viewport_preferred_width = 300;

  int  num_data_blocks = getDataSet().getNum_entries();
  viewport.setPreferredSize( new Dimension( viewport_preferred_width, 
                                            100*num_data_blocks ) );

  hgraph_scroll_pane.doLayout();
}



/* ---------------------------- DrawGraphs ----------------------------- */

private void DrawGraphs( )
{
  if ( h_graph != null )   // get rid of old graphs
  {  
    for ( int i = 0; i < h_graph.length; i++ )
      h_graph[i] = null;
    h_graph = null;
    viewport.removeAll();
    viewport.setVisible(false);
  }

  int  num_data_blocks = getDataSet().getNum_entries();

  h_graph      = new GraphJPanel[ num_data_blocks ];
  was_selected = new boolean[ num_data_blocks ];

  viewport.setLayout( new GridLayout(num_data_blocks, 1) );
  viewport.setPreferredSize( new Dimension(viewport_preferred_width, 
                                           100*num_data_blocks ) );

  SelectionKeyAdapter key_adapter = new SelectionKeyAdapter();

  float x_min = x_range_ui.getMin();
  float x_max = x_range_ui.getMax();
  int num_cols = (int)n_bins_ui.getValue();
  UniformXScale x_scale = new UniformXScale( x_min, x_max, num_cols );

  for ( int i = 0; i < num_data_blocks; i++ )
  {
    Data data_block = getDataSet().getData_entry( i );;       // don't rebin, or
//    Data temp_data_block = getDataSet().getData_entry( i ); // rebin the Data 
//    Data data_block = (Data)temp_data_block.clone();
//    data_block.ResampleUniformly( x_scale );

    float x[] = data_block.getX_scale().getXs();
    float y[] = data_block.getY_values();

    h_graph[i] = new GraphJPanel();
    if ( data_block.isSelected() )
      h_graph[i].setColor( Color.blue, 0, false );
    else 
      h_graph[i].setColor( Color.black, 0, false );

    h_graph[i].setData( x, y, 0, false );

    h_graph[i].addKeyListener( key_adapter ); 

    CoordBounds graph_bounds = h_graph[i].getGlobalWorldCoords();
    graph_bounds.setBounds( x_min, graph_bounds.getY1(),
                            x_max, graph_bounds.getY2() );
    h_graph[i].setGlobalWorldCoords( graph_bounds );

    h_graph[i].addMouseMotionListener( new HGraphMouseMotionAdapter() );
    h_graph[i].addMouseListener( new HGraphMouseAdapter() );

    String border_label = data_block.toString();
    if ( data_block.isSelected() )
      border_label += " (Selected)";

    JPanel border_panel = new JPanel();
    TitledBorder border = new TitledBorder( LineBorder.createBlackLineBorder(),
                      border_label );
    border.setTitleFont( FontUtil.BORDER_FONT );
    if ( data_block.isSelected() )
    {
      border.setTitleColor( Color.blue );
      was_selected[i] = true;
    }
    else
    { 
      border.setTitleColor( Color.black );
      was_selected[i] = false;
    }

    border_panel.setBorder( border );

    border_panel.setLayout( new GridLayout( 1, 1) );
    border_panel.add( h_graph[i] );
    viewport.add( border_panel );
  }

  if ( getState().getHorizontal_scrolling() )
    SetHorizontalScrolling( true );

  viewport.setVisible(true);
//  viewport.repaint();           // is this needed?
}

/* -------------------------- DrawSelectedGraphs ------------------------- */

private void DrawSelectedGraphs()
{
  if ( h_graph == null )              // nothing to do yet
    return;

  int  num_data_blocks = getDataSet().getNum_entries();

                                      // redraw the graphs with a different
                                      // selection status
  for ( int i = 0; i < num_data_blocks; i++ )
    if ( getDataSet().isSelected(i) && !was_selected[i]  || 
        !getDataSet().isSelected(i) &&  was_selected[i]   )
      DrawSpecifiedGraph( i );

                                      // update the selection status
  for ( int i = 0; i < num_data_blocks; i++ )
    was_selected[i] = getDataSet().isSelected(i);
}



/* -------------------------- DrawPointedAtGraph ------------------------- */

private void DrawPointedAtGraph()
{
                                      // actually, for now, since we are NOT
                                      // tracking the cursor or anything like
                                      // that, we don't need to do anything.
/*
  if ( h_graph == null )              // nothing to do yet 
    return;

  int pointed_at_row = getDataSet().getPointedAtIndex();
  DrawSpecifiedGraph( pointed_at_row );
*/
}

/* -------------------------- DrawSpecifiedGraph ------------------------- */

private void DrawSpecifiedGraph( int index )
{
  if ( h_graph == null )              // nothing to do yet
    return;

  int  num_data_blocks = getDataSet().getNum_entries();

  if ( index < 0 || index >= num_data_blocks )
    return;                           // index is invalid, so nothing to do

  float x_min = x_range_ui.getMin();
  float x_max = x_range_ui.getMax();
  int num_cols = (int)n_bins_ui.getValue();
  UniformXScale x_scale = new UniformXScale( x_min, x_max, num_cols );
 
  Data data_block = getDataSet().getData_entry( index );   // don't rebin or
                                                           // rebin the Data
//  Data temp_data_block = getDataSet().getData_entry( index );
//  Data data_block = (Data)temp_data_block.clone();
//  data_block.ResampleUniformly( x_scale );

  JPanel border_panel = (JPanel)h_graph[ index ].getParent();
  TitledBorder border = (TitledBorder)border_panel.getBorder();
  String border_label = data_block.toString();

  if ( data_block.isSelected() )
  {
    h_graph[index].setColor( Color.blue, 0, false );
    border.setTitleColor( Color.blue );
    border_label += " (Selected)";
    was_selected[index] = true;
  }
  else
  {
    h_graph[index].setColor( Color.black, 0, false );
    border.setTitleColor( Color.black );
    was_selected[index] = false;
  }

  float x[] = data_block.getX_scale().getXs();
  float y[] = data_block.getY_values();
  h_graph[index].setData( x, y, 0, true );

  border.setTitle( border_label );
  border_panel.repaint();
}



/* ---------------------------- getBlockNumber --------------------------- */

private int getBlockNumber( GraphJPanel gp )
{
  int  num_data_blocks = getDataSet().getNum_entries();
  for ( int i = 0; i < num_data_blocks; i++ )
    if ( gp == h_graph[i] )
      return i;

  System.out.println("ERROR: GraphJPanel not found in getBlockNumber");
  return -1; 
}


/* -------------------------- UpdateHGraphReadout ------------------------- */

private void UpdateHGraphReadout( GraphJPanel gp )
{
  floatPoint2D float_pt = gp.getCurrent_WC_point();
  int row = getDataSet().getPointedAtIndex();
  graph_table.showConversions( float_pt.x, float_pt.y, row );
}



/* -------------------------------------------------------------------------
 *
 *  INTERNAL CLASSES
 *
 */

/* ------------------------- HGraphScaleEventHandler ----------------------- */

private class HGraphScaleEventHandler implements ChangeListener,
                                                 Serializable
{
  public void stateChanged(ChangeEvent e)
  {
    JSlider slider = (JSlider)e.getSource();
                                                          // update all graph
    if ( slider.getValue() == 0 )                         // ranges
    {
      for ( int i = 0; i < getDataSet().getNum_entries(); i++ )  
        h_graph[i].autoY_bounds();

      TitledBorder border = new TitledBorder(
                                  LineBorder.createBlackLineBorder(),
                                  "Auto-Scale" );
      border.setTitleFont( FontUtil.BORDER_FONT );
      slider.setBorder( border );
    }
    else
    {
      float y_min = y_range.getStart_x();
      float y_max = y_range.getEnd_x();
      float range = ( y_max - y_min ) * slider.getValue() / 1000.0f;
      for ( int i = 0; i < getDataSet().getNum_entries(); i++ )  
        h_graph[i].setY_bounds( y_min, y_min + range );
      
      TitledBorder border = new TitledBorder(
                              LineBorder.createBlackLineBorder(),
                              ""+slider.getValue()/10.0f+"%(max)" ); 
      border.setTitleFont( FontUtil.BORDER_FONT );
      slider.setBorder( border );
    } 
  }
}

private class HGraphMouseMotionAdapter extends    MouseMotionAdapter
                                       implements Serializable
{
   public void mouseDragged( MouseEvent e )
   {
     int last_data_block = -1;
     GraphJPanel gp = (GraphJPanel)e.getComponent();     

     int row = getBlockNumber( gp );
     if ( row != last_data_block )
     {
       getDataSet().setPointedAtIndex( row );
       last_data_block = row;
       getDataSet().notifyIObservers( IObserver.POINTED_AT_CHANGED );
     }

     UpdateHGraphReadout( gp );
   }
}

private class HGraphMouseAdapter extends    MouseAdapter
                                 implements Serializable
{
   public void mousePressed( MouseEvent e )
   {
     int last_data_block = -1;
     GraphJPanel gp = (GraphJPanel)e.getComponent();

     int row = getBlockNumber( gp );
     if ( row != last_data_block )
     {
       getDataSet().setPointedAtIndex( row );
       last_data_block = row;
       getDataSet().notifyIObservers( IObserver.POINTED_AT_CHANGED );
     }

     UpdateHGraphReadout( gp );
   }
}


  /**
   *  Trace the finalization of objects
   */
/*
  protected void finalize() throws IOException
  {
    System.out.println( "finalize GraphView" );
  }
*/

  private class OptionMenuHandler implements ActionListener,
                                             Serializable
  {
    public void actionPerformed( ActionEvent e )
    {
      String action = e.getActionCommand();

       if ( action == HORIZONTAL_SCROLL )
       {
         boolean state = ((JCheckBoxMenuItem)e.getSource()).getState();
         SetHorizontalScrolling( state );
       }
    }
  }


private class SelectionKeyAdapter extends    KeyAdapter
                                  implements Serializable
{
  public void keyPressed( KeyEvent e )
  {
    GraphJPanel gp = (GraphJPanel)e.getComponent();
    int row = getBlockNumber( gp );

    KeySelect.ProcessKeySelection( getDataSet(), row, e );

    e.consume();
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

  private class HScrollBarListener implements AdjustmentListener,
                                              Serializable
  {
    public void adjustmentValueChanged( AdjustmentEvent e )
    {
      // record the new scroll bar postion
  
      JScrollBar bar = hgraph_scroll_pane.getHorizontalScrollBar();
      float bar_min   = bar.getMinimum();
      float bar_max   = bar.getMaximum();
      float bar_value = bar.getValue();
      float fraction  = 0;
      if ( bar_max == bar_min )
        fraction = 0;
      else
        fraction = (bar_value - bar_min)/(bar_max - bar_min);
      getState().setHorizontal_scroll_fraction( fraction );
    }
  }

}

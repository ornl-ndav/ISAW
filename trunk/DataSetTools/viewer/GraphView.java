/**
 * @(#)GraphView.java  0.1 99/08/17 Dennis Mikkelson
 *
 */
package DataSetTools.viewer;

import DataSetTools.dataset.*;
import DataSetTools.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import DataSetTools.components.image.*;
import DataSetTools.components.containers.*;
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

public class GraphView extends DataSetViewer
{                                                   // Image and border
  SplitPaneWithState   main_split_pane;

  private JPanel       display_controls  = new JPanel();

  private JPanel       graph_data_panel  = new JPanel();
  private JLabel       graph_x_label     = new JLabel();
  private JLabel       graph_y_label     = new JLabel();
  private GraphJPanel  h_graph[];

                                              // panel for the horizontal graph
  JPanel viewport = new JPanel();
  private JScrollPane  hgraph_scroll_pane = new JScrollPane( viewport, 
                                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                                          );

  private JSlider      hgraph_scale_slider = new JSlider( JSlider.HORIZONTAL,
                                                          0, 1000, 0 );
  UniformXScale        y_range;

/* --------------------------------------------------------------------------
 *
 * CONSTRUCTORS
 */

/* ------------------------------------------------------------------------ */
public GraphView( DataSet data_set ) 
{
  super(data_set, null);        

  main_split_pane = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
                                          MakeHorGraphArea(),
                                          MakeControlArea(),
                                          0.7f );

  main_split_pane.setContinuousLayout( false );

  String title = data_set.toString();
  AttributeList attr_list = data_set.getAttributeList();
  Attribute     attr      = attr_list.getAttribute(Attribute.RUN_TITLE);
  if ( attr != null )
   title = attr.getStringValue();
 
  main_split_pane.setBorder( new TitledBorder(
                                    LineBorder.createBlackLineBorder(),
                                    title ) );
  
  this.setLayout( new GridLayout(1,1) );
  this.add( main_split_pane );
  MakeConnections();                       // Add event handlers

  //----------------------------------- now extract the image from the data

  y_range = data_set.getYRange();
  DrawGraphs();
}

/* -----------------------------------------------------------------------
 *
 *  PUBLIC METHODS
 *
 */

public void doLayout()
{
  if ( main_split_pane != null )
    main_split_pane.my_setDividerLocation( 0.7f );

  super.doLayout();
}


/* ---------------------- getSelectedIndices ----------------------------- */
     
   public int[] getSelectedIndices(){ return null;}


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

  GraphView graph_view = new GraphView( data_set );
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

/* ---------------------------- MakeControlArea --------------------------- */
/*
 *  Construct the group of controls for color, logscale, etc. 
 */
private JPanel MakeControlArea()
{
  JPanel control_area = new JPanel();
  control_area.setLayout( new GridLayout(3,1) );

  display_controls.setBorder( 
                          new TitledBorder(LineBorder.createBlackLineBorder(), 
                          "DISPLAY CONTROLS" ) );
  display_controls.setLayout( new GridLayout(1,1) );

  hgraph_scale_slider.setPreferredSize( new Dimension(120,50) );
  hgraph_scale_slider.setBorder(
                          new TitledBorder(LineBorder.createBlackLineBorder(),
                          "Auto-Scale" ) );
  display_controls.add( hgraph_scale_slider );


  control_area.add( display_controls );      

  graph_data_panel.setBorder( 
                          new TitledBorder(LineBorder.createBlackLineBorder(),
                          "GRAPH DATA" ) );
  graph_data_panel.setLayout( new GridLayout(2,1) );
  graph_data_panel.add( graph_x_label ); 
  graph_data_panel.add( graph_y_label ); 
  control_area.add( graph_data_panel );      

  return control_area;
}

/* ------------------------ MakeHorizontalGraphArea ----------------------- */
/*
 * Construct panel for the horizontal graph.
 */

private JScrollPane MakeHorGraphArea( )
{
  hgraph_scroll_pane.setBorder( 
                    new TitledBorder( LineBorder.createBlackLineBorder(),
                    "HORIZONTAL GRAPH DISPLAY" ));

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
}


/* ---------------------------- DrawGraphs ----------------------------- */

private void DrawGraphs( )
{
  int  num_data_blocks = dataSet.getNum_entries();

  h_graph = new GraphJPanel[ num_data_blocks ];

  viewport.setLayout( new GridLayout(num_data_blocks, 1) );
  viewport.setPreferredSize( new Dimension(300, 100*num_data_blocks ) );
  for ( int i = 0; i < num_data_blocks; i++ )
  {
    Data data_block = dataSet.getData_entry( i );
    float x[] = data_block.getX_scale().getXs();
    float y[] = data_block.getY_values();

    JPanel border_panel = new JPanel();
    h_graph[i] = new GraphJPanel();
    border_panel.setBorder( 
                      new TitledBorder( LineBorder.createBlackLineBorder(),
                      data_block.toString() ));
    border_panel.setLayout( new GridLayout(1,1) );
    border_panel.add( h_graph[i] );
    viewport.add( border_panel );

    h_graph[i].setData( x, y );
    CoordBounds graph_bounds = h_graph[i].getGlobalWorldCoords();
    graph_bounds.setBounds( graph_bounds.getX1(), graph_bounds.getY1(),
                            graph_bounds.getX2(), graph_bounds.getY2() );
    h_graph[i].setGlobalWorldCoords( graph_bounds );
    h_graph[i].addMouseMotionListener( new HGraphMouseMotionAdapter() );
    h_graph[i].repaint();
  }
}


/* -------------------------- UpdateHGraphReadout ------------------------- */

private void UpdateHGraphReadout( GraphJPanel graph )
{
  int  num_data_blocks = dataSet.getNum_entries();

  floatPoint2D float_pt = graph.getCurrent_WC_point();
  NumberFormat f = NumberFormat.getInstance();

  String x_string = f.format( float_pt.x );
  String y_string = f.format( float_pt.y );
  graph_x_label.setText( dataSet.getX_units() + ":   "+ x_string );
  graph_y_label.setText( dataSet.getY_units() + ":   "+ y_string );
}


/* -------------------------------------------------------------------------
 *
 *  INTERNAL CLASSES
 *
 */

/* ------------------------- HGraphScaleEventHandler ----------------------- */

class HGraphScaleEventHandler implements ChangeListener
{
  public void stateChanged(ChangeEvent e)
  {
    JSlider slider = (JSlider)e.getSource();

    for ( int i = 0; i < dataSet.getNum_entries(); i++ )  // update all graph
      if ( slider.getValue() == 0 )                       // ranges
      {
        h_graph[i].autoY_bounds();
        slider.setBorder( new TitledBorder(LineBorder.createBlackLineBorder(),
                          "Auto-Scale" ) );
      }
      else
      {
        float y_min = y_range.getStart_x();
        float y_max = y_range.getEnd_x();
        float range = ( y_max - y_min ) * slider.getValue() / 1000.0f;
        h_graph[i].setY_bounds( y_min, y_min + range );
        slider.setBorder( new TitledBorder(LineBorder.createBlackLineBorder(),
                         ""+slider.getValue()/10.0f+"%(max)" ) );

      }
  }
}

class HGraphMouseMotionAdapter extends MouseMotionAdapter
{
   public void mouseDragged( MouseEvent e )
   {
     UpdateHGraphReadout( (GraphJPanel) e.getComponent() );
   }
}

}

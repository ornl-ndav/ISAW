/**
 * @(#)ImageView.java  1.0 99/7/22 Dennis Mikkelson
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
 * The image view of a Data Set.
 *
 * @see DataSetTools.dataset.DataSet
 *
 * @version 1.01
 *
 */

public class ImageView extends DataSetViewer
{                                                   // Image and border
  private ImageJPanel  image_Jpanel      = new ImageJPanel();   
  private JScrollPane  image_scroll_pane = new JScrollPane( image_Jpanel,
                                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                                          );
 
  SplitPaneWithState   main_split_pane;
  SplitPaneWithState   left_split_pane;

  private JPanel       display_controls  = new JPanel();

  private JPanel       image_data_panel  = new JPanel();
  private JLabel       image_x_label     = new JLabel();
  private JLabel       image_value_label = new JLabel();

  private JPanel       graph_data_panel  = new JPanel();
  private JLabel       graph_x_label     = new JLabel();
  private JLabel       graph_y_label     = new JLabel();

                                              // panel for the horizontal graph
  private GraphJPanel  h_graph = new GraphJPanel();
  private JPanel       graph_border = new JPanel();
  private JScrollPane  hgraph_scroll_pane = new JScrollPane( graph_border,
                                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                                          );

  private JSlider      log_scale_slider = new JSlider();
  private JSlider      hgraph_scale_slider = new JSlider(JSlider.HORIZONTAL,
                                                         0, 1000, 0);
  UniformXScale        y_range;

/* --------------------------------------------------------------------------
 *
 * CONSTRUCTORS
 */

/* ------------------------------------------------------------------------ */
public ImageView( DataSet data_set ) 
{
  super(data_set, null);        

  main_split_pane = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
                                          MakeDisplayArea(),
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

  MakeImage();

  y_range = data_set.getYRange();
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

  if ( left_split_pane != null )
    left_split_pane.my_setDividerLocation( 0.7f );

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

  ImageView image_view = new ImageView( data_set );
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


/* ------------------------- MakeImage ---------------------------- */
/*
 *  Construct displayed image from the data in the data set.
 *
 */
public void MakeImage()
{ 
  float         image_data[][];
  Data          data_block;
  Data          rebinned_data_block;
  int           num_rows = dataSet.getNum_entries();
  int           num_cols = dataSet.getMaxXSteps();
//              num_cols = 500;                   // to cut down on memory use
  UniformXScale x_scale  = dataSet.getXRange();

//  System.out.println("Making image with" + num_rows + " rows" );
//  System.out.println("XRange is        " + x_scale );
//  System.out.println("num_cols         " + num_cols );

  x_scale.setNum_x( num_cols );
  if ( num_cols < 2 )    // #### degenerate size of JPanel, so just return
    return;

  image_data = new float[ num_rows ][];
  for ( int i = 0; i < num_rows; i++ )
  {
    data_block = dataSet.getData_entry(i);
    rebinned_data_block = (Data)data_block.clone();
    rebinned_data_block.ReBin( x_scale );
    image_data[i] = rebinned_data_block.getY_values();
  }
                              // set the log scale and image data, but don't
                              // remake the image yet. It's done when the
                              // component is initially resized.     
  image_Jpanel.changeLogScale( log_scale_slider.getValue(), false );
  image_Jpanel.setData( image_data, false );
  CoordBounds bounds = new CoordBounds( x_scale.getStart_x(), 0,
                                        x_scale.getEnd_x(), num_rows-1 );
  image_Jpanel.setGlobalWorldCoords( bounds );
}

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
  display_controls.setLayout( new GridLayout(2,1) );
  log_scale_slider.setPreferredSize( new Dimension(120,50) );
  log_scale_slider.setValue(30);
  log_scale_slider.setMajorTickSpacing(20);
  log_scale_slider.setMinorTickSpacing(5);
  log_scale_slider.setPaintTicks(true);
  log_scale_slider.setBorder( 
                          new TitledBorder(LineBorder.createBlackLineBorder(),
                          "Brightness" ) );
  display_controls.add( log_scale_slider );

  hgraph_scale_slider.setPreferredSize( new Dimension(120,50) );
  hgraph_scale_slider.setBorder(
                          new TitledBorder(LineBorder.createBlackLineBorder(),
                          "Auto-Scale" ) );
  display_controls.add( hgraph_scale_slider );


  control_area.add( display_controls );      

  image_data_panel.setBorder( 
                          new TitledBorder(LineBorder.createBlackLineBorder(),
                          "IMAGE_DATA" ) );
  image_data_panel.setLayout( new GridLayout(2,1) );
  image_data_panel.add( image_x_label );
  image_data_panel.add( image_value_label );
  control_area.add( image_data_panel );   
   
  graph_data_panel.setBorder( 
                          new TitledBorder(LineBorder.createBlackLineBorder(),
                          "GRAPH DATA" ) );
  graph_data_panel.setLayout( new GridLayout(2,1) );
  graph_data_panel.add( graph_x_label ); 
  graph_data_panel.add( graph_y_label ); 
  control_area.add( graph_data_panel );      

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
                                    0.65f );

  left_split_pane.setContinuousLayout( true );

  return left_split_pane;
}


/* ------------------------ MakeHorizontalGraphArea ----------------------- */
/*
 * Construct panel for the horizontal graph.
 */

private JScrollPane MakeHorGraphArea( )
{
  graph_border.setLayout( new GridLayout(1,1) );
  graph_border.setBorder( 
                    new TitledBorder( LineBorder.createBlackLineBorder(),
                    "HORIZONTAL GRAPH DISPLAY" ));
  graph_border.add( h_graph );

  return hgraph_scroll_pane;
}


/* --------------------------- MakeImageArea ---------------------------- */
/*
 * Construct a panel with a border for the main image area.
 */

private JScrollPane MakeImageArea( )
{
  String title;

  OperationLog op_log = dataSet.getOp_log();
  if ( op_log.numEntries() <= 0 )
    title = "IMAGE DISPLAY AREA";
  else
    title = op_log.getEntryAt( op_log.numEntries() - 1 );

  image_scroll_pane.setBorder( new TitledBorder( 
                                LineBorder.createBlackLineBorder(),
                                title ) ); 

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
   h_graph.addMouseMotionListener( new HGraphMouseMotionAdapter() );
}

/* -------------------------- UpdateImageReadout ------------------------- */

private void UpdateImageReadout( )
{
  int  num_data_blocks = dataSet.getNum_entries();
     
  floatPoint2D float_pt = image_Jpanel.getCurrent_WC_point();

  NumberFormat f = NumberFormat.getInstance();

  String x_string = f.format( float_pt.x );
  image_x_label.setText( dataSet.getX_units() + ":   "+ x_string );

  String y_string = f.format( image_Jpanel.ImageValue_at_Cursor() );
  image_value_label.setText( dataSet.getY_units() + ":   "+ y_string );
}

/* ---------------------------- RedrawHGraph ----------------------------- */

private void RedrawHGraph( )
{
  int  num_data_blocks = dataSet.getNum_entries();
     
  Point pix_pt = image_Jpanel.getCurrent_pixel_point();
  int row      = image_Jpanel.ImageRow_of_PixelRow( pix_pt.y );

  Data  data_block;
  data_block = dataSet.getData_entry( row );
  float x[] = data_block.getX_scale().getXs();
  float y[] = data_block.getY_values();

  graph_border.setBorder( new TitledBorder( LineBorder.createBlackLineBorder(),
                          data_block.toString() ));

  h_graph.setData( x, y );
  CoordBounds graph_bounds = h_graph.getGlobalWorldCoords();
  CoordBounds image_bounds = image_Jpanel.getLocalWorldCoords();
  graph_bounds.setBounds( image_bounds.getX1(), graph_bounds.getY1(),
                          image_bounds.getX2(), graph_bounds.getY2() );
  h_graph.setGlobalWorldCoords( graph_bounds );
  h_graph.repaint();
}


/* -------------------------- UpdateHGraphReadout ------------------------- */

private void UpdateHGraphReadout( )
{
  int  num_data_blocks = dataSet.getNum_entries();

  floatPoint2D float_pt = h_graph.getCurrent_WC_point();
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

/* ---------------------------- LogScaleEventHandler ----------------------- */

class LogScaleEventHandler implements ChangeListener
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
class LogScaleMouseHandler extends MouseAdapter
{
  public void mouseReleased(MouseEvent e)
  {
    JSlider slider = (JSlider)e.getSource();
    image_Jpanel.changeLogScale( slider.getValue(), true );
  }
}

/* ------------------------- HGraphScaleEventHandler ----------------------- */

class HGraphScaleEventHandler implements ChangeListener
{
  public void stateChanged(ChangeEvent e)
  {
    JSlider slider = (JSlider)e.getSource();

    if ( slider.getValue() == 0 )
    {
      h_graph.autoY_bounds();
      slider.setBorder( new TitledBorder(LineBorder.createBlackLineBorder(),
                          "Auto-Scale" ) );
    }
    else
    {
      float y_min = y_range.getStart_x();
      float y_max = y_range.getEnd_x();
      float range = ( y_max - y_min ) * slider.getValue() / 1000.0f;
      h_graph.setY_bounds( y_min, y_min + range );
      slider.setBorder( new TitledBorder(LineBorder.createBlackLineBorder(),
                         ""+slider.getValue()/10.0f+"%(max)" ) );
    }
  }
}


/* ------------------------ ImageMouseMotionAdapter ------------------------ */

class ImageMouseMotionAdapter extends MouseMotionAdapter 
{
   int last_image_row = 0;
   int last_image_col = 0;

   public void mouseDragged( MouseEvent e )
   {
// #### BUG in symmantec or jdk1.1.3 prevents proper detection of button ###
//    if ( (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 )
     { 
       floatPoint2D float_pt = image_Jpanel.getCurrent_WC_point();
       Point pix_pt          = image_Jpanel.getCurrent_pixel_point();
       int row = image_Jpanel.ImageRow_of_PixelRow( pix_pt.y );
       int col = image_Jpanel.ImageCol_of_PixelCol( pix_pt.x );

       if ( row == last_image_row && col == last_image_col )
         return;

       last_image_row = row;
       last_image_col = col;

       UpdateImageReadout();
       RedrawHGraph();
     }
   }
}


class HGraphMouseMotionAdapter extends MouseMotionAdapter
{
   public void mouseDragged( MouseEvent e )
   {
     UpdateHGraphReadout();
   }
}


}

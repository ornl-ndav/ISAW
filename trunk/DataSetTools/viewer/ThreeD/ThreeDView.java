/**
 * File:  ThreeDView.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2001/05/29 15:06:47  dennis
 * Now shows colored markers with colors corresponding to TOF
 *
 * Revision 1.2  2001/05/23 17:26:14  dennis
 * Now uses a ViewController to change the observer's
 * viewing position.
 *
 * Revision 1.1  2001/05/09 21:32:00  dennis
 * Viewer to display 3D view of Data block positions, if they have and
 * attribute that is of type Position3D.
 *
 */

package DataSetTools.viewer.ThreeD;

import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.components.image.*;
import DataSetTools.viewer.*;
import DataSetTools.math.*;
import DataSetTools.components.containers.*;
import DataSetTools.components.ThreeD.*;
import DataSetTools.components.ui.*;
import DataSetTools.retriever.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * Provides a mechanism for selecting and viewing portions of a Data Set using 
 * stacked or overplotted graphs.
 *
 * @see DataSetTools.dataset.DataSet
 * @see DataSetTools.viewer.DataSetViewer
 *
 */

public class ThreeDView extends DataSetViewer
{                         
  private final int LOG_TABLE_SIZE      = 60000;
  private final int NUM_POSITIVE_COLORS = 127;
  private final int NUM_PSEUDO_COLORS   = 2 * NUM_POSITIVE_COLORS + 1;
  private final int ZERO_COLOR_INDEX    = NUM_POSITIVE_COLORS;

  private ThreeD_JPanel       threeD_panel  = null; 
  private Box                 control_panel = null; 
  private AltAzController     view_control  = null;
  private AnimationController frame_control = null;
  private SplitPaneWithState  split_pane  = null;
  private Color               colors[][]  = null;
  private IThreeD_Object      objects[]   = null;
  private float               log_scale[] = null;

/* --------------------------------------------------------------------------
 *
 * CONSTRUCTORS
 */

/* ------------------------------------------------------------------------ */

public ThreeDView( DataSet data_set, ViewerState state ) 
{
  super(data_set, state);  // Records the data_set and current ViewerState
                           // object in the parent class and then
                           // sets up the menu bar with items handled by the
                           // parent class.

  setLogScale( 50 );
  init();
                                        // Add an item to the Option menu and
                                        // add a listener for the option menu
                                        // If the menu options are dependent
                                        // on the DataSet, they must be added
                                        // in init.  Unfortunately, in that 
                                        // case, the old versions would have
                                        // to be removed before adding the 
                                        // new ones.
  OptionMenuHandler option_menu_handler = new OptionMenuHandler();
  JMenu option_menu = menu_bar.getMenu( OPTION_MENU_ID );

  JMenuItem button = new JMenuItem( "My new option" );
  button.addActionListener( option_menu_handler );
  option_menu.add( button );

  MakeThreeD_Scene();
}

/* -----------------------------------------------------------------------
 *
 *  PUBLIC METHODS
 *
 */


public void redraw( String reason )
{
    // This will be called by the "outside world" if the contents of the
    // DataSet are changed and it is necesary to redraw the graphs using the
    // current DataSet.

   if ( reason == IObserver.POINTED_AT_CHANGED )
   {
      DataSet ds = getDataSet();
      Vector3D detector_location = detector_position( ds.getPointedAtIndex() );

      Point   pixel_point;
      if ( detector_location != null )
      {
        pixel_point = threeD_panel.project( detector_location );
        threeD_panel.set_crosshair( pixel_point );
      }
   }
   else
     MakeThreeD_Scene();
}


public void setDataSet( DataSet ds )
{   
  // This will be called by the "outside world" if the viewer is to replace 
  // its reference to a DataSet by a reference to a new DataSet, ds, and
  // rebuild the entire display, titles, borders, etc.

  setVisible( false );
  super.setDataSet( ds );
  init();
  redraw( NEW_DATA_SET );
  setVisible( true );
}


/* -------------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */


private Vector3D detector_position( int index )
{
  DataSet ds = getDataSet();
  int     n_data = ds.getNum_entries();

  if ( index < 0 || index >= n_data )
    return null;

  Data d = ds.getData_entry(index);
  
  Position3D position= (Position3D)d.getAttributeValue( Attribute.DETECTOR_POS);
  
  if ( position == null )
    return null;

  float coords[] = position.getCartesianCoords();
  Vector3D pt_3D = new Vector3D( coords[0], coords[1], coords[2] );

  return pt_3D;
}


private void MakeThreeD_Scene()
{
  DataSet ds       = getDataSet();
  int     n_data   = ds.getNum_entries();

  boolean all_null   = true;
  float   max_radius = 0;
  float   radius = 0.01f;

  Vector3D  points[]  = new Vector3D[1];
  Vector3D  point;
  points[0] = new Vector3D();
  objects   = new IThreeD_Object[ n_data + 3 ];

  for ( int i = 0; i < n_data; i++ )
  {
    point = detector_position( i );
    if ( point == null )
      objects[i] = new ThreeD_Non_Object();
    else
    {
      all_null = false;
      radius = point.length();
      if ( radius > max_radius )
        max_radius = radius;
 
      points[0]= point;
      objects[i] = new Polymarker( points, Color.red ); 
      objects[i].setPickID( i );
      ((Polymarker)(objects[i])).setType( Polymarker.STAR );
      ((Polymarker)(objects[i])).setSize( 2 );
    }
  }

  if ( all_null )
  {
    objects = null;
    threeD_panel.setObjects( objects );
  }
  else
  {
    radius = max_radius;
    add_axes( objects, radius/5 );
    view_control.setViewAngle( 40 );
    view_control.setAltitudeAngle( 30 );
    view_control.setAzimuthAngle( 0 );
    view_control.setDistanceRange( 0.5f*radius, 5*radius );
    view_control.setDistance( 4f*radius );
    threeD_panel.setObjects( objects );
    view_control.apply( true );
  }

  MakeColorList();
  set_colors( 50 );
}


private void set_colors( int frame )
{
  Color new_colors[] = new Color[ colors.length ];
  for ( int i = 0; i < colors.length; i++ )
    new_colors[i] = colors[i][frame];

  threeD_panel.setColors( new_colors );
  threeD_panel.repaint(0 );
}


private void MakeColorList()
{
  System.out.println("Start MakeColorList");

  DataSet ds = getDataSet();
  float   y_vals[][];

  int  num_rows = ds.getNum_entries();
  if ( num_rows == 0 ) 
    return;

  UniformXScale x_scale = ds.getXRange();
  float x_min    = x_scale.getStart_x();
  float x_max    = x_scale.getEnd_x();
//  int   num_cols = x_scale.getNum_x();
  int   num_cols = 500;
  x_scale = new UniformXScale( x_min, x_max, num_cols );

  frame_control.setFrame_values( x_scale.getXs() );

  if ( num_cols == 0 )
    return;
  
  colors = new Color[num_rows][num_cols];

  y_vals = new float[num_rows][];
  Data  data_block;
  Data  rebinned_data_block;
  for ( int i = 0; i < num_rows; i++ )
  {
    data_block = ds.getData_entry(i);
    rebinned_data_block = (Data)data_block.clone();

/*
    if ( !rebinned_data_block.isFunction() )           // need to treat it as
      rebinned_data_block.ConvertToFunction( false );  // intensity for image
                                                       // display when starting
                                                       // with widely different
                                                       // sizes of x-bins
*/
    rebinned_data_block.ResampleUniformly( x_scale );
    y_vals[i] = rebinned_data_block.getY_values();
  }

  float max_data = Float.NEGATIVE_INFINITY;
  float min_data = Float.POSITIVE_INFINITY;
  float val;
  for ( int i = 0; i < y_vals.length; i++ )
   for ( int j = 0; j < y_vals[0].length; j++ )
   {
     val = y_vals[i][j];
     if ( val > max_data )
       max_data = y_vals[i][j];
     if ( val < min_data )
       min_data = y_vals[i][j]; 
   } 

  float max_abs = 0;
  if ( Math.abs( max_data ) > Math.abs( min_data ) )
    max_abs = Math.abs( max_data );
  else
    max_abs = Math.abs( min_data );

  float scale_factor;
  if ( max_abs > 0 )
    scale_factor = (LOG_TABLE_SIZE - 1) / max_abs;
  else
    scale_factor = 0;

  Color color_table[] =IndexColorMaker.getDualColorTable(
                                         IndexColorMaker.HEATED_OBJECT_SCALE_2,
                                         NUM_POSITIVE_COLORS );
  int index;
  for ( int i = 0; i < y_vals.length; i++ )
   for ( int j = 0; j < y_vals[0].length; j++ )
    {
      val = y_vals[i][j] * scale_factor;
      if ( val >= 0 )
        index = (int)( ZERO_COLOR_INDEX + log_scale[(int)val] );
      else
        index = (int)( ZERO_COLOR_INDEX - log_scale[(int)(-val)] );

      colors[i][j] = color_table[index];
    }
  System.out.println("End MakeColorList");
}


/* ----------------------------- setLogScale -------------------------- */

  private void setLogScale( double s )
  {
    if ( s > 100 )                                // clamp s to [0,100]
      s = 100;
    if ( s < 0 )
      s = 0;

    s = Math.exp(20 * s / 100.0) + 0.1; // map [0,100] exponentially to get
                                        // scale change that appears more linear
    double scale = NUM_POSITIVE_COLORS / Math.log(s);

    log_scale = new float[LOG_TABLE_SIZE];

    for ( int i = 0; i < LOG_TABLE_SIZE; i++ )
      log_scale[i] = (byte)
                     (scale * Math.log(1.0+((s-1.0)*i)/LOG_TABLE_SIZE));
  }


private void add_axes( IThreeD_Object objects[], float length  )
{
  int      index = objects.length-3;
  Vector3D points[] = new Vector3D[2];

  points[0] = new Vector3D( 0, 0, 0 );                    // x-axis
  points[1] = new Vector3D( length, 0, 0 );
  objects[index] = new Polyline( points, Color.red );
  index++;
                                                          // y_axis
  points[1] = new Vector3D( 0, length, 0 );
  objects[index] = new Polyline( points, Color.green );
  index++;
                                                          // z_axis
  points[1] = new Vector3D( 0, 0, length );
  objects[index] = new Polyline( points, Color.blue );
}


/* ----------------------------- main ------------------------------------ */
/*
 *  For testing purposes only
 */
public static void main(String[] args)
{
/*
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

    spectrum = new Data( x_scale, y_values, id ); 
    float x = 0;
    float y = 0;
    x = (float)( 2*Math.cos( id*Math.PI/10 ) );
    y = (float)( 2*Math.sin( id*Math.PI/10 ) );
    DetectorPosition pos = new DetectorPosition();
    pos.setCartesianCoords( x, y, 0 );
    DetPosAttribute  att = new DetPosAttribute( Attribute.DETECTOR_POS, pos );
    spectrum.setAttribute( att );
    data_set.addData_entry( spectrum );    
  }
*/
//  String file_name = "/usr/home/dennis/ARGONNE_DATA/glad0816.run";
//  String file_name = "/usr/home/dennis/ARGONNE_DATA/GLAD4696.RUN";
//  String file_name = "/usr/home/dennis/ARGONNE_DATA/hrcs2936.run";
  String file_name = "/usr/home/dennis/ARGONNE_DATA/GPPD12358.RUN";

  RunfileRetriever rr = new RunfileRetriever( file_name ); 
  DataSet data_set = rr.getDataSet( 1 );
  ThreeDView view = new ThreeDView( data_set, null );
  JFrame f = new JFrame("Test for ThreeDView");
  f.setBounds(0,0,600,400);
  f.setJMenuBar( view.getMenuBar() );
  f.getContentPane().add( view );
  f.setVisible( true );
}
   

/* -----------------------------------------------------------------------
 *
 * PRIVATE METHODS
 *
 */ 


private void init()
{
  if ( threeD_panel != null )          // get rid of old components first 
  {
    threeD_panel.removeAll();
    split_pane.removeAll();
    control_panel.removeAll();
    removeAll();
  }
  threeD_panel  = new ThreeD_JPanel();
  threeD_panel.setBackground( new Color( 100, 100, 100 ) );

  control_panel = new Box( BoxLayout.Y_AXIS );

  view_control  = new AltAzController();
  view_control.addControlledPanel( threeD_panel );
  control_panel.add( view_control );

  frame_control = new AnimationController();
  frame_control.setBorderTitle( getDataSet().getX_label() );
  frame_control.setTextLabel( getDataSet().getX_units() );
  control_panel.add( frame_control );

  JPanel filler = new JPanel();
  filler.setPreferredSize( new Dimension( 120, 2000 ) );
  control_panel.add( filler );

 
                                        // make a titled border around the
                                        // whole viewer, using an appropriate
                                        // title from the DataSet. 
  String title = getDataSet().toString();
  AttributeList attr_list = getDataSet().getAttributeList();
  Attribute     attr      = attr_list.getAttribute(Attribute.RUN_TITLE);
  if ( attr != null )
   title = attr.getStringValue();

  TitledBorder border = new TitledBorder(
                                    LineBorder.createBlackLineBorder(), title);
  border.setTitleFont( FontUtil.BORDER_FONT );
  setBorder( border );
            
                                     // Place the graph area inside of a
                                     // JPanel and make a titled border around
                                     // the JPanel graph area using the last 
                                     // message, if available
  JPanel graph_container = new JPanel();
  OperationLog op_log = getDataSet().getOp_log();
  if ( op_log.numEntries() <= 0 )
    title = "Graph Display Area";
  else
    title = op_log.getEntryAt( op_log.numEntries() - 1 );

  border = new TitledBorder( LineBorder.createBlackLineBorder(), title );
  border.setTitleFont( FontUtil.BORDER_FONT );
  graph_container.setBorder( border );
  graph_container.setLayout( new GridLayout(1,1) );
  graph_container.add( threeD_panel );
  
  split_pane = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
                                       graph_container,
                                       control_panel,
                                       0.7f );

                                        // Add the control area and graph
                                        // container to the main viewer
                                        // panel and draw.
  setLayout( new GridLayout(1,1) );
  add ( split_pane ); 

  redraw( NEW_DATA_SET );

  threeD_panel.addMouseMotionListener( new ViewMouseMotionAdapter() );
  frame_control.addActionListener( new FrameControlListener() );
}


/* -------------------------------------------------------------------------
 *
 *  INTERNAL CLASSES
 *
 */

/**
 *  Listen for mouse motion events and just print out the pixel coordinates
 *  to demonstrate how to handle such events.
 */
class ViewMouseMotionAdapter extends MouseMotionAdapter
{
   int last_index = IThreeD_Object.INVALID_PICK_ID;

   public void mouseDragged( MouseEvent e )
   {
     // System.out.println("Mouse moved at: " + e.getPoint() );
     Point pt = e.getPoint();
     int index = threeD_panel.pickID( e.getX(), e.getY(), 10 );
     if ( index != last_index )
     {
       last_index = index;
       DataSet ds = getDataSet();
       if ( index >= 0 && index < ds.getNum_entries() ) 
       {
         ds.setPointedAtIndex( index );
         ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
       }
     }
   }
}


/**
 *  Listen for Option menu selections and just print out the selected option.
 *  It may be most convenient to have a separate listener for each menu.
 */
private class OptionMenuHandler implements ActionListener
{
  public void actionPerformed( ActionEvent e )
  {
    String action = e.getActionCommand();
    System.out.println("The user selected : " + action );
  }
}

private class FrameControlListener implements ActionListener
{
  public void actionPerformed( ActionEvent e )
  {
    String action = e.getActionCommand();
    int    frame  = Integer.valueOf(action).intValue();
    set_colors( frame );
  }

}


}

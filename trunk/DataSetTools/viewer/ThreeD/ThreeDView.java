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
 * Revision 1.2  2001/05/23 17:26:14  dennis
 * Now uses a ViewController to change the observer's
 * viewing position.
 *
 * Revision 1.1  2001/05/09 21:32:00  dennis
 * Viewer to display 3D view of Data block positions, if they have and
 * attribute that is of type Position3D.
 *
 *
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
  private ThreeD_JPanel       threeD_panel  = null; 
  private JPanel              control_panel = null; 
  private AltAzController     view_control  = null;
  private SplitPaneWithState  split_pane = null;

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

  IThreeD_Object objects[] = new IThreeD_Object[ n_data + 3 ];
  Vector3D       points[]  = new Vector3D[1];
  Vector3D       point;
  points[0] = new Vector3D();

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
      objects[i] = new Polymarker( points, Color.black ); 
      objects[i].setPickID( i );
      ((Polymarker)(objects[i])).setType( Polymarker.CROSS );
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
    view_control.setViewAngle( 50 );
    view_control.setAltitudeAngle( 30 );
    view_control.setAzimuthAngle( 0 );
    view_control.setDistanceRange( 0.5f*radius, 10*radius );
    view_control.setDistance( 2.5f*radius );
    threeD_panel.setObjects( objects );
    view_control.apply();
  }

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
  threeD_panel.setBackground( Color.white );

  view_control  = new AltAzController();
  view_control.addControlledPanel( threeD_panel );
  control_panel = new JPanel();
  control_panel.setLayout( new GridLayout( 1,1 ) );
  control_panel.add( view_control );
 
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
  


                                        // make a titled border around the
                                        // control area
  border = new TitledBorder( LineBorder.createBlackLineBorder(), "Controls" );
  border.setTitleFont( FontUtil.BORDER_FONT );
  control_panel.setBorder( border );

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
  threeD_panel.addComponentListener( new ViewComponentAdapter() );
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
 *  Listen for resize events on the and just print out the new size to
 *  to demonstrate how to handle such events.
 */
class ViewComponentAdapter extends ComponentAdapter
{
  public void componentResized( ComponentEvent c )
  {
//    System.out.println("View Area resized: " + c.getComponent().getSize() );
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

}

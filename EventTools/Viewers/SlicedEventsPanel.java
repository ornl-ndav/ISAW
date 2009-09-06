/* 
 * File: SlicedEventsPanel.java
 *
 * Copyright (C) 2009, Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.Viewers;

//import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//import javax.swing.*;
import javax.media.opengl.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.ColorScaleControl.*;
import gov.anl.ipns.ViewTools.Panels.Image.*;

import EventTools.EventList.IEventList3D;
import EventTools.EventList.ByteFile16EventList3D;
import EventTools.Histogram.Histogram3D;
import EventTools.Histogram.IProjectionBinner3D;
import EventTools.Histogram.ProjectionBinner3D;
import EventTools.Histogram.UniformEventBinner;
import EventTools.Histogram.IEventBinner;

import SSG_Tools.Cameras.OrthographicCamera;

import SSG_Tools.SSG_Nodes.StateControls.glEnableNode;
import SSG_Tools.SSG_Nodes.StateControls.glDisableNode;
import SSG_Tools.SSG_Nodes.SimpleShapes.*;
import SSG_Tools.SSG_Nodes.*;
import SSG_Tools.SSG_Nodes.Groups.Transforms.*;
import SSG_Tools.SSG_Nodes.Groups.Switches.*;
import SSG_Tools.Cameras.*;
import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;


/**
 * Intial version of display of a 3D event list using GL_POINTS with 
 * texture mapped slicing plane.
 * This can provide a very fast and useful display of a large number of
 * events.
 */

public class SlicedEventsPanel 
{
  private Group           points_group;
  private Group           marker_group;
  private OnOff           axis_group;
  private SimpleShape     marker_shape;
  private Color[]         colors;
  private int[]           color_tran;
  private float           min = 10f;
  private float           max = 500;
  private boolean         filter_above_max = false;
  private float           point_size = 3;
  private boolean         filter_below_min = true;
  private boolean         use_alpha        = false;
  private float           alpha            = 1;
  private boolean         orthographic     = false;

  private JoglPanel       jogl_panel;

  public SlicedEventsPanel( )
  {
    Vector3D xmin = new Vector3D( -25,  0,  0 );
    Vector3D xmax = new Vector3D(  25,  0,  0 );
    Vector3D ymin = new Vector3D(  0, -25,  0 );
    Vector3D ymax = new Vector3D(  0,  25,  0 );
    Vector3D zmin = new Vector3D(  0,  0, -25 );
    Vector3D zmax = new Vector3D(  0,  0,  25 );

    Axis x_axis = Axis.getInstance( xmin, xmax, "X-Axis", Color.RED );
    Axis y_axis = Axis.getInstance( ymin, ymax, "Y-Axis", Color.GREEN );
    Axis z_axis = Axis.getInstance( zmin, zmax, "Z-Axis", Color.BLUE );

    axis_group = new OnOff( true );
    axis_group.addChild( x_axis );
    axis_group.addChild( y_axis );
    axis_group.addChild( z_axis );

    points_group = new Group();
    marker_group = new Group();

    Group root = new Group();
    root.addChild( new glDisableNode(GL.GL_LIGHTING) );
    root.addChild( axis_group );
    root.addChild( points_group );
    root.addChild( marker_group );

//  jogl_panel = new JoglPanel( root, true, JoglPanel.DEBUG_MODE );
    jogl_panel = new JoglPanel( root, true, JoglPanel.NORMAL_MODE );

    Camera camera = jogl_panel.getCamera();
    camera.setVUV( new Vector3D(   0, 0,  1 ) );
    camera.setVRP( new Vector3D(  -7, 0,  0 ) );
    camera.setCOP( new Vector3D( -20, 0, 10 ) );
    camera.SetViewVolume( 0.25f, 250, 60 );

//    jogl_panel.setBackgroundColor( Color.GRAY );
    Color background_color = new Color( 238, 238, 238 );
    jogl_panel.setBackgroundColor( background_color );

    new MouseArcBall( jogl_panel );

    colors = IndexColorMaker.getColorTable( "Heat 1", 128 );
    color_tran = new int[ colors.length ];
    for ( int i = 0; i < color_tran.length; i++ )
      color_tran[i] = i;
  }

  public JoglPanel getJoglPanel()
  {
    return jogl_panel;
  }


  /**
   * Add the specified event list in 3D to the collection of points to
   * be drawn.
   * 
   * @param events       The event list to display. 
   */
  public void addEvents( IEventList3D events )
  {
    float[] xyz_vals = events.eventVals();
    float[] weights = events.eventWeights();

    float[] codes = new float[xyz_vals.length/3];
    if ( weights == null )
    {
      for ( int i = 0; i < codes.length; i++ )
        codes[i] = 1;
    }
    else
    {
      for ( int i = 0; i < weights.length; i++ )
        codes[i] = weights[i];
    }

    color_tran = new int[ colors.length ];
    for ( int i = 0; i < color_tran.length; i++ )
      color_tran[i] = i;

    MultiColoredPointList  points = new MultiColoredPointList( xyz_vals,
                                                               codes,
                                                               min,
                                                               max,
                                                               color_tran,
                                                               colors,
                                                               point_size,
                                                               1.0f );
    points.setDrawOptions( filter_above_max,
                           filter_below_min,
                           point_size,
                           use_alpha,
                           alpha );

    points_group.addChild( points );
//    jogl_panel.Draw();
  }


  public void clear()
  {
    ClearEvents();
    ClearMarkers();
    // TODO Clear slice plane; 
  }

  public void ClearEvents()
  {
    points_group.Clear();
  }


  public void addMarkers( Vector3D[] verts,
                          int        size,
                          int        type,
                          Color      color )
  {
    ClearMarkers();
    marker_shape = new Polymarker(verts, size, type, color);
    marker_group.addChild( marker_shape );
  }

 
  public void ClearMarkers()
  {
    marker_group.Clear();
    marker_shape = null;           // get rid of old markers so we can't
                                   // show "stale" markers.
  }


  public void SetMarkersOnOff( boolean turn_on )
  {
    if ( marker_shape == null )    // nothing to do
      return;

    if ( turn_on )
    {
      marker_group.Clear();
      marker_group.addChild( marker_shape );
    }
    else
      marker_group.Clear();
  }


  public void setOrthographicView( boolean set_ortho_on )
  {
    Camera camera = jogl_panel.getCamera();
    if ( set_ortho_on )
    { 
      if ( camera instanceof PerspectiveCamera )
      {
        camera = new OrthographicCamera( camera );
        jogl_panel.setCamera( camera );
        jogl_panel.Draw();
      }      
    }
    else
      if ( camera instanceof OrthographicCamera )
      {
        camera = new PerspectiveCamera( camera );
        jogl_panel.setCamera( camera );
        jogl_panel.Draw();
      }
  }


  public void setColors( ColorScaleInfo color_info )
  {
    String         scale_name  = color_info.getColorScaleName();
    int            num_colors  = color_info.getNumColors();
    byte[]         color_table = color_info.getColorIndexTable();
    float          min         = color_info.getTableMin();
    float          max         = color_info.getTableMax();

    colors = IndexColorMaker.getColorTable( scale_name, num_colors );
    color_tran = new int[color_table.length];
    for ( int i = 0; i < color_table.length; i++ )
      color_tran[i] = color_table[i];

    Vector<MultiColoredPointList> nodes = getColoredPointLists();
    for ( int i = 0; i < nodes.size(); i++ )
      nodes.elementAt(i).setColorInfo( min, max, color_tran, colors );
  }


  public void setDrawingOptions( boolean filter_above_max,
                                 boolean filter_below_min,
                                 boolean show_axes,
                                 float   point_size,
                                 boolean use_alpha,
                                 float   alpha,
                                 boolean orthographic )
  {
    this.filter_above_max = filter_above_max;
    this.filter_below_min = filter_below_min;
    this.point_size       = point_size;
    this.use_alpha        = use_alpha;
    this.alpha            = alpha;
    this.orthographic     = orthographic;

    Vector<MultiColoredPointList> nodes = getColoredPointLists();
    for ( int i = 0; i < nodes.size(); i++ )
      nodes.elementAt(i).setDrawOptions( filter_above_max,
                                         filter_below_min,
                                         point_size,
                                         use_alpha,
                                         alpha );
    axis_group.set( show_axes );
    setOrthographicView( orthographic );
  }


  public void updateDisplay()
  {
    jogl_panel.Draw();
  }


  /**
   *  Get all of the multicolored point lists that are displayed by
   *  this viewer.
   */
  private Vector<MultiColoredPointList> getColoredPointLists()
  {
      Vector<MultiColoredPointList> nodes = 
                                      new Vector<MultiColoredPointList>(); 

                                      // the following allows for possible
                                      // transformation nodes above the
                                      // colored point list nodes.
      Node                    node;
      MultiColoredPointList point_list_node;
      for ( int i = 0; i < points_group.numChildren(); i++ )
      {
         node = points_group.getChild( i );
         while ( !(node instanceof MultiColoredPointList) )
           node = ((Group)node).getChild(0);

         point_list_node = (MultiColoredPointList)node;
         nodes.add( point_list_node );
      }

      return nodes;
  }


  public static void main( String args[] ) throws Exception
  {

    long start_time = System.nanoTime();
    ByteFile16EventList3D events = new ByteFile16EventList3D(args[0]);
    long elapsed = System.nanoTime() - start_time;
    System.out.println("Time(ms) to load events = " + elapsed/1.e6);

    start_time = System.nanoTime();
    int NUM_BINS = 512;

    Vector3D xVec = new Vector3D(1,0,0);
    Vector3D yVec = new Vector3D(0,1,0);
    Vector3D zVec = new Vector3D(0,0,1);
    
    IEventBinner x_bin1D = new UniformEventBinner( -26.5f,  0,    NUM_BINS );
    IEventBinner y_bin1D = new UniformEventBinner( -12.8f, 12.8f, NUM_BINS );
    IEventBinner z_bin1D = new UniformEventBinner( -12.8f, 12.8f, NUM_BINS );

    IProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, xVec);
    IProjectionBinner3D y_binner = new ProjectionBinner3D(y_bin1D, yVec);
    IProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, zVec);

    Histogram3D histogram = new Histogram3D(x_binner, y_binner, z_binner);

    elapsed = System.nanoTime() - start_time;
    System.out.println("Time(ms) to allocate histogram = " + elapsed/1.e6);

    start_time = System.nanoTime();
    histogram.addEvents( events );

    SlicedEventsPanel my_panel = new SlicedEventsPanel( );

    elapsed = System.nanoTime() - start_time;
    System.out.println("Time(ms) to add events to histogram = " + elapsed/1.e6);

    System.out.println("histogram.minVal() = " + histogram.minVal() );
    System.out.println("histogram.maxVal() = " + histogram.maxVal() );

    my_panel.addEvents( events );

    JFrame frame = new JFrame( args[0] );
    frame.setSize(750,750);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( my_panel.getJoglPanel().getDisplayComponent() );
    frame.setVisible( true );

    my_panel.updateDisplay();
  }

}

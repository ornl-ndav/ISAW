/* 
 * File: SlicedEventsViewer.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import javax.swing.*;
import javax.media.opengl.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.ColorScaleControl.*;

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
import SSG_Tools.Cameras.*;
import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;

import gov.anl.ipns.ViewTools.Panels.Image.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.FrameController;

/**
 * Intial version of display of a 3D event list using GL_POINTS with 
 * texture mapped slicing plane.
 * This can provide a very fast and useful display of a large number of
 * events.
 */

public class SlicedEventsViewer 
{
  private Histogram3D     histogram;
  private ImageJPanel     panel;
  private Group           plane_group;
  private Group           points_group;
  private Color[]         colors;
  private int[]           color_tran;
  private float           alpha = 0.55f;
  private FrameController frame_control;
  private JCheckBox       filter_on_max;
  private JCheckBox       filter_on_min;
  private JCheckBox       alpha_blend;
  private JCheckBox       orthographic;

  private ColorEditPanel  color_control;
  private JoglPanel       jogl_panel;


  public SlicedEventsViewer( Histogram3D histogram, String title )
  {
    this.histogram = histogram;

    Group root = new Group();
    root.addChild( new glDisableNode(GL.GL_LIGHTING) );

    Vector3D xmin = new Vector3D( -25,  0,  0 );
    Vector3D xmax = new Vector3D(  25,  0,  0 );
    Vector3D ymin = new Vector3D(  0, -25,  0 );
    Vector3D ymax = new Vector3D(  0,  25,  0 );
    Vector3D zmin = new Vector3D(  0,  0, -25 );
    Vector3D zmax = new Vector3D(  0,  0,  25 );

    Axis x_axis = Axis.getInstance( xmin, xmax, "X-Axis", Color.RED );
    Axis y_axis = Axis.getInstance( ymin, ymax, "Y-Axis", Color.GREEN );
    Axis z_axis = Axis.getInstance( zmin, zmax, "Z-Axis", Color.BLUE );

    root.addChild( x_axis );
    root.addChild( y_axis );
    root.addChild( z_axis );

    plane_group  = new Group();
    points_group = new Group();

    root.addChild( new glEnableNode(GL.GL_BLEND) );
    root.addChild( points_group );
    root.addChild( plane_group );
    root.addChild( new glDisableNode(GL.GL_BLEND) );

//  jogl_panel = new JoglPanel( root, true, JoglPanel.DEBUG_MODE );
    jogl_panel = new JoglPanel( root, true, JoglPanel.NORMAL_MODE );

    Camera camera = jogl_panel.getCamera();
    camera.setVUV( new Vector3D(   0, 0,  1 ) );
    camera.setVRP( new Vector3D(  -5, 0,  0 ) );
    camera.setCOP( new Vector3D( -15, 0, 10 ) );

    jogl_panel.setBackgroundColor( Color.GRAY );
    new MouseArcBall( jogl_panel );

    JFrame frame = new JFrame( title );
    frame.setSize(750,750);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( jogl_panel.getDisplayComponent() );
    frame.setVisible( true );

    colors = IndexColorMaker.getColorTable( "Heat 1", 128 );
    color_tran = new int[ colors.length ];
    for ( int i = 0; i < color_tran.length; i++ )
      color_tran[i] = i;

    make_controls();

    make_image_panel();
  }


  /**
   * Add the specified event list in 3D to the collection of points to
   * be drawn.
   * 
   * @param events       The event list to display. 
   * @param assign_codes Boolean indicating whether to assign new codes
   *                     based on the current histogram.  If this is
   *                     true, the new codes will be assigned whether or
   *                     not the eventlist currently has codes.  If this
   *                     is false, new codes will be assigned only if the
   *                     current codes are null.
   */
  public void add_events( IEventList3D events, 
                          boolean      assign_codes,
                          float        phi,
                          float        chi,
                          float        omega )
  {
    Group current_tran = points_group;
                                                  // rotations in IPNS coords
    if ( phi != 0 )                               // unrotate phi about z-axis
    {
      float phi_radians = (float)(phi * Math.PI / 180);
      Rotate phi_rot = new Rotate( -phi_radians, new Vector3D( 0, 0, 1 ) );
      current_tran.addChild( phi_rot );
      current_tran = phi_rot;
    }

    if ( chi != 0 )                               // unrotate chi about x-axis
    {
      float chi_radians = (float)(chi * Math.PI / 180);
      Rotate chi_rot = new Rotate( -chi_radians, new Vector3D( 1, 0, 0 ) );
      current_tran.addChild( chi_rot );
      current_tran = chi_rot;
    }

    if ( omega != 0 )                          // unrotate omega about x-axis
    {
      float omega_radians = (float)(omega * Math.PI / 180);
      Rotate omega_rot = new Rotate( -omega_radians, new Vector3D( 1, 0, 0 ) );
      current_tran.addChild( omega_rot );
      current_tran = omega_rot;
    }

    int n_events = events.numEntries();
    int[] codes = events.eventCodes();
    if ( codes == null || assign_codes )
    {
      AssignCodes( events );
      codes = events.eventCodes();
    }
    float[] xyz_vals = events.eventVals();
/* 
    System.out.println("FIRST 10 POINTS IN SlicedEventsViewer.add_events");
    for ( int i = 0; i < 10; i++ )
      System.out.printf(" %5.1f  %5.1f  %5.1f  %4d\n", 
                         vals[3*i], vals[3*i+1], vals[3*i+2], codes[i] );
*/
    int size = 1;
    SimpleShape shape = new MultiColoredPointList_2( xyz_vals,
                                                     codes,
                                              (float)histogram.minVal(),
                                              (float)histogram.maxVal(),
                                                     color_tran,
                                                     colors,
                                                     size,
                                                     1.0f );
    current_tran.addChild( shape );
  }


  public void update_slice()
  {
    int slice_num = frame_control.getFrameNumber();
    set_slice_plane( slice_num );
    show_image_slice( slice_num );
    jogl_panel.Draw();
  }


  public void ClearEvents()
  {
    points_group.Clear();
  }


  public void setHistogram( Histogram3D histogram )
  {
    this.histogram = histogram;
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


  public void updateDisplay()
  {
    applyDrawingOptions();
    applyColorOptions();
    update_slice();
    jogl_panel.Draw();
  }


  private void AssignCodes( IEventList3D events )
  {
    long start = System.nanoTime();

    int n_events = events.numEntries();

    int[] codes = new int[n_events];

    float  eventX,
           eventY,
           eventZ;

    for ( int i = 0; i < n_events; i++ )
    {
      eventX   = (float)events.eventX( i );
      eventY   = (float)events.eventY( i );
      eventZ   = (float)events.eventZ( i );
      codes[i] = (int)histogram.valueAt( eventX, eventY, eventZ );
    }

    events.setEventCodes( codes );

    System.out.printf("Time to assign codes = %5.1f ms\n",
                       (System.nanoTime() - start)/1.0e6 );
  }


  private void set_slice_plane( int slice_num )
  {
    if ( histogram == null )
      return;

    IProjectionBinner3D x_binner = histogram.xEdgeBinner();
    IProjectionBinner3D y_binner = histogram.yEdgeBinner();
    IProjectionBinner3D z_binner = histogram.zEdgeBinner();

                                                    // calculate corner point
    Vector3D ll_corner = x_binner.minVec(0);
    ll_corner.add( y_binner.minVec(0) );
    ll_corner.add( z_binner.centerVec(slice_num) );
                                                    // calculate base vector
    int last_x_index = x_binner.numBins() - 1;
    Vector3D base = x_binner.maxVec( last_x_index );
    base.subtract( x_binner.minVec(0) );
                                                   // calculate up vector
    int last_y_index = y_binner.numBins() - 1;
    Vector3D up = y_binner.maxVec( last_y_index );
    up.subtract( y_binner.minVec(0) );

    float[][] slice = histogram.pageSlice( slice_num );

    TextureMappedPlane plane = new TextureMappedPlane( slice, 
                                                       (int)histogram.maxVal(),
                                                       color_tran,
                                                       colors,
                                                       ll_corner, 
                                                       base, 
                                                       up, 
                                                       alpha );
    plane_group.Clear();
    plane_group.addChild( plane );
  }


  private void make_image_panel( )
  {
    JFrame f = new JFrame("Slice in Reciprocal Space");
    f.setBounds(760,0,500,500);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    panel = new ImageJPanel();

    panel.setNamedColorModel( IndexColorMaker.HEATED_OBJECT_SCALE_2,
                              true,
                              true );
    f.getContentPane().add(panel);
    f.setVisible(true);
    panel.changeLogScale(50,true);
  }


  private void show_image_slice( int slice_num )
  {
    if ( histogram == null )
      return;

    float[][] slice = histogram.pageSlice( slice_num );
    float[][] flipped_slice = new float[slice.length][slice[0].length];
    for ( int k = 0; k < slice.length; k++ )
      flipped_slice[k] = slice[slice.length-k-1];
    panel.setData( flipped_slice, true );
  }


  private void make_controls()
  {
    JFrame f = new JFrame("Display Options");
    f.setBounds(750,500,400,650);
    f.getContentPane().setLayout( new GridLayout(1,1) );

    Box box = new Box( BoxLayout.Y_AXIS );
    f.getContentPane().add( box );

    make_color_control();
    make_frame_control();
    filter_on_max = new JCheckBox("Omit events above max?", false);
    filter_on_min = new JCheckBox("Omit events below min?", true);
    alpha_blend   = new JCheckBox("Blend events", false);
    orthographic  = new JCheckBox("Orthographic Projection", false);
    JButton  apply_button  = new JButton("Apply");

    box.add( frame_control );
    box.add( filter_on_max );
    box.add( filter_on_min );
    box.add( alpha_blend );
    box.add( orthographic );
    box.add( color_control );

    applyDrawingOptions();
    applyColorOptions();  // NOTE: There will probably NOT be any multi-
                          //       colored point lists yet, but this will
                          //       get the colors and color_index tables set up

    f.setVisible( true );
  }


  private void applyDrawingOptions()
  {
    boolean max_filter_on = filter_on_max.isSelected();

    Vector<MultiColoredPointList_2> nodes = getColoredPointLists();
    for ( int i = 0; i < nodes.size(); i++ )
      nodes.elementAt(i).setDrawOptions( filter_on_max.isSelected(),
                                         filter_on_min.isSelected(),
                                         alpha_blend.isSelected() );

    setOrthographicView( orthographic.isSelected() );
  }


  private void applyColorOptions()
  {
    ColorScaleInfo color_info  =
                            (ColorScaleInfo)color_control.getControlValue();

    String         scale_name  = color_info.getColorScaleName();
    int            num_colors  = color_info.getNumColors();
    byte[]         color_table = color_info.getColorIndexTable();
    float          min         = color_info.getTableMin();
    float          max         = color_info.getTableMax();

    colors = IndexColorMaker.getColorTable( scale_name, num_colors );
    int[] color_index = new int[color_table.length];
    for ( int i = 0; i < color_table.length; i++ )
      color_index[i] = color_table[i];

    Vector<MultiColoredPointList_2> nodes = getColoredPointLists();
    for ( int i = 0; i < nodes.size(); i++ )
      nodes.elementAt(i).setColorInfo( min, max, color_index, colors );
  }


  private void make_frame_control()
  {
    frame_control  = new FrameController();

    IProjectionBinner3D page_binner = histogram.zBinner();
    int num_pages = page_binner.numBins();
    float values[] = new float[num_pages];
    for ( int i = 0; i < values.length; i++ )
      values[i] = (float)page_binner.centerVal(i);

    frame_control.setFrame_values( values );
    frame_control.setStep_time( 20 );
    frame_control.setFrameNumber( num_pages/2 );
    frame_control.addActionListener( new FrameListener() );
  }


  public class FrameListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      int slice_num = frame_control.getFrameNumber();
      set_slice_plane( slice_num );
      show_image_slice( slice_num );
      jogl_panel.Draw();
    }
  }


  private void make_color_control()
  {
    float min = (float)histogram.minVal();
    float max = (float)histogram.maxVal();
    if ( min <= 0 )
      min = 1;
    color_control = new ColorEditPanel( min, max );
    color_control.setLogScale(true);

    color_control.addActionListener( new ColorListener() );
  }


  public class ColorListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      applyDrawingOptions();

      applyColorOptions();
 
      jogl_panel.Draw(); 
    }
  }


  /**
   *  Get all of the multicolored point lists that are displayed by
   *  this viewer.
   */
  private Vector<MultiColoredPointList_2> getColoredPointLists()
  {
      Vector<MultiColoredPointList_2> nodes = 
                                      new Vector<MultiColoredPointList_2>(); 
      Node                    node;
      MultiColoredPointList_2 point_list_node;
      for ( int i = 0; i < points_group.numChildren(); i++ )
      {
         node = points_group.getChild( i );
         while ( !(node instanceof MultiColoredPointList_2) )
           node = ((Group)node).getChild(0);

         point_list_node = (MultiColoredPointList_2)node;
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

    SlicedEventsViewer my_viewer = new SlicedEventsViewer( histogram,
                                                           args[0] );

    elapsed = System.nanoTime() - start_time;
    System.out.println("Time(ms) to add events to histogram = " + elapsed/1.e6);

    my_viewer.add_events( events, true, 0, 0, 0 );

    my_viewer.updateDisplay();
  }

}

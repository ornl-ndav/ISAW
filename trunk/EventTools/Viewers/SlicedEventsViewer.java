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
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.media.opengl.*;
import gov.anl.ipns.MathTools.Geometry.*;
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
import SSG_Tools.SSG_Nodes.Group;
import SSG_Tools.Cameras.Camera;
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
  private Color[]         colors;
  private int[]           color_tran;
  private float           alpha = 0.55f;
  private FrameController frame_control;
  private JoglPanel       jogl_panel;

  /**
   * Show the specified event list in 3D using GL_POINTS, with point
   * color and size set based on the bin where the event occurs in the 
   * specified histogram, and the specified event binner.
   * 
   * @param events    The event list to display
   * @param histogram The histogram use to assign color and size to events
   * @param binner    The binner used with the histogram value to assign
   *                  color and size to events.  
   */
  public void show_events( IEventList3D events, 
                           Histogram3D  histogram,
                           IEventBinner binner     )
  {
    this.histogram = histogram;
    int n_events = events.numEntries();

    float[][] event_array = new float[3][n_events];
    int[]     codes       = new int[n_events];
    colors = IndexColorMaker.getColorTable( "Heat 1", 128 );

    Group group = new Group();
    group.addChild( new glDisableNode(GL.GL_LIGHTING) );

    float  eventX,
           eventY,
           eventZ;
    int eventCode  = 0;
    int eventCount = 0;
    int threshold = 12;
    for ( int i = 0; i < n_events; i++ )
    {
      eventX = (float)events.eventX( i );
      eventY = (float)events.eventY( i );
      eventZ = (float)events.eventZ( i );
      eventCode = (int)histogram.valueAt( eventX, eventY, eventZ );
      if ( eventCode > threshold )
      {
        event_array[0][eventCount] = (float)eventX;
        event_array[1][eventCount] = (float)eventY;
        event_array[2][eventCount] = (float)eventZ;
        if ( eventCode < colors.length )
          codes[eventCount] = eventCode;
        else
          codes[eventCount] = colors.length - 1; 
        eventCount++;
      }
    }

    float[] eventXs = new float[eventCount];
    float[] eventYs = new float[eventCount];
    float[] eventZs = new float[eventCount];

    System.arraycopy( event_array[0], 0, eventXs, 0, eventCount );
    System.arraycopy( event_array[1], 0, eventYs, 0, eventCount );
    System.arraycopy( event_array[2], 0, eventZs, 0, eventCount );

    int size = 1;
    SimpleShape shape = new MultiColoredPointList_2( eventXs,
                                                   eventYs,
                                                   eventZs,
                                                   codes,
                                                   colors,
                                                   size,
                                                   1.0f );

    Vector3D xmin = new Vector3D( -25,  0,  0 );
    Vector3D xmax = new Vector3D(  25,  0,  0 );
    Vector3D ymin = new Vector3D(  0, -25,  0 );
    Vector3D ymax = new Vector3D(  0,  25,  0 );
    Vector3D zmin = new Vector3D(  0,  0, -25 );
    Vector3D zmax = new Vector3D(  0,  0,  25 );

    Axis x_axis = Axis.getInstance( xmin, xmax, "X-Axis", Color.RED );
    Axis y_axis = Axis.getInstance( ymin, ymax, "Y-Axis", Color.GREEN );
    Axis z_axis = Axis.getInstance( zmin, zmax, "Z-Axis", Color.BLUE );

    group.addChild( x_axis );
    group.addChild( y_axis );
    group.addChild( z_axis );

    color_tran = new int[ colors.length ];
    for ( int i = 0; i < color_tran.length; i++ )
      color_tran[i] = i;

    plane_group = new Group();

    group.addChild( new glEnableNode(GL.GL_BLEND) );
    group.addChild( shape );
    group.addChild( plane_group );
    group.addChild( new glDisableNode(GL.GL_BLEND) );

    jogl_panel = new JoglPanel( group, true, JoglPanel.DEBUG_MODE );
    Camera camera = jogl_panel.getCamera();
//    camera = new OrthographicCamera( camera );
//    jogl_panel.setCamera( camera );
    camera.setVRP( new Vector3D( -5, 5, 0 ) ); 

    jogl_panel.setBackgroundColor( Color.GRAY );
    new MouseArcBall( jogl_panel );

    JFrame frame = new JFrame( "ARCS_419 Events" );
    frame.setSize(750,750);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( jogl_panel.getDisplayComponent() );
    frame.setVisible( true );

    make_image_panel( histogram );

    set_slice_plane( histogram, 255 );
    show_image_slice( histogram, 255 );
  }


  private void set_slice_plane( Histogram3D histogram, int slice_num )
  {
    IProjectionBinner3D x_binner = histogram.xBinner();
    IProjectionBinner3D y_binner = histogram.yBinner();
    IProjectionBinner3D z_binner = histogram.zBinner();

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
    jogl_panel.Draw();
  }


  private void make_image_panel( Histogram3D histogram )
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


  private void show_image_slice( Histogram3D histogram, int slice_num )
  {
    float[][] slice = histogram.pageSlice( slice_num );
    float[][] flipped_slice = new float[slice.length][slice[0].length];
    for ( int k = 0; k < slice.length; k++ )
      flipped_slice[k] = slice[slice.length-k-1];
    panel.setData( flipped_slice, true );
  }


  public void make_frame_control()
  {
    JFrame f = new JFrame("Slice Selector");
    f.setBounds(750,500,200,150);
    frame_control  = new FrameController();

    f.getContentPane().setLayout( new GridLayout(1,1) );
    f.getContentPane().add(frame_control);

    IProjectionBinner3D page_binner = histogram.zBinner();
    int num_pages = page_binner.numBins();
    float values[] = new float[num_pages];
    for ( int i = 0; i < values.length; i++ )
      values[i] = (float)page_binner.centerVal(i);

    frame_control.setFrame_values( values );
    frame_control.setStep_time( 50 );
    frame_control.setFrameNumber( num_pages/2 );
    frame_control.addActionListener( new FrameListener() );

    f.setVisible(true);
  }

  public class FrameListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      int slice_num = frame_control.getFrameNumber();
      set_slice_plane( histogram, slice_num );
      show_image_slice( histogram, slice_num );
    }
  }


  public static void main( String args[] ) throws IOException
  {
    SlicedEventsViewer my_viewer = new SlicedEventsViewer(); 

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
    IEventBinner y_bin1D = new UniformEventBinner(      0, 25.6f, NUM_BINS );
    IEventBinner z_bin1D = new UniformEventBinner( -12.8f, 12.8f, NUM_BINS );

    IProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, xVec);
    IProjectionBinner3D y_binner = new ProjectionBinner3D(y_bin1D, yVec);
    IProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, zVec);

    Histogram3D histogram = new Histogram3D(x_binner, y_binner, z_binner);
    my_viewer.histogram = histogram;
    elapsed = System.nanoTime() - start_time;
    System.out.println("Time(ms) to allocate histogram = " + elapsed/1.e6);

    start_time = System.nanoTime();
    histogram.addEvents( events );
    elapsed = System.nanoTime() - start_time;
    System.out.println("Time(ms) to add events to histogram = " + elapsed/1.e6);

    float min  =  25;
    float max  =  1000;
    int   bins =  20;
    UniformEventBinner binner = new UniformEventBinner( min, max, bins );
    my_viewer.show_events( events, histogram, binner );

    my_viewer.make_frame_control();

  }

}

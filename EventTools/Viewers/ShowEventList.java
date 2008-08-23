/* 
 * File: ShowEventList.java
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
 *  $Author: eu7 $
 *  $Date: 2008-04-08 15:50:23 -0500 (Tue, 08 Apr 2008) $            
 *  $Revision: 19029 $
 */

package EventTools.Viewers;

import java.io.*;
import java.awt.*;

import javax.swing.*;
import javax.media.opengl.*;
import gov.anl.ipns.MathTools.Geometry.*;
import EventTools.EventList.IEventList3D;
import EventTools.EventList.ByteFile16EventList3D;
import EventTools.Histogram.Histogram3D;
import EventTools.Histogram.ProjectionBinner3D;
import EventTools.Histogram.IEventBinner;

//import SSG_Tools.Cameras.OrthographicCamera;

import SSG_Tools.SSG_Nodes.StateControls.glEnableNode;
import SSG_Tools.SSG_Nodes.StateControls.glDisableNode;
import SSG_Tools.SSG_Nodes.SimpleShapes.*;
import SSG_Tools.SSG_Nodes.Group;
import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;

import gov.anl.ipns.ViewTools.Panels.Image.*;

/**
 * Prototype to test the display of a 3D event list using GL_POINTS.
 * This can provide a very fast and useful display of a large number of
 * events.
 */

public class ShowEventList
{
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
  public static void show_events( IEventList3D events, 
                                  Histogram3D  histogram,
                                  IEventBinner binner     )
  {
    int n_events = events.numEntries();

    float[][] event_array = new float[3][n_events];
    int[]     codes       = new int[n_events];
    Color[] colors = IndexColorMaker.getColorTable( "Heat 1", 128 );

    Group group = new Group();
    group.addChild( new glDisableNode(GL.GL_LIGHTING) );

    float  eventX,
           eventY,
           eventZ;
    int eventCode  = 0;
    int eventCount = 0;
    for ( int i = 0; i < n_events; i++ )
    {
      eventX = (float)events.eventX( i );
      eventY = (float)events.eventY( i );
      eventZ = (float)events.eventZ( i );
      eventCode = (int)histogram.valueAt( eventX, eventY, eventZ );
      if ( eventCode > 12 )
      {
        event_array[0][eventCount] = (float)eventX;
        event_array[1][eventCount] = (float)eventY;
        event_array[2][eventCount] = (float)eventZ;
//      eventCode = 8 * eventCode;
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
    SimpleShape shape = new MultiColoredPointList( eventXs,
                                                   eventYs,
                                                   eventZs,
                                                   codes,
                                                   colors,
                                                   size,
                                                   1.0f );
    group.addChild( shape );
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

    int slice_num = 255;
    float[][] slice = histogram.pageSlice( slice_num );

    ProjectionBinner3D x_binner = histogram.xBinner();
    ProjectionBinner3D y_binner = histogram.yBinner();
    ProjectionBinner3D z_binner = histogram.zBinner();

    System.out.println("x_binner, min = " + x_binner.axisMin() +
                               ", max = " + x_binner.axisMax()  );

    System.out.println("y_binner, min = " + y_binner.axisMin() +
                               ", max = " + y_binner.axisMax()  );

    System.out.println("z_binner, min = " + z_binner.axisMin() +
                               ", max = " + z_binner.axisMax()  );

                                                    // calculate corner point
    Vector3D ll_corner = x_binner.minVec(0);
    ll_corner.add( y_binner.minVec(0) );
    ll_corner.add( z_binner.centerVec(slice_num) );
                                                    // calculate base vector
    int last_x_index = x_binner.numBins() - 1;
    Vector3D base = x_binner.maxVec( last_x_index );
    base.subtract( x_binner.minVec(0) );
    System.out.println("base vec = " + base + ", length = " + base.length());
                                                   // calculate up vector
    int last_y_index = y_binner.numBins() - 1;
    Vector3D up = y_binner.maxVec( last_y_index );
    up.subtract( y_binner.minVec(0) );
    System.out.println("up vec = " + up + ", length = " + up.length());

    float alpha = 0.5f;
    int[] color_tran = new int[ colors.length ];
    for ( int i = 0; i < color_tran.length; i++ )
      color_tran[i] = i;

    TextureMappedPlane plane = new TextureMappedPlane( slice, 
                                                       (int)histogram.maxVal(),
                                                       color_tran,
                                                       colors,
                                                       ll_corner, 
                                                       base, 
                                                       up, 
                                                       alpha );

    Group plane_group = new Group();

    group.addChild( new glEnableNode(GL.GL_BLEND) );
    group.addChild( plane_group );
      plane_group.addChild( plane );
    group.addChild( new glDisableNode(GL.GL_BLEND) );

    JoglPanel demo = new JoglPanel( group, true, JoglPanel.DEBUG_MODE );
    demo.setBackgroundColor( Color.GRAY );
    new MouseArcBall( demo );

    JFrame frame = new JFrame( "Thresholded Histogram" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );

    JFrame f = new JFrame("Test for ImageJPanel");
    f.setBounds(0,0,500,500);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ImageJPanel panel = new ImageJPanel();
    panel.setData( histogram.pageSlice(slice_num), true );
    panel.setNamedColorModel( IndexColorMaker.HEATED_OBJECT_SCALE_2,
                              true,
                              true );
    f.getContentPane().add(panel);
    f.setVisible(true);
    panel.changeLogScale(50,true);

    float[][] flipped_slice = new float[y_binner.numBins()][x_binner.numBins()];
    for ( int i = 0; i < z_binner.numBins(); i++ )
    {
       ll_corner = x_binner.minVec(0);
       ll_corner.add( y_binner.minVec(0) );
       ll_corner.add( z_binner.centerVec(i) );

       slice = histogram.pageSlice( i );
       plane = new TextureMappedPlane( slice,
                                       (int)histogram.maxVal(),
                                        color_tran,
                                        colors,
                                        ll_corner,
                                        base,
                                        up,
                                        alpha );
       plane_group.Clear();
       plane_group.addChild(plane);
       demo.Draw();

       for ( int k = 0; k < slice.length; k++ )
         flipped_slice[k] = slice[y_binner.numBins()-k-1];

       panel.setData( flipped_slice, true );

       try
       {
         Thread.sleep( 50 );
       }
       catch ( Exception ex )
       {
         System.out.println("Exception Sleeping " + ex);
       }
    }

    ll_corner = x_binner.minVec(0);
    ll_corner.add( y_binner.minVec(0) );
    ll_corner.add( z_binner.centerVec(slice_num) );

    slice = histogram.pageSlice( slice_num );
    plane = new TextureMappedPlane( slice,
                                    (int)histogram.maxVal(),
                                     color_tran,
                                     colors,
                                     ll_corner,
                                     base,
                                     up,
                                     alpha );
    plane_group.Clear();
    plane_group.addChild(plane);
    demo.Draw();

  }


  /**
   * Show the specified event list in 3D using GL_POINTS
   * 
   * @param events   The event list to display
   */
  public static void show_events( IEventList3D events )
  {
    int n_events = events.numEntries();

    float[][] event_array = new float[4][n_events];

    Color[] colors = IndexColorMaker.getColorTable( "Heat 1", 128 );

    Group group = new Group();
    group.addChild( new glDisableNode(GL.GL_LIGHTING) );

    float eventX,
          eventY,
          eventZ;
    int eventCount = 0;
    for ( int i = 0; i < n_events; i++ )
    {
      eventX = (float)events.eventX( i );
      eventY = (float)events.eventY( i );
      eventZ = (float)events.eventZ( i );
      if ( eventX < -3    && eventX > -4.5 &&
           eventY >  2.25    && eventY <  3.75 &&
           eventZ > -0.75 && eventZ <  0.75 )
      {
        event_array[0][eventCount] = eventX;
        event_array[1][eventCount] = eventY;
        event_array[2][eventCount] = eventZ;
        event_array[3][eventCount] = (float)events.eventCode( i );
        eventCount++;
      }
    }
    System.out.println("Event Count in subregion = " + eventCount );

    float[] eventXs = new float[eventCount]; 
    float[] eventYs = new float[eventCount]; 
    float[] eventZs = new float[eventCount]; 
    float[] codes   = new float[eventCount]; 

    System.arraycopy( event_array[0], 0, eventXs, 0, eventCount );
    System.arraycopy( event_array[1], 0, eventYs, 0, eventCount );
    System.arraycopy( event_array[2], 0, eventZs, 0, eventCount );
    System.arraycopy( event_array[3], 0, codes,   0, eventCount );

    int size = 1;
    SimpleShape shape = new PointList( eventXs, 
                                       eventYs, 
                                       eventZs, 
                                       size, 
                                       colors[65], 
                                       1.0f );
    group.addChild( shape );

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

    JoglPanel demo = new JoglPanel( group, true );
    demo.setBackgroundColor( Color.GRAY );
//    demo.setCamera( new OrthographicCamera( demo.getCamera() ) );
    new MouseArcBall( demo );

    JFrame frame = new JFrame( "Thresholded Histogram" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

  public static void main( String args[] ) throws IOException
  {
    ByteFile16EventList3D events = new ByteFile16EventList3D(args[0]);
    show_events( events );
  }

}

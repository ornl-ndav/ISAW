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
import EventTools.Histogram.IEventBinner;

//import SSG_Tools.Cameras.OrthographicCamera;
import SSG_Tools.Appearance.Appearance;
import SSG_Tools.Appearance.TransparentMaterial;
import SSG_Tools.Appearance.Textures.Texture2D;
import SSG_Tools.Geometry.Geometry;
import SSG_Tools.Geometry.PlaneGeometry;
import SSG_Tools.SSG_Nodes.Shapes.GenericShape;
import SSG_Tools.SSG_Nodes.StateControls.glEnableNode;
import SSG_Tools.SSG_Nodes.StateControls.glDisableNode;
import SSG_Tools.SSG_Nodes.StateControls.glFrontFaceNode;
import SSG_Tools.SSG_Nodes.SimpleShapes.*;
import SSG_Tools.SSG_Nodes.Group;
import SSG_Tools.Utils.LoadTexture;
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
      if ( eventCode > 2 )
      {
        event_array[0][eventCount] = (float)eventX;
        event_array[1][eventCount] = (float)eventY;
        event_array[2][eventCount] = (float)eventZ;
        eventCode = 8*eventCode;
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

    Geometry plane_geometry = new PlaneGeometry( 10, 10, 20, 20 );
    GenericShape plane = new GenericShape( plane_geometry, null );

    Appearance plane_appearance = new Appearance(); 
    String directory = System.getProperty( "user.home" ) +
                       "/SSG_Data/TextureImages/OpenInventor/";
    String name = "Marble01.jpg";
    int n_rows = 512;
    int n_cols = 512;
    byte image[] = LoadTexture.LoadImage( directory + name, n_rows, n_cols );
    Texture2D texture = new Texture2D( image, n_rows, n_cols );

    TransparentMaterial material = new TransparentMaterial();
    material.setColor( Color.WHITE );
    material.setAlpha(0.25f);

    plane_appearance.setMaterial( material );
    plane_appearance.setTexture( texture );
    plane.setAppearance( plane_appearance );

    group.addChild( new glEnableNode(GL.GL_BLEND) );
    group.addChild(plane);
    group.addChild( new glDisableNode(GL.GL_BLEND) );

    JoglPanel demo = new JoglPanel( group, true, JoglPanel.DEBUG_MODE );
    demo.setBackgroundColor( Color.GRAY );
    new MouseArcBall( demo );

    JFrame frame = new JFrame( "Thresholded Histogram" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
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

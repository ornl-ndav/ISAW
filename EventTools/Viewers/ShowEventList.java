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

import gov.anl.ipns.MathTools.Geometry.*;
import EventTools.EventList.IEventList3D;
import EventTools.EventList.ByteFile16EventList3D;

//import SSG_Tools.Cameras.OrthographicCamera;
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

    for ( int i = 0; i < n_events; i++ )
    {
      event_array[0][i] = (float)events.eventX( i );
      event_array[1][i] = (float)events.eventY( i );
      event_array[2][i] = (float)events.eventZ( i );
      event_array[3][i] = (float)events.eventCode( i );
    }

    int size = 1;
    SimpleShape shape = new PointList( event_array[0], 
                                       event_array[1], 
                                       event_array[2], 
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

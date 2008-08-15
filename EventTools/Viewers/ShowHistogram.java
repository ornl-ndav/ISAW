/* 
 * File: ShowHistogram.java
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

import java.awt.*;
import java.util.Vector;

import javax.swing.*;

import gov.anl.ipns.MathTools.Geometry.*;
import EventTools.EventList.FloatArrayEventList3D;
import EventTools.Histogram.Histogram3D;
import EventTools.Histogram.IEventBinner;
//import SSG_Tools.Cameras.OrthographicCamera;
import SSG_Tools.SSG_Nodes.SimpleShapes.*;
import SSG_Tools.SSG_Nodes.Group;
import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;


import gov.anl.ipns.ViewTools.Panels.Image.*;

/**
 * Crude prototype to test the display of a 3D histogram using GL_POINTS.
 * This can provide a very fast and useful display of a large number of
 * events.
 */

public class ShowHistogram 
{
  /**
   * Show the specified histogram in 3D using GL_POINTS
   * 
   * @param hist_3D  The histogram to display
   * @param binner   IEventBinner specifying the levels of bins from
   *                 the histogram that should be displayed.
   */
  public static void show_histogram( Histogram3D hist_3D, 
                                     IEventBinner binner  )
  {
    int n_bins = binner.numBins();
    int shift  = n_bins/3;
    int size = 1;
    Color[] colors = IndexColorMaker.getColorTable( "Heat 1", n_bins+shift );

    Vector all_lists = hist_3D.getEventLists( binner );

    Group group = new Group();

    SimpleShape shape;
    FloatArrayEventList3D events;
    float[][] pts;
    for ( int i = 0; i < all_lists.size(); i++ )
    {
      Vector lists = (Vector)all_lists.elementAt(i);
      for ( int k = 0; k < lists.size(); k++ )
      {
        if ( lists.elementAt(k) != null )
        {
          events = (FloatArrayEventList3D)(lists.elementAt(k));
          pts = events.getEventArrays();
//        size = (6*k)/lists.size() + 2;
          size = k/2 + 2;
          shape = new PointList( pts[0], pts[1], pts[2], 
                                 size, colors[k+shift], 1.0f );
          group.addChild( shape );
        }
      }
    }

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

}

/*
 * File:  LineStrip.java
 *
 * Copyright (C) 2005 Dennis Mikkelson
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
 * Modified:
 *
 *  $Log: LineStrip.java,v $
 *  Revision 1.2  2006/07/04 00:40:47  dennis
 *  Replaced call to deprecated method JFrame.show(), with call
 *  to setVisible(true).
 *
 *  Revision 1.1  2005/07/25 15:46:02  dennis
 *  Initial version of simple shape.
 *
 *
 */
package SSG_Tools.SSG_Nodes.SimpleShapes;

import java.awt.*;
import javax.swing.*;
import net.java.games.jogl.*;

import gov.anl.ipns.MathTools.Geometry.*;
import SSG_Tools.Viewers.*;

import SSG_Tools.Cameras.*;

/**
 *  This Node draws a colored segmented line through the specified 
 *  vertices, in the order specified.
 */

public class LineStrip extends SimpleShape 
{
  Vector3D vertices[];

  /* --------------------------- Constructor --------------------------- */
  /**
   *  Construct a LineStrip through the specified points.
   *
   *  @param  new_color    The color of the parallelogram.
   *
   */
  public LineStrip( Vector3D verts[], 
                    Color    new_color )
  {
    super(new_color);
    vertices = new Vector3D[ verts.length ];
    for ( int i = 0; i < verts.length; i++ )
      vertices[i] = verts[i];
  }


  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this LineStrip to the specified drawable.  
   *
   *  @param  drawable  The drawable on which the Parallelogram is to be drawn.
   */
  public void Render( GLDrawable drawable )
  {
    GL gl = drawable.getGL();

    super.preRender( drawable );
    gl.glBegin( GL.GL_LINE_STRIP );
      gl.glColor3fv( color );
      for ( int i = 0; i < vertices.length; i++ )
        gl.glVertex3fv( vertices[i].get() );
    gl.glEnd();
    super.postRender( drawable );
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that constructs an instance of Parallelogram and displays 
   *  it in 3D for testing purposes.  
   */
  public static void main( String args[] )
  {
    Vector3D verts[] = new Vector3D[4];
    verts[0] = new Vector3D( 0, 0, 0 );
    verts[1] = new Vector3D( 1, 0, 0 );
    verts[2] = new Vector3D( 1, 1, 0 );
    verts[3] = new Vector3D( 1, 1, 1 );
    LineStrip line_strip = new LineStrip( verts, Color.GREEN );

    JoglPanel demo = new JoglPanel( line_strip );
    demo.enableHeadlight( true );

    Camera camera = demo.getCamera();
    camera.setVRP( new Vector3D( 0,0, 0 ) );
    camera.setCOP( new Vector3D( 0, 0, 4 ) );
    camera.SetViewVolume( 1, 10, 40 );
    demo.setCamera( camera );
  
    JFrame frame = new JFrame( "Sphere Scene" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible(true);
  }

}

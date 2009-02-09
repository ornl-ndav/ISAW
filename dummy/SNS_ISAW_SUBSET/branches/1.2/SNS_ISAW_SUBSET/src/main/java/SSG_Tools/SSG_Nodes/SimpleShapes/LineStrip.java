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
 *  Last Modified:
 * 
 *  $Author: eu7 $
 *  $Date: 2008-08-21 15:12:56 -0500 (Thu, 21 Aug 2008) $            
 *  $Revision: 302 $
 *
 *  $Log: LineStrip.java,v $
 *
 *  2008/08/21  Updated to latest version from UW-Stout repository.
 *
 *  Revision 1.6  2007/12/07 05:17:52  dennis
 *  Fixed some minor javadoc errors.
 *
 *  Revision 1.5  2006/11/26 01:43:02  dennis
 *  Changed to allow a null color.  If color is null, the last color
 *  that was set will be used.
 *
 *  Revision 1.4  2006/09/23 05:05:32  dennis
 *  Added MouseArcBall to control the view.
 *
 *  Revision 1.3  2006/08/04 02:16:21  dennis
 *  Updated to work with JSR-231, 1.0 beta 5,
 *  instead of jogl 1.1.1.
 *
 *  Revision 1.2  2006/07/20 19:59:01  dennis
 *  Replaced deprecated method frame.show() with setVisible(true)
 *
 *  Revision 1.1  2005/10/14 04:04:11  dennis
 *  Copied into local CVS repository from CVS repository at IPNS.
 */

package SSG_Tools.SSG_Nodes.SimpleShapes;

import java.awt.*;
import javax.swing.*;
import javax.media.opengl.*;

import gov.anl.ipns.MathTools.Geometry.*;
import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;

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
   *  @param  new_color    The color of the LineStrip.
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
   *  @param  drawable  The drawable on which the LineStrip is to be drawn.
   */
  public void Render( GLAutoDrawable drawable )
  {
    GL gl = drawable.getGL();

    super.preRender( drawable );

    if ( color != null )
      gl.glColor3fv( color, 0 );

    gl.glBegin( GL.GL_LINE_STRIP );
      for ( int i = 0; i < vertices.length; i++ )
        gl.glVertex3fv( vertices[i].get(), 0 );
    gl.glEnd();

    super.postRender( drawable );
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that constructs an instance of the LineStrip and displays 
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
    new MouseArcBall( demo );

    Camera camera = demo.getCamera();
    camera.setVRP( new Vector3D( 0, 0, 0 ) );
    camera.setCOP( new Vector3D( 3, 4, 5 ) );
    camera.SetViewVolume( 1, 10, 40 );
    demo.setCamera( camera );
  
    JFrame frame = new JFrame( "LineStrip" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

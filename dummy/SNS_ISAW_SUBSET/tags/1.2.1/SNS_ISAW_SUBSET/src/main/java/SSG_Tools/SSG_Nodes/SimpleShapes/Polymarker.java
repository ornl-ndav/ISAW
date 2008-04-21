/*
 * File:  Polymarker.java
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
 *  $Log: Polymarker.java,v $
 *  Revision 1.3  2007/08/14 00:03:33  dennis
 *  Major update to JSR231 based version from UW-Stout repository.
 *
 *  Revision 1.5  2006/11/26 01:43:02  dennis
 *  Changed to allow a null color.  If color is null, the last color
 *  that was set will be used.
 *
 *  Revision 1.4  2006/09/23 05:04:41  dennis
 *  Removed the setType() and setSize() methods to make the class
 *  immutable.  Added a MouseArcBall to control the view.
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
 *  This Node draws colored markers of a specified type and size at the 
 *  specified points.
 */

public class Polymarker extends SimpleShape 
{
  public static final int   DOT   = 1;
  public static final int   PLUS  = 2;
  public static final int   STAR  = 3;
  public static final int   BOX   = 4;
  public static final int   CROSS = 5;
  public static final float SIZE_SCALE = 100;

  Vector3D vertices[];
  float    size = 1;
  int      type = 1;


  /* --------------------------- Constructor --------------------------- */
  /**
   *  Construct a Polymarker with marks at the specified points.  The 
   *  default marker type is a dot.
   *
   *  @param  verts        Array of Vector3D objects specifying where the
   *                       markers should be drawn.
   *  @param  size         The size of the marker.  For all markers other 
   *                       than DOT, the size is specified in terms of a 
   *                       percentage of a unit length in world coordinates. 
   *                       For a DOT, the actual size in pixels is specified.
   *                       
   *  @param  type         Type code for the marker to use.  This should be 
   *                       one of the defined marker types such as DOT, PLUS, 
   *                       STAR, etc.
   *  @param  new_color    The color of the parallelogram.
   */
  public Polymarker( Vector3D verts[], 
                     int      size,
                     int      type,
                     Color    new_color )
  {
    super(new_color);

    this.size = size;
    this.type = type;

    vertices = new Vector3D[ verts.length ];

    for ( int i = 0; i < verts.length; i++ )
      vertices[i] = verts[i];
  }


  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this Polymarker to the specified drawable.  
   *
   *  @param  drawable  The drawable on which the Parallelogram is to be drawn.
   */
  public void Render( GLAutoDrawable drawable )
  {
    GL gl = drawable.getGL();

    super.preRender( drawable );

    if ( color != null )
      gl.glColor3fv( color, 0 );

    if ( type == DOT )
      drawDots( gl );
    else if (type == PLUS )
      drawPluses( gl );
    else if (type == STAR )
      drawStars( gl );
    else if (type == BOX )
      drawBoxes( gl );
    else if (type == CROSS )
      drawCrosses( gl );
    else
      drawPluses( gl );

    super.postRender( drawable );
  }


  /* -------------------------------------------------------------------------
   *
   *  Private methods to draw various marker types
   *
   */
 
  private void drawDots( GL gl )
  {
    gl.glPointSize( size );
    gl.glBegin( GL.GL_POINTS );
      for ( int i = 0; i < vertices.length; i++ )
        gl.glVertex3fv( vertices[i].get(), 0 );
    gl.glEnd();
  }


  private void drawPluses( GL gl )
  {
    float delta = size/SIZE_SCALE;
    float center[];
    gl.glBegin( GL.GL_LINES );
      for ( int i = 0; i < vertices.length; i++ )
      {
        center = vertices[i].get(); 
        gl.glVertex3f( center[0] + delta, center[1], center[2] );
        gl.glVertex3f( center[0] - delta, center[1], center[2] );
        gl.glVertex3f( center[0], center[1] + delta, center[2] );
        gl.glVertex3f( center[0], center[1] - delta, center[2] );
        gl.glVertex3f( center[0], center[1], center[2] + delta );
        gl.glVertex3f( center[0], center[1], center[2] - delta );
      }
    gl.glEnd();
  }


  private void drawStars( GL gl )
  {
    float delta_1 = size/SIZE_SCALE;
    float delta_2 = .7f * delta_1;
    float center[];
    gl.glBegin( GL.GL_LINES );
      for ( int i = 0; i < vertices.length; i++ )
      {
        center = vertices[i].get();
        gl.glVertex3f( center[0] + delta_1, center[1], center[2] );
        gl.glVertex3f( center[0] - delta_1, center[1], center[2] );
        gl.glVertex3f( center[0], center[1] + delta_1, center[2] );
        gl.glVertex3f( center[0], center[1] - delta_1, center[2] );
        gl.glVertex3f( center[0], center[1], center[2] + delta_1 );
        gl.glVertex3f( center[0], center[1], center[2] - delta_1 );

        gl.glVertex3f( center[0] + delta_2, center[1] + delta_2, center[2] );
        gl.glVertex3f( center[0] - delta_2, center[1] - delta_2, center[2] );
        gl.glVertex3f( center[0] + delta_2, center[1] - delta_2, center[2] );
        gl.glVertex3f( center[0] - delta_2, center[1] + delta_2, center[2] );

        gl.glVertex3f( center[0], center[1] + delta_2, center[2] + delta_2 );
        gl.glVertex3f( center[0], center[1] - delta_2, center[2] - delta_2 );
        gl.glVertex3f( center[0], center[1] - delta_2, center[2] + delta_2 );
        gl.glVertex3f( center[0], center[1] + delta_2, center[2] - delta_2 );

        gl.glVertex3f( center[0] + delta_2, center[1], center[2] + delta_2 );
        gl.glVertex3f( center[0] - delta_2, center[1], center[2] - delta_2 );
        gl.glVertex3f( center[0] - delta_2, center[1], center[2] + delta_2 );
        gl.glVertex3f( center[0] + delta_2, center[1], center[2] - delta_2 );
      }
    gl.glEnd();
  }


  private void drawBoxes( GL gl )
  {
    float delta = size/SIZE_SCALE;
    float center[];
    for ( int i = 0; i < vertices.length; i++ )
    {
      gl.glBegin( GL.GL_LINE_LOOP );
        center = vertices[i].get();
        gl.glVertex3f( center[0] - delta, center[1] - delta, center[2] + delta);
        gl.glVertex3f( center[0] + delta, center[1] - delta, center[2] + delta);
        gl.glVertex3f( center[0] + delta, center[1] + delta, center[2] + delta);
        gl.glVertex3f( center[0] - delta, center[1] + delta, center[2] + delta);
      gl.glEnd();

      gl.glBegin( GL.GL_LINE_LOOP );
        gl.glVertex3f( center[0] - delta, center[1] - delta, center[2] - delta);
        gl.glVertex3f( center[0] + delta, center[1] - delta, center[2] - delta);
        gl.glVertex3f( center[0] + delta, center[1] + delta, center[2] - delta);
        gl.glVertex3f( center[0] - delta, center[1] + delta, center[2] - delta);
      gl.glEnd();

      gl.glBegin( GL.GL_LINES );
        gl.glVertex3f( center[0] - delta, center[1] - delta, center[2] + delta);
        gl.glVertex3f( center[0] - delta, center[1] - delta, center[2] - delta);

        gl.glVertex3f( center[0] + delta, center[1] - delta, center[2] + delta);
        gl.glVertex3f( center[0] + delta, center[1] - delta, center[2] - delta);

        gl.glVertex3f( center[0] + delta, center[1] + delta, center[2] + delta);
        gl.glVertex3f( center[0] + delta, center[1] + delta, center[2] - delta);

        gl.glVertex3f( center[0] - delta, center[1] + delta, center[2] + delta);
        gl.glVertex3f( center[0] - delta, center[1] + delta, center[2] - delta);
      gl.glEnd();
    }
  }


  private void drawCrosses( GL gl )
  {
    float delta_1 = size/SIZE_SCALE;
    float delta_2 = .7f * delta_1;
    float center[];
    gl.glBegin( GL.GL_LINES );
      for ( int i = 0; i < vertices.length; i++ )
      {
        center = vertices[i].get();
        gl.glVertex3f( center[0] + delta_2, center[1] + delta_2, center[2] );
        gl.glVertex3f( center[0] - delta_2, center[1] - delta_2, center[2] );
        gl.glVertex3f( center[0] + delta_2, center[1] - delta_2, center[2] );
        gl.glVertex3f( center[0] - delta_2, center[1] + delta_2, center[2] );

        gl.glVertex3f( center[0], center[1] + delta_2, center[2] + delta_2 );
        gl.glVertex3f( center[0], center[1] - delta_2, center[2] - delta_2 );
        gl.glVertex3f( center[0], center[1] - delta_2, center[2] + delta_2 );
        gl.glVertex3f( center[0], center[1] + delta_2, center[2] - delta_2 );

        gl.glVertex3f( center[0] + delta_2, center[1], center[2] + delta_2 );
        gl.glVertex3f( center[0] - delta_2, center[1], center[2] - delta_2 );
        gl.glVertex3f( center[0] - delta_2, center[1], center[2] + delta_2 );
        gl.glVertex3f( center[0] + delta_2, center[1], center[2] - delta_2 );
      }
    gl.glEnd();
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that constructs an instance of a Polymarker and displays 
   *  it in 3D for testing purposes.  
   */
  public static void main( String args[] )
  {
    Vector3D verts[] = new Vector3D[4];
    verts[0] = new Vector3D( 0, 0, 0 );
    verts[1] = new Vector3D( 1, 0, 0 );
    verts[2] = new Vector3D( 1, 1, 0 );
    verts[3] = new Vector3D( 1, 1, 1 );

    int size = 5;
    Polymarker markers = new Polymarker( verts, size, BOX, Color.RED );

    JoglPanel demo = new JoglPanel( markers );
    demo.enableHeadlight( true );
    new MouseArcBall( demo );

    Camera camera = demo.getCamera();
    camera.setVRP( new Vector3D( 0, 0, 0 ) );
    camera.setCOP( new Vector3D( 0, 0, 8 ) );
    camera.SetViewVolume( 1, 20, 40 );
    demo.setCamera( camera );
  
    JFrame frame = new JFrame( "Polymarker" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

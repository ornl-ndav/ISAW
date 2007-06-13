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
 *  Revision 1.2  2006/07/04 00:40:47  dennis
 *  Replaced call to deprecated method JFrame.show(), with call
 *  to setVisible(true).
 *
 *  Revision 1.1  2005/07/25 15:46:03  dennis
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
 *  This Node draws colored markers of a specified type and size at the 
 *  specified points.
 */

public class Polymarker extends SimpleShape 
{
  public static final int DOT   = 1;
  public static final int PLUS  = 2;
  public static final int STAR  = 3;
  public static final int BOX   = 4;
  public static final int CROSS = 5;

  Vector3D vertices[];
  float    size = 1;
  int      type = 1;


  /* --------------------------- Constructor --------------------------- */
  /**
   *  Construct a Polymarker of the specified type through the specified points.
   *
   *  @param  new_color    The color of the parallelogram.
   *
   */
  public Polymarker( Vector3D verts[], 
                     Color    new_color )
  {
    super(new_color);
    vertices = new Vector3D[ verts.length ];
    for ( int i = 0; i < verts.length; i++ )
      vertices[i] = verts[i];
  }


  /* ------------------------------- setSize ---------------------------- */
  /**
   *  Set the size of the markers to be drawn.  The sizes are specified in
   *  terms of the number of pixels to move on each side of the pixel at
   *  which the marker is placed.  This affects all of the marker types,
   *  except the DOT marker.  A DOT marker is always just one pixel.
   *
   *  @param size  Adjust the size of the marker.  The actual size in pixels
   *               is 2*size + 1, since this parameter specifies the distance
   *               to draw from the center position.
   */
  public void setSize( int size )
  {
    if ( size < 1 )
      return;

    this.size = size;
  }


  /* ------------------------------- setType ------------------------------ */
  /**  
   *  Specify the type of marker to be placed at each point of this polymarker.
   *
   *  @param  type  Type code for the marker to use.  This should be one of
   *                the defined marker types such as DOT, PLUS, STAR, etc.
   */
  public void setType( int type )
  {
    if ( type < DOT || type > CROSS )
      return;

    this.type = type;
  }


  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this Polymarker to the specified drawable.  
   *
   *  @param  drawable  The drawable on which the Parallelogram is to be drawn.
   */
  public void Render( GLDrawable drawable )
  {
    GL gl = drawable.getGL();

    super.preRender( drawable );

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
   *  Private methods 
   *
   */
 
  private void drawDots( GL gl )
  {
    gl.glPointSize( size );
    gl.glBegin( GL.GL_POINTS );
      gl.glColor3fv( color );
      for ( int i = 0; i < vertices.length; i++ )
        gl.glVertex3fv( vertices[i].get() );
    gl.glEnd();
  }


  private void drawPluses( GL gl )
  {
    float delta = size/100;
    float center[];
    gl.glBegin( GL.GL_LINES );
      gl.glColor3fv( color );
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
    float delta_1 = size/100;
    float delta_2 = .7f * delta_1;
    float center[];
    gl.glBegin( GL.GL_LINES );
      gl.glColor3fv( color );
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
    float delta = size/100;
    float center[];
    for ( int i = 0; i < vertices.length; i++ )
    {
      gl.glColor3fv( color );
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
    float delta_1 = size/100;
    float delta_2 = .7f * delta_1;
    float center[];
    gl.glBegin( GL.GL_LINES );
      gl.glColor3fv( color );
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
    Polymarker markers = new Polymarker( verts, Color.RED );
    markers.setType( BOX );
    markers.setSize( 5 );

    JoglPanel demo = new JoglPanel( markers );
    demo.enableHeadlight( true );

    Camera camera = demo.getCamera();
    camera.setVRP( new Vector3D( 0,0, 0 ) );
    camera.setCOP( new Vector3D( 0, 0, 8 ) );
    camera.SetViewVolume( 1, 20, 40 );
    demo.setCamera( camera );
  
    JFrame frame = new JFrame( "Sphere Scene" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible(true);
  }

}

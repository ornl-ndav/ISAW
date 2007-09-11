/*
 * File:  SolidBox.java
 *
 * Copyright (C) 2004, Dennis Mikkelson
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
 * $Log: SolidBox.java,v $
 *
 */

package SSG_Tools.SSG_Nodes.VerySimpleShapes;

import net.java.games.jogl.*;
import javax.swing.*;

import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;
import SSG_Tools.SSG_Nodes.*;

/** 
 *  This class draws a solid box of the specified width, height and depth
 *  centered at the origin.  NO Texture Coordinates are set.
 */

public class SolidBox extends Node 
{
  float width   =  1;        // The dimensions of this box in x,y,z directions
  float height  =  1;
  float depth   =  1;
  int   list_id = -1;


  /* ------------------------ default constructor ------------------------ */
  /**
   *  Construct a default box object with the width, height and depth all  
   *  equal to 1.
   */
  public SolidBox()
  {
    // Default constructor
  }


  /* --------------------------- constructor --------------------------- */
  /**
   *  Construct a box object with the specified width, height and depth
   *
   *  @param  width    The total width  of the box in the "x" direction.
   *  @param  height   The total height of the box in the "y" direction.
   *  @param  depth    The total depth  of the box in the "z" direction.
   */
  public SolidBox( float width, float height, float depth )
  {
    this.width  = width;
    this.height = height;
    this.depth  = depth;
  }


  /* ---------------------------- getWidth ---------------------------- */
  /**
   *  Get the width of this box
   *
   *  @return the width of the box that was set when the box was constructed
   */
  public float getWidth()
  {
    return width;
  }


  /* ---------------------------- getHeight ---------------------------- */
  /**
   *  Get the height of this box
   *
   *  @return the height of the box that was set when the box was constructed
   */
  public float getHeight()
  {
    return height;
  }


  /* ---------------------------- getDepth ---------------------------- */
  /**
   *  Get the depth of this box
   *
   *  @return the depth of the box that was set when the box was constructed
   */
  public float getDepth()
  {
    return depth;
  }


  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this box to the specified drawable.
   *
   *  @param  drawable  The drawable on which the box is to be drawn.
   */
  public void Render( GLDrawable drawable )
  {
    GL gl = drawable.getGL();

    if ( list_id == -1 )
    {
      list_id = gl.glGenLists(1);
      gl.glNewList( list_id, GL.GL_COMPILE );

      preRender( drawable );                 // handle name stack and appearance

      float x0 = -width/2;
      float y0 = -height/2;
      float z0 = -depth/2;
      float x1 =  width/2;
      float y1 =  height/2;
      float z1 =  depth/2;

      gl.glBegin( GL.GL_QUADS );
        gl.glNormal3f( 0, 0, -1 );          // back face, negative z direction
        gl.glVertex3f( x0, y0, z0 );
        gl.glVertex3f( x1, y0, z0 );
        gl.glVertex3f( x1, y1, z0 );
        gl.glVertex3f( x0, y1, z0 );
 
        gl.glNormal3f( 0, 1, 0 );           // top face, positive y direction
        gl.glVertex3f( x0, y1, z0 );
        gl.glVertex3f( x1, y1, z0 );
        gl.glVertex3f( x1, y1, z1 );
        gl.glVertex3f( x0, y1, z1 );

        gl.glNormal3f( 0, 0, 1 );           // front face, positive z direction
        gl.glVertex3f( x0, y1, z1 );
        gl.glVertex3f( x1, y1, z1 );
        gl.glVertex3f( x1, y0, z1 );
        gl.glVertex3f( x0, y0, z1 );

        gl.glNormal3f( 0, -1, 0 );          // bottom face, negative y direction
        gl.glVertex3f( x0, y0, z1 );
        gl.glVertex3f( x1, y0, z1 );
        gl.glVertex3f( x1, y0, z0 );
        gl.glVertex3f( x0, y0, z0 );

        gl.glNormal3f( 1, 0, 0 );           // right face, positive x direction
        gl.glVertex3f( x1, y0, z0 );
        gl.glVertex3f( x1, y0, z1 );
        gl.glVertex3f( x1, y1, z1 );
        gl.glVertex3f( x1, y1, z0 );

        gl.glNormal3f(  -1, 0, 0 );        // left face, negative x direction
        gl.glVertex3f( x0, y0, z0 );
        gl.glVertex3f( x0, y1, z0 );
        gl.glVertex3f( x0, y1, z1 );
        gl.glVertex3f( x0, y0, z1 );
      gl.glEnd();

      postRender( drawable );              // clean up after drawing
      gl.glEndList();
    }

    gl.glCallList( list_id );

  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
    JoglPanel demo = new JoglPanel( new SolidBox( 2, 2, 4 ) );

    demo.enableLighting( false );
    new MouseArcBall( demo );

    JFrame frame = new JFrame( "Very Simple SolidBox Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

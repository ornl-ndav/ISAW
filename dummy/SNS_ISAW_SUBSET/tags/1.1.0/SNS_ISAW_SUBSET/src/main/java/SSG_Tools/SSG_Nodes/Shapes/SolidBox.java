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
 * Revision 1.5  2006/07/20 15:49:08  dennis
 * Updated from CVS repository at isaw.mscs.uwstout.edu
 * Added texture coordinates to box.
 * Added get methods for width, height and depth of box.
 * Expanded javadoc comments.
 *
 * Revision 1.5  2005/10/30 01:14:39  dennis
 * Added texture coordinates to the box.
 * Added default constructor.
 * Added some additional javadoc comments at the start of the class.
 *
 * Revision 1.4  2005/10/14 18:41:45  dennis
 * Added "getter" methods for the width, depth and height of the box.
 *
 * Revision 1.3  2004/11/15 19:18:35  dennis
 * Added tag for logging.
 *
 */

package SSG_Tools.SSG_Nodes.Shapes;

import net.java.games.jogl.*;
import javax.swing.*;

import SSG_Tools.Viewers.*;

/** 
 *  This class draws a solid box of the specified width, height and depth
 *  centered at the origin.  Texture coordinates are set for each face
 *  individually, covering the unit square, [0,1]x[0,1] once for each face.
 *  For example, the face facing in the positive z direction has texture 
 *  coordinates between 0 and 1 in s and t assigned to that face, (0,0)
 *  at the lower left corner, s increasing in the +x direction and t
 *  increasing in the +y direction.
 *     
 *  NOTE: These explicitly generated texture coordinates will be used
 *  UNLESS Shape.setTexCoordGenMode() has been used to turn on automatic
 *  generation of texture coordinates by OpenGL.  In that case, the
 *  automatically generated coordinates will take precedence over the values
 *  set by glTexCoord2f().
 */

public class SolidBox extends Shape
{
  float width  = 1;        // The dimensions of this box in x,y,z directions
  float height = 1;
  float depth  = 1;


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
    preRender( drawable );                 // handle name stack and appearance

    GL gl = drawable.getGL();

    float x0 = -width/2;
    float y0 = -height/2;
    float z0 = -depth/2;
    float x1 =  width/2;
    float y1 =  height/2;
    float z1 =  depth/2;

    gl.glBegin( GL.GL_QUADS );
      gl.glNormal3f( 0, 0, -1 );          // back face, negative z direction

      gl.glTexCoord2f( 1, 0 );
      gl.glVertex3f( x0, y0, z0 );

      gl.glTexCoord2f( 0, 0 );
      gl.glVertex3f( x1, y0, z0 );

      gl.glTexCoord2f( 0, 1 );
      gl.glVertex3f( x1, y1, z0 );

      gl.glTexCoord2f( 1, 1 );
      gl.glVertex3f( x0, y1, z0 );

      gl.glNormal3f( 0, 1, 0 );           // top face, positive y direction

      gl.glTexCoord2f( 0, 1 );
      gl.glVertex3f( x0, y1, z0 );

      gl.glTexCoord2f( 1, 1 );
      gl.glVertex3f( x1, y1, z0 );

      gl.glTexCoord2f( 1, 0 );
      gl.glVertex3f( x1, y1, z1 );

      gl.glTexCoord2f( 0, 0 );
      gl.glVertex3f( x0, y1, z1 );

      gl.glNormal3f( 0, 0, 1 );           // front face, positive z direction

      gl.glTexCoord2f( 0, 1 );
      gl.glVertex3f( x0, y1, z1 );

      gl.glTexCoord2f( 1, 1 );
      gl.glVertex3f( x1, y1, z1 );

      gl.glTexCoord2f( 1, 0 );
      gl.glVertex3f( x1, y0, z1 );

      gl.glTexCoord2f( 0, 0 );
      gl.glVertex3f( x0, y0, z1 );

      gl.glNormal3f( 0, -1, 0 );          // bottom face, negative y direction

      gl.glTexCoord2f( 0, 1 );
      gl.glVertex3f( x0, y0, z1 );

      gl.glTexCoord2f( 1, 1 );
      gl.glVertex3f( x1, y0, z1 );

      gl.glTexCoord2f( 1, 0 );
      gl.glVertex3f( x1, y0, z0 );

      gl.glTexCoord2f( 0, 0 );
      gl.glVertex3f( x0, y0, z0 );

      gl.glNormal3f( 1, 0, 0 );           // right face, positive x direction

      gl.glTexCoord2f( 1, 0 );
      gl.glVertex3f( x1, y0, z0 );

      gl.glTexCoord2f( 0, 0 );
      gl.glVertex3f( x1, y0, z1 );

      gl.glTexCoord2f( 0, 1 );
      gl.glVertex3f( x1, y1, z1 );

      gl.glTexCoord2f( 1, 1 );
      gl.glVertex3f( x1, y1, z0 );

      gl.glNormal3f(  -1, 0, 0 );        // left face, negative x direction

      gl.glTexCoord2f( 0, 0 );
      gl.glVertex3f( x0, y0, z0 );

      gl.glTexCoord2f( 0, 1 );
      gl.glVertex3f( x0, y1, z0 );

      gl.glTexCoord2f( 1, 1 );
      gl.glVertex3f( x0, y1, z1 );

      gl.glTexCoord2f( 1, 0 );
      gl.glVertex3f( x0, y0, z1 );
    gl.glEnd();

    postRender( drawable );              // clean up after drawing
  }

  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
    JoglPanel demo = new JoglPanel( new SolidBox( 2, 2, 4 ) );

    JFrame frame = new JFrame( "SolidBox Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

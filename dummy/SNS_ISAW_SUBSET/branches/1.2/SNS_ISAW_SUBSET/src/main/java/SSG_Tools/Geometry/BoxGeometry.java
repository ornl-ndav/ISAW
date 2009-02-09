/*
 * File:  BoxGeometry.java
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
 * $Log$
 * Revision 1.1  2007/08/14 00:26:50  dennis
 * New geometry classes from updated SSG_Tools at UW-Stout.
 *
 * Revision 1.2  2006/10/16 04:03:14  dennis
 * Renamed Render() method to DrawGeometry() so that the base
 * class Render() method can take care of texture coordinate
 * generation before calling DrawGeometry() to actually do the
 * drawing.
 *
 * Revision 1.1  2006/10/15 01:17:14  dennis
 * Factored out Geometry from SolidBox Shape.
 *
 * Revision 1.7  2006/08/04 02:16:21  dennis
 * Updated to work with JSR-231, 1.0 beta 5,
 * instead of jogl 1.1.1.
 *
 * Revision 1.6  2006/07/20 19:59:01  dennis
 * Replaced deprecated method frame.show() with setVisible(true)
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

package SSG_Tools.Geometry;

import javax.media.opengl.*;
import javax.swing.*;
import java.awt.Color;

import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;
import SSG_Tools.SSG_Nodes.Shapes.*;
import SSG_Tools.Appearance.*;

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
 *  UNLESS setTexCoordGenMode() has been used to turn on automatic
 *  generation of texture coordinates by OpenGL.  In that case, the
 *  automatically generated coordinates will take precedence over the
 *  values set by glTexCoord2f().
 */

public class BoxGeometry extends Geometry
{
  float width  = 1;        // The dimensions of this box in x,y,z directions
  float height = 1;
  float depth  = 1;


  /* ------------------------ default constructor ------------------------ */
  /**
   *  Construct a default box object with the width, height and depth all  
   *  equal to 1.
   */
  public BoxGeometry()
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
  public BoxGeometry( float width, float height, float depth )
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


  /* ---------------------------- DrawGeometry --------------------------- */
  /**
   *  Draw this Geometry using the specified GL object.
   *
   *  @param  gl  The GL object to use for drawing this Geometry object.
   */
  public void DrawGeometry( GL gl )
  {
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
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
    Geometry   box_geometry   = new BoxGeometry( 2, 2, 4 );
    Appearance box_appearance = new Appearance( new Material(Color.BLUE) );
    Shape box_shape = new GenericShape( box_geometry, box_appearance ); 
 
    JoglPanel demo = new JoglPanel( box_shape, false );
    new MouseArcBall( demo );

    JFrame frame = new JFrame( "BoxGeometry Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

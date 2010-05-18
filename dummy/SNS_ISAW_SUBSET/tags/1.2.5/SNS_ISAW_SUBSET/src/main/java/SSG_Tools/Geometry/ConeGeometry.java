/*
 * File:  ConeGeometry.java
 *
 * Copyright (C) 2006, Dennis Mikkelson
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
 * Revision 1.2  2007/08/26 23:14:52  dennis
 * Copied latest version from UW-Stout repository.
 *
 * Revision 1.3  2007/08/25 04:00:15  dennis
 * Made n_slices and n_stacks ints.
 *
 * Revision 1.2  2006/10/18 02:27:54  dennis
 * Removed unused variable "y" from the DrawGeometry() method.
 *
 * Revision 1.1  2006/10/17 01:04:46  dennis
 * Initial version of ConeGeometry, adapted from CylinderGeometry
 */

package SSG_Tools.Geometry;

import javax.media.opengl.*;
import java.awt.Color;
import javax.swing.*;

import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;
import SSG_Tools.Appearance.*;
import SSG_Tools.SSG_Nodes.Shapes.*;

/** 
 *  This class draws a hollow cone of the specified radius, and height,
 *  with its base centered at the origin with its axis along the y-axis.
 *  The cone is subdivided into a number of "slices", by lines running from
 *  the vertex at the top of the cone to a point on the circular base of 
 *  the cone.  It is divided into "stacks" by circles around the cone.
 *  The number of slices and stacks can be specified when 
 *  the cone is constructed.  In addition to the points of the cone,
 *  and surface normals, this class also generates texture coordinates.
 *  The "s" coordiante increases from 0 to 1 around the cone in the  
 *  direction of positive rotation around the y-axis.  The "t" 
 *  coordinate increases from 0 to 1 from the "base" of the cone to the 
 *  vertex of the cone along the positive y-axis.
 *
 *  NOTE: These explicitly generated texture coordinates will be used
 *  UNLESS setTexCoordGenMode() has been used to turn on automatic
 *  generation of texture coordinates by OpenGL.  In that case, the
 *  automatically generated coordinates will take precedence over the 
 *  values set by glTexCoord2f().
 */

public class ConeGeometry extends Geometry 
{
  public static final float DEFAULT_RADIUS = 1;
  public static final float DEFAULT_HEIGHT = 1;
  public static final int   DEFAULT_NUM_STACKS = 10;
  public static final int   DEFAULT_NUM_SLICES = 10;
 
  private float radius   = DEFAULT_RADIUS;
  private float height   = DEFAULT_HEIGHT;
  private int   n_slices = DEFAULT_NUM_STACKS;
  private int   n_stacks = DEFAULT_NUM_SLICES;


  /* ------------------------ default constructor ------------------------ */
  /**
   *  Construct a default cone object with the default radius, height,
   *  and number of slices and stacks.   The defaults produce a cone with 
   *  base radius 1, height 1 with 10 slices and stack
   */
  public ConeGeometry()
  {
    // Default constructor
  }


  /* --------------------------- constructor --------------------------- */
  /**
   *  Construct a cone object with the specified radius and height, 
   *  using the specified number of subdivisions. 
   *
   *  @param  radius  The radius of the base of the cone
   *  @param  height  The height of the cone 
   *  @param  slices  The number of lines of surface points, from the base 
   *                  to the vertex.  If this is less than or equal to zero, 
   *                  the default number of slices will be used.
   *  @param  stacks  The number of circles of surface points used to make the
   *                  cone.  If this is less than or equal to zero, the 
   *                  default number of stacks will be used.
   */
  public ConeGeometry( float radius, float height, int slices, int stacks )
  {
    this();
    this.radius = radius;
    this.height = height;

    if ( slices > 0 )
      n_slices = slices;

    if ( stacks > 0 )
      n_stacks = stacks;
  }


  /* ---------------------------- getRadius -------------------------- */
  /**
   *  Get the radius of this cone.
   *
   *  @return the radius that was specified at construction time.
   */
  public float getRadius()
  {
    return radius;
  }


  /* ---------------------------- getHeight -------------------------- */
  /**
   *  Get the height of this cone.
   *
   *  @return the height that was specified at construction time.
   */
  public float getHeight()
  {
    return height;
  }


  /* ---------------------------- getSlices -------------------------- */
  /**
   *  Get the number of slices used for the cone.
   *
   *  @return the number of slices that were specified at construction time.
   */
  public int getSlices()
  {
    return n_slices;
  }


  /* ---------------------------- getStacks -------------------------- */
  /**
   *  Get the number of circles used to make the cone.
   *
   *  @return the number of stacks that were specified at construction time.
   */
  public int getStacks()
  {
    return n_stacks;
  }


  /* ---------------------------- DrawGeometry --------------------------- */
  /**
   *  Draw this Geometry using the specified GL object.
   *
   *  @param  gl  The GL object to use for drawing this Geometry object.
   */
  public void DrawGeometry( GL gl )
  {
      float delta_theta = (float)( 2 * Math.PI / n_slices ); 
      float delta_h     = height / n_stacks;    

      float theta = 0;                                // current "longitude"
      float h1 = 0;                                   // current "height"
      float h2 = delta_h;                             // new "height"

      float x,                                        // coordinates of
            z;                                        // point on cone

      float r1,                                       // radii of two succesive
            r2;                                       // levels around the cone 

      float s,                                        // texture coordinates
            t1, t2;                                   // for points on cone 


      float nx, ny, nz;                               // surface normal vector
      float length;

      for ( int k = 0; k < n_stacks; k++ )            // draw quad strips in
      {                                               // bands between circles
        gl.glBegin( GL.GL_QUAD_STRIP ); 
          h1 = k  * delta_h;
          h2 = h1 + delta_h;
          t1 = h1 / height;
          t2 = h2 / height;
          r1 = (height - h1)/height * radius;
          r2 = (height - h2)/height * radius;
          for ( int i = 0; i <= n_slices; i++ )
          {
            theta = i * delta_theta;
            z  = (float)(r1 * Math.cos( theta ) );    // note: this could be
            x  = (float)(r1 * Math.sin( theta ) );    // optimized.
            s  = (float)(theta/(2*Math.PI));
                                                      // note: the calculation
            nx = x;                                   // of the normal can be
            ny = r1 * radius/height;                  // optimized.
            nz = z; 
            length = (float)Math.sqrt(nx*nx + ny*ny + nz*nz);
            nx /= length;
            ny /= length;
            nz /= length;

            gl.glTexCoord2f( s, t1 );
            gl.glNormal3f( nx, ny, nz );
            gl.glVertex3f( x, h1, z );

            z  = (float)(r2 * Math.cos( theta ) );
            x  = (float)(r2 * Math.sin( theta ) );
            gl.glTexCoord2f( s, t2 ); 
            gl.glVertex3f( x, h2, z );
          }
        gl.glEnd();
      }
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
    Geometry geometry = new ConeGeometry( 1, 2, 60, 20 );

    Material material = new Material();
    material.setColor( Color.WHITE );

    Appearance appearance = new Appearance();
    appearance.setMaterial( material );

    GenericShape cone = new GenericShape( geometry, appearance );

    JoglPanel demo = new JoglPanel( cone );
    new MouseArcBall( demo );

    JFrame frame = new JFrame( "ConeGeometry Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

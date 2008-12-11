/*
 * File:  PlaneGeometry.java
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
 * Revision 1.1  2007/08/14 00:26:51  dennis
 * New geometry classes from updated SSG_Tools at UW-Stout.
 *
 * Revision 1.3  2006/10/25 18:11:45  dennis
 * Fixed some java doc comments.
 *
 * Revision 1.2  2006/10/16 04:03:14  dennis
 * Renamed Render() method to DrawGeometry() so that the base
 * class Render() method can take care of texture coordinate
 * generation before calling DrawGeometry() to actually do the
 * drawing.
 *
 * Revision 1.1  2006/10/15 04:46:31  dennis
 * Factored out Geometry object from PlanarSurface shape.
 *
 * Revision 1.10  2006/08/04 02:16:21  dennis
 * Updated to work with JSR-231, 1.0 beta 5,
 * instead of jogl 1.1.1.
 *
 * Revision 1.9  2006/07/20 19:59:01  dennis
 * Replaced deprecated method frame.show() with setVisible(true)
 *
 * Revision 1.8  2005/10/30 01:22:41  dennis
 * Added default constructor.
 *
 * Revision 1.7  2005/10/29 23:07:40  dennis
 * Changed orientation of default texture coordinates.  The "s" direction
 * is now in the direction of the positive z-axis, and the "t" direction is
 * in the direction of the positive x-axis.
 * Added some information on the automatic generation of texture coordinates
 * to the javadocs.
 *
 * Revision 1.6  2005/10/25 03:44:04  dennis
 * Added methods to get the size, number of steps and max texture
 * coordinates.
 *
 * Revision 1.5  2005/10/24 21:43:34  dennis
 * Expanded docs at start of class, and in the Render method.
 * Added test of texture coordinates to main test program.
 *
 * Revision 1.4  2005/10/14 03:46:47  dennis
 * Updated from current version kept in CVS at IPNS.
 *
 * Revision 1.3  2004/11/15 17:32:04  dennis
 * Added methods setMax_s() and setMax_t() that can be used to determine
 * how many times the texture map is repeated across the surface.
 * NOTE: This still generates texture coordinates explicitly using
 *       glTexCoord2f() as the vertices are generated.  Alternatively,
 *       the texture coordinates could be generated automatically using
 *       the setTexCoordGenMode() method inherited from the Shape class.
 *
 */
package SSG_Tools.Geometry;

import javax.media.opengl.*;
import javax.swing.*;

import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;
import SSG_Tools.SSG_Nodes.Shapes.*;
import SSG_Tools.Appearance.*;
import SSG_Tools.Appearance.Textures.*;
import SSG_Tools.Utils.*;

/** 
 *  This class draws a rectangular plane surface in the x-z plane, centered
 *  at the origin, split into many smaller rectangles, to provide more
 *  vertices for the OpenGL lighting calculation.  Texture coordinates from
 *  [0,max_s] and [0,max_t] are also generated using calls to glTexCoord2f().
 *  The texture coordinates are assigned as follows: 
 *  The "s" coordinate increases from 0 to max_s in the z-direction.  The
 *  "t" coordinate increases from 0 to max_t in the x-direction.  The texture
 *  coordinates (s,t) = (0,0) are assigned to the point (-width/2,0,-depth/2).
 *
 *  NOTE: These explicitly generated texture coordinates will be used
 *  UNLESS Shape.setTexCoordGenMode() has been used to turn on automatic
 *  generation of texture coordinates by OpenGL.  In that case, the
 *  automatically generated coordinates will take precedence over the values
 *  set by glTexCoord2f().
 */

public class PlaneGeometry extends Geometry 
{
  private float width = 10;     // The dimensions of this plane in x and z dir
  private float depth = 10;     
  private int   n_x_steps = 10; // The number of steps in the x and z directions
  private int   n_z_steps = 10;
  private float max_s     = 1;  // The number of times a texture map should be
  private float max_t     = 1;  // repeated in the x and z directions


  /* ------------------------ default constructor ------------------------ */
  /**
   *  Construct a default PlanarSurface object with the width and depth both  
   *  equal to 10, with 10 subdivisions in the width and depth direction.
   *  The default texture coordinates range from 0 to 1 in s and t, across
   *  the full surface.
   */
  public PlaneGeometry()
  {
    // Default constructor
  }


  /* --------------------------- constructor --------------------------- */
  /**
   *  Construct a PlanarSurface object with the specified width, height
   *  and number of subdivisions.  By default, texture coordinates will
   *  range from 0 to 1 in both "s" and "t" directions.
   *
   *  @param  width    The total width  of the plane in the "x" direction.
   *  @param  depth    The total depth  of the plane in the "y" direction.
   *  @param  num_x    The number of rectangles to use in the x direction.
   *                   If a negative or zero value is passed in, a default of
   *                   10 will be used.
   *  @param  num_z    The number of rectangles to use in the z direction.
   *                   If a negative or zero value is passed in, a default of
   *                   10 will be used.
   */
  public PlaneGeometry( float width, float depth, int num_x, int num_z )
  {
    this.width = width;
    this.depth = depth;
    if ( num_x > 0 )
      n_x_steps   = num_x; 
    if ( num_z > 0 )
      n_z_steps   = num_z; 
  }


  /* --------------------------- constructor --------------------------- */
  /**
   *  Construct a PlanarSurface object with the specified width, height,
   *  number of subdivisions and texture coordinate range. 
   *
   *  @param  width    The total width  of the plane in the "x" direction.
   *  @param  depth    The total depth  of the plane in the "y" direction.
   *  @param  num_x    The number of rectangles to use in the x direction.
   *                   If a negative or zero value is passed in, a default of
   *                   10 will be used.
   *  @param  num_z    The number of rectangles to use in the z direction.
   *                   If a negative or zero value is passed in, a default of
   *                   10 will be used.
   *  @param  max_s    The largest texture coordinate "s" to use on this
   *                   planar surface.
   *  @param  max_t    The largest texture coordinate "t" to use on this
   *                   planar surface.
   */
  public PlaneGeometry( float width, float depth, 
                        int   num_x, int   num_z,
                        float max_s, float max_t )
  {
    this( width, depth, num_x, num_z );
    this.max_s = max_s;
    this.max_t = max_t;
  }


  /* -------------------------- getNum_x_steps ---------------------------- */
  /**
   *  Get the value for the number of subdivisions of this
   *  surface in the direction of the x-axis. 
   *
   *  @return  The number of rectangles used in the x direction.
   * 
  public int getNum_x_steps()
  {
    return n_x_steps;
  }


  /* -------------------------- getNum_z_steps ---------------------------- */
  /**
   *  Get the value for the number of subdivisions of this
   *  surface in the direction of the z-axis. 
   *
   *  @return  The number of rectangles used in the z direction.
   * 
  public int getNum_z_steps()
  {
    return n_z_steps;
  }


  /* ---------------------------- getMax_s ------------------------------ */
  /**
   *  Get the value for the maximum "s" texture coordinate. 
   *
   *  @return  max_s  the maximum texture coordinate "s" that is generated.
   */
  public float getMax_s()
  {
    return max_s;
  }


  /* ---------------------------- getMax_t ------------------------------ */
  /**
   *  Get the value for the maximum "t" texture coordinate. 
   *
   *  @return  max_t  the maximum texture coordinate "t" that is generated.
   */
  public float getMax_t()
  {
    return max_t;
  }


  /* ---------------------------- getWidth ---------------------------- */
  /**
   *  Get the width of this PlanarSurface
   *
   *  @return the width that was set when the PlanarSurface was constructed
   */
  public float getWidth()
  {
    return width;
  }


  /* ---------------------------- getDepth ---------------------------- */
  /**
   *  Get the depth of this PlanarSurface
   *
   *  @return the depth that was set when the PlanarSurface was constructed
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
    float x, 
          z1,
          z2;

    float dx = width / n_x_steps;
    float dz = depth / n_z_steps;

    gl.glNormal3f( 0, 1, 0 );                // one normal for the whole plane

    for ( int i = 0; i < n_z_steps; i++ )    // now step across the plane, 
    {                                        // along two lines of constant z. 
      z1 = -depth/2 +    i    * dz;
      z2 = -depth/2 + (i + 1) * dz;
      gl.glBegin( GL.GL_QUAD_STRIP );            // do a QuadStrip, extending
        for ( int j = 0; j <= n_x_steps; j++ )   // in the x-direction, between
        {                                        // z1 and z2
          x = -width/2 + j * dx;
          gl.glTexCoord2f( (max_s * i)/n_z_steps,
                           (max_t * j)/n_x_steps );
          gl.glVertex3d( x, 0, z1 ); 

          gl.glTexCoord2f( (max_s * (i+1)) / n_z_steps, 
                           (max_t *   j  ) / n_x_steps );
          gl.glVertex3d( x, 0, z2 ); 
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
    Geometry plane_geometry = new PlaneGeometry( 20, 20, 100, 100 );

    Appearance plane_appearance = new Appearance();  // make default
                                                     // Appearance object.

                                                     // Load a texture image
                                                     // and create a Texture2D
    String directory = System.getProperty( "user.home" ) +
                       "/SSG_Data/TextureImages/OpenInventor/";
    String name = "Marble01.jpg";
    int n_rows = 256;
    int n_cols = 256;
    byte image[] = LoadTexture.LoadImage( directory + name, n_rows, n_cols );
    Texture2D texture = new Texture2D( image, n_rows, n_cols );

                                             // Give the texture to the
                                             // Appearance object and set that
                                             // as the appearance for the plane
    plane_appearance.setTexture( texture ); 
    Shape plane = new GenericShape( plane_geometry, plane_appearance );
                                             // Make a JoglPanel to display the
                                             // plane, and put that in a frame 
    JoglPanel demo = new JoglPanel( plane );
    new MouseArcBall( demo );

    JFrame frame = new JFrame( "PlaneGeometry Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

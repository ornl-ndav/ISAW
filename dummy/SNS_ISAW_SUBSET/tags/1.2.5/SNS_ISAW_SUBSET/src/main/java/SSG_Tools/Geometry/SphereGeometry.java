/*
 * File:  SphereGeometry.java
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
 * Revision 1.2  2007/08/26 23:14:53  dennis
 * Copied latest version from UW-Stout repository.
 *
 * Revision 1.4  2007/08/25 04:00:15  dennis
 * Made n_slices and n_stacks ints.
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
 * Revision 1.1  2006/10/15 01:18:00  dennis
 * Factored out Sphere geometry from Sphere shape.
 * 
 * Revision 1.10  2006/08/04 02:16:21  dennis
 * Updated to work with JSR-231, 1.0 beta 5,
 * instead of jogl 1.1.1.
 *
 * Revision 1.9  2006/07/20 19:59:01  dennis
 * Replaced deprecated method frame.show() with setVisible(true)
 *
 * Revision 1.8  2005/10/29 23:08:48  dennis
 * Added some information about automatically generated texture coordinates
 * to the javadocs.
 *
 * Revision 1.7  2005/10/29 22:32:41  dennis
 * Added generation of texture coordinates.  This required using quad strips
 * around the north and south poles, instead of triangle fans, to allow
 * for better texture mapping.
 * Added some additional explanatory javadocs comments.
 *
 * Revision 1.6  2005/10/25 04:17:22  dennis
 * Added methods to get the radius, and number of slices and stacks.
 * Added explicit specification of texture coordinates.
 * Expanded java doc description of the class.
 *
 * Revision 1.5  2004/12/05 00:55:12  dennis
 * Fixed bug... is_list flag was not set true after forming the
 * display list.
 *
 * Revision 1.4  2004/11/22 18:50:01  dennis
 * Temporarily changed to use a display list for the sphere, to improve
 * efficiency.
 *
 * Revision 1.3  2004/11/15 19:18:35  dennis
 * Added tag for logging.
 *
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
 *  This class draws a solid sphere of the specified radius centered at the 
 *  origin.  The sphere is subdivided into a number of "slices" (lines of
 *  longitude) and "stacks" (lines of latitude) that can be specified when
 *  the sphere is constructed.  In addition to the points of the sphere,
 *  and surface normals, this class also generates texture coordinates.
 *  The "s" coordinate increases from 0 to 1 around the sphere in the 
 *  "easterly" direction, along lines of constant latitude.  The "t" 
 *  coordinate increases from 0 to 1 from the south pole to the north pole,
 *  along lines of constant longitude.
 *
 *  NOTE: These explicitly generated texture coordinates will be used
 *  UNLESS setTexCoordGenMode() has been used to turn on automatic
 *  generation of texture coordinates by OpenGL.  In that case, the
 *  automatically generated coordinates will take precedence over the 
 *  values set by glTexCoord2f().
 */

public class SphereGeometry extends Geometry 
{
  public static final float DEFAULT_RADIUS = 1;
  public static final int   DEFAULT_NUM_STACKS = 10;
  public static final int   DEFAULT_NUM_SLICES = 10;
 
  private float radius   = DEFAULT_RADIUS;
  private int   n_slices = DEFAULT_NUM_STACKS;
  private int   n_stacks = DEFAULT_NUM_SLICES;


  /* ------------------------ default constructor ------------------------ */
  /**
   *  Construct a default sphere object with the default radius, number
   *  of slices and stacks.   The defaults produce a sphere of radius 1,
   *  with 10 slices and stack
   */
  public SphereGeometry()
  {
    // Default constructor
  }


  /* --------------------------- constructor --------------------------- */
  /**
   *  Construct a sphere object with the specified radius using the specified
   *  number of subdivisions.  Treating the sphere like a globe, the 
   *  "north pole" is along the y-axis.  The number of slices is the number
   *  of bands of constant longitude used and the number of stacks is the 
   *  number of bands of constant latitude used.
   *
   *  @param  radius  The radius of the sphere
   *  @param  slices  The number of lines of longitude used to make the sphere.
   *                  If this is less than or equal to zero, the default number
   *                  of slices will be used.
   *  @param  stacks  The number of lines of latitude used to make the sphere.
   *                  If this is less than or equal to zero, the default number
   *                  of stacks will be used.
   */
  public SphereGeometry( float radius, int slices, int stacks )
  {
    this();
    this.radius = radius;

    if ( slices > 0 )
      n_slices = slices;

    if ( stacks > 0 )
      n_stacks = stacks;
  }


  /* ---------------------------- getRadius -------------------------- */
  /**
   *  Get the radius of this sphere.
   *
   *  @return the radius that was specified at construction time.
   */
  public float getRadius()
  {
    return radius;
  }


  /* ---------------------------- getSlices -------------------------- */
  /**
   *  Get the number of lines of longitude used to make the sphere.
   *
   *  @return the number of slices that were specified at construction time.
   */
  public int getSlices()
  {
    return n_slices;
  }


  /* ---------------------------- getStacks -------------------------- */
  /**
   *  Get the number of lines of latitude used to make the sphere.
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
                               // generate the required points on the sphere
                               // using spherical polar coordinates

      float delta_theta = (float)( 2 * Math.PI / n_slices );  // around equator
      float delta_phi   = (float)( Math.PI / n_stacks );      // from N. pole

      float theta = 0;                                // current "longitude"
      float phi1  = 0;                                // current "latitude"
      float phi2  = delta_phi;                        // new "latitude"

      float r1,                                       // radius at current
            r2;                                       // and next latitude

      float x,                                        // coordinates of
            y1, y2,                                   // point on sphere
            z; 

      float s,                                        // texture coordinates
            t1, t2;                                   // for points on sphere

      for ( int k = 0; k < n_stacks; k++ )            // draw quad strips in
      {                                               // bands between lines of
        gl.glBegin( GL.GL_QUAD_STRIP );               // constant latitude
          phi1 =  k    * delta_phi;
          phi2 = (k+1) * delta_phi;
          y1 = (float)( radius * Math.cos( phi1 ) );
          y2 = (float)( radius * Math.cos( phi2 ) );
          t1 = (float)(1 - phi1/Math.PI);
          t2 = (float)(1 - phi2/Math.PI);
          for ( int i = 0; i <= n_slices; i++ )
          {
            theta = i * delta_theta;
            r1 = (float)( radius * Math.sin( phi1 ) );
            z  = (float)(r1 * Math.cos( theta ) );
            x  = (float)(r1 * Math.sin( theta ) );
            s  = (float)(theta/(2*Math.PI));
            gl.glTexCoord2f( s, t1 );
            gl.glNormal3f( x/radius, y1/radius, z/radius );
            gl.glVertex3f( x, y1, z );

            r2 = (float)( radius * Math.sin( phi2 ) );
            z  = (float)(r2 * Math.cos( theta ) );
            x  = (float)(r2 * Math.sin( theta ) );
            gl.glTexCoord2f( s, t2 ); 
            gl.glNormal3f( x/radius, y2/radius, z/radius );
            gl.glVertex3f( x, y2, z );
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
    SphereGeometry sphere_geometry = new SphereGeometry( 1, 20, 20 );

    Material material = new Material();
    material.setColor( Color.RED );

    Appearance appearance = new Appearance();
    appearance.setMaterial( material );

    GenericShape sphere = new GenericShape( sphere_geometry, appearance );
  	
    JoglPanel demo = new JoglPanel( sphere );
    new MouseArcBall( demo );

    JFrame frame = new JFrame( "SphereGeometry Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

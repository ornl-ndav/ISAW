/*
 * File:  Sphere.java
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
 * $Log: Sphere.java,v $
 * Revision 1.9  2006/07/20 15:53:22  dennis
 * Updated from CVS repository at isaw.mscs.uwstout.edu.
 * Added generation of texture coordinates.
 * Added methods to get the radius, number of slices, etc.
 * Expanded javadocs.
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

package SSG_Tools.SSG_Nodes.Shapes;

import net.java.games.jogl.*;
import java.awt.*;
import javax.swing.*;

import gov.anl.ipns.MathTools.Geometry.*;
import SSG_Tools.Viewers.*;
import SSG_Tools.Appearance.*;

/** 
 *  This class draws a solid sphere of the specified radius centered at the 
 *  origin.  The sphere is subdivided into a number of "slices" (lines of
 *  longitude) and "stacks" (lines of latitude) that can be specified when
 *  the sphere is constructed.  In addition to the points of the sphere,
 *  and surface normals, this class also generates texture coordinates.
 *  The "s" coordiante increases from 0 to 1 around the sphere in the 
 *  "easterly" direction, along lines of constant latitude.  The "t" 
 *  coordinate increases from 0 to 1 from the south pole to the north pole,
 *  along lines of constant longitude.
 *
 *  NOTE: These explicitly generated texture coordinates will be used
 *  UNLESS Shape.setTexCoordGenMode() has been used to turn on automatic
 *  generation of texture coordinates by OpenGL.  In that case, the
 *  automatically generated coordinates will take precedence over the values
 *  set by glTexCoord2f().
 */

public class Sphere extends Shape
{
  public static final float DEFAULT_RADIUS = 1;
  public static final int   DEFAULT_NUM_STACKS = 10;
  public static final int   DEFAULT_NUM_SLICES = 10;
 
  private float radius   = DEFAULT_RADIUS;
  private float n_slices = DEFAULT_NUM_STACKS;
  private float n_stacks = DEFAULT_NUM_SLICES;

  private boolean is_list = false;
  private int     list_id;

  /* ------------------------ default constructor ------------------------ */
  /**
   *  Construct a default sphere object with the default radius, number
   *  of slices and stacks.   The defaults produce a sphere of radius 1,
   *  with 10 slices and stack
   */
  public Sphere()
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
  public Sphere( float radius, int slices, int stacks )
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
  public float getSlices()
  {
    return n_slices;
  }


  /* ---------------------------- getStacks -------------------------- */
  /**
   *  Get the number of lines of latitude used to make the sphere.
   *
   *  @return the number of stacks that were specified at construction time.
   */
  public float getStacks()
  {
    return n_stacks;
  }


  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this sphere to the specified drawable.  
   *
   *  @param  drawable  The drawable on which the sphere is to be drawn.
   */
  public void Render( GLDrawable drawable )
  {
    GL gl = drawable.getGL();

    if ( !is_list )                    // if not a display list, make it one
    {
      list_id = gl.glGenLists( 1 );
      gl.glNewList( list_id, GL.GL_COMPILE_AND_EXECUTE );

      preRender( drawable );     // handle name stack and appearance

                               // generate the required points on the sphere
                               // using spherical polar coordinates

      float delta_theta = (float)( 2 * Math.PI / n_slices );  // around equator
      float delta_phi   = (float)( Math.PI / n_stacks );      // from N. pole

      float theta = 0;                                 // current "longitude"
      float phi1  = 0;                                 // current "latitude"
      float phi2  = delta_phi;                         // new "latitude"

      float r1,                                        // radius at current
            r2;                                        // and next latitude

      float x,                                         // coordinates of
            y1, y2,                                    // point on sphere
            z; 

      float s,                                         // texture coordinates
            t1, t2;                                    // for points on sphere

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

      postRender( drawable );                      // clean up, after rendering
      gl.glEndList();
      is_list = true;
    }
    else
      gl.glCallList( list_id );
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
    Shape sphere = new Sphere( 1, 20, 20 );
    Material material = new Material();
    material.setColor( Color.RED );
    Appearance appearance = new Appearance();
    appearance.setMaterial( material );
    sphere.setAppearance( appearance );
  	
    JoglPanel demo = new JoglPanel( sphere );
    
    demo.getCamera().setVUV( new Vector3D( 1, 0, 0 ) );
    demo.getCamera().setCOP( new Vector3D( 0, 5, 0 ) );

    JFrame frame = new JFrame( "Sphere Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

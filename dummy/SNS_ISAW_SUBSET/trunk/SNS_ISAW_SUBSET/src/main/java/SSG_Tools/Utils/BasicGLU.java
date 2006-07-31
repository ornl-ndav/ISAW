/*
 * File:  BasicGLU.java
 *
 * Adapted and ported to java from the Mesa GLU implementation, "glu.c", 
 * Version 3.5, Copyright (C) 1995-2001, Brian Paul.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *  Modified:
 *
 *  $Log: BasicGLU.java,v $
 *  Revision 1.4  2005/07/14 21:49:06  dennis
 *  Switched from local copy of Vector3D, etc. to using Vector3D, etc.
 *  from gov.anl.ipns.MathTools.Geometry.
 *
 *  Revision 1.3  2004/11/22 18:46:07  dennis
 *  Documented empty body of default constructor.
 *
 *  Revision 1.2  2004/10/27 19:20:45  dennis
 *  Added gluUnProject() method.
 *
 *  Revision 1.1  2004/10/25 21:45:30  dennis
 *  Added to SSG_Tools CVS repository
 *
 *
 *  10/13/2004  Temporarily removed gluUnProject, since the interface
 *              used double precision values
 *
 *  6/15/2004  Initial implementation of a few basic GLU functions. 
 *             This was needed since jogl did not work with the 
 *             GL Utilities (GLU) provided on some platforms 
 *             (specifically with Mesa 6.0.1).
 *
 */

package SSG_Tools.Utils;

import net.java.games.jogl.*;

import gov.anl.ipns.MathTools.*;

/**
 *  This class contains several utilities typically provided by the GLU 
 *  package.  Some of these are ported from the Mesa 3D graphics library,
 *  some re-implemented from scratch.  The port was done since as of 6/15/04,
 *  the jogl library did not interface properly to the GLU in several 
 *  several graphics environments.  As a result basic support for the most
 *  commonly used GLU functions was needed.
 */
public class BasicGLU
{
  /**
   *  Private constructor, since no one should instantiate this class.
   */
  private BasicGLU()
  {
  	// private constructor prevents someone from intantiating it
  }

  /* ----------------------------- gluLookAt --------------------------- */
  /**
   *  This method constructs and multiplies the current GL matrix by a 
   *  matrix representing the viewing process.  This is a replacement for 
   *  GLU's gluLookAt since that crashes when used from jogl on some platforms.
   *  This method is normally called once when starting to draw a scene, 
   *  immediately after changing the matrix mode to GL_MODELVIEW, and 
   *  loading the identity.  This routine is ported from the Mesa package,
   *  Copyright Brian Paul.
   * 
   *  @param  gl       The GL context
   *  @param  eyex     The x coordinate of the observer's position
   *  @param  eyey     The y coordinate of the observer's position
   *  @param  eyez     The z coordinate of the observer's position
   *  @param  centerx  The x coordinate of the point the observer is looking at
   *  @param  centery  The y coordinate of the point the observer is looking at
   *  @param  centerz  The z coordinate of the point the observer is looking at
   *  @param  upx      The x coordinate of the up direction of the observer
   *  @param  upy      The y coordinate of the up direction of the observer
   *  @param  upz      The z coordinate of the up direction of the observer
   */
  public static void gluLookAt( GL gl,
                                double eyex,    double eyey,    double eyez,
                                double centerx, double centery, double centerz,
                                double upx,     double upy,     double upz)
  {
    double m[] = new double[16];
    double x[] = new double[3];
    double y[] = new double[3];
    double z[] = new double[3];
    double mag;

    /* Make rotation matrix */

    /* Z vector */
     z[0] = eyex - centerx;
     z[1] = eyey - centery;
     z[2] = eyez - centerz;
     mag = Math.sqrt(z[0] * z[0] + z[1] * z[1] + z[2] * z[2]);
     if (mag > 0.0) {                     /* mpichler, 19950515 */
        z[0] /= mag;
        z[1] /= mag;
        z[2] /= mag;
     }

     /* Y vector */
     y[0] = upx;
     y[1] = upy;
     y[2] = upz;

     /* X vector = Y cross Z */
     x[0] = y[1] * z[2] - y[2] * z[1];
     x[1] = -y[0] * z[2] + y[2] * z[0];
     x[2] = y[0] * z[1] - y[1] * z[0];

     /* Recompute Y = Z cross X */
     y[0] = z[1] * x[2] - z[2] * x[1];
     y[1] = -z[0] * x[2] + z[2] * x[0];
     y[2] = z[0] * x[1] - z[1] * x[0];

     /* mpichler, 19950515 */
     /* cross product gives area of parallelogram, which is < 1.0 for
      * non-perpendicular unit-length vectors; so normalize x, y here
      */

     mag = Math.sqrt(x[0] * x[0] + x[1] * x[1] + x[2] * x[2]);
     if (mag > 0.0) {
        x[0] /= mag;
        x[1] /= mag;
        x[2] /= mag;
     }

     mag = Math.sqrt(y[0] * y[0] + y[1] * y[1] + y[2] * y[2]);
     if (mag > 0.0) {
        y[0] /= mag;
        y[1] /= mag;
        y[2] /= mag;
     }

     m[0+4* 0] = x[0];
     m[0+4* 1] = x[1];
     m[0+4* 2] = x[2];
     m[0+4* 3] = 0.0;
     m[1+4* 0] = y[0];
     m[1+4* 1] = y[1];
     m[1+4* 2] = y[2];
     m[1+4* 3] = 0.0;
     m[2+4* 0] = z[0];
     m[2+4* 1] = z[1];
     m[2+4* 2] = z[2];
     m[2+4* 3] = 0.0;
     m[3+4* 0] = 0.0;
     m[3+4* 1] = 0.0;
     m[3+4* 2] = 0.0;
     m[3+4* 3] = 1.0;

     gl.glMultMatrixd(m);

     /* Translate Eye to Origin */
     gl.glTranslated(-eyex, -eyey, -eyez);
  }


  /* --------------------------- gluPerspective ------------------------ */
  /**
   *  This method calls glFrustum() with parameters calculated to preserve
   *  the specified angluar field of view in the vertical direction and 
   *  with the specified aspect ratio.  This is a replacement for GLU's 
   *  gluPerspective, since that crashes when used from jogl on some platforms.
   *  This routine is ported from the Mesa package, Copyright Brian Paul.
   *
   *  @param  gl       The GL context
   *  @param  fovy     The angular field of view in the "y" direction, 
   *                   specified in degrees
   *  @param  aspect   The desired width/height ratio
   *  @param  zNear    The distance to the near clipping plane
   *  @param  zFar     The distance to the far clipping plane
   */
  public static void gluPerspective( GL     gl,
                                     double fovy,
                                     double aspect,
                                     double zNear,
                                     double zFar)
  {
    double xmin, xmax, ymin, ymax;

    ymax = zNear * Math.tan(fovy * Math.PI / 360.0);
    ymin = -ymax;
    xmin = ymin * aspect;
    xmax = ymax * aspect;

    gl.glFrustum(xmin, xmax, ymin, ymax, zNear, zFar);
  }


  /* --------------------------- gluPickMatrix -------------------------- */
  /**
   *  This method multiplies the current OpenGL matrix by a matrix that 
   *  scales and translates the view so that a small region around the 
   *  pixel x, y is mapped to the whole window.  This is designed to be 
   *  used during the selection process, in which case rendering to the 
   *  screen is not actually done.  This is a replacement for GLU's 
   *  gluPickMatrix since that crashes on some implementations.  
   *  This routine is ported from the Mesa package, Copyright Brian Paul.
   *
   *  @param  gl       The GL context
   *  @param  x        The pixel x coordinate
   *  @param  y        The pixel y coordinate, in "right side up" coordinates
   *  @param  width    The width of the region, in pixels, to use for picking
   *  @param  height   The height of the region, in pixels, to use for picking
   *  @param  viewport Array containing the x, y, width and height of the
   *                   current viewport.
   */
   public static void gluPickMatrix( GL gl,
                                     float x,
                                     float y,
                                     float width,
                                     float height,
                                     int viewport[] )
   {
     if (width <= 0 || height <= 0)
     {
       return;
     }

     float sx, sy;
     float tx, ty;

     sx = viewport[2] / width;
     sy = viewport[3] / height;
     tx = (viewport[2] + 2 * (viewport[0] - x)) / width;
     ty = (viewport[3] + 2 * (viewport[1] - y)) / height;

     float m[] = new float[16];
     m[0]  = sx;    // column 1
     m[1]  = 0;
     m[2]  = 0;
     m[3]  = 0;

     m[4]  = 0;     // column 2
     m[5]  = sy;
     m[6]  = 0;
     m[7]  = 0;

     m[8]  = 0;     // column 3
     m[9]  = 0;
     m[10] = 1;
     m[11] = 0;

     m[12] = tx;    // column 4
     m[13] = ty;
     m[14] = 0;
     m[15] = 1;

     gl.glMultMatrixf(m);
  }


 /* --------------------------- gluUnProject ---------------------------- */
 /**
  *  This method maps a point from window coordinates (with depth nomalized
  *  to [0,1] back to a point in the view volumne.  This is designed to be
  *  used to locate a point in 3D given a point on the window.
  *  This is a replacement for GLU's  gluUnProject since the GLU functions
  *  crashes when used with jogl on some implementations.  This code was
  *  loosely adapted from the code in Mesa, originally written by 
  *  M. Buffat 17/2/95.  
  *
  *  @param  win_x           The x-coordinate of the pixel
  *  @param  win_y           The y-coordinate of the pixel 
  *                          (in right-side-up pixel coordinates)
  *  @param  win_z           The nomalized z-coordinate of the pixel, in [0,1],
  *                          as returned by glReadPixels()
  *  @param  model_view_arr  One dimensional array containing the entries 
  *                          from the model view matrix, in column major order,
  *                          as returned by GetDoublev( GL_MODELVIEW_MATRIX, m)
  *  @param  projection_arr  One dimensional array containing the entries 
  *                          from the projection matrix, in column major order,
  *                          as returned by GetDoublev( GL_PROJECTION_MATRIX, m)  *  @param  viewport        One dimensional array containing the viewport
  *                          position and dimensions {x,y,width,height}.
  *  @param  world_x         Array to hold returned x coordinate in world_x[0]
  *  @param  world_y         Array to hold returned y coordinate in world_y[0]
  *  @param  world_z         Array to hold returned z coordinate in world_z[0]
  */
  public static void gluUnProject( double win_x,
                                   double win_y,
                                   double win_z,
                                   double model_view_arr[],
                                   double projection_arr[],
                                   int    viewport[],
                                   double world_x[],
                                   double world_y[],
                                   double world_z[] )
  {
     double model_view[][] = new double[4][4];
     double projection[][] = new double[4][4];

     int index = 0;                                     // copy into 2D array
     for ( int col = 0; col < 4; col++ )                // NOTE: GL & GLU list
       for ( int row = 0; row < 4; row++ )              // matrices in column
       {                                                // major order!!
         model_view[row][col] = model_view_arr[ index ];
         projection[row][col] = projection_arr[ index ];
         index++;
       }

     double product_matrix[][] = LinearAlgebra.mult(projection, model_view);

     double point[] = new double[4];
     point[0] = (win_x - viewport[0]) * 2.0 / viewport[2] - 1.0;
     point[1] = (win_y - viewport[1]) * 2.0 / viewport[3] - 1.0;
     point[2] = 2 * win_z - 1.0;
     point[3] = 1.0;

     LinearAlgebra.solve( product_matrix, point );
     double w = point[3];
     world_x[0] = point[0]/w;
     world_y[0] = point[1]/w;
     world_z[0] = point[2]/w;
  }
  
}

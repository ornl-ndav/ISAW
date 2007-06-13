/*
 * File:  OrthographicCamera.java
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
 *  $Log: OrthographicCamera.java,v $
 *  Revision 1.1  2005/07/18 20:59:47  dennis
 *  Initial checkin of camera for Orthographic Projection.
 *
 *
 */
package SSG_Tools.Cameras;

import net.java.games.jogl.*;
 
import SSG_Tools.RayTools.*;

import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This class extends the basic camera class and implements an
 *  orthographic projection.
 */

public class OrthographicCamera extends Camera
{


/* --------------------------- default constructor --------------------- */
/**
 *  Make a new orthographic camera with default values 
 */ 
  public OrthographicCamera()
  {
  }


/* ---------------------------- copy constructor ----------------------- */
/**
 *  Make a new orthograpnic camera with it's values copied from the 
 *  specified camera object.
 */ 
  public OrthographicCamera( Camera camera )
  {
    set( camera );
  }


/* ------------------------ MakeProjectionMatrix ------------------------ */ 
/** 
  *  Set up an orthographic projection matrix, based on the specified window 
  *  dimensions  and the current view angle and clipping planes.  
  *  NOTE: It is assumed that the matrix mode is GL_PROJECTION.
  *
  *  @param drawable  The GLDrawable for this canvas.
  *  @param width     The width of the window in pixels
  *  @param height    The height of the window in pixels
  */
  public void MakeProjectionMatrix( GLDrawable drawable,
                                    int width,
                                    int height )
  {
    GL gl = drawable.getGL();

    float half_h = calculateHalfHeight();
    float half_w = half_h * width/(float)height;
    gl.glOrtho( -half_w, half_w, -half_h, half_h, near_plane, far_plane );
  }


/* --------------------------- getLineOfSight ---------------------------- */
/**
 *  Get a Ray in world coordinates corresponding to the line of sight
 *  through the specified pixel on a window with the specified dimensions.
 *
 *  @param  x       pixel x coordinate 
 *  @param  y       pixel y coordinate 
 *  @param  width   the width of the current window in pixels
 *  @param  height  the width of the current window in pixels
 *
 *  @return  A Ray, starting at a point on the front clipping plane, along
 *           the line of sight through pixel ( x, y ) on the window.
 */
  public Ray getLineOfSight( int x, int y, int width, int height )
  {
     float ymax = calculateHalfHeight();

                                     // now that we can find the scale factor
                                     // between pixels and points on the front
                                     // clipping plane.
     float scale_factor = ymax/(height/2.0f);

                                     // find point on near plane in terms of
                                     // u,v and n vectors
     float u_comp = (x -  width/2.0f) * scale_factor;
     float v_comp = -(y - height/2.0f) * scale_factor;
     float n_comp = near_plane;

     Vector3D u = getU();
     Vector3D v = getV();
     Vector3D n = getN();

     u.multiply( u_comp );
     v.multiply( v_comp );
     n.multiply( n_comp );

     Vector3D point = new Vector3D(COP);
     point.add( u );
     point.add( v );
     point.add( n );
                                     // for OrthographicProject, the direction
                                     // of line of sight is just the "N" vector
     Vector3D direction = getN();

     return new Ray( point, direction );
  }


/* ------------------------- calculateHalfHeight ------------------------- */
/**
 *  Calculate the half-height of the front clipping plane, based on the
 *  view_angle, and distance between the COP and VRP.  NOTE: since this is
 *  an orthographic projection, that is the same as the half-height of the
 *  plane parallel to the front clipping plane, through the VRP.
 *
 *  @return half of the height of the front clipping plane.
 */
  private float calculateHalfHeight()
  {
     float angle_radians = (float)(Math.PI * view_angle/2 / 180);
     float half_h = (float)Math.tan( angle_radians ) * distance();
     return half_h;
  }

}

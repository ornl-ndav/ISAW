/*
 * File:  PerspectiveCamera.java
 *
 * Copyright (C) 2004 Dennis Mikkelson
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
 *  $Log: PerspectiveCamera.java,v $
 *  Revision 1.3  2005/07/18 18:39:44  dennis
 *  Added a copy constructor to initialize the camera values from
 *  another camera.  Added getLineOfSight() method that finds a
 *  ray in world coordinatates, starting at a point on the front
 *  clipping plane, pointing in the direction of a line of sight,
 *  given the pixel coordinates (x,y) of a point on the display
 *  window, and the size of the window in pixels.
 *
 *  Revision 1.2  2004/10/25 21:59:20  dennis
 *  Added to SSG_Tools CVS repository
 *
 *
 *   10/25/04  MakeProjectionMatrix no longer sets the matrix mode to 
 *             GL_PROJECTION and loads the identity.  This must now be
 *             done before calling MakeProjectionMatrix.  This change
 *             was needed so that a call to GLU.gluPickMatrix() could 
 *             be inserted BEFORE making the projection matrix, when 
 *             doing picking.
 *
 */
package SSG_Tools.Cameras;

import net.java.games.jogl.*;

import SSG_Tools.Utils.*;
import SSG_Tools.RayTools.*;

import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This class extends the basic camera class and implements a 
 *  perspective projection.
 */

public class PerspectiveCamera extends Camera
{


/* --------------------------- default constructor --------------------- */
/**
 *  Make a new perspective camera with default values 
 */ 
  public PerspectiveCamera()
  {
  }


/* ---------------------------- copy constructor ----------------------- */
/**
 *  Make a new perspective camera with it's values copied from the 
 *  specified camera object.
 */
  public PerspectiveCamera( Camera camera )
  {
    set( camera );
  } 


/* ------------------------ MakeProjectionMatrix ------------------------ */ 
/** 
  *  Set up a perspective projection matrix, based on the specified window 
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
    GL  gl  = drawable.getGL();

    BasicGLU.gluPerspective( gl,
                             view_angle,
                             width/(float)height,
                             near_plane,
                             far_plane );
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
                                     // NOTE: this is what gluPerspective does
                                     // so if gluPerspective were changed this
                                     // would also need to be changed.
     float ymax = (float)(near_plane * Math.tan(view_angle * Math.PI / 360.0));

                                     // now that we can find the scale factor
                                     // between pixels and points on the front
                                     // clipping plane.
     float scale_factor = ymax/(height/2.0f);

     float u_comp = (x -  width/2.0f) * scale_factor; 
     float v_comp = -(y - height/2.0f) * scale_factor; 
     float n_comp = near_plane;
                                     // find point on near plane in terms of
                                     // u,v and n vectors
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
                                     // find direction of line of sight by
                                     // subtracting the COP
     Vector3D direction = new Vector3D( point );
     direction.subtract( COP );
 
     return new Ray( point, direction );
  }

}

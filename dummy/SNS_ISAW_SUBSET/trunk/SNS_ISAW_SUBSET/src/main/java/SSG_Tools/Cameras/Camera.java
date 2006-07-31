/*
 * File:  Camera.java
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
 *  $Log: Camera.java,v $
 *  Revision 1.7  2006/07/20 14:40:12  dennis
 *  Added additional explanator java docs at the start of the class.
 *
 *  Revision 1.6  2005/07/18 18:47:26  dennis
 *  Added methods:
 *  set()             .. copy values from another camera
 *  RotateAroundCOP() .. rotate VRP & VUV about axis through COP
 *  WorldCoordMove()  .. move "camera" by WorldCoord Vector
 *  UVN_CoordMove()   .. move "camera" by UVN coord Vector
 *  getLineOfSight()  .. abstract method to get line of sight
 *                       given pixel (x,y) and window size
 *
 *  Revision 1.5  2005/07/14 21:48:57  dennis
 *  Switched from local copy of Vector3D, etc. to using Vector3D, etc.
 *  from gov.anl.ipns.MathTools.Geometry.
 *
 *  Revision 1.4  2005/07/13 18:43:03  dennis
 *  Added methods to get the U,V,N vectors that form a local coordinate
 *  system for the camera, calculated from the COP, VRP and VUV vectors.
 *  Added method to get the distance from the VRP to the COP.
 *  Added method, RotateAroundVRP(), to rotate the camera around any
 *  specified axis through the view reference point.
 *
 *  Revision 1.3  2004/12/06 20:04:53  dennis
 *  Added methods to get copies of the currently specified
 *  COP, VRP and VUV.
 *
 *  Revision 1.2  2004/10/25 21:59:20  dennis
 *  Added to SSG_Tools CVS repository
 *
 *  10/25/04  Make view matrix no longer sets the matrix mode and loads the
 *            identity. 
 */
package SSG_Tools.Cameras;

import net.java.games.jogl.*;

import SSG_Tools.Utils.*;
import SSG_Tools.RayTools.*;
import gov.anl.ipns.MathTools.Geometry.*;

/**
 *    This class is the abstract base class for classes that use information 
 *  about an observers position, view direction, near and far clipping planes
 *  to set the projection matrix and initial model view matrix to
 *  determine a view of the virtual world.  A camera object is associated
 *  with a JoglPanel that displays a scene graph.  The camera object determines
 *  how the scene is viewed, by allowing control of the position of the 
 *  the observer (center of projection), the point in space where the
 *  observer is looking (view reference point), the up direction for the
 *  obseverd (view up vector), the view angle, and near and far clipping
 *  planes.  
 *    
 *    In addition to basic methods for controlling the view, this class also
 *  defines a number of convenience methods for changing the view in 
 *  "world coordinates", or the "observers UVN coordinates", getting the
 *  "line of sight" corresponding to a particular pixel, etc.
 *
 *    The camera associated with a particular JoglPanel can be either a
 *  PerspectiveCamera, or an OrthographicCamera.  A PerspectiveCamera is 
 *  be used by default.
 */

abstract public class Camera 
{
                     // Default view parameters with camera looking at origin, 
                     // from (8,9,10)  with y-axis "up"

  protected Vector3D COP = new Vector3D( 8, 9, 10 ); 
  protected Vector3D VRP = new Vector3D( 0, 0,  0 );  
  protected Vector3D VUV = new Vector3D( 0, 1,  0 );  

                     // Default view volume

  protected float near_plane = 1;  
  protected float far_plane  = 50;
  protected float view_angle = 60; 


/* -------------------------------- set -------------------------------- */
/**
 *  Set values for the center of projection, view reference point, 
 *  view up vector, new_plane, far_plane and view angle from the
 *  specified camera.
 *
 *  @param  camera  The camera whose values are to be copied into this
 *                  camera.
 */
 public void set( Camera camera )
 {
   this.COP = camera.COP;
   this.VRP = camera.VRP;
   this.VUV = camera.VUV;
   this.near_plane = camera.near_plane;
   this.far_plane  = camera.far_plane;
   this.view_angle = camera.view_angle;
 }


/* ------------------------------ SetView -------------------------------- */
/**
 *  Set values for the center of projection, view reference point and 
 *  view up vector.  
 *
 *  @param  cop   Vector3D containing the x,y,z, coordinates of the
 *                center of projection
 *  @param  vrp   Vector3D containing the x,y,z, coordinates of the
 *                view reference point 
 *  @param  vuv   Vector3D containing the x,y,z, coordinates of the
 *                view up vector 
 */
 public void SetView( Vector3D cop, Vector3D vrp, Vector3D vuv )
 {
   String error_message = checkViewParameters( cop, vrp, vuv );

   if ( error_message == null )               // record the paramters if valid
   {
     COP = new Vector3D( cop );
     VRP = new Vector3D( vrp );
     VUV = new Vector3D( vuv );
   }
   else
     System.out.println( error_message + "in Camera.SetView()" ); 
 }


/* ------------------------------ setCOP -------------------------------- */
/**
 *  Set a new value for the center of projection.  The new COP must not be
 *  at the VRP, or so that (COP-VRP) is colinear with VUV.  If the new COP
 *  is invalid, an error message will be printed and the method call is
 *  ignored. 
 *
 *  @param  cop   Vector3D containing the x,y,z, coordinates of the
 *                center of projection
 */
 public void setCOP( Vector3D cop )
 {
   String error_message = checkViewParameters( cop, VRP, VUV );

   if ( error_message == null )               // record the paramters if valid
     COP = new Vector3D( cop );
   else
     System.out.println( error_message + "in Camera.SetCOP()" );
 }


/* ------------------------------ getCOP -------------------------------- */
/**
 *  Get a copy of the current center of projection.  
 *
 *  @return  A copy of the Vector3D containing the x,y,z, coordinates of the
 *           center of projection
 */
 public Vector3D getCOP()
 {
   return new Vector3D( COP );
 }


/* ------------------------------ setVRP -------------------------------- */
/**
 *  Set a new value for the view reference point.  The new VRP must not be
 *  at the COP, or so that (COP-VRP) is colinear with VUV.  If the new VRP 
 *  is invalid, an error message will be printed and the method call is
 *  ignored. 
 *
 *  @param  vrp   Vector3D containing the x,y,z, coordinates of the
 *                view reference point 
 */
 public void setVRP( Vector3D vrp )
 {
   String error_message = checkViewParameters( COP, vrp, VUV );

   if ( error_message == null )               // record the paramters if valid
     VRP = new Vector3D( vrp );
   else
     System.out.println( error_message + "in Camera.SetVRP()" );
 }


/* ------------------------------ getVRP -------------------------------- */
/**
 *  Get a copy of the current view reference point.  
 *
 *  @return  A copy of the Vector3D containing the x,y,z, coordinates of the
 *           view reference point 
 */
 public Vector3D getVRP()
 {
   return new Vector3D( VRP );
 }


/* ------------------------------ setVUV -------------------------------- */
/**
 *  Set a new value for the view up vector.  The new VUV must not be
 *  colinear with (COP-VRP).  If the new VUV is invalid, an error message 
 *  will be printed and the method call is ignored. 
 *
 *  @param  vuv   Vector3D containing the x,y,z, coordinates of the
 *                view up vector 
 */
 public void setVUV( Vector3D vuv )
 {
   String error_message = checkViewParameters( COP, VRP, vuv );

   if ( error_message == null )               // record the paramters if valid
     VUV = new Vector3D( vuv );
   else
     System.out.println( error_message + "in Camera.SetVUV()" );
 }


/* ------------------------------ getVUV -------------------------------- */
/**
 *  Get a copy of the current view up vector.  
 *
 *  @return  A copy of the Vector3D containing the x,y,z, coordinates of the
 *           view up vector 
 */
 public Vector3D getVUV()
 {
   return new Vector3D( VUV );
 }


/* --------------------------- RotateAroundVRP --------------------------- */
/**
 *  Set a new value for the COP and VUV by rotating the virtual camera 
 *  through the specified angle, about the specified axis through the 
 *  VRP.  (NOTE: The scene will appear to rotate in the opposite direction.)
 *
 *  @param  angle The rotation angle in degrees.
 */
 public void RotateAroundVRP( float angle, Vector3D axis )
 {
   if ( Float.isNaN( angle ) )
   {
     System.out.println("ERROR: angle is NaN in Camera.RotateAroundVRP()" );
     return;
   }

   if ( axis == null || axis.length() == 0 )
   {
     System.out.println("ERROR: invalid axis in Camera.RotateAroundVRP()" );
     System.out.println("Axis = " + axis );
     return;
   }
                                               // Make rotation matrix
   Tran3D rock_tran = new Tran3D();
   rock_tran.setRotation( angle, axis );
                                               // Rotate VUV
   rock_tran.apply_to( VUV, VUV );
                                               // Rotate COP about VRP
   Vector3D diff = new Vector3D( COP );
   diff.subtract( VRP );
   rock_tran.apply_to( diff, diff );
   diff.add( VRP );
   COP = diff;
 }


/* --------------------------- RotateAroundCOP --------------------------- */
/**
 *  Set a new value for the VRP and VUV by rotating the virtual camera 
 *  through the specified angle, about the specified axis through the 
 *  COP.  (NOTE: The scene will appear to rotate in the opposite direction.)
 *
 *  @param  angle The rotation angle in degrees.
 */
 public void RotateAroundCOP( float angle, Vector3D axis )
 {
   if ( Float.isNaN( angle ) )
   {
     System.out.println("ERROR: angle is NaN in Camera.RotateAroundCOP()" );
     return;
   }

   if ( axis == null || axis.length() == 0 )
   {
     System.out.println("ERROR: invalid axis in Camera.RotateAroundCOP()" );
     System.out.println("Axis = " + axis );
     return;
   }
                                               // Make rotation matrix
   Tran3D rock_tran = new Tran3D();
   rock_tran.setRotation( angle, axis );
                                               // Rotate VUV
   rock_tran.apply_to( VUV, VUV );
                                               // Rotate VRP about COP 
   Vector3D diff = new Vector3D( VRP );
   diff.subtract( COP );
   rock_tran.apply_to( diff, diff );
   diff.add( COP );
   VRP = diff;
 }


/* ---------------------------- WorldCoordMove -------------------------- */
/**
 *  Set new values for BOTH the VRP and COP by adding the specified 
 *  world coordinate vector to them.   
 *  (NOTE: The scene will appear to move in the opposite direction.)
 *
 *  @param  move_vec The world coordinate vector through which the 
 *                   VRP and COP are moved. 
 */
 public void WorldCoordMove( Vector3D move_vec )
 {
   if ( move_vec == null )
   {
     System.out.println("ERROR: invalid move_vec in Camera.WorldCoordMove()" );
     System.out.println("move_vec = " + move_vec );
     return;
   }

   VRP.add( move_vec );
   COP.add( move_vec );
 }


/* ---------------------------- UVN_CoordMove -------------------------- */
/**
 *  Set new values for BOTH the VRP and COP by adding the specified 
 *  vector, in the camera's local UVN coordnate system.  If the "n"
 *  component of the move vector is zero, this will give a "PAN"
 *  motion.  If the "u" and "v" coordinates are zero, this will give a 
 *  dolly in motion if the "n" coordinate is positive and dolly out if
 *  the "n" coordinate is negative.
 *  (NOTE: The scene will appear to move in the opposite direction.)
 *
 *  @param  move_vec The UVN coordinate vector through which the 
 *                   VRP and COP are moved. 
 */
 public void UVN_CoordMove( Vector3D move_vec )
 {
   if ( move_vec == null )
   {
     System.out.println("ERROR: invalid move_vec in Camera.UVN_CoordMove()" );
     System.out.println("move_vec = " + move_vec );
     return;
   }

   Vector3D u_comp = getU();
   Vector3D v_comp = getV();
   Vector3D n_comp = getN();

   float  components[] = move_vec.get();
   u_comp.multiply( components[0] );
   v_comp.multiply( components[1] );
   n_comp.multiply( components[2] );

   Vector3D wc_vec = new Vector3D( u_comp );
   wc_vec.add( v_comp );
   wc_vec.add( n_comp );

   WorldCoordMove( wc_vec ); 
 }


/* -------------------------------- getU ------------------------------- */
/**
  *  Calculate the local "u" vector for the camera, pointing to the right 
  *  from the camera's point of view, perpendicular to the line-of-sight.
  *
  *  @return unit vector pointing to the right from the camera's point of view.
  */
  public Vector3D getU()
  {
    Vector3D n = getN();
    Vector3D u = new Vector3D();
    u.cross( n, VUV );
    u.normalize();
    return u;
  }


/* -------------------------------- getV ------------------------------- */
/**
  *  Calculate the local "v" vector for the camera, pointing up from the 
  *  camera's point of view, perpendicular to the line-of-sight.
  *
  *  @return unit vector pointing up from the camera's point of view.
  */
  public Vector3D getV()
  {
    Vector3D n = getN();
    Vector3D u = getU();
    Vector3D v = new Vector3D();
    v.cross( u, n );
    v.normalize();
    return v;
  }


/* -------------------------------- getN ------------------------------- */
/**
  *  Calculate the local "n" vector for the camera, pointing from the 
  *  camera to the VRP, in the direction of the line-of-sight.
  *
  *  @return  unit vector pointing from the COP to the VRP
  */
  public Vector3D getN()
  {
     Vector3D n = new Vector3D( VRP );
     n.subtract( COP );
     n.normalize();
     return n;
  }


/* -------------------------------- distance ------------------------------- */
/**
  *  Get the distance from the COP to the VRP.
  *
  *  @return  the distance from the COP to the VRP
  */
  public float distance()
  {
     Vector3D n = new Vector3D( VRP );
     n.subtract( COP );
     return n.length();
  }


/* --------------------------- SetViewVolume ----------------------------- */
/**
 *  Set values for the near and far clipping planes and the view angle  
 *
 *  @param  near   the distance to the near clipping plane
 *  @param  far    the distance to the far clipping plane
 *  @param  angle  the angle (in degrees) subtended by the view volume 
 *                 in the y direction
 */
 public void SetViewVolume( float near, float far, float angle )
 {
   if ( near >= far )
   {
     System.out.println("ERROR: near >= far in " +
                        "JoglPanel.SetViewVolume()" );
     return;
   }
   if ( near <= 0 )
   {
     System.out.println("ERROR: near <= 0 in " +
                        "JoglPanel.SetViewVolume()" );
     return;
   }
   if ( angle < 0 || angle > 179 )
   {
     System.out.println("ERROR: invalid angle (" + angle + ") in " +
                        "JoglPanel.SetViewVolume()" );
     return;
   }
                       // if the parameters are usable, record them and redraw
   near_plane = near;
   far_plane  = far;
   view_angle = angle;
 }


/* ------------------------------- getNear ------------------------------ */
/**
 *  Get the distance to the near clipping plane
 *
 *  @return distance to near clipping plane
 */
  public float getNear()
  {
    return near_plane;
  }


/* -------------------------------- getFar ------------------------------ */
/**
 *  Get the distance to the far clipping plane
 *
 *  @return distance to the far clipping plane
 */
  public float getFar()
  {
    return far_plane;
  }


/* ---------------------------- getViewAngle ---------------------------- */
/**
 *  Get the nominal view angle, in degrees
 *
 *  @return the nominal view angle, subtended by the near clipping plane 
 */
  public float getViewAngle()
  {
    return view_angle;
  }


/* ------------------------- MakeViewMatrix ---------------------------- */
/** 
  *  Call gluLookAt to set up the initial ModelView matrix, RT, based on the
  *  previously specified COP, VRP and VUV vectors.  This method is not 
  *  typically called directly by application programs.  NOTE: It is assumed
  *  that the matrix mode is GL_MODELVIEW, and that the matrix stack has been
  *  reset at the time that this method is called.
  *
  *  @param drawable  The GLDrawable for the canvas.
  */
  public void MakeViewMatrix( GLDrawable drawable )
  {
    GL  gl  = drawable.getGL();

    float cop[] = COP.get();
    float vrp[] = VRP.get();
    float vuv[] = VUV.get();

    BasicGLU.gluLookAt( gl,                 // multiply in viewing tranform RT
                        cop[0], cop[1], cop[2],
                        vrp[0], vrp[1], vrp[2],
                        vuv[0], vuv[1], vuv[2] );
  }


/* ------------------------ MakeProjectionMatrix ------------------------ */
/** 
  *  Set up the projection matrix, based on the specified window dimensions 
  *  and the current view angle and clipping planes.  Concrete subclasses
  *  will implement this in different ways, depending on the type of 
  *  projection needed.  This method is not typically called directly by 
  *  application programs.  NOTE: It is assumed that the matrix mode is
  *  GL_PROJECTION.
  *
  *  @param drawable  The GLDrawable for this canvas.
  *  @param width     The width of the window in pixels
  *  @param height    The height of the window in pixels
  */
  abstract public void MakeProjectionMatrix( GLDrawable drawable,
                                             int        width,
                                             int        height );


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
  abstract public Ray getLineOfSight( int x, int y, int width, int height );


/* ------------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

/* ---------------------------- checkViewParameters ---------------------- */
/*
 *  Check that the cop, vrp and vuv are valid.  
 *
 *  @param  cop  The center of projection
 *  @param  vrp  The view reference point
 *  @param  vuv  The view up vector 
 *
 *  @return an error string if there is a problem with the parameters, or
 *          null if the parameters are ok.
 */
  private String checkViewParameters( Vector3D cop, Vector3D vrp, Vector3D vuv )
  {
    if ( cop == null || vrp == null || vuv == null )
      return "ERROR, null parameter ";

    Vector3D temp = new Vector3D( cop );
    temp.subtract( vrp );
    if ( temp.length() <= 0 )
      return "ERROR, cop and vrp are the same vector ";

    if ( vuv.length() <= 0 )
      return "ERROR, vuv has zero length ";

    temp.cross( temp, vuv );
    if ( temp.length() <= 0 )
      return "ERROR, cop-vrp is co-linear with vuv ";

    return null;
  } 

}

/*
 * File:  ArcBallCalc.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 *  $Log: ArcBallCalc.java,v $
 *  Revision 1.3  2005/08/03 16:50:04  dennis
 *  ArcBall method now reverts to ad_hoc_ArcBall in another case.
 *  The calculation of the angle of rotation using arccos(dot_prod/norms)
 *  would occasionally fail when the dot_prod/norms was essentially 1.
 *  Rounding errors would lead to 1.0000001, and the arccos would
 *  return NaN.  In this case, ad_hoc_ArcBall will now be used instead.
 *  Also made some minor additions to the java docs.
 *
 *  Revision 1.2  2005/08/02 14:46:01  dennis
 *  Removed unused imports.
 *
 *  Revision 1.1  2005/07/25 15:31:05  dennis
 *  Collection of staic methods to support "ArcBall" behaviour, panning
 *  and "dollying" in/out.  These methods can be called in response to
 *  mouse events, keyboard events, etc. to control the view in the specified
 *  panel.
 *
 */

package SSG_Tools.Viewers.Controls;

import java.awt.*;

import gov.anl.ipns.MathTools.Geometry.*;

import SSG_Tools.Viewers.*;
import SSG_Tools.RayTools.*;
import SSG_Tools.Cameras.*;


/* ---------------------------- ArcBallCalc ------------------------------ */
/**
 *  This class contains static methods to adjust the view for a JoglPanel,
 *  based on changes in pixel locations pointed at.  The changes in 
 *  pixel positions could be the result of mouse drags, keypresses, or
 *  some other action.
 */
public class ArcBallCalc 
{

   private ArcBallCalc()
   {
     // don't let anyone instantiate this method
   }


   /* ---------------------------- Pan ----------------------------- */
   /**
    *  This method rotates the camera about the COP, based on pixel position.
    *  Both the VRP and VUV rotate by the same angle.
    *  
    *  @param  x        The current x pixel position
    *  @param  y        The current y pixel position
    *  @param  last_x   The previously used x pixel position
    *  @param  last_y   The previously used y pixel position
    *  @param  panel    The panel whose camera is to be controlled by this
    *                   object.
    */
   public static void Pan(int x, int y, int last_x, int last_y, JoglPanel panel)
   {
      if ( x == last_x && y == last_y )
        return;

      Component comp = panel.getDisplayComponent();
      Dimension size = comp.getSize();

      Camera camera = panel.getCamera();
      Ray line_of_sight     = camera.getLineOfSight( x, y, 
                                                     size.width, size.height);
      Ray old_line_of_sight = camera.getLineOfSight( last_x, last_y, 
                                                     size.width, size.height);

      Vector3D v1 = old_line_of_sight.getPoint();
      v1.subtract( camera.getCOP() );

      Vector3D v2 = line_of_sight.getPoint();
      v2.subtract( camera.getCOP() );
        
      float angle = (float)Math.acos( v1.dot(v2) / (v1.length()*v2.length()));
      angle *= (float)(180/Math.PI);

      Vector3D axis = new Vector3D();
      axis.cross( v2, v1 );
      axis.normalize();

      camera.RotateAroundCOP( angle, axis ); 

      panel.Draw();
   }


   /* ---------------------------- DollyInOut ----------------------------- */
   /**
    *  This method moves the camera forward or backward along the line of
    *  sight, based on pixel position.  Both the COP and VRP move forward
    *  or backward by the same amount.
    *  
    *  @param  x        The current x pixel position
    *  @param  y        The current y pixel position
    *  @param  last_x   The previously used x pixel position
    *  @param  last_y   The previously used y pixel position
    *  @param  panel    The panel whose camera is to be controlled by this
    *                   object.
    */
   public static void DollyInOut( int       x, 
                                  int       y, 
                                  int       last_x, 
                                  int       last_y, 
                                  JoglPanel panel)
   {
      if ( x == last_x && y == last_y )
        return;

      Component comp = panel.getDisplayComponent();
      Dimension size = comp.getSize();

      Camera camera = panel.getCamera();
      float distance = (last_y - y) * camera.distance() / size.height; 
      Vector3D move = camera.getN();
      move.multiply( distance );
      camera.WorldCoordMove( move );

      panel.Draw();
   }

 
   /* ----------------------------- ArcBall ---------------------------- */
   /**
    *  This method provides an "arc ball" like rotation of a view, given
    *  a new (x,y) point on the screen, the last (x,y) point on the 
    *  screen and the world coordinate of the last point on the screen.
    *  Both the COP and VUV are rotated about the VRP by the same angle.
    *  If the point is closer to the viewing plane than the view reference
    *  point, the scene is rotated around the view reference point, as
    *  though the user "grabbed" a point on a sphere whose radius is 
    *  is equal to the distance from the VRP to world coordinate point.
    *  If the world coordinate point is further away than the VRP, then
    *  the motion is a though the user had grabbed a sphere with radius
    *  1/4 of the distance from the COP to the VRP.  If the current point
    *  (x,y) is the same as the last point (last_x, last_y) this method
    *  just returns.  If something else goes wrong with the calculation, 
    *  "ad_hoc_ArcBall" will be called.
    *  
    *  @param  x        The current x pixel position
    *  @param  y        The current y pixel position
    *  @param  last_x   The previously used x pixel position
    *  @param  last_y   The previously used y pixel position
    *  @param  wc_point The world coordinates of the last point selected
    *  @param  panel    The panel whose camera is to be controlled by this
    *                   object.
    */
   public static void ArcBall( int       x,      
                               int       y, 
                               int       last_x,  
                               int       last_y, 
                               Vector3D  wc_point,
                               JoglPanel panel )
   {
      if ( x == last_x && y == last_y )
        return;

      Component comp = panel.getDisplayComponent();
      Dimension size = comp.getSize();

      Camera camera = panel.getCamera();
      Ray line_of_sight     = camera.getLineOfSight( x, y,
                                                     size.width, size.height);
      Ray old_line_of_sight = camera.getLineOfSight( last_x, last_y,
                                                     size.width, size.height);
      //
      // The scene will be rotated around the VRP, as though the user grabbed
      // a point on a sphere, centered at the the VRP.  If the world coord
      // point is closer to the observer than the VRP, the sphere will be 
      // the sphere centered at the VRP passing through the world coord point.
      // Otherwise, the sphere will be a sphere centered at the VRP, with
      // a radius 1/4 the distance from the COP to the VRP.
      //
      Sphere ball = null;
      if ( camera.getCOP().distance( wc_point ) < camera.distance() )
      {                                                
        Vector3D diff = new Vector3D( camera.getVRP() );
        diff.subtract( wc_point );
        ball = new Sphere( camera.getVRP(), diff.length() );
      }
      else
        ball = new Sphere( camera.getVRP(), camera.distance()/4 );

      Vector3D p1 = ball.intersect( old_line_of_sight );
      Vector3D p2 = ball.intersect( line_of_sight );

      if ( p1 == null || p2 == null )
        ad_hoc_ArcBall( x, y, last_x, last_y, panel );
      else
      {
         Vector3D v1 = new Vector3D( p1 ); 
         Vector3D v2 = new Vector3D( p2 ); 
         v1.subtract( camera.getVRP() );
         v2.subtract( camera.getVRP() );
         float normalized_dot_prod = v1.dot(v2) / (v1.length()*v2.length());

                                          // due to rounding errors, this can
                                          // exceed 1, so fall back to ad_hoc 
         if ( normalized_dot_prod >= 1 )  
         {
           ad_hoc_ArcBall( x, y, last_x, last_y, panel );  
           return;
         }
 
         float angle = (float)Math.acos( normalized_dot_prod );
         angle *= (float)(180/Math.PI);

         Vector3D axis = new Vector3D();
         axis.cross( v2, v1 );
         axis.normalize();

         camera.RotateAroundVRP( angle, axis );

         panel.Draw();
      }
   }


   /* ------------------------- ad_hoc_ArcBall -------------------------- */
   /**
    *  This method provides an "arc ball" like rotation of a view, given
    *  a new (x,y) point on the screen and the last (x,y) point on the screen.
    *  Both the COP and VUV are rotated about the VRP by the same angle.
    *  The motion is calculated independently of what is visible
    *  on the screen.  If the points are in the central portion of the 
    *  screen the motion of the camera is up/down and left/right. 
    *  If the points are in the outer portion of the screen, the motion is
    *  a roll around the line of sight.  If the points are between the 
    *  central and outer portions, the motion is a blended between the two
    *  inner and outer type of motion.  The inner and outer portions of
    *  the screen are defined in terms of the diagonal size.  Points closer
    *  to the center than 1/10 the diagonal are in the central portion.
    *  Points further away than 1/3 the diagonal are in the outer portion.
    *  
    *  @param  x       The current x pixel position
    *  @param  y       The current y pixel position
    *  @param  last_x  The previously used x pixel position
    *  @param  last_y  The previously used y pixel position
    *  @param  panel   The panel whose camera is to be controlled by this
    *                  object.
    */ 
   public static void ad_hoc_ArcBall( int x,      int y, 
                                      int last_x, int last_y, 
                                      JoglPanel panel )
   {
      if ( x == last_x && y == last_y )
        return;

      Component comp = panel.getDisplayComponent();

      Dimension size = comp.getSize();
      float diag = (float)Math.sqrt( size.width  * size.width +
                                     size.height * size.height );

      float  mid_x   = size.width/2.0f;
      float  mid_y   = size.height/2.0f;

      float  delta_x = x - last_x;
      float  delta_y = y - last_y;
      float  dist_to_center = (float)Math.sqrt( (y-mid_y)*(y-mid_y) +
                                                (x-mid_x)*(x-mid_x) );
      Camera camera = panel.getCamera();

      float threshold_1 = diag/10;
      float threshold_2 = diag/3;
      float roll_blend = 0;
      if ( dist_to_center > threshold_1 && dist_to_center <= threshold_2 )
        roll_blend = ( dist_to_center - threshold_1) /
                     ( threshold_2    - threshold_1 );
      else if ( dist_to_center > threshold_2 )
        roll_blend = 1;
      float uv_blend   = 1 - roll_blend;

      float scale = 2/diag;
      float u_angle = (float)(delta_y * 180/Math.PI * scale * uv_blend);
      float v_angle = (float)(delta_x * 180/Math.PI * scale * uv_blend);
      camera.RotateAroundVRP( -u_angle, camera.getU() );
      camera.RotateAroundVRP( -v_angle, camera.getV() );

                                 // Now fix VUV to be perpendicular to u and n
                                 // and do roll motion through a "blended"
                                 // angle of rotation, about the "n" vector.
      camera.setVUV( camera.getV() );

                                 // find change in angle around center
                                 // from the last to the current point

      float  theta      = (float)Math.atan2( y - mid_y, x - mid_x );
      float  last_theta = (float)Math.atan2( last_y - mid_y, last_x - mid_x );
      float  delta_theta = last_theta - theta;
      if ( delta_theta > Math.PI )
        delta_theta = (float)( delta_theta - 2*Math.PI  );
      else if ( delta_theta < -Math.PI )
        delta_theta = (float)( delta_theta + 2*Math.PI  );

      camera.RotateAroundVRP( delta_theta * 180/(float)Math.PI * roll_blend,
                              camera.getN() );
      panel.Draw();
   }

}

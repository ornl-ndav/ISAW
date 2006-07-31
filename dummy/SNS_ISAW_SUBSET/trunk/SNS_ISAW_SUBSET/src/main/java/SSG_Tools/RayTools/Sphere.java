/*
 * File:  Sphere.java
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
 *  $Log: Sphere.java,v $
 *  Revision 1.2  2005/08/02 23:02:39  dennis
 *  Fixed javadoc error.
 *
 *  Revision 1.1  2005/07/18 16:53:55  dennis
 *  Initial version of class describining primitive shape, with
 *  methods for finding intersections with "rays" and a reflected
 *  ray.
 *
 *
 */
package SSG_Tools.RayTools;

import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This class represents an infinite sphere by a center point and
 *  a radius.  Basic intersection and reflection methods for
 *  a ray intersecting a sphere are included.
 */
public class Sphere 
{
  Vector3D  center;
  float    radius;


/* ---------------------------- default constructor ---------------------- */
/**
 *  Construct a Sphere of radius 1 at the origin, by default
 */
  public Sphere()
  {
    center = new Vector3D();
    radius = 1;
  }


/* ------------------------------ constructor --------------------------- */
/**
 *  Construct the Sphere, centered at the specified point and with the
 *  specified radius.
 *
 *  @param  center   the center of the newly created sphere.
 *  @param  radius   the radius of the newly created sphere.
 */
  public Sphere( Vector3D center, float radius )
  {
    this.center = new Vector3D( center );
    this.radius = radius;
  }


/* ------------------------------- setCenter ---------------------------- */
/**
 *  Set the center of this sphere to the specified point.
 *
 *  @param center new point that will be used to define the center of the
 *                sphere.
 */
  public void setCenter( Vector3D center )
  {
    this.center = new Vector3D( center );
  }


/* ------------------------------- getCenter ---------------------------- */
/**
 *  Get the center of this sphere.
 *
 *  @return  the center of this sphere.
 */
  public Vector3D getCenter()
  {
    return new Vector3D( center );
  }


/* ------------------------------- setRadius ---------------------------- */
/**
 *  Set the radius of this sphere to the specified value.
 *
 *  @param radius the new value for the radius of this sphere.
 */
  public void setRadius( float radius )
  {
    this.radius = radius;
  }


/* ------------------------------- getRadius ---------------------------- */
/**
 *  Get the current radius of this sphere.
 *  
 *  @return the radius for this sphere.
 */
  public float getRadius()
  {
    return radius;
  }


/* ------------------------------- intersect --------------------------- */
/**
 *  Find the point of intersection of the specified Ray with this sphere.
 *
 *  @param  ray    The ray for which the intersction with this sphere is 
 *                 calculated.
 *
 *  @return the point of intersection of the Ray with this sphere, or null
 *          if the Ray does not intersect the sphere.
 */
  public Vector3D intersect( Ray ray )
  {
    Vector3D point      = ray.getPoint();
    Vector3D direction  = ray.getDirection();

    Vector3D diff = new Vector3D( point );
    diff.subtract( center );
                                           // find coefficients for quadratic
    float a = direction.dot( direction );
    float b = 2 * diff.dot( direction );
    float c = diff.dot( diff ) - radius * radius;

    float t = (float)RayMath.MinPosRootOfQuadratic( a, b, c );

    if ( Float.isNaN(t) )
      return null;

    direction.multiply( t );

    point.add( direction );
    return point;
  }


/* ------------------------------- reflect --------------------------- */
/**
 *  Find the reflected ray when the specified ray hits this Sphere.
 *
 *  @param  ray    The ray for which the reflection from this Sphere is 
 *                 calculated.
 *
 *  @return the reflected Ray.
 */
  public Ray reflect( Ray ray )
  {                                       // find reflection from tangent plane
     Vector3D intersect_pt = intersect( ray );
     Vector3D normal = new Vector3D( intersect_pt );
     normal.subtract( center );

     Plane plane = new Plane( intersect_pt, normal );
     return plane.reflect( ray ); 
  }


/* ------------------------------- distance --------------------------- */
/**
 *  Find the distance from the specified point to this Sphere.  The
 *  distance will be negative, zero or positive, depending on whether
 *  the point is in, on, or outside of the sphere.
 *
 *  @param  a_point  The point whose distance to the Sphere is calculated.
 *
 *  @return the distance from the point to this Sphere.
 */
  public float distance( Vector3D a_point )
  {
     float dist = center.distance( a_point );
     return dist-radius;
  }


/* ------------------------------- toString --------------------------- */
/**
 *  Get a multiline string listing the center and radius that define
 *  this sphere.
 *  
 *  @return a multiline string giving the center and radius. 
 */
  public String toString()
  {
    return "Center: " + center.toString() + "\n" +
           "Radius: " + radius;
  }


/* ------------------------------ main ---------------------------------- */
/**
 *  Main program with basic tests for the methods of this class.
 */
  public static void main( String args[] )
  {
     Ray ray = new Ray( new Vector3D( 0, 2, 0 ),
                        new Vector3D( 1, 0, 0 ) );
     Sphere sphere = new Sphere( new Vector3D( 10, 0, 0 ), 4 );

     System.out.println("Ray ............... \n" + ray );
     System.out.println("Sphere ............ \n" + sphere );

     System.out.println( "Intersect at " + sphere.intersect( ray ) );

     Ray reflected_ray = sphere.reflect( ray );
     System.out.println( "Reflected " + reflected_ray );
  }

}

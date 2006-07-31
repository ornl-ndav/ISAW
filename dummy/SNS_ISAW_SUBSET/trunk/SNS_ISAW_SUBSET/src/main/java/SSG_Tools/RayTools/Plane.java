/*
 * File:  Plane.java
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
 *  $Log: Plane.java,v $
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
 *  This class represents an infinite plane by a point on the plane and
 *  a normal direction.  Basic intersection and reflection methods for
 *  a ray intersecting a plane are included.
 */
public class Plane 
{
  Vector3D  point;
  Vector3D  normal;


/* ---------------------------- default constructor ---------------------- */
/**
 *  Construct the xy plane by default
 */
  public Plane()
  {
    point  = new Vector3D();
    normal = new Vector3D( 0, 0, 1 );
  }


/* ------------------------------ constructor --------------------------- */
/**
 *  Construct the plane passing through the specified point and with the
 *  specified plane normal.
 *
 *  @param  point   A point on the plane
 *  @param  normal  A vector normal to the the plane 
 */
  public Plane( Vector3D point, Vector3D  normal )
  {
    this.point  = new Vector3D( point );
    this.normal = new Vector3D( normal );
    this.normal.normalize();
  }


/* ------------------------------- setPoint ---------------------------- */
/**
 *  Set the point used to define this plane to the specified point.
 *
 *  @param point  new point on the plane, that will be used to define 
 *                plane.
 */
  public void setPoint( Vector3D point ) 
  {
    this.point = new Vector3D( point );
  }


/* ------------------------------- getPoint ---------------------------- */
/**
 *  Get a copy of the point on this plane that is used to define the plane.
 *
 *  @return a copy of the point used to define the plane.
 */
  public Vector3D getPoint()
  {
    return new Vector3D( point );
  }


/* ------------------------------- setNormal --------------------------- */
/**
 *  Set the normal used to define this plane to the specified vector.
 *
 *  @param normal  new normal vector that will be used to define the plane. 
 */
  public void setNormal( Vector3D  normal )
  {
    this.normal = new Vector3D( normal );
    this.normal.normalize();
  }


/* ------------------------------- getNormal --------------------------- */
/**
 *  Get a copy of the normal that is used to define this plane.
 *  
 *  @return a copy of the normal vector that is used to define the plane. 
 */
  public Vector3D getNormal()
  {
    return new Vector3D( normal );
  }


/* ------------------------------- intersect --------------------------- */
/**
 *  Find the point of intersection of the specified Ray with this plane.
 *
 *  @param  ray    The ray for which the intersction with this plane is 
 *                 calculated.
 *
 *  @return the point of intersection of the Ray with this plane, or null
 *          if the Ray does not intersect the plane.
 */
  public Vector3D intersect( Ray ray )
  {
     Vector3D  direction = ray.getDirection();

     Vector3D  diff = new Vector3D( point );      // diff is vector from ray
     diff.subtract( ray.getPoint() );             // start point to plane point

                                                  // first check degenerate 
                                                  // cases with ray parallel
                                                  // to the plane.
     float normal_component = direction.dot( normal );
     if ( normal_component == 0 )                 // ray is parallel to plane
     {
        if ( diff.dot( normal ) == 0 )            // ray is in plane, so just
          return ray.getPoint();                  // use start point of ray
        else
          return null;                            // ray parallel, but outside
                                                  // of plane.
     }
                                                  // now for the "typical" case
                                                  // we calculate the point of
                                                  // intersection
     float t = diff.dot( normal ) / direction.dot( normal );
     direction.multiply( t );

     Vector3D intersect_pt = ray.getPoint();
     intersect_pt.add( direction );
     return intersect_pt;       
  }


/* ------------------------------- reflect --------------------------- */
/**
 *  Find the reflected ray when the specified ray hits this plane.
 *
 *  @param  ray    The ray for which the reflection from this plane is 
 *                 calculated.
 *
 *  @return the reflected Ray.
 */
  public Ray reflect( Ray ray )
  {
     Vector3D intersect_pt = intersect( ray );

     Vector3D  n = new Vector3D( normal );
     if ( normal.dot( ray.getDirection() ) <= 0 )     // outward normal so make
       n.multiply( -1 );                              // it an inward normal
 
     float normal_component = n.dot( ray.getDirection() );
     n.multiply( normal_component );
     
     Vector3D new_direction = ray.getDirection();
     new_direction.subtract( n );
     new_direction.multiply( 2 );
     
     new_direction.subtract( ray.getDirection() );
 
     return new Ray( intersect_pt, new_direction ); 
  }


/* ------------------------------- distance --------------------------- */
/**
 *  Find the distance from the specified point to this plane. 
 *
 *  @param  a_point  The point whose distance to the plane is calculated.
 *
 *  @return the distance from the point to this plane.
 */
  public float distance( Vector3D a_point )
  {
     Vector3D diff = new Vector3D( a_point );
     diff.subtract( point );
     float dist = Math.abs( diff.dot( normal) );
     return dist; 
  }


/* ------------------------------- toString --------------------------- */
/**
 *  Get a multiline string listing the point and normal that define
 *  this plane.
 *  
 *  @return a multiline string giving the point and normal vectors. 
 */
  public String toString()
  {
    return "Point:  " + point.toString() + "\n" +
           "Normal: " + normal.toString();
  }


/* ------------------------------ main ---------------------------------- */
/**
 *  Main program with basic tests for the methods of this class.
 */
  public static void main( String args[] )
  {
     Ray   ray   = new Ray(   new Vector3D( 2, 3, 4 ), 
                              new Vector3D( 1, 1, 1 ) );

     Plane plane = new Plane( new Vector3D( 5, 0, 0 ), 
                              new Vector3D( 1, 0, 0 ) );

     System.out.println("Ray ............... \n" + ray );
     System.out.println("Plane ............. \n" + plane );

     System.out.println( "Intersect at " + plane.intersect( ray ) );

     Ray reflected_ray = plane.reflect( ray );
     System.out.println( "Reflected Ray...... \n " + reflected_ray );
  }

}

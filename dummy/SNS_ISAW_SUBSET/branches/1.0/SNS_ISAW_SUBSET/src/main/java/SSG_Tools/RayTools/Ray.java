/*
 * File:  Ray.java
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
 *  $Log: Ray.java,v $
 *  Revision 1.1  2005/07/18 16:52:12  dennis
 *  Initial version of class encapsulating a "ray" consisting of an
 *  initial 3D point and a 3D vector.
 *
 *
 */
package SSG_Tools.RayTools;

import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This class represents a "ray" consisting of a point of origin and a
 *  direction vector.
 */

public class Ray
{
  private Vector3D  point;
  private Vector3D  direction;


/* ------------------------- default constructor ------------------------- */
/**
 *  Construct a default ray, starting at the origin pointing in the direction
 *  of the positive x axis.
 */
  public Ray()
  {
    point     = new Vector3D( 0, 0, 0 );
    direction = new Vector3D( 1, 0, 0 );
  }


/* ------------------------------ constructor --------------------------- */
/**
 *  Construct a ray, starting at the specified point and pointing in the 
 *  specified direction.
 *
 *  @param point       Vector giving the coordinates of the starting point for
 *                     the ray.
 *  @param direction   Vector giving the direction of the ray.
 */
  public Ray( Vector3D point, Vector3D  direction )
  {
    this.point     = new Vector3D( point );
    this.direction = new Vector3D( direction );
    this.direction.normalize();
  }


/* ------------------------------ setPoint ---------------------------- */
/**
 *  Change the starting point for this ray to the specified point.
 *
 *  @param  point    Vector giving the new coordinates of the starting
 *                   point for the ray.
 */
  public void setPoint( Vector3D point )
  {
    this.point = new Vector3D( point );
  }


/* ------------------------------ getPoint ---------------------------- */
/**
 *  Get the starting point for this ray.
 *
 *  @return  Vector giving the coordinates of the starting point for this ray.
 */
  public Vector3D getPoint()
  {
    return new Vector3D( point );
  }


/* ---------------------------- setDirection -------------------------- */
/**
 *  Change the direction for this ray to the specified direction.
 *
 *  @param direction   Vector giving the new direction of the ray.
 */
  public void setDirection( Vector3D  direction )
  {
    this.direction = new Vector3D( direction );
    this.direction.normalize();
  }


/* ---------------------------- getDirection -------------------------- */
/**
 *  Get the direction for this ray.
 *
 *  @return  Vector giving direction of this ray.
 */
  public Vector3D getDirection()
  {
    return new Vector3D( direction );
  }


/* ------------------------------ toString ---------------------------- */
/**
 *  Get get a string form of the point and direction for this ray.
 *
 *  @return  a multi-line String specifying the point and direction 
 *           for this ray.
 */
  public String toString()
  {
    return "Point:     " + point.toString() + "\n" +
           "Direction: " + direction.toString();
  }

}

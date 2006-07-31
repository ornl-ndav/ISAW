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
 *  $Log: RayMath.java,v $
 *  Revision 1.1  2005/07/18 16:54:40  dennis
 *  Convenience class for some basic calculations needed for Rays.
 *
 *
 */
package SSG_Tools.RayTools;

import gov.anl.ipns.MathTools.Geometry.*;

public class RayMath
{

  /* ------------------------- default constructor ---------------------- */
  /**
   *  Private constructor, so this class can't be instantiated
   */ 
  private RayMath()
  {
    // This class has static methods and shouldn't be instantiated
  }


  /* ------------------------ MinPosRootOfQuadratic ---------------------- */
  /**
   *  Find the smallest positive root of a quadratic equation, if possible,
   *  else return Double.NaN
   *
   *  @param  a      quadratic coefficient of quadratic equation
   *  @param  b      linear    coefficient of quadratic equation
   *  @param  c      constant  coefficient of quadratic equation
   */
  public static double MinPosRootOfQuadratic( double a, double b, double c )
  {
    if ( a == 0 )                          // equation is at most linear
    {
      if ( b == 0 )                        // equation is actually constant
      {
         if ( c == 0 )                     // any number is a solution
           return 1;
         else                              // no solution
           return Double.NaN;
      }
      else
      {
        if ( -c/b > 0 )
          return -c/b;                     // positive solution to linear eqn.
        else
          return Double.NaN;               // no positive solution
      }
    }
                                           // use quadratic equation to find
    double disc = b*b - 4 * a * c;         // real point of intersection, if
                                           // any
    if ( disc <= 0 )
      return Double.NaN;

     double t1 = (-b - Math.sqrt( disc )) / (2*a);
     double t2 = (-b + Math.sqrt( disc )) / (2*a);

     double min = Math.min( t1, t2 );
     double max = Math.max( t1, t2 );

     if ( max < 0 )
       return Double.NaN;

     if ( min > 0 )
       return min;
     else
       return max;
  }


  /* --------------------------- PerpVector ---------------------------- */
  /**
   *  Find a unit vector that is perpendicular to the specified vector.
   *
   *  @param  vec  The vector for which a perpendicular vector is to
   *               be constructed.
   *
   *  @return  A vector that is perpendicular to the specified vector.
   */
  public static Vector3D PerpVector( Vector3D vec )
  {
     Vector3D u = new Vector3D( vec );
     u.normalize();
                                                    // first find a vector
     float dot_prods[] = new float[3];              // that is not collinear
     dot_prods[0] = u.dot( new Vector3D(1,0,0) );   // by choosing a coord
     dot_prods[1] = u.dot( new Vector3D(0,1,0) );   // axis vector with the
     dot_prods[2] = u.dot( new Vector3D(0,0,1) );   // smallest dot product
                                                    // with the given vector
     int min_index = 0;
     float min = dot_prods[0];
     for ( int i = 1; i < 3; i++ )
       if ( dot_prods[i] < min )
       {
         min_index = i;
         min = dot_prods[i];
       }

     float arr[] = new float[4];
     for ( int i = 0; i < 3; i++ )
       if ( i == min_index )
         arr[i] = 1;
       else
         arr[i] = 0;
     arr[3] = 1;
     Vector3D temp = new Vector3D( arr );
                                                      // now make perpendicular 
     temp.cross( u, temp );
     temp.normalize();
     return temp; 
  }


  /**
   *  Main program to perform some basic tests of the methods in this class.
   */
  public static void main( String args[] )
  {
    Vector3D  u = new Vector3D( 0.5f, .866f, 0 );
    Vector3D  v = PerpVector( u );
 
    System.out.println("u =   " + u );
    System.out.println("v =   " + v );
    System.out.println("dot = " + u.dot(v) );

    System.out.println("Min positive root of x^2 - x - 6 is ");
    System.out.println( MinPosRootOfQuadratic( 1, -1, -6 ) );

    System.out.println("Min positive root of x^2 - 5x + 6 is ");
    System.out.println( MinPosRootOfQuadratic( 1, -5, 6 ) );

    System.out.println("Min positive root of 2x^2 - 5x + 1 is ");
    System.out.println( MinPosRootOfQuadratic( 2, -5, 1 ) );

    System.out.println("Min positive root of - 5x + 1 is ");
    System.out.println( MinPosRootOfQuadratic( 0, -5, 1 ) );

    System.out.println("Min positive root of 5x + 1 is ");
    System.out.println( MinPosRootOfQuadratic( 0, 5, 1 ) );

    System.out.println("Min positive root of 0x + 1 is ");
    System.out.println( MinPosRootOfQuadratic( 0, 0, 1 ) );

    System.out.println("Min positive root of 0x + 0 is ");
    System.out.println( MinPosRootOfQuadratic( 0, 0, 0 ) );
  }

}

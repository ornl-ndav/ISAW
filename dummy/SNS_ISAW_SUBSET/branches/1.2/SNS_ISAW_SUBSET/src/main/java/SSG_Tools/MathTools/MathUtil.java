/*
 * File:  MathUtil.java
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
 *  $Log$
 *  Revision 1.2  2007/08/26 23:23:20  dennis
 *  Updated to latest version from UW-Stout repository.
 *
 *  Revision 1.3  2007/08/25 03:57:28  dennis
 *  Adjusted logic in adjusting start value, to remove empty statement.
 *
 *  Revision 1.2  2006/09/24 05:35:55  dennis
 *  Added method subDivide() to get a "nice" spacing of tick
 *  marks along a calibrated axis.  This method was extracted
 *  and adapted from the CalibrationUtil class of ISAW.
 *
 *  Revision 1.1  2005/12/07 03:41:16  dennis
 *  Moved this generally useful class from "BallWorld" demo.
 */
package SSG_Tools.MathTools;

/**
 *  This class implements a few utilities for finding solutions of equations. 
 *  Specifically, utilities to find the smallest positive and largest negative
 *  solution to a quadratic equation are provided, as is the bisection method
 *  for an approximate zero of a more general function.
 */

public class MathUtil
{

  /* --------------------------- Constructor --------------------------- */
  /**
   *  This class contains only static methods, so the default constructor
   *  is private.
   */
  private MathUtil()
  {
     // private constructor prevents instantiation
  }


  /* ------------------------ MinPosRootOfQuadratic ----------------------- */
  /**
   *  Find the smallest positive root of a quadratic equation, if possible,
   *  else return Double.NaN
   *
   *  @param  a     The coefficient on x^2 in the quadratic equation.
   *  @param  b     The coefficient on x in the quadratic equation.
   *  @param  c     The constant coefficient in the quadratic equation.
   *
   *  @return The smallest positive root of the equation a*x^2 + b*x + c = 0, 
   *          if there is one.  If not Double.NaN is returned.
   */
  public static double MinPosRootOfQuadratic( double a, double b, double c )
  {
    if ( a == 0 )                          // degenerate case
    {
      if ( b == 0 )
        return Double.NaN;                 // either every x is a solution of
                                           // no x is a solution
      double root = -c/b;
      if ( root > 0 )
        return root;
     
      return Double.NaN;                   // no POSITIVE root
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
     
     return max;
  }


  /* ------------------------ MaxNegRootOfQuadratic ----------------------- */
  /**
   *  Find the largest negative root of a quadratic equation, if possible,
   *  else return Double.NaN
   *
   *  @param  a     The coefficient on x^2 in the quadratic equation.
   *  @param  b     The coefficient on x in the quadratic equation.
   *  @param  c     The constant coefficient in the quadratic equation.
   *
   *  @return The largest negative root of the equation a*x^2 + b*x + c = 0, 
   *          if there is one.  If not Double.NaN is returned.
   */
  public static double MaxNegRootOfQuadratic( double a, double b, double c )
  {
    if ( a == 0 )                          // degenerate case, not a quadratic
    {
      if ( b == 0 )
        return Double.NaN;                 // either every x is a solution of
                                           // no x is a solution
      double root = -c/b;

      if ( root < 0 )
        return root;
      
      return Double.NaN;                   // no NEGATIVE root
    }
                                           // use quadratic equation to find
    double disc = b*b - 4 * a * c;         // real point of intersection, if
                                           // any
    if ( disc <= 0 )
      return Double.NaN;

     double root = Math.sqrt( disc );
     double t1 = (-b - root ) / (2*a);
     double t2 = (-b + root ) / (2*a);

     double min = Math.min( t1, t2 );
     double max = Math.max( t1, t2 );

     if ( min > 0 )
       return Double.NaN;

     if ( max < 0 )
       return max;
     
     return min;
  }

  
  /* -------------------------- BisectionSolution -------------------------- */
  /**
   *  Find the solution to f(x) = 0 on the interval [a,b], assuming that the
   *  function changes sign on the interval.
   *
   *  @param  my_f         The one variable function for which a zero is to 
   *                       be found.
   *  @param  a            The left hand endpoint of the interval.
   *  @param  b            The right hand endpoint of the interval.
   *  @param  tolerance_f  The maximum allowable size of |my_f(x)|.
   *
   *  @return An approximate solution to f(x) = 0 on [a,b], or Double.NaN
   *          in case [a,b] is degenerate, or the function has the same
   *          sign at "a" and at "b".
   */ 
  public static double BisectionSolution( IOneVariableFunction my_f,
                                          double               a, 
                                          double               b,
                                          double               tolerance_f )
  {
     if ( a >= b  || my_f.f(a) * my_f.f(b) >= 0 )
       return Double.NaN;

     int    MAX_COUNT = 20;
     int    count     = 0;
     double v1        = my_f.f( a );
     double mid       = (a + b ) / 2; 
     double v_mid     = my_f.f( mid );
 
     while ( count <= MAX_COUNT && Math.abs(v_mid) > tolerance_f )
     {
        System.out.println("a, mid, b, v1, v_mid = " 
                    + a + ", " + mid + ", " + b + ", " + v1 + ", " + v_mid );
        if ( v1 * v_mid < 0 )
          b = mid;
        else
        {
          a  = mid;
          v1 = my_f.f( a );
        }
        mid = ( a + b ) / 2;
        v_mid = my_f.f( mid );
        count++;
     }

     return mid;
  }


 /* ------------------------------- subDivide ------------------------*
  *
  * Given an interval [a,b] find a "rounded" step size "step" and a
  * "rounded" starting point "start" in [a,b], so that start+k*step
  * for k = 0,1,... gives a reasonable subdivision of [a,b].
  * However, a and b are not changed.
  *
  *  @param  xmin  the left  end point, "a"
  *  @param  xmax  the right end point, "b"
  *
  *  @return array containing step, start, and numsteps
  */
  public static float[] subDivide( double xmin, double xmax )
  {
    final double MAX_STEPS = 1000;

    double  s_diff  = 0;
    int     i_power = 0;
    double  start   = 0;
    double  step    = 0;
    float[] values  = new float[3];

    if ( xmax < xmin )                   // force the min & max values to be
    {                                    // in increasing order and different
      double temp = xmin;
      xmin = xmax;
      xmax = temp;
    }

    if ( xmax == xmin )
    {
      if ( xmin == 0 )
        xmax = 1;
      if ( xmin < 0 )
        xmin = 10 * xmin;
      if ( xmax > 0 )
        xmax = 10 * xmax;
    }
      
    s_diff = xmax - xmin;

 /* Now express the length of the interval in the form  s_diff * 10^ipower
    where s_diff is in the interval [1., 10.) */
    i_power = 0;
    while ( s_diff >= 10.0 )
    {
      s_diff = s_diff / 10.0f;
      i_power = i_power + 1;
    }
    while ( s_diff < 1.0 )
    {
      s_diff = s_diff * 10.0f;
      i_power = i_power - 1;
    }

 /* Now choose step size to give a reasonable number of subdivisions
    over an interval of length b-a. */

    if ( s_diff <= 1.2 )
      step = .1 * Math.pow(10.0, i_power );
    else if ( s_diff <= 2.0 )
      step = .2 * Math.pow( 10.0, i_power );
    else if ( s_diff <= 2.5 )
      step = .25 * Math.pow( 10.0, i_power );
    else if ( s_diff <= 5.0 )
      step = .5 * Math.pow( 10.0, i_power );
    else
      step = 1.0f * Math.pow( 10.0, i_power );

 /* Now find the first grid point in the specified interval. */

    start = xmin;
    if( start%step != 0 )   // adjust start
    {
      if ( start >= 0.0 )
        start = start - ((start%step ) - step );
      else
      {
        start = -start;
        start = start - (start%step);
        start = -start;
      }
    }
  // return the number of steps
  // NOTE: This can fail due to rounding errors in the floating point
  //       calculation.  If step is so small that it is the "noise level"
  //       of floating point arithmetic, it can happen that sum+step is
  //       no different than sum.  To avoid this, we break out of the loop
  //       when too many iterations have occurred and try to subdivide a
  //       larger interval.
    double sum = start;
    int numstep = 0;
    while( sum <= xmax && numstep < MAX_STEPS )
    {
      sum = sum + step;
      numstep++;
    }

    //System.out.println("Step = " + step );
    //System.out.println("Degree = " + i_power );
    //System.out.println("Start = " + start );
    //System.out.println("NumStep = " + numstep );

    if ( numstep < MAX_STEPS )
    {
      values[0] = (float)step;
      values[1] = (float)start;
      values[2] = numstep;
    }
    else
    {
      if ( xmin > 0 && xmax > 0 ) 
        return subDivide( xmin/5, xmax*5 );
      else if  ( xmin < 0 && xmax < 0 )
        return subDivide( xmin*5, xmax/5 );
      else
        return subDivide( xmin*5, xmax*5 );
    }

    return values;
  }



  /* -------------------------------- main ------------------------------- */
  /**
   *  Basic functionality test for the methods in this class.
   */
  public static void main( String args[] )
  {
    System.out.println("Min Positive Root of x^2 + x - 6 is " +
                        MinPosRootOfQuadratic( 1, 1, -6 ) );
    System.out.println("Max Negative Root of x^2 + x - 6 is " +
                        MaxNegRootOfQuadratic( 1, 1, -6 ) );

    IOneVariableFunction test_f = new TestFunction();

    for ( int i = -6; i <= 6; i++ )
      System.out.println("i, f(i) = " + i + ", " + test_f.f( i ) );

    System.out.println("Bisection Solution for positive root is " +
                        BisectionSolution( test_f, 1, 6, 0.00000001 ) ); 

    System.out.println("Bisection Solution for negative root is " +
                        BisectionSolution( test_f, -6, 1, 0.00000001 ) );
  }

}

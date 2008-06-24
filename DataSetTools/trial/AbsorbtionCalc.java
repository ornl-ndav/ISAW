/* 
 * File: AbsorbtionCalc.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package DataSetTools.trial;

import SSG_Tools.RayTools.*;
import gov.anl.ipns.MathTools.Geometry.*; 

/**
 *    This class has a method for the calculation of absorbtion coefficients
 *  for a sphere from basic principles.  At this point, very little has been
 *  optimized.  It just implements a brute force approximation as a proof
 *  of principle.  A bounding cube for the sphere is divided into uniform
 *  size cubes.  For each cube, if its center is inside the sphere, then
 *  the distance from the cube center to the sphere boundary is found for
 *  both the incoming neutron and the scattered neutron.  The absorbtion
 *  for that total path length is calculated.  All calculated absorbtions
 *  are averaged to find an average absorbtion value.  
 *    A large number of such cubes are needed to get values that are in 
 *  good agreement with tabulated values from the literature (Dwiggins 1975).
 *  Recognizable values are obtained with as few as 10*21*21 = 4410 cubes.
 *  Using 80*161*161 = 2,073,680 cubes gives values that should be usable.
 *  Using 400*801*801 = 256,640,400 cubes gives values that are essentially
 *  the same as Dwiggins. (Max difference about .01%)
 *    Two optimizations have been included.  First, the calculation is
 *  done only for the top half-sphere, since assuming the scattering angle
 *  is in the horizontal plane, the scattering in that direction will be the
 *  same from the bottom half sphere.  Second, it is more efficient to 
 *  calculate the table of values a column at a time (fixed angle).  In that
 *  case the list of total path lengths for that angle, is calculated once
 *  and then the average absorbtion is calculated for different muR values,
 *  using those lengths.
 *    One advantage to this simple-minded approach is that it is quite
 *  generic.  If the shape of an actual crystal is known, and that shape 
 *  is such that we can
 *    1) determine if an arbitrary point is in the crystal and
 *    2) find the path length of an arbitrary ray from such a point to the
 *    3) surface of the crystal,
 *  then it should be possible to adapt this structure to find the
 *  absorbtion for scattering in any direction.
 */
public class AbsorbtionCalc
{

  /**
   *  Approximate the absorbtion factor, A, for a sphere of radius R
   *  for scattering angle theta, using the specified number of divisions
   *  in the x,y and z directions.  The integral of exp(-mu * T) is 
   *  replaced by the integral of exp(-mu_R * T/R), which is in terms
   *  of the relative path length T/R.  The integral is only done over the
   *  top hemisphere, since the value would be the same over the lower
   *  hemisphere.
   *
   *  @param theta        The scattering angle (two times Bragg angle) 
   *                      in degrees.
   *  @param mu_R         The product of the the decay rate and the sphere 
   *                      radius.  
   *  @param n_divisions  The number of subdivisions to use in the x and y
   *                      directions. 
   */ 
  public static double A_sphere( float theta, float mu_R, int n_divisions )
  {
     Sphere   sphere      = new Sphere();
     Vector3D to_source   = new Vector3D( -1, 0, 0 );
     Vector3D to_detector = new Vector3D(  1, 0, 0 );
     Tran3D   rotation    = new Tran3D();
     rotation.setRotation( theta, new Vector3D( 0, 0, 1 ) );
     rotation.apply_to( to_detector, to_detector );

     Vector3D point;
     float delta    = 1.0f/n_divisions;
     float dv       = delta*delta*delta;
     float x,y,z;
     double A           = 0;
     double path_length = 0;
     long   n_voxels    = 0;

     for ( int i = 0; i < n_divisions; i++ )
     {
        z = (i+0.5f) * delta; 
        for ( int j = -n_divisions; j <= n_divisions; j++ )
        {
           x = j * delta;
           for ( int k = -n_divisions; k <= n_divisions; k++ )
           {
              y = k * delta;
              if ( x*x + y*y + z*z < 1 )
              { 
                point = new Vector3D( x, y, z );
                path_length = pathLength( point,
                                          to_source,
                                          to_detector,
                                          sphere );
                A += Math.exp( -mu_R * path_length );
                n_voxels += 1;
              }
           }
        }
     }
     A = A / n_voxels;
     return A; 
  }  


  /**
   *  Approximate the absorbtion factor, A, for a sphere of radius R
   *  for scattering angle theta, using the specified number of divisions
   *  in the x,y and z directions.  The integral of exp(-mu * T) is 
   *  replaced by the integral of exp(-mu_R * T/R), which is in terms
   *  of the relative path length T/R.  The integral is only done over the
   *  top hemisphere, since the value would be the same over the lower
   *  hemisphere.
   *
   *  @param mu_R         The product of the the decay rate and the sphere 
   *                      radius.  
   *  @param path_lengths List of total path lengths through the sphere
   *                      for neutrons entering the sphere from the -x
   *                      direction, and leaving the sphere at specific 
   *                      angle. 
   */
  public static double A_sphere( float mu_R, float[] path_lengths )
  {
    double A = 0;

    for ( int i = 0; i < path_lengths.length; i++ )
      A += Math.exp( -mu_R * path_lengths[i] );
    A = A / path_lengths.length;

    return A;
  }


  /**
   *  Find a list of total path lengths through the sphere for neutrons 
   *  entering the sphere from the -x direction, and leaving the sphere 
   *  at specific scattering angle, from the +x direction.
   * 
   *  @param  theta        The scattering angle (twice the Bragg angle ).
   *  @param  n_divisions  The number of divisions that will be used in
   *                       the vertical directions for the top hemisphere.
   *                       The total number of voxels tried is:
   *                       n_divisions * (n_divisions+1)^2
   *  @return a list of path length values.
   */
  public static float[] pathLengths( float theta, int n_divisions )
  {
     Sphere   sphere      = new Sphere();
     Vector3D to_source   = new Vector3D( -1, 0, 0 );
     Vector3D to_detector = new Vector3D(  1, 0, 0 );
     Tran3D   rotation    = new Tran3D();
     rotation.setRotation( theta, new Vector3D( 0, 0, 1 ) );
     rotation.apply_to( to_detector, to_detector );

     Vector3D point;
     float    length;
     float[]  lengths = new float[n_divisions * n_divisions * n_divisions * 4];
     long     n_voxels = 0;
     float delta = 1.0f/n_divisions;
     float dv    = delta*delta*delta;
     float x,
           y,
           z;

     for ( int i = 0; i < n_divisions; i++ )
     {
        z = (i+0.5f) * delta;
        for ( int j = -n_divisions; j <= n_divisions; j++ )
        {
           x = j * delta;
           for ( int k = -n_divisions; k <= n_divisions; k++ )
           {
              y = k * delta;
              if ( x*x + y*y + z*z < 1 )
              {
                point = new Vector3D( x, y, z );
                length = pathLength( point,
                                     to_source,
                                     to_detector,
                                     sphere );
                lengths[(int)n_voxels] = length;
                n_voxels += 1;
              }
           }
        }
     }
     float[] result = new float[(int)n_voxels];
     System.arraycopy( lengths, 0, result, 0, (int)n_voxels );
     return result;
  }


  /**
   *  Find the total path length for a neutron scattering from the
   *  specified point in the specified sphere. 
   *  @param  point       The center of a voxel where scattering occurs
   *  @param  to_source   Vector pointing back to the source
   *  @param  to_detector Vector point towards the detector
   *  @param  sphere      The sphere containing the point
   *
   *  @return the sum of the distance from the point to the surface of the
   *          sphere in the direction of the to_source vector PLUS the
   *          distance from the point to the surface of the sphere in the
   *          direction of the to_detector vector.
   */
  public static float pathLength( Vector3D  point,
                                  Vector3D  to_source,
                                  Vector3D  to_detector,
                                  Sphere    sphere )
  {
    Ray to_source_ray   = new Ray( point, to_source );
    Ray to_detector_ray = new Ray( point, to_detector );

    Vector3D in_vec  = sphere.intersect( to_source_ray );
    Vector3D out_vec = sphere.intersect( to_detector_ray );

    in_vec.subtract( point );
    out_vec.subtract( point );

    float path_length = in_vec.length() + out_vec.length();
    return path_length;
  }


  /**
   * Make a two-dimensional array of A* values using the specified
   * number of divisions, angles, mu_R values and max_mu_R value.
   * Angles will always run from 0 to 90 (Bragg angle).  
   *
   * @param  n_divisions   The number of divisions in the vertical direction
   *                       of the top hemisphere
   * @param  n_theta       The number of scattering angles at which the
   *                       table will be evaluated.
   * @param  n_mu_R        The number of mu_R values that will be used for
   *                       the table.
   * @param  max_mu_R      The maximum mu_R value to use for the table.
   *
   * @return A two dimension array containing the absorbtion values 
   *         A* = 1/A.
   */
  public static float[][] MakeAbsTable( int    n_divisions,
                                        int    n_theta,
                                        int    n_mu_R,
                                        float  max_mu_R )
  {
    float delta_theta = 180f / (n_theta-1);
    float delta_mu_R  = max_mu_R / (n_mu_R-1);

    float theta;
    float mu_R;
    float[][] table = new float[n_mu_R][n_theta];

    for ( int j = 0; j < n_theta; j++ )
    {
      theta = j * delta_theta;
      System.out.printf("BRAGG ANGLE = %6.2f\n", theta/2 );
      float[] lengths = pathLengths( theta, n_divisions );
      for ( int i = 0; i < n_mu_R; i++ )
      {
        mu_R = i * delta_mu_R;
        double A = A_sphere( mu_R, lengths );
        table[i][j] = (float)(1.0/A); 
        System.out.printf( "%6.2f   %7.4f\n", mu_R, table[i][j] );
      }
    }

    return table;
  }


  /**
   *  Print the specified table of absorbtion values.
   */ 
  public static void PrintAbsTable( float[][] abs_table,
                                    float     max_mu_R )
  {
    int n_mu_R  = abs_table.length;
    int n_theta = abs_table[0].length;

    float delta_theta = 180f / (n_theta-1);
    float delta_mu_R  = max_mu_R / (n_mu_R-1);

    float theta;
    float mu_R;
    
    System.out.printf("  mu_R\\th");
    for ( int j = 0; j < n_theta; j++ )
    {
      theta = j * delta_theta;
      System.out.printf(" %6.2f  ", theta/2 );  
    }    
    System.out.printf("\n");

    for ( int i = 0;  i < n_mu_R; i++ )
    {
      mu_R = i * delta_mu_R;
      System.out.printf("%7.2f", mu_R);
      for ( int j = 0; j < n_theta; j++ )
        System.out.printf("%9.4f", abs_table[i][j] );
      System.out.printf("\n");
    }

  }


  /**
   *  Test by evaluating for a sequence of mu_R and a fixed theta.
   */
  public static void Test1()
  {
    float theta = 90;
    float mu_R  = 1.0f;
    int   n_divisions = 100;
    System.out.println( "TEST 1:  THETA = " + theta + "  n_divisions = " +
                         n_divisions );
    System.out.println( "mu_R      A*");
    for ( int i = 0; i <= 25; i++ )
    {
      mu_R = i * 0.1f;
      double A = A_sphere( theta, mu_R, n_divisions );
      System.out.printf( "%4.1f   %7.4f\n", mu_R, 1/A );
    }
  }


  /**
   * Test by evaluating one A*(theta, m_R) using different numbers of
   * steps
   */
  public static void Test2()
  {
    float theta = 90;
    float mu_R  = 1.0f;
    int   n_divisions = 10;

    int      N_STEPS = 10;
    double[] vals    = new double[N_STEPS];
    System.out.println( "TEST 2: THETA = " + theta + " mu_R = " + mu_R  );
    System.out.println( "n_divisions    A*");
    for ( int i = 0; i < N_STEPS; i++ )
    {
      double A = A_sphere( theta, mu_R, n_divisions );
      vals[i] = 1/A;

      System.out.printf( "%5d   %10.6f\n", n_divisions, 1/A );
      n_divisions = (int)(1.2*n_divisions);
    }
  }

  /**
   * Test by getting an array of path lengths, then forming the
   * average of attenuations for those path lengths.
   */
  public static void Test3()
  {
    float theta = 90;
    float mu_R  = 1.0f;
    int   n_divisions = 100;
 
    n_divisions = 300;
    float[] lengths = pathLengths( theta, n_divisions );
    for ( int i = 0; i <= 25; i++ )
    {
      mu_R = i * 0.1f;
      double A = A_sphere( mu_R, lengths );
      System.out.printf( "%4.1f   %7.4f\n", mu_R, 1/A );
    }
  }

  /**
   *  Basic main program for testing, or printing a table of absorbtion 
   *  values.
   */
  public static void main( String args[] )
  {
    // Test1();
    // Test2();
    // Test3();

//  int    n_divisions = 400;
    int    n_divisions = 20;

//  int    n_theta = 37;
    int    n_theta = 19;

//  int    n_mu_R  = 51;
    int    n_mu_R  = 26;

    float  max_mu_R = 2.5f;

    float[][] table = MakeAbsTable( n_divisions, n_theta, n_mu_R, max_mu_R );
    PrintAbsTable( table, max_mu_R );
  } 

}

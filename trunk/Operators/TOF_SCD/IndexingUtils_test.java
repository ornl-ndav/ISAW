/* File: IndexingUtils_test.java 
 *
 * Copyright (C) 2011, Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */


package Operators.TOF_SCD;

import java.util.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.MathTools.*;

public class IndexingUtils_test
{
  private static boolean TS_ASSERT_EQUALS( boolean val_1, boolean val_2 )
  {
    if ( val_1 == val_2 )
      return true;
    else
    {
      System.out.println("FAILED: " + val_1 + " NOT EQUAL TO " + val_2 );
      return false;
    }
  }


  private static boolean TS_ASSERT_EQUALS( double val_1, double val_2 )
  {
    if ( val_1 == val_2 )
      return true;
    else
    {
      System.out.println("FAILED: " + val_1 + " NOT EQUAL TO " + val_2 );
      return false;
    }
  }

  private static boolean TS_ASSERT_DELTA(double val_1, double val_2, double tol)
  {
    if ( Math.abs( val_1 - val_2 ) <= tol )
      return true;
    else
    {
      System.out.println("FAILED: " + val_1 + " - " + val_2 + " exceeds "+tol);
      return false;
    }
  }


  private static Vector getNatroliteQs()
  {
    Vector q_vectors = new Vector();

    q_vectors.add( new Vector3D(-0.57582f, -0.35322f, -0.19974f ));
    q_vectors.add( new Vector3D(-1.41754f, -0.78704f, -0.75974f ));
    q_vectors.add( new Vector3D(-1.12030f, -0.53578f, -0.27559f ));
    q_vectors.add( new Vector3D(-0.68911f, -0.59397f, -0.12716f ));
    q_vectors.add( new Vector3D(-1.06863f, -0.43255f,  0.01688f ));
    q_vectors.add( new Vector3D(-1.82007f, -0.49671f, -0.06266f ));
    q_vectors.add( new Vector3D(-1.10465f, -0.73708f, -0.01939f ));
    q_vectors.add( new Vector3D(-0.12747f, -0.32380f,  0.00821f ));
    q_vectors.add( new Vector3D(-0.84210f, -0.37038f,  0.15403f ));
    q_vectors.add( new Vector3D(-0.54099f, -0.46900f,  0.11535f ));
    q_vectors.add( new Vector3D(-0.90478f, -0.50667f,  0.51072f ));
    q_vectors.add( new Vector3D(-0.50387f, -0.58561f,  0.43502f ));

    return q_vectors;
  }


  private static void test_BestFit_UB_given_lattice_parameters()
  {
    double[][] correct_UB = { {-0.0596604,  0.04964820, -0.00775391 },
                              { 0.0930100,  0.00751049, -0.04198350 },
                              {-0.1046440, -0.02161340, -0.03225860 } };

    Vector q_vectors = getNatroliteQs();

    float a =  6.5781f;
    float b = 18.2995f;
    float c = 18.6664f;
    float alpha = 90;
    float beta  = 90;
    float gamma = 90;

    float required_tolerance = 0.20f;

    Tran3D UB = new Tran3D();
    double error = IndexingUtils.BestFit_UB( UB, q_vectors, required_tolerance,
                                             a, b, c,
                                             alpha, beta, gamma );
    TS_ASSERT_DELTA( error, 0.000111616, 1e-5 );

    float[][] UB_arr = UB.get();
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
        TS_ASSERT_DELTA( UB_arr[row][col], correct_UB[row][col], 1e-5 );

    int num_indexed = IndexingUtils.NumberIndexed( UB, 
                                                   q_vectors, 
                                                   required_tolerance );
    TS_ASSERT_EQUALS( num_indexed, 12 );
  }


  private static void test_BestFit_UB()
  {
     float h_vals[]  = {  1f,  0,  0, -1,  0,  0, 1, 1 };
     float k_vals[]  = { .1f,  1,  0,  0, -1,  0, 1, 2 };
     float l_vals[]  = {-.1f,  0,  1,  0,  0, -1, 1, 3 };

     float qx_vals[]  = {  2,  0,  0, -2,  0,  0, 2,  2 };
     float qy_vals[]  = {  1,  3,  0,  0, -3,  0, 3,  6 };
     float qz_vals[]  = {  0,  0,  4,  0,  0, -4, 4, 12 };

     float[][] correct_UB = {{2.000000e+00f,  0.000000e+00f, -0.000000e+00f, 0},
                             {2.766704e-01f,  2.959570e+00f, -7.214043e-02f, 0},
                             {1.580974e-01f, -2.310306e-02f,  3.958777e+00f, 0},
                             {0,              0,              0,            1}};

     int N_INDEXED_PEAKS = 8;

     Vector q_list = new Vector();
     for ( int row = 0; row < N_INDEXED_PEAKS; row++ )
     {
       Vector3D qxyz = new Vector3D(qx_vals[row], qy_vals[row], qz_vals[row]);
       q_list.add(qxyz);
     }

     Vector hkl_list = new Vector();
     for ( int row = 0; row < N_INDEXED_PEAKS; row++ )
     {
       Vector3D hkl = new Vector3D( h_vals[row], k_vals[row], l_vals[row] );
       hkl_list.add( hkl );
     }

     Tran3D UB = new Tran3D();
     float sum_sq_error = IndexingUtils.BestFit_UB( UB, hkl_list, q_list );

     float[][] UB_returned = UB.get();

     for ( int i = 0; i < 3; i++ )
       for ( int j = 0; j < 3; j++ )
         TS_ASSERT_DELTA( UB_returned[i][j], correct_UB[i][j], 1.e-5 );

     TS_ASSERT_DELTA( sum_sq_error, 0.390147, 1e-5 );
  }


  private static void test_BestFit_Direction()
  {
    Vector index_values = new Vector();
    int correct_indices[] = { 1, 4, 2, 0, 1, 3, 0, -1, 0, -1, -2, -3 };
    for ( int i = 0; i < 12; i++ )
      index_values.add( correct_indices[i] );
    
    Vector q_vectors = getNatroliteQs();

    Vector3D best_vec = new Vector3D();
    double error = IndexingUtils.BestFit_Direction( best_vec, 
                                                    index_values, 
                                                    q_vectors );
    TS_ASSERT_DELTA( error, 0.00218606, 1e-5 );
    TS_ASSERT_DELTA( best_vec.getX(), -2.58222, 1e-4 );
    TS_ASSERT_DELTA( best_vec.getY(),  3.97345, 1e-4 );
    TS_ASSERT_DELTA( best_vec.getZ(), -4.55145, 1e-4 );
  }


  private static void test_ValidIndex()
  {
    Vector3D hkl = new Vector3D(0,0,0);
    TS_ASSERT_EQUALS( IndexingUtils.ValidIndex(hkl,0.1f), false );

    hkl.set( 2.09f, -3.09f, -2.91f );
    TS_ASSERT_EQUALS( IndexingUtils.ValidIndex(hkl,0.1f), true );

    hkl.set( 2.11f, -3.09f, -2.91f );
    TS_ASSERT_EQUALS( IndexingUtils.ValidIndex(hkl,0.1f), false );

    hkl.set( 2.09f, -3.11f, -2.91f );
    TS_ASSERT_EQUALS( IndexingUtils.ValidIndex(hkl,0.1f), false );

    hkl.set( 2.09f, -3.09f, -2.89f );
    TS_ASSERT_EQUALS( IndexingUtils.ValidIndex(hkl,0.1f), false );
  }


  private static void test_NumberIndexed()
  {

    float[][] values = { {-0.141251f,  0.3042650f, -0.147160f, 0 },
                         { 0.120633f,  0.0907082f,  0.106323f, 0 },
                         { 0.258332f, -0.0062807f, -0.261151f, 0 },
                         { 0,          0,           0,         1 } };
    Tran3D UB = new Tran3D( values );

    Vector q_list = new Vector(5);

    q_list.add( new Vector3D( -1.02753f, 0.47106f, -0.25957f ) );
    q_list.add( new Vector3D( -2.05753f, 0.93893f, -0.51988f ) );
    q_list.add( new Vector3D( -2.19878f, 1.05926f, -0.27486f ) );
    q_list.add( new Vector3D( -2.63576f, 1.39119f, -0.53007f ) );
    q_list.add( new Vector3D( -1.75324f, 1.02999f, -0.52537f ) );

    TS_ASSERT_EQUALS( IndexingUtils.NumberIndexed( UB, q_list, 0.017f ), 4 );
  }


  private static void test_GetIndexedPeaks_1D()
  {
    int correct_indices[] = { 1, 4, 2, 0, 1, 3, 0, -1, 0, -1, -2, -3 };

    Vector q_vectors = getNatroliteQs();

    Vector3D direction = new Vector3D(-2.62484f, 4.04988f, -4.46991f);
    float required_tolerance = 0.1f;
    float[] fit_error = { 0 };

    Vector index_vals = new Vector();
    Vector indexed_qs = new Vector();

    int num_indexed = IndexingUtils.GetIndexedPeaks_1D( q_vectors,
                                                        direction,
                                                        required_tolerance,
                                                        index_vals,
                                                        indexed_qs,
                                                        fit_error );
    TS_ASSERT_EQUALS( num_indexed, 12 );
    TS_ASSERT_EQUALS( index_vals.size(), 12 );
    TS_ASSERT_EQUALS( indexed_qs.size(), 12 );
    TS_ASSERT_DELTA( fit_error[0], 0.011419, 1e-5 );

    for ( int i = 0; i < index_vals.size(); i++ )
    {
      TS_ASSERT_EQUALS((Integer)(index_vals.elementAt(i)), correct_indices[i]);
    }
  }

  private static void test_GetIndexedPeaks_3D()
  {
    Vector correct_indices = new Vector();
    correct_indices.add( new Vector3D( 1,  9, -9) );
    correct_indices.add( new Vector3D( 4, 20,-24) );
    correct_indices.add( new Vector3D( 2, 18,-14) );
    correct_indices.add( new Vector3D( 0, 12,-12) );
    correct_indices.add( new Vector3D( 1, 19, -9) );
    correct_indices.add( new Vector3D( 3, 31,-13) );
    correct_indices.add( new Vector3D( 0, 20,-14) );
    correct_indices.add( new Vector3D(-1,  3, -5) );
    correct_indices.add( new Vector3D( 0, 16, -6) );
    correct_indices.add( new Vector3D(-1, 11, -7) );
    correct_indices.add( new Vector3D(-2, 20, -4) );
    correct_indices.add( new Vector3D(-3, 13, -5) );

    Vector q_vectors = getNatroliteQs();

    Vector3D direction_1 = new Vector3D(-2.58222f,3.97345f,-4.55145f);
    Vector3D direction_2 = new Vector3D(-16.6082f,-2.50165f,7.24628f);
    Vector3D direction_3 = new Vector3D(2.7609f,14.5661f,11.3343f);

    float required_tolerance = 0.1f;
    float[] fit_error = { 0 };

    Vector index_vals = new Vector();
    Vector indexed_qs = new Vector();
    int num_indexed = IndexingUtils.GetIndexedPeaks_3D( q_vectors,
                                                        direction_1,
                                                        direction_2,
                                                        direction_3,
                                                        required_tolerance,
                                                        index_vals,
                                                        indexed_qs,
                                                        fit_error );
    TS_ASSERT_EQUALS( num_indexed, 12 );
    TS_ASSERT_EQUALS( index_vals.size(), 12 );
    TS_ASSERT_EQUALS( indexed_qs.size(), 12 );
    TS_ASSERT_DELTA( fit_error[0], 0.0258739, 1e-5 );

    for ( int i = 0; i < index_vals.size(); i++ )
    {
      Vector3D hkl     = (Vector3D)( index_vals.elementAt(i) );
      Vector3D correct = (Vector3D)( correct_indices.elementAt(i) );
      TS_ASSERT_EQUALS( hkl.getX(), correct.getX() );
      TS_ASSERT_EQUALS( hkl.getY(), correct.getY() );
      TS_ASSERT_EQUALS( hkl.getZ(), correct.getZ() );
    }
  }


  private static void test_MakeHemisphereDirections()
  {
    Vector direction_list = IndexingUtils.MakeHemisphereDirections(5);

    TS_ASSERT_EQUALS( direction_list.size(), 64 );

    // check some random entries

    Vector3D vec = (Vector3D)( direction_list.elementAt(0) );
    TS_ASSERT_DELTA( vec.getX(), 0, 1e-5 );
    TS_ASSERT_DELTA( vec.getY(), 1, 1e-5 );
    TS_ASSERT_DELTA( vec.getZ(), 0, 1e-5 );

    vec = (Vector3D)( direction_list.elementAt(5) );
    TS_ASSERT_DELTA( vec.getX(), -0.154508, 1e-5 );
    TS_ASSERT_DELTA( vec.getY(),  0.951057, 1e-5 );
    TS_ASSERT_DELTA( vec.getZ(), -0.267617, 1e-5 );

    vec = (Vector3D)( direction_list.elementAt(10) );
    TS_ASSERT_DELTA( vec.getX(), 0, 1e-5 );
    TS_ASSERT_DELTA( vec.getY(), 0.809017, 1e-5 );
    TS_ASSERT_DELTA( vec.getZ(), 0.587785, 1e-5 );

    vec = (Vector3D)( direction_list.elementAt(63) );
    TS_ASSERT_DELTA( vec.getX(), -0.951057, 1e-5 );
    TS_ASSERT_DELTA( vec.getY(),  0, 1e-5 );
    TS_ASSERT_DELTA( vec.getZ(),  0.309017, 1e-5 );
  }

  private static void test_MakeCircleDirections()
  {
    int num_steps = 8;
    Vector3D axis = new Vector3D( 1, 1, 1 );
    float angle_degrees = 90;

    Vector direction_list = IndexingUtils.MakeCircleDirections( num_steps, 
                                                                axis, 
                                                                angle_degrees);
    TS_ASSERT_EQUALS( direction_list.size(), 8 );

    Vector3D vec = (Vector3D)(direction_list.elementAt(0));
    TS_ASSERT_DELTA( vec.getX(), -0.816497, 1e-5 );
    TS_ASSERT_DELTA( vec.getY(),  0.408248, 1e-5 );
    TS_ASSERT_DELTA( vec.getZ(),  0.408248, 1e-5 );

    vec = (Vector3D)(direction_list.elementAt(1));
    TS_ASSERT_DELTA( vec.getX(), -0.577350, 1e-5 );
    TS_ASSERT_DELTA( vec.getY(), -0.211325, 1e-5 );
    TS_ASSERT_DELTA( vec.getZ(),  0.788675, 1e-5 );

    vec = (Vector3D)(direction_list.elementAt(7));
    TS_ASSERT_DELTA( vec.getX(), -0.577350, 1e-5 );
    TS_ASSERT_DELTA( vec.getY(),  0.788675, 1e-5 );
    TS_ASSERT_DELTA( vec.getZ(), -0.211325, 1e-5 );

    double dot_prod;
    for ( int i = 0; i < direction_list.size(); i++ )
    {
      dot_prod = axis.dot( (Vector3D)(direction_list.elementAt(i)));
      TS_ASSERT_DELTA( dot_prod, 0, 1e-5 );
    }
  }

  private static void test_SelectDirection()
  {
    Vector3D best_direction = new Vector3D();

    Vector q_vectors = getNatroliteQs();

    Vector directions = IndexingUtils.MakeHemisphereDirections(90);

    float plane_spacing = 1.0f/6.5781f;
    float required_tolerance = 0.1f;

    int num_indexed = IndexingUtils.SelectDirection( best_direction,
                                                     q_vectors,
                                                     directions,
                                                     plane_spacing,
                                                     required_tolerance );

    TS_ASSERT_DELTA( best_direction.getX(), -0.399027, 1e-5 );
    TS_ASSERT_DELTA( best_direction.getY(),  0.615661, 1e-5 );
    TS_ASSERT_DELTA( best_direction.getZ(), -0.679513, 1e-5 );

    TS_ASSERT_EQUALS( num_indexed, 12 );
  }


  public static void main( String[] args )
  {
    test_BestFit_UB_given_lattice_parameters();
    System.out.println("Finished Test 1 ...........");    

    test_BestFit_UB();
    System.out.println("Finished Test 2 ...........");    

    test_BestFit_Direction();
    System.out.println("Finished Test 3 ...........");    

    test_ValidIndex();
    System.out.println("Finished Test 4 ...........");    
    test_NumberIndexed();
    System.out.println("Finished Test 5 ...........");    
    test_GetIndexedPeaks_1D();
    System.out.println("Finished Test 6 ...........");    
    
    test_GetIndexedPeaks_3D();
    System.out.println("Finished Test 7 ...........");    
    test_MakeHemisphereDirections();
    System.out.println("Finished Test 8 ...........");    
    test_MakeCircleDirections();
    System.out.println("Finished Test 9 ...........");    
    test_SelectDirection();
    System.out.println("Finished Test 10 ...........");    
    System.out.println("Tests Completed");
  }

}

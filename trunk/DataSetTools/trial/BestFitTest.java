/*
 * File:  BestFitTest.java
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2003/06/04 14:18:19  dennis
 * Added test of least-squares errors, timing comparison
 * between current implementation and implementation
 * based on Jama and some different test cases.
 *
 * Revision 1.1  2003/03/19 21:38:43  dennis
 * Basic functionality test and example of use of the
 * LinearAlgebra.BestFitMatrix(M,q,r) method.
 *
 */
import java.io.*;
import DataSetTools.math.*;
import DataSetTools.util.*;

public class BestFitTest
{
  public static void main( String args[] )
  {
    double q[][] = { { 1.1, 0, 0 },               // list of "q" vectors
                     { 2, 0, 0 },
                     { 3, 0, 0 },
                     { 4, 0, 0 },
                     {  .75, 0.1,  .75 },
                     { 1.50, 0, 1.50 },
                     { 2.25, 0, 2.25 },
                     { 3.00, 0, 3.00 },
                     { 0, 1, 1.6 },
                     { 0, 2, 3.0 },
                     { 0, 3, 4.5 },
                     { 0.1, 4, 6.0 } };

    double r[][] = { { 1, 0, 0 },
                     { 2, 0, 0 },
                     { 3, 0, 0 },
                     { 4, 0, 0 },
                     { 0, 1, 0 },
                     { 0, 2, 0 },
                     { 0, 3, 0 },
                     { 0, 4, 0 },
                     { 0, 0, 1 },
                     { 0, 0, 2 },
                     { 0, 0, 3 },
                     { 0, 0, 4 } };
  
    double M[][] = new double[3][3];
                                                   // save copies of originals
    float r_orig[][] = new float[r.length][3];
    for ( int row = 0; row < r.length; row++ )
      for ( int col = 0; col < 3; col++ )
        r_orig[row][col] = (float)r[row][col];

    float q_orig[][] = new float[q.length][3];
    for ( int row = 0; row < q.length; row++ )
      for ( int col = 0; col < 3; col++ )
        q_orig[row][col] = (float)q[row][col];


    ElapsedTime timer = new ElapsedTime();
    timer.reset();
    double errors = 0;
    for ( int count = 0; count < 100000; count++ )   // solve the system repeatedly to test time needed
    {
       for ( int row = 0; row < r.length; row++ )
         for ( int col = 0; col < 3; col++ )
           r[row][col] = r_orig[row][col];

       for ( int row = 0; row < q.length; row++ )
         for ( int col = 0; col < 3; col++ )
           q[row][col] = q_orig[row][col];
                                                  // find the best fit matrix
      //errors = LinearAlgebra.BestFitMatrix2( M, q, r ); 
      errors = LinearAlgebra.BestFitMatrix( M, q, r ); 
    }
    System.out.println("Time required to solve 100000 systems is: " + timer.elapsed() );
//    System.out.println("Using BestFitMatrix 3   ****");
    System.out.println("Using BestFitMatrix   ****");

    System.out.println("Residual errors are: " + errors );
    System.out.println("Matrix M is : " );
    for ( int row = 0; row < 3; row++ )
    {
      for ( int col = 0; col < 3; col++ )
        System.out.print( " " + M[row][col] + "    " );
      System.out.println();
    }

                                                 // calculate the actual errors
                                                 // between Mq and r.
    Tran3D matrix = new Tran3D();                // identity by default;
    float m[][] = matrix.get();                  // get reference to the matrix;
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
        m[row][col] = (float)M[row][col];


    float length;
    float sum_sq = 0;                            // map each original vector
    for ( int i = 0; i < q_orig.length; i++ )    // and accumulate the errors
    {
      Vector3D  vec_q = new Vector3D( q_orig[i] );
      Vector3D  m_q   = new Vector3D();
      matrix.apply_to( vec_q, m_q );
      Vector3D  vec_r = new Vector3D( r_orig[i] );
      vec_r.subtract( m_q );
      length = vec_r.length();
      sum_sq += length * length; 

      System.out.println("q = " + vec_q + ",  m_q = " + m_q );
    }

    System.out.println( "Actual errors = " + Math.sqrt( sum_sq ) );

  }
}

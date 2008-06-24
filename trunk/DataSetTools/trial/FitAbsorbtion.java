/* 
 * File: FitAbsorbtion.java
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

import java.io.*;
import java.util.*;
import gov.anl.ipns.MathTools.*;

/**
 *  This class contains routines to generate and evaluate bi-variate 
 *  polynomial approximations of degree 1,2,3,4 and 5, to tables of
 *  values.  The code is written in terms of tables of absorbtion 
 *  coefficients, but it will in fact work for any 2D table.
 */
public class FitAbsorbtion
{
  private int num_muRs; 
  private int num_angles;

  private double[]   muRs    = null;
  private double[]   angles  = null;
  private double[][] absvals = null;

  /**
   *  Load x, y and value arrays from a file, assuming the file starts with 
   *  the number of rows and columns, has a first row giving the "x" values
   *  and has a first column giving the "y" values.
   *
   *  @param filename  The name of the text file containing the table.
   */
  private void loadArrays( String filename ) throws IOException
  {
    FileReader in_file    = new FileReader( filename );
    BufferedReader buffer = new BufferedReader( in_file );
    Scanner sc            = new Scanner( buffer );

    num_muRs   = sc.nextInt(); 
    num_angles = sc.nextInt();
                 sc.nextLine();                   // skip end of line mark
            
    muRs    = new double[ num_muRs ];
    angles  = new double[ num_angles ];
    absvals = new double[ num_muRs ][ num_angles ];

    sc.next();                                   // skip mu_R\angle label
    for ( int col = 0; col < num_angles; col++ )
      angles[col] = sc.nextDouble();
    sc.nextLine();

    for ( int row = 0; row < num_muRs; row++ )
    {
      muRs[ row ] = sc.nextDouble();
      for ( int col = 0; col < num_angles; col++ )
        absvals[ row ][ col ] = sc.nextDouble();
      sc.nextLine();
    }

    sc.close();
    buffer.close();
    in_file.close();
  }


  /**
   *  Fit the bilinear polynomial  a0 + a1*x + a2*y to the data, where
   *  x is the angle and y is the muR value.  
   *
   *  @return a0, a1 and a2 as the first three entries of an array of doubles
   */
  public double[] FitBiLinear()
  {
    final int NUM_COEFF = 3;

    double angle,
           muR;
    int    n_rows = num_angles * num_muRs;
    int    index;
                                       // set up overdetermined system Ax = b

    double[][]  A = new double[ n_rows ][ NUM_COEFF ];
    double[]    b = new double[ n_rows ];
    
    index = 0;
    for ( int row = 0; row < num_muRs; row++ )
      for ( int col = 0; col < num_angles; col++ )
      {
        angle = angles[col];
        muR   = muRs[row];
        A[index][0] = 1;

        A[index][1] = angle;
        A[index][2] = muR;

        b[index]    = absvals[row][col];
        index++;
      }

    double residual = LinearAlgebra.solve( A, b );
    System.out.println("Residual = " + residual );
 
    double[] result = new double[NUM_COEFF]; 
    for ( int i = 0; i < NUM_COEFF; i++ )
      result[i] = b[i];

    return result;
  }


  /**
   *  Evaluate the bilinear approximation polynomial at the specified 
   *  angle and muR, using the coefficients returned by FitBiLinear.
   *
   *  @param  angle   The "x", ie. fractional column value, at which to
   *                  evaluate the polynomial approximation.
   *  @param  muR     The "y", ie. fractional row value, at which to
   *                  evaluate the polynomial approximation.
   *  @param  coeff   List of coefficients for the polynomial.
   *
   *  @return the bilinear approximation polynomial value at the specified
   *          point.
   */
  public double BiLinearApprox( double angle, 
                                double muR,
                                double coeff[]  )
  {
    return  coeff[0] + coeff[1] * angle + coeff[2] * muR;
  }


  /**
   *  Find the max percentage error of the BiLinear approximation at the
   *  tabulated points.
   */
  public double BiLinearMaxPercentError( double coeff[] )
  {
    double max_relative_err = 0;
    double err;
    double angle,
           muR,
           val;
    for ( int row = 0; row < num_muRs; row++ )
      for ( int col = 0; col < num_angles; col++ )
      {
        angle = angles[col];
        muR   = muRs[row];
        val   = BiLinearApprox( angle, muR, coeff );
        err   = Math.abs(( val - absvals[row][col] )/ absvals[row][col]);
        if ( err > max_relative_err )
          max_relative_err = err;
      } 
    return max_relative_err * 100;
  }
  

  /**
   *  Fit the quadratic polynomial:
   *
   *      a0 + a1 x + a2 y + a3 x^2 + a4 x y + a5 y^2
   *
   *  where x is the angle and y is the muR value.  
   *
   *  @return a0-a5 as the first six entries of an array of doubles
   */
  public double[] FitBiQuadratic()
  {
    final int NUM_COEFF = 6;

    double angle,
           muR;
    int    n_rows = num_angles * num_muRs;
    int    index;
                                       // set up overdetermined system Ax = b

    double[][]  A = new double[ n_rows ][ NUM_COEFF ];
    double[]    b = new double[ n_rows ];

    index = 0;
    for ( int row = 0; row < num_muRs; row++ )
      for ( int col = 0; col < num_angles; col++ )
      {
        angle = angles[col];
        muR   = muRs[row];
        A[index][0] = 1;

        A[index][1] = angle;
        A[index][2] = muR;

        A[index][3] = angle * angle;
        A[index][4] = angle * muR;
        A[index][5] = muR * muR;

        b[index]    = absvals[row][col];
        index++;
      }

    double residual = LinearAlgebra.solve( A, b );
    System.out.println("Residual = " + residual );

    double[] result = new double[NUM_COEFF];
    for ( int i = 0; i < NUM_COEFF; i++ )
      result[i] = b[i];

    return result;
  }


  /**
   *  Evaluate the bi-variate quadratic approximation polynomial at the 
   *  specified angle and muR, using the coefficients returned by
   *  FitBiQuadratic.
   *
   *  @param  angle   The "x", ie. fractional column value, at which to
   *                  evaluate the polynomial approximation.
   *  @param  muR     The "y", ie. fractional row value, at which to
   *                  evaluate the polynomial approximation.
   *  @param  coeff   List of coefficients for the polynomial.
   *
   *  @return the bi-variate quadratic approximation polynomial value at 
   *          the specified point.
   */
  public double BiQuadraticApprox( double angle,
                                   double muR,
                                   double coeff[]  )
  {
    return  coeff[0] + 

            coeff[1] * angle + 
            coeff[2] * muR   +

            coeff[3] * angle * angle +
            coeff[4] * angle * muR   +
            coeff[5] * muR   * muR;
  }


  /**
   *  Find the max percentage error of the BiQuadratic approximation at the
   *  tabulated points.
   */
  public double BiQuadraticMaxPercentError( double coeff[] )
  {
    double max_relative_err = 0;
    double err;
    double angle,
           muR,
           val;
    for ( int row = 0; row < num_muRs; row++ )
      for ( int col = 0; col < num_angles; col++ )
      {
        angle = angles[col];
        muR   = muRs[row];
        val   = BiQuadraticApprox( angle, muR, coeff );
        err   = Math.abs(( val - absvals[row][col] )/ absvals[row][col]);
        if ( err > max_relative_err )
          max_relative_err = err;
      }
    return max_relative_err * 100;
  }


  /**
   *  Fit the cubic polynomial:
   *
   *      a0     +
   *      a1 x   + a2 y     + 
   *      a3 x^2 + a4 x y   + a5 y^2   +
   *      a6 x^3 + a7 x^2 y + a8 x y^2 + a9 y^3
   *
   *  where x is the angle and y is the muR value.  
   *
   *  @return a0-a9 as the first 10 entries of an array of doubles
   */
  public double[] FitBiCubic()
  {
    final int NUM_COEFF = 10;

    double angle,
           muR;
    int    n_rows = num_angles * num_muRs;
    int    index;
                                       // set up overdetermined system Ax = b

    double[][]  A = new double[ n_rows ][ NUM_COEFF ];
    double[]    b = new double[ n_rows ];

    index = 0;
    for ( int row = 0; row < num_muRs; row++ )
      for ( int col = 0; col < num_angles; col++ )
      {
        angle = angles[col];
        muR   = muRs[row];
        A[index][0] = 1;

        A[index][1] = angle;
        A[index][2] = muR;

        A[index][3] = angle * angle;
        A[index][4] = angle * muR;
        A[index][5] = muR   * muR;

        A[index][6] = angle * angle * angle;
        A[index][7] = angle * angle * muR;
        A[index][8] = angle * muR   * muR;
        A[index][9] = muR   * muR   * muR;

        b[index]    = absvals[row][col];
        index++;
      }

    double residual = LinearAlgebra.solve( A, b );
    System.out.println("Residual = " + residual );

    double[] result = new double[NUM_COEFF];
    for ( int i = 0; i < NUM_COEFF; i++ )
      result[i] = b[i];

    return result;
  }


  /**
   *  Evaluate the bi-variate cubic approximation polynomial at the 
   *  specified angle and muR, using the coefficients returned by
   *  FitBiCubic.
   *
   *  @param  angle   The "x", ie. fractional column value, at which to
   *                  evaluate the polynomial approximation.
   *  @param  muR     The "y", ie. fractional row value, at which to
   *                  evaluate the polynomial approximation.
   *  @param  coeff   List of coefficients for the polynomial.
   *
   *  @return the bi-variate cubic approximation polynomial value at 
   *          the specified point.
   */
  public double BiCubicApprox( double angle,
                               double muR,
                               double coeff[]  )
  {
    return  coeff[0] +

            coeff[1] * angle +
            coeff[2] * muR   +

            coeff[3] * angle * angle +
            coeff[4] * angle * muR   +
            coeff[5] * muR   * muR   +

            coeff[6] * angle * angle * angle +
            coeff[7] * angle * angle * muR   +
            coeff[8] * angle * muR   * muR   +
            coeff[9] * muR   * muR   * muR;
  }


  /**
   *  Find the max percentage error of the BiCubic approximation at the
   *  tabulated points.
   */
  public double BiCubicMaxPercentError( double coeff[] )
  {
    double max_relative_err = 0;
    double err;
    double angle,
           muR,
           val;
    for ( int row = 0; row < num_muRs; row++ )
      for ( int col = 0; col < num_angles; col++ )
      {
        angle = angles[col];
        muR   = muRs[row];
        val   = BiCubicApprox( angle, muR, coeff );
        err   = Math.abs(( val - absvals[row][col] )/ absvals[row][col]);
        if ( err > max_relative_err )
          max_relative_err = err;
      }
    return max_relative_err * 100;
  }
 

  /**
   *  Fit the quartic polynomial:
   *
   *      a0     +
   *      a1 x   +  a2 y     + 
   *      a3 x^2 +  a4 x y   +  a5 y^2     +
   *      a6 x^3 +  a7 x^2 y +  a8 x   y^2 +  a9 y^3
   *     a10 x^4 + a11 x^3 y + a12 x^2 y^2 + a13 x y^3 + a14 y^4
   *
   *  where x is the angle and y is the muR value.  
   *
   *  @return a0-a14 as the first 15 entries of an array of doubles
   */
  public double[] FitBiQuartic()
  {
    final int NUM_COEFF = 15;

    double angle,
           muR;
    int    n_rows = num_angles * num_muRs;
    int    index;
                                       // set up overdetermined system Ax = b

    double[][]  A = new double[ n_rows ][ NUM_COEFF ];
    double[]    b = new double[ n_rows ];

    index = 0;
    for ( int row = 0; row < num_muRs; row++ )
      for ( int col = 0; col < num_angles; col++ )
      {
        angle = angles[col];
        muR   = muRs[row];
        A[index][ 0] = 1;

        A[index][ 1] = angle;
        A[index][ 2] = muR;

        A[index][ 3] = angle * angle;
        A[index][ 4] = angle * muR;
        A[index][ 5] = muR   * muR;

        A[index][ 6] = angle * angle * angle;
        A[index][ 7] = angle * angle * muR;
        A[index][ 8] = angle * muR   * muR;
        A[index][ 9] = muR   * muR   * muR;

        A[index][10] = angle * angle * angle * angle;
        A[index][11] = angle * angle * angle * muR;
        A[index][12] = angle * angle * muR   * muR;
        A[index][13] = angle * muR   * muR   * muR;
        A[index][14] = muR   * muR   * muR   * muR;

        b[index]    = absvals[row][col];
        index++;
      }

    double residual = LinearAlgebra.solve( A, b );
    System.out.println("Residual = " + residual );

    double[] result = new double[NUM_COEFF];
    for ( int i = 0; i < NUM_COEFF; i++ )
      result[i] = b[i];

    return result;
  }


  /**
   *  Evaluate the bi-variate quartic approximation polynomial at the 
   *  specified angle and muR, using the coefficients returned by
   *  FitBiQuartic.
   *
   *  @param  angle   The "x", ie. fractional column value, at which to
   *                  evaluate the polynomial approximation.
   *  @param  muR     The "y", ie. fractional row value, at which to
   *                  evaluate the polynomial approximation.
   *  @param  coeff   List of coefficients for the polynomial.
   *
   *  @return the bi-variate quartic approximation polynomial value at 
   *          the specified point.
   */
  public double BiQuarticApprox( double angle,
                                 double muR,
                                 double coeff[]  )
  {
    return  coeff[ 0] +
            coeff[ 1] * angle +
            coeff[ 2] * muR   +

            coeff[ 3] * angle * angle +
            coeff[ 4] * angle * muR   +
            coeff[ 5] * muR   * muR   +

            coeff[ 6] * angle * angle * angle +
            coeff[ 7] * angle * angle * muR   +
            coeff[ 8] * angle * muR   * muR   +
            coeff[ 9] * muR   * muR   * muR   +

            coeff[10] * angle * angle * angle * angle +
            coeff[11] * angle * angle * angle * muR   +
            coeff[12] * angle * angle * muR   * muR   +
            coeff[13] * angle * muR   * muR   * muR   + 
            coeff[14] * muR   * muR   * muR   * muR;
  }


  /**
   *  Find the max percentage error of the BiQuartic approximation at the
   *  tabulated points.
   */
  public double BiQuarticMaxPercentError( double coeff[] )
  {
    double max_relative_err = 0;
    double err;
    double angle,
           muR,
           val;
    for ( int row = 0; row < num_muRs; row++ )
      for ( int col = 0; col < num_angles; col++ )
      {
        angle = angles[col];
        muR   = muRs[row];
        val   = BiQuarticApprox( angle, muR, coeff );
        err   = Math.abs(( val - absvals[row][col] )/ absvals[row][col]);
        if ( err > max_relative_err )
          max_relative_err = err;
      }
    return max_relative_err * 100;
  }


  /**
   *  Fit the quintic polynomial:
   *
   *      a0     +
   *      a1 x   +  a2 y     + 
   *      a3 x^2 +  a4 x y   +  a5 y^2     +
   *      a6 x^3 +  a7 x^2 y +  a8 x   y^2 +  a9 y^3
   *     a10 x^4 + a11 x^3 y + a12 x^2 y^2 + a13 x y^3   + a14 y^4
   *     a15 x^5 + a16 x^4 y + a17 x^3 y^2 + a18 x^2 y^3 + a19 x y^4 + a20 y^5
   *
   *  where x is the angle and y is the muR value.  
   *
   *  @return a0-a20 as the first 21 entries of an array of doubles
   */
  public double[] FitBiQuintic()
  {
    final int NUM_COEFF = 21;

    double angle,
           muR;
    int    n_rows = num_angles * num_muRs;
    int    index;
                                       // set up overdetermined system Ax = b

    double[][]  A = new double[ n_rows ][ NUM_COEFF ];
    double[]    b = new double[ n_rows ];

    index = 0;
    for ( int row = 0; row < num_muRs; row++ )
      for ( int col = 0; col < num_angles; col++ )
      {
        angle = angles[col];
        muR   = muRs[row];
        A[index][ 0] = 1;

        A[index][ 1] = angle;
        A[index][ 2] = muR;

        A[index][ 3] = angle * angle;
        A[index][ 4] = angle * muR;
        A[index][ 5] = muR   * muR;

        A[index][ 6] = angle * angle * angle;
        A[index][ 7] = angle * angle * muR;
        A[index][ 8] = angle * muR   * muR;
        A[index][ 9] = muR   * muR   * muR;

        A[index][10] = angle * angle * angle * angle;
        A[index][11] = angle * angle * angle * muR;
        A[index][12] = angle * angle * muR   * muR;
        A[index][13] = angle * muR   * muR   * muR;
        A[index][14] = muR   * muR   * muR   * muR;

        A[index][15] = angle * angle * angle * angle * angle;
        A[index][16] = angle * angle * angle * angle * muR;
        A[index][17] = angle * angle * angle * muR   * muR;
        A[index][18] = angle * angle * muR   * muR   * muR;
        A[index][19] = angle * muR   * muR   * muR   * muR;
        A[index][20] = muR   * muR   * muR   * muR   * muR;

        b[index]    = absvals[row][col];
        index++;
      }

    double residual = LinearAlgebra.solve( A, b );
    System.out.println("Residual = " + residual );

    double[] result = new double[NUM_COEFF];
    for ( int i = 0; i < NUM_COEFF; i++ )
      result[i] = b[i];

    return result;
  }


  /**
   *  Evaluate the bi-variate quintic approximation polynomial at the 
   *  specified angle and muR, using the coefficients returned by
   *  FitBiQuintic.
   *
   *  @param  angle   The "x", ie. fractional column value, at which to
   *                  evaluate the polynomial approximation.
   *  @param  muR     The "y", ie. fractional row value, at which to
   *                  evaluate the polynomial approximation.
   *  @param  coeff   List of coefficients for the polynomial.
   *
   *  @return the bi-variate quintic approximation polynomial value at 
   *          the specified point.
   */
  public double BiQuinticApprox( double angle,
                                 double muR,
                                 double coeff[]  )
  {
    return  coeff[ 0] +

            coeff[ 1] * angle +
            coeff[ 2] * muR   +

            coeff[ 3] * angle * angle +
            coeff[ 4] * angle * muR   +
            coeff[ 5] * muR   * muR   +

            coeff[ 6] * angle * angle * angle +
            coeff[ 7] * angle * angle * muR   +
            coeff[ 8] * angle * muR   * muR   +
            coeff[ 9] * muR   * muR   * muR   +

            coeff[10] * angle * angle * angle * angle +
            coeff[11] * angle * angle * angle * muR   +
            coeff[12] * angle * angle * muR   * muR   +
            coeff[13] * angle * muR   * muR   * muR   +
            coeff[14] * muR   * muR   * muR   * muR   +

            coeff[15] * angle * angle * angle * angle * angle +
            coeff[16] * angle * angle * angle * angle * muR   + 
            coeff[17] * angle * angle * angle * muR   * muR   + 
            coeff[18] * angle * angle * muR   * muR   * muR   + 
            coeff[19] * angle * muR   * muR   * muR   * muR   + 
            coeff[20] * muR   * muR   * muR   * muR   * muR;
  } 


  /**
   *  Find the max percentage error of the BiQuintic approximation at the
   *  tabulated points.
   */
  public double BiQuinticMaxPercentError( double coeff[] )
  {
    double max_relative_err = 0;
    double err;
    double angle,
           muR,
           val;
    for ( int row = 0; row < num_muRs; row++ )
      for ( int col = 0; col < num_angles; col++ )
      {
        angle = angles[col];
        muR   = muRs[row];
        val   = BiQuinticApprox( angle, muR, coeff );
        err   = Math.abs(( val - absvals[row][col] )/ absvals[row][col]);
        if ( err > max_relative_err )
          max_relative_err = err;
      }
    return max_relative_err * 100;
  }


  /**
   *  Basic test of linear, quadratic, cubic, quartic and quintic fits.
   */
  public static void main( String args[] ) throws IOException
  {
    FitAbsorbtion fitter = new FitAbsorbtion();
    fitter.loadArrays( "AbsorbtionTable2.dat"  );
//    fitter.loadArrays( "TEST_1.dat"  );
/*
    for ( int row = 0; row < fitter.num_muRs; row++ )
      System.out.println( fitter.muRs[ row ] );

    for ( int col = 0; col < fitter.num_angles; col++ )
      System.out.println( fitter.angles[col] );
*/
    // *** Test Linear Fit
    System.out.println("\n*************** LINEAR FIT ****************");
    double[] bilinear_coeff = fitter.FitBiLinear();
    for ( int i = 0; i < bilinear_coeff.length; i++ )
      System.out.println("Coeff = " + bilinear_coeff[i] );

    System.out.println("Error = " + 
                       fitter.BiLinearMaxPercentError( bilinear_coeff ));

    System.out.println("BiLinearFit at 45 degrees is: ");
    double angle = 45;
    for ( int i = 0; i < fitter.num_muRs; i++ )
    {
      double muR = fitter.muRs[i];
      System.out.println( fitter.BiLinearApprox( angle, muR, bilinear_coeff ));
    }

    // *** Test Quadratic Fit
    System.out.println("\n*************** QUADRATIC FIT ****************");
    double[] biquad_coeff = fitter.FitBiQuadratic();
    for ( int i = 0; i < biquad_coeff.length; i++ )
      System.out.println("Coeff = " + biquad_coeff[i] );

    System.out.println("Error = " +
                       fitter.BiQuadraticMaxPercentError( biquad_coeff ));

    System.out.println("BiQuadraticFit at 45 degrees is: ");
    angle = 45;
    for ( int i = 0; i < 26; i++ )
    {
      double muR = fitter.muRs[i];
      System.out.println(fitter.BiQuadraticApprox(angle, muR, biquad_coeff));
    }


    // *** Test Cubic Fit
    System.out.println("\n*************** CUBIC FIT ****************");
    double[] bicubic_coeff = fitter.FitBiCubic();
    for ( int i = 0; i < bicubic_coeff.length; i++ )
      System.out.println("Coeff = " + bicubic_coeff[i] );

    System.out.println("Error = " +
                       fitter.BiCubicMaxPercentError( bicubic_coeff ));

    System.out.println("BiCubicFit at 45 degrees is: ");
    angle = 45;
    for ( int i = 0; i < fitter.num_muRs; i++ )
    {
      double muR = fitter.muRs[i];
      System.out.println(fitter.BiCubicApprox(angle, muR, bicubic_coeff));
    }


    // *** Test Quartic Fit
    System.out.println("\n*************** QUARTIC FIT ****************");
    double[] biquartic_coeff = fitter.FitBiQuartic();
    for ( int i = 0; i < biquartic_coeff.length; i++ )
      System.out.println("Coeff = " + biquartic_coeff[i] );

    System.out.println("Error = " +
                       fitter.BiQuarticMaxPercentError( biquartic_coeff ));

    System.out.println("BiQuarticFit at 45 degrees is: ");
    angle = 45;
    for ( int i = 0; i < fitter.num_muRs; i++ )
    {
      double muR = fitter.muRs[i];
      System.out.println(fitter.BiQuarticApprox(angle, muR, biquartic_coeff));
    }

    // *** Test Quintic Fit
    System.out.println("\n*************** QUINTIC FIT ****************");
    double[] biquintic_coeff = fitter.FitBiQuintic();
    for ( int i = 0; i < biquintic_coeff.length; i++ )
      System.out.println("Coeff = " + biquintic_coeff[i] );

    System.out.println("Error = " +
                       fitter.BiQuinticMaxPercentError( biquintic_coeff ));

    System.out.println("BiQuinticFit at 45 degrees is: ");
    angle = 45;
    for ( int i = 0; i < fitter.num_muRs; i++ )
    {
      double muR = fitter.muRs[i];
      System.out.println(fitter.BiQuinticApprox(angle, muR, biquintic_coeff));
    }

  }

} 

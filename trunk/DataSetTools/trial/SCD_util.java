/* 
 * File: SCD_util.java
 *  
 * Copyright (C) 2005     Dennis Mikkelson
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
 * Contact :  Dennis Mikkelson<mikkelsond@uwstout.edu>
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2006/01/04 22:37:25  dennis
 * Removed reference to test_ub, and use method in the current
 * set of utilities.
 *
 * Revision 1.2  2006/01/04 04:26:52  dennis
 * Modified ResidualError function.
 * It now first calculates the UB matrix, then factors the UB matrix
 * as U * B1 * Bc, where Bc is the "constraint" matrix, corresponding
 * to the specified lattice parameters.
 * Next it calculates the sum of the squares of the differences:
 *  (U * Bc * hkl - q)
 * for the specified lists of Miller indices, hkl and corresponding q values.
 *
 * Revision 1.1  2005/12/31 02:49:57  dennis
 * Class to hold static methods for SCD data reduction.
 * Initially, this just has methods for calculating the residual
 * errors in the mapping from (h,k,l) to (qx,qy,qz) determined by
 * a specified set of lattice parameters.
 *
 */

package DataSetTools.trial;

import  gov.anl.ipns.MathTools.*;

/**
 *  This class contains static utility methods for SCD data reduction.
 */

public class SCD_util
{

  /**
   * Don't instantiate this class.
   */
  private SCD_util() {}


  /* ---------------------------- ResidualError --------------------------- */
  /**
   *  This method calculates the sum squared errors in "q" space for the
   *  mapping from the specified hkl_vals to the specified q_vals, using the
   *  specified lattice parameters.  The lattice parameters are used to 
   *  construct the matrix Bc, then a least squared fit is used to determine
   *  matrix UB.  This UB matrix is factored as U1*B1*Bc.  The summed residual
   *  errors between the calculated U1*Bc*hkl_vals and the q_vals is returned
   *  as the value of this function.
   *
   *  @param  lattice_parameters  Array containing the lattice parameters
   *                              to use, in the order a, b, c, alpha, beta,
   *                              gamma.
   *  @param  hkl_vals            Nx3 array, containing the Miller indices
   *                              (h,k,l), as rows, in the same order as the
   *                              corresponding q_vals[][3].   
   *  @param  q_vals              Nx3 array, containing the q values 
   *                              (qx,qy,qz), as rows, in the same order as the
   *                              corresponding hkl_vals[][3].   
   *  @param  U1                  3x3 array in which to return the calculated
   *                              U1 matrix.
   *  @param  B1                  3x3 array in which to return the calculated
   *                              B1 matrix.
   *  @param  Bc                  3x3 array in which to return the calculated
   *                              Bc matrix.
   *
   *  @return the sum squared differences in q between the specified q_vals
   *          and U1*Bc*hkl_vals.
   */
  public static double ResidualError( double lattice_parameters[],
                                      double hkl_vals[][],
                                      double q_vals[][],
                                      double U1[][],
                                      double B1[][],
                                      double Bc[][] )
  {
    int n_peaks = hkl_vals.length;

    double A[][] = lattice_calc.A_matrix( lattice_parameters );
    LinearAlgebra.copy( LinearAlgebra.getInverse( A ), Bc ); 

    double temp_hkl_vals[][] = LinearAlgebra.copy( hkl_vals );
    double temp_q_vals[][]   = LinearAlgebra.copy( q_vals   );
    double UB[][] = new double[3][3];

    LinearAlgebra.BestFitMatrix( UB, temp_hkl_vals, temp_q_vals );

    double U1_B1[][] = LinearAlgebra.mult( UB, A );    // mult by A = Bc inverse
                                                       // to factor out Bc then
    LinearAlgebra.copy(lattice_calc.getU( U1_B1 ),U1); // get the other two 
    LinearAlgebra.copy(lattice_calc.getB( U1_B1 ),B1); // factors U1 and B1
/*
    System.out.println("U1 = ");
    LinearAlgebra.print( U1 );

    System.out.println("B1 = ");
    LinearAlgebra.print( B1 );

    System.out.println("Bc = ");
    LinearAlgebra.print( Bc );
*/
    double U1_Bc[][] = LinearAlgebra.mult( U1, Bc );

    double U1_Bc_hkl[][] = new double[n_peaks][];
    for ( int i = 0; i < n_peaks; i++ )
      U1_Bc_hkl[i] = LinearAlgebra.mult( U1_Bc, hkl_vals[i] );

    double diff;
    double sum_sq = 0;
    for ( int row = 0; row < n_peaks; row++ )
      for ( int col = 0; col < 3; col++ )
      {   
        diff = U1_Bc_hkl[row][col] - q_vals[row][col];
        sum_sq += diff * diff;
      }

    return sum_sq;
  }


  /* ---------------------------- ResidualError --------------------------- */
  /**
   *  This method calculates the sum squared errors in "q" space for the
   *  mapping from the specified hkl_vals to the specified q_vals, using the
   *  specified lattice parameters.  It allocates local storage for matrices
   *  U1, B1 and Bc then invokes the form of the ResidualError calculation that
   *  takes these as additional parameters.  The summed residual errors 
   *  between the calculated U1*Bc*hkl_vals and the q_vals is returned as 
   *  the value of this function.
   *
   *  @param  lattice_parameters  Array containing the lattice parameters
   *                              to use, in the order a, b, c, alpha, beta,
   *                              gamma.
   *  @param  hkl_vals            Nx3 array, containing the Miller indices
   *                              (h,k,l), as rows, in the same order as the
   *                              corresponding q_vals[][3].   
   *  @param  q_vals              Nx3 array, containing the q values 
   *                              (qx,qy,qz), as rows, in the same order as the
   *                              corresponding hkl_vals[][3].   
   *
   *  @return the sum squared differences in q between the specified q_vals
   *          and U1*Bc*hkl_vals.
   */
  public static double ResidualError( double lattice_parameters[],
                                      double hkl_vals[][],
                                      double q_vals[][] )
  {
    double U1[][] = new double[3][3];
    double B1[][] = new double[3][3];
    double Bc[][] = new double[3][3];

    return ResidualError( lattice_parameters, hkl_vals, q_vals, U1, B1, Bc );
  }


  /* ------------------------ ColumnDotProduct ------------------------ */
  /**
   *  Calculate the dot product of two specified columns of a matrix.
   *
   *  @param  matrix  The matrix containing the columns.  This MUST be
   *                  rectangular, with each row having the same length.
   *  @param  col_1   One of the columns to use when calculating the dot 
   *                  product. 
   *  @param  col_2   The other column to use when calculating the dot 
   *                  product. 
   *
   *  @return  The dot product of the two specified columns, or Double.NaN
   *           if the columns or matrix are invalid.
   */
  public static double ColumnDotProduct( double matrix[][], 
                                         int    col_1,
                                         int    col_2 )
  {
    if ( matrix == null            || 
         matrix.length == 0        || 
         matrix[0].length <= col_1 ||
         matrix[0].length <= col_2 )
      return Double.NaN;

    double sum = 0;
    for ( int row = 0; row < matrix.length; row++ )
      sum += matrix[row][col_1] * matrix[row][col_2];

    return sum;
  }


  /* ---------------------- ShowAllColumnDotProducts ---------------------- */
  /**
   *  Calculate and show the dot product of each pair of columns of a
   *  3x3 matrix matrix.
   *
   *  @param  matrix  The 3x3 matrix containing the columns
   */

  public static void ShowAllColumnDotProducts( double matrix[][] )
  {
    System.out.println( "col 0 dot col 0 = " + ColumnDotProduct(matrix, 0, 0));
    System.out.println( "col 1 dot col 1 = " + ColumnDotProduct(matrix, 1, 1));
    System.out.println( "col 2 dot col 2 = " + ColumnDotProduct(matrix, 2, 2));
    System.out.println( "col 0 dot col 1 = " + ColumnDotProduct(matrix, 0, 1));
    System.out.println( "col 0 dot col 2 = " + ColumnDotProduct(matrix, 0, 2));
    System.out.println( "col 1 dot col 2 = " + ColumnDotProduct(matrix, 1, 2));
  }


  /* ------------------------------ Main --------------------------------- */
  /**
   *  Main program for testing purposes.
   */
  public static void main( String args[] )
  {
    double lattice_parameters[] = { 1, 1, 1, 90, 90, 90 };
    double hkl_vals[][] = { { 1, 0, 0 },
                            { 2, 0, 0 }, 
                            { 3, 0, 0 }, 
                            { 0, 1, 0 }, 
                            { 0, 2, 0 }, 
                            { 0, 3, 0 },
                            { 0, 0, 1 }, 
                            { 0, 0, 2 }, 
                            { 0, 0, 3 } };

    double q_vals[][] = { 
                          { 1, 0, 0 },
                          { 2, 0, 0 },
                          { 3.1, 0, 1 },
                          { 0, 0, 1 },
                          { 0, 0, 2 },
                          { 0, 0, 3 },
                          { 0, 1, 0 },
                          { 0, 2, 0 },
                          { 0, 3, 0 },
/*
                          { 0, 0.866, -0.5 },
                          { 0, 1.732, -1.0 },
                          { 0, 2.598, -1.5 },
*/
                        };
    
    double U1[][] = new double[3][3];
    double B1[][] = new double[3][3];
    double Bc[][] = new double[3][3];
    
    System.out.println( "Residual Error = " +
           ResidualError( lattice_parameters, hkl_vals, q_vals, U1, B1, Bc ) );

    System.out.println( "U1 Matrix = ..........." );
    LinearAlgebra.print( U1 );

    System.out.println( "B1 Matrix = ..........." );
    LinearAlgebra.print( B1 );

    System.out.println( "Bc Matrix = ..........." );
    LinearAlgebra.print( Bc );

    double Bc_lattice_params[] = lattice_calc.LatticeParamsOfUB( Bc );
    System.out.println( "Specified lattice params = " );
    LinearAlgebra.print( Bc_lattice_params );

    double residual_params[] = lattice_calc.LatticeParamsOfUB( B1 );
    System.out.println( "Residual lattice params = " );
    LinearAlgebra.print( residual_params );

    ShowAllColumnDotProducts( U1 );
    System.out.println("Determinant of U1 = " + 
                        LinearAlgebra.determinant( U1 ) );
  }

}

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


  /**
   *  This method calculates the sum squared errors in "q" space for the
   *  mapping from the specified hkl_vals to the specified q_vals, using the
   *  specified lattice parameters.  The lattice parameters are used to 
   *  construct the matrix B, then a least squared fit is used to determine
   *  matrix U.  The summed residual errors between the calculated 
   *  U*B*hkl_vals and the q_vals is returned as the value of this function.
   *  If U and B are non-degenerate 3x3 arrays, they will be filled with 
   *  the matrices U and B that were calculated.
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
   *  @param  U                   3x3 array in which to return the calculated
   *                              U matrix.
   *  @param  B                   3x3 array in which to return the calculated
   *                              B matrix.
   *
   *  @return the sum squared differences in q between the specified q_vals
   *          and U*B*hkl_vals.
   */
  public static double ResidualError( double lattice_parameters[],
                                      double hkl_vals[][],
                                      double q_vals[][],
                                      double U[][],
                                      double B[][] )
  {
    double A[][]      = lattice_calc.A_matrix( lattice_parameters );
//    double B_temp[][] = LinearAlgebra.copy( A ); 
    double B_temp[][] = LinearAlgebra.getInverse( A ); 

    if ( B != null && B.length == 3 && B[0].length == 3 )
      LinearAlgebra.copy( B_temp, B );
 
    int n_peaks = hkl_vals.length;
    double B_hkl[][] = new double[n_peaks][];

    for ( int i = 0; i < n_peaks; i++ )
      B_hkl[i] = LinearAlgebra.mult( B_temp, hkl_vals[i] );

    System.out.println("B_hkl values = ......" );
    for ( int i = 0; i < n_peaks; i++ )
      LinearAlgebra.print( B_hkl[i] );

    System.out.println("q values = ......" );
    for ( int i = 0; i < n_peaks; i++ )
      LinearAlgebra.print( q_vals[i] );

    double q_temp[][] = LinearAlgebra.copy( q_vals );

    double U_temp[][];
    if ( U != null && U.length == 3 && U[0].length == 3 )
      U_temp = U;
    else
      U_temp = new double[3][3];

    double chisq = LinearAlgebra.BestFitMatrix( U_temp, B_hkl, q_temp ); 
    return chisq;
  }


  /**
   *  This method calculates the sum squared errors in "q" space for the
   *  mapping from the specified hkl_vals to the specified q_vals, using the
   *  specified lattice parameters.  It allocates local storage for matrices
   *  U and B, and invokes the form of the ResidualError calculation that
   *  takes U and B as additional parameters.  The summed residual errors 
   *  between the calculated U*B*hkl_vals and the q_vals is returned as 
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
   *          and U*B*hkl_vals.
   */
  public static double ResidualError( double lattice_parameters[],
                                      double hkl_vals[][],
                                      double q_vals[][] )
  {
    double U[][] = new double[3][3];
    double B[][] = new double[3][3];

    return ResidualError( lattice_parameters, hkl_vals, q_vals, U, B );
  }


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

    double q_vals[][] = { { 1, 0, 0 },
                          { 2, 0, 0 },
                          { 3, 0, 0 },
                          { 0, 0, 1 },
                          { 0, 0, 2 },
                          { 0, 0, 3 },
                          { 0, 1, 0 },
                          { 0, 2, 0 },
                          { 0, 3, 0 } };
    
    double U[][] = new double[3][3];
    double B[][] = new double[3][3];
    
    System.out.println( "Residual Error = " +
               ResidualError( lattice_parameters, hkl_vals, q_vals, U, B ) );

    System.out.println( "U Matrix = ..........." );
    LinearAlgebra.print( U );

    System.out.println( "B Matrix = ..........." );
    LinearAlgebra.print( B );

    double B_lattice_params[] = lattice_calc.LatticeParamsOfUB( B );
    System.out.println( "Specified lattice params = " );
    LinearAlgebra.print( B_lattice_params );

    double residual_params[] = lattice_calc.LatticeParamsOfUB( U );
    System.out.println( "Residual lattice params = " );
    LinearAlgebra.print( residual_params );
  }

}

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
 * Revision 1.6  2006/02/06 00:09:49  dennis
 * Added method: DetectorToMinMaxHKL() to calculate the range of hkl values
 * covered by a particular detector and time-of-flight range, given the
 * initial flight path, inverse goniometer rotation and the inverse of the
 * orientation matrix.
 *
 * Revision 1.5  2006/02/05 22:38:12  dennis
 * Added convenience method:  RealToHKL to map a specified pixel and
 * time-of-flight to hkl, given the inverse goniometer rotation and the
 * inverse orientation matrix.
 *
 * Revision 1.4  2006/01/06 06:52:11  dennis
 * Added method to calculate the BestFitMatrix, "UB" using
 * constraints based on the unit cell type.
 * Added test code to main program to exercise fitting using
 * the 9 different Bravais unit cells (counting three forms
 * of a Mononclinic cell.)
 *
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
import  gov.anl.ipns.MathTools.Geometry.*;
import  gov.anl.ipns.MathTools.Functions.*;
import  DataSetTools.math.*;
import  DataSetTools.dataset.*;

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



  /* -------------------------- BestFitMatrix --------------------------- */
  /**
   *  This method uses a MaraquardtArrayFitter to carry out a constrained
   *  least squares optimization of the orientation matrix for a specified
   *  cell type.
   *
   *  @param  cell_type   The type of cell to be fit.  This must be one of
   *                      the strings:
   *                      Triclinic
   *                      Monoclinic ( b unique )
   *                      Monoclinic ( a unique )
   *                      Monoclinic ( c unique )
   *                      Orthorhombic
   *                      Tetragonal
   *                      Rhombohedral
   *                      Hexagonal
   *                      Cubic
   *  @param  UB          A 3x3 array of doubles that will be filled with
   *                      the best (constrained) fit matrix.
   *  @param  hkl_vals    An Nx3 array of doubles containing the list of
   *                      hkl values
   *  @param  q_vals      An Nx3 array of doubles containing the list of
   *                      corresponding q values
   *
   *  @return  The sum squared error after fitting.
   */
  public static double BestFitMatrix( String cell_type, 
                                      double UB[][],
                                      double hkl_vals[][],
                                      double q_vals[][]   )
  {
    SCD_ConstrainedLsqrsError f = null;

    if ( cell_type.startsWith( "Triclinic" ) )
      f = new TriclinicFitError( hkl_vals, q_vals );

    else if ( cell_type.startsWith( "Monoclinic ( a" ) )
      f = new Monoclinic_a_uniqueFitError( hkl_vals, q_vals );

    else if ( cell_type.startsWith( "Monoclinic ( b" ) )
      f = new Monoclinic_b_uniqueFitError( hkl_vals, q_vals );

    else if ( cell_type.startsWith( "Monoclinic ( c" ) )
      f = new Monoclinic_c_uniqueFitError( hkl_vals, q_vals );

    else if ( cell_type.startsWith( "Ortho" ) )
      f = new OrthorhombicFitError( hkl_vals, q_vals );

    else if ( cell_type.startsWith( "Tetra" ) )
      f = new TetragonalFitError( hkl_vals, q_vals );

    else if ( cell_type.startsWith( "Rhomb" ) )
      f = new RhombohedralFitError( hkl_vals, q_vals );

    else if ( cell_type.startsWith( "Hex" ) )
      f = new HexagonalFitError( hkl_vals, q_vals );

    else if ( cell_type.startsWith( "Cubic" ) )
      f = new CubicFitError( hkl_vals, q_vals );

    if ( f == null )
    {
      System.out.println("ERROR: f == null in BestFitMatrix");
      System.out.println("Using basic Triclinic fit method");
      double temp_hkl[][] = LinearAlgebra.copy( hkl_vals );
      double temp_q[][]   = LinearAlgebra.copy( q_vals );
      return LinearAlgebra.BestFitMatrix( UB, temp_hkl, temp_q );
    }

    double z_vals[]  = new double[ hkl_vals.length ];
    double sigmas[]  = new double[ hkl_vals.length ];
    double x_index[] = new double[ hkl_vals.length ];
    for ( int i = 0; i < hkl_vals.length; i++ )
    {
      z_vals[i] = 0;
      sigmas[i] = 1.0;
      x_index[i]  = i;
    }
                                         // build the data fitter and display
                                         // the results.
    MarquardtArrayFitter fitter =
    new MarquardtArrayFitter( f, x_index, z_vals, sigmas, 1.0e-16, 500);

    // System.out.println( fitter.getResultsString() );
    // f.ShowStatus();
 
    double ApproximateUB[][] = f.getU1_Bc();
    LinearAlgebra.copy( ApproximateUB, UB );

    return f.TotalError();
  }


  /* -------------------------- RealToHKL --------------------------- */
  /**
   *  Calculate the HKL value corresponding to a specified pixel position
   *  and time-of-flight, given the initial flight path, inverse goniometer
   *  rotation and the inverse of the orientation matrix.
   *
   *  @param  pixel_vec               Vector giving the location of the
   *                                  pixel relative to the sample
   *  @param  initial_path_m          The initial flight path length in meters
   *
   *  @param  tof_us                  The time-of-flight in microseconds
   *
   *  @param  inv_goniometer_matrix   The inverse of the goniometer rotation 
   *                                  matrix, used to map Q from laboratory
   *                                  coordinates to a vector in a coordinate
   *                                  system relative to the crystal
   *  @param  inv_orientation_matrix  The inverse of the orientation matrix.
   *                                  NOTE: To obtain this from the "IPNS SCD" 
   *                                  orientation matrix, the IPNS orientation 
   *                                  matrix is transposed, then multiplied 
   *                                  by 2*PI and finally inverted.
   *
   *  @return a vector whose first three entries are the hkl values 
   *          corresponding to the specified conditions.
   */
  public static Vector3D RealToHKL( Vector3D   pixel_vec,
                                    float      initial_path_m,
                                    float      tof_us,
                                    Tran3D     inv_goniometer_matrix,
                                    Tran3D     inv_orientation_matrix )
  {
    Vector3D  q_lab;      // Q vector in "laboratory coordinates

    Vector3D  q_crystal;  // Q vector un-rotated by goniometer rotation, to
                          // be in coordinate system attached to the crystal

    Vector3D  hkl_vec;    // resulting hkl values 

    q_lab = tof_calc.DiffractometerVecQ( pixel_vec, initial_path_m, tof_us );

    q_crystal  = new Vector3D();
    inv_goniometer_matrix.apply_to( q_lab, q_crystal );

    hkl_vec = new Vector3D();
    inv_orientation_matrix.apply_to( q_crystal, hkl_vec );

    return hkl_vec;
  }


  /* ------------------------ DetectorToMinMaxHKL ------------------------ */
  /**
   *  Calculate the min and max HKL values corresponding to a specified 
   *  detector grid over a specified time-of-flight range, given the initial
   *  flight path, the inverse goniometer rotation and the inverse of the 
   *  orientation matrix.
   *
   *  @param  grid                    The grid for the detector, giving the
   *                                  locations of all pixels on the detector.
   *  @param  initial_path_m          The initial flight path length in meters
   *
   *  @param  min_tof                 The minimum time-of-flight in microseconds
   *  @param  max_tof                 The maximum time-of-flight in microseconds
   *
   *  @param  inv_goniometer_matrix   The inverse of the goniometer rotation 
   *                                  matrix, used to map Q from laboratory
   *                                  coordinates to a vector in a coordinate
   *                                  system relative to the crystal
   *  @param  inv_orientation_matrix  The inverse of the orientation matrix.
   *                                  NOTE: To obtain this from the "IPNS SCD" 
   *                                  orientation matrix, the IPNS orientation 
   *                                  matrix is transposed, then multiplied 
   *                                  by 2*PI and finally inverted.
   *
   *  @return an array of two vectors listing the minimum hkl values in the
   *          first vector an the maximum hkl values in the second vector.
   */
  public static Vector3D[] DetectorToMinMaxHKL(
                                             IDataGrid grid,
                                             float     initial_path_m,
                                             float     min_tof,
                                             float     max_tof,
                                             Tran3D    inv_goniometer_matrix,
                                             Tran3D    inv_orientation_matrix )
  {
    int n_rows = grid.num_rows();
    int n_cols = grid.num_cols();

    Vector3D  hkl[] = new Vector3D[n_rows * n_cols + 4];  
                                          // vector to hold positions of pixels
                                          // whose hkl are calculated

                                          // the surface in HKL is bowed
                                          // outward at min time (max Q), so
                                          // find the HKL at every point to be
                                          // sure we get the max HKL.
    int index = 0;
    for ( int row = 1; row <= n_rows; row++ )
      for ( int col = 1; col <= n_cols; col++ )
        hkl[index++] = RealToHKL( grid.position( row, col ), 
                                  initial_path_m,
                                  min_tof,
                                  inv_goniometer_matrix,
                                  inv_orientation_matrix );

                                          // for max time (min Q) we can just
                                          // check the corners
    hkl[index++] = RealToHKL( grid.position( 1, 1 ), 
                              initial_path_m, 
                              max_tof, 
                              inv_goniometer_matrix, 
                              inv_orientation_matrix );
    hkl[index++] = RealToHKL( grid.position( 1, n_cols ),
                              initial_path_m,  
                              max_tof,  
                              inv_goniometer_matrix,  
                              inv_orientation_matrix );
    hkl[index++] = RealToHKL( grid.position( n_rows, 1 ),
                              initial_path_m,  
                              max_tof,  
                              inv_goniometer_matrix,  
                              inv_orientation_matrix );
    hkl[index++] = RealToHKL( grid.position( n_rows, n_cols ),
                              initial_path_m,  
                              max_tof,  
                              inv_goniometer_matrix,  
                              inv_orientation_matrix );

                                            // now scan through the hkls to
                                            // find the min and max values
    float hkl_vals[];
    float min_hkl_vals[] = hkl[0].getCopy(); 
    float max_hkl_vals[] = hkl[0].getCopy(); 
    for ( int i = 1; i < hkl.length; i++ )
    {
      hkl_vals = hkl[i].get(); 
      for ( int k = 0; k < 3; k++ )
      {
        if ( hkl_vals[k] < min_hkl_vals[k] )
          min_hkl_vals[k] = hkl_vals[k];

        if ( hkl_vals[k] > max_hkl_vals[k] )
          max_hkl_vals[k] = hkl_vals[k];
      }
    }
                                             // make vectors out of the min &
                                             // max hkls and return the two
                                             // vectors in an array
    Vector3D result[] = new Vector3D[2];
    result[0] = new Vector3D( min_hkl_vals );
    result[1] = new Vector3D( max_hkl_vals );
  
    return result;
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
                            { 4, 0, 0 }, 

                            { 0, 1, 0 }, 
                            { 0, 2, 0 }, 
                            { 0, 3, 0 },
                            { 0, 4, 0 },

                            { 0, 0, 1 }, 
                            { 0, 0, 2 }, 
                            { 0, 0, 3 },
                            { 0, 0, 4 }  };

    double q_vals[][] = { 
                          { 1.0, 0, 0 },
                          { 2.1, 0, 0 },
                          { 2.9, 0, 1 },
                          { 3.8, 0, 1 },

                          { 0, 0,   1.2 },
                          { 0, 0,   2.1 },
                          { 0, 0.1, 3.2 },
                          { 0, 0.1, 4.1 },

                          { 0, 1.1, 0 },
                          { 0, 2.3, 0 },
                          { 0, 3.0, 1 },
                          { 0, 4.1, 1 },
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

    String cell_type[] = { "Non-Existent",
                           "Triclinic", 
                           "Monoclinic ( a",
                           "Monoclinic ( b",
                           "Monoclinic ( c",
                           "Orthorhombic",
                           "Tetragonal",
                           "Rhombohedral",
                           "Hexagonal",
                           "Cubic" };
 
    double err;
    double UB[][] = new double[3][3];

    for ( int i = 0; i < cell_type.length; i++ )
    {
       System.out.println("\nCell Type : " + cell_type[i] + " ......" );
       err = BestFitMatrix( cell_type[i], UB, hkl_vals, q_vals );
       System.out.println( "err = " + err );
//       System.out.println( "UB = " );
//       LinearAlgebra.print( UB );
       lattice_parameters = lattice_calc.LatticeParamsOfUB( UB ); 
       System.out.println("Lattice Parameters = " );
       LinearAlgebra.print( lattice_parameters );
    }

  }

}

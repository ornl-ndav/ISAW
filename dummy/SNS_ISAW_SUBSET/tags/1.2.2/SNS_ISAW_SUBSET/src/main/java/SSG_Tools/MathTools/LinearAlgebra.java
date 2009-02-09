/*
 * File:  LinearAlgebra.java
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *  Last Modified:
 * 
 *  $Author: eu7 $
 *  $Date: 2008-08-21 15:29:09 -0500 (Thu, 21 Aug 2008) $            
 *  $Revision: 305 $
 * 
 *  $Log: LinearAlgebra.java,v $
 *
 *  2008/08/21  Updated to latest version from UW-Stout repository.
 *
 *  Revision 1.3  2008/08/09 21:39:50  dennis
 *  Method HouseholderTransform() checks for null vectors before
 *  checking for unequal length vectors.
 *  Method BestFitMatrix() checks for Q == null before printing Q info.
 *
 *  Revision 1.2  2004/11/22 18:42:33  dennis
 *  Documented empty body of default constructor.
 *
 *  Revision 1.1  2004/10/27 19:19:45  dennis
 *  Extracted and modified portions of LinearAlgebra class from ISAW.
 *  Added to SSG_Tools CVS.
 *
 */

package SSG_Tools.MathTools;

/**  Basic linear algebra operations such as QR factorization and solution of
 *   system of linear equations using QR factorization
 */

public final class LinearAlgebra
{
  public static final boolean DEBUG=false;
  /*
   * Don't let anyone instantiate this class.
   */
  private LinearAlgebra()
  {
  	// private constructor prevents anyone from instantiating it
  }


  /* ------------------------------- solve -------------------------------- */
  /**
    * Solve the system of linear equations Ax = b using the QR factorization
    * of matrix A.  This is NOT the fastest way to solve a linear system, but
    * it is well behaved.  It also immediately provides the "least squares"
    * approximation to a solution of an overdetermined system of equations,
    * as is encountered when fitting a polynomial to data points.  In this case
    * the residual error is returned as the value of the function.  The 
    * solution is returned in parameter "b".  Specifically if A has N columns,
    * then the first N entries of "b" represent the solution to Ax = b if A 
    * is a square matrix.  The first N entries of "b" represent the least 
    * squares solution if A has more rows than columns.
    *
    * @param   A     Rectangular array containing a matrix "A".  This is altered
    *                to contain a matrix "R" where "R" is upper triangular and
    *                A = QR.  The number of rows of A must equal or exceed the
    *                number of columns of A.
    *
    * @param   b     One dimension array of values containing the right hand
    *                side of the linear system of equations Ax = b.  "b" must
    *                have as many rows as matrix "A".
    *
    * @return  The return value represents the residual error in the least
    *           squares approximation if A has more rows than columns.  If
    *           A is square, the return value is 0.  If A has more columns
    *           than rows, or if the system is singular, this function fails 
    *           and returns NaN.
    */

  public static double solve( double A[][], double b[] )
  {
    String error_flag = checkRectangular( A );
    if ( error_flag != null )
    {
      System.out.println("ERROR: A " + error_flag +
                         " in LinearAlgebra.solve ");
      return Double.NaN;
    }

    if ( b == null || A.length != b.length || A.length < A[0].length )
    {
      System.out.println("ERROR: invalid parameters in LinearAlgebra.solve");
      return Double.NaN;
    }

    double u[][] = QR_factorization( A );
                                          // Apply the Householder transforms
                                          // that reduced A to upper triangular
                                          // form to the right hand side, b. 
    return QR_solve( A, u, b );
  }


  /* ------------------------------- QR_solve ----------------------------- */
  /**
   *  Solve a system of linear equations, Ax = b, using the QR factored 
   *  form of A.  This is a portion of the full solution process.  To solve
   *  Ax = b, either:
   *  1. use u = QR_factorization(A) to construct u = Q, A = R and
   *  2. use QR_solve(A,u,b) to replace the components of b with the components
   *     of the solution x.
   *
   *  or just use the solve(A,b) method which combines these two steps.
   *
   *  @param   A     Rectangular array containing a matrix "A", as altered by
   *                 the method QR_factorization(A).
   *
   *  @param  u      The QR factorization of A as returned by the method
   *                 QR_factorization.
   *
   *  @param  b      The right hand side of the linear equations Ax = b
   *
   *  @return   The return value represents the residual error in the least
   *            squares approximation if u has more rows than columns.  If
   *            u is square, the return value is 0.  If u has more columns
   *            than rows, or if the system is singular, this function fails 
   *            and returns NaN.
   */
  public static double QR_solve( double A[][], double u[][], double b[] )
  {
    if ( b == null )
    {
      System.out.println("ERROR: parameter b is null " +
                         " in LinearAlgebra.QR_solve ");
      return Double.NaN;
    }

    String error_flag = checkRectangular( A );
    if ( error_flag != null )
    {
      System.out.println("ERROR: A " + error_flag + 
                         " in LinearAlgebra.QR_solve ");
      return Double.NaN;
    }

    error_flag = checkRectangular( u );
    if ( error_flag != null )
    {
      System.out.println("ERROR: u " + error_flag + 
                         " in LinearAlgebra.QR_solve ");
      return Double.NaN;
    }
                                          // Apply the Householder transforms
                                          // that reduced A to upper triangular
                                          // form to the right hand side, b. 
    for ( int i = 0; i < u.length; i++ )
      HouseholderTransform( u[i], b );
                                          // now back substitute  
    for ( int i = A[0].length-1; i >= 0; i-- )
    {
      if ( A[i][i] == 0 )
      {
        System.out.println("ERROR: singular system in  LinearAlgebra.solve");
        return Double.NaN;
      }
      double sum = 0.0;
      for ( int j = i+1; j < A[0].length; j++ )
        sum += b[j] * A[i][j];

      b[i] = ( b[i] - sum )/A[i][i];
    }

    double error = 0.0;
    for ( int i = A[0].length; i < A.length; i++ )
      error += b[i]*b[i];

    return Math.sqrt( error );
  }

  /* --------------------------- QR_factorization -------------------------- */
  /**
    * Produce the QR factorization of a specified matrix A.  The matrix A is
    * assumed to be a full rectangular array of values ( not a "ragged array").
    * Matrix A is altered to be the upper triangular matrix "R" obtained by
    * applying a sequence of orthogonal transformations to A.  The information
    * required to construct Q is returned in a two-dimensional array as the
    * "value" of this function.
    *
    * @param   A     rectangular array containing a matrix "A".  This is altered
    *                to contain a matrix "R" where "R" is upper triangular and
    *                A = QR.  The number of rows of A must equal or exceed the
    *                number of columns of A.
    *
    * @return  A 2D array containing the unit vectors "U" that generate 
    *          the Householder transformations that were used to reduce the
    *          matrix A to upper triangular form.  Row zero of the returned
    *          array contains the vector "U" that was used to get 0's under
    *          element A[0][0].  Row one of the returned array contains the
    *          vector "U" that was used to get 0's under element A[1][1], etc.
    *          For each i, row i contains the entries of a column vector Ui 
    *          corresponding to an orthogonal transformation 
    *          Qi = I - 2*Ui*transpose(Ui), where I is the identity matrix.
    *          A is reduced to the triangular matrix R by multiplication on 
    *          the left by the product: Qn * ... * Q2 * Q1 * Q0.  
    */

  public static double[][] QR_factorization( double A[][] )
  {
    String error_flag = checkRectangular( A );
    if ( error_flag != null )
    {
      System.out.println("ERROR: A " + error_flag +
                         " in LinearAlgebra.QR_factorization ");
      return null;
    }

    double s;                        // s holds the sqrt of the sum of the 
                                     // squares of the last terms in a column 
    double dot_prod;                 // holds the dot product of U with a vector
    int    row,
           col;
    int    n_rows_A = A.length;
    int    n_cols_A = A[0].length;
    double U[][] = new double[n_cols_A][n_rows_A];

    for ( col = 0; col < n_cols_A; col++ )        // reduce each column of A
    {
      s = 0.0;                                    // find sum of squares of
      for ( row = col; row < n_rows_A; row++ )    // later elements in A[][col]
        s += A[row][col] * A[row][col];
      s = Math.sqrt( s );

      for ( row = 0; row < col; row++ )           // build U vector
        U[col][row] = 0;

      if ( A[col][col] > 0 )
        U[col][col] = A[col][col] + s;
      else
        U[col][col] = A[col][col] - s;

      for ( row = col+1; row < n_rows_A; row++ )
        U[col][row] = A[row][col];

      normalize( U[col] );

      // Now multiply A by the Housholder transform corresponding to U[col].  
      // The effect of the Householder transform on any vector V is 
      // to change V to V - cU, where c is twice the dot product of V and U.
      // Since the first entries in U are non-zero, we only alter the values 
      // of A in the lower right portion of A.

      for ( int j = col; j < n_cols_A; j++ )   // alter column "j"
      {
         dot_prod = 0.0;                      // find dot product of U and col j
         for ( int i = col; i < n_rows_A; i++ )
           dot_prod += U[col][i] * A[i][j];
                                              // V = V - cU, where V is col j 
                                              // of A
         for ( int i = col; i < n_rows_A; i++ )
           A[i][j] -= 2 * dot_prod * U[col][i];
      }
    }
    return U;
  }

  /* -------------------------- BestFitMatrix ---------------------------- */
  /**
   *  Calculate and return the matrix M that most nearly maps the vectors in
   *  the list q to the vectors in the list r.  This performs least squares
   *  fitting of the coefficients in the matrix M, as is needed when refining
   *  the orientation matrix for SCD data proccessing. Specifically, M will
   *  be calculated to be the matrix that best fits the equations  Mqi=ri
   *  for vectors qi, ri, i=0,...,k-1, where k is the number of data vectors.
   *  The vectors qi each have m components and the vectors ri each have n
   *  components.  In many cases n = m = 3, but the solution is more general.
   *  In the "simple" case, this finds the 3x3 matrix M that most nearly 
   *  maps the 3D vectors qi to the 3D vectors ri.  Parameter q holds the
   *  3D vectors qi in its ROWS and parameter r holds the 3D vectors ri in
   *  its ROWS.
   *  This version uses the QR factorization method from this package and 
   *  returns the residual errors.  The BestFitMatrix2() is based on the
   *  Jama package from NIST.  It is included for testing purposes.
   *  BestFitMatrix2() is slower than BestFitMatrix() method and does NOT 
   *  return residual errors, so BestFitMatrix() method should generally
   *  be used.
   *
   *  @param  M      The n X m matrix that maps vector q[i][*] to 
   *                 vector r[i][*].  The components of M are set by this
   *                 method, but the storage for M must be properly allocated
   *                 by the calling code.
   *  @param  q      This is the list of vector q data points.  Each row of
   *                 the 2-dimensional array q is assumed to be a vector of
   *                 size m.  There must be the same number of q vectors, as
   *                 of r vectors.  Specifically, q must be of size k x m.
   *  @param  r      This is the list of vector r data points.  Each row of
   *                 the 2-dimensional array r is assumed to be a vector of
   *                 size n.  There must be the same number of r vectors, as
   *                 q vectors.  Specifically, r must be of size k x n.
   *                  
   *  @return On completion, the best fit matrix will have been entered in
   *  parameter M and the return value will be the square root of the sum of 
   *  the squares of the residual errors.  If an error is encountered, error
   *  information will be printed to the console and the return value will
   *  be set to Double.NaN.  The state of the parameters is indeterminate
   *  if an error is encountered.  NOTE: The values stored in M, q and
   *  r will be altered during the calculation, so if the calling code needs 
   *  their original values, it is responsible for saving copies of the 
   *  original data.
   */
  public static double BestFitMatrix(double M[][], double q[][], double r[][])
  {
    String error_flag = checkRectangular( M );
    if ( error_flag != null )
    {
      System.out.println("ERROR: M " + error_flag +
                         " in LinearAlgebra.BestFitMatrix ");
      return Double.NaN;
    }

    error_flag = checkRectangular( q );
    if ( error_flag != null )
    {
      System.out.println("ERROR: q " + error_flag +
                         " in LinearAlgebra.BestFitMatrix ");
      return Double.NaN;
    }

    error_flag = checkRectangular( r );
    if ( error_flag != null )
    {
      System.out.println("ERROR: r " + error_flag +
                         " in LinearAlgebra.BestFitMatrix ");
      return Double.NaN;
    }

    int k = q.length;
    int n = M.length;
    int m = M[0].length;
    double b[] = new double[k];           // Temporary storage for right hand
                                          // side of least squares problems

    double residual = 0;                  // sum of squares of residual errors
    double error    = 0;                  // residual error from one row of M.
    double Q[][]    = null;
    int    row      = 0;
    try
    {
      // NOTE: The entries in q form the coefficient matrix for the least
      // squares fitting.  After the call to QR_factorization, we will have
      // factored the original q matrix to  Q*q, with the altered "q" matrix
      // being the factor R. 

      Q = QR_factorization( q );
                                          // Now solve for the best fit entries
                                          // in each row of matrix M
      for ( row = 0; row < n; row++ )
      {
        for ( int i = 0; i < k; i++ )
          b[i] = r[i][row];

        error = QR_solve( q, Q, b );
        if ( Double.isNaN( error ) )
        {
          System.out.println("Singular matrix, in LinearAlgebra.BestFitMatrix");
          return Double.NaN;
        }

        residual += error * error;
        for ( int col = 0; col < m; col++ )  // copy result to row of solution M
          M[row][col] = b[col];
      }
    }
    catch ( Exception e )    // dump info to figure out which parameter was bad
    {
      System.out.println("Exception in BestFitMatrix " + e );
      e.printStackTrace();
      System.out.println("Row = " + row );
      System.out.println("M size = " + M.length + " by " + M[0].length );
      System.out.println("r size = " + r.length + " by " + r[0].length );
      System.out.println("q size = " + q.length + " by " + q[0].length );
      System.out.println("b length = " + b.length );
      if ( Q == null )
        System.out.println("Q == null " );
      else
        System.out.println("Q size = " + Q.length + " by " + Q[0].length );
      return Double.NaN;
    }

    return Math.sqrt(residual);
  }

  /* ---------------------- HouseholderTransform --------------------------- */
  /**
   *  Calculate the effect of the Householder transform Q = I - 2 u transp(u)
   *  on a vector y, given the unit vector u.  This function replaces y by Qy. 
   *
   *  @param  u   Unit vector that determines the Householder transformation, Q.
   *  @param  y   Vector to which the Householder transformation is applied. 
   *              y is altered by the method and on completion y = Qy.
   *
   */
   public static void HouseholderTransform( double u[], double y[] )
   {
      if ( u == null || y == null )
      {
        System.out.println("ERROR: null vectors in Householder Tranform");
        return;
      }
      
      if ( u.length != y.length )
      {
        System.out.println("ERROR: Invalid vectors in Householder Tranform");
        System.out.println("u.length = " + u.length );
        System.out.println("y.length = " + y.length );
        return;
      }

      double sum = 0.0;
      for ( int i = 0; i < y.length; i++ )
        sum += u[i] * y[i];

      double c = 2.0 * sum;
      for ( int i = 0; i < y.length; i++ )
        y[i] -= c * u[i];
   }


  /* --------------------------- Normalize --------------------------------- */
  /**
   *  Normalize the given vector to have length 1, by dividing by it's length
   *  if it's not the zero vector.
   *
   *  @param  v    The vector to be normalized.  If v is not the zero vector,
   *               each component of v is divided by the length of v.  If v is 
   *               the zero vector, it is not changed.
   */

  public static void normalize( double v[] )
  {
    double norm = 0.0;

    for ( int i = 0; i < v.length; i++ )
      norm += v[i]*v[i];

    if ( norm == 0 )              // if we have a zero vector, don't change it
      return;

    norm = Math.sqrt( norm );

    for ( int i = 0; i < v.length; i++ )
      v[i] /= norm;
  }


  /* --------------------------- Transpose  ------------------------------ */
  /**
   *  Make a new matrix that is the transpose of the specified matrix.
   *  The specified matrix MUST be a rectangular matrix.
   *
   *  @param   M  The matrix to be transposed.
   *
   *  @return  A new matrix that is the transpose of the matrix M, or null 
   *           if the transpose could not be computed.
   */

  public static double[][] transpose( double M[][] )
  {
    String error_flag = checkRectangular( M );
    if ( error_flag != null )
    {
      System.out.println("ERROR: " + error_flag +" in LinearAlgebra.transpose");
      return null;
    }

    int n_rows = M.length;
    int n_cols = M[0].length;
    double T[][] = new double[n_cols][n_rows];
    for ( int row = 0; row < n_rows; row++ )
      for ( int col = 0; col < n_cols; col++ )
        T[col][row] = M[row][col];

    return T;
  }


  /* --------------------------- multiply ------------------------------ */
  /**
   *  Multiply the specified matrices, A, B and return a new matrix 
   *  containing the product AB. 
   * 
   *  @param   A  The first matrix factor. 
   *  @param   B  The second matrix factor. 
   *
   *  @return the product AB, or null if the matrices could not be multiplied.
   */
  public static double[][] multiply( double A[][], double B[][] )
  {
    String error_flag = checkRectangular( A );
    if ( error_flag != null )
    {
      System.out.println("ERROR: A " +error_flag+" in LinearAlgebra.multiply");
      return null;
    }

    error_flag = checkRectangular( B );
    if ( error_flag != null )
    {
      System.out.println("ERROR: B " +error_flag+" in LinearAlgebra.multiply");
      return null;
    }

    if ( A[0].length != B.length )
    {
      System.out.println("ERROR: number of cols in A = " + A[0].length +
                         " doesn't match number of rows in B = " + B.length +
                         " in LinearAlgebra.multiply" );
      return null;
    }
 
    double product[][] = new double[ A.length ][ B[0].length ];
    double sum;
    for ( int i = 0; i < 4; i++ )
    {
      for ( int j = 0; j < 4; j++ )
      {
        sum = 0.0;                               // find product of row i
        for ( int k = 0; k < 4; k++ )            // with the columns of "B".
          sum = sum + A[i][k] * B[k][j];
        product[i][j] = sum;
      }
    }

    return product;
  }


  /* ------------------------- checkRectangular ------------------------ */
  /**
   *  Check if M is a valid rectangular matrix.
   *
   *  @param   M  The matrix to be checked. 
   *
   *  @return  An error string if there is a problem with the matrix M, or
   *           null, if the matrix M is a non-degenerate rectangular matrix.
   */
  public static String checkRectangular( double M[][] )
  {
    if ( M == null || M.length == 0 || M[0].length == 0 )
      return "Matrix is degenerate";

    for ( int row = 1; row < M.length; row++ )
      if ( M[row].length != M[0].length )
        return "Row length is not constant";

    return null;
  }


}

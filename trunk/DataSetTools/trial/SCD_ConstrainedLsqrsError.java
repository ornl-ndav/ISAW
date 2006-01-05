/*
 * File: SCD_ConstrainedLsqrsError.java 
 *
 * Copyright (C) 2006, Dennis Mikkelson
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2006/01/05 22:11:44  dennis
 *  Initial version of file for constrained least squares fitting
 *  for the SCD.
 *
 */

package DataSetTools.trial;

import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Functions.*;

/**
 *   This class is the abstract base class for functions that calculate 
 * the error in the mapping: U * Bc * hkl -> q  for a specified entry 
 * in a list of q values and corresponding Miller indices hkl.  
 * These functions are used for optimizing the values in Bc, subject 
 * to constraints on the lattice parameters for different cell types. 
 *   There is a concrete derived class for each of the Bravais lattice
 * types, which implement the init() and setParameters() methods.  These
 * methods enforce the constraints for the cell type. 
 */

abstract public class   SCD_ConstrainedLsqrsError 
                extends OneVarParameterizedFunction
{
  protected int    count = 0;

  protected double hkl[][];
  protected double q[][];

  protected double lattice_parameters[];

  protected double U1[][] = new double[3][3];
  protected double B1[][] = new double[3][3];
  protected double Bc[][] = new double[3][3];

  protected double UB[][] = new double[3][3];
  protected double U1_Bc[][];


 /* ----------------------------- constructor ---------------------------- */
 /**
  *  Construct a new error model function for the specified
  *  set of hkl and q values.
  *
  *  @param  title       The name of the function
  *  @param  params      List of parameter values for this function.  This
  *                      will vary depending on the cell type.  In particular
  *                      for a Hexagonal cell there are only two parameters,
  *                      "a" and "c", since b=c and the angles are fixed at
  *                      90, 90 and 120 degrees.
  *  @param  param_names List of names for the params.  This must have the
  *                      same length as params[].
  *  @param  hkl_vals    The list of Miller indices for the peaks  
  *  @param  q_vals      The list q_values of peaks
  */
  public SCD_ConstrainedLsqrsError( String title, 
                                    double params[],
                                    String param_names[],
                                    double hkl_vals[][], 
                                    double q_vals[][]   )
  {
    super( title, params, param_names );

    hkl = LinearAlgebra.copy( hkl_vals );
    q   = LinearAlgebra.copy( q_vals );
                                                  // get some good starting
                                                  // estimates for the 
                                                  // lattice parameters
    double hkl_temp[][] = LinearAlgebra.copy( hkl_vals );
    double q_temp[][]   = LinearAlgebra.copy( q_vals );

    LinearAlgebra.BestFitMatrix( UB, hkl_temp, q_temp );

    init();                                       // constrain the parameters
                                                  // for the specific cell type

                                                  // now form the matrix U1_Bc
                                                  // for these parameters so  
                                                  // that the errors can be 
                                                  // calculated
    SCD_util.ResidualError( lattice_parameters, hkl_vals, q_vals, U1, B1, Bc );
    U1_Bc = LinearAlgebra.mult( U1, Bc );
  }

 
 /* ----------------------------- init ---------------------------- */
 /**
  *  This method is used to initialize the list of six lattice parameters
  *  from the calculated UB matrix, subject to constraints corresponding
  *  to a specific unit cell type.  This method AND the setParameters()
  *  method MUST be implemented in derived classes to apply the constraints
  *  for the cell type.
  */
  abstract protected void init();


 /* ----------------------------- getValue ---------------------------- */
 /**
  *  Evaluate the error at the specified point.
  *
  *  @param  index  the index of the point at which the error is to be 
  *                 evaluated 
  *
  *  @return the error, at the specified point, provided the index is 
  *          is in the range 0 to number q values - 1.
  */
  public double getValue( double index )
  {
    if ( index < 0 || index >= hkl.length )
      return 0; 

    double predicted_q[] = LinearAlgebra.mult( U1_Bc, hkl[ (int)index ] );

    double diff;
    double sum_sq = 0;
    for ( int col = 0; col < 3; col++ )
    {
      diff = predicted_q[col] - q[(int)index][col];
      sum_sq += diff * diff;
    }

    return sum_sq;
  }


 /* ----------------------------- getValues ---------------------------- */
 /**
  *  Evaluate the error at the specified list of points.
  *
  *  @param  indices  list of indices of points at which the error
  *                   is to be evaluated
  *
  *  @return an array containing the errors at the specified points.  
  *          If an index is not valid, the value is taken to be 0 at 
  *          that point.
  */
  public double[] getValues( double indices[] )
  {
    int index;
    double diff;
    double sum_sq[] = new double[ indices.length ];
    double predicted_q[];

    for ( int i = 0; i < indices.length; i++ )
    {
      index = (int)indices[i];
      if ( index < 0 || index >= hkl.length )
        sum_sq[i] = 0;

      predicted_q = LinearAlgebra.mult( U1_Bc, hkl[ index ] );

      for ( int col = 0; col < 3; col++ )
      {
        diff = predicted_q[col] - q[index][col];
        sum_sq[i] += diff * diff;
      }
    }
 
    return sum_sq;
  }


 /* ----------------------------- ShowStatus ---------------------------- */
 /**
  *  Print the current matrices being used and the TotalError.
  */
  public void ShowStatus()
  {
    System.out.println("U = ......");
    double U[][] = lattice_calc.getU( UB );
    LinearAlgebra.print( U );

    System.out.println("U1 = ......");
    LinearAlgebra.print( U1 );

    System.out.println("B1 = ......");
    LinearAlgebra.print( B1 );

    System.out.println("Bc = ......");
    LinearAlgebra.print( Bc );

    System.out.println("TotalError = " + TotalError() );
  }


 /* ----------------------------- TotalError ---------------------------- */
 /**
  *  Calculate the sum of the errors for all of the peaks
  *
  *  @return the sum of all of the values returned by the getValues()
  *          method.
  */
  public double TotalError()
  {
    double indices[] = new double[ hkl.length ];
    for ( int i = 0; i < indices.length; i ++ )
      indices[i] = i;

    double errs[] = getValues( indices );
  
    double sum = 0;
    for ( int i = 0; i < errs.length; i++ )
      sum += errs[i];

    return sum;
  }


}

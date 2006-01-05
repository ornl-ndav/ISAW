/*
 * File:  CubicFitError.java
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
 *  Revision 1.1  2006/01/05 22:29:57  dennis
 *  Initial version of error function for constrained least squares
 *  determination of lattice constants for a cubic cell.
 *
 *
 */

package DataSetTools.trial;

import gov.anl.ipns.MathTools.*;

/**
 * This class calculates the error in the mapping:
 * U1 * Bc * hkl -> q  for a specified entry in a list of q values and 
 * corresponding Miller indices hkl assuming that the cell type is 
 * Cubic.  It is intended to be used for optimizing the values in 
 * the constrained "B" matrix, Bc.
 */

public class CubicFitError extends SCD_ConstrainedLsqrsError
{

 /* ----------------------------- constructor --------------------------- */
 /**
  *  Construct a new CubicFitError model function object for the 
  *  specified set of hkl and q values.
  *
  *  @param  hkl_vals  The list of Miller indices for the peaks  
  *  @param  q_vals    The list q_values of peaks
  */
  public CubicFitError( double hkl_vals[][], double q_vals[][] )
  {
    super("CubicFitError", new double[1], new String[1], hkl_vals, q_vals);
  }


 /* -------------------------------- init ------------------------------ */
  /**
   *  This method is used to initialize the list of six lattice parameters
   *  from the calculated UB matrix, subject to constraints corresponding
   *  to a Cubic unit cell.
   */
  protected void init()
  {
    lattice_parameters = lattice_calc.LatticeParamsOfUB( UB );
    double average = (  lattice_parameters[0] 
                      + lattice_parameters[1]
                      + lattice_parameters[2] )/3;
    lattice_parameters[0] = average;
    lattice_parameters[1] = average;
    lattice_parameters[2] = average;
    lattice_parameters[3] = 90;
    lattice_parameters[4] = 90;
    lattice_parameters[5] = 90;

    parameter_names[0] = "a"; 

    parameters[0] = lattice_parameters[0];         // copy over value of 'a'
  }


 /* ---------------------------- setParameters --------------------------- */
  /**
   *  This method takes values for lattice parameter "a" from params[0] and
   *  sets those as the values of "a", "b" and "c" in the list of all six
   *  lattice parameters, using the Cubic unit cell constraints.  
   *  In addition the parameters are set in the base class.  Finally,
   *  it calculates the U1_Bc matrix used in the evaluation of the 
   *  residual error.
   *
   *  @param  params  An array of length 1 containing the values for lattice
   *                  parameter "a".
   */
  public void setParameters( double params[] )
  {
    super.setParameters( params );
    lattice_parameters[0] = params[0];       // set  a = b = c = params[0]
    lattice_parameters[1] = params[0];
    lattice_parameters[2] = params[0]; 

    lattice_parameters[3] = 90;
    lattice_parameters[4] = 90;
    lattice_parameters[5] = 90;

    SCD_util.ResidualError( lattice_parameters, hkl, q, U1, B1, Bc );
    U1_Bc = LinearAlgebra.mult( U1, Bc );
  }


 /* -------------------------------------------------------------------------
  *
  * MAIN  ( Basic main program for testing purposes only. )
  *
  */
    public static void main(String[] args)
    {
      double hkl_vals[][] = { { 1, 0, 0 }, 
                              { 2, 0, 0 },
                              { 0, 1, 0 },
                              { 0, 0, 1 } };

      double q_vals[][] = { { 1, 0, 0 }, 
                            { 2.1, 0, 0 },
                            { 0, 1, 0 },
                            { 0, 0, 1 } };

      CubicFitError f = new  CubicFitError( hkl_vals, q_vals );
      System.out.println("Total error is : " + f.TotalError() );

      double param[] = new double[2];
      param[0] = 1;
      f.setParameters( param );
      System.out.println("Total error is : " + f.TotalError() );
    }
}

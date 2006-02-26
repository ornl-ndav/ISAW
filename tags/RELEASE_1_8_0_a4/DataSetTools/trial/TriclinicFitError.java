/*
 * File:  TriclinicFitError.java
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
 *  Revision 1.1  2006/01/06 04:01:19  dennis
 *  Error function for Triclinic cell.  While this is not needed
 *  since the Triclinic case can be handled with un-constrained
 *  optimization, it is useful for testing.
 *
 */

package DataSetTools.trial;

import gov.anl.ipns.MathTools.*;

/**
 * This class calculates the error in the mapping:
 * U1 * Bc * hkl -> q  for a specified entry in a list of q values and 
 * corresponding Miller indices hkl assuming that the cell type is 
 * Triclinic.  It is intended to be used for optimizing the 
 * values in the constrained "B" matrix, Bc.
 */

public class TriclinicFitError extends SCD_ConstrainedLsqrsError
{

 /* ----------------------------- constructor --------------------------- */
 /**
  *  Construct a new TriclinicFitError model function object for the 
  *  specified set of hkl and q values.
  *
  *  @param  hkl_vals  The list of Miller indices for the peaks  
  *  @param  q_vals    The list q_values of peaks
  */
  public TriclinicFitError( double hkl_vals[][], double q_vals[][] )
  {
    super( "TriclinicFitError", 
            new double[6], 
            new String[6], 
            hkl_vals, 
            q_vals );
  }


  /* -------------------------------- init ------------------------------ */
  /**
   *  This method is used to initialize the list of six lattice parameters
   *  from the calculated UB matrix.  The lattice parameters are unconstrained
   *  for a Triclinic unit cell.
   */
  protected void init()
  {
    lattice_parameters = lattice_calc.LatticeParamsOfUB( UB );
    
    parameter_names[0] = "a"; 
    parameter_names[1] = "b";
    parameter_names[2] = "c"; 
    parameter_names[3] = "alpha"; 
    parameter_names[4] = "beta"; 
    parameter_names[5] = "gamma"; 
                                   // copy over the values of all parameters
                                   // a, b, c, alpha, beta, gamma 
    for ( int i = 0; i < 6; i++ )
      parameters[i] = lattice_parameters[i]; 
  }


 /* ---------------------------- setParameters --------------------------- */
  /**
   *  This method takes values for lattice parameters "a", "b", "c",
   *  "alpha", "beta" and "gamma" from params[0..5].  It sets those into 
   *  the list of all six lattice parameters.
   *  In addition the parameters are set in the base class.  Finally,
   *  it calculates the U1_Bc matrix used in the evaluation of the 
   *  residual error.
   *
   *  @param  params  An array of length 6 containing the values for lattice
   *                  parameters "a", "b", "c", "alpha", "beta" and "gamma".
   */
  public void setParameters( double params[] )
  {
    super.setParameters( params );

    for ( int i = 0; i < 6; i++ )
      lattice_parameters[i] = params[i]; 

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
                            { 2, 0, 0 },
                            { 0, 1, 0 },
                            { 0, 0, 1 } };

      TriclinicFitError f = new TriclinicFitError( hkl_vals, q_vals );

      System.out.println("Total error is : " + f.TotalError() );

      double param[] = new double[6];
      param[0] = 1;
      param[1] = 1.1;
      param[2] = 1.1;
      param[3] = 90;
      param[4] = 90;
      param[5] = 90;
      f.setParameters( param );
      System.out.println("Total error is : " + f.TotalError() );
    }
}

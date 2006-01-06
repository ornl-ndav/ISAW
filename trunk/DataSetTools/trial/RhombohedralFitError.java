/*
 * File:  RhombohedralFitError.java
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
 *  Revision 1.2  2006/01/06 02:46:18  dennis
 *  Minor fix to javadocs.
 *  Minor code clean up.
 *  Minor adjustment to test parameters.
 *
 *  Revision 1.1  2006/01/05 22:43:44  rmikk
 *  Initial Checkin
 *
 *  Revision 1.1  2006/01/05 22:11:44  dennis
 *  Initial version of file for constrained least squares fitting
 *  for the SCD.
 *
 */

package DataSetTools.trial;

import gov.anl.ipns.MathTools.*;

/**
 * This class calculates the error in the mapping:
 * U1 * Bc * hkl -> q  for a specified entry in a list of q values and 
 * corresponding Miller indices hkl assuming that the cell type is 
 * Rhombohedral.  It is intended to be used for optimizing the values in 
 * the constrained "B" matrix, Bc.
 */

public class RhombohedralFitError extends SCD_ConstrainedLsqrsError
{

 /* ----------------------------- constructor --------------------------- */
 /**
  *  Construct a new RhombohedralFitError model function object for the 
  *  specified set of hkl and q values.
  *
  *  @param  hkl_vals  The list of Miller indices for the peaks  
  *  @param  q_vals    The list q_values of peaks
  */
  public RhombohedralFitError( double hkl_vals[][], double q_vals[][] )
  {
    super("RhombohedralFitError",new double[2],new String[2],hkl_vals,q_vals);
  }


 /* -------------------------------- init ------------------------------ */
  /**
   *  This method is used to initialize the list of six lattice parameters
   *  from the calculated UB matrix, subject to constraints corresponding
   *  to a Rhombohedral unit cell.
   */
  protected void init()
  {
    lattice_parameters = lattice_calc.LatticeParamsOfUB( UB );
    double average_side = ( lattice_parameters[0] + 
                            lattice_parameters[1] +
                            lattice_parameters[2] ) / 3;
    lattice_parameters[0] = average_side;
    lattice_parameters[1] = average_side;
    lattice_parameters[2] = average_side;

    double average_angle = ( lattice_parameters[3] + 
                             lattice_parameters[4] +
                             lattice_parameters[5] ) / 3;
    lattice_parameters[3] = average_angle; 
    lattice_parameters[4] = average_angle; 
    lattice_parameters[5] = average_angle; 

    parameter_names[0] = "a"; 
    parameter_names[1] = "alpha";

    parameters[0] = lattice_parameters[0];         // copy over values of 'a'
    parameters[1] = lattice_parameters[3];         // and of 'c'
  }


 /* ---------------------------- setParameters --------------------------- */
  /**
   *  This method takes values for lattice parameters "a" and "alpha" from
   *  params[0] and params[1].  It sets those into the list of all six
   *  lattice parameters, using the Rhombohedral unit cell constraints.  
   *  In addition the parameters are set in the base class.  Finally,
   *  it calculates the U1_Bc matrix used in the evaluation of the 
   *  residual error.
   *
   *  @param  params  An array of length 2 containing the values for lattice
   *                  parameters "a" and "alpha".
   */
  public void setParameters( double params[] )
  {
    super.setParameters( params );
    lattice_parameters[0] = params[0];       // set  a = b = params[0]
    lattice_parameters[1] = params[0];
    lattice_parameters[2] = params[0];       // set  c = params[1]

    lattice_parameters[3] = params[1];
    lattice_parameters[4] = params[1];
    lattice_parameters[5] = params[1];

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

      RhombohedralFitError f = new  RhombohedralFitError( hkl_vals, q_vals );
      System.out.println("Total error is : " + f.TotalError() );

      double param[] = new double[2];
      param[0] = 1.1;
      param[1] = 90;
      f.setParameters( param );
      System.out.println("Total error is : " + f.TotalError() );
    }
}

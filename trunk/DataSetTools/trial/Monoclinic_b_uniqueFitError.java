/*
 * File:  Monoclinic_b_uniqueFitError.java
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
 *  Revision 1.3  2007/02/25 20:20:43  rmikk
 *  Added the method to expand the parameter error array
 *
 *  Revision 1.2  2006/01/06 03:40:53  dennis
 *  Minor code clean-up and fixes to javadocs.
 *
 *  Revision 1.1  2006/01/05 23:11:52  rmikk
 *  Initial Checkin
 *
 */

package DataSetTools.trial;

import gov.anl.ipns.MathTools.*;

/**
 * This class calculates the error in the mapping:
 * U1 * Bc * hkl -> q  for a specified entry in a list of q values and 
 * corresponding Miller indices hkl assuming that the cell type is 
 * Monoclinic(b_unique).  It is intended to be used for optimizing the 
 * values in the constrained "B" matrix, Bc.
 */

public class Monoclinic_b_uniqueFitError extends SCD_ConstrainedLsqrsError
{

 /* ----------------------------- constructor --------------------------- */
 /**
  *  Construct a new Monoclinic_b_uniqueFitError model function object for the 
  *  specified set of hkl and q values.
  *
  *  @param  hkl_vals  The list of Miller indices for the peaks  
  *  @param  q_vals    The list q_values of peaks
  */
  public Monoclinic_b_uniqueFitError( double hkl_vals[][], double q_vals[][] )
  {
    super( "Monoclinic_b_uniqueFitError", 
            new double[4], 
            new String[4], 
            hkl_vals, 
            q_vals );
  }


 /* -------------------------------- init ------------------------------ */
  /**
   *  This method is used to initialize the list of six lattice parameters
   *  from the calculated UB matrix, subject to constraints corresponding
   *  to a Monoclinic_b_unique unit cell.
   */
  protected void init()
  {
    lattice_parameters = lattice_calc.LatticeParamsOfUB( UB );
    
    lattice_parameters[3] = 90; 
    lattice_parameters[5] = 90;

    parameter_names[0] = "a"; 
    parameter_names[1] = "b";
    parameter_names[2] = "c"; 
    parameter_names[3] = "beta"; 

    parameters[0] = lattice_parameters[0];         // copy over values of 'a'
    parameters[1] = lattice_parameters[1];         // and of 'b'
    parameters[2] = lattice_parameters[2];         // and of 'c'
    parameters[3] = lattice_parameters[4];         // and of 'beta'
  }


 /* ---------------------------- setParameters --------------------------- */
  /**
   *  This method takes values for lattice parameters "a", "b", "c" and
   *  "beta" from params[0..3].  It sets those into the list of all six
   *  lattice parameters, using the Monoclinic ( b unique ) unit cell
   *  constraints.  
   *  In addition the parameters are set in the base class.  Finally,
   *  it calculates the U1_Bc matrix used in the evaluation of the 
   *  residual error.
   *
   *  @param  params  An array of length 4 containing the values for lattice
   *                  parameters "a", "b", "c" and "beta".
   */
  public void setParameters( double params[] )
  {
    super.setParameters( params );
    lattice_parameters[0] = params[0];  
    lattice_parameters[1] = params[1];
    lattice_parameters[2] = params[2]; 

    lattice_parameters[3] = 90;
    lattice_parameters[4] = params[3];
    lattice_parameters[5] = 90;

    SCD_util.ResidualError( lattice_parameters, hkl, q, U1, B1, Bc );
    U1_Bc = LinearAlgebra.mult( U1, Bc );
  }


 /* ---------------------------- ExpandErrors ---------------------------- */
  /**
   *  Expands the errors in the changing parameters to the errors in all 7
   *  lattice constants.  Some constraints vary fewer than 6 of the
   *  lattice parameters.
   *    
   * @param ParamErrors  The errors in the basic lattice parameters that
   *                     vary for the given constraint
   *
   * @return   a float[7] containing the errors in the corresponding
   *           lattice constants
   */
  public double[] ExpandErrors( double[] ParamErrors )
  {
    double[] Res = new double[7];
    Res[0] = ParamErrors[0];
    Res[1] = ParamErrors[1];
    Res[2] = ParamErrors[2];
    Res[3] = 0;
    Res[4] = ParamErrors[3];
    Res[5] = 0;

    Res[6] = super.calcVolumeError( Res );
    return Res;
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

      Monoclinic_b_uniqueFitError f = 
                        new Monoclinic_b_uniqueFitError( hkl_vals, q_vals );

      System.out.println("Total error is : " + f.TotalError() );

      double param[] = new double[4];
      param[0] = 1;
      param[1] = 1.1;
      param[2] = 1;
      param[3] = 90;

      f.setParameters( param );
      System.out.println("Total error is : " + f.TotalError() );
    }

}

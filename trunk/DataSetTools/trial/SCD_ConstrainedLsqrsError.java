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
 *  Revision 1.4  2007/02/25 20:21:35  rmikk
 *  Added the abstract method to expand the parameter error array and a routine
 *  to calculate the error in the volume.
 *
 *  Revision 1.3  2006/01/06 06:48:34  dennis
 *  Now returns the error rather than the error squared.
 *  (The iteration did not converge in some cases, such as
 *   the Triclinic case, when the error squared was returned.)
 *
 *  Revision 1.2  2006/01/06 03:43:39  dennis
 *  Added method to get the computed U1_Bc matrix.
 *
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

    return Math.sqrt( sum_sq );
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
      else
      {
        predicted_q = LinearAlgebra.mult( U1_Bc, hkl[ index ] );

        for ( int col = 0; col < 3; col++ )
        {
          diff = predicted_q[col] - q[index][col];
          sum_sq[i] += diff * diff;
        }
        sum_sq[i] = Math.sqrt( sum_sq[i] );
      }
    }
 
    return sum_sq;
  }


 /* ------------------------------ getU1_Bc ----------------------------- */
 /**
  *  Get the current U1_Bc product matrix being used by this function.
  *
  *  @return A reference to this error functions U1_Bc matrix.
  */
  public double[][] getU1_Bc()
  {
    return LinearAlgebra.copy( U1_Bc );
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
  
  
  public static double calcVolume( double a, double b, double c, double alpha,
             double beta, double gamma){
    /* double x1= Math.sin( beta/180.*Math.PI);
     double x2=Math.cos(gamma/180*Math.PI)-Math.cos(alpha/180*Math.PI)*
                                         Math.cos(beta/180*Math.PI);
     x2 = x2/Math.sin( alpha/180*Math.PI);
     //return a*b*c*Math.sin(alpha/180.*Math.PI)*Math.sqrt(
     //      x1*x1-x2*x2  );
      * */
      
     double xA=Math.cos(alpha/180.*Math.PI);
     double xB=Math.cos(beta/180.*Math.PI);
     double xC=Math.cos(gamma/180.*Math.PI);
     return a*b*c*Math.sqrt(1-xA*xA-xB*xB-xC*xC+2*xA*xB*xC);
              
     
    
  }
  public static double calcVolumeError( double[] latticeParams, 
                                       double[] lattice_errors)
  {
     double xA=Math.cos(latticeParams[3]/180.*Math.PI);
     double xB=Math.cos(latticeParams[4]/180.*Math.PI);
     double xC=Math.cos(latticeParams[5]/180.*Math.PI);
     double Volume = calcVolume( latticeParams[0], latticeParams[1], latticeParams[2],
              latticeParams[3], latticeParams[4], latticeParams[5]);
     //double[] V2 = DataSetTools.operator.Generic.TOF_SCD.Util.abc(U1_Bc) ; 
     //if( Math.abs(V2[6]-Volume)>.000001)
    //    System.out.println("********************error*************************");
     double dV =0;
     for( int i=0;i<3;i++)
        dV+=sqr( Volume/latticeParams[i]*lattice_errors[i]);
     Volume = Volume/2.0/(1-xA*xA-xB*xB-xC*xC+2*xA*xB*xC);
     dV += sqr(lattice_errors[3])*sqr(Math.sin(2*latticeParams[3]/180.*Math.PI)-
               Math.sin(latticeParams[3]/180.*Math.PI)*Math.cos(latticeParams[4]/180*Math.PI)*
               Math.cos(latticeParams[5]/180*Math.PI));
     dV += sqr(lattice_errors[4])*sqr(Math.sin(2*latticeParams[4]/180.*Math.PI)-
              Math.sin(latticeParams[4]/180.*Math.PI)*Math.cos(latticeParams[3]/180*Math.PI)*
              Math.cos(latticeParams[5]/180*Math.PI));
     dV += sqr(lattice_errors[5])*sqr(Math.sin(2*latticeParams[5]/180.*Math.PI)-
              Math.sin(latticeParams[5]/180.*Math.PI)*Math.cos(latticeParams[4]/180*Math.PI)*
              Math.cos(latticeParams[3]/180*Math.PI));
     dV = Math.sqrt(dV);
     double MinAlpha,MinBeta,MinGamma,MaxAlpha,MaxBeta,MaxGamma;
     MinAlpha = findMin( latticeParams[3],latticeParams[4],latticeParams[5],lattice_errors[3]);
     MinBeta = findMin( latticeParams[4],latticeParams[3],latticeParams[5],lattice_errors[4]);
     MinGamma = findMin( latticeParams[5],latticeParams[4],latticeParams[3],lattice_errors[5]);
     MaxAlpha = findMax( latticeParams[3],latticeParams[4],latticeParams[5],lattice_errors[3]);
     MaxBeta = findMax( latticeParams[4],latticeParams[3],latticeParams[5],lattice_errors[4]);
     MaxGamma = findMax( latticeParams[5],latticeParams[4],latticeParams[3],lattice_errors[5]);
     
     
   /*  System.out.println("********Error="+(calcVolume(latticeParams[0]+lattice_errors[0],
              latticeParams[1]+lattice_errors[1],latticeParams[2]+lattice_errors[2],
              MaxAlpha,MaxBeta,MaxGamma)-
              calcVolume(latticeParams[0]-lattice_errors[0],
              latticeParams[1]-lattice_errors[1],latticeParams[2]-lattice_errors[2],
              MinAlpha,MinBeta,MinGamma))+"********"+(2*dV));
     
     
     */
   
     return dV;
  }
   public double  calcVolumeError( double[] lattice_errors){
      
      double xA=Math.cos(lattice_parameters[3]/180.*Math.PI);
      double xB=Math.cos(lattice_parameters[4]/180.*Math.PI);
      double xC=Math.cos(lattice_parameters[5]/180.*Math.PI);
      double Volume = calcVolume( lattice_parameters[0], lattice_parameters[1], lattice_parameters[2],
               lattice_parameters[3], lattice_parameters[4], lattice_parameters[5]);
      double[] V2 = DataSetTools.operator.Generic.TOF_SCD.Util.abc(U1_Bc) ; 
      if( Math.abs(V2[6]-Volume)>.000001)
         System.out.println("********************error*************************");
      double dV =0;
      for( int i=0;i<3;i++)
         dV+=sqr( Volume/lattice_parameters[i]*lattice_errors[i]);
      Volume = Volume/2.0/(1-xA*xA-xB*xB-xC*xC+2*xA*xB*xC);
      dV += sqr(lattice_errors[3])*sqr(Math.sin(2*lattice_parameters[3]/180.*Math.PI)-
                Math.sin(lattice_parameters[3]/180.*Math.PI)*Math.cos(lattice_parameters[4]/180*Math.PI)*
                Math.cos(lattice_parameters[5]/180*Math.PI));
      dV += sqr(lattice_errors[4])*sqr(Math.sin(2*lattice_parameters[4]/180.*Math.PI)-
               Math.sin(lattice_parameters[4]/180.*Math.PI)*Math.cos(lattice_parameters[3]/180*Math.PI)*
               Math.cos(lattice_parameters[5]/180*Math.PI));
      dV += sqr(lattice_errors[5])*sqr(Math.sin(2*lattice_parameters[5]/180.*Math.PI)-
               Math.sin(lattice_parameters[5]/180.*Math.PI)*Math.cos(lattice_parameters[4]/180*Math.PI)*
               Math.cos(lattice_parameters[3]/180*Math.PI));
      dV = Math.sqrt(dV);
      double MinAlpha,MinBeta,MinGamma,MaxAlpha,MaxBeta,MaxGamma;
      MinAlpha = findMin( lattice_parameters[3],lattice_parameters[4],lattice_parameters[5],lattice_errors[3]);
      MinBeta = findMin( lattice_parameters[4],lattice_parameters[3],lattice_parameters[5],lattice_errors[4]);
      MinGamma = findMin( lattice_parameters[5],lattice_parameters[4],lattice_parameters[3],lattice_errors[5]);
      MaxAlpha = findMax( lattice_parameters[3],lattice_parameters[4],lattice_parameters[5],lattice_errors[3]);
      MaxBeta = findMax( lattice_parameters[4],lattice_parameters[3],lattice_parameters[5],lattice_errors[4]);
      MaxGamma = findMax( lattice_parameters[5],lattice_parameters[4],lattice_parameters[3],lattice_errors[5]);
      
      
    /*  System.out.println("********Error="+(calcVolume(lattice_parameters[0]+lattice_errors[0],
               lattice_parameters[1]+lattice_errors[1],lattice_parameters[2]+lattice_errors[2],
               MaxAlpha,MaxBeta,MaxGamma)-
               calcVolume(lattice_parameters[0]-lattice_errors[0],
               lattice_parameters[1]-lattice_errors[1],lattice_parameters[2]-lattice_errors[2],
               MinAlpha,MinBeta,MinGamma))+"********"+(2*dV));
      
      
      */
    
      return dV;
   }
   private static double sqr( double x){
      return x*x;
   }
   
   
   private static double findMin( double A1, double A2, double A3, double delta){
      
      if( A1-delta >90)
         return A1-delta;
     if( A1-delta< 90) if( A1+delta <90)
        return A1+delta;
     return 90-Math.min(90-A1+delta,A1+delta-90);
   }  
   
   
   private static double findMax( double A1, double A2, double A3, double delta){
      if( A1-delta >90)
         return A1+delta;
      if( A1-delta< 90) if( A1+delta <90)
         return A1-delta;
      return 90;
   }
   
   
   /**
    *  Expands the errors in the changing parameteers to the errors in all 7
    *    lattice constants.  Some constraints vary fewer than 6 of the
    *    lattice parameters.
    *    
    * @param ParamErrors  The errors in the basic lattice parameters that
    *                      vary for the given constraint
    * @return   a float[7] containing the errors in the corresponding
    *            lattice constants
    */
  public abstract double[] ExpandErrors( double[] ParamErrors );

}

/*
 * File:  TestFit.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2003/06/10 22:32:27  dennis
 *  Simple demo of Marquardt fitting for one variable functions,
 *  applied to what is really a two variable function.
 *
 */

package  DataSetTools.trial;

import java.io.*;
import java.util.*;
import DataSetTools.math.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.functions.*;

/**
 * This class implements a parameterized "function" of two variables as a 
 * parameterized function of one variable, as a simple test/demo of
 * the Marquardt fitting code. 
 */

public class TestFit extends    OneVarParameterizedFunction
                     implements Serializable
{
  double grid_x_vals[];
  double grid_y_vals[];

  /**
   *  Construct a function defined on the grid of (x,y) values specified, 
   *  using the parameters and parameter names specified.  The grid points
   *  are numbered in a sequence and the point's index in the sequence is 
   *  used as the one variable.
   */
   public TestFit( double  grid_x_vals[], 
                   double  grid_y_vals[], 
                   double params[],
                   String param_names[] )
   {
     super( "TestFit", params, param_names );
     this.grid_x_vals = grid_x_vals;
     this.grid_y_vals = grid_y_vals;
   }


  /**
   *  Evaluate the value of this model function at the specified index.
   *
   *  @param  x  the index into the list of points at which the model is to be 
   *             evaluated. 
   *
   *  @return the value of the model function, at the specified point 
   */
  public double getValue( double x )
  {
    if ( x >= 0 && x < grid_x_vals.length * grid_y_vals.length )
    {
      int col = (int)(x) % grid_x_vals.length;
      int row = (int)(x) / grid_x_vals.length;
      double xval = grid_x_vals[ col ]; 
      double yval = grid_y_vals[ row ]; 
                                               // just some non-linear function
                                               // to use for this demo
      double function_value = parameters[0] * xval * xval + 
                              parameters[1] * Math.sqrt(yval) + 
                              parameters[2];
      return function_value;
    }
    else
      return 0;
  }

 /* -------------------------------------------------------------------------
  *
  * MAIN  ( Basic main program for testing purposes only. )
  *
  */
    public static void main(String[] args)
    {
                                              // set up the grid of x,y values
                                              // and the list of indices into
                                              // the grid of values
      double grid_x[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      double grid_y[] = { 2, 3, 4, 5.1, 6, 7, 8.5 };

      int num_x = grid_x.length;
      int num_y = grid_y.length;

      double index_list[] = new double[ grid_x.length * grid_y.length ];
      for ( int i = 0; i < num_x * num_y; i++ )
        index_list[i] = i; 
                                                   // set up the list of 
                                                   // parameters and names
      String parameter_names[] = new String[3];
      parameter_names[0] = "param 1";
      parameter_names[1] = "param 2";
      parameter_names[2] = "param 3";

      double parameters[] = new double[3];
      parameters[0] = 5;
      parameters[1] = 2;
      parameters[2] = 3;
                                                     // build the one variable
                                                     // function
      OneVarParameterizedFunction test_f = 
         new TestFit( grid_x, grid_y, parameters, parameter_names ); 

                                                     // build the array of 
                                                     // function values with  
                                                     // noise and "fake" sigmas
      double z_vals[] = new double[ num_x * num_y ]; 
      double sigmas[] = new double[ num_x * num_y ]; 

      Random ran = new Random( 10000 );
      int index = 0;
      for ( int row = 0; row < num_y; row++ )
        for ( int col = 0; col < num_x; col++ )
        {
          z_vals[index] = test_f.getValue( index ) + ran.nextGaussian()/10;
          sigmas[index] = 1;
          index++;
        }

                                           // build the data fitter and display 
                                           // the results.
      MarquardtArrayFitter fitter = 
        new MarquardtArrayFitter( test_f, index_list, z_vals, sigmas, 
                                  1.0e-10, 500 );

      double p_sigmas[];
      double p_sigmas_2[];
      double coefs[];
      String names[];
      p_sigmas = fitter.getParameterSigmas();
      p_sigmas_2 = fitter.getParameterSigmas_2();
      coefs = test_f.getParameters();
      names = test_f.getParameterNames();
      for ( int i = 0; i < test_f.numParameters(); i++ )
        System.out.println(names[i] + " = " + coefs[i] +
                           " +- " + p_sigmas[i] +
                           " +- " + p_sigmas_2[i] );
    }
}

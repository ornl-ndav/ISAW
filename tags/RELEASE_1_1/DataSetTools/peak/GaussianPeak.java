/*
 * File:  GaussianPeak.java
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2001/04/25 21:32:13  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.1  2000/07/10 22:48:57  dennis
 *  New classes to deal with peaks/peak fitting
 *
 *  Revision 1.4  2000/06/15 16:49:20  dennis
 *  improved documentation
 *
 *  Revision 1.3  2000/06/13 20:41:00  dennis
 *  now supports evaluation mode to select between Peak, Background and
 *  Peak plus Background
 *
 *  Revision 1.2  2000/05/11 16:09:42  dennis
 *  added RCS logging
 */

package  DataSetTools.peak;

import java.io.*;
import DataSetTools.math.*;
import DataSetTools.dataset.*;

/**
 * This class implements the basic data and operations on a Gaussian 
 * distribution model of a peak in neutron scattering data based.
 */

public class GaussianPeak extends    ModelPeak
                          implements Serializable
{
                            // conversion constant to switch between the 
                            // standard deviation, sigma, and the full width,
                            // half max. ( fwhm = sigma * SIGMA_TO_FWHM )     
  public static final float SIGMA_TO_FWHM = 
                                     (float)(2.0*Math.sqrt(2.0*Math.log(2.0)));
  public static final double ROOT_2_PI = Math.sqrt(2.0*Math.PI);

  /**
   *  Construct a new GaussianPeak object with the specified characteristics 
   */
  public  GaussianPeak( float position, 
                        float amplitude, 
                        float fwhm, 
                        float slope,
                        float intercept )
   {
     super( GAUSSIAN_PEAK, position, amplitude, fwhm, slope, intercept );
   }

  /**
   *  Calculates the parameters for the GAUSSIAN distribution that best fits
   *  the model peak to the given Data block on the specified interval.
   *
   *  @param  data      The Data block containing the data to fit.
   *  @param  start_x   The left hand endpoint for the interval on which the
   *                    fit is to be calculated.
   *  @param  end_x     The right hand endpoint for the interval on which the
   *                    fit is to be calculated.
   */
  public boolean FitPeakToData( Data  data,
                                float start_x,
                                float end_x   )
  {
    float x_vals[] = data.getX_scale().getXs();
    float y_vals[] = data.getY_values();

    intercept = 0;
    slope = 0;

//  linear background not working yet......
/*
                                                      // find midpoints of bins
    float x_mids[] = new float[ x_vals.length - 1 ];
    for ( int i = 0; i < x_mids.length; i++ )
      x_mids[i] = ( x_vals[i] + x_vals[i+1] ) /2.0f;

                                                     // fit a linear background
    float line_params[] = Statistics.FitLine( x_mids, y_vals );
    slope     = line_params[0];
    intercept = line_params[1];
    System.out.println("Slope, intercept = " + slope + ", " + intercept );
                                          // adjust intercept to keep y >= 0
    float min  = 0;
    float diff = 0;
    for ( int i = 0; i < x_mids.length; i++ )
    {
      diff = y_vals[i] - ( slope*x_mids[i] + intercept); 
      if ( diff < min )
        min = diff; 
    }
    intercept += diff;
    System.out.println("Slope, intercept = " + slope + ", " + intercept );
                                          // subtract off the linear background
    for ( int i = 0; i < y_vals.length; i++ )
      y_vals[i] = y_vals[i] - ( slope*x_mids[i] + intercept);
*/
                                            // set the peak position using
                                            // the first moment
    float area = NumericalAnalysis.IntegrateHistogram( x_vals, y_vals,
                                                       start_x, end_x );
    float moment_1 = NumericalAnalysis.HistogramMoment( x_vals, y_vals,
                                                        start_x, end_x, 0, 1 );
    position = moment_1/area;

                                            // set the standard deviation 
                                            // based on the second moment
    float moment_2 = NumericalAnalysis.HistogramMoment( x_vals, y_vals,
                                                        start_x, end_x, 
                                                        position, 2 );
    float sigma =(float)Math.sqrt( moment_2/area );
    setSigma( sigma );
                                            // set the amplitude so that the
                                            // areas above half max match.
    setAmplitude( 1 );
    float peak_area = Area( position-fwhm/2, position+fwhm/2 );
    float data_area = NumericalAnalysis.IntegrateHistogram( x_vals, y_vals,
                                     position-fwhm/2, position+fwhm/2 );
    setAmplitude( data_area/peak_area );
    return true;
  }

  /**
   *  Get the standard deviation for this peak.  Note: The standard deviation
   *  and FWHM are related by a constant.
   */ 
   public float getSigma()
   {
     return fwhm / SIGMA_TO_FWHM;
   }

  /**
   *  Set the standard deviation for this peak.  Note: Since the standard 
   *  deviation and FWHM are related by a constant, this actually just sets
   *  the fwhm variable to a multiple of the specified sigma. 
   */
   public void setSigma( float sigma )
   {
     fwhm = sigma * SIGMA_TO_FWHM;
   }


  /**
   *  Evaluate the y-value of the peak at the specified x-value.
   *
   *  @param  x  the point at which the peak is to be evaluated
   *
   *  @return the value of the peak, background or peak plus background
   *          at the specified point, depending on the evaluation mode
   *          (see method: setEvaluationMode())
   */
  public double getValue( double x )
  {
    double sigma = getSigma();
    
    if ( Math.abs( x - position ) < extent_factor*fwhm/2 )
    {
     if ( eval_mode == PEAK_ONLY )
       return amplitude*Math.exp( -(x-position)*(x-position)/(2.0*sigma*sigma));

     else if ( eval_mode == BACKGROUND_ONLY )
        return x * slope + intercept;

     else 
        return amplitude*Math.exp( -(x-position)*(x-position)/(2.0*sigma*sigma))
                   + x * slope + intercept;
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
      System.out.println("With standard normal distribution");
      GaussianPeak peak = new GaussianPeak(0, 
                                           (float)(1.0/ROOT_2_PI), 
                                           (float)SIGMA_TO_FWHM, 0, 0);

      System.out.println("Area within one standard deviation is "+
                          peak.Area( -1, 1 ) );

      System.out.println("Moments about position are: ");
      for ( int i = 0; i < 6; i++ )
        System.out.println("N = " + i + ",  Nth moment = " + 
                            peak.Moment( -1, 1, i ) ); 

      System.out.println("Moments about 0 are: ");
      for ( int i = 0; i < 6; i++ )
        System.out.println("N = " + i + ",  Nth moment = " + 
                            peak.Moment( -1, 1, 0, i ) );

      peak = new GaussianPeak( 1494, 60635, 8.7f, 0, 0); 
      System.out.println("With new peak, moments about position are: ");
      for ( int i = 0; i < 6; i++ )
        System.out.println("N = " + i + ",  Nth moment = " +
                            peak.Moment( 1480, 1500, i ) );

      System.out.println("With new peak, moments about position are: ");
      for ( int i = 0; i < 6; i++ )
        System.out.println("N = " + i + ",  Nth moment = " +
                            peak.Moment( 1480, 1500, 1494, i ) );

    }
}

/*
 * File:  IPeak.java
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.4  2002/11/27 23:22:56  pfpeterson
 *  standardized header
 *
 */

package  DataSetTools.peak;

import java.io.*;
import DataSetTools.dataset.*;

/**
 * This class forms the abstract base class for sampled and model "peaks"
 * in neutron scattering data.
 *
 * @see  ModelPeak 
 * @see  HistogramDataPeak
 * @see  Data 
 * @see  XScale
 */

public interface IPeak
{
  public static final int  HISTOGRAM_DATA_PEAK  = 0;
  public static final int  GAUSSIAN_PEAK        = 1;
  public static final int  LORENTZIAN_PEAK      = 2;

  public static final int  PEAK_ONLY            = 0;
  public static final int  BACKGROUND_ONLY      = 1;
  public static final int  PEAK_PLUS_BACKGROUND = 2;
  /**
   *  Return the integer code for the peak shape for this peak.  
   *  For a "ModelPeak" this returns the theoretical peak shape being used.  
   *  For a "DataPeak" this returns the peak shape that is currently being 
   *  used to fit the data. 
   */
  abstract public int getShape();

  /**
   *  Get the maximum height of the peak.
   */
  abstract public float getAmplitude();

  /**
   *  Get the position of the peak "center".
   */
  abstract public float getPosition();

  /**
   *  Get the "Full Width at Half Max" of the peak.
   */
  abstract public float getFWHM();

  /**
   *  Return the size of the interval over which this peak is to be used.  
   *  The size is expressed in terms of the FWHM of the peak.  In particular,
   *  if this function returns 5 then the peak is assumed to be zero outside
   *  of an interval of length 5*FWHM centered at the peak position.
   */
  abstract public float getExtent_factor();

  /**
   *  Set the size of the interval over which this peak is to be used.
   *  The size is expressed in terms of the FWHM of the peak.  In particular, 
   *  if 5 is specified as the factor then the peak is assumed to be zero 
   *  outside of an interval of length 5*FWHM centered at the peak position.
   */
  abstract public void  setExtent_factor( float factor );
  
  /**
   *  Get the slope of the linear background for this peak.
   */
  abstract public float getSlope();

  /**
   *  Get the y-intercept of the linear background for this peak.
   */
  abstract public float getIntercept();


  /**
   *  Choose between evaluating the peak only, background only or peak plus 
   *  background when calling getValue, evaluating area and moments and getting
   *  the peak data.
   *
   *  @param  mode   the mode to use, must be one of the values:
   *
   *                    PEAK_ONLY 
   *                    BACKGROUND_ONLY 
   *                    PEAK_PLUS_BACKGROUND 
   */
  abstract public void setEvaluationMode( int mode );


  /**
   *  Evaluate the y-value of the peak at the specified x-value.
   *
   *  @param  x  the point at which the peak is to be evaluated
   *
   *  @return the value of the peak, background or peak plus background 
   *          at the specified point, depending on the evaluation mode.
   *          (see method: setEvaluationMode()) 
   */
  abstract public double getValue( double x );

  /**
   *  Return a Data block that contains the y-values of the peak at a 
   *  default set of x-values.
   *
   *  @return a reference to a Data block that contains the y-values of the
   *  peak only, the background or the peak plus background (depending on
   *  the evaluation mode) at a default set of x-values.
   *  (see method: setEvaluationMode()) 
   *
   */
  abstract public Data  PeakData( );

  /**
   *  Return a Data block that contains the y-values of the peak at the 
   *  set of x-values given by the XScale interval.
   *
   *  @param interval  specifies the set of x-values at which the peak is to
   *                   be evaluated.
   *
   *  @return a reference to a Data block that contains the y-values of the
   *  peak only, the background or the peak plus background (depending on
   *  the evaluation mode) at the set of x-values given by the XScale interval.
   *  (see method: setEvaluationMode()) 
   *
   */
  abstract public Data  PeakData( XScale interval );

  /**
   *  Calculate the area under the curve over the interval [a,b].
   *
   *  @param  a        Left endpoint of [a,b].
   *  @param  b        Right endpoint of [a,b].
   *
   *  @return The area under the peak only, the background or the peak plus
   *          background depending on the evaluation mode that has been
   *          set.   (see method: setEvaluationMode()) 
   *
   */
  abstract public float Area( float a, float b );


  /**
   *  Calculate the area under the curve over the current default peak extent.
   *
   *  @return The area under the peak only, the background or the peak plus
   *          background depending on the evaluation mode that has been
   *          set.   (see method: setEvaluationMode())
   */
  abstract public float Area( );


  /**
   *  Calculate the "nth" moment of the peak about the peak position over the 
   *  interval [a,b].  The function  f(x)*(x-position)**n is
   *  integrated over the specified interval, where "f()" is the peak function
   *  and "position" is the postion of the peak.
   *
   *  @param  a        Left endpoint of [a,b].
   *  @param  b        Right endpoint of [a,b].
   *  @param  n        specifies the order of the moment to calculate. If 
   *                   n <= 0, the area is returned.  
   *
   *  @return The moment of the peak only, the background or the peak plus
   *          background depending on the evaluation mode that has been
   *          set.   (see method: setEvaluationMode()) 
   */
  abstract public float Moment( float a, float b, int n ); 



  /**
   *  Calculate the "nth" moment of the peak about the peak position over the
   *  current default peak extent.  The function  f(x)*(x-position)**n is
   *  integrated over the specified interval, where "f()" is the peak function
   *  and "position" is the postion of the peak.
   *
   *  @param  n        specifies the order of the moment to calculate. If
   *                   n <= 0, the area is returned.
   *
   *  @return The moment of the peak only, the background or the peak plus
   *          background depending on the evaluation mode that has been
   *          set.   (see method: setEvaluationMode())
   */
  abstract public float Moment( int n );




  /**
   *  Calculate the "nth" moment of the peak about the specified center point
   *  over the interval [a,b].  The function  f(x)*(x-center)**n is
   *  integrated over the specified interval, where "f()" is the peak function
   *  and "center" is the specified center point.
   *
   *  @param  a        Left endpoint of [a,b].
   *  @param  b        Right endpoint of [a,b].
   *  @param  center   the center point for the moment calculation. 
   *  @param  n        specifies the order of the moment to calculate. If 
   *                   n <= 0, the area is returned.
   *  @return The moment of the peak only, the background or the peak plus
   *          background depending on the evaluation mode that has been
   *          set. (see method: setEvaluationMode()) 
   *
   */
  abstract public float Moment( float a, float b, float center, int n ); 



  /**
   *  Calculate the "nth" moment of the peak about the specified center point
   *  over the current default peak extent.  The function  f(x)*(x-center)**n is
   *  integrated over the specified interval, where "f()" is the peak function
   *  and "center" is the specified center point.
   *
   *  @param  center   the center point for the moment calculation.
   *  @param  n        specifies the order of the moment to calculate. If
   *                   n <= 0, the area is returned.
   *  @return The moment of the peak only, the background or the peak plus
   *          background depending on the evaluation mode that has been
   *          set. (see method: setEvaluationMode())
   *
   */
  abstract public float Moment( float center, int n );


}

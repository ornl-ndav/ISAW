/*
 * @(#)ModelPeak.java     0.1  2000/03/06  Dennis Mikkelson
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.1  2000/07/10 22:49:00  dennis
 *  New classes to deal with peaks/peak fitting
 *
 *  Revision 1.4  2000/06/15 16:49:20  dennis
 *  improved documentation
 *
 *  Revision 1.3  2000/06/13 20:37:33  dennis
 *  added setEvaluationMode to choose between Peak, Background and
 *  Peak+Background modes.
 *
 *  Revision 1.2  2000/05/11 16:09:42  dennis
 *  added RCS logging
 *
 *
 */
package  DataSetTools.peak;

import java.io.*;
import DataSetTools.math.*;
import DataSetTools.dataset.*;

/**
 * This class encapsulates the basic data and operations on models of "peaks" 
 * in neutron scattering data based on various statistical distributions.
 */

abstract public class ModelPeak implements IPeak,
                                           IOneVariableFunction,
                                           Serializable
{
  protected int    peak_shape;   // the distribution used (GAUSSIAN, 
                                 // LORENTZIAN...)
  protected float  position;
  protected float  amplitude;          
  protected float  fwhm;         // full width half max for the peak

  protected float  extent_factor = 5; 
                               // when data points are calculated, they will 
                               // be zero outside of an interval of length
                               // extent * fwhm centered at the peak position. 

  protected float  slope;      // slope and y-intercept for linear  
  protected float  intercept;  // background for this peak
  protected int    eval_mode = PEAK_ONLY; 

  /**
   *  Construct a new ModelPeak object with the specified characteristics.
   *
   *  @param  peak_shape   Specifies which type of statistical distribution is
   *                       used for this ModelPeak.
   *  @param  position     Specifies the position of peak.
   *  @param  amplitude    Spcifies the maximum value of the peak.
   *  @param  fwhm         Specifies the "Full Width at Half Max" for the peak.
   *  @param  slope        Specifies the slope of a linear background function
   *                       for the peak. 
   *  @param  intercept    Specifies the y-intercept for a linear background
   *                       for the peak.
   *  @see IPeak
   *  @see GaussianPeak 
   */
  public  ModelPeak( int   peak_shape,
                     float position, 
                     float amplitude, 
                     float fwhm, 
                     float slope,
                     float intercept )
   {
     this.peak_shape = peak_shape;
     this.position   = position;
     setAmplitude( amplitude );
     setFWHM( fwhm );
     this.slope      = slope;
     this.intercept  = intercept;
   }

  /**
   *  Calculates the parameters for the model peak that best fits the 
   *  model peak to the given Data block on the specified interval. 
   *
   *  @param  data      The Data block containing the data to fit.  
   *  @param  start_x   The left hand endpoint for the interval on which the
   *                    fit is to be calculated.
   *  @param  end_x     The right hand endpoint for the interval on which the
   *                    fit is to be calculated.
   */
  abstract public boolean FitPeakToData( Data  data,
                                         float start_x,
                                         float end_x   );

  /**
   *  Return the integer code for the peak shape for this peak.
   */
  public int getShape()
  {
    return peak_shape;
  }

  /**
   *  Get the maximum height of the peak.
   */
  public float getAmplitude()
  {
    return amplitude;
  }

  /**
   *  Set the maximum height of the peak.
   *
   *  @param  amplitude    Spcifies the maximum value of the peak.  This MUST
   *                       be greater than or equal to zero.
   */
  public boolean setAmplitude( float amplitude )
  {
    if ( amplitude >= 0 )
    {
      this.amplitude = amplitude;
      return true; 
    }
    else
    {
      System.out.println("ERROR: amplitude of peak < 0 in setAmplitude");
      return false;
    }
  }

  /**
   *  Get the position of the peak "center".
   */
  public float getPosition()
  {
    return position;
  }

  /**
   *  Set the position of the peak "center".
   *
   *  @param  position     Specifies the position of peak.
   */
  public void setPosition( float position )
  {
    this.position = position;
  }

  /**
   *  Get the "Full Width at Half Max" of the peak.
   */
  public float getFWHM()
  {
    return fwhm;
  }

  /**
   *  Set the "Full Width at Half Max" of the peak.
   *
   *  @param  fwhm         Specifies the "Full Width at Half Max" for the peak.
   */
  public boolean setFWHM( float fwhm )
  {
    if ( fwhm >= 0 )
    {
      this.fwhm = fwhm;
      return true; 
    }
    else
    {
      System.out.println("ERROR: fwhm of peak < 0 in setFWHM");
      return false;
    }
  }

  /**
   *  Return the size of the interval over which this peak is to be used.
   *  The size is expressed in terms of the FWHM of the peak.  In particular,
   *  if this function returns 5 then the peak is assumed to be zero outside
   *  of an interval of length 5*FWHM centered at the peak position.
   */
  public float getExtent_factor()
  {
    return extent_factor;
  }

  /**
   *  Set the size of the interval over which this peak is to be used.
   *  The size is expressed in terms of the FWHM of the peak.  In particular,
   *  if 5 is specified as the factor then the peak is assumed to be zero
   *  outside of an interval of length 5*FWHM centered at the peak position.
   */
  public void setExtent_factor( float factor )
  {
    extent_factor = factor;
  } 

  /**
   *  Get the slope of the linear background for this peak.
   *
   *  @param  slope        Specifies the slope of a linear background function
   *                       for the peak.
   */
  public float getSlope()
  {
    return slope;
  }

  /**
   *  Set the slope of the linear background for this peak.
   */
  public void setSlope( float slope )
  {
    this.slope = slope;
  }

  /**
   *  Get the y-intercept of the linear background for this peak.
   */
  public float getIntercept()
  {
    return intercept;
  }

  /**
   *  Set the y-intercept of the linear background for this peak.
   *
   *  @param  intercept    Specifies the y-intercept for a linear background
   *                       for the peak.
   */
  public void setIntercept( float intercept )
  {
    this.intercept = intercept;
  }

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
  public void setEvaluationMode( int mode )
  {
    if ( PEAK_ONLY <= mode && mode <= PEAK_PLUS_BACKGROUND )
      eval_mode = mode;
    else
      System.out.println("ERROR: invalid mode in setEvaluationMode()");
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
  abstract public double getValue( double x );


  /**
   *  Return a reference to a Data block that contains the y-values of the
   *  peak at a default set of x-values.
   *
   *  @return a reference to a Data block that contains the y-values of the
   *  peak only, the background or the peak plus background (depending on
   *  the evaluation mode) at a default set of x-values.
   *  (see method: setEvaluationMode())
   */
  public Data  PeakData( )
  {
    float start_x = position - extent_factor * fwhm/2;
    float end_x   = position + extent_factor * fwhm/2;

    XScale interval = new UniformXScale( start_x, end_x, 100 );
    return PeakData( interval );
  }


  /**
   *  Return a reference to a Data block that contains the y-values of the
   *  peak at the set of x-values given by the XScale "interval".
   *
   *  @param  interval  specifies the set of x-values at which the peak is to
   *                    be evaluated.
   *
   *  @return a reference to a Data block that contains the y-values of the
   *  peak only, the background or the peak plus background (depending on
   *  the evaluation mode) at the set of x-values given by the XScale interval.
   *  (see method: setEvaluationMode())
   */
  public Data  PeakData( XScale interval )
  {
    float x_vals[] = interval.getXs();
    float y_vals[] = new float[ x_vals.length ];

    for ( int i = 0; i < x_vals.length; i++ )
      y_vals[i] = (float)getValue( x_vals[i] );

    return new Data( interval, y_vals, 1 );
  }


  /**
   *  Calculate the area under the curve over the interval [a,b].
   *
   *  @param  a        Left endpoint of [a,b].
   *  @param  b        Right endpoint of [a,b].
   *
   *  @return The area under the peak only, the background or the peak plus
   *          background depending on the evaluation mode that has been
   *          set.  (see method: setEvaluationMode())
   *
   */
  public float Area( float a, float b )
  {
    return NumericalAnalysis.RombergIntegrate( this, a, b, 0.000001f, 15 );
  }


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
   *          set.  (see method: setEvaluationMode())
   *
   */
  public float Moment( float a, float b, int n )
  {
    return NumericalAnalysis.FunctionMoment( this, a, b, position, n );
  }


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
   *
   *  @return The moment of the peak only, the background or the peak plus
   *          background depending on the evaluation mode that has been
   *          set.  (see method: setEvaluationMode())
   *
   */

  public float Moment( float a, float b, float center, int n )
  {
    return NumericalAnalysis.FunctionMoment( this, a, b, center, n );
  }

}

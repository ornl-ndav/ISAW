/*
 * @(#)HistogramDataPeak.java     0.1  2000/06/12  Dennis Mikkelson
 *
 * ---------------------------------------------------------------------------
 *
 *  $Log$
 *  Revision 1.1  2000/07/10 22:48:58  dennis
 *  New classes to deal with peaks/peak fitting
 *
 *  Revision 1.3  2000/06/15 16:49:20  dennis
 *  improved documentation
 *
 *  Revision 1.2  2000/06/14 21:15:07  dennis
 *  Placed fitting of linear background in a private method.
 *  Added PrintPeakInfo() to show basic information about the peak.
 *
 *  Revision 1.1  2000/06/14 19:25:18  dennis
 *  Initial revision
 *
 *
 */

package  DataSetTools.peak;

import java.io.*;
import DataSetTools.math.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*; 


/**
 * This class encapsulates the basic data and operations on a peak given
 * by a Data block.  The Data block is assumed to contain a single isolate
 * peak. 
 */

public class HistogramDataPeak implements IPeak,
                                                   Serializable
{
  protected int    peak_shape = HISTOGRAM_DATA_PEAK;
  protected int    position_index;  // indices of peak max, and last left and
  protected int    right_HM_index;  // right points where the data > half max 
  protected int    left_HM_index;

  protected float  extent_factor = 15; 
                               // when data points are calculated, they will 
                               // be zero outside of an interval of length
                               // extent * fwhm centered at the peak position. 

  protected float  slope = 0;       // slope and y-intercept for linear  
  protected float  intercept = 0;;  // background for this peak
  protected float  x_vals[] = null;
  protected float  y_vals[] = null;

  protected int    eval_mode = PEAK_ONLY;

  /**
   *  Construct a new HistogramDataPeak object from the specified Data block.
   *
   *  @param  data  The data block containing the peak.
   *  @see IPeak
   */
  public  HistogramDataPeak( Data  data )
   {
     if ( data == null )
     {
       System.out.println("ERROR null data in HistogramPeakData" );
       return;
     }

     x_vals        = data.getX_scale().getXs();
     if ( x_vals == null || x_vals.length < 23 )
     {
       System.out.println( 
                 "ERROR: too few x values in Data block in HistogramPeakData"); 
       x_vals = null;
       return;
     }

     float temp[]  = data.getY_values();
     if ( temp == null || temp.length < 22 )
     {
       System.out.println( 
                 "ERROR: too few y values in Data block in HistogramPeakData");
       return;
     }
     y_vals = new float[temp.length];
     System.arraycopy( temp, 0, y_vals, 0, temp.length );

     if ( y_vals.length < 1 || x_vals.length != y_vals.length + 1 )
     {
       System.out.println("ERROR: invalid Data lengths in HistogramPeakData "+
                          "constructor" );
       System.out.println("X's:" + x_vals.length+ "  Y's:" + y_vals.length );
       return;
     }

     position_index = 0;                          // search for peak value
     float amplitude  = y_vals[0];
     for ( int i = 0; i < y_vals.length; i++ )
       if ( y_vals[i] > amplitude )
       {
         amplitude = y_vals[i];
         position_index = i;
       } 
                                                   // set FWHM by searching
                                                   // left and right of peak
                                                   // to find half-max
     right_HM_index = position_index;
     boolean half_max_found = false;
     while ( right_HM_index < y_vals.length && !half_max_found )
     {
       if ( y_vals[right_HM_index] < amplitude/2 )
         half_max_found = true;
       else
         right_HM_index++;
     }
     right_HM_index--;                          // step back to val >= half_max 

     half_max_found = false;
     left_HM_index = position_index;
     while ( left_HM_index >= 0 && !half_max_found )
     {
       if ( y_vals[left_HM_index] < amplitude/2 )
         half_max_found = true;
       else
         left_HM_index--;
     }
     left_HM_index++;                          // step back to val >= half_max 

     calculate_linear_background( extent_factor/2 );
   }

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
    return y_vals[ position_index ]; 
  }

  /**
   *  Get the position of the peak "center".  The peak position is considered
   *  to be a the center of the bin with the maximum count.
   *
   *  @return the position of the center of the bin with the maximum count.
   */
  public float getPosition()
  {
     return (x_vals[position_index] + x_vals[position_index+1] ) / 2; 
  } 


  /**
   *  Get the "Full Width at Half Max" of the peak.
   */
  public float getFWHM()
  {
    return x_vals[right_HM_index+1] - x_vals[left_HM_index];
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
   *  Get the y-intercept of the linear background for this peak.
   */
  public float getIntercept()
  {
    return intercept;
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
  public double getValue( double x )
  {
    if ( x < getPosition() - extent_factor/2 * getFWHM()  ||
         x > getPosition() + extent_factor/2 * getFWHM()  )
      return 0;

    if ( x < x_vals[0] || x > x_vals[ x_vals.length-1 ] )
      return 0;
 
    double value;
    if ( eval_mode == PEAK_ONLY )
    {
      int index = arrayUtil.get_index_of( (float)x, x_vals );
      value = y_vals[ index ]; 
    }
    else if ( eval_mode == BACKGROUND_ONLY )
      value = getBackgroundValue( x );
    else
    {
      int index = arrayUtil.get_index_of( (float)x, x_vals );
      value = y_vals[ index ] - getBackgroundValue( x );
    }
      
    return value; 
  }


  /**
   *  Return a reference to a Data block that contains the y-values of the
   *  peak at a default set of x-values.
   *
   *  @return a reference to a Data block that contains the y-values of the
   *  peak only, the background or the peak plus background (depending on
   *  the evaluation mode) at a default set of x-values.
   *  (see method: setEvaluationMode())
   *
   */
  public Data PeakData( )
  {
    float start_x = getPosition() - extent_factor * getFWHM()/2;
    float end_x   = getPosition() + extent_factor * getFWHM()/2;

    XScale interval = new UniformXScale( start_x, end_x, 100 );
    return PeakData( interval );
  }


  /**
   *  Return a reference to a Data block that contains the y-values of the
   *  peak at the set of x-values given by the XScale "interval".
   *
   *  @param  interval  specifies the set of x-values at which the peak is to
   *                   be evaluated.
   *  @return a reference to a Data block that contains the y-values of the
   *  peak only, the background or the peak plus background (depending on
   *  the evaluation mode) at the set of x-values given by the XScale interval.
   *  (see method: setEvaluationMode())
   */
  public Data PeakData( XScale interval )
  {
    float x_vals[] = interval.getXs();
    float y_vals[] = new float[ x_vals.length-1 ];

    for ( int i = 0; i < y_vals.length; i++ )
      y_vals[i] = (float)getValue( (x_vals[i]+x_vals[i+1])/2 );

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
    float y[] = getEffectiveYValues();
    return NumericalAnalysis.IntegrateHistogram( x_vals, y, a, b );
  }


  /**
   *  Calculate the "nth" moment of the peak about the peak position over the
   *  interval [a,b].  The values  y(x) * (x-position)**n are summed over
   *  over the interval.  Here x means the position of the bin centers 
   *  and "position" is the postion of the peak.
   *
   *  @param  a        Left endpoint of [a,b].
   *  @param  b        Right endpoint of [a,b].
   *  @param  n        specifies the order of the moment to calculate. If
   *                   n <= 0, the area is returned. 
   *  @return The moment of the peak only, the background or the peak plus
   *          background depending on the evaluation mode that has been
   *          set.  (see method: setEvaluationMode())
   *
   */
  public float Moment( float a, float b, int n )
  {
    return Moment( a, b, getPosition(), n );
  }


  /**
   *  Calculate the "nth" moment of the peak about the specified center point
   *  over the interval [a,b].  The values y(x)*(x-center)**n are summed over 
   *  the interval.  Here x means the position of the bin centers
   *  and "center" is the specified center point.
   *
   *  @param  a        Left endpoint of [a,b].
   *  @param  b        Right endpoint of [a,b].
   *  @param  center   the center point for the moment calculation.
   *  @param  n        specifies the order of the moment to calculate. If
   *                   n <= 0, the area is returned.
   *  @return The moment of the peak only, the background or the peak plus
   *          background depending on the evaluation mode that has been
   *          set.  (see method: setEvaluationMode())
   *
   */

  public float Moment( float a, float b, float center, int n )
  {
    float y[] = getEffectiveYValues();
    return NumericalAnalysis.HistogramMoment( x_vals, y, a, b, center, n );
  }


 /**
  *  Print basic information about the peak, background or peak plus 
  *  background depending on the evaluation mode.
  *
  *  @param  label            A label that will be printed on a line before the
  *                           peak information.
  *  @param  evaluation_mode  A mode flag to indicate whether the data should
  *                           be about the peak alone, background alone or
  *                           peak plus background.  The mode to use, must be 
  *                           one of the values:
  *
  *                              PEAK_ONLY
  *                              BACKGROUND_ONLY
  *                              PEAK_PLUS_BACKGROUND
  *
  *                           (see method: setEvaluationMode())
  *  
  */ 
  public void PrintPeakInfo( String label, int evaluation_mode )
  {
    int old_eval_mode = eval_mode;

    setEvaluationMode( evaluation_mode );

    float position = getPosition();
    float fwhm     = getFWHM();
    float area = Area( position-2.5f*fwhm, position+2.5f*fwhm);
    float centroid = Moment( position-2.5f*fwhm, position+2.5f*fwhm, 0, 1)/area;
    float variance = Moment( position-2.5f*fwhm, position+2.5f*fwhm, 2) / area;

    System.out.println( label );
    System.out.println("position = " + position );
    System.out.println("fwhm     = " + fwhm );
    System.out.println("area     = " + area );
    System.out.println("centroid = " + centroid );
    System.out.println("variance = " + variance );
    System.out.println("sd       = " + Math.sqrt(variance) );

    setEvaluationMode( old_eval_mode );
  }

/*---------------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

  /*
   *  Get the list of y_values to use for calculating the area and moment.
   *  This will be the original y_values, the original y_values minus 
   *  background or the background y_values alone depending n the evaluation
   *  mode.
   */
  private float[] getEffectiveYValues()
  {
    if ( eval_mode == PEAK_ONLY )
    { 
      float temp_y[] = new float[ y_vals.length ];
                                                  // subtract background
      for ( int i = 0; i < y_vals.length; i++ )
        temp_y[i] = y_vals[i] - 
                    (float)getBackgroundValue( (x_vals[i] + x_vals[i+1])/2 );
      
      return temp_y; 
    }  
    else if ( eval_mode == BACKGROUND_ONLY )
    {
      float temp_y[] = new float[ y_vals.length ];
                                                  // evaluate background
      for ( int i = 0; i < y_vals.length; i++ )
        temp_y[i] = (float)getBackgroundValue( (x_vals[i] + x_vals[i+1])/2 );            
      return temp_y;
    } 
    else 
      return y_vals;
  }


  /*
   *  Get the y value of the linear background function for this peak at
   *  the specified x value.
   *
   *  @param  x   the point at which the background is to be evaluated.
   *
   *  @return  The value of the linear background at the specified point.
   */
  private double getBackgroundValue( double x )
  {
    return x * slope + intercept;
  }


 /*
  *  Find linear background based on averages of 11 channels left of the peak
  *  by a multiple of FWHM and 11 channels right of the peak by a multiple of 
  *  FWHM.
  *  
  *  @param  width_factor  The multiple of one FWHM that the interval
  *                        extends left and right. 
  */

  private void calculate_linear_background( float width_factor )
  {
     float x_min = getPosition() - getFWHM() * width_factor;
     float x_max = getPosition() + getFWHM() * width_factor;

     int  i_min = arrayUtil.get_index_of( x_min, x_vals );
     int  i_max = arrayUtil.get_index_of( x_max, x_vals );

     if ( i_min - 5  < 0 )                   // make sure that the indices stay
       i_min = 5;                            // valid

     if ( i_max + 5 >= y_vals.length )
       i_max = y_vals.length - 6;

     float x_sum = 0;                            // average points left of
     float y_sum = 0;                            // the peak
     for ( int i = i_min-5; i <= i_min+5; i++ )
     {
       y_sum += y_vals[i];
       x_sum += (x_vals[i] + x_vals[i+1])/2;
     }
     float x1 = x_sum/11;
     float y1 = y_sum/11;

     x_sum = 0;                                   // average points right of
     y_sum = 0;                                   // the peak
     for ( int i = i_max-5; i <= i_max+5; i++ )
     {
       y_sum += y_vals[i];
       x_sum += (x_vals[i] + x_vals[i+1])/2;
     }
     float x2 = x_sum/11;
     float y2 = y_sum/11;
                                               // calculate background as
                                               // line joining averaged points
     slope = (y2 - y1)/(x2 - x1);
     intercept = y1 - slope * x1;
  }

}

/*
 * @(#)tof_calc.java        1.00 98/08/03  Dennis Mikkelson
 *
 *  Ported to Java from tof_vis_calc.c
 * 
 *  $Log$
 *  Revision 1.6  2000/07/31 13:36:47  dennis
 *  Added methods to "smooth" functions by averaging nearby points.  One version
 *  just smooths the functions, the other version also calculates new error values.
 *
 *  Revision 1.5  2000/07/26 20:47:00  dennis
 *  Now allow zero parameter in velocity<->energy conversions
 *
 *  Revision 1.4  2000/07/17 19:07:29  dennis
 *  Added methods to convert between energy and wavelength
 *
 *  Revision 1.3  2000/07/14 19:10:23  dennis
 *  Added methods to convert between velocity and energy and between
 *  velocity and wavelength.  Also added documentation to clarify the
 *  units used for parameters.
 *
 *  Revision 1.2  2000/07/10 22:25:15  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.5  2000/07/06 21:19:59  dennis
 *  added some constants for Planck's constant and added VelocityOfEnergy()
 *  function.
 *
 *  Revision 1.3  2000/05/11 16:08:13  dennis
 *  Added RCS logging
 *
 */

package DataSetTools.math;


public final class tof_calc
{

/* --------------------------------------------------------------------------

   CONSTANTS

*/

                                                         // mass of neutron(kg)
public static final float  MN_KG        = 1.67492716e-27f;

public static final float  JOULES_PER_meV=  1.602176462e-22f;

                                                      //h in Joule seconds
public static final float  H_JS          =  6.62606876e-34f;

                                                      // h in erg seconds
public static final float  H_ES          =  6.62606876e-27f;

                                                      // h_bar in Joule seconds
public static final float  H_BAR_JS      =  1.05457160e-34f;

                                                      // h in erg seconds
public static final float  H_BAR_ES      =  1.05457160e-27f;   


public static final float  meV_per_mm_per_us_2 = 5.227037f;    // meV/(mm/us)^2 

public static final float  ANGST_PER_US_PER_M  = 3.956058e-3f;

public static final float  ANGST_PER_US_PER_MM = 3.956058f;

public static final float  RADIANS_PER_DEGREE  = 0.01745332925f;
 
  /**
   * Don't let anyone instantiate this class.
   */
  private tof_calc() {}


  /* ------------------------------ ReBin ---------------------------------- */
  /**
   * Constructs a new histogram by rearranging the counts of an input histogram
   * into a new set of bins in a new histogram.  If a new histogram bin 
   * overlaps part of a bin of the input histogram, a number of counts
   * proportional to the amount of the input bin covered by the new bin is 
   * assigned to the new bin.  If a new histogram bin entirely contains an
   * input histogram bin, all of the counts from the input bin are assigned 
   * to the new bin.  The bins of the new histogram recieve the total of all 
   * such counts from bins in the input histogram that they contain, or 
   * partially overlap.  Also see the method "ResampleBin".
   *
   * @param   iX[]      Array of bin boundaries for the input histogram.  These
   *                    can be an arbitrary non-decreasing sequence of X values.
   * @param   iHist[]   Array of histogram values for the input histogram. The
   *                    length of iHist[] must be one less than the length of
   *                    iX[].
   * @param   nX[]      Array of bin boundaries for the new histogram.  These
   *                    can be an arbitrary non-decreasing sequence of X values.
   * @param   nHist[]   Array of histogram values for the input histogram. The
   *                    length of nHist[] must be one less than the length of
   *                    nX[].
   */

  public static boolean ReBin( float iX[], float iHist[], 
                               float nX[], float nHist[] )
  {
    int  num_i, num_n;
    int  n, k, i, il;
    float  nXa, nXb,
           iXa, iXb,
           iXmin, iXmax,
           nXmin, nXmax;
    float  iXtemp;
    float  sum;

                                /* do some basic checks on validity of data */
    num_i = iX.length - 1;
    if ( num_i <= 0 )
    {
      System.out.println("ERROR in ReBin ... not enough input X values");
      return( true );
    }
    iXmin = iX[ 0 ];
    iXmax = iX[ num_i ];

    num_n = nX.length - 1;
    if ( num_n <= 0 )
    {
      System.out.println("ERROR in ReBin ... not enough output X values");
      return( false );
    }
    nXmin = nX[ 0 ];
    nXmax = nX[ num_n ];

    if ( iHist.length != num_i )
    {
      System.out.println("ERROR in ReBin ... iHist size wrong");
      return( false );
    }
    if ( nHist.length != num_n )
    {
      System.out.println("ERROR in ReBin ... nHist size wrong");
      return( false );
    }

                                              /* check for degenerate cases */
    if ( (nXmax <= iXmin) || (nXmin >= iXmax) ||
         (nXmin >= nXmax) || (iXmin >= iXmax)  )
    {
      for ( n = 0; n < nHist.length; n++ )
        nHist[n] = 0.0f;
      return( true );
    }

                                  /* advance on new interval to first point */
                                  /* nXa  >= start of input interval.       */
    n   = 0;
    nXa = nXmin;
    while ( nXa < iXmin )
    {
      nHist[n] = 0.0f;
      n++;
      nXa = nX[n];
    }
                               /* advance on input interval to first point  */
                               /* >= nXa (if possible).  As we go, find sum */
                               /* of initial bins in input histogram.       */
    sum = 0.0f;
    i   = 0;
    iXa = iXmin;
    while ( (iXa < nXa) && (i < num_i) )
    {
      sum = sum + iHist[i];
      i++;
      iXa = iX[i];
    }
                                /* if we exceeded nXa, correct for partial  */
                                /* bin and set iXa back to GLB of nXa       */
    if ( (iXa > nXa) && (i > 0) )
    {
       sum = sum - iHist[i-1] * (iXa - nXa)/(iXa - iX[i-1]);
       i--;
       iXa = iX[i];
    } 
                               /* if there is an initial output bin for the */
                               /* sum of the initial input bins, save sum.  */
    if ( n > 0 )
      nHist[n-1] = sum;

                               /* now deal with general case.  At this point */
                               /* we know iXa = GLB( nXa )                   */
    while ( (n < num_n) && (nXa < iXmax) )
    {
      nXb = nX[ n+1 ];
      il = i + 1;
      while ( (il < num_i) && (iX[il] < nXb) )
        il++;

      iXb = iX[ il ];
      if ( il == i+1 )                /* [iXa, iXb] is just one subinterval  */
        {
                                   /* add portion of histogram corresponding */
                                   /* overlapping part of the two intervals  */
                                   /* [iXa, iXb] and [nXa, nXb]              */

          if ( iXb < nXb )            /* we're at the end of input intervals */
            sum = iHist[i] * ( iXb - nXa ) / ( iXb - iXa );
          else              
            sum = iHist[i] * ( nXb - nXa ) / ( iXb - iXa );
        }

      else                          /* [iXa, iXb] contains >= 2 subintervals */
        {
                                             /* start with part of first bin */
          iXtemp = iX[ i+1 ];
          sum = iHist[i] * ( iXtemp - nXa ) / ( iXtemp - iXa );

          for ( k = i + 1; k < il - 1; k++ )           /* add up middle bins */
            sum = sum + iHist[k];
          
          if ( iXb < nXb )                /* we're at end of input intervals */
                                          /* so add all of last bin          */
            sum = sum + iHist[il-1];
          else                                       /* add part of last bin */
            {
              iXtemp = iX[il - 1];
              sum = sum + iHist[il-1] * (nXb - iXtemp)/(iXb -iXtemp);
            }
        }

      nHist[n] = sum;                   /* save result in new histogram */

      n++;                           /* advance to next bin in new histogram */
      nXa = nXb;
                                               /* advance in input histogram */
                                               /* keeping iXa = GLB(nXa)     */ 
      i   = il - 1;
      iXa = iX[i];  
    } 
                                  /* fill out rest of new histogram (if any) */ 
                                  /* with zeros.                             */

    for ( k = n; k < num_n; k++ )
      nHist[k] = 0.0f;

    return( true );
  }



  /* --------------------------- ResampleBin ------------------------------ */
  /**
   * Constructs a simple histogram by spreading the counts of one input "bin"
   * from a histogram across a set of equal size bins in another histogram.
   * Also see the method "ReBin". 
   *
   * @param   iXmin     the left  bin boundary of the input bin
   * @param   iXmax     the right bin boundary of the input bin
   * @param   iBin      the counts in in input bin 
   * @param   nXmin     the left  boundary of the new histogram bins
   * @param   nXmax     the right boundary of the new histogram bins
   * @param   nHist[]   array of bin count values in another histogram.  The
   *                    count values MUST have been initialized on input.  This
   *                    function adds counts from the input bin to the values
   *                    initially in the nHist[] bins.
   */

  public static boolean ResampleBin( float  iXmin, 
                                     float  iXmax,
                                     float  iBin,
                                     float  nXmin,
                                     float  nXmax,
                                     float  nHist[]  )
  {
    int      num_n;          
    int      i, 
             n_steps;
    int      n_first, 
             n_last;
    float    counts;
    float    delta_x;
    float    first, 
             last;

                                /* do some basic checks on validity of data */
    num_n = nHist.length;
    if ( (nXmin >= nXmax)         || 
         (iXmin >= iXmax)         ||  
         (num_n <= 0)              )
    {
      System.out.println(" Invalid input data in ResampleBin " + 
                         " in  = " + iXmin + iXmax +
                         " out = " + nXmin + nXmax +
                         " n   = " + num_n );
      return( false );
    }
                                              /* check for degenerate cases */
    if ( (nXmax <= iXmin) || (nXmin >= iXmax) ) 
      return( true );

                                 /* find first grid point in new interval so */
                                 /* first >= start of input interval.        */
    delta_x = (nXmax - nXmin) / num_n;
    n_first = (int) ( (iXmin - nXmin) / delta_x + 1.0f );
    if ( n_first < 0 )
      n_first = 0;
    first = nXmin + n_first * delta_x;

                                  /* find last grid point in new interval so */
                                  /* last <= end of input interval.          */
    n_last = (int) ( (iXmax - nXmin) / delta_x );
    if ( n_last > num_n )
      n_last = num_n;
    last = nXmin + n_last * delta_x;

                                /* check for case where input interval is    */
                                /* entirely contained in one bin of new hist */
    if ( n_first > n_last )
    {
      nHist[n_last] += iBin;  /* all counts go into one bin & we're done */
      return( true );
    }

                                  /* spread bin values across the whole bins */
                                  /* from first to last, if there are any.   */
    if ( n_last > n_first )
    {
      n_steps = n_last - n_first;
      counts  = iBin * delta_x / (iXmax - iXmin);
      for ( i = n_first; i < n_last; i++ )
        nHist[i] += counts;
    }
                                     /* assign part of bin values to first  */
                                     /* partial bin, if there is one.       */
    if ( n_first > 0 )
    {
      if ( iXmin < nXmin )           /* input bin extends beyond new interval */
        counts  = iBin * delta_x / (iXmax - iXmin);
      else
        counts = iBin * (first - iXmin) / (iXmax - iXmin);
      nHist[n_first-1] += counts;
    }
                                       /* assign part of bin values to last */
                                       /* partial bin, if there is one.     */
    if ( n_last < num_n )
    {
      if ( iXmax > nXmax )           /* input bin extends beyond new interval */
        counts  = iBin * delta_x / (iXmax - iXmin);
      else
        counts = iBin * (iXmax - last) / (iXmax - iXmin);
      nHist[n_last] += counts;
    }

    return( true );
  }


/* -------------------------- CLSmoothFunction ------------------------- */
/**
 *  Smooth a function.  This algorithm smooths a tabulated function by 
 *  replacing sets of points with "nearly equal" x values with a new point
 *  obtained by averaging the x and y values.  "x values" are considered
 *  "nearly equal" if the distance between them is less than the total length
 *  of the interval divided by a specified number of steps.
 *  
 *  @param  iX       The original array of x values.  This will be altered and
 *                   the new smoothed x values will be returned in the initial
 *                   part of this array. 
 *  @param  iY       The original array of y values.  This will be altered and
 *                   the new smoothed y values will be returned in the initial
 *                   part of this array. 
 *  @param  err      The original array of error values.  This will be altered
 *                   and the new error values will be returned in the initial
 *                   part of this array. 
 *  @param  n_steps  The approximate number of steps to use for the new 
 *                   smoothed function.
 *
 *  @return The number of entries in iX, iY and err that were used for the 
 *          smoothed function.
 *  
 */
 public static int CLSmooth( float iX[], float iY[], float err[], int n_steps )
 {
   int   i,          // indexes the next point to process in the list
         smoothed_i, // indexes the averaged point to insert in the list 
         n_summed;
   float step,
         x_start,
         x_sum,
         y_sum,
         err_sum;
                                                   // need at least 2 points
   if ( iX.length  <= 1         || 
        iY.length  <  iX.length || 
        err.length <  iX.length || 
        n_steps    <  1 )
     return 0;

   step = ( iX[iX.length-1] - iX[0] ) / n_steps;

   i          = 0;           
   smoothed_i = 0;   
   while ( i < iX.length )  // look for groups of "nearly equal" x values
   {
     x_start  = iX[i];                         // record the current point
     n_summed = 1;
     x_sum    = iX[i];
     y_sum    = iY[i];
     err_sum  = err[i] * err[i];
     i++;                                    // sum all nearly equal points
     while ( i < iX.length && iX[i]-x_start <= step )
     {
       n_summed++;
       x_sum   += iX[i]; 
       y_sum   += iY[i]; 
       err_sum += err[i] * err[i];
       i++;
     } 
                                           // save the average x, y and error
                                           // in the earlier part of the list
     iX [ smoothed_i ] = x_sum / n_summed;
     iY [ smoothed_i ] = y_sum / n_summed;
     err[ smoothed_i ] = (float)Math.sqrt( err_sum ) / n_summed; 
     smoothed_i++;
   }

   return smoothed_i;
 }


/* -------------------------- CLSmoothFunction ------------------------- */
/**
 *  Smooth a function.  This algorithm smooths a tabulated function by
 *  replacing sets of points with "nearly equal" x values with a new point
 *  obtained by averaging the x and y values.  "x values" are considered
 *  "nearly equal" if the distance between them is less than the total length
 *  of the interval divided by a specified number of steps.
 * 
 *  @param  iX       The original array of x values.  This will be altered and
 *                   the new smoothed x values will be returned in the initial
 *                   part of this array.
 *  @param  iY       The original array of y values.  This will be altered and
 *                   the new smoothed y values will be returned in the initial
 *                   part of this array.
 *  @param  n_steps  The approximate number of steps to use for the new
 *                   smoothed function.
 *
 *  @return The number of entries in iX and iY that were used for the
 *          smoothed function.
 *
 */
 public static int CLSmooth( float iX[], float iY[], int n_steps )
 {
   int   i,          // indexes the next point to process in the list
         smoothed_i, // indexes the averaged point to insert in the list
         n_summed;
   float step,
         x_start,
         x_sum,
         y_sum;
                                                   // need at least 2 points
   if ( iX.length  <= 1         ||
        iY.length  <  iX.length ||
        n_steps    <  1 )
     return 0;

   step = ( iX[iX.length-1] - iX[0] ) / n_steps;

   i          = 0;
   smoothed_i = 0;
   while ( i < iX.length )  // look for groups of "nearly equal" x values
   {
     x_start  = iX[i];                         // record the current point
     n_summed = 1;
     x_sum    = iX[i];
     y_sum    = iY[i];
     i++;                                    // sum all nearly equal points
     while ( i < iX.length && iX[i]-x_start <= step )
     {
       n_summed++;
       x_sum   += iX[i];
       y_sum   += iY[i];
       i++;
     }
                                           // save the average x, y 
                                           // in the earlier part of the list
     iX [ smoothed_i ] = x_sum / n_summed;
     iY [ smoothed_i ] = y_sum / n_summed;
     smoothed_i++;
   }

   return smoothed_i;
 }


/* --------------------------- Energy -------------------------------- */
/**
 *   Calculate the energy of a neutron based on the time it takes to travel
 *   a specified distance.
 *
 *   @param path_len_m  The distance traveled in meters.
 *   @param time_us     The time in microseconds for the neutron to travel 
 *                      the distance.
 *
 *   @return The energy of the neutron in meV
 */
public static float Energy( float path_len_m, float time_us )
{
  float   v;
  float   energy;

  if ( time_us <= 0.0f )                     /* NOT MEANINGFUL */
    return( Float.NaN );

  v = (path_len_m * 1000.0f)/ time_us;        /*   velocity in mm/us    */
  energy = meV_per_mm_per_us_2 * v * v; 
  return( energy );  
}


/* ----------------------------- TOFofEnergy ---------------------------- */
/**
 *   Calculate the time it takes a neutron of a specified energy to travel
 *   a specified distance.
 *
 *   @param path_len_m  The distance traveled in meters.
 *   @param e_meV       The energy of the neutron in meV.
 *
 *   @return  The time in microseconds for the neutron to travel the distance.
 */


public static float  TOFofEnergy( float path_len_m, float e_meV )
{
  return (float)( path_len_m * 1000.0/Math.sqrt( e_meV/meV_per_mm_per_us_2 ));
}


/* ------------------------- VelocityFromEnergy ---------------------------- */
/**
 *   Calculate the velocity of a neutron based on it's energy.
 *
 *   @param e_meV       The energy of the neutron in meV.
 *
 *   @return The velocity of a neutron in meters per microsecond. 
 */

public static float VelocityFromEnergy( float e_meV )
{

  if ( e_meV < 0.0f )                        /* NOT MEANINGFUL */
    return( Float.NaN );

  float   v_m_per_us = (float)Math.sqrt( e_meV / meV_per_mm_per_us_2 )/1000;

  return( v_m_per_us );
}


/* ------------------------- EnergyFromVelocity ---------------------------- */
/**
 *   Calculate the energy of a neutron based on it's velocity
 *
 *   @param v_m_per_us  The velocity of a neutron in meters per microsecond. 
 *
 *   @return The energy of the neutron in meV
 */
public static float EnergyFromVelocity( float v_m_per_us )

{
  if ( v_m_per_us < 0.0f )                     /* NOT MEANINGFUL */
    return( Float.NaN );

  float   e_meV = v_m_per_us * v_m_per_us * 1000000 * meV_per_mm_per_us_2;

  return( e_meV );
}


/* ------------------------ EnergyFromWavelength --------------------------- */
/**
 *  Calculate the energy of a neutron based on it's wavelength 
 *
 *   @param wavelength_A  The wavelength of the neutron in Angstroms. 
 *
 *   @return The energy of the neutron in meV
 */
public static float EnergyFromWavelength( float wavelength_A )
{

  if ( wavelength_A <= 0.0f )                     /* NOT MEANINGFUL */
    return( Float.NaN );

  float  v_m_per_us = ANGST_PER_US_PER_M / wavelength_A;
  float  e_meV      = v_m_per_us * v_m_per_us * 1000000 * meV_per_mm_per_us_2;

  return( e_meV );
}



/* ------------------------ WavelengthFromEnergy --------------------------- */
/**
 *  Calculate the wavelength of a neutron based on it's energy
 *
 *   @param e_meV       The energy of the neutron in meV.
 *
 *   @return The wavelength of the neutron in Angstroms. 
 */
public static float WavelengthFromEnergy( float e_meV )
{

  if ( e_meV <= 0.0f )                     /* NOT MEANINGFUL */
    return( Float.NaN );

  float   v_m_per_us = (float)Math.sqrt( e_meV / meV_per_mm_per_us_2 )/1000;
  float   wavelength_A = ANGST_PER_US_PER_M / v_m_per_us;

  return( wavelength_A );
}



/* ------------------------ WavelengthFromVelocity ------------------------- */
/**
 *   Calculate the wavelength of a neutron based on it's velocity.
 *
 *   @param v_m_per_us  The velocity of a neutron in meters per microsecond.
 *   
 *   @return The wavelength of the neutron in Angstroms. 
 */
public static float WavelengthFromVelocity( float v_m_per_us )

{
  if ( v_m_per_us <= 0.0f )                     /* NOT MEANINGFUL */
    return( Float.NaN );

  float  wavelength_A = ANGST_PER_US_PER_M / v_m_per_us;

  return( wavelength_A );
}


/* ------------------------ VelocityFromWavelength ------------------------- */
/**
 *   Calculate the velocity of a neutron based on it's wavelength.
 *   
 *   @param wavelength_A  The wavelength of the neutron in Angstroms. 
 *   
 *   @return The velocity of a neutron in meters per microsecond.
 */

public static float VelocityFromWavelength( float wavelength_A )
{
  if ( wavelength_A <= 0.0f )                     /* NOT MEANINGFUL */
    return( Float.NaN );

  float  v_m_per_us = ANGST_PER_US_PER_M / wavelength_A;

  return( v_m_per_us );
}



/* ----------------------------- Wavelength -------------------------- */
/**
 *   Calculate the wavelength of a neutron based on a distance traveled 
 *   and the time it took to travel that distance.
 *
 *   @param path_len_m  The distance traveled in meters.
 *   @param time_us     The time in microseconds for the neutron to travel 
 *                      the distance.
 *   
 *   @return The wavelength of the neutron in Angstroms.
 */

public static float Wavelength( float path_len_m, float time_us )
{
                                 /* convert time in microseconds to time    */
                                 /* in seconds.  Calculate the wavelength   */
                                 /* in meters and then convert to Angstroms */
  return( ANGST_PER_US_PER_M * time_us / path_len_m );
}


/* ---------------------------- TOFofWavelength ------------------------- */
/**
 *   Calculate the time it takes a neutron of a specified wavelength to travel
 *   a specified distance.
 *
 *   @param path_len_m    The distance traveled in meters.
 *   @param wavelength_A  The wavelength of the neutron in Angstroms. 
 *
 *   @return  The time in microseconds for the neutron to travel the distance.
 */


public static float TOFofWavelength( float path_len_m, float wavelength_A )
{
  return( wavelength_A * path_len_m / ANGST_PER_US_PER_M );
}


/* -------------------------------- DSpacing ----------------------------- */
/**
 *   Calculate a "D" value based on the scattering angle, total flight path 
 *   length and time of flight for a neutron that was scattered by a sample.
 *
 *   @param angle_radians   The angle between the neutron beam and the line 
 *                          from the sample to the detector, in radians.
 *   @param path_len_m      The distance from the moderator to the detector
 *                          in meters.
 *   @param time_us         The time in microseconds for the neutron to travel 
 *                          the distance from the moderator to the detector.
 *   
 *   @return The corresponding "D" value in Angstroms.
 *
 */
public static float  DSpacing( float angle_radians, 
                               float path_len_m, 
                               float time_us )
{
  float wavelength;
  float theta_radians;

  wavelength    = ANGST_PER_US_PER_M * time_us / path_len_m;
  theta_radians = Math.abs( angle_radians / 2.0f );

  return (float)( wavelength / (2.0 * Math.sin( theta_radians ) ) ); 
}


/* ---------------------------- TOFofDSpacing ---------------------------- */
/**
 *   Calculate the time-of-flight for a neutron based on the scattering angle, 
 *   total flight path length and a "D" value for sample that scattered 
 *   the neutron beam.
 *
 *   @param angle_radians   The angle between the neutron beam and the line 
 *                          from the sample to the detector, in radians.
 *   @param path_len_m      The distance from the moderator to the detector
 *                          in meters.
 *   @param d_A             The "D" value in Angstroms.
 *                          the distance.
 *   
 *   @return The time in microseconds for the neutron to travel the distance 
 *           from the moderator to the detector.
 *
 */
public static float  TOFofDSpacing( float angle_radians, 
                                    float path_len_m, 
                                    float d_A          )
{
  float  wavelength;
  float  theta_radians;

  theta_radians = Math.abs( angle_radians / 2.0f );
  wavelength    = (float)(2.0 * Math.sin( theta_radians ) * d_A);

  return( wavelength * path_len_m / ANGST_PER_US_PER_M );
}


/* --------------------------- DiffractometerQ --------------------------- */
/**
 *   Calculate a "Q" value based on the scattering angle, total flight path 
 *   length and time of flight for a neutron that was scattered by a sample.
 *
 *   @param angle_radians   The angle between the neutron beam and the line
 *                          from the sample to the detector, in radians.
 *   @param path_len_m      The distance from the moderator to the detector
 *                          in meters.
 *   @param time_us         The time in microseconds for the neutron to travel
 *                          the distance from the moderator to the detector.
 *   
 *   @return The magnitude of "Q" in inverse Angstroms.
 *
 */

public static float  DiffractometerQ( float angle_radians, 
                                      float path_len_m, 
                                      float time_us    )
{
  float  wavelength;
  float  theta_radians;

  wavelength    = ANGST_PER_US_PER_M * time_us / path_len_m;
  theta_radians = Math.abs( angle_radians / 2.0f );

  return (float)( 4.0 * Math.PI * Math.sin( theta_radians ) / wavelength );
}

/* ------------------------ TOFofDiffractometerQ -------------------------- */
/**
 *   Calculate the time of flight for a neutron based on the scattering angle, i
 *   total flight path length and a "Q" value for a sample that scattered
 *   the neutron beam.
 *
 *   @param angle_radians   The angle between the neutron beam and the line
 *                          from the sample to the detector, in radians.
 *   @param path_len_m      The distance from the moderator to the detector
 *                          in meters.
 *   @param Q_invA          Q in inverse Angstroms.
 *  
 *   @return The time in microseconds for the neutron to travel the distance
 *           from the moderator to the detector.
 */

public static float  TOFofDiffractomerQ( float angle_radians, 
                                         float path_len_m, 
                                         float Q_invA     )

{
  float  wavelength;
  float  theta_radians;

  theta_radians = Math.abs( angle_radians / 2.0f );
  wavelength    = (float)( 4.0 * Math.PI * Math.sin( theta_radians ) / Q_invA );

  return( wavelength * path_len_m / ANGST_PER_US_PER_M );
}

/* --------------------------- SpectrometerQ ---------------------------- */
/**
 *   Calculate a "Q" value for a Spectrometer based on the initial energy,
 *   final energy and scattering angle.
 *
 *   @param e_in_meV        The initial energy of a neutron before being  
 *                          scattered by the sample.
 *   @param e_out_meV       The final energy of a neutron after being  
 *                          scattered by the sample.
 *   @param angle_radians   The angle between the neutron beam and the line
 *                          from the sample to the detector, in radians.
 *
 *   @return The magnitude of "Q" in inverse Angstroms.
 *
 */

public static float SpectrometerQ( float e_in_meV, 
                                   float e_out_meV, 
                                   float angle_radians )
{
  float  temp;
  float  two_theta_radians;

  two_theta_radians = Math.abs( angle_radians );
  temp = (float) (e_in_meV + e_out_meV
     - 2.0 * Math.sqrt( e_in_meV * e_out_meV ) * Math.cos( two_theta_radians ));

  if ( temp < 0.0f )
    {
      System.out.println("ERROR in ChopQ ... sqrt of negative number");
      return( -1.0f );
    }
  return (float)( Math.sqrt( temp / 2.0721 ) );
}

}

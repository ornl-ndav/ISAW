/*
 * @(#)tof_calc.java        1.00 98/08/03  Dennis Mikkelson
 *
 *  Ported to Java from tof_vis_calc.c
 * 
 */

package DataSetTools.math;


public final class tof_calc
{

/* --------------------------------------------------------------------------

   CONSTANTS

*/

static final float  MN_KG               =  1.67495e-27f;  // mass of neutron(kg)
static final float  JOULES_PER_meV      =  1.60206e-22f;
static final float  H_JS                =  6.6262e-34f;    // h in Joule seconds          */

static final float  meV_per_mm_per_us_2 =  5.2276f;       // meV/(mm/us)^2 
static final float  ANGST_PER_US_PER_M  =  3.956058e-3f;
static final float  ANGST_PER_US_PER_MM =  3.956058f;
static final float  RADIANS_PER_DEGREE  =  0.01745332925f;
 
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
   * partially overlap.
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
   * @see ResampleBin
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
   *
   * @see ReBin 
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



/* --------------------------- Energy -------------------------------- */

public static float Energy( float path_len_m, float time_us )
{
  float   v;
  float   energy;

  if ( time_us <= 0.0f )                     /* NOT MEANINGFUL */
    return( Float.MAX_VALUE );

  v = (path_len_m * 1000.0f)/ time_us;        /*   velocity in mm/us    */
  energy = meV_per_mm_per_us_2 * v * v; 
  return( energy );  
}


/* ----------------------------- TOFofEnergy ---------------------------- */

public static float  TOFofEnergy( float path_len_m, float e_meV )
{
  return (float)( path_len_m * 1000.0/Math.sqrt( e_meV/meV_per_mm_per_us_2 ));
}



/* ----------------------------- Wavelength -------------------------- */

public static float Wavelength( float path_len_m, float time_us )
{
                                 /* convert time in microseconds to time    */
                                 /* in seconds.  Calculate the wavelength   */
                                 /* in meters and then convert to Angstroms */
  return( ANGST_PER_US_PER_M * time_us / path_len_m );
}


/* ---------------------------- TOFofWavelength ------------------------- */

public static float TOFofWavelength( float path_len_m, float wavelength_A )
{
  return( wavelength_A * path_len_m / ANGST_PER_US_PER_M );
}


/* -------------------------------- DSpacing ----------------------------- */

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

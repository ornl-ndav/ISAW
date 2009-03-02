package DataSetTools.trial;

import java.io.*;
import java.util.*;
import IPNSSrc.*;

import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Functions.*;
import gov.anl.ipns.MathTools.Geometry.*;

import DataSetTools.instruments.*;
import DataSetTools.operator.Generic.TOF_SCD.*;

public class IndexAllPeaks
{
   /**
    *  Find the UB matrix that indexes one crystallite.
    *
    *  @param peaks           The initial list of peaks to be indexed.  On
    *                         return, this vector will only contain those
    *                         peaks that are NOT indexed by this method.
    *  @param indexed_peaks   The peaks that are indexed will be added to
    *                         this vector.
    *  @param lattice_params  The lattice parameters of a crystalite that
    *                         should be indexed.
    *  @param UBinv           This must be passed in as a 3x3 array and
    *                         it will be filled in with the UBinv matrix
    *                         that indexes the peaks remaing i
    *  @return a two dimensional array containing the calculated h,k,l values
    *          for the peaks listed in the indexed_peaks vector.
    */
   public static double[][] IndexCrystallite( 
                                  Vector<Peak_new> peaks, 
                                  Vector<Peak_new> indexed_peaks,
                                  double[]         lattice_params,
                                  double[][]       UBinv  )
   {
     return null;
   }


   public static double[][] IndexByOptimizing( 
                                  Vector<Peak_new> peaks, 
                                  double[]         lattice_params,
                                  double[][]       UBinv  )
   {
    String[] param_names = { "phi", "chi", "omega" };
    double[] params      = { 40, 30, 60 };


    double                 best_chi_sqr = Double.POSITIVE_INFINITY;
    SCD_OrientationErrorF  best_error_f = null;
    MarquardtArrayFitter   best_fitter  = null;

    SCD_OrientationErrorF  error_f = null;

    Random random = new Random();
    MarquardtArrayFitter fitter = null;
    for ( int count = 0; count < 200; count++ )
    {
      params[0] = 360 * random.nextDouble();
      params[1] =  90 * random.nextDouble();
      params[2] = 360 * random.nextDouble();
      error_f = new SCD_OrientationErrorF( peaks,
                                           lattice_params,
                                           params,
                                           param_names );
      double z_vals[] = new double[ peaks.size() ];
      double sigmas[] = new double[ peaks.size() ];
      double x_index[] = new double[ peaks.size() ];
      for ( int i = 0; i < peaks.size(); i++ )
      {
        z_vals[i] = 0;
        sigmas[i] = 1.0;
        x_index[i]  = i;
      }
                                           // build the data fitter 
      fitter = new MarquardtArrayFitter( error_f,
                                         x_index,
                                         z_vals,
                                         sigmas,
                                         1.0e-10,
                                         200 );

      double chi_sqr = fitter.getChiSqr();

      if ( chi_sqr < best_chi_sqr )
      {
        best_chi_sqr = chi_sqr;
        best_error_f = error_f;
        best_fitter  = fitter;
      }
/*
      if ( count % 100 == 10 )
      {
        System.out.println( "RANDOM DIRECTION NUMBER " + count );
        System.out.println( "ChiSqr = " + chi_sqr );
        System.out.println( "BestChiSqr = " + best_chi_sqr );
      }
*/
    }

//    System.out.println( fitter.getResultsString() );
//    System.out.println("BEST CHI SQ = " + best_chi_sqr );
    for ( int i = 0; i < peaks.size(); i++ )
      best_error_f.show_hkl( i );
      
    double[][] UBinverse = best_error_f.getUBinverse();
    double[][]  indexing = best_error_f.get_hkls( peaks, UBinverse );
/*
    System.out.println("*** Using get_hkls, indexing is: ");
    for ( int row = 0; row < indexing.length; row++ )
    {
      System.out.printf( "%4d", row );
      for ( int col = 0; col < 3; col++ )
        System.out.printf( " %5.2f ", indexing[row][col] );
      System.out.println();
    }
*/
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
        UBinv[row][col] = UBinverse[row][col];

    return indexing;
   }


   /**
    *  Sort the vector of peaks based on their ipkobs value. 
    *
    *  @param  peaks       Vector of peaks to be sorted.
    */
   public static void SortPeaks( Vector<Peak_new> peaks )
   {
      System.out.println("There are " + peaks.size() + " peaks");
      Peak_new[] peak_arr = new Peak_new[ peaks.size() ];
      for ( int i = 0; i < peak_arr.length; i++ )
        peak_arr[i] = peaks.elementAt(i);

      Arrays.sort( peak_arr, new IPkObsComparator() );

      peaks.clear();
      for ( int i = 0; i < peak_arr.length; i++ )
        peaks.add( peak_arr[i] );
      System.out.println("After sorting there are " + peaks.size() + " peaks");
   }


   /**
    *  Sort the vector of peaks based on their distance to the specified
    *  fixed peak.
    *
    *  @param  fixed_peak  List will be sorted based on distance in Q to
    *                      this peak.
    *  @param  peaks       Vector of peaks to be sorted.
    */
   public static void SortPeaks( Peak_new fixed_peak, Vector<Peak_new> peaks )
   {
      System.out.println("There are " + peaks.size() + " peaks");
      Peak_new[] peak_arr = new Peak_new[ peaks.size() ];
      for ( int i = 0; i < peak_arr.length; i++ )
        peak_arr[i] = peaks.elementAt(i);

      Arrays.sort( peak_arr, new DistanceComparator1( fixed_peak ) );

      peaks.clear();
      for ( int i = 0; i < peak_arr.length; i++ )
        peaks.add( peak_arr[i] );
      System.out.println("After sorting there are " + peaks.size() + " peaks");
   }


   /**
    *  Sort the vector of peaks based on the smaller of their distances 
    *  to two specified fixed peaks.
    *
    *  @param  fixed_peak_1  First fixed peak 
    *  @param  fixed_peak_2  Second fixed peak 
    *  @param  peaks         Vector of peaks to be sorted.
    */
   public static void SortPeaks( Peak_new         fixed_peak_1, 
                                 Peak_new         fixed_peak_2,
                                 Vector<Peak_new> peaks )
   {
      System.out.println("There are " + peaks.size() + " peaks");
      Peak_new[] peak_arr = new Peak_new[ peaks.size() ];
      for ( int i = 0; i < peak_arr.length; i++ )
        peak_arr[i] = peaks.elementAt(i);

      Arrays.sort( peak_arr, 
                   new DistanceComparator2( fixed_peak_1, fixed_peak_2 ) );

      peaks.clear();
      for ( int i = 0; i < peak_arr.length; i++ )
        peaks.add( peak_arr[i] );

      System.out.println("After sorting there are " + peaks.size() + " peaks");
   }


   /**
    *  Find the peak with the maximum intensity in the specfied Vector 
    *  of peaks.
    *
    *  @param  peaks  Vector of peaks from which the largest peak will be
    *                 returned. 
    *
    *  @return A reference to the largest peak, or null if the list of peaks
    *          is empty.
    */
   public static Peak_new GetMaxPeak( Vector<Peak_new> peaks )
   {
     if ( peaks == null || peaks.size() == 0 )
       return null;

     Peak_new largest = peaks.elementAt(0);
     for ( int i = 0; i < peaks.size(); i++ )
     {
       if ( largest.ipkobs() < peaks.elementAt(i).ipkobs() )
         largest = peaks.elementAt(i);
     }
     return largest;
   }


   private static class IPkObsComparator implements Comparator
   {
      /**
       *  Compare two Peak_new objects based on their ipkobs value.
       *
       *  @param  peak_1   The first  peak
       *  @param  peak_2   The second peak 
       *
       *  @return A positive integer if peak_1's run number is greater than
       */
       public int compare( Object peak_1, Object peak_2 )
       {
         float ipk_1  = ((Peak_new)peak_1).ipkobs();
         float ipk_2  = ((Peak_new)peak_2).ipkobs();
         if ( ipk_1 < ipk_2 )
           return 1;
         else if  ( ipk_1 > ipk_2 )
           return -1;

          return 0;
       }
   }


   private static class DistanceComparator1 implements Comparator
   {
      float[] fixed_q;

      public DistanceComparator1( Peak_new fixed_peak )
      {
        fixed_q = fixed_peak.getUnrotQ();
      }

      /**
       *  Compare two Peak_new objects based on their distance to the
       *  fixed peak passed in to the constructor.
       *
       *  @param  peak_1   The first  peak
       *  @param  peak_2   The second peak 
       *
       *  @return A positive integer if peak_1's run number is greater than
       */
       public int compare( Object peak_1, Object peak_2 )
       {
         float distance_1 = distance( (Peak_new)peak_1 );
         float distance_2 = distance( (Peak_new)peak_2 );
         if ( distance_1 < distance_2 )
           return -1;
         else if  ( distance_1 < distance_2 )
           return 0;
           
          return 1;
       }

       /**
        *  Calculate the squared distance in Q, from the specified peak 
        *  to the fixed_peak
        */
       private float distance( Peak_new peak )
       {
          float[] q     = peak.getUnrotQ();
          float   delta = 0;
          float   sum   = 0;
          for ( int i = 0; i < 3; i++ )
          {
             delta = (q[i] - fixed_q[i]);
             sum += delta * delta ;
          }
          return sum;
       }
   }


   private static class DistanceComparator2 implements Comparator
   {
      float[] fixed_q_1;
      float[] fixed_q_2;

      public DistanceComparator2( Peak_new fixed_peak_1, 
                                  Peak_new fixed_peak_2 )
      {
        fixed_q_1 = fixed_peak_1.getUnrotQ();
        fixed_q_2 = fixed_peak_2.getUnrotQ();
      }

      /**
       *  Compare two Peak_new objects based on their distance to the
       *  fixed peak passed in to the constructor.
       *
       *  @param  peak_1   The first  peak
       *  @param  peak_2   The second peak 
       *
       *  @return A positive integer if peak_1's run number is greater than
       */
       public int compare( Object peak_1, Object peak_2 )
       {
         float distance_1 = distance( (Peak_new)peak_1 );
         float distance_2 = distance( (Peak_new)peak_2 );
         if ( distance_1 < distance_2 )
           return -1;
         else if  ( distance_1 < distance_2 )
           return 0;

          return 1;
       }

       /**
        *  Calculate the squared distance in Q, from the specified peak 
        *  to the two specified fixed peaks, and return the smaller of
        *  the two squared distances.
        */
       private float distance( Peak_new peak )
       {
          float[] q     = peak.getUnrotQ();
          float   delta = 0;

          float   sum_1 = 0;
          for ( int i = 0; i < 3; i++ )          // find first distance
          {
             delta = (q[i] - fixed_q_1[i]);
             sum_1 += delta * delta ;
          }

          float   sum_2 = 0;
          for ( int i = 0; i < 3; i++ )          // find second distance
          {
             delta = (q[i] - fixed_q_2[i]);
             sum_2 += delta * delta ;
          }

          if ( sum_1 < sum_2 )
            return sum_1;

          return sum_2;
       }
   }


   public static boolean areCollinear( Peak_new peak_1, 
                                       Peak_new peak_2,
                                       float    min_angle )
   {
      float[] q = peak_1.getUnrotQ();
      Vector3D q1 = new Vector3D( q );

      q = peak_2.getUnrotQ();
      Vector3D q2 = new Vector3D( q );

      q1.normalize();
      q2.normalize();

      double threshold = Math.cos( Math.PI * min_angle / 180.0 );

      System.out.println("Threshold = " + threshold );
      System.out.println("dot prod = " + Math.abs( q1.dot(q2) ) );
      return Math.abs( q1.dot(q2) ) > threshold;
   }

   public static void clearIndexes( Vector<Peak_new> peaks )
   {
     for ( int i = 0; i < peaks.size(); i++ )
       peaks.elementAt(i).sethkl( 0, 0, 0 );
   }


   public static void Index( Vector<Peak_new> peaks, double[][] UBinverse )
   {
     double[][] hkls = SCD_OrientationErrorF.get_hkls( peaks, UBinverse );
     for ( int i = 0; i < hkls.length; i++ )
     {
       peaks.elementAt(i).sethkl( (float)hkls[i][0],
                                  (float)hkls[i][1],
                                  (float)hkls[i][2]  );
     }
   }

   public static double DistanceToInts( double[] hkl )
   {
     double dist = 0;
     double diff;
     for ( int i = 0; i < 3; i++ )
     {
       diff = hkl[i] - Math.floor( hkl[i] );
       if ( diff > 0.5 )
         diff = 1.0 - diff;
    
       if ( diff > dist )
         dist = diff;
     } 
     return dist;
   }


  public static int NumIndexed( Vector<Peak_new> peaks,
                                double[][]       UBinverse,
                                double           tolerance )
  {
     double hkl_vals[][] = SCD_OrientationErrorF.get_hkls( peaks, UBinverse );
     int num_indexed = 0;
     for ( int row = 0; row < hkl_vals.length; row++ )
       if ( DistanceToInts( hkl_vals[ row ] ) < tolerance )
         num_indexed++;
     return num_indexed;
  }


  /**
   * Find new list of hkls, and new UBinverse using the BestFitMatrix
   */
   public static double[][] OptimizeUB( Vector<Peak_new> peaks, 
                                        double[][]       UBinverse,
                                        double[][]       newUBinverse,
                                        double           tolerance )
   {
     double UB[][] = new double[3][3];
     double hkl_vals[][] = SCD_OrientationErrorF.get_hkls( peaks, UBinverse );
     Vector<double[]> good_hkl = new Vector<double[]>();
     Vector<Peak_new> good_peaks = new Vector<Peak_new>();
     int num_bad = 0;
     for ( int row = 0; row < hkl_vals.length; row++ )
     {
       if ( DistanceToInts( hkl_vals[ row ] ) < tolerance )
       {
         for ( int col = 0; col < 3; col++ )
           hkl_vals[row][col] = Math.round( hkl_vals[row][col]);
         good_hkl.add( hkl_vals[row] );
         good_peaks.add( peaks.elementAt( row ) );
       }
       else
         num_bad++;
     }

     double q_vals[][] = new double[good_peaks.size()][3];
     float[] temp;
     for ( int index = 0; index < good_peaks.size(); index++ )
     {
       temp = good_peaks.elementAt(index).getUnrotQ();
       for ( int i = 0; i < 3; i++ )
         q_vals[index][i] = temp[i] * 2 * Math.PI;
     }

     hkl_vals = new double[good_peaks.size()][];
     for ( int index = 0; index < good_peaks.size(); index++ )
       hkl_vals[index] = good_hkl.elementAt(index);

     System.out.println( "NUM INDEXED = " +  hkl_vals.length +
                         "  NUM NOT = " + num_bad +
                         " WITH TOLERANCE = " + tolerance );
/*
     System.out.println("   H       K       L          qx      qy      qz");
     for ( int row = 0; row < good_peaks.size(); row++ )
     {
       System.out.printf("%6.2f  %6.2f  %6.2f        ",
                          hkl_vals[row][0],
                          hkl_vals[row][1],
                          hkl_vals[row][2] );
       System.out.printf("%6.2f  %6.2f  %6.2f\n",
                          q_vals[row][0],
                          q_vals[row][1],
                          q_vals[row][2] );
     }
*/
     double residual = LinearAlgebra.BestFitMatrix( UB, hkl_vals, q_vals );
     System.out.println("RESIDUAL = " + residual );

     if ( Double.isNaN(residual) )
       return null;

     for ( int row = 0; row < 3; row++ )
       for ( int col = 0; col < 3; col++ )
         newUBinverse[row][col] = UB[row][col];

     LinearAlgebra.invert( newUBinverse );

     return SCD_OrientationErrorF.get_hkls( peaks, newUBinverse );
   }

   public static void ShowLatticeParams( double[][] UBinverse )
   {
     double[][] temp = LinearAlgebra.copy( UBinverse );
     LinearAlgebra.invert( temp );
     double[] lat_params = lattice_calc.LatticeParamsOfUB( temp );
     for ( int k = 0; k < 3; k++ )
       lat_params[k] *= Math.PI * 2;
     System.out.printf("%7.4f  %7.4f  %7.4f   %8.4f %8.4f %8.4f  %7.2f \n",
                       lat_params[0], lat_params[1], lat_params[2],
                       lat_params[3], lat_params[4], lat_params[5],
                       lat_params[6] );
   }
 
   public static void ShowHKLS( Vector<Peak_new> peaks, 
                                double[][]       UBinverse,
                                double           tolerance )
   {
     double[][] hkls = SCD_OrientationErrorF.get_hkls( peaks, UBinverse );
     for( int i = 0; i < hkls.length; i++ )
       if ( DistanceToInts( hkls[ i ] ) < tolerance )
         System.out.printf("%4d  %6.3f  %6.3f  %6.3f \n", 
                            i, hkls[i][0], hkls[i][1], hkls[i][2] );
       else
         System.out.printf("%4d  %6.3f  %6.3f  %6.3f ******* \n", 
                            i, hkls[i][0], hkls[i][1], hkls[i][2] );
   }

   public static void WriteNotIndexedPeaks( Vector<Peak_new> peaks,
                                            double[][]       UBinverse,
                                            double           tolerance,
                                            String           filename )
                      throws IOException
   {
     Vector<Peak_new> not_indexed = new Vector<Peak_new>();

     double[][] hkls = SCD_OrientationErrorF.get_hkls( peaks, UBinverse );
     for( int i = 0; i < hkls.length; i++ )
       if ( DistanceToInts( hkls[ i ] ) >= tolerance )
         not_indexed.add( peaks.elementAt(i) );       

     Peak_new_IO.WritePeaks_new( filename, not_indexed, false );
   }

   public static void main( String args[] ) throws IOException
   {
      // Lattice Parameters 
      double lattice_params[] = new double[6];

     if ( args[0].equalsIgnoreCase( "oxalic" ) )
     {
       lattice_params[0] = 6.094;
       lattice_params[1] = 3.601;
       lattice_params[2] = 11.915;
       lattice_params[3] = 90;
       lattice_params[4] = 103.2;
       lattice_params[5] = 90;
     }
     else if ( args[0].equalsIgnoreCase( "quartz" ) ) 
     {
       lattice_params[0] = 4.9138;
       lattice_params[1] = 4.9138;
       lattice_params[2] = 5.4051;
       lattice_params[3] = 90;
       lattice_params[4] = 90;
       lattice_params[5] = 120;
     }
     else if ( args[0].equalsIgnoreCase( "bad_quartz" ) )
     {
       lattice_params[0] = 4.8;
       lattice_params[1] = 4.8;
       lattice_params[2] = 5.3;
       lattice_params[3] = 90;
       lattice_params[4] = 90;
       lattice_params[5] = 120;
     }
     else if ( args[0].equalsIgnoreCase( "BaFeAs" ) )
     {
       lattice_params[0] = 4;
       lattice_params[1] = 4;
       lattice_params[2] = 12.9;
       lattice_params[3] = 90;
       lattice_params[4] = 90;
       lattice_params[5] = 90;
     }
     else if ( args[0].equalsIgnoreCase( "FAKE" ) )
     {
       lattice_params[0] = 1;
       lattice_params[1] = 1;
       lattice_params[2] = 1;
       lattice_params[3] = 90;
       lattice_params[4] = 90;
       lattice_params[5] = 90;
     }
     else
     {
       System.out.println("NO LATTICE PARAMETERS, CURRENTLY MUST BE ONE OF:");
       System.out.println("oxalic");
       System.out.println("quartz");
       System.out.println("BaFeAs");
       System.exit(1);
     }
     

    String filename = "/home/dennis/NotIndexed.peaks";
 
    Vector<Peak_new> all_peaks = Peak_new_IO.ReadPeaks_new( filename );
    clearIndexes( all_peaks );
                                                 // randomize just to be sure
                                                 // this is not working due to
                                                 // sequence of peaks.
    Vector<Peak_new> temp = new Vector<Peak_new>();
    Random random = new Random();
    int N_PEAKS = all_peaks.size();
    for ( int i = 0; i < N_PEAKS; i++ )
      temp.add( 
          all_peaks.elementAt( (int)(all_peaks.size() * random.nextDouble())));
    
    all_peaks.clear();
    for ( int i = 0; i < N_PEAKS; i++ )
      all_peaks.add( temp.elementAt(i) ); 

    Vector<Peak_new> peaks = new Vector<Peak_new>();
                 
                                                 // now sort by ipkobs
    SortPeaks( all_peaks );
    Peak_new largest = all_peaks.elementAt(0);
    System.out.println("Largest Peak is " + largest );

                                                  // just look at strong peaks
    Vector<Peak_new> strong_peaks = new Vector<Peak_new>();
    int num_strong = all_peaks.size() / 100;
    if ( num_strong < 50 )
      num_strong = all_peaks.size()/50;
    if ( num_strong < 50 )
      num_strong = all_peaks.size()/20;
    if ( num_strong < 50 )
      num_strong = all_peaks.size()/10;
    if ( num_strong < 50 )
      num_strong = all_peaks.size()/5;
    if ( num_strong < 50 )
      num_strong = all_peaks.size()/2;
    if ( num_strong < 50 )
      num_strong = all_peaks.size();

    for ( int i = 0; i < num_strong; i++ )
      strong_peaks.add( all_peaks.elementAt(i) );

    double[][] UBinverse = new double[3][3];
    double[][] newUBinverse = new double[3][3];
    double[][] hkls;

    double REQUIRED_FRACTION = 0.4;  // NOTE: This is critical.  IF too low
                                      //       (say .3) quartz many fails
    double hkl_tol       = 0.12;
    int    MAX_ATTEMPTS  = 25;
    int    num_attempts  = 0;
    int    num_indexed   = 0;
    int    NUM_TO_PROCESS = 2;
    int    NUM_NEIGHBORS  = 20;
    int    first_peak     = 0;
    int    second_peak    = 0;
    int    MIN_TO_INDEX   = 10;

    if ( NUM_NEIGHBORS > strong_peaks.size() )
      NUM_NEIGHBORS = strong_peaks.size();

                                       // Initial indexing -------------------
    while ( num_indexed < REQUIRED_FRACTION * NUM_NEIGHBORS && 
//    while ( num_indexed < MIN_TO_INDEX && 
            num_attempts < MAX_ATTEMPTS ) 
    {
      System.out.println(" ===================== AUTO INDEXING, ATTEMPT # " 
                         + (num_attempts + 1) );
      peaks.clear();
      int next_peak = 0;
      int num_kept = 0;

      SortPeaks( strong_peaks );
      peaks.add( strong_peaks.elementAt(0) );
//      first_peak = (int)( (strong_peaks.size()-1) * random.nextDouble());
//      peaks.add( strong_peaks.elementAt( first_peak) );

      second_peak = 1+(int)( (strong_peaks.size()-2) * random.nextDouble());
      peaks.add( strong_peaks.elementAt( second_peak ) );

      SortPeaks( peaks.elementAt(0), peaks.elementAt(1), strong_peaks );
      Vector<Peak_new> neighbors = new Vector<Peak_new>();
      for ( int i = 2; i < Math.min(NUM_NEIGHBORS, strong_peaks.size()); i++ )
        neighbors.add( strong_peaks.elementAt(i) );
/*
      System.out.println("Largest = " + largest );
      System.out.println("First   = " + peaks.elementAt(0) );
      System.out.println("Second  = " + peaks.elementAt(1) );
      for ( int i = 0; i < Math.min( 8, strong_peaks.size()); i++ )
        System.out.println("PEAK " + i + " = " + strong_peaks.elementAt(i) );
*/
      int retries = 0;
      while ( num_indexed < REQUIRED_FRACTION * NUM_NEIGHBORS &&
//      while ( num_indexed < MIN_TO_INDEX &&
              num_attempts < MAX_ATTEMPTS                     &&
              retries < 3 )
      { 
        double[][] indexing = IndexByOptimizing(peaks,lattice_params,UBinverse);
        System.out.println("After Optimize");
        ShowLatticeParams( UBinverse );

        num_indexed = NumIndexed( neighbors, UBinverse, hkl_tol );
        System.out.println("---------- NUM INDEXED = " + num_indexed +
                           " OUT OF " + neighbors.size() +
                           " WITH TOLERANCE = " + hkl_tol );
/*
        double[][] UBinverse_save = LinearAlgebra.copy( UBinverse );
        if ( num_indexed > 5 )
        {
          try
          {
            hkls = OptimizeUB( neighbors, UBinverse, newUBinverse, hkl_tol );
            if ( hkls == null )
            {
              UBinverse = LinearAlgebra.copy( UBinverse_save );
              System.out.println("****OptUB************* failed" );
            }
            else
            {
              UBinverse = LinearAlgebra.copy( newUBinverse );
              num_indexed = NumIndexed( neighbors, UBinverse, hkl_tol );
              System.out.println("****OptUB, NUM INDEXED = " + num_indexed );
            }
          }
          catch ( Exception ex )
          {
            System.out.println("******* OptUB ======= Singular System");
            UBinverse = LinearAlgebra.copy( UBinverse_save );
          }
        }
*/
        num_attempts++;
        retries++;
      }
    }
 
                                         // Refine UB -----------------------
    if ( num_attempts < MAX_ATTEMPTS )
    {
     peaks.clear();
     int next_peak = 0;
     int num_to_add = NUM_NEIGHBORS;
     while ( next_peak < strong_peaks.size() )
     { 
       int count = 0;
       while ( next_peak < strong_peaks.size() && count < num_to_add )
       {
         peaks.add( strong_peaks.elementAt(next_peak) );
         next_peak++;
         count++;
       }
       num_to_add = (int)( .2 * peaks.size() );
//       num_to_add = num_to_add + 20;

       System.out.println("NUM INDEXED = " +
                           NumIndexed( peaks, UBinverse, hkl_tol ) +
                          " OUT OF " + peaks.size() + 
                          " WITH TOLERANCE = " + hkl_tol );

       hkls = OptimizeUB( peaks, UBinverse, newUBinverse, hkl_tol );
       if ( hkls == null )
       {
          System.out.println("FAILED**********************");
          num_attempts = MAX_ATTEMPTS + 1;
          next_peak = strong_peaks.size() + 1;
       }
       else
       {
         System.out.println("newUBinverse = " );
         LinearAlgebra.print( newUBinverse );
         UBinverse = LinearAlgebra.copy( newUBinverse );
         ShowLatticeParams( newUBinverse );
//       ShowHKLS( peaks, newUBinverse, hkl_tol );
       }
     }

     if ( num_attempts <= MAX_ATTEMPTS )
     {
                                        // Iterate on all peaks -------------
       for ( int i = 0; i < 5; i++ )
       {
         UBinverse = LinearAlgebra.copy( newUBinverse );
         hkls = OptimizeUB( all_peaks, UBinverse, newUBinverse, hkl_tol );
         ShowLatticeParams( newUBinverse );
//       ShowHKLS( all_peaks, newUBinverse, hkl_tol );

         System.out.println("NUM INDEXED = " +
                             NumIndexed( all_peaks, UBinverse, hkl_tol ) +
                            " OUT OF " + all_peaks.size() +
                            " WITH TOLERANCE = " + hkl_tol );
        }
        String outfile = "/home/dennis/NotIndexed.peaks";
        WriteNotIndexedPeaks( all_peaks, newUBinverse, hkl_tol, outfile );

                            
                                         // standardize the unit cell
        double[][] UB = LinearAlgebra.copy( newUBinverse );
        LinearAlgebra.invert( UB );
        float[][]  floatUB = new float[3][3];
        for ( int row = 0; row < 3; row++ )
          for ( int col = 0; col < 3; col++ )
            floatUB[row][col] = (float)(UB[row][col] / (Math.PI * 2));
                                          // NOTE: Transpose and factor of 2PI
        blind my_blind = new blind();
        my_blind.blaue( floatUB );
        for ( int row = 0; row < 3; row++ )
          for ( int col = 0; col < 3; col++ )
            floatUB[row][col] = (float)(my_blind.UB[row][col]);
        Util.WriteMatrix( "FoundPeaks.mat", floatUB );
      }
    }
    else
      System.out.println("FAILED TO INDEX PEAKS");
 

   }
}

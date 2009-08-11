/* 
 * File: IndexPeaks_Calc.java
 *
 * Copyright (C) 2009, Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package Operators.TOF_SCD;

import java.io.*;
import java.util.*;
import IPNSSrc.*;

import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Functions.*;
import gov.anl.ipns.MathTools.Geometry.*;

import DataSetTools.trial.*;
import DataSetTools.operator.Generic.TOF_SCD.*;

public class IndexPeaks_Calc
{
   /**
    *  Find the UB matrix that indexes one crystallite.
    *
    *  @param peaks           The initial list of peaks to be indexed.
    *  @param lattice_params  The lattice parameters of a crystalite that
    *                         should be indexed.
    *  @param UBinv           This must be passed in as a 3x3 array and
    *                         it will be filled in with the UBinv matrix
    *                         that indexes the peaks remaing i
    *  @return a two dimensional array containing the calculated h,k,l values
    *          for the peaks 
    */
   public static double[][] IndexByOptimizing( Vector<IPeakQ> peaks, 
                                               double[]       lattice_params,
                                               double[][]     UBinv  )
   {
    String[] param_names = { "phi", "chi", "omega" };
    double[] params      = { 40, 30, 60 };


    double                 best_chi_sqr = Double.POSITIVE_INFINITY;
    SCD_OrientationErrorF  best_error_f = null;
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
   public static void SortPeaks( Vector<IPeakQ> peaks )
   {
      IPeakQ[] peak_arr = new IPeakQ[ peaks.size() ];
      for ( int i = 0; i < peak_arr.length; i++ )
        peak_arr[i] = peaks.elementAt(i);

      Arrays.sort( peak_arr, new IPkObsComparator() );

      peaks.clear();
      for ( int i = 0; i < peak_arr.length; i++ )
        peaks.add( peak_arr[i] );
   }


   /**
    *  Sort the vector of peaks based on the magnitude of their Q value. 
    *
    *  @param  peaks       Vector of peaks to be sorted.
    *  @param  decreasing  Set true to sort from largest to smallest;
    *                      set false to sort from smallest to largest.
    */
   public static void SortPeaksMagQ( Vector<IPeakQ> peaks, 
                                     boolean          decreasing )
   {
      IPeakQ[] peak_arr = new IPeakQ[ peaks.size() ];
      for ( int i = 0; i < peak_arr.length; i++ )
        peak_arr[i] = peaks.elementAt(i);

      Arrays.sort( peak_arr, new MagnitudeQComparator(decreasing) );

      peaks.clear();
      for ( int i = 0; i < peak_arr.length; i++ )
        peaks.add( peak_arr[i] );
   }


   /**
    *  Sort the vector of peaks based on their distance to the specified
    *  fixed peak.
    *
    *  @param  fixed_peak  List will be sorted based on distance in Q to
    *                      this peak.
    *  @param  peaks       Vector of peaks to be sorted.
    */
   public static void SortPeaks( IPeakQ fixed_peak, Vector<IPeakQ> peaks )
   {
      IPeakQ[] peak_arr = new IPeakQ[ peaks.size() ];
      for ( int i = 0; i < peak_arr.length; i++ )
        peak_arr[i] = peaks.elementAt(i);

      Arrays.sort( peak_arr, new DistanceComparator1( fixed_peak ) );

      peaks.clear();
      for ( int i = 0; i < peak_arr.length; i++ )
        peaks.add( peak_arr[i] );
   }


   /**
    *  Sort the vector of peaks based on the smaller of their distances 
    *  to two specified fixed peaks.
    *
    *  @param  fixed_peak_1  First fixed peak 
    *  @param  fixed_peak_2  Second fixed peak 
    *  @param  peaks         Vector of peaks to be sorted.
    */
   public static void SortPeaks( IPeakQ         fixed_peak_1, 
                                 IPeakQ         fixed_peak_2,
                                 Vector<IPeakQ> peaks )
   {
      IPeakQ[] peak_arr = new IPeakQ[ peaks.size() ];
      for ( int i = 0; i < peak_arr.length; i++ )
        peak_arr[i] = peaks.elementAt(i);

      Arrays.sort( peak_arr, 
                   new DistanceComparator2( fixed_peak_1, fixed_peak_2 ) );

      peaks.clear();
      for ( int i = 0; i < peak_arr.length; i++ )
        peaks.add( peak_arr[i] );
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
   public static IPeakQ GetMaxPeak( Vector<IPeakQ> peaks )
   {
     if ( peaks == null || peaks.size() == 0 )
       return null;

     IPeakQ largest = peaks.elementAt(0);
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
       *  Compare two IPeakQ objects based on their ipkobs value.
       *
       *  @param  peak_1   The first  peak
       *  @param  peak_2   The second peak 
       *
       *  @return A positive integer if peak_1's run number is greater than
       */
       public int compare( Object peak_1, Object peak_2 )
       {
         float ipk_1  = ((IPeakQ)peak_1).ipkobs();
         float ipk_2  = ((IPeakQ)peak_2).ipkobs();
         if ( ipk_1 < ipk_2 )
           return 1;
         else if  ( ipk_1 > ipk_2 )
           return -1;

          return 0;
       }
   }


   private static class MagnitudeQComparator implements Comparator
   {
     boolean decreasing;

     /**
      *  Construct a comparator to sort a list of peaks in increasing or
      *  decreasing order based on |Q|.
      *
      *  @param  decreasing  Set true to sort from largest to smallest;
      *                      set false to sort from smallest to largest.
      */
     public MagnitudeQComparator( boolean decreasing )
     {
       this.decreasing = decreasing;
     }


     /**
       *  Compare two IPeakQ objects based on the magnitude of their Q value.
       *
       *  @param  peak_1   The first  peak
       *  @param  peak_2   The second peak 
       *
       *  @return A positive integer if peak_1's run number is greater than
       */
       public int compare( Object peak_1, Object peak_2 )
       {
         float[] q1  = ((IPeakQ)peak_1).getUnrotQ();
         float[] q2  = ((IPeakQ)peak_2).getUnrotQ();

         float mag_q1 = q1[0]*q1[0] + q1[1]*q1[1] + q1[2]*q1[2];
         float mag_q2 = q2[0]*q2[0] + q2[1]*q2[1] + q2[2]*q2[2];

         if ( decreasing )
         {
           if ( mag_q1 < mag_q2 )
             return 1;
           else if  ( mag_q1 > mag_q2 )
             return -1;
         }
         else
         {
           if ( mag_q1 < mag_q2 )
             return -1;
           else if  ( mag_q1 > mag_q2 )
             return 1;
         }

         return 0;
       }
   }


   private static class DistanceComparator1 implements Comparator
   {
      float[] fixed_q;

      public DistanceComparator1( IPeakQ fixed_peak )
      {
        fixed_q = fixed_peak.getUnrotQ();
      }

      /**
       *  Compare two IPeakQ objects based on their distance to the
       *  fixed peak passed in to the constructor.
       *
       *  @param  peak_1   The first  peak
       *  @param  peak_2   The second peak 
       *
       *  @return A positive integer if peak_1's run number is greater than
       */
       public int compare( Object peak_1, Object peak_2 )
       {
         float distance_1 = distance( (IPeakQ)peak_1 );
         float distance_2 = distance( (IPeakQ)peak_2 );
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
       private float distance( IPeakQ peak )
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

      public DistanceComparator2( IPeakQ fixed_peak_1, 
                                  IPeakQ fixed_peak_2 )
      {
        fixed_q_1 = fixed_peak_1.getUnrotQ();
        fixed_q_2 = fixed_peak_2.getUnrotQ();
      }

      /**
       *  Compare two IPeakQ objects based on their distance to the
       *  fixed peak passed in to the constructor.
       *
       *  @param  peak_1   The first  peak
       *  @param  peak_2   The second peak 
       *
       *  @return A positive integer if peak_1's run number is greater than
       */
       public int compare( Object peak_1, Object peak_2 )
       {
         float distance_1 = distance( (IPeakQ)peak_1 );
         float distance_2 = distance( (IPeakQ)peak_2 );
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
       private float distance( IPeakQ peak )
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


   /**
    *  Check wheter or not the specified peaks lie along a line through
    *  the origin to within a specified angular tolerance.
    *
    *  @param peak_1     The first peak to check 
    *  @param peak_2     The peak to compare with the first peak
    *  @param min_angle  If the vectors from the origin to the two peaks
    *                    are collinear to within this minimm angle, they
    *                    will be considered to be collinear.
    *  @return true if the two peaks are collinear to within the specified
    *          angle.
    */
   public static boolean areCollinear( IPeakQ peak_1, 
                                       IPeakQ peak_2,
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


   /**
    *  Set all of the peaks h,k,l to be 0,0,0
    *
    *  @param peaks  The list of peaks to set to (h,k,l) = (0,0,0)
    */
   public static void clearIndexes( Vector<IPeakQ> peaks )
   {
     for ( int i = 0; i < peaks.size(); i++ )
       peaks.elementAt(i).sethkl( 0, 0, 0 );
   }


   /**
    * Index the specified list of peaks using the specified UBinverse
    * matrix.
    *
    * @param peaks     The list of peaks to index
    * @param UBinverse The inverse of the UB matrix
    * @param tolerance The HKL value assigned to the peak must be
    *                  closer to integer values than the specified
    *                  tolerance, in order to consider the peak to be
    *                  indexed.
    */
   public static void Index( Vector<IPeakQ> peaks, 
                             double[][]     UBinverse,
                             double         tolerance )
   {
     double[][] hkls = SCD_OrientationErrorF.get_hkls( peaks, UBinverse );
     for ( int i = 0; i < hkls.length; i++ )
     {
       if ( DistanceToInts( hkls[i] ) < tolerance )
         peaks.elementAt(i).sethkl( (float)hkls[i][0],
                                    (float)hkls[i][1],
                                    (float)hkls[i][2]  );
       else
         peaks.elementAt(i).sethkl( 0, 0, 0 );
     }
   }

   
   /**
    * Find the largest distance from the specified h,k,l values to the
    * nearest integers.
    *
    * @param hkl  Array of three floats representing an approximate h,k,l
    *             indexing
    * @return the largest of the distances (h-Ih), (k-Ik) and (l-Il) where
    *         Ih, Ik and Il are the integers nearest to h, k and l 
    *         respectively.
    */
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


  /**
   *  Calculate how many of the specified peaks are indexed to within
   *  the specified tolerance.
   *
   *  @param peaks     The list of peaks to check
   *  @param UBinverse The inverse of an orientation matrix UB
   *  @param tolerance The required tolerance for distance from the
   *                   approximate index values to integer values.
   *  @return The number of peaks indexed to within the specified tolerance.
   */
  public static int NumIndexed( Vector<IPeakQ> peaks,
                                double[][]     UBinverse,
                                double         tolerance )
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
   * linear least squares method.
   *
   * @param peaks         List of peaks
   * @param UBinverse     Inverse of UB matrix that determines the
   *                      initial indexing of the peaks.  Peaks that are
   *                      indexed to within the specified tolerance are
   *                      considered to be indexed, and are used to obtain
   *                      the new UB inverse.
   * @param newUBinverse  A 3x3 array that will be filled out with the
   *                      new UB inverse
   * @param tolerance     Tolerance on the initial indexing.
   *
   * @return A two dimensional array listing the h,k,l values for the
   *         new indexing as rows.  The newUBinverse matrix is also filled
   *         out the newly calculated inverse.
   */
   public static double[][] OptimizeUB( Vector<IPeakQ> peaks, 
                                        double[][]     UBinverse,
                                        double[][]     newUBinverse,
                                        double         tolerance )
   {
     double UB[][] = new double[3][3];
     double hkl_vals[][] = SCD_OrientationErrorF.get_hkls( peaks, UBinverse );
     Vector<double[]> good_hkl   = new Vector<double[]>();
     Vector<IPeakQ>   good_peaks = new Vector<IPeakQ>();
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


   /**
    *  Print out the lattice parameters of the specified UB inverse matrix.
    *
    *  @param UBinverse the inverse of a UB matrix.
    */
   public static void ShowLatticeParams( double[][] UBinverse )
   {
     System.out.println( getLatticeParams( UBinverse ) );
   }
 

  /**
   *  Get a String form of the lattice parameters corresponding to the
   *  specified UB inverse matrix.
   *
   *  @param UBinverse the inverse of a UB matrix.
   */
   private static String getLatticeParams( double[][] UBinverse )
   {
     double[][] temp = LinearAlgebra.copy( UBinverse );
     LinearAlgebra.invert( temp );
     double[] lat_params = lattice_calc.LatticeParamsOfUB( temp );
     for ( int k = 0; k < 3; k++ )
       lat_params[k] *= Math.PI * 2;
     lat_params[6] *= 8 * Math.PI * Math.PI * Math.PI;
     return String.format(" %3.2f %3.2f %3.2f   %4.1f %4.1f %4.1f  %5.1f \n",
                       lat_params[0], lat_params[1], lat_params[2],
                       lat_params[3], lat_params[4], lat_params[5],
                       lat_params[6] );
   }


   /**
    *  Print out the h,k,l indices of a list of peaks.
    *
    *  @param peaks     The list of peaks
    *  @param UBinverse Inverse of a UB matrix that determines the indexing
    *                   of the peaks.
    *  @param tolerance Determines which peaks are considered well indexed;
    *                   other peaks are marked with ******
    */
   public static void ShowHKLS( Vector<IPeakQ> peaks, 
                                double[][]     UBinverse,
                                double         tolerance )
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


   /**
    *  Write peaks that are not well indexed with a specified UB inverse
    *  matrix.
    *  @param peaks     List of peaks
    *  @param UBinverse Inverse UB matrix that determines the indexing
    *  @param tolerance Peaks that are not indexed to within the specified
    *                   tolerance will be written
    *  @param filename  The name of the file to be written.
    */
   public static void WriteNotIndexedPeaks( Vector<IPeakQ> peaks,
                                            double[][]     UBinverse,
                                            double         tolerance,
                                            String         filename )
                      throws IOException
   {
     if ( filename == null || filename.length() <= 0 )
       return;

     Vector not_indexed = new Vector();

     double[][] hkls = SCD_OrientationErrorF.get_hkls( peaks, UBinverse );
     for( int i = 0; i < hkls.length; i++ )
       if ( DistanceToInts( hkls[ i ] ) >= tolerance )
         not_indexed.add( peaks.elementAt(i) );       

     if ( not_indexed.size() > 0 &&
          not_indexed.elementAt(0) instanceof Peak_new )
       Peak_new_IO.WritePeaks_new( filename, not_indexed, false );
   }


   /**
    * Attempt to index the peaks in the specified file, given the
    * lattice constants.
    * This method proceeds in three stages.  First the strongest
    * peaks in the file are used with an optimization based method
    * to find an initial indexing for a significant fraction of the
    * strongest peaks, using the specified lattice constants.  
    * Next, the algorithm attempts to index all of the strongest peaks
    * by adjusting the lattice parameters.
    * Finally, the algorithm attempts to extend this indexing to all
    * of the peaks.  The peaks that are not indexed are written to
    * a specified file, so that they can be (possibly) indexed latter.
    * This allows indexing twins or more complicated samples with several
    * crystalites.
    *
    * @param  peaks_file_name       The name of the file containing the
    *                               original list of peaks.
    * @param  matrix_file_name      The name of the matrix file to write.
    * @param  not_indexed_file_name The name of the file to write with peaks
    *                               that could not be indexed.
    * @param  a                     Lattice parameter 'a'
    * @param  b                     Lattice parameter 'b'
    * @param  c                     Lattice parameter 'c'
    * @param  alpha                 Lattice parameter alpha 
    * @param  beta                  Lattice parameter beta 
    * @param  gamma                 Lattice parameter gamma
    */
   public static String IndexPeaksWithOptimizer( String peaks_file_name,
                                                 String matrix_file_name,
                                                 String not_indexed_file_name,
                                                 float a,
                                                 float b,
                                                 float c,
                                                 float alpha,
                                                 float beta,
                                                 float gamma ) 
                                                 throws IOException
   {
     int MAX_STRONG     = 40;
     int MAX_ATTEMPTS   = 25;
     int NUM_NEIGHBORS  = 20;

     double lattice_params[] = new double[6];
     lattice_params[0] = a;
     lattice_params[1] = b;
     lattice_params[2] = c;
     lattice_params[3] = alpha;
     lattice_params[4] = beta;
     lattice_params[5] = gamma;

     if ( peaks_file_name == null || peaks_file_name.length() <= 0 )
       throw new IllegalArgumentException("Invalid peaks file name " +
                                           peaks_file_name );

     Vector<Peak_new> peaks_new = Peak_new_IO.ReadPeaks_new( peaks_file_name );

     Vector<IPeakQ> all_peaks = new Vector<IPeakQ>();
     for ( int i = 0; i < peaks_new.size(); i++ )
       all_peaks.add( peaks_new.elementAt(i) );

     clearIndexes( all_peaks );
                                                // now sort by ipkobs
     SortPeaks( all_peaks );

     Vector<IPeakQ> strong_peaks = new Vector<IPeakQ>();
     int num_strong = all_peaks.size();
                                          // just look at 40 strongest peaks
     if (num_strong > MAX_STRONG )
       num_strong = MAX_STRONG;

     for ( int i = 0; i < num_strong; i++ )
       strong_peaks.add( all_peaks.elementAt(i) );
                                         // sort in increasing order of |Q|
     SortPeaksMagQ( strong_peaks, false );

     double[][] UBinverse = new double[3][3];
     double[][] newUBinverse = new double[3][3];
     double[][] hkls;

     double REQUIRED_FRACTION = 0.4;  // NOTE: This is critical.  IF too low
                                      //       (say .3) quartz many fails
     double hkl_tol        = 0.12;
     int    num_attempts   = 0;
     int    num_indexed    = 0;
     int    second_peak    = 0;

     if ( NUM_NEIGHBORS > strong_peaks.size() )
       NUM_NEIGHBORS = strong_peaks.size();
 
                                   // Initial indexing -------------------
    Vector<IPeakQ> peaks = new Vector<IPeakQ>();
    Random random = new Random();
    while ( num_indexed  < REQUIRED_FRACTION * NUM_NEIGHBORS &&
            num_attempts < MAX_ATTEMPTS )
    {
      System.out.println(" ===================== AUTO INDEXING, ATTEMPT # "
                         + (num_attempts + 1) );
      peaks.clear();

      SortPeaksMagQ( strong_peaks, false );
      peaks.add( strong_peaks.elementAt(0) );
      second_peak = 1+(int)( (strong_peaks.size()/2) * random.nextDouble());
      peaks.add( strong_peaks.elementAt( second_peak ) );

      SortPeaks( peaks.elementAt(0), peaks.elementAt(1), strong_peaks );
      Vector<IPeakQ> neighbors = new Vector<IPeakQ>();
      for ( int i = 2; i < Math.min(NUM_NEIGHBORS, strong_peaks.size()); i++ )
        neighbors.add( strong_peaks.elementAt(i) );

      int retries = 0;
      while ( num_indexed  < REQUIRED_FRACTION * NUM_NEIGHBORS &&
              num_attempts < MAX_ATTEMPTS                      &&
              retries      < 3 )
      {
        IndexByOptimizing(peaks,lattice_params,UBinverse);
        System.out.println("After Optimize");
        ShowLatticeParams( UBinverse );

        num_indexed = NumIndexed( neighbors, UBinverse, hkl_tol );
        System.out.println("---------- NUM INDEXED = " + num_indexed +
                           " OUT OF " + neighbors.size() +
                           " WITH TOLERANCE = " + hkl_tol );
        num_attempts++;
        retries++;
      }
    }
                                         // Refine UB -----------------------
    String return_msg = "FALIED";
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
       num_to_add = (int)( .1 * peaks.size() );
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
         UBinverse = LinearAlgebra.copy( newUBinverse );
         ShowLatticeParams( newUBinverse );
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
       }
       UBinverse = LinearAlgebra.copy( newUBinverse );

        return_msg = "INDEXED: " +
                      NumIndexed( all_peaks, UBinverse, hkl_tol ) +
                     " OF " + all_peaks.size() +
                     " WITHIN " + hkl_tol;

        return_msg += "  LATTICE CONSTANTS:" + getLatticeParams( UBinverse );
       
        WriteNotIndexedPeaks( all_peaks, 
                              newUBinverse, 
                              hkl_tol, 
                              not_indexed_file_name );

        Index( all_peaks, UBinverse, hkl_tol );
        if ( peaks_file_name.length() > 0 )
          if ( all_peaks.size() > 0 &&                      // assume all peaks
               all_peaks.elementAt(0) instanceof Peak_new ) // are same type
          {
            Vector write_peaks = new Vector();
            for ( int i = 0; i < all_peaks.size(); i++ )
              write_peaks.add( all_peaks.elementAt(i) );
            Peak_new_IO.WritePeaks_new( peaks_file_name, write_peaks, false );
          }

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

        if ( matrix_file_name != null && matrix_file_name.length() > 0 )
          Util.WriteMatrix( matrix_file_name, floatUB );
      }
    }

    return return_msg;
  }

 

   /**
    * Attempt to index the peaks from the vector of peaks, given the
    * lattice constants.
    * This method proceeds in three stages.  First the strongest
    * peaks in the file are used with an optimization based method
    * to find an initial indexing for a significant fraction of the
    * strongest peaks, using the specified lattice constants.  
    * Next, the algorithm attempts to index all of the strongest peaks
    * by adjusting the lattice parameters.
    * Finally, the algorithm attempts to extend this indexing to all
    * of the peaks. The method WriteNotIndexedPeaks can be used to
    * write the unindexed peaks to another file so that they can be (possibly)
    * indexed latter. This allows indexing twins or more complicated samples
    * with several crystalites.
    * @see WriteNotIndexedPeaks
    *
    * @param  all_peaks             Vector of all peaks
    * @param  a                     Lattice parameter 'a'
    * @param  b                     Lattice parameter 'b'
    * @param  c                     Lattice parameter 'c'
    * @param  alpha                 Lattice parameter alpha 
    * @param  beta                  Lattice parameter beta 
    * @param  gamma                 Lattice parameter gamma
    */
   public static float[][] IndexPeaksWithOptimizer( Vector all_peaks,
                                                 float a,
                                                 float b,
                                                 float c,
                                                 float alpha,
                                                 float beta,
                                                 float gamma)
                                                 throws IOException
   {
     int MAX_STRONG     = 40;
     int MAX_ATTEMPTS   = 25;
     int NUM_NEIGHBORS  = 20;

     double lattice_params[] = new double[6];
     lattice_params[0] = a;
     lattice_params[1] = b;
     lattice_params[2] = c;
     lattice_params[3] = alpha;
     lattice_params[4] = beta;
     lattice_params[5] = gamma;

    
     clearIndexes( all_peaks );
                                                // now sort by ipkobs
     SortPeaks( all_peaks );

     Vector strong_peaks = new Vector();
     int num_strong = all_peaks.size();
                                          // just look at 40 strongest peaks
     if ( num_strong > MAX_STRONG )
       num_strong = MAX_STRONG;

     for ( int i = 0; i < num_strong; i++ )
       strong_peaks.add( all_peaks.elementAt(i) );
                                         // sort in increasing order of |Q|
     SortPeaksMagQ( strong_peaks, false );

     double[][] UBinverse = new double[3][3];
     double[][] newUBinverse = new double[3][3];
     double[][] hkls;

     double REQUIRED_FRACTION = 0.4;  // NOTE: This is critical.  IF too low
                                      //       (say .3) quartz many fails
     double hkl_tol        = 0.12;
     int    num_attempts   = 0;
     int    num_indexed    = 0;
     int    second_peak    = 0;

     if ( NUM_NEIGHBORS > strong_peaks.size() )
       NUM_NEIGHBORS = strong_peaks.size();
 
                                   // Initial indexing -------------------
    Vector peaks = new Vector();
    Random random = new Random();
    while ( num_indexed  < REQUIRED_FRACTION * NUM_NEIGHBORS &&
            num_attempts < MAX_ATTEMPTS )
    {
      System.out.println(" ===================== AUTO INDEXING, ATTEMPT # "
                         + (num_attempts + 1) );
      peaks.clear();

      SortPeaksMagQ( strong_peaks, false );
      peaks.add( strong_peaks.elementAt(0) );
      second_peak = 1+(int)( (strong_peaks.size()/2) * random.nextDouble());
      peaks.add( strong_peaks.elementAt( second_peak ) );

      SortPeaks( (IPeakQ)peaks.elementAt(0), 
                 (IPeakQ)peaks.elementAt(1), 
                 strong_peaks );

      Vector<IPeakQ> neighbors = new Vector<IPeakQ>();
      for ( int i = 2; i < Math.min(NUM_NEIGHBORS, strong_peaks.size()); i++ )
        neighbors.add( (IPeakQ)strong_peaks.elementAt(i) );

      int retries = 0;
      while ( num_indexed  < REQUIRED_FRACTION * NUM_NEIGHBORS &&
              num_attempts < MAX_ATTEMPTS                      &&
              retries      < 3 )
      {
        IndexByOptimizing(peaks,lattice_params,UBinverse);
        System.out.println("After Optimize");
        ShowLatticeParams( UBinverse );

        num_indexed = NumIndexed( neighbors, UBinverse, hkl_tol );
        System.out.println("---------- NUM INDEXED = " + num_indexed +
                           " OUT OF " + neighbors.size() +
                           " WITH TOLERANCE = " + hkl_tol );
        num_attempts++;
        retries++;
      }
    }
                                         // Refine UB -----------------------
    String return_msg = "FAILED";
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
       num_to_add = (int)( .1 * peaks.size() );

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
         UBinverse = LinearAlgebra.copy( newUBinverse );
         ShowLatticeParams( newUBinverse );
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

        }
        return_msg = "NUM INDEXED = " +
                      NumIndexed( all_peaks, UBinverse, hkl_tol ) +
                     " OUT OF " + all_peaks.size() +
                     " WITH TOLERANCE = " + hkl_tol;
       
       

        Index( all_peaks, UBinverse, hkl_tol );
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

        System.out.println(return_msg);
        return floatUB ;
      }
    }
    
    throw new IllegalArgumentException("Could not find orientation matrix");
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
       lattice_params[0] = 3.96;
       lattice_params[1] = 3.96;
       lattice_params[2] = 13.09;
       lattice_params[3] = 90;
       lattice_params[4] = 90;
       lattice_params[5] = 90;
     }
     else if ( args[0].equalsIgnoreCase( "FeSi" ) )
     {
       lattice_params[0] = 4.486;
       lattice_params[1] = 4.486;
       lattice_params[2] = 4.486;
       lattice_params[3] = 90;
       lattice_params[4] = 90;
       lattice_params[5] = 90;
     }
     else if ( args[0].equalsIgnoreCase( "natrolite" ) )
     {
       lattice_params[0] = 18.325;
       lattice_params[1] = 18.653; 
       lattice_params[2] = 6.601;
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
       System.out.println("FeSi");
       System.out.println("natrolite");
       System.out.println("FAKE");
       System.exit(1);
     }

     String peaks_file_name       = "/home/dennis/NotIndexed.peaks";
     String matrix_file_name      = "/home/dennis/IndexAll.mat";
     String not_indexed_file_name = "/home/dennis/StillNotIndexed.peaks";
     IndexPeaksWithOptimizer( peaks_file_name,
                              matrix_file_name,
                              not_indexed_file_name,
                              (float)lattice_params[0],
                              (float)lattice_params[1],
                              (float)lattice_params[2],
                              (float)lattice_params[3],
                              (float)lattice_params[4],
                              (float)lattice_params[5] );
   }
}

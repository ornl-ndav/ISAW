/* 
 * File: FindPeaksViaSort.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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

package DataSetTools.operator.Generic.TOF_SCD;

import java.util.*;

public class FindPeaksViaSort 
{
  private static String  discard_fmt_1 = "%3d %3d %4d %6d Discarded: %s\n" ;
  private static String  discard_fmt_2 = "%3d %3d %4d %6d Discarded: %s %d\n" ;

  private static String BODY_OVERLAPS      = "Body Overlaps Peak#";
  private static String OVERLAPS           = "Overlaps Peak#";
  private static String UNDEFINED_CENTROID = "Undefined Centroid";
  private static String NOT_LOCAL_MAX      = "Not Local Max";

  /**
   *  Add information on possible peak that was discarded to the specified
   *  log buffer.
   *
   *  @param  log      String buffer that holds the overall log info
   *  @param  reason   String giving reason for discarding peak
   *  @param  row      The row number of this possible peak
   *  @param  col      The column number of this possible peak
   *  @param  chan     The channel number of this possible peak
   *  @param  value    The value of this possible peak
   *  @param  index    The index of a second peak that this overlaps
   */
  public static void ShowDiscard( StringBuffer  log,
                                  String reason,
                                  int row,
                                  int col,
                                  int chan,
                                  int value,
                                  int index  )
  {
    String message;

    if ( index >= 0 )
      message = String.format( discard_fmt_2, 
                               col, row, chan, value, reason, index );
    else
      message = String.format( discard_fmt_1, 
                               col, row, chan, value, reason );

    log.append( message );  
  }


  /**
   *  Find the peaks in the three dimension array of SCD data.  
   *  NOTE: We assume data stored as raw_data[row][col][channel] !
   *        Histogram must be allocated array of 10000 ints !
   *  
   *  @param  raw_data        The 3D array of data from one detector
   *  @param  do_smoothing    If true, the data will be smoothed by 
   *                          replacing the value at each bin by the sum
   *                          of the 3x3 neighborhood of the bin on the
   *                          time slice.
   *  @param  num_requested   The maximum number of peaks that should be
   *                          returned.
   *  @param  threshold       The minimum raw peak count to use in the
   *                          initial search for a peak.
   *  @param  row_border      The number of rows to omit at the top and
   *                          bottom of the detector
   *  @param  col_border      The number of columns to omit at the top and
   *                          bottom of the detector
   *  @param  min_chan_index  The minimum time channel to use
   *  @param  max_chan_index  The maximum time channel to use
   *  @param  histogram       An array of 10,000 floats that will be filled
   *                          out with the histogram of the detector data
   *  @param  log             The StringBuffer into which log messages will
   *                          be written
   *
   *  @return an array of BasicPeakInfo objects listing the peaks that were
   *          found.
   */
  public static BasicPeakInfo[] getPeaks( float[][][]  raw_data,
                                          boolean      do_smoothing,
                                          int          num_requested,
                                          int          threshold,
                                          int          row_border,
                                          int          col_border,
                                          int          min_chan_index,
                                          int          max_chan_index,
                                          int[]        histogram,
                                          StringBuffer log  )
  {
    log.append( "START: MIN_CHAN_INDEX = " + min_chan_index + "\n" );
    log.append( "START: MAX_CHAN_INDEX = " + max_chan_index + "\n" );

    long        start;           // used for timing operations
    long        end;

    float[][][] data_arr;        // possibly smoothed counts 
    long[]      count_list;      // sorted list of counts

    float INITIAL_EXTENT = 1.0f;
    int   CHAN_BORDER    = 3;

    start = System.nanoTime();

    int n_rows  = raw_data.length;
    int n_cols  = raw_data[0].length;
    int n_pages = raw_data[0][0].length;

    if ( min_chan_index < CHAN_BORDER )
      min_chan_index = CHAN_BORDER;

    if ( max_chan_index > n_pages - CHAN_BORDER - 1 )
         max_chan_index = n_pages - CHAN_BORDER - 1;

    log.append("NUMBER OF ROWS  = " + n_rows + "\n" );
    log.append("NUMBER OF COLS  = " + n_cols + "\n" );
    log.append("NUMBER OF PAGES = " + n_pages + "\n" );

    if ( do_smoothing )                // Smooth IN PLACE!
    {
      float[][] prev_row_average = new float[n_cols][n_pages];
      float[][] row_average      = new float[n_cols][n_pages];
      float[][] temp;

                                       // scale row 0 by factor of 9
      for ( int col = 0; col < n_cols-1; col++ )
        for ( int page = 0; page < n_pages; page++ )
          prev_row_average[col][page] = 9 * raw_data[0][col][page];


      for ( int row = 1; row < n_rows-1; row++ )
      {
                                      // scale first and last col
                                      // by 9
        for ( int page = 0; page < n_pages; page++ )
        {
          row_average[    0   ][page] = 9 * raw_data[row][    0   ][page];
          row_average[n_cols-1][page] = 9 * raw_data[row][n_cols-1][page];
        }
/*
        System.arraycopy( raw_data[row][0], 0, row_average[0], 0, n_pages );
        System.arraycopy( raw_data[row][n_cols-1], 0, 
                          row_average[n_cols-1], 0, n_pages );
*/
        for ( int col = 1; col < n_cols-1; col++ )
          for ( int page = 0; page < n_pages; page++ )
            row_average[col][page] = raw_data[row-1][col-1][page] +
                                     raw_data[row-1][col  ][page] +
                                     raw_data[row-1][col+1][page] +
                                     raw_data[row  ][col-1][page] +
                                     raw_data[row  ][col  ][page] +
                                     raw_data[row  ][col+1][page] +
                                     raw_data[row+1][col-1][page] +
                                     raw_data[row+1][col  ][page] +
                                     raw_data[row+1][col+1][page];

         temp = raw_data[row - 1];
         raw_data[row - 1] = prev_row_average;
         prev_row_average = row_average;
         row_average = temp;
      }

      raw_data[ n_rows - 2 ] = prev_row_average;

                                     // scale last row by factor of 9 
      for ( int col = 0; col < n_cols-1; col++ )
        for ( int page = 0; page < n_pages; page++ )
          raw_data[n_rows-1][col][page] *= 9;

      end = System.nanoTime();
      log.append("--- Time(ms) to smooth data = " + (end-start)/1e6 + "\n" );
    }

    data_arr = raw_data;
      

/*
    if ( do_smoothing )                // Smooth into new array
    {
      float[][][] smoothed_data = new float[n_rows][n_cols][n_pages];

                                       // NOTE: We leave a border of 0's around
                                       //       the edges of the array.
      for ( int col = 1; col < n_cols-1; col++ )
        for ( int row = 1; row < n_rows-1; row++ )
          for ( int page = 0; page < n_pages; page++ )
            smoothed_data[row][col][page] = raw_data[row-1][col-1][page] +
                                            raw_data[row-1][col  ][page] +
                                            raw_data[row-1][col+1][page] +
                                            raw_data[row  ][col-1][page] +
                                            raw_data[row  ][col  ][page] +
                                            raw_data[row  ][col+1][page] +
                                            raw_data[row+1][col-1][page] +
                                            raw_data[row+1][col  ][page] +
                                            raw_data[row+1][col+1][page];
      end = System.nanoTime();
      log.append("--- Time(ms) to smooth data = " + (end-start)/1e6 + "\n" );
      data_arr = smoothed_data;
    }
    else
      data_arr = raw_data;
*/

    start = System.nanoTime();

    // find histogram ....

    int max_histogram = histogram.length;
    int     value;
    for ( int col = 0; col < n_cols; col++ )
      for ( int row = 0; row < n_rows; row++ )
        for ( int page = 0; page < n_pages; page++ )
        {
          value = (int)data_arr[row][col][page];

          if ( value >= max_histogram )
            histogram[ max_histogram - 1 ]++;
          else if ( value < 0 )
            histogram[0]++; 
          else
            histogram[ value ]++;
        }

    // find cumulative distribution function of smoothed data ....
    // cdf[k] is the number of bins with counts less than or equal to k.
    int[] cdf = new int[max_histogram];
    cdf[0] = histogram[0];
    for ( int i = 1; i < max_histogram; i++ )
      cdf[i] = cdf[i-1] + histogram[i];

    if ( do_smoothing )                      // scale up any specified 
      threshold *= 9;                        // threshold

    int num_bins = n_rows * n_cols * n_pages;
    int cutoff;
    if ( threshold <= 0 )                     // automatically set threshold
    {
      cutoff = (int)( num_bins * 0.999f );
      threshold = 0;
      while ( threshold < max_histogram && cdf[threshold] < cutoff )
        threshold++;
 
      log.append("Computed threshold = " + threshold + "\n" );

      if ( do_smoothing )
      {
        if ( threshold < 5 )        // we don't consider a bin with less
          threshold = 5;            // than actual 1/2 count to be a peak for 
      }                             // smoothed data.
      else
      {
        if ( threshold < 3 )        // we don't consider a bin with less than
          threshold = 3;            // three counts to be a peak for raw data.
      }
    }
                                    // "Safety": don't check more than 1% of
                                    // the bins, regardless of what the user
                                    // might have requested.
    cutoff = (int)( num_bins * 0.99f );
    while ( threshold < max_histogram && cdf[threshold] < cutoff )
      threshold++;

    if ( threshold > cdf.length - 2) // BAD THRESHOLD would find 0 peaks if
      threshold = cdf.length - 2;    // histogram is large enough.

    if ( threshold < 2 )            // in any case require at least two conunts
      threshold = 2;                // to be a peak!
    
    int num_above_threshold = num_bins - cdf[threshold];
    log.append( "THRESHOLD = " + threshold + "\n" );
    log.append( "NUM_ABOVE = " + num_above_threshold + "\n" );
    long[] temp_list = new long[ num_above_threshold ];

    int index = 0;
    int bin_count = 0;
    int max = 0;
    for ( int row = row_border; row < n_rows - 1 - row_border; row++ )
      for ( int col = col_border; col < n_cols - 1 - col_border; col++ ) 
        for ( int chan = min_chan_index; chan <= max_chan_index; chan++ )
        {
           value = (int)data_arr[row][col][chan];
           bin_count++;
           if ( value > max )
             max = value;
           if ( value > threshold )
           {
             temp_list[index] = BinaryPeakCode.Encode( value, row, col, chan );
             index++;
           }
        }
        
    log.append("MIN_CHAN_INDEX = " + min_chan_index + "\n" );
    log.append("MAX_CHAN_INDEX = " + max_chan_index + "\n" );
    log.append("NUM BINS = " + bin_count + "\n" );
    log.append("MAX = " + max + "\n" );
    count_list = new long[ index ];
    System.arraycopy( temp_list, 0, count_list, 0, index );
    log.append(count_list.length + " interior bins over " + threshold + "\n" );

    temp_list = null;
    
    Arrays.sort( count_list );

    int info[]   = new int[4]; 
    int pk_count = 0;

/*  It takes a lot of room in the log file to print out this list,
    so for now we won't print it.

    log.append( "--- Sorted list of bins above threshold" + "\n"  );
    log.append( String.format("%5s; %4s %4s %4s %5s\n", 
                              "Index", "Col", "Row", "Chan", "Value") );

                                                   // print list of peaks we're
    for ( int i = 0; i < count_list.length; i++ )  // considering...
    {
      info = BinaryPeakCode.Decode( count_list[i], info );
      int chan = info[0];
      int col  = info[1];
      int row  = info[2];
      int val  = info[3];
      log.append( String.format ( "%5d; %4d %4d %4d %5d\n",
                                   i, col, row, chan, val ) );
    }
*/

    index = count_list.length - 1;
    Vector peaks = new Vector( num_requested );
    while ( index >= 0 && peaks.size() < num_requested )
    {
      info = BinaryPeakCode.Decode( count_list[index], info );
      int chan = info[0];
      int col  = info[1];
      int row  = info[2];
      int val  = info[3];

      boolean overlaps = false;                    // check if point overlaps
      int     peak_index = 0;                      // a previous peak.
      while ( !overlaps && peak_index < peaks.size() )
      {
        BasicPeakInfo old_peak = (BasicPeakInfo)peaks.elementAt( peak_index );
        if ( old_peak.overlap( row, col, chan ) )
          overlaps = true;
        else
          peak_index++;
      }

      boolean local_max = true;                 // check if point is local max
      value = (int)data_arr[row][col][chan];

      if ( !overlaps )        
      { 
        int delta_row  = 3;
        int delta_col  = 3;
        int delta_chan = 3;
        int row_0 = Math.max( row-delta_row, 0 );
        int row_1 = Math.min( row+delta_row, n_rows-1 );
        int col_0 = Math.max( col-delta_col, 0 );
        int col_1 = Math.min( col+delta_col, n_cols-1 );
        int chan_0 = Math.max( chan-delta_chan, 0 );
        int chan_1 = Math.min( chan+delta_chan, n_pages-1 );
        for ( int i = row_0; i <= row_1; i++ )
          for ( int j = col_0; j <= col_1; j++ )
            for ( int k = chan_0; k <= chan_1; k++ )
              if ( data_arr[i][j][k] > value )
                local_max = false;

        if ( !local_max )
          ShowDiscard( log, NOT_LOCAL_MAX, col, row, chan, value, -1 );
      }
                                              // if point ok, find the extent
                                              // of the peak and check if the
                                              // body overlaps a previous peak
      if ( ! overlaps && local_max ) 
      {
        BasicPeakInfo peak = new BasicPeakInfo( row  + 0.5f, 
                                                col  + 0.5f, 
                                                chan + 0.5f, 
                                                2*INITIAL_EXTENT,
                                                2*INITIAL_EXTENT, 
                                                  INITIAL_EXTENT,
                                                val   );

        
        log.append( "\nCHECKING POSSIBLE PEAK " + 
                       peak.col_row_chan_ipk(data_arr)  );

        boolean peak_ok;                         // true if peak seems
                                                 // ok after finding
                                                 // its extent.
        peak_ok = peak.set_centroid_and_extent( data_arr, log ); 

        if ( peak_ok )                           // make sure it doesn't        
        {                                        // overlap a previous peak
          peak_index = 0;
          while ( !overlaps && peak_index < peaks.size() )
          {
           BasicPeakInfo old_peak = (BasicPeakInfo)peaks.elementAt(peak_index);
            if ( old_peak.overlap( peak ) )
              overlaps = true;
            else
              peak_index++;
          }

          if ( ! overlaps )
          {
            peaks.add( peak );
            log.append("*******" + pk_count + ":  ADDED PEAK " + 
                                   peak.col_row_chan_ipk( data_arr ) + "\n");
            pk_count++;
          }
          else
            ShowDiscard(log, BODY_OVERLAPS, col, row, chan, value, peak_index);
        }
        else
          ShowDiscard( log, UNDEFINED_CENTROID, col, row, chan, value, -1 );
      }
      else 
        ShowDiscard( log, OVERLAPS, col, row, chan, value, peak_index );

      index--;
    } 

    log.append("\n");
    log.append("NUMBER OF PEAKS = " + peaks.size() + "\n" );

    end = System.nanoTime();
    log.append("Time(ms) to find peaks = " + (end-start)/1e6 + "\n");
    log.append("--------------------- Peaks Found ------------------------\n");

    BasicPeakInfo[] peak_array = new BasicPeakInfo[ peaks.size() ];
    for ( int i = 0; i < peak_array.length; i++ )
      peak_array[i] = (BasicPeakInfo)peaks.elementAt(i);

    for ( int i = 0; i < peak_array.length; i++ )
    {
      log.append( String.format("Peak #%2d   ", i) );
      log.append( peak_array[i].col_row_chan_ipk( raw_data ) + "\n" );
    }

    return peak_array;
  }

}

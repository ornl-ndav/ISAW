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

  /**
   *  NOTE: We assume data stored as raw_data[row][col][channel] !
   *        Histogram must be allocated array of 10000 ints !
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

    if ( do_smoothing )
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

    if ( threshold <= 0 )                     // set threshold
    {
      int num_bins = n_rows * n_cols * n_pages;
      int cutoff = (int)( num_bins * 0.999f );
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

    if ( threshold < 2 )            // in any case require at least two conunts
      threshold = 2;                // to be a peak!

    // NOTE: Figure out the size of the array we need, when we
    //       sort the potential peaks.
    int num_bins = n_rows * n_cols * n_pages;

    if ( threshold > cdf.length - 2) // BAD THRESHOLD would find 0 peaks if
      threshold = cdf.length - 2;    // histogram is large enough.
    
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

    log.append( "--- Sorted list of bins above threshold" + "\n"  );
    log.append( String.format("%5s; %4s %4s %4s %5s\n", 
                              "Index", "Col", "Row", "Chan", "Value") );

    int pk_count = 0;

    int info[] = new int[4];                        // show positions we're 
    for ( int i = 0; i < count_list.length; i++ )   // considering...
    {
      info = BinaryPeakCode.Decode( count_list[i], info );
      int chan = info[0];
      int col  = info[1];
      int row  = info[2];
      int val  = info[3];
      log.append( String.format ( "%5d; %4d %4d %4d %5d\n",
                                   i, col, row, chan, val ) );
    }

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
          log.append("NOT LOCAL MAX " + col  + 
                                  " " + row  + 
                                  " " + chan + 
                                  " " + value );
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
            log.append("Bin Index: " + index + " BODY OVERLAPS " +
                        peak_index + "\n");

        }
        else
          log.append("Bin Index: " + index + " UNDEFINED CENTROID\n");
      }
      else 
        log.append("Bin Index: " + index + " OVERLAPS " + peak_index + "\n");

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

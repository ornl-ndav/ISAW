/* 
 * File: FindPeaksViaSort.java
 *
 * Copyright (C) 2008,2009 Dennis Mikkelson
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
import gov.anl.ipns.Util.Numeric.*;

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
  public static void LogDiscard( StringBuffer  log,
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
   *  Smooth the specified 3D array by summing neighborhoods of each
   *  pixel on each "page" and return a new array with the smoothed
   *  data.  The array indices of the original array are assumed to 
   *  represent rows, columns and pages in that order.  This method 
   *  does the smoothing "in place" so minimal extra storage is needed,
   *  but THE ORIGINAL ARRAY IS DESTROYED.  The returned 3D array has the same
   *  dimensions as the original array and contains the smoothed data.
   *  For pixels interior to a page the value is the sum of the neighboring
   *  pixels in a 3x3 neighborhood centered a the pixel.  For edge pixels,
   *  the sum of a 3x2 or 2x3 neighborhood is scaled by a factor of 9/6
   *  and used as value of the smoothed data.  For corner pixels, the sum
   *  of a 2x2 neighborhood containing the corner pixel is scaled by a
   *  factor of 9/4 and used as the value of the smoothed data.
   *
   *  NOTE: While this method returns complete smoothed data, it takes
   *  about three times as long as SmoothData_1_Fast.
   *
   *  @param raw_data  3D array of floats containing the original data.  This
   *                   array must be a full rectangular array, i.e. NOT a 
   *                   "ragged" array.
   *
   *  @return a 3D array of floats containing the smoothed data, using 
   *          mostly the same memory locations as in the raw_data array,
   *          BUT ARRANGED DIFFERENTLY.
   */
  public static float[][][] SmoothData( float[][][] raw_data )
  {
    int n_rows  = raw_data.length;
    int n_cols  = raw_data[0].length;
    int n_pages = raw_data[0][0].length;

                                       // Smooth IN PLACE!  Use buffers to 
                                       // hold the row sums from two complete
                                       // rows, so the original values can
                                       // be used from the raw_data array,
                                       // before replacing with the sums. 
    float[][] prev_row_sums = new float[n_cols][n_pages];
    float[][] row_sums      = new float[n_cols][n_pages];
    float[][] temp;

    for ( int row = 0; row < n_rows; row++ )
    {
      int row_0 = Math.max(        0, row-1 );
      int row_1 = Math.min( n_rows-1, row+1 );
      for ( int col = 0; col < n_cols; col++ )
      {
        int col_0 = Math.max(        0, col-1 );
        int col_1 = Math.min( n_cols-1, col+1 );

        for ( int page = 0; page < n_pages; page++ )
          row_sums[col][page] = 0;

        for ( int rr = row_0; rr <= row_1; rr++ )
          for ( int cc = col_0; cc <= col_1; cc++ )
            for ( int page = 0; page < n_pages; page++ )
              row_sums[col][page] += raw_data[rr][cc][page];

        int n_pix = (row_1 - row_0 + 1) * (col_1 - col_0 + 1);
        float scale = 9.0f/n_pix;
        for ( int page = 0; page < n_pages; page++ )
          row_sums[col][page] *= scale;
      }
                                      // now cycle the buffers forward by one
      if ( row > 0 )
      {
        temp = raw_data[row - 1];
        raw_data[row - 1] = prev_row_sums;
        prev_row_sums = row_sums;
        row_sums = temp;
      }
      else                          // first time through, swap buffers
      {
        temp          = prev_row_sums;
        prev_row_sums = row_sums;
        row_sums      = temp;
      }
    }                               // finally, take care of the last row 
    raw_data[ n_rows - 1 ] = prev_row_sums;

    return raw_data;
  }


  /**
   *  Smooth the specified 3D array by summing neighborhoods of each
   *  pixel on each "page" and return a new array with the smoothed
   *  data.  The array indices of the original array are assumed to 
   *  represent rows, columns and pages in that order.  This method 
   *  does the smoothing "in place" so minimal extra storage is needed,
   *  but THE ORIGINAL ARRAY IS DESTROYED.  The returned 3D array has the same
   *  dimensions as the original array and contains the smoothed data.
   *  For pixels interior to a page the value is the sum of the neighboring
   *  pixels in a 3x3 neighborhood centered a the pixel.  For edge pixels,
   *  the sum of a 3x2 or 2x3 neighborhood is scaled by a factor of 9/6
   *  and used as value of the smoothed data.  For corner pixels, the sum
   *  of a 2x2 neighborhood containing the corner pixel is scaled by a
   *  factor of 9/4 and used as the value of the smoothed data.
   *
   *  NOTE: This method also uses temporary references to arrays of floats
   *  to do the smoothing very rapidly.  It both does complete smoothing
   *  including edge and corner pixels and executes very fast (about
   *  three times FASTER then the SmoothData method.)
   *
   *  @param raw_data  3D array of floats containing the original data.  This
   *                   array must be a full rectangular array, i.e. NOT a 
   *                   "ragged" array.
   *
   *  @return a 3D array of floats containing the smoothed data, using 
   *          mostly the same memory locations as in the raw_data array,
   *          BUT ARRANGED DIFFERENTLY.
   */
  public static float[][][] SmoothData_Fast( float[][][] raw_data )
  {
    int n_rows  = raw_data.length;
    int n_cols  = raw_data[0].length;
    int n_pages = raw_data[0][0].length;

    float[] temp00;
    float[] temp01;
    float[] temp02;
    float[] temp10;
    float[] temp11;
    float[] temp12;
    float[] temp20;
    float[] temp21;
    float[] temp22;
                                     // find first and last rows of smoothed
                                     // data, BEFORE messing with the
                                     // original data
    float[][] first_row = new float[n_cols][n_pages];
    float[][] last_row  = new float[n_cols][n_pages];
    int last_row_index  = n_rows-1;

    for ( int col = 1; col < n_cols-1; col++ )
    {
                                                // interior of first row
        temp10 = raw_data[0][col-1];            // scale by 9/6
        temp11 = raw_data[0][col  ];
        temp12 = raw_data[0][col+1];
        temp20 = raw_data[1][col-1];
        temp21 = raw_data[1][col  ];
        temp22 = raw_data[1][col+1];
        for ( int page = 0; page < n_pages; page++ )
          first_row[col][page] = 1.5f * ( temp10[page] +
                                          temp11[page] +
                                          temp12[page] +
                                          temp20[page] +
                                          temp21[page] +
                                          temp22[page] );

                                                     // interior of last row
        temp00 = raw_data[last_row_index-1][col-1];  // scale by 9/6
        temp01 = raw_data[last_row_index-1][col  ];
        temp02 = raw_data[last_row_index-1][col+1];
        temp10 = raw_data[last_row_index  ][col-1];
        temp11 = raw_data[last_row_index  ][col  ];
        temp12 = raw_data[last_row_index  ][col+1];
        for ( int page = 0; page < n_pages; page++ )
          last_row[col][page] = 1.5f * ( temp00[page] +
                                          temp01[page] +
                                          temp02[page] +
                                          temp10[page] +
                                          temp11[page] +
                                          temp12[page] );
    }
                                                // NOW DO FOUR CORNER PIXELS
                                                // AT ENDS OF FIRST AND LAST
                                                // ROWS.
    temp11 = raw_data[0][0];                    // first row, first col
    temp12 = raw_data[0][1];                    // scale by 9/4
    temp21 = raw_data[1][0];
    temp22 = raw_data[1][1];
    for ( int page = 0; page < n_pages; page++ )
      first_row[0][page] = 2.25f * ( temp11[page] +
                                     temp12[page] +
                                     temp21[page] +
                                     temp22[page] );

    int last_col_index = n_cols - 1;
    temp10 = raw_data[0][last_col_index-1];     // first row, last col
    temp11 = raw_data[0][last_col_index  ];     // scale by 9/4
    temp20 = raw_data[1][last_col_index-1];
    temp21 = raw_data[1][last_col_index  ];
    for ( int page = 0; page < n_pages; page++ )
      first_row[last_col_index][page] = 2.25f * ( temp10[page] +
                                                  temp11[page] +
                                                  temp20[page] +
                                                  temp21[page] );

    temp01 = raw_data[last_row_index-1][0];      // last row, first_col
    temp02 = raw_data[last_row_index-1][1];      // scale by 9/4
    temp11 = raw_data[last_row_index  ][0];
    temp12 = raw_data[last_row_index  ][1];
    for ( int page = 0; page < n_pages; page++ )
      last_row[0][page] = 2.25f * ( temp01[page] +
                                    temp02[page] +
                                    temp11[page] +
                                    temp12[page] );

                                                   // last row, last_col
                                                   // scale by 9/4
    temp00 = raw_data[last_row_index-1][last_col_index-1];
    temp01 = raw_data[last_row_index-1][last_col_index  ];
    temp10 = raw_data[last_row_index  ][last_col_index-1];
    temp11 = raw_data[last_row_index  ][last_col_index  ];
    for ( int page = 0; page < n_pages; page++ )
      last_row[last_col_index][page] = 2.25f * ( temp00[page] +
                                                 temp01[page] +
                                                 temp10[page] +
                                                 temp11[page] );

                                                 // NOW TAKE CARE OF ALL
                                                 // INTERIOR ROWS.

    float[][][] smoothed_data = new float[n_rows][][];
    smoothed_data[0]          = new float[n_cols][n_pages];
    smoothed_data[n_rows-1]   = new float[n_cols][n_pages];

    float[][] row_sums =  new float[n_cols][n_pages];
    float[][] row_buffer = raw_data[0];
    for ( int row = 1; row < n_rows-1; row++ )
    {                                       // Do Column 0, with 6 neighbors
                                            // so weight by factor of 1.5
      temp01 = row_buffer[ 0 ];
      temp02 = row_buffer[ 1 ];
      temp11 = raw_data[row  ][ 0 ];
      temp12 = raw_data[row  ][ 1 ];
      temp21 = raw_data[row+1][ 0 ];
      temp22 = raw_data[row+1][ 1 ];
      for ( int page = 0; page < n_pages; page++ )
        row_sums[0][page] = 1.50f *( temp01[page] +
                                     temp02[page] +
                                     temp11[page] +
                                     temp12[page] +
                                     temp21[page] +
                                     temp22[page] );

                                              // Do columns 1 - n-2, with 9
                                              // neighbors, so weight is 1
      for ( int col = 1; col < n_cols-1; col++ )
      {
        temp00 = row_buffer[col-1];
        temp01 = row_buffer[col  ];
        temp02 = row_buffer[col+1];
        temp10 = raw_data[row  ][col-1];
        temp11 = raw_data[row  ][col  ];
        temp12 = raw_data[row  ][col+1];
        temp20 = raw_data[row+1][col-1];
        temp21 = raw_data[row+1][col  ];
        temp22 = raw_data[row+1][col+1];
        for ( int page = 0; page < n_pages; page++ )
          row_sums[col][page] = temp00[page] +
                                temp01[page] +
                                temp02[page] +
                                temp10[page] +
                                temp11[page] +
                                temp12[page] +
                                temp20[page] +
                                temp21[page] +
                                temp22[page];
       }

                                          // Do last column, with 6 neighbors
                                          // so weight by factor of 1.5
      int last_col = n_cols - 1;
      temp00 = row_buffer[last_col - 1];
      temp01 = row_buffer[last_col    ];
      temp10 = raw_data[row  ][last_col - 1];
      temp11 = raw_data[row  ][last_col    ];
      temp20 = raw_data[row+1][last_col - 1];
      temp21 = raw_data[row+1][last_col    ];
      float sum;
      for ( int page = 0; page < n_pages; page++ )
        row_sums[last_col][page] = 1.50f *( temp00[page] +
                                            temp01[page] +
                                            temp10[page] +
                                            temp11[page] +
                                            temp20[page] +
                                            temp21[page] );
       smoothed_data[row] = row_sums;
       row_sums = row_buffer;
       row_buffer = raw_data[row];
    }
                                         // finally put in the first and last
                                         // rows of smoothed data.
    smoothed_data[0]        = first_row;
    smoothed_data[n_rows-1] = last_row;

    return smoothed_data;
  }


  /**
   *  Smooth the specified 3D array by summing 3x3 neighborhoods of each
   *  non-border pixel on each "page" and return a new array with the smoothed
   *  data.  The array indices of the original array are assumed to 
   *  represent rows, columns and pages in that order.  The original array
   *  is NOT changed by this method.  The returned 3D array has the same
   *  dimensions as the original array and contains the smoothed data for
   *  pixels interior to a page, but has zero values in all first and last 
   *  rows and columns. 
   *  NOTE: Since a new array is created and the original array is not changed
   *  this smoothing method uses twice as much memory as the original array.
   *
   *  @param raw_data  3D array of floats containing the original data.  This
   *                   array must be a full rectangular array, i.e. NOT a 
   *                   "ragged" array.
   *
   *  @return a new 3D array of floats containing the smoothed data.
   */
  public static float[][][] SmoothData_1( float[][][] raw_data )
  {
    int n_rows  = raw_data.length;
    int n_cols  = raw_data[0].length;
    int n_pages = raw_data[0][0].length;

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
    return smoothed_data;
  }


  /**
   *  Smooth the specified 3D array by summing 3x3 neighborhoods of each
   *  non-border pixel on each "page" and return a new array with the smoothed
   *  data.  This method produces the same result, and requires the same
   *  amount of memory as SmoothData_1.  However, it has been optimized for
   *  speed by reducing the amount of array dereferencing, using references
   *  to neighboring 1-D arrays.  It takes about 50-75% of the time 
   *  the SmoothData_1 method.
   *
   *  @param raw_data  3D array of floats containing the original data.  This
   *                   array must be a full rectangular array, i.e. NOT a 
   *                   "ragged" array.
   *
   *  @return a new 3D array of floats containing the smoothed data.
   */
  public static float[][][] SmoothData_1_Fast( float[][][] raw_data )
  {
    int n_rows  = raw_data.length;
    int n_cols  = raw_data[0].length;
    int n_pages = raw_data[0][0].length;

    float[] temp00;        // These temporary arrays are set to point to 
    float[] temp01;        // spectra that are in a neighborhood of the 
    float[] temp02;        // central spectrum, temp11.
    float[] temp10;
    float[] temp11;
    float[] temp12;
    float[] temp20;
    float[] temp21;
    float[] temp22;

    float[][][] smoothed_data = new float[n_rows][n_cols][n_pages];

                                       // NOTE: We leave a border of 0's around
                                       //       the edges of the array.
    for ( int col = 1; col < n_cols-1; col++ )
      for ( int row = 1; row < n_rows-1; row++ )
      {
        temp00 = raw_data[row-1][col-1];
        temp01 = raw_data[row-1][col  ];
        temp02 = raw_data[row-1][col+1];
        temp10 = raw_data[row  ][col-1];
        temp11 = raw_data[row  ][col  ];
        temp12 = raw_data[row  ][col+1];
        temp20 = raw_data[row+1][col-1];
        temp21 = raw_data[row+1][col  ];
        temp22 = raw_data[row+1][col+1];
        for ( int page = 0; page < n_pages; page++ )
          smoothed_data[row][col][page] = temp00[page] +
                                          temp01[page] +
                                          temp02[page] +
                                          temp10[page] +
                                          temp11[page] +
                                          temp12[page] +
                                          temp20[page] +
                                          temp21[page] +
                                          temp22[page];
       }

    return smoothed_data;
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
   *                          time slice.  NOTE: This will alter the values
   *                          in the array, so a copy of the data should be
   *                          passed in if do_smoothing is set true.
   *  @param  num_requested   The maximum number of peaks that should be
   *                          returned.
   *  @param  threshold       Value that must be exceeded to consider a
   *                          bin to be a possible peak duing the initial 
   *                          scan through the data.
   *  @param  row_list        The list of rows to use when looking for peaks
   *                          numbered starting at 1, as on the data grid.
   *  @param  col_list        The list of columns to use when looking for peaks 
   *                          numbered starting at 1, as on the data grid.
   *  @param  min_chan        The first time channel to use when looking for
   *                          peaks
   *  @param  max_chan        The last time channel to use when looking for 
   *                          peaks
   *  @param  histogram       An array of 10,000 floats that will be filled
   *                          out with the histogram of the detector data
   *  @param  log             The StringBuffer into which log messages will
   *                          be written
   *
   *  @return an array of BasicPeakInfo objects listing the peaks that were
   *          found. NOTE: The row and column numbers in the BasicPeakInfo
   *          objects, are numbered starting with 0, so the values returned
   *          by the get centroid methods must be incremented by 1 to convert
   *          them to grid coordinates.
   */
  public static BasicPeakInfo[] getPeaks( float[][][]  raw_data,
                                          boolean      do_smoothing,
                                          int          num_requested,
                                          int          threshold,
                                          int[]        row_list,
                                          int[]        col_list,
                                          int          min_chan,
                                          int          max_chan,
                                          int[]        histogram,
                                          StringBuffer log  )
  {
    if ( raw_data == null )
      throw new IllegalArgumentException( "raw_data array is null" );
    else if ( raw_data.length <= 0 )
      throw new IllegalArgumentException( "raw_data array is missing rows" );
    else if ( raw_data[0] == null || raw_data[0].length <= 0 )
      throw new IllegalArgumentException( "raw_data array is missing columns" );
    else if ( raw_data[0][0] == null || raw_data[0][0].length <= 0 )
      throw new IllegalArgumentException( "raw_data is missing channels" );

    int n_rows  = raw_data.length;
    int n_cols  = raw_data[0].length;
    int n_pages = raw_data[0][0].length;

    log.append("NUMBER OF ROWS  = " + n_rows + "\n" );
    log.append("NUMBER OF COLS  = " + n_cols + "\n" );
    log.append("NUMBER OF PAGES = " + n_pages + "\n" );

    if ( min_chan < 0 || min_chan > n_pages-1 )
      min_chan = 0;
    if ( max_chan < 0 || max_chan > n_pages-1 )
      max_chan = n_pages - 1;

    log.append( "ROWS CHECKED = " + IntList.ToString(row_list)  + "\n" );
    log.append( "COLS CHECKED = " + IntList.ToString(col_list)  + "\n" );
    log.append( "MIN_CHAN = " + min_chan + "\n" );
    log.append( "MAX_CHAN = " + max_chan + "\n" );

    long        start;           // used for timing operations
    long        end;

    float[][][] data_arr;        // possibly smoothed counts 
    long[]      count_list;      // sorted list of counts

    float INITIAL_EXTENT = 1.0f;

    start = System.nanoTime();

    if ( do_smoothing )  
    {                            
//    data_arr = SmoothData       ( raw_data );      // 18 sec/DataSet 
      data_arr = SmoothData_Fast  ( raw_data );      //  4 sec/DataSet
//    data_arr = SmoothData_1     ( raw_data );      //  7 sec/DataSet
//    data_arr = SmoothData_1_Fast( raw_data );      //  7 sec/DataSet
      end = System.nanoTime();
      log.append("--- Time(ms) to smooth data = " + (end-start)/1e6 + "\n" );
    }
    else
      data_arr = raw_data;

/* 
   DUMP OUT DATA FROM ARRAY AS TEST
  
    System.out.println("IN FIND PEAKS ----------------------------");
    for ( int page = 0; page < n_pages; page++ )
    {
      for ( int row = 0; row < n_rows; row++ )
      {
        for ( int col = 0; col < n_cols; col++ )
          System.out.printf(" %6.2f", data_arr[row][col][page] );
        System.out.println();
      }

      System.out.println();
    }
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

    int num_bins = n_rows * n_cols * n_pages;
    int cutoff;
    if ( threshold <= 0 )           // If threshold not specified compute one
    {                               // that will select 0.1% of the points as
                                    // possible peaks.
      cutoff = (int)( num_bins * 0.999f );
      threshold = 0;
      while ( threshold < max_histogram && cdf[threshold] < cutoff )
        threshold++;

      log.append("COMPUTED THRESHOLD = " + threshold + "\n" );
    }
                                    // Now "tweak" the threshold, regardless
                                    // where we got it.
                                    // "Safety": don't check more than 1% of
                                    // the bins, regardless of what the user
                                    // might have requested.
    cutoff = (int)( num_bins * 0.99f );
    while ( threshold < max_histogram && cdf[threshold] < cutoff )
      threshold++;
                                    // If the number of counts is low, 
                                    // increasing threshold as above may have
                                    // set threshold so all bins are <= to it.
                                    // In this case shift down to include
                                    // some values. 
    if ( threshold >= cdf.length )
      threshold = cdf.length - 1;

    while ( threshold > 0 && cdf[threshold] >= num_bins )
      threshold--;

                                    // finally don't allow threshold to be
                                    // too low in any case.
    if ( do_smoothing )
    {
      if ( threshold < 5 )          // we don't consider a bin with less
        threshold = 5;              // than actual 2/3 count to be a peak for
    }                               // smoothed data.
    else
    {
      if ( threshold < 3 )          // we don't consider a bin with less than
        threshold = 3;              // four counts to be a peak for raw data.
    }
    
    int num_above_threshold = num_bins - cdf[threshold];
    log.append( "THRESHOLD = " + threshold + "\n" );
    log.append( "NUM_ABOVE = " + num_above_threshold + "\n" );
    long[] temp_list = new long[ num_above_threshold ];

    int index = 0;
    int bin_count = 0;
    int max = 0;
                                    // step through only the rows and columns
                                    // to keep, when looking for maxima
    for ( int r_index = 0; r_index < row_list.length; r_index++ )
      for ( int c_index = 0; c_index < col_list.length; c_index++ ) 
      {
                                    // rows and columns are specified in terms
                                    // of grid indices, which start at 1 :-(
        int row = row_list[r_index] - 1;     
        int col = col_list[c_index] - 1;
        if ( row >= 0 && row < n_rows &&
             col >= 0 && col < n_cols  )
          for ( int chan = min_chan; chan <= max_chan; chan++ )
          {
             value = (int)data_arr[row][col][chan];
             bin_count++;
             if ( value > max )
               max = value;
             if ( value > threshold )
             {
               temp_list[index] = BinaryPeakCode.Encode(value, row, col, chan);
               index++;
             }
          }
      }
        
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

      boolean bad_peak = false;                // check if point is in 
      int     peak_index = 0;                  // any previous peaks.
      BasicPeakInfo old_peak;
      while ( !bad_peak && peak_index < peaks.size() )
      {
        old_peak = (BasicPeakInfo)peaks.elementAt( peak_index );
        if ( old_peak.overlap( row, col, chan ) )
        {
          bad_peak = true;
          LogDiscard(log, OVERLAPS, col, row, chan, val, peak_index);
        }
        else
          peak_index++;
      }
                                               // if it misses all previous
                                               // peaks, check if local max
      if ( !bad_peak ) 
      { 
        float center_value = data_arr[row][col][chan];
        int delta_row  = 3;
        int delta_col  = 3;
        int delta_chan = 3;
        int row_0 = Math.max( row-delta_row, 0 );
        int row_1 = Math.min( row+delta_row, n_rows-1 );
        int col_0 = Math.max( col-delta_col, 0 );
        int col_1 = Math.min( col+delta_col, n_cols-1 );
        int chan_0 = Math.max( chan-delta_chan, 0 );
        int chan_1 = Math.min( chan+delta_chan, n_pages-1 );
/*
        log.append("VOXEL = " + col + ", " + row + ", " + chan + 
                   ", " + center_value + "\n" );
        log.append("COL  RANGE = " + col_0 + ", " + col_1   + "\n" );
        log.append("ROW  RANGE = " + row_0 + ", " + row_1   + "\n" );
        log.append("CHAN RANGE = " + chan_0 + ", " + chan_1 + "\n" );
*/
        for ( int i = row_0; i <= row_1 && !bad_peak; i++ )
          for ( int j = col_0; j <= col_1 && !bad_peak; j++ )
            for ( int k = chan_0; k <= chan_1; k++ )
            {
//            log.append( "i,j,k,val = " + i + ", " + j + ", " + k + 
//                        ", " + data_arr[i][j][k] + "\n"); 
              if ( data_arr[i][j][k] > center_value )
              {
//              log.append( "*****TOO BIG i,j,k,val = " + i + ", " + j + 
//                          ", " + k + ", " + data_arr[i][j][k] + "\n"); 
                bad_peak = true;
              }
            }
        if ( bad_peak )
          LogDiscard( log, NOT_LOCAL_MAX, col, row, chan, val, -1 );
      }
                                            // if point misses previous peaks
                                            // and is local max, find it's 
                                            // extent and check if the 
                                            // body overlaps a previous peak
      if ( !bad_peak ) 
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

                                                 // does peak seem
                                                 // ok after finding
                                                 // its extent.
        bad_peak = !peak.set_centroid_and_extent( data_arr, log ); 

        if ( !bad_peak )                         // make sure it doesn't        
        {                                        // overlap a previous peak
          peak_index = 0;
          while ( !bad_peak && peak_index < peaks.size() )
          {
            old_peak = (BasicPeakInfo)peaks.elementAt(peak_index);
            if ( old_peak.overlap( peak ) )
            {
              bad_peak = true;
              LogDiscard(log, BODY_OVERLAPS, col, row, chan, val, peak_index);
            }
            else
              peak_index++;
          }

          if ( !bad_peak )
          {
            peaks.add( peak );
            log.append("*******" + pk_count + ":  ADDED PEAK " + 
                                   peak.col_row_chan_ipk( data_arr ) + "\n");
            pk_count++;
          }
        }
        else
          LogDiscard( log, UNDEFINED_CENTROID, col, row, chan, val, -1 );
      }

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
      log.append( peak_array[i].col_row_chan_ipk( data_arr ) + "\n" );
    }

    return peak_array;
  }

}

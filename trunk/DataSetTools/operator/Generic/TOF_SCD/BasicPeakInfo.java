/* 
 * File: BasicPeakInfo.java
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


/**
 *  This class calculates basic statistics about an individual peak in
 *  an SCD data set.  There are methods to check if a peak overlaps 
 *  a point or with another peak.  The calculation of the statistics is 
 *  out by a call to set_centroid_and_extent().  The typical use of this
 *  class should be as follows:
 *    1. construct a BasicPeakInfo object for a list of points that
 *       are local maxima in an SCD histogram
 *    2. Process the local maxima in order of decreasing magnitude.
 *    3. Call peak.overlap( point ) for each previous peak, and discard
 *       the point if it overlaps a previous peak.  If so, move on to the
 *       next point.
 *    4. If the point does not overlap any previous peak, then call
 *       set_centroid_and_extent(), to determine the mean, standard 
 *       deviation and MANY other statistics about the peak.
 *    5. Call peak.overlap( new_peak ) for each previous peak to see if
 *       the body of this peak overlaps any other peak. If so, move on
 *       to the next point.
 *    6. BasicPeakInfo objects that don't overlap other such objects
 *       can be tested for validity by calling the isValid() method.
 */
public class BasicPeakInfo 
{
  private float row_cent;          // These record the centroid of the peak
  private float col_cent;
  private float chan_cent;

  private float init_row;          // These record the position of the bin 
  private float init_col;          // with the max counts in this peak
  private float init_chan; 

  private float delta_row;         // These record the extents of the peak
  private float delta_col;
  private float delta_chan;

  private int   ipk;               // max count value

  private SliceStats[] slice_info_arr = new SliceStats[5]; 
                                                    // This records more
                                                    // detailed info on each
                                                    // slice of the peak.
  private boolean test_1 = false,
                  test_2 = false, 
                  test_3 = false,
                  test_4 = false,
                  test_5 = false,
                  test_6 = false,
                  test_7 = false,
                  test_8 = false;

  /**
   *  Construct a BasicPeakInfo object, using a specified bin and initial 
   *  extents specified so that the full peak plus and minus extents is 
   *  contained in the data volume.
   *
   *  @param  row    The row number of the bin with max counts for this peak.
   *  @param  col    The row number of the bin with max counts for this peak.
   *  @param  chan   The row number of the bin with max counts for this peak.
   *  @param  d_row  The initial extent of the peak in the row direction.
   *  @param  d_col  The initial extent of the peak in the column direction.
   *  @param  d_chan The initial extent of the peak in the channel direction.
   *  @param  ipk    The intensity at the bin with max intensity.
   */
  public BasicPeakInfo( float row,   float col,   float chan,
                        float d_row, float d_col, float d_chan,
                        int   ipk  )
  {
    row_cent  = row + 0.5f;
    col_cent  = col + 0.5f;
    chan_cent = chan; 

    init_row  = row;
    init_col  = col;
    init_chan = chan; 

    delta_row  = d_row;
    delta_col  = d_col;
    delta_chan = d_chan;

    this.ipk   = ipk;
  }


  /**
   *  Check if the specified row, col and channel are close to this peak.
   *
   *  @param  row   The row to check
   *  @param  col   The column to check
   *  @param  chan  The channel to check
   *
   *  @return true if the specified row, col and channel are within two times
   *               the standard deviation plus one, bin of the peak center.
   */
  public boolean overlap( int row, int col, int chan )
  {
    //
    // NOTE:  We could calculate the straight line distance between
    // the peaks for the  (col,row) components.
    //
    float row_dist  = Math.abs( row + 0.5f - row_cent );
    float col_dist  = Math.abs( col + 0.5f - col_cent );
    float chan_dist = Math.abs( chan+ 0.5f - chan_cent );
    if ( row_dist  <= 2*delta_row + 1 && 
         col_dist  <= 2*delta_col + 1 &&
         chan_dist <= 2*delta_chan + 1 )
      return true;

    return false;
  }


  /**
   *  Check if the specified peak overlaps this peak.
   *
   *  @param other_peak  The peak to check
   *
   *  @return true if the row, col and channel distance of the center of the 
   *               specified peak from the center of this peak is within 
   *               twice the sum of their standard deviations + 1.
   */
  public boolean overlap( BasicPeakInfo other_peak )
  { 
    //
    // NOTE:  We could calculate the straight line distance between
    // the peaks for the  (col,row) components.
    //
    float row_dist  = Math.abs( row_cent  - other_peak.row_cent );
    float col_dist  = Math.abs( col_cent  - other_peak.col_cent );
    float chan_dist = Math.abs( chan_cent - other_peak.chan_cent );

    if ( row_dist  <= 2 * (delta_row  + other_peak.delta_row)  + 1  && 
         col_dist  <= 2 * (delta_col  + other_peak.delta_col)  + 1  && 
         chan_dist <= 2 * (delta_chan + other_peak.delta_chan) + 1   )
      return true; 

    return false;
  }


  /**
   *  Calculate the basic statistics of this peak, based on the data
   *  in the counts array.
   *
   *  @param counts  the overall array of counts for the data that contains
   *                 this peak.
   *
   *  @return True, if the centroid and extent have been set successfully, or
   *          false if the centroid remains undefined.  This will happen if
   *          the estimate of the background is too large relative to the
   *          estimate of the intensity of the peak.
   */
  public boolean set_centroid_and_extent( float        counts[][][],
                                          StringBuffer log           )
  {
    int chan_0 = (int)( chan_cent - 2 );
    int chan_1 = (int)( chan_cent + 2 );

    if ( chan_0 < 0 )
      chan_0 = 0;

    if ( chan_1 > counts[0][0].length - 1 )
      chan_1 = counts[0][0].length - 1;

    log.append("\n" + headerString() + "\n" );
    int index = 0;
    for ( int chan = chan_0; chan <= chan_1; chan++ ) 
    {
      SliceStats info = oneSliceExtent( chan, counts, log );
      if ( info == null )
        log.append("ERROR: null info from oneSliceExtent\n");
      slice_info_arr[ index ] = info; 
      index++;
    }

    float row_std_dev = 0;
    float col_std_dev = 0;

/*  NOTE: As we move away from the peak, the centroid (with or without
          background) seems to drift away from where it appears to belong
          visually.  For now we'll just use the x,y centroid on the peak
          slice, and possibly do a weighted average of three slices for
          the channel.
*/
    row_std_dev = slice_info_arr[2].row_std_dev;
    col_std_dev = slice_info_arr[2].col_std_dev;

    float row_centroid = slice_info_arr[2].rowCentroid();
    float row_move     = Math.abs( row_centroid - row_cent );
    if ( Float.isNaN(row_centroid) )   
      log.append("ERROR: Row Centroid NaN\n");
    else if ( row_move >= 5 )
      log.append("ERROR: Row Centroid moved by 5 pixels or more\n");
    else
      row_cent = row_centroid;

    float col_centroid = slice_info_arr[2].colCentroid();
    float col_move     = Math.abs( col_centroid - col_cent );
    if ( Float.isNaN(col_centroid) )   
      log.append("ERROR: Column Centroid NaN\n");
    else if ( col_move >= 5 )
      log.append("ERROR: Column moved by 5 pixels or more\n");
    else
      col_cent = col_centroid;

/*  NOTE: for now, leave the channel center at the peak max

    float cent_sum   = 0;
    float weight_sum = 0;
    for ( int i = 1; i < slice_info_arr.length - 1; i++ ) 
    {
      weight_sum += slice_info_arr[i].Ipk();
      cent_sum   += slice_info_arr[i].Ipk() * slice_info_arr[i].channel_num;
    }
    chan_cent = cent_sum/weight_sum; 
*/

/*  DON'T DO THIS WEIGHTING NOW:
    for ( int i = 0; i < slice_info_arr.length; i++ )
    {
      row_mean += slice_info_arr[i].row_mean;
      col_mean += slice_info_arr[i].col_mean;

      if ( slice_info_arr[i].row_std_dev > row_std_dev )
        row_std_dev = slice_info_arr[i].row_std_dev;

      if ( slice_info_arr[i].col_std_dev > col_std_dev )
        col_std_dev = slice_info_arr[i].col_std_dev;
    } 
                                                  // TODO: either weight these 
    row_cent = row_mean / slice_info_arr.length;  // or just use the centroid
    col_cent = col_mean / slice_info_arr.length;  // of the middle slice
*/

    delta_row = 2 * row_std_dev;
    delta_col = 2 * col_std_dev;
                                                  // If some slice does not
                                                  // exist, return false since
                                                  // we can't carry out our
                                                  // validity tests.
    for ( int i = 0; i < slice_info_arr.length; i++ )
    {
      if ( slice_info_arr[i] == null )            // THIS SHOULD NOT HAPPEN!!
        return false;
    }    

    log.append("\nDATA FROM ADJACENT SLICES:\n");
    for ( int i = 0; i < slice_info_arr.length; i++ )
      log.append( slice_info_arr[i].toString() );

                                                  // But this can happen....
    if ( Float.isNaN( slice_info_arr[2].rowCentroid() ) ||  
         Float.isNaN( slice_info_arr[2].colCentroid() ) )
      return false;  

// Check the signal to noise ratio on the central slice.

    float MIN_SIG_TO_NOISE = 5;

    test_5 = false;
    float sig_to_noise = slice_info_arr[2].SignalToNoise();
    if ( sig_to_noise > MIN_SIG_TO_NOISE )
    {
      test_5 = true;
      log.append("5:*** GOOD Signal To Noise = " + sig_to_noise + "\n" );
    }
    else
      log.append("5:  FAILED Signal To Noise = " + sig_to_noise + "\n" );

// Check the ratio of the largest value on the center slice, compared to
// the average values on slices two steps before and after the center slice.
// A valid peak should have a substantially larger value on the center slice
// than two slices away.  If the average of the neighboring peaks is 0, the
// count rate must be very low, so accept peaks based on their IPK value.
 
    float MIN_IPK_RATIO = 3;
    float MIN_IPK       = 9;

    test_6 = false;
    float ave_ipk_2 = ( slice_info_arr[0].Ipk() + 
                        slice_info_arr[4].Ipk() ) / 2.0f;
    if ( ave_ipk_2 > 0 )
    { 
      float quality_ratio = slice_info_arr[2].Ipk() / ave_ipk_2;
      if ( quality_ratio > MIN_IPK_RATIO ) 
      { 
        test_6 = true;
        log.append("6:*** GOOD IPK Ratio = " + quality_ratio + "\n" ); 
      }
      else
        log.append("6:  FAILED IPK Ratio = " + quality_ratio + "\n" ); 
    }
    else if ( slice_info_arr[2].Ipk() > MIN_IPK  )
    {
      test_6 = true;
      log.append("6:*** GOOD Modified IPK = " + 
                  slice_info_arr[2].Ipk() + "\n" );
    }
    else  
      log.append("6:  FAILED IPK Ratio undefined" + "\n" );

// Check the ratio of the intensity in the peak, to the background for the
// peak.  A valid peak should have a substantially larger average intensity
// in the peak pixels, as opposed to the background pixels.
 
    test_7 = false;
    float peak_ave = slice_info_arr[2].PeakAve();
    float back_ave = slice_info_arr[2].BackAve();
    if ( back_ave > 0 )
    {
      float quality_ratio = peak_ave/back_ave;
      if ( quality_ratio > 4 )                                   // was 2.0f
      {
        test_7 = true;
        log.append("7:*** GOOD pkave/backave " + quality_ratio + "\n" );
      }
      else
        log.append("7:  FAILED pkave/backave = " + quality_ratio + "\n" );
    }
    else
      log.append("7:  FAILED pkave/backave undefined\n" );

// Check the ratio of the average intensity in the peak(minus background)
// at the center slice compared to the average of the average intensities
// in slices 0 and 4.

    test_8 = false;
    float center_signal    = slice_info_arr[2].Signal();
    float average_2_signal = Math.abs( ( slice_info_arr[0].Signal() +
                                         slice_info_arr[4].Signal() ) / 2 );
    if ( average_2_signal > 0 )
    {
      float quality_ratio = center_signal / average_2_signal;
      if ( quality_ratio > 12 )                          
      {
        test_8 = true;
        log.append("8:*** GOOD signal/2_signals" + quality_ratio + "\n" );
      }
      else
        log.append("8:  FAILED signal/2_signals = " + quality_ratio + "\n" );
    }
    else
    {
      if ( center_signal > 12 )
      {
        test_8 = true;
        log.append("8:*** GOOD center_signal = " + center_signal + "\n"  );
      }
      else
        log.append("8:  FAILED center_signal = " + center_signal + "\n" );
    }

    return true;   // peaks seems to be OK.  we should check this further
  }


  /**
   *  Check if this peak appears to be valid.  NOTE: The return value will
   *  only be useful if the set_centroid_and_extent() method has already
   *  been called.
   *
   *  @return true if the peak has passed some validity tests after calling
   *               set_centroid_and_extent().
   */
  public boolean isValid()
  {
//    return test_2 || test_3 || test_4 || test_5 || test_6;
    return test_6 || test_7 || test_8;
  }


  /**
   *  Get a SliceStats object giving statistical information about the
   *  data values on one slice of this peak.  This method iteratively calls
   *  setSliceStats to get statistics on the peak region and recalculate the
   *  mean and standard deviation.  Each iteration may provide a different
   *  mean and standard deviation.  If there is only "noise" the standard
   *  deviations tend to keep increasing.  If there is a well defined peak,
   *  the iterations settle down quite rapidly.
   *
   *  @param  chan  Specifies a channel near the current peak center
   *                channel.
   */
  private SliceStats oneSliceExtent( int          chan, 
                                     float        counts[][][],
                                     StringBuffer log )
  {
    int NUM_ITERATIONS = 7;

    SliceStats info = new SliceStats( chan,
                                      row_cent,
                                      col_cent,
                                      1,
                                      1  );
    
    SliceStats[] stats = new SliceStats[ NUM_ITERATIONS ];
    stats[0] = info;
    for ( int i = 1; i < NUM_ITERATIONS; i++ )
    {
      stats[i] = setSliceMeanAndStdDev( stats[i-1], counts, log );
//    stats[i] = setCentroidStats( stats[i], counts, log );  // just do once 
    }                                                        // for last 
                                                             // iteration

    stats[NUM_ITERATIONS - 1] = setCentroidStats( stats[NUM_ITERATIONS -1], 
                                                  counts,
                                                  log );
    return stats[NUM_ITERATIONS - 1];
  }


  /**
   *   
   */
  private SliceStats setSliceMeanAndStdDev( SliceStats   info,
                                            float        counts[][][],
                                            StringBuffer log )
  {
    log.append( info.toPartialString() + "\n" );

    SliceStats new_info = new SliceStats( info );

    int chan = new_info.channel_num;

    float row_step = 2;
    if ( info.row_std_dev >= 1 )
      row_step = 2 * info.row_std_dev;

    int row_0  = (int)( info.row_mean - row_step );
    int row_1  = (int)( info.row_mean + row_step );

    float col_step = 2;
    if ( info.col_std_dev >= 1 )
      col_step = 2 * info.col_std_dev;

    int col_0  = (int)( info.col_mean - col_step );
    int col_1  = (int)( info.col_mean + col_step );

    if ( row_0 < 0 )
      row_0 = 0;

    if ( col_0 < 0 )
      col_0 = 0;

    if ( row_1 > counts.length - 1 )
      row_1 = counts.length - 1;

    if ( col_1 > counts[0].length - 1 )
      col_1 = counts[0].length - 1;

    float row_sum    = 0;
    float col_sum    = 0;

    float row_sum_2  = 0;
    float col_sum_2  = 0;

    float row_prod,
          col_prod;

    float value = 0;
    float ipk   = 0;
    float total = 0;                              // Get new estimate of mean
    for ( int row = row_0; row <= row_1; row++ )  // and standard deviation
      for ( int col = col_0; col <= col_1; col++ )
      {
        value = counts[row][col][chan];

        total += value;
        if ( value > ipk )
          ipk = value;

        row_prod = (row + 0.5f) * value;
        col_prod = (col + 0.5f) * value;

        row_sum += row_prod;
        col_sum += col_prod;

        row_sum_2 += row_prod  * (row  + 0.5f);
        col_sum_2 += col_prod  * (col  + 0.5f);
      }

    if ( total != 0 )                     // only update if there were some
    {                                     // counts
      new_info.row_mean = row_sum / total;
      new_info.col_mean = col_sum / total;
    }

    new_info.total_counts = (int)total;
    new_info.ipk          = (int)ipk;

    float drow;                           // rounding errors can make term_1
    float term_1 = row_sum_2/total;       // < term_2, which leads to NaN
    float term_2 = new_info.row_mean * new_info.row_mean;
    if ( term_1 >= term_2 )
      drow = (float)Math.sqrt(term_1 - term_2 );
    else 
      drow = 0;

    float dcol;
    term_1 = col_sum_2/total;
    term_2 = new_info.col_mean * new_info.col_mean;
    if ( term_1 >= term_2 )
      dcol = (float)Math.sqrt(term_1 - term_2 );
    else
      dcol = 0;

    if ( drow >= 1 )                             // clamp this so the std. dev.
      new_info.row_std_dev = drow;               // doesn't go below 1

    if ( dcol >= 1 )
      new_info.col_std_dev = dcol;

    return new_info;
  }


  private SliceStats setCentroidStats( SliceStats   info,
                                       float        counts[][][],
                                       StringBuffer log  )
  {
    SliceStats new_info = new SliceStats( info );
    int chan = new_info.channel_num;
                                                  // reset row, col, 0, 1 from
                                                  // new mean and stand. dev.
                                                  // using larger region
    int row_0  = (int)( new_info.row_mean - new_info.row_std_dev * 2 - 2 );
    int row_1  = (int)( new_info.row_mean + new_info.row_std_dev * 2 + 2 );

    int col_0  = (int)( new_info.col_mean - new_info.col_std_dev * 2 - 2 );
    int col_1  = (int)( new_info.col_mean + new_info.col_std_dev * 2 + 2 );

    if ( row_0 < 0 )
      row_0 = 0;

    if ( col_0 < 0 )
      col_0 = 0;

    if ( row_1 > counts.length - 1 )
      row_1 = counts.length - 1;

    if ( col_1 > counts[0].length - 1 )
      col_1 = counts[0].length - 1;

    float peak_total = 0;
    float back_total = 0;
    int   peak_num   = 0;
    int   back_num   = 0;

    float row_index_sum = 0;
    float col_index_sum = 0;

    float row_value_sum = 0;
    float col_value_sum = 0;

    float dx = new_info.col_std_dev * 2;
    float dy = new_info.row_std_dev * 2;

    float radius_squared = dx * dx + dy * dy;

    float ipk   = 0;
    float total = 0;
    for ( int row = row_0; row <= row_1; row++ )
      for ( int col = col_0; col <= col_1; col++ )
      {
        float value = counts[row][col][chan];
        total += value;
        if ( value > ipk )
          ipk = value;

        float row_dist = Math.abs(((row+0.5f) - new_info.row_mean));
        float col_dist = Math.abs(((col+0.5f) - new_info.col_mean));

        if ( row_dist * row_dist + col_dist * col_dist >= radius_squared )
        {
           back_total += value;
           back_num++;
        }
        else
        {
           peak_total += value;
           peak_num++;

           row_index_sum += row + 0.5f;
           col_index_sum += col + 0.5f;

           row_value_sum += (row + 0.5f) * value;
           col_value_sum += (col + 0.5f) * value;
        }
      }

    new_info.peak_num = peak_num;
    new_info.back_num = back_num;

    new_info.peak_total = peak_total;
    new_info.back_total = back_total;

    new_info.row_index_sum = row_index_sum;
    new_info.col_index_sum = col_index_sum;

    new_info.row_value_sum = row_value_sum;
    new_info.col_value_sum = col_value_sum;

    log.append( new_info.toString() + "\n" );

    return new_info;
  }


  /** 
   *  Get a small local volume of data from around the center of this
   *  peak.
   *  NOTE: the main counts array is indexed as counts[row][col][chan]
   *  but we need to produce an array indexed as bins[chan][row][col];
   *
   *  @param  n_rows   The number of rows around the peak to copy.  This 
   *                   should be an ODD number, such as 21.
   *
   *  @param  n_cols   The number of columns around the peak to copy.  This 
   *                   should be an ODD number such as 21.
   *
   *  @param  n_pages  The number of channels around the peak to copy.  This 
   *                   should be a small ODD number such as 5.
   * 
   *  @param  counts   The full 3D array of data that contains the peak.
   *
   *  @return a  3D array of values from the region around this peak.
   */
  public float[][][] getDisplayArray( int n_rows, 
                                      int n_cols,
                                      int n_pages, 
                                      float[][][] counts )
  {
    float[][][] bins = new float[n_pages][n_rows][n_cols];

    int page_offset = 0;                      // NOTE: If the region requested
    int page_0 = (int)chan_cent - n_pages/2;  // goes off the edge of the 
    int page_1 = page_0 + n_pages - 1;        // data array, we need to just
    if ( page_0 < 0 )                         // just fill out the portion that
    {                                         // overlaps the data, and put it
      page_offset = -page_0;                  // in the display array, offset
      page_0 = 0;                             // so the center row, col, chan
    }                                         // is in the center of the
    if ( page_1 > counts[0][0].length - 1 )   // display array.
      page_1 = counts[0][0].length - 1;
    
    int row_offset = 0;
    int row_0 = (int)row_cent - n_rows/2;
    int row_1 = row_0 + n_rows - 1;
    if ( row_0 < 0 )
    {
      row_offset = -row_0;
      row_0 = 0;
    }
    if ( row_1 > counts.length - 1 )
      row_1 = counts.length - 1;

    int col_offset = 0;
    int col_0 = (int)col_cent - n_cols/2;
    int col_1 = col_0 + n_cols - 1;
    if ( col_0 < 0 )
    {
      col_offset = -col_0;
      col_0 = 0;
    }
    if ( col_1 > counts[0].length - 1 )
      col_1 = counts[0].length - 1;
    
                                             // NOTE: The ImageJPanel has row 
                                             //       one at the top of the 
                                             //       image, so we flip the
                                             //       row numbers.
    int i_page = page_offset;
    for ( int page = page_0; page <= page_1; page++ )
    {
      int i_row = row_offset;
      for ( int row = row_0; row <= row_1; row++ )
      {
        int i_col = col_offset; 
        for ( int col = col_0; col <= col_1; col++ )
        {
          bins[i_page][n_rows - 1 - i_row][i_col] = counts[row][col][page];
          i_col++;
        }
        i_row++;
      }
      i_page++;
    }

    return bins;
  }


  /**
   *  Print some of the count values around the peak.
   */
  public void print_counts( float counts[][][], StringBuffer log )
  {
    int chan_0 = (int)( chan_cent - delta_chan );
    int chan_1 = (int)( chan_cent + delta_chan );

    int row_0  = (int)( row_cent  - delta_row );
    int row_1  = (int)( row_cent  + delta_row );

    int col_0  = (int)( col_cent  - delta_col );
    int col_1  = (int)( col_cent  + delta_col );

    if ( row_0 < 0 )
      row_0 = 0;

    if ( col_0 < 0 )
      col_0 = 0;

    if ( chan_0 < 0 )
      chan_0 = 0;

    if ( row_1 > counts.length - 1 )
      row_1 = counts.length - 1;
  
    if ( col_1 > counts[0].length - 1 )
      col_1 = counts[0].length - 1;

    if ( chan_1 > counts[0][0].length - 1 )
      chan_1 = counts[0][0].length - 1;

    for ( int chan = chan_0; chan <= chan_1; chan++ ) 
    {
       log.append("\n");
       for ( int row = row_0; row <= row_1; row++ ) 
       {
         log.append("\n");
         for ( int col = col_0; col <= col_1; col++ ) 
           log.append( "   " + (int)counts[row][col][chan] + "\n" );
       }
    }
  }



  /**
   *  Get a String giving the column, row, channel, iPeak, and standard
   *  deviations for this peak.
   *
   *  @param counts  The array of counts from which iPeak will be taken.
   */
  public String col_row_chan_ipk( float counts[][][] )
  {
    String result =  String.format( 
           "%8.2f %8.2f %8.2f   %4d  %5.2f  %5.2f  %5.2f",
           (col_cent), 
           (row_cent), 
           (chan_cent),
           (int)counts[(int)init_row][(int)init_col][(int)init_chan],
           delta_col,
           delta_row,
           delta_chan ) ;

     if ( isValid() ) 
       result += "  VALID";

     return result;
  }


  /**
   *  Get the fractional row center for this peak.  NOTE: This should be
   *  valid if set_centroid_and_extent() has been called.
   *
   *  @param the fractional row number of this peak.
   */
  public float getRowCenter()
  {
    return row_cent;
  }

  /**
   *  Get the intensity at the point at which this peak was 
   *  constructed.  NOTE: If smoothed data was used, this will not
   *  be the same as the intensity in the raw data at that point.
   *  
   *  @return the intensity of the data (raw or smoothed) at the 
   *          local maximum where this peak was constructed.
   */
  public float Ipk()
  {
    return ipk;
  }

  /**
   *  Get the fractional column center for this peak.  NOTE: This should be
   *  valid if set_centroid_and_extent() has been called.
   *
   *  @param the fractional column number of this peak.
   */


  public float getColCenter()
  {
    return col_cent;
  }


  /**
   *  Get the fractional column center for this peak.  NOTE: This should be
   *  valid if set_centroid_and_extent() has been called.  Currently, this
   *  is not moved from the center of the initially specified channel.
   *
   *  @param the fractional channel number of this peak.
   */
  public float getChanCenter()
  {
    return chan_cent;
  }

 
  /**
   *  Get titles for columns of information on slices returned by the
   *  SliceStats.toString() method.
   */
  public static String headerString()
  {
     return String.format(
     "%8s  %8s  %4s  %5s  %5s  %5s  %7s  %8s  %8s "+
     "  %3s %8s  %3s %7s %8s %8s",
           "Col",
           "Row",
           "Chan",
           "C Sig",
           "R Sig",
           "IPK",
           "Total",
           "C Ctroid" ,
           "R Ctroid",
           "NPk",
           "PkAve",
           "NBk",
           "BkAve",
           "Sig/Noi",
           "IsigI"
                    );
   }

  // --------------------------------------------------------------------
  /**
   *  This internal class maintains various statistical information about
   *  one slice of the peak, and provides methods for calculating some 
   *  derived information such as the centroid and IsigI.
   */
  public class SliceStats
  {
    int   ipk;                   // set to max intensity on this slice
    int   channel_num;
    int   total_counts = 0;      // total counts inside region on this slice
    float row_mean;
    float col_mean;
    float row_std_dev;
    float col_std_dev;
    int   peak_num   = 0;
    int   back_num   = 0;
    float peak_total = 0;
    float back_total = 0;
    float col_value_sum = 0;
    float row_value_sum = 0;
    float col_index_sum = 0;     // sum of the column numbers in peak region
    float row_index_sum = 0;     // sum of the row numbers in peak region


    /**
     *  Construct a SliceStats object for the specified row, col and channel,
     *  starting with the specified row and col standard deviation estimates.
     *  All values that are not specified by parameters are initialized to 0.
     *
     *  @param chan         The channel specifying the slice through the peak,
     *                      for which various statistics will be estimated. 
     *  @param row_mean     Initial estimate of the row coordinate of the 
     *                      center.
     *  @param col_mean     Initial estimate of the column coordinate of the
     *                      center.
     *  @param row_std_dev  Initial estimate of the standard deviation in 
     *                      the row direction.  (Set to 1 if not known.)
     *  @param col_std_dev  Initial estimate of the standard deviation in 
     *                      the col direction.  (Set to 1 if not known.)
     */
    public SliceStats( int chan,
                       float row_mean,
                       float col_mean,
                       float row_std_dev,
                       float col_std_dev  )
    {
      channel_num  = chan;
      total_counts = 0;
      this.row_mean    = row_mean;
      this.col_mean    = col_mean;
      this.row_std_dev = row_std_dev;
      this.col_std_dev = col_std_dev;
    }


    /**
     *  Copy constructor.
     */
    public SliceStats( SliceStats info )
    {
      this.channel_num   = info.channel_num;
      this.ipk           = info.ipk;
      this.total_counts  = info.total_counts;

      this.row_mean      = info.row_mean;
      this.col_mean      = info.col_mean;

      this.row_std_dev   = info.row_std_dev;
      this.col_std_dev   = info.col_std_dev;

      this.peak_num      = info.peak_num;
      this.back_num      = info.back_num;

      this.peak_total    = info.peak_total;
      this.back_total    = info.back_total;

      this.col_value_sum = info.col_value_sum;
      this.row_value_sum = info.row_value_sum;

      this.col_index_sum = info.col_index_sum;
      this.row_index_sum = info.row_index_sum;
    }

    /**
     *  Get an estimate of the row coordinate of the centroid, 
     *  using peak - background values.
     *
     *  @return An estimate of the row coordinate of the centroid,
     *          if the number of background pixels is positive and the
     *          net peak count (minus background estimate) is positive.
     *          Return Float.NaN other wise.
     */
    public float rowCentroid()
    {
      if ( back_num == 0 )
        return Float.NaN;

      float ave_back = back_total/back_num;

      float weighted_sum   = row_value_sum - row_index_sum * ave_back;
      float net_peak_count = peak_total    - peak_num * ave_back;      

      if ( net_peak_count > 0 )
        return weighted_sum / net_peak_count;
      else
        return Float.NaN;
    }

    
    /**
     *  Get an estimate of the column coordinate of the centroid, 
     *  using peak - background values.
     *
     *  @return An estimate of the column coordinate of the centroid,
     *          if the number of background pixels is positive and the
     *          net peak count (minus background estimate) is positive.
     *          Return Float.NaN other wise.
     */
    public float colCentroid()
    {
      if ( back_num == 0 )
        return Float.NaN;

      float ave_back = back_total/back_num;

      float weighted_sum   = col_value_sum - col_index_sum * ave_back;
      float net_peak_count = peak_total    - peak_num * ave_back;       

      if ( net_peak_count > 0 )
        return weighted_sum / net_peak_count;
      else
        return Float.NaN;
    }


   /**
    *  Get an estimate of the peak intensity (minus background)
    *  divided by the standard deviation of the peak and background.
    *
    *  @return an estimate of I/sigI, if the number of background pixels
    *          is positive, and return 0 otherwise.
    */
   public float IsigI()
   {
     if ( back_num <= 0 )
       return 0;

     float signal = peak_total - peak_num * back_total/back_num;
     float ratio  = (float)peak_num / (float)back_num;

     float sigma_signal = (float)
                           Math.sqrt(peak_total + ratio*ratio*back_total );

     float I_by_sigI = signal / sigma_signal;
     return I_by_sigI;
   }


   /**
    *  Get the average count rate in the peak region of this slice.
    */
   public float PeakAve()
   {
     float peak_ave; 
     if ( peak_num <= 0 )
       peak_ave = 0;
     else
       peak_ave = peak_total/peak_num;

     return peak_ave;
   }


   /**
    *  Get the average count rate in the background region of this slice.
    */
   public float BackAve()
   {
     float back_ave; 
     if ( back_num <= 0 )
       back_ave = 0;
     else
       back_ave = back_total/back_num;

     return back_ave;
   }


   /**
    *  Get the signal to noise ratio computed as:
    *  (peak_ave - back_ave)/back_ave
    */
   public float SignalToNoise()
   {
      float peak_ave = PeakAve();
      float back_ave = BackAve();
      float sig_to_noise;
      if ( back_ave > 0 )
        sig_to_noise = (peak_ave - back_ave)/back_ave;
      else
        sig_to_noise = 0;
      return sig_to_noise;
   }

  
   /**
    *  Get the difference between the average count in the peak
    *  region and the average count in the background region.
    */
   public float Signal()
   {
      return PeakAve() - BackAve();
   }

   /**
    *  Get the maximum count on this slice.
    */
   public int Ipk()
   {
      return ipk;
   }

   /**
    *  Get a string listing the following information about this slice:
    *  column mean
    *  row_mean
    *  channel_num
    *  colCentroid()
    *  rowCentroid()
    *  col_std_dev
    *  row_std_dev
    *  ipk
    *  total_counts
    */
   public String toPartialString()
   {
      return String.format( "%8.3f  %8.3f  %4d  %5.2f  %5.2f  %5d  %7d",
            col_mean,
            row_mean,
            channel_num,
            col_std_dev,
            row_std_dev,
            ipk,
            total_counts  );
    }

   /**
    *  Get a string listing the following information about this slice:
    *  column mean
    *  row_mean
    *  channel_num
    *  ipk
    *  total_counts
    *  colCentroid()
    *  rowCentroid()
    *  col_std_dev
    *  row_std_dev
    *  peak_num
    *  PeakAve()
    *  back_num
    *  BackAve()
    *  SignalToNoise()
    *  IsigI()
    */
   public String toString()
   {
      return String.format(
     "%8.3f  %8.3f  %4d  %5.2f  %5.2f  %5d  %7d  %8.3f  %8.3f "+
     "  %3d %8.2f  %3d %7.2f %8.2f %8.2f\n",
            col_mean,
            row_mean,
            channel_num,
            col_std_dev,
            row_std_dev,
            ipk,
            total_counts,
            colCentroid(),
            rowCentroid(),
            peak_num,
            PeakAve(),
            back_num,
            BackAve(),
            SignalToNoise(),
            IsigI()
                          );
    }

  }

}

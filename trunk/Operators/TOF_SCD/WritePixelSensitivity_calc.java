
package Operators.TOF_SCD;

import java.util.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import EventTools.EventList.*;

/**
 * This class has various methods for calculating and saving or using
 * the sensitivity of regions on the face of an Anger camera.
 */

public class WritePixelSensitivity_calc
{

  /**
   *  Given a two-dimensional array of values, find the average value in
   *  square neighborhoods around each interior point and return these 
   *  average values in a new array of the same size.  An array position 
   *  is considered interior, if it is not within a distance of 
   *  zero_border + half_width from an edge of the array.
   *
   *  @param  sums         The initial array of values for which the average
   *                       value of neighborhoods will be calculated.
   *  @param  zero_border  The number of rows and columns around the edge
   *                       of the detector that do not have any data, but
   *                       are just zero.
   *  @param  half_width   The distance from the center of a square 
   *                       neighborhood to the edge of the neighborhood.
   *                       Each square neighborhood has dimensions
   *                       (2*zero_border + 1)^2.
   **/
  private float[][] averageSums( float[][] sums, 
                                 int       zero_border,
                                 int       half_width )
  {
    int n_rows = sums.length;
    int n_cols = sums[0].length;

    float[][] ave_sums = new float[ n_rows ][ n_cols ];

    int first_row = zero_border + half_width;
    int last_row  = n_rows - 1 - zero_border - half_width;

    int first_col = zero_border + half_width;
    int last_col  = n_cols - 1 - zero_border - half_width;

    int n_pix = (2*half_width + 1) * (2*half_width + 1);
    for ( int row = first_row; row <= last_row; row++ )
      for ( int col = first_col; col <= last_col; col++ )
      {
        float sum = 0;
        for ( int r = row - half_width; r <= row + half_width; r++ )
          for ( int c = col - half_width; c <= col + half_width; c++ )
            sum += sums[r][c];
        ave_sums[row][col] = sum / n_pix;
      }

    return ave_sums;
  }


  /**
   *  Set up the det_max, det_scale_max and scale_fac arrays for the
   *  specified data set number from the specified retriever.
   *
   *  @param  nr           The data retriever for this run
   *  @param  ds_num       The number of the sample DataSet that should
   *                       be loaded and summed..
   *  @param  min_tof      The start of the interval of times-of-flight
   *                       that will be summed.
   *  @param  max_tof      The end of the interval of times-of-flight
   *                       that will be summed.
   *  @param  zero_border  The number of rows and columns around the edge
   *                       of the detector that do not have any data, but
   *                       are just zero.
   *  @param  half_width   The distance from the center of a square 
   *                       neighborhood to the edge of the neighborhood.
   *                       Each square neighborhood has dimensions
   *                       (2*zero_border + 1)^2.
   *
   */
  private float[][] getDetectorAveSums( NexusRetriever nr,
                                        int            ds_num,
                                        float          min_tof,
                                        float          max_tof,
                                        int            zero_border,
                                        int            half_width,
                                        int[]          detector_id,
                                        float[][][]    scale_fac )
  {
    DataSet ds = nr.getDataSet( ds_num );
    // System.out.println("Loaded DataSet " + ds );

    int[] grid_ids = Grid_util.getAreaGridIDs( ds );
    if ( grid_ids.length != 1 )
      throw new IllegalArgumentException( 
           "Each DataSet must correspond to precisely one detector module, "
         + "got: " + grid_ids.length  );

    int det_id = grid_ids[0];

    detector_id[0] = det_id;

    // System.out.println("**** GridID = " + det_id + " ds_num = " + ds_num );

    IDataGrid grid = Grid_util.getAreaGrid( ds, grid_ids[0] );

    int n_rows = grid.num_rows();
    int n_cols = grid.num_cols();

    float[][] sums = new float[n_rows][n_cols];

    for ( int col = 0; col < n_cols; col++ )
      for ( int row = 0; row < n_rows; row++ )
      {
        int index = col * n_rows + row;
        float[] ys = ds.getData_entry( index ).getY_values();
        XScale xscale = ds.getData_entry( index ).getX_scale();

        int first_index = xscale.getI_GLB( min_tof );
        if ( first_index < 0 )
          first_index = 0;

        int last_index = xscale.getI( max_tof );
        if ( last_index >= ys.length )
          last_index = ys.length-1;

        float sum = 0;
        for ( int i = first_index; i <= last_index; i++ )
          sum += ys[i];

        sums[row][col] = sum;
      }

    sums = averageSums( sums, zero_border, half_width );
    return sums;
  }


  private float[][] ConvertSumsToScaleFactors( float[][]      sums,
                                               int            det_id,
                                               float[]        det_max,
                                               float[]        det_scale_max,
                                               float[][][]    scale_fac )
  {
    float max_sum = 0;
    int n_rows = sums.length;
    int n_cols = sums[0].length;
    for ( int row = 0; row < n_rows; row++ )
      for ( int col = 0; col < n_cols; col++ )
        if ( sums[row][col] > max_sum )
          max_sum = sums[row][col];

    for ( int row = 0; row < n_rows; row++ )
      for ( int col = 0; col < n_cols; col++ )
        if ( sums[row][col] > 0 )
          sums[row][col] = max_sum / sums[row][col];
        else
          sums[row][col] = 1f;

    float max_scale = 0;
    for ( int row = 0; row < n_rows; row++ )
      for ( int col = 0; col < n_cols; col++ )
        if ( sums[row][col] > max_scale )
          max_scale = sums[row][col];

    det_max[det_id]       = max_sum;
    det_scale_max[det_id] = max_scale;
    scale_fac[det_id]     = sums;

    return sums;
}


  /**
   *  Approximate the relative sensitivity in each detector from the
   *  specified NeXus file.  This information is recorded for each 
   *  detector in the det_max, det_scale_max and scale_fac arrays.
   *  specified data set number from the specified retriever.
   *
   *  @param  van_nr       The NeXus file retriever for the vanadium file
   *  @param  min_tof      The start of the interval of times-of-flight
   *                       that will be summed.
   *  @param  max_tof      The end of the interval of times-of-flight
   *                       that will be summed.
   *  @param  zero_border  The number of rows and columns around the edge
   *                       of the detector that do not have any data, but
   *                       are just zero.
   *  @param  half_width   The distance from the center of a square 
   *                       neighborhood to the edge of the neighborhood.
   *                       Each square neighborhood has dimensions
   *                       (2*zero_border + 1)^2.
   */
  public void CalculatePixelSensitivity( NexusRetriever van_nr,
                                         float          min_tof,
                                         float          max_tof,
                                         int            zero_border,
                                         int            half_width,
                                         float[]        det_max,
                                         float[]        det_scale_max,
                                         float[][][]    scale_fac )
  {
    int n_data_sets = van_nr.numDataSets();
 
                                         // allow extra space in these arrays
                                         // in case detector ID's are much
                                         // larger than DataSet numbers
                                         // Unused positions in the 3D array
                                         // will be null, so not much space 
                                         // is wasted.
    for ( int i = 1; i < n_data_sets; i++ )
    {
      String[] nx_info = van_nr.getDataSetInfo( i );
      int    ds_type = van_nr.getType( i );
      String bank_name = nx_info[0];
      String id_range  = nx_info[nx_info.length-1];
      if ( ds_type == Retriever.HISTOGRAM_DATA_SET )
      {
        System.out.println( bank_name + " is type " + ds_type + 
                            " id range = " + id_range );

        int[] detector_id = new int[1];

        float[][] vanadium_sums = getDetectorAveSums( van_nr, i,
                                                      min_tof, max_tof, 
                                                      zero_border, half_width,
                                                      detector_id,
                                                      scale_fac );

        ConvertSumsToScaleFactors( vanadium_sums, detector_id[0],
                                   det_max, det_scale_max, scale_fac );
      }
    }
    van_nr.close();
  }


  /**
   *  Approximate the relative sensitivity in each detector from the
   *  specified NeXus file.  This information is recorded for each 
   *  detector in the det_max, det_scale_max and scale_fac arrays.
   *  specified data set number from the specified retriever.
   *
   *  @param  van_nr       The NeXus file retriever for the vanadium file
   *  @param  back_nr      The NeXus file retriever for the background file
   *  @param  min_tof      The start of the interval of times-of-flight
   *                       that will be summed.
   *  @param  max_tof      The end of the interval of times-of-flight
   *                       that will be summed.
   *  @param  zero_border  The number of rows and columns around the edge
   *                       of the detector that do not have any data, but
   *                       are just zero.
   *  @param  half_width   The distance from the center of a square 
   *                       neighborhood to the edge of the neighborhood.
   *                       Each square neighborhood has dimensions
   *                       (2*zero_border + 1)^2.
   */
  public void CalculatePixelSensitivity( NexusRetriever van_nr,
                                         NexusRetriever back_nr,
                                         float          min_tof,
                                         float          max_tof,
                                         int            zero_border,
                                         int            half_width,
                                         float[]        det_max,
                                         float[]        det_scale_max,
                                         float[][][]    scale_fac )
  {
    int n_data_sets = van_nr.numDataSets();
    Vector<Integer> det_ids = new Vector<Integer>();
    int[] detector_id = new int[1];
    int   det_id = 0;
    int   mon_id = 2;
                                     // First get the average sums for vandium
    float[][][] vanadium_sums = new float[ scale_fac.length ][][];
    for ( int i = 1; i < n_data_sets; i++ )
    {
      String[] nx_info = van_nr.getDataSetInfo( i );
      int    ds_type = van_nr.getType( i );
      String bank_name = nx_info[0];
      String id_range  = nx_info[nx_info.length-1];
      if ( ds_type == Retriever.HISTOGRAM_DATA_SET )
      {
        System.out.println( bank_name + " is type " + ds_type +
                            " id range = " + id_range );

        float[][] sums = getDetectorAveSums( van_nr, i,
                                             min_tof, max_tof,
                                             zero_border, half_width,
                                             detector_id,
                                             scale_fac );
        det_id = detector_id[0];
        vanadium_sums[ det_id ] = sums;
        det_ids.add( det_id );
      }
    }
    double van_mon_counts = getMonitorCounts( van_nr, mon_id );

    van_nr.close();

                                   // Next get the average sums for background 
    float[][][] back_sums = new float[ scale_fac.length ][][];
    for ( int i = 1; i < n_data_sets; i++ )
    {
      String[] nx_info = back_nr.getDataSetInfo( i );
      int    ds_type = back_nr.getType( i );
      String bank_name = nx_info[0];
      String id_range  = nx_info[nx_info.length-1];
      if ( ds_type == Retriever.HISTOGRAM_DATA_SET )
      {
        System.out.println( bank_name + " is type " + ds_type +
                            " id range = " + id_range );

        float[][] sums = getDetectorAveSums( back_nr, i,
                                             min_tof, max_tof,
                                             zero_border, half_width,
                                             detector_id,
                                             scale_fac );
        det_id = detector_id[0];
        back_sums[ det_id ] = sums;
      }
    }
    double back_mon_counts = getMonitorCounts( back_nr, mon_id );
    back_nr.close();
                                  // now subtract the background.
                                  // TODO: Should scale by monitor counts
                               
    float scale = (float)( van_mon_counts/back_mon_counts );
    System.out.println("Background to vanadium monitor scale factor = " + 
                        scale );
    for ( int i = 0; i < det_ids.size(); i++ )  
    {
      det_id = (int)det_ids.elementAt(i);
      float[][] van  = vanadium_sums[ det_id ];
      float[][] back = back_sums[ det_id ];
      if ( van.length != back.length )
        throw new IllegalArgumentException( "Different number of rows in " +
                           " vanadium and background for det id " + det_id );
      if ( van[0].length != back[0].length )
        throw new IllegalArgumentException( "Different number of cols in " +
                           " vanadium and background for det id " + det_id );
      int n_rows = van.length;
      int n_cols = van[0].length;

      float[][] net_counts = new float[n_rows][n_cols];
      for ( int row = 0; row < n_rows; row++ )
        for ( int col = 0; col < n_cols; col++ )   
          net_counts[row][col] = van[row][col] - back[row][col] * scale; 

      ConvertSumsToScaleFactors( net_counts, det_id,
                                 det_max, det_scale_max, scale_fac );
    }
  }


  public double getMonitorCounts( NexusRetriever nr, int mon_id )
  {
    if ( nr.getType( 0 ) == Retriever.MONITOR_DATA_SET )
    {
      DataSet ds = nr.getDataSet( 0 );
      Data mon_data = ds.getData_entry_with_id( mon_id );
      if ( mon_data == null )
      {
        System.out.println("Warning: MONITOR WITH ID " + mon_id + " NOT FOUND "
                           + "using normalization factor = 1 ");
        return 1;
      }
      float[] vals = mon_data.getY_values();
      double total = 0;
      for ( int i = 0; i < vals.length; i++ )
        total += vals[i];
      System.out.println(" Total Monitor Count = " + total );
      return total;
    }
    else
    {
      System.out.println("Warning: MONITOR DATA SET NOT FOUND " +
                         "using normalization factor = 1 ");
      return 1;
    }
  }


  /**
   *  Use the calculated scale factors to adjust the integrated intensitiy
   *  and standard deviations of the peaks in a peaks file.
   *
   *  @param  peaks_file     The name of the peaks file whose intensities
   *                         are to be adjusted
   *  @param  adjusted_file  The name of the new peaks file that should be
   *                         written containing the adjusted peak intensities.
    *  @param  zero_border    The number of rows and columns around the edge
   *                         of the detector that do not have any data, but
   *                         are just zero.
   *  @param  half_width     The distance from the center of a square 
   *                         neighborhood to the edge of the neighborhood.
   *                         Each square neighborhood has dimensions
   *                         (2*zero_border + 1)^2.
   */
  public static void AdjustPeaksFile( String        peaks_file,
                                      String        adjusted_file,
                                      float[]       det_max,
                                      float[]       det_scale_max,
                                      float[][][]   scale_fac,
                                      int           zero_border,
                                      int           half_width  )
                      throws Exception
  {
                  // printout some general information for comparing detectors
    for ( int i = 0; i < det_max.length; i++ )
      if ( scale_fac[i] != null )
        System.out.printf(
            "Det = %3d   Max AverageCount = %7.2f  Max Scale = %7.3f\n",
            i,  det_max[i], det_scale_max[i] );

    Vector<Peak_new> peaks = Peak_new_IO.ReadPeaks_new( peaks_file );

    Vector<Peak_new> adjusted_peaks = new Vector<Peak_new>();

    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = peaks.elementAt(i);
      int det_num = peak.detnum();
      int row     = (int)peak.y();
      int col     = (int)peak.x();

      if (det_num >= scale_fac.length || scale_fac[det_num] == null)
        throw new IllegalArgumentException(
          "Invalid detector number in peaks file: " + det_num );

      int n_rows = scale_fac[det_num].length;
      int n_cols = scale_fac[det_num][0].length;

      int first_row = zero_border + half_width;
      int last_row  = n_rows - 1 - zero_border - half_width;

      int first_col = zero_border + half_width;
      int last_col  = n_cols - 1 - zero_border - half_width;

      if ( row >= first_row && row <= last_row &&
           col >= first_col && col <= last_col  )
      {
        float scale = scale_fac[det_num][row][col];
        float inti = peak.inti();
        float sigi = peak.sigi();
        peak.inti( inti * scale );
        peak.sigi( sigi * scale );

        adjusted_peaks.add( peak );
      }
    }

    Peak_new_IO.WritePeaksInSequence( adjusted_file, adjusted_peaks, false );
  }


  /**
   *  Adjust each peak intensity for the relative sensitivity of pixels
   *  in each detector, based on data from the specified NeXus file.  The
   *  NeXus file should contain data from a uniform scatterer, such as 
   *  vanadium.  The average sensitivity of pixels in a square neighborhood
   *  of each interior pixel is calculated.  For all interior pixels, the 
   *  average total count over a square neighborhood of size (2*half-width+1)^2
   *  is first calculated.  Based on these averages, the sensitivity scale 
   *  factor for each pixel with a non-zero average total count is taken to 
   *  be MAX_COUNT/ave_count, where MAX_COUNT is the maximum average total 
   *  count for any interior pixel in the detector, and ave_count is
   *  the average total count for that pixel.  If the ave_count is zero for
   *  a pixel (as it will be for border pixels) the sensitivity scale 
   *  factor is set to 1.  Therefore, this sensitivity scale factor
   *  is >= 1 for all pixels.  The peak intensity for each peak in the 
   *  peaks file is multiplied by the sensitivity scale factor for the 
   *  corresponding pixel in that detector.  The "SIGI" value is also 
   *  multiplied by the same scale factor.
   *
   *  @param  nx_file        The name of the NeXus file containing the
   *                         vanadium data.
   *  @param  peaks_file     The name of the peaks file whose intensities
   *                         are to be adjusted
   *  @param  adjusted_file  The name of the new peaks file that should be
   *                         written containing the adjusted peak intensities.
   *  @param  min_tof        The start of the interval of times-of-flight
   *                         that will be summed.
   *  @param  max_tof        The end of the interval of times-of-flight
   *                         that will be summed.
   *  @param  zero_border    The number of rows and columns around the edge
   *                         of the detector that do not have any data, but
   *                         are just zero.
   *  @param  half_width     The distance from the center of a square 
   *                         neighborhood to the edge of the neighborhood.
   *                         Each square neighborhood has dimensions
   *                         (2*zero_border + 1)^2.
   **/
  public static void AdjustPeaksForPixelSensitivity
                            ( String nx_file,
                              String peaks_file,
                              String adjusted_file, 
                              float  min_tof,
                              float  max_tof,
                              int    zero_border,
                              int    half_width )
                     throws Exception
  {
    WritePixelSensitivity_calc calc = new WritePixelSensitivity_calc();

    NexusRetriever van_nr = new NexusRetriever( nx_file );
    int n_data_sets = van_nr.numDataSets();
                                         // allow extra space in these arrays
                                         // in case detector ID's are much
                                         // larger than DataSet numbers
                                         // Unused positions in the 3D array
                                         // will be null, so not much space 
                                         // is wasted.
    float[]     det_max       = new float[100 * n_data_sets];
    float[]     det_scale_max = new float[100 * n_data_sets];
    float[][][] scale_fac     = new float[100 * n_data_sets][][]; 

    calc.CalculatePixelSensitivity( van_nr, 
                                    min_tof, max_tof, 
                                    zero_border, half_width,
                                    det_max, det_scale_max, scale_fac );

    AdjustPeaksFile( peaks_file, adjusted_file, 
                     det_max, det_scale_max, scale_fac,
                     zero_border, half_width );
  }


  /**
   *  Adjust each peak intensity for the relative sensitivity of pixels
   *  in each detector, based on data from the specified NeXus vanadium file
   *  and the specified NeXus background run file.
   *  The average sensitivity of pixels in a square neighborhood
   *  of each interior pixel is calculated.  For all interior pixels, the 
   *  average total count over a square neighborhood of size (2*half-width+1)^2
   *  is first calculated.  The same calculation is also carried out for the
   *  specified background, and the weighted background averages are subtracted
   *  from the vanadium averages.  The weight factor is based on the monitor
   *  counts.  Based on these averages, the sensitivity scale 
   *  factor for each pixel with a non-zero average total count is taken to 
   *  be MAX_COUNT/ave_count, where MAX_COUNT is the maximum average total 
   *  count for any interior pixel in the detector, and ave_count is
   *  the average total count for that pixel.  If the ave_count is zero for
   *  a pixel (as it will be for border pixels) the sensitivity scale 
   *  factor is set to 1.  Therefore, this sensitivity scale factor
   *  is >= 1 for all pixels.  The peak intensity for each peak in the 
   *  peaks file is multiplied by the sensitivity scale factor for the 
   *  corresponding pixel in that detector.  The "SIGI" value is also 
   *  multiplied by the same scale factor.
   *
   *  @param  van_file       The name of the NeXus file containing the
   *                         vanadium data.
   *  @param  back_file      The name of the NeXus file containing the
   *                         background data.
   *  @param  peaks_file     The name of the peaks file whose intensities
   *                         are to be adjusted.
   *  @param  adjusted_file  The name of the new peaks file that should be
   *                         written containing the adjusted peak intensities.
   *  @param  min_tof        The start of the interval of times-of-flight
   *                         that will be summed.
   *  @param  max_tof        The end of the interval of times-of-flight
   *                         that will be summed.
   *  @param  zero_border    The number of rows and columns around the edge
   *                         of the detector that do not have any data, but
   *                         are just zero.
   *  @param  half_width     The distance from the center of a square 
   *                         neighborhood to the edge of the neighborhood.
   *                         Each square neighborhood has dimensions
   *                         (2*zero_border + 1)^2.
   **/
  public static void AdjustPeaksForPixelSensitivity_2
                            ( String van_file,
                              String back_file,
                              String peaks_file,
                              String adjusted_file,
                              float  min_tof,
                              float  max_tof,
                              int    zero_border,
                              int    half_width )
                     throws Exception
  {
    WritePixelSensitivity_calc calc = new WritePixelSensitivity_calc();

    NexusRetriever van_nr  = new NexusRetriever( van_file );
    NexusRetriever back_nr = new NexusRetriever( back_file );

    int n_data_sets = van_nr.numDataSets();
    if ( back_nr.numDataSets() != n_data_sets )
      throw new IllegalArgumentException("Number of Data sets different in " +
                                          van_file + " and " + back_file );

    float[]     net_max       = new float[100 * n_data_sets];
    float[]     net_scale_max = new float[100 * n_data_sets];
    float[][][] scale_fac     = new float[100 * n_data_sets][][];

    calc.CalculatePixelSensitivity( van_nr,
                                    back_nr,
                                    min_tof, max_tof,
                                    zero_border, half_width,
                                    net_max, net_scale_max, scale_fac );

    AdjustPeaksFile( peaks_file, adjusted_file,
                     net_max, net_scale_max, scale_fac,
                     zero_border, half_width );
  }


  /**
   *  Write a file with per-pixel scale factors based on the summed pixel 
   *  counts in a vanadium run file.  The first file must contain data from 
   *  a uniform scatterer, such as vanadium.  The average sensitivity of 
   *  pixels in a square neighborhood of each interior pixel is calculated.  
   *  For all interior pixels, the average total
   *  count over a square neighborhood of size (2*half-width+1)^2
   *  is first calculated.  Based on these averages, the sensitivity scale 
   *  factor for each pixel with a non-zero average total count is taken to 
   *  be MAX_COUNT/ave_count, where MAX_COUNT is the maximum average total 
   *  count for any interior pixel in the detector, and ave_count is
   *  the average total count for that pixel.  If the ave_count is zero for
   *  a pixel (as it will be for border pixels) the sensitivity scale 
   *  factor is set to 1.  Therefore, this sensitivity scale factor
   *  is >= 1 for all pixels.  The scale factor for each pixel of each
   *  detector is written to a simple binary file in PC (little-endian) 
   *  format.  The first entry in the file is an integer giving the
   *  number of detector modules.  For each module the file contains
   *  (in sequence) three integers giving the bank number, the number of
   *  rows and the number of columns, followed by a list of 32-bit floats
   *  giving the sensitivity scale factor for each pixel, in row major order.
   *
   *  @param  nx_file        The name of the NeXus file containing the
   *                         vanadium data.
   *  @param  sens_file      The name of the sensitivity file to write.
   *  @param  min_tof        The start of the interval of times-of-flight
   *                         that will be summed.
   *  @param  max_tof        The end of the interval of times-of-flight
   *                         that will be summed.
   *  @param  zero_border    The number of rows and columns around the edge
   *                         of the detector that do not have any data, but
   *                         are just zero.
   *  @param  half_width     The distance from the center of a square 
   *                         neighborhood to the edge of the neighborhood.
   *                         Each square neighborhood has dimensions
   *                         (2*zero_border + 1)^2.
   **/
  public static void WritePixelSensitivity ( String nx_file,
                                             String sens_file,
                                             float  min_tof,
                                             float  max_tof,
                                             int    zero_border,
                                             int    half_width )
  {
    WritePixelSensitivity_calc calc = new WritePixelSensitivity_calc();

    NexusRetriever van_nr = new NexusRetriever( nx_file );
    int n_data_sets = van_nr.numDataSets();
                                         // allow extra space in these arrays
                                         // in case detector ID's are much
                                         // larger than DataSet numbers
                                         // Unused positions in the 3D array
                                         // will be null, so not much space 
                                         // is wasted.
    float[]     det_max       = new float[100 * n_data_sets];
    float[]     det_scale_max = new float[100 * n_data_sets];
    float[][][] scale_fac     = new float[100 * n_data_sets][][];

    calc.CalculatePixelSensitivity( van_nr,
                                    min_tof, max_tof,
                                    zero_border, half_width,
                                    det_max, det_scale_max, scale_fac );

    int num_dets = 0;
    for ( int i = 0; i < scale_fac.length; i++ )
      if ( scale_fac[i] != null )
        num_dets++;

    System.out.println("There are " + num_dets + " detectors");

    int index = 0;
    int[] ids = new int[ num_dets ];
    float[][][] factors = new float[ num_dets ][][];
    for ( int i = 0; i < scale_fac.length; i++ )
      if ( scale_fac[i] != null )
      {
        ids[index]     = i;
        factors[index] = scale_fac[i];
        System.out.println("index = " + index + " id = " + ids[index] + 
                           " factors = " + factors[index] );
        index++;
      }

    PixelSensitivityMap sense_map = new PixelSensitivityMap( ids, factors );
    FileUtil.SaveSensitivityFile( sens_file, sense_map );
  }


  /**
   *  Write a file with per-pixel scale factors based on the summed pixel 
   *  counts in a vanadium run file minus the summed pixel counts from a
   *  background run.  The average sensitivity of pixels in a square 
   *  neighborhood of each interior pixel is calculated.  For all interior 
   *  pixels, the average total count over a square neighborhood of size 
   *  (2*half-width+1)^2 is first calculated for both the vanadium run and
   *  the background run.  Based on the difference of these averages, the 
   *  sensitivity scale factor for each pixel with a non-zero average is taken 
   *  to be MAX_COUNT/ave_count, where MAX_COUNT is the maximum average 
   *  count for any interior pixel in the detector, and ave_count is
   *  the average count for that pixel.  If the ave_count is zero for
   *  a pixel (as it will be for border pixels) the sensitivity scale 
   *  factor is set to 1.  Therefore, this sensitivity scale factor
   *  is >= 1 for all pixels.  The scale factor for each pixel of each
   *  detector is written to a simple binary file in PC (little-endian) 
   *  format.  The first entry in the file is an integer giving the
   *  number of detector modules.  For each module the file contains
   *  (in sequence) three integers giving the bank number, the number of
   *  rows and the number of columns, followed by a list of 32-bit floats
   *  giving the sensitivity scale factor for each pixel, in row major order.
   *
   *  @param  van_file       The name of the NeXus file containing the
   *                         vanadium data.
   *  @param  back_file      The name of the NeXus file containing the
   *                         background data.
   *  @param  sens_file      The name of the sensitivity file to write.
   *  @param  min_tof        The start of the interval of times-of-flight
   *                         that will be summed.
   *  @param  max_tof        The end of the interval of times-of-flight
   *                         that will be summed.
   *  @param  zero_border    The number of rows and columns around the edge
   *                         of the detector that do not have any data, but
   *                         are just zero.
   *  @param  half_width     The distance from the center of a square 
   *                         neighborhood to the edge of the neighborhood.
   *                         Each square neighborhood has dimensions
   *                         (2*zero_border + 1)^2.
   **/
  public static void WritePixelSensitivity_2( String van_file,
                                              String back_file,
                                              String sens_file,
                                              float  min_tof,
                                              float  max_tof,
                                              int    zero_border,
                                              int    half_width )
  {
    WritePixelSensitivity_calc calc = new WritePixelSensitivity_calc();

    NexusRetriever van_nr  = new NexusRetriever( van_file );
    NexusRetriever back_nr = new NexusRetriever( back_file );

    int n_data_sets = van_nr.numDataSets();
    if ( back_nr.numDataSets() != n_data_sets )
      throw new IllegalArgumentException("Number of Data sets different in " +
                                          van_file + " and " + back_file );

                                         // allow extra space in these arrays
                                         // in case detector ID's are much
                                         // larger than DataSet numbers
                                         // Unused positions in the 3D array
                                         // will be null, so not much space 
                                         // is wasted.
    float[]     net_max       = new float[100 * n_data_sets];
    float[]     net_scale_max = new float[100 * n_data_sets];
    float[][][] scale_fac     = new float[100 * n_data_sets][][];

    calc.CalculatePixelSensitivity( van_nr,
                                    back_nr,
                                    min_tof, max_tof,
                                    zero_border, half_width,
                                    net_max, net_scale_max, scale_fac );
    int num_dets = 0;
    for ( int i = 0; i < scale_fac.length; i++ )
      if ( scale_fac[i] != null )
        num_dets++;

    System.out.println("There are " + num_dets + " detectors");

    int index = 0;
    int[] ids = new int[ num_dets ];
    float[][][] factors = new float[ num_dets ][][];
    for ( int i = 0; i < scale_fac.length; i++ )
      if ( scale_fac[i] != null )
      {
        ids[index]     = i;
        factors[index] = scale_fac[i];
        System.out.println("index = " + index + " id = " + ids[index] + 
                           " factors = " + factors[index] );
        index++;
      }

    PixelSensitivityMap sense_map = new PixelSensitivityMap( ids, factors );
    FileUtil.SaveSensitivityFile( sens_file, sense_map );
  }


  /**
   * Adjust the measured intensities in the specified DataSet using
   * the pixel sensitivity factors stored in the specified pixel sensitivity
   * map file.  The pixel sensitivity map file must be of the format written
   * by WritePixelSensitivity or WritePixelSensitivity_2. 
   *
   * @param ds        The DataSet whose values are to be adjusted for
   *                  the calculated pixel sensitivities.
   * @param filename  The name of the file containing the pixel sensitivity
   *                  map.
   */
  public static void AdjustDataSetForPixelSensitivity( DataSet ds,
                                                       String filename )
  {
    PixelSensitivityMap sense_map = FileUtil.LoadSensitivityFile( filename );
    int[] bank_ids      = sense_map.getBankIDs();
    float[][][] factors = sense_map.getFactors();

    int[] grid_ids = Grid_util.getAreaGridIDs( ds );    
    for ( int i = 0; i < grid_ids.length; i++ )
    {
      IDataGrid grid = Grid_util.getAreaGrid( ds, grid_ids[i] );
      /*
      System.out.println( "Info for GRID with ID " + grid.ID() + " ......");
      System.out.println( grid );
      System.out.println();
      */
      int index = -1;
      for ( int j = 0; j < bank_ids.length; j++ )
        if ( grid.ID() == bank_ids[j] )
          index = j;

      if ( index == -1 )
        throw new IllegalArgumentException( "NO Sensitivity Info For Bank " +
                                             grid.ID() );
      int n_rows = grid.num_rows();
      int n_cols = grid.num_cols();
      for ( int row = 1; row <= n_rows; row++ )
        for ( int col = 1; col <= n_cols; col++ )
        {
          IData data = grid.getData_entry( row, col ); 
          if ( data == null )
            throw new IllegalArgumentException( "No Data Entry Set for ID = " 
                  + grid.ID() + " row = " + row + " col = " + col );

          float   scale = factors[ index ][ row-1 ][ col-1 ];
          float[] ys    = data.getY_values();
          for ( int j = 0; j < ys.length; j++ )
            ys[j] *= scale;
        }
    }
  }

  /**
   *  Basic main program for testing
   */
  public static void main( String args[] ) throws Exception
  {
    if ( args.length <= 0 )
    {
      System.out.println("YOU MUST ENTER A PEAKS FILE NAME");
      System.exit(0);
    }

    String van_filename  = "/usr2/TOPAZ_VANADIUM/TOPAZ_3023_histo.nxs";
    String back_filename = "/usr2/TOPAZ_VANADIUM/TOPAZ_3163_histo.nxs";
    float  min_tof = 1000;
    float  max_tof = 16000;
    int    zero_border = 15;
    int    half_width  = 7;
    String peaks_filename = args[0];
    String out_peaks_filename = peaks_filename + "van-back_adjusted_15_7";
    AdjustPeaksForPixelSensitivity_2( van_filename,
                                      back_filename,
                                      peaks_filename,
                                      out_peaks_filename,
                                      min_tof,
                                      max_tof,
                                      zero_border,
                                      half_width );
  }

}

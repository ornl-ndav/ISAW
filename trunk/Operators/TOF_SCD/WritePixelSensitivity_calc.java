
package Operators.TOF_SCD;

import java.util.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.operator.Generic.TOF_SCD.*;

public class WritePixelSensitivity_calc
{

  private float[]     det_max       = null;
  private float[]     det_scale_max = null; 
  private float[][][] scale_fac     = null; 


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
   */
  private float[][] getDetectorSums( NexusRetriever nr,
                                     int            ds_num,
                                     float          min_tof,
                                     float          max_tof,
                                     int            zero_border,
                                     int            half_width )
  {
    DataSet ds = nr.getDataSet( ds_num );
    // System.out.println("Loaded DataSet " + ds );

    int[] grid_ids = Grid_util.getAreaGridIDs( ds );
    if ( grid_ids.length != 1 )
      throw new IllegalArgumentException( 
           "Each DataSet must correspond to precisely one detector module, "
         + "got: " + grid_ids.length  );

    int det_id = grid_ids[0];
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
    float max_sum = 0;
    for ( int row = 0; row < n_rows; row++ )
      for ( int col = 0; col < n_cols; col++ )
        if ( sums[row][col] > max_sum )
          max_sum = sums[row][col];

    // System.out.println( "max_sum = " + max_sum );

    for ( int row = 0; row < n_rows; row++ )
      for ( int col = 0; col < n_cols; col++ )
        if ( sums[row][col] != 0 )
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
/*
    for ( int col = 0; col < n_cols; col++ )
      for ( int row = 0; row < n_rows; row++ )
      {
        int index = col * n_rows + row;
        float[] ys = ds.getData_entry( index ).getY_values();
        float scale = sums[row][col];
        for ( int i = 0; i < ys.length; i++ )
          ys[i] = scale;
      }
    new ViewManager( ds, IViewManager.THREE_D );
*/
    return sums;
  }


  /**
   *  Approximate the relative sensitivity in each detector from the
   *  specified NeXus file.  This information is recorded for each 
   *  detector in the det_max, det_scale_max and scale_fac arrays.
   *  specified data set number from the specified retriever.
   *
   *  @param  nx_filename  The name of the NeXus file containing the
   *                       vanadium data.
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
  public void CalculatePixelSensitivity( String nx_filename,
                                         float  min_tof,
                                         float  max_tof,
                                         int    zero_border,
                                         int    half_width )
  {
    NexusRetriever nr = new NexusRetriever( nx_filename );
    int n_data_sets = nr.numDataSets();
 
                                         // allow extra space in these arrays
                                         // in case detector ID's are much
                                         // larger than DataSet numbers
                                         // Unused positions in the 3D array
                                         // will be null, so not much space 
                                         // is wasted.
    det_max       = new float[100 * n_data_sets];
    det_scale_max = new float[100 * n_data_sets];
    scale_fac     = new float[100 * n_data_sets][][]; 

    // System.out.println("File " + nx_filename + 
    //                    " has " + n_data_sets + " DataSets" );
    for ( int i = 1; i < n_data_sets; i++ )
    {
      String[] nx_info = nr.getDataSetInfo( i );
      int    ds_type = nr.getType( i );
      String bank_name = nx_info[0];
      String id_range  = nx_info[nx_info.length-1];
      if ( ds_type == Retriever.HISTOGRAM_DATA_SET )
        System.out.println( bank_name + " is type " + ds_type + 
                            " id range = " + id_range );

      float[][] vanadium_sums = getDetectorSums( nr, i,
                                                 min_tof, max_tof, 
                                                 zero_border, half_width );

      // We should eventually write these to a file
      // and use them in a separate program
    }
    nr.close();
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
   *  @param  nx_filename    The name of the NeXus file containing the
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
    
    calc.CalculatePixelSensitivity( nx_file, 
                                    min_tof, max_tof, 
                                    zero_border, half_width );

                  // printout some general information for comparing detectors
    for ( int i = 0; i < calc.det_max.length; i++ )
      if ( calc.scale_fac[i] != null )
        System.out.printf(
            "Det = %3d   Max AverageCount = %7.2f  Max Scale = %7.3f\n",
            i,  calc.det_max[i], calc.det_scale_max[i] );

    Vector<Peak_new> peaks = Peak_new_IO.ReadPeaks_new( peaks_file );
    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = peaks.elementAt(i);
      int det_num = peak.detnum();
      int row     = (int)peak.y();
      int col     = (int)peak.x();

      if (det_num >= calc.scale_fac.length || calc.scale_fac[det_num] == null)
        throw new IllegalArgumentException(
          "Invalid detector number in peaks file: " + det_num );
        
      float scale = calc.scale_fac[det_num][row][col];
      float inti = peak.inti();
      float sigi = peak.sigi();
      peak.inti( inti * scale );
      peak.sigi( sigi * scale );
    }

    Peak_new_IO.WritePeaks_new( adjusted_file, peaks, false );
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

    String nx_filename  = "/usr2/TOPAZ_VANADIUM/TOPAZ_1750.nxs";
    float  min_tof = 1000;
    float  max_tof = 16000;
    int    zero_border = 9;
    int    half_width  = 5;
    String peaks_filename = args[0];
    String out_peaks_filename = peaks_filename + "_sensitivity_adjusted";
    AdjustPeaksForPixelSensitivity( nx_filename,
                                    peaks_filename,
                                    out_peaks_filename,
                                    min_tof,
                                    max_tof,
                                    zero_border,
                                    half_width );
  }

}

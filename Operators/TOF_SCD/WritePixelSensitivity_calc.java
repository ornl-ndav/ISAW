
package Operators.TOF_SCD;

import java.util.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.operator.Generic.TOF_SCD.*;

public class WritePixelSensitivity_calc
{

  private static float[]     det_max       = null;
  private static float[]     det_scale_max = null; 
  private static float[][][] scale_fac     = null; 

  private static float[][] averageSums( float[][] sums, 
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

  private static float[][] getVanadiumSums( NexusRetriever nr,
                                            int            ds_num,
                                            float          min_tof,
                                            float          max_tof,
                                            int            zero_border,
                                            int            half_width )
  {
    DataSet ds = nr.getDataSet( ds_num );
    System.out.println("Loaded DataSet " + ds );

    int n_rows = 256;       // should get this from the Data grid
    int n_cols = 256; 

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

    System.out.println( "max_sum = " + max_sum );

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

    det_max[ds_num]       = max_sum;
    det_scale_max[ds_num] = max_scale;
    scale_fac[ds_num]     = sums;
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


  public static void WritePixelSensitivity( String nx_filename,
                                            float  min_tof,
                                            float  max_tof,
                                            int    zero_border,
                                            int    half_width,
                                            String out_filename )
  {
    NexusRetriever nr = new NexusRetriever( nx_filename );
    int n_data_sets = nr.numDataSets();

    det_max       = new float[n_data_sets];
    det_scale_max = new float[n_data_sets];
    scale_fac     = new float[n_data_sets][][]; 

    System.out.println("File " + nx_filename + 
                       " has " + n_data_sets + " DataSets" );
    for ( int i = 1; i < n_data_sets; i++ )
//    for ( int i = 5; i < 6; i++ )
    {
      String[] nx_info = nr.getDataSetInfo( i );
      int    ds_type = nr.getType( i );
      String bank_name = nx_info[0];
      String id_range  = nx_info[nx_info.length-1];
      if ( ds_type == Retriever.HISTOGRAM_DATA_SET )
        System.out.println( bank_name + " is type " + ds_type + 
                            " id range = " + id_range );

      float[][] vanadium_sums = getVanadiumSums( nr, i,
                                                 min_tof, max_tof, 
                                                 zero_border, half_width );

      // We should eventually write these to a file
      // and use them in a separate program
    }
    nr.close();
  }


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
    String out_filename = "/usr2/TOPAZ_VANADIUM/TOPAZ_1750_sens_map.txt";
    
    WritePixelSensitivity( nx_filename, 
                           min_tof, max_tof, 
                           zero_border, half_width,
                           out_filename );

    for ( int i = 0; i < det_max.length; i++ )
      System.out.printf(
            "Det = %3d   Max AverageCount = %7.2f  Max Scale = %7.3f\n",
            i,  det_max[i], det_scale_max[i] );

    String peaks_filename = args[0];
    String out_peaks_filename = peaks_filename + "_sensitivity_adjusted";
    Vector<Peak_new> peaks = Peak_new_IO.ReadPeaks_new( peaks_filename );
    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = peaks.elementAt(i);
      int det_num = peak.detnum();
      int row     = (int)peak.y();
      int col     = (int)peak.x();
      float scale = scale_fac[det_num][row][col];
      float inti = peak.inti();
      float sigi = peak.sigi();
      peak.inti( inti * scale );
      peak.sigi( sigi * scale );
    }

    Peak_new_IO.WritePeaks_new( out_peaks_filename, peaks, false );
  }

}

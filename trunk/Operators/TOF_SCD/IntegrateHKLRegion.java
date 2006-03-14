/*
 * File: IntegrateHKLRegion.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * Modified:
 * 
 * $LOG:$
 *
 */
package Operators.TOF_SCD;

import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.trial.*;
import DataSetTools.operator.DataSet.Attribute.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.File.*;
import gov.anl.ipns.Util.Numeric.*;

public class IntegrateHKLRegion
{
  float intensities[][][] = null;
  int   n_steps_h = 0,
        n_steps_k = 0,
        n_steps_l = 0;
  float h_min = 0,
        h_max = 0;
  float k_min = 0, 
        k_max = 0;
  float l_min = 0, 
        l_max = 0;

  /**
   *  Calculate an approximate integral over a rectangluar region in h,k,l.
   *  Each the specified range of h, k and l values is subdivided into the 
   *  specified number of steps in the h, k and l directions.  The three 
   *  dimensional array of division points, (h,k,l) are mapped back to 
   *  points in the raw data and a value is interpolated at each point.
   *  The sum of these values is returned.
   *
   *  @param  ds                 The DataSet for this run
   *  @param  grid               The detector (grid) to be used
   *  @param  orientation_matrix The orientation matrix determining the hkls
   *  @param  min_h              The minimum h value
   *  @param  max_h              The maximum h value
   *  @param  n_h_steps          The number of intervals in the h direction
   *  @param  min_k              The minimum k value
   *  @param  max_k              The maximum k value
   *  @param  n_k_steps          The number of intervals in the k direction
   *  @param  min_l              The minimum l value
   *  @param  max_l              The maximum l value
   *  @param  n_l_steps          The number of intervals in the l direction
   *
   *  @return  An array containing three values in order: the sum of the  
   *           intensities at the points in the specified hkl region;
   *           the number of points that mapped back into the region covered
   *           by the detector, and the number of points that mapped outside
   *           of the region covered by the detector. 
   */
  public float[] IntegrateInterp( DataSet    ds,
                                  IDataGrid  grid,
                                  Tran3D     orientation_matrix,
                                  float      min_h,
                                  float      max_h,
                                  int        n_h_steps,
                                  float      min_k,
                                  float      max_k,
                                  int        n_k_steps,
                                  float      min_l,
                                  float      max_l,
                                  int        n_l_steps )
  {
     float result[] = { 0, 0, 0 };    // result array is all zeros if the 
                                      // hkl region is outside of the region
                                      // measured by the detector.
     //
     // First, find the approximate range of rows, cols and channels 
     // corresponding to the specified region in h,k,l, by mapping points 
     // on the faces of the region of h, k, l, back into row, col, channel.
     //
     intensities = new float[n_h_steps+1][n_k_steps+1][n_l_steps+1];
     h_min = min_h;
     h_max = max_h;
     k_min = min_k;
     k_max = max_k;
     l_min = min_l;
     l_max = max_l;
     n_steps_h = n_h_steps;
     n_steps_k = n_k_steps;
     n_steps_l = n_l_steps;

     float  h_step = (max_h - min_h) / n_h_steps;
     float  k_step = (max_k - min_k) / n_k_steps;
     float  l_step = (max_k - min_k) / n_k_steps;

     int    count = 0;
     int    out_count = 0;
     float  sum = 0;
     float  intensity;
     VecQToTOF transformer = new VecQToTOF( ds, grid );
     Vector3D point        = new Vector3D();
     float    rc_chan[];
     for ( int page = 0; page <= n_h_steps; page++ )
       for ( int row = 0; row <= n_k_steps; row++ )
         for ( int col = 0; col <= n_l_steps; col++ )
         {
            point.set( min_h + h_step * page, 
                       min_k + k_step * row,
                       min_l + l_step * col );
            orientation_matrix.apply_to( point, point );
            intensity = transformer.intensityAtQ( point );
            if ( intensity < 0 )
            {
              intensities[ page ][ row ][ col ] = 0;
              out_count++;
            }
            else
            { 
              intensities[ page ][ row ][ col ] = intensity;
              sum += intensity;
              count++;
            }
         }

     result[0] = sum;
     result[1] = count;
     result[2] = out_count;
     return result;
  }


  /**
   *  Sum the volume of data obtained from the last integration, in the
   *  k and l directions, leaving a function of one variable, h.
   *
   *  @return a DataSet with one Data block, the summed crossections, as
   *            a function of h.
   */
  public DataSet h_profile()
  {
    if ( intensities == null )
    {
      System.out.println("intensities array not set");
      return DataSet.EMPTY_DATA_SET;
    }

    float sums[] = new float[ n_steps_h + 1 ];
    for ( int page = 0; page <= n_steps_h; page++ )
    {
      sums[page] = 0;
      for ( int row = 0; row <= n_steps_k; row++ )
        for ( int col = 0; col <= n_steps_l; col++ )
           sums[page] += intensities[ page ][ row ][ col ]; 
    }

    XScale x_scale = new UniformXScale( h_min, h_max, n_steps_h+1 );
    FunctionTable profile = new FunctionTable( x_scale, sums, 1 );

    String k_interval = "[" + Format.real( k_min, 10, 2 ).trim() + "," +
                              Format.real( k_max, 10, 2 ).trim() + "]";
    String l_interval = "[" + Format.real( l_min, 10, 2 ).trim() + "," +
                              Format.real( l_max, 10, 2 ).trim() + "]";
    String title = "h Profile: k, l summed in " + k_interval +"X"+l_interval;
    DataSetFactory factory = new DataSetFactory( title,
                                              "index", "h",
                                              "intensity", "Summed over k, l" );

    DataSet new_ds = factory.getDataSet();
    new_ds.addData_entry( profile );
    return new_ds;
  }


  /**
   *  Sum the volume of data obtained from the last integration, in the
   *  h and l directions, leaving a function of one variable, k.
   *
   *  @return a DataSet with one Data block, the summed crossections, as
   *            a function of k.
   */
  public DataSet k_profile()
  {
    if ( intensities == null )
    {
      System.out.println("intensities array not set");
      return DataSet.EMPTY_DATA_SET;
    }

    float sums[] = new float[ n_steps_k + 1 ];
    for ( int row = 0; row <= n_steps_k; row++ )
    {
      sums[row] = 0;
      for ( int page = 0; page <= n_steps_h; page++ )
        for ( int col = 0; col <= n_steps_l; col++ )
           sums[row] += intensities[ page ][ row ][ col ]; 
    }

    XScale x_scale = new UniformXScale( k_min, k_max, n_steps_k+1 );
    FunctionTable profile = new FunctionTable( x_scale, sums, 1 );

    String h_interval = "[" + Format.real( h_min, 10, 2 ).trim() + "," +
                              Format.real( h_max, 10, 2 ).trim() + "]";
    String l_interval = "[" + Format.real( l_min, 10, 2 ).trim() + "," +
                              Format.real( l_max, 10, 2 ).trim() + "]";
    String title = "k Profile: h, l summed in " + h_interval +"X"+l_interval;
    DataSetFactory factory = new DataSetFactory( title,
                                              "index", "k",
                                              "intensity", "Summed over h, l" );

    DataSet new_ds = factory.getDataSet();
    new_ds.addData_entry( profile );
    return new_ds;
  }


  /**
   *  Sum the volume of data obtained from the last integration, in the
   *  h and k directions, leaving a function of one variable, l.
   *
   *  @return a DataSet with one Data block, the summed crossections, as
   *            a function of l.
   */
  public DataSet l_profile()
  {
    if ( intensities == null )
    {
      System.out.println("intensities array not set");
      return DataSet.EMPTY_DATA_SET;
    }

    float sums[] = new float[ n_steps_l + 1 ];
    for ( int col = 0; col <= n_steps_l; col++ )
    {
      sums[col] = 0;
      for ( int page = 0; page <= n_steps_h; page++ )
        for ( int row = 0; row <= n_steps_k; row++ )
           sums[col] += intensities[ page ][ row ][ col ];
    }

    XScale x_scale = new UniformXScale( l_min, l_max, n_steps_l+1 );
    FunctionTable profile = new FunctionTable( x_scale, sums, 1 );

    String h_interval = "[" + Format.real( h_min, 10, 2 ).trim() + "," +
                              Format.real( h_max, 10, 2 ).trim() + "]";
    String k_interval = "[" + Format.real( k_min, 10, 2 ).trim() + "," +
                              Format.real( k_max, 10, 2 ).trim() + "]";
    String title = "l Profile: h, k summed in " + h_interval +"X"+k_interval;
    DataSetFactory factory = new DataSetFactory( title,
                                              "index", "l",
                                              "intensity", "Summed over h, k" );

    DataSet new_ds = factory.getDataSet();
    new_ds.addData_entry( profile );
    return new_ds;
  }


  /**
   *  Calculate the total number of counts from all histogram bins that 
   *  came from the specified detector (grid) and specified DataSet,
   *  and that have h,k,l values in the specified ranges.
   *
   *  @param  ds                 The DataSet for this run
   *  @param  grid               The detector (grid) to be used
   *  @param  orientation_matrix The orientation matrix determining the hkls
   *  @param  min_h              The minimum h value
   *  @param  max_h              The maximum h value
   *  @param  min_k              The minimum k value
   *  @param  max_k              The maximum k value
   *  @param  min_l              The minimum l value
   *  @param  max_l              The maximum l value
   *
   *  @return  An array containing three values in order: the sum of the bins 
   *           in the specified hkl region, the number of bins that were 
   *           summed in the data region, and the number of bins in the hkl 
   *           region that were outside of the measured data, in that order.
   */
  public static float[] IntegrateInside( DataSet    ds,
                                         IDataGrid  grid,
                                         Tran3D     orientation_matrix,
                                         float      min_h,
                                         float      max_h,
                                         float      min_k,
                                         float      max_k,
                                         float      min_l,
                                         float      max_l  )
  { 
     float result[] = { 0, 0, 0 };    // result array is all zeros if the 
                                      // hkl region is outside of the region
                                      // measured by the detector.
     //
     // First, find the approximate range of rows, cols and channels 
     // corresponding to the specified region in h,k,l, by mapping points 
     // on the faces of the region of h, k, l, back into row, col, channel.
     //
     float  h_step = 0.1f;
     float  k_step = 0.1f;
     float  l_step = 0.1f;

     float  min_row = Float.POSITIVE_INFINITY;
     float  max_row = Float.NEGATIVE_INFINITY;

     float  min_col = Float.POSITIVE_INFINITY;
     float  max_col = Float.NEGATIVE_INFINITY;

     float  min_chan = Float.POSITIVE_INFINITY;
     float  max_chan = Float.NEGATIVE_INFINITY;

     VecQToTOF transformer = new VecQToTOF( ds, grid );
     Vector3D point        = new Vector3D();
     float    rc_chan[];
     for ( float h = min_h; h <= max_h; h = h + h_step )
       for ( float k = min_k; k <= max_k; k = k + k_step )
         for ( float l = min_l; l <= max_l; l = l + l_step )
         { 
            if ( h <= min_h  ||  h >= max_h  ||      // the point is on a face
                 k <= min_k  ||  k >= max_k  || 
                 l <= min_l  ||  l >= max_l   )
            {
              point.set( h, k, l );
              orientation_matrix.apply_to( point, point );
              rc_chan = transformer.QtoRowColChan( point );
              if ( rc_chan != null )
              {
                if ( rc_chan[0] < min_row )
                  min_row = rc_chan[0];
                else if ( rc_chan[0] > max_row )
                  max_row = rc_chan[0];

                if ( rc_chan[1] < min_col )
                  min_col = rc_chan[1];
                else if ( rc_chan[1] > max_col )
                  max_col = rc_chan[1];

                if ( rc_chan[2] < min_chan )
                  min_chan = rc_chan[2];
                else if ( rc_chan[2] > max_chan )
                  max_chan = rc_chan[2];
              }
            } 
         }

     if ( Float.isInfinite( min_row )  || Float.isInfinite( max_row  ) ||
          Float.isInfinite( min_col )  || Float.isInfinite( max_col  ) ||
          Float.isInfinite( min_chan ) || Float.isInfinite( max_chan )  )
        return result;

     //
     // Now that we have a valid region in real space, step across that
     // region and sum up those bins whose midpoints map into the correct 
     // region in hkl.
     //
     min_row  = (int)Math.floor( min_row  ) - 1;  // include a small border 
     max_row  = (int)Math.ceil ( max_row  ) + 1;  // around the region, just
     min_col  = (int)Math.floor( min_col  ) - 1;  // in case we missed the 
     max_col  = (int)Math.ceil ( max_col  ) + 1;  // extreme points.  This
     min_chan = (int)Math.floor( min_chan ) - 1;  // will also allow us to see
     max_chan = (int)Math.ceil ( max_chan ) + 1;  // if we are near the edge
                                                  // of the data.
     float sum       = 0; 
     int   count     = 0;
     int   out_count = 0;       // count the number of bins that are outside
                                // of the data volume

     Data   d       = ds.getData_entry(0);
     XScale x_scale = d.getX_scale();        // we are assuming all data blocks
                                             // have the same x_scale and 
                                             // initial path.
     float initial_path = AttrUtil.getInitialPath( d );
     if ( Float.isNaN( initial_path ) )
     {
       System.out.println("ERROR: No initial path");    // we should throw
       return result;                                   // an exception
     }

     float  tof,
            h, k, l;
     float  coords[];
     Tran3D inv_goniometer_matrix = transformer.getGoniometerRotationInverse();
     Tran3D inv_orientation_matrix = new Tran3D( orientation_matrix );
     inv_orientation_matrix.invert();

     int n_rows = grid.num_rows();
     int n_cols = grid.num_cols();
     int max_chan_num = x_scale.getNum_x() - 1;

     // System.out.println("Min max row  = " + min_row + ",  " + max_row );
     // System.out.println("Min max col  = " + min_col + ",  " + max_col );
     // System.out.println("Min max chan = " + min_chan + ",  " + max_chan );

     for ( int row = (int)min_row; row <= max_row; row++ ) 
       for ( int col = (int)min_col; col <= max_col; col++ )
         for ( int chan = (int)min_chan; chan <= max_chan; chan++ )
         {
            if ( row  >= 1 && row  <= n_rows &&      // point is in the data
                 col  >= 1 && col  <= n_cols &&      // volume so proceed to
                 chan >= 0 && chan <= max_chan_num ) // check if it's in region
            {
              tof = ( x_scale.getX( chan ) + x_scale.getX( chan + 1 ) ) / 2;
              point = SCD_util.RealToHKL( grid.position( row, col ), 
                                          initial_path,
                                          tof,
                                          inv_goniometer_matrix,
                                          inv_orientation_matrix );
              coords = point.get();
              h = coords[0];
              k = coords[1];
              l = coords[2];
              if ( h >= min_h && h <= max_h &&
                   k >= min_k && k <= max_k &&
                   l >= min_l && l <= max_l  )
              {
                 sum += grid.getData_entry( row, col ).getY_values()[chan];
                 count++;
              } 
            }
            else
              out_count++;     // point is outside the data volume, so we
                               // probably won't get the right integral
         }

     result[0] = sum;          // pack the sum and counts of bins in and out
     result[1] = count;        // of the data region, into the result array
     result[2] = out_count;
     return result;
  }


  /* ------------------------- loadOrientationMatrix -------------------- */
  /**
   *  Get the orientation matrix attribute from the specified file and
   *  multiply all entries by 2 * PI.  
   *
   *  @param  file_name   The name of the *.mat file containing the 
   *                      orientation matrix.
   *
   *  @return A Tran3D object representing the orientation matrix multiplied
   *          by 2 * PI, or null if the matrix couldn't be loaded.
   */

  public static Tran3D loadOrientationMatrix( String file_name )
  {
    float or_mat[][] = new float[3][3];
    try
    {
      TextFileReader tfr = new TextFileReader( file_name );
      or_mat = new float[3][3];

      for ( int col = 0; col < 3; col++ )
        for ( int row = 0; row < 3; row++ )
          or_mat[row][col] = tfr.read_float();
    }
    catch ( Exception e )
    {
      System.out.println("Exception reading orientation matrix is " + e );
      e.printStackTrace();
      return null;
    }

    for ( int i = 0; i < 3; i++ )
      for ( int j = 0; j < 3; j++ )
        or_mat[i][j] *= ((float)Math.PI * 2);

    return new Tran3D( or_mat );
  }


  /* ------------------------- getOrientationMatrix ------------------------ */
  /**
   *  Get the orientation matrix attribute from the specified DataSet and
   *  multiply all entries by 2 * PI.  The Orientation matrix must have been
   *  previously added to the DataSet using the LoadOrientation() operator.
   *
   *  @param  ds   The DataSet with the orientation matrix loaded.
   *
   *  @return A Tran3D object representing the orientation matrix multiplied
   *          by 2 * PI.
   */
  public static Tran3D getOrientationMatrix( DataSet ds )
  {
    float orientation_matrix_arr[][] = AttrUtil.getOrientMatrix( ds );
    if ( orientation_matrix_arr == null )
      return null;

    for ( int i = 0; i < 3; i++ )
      for ( int j = 0; j < 3; j++ )
        orientation_matrix_arr[i][j] *= (float)(2.0*Math.PI);

    return new Tran3D( orientation_matrix_arr );
  }


  /**
   *
   */
  public static Object Integrate( DataSet ds,
                                  int     det_id,
                                  Tran3D  orientation_matrix,
                                  float   min_h,
                                  float   max_h,
                                  float   min_k,
                                  float   max_k,
                                  float   min_l,
                                  float   max_l  )
  {
    IDataGrid grid = Grid_util.getAreaGrid( ds, det_id );
    
    float result[] = IntegrateInside( ds, 
                                      grid, 
                                      orientation_matrix,
                                      min_h, max_h, 
                                      min_k, max_k, 
                                      min_l, max_l );
    return result;
  }


  public static void main( String args[] )
  {
    IntegrateHKLRegion integrator = new IntegrateHKLRegion(); 

    String data_file_name   = "/usr2/SCD_TEST/scd08336.run";
    String or_mat_file_name = "/usr2/SCD_TEST/lsquartz8336.mat";
    String calib_file_name  = "/usr2/SCD_TEST/instprm.dat";

    RunfileRetriever rr = new RunfileRetriever( data_file_name );
    DataSet ds = rr.getDataSet(2);
    IDataGrid grid = Grid_util.getAreaGrid( ds, 17 );

    Tran3D orientation_matrix = loadOrientationMatrix( or_mat_file_name ); 
    System.out.println( "Tran 3D orientation_matrix " + orientation_matrix );

    LoadOrientation op = new LoadOrientation(ds, or_mat_file_name);
    op.getResult();

    orientation_matrix = getOrientationMatrix( ds );
    System.out.println( "From DataSet, AttrUtil: " + orientation_matrix );
    
    LoadSCDCalib load_calib = new LoadSCDCalib( ds,
                                                calib_file_name,
                                                -1,
                                                null );
    load_calib.getResult(); 

    float small_step = 0.1f;
    float large_step = 0.15f;

    for ( int h = -13; h <= -3; h++ )
      for ( int k =   2; k <=  10; k++ )
        for ( int l =   1; l <=   8; l++ )
        {
          float min_h = h - large_step;
          float max_h = h + large_step;
          float min_k = k - large_step;
          float max_k = k + large_step;
          float min_l = l - large_step;
          float max_l = l + large_step;

          float tot_result[] = IntegrateInside( ds,
                                          grid,
                                          orientation_matrix,
                                          min_h, max_h,
                                          min_k, max_k,
                                          min_l, max_l );

          float int_result[] = integrator.IntegrateInterp( ds,
                                          grid,
                                          orientation_matrix,
                                          min_h, max_h, 10,
                                          min_k, max_k, 10,
                                          min_l, max_l, 10 );

         if ( tot_result[0] > 0 && tot_result[2] <= 0 && int_result[2] <= 0 )
         {
            System.out.println();
            System.out.print( ""+ h + " " + k + " " + l + "    " );
            System.out.print( tot_result[0]/tot_result[1] + "    ");
            System.out.println( tot_result[0] + " " + 
                                tot_result[1] + " " + 
                                tot_result[2] );
            System.out.print( ""+ h + " " + k + " " + l + "    " );
            System.out.print( int_result[0]/int_result[1] + "    ");
            System.out.println( int_result[0] + " " + 
                                int_result[1] + " " + 
                                int_result[2] );

         }
        }
/*
    float result[] = integrator.IntegrateInterp( ds,
                                          grid,
                                          orientation_matrix,
                                          -9.5f, -5.5f, 200,
                                          4.95f, 5.05f, 10,
                                          2.95f, 3.05f, 10 );
*/
    float result[] = integrator.IntegrateInterp( ds,
                                          grid,
                                          orientation_matrix,
                                          -6.2f, -5.8f, 40,
                                          4.8f, 5.2f, 40,
                                          2.8f, 3.2f, 40 );
    DataSet h_ds = integrator.h_profile();
    new ViewManager( h_ds, IViewManager.SELECTED_GRAPHS );

    DataSet k_ds = integrator.k_profile();
    new ViewManager( k_ds, IViewManager.SELECTED_GRAPHS );

    DataSet l_ds = integrator.l_profile();
    new ViewManager( l_ds, IViewManager.SELECTED_GRAPHS );
  }
}

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
 * $Log$
 * Revision 1.6  2006/03/16 21:43:11  dennis
 * Added try...catch around steps to write to file, so that
 * if the file can't be written, the operator still completes
 * properly.  An error message is just written to the SharedMessages.
 *
 * Revision 1.5  2006/03/16 17:55:10  dennis
 * Now throws exceptions if the orientation matrix, or
 * DataGrid for the detector, are not present in the DataSet.
 *
 * Revision 1.4  2006/03/16 05:05:13  dennis
 * Added top level static method, IntegrateHKL() to integrate a region
 * specified as a box in HKL.  This method will be used by the
 * Method2OperatorWizard to form an operator with the same name.
 *
 * Revision 1.3  2006/03/15 23:32:40  dennis
 * Added method to write interpolated intensites to an ascii file, with
 * the (h,k,l,x,y,z,xcm,ycm,wl,intensity) values listed one per line.
 * This is in roughly the same format as a peaks file.  The basic
 * operations now just need to be called from a static method, and the
 * static method needs to be wrapped by an operator.
 *
 * Revision 1.2  2006/03/14 05:31:17  dennis
 * Added method to integrate a region in hkl by summing the values
 * at corresponding points in x,y,channel, using interpolation to
 * obtain values at non-integral x,y,channel points.
 * Added methods to find h, k or l profiles of the region by summing
 * on k,l, or h,l or h,k. respectively.
 *
 * Revision 1.1  2006/03/13 22:43:10  dennis
 * Initial version of class with static methods to integrate a
 * region specified in terms of ranges in h, k and l.  Currently,
 * this only has a method to integrate by summing the counts from
 * those voxels whose centers lie within the specified hkl region.
 *
 */
package Operators.TOF_SCD;

import java.io.*;
import java.util.*;

import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.trial.*;
import DataSetTools.operator.DataSet.Attribute.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.File.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.Sys.*;

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

  DataSet   ds     = null;
  IDataGrid grid   = null;
  Tran3D    or_mat = null;

  /**
   *  Calculate an approximate integral over a rectangluar region in h,k,l.
   *  The specified range of h, k and l values is subdivided into the 
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
     // First, record the parameters as state information, so that they
     // can be used when retrieving different derived results. 
     //
     h_min = min_h;
     h_max = max_h;
     k_min = min_k;
     k_max = max_k;
     l_min = min_l;
     l_max = max_l;
     n_steps_h = n_h_steps;
     n_steps_k = n_k_steps;
     n_steps_l = n_l_steps;

     this.ds = ds;
     this.grid = grid;
     this.or_mat = new Tran3D( orientation_matrix );

     //
     // Now step over the specified volume in h,k,l and evaluate the intensity
     // at each of the points.  The intensities are both summed and saved to
     // use for extracting derived information.
     // 
     intensities = new float[n_h_steps+1][n_k_steps+1][n_l_steps+1];
     int    count = 0;
     int    out_count = 0;
     float  sum = 0;
     float  intensity;
     VecQToTOF transformer = new VecQToTOF( ds, grid );
     Vector3D point        = new Vector3D();

     float  h_step = (max_h - min_h) / n_steps_h;
     float  k_step = (max_k - min_k) / n_steps_k;
     float  l_step = (max_l - min_l) / n_steps_l;

     for ( int page = 0; page <= n_steps_h; page++ )
       for ( int row = 0; row <= n_steps_k; row++ )
         for ( int col = 0; col <= n_steps_l; col++ )
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
   *  Write a list of h,k,l,x,y,z,xcm,ycm,wl,intensity values to the
   *  specified file.  An IOException will be thrown if the file
   *  can't be opened and written.
   *  NOTE: The IntegrateInterp() method MUST be called once to construct
   *  the 3D array of interpolated intensities before calling this method.
   *
   *  @param filename  The fully qualified name of the file.
   */
  public boolean WriteFile( String filename ) throws IOException
  {
     if ( intensities == null )
       return false;

     File out_f = new File( filename );
     PrintWriter writer = new PrintWriter( out_f );

     VecQToTOF transformer = new VecQToTOF( ds, grid );
     Vector3D point        = new Vector3D();

     float  h_step = (h_max - h_min) / n_steps_h;
     float  k_step = (k_max - k_min) / n_steps_k;
     float  l_step = (l_max - l_min) / n_steps_l;
     float  h, 
            k, 
            l;

     float  xcm, 
            ycm, 
            wl; 
     float  det_row,
            det_col,
            chan,
            value;

     float rc_chan[];
     float xcm_ycm_wl[];

     writer.print( Format.string( "H", 5, true ) + " " +
                       Format.string( "K", 5, true ) + " " +
                       Format.string( "L", 5, true ) + "  ");
     writer.print( Format.string( "X", 6, true ) + " " +
                       Format.string( "Y", 6, true ) + " " +
                       Format.string( "Z", 6, true ) + "  ");
     writer.print( Format.string( "XCM", 7, true ) + " " +
                       Format.string( "YCM", 7, true ) + " " +
                       Format.string( "WL",  7, true ) + "  ");
     writer.println( Format.string( "Interp Val", 10) );
     

     for ( int page = 0; page <= n_steps_h; page++ )
       for ( int row = 0; row <= n_steps_k; row++ )
         for ( int col = 0; col <= n_steps_l; col++ )
         {
            h = h_min + h_step * page;
            k = k_min + k_step * row;
            l = l_min + l_step * col;
            point.set( h, k, l ); 
            or_mat.apply_to( point, point );
            rc_chan = transformer.QtoRowColChan( point );
            xcm_ycm_wl = transformer.QtoXcmYcmWl( point );

            if ( rc_chan != null )
            {
              det_row = rc_chan[0];
              det_col = rc_chan[1];
              chan    = rc_chan[2];
              xcm     = xcm_ycm_wl[0];
              ycm     = xcm_ycm_wl[1];
              wl      = xcm_ycm_wl[2];
              value   = intensities[page][row][col];
            }
            else
            {
              det_row = -1; 
              det_col = -1;
              chan    = -1;
              xcm     = -1;
              ycm     = -1;
              wl      = -1;
              value   = -1;
            }
            writer.print( Format.real( h, 5, 2 ) + " " +
                          Format.real( k, 5, 2 ) + " " +
                          Format.real( l, 5, 2 ) + "  " );
            writer.print( Format.real( det_col, 6, 2 ) + " " +
                          Format.real( det_row, 6, 2 ) + " " +
                          Format.real( chan,    6, 2 ) + "  " );
            writer.print( Format.real( xcm, 7, 2 ) + " " +
                          Format.real( ycm, 7, 2 ) + " " +
                          Format.real( wl,  7, 4 ) + "  " );
            writer.println( Format.real( value, 10, 3 ) );
         }
     writer.close();
     return true;
  }


  /**
   *  Sum the volume of data obtained from the last integration, in the
   *  k and l directions, leaving a function of one variable, h.
   *  NOTE: The IntegrateInterp() method MUST be called once to construct
   *  the 3D array of interpolated intensities before calling this method.
   *
   *  @return a DataSet with one Data block, the summed crossections, as
   *          a function of h.
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
   *  NOTE: The IntegrateInterp() method MUST be called once to construct
   *  the 3D array of interpolated intensities before calling this method.
   *
   *  @return a DataSet with one Data block, the summed crossections, as
   *          a function of k.
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
   *  NOTE: The IntegrateInterp() method MUST be called once to construct
   *  the 3D array of interpolated intensities before calling this method.
   *
   *  @return a DataSet with one Data block, the summed crossections, as
   *          a function of l.
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
   *  Calculate an approximate integral over a rectangluar region in h,k,l
   *  using the IntegrateInterp() method.  Also, use the h_profile(),
   *  k_profile() and l_profile methods to construct DataSets with functions
   *  of one parameter, h, k or l, by summing over the other two dimensions
   *  in the specified region in h, k and l.  Finally, if a file is specified,
   *  the interpolated intensities will be written to a file, along with
   *  the h,k,l,x,y,z,xcm,ycm,wl values. NOTE: The DataSet ds must have 
   *  the orienatation matrix already loaded using the LoadOrientation()
   *  matrix operator.  In addition, for IPNS runfiles, it should have had 
   *  the calibration information loaded, using the LoadSCDCalib()
   *  operator.
   *
   *  @param  ds                 The DataSet for this run, containing all
   *                             needed attributes, including the orientation
   *                             matrix.
   *  @param  det_id             The id of the detector to use
   *  @param  min_h              The minimum h value
   *  @param  max_h              The maximum h value
   *  @param  n_h_steps          The number of intervals in the h direction
   *  @param  min_k              The minimum k value
   *  @param  max_k              The maximum k value
   *  @param  n_k_steps          The number of intervals in the k direction
   *  @param  min_l              The minimum l value
   *  @param  max_l              The maximum l value
   *  @param  n_l_steps          The number of intervals in the l direction
   *  @param  filename           The name of the file to write the list of
   *                             of interpolated intensities to.  If this
   *                             is blank, or the specified file cannot be 
   *                             created, no file will be written.
   *
   *  @return This method returns a Vector with six pairs of entries.  The
   *  pairs consist of a descriptive String followed by a value.
   *  The first pair of entries are a String name and Float containing 
   *  the sum of all of the interpolated intensities in and on the boundary 
   *  of the specified region.  
   *  The second pair of entries are a String name and an Integer giving 
   *  the number of sample points in the specified region that mapped 
   *  INSIDE of the specified detector's data.  
   *  The third pair of entries are a String name and an Integer giving 
   *  the number of sample points in the specified region that mapped 
   *  OUTSIDE of the specified detector's data.  
   *  The fourth pair of entries are a String name and a DataSet containing 
   *  the "h profile", i.e. sums of the interpolated intensities in the 
   *  directions of k and l, as a function of h. 
   *  The fifth pair of entries are a String name and a DataSet containing 
   *  the "k profile", i.e. sums of the interpolated intensities in the 
   *  directions of h and l, as a function of k. 
   *  The sixth pair of entries are a String name and a DataSet containing 
   *  the "l profile", i.e. sums of the interpolated intensities in the 
   *  directions of h and k, as a function of l. 
   */
  public static Object IntegrateHKL( DataSet ds,
                                     int     det_id,
                                     float   min_h,
                                     float   max_h,
                                     int     n_h_steps,
                                     float   min_k,
                                     float   max_k,
                                     int     n_k_steps,
                                     float   min_l,
                                     float   max_l,
                                     int     n_l_steps,
                                     String  filename  ) throws Exception
  {
    IDataGrid grid = Grid_util.getAreaGrid( ds, det_id );
    if ( grid == null )
      throw new Exception("ERROR: No DataGrid found for detector " + det_id );

    Tran3D orientation_matrix = getOrientationMatrix( ds );
    if ( orientation_matrix == null )
      throw new Exception("ERROR: No Orientation matrix in DataSet");
 
    IntegrateHKLRegion integrator = new IntegrateHKLRegion();

    float result[] = integrator.IntegrateInterp( ds,
                                                 grid,
                                                 orientation_matrix,
                                                 min_h, max_h, n_h_steps,
                                                 min_k, max_k, n_k_steps,
                                                 min_l, max_l, n_l_steps );
    Vector list = new Vector();
    list.add( "Sum of interpolated intensities ");
    list.add( new Float( result[0] ) ); 

    list.add( "Number of points inside detector ");
    list.add( new Integer( (int)result[1] ) );

    list.add( "Number of points outside detector "); 
    list.add( new Integer( (int)result[2] ) ); 

    list.add( "Sums in k, l directions, as function of h ");
    list.add( integrator.h_profile() );
 
    list.add( "Sums in h, l directions, as function of k ");
    list.add( integrator.k_profile() );
 
    list.add( "Sums in h, k directions, as function of l ");
    list.add( integrator.l_profile() );
 
    try 
    {
      if ( filename != null && filename.length() > 0 )
        integrator.WriteFile( filename );
    }
    catch ( Exception e )
    {
      SharedMessages.addmsg("\n ERROR: Could not write intensities to file " 
                            + filename + "\n" );
    }

    return list;
  }


  /**
   *  This does a basic functionality test by loading files and invoking
   *  the integrate(), h_profile(), k_profile(), l_profile() and WriteFile()
   *  methods.
   */
  public static void main( String args[] ) throws Exception
  {
    // 
    //  First load up a DataSet and set the calibration information
    //  and the orientation matrix in the DataSet.  These steps will
    //  be assumed to be done BEFORE the integration is carried out. 
    // 

    String data_file_name   = "/usr2/SCD_TEST/scd08336.run";
    String or_mat_file_name = "/usr2/SCD_TEST/lsquartz8336.mat";
    String calib_file_name  = "/usr2/SCD_TEST/instprm.dat";

    RunfileRetriever rr = new RunfileRetriever( data_file_name );
    DataSet ds = rr.getDataSet(2);

    LoadOrientation op = new LoadOrientation(ds, or_mat_file_name);
    op.getResult();

    LoadSCDCalib load_calib = new LoadSCDCalib( ds,
                                                calib_file_name,
                                                -1,
                                                null );
    load_calib.getResult(); 

    //
    // Now either test the top level IntegrateHKL method, or the individual
    // lower level methods, by integrating one peak. 
    //

    boolean test_top_level = true;
    if ( test_top_level )
    {
    //
    // To check everthing from end to end, call the IntegrateHKL method
    // for one peak, and show the results.
    //
      Vector res_vec = (Vector)IntegrateHKL( ds, 17,
                                            -3.1f, -2.9f, 10,
                                             1.9f,  2.1f, 10,
                                             0.5f,  3.5f, 80,
                                            "Test1.dat"    );
      for ( int i = 0; i < 3; i++ )
      {
        System.out.print  ( res_vec.elementAt( 2*i ) + " " );
        System.out.println( (Number)res_vec.elementAt( 2*i + 1 ) );
      }

      for ( int i = 3; i < 6; i++ )
      {
        System.out.print  ( res_vec.elementAt( 2*i ) + " " );
        System.out.println( res_vec.elementAt( 2*i + 1 ) );
      }

      DataSet h_ds = (DataSet)res_vec.elementAt( 7 );
      new ViewManager( h_ds, IViewManager.SELECTED_GRAPHS );

      DataSet k_ds = (DataSet)res_vec.elementAt( 9 );
      new ViewManager( k_ds, IViewManager.SELECTED_GRAPHS );

      DataSet l_ds = (DataSet)res_vec.elementAt( 11 );
      new ViewManager( l_ds, IViewManager.SELECTED_GRAPHS );
    }
    else
    {
    //
    //  Get the grid and orientation matrix out of the DataSet and
    //  then make an instance of the integrator, integrate a peak,
    //  get and display the profiles and write the file.  NOTE: The
    //  integrate routine must be called once to set the state information
    //  needed for the other methods.
    //
    IDataGrid grid = Grid_util.getAreaGrid( ds, 17 );

    Tran3D orientation_matrix = getOrientationMatrix( ds );

    System.out.println( "From DataSet, AttrUtil: " + orientation_matrix );

    IntegrateHKLRegion integrator = new IntegrateHKLRegion(); 

      float result[] = integrator.IntegrateInterp( ds,
                                                   grid,
                                                   orientation_matrix,
                                                  -3.5f, -2.5f, 40,
                                                   1.5f,  2.5f, 40,
                                                   0.5f,  1.5f, 40 );

      System.out.println( "TOTAL     = " + result[0] );
      System.out.println( "N_COUNTED = " + result[1] );
      System.out.println( "N_MISSED  = " + result[2] );

      DataSet h_ds = integrator.h_profile();
      new ViewManager( h_ds, IViewManager.SELECTED_GRAPHS );

      DataSet k_ds = integrator.k_profile();
      new ViewManager( k_ds, IViewManager.SELECTED_GRAPHS );

      DataSet l_ds = integrator.l_profile();
      new ViewManager( l_ds, IViewManager.SELECTED_GRAPHS );

      integrator.WriteFile( "Test2.dat" );
    }
  }
}

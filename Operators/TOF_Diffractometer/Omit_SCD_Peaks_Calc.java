/* 
 * File: Omit_SCD_Peak_Calc.java
 *
 * Copyright (C) 2010, Dennis Mikkelson
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
 *  $Author:  $
 *  $Date:  $            
 *  $Revision:  $
 */
package Operators.TOF_Diffractometer;

import java.util.*;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.IData;
import DataSetTools.retriever.NexusRetriever;
import DataSetTools.dataset.ReferenceGrid;
import DataSetTools.viewer.ViewManager;
import DataSetTools.viewer.IViewManager;
import DataSetTools.operator.Generic.TOF_SCD.BasicPeakInfo;
import DataSetTools.operator.Generic.TOF_SCD.FindPeaksViaSort;

public class Omit_SCD_Peaks_Calc
{

  /**
   *  Get a Vector of BasicPeakInfo objects for specified DataSet with
   *  data from ONE AREA DETECTOR.  If the DataSet has data from more than 
   *  one detector, peaks will only be found in the detector with the 
   *  smallest ID.  Passing in a DataSet with data from more than one 
   *  detector is inefficient. 
   *  This method just gets a 3D histogram of values from the DataSet
   *  and then calls FindPeaksViaSort.getPeaks() to get the peaks.
   *
   *  @param ds             DataSet with data from ONE AREA DETECTOR.
   *  @param max_num_peaks  Maximum number of peaks to return 
   *  @param threshold      Minimum peak intensity to consider
   *  @param do_smoothing   Pass in as true if data should be smoothed
   *                        before searching for peaks.  
   *                        NOTE: If passed in as true, a copy of the
   *                        data will be made, so more memory will be
   *                        needed.
   *
   *  @return A Vector of BasicPeakInfo objects containing information about
   *          the peaks that are found in the DataSet.
   */
  public static Vector getBasicPeakInfo( DataSet ds,
                                         int     max_num_peaks,
                                         int     threshold,
                                         boolean do_smoothing  )
  {
    if ( ds == null || ds.getNum_entries() < 1 )
      throw new IllegalArgumentException(
                                      "Null or empty DataSet in getPeakInfo"); 

    ReferenceGrid[] ref_grids = ReferenceGrid.MakeDataReferenceGrids( ds );
    if ( ref_grids == null || ref_grids.length < 1 )
      throw new IllegalArgumentException( 
                                    "Could not find area detector in " + ds );

    System.out.println("Got Reference Grids " + ref_grids.length );

    int num_rows = ref_grids[0].num_rows();
    int num_cols = ref_grids[0].num_cols();

    IData data;
    for ( int i = 1; i <= num_rows; i++ )
      for ( int j = 1; j <= num_cols; j++ )
      {
        data = ref_grids[0].getData_entry( i, j );
        if ( data == null )
          throw new IllegalArgumentException(
            "Missing Data entry " + i + ", " + j + " from detector " +
            ref_grids[0].ID() + " in getPeakInfo" );
      }

    data = ref_grids[0].getData_entry( 1, 1 );
    int num_chan = data.getY_values().length;

    float[][][]  raw_data = new float[num_rows][num_cols][];
    for ( int i = 0; i < num_rows; i++ )
      for ( int j = 0; j < num_cols; j++ )
      {
        data = ref_grids[0].getData_entry( i+1, j+1 );
        raw_data[i][j] = data.getCopyOfY_values();
      }

    int[] row_list = new int[num_rows];
    for ( int i = 0; i < num_cols; i++ )
      row_list[i] = i + 1;

    int[] col_list = new int[num_cols];
    for ( int j = 0; j < num_cols; j++ )
      col_list[j] = j + 1;

    int          min_chan   = 0;                    // use all channels
    int          max_chan   = num_chan;
    int[]        histogram  = new int[10000];
    StringBuffer log_buffer = new StringBuffer();
    BasicPeakInfo[] peaks = FindPeaksViaSort.getPeaks( raw_data,
                                                       do_smoothing,
                                                       max_num_peaks,
                                                       threshold,
                                                       row_list,
                                                       col_list,
                                                       min_chan,
                                                       max_chan,
                                                       histogram,
                                                       log_buffer );

    Vector vector = new Vector();
    for ( int i = 0; i < peaks.length; i++ )
      vector.add( peaks[i] );

    return vector;
  }


  /**
   *  Remove all spectra in the specified DataSet that are at row,col positions
   *  affected by the specified peaks.  The DataSet must only contain data
   *  from one area detector, and that must be the same as the detector in
   *  which the specified peaks were found.
   *
   *  Note: This method uses the selection flags to efficiently remove 
   *  many Data blocks from the DataSet.  The selection flags of the 
   *  remaining Data blocks will all be cleared.  
   *
   *  @param  ds     DataSet with data from one detector.
   *  @param  peaks  Vector of BasicPeakInfo objects containing list of peaks
   *                 found in the DataSet.
   */
  public static void OmitSpectraWithPeaks( DataSet ds, Vector peaks )
  {
    ReferenceGrid[] ref_grids = ReferenceGrid.MakeDataReferenceGrids( ds );
    if ( ref_grids == null || ref_grids.length < 1 )
      throw new IllegalArgumentException(
                                    "Could not find area detector in " + ds );

                                    // select the Data blocks near peak
    ds.clearSelections();
    for ( int i = 0; i < peaks.size(); i++ )
    {
      BasicPeakInfo peak = (BasicPeakInfo)peaks.elementAt(i);
      float row_center = 1 + peak.getRowCentroid();
      float col_center = 1 + peak.getColCentroid();
      float row_sigma  = peak.RowSigma();
      float col_sigma  = peak.ColSigma();

      int min_row = Math.max( 1, (int)(row_center - row_sigma * 2) );
      int min_col = Math.max( 1, (int)(col_center - col_sigma * 2) );
      
      int num_rows = ref_grids[0].num_rows();
      int num_cols = ref_grids[0].num_cols();

      int max_row = Math.min( num_rows, (int)(row_center + row_sigma * 2) );
      int max_col = Math.min( num_cols, (int)(col_center + col_sigma * 2) );
      
      for ( int row = min_row; row <= max_row; row++ )
        for ( int col = min_col; col <= max_col; col++ )
        {
          IData data = ref_grids[0].getData_entry( row, col );
          if ( data != null )
            data.setSelected(true);
        }
    }
    
    ds.removeSelected( true );
  }


  public static void main( String[] args )
  {
    String filename = "/usr2/SNAP_7/SNAP_2963.nxs";
    NexusRetriever nr = new NexusRetriever( filename );
    DataSet ds = nr.getDataSet( 5 );
    
    System.out.println("Loaded DataSet " + ds );
    System.out.println("DataSet has " + ds.getNum_entries() + " entries." );

    Vector peaks = getBasicPeakInfo( ds, 100, 0, true );
    for ( int i = 0; i < peaks.size(); i++ )
    {
      BasicPeakInfo one_peak = (BasicPeakInfo)peaks.elementAt(i);
      System.out.println( one_peak );
    }

    OmitSpectraWithPeaks( ds, peaks );

    System.out.println("After omitting " + peaks.size() + " Peaks " );
    System.out.println("DataSet has " + ds.getNum_entries() + " entries." );

    new ViewManager( ds, IViewManager.THREE_D );
  }

}

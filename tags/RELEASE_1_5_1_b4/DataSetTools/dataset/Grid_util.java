/*
 * File:  Grid_util.java
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2003/08/05 15:02:40  dennis
 *  Removed debug print.
 *
 *  Revision 1.1  2003/07/15 21:50:40  dennis
 *  Initial version of utilities for working with DataSets and DataGrids.
 *  Contains static methods:
 *
 *    getAreaGridIDs( ds )
 *    getAreaGrid( ds, det_id )
 *    setEffectivePositions( ds, det_id )
 *
 *  to get list of area detector IDs from a DataSet, get an IDataGrid for
 *  a particular detector ID from a DataSet and to set the effective positions
 *  for the Data blocks of a detector to the position values from the
 *  IDataGrid.
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import java.util.*;
import DataSetTools.math.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;


/** 
 *  This class has static utility methods for manipulating DataGrids and
 *  DataSets.
 */

public class Grid_util
{
  private Grid_util()           // Don't let anyone instantiate this class
  {
  }

  /**
   *  Set the effective position (DETECTOR_POS) attribute of the Data 
   *  blocks for the specified area detector DataGrid, from the DataGrid 
   *  pixel positions.
   *
   *  @param  ds      The DataSet for which some of the Data blocks will
   *                  have positions set from the specified grid.  NOTE:
   *                  if the DataSet contains Data blocks from several
   *                  area detectors, this method must be called once for
   *                  each area detector, if positions are to be set for
   *                  all of the aread detetors' Data.
   *  @param  det_id  The id of the area detector whose pixel position 
   *                  information is to be copied to the effective position 
   *                  attribute.
   *
   * @return True, if the positions were changed and false if there was
   *         a problem setting the positions.
   */
  public static boolean setEffectivePositions( DataSet ds, int det_id )
  {
     IDataGrid grid = getAreaGrid( ds, det_id );
     if ( grid == null )
     {
       System.out.println("Failed to get specified grid, # " + det_id +
                          " in Grid_util.setEffectivePositions ");
       return false;
     }

     if ( !grid.isData_entered() )
       if ( !grid.setData_entries( ds ) )
       {
         System.out.println("Data references NOT SET in " +
                            "Grid_util.setEffectivePositions ");
         return false;
       }

     Data d;
     for ( int row = 1; row <= grid.num_rows(); row++ )
       for ( int col = 1; col <= grid.num_cols(); col++ )
       {
         d = grid.getData_entry( row, col );
         if ( d != null )
         {
           DetectorPosition pos = new DetectorPosition(grid.position(row,col));
           d.setAttribute( new DetPosAttribute(Attribute.DETECTOR_POS, pos) );
         }
       }

     return true;
   }

  /**
   *  Get a list of all of the IDs of the all of the IDataGrids for 
   *  area detectors, from a DataSet.
   *
   *  @param ds The DataSet to look through to find the area detector
   *            grids.
   *
   *  @return  An array listing the IDs of the area detector gris in the
   *           specified DataSet.  This array will have length 0, if there
   *           are no area detectors.
   */
  public static int[] getAreaGridIDs( DataSet ds )
  {
    if ( ds == null || ds.getNum_entries() <= 0 )
      return new int[0];

    Hashtable  area_grids = new Hashtable();
    Data          d                        = null;
    IDataGrid     grid                     = null;
    PixelInfoList pil;
    Attribute     attr;
    int           id;

    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
       d = ds.getData_entry( i );
       attr = d.getAttribute( Attribute.PIXEL_INFO_LIST );
       if ( attr != null && attr instanceof PixelInfoListAttribute )
       {
         pil  = (PixelInfoList)attr.getValue();
         grid = pil.pixel(0).DataGrid();
         id = grid.ID();
         if ( grid.num_rows() > 1 && grid.num_cols() > 1 )
         {
           area_grids.put( new Integer( id ), new Integer( id ) );
                                                               // skip the
           i += grid.num_rows()*grid.num_cols() - 1;           // other pixels
         }                                                     // in this grid
       }
    }

   if ( area_grids.isEmpty() )
     return new int[0];
   else
   {
     int id_list[] = new int[ area_grids.size() ];
     int index     = 0;
     Enumeration e = area_grids.elements();
     while ( e.hasMoreElements() )
     {
       Integer ID = (Integer)e.nextElement();
       id_list[index] = ID.intValue();
       index++;
     }
     Arrays.sort( id_list );
     return id_list;
   }
  }


  /**
   *  Get an IDataGrid for an area detector, specified by detector ID,
   *  from a DataSet.
   *
   *  @param ds        The DataSet to look through to find the IDataGrid
   *  @param det_id    The ID of the IDataGrid, that is, the detector ID
   *                   for the grid.
   *
   *  @return  If the specified area detector DataGrid is found, a reference
   *           to it will be returned.  If it is not found, null will be
   *           returned.
   */
  public static IDataGrid getAreaGrid( DataSet ds, int det_id )
  {
    if ( ds == null || ds.getNum_entries() <= 0 )
      return null;

    int           num_area_detectors_found = 0;
    int           n_data                   = ds.getNum_entries();
    Data          d                        = null;
    IDataGrid     grid                     = null;
    PixelInfoList pil;
    Attribute     attr;
    boolean       area_detector_found  = false;
    boolean       right_grid_found     = false;
    int           data_index = 0;
    while ( !right_grid_found && data_index < n_data )
    {
      while ( !area_detector_found && data_index < n_data )
      {
        d = ds.getData_entry( data_index );
        attr = d.getAttribute( Attribute.PIXEL_INFO_LIST );
        if ( attr != null && attr instanceof PixelInfoListAttribute )
        {
          pil  = (PixelInfoList)attr.getValue();
          grid = pil.pixel(0).DataGrid();
          if ( grid.num_rows() > 1 && grid.num_cols() > 1 )
          {
            area_detector_found = true;
            num_area_detectors_found++;
            if ( det_id == grid.ID() )
              right_grid_found = true;
                                                                // skip the
            data_index += grid.num_rows()*grid.num_cols() - 1;  // other pixels
          }                                                     // in this grid
        }
        else
        {
          System.out.println(
                     "ERROR: need PixelInfoList attribute in getAreaGrid");
          return null;
        }
        data_index++;
      }
      if ( !right_grid_found )
        area_detector_found = false;    // start looking for the next area det
    }

    if ( !right_grid_found )
    {
      System.out.println("ERROR:Didn't find area det#" + det_id +
                         " in getAreaGrid");
      return null;
    }
    return grid;
  }

  /**
   *
   *  Basic main program for testing... shows the DataGrids from a DataSet
   *  if the runfile is specified on the command line.
   *
   */
  public static void main( String args[] )
  {
    RunfileRetriever rr = new RunfileRetriever( args[0] );
    DataSet ds = rr.getFirstDataSet( Retriever.HISTOGRAM_DATA_SET);
    int area_dets[] = getAreaGridIDs( ds );

    if ( area_dets.length == 0 )
      System.out.println("NO AREA DETECTORS IN " + args[0] );

    for ( int i = 0; i < area_dets.length; i++ )
      System.out.println("Found ID " + area_dets[i] );

    for ( int i = 0; i < area_dets.length; i++ )
    {
      IDataGrid grid = getAreaGrid( ds, area_dets[i] );
      System.out.println( "Info for GRID with ID " + grid.ID() + " ......");
      System.out.println( grid );
      System.out.println();

      setEffectivePositions( ds, area_dets[i] );
    }
    ViewManager vm = new ViewManager( ds, ViewManager.IMAGE );
  }

}

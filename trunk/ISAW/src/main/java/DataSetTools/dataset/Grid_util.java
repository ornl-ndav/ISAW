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
 *  Revision 1.9  2006/02/20 23:07:24  dennis
 *  Added convenience method, getAllDataGrids( ds ), to return a
 *  Hashtable of all detector grids in the DataSet, whether they
 *  are single tubes, LPSDs or area detectors.
 *
 *  Revision 1.8  2006/02/13 03:26:06  dennis
 *  Added convenience method getAreaGridByIndex() to get the N_th
 *  area detector grid in a DataSet.
 *
 *  Revision 1.7  2005/06/14 23:15:50  dennis
 *  Minor clarification of javadocs.
 *
 *  Revision 1.6  2005/06/14 15:31:40  dennis
 *  Clarified javadocs indicating what conditions need to be satisfied
 *  by the DataSet for these utilities to work properly.
 *
 *  Revision 1.5  2004/05/10 20:42:20  dennis
 *  Test program now just instantiates a ViewManager to diplay
 *  calculated DataSet, rather than keeping a reference to it.
 *  This removes an Eclipse warning about a local variable that is
 *  not read.
 *
 *  Revision 1.4  2004/03/15 06:10:37  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.3  2004/03/15 03:28:07  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
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

import gov.anl.ipns.MathTools.Geometry.*;

import java.util.*;

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
   *  pixel positions.  The DataGrid for the detector MUST have previously
   *  had references to the corresponding Data blocks set.
   *
   *  @param  ds      The DataSet for which some of the Data blocks will
   *                  have positions set from the specified grid.  NOTE:
   *                  if the DataSet contains Data blocks from several
   *                  area detectors, this method must be called once for
   *                  each area detector, if positions are to be set for
   *                  all of the area detetors' Data.
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
   *  Get a Hashtable of all of the IDataGrid objects of all dimensions for the
   *  entries in a DataSet.  Simple detectors, LPSDs and area detectors will
   *  all be returned if present in the pixel lists of the Data blocks.
   *  Two IDataGrid objects will be considered equal if their grid IDs are
   *  equal.  If several grid objects have the same ID, only one of the
   *  grid objects will be returned in the list of IDataGrids.  (It is NOT
   *  a good idea to have two IDataGrids with the same ID in the same
   *  DataSet.)
   *
   *  @param ds The DataSet to look through to find the detector grids.
   *
   *  @return  An Hastable of detector grids, with the detector IDs as keys.
   *           This hashtable be empty, if there are no detectors.
   */
  public static Hashtable getAllDataGrids( DataSet ds )
  {
    if ( ds == null || ds.getNum_entries() <= 0 )
      return new Hashtable();

    Hashtable  grids = new Hashtable();
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
         grids.put( new Integer( id ), grid );
       }
    }

    return grids;
  }


  /**
   *  Get a list of all of the IDs of the all of the IDataGrids for area
   *  detectors, from a DataSet, assuming that all of the Data blocks for 
   *  each detector are present and are in consecutive positions in the 
   *  DataSet.  This will be the case when a DataSet has just been loaded 
   *  from an IPNS runfile, but may NOT be the case if Data blocks have 
   *  been removed, inserted or reordered.
   *
   *  @param ds The DataSet to look through to find the area detector
   *            grids.
   *
   *  @return  An array listing the IDs of the area detector grids in the
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
   *  from a DataSet, assuming that all of the Data blocks for each detector
   *  are present and are in consecutive positions in the DataSet.  This will
   *  be the case when a DataSet has just been loaded from an IPNS runfile,
   *  but may NOT be the case if Data blocks have been removed, inserted
   *  or reordered.
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

    int           n_data = ds.getNum_entries();
    Data          d      = null;
    IDataGrid     grid   = null;
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
                         " in Grid_util.getAreaGrid()");
      return null;
    }
    return grid;
  }


  /**
   *  Get an IDataGrid for an area detector, specified by by the it's position 
   *  in the list of area detectors, numbered 1,2,3,...etc.
   *  This assumes that all of the Data blocks for each detector
   *  are present and are in consecutive positions in the DataSet.  This will
   *  be the case when a DataSet has just been loaded from an IPNS runfile,
   *  but may NOT be the case if Data blocks have been removed, inserted
   *  or reordered.
   *
   *  @param ds        The DataSet to look through to find the IDataGrid
   *  @param det_index The index of the IDataGrid, 1, 2, 3 etc, in the
   *                   list of area detectors in the DataSet.
   *
   *  @return  If the specified area detector DataGrid is found, a reference
   *           to it will be returned.  If it is not found, null will be
   *           returned.
   */
  public static IDataGrid getAreaGridByIndex( DataSet ds, int det_index )
  {
    if ( ds == null || ds.getNum_entries() <= 0 )
      return null;

    int n_data = ds.getNum_entries();
    int num_area_detectors_found = 0;

    Data          d    = null;
    PixelInfoList pil  = null;
    IDataGrid     grid = null;
    Attribute attr;
    boolean area_detector_found  = false;
    boolean right_detector_found = false;
    int data_index = 1;
    while ( !right_detector_found && data_index < n_data )
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
            if ( det_index == num_area_detectors_found )
              right_detector_found = true;
                                                              // skip the 
            data_index += grid.num_rows() * grid.num_cols();  // other pixels
          }                                                   // in this grid
        }
        else
        {
          System.out.println(
             "ERROR: need PixelInfoList attribute in VecQToTOF constructor");
          return null;
        }
        data_index++;
      }
      if ( !right_detector_found )
        area_detector_found = false;    // start looking for the next area det
    }

    if ( !right_detector_found )
    {
      System.out.println("Didn't find ith area detector, i = " + det_index +
                         "in Grid_util.getAreaGridByIndex()" );
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
    new ViewManager( ds, ViewManager.IMAGE );

    Hashtable grids = getAllDataGrids( ds );
    Enumeration e = grids.elements();
    while ( e.hasMoreElements() )
    {
      IDataGrid grid = (IDataGrid)e.nextElement();
      System.out.println( grid );
    }
  }

}

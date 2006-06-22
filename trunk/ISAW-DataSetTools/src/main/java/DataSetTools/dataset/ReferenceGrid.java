/*
 * File:  ReferenceGrid.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * number DMR-0426797.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * 
 *  $Log$
 *  Revision 1.1  2005/07/11 20:45:14  dennis
 *  Initial version of ReferenceGrid class.  This class maintains a
 *  list of references to Data blocks, corresponding to one detetor.
 *  This allows a program to step across the Data blocks that came
 *  from one detector.  This currently duplicates ONE of the
 *  capabilities of a DataGrid.  However, for conceptual clarity
 *  and long term maintainablity, it is better to factor out the
 *  references from the the DataGrid.  For future work, the DataGrid
 *  should only maintain the geometric information about the detector.
 *  The task of maintaining references to the Data blocks for that
 *  detector will be handled separately, by a ReferenceGrid.
 *
 */

package  DataSetTools.dataset;

import java.util.*;

import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import Operators.Generic.Load.*;

/**
 *   A ReferenceGrid is a two dimensional array (with indicies starting at 1)
 * containing references to Data blocks that is intended to hold references 
 * to all of the Data blocks for particular detector.  
 *   If the detector is an area detector, it will actually be two dimensional.
 * If the detector is an LPSD, there will be only one column and multiple rows
 * or one row and multiple columns.  If the detector is a single tube, there
 * will only be one row and one column.  NOTE: The information here is 
 * intended only for short term use.  That is, if all Data blocks in a DataSet
 * need to be accessed by detector, the appropriate ReferenceGrids can be
 * constructed from the DataSet using methods in this class.  However, the
 * information in the ReferenceGrid will not be updated when Data blocks
 * are added to, or removed from the DataSet.  IF the DataSet is altered,
 * the ReferenceGrid(s) will need to be reconstructed.
 */  

public class ReferenceGrid 
{
  private int       id;

  private int       n_rows = 1,      // number of rows and columns that the
                    n_cols = 1;      // grid is divided into

  private Data      data[][] = null;

  /**
   *  Construct a new ReferenceGrid with the specified number of rows and
   *  columns, but with all references null.
   *
   *  @param  id         Unique integer ID to be used for this data grid
   *
   *  @param  n_rows     The number of rows of pixels in the whole grid. 
   *                     This must be at least 1. 
   *
   *  @param  n_cols     The number of columns of pixels in the whole grid. 
   *                     This must be at least 1. 
   */
  public ReferenceGrid( int id, int n_rows, int n_cols )
  {
    this.id       = id;

    if ( n_cols > 0 )
      this.n_cols = n_cols;

    if ( n_rows > 0 )
      this.n_rows = n_rows;
  }


  /**
   *  Make a new ReferenceGrid, copying the dimensions, ID and reference
   *  values from the given ReferenceGrid.
   *  
   *  @param  grid      The grid to be copied
   */
  public ReferenceGrid( ReferenceGrid grid )
  {
    id     = grid.id;
    n_rows = grid.n_rows;
    n_cols = grid.n_cols;

    if ( grid.data != null )
    {
      data = new Data[ data.length ][ data[0].length ];
      for ( int i = 0; i < data.length; i++ )
        for ( int j = 0; j < data[0].length; i++ )
          data[i][j] = grid.data[i][j];
    }
  }


  /**
   *  Get the ID of the current data grid (i.e. detector).  This ID should be 
   *  unique within the set of all detectors on an instrument.
   *
   *  @return a unique integer ID for this data grid.
   */
  public int ID()
  {
    return id;
  }


  /**
   *  Get the number of rows in this data grid.  
   *
   *  @return the number of rows.
   */
  public int num_rows()
  {
    return n_rows; 
  }


  /**
   *  Get the number of columns in this data grid.  
   *
   *  @return the number of columns.
   */
  public int num_cols()
  {
    return n_cols;  
  }


  /**
   *  Get the Data block from one pixel.
   *
   *  @param  row   the row number of the Data block, 1..n_rows
   *  @param  col   the column number of the Data block, 1..n_cols
   *
   *  @return  The Data block corresponding to the specified row and column.
   *           If no Data entriew have been set, or if row, col are out of
   *           bounds, this returns null.
   */
  public Data getData_entry( int row, int col )
  {
    if ( data == null )
      return null;

    if ( row < 1 || row > n_rows || data[row-1] == null )
      return null;

    if ( col < 1 || col > n_cols )
      return null;

    return data[row-1][col-1];
  }


  /**
   *  Set the Data block reference for one pixel.
   *
   *  @param  d     The Data block the specified row and column will refer
   *  @param  row   the row number of the Data block, 1..n_rows
   *  @param  col   the column number of the Data block, 1..n_cols
   *                to.
   */
  public void setData_entry( Data d, int row, int col )
  {
    if ( row < 1 || row > n_rows ) 
    {
      System.out.println("Invalid row number: " + row + " in " +
                         "ReferenceGrid.setData_entry() " );
      System.out.println("Must be beween 1 and " + n_rows );
      return;
    }

    if ( col < 1 || col > n_cols )
    {
      System.out.println("Invalid column number: " + col + " in " +
                         "ReferenceGrid.setData_entry() " );
      System.out.println("Must be beween 1 and " + n_cols );
      return;
    }

    if ( data == null )
      data = new Data[n_rows][n_cols];

    data[row-1][col-1] = d;
  }


  /**
   *  Clear all references to Data blocks.
   */
  public void clearData_entries()
  {
    data = null;
  }


  /**
   *  Write the data that defines the grid in a multi-line string.
   *
   *  @return A multi-line String listing the internal state information 
   *          for the grid. 
   */
  public String toString()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append("ID:" + ID() +'\n' );
    buffer.append("Nrows:" + num_rows() +'\n');
    buffer.append("Ncols:" + num_cols() +'\n');
    return buffer.toString();
  }


  /**
   *  This method goes through the Data blocks of a DataSet and records
   *  a reference to each of the Data blocks whose detector ID matches
   *  the ID of this ReferenceGrid.  
   *
   *  @param  ds   The DataSet  from which references to Data blocks will
   *               be obtained.
   */
  public void setData_entries( DataSet ds )
  {
    if ( ds == null )
    {
      System.out.println("DataSet null in ReferenceGrid.setData_entries()");
      return;
    }

    int n_data = ds.getNum_entries();
    if ( n_data <= 0 )
    {
      System.out.println("DataSet empty in ReferenceGrid.setData_entries()");
      return;
    }

    Data          d;
    int           row,
                  col;
    PixelInfoList pil;

    for ( int i = 0; i < n_data; i++ )
    {
      d = ds.getData_entry(i);
      pil = AttrUtil.getPixelInfoList( d );
      if ( pil != null )
      {
        for ( int j = 0; j < pil.num_pixels(); j++ )
          if ( pil.pixel(j).gridID() == id )                // record reference 
          {
            row = Math.round(pil.pixel(j).row());
            col = Math.round(pil.pixel(j).col());
            setData_entry( d, row, col );
          }
      }
    }
  }


  /**
   *  This method goes through the Data blocks of a DataSet and builds 
   *  the list of ReferenceGrids, complete with references to the Data 
   *  blocks.  The number of ReferenceGrids constructed and the references
   *  themselves, are determined by the PixelInfoLists of the Data blocks
   *  in the DataSet.
   *  
   *  @param  ds   The DataSet for which ReferenceGrids with references 
   *               to its Data blocks are to constructed.
   *
   *  @return If all entries in the DataSet had a pixel info list and
   *          those all referenced UniformGrids, then all entries in 
   *          the DataSet will be referrenced from the corresponding 
   *          DataGrids, and this method will return true.
   */
  public static ReferenceGrid[] MakeDataReferenceGrids( DataSet ds )
  {
    if ( ds == null )
      return null;

    int n_data = ds.getNum_entries();
    if ( n_data <= 0 )
      return null;

    Hashtable     hashtable = new Hashtable();
    ReferenceGrid grid;
    IDataGrid     data_grid;
    Data          d;
    int           row,
                  col;
    PixelInfoList pil;
    int           det_id;
    Integer       key;

    for ( int i = 0; i < n_data; i++ )
    {
      d = ds.getData_entry(i);
      pil = AttrUtil.getPixelInfoList( d ); 
      if ( pil != null )
      {
        for ( int j = 0; j < pil.num_pixels(); j++ )
        {
          IPixelInfo pixel = pil.pixel(j);
          row = Math.round(pixel.row());
          col = Math.round(pixel.col());
          det_id = pixel.gridID();
          key    = new Integer( det_id );
          grid = (ReferenceGrid)hashtable.get( key );
          if ( grid == null )                            // found a new ID so
          {                                              // put it in hashtable
            data_grid = pixel.DataGrid();
            grid = new ReferenceGrid( det_id, 
                                      data_grid.num_rows(), 
                                      data_grid.num_cols() );
            hashtable.put( key, grid ); 
          }
          grid.setData_entry( d, row, col );             // record reference
        }
      }
    }
                                                        // now get the list of
                                                        // grids, ordered by ID
    ReferenceGrid all_grids[] = new ReferenceGrid[ hashtable.size() ];

    Collection collection = hashtable.values();
    Object obj_arr[] = collection.toArray();
    for ( int i = 0; i < obj_arr.length; i++ )
      all_grids[i] = (ReferenceGrid)(obj_arr[i]);
        
    Arrays.sort( all_grids, new ReferenceGridComparator() );

    return all_grids;
  }


  /* ------------------------------------------------------------------- */
  /**
   *  Main program for testing purposes.
   */ 
  public static void main( String args[] )
  {
    int      id       =  1;
    int      n_rows   = 10;
    int      n_cols   = 5;

    ReferenceGrid test = new ReferenceGrid( id, n_rows, n_cols );

    System.out.print( test.toString() );          // show basic grid info

/*  The second section tests the Load_GLAD_LPSD_Info method, using data
    and detector information from the GLAD instrument at IPNS
*/
    String data_path     = "/usr2/ARGONNE_DATA/";
    String detector_path = "/home/dennis/WORK/ISAW/Databases/";
    String file_name     = data_path + "glad6942.run";

    Retriever retriever = new RunfileRetriever( file_name );
    DataSet   ds        = retriever.getDataSet(1);

    file_name = detector_path + "gladdets6.par";
    LoadUtil.Load_GLAD_LPSD_Info( ds, file_name );

    ReferenceGrid all_grids[] = MakeDataReferenceGrids( ds );
    System.out.println("FOUND " + all_grids.length + " GRIDS" );
    for ( int i = 0; i < all_grids.length; i++ )
    {
      System.out.print( "GRID ....." + all_grids[i] );
      Data d = all_grids[i].getData_entry( 50, 1 );
      System.out.println("Data group ID = " + d.getGroup_ID() );
      System.out.println("Number of y values = " + d.getY_values().length );
    }

    for ( int i = 1; i <= all_grids[100].num_rows(); i++ )
    {
      Data d = all_grids[100].getData_entry( i, 1 );
      if ( d != null )
        System.out.println( "" + i + ": Data group ID = " + d.getGroup_ID() );
      else
        System.out.println("" + i + ": null");
    }

    new ViewManager( ds, "3D View" );
  }
}

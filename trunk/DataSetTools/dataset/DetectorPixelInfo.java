/*
 * File:  DetectorPixelInfo.java
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
 *  Revision 1.1  2003/02/04 19:21:21  dennis
 *  Initial Version.
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import DataSetTools.math.*;

/**
 *   A DetectorPixelInfo object provides access to information about the 
 * position, size and orientation of a rectangular box representing one  
 * segment on a grid of detector segments.
 */  

public class DetectorPixelInfo implements IPixelInfo 
{
  private int        id;
  private short      row;
  private short      col;
  private IDataGrid  grid;

  /**
   *  Construct a DetectorPixelInfo object with the specified id, row, column
   *  and data grid.
   *
   *  @param  id    The unique segment id for this pixel.
   *  @param  row   The row number on the grid for this pixel.
   *  @param  col   The column number on the grid for this pixel.
   *  @param  grid  The grid that provides the information about the
   *                position, orientation, etc. for this pixel.
   */
  public DetectorPixelInfo( int id, short row, short col, IDataGrid grid )
  {
    this.id   = id;
    this.row  = row;
    this.col  = col;
    this.grid = grid;
  }

  /**
   *  Get the ID of the current pixel.  This ID should be unique within the
   *  set of all pixels on an instrument, such as the "segment ID" of the
   *  detector elements in an IPNS runfile.
   *
   *  @return a unique integer ID for this pixel.
   */
  public int ID()
  {
    return id;
  }

  /**
   *  Get the ID of the data grid (i.e. detector) that contains this pixel.
   *
   *  @return the ID for the IDataGrid containing this pixel.
   */
  public int gridID()
  {
    return grid.ID(); 
  }
  
  /**
   *  Get a reference to the data grid (i.e. detector) that contains this pixel.
   *
   *  @return a reference to the IDataGrid object that provides information 
   *          about this pixel.  If no data grid is used, this will return 
   *          null.
   */
  public IDataGrid DataGrid()
  {
    return grid;
  }

  /**
   *  Get the position in 3D of the center of this pixel.  
   *
   *  @return A vector giving the position in 3D of the center of the pixel.
   */
  public Vector3D position()
  {
    return grid.position( row, col );
  }

  /**
   *  Get the vector in the "x" direction for the local coordinate system
   *  of this pixel.
   *
   *  @return A vector giving the components of a vector in the local
   *          "x" direction for this pixel.
   */
  public Vector3D x_vec()
  {
    return grid.x_vec( row, col );
  }

  /**
   *  Get the vector in the "y" direction for the local coordinate system
   *  of this pixel.
   *
   *  @return A vector giving the components of a vector in the local
   *          "y" direction for this pixel.
   */
  public Vector3D y_vec()
  {
    return grid.y_vec( row, col );
  }

  /**
   *  Get the vector in the "z" direction for the local coordinate system
   *  of this pixel.
   *
   *  @return A vector giving the components of a vector in the local
   *          "z" direction for this pixel.
   */
  public Vector3D z_vec()
  {
    return grid.z_vec( row, col );
  }

  /**
   *  Get the nominal width of this pixel.  The width is the size of the
   *  pixel in the direction given by x_vec().
   *
   *  @return the width of the pixel. 
   */
  public float width()
  {
    return grid.width( row, col );
  }

  /**
   *  Get the nominal height of this pixel.  The height is the size of the
   *  pixel in the direction given by y_vec().
   *
   *  @return the height of the pixel.
   */
  public float height()
  {
    return grid.height( row, col );
  }

  /**
   *  Get the nominal depth of this pixel.  The depth is the size of the
   *  pixel in the direction given by z_vec().
   *
   *  @return the depth of the pixel.
   */
  public float depth()
  {
    return grid.depth( row, col );
  }

  /**
   *  Find the "row" number for this pixel on it's data grid.  
   *
   *  @return the row number corresponding to this pixel.
   */
  public float row()
  {
    return row;
  }

  /**
   *  Find the "column" number for this pixel on it's data grid.  
   *
   *  @return the column number corresponding to this pixel.
   */
  public float col()
  {
    return col;
  }

  /**
   *  Get the solid angle subtended by this pixel from the origin.
   * 
   *  @return the solid angle subtended by the pixel.
   */
  public float SolidAngle()
  {
    return grid.SolidAngle( row, col );
  }

  /**
   *  Get the approximate range of scattering angles subtended by this pixel.
   *
   *  @return the range of scattering angles for the specified pixel.
   */
  public float Delta2Theta()
  {
    return grid.Delta2Theta( row, col );
  }

  /**
   *  Write the data that defines the pixel in a multi-line string.
   *
   *  @return A multi-line String listing the internal state information
   *          for the pixel.

   */
  public String toString()
  {
    String buffer = "grid_id   = " + grid.ID() + '\n' +
                    "pixel_id  = " + id + '\n' +
                    "row       = " + row + '\n' +
                    "col       = " + col; 
    return buffer;
  }

  /* ------------------------------------------------------------------- */
  /**
   *  Main program for testing purposes.
   */
  public static void main( String args[] )
  {
    int      id       =  1;
    String   units    = "meters";
    Vector3D center   = new Vector3D( 2.5f, 5, 0.5f );
    Vector3D x_vector = new Vector3D( 1, 0, 0 );
    Vector3D y_vector = new Vector3D( 0, 1, 0 );
    float    width    = 5;
    float    height   = 10;
    float    depth    = 1;
    int      n_rows   = 10;
    int      n_cols   = 5;

    UniformGrid test_grid = new UniformGrid( id, units,
                                             center, x_vector, y_vector,
                                             width, height, depth,
                                             n_rows, n_cols );


    System.out.print( test_grid.toString() );     // show basic grid info

    short row = 9;                                  // show info on one pixel
    short col = 4;
    DetectorPixelInfo test_pixel = 
                          new DetectorPixelInfo( 2, row, col, test_grid );
 
    System.out.print( test_pixel.toString() );    // show basic pixel info
    System.out.println("---------------------------------------");
    System.out.println("At row = " + row + " col = " + col );
    System.out.println("position() = " + test_pixel.position() );
    System.out.println("x_vec()    = " + test_pixel.x_vec() );
    System.out.println("y_vec()    = " + test_pixel.y_vec() );
    System.out.println("z_vec()    = " + test_pixel.z_vec() );
    System.out.println("width()    = " + test_pixel.width() );
    System.out.println("depth()    = " + test_pixel.depth() );
    System.out.println("height()   = " + test_pixel.height() );
    System.out.println("SolidAngle()  = " + test_pixel.SolidAngle() );
    System.out.println("Delta2Theta() = " + test_pixel.Delta2Theta() );
  }


} 

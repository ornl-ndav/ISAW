/*
 * File:  UniformGrid_d.java
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
 *  Revision 1.1  2003/07/14 22:25:13  dennis
 *  Double precision version, ported from single precision version.
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import DataSetTools.math.*;

/**
 *   A UniformGrid_d is an abstraction of an area detector with equal size
 * "pixels" uniformly spaced in 3D at which data is measured.  Each position 
 * where a measurement is taken is approximated by a rectangular 3D "box" with 
 * arbitrary orientation and dimensions.  The grid manages "topological" 
 * information about the positions.  That is, it organizes the positions 
 * into 2D arrays of 3D positions where successive entries in rows and
 * columns of the 2D array are adjacent in 3D.  The rows and columns are 
 * numbered starting with row = 1 and column = 1.
 */  

public class UniformGrid_d implements IDataGrid_d
{
  // NOTE: any field that is static or transient is NOT serialized.
  //
  // CHANGE THE "serialVersionUID" IF THE SERIALIZATION IS INCOMPATIBLE WITH
  // PREVIOUS VERSIONS, IN WAYS THAT CAN NOT BE FIXED BY THE readObject()
  // METHOD.  SEE "IsawSerialVersion" COMMENTS BELOW.  CHANGING THIS CAUSES
  // JAVA TO REFUSE TO READ DIFFERENT VERSIONS.
  //
  public  static final long serialVersionUID = 1L;


  // NOTE: The following fields are serialized.  If new fields are added that
  //       are not static, reasonable default values should be assigned in the
  //       readObject() method for compatibility with old servers, until the
  //       servers can be updated.
  private int IsawSerialVersion = 1;         // CHANGE THIS WHEN ADDING OR
                                             // REMOVING FIELDS, IF
                                             // readObject() CAN FIX ANY
                                             // COMPATIBILITY PROBLEMS
  private int       id;
  private String    units;

  private double    center[] = { 0, 0, 0 };         // 3D point giving the  
                                                    // center of the grid

  private double    x_vector[] = { 1, 0, 0 },       // local coordinate system
                    y_vector[] = { 0, 1, 0 },       // for the grid
                    z_vector[] = { 0, 0, 1 };

  private double    width  = 1,      // overall dimensions of the grid
                    height = 1,
                    depth  = 1;

  private int       n_rows = 1,      // number of rows and columns that the
                    n_cols = 1;      // grid is divided into

  private double    dx,              // width per column on the grid
                    dy;              // height per row on the grid

  private double    col_x_offset,    // offsets from center to center of boxs
                    row_y_offset;    // in row 0 and/or column 0;

  private Data      data[][] = null;

  private boolean   data_loaded = false;

  /**
   *  Construct a new UniformGrid_d object.
   *
   *  Note: The pixel locations are at the center of the boxes.  For example,
   *        to describe the uniform grid consisting of unit cubes filling 
   *        the region of space: 0 <= x <= 5, 0 <= y < 10 and 0 <= z <= 1
   *        with rows numbered 1..10 and columns numbered 1..5 we would 
   *        specify:
   *
   *            center   = (2.5,5,0.5)
   *            x_vector = (1,0,0)
   *            y_vector = (0,1,0)
   *            width    = 5 
   *            height   = 10
   *            depth    = 1
   *            n_rows   = 10
   *            n_cols   = 5
   *     
   *        The "box" in row 1, col 1 of the grid will be in position
   *        (0.5,0.5,0.5).  The "box" in row 10, col 5 will be in position
   *        (4.5,9.5,0.5) 
   *
   *  @param  id         Unique integer ID to be used for this data grid
   *  @param  units      The measurement units for the position, width, height,
   *                     etc. for this data grid. 
   *  @param  center     The position of the center of this DataGrid
   *  @param  x_vector   Vector in the "x" direction of the local coordinate
   *                     system for this grid.
   *  @param  y_vector   Vector in the "y" direction of the local coordinate
   *                     system for this grid.
   *  @param  width      Overall width of the whole grid in the "x" direction.
   *                     The width must be positive.
   *  @param  height     Overall height of the whole grid in the "y" direction 
   *                     The height must be positive.
   *  @param  n_rows     The number of rows of pixels in the whole grid. 
   *                     NOTE: This is the number of "boxes" the grid is 
   *                     divided into in the "y" direction. 
   *                     This must be at least 1. 
   *  @param  n_cols     The number of columns of pixels in the whole grid. 
   *                     NOTE: This is the number of "boxes" the grid is 
   *                     divided into in the "x" direction. 
   *                     This must be at least 1. 
   */
  public UniformGrid_d( int         id, 
                        String      units,
                        Vector3D_d  center, 
                        Vector3D_d  x_vector,
                        Vector3D_d  y_vector,
                        double      width, 
                        double      height, 
                        double      depth, 
                        int         n_rows,
                        int         n_cols  )
  {
    this.id       = id;
    this.units    = units;

    if ( n_cols > 0 )
      this.n_cols = n_cols;

    if ( n_rows > 0 )
      this.n_rows = n_rows;

    setHeight( height );
    setWidth( width );
    setDepth( depth );

    setCenter( center );
    setOrientation( x_vector, y_vector );
  }


  /* ----------------------------- copy constructor ---------------------- */
  /**
   *  Make a new UniformGrid_d, copying the values from the given UniformGrid_d.
   *  
   *  @param  grid      The grid to be copied
   *  @param  copy_data Flag indicating whether the references to the Data
   *                    blocks should be copied, if the references are set.
   */
  public UniformGrid_d( UniformGrid_d grid, boolean copy_data )
  {
    id = grid.id;
    units = grid.units;
    center = new double[3]; 
    x_vector = new double[3]; 
    y_vector = new double[3]; 
    z_vector = new double[3]; 
    for ( int i = 0; i < 3; i++ )
    {
      center[i]   = grid.center[i];
      x_vector[i] = grid.x_vector[i];
      y_vector[i] = grid.y_vector[i];
      z_vector[i] = grid.z_vector[i];
    }

    width  = grid.width;
    height = grid.height;
    depth  = grid.depth;

    n_rows = grid.n_rows;
    n_cols = grid.n_cols;

    dx = grid.dx;
    dy = grid.dy;

    col_x_offset = grid.col_x_offset;
    row_y_offset = grid.row_y_offset;

    data = null;
    data_loaded = false;
    if ( copy_data && grid.data_loaded )
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
   *  Get the total number of points that make up this grid.  This will equal
   *  num_rows() * num_cols().
   *
   *  @return a non-negative integer giving the total number of points in
   *          this data grid.
   */
  public int num_points()
  {
    return n_rows * n_cols;
  }
 
  /**
   *  Get the units for this data grid.  The points of this IDataGrid are 
   *  positioned in some three dimensional space.  The units on each dimension
   *  of this space are assumed to be the same.
   *
   *  @return A string describing the units used.  
   */
  public String units() 
  {
    return units; 
  }

  /**
   *  Get the vector in the "x" direction for the local coordinate system 
   *  of the data grid.  If the data grid is non-planar, this will be a vector
   *  in the local "x" direction of a planar approximation to the data grid.
   *
   *  @return A vector giving the components of a vector in the local
   *          "x" direction for this data grid.
   */
  public Vector3D_d x_vec()
  {
    return new Vector3D_d( x_vector ); 
  }

  /**
   *  Get the vector in the "y" direction for the local coordinate system 
   *  of the data grid.  If the data grid is non-planar, this will be a vector
   *  in the local "y" direction of a planar approximation to the data grid.
   *
   *  @return A vector giving the components of a vector in the local
   *          "y" direction for this data grid.
   */
  public Vector3D_d y_vec()
  {
    return new Vector3D_d( y_vector ); 
  }

  /**
   *  Get the vector in the "z" direction for the local coordinate system 
   *  of the data grid.  If the data grid is planar, this will be in the
   *  direction of the normal to the plane.  If the data grid is non-planar, 
   *  this will be a vector normal to a planar approximation to the data grid.
   *  The "z" direction must be chosen to form a right handed coordinate 
   *  system.
   *
   *  @return A vector giving the components of a vector in the local
   *          "z" direction for this data grid.
   */
  public Vector3D_d z_vec()
  {
    return new Vector3D_d( z_vector ); 
  }

  /**
   *  Get the nominal position of the center of the data grid.
   *
   *  @return A vector giving the nominal center of this data grid.
   */
  public Vector3D_d position()
  {
    return new Vector3D_d( center ); 
  }

  /**
   *  Get the nominal width of the data grid.  The width is the size of the
   *  grid in the direction given by x_vec().
   *
   *  @return the width of the data grid.
   */
  public double width()
  {
    return width;
  }

  /**
   *  Get the nominal height of the data grid.  The height is the size of the
   *  grid in the direction given by y_vec().
   *
   *  @return the height of the data grid.
   */
  public double height()
  {
    return height;
  }

  /**
   *  Get the nominal depth of the data grid.  The depth is the size of the
   *  grid in the direction given by z_vec().
   *
   *  @return the depth of the data grid.
   */
  public double depth()
  {
    return depth;
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
   *  Find the offset in the directon of "x_vec()", from the center of the 
   *  grid to the specified position.  If row and col are integers, this 
   *  will be the offset to the center of the specified "box".  The rows
   *  and columns are numbered starting with row = 1 and col = 1 in the 
   *  lower left corner of the detector.
   *
   *  @param row  row number from 1 to the total number of rows.
   *  @param col  column number from 1 to the total number of columns.
   *
   *  @return the offset in the "x" direction to the specified position on
   *          the grid.
   */
  public double x( double row, double col )
  {
    return ((col-1) * dx + col_x_offset); 
  }

  /**
   *  Find the offset in the directon of "y_vec()", from the center of the 
   *  grid to the specified position.  If row and col are integers, this 
   *  will be the offset to the center of the specified "box".  The rows
   *  and columns are numbered starting with row = 1 and col = 1 in the 
   *  lower left corner of the detector.
   *
   *  @param row  row number from 1 to the total number of rows.
   *  @param col  column number from 1 to the total number of columns.
   *
   *  @return the offset in the "y" direction to the specified position on
   *          the grid.
   */
  public double y( double row, double col )
  {
    return ((row-1) * dy + row_y_offset); 
  }

  /**
   *  Find the "row" number for the specified position on the grid.  The 
   *  position is specified in terms of offsets from the center of the 
   *  grid in the directions of "x_vec()" and "y_vec()".  
   *  The row value is returned as a double, to allow for 
   *  positions that are NOT at the exact center of a "box".  If an integer
   *  row number is needed, the value returned by this method should be
   *  rounded.   The rows and columns are numbered starting with row = 1 
   *  and col = 1 in the lower left corner of the detector.
   *
   *  @return the fractional row number corresponding to the specified 
   *          position on the grid.
   */ 
  public double row( double x, double y )
  {
    return ( (y - row_y_offset)/dy ) + 1;
  }

  /**
   *  Find the "column" number for the specified position on the grid.  The 
   *  position is specified in terms of offsets from the center of the 
   *  grid in the directions of "x_vec()" and "y_vec()".  
   *  The column value is returned as a double, to allow for 
   *  positions that are NOT at the exact center of a "box".  If an integer
   *  column number is needed, the value returned by this method should be
   *  rounded.  The rows and columns are numbered starting with row = 1 
   *  and col = 1 in the lower left corner of the detector.
   *
   *  @return the fractional column number corresponding to the specified 
   *          position on the grid.
   */ 
  public double col( double x, double y )
  {
    return ( (x - col_x_offset)/dx ) + 1;
  }

  /**
   *  Get the position in 3D of the specified point on the grid.  If the
   *  row and col values are integers, this will be the center point of
   *  a grid "box".  If row and/or col are not integers, the position
   *  returned will be offset from the center of the grid "box". The rows
   *  and columns are numbered starting with row = 1 and col = 1 in the
   *  lower left corner of the detector.
   *
   *  @param row  row number from 1 to the total number of rows.
   *  @param col  column number from 1 to the total number of columns.
   *
   *  @return A vector giving the position in 3D of the specified row
   *          and column values.
   */
  public Vector3D_d position( double row, double col )
  {
    double delta_x = x(row,col);
    double delta_y = y(row,col);

    double pos[] = new double[3];
    for ( int i = 0; i < 3; i++ )
      pos[i] = center[i] + delta_x * x_vector[i] + delta_y * y_vector[i];

    return new Vector3D_d( pos );
  }

  /**
   *  Get the width of the specified grid "box".  If row and col are not
   *  integers, they will be rounded to obtain integer values that 
   *  specifiy a particular grid "box".  The rows and columns are numbered 
   *  starting with row = 1 and col = 1 in the lower left corner of the 
   *  detector.
   *
   *  @param row  row number from 1 to the total number of rows.
   *  @param col  column number from 1 to the total number of columns.
   *
   *  @return The width of the specified grid "box" in the direction of 
   *          the "x_vec(row,col)".
   */
  public double width( double row, double col )
  {
    return dx;
  }

  /**
   *  Get the height of the specified grid "box".  If row and col are not
   *  integers, they will be rounded to obtain integer values that 
   *  specifiy a particular grid "box".   The rows and columns are numbered 
   *  starting with row = 1 and col = 1 in the lower left corner of the 
   *  detector.
   *
   *  @param row  row number from 1 to the total number of rows.
   *  @param col  column number from 1 to the total number of columns.
   *
   *  @return The height of the specified grid "box" in the direction of 
   *          the "y_vec(row,col)".
   */
  public double height( double row, double col )
  {
    return dy;
  }

  /**
   *  Get the depth of the specified grid "box".  If row and col are not
   *  integers, they will be rounded to obtain integer values that 
   *  specifiy a particular grid "box". The rows and columns are numbered 
   *  starting with row = 1 and col = 1 in the lower left corner of the 
   *  detector.
   *
   *  @param row  row number from 1 to the total number of rows.
   *  @param col  column number from 1 to the total number of columns.
   *
   *  @return The depth of the specified grid "box" in the direction of 
   *          the "z_vec(row,col)".
   */
  public double depth( double row, double col )
  {
    return this.depth;
  }

  /**
   *  Get the vector in the "x" direction for a particular grid "box".
   *  If the data grid is planar, this may return the same vector as
   *  the x_vec() method.  If the data grid is non-planar, this will be 
   *  a vector in the local "x" direction for the particular grid "box".
   *  The rows and columns are numbered starting with row = 1 and col = 1 
   *  in the lower left corner of the  detector.
   *
   *  @param row  row number from 1 to the total number of rows.
   *  @param col  column number from 1 to the total number of columns.
   *
   *  @return A vector giving the components of a vector in the local
   *          "x" direction for this grid "box".
   */
  public Vector3D_d x_vec( double row, double col )
  {
    return new Vector3D_d( x_vector );   // same for all pixels on uniform grid
  }

  /**
   *  Get the vector in the "y" direction for a particular grid "box".
   *  If the data grid is planar, this may return the same vector as
   *  the y_vec() method.  If the data grid is non-planar, this will be 
   *  a vector in the local "y" direction for the particular grid "box".
   *  The rows and columns are numbered starting with row = 1 and col = 1 
   *  in the lower left corner of the  detector.
   *
   *  @param row  row number from 1 to the total number of rows.
   *  @param col  column number from 1 to the total number of columns.
   *
   *  @return A vector giving the components of a vector in the local
   *          "y" direction for this grid "box".
   */
  public Vector3D_d y_vec( double row, double col ) 
  {
    return new Vector3D_d( y_vector );   // same for all pixels on uniform grid
  }

  /**
   *  Get the vector in the "z" direction for a particular grid "box".
   *  If the data grid is planar, this will return the same vector as
   *  the z_vec() method.  If the data grid is non-planar, this will be 
   *  a vector in the local "z" direction for the particular grid "box".
   *  The rows and columns are numbered starting with row = 1 and col = 1 
   *  in the lower left corner of the  detector.
   *
   *  @param row  row number from 1 to the total number of rows.
   *  @param col  column number from 1 to the total number of columns.
   *
   *  @return A vector giving the components of a vector in the local
   *          "z" direction for this grid "box".
   */
  public Vector3D_d z_vec( double row, double col )
  {
    return new Vector3D_d( z_vector );   // same for all pixels on uniform grid
  }

  /**
   *  Get the solid angle subtended by this grid "box" from the origin. 
   *  If the row and column values are not integers, they will be rounded
   *  to obtain integer values that specify a particular grid "box".  The
   *  solid angle is approximated as:
   *
   *    A * |cos(t)| / (r*r) 
   *  
   *  where A = dx * dy is the area of the "face" of the pixel, t is the
   *  angle between the unit vector pointing towards the origin from the 
   *  center of the box and the "z" orientation vector for the box.
   *
   *  @param row  row number from 1 to the total number of rows.
   *  @param col  column number from 1 to the total number of columns.
   * 
   *  @return the solid angle subtended by the specified grid box.
   */
  public double SolidAngle( double row, double col )
  {
    row = Math.round(row);
    col = Math.round(col);

    Vector3D_d pos = position( row, col );
    double r = pos.length();

    if ( r == 0 )
      return 0;

    pos.normalize();
    double pos_arr[] = pos.get();
    double dot = 0;
    for ( int i = 0; i < 3; i++ )
      dot += z_vector[i] * pos_arr[i];

    if ( dot < 0 )
      dot = -dot;

    return dot * dx * dy / ( r * r );
  }

  /**
   *  Get the approximate range of scattering angles subtended by the 
   *  specified grid "box".  If the row and column values are not integers, 
   *  they will be rounded to obtain integer values that specify a particular 
   *  grid "box".  The box is assumed to be oriented so that the "z" vector
   *  points towards the origin.  In that case the delta two theta value
   *  will be determined by the height and width of the box.
   *
   *  @param row  row number from 1 to the total number of rows.
   *  @param col  column number from 1 to the total number of columns.
   *
   *  @return the range of scattering angles for the specified grid box,
   *          in degrees.
   */
  public double Delta2Theta( double row, double col )
  {
    Vector3D_d pos = position( row, col );
    double r = pos.length();

    if ( r == 0 )
      return 180.0;

    double angle = 2 * Math.atan( Math.sqrt(dx*dx + dy*dy)/2/r );

    return  angle * 180 / Math.PI;
  }


  /**
   *  Move the UniformGrid_d to a new center point.
   *
   *  @param center  The new position of the center of the grid.
   */
  public void setCenter( Vector3D_d center )
  {
    double temp[] = center.get();

    for (int i = 0; i < 3; i++ )
      this.center[i] = temp[i]; 
  }


  /**
   *  Change the orientation of the UniformGrid_d by specifying new x_vec and
   *  y_vec direction vectors.  The x_vec and y_vec directions should be
   *  perpendicular, but must at least not be parallel.  The x_vec and y_vec
   *  is used to calculate the vector z_vec in the direction of the
   *  cross product (x_vec X y_vec).  Subsequently the value of the y_vec
   *  direction is recalculated to be in the direction (z_vec X x_vec).
   *  All resulting vectors are normalized to be of unit length, so they
   *  will form an orthonormal coordinate system.
   *
   *  @param x_vector  The direction to be used as the "x" direction of the
   *                   DataGrid.
   *  @param y_vector  The direction to be used as the "y" direction of the
   *                   DataGrid.
   *
   *  @return This returns true if the input vectors were valid (not null,
   *          non-zero and not colinear) and returns false otherwise.  If
   *          the input vectors are not valid, the orientation will not be
   *          changed.
   */
  public boolean setOrientation( Vector3D_d x_vector, Vector3D_d y_vector )
  {
    if ( x_vector == null || y_vector == null )
      return false;

    Vector3D_d temp_z = new Vector3D_d();              // calculate vector in z dir
    temp_z.cross( x_vector, y_vector );

    if ( temp_z.length() == 0 )
      return false;

    Vector3D_d temp_x = new Vector3D_d( x_vector );
    Vector3D_d temp_y = new Vector3D_d( y_vector );

    temp_x.normalize();                            // make sure they're unit
    temp_y.normalize();                            // vectors
    temp_z.normalize();
                                                   // copy into local arrays
    for ( int i = 0; i < 3; i++ )
    {
      this.x_vector[i] = temp_x.get()[i];
      this.y_vector[i] = temp_y.get()[i];
      this.z_vector[i] = temp_z.get()[i];
    }
    return true;
  }


  /**
   *  Set a new height for the grid.  The specified height must be positive, 
   *  or the specified value will be ignored.
   *
   *  @param  height   The new height for the UniformGrid_d.
   */
  public boolean setHeight( double height )
  {
    if ( height <= 0 )
      return false;

    this.height = height;
    dy = height/n_rows;                     // calculate height of one row
    row_y_offset = -dy *(n_rows-1)/2.0;     // and the offset to row 0
    return true;
  }


  /**
   *  Set a new width for the grid.  The specified width must be positive,
   *  or the specified value will be ignored.
   *
   *  @param  width   The new width for the UniformGrid_d.
   */
  public boolean setWidth( double width )
  {
    if ( width <= 0 )
      return false;

    this.width = width;
    dx = width/n_cols;                      // calculate width of one column 
    col_x_offset = -dx *(n_cols-1)/2.0;     // and the offset to column 0
    return true;
  }


  /**
   *  Set a new depth for the grid.  The specified depth must be positive,
   *  or the specified value will be ignored.
   *
   *  @param  depth   The new depth for the UniformGrid_d.
   */
  public boolean setDepth( double depth )
  {
    if ( depth <= 0 )
      return false;

    this.depth = depth;
    return true;
  }


  /**
   *  This method goes through the Data blocks of a DataSet and records
   *  a reference to each of the Data blocks in all of the UniformDataGrids 
   *  associated with the pixels in it's pixel info list.  If any of the 
   *  Data blocks don't have a pixel info list this returns false.  Otherwise
   *  it returns true.  The DataGrids that this finds will also have their
   *  data_loaded flags set to true, so this method should only be used on 
   *  DataSets containing a complete set of Data blocks for the Data grids
   *  that appear in the PixelInfoList.
   *
   *  @param  ds   The DataSet for which references to its Data blocks are to
   *               be made from their Data grids.
   *
   *  @return If all entries in the DataSet had a pixel info list and
   *          those all referenced UniformGrid_ds, then all entries in 
   *          the DataSet will be referrenced from the corresponding 
   *          DataGrids, and this method will return true.
   */
  public static boolean setDataEntriesInAllGrids( DataSet ds )
  {
    if ( ds == null )
      return false;

    int n_data = ds.getNum_entries();
    if ( n_data <= 0 )
      return false;

    Data          d;
    Attribute     attr;
    int           row,
                  col;
    PixelInfoList pil;
    UniformGrid_d     grid;
    boolean       complete = true;

    for ( int i = 0; i < n_data; i++ )
    {
      d = ds.getData_entry(i);
      attr = d.getAttribute( Attribute.PIXEL_INFO_LIST );
      if ( attr != null && attr instanceof PixelInfoListAttribute )
      {
        pil = (PixelInfoList)attr.getValue();
        for ( int j = 0; j < pil.num_pixels(); j++ )
        {
          grid = (UniformGrid_d)(pil.pixel(j).DataGrid());
          if ( grid.data == null )
            grid.data = new Data[grid.n_rows][grid.n_cols]; 
          row = Math.round(pil.pixel(j).row());
          col = Math.round(pil.pixel(j).col());
          grid.data[row-1][col-1] = d;                      // record reference
          grid.data_loaded = true;
        }
      }
      else
        complete = false;
    }

    return complete;
  }


  /**
   *  This method goes through the Data blocks of a DataSet and records
   *  a reference to each of the Data blocks whose detector ID matches
   *  the ID of this DataGrid.  References to the Data blocks are recorded
   *  in a table, indexed by the row and column numbers.  This allows the
   *  Data to be accessed based on (row, col) pairs by the getData_entry
   *  method.  NOTE: The Data blocks in the DataSet must have PixelInfoLists
   *  stored as an Attribute.PIXEL_INFO_LIST attribute.
   *
   *  @param  ds   The DataSet  from which references to Data blocks will
   *               be obtained.
   *
   *  @return true if this DataGrid now has a complete set of references to
   *               Data blocks for each of its grid positions.
   */
  public boolean setData_entries( DataSet ds )
  {
    if ( ds == null )
      return false;

    int n_data = ds.getNum_entries();
    if ( n_data <= 0 )
      return false;

    Data          d;
    Attribute     attr;
    int           row, 
                  col;
    PixelInfoList pil;

    data_loaded = false;
    data        = new Data[n_rows][n_cols];
    for ( int i = 0; i < n_data; i++ )
    {
      d = ds.getData_entry(i);
      attr = d.getAttribute( Attribute.PIXEL_INFO_LIST ); 
      if ( attr != null && attr instanceof PixelInfoListAttribute )
      {
        pil = (PixelInfoList)attr.getValue();
        for ( int j = 0; j < pil.num_pixels(); j++ )
          if ( pil.pixel(j).gridID() == id )                // record reference 
          {
            row = Math.round(pil.pixel(j).row());
            if ( row >= 1 && row <= n_rows )
            {
              col = Math.round(pil.pixel(j).col());
              if ( col >= 1 && col <= n_cols )
                data[row-1][col-1] = d;
            }
          }
      }
    } 

    data_loaded = true;
    row = 0;
    while ( row < n_rows && data_loaded )
    {
      col = 0;
      while ( col < n_cols && data_loaded )
      {
        if ( data[row][col] == null )
          data_loaded = false;
        else
          col++;
      }
      row++;
    }

    return data_loaded;
  }


  /**
   *  Get the Data block from one pixel, if references to the Data blocks
   *  have been set.
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
   *  Check whether or not Data blocks have been set for each row and column
   *  of this DataGrid.
   *
   *  @return  true if Data entries have been set for all of the pixels
   *           and false if any pixel has a null Data block.
   */
  public boolean isData_entered()
  {
    return data_loaded;
  }

  /**
   *  Clear all references to Data blocks.
   */
  public void clearData_entries()
  {
    data = null; 
    data_loaded = false;
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
    buffer.append("Cen:" + position() +'\n');
    buffer.append("Width:" + width() +'\n');
    buffer.append("Height:" + height() +'\n');
    buffer.append("Depth:" + depth() +'\n');
    buffer.append("x_vec:" + x_vec() +'\n');
    buffer.append("y_vec:" + y_vec() +'\n');
    buffer.append("z_vec:" + z_vec() +'\n');
    buffer.append("Units:" + units() +'\n');
    buffer.append("Data Loaded: " + data_loaded );
    return buffer.toString();
  }


/* -----------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

/* ---------------------------- readObject ------------------------------- */
/**
 *  The readObject method is called when objects are read from a serialized
 *  ojbect stream, such as a file or network stream.  The non-transient and
 *  non-static fields that are common to the serialized class and the
 *  current class are read by the defaultReadObject() method.  The current
 *  readObject() method MUST include code to fill out any transient fields
 *  and new fields that are required in the current version but are not
 *  present in the serialized version being read.
 */

  private void readObject( ObjectInputStream s ) throws IOException,
                                                        ClassNotFoundException
  {
    s.defaultReadObject();               // read basic information

    if ( IsawSerialVersion != 1 )
      System.out.println("Warning:UniformGrid_d IsawSerialVersion != 1");
  }



  /* ------------------------------------------------------------------- */
  /**
   *  Main program for testing purposes.
   */ 
  public static void main( String args[] )
  {
    int      id         =  1;
    String   units      = "meters";
    Vector3D_d center   = new Vector3D_d( 2.5, 5, 0.5 );
    Vector3D_d x_vector = new Vector3D_d( 1, 0, 0 );
    Vector3D_d y_vector = new Vector3D_d( 0, 1, 0 );
    double    width     = 5;
    double    height    = 10;
    double    depth     = 1;
    int      n_rows     = 10;
    int      n_cols     = 5;

    UniformGrid_d test = new UniformGrid_d( id, units, 
                                        center, x_vector, y_vector,
                                        width, height, depth,
                                        n_rows, n_cols );
   
    System.out.print( test.toString() );          // show basic grid info
 
    double row_list[] = { 1, 5.5, 10 };
    double col_list[] = { 1, 3, 5 };
    double row; 
    double col;
                                                  // show info on some pixels
    for ( int i = 0; i < row_list.length; i++ )
    {
      row = row_list[i];
      col = col_list[i];
      double x = test.x(row,col);
      double y = test.y(row,col);
      System.out.println("---------------------------------------");
      System.out.println("At row = " + row + " col = " + col );
      System.out.println("x(row,col) = " + test.x(row,col) );
      System.out.println("y(row,col) = " + test.y(row,col) );
      System.out.println("row(x,y)   = " + test.row(x,y) );
      System.out.println("col(x,y)   = " + test.col(x,y) );
      System.out.println("position(row,col) = " + test.position(row,col) );
      System.out.println("x_vec(row,col)    = " + test.x_vec(row,col) );
      System.out.println("y_vec(row,col)    = " + test.y_vec(row,col) );
      System.out.println("z_vec(row,col)    = " + test.z_vec(row,col) );
      System.out.println("width(row,col)    = " + test.width(row,col) );
      System.out.println("depth(row,col)    = " + test.depth(row,col) );
      System.out.println("height(row,col)   = " + test.height(row,col) );
      System.out.println("SolidAngle(row,col)  = " + test.SolidAngle(row,col));
      System.out.println("Delta2Theta(row,col) = " + test.Delta2Theta(row,col));
    }
  }

} 

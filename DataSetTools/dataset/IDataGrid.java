/*
 * File:  IDataGrid.java
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
 *  Revision 1.7  2007/12/19 19:18:32  rmikk
 *  Added a clone method to interface. There are now two grid types so using a
 *    constructor does not work generically.
 *
 *  Revision 1.6  2005/07/11 21:00:03  dennis
 *  Removed num_points() method, since it is redundant.
 *
 *  Revision 1.5  2004/03/15 06:10:38  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.4  2004/03/15 03:28:07  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.3  2003/05/24 21:09:14  dennis
 *  Added methods setData_entries(ds), getData_entry(row,col), and
 *  isData_entered() to allow DataGrids to keep a list of references
 *  to Data blocks associated with each pixel.
 *
 *  Revision 1.2  2003/02/20 19:45:00  dennis
 *  Now implements Serializable.
 *
 *  Revision 1.1  2003/02/04 18:15:06  dennis
 *  Initial version.
 *
 */

package  DataSetTools.dataset;

import gov.anl.ipns.MathTools.Geometry.*;

import java.io.*;

/**
 * An IDataGrid is an abstraction of a detector.  It specifies a set of 
 * positions in 3D, at which data is measured.  Each position where a 
 * measurement is taken is approximated by a rectangular 3D "box" with 
 * arbitrary orientation and dimensions.  The grid manages "topological" 
 * information about the positions.  That is, it organizes the positions 
 * into 2-D arrays of 3D positions.  The interface is implemented by class 
 * UniformGrid, class describing a collection of positions arranged to 
 * form a uniform grid, such a many area detectors or LPSDs.  Other classes 
 * can implement this interface, as needed, to describe a non-uniform grid.
 */  

public interface IDataGrid extends Serializable
{
   /**
    * Clones an Copy of itself that can be changed without changing the
    * current grid. Note the following two points
    * 
    * 1) The Data Entries that have been set into the returned grid will agree
    *   with those set in the current grid. You may want to ClearDataEntries
    *    
    * 2) The returned grid has the same ID as the current grid. If this grid
    *     is to be used except for scratch, its ID should be changed. 
    *     SetDataEntries need not be done because of point 1.
    *  
    * 
    * @return  The cloned data grid.
    */
   public IDataGrid clone();
   
  /**
   *  Get the ID of the current data grid (i.e. detector).  This ID should be 
   *  unique within the set of all detectors on an instrument.
   *
   *  @return a unique integer ID for this data grid.
   */
  public int ID();

  /**
   *  Get the units for this data grid.  The points of this IDataGrid are 
   *  positioned in some three dimensional space.  The units on each dimension
   *  of this space are assumed to be the same.
   *
   *  @return A string describing the units used.  
   */
  public String units(); 

  /**
   *  Get the vector in the "x" direction for the local coordinate system 
   *  of the data grid.  If the data grid is non-planar, this will be a vector
   *  in the local "x" direction of a planar approximation to the data grid.
   *
   *  @return A vector giving the components of a vector in the local
   *          "x" direction for this data grid.
   */
  public Vector3D x_vec();

  /**
   *  Get the vector in the "y" direction for the local coordinate system 
   *  of the data grid.  If the data grid is non-planar, this will be a vector
   *  in the local "y" direction of a planar approximation to the data grid.
   *
   *  @return A vector giving the components of a vector in the local
   *          "y" direction for this data grid.
   */
  public Vector3D y_vec();

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
  public Vector3D z_vec();

  /**
   *  Get the nominal position of the center of the data grid.
   *
   *  @return A vector giving the nominal center of this data grid.
   */
  public Vector3D position();

  /**
   *  Get the nominal width of the data grid.  The width is the size of the
   *  grid in the direction given by x_vec().
   *
   *  @return the width of the data grid.
   */
  public float width();

  /**
   *  Get the nominal height of the data grid.  The height is the size of the
   *  grid in the direction given by y_vec().
   *
   *  @return the height of the data grid.
   */
  public float height();

  /**
   *  Get the nominal depth of the data grid.  The depth is the size of the
   *  grid in the direction given by z_vec().
   *
   *  @return the depth of the data grid.
   */
  public float depth();

  /**
   *  Get the number of rows in this data grid.  
   *
   *  @return the number of rows.
   */
  public int num_rows();

  /**
   *  Get the number of columns in this data grid.  
   *
   *  @return the number of columns.
   */
  public int num_cols();

  /**
   *  Find the offset in the direction of "x_vec()", from the center of the 
   *  grid to the specified position.  If row and col are integers, this 
   *  will be the offset to the center of the specified "box".  
   *
   *  @return the offset in the "x" direction to the specified position on
   *          the grid.
   */
  public float x( float row, float col );

  /**
   *  Find the offset in the direction of "y_vec()", from the center of the 
   *  grid to the specified position.  If row and col are integers, this 
   *  will be the offset to the center of the specified "box".  
   *
   *  @return the offset in the "y" direction to the specified position on
   *          the grid.
   */
  public float y( float row, float col );

  /**
   *  Find the "row" number for the specified position on the grid.  The 
   *  position is specified in terms of offsets from the center of the
   *  grid in the directions of "x_vec()" and "y_vec()".  
   *  The row value is returned as a float, to allow for 
   *  positions that are NOT at the exact center of a "box".  If an integer
   *  row number is needed, the value returned by this method should be
   *  rounded.
   *
   *  @return the fractional row number corresponding to the specified 
   *          position on the grid.
   */ 
  public float row( float x, float y );

  /**
   *  Find the "column" number for the specified position on the grid.  The 
   *  position is specified in terms of offsets from the center of the 
   *  grid in the directions of "x_vec()" and "y_vec()".  
   *  The column value is returned as a float, to allow for 
   *  positions that are NOT at the exact center of a "box".  If an integer
   *  column number is needed, the value returned by this method should be
   *  rounded.
   *
   *  @return the fractional column number corresponding to the specified 
   *          position on the grid.
   */ 
  public float col( float x, float y );

  /**
   *  Get the position in 3D of the specified point on the grid.  If the
   *  row and col values are integers, this will be the center point of
   *  a grid "box".  If row and/or col are not integers, the position
   *  returned will be offset from the center of the grid "box". 
   *
   *  @return A vector giving the position in 3D of the specified row
   *          and column values.
   */
  public Vector3D position( float row, float col );

  /**
   *  Get the width of the specified grid "box".  If row and col are not
   *  integers, they will be rounded to obtain integer values that 
   *  specify a particular grid "box".
   *
   *  @return The width of the specified grid "box" in the direction of 
   *          the "x_vec(row,col)".
   */
  public float width( float row, float col );

  /**
   *  Get the height of the specified grid "box".  If row and col are not
   *  integers, they will be rounded to obtain integer values that 
   *  specify a particular grid "box".
   *
   *  @return The height of the specified grid "box" in the direction of 
   *          the "y_vec(row,col)".
   */
  public float height( float row, float col );

  /**
   *  Get the depth of the specified grid "box".  If row and col are not
   *  integers, they will be rounded to obtain integer values that 
   *  specify a particular grid "box".
   *
   *  @return The depth of the specified grid "box" in the direction of 
   *          the "z_vec(row,col)".
   */
  public float depth( float row, float col );

  /**
   *  Get the vector in the "x" direction for a particular grid "box".
   *  If the data grid is planar, this may return the same vector as
   *  the x_vec() method.  If the data grid is non-planar, this will be 
   *  a vector in the local "x" direction for the particular grid "box".
   *
   *  @return A vector giving the components of a vector in the local
   *          "x" direction for this grid "box".
   */
  public Vector3D x_vec( float row, float col );

  /**
   *  Get the vector in the "y" direction for a particular grid "box".
   *  If the data grid is planar, this may return the same vector as
   *  the y_vec() method.  If the data grid is non-planar, this will be 
   *  a vector in the local "y" direction for the particular grid "box".
   *
   *  @return A vector giving the components of a vector in the local
   *          "y" direction for this grid "box".
   */
  public Vector3D y_vec( float row, float col );

  /**
   *  Get the vector in the "z" direction for a particular grid "box".
   *  If the data grid is planar, this will return the same vector as
   *  the z_vec() method.  If the data grid is non-planar, this will be 
   *  a vector in the local "z" direction for the particular grid "box".
   *
   *  @return A vector giving the components of a vector in the local
   *          "z" direction for this grid "box".
   */
  public Vector3D z_vec( float row, float col );


  /**
   *  This method goes through the Data blocks of a DataSet and records
   *  a reference to each of the Data blocks whose detector ID matches
   *  the ID of this DataGrid.  References to the Data blocks are recorded
   *  in a table, indexed by the row and column numbers.  This allows the
   *  Data to be accessed based on (row, col) pairs by the getData_entry
   *  method.  
   *
   *  @param  ds   The DataSet  from which references to Data blocks will
   *               be obtained.
   */
  public boolean setData_entries( DataSet ds );


  /**
   *  Get the Data block from one pixel, if references to the Data blocks
   *  have been set. 
   *
   *  @param  row   the row number of the Data block
   *  @param  col   the column number of the Data block
   *
   *  @return  The Data block corresponding to the specified row and column.
   *           If no Data entries have been set, or if row, col are out of
   *           bounds, this returns null.
   */
  public Data getData_entry( int row, int col );


  /**
   *  Check whether or not Data blocks have been set for each row and column
   *  of this DataGrid.
   *
   *  @return  true if Data entries have been set for all of the pixels
   *           and false if any pixel has a null Data block.
   */
  public boolean isData_entered();

  /**
   *  Clear all references to Data blocks.
   */
  public void clearData_entries();

  /**
   *  Get the solid angle subtended by this grid "box" from the origin. 
   *  If the row and column values are not integers, they will be rounded
   *  to obtain integer values that specify a particular grid "box".
   *  
   *  @return the solid angle subtended by the specified grid box.
   */
  public float SolidAngle( float row, float col );

  /**
   *  Get the approximate range of scattering angles subtended by the 
   *  specified grid "box".  If the row and column values are not integers, 
   *  they will be rounded to obtain integer values that specify a particular 
   *  grid "box".
   *
   *  @return the range of scattering angles for the specified grid box.
   */
  public float Delta2Theta( float row, float col );


} 

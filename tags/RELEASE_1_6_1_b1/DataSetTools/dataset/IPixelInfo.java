/*
 * File:  IPixelInfo.java
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
 *  Revision 1.2  2003/02/20 19:45:00  dennis
 *  Now implements Serializable.
 *
 *  Revision 1.1  2003/02/04 19:21:21  dennis
 *  Initial Version.
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import DataSetTools.math.*;

/**
 *   IPixelInfo defines an interface to information about the position, size
 * and orientation of a rectangular box representing a "point" measured
 * by a Data block.  Data blocks coming from an area detector or LPSD 
 * have a PixelInfoList attribute containing a list of IPixelInfo objects.  
 * Each of these IPixelInfo objects would be used to provide the position, 
 * size and orientation of the portion of the detector corresponding 
 * to that Data block.
 */  

public interface IPixelInfo extends Serializable
{
  /**
   *  Get the ID of the current pixel.  This ID should be unique within the
   *  set of all pixels on an instrument, such as the "segment ID" of the
   *  detector elements in an IPNS runfile.
   *
   *  @return a unique integer ID for this pixel.
   */
  public int ID();

  /**
   *  Get the ID of the data grid (i.e. detector) that contains this pixel.
   *
   *  @return the ID for the IDataGrid containing this pixel.
   */
  public int gridID();
  
  /**
   *  Get a reference to the data grid (i.e. detector) that contains this pixel.
   *
   *  @return a reference to the IDataGrid object that provides information 
   *          about this pixel.  If no data grid is used, this will return 
   *          null.
   */
  public IDataGrid DataGrid();

  /**
   *  Get the position in 3D of the center of this pixel.  
   *
   *  @return A vector giving the position in 3D of the center of the pixel.
   */
  public Vector3D position();

  /**
   *  Get the vector in the "x" direction for the local coordinate system
   *  of this pixel.
   *
   *  @return A vector giving the components of a vector in the local
   *          "x" direction for this pixel.
   */
  public Vector3D x_vec();

  /**
   *  Get the vector in the "y" direction for the local coordinate system
   *  of this pixel.
   *
   *  @return A vector giving the components of a vector in the local
   *          "y" direction for this pixel.
   */
  public Vector3D y_vec();

  /**
   *  Get the vector in the "z" direction for the local coordinate system
   *  of this pixel.
   *
   *  @return A vector giving the components of a vector in the local
   *          "z" direction for this pixel.
   */
  public Vector3D z_vec();

  /**
   *  Get the nominal width of this pixel.  The width is the size of the
   *  pixel in the direction given by x_vec().
   *
   *  @return the width of the pixel. 
   */
  public float width();

  /**
   *  Get the nominal height of this pixel.  The height is the size of the
   *  pixel in the direction given by y_vec().
   *
   *  @return the height of the pixel.
   */
  public float height();

  /**
   *  Get the nominal depth of this pixel.  The depth is the size of the
   *  pixel in the direction given by z_vec().
   *
   *  @return the depth of the pixel.
   */
  public float depth();

  /**
   *  Find the "row" number for this pixel on it's data grid.  If this is
   *  an actual pixel on a DataGrid, this will return an integer value. 
   *  If it is a "computed" pixel, the row number may be fractional.
   *
   *  @return the row number corresponding to this pixel.
   */
  public float row();

  /**
   *  Find the "column" number for this pixel on it's data grid.  If this is
   *  an actual pixel on a DataGrid, this will return an integer value. 
   *  If it is a "computed" pixel, the row number may be fractional.
   *
   *  @return the column number corresponding to this pixel.
   */
  public float col();

  /**
   *  Get the solid angle subtended by this pixel from the origin.
   * 
   *  @return the solid angle subtended by the pixel.
   */
  public float SolidAngle( );

  /**
   *  Get the approximate range of scattering angles subtended by this pixel.
   *
   *  @return the range of scattering angles for the specified pixel.
   */
  public float Delta2Theta( );
  
} 


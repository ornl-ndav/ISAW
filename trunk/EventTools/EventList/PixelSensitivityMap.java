/* 
 * File: PixelSensitivity.java
 *
 * Copyright (C) 2011, Dennis Mikkelson
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
 *  $Author:$
 *  $Date:$            
 *  $Revision:$
 */

package EventTools.EventList;

/**
 * This class holds basic information about the sensitivity of pixels in
 * a list of detectors modules.
 */
public class PixelSensitivityMap
{
  private int[]       my_bank_ids = null;
  private float[][][] my_factors = null;

/**
  * Construct a pixel sensitivity map for the specified list of detector ids
  * specified list of scale factors.  The supplied data is copied into arrays
  * maintained internally in this class.
  *
  *  @param bank_ids   List of bank ids for the detector modules
  *  @param factors    List of scale factors to apply.  The first index
  *                    varies from 0 to the number of banks-1.  The scale
  *                    factors for the data from bank number bank_id[k] is
  *                    in factors[k][][].  The second index is the row number
  *                    and the last index is the column number.
  */
  public PixelSensitivityMap( int[] bank_ids, float[][][] factors )
  {
    if ( bank_ids == null || bank_ids.length == 0 )
      throw new IllegalArgumentException(
                               "Array of bank ids is null or zero length");

    if ( factors == null || factors.length == 0 )
      throw new IllegalArgumentException(
                              "Array of scale factors is null or zero length");

    if ( bank_ids.length != factors.length )
      throw new IllegalArgumentException(
                          "Number of Ids and scale factor arrays don't match");

    for ( int i = 0; i < bank_ids.length; i++ )
      if ( factors[i] == null || factors[i].length == 0 )
        throw new IllegalArgumentException(
                     "Scale factor array for id " + bank_ids[i] +
                     " is null or zero length");
                                                       // copy id array
    my_bank_ids = new int[ bank_ids.length ];
    for ( int i = 0; i < my_bank_ids.length; i++ )
      my_bank_ids[i] = bank_ids[i];
                                                       // copy factors array
    my_factors = new float[ factors.length ][][];
    for ( int i = 0; i < my_bank_ids.length; i++ )
    {
      int n_rows = factors[i].length;
      int n_cols = factors[i][0].length;
      my_factors[i] = new float[ n_rows ][ n_cols ];
      for ( int row = 0; row < n_rows; row++ )
        System.arraycopy( factors[i][row], 0, my_factors[i][row], 0, n_cols );
    }
  } 

/**
 * Get a reference to the list of bank Ids.  The calling code must NOT modify
 * the values in the bank Id list.
 */
  public int[] getBankIDs()
  {
    return my_bank_ids;
  }

/**
 * Get a reference to the list of scale factors for all of the banks.  The 
 * calling code must NOT modify the values in the scale factors array.
 */
  public float[][][] getFactors()
  {
    return my_factors;
  }

}

/*
 * File:  DataArrayMerge_calc.java 
 *             
 * Copyright (C) 2008, Dennis Mikkelson
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Last Modified:
 *
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package Operators.Special;

import  java.util.Vector;
import  DataSetTools.dataset.*;

/**
  * This class contains methods to create a new DataSet by merging the 
  * Data blocks from all DataSets in an array (or Vector) of DataSets.
  * The attributes of the first DataSet in the array (or Vector) are used
  * for the DataSet attributes of the new DataSet.  The new DataSet is
  * populated by adding REFERENCES to the Data blocks from the DataSets 
  * in the array (or Vector).  This method does a SHALLOW COPY of the Data 
  * blocks from an array of DataSets to form the new DataSet.  These 
  * methods are helpful when loading a NeXus file that contains a large
  * number of NxData entries, corresponding to detector banks, if a large
  * number of the NxDatas are to be merged. 
  */

public class DataSetArrayMerge_calc 
{

  /* ---------------------------- merge ------------------------------- */
  /**
   *  Merge the Data blocks from an array of DataSets to form a new DataSet.
   *
   *  @param ds_array   Array of DataSets to merge.
   *
   *  @return A DataSet with attributes taken from the first DataSet and
   *          containing all of the Data blocks from all of the DataSets 
   *          in the array.  If the array is empty, the EMPTY_DATA_SET
   *          is returned.
   *
   *  @throws An IllegalArgumentException if the array is null, or contains
   *          a null.
   */
  public static DataSet merge( DataSet[] ds_array ) 
  {                             
    if ( ds_array == null )
      throw new IllegalArgumentException("Array of DataSets to merge is Null");

    if ( ds_array.length == 0 )
      return DataSet.EMPTY_DATA_SET;

    for ( int i = 0; i < ds_array.length; i++ )
      if ( ds_array[i] == null )
        throw new IllegalArgumentException("null entry in list to merge");

    int num_data;
    DataSet result_ds = ds_array[0].empty_clone();
    for ( int i = 0; i < ds_array.length; i++ )
    {
      num_data = ds_array[i].getNum_entries();
      for ( int j = 0; j < num_data; j++ )
        result_ds.addData_entry( ds_array[i].getData_entry(j) );
    }

    result_ds.addLog_entry( "Merged array of DataSets" );
    return result_ds;
  }


  /* ---------------------------- merge ------------------------------- */
  /**
   *  Merge the Data blocks from a Vector of DataSets to form a new DataSet.
   *
   *  @param ds_array   Vector containing the DataSets to merge.
   *
   *  @return A DataSet with attributes taken from the first DataSet and
   *          containing all of the Data blocks from all of the DataSets 
   *          in the Vector.  If the Vector is empty, the EMPTY_DATA_SET
   *          is returned.
   *
   *  @throws An IllegalArgumentException if the Vector is null, contains
   *          a null element, or contains an element that is not a DataSet.
   */

  public static DataSet merge( Vector ds_list )
  {
    if ( ds_list == null )
      throw new IllegalArgumentException("Vector of DataSets to merge is Null");

    for ( int i = 0; i < ds_list.size(); i++ )
      if ( !(ds_list.elementAt(i) instanceof DataSet) )
        throw new IllegalArgumentException(
                                "NON-DataSet in Vector of DataSets to merge");

    DataSet[] ds_array = new DataSet[ ds_list.size() ];
    for ( int i = 0; i < ds_array.length; i++ )
      ds_array[i] = (DataSet)ds_list.elementAt(i);

    return merge( ds_array ); 
  }


}

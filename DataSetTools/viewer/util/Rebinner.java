/*
 * File: Rebinner 
 *
 * Copyright (C) 2007, Dennis Mikkelson
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
 * number DMR-0426797 and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2007/07/11 18:43:11  dennis
 *  This is a utility class to take care of some common tasks
 *  related to rebinning of DataSets for visualization purposes.
 *
 */

package DataSetTools.viewer.util;


import DataSetTools.dataset.*;

/**
 * This class is responsible for maintaining a complete set of XScales and
 * rebinned values for a specified DataSet.  If a new XScale has not been
 * specified, these are just references to the XScales and values from the
 * DataSet.  If a new XScale has been specified, the new XScale will be used
 * to resample the data.
 */   

public class Rebinner 
{
  private DataSet   ds = null;        // The DataSet to be rebinned

  private float[][] ys = null;        // This array initially just contains
                                      // references to the y value arrays of 
                                      // the DataSet.  If an XScale is 
                                      // specified, it will be set to hold
                                      // copies of the data, rebinned to
                                      // the specified XScale

  private XScale[]  x_scales = null;  // This holds references to the x_scales
                                      // for the corresponding list of y_values.

  private boolean   same_x_scale;     // If this is set true, there is only one
                                      // x_scale for all the lists of y_values.

  private XScale    x_scale = null;   // The one common x_scale for all Data
                                      // blocks, if a common x_scale was 
                                      // specified.

 /**
  * Construct a Rebinner object to maintain rebinned information for the
  * specified DataSet.
  *
  * @param ds  The DataSet for rebinned data may be needed.
  */
  public Rebinner( DataSet ds )
  {
    this.ds = ds;
    same_x_scale = false;

    if ( ds == null )
      throw new IllegalArgumentException(
                      "DataSet null in Rebinner Constructor");

    int n_data = ds.getNum_entries();
    if ( n_data <= 0 )
      throw new IllegalArgumentException(
                      "DataSet has no entries in Rebinner Constructor");

    x_scales = new XScale[ n_data ];
    ys       = new float[n_data][];
  
    reset();
  }


 /**
  * Reset this rebinner to use the original XScales and y-values from
  * the DataSet.
  */
  public void reset()
  {
    Data d;
    int  n_data = ds.getNum_entries();

    for ( int i = 0; i < n_data; i++ )
    {
      d = ds.getData_entry(i);
      x_scales[i] = d.getX_scale();
      ys[i]       = d.getY_values();
    }

    x_scale = x_scales[0];
    same_x_scale = true;
    int i = 1;
    while ( i < n_data && same_x_scale )
    {
      if ( x_scale != x_scales[i] )
        same_x_scale = false;
      i++;
    }
  }


 /**
  * Set one common XScale to be used for all the Data blocks.  The data will
  * be resampled using the specified XScale.
  *
  * @param  x_scale  The XScale to use when getting rebinned data.
  */
  public void setXScale( XScale x_scale )
  {
    same_x_scale = true;
    this.x_scale = x_scale;

    Data d;
    int n_data = ds.getNum_entries();
    for ( int i = 0; i < n_data; i++ )
    {
      d = ds.getData_entry(i);
      ys[i] = d.getY_values( x_scale, IData.SMOOTH_NONE );
    }
  }


 /**
  * Get the (possibly rebinned) y-value corresponding to the specified
  * x value, in the specified Data block of the DataSet.
  *
  * @param  data_index  The index of the Data block in the DataSet
  * @param  x           The x value at which the Data value is needed
  *
  * @return the y-value at the specified x value in the Data block.  If
  *         the x value is outside of the interval covered by the XScale,
  *         or if the index is not valid, this returns 0.
  */
  public float getY_valueAtX( int data_index, float x )
  {
    int n_data = ds.getNum_entries();
    if ( data_index < 0 || data_index >= n_data )
      return 0;

    XScale my_x_scale;
    if ( same_x_scale )
      my_x_scale = x_scale;
    else
      my_x_scale = x_scales[ data_index ];

    int bin_number = my_x_scale.getI(x);
    if ( bin_number < 0 || bin_number >= ys[data_index].length )
      return 0;
    else
      return ys[data_index][bin_number];
  }


 /**
  * Get all of the (possibly rebinned) y-values corresponding to the specified
  * x value, in the specified Data block of the DataSet.
  *
  * @param  x   The x value at which the Data values are needed
  *
  * @return the y-value at the specified x value in the Data block.  If
  *         the x value is outside of the interval covered by an XScale,
  *         0 is recorded for the value of that Data block at the specified x.
  */
  public float[] getY_valuesAtX( float x )
  {
    int n_data = ds.getNum_entries();

    int bin_number = 0;
    XScale my_x_scale;
    if ( same_x_scale )
      bin_number = x_scale.getI(x);

    float[] y_vals = new float[ n_data ];
    for ( int i = 0; i < n_data; i++ )
    {
      if ( !same_x_scale )
        bin_number = x_scales[i].getI(x);

      if ( bin_number < 0 || bin_number >= ys[i].length )
        y_vals[i] = 0; 
      else
        y_vals[i] = ys[i][bin_number];
    }

    return y_vals;
  }



 /**
  *  Step through the list of rebinned data values to find the max and min.
  *
  *  @return An array with two entries, the min and max in that order.
  */
  public float[] getDataRange()
  {
    float min = ys[0][0];
    float max = min;

    float[] temp;
    float   val;
    for ( int i = 0; i < ys.length; i++ )
    {
      temp = ys[i];
      for ( int j = 0; j < temp.length; j++ )
      {
        val = temp[j];
        if ( val < min )
          min = val;
        else if ( val > max )
          max = val;
      }
    }

    float[] result = { min, max };
    return result;
  }
   

 /**
  *  Get the common XScale for all Data blocks, if there is one, or
  *  the XScale for the first Data block if there is not one.
  */

  public XScale getXScale()
  {
    if ( same_x_scale )
      return x_scale;

    return x_scales[0];
  }

} 

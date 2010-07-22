/*
 * File:  ClampValues_calc.java 
 *
 * Copyright (C) 2010, Dennis Mikkelson
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
 * Last Modified:
 * 
 * $Author$
 * $Date$            
 * $Revision$
 */
package Operators.Special;

import DataSetTools.dataset.*;

/**
 *  This class provides the calculation routine for the operator that 
 *  clamps all values in a DataSet to be above (or below) a specified
 *  level.  The most typical use of this is to force all values in a 
 *  DataSet to be strictly positive, so that it can be divided into 
 *  another DataSet without dividing by zero.
 */
public class ClampValues_calc
{
  /**
   *  Clamp all values in the specified DataSet to be above (or below)
   *  the specified level.  If error values have been specified, the error
   *  values at these points will be set to 1.
   *
   *  @param ds          The DataSet whose values are to be clamped.
   *  @param level       The level at which the values are to be clamped.
   *  @param is_min_val  Flag indicating whether the level is a minimum
   *                     or a maximum level for values in the DataSet.
   */
  public static void ClampValues( DataSet ds, float level, boolean is_min_val )
  {
    if ( ds == null || ds.getNum_entries() == 0 )
      throw new IllegalArgumentException("ERROR: null or empty DataSet");

    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      Data data = ds.getData_entry( i );

      if ( data instanceof TabulatedData )    // ignore an ModeledData Data
      {                                       // blocks since we can't set
                                              // individual values
        float[] vals = data.getY_values();
        float[] errs = data.getErrors();

        if ( is_min_val )
          for ( int j = 0; j < vals.length; j++ )
          {
            if ( vals[j] < level )
            {
              vals[j] = level;
              if ( errs != null )
                errs[j] = 1;
            }   
          }
        else
          for ( int j = 0; j < vals.length; j++ )
          {
            if ( vals[j] > level )
            {
              vals[j] = level;
              if ( errs != null )
                errs[j] = 1;
            }
          }

        if ( errs != null )
          ((TabulatedData)data).setErrors( errs );
      }
    }
  }

}

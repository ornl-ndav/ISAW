/*
 * File: FunctionModel.java 
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2002/04/11 21:05:24  dennis
 *  Now uses the OneVariableFunction classes from package
 *  DataSetTools.functions.  Also includes a main program
 *  as a basic test.
 *
 *  Revision 1.1  2002/04/04 19:47:00  dennis
 *  A function Data object whose values are
 *  determined by a function of one variable.
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import DataSetTools.math.*;
import DataSetTools.functions.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;

/**
 * This class defines Data objects that represent functions given by a 
 * OneVarFunction.
 */

public class FunctionModel extends    ModeledData
                           implements Serializable
{
  /**
   * Constructs a FunctionModel object by specifying an "X" scale, 
   * the OneVarFunction that generates the y_values and a group id for 
   * this data object.
   *
   * @param   x_scale   the list of x values for this Data object
   * @param   function  the IOneVarFunction that gnerates the y_values
   * @param   group_id  an integer id for this data object
   *
   */
  public FunctionModel( XScale x_scale, IOneVarFunction function, int group_id )
  {
    super( x_scale, function, group_id );
  } 

  /**
   * Constructs a FunctionData object by specifying an "X" scale, the 
   * OneVarFunctions that generates the y_values and errors, and a group id 
   * for this data object.
   *
   * @param   x_scale   the list of x values for this Data object
   * @param   function  the IModelFunction that gnerates the y_values
   * @param   group_id  an integer id for this data object
   *
   */
  public FunctionModel( XScale          x_scale, 
                        IOneVarFunction function, 
                        IOneVarFunction errors, 
                        int             group_id )
  { 
    super( x_scale, function, errors, group_id );
  }

  /**
    * Determine whether or not the current Data block has HISTOGRAM data.
    *
    * @return  false 
    */
  public boolean isHistogram()
  {
    return false;
  }

  /**
   * Returns a list of "Y" values obtained by evaluating the function at 
   * the current x_scale.
   *
   * @return A new array listing approximate y-values at the current x_scale.
   */
  public float[] getY_values()
  { 
    return getY_values( x_scale, smooth_flag );
  }

  /**
   *  Get a list of "Y" values for this Data object, by evaluating the
   *  function at the x values specified by the XScale.
   *
   *  @param  x_scale      The XScale to be used for evaluating the function.
   *  @param  smooth_flag  Flag indicating the type of smoothing to use,
   *                       as defined in IData. #### not currently implemented
   *                       
   *  @return  A new array listing approximate y-values corresponding to the
   *           given x-scale.  
   */
  public float[] getY_values( XScale x_scale, int smooth_flag )
  {
    float x_vals[] = x_scale.getXs();
    return function.getValues( x_vals );
  }

 /**
  *  Get the "Y" value by evaluating the function at the specified x_value.
  *
  *  @param  x_value      the x value where the function is evaluated.
  * 
  *  @param  smooth_flag  Flag indicating the type of smoothing to use,
  *                       as defined in IData. #### not currently implemented
  *
  *  @return approximate y value at the specified x value
  */
  public float getY_value( float x_value, int smooth_flag )
  {
    return function.getValue( x_value );
  }


  /**
   * Returns a reference to list of the error values for this function.  
   * If no error values have been set, this returns null.
   */
  public float[] getErrors()
  { 
    if ( use_sqrt_errors )
    {
      float y_vals[] = getY_values();
      float errs[] = new float[ y_vals.length ];
      for ( int i = 0; i < errs.length; i++ )
        errs[i] = (float)Math.sqrt( Math.abs( y_vals[i] ) );
      return errs; 
    }
    else if ( errors != null )
    {
      float x_vals[] = x_scale.getXs();
      return errors.getValues( x_vals ); 
    }
    else
      return null;
  }

  /**
   * Return a new FunctionModel object containing a copy of the x_scale, 
   * function, error function, group_id and attributes from the current 
   * FunctionModel object.
   */
  public Object clone()
  {
    FunctionModel temp = new FunctionModel(x_scale, function, errors, group_id);

                                      // copy the list of attributes.
    AttributeList attr_list = getAttributeList();
    temp.setAttributeList( attr_list );
    temp.selected = selected;
    temp.hide     = hide;

    return temp;
  }
 

  public static void main( String argv[] )
  {
    DataSet ds = new DataSet( "Sample Gaussian", "Initial Version" );

    XScale x_scale = new UniformXScale( -5, 5, 500 );
    OneVarFunction gaussian; 
    Data gaussian_data; 
    for ( int i = 0; i < 100; i++ )
    {
      gaussian      = new Gaussian( -5+i/10.0f, 1, 2 );
      gaussian_data = new FunctionModel( x_scale, gaussian, i );
      ds.addData_entry( gaussian_data );
    }

    ViewManager vm = new ViewManager( ds, IViewManager.IMAGE );
  }

}

/*
 * File: ModeledData.java 
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
 *  Revision 1.4  2002/05/29 22:46:19  dennis
 *  Minor fix to documentation.
 *
 *  Revision 1.3  2002/04/19 15:42:32  dennis
 *  Revised Documentation
 *
 *  Revision 1.2  2002/04/11 21:03:55  dennis
 *  Now uses the OneVariableFunction classes from package
 *  DataSetTools.functions.
 *
 *  Revision 1.1  2002/04/04 19:46:19  dennis
 *  Abstract base class for Data objects whose values are
 *  determined by a function of one variable.
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import DataSetTools.math.*;
import DataSetTools.functions.*;
import DataSetTools.util.*;

/**
 * The abstract base class for a data object whose values are determined by
 * a model function.  This class bundles together the basic methods necessary 
 * to describe a function or frequency histogram of one variable using a
 * model function.  An object of this class contains a model function object
 * and a list of "X" values that are to be used if a list of Y values is to
 * be generated.  A model function specifying the errors for the "Y" values 
 * can also be kept.
 */

public abstract class ModeledData extends    Data
                                  implements Serializable
{
  protected IOneVarFunction function        = null;
  protected IOneVarFunction errors          = null;
  protected boolean         use_sqrt_errors = false;
  protected int             smooth_flag     = IData.SMOOTH_NONE;

  /**
   * Constructs a ModeledData object by specifying an "X" scale, the 
   * ModelFunction that generates the y_values and a group id for this 
   * data object.
   *
   * @param   x_scale   the list of x values for this Data object
   * @param   function  the IModelFunction that gnerates the y_values
   * @param   group_id  an integer id for this data object
   *
   */
  public ModeledData( XScale x_scale, IOneVarFunction function, int group_id )
  {
    super( x_scale, group_id );
    this.function = function;
  } 

  /**
   * Constructs a ModeledData object by specifying an "X" scale, the 
   * ModelFunction that generates the y_values, a function that describes the
   * errors in the data and a group id for this data object.
   *
   * @param   x_scale   the list of x values for this Data object
   * @param   function  the IOneVarFunction that gnerates the y_values
   * @param   errors    the IOneVarFunction that describes the errors
   * @param   group_id  an integer id for this data object
   *
   */
  public ModeledData( XScale          x_scale, 
                      IOneVarFunction function, 
                      IOneVarFunction errors, 
                      int             group_id )
  { 
    super( x_scale, group_id );
    this.function = function;
    this.errors   = errors;
  }


 /**
   * Set the function defining the errors for this data object.
   *
   * @param   err     New OneVarFunction for the errors to use for this data 
   *                  object. 
   */ 
  public void setErrors( IOneVarFunction err )
  {
    use_sqrt_errors = false;
    errors = err;
  }

  /**
    * Set the error function for this data object to the square root of the
    * corresponding y value.
    */ 
  public void setSqrtErrors( )
  {
    errors = null;
    use_sqrt_errors = true; 
  }


  /**
   *  Resample the Data block on an arbitrarily spaced set of points given by
   *  the new_X scale parameter.  In this case (ModeledData) this just records
   *  a new x_scale that will be used when the function is evaluated.
   *
   *  @param new_X        The x scale giving the set of x values to use for the
   *                      resampling and/or rebinning operation.
   *  @param smooth_flag  Flag indicating the smoothing type to be used, 
   *                      as defined in IData.  #### not currently implemented
   */
  public void resample( XScale new_X, int smooth_flag )
  {
    x_scale = new_X;
    this.smooth_flag = smooth_flag;
  }


  public static void main( String argv[] )
  {
  }

}

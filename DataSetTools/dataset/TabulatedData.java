/*
 * File: TabulatedData.java 
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
 *  Revision 1.2  2002/03/18 21:38:21  dennis
 *  Minor fix to documentation.
 *
 *  Revision 1.1  2002/03/13 16:08:37  dennis
 *  Data class is now an abstract base class that implements IData
 *  interface. FunctionTable and HistogramTable are concrete derived
 *  classes for storing tabulated functions and frequency histograms
 *  respectively.
 *
 */

package  DataSetTools.dataset;

import java.util.Vector;
import java.io.*;
import DataSetTools.math.*;
import DataSetTools.util.*;

/**
 * The abstract base class for a tabulated function data object.  This class
 * bundles together the basic data necessary to describe a tabulated function 
 * or frequency histogram of one variable.  An object of this class contains 
 * a list of "X" values and a list of "Y" values together with an extensible 
 * list of attributes for the object.  A list of errors for the "Y" values 
 * can also be kept.
 *  
 * @see DataSetTools.dataset.IData
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.FunctionTable
 * @see DataSetTools.dataset.HistogramTable
 *
 * @version 1.0  
 */

public abstract class TabulatedData extends    Data
                                    implements Serializable
{
  protected float  y_values[];
  protected float  errors[];

  /**
   * Constructs a Data object by specifying an "X" scale, "Y" values
   * and a group id for that data object.  
   *
   * @param   x_scale   the list of x values for this Data object 
   * @param   y_values  the list of y values for this Data object
   * @param   group_id  an integer id for this data object
   *
   * @see DataSetTools.dataset.XScale
   * @see DataSetTools.dataset.DataSet
   * @see DataSetTools.dataset.Attribute
   */
  public TabulatedData( XScale  x_scale, float y_values[], int group_id )
  {
    super( x_scale, group_id );
    this.y_values = y_values;
    this.errors   = null;
  }

  /**
   * Constructs a Data object by specifying an "X" scale, 
   * "Y" values and an array of error values.
   *
   * @param   x_scale   the list of x values for this data object 
   * @param   y_values  the list of y values for this Data object
   * @param   errors    the list of error values for this data object.  The
   *                    length of the error list should be the same as the
   *                    length of the list of y_values.  
   * @param   group_id  an integer id for this data object
   *
   * @see DataSetTools.dataset.XScale
   * @see DataSetTools.dataset.DataSet
   * @see DataSetTools.dataset.Attribute
   */
  public TabulatedData( XScale x_scale, 
                        float  y_values[], 
                        float  errors[], 
                        int    group_id )
  {
    this( x_scale, y_values, group_id );
    this.setErrors( errors );
  }

  /**
   * Returns a reference to the list of "Y" values
   */
  public float[] getY_values()
  { 
    return y_values;
  }

/**
 *  Get a list of "Y" values for this Data object, resampled at the x
 *  values specified by the XScale.
 *
 *  @param  x_scale  The XScale to be used for resampling the Data block.
 *
 *  @return  A new array listing approximate y-values corresponding to the
 *           given x-scale.  If the Data block is a histogram, the x-scale
 *           is considered to list the bin-boundaries and there will be one
 *           more bin-boundary than y-values.  If the Data block is a function,
 *           there will be the same number of y-values as x-values.
 */
public float[] getY_values( XScale x_scale, int smooth_flag ) //#############
{
  float y_vals[];

  int   num_x   = x_scale.getNum_x();
  if ( num_x > 1 )                          // resample over non-degenerate
  {                                         // interval
    Data data = (Data)this.clone();
    data.resample( x_scale, smooth_flag );
    y_vals = data.getY_values();
  }
  else                                      // just one point, so evaluate
  {                                         // at that point
    y_vals = new float[1];
    y_vals[0] = getY_value( x_scale.getStart_x(), smooth_flag );  
  }

  return y_vals;
}


  /**
   * Returns a reference to the list of the error values.  If no error values 
   * have been set, this returns null.
   */
  public float[] getErrors()
  { 
    return errors;
  }


 /**
   * Set the error array for this data object by copying the values from
   * the specified array.  If there are more error values than y values in
   * the data object, only the first "y_values.length" entries from the
   * error list will be used.  If there are fewer error values than y values,
   * the errors for the remaining y values will be set to 0. 
   *
   * @param   err        Array of error bounds to be used for this data 
   *                     object. 
   */ 
  public void setErrors( float  err[] )
  {
    if ( err == null )
    {
      this.errors = null;
      return;
    }

    this.errors = new float[ this.y_values.length ];
    int length = Math.min( y_values.length, err.length );
    System.arraycopy( err, 0, this.errors, 0, length ); 
    for ( int i = length; i < this.errors.length; i++ )
      this.errors[i] = 0;
  }

  /**
    * Set the error array for this data object to the square root of the
    * corresponding y value.
    */ 
  public void setSqrtErrors( )
  {
    this.errors = new float[ this.y_values.length ];
    for ( int i = 0; i < this.errors.length; i++ )
    {
      if ( this.y_values[i] >= 0 )
        this.errors[i] = (float)Math.sqrt( this.y_values[i] );
      else
        this.errors[i] = (float)Math.sqrt( -this.y_values[i] );
    }
  }



  /**
    * Construct a new Data object by ADDING the specified value "x" to each
    * "y" value of the  current Data object.  The error in the value "x" 
    * is specified.  If the current Data object has an error array,
    * the error values will be combined with the specified error and
    * set in the new Data object.
    *
    * @param   x    The value to be added to the y values of the current 
    *               data object
    * @param   err  The error bound for the specified value "x".
    */

  public Data add( float x, float err )
  {
    TabulatedData temp = (TabulatedData)this.clone();
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] += x;
    
    if ( this.errors != null ) 
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( this.errors[i] * this.errors[i] +
                                                       err * err ); 
    else
      temp.errors = null;

    return temp; 
  }


  /**
    * Construct a new Data object by SUBTRACTING the specified value "x" 
    * from each "y" value of the  current Data object.  The error in the value 
    * "x" is specified.  If the current Data object has an error array,
    * the error values will be combined with the specified error and set
    * in the Data object.
    *
    * @param   x    The value to be added to the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "x".
    */

  public Data subtract( float x, float err )
  {
    TabulatedData temp = (TabulatedData)this.clone();
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] -= x;
    
    if ( this.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( this.errors[i] * this.errors[i] +
                                                       err * err );
    else
      temp.errors = null;

    return temp; 
  }


  /**
    * Construct a new Data object by MULTIPLYING the specified value "x"
    * times each "y" value of the  current Data object.  The error in the value
    * "x" is specified.  If the current Data object has an error array,
    * the error values will be combined with the specified error and set
    * in the new Data object. 
    *
    * @param   x    The value to be multiplied times the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "x".
    */

  public Data multiply( float x, float err )
  {
    TabulatedData temp = (TabulatedData)this.clone();
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] *= x;
    
    if ( this.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( 
                         this.errors[i] * x * this.errors[i] * x + 
                         err * this.y_values[i] * err * this.y_values[i] );
    else
      temp.errors = null;

    return temp; 
  }

  /**
    * Construct a new Data object by DIVIDING the specified value "x"
    * into each "y" value of the  current Data object.  The error in the value
    * "x" is specified.  If the current Data object has an error array,
    * the error values will be combined with the specified error and set in
    * the new Data object. 
    *
    * @param   x    The value to be divided into the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "x".
    */

  public Data divide( float x, float err )
  {
    if ( x == 0.0f )
      return null;

    TabulatedData temp = (TabulatedData)this.clone();
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] /= x;
    
    if ( this.errors != null ) 
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = temp.y_values[i] * (float) Math.sqrt( 
          this.errors[i] / this.y_values[i] * this.errors[i] / this.y_values[i]+
          err / x * err / x ); 
    else
      temp.errors = null;

    return temp;
  }


 /**
  *  Dump the index, x, y and error value to standard output
  *  
  *  @param first_index  The index of the first data point to dump
  *  @param last_index   The index of the last data point to dump
  *
  */
  public void print( int first_index, int last_index )
  {
    if ( first_index < 0 )
      first_index = 0;
    if ( first_index >= y_values.length )
      first_index = y_values.length - 1;

    if ( last_index < first_index )
      last_index = first_index;

    if ( last_index >= y_values.length )
      last_index = y_values.length - 1;

    float x[] = x_scale.getXs();
    for ( int i = first_index; i <= last_index; i++ )
    {
      System.out.print( Format.integer( i, 6 ) + " ");
      System.out.print( Format.real( x[i], 15, 6 ) + " " );
      System.out.print( Format.real( y_values[i], 15, 6 )+ " " );
      if ( errors != null )
        System.out.println( Format.real( errors[i], 15, 6 ) );
      else
        System.out.println();
    }
  } 


  public static void main( String argv[] )
  {
  }

}

/*
 * @(#)Data.java     1.01  98/07/31  Dennis Mikkelson
 *
 */

package  DataSetTools.dataset;

import java.util.Vector;
import java.io.*;
import DataSetTools.math.*;
import DataSetTools.util.*;

/**
 * The concrete root class for a tabulated function data object.  This class
 * bundles together the basic data necessary to describe a tabulated function 
 * of one variable.  An object of this class contains a list of "X" values 
 * and a list of "Y" values together with an extensible list of attributes 
 * for the object.  A list of errors for the "Y" values can also be kept.
 *  
 * @see DataSetTools.dataset.XScale
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.DataSet
 *
 * @version 1.0  
 */

public class Data implements IAttributeList,
                             Serializable
{
  private int            group_id;  //NOTE: we force a Data object to have an
                                    // id and also keep the same id as an
                                    // attribute.  The id can only be
                                    // changed through the setID() method.
  private XScale         x_scale;
  private float          y_values[];
  private float          errors[];
  private AttributeList  attr_list;

  /**
   * Constructs a tabulated data object by specifying an "X" scale, "Y" values
   * and a group id for that data object.  
   *
   * @param   x_scale   the list of x values for this data object 
   * @param   y_values  the list of y values for this data object 
   * @param   group_id  an integer id for this data object
   *
   * @see DataSetTools.dataset.XScale
   * @see DataSetTools.dataset.DataSet
   * @see DataSetTools.dataset.Attribute
   */
  public Data( XScale  x_scale, float   y_values[], int group_id )
  {
    int length = Math.min( x_scale.getNum_x(), y_values.length );
    if ( length < 1 )
      System.out.println( "Not enough data in class Data constructor" ); 

    this.x_scale  = (XScale) x_scale.clone();
    this.y_values = new float[ length ];
    this.errors   = null;
    System.arraycopy( y_values, 0, this.y_values, 0, length );
    this.attr_list = new AttributeList();
    setGroup_ID( group_id );
  }

  /**
   * Constructs a tabulated data object by specifying an "X" scale, 
   * "Y" values and an array of error values.
   *
   * @param   x_scale   the list of x values for this data object 
   * @param   y_values  the list of y values for this data object 
   * @param   errors    the list of error values for this data object.  The
   *                    length of the error list should be the same as the
   *                    length of the list of y_values.  
   * @param   group_id  an integer id for this data object
   *
   * @see DataSetTools.dataset.XScale
   * @see DataSetTools.dataset.DataSet
   * @see DataSetTools.dataset.Attribute
   */
  public Data( XScale  x_scale, float y_values[], float errors[], int group_id )
  {
    this( x_scale, y_values, group_id );
    this.setErrors( errors );
  }

  /**
   * Returns a reference to the "X" scale
   */
  public XScale getX_scale() { return x_scale; }


  /**
   * Returns a reference to the list of "Y" values
   */
  public float[] getY_values()
  { 
    return y_values;
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
   * Returns a reference to the list of the error values.  If no error values
   * have been set, this returns null.
   */
  public int getGroup_ID()
  {
    return group_id;
  }


  /**
   * Sets the id of the Data object both as an instance variable and as
   * an attribute of the Data object.
   *
   * @param  group_id   The id to use for the new group_id of the data object 
   */
  public void setGroup_ID( int group_id )
  {
    this.group_id = group_id;

    IntAttribute id_attr = new IntAttribute( Attribute.GROUP_ID, group_id );
    attr_list.setAttribute( id_attr, 0 );
  }


  /**
   * Returns a copy of the list of "Y" values
   */
  public float[] getCopyOfY_values() 
  { 
    float y[] = new float[ y_values.length ];
    System.arraycopy( y_values, 0, y, 0, y.length );
    return y;
  }

  /**
   * Returns a copy of the error values.  If no error values have been set, this
   * returns null.
   */
  public float[] getCopyOfErrors() 
  { 
    if ( errors == null )
      return( null );

    float err[] = new float[ this.errors.length ];
    System.arraycopy( this.errors, 0, err, 0, err.length );
    return err;
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
      this.errors[i] = (float)Math.sqrt( this.y_values[i]);
  }


  /**
   *  Get a reference to the list of attributes for this Data object.
   */
  public AttributeList getAttributeList()
  {
    return (AttributeList)attr_list.clone();
  }


  /**
   *  Set the list of attributes for this Data object to be a COPY of the
   *  specified list of attributes.
   */
  public void setAttributeList( AttributeList attr_list )
  {
    this.attr_list = (AttributeList)attr_list.clone();

    setGroup_ID( this.group_id );   // force the attribute list to contain the 
                                    // correct ID
  }



  /**
   * Return a new Data object containing a copy of the x_scale, y_values
   * errors, group_id and attributes from the current Data object.
   */
  public Object clone()
  {
    Data  temp = new Data( x_scale, y_values, errors, group_id );

                                      // copy the list of attributes.
    AttributeList attr_list = getAttributeList();
    temp.setAttributeList( attr_list );

    return temp;
  }

  /**
   * Alter this Data object by "rebinning" the y values of the current 
   * object to correspond to a new set of x "bins" given by the parameter
   * "new_X".  Also, rebin the error array.
   *
   * @param  new_X    This specifies the new set of "x" values to be used
   */
  public void ReBin( XScale new_X )
  {
                                           // Rebin the y_values
    float old_ys[] = arrayUtil.getPortion( y_values, x_scale.getNum_x() - 1 );
    float new_ys[] = new float[ new_X.getNum_x() - 1 ];
    tof_calc.ReBin( x_scale.getXs(), old_ys, new_X.getXs(), new_ys );
    y_values = new_ys;

    if ( errors != null )                 // Rebin the errors
    {
      float old_errors[] = arrayUtil.getPortion( errors, 
                                                 x_scale.getNum_x() - 1 );
      float new_errors[] = new float[ new_X.getNum_x() - 1 ];
      tof_calc.ReBin( x_scale.getXs(), old_errors, new_X.getXs(), new_errors );
      errors = new_errors;
    }
    x_scale  = (XScale)new_X.clone();
  }

  /**
    * Returns true or false depending on whether the two Data objects are
    * capable of being added, etc., based on the size of their value arrays,
    * and the size and extent of their x_scales.  If the XScales are NOT
    * uniform, each corresponding point of the XScales should probably be
    * compared, however, this is not currently done.  
    *
    *  @param  d     The Data object to be compared with the current data
    *                object.
    */

  public boolean compatible( Data d )
  {
    if ( this.y_values.length != d.y_values.length )
      return false;

    if ( this.x_scale.getNum_x() != d.x_scale.getNum_x() )
      return false;

    if ( this.x_scale.getStart_x() != d.x_scale.getStart_x() )
      return false;

    if ( this.x_scale.getEnd_x() != d.x_scale.getEnd_x() )
      return false;

    return true;  
  }

  /**
    * Construct a new Data object by ADDING corresponding "y" values of the
    * current Data object and the specified Data object d.  If both the 
    * current and the specified Data object d have error arrays, the errors
    * will propagate to the new Data object.  If the two Data objects cannot
    * be added (as determined by method "compatible") this method returns null.
    *
    * @param   d   The Data object to be added to the current data object
    *
    * @see compatible 
    */

  public Data add( Data d )
  {
    if ( ! this.compatible( d ) )
      return null;

    Data temp = (Data)this.clone();
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] += d.y_values[i];
    
    if ( this.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( this.errors[i] * this.errors[i] +
                                               d.errors[i] *    d.errors[i] ); 
    else
      temp.errors = null;

    temp.CombineAttributeList( d );
    return temp; 
  }

  /**
    * Construct a new Data object by SUBTRACTING corresponding "y" values of 
    * the current Data object and the specified Data object d.  If both the 
    * current and the specified Data object d have error arrays, the errors
    * will propagate to the new Data object.  If the two Data objects cannot
    * be subtracted (as determined by method "compatible") this method 
    * returns null.
    *
    * @param   d   The Data object to be subtracted from the current data 
    *              object
    *
    * @see compatible
    */

  public Data subtract( Data d )
  {
    if ( ! this.compatible( d ) )
      return null;

    Data temp = (Data)this.clone();
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] -= d.y_values[i];
    
    if ( this.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( this.errors[i] * this.errors[i] +
                                               d.errors[i] *    d.errors[i] ); 
    else
      temp.errors = null;

    temp.CombineAttributeList( d );
    return temp; 
  }

  /**
    * Construct a new Data object by MULTIPLYING corresponding "y" values of 
    * the current Data object and the specified Data object d.  If both the
    * current and the specified Data object d have error arrays, the errors
    * will propagate to the new Data object.  If the two Data objects cannot
    * be multiplied (as determined by method "compatible") this method 
    * returns null.
    *
    * @param   d   The Data object to be multiplied times the current data
    *              object
    *
    * @see compatible
    */

  public Data multiply( Data d )
  {
    if ( ! this.compatible( d ) )
      return null;

    Data temp = (Data)this.clone();
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] *= d.y_values[i];
    
    if ( this.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( 
              this.errors[i] * d.y_values[i] * this.errors[i] * d.y_values[i] + 
              d.errors[i] * this.y_values[i] * d.errors[i] * this.y_values[i] ); 
    else
      temp.errors = null;

    temp.CombineAttributeList( d );
    return temp; 
  }

  /**
    * Construct a new Data object by DIVIDING corresponding "y" values of
    * the current Data object and the specified Data object d.  If both the
    * current and the specified Data object d have error arrays, the errors
    * will propagate to the new Data object.  If the two Data objects cannot
    * be divided (as determined by method "compatible") this method
    * returns null.
    *
    * @param   d   The Data object to be divided into the current data
    *              object
    *
    * @see compatible
    */

  public Data divide( Data d )
  {
    if ( ! this.compatible( d ) )
      return null;

    Data temp = (Data)this.clone();
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] /= d.y_values[i];
    
    if ( this.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = temp.y_values[i] * (float) Math.sqrt( 
          this.errors[i] / this.y_values[i] * this.errors[i] / this.y_values[i]+
          d.errors[i] / d.y_values[i] * d.errors[i] / d.y_values[i] ); 
    else
      temp.errors = null;

    temp.CombineAttributeList( d );
    return temp;
  }

  /**
    * Construct a new Data object by ADDING the specified value "x" to each
    * "y" value of the  current Data object.  The error in the value "x"
    * is assumed to be 0.  If the current Data object has an error array,
    * the error values will propagate to the new Data object.
    *
    * @param   x   The value to be added to the y values of the current 
    *               data object
    */

  public Data add( float x )
  {
    return this.add( x, 0.0f );
  }

  /**
    * Construct a new Data object by ADDING the specified value "x" to each
    * "y" value of the  current Data object.  The error in the value "x" 
    * is specified.  If the current Data object has an error array,
    * the error values will propagate to the new Data object.
    *
    * @param   x    The value to be added to the y values of the current 
    *               data object
    * @param   err  The error bound for the specified value "x".
    */

  public Data add( float x, float err )
  {
    Data temp = (Data)this.clone();
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
    * "x" is assumed to be 0.  If the current Data object has an error array,
    * the error values will propagate to the new Data object.
    *
    * @param   x   The value to be SUBTRACTED from the y values of the current 
    *               data object
    */

  public Data subtract( float x )
  {
    return this.subtract( x, 0.0f );
  }

  /**
    * Construct a new Data object by SUBTRACTING the specified value "x" 
    * from each "y" value of the  current Data object.  The error in the value 
    * "x" is specified.  If the current Data object has an error array,
    * the error values will propagate to the new Data object.
    *
    * @param   x    The value to be added to the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "x".
    */

  public Data subtract( float x, float err )
  {
    Data temp = (Data)this.clone();
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
    * "x" is assumed to be 0.  If the current Data object has an error array,
    * the error values will propagate to the new Data object.
    *
    * @param   x   The value to be multiplied times the y values of the current
    *              data object
    */

  public Data multiply( float x )
  {
    return this.multiply( x, 0.0f );
  }

  /**
    * Construct a new Data object by MULTIPLYING the specified value "x"
    * times each "y" value of the  current Data object.  The error in the value
    * "x" is specified.  If the current Data object has an error array,
    * the error values will propagate to the new Data object.
    *
    * @param   x    The value to be multiplied times the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "x".
    */

  public Data multiply( float x, float err )
  {
    Data temp = (Data)this.clone();
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
    * "x" is assumed to be 0.  If the current Data object has an error array,
    * the error values will propagate to the new Data object.
    *
    * @param   x   The value to be divided into the y values of the current
    *              data object
    */

  public Data divide( float x )
  {
    return this.divide( x, 0.0f );
  }

  /**
    * Construct a new Data object by DIVIDING the specified value "x"
    * into each "y" value of the  current Data object.  The error in the value
    * "x" is specified.  If the current Data object has an error array,
    * the error values will propagate to the new Data object.
    *
    * @param   x    The value to be divided into the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "x".
    */

  public Data divide( float x, float err )
  {
    if ( x == 0.0f )
      return null;

    Data temp = (Data)this.clone();
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
   * Combine the attribute list of the specified Data object with the attribute
   * list of the current Data object to obtain a new attribute list for the
   * current Data object.
   *
   *  @param  d    The Data object whose attribute list is to be combined
   *               with the current object's attribute list
   */  
   public void CombineAttributeList( Data d )
   {
     attr_list.combine( d.getAttributeList() );
   }

  /**
   *  Return the x,y values of this Data object in one array of floats.
   *  The x values are in even numbered positions and, the y values are 
   *  in odd numbered positions.  This was needed for the graph class and
   *  was added the Data object 8/5/98 by Dahr.  
   */
  public double[] getGraphDataList()
  {
    float[] f = x_scale.getXs();
  
    int size = 0;
    if(f.length < y_values.length)
    {
        size = f.length;
    }
    else
    {
        size = y_values.length;
    }
    double[] d = new double[2*size];
    for(int i = 0; i<d.length - 1; i += 2)
    {
        d[i] = f[i/2];
    }
    for(int j = 1; j<d.length; j += 2)
    {
        d[j] = y_values[(int)(j/2 + 1)];
    }
    return d;
  }

  /**
   *  Provide an identifier string for this Data block
   */
  public String toString()
  {
    Attribute attr;
      return "Group ID " + group_id;
  }


}

/*
 * @(#)Data.java     1.1  2000/04/25  Dennis Mikkelson
 *
 * Modified:  
 *
 *    2000/03/10 Dennis Mikkelson  Added selection and hidden flags and methods
 *
 *    2000/04/25 Dennis Mikkelson  Added getY_value( x )  to interpolate a
 *                                 y value at the specified x value
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.5  2000/07/31 20:54:48  dennis
 *  Added CLSmooth routine to smooth function Data
 *
 *  Revision 1.4  2000/07/26 19:16:49  dennis
 *  Adding two Data blocks now also adds the values of attributes:
 *  NUMBER_OF_PULSES and TOTAL_COUNT.
 *
 *  Revision 1.3  2000/07/10 22:23:53  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.23  2000/06/08 15:12:28  dennis
 *  Added wrapper methods to directly set/get attributes without getting the
 *  entire list of attributes.
 *
 *  Revision 1.22  2000/06/01 21:14:34  dennis
 *  Fixed error in documentation
 *
 *  Revision 1.21  2000/05/12 15:55:42  dennis
 *  Modified to take advantage of IMMUTABLE XScales.  The constructor now
 *  does NOT clone the XScale it was given, but just saves the reference to
 *  it.  Also, the ReBin method now just records a reference to the specified
 *  XScale, it does not make a clone of it.
 *
 *  Revision 1.20  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
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
                                           // each Data block is tagged as
  private long           selected = 0;     // selected by giving it a non-zero 
  static  private long   select_count = 1; // selected value.  The value is
                                           // value of select_count after
                                           // incrementing select_count.
                                           // selected == select_count
                                           // for the most recently selected
                                           // Data block.
  private boolean        hide = false;

  /**
   * Constructs a Data object by specifying an "X" scale, "Y" values
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

    this.x_scale  = x_scale;
    this.y_values = new float[ length ];
    this.errors   = null;
    System.arraycopy( y_values, 0, this.y_values, 0, length );
    this.attr_list = new AttributeList();
    setGroup_ID( group_id );
    selected = 0; 
    hide     = false;
  }

  /**
   * Constructs a Data object by specifying an "X" scale, 
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
   *  Set the "hide" flag of this Data block to the specified value.
   * 
   *  @param  hide  New value for the hidden flag.
   */
  public void setHide( boolean hide )
  {
    this.hide = hide;
  }

  /**
   *  Toggle the "hide" flag of this Data block to the opposite state that
   *  it is currently in.
   */
  public void toggleHide()
  {
    hide = !hide;
  }

  /**
   *  get the "hide" flag of this Data block.
   *
   *  @return  true if this Data block is marked "hide", false otherwise.
   */
  public boolean isHidden( )
  {
    return hide;
  }



  /**
   *  Set the selected flag of this Data block to the specified value.
   *
   *  @param  selected  New value for the selected flag. 
   */
  public void setSelected( boolean selected )
  {
    if ( selected )
    {
      select_count++;
      this.selected = select_count;
    }
    else
      this.selected = 0;
  } 

  /**
   *  Set the selected flag of this Data block to same value as the selected
   *  flag of the specified Data block.
   *
   *  @param  d  The Data block whose selected flag is to be copied.
   */
  public void setSelected( Data d )
  {
    this.selected = d.selected;
  }


  /**
   *  Toggle the selected flag of this Data block to the opposite state that
   *  it is currently in.
   */
  public void toggleSelected()
  {
    if ( isSelected() )
      setSelected( false );
    else
      setSelected( true );
  }


  /**
   *  get the selected flag of this Data block.
   *
   *  @return  true if this Data block is selected, false otherwise.
   */
  public boolean isSelected( )
  {
    if ( selected != 0 )
      return true;
    else
      return false;
  }

  /**
   *  Determine if this is the most recently selected Data block. 
   *
   *  @return  true if this Data block is the most recently selected,
   *  otherwise return false.
   */
  public boolean isMostRecentlySelected( )
  {
    if ( selected == select_count )
      return true;
    else
      return false;
  }

  /**
   *  Get the value of the selection count for this Data block. 
   *
   *  @return  The value of the selection count for this Data block.
   */
  public long getSelectionTagValue()
  {
    return selected;
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
 *  Get the y value corresponding to the specified x_value in this Data
 *  block. If the x_value is outside of the interval of x values
 *  for the Data, this returns 0.  In other cases, the tablulated y values are
 *  interpolated to obtain an approximate y value at the specified x value.
 *
 *  @param  x_value      the x value for which the corresponding y value is to
 *                       be interpolated
 *
 *  @return interpolated y value at the specified x value
 */
public float getY_value( float x_value )
{
  
  if ( x_value < x_scale.getStart_x() || 
       x_value > x_scale.getEnd_x()    )
    return 0.0f;

  float x_vals[] = x_scale.getXs();

  int first = 0;                         // do a binary search for the x_value
  int last  = x_vals.length-1;           // in the list of x_vals[]
  int mid = 0;
  boolean found = false;

  while ( !found && first <= last )
  {
    mid   = (first + last) / 2;
    if ( x_value == x_vals[mid] )
      found = true;
    else if ( x_value < x_vals[mid] )
      last = mid - 1;
    else if ( x_value > x_vals[mid] )
      first = mid + 1;
  }

  if ( found )                          // if exact value is in list, return
    return y_values[mid];               // the corresponding y value.

  float x1 = x_vals[last];              // first & last have crossed
  float x2 = x_vals[first];

  if ( y_values.length < x_vals.length || x1 == x2 )  // histogram, or
    return y_values[last];                            // duplicate x values

  float y1 = y_values[last];                         // otherwise, interpolate
  float y2 = y_values[first];
  return y1 + ( x_value - x1 )*( y2 - y1 ) / ( x2 - x1 );
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
   *  Get a copy of the list of attributes for this Data object.
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
   * Gets the number of attributes set for this object.  
   */
  public int getNum_attributes()
  {
    return attr_list.getNum_attributes();
  }


  /**
   * Set the value of the specified attribute in the list of attributes.
   * If the attribute is already present in the list, the value is changed
   * to the value of the new attribute.  If the attribute is not already
   * present in the list, the new attribute is added to the list.
   *
   *  @param  attribute    The new attribute to be set in the list of
   *                       attributes.
   */
  public void setAttribute( Attribute attribute )
  {
    attr_list.setAttribute( attribute );
  }
  

  /**
   * Set the value of the specified attribute in the list of attributes at
   * at the specified position.  If the attribute is already present in the
   * list, the value is changed to the value of the new attribute.  If the
   * attribute is not already present in the list, the new attribute is added
   * to the list.
   *
   *  @param  attribute    The new attribute to be set in the list of
   *                       attributes.
   *
   *  @param  index        The position where the attribute is to be
   *                       inserted.
   */
  public void setAttribute( Attribute attribute, int index )
  {
    attr_list.setAttribute( attribute, index );
  }


  /**
   * Get the value of the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   *
   * @param  name  The name of the attribute value to get.
   */
  public Object  getAttributeValue( int index )
  {
    return attr_list.getAttributeValue( index );
  }


  /**
   * Get the value of the attribute with the specified name from the list of
   * attributes.  If the named attribute is not in the list, this returns null.
   *
   * @param  name  The name of the attribute value to get.
   */
  public Object getAttributeValue( String name )
  {
    return attr_list.getAttributeValue( name );
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
    temp.selected = selected;
    temp.hide     = hide;

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
    x_scale  = new_X;
  }


  /**
   * Smooth this Data object by resampling/averaging the y values and errors
   * of the current data so that there are roughly the specified number of
   * samples of the data.
   *
   * @param  num_X    This specifies the approximate number of "x" values to 
   *                  be used
   */
  public void CLSmooth( int num_X )
  {
    boolean function;
    float   old_x[] = null;

    if ( x_scale.getNum_x() == y_values.length )          // function
    {
      function = true;
      old_x  = x_scale.getXs();
    }
    else if (  x_scale.getNum_x() == y_values.length+1 )  // histogram 
    {                                                     // so use bin centers
      function = false;                                   // for the x values
      float temp_x[] = x_scale.getXs();
      old_x = new float[ temp_x.length -1 ];
      for ( int i = 0; i < old_x.length; i++ )
        old_x[i] = (temp_x[i] + temp_x[i+1]) / 2;
    }
    else
    {
      System.out.println("ERROR: invalid Data block in CLSmooth");
      return;
    }
                                                        // now do the smoothing
   if ( errors != null )
     num_X = tof_calc.CLSmooth( old_x, y_values, errors, num_X );
   else
     num_X = tof_calc.CLSmooth( old_x, y_values, num_X );

                                                // put the y values and
                                                // errors in right size arrays
   float new_y[]   = new float[num_X];
   System.arraycopy( y_values, 0, new_y, 0, num_X );
   y_values = new_y;

   if ( errors != null )
   {
     float new_err[]   = new float[num_X];
     System.arraycopy( errors, 0, new_err, 0, num_X );
     errors = new_err;
   }
                                                // copy over the x values, or
                                                // synthesize bin boundaries
                                                // as needed and make XScale
   if ( function )
   {
     float new_x[] = new float[num_X];
     System.arraycopy( old_x, 0, new_x, 0, num_X );
     x_scale = new VariableXScale( new_x );
   }
   else                                    
   {
     float new_x[] = new float[num_X+1];              

     new_x[0] = old_x[0] - ( old_x[1] - old_x[0] ) / 2; 

     for ( int i = 1; i < num_X; i++ ) 
       new_x[i] = ( old_x[i-1] + old_x[i] ) / 2;

     new_x[num_X] = old_x[num_X-1] + ( old_x[num_X-1] - old_x[num_X-2] ) / 2;

     x_scale = new VariableXScale( new_x );
   }

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
    * Also see the documentation for the method "compatible".
    *
    * @param   d   The Data object to be added to the current data object
    *
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

                                         // now take car of attributes... most
                                         // will use the default combine method
                                         // but some must really be added
    temp.CombineAttributeList( d );
    temp.attr_list.add( Attribute.NUMBER_OF_PULSES,
                        this.getAttributeList(),  
                        d.getAttributeList()    );
    temp.attr_list.add( Attribute.TOTAL_COUNT,
                        this.getAttributeList(),  
                        d.getAttributeList()    );
    return temp; 
  }

  /**
    * Construct a new Data object by SUBTRACTING corresponding "y" values of 
    * the current Data object and the specified Data object d.  If both the 
    * current and the specified Data object d have error arrays, the errors
    * will propagate to the new Data object.  If the two Data objects cannot
    * be subtracted (as determined by method "compatible") this method 
    * returns null.
    * Also see the documentation for the method "compatible".
    *
    * @param   d   The Data object to be subtracted from the current data 
    *              object
    *
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
    * Also see the documentation for the method "compatible".
    *
    * @param   d   The Data object to be multiplied times the current data
    *              object
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
    * Also see the documentation for the method "compatible".
    *
    * @param   d   The Data object to be divided into the current data
    *              object
    */

  public Data divide( Data d )
  {
    if ( ! this.compatible( d ) )
      return null;

    Data temp = (Data)this.clone();
    for ( int i = 0; i < temp.y_values.length; i++ )
      if ( d.y_values[i] > 0.01 )                           // D.M. 6/7/2000
        temp.y_values[i] /= d.y_values[i];
      else
        temp.y_values[i] = 0;
    
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
    * the error values will be combined with the specified error and
    * set in the new Data object.
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
    * the error values will be combined with the specified error and set
    * in the Data object.
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
    * the error values will be combined with the specified error and set
    * in the new Data object. 
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
   *  Return the x,y values of this Data object in one array of doubles.
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
        d[i]   = f[i/2];
        d[i+1] = y_values[i/2];
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


  /**
   *  Trace the finalization of objects
   */
/*  protected void finalize() throws IOException
  {
    System.out.println( "finalize Data" );
  }
*/
}

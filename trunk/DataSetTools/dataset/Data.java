/*
 * File: Data.java 
 *
 * Copyright (C) 1999, Dennis Mikkelson
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
 *  Revision 1.15  2001/07/02 16:41:40  dennis
 *  Added methods:
 *    getAttribute( index )
 *    getAttribute( name )
 *
 *  Revision 1.14  2001/04/25 19:03:32  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.13  2001/04/20 20:16:15  dennis
 *  Added code to Data.java to handle resampling when the original
 *  function has only one data point.  This fixed an index out of
 *  bounds problem triggered by using the TrueAngle operator.  Also
 *  fixed a problem with resampling for the degenerate case when
 *  the intervals didn't overlap.
 *
 *  Revision 1.12  2000/12/15 05:19:45  dennis
 *  Now Resamples the second Data block when doing +,-,*,/
 *  of two Data blocks, if the x scales don't match.
 *
 *  Revision 1.11  2000/12/15 03:52:15  dennis
 *  Use methods isHistogram() and isFunction() to check type of Data block.
 *
 *  Revision 1.10  2000/12/07 22:17:07  dennis
 *  Added methods isHistogram(),
 *                isFunction(),
 *                Stitch(),
 *                ConvertToHistogram(),
 *                Resample()
 *  Minor improvements to ConvertToFunction(),
 *                        setSqrtErrors()
 *
 *  Revision 1.9  2000/08/03 15:48:40  dennis
 *  Added ConvertHistogramToFunction() method
 *
 *  Revision 1.8  2000/08/02 01:34:22  dennis
 *  Added ResampleUniformly() to generalize ReBin().  ReBin only applies to
 *  histograms, ResampleUniformly applies to both histgrams and tabulated
 *  functions.  For histograms, it just calls ReBin.  For tabulated functions,
 *  CLSmooth is called and then the points are resampled to a uniform grid of
 *  x values by interpolation.
 *
 *  Revision 1.7  2000/08/01 19:10:37  dennis
 *  Data.ReBin now calls error handling version of Sample.ReBin() if
 *  the Data block has non-null errors.  Also, added crude print() routine
 *  to print x,y,err values for debugging.
 *
 *  Revision 1.6  2000/08/01 01:32:13  dennis
 *  Changed getAttributeList() to return null if the current list is null.
 *
 *  Revision 1.5  2000/07/31 20:54:48  dennis
 *  Added CLSmooth routine to smooth function Data
 *
 *  Revision 1.4  2000/07/26 19:16:49  dennis
 *  Adding two Data blocks now also adds the values of attributes:
 *  NUMBER_OF_PULSES and TOTAL_COUNT.
 *
 *  Revision 1.3  2000/07/10 22:23:53  dennis
 *  Now using CVS 
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
 *    2000/04/25 Dennis Mikkelson  Added getY_value( x )  to interpolate a
 *                                 y value at the specified x value
 *
 *    2000/03/10 Dennis Mikkelson  Added selection and hidden flags and methods
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
  public static final int KEEP    = 1; 
  public static final int AVERAGE = 2;
  public static final int DISCARD = 3;

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

  if ( isHistogram() || x1 == x2 )                    // histogram, or
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
    {
      if ( this.y_values[i] >= 0 )
        this.errors[i] = (float)Math.sqrt( this.y_values[i] );
      else
        this.errors[i] = (float)Math.sqrt( -this.y_values[i] );
    }
  }


  /**
    * Determine whether or not the current Data block has HISTOGRAM data.
    * HISTOGRAM data records bin boundaries and a number of counts in each
    * bin, so the number of x-values is one more than the number of y-values.
    *
    * @return  true if the number of x-values is one more than the number
    *          of y-values.
    */
  public boolean isHistogram()
  {
    if ( x_scale.getNum_x() == y_values.length + 1 )
      return true;
    else
      return false;
  }  
 
  /**
    * Determine whether or not the current Data block has FUNCTION data.
    * FUNCTION data records a y-value at each x-value, so the number of 
    * x-values is equals the number of y-values.
    *
    * @return  true if the number of x-values is the same as the number of 
    *          y-values.
    */
  public boolean isFunction()
  {
    if ( x_scale.getNum_x() == y_values.length )
      return true;
    else
      return false;
  } 


  /**
   *  Get a copy of the list of attributes for this Data object.
   */
  public AttributeList getAttributeList()
  {
    if ( attr_list != null )
      return (AttributeList)attr_list.clone();
    else
      return null;
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
   * Get the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   *
   * @param  index  The position of the attribute in the list of attributes.
   */
  public Attribute getAttribute( int index )
  {
    return attr_list.getAttribute( index );
  }


  /**
   * Get the attribute with the specified name from the list of
   * attributes.  If the named attribute is not in the list, this
   * returns null.
   *
   * @param  name  The name of the attribute value to get.
   */
  public Attribute getAttribute( String name )
  {
    return attr_list.getAttribute( name );
  }


  /**
   * Get the value of the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   *
   * @param  index  The position of the attribute in the list of attributes.
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
   *    "Stitch" another Data block together with the current Data block to form
   *  a new Data block with the same attributes as the current Data block,
   *  but whose data is a combination of the two.  
   *
   *    The other Data block will first be converted to the same type of
   *  Data ( HISTOGRAM or FUNCTION ) as the current Data block.  Then a new
   *  XScale is formed and the y-values from the two Data blocks are "stitched"
   *  together over the new XScale.  The XScale for the new Data object covers 
   *  an interval containing the union of the current Data's XScale and the 
   *  other Data's XScale.
   *
   *    If the XScale of the current Data block is a uniform XScale, the new  
   *  XScale will be a uniform XScale with the same spacing as the current 
   *  XScale, aligned with the current XScale.  If the XScale of the current
   *  Data block is a VariableXScale, the new XScale will be a VariableXScale
   *  using the same x-values as the current Data's for the full extent of the
   *  current Data's XScale.  In this case, the new VariableXScale will only
   *  use the other_data's x-values for the interval covered by it's XScale
   *  and NOT covered by the current Data's XScale.
   *
   *    The other_data object will be resampled over the new XScale before 
   *  forming the y-values.  For the portion of the new XScale covered by 
   *  only the current Data, the current Data's y-values are used.  For the  
   *  portion of the new XScale covered by only the other Data's XScale, the 
   *  other Data's y-values are used.  The y-values for portions of the 
   *  intervals that overlap are selected from the current Data or the other 
   *  Data's y-values, or the average of the y-values, as determined by the 
   *  "overlap" parameter. 
   *
   *  @param  other_data  The other Data block whose data is to be combined
   *                      with the current data. 
   *  @param  overlap     Flag that indicates what should be done on the
   *                      interval where the two Data blocks overlap ( if any ).
   *                      This must be one of the constants:
   * 
   *                              Data.KEEP 
   *                              Data.AVERAGE 
   *                              Data.DISCARD
   *
   *                      indicating that the original Data's values should be
   *                      kept, averaged with the other Data's values or 
   *                      discarded and the other Data's values used instead.
   *
   *  @return  A new Data object with the same attributes as the current 
   *           Data object, but whose data is a combination of the two.  The
   *           XScale for the new DataObject 
   */
  public Data Stitch( Data other_data, int overlap )
  {
    Data  temp_data = (Data)other_data.clone();
    Data  new_data  = (Data)this.clone();

                                          // adjust temp_data to be of the same
                                          // type ( histogram or function ) as
                                          // new_data.
    if ( new_data.isFunction() && temp_data.isHistogram() )
      temp_data.ConvertToFunction( false );

    else if ( new_data.isHistogram() && temp_data.isFunction() )
      temp_data.ConvertToHistogram( false );

                                         // set error values for new_data if
                                         // needed and not present 
    boolean has_error_info = false;  
    if ( new_data.errors != null ) 
    {
      has_error_info = true;
      if ( temp_data.errors == null )   // if no previouse error values set,
        temp_data.setSqrtErrors();      // use the sqrt of number of counts
    }
                                        // record the extent of the original
                                        // XScales
    ClosedInterval interval = new ClosedInterval( new_data.x_scale.getStart_x(),
                                                  new_data.x_scale.getEnd_x() );
    ClosedInterval other_interval = 
                           new ClosedInterval( temp_data.x_scale.getStart_x(),
                                               temp_data.x_scale.getEnd_x() );

                                          // extablish a new XScale as the
                                          // common XScale for the Data blocks
    XScale new_x_scale = new_data.x_scale.extend( temp_data.x_scale );
    temp_data.Resample( new_x_scale );
    new_data.Resample( new_x_scale );
                                          // Now combine the y_values and errors
    float x[] = new_x_scale.getXs();
    if ( new_data.isFunction() )                   // use one x value to decide
      for ( int i = 0; i < new_data.y_values.length; i++ )
      {
        if ( interval.contains( x[i] ) )             // keep the current y_value
        {                                            // or alter it if necessary
          if ( other_interval.contains( x[i] ) )
            if ( overlap == Data.AVERAGE )
            {
              new_data.y_values[i] = ( new_data.y_values[i] + 
                                       temp_data.y_values[i] )/2;
              if ( has_error_info )
                new_data.errors[i] = (float)Math.sqrt( 
                                  new_data.errors[i] * new_data.errors[i] +
                                 temp_data.errors[i] * temp_data.errors[i] )/2;
            }
            else if ( overlap == Data.DISCARD )
            {
              new_data.y_values[i] = temp_data.y_values[i];
              if ( has_error_info )
                new_data.errors[i] = temp_data.errors[i];
            }
        } 

        else if ( other_interval.contains( x[i] ) )  // use the other y_value
        { 
          new_data.y_values[i] = temp_data.y_values[i];
          if ( has_error_info )
            new_data.errors[i] = temp_data.errors[i];
        }
        else
        {
          new_data.y_values[i] = 0;                  // neither interval
          if ( has_error_info )
            new_data.errors[i] = 0;
        } 
      }

    else if ( new_data.isHistogram() )           // use bin boudaries to decide
      for ( int i = 0; i < new_data.y_values.length; i++ )
      {
        if ( interval.contains( x[i] ) && 
             interval.contains( x[i + 1] ) )         // keep the current y_value
        {                                            // or alter it if necessary
          if ( other_interval.contains( x[ i ] ) && 
               other_interval.contains( x[ i + 1 ] ) )                         
            if ( overlap == Data.AVERAGE )
            {
              new_data.y_values[i] = ( new_data.y_values[i] + 
                                       temp_data.y_values[i] )/2;
              if ( has_error_info )
                new_data.errors[i] = (float)Math.sqrt( 
                                  new_data.errors[i] * new_data.errors[i] +
                                 temp_data.errors[i] * temp_data.errors[i] )/2;
            }   
            else if ( overlap == Data.DISCARD )
            {
              new_data.y_values[i] = temp_data.y_values[i];
              if ( has_error_info )
                new_data.errors[i] = temp_data.errors[i];
            }
        }

        else if ( other_interval.contains( x[i] ) &&
                  other_interval.contains( x[i+1] ) ) // use the other y_value
        {
          new_data.y_values[i] = temp_data.y_values[i]; 
          if ( has_error_info )
            new_data.errors[i] = temp_data.errors[i];
        }
        else
        {
          new_data.y_values[i] = 0;                  // neither interval
          if ( has_error_info )
            new_data.errors[i] = 0;
        }
      }
 
    return new_data;
  }



  /**
   *  Convert the Data to tablulated function Data if it is currently 
   *  histogram Data.  To do this, consider the histogram values to be 
   *  samples of the function at the center of the histogram bin.  To 
   *  divide by the bin width, to properly convert from a histogram to a
   *  density function, the "divide" flag must be passed in as true.  
   *
   *  @param  divide    Flag that indicates whether the histogram values
   *                    should be divided by the width of the histogram bin.
   */
  public void ConvertToFunction( boolean divide )
  {
    if ( isFunction() )                              // function already
      return;

    if ( isHistogram() ) 
    { 
      float temp_x[] = null;
      if ( divide )                   // divide y values by the bin width
      {
        temp_x = x_scale.getXs();
        for ( int i = 0; i < y_values.length; i++ )
          y_values[i] = y_values[i] / (temp_x[i+1] - temp_x[i]);

        if ( errors != null )                     // assume no error in x values
          for ( int i = 0; i < y_values.length; i++ )
            errors[i] = errors[i] / (temp_x[i+1] - temp_x[i]);
      }

                                          //  use bin centers for the x values
      if ( x_scale instanceof UniformXScale )  
      {
        float start = x_scale.getStart_x();
        float end   = x_scale.getEnd_x();
        int   num_x = x_scale.getNum_x();
        float step  = (float)((UniformXScale)x_scale).getStep();

        if ( num_x > 2 )                               // Note: due to rounding
          x_scale = new UniformXScale( start + step/2, // errors, if there are 
                                       end   - step/2, // only two points, this 
                                       num_x - 1  );   // could be an invalid 
                                                       // XScale with end<start

        else                                           // in that case, make
                                                       // sure that start == end
          x_scale = new UniformXScale( start + step/2,
                                       start + step/2,
                                       num_x - 1  );  
      }
      else                                             // VariableXScale, so
      {                                                // calculate bin centers
        if ( temp_x == null )  // not assigned yet
          temp_x = x_scale.getXs();
        float new_x[] = new float[ y_values.length ];
        for ( int i = 0; i < y_values.length; i++ )
          new_x[i] = (temp_x[i] + temp_x[i+1]) / 2;
        x_scale = new VariableXScale( new_x );
      }
    }
  }

  /**
   *  Convert the Data to histogram Data block, if it is currently tabulated 
   *  function Data.  This assumes that the tabulated function values are 
   *  values at the bin centers of a corresponding histogram.  To multiply
   *  the values by the bin widths, to properly convert from a density function
   *  to a histogram, the "multiply" flag must be passed in as true. 
   *
   *  @param  width_1   Width of the first bin.  For VariableXScales, this is
   *                    is used to determine the bin boundaries.  NOTE: this
   *                    process is subject to rounding errors and should be
   *                    avoided if possible.  If width_1 is less than or equal
   *                    to zero, the distance between the first two x values
   *                    will be used as a default.
   *
   *  @param  multiply  Flag that indicates whether the function values
   *                    should be multiplied by the width of the histogram bin.
   */
  public void ConvertToHistogram( float width_1, boolean multiply )
  {
    if ( isHistogram() )                             // histogram already
      return;

    if ( isFunction() ) 
    {
      float start    = x_scale.getStart_x();
      float end      = x_scale.getEnd_x();
      int   num_x    = x_scale.getNum_x();
      float new_x[]  = null;                         // for the bin boundaries
                                                     // if needed.

      if ( x_scale instanceof UniformXScale )        // bin boundaries are 
      {                                              // also uniformly spaced
        float step  = (float)((UniformXScale)x_scale).getStep();
        x_scale = new UniformXScale( start - step/2,
                                     end   + step/2,
                                     num_x + 1      );
      }
      else                                          // calculate the bin 
      {                                             // boundaries
        float old_x[]    = x_scale.getXs();

        if ( width_1 <= 0 )
        {
          if ( old_x.length < 2 )
          {
            System.out.println("ERROR: bad width_1 in Data.ConvertToHistogram");
            return;
          }
          width_1 = old_x[1] - old_x[0];
        }
        float half_width = width_1 / 2;

        new_x    = new float[ num_x + 1 ]; 
        new_x[0] = old_x[0] - half_width;

        for ( int i = 1; i < num_x; i++ )
        {
          new_x[i]   = old_x[i-1] + half_width;
          half_width = old_x[i]   - new_x[i];
        }
        new_x[ num_x ] = old_x[ num_x - 1 ] + half_width;
        x_scale = new VariableXScale( new_x );
      } 

      if ( multiply )                   // multiply y values by the bin width
      {
        if ( new_x == null )
          new_x = x_scale.getXs();

        for ( int i = 0; i < y_values.length; i++ )
          y_values[i] = y_values[i] * (new_x[i+1] - new_x[i]);

        if ( errors != null )                     // assume no error in x values
          for ( int i = 0; i < y_values.length; i++ )
            errors[i] = errors[i] * (new_x[i+1] - new_x[i]);
      }
    }
  }

  /**
   *  Convert the Data to histogram Data block, if it is currently tabulated
   *  function Data.  This assumes that the tabulated function values are
   *  values at the bin centers of a corresponding histogram.  To multiply
   *  the values by the bin widths, to properly convert from a density function
   *  to a histogram, the "multiply" flag must be passed in as true.  If 
   *  a VariableXScale is used, the width of the first bin is taken to be 
   *  1/2 the distance between the first two x-values, by default. 
   *
   *  @param  multiply  Flag that indicates whether the function values
   *                    should be multiplied by the width of the histogram bin.
   */
   public void ConvertToHistogram( boolean multiply )
   {
     ConvertToHistogram( -1, multiply );
   }

  /**
   *  Resample the Data block on an arbitrarily spaced set of points given by
   *  the new_X scale parameter.  If the Data block is a tabulated function,
   *  the function will just be interpolated at the specified points.  If the 
   *  Data block is a histogram, the histogram will be re-binned to form a 
   *  new histogram with the specified bin sizes.
   *
   *  @param new_X  The x scale giving the set of x values to use for the
   *                 resampling and/or rebinning operation.
   */
  public void Resample( XScale new_X )
  {
    if ( isHistogram() )                     // histogram, so ReBin
    {
      ReBin( new_X );
      return;
    }
                                             // otherwise, must be a function 
                                             // so Resample
    float x[]  = x_scale.getXs();
    float nX[] = new_X.getXs();
    y_values   = Sample.Resample( x, y_values, nX );

    if ( errors != null )
      errors = Sample.Resample( x, errors, nX );

    x_scale = (XScale)new_X.clone();
  }

  /**
   *  Resample the Data block on a uniformly spaced set of points given by
   *  the new_X scale parameter.  If the Data block is a tabulated function,
   *  the function will be interpolated and smoothed by averaging nearby 
   *  points.  If the Data block is a histogram, the histogram will be 
   *  re-binned to form a new histogram with uniform bin sizes.
   *
   *  @param new_X  The x scale giving the set of x values to use for the
   *                 resampling and/or rebinning operation. 
   */
  public void ResampleUniformly( UniformXScale new_X )
  {
    if ( isHistogram() )
      ReBin( new_X );

    else                                               // function
    {
      float x_start = new_X.getStart_x();
      float x_end   = new_X.getEnd_x();

      float x[]     = x_scale.getXs();
      if ( x_start > x[ x.length-1 ] || x_end < x[0] || x.length ==0 )  
      {                                                 // degenerate case
        x_scale = (UniformXScale)new_X.clone();         // intervals don't 
        y_values = new float[x_scale.getNum_x()];       // overlap

        for ( int i = 0; i < y_values.length; i++ )     // assume function is 0
          y_values[i] = 0;                              // outside

        if ( errors != null )
        {
          errors = new float[y_values.length];
          for ( int i = 0; i < y_values.length; i++ )
            errors[i] = 0;
        } 
      }
      else if ( x.length == 1 )                        // only one point
      { 
        float x_val = x[0];
        float y_val = y_values[0];
        
        x_scale = (UniformXScale)new_X.clone();
        x        = x_scale.getXs();
        y_values = new float[x_scale.getNum_x()];  

        for ( int i = 0; i < y_values.length; i++ )
          y_values[i] = 0;

        int x_index = arrayUtil.get_index_of( x_val, x );        
        y_values[ x_index ] = y_val;
        if ( x_val != x[x_index] )                     // the one point affects 
          y_values[ x_index+1 ] = y_val;               // value at both
                                                       // adjacent grid points
        if ( errors != null )
        {
          float err = errors[0];
          errors = new float[y_values.length];
          for ( int i = 0; i < y_values.length; i++ )
            errors[i] = 0;

          errors[ x_index ] = err;
          if ( x_val != x[x_index] )                   // the one point affects
            errors[ x_index+1 ] = err;                 // error at both
                                                       // adjacent grid points
        }
      }
      else    // the intervals overlap, so get the portions of the x&y arrays 
      {       // that we are dealing with and pass those into the smooth op

         int i_start = arrayUtil.get_index_of( x_start, x );
         if ( i_start < 0 )
           i_start = 0;

         int i_end = arrayUtil.get_index_of( x_end, x );
         if ( i_end < 0 )
           i_end = x.length-1;

         int num_new_x = i_end - i_start + 1;       // extract part of the
         float new_x[] = new float[ num_new_x ];    // existing x,y values
         float new_y[] = new float[ num_new_x ];
         for ( int i = 0; i < num_new_x; i++ )
         {
           new_x[i] = x[ i + i_start ];
           new_y[i] = y_values[ i + i_start ];
         }

         x_scale = new VariableXScale( new_x );     // keep only part of the
         y_values = new_y;                          // existing x,y values

         if ( errors != null )                      // keep part of existing 
         {                                          // errors if not null.
           float new_err[] = new float[ num_new_x ];
           for ( int i = 0; i < num_new_x; i++ )
             new_err[i] = errors[ i + i_start ];
           errors = new_err;
         }
         CLSmooth( new_X.getNum_x() );             // changes current Data
                                                   // block x, y, errors
        
         x = x_scale.getXs();
         float nX[] = new_X.getXs();
         y_values = Sample.Resample( x, y_values, nX );   
       
         if ( errors != null ) 
           errors   = Sample.Resample( x, errors, nX );
         x_scale = (UniformXScale)new_X.clone();
      }
                
    }
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

    if ( errors != null )
    {
      float new_errs[] = new float[ new_X.getNum_x() - 1 ];
      Sample.ReBin( x_scale.getXs(), old_ys, errors,
                    new_X.getXs(),   new_ys, new_errs );
      y_values = new_ys;
      errors   = new_errs;
    }
    else
    {
      Sample.ReBin( x_scale.getXs(), old_ys, new_X.getXs(), new_ys );
      y_values = new_ys;
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

    if ( isFunction() ) 
    {
      function = true;
      old_x    = x_scale.getXs();
    }
    else if ( isHistogram() )                             // histogram 
    {                                                     // so use bin centers
      function       = false;                             // for the x values
      float temp_x[] = x_scale.getXs();
      old_x          = new float[ temp_x.length -1 ];
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
     num_X = Sample.CLSmooth( old_x, y_values, errors, num_X );
   else
     num_X = Sample.CLSmooth( old_x, y_values, num_X );

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
//   if ( function )
//   {
     float new_x[] = new float[num_X];
     System.arraycopy( old_x, 0, new_x, 0, num_X );
     x_scale = new VariableXScale( new_x );
/*
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
*/
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
//    System.out.println("y lengths: " + y_values.length+
//                       ", " +        d.y_values.length);
    if ( this.y_values.length != d.y_values.length )
      return false;

//    System.out.println("x lengths: " + x_scale.getNum_x()+
//                       ", " +        d.x_scale.getNum_x() );
    if ( this.x_scale.getNum_x() != d.x_scale.getNum_x() )
      return false;

//    System.out.println("Start x: " + x_scale.getStart_x()+
//                       ", " +      d.x_scale.getStart_x() );
    if ( this.x_scale.getStart_x() != d.x_scale.getStart_x() )
      return false;

//    System.out.println("End x: " + x_scale.getEnd_x()+
//                       ", " +      d.x_scale.getEnd_x() );
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
    {
      d = (Data)d.clone();                  // make a clone and resample it
      d.Resample( x_scale );                // to match the current Data block
    }

    Data temp = (Data)this.clone();

    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] += d.y_values[i];
    
    if ( this.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( this.errors[i] * this.errors[i] +
                                               d.errors[i] *    d.errors[i] ); 
    else
      temp.errors = null;
                                         // now take care of attributes... most
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
    {
      d = (Data)d.clone();                  // make a clone and resample it
      d.Resample( x_scale );                // to match the current Data block
    }

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
    {
      d = (Data)d.clone();                  // make a clone and resample it
      d.Resample( x_scale );                // to match the current Data block
    }

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
    {
      d = (Data)d.clone();                  // make a clone and resample it
      d.Resample( x_scale );                // to match the current Data block
    }

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

  public static void main( String argv[] )
  {
    final int NUM_POINTS = 10000;
    float y[] = new float[NUM_POINTS];

    XScale x_scale = new UniformXScale( 0, 10, NUM_POINTS + 1 );

                                          // Un-comment the following lines to
                                          // use variable x scales.  That is
                                          // more subject to rounding errors.
//    float x[] = x_scale.getXs();          
//    x_scale = new VariableXScale( x );

    for ( int i = 0; i < NUM_POINTS; i++ )
      y[i] = 10000;
  
    Data d = new Data( x_scale, y, 101010 );
    d.setSqrtErrors();
 
    System.out.println("As Histogram....");
    if ( d.isHistogram() )
      System.out.println("NOW HISTOGRAM" );
    d.print( 0, 20 ); 
    d.print( NUM_POINTS-20, NUM_POINTS ); 

    d.ConvertToFunction( true );
    System.out.println("As Function....");
    if ( d.isFunction() )
      System.out.println("NOW FUNCTION" );
    d.print( 0, 20 ); 
    d.print( NUM_POINTS-20, NUM_POINTS ); 

    d.ConvertToHistogram( 10.0f/NUM_POINTS, true );
    System.out.println("As Histogram AGAIN!!!....");
    if ( d.isHistogram() )
      System.out.println("NOW HISTOGRAM" );
    d.print( 0, 20 );
    d.print( NUM_POINTS-20, NUM_POINTS );
  }

}

/** 
 * File: Data.java
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
 *  Revision 1.23  2002/04/04 18:10:08  dennis
 *  Moved implementation of stitch() and of scalar and DataSet
 *  add(), subtract(), multiply(), divide() to this level.
 *  A TabulatedData object will be created and returned
 *  by these methods, for ANY type of Data object.
 *
 *  Revision 1.22  2002/03/18 21:37:17  dennis
 *  Now adds Label attribute, initialized as the Group ID.
 *  getCopyOfY_values() and getCopyOfErrors() now check whether
 *  or not the array is null before trying to copy it.
 *
 *  Revision 1.21  2002/03/14 23:41:51  dennis
 *  Now implements Serializable.
 *
 *  Revision 1.20  2002/03/13 16:08:31  dennis
 *  Data class is now an abstract base class that implements IData
 *  interface. FunctionTable and HistogramTable are concrete derived
 *  classes for storing tabulated functions and frequency histograms
 *  respectively.
 *
 */
package DataSetTools.dataset;

import java.io.*;
import DataSetTools.math.*;
import DataSetTools.util.*;

public abstract class Data implements IData,
                                      Serializable 
{
  public static final String FUNCTION  = "Function";
  public static final String HISTOGRAM = "Histogram";

  protected int          group_id;  //NOTE: we force a Data object to have an
                                    // id and also keep the same id as an
                                    // attribute.  The id should only be
                                    // changed through the setID() method.
  protected XScale        x_scale;
  protected AttributeList attr_list;
                                           // each Data block is tagged as
  protected long          selected = 0;    // selected by giving it a non-zero
  private   static long   select_count = 1;// selected value.  The value is
                                           // value of select_count after
                                           // incrementing select_count.
                                           // selected == select_count
                                           // for the most recently selected
                                           // Data block.
  protected boolean       hide = false;

  /**
   *  Create an instance of a FunctionData or HistogramData object.
   */ 
  public static Data getInstance( XScale x_scale, 
                                  float  y_values[], 
                                  float  errors[], 
                                  int    group_id  )
  {
    if ( y_values.length == x_scale.getNum_x() - 1 )
      return new HistogramTable( x_scale, y_values, errors, group_id );
    else
      return new FunctionTable( x_scale, y_values, errors, group_id );
  }

  /**
   *  Create an instance of a FunctionData or HistogramData object, without
   *  specified errors.
   */
  public static Data getInstance( XScale x_scale,
                                  float  y_values[],
                                  int    group_id  )
  {
    if ( y_values.length == x_scale.getNum_x() - 1 )
      return new HistogramTable( x_scale, y_values, group_id );
    else
      return new FunctionTable( x_scale, y_values, group_id );
  }


  /**
   * Constructs a Data object with "X" scale and a group id for that Data 
   * object.  This will be called from the constructors of subclasses.  Any
   * concrete derived class must manage the y-values associated with that
   * subclass.
   *
   * @param   x_scale   the list of x values for this data object
   * @param   group_id  an integer id for this data object
   *
   * @see DataSetTools.dataset.XScale
   * @see DataSetTools.dataset.Attribute
   */
  public Data( XScale  x_scale, int group_id )
  {
    this.x_scale   = x_scale;
    this.attr_list = new AttributeList();
    setGroup_ID( group_id );
    selected = 0;
    hide     = false;
    StringAttribute attr = new StringAttribute( Attribute.LABEL, 
                                                "Group " + group_id );
    attr_list.setAttribute( attr );
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
   * Returns a reference to the list of the error values.  If no error values
   * have been set, this returns null.
   */
  public int getGroup_ID()
  {
    return group_id;
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
   *  Set the "hide" flag of this Data block to the specified value.
   *  
   *  @param  hide  New value for the hidden flag.
   */
  public void setHide( boolean hide )
  {
    this.hide = hide;
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
   *  Toggle the "hide" flag of this Data block to the opposite state that
   *  it is currently in.
   */
  public void toggleHide()
  {
    hide = !hide;
  }
 
  /**
   * Combine the attribute list of the specified Data object with the attribute
   * list of the current Data object to obtain a new attribute list for the
   * current Data object.
   *
   *  @param  d    The Data object whose attribute list is to be combined
   *               with the current object's attribute list
   */
   public void combineAttributeList( Data d )
   {
     attr_list.combine( d.getAttributeList() );
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
   * Returns a copy of the array of x values for this Data object 
   */
  public float[] getX_values()
  { 
    return x_scale.getXs(); 
  }

  /**
   * Returns a reference to the "X" scale for this Data object
   */
  public XScale getX_scale() 
  { 
    return x_scale; 
  }

  // These are convenience methods that get a reference to the lower level 
  // array and then copy it and return a copy of it.  

  /**
   *  Returns a reference to a new copy of the y_values
   */
  public float[]  getCopyOfY_values()       
  {
    float y_vals[]   = getY_values();
    if ( y_vals != null )
    {
      float new_vals[] = new float[ y_vals.length ];
      System.arraycopy( y_vals, 0, new_vals, 0, y_vals.length );
      return new_vals;
    }
    else
      return null;
  } 

  /**
   *  Returns a reference to a new copy of the errors 
   */
  public float[]  getCopyOfErrors()       
  {
    float errors[]   = getErrors();
    if ( errors != null )
    {
      float new_vals[] = new float[ errors.length ];
      System.arraycopy( errors, 0, new_vals, 0, errors.length );
      return new_vals;
    }
    else
      return null;
  } 


  /**
    * Construct a new Data object by ADDING corresponding "y" values of the
    * current Data object and the specified Data object d.  If both the
    * current and the specified Data object d have errors, the errors
    * will propagate to the new Data object.  The attributes for the resulting 
    * Data object are a combination of the attributes of the current Data 
    * object and the specified Data object d.
    *
    * The combination is treated differently, depending on whether or not
    * the two data blocks have the same group ID and on what the attribute is.
    * Specifically the treatment of some of the most important attributes is
    * as follows:
    *
    * Same Group ID:
    *   TOTAL_COUNT       summed
    *   NUMBER_OF_PULSES  summed
    *   SOLID_ANGLE       averaged
    *   RAW_ANGLE         averaged
    *   DELTA_TWO_THETA   averaged
    *   DETECTOR_POS      averaged
    *   DETECTOR_POS      average, weighted by SOLID_ANGLES if present
    *
    * Different Group ID:
    *   TOTAL_COUNT       summed
    *   NUMBER_OF_PULSES  averaged
    *   SOLID_ANGLE       summed
    *   RAW_ANGLE         keep raw angle of current Data object
    *   DELTA_TWO_THETA   max of current value or difference of RAW_ANGLEs
    *   DETECTOR_POS      average, weighted by SOLID_ANGLES if present
    *
    * @param   other_d   The Data object to be added to the current data object
    *
    */
  public Data add( Data other_d )
  {
    String type;
    if ( isHistogram() )
      type = Data.HISTOGRAM;
    else
      type = Data.FUNCTION;
                                           // make a new TabulatedData object
                                           // from both the Data objects
    TabulatedData temp = TabulatedData.getInstance( this, group_id, type );
    TabulatedData d    = TabulatedData.getInstance( other_d, group_id, type );

    if ( ! this.compatible( d ) )
      d.resample( x_scale, SMOOTH_LINEAR );

    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] += d.y_values[i];

    if ( temp.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( temp.errors[i] * temp.errors[i] +
                                               d.errors[i] *    d.errors[i] );
    else
      temp.errors = null;
                                         // now take care of attributes... most
                                         // will use the default combine method
                                         // but some be treated differently
    Attribute attr,
              attr1,
              attr2;
    temp.combineAttributeList( d );
    temp.attr_list.add( Attribute.TOTAL_COUNT,
                        this.getAttributeList(),
                        d.getAttributeList()    );
    // do weighted sum of DetectorPosition, weighted by solid angle.
    attr1 = this.attr_list.getAttribute(Attribute.DETECTOR_POS);
    attr2 = d.attr_list.getAttribute(Attribute.DETECTOR_POS);
    if ( attr1 != null && attr2 != null )
    {
      DetectorPosition points[] = new DetectorPosition[2];
      points[0] = (DetectorPosition)attr1.getValue();
      points[1] = (DetectorPosition)attr2.getValue();
      attr1 = this.attr_list.getAttribute(Attribute.SOLID_ANGLE);
      attr2 = d.attr_list.getAttribute(Attribute.SOLID_ANGLE);
      if ( attr1 != null && attr2 != null )
      {
        float weights[] = new float[2];
        weights[0] = (float)attr1.getNumericValue();
        weights[1] = (float)attr2.getNumericValue();
        DetectorPosition ave_pos =
                  DetectorPosition.getAveragePosition( points, weights );
        attr = new DetPosAttribute( Attribute.DETECTOR_POS, ave_pos );
        temp.attr_list.setAttribute(attr);
      }
    }
                                         // special cases for adding same
                                         // groups from different runs, or
                                         // different groups ( same run? )
    if ( this.group_id == d.group_id )
      temp.attr_list.add( Attribute.NUMBER_OF_PULSES,
                          this.getAttributeList(),
                          d.getAttributeList()    );
    else
    {
      temp.attr_list.add( Attribute.SOLID_ANGLE,
                          this.getAttributeList(),
                          d.getAttributeList()    );

                                           // keep raw angle of first Data block
      attr = this.attr_list.getAttribute(Attribute.RAW_ANGLE);
      if ( attr != null )
        temp.attr_list.setAttribute( attr );
                                                    // approximate maximum for
                                                    // delta two theta
      attr1 = this.attr_list.getAttribute(Attribute.RAW_ANGLE);
      attr2 = d.attr_list.getAttribute(Attribute.RAW_ANGLE);
      if ( attr1 != null && attr2 != null )
      {
        float new_delta = (float)Math.abs( attr1.getNumericValue() -
                                           attr2.getNumericValue() );
        attr = this.attr_list.getAttribute(Attribute.DELTA_2THETA);
        if ( attr != null )
        {
          float delta = (float)attr.getNumericValue();
          attr = new FloatAttribute( Attribute.DELTA_2THETA,
                                     Math.max( delta, new_delta ) );
          temp.attr_list.setAttribute(attr);
        }
        else
        {
          attr = new FloatAttribute( Attribute.DELTA_2THETA,
                                     Math.max( new_delta, new_delta ) );
          temp.attr_list.setAttribute(attr);
        }
      }
    }

    return temp;
  }


  /**
    * Construct a new Data object by SUBTRACTING corresponding "y" values of
    * the current Data object and the specified Data object d.  If both the
    * current and the specified Data object have errors, the errors
    * will propagate to the new Data object.  The attributes for the resulting 
    * Data object are the same as the attributes of the current Data object.
    *
    * @param   other_d   The Data object to be subtracted from the current data
    *                    object
    */

  public Data subtract( Data other_d )
  {
    String type;
    if ( isHistogram() )
      type = Data.HISTOGRAM;
    else
      type = Data.FUNCTION;
                                           // make a new TabulatedData object
                                           // from both the Data objects
    TabulatedData temp = TabulatedData.getInstance( this, group_id, type );
    TabulatedData d    = TabulatedData.getInstance( other_d, group_id, type );

    if ( ! this.compatible( d ) )
      d.resample( x_scale, SMOOTH_LINEAR ); 

    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] -= d.y_values[i];

    if ( temp.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( temp.errors[i] * temp.errors[i] +
                                               d.errors[i] *    d.errors[i] );
    else
      temp.errors = null;

    return temp;
  }


  /**
    * Construct a new Data object by MULTIPLYING corresponding "y" values of
    * the current Data object and the specified Data object d.  If both the
    * current and the specified Data object d have errors, the errors
    * will propagate to the new Data object.  The attributes for the 
    * resulting Data object are the same as the attributes of the current 
    * Data object.
    *
    * @param   other_d   The Data object to be multiplied times the current data
    *                    object
    */

  public Data multiply( Data other_d )
  {
    String type;
    if ( isHistogram() )
      type = Data.HISTOGRAM;
    else                                   
      type = Data.FUNCTION;
                                           // make a new TabulatedData object
                                           // from both the Data objects
    TabulatedData temp = TabulatedData.getInstance( this, group_id, type );
    TabulatedData d    = TabulatedData.getInstance( other_d, group_id, type );

    if ( ! this.compatible( d ) )
      d.resample( x_scale, SMOOTH_LINEAR ); 

    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] *= d.y_values[i];

    if ( temp.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt(
              temp.errors[i] * d.y_values[i] * temp.errors[i] * d.y_values[i] +
              d.errors[i] * temp.y_values[i] * d.errors[i] * temp.y_values[i] );
    else
      temp.errors = null;

    return temp;
  }


  /**
    * Construct a new Data object by DIVIDING corresponding "y" values of
    * the current Data object and the specified Data object d.  If both the
    * current and the specified Data object d have errors, the errors
    * will propagate to the new Data object.  The attributes for the
    * resulting Data object are the same as the attributes of the current
    * Data object.
    *
    * @param   other_d  The Data object to be divided into the current data
    *                   object
    */

  public Data divide( Data other_d )
  {
    String type;
    if ( isHistogram() )
      type = Data.HISTOGRAM;
    else
      type = Data.FUNCTION;
                                           // make a new TabulatedData object
                                           // from both the Data objects
    TabulatedData temp = TabulatedData.getInstance( this, group_id, type ); 
    TabulatedData d    = TabulatedData.getInstance( other_d, group_id, type ); 

    if ( !temp.compatible( d ) )
      d.resample( temp.x_scale, SMOOTH_LINEAR );

    for ( int i = 0; i < temp.y_values.length; i++ )
      if ( d.y_values[i] > 0.01 )                           // D.M. 6/7/2000
        temp.y_values[i] /= d.y_values[i];
      else
        temp.y_values[i] = 0;

    if ( temp.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = temp.y_values[i] * (float) Math.sqrt(
          temp.errors[i] / temp.y_values[i] * temp.errors[i] / temp.y_values[i]+
          d.errors[i] / d.y_values[i] * d.errors[i] / d.y_values[i] );
    else
      temp.errors = null;

    return temp;
  }


  /**
    * Construct a new Data object by ADDING the specified value "val" to each
    * "y" value of the  current Data object.  The error in the value "val"
    * is specified.  If the current Data object has an error array,
    * the error values will be combined with the specified error and
    * set in the new Data object.
    *
    * @param   val  The value to be added to the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "val".
    */

  public Data add( float val, float err )
  {
    TabulatedData temp = (TabulatedData)this.clone();
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] += val;
   
    if ( temp.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( temp.errors[i] * temp.errors[i] +
                                                       err * err );
    else
      temp.errors = null;

    return temp;
  }


  /**
    * Construct a new Data object by SUBTRACTING the specified value "val"
    * from each "y" value of the  current Data object.  The error in the value
    * "val" is specified.  If the current Data object has errors,
    * the error values will be combined with the specified error and set
    * in the Data object.
    *
    * @param   val  The value to be added to the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "val".
    */

  public Data subtract( float val, float err )
  {
    TabulatedData temp = TabulatedData.getInstance( this, this.group_id );
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] -= val;

    if ( temp.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( temp.errors[i] * temp.errors[i] +
                                                       err * err );
    else
      temp.errors = null;

    return temp;
  }


  /**
    * Construct a new Data object by MULTIPLYING the specified value "val"
    * times each "y" value of the  current Data object.  The error in the value
    * "val" is specified.  If the current Data object has an error array,
    * the error values will be combined with the specified error and set
    * in the new Data object.
    *
    * @param   val  The value to be multiplied times the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "val".
    */

  public Data multiply( float val, float err )
  {
    TabulatedData temp = TabulatedData.getInstance( this, this.group_id );
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] *= val;

    if ( temp.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt(
                         temp.errors[i] * val * temp.errors[i] * val +
                         err * temp.y_values[i] * err * temp.y_values[i] );
    else
      temp.errors = null;

    return temp;
  }


  /**
    * Construct a new Data object by DIVIDING the specified value "val"
    * into each "y" value of the  current Data object.  The error in the value
    * "val" is specified. If the current Data object has specified error values,
    * the error values will be combined with the specified error and set in
    * the new Data object.
    *
    * @param   val  The value to be divided into the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "val".
    */

  public Data divide( float val, float err )
  {
    if ( val == 0.0f )
      return null;

    TabulatedData temp = TabulatedData.getInstance( this, this.group_id );
    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] /= val;

    if ( temp.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = temp.y_values[i] * (float) Math.sqrt(
          temp.errors[i] / temp.y_values[i] * temp.errors[i] / temp.y_values[i]+
          err / val * err / val );
    else
      temp.errors = null;

    return temp;
  }


  /**
   *    "Stitch" another Data block together with the current Data block to form
   *  a new TabulatedData block with the same attributes as the current Data 
   *  block, but whose data is a combination of the two. 
   *
   *    New TabulatedData blocks will first be generated from the current
   *  Data object and the other_data block.  These TabulatedData blocks will
   *  of the same type ( HISTOGRAM or FUNCTION ) as the current Data block.  
   *  Then a new XScale is formed and the y-values from the two Data blocks 
   *  are "stitched" together over the new XScale.  The XScale for the new 
   *  Data object covers an interval containing the union of the current 
   *  Data's XScale and the other Data's XScale.
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
  public Data stitch( Data other_data, int overlap )
  {
    String type;
    if ( isHistogram() )
      type = Data.HISTOGRAM;
    else
      type = Data.FUNCTION;
                                           // make a new TabulatedData object
                                           // from both the Data objects
    TabulatedData new_data  = TabulatedData.getInstance( this, group_id, type );
    TabulatedData temp_data = TabulatedData.getInstance( other_data, 
                                                         group_id, type );

                                         // set error values for new_data if
                                         // needed and not present
    boolean has_error_info = false; 
    if ( new_data.errors != null )
    {
      has_error_info = true;
      if ( temp_data.errors == null )   // if no previous error values set,
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
    temp_data.resample( new_x_scale, SMOOTH_LINEAR );
    new_data.resample( new_x_scale, SMOOTH_LINEAR );
                                          // Now combine the y_values and errors
    float x[] = new_x_scale.getXs();
    if ( !new_data.isHistogram() )                 // use one x value to decide
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
   *  Provide an identifier string for this Data block
   */
  public String toString()
  {
    Attribute attr = getAttribute( Attribute.LABEL );

    if ( attr != null )
      return "Label " + attr;
    else
      return "Group ID " + group_id;
  }


  // The remaining methods are abstract and will have to be implemented in 
  // each derived class.  Consequently, the semantics of the methods will vary
  // from class to class.
  // NOTE: There is not a setY_values() or a setX_values().  The x and y 
  //       values must remain coordinated, and are only directly set in the 
  //       constructor or indirectly adjusted using the resample method. 
 
  abstract public boolean isHistogram();

  abstract public float[] getY_values();
  abstract public float[] getY_values( XScale x_scale, int smooth_flag ); 
  abstract public float   getY_value( float x, int smooth_flag ); 

  abstract public float[] getErrors();
  abstract public void    setSqrtErrors();

  abstract public void    resample( XScale x_scale, int smooth_flag );
  abstract public Object  clone();

  /* -------------------------------------------------------------------------
   *
   *  PRIVATE METHODS
   *
   */

  /**
    * Returns true or false depending on whether the two Data objects are
    * capable of being added, etc., based on the size and extent of their
    * x_scales.  If the XScales are NOT uniform, each corresponding point
    * of the XScales should probably be compared, however, this is not
    * currently done.  Variable x_scales are assumed to be in compatible
    *
    *  @param  d     The Data object to be compared with the current data
    *                object.
    */
  public boolean compatible( Data d )
  {
    if ( !(  x_scale instanceof UniformXScale)  ||
         !(d.x_scale instanceof UniformXScale)      )
      return false;

    if ( this.x_scale.getNum_x() != d.x_scale.getNum_x() )
      return false;

    if ( this.x_scale.getStart_x() != d.x_scale.getStart_x() )
      return false;

    if ( this.x_scale.getEnd_x() != d.x_scale.getEnd_x() )
      return false;

    return true;
  }

}

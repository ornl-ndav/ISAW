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
import DataSetTools.dataset.XScale;
import DataSetTools.dataset.IAttributeList;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Attribute;

public abstract class Data implements IData,
                                      Serializable 
{
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
  abstract public void    setErrors( float errors[] );
  abstract public void    setSqrtErrors();

  abstract public Data    add( Data d );
  abstract public Data    add( float y, float err );
  abstract public Data    subtract( Data d );
  abstract public Data    subtract( float y, float err );
  abstract public Data    multiply( Data d );
  abstract public Data    multiply( float y, float err );
  abstract public Data    divide( Data d );
  abstract public Data    divide( float y, float err );

  abstract public void    resample( XScale x_scale, int smooth_flag );
  abstract public Data    stitch( Data other_data, int overlap );

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

  abstract public Object  clone();
}

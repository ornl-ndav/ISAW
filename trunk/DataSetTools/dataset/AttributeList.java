/*
 * @(#)AttributeList.java     1.01  99/07/9  Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.7  2000/12/13 00:15:50  dennis
 *  Modified setAttribute() method to leave the attribute that is set in
 *  it's original position in the AttributeList, if it was originally in the
 *  list.
 *
 *  Revision 1.6  2000/11/10 22:52:10  dennis
 *  Trivial change to documentation.
 *
 *  Revision 1.5  2000/10/03 21:50:12  dennis
 *  Removed code in the combine() method that dealt with DataSet tags,
 *  since the DataSet tags will no longer be attributes, but will be
 *  a field in the DataSet.
 *
 *  Revision 1.4  2000/08/01 01:31:17  dennis
 *  Changed so that if an attribute is present in the first list, but not the
 *  second, the attribute from the first list used.
 *
 *  Revision 1.3  2000/07/26 19:15:41  dennis
 *  Now has method add() that will allow adding the values of a specified
 *  attribute from two lists and setting the summed attribute into the
 *  current list.
 *
 *  Revision 1.2  2000/07/10 22:23:52  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.10  2000/06/08 15:13:30  dennis
 *  Fixed minor documentation errors.
 *
 *  Revision 1.9  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 */

package  DataSetTools.dataset;

import java.util.Vector;
import java.io.*;

/**
 * Class that maintains a list of attributes. 
 *
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.DetPosAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.IntAttribute
 * @see DataSetTools.dataset.IntListAttribute
 * @see DataSetTools.dataset.StringAttribute
 *
 * @version 1.0  
 */

public class AttributeList implements Serializable
{
  private Vector attributes;

  /**
   * Constructs an initial empty list of Attributes.
   */
  public AttributeList( )
  {
    this.attributes = new Vector();
  }

  /**
   * Adds a new attribute to the list of attributes, if that attribute is
   * not already present in the list.  If the attribute is already present
   * in the list, this method returns false.
   *
   *  @param  attribute    The new attribute to be added to the list of
   *                       attributes.
   *
   *  @return              Returns true if the attribute was added to the list.
   *                       Returns false if the attribute was already in the
   *                       list and the new attribute was not added to the list.
   */
  public boolean addAttribute( Attribute attribute )
  {
    if ( getAttribute( attribute.getName() ) == null )
    {
      attributes.addElement( (Attribute)attribute.clone() );
      return true;
    }
   else
      return false;
  }

  /**
   * Adds a new attribute to the list of attributes at at the specified 
   * position.  If the attribute is already present in the list, the attribute
   * is not added and this method returns false.
   *
   *  @param  attribute    The new attribute to be set in the list of
   *                       attributes.
   *
   *  @param  index        The position where the attribute is to be
   *                       inserted.
   *
   *  @return              Returns true if the attribute was added to the list.
   *                       Returns false if the attribute was already in the
   *                       list and the new attribute was not added to the list.
   */
  public boolean addAttribute( Attribute attribute, int index )
  {
    if ( index >= 0                   && 
         index <= attributes.size()   &&
         getAttribute( attribute.getName() ) == null )
    {
      attributes.insertElementAt( (Attribute)attribute.clone(), index );
      return true;
    }
    else
      return false;
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
    boolean attr_set = false;
    String  new_name = attribute.getName();
    String  attr_name;

    for ( int i = 0; i < attributes.size(); i++ )
    {
      attr_name = ((Attribute)attributes.elementAt( i )).getName();

      if ( new_name.equalsIgnoreCase( attr_name) )
      {
        attributes.setElementAt( attribute, i );
        attr_set = true;
      }
    }

    if ( !attr_set )
      addAttribute( attribute );
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
    removeAttribute( attribute.getName() );
    addAttribute( attribute, index );
  }


  /**
   * Copy the attributes from the specified AttributeList into the current
   * AttributeList.  If the same named attribute is already present in the 
   * list, the attribute is not added.
   */
  public void addAttributes( AttributeList new_list )
  {
    AttributeList current_list;

    Attribute attribute;
    for ( int i = 0; i < new_list.getNum_attributes(); i++ )
    {
      attribute = new_list.getAttribute(i);
      addAttribute( attribute );
    }
  }

  /**
   * Set the attributes from the specified AttributeList into the current
   * AttributeList.  If the same named attribute is already present in the
   * list, the attribute is changed to the same value.
   */
  public void setAttributes( AttributeList new_list )
  {
    AttributeList current_list;

    Attribute attribute;
    for ( int i = 0; i < new_list.getNum_attributes(); i++ )
    {
      attribute = new_list.getAttribute(i);
      setAttribute( attribute );
    }
  }


  /**
   * Gets the number of attributes in this AttributeList object
   */
  public int getNum_attributes()
  {
    return( attributes.size() );
  }


  /**
   * Get the attribute at the specified index from the list of attributes.
   * attributes. If the index is invalid, this returns null.
   *
   *  @param  index    The index in the list of attributes of the attribute
   *                   that is to be returned. 
   */
  public Attribute getAttribute( int index )
  {
    if ( index >= 0 && index < getNum_attributes() )
      return (Attribute)attributes.elementAt( index );
    else
      return null;
  }

  /**
   * Get the value of the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   *
   * @param  name  The name of the attribute value to get.
   */
  public Object  getAttributeValue( int index )
  {
    Attribute attribute;

    attribute = getAttribute( index );

    if ( attribute != null )
      return attribute.getValue();
    else                                          // if not found, return null
      return null;
  }


  /**
   * Get the attribute with the specified "name" from the list of attributes.
   * If the specified attribute is not in the list of attributes, null is 
   * returned.
   *
   *  @param  name    The "name" field of the attribute that is to be returned. 
   */
  public Attribute getAttribute( String name )
  {
    String attr_name;

    for ( int i = 0; i < attributes.size(); i++ )
    {
      attr_name = ((Attribute)attributes.elementAt( i )).getName();

      if ( name.equalsIgnoreCase( attr_name) )
        return (Attribute)attributes.elementAt( i );
    }

    return null;   // didn't find it
  }

  /**
   * Get the value of the attribute with the specified name from this list of
   * attributes.  If the named attribute is not in the list, this returns null.
   *
   * @param  name  The name of the attribute value to get.
   */
  public Object getAttributeValue( String name )
  {
    Attribute attribute;

    attribute = getAttribute( name );

    if ( attribute != null )
      return attribute.getValue();
    else                                          // if not found, return null
      return null;
  }

  /**
   * Removes the attribute at the specified index from this list of attributes.
   *
   *  @param  index    The index in the list of attributes of the attribute
   *                   that is to be removed. 
   */
  public void removeAttribute( int index )
  {
    if ( index >= 0 && index < getNum_attributes() )
      attributes.removeElementAt( index );
  }

  /**
   * Removes any attribute with the specified name from this list of attributes.
   *
   *  @param  name    The "name" field of the attribute that is to be removed. 
   */
  public void removeAttribute( String name )
  {
    String attr_name;

    for ( int i = 0; i < attributes.size(); i++ )
    {
      attr_name = ((Attribute)attributes.elementAt( i )).getName();

      if ( name.equalsIgnoreCase( attr_name) )
        attributes.removeElementAt( i );
    }
  }

  /**
   * Combine the attributes from another AttributeList with the current
   * attribute list.  Any attribute that is not in both lists will be deleted
   * from the current list.  If an attribute is present in both lists, the
   * combine() method is typically used to obtain the new value for the
   * attribute in the new list.   If an attribute is one of the specific 
   * named attributes defined in class Attribute, it may be treated in a 
   * special way determined by what it represents.  Other attributes will 
   * be combined using a combine operation for the appropriate class.
   *
   *  @param  attr_list  The attribute list that is to be combined with the
   *                      current attribute list.
   */
   public void combine( AttributeList attr_list )
   {
      Attribute     this_attr,
                    other_attr;
      String        attr_name;  
      AttributeList new_list = new AttributeList();

      for ( int i = 0; i < getNum_attributes(); i++ )
      {
        this_attr  = getAttribute( i );
        attr_name  = this_attr.getName();
        other_attr = attr_list.getAttribute( attr_name );

        if ( other_attr == null )             // there is no matching attribute
          new_list.addAttribute( this_attr ); // just use the current attribute

        else                              // combine them and append the result 
        {  
          if ( attr_name == Attribute.RUN_TITLE )
            new_list.addAttribute( this_attr ); 
              // keep the run title from the first attribute list, it would
              // be too long if concatenated

          else if ( attr_name == Attribute.END_TIME )
            ; // omit the End Time, it doesn't make sense to average it

          else if ( attr_name == Attribute.END_DATE )
            ; // omit the End Date, it doesn't make sense to average it

          else if ( attr_name == Attribute.GROUP_ID )
            new_list.addAttribute( this_attr );
              // keep the Group ID from the first attribute list

          else
          { 
           this_attr.combine( other_attr );     // alter this_attr by combining
           new_list.addAttribute( this_attr ); 
          }
        }
      }
   
      this.attributes = new_list.attributes;
   }

  /**
   * Add the specified attribute values from two attribute lists and set
   * the resulting sum as the attribtue value in the current attribute list.
   * This method is intended to be called after combining attribute lists to
   * implement special behavior for those attributes where the default 
   * "combine" behavior is not correct.
   *
   *  @param  attr_list  The attribute list that is to be combined with the
   *                      current attribute list.
   */
   public void add( String        attr_name,
                    AttributeList attr_list_1,
                    AttributeList attr_list_2 )
   {
      Attribute     attr_1,
                    attr_2;
                                         // get references to attributes from
                                         // the lists
      attr_1 = attr_list_1.getAttribute( attr_name );
      attr_2 = attr_list_2.getAttribute( attr_name );

      if ( attr_1 != null  && attr_2 != null ) 
      {                                  // the attributes are in the lists
                                         // so make a copy then add and put in
                                         // the list
         attr_1 = (Attribute)attr_1.clone();
         attr_1.add( attr_2 );    
         setAttribute( attr_1 ); 
      }      
   }



  /**
   * Return a new Data object containing a copy of the x_scale, y_values
   * errors, id and attributes from the current Data object.
   */
  public Object clone()
  {
    AttributeList  temp = new AttributeList( );

    for ( int i = 0; i < this.getNum_attributes(); i++ )
      temp.addAttribute( this.getAttribute(i) );

    return temp;
  }
  
  public String toString()
  {
    String buffer = "Attribute list: \n";
    
    for(int i = 0; i<attributes.size(); i++)
      buffer = buffer + attributes.elementAt(i).toString()+ "\n";

    return buffer;
  }

}

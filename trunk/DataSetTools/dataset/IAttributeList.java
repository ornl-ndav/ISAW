/*
 * @(#)IAttributeList.java  1.0  1999/07/?? Dennis Mikkelson
 * 
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.3  2000/07/10 22:24:00  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.5  2000/06/08 15:06:19  dennis
 *  Added "wrapper" methods to directly set/get attributes without getting
 *  the entire list of attributes.
 *
 *  Revision 1.4  2000/05/12 15:50:13  dennis
 *  removed DOS TEXT  ^M
 *
 *  Revision 1.3  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 *
 */

package  DataSetTools.dataset;

import java.io.*;

/**
 * IAttributeList provides the interface to get/set a List of attributes 
 * in an object.
 *
 * @see Data  
 * @see DataSet
 */  

public interface IAttributeList 
{

  /**
   *  Get a copy of the whole list of attributes for an object.
   */
  public AttributeList getAttributeList();



  /**
   *  Set the whole list of attributes for an object to be a COPY of the 
   *  specified list of attributes.
   */
  public void setAttributeList( AttributeList attr_list );


  /**
   * Gets the number of attributes set for the attribute list.
   */
  public int getNum_attributes();


  /**
   * Set the value of the specified attribute in the list of attributes.
   * If the attribute is already present in the list, the value is changed
   * to the value of the new attribute.  If the attribute is not already
   * present in the list, the new attribute is added to the list.
   *
   *  @param  attribute    The new attribute to be set in the list of
   *                       attributes.
   */
  public void setAttribute( Attribute attribute );
  

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
  public void setAttribute( Attribute attribute, int index );


  /**
   * Get the value of the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   *
   * @param  name  The name of the attribute value to get.
   */
  public Object  getAttributeValue( int index );


  /**
   * Get the value of the attribute with the specified name from the list of
   * attributes.  If the named attribute is not in the list, this returns null.
   *
   * @param  name  The name of the attribute value to get.
   */
  public Object getAttributeValue( String name );

} 

/*
 * File:  IAttributeList.java
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * 
 *  $Log$
 *  Revision 1.8  2003/02/10 13:28:36  dennis
 *  getAttributeList() now returns a reference to the attribute list,
 *  not a clone.
 *
 *  Revision 1.7  2002/11/27 23:14:06  pfpeterson
 *  standardized header
 *
 *  Revision 1.6  2002/07/10 16:02:25  pfpeterson
 *  Added removeAttribute() methods.
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
   *  Get a reference to the whole list of attributes for an object.
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
   * Remove the attribute at the specified index from the list of
   * attributes. If the index is invalid, this does nothing.
   *
   * @param index The position of the attribute to remove.
   */
  public void removeAttribute( int index );

  /**
   * Remove the attribute with the specified name from the list of
   * attributes. If the named attribute is not in the list, this does
   * nothing.
   *
   * @param name The name of the attribute to remove.
   */
  public void removeAttribute( String name );

  /**
   * Get the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   *
   * @param  index  The position of the attribute in the list of attributes.
   */
  public Attribute getAttribute( int index );


  /**
   * Get the attribute with the specified name from the list of
   * attributes.  If the named attribute is not in the list, this 
   * returns null.
   *
   * @param  name  The name of the attribute value to get.
   */
  public Attribute getAttribute( String name );


  /**
   * Get the value of the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   *
   * @param  index  The position of the attribute in the list of attributes.
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

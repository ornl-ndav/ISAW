/*
 * File:  PixelInfoListAttribute.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2003/02/07 18:58:30  dennis
 *  Initial Version
 *
 */

package  DataSetTools.dataset;

import DataSetTools.math.*;
import DataSetTools.dataset.*;
import java.io.*;

/**
 * This class is an Attribute whose value records information about all
 * detector segments contributing to a spectrum in a PixelInfoList object.
 *
 * @see DataSetTools.dataset.PixelInfoList
 * @see DataSetTools.dataset.IPixelInfo
 * @see DataSetTools.dataset.IDataGrid
 */

public class PixelInfoListAttribute extends Attribute
                                                   
{
  // NOTE: any field that is static or transient is NOT serialized.
  //
  // CHANGE THE "serialVersionUID" IF THE SERIALIZATION IS INCOMPATIBLE WITH
  // PREVIOUS VERSIONS, IN WAYS THAT CAN NOT BE FIXED BY THE readObject()
  // METHOD.  SEE "IsawSerialVersion" COMMENTS BELOW.  CHANGING THIS CAUSES
  // JAVA TO REFUSE TO READ DIFFERENT VERSIONS.
  //
  public  static final long serialVersionUID = 1L;


  // NOTE: The following fields are serialized.  If new fields are added that
  //       are not static, reasonable default values should be assigned in the
  //       readObject() method for compatibility with old servers, until the
  //       servers can be updated.

  private int IsawSerialVersion = 1;         // CHANGE THIS WHEN ADDING OR
                                             // REMOVING FIELDS, IF
                                             // readObject() CAN FIX ANY
                                             // COMPATIBILITY PROBLEMS
  private PixelInfoList value = null;

  /**
   * Constructs a PixelInfoListAttribute object using the specified name 
   * and PixelInfoList.
   */
  public PixelInfoListAttribute( String name, PixelInfoList value )
  {
    super( name );

    if ( value == null )
      return;

    this.value = value;
  }

  /**
   * Returns reference to the PixelInfoList kept by this attribute,
   * as a generic object. 
   */
  public Object getValue( )
  {
    return( value );
  } 

  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  The
   * new value is the result of merging the two PixelInfoList objects.
   * If the merge operation does not change anything, 'this' attribute
   * is returned.
   *
   *  @param   attr   An attribute whose value is to be "combined" with the 
   *                  value of the this attribute.
   */
  public Attribute combine( Attribute attr )
  {
    if ( !(attr instanceof PixelInfoListAttribute) )    // can't do it so don't 
      return this;                                          
       
    PixelInfoListAttribute other_attr = (PixelInfoListAttribute)attr; 

    PixelInfoList list       = (PixelInfoList)value;
    PixelInfoList other_list = (PixelInfoList)(attr.getValue()); 

    PixelInfoList new_list = list.merge( other_list );

    if ( new_list.equals( list ) )    // no change made so reuse this attribute
      return this;
    else
      return new PixelInfoListAttribute( name, new_list );
  }


  /**
   * Get a numeric value to be used for sorting based on this attribute.
   */
   public double getNumericValue()
   { 
                                            // use first entry ID as 
     if ( value.num_pixels() > 0 )          // "Numeric Value" if there is one.
       return value.pixel(0).ID();
     else
       return Double.MAX_VALUE;
   }

  /**
   * Returns a string representation of the list of PixelInfo objects 
   */
  public String getStringValue()
  {
    StringBuffer buffer = new StringBuffer();
    if ( value.num_pixels() < 5 )
      for ( int i = 0; i < value.num_pixels(); i++ )
        buffer.append(value.pixel(i).toString());
    else
    {
      for ( int i = 0; i < 5; i++ )
        buffer.append(value.pixel(i).toString());
      buffer.append( "..." );
    }
    return buffer.toString();
  }

  /**
   * Returns a string representation of the (name,value) pair for this
   * attribute
   */
  public String toString()
  {
    return this.getName() + ": " + this.getStringValue();
  }

 /* -----------------------------------------------------------------------
  *
  *  PRIVATE METHODS
  *
  */

 /* ---------------------------- readObject ------------------------------- */
 /**
  *  The readObject method is called when objects are read from a serialized
  *  ojbect stream, such as a file or network stream.  The non-transient and
  *  non-static fields that are common to the serialized class and the
  *  current class are read by the defaultReadObject() method.  The current
  *  readObject() method MUST include code to fill out any transient fields
  *  and new fields that are required in the current version but are not
  *  present in the serialized version being read.
  */

  private void readObject( ObjectInputStream s ) throws IOException,
                                                        ClassNotFoundException
  {
    s.defaultReadObject();               // read basic information

    if ( IsawSerialVersion != 1 )
      System.out.println(
                 "Warning:PixelInfoListAttribute IsawSerialVersion != 1");
  }


/* ---------------------------- main --------------------------------- */
/*
 *  Basic main program for testing
 */
 
  public static void main( String args[] )
  {
    int      id       =  1;
    String   units    = "meters";
    Vector3D center   = new Vector3D( 0,-.32f, 0 );
    Vector3D x_vector = new Vector3D(-1,   0, 0 );
    Vector3D y_vector = new Vector3D( 0,   0, 1 );
    float    width    = .25f;
    float    height   = .25f;
    float    depth    = 0.002f;
    int      n_rows   = 85;
    int      n_cols   = 85;

    UniformGrid test_grid = new UniformGrid( id, units,
                                             center, x_vector, y_vector,
                                             width, height, depth,
                                             n_rows, n_cols );

    short row;                                  // show info on one pixel
    short col;
    DetectorPixelInfo test_pixel;
    IPixelInfo list[] = new IPixelInfo[4];
    for ( row = 0; row < 2; row++ )
      for ( col = 0; col < 2; col++ )
        list[row * 2 + col] = new DetectorPixelInfo( row*2+col+100,
                                                     row, col,
                                                     test_grid );
    PixelInfoList pi_list = new PixelInfoList( list );
    PixelInfoListAttribute attr = new PixelInfoListAttribute( "TEST",pi_list );
    System.out.println("PixelInfoList value is:");
    System.out.println(attr); 
  }



}

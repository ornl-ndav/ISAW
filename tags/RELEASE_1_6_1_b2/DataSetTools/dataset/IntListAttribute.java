/*
 * File:  IntListAttribute.java
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
 *  Revision 1.14  2002/11/27 23:14:07  pfpeterson
 *  standardized header
 *
 *  Revision 1.13  2002/11/12 22:00:35  dennis
 *  Constructor now makes copy of array of values, to guarantee that
 *  the Attribute is immutable.
 *
 *  Revision 1.12  2002/11/12 00:47:20  dennis
 *  Made immutable by:
 *  1. remove setValue() method
 *  2. add() & combine() methods now return a new Attribute
 *  3. getValue() returns copy of the array value
 *
 *  Also:
 *  4. Since it is now immutable, clone() method is not needed and
 *     was removed
 *  5. Default constructor is now private, since the value can't
 *     be set from outside of the class
 *
 *  Revision 1.11  2002/08/01 22:33:35  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.10  2002/06/14 21:12:51  rmikk
 *  Implements IXmlIO interface
 *
 */

package  DataSetTools.dataset;

import DataSetTools.util.*;
import java.io.*;
/**
 * The concrete class for an attribute whose value is a list of integers.  
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.StringAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 * @see DataSetTools.dataset.DetPosAttribute
 *
 * @version 1.0  
 */

public class IntListAttribute extends Attribute
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
  private int[] values;

  /**
   * Constructs an IntListAttribute object using the specified name and values.
   */
  public IntListAttribute( String name, int values[] )
  {
    super( name );
                                             // make copy of the array
    this.values = new int[ values.length ];
    System.arraycopy( values, 0, this.values, 0, values.length ); 
  }

  private IntListAttribute( )
  {
    super( "" );

    values = new int[0];
  }

  /**
   * Returns a copy of the list of int values of this attribute, 
   * as a generic object.
   */
  public Object getValue( )
  {
    int new_array[] = new int[ values.length ];
    System.arraycopy( values, 0, new_array, 0, values.length );

    return( new_array );
  } 


  /**
   * Returns a copy of the int list value of this attribute,
   * as an array of ints.
   */
   public int[] getIntegerValue( )
   {
     int new_array[] = new int[ values.length ];
     System.arraycopy( values, 0, new_array, 0, values.length );

     return( new_array );
   }

  public boolean XMLwrite( OutputStream stream, int mode )
  {
    return xml_utils.AttribXMLwrite( stream, mode, this);
  }

  public boolean XMLread( InputStream stream )
  {
    return xml_utils.AttribXMLread(stream, this);
  }

  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a Attribute.  The value the new Attribute
   * is the result of merging the two lists of integers.
   *
   *  @param   attr   An attribute whose value is to be "combined" with the 
   *                  value of the this attribute.
   *  @return  If the attr parameter is an IntListAttribute, this returns 
   *           a new IntListAttribute whose value is the result of merging 
   *           the lists of integers, otherwise, it returns the current
   *           attribute.
   */
  public Attribute combine( Attribute attr )
  {
    if ( !(attr instanceof IntListAttribute) )         // can't do it so don't 
      return this;                                          
       
    IntListAttribute other_attr = (IntListAttribute)attr; 

    int      temp[]   = new int [ values.length + other_attr.values.length ];
    int      i        = 0,
             j        = 0,
             num_used = 0;
                                                             // merge the lists 
    while ( i < values.length && j < other_attr.values.length )    
    {
      if ( values[ i ] < other_attr.values[ j ] )
      {
        temp[ num_used ] = values[i];
        i++;
      }
      else if ( values[ i ] > other_attr.values[ j ] )    
      {
        temp[ num_used ] = other_attr.values[ j ];
        j++;
      }
      else
      {
        temp[ num_used ] = values[ i ];
        i++;
        j++;
      }
      num_used++;
    }

    if ( i < values.length )
    {
      System.arraycopy( values, i, temp, num_used, values.length - i );
      num_used += values.length - i;
    }
 
    else
    {
      System.arraycopy( other_attr.values, j, temp, num_used, 
                        other_attr.values.length - j );
      num_used += other_attr.values.length - j;
    }
                                                  // now copy the values into
                                                  // a new attribute
    int new_values[] = new int[ num_used ]; 
    System.arraycopy( temp, 0, new_values, 0, num_used );
    return new IntListAttribute( name, new_values );
  }


  /**
   * Get a numeric value to be used for sorting based on this attribute.
   */
   public double getNumericValue()
   {                                   // use first entry as "numeric value"
     if ( values.length > 0 )          // if there is one.
       return values[0];
     else
       return Double.MAX_VALUE;
   }

  /**
   * Returns a string representation of the list of integers 
   */
  public String getStringValue()
  {
    return IntList.ToString( values );
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
      System.out.println("Warning:IntListAttribute IsawSerialVersion != 1");
  }

}

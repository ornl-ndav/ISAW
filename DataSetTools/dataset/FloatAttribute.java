/*
 * File:  FloatAttribute.java
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
 *  Revision 1.8  2002/08/01 22:33:35  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.7  2002/06/14 21:00:09  rmikk
 *  Implements IXmlIO interface
 *
 *  Revision 1.6  2001/04/25 19:03:49  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.5  2000/07/26 14:52:24  dennis
 *  Now includes method to add() attributes.
 *
 *  Revision 1.4  2000/07/13 22:23:39  dennis
 *  Removed control-M characters
 *
 *  Revision 1.3  2000/07/10 22:23:59  dennis
 *  Now using CVS 
 *
 *  Revision 1.5  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 *  99/06/02, 1.01, Added methods to set the attribute's name and value.
 *                  Added method "compare" to compare objects.
 *                  Removed method "greater_than" to compare objects.
 */

package  DataSetTools.dataset;

import java.text.*;
import java.io.*;

/**
 * The concrete class for an attribute whose value is a float.  
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.StringAttribute
 * @see DataSetTools.dataset.IntAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 * @see DataSetTools.dataset.DetPosAttribute
 */

public class FloatAttribute extends Attribute
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
  private float value;

  /**
   * Constructs a FloatAttribute object using the specified name and value.
   */
  public FloatAttribute( String name, float value )
  {
    super( name );
    this.value = value;
  }

  public FloatAttribute(  )
  {
    super( "" );
    this.value = 0.0f;
  }
  /**
   * Returns the float value of this attribute, as a generic object.
   */
  public Object getValue( )
  {
    return( new Float(value) );
  } 

  /**
   * Set the value for the float attribute using a generic object.  The actual
   * class of the object must be a Float object.
   */
  public boolean setValue( Object obj )
  {
    if ( obj instanceof Double )
      value = ((Double)obj).floatValue();
    else if ( obj instanceof Float )
      value = ((Float)obj).floatValue();
    else if ( obj instanceof Integer )
      value = ((Integer)obj).floatValue();
    else
      return false;

    return true;
  }   

  /**
   * Returns the float value of this attribute as a float.
   */
   public float getFloatValue( )
   {
     return value;
   }

  /**
   * Set the value for the float attribute using a float.
   */
  public void setFloatValue( float value )
  {
    this.value = value;
  }

  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  The
   * new value is just the average of the values of the two attributes.
   *
   *  @param   attr   An attribute whose value is to be "combined" with the 
   *                  value of the this attribute.
   *
   */
  public void combine( Attribute attr )
  {
     this.value = (float)( this.value + attr.getNumericValue() ) / 2;
  }

  /**
   * Add the value of the specified attribute to the value of this
   * attribute obtain a new value for this attribute.  
   *
   *  @param   attr   An attribute whose value is to be "added" to the
   *                  value of the this attribute.
   *
   */
  public void add( Attribute attr )
  {
     this.value = (float)( this.value + attr.getNumericValue() );
  }

  public boolean XMLwrite( OutputStream stream, int mode )
    {return xml_utils.AttribXMLwrite( stream, mode, this);

     }
  public boolean XMLread( InputStream stream )
    {return xml_utils.AttribXMLread(stream, this);
    }

  /**
   * Get a numeric value to be used for sorting based on this attribute.
   */
   public double getNumericValue()
   {
     return value;
   }

  /**
   * Returns a string representation of the float value of this attribute
   */
  public String getStringValue()
  {
     NumberFormat f = NumberFormat.getInstance();
     return f.format( value );
  }

  /**
   * Returns a string representation of the (name,value) pair for this
   * attribute
   */
  public String toString()
  {
     return this.getName() + ": " + this.getStringValue();
  }

  /**
   * Returns a copy of the current attribute
   */
  public Object clone()
  {
    return new FloatAttribute( this.getName(), value );
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
      System.out.println("Warning:FloatAttribute IsawSerialVersion != 1");
  }

}

/*
 * File:  StringAttribute.java
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
 *  Revision 1.7  2002/08/01 22:33:35  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.6  2002/06/14 21:13:19  rmikk
 *  Implements IXmlIO interface
 *
 *  Revision 1.5  2002/06/05 20:28:56  dennis
 *  Now the combine() method will not form LABEL attributes longer than
 *  a specified MAX length.
 *
 *  Revision 1.4  2001/04/25 19:04:07  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.3  2000/07/10 22:24:05  dennis
 *  Now Using CVS 
 *
 *  Revision 1.5  2000/05/12 15:50:13  dennis
 *  removed DOS TEXT  ^M
 *
 *  Revision 1.4  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 *  99/06/02, 1.01, Added methods to set the attribute's name and value.
 *                  Added method "compare" to compare objects.
 *                  Removed method "greater_than" to compare objects.
 */

package  DataSetTools.dataset;
import java.io.*;
/**
 * The concrete class for an attribute whose value is a string.  
 *
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.Data
 *
 * @version 1.0  
 */

public class StringAttribute extends Attribute 
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
  private String value;

  /**
   * Constructs a StringAttribute object using the specified name and value.
   */
  public StringAttribute( String name, String value )
  {
    super( name );
    this.value = value;
  }

 public StringAttribute()
  {
    super( "" );
    this.value = "";
  }
  /**
   * Returns the String value of this attribute as a generic object. 
   */
  public Object getValue( )
  {
    return value;
  } 

  /**
   * Set the value for the String attribute using a generic object.  The actual
   * class of the object must be a String.
   */
  public boolean setValue( Object obj )
  {
    if ( !(obj instanceof String) )
      return false;

    value = (String)obj;
    return true;
  }  

  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  The
   * new value is obtained by concatenating the strings with a comma 
   * separator, provided that the two strings are not the same and that neither
   * string is a substring of the other. For an Attribute.LABEL value the
   * length is limited to the maximum label length.
   *
   *  @param   attr   An attribute whose string value is to be concatenated
   *                  with the value of the this attribute.
   */
  public void combine( Attribute attr )
  {
     if ( !(this.value.equalsIgnoreCase(attr.getStringValue()))    &&
            this.value.lastIndexOf( attr.getStringValue())  == -1  &&
            attr.getStringValue().lastIndexOf( this.value ) == -1  )
     {
       if ( this.name.equals( LABEL ) )                   // keep labels short 
       {
         if ( this.value.length() + attr.getStringValue().length() 
              > MAX_LABEL_LENGTH )
           return;                                        // don't concatenate
       }
       this.value = this.value + "," + attr.getStringValue();
     }
  }

  public boolean XMLwrite( OutputStream stream, int mode )
    {return xml_utils.AttribXMLwrite( stream, mode, this);

     }
  public boolean XMLread( InputStream stream )
    {return xml_utils.AttribXMLread(stream, this);
    }

  /**
   * Returns the String value of this attribute as a String.
   */
  public String getStringValue()
  {
     return value;
  }


  /**
   * Set the String value of this attribute using a String.
   */
  public void setStringValue( String value )
  {
     this.value = value;
  }


  /**
   * Returns the name and value strings for this attribute
   */
  public String toString()
  {
     return this.getName() + ": " + this.value;
  }

  /**
   * Returns a copy of the current attribute
   */
  public Object clone()
  {
    return new StringAttribute( this.getName(), value );
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
      System.out.println("Warning:StringAttribute IsawSerialVersion != 1");
  }

}

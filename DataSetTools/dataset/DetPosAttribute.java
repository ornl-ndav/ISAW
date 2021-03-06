/*
 * File:  DetPosAttribute.java
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
 *  Revision 1.16  2005/06/15 20:18:21  rmikk
 *  Added a static method to convert Detector Position to a Vector of Floats.
 *    The format of the Floats depends on the Field argument.  This is wrapped
 *    in an operator so that Scripts can have access to this information
 *
 *  Revision 1.15  2004/04/26 13:09:54  rmikk
 *  Made the null constructor public
 *  Added more documentation
 *
 *  Revision 1.14  2004/03/15 06:10:36  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.13  2004/03/15 03:28:05  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.12  2002/11/27 23:14:06  pfpeterson
 *  standardized header
 *
 *  Revision 1.11  2002/11/12 02:08:43  dennis
 *  Made immutable by:
 *  1. remove setValue() method
 *  2. add() & combine() methods now return a new Attribute
 *
 *  Also:
 *  3. Since it is now immutable, clone() method is not needed and
 *     was removed
 *  4. Default constructor is now private, since the value can't
 *     be set from outside of the class
 *
 *  Revision 1.10  2002/08/01 22:33:35  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.9  2002/06/14 20:59:48  rmikk
 *  Implements IXmlIO interface
 *
 */

package  DataSetTools.dataset;

import gov.anl.ipns.MathTools.Geometry.*;
import   java.io.*;
import DataSetTools.operator.Utils;
import java.util.*;
/**
 * The concrete class for an attribute whose value is a Position3D object.  
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.StringAttribute
 * @see DataSetTools.dataset.IntAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 *
 * @version 1.0  
 */

public class DetPosAttribute extends Attribute
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
  private DetectorPosition value;

  /**
   * Constructs a DetPosAttribute object using the specified name and value.
   */
  public DetPosAttribute( String name, DetectorPosition value )
  {
    super( name );
    this.value = new DetectorPosition( value );
  }

  public DetPosAttribute()
  {
    super("");
    this.value = new DetectorPosition();
  }

  /**
   * Returns a copy the DetectorPosition object that is the value of this
   * attribute, as a generic object.
   */
  public Object getValue( )
  {
     return new DetectorPosition( value );
  } 


  /**
   * Returns a copy the DetectorPosition object that is the value of this
   * attribute.
   */
   public DetectorPosition getDetectorPosition( )
   {
     return new DetectorPosition( value );
   }


   /**
     *  This method will write the information in this DetPosAttribute to the
     *  output stream in the xml format
     */
   public boolean XMLwrite( OutputStream stream, int mode )
    { 
      try
      {
       StringBuffer SS = new StringBuffer(1000);
       SS.append("<DetPosAttribute>\n<name>");
       SS.append(name);
       SS.append("</name>\n");
       SS.append( "<value>\n");
       stream.write( SS.substring(0).getBytes());
       if(!((Position3D)this.value).XMLwrite( stream, mode))
         return false;
       stream.write( "</value>\n</DetPosAttribute>\n".getBytes());
       return true;
          }
      catch( Exception s)
        {return xml_utils.setError( "IO Err="+s.getMessage());
         }
     }

  /**
    * This method reads Detector Position Attribute information from the
    * Input Stream and assigns the appropriate values to this Attribute.
    * NOTE: This method assumes the initial tag, but not the attributes
    * has been read
    */
  public  boolean XMLread( InputStream stream )
  {
    try{
//-----------------get name v
      String Tag = xml_utils.getTag( stream );
      if( Tag == null)
        return xml_utils.setError( xml_utils.getErrorMessage());
      if(!xml_utils.skipAttributes( stream))
         return xml_utils.setError( xml_utils.getErrorMessage());
      if( !Tag.equals("name"))
        return xml_utils.setError("name Tag Missing in Det pos");
      name = xml_utils.getValue( stream);
      if( name == null)
        return xml_utils.setError("name Tag Missing in Det pos");

      Tag =xml_utils.getEndTag( stream );
      if( Tag == null)
        return xml_utils.setError( xml_utils.getErrorMessage());
      if( !Tag.equals("/name"))
        return xml_utils.setError("name Tag not nested in Det pos");
      if(!xml_utils.skipAttributes( stream))
         return xml_utils.setError( xml_utils.getErrorMessage());

//----------------  get value field----------------
      Tag =xml_utils.getTag( stream );
      if( Tag == null)
        return xml_utils.setError( xml_utils.getErrorMessage());
      if( !Tag.equals("value"))
        return xml_utils.setError("missing value tag in Det Pos"+Tag); 
      if(!xml_utils.skipAttributes( stream))
         return xml_utils.setError( xml_utils.getErrorMessage());
//-----------actual values
      Tag =xml_utils.getTag( stream );
      if( Tag == null)
        return xml_utils.setError( xml_utils.getErrorMessage());
      if( !Tag.equals("DetectorPosition"))
        return xml_utils.setError(
                            "missing DetectorPosition tag in Det Pos"+Tag); 
      if(!xml_utils.skipAttributes( stream))
         return xml_utils.setError( xml_utils.getErrorMessage());
      value= new DetectorPosition();
      if(!(((Position3D)value).XMLread(stream)))
          return false;
//      if(!((Poition3D)this.value).XMLread( stream))
//          return null;
//-------------------- get End tags
      Tag =xml_utils.getTag( stream ); 
      if( Tag == null)
        return xml_utils.setError( xml_utils.getErrorMessage());
      if( !Tag.equals("/value"))
        return xml_utils.setError("Tags not nested in Pos 3D"+Tag);
      if(!xml_utils.skipAttributes( stream ))
        return xml_utils.setError( xml_utils.getErrorMessage());

      Tag =xml_utils.getTag( stream ); 
      if( Tag == null)
        return xml_utils.setError( xml_utils.getErrorMessage());
      if( !Tag.equals("/DetPosAttribute"))
        return xml_utils.setError("Tags not nested in Pos 3D"+Tag);
      if(!xml_utils.skipAttributes( stream ))
        return xml_utils.setError( xml_utils.getErrorMessage());
      return true;//new DetPosAttribute(name, (DetectorPosition)value);
      }
   
    catch( Exception s)
    { return xml_utils.setError( "Exception ="+s.getMessage());
     }
    }

  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  The
   * new value is obtained by "averaging" the 3D Positions represented by
   * the two attributes.  The "averaging" is done by averaging the 
   * components in spherical coordinates. 
   *
   *  @param   attr   A DetPosAttribute whose position is to be averaged 
   *                  with the value of the this attribute.
   */
  public Attribute combine( Attribute attr )
  {
     if ( !(attr instanceof DetPosAttribute) )       // can't combine
       return this;

     float pos[] = new float[3]; 
     float this_pos[],
           other_pos[];

     this_pos  = this.value.getSphericalCoords();
     other_pos = ((DetectorPosition)attr.getValue()).getSphericalCoords();

     for ( int i = 0; i < 3; i++ )
       pos[i] = ( this_pos[i] + other_pos[i] ) / 2.0f;
  
     DetectorPosition new_pos = new DetectorPosition();
     new_pos.setSphericalCoords( pos[0], pos[1], pos[2] );
     return new DetPosAttribute( name, new_pos );
  }


  /**
   * Add the specified position to this position to obtain an new position
   * value for this attribute.  
   *
   *  @param   attr   An attribute whose position value is to be "added" to the
   *                  position value of the this attribute.
   *
   */
  public Attribute add( Attribute attr )
  {
     if ( !(attr instanceof DetPosAttribute) )       // can't combine
       return this;

     float xyz[] = new float[3];
     float this_xyz[],
           other_xyz[];

     this_xyz  = this.value.getCartesianCoords();
     other_xyz = ((DetectorPosition)attr.getValue()).getCartesianCoords();

     for ( int i = 0; i < 3; i++ )
       xyz[i] = this_xyz[i] + other_xyz[i];

     DetectorPosition new_pos = new DetectorPosition();
     new_pos.setCartesianCoords( xyz[0], xyz[1], xyz[2] );
     return new DetPosAttribute( name, new_pos );
  }


  /**
   * Get a numeric value to be used for sorting based on this attribute.
   */
   public double getNumericValue()
   {
     return value.getScatteringAngle();
   }


  /**
   * Returns a string representation of the value of this attribute
   */
   public String getStringValue()
   {
     return value.toString();
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
      System.out.println("Warning:DetPosAttribute IsawSerialVersion != 1");
  }

  /**
   * This static method returns the appropriate information from the DetPos.
   * Used by an operator for Scripts
   * @param DetPos  the detector postion
   * @param Field   Either Spherical,Cartesian,Rectangular, or Cylindrical. 
   *           These names are NOT case sensitive and has only 3 character 
   *           significance
   * @return   A Vector of Floats representing corresponding information
   */

   public static java.util.Vector DetPosGet( DetectorPosition DetPos, String Field)throws
                         IllegalArgumentException
   {
      if( DetPos == null)
          throw new IllegalArgumentException("Null Detector Position in DetPosGet");
      if( Field == null)
          throw new IllegalArgumentException("Null Field name in DetPosGet");
       Field = Field.trim();
       if( Field.length() < 3)
          throw new IllegalArgumentException(" Improper Field name in DetPosGet");
       Field = Field.substring(0,3);
       if( ";SPH;REC;CYL;CAR;".indexOf(";"+Field.toUpperCase()+";")<0)
            throw new IllegalArgumentException(" Improper Field name in DetPosGet");
       Field = Field.toUpperCase();
       if( Field.startsWith("SPH"))
          return (Vector)Utils.ToVec(DetPos.getSphericalCoords());
       if( Field.startsWith("REC") || Field.startsWith("CAR"))
          return (Vector)Utils.ToVec(DetPos.getCartesianCoords());
       return (Vector)Utils.ToVec(DetPos.getCylindricalCoords());
   }
}

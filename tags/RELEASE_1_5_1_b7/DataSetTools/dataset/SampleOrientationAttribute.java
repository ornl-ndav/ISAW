/*
 * File:  SampleOrientationAttribute.java
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
 *  Revision 1.1  2003/02/18 19:00:20  dennis
 *  Initial version.
 *
 */

package  DataSetTools.dataset;

import DataSetTools.instruments.*;
import DataSetTools.dataset.*;
import java.io.*;

/**
 * This class is an Attribute whose value records information about the 
 * sample orientation angles, phi chi and omega.
 */

public class SampleOrientationAttribute extends Attribute
                                                   
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
  private SampleOrientation value = null;

  /**
   * Constructs a SampleOrientation object using the specified name 
   * and SampleOrientation object.
   */
  public SampleOrientationAttribute( String name, SampleOrientation value )
  {
    super( name );

    this.value = value;
  }

  /**
   * Returns reference to the SampleOrientation object kept by this attribute,
   * as a generic object. 
   */
  public Object getValue( )
  {
    return( value );
  } 

  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  Since
   * there is no meaningful way to combine the attributes, this just returns
   * a reference to the current attribute.
   *
   *  @param   attr   An attribute whose value is to be "combined" with the 
   *                  value of the this attribute.
   */
  public Attribute combine( Attribute attr )
  {
    return this;                                          
  }

  /**
   * Get a numeric value to be used for sorting based on this attribute.
   * In this case we return the sum of the angles.
   */
   public double getNumericValue()
   { 
     SampleOrientation so = (SampleOrientation)value;
     return so.getPhi() + so.getChi() + so.getOmega();
   }

  /**
   * Returns a string representation of the list of PixelInfo objects 
   */
  public String getStringValue()
  {
    SampleOrientation so = (SampleOrientation)value;
    return "phi="    + so.getPhi() + 
           ",chi="   + so.getChi() + 
           ",omega=" + so.getOmega();
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
                 "Warning:SampleOrientationAttribute IsawSerialVersion != 1");
  }


/* ---------------------------- main --------------------------------- */
/*
 *  Basic main program for testing
 */
 
  public static void main( String args[] )
  {
    IPNS_SCD_SampleOrientation so = new IPNS_SCD_SampleOrientation(20,30,40); 
    SampleOrientationAttribute attr = 
          new SampleOrientationAttribute( Attribute.SAMPLE_ORIENTATION, so ); 
    System.out.println("SampleOrientationAttribute is:");
    System.out.println(attr); 
  }

}

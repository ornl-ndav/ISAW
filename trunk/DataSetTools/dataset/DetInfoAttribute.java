/*
 * File:  DetInfoAttribute.java
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
 *  Revision 1.1  2001/07/10 18:48:03  dennis
 *  Attribute for infomation on individual detector positions,
 *  sizes, efficiency, etc.
 *
 */

package  DataSetTools.dataset;

import   java.text.*;
import   DataSetTools.math.*;

/**
 * The concrete class for an attribute whose value is a Position3D object.  
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.StringAttribute
 * @see DataSetTools.dataset.IntAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 * @see DataSetTools.dataset.DetPosAttribute
 *
 */

public class DetInfoAttribute extends Attribute
{
  private DetectorInfo value;

  /**
   * Constructs a DetInfoAttribute object using the specified name and value.
   */
  public DetInfoAttribute( String name, DetectorInfo value )
  {
    super( name );
    this.value = new DetectorInfo( value );
  }


  /**
   * Returns a copy the DetectorInfo object that is the value of this
   * attribute, as a generic object.
   */
  public Object getValue( )
  {
     return new DetectorInfo( value );
  } 


  /**
   * Set the value for this detector info attribute using a generic object.
   * The actual class of the object must be a DetectorInfo object.
   */
  public boolean setValue( Object obj )
  {
    if ( obj instanceof DetectorInfo )
      value = new DetectorInfo( (DetectorInfo)obj );
    else
      return false;

    return true;
  }   


  /**
   * Returns a copy the DetectorInfo object that is the value of this
   * attribute.
   */
   public DetectorInfo getDetectorInfo( )
   {
     return new DetectorInfo( value );
   }


  /**
   *  NOP... can't combine two DetInfoAttributes
   */
  public void combine( Attribute attr )
  {
    // Can't combine DetInfoAttributes, so just leave it unchanged.
  }


  /**
   * NOP... can't add two DetInfoAttributes.
   */
  public void add( Attribute attr )
  {
    // Can't add DetInfoAttributes, so just leave it unchanged.
  }


  /**
   * Get a numeric value to be used for sorting based on this attribute.
   */
   public double getNumericValue()
   {
     return value.getSeg_num();
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


  /**
   * Returns a copy of the current attribute
   */
  public Object clone()
  {
    return new DetInfoAttribute( this.getName(), value );
  }
}

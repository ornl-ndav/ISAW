/*
 * File:  DoubleAttribute.java
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
 *  Revision 1.5  2001/04/25 19:03:46  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.4  2000/07/26 14:52:23  dennis
 *  Now includes method to add() attributes.
 *
 *  Revision 1.3  2000/07/10 22:23:58  dennis
 *  Now using CVS 
 *
 *  Revision 1.5  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 */

package  DataSetTools.dataset;

import java.text.*;

/**
 * The concrete class for an attribute whose value is a double.  
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.StringAttribute
 * @see DataSetTools.dataset.IntAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.DetPosAttribute
 *
 * @version 1.0  
 */

public class DoubleAttribute extends Attribute
{
  private double value;

  /**
   * Constructs a DoubleAttribute object using the specified name and value.
   */
  public DoubleAttribute( String name, double value )
  {
    super( name );
    this.value = value;
  }


  /**
   * Returns the double value of this attribute, as a generic object.
   */
  public Object getValue( )
  {
    return( new Double(value) );
  } 

  /**
   * Set the value for the double attribute using a generic object.  The actual
   * class of the object must be a Double object.
   */
  public boolean setValue( Object obj )
  {
    if ( obj instanceof Double )
      value = ((Double)obj).doubleValue();
    else if ( obj instanceof Float )
      value = ((Float)obj).doubleValue();
    else if ( obj instanceof Integer )
      value = ((Integer)obj).doubleValue();
    else
      return false;

    return true;
  }   

  /**
   * Returns the value of this attribute as a double.
   */
   public double getDoubleValue( )
   {
     return value;
   }

  /**
   * Set the value for the double attribute using a double.
   */
  public void setDoubleValue( double value )
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
     this.value = ( this.value + attr.getNumericValue() ) / 2;
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
     this.value = ( this.value + attr.getNumericValue() );
  }


  /**
   * Get a numeric value to be used for sorting based on this attribute.
   */
   public double getNumericValue()
   {
     return value;
   }

  /**
   * Returns a string representation of the double value of this attribute
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
    return new DoubleAttribute( this.getName(), value );
  }
}

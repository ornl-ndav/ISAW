/*
 * File: ColorAttribute.java
 *
 * Copyright (C) 2001, Kevin Neff
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
 * $Log$
 * Revision 1.2  2002/11/27 23:25:12  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/07/18 22:06:19  dennis
 * Moved separate OverplotView hiearchy into DataSetTools/viewer
 * hierarchy.
 *
 */

package DataSetTools.viewer.OverplotView;

import java.awt.Color;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;

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
 */

public class ColorAttribute 
  extends Attribute
{
  private Color value;


  /**
   * Constructs a DoubleAttribute object using the specified name and value.
   */
  public ColorAttribute( String name, Color value )
  {
    super( name );
    this.value = value;
  }


  /**
   * Returns the Color value of this attribute, as a generic object.
   */
  public Object getValue( )
  {
    return new Color(  value.getRGB()  );
  } 


  /**
   * Set the value for the double attribute using a generic object.  The actual
   * class of the object must be a Double object.
   */
  public boolean setValue( Object obj )
  {
    value = (Color)obj;
    return true;
  }   


  /**
   * Returns the value of this attribute as a double.
   */
   public Color getColorValue( )
   {
     return value;
   }


  /**
   * Returns a string representation of the double value of this attribute
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
    return new ColorAttribute( this.getName(), value );
  }
}

package  OverplotView;

/*
 * $Id$
 * ----------
 * $Log$
 * Revision 1.1  2001/06/29 16:28:17  neffk
 * attribute for storing color information in AttributeList objects
 *
 * ----------
 */

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

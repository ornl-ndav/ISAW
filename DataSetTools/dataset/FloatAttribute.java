/*
 * @(#)FloatAttribute.java       1.01 99/06/02  Dennis Mikkelson
 *
 *  99/06/02, 1.01, Added methods to set the attribute's name and value.
 *                  Added method "compare" to compare objects.
 *                  Removed method "greater_than" to compare objects.
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.3  2000/07/10 22:23:59  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.6  2000/05/12 15:44:34  dennis
 *  *** empty log message ***
 *
 *  Revision 1.5  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 *
 */

package  DataSetTools.dataset;

import java.text.*;

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
  private float value;

  /**
   * Constructs a FloatAttribute object using the specified name and value.
   */
  public FloatAttribute( String name, float value )
  {
    super( name );
    this.value = value;
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
}

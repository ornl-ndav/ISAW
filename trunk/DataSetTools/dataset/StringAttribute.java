/*
 * @(#)StringAttribute.java    1.01 99/06/02  Dennis Mikkelson
 *
 *  99/06/02, 1.01, Added methods to set the attribute's name and value.
 *                  Added method "compare" to compare objects.
 *                  Removed method "greater_than" to compare objects.
 */

package  DataSetTools.dataset;

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
  private String value;

  /**
   * Constructs a StringAttribute object using the specified name and value.
   */
  public StringAttribute( String name, String value )
  {
    super( name );
    this.value = value;
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
   * string is a substring of the other.
   *
   *  @param   attr   An attribute whose string value is to be concatenated
   *                  with the value of the this attribute.
   */
  public void combine( Attribute attr )
  {
     if ( !(this.value.equalsIgnoreCase(attr.getStringValue()))    &&
            this.value.lastIndexOf( attr.getStringValue())  == -1  &&
            attr.getStringValue().lastIndexOf( this.value ) == -1  )
       this.value = this.value + "," + attr.getStringValue();
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

}

/*

 * @(#)IntAttribute.java       1.0 99/06/09  Dennis Mikkelson

 *

 */



package  DataSetTools.dataset;



/**

 * The concrete class for an attribute whose value is an int.  

 *

 * @see DataSetTools.dataset.Data

 * @see DataSetTools.dataset.Attribute

 * @see DataSetTools.dataset.StringAttribute

 * @see DataSetTools.dataset.FloatAttribute

 * @see DataSetTools.dataset.DoubleAttribute

 * @see DataSetTools.dataset.DetPosAttribute

 *

 * @version 1.0  

 */



public class IntAttribute extends Attribute

{

  private int value;



  /**

   * Constructs an IntAttribute object using the specified name and value.

   */

  public IntAttribute( String name, int value )

  {

    super( name );

    this.value = value;

  }





  /**

   * Returns the int value of this attribute, as a generic object.

   */

  public Object getValue( )

  {

    return( new Integer(value) );

  } 



  /**

   * Set the value for the int attribute using a generic object.  The actual

   * class of the object must be an Int.

   */

  public boolean setValue( Object obj )

  {

    if ( obj instanceof Double )

      value = ((Double)obj).intValue();

    else if ( obj instanceof Float )

      value = ((Float)obj).intValue();

    else if ( obj instanceof Integer )

      value = ((Integer)obj).intValue();

    else

      return false;



    return true;

  }   



  /**

   * Returns the int value of this attribute as an int.

   */

   public int getIntegerValue( )

   {

     return value;

   }



  /**

   * Set the value for the int attribute using an int.

   */

  public void setIntValue( int value )

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

     this.value = (int)( this.value + attr.getNumericValue() ) / 2;

  }





  /**

   * Get a numeric value to be used for sorting based on this attribute.

   */

   public double getNumericValue()

   {

     return value;

   }



  /**

   * Returns a string representation of the int value of this attribute

   */

  public String getStringValue()

  {

     return Integer.toString(value);

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

    return new IntAttribute( this.getName(), value );

  }

}


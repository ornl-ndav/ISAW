/*
 * @(#)Parameter.java     .1 99/06/04  Dennis Mikkelson
 *
 */

package  DataSetTools.operator;
import java.io.*;

/**
 * The class Parameter groups a name and generic object that is used to 
 * pass parameters between a GUI and an operator.
 *
 * @version 0.1  
 */

public class Parameter extends Object implements Serializable
{
  private String name;
  private Object value;

  /**
   *  Construct a Parameter object using the specified name an value
   */
  public Parameter( String name, Object value )
  {
    this.name  = name;
    this.value = value;
  }

  /**
   * Returns the name of the parameter
   */
  public String getName() 
  { 
    return name; 
  }

  /**
   * Returns the value of the parameter, as a generic object
   */
  public Object getValue() 
  {  
    return value;
  } 

  /**
   * Set the value for the parameter
   */
  public void setValue( Object object ) 
  { 
    value = object;
  }

  /**
   * Return a copy of the parameter object.  Note: Currently this is not a
   * "deep copy", but it does create a new Parameter object.  When the value
   * is set for this new parameter object, it will not affect the value of
   * the current parameter object.  Consequently, this "clone" method will
   * meet our current needs.  A "deep copy" would require the ability to 
   * copy an Object of any type.
   */
  public Object clone()
  {
    Parameter new_parameter = new Parameter( name, value );
    return new_parameter;
  }


}

/*
 * @(#)IntListAttribute.java       1.0 99/06/09  Dennis Mikkelson
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.5  2000/09/11 23:02:57  dennis
 *  Now converts list of integers to compact string form n:m,i:j,...
 *
 *  Revision 1.4  2000/07/13 22:23:03  dennis
 *  Removed control-M characters
 *
 *  Revision 1.3  2000/07/10 22:24:02  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.5  2000/05/12 15:50:13  dennis
 *  removed DOS TEXT  ^M
 *
 *  Revision 1.4  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 *
 */

package  DataSetTools.dataset;

import DataSetTools.util.*;

/**
 * The concrete class for an attribute whose value is a list of integers.  
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

public class IntListAttribute extends Attribute
{
  private int[] values;

  /**
   * Constructs an IntListAttribute object using the specified name and values.
   */
  public IntListAttribute( String name, int values[] )
  {
    super( name );

    this.values = new int[ values.length ];
    System.arraycopy( values, 0, this.values, 0, values.length );
  }


  /**
   * Returns the list of int values of this attribute, as a generic object.
   */
  public Object getValue( )
  {
    int new_array[] = new int[ values.length ];
    System.arraycopy( values, 0, new_array, 0, values.length );

    return( new_array );
  } 

  /**
   * Set the value for the int list attribute using a generic object.  The 
   * actual class of the object must be an array of int.
   */
  public boolean setValue( Object obj )
  {
    if ( obj instanceof int[] )
    {
      int length  = ((int[])obj).length;
      this.values = new int[ length ];
      System.arraycopy( (int[])obj, 0, this.values, 0, length );
    }
    else
      return false;

    return true;
  }   

  /**
   * Returns the int list value of this attribute as an array of ints.
   */
   public int[] getIntegerValue( )
   {
     int new_array[] = new int[ values.length ];
     System.arraycopy( values, 0, new_array, 0, values.length );

     return( new_array );
   }

  /**
   * Set the value for the integer list attribute using an integer list.
   */
  public void setIntValue( int values[] )
  {
    this.values = new int[ values.length ];
    System.arraycopy( values, 0, this.values, 0, values.length );
  }

  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  The
   * new value is the result of merging the two lists of integers.
   *
   *  @param   attr   An attribute whose value is to be "combined" with the 
   *                  value of the this attribute.
   *
   */
  public void combine( Attribute attr )
  {
    if ( !(attr instanceof IntListAttribute) )         // can't do it so don't 
      return;                                          
       
    IntListAttribute other_attr = (IntListAttribute)attr; 

    boolean  found;
    int      i;
    int      temp[] = new int [ values.length + other_attr.values.length ];
 
    int num_used = values.length;       
                                                  // start with the integers 
    for ( i = 0; i < values.length; i++ )         // from this attribute's list
      temp[i] = values[i];

                                                  // append any integers from
                                                  // the new list that are not
                                                  // in the original list 
    for ( int k = 0; k < other_attr.values.length; k++ )
    {                                             
      found = false;                             
      i = 0;
      while ( !found && i < values.length )
        if (  other_attr.values[k] == values[i] )
          found = true;
        else
          i++;
      if ( !found )                               // append and count this
      {                                           // new integer
        temp[ num_used ] = other_attr.values[k];
        num_used++;
      }       
    }    
                                                  // now copy the values into
    values = new int[ num_used ];                 // a new array for this
    for ( i = 0; i < values.length; i++ )         // attribute
      values[i] = temp[i];
  }


  /**
   * Get a numeric value to be used for sorting based on this attribute.
   */
   public double getNumericValue()
   {                                   // use first entry as "numeric value"
     if ( values.length > 0 )          // if there is one.
       return values[0];
     else
       return Double.MAX_VALUE;
   }

  /**
   * Returns a string representation of the list of integers 
   */
  public String getStringValue()
  {
    return IntList.ToString( values );
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
    return new IntListAttribute( this.getName(), values );
  }
}

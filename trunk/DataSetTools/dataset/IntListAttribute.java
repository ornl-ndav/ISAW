/*
 * File:  IntListAttribute.java
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
 *  Revision 1.7  2001/04/25 19:03:57  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.6  2001/04/20 19:56:43  dennis
 *  Now guarantees that an IntListAttribute will keep the list of
 *  integer in increasing order.  Also, the combine() method now uses
 *  a "merge" operation to combine the lists more efficiently and keep them
 *  in order.
 *
 *  Revision 1.5  2000/09/11 23:02:57  dennis
 *  Now converts list of integers to compact string form n:m,i:j,...
 *
 *  Revision 1.4  2000/07/13 22:23:03  dennis
 *  Removed control-M characters
 *
 *  Revision 1.3  2000/07/10 22:24:02  dennis
 *  Now Using CVS 
 *
 *  Revision 1.5  2000/05/12 15:50:13  dennis
 *  removed DOS TEXT  ^M
 *
 *  Revision 1.4  2000/05/11 16:00:45  dennis
 *  Added RCS logging
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

    boolean in_order = true;
    for ( int i = 0; i < values.length - 1; i++ )
      if ( values[i] > values[i+1] )
        in_order = false;

    if ( !in_order ) 
    {
      System.out.println("Warning: values not ordered in IntListAttribute");
      arrayUtil.sort( this.values );
    }
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

    int      temp[]   = new int [ values.length + other_attr.values.length ];
    int      i        = 0,
             j        = 0,
             num_used = 0;
                                                             // merge the lists 
    while ( i < values.length && j < other_attr.values.length )    
    {
      if ( values[ i ] < other_attr.values[ j ] )
      {
        temp[ num_used ] = values[i];
        i++;
      }
      else if ( values[ i ] > other_attr.values[ j ] )    
      {
        temp[ num_used ] = other_attr.values[ j ];
        j++;
      }
      else
      {
        temp[ num_used ] = values[ i ];
        i++;
        j++;
      }
      num_used++;
    }

    if ( i < values.length )
    {
      System.arraycopy( values, i, temp, num_used, values.length - i );
      num_used += values.length - i;
    }
 
    else
    {
      System.arraycopy( other_attr.values, j, temp, num_used, 
                        other_attr.values.length - j );
      num_used += other_attr.values.length - j;
    }
      
                                                  // now copy the values into
    values = new int[ num_used ];                 // a new array for this
                                                  // attribute
    System.arraycopy( temp, 0, values, 0, num_used );
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

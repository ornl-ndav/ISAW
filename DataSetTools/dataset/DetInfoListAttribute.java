/*
 * File:  DetInfoListAttribute.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 *  Revision 1.2  2001/07/10 19:37:14  dennis
 *  Now imports DataSetTools.instruments.*
 *
 *  Revision 1.1  2001/07/10 18:48:49  dennis
 *  Attribute for infomation on list of individual
 *  detector segment positions, sizes, efficiency, etc.
 *
 */

package  DataSetTools.dataset;

import DataSetTools.util.*;
import DataSetTools.instruments.*;

/**
 * The concrete class for an attribute whose value is a list of DetectorInfo
 * objects.  
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.StringAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 * @see DataSetTools.dataset.DetPosAttribute
 * @see DataSetTools.dataset.DetInfoAttribute
 *
 * @version 1.0  
 */

public class DetInfoListAttribute extends Attribute
{
  private DetectorInfo[] values;

  /**
   * Constructs a DetInfoListAttribute object using the specified name 
   * and values.
   */
  public DetInfoListAttribute( String name, DetectorInfo values[] )
  {
    super( name );
    setValue( values );
  }


  /**
   * Returns reference to the list of DetectorInfo objects for this attribute,
   * as a generic object.
   */
  public Object getValue( )
  {
    return( values );
  } 

  /**
   * Set the value for the int list attribute using a generic object.  The 
   * actual class of the object must be an array of DetectorInfo objects.
   */
  public boolean setValue( Object obj )
  {
    if ( obj == null )
      System.out.println("ERROR: null object in DetInfoListAttribute.setValue");

    if ( obj instanceof DetectorInfo[] )
    {
      DetectorInfo  new_vals[] = (DetectorInfo[])obj;

      int length  = new_vals.length;
      values = new DetectorInfo[ length ];

      for ( int i = 0; i < values.length; i++ )
        values[i] = new DetectorInfo( new_vals[i] );

      boolean in_order = true;
      for ( int i = 0; i < values.length - 1; i++ )
        if ( values[i].getSeg_num() > values[i+1].getSeg_num() )
          in_order = false;

      if ( !in_order )
      {
        System.out.println("Warning: " + 
                           "values not ordered in DetInfoListAttribute");
       // #### need yet another sort method!!!   arrayUtil.sort( this.values );
      }
    }
    else
      return false;
    
    return true;
  }   

  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  The
   * new value is the result of merging the two lists of DetectorInfo objects.
   *
   *  @param   attr   An attribute whose value is to be "combined" with the 
   *                  value of the this attribute.
   */
  public void combine( Attribute attr )
  {
    if ( !(attr instanceof DetInfoListAttribute) )      // can't do it so don't 
      return;                                          
       
    DetInfoListAttribute other_attr = (DetInfoListAttribute)attr; 

    DetectorInfo temp[] = new DetectorInfo[
                                       values.length+other_attr.values.length ];
    int i        = 0,
        j        = 0,
        num_used = 0;
                                                             // merge the lists 
    while ( i < values.length && j < other_attr.values.length )    
    {
      if ( values[ i ].getSeg_num() < other_attr.values[ j ].getSeg_num() )
      {
        temp[ num_used ] = values[i];
        i++;
      }
      else if ( values[ i ].getSeg_num() > other_attr.values[ j ].getSeg_num() )
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
    values = new DetectorInfo[ num_used ];        // a new array for this
                                                  // attribute
    System.arraycopy( temp, 0, values, 0, num_used );
  }


  /**
   * Get a numeric value to be used for sorting based on this attribute.
   */
   public double getNumericValue()
   {                                   // use first entry as "numeric value"
     if ( values.length > 0 )          // if there is one.
       return values[0].getSeg_num();
     else
       return Double.MAX_VALUE;
   }

  /**
   * Returns a string representation of the list of DetectorInfo objects 
   */
  public String getStringValue()
  {
    String s = new String();
    for ( int i = 0; i < values.length; i++ )
      s += values[i].toString();

    return s;
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
    return new DetInfoListAttribute( this.getName(), values );
  }
}

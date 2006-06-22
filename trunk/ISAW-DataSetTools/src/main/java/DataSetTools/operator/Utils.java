/*
 * File:   GetDSAttribute.java 
 *             
 * Copyright (C) 2000, Ruth Mikkelson, Dennis Mikkelson
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
 * Modified:
 *
 * This operator gets a DataSet Attribute
 *
 *  $Log$
 *  Revision 1.1  2005/06/08 22:34:52  rmikk
 *  Initial Checkin
 *
*/
package DataSetTools.operator;
import java.util.*;
import java.lang.reflect.*;
import DataSetTools.dataset.*;
public class Utils{
  
  /**
   * This method attempts to convert an Object to a Vector. If the
   * Object is an array of arrays of arrays of... it will convert it
   * to a Vector of Vectors of Vectors
   * @param O  The Object to be converted
   * @return   The Vectorified Object if possible, otherwise the Object is
   *            returned unchanged.
   */
  public static Object ToVec( Object O){
    Vector Res = new Vector();
    if( O == null)
      return null;
    if( O instanceof Vector){
       for( int i=0; i< ((Vector)O).size(); i++)
          Res.add( ToVec(((Vector)O).elementAt(i)));
       return Res;
       
    }
   if( O.getClass().isArray()){
      for( int i=0; i< Array.getLength(O); i++)
         Res.add( ToVec( Array.get(O,i)));
         return Res;
   }
   if( O.getClass().equals(float.class))
       return new Float(O.toString().trim());
  
   if( O.getClass().equals(int.class))
     return new Integer(O.toString().trim());
   if( O instanceof String)
     return O;
   if( O.getClass().equals( byte.class))
     return new Byte(O.toString().trim());
   if( O.getClass().equals( long.class))
     return new Long(O.toString().trim());
   if( O.getClass().equals( short.class))
     return new Short(O.toString().trim());
    if( O.getClass().equals(double.class))
      return new Double(O.toString().trim());
      
  return O;
  }
  
  
  
  /**
   * Returns an XScale for the index-th entry of the Data Set DS
   * @param DS  The Data Set with the data block of interest
   * @param index The index of the data block of interest
   * @return The XScale as a Vector of Floats.
   */
  public static Vector GetXScaleAsVec( DataSet DS, int index){
      if(DS == null)
         return new Vector();
      if( index <0)
         return new Vector();
      if( index >= DS.getNum_entries())
         return new Vector();
      float[] f = DS.getData_entry( index ).getX_scale().getXs();
      return (Vector)ToVec( f );
  }
  
  
  
  
  public static Vector GetYvaluesAsVec( DataSet DS, int index){
    
    if(DS == null)
       return new Vector();
    if( index <0)
       return new Vector();
    if( index >= DS.getNum_entries())
       return new Vector();
    float[] f = DS.getData_entry( index ).getY_values();
    return (Vector)ToVec( f );
    
  }
  
  
  
  
  public static void main( String[] args){
    int[][][] X ={ { {1,2,3},{4,5,6},{7,8,9}
    },{ {-1,-2,-3},{-4,-5,-6},{-7,-8,-9}}
    };
    Object O = Utils.ToVec( X );
    System.out.println("O class="+O.getClass());
    
    Command.ScriptUtil.display(O);
  }
}
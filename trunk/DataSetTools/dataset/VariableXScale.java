/*
 * File:  VariableXScale.java     
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
 *  Revision 1.5  2001/04/25 19:04:13  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *
 *  Revision 1.4  2000/12/07 22:29:00  dennis
 *  Added method extend().
 *  Added main() with test code for extend() method.
 *
 *  Revision 1.3  2000/07/10 22:24:07  dennis
 *  Now using CVS 
 *
 *  Revision 1.3  2000/05/12 15:50:13  dennis
 *  removed DOS TEXT  ^M
 *
 *  Revision 1.2  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 */

package DataSetTools.dataset;
import java.io.*;
import DataSetTools.util.*;

/**
 * The class for variable "X" scales that consist of a sequence of points
 * in an interval with arbitrary spacing between points.  The points must
 * be monotonic.  
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.XScale
 * @see DataSetTools.dataset.UniformXScale
 *
 * @version 1.0  
 */


public class VariableXScale extends XScale implements Serializable 
{
  private float x[] = { 0, 1 }; 

  /**
   * Constructs a VariableXScale object by specifying a list of x values.
   * The list of "X" values must be monotonic.
   */
  public VariableXScale( float x[] )
  {
    super( x[0], x[x.length-1], x.length );

    int num_x = x.length;
    if ( num_x > 1 )
    {
      boolean x_increasing = true;
      boolean x_decreasing = true;
      for ( int i = 0; i < num_x - 1; i++ )
      {
        if ( x[i] >= x[i+1] )
          x_increasing = false;
        if ( x[i] <= x[i+1] )
          x_decreasing = false;
      }

      if ( !x_increasing && !x_decreasing )
        System.out.println("ERROR: x_values not monotonic in " +
                           "VariableXScale constructor" );
      else  
      {
        this.x = null;                  // valid X scale, so make a copy of it
        this.x = new float[ x.length ];
        System.arraycopy( x, 0, this.x, 0, x.length ); 
      } 
    }  
  }

  /**
   * Returns the array of "X" values specified when the constructor was called.
   */
  public float[] getXs()
  {
    float copy[] = new float[ x.length ];
    System.arraycopy( x, 0, copy, 0, x.length );
    return( copy );
  }

  /**
   *  Constructs a new VariableXScale that extends over the smallest interval
   *  containing both this XScale and the specifed XScale.  The points of the 
   *  the current XScale are used, except for any interval covered by the
   *  other XScale and NOT covered by the current XScale.  In that case, the
   *  points of the other XScale are used.
   *
   *  @param  other_scale  The x scale that is used to extend the current XScale
   *
   *  @return  A new VariableXScale that covers the union of the intervals 
   *           covered by the current XScale and the other XScale.  
   */
   public XScale extend( XScale other_scale )
   {
     float other_x[] = other_scale.getXs();
 
     float temp[] = new float[ x.length + other_x.length ];
     int   n_used      = 0;

     if ( x[0] <= other_x[0] )                 // start with the points in x[]
     {
       for ( int i = 0; i < x.length; i++ )
         temp[i] = x[i];
   
       n_used = x.length;
       if ( other_x[ other_x.length-1 ] > x[ x.length-1 ] )  // some points in
       {                                                     // other_x needed
         if ( other_x[0] > x[ x.length-1 ] )        // all are other_x's needed
         {
           for ( int i = 0; i < other_x.length; i++ )
             temp[ n_used + i ] = other_x[i];
           n_used += other_x.length;
         }    
         else                                       // just use some other_x's
         {
           int index = arrayUtil.get_index_of( temp[ n_used - 1 ], other_x );
           for ( int i = index+1; i < other_x.length; i++ )
             temp[ n_used + i - (index+1) ] = other_x[i];

           n_used += other_x.length - (index+1); 
         }
       }
     }

     else                                   // start with the points in other_x
     {
       while ( n_used < other_x.length && other_x[n_used] < x[0] )
       {
         temp[ n_used ] = other_x[n_used];
         n_used++;  
       }                                    // copy the points from x
    
       for ( int i = 0; i < x.length; i++ )
         temp[ n_used + i ] = x[i];

       n_used += x.length;
       
       if (  other_x[ other_x.length-1 ] > x[ x.length-1 ] ) // more points from
       {                                                     // other_x needed

         int index = arrayUtil.get_index_of( temp[ n_used - 1 ], other_x );
         for ( int i = index+1; i < other_x.length; i++ )
           temp[ n_used + i - (index+1) ] = other_x[i];

         n_used += other_x.length - (index+1);
       }
     }
                                             // copy the x's used into a new
                                             // array with the right length 
     float new_x[] = new float[ n_used ];
     for ( int i = 0; i < n_used; i++ )
       new_x[i] = temp[i];

     return new VariableXScale( new_x );     // return the new XScale
   }


  /**
   * Creates a new VariableXScale object with the same data as the original
   * VariableXScale object.
   */
  public Object clone()
  {
    VariableXScale copy = new VariableXScale( x );
    return copy;
  }


  /*
   * main program for basic testing only
   */
  public static void main( String args[] )
  {
    float  vals[]   =  {  10.0f, 10.4f, 10.8f, 11.2f, 11.6f, 12.0f };
    XScale scale    = new VariableXScale( vals );

    XScale u1_scale = new UniformXScale(  8.5f,  9.5f, 11 );
    XScale u2_scale = new UniformXScale(  9.5f, 10.5f, 11 );
    XScale u3_scale = new UniformXScale( 10.5f, 11.5f, 11 );
    XScale u4_scale = new UniformXScale( 11.5f, 12.5f, 11 );
    XScale u5_scale = new UniformXScale( 12.5f, 13.5f, 11 );
    XScale u6_scale = new UniformXScale(  9.5f, 12.5f, 11 );

    float v1_x[] = {   8.5f,  8.75f,  9.0f,  9.25f,  9.5f };
    float v2_x[] = {   9.5f,  9.75f, 10.0f, 10.25f, 10.5f };
    float v3_x[] = {  10.5f, 10.75f, 11.0f, 11.25f, 11.5f };
    float v4_x[] = {  11.5f, 11.75f, 12.0f, 12.25f, 12.5f };
    float v5_x[] = {  12.5f, 12.75f, 13.0f, 13.25f, 13.5f };
    float v6_x[] = {   9.5f, 10.0f,  11.0f, 12.0f,  12.5f };

    XScale v1_scale = new VariableXScale ( v1_x );
    XScale v2_scale = new VariableXScale ( v2_x );
    XScale v3_scale = new VariableXScale ( v3_x );
    XScale v4_scale = new VariableXScale ( v4_x );
    XScale v5_scale = new VariableXScale ( v5_x );
    XScale v6_scale = new VariableXScale ( v6_x );

//    XScale extended = scale.extend( u1_scale );     // uniform scale extend
//    XScale extended = scale.extend( u2_scale );
//    XScale extended = scale.extend( u3_scale );
//    XScale extended = scale.extend( u4_scale );
//    XScale extended = scale.extend( u5_scale );
//    XScale extended = scale.extend( u6_scale );

//    XScale extended = scale.extend( v1_scale );    // variable scale extend
//    XScale extended = scale.extend( v2_scale );
//    XScale extended = scale.extend( v3_scale );
//    XScale extended = scale.extend( v4_scale );
//    XScale extended = scale.extend( v5_scale );
    XScale extended = scale.extend( v6_scale );
    
    float x[] = extended.getXs();
    for ( int i = 0; i < x.length; i++ )
      System.out.println( x[i] );
  }


}

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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.10  2003/02/24 13:33:54  dennis
 *  Added method restrict() to restrict an XScale to the intersection
 *  of the XScale and a ClosedInterval.
 *
 *  Revision 1.9  2002/11/27 23:14:07  pfpeterson
 *  standardized header
 *
 *  Revision 1.8  2002/11/12 19:59:32  dennis
 *  Removed clone() method... since XScales are immutable, there is no
 *  need to clone them.
 *
 *  Revision 1.7  2002/08/01 22:33:35  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.6  2002/06/10 20:19:31  dennis
 *  Added getI(x) and getX(i) methods to get individual points and positions
 *  of individual points in the list.
 *
 */

package DataSetTools.dataset;
import java.io.*;
import java.util.*;
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
  // NOTE: any field that is static or transient is NOT serialized.
  //
  // CHANGE THE "serialVersionUID" IF THE SERIALIZATION IS INCOMPATIBLE WITH
  // PREVIOUS VERSIONS, IN WAYS THAT CAN NOT BE FIXED BY THE readObject()
  // METHOD.  SEE "IsawSerialVersion" COMMENTS BELOW.  CHANGING THIS CAUSES
  // JAVA TO REFUSE TO READ DIFFERENT VERSIONS.
  //
  public  static final long serialVersionUID = 1L;


  // NOTE: The following fields are serialized.  If new fields are added that
  //       are not static, reasonable default values should be assigned in the
  //       readObject() method for compatibility with old servers, until the
  //       servers can be updated.

  private int IsawSerialVersion = 1;         // CHANGE THIS WHEN ADDING OR
                                             // REMOVING FIELDS, IF
                                             // readObject() CAN FIX ANY
                                             // COMPATIBILITY PROBLEMS
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
   *  Get the ith x-value from this XScale.
   *
   *  @param  i    The position of the x-value that is needed.  This should be
   *               between 0 and the number_of_x_values - 1.
   *
   *  @return The x value in position i in the "list" of x-values for this
   *          x scale.  If there is no such x value, this will return Float.NaN.   */
  public float getX( int i )
  {
    if ( i < 0 || i >= num_x )
      return Float.NaN;

    return x[i];
  }


  /**
   *  Get the position of the specified x-value in this XScale.
   *
   *  @param  x_value   The x value to find in the "list" of x values 
   *                    represented by this x scale.
   *
   *  @return The position "i" in the list of x-values, where the specified
   *          x occurs, if it is in the list.  If the specified x is less
   *          than or equal to the last x in the "list", this function returns
   *          the index of the first x that is greater than or equal to the
   *          specified x.  If the specified x value is above the end of
   *          the x scale, the number of points in the x scale is returned.
   */
  public int getI( float x_value )
  {
    int position = Arrays.binarySearch( x, x_value );
    if ( position < 0 )
      position = -( position + 1 );

    return position;
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
   *  Constructs a new XScale that is the restriction of the current
   *  XScale to the intersection of the ClosedInterval and the interval
   *  of the current XScale.  If the intersection is empty, this method
   *  returns null.
   *
   *  @param   interval  the interval the XScale is restricted to.
   *
   *  @return  A new VariableXScale is returned if the intersection is 
   *           non-empty, or null is returned if the intersection is empty.
   */
   public XScale restrict( ClosedInterval interval )
   {
     float min = interval.getStart_x();
     float max = interval.getEnd_x();

     if ( min >= end_x )
       return null;

     if ( max <= start_x )
       return null;

     int i_min = getI( min );
     int i_max = getI( max );

     float new_xs[] = new float[ i_max - i_min + 1 ];
     for ( int i = 0; i < new_xs.length; i++ )
       new_xs[i] = x[ i + i_min ];

     return new VariableXScale( new_xs );
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

    System.out.println("Position of 0    " + extended.getI( 0.0f ) ); 
    System.out.println("Position of 8    " + extended.getI( 8.0f ) ); 
    System.out.println("Position of 9    " + extended.getI( 9.0f ) ); 
    System.out.println("Position of 9.5  " + extended.getI( 9.5f ) ); 
    System.out.println("Position of 9.75 " + extended.getI( 9.75f ) ); 
    System.out.println("Position of 10   " + extended.getI( 10.0f ) ); 
    System.out.println("Position of 12.5 " + extended.getI( 12.5f ) ); 
    System.out.println("Position of 130  " + extended.getI( 130.0f ) ); 
  }

/* -----------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

/* ---------------------------- readObject ------------------------------- */
/**
 *  The readObject method is called when objects are read from a serialized
 *  ojbect stream, such as a file or network stream.  The non-transient and
 *  non-static fields that are common to the serialized class and the
 *  current class are read by the defaultReadObject() method.  The current
 *  readObject() method MUST include code to fill out any transient fields
 *  and new fields that are required in the current version but are not
 *  present in the serialized version being read.
 */

  private void readObject( ObjectInputStream s ) throws IOException,
                                                        ClassNotFoundException
  {
    s.defaultReadObject();               // read basic information

    if ( IsawSerialVersion != 1 )
      System.out.println("Warning:VariableXScale IsawSerialVersion != 1");
  }

}

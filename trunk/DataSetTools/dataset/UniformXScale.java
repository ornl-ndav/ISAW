/*
 * File:  UniformXScale.java     
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
 *  Revision 1.12  2003/07/09 14:57:14  dennis
 *  Updated java docs for getI() method.
 *
 *  Revision 1.11  2003/02/24 13:33:54  dennis
 *  Added method restrict() to restrict an XScale to the intersection
 *  of the XScale and a ClosedInterval.
 *
 *  Revision 1.10  2002/11/27 23:14:07  pfpeterson
 *  standardized header
 *
 *  Revision 1.9  2002/11/12 21:58:50  dennis
 *  Removed clone() method, since UniformXScales are immutable, it is
 *  not needed.  Also, made "step" private.
 *
 *  Revision 1.8  2002/08/01 22:33:35  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.7  2002/06/11 20:51:38  dennis
 *  Moved "step" instance variable before javadoc comment for the constructor
 *  to avoid problem with javadoc.
 *
 *  Revision 1.6  2002/06/10 20:19:17  dennis
 *  Added getI(x) and getX(i) methods to get individual points and positions
 *  of individual points in the list.
 *  Added instance variable "step" to avoid recalculating it every time it's
 *  needed.
 *
 */

package DataSetTools.dataset;

import java.io.*;
import DataSetTools.util.*;

/**
 * The class for "X" scales that consist of evenly spaced points along a
 * a specified interval.  The evenly spaced points are specified by giving
 * the end points of the interval and the number of points to create.
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.XScale
 * @see DataSetTools.dataset.VariableXScale
 *
 * @version 1.0  
 */

public class UniformXScale extends XScale implements Serializable
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
  private double step; 

  /**
   * Constructs a UniformXScale object by specifying the starting x, ending x 
   * and number of x values to be used.   For example, if the scale was to run 
   * from 0 to 1000 in steps of 10, the values 0, 1000, 101 would be passed 
   * to this constructor.  The number of x values must be at least 1.  If
   * the number of x values is greater than 1, then the start and end x values
   * must be unequal.  If the number of x values is equal to 1, then the
   * start and end x values should be the same.  If they are not, only the
   * start value will actually be used.
   *
   * @param   start_x  the starting x
   * @param   end_x    the ending x  
   * @param   num_x    the number of x values.  
   *
   * @see DataSetTools.dataset.XScale
   */

   public UniformXScale( float start_x, float end_x, int num_x )
   {
     super( start_x, end_x, num_x );
     if ( num_x > 1 )
       step = (end_x - start_x) / (double)(num_x - 1);
     else
       step = 0;
   }


  /**
   * @return the array of "X" values.  The array will have num_x entries.   
   * The "X" values are uniformly spaced and are calculated from start_x, 
   * end_x and num_x.
   */
  public float[] getXs()
  {
    float x[] = new float[num_x];

    for ( int i = 0; i < num_x; i++ )
      x[i] = (float)( start_x + i * step );

    return x;
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

    return (float)(start_x + i * step);
  }


  /**
   *  Get the position (or index of the LUB) of the specified x-value in
   *  this XScale.
   *
   *  @param  x    The x value to find in the "list" of x values represented
   *               by this x scale.
   *
   *  @return The position "i" in the list of x-values, where the specified  
   *          x occurs, if it is in the list.  If the specified x is less
   *          than or equal to the last x in the "list", this function returns
   *          the index of the first x that is greater than or equal to the 
   *          specified x.  If the specified x value is above the end of 
   *          the x scale, the number of points in the x scale is returned.  
   *          This is the index of the Least Upper Bound (chosen from the
   *          list of values in the XScale) for the specified x_value.
   */
  public int getI( float x )
  {
    int position;

    if (step <= 0 )                             // one point x_scale
    {
      if ( x <= start_x )
        return 0;
      else
        return 1;
    }
    else                                        // non-degenerate scale
    {
      position = (int)Math.ceil( (x-start_x)/step ); 
      if ( position < 0 )
        position = 0;
      else if ( position > num_x )
        position = num_x;     

      if ( position > 0 )
      {
        if ( getX(position-1) == x )
          return position - 1; 
      }
      return position;
    }
  }


  /**
   * Get the separation between successive x values for this XScale.
   *
   * @return  The distance between two successive x values in this XScale.
   */
  public double getStep()
  {
    return step;
  }


  /**
   *  Constructs a new UniformXScale that extends over the smallest interval
   *  containing both this XScale and the specifed XScale.  The number of 
   *  x-values to use is extended to the larger of the numbers of x-values 
   *  in this XScale and the specified XScale. 
   */
   public UniformXScale expand( XScale scale )
   {
     float temp_start_x = Math.min( this.start_x, scale.start_x );
     float temp_end_x   = Math.max( this.end_x,   scale.end_x );
     int   temp_num_x   = Math.max( this.num_x,   scale.num_x );

     return new UniformXScale( temp_start_x, temp_end_x, temp_num_x ); 
   }


  /**
   *  Constructs a new UniformXScale that extends over the smallest interval
   *  containing both this XScale and the specifed XScale.  The spacing between
   *  points is the same as for the current XScale and the start_x of the
   *  new XScale is choosen so that the division points of the current XScale
   *  are still used.
   *
   *  @param  other_scale  The x scale that is used to extend the current XScale
   *
   *  @return  A new UniformXScale is returned that covers the union
   *           of the intervals covered by the current XScale and the
   *           other XScale.
   */

   public XScale extend( XScale other_scale )
   {
     float temp_start_x = Math.min( this.start_x, other_scale.start_x );
     float temp_end_x   = Math.max( this.end_x,   other_scale.end_x );
     int   temp_num_x   = Math.max( this.num_x,   other_scale.num_x );

                                                       // keep current delta_x
     if ( num_x <= 1 )
       return new UniformXScale( temp_start_x, temp_end_x, temp_num_x );

     int n_steps_to_start = (int)Math.round((start_x-temp_start_x)/step);
     temp_start_x = (float)(start_x - n_steps_to_start * step);

     int n_steps_to_end = (int)Math.round((temp_end_x-temp_start_x)/step); 
     temp_end_x = (float)(temp_start_x + n_steps_to_end * step);

     temp_num_x = (int)Math.round( (temp_end_x - temp_start_x) / step ) + 1;

     return new UniformXScale( temp_start_x, temp_end_x, temp_num_x );
   }

  /**
   *  Constructs a new XScale that is the restriction of the current
   *  XScale to the intersection of the ClosedInterval and the interval
   *  of the current XScale.  If the intersection is empty, this method
   *  returns null.
   *
   *  @param   interval  the interval the XScale is restricted to.
   *
   *  @return  A new XScale of the same type ( Uniform or Variable ) as the
   *           current XScale is returned if the intersection is non-empty,
   *           or null if the intersection is empty.
   */
   public XScale restrict( ClosedInterval interval )
   {
     float min = interval.getStart_x();
     float max = interval.getEnd_x();
  
     if ( min > end_x )
       return null;
 
     if ( max < start_x )
       return null;

     int i_min = getI( min );
     int i_max = getI( max );

     min = getX( i_min );
     max = getX( i_max );

     return new UniformXScale( min, max, i_max - i_min + 1 );
   }


  /*
   * main program for basic testing only
   */
  public static void main( String args[] )
  {
    XScale    scale = new UniformXScale(    10,    12, 11 );
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
    XScale extended = scale.extend( u6_scale );

//    XScale extended = scale.extend( v1_scale );    // variable scale extend
//    XScale extended = scale.extend( v2_scale );
//    XScale extended = scale.extend( v3_scale );
//    XScale extended = scale.extend( v4_scale );
//    XScale extended = scale.extend( v5_scale );
//    XScale extended = scale.extend( v6_scale );

    float x[] = extended.getXs();
    for ( int i = 0; i < x.length; i++ )
      System.out.println( x[i] );

    System.out.println("Position of 0    " + extended.getI( 0.0f ) );
    System.out.println("Position of 8    " + extended.getI( 8.0f ) );
    System.out.println("Position of 9    " + extended.getI( 9.0f ) );
    System.out.println("Position of 9.4  " + extended.getI( 9.4f ) );
    System.out.println("Position of 9.75 " + extended.getI( 9.75f ) );
    System.out.println("Position of 10   " + extended.getI( 10.0f ) );
    System.out.println("Position of 12.4 " + extended.getI( 12.4f ) );
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
      System.out.println("Warning:UniformXScale IsawSerialVersion != 1");
  }

}

/*
 * File:  GeometricProgressionXScale.java     
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2005/11/13 03:10:29  dennis
 *  Initial version of XScale object for "logarithmic" x scales.
 *  The points are of the form xk = x0 * r^k, for a fixed ratio r.
 *
 */

package DataSetTools.dataset;

import gov.anl.ipns.Util.Numeric.*;
import java.io.*;

/**
 * The class for "X" scales that consist of points forming a geometric 
 * progression, xN = x0 * r^N  along a specified interval.  The points 
 * are constructed, as needed, from a starting point, the length of the 
 * first interval, and a nominal ending point.  Points up to and including
 * the first xK = x0 * r^K that is greater than or equal to a specified
 * ending point are included in the XScale.
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.XScale
 * @see DataSetTools.dataset.UniformXScale
 * @see DataSetTools.dataset.VariableXScale
 *
 * @version 1.0  
 */

public class GeometricProgressionXScale extends XScale implements Serializable
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
  private double ratio; 

  /**
   *   Constructs a GeometricProgressionXScale object by specifying the 
   * starting x, nominal ending x and first interval length.  The starting x
   * and first interval length must be more than 0.  Also, since the ratio
   * is determined as r=(start_x+first_step)/start_x, this ratio must evaluate
   * to a number strictly more than 1.   If this is not the case an 
   * IllegalArgumentException will be thrown.
   *
   *   For example, if the scale was to run from 100 to 1000, increasing by
   * 5% at each step, the scale would be constructed specifying 
   * start_x = 100, end_x = 1000, and first interval length = 5.  The points
   * of the XScale would be:
   *
   *    x0=100, x1=105, x2=110.25, ..., x48=1040.127
   *
   *   On the other hand, specifying start_x = 100000, first_step = 0.00001
   * is not allowed, because (start_x+first_step)/start_x to the accuracy of
   * floating point calculations.
   *
   * @param   start_x     the first point in the XScale 
   * @param   end_x       the approximate last point in the XScale.  Values
   *                      are generated in a geometric progression, up to 
   *                      and including the first value that is greater than
   *                      or equal to the specified end_x value.  
   * @param   first_step  the length of the first subinterval 
   *                      (this must be positive)  
   *
   * @see DataSetTools.dataset.XScale
   * @see DataSetTools.dataset.UniformXScale
   * @see DataSetTools.dataset.VariableXScale
   */

   public GeometricProgressionXScale( float start_x, 
                                      float end_x, 
                                      float first_step )
   {
     super( start_x, end_x, 10 );

     if ( end_x < start_x )
       throw new IllegalArgumentException( "start_x > end_x " + start_x + 
                                           " > " + end_x);

     ratio = (start_x + (double)first_step)/start_x;
     
     if ( ((float)ratio) <= 1 )
       throw new IllegalArgumentException( "ratio not greater than 1: "+ratio);

     double num_vals = (Math.log(end_x/(double)start_x) / Math.log( ratio ));
     num_x = (int)Math.ceil( num_vals ) + 1;

     this.end_x = getX( num_x - 1 );
   }


  /**
   *   Static method to create a GeometricProgressionXScale.  This static 
   * method is used by the Operator that creates a GeometricProgressionXScale.
   *  
   * @param   start_x     the first point in the XScale 
   * @param   end_x       the approximate last point in the XScale.  Values
   *                      are generated in a geometric progression, up to 
   *                      and including the first value that is greater than
   *                      or equal to the specified end_x value.  
   * @param   first_step  the length of the first subinterval 
   *                      (this must be positive)  
   *
   * @return  a GeometricProgressionXScale 
   */

   public static GeometricProgressionXScale 
                 createGeometricProgressionXScale( float start_x,
                                                   float end_x,
                                                   float first_step )
   {  
     return new GeometricProgressionXScale( start_x, end_x, first_step );
   }
   
   
  /**
   *  Get the array of "X" values for this XScale.  NOTE: The array of 
   * values is generated on demand, not stored as part of the XScale object.
   *
   * @return the array of "X" values.  The array will have num_x entries.   
   * The "X" values follow a geometric progression starting with "start_x".
   */
  public float[] getXs()
  {
    float x[] = new float[num_x];

    x[0] = start_x;
    double temp = start_x;
    for ( int i = 1; i < num_x; i++ )
    {
      temp *= ratio;
      x[i] = (float)temp;
    }
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

    if ( i == 0 )
      return start_x;
    else
      return (float)( start_x * Math.pow( ratio, i ) );
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

    if ( x <= start_x )                      // off the left edge of the scale
      return 0;                                

    if ( x > end_x )                         // off the right edge of the scale
      return num_x;

    if ( x == end_x )                        // right endpoint.
      return num_x - 1;
                                             // if we get here, we have a
                                             // non-degenerate scale with x
                                             // strictly between end points.

    position = (int)Math.ceil( Math.log(x/(double)start_x)/Math.log(ratio) ); 

    if ( x > getX(position) )                // this can happed due to rounding
      position++;                            // errors

    else if ( getX(position-1) == x )
      position--; 

    return position;
  }


  /**
   * Get the ratio between successive x values for this XScale.
   *
   * @return  The ratio between two successive x values in this XScale.
   */
  public double getRatio()
  {
    return ratio;
  }


  /**
   *  Constructs a new GeometricProgressionXScale that extends over the 
   *  smallest interval containing both this XScale and the specifed XScale. 
   *  The ratio for the geometric progression is the same as for the 
   *  current XScale and the start_x of the new XScale is choosen so that 
   *  the division points of the current XScale are still used.  NOTE: since
   *  the values are calculated, not stored, there may be slight differences
   *  between the original values and the new values due to rounding errors.
   *
   *  @param  other_scale  The x scale that is used to extend the current 
   *                       XScale
   *
   *  @return  A new GeometricProgressionXScale is returned that covers the
   *           union of the intervals covered by the current XScale and the
   *           other XScale, preserving the original x-values from this XScale
   *           that are in the interval covered by both XScales.
   */
   public XScale extend( XScale other_scale )
   {
     float  temp_end_x = Math.max( this.end_x, other_scale.end_x );
     float  temp_start_x;
     float  first_step;

     if ( this.start_x <= other_scale.start_x )     // use current start_x
       temp_start_x = this.start_x;

     else                                         // calculate smaller start_x
     {
       double n_back = Math.log( this.start_x / (double)other_scale.start_x ) /
                       Math.log( ratio );
       n_back = Math.floor( n_back );
       temp_start_x = (float)( this.start_x * Math.pow( ratio, -n_back ));
     }

     first_step = (float)( (ratio-1)*temp_start_x );

     GeometricProgressionXScale temp =
        new GeometricProgressionXScale( temp_start_x, temp_end_x, first_step );

     ClosedInterval interval = new ClosedInterval( temp_start_x, temp_end_x );
     return (GeometricProgressionXScale)temp.restrict( interval ); 
   }


  /**
   *  Constructs a new GeometricProgressionXScale that is the restriction 
   *  of the current XScale to the intersection of the ClosedInterval and 
   *  the interval of the current XScale.  The starting and ending points
   *  of the new XScale will be selected from points that are in the
   *  current XScale.  As a result, the new XScale may not cover the entire
   *  closed interval. If the intersection is empty, this method returns null.
   *
   *  @param   interval  the interval the XScale is restricted to.
   *
   *  @return  A new GeometricProgressionXScale is returned if the 
   *           intersection is non-empty, and null is returned otherwise. 
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
     int i_max = getI_GLB( max );

     min = getX( i_min );
     max = getX( i_max );

     float first_step = (float)( (ratio - 1) * (double)min );

                                     // we should hit this max point exactly,
     max = max - first_step/2;       // but because of rounding we might not,
                                     // so back up a bit.

     return new GeometricProgressionXScale( min, max, first_step );
   }


  /**
   * main program for basic testing only
   */
  public static void main( String args[] )
  {
    XScale scale = new GeometricProgressionXScale( 1000, 2000, 5f );
    System.out.println();
    scale.Print( "1000 to 2000 increasing by 0.5%", SOME, 5 );
  
    XScale         scale_2;
    XScale         other_scale;

/*
    //
    // Tests for restrict() method
    //
    ClosedInterval interval;
    interval = new ClosedInterval( 1000, 1500 );
    scale_2 = scale.restrict( interval ); 
    System.out.println();
    System.out.println("Scale restricted to " + interval );
    scale_2.Print("restricted scale.....", SOME, 5 );

    ClosedInterval interval = new ClosedInterval( 1500, 2006.7634f );
    scale_2 = scale.restrict( interval );
    System.out.println();
    System.out.println("Scale restricted to " + interval );
    scale_2.Print("restricted scale.....", SOME, 5 );

    ClosedInterval interval = new ClosedInterval( 900, 2100 );
    scale_2 = scale.restrict( interval );
    System.out.println();
    System.out.println("Scale restricted to " + interval );
    scale_2.Print("restricted scale.....", SOME, 4 );

    ClosedInterval interval = new ClosedInterval( 1030.301f, 1430.7688f );
    scale_2 = scale.restrict( interval );
    System.out.println();
    System.out.println("Scale restricted to " + interval );
    scale_2.Print("restricted scale.....", SOME, 5 );

    ClosedInterval interval = new ClosedInterval( 1025, 1425 );
    scale_2 = scale.restrict( interval );
    System.out.println();
    System.out.println("Scale restricted to " + interval );
    scale_2.Print("restricted scale.....", SOME, 6 );
*/
    //
    // Tests for extend() method
    //
    other_scale = new UniformXScale( 900, 1500, 10 );
    scale_2 = scale.extend( other_scale );
    System.out.println();
    System.out.println("Scale extended by " + other_scale );
    scale_2.Print("extended scale.....", SOME, 25 );

    other_scale = new UniformXScale( 1500, 2000.2422f, 10 );
    scale_2 = scale.extend( other_scale );
    System.out.println();
    System.out.println("Scale extended by " + other_scale );
    scale_2.Print("extended scale.....", SOME, 10 );

    other_scale = new UniformXScale( 1500, 2200, 10 );
    scale_2 = scale.extend( other_scale );
    System.out.println();
    System.out.println("Scale extended by " + other_scale );
    scale_2.Print("extended scale.....", SOME, 25 );

    other_scale = new UniformXScale( 900, 2200, 10 );
    scale_2 = scale.extend( other_scale );
    System.out.println();
    System.out.println("Scale extended by " + other_scale );
    scale_2.Print("extended scale.....", SOME, 25 );

    //
    // Consistency tests for getX(), getXs(), getI(), getI_GLB()
    //
    int error_count = 0;
    System.out.println();
    System.out.print( "Testing getX(getI(x))... " );
    error_count = Test_GetXofGetI( scale ); 
    System.out.println("Error count = " + error_count );

    System.out.println();
    System.out.print( "Testing getXs() and getX()... " );
    error_count = XScale.Test_GetXsGetX( scale );
    System.out.println("Error count = " + error_count );

    int n_points = 1000000;
    System.out.println();
    System.out.println( "Testing getI() and getI_GLB() using " + n_points );
    System.out.print( " points (this takes time)... " );
    error_count = XScale.Test_GetI_GetI_GLB( scale, n_points );
    System.out.println("Error count = " + error_count );
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

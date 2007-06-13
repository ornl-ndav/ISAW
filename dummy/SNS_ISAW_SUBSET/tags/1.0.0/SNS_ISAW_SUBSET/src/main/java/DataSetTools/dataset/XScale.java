/*
 * File:  XScale.java     
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
 *
 *  $Log: XScale.java,v $
 *  Revision 1.22  2005/11/13 03:07:23  dennis
 *  Added Print() method for debugging.
 *  Added test methods to test getX(), getXs(), getI(), getI_GLB().
 *
 *  Revision 1.21  2005/11/10 22:44:17  dennis
 *  Clarified role of MACHINE_EPSILON in checking whether a VariableXScale
 *  is essentially equal to a UniformXScale.  Also made single and double
 *  precision versions of MACHINE_EPSILON into public static final values.
 *
 *  Revision 1.20  2004/03/15 06:10:39  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.19  2004/03/15 03:28:09  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.18  2003/07/09 14:39:40  dennis
 *  The getI(x) method returns the index of the "Least Upper Bound"
 *  of x in the x-values of this XScale.  The new method getI_GLB(x)
 *  returns the "Greatest Lower Bound" of x in the x-values of this
 *  XScale.
 *
 *  Revision 1.17  2003/02/24 13:33:54  dennis
 *  Added method restrict() to restrict an XScale to the intersection
 *  of the XScale and a ClosedInterval.
 *
 *  Revision 1.16  2002/11/27 23:14:07  pfpeterson
 *  standardized header
 *
 *  Revision 1.15  2002/11/12 19:46:25  dennis
 *  Removed clone() method... since XScales are immutable, there is
 *  no need to clone() them.  Made start_x, end_x and num_x protected.
 *
 *  Revision 1.14  2002/08/01 22:33:35  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.13  2002/07/19 19:30:27  dennis
 *  getInstance() method now returns a uniform XScale if all
 *  specified points are within single precision tolerance of
 *  the points of a uniform XScale.  This is less effiecient,
 *  but returns a uniform XScale in more cases than the
 *  previous version.
 *
 *  Revision 1.12  2002/07/17 22:18:33  pfpeterson
 *  Defined the equals method that all objects should have.
 *
 *  Revision 1.11  2002/07/10 20:21:12  dennis
 *  Removed debug print.
 *
 *  Revision 1.10  2002/07/10 20:06:41  dennis
 *  Added getInstance() method to get uniform or variable XScale, as
 *  appropriate, from an array of x values.
 *
 *  Revision 1.9  2002/07/10 15:59:29  pfpeterson
 *  Added inRange() method.
 *
 *  Revision 1.8  2002/06/10 20:19:52  dennis
 *  Added getI(x) and getX(i) methods to get individual points and positions
 *  of individual points in the list.
 *
 */

package  DataSetTools.dataset;

import gov.anl.ipns.Util.Numeric.*;

import java.util.*;
import java.io.*;

/**
 * The abstract root class for "X" scales used in data objects.  
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.UniformXScale
 * @see DataSetTools.dataset.VariableXScale
 *
 * @version 1.0  
 */

abstract public class XScale implements Serializable
{
  // NOTE: any field that is static or transient is NOT serialized.
  //
  // CHANGE THE "serialVersionUID" IF THE SERIALIZATION IS INCOMPATIBLE WITH
  // PREVIOUS VERSIONS, IN WAYS THAT CAN NOT BE FIXED BY THE readObject()
  // METHOD.  SEE "IsawSerialVersion" COMMENTS BELOW.  CHANGING THIS CAUSES
  // JAVA TO REFUSE TO READ DIFFERENT VERSIONS.
  //
  public  static final long serialVersionUID = 1L;

  // Machine epsilon is the largest value, eps, for which 1 + eps == 1.
  // The values of eps are listed below for float and double, correct to the
  // number of digits shown.
  //
  public static final double MACHINE_EPSILON_D   = 1.1102230246251565E-16;
  public static final float  MACHINE_EPSILON     = 5.9604645E-8f;
  public static final float  TWO_MACHINE_EPSILON = 2*MACHINE_EPSILON;
  
  // Flags for terms of XScale to print
  //
  public static final int    BEGINNING           = 0;
  public static final int    MIDDLE              = 1;
  public static final int    ENDING              = 2;
  public static final int    SOME                = 3;
  public static final int    ALL                 = 4;


  // NOTE: The following fields are serialized.  If new fields are added that
  //       are not static, reasonable default values should be assigned in the
  //       readObject() method for compatibility with old servers, until the
  //       servers can be updated.

  private int IsawSerialVersion = 1;         // CHANGE THIS WHEN ADDING OR
                                             // REMOVING FIELDS, IF
                                             // readObject() CAN FIX ANY
                                             // COMPATIBILITY PROBLEMS
  protected float  start_x = 0;
  protected float  end_x   = 1;
  protected int    num_x   = 2;

  /**
   * Constructs an XScale object by specifying the starting x, ending x and
   * number of x values to be used.  Since XScale is an abstract class, this
   * constructor is never used directly.  It provides validity checks
   * on the input paramters for the concrete classes derived from XScale.
   *
   * @see DataSetTools.dataset.UniformXScale
   * @see DataSetTools.dataset.VariableXScale
   */
  protected  XScale( float start_x, float end_x, int num_x )
  { 
    if ( num_x < 1 )                                    // invalid x scale
    {
      System.out.println( "ERROR: num_x less than 1 in " +
                          "XScale constructor" );
      System.out.println(" start_x = " + start_x +
                         " end_x = "   + end_x   +
                         " num_x = "   + num_x );
      num_x = 1;                                        // set default
    }

    if ( num_x == 1 )                                   // force valid end_x
      end_x = start_x;                                  // if only one x
    
    if ( num_x > 1 )
      if ( start_x == end_x )                           // force usable end_x
      {                              
        System.out.println( "ERROR: start_x = end_x but num_x > 1 in "+
                            "XScale constructor... using default end_x" );
        System.out.println(" start_x = " + start_x +
                           " end_x = "   + end_x   +
                           " num_x = "   + num_x );
        end_x = start_x + 1;
      }

    this.start_x = start_x;
    this.end_x   = end_x;
    this.num_x   = num_x;
  }

  /**
   *  Returns an XScale with the specified x values.  If the x values are
   *  uniformly spaced, a UniformXScale is returned, otherwise a
   *  VariableXScale is returned.
   *
   *  @param  x     The array of x values used to generate the XScale
   *
   *  @return   If the entries in x[] are evenly spaced, return a UniformXScale
   *            corresponding to those x values, otherwise return a 
   *            VariableXScale.  If the array x[] is empty or null, this 
   *            returns null.
   */
  public static XScale getInstance( float x[] )
  {
    if ( x == null || x.length == 0 )
      return null;
    else if ( x.length > 2 )
    {                             // check for non-degenerate UniformXScale
      XScale uniform_scale = new UniformXScale( x[0], x[x.length-1], x.length );
      float uniform_x[] = uniform_scale.getXs();
                                               // if relative error > EPSILON
      for ( int i = 0; i < x.length; i++ )     // use the Variable XScale
        if ( x[i] != 0 )
        {
          if ( Math.abs((uniform_x[i] - x[i]) / x[i]) > TWO_MACHINE_EPSILON )
            return new VariableXScale( x );
        }
        else if ( uniform_x[i] != 0 )
          if (Math.abs((uniform_x[i] - x[i]) / uniform_x[i]) >
                                                        TWO_MACHINE_EPSILON )
            return new VariableXScale( x );
    }
                             // if not Variable && not too short, it's Uniform  
    return new UniformXScale( x[0], x[x.length-1], x.length );
  }


  /**
   * Get the starting "X" value
   *
   * @return the first x value in this XScale 
   */
  public float getStart_x() 
  { 
     return start_x; 
  }


  /**
   * Get the ending "X" value
   *
   * @return the last x value in this XScale
   */
  public float getEnd_x()   
  { 
     return end_x; 
  }


  /**
   * Get the number of "X" values 
   *
   * @return the number of points in this XScale
   */
  public int getNum_x() 
  { 
    return num_x; 
  }


  /**
   * Determines if the specified "X" is within the range of the
   * Xscale.
   */
  public boolean inRange( float val ){
      if( val>=start_x && val<=end_x )
          return true;
      else
          return false;
  }


  /**
   * Returns the array of "X" values.  The array will have num_x entries.   
   * For a UniformXScale object, the values will be uniformly spaced.  
   * For a VariableXScale ojbect, the values may be arbitrarily spaced, 
   * as specified when the object was constructed.
   */
  abstract public float[] getXs();


  /**
   *  Get the ith x-value from this XScale.
   *
   *  @param  i    The position of the x-value that is needed.  This should be
   *               between 0 and the number_of_x_values - 1.
   *
   *  @return The x value in position i in the "list" of x-values for this
   *          x scale.  If there is no such x value, this will return Float.NaN.
   */
  abstract public float getX( int i );


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
  abstract public int getI( float x );


  /**
   *  Get the position (or index of the GLB) of the specified x-value in this 
   *  XScale.
   *
   *  @param  x_value   The x value to find in the "list" of x values 
   *                    represented by this x scale.
   *
   *  @return The position "i" in the list of x-values, where the specified
   *          x occurs, if it is in the list.  If the specified x is greater 
   *          than or equal to the first x in the "list", this function returns
   *          the index of the last x that is less than or equal to the
   *          specified x.  If the specified x value is below the start of
   *          the x scale, -1 is returned.
   *          This is the index of the Greatest Lower Bound (chosen from the
   *          list of values in the XScale) for the specified x_value.
   */
  public int getI_GLB( float x_value ) 
  {
    int position = getI( x_value );
    if ( getX( position ) == x_value )
      return position;
    else
      return position - 1;
  }


  /**
   *  Constructs a new XScale that extends over the smallest interval
   *  containing both this XScale and the specifed XScale.  The points of the
   *  the current XScale are used, except for any interval covered by the
   *  other XScale and NOT covered by the current XScale.  
   *
   *  @param  other_scale  The x scale that is used to extend the current XScale
   *
   *  @return  A new XScale of the same type ( Uniform or Variable ) as the
   *           current XScale is returned.  The new XScale covers the union
   *           of the intervals covered by the current XScale and the
   *           other XScale.  See the subclasses for specific behavior.
   */
   abstract public XScale extend( XScale other_scale );


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
   abstract public XScale restrict( ClosedInterval interval );


  /**
   * Compare this XScale to another one. Unless the two scales are
   * genuinely the same object the method returns true
   * quickly. Otherwise the method is written to speed up returning
   * false.
   */
  public boolean equals( Object other_scale ){
      //set the tolerance for comparisons
      float  tol   = 0f;
      // get a real XScale ready for later convenience
      XScale other = null;

      // return false if another object is not specified
      if(other_scale==null)return false;

      // make sure we are comparing to an XScale
      if(other_scale instanceof XScale)
          other=(XScale)other_scale;
      else
          return false;
      
      // if they are the same object return true
      if(this==other_scale)return true;

      // if have different start values can't be same
      if(Math.abs(this.getStart_x()-other.getStart_x())>tol)return false;

      // if have different end values can't be same
      if(Math.abs(this.getEnd_x()-other.getEnd_x())>tol)return false;

      // if have different number of values can't be same
      if(Math.abs(this.getNum_x()-other.getNum_x())>tol)return false;

      // if they are both UniformXScale they are already the same
      if((this instanceof UniformXScale)&&(other instanceof UniformXScale))
          return true;

      // now just brute force it
      float[] this_x=this.getXs();
      float[] other_x=other.getXs();
      try{
          for( int i=0 ; i<this_x.length ; i++ ){
              // all x's must be equal or return false
              if(!(this_x[i]==other_x[i]))return false;
          }
      }catch(ArrayIndexOutOfBoundsException e){
          // if we hit this exception they cannot be the same thing
          return false; 
      }

      // we've passed all the tests, they must be the same
      return true;
  }


  /**
   *  Print the range and number of X values in this x scale
   */
  public String toString()
  {
    return "["+start_x+","+end_x+"]" + " in " + num_x + " steps"; 
  }


  /**
   *  Print part of all of this XScale, depending on what print mode is
   *  specified.
   *
   *  @param  message  text message at start of printout
   *  @param  mode     integer flag BEGINNING, MIDDLE, ENDING, SOME or ALL
   *  @param  num      number of elements to print, in cases BEGININNG, 
   *                   MIDDLE or ENDING.
   */
  public void Print( String message, int mode, int num )
  {
    if ( message != null && message.length() > 0 )
      System.out.println( message );

    int num_to_print = num_x;
    if ( num_to_print > num )
      num_to_print = num;

    int first = 0,
        last;

    if ( mode == BEGINNING || mode == ALL )
      first = 0;
    else if ( mode == MIDDLE )
    {
      first = num_x / 2 - num/2;
      if ( first < 0 )
        first = 0;
    }
    else if ( mode == ENDING )
    {
      first = num_x - num_to_print;
      if ( first < 0 )
        first = 0;
    }
    else if ( mode == SOME )
    {
      if ( num_x < 30 )
      {
        Print( message, ALL, num_x );
        return;
      }
      Print( "", BEGINNING, num );
      System.out.println("...");
      Print( "", MIDDLE, num );
      System.out.println("...");
      Print( "", ENDING, num );
      return;
    }
    
    if ( mode == ALL )
      num_to_print = num_x;

    last = first + num_to_print - 1;
    if ( last > num_x - 1 )
      last = num_x - 1;

    for ( int i = first; i <= last; i++ )
      System.out.println( "" + i + ",  " + getX(i) );
  }


  /**
   *  Verify that the points returned in an array by getXs() are the
   *  same as those returned by individual calls to getX(). 
   *
   *  @param  scale      The XScale to test
   *
   *  @return the number of points where errors occured.
   */
  public static int Test_GetXsGetX( XScale scale )
  {
    float xs[] = scale.getXs();
    int   error_count = 0;

    for ( int i = 0; i < xs.length; i++ )
    {
      if ( scale.getX(i) != xs[i] )
      {
        error_count++;
        System.out.println("*** ERROR: getX(i) != xs[i] " +
                            scale.getX(i) + " != " +
                            xs[i] );
      }
    }
    return error_count;
  }


  /**
   *  Verify that getX(getI(x)) == x for every point "x" in the XScale.
   *
   *  @param  scale      The XScale to test
   *
   *  @return the number of points where errors occured.
   */
  public static int Test_GetXofGetI( XScale scale )
  {
    float x;
    int   error_count = 0;
    for ( int i = 0; i < scale.getNum_x(); i++ )
    {
      x = scale.getX(i);
      if ( x != scale.getX( scale.getI(x) ) )
      {
        System.out.println("ERROR:  getX(getI(x)) != x" );
        System.out.println("getX(getI(x)) = " + scale.getX( scale.getI(x) ));
        System.out.println("x = " + x );
        error_count++;
      }
    }
    return error_count;
  }


  /**
   *  Verify that getI(x) and getI_GLB(x) return correct values 
   *  for a collection of randomly generated "xs" in the interval 
   *  [start_x-1, end_x+1].  For values in [start_x, end_x] this 
   *  requires that getX(getI_GLB(x)) <= x <= getX(getI(x)).
   *
   *  @param  scale      The XScale to test
   *  @param  num_points The number of random values to generate
   *
   *  @return the number of points where errors occured.
   */
  public static int Test_GetI_GetI_GLB( XScale scale, int num_points )
  {
    float x;
    int   index;
    int   error_count = 0;
    int   lower_index;
    Random random = new Random();
    float a = scale.getStart_x() - 1;
    float b = scale.getEnd_x() + 1;

    for ( int i = 0; i < num_points; i++ )
    {
      x = random.nextFloat();
      x = a + x * ( b - a );
      index = scale.getI(x);
      if ( x < scale.getStart_x() )
      {
         if ( index != 0 )
         {
            error_count++;
            System.out.println();
            System.out.println("error at start x");
            System.out.println("  x           = " + x );
            System.out.println("  getX(index) = " + scale.getX(index) );
            System.out.println("  getI        = " + scale.getI(x) );
         }
      }
      else if ( x > scale.getEnd_x() )
      {
         if ( index != scale.getNum_x() )
         {
            error_count++;
            System.out.println();
            System.out.println("error at end x");
            System.out.println("  x           = " + x );
            System.out.println("  getX(index) = " + scale.getX(index) );
            System.out.println("  getI        = " + scale.getI(x) );
         }
      }
      else
      {
         lower_index = scale.getI_GLB(x);
         if ( x < scale.getX(lower_index) || x > scale.getX( index ) )
         {
            error_count++;
            System.out.println();
            System.out.println("error at mid x ");
            System.out.println("  x           = " + x );
            System.out.println("  getX(glb)   = " + scale.getX(lower_index) );
            System.out.println("  getX(index) = " + scale.getX(index) );
            System.out.println("  getI_GLB    = " + scale.getI_GLB(x) );
            System.out.println("  getI        = " + scale.getI(x) );
         }
      }
    }
    return error_count;
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
      System.out.println("Warning:XScale IsawSerialVersion != 1");
  }

}

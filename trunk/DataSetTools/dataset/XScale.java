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
 *  $Log$
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
    float MACHINE_EPSILON = 1.1920929E-7f;

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
          if ( Math.abs((uniform_x[i] - x[i]) / x[i]) > MACHINE_EPSILON )
            return new VariableXScale( x );
        }
        else if ( uniform_x[i] != 0 )
          if (Math.abs((uniform_x[i] - x[i]) / uniform_x[i]) > MACHINE_EPSILON)
            return new VariableXScale( x );
    }
                             // if not Variable && not too short, it's Uniform  
    return new UniformXScale( x[0], x[x.length-1], x.length );
  }

  /**
   * Returns the starting "X" value
   */
  public float getStart_x() { return start_x; }

  /**
   * Returns the ending "X" value
   */
  public float getEnd_x()   { return end_x; }

  /**
   * Returns the number of "X" values.
   */
  public int getNum_x()  { return num_x; }

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
   *  Get the position of the specified x-value in this XScale.
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
   */
  abstract public int getI( float x );


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

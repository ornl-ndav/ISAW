/*
 * @(#)XScale.java     1.0  98/06/08  Dennis Mikkelson
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.4  2000/07/31 20:54:11  dennis
 *  Added extra information to debug printout.
 *
 *  Revision 1.3  2000/07/10 22:24:08  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.3  2000/05/12 15:50:13  dennis
 *  removed DOS TEXT  ^M
 *
 *  Revision 1.2  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
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
  float  start_x = 0;
  float  end_x   = 1;
  int    num_x   = 2;

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
  public int   getNum_x()  { return num_x; }

  /**
   * Returns the array of "X" values.  The array will have num_x entries.   
   * For a UniformXScale object, the values will be uniformly spaced.  
   * For a VariableXScale ojbect, the values may be arbitrarily spaced, 
   * as specified when the object was constructed.
   */
  abstract public float[] getXs();

  /**
   * Creates a new instance of one of the concrete objects derived from
   * the XScale object with the same data as the original object.
   */
  abstract public Object clone();


  /**
   *  Print the range and number of X values in this x scale
   */
  public String toString()
  {
    return "XScale [ "+start_x+", "+end_x+": "+num_x+" ]"; 
  }
}

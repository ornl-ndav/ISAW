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
 *
 *  $Log$
 *  Revision 1.7  2001/04/25 19:04:15  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.6  2000/12/07 22:30:33  dennis
 *  Added method extend().
 *
 *  Revision 1.5  2000/11/17 23:39:04  dennis
 *  Minor change to format of output in toString() method.
 *
 *  Revision 1.4  2000/07/31 20:54:11  dennis
 *  Added extra information to debug printout.
 *
 *  Revision 1.3  2000/07/10 22:24:08  dennis
 *  Now Using CVS 
 *
 *  Revision 1.3  2000/05/12 15:50:13  dennis
 *  removed DOS TEXT  ^M
 *
 *  Revision 1.2  2000/05/11 16:00:45  dennis
 *  Added RCS logging
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
   * Creates a new instance of one of the concrete objects derived from
   * the XScale object with the same data as the original object.
   */
  abstract public Object clone();


  /**
   *  Print the range and number of X values in this x scale
   */
  public String toString()
  {
    return "["+start_x+","+end_x+"]" + " in " + num_x + " steps"; 
  }
}

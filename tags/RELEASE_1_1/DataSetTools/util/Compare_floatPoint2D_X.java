/*
 * File:   Compare_floatPoint2D_X.java
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 *  Revision 1.4  2001/04/25 22:24:02  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.3  2000/11/07 15:37:33  dennis
 *  Removed "implements Comparator" for compatibility with JDK 1.1.8 on Mac.
 *
 *  Revision 1.2  2000/10/10 20:19:19  dennis
 *  Log message was missing?  This class implements a comparator function for
 *  sorting points.
 *
 */
package DataSetTools.util;

import java.util.*;

/**
 *  This class implements the Comparator interface for objects of type
 *  floatPoint2D, to allow for sorting based on the X-coordinate.
 *
 */

public class Compare_floatPoint2D_X extends    Object
// #####NOT IN JAVA 1.1                        implements Comparator
{

  /**
   *  Compare two floatPoint2D objects, based on their X-coordianates.
   *
   *  @param  o1   the first floatPoint2D object
   *  @param  o2   the second floatPoint2D object
   *
   *  @returns   Return -1 if o1.x < o2.x, +1 if o1.x > o2.x, and zero
   *             otherwise.
   */
  public int compare( Object o1, Object o2 )
  {
    floatPoint2D  point_1 = (floatPoint2D) o1;
    floatPoint2D  point_2 = (floatPoint2D) o2;

    if ( point_1.x < point_2.x )
      return -1;
    
    if ( point_1.x > point_2.x )
      return 1;
    
    return 0;
  }
  
}

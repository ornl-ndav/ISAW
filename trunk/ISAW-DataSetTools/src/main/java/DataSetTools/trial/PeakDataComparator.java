/*
 * File:  PeakDataComparator.java
 *
 * Copyright (C) 2006, Dennis Mikkelson
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
 * $Log$
 * Revision 1.1  2006/01/16 03:39:48  dennis
 * Comparator class to sort PeakData_d objects based on their
 * run number, h, k and l values, in that order.  This is used
 * to sort the peaks after centroiding.
 *
 */

package DataSetTools.trial;

import java.util.*;

/**
 *  This class compares PeakData_d objects based on their run number,
 *  h, k and l values, to allow sorting the peaks.
 */
public class PeakDataComparator implements Comparator
{

  /**
   * Compare two PeakData_d objects based on run number, h, k and l values
   * in that order.
   *
   * @return -1, 0, or 1 depending on the ordering of the PeakData_d objects
   *          based on first on run number, then on h, k and l values in
   *          that order.  The first of these items that differ will cause
   *          the value -1 or 1 to be returned.  If all four are equal, 0
   *          is returned.
   */
  public int compare( Object o1, Object o2 )
  {
    PeakData_d  peak_1 = (PeakData_d) o1;
    PeakData_d  peak_2 = (PeakData_d) o2;

    if ( peak_1.run_num < peak_2.run_num )
      return -1;

    if ( peak_1.run_num == peak_2.run_num )
    {
      if ( Math.round(peak_1.h) < Math.round(peak_2.h) )
        return -1;

      if ( Math.round(peak_1.h) == Math.round(peak_2.h) ) 
      {
        if ( Math.round(peak_1.k) < Math.round(peak_2.k) )
          return -1;

        if ( Math.round(peak_1.k) == Math.round(peak_2.k) )
        {
          if ( Math.round(peak_1.l) < Math.round(peak_2.l) )
            return -1;

          if ( Math.round(peak_1.l) == Math.round(peak_2.l) )
            return 0;

          if ( Math.round(peak_1.l) > Math.round(peak_2.l) )
            return 1;
        }

        if ( Math.round(peak_1.k) > Math.round(peak_2.k) )
          return 1;
      } 

      if ( Math.round(peak_1.h) > Math.round(peak_2.h) )
        return 1;
    }

    if ( peak_1.run_num > peak_2.run_num )
      return 1;

    return 1;
  }

}

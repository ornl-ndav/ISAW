/* 
 * File: Peak_newComparator.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */
package DataSetTools.operator.Generic.TOF_SCD;

import java.util.*;

/**
 *   A Peak_newComparator compares two Peak_new objects based on their 
 * run number, detector ID and hkl values so that a list of Peak_new 
 * objects can be sorted in the order required by the peaks file.
 */

public class Peak_newComparator implements Comparator
{

  /**
   *  Compare two Peak_new objects based on their run numbers and 
   *  detector IDs. 
   *
   *  @param  peak_1   The first  peak
   *  @param  peak_2   The second peak 
   *
   *  @return A positive integer if peak_1's run number is greater than
   *          peak_2's run number, or the run numbers are equal and
   *          peak_1's grid id is greater than peak_2's grid id.  If
   *          the run numbers and grid ids are the same, then the
   *          h, k and l values are compared.
   */
  public int compare( Object peak_1, Object peak_2 )
  {
    Peak_new p1 = (Peak_new)peak_1;
    Peak_new p2 = (Peak_new)peak_2;

    int run_1 = p1.nrun();
    int run_2 = p2.nrun();

    if ( run_1 != run_2 )
      return run_1 - run_2;

    int id_1 = p1.getGrid().ID();
    int id_2 = p2.getGrid().ID();
    if ( id_1 != id_2 )
      return id_1 - id_2;

    int h1 = Math.round( p1.h() );
    int h2 = Math.round( p2.h() );
    if ( h1 != h2 )
      return h1 - h2;

    int k1 = Math.round( p1.k() );
    int k2 = Math.round( p2.k() );
    if ( k1 != k2 )
      return k1 - k2;

    int l1 = Math.round( p1.l() );
    int l2 = Math.round( p2.l() );
    return l1 - l2;
  }

}


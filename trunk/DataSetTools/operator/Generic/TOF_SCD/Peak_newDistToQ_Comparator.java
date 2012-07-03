/* 
 * File: Peak_newDistToQ_Comparator.java 
 *
 * Copyright (C) 2012, Dennis Mikkelson
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
 *  $Author: $
 *  $Date: $            
 *  $Revision: $
 */
package DataSetTools.operator.Generic.TOF_SCD;

import java.util.*;

/**
 *   A Peak_newDistToQ_Comparator compares two Peak_new objects based on 
 *   their distance to a fixed Q vector, so that a list of peaks can 
 *   be sorted in increasing order of distance to the fixed Q.
 */

public class Peak_newDistToQ_Comparator implements Comparator
{
  float[] fixed_q;

  public Peak_newDistToQ_Comparator( float qx, float qy, float qz )
  {
    fixed_q = new float[3];
    fixed_q[0] = qx;
    fixed_q[1] = qy;
    fixed_q[2] = qz;
  }

  /**
   *  Compare two Peak_new objects based on their distance to the
   *  fixed Q vector passed in to the constructor.
   *
   *  @param  peak_1   The first  peak
   *  @param  peak_2   The second peak 
   *
   *  @return A positive integer if peak_1's distance to the fixed Q is 
   *          greater than peak_2's.
   */
   public int compare( Object peak_1, Object peak_2 )
   {
     float distance_1 = distance( (Peak_new)peak_1 );
     float distance_2 = distance( (Peak_new)peak_2 );
     if ( distance_1 < distance_2 )
       return -1;
     else if  ( distance_1 == distance_2 )
       return 0;

     return 1;
   }

   /**
    *  Calculate the squared distance in Q, from the specified peak 
    *  to the fixed Q-vector
    */
    private float distance( Peak_new peak )
    {
      float[] q     = peak.getQ();
      float   delta = 0;
      float   sum   = 0;
      for ( int i = 0; i < 3; i++ )
      {
        delta = (q[i] - fixed_q[i]);
        sum += delta * delta ;
      }
      return sum;
   }
}


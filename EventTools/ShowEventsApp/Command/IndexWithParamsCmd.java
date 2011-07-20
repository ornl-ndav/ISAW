/* 
 * File: IndexWithParamsCmd.java
 *
 * Copyright (C) 2011, Dennis Mikkelson
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
 *  $Author: eu7 $
 *  $Date: 2010-04-19 15:33:36 -0500 (Mon, 19 Apr 2010) $            
 *  $Revision: 20663 $
 */

package EventTools.ShowEventsApp.Command;

public class IndexWithParamsCmd extends IndexPeaksLatticeParams
{
   private int        num_initial;
   private float      angle_step;   
   
   public IndexWithParamsCmd( float a,     float b,    float c,
                              float alpha, float beta, float gamma, 
                              float        angle_step, 
                              int          num_initial,
                              float        tolerance
                              )
   {
      super( a, b, c, alpha, beta, gamma, tolerance );
      this.angle_step  = angle_step;
      this.num_initial = num_initial;
   }


   public float getAngle_step()
   {
      return angle_step;
   }


   public int getNum_initial()
   {
      return num_initial;
   }


   public String toString()
   {
      return super.toString() + 
             "\nNum_initial : " + getNum_initial() +
             "\nAngle step  : " + getAngle_step();
   }
}

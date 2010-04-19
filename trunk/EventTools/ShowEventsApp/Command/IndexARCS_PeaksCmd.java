/* 
 * File: IndexARCS_PeaksCmd.java
 *
 * Copyright (C) 2010, Dennis Mikkelson
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

package EventTools.ShowEventsApp.Command;

import gov.anl.ipns.MathTools.Geometry.Vector3D_d;

public class IndexARCS_PeaksCmd extends IndexPeaksLatticeParams
{
   private float      psi;
   private Vector3D_d u_hkl;
   private Vector3D_d v_hkl;
   private int        initial_num;
   
   
   public IndexARCS_PeaksCmd( float a,     float b,    float c,
                              float alpha, float beta, float gamma, 
                              float        psi,
                              Vector3D_d   u_hkl,
                              Vector3D_d   v_hkl,
                              float        tolerance,
                              int          initial_num,
                              float        req_fraction
                              )
   {
      super( a, b, c, alpha, beta, gamma, tolerance, req_fraction );
      this.psi         = psi;
      this.u_hkl       = new Vector3D_d( u_hkl );
      this.v_hkl       = new Vector3D_d( v_hkl );
      this.initial_num = initial_num;
   }


   public float getPSI()
   {
      return psi;
   }


   public Vector3D_d getU_hkl()
   {
      return new Vector3D_d( u_hkl );
   }


   public Vector3D_d getV_hkl()
   {
      return new Vector3D_d( v_hkl );
   }


   public int getInitialNum()
   {
      return initial_num;
   }

   
   public String toString()
   {
      return super.toString() + 
             "\nInitial_num :" + getInitialNum() +
             "\nPSI angle : "  + getPSI() +
             "\nU hkl     : "  + getU_hkl() +
             "\nV hkl     : "  + getV_hkl();
   }
}

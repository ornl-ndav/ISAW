/* 
 * File: SelectPointCmd.java
 *
 * Copyright (C) 2009, Dennis Mikkelson
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
 *  $Rev$
 */
package EventTools.ShowEventsApp.Command;

import gov.anl.ipns.MathTools.Geometry.Vector3D;

public class SelectPointCmd
{
   private float qx;
   private float qy;
   private float qz;
   private float dx;
   private float dy;
   private float dz;
   
   public SelectPointCmd(float qx, float qy, float qz,
                         float dx, float dy, float dz)
   {
      this.qx = qz;
      this.qy = qy;
      this.qz = qz;
      this.dx = dx;
      this.dy = dy;
      this.dz = dz;
   }

   public SelectPointCmd(Vector3D Q, Vector3D D)
   {
      this.qx = Q.getX();
      this.qy = Q.getY();
      this.qz = Q.getZ();
      this.dx = D.getX();
      this.dy = D.getY();
      this.dz = D.getZ();
   }


   public Vector3D getQ_vec()
   {
     return new Vector3D( qx, qy, qz );
   }
   
   public float getQx()
   {
      return qx;
   }

   public float getQy()
   {
      return qy;
   }

   public float getQz()
   {
      return qz;
   }

   public float getDx()
   {
      return dx;
   }

   public float getDy()
   {
      return dy;
   }

   public float getDz()
   {
      return dz;
   }
   
   public String toString()
   {
      String format = "Q(%f,%f,%f)\nD(%f,%f,%f)";
      return String.
         format(format, getQx(), getQy(), getQz(), getDz(), getDy(), getDz());
   }
}

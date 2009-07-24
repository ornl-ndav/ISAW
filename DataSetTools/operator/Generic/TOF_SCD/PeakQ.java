/*
 * File: PeakQ.java 
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
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 * 
 */
package DataSetTools.operator.Generic.TOF_SCD;

/**
 *
 */
public class PeakQ implements IPeakQ
{
  private float qx, qy, qz;
  private float h, k, l;
  private int   ipkobs;


  public PeakQ( float qx, float qy, float qz, int ipkobs )
  {
    this.qx     = qx;
    this.qy     = qy;
    this.qz     = qz;
    this.ipkobs = ipkobs;
  }   

  @Override
  public float[]  getUnrotQ()
  {
    float[] q_vals = { qx, qy, qz };
    return q_vals;
  }


  @Override
  public int  ipkobs( int pkObs )
  {
    ipkobs = pkObs;
    return ipkobs;
  }


  @Override
  public int  ipkobs()
  {
    return ipkobs;
  }


  @Override
  public void sethkl(float h, float k, float l) throws IllegalArgumentException
  {
    this.h = h;
    this.k = k;
    this.l = l;
  }


  @Override
  public float h()
  {
    return h;
  }


  @Override
  public float k()
  {
    return k;
  }


  @Override
  public float l()
  {
    return l;
  }

 
  public String toString()
  {
    return String.format( "%6.2f  %6.2f  %6.2f    " +
                          "%7.2f  %7.2f  %7.2f    %8d",
                          h, k, l, qx, qy, qz, ipkobs );
  }

}

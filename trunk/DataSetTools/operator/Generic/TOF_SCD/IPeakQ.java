/*
 * File:  IPeakQ.java 
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
public interface IPeakQ
{

   /**
    * Returns the  "Q" vector (really Q/2PI) associated with this peak. 
    *
    * @return the Q/2PI vector in crystal coordinates associated with this 
    *         peak
    */
   float[]  getUnrotQ();


   /**
    * Sets the intensity associated with the peak position. 
    *       
    * @param pkObs the intensity of this peak.
    */
   int  ipkobs( int pkObs );


   /**
    *  Returns the intensity associated with the peak position.
    *       
    * @return the intensity of this peak.
    */
   int  ipkobs();


   /**
    * Set the h,k and l values
    * 
    * @param  h   First  Miller index
    * @param  k   Second Miller index
    * @param  l   Third  Miller index
    */
   void sethkl(float h, float k, float l) throws IllegalArgumentException;


   /**
    * Return the first Miller index (h) corresponding to this peak
    *
    * @return the h value corresponding to this peak or 
    *         zero if cannot be determined
    */
   float h();


   /**
    * Return the second Miller index (k) corresponding to this peak
    *
    * @return the k value corresponding to this peak or 
    *         zero if cannot be determined
    */
   float k();


   /**
    * Return the third Miller index (l) corresponding to this peak
    *
    * @return the l value corresponding to this peak or 
    *         zero if cannot be determined
    */
   float l();

}

/* 
 * File: ConfigLoadCmd.java
 *
 * Copyright (C) 2010, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author:$
 *  $Date:$            
 *  $Rev:$
 */
package EventTools.ShowEventsApp.Command;


public class ConfigLoadCmd
{
   int runNumber;
   float phi;
   float chi;
   float omega;
   int nbins;
   public ConfigLoadCmd(  int runNumber,
                          float phi,
                          float chi,
                          float omega,
                          int nbins)
   {
      this.runNumber = runNumber ;
      this.phi = phi ;
      this.chi = chi ;
      this.omega = omega ;
      this.nbins = nbins ;
   }
   
   public int getRunNumber()
   {
      return runNumber;
   }   
   
   public int getNbins()
   {
      return nbins;
   }
   
   
   public float getPhi()
   {
      return phi;
   } 
   public float getChi()
   {
      return chi;
   } 
   public float getOmega()
   {
      return omega;
   }
   
   public void setRunNumber( int runNumber)
   {
      this.runNumber = runNumber;
   }   
   
   public void setNbins(int nbins)
   {
      this.nbins = nbins;
   }
   
   
   public void setPhi( float phi)
   {
      this.phi = phi;
   } 
   public void setChi( float chi)
   {
      this.chi = chi;
   } 
   public void setOmega( float omega)
   {
      this.omega = omega;
   }
   
   public String toString()
   {
      return "\nRun Number : " + getRunNumber() +
             "\nPhi        : " + getPhi() + 
             "\nChi        : " + getChi() + 
             "\nOmega      : " + getOmega() + 
             "\n# bins     : " + getNbins() ;
   }
   
   
}


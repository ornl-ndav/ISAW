/* 
 * File: IntegratePeaksCmd.java
 *
 * Copyright (C) 2010 Dennis Mikkelson
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

import java.util.Vector;
import DataSetTools.operator.Generic.TOF_SCD.IPeakQ;

/**
 * This class holds information about peaks that are to be integrated using
 * the sphereical histogram region method.
 */
public class IntegratePeaksCmd
{
   private Vector   peaks;
   private Vector   i_isigi_vec = null;
   private float    sphere_radius;
   private boolean  current_peaks_only;
   private boolean  record_as_peaks_list;
   
   /**
    * Make a command object with the specified control values. 
    *
    * @param peaks                Vector of peaks to display, or null if this
    *                             command is just setting parameters and 
    *                             requesting that some class set up the 
    *                             list of peaks to process.
    * @param sphere_radius        Radius of the sphere to integrate, 
    *                             specified in inverse Angstroms.
    * @param current_peaks_only   If true, only peaks in the current list of
    *                             peaks should be integrated. 
    * @param record_as_peaks_list If true, the integrated peaks should be 
    *                             recorded by the PeakListHandler as the
    *                             current list of peaks.
    */
   public IntegratePeaksCmd( Vector  peaks, 
                             float   sphere_radius,
                             boolean current_peaks_only, 
                             boolean record_as_peaks_list )
   {
     this.peaks                = peaks;
     this.sphere_radius        = sphere_radius;
     this.current_peaks_only   = current_peaks_only;
     this.record_as_peaks_list = record_as_peaks_list; 
   }


   /**
    *  Get the Vector of peaks.
    *
    *  @return This will return the vector of peaks, or null if no peaks
    *          were specified.
    */
   public Vector getPeaks()
   {
      return peaks;
   }

 
   /**
    *  Get the requested sphere radius.
    *
    *  @return the sphere radius.
    */
   public float getSphere_radius()
   {
     return sphere_radius;
   }


   /**
    *  Get the current_peaks_only flag 
    *
    *  @return true if the current_peaks_only flag is true.
    */
   public boolean getCurrent_peaks_only()
   {
      return current_peaks_only;
   }


   /**
    *  Get the record_as_peaks_list flag 
    *
    *  @return true if the record_as_peaks_list flag is true.
    */
   public boolean getRecord_as_peaks_list()
   {
      return record_as_peaks_list;
   }


   /**
    * Get the I, IsigI values stored in a vector of two element float 
    * arrays.  The I value is in position 0 and the I/sigI value is 
    * in position 1 of each array stored in the vector.
    *
    * @return a Vector with entries that are arrays of floats with two
    *         entries, the I and I/sigI value for each peak.
    */
   public Vector getI_and_IsigI()
   {
     return i_isigi_vec;
   }

   /**
    * Set the vector of I and I/sigI arrays.
    *
    * @param i_isigi_vec  A Vector containing arrays with two floats.  There
    *                     must be one array in the vector for each peak and
    *                     each array must contain I and I/sigI for the 
    *                     corresponding peak, in positions 0 and 1.
    */
   public void setI_and_IsigI( Vector i_isigi_vec )
   {
     this.i_isigi_vec = i_isigi_vec;
   }


   /**
    * Get a string giving the number of peaks and regions in this command
    * object.
    */
   public String toString()
   {
      String result;
      if ( peaks == null )
           result = "\n NO PEAKS SPECIFIED ";
      else
           result = "\nNum Peaks             : " + getPeaks().size(); 

      if ( i_isigi_vec == null )
           result += "\n NO I, I/sigI specified ";
      else
           result += "\nNum I, I/sigI        : " + getI_and_IsigI();
 
      result += "\nSphere Radius             : " + getSphere_radius() +
                "\nCurrent Peaks Only Flag   : " + getCurrent_peaks_only() +
                "\nRecord as Peaks List Flag : " + getRecord_as_peaks_list();

      return result;
   }

}

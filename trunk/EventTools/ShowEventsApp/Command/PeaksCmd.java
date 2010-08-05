/* 
 * File: PeaksCmd.java
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
 * This class holds information about peaks and peak images to be recorded
 * and displayed.  It is used when setting lists of PeakQ, lists of Peak_new
 * and when requesting the display of images of peaks.  
 * CAUTION: Depending on the context in which it is used, the peaks vector
 *          might be null, or contain either PeakQ or Peak_new objects.
 */
public class PeaksCmd
{
   private Vector   peaks;
   private boolean  show_images;
   private float    image_size;
   private int      max_offset;
   
   /**
    * Make a command object with the specified control values. 
    *
    * @param peaks       Vector of peaks to display, or null if this
    *                    command is just setting parameters for the iamges
    *                    or turning the image display on/off.
    * @param show_images Flag indicating wheter to show or remove the
    *                    image display panel(s).
    * @param image_size  Size of the displayed image in reciprocal
    *                    Angstroms.
    * @param max_offset  Maximum number of slices before and after the
    *                    the image slice through the peak center.
    */
   public PeaksCmd( Vector  peaks, 
                    boolean show_images, 
                    float   image_size, 
                    int     max_offset )
   {
     this.peaks       = peaks;
     this.show_images = show_images;
     this.image_size  = image_size;
     this.max_offset  = max_offset;
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
    *  Get the show_images flag.
    *
    *  @return true if the show images flag is true.
    */
   public boolean getShowImages()
   {
      return show_images;
   }


   /**
    *  Get the requested image size in reciprocal Angstroms.
    *
    *  @return The requested image size.
    */
   public float getImageSize()
   {
      return image_size;
   }


   /**
    *  Get the requested maximum slice offset.
    *
    *  @return The requested maximum slice offset.
    */
   public int getMaxOffset()
   {
      return max_offset;
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
           result = "\nNum Peaks    : " + getPeaks().size(); 
 
      result += "\nShow Images : " + getShowImages() +
                "\nImage Size  : " + getImageSize() +
                "\nMax Offset  : " + getMaxOffset() ;

      return result;
   }

}

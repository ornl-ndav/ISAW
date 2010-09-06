/* 
 * File: FindPeaksCmd.java
 *
 * Copyright (C) 2009,2010 Dennis Mikkelson
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

/**
 *  This command object holds the data from the findPeaksPanel
 *  that is sent to the HistogramHandler to control the 
 *  find peaks operation.
 */
public class FindPeaksCmd
{
   private int     max_num_peaks;
   private float   min_intensity;
   private boolean smooth_data;
   private boolean mark_peaks;
   private float   mark_size;
   private boolean show_images;
   private float   image_size;
   private int     max_offset;
   private String  logFileName;
   
   /**
    *  Construct a find peaks command object with the specified 
    *  parameters.
    *  @param  max_num_peaks     The maximum number of peaks to check
    *  @param  min_intensity     The minimu peak intensity to consider
    *  @param  smooth_data       Boolean flag indicating whether or
    *                            not to smooth the data.
    *  @param  mark_peaks        Boolean flag indicating whether or not
    *                            to mark the peaks that were found
    *  @param  mark_size         The size of the mark to use if peaks
    *                            are marked
    *  @param  show_images       Booleam flag indicating whether or not
    *                            to pop up image view of peaks
    *  @param  image_size        The size of image to be displayed if
    *                            peak images are shown
    *  @param  max_offset        The maximum number of slices to keep
    *                            above or below the image slice with
    *                            the peak center
    *  @param  logFileName       The name of the log file to write
    *                            if any.
    */
   public FindPeaksCmd( int     max_num_peaks,
                        float   min_intensity, 
                        boolean smooth_data,
                        boolean mark_peaks,
                        float   mark_size,
                        boolean show_images,
                        float   image_size,
                        int     max_offset, 
                        String  logFileName )
   {
      this.max_num_peaks = max_num_peaks;
      this.min_intensity = min_intensity;
      this.smooth_data   = smooth_data;
      this.mark_peaks    = mark_peaks;
      this.mark_size     = mark_size;
      this.show_images   = show_images;
      this.image_size    = image_size;
      this.max_offset    = max_offset;
      this.logFileName   = logFileName;
   }
   
   public int getMaxNumberPeaks()
   {
      return max_num_peaks;
   }

   public float getMinPeakIntensity()
   {
      return min_intensity;
   }

   public boolean getSmoothData()
   {
      return smooth_data;
   }
   
   public boolean getMarkPeaks()
   {
      return mark_peaks;
   }

   public float getMarkSize()
   {
      return mark_size;
   }

   public boolean getShowImages()
   {
      return show_images;
   }

   public float getImageSize()
   {
      return image_size;
   }

   public int getMaxOffset()
   {
      return max_offset;
   }
   
   public String getLogFileName()
   {
      return logFileName.trim();
   }
   
   public String toString()
   {
      return 
             "\nMax # Peaks  : " + getMaxNumberPeaks()   +
             "\nMin Peak Int.: " + getMinPeakIntensity() +
             "\nSmooth Data  : " + getSmoothData()       +
             "\nMark Peaks   : " + getMarkPeaks()        +
             "\nMark Size    : " + getMarkSize()         +
             "\nShow Images  : " + getShowImages()       +
             "\nImage Size   : " + getImageSize()        +
             "\nMax Offset   : " + getMaxOffset()        +
             "\nLog File     : " + getLogFileName()      + "\n";
   }
}

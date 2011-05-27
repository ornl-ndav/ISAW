/* 
 * File: MarkPeaksCmd.java
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
 *  $Author: eu7 $
 *  $Date: 2010-08-19 07:48:49 -0500 (Thu, 19 Aug 2010) $            
 *  $Revision: 20936 $
 */

package EventTools.ShowEventsApp.Command;

import java.util.Vector;
import DataSetTools.operator.Generic.TOF_SCD.IPeakQ;
import SSG_Tools.SSG_Nodes.SimpleShapes.Polymarker;
import java.awt.Color;

/**
 * This class holds information about a list of peaks to be marked and
 * and displayed.  It is used when marking peak positions.
 */
public class MarkPeaksCmd
{
   private Vector   peaks;
   private boolean  show_marks;
   private float    size;
   private int      type;
   private Color    color;
   
   /**
    * Make a command object with the specified control values. 
    *
    * @param peaks       Vector of peaks to mark, or null if this
    *                    command is just setting parameters for the 
    *                    marks or turning the marks on/off.
    * @param show_marks  Flag indicating whether to show or remove the
    *                    peak marker from the display.
    * @param size        Size of the displayed mark in reciprocal
    *                    Angstroms.  If mark_size <= 0, a size of 0.1
    *                    will be used.
    *                    NOTE: For a DOT marker, the size should be an 
    *                    integer, listing the number of pixels high and
    *                    wide the dot should be made.
    * @param type        Integer code for the marker type.  This must be 
    *                    one of the following values from the Polymarker
    *                    class:
    *                           Polymarker.DOT   = 1;
    *                           Polymarker.PLUS  = 2;
    *                           Polymarker.STAR  = 3;
    *                           Polymarker.BOX   = 4;
    *                           Polymarker.CROSS = 5;
    *                    If the type code is not one of the listed values,
    *                    a BOX will be used.
    * @param color       The color to use for the marker.  If null, WHITE
    *                    will be used.
    */
   public MarkPeaksCmd( Vector  peaks, 
                        boolean show_marks, 
                        float   size, 
                        int     type,
                        Color   color )
   {
     this.peaks       = peaks;
     this.show_marks  = show_marks;

     if ( size > 0 )
       this.size = size;
     else
       this.size = 0.1f;

     if ( type >= 1 && type <= 5 )
       this.type = type;
     else
       this.type = Polymarker.BOX;
     
     if ( color != null )
       this.color = color;
     else
       this.color = Color.WHITE;
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
    *  Get the show_marks flag.
    *
    *  @return true if the show marks flag is true.
    */
   public boolean getShowMarks()
   {
      return show_marks;
   }


   /**
    *  Get the requested marker size in reciprocal Angstroms.
    *
    *  @return The requested marker size.
    */
   public float getSize()
   {
      return size;
   }


   /**
    *  Get the requested marker type.
    *
    *  @return The requested marker type.
    */
   public int getType()
   {
      return type;
   }


   /**
    *  Get the requested marker color.
    *
    *  @return The requested marker color.
    */
   public Color getColor()
   {
      return color;
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
 
      result += "\nShow Marks  : " + getShowMarks() +
                "\nMarker Size : " + getSize() +
                "\nMarker Type : " + getType() ;

      return result;
   }

}

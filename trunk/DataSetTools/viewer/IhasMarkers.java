/* 
 * File: IhasMarkers.java
 *
 * Copyright (C) 2009, Ruth Mikkelson
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
 *  $Author$:
 *  $Date$:            
 *  $Rev$:
 */
package DataSetTools.viewer;

import gov.anl.ipns.ViewTools.Components.Transparency.Marker;



/**
 * Implementers of this interface calculate the marks that appear on 
 * a 2D display. The position of the marks are at floatPoint2D (x-col,
 * y-row) where y=1 is at the top of the display
 * 
 * @author Ruth
 *
 */
public interface IhasMarkers
{
   /**
    * Returns the Markers that are to be displayed in a 2D view
    * @return   The Marks to be displayed. The position of the marks
    *       are at floatPoint2D (x-col,y-row) where y=1 is at the top
    *       of the display
    */
  public Marker getMarkers();
}

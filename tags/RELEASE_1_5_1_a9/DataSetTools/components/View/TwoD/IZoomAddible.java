/*
 * File: IZoomAddible.java
 *
 * Copyright (C) 2003, Mike Miller
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
 * Primary   Mike Miller <millermi@uwstout.edu>
 * Contact:  Student Developer, University of Wisconsin-Stout
 *           
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2003/07/25 14:35:11  dennis
 *  - Initial version - Removes methods getLocalCoordBounds() and
 *  getGlobalCoordBounds() from AxisAddible2D interface. Any view
 *  component using the annotation or selection overlay will need
 *  to implement this interface. (Mike Miller)
 *
 */
 
package DataSetTools.components.View.TwoD;

import DataSetTools.components.image.CoordBounds;

/**
 * This interface is implemented by view components that utilize overlays 
 * which respond to zooming. Examples include Annotation and Selection Overlay,
 * but exclude the Axis Overlay (uses IAxisAddible2D).
 */
public interface IZoomAddible extends IOverlayAddible
{   
  /**
   * This method will return the local coordinate bounds of the center
   * jpanel. To be implemented, the center may have to be a coordjpanel.
   */
   public CoordBounds getLocalCoordBounds();
      
  /**
   * This method will return the global coordinate bounds of the center
   * jpanel. To be implemented, the center may have to be a coordjpanel.
   */
   public CoordBounds getGlobalCoordBounds();
}




   

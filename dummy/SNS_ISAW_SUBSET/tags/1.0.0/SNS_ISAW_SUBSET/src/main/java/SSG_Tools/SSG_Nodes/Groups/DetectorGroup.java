/*
 * File:  DetectorGroup.java
 *
 * Copyright (C) 2005, Chad Jones
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
 * Primary   Chad Jones <cjones@cs.utk.edu>
 * Contact:  Student Developer, University of Tennessee
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * Modified:
 *
 *  $Log: DetectorGroup.java,v $
 *  Revision 1.2  2006/07/19 18:07:16  dennis
 *  Removed unused imports.
 *
 *  Revision 1.1  2005/07/19 16:01:28  cjones
 *  Added group and shape for Detectors and Pixels that have user IDs.
 *
 */

package SSG_Tools.SSG_Nodes.Groups;

import SSG_Tools.SSG_Nodes.Group;

/**
 * Detector that holds groups of pixels.  Detector can be assigned a user
 * defined integer value.
 */
public class DetectorGroup extends Group
{
  private int DetectorID; 

  /* -------------------------- constructor ------------------------ */
  /**
   *  Construct a new empty detector group node.
   *
   *    @param id User specified integer to associated with detector.
   */
  public DetectorGroup(int id)
  {
    super();
    DetectorID = id;
  }

  /**
   * Return detector's id.
   *
   *    @return Detector id
   */
  public int getDetectorID()
  {
    return DetectorID;
  }
}

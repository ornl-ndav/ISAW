/*
 * File:  ReferenceGridComparator.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * 
 *  $Log$
 *  Revision 1.1  2005/07/11 20:40:06  dennis
 *  Comparator to allow sorting a list of ReferenceGrids, based
 *  on their grid IDs.
 *
 */

package  DataSetTools.dataset;

import java.util.*;

/**
 *   A ReferenceGridComparator compares two ReferenceGrids based on their 
 * grid IDs, so that a list of ReferenceGrids is easily sorted into 
 * increasing order.
 */  

public class ReferenceGridComparator implements Comparator
{

  /**
   *  Compare two grids based on their grid ID's
   *
   *  @param  grid_1   The first grid
   *  @param  grid_2   The second grid
   *
   *  @return the difference, grid_1.ID - grid_2.ID
   */
  public int compare( Object grid_1, Object grid_2 )
  {
    int id_1 = ((ReferenceGrid)grid_1).ID();
    int id_2 = ((ReferenceGrid)grid_2).ID();
    return id_1 - id_2;
  }

}

/*
 * File:  DetectorInfoComparator.java
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2002/11/12 15:06:21  dennis
 *  Comparator for sorting the DetInfoListAttribute
 *
 */

package  DataSetTools.dataset;

import  java.io.*;
import  java.util.*;
import  DataSetTools.instruments.*;

/**
 * Comparator class for sorting list of DetectorInfo objects.
 */

public class DetectorInfoComparator implements Serializable, 
                                               Comparator 
{
  public int compare( Object o1, Object o2 )
  {
    int seg_1 = ((DetectorInfo)o1).getSeg_num();
    int seg_2 = ((DetectorInfo)o2).getSeg_num();

    if ( seg_1 < seg_2 )
      return -1;
    else if ( seg_1 == seg_2 )
      return 0;
    else
      return 1;
  }
}

/*
 * File:  PixelInfoComparator.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2003/02/07 19:01:52  dennis
 *  Initial Version
 *
 */

package  DataSetTools.dataset;

import  java.io.*;
import  java.util.*;
import  DataSetTools.instruments.*;

/**
 * Comparator class for sorting list of IPixelInfo objects.
 */

public class PixelInfoComparator implements Serializable, 
                                            Comparator 
{
  public int compare( Object o1, Object o2 )
  {
    int id_1 = ((IPixelInfo)o1).ID();
    int id_2 = ((IPixelInfo)o2).ID();

    if ( id_1 < id_2 )
      return -1;
    else if ( id_1 == id_2 )
      return 0;
    else
      return 1;
  }
}

/*
 * File:  DataComparator.java
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
 *  Revision 1.1  2002/07/15 20:25:07  dennis
 *  Class for comparing Data blocks for sorting.
 *
 */

package  DataSetTools.dataset;

import  java.io.*;
import  java.util.*;

/**
 * Comparator class using attributes to use with Sort method from
 * java.util.Array.
 */


public class DataComparator implements Serializable, 
                                       Comparator 
{
  String attr_name;

  public DataComparator( String attr_name )
  {
    this.attr_name = attr_name;
  }

  public int compare( Object o1, Object o2 )
  {
    Attribute attr_1 = ((IAttributeList)o1).getAttribute( attr_name );
    Attribute attr_2 = ((IAttributeList)o2).getAttribute( attr_name );
    return attr_1.compare( attr_2 );
  }
}

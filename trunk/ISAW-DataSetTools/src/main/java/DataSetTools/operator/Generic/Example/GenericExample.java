/*
 * File:  GenericExample.java 
 *
 * Copyright (C) 2004, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.3  2005/08/24 19:47:39  dennis
 *  Changed logical name of menu from UTILS_EXAMPLES to GENERAL_EXAMPLES.
 *
 *  Revision 1.2  2005/01/10 15:21:18  dennis
 *  Modified getCategoryList to put derived classes in proper place in
 *  menu system.
 *
 *  Revision 1.1  2004/05/07 16:11:57  dennis
 *  Base class for examples of Generic operators.
 *
 */

package DataSetTools.operator.Generic.Example;

import java.io.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.GenericOperator;

/**
 *   Base class for generic operators that are simple examples of
 *   Generic operators. 
 *
 * @see GenericOperator
 */

abstract public class GenericExample extends    GenericOperator 
                                     implements Serializable
{
  private static String[] categoryList=null;

   protected GenericExample( String title )
   {
      super( title );
   } 

  /* ------------------------ getCategoryList ------------------------------ */
  /**
   * Get a list of strings giving the categories to be used when placing
   * the operator in menus.  The first entry in the array must be the string:
   *
   *      Operator.OPERATOR
   *
   * @return  An array of Strings specifying the category names to use 
   *          for this operator. 
   */
  public String[] getCategoryList()
  {
    if(categoryList==null)
      categoryList = Operator.GENERAL_EXAMPLES;

    return categoryList;
  }

} 

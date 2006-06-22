/*
 * File:  GenericTOF_SAD.java 
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
 * $Log$
 * Revision 1.3  2005/01/10 15:14:07  dennis
 * Updated java docs for getCategoryList method.
 *
 * Revision 1.2  2005/01/07 17:52:52  dennis
 * Now sets category list based on instrument type String from Operator
 * base class.
 *
 * Revision 1.1  2003/07/05 22:10:25  dennis
 * Base class for ISAW operators that process SAD data.
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;

import java.io.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.GenericOperator;

/**
 *   Base class for generic operators that work with time-of-flight
 * small angle diffractometers.
 *
 * @see GenericOperator
 *
 */

abstract public class GenericTOF_SAD extends GenericOperator 
                                             implements Serializable
{
   private static String[] categoryList=null;

   protected GenericTOF_SAD( String title )
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
      categoryList = Operator.TOF_NSAS;

    return categoryList;
  }

}

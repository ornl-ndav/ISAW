/*
 * File:  GenericTOF_GLAD.java 
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.1  2005/01/07 18:07:47  dennis
 * Base class for operators for time-of-flight glass, liquid and
 * amorphous materials diffractometers.
 *
 */
package DataSetTools.operator.Generic.TOF_GLAD;

import java.io.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.GenericOperator;

/**
 *   Base class for generic operators that work with time-of-flight
 * glass, liquid and amorphous material diffractometers.
 *
 * @see GenericOperator
 *
 */

abstract public class GenericTOF_GLAD extends GenericOperator 
                                              implements Serializable
{
   private static String[] categoryList=null;

   protected GenericTOF_GLAD( String title )
   {
      super( title );
   } 

  /* ------------------------ getCategoryList ------------------------------ */
  /**
   * Get an array of strings listing the operator category names of base
   * classes for this operator.  The first entry in the array is the string:
   *
   *      Operator.OPERATOR
   *
   * The last entry is the category of the last abstract base class that is
   * is a base class for the current operator.
   *
   * @return  A list of Strings specifying the category names of the abstract
   * base classes from which this operator is derived.
   */
  public String[] getCategoryList()
  {
    if(categoryList==null)
      categoryList = Operator.TOF_NGLAD;

    return categoryList;
  }

}

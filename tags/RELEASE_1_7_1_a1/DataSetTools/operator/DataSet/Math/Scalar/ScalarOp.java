/*
 * File:  ScalarOp.java 
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 *  Revision 1.3  2003/06/16 19:04:39  pfpeterson
 *  Removed old code and updated to work with new getCategoryList() code
 *  in base operator class.
 *
 *  Revision 1.2  2002/11/27 23:19:03  pfpeterson
 *  standardized header
 *
 *  Revision 1.1  2002/02/22 21:03:19  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Math.Scalar;

import java.io.*;
import DataSetTools.operator.DataSet.Math.DS_Math;

/**
 * Base class for DataSetOperators that operate on a DataSet using
 * scalar operations such as add, subtract, multiply and divide by
 * a scalar.
 *
 * @see DS_Math 
 * @see DataSetScalarAdd 
 *
 */

abstract public class ScalarOp extends    DS_Math 
                               implements Serializable
{
   private static String[] categoryList=null;
   protected ScalarOp( String title )
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
      categoryList=createCategoryList();

    return categoryList;
  }
} 

/*
 * File:  XAxisInformationOp.java
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.7  2004/03/19 17:20:49  dennis
 * Removed unused variable(s)
 *
 * Revision 1.6  2004/03/15 06:10:48  dennis
 * Removed unused import statements.
 *
 * Revision 1.5  2003/06/16 19:03:28  pfpeterson
 * Removed old code and updated to work with new getCategoryList() code
 * in base operator class.
 *
 * Revision 1.4  2002/11/27 23:18:10  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/07/31 16:10:28  dennis
 * Implements IDataPointInfo to display data in a viewer's
 * DataSetXConversionsTable.
 *
 * Revision 1.2  2002/05/29 22:44:17  dennis
 * Added x and i parameters to the XInfo_label() method to allow changing the
 * label when the cursor is moved.
 *
 * Revision 1.1  2002/04/08 15:39:04  dennis
 * Base class for operators that provide information about the
 * X Axis
 *
 */

package DataSetTools.operator.DataSet.Information.XAxis;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.operator.DataSet.Information.DS_Information;

/**
  * This abstract class is the base class for DataSetOperators that provide 
  * information about the X axis.
  *
  *  @see DS_Information
  */

abstract public class XAxisInformationOp extends    DS_Information
                                         implements IDataPointInfo,
                                                    Serializable
{
  private static String[] categoryList=null;
  public XAxisInformationOp( String title )
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

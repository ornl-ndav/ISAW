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
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
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
  public XAxisInformationOp( String title )
  {
    super( title );
    Parameter parameter;
  }

  /* -------------------------- getCategory -------------------------------- */
  /**
   * Get the category of this DataSet operator
   *
   * @return  A String specifying the category of this operator.
   */
  public String getCategory()
  {
    return X_AXIS_INFORMATION;
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
    String partial_list[] = super.getCategoryList();  // get list of ancestor
                                                      // categories and put
                                                      // them in a new larger
                                                      // list.
    return AppendCategory( X_AXIS_INFORMATION, partial_list );
  }
}

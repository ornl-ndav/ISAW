/*
 * File:AnalyzeOp.java 
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
 *  Revision 1.3  2001/04/26 19:04:23  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.2  2001/03/01 20:55:05  dennis
 *  Fixed naming errors in @see comments.
 *
 *  Revision 1.1  2000/11/10 23:01:10  dennis
 *  New abstract base class for operators.  Allows automatic
 *  generation of hierarchical menus using operator categories.
 *
 */

package DataSetTools.operator;

import java.io.*;

/**
 * Base class for DataSetOperators that operate on a DataSet using
 * numerical analyis operations such as IntegrateGroup 
 *
 * @see DS_Math 
 * @see IntegrateGroup 
 *
 */

abstract public class AnalyzeOp extends    DS_Math 
                                implements Serializable
{
   protected AnalyzeOp( String title )
   {
      super( title );
   } 

  /* -------------------------- getCategory -------------------------------- */
  /**
   * Get the category of this operator
   *
   * @return  A String specifying the category of this operator.  This is
   *          actually the category of the abstract base class from which
   *          the current operator is directly derived.
   */
  public String getCategory()
  {
    return ANALYZE;
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
    return AppendCategory( ANALYZE, partial_list );
  }

} 

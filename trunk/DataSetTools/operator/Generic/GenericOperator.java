/*
 * File:  GenericOperator.java
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
 *  Revision 1.4  2003/06/12 18:48:05  pfpeterson
 *  Updated javadocs to reflect a idiosycracy of Script_Class_List_Handler.
 *
 *  Revision 1.3  2002/11/27 23:20:43  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/03/05 19:27:27  pfpeterson
 *  Updated @see references in javadocs.
 *
 *  Revision 1.1  2002/02/22 20:56:53  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.Generic;

import java.io.*;
import DataSetTools.operator.Operator;

/**
 *   Base class for operators that are independent of any DataSet.
 * This class extends the Operator class by overriding the getCategory and
 * getCategoryList methods.  All "Generic" operators should be ultimately
 * derived from this class.
 *
 * @see Operator
 * @see DataSetTools.operator.Generic.Load.GenericLoad
 * @see DataSetTools.operator.Generic.Batch.GenericBatch
 *
 * <B>NOTE:</B> No class should directly extend Operator. Instead they
 * should extend either {@link
 * DataSetTools.operator.Generic.GenericOperator GenericOperator} or
 * {@link DataSetTools.operator.DataSet.DataSetOperator
 * DataSetOperator}. If it does not then they will not be categorized
 * by {@link Command.Script_Class_ListHandler
 * Script_Class_List_Handler}. The effect of this is that the operatr
 * will not be added to menus, will not be found by the help system,
 * and will not be available in scripts.
 */

abstract public class GenericOperator extends  Operator 
                                      implements Serializable
{

   protected GenericOperator( String title )
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
    return GENERIC;
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
    return AppendCategory( GENERIC, partial_list );
  }

} 

/*
 * File:  DataSetOperator.java
 *
 * Copyright (C) 1999, Dennis Mikkelson
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
 *  Revision 1.5  2003/06/16 19:01:58  pfpeterson
 *  Removed old code and updated to work with new getCategoryList() code
 *  in base operator class.
 *
 *  Revision 1.4  2003/06/12 18:47:50  pfpeterson
 *  Updated javadocs to reflect a idiosycracy of Script_Class_List_Handler.
 *
 *  Revision 1.3  2002/11/27 23:16:27  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/03/05 19:25:01  pfpeterson
 *  Updated @see references in javadocs.
 *
 *  Revision 1.1  2002/02/22 20:59:43  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet;

import java.util.Vector;
import java.io.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.Operator;

/**
 *   Base class for operators that operate on a DataSet object.  This class 
 * extends the Operator class by adding a DataSet member and methods 
 * to get/set the DataSet member. 
 *
 *   A DataSetOperator is typically used in one of two ways.  First, the 
 * operator might be added to the list of operators that work on a particular
 * DataSet.  In this case, the DataSet associated with the operator is set
 * by the DataSet's addOperator method.  If the default constructor for the
 * specific DataSetOperator is used, any additional parameters required
 * must be set later.  In particular, a GUI can get the operator from the 
 * DataSet and have the user specify values for any remaining parameters.
 * After the parameter values are set the getResult() method of the operator
 * is used to carry out the operation.
 *
 *   Alternatively, an operator can be applied directly if the so-called
 * "full constructor" is used.  A full constructor includes the DataSet
 * that is associated with the operator, as well as values for all other 
 * parameters needed by the operator.  In this case, the operation can be 
 * carried out immediately by calling getResult().
 *
 * @see Operator
 * @see DataSetTools.operator.DataSet.Math.DataSet.DataSetAdd
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

abstract public class DataSetOperator extends Operator implements Serializable
{
   private DataSet data_set;
   private static String[] categoryList=null;

   protected DataSetOperator( String title )
   {
      super( title );
      this.data_set = null;
   } 

  /**
   * Set the "this" DataSet for the operator.  That is, a DataSetOperator is
   * an operator that belongs to the set of operations that can be performed
   * on a DataSet.  The DataSet that the operator operates on is set by calling
   * this method when the operator is added to the list of operators for a
   * DataSet.
   *
   *  @param  data_set  the DataSet that this operator will operate on when
   *                    the getResult() method is called.
   */
  public void setDataSet( DataSet data_set )
  {
    this.data_set = data_set; 
  }

  /**
   * Get the "this" DataSet for the operator.  This is the data set on which
   * the operation is to be performed.
   *
   * @return  a reference to the DataSet this operator acts on.
   */
  public DataSet getDataSet()
  {
    return data_set;
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

  /**
   * Get a copy of the current DataSetOperator.  The list of parameters
   * and the reference to the DataSet to which it applies is copied.  This
   * method must be implemented in classes derived from DataSetOperator.
   */
  abstract public Object clone();
} 

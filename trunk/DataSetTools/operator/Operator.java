/*
 * File:  Operator.java 
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
 *  Revision 1.8  2001/07/30 19:52:29  dennis
 *  Added GENERIC SAVE category.
 *
 *  Revision 1.7  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.6  2001/05/23 17:41:46  dennis
 *  Minor improvement to documentation.
 *
 *  Revision 1.5  2001/04/26 19:10:12  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.4  2000/11/10 22:41:34  dennis
 *    Introduced additional abstract classes to better categorize the operators.
 *  Existing operators were modified to be derived from one of the new abstract
 *  classes.  The abstract base class hierarchy is now:
 *
 *   Operator
 *
 *    -GenericOperator
 *       --GenericLoad
 *       --GenericBatch
 *
 *    -DataSetOperator
 *      --DS_EditList
 *      --DS_Math
 *         ---ScalarOp
 *         ---DataSetOp
 *         ---AnalyzeOp
 *      --DS_Attribute
 *      --DS_Conversion
 *         ---XAxisConversionOp
 *         ---YAxisConversionOp
 *         ---XYAxesConversionOp
 *      --DS_Special
 *
 *     To allow for automatic generation of hierarchial menus, each new operator
 *  should fall into one of these categories, or a new category should be
 *  constructed within this hierarchy for the new operator.
 *
 *  Revision 1.3  2000/10/03 21:51:40  dennis
 *  Replaced vector.clear() with vector.removeAllElements() for
 *  compatibility with Java 1.1.8
 *
 *  Revision 1.2  2000/07/10 22:36:12  dennis
 *  Now Using CVS 
 *
 *  Revision 1.8  2000/06/14 16:46:28  dennis
 *  improved comments
 *
 *  Revision 1.7  2000/06/09 16:12:35  dennis
 *  Added getCommand() method to return the abbreviated command string for
 *  this operator
 *
 *  Revision 1.6  2000/05/16 15:35:34  dennis
 *  fixed error in documentation due to DOS text format
 *
 *  Revision 1.5  2000/05/16 15:22:11  dennis
 *  Changed CopyParametersFrom() method to first clear the list of parameters.
 * 
 *  Revision 1.4  2000/05/15 21:43:45  dennis
 *  now uses constant Parameter.NUM_BINS rather than the string
 * "Number of Bins"
 *
 *  Revision 1.3  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 * 
 *  99/06/04  0.2, modified to use the Parameter class, rather than the
 *                 attribute class.
 */

package DataSetTools.operator;

import java.util.Vector;
import java.io.*;

/**
 * The base class for operators.  An operator object provides information about
 * an operation, including a title, parameter names and types.  It also has 
 * methods to set the required parameters and to get the result of performing 
 * the operation, as an object.
 */

abstract public class Operator implements Serializable
{
                                 // Constants giving operator category names
                                 // for the inheritance hierarchy of operators.
                                 // The strings returned in the getCategoryList
                                 // method are chosen from these and used to
                                 // generate menus.
   public static final String  OPERATOR                 = "Operator";
   public static final String    GENERIC                = "Generic";
   public static final String      LOAD                 = "Load";
   public static final String      SAVE                 = "Save";
   public static final String      BATCH                = "Batch";
   public static final String    DATA_SET_OPERATOR      = "DataSet Operator";
   public static final String      EDIT_LIST            = "Edit List";
   public static final String      MATH                 = "Math";
   public static final String        SCALAR             = "Scalar";
   public static final String        DATA_SET_OP        = "DataSet";
   public static final String        ANALYZE            = "Analyze";
   public static final String      ATTRIBUTE            = "Attribute";
   public static final String      CONVERSION           = "Conversion";
   public static final String        X_AXIS_CONVERSION  = "X Axis Conversion";
   public static final String        Y_AXIS_CONVERSION  = "Y Axis Conversion";
   public static final String        XY_AXIS_CONVERSION = "XY Axes Conversion";
   public static final String      SPECIAL              = "Special";



   private   String    title;
   protected Vector    parameters;

   /* --------------------------- Constructor ----------------------------- */
   /** 
    * Constructs an operator object with a specified title and default list 
    * of parameters.
    */

   protected Operator( String title )
   {
      this.title = title;
      setDefaultParameters();
   } 

  /* ----------------------------- getResult ---------------------------- */
  /**
   * Returns the object that is the result of applying this operation.  This
   * should be called after setting the appropriate parameters.  Derived classes
   * will override this method with code that will carry out the required 
   * operation.
   *
   * @return  The result of carrying out this operation is returned as a Java
   *          Object.
   */ 
   abstract public Object getResult();


  /* ------------------------------ getTitle ----------------------------- */
  /**
   * Returns the title for this operation.
   *
   *  @return  The title of the current operation is returned.
   */
   public String getTitle()
   {
     return this.title;
   }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor
   *
   */
   abstract public String getCommand();


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
    return OPERATOR;
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
    String list[] = new String[1];
    list[0] = OPERATOR;
    return list;
  }

  /* -------------------------- AppendCategory ---------------------------- */
  /**
   *  Utility function to append an extra operator category onto an array of
   *  operators.
   */
  String[] AppendCategory( String category, String[] partial_list )
  {
                          // get a new array with an extra space, copy the
                          // original array and append the new category

    String list[] = new String[ partial_list.length + 1 ];

    for ( int i = 0; i < partial_list.length; i++ )
      list[i] = partial_list[i];

    list[ partial_list.length ] = category;
 
    return list;
  }

   /* ---------------------------- addParameter ---------------------------- */
   /**
    * Add the specified parameter to the list of parameters for this operation
    * object.  This method will typically be called by the constructor for the
    * derived class.
    *
    *  @param   parameter   The new (name, value) pair to be added to the list
    *                       of parameters for this object.
    */
   protected void addParameter( Parameter parameter )
   {
     parameters.addElement( parameter.clone() );
   }

  /* ---------------------------- getNum_parameters ------------------------ */
  /**
   * Gets the number of parameters for this operator 
   *
   *  @return  Returns the number of parameters that this operator has.
   */
  public int getNum_parameters()
  {
    return( parameters.size() );
  }


  /* ----------------------------- getParameter -------------------------- */
  /**
   * Get the parameter at the specified index from the list of parameters
   * for this operator.  Note: This returns a reference to the specified
   * parameter.  Consequently the value of the parameter can be altered.
   *
   *  @param  index    The index in the list of parameters of the parameter
   *                   that is to be returned.  "index" must be between 0 and 
   *                   the number of parameters - 1.
   *
   *  @return  Returns the parameters at the specified position in the list
   *           of parameters for this object.  If the index is invalid,
   *           this returns null.
   */
  public Parameter getParameter( int index )
  {
    if ( index >= 0 && index < parameters.size() )
      return( (Parameter)parameters.elementAt( index ) );
    else
      return null;
  }

  /* ---------------------------- setParameter --------------------------- */
  /**
   * Set the parameter at the specified index in the list of parameters
   * for this operator.  The parameter that is set MUST have the same type
   * of value object as that was originally placed in the list of parameters
   * using the addParameter() method.  Typically, the "GUI" will get a parameter
   * from the operator, change its value and then set the parameter back at
   * the same index.
   *
   *  @param  index    The index in the list of parameters of the parameter
   *                   that is to be set.  "index" must be between 0 and the
   *                   number of parameters - 1.
   *
   *  @return  Returns true if the parameter was properly set, and returns 
   *           false otherwise.  Specifically, it returns false if either
   *           the given index is invalid, or the specified parameter
   *           has a different data type than the parameter at the given
   *           index.
   */
  public boolean setParameter( Parameter parameter, int index )
  {
    if ( index < 0 || index >= parameters.size() )
      return false;
 
    if (  parameter.getValue().getClass() == 
          ((Parameter)parameters.elementAt( index )).getValue().getClass() )
    {
      parameters.setElementAt( parameter, index );   // types ok, so record it
      return true;
    }
    else
      return false;
  }


  /* -------------------------- setDefaultParameters ----------------------- */ 
  /**
   *  Set the parameters to default values.  This function should be overridden
   *  in derived classes to produce a reasonable set of default parameter
   *  values.
   */
  abstract public void setDefaultParameters();


  /* ------------------------------ toString ------------------------------- */
  /**
   * "Convert" the current operator to a string by returning it's title.
   *
   *  @return  Returns the name of this operator
   */
  public String toString()
  {
    return title;
  }

  /* -------------------------- CopyParametersFrom ------------------------- */
  /**
   * Copy the parameter list from operator "op" to the current operator.  The
   * original list of parameters is cleared before copying the new parameter
   * list. 
   *
   *  @param  op  The operator object whose parameter list is to be 
   *              copied to the current operator.
   */
  public void CopyParametersFrom( Operator op )
  {
    int      num_param = op.getNum_parameters();

    parameters.removeAllElements();
    for ( int i = 0; i < num_param; i++ )
      addParameter( (Parameter)op.getParameter(i).clone() );
  }

} 

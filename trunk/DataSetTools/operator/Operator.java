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
 *  Revision 1.29  2003/06/19 18:46:21  pfpeterson
 *  Implemented clone().
 *
 *  Revision 1.28  2003/06/17 22:21:26  pfpeterson
 *  Format changes to javadocs.
 *
 *  Revision 1.27  2003/06/17 22:04:27  pfpeterson
 *  Fixed a javadoc.
 *
 *  Revision 1.26  2003/06/17 16:12:19  pfpeterson
 *  Uses StringUtil.split to create the category list array.
 *
 *  Revision 1.25  2003/06/16 19:01:02  pfpeterson
 *  Full reworking of getCategoryList() code. List is now synthesized from
 *  the package of the nearest abstract ancestor. The list is also static
 *  for each abstract class and created only when needed.
 *
 *  Revision 1.24  2003/06/12 18:47:42  pfpeterson
 *  Updated javadocs to reflect a idiosycracy of Script_Class_List_Handler.
 *
 *  Revision 1.23  2003/06/11 21:23:49  pfpeterson
 *  Added functionality to getCommand to work better with Jython operators.
 *
 *  Revision 1.22  2003/05/29 15:24:49  pfpeterson
 *  Removed getCategory() and made a concrete version of setDefaultParameters()
 *  that throws an exception so jython works.
 *
 *  Revision 1.21  2003/05/07 15:08:52  pfpeterson
 *  Changed 'getCommand()' to be a concrete method that takes returns
 *  the class name as the command. The constructor no longer creates a
 *  new vector for the parameters, since most subclasses do this already.
 *  Other functions that work with the parameters altered to accomodate
 *  this change.
 *
 *  Revision 1.20  2002/12/02 17:27:26  pfpeterson
 *  Moved the default documentation into a 'public static final String' so it can be
 *  checked against by outside classes.
 *
 *  Revision 1.19  2002/11/27 23:16:15  pfpeterson
 *  standardized header
 *
 *  Revision 1.18  2002/10/09 16:33:34  dennis
 *  Made form of getDocumentation() method consistent with the earlier code.
 *
 *  Revision 1.17  2002/09/27 22:08:56  pfpeterson
 *  Added another parameter to getDocumentation method.
 *
 *  Revision 1.16  2002/09/19 16:04:59  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.15  2002/05/29 22:45:46  dennis
 *  Uncommented some String definitions.
 *
 *  Revision 1.14  2002/04/08 15:36:27  dennis
 *  Added categories for X, Y and XY Axis Information operators.
 *
 *  Revision 1.13  2002/02/22 20:49:26  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.12  2002/01/11 22:14:56  dennis
 *  Temporarilly added names for new Abstract base classs for
 *  Generic operators for various instruments.
 *
 */

package DataSetTools.operator;

import java.util.Vector;
import java.io.*;
import DataSetTools.parameter.IParameter;
import DataSetTools.util.StringUtil;

/**
 * The base class for operators.  An operator object provides information about
 * an operation, including a title, parameter names and types.  It also has 
 * methods to set the required parameters and to get the result of performing 
 * the operation, as an object.
 *
 * <P><B>NOTE:</B> No class should directly extend Operator. Instead
 * they should extend either {@link
 * DataSetTools.operator.Generic.GenericOperator GenericOperator} or
 * {@link DataSetTools.operator.DataSet.DataSetOperator
 * DataSetOperator}. If it does not then they will not be categorized
 * by {@link Command.Script_Class_List_Handler
 * Script_Class_List_Handler}. The effect of this is that the operatr
 * will not be added to menus, will not be found by the help system,
 * and will not be available in scripts.</P>
 */

abstract public class Operator implements Serializable
{
                                 // Constants giving operator category names
                                 // for the inheritance hierarchy of operators.
                                 // The strings returned in the getCategoryList
                                 // method are chosen from these and used to
                                 // generate menus.
   public static final String  OPERATOR                 = "Operator";

   private static final int dstools_length="DataSetTools.".length();
   private static String[] categoryList=null;

   public static final String DEFAULT_DOCS =  "This is the placeholder "
     +"documentation. The full documentation needs to be written using the "
     +"following options in a manner consistent with JavaDocs\n\n"
     +"@overview\n\n"
     +"@assumptions\n\n"
     +"@algorithm\n\n"
     +"@param\n\n"
     +"@return\n\n"
     +"@error";


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
      parameters = null;
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
  public String getCommand(){
    // tear of the package name
    String command=this.getClass().getName();
    int index=command.lastIndexOf(".");
    if(index>=0)
      command=command.substring(index+1);

    // if there is dollars in the name then we should take what is in
    // between them (this is necessary for python)
    index=command.indexOf("$");
    if(index>=0){
      int end=command.indexOf("$",index+1);
      if(end<=index) end=command.length();
      command=command.substring(index+1,end);
    }

    // return the result
    return command;
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
   * Method to create a category list from this classes nearest
   * abstract parent's package name.
   */
  protected String[] createCategoryList(){
    // determine the correct abstract class
    Class klass=this.getClass();
    //System.out.print(klass.getName()+"->"); // REMOVE
    while(!isAbstract(klass)){
      klass=klass.getSuperclass();
    }

    // get the category name and shorten it
    String category=klass.getPackage().getName().substring(dstools_length);

    // split up into an array and return
    return StringUtil.split(category,".");
  }

  static private boolean isAbstract(Class klass){
    int modifier=klass.getModifiers();
    return java.lang.reflect.Modifier.isAbstract(modifier);
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
   protected void addParameter( IParameter parameter )
   {
       if(parameters==null)
         parameters=new Vector();
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
    if(parameters==null)
      return 0;
    else
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
  public IParameter getParameter( int index )
  {
    if(parameters==null)
      return null;
    else if ( index >= 0 && index < parameters.size() )
      return( (IParameter)parameters.elementAt( index ) );
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
  public boolean setParameter( IParameter parameter, int index )
  { 
    if ( index < 0 || parameters==null || index >= parameters.size() )
      return false;
                                             // NOTE: object parameters are
                                             //       represented using null
   
    Object value = ((IParameter)parameters.elementAt( index )).getValue();
    if (  value == null || parameter.getValue() == null ||
          value.getClass() == parameter.getValue().getClass() ) 
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
   *
   * This method was changed from abstract to throw an exception so it
   * can play nice with jython.
   */
  public void setDefaultParameters(){
    throw new java.lang.IllegalStateException("subclass must implement setDefaultParameters()");
  }

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

    if(parameters!=null)
      parameters.removeAllElements();
    else
      parameters=new Vector();
    for ( int i = 0; i < num_param; i++ )
      addParameter( (IParameter)op.getParameter(i).clone() );
  }

  /**
   * This should take care of clone for the majority of
   * operators. Classes that need more can still override this method.
   */
  public Object clone(){
    try{
      Operator operator=(Operator)this.getClass().newInstance();
      operator.CopyParametersFrom(this);
      return operator;
    }catch(InstantiationException e){
      throw new InstantiationError(e.getMessage());
    }catch(IllegalAccessException e){
      throw new IllegalAccessError(e.getMessage());
    }
  }

  /* -------------------------- getDocumentation --------------------------- */
  /**
   * Returns a string containing the end-user documentation for the
   * new help system.
   */
  public String getDocumentation(){
    return DEFAULT_DOCS;
  }
} 

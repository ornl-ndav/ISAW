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
 *  Revision 1.49  2005/08/25 14:51:36  dennis
 *  Made/added to category DATA_SET_ANALYZE_MACROS.
 *
 *  Revision 1.48  2005/08/24 20:29:20  dennis
 *  Added/Moved to menu DATA_SET_INFO_MACROS
 *
 *  Revision 1.47  2005/08/24 20:22:00  dennis
 *  Added/moved to category DATA_SET_FILTERS_MACROS
 *
 *  Revision 1.46  2005/08/24 20:11:30  dennis
 *  Added/moved to Macros->Data Set->Edit List menu.
 *
 *  Revision 1.45  2005/08/24 19:51:03  dennis
 *  Changed logical name of menu from UTILS_DATA_SET to
 *  DATA_SET_MACROS
 *
 *  Revision 1.44  2005/08/24 19:47:38  dennis
 *  Changed logical name of menu from UTILS_EXAMPLES to GENERAL_EXAMPLES.
 *
 *  Revision 1.43  2005/08/24 19:25:02  dennis
 *  Changed UTILS_EXAMPLES string list to point to be just
 *  {"operator", "Examples"}, so that the Examples directory is at
 *  the top level.
 *
 *  Revision 1.42  2005/08/05 20:08:42  rmikk
 *  Fixed setParameter to deal with the new ParameterGUI's better. Needed for
 *   Wizards to act properly.
 *
 *  Revision 1.41  2005/06/17 13:16:48  dennis
 *  Minor fixes to documentation.
 *
 *  Revision 1.40  2005/01/28 19:28:22  dennis
 *  Added category Utils->Tests
 *
 *  Revision 1.39  2005/01/20 21:51:43  dennis
 *  Fixed minor javadoc error.
 *
 *  Revision 1.38  2005/01/10 15:05:39  dennis
 *  Added string lists for menu categories for FILE: LOAD, SAVE, PRINT
 *  and UTILS: CALCULATORS, CONVERSIONS, EXAMPLES, DATA_SET and SYSTEM.
 *
 *  Revision 1.37  2005/01/07 17:26:41  rmikk
 *  Added the fields that were possible return values for getCategoryList
 *     method that were in IWrappedWithCategoryList.
 *
 *  Revision 1.36  2005/01/02 17:59:34  rmikk
 *  Added a getSource Method for producing documentation.
 *  
 *  Eliminated a warning
 *
 *  Revision 1.35  2004/05/24 18:37:42  rmikk
 *  Eliminated a clone in the CopyParametersfrom method because the
 *    AddParametersMethod already clones the parameter
 *
 *  Revision 1.34  2004/01/08 22:27:58  bouzekc
 *  Now uses java.lang.String.split() rather than StringUtil.split().
 *  Changed indexing scheme for removing first name from package hierarchy
 *  so that it is based on the first "." rather than a set number of
 *  characters.
 *
 *  Revision 1.33  2004/01/08 14:48:07  bouzekc
 *  Changed title to getTitle() in toString().
 *
 *  Revision 1.32  2003/10/29 01:09:29  bouzekc
 *  Made field ds_tools and method isAbstract() protected for use with the
 *  JavaWrapperOperator.
 *
 *  Revision 1.31  2003/10/23 16:56:16  bouzekc
 *  Added method getResultRemotely().
 *
 *  Revision 1.30  2003/08/11 18:00:24  bouzekc
 *  Added protected final method to reset the parameters Vector.
 *
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
 *  Moved the default documentation into a 'public static final String' so 
 *  it can be checked against by outside classes.
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
import DataSetTools.util.SharedData;
import DataSetTools.parameter.*;
import NetComm.RemoteOpExecClient;

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
  /**
   *  String arrays that return useful values for the getCategoryList
   *  method of derived classes of operators.
   */
   public static final String[] TOF_NSAS= {"operator","Instrument Type",
                                                   "TOF_NSAS"};
                                     
   public static final String[] TOF_NSCD = {"operator","Instrument Type",
                                                       "TOF_NSCD"};
                                                     
   public static final String[] TOF_NPD = {"operator","Instrument Type",
                                                      "TOF_NPD"};
                                                     
   public static final String[] TOF_NDGS = {"operator","Instrument Type",
                                                       "TOF_NDGS"};
                                                        
   public static final String[] TOF_NGLAD = {"operator","Instrument Type",
                                                        "TOF_NGLAD"};
   
   public static final String[] FILE_LOAD = {"operator","File",
                                                        "Load"};
   
   public static final String[] FILE_SAVE = {"operator","File",
                                                        "Save"};

   public static final String[] FILE_PRINT = {"operator","File",
                                                         "Print"};

   public static final String[] UTILS_CALCULATORS = {"operator","Utils",
                                                          "Calculators"};
   
   public static final String[] UTILS_CONVERSIONS = {"operator","Utils",
                                                                "Convert"};
   
   public static final String[] GENERAL_EXAMPLES = {"operator", "Examples"};
   
   public static final String[] UTILS_TESTS = {"operator","Utils", 
                                                          "Tests"};
   
   public static final String[] DATA_SET_MACROS = {"operator", "DataSet"};

   public static final String[] DATA_SET_EDIT_LIST_MACROS = 
                                         {"operator", "DataSet", "Edit List"};

   public static final String[] DATA_SET_FILTERS_MACROS = 
                                         {"operator", "DataSet", "Filters"};
   
   public static final String[] DATA_SET_INFO_MACROS = 
                                         {"operator", "DataSet", "Info"};

   public static final String[] DATA_SET_ANALYZE_MACROS =
                                         {"operator", "DataSet", "Analyze"};

   
   public static final String[] UTILS_SYSTEM = {"operator","Utils",
                                                           "System"};
   
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


  /* -------------------------- getResultRemotely ----------------------- */
   /**
    * Uses RemoteOpExecClient to get a remote result.  The connection is made
    * using the REMOTE_HOST and REMOTE_PORT specified in IsawProps.dat.
    *
    * @return The result of executing this Operator remotely.
    */
   public Object getResultRemotely(  ) {
     RemoteOpExecClient operatorExecutor = new RemoteOpExecClient(  );

     operatorExecutor.setHost( SharedData.getProperty( "REMOTE_HOST" ) );
     String port = SharedData.getProperty( "REMOTE_PORT" );
     operatorExecutor.setPort( Integer.parseInt( port ) );
     operatorExecutor.MakeConnection(  );

     Object result = operatorExecutor.getResult( this );
     operatorExecutor.Exit(  );

     return result;
   }


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


   /* ------------------------- createCategoryList ---------------------- */
   /**
    * Method to create a category list from this classes nearest
    * abstract parent's package name.
    */
   protected String[] createCategoryList(){

     // determine the correct abstract class
     Class klass=this.getClass();

     while(!isAbstract(klass)){
       klass=klass.getSuperclass();
     }

     // get the category name and shorten it
     String category=klass.getPackage().getName();
     category = category.substring( category.indexOf( "." ) + 1, 
                                                       category.length(  ) );

     // split up into an array and return
     return category.split( "\\." );
  }


   /* ---------------------------- isAbstract ---------------------------- */
   /**
    *  Check whether or not the specified class is abstract.
    *
    *  @param  klass  The class to check
    */
   static protected boolean isAbstract(Class klass){
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
       IParameter newParameter = (IParameter)parameter.clone();
       parameters.addElement( newParameter );
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
    if( parameter instanceof IParameterGUI)
       if(!(parameter instanceof RealArrayPG))
           if( parameter.getClass().equals
                    (parameters.elementAt(index).getClass())){
               parameters.setElementAt( parameter, index);
               return true;
           }
    
                 
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
   *  values.  This method was changed from abstract to throw an exception 
   *  so it can play nice with jython.
   */
  public void setDefaultParameters(){
    throw new java.lang.IllegalStateException(
                             "subclass must implement setDefaultParameters()");
  }


  /* ------------------------------ toString ------------------------------- */
  /**
   * "Convert" the current operator to a string by returning it's title.
   *
   *  @return  Returns the name of this operator
   */
  public String toString()
  {
    return getTitle();
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
    int num_param = op.getNum_parameters();

    if(parameters!=null){
      while(parameters.size()>0){
      	//Object O =
            parameters.remove(0);
      }
      parameters = null;
      
      parameters = new Vector();
      //parameters.removeAllElements();
    }else
      parameters=new Vector();
    for ( int i = 0; i < num_param; i++ )
      addParameter( (IParameter)op.getParameter(i) );
  }


  /* ------------------------------ clone --------------------------------- */
  /**
   * Default clone method to take care of clone operation for the majority of
   * operators.  Classes that need more can still override this method.
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


  /* ------------------------- getDocumentation --------------------------- */
  /**
   * Returns a string containing the end-user documentation for the
   * new help system.
   */
  public String getDocumentation(){
    return DEFAULT_DOCS;
  }


  /* ----------------------- cleanParametersVector ------------------------ */
  /**
   * Utility method for clearing parameters Vector from Jython subclasses.
   * By making this final protected, Jython code can access it in
   * setDefaultParameters.
   */
  protected final void clearParametersVector(  ) {
    parameters = new Vector(  );
  }
  

 /* ------------------------------- getSource ----------------------------- */
 /**
  * Returns the filename or classname associated with this operator.  It 
  * translates ScriptOperators, PyScriptOperators, and wrappables.
  *
  * @return   The filename or classname for the source of this operator
  */ 
 public String getSource(){
   Operator op = this;
   if( op instanceof JavaWrapperOperator){
     return ((JavaWrapperOperator)op).getWrappable().getClass().toString();
   }else if( op instanceof Command.ScriptOperator){
      return ((Command.ScriptOperator)op).getFileName();
   }else if( op instanceof PyScriptOperator){
      return ((PyScriptOperator)op).getFileName();
   }else
      return op.getClass().toString();
 }
} 

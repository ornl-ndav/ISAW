/*
 * @(#)Operator.java     0.2  99/06/02  Dennis Mikkelson
 *
 *  99/06/04  0.2, modified to use the Parameter class, rather than the
 *                 attribute class.
 *
 *  $Log$
 *  Revision 1.3  2000/10/03 21:51:40  dennis
 *  Replaced vector.clear() with vector.removeAllElements() for
 *  compatibility with Java 1.1.8
 *
 *  Revision 1.2  2000/07/10 22:36:12  dennis
 *  July 10, 2000 version... many changes
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
 */

package DataSetTools.operator;

import java.util.Vector;
import java.io.*;

/**
 * The root class for operators.  An operator object provides information about
 * an operation, including a title, parameter names and types.  It also has 
 * methods to set the required parameters and to get the result of performing 
 * the operation, as an object.
 */

abstract public class Operator implements Serializable
{
   private   String    title;
   protected Vector    parameters;

   /* --------------------------- Constructor ----------------------------- */
   /** 
    * Constructs an operator object by specifying the title and list of
    * paramters. 
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
   * Returns the abbreviated command string for this operator.
   *
   */
   abstract public String getCommand();


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

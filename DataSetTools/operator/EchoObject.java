/*
 * @(#)EchoObject.java   1.0  2000/10/19   Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.2  2000/11/10 22:41:34  dennis
 *     Introduced additional abstract classes to better categorize the operators.
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
 *  Revision 1.1  2000/11/07 16:23:10  dennis
 *  Operator to display messages on the console terminal.
 *
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;

/**
 * This operator prints the result of calling an object's toString() method
 * on the standard output and returns the string as the result of the operation.
 *
 */

public class EchoObject extends    GenericBatch 
                                   implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct a default EchoObject operator with a default Object.
   */

  public EchoObject( )
  {
    super( "Echo Object" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an EchoObject operator that will print the given object using
   *  the toString() method.
   *
   *  @param  ob   The object to print. 
   */

  public EchoObject( Object ob )
  {
    this();
    Parameter parameter = getParameter(0);
    parameter.setValue( ob );
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "Echo";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the object to an default Object.
  */
  public void setDefaultParameters()
  {
     parameters = new Vector();  // must do this to create empty list of 
                                 // parameters

     Parameter parameter= new Parameter("Object to Echo ", null );
     addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
    Object ob     = getParameter(0).getValue();

    String result = ob.toString();

    System.out.println( result );

    return result;
  }  

  /* --------------------------- main ----------------------------------- */
  /*
   *  Main program for testing purposes
   */ 
  public static void main( String[] args )
  {
    Operator op = new EchoObject();
 
    String list[] = op.getCategoryList();
    System.out.println( "Categories are: " );
    for ( int i = 0; i < list.length; i++ )
      System.out.println( list[i] );
  }

}

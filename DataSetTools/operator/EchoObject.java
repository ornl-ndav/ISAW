/*
 * @(#)EchoObject.java   1.0  2000/10/19   Dennis Mikkelson
 *
 *  $Log$
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

public class EchoObject extends    Operator
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


}

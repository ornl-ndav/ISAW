/*
 * @(#)sin.java   00-07-12  Ruth Mikkelson
 *             
 *
 */


package Command;
import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import DataSetTools.operator.*;
/**
  *  Allows the user to set attributes
  *
  *  @see DataSetOperator
  *  @see Operator
  */

public class sin extends   Operator 
                                   implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public sin( )
  {
    super( "sin" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  f         The angle in radians to sin
   * 
   *
   */

  public sin  ( Float f
                     
			   )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( f);

    

                  // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "sin";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters


    Parameter parameter = new Parameter( "Angle?", new Float(0) );
    addParameter( parameter );
   
   
    
    
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
    { double A;
       A= ((Float)(getParameter(0).getValue())).doubleValue();
       A = java.lang.Math.sin(A);
       return new Float((float)A);
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current sin Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    
   sin new_op    = new sin( );
                                                 // copy the data set associated
                                                 // with this operator
   
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}

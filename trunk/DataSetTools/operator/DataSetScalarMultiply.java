/*
 * @(#)DataSetScalarMultiply.java   0.2  99/06/02   Dennis Mikkelson
 *                                       99/08/16   Added constructor to allow
 *                                                  calling operator directly
 *             
 *
 *  $Log$
 *  Revision 1.5  2000/11/10 22:41:34  dennis
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
 *  Revision 1.4  2000/07/10 22:35:58  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.7  2000/06/15 16:26:14  dennis
 *  Added parameter make_new_ds to determine wheter or not a new DataSet
 *  is created.  Also changed getResult to call the static method:
 *  DSOpsImplementation.DoDSScalarOp() so that the implementation could
 *  be shared for +,-,*,/
 *
 *  Revision 1.6  2000/06/09 16:12:35  dennis
 *  Added getCommand() method to return the abbreviated command string for
 *  this operator
 *
 *  Revision 1.5  2000/05/16 15:36:34  dennis
 *  Fixed clone() method to also copy the parameter values from
 *  the current operator.
 *
 *  Revision 1.4  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 *
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;

/**
  * This operator multiplies all data objects in a data set by a scalar value.
  */

public class DataSetScalarMultiply extends    ScalarOp 
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

  public DataSetScalarMultiply( )
  {
    super( "Multiply by Scalar" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @parm   value       The value to multiply times each point in each Data
   *                      block in ds 
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the value is just multiplied times
   *                      the Data blocks of the DataSet.
   */

  public DataSetScalarMultiply( DataSet ds, float value, boolean make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s)

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float( value) );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "Mult";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Scale Factor", new Float(1.0) );
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
    return DSOpsImplementation.DoDSScalarOp( this );
  }


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetScalarMultiply Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    DataSetScalarMultiply new_op = new DataSetScalarMultiply( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}

/*
 * @(#)DataSetAdd.java   0.2  99/07/13   Dennis Mikkelson
 *                            99/08/16   Added constructor to allow
 *                                       calling operator directly
 *             
 * This operator adds two DataSets by adding the corresponding Data "blocks" in
 * the DataSets.
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.3  2000/07/10 22:35:49  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.8  2000/06/14 21:21:54  dennis
 *  getResult() method now calls DSOpsImplementation.DoDSBinaryOp( this )
 *  so that add, subtract, multiply and divide can all share the same
 *  basic implemention.
 *
 *  Revision 1.7  2000/06/09 16:12:35  dennis
 *  Added getCommand() method to return the abbreviated command string for
 *  this operator
 *
 *  Revision 1.6  2000/05/16 15:36:34  dennis
 *  Fixed clone() method to also copy the parameter values from
 *  the current operator.
 *
 *  Revision 1.5  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 *
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  *  Add corresponding Data "blocks" from the current DataSet to Data "blocks"
  *  from a second DataSet to form a new DataSet. This operator adds two 
  *  DataSets by adding the corresponding Data blocks in the DataSets to form 
  *  a new DataSet.  A Data block in the current DataSet will be added to the 
  *  first Data block in the second DataSet with the same Group ID provided
  *  that they have the same units, the same number of data values and extend 
  *  over the same X-interval.
  *
  *  @see DataSetOperator
  *  @see Operator
  */

public class DataSetAdd extends    DataSetOperator 
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

  public DataSetAdd( )
  {
    super( "Add a DataSet" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @parm   ds_to_add   The DataSet to be added to DataSet ds.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the Data blocks of the second 
   *                      DataSet are just added to the Data blocks of the
   *                      first DataSet.
   */

  public DataSetAdd( DataSet    ds,
                     DataSet    ds_to_add,
                     boolean    make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( ds_to_add );

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
     return "Add";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "DataSet to Add",
                              new DataSet("DataSetToAdd", "Empty DataSet") );
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {       
    return DSOpsImplementation.DoDSBinaryOp( this );
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetAdd Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    DataSetAdd new_op    = new DataSetAdd( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}

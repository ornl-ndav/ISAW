/*
 * @(#)DeleteCurrentlySelected.java   0.1  2000/03/16   Dennis Mikkelson
 *             
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.1  2000/07/10 22:36:03  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.9  2000/06/09 16:12:35  dennis
 *  Added getCommand() method to return the abbreviated command string for
 *  this operator
 *
 *  Revision 1.8  2000/06/08 15:25:10  dennis
 *  hanged order of parameters to be more consistent with other operators.
 *
 *  Revision 1.7  2000/05/25 19:03:38  dennis
 *  Fixed interpretation of "Keep selected data".
 *
 *  Revision 1.6  2000/05/16 15:36:34  dennis
 *  Fixed clone() method to also copy the parameter values from
 *  the current operator.
 *
 *  Revision 1.5  2000/05/15 21:43:45  dennis
 *  now uses constant Parameter.NUM_BINS rather than the string
 *  "Number of Bins"
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
import  DataSetTools.util.*;

/**
  *  Remove Data blocks that are marked as selected, or that are not marked
  *  as selected, depending on the paramters. 
  */

public class DeleteCurrentlySelected  extends    DataSetOperator 
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

  public DeleteCurrentlySelected( )
  {
    super( "Delete currently selected Data blocks" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *
   *  @param  status      Flag that determines whether the selected or 
   *                      un-selected Data blocks are deleted.  
   *                      If status==true, the selected blocks are deleted.
   *                      If status==false, the un-selected blocks are deleted.
   *
   *  @param  make_new_ds Flag that determines whether removing the Data
   *                      blocks makes a new DataSet and returns the new 
   *                      DataSet as a value, or just removes the selected
   *                      blocks from the current DataSet and returns a 
   *                      message indicating that the remove operation was
   *                      done.
   */

  public DeleteCurrentlySelected( DataSet  ds,
                                  boolean  status,
                                  boolean  make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Boolean( status ) );

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
     return "DelSel";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Delete (or keep) selected groups?",
                               new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  // get the parameters

    boolean status      = ((Boolean)getParameter(0).getValue()).booleanValue();
    boolean make_new_ds = ((Boolean)getParameter(1).getValue()).booleanValue();

    DataSet ds     = this.getDataSet();

    DataSet new_ds = ds;             // set new_ds to either a reference to ds
    if ( make_new_ds )               // or a clone of ds
      new_ds = (DataSet)ds.clone();

    new_ds.removeSelected( status );
    new_ds.clearSelections();

    if ( status )
      new_ds.addLog_entry( "Deleted selected Data blocks" );
    else
      new_ds.addLog_entry( "Deleted un-selected Data blocks" );

    if ( make_new_ds )                           
      return new_ds;
    else
    {
      new_ds.notifyIObservers( IObserver.DATA_DELETED );
      return new String("Specified Data blocks REMOVED");
    }                           
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DeleteCurrentlySelected Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    DeleteCurrentlySelected new_op = new DeleteCurrentlySelected( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}

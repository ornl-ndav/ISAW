/*
 * @(#)SumCurrentlySelected.java   0.1  2000/05/10   Dennis Mikkelson
 *             
 * This operator sums all "selected" Data blocks in a DataSet, then deletes
 * the selected Data blocks and adds the new summed Data block.
 *
 * $Log$
 * Revision 1.1  2000/07/10 22:36:25  dennis
 * July 10, 2000 version... many changes
 *
 * Revision 1.6  2000/06/14 22:17:51  dennis
 * Placed make_new_ds parameter last in list of parameters for consistency
 * with the other operators.
 *
 * Revision 1.5  2000/06/09 16:12:35  dennis
 * Added getCommand() method to return the abbreviated command string for
 * this operator
 *
 * Revision 1.4  2000/05/25 19:06:14  dennis
 * fixed problem with documentation format
 *
 *  Revision 1.3  2000/05/16 15:36:34  dennis
 *  Fixed clone() method to also copy the parameter values from
 *  the current operator.
 * 
 *  Revision 1.2  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  *  Replace Data blocks with the sum of the Data blocks.  Data blocks that 
  *  are marked as selected, or that are  not marked as selected, are summed
  *  and replaced depending on the paramters. 
  */

public class SumCurrentlySelected  extends    DataSetOperator 
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

  public SumCurrentlySelected( )
  {
    super( "Sum currently selected Data blocks" );
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
   *                      un-selected Data blocks are summed.
   *                      If status==true, the selected blocks are summed.
   *                      If status==false, the un-selected blocks are summed.
   *
   *  @param  make_new_ds Flag that determines whether removing the Data
   *                      blocks makes a new DataSet and returns the new 
   *                      DataSet as a value, or just sums and removes the 
   *                      Data blocks from the current DataSet and returns a 
   *                      message indicating that the sum operation was
   *                      done.
   */

  public SumCurrentlySelected( DataSet  ds,
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
     return "SumSel";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Sum selected (or un-selected) groups?",
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

    Data    sum   = null;
    Data    data  = null;
    boolean first = true;
    int num_data = ds.getNum_entries();
    int ids[] = new int[num_data];
    int n_summed = 0;
    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );
      if ( data.isSelected() == status )  
      {                                     
        ids[n_summed] = data.getGroup_ID();    // record the Data blocks that
        n_summed++;                            // were summed 

        if ( first )                           // initialize the sum Data block
        {
          sum   = (Data)data.clone();
          first = false;
        }
        else                                   // add later Data blocks
        {
          sum = sum.add( data );
          if ( sum == null )                   // somethings wrong, bail out
          {
            ErrorString message = new ErrorString(
                           "ERROR: Data block not compatible for adding" );
            System.out.println( message );
            return message;
          }
        } 
      }
    }
                            // now remove the summed Data and append the sum
    new_ds.removeSelected( status );
    new_ds.addData_entry( sum );
    new_ds.clearSelections();

    String list = new String("[");
    for ( int i = 0; i < n_summed; i++)
      list = list +  ids[i] + ",";
    list = list + "]";

    if ( status )
      new_ds.addLog_entry( "Summed selected Data blocks: "+list );
    else
      new_ds.addLog_entry( "Summed un-selected Data blocks: "+list );

    if ( make_new_ds )                           
      return new_ds;
    else
    {
      new_ds.notifyIObservers( IObserver.DATA_DELETED );
      return new String("Specified Data blocks SUMMED");
    }                           
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SumCurrentlySelected Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    SumCurrentlySelected new_op = new SumCurrentlySelected( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}

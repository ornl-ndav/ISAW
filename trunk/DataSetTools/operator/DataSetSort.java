/*
 * @(#)DataSetSort.java   0.2  99/07/28   Dennis Mikkelson
 *                             99/08/16   Added constructor to allow
 *                                        calling operator directly
 *             
 * This operator sorts a DataSet based on an attribute of the Data entries.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.util.*;
import  DataSetTools.dataset.*;

/**
  *  Sort a data set. 
  */

public class DataSetSort  extends    DataSetOperator 
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

  public DataSetSort( )
  {
    super( "Sort on one group attribute" );

    Parameter parameter = new Parameter("Group Attribute to Sort on",
                               new AttributeNameString("Raw Detector Angle") );
    addParameter( parameter );

    parameter = new Parameter("Sort in Increasing Order?", new Boolean(true) );
    addParameter( parameter );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  attr_name   The name of that attribute to be used for the
   *                      sort criterion
   *  @param  increasing  Flag that indicates whether the sort should put
   *                      the Data blocks in increasing or decreasing order
   *                      based on the specified attribute
   */

  public DataSetSort( DataSet             ds,
                      AttributeNameString attr_name,
                      boolean             increasing   )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( attr_name );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( increasing ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  // get the parameters

    String attr_name = ((SpecialString)getParameter(0).getValue()).toString();
    boolean increasing = ((Boolean)getParameter(1).getValue()).booleanValue();

    DataSet ds     = this.getDataSet();
    DataSet new_ds = (DataSet)ds.clone();

    new_ds.addLog_entry( "Sorted by " + attr_name );

    if ( new_ds.Sort(attr_name, increasing) )
      return new_ds;
    else
      {
        ErrorString message = new ErrorString(
                           "ERROR: Sort failed... no attribute: " + attr_name );
        System.out.println( message );
        return message;
      }
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetSort Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    DataSetSort new_op    = new DataSetSort( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }


}

/*
 * @(#)DataSetMultiSort.java   0.2  99/08/05   Dennis Mikkelson
 *                                  99/08/16   Added constructor to allow
 *                                             calling operator directly
 *             
 * This operator sorts a DataSet based on multiple attributes of the Data 
 * entries.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.util.*;
import  DataSetTools.dataset.*;

/**
  *  Sort a data set using multiple attributes. 
  */

public class DataSetMultiSort  extends    DataSetOperator 
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

  public DataSetMultiSort( )
  {
    super( "Sort on mutliple group attributes" );
                                               // First Key.....

    Parameter parameter = new Parameter("First Group Attribute to Sort on",
                               new AttributeNameString("Raw Detector Angle") );
    addParameter( parameter );

    parameter = new Parameter("Sort in Increasing Order?", new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter("Use this attribute to sort?", new Boolean(true));
    addParameter( parameter );

                                               // Second Key.....

    parameter = new Parameter("Second Group Attribute to Sort on",
                               new AttributeNameString("Raw Detector Angle") );
    addParameter( parameter );

    parameter = new Parameter("Sort in Increasing Order?", new Boolean(true) );
    addParameter( parameter );
  
    parameter = new Parameter("Use this attribute to sort?",new Boolean(false));
    addParameter( parameter );

                                               // Third Key.....

    parameter = new Parameter("Third Group Attribute to Sort on",
                               new AttributeNameString("Raw Detector Angle") );
    addParameter( parameter );

    parameter = new Parameter("Sort in Increasing Order?", new Boolean(true) );
    addParameter( parameter );
  
    parameter = new Parameter("Use this attribute to sort?",new Boolean(false));
    addParameter( parameter );

  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds           The DataSet to which the operation is applied
   *  @param  attr_name_1  The name of the first attribute to be used for the
   *                       sort
   *  @param  increasing_1 Flag that indicates whether the sort should place
   *                       the items in increasing ( vs. decreasing ) order
   *  @param  use_it_1     Flag that indicates whether this attribute is to
   *                       actually be used, or is to be ignored.
   *  @param  attr_name_2  The name of the second attribute to be used for the
   *                       sort
   *  @param  increasing_2 Flag that indicates whether the sort should place
   *                       the items in increasing ( vs. decreasing ) order
   *  @param  use_it_2     Flag that indicates whether this attribute is to
   *                       actually be used, or is to be ignored.
   *  @param  attr_name_3  The name of the third attribute to be used for the
   *                       sort
   *  @param  increasing_3 Flag that indicates whether the sort should place
   *                       the items in increasing ( vs. decreasing ) order
   *  @param  use_it_3     Flag that indicates whether this attribute is to
   *                       actually be used, or is to be ignored.
   */

  public DataSetMultiSort( DataSet             ds,
                           AttributeNameString attr_name_1,
                           boolean             increasing_1,
                           boolean             use_it_1,
                           AttributeNameString attr_name_2,
                           boolean             increasing_2,
                           boolean             use_it_2,
                           AttributeNameString attr_name_3,
                           boolean             increasing_3,
                           boolean             use_it_3     )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );            // first attribute
    parameter.setValue( attr_name_1 );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( increasing_1 ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Boolean( use_it_1 ) );


    parameter = getParameter( 3 );                      // second attribute
    parameter.setValue( attr_name_2 );

    parameter = getParameter( 4 );
    parameter.setValue( new Boolean( increasing_2 ) );

    parameter = getParameter( 5 );
    parameter.setValue( new Boolean( use_it_2 ) );


    parameter = getParameter( 6 );                      // third attribute
    parameter.setValue( attr_name_3 );

    parameter = getParameter( 7 );
    parameter.setValue( new Boolean( increasing_3 ) );

    parameter = getParameter( 8 );
    parameter.setValue( new Boolean( use_it_3 ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  // get the parameters

    String attr_name_1 = ((SpecialString)getParameter(0).getValue()).toString();
    boolean increasing_1 = ((Boolean)getParameter(1).getValue()).booleanValue();
    boolean use_it_1     = ((Boolean)getParameter(2).getValue()).booleanValue();

    String attr_name_2 = ((SpecialString)getParameter(3).getValue()).toString();
    boolean increasing_2 = ((Boolean)getParameter(4).getValue()).booleanValue();
    boolean use_it_2     = ((Boolean)getParameter(5).getValue()).booleanValue();

    String attr_name_3 = ((SpecialString)getParameter(6).getValue()).toString();
    boolean increasing_3 = ((Boolean)getParameter(7).getValue()).booleanValue();
    boolean use_it_3     = ((Boolean)getParameter(8).getValue()).booleanValue();

    DataSet ds     = this.getDataSet();
    DataSet new_ds = (DataSet)ds.clone();

                                   // now try to sort on each of the attributes
                                   // requested.  If the sort succeeds, add a 
                                   // a log entry, else exit with an error
    if ( use_it_3 )
    {
      if ( new_ds.Sort(attr_name_3, increasing_3) )
        new_ds.addLog_entry( "Sorted by " + attr_name_3 );
      else
      {
        ErrorString message = new ErrorString(
                        "ERROR: Sort failed... no attribute: " + attr_name_3 );
        return message;
      }
    }

    if ( use_it_2 )
    {
      if ( new_ds.Sort(attr_name_2, increasing_2) )
        new_ds.addLog_entry( "Sorted by " + attr_name_2 );
      else
      {
        ErrorString message = new ErrorString(
                       "ERROR: Sort failed... no attribute: " + attr_name_2 );
        return message;
      }
    }

    if ( use_it_1 )
    {
      if ( new_ds.Sort(attr_name_1, increasing_1) )
        new_ds.addLog_entry( "Sorted by " + attr_name_1 );
      else
      { 
        ErrorString message = new ErrorString(
                       "ERROR: Sort failed... no attribute: " + attr_name_1 );
        return message;
      }
    }

    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetMultiSort Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    DataSetMultiSort new_op    = new DataSetMultiSort( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }


}

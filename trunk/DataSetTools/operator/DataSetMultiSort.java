/*
 * @(#)DataSetMultiSort.java   0.2  99/08/05   Dennis Mikkelson
 *                                  99/08/16   Added constructor to allow
 *                                             calling operator directly
 *             
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.5  2000/11/07 15:59:06  dennis
 *  Replaced "groups" with "Groups" in operator title.
 *
 *  Revision 1.4  2000/07/10 22:35:53  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.9  2000/06/09 16:12:35  dennis
 *  Added getCommand() method to return the abbreviated command string for
 *  this operator
 *
 *  Revision 1.8  2000/06/08 15:25:59  dennis
 *  Changed type casting of attribute names from (SpecialString) to
 *  (AttributeNameString).
 *
 *  Revision 1.7  2000/05/16 15:36:34  dennis
 *  Fixed clone() method to also copy the parameter values from
 *  the current operator.
 *
 *  Revision 1.6  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 *
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.util.*;
import  DataSetTools.dataset.*;

/**
  * This operator sorts a DataSet based on multiple attributes of the Data 
  * entries.
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
    super( "Sort on Group attributes" );
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
   *  @param  make_new_ds  Flag that determines whether the sort creates a
   *                       new DataSet and returns the new DataSet as a value,
   *                       or just does the sort "in place" and just returns
   *                       a message indicating the sort was done.
   */

  public DataSetMultiSort( DataSet   ds,
                           String    attr_name_1,
                           boolean   increasing_1,
                           boolean   use_it_1,
                           String    attr_name_2,
                           boolean   increasing_2,
                           boolean   use_it_2,
                           String    attr_name_3,
                           boolean   increasing_3,
                           boolean   use_it_3,
                           boolean   make_new_ds     )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );            // first attribute
    parameter.setValue( new AttributeNameString(attr_name_1) );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( increasing_1 ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Boolean( use_it_1 ) );


    parameter = getParameter( 3 );                      // second attribute
    parameter.setValue( new AttributeNameString(attr_name_2) );

    parameter = getParameter( 4 );
    parameter.setValue( new Boolean( increasing_2 ) );

    parameter = getParameter( 5 );
    parameter.setValue( new Boolean( use_it_2 ) );


    parameter = getParameter( 6 );                      // third attribute
    parameter.setValue( new AttributeNameString(attr_name_3) );

    parameter = getParameter( 7 );
    parameter.setValue( new Boolean( increasing_3 ) );

    parameter = getParameter( 8 );
    parameter.setValue( new Boolean( use_it_3 ) );

    parameter = getParameter( 9 );
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
     return "SortMK";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("First Group Attribute to Sort on",
                               new AttributeNameString(Attribute.RAW_ANGLE) );
    addParameter( parameter );

    parameter = new Parameter("Sort in Increasing Order?", new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter("Use this attribute to sort?", new Boolean(true));
    addParameter( parameter );

                                               // Second Key.....

    parameter = new Parameter("Second Group Attribute to Sort on",
                               new AttributeNameString(Attribute.RAW_ANGLE) );
    addParameter( parameter );

    parameter = new Parameter("Sort in Increasing Order?", new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter("Use this attribute to sort?",new Boolean(false));
    addParameter( parameter );

                                               // Third Key.....

    parameter = new Parameter("Third Group Attribute to Sort on",
                               new AttributeNameString(Attribute.RAW_ANGLE) );
    addParameter( parameter );

    parameter = new Parameter("Sort in Increasing Order?", new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter("Use this attribute to sort?",new Boolean(false));
    addParameter( parameter );

    parameter = new Parameter("Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  // get the parameters

    String attr_name_1 = 
               ((AttributeNameString)getParameter(0).getValue()).toString();
    boolean increasing_1 = ((Boolean)getParameter(1).getValue()).booleanValue();
    boolean use_it_1     = ((Boolean)getParameter(2).getValue()).booleanValue();

    String attr_name_2 = 
               ((AttributeNameString)getParameter(3).getValue()).toString();
    boolean increasing_2 = ((Boolean)getParameter(4).getValue()).booleanValue();
    boolean use_it_2     = ((Boolean)getParameter(5).getValue()).booleanValue();

    String attr_name_3 = 
               ((AttributeNameString)getParameter(6).getValue()).toString();
    boolean increasing_3 = ((Boolean)getParameter(7).getValue()).booleanValue();
    boolean use_it_3     = ((Boolean)getParameter(8).getValue()).booleanValue();

    boolean make_new_ds = ((Boolean)getParameter(9).getValue()).booleanValue();

    DataSet ds     = this.getDataSet();

    DataSet new_ds = ds;
    if ( make_new_ds )
      new_ds = (DataSet)ds.clone();

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

    if ( make_new_ds )
      return new_ds;
    else
    {
      new_ds.notifyIObservers( IObserver.DATA_REORDERED );
      return new String("DataSet sorted");
    }

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
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}

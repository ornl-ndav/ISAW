/*
 * @(#)DataSetMultiSort.java   0.1  99/07/28   Dennis Mikkelson
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
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public DataSetMultiSort( )
  {
    super( "Sort on multiple group attributes" );
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

/*
 * @(#)DataSetSort.java   0.1  99/07/28   Dennis Mikkelson
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
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public DataSetSort( )
  {
    super( "Sort based on Group attribute" );

    Parameter parameter = new Parameter("Group Attribute to Sort on",
                               new AttributeNameString("Raw Detector Angle") );
    addParameter( parameter );

    parameter = new Parameter("Sort in Increasing Order?", new Boolean(true) );
    addParameter( parameter );
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

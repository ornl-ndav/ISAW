/*
 * @(#)SelectData.java   0.1  99/08/05   Dennis Mikkelson
 *             
 * This operator forms a new data set by selecting Data blocks with a specified 
 * attribute in a specified range.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  *  Select Data blocks to form a new DataSet using attribute values. 
  */

public class SelectData extends    DataSetOperator 
                                   implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public SelectData( )
  {
    super( "Select based on Group attribute" );

    Parameter parameter = new Parameter("Group Attribute to use for Selection",
                               new AttributeNameString("Raw Detector Angle") );
    addParameter( parameter );

    parameter = new Parameter("Keep (or delete) selected groups?", 
                               new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter( "Lower bound", new Float(-1.0) );
    addParameter( parameter );

    parameter = new Parameter( "Upper bound", new Float(1.0) );
    addParameter( parameter );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */

                                    // This constructor actually sets values
                                    // for the parameters so that the operator
                                    // is ready to be invoked using getResult()
  public SelectData( DataSet             ds,
                     AttributeNameString attr_name,
                     boolean             keep,
                     float               min,
                     float               max   )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s)

    Parameter parameter = getParameter( 0 );
    parameter.setValue( attr_name );
    setParameter( parameter, 0 );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( keep ) );
    setParameter( parameter, 1 );

    parameter = getParameter( 2 );
    parameter.setValue( new Float( min ) );
    setParameter( parameter, 2 );

    parameter = getParameter( 3 );
    parameter.setValue( new Float( max ) );
    setParameter( parameter, 3 );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }



  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                  // get the parameters specified by the user 

    String attr_name = ((SpecialString)getParameter(0).getValue()).toString();
    boolean keep     = ((Boolean)getParameter(1).getValue()).booleanValue();

    float min = ( (Float)(getParameter(2).getValue()) ).floatValue();
    float max = ( (Float)(getParameter(3).getValue()) ).floatValue();

                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = (DataSet)ds.empty_clone(); 
    if ( keep )
      new_ds.addLog_entry( "kept groups with " + attr_name + 
                           " in [" + min + ", " + max + "]" );
    else
      new_ds.addLog_entry( "omitted groups with " + attr_name + 
                           " in [" + min + ", " + max + "]" );
                                            // do the operation
    int num_data = ds.getNum_entries();
    Data data,
         new_data;
    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );        // get reference to the data entry
                                           // keep or reject it based on the
                                           // attribute value.
      Attribute attr = data.getAttributeList().getAttribute( attr_name );
      float val = (float)attr.getNumericValue(); 
      if (attr_name == Attribute.DETECTOR_POS )        // convert to degrees
        val *= (float) 180.0/Math.PI;

      if ( keep && min <= val && val <= max  ||
          !keep && (min > val || val > max)   ) 
      {
        new_data = (Data)data.clone();
        new_ds.addData_entry( new_data );      
      } 
   
    }

    if ( new_ds.getNum_entries() <= 0 )
    {
      ErrorString message = new ErrorString(
                         "ERROR: No Data blocks satisfy the condition" );
      System.out.println( message );
      return message;
    }
    else
      return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SelectData Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    SelectData new_op    = new SelectData( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }


}

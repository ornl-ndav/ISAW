/*
 * @(#)ExtractByAttribute.java   0.2  99/08/05   Dennis Mikkelson
 *                                    99/08/16   Added constructor to allow
 *                                               calling operator directly
 *                                   2000/06/09  Renamed from SelectData
 *             
 * $Log$
 * Revision 1.2  2000/08/03 15:46:19  dennis
 * Fixed spelling error in comment
 *
 * Revision 1.1  2000/07/10 22:36:08  dennis
 * July 10, 2000 version... many changes
 *
 * Revision 1.2  2000/06/09 16:12:35  dennis
 * Added getCommand() method to return the abbreviated command string for
 * this operator
 *
 * Revision 1.1  2000/06/09 14:59:09  dennis
 * Initial revision
 *
 * Revision 1.8  2000/06/08 15:27:51  dennis
 * Changed type casting of attribute names from (SpecialString) to
 * (AttributeNameString)
 *
 * Revision 1.7  2000/05/16 19:31:21  dennis
 * fixed error in documentation caused by DOS text
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
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  * This operator forms a new data set by selecting Data blocks with a 
  * specified attribute in a specified range.
  */

public class ExtractByAttribute extends    DataSetOperator 
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

  public ExtractByAttribute( )
  {
    super( "Extract Data blocks based on Attribute" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  attr_name   The name of that attribute to be used for the 
   *                      selection criterion
   *  @param  keep        Flag that indicates whether Data blocks that meet 
   *                      the selection criteria are to be kept or removed 
   *                      from the data set.
   *  @param  min         The lower bound for the selection criteria.  The
   *                      selected Data blocks satisfy:
   *                          min <= attribute value <= max
   *  @param  max         The upper bound for the selection criteria.
   */

  public ExtractByAttribute( DataSet  ds,
                             String   attr_name,
                             boolean  keep,
                             float    min,
                             float    max   )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new AttributeNameString(attr_name) );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( keep ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float( min ) );

    parameter = getParameter( 3 );
    parameter.setValue( new Float( max ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "ExtAtt";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Group Attribute to use for Selection",
                               new AttributeNameString(Attribute.RAW_ANGLE) );
    addParameter( parameter );

    parameter = new Parameter("Keep (or delete) selected groups?",
                               new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter( "Lower bound", new Float(-1.0) );
    addParameter( parameter );

    parameter = new Parameter( "Upper bound", new Float(1.0) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                  // get the parameters specified by the user 

    String attr_name = 
           ((AttributeNameString)getParameter(0).getValue()).toString();
    boolean keep     = ((Boolean)getParameter(1).getValue()).booleanValue();

    float min = ( (Float)(getParameter(2).getValue()) ).floatValue();
    float max = ( (Float)(getParameter(3).getValue()) ).floatValue();

                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = ds.empty_clone(); 
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
   * Get a copy of the current ExtractByAttribute Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    ExtractByAttribute new_op = new ExtractByAttribute( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}

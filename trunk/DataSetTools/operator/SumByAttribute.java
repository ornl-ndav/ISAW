/*
 * @(#)SumByAttribute.java   0.4  2000/06/09   Dennis Mikkelson
 *                      
 *    99/08/16  Dennis Mikkelson
 *              Added constructor to allow calling operator directly 
 *  2000/05/09  Now returns error message if any of the Data blocks are
 *              not compatible
 *
 *  2000/06/09  This operator was renamed from SumSelectedData 
 *             
 *  $Log$
 *  Revision 1.1  2000/07/10 22:36:24  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.2  2000/06/09 16:12:35  dennis
 *  Added getCommand() method to return the abbreviated command string for
 *  this operator
 *
 *  Revision 1.1  2000/06/09 15:00:11  dennis
 *  Initial revision
 *
 *  Revision 1.8  2000/06/08 15:27:51  dennis
 *  Changed type casting of attribute names from (SpecialString) to
 *  (AttributeNameString)
 *
 *  Revision 1.7  2000/06/05 14:14:25  dennis
 *  Fixed documentation format problem.
 *
 *  Revision 1.6  2000/05/16 15:36:34  dennis
 *  Fixed clone() method to also copy the parameter values from
 *  the current operator.
 * 
 *  Revision 1.5  2000/05/11 16:41:28  dennis
 *  Added RCS logging
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  *  Sum Data blocks specified by an attribute to form a new DataSet 
  *  with one Data block.  The new data set is formed by summing 
  *  selected Data blocks with a specified attribute in a specified range.
  */

public class SumByAttribute extends    DataSetOperator 
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

  public SumByAttribute( )
  {
    super( "Sum Data blocks based on Attribute" );
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
   *                      the selection criteria are to be included in the
   *                      sum, or omitted from the sum
   *  @param  min         The lower bound for the selection criteria.  The
   *                      selected Data blocks satisfy:
   *                          min <= attribute value <= max
   *  @parm   max         The upper bound for the selection criteria.
   */

  public SumByAttribute( DataSet   ds, 
                         String    attr_name,
                         boolean   keep,
                         float     min,
                         float     max   )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s)

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
     return "SumAtt";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.  
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters
     
    Parameter parameter = new Parameter("Attribute to use for Selection",
                               new AttributeNameString(Attribute.RAW_ANGLE) );
    addParameter( parameter );

    parameter = new Parameter("Sum (or omit) selected groups?",
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
      new_ds.addLog_entry( "summed groups with " + attr_name + 
                           " in [" + min + ", " + max + "]" );
    else
      new_ds.addLog_entry( "summed groups except those with " + attr_name + 
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
    else                                 // sum up the data blocks that were 
    {                                    // selected and return

      Data sum = new_ds.getData_entry( 0 );                // get the first 
      for ( int i = 1; i < new_ds.getNum_entries(); i++ )  // and add all the 
      {                                                    // later ones to it 
        sum = sum.add( new_ds.getData_entry(i) ); 
        if ( sum == null )
        {
          ErrorString message = new ErrorString(
                         "ERROR: Data block not compatible for adding" );
          System.out.println( message );
          return message;
        } 
      }
 
      for ( int i = new_ds.getNum_entries()-1; i >= 0 ; i-- ) 
        new_ds.removeData_entry(i);                    // throw out all entries

      new_ds.addData_entry( sum );                     // put the sum in the
                                                       // data set
      return new_ds;
    }
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SumByAttribute Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    SumByAttribute new_op    = new SumByAttribute( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}

/*
 * @(#)DataSetCrossSection.java   0.2  99/08/03   Dennis Mikkelson
 *                                     99/08/16   Added constructor to allow
 *                                                calling operator directly
 *             
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.5  2000/07/10 22:35:50  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.7  2000/06/09 16:12:35  dennis
 *  Added getCommand() method to return the abbreviated command string for
 *  this operator
 *
 *  Revision 1.6  2000/06/08 15:25:59  dennis
 *  Changed type casting of attribute names from (SpecialString) to
 *  (AttributeNameString).
 *
 *  Revision 1.5  2000/05/16 15:36:34  dennis
 *  Fixed clone() method to also copy the parameter values from
 *  the current operator.
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
import  DataSetTools.math.*;

/**
  *  Form a new DataSet that contains the integrated crossection of the
  * current DataSet.
  *
  *  This operator calculates the integral over a specified interval for each
  * Data block in a DataSet and forms a new DataSet with one entry: a Data
  * block whose value at each of the original Data blocks is the value of the
  * integral for the original Data block.  The new Data block will have an
  * X-Scale taken from an attribute of one of the original Data blocks.  The
  * integral values will be ordered according to increasing attribute value.
  * If several Data blocks have the same value of the attribute, their integral
  * values are averaged.  For example, if the "Raw Detector Angle" attribute
  * is used and several Data blocks have the same angle value, the integral
  * values for that angle are averaged to form the y-value that corresponds to
  * that angle in the new Data block.
  *
  *  @see DataSetOperator
  *  @see Operator
  */

public class DataSetCrossSection extends    DataSetOperator 
                                 implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public DataSetCrossSection( )
  {
    super( "Integrated Cross Section" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  a           The left hand endpoint of the interval [a, b] over
   *                      which each Data block is integrated
   *  @param  b           The righ hand endpoint of the interval [a, b] over
   *                      which each Data block is integrated
   *  @param  attr_name   The name of that attribute to be used for ordering
   *                      the integrated Data block values
   */

  public DataSetCrossSection( DataSet  ds,
                              float    a,
                              float    b,
                              String   attr_name )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float( a ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( b ) );

    parameter = getParameter( 2 );
    parameter.setValue( new AttributeNameString(attr_name) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "CrossSect";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Left end point (a)", new Float(0));
    addParameter( parameter );

    parameter = new Parameter("Right end point (b)", new Float(0));
    addParameter( parameter );

    parameter = new Parameter(
                          "Group Attribute to Order Crossection by",
                           new AttributeNameString(Attribute.RAW_ANGLE) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
    float a = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float b = ( (Float)(getParameter(1).getValue()) ).floatValue();

    String attr_name = 
                ((AttributeNameString)getParameter(2).getValue()).toString();

                                     // get the current data set
    DataSet ds = this.getDataSet();
    int num_data = ds.getNum_entries();

    if ( num_data == 0 )
      {
        ErrorString error = new ErrorString( "ERROR: In DataSetCrossSection, " +
                                       " there are no Data groups in " + ds );
        return error;
      }
                                    // now proceed to make the new DataSet
    DataSetFactory factory = new DataSetFactory( ds.getTitle() );
    DataSet new_ds = factory.getDataSet();

    new_ds.setOp_log( ds.getOp_log() );
    new_ds.addLog_entry( "Integrated CrossSection " +
                          a + " to " + b + " " +
                          ds.getX_units() );

    new_ds.setX_label( attr_name );
    if ( attr_name == Attribute.DETECTOR_POS ||
         attr_name == Attribute.RAW_ANGLE    ||
         attr_name == Attribute.TEMPERATURE    )
      new_ds.setX_units( "Degrees" );
    else
       new_ds.setX_units( attr_name );

    new_ds.setY_label( "Integrated " + ds.getY_label() );
    new_ds.setY_units( ds.getY_units()+"*"+ds.getX_units() );

                                     // clone the DataSet and sort the clone
                                     // based on the specified attribute.
    ds = (DataSet)ds.clone();
    if ( !ds.Sort(attr_name, true) )
      {
        ErrorString message = new ErrorString(
                           "ERROR: DataSetCrossSection faiiled...no attribute:"                            + attr_name );

        System.out.println( message );
        return message;
      }

    float  integral_val[]  = new float[num_data];
    float  attribute_val[] = new float[num_data];

                                            // do the integration for each Data 
                                            // block and get the attribute val
    Data data;
    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );        // get reference to the data entry
      float x_vals[] = data.getX_scale().getXs();
      float y_vals[] = data.getY_values();

      integral_val[i] = NumericalAnalysis.IntegrateHistogram( x_vals, y_vals, 
                                                              a,      b );
      AttributeList attr_list = data.getAttributeList();
      Attribute     attr      = attr_list.getAttribute( attr_name );
      attribute_val[i]        = (float)attr.getNumericValue(); 

      if ( attr_name.equalsIgnoreCase( Attribute.DETECTOR_POS ) )
       attribute_val[i] *= 180.0f / (float)Math.PI;     // convert to degrees
    }

                                            // next combine the integral
                                            // results from groups with the 
                                            // same attribute.

    float distinct_integral_val[]  = new float[ num_data ];
    float distinct_attribute_val[] = new float[ num_data ];
    int num_distinct = 0;
    int i = 0;
    while ( i < num_data - 1 )
    {
       float total_val = integral_val[i];
       int   num_same  = 1;
       while ( i + 1 < num_data                       && 
               attribute_val[i] == attribute_val[i+1] )
       {
         total_val += integral_val[i+1];
         num_same++;
         i++;
       }
       distinct_integral_val[ num_distinct  ] = total_val/num_same;
       distinct_attribute_val[ num_distinct ] = attribute_val[i];
       num_distinct++;
       i++;
    }

                                            // finally, copy the distinct vals
                                            // into arrays of proper length 
    float x[] = new float[ num_distinct ];
    float y[] = new float[ num_distinct ];

    System.arraycopy( distinct_attribute_val, 0, x, 0, num_distinct );
    System.arraycopy( distinct_integral_val, 0, y, 0, num_distinct );

    XScale x_scale = new VariableXScale( x );
    Data new_data = new Data( x_scale, y, 0 );

    new_ds.addData_entry( new_data );      

    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetCrossSection.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    DataSetCrossSection new_op = new DataSetCrossSection( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}

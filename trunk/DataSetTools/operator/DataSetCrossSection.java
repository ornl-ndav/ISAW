/*
 * @(#)DataSetCrossSection.java   0.1  99/08/03   Dennis Mikkelson
 *             
 * This operator calculates the integral over a specified interval for each
 * Data block in a DataSet and forms a new DataSet with one entry: a Data
 * block whose value at each of the original Data blocks is the value of the
 * integral for the original Data block.  The new Data block will have an
 * X-Scale taken from an attribute of one of the original Data blocks.  The
 * integral values will be ordered according to increasing attribute value.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;

/**
  *  Form a new DataSet that contains the integrated crossection of the
  *  current DataSet.
  */

public class DataSetCrossSection extends    DataSetOperator 
                                 implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public DataSetCrossSection( )
  {
    super( "Integrated Cross Section" );

    Parameter parameter = new Parameter("Left end point (a)", new Float(0));
    addParameter( parameter );

    parameter = new Parameter("Right end point (b)", new Float(0));
    addParameter( parameter );

    parameter = new Parameter(
                          "Group Attribute to Order Crossection by",
                           new AttributeNameString("Raw Detector Angle") );
    addParameter( parameter );
}


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
    float a = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float b = ( (Float)(getParameter(1).getValue()) ).floatValue();

    String attr_name = ((SpecialString)getParameter(2).getValue()).toString();

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

    return new_op;
  }


}

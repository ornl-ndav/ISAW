/*
 * @(#)ResampleDataSet.java   0.1 2000/08/2   Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.1  2000/08/03 03:01:47  dennis
 *  Operator to resample function ( or rebin histogram ) using a set of
 *  uniformly spaced points.
 *
 *  
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;

/**
 * This operator will resample the Data blocks of a DataSet on a uniformly 
 * spaced grid.  If the Data blocks are histograms, a rebinning process is 
 * used.  If the Data blocks are tabulated functions, averaging and 
 * interpolation are used.
 */

public class ResampleDataSet extends DataSetOperator 
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

  public ResampleDataSet( )
  {
    super( "Resample" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  min_X       The left hand end point of the interval over which
   *                      the function is to be resampled 
   *  @param  max_X       The right hand end point of the interval over which
   *                      the function is to be resampled 
   *  @param  num_X       For histogram Data, this specifies the number of 
   *                      "bins" to be used between min_X and max_X.  For 
   *                      Tabulated functions, this specifies the number of
   *                      sample points to use.
   */

  public ResampleDataSet( DataSet     ds,
                          float       min_X,
                          float       max_X,
                          int         num_X )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float( min_X ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( max_X ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Integer( num_X ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "Resample";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter;

    parameter = new Parameter( "Min X", new Float(0) );
    addParameter( parameter );

    parameter = new Parameter("Max X", new Float(1000) );
    addParameter( parameter );

    parameter = new Parameter( "Num X", new Integer( 200 ) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds

    DataSet new_ds = ds.empty_clone(); 
    new_ds.addLog_entry( "Resampled" );

                                     // get the new x scale parameters 
    float min_X = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_X = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   num_X = ( (Integer)(getParameter(2).getValue()) ).intValue() + 1;

                                     // validate interval bounds
    if ( min_X > max_X )             // swap bounds to be in proper order
    {
      float temp = min_X;
      min_X = max_X;
      max_X = temp;
    }

    UniformXScale new_x_scale;
    if ( num_X < 2 || min_X >= max_X )      // no valid scale set
    {
      return new ErrorString("ERROR: invalid interval in ResampleDataSet");
    }
    else
      new_x_scale = new UniformXScale( min_X, max_X, num_X );  

                                            // now proceed with the operation 
                                            // on each data block in DataSet 
    Data             data,
                     new_data;
    int              num_data = ds.getNum_entries();
    AttributeList    attr_list;

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );        // get reference to the data entry
      new_data = (Data)data.clone();

      attr_list = data.getAttributeList();
      new_data.setAttributeList( attr_list ); // copy the attributes

      new_data.ResampleUniformly( new_x_scale );
      new_ds.addData_entry( new_data );      
    }

    return new_ds;
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current ResampleDataSet Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    ResampleDataSet new_op = new ResampleDataSet( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}

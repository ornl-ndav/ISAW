/*
 * @(#)ResampleDataSet.java   0.1 2000/08/2   Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.3  2000/08/03 14:35:25  dennis
 *  made more efficient by removing redundant copy of attributes and only
 *  cloning Data blocks when a new DataSet is to be created.
 *
 *  Revision 1.2  2000/08/03 14:21:58  dennis
 *  Now includes parameter "make_new_ds" to determine whether or not to
 *  construct a new DataSet
 *
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
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the Data blocks of the original
   *                      DataSet are just altered.
   */

  public ResampleDataSet( DataSet     ds,
                          float       min_X,
                          float       max_X,
                          int         num_X,
                          boolean     make_new_ds )
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

    parameter = getParameter( 3 );
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

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();

                                     // get the new x scale parameters
    float min_X         = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_X         = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   num_X         = ( (Integer)(getParameter(2).getValue()) ).intValue();
    boolean make_new_ds = ((Boolean)getParameter(3).getValue()).booleanValue();

                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = null;
    if ( make_new_ds )
      new_ds = ds.empty_clone();
    else
      new_ds = ds;

    new_ds.addLog_entry( "Resampled" );

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

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );        // get reference to the data entry

      if ( make_new_ds )
      {
        new_data = (Data)data.clone();
        new_data.ResampleUniformly( new_x_scale );
        new_ds.addData_entry( new_data );
      }
      else
        data.ResampleUniformly( new_x_scale );
    }

    if ( make_new_ds )
      return new_ds;
    else
    {
      ds.notifyIObservers( IObserver.DATA_CHANGED );
      return new String( "Data resampled uniformly" );
    }

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

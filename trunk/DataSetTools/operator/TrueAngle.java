/*
 * @(#)TrueAngle.java   0.1  2000/06/01   Dennis Mikkelson
 *             
 * This operator resamples a DataSet containing spectra from detectors at
 * a variety of different angles to a set of spectra at a uniform set of angles.
 */

/*
 * $LOG:$
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;

/**
  *  Resample  a DataSet containing spectra from detectors at a variety
  *  of different angles to a set of spectra at a uniform set of angles. 
  *  The Data blocks will first be rebinned into x-bins with the same 
  *  bin boundaries.  Next, for each x-bin, the data values from all 
  *  detectors at that x-bin, will be merged into a set of equal angle bins,
  *  using the estimated angular coverage of each detector.  This will 
  *  produce a two-dimensional set of values which will be cut into rows
  *  to produce a new DataSet.
  *
  */

public class TrueAngle extends    DataSetOperator 
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

  public TrueAngle( ) 
  {
    super( "Convert to True Angle DataSet" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  min_angle   The minimum angle to include
   *  @param  max_angle   The maximum angle to include 
   *  @param  n_bins      The number of "bins" to be used between min_angle and
   *                      max_angle.
   */

  public TrueAngle( DataSet     ds,
                    float       min_angle,
                    float       max_angle,
                    int         n_bins )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter(0);
    parameter.setValue( new Float(min_angle) );
    
    parameter = getParameter(1); 
    parameter.setValue( new Float(max_angle) );
    
    parameter = getParameter(2);
    parameter.setValue( new Integer(n_bins) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "ToYAngle";
   }



 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Min Angle", new Float(-180) );
    addParameter( parameter );

    parameter = new Parameter( "Max Angle", new Float(180) );
    addParameter( parameter );

    parameter = new Parameter( Parameter.NUM_BINS, new Integer(180));
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
    new_ds.addLog_entry( "Converted to true angle" );

    // copy the attributes of the original data set
    new_ds.setAttributeList( ds.getAttributeList() );

                                     // get the angle scale parameters
    float min_angle = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_angle = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   n_bins    = ( (Integer)(getParameter(2).getValue()) ).intValue();

                                     // validate angular bounds
    if ( min_angle > max_angle )     // swap bounds to be in proper order
    {
      float temp = min_angle;
      min_angle  = max_angle;
      max_angle  = temp;
    }

    if ( n_bins <= 0 )                               // calculate the default 
      n_bins = (int)(max_angle - min_angle);         // number of angle bins.

    UniformXScale angle_scale = null;
    if ( n_bins <= 0 || min_angle >= max_angle )   // no valid range specified 
    {
      ErrorString message = new ErrorString(
                      "ERROR: no valid angle range in TrueAngle operator");
      return message;
    }
    else
     angle_scale = new UniformXScale( min_angle, max_angle, n_bins + 1 );

                                                // get common XScale for Data 
    int num_cols = ds.getMaxXSteps();       
    UniformXScale x_scale = ds.getXRange();
    x_scale = new UniformXScale( x_scale.getStart_x(), 
                                 x_scale.getEnd_x(),
                                 num_cols + 1 );

                                                // place all y_values from all
                                                // spectra in one 2D array
                                                // also, record min & max angle
                                                // values corresponding to the
                                                // spectra.  The 2D array will 
                                                // be resampled to form the
                                                // true angle image "in place"
                                                // so it must be large enough.  
    int num_data = ds.getNum_entries();
    float y_vals[][];
    if ( num_data > n_bins )
      y_vals = new float[num_data][] ; 
    else
      y_vals = new float[n_bins][] ; 

    float min_ang[]  = new float[num_data];
    float max_ang[]  = new float[num_data];
    Data             data,
                     rebinned_data;
    AttributeList    attr_list;
    DetectorPosition position = null;
    float            scattering_angle; 
    Float            delta_theta_obj = null;
    float            delta_theta;
    Float            raw_angle_obj = null;
    float            raw_angle;
    for ( int i = 0; i < num_data; i++ )
    {
      data          = ds.getData_entry(i);
      rebinned_data = (Data)data.clone();
      rebinned_data.ReBin( x_scale );
      y_vals[i]     = rebinned_data.getY_values();

      attr_list = data.getAttributeList();
      position = (DetectorPosition)
                  attr_list.getAttributeValue( Attribute.DETECTOR_POS);
      if ( position == null )
      {
        ErrorString message = new ErrorString(
                    "ERROR: no DETECTOR_POS attribute in TrueAngle operator");
        return message;
      }
     
      delta_theta_obj = (Float)
                        attr_list.getAttributeValue( Attribute.DELTA_2THETA );
      if ( delta_theta_obj == null )
      {
        ErrorString message = new ErrorString(
                    "ERROR: no DELTA_2THETA attribute in TrueAngle operator");
        return message;
      }

      raw_angle_obj = (Float)
                        attr_list.getAttributeValue( Attribute.RAW_ANGLE );
      if ( raw_angle_obj == null )
      {
        ErrorString message = new ErrorString(
                    "ERROR: no RAW_ANGLE attribute in TrueAngle operator");
        return message;
      }

      scattering_angle = position.getScatteringAngle() * (float)(180.0/Math.PI);
      delta_theta = delta_theta_obj.floatValue();
      raw_angle = raw_angle_obj.floatValue();

//      min_ang[i] = scattering_angle - delta_theta/2; 
//      max_ang[i] = scattering_angle + delta_theta/2; 
      min_ang[i] = raw_angle - delta_theta/2; 
      max_ang[i] = raw_angle + delta_theta/2; 
    }    

    for ( int i = num_data; i < n_bins; i++ )
      y_vals[i] = new float[num_cols];
                                                // now resample the columns
                                                // of the 2D array
    float resampled_col[] = new float[n_bins];
    float zero_array[]    = new float[n_bins];
    for ( int row = 0; row < n_bins; row++ )
      zero_array[row] = 0.0f;

    for ( int col = 0; col < y_vals[0].length; col++ )    
    {
      System.arraycopy( zero_array, 0, resampled_col, 0, n_bins );
      for ( int row = 0; row < num_data; row++ )
        Sample.ResampleBin( min_ang[row], max_ang[row], y_vals[row][col],
                              min_angle, max_angle,  resampled_col );  

      for ( int row = 0; row < n_bins; row++ )
        y_vals[row][col] = resampled_col[row];        
    }

    Data new_data;
    boolean all_zero;
    int col;
    UniformXScale two_point_xscale = new UniformXScale( x_scale.getStart_x(), 
                                                        x_scale.getEnd_x(),
                                                        2 );
    float one_point_y[] = new float[1];
    one_point_y[0] = 0;
    for ( int row = 0; row < n_bins; row++ )
    {
      all_zero = true;
      col = 0;
      while ( all_zero && col < y_vals[0].length )
      {
        if ( y_vals[row][col] != 0 )
          all_zero = false;
        col++;
      }  

      if ( all_zero )
        new_data = new Data( two_point_xscale, one_point_y, row+1 );
      else
        new_data = new Data( x_scale, y_vals[row], row+1 );

      new_ds.addData_entry( new_data );      
    }

    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current TofToChannel Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    TrueAngle new_op = new TrueAngle( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }

}

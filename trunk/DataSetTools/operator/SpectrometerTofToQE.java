/*
 * @(#)SpectrometerTofToQE.java   0.2  2000/06/01   Dennis Mikkelson
 *             
 * This operator converts an energy spectrum from a Spectrometer into a 
 * DataSet containing the rows of a QE image.
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
  *  Convert an energy spectrum from a Spectrometer into a DataSet containing
  *  the rows of a QE image. The Data blocks will first be rebinned into 
  *  energy bins with the same bin boundaries.  Next, for each energy-bin, 
  *  the data values from all detectors at that energy-bin, will be merged 
  *  into a set of equal Q bins, using the estimated Q coverage of each 
  *  detector.  This will produce a two-dimensional set of values which will 
  *  be cut into rows to produce a new DataSet.
  *
  */

public class SpectrometerTofToQE extends    DataSetOperator 
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

  public SpectrometerTofToQE( ) 
  {
    super( "Convert to QE plot" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  min_Q       The minimum Q to include
   *  @param  max_Q       The maximum Q to include 
   *  @param  n_bins      The number of "bins" to be used between min_Q and
   *                      max_Q.
   */

  public SpectrometerTofToQE( DataSet     ds,
                              float       min_Q,
                              float       max_Q,
                              int         n_bins )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter(0);
    parameter.setValue( new Float(min_Q) );
    
    parameter = getParameter(1); 
    parameter.setValue( new Float(max_Q));
    
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
     return "ToQE";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Min Q", new Float(0.0f) );
    addParameter( parameter );

    parameter = new Parameter( "Max Q", new Float(20.0f) );
    addParameter( parameter );

    parameter = new Parameter( Parameter.NUM_BINS, new Integer(200));
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

    DataSetOperator op = ds.getOperator( "Convert to Energy" );
    op.setDefaultParameters();
    DataSet new_ds = (DataSet)op.getResult();
    new_ds.addLog_entry( "Converted to Q vs. E" );

    // copy the attributes of the original data set
    new_ds.setAttributeList( ds.getAttributeList() );

                                     // get the Q scale parameters
    float min_Q  = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_Q  = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   n_bins = ( (Integer)(getParameter(2).getValue()) ).intValue();

                                     // validate angular bounds
    if ( min_Q > max_Q )             // swap bounds to be in proper order
    {
      float temp = min_Q;
      min_Q  = max_Q;
      max_Q  = temp;
    }

    if ( n_bins <= 0 )                               // calculate a default 
      n_bins = 200;                                  // number of Q bins.

    UniformXScale Q_scale = null;
    if ( min_Q >= max_Q )                       // no valid range specified 
    {
      ErrorString message = new ErrorString(
                      "ERROR: no valid Q range in QE operator");
      return message;
    }
    else
     Q_scale = new UniformXScale( min_Q, max_Q, n_bins + 1 );

                                                // get common XScale for Data 
    int num_xsteps = new_ds.getMaxXSteps();       
    UniformXScale x_scale = new_ds.getXRange();
    x_scale = new UniformXScale( x_scale.getStart_x(), 
                                 x_scale.getEnd_x(),
                                 num_xsteps );

    int num_cols = num_xsteps - 1;

                                                // place all y_values from all
                                                // spectra in one 2D array
                                                // also, record min & max Q 
                                                // values corresponding to the
                                                // spectra.  The 2D array will 
                                                // be resampled to form the
                                                // QE image "in place"
                                                // so it must be large enough.  
    int num_data = new_ds.getNum_entries();
    float y_vals[][];
    if ( num_data > n_bins )
      y_vals = new float[num_data][] ; 
    else
      y_vals = new float[n_bins][] ; 

    float min_ang[]  = new float[num_data];
    float max_ang[]  = new float[num_data];
    float e_in[]     = new float[num_data];
    Data             data;
    AttributeList    attr_list;
    DetectorPosition position = null;
    float            scattering_angle; 
    Float            delta_theta_obj = null;
    float            delta_theta;
    Float            energy_in_obj = null;

    for ( int i = 0; i < num_data; i++ )
    {
      data          = new_ds.getData_entry(i);
      y_vals[i]     = data.getY_values();

      attr_list = data.getAttributeList();
      position = (DetectorPosition)
                  attr_list.getAttributeValue( Attribute.DETECTOR_POS);
      if ( position == null )
      {
        ErrorString message = new ErrorString(
                    "ERROR: no DETECTOR_POS attribute in QE operator");
        return message;
      }
     
      delta_theta_obj = (Float)
                        attr_list.getAttributeValue( Attribute.DELTA_2THETA );
      if ( delta_theta_obj == null )
      {
        ErrorString message = new ErrorString(
                    "ERROR: no DELTA_2THETA attribute in QE operator");
        return message;
      }

      energy_in_obj = (Float)attr_list.getAttributeValue(Attribute.ENERGY_IN);
      e_in[i]       = energy_in_obj.floatValue();

      scattering_angle = position.getScatteringAngle() * (float)(180.0/Math.PI);
      delta_theta = delta_theta_obj.floatValue();

      min_ang[i] = (float)(Math.PI/180.0) * (scattering_angle - delta_theta/2); 
      max_ang[i] = (float)(Math.PI/180.0) * (scattering_angle + delta_theta/2); 
    }    

    for ( int i = num_data; i < n_bins; i++ )   // allocate the rest of the
      y_vals[i] = new float[num_cols];          // array, if needed.

    for ( int row = num_data-1; row >= 0; row-- )  // clear out the new DataSet
      new_ds.removeData_entry( row );

                                                // now resample the columns
                                                // of the 2D array
    float resampled_col[] = new float[n_bins];
    float zero_array[]    = new float[n_bins];
    for ( int row = 0; row < n_bins; row++ )
      zero_array[row] = 0.0f;

    float e_vals[] = x_scale.getXs();
    float q1, q2;

    for ( int col = 0; col < y_vals[0].length; col++ )    
    {
      System.arraycopy( zero_array, 0, resampled_col, 0, n_bins );
      for ( int row = 0; row < num_data; row++ )
      {
        q1 = tof_calc.SpectrometerQ( e_in[row], 
                                   ( e_vals[col] + e_vals[col+1] ) / 2, 
                                     min_ang[row] );
        q2 = tof_calc.SpectrometerQ( e_in[row], 
                                   ( e_vals[col] + e_vals[col+1] ) / 2, 
                                     max_ang[row] );
        if ( q1 > q2 )
        {
          float temp = q1;
          q1 = q2;
          q2 = temp;
        }

        Sample.ResampleBin( q1, q2, y_vals[row][col],
                            min_Q, max_Q,  resampled_col );  
      }
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
    SpectrometerTofToQE new_op = new SpectrometerTofToQE( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }

}

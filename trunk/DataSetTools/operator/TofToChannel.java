/*
 * @(#)TofToChannel.java   0.2  2000/05/25   Dongfeng, Alok and Dennis
 *             
 * This operator converts neutron time-of-flight DataSet to channel.  The
 * DataSet must contain spectra with an attribute giving the detector position.
 * In addition, it is assumed that the XScale for the spectra represents the
 * time-of-flight from the sample to the detector. 
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
  *  Convert a neutron time-of-flight DataSet to Channel. 
  */

public class TofToChannel extends  XAxisConversionOperator 
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

  public TofToChannel( ) 
  {
    super( "Convert to Channel" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  min_chan    The minimum channel number to be binned
   *  @param  max_chan    The maximum channel number to be binned
   *  @param  n_bins      The number of "bins" to be used between min_chan and
   *                      max_chan.  That is, the data values between the
   *                      min and max channels will be rebinned into the 
   *                      specified number of bins.  If num_bins <= 0, the data 
   *                      will not be rebinned, but only data values between 
   *                      the specified min and max channels will be kept.
   */

  public TofToChannel( DataSet     ds,
                       float       min_chan,
                       float       max_chan,
                       int         n_bins )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter(0); 
    parameter.setValue( new Float(min_chan) );
    
    parameter = getParameter(1); 
    parameter.setValue( new Float(max_chan) );
    
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
     return "ToChan";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    UniformXScale scale = getXRange();
    float min_chan,
          max_chan;

    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter;

    if ( scale == null )
    {
      min_chan = 0;
      max_chan = 1000;
    }
    else
    {
      min_chan = scale.getStart_x();
      max_chan = scale.getEnd_x();
    }

    parameter = new Parameter( "Min Channel Number", new Float(min_chan) );
    addParameter( parameter );

    parameter = new Parameter( "Max Channel Number", new Float(max_chan));
    addParameter( parameter );

    parameter = new Parameter( Parameter.NUM_BINS, new Integer(0) );
    addParameter( parameter );
  }


  /* -------------------------- new_X_label ---------------------------- */
  /**
   * Get string label for converted x values.
   *
   *  @return  String describing the x label and units for converted x values.
   */
   public String new_X_label()
   {
     return new String( "Channel" );
   }


  /* ---------------------- convert_X_Value ------------------------------- */
  /**
   * Evaluate the axis conversion function at one point only.
   *
   *  @param  x    the x-value where the axis conversion function is to be
   *               evaluated.
   *
   *  @param  i    the index of the Data block for which the axis conversion
   *               function is to be evaluated.
   *
   *  @return  the value of the axis conversion function at the specified x.
   */
  public float convert_X_Value( float x, int i )
  {
    float  channel;

    DataSet ds = this.getDataSet();          // make sure we have a DataSet
    if ( ds == null )
      return Float.NaN;

    int num_data = ds.getNum_entries();      // make sure we have a valid Data
    if ( i < 0 || i >= num_data )            // index
      return Float.NaN;

    Data   data  = ds.getData_entry( i );
    XScale scale = data.getX_scale();
    float  min_x = scale.getStart_x();
    float  max_x = scale.getEnd_x();
    int    num_x = scale.getNum_x();

    if ( num_x < 2 || x < min_x || x > max_x )
      return Float.NaN;

    channel = (x - min_x) / (max_x - min_x) * num_x;
    channel = (int)( channel );
   
    return channel;
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, but new units, and operations.
    DataSetFactory factory = new DataSetFactory( 
                                     ds.getTitle(),
                                     "Channel",
                                     "Number",
                                     "Counts",
                                     "Scattering Intensity" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet(); 
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to Channel Number" );

    // copy the attributes of the original data set
    new_ds.setAttributeList( ds.getAttributeList() );

                                     // get the channel scale parameters
    float min_chan = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_chan = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   num_chan = ( (Integer)(getParameter(2).getValue()) ).intValue();

    min_chan = Math.round( min_chan );
    max_chan = Math.round( max_chan );
                                     // validate channel bounds
    if ( min_chan > max_chan )       // swap bounds to be in proper order
    {
      float temp = min_chan;
      min_chan = max_chan;
      max_chan = temp;
    }

    UniformXScale new_channel_scale;
    if ( num_chan <= 0 )                             // calculate the default
      num_chan = (int)(max_chan-min_chan+1);         // number of channels.


    if ( num_chan <= 1.0 || min_chan >= max_chan )   // no valid scale set
      new_channel_scale = null;
    else
      new_channel_scale = new UniformXScale( min_chan, 
                                             max_chan, 
                                             num_chan );

                                            // now proceed with the operation 
                                            // on each data block in DataSet 
    Data             data,
                     new_data;
    float            y_vals[];               // y_values from one spectrum
    float            errors[];               // errors from one spectrum
    XScale           channel_scale;
    int              num_data = ds.getNum_entries();
    AttributeList    attr_list;

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );         // get reference to the data entry

      attr_list = data.getAttributeList();  // copy the Data attributes

      y_vals = data.getY_values();
      errors = data.getErrors();
      channel_scale = new UniformXScale( 0,  
                                         y_vals.length,  
                                         y_vals.length + 1 );

                                                 // create new data block with 
                                                 // time-channel XScale and 
                                                 // the original y_vals.
      new_data = new Data( channel_scale, y_vals, errors, data.getGroup_ID() );
      new_data.setAttributeList( attr_list );  

      if ( new_channel_scale != null )                    // resample if a valid
          new_data.ResampleUniformly( new_channel_scale );// scale was specified

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
    TofToChannel new_op = new TofToChannel( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }

}

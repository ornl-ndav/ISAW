/*
 * @(#)DiffractometerTofToQ.java   0.2  99/06/16   Dennis Mikkelson
 *                                      99/08/16   Added constructor to allow
 *                                                 calling operator directly
 *             
 * This operator converts a neutron time-of-flight DataSet to "Q".  The
 * DataSet must contain spectra with attributes giving the detector position
 * and source to sample distance ( the initial flight path ). In addition, 
 * it is assumed that the XScale for the spectra represents the time-of-flight 
 * from the source to the detector.
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;

/**
  *  Convert a neutron time-of-flight DataSet to Q. 
  */

public class DiffractometerTofToQ extends    DataSetOperator 
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

  public DiffractometerTofToQ( )
  {
    super( "Convert to Q" );
    Parameter parameter;

    parameter = new Parameter( "Min Q(inv A)", new Float(0.0) );
    addParameter( parameter );

    parameter = new Parameter( "Max Q(inv A)", new Float(20.0) );
    addParameter( parameter );

    parameter = new Parameter( "Number of Bins ", new Integer(1000) );
    addParameter( parameter );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  min_Q       The minimum Q value to be binned
   *  @param  max_Q       The maximum Q value to be binned
   *  @param  num_Q       The number of "bins" to be used between min_Q and
   *                      max_Q
   */

  public DiffractometerTofToQ( DataSet     ds,
                               float       min_Q,
                               float       max_Q,
                               int         num_Q )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float( min_Q ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( max_Q ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Integer( num_Q ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }



  /* ---------------------------- getResult ------------------------------- */

                                     // The concrete operation extracts the
                                     // current value of the scalar to add 
                                     // and returns the result of adding it
                                     // to each point in each data block.
  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, as the current DataSet, ds.
    DataSetFactory factory = new DataSetFactory( 
                                     ds.getTitle(),
                                     "Inverse Angstroms",
                                     "Q",
                                     "counts",
                                     "Scattering Intensity" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet(); 
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to Q" );

    // copy the attributes of the original data set
    new_ds.getAttributeList().addAttributes( ds.getAttributeList() );
                                     

                                     // get the Q scale parameters 
    float min_Q = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_Q = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   num_Q = ( (Integer)(getParameter(2).getValue()) ).intValue() + 1;

                                     // validate Q bounds
    if ( min_Q > max_Q )             // swap bounds to be in proper order
    {
      float temp = min_Q;
      min_Q = max_Q;
      max_Q = temp;
    }

    XScale new_Q_scale;
    if ( num_Q <= 1.0 || min_Q >= max_Q )       // no valid scale set
      new_Q_scale = null;
    else
      new_Q_scale = new UniformXScale( min_Q, max_Q, num_Q );  

                                            // now proceed with the operation 
                                            // on each data block in DataSet 
    Data             data,
                     new_data;
    DetectorPosition position;
    float            initial_path;
    Float            initial_path_obj;
    float            total_length;
    float            scattering_angle;
    float            y_vals[];              // y_values from one spectrum
    float            Q_vals[];              // Q values at bin boundaries
                                            // calculated from tof bin bounds
    XScale           Q_scale;
    float            spherical_coords[];
    int              num_data = ds.getNum_entries();
    AttributeList    attr_list;

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );        // get reference to the data entry
      attr_list = data.getAttributeList();
                                           // get the detector position and
                                           // initial path length 
      position=(DetectorPosition)
                   attr_list.getAttributeValue(Attribute.DETECTOR_POS);
      initial_path_obj=(Float)
                          attr_list.getAttributeValue(Attribute.INITIAL_PATH);

      if( position != null && initial_path_obj != null)
                                                       // has needed attributes 
      {                                                // so convert it to E
                                       // calculate d-values at bin boundaries
        initial_path     = initial_path_obj.floatValue();
        spherical_coords = position.getSphericalCoords();
        total_length     = initial_path + spherical_coords[0];
        scattering_angle = position.getScatteringAngle();
 
        Q_vals           = data.getX_scale().getXs();
        for ( int i = 0; i < Q_vals.length; i++ )
          Q_vals[i] = tof_calc.DiffractometerQ( scattering_angle,
                                                total_length, 
                                                Q_vals[i]        );
        arrayUtil.Reverse( Q_vals );
        Q_scale = new VariableXScale( Q_vals );

        y_vals  = data.getCopyOfY_values();
        arrayUtil.Reverse( y_vals );

        new_data = new Data( Q_scale, y_vals, data.getGroup_ID() );
                                                // create new data block with 
        new_data.setSqrtErrors();               // non-uniform Q_scale and 
                                                // the original y_vals.
        new_data.setAttributeList( attr_list ); // copy the attributes

        if ( new_Q_scale != null )              // rebin if a valid scale was
          new_data.ReBin( new_Q_scale );        // specified

        new_ds.addData_entry( new_data );      
        new_ds.setAttributeList( attr_list ); // copy the attributes
      }
    }

    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DiffractometerTofToQ Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    DiffractometerTofToQ new_op = new DiffractometerTofToQ( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }



}

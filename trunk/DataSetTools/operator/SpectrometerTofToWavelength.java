/*
 * @(#)SpectrometerTofToWavelength.java   0.2  99/06/17   Dennis Mikkelson
 *
 *                                 99/08/16   Added constructor to allow
 *                                            calling operator directly
 *             
 * This operator converts neutron time-of-flight DataSet for a Spectrometer
 * to wavelength.  The DataSet must contain spectra with an attribute giving 
 * the detector position. In addition, it is assumed that the XScale for the 
 * spectra represents the time-of-flight from the SAMPLE to the detector. 
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;

/**
  *  Convert a neutron time-of-flight DataSet to wavelength. 
  */

public class SpectrometerTofToWavelength extends    DataSetOperator 
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

  public SpectrometerTofToWavelength( )
  {
    super( "Convert to Wavelength" );
    Parameter parameter;

    parameter = new Parameter( "Min Wavelength(A)", new Float(0.0) );
    addParameter( parameter );

    parameter = new Parameter( "Max Wavelength(A)", new Float(5.0) );
    addParameter( parameter );

    parameter = new Parameter( "Number of Bins ", new Integer( 500 ) );
    addParameter( parameter );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  min_wl      The minimum wavelength value to be binned
   *  @param  max_wl      The maximum wavelength value to be binned
   *  @param  num_wl      The number of "bins" to be used between min_wl and
   *                      max_wl
   */

  public SpectrometerTofToWavelength( DataSet     ds,
                                      float       min_wl,
                                      float       max_wl,
                                      int         num_wl )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float( min_wl ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( max_wl ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Integer( num_wl ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSetFactory factory = new DataSetFactory( 
                                     ds.getTitle(),
                                     "Angstroms",
                                     "Wavelength",
                                     "counts",
                                     "Scattering Intensity" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet(); 
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to Wavelength" );

    // copy the attributes of the original data set
    new_ds.getAttributeList().addAttributes( ds.getAttributeList() );

                                     // get the wavelength scale parameters 
    float min_wl = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_wl = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   num_wl = ( (Integer)(getParameter(2).getValue()) ).intValue() + 1;

                                     // validate wavelength bounds
    if ( min_wl > max_wl )             // swap bounds to be in proper order
    {
      float temp = min_wl;
      min_wl = max_wl;
      max_wl = temp;
    }

    XScale new_wl_scale;
    if ( num_wl < 2 || min_wl >= max_wl )      // no valid scale set
      new_wl_scale = null;
    else
      new_wl_scale = new UniformXScale( min_wl, max_wl, num_wl );  

                                            // now proceed with the operation 
                                            // on each data block in DataSet 
    Data             data,
                     new_data;
    DetectorPosition position;
    float            y_vals[];            // y_values from one spectrum
    float            wl_vals[];           // wavelength values at bin boundaries
                                          // calculated from tof bin bounds
    XScale           wl_scale;
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

      if( position != null )           // has needed attributes so convert it 
                                       // to wavelength
      { 
                                       // calculate wavelength at bin boundaries
        spherical_coords = position.getSphericalCoords();
        wl_vals          = data.getX_scale().getXs();
        for ( int i = 0; i < wl_vals.length; i++ )
          wl_vals[i] = tof_calc.Wavelength( spherical_coords[0], wl_vals[i] );
  
        wl_scale = new VariableXScale( wl_vals );

        y_vals = data.getCopyOfY_values();

        new_data = new Data( wl_scale, y_vals, data.getGroup_ID() );
                                                 // create new data block with 
        new_data.setSqrtErrors();                // non-uniform E_scale and 
                                                 // the original y_vals.
        new_data.setAttributeList( attr_list );  // copy the attributes

        if ( new_wl_scale != null )              // rebin if a valid scale was
          new_data.ReBin( new_wl_scale );        // specified

        new_ds.addData_entry( new_data );      
      }
    }

    return new_ds;
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SpectrometerTofToWavelength Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SpectrometerTofToWavelength new_op = new SpectrometerTofToWavelength( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }

}

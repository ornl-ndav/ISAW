/*
 * @(#)DiffractometerTofToWavelength.java   0.1  99/06/17   Dennis Mikkelson
 *             
 * This operator converts a neutron time-of-flight DataSet to wavelength.  The
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
  *  Convert a neutron time-of-flight DataSet to wavelength. 
  */

public class DiffractometerTofToWavelength extends    DataSetOperator 
                                           implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public DiffractometerTofToWavelength( )
  {
    super( "Convert to Wavelength" );
    Parameter parameter;

    parameter = new Parameter( "Min Wavelength(A)", new Float(0.0) );
    addParameter( parameter );

    parameter = new Parameter( "Max Wavelength(A)", new Float(10.0) );
    addParameter( parameter );

    parameter = new Parameter( "Number of Bins ", new Float(200.0) );
    addParameter( parameter );
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
    int   num_wl = ( (Float)(getParameter(2).getValue()) ).intValue() + 1;

                                     // validate wavelength bounds
    if ( min_wl > max_wl )             // swap bounds to be in proper order
    {
      float temp = min_wl;
      min_wl = max_wl;
      max_wl = temp;
    }

    XScale new_wl_scale;
    if ( num_wl <= 1.0 || min_wl >= max_wl )       // no valid scale set
      new_wl_scale = null;
    else
      new_wl_scale = new UniformXScale( min_wl, max_wl, num_wl );  

                                            // now proceed with the operation 
                                            // on each data block in DataSet 
    Data             data,
                     new_data;
    DetectorPosition position;
    float            initial_path;
    Float            initial_path_obj;
    float            total_length;
    float            y_vals[];           // y_values from one spectrum
    float            wl_vals[];          // wavelength values at bin boundaries
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

      initial_path_obj=(Float)
                         attr_list.getAttributeValue(Attribute.INITIAL_PATH);

      if( position != null && initial_path_obj != null)
                                                       // has needed attributes 
      {                                                // so convert it to E
                                       // calculate wavelengths at bin boundaries
        initial_path     = initial_path_obj.floatValue();
        spherical_coords = position.getSphericalCoords();
        total_length     = initial_path + spherical_coords[0];
 
        wl_vals           = data.getX_scale().getXs();
        for ( int i = 0; i < wl_vals.length; i++ )
          wl_vals[i] = tof_calc.Wavelength( total_length, wl_vals[i] );
  
        wl_scale = new VariableXScale( wl_vals );

        y_vals  = data.getCopyOfY_values();

        new_data = new Data( wl_scale, y_vals, data.getGroup_ID() );
                                                // create new data block with 
        new_data.setSqrtErrors();               // non-uniform E_scale and 
                                                // the original y_vals.
        new_data.setAttributeList( attr_list ); // copy the attributes

        if ( new_wl_scale != null )             // rebin if a valid scale was
          new_data.ReBin( new_wl_scale );       // specified

        new_ds.addData_entry( new_data );      
      }
    }

    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DDiffractometerTofToWavelength Operator.  The 
   * list of * parameters and the reference to the DataSet to which it applies 
   * are also copied.
   */
  public Object clone()
  {
    DiffractometerTofToWavelength new_op = new DiffractometerTofToWavelength( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }


}

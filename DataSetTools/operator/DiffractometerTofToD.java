/*
 * @(#)DiffractometerTofToD.java   0.1  99/06/16   Dennis Mikkelson
 *             
 * This operator converts a neutron time-of-flight DataSet to D-spacing.  The
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
  *  Convert a neutron time-of-flight DataSet to d-Spacing.. 
  */

public class DiffractometerTofToD extends    DataSetOperator 
                                  implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public DiffractometerTofToD( )
  {
    super( "Convert to d-Spacing" );
    Parameter parameter;

    parameter = new Parameter( "Min d(A)", new Float(0.0) );
    addParameter( parameter );

    parameter = new Parameter( "Max d(A)", new Float(5.0) );
    addParameter( parameter );

    parameter = new Parameter( "Number of Bins ", new Float(1000.0) );
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
                                     // title, as the current DataSet, ds.
    DataSetFactory factory = new DataSetFactory( 
                                     ds.getTitle(),
                                     "Angstroms",
                                     "d-Spacing",
                                     "counts",
                                     "Scattering Intensity" );

    DataSet new_ds = factory.getDataSet(); 

    // #### must take care of the operation log... this starts with it empty
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to d-Spacing" );

    // copy the attributes of the original data set
    new_ds.getAttributeList().addAttributes( ds.getAttributeList() );  

                                     // get the d-Spacing scale parameters 
    float min_D = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_D = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   num_D = ( (Float)(getParameter(2).getValue()) ).intValue() + 1;

                                     // validate d-Spacing bounds
    if ( min_D > max_D )             // swap bounds to be in proper order
    {
      float temp = min_D;
      min_D = max_D;
      max_D = temp;
    }

    XScale new_d_scale;
    if ( num_D <= 1.0 || min_D >= max_D )       // no valid scale set
      new_d_scale = null;
    else
      new_d_scale = new UniformXScale( min_D, max_D, num_D );  

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
    float            d_vals[];              // d values at bin boundaries
                                            // calculated from tof bin bounds
    XScale           D_scale;
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
                       attr_list.getAttributeValue( Attribute.DETECTOR_POS);

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
 
        d_vals           = data.getX_scale().getXs();
        for ( int i = 0; i < d_vals.length; i++ )
          d_vals[i] = tof_calc.DSpacing( scattering_angle,
                                         total_length, 
                                         d_vals[i]        );
  
        D_scale = new VariableXScale( d_vals );

        y_vals  = data.getCopyOfY_values();

        new_data = new Data( D_scale, y_vals, data.getGroup_ID() );
                                               // create new data block with 
        new_data.setSqrtErrors();               // non-uniform E_scale and 
                                                // the original y_vals.
        new_data.setAttributeList( attr_list ); // copy the attributes

        if ( new_d_scale != null )              // rebin if a valid scale was
          new_data.ReBin( new_d_scale );        // specified

        new_ds.addData_entry( new_data );      
      }
    }

    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DiffractometerTofToD Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    DiffractometerTofToD new_op    = new DiffractometerTofToD( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }

}

/*
 * @(#)SpectrometerTofToEnergyLoss.java   0.1  99/06/15   Dennis Mikkelson
 *             
 * This operator converts a neutron time-of-flight DataSet to energy.  The
 * DataSet must contain spectra with attributes giving the detector position
 * and initial energy. In addition, it is assumed that the XScale for the 
 * spectra represents the time-of-flight from the sample to the detector.
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  graph.*;

/**
  *  Convert a neutron time-of-flight DataSet to energy loss.. 
  */

public class SpectrometerTofToEnergyLoss extends    DataSetOperator 
                                         implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public SpectrometerTofToEnergyLoss( )
  {
    super( "Convert to Energy Loss" );
    Parameter parameter;

    parameter = new Parameter( "Min Energy Loss(meV)", new Float(-50.0) );
    addParameter( parameter );

    parameter = new Parameter( "Max Energy Loss(meV)", new Float(50.0) );
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
     DataSetTools.dataset.DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSetFactory factory = new DataSetFactory( 
                                     ds.getTitle(),
                                     "meV",
                                     "EnergyLoss",
                                     "counts",
                                     "Scattering Intensity" );

    // #### must take care of the operation log... this starts with it empty
     DataSetTools.dataset.DataSet new_ds = factory.getDataSet(); 
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to Energy Loss" );

    // copy the attributes of the original data set
    new_ds.getAttributeList().addAttributes( ds.getAttributeList() );

                                     // get the energy scale parameters 
    float min_E = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_E = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   num_E = ( (Float)(getParameter(2).getValue()) ).intValue() + 1;

                                     // validate energy bounds
    if ( min_E > max_E )             // swap bounds to be in proper order
    {
      float temp = min_E;
      min_E = max_E;
      max_E = temp;
    }

    XScale new_e_scale;
    if ( num_E <= 1.0 || min_E >= max_E )       // no valid scale set
      new_e_scale = null;
    else
      new_e_scale = new UniformXScale( min_E, max_E, num_E );  

                                            // now proceed with the operation 
                                            // on each data block in DataSet 
    Data             data,
                     new_data;
    DetectorPosition position;
    float            energy_in;
    Float            energy_in_obj;
    float            y_vals[];              // y_values from one spectrum
    float            e_vals[];              // energy values at bin boundaries
                                            // calculated from tof bin bounds
    XScale           E_scale;
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

      energy_in_obj=(Float)
                      attr_list.getAttributeValue(Attribute.ENERGY_IN);

      if( position != null && energy_in_obj != null)
                                                       // has needed attributes 
      {                                                // so convert it to E
                                       // calculate energies at bin boundaries
        energy_in        = energy_in_obj.floatValue();

        spherical_coords = position.getSphericalCoords();
        e_vals           = data.getX_scale().getXs();
        for ( int i = 0; i < e_vals.length; i++ )
          e_vals[i] = energy_in - 
                      tof_calc.Energy( spherical_coords[0], e_vals[i] );
  
        E_scale = new VariableXScale( e_vals );
        y_vals  = data.getCopyOfY_values();

        new_data = new Data( E_scale, y_vals, data.getGroup_ID() ); 
                                                // create new data block with 
        new_data.setSqrtErrors();               // non-uniform E_scale and 
                                                // the original y_vals.
        new_data.setAttributeList( attr_list ); // copy the attributes

        if ( new_e_scale != null )              // rebin if a valid scale was
          new_data.ReBin( new_e_scale );        // specified

        new_ds.addData_entry( new_data );      
      }
    }
   // ChopTools.chop_dataDrawer.drawgraphDataSet(new_ds);
    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SpectrometerTofToEnergyLoss Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SpectrometerTofToEnergyLoss new_op = new SpectrometerTofToEnergyLoss( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }



}

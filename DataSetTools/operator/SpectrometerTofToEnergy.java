/*
 * @(#)SpectrometerTofToEnergy.java   0.2  99/06/11   Dennis Mikkelson
 *
 *                                 99/08/16   Added constructor to allow
 *                                            calling operator directly
 *             
 * This operator converts neutron time-of-flight DataSet to energy.  The
 * DataSet must contain spectra with an attribute giving the detector position.
 * In addition, it is assumed that the XScale for the spectra represents the
 * time-of-flight from the sample to the detector. 
 */

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;

/**
  *  Convert a neutron time-of-flight DataSet to energy.. 
  */

public class SpectrometerTofToEnergy extends    DataSetOperator 
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

  public SpectrometerTofToEnergy( )
  {
    super( "Convert to Energy" );
    Parameter parameter;

    parameter = new Parameter( "Min Energy(meV)", new Float(5.0) );
    addParameter( parameter );

    parameter = new Parameter( "Max Energy(meV)", new Float(500.0) );
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
   *  @param  min_E       The minimum energy value to be binned
   *  @param  max_E       The maximum energy value to be binned
   *  @param  num_E       The number of "bins" to be used between min_E and
   *                      max_E
   */

  public SpectrometerTofToEnergy( DataSet     ds,
                                  float       min_E,
                                  float       max_E,
                                  int         num_E )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float( min_E ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( max_E ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Integer( num_E ) );

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
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSetFactory factory = new DataSetFactory( 
                                     ds.getTitle(),
                                     "meV",
                                     "FinalEnergy",
                                     "counts",
                                     "Scattering Intensity" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet(); 
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to Energy" );

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
    if ( num_E < 2 || min_E >= max_E )      // no valid scale set
      new_e_scale = null;
    else
      new_e_scale = new UniformXScale( min_E, max_E, num_E );  

                                            // now proceed with the operation 
                                            // on each data block in DataSet 
    Data             data,
                     new_data;
    DetectorPosition position;
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

      if( position != null )           // has needed attributes so convert to E 
      { 
                                       // calculate energies at bin boundaries
        spherical_coords = position.getSphericalCoords();
        e_vals           = data.getX_scale().getXs();
        for ( int i = 0; i < e_vals.length; i++ )
          e_vals[i] = tof_calc.Energy( spherical_coords[0], e_vals[i] );
  
                                               // reorder values to keep in
                                               // increasing order
        arrayUtil.Reverse( e_vals );
        E_scale = new VariableXScale( e_vals );

        y_vals = data.getCopyOfY_values();
        arrayUtil.Reverse( y_vals );

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

    return new_ds;
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SpectrometerTofToEnergy Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SpectrometerTofToEnergy new_op = new SpectrometerTofToEnergy( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }


}

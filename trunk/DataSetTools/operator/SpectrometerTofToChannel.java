/*
 * @(#)SpectrometerTofToChannel.java   0.1  99/06/17   Dongfeng & Alok
 *             
 * This operator converts neutron time-of-flight DataSet to channel.  The
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
  *  Convert a neutron time-of-flight DataSet to Channel. 
  */

public class SpectrometerTofToChannel extends    DataSetOperator 
                                         implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */

                                     // The constructor calls the super
                                     // class constructor, then sets up the
                                     // list of parameters.
  public SpectrometerTofToChannel( ) 
  {
    super( "Convert to Channel" );
   /*
    Parameter parameter;

    parameter = new Parameter( "Min Wavelength(A)", new Float(0.0) );
    addParameter( parameter );

    parameter = new Parameter( "Max Wavelength(A)", new Float(10.0) );
    addParameter( parameter );

    parameter = new Parameter( "Number of Bins ", new Float(1000.0) );
    addParameter( parameter );//*/
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
                                     "Channel",
                                     "Number",
                                     "Counts",
                                     "Scattering Intensity" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet(); 
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to Channel" );

    // copy the attributes of the original data set
    new_ds.getAttributeList().addAttributes( ds.getAttributeList() );

                                            // now proceed with the operation 
                                            // on each data block in DataSet 
    Data             data,
                     new_data;
    float            y_vals[];            // y_values from one spectrum
    XScale           wl_scale;
    int              num_data = ds.getNum_entries();
    AttributeList    attr_list;

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );        // get reference to the data entry
      attr_list = data.getAttributeList();

                                           // get the detector position and
                                           // initial path length 
      { 
                                       // calculate wavelength at bin boundaries

         y_vals = data.getCopyOfY_values();
        wl_scale = new  UniformXScale( 0,  y_vals.length-1,  y_vals.length );

        new_data = new Data( wl_scale, y_vals, data.getGroup_ID() );
                                                 // create new data block with 
        new_data.setSqrtErrors();                // non-uniform E_scale and 
                                                 // the original y_vals.
        new_data.setAttributeList( attr_list );  // copy the attributes


        new_ds.addData_entry( new_data );      
      }
    }

    return new_ds;
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SpectrometerTofToChannel Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SpectrometerTofToChannel new_op = new SpectrometerTofToChannel( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }

}

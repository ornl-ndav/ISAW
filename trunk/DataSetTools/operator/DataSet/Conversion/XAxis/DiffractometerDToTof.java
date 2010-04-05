/*
 * File:  DiffractometerDToTof.java 
 *             
 * Copyright (C) 2010 Dennis Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package DataSetTools.operator.DataSet.Conversion.XAxis;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Parameters.IParameter;
import gov.anl.ipns.ViewTools.UI.*;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.instruments.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.gsastools.GsasCalib;
import  DataSetTools.viewer.*;

/**
 * This operator converts a neutron time-of-flight from D-spacing to
 * time-of-flight.  The DataSet must contain spectra with attributes 
 * giving the detector position and source to sample distance.  
 */

public class DiffractometerDToTof extends    XAxisConversionOp 
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

  public DiffractometerDToTof()
  {
    super( "Convert d-Spacing to TOF" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter value so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   */

  public DiffractometerDToTof( DataSet ds )
  {
    this();          
    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return  the command name to be used with script processor: 
   *          in this case, ToTof
   */
   public String getCommand()
   {
     return "ToTof";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters
  }


  /* -------------------------- new_X_label ---------------------------- */
  /**
   * Get string label for converted x values.
   *
   *  @return  String describing the x label and units for converted x values.
   */
   public String new_X_label()
   {
     return new String( "Time(us)" );
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
    DataSet ds = this.getDataSet();          // make sure we have a DataSet
    if ( ds == null )
      return Float.NaN;

    int num_data = ds.getNum_entries();      // make sure we have a valid Data
    if ( i < 0 || i >= num_data )            // index
      return Float.NaN;

    Data data = ds.getData_entry( i );
                                             // get the detector position and
                                             // initial path length
    DetectorPosition position = AttrUtil.getDetectorPosition( data );
    float initial_path        = AttrUtil.getInitialPath( data );

    if( position == null || Float.isNaN(initial_path) )// make sure it has
      return Float.NaN;                                // the needed attributes
        
    float spherical_coords[] = position.getSphericalCoords();
    float total_length       = initial_path + spherical_coords[0];
    float scattering_angle   = position.getScatteringAngle();
        
    return tof_calc.TOFofDSpacing( scattering_angle, total_length, x );
  }


  /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator converts the X-axis units on a DataSet ");
    s.append("from D-spacing to time-of-flight in microseconds.");
    s.append("@assumptions The DataSet must contain spectra with ");
    s.append("attributes giving the detector position and source to sample ");
    s.append("distance ( the initial flight path ). In addition, it is ");
    s.append("assumed that the XScale for the spectra is currently ");
    s.append("Angstroms, representing the d-spacing value.");
    s.append("@algorithm Creates a new DataSet which has the same title ");
    s.append("as the input DataSet, the same y-values as the input DataSet, ");
    s.append("and whose X-axis units have been converted to time-of-flight. ");
    s.append("The new DataSet also has new time-of-flight converstion ");
    s.append("operators added to its ");
    s.append("operator list, and a message appended to its log indicating ");
    s.append("that a conversion to units of TOF on the X-axis was done.");
    s.append("@param ds The DataSet to which the operation is applied.");
    s.append("@return A new DataSet which is the result of converting the ");
    s.append("input DataSet's X-axis units from d to time-of-flight .");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   *  Converts the input DataSet to a DataSet which is identical except that
   *  the new DataSet's X-axis units have been converted to time-of-flight.
   *  
   *  @return DataSet whose X-axis units have been converted to TOF.
   */
  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();

    String x_units = ds.getX_units();
    if ( ! x_units.equalsIgnoreCase( "Angstroms" ) )
      throw new IllegalArgumentException("DataSet X-units must be Angstroms");

                                     // construct a new data set with the same
                                     // title, as the current DataSet, ds.
    DataSetFactory factory = new DataSetFactory( 
                                     ds.getTitle(),
                                     "Time-of-flight",
                                     "Time(us)",
                                     "Counts",
                                     "Scattering Intensity" );

    DataSet new_ds = factory.getDataSet(); 

    // #### must take care of the operation log... this starts with it empty
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted from d-Spacing to TOF" );

    // copy the attributes of the original data set
    new_ds.setAttributeList( ds.getAttributeList() );

                                            // now proceed with the operation 
                                            // on each data block in DataSet 
    Data             data,
                     new_data;
    DetectorPosition position;
    float            initial_path;
    float            total_length;
    float            scattering_angle;
    float            y_vals[];              // y_values from one spectrum
    float            errors[];              // errors from one spectrum
    float            d_vals[];              // d-values at bin boundaries
    float            tof_vals[];            // tof values at bin boundaries
                                            // calculated from "d" bin bounds
    XScale           tof_scale;
    float            spherical_coords[];
    int              num_data = ds.getNum_entries();
    AttributeList    attr_list;
	
    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );        // get reference to the data entry
      attr_list = data.getAttributeList();

                                           // get the detector position and
                                           // initial path length 
      position = AttrUtil.getDetectorPosition( data );

      initial_path = AttrUtil.getInitialPath( data );
      if ( Float.isNaN( initial_path ) )
        initial_path = AttrUtil.getInitialPath( ds );

                                            // check that the Data block has
                                            // the needed parameters and if 
                                            // so, convert to time-of-flight
      if ( position == null || Float.isNaN(initial_path) )
         throw new IllegalArgumentException(
                    "DataSet is missing Detector Position or Initial Path");
      else
      {
        spherical_coords = position.getSphericalCoords();
        total_length     = initial_path + spherical_coords[0];
        scattering_angle = position.getScatteringAngle();
 
        d_vals           = data.getX_scale().getXs();
        tof_vals         = new float[ d_vals.length ];
        for ( int i = 0; i < d_vals.length; i++ )
          tof_vals[i] = tof_calc.TOFofDSpacing( scattering_angle,
                                                total_length, 
                                                d_vals[i]        );
  
        tof_scale = new VariableXScale( tof_vals );

        y_vals  = data.getCopyOfY_values();
        errors  = data.getErrors();

        new_data = Data.getInstance( tof_scale, 
                                     y_vals, 
                                     errors, 
                                     data.getGroup_ID() );
                                                // create new data block with 
                                                // new tof x_scale and 
                                                // the original y_vals.
        new_data.setAttributeList( attr_list ); // copy the attributes

        new_ds.addData_entry( new_data );      
      }
    }

    DataSetFactory.addOperators( new_ds, InstrumentType.TOF_DIFFRACTOMETER );

    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DiffractometerDToTof Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    DiffractometerDToTof new_op = new DiffractometerDToTof( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}

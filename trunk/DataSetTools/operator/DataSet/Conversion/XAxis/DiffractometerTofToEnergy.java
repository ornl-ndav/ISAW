/*
 * File:  DiffractometerTofToEnergy.java
 *
 * Copyright (C) 1999, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *             
 *  $Log$
 *  Revision 1.8  2004/03/15 03:28:26  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.7  2004/01/24 19:10:46  bouzekc
 *  Removed unused variables from main().  Removed unused imports.
 *
 *  Revision 1.6  2002/12/17 19:18:40  dennis
 *  Added getDocumentation() method, simple main test program and
 *  Java docs for the getResult() method. (Chris Bouzek)
 *
 *  Revision 1.5  2002/11/27 23:17:04  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/09/19 16:00:28  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.3  2002/03/18 21:32:47  dennis
 *  Now checks whether or not the errors array is null before attempting
 *  to reverse the array.
 *
 *  Revision 1.2  2002/03/13 16:19:17  dennis
 *  Converted to new abstract Data class.
 *
 *  Revision 1.1  2002/02/22 21:00:51  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Conversion.XAxis;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Numeric.*;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;
import  DataSetTools.viewer.*;

/**
 * This operator converts a neutron time-of-flight DataSet to energy.  The
 * DataSet must contain spectra with attributes giving the detector position
 * and source to sample distance ( the initial flight path ). In addition,
 * it is assumed that the XScale for the spectra represents the time-of-flight
 * from the source to the detector.
 */

public class DiffractometerTofToEnergy extends    XAxisConversionOp 
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

  public DiffractometerTofToEnergy( )
  {
    super( "Convert to Energy" );
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

  public DiffractometerTofToEnergy( DataSet     ds,
                                    float       min_E,
                                    float       max_E,
                                    int         num_E )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( new Float( min_E ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( max_E ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Integer( num_E ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   *         in this case, ToE
   */
   public String getCommand()
   {
     return "ToE";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    UniformXScale scale = getXRange();

    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter;

    if ( scale == null )
      parameter = new Parameter( "Min Energy(meV)", new Float(0.0) );
    else
      parameter = new Parameter( "Min Energy(meV)",
                                  new Float(scale.getStart_x()));
    addParameter( parameter );

    if ( scale == null )
      parameter = new Parameter( "Max Energy(meV)", new Float(500.0) );
    else
      parameter = new Parameter( "Max Energy(meV)",
                                  new Float(scale.getEnd_x()));
    addParameter( parameter );

    parameter = new Parameter( Parameter.NUM_BINS, new Integer(1000) );
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
     return new String( "E(meV)" );
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

    Data data               = ds.getData_entry( i );
    AttributeList attr_list = data.getAttributeList();

                                             // get the detector position and
                                             // initial path length
    DetectorPosition position=(DetectorPosition)
                       attr_list.getAttributeValue( Attribute.DETECTOR_POS);

    Float initial_path_obj=(Float)
                        attr_list.getAttributeValue(Attribute.INITIAL_PATH);

    if( position == null || initial_path_obj == null)  // make sure it has the
      return Float.NaN;                                // needed attributes
                                                       // to convert it to D

    float initial_path       = initial_path_obj.floatValue();
    float spherical_coords[] = position.getSphericalCoords();
    float total_length       = initial_path + spherical_coords[0];

    return tof_calc.Energy( total_length, x );
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
    s.append("from neutron time-of-flight to energy.");
    s.append("@assumptions The DataSet must contain spectra with ");
    s.append("attributes giving the detector position and source to sample ");
    s.append("distance ( the initial flight path ). In addition, it is ");
    s.append("assumed that the XScale for the spectra represents the ");
    s.append("time-of-flight from the source to the detector.");
    s.append("@algorithm Creates a new DataSet which has the same title ");
    s.append("as the input DataSet, the same y-values as the input DataSet, ");
    s.append("and whose X-axis units have been converted to energy.  The new ");
    s.append("DataSet also has a message appended to its log indicating ");
    s.append("that a conversion to units of energy on the X-axis was done.");
    s.append("@param ds The DataSet to which the operation is applied.");
    s.append("@param min_E The minimum energy value to be binned.");
    s.append("@param max_E The maximum energy value to be binned.");
    s.append("@param num_E The number of \"bins\" to be used between ");
    s.append("min_E and max_E.");
    s.append("@return A new DataSet which is the result of converting the ");
    s.append("input DataSet's X-axis units to energy.");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   *  Converts the input DataSet to a DataSet which is identical except that
   *  the new DataSet's X-axis units have been converted to energy.
   *  
   *  @return DataSet whose X-axis units have been converted to energy.
   */
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
                                     "Energy",
                                     "Counts",
                                     "Scattering Intensity" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet(); 
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to Energy" );

    // copy the attributes of the original data set
    new_ds.setAttributeList( ds.getAttributeList() );
                                     
                                     // get the energy scale parameters 
    float min_E = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_E = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   num_E = ( (Integer)(getParameter(2).getValue()) ).intValue() + 1;

                                     // validate energy bounds
    if ( min_E > max_E )             // swap bounds to be in proper order
    {
      float temp = min_E;
      min_E = max_E;
      max_E = temp;
    }

    UniformXScale new_e_scale;
    if ( num_E <= 1.0 || min_E >= max_E )       // no valid scale set
      new_e_scale = null;
    else
      new_e_scale = new UniformXScale( min_E, max_E, num_E );  

                                            // now proceed with the operation 
                                            // on each data block in DataSet 
    Data             data,
                     new_data;
    DetectorPosition position;
    float            initial_path;
    Float            initial_path_obj;
    float            total_length;
    float            y_vals[];              // y_values from one spectrum
    float            errors[];              // errors from one spectrum
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

      initial_path_obj=(Float)
                         attr_list.getAttributeValue(Attribute.INITIAL_PATH);

      if( position != null && initial_path_obj != null)
                                                       // has needed attributes 
      {                                                // so convert it to E
                                       // calculate energies at bin boundaries
        initial_path     = initial_path_obj.floatValue();
        spherical_coords = position.getSphericalCoords();
        total_length     = initial_path + spherical_coords[0];
 
        e_vals           = data.getX_scale().getXs();
        for ( int i = 0; i < e_vals.length; i++ )
          e_vals[i] = tof_calc.Energy( total_length, e_vals[i] );
  
                                               // reorder values to keep in
                                               // increasing order
        arrayUtil.Reverse( e_vals );
        E_scale = new VariableXScale( e_vals );

        y_vals  = data.getCopyOfY_values();    // need copy, since we alter it
        errors  = data.getCopyOfErrors();
        arrayUtil.Reverse( y_vals );
        if ( errors != null )
          arrayUtil.Reverse( errors );

        new_data = Data.getInstance( E_scale, 
                                     y_vals, 
                                     errors, 
                                     data.getGroup_ID() ); 
                                                // create new data block with 
                                                // non-uniform E_scale and 
                                                // the original y_vals.
        new_data.setAttributeList( attr_list ); // copy the attributes

                                                // resample if a valid
        if ( new_e_scale != null )              // scale was specified
          new_data.resample( new_e_scale, IData.SMOOTH_NONE ); 

        new_ds.addData_entry( new_data );      
      }
    }

    return new_ds;
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DiffractometerTofToEnergy Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    DiffractometerTofToEnergy new_op = new DiffractometerTofToEnergy( );
                                                // copy the data set associated
                                                // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

  /* --------------------------- main ----------------------------------- */
  /*
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
    float min_1 = 0.5f, max_1 = 1.0f;
                                              //create the first test DataSet
    DataSet ds1 = DataSetFactory.getTestDataSet(); 
     new ViewManager(ds1, ViewManager.IMAGE);

    DiffractometerTofToEnergy op = 
                        new DiffractometerTofToEnergy(ds1, min_1, max_1, 100);

    DataSet new_ds = (DataSet)op.getResult();
    new ViewManager(new_ds, ViewManager.IMAGE);	
    System.out.println(op.getDocumentation());
  }
}

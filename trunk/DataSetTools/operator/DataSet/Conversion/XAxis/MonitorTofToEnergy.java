/*
 * File:  MonitorTofToEnergy.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 *  Revision 1.6  2003/01/09 17:34:50  dennis
 *  Added getDocumentation(), main test program and java docs on getResult()
 *  (Chris Bouzek)
 *
 *  Revision 1.5  2002/11/27 23:17:04  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/09/19 16:00:33  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.3  2002/03/18 21:32:49  dennis
 *  Now checks whether or not the errors array is null before attempting
 *  to reverse the array.
 *
 *  Revision 1.2  2002/03/13 16:19:17  dennis
 *  Converted to new abstract Data class.
 *
 *  Revision 1.1  2002/02/22 21:00:54  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Conversion.XAxis;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;
import  DataSetTools.viewer.*;
import  DataSetTools.retriever.*;

/**
 * This operator converts a beammonitor time-of-flight DataSet to energy.  The
 * DataSet must contain spectra corresponding to monitors along the beam line
 * with attributes giving the initial path length from the source to the
 * sample and the monitor position relative to the sample.  The monitor
 * must be along the x-axis.  In addition, it is assumed that the
 * XScale for the spectra represents the time-of-flight from the source to
 * the monitor.
 */

public class MonitorTofToEnergy extends    XAxisConversionOp
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

  public MonitorTofToEnergy( )
  {
    super( "Monitor to Energy" );
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

  public MonitorTofToEnergy( DataSet     ds,
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
   *         in this case, MonToE
   */
   public String getCommand()
   {
     return "MonToE";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    UniformXScale scale = getXRange();

    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter;

    if ( scale == null )
      parameter = new Parameter( "Min Energy(meV)", new Float(5.0) );
    else
      parameter = new Parameter( "Min Energy(meV)",
                                  new Float(scale.getStart_x()) );
    addParameter( parameter );

    if ( scale == null )
      parameter = new Parameter("Max Energy(meV)", new Float(500.0) );
    else
      parameter = new Parameter("Max Energy(meV)", new Float(scale.getEnd_x()));

    addParameter( parameter );

    parameter = new Parameter( Parameter.NUM_BINS, new Integer( 500 ) );
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

                                             // get the detector position
    DetectorPosition position=(DetectorPosition)
                       attr_list.getAttributeValue( Attribute.DETECTOR_POS);

    Float initial_path_obj=(Float)
                        attr_list.getAttributeValue(Attribute.INITIAL_PATH);

    if( position == null || initial_path_obj == null)  // make sure it has the
      return Float.NaN;                                // needed attributes
                                                       // to convert it to E

    float initial_path       = initial_path_obj.floatValue();
    float cartesian_coords[] = position.getCartesianCoords();
    float total_path         = initial_path + cartesian_coords[0];

    return tof_calc.Energy( total_path, x );
  }

  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator converts the X-axis units on a ");
    s.append("beammonitor DataSet from time-of-flight to energy.\n");
    s.append("@assumptions The DataSet must contain spectra corresponding ");
    s.append("to monitors along the beam line with attributes giving the ");
    s.append("initial path length from the source to the sample and the ");
    s.append("monitor position relative to the sample.  The monitor must ");
    s.append("be along the X-axis.  In addition, it is assumed that the ");
    s.append("XScale for the spectra represents the time-of-flight from the ");
    s.append("source to the monitor.\n");
    s.append("@algorithm Creates a new beammonitor DataSet which has the same ");
    s.append("title as the input DataSet, the same y-values as the input ");
    s.append("DataSet, and whose X-axis units have been converted to energy.  ");
    s.append("The new DataSet also has a message appended to its log ");
    s.append("indicating that a conversion to units of energy on the X-axis ");
    s.append("was done.\n");
    s.append("@param ds The beammonitor DataSet to which the operation is ");
    s.append("applied.\n");
    s.append("@param min_E The minimum energy value to be binned.\n");
    s.append("@param max_E The maximum energy value to be binned.\n");
    s.append("@param num_E The number of \"bins\" to be used between ");
    s.append("min_E and max_E.\n");
    s.append("@return A new beammonitor DataSet which is the result of ");
    s.append("converting the input DataSet's X-axis units to energy.\n");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   *  Converts the input beammonitor DataSet to a DataSet which is identical
   *  except that the new DataSet's X-axis units have been converted from
   *  time-of-flight to energy.
   *
   *  @return DataSet whose X-axis units have been converted from
   *  time-of-flight to energy.
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
                                     "FinalEnergy",
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
    if ( num_E < 2 || min_E >= max_E )      // no valid scale set
      new_e_scale = null;
    else
      new_e_scale = new UniformXScale( min_E, max_E, num_E );

                                            // now proceed with the operation
                                            // on each data block in DataSet
    Data             data,
                     new_data;
    DetectorPosition position;
    Float            initial_path_obj;
    float            initial_path;
    float            total_path;
    float            y_vals[];              // y_values from one spectrum
    float            errors[];              // errors from one spectrum
    float            e_vals[];              // energy values at bin boundaries
                                            // calculated from tof bin bounds
    XScale           E_scale;
    float            cartesian_coords[];
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

                                           // make sure it has the needed
                                           // attributes to convert it to E
      if( position != null && initial_path_obj != null )
      {
                                       // calculate energies at bin boundaries
        initial_path     = initial_path_obj.floatValue();
        cartesian_coords = position.getCartesianCoords();
        total_path       = initial_path + cartesian_coords[0];
        e_vals           = data.getX_scale().getXs();
        for ( int i = 0; i < e_vals.length; i++ )
          e_vals[i] = tof_calc.Energy( total_path, e_vals[i] );

                                               // reorder values to keep in
                                               // increasing order
        arrayUtil.Reverse( e_vals );
        E_scale = new VariableXScale( e_vals );

        y_vals = data.getCopyOfY_values();    // need copy, since we alter it
        errors = data.getCopyOfErrors();
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
   * Get a copy of the current MonitorTofToEnergy Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    MonitorTofToEnergy new_op = new MonitorTofToEnergy( );
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
    float min_1 = (float)45.0, max_1 = (float)80.0;
    String file_name = "/home/groups/SCD_PROJECT/SampleRuns/GPPD12358.RUN";
                       //"D:\\ISAW\\SampleRuns\\GPPD12358.RUN";

    try
    {
      RunfileRetriever rr = new RunfileRetriever( file_name );
      DataSet ds1 = rr.getDataSet(1);
      ViewManager viewer = new ViewManager(ds1, IViewManager.IMAGE);
      MonitorTofToEnergy op =
                         new MonitorTofToEnergy(ds1, min_1, max_1, 100);
      DataSet new_ds = (DataSet)op.getResult();
      ViewManager new_viewer = new ViewManager(new_ds, IViewManager.IMAGE);
      System.out.println(op.getDocumentation());
    }
      catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}

/*
 * File:  DiffractometerTofToWavelength.java
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
 * $Log$
 * Revision 1.10  2004/01/24 19:10:46  bouzekc
 * Removed unused variables from main().  Removed unused imports.
 *
 * Revision 1.9  2002/12/20 17:11:01  dennis
 * Added getDocumentation() method, java docs for getResult() and
 * simple main test program. (Chris Bouzek)
 *
 * Revision 1.8  2002/11/27 23:17:04  pfpeterson
 * standardized header
 *
 * Revision 1.7  2002/09/19 16:00:30  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.6  2002/07/23 18:12:58  dennis
 * Fixed javadoc comment.
 *
 * Revision 1.5  2002/07/08 20:46:05  pfpeterson
 * Now uses String constants in FontUtil.
 *
 * Revision 1.4  2002/07/02 17:06:51  pfpeterson
 * Now uses string constants defined in IsawGUI.Isaw.
 *
 * Revision 1.3  2002/06/19 21:59:50  pfpeterson
 * Modified to add the new conversion operators once Operation
 * is completed.
 *
 * Revision 1.2  2002/03/13 16:19:17  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.1  2002/02/22 21:00:53  pfpeterson
 * Operator reorganization.
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
 * This operator converts a neutron time-of-flight DataSet for a Diffractometer,
 * to wavelength.  The DataSet must contain spectra with attributes giving the
 * detector position and source to sample distance ( the initial flight path ).
 * In addition, it is assumed that the XScale for the spectra represents the
 * time-of-flight from the SOURCE to the detector.
 */

public class DiffractometerTofToWavelength extends    XAxisConversionOp
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

  public DiffractometerTofToWavelength( )
  {
    super( "Convert to Wavelength" );
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

  public DiffractometerTofToWavelength( DataSet     ds,
                                        float       min_wl,
                                        float       max_wl,
                                        int         num_wl )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( new Float( min_wl ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( max_wl ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Integer( num_wl ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor:
   *            in this case, ToWL
   */
   public String getCommand()
   {
     return "ToWL";
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
      parameter = new Parameter( "Min Wavelength("+FontUtil.ANGSTROM+")",
                                  new Float(0.0) );
    else
      parameter = new Parameter( "Min Wavelength("+FontUtil.ANGSTROM+")",
                                  new Float(scale.getStart_x()));
    addParameter( parameter );

    if ( scale == null )
      parameter = new Parameter( "Max Wavelength("+FontUtil.ANGSTROM+")",
                                 new Float(5.0) );
    else
      parameter = new Parameter( "Max Wavelength("+FontUtil.ANGSTROM+")",
                                  new Float(scale.getEnd_x()));

    addParameter( parameter );

    parameter = new Parameter( Parameter.NUM_BINS, new Integer( 1000 ) );
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
     return new String( FontUtil.LAMBDA + "("+FontUtil.ANGSTROM+")" );
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

    return tof_calc.Wavelength( total_length, x );
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
    s.append("DataSet from neutron time-of-flight to wavelength.");
    s.append("@assumptions The DataSet must contain spectra with ");
    s.append("attributes giving the detector position and source to sample ");
    s.append("distance ( the initial flight path ). In addition, it is ");
    s.append("assumed that the XScale for the spectra represents the ");
    s.append("time-of-flight from the source to the detector.");
    s.append("@algorithm Creates a new DataSet which has the same title ");
    s.append("as the input DataSet, the same y-values as the input DataSet, ");
    s.append("and whose X-axis units have been converted to wavelength.  ");
    s.append("The new DataSet also has a message appended to its log ");
    s.append("indicating that a conversion to units of wavelength on the ");
    s.append("X-axis was done.  ");
    s.append("Furthermore, two operators are added to the DataSet: ");
    s.append("DiffractometerWavelengthToQ and DiffractometerWavelengthToD.");
    s.append("@param ds The DataSet to which the operation is applied.");
    s.append("@param min_wl The minimum wavelength value to be binned.");
    s.append("@param max_wl The maximum wavelength value to be binned.");
    s.append("@param num_wl The number of \"bins\" to be used between ");
    s.append("min_wl and max_wl.");
    s.append("@return A new DataSet which is the result of converting the ");
    s.append("input DataSet's X-axis units to wavelength.");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   *  Converts the input DataSet to a DataSet which is identical except that
   *  the new DataSet's X-axis units have been converted to wavelength.
   *
   *  @return DataSet whose X-axis units have been converted to wavelength.
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
                                     "Angstroms",
                                     "Wavelength",
                                     "Counts",
                                     "Scattering Intensity" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet();
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to Wavelength" );

    // copy the attributes of the original data set
    new_ds.setAttributeList( ds.getAttributeList() );

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

    UniformXScale new_wl_scale;
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
    float            errors[];           // errors from one spectrum
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
      {                                    // so convert it to wavelength
                                      // calculate wavelengths at bin boundaries
        initial_path     = initial_path_obj.floatValue();
        spherical_coords = position.getSphericalCoords();
        total_length     = initial_path + spherical_coords[0];

        wl_vals           = data.getX_scale().getXs();
        for ( int i = 0; i < wl_vals.length; i++ )
          wl_vals[i] = tof_calc.Wavelength( total_length, wl_vals[i] );

        wl_scale = new VariableXScale( wl_vals );

        y_vals  = data.getY_values();
        errors  = data.getErrors();

        new_data = Data.getInstance( wl_scale,
                                     y_vals,
                                     errors,
                                     data.getGroup_ID() );
                                                // create new data block with
                                                // non-uniform E_scale and
                                                // the original y_vals.
        new_data.setAttributeList( attr_list ); // copy the attributes

                                                // resample if a valid
        if ( new_wl_scale != null )             // scale was specified
          new_data.resample( new_wl_scale, IData.SMOOTH_NONE );

        new_ds.addData_entry( new_data );
      }
    }
    new_ds.addOperator(new DiffractometerWavelengthToQ());
    new_ds.addOperator(new DiffractometerWavelengthToD());

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
    new_op.CopyParametersFrom( this );

    return new_op;
  }

  /* --------------------------- main ----------------------------------- */
  /*
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
    float min_1 = 0.75f, max_1 = 2.0f;
    String file_name = "/home/groups/SCD_PROJECT/SampleRuns/GPPD12358.RUN";
                       /*"D:\\ISAW\\SampleRuns\\GPPD12358.RUN";*/

    try
    {
      RunfileRetriever rr = new RunfileRetriever( file_name );
      DataSet ds1 = rr.getDataSet(1);
      new ViewManager(ds1, IViewManager.IMAGE);
      DiffractometerTofToWavelength op =
                     new DiffractometerTofToWavelength(ds1, min_1, max_1, 100);
      DataSet new_ds = (DataSet)op.getResult();
      new ViewManager(new_ds, IViewManager.IMAGE);
      System.out.println(op.getDocumentation());
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

}

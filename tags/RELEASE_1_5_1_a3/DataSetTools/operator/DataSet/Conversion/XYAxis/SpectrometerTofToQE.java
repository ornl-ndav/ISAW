/*
 * File:  SpectrometerTofToQE.java
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 * Revision 1.5  2003/01/14 19:18:52  dennis
 * Added getDocumentation(), basic main test program and java docs
 * for getResult().  (Chris Bouzek)
 *
 * Revision 1.4  2002/11/27 23:17:15  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/09/19 16:00:49  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.2  2002/03/13 16:19:17  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.1  2002/02/22 21:01:13  pfpeterson
 * Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Conversion.XYAxis;

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
  *  Convert an energy spectrum from a Spectrometer into a DataSet containing
  *  the rows of a QE image. The Data blocks will first be rebinned into
  *  energy bins with the same bin boundaries.  Next, for each energy-bin,
  *  the data values from all detectors at that energy-bin, will be merged
  *  into a set of equal Q bins, using the estimated Q coverage of each
  *  detector.  This will produce a two-dimensional set of values which will
  *  be cut into rows to produce a new DataSet.
  *
  *  @see XYAxisConversionOp
  */

public class SpectrometerTofToQE extends    XYAxisConversionOp
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

  public SpectrometerTofToQE( )
  {
    super( "Convert to QE plot" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  min_Q       The minimum Q to include
   *  @param  max_Q       The maximum Q to include
   *  @param  n_Q_bins    The number of "bins" to be used between min_Q and
   *                      max_Q.
   *  @param  min_E       The minimum E to include
   *  @param  max_E       The maximum E to include
   *  @param  n_E_bins    The number of "bins" to be used between min_E and
   *                      max_E.
   */

  public SpectrometerTofToQE( DataSet     ds,
                              float       min_Q,
                              float       max_Q,
                              int         n_Q_bins,
                              float       min_E,
                              float       max_E,
                              int         n_E_bins )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter(0);
    parameter.setValue( new Float(min_Q) );

    parameter = getParameter(1);
    parameter.setValue( new Float(max_Q));

    parameter = getParameter(2);
    parameter.setValue( new Integer(n_Q_bins) );

    parameter = getParameter(3);
    parameter.setValue( new Float(min_E) );

    parameter = getParameter(4);
    parameter.setValue( new Float(max_E));

    parameter = getParameter(5);
    parameter.setValue( new Integer(n_E_bins) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   *         in this case, ToQE
   */
   public String getCommand()
   {
     return "ToQE";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Min Q", new Float(0.0f) );
    addParameter( parameter );

    parameter = new Parameter( "Max Q", new Float(20.0f) );
    addParameter( parameter );

    parameter = new Parameter( "Num Q bins", new Integer(100));
    addParameter( parameter );

    parameter = new Parameter( "Min E", new Float(40.0f) );
    addParameter( parameter );

    parameter = new Parameter( "Max E", new Float(200.0f) );
    addParameter( parameter );

    parameter = new Parameter( "Num E bins", new Integer(100));
    addParameter( parameter );
  }

  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator converts an energy spectrum from ");
    s.append("a Spectrometer into a DataSet containing the rows of a ");
    s.append("QE image.  The X-Axis is in units of Q values; the Y-axis ");
    s.append("is in units of energy.\n");
    s.append("@assumptions The DataSet must contain spectra with an ");
    s.append("attribute giving the detector position.  In addition, ");
    s.append("it is assumed that the XScale for the spectra represents ");
    s.append("the time-of-flight from the sample to the detector.\n");
    s.append("Furthermore, a valid range of Q values must be specified, ");
    s.append("and a valid delta_2theta attribute must be available from ");
    s.append("the DataSet.\n");
    s.append("@algorithm The Data blocks will first be rebinned into ");
    s.append("energy bins with the same bin boundaries.\n  Next, for ");
    s.append("each energy-bin, the data values from all detectors at ");
    s.append("that energy-bin will be merged into a set of equal Q ");
    s.append("bins, using the estimated Q coverage of each detector.\n  ");
    s.append("This will produce a two-dimensional set of values which ");
    s.append("will be cut into rows to produce a new DataSet.  ");
    s.append("The new DataSet also has a message appended to its log ");
    s.append("that a conversion to a S(Q,E) plot was done.\n");
    s.append("@param ds The DataSet to which the operation is applied.\n");
    s.append("@param min_Q The minimum Q value to include.\n");
    s.append("@param max_Q The maximum Q value to include.\n");
    s.append("@param n_Q_bins The number of \"bins\" to be used between ");
    s.append("min_Q and max_Q.\n");
    s.append("@param min_E The minimum energy value to include.\n");
    s.append("@param max_E The maximum energy value to include.\n");
    s.append("@param n_E_bins The number of \"bins\" to be used between ");
    s.append("min_E and max_E.\n");
    s.append("@return A new DataSet which is the result of converting the ");
    s.append("input DataSet's X-axis units to Q values and its Y-axis units ");
    s.append("to energy.\n");
    s.append("@error Returns an error if no valid Q range is specified.\n");
    s.append("@error Returns an error if no detector position attribute is ");
    s.append("available.\n");
    s.append("@error Returns an error if no delta_2theta attribute is ");
    s.append("available.\n");
    return s.toString();
  }

 /* ---------------------------- getResult ------------------------------- */
 /**
  *  Converts the input DataSet to a DataSet which is a S(Q,E) plot of the
  *  original DataSet.
  *
  *  @return DataSet whose X-axis units have been converted to Q values and
  *  whose Y-axis units have been converted to energy.
  */
  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds

                                     // get the E and Q scale parameters
    float min_Q    = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float max_Q    = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int   n_Q_bins = ( (Integer)(getParameter(2).getValue()) ).intValue();
    float min_E    = ( (Float)(getParameter(3).getValue()) ).floatValue();
    float max_E    = ( (Float)(getParameter(4).getValue()) ).floatValue();
    int   n_E_bins = ( (Integer)(getParameter(5).getValue()) ).intValue();

                                        // validate energy bounds
    if ( min_E > max_E )                // swap bounds to be in proper order
    {
      float temp = min_E;
      min_E  = max_E;
      max_E  = temp;
    }

    if ( n_E_bins <= 0 )                // calculate a default
      n_E_bins = 100;                   // number of E bins.

    UniformXScale E_scale = null;
    if ( min_E >= max_E )               // no valid range specified, so err out
    {
      ErrorString message = new ErrorString(
                      "ERROR: no valid E range in QE operator");
      return message;
    }
    else
     E_scale = new UniformXScale( min_E, max_E, n_E_bins );


                                        // validate Q bounds
    if ( min_Q > max_Q )                // swap bounds to be in proper order
    {
      float temp = min_Q;
      min_Q  = max_Q;
      max_Q  = temp;
    }

    if ( n_Q_bins <= 0 )                // calculate a default
      n_Q_bins = 100;                   // number of E bins.

    if ( min_Q >= max_Q )               // no valid range specified, so err out
    {
      ErrorString message = new ErrorString(
                      "ERROR: no valid Q range in QE operator");
      return message;
    }


    // Now, map the counts from each time channel in each spectrum to a
    // point in QE space.  The values at the grid of points in QE space
    // will be averaged.

                                                   // set up arrays for S(Q,E)
                                                   // and counters.
    float QE_vals[][]   = new float[n_Q_bins][n_E_bins] ;
    int   n_QE_vals[][] = new int[n_Q_bins][n_E_bins] ;
    float old_QE_val;
    int   count;

    int q_index,
        e_index;
    for ( q_index = 0; q_index < n_Q_bins; q_index++ )
      for ( e_index = 0; e_index < n_E_bins; e_index++ )
      {
        QE_vals  [q_index][e_index] = 0.0f;
        n_QE_vals[q_index][e_index] = 0;
      }

    float            min_ang,
                     max_ang;
    float            e_in;
    Data             data;
    DetectorPosition position = null;
    float            scattering_angle;
    Float            delta_theta_obj = null;
    float            delta_theta;
    Float            energy_in_obj = null;
    float            q1,
                     q2,
                     q_val;
    float            e_val,
                     y_val;
    float            spherical_coords[];
    AttributeList    attr_list;

    float            tof_vals[],
                     center_tof;
    float            y_vals[];

    //
    // Now step along each time-of-flight spectrum, calculate the Q,E
    // coordinates for each bin and map the events in the bin to a point in
    // the Q,E plane.
    //
    int num_data = ds.getNum_entries();
    for ( int i = 0; i < num_data; i++ )
    {
      data             = ds.getData_entry(i);
                                              // first, get the position,
                                              // delta 2theta and initial
                                              // energy attributes for the
                                              // current spectrum
      attr_list = data.getAttributeList();
      position = (DetectorPosition)
                  attr_list.getAttributeValue( Attribute.DETECTOR_POS);
      if ( position == null )
      {
        ErrorString message = new ErrorString(
                    "ERROR: no DETECTOR_POS attribute in QE operator");
        return message;
      }
      spherical_coords = position.getSphericalCoords();

      delta_theta_obj = (Float)
                        attr_list.getAttributeValue( Attribute.DELTA_2THETA );
      if ( delta_theta_obj == null )
      {
        ErrorString message = new ErrorString(
                    "ERROR: no DELTA_2THETA attribute in QE operator");
        return message;
      }

      energy_in_obj = (Float)attr_list.getAttributeValue(Attribute.ENERGY_IN);
      e_in          = energy_in_obj.floatValue();

      scattering_angle = position.getScatteringAngle() * (float)(180.0/Math.PI);
      delta_theta = delta_theta_obj.floatValue();

      min_ang = (float)(Math.PI/180.0) * (scattering_angle - delta_theta/2);
      max_ang = (float)(Math.PI/180.0) * (scattering_angle + delta_theta/2);

                                             // Now get the x and y values from
                                             // the spectrum and map them to
                                             // the Q,E plane
      tof_vals = data.getX_scale().getXs();
      y_vals   = data.getY_values();
      for ( int col = 0; col < y_vals.length; col++ )
      {
                                             // calculate the energy at the
                                             // bin center
        center_tof = (tof_vals[col] + tof_vals[col+1]) / 2.0f;
        e_val = tof_calc.Energy( spherical_coords[0], center_tof );

                                             // calculate the q values at the
                                             // left and right edges of the
                                             // detector
        q1 = tof_calc.SpectrometerQ( e_in, e_val, min_ang );
        q2 = tof_calc.SpectrometerQ( e_in, e_val, max_ang );
        if ( q1 > q2 )
        {
          float temp = q1;
          q1 = q2;
          q2 = temp;
        }                               // use the average Q value
        q_val = ( q1 + q2 ) / 2.0f;
                                        // divide by the counts by delta Q,
                                        // to get intensity  ( we assume that
                                        // the values have already been divided
                                        // by delta E and delta Omega as in the
                                        // DSDODE operator
        y_val = y_vals[col] / (q2 - q1);
                                        // now map the y(Q,E) value to the
                                        // array of values and average it with
                                        // any previous values

        q_index = Math.round(( q_val - min_Q )/( max_Q - min_Q ) * n_Q_bins);
        e_index = Math.round(( e_val - min_E )/( max_E - min_E ) * n_E_bins);

        if ( q_index > 0 && q_index < n_Q_bins &&      // the value falls in
             e_index > 0 && e_index < n_E_bins   )     // the range we're using
        {
          old_QE_val = QE_vals[q_index][e_index];
          count      = n_QE_vals[q_index][e_index];
          QE_vals[q_index][e_index] = (old_QE_val * count + y_val)/( count + 1);
          n_QE_vals[q_index][e_index] = count + 1;
        }

      }

    }

   DataSetFactory factory = new DataSetFactory(
                                     ds.getTitle(),
                                     "meV",
                                     "FinalEnergy",
                                     "Counts",
                                     "Scattering Intensity" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet();
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to S(Q,E)" );

    // copy the attributes of the original data set
    new_ds.setAttributeList( ds.getAttributeList() );

    Data new_data;
    for ( int row = 0; row < n_Q_bins; row++ )
    {
      new_data = Data.getInstance( E_scale, QE_vals[row], row+1 );

      q_val = row * (max_Q - min_Q) / n_Q_bins + min_Q;
      Attribute q_attr = new FloatAttribute( Attribute.Q_VALUE, q_val );
      new_data.setAttribute( q_attr );

      new_ds.addData_entry( new_data );
    }

    return new_ds;
  }

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current TofToChannel Operator.  The list
   * of parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SpectrometerTofToQE new_op = new SpectrometerTofToQE( );
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
    float minQ, maxQ, minE, maxE;
    int Ebins, Qbins;

    minE = (float)4.0;
    maxE = (float)250;
    Ebins = 500;
    minQ = (float)0.225;
    maxQ = (float)3.2;
    Qbins = 500;

    String file_name = "/home/groups/SCD_PROJECT/SampleRuns/hrcs2447.run ";
                       //"D:\\ISAW\\SampleRuns\\hrcs2447.run ";
    try
    {
       RunfileRetriever rr = new RunfileRetriever( file_name );
       DataSet ds1 = rr.getDataSet(1);
       ViewManager viewer = new ViewManager(ds1, IViewManager.IMAGE);
       SpectrometerTofToQE op =
             new SpectrometerTofToQE(ds1, minQ, maxQ, Qbins, minE, maxE, Ebins);
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

/*
 * File:  SpectrometerTofToQ2E.java
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
 * Revision 1.1  2006/07/11 16:57:58  dennis
 * First cut at mapping to S(Q^2,E), adapted from ToQE operator.
 * Documentation still needs to be revised, and operator needs to
 * be checked by Sasha.
 *
 */


package DataSetTools.operator.DataSet.Conversion.XYAxis;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Parameters.IParameter;
import gov.anl.ipns.Parameters.SaveFilePG;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.ViewTools.UI.*;
import gov.anl.ipns.Util.Numeric.*;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.viewer.*;
import  DataSetTools.retriever.*;
import  DataSetTools.util.*;


/**
  *  Convert a time-of-flight, or energy loss spectrum from a Spectrometer 
  *  into a DataSet containing the rows of an S(Qsquared,E) image.  Each row 
  *  of the resulting image gives the scattering intensity at various 
  *  "Q-squared" values for a fixed energy loss.  That is, the Kth row 
  *  contains S(Q-squared,Ek) and the
  *  jth column contains S(Q-squaredj,E).  
  *
  *  The Q and E values corresponding to each bin center are calculated.
  *  The counts from each bin are divided by the range of Q values covered
  *  by the bin (delta_Q).  The counts are NOT divided by the range of E
  *  values covered by the bin (delta_E).  Division by delta_E is already
  *  done in the DSODE operator, so it is not needed when this is applied 
  *  to the result of the DSODE operator, or to the result of the Scattering
  *  Function operator.  (If this operator is applied to a time-of-flight
  *  DataSet, the results will only give a rough idea of S(Q,E), since the
  *  corrections applied in the DSODE operator will not have been done.)
  *  The scaled counts from each bin are then added to the bin in S(Q,E)
  *  containing the calculated Q and E values.  The accumulated counts in
  *  a particluar (Q,E) bin are divided by the number of bins that contribute
  *  to it, producing an average intensity at that point in (Q,E) space. 
  *  
  *  The resulting two dimensional array of averaged S(Q,E) values is 
  *  then placed into in a DataData set, with the values S(Q,Ek) forming
  *  the kth Data block in the DataSet.  The S(Q,E) values can also be
  *  written to a file.
  *
  *  NOTE: If the DSODE operator is applied before this ToQ2E operator,
  *  the DSODE operator should maintain a large number of energy loss 
  *  bins.  A large number of bins, eg. 1000-2000, reduces the artifacts
  *  introduced by first mapping the data in a small number of Energy 
  *  bins, and subsequently mapping them in to another small number of
  *  (Q,E) bins.
  *
  *  @see XYAxisConversionOp
  */

public class SpectrometerTofToQ2E extends    XYAxisConversionOp
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

  public SpectrometerTofToQ2E( )
  {
    super( "Convert to Q2E plot" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct a ToQ2E operator for a specified time-of-flight, or Energy
   *  loss DataSet using the specified parameter values so that the 
   *  operation can be invoked immediately by calling getResult().
   *
   *  @param  ds          The time-of-flight or Energy loss DataSet to
   *                      which the operation will be applied
   *  @param  min_Q2      The minimum Q to include
   *  @param  max_Q2      The maximum Q to include
   *  @param  n_Q2_bins   The number of "bins" to be used between min_Q and
   *                      max_Q.
   *  @param  min_E       The minimum E to include, in meV.  NOTE: This is the
   *                      actual energy value, not the energy loss value.
   *                      This parameter can be set to 0.
   *  @param  max_E       The maximum E to include, in meV.  NOTE: This is the
   *                      actual energy value, not the energy loss value.
   *                      This can safely be specified to be twice the
   *                      incident energy.
   *  @param  n_E_bins    The number of "bins" to be used between min_E and
   *                      max_E.
   *  @param  file_name   The name of the file to which the array of S(Q,E)
   *                      values are written.  If the named file cannot be
   *                      opened, the file will not be written.  A zero 
   *                      length string, "", can be used to disable writing
   *                      the file.
   */

  public SpectrometerTofToQ2E( DataSet     ds,
                               float       min_Q2,
                               float       max_Q2,
                               int         n_Q2_bins,
                               float       min_E,
                               float       max_E,
                               int         n_E_bins,
                               String      file_name )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter(0);
    parameter.setValue( new Float(min_Q2) );

    parameter = getParameter(1);
    parameter.setValue( new Float(max_Q2));

    parameter = getParameter(2);
    parameter.setValue( new Integer(n_Q2_bins) );

    parameter = getParameter(3);
    parameter.setValue( new Float(min_E) );

    parameter = getParameter(4);
    parameter.setValue( new Float(max_E));

    parameter = getParameter(5);
    parameter.setValue( new Integer(n_E_bins) );

    parameter = getParameter(6);
    parameter.setValue( file_name );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   *         in this case, ToQ2E
   */
   public String getCommand()
   {
     return "ToQ2E";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    IParameter parameter = new Parameter( "Min Q-squared", new Float(0.0f) );
    addParameter( parameter );

    parameter = new Parameter( "Max Q-squared", new Float(400.0f) );
    addParameter( parameter );

    parameter = new Parameter( "Num Q-squared bins", new Integer(100));
    addParameter( parameter );

    parameter = new Parameter( "Min E", new Float(0.0f) );
    addParameter( parameter );

    parameter = new Parameter( "Max E", new Float(200.0f) );
    addParameter( parameter );

    parameter = new Parameter( "Num E bins", new Integer(200));
    addParameter( parameter );

    parameter = new SaveFilePG( "Save Q2E array to file", null );
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
    s.append("@overview This operator converts an energy loss or ");
    s.append("time-of-flight spectrum from a spectrometer, into a ");
    s.append("DataSet containing the rows of an S(Q,E) image. ");
    s.append("Each row of the resulting image gives the scattering ");
    s.append("intensity at various Q values for a fixed energy loss. ");
    s.append("That is, the kth row contains S(Q,Ek) and the ");
    s.append("jth column contains S(Qj,E).  ");
    s.append("<p> ");
    s.append("NOTE: If the DSODE operator is applied before this ToQ2E ");
    s.append("operator, the DSODE operator should maintain a large number ");
    s.append("of energy loss bins.  A large number of bins, eg. 1000-2000, ");
    s.append("reduces the artifacts introduced by first mapping the data ");
    s.append("into a small number of Energy bins using the DSODE operator ");
    s.append("and subsequently mapping ");
    s.append("them in to another small number of (Q,E) bins.");

    s.append("@assumptions The DataSet must contain spectra with an ");
    s.append("attribute giving the detector position.  In addition, ");
    s.append("it is assumed that the XScale for the spectra represents ");
    s.append("either the time-of-flight from the sample to the detector ");
    s.append("or the energy loss, E_in - E. ");
    s.append("Furthermore, a valid range of Q values must be specified, ");
    s.append("and a valid delta_2theta attribute must be available from ");
    s.append("the DataSet.");
    s.append("<p> ");

    s.append("@algorithm The Q and E values corresponding to each bin ");
    s.append("center are calculated.  The counts from each bin are divided ");
    s.append("by the range of Q values covered by the bin (delta_Q). ");
    s.append("The counts are NOT divided by the range of E values covered ");
    s.append("by the bin (delta_E).  Division by delta_E is already ");
    s.append("done in the DSODE operator, so it is not needed  ");
    s.append("when this is applied to the result of the DSODE operator, ");
    s.append("or to the result of the Scattering Function operator. ");
    s.append("(If this operator is applied to a time-of-flight DataSet, ");
    s.append("the results will only give a rough idea of S(Q,E), since the ");
    s.append("corrections applied in the DSODE operator will not have been ");
    s.append("done.) The scaled counts from each bin are then added to ");
    s.append("the bin in S(Q,E) containing the calculated Q and E values.  ");
    s.append("The accumulated counts in a particluar (Q,E) bin are divided ");
    s.append("by the number of bins that contribute to it, producing an ");
    s.append("average intensity at that point in (Q,E) space. ");
    s.append("<p> ");
    s.append("The resulting two dimensional array of averaged S(Q,E) values ");
    s.append("is  then placed into in a DataSet, with the values ");
    s.append("S(Q,Ek) forming the kth Data block in the DataSet. ");
    s.append("The S(Q,E) values can also be written to a file. ");
    s.append(" ");
    s.append(" ");
    s.append("@param ds The time-of-flight or Energy loss DataSet to ");
    s.append("which the operation will be applied. ");

    s.append("@param min_Q The minimum Q value to include.\n");

    s.append("@param max_Q The maximum Q value to include.\n");

    s.append("@param n_Q_bins The number of \"bins\" to be used between ");
    s.append("min_Q and max_Q.\n");

    s.append("@param min_E The minimum energy value to include, in meV.  ");
    s.append("NOTE: This is the actual energy value, not the energy loss ");
    s.append("value.  This parameter can be set to 0.");

    s.append("@param max_E The maximum energy value to include, in meV.  ");
    s.append("NOTE: This is the actual energy value, not the energy loss ");
    s.append("value.  This can safely be specified to be twice the  ");
    s.append("incident energy.");

    s.append("@param n_E_bins The number of \"bins\" to be used between ");
    s.append("min_E and max_E.\n");

    s.append("@param file_name The name of the file to which the array ");
    s.append("of S(Q,E) values are written.  If the named file cannot be ");
    s.append("opened, the file will not be written.  A zero length string ");
    s.append("can be used to disable writing the file.");

    s.append("@return A new DataSet which is the result of converting the ");
    s.append("input DataSet's X-axis units to Q values and its Y-axis units ");
    s.append("to energy loss.\n");
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
  {                                  // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds

                                     // get the E and Q scale parameters
    float  min_Q2    = ( (Float)(getParameter(0).getValue()) ).floatValue();
    float  max_Q2    = ( (Float)(getParameter(1).getValue()) ).floatValue();
    int    n_Q2_bins = ( (Integer)(getParameter(2).getValue()) ).intValue();
    float  min_E     = ( (Float)(getParameter(3).getValue()) ).floatValue();
    float  max_E     = ( (Float)(getParameter(4).getValue()) ).floatValue();
    int    n_E_bins  = ( (Integer)(getParameter(5).getValue()) ).intValue();
    float  e_in      = 0;
    String file_name = ((SaveFilePG)getParameter(6)).getStringValue();

                                        // find out if the units are time (us)
    boolean is_tof;                     // or energy (meV) 
    String x_units = ds.getX_units();
    if ( x_units.equalsIgnoreCase( "meV" ) )
      is_tof = false;
    else if ( x_units.equalsIgnoreCase( "Time(us)" ) )
      is_tof = true;
    else
      return new ErrorString( "Unsupported units in ToQ2E: " + x_units );

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
                      "ERROR: no valid E range in Q2E operator");
      return message;
    }
    else
    {
      if ( ds.getNum_entries() > 0 )
        e_in = AttrUtil.getEnergyIn( ds.getData_entry(0) );
      E_scale = new UniformXScale( e_in - max_E, e_in - min_E, n_E_bins+1 );
    }

                                        // validate Q-squared bounds
    if ( min_Q2 > max_Q2 )              // swap bounds to be in proper order
    {
      float temp = min_Q2;
      min_Q2  = max_Q2;
      max_Q2  = temp;
    }

    if ( n_Q2_bins <= 0 )               // calculate a default
      n_Q2_bins = 100;                  // number of E bins.

    UniformXScale Q2_scale = null;
    if ( min_Q2 >= max_Q2 )             // no valid range specified, so err out
    {
      ErrorString message = new ErrorString(
                      "ERROR: no valid Q2 range in Q2E operator");
      return message;
    }
    else
      Q2_scale = new UniformXScale( min_Q2, max_Q2, n_Q2_bins+1 );

                                               // set up arrays for S(Q2,E),
                                               // error estimates and counters.
    float Q2E_vals[][]   = new float[n_Q2_bins][n_E_bins] ;
    float err_vals[][]   = new float[n_Q2_bins][n_E_bins] ;
    int   n_Q2E_vals[][] = new int[n_Q2_bins][n_E_bins] ;

    int q_index,
        e_index;
    for ( q_index = 0; q_index < n_Q2_bins; q_index++ )
      for ( e_index = 0; e_index < n_E_bins; e_index++ )
      {
        Q2E_vals  [q_index][e_index] = 0.0f;
        err_vals  [q_index][e_index] = 0.0f;
        n_Q2E_vals[q_index][e_index] = 0;
      }

    float            min_ang,
                     max_ang;
    Data             data;
    DetectorPosition position = null;
    float            scattering_angle;
    Float            delta_theta_obj = null;
    float            delta_theta;
    Float            energy_in_obj = null;
    float            q1,
                     q2,
                     qsq_val;
    float            e_val,
                     y_val,
                     err_val;
    float            spherical_coords[];
    AttributeList    attr_list;

    float            x_vals[],
                     center_x;
    float            y_vals[],
                     errors[];

    //
    // Now step along each time-of-flight spectrum, calculate the Q,E
    // coordinates for each bin and map the events in the bin to a point in
    // the Q,E plane.
    //
    int num_data = ds.getNum_entries();
    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry(i);
                                              // first, get the position,
                                              // delta 2theta and initial
                                              // energy attributes for the
                                              // current spectrum
      attr_list = data.getAttributeList();
      position = (DetectorPosition)
                  attr_list.getAttributeValue( Attribute.DETECTOR_POS );
      if ( position == null )
      {
        ErrorString message = new ErrorString(
                    "ERROR: no DETECTOR_POS attribute in Q2E operator");
        return message;
      }
      spherical_coords = position.getSphericalCoords();

      delta_theta_obj = (Float)
                        attr_list.getAttributeValue( Attribute.DELTA_2THETA );
      if ( delta_theta_obj == null )
      {
        ErrorString message = new ErrorString(
                    "ERROR: no DELTA_2THETA attribute in Q2E operator");
        return message;
      }

      e_in = AttrUtil.getEnergyIn( data );

      scattering_angle = position.getScatteringAngle() * (float)(180.0/Math.PI);
      delta_theta = delta_theta_obj.floatValue();

      min_ang = (float)(Math.PI/180.0) * (scattering_angle - delta_theta/2);
      max_ang = (float)(Math.PI/180.0) * (scattering_angle + delta_theta/2);

                                             // Now get the x and y values from
                                             // the spectrum and map them to
                                             // the Q,E plane
      x_vals = data.getX_scale().getXs();
      y_vals = data.getY_values();
      errors = data.getErrors();
      for ( int col = 0; col < y_vals.length; col++ )
      {
        if ( data.isHistogram() )            // use energy at the bin center
          center_x = (x_vals[col] + x_vals[col+1]) / 2.0f;
        else                                 // already at the bin center
          center_x = x_vals[col];

        if ( is_tof )
          e_val = tof_calc.Energy( spherical_coords[0], center_x );
        else
          e_val = e_in - center_x;
                                             // calculate the q-squared values
                                             // at the left and right edges of
                                             // the detector
        q1 = tof_calc.SpectrometerQ( e_in, e_val, min_ang );
        q1 = q1 * q1;

        q2 = tof_calc.SpectrometerQ( e_in, e_val, max_ang );
        q2 = q2 * q2;

        if ( q1 > q2 )
        {
          float temp = q1;
          q1 = q2;
          q2 = temp;
        }                               // use the average Q-squared value
        qsq_val = ( q1 + q2 ) / 2.0f;
                                        // divide by the counts by delta Qsqr,
                                        // to get intensity  ( we assume that
                                        // the values have already been divided
                                        // by delta E and delta Omega as in the
                                        // DSDODE operator
        y_val = y_vals[col] / (q2 - q1);
        if ( errors != null )
          err_val = errors[col] / (q2 - q1);
        else
          err_val = 0;
                                        // now map the y(Q2,E) value to the
                                        // array of values and add it with
                                        // any previous values

        q_index = Math.round(( qsq_val-min_Q2 )/(max_Q2 - min_Q2) *n_Q2_bins);
        e_index = Math.round(( e_val - min_E )/( max_E - min_E ) * n_E_bins);

        if ( q_index > 0 && q_index < n_Q2_bins &&     // the value falls in
             e_index > 0 && e_index < n_E_bins   )     // the range we're using
        {
          Q2E_vals[q_index][e_index]   += y_val;
          n_Q2E_vals[q_index][e_index] += 1;
          err_vals[q_index][e_index]   += err_val * err_val; // accumulate sum
        }                                                    // of variances
      }
    }

   int n_samples;
   for ( int row = 0; row < n_Q2_bins; row++ )
     for ( int col = 0; col < n_E_bins; col++ )
       if ( n_Q2E_vals[row][col] > 0 )
       {
         n_samples = n_Q2E_vals[row][col]; 
         Q2E_vals[row][col] /= n_samples; 
         err_vals[row][col]  = (float)Math.sqrt( err_vals[row][col] );
         err_vals[row][col] /= n_samples;
       }

                                        // this version uses cuts at constant E
   DataSetFactory factory = new DataSetFactory(
                                     ds.getTitle(),
                                     "(Inv("+FontUtil.ANGSTROM+"^2"+"))",
                                     "Q-squared",
                                     "Counts",
                                     "Scattering Intensity" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet();
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to S(Q2,E)" );

    // copy the attributes of the original data set
    new_ds.setAttributeList( ds.getAttributeList() );

    Data new_data;
                                               // This version creates DataSet 
                                               // using cuts at constant E.
    for ( int col = 0; col < n_E_bins; col++ ) // make data entries from columns
    {                                          // i.e. using constant E values

      float const_e_slice[] = new float[ n_Q2_bins ];
      float slice_errors[]  = new float[ n_Q2_bins ];
      for ( int row = 0; row < n_Q2_bins; row++ )
      {
        const_e_slice[row] = Q2E_vals[row][col];
        slice_errors[row]  = err_vals[row][col];
      }

      new_data = Data.getInstance(Q2_scale, const_e_slice, slice_errors, col+1);
      e_val = col * (max_E - min_E) / n_E_bins + min_E;
      Attribute e_attr = new FloatAttribute( Attribute.ENERGY_TRANSFER, 
                                             e_in - e_val );  
      new_data.setAttribute( e_attr );
      new_data.setLabel( Attribute.ENERGY_TRANSFER );

      new_ds.addData_entry( new_data );
    }
  
    if ( file_name != null && file_name.length() > 0 )
    {
      ErrorString err = PrintToFile( file_name, 
                                     Q2_scale, 
                                     E_scale, 
                                     Q2E_vals, 
                                     err_vals );
      if ( err != null )
      {
        SharedData.addmsg( "Didn't write S(Q2,E), " + new_ds + ", to file" );
        return new_ds;
      }
      else
        SharedData.addmsg( "Wrote S(Q2,E), " + new_ds + ", to file "+file_name);
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
    SpectrometerTofToQ2E new_op = new SpectrometerTofToQ2E( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


  /* ---------------------------- PrintToFile ------------------------------ */
  /*
   *  Private method to print the array of S(Q,E) values to a file
   */

  private ErrorString PrintToFile( String file_name, 
                                   XScale Q_scale, 
                                   XScale E_scale, 
                                   float  Q2E_vals[][],
                                   float  errors[][] )
  {
     FileOutputStream fout= null;
     try 
     {
        fout = new FileOutputStream( file_name );
     }
     catch( Exception ss)
     {
       return new ErrorString("Could not open output file:" + file_name);
     }

     StringBuffer buff = new StringBuffer( 1000 );
     try
     {
       buff.append("# Row:    " + Q2E_vals[0].length + "\n");
       buff.append("# Column: " + Q2E_vals.length + "\n");
       buff.append("# X Label: Momentum Transfer \n");
       buff.append("# X Units: inv(A) \n");
       buff.append("# Y Label: Energy Transfer \n");
       buff.append("# Y Units: meV \n");
       buff.append("# Z Label: Intensity \n");
       buff.append("# Z Units: Arb. Units\n");
       float x   = 0;
       float y   = 0;
       float val = 0;
       float err = 0;

       int max_row = Q2E_vals.length - 1;
       int max_col = Q2E_vals[0].length - 1;
       for( int row = 0; row <= max_row; row++)
         for( int col = 0; col <= max_col; col++)
         { 
            x = Q_scale.getX( row );
            y = E_scale.getX( col );
            val = Q2E_vals[row][max_col - col];     // Data is inverted
            err = errors[row][max_col - col];
            buff.append( Format.real(x,15,5) );
            buff.append( Format.real(y,15,5) );
            buff.append( Format.real(val,15,5) );
            buff.append( Format.real(err,15,5) + "\n" );

            fout.write( buff.toString().getBytes());
            buff.setLength(0);
          }
         fout.close();
      }
      catch( Exception e )
      { 
        return new ErrorString("Error: " + e.toString() );
      }
      return null;
  }


  /* --------------------------- main ----------------------------------- */
  /*
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
    float minQ2, maxQ2, minE, maxE;
    int Ebins, Q2bins;

    minE  = 4.0f;
    maxE  = 250f;
    Ebins = 500;
    minQ2 = 0.225f;
    maxQ2 = 3.2f;
    Q2bins = 500;

    String file_name = "/home/groups/SCD_PROJECT/SampleRuns/hrcs2447.run ";
                       //"D:\\ISAW\\SampleRuns\\hrcs2447.run ";
    try
    {
       RunfileRetriever rr = new RunfileRetriever( file_name );
       DataSet ds1 = rr.getDataSet(1);
        new ViewManager(ds1, IViewManager.IMAGE);
       SpectrometerTofToQ2E op = new SpectrometerTofToQ2E
                          (ds1, minQ2, maxQ2, Q2bins, minE, maxE, Ebins, null);

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

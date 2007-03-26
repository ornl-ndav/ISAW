/*
 * File:  FitSineWave.java
 *
 * Copyright (C) 2007, Julian Tao
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
 * Contact : Julian Tao <taoj@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 */
package Operators.Special;

import java.util.*;

import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.MathTools.Functions.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.math.*;
import DataSetTools.util.*;
import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This class fits a sine function/wave (with a hardcoded fixed period)
 *   to data in a specified interval of a specified Data block 
 *  of a DataSet. A vector containing chi_square, the names and values of
 *  the fitted parameters, together with error estimates for the fitted
 *  parameters, and a DataSet with the fitted curve is returned.
 *  
 */

public class FitSineWave implements Wrappable, IWrappableWithCategoryList
{
  public DataSet  data_set = DataSet.EMPTY_DATA_SET;
  public int      group_id;
  public float   min_x;
  public float   max_x;
  public int      npts;
  public double   amp;
  public double   pha;
  public double   bkg;

  private boolean debug_flag = false;

  /**
   *  Get the command name to be used in scripts.
   *  @return The command name 'FitSine'
   */
  public String getCommand() 
  {
    return "FitSine";
  }


 /* ---------------------------- getCategoryList -------------------------- */
 /**
  *  Get the list of categories describing where this operator should appear
  *  in the menu system.
  *
  *  @return an array of strings listing the menu where the operator 
  *  should appear.
  */
  public String[] getCategoryList()
  {
    return Operator.DATA_SET_ANALYZE_MACROS;
  }


  /**
   */
  public String getDocumentation() 
  {
    StringBuffer s = new StringBuffer();
    s.append( "@overview This operator fits a fixed period sine wave to data" );
    s.append( "in a specified interval of a specified" );
    s.append( "Data block of a DataSet." );

    s.append( "@algorithm The average value on the specified " );
    s.append( "interval is used as initial estimate for the " );
    s.append( "background value; the difference between the maximum and background ");
    s.append( "for the amplitude value; phase value starts at zero. ");
    s.append( "A Marquardt type algorithm is used to " );
    s.append( "optimize these parameters to fit the data. " );
    s.append( "The operator also prints out tau and qbar information for the MISANS experiments." );

    s.append( "@param data_set  The DataSet containing the Data block " );
    s.append( "to fit." );
    s.append( "@param group_id  The ID for the Data block " );
    s.append( "to fit." );
    s.append( "@param min_x  The left hand endpoint of an interval " );
    s.append( "containing the single peak to fit." );
    s.append( "@param max_x  The right hand endpoint of an interval " );
    s.append( "containing the single peak to fit." );
    s.append( "@param npts   The number of data points to rebin into " );
    s.append( "@return  A vector is returned that contains a mix of " );
    s.append( "descriptive strings and values calculated during the " );
    s.append( "fitting process. " );
    s.append( "@error If an error occurs during processing, an ");
    s.append( "error string indicating the cause of the error is ");
    s.append( "returned.");
    s.append( "If the fit just failed to converge, check the data");
    s.append( "to see if it is valid, or too noisy to do a");
    s.append( "meaningful fit.");
    return s.toString();
  }


  /**
   *  This operator uses a Marquardt type optimization routine to fit a
   *  single peak on an interval of a Data block.  A vector is returned
   *  containing string names, values and error estimates for the 
   *  computed values. 
   *  
   *  @return a vector containing the following strings and values in the
   *  order listed.
   *    "ChiSqr"
   *     chi_sqr_value
   *    "Amplitude"
   *     amplitude_value
   *     error_in_amplitude
   *    "Phase"
   *     phase_value
   *     error_in_phase
   *    "Background"
   *     background_value
   *     error_in_background
   *    "Fit DataSet"
   *     DataSet_with_fit_function
   */
  public Object calculate() 
  {
    if ( data_set == null )
      return new ErrorString("Null DataSet in FitSineWave");

    if ( data_set == DataSet.EMPTY_DATA_SET )
      return new ErrorString("Empty DataSet in FitSineWave");

    int n_data = data_set.getNum_entries();
    if ( n_data <= 0 )
      return new ErrorString("No Data blocks in DataSet in FitSineWave"); 
    Data d = data_set.getData_entry_with_id( group_id );
    if ( d == null )
      return new ErrorString("ID " + group_id + 
                             " not in DataSet in FitSineWave");

    d.resample(new UniformXScale(min_x, max_x, npts+1), IData.SMOOTH_NONE);
    XScale x_scale = d.getX_scale();
    float xbar = (min_x + max_x)/2;
    
    DetectorPosition dp = (DetectorPosition)d.getAttributeValue(Attribute.DETECTOR_POS);
    float d1 = ((Float)d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
    float scattering_angle = dp.getScatteringAngle();
    float d2 = dp.getDistance();
    float qbar = tof_calc.DiffractometerQ(scattering_angle, d1+d2, xbar);
    float tau = 0.66f*(float)Math.pow(xbar/(d1+d2), 3);
//    System.out.println("qbar: "+qbar+" tau: "+tau);
    
    if ( min_x < x_scale.getStart_x() )
      return new ErrorString("Interval extends below x_min in FitSineWave");
  
    if ( max_x > x_scale.getEnd_x() )
      return new ErrorString("Interval extends above x_max in FitSineWave");
 
    float xf[]     = d.getX_values();
    float yf[]     = d.getY_values();
    float sigmaf[] = d.getErrors();
    
    int min_i = x_scale.getI_GLB( (float)min_x );
    int max_i = x_scale.getI( (float)max_x )-1;
    int n_pts = max_i - min_i + 1;

    min_x = xf[ min_i ];                        // reset min & max x to actual
    max_x = xf[ max_i ];                        // points on the XScale

//    System.out.println("min_x: "+min_x+" max_x: "+max_x+" min_i: "+min_i+" max_i: "+max_i+" xf.length: "+xf.length);
    boolean is_hist = d.isHistogram();
    if ( is_hist && max_i > xf.length - 2 )
      return new ErrorString("Histogram points above x_max in FitSineWave");
     
    if ( n_pts < 10 )
      return new ErrorString("Too few points in interval(" + n_pts + ")" );

    double x[] = new double[ n_pts ];
    double y[] = new double[ n_pts ];
    double sigma[] = new double[ n_pts ];
    for ( int i = min_i; i <= max_i; i++ )
    {
      if ( is_hist )
        x[i - min_i] = (xf[i] + xf[i+1])/2;     // histogram, so use bin center
      else
        x[i - min_i] = xf[i];                   // function, so use x value

      y[i - min_i ]     = yf[i];
      sigma[i - min_i ] = sigmaf[i];
    }

    // estimate initial parameter values
    int index_of_max = 0;
    double ybar = 0.0;
    for ( int i = 1; i < n_pts; i++ ) {
      ybar += y[i];
      if ( y[i] > y[index_of_max] )
        index_of_max = i; 
    }
    ybar /= n_pts;
    bkg = ybar;
    amp = y[ index_of_max ] - bkg;

    if ( debug_flag )
    {
      System.out.println("n_pts = " + n_pts );
      System.out.println("Max at " + index_of_max );
      System.out.println("Estimated amplitude: " + amp + " background: " + bkg );
    }

    SineFixedPeriod function = new SineFixedPeriod ( amp, pha, bkg);   
                 
    ClosedInterval interval = new ClosedInterval((float)min_x, (float)max_x );
    function.setDomain( interval );

    double coefs[];
    String names[];
    if ( debug_flag )
    {
      System.out.println("Before fit, initial params are" );
      coefs = function.getParameters();
      names = function.getParameterNames();
      for ( int i = 0; i < function.numParameters(); i++ )
        System.out.println(names[i] + " = " + coefs[i] );
    }

    CurveFitter fitter = new MarquardtArrayFitter( function, x, y, sigma, 
                                                   1.0e-8, 100 );

    double chi_sqr    = fitter.getChiSqr();
    double p_sigmas[] = fitter.getParameterSigmas();
    coefs = function.getParameters();
    names = function.getParameterNames();
    if ( debug_flag )
    {
      System.out.println("Chi Sq = " + chi_sqr );
      for ( int i = 0; i < function.numParameters(); i++ )
        System.out.println(names[i] + " = " + coefs[i] + 
                           " +- " + p_sigmas[i] );
    }

    if ( debug_flag )                          // add model function to DataSet
    {
      data_set.addLog_entry( "Added model function for peak at " + coefs[0] );
      FunctionModel model = new FunctionModel( x_scale, function, group_id ); 
      data_set.addData_entry( model );
      data_set.notifyIObservers( IObserver.DATA_CHANGED );
    }
   
    boolean failed = false;
    Vector parameters = new Vector();
    parameters.addElement( "ChiSq" );
    parameters.addElement( new Float( chi_sqr) );
    if ( BadValue(chi_sqr) )
      failed = true;

    for ( int i = 0; i < coefs.length; i++ )
    {
      parameters.addElement( names[i] );
      parameters.addElement( new Float( coefs[i] ) );
      parameters.addElement( new Float( p_sigmas[i] ) );
      if ( BadValue( coefs[i] ) || BadValue( p_sigmas[i] ) )
        failed = true;
    }

/*
    parameters.addElement( "Background Function" );
    parameters.addElement( lin_funct );
*/

    if ( failed )
      return new ErrorString( "Fit failed to converge, check data.");
    else
    {
      DataSet fitted_ds = data_set.empty_clone();
      XScale model_scale  = x_scale.restrict( interval ); 
      FunctionModel model = new FunctionModel( model_scale, function,
                                               group_id + 1000000 );
      fitted_ds.addData_entry( model );

      Data new_d = (Data)d.clone();
      new_d.resample( model_scale, IData.SMOOTH_NONE ); 
      fitted_ds.addData_entry( new_d );

      fitted_ds.addLog_entry( "Fitted ID " + group_id );
      fitted_ds.setTitle( "Fitted ID " + group_id + 
                          " for " + fitted_ds.getTitle() );

      if ( debug_flag )
      {
        System.out.println("Requested interval = " + min_x + " to " + max_x );
        System.out.println("model_scale = " + model_scale );
        float new_x[] = model_scale.getXs();
        for ( int i = 0; i < n_pts; i++ )
          System.out.println( "old_x = " + xf[i+min_i] +
                              "new_x = " + new_x[i] );
      }

      SharedData.addmsg(tau+"\t"+qbar+"\t"+coefs[0]+"\t"+p_sigmas[0]+"\t"+coefs[2]+"\t"+p_sigmas[2]+"\t"+chi_sqr);
      
      parameters.addElement( "Fit DataSet" );
      parameters.addElement( fitted_ds );
      return parameters;
    }
  }

  /**
   *  Check for infinite or NaN values.
   *
   *  @param value the double value to check for validity
   *
   *  @return True if the value is infinite or NaN, false otherwise.
   */
   private boolean BadValue( double value )
   {
     if ( Double.isNaN( value ) )
       return true;

     if ( Double.isInfinite( value ) )
       return true;

     return false;
   }

  /**
   *
   *  Main program for testing purposes.
   *
   */
  public static void main( String args[] )
  {
    String  file_name   = "/IPNShome/sasi/data/sasi2629.run";
    RunfileRetriever rr = new RunfileRetriever( file_name );

    DataSet ds = rr.getDataSet( 1 );
//    new ViewManager( ds, IViewManager.IMAGE );

    FitSineWave op_core = new FitSineWave();
    op_core.data_set = ds;
    op_core.group_id = 499;
    op_core.min_x    = 7400;
    op_core.max_x    = 7600;
    op_core.npts     = 200;
    Operator fit_op = new JavaWrapperOperator( op_core );

    Object result = fit_op.getResult();
    Vector values = null;
    if ( result instanceof ErrorString )
      System.out.println("Error occured: " + ((ErrorString)result).toString());
    else if ( result instanceof Vector )
    {
      values = (Vector)result;
      for ( int i = 0; i < values.size(); i++ )
        System.out.println("" + values.elementAt(i) );
    }

    DataSet fitted_ds = (DataSet)values.elementAt( values.size()-1 );
    new ViewManager( fitted_ds, IViewManager.IMAGE );
  }

}

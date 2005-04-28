/*
 * File:  FitGaussianPeak.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.8  2005/04/28 18:59:28  dennis
 * Now makes an initial estimate of the FWHM by checking values
 * to the left and right of the peak value until values less than
 * half the peak amplitude are found.
 *
 * Revision 1.7  2005/04/22 17:29:47  dennis
 * The returned vector now has a new DataSet containing the fit function that
 * was constructed, as the last entry in the returned vector.
 * Vector now has separate strings for the phrase "Background Function"
 * and the actual string defining the background function as: m*(x-x0)+y0
 * where x0 is replaced by a constant near the peak, and m and y0 are
 * the parameters for the linear background that are returned in the
 * vector.
 * Updated documentation to include info on the linear background and
 * returned DataSet.
 * Updated main test program to also display the returned fit function
 * DataSet.
 *
 * Revision 1.6  2005/04/11 22:00:32  dennis
 * Now checks to make sure that all computed parameters are
 * valid finite numbers.  If not, an error string is returned.
 *
 * Revision 1.5  2005/04/11 02:27:09  dennis
 * Changed tolerance parameter to fitter to 10e-8, instead of 10e-20,
 * and changed max number of steps to 100, instead of 500.  This
 * allows the fitting process to terminate with essentially the same
 * values as before in a few tens of steps, rather than always going
 * through 500 iterations, attempting to meet the unrealistic tolerance.
 * This should speed up programs that spent most of their time in the
 * fit, by a factor of 20-50.
 *
 * Revision 1.4  2005/04/08 21:59:52  dennis
 * The fitted function now includes a linear background.
 * The coefficients of the linear function and the expression
 * defining the linear function are now added on to the end
 * of the returned Vector.
 * Set debug flag to false.
 *
 * Revision 1.3  2005/04/08 21:08:31  dennis
 * Changed to return Floats instead of Doubles in the return
 * Vector.
 *
 * Revision 1.2  2005/04/08 19:03:54  dennis
 * Added basic main program for testing.
 * Fixed copyright date and NSF grant number.
 *
 * Revision 1.1  2005/04/06 03:09:05  dennis
 * Initial version of operator to fit a Gaussian to an
 * isolated peak.
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

/**
 *  This class fits a Gaussian to a peak plus linear background to data
 *  in a specified interval of a specified Data block of a DataSet.  
 *  A vector containing chi_square, the names and values of the fitted 
 *  parameters, together with error estimates for the fitted parameters,
 *  and a DataSet with the fitted curve is returned. 
 */
public class FitGaussianPeak implements Wrappable
{
  public DataSet  data_set = DataSet.EMPTY_DATA_SET;
  public int      group_id = 49;
  public double   min_x = 22140;
  public double   max_x = 24440;

  private boolean debug_flag = false;

  /**
   *  Get the command name to be used in scripts.
   *  @return The command name 'FitGaussian'
   */
  public String getCommand() 
  {
    return "FitGaussian";
  }


  /**
   */
  public String getDocumentation() 
  {
    StringBuffer s = new StringBuffer();
    s.append( "@overview This operator fits a Gaussian to a single " );
    s.append( "isolated peak in a specified interval of a specified" );
    s.append( "Data block of a DataSet." );

    s.append( "@algorithm The maximum value on the specified " );
    s.append( "interval is used for the initial estimate for the " );
    s.append( "peak position.  The maximum value is used for the " );
    s.append( "initial estimate of the peak amplitude " );
    s.append( "A Marquardt type algorithm is used to " );
    s.append( "optimize the parameters for a Gaussian function " );
    s.append( "to fit the data." );

    s.append( "@param data_set  The DataSet containing the Data block " );
    s.append( "to fit." );
    s.append( "@param group_id  The ID for the Data block " );
    s.append( "to fit." );
    s.append( "@param min_x  The left hand endpoint of an interval " );
    s.append( "containing the single peak to fit." );
    s.append( "@param max_x  The right hand endpoint of an interval " );
    s.append( "containing the single peak to fit." );

    s.append( "@return  A vector is returned that contains a mix of " );
    s.append( "descriptive strings and values calculated during the " );
    s.append( "fitting process.  In particular, the vector contains, " );
    s.append( "in order: \n" );
    s.append( " 'ChiSqr'\n" );
    s.append( "  chi_sqrr_value\n" );
    s.append( " 'Position'\n" );
    s.append( "  position_value\n" );
    s.append( "  error_in_position\n" );
    s.append( " 'Amplitude'\n" );
    s.append( "  amplitude_value\n" );
    s.append( "  error_in_amplitude\n" );
    s.append( " 'FWHM'\n" );
    s.append( "  fwhm_value\n" );
    s.append( "  error_in_fwhm\n" );
    s.append( " 'y0'\n" );
    s.append( "  y0_value\n" );
    s.append( "  error_in_y0\n" );
    s.append( " 'm'\n" );
    s.append( "  m_value\n" );
    s.append( "  error_in_m\n" );
    s.append( "  Background Function\n");
    s.append( "  m*(x-x0)+y0\n");
    s.append( "  Fit DataSet");
    s.append( "  DataSet_with_fit_function");

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
   *    "Position"
   *     position_value
   *     error_in_position
   *    "Amplitude"
   *     amplitude_value
   *     error_in_amplitude
   *    "FWHM"
   *     fwhm_value
   *     error_in_fwhm
   *    "y0"
   *     y0_value
   *     error_in_y0
   *    "m"
   *     m_value
   *     error_in_m
   *    "Background Function"
   *    "m*(x-x0)+y0\n"
   *    "Fit DataSet"
   *     DataSet_with_fit_function
   */
  public Object calculate() 
  {
    if ( data_set == null )
      return new ErrorString("Null DataSet in FitGaussianPeak");

    if ( data_set == DataSet.EMPTY_DATA_SET )
      return new ErrorString("Empty DataSet in FitGaussianPeak");

    int n_data = data_set.getNum_entries();
    if ( n_data <= 0 )
      return new ErrorString("No Data blocks in DataSet in FitGaussianPeak"); 
    Data d = data_set.getData_entry_with_id( group_id );
    if ( d == null )
      return new ErrorString("ID " + group_id + 
                             " not in DataSet in FitGaussianPeak");

    XScale x_scale = d.getX_scale();
    if ( min_x < x_scale.getStart_x() )
      return new ErrorString("Interval extends below x_min in FitGaussianPeak");
  
    if ( max_x > x_scale.getEnd_x() )
      return new ErrorString("Interval extends above x_max in FitGaussianPeak");
 
    float xf[]     = d.getX_values();
    float yf[]     = d.getY_values();
    float sigmaf[] = d.getErrors();
    
    int min_i = x_scale.getI_GLB( (float)min_x );
    int max_i = x_scale.getI( (float)max_x );
    int n_pts = max_i - min_i + 1;

    boolean is_hist = d.isHistogram();
    if ( is_hist && max_i > xf.length - 2 )
      return new ErrorString("Histogram points above x_max in FitGaussianPeak");
     
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

    // find max on interval and use it as the initial estimate for the 
    // peak position
    int index_of_max = 0;
    for ( int i = 1; i < n_pts; i++ )
      if ( y[i] > y[index_of_max] )
        index_of_max = i; 
    double amplitude = y[ index_of_max ];

    if ( debug_flag )
    {
      System.out.println("n_pts = " + n_pts );
      System.out.println("Max of " + amplitude + " at " + index_of_max );
    }

    // find estimate for fwhm
    int k = index_of_max;
    while ( k > 0 && y[ k ] > amplitude/2 )
      k--;
    int fwhm_i_min = k;

    k = index_of_max;
    while ( k < y.length-1 && y[ k ] > amplitude/2 )
      k++;
    int fwhm_i_max = k;

    double fwhm = x[fwhm_i_max] - x[fwhm_i_min]; 
    Gaussian g1 = new Gaussian( x[index_of_max], amplitude, fwhm);

    String p_names[] = { "y0", "m" };
    double p_vals[]  = { 1, 0 };
    String x0_str    = (new Double( x[index_of_max] ) ).toString();
    String lin_funct = "m * (x- " + x0_str + ") + y0";
    Expression e1 = new Expression( lin_funct, "x", p_names, p_vals );
   
    ClosedInterval interval = new ClosedInterval((float)min_x, (float)max_x );
    e1.setDomain( interval );

    IOneVarParameterizedFunction funs[] = new IOneVarParameterizedFunction[2];
    funs[0] = g1;
    funs[1] = e1;

    SumFunction function = new SumFunction( funs );
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
    parameters.addElement( "Background Function" );
    parameters.addElement( lin_funct );

    if ( failed )
      return new ErrorString( "Fit failed to converge, check data.");
    else
    {
      DataSet fitted_ds = data_set.empty_clone();
      FunctionModel model = new FunctionModel( x_scale, function, group_id );
      fitted_ds.addData_entry( model );
      fitted_ds.addLog_entry( "Fitted ID " + group_id );
      fitted_ds.setTitle( "Fitted ID " + group_id + 
                          " for " + fitted_ds.getTitle() );
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
    String  file_name   = "/usr2/ARGONNE_DATA/gppd12358.run";
    RunfileRetriever rr = new RunfileRetriever( file_name );

    DataSet ds = rr.getDataSet( 1 );
    new ViewManager( ds, ViewManager.IMAGE );

    FitGaussianPeak op_core = new FitGaussianPeak();
    op_core.data_set = ds;
    op_core.group_id = 81;
    op_core.min_x    = 11700;
    op_core.max_x    = 13000;
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
    new ViewManager( fitted_ds, ViewManager.IMAGE );
  }

}

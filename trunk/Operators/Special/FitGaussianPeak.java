/*
 * File:  FitGaussianPeak.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.1  2005/04/06 03:09:05  dennis
 * Initial version of operator to fit a Gaussian to an
 * isolated peak.
 *
 */
package Operators.Special;

import java.util.*;

import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.MathTools.Functions.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;

/**
 *  This class fits a Gaussian to a peak in a specified interval of a 
 *  specified Data block of a DataSet.  A vector containing chi_square,
 *  the names and values of the fitted parameters, together with error
 *  estimates for the fitted parameters is returned. 
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
    s.append( "  chi_sqr\n" );
    s.append( " 'Position'\n" );
    s.append( "  position\n" );
    s.append( "  error_in_position\n" );
    s.append( " 'Amplitude'\n" );
    s.append( "  amplitude\n" );
    s.append( "  error_in_amplitude\n" );
    s.append( " 'FWHM'\n" );
    s.append( "  fwhm\n" );
    s.append( "  error_in_fwhm\n" );
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
   *     chi_sqr
   *    "Position"
   *     position
   *     error_in_position
   *    "Amplitude"
   *     amplitude
   *     error_in_amplitude
   *    "FWHM"
   *     fwhm
   *     error_in_fwhm
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
    
    System.out.println("n_pts = " + n_pts );
    System.out.println("Max of " + y[index_of_max] + " at " + index_of_max );

    double fwhm = (max_x - min_x)/10;
    double amplitude = y[ index_of_max ];
    Gaussian function = new Gaussian( x[index_of_max], amplitude, fwhm);

    CurveFitter fitter = new MarquardtArrayFitter( function, x, y, sigma, 
                                                   1.0e-20, 500 );

    double chi_sqr    = fitter.getChiSqr();
    double p_sigmas[] = fitter.getParameterSigmas();
    double coefs[] = function.getParameters();
    String names[] = function.getParameterNames();
    System.out.println("Chi Sq = " + chi_sqr );
    for ( int i = 0; i < function.numParameters(); i++ )
      System.out.println(names[i] + " = " + coefs[i] + 
                         " +- " + p_sigmas[i] );

    if ( debug_flag )                          // add model function to DataSet
    {
      FunctionModel model = new FunctionModel( x_scale, function, 3 ); 
      data_set.addData_entry( model );
      data_set.notifyIObservers( IObserver.DATA_CHANGED );
    }
   
    Vector parameters = new Vector();
    parameters.addElement( "ChiSq" );
    parameters.addElement( new Double( chi_sqr) );
    for ( int i = 0; i < coefs.length; i++ )
    {
      parameters.addElement( names[i] );
      parameters.addElement( new Double( coefs[i] ) );
      parameters.addElement( new Double( p_sigmas[i] ) );
    }

    return parameters;
  }
}

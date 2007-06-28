/*
 * File:  SelfShieldingCalc.java 
 *
 * Copyright (C) 2007, Dennis Mikkelson
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
 *  Revision 1.1  2007/06/28 20:55:01  dennis
 *  Class with static methods for self-shielding calculation
 *  for direct geometry spectrometers, such as HRMECS.
 *
 */ 
package Operators.TOF_DG_Spectrometer;

import java.util.*;

import gov.anl.ipns.MathTools.Geometry.*;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import DataSetTools.math.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;

/**
 *  This class contains static methods for calculating the self-shielding
 *  of a sample in a direct geometry spectrometer, such as the HRMECS
 *  instrument at IPNS.
 */
public class SelfShieldingCalc
{
  /**
   *  Private constructor so no one can instantiate this class.
   */
  private SelfShieldingCalc()
  {
    // This class should just contain static methods.  Don't let anyone
    // instantiate it.
  }
  
  /**
   *  Correct the specified spectrometer DataSet for self-shielding by the
   *  sample.  The DataSet must be in terms of EnergyLoss or time-of-flight
   *
   *  @param  ds           The DataSet to correct for self-shielding
   *  @param  mutS         Inverse scattering length * sample thickness
   *  @param  mutA         Inverse absorption length * sample thickness
   *  @param  gamma        Acute angle between a vector perpendicular to
   *                       the sample surface and the beam direction.
   *  @param  make_new_ds  Flag indicating whether the corrected data should
   *                       be placed in a new DataSet or if the current 
   *                       DataSet should be altered to have the corrected data
   * 
   */
  public static Vector SelfShielding( DataSet ds, 
                                      float   mutS,
                                      float   mutA,
                                      float   gamma,
                                      boolean make_new_ds )
  {
    if ( ds == null )
      throw new IllegalArgumentException("DataSet is null");

    if ( ds.getNum_entries() <= 0 )
      throw new IllegalArgumentException("DataSet is empty");

    Data    data;
    DataSet new_ds;
    float[] y_vals;
    float[] x_vals;
    float[] err_vals;
    float[] self_vals;
    float[] energy_vals;
    double  phi,
            phi_rad;
    double  self;
    double  Ei;
    DetectorPosition pos;

    if ( make_new_ds )
      new_ds = (DataSet)ds.clone();
    else
      new_ds = ds;

    DataSetFactory ds_factory = new DataSetFactory( "Self-Shielding Factors",
                                                    ds.getX_units(),
                                                    ds.getX_label(),
                                                    "number",
                                                    "self factor" );
    DataSet self_ds = ds_factory.getDataSet();

    String units = ds.getX_units();
    boolean time_of_flight = false;
    if ( units.equalsIgnoreCase( "time(us)" ) )
      time_of_flight = true;

    int num_data = ds.getNum_entries();
    for (int index = 0; index < num_data; index++ )
    {
      data     = new_ds.getData_entry( index );
      y_vals   = data.getY_values();
      x_vals   = data.getX_values();
      err_vals = data.getErrors();

      Ei      = AttrUtil.getEnergyIn( data );
      pos     = AttrUtil.getDetectorPosition( data );
      phi_rad = pos.getScatteringAngle();
      phi     = phi_rad * 180.0 / Math.PI;

      if ( data.isHistogram() )                          // use bin centers
        for ( int i = 0; i < x_vals.length-1; i++ )
          x_vals[i] = (x_vals[i] + x_vals[i+1])/2;

      energy_vals = new float[ y_vals.length ];          // find energy at Xs
      float length = pos.getDistance();           
      if ( time_of_flight )                              // convert to ENERGY
        for ( int i = 0; i < energy_vals.length; i++ )
          energy_vals[i] = tof_calc.Energy( length, x_vals[i] );
      else
        for ( int i = 0; i < energy_vals.length; i++ )   // assume x_vals are 
          energy_vals[i] = (float)(Ei - x_vals[i]);      // ENERGY LOSS so also
                                                         // convert to ENERGY
      self_vals   = new float[ y_vals.length ];
      for ( int i = 0; i < y_vals.length; i++ )
      {
        self = SelfShielding( energy_vals[i], Ei, phi, mutS, mutA, gamma );

        y_vals[i]     *= self;
        err_vals[i]   *= self;

        self_vals[i]   = (float)self;
      }
                                                  // y_vals are changed by
      ((TabulatedData)data).setErrors(err_vals);  // reference, but we need
                                                  // to explicitly set the
                                                  // new error values

      float[] new_x_vals = new float[self_vals.length]; // now make a new Data
      for ( int i = 0; i < new_x_vals.length; i++ )     // block for the self
        new_x_vals[i] = x_vals[i];                      // shieldling factors
                                                        // relative to original
                                                        // x_vals.
      Data d = new FunctionTable( new VariableXScale(new_x_vals),
                                  self_vals,
                                  index );       
      self_ds.addData_entry( d );
    }

    new_ds.addLog_entry("Applied Self Shielding correction using \n"+
                        " mutS  = " + mutS  + "\n" +
                        " mutA  = " + mutA  + "\n" +
                        " gamma = " + gamma + "\n" );

    self_ds.addLog_entry("Created DataSet using Energy Loss values \n" +
                         "as the X-values and the calculated self \n"  +
                         "shielding factors for the Y-values." );

    Vector results = new Vector();
    results.add( new_ds );
    results.add( self_ds );
    return results;
  }


  /**
   *  Calculate a correction factor for self-shielding by the sample, for 
   *  one specific energy level.
   *
   *  @param  energy       The energy level at which the self-shielding should
   *                       be calculated
   *  @param  Ei           Incident Energy
   *  @param  phi          Scattering angle
   *  @param  mutS         Inverse scattering length * sample thickness
   *  @param  mutA         Inverse absorption length * sample thickness
   *  @param  gamma        Acute angle between a vector perpendicular to
   *                       the sample surface and the beam direction.
   *
   *  @return The calculated self-shielding factor.
   */
  public static double SelfShielding( double  energy,
                                      double  Ei,
                                      double  phi,
                                      double  mutS,
                                      double  mutA,
                                      double  gamma )
  {
    double self = 0;                           // the calculated self shielding
    double muti,
           mutf;

    double gamma_rad = gamma * Math.PI/180;   // radian version of angles for
    double phi_rad   = phi   * Math.PI/180;   // Math.cos() function

    if ( mutA == 0 && mutS == 0 )
      self = 1.0;                              // default to self = 1

    else if ( gamma < 89.9 || gamma > 90.1 )   // do the full calculation
    {
      muti = ( mutS + mutA * Math.sqrt(25.299/Ei)) / Math.cos(gamma_rad);
      if ( phi-gamma < 89.9 )
      {
        mutf = (mutS+mutA*Math.sqrt(25.299/energy))/
                Math.cos( phi_rad - gamma_rad );
        self = (mutf-muti)/(Math.exp(-muti)-Math.exp(-mutf));
      }
      else if ( phi-gamma > 90.1 )
      {
        mutf = (mutS+mutA*Math.sqrt(25.299/energy))/
                Math.cos( phi_rad - gamma_rad );
        self = (mutf-muti)/(Math.exp(mutf-muti)-1.0);
      }
    }

    return self;
  }

  /**
   *  Main program for basic functionality tests.
   */
  public static void main( String args[] )
  {
    double Ei     = 500;
    double EPS    =  10;
    double phi    =  60;
    float  mutS   = 0.1f;
    float  mutA   = 0.2f;
    float  gamma  =  45;
    double energy = Ei - EPS;

    double self = SelfShielding( energy, Ei, phi, mutS, mutA, gamma );
    System.out.println( "self shielding factor = " + self );
    // result should be: 1.1944503488285683

    String filename = "/usr2/HRCS_TEST/hrcs3084.run";
    RunfileRetriever rr = new RunfileRetriever( filename );
    DataSet ds = rr.getDataSet(1);
    Operator ToEL = new SpectrometerTofToEnergyLoss( ds, 0, 0, 0 );
    ds = (DataSet)ToEL.getResult();
    new ViewManager( ds, IViewManager.IMAGE );

    Vector result = SelfShielding( ds, mutS, mutA, gamma, true );

    DataSet ss_ds_1 = (DataSet)result.elementAt(0); 
    new ViewManager( ss_ds_1, IViewManager.IMAGE );

    DataSet ss_ds_2 = (DataSet)result.elementAt(1); 
    new ViewManager( ss_ds_2, IViewManager.IMAGE );
  }

}

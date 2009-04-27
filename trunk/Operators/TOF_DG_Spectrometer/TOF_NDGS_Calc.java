/*
 * File: TOF_NDGS_Calc.java
 *
 * Copyright (C) 2009, Dennis Mikkelson
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
 * For further information, see <http://ftp.sns.gov/ISAW/>
 *
 *   Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */
package Operators.TOF_DG_Spectrometer;

import java.util.*;

import gov.anl.ipns.MathTools.Geometry.*;

import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.math.*;

import Operators.Special.*;

/**
 *  This class contains static methods for direct geometry neutron 
 *  spectrometers.  The public static methods can be wrapped to generate
 *  ISAW operators. 
 */
public class TOF_NDGS_Calc
{
  public static final int MIN_DELAY_CHANNELS = 10;

  /**
   *  Switch the specified TOF_NDGS DataSet so the Data block's time-of-flight
   *  axes all specify the time-of-flight from the sample to the detector
   *  instead of from the moderator to the detector.  The time-of-flight 
   *  axes are assumed to be the same on all Data blocks in the DataSet.
   *  Three operations are performed.
   *
   *  First, a corrected total time-of-flight is calculated, based on the
   *  the experimentally measured time of flight as:
   *   t_total = t_exp -
   *  (1+tanh((Ei-a)/b))/(2*c*Ei)+(1-tanh((Ei-a)/b))/(2*d*Ei)+f+g*tanh((Ei-a)/b)
   *
   *  This funtion models the delay time of neutron emission from the 
   *  moderator(Alexander Kolesnikov).  
   *
   *  Second, the time-of-flight from the moderator to the sample is calculated
   *  based on Ein and the initial flight path.  This "initial time-of-flight"
   *  is subtracted from the corrected total time-of-flight, to obtain the
   *  sample to detector time-of-flight.
   *
   *  Third, the initial portion of the data, corresponding to zero or negative 
   *  sample to detector times-of-flight is removed.  In fact, some additional
   *  channels should be omitted, to keep the energies bounded.
   *
   *  @param  ds                The sample histogram DataSet to be adjusted
   *                            to give sample to detector times-of-flight.
   *  @param  Ein               The incident energy
   *  @param  a                 The "a" coefficient in the correction equation
   *  @param  b                 The "b" coefficient in the correction equation
   *  @param  c                 The "c" coefficient in the correction equation
   *  @param  d                 The "d" coefficient in the correction equation
   *  @param  f                 The "f" coefficient in the correction equation
   *  @param  g                 The "g" coefficient in the correction equation
   *  @param  n_channels_delay  The number of time channels to skip beyond
   *                            the channel where the time-of-flight from the
   *                            sample to detector is 0.  This must be at
   *                            least 10 and more typically should be several
   *                            hundred.
   */
  public static void SetFinalTOF( DataSet ds, 
                                  float   Ein, 
                                  float   a, 
                                  float   b, 
                                  float   c, 
                                  float   d, 
                                  float   f, 
                                  float   g,
                                  int     n_channels_delay )
  {
    if ( ds == null )
      throw new IllegalArgumentException("DataSet is null");
    
    if ( ds.getNum_entries() == 0 )
      throw new IllegalArgumentException("DataSet is empty");
 
    IData data = ds.getData_entry(0);
    float[] xs = data.getX_scale().getXs();
    if ( xs.length < 2 )
      throw new IllegalArgumentException(
                                "Time-of-flight has less than one bin.");

                                  // first adjust the total time-of-flight 
                                  // for each time bin.

    float initial_path = AttrUtil.getInitialPath( ds );
    if ( Float.isNaN(initial_path) )
      initial_path = AttrUtil.getInitialPath( data );

    float initial_tof = tof_calc.TOFofEnergy( initial_path, Ein );

    double tanh_E = Math.tanh((Ein-a)/b);
    double correction = (1+tanh_E)/(2*c*Ein)+(1-tanh_E)/(2*d*Ein)+f+g*tanh_E;
    
    for ( int i = 0; i < xs.length; i++ )
       xs[i] = (float)(xs[i] - correction - initial_tof);

    if ( n_channels_delay < MIN_DELAY_CHANNELS )
      n_channels_delay = MIN_DELAY_CHANNELS;

    int start    = n_channels_delay + Math.abs( Arrays.binarySearch( xs, 0 ));
    int num_kept = xs.length - start;
    float[] new_xs = new float[num_kept];
    System.arraycopy( xs, start, new_xs, 0, num_kept );
    VariableXScale new_x_scale = new VariableXScale( new_xs );

    int n_data = ds.getNum_entries();
    Attribute Ein_attr = new FloatAttribute( Attribute.ENERGY_IN, Ein );
    Attribute intof_attr = new FloatAttribute( Attribute.SOURCE_TO_SAMPLE_TOF,
                                               initial_tof );
    ds.setAttribute( Ein_attr );
    ds.setAttribute( intof_attr );
    for ( int i = 0; i < n_data; i++ )
    {
      HistogramTable hist_data = (HistogramTable)ds.getData_entry(i);
      AttributeList attr_list = hist_data.getAttributeList();
      int id     = hist_data.getGroup_ID();
      float[] ys = hist_data.getY_values();
      float[] new_ys = new float[ num_kept - 1 ];
      System.arraycopy( ys, start, new_ys, 0, num_kept-1 );

      HistogramTable new_data = new HistogramTable( new_x_scale, new_ys, id );

      new_data.setAttributeList(attr_list);
      new_data.setAttribute( Ein_attr );
      new_data.setAttribute( intof_attr );

      ds.replaceData_entry( new_data, i );
    }
  }


  /**
   *  Use the first two Data blocks from the monitor DataSet to calculate
   *  the incident energy, given an estimate of the incident energy and
   *  information about the width of a time-of-flight window centered on
   *  that incident energy, that should be used to locate the incident 
   *  pulse in a monitor spectrum.
   *
   *  @param mon_ds             A monitor DataSet containing two distinct
   *                            beam monitors as the first two entries.
   *  @param Ein_estimate       Estimate of the incident energy(meV), used to 
   *                            find the time-of-flight that will be at the 
   *                            center of windows containing the incident
   *                            neutron pulse in the monitor spectra.
   *  @param tof_half_interval  Half-width in microseconds of the 
   *                            time-of-flight windows containing the
   *                            incident neutron pulse in monitor spectra.
   */
  public static float EnergyFromMonitors( DataSet  mon_ds,
                                          float    Ein_estimate,
                                          float    tof_half_interval )
  {
    if ( mon_ds == null )
      throw new IllegalArgumentException( "Monitor DataSet is null" );

    if ( mon_ds.getNum_entries() < 2 )
      throw new IllegalArgumentException(
        "Less than two monitors in Monitor DataSet");

    float initial_path = AttrUtil.getInitialPath( mon_ds );
    HistogramTable mon_0 = RestrictMonitor( mon_ds.getData_entry(0),
                                            initial_path,
                                            Ein_estimate,
                                            tof_half_interval );

    HistogramTable mon_1 = RestrictMonitor( mon_ds.getData_entry(1),
                                            initial_path,
                                            Ein_estimate,
                                            tof_half_interval );

    float Ein = tof_data_calc.EnergyFromMonitorData( mon_0, mon_1 );
    return Ein;
  }


  /**
   *  Return a new monitor Data block obtained by restricting the specified  
   *  monitor Data block to a time-of-flight window centered on the 
   *  specified energy, with width twices the specified tof half interval.
   *  The specified Data block must be a HistogramTable.
   *
   *  @param data               A monitor Data block
   *  @param initial_path       The initial path from the monitor DataSet.
   *                            If this is passed in as NaN, the method will
   *                            attempt to get the initial path from the Data
   *                            block attributes.
   *  @param Ein_estimate       Estimate of the incident energy(meV), used to 
   *                            find the time-of-flight that will be at the 
   *                            center of a window containing the incident
   *                            neutron pulse in this Data block.
   *  @param tof_half_interval  Half-width in microseconds of the 
   *                            time-of-flight window containing the
   *                            incident neutron pulse in this Data block.
   */
  private static HistogramTable RestrictMonitor( IData data,
                                                 float initial_path,
                                                 float Ein_estimate,
                                                 float tof_half_interval )
  {
    if ( Float.isNaN( initial_path ) )
    {
      initial_path = AttrUtil.getInitialPath( data );
      if ( Float.isNaN( initial_path ) ) 
        throw new IllegalArgumentException("Initial Path not specified");
    }

    DetectorPosition position = AttrUtil.getDetectorPosition( data );
    float coords[] = position.getCartesianCoords();
    float displacement = coords[0];

    float center_tof = tof_calc.TOFofEnergy( initial_path + displacement,
                                             Ein_estimate );

    XScale x_scale = data.getX_scale();
    int start = x_scale.getI_GLB( center_tof - tof_half_interval );
    if ( start < 0 )
      throw new IllegalArgumentException(
                       "left edge of window is below TOF interval");

    int end   = x_scale.getI( center_tof + tof_half_interval );
    if ( end >= x_scale.getNum_x() )
      throw new IllegalArgumentException(
                       "right edge of window is above TOF interval");
     
     float[] xs = x_scale.getXs();
     float[] ys = data.getY_values();

     int num_kept = end - start + 1;
     float[] new_xs = new float[num_kept]; 
     float[] new_ys = new float[num_kept-1];

     System.arraycopy( xs, start, new_xs, 0, num_kept   );
     System.arraycopy( ys, start, new_ys, 0, num_kept-1 );

     VariableXScale new_x_scale = new VariableXScale( new_xs );
     int id = data.getGroup_ID();

     HistogramTable new_data = new HistogramTable( new_x_scale, new_ys, id ); 
     new_data.setAttributeList( data.getAttributeList() );
 
     return new_data;
  }


  /**
   *  Basic test program.
   */
  public static void main( String args[] )
  {
    String filename = "/usr2/SEQUOIA/SEQ_195.nxs";
    NexusRetriever retriever = new NexusRetriever( filename );
    retriever.RetrieveSetUpInfo(null);                        // get cache info

    DataSet mon_ds = retriever.getDataSet(0);
    float Ein = EnergyFromMonitors( mon_ds,
                                    50,
                                    700 );
    System.out.println( "Calculated Ein is : " + Ein );

    DataSet one_ds = retriever.getDataSet(10);                // ds 10 has data

//  float Ein =  45.504f;
    float a   =  92.0f;
    float b   =  81.3f;
    float c   =  0.00130f;
    float d   = -751f;
    float f   =  1.65f;
    float g   = -14.5f;

    SetFinalTOF( one_ds, Ein, a, b, c, d, f, g, 300 );

    SetInstrumentTypeCalc.setInstrumentType( one_ds, "TOF_NDGS" );

    new ViewManager( one_ds, ViewManager.IMAGE );

    for ( int i = 1; i < retriever.numDataSets(); i++ )
    {
      System.out.println("Converting DS # " + i );
      one_ds = retriever.getDataSet(i);
      SetFinalTOF( one_ds, Ein, a, b, c, d, f, g, 300 );
      SetInstrumentTypeCalc.setInstrumentType( one_ds, "TOF_NDGS" );
      if ( i % 10 == 0 )
        new ViewManager( one_ds, ViewManager.IMAGE );
    }
  }

}


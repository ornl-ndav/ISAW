/* 
 * File: RemovePeaks_Calc.java
 *
 * Copyright (C) 2010, Dennis Mikkelson
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
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */
package Operators.TOF_Diffractometer;

import java.util.*;
import java.io.*;
import gov.anl.ipns.Util.File.TextFileReader;
import gov.anl.ipns.MathTools.Geometry.DetectorPosition;

import EventTools.EventList.FileUtil;

import DataSetTools.operator.Operator;
import DataSetTools.operator.DataSet.Math.DataSet.DataSetSubtract;
import DataSetTools.dataset.AttrUtil;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.IData;
import DataSetTools.dataset.XScale;
import DataSetTools.math.tof_calc;
import DataSetTools.viewer.ViewManager;
import DataSetTools.viewer.IViewManager;

import Operators.Special.LowPassFilterDS;

public class RemovePeaks_Calc
{

  /**
   *  Replace small intervals of spectra that have a peak that should not
   *  be included, with a linear interpolation of average values below and
   *  above the peak extent.  One use of this method is to remove peaks in
   *  a vanadium spectrum, before smoothing the spectrum.
   *
   *  @param  ds              DataSet with spectra from which isolated peaks
   *                          will be removed.  The spectra in this DataSet
   *                          must be interms of d-spacing (Angstroms).
   *  @param  peak_file       ASCII file listing the h,k,l and d-spacing in
   *                          Angstroms, for the peaks that are to be removed
   *                          from the spectra.  Each line of the file must
   *                          have at least the four values.  Only the 
   *                          d-spacing value is currently used, but the
   *                          first three numbers (representing h,k,l) must 
   *                          be present, since they are read and skipped 
   *                          to get to the d-space value.  Lines starting 
   *                          with a '#' symbol are ignored.
   *  @param  delta_d_over_d  Estimate of the width of the peaks specified
   *                          by delta_d/d.  The actual width used will
   *                          depend on the d_value as width = d*delta_d_over_d.
   *  @param  replace_dist    Fractional number of peak widths, left and
   *                          right of the nominal position, that defines the 
   *                          interval over which values will be replaced by
   *                          interpolated values.  This is restricted to be 
   *                          between 1 and 20, so the values on the interval
   *                          [d-replace_dist*width,d+replace_dist*width]
   *                          will be replaced by linearly interpolated values.
   *                          This interval is considered to be the peak
   *                          extent.
   *  @param  num_to_average  The number of channels below and above the
   *                          peak extent that will be averaged to obtain
   *                          values left and right of the peak postion.
   *                          The actual values of the histogram will be
   *                          replaced by values that are linearly 
   *                          interpolated between these average values.
   *                          This is restricted to be between 1 and 100.
   *                           
   */
  public static void RemovePeaks_d( DataSet ds, 
                                    String  peak_file,
                                    float   delta_d_over_d,
                                    float   replace_dist,
                                    int     num_to_average )
                     throws Exception 
  {
    CheckParameters(ds, peak_file, delta_d_over_d, replace_dist, num_to_average);

    if ( ! ds.getX_units().equals( "Angstroms" ) )
      throw new IllegalArgumentException("DataSet must have X-units Angstroms");

    float[] peak_d_vals = load_peak_data( peak_file ); 

    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      IData data = ds.getData_entry( i );
      XScale xscale = data.getX_scale();

      for ( int j = 0; j < peak_d_vals.length; j++ )
      {
        float d_val    = peak_d_vals[j];
        float width    = delta_d_over_d * d_val;
        int min_index  = xscale.getI_GLB( d_val - width * replace_dist );
        int max_index  = xscale.getI_GLB( d_val + width * replace_dist );
        float[] x_vals = xscale.getXs();
        float[] y_vals = data.getY_values();
        Interpolate( x_vals, y_vals, min_index, max_index, num_to_average );
      } 
    }
  }

  /**
   *  Replace small intervals of spectra that have a peak that should not
   *  be included, with a linear interpolation of average values below and
   *  above the peak extent.  One use of this method is to remove peaks in
   *  a vanadium spectrum, before smoothing the spectrum.
   *
   *  @param  ds              DataSet with spectra from which isolated peaks
   *                          will be removed.  The spectra in this DataSet
   *                          must be interms of time-of-flight and the
   *                          Data blocks MUST have the effective position
   *                          and initial path attributes.
   *  @param  peak_file       ASCII file listing the h,k,l and d-spacing in
   *                          Angstroms, for the peaks that are to be removed
   *                          from the spectra.  Each line of the file must
   *                          have at least the four values.  Only the 
   *                          d-spacing value is currently used, but the
   *                          first three numbers (representing h,k,l) must 
   *                          be present, since they are read and skipped 
   *                          to get to the d-space value.  Lines starting 
   *                          with a '#' symbol are ignored.
   *  @param  delta_d_over_d  Estimate of the width of the peaks specified
   *                          by delta_d/d.  The actual width used will
   *                          depend on the d_value as width = d*delta_d_over_d.
   *  @param  replace_dist    Fractional number of peak widths, left and
   *                          right of the nominal position, that defines the 
   *                          interval over which values will be replaced by
   *                          interpolated values.  This is restricted to be 
   *                          between 1 and 20, so the values on the interval
   *                          [d-replace_dist*width,d+replace_dist*width]
   *                          will be replaced by linearly interpolated values.
   *                          This interval is considered to be the peak
   *                          extent.
   *  @param  num_to_average  The number of channels below and above the
   *                          peak extent that will be averaged to obtain
   *                          values left and right of the peak postion.
   *                          The actual values of the histogram will be
   *                          replaced by values that are linearly 
   *                          interpolated between these average values.
   *                          This is restricted to be between 1 and 100.
   *                           
   */
  public static void RemovePeaks_tof( DataSet ds,
                                      String  peak_file,
                                      float   delta_d_over_d,
                                      float   replace_dist,
                                      int     num_to_average )
                     throws Exception
  {
    CheckParameters(ds, peak_file, delta_d_over_d, replace_dist, num_to_average);

    if ( ! ds.getX_units().equals( "Time(us)" ) )
      throw new IllegalArgumentException(
                        "DataSet must have X-units microseconds, Time(us)");

    float[] peak_d_vals = load_peak_data( peak_file );

    DetectorPosition det_pos = null;
    float l1, 
          l2, 
          angle_radians;

    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      IData  data   = ds.getData_entry( i );
      XScale xscale = data.getX_scale();

      det_pos = AttrUtil.getDetectorPosition( data );
      if ( det_pos == null )
        throw new IllegalArgumentException(
                        "Data must have DetectorPosition attribute");

      l1 = AttrUtil.getInitialPath( data );

      if ( Float.isNaN( l1 ) )
        l1 = AttrUtil.getInitialPath( ds );

      if ( Float.isNaN( l1 ) )
        throw new IllegalArgumentException(
                        "Data must have initial flight path attribute");

      l2            = det_pos.getDistance();
      angle_radians = det_pos.getScatteringAngle();
  
      for ( int j = 0; j < peak_d_vals.length; j++ )
      {
        float d_val    = peak_d_vals[j];
        float d_width  = delta_d_over_d * d_val;

        float min_d    = d_val - d_width * replace_dist;
        float max_d    = d_val + d_width * replace_dist;

        float min_tof  = tof_calc.TOFofDSpacing( angle_radians, l1+l2, min_d );
        float max_tof  = tof_calc.TOFofDSpacing( angle_radians, l1+l2, max_d );

        int min_index  = xscale.getI_GLB( min_tof );
        int max_index  = xscale.getI_GLB( max_tof );

        float[] x_vals = xscale.getXs();
        float[] y_vals = data.getY_values();

        if ( min_index != max_index )
          Interpolate( x_vals, y_vals, min_index, max_index, num_to_average );
      }
    }
  }


  private static void CheckParameters( DataSet ds, 
                                       String  peak_file,
                                       float   delta_d_over_d,
                                       float   replace_dist,
                                       int     num_to_average )
  {
    if ( ds == null )
      throw new IllegalArgumentException("DataSet null");

    if ( ds.getNum_entries() <= 0 )
      throw new IllegalArgumentException("DataSet is empty");

    if ( ! ds.getData_entry(0).isHistogram() )
      throw new IllegalArgumentException("Data in DataSet must be histogram");

    FileUtil.CheckFile( peak_file );

    if ( delta_d_over_d <= 0 )
      throw new IllegalArgumentException(
           "Peak width factor, delta_d_over_d " + delta_d_over_d +
           " MUST be > 0" );

    if ( replace_dist < 1 || replace_dist > 20 )
      throw new IllegalArgumentException("The replace distance = " +
                             replace_dist + " must be between 1 and 20" );

    if ( num_to_average < 1 || num_to_average > 100 )
      throw new IllegalArgumentException("Number to average = " +
                             num_to_average + " must be between 1 and 100" );
  } 


  /**
   *  Replace the y_values in positions between min and max with interpolated
   *  values.  The interpolated values are points along a line joining the
   *  the average of num (x,y) values just below position min, and num (x,y)
   *  values just above position max. 
   */
  private static void Interpolate( float[] x_vals,
                                   float[] y_vals, 
                                   int     min, 
                                   int     max, 
                                   int     num )
  {
    if ( min <= 0 )                 // no bins to the left to average
      return;

    if ( max >= x_vals.length-1 )   // no bins to the right to average
      return;

    if ( min == max )               // no interior of peak extent
      return;
                                                   // find average to LEFT
    int   start   = Math.max( 0, min - num + 1 );
    int   num_ave = min - start + 1;
    float x_sum = 0;
    float y_sum = 0;
    for ( int i = start; i <= min; i++ )
    {
      x_sum += (x_vals[i] + x_vals[i+1] ) / 2;    // we assume this is histogram
      y_sum += y_vals[i];
    }

    float x0 = x_sum / num_ave;
    float y0 = y_sum / num_ave;

                                                  // find average to RIGHT
    int   end   = Math.min( y_vals.length-2, max + num - 1 );
    num_ave = end - max + 1;
    x_sum = 0;
    y_sum = 0;
    for ( int i = max; i <= end; i++ )
    {
      x_sum += (x_vals[i] + x_vals[i+1] ) / 2; 
      y_sum += y_vals[i];
    }

    float x1 = x_sum / num_ave;
    float y1 = y_sum / num_ave;

    float x;                                    // interpolated point
    for ( int i = min; i <= max; i++ )
    {
      x = ( x_vals[i] + x_vals[i+1] ) / 2;
      y_vals[i] = y0 + (y1 - y0) * (x - x0) / (x1 - x0);
    }
  }


  /**
   *  Load a listing of d-space values for the positions of peaks that should
   *  be filtered out.  The file must be a simple ASCII file containing
   *  data lines with at least four columns.  The first three columns are 
   *  currently h,k,l values in the file supplied by Jason Hodges, though
   *  these values are currently ignored.  The fourth column contains the
   *  d-space values of peaks, in Angstroms.
   *
   *  @param  filename   The name of the file containing information about
   *                     the peak positions.
   *
   *  @return An array of floats specifying the peak positions, in Angstroms.
   */
  public static float[] load_peak_data( String filename ) 
                        throws IOException
  {
    Vector peak_d_vals = new Vector();

    float d_spacing;

    try
    {
      TextFileReader tfr = new TextFileReader( filename );
      String line;
      while ( true )
      {
        line = tfr.read_line();
        if ( !line.startsWith("#") )       // skip comment lines
        {
          tfr.unread();
          tfr.read_float();                // read across h, k and l
          tfr.read_float();
          tfr.read_float();
          d_spacing = tfr.read_float();
          peak_d_vals.add( d_spacing ); 
          tfr.read_line();                 // skip the rest of the line
        }
      }
    }
    catch ( Exception ex )
    {
      // empty catch block... we hit this at the end of the file
    }

    float[] result = new float[ peak_d_vals.size() ];
    for ( int i = 0; i < result.length; i++ )
      result[i] = (Float)( peak_d_vals.elementAt(i) );
    
    return result;
  }


  /**
   *  Main program used for testing.
   */
  public static void main( String[] args ) throws Exception
  {
    String peak_file_name = "/usr2/PG3/VanadiumPeaks.dat";
/*    
    float[] peak_d_vals = load_peak_data( peak_file_name );
    for ( int i = 0; i < peak_d_vals.length; i++ )
      System.out.println("d = " + peak_d_vals[i] );
*/ 
    String Instrument = "PG3";
    String EventFileName = "/usr2/PG3/PG3_539_neutron_event.dat";

    String   DetCalFileName   = null;
    String   bankInfoFileName = null;
    String   MappingFileName  = null;
    float    firstEvent = 1;
    float    NumEventsToLoad = 1e12f;
    boolean  isLog = true;
    int      nUniformbins  = 10000;

    DataSet  original_ds = null;
    boolean  test_d_version = false;
    if ( test_d_version )
    {
      float    min = 0.2f;
      float    max = 4.5f;
      float    first_logStep = 1e-4f;
      original_ds = Util.Make_d_DataSet( EventFileName,
                                         DetCalFileName,
                                         bankInfoFileName,
                                         MappingFileName,
                                         firstEvent,
                                         NumEventsToLoad,
                                         min,
                                         max,
                                         isLog,
                                         first_logStep,
                                         nUniformbins,
                                         false,
                                         null,
                                         false,
                                         null,
                                         0,
                                         0  );
     }
     else
     {
       float min = 100;
       float max = 32000;
       float first_logStep = .1f;
       original_ds = Util.MakeTimeFocusedDataSet( EventFileName,
                                               DetCalFileName,
                                               bankInfoFileName,
                                               MappingFileName,
                                               firstEvent,
                                               NumEventsToLoad,
                                               90f,
                                               .5f,
                                               min,
                                               max,
                                               isLog,
                                               first_logStep,
                                               nUniformbins ,
                                               false,
                                               null,
                                               0,
                                               0);
     }

     DataSet combined_ds = (DataSet)original_ds.clone();

     DataSet ds = (DataSet)original_ds.clone();

     if ( test_d_version )
       RemovePeaks_d( ds, peak_file_name, 0.005f, 2, 10 );
     else
       RemovePeaks_tof( ds, peak_file_name, 0.005f, 2, 10 );

     for ( int i = 0; i < ds.getNum_entries(); i++ )
     {
       Data data = ds.getData_entry(i);
       int gid = data.getGroup_ID();
       data = (Data)data.clone();
       data.setGroup_ID( gid + 10000 );
       combined_ds.addData_entry( data );
     }

     DataSet filtered_original = (DataSet)original_ds.clone();
     int   order  = 2;
     float cutoff = .02f;
     Operator op  = new LowPassFilterDS( filtered_original, cutoff, order ); 
     op.getResult();

     for ( int i = 0; i < filtered_original.getNum_entries(); i++ )
     {
       Data data = filtered_original.getData_entry(i);
       int gid = data.getGroup_ID();
       data = (Data)data.clone();
       data.setGroup_ID( gid + 20000 );
       combined_ds.addData_entry( data );
     }

     DataSet filtered_ds = (DataSet)ds.clone();
     op  = new LowPassFilterDS( filtered_ds, cutoff, order );
     op.getResult();

     for ( int i = 0; i < filtered_ds.getNum_entries(); i++ )
     {
       Data data = filtered_ds.getData_entry(i);
       int gid = data.getGroup_ID();
       data = (Data)data.clone();
       data.setGroup_ID( gid + 30000 );
       combined_ds.addData_entry( data );
     }

     new ViewManager( combined_ds, IViewManager.IMAGE );

     op = new DataSetSubtract( filtered_original, filtered_ds, true );

     Object result = op.getResult();
     if ( result instanceof DataSet )
     {
       DataSet filtered_difference = (DataSet)op.getResult();
       new ViewManager( filtered_difference, IViewManager.IMAGE );
     }
     else
       System.out.println("RESULT NOT A DataSet: " + result );
  }

}

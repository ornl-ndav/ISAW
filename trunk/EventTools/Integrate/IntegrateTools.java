/* 
 * File: IntegrateTools.java
 *
 * Copyright (C) 2012, Dennis Mikkelson
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

package EventTools.Integrate;

import java.util.Vector;
import java.io.*;

import EventTools.EventList.*;
import EventTools.Histogram.*;

import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.File.FileIO;

/**
 *  This class has various rudimenary tools for testing peak
 *  integration concepts.
 */
public class IntegrateTools
{
  public static final float BACKGROUND_RATIO = 1.25992105f;

  private static DataSet isigi_ds   = new DataSet();
  private static DataSet density_ds = new DataSet();
  private static DataSet normalized_density_ds = new DataSet();
  private static DataSet signal_ds = new DataSet();



  public static boolean isIndexed( Peak_new peak )
  {
    return ( peak.h() != 0 || peak.k() != 0 || peak.l() != 0 ); 
  }


  public static void IntegratePeaksEvents( Vector<Peak_new> peaks, 
                                           float            peak_radius,
                                           float            bkg_radius,
                                           IEventList3D     Q_evl )
  {
    float[][] all_Qs   = getPeakQsFromPeakVector( peaks );
    float[]   radii    = { peak_radius, bkg_radius };

    float  peak_volume = (float)(4.0/3.0 * Math.PI * Math.pow( peak_radius,3));
    float  bkg_volume  = (float)(4.0/3.0 * Math.PI * Math.pow( bkg_radius,3))
                         - peak_volume;

    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = peaks.elementAt(i);

      int row = Math.round(peak.y());
      int col = Math.round(peak.x());

      if ( isIndexed( peak ) && 
           row >=  21 &&
           row <= 236 &&
           col >=  21 &&
           col <= 236   )
      {
        float[] sums = IntegrateSpheres( all_Qs[i], radii, false, Q_evl );

        float peak_count = sums[0];
        float bkg_count  = sums[1] - sums[0];
        
        float[] temp = getI_and_sigI( peak_count, peak_volume,
                                      bkg_count, bkg_volume );
        float net_signal = temp[0];
        float sigI       = temp[1];

        peak.inti( net_signal );
        peak.sigi( sigI );    
        peak.reflag( 500 + peak.reflag() );
      }
    }
  }


  public static void IntegratePeaksHistogram( Vector<Peak_new> peaks,
                                              float            peak_radius,
                                              float            bkg_radius,
                                              Histogram3D      histogram )
  {
    float[][] all_Qs = getPeakQsFromPeakVector( peaks );
    float[]   radii  = { peak_radius, bkg_radius };

    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = peaks.elementAt(i);
      int row = Math.round(peak.y());
      int col = Math.round(peak.x());

      if ( isIndexed( peak ) &&
           row >=  21 &&
           row <= 236 &&
           col >=  21 &&
           col <= 236   )
      {
        float[] q_vec = all_Qs[i];
        Vector result = histogram.sphereIntegrals( q_vec[0],
                                                   q_vec[1],
                                                   q_vec[2],
                                                   radii  );
        float[] sums    = (float[])result.elementAt(0);
        float[] volumes = (float[])result.elementAt(1);

        float peak_count = sums[0];
        float bkg_count  = sums[1] - peak_count;

        float peak_volume = volumes[0];
        float bkg_volume  = volumes[1] - peak_volume;

        float[] temp = getI_and_sigI( peak_count, peak_volume,
                                      bkg_count, bkg_volume );
        float net_signal = temp[0];
        float sigI       = temp[1];

        peak.inti( net_signal );
        peak.sigi( sigI );
        peak.reflag( 600 + peak.reflag() );
      }
    }
  }



  private static Histogram3D MakeEmptyHistogram()
  {
    int hist_bins = 1024;
    UniformEventBinner x_binner = new UniformEventBinner( -20,  0, hist_bins );
    UniformEventBinner y_binner = new UniformEventBinner( -18, 18, hist_bins );
    UniformEventBinner z_binner = new UniformEventBinner( -18, 18, hist_bins );

    Vector3D x_vec = new Vector3D( 1, 0, 0 );
    Vector3D y_vec = new Vector3D( 0, 1, 0 );
    Vector3D z_vec = new Vector3D( 0, 0, 1 );

    ProjectionBinner3D x_proj_binner = new ProjectionBinner3D(x_binner, x_vec);
    ProjectionBinner3D y_proj_binner = new ProjectionBinner3D(y_binner, y_vec);
    ProjectionBinner3D z_proj_binner = new ProjectionBinner3D(z_binner, z_vec);

    Histogram3D histogram = new Histogram3D( x_proj_binner,
                                             y_proj_binner,
                                             z_proj_binner );
    return histogram;
  }


  public static float[] IntegrateSpheres( float[]      q_vec,
                                          float[]      radii,
                                          boolean      use_weights,
                                          IEventList3D event_list )
  {
    float qx = q_vec[0];
    float qy = q_vec[1];
    float qz = q_vec[2];

    int num_radii = radii.length;

    float[] sums       = new float[ num_radii ];
    float[] d_squareds = new float[ num_radii ];

    for ( int i = 0; i < num_radii; i++ )
    {
      sums[i] = 0;
      d_squareds[i] = radii[i] * radii[i];
    } 

    int     num_events = event_list.numEntries();
    float[] xyz        = event_list.eventVals();
    float[] weights    = event_list.eventWeights();
    float   x,
            y,
            z;
    float   d_squared;

    int index = 0;
    for ( int i = 0; i < num_events; i++ )
    {
      x = qx - xyz[ index++ ];
      y = qy - xyz[ index++ ];
      z = qz - xyz[ index++ ];
      d_squared = x*x + y*y + z*z;
      for ( int k = 0; k < num_radii; k++ )
        if ( d_squared <= d_squareds[k] )
        {
          if ( use_weights )
            sums[ k ] += weights[ i ];
          else
            sums[ k ] += 1;
        }
    }
    return sums;
  }


  /**
   * Generate a list of radii that are integer multiples of the specified
   * min_radius.
   */
  public static float[] getUniformRadii( float min_radius, int num_radii )
  {
    float[] radii = new float[ num_radii ];
    for ( int i = 0; i < num_radii; i++ )
      radii[i] = (i+1) * min_radius;

    return radii;
  }


  public static float[] getGeometricProgRadii( float min_radius, 
                                               int   num_radii,
                                               float ratio )
  {
    float[] radii = new float[ num_radii ];
    radii[0] = min_radius;
    for ( int i = 1; i < num_radii; i++ )
      radii[i] = radii[i-1] * ratio;

    return radii;
  }


  public static float[] getUniformVolumeRadii(float min_radius, int num_radii)
  {
    float[] radii  = new float[ num_radii ];
    float vol_step = (float)(4.0/3.0 * Math.PI * Math.pow( min_radius, 3 ));
    float volume   = vol_step;
    radii[0] = min_radius;
    for ( int i = 1; i < num_radii; i++ )
    {
      volume += vol_step;
      radii[i] = (float)Math.pow( (3.0/4.0 * volume / Math.PI), 1.0/3.0 );
    }
    return radii;
  }


  public static float[] hkl_to_q( float[][] mat, float[] hkl )
  {
    float[][] or_mat = LinearAlgebra.getTranspose( mat );
    float[]   q      = LinearAlgebra.mult( or_mat, hkl );
    return q;
  }


  /**
   * Find I and sigI for the specified signal, background, sample
   * volume for the signal and sample volume for the background.  The
   * value "I" is the net, peak - background, counts in the peak region.
   * The value sigI is the estimated standard deviation of the net peak counts.
   *
   * @param raw_sig   The total raw counts in the region that belongs
   *                  to the peak.
   * @param raw_vol   The volume of the region that belongs to the peak.
   * @param back      The raw counts in the region that belongs to the
   *                  background.
   * @param back_vol  The volume of the region that belongs to the
   *                  background.
   *
   * @return an array with I and sigI as the first two entries.
   */
  public static float[] getI_and_sigI( float raw_sig,  float raw_vol,
                                       float back,     float back_vol )
  {
    if ( back_vol <= 0 )
    {
      float[] result = { raw_sig, 0.0f };
      return result;
    }

    float ratio  = raw_vol/back_vol;
    float signal = raw_sig - ratio * back;

    float sigma_signal = (float)Math.sqrt( raw_sig + ratio * ratio * back );

    float[] result = { signal, sigma_signal };
    return result;
  }


  public static void ShowResults( int     seq_num,
                                  float[] radii, 
                                  float[] counts,
                                  float[] volumes )
  {

    float[] shell_volumes = new float[ radii.length ];
    float[] shell_counts  = new float[ radii.length ];
    
    shell_volumes[0] = volumes[0];
    shell_counts[0]  = counts[0];
    for ( int i = 1; i < radii.length; i++ )
    {
      shell_counts[i]  = counts[i]  - counts[i-1];
      shell_volumes[i] = volumes[i] - volumes[i-1];
    }

    float[] net_signal = new float[ radii.length ];
    float[] I_sigI     = new float[ radii.length ];
    for ( int i = 0; i < radii.length - 1; i++ )
    {
      float[] temp = getI_and_sigI( counts[i], volumes[i],
                                    shell_counts[i+1], shell_volumes[i+1] );
      net_signal[i] = temp[0];
      I_sigI[i]     = temp[0] / temp[1];
    }

    float[] valid_xs = new float[ radii.length - 1 ];   // last I, and IsigI
                                                        // are not valid since
                                                        // there is no next
                                                        // spherical shell
    for ( int i = 0; i < valid_xs.length; i++ )
      valid_xs[i] = radii[i];
    XScale x_scale = new VariableXScale( valid_xs );
    Data d = new FunctionTable( x_scale, I_sigI, seq_num );
    d.setLabel( "Peak Seq #" + seq_num );
    isigi_ds.addData_entry( d );

    d = new FunctionTable( x_scale, net_signal, seq_num );
    d.setLabel( "Peak Seq #" + seq_num );
    signal_ds.addData_entry( d );

    float[] density = new float[ radii.length ];
    for ( int i = 0; i < density.length; i++ )
      density[i] = shell_counts[i]/shell_volumes[i];

    d = new FunctionTable( x_scale, density, seq_num );
    d.setLabel( "Peak Seq #" + seq_num );
    density_ds.addData_entry( d );

    float max_density = density[0];
    for ( int i = 0; i < density.length; i++ )
      if ( density[i] > max_density )
        max_density = density[i];

    if ( max_density > 0 )                          // Normalize the densities
    {
      float[] norm_density = new float[ density.length ];
      for ( int i = 0; i < density.length; i++ )
        norm_density[i] = density[i] / max_density;
    
      d = new FunctionTable( x_scale, norm_density, seq_num );
      d.setLabel( "Peak Seq #" + seq_num );
      normalized_density_ds.addData_entry( d );
    }

/*                                               // NOTE: This could fail if
    int   max_index = 0;                         //       there were no counts
    for ( int i = 1; i < radii.length; i++ )     //       in original sphere 
    {
      if ( net_signal[max_index] > 0 && net_signal[i] > 0 )
      { 
        float scale_0 = (float)(1.0 / Math.sqrt( net_signal[ max_index ] ));
        float scale_1 = (float)(1.0 / Math.sqrt( net_signal[i] ));
        if ( scale_1 *  I_sigI[i] > scale_0 * I_sigI[ max_index ] )
          max_index = i;
      }
    }
*/

    int   max_index = 0;                         //       there were no counts
    for ( int i = 1; i < radii.length; i++ )     //       in original sphere 
    {
      if ( I_sigI[i] > I_sigI[ max_index ] )
        max_index = i;
    }


    System.out.println(
 " i    count     radius    density        vol  shell_vol          I    IsigI");
    for ( int i = 0; i < radii.length; i++ )
    {
      if ( shell_volumes[i] != 0 )
        System.out.printf( 
         "%2d %8.0f %10.4f %10.2f %10.2f %10.2f %10.2f %8.1f",
           i, 
           counts[i], 
           radii[i], 
           shell_counts[i]/shell_volumes[i], 
           volumes[i],
           shell_volumes[i],
           net_signal[i],
           I_sigI[i]  );
      else
        System.out.printf( 
         "%2d %8.0f %10.4f %10.2f %10.2f %10.2f %10.2f %8.1f",
           i, 
           counts[i], 
           radii[i], 
           Float.NaN, 
           volumes[i],
           shell_volumes[i], 
           net_signal[i], 
           I_sigI[i]  );

      if ( i == max_index )
        System.out.println("*");
      else
        System.out.println();
    }

    System.out.println("I = " + net_signal[ max_index ] +
                       "  IsigI = " + I_sigI[ max_index ] );
  }


  public static float[] getSNAP_240_peak_Q()
  {
    float[] q_vec = new float[3];

    // peak with h,k,l = -3,-1,0 in run SNAP_240, 
    // detector 12, col = 116.89, row = 160.08, chan = 546.8
    //
    // Orientation matrix  0.121733  0.162127 -0.118117
    //                     0.128067  0.158202  0.116424
    //                     0.145964 -0.113030 -0.005984
/*
    float qx = -.49f;
    float qy = -.64f;
    float qz =  .24f;
    q_vec[0] = qx;
    q_vec[1] = qy;
    q_vec[2] = qz;
*/
    float[][] mat = { { 0.121733f,  0.162127f, -0.118117f },
                      { 0.128067f,  0.158202f,  0.116424f },
                      { 0.145964f, -0.113030f, -0.005984f } };

//    float[] hkl = { -3, -1, 0 };
      float[] hkl = { -4, -1, 0 };
//    float[] hkl = { -4, -2, -1 };
    float[] temp_q = hkl_to_q( mat, hkl );
    System.out.println("Calculated Q = " + temp_q[0] +
                                    ", " + temp_q[1] +
                                    ", " + temp_q[2]  );
    q_vec = temp_q;

    q_vec[0] *= (float)Math.PI * 2;
    q_vec[1] *= (float)Math.PI * 2;
    q_vec[2] *= (float)Math.PI * 2;

    return q_vec;
  }


  public static float[][] getPeakQsFromPeakVector( Vector<Peak_new> peaks )
  {
    float[][] peaks_qxyz = new float[ peaks.size() ][ 3 ];

    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = (Peak_new)peaks.elementAt(i);

      float   ipk  = peak.ipkobs();
      float[] qxyz = peak.getQ();
      for ( int j = 0; j < 3; j++ )
        qxyz[j] = (float)( 2 * Math.PI * qxyz[j] );

//      System.out.printf( "%3d    %8.2f    %8.2f    %8.2f     %8.1f\n",
//                         (i+1), qxyz[0], qxyz[1], qxyz[2], ipk );

      for ( int k = 0; k < 3; k++ )
        peaks_qxyz[i][k] = qxyz[k];
    }

    return peaks_qxyz;
  }
  

  public static float[][] getPeakQsFromFile( String filename ) 
                          throws IOException
  {
    Vector peaks = Peak_new_IO.ReadPeaks_new( filename );

    float[][] peaks_qxyz = getPeakQsFromPeakVector( peaks );

    return peaks_qxyz;
  }


  public static void calculateIntegrals( float[]       radii, 
                                         float[][]     q_vecs,
                                         IEventList3D  Q_evl,
                                         Histogram3D   histogram)
  {
    float scale = 16734;                // scale factor to make histogram bin
                                        // count and actual volume equal.
    int     num_radii = radii.length;
    float[] volumes   = new float[ num_radii ];
    for ( int i = 0; i < num_radii; i++ )
      volumes[i] = (float)(4./3. * Math.PI * Math.pow( radii[i], 3 ))*scale;

    for ( int i = 0; i < q_vecs.length; i++ )
    {
      System.out.println("==============================================");
      System.out.println("  SEQUENCE NUMBER " + (i+1) );
      System.out.println("==============================================");
      float[] q_vec = q_vecs[i];
      
      float[] integrals;
      boolean use_events = true;
      if ( use_events )
      {
        integrals = IntegrateSpheres( q_vec, radii, false, Q_evl );
        System.out.println("\n ---- USING RAW EVENTS --------- " );
        ShowResults( i+1, radii, integrals, volumes );
      }
      else
      {
        System.out.println("Integrating Events in histogram ... ");
        Vector result = histogram.sphereIntegrals( q_vec[0],
                                                   q_vec[1],
                                                   q_vec[2],
                                                   radii  );
        if ( result != null )
        { 
          integrals = (float[])result.elementAt(0);
          volumes   = (float[])result.elementAt(1);  // volume is number of 
                                                     // bins added
          System.out.println("\n ---- USING RAW 3D HISTOGRAM ------- " );
          ShowResults( i+1, radii, integrals, volumes );
        }
      }
    }
  }


  public static float[][] calculateIntegrals( float[]       radii,
                                              float[][]     q_vecs,
                                              IEventList3D  Q_evl )
  {
    int num_radii = radii.length;
    float[] volumes = new float[ num_radii ];
    for ( int i = 0; i < num_radii; i++ )
      volumes[i] = (float)(4./3. * Math.PI * Math.pow( radii[i], 3 ));

    for ( int i = 0; i < q_vecs.length; i++ )
    {
      System.out.println("==============================================");
      System.out.println("  INTEGRATING SEQUENCE NUMBER " + (i+1) );
      float[] q_vec = q_vecs[i];

      float[] integrals;
      {
        integrals = IntegrateSpheres( q_vec, radii, false, Q_evl );
        System.out.println("\n ---- USING RAW EVENTS --------- " );
        ShowResults( i+1, radii, integrals, volumes );
      }
      System.out.println("==============================================");
    }

    return null;
  }


  /**
   *  First argument is event file name
   *  Second is peaks file name
   *  Third is output file name
   *  Fourth is the peak radius to use
   */
  public static void main( String args[] ) throws Exception
  {
    if ( args.length != 4 )
    {
      System.out.println("Enter four parameters on command line");
      System.out.println("First : The event file name.");
      System.out.println("Second: The peaks file name.");
      System.out.println("Third : The output file name.");
      System.out.println("Fourth: The radius to use for the peak, in |Q|");
      System.exit(1);
    }

    SNS_Tof_to_Q_map  mapper;
    String filename   = args[0];
    String peaks_file = args[1];
    String out_file   = args[2];
    String inst_name  = FileIO.getSNSInstrumentName( filename );

    System.out.println();
    System.out.println("Instrument  : " + inst_name );
    System.out.println("Event file  : " + filename );
    System.out.println("Peaks file  : " + peaks_file );
    System.out.println("Output file : " + out_file );
    System.out.println("Peak radius : " + args[3] );
    System.out.println();

//    String DetCalFile = "/usr2/TOPAZ_11/natrolite1242_1.DetCal";
    String DetCalFile = null;
    mapper = new SNS_Tof_to_Q_map( DetCalFile, null, inst_name );
    SNS_TofEventList  tof_evl = new SNS_TofEventList( filename );

    int num_events = (int)tof_evl.numEntries();
    IEventList3D Q_evl = mapper.MapEventsToQ( tof_evl, 0, num_events );

    float peak_radius = Float.parseFloat( args[3].trim() ); 
    float bkg_radius  = BACKGROUND_RATIO * peak_radius;
    float[] radii = { peak_radius, bkg_radius };

    Vector peaks = Peak_new_IO.ReadPeaks_new( peaks_file );
    float[][] all_Qs = getPeakQsFromFile( peaks_file );

    boolean raw_events = true;
    if ( raw_events )
    {
      System.out.println("Integrating using RAW EVENTS");
      long start = System.nanoTime();
      IntegratePeaksEvents( peaks, peak_radius, bkg_radius, Q_evl );
      long end = System.nanoTime();
      System.out.printf("Time to integrate events = %5.2f ms\n",
                         ( end-start )/1.0E6 );
    }
    else
    {
      System.out.println("Integrating using HISTOGRAM");
      Histogram3D histogram = MakeEmptyHistogram();
      histogram.addEvents( Q_evl, false );
      long start = System.nanoTime();
      IntegratePeaksHistogram( peaks, peak_radius, bkg_radius, histogram );
      long end = System.nanoTime();
      System.out.printf("Time to integrate histogram = %5.2f ms\n",
                         ( end-start )/1.0E6 );
   }

    Peak_new_IO.WritePeaks_new( out_file, peaks, false );
  }


  public static void main_1( String args[] ) throws Exception
  {
    if ( args.length != 4 )
    {
      System.out.println("Enter four parameters on command line");
      System.out.println("First : The event file name.");
      System.out.println("Second: The peaks file name.");
      System.out.println("Third : The output file name.");
      System.out.println("Fourth: The radius to use for the peak, in |Q|");
      System.exit(1);
    }

    SNS_Tof_to_Q_map  mapper;
    String filename   = args[0];
    String peaks_file = args[1];
    String out_file   = args[2];
    String inst_name  = FileIO.getSNSInstrumentName( filename );

    System.out.println();
    System.out.println("Instrument  : " + inst_name );
    System.out.println("Event file  : " + filename );
    System.out.println("Peaks file  : " + peaks_file );
    System.out.println("Output file : " + out_file );
    System.out.println("Peak radius : " + args[3] );
    System.out.println();

    mapper = new SNS_Tof_to_Q_map( null, null, inst_name );
    SNS_TofEventList  tof_evl = new SNS_TofEventList( filename );

    int num_events = (int)tof_evl.numEntries();
    IEventList3D Q_evl = mapper.MapEventsToQ( tof_evl, 0, num_events );


//    float[] q_vec = getSNAP_240_peak_Q();
    float[][] all_Qs = getPeakQsFromFile( peaks_file );
    int seqn = 16;
    float[] q_vec = all_Qs[seqn - 1]; 

//  String method = "CONSTANT VOLUME INCREASE";
//  String method = "EQUAL STEP IN RADIUS";
    String method = "VOLUME INCREASES BY 100% EACH STEP";
//  String method = "VOLUME INCREASES BY 50% EACH STEP";
    float[] radii = null;
    int     num_radii  = 30;
    float   min_radius = 0.04f;
    if ( method.equals( "CONSTANT VOLUME INCREASE" ) )
    {
      num_radii  = 100;
      min_radius = 0.1f; 
      radii = getUniformVolumeRadii( min_radius, num_radii );
    }

    else if ( method.equals( "EQUAL STEP IN RADIUS" ) )
    {
      num_radii  = 40;
      min_radius = 0.025f;
      radii = getUniformRadii( min_radius, num_radii );
    }

    else if ( method.equals( "VOLUME INCREASES BY 100% EACH STEP" ) )
    {
      float   ratio   = 1.25992105f;    // this will cause the shell volume
                                        // to double each time
      num_radii  = 22;
      min_radius = 0.01f; 
      radii = getGeometricProgRadii( min_radius, num_radii, ratio );
    }
    else if ( method.equals( "VOLUME INCREASES BY 50% EACH STEP" ) )
    {
      float   ratio   = 1.1447142f;     // this will cause the shell volume
                                        // to increase by 50% each time 
      num_radii  = 37;
      min_radius = 0.01f;
      radii = getGeometricProgRadii( min_radius, num_radii, ratio );
    }
    

    float scale = 16734;                // scale factor to make histogram bin
                                        // count and actual volume equal.
    float[] volumes   = new float[ num_radii ];
    for ( int i = 0; i < num_radii; i++ )
      volumes[i] = (float)(4./3. * Math.PI * Math.pow( radii[i], 3 ))*scale;


    float[] integrals = IntegrateSpheres( q_vec, radii, false, Q_evl );

    System.out.println("\n ---- USING RAW EVENTS --------- " );
    ShowResults( seqn, radii, integrals, volumes );

    Histogram3D histogram =  MakeEmptyHistogram();

    float[] weights = Q_evl.eventWeights();     // get REFERENCE to list of
                                                // weights and set them to 1
    for ( int i = 0; i < weights.length; i++ )
      weights[i] = 1; 

    System.out.println("Adding events to histogram ... ");
    histogram.addEvents( Q_evl, false );

    System.out.println("Integrating Events in histogram ... ");
    Vector result = histogram.sphereIntegrals( q_vec[0],
                                               q_vec[1],
                                               q_vec[2],
                                               radii  );
    integrals = (float[])result.elementAt(0);
    volumes   = (float[])result.elementAt(1);

    System.out.println("\n ---- USING RAW 3D HISTOGRAM ------- " );
    ShowResults( seqn, radii, integrals, volumes );

    isigi_ds   = new DataSet("I/sigI Values for Peaks", new OperationLog(), 
                             "Inverse Anstroms", "Sphere Radius",
                             "Number", "I/sigI" );

    density_ds = new DataSet("Count Density Near Peaks", new OperationLog(),
                             "Inverse Anstroms", "Sphere Radius",
                             "Counts/Unit Volume", "Count Density" );

    normalized_density_ds = 
                 new DataSet("Normalized Count  Density Near Peaks", 
                              new OperationLog(), 
                             "Inverse Anstroms", "Sphere Radius",
                             "Counts/Unit Volume", "Count Density" );

    signal_ds = new DataSet("Integrated Intensity vs Radius",new OperationLog(),
                           "Inverse Anstroms", "Sphere Radius",
                           "Counts", "Net Counts in Sphere" );

    System.out.println("INTEGRATING ALL PEAKS IN PEAKS FILE");
    calculateIntegrals( radii, all_Qs, Q_evl, histogram );

    new ViewManager( isigi_ds, IViewManager.IMAGE );
    new ViewManager( density_ds, IViewManager.IMAGE );
    new ViewManager( normalized_density_ds, IViewManager.IMAGE );
    new ViewManager( signal_ds, IViewManager.IMAGE );
  }

}

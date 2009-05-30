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
import java.io.*;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Numeric.*;

import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.math.*;
import DataSetTools.peak.*;

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
   *    This method merges a list (Vector) of DataSets, each of which 
   *  is an S(Q,E) (or S(Q^2,E)) DataSet and produces a new 
   *  DataSet combining the information from all of the DataSets.  
   *  The DataSets must cover the same range of Q and E values with 
   *  the same number of subdivisions in Q and E.  In most cases the 
   *  DataSets will come from making a QE DataSet, with the same parmeters
   *  but using different detectors.  The resulting combined QE DataSet
   *  can be written to a text file, if the specified filename is not
   *  null and the file is writeable by the user.
   *    The ToQE and ToQ2E operators record an average S(Q,E) array 
   *  normalized by the number of pixels that contribute to a S(Q,E) bin.
   *  (The contribution from an individual pixel is first normalized by
   *  by the solid angle subtended by the pixel.)  These operators also
   *  record the number of pixels that contributed to each S(Q,E) bin.
   *  This allow merging information from multiple S(Q,E) DataSets by
   *  first multiplying the averaged S(Q,E) values by the number of
   *  pixels used, summing across all DataSets, then dividing by the
   *  new total count.  The errors are treated similarly.
   *
   *  @param  dss       List (i.e.Vector) of QE or Q2E DataSets to merge.
   *  @param  filename  The name of the file to write.  If null or blank,
   *                    no file will be written.
   *
   *  @return The combined S(Q,E) DataSet. 
   */
  public static DataSet MergeQE( Vector<DataSet> dss, String filename )
  {
    if ( dss == null )
      throw new IllegalArgumentException( 
                                 "null list of DataSets in MergeQE_DSs" );
    if ( dss.size() <= 0 )
      throw new IllegalArgumentException( 
                                 "empty list of DataSets in MergeQE_DSs" );
    
    int required_rows = -1;
    int required_cols = -1;
    for ( int i = 0; i < dss.size(); i++ )
    {
      Object obj = dss.elementAt(i);
      if ( !(obj instanceof DataSet ) || obj == null )
        throw new IllegalArgumentException( 
          "Element " + i + " not a DataSet in MergeQE_DSs" );
      
      DataSet ds = (DataSet)obj;
      int n_rows = ds.getNum_entries();
      if ( n_rows <= 0 )
        throw new IllegalArgumentException( 
          "DataSet " + i + " empty in MergeQE_DSs" );
      
      if ( required_rows == -1 )
        required_rows = n_rows;
      else if ( n_rows != required_rows )
          throw new IllegalArgumentException( 
              "number of entries don't match in MergeQE_DS\n" +
              "To match first DataSet, need " + required_rows + "\n" + 
              "Data block " + i + " has " + n_rows );
      
      int n_cols = ds.getData_entry(0).getY_values().length;
      if ( n_cols <= 0 )
        throw new IllegalArgumentException( 
          "DataSet " + i + " has no channels in MergeQE_DSs" );

      if ( required_cols == -1 )
        required_cols = n_cols;
      else if ( n_cols != required_cols )       // should check all Data, for
        throw new IllegalArgumentException(     // now just check the first
            "number of columns don't match in MergeQE_DS\n" +
            "To match first DataSet, need " + required_cols + "\n" + 
            "Data block " + i + " has " + n_cols );
    }

    if ( required_rows < 2 )
      throw new IllegalArgumentException
        ("Need at least 2 energy bins in MergeQE, got " + required_rows );

                                     // first build sums of restored QEs,
                                     // variances and numbers of bins
    int n_E_bins = required_rows;
    int n_Q_bins = required_cols;
    int n_samples;
    float err_val;
    float[][] QE_vals   = new float[n_E_bins][n_Q_bins];
    float[][] err_vals  = new float[n_E_bins][n_Q_bins];
    float[][] n_QE_vals = new float[n_E_bins][n_Q_bins];
    for ( int ds_count = 0; ds_count < dss.size(); ds_count++ )
    {
      DataSet ds_1 = dss.elementAt( ds_count );
      for ( int row = 0; row < n_E_bins; row++ )
      {  
        Data data = ds_1.getData_entry( row );
        float[] QE_vals_1 = data.getY_values();
        float[] err_vals_1 = data.getErrors();
        float[] n_QE_vals_1 = AttrUtil.getBinWeights( data );
        for ( int col = 0; col < n_Q_bins; col++ )
        {
          n_samples = (int)n_QE_vals_1[col];
          QE_vals[row][col] += n_samples * QE_vals_1[col];
          err_val = n_samples * err_vals_1[col];
          err_vals[row][col] += err_val * err_val;    // add variances
          n_QE_vals[row][col] += n_samples;
        }
      }
    }
                                               // then change to average
                                               // values for QE, and std_dev
                                               // for error estimates
    for ( int row = 0; row < n_Q_bins; row++ )
      for ( int col = 0; col < n_E_bins; col++ )
        if ( n_QE_vals[row][col] > 0 )
        {
          n_samples = (int)n_QE_vals[row][col];
          QE_vals[row][col]  /= n_samples;
          err_vals[row][col]  = (float)Math.sqrt( err_vals[row][col] );
          err_vals[row][col] /= n_samples;
        }
                                               // finally, put this all back
                                               // as Data blocks in the new
                                               // merged DataSet
    DataSet new_ds = dss.elementAt( 0 ).empty_clone();
    new_ds.setTitle( "COMBINED QE" );
    Data new_data;

    Data old_data = dss.elementAt( 0 ).getData_entry( 0 );
    XScale Q_scale = old_data.getX_scale();
                                                // reconstruct E_scale from
                                                // energy transfer values of
                                                // the rows.  We assume these
                                                // are ordered in reverse from
                                                // the group_ID.

    float E_center_max = AttrUtil.getEnergyTransfer( old_data );

    old_data = dss.elementAt( 0 ).getData_entry( n_E_bins - 1 );

    float E_center_min = AttrUtil.getEnergyTransfer( old_data );

    float delta_E = (E_center_max - E_center_min) / (n_E_bins - 1 );    
    
    XScale E_scale = new UniformXScale( E_center_min - delta_E/2,
                                        E_center_max + delta_E/2,
                                        n_E_bins + 1 );        
                
                                                // Create new DataSet using 
                                                // cuts at constant E.
    for ( int row = 0; row < n_E_bins; row++ ) 
    { 
      float const_e_slice[] = QE_vals[row];
      float slice_errors[]  = err_vals[row];
      float bin_weights[]   = n_QE_vals[row];

      new_data = Data.getInstance( Q_scale, const_e_slice, slice_errors, row+1);

      old_data = dss.elementAt( 0 ).getData_entry( row );
      Attribute e_attr = old_data.getAttribute( Attribute.ENERGY_TRANSFER );
      new_data.setAttribute( e_attr );
      new_data.setLabel( Attribute.ENERGY_TRANSFER );

      Attribute weight_attr = new Float1DAttribute( Attribute.BIN_WEIGHTS,
                                                    bin_weights );
      new_data.setAttribute( weight_attr );

      new_ds.addData_entry( new_data );
    }

    if ( filename != null && filename.trim().length() > 0 )
    {
      String x_units = "inv(A)";
      String x_label = "Momentum Transfer";
      String y_label = "Energy Transfer";
      ErrorString err = PrintToFile( filename, 
                                     Q_scale, 
                                     E_scale, 
                                     x_label, 
                                     x_units,
                                     y_label,
                                     QE_vals,
                                     err_vals  );
      if ( err != null )
      {
        SharedMessages.addmsg( "Didn't write S(Q,E), " + new_ds + 
                           ", to file " + filename );
        return new_ds;
      }
      else
        SharedMessages.addmsg( "Wrote S(Q,E), " + new_ds + 
                               ", to file " + filename);
    }    

    return new_ds;
  }


  /*
   *  Private method to print the array of S(Q,E) (or S(Q^2,E))
   *  values to a file
   */

  private static ErrorString PrintToFile( String file_name,
                                          XScale Q_scale,
                                          XScale E_scale,
                                          String x_label,
                                          String x_units,
                                          String y_label,
                                          float  QE_vals[][],
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
       buff.append("# Row:    " + QE_vals[0].length + "\n");
       buff.append("# Column: " + QE_vals.length + "\n");
       buff.append("# X Label: " + x_label + "\n");
       buff.append("# X Units: " + x_units + "\n");
       buff.append("# Y Label: " + y_label + "\n");
       buff.append("# Y Units: meV \n");
       buff.append("# Z Label: Intensity \n");
       buff.append("# Z Units: Arb. Units\n");
       float x   = 0;
       float y   = 0;
       float val = 0;
       float err = 0;

       int max_row = QE_vals.length - 1;
       int max_col = QE_vals[0].length - 1;
       for( int row = 0; row <= max_row; row++)
         for( int col = 0; col <= max_col; col++)
         {
            x = Q_scale.getX( row );
            y = E_scale.getX( col );
            val = QE_vals[row][max_col - col];     // Data is inverted
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


  /**
   *  Calculate an energy dependent t0 correction for the total
   *  time-of-flight.  The correction should be subtracted from the
   *  experimentally determined time of flight.  The correction is:
   *
   *  t0_shift = 
   * (1+tanh((Ei-a)/b))/(2*c*Ei)+(1-tanh((Ei-a)/b))/(2*d*Ei)+f+g*tanh((Ei-a)/b).
   *
   *  This funtion models the delay time of neutron emission from the 
   *  moderator. (Alexander Kolesnikov).  
   *
   *  @param  Ein               The incident energy
   *  @param  a                 The "a" coefficient in the correction equation
   *  @param  b                 The "b" coefficient in the correction equation
   *  @param  c                 The "c" coefficient in the correction equation
   *  @param  d                 The "d" coefficient in the correction equation
   *  @param  f                 The "f" coefficient in the correction equation
   *  @param  g                 The "g" coefficient in the correction equation
   *
   *  @return  The t0_shift that should be subtracted from the experimentally
   *           measured time-of-flight.
   */
  public static float NDGS_t0_correction( float   Ein,
                                          float   a,
                                          float   b,
                                          float   c,
                                          float   d,
                                          float   f,
                                          float   g )
  {
    double tanh_E   = Math.tanh((Ein-a)/b);
    double t0_shift = (1+tanh_E)/(2*c*Ein)+(1-tanh_E)/(2*d*Ein)+f+g*tanh_E;

    return (float)t0_shift;
  }


  /**
   *  Switch the specified TOF_NDGS DataSet so the Data block's time-of-flight
   *  axes all specify the time-of-flight from the sample to the detector
   *  instead of from the moderator to the detector.  The time-of-flight 
   *  axes are assumed to be the same on all Data blocks in the DataSet.
   *  Three operations are performed.
   *
   *  First, a correction for the total time-of-flight is calculated, based 
   *  on the the experimentally measured time of flight as:
   *  t0_shift = 
   * (1+tanh((Ei-a)/b))/(2*c*Ei)+(1-tanh((Ei-a)/b))/(2*d*Ei)+f+g*tanh((Ei-a)/b).
   *
   *  This funtion models the delay time of neutron emission from the 
   *  moderator(Alexander Kolesnikov).  
   *
   *  The total time-of-flight is then corrected to t_total = t_exp - t0_shift.
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

    float t0_shift = NDGS_t0_correction( Ein, a, b, c, d, f, g );

    SetFinalTOF( ds, Ein, (float)t0_shift, n_channels_delay );
  }


  /**
   *  Switch the specified TOF_NDGS DataSet so the Data block's time-of-flight
   *  axes all specify the time-of-flight from the sample to the detector
   *  instead of from the moderator to the detector.  The time-of-flight 
   *  axes are assumed to be the same on all Data blocks in the DataSet.
   *  Three operations are performed.  A t0 shift value may specified.
   *  The t0 shift value is subtracted from the total time-of-flight.
   *
   *  First, the total time-of-flight is corrected using the 
   *  specified t0_shift value as:  t_total = t_exp - t0_shift.
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
   *  @param  t0_shift          The value to subtract from the total time-of
   *                            flight to get a corrected total 
   *                            time-of-flight.
   *  @param  n_channels_delay  The number of time channels to skip beyond
   *                            the channel where the time-of-flight from the
   *                            sample to detector is 0.  This must be at
   *                            least 10 and more typically should be several
   *                            hundred.
   */
  public static void SetFinalTOF( DataSet ds, 
                                  float   Ein, 
                                  float   t0_shift, 
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

//  System.out.print("Calculated initial_tof : " + initial_tof );
//  System.out.println("  T0 shift = " +  t0_shift );

    for ( int i = 0; i < xs.length; i++ )
       xs[i] = (float)(xs[i] - t0_shift - initial_tof);

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
      new_data.setSqrtErrors( true );

      new_data.setAttributeList(attr_list);
      new_data.setAttribute( Ein_attr );
      new_data.setAttribute( intof_attr );
                                               // check/set attributes that
                                               // are needed later
      Float solid_angle = AttrUtil.getSolidAngle(new_data);
      Float delta2theta = AttrUtil.getDelta2Theta(new_data);
      if ( Float.isNaN(solid_angle) || Float.isNaN(delta2theta) )
      {
        PixelInfoList pil = AttrUtil.getPixelInfoList(new_data);

        if ( pil != null )
        {
          if ( Float.isNaN(solid_angle) )
          {
            solid_angle = pil.SolidAngle();
            new_data.setAttribute(
                     new FloatAttribute(Attribute.SOLID_ANGLE,solid_angle) );
          }
          if ( Float.isNaN(delta2theta) )
          {
            delta2theta = pil.Delta2Theta();
            new_data.setAttribute(
                    new FloatAttribute(Attribute.DELTA_2THETA,delta2theta) );
          }
        }
      }

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
    if ( Float.isNaN( Ein ) )
    {
      SharedMessages.addmsg(
            "WARNING: EnergyFromMonitorData FAILED, using Ein_estimate.." ); 
      return Ein_estimate;
    }

    return Ein;
  }


  /**
   *  Calculate the area under the specified peak in the specified
   *  monitor.  The peak to integrate is determined by finding the
   *  largest histogram bin within the specified time-of-flight 
   *  interval around the specified incident energy.  An estimate of
   *  a linear background is subtracted from the peak area.
   *
   *  @param mon_ds             A monitor DataSet containing the specified
   *                            beam monitor data.
   *  @param mon_id             The id of the monitor to integrate.
   *  @param Ein_estimate       Estimate of the incident energy(meV), used to 
   *                            find the time-of-flight that will be at the 
   *                            center of a window containing the incident
   *                            neutron pulse in the monitor spectrum.
   *  @param tof_half_interval  Half-width in microseconds of the 
   *                            time-of-flight windows containing the
   *                            incident neutron pulse in monitor spectra.
   *                            The search for the peak is restricted to
   *                            the time-of-flight corresponding to the
   *                            estimated Ein, plus or minus the 
   *                            tof_half_interval.
   *  @param extent_factor      The extent factor to use for the peak.  The
   *                            peak will be considered to have zero counts
   *                            outside of the interval extent_factor*FWHM.
   *                            The integration and background estimate is
   *                            done on the interval of length 
   *                            extent_factor*FWHM.
   *
   *  @return The integrated monitor counts over the interval of length
   *          extent_factor*FWHM, centered on the peak maximum,
   *          minus an estimated linear background.
   */
  public static float MonitorPeakArea(  DataSet  mon_ds,
                                        int      mon_id,
                                        float    Ein_estimate,
                                        float    tof_half_interval,
                                        float    extent_factor )
  {
    if ( mon_ds == null )
      throw new IllegalArgumentException( "Monitor DataSet is null" );

    if ( mon_ds.getNum_entries() < 1 )
      throw new IllegalArgumentException( "no data in Monitor DataSet");

    float initial_path = AttrUtil.getInitialPath( mon_ds );
    Data  data = mon_ds.getData_entry_with_id( mon_id );
    HistogramTable mon_data = RestrictMonitor( data,
                                               initial_path,
                                               Ein_estimate,
                                               tof_half_interval );

    HistogramDataPeak peak = new HistogramDataPeak( mon_data, extent_factor );
    peak.setEvaluationMode( IPeak.PEAK_ONLY );

    System.out.println("PEAK in monitor: " + mon_id  );
    System.out.println("position  : " + peak.getPosition() );
    System.out.println("amplitude : " + peak.getAmplitude() );
    System.out.println("FWHM      : " + peak.getFWHM() );
    System.out.println("area      : " + peak.Area() );
    System.out.println("extent    : " + peak.getExtent_factor() );
    return peak.Area();
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
    float Ein_estimate = 45;

//    String filename = "/usr2/SEQUOIA/SEQ_223.nxs";
//    float Ein_estimate = 92;


    NexusRetriever retriever = new NexusRetriever( filename );
    retriever.RetrieveSetUpInfo(null);                        // get cache info

    DataSet mon_ds = retriever.getDataSet(0);
    float Ein = EnergyFromMonitors( mon_ds,
                                    Ein_estimate,
                                    700 );
    System.out.println( "Calculated Ein is : " + Ein );

    float tof_int = 700;
    float ext_f = 8.5f;
    float mon_1_peak_area = MonitorPeakArea( mon_ds, 1, Ein, tof_int, ext_f );
    float mon_2_peak_area = MonitorPeakArea( mon_ds, 2, Ein, tof_int, ext_f );
    System.out.println("monitor 1 area = " + mon_1_peak_area );
    System.out.println("monitor 2 area = " + mon_2_peak_area );

    DataSet one_ds = retriever.getDataSet(10);                // ds 10 has data

//  float Ein =  45.504f;
    float a   =  Ein_estimate;
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


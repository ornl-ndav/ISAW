/*
 * File:  tof_data_calc.java    
 *
 * Copyright (C) 1999, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.7  2001/04/25 20:56:57  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.6  2001/01/29 21:06:00  dennis
 *  Now uses CVS revision numbers.
 *
 *  Revision 1.5  2000/12/15 05:16:46  dennis
 *  Fixed bugs in preliminary version of IncSpecFocus:
 *  1.Bank angles are really 2*theta
 *  2.Now works for very small angle ranges, eg. one detector
 *  3.New Data's Detector position was set wrong.
 *
 *  Revision 1.4  2000/12/13 00:09:26  dennis
 *  Added static method IncSpecFocus to focus the incident spectrum
 *  to a bank of detectors for a powder diffractometer.
 *
 */

package DataSetTools.math;

import DataSetTools.peak.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;

/**
 *  Basic time-of-flight calculations on DataSets and Data blocks
 */
public final class tof_data_calc
{

public static final float  MONITOR_PEAK_EXTENT_FACTOR = 8.5f;
                                  // determines interval over which the 
                                  // monitor peaks are evaluated
 
  /**
   * Don't let anyone instantiate this class.
   */
  private tof_data_calc() {}


/* ------------------------- EnergyFromMonitorData ------------------------- */
/**
 *  Calculate the incident energy of a neutron beam based on the pulse data
 *  for two beam monitors. 
 *
 *  @param  mon_1_data   The Data block for the first beam monitor
 *  @param  mon_2_data   The Data block for the second beam monitor
 *
 *  @return  The energy of the beam in meV
 */

 public static float EnergyFromMonitorData( Data mon_1_data, Data mon_2_data )
  {
     Data mon[] = new Data[2];
     mon[0] = mon_1_data;
     mon[1] = mon_2_data;
                                       // for each monitor, find the peak
                                       // centroid and x position of the
                                       // detector
     float centroid[] = new float[2];
     float x[]        = new float[2];
     for ( int i = 0; i < 2; i++ )
     {
       HistogramDataPeak peak = 
                  new HistogramDataPeak( mon[i], MONITOR_PEAK_EXTENT_FACTOR );

       float area        = peak.Area();
       centroid[i] = peak.Moment( 0, 1) / area;

       DetectorPosition position = (DetectorPosition)
                         mon[i].getAttributeValue(Attribute.DETECTOR_POS);
       float coords[] = position.getCartesianCoords();
       x[i] = coords[0];
     }

     float energy = tof_calc.Energy( x[1]-x[0], centroid[1]-centroid[0] );
     return energy;
  }


/* --------------------------- getEfficiencyFactor ----------------------- */
/**
 *  Calculate detector efficiency factor and correction to flight path for
 *  a detector, based on the neutron final speed.  This code was adapted from
 *  Chun Loong's FORTRAN version by Dongfeng Chen.
 *
 *  @param  finalSpeed    The speed of the detected neutron in meters per 
 *                        micro-second.
 *  @param  detectorType  Integer code for the detector type__________________
 *
 *  @return an array with two values, the detector efficiency factor at the
 *          specified final speed and a flight path correction to be added 
 *          to the flight path for the specified final speed.
 */
 public static float[] getEfficiencyFactor(float finalSpeed, 
                                           int   detectorType      )
 {
    finalSpeed *= 1000000;   // Originally this function used finalSpeed in
                             // meters per second.

                       // SIGMA(detectorType) is macroscopic absorption 
                       // cross section at 2200 m/sec

    float[] SIGMA = { 0.80017f, 0.80017f, 1.0669f, 1.0669f, 0.80017f };

    float R    = 1.27f*(1.0f-0.063f);
    float DR   = R/100.0f;
    float ABS  = 0.0f;

    float FPCORR = 0.0f;

    float Y     = 0.0f;
    float X     = 0.0f;
    float SIGX  = 0.0f;
    float TRANS = 0.0f;

    for (int i=0; i<100; i++)
    {
      Y      = DR * (i+1-0.5f);
      X      = (float)Math.sqrt( R * R - Y * Y );
      SIGX   = X * SIGMA[ detectorType ] * 2200.0f / finalSpeed;
      TRANS  = (float)Math.exp( -2.0f*SIGX );
      ABS    = ABS + ( 1.0f-TRANS );
      FPCORR = FPCORR - X *( 1.0f-1.0f/SIGX + (1.0f+1.0f/SIGX) * TRANS );
     }

     float [] result = new float[2];

     result[0] = ABS / 100.0f;           // the detector efficiency EFF
     result[1] = FPCORR / ABS / 100.0f;  // the flight path correction FPCORR

     return result;
    }


/**
 *  Focus the incident spectrum from a beam monitor to a bank of detectors
 *  at a specified total flight path length and range of angles.  This
 *  function is based on the FORTRAN SUBROUTINE inc_spec_focus from IPNS.
 *
 *  @param  monitor_spec     Data block containing the incident beam monitor
 *                           data.
 *  @param  new_tof_scale    XScale giving the time of flight range to which
 *                           the incident spectrum is to be focused.
 *  @param  new_path_length  The total flight path length for the bank to
 *                           which the spectrum is focussed. 
 *  @param  theta            The nominal scattering angle for the bank to 
 *                           which the spectrum is focussed.
 *  @param  theta_min        The minimum scattering angle for the bank to 
 *                           which the spectrum is focussed.
 *  @param  theta_max        The maximum scattering angle for the bank to 
 *                           which the spectrum is focussed.
 *  @param  new_group_ID     The group_ID to be used for the focused spectrum
 *
 *  @return   A new Data block with the specified tof "x" scale, position, 
 *            group ID containing the focused incident spectrum values.
 */
public static Data IncSpecFocus( Data    monitor_spec, 
                                 XScale  new_tof_scale, 
                                 float   new_path_length,
                                 float   theta,
                                 float   theta_min,
                                 float   theta_max,
                                 int     new_group_ID  )
{
                            // First get the position and path length values
                            // from the monitor Data block.  Check that they
                            // exist and that the position is on the beam
                            // line.

  AttributeList attr_list   = monitor_spec.getAttributeList();
  DetectorPosition position = (DetectorPosition)
                       attr_list.getAttributeValue( Attribute.DETECTOR_POS );

  if ( position == null )
  {
    System.out.println("ERROR:No Detector Position attribute in InSpecFocus()");
    return null;
  }

  Float initial_path_obj = (Float)
                        attr_list.getAttributeValue(Attribute.INITIAL_PATH);
  if ( initial_path_obj == null )
  {
    System.out.println("ERROR: No Initial Path attribute in InSpecFocus()");
    return null;
  }

  float initial_path       = initial_path_obj.floatValue();
  float cartesian_coords[] = position.getCartesianCoords();
  float path_length        = initial_path + cartesian_coords[0];

  if ( Math.abs( cartesian_coords[1] ) > 0.01 || 
       Math.abs( cartesian_coords[2] ) > 0.01  )
  {
    System.out.println("ERROR: Not monitor Data in InSpecFocus()");
    return null;
  }

                           // Now get the x and y values from the monitor Data
                           // as well as the new x values and an array for the
                           // y values.

  float old_y[] = monitor_spec.getY_values();
  float old_x[] = monitor_spec.getX_scale().getXs();
  float new_x[] = new_tof_scale.getXs();
  float new_y[] = new float[ new_x.length-1 ];

                           // The angles specify the scattering angle "2 theta"
                           // so divide by 2 when converting to radians.
  float path_length_ratio = path_length/new_path_length;
  float min_theta_ratio   = (float)(Math.sin( theta_min * Math.PI/360.0 ) / 
                                    Math.sin( theta * Math.PI/360.0 )     );
  float max_theta_ratio   = (float)(Math.sin( theta_max * Math.PI/360.0 ) / 
                                    Math.sin( theta * Math.PI/360.0 )     );

  float new_tof,
        min_tof,
        max_tof;
  float total;
  int   min_index,
        max_index;
  float first_fraction,
        last_fraction;

  for ( int i = 0; i < new_y.length; i++ )   // for each bin in the focused spec
  {
    new_tof = (new_x[i] + new_x[i+1]) / 2;   // find tof at center of bin and
                                             // map that tof back to monitor
                                             // spectrum, using interval of 
                                             // angles. 
    min_tof =  path_length_ratio * min_theta_ratio * new_tof;
    max_tof =  path_length_ratio * max_theta_ratio * new_tof;

    if ( min_tof > max_tof )
    {
      float temp = min_tof;
      min_tof    = max_tof;
      max_tof    = temp;
    }

    min_index = arrayUtil.get_index_of( min_tof, old_x );
    max_index = arrayUtil.get_index_of( max_tof, old_x );

    if ( min_index != -1 && max_index != -1 && max_index < new_x.length-1 )
    {
      if ( min_index == max_index )          // degenerates to one bin, use it
        new_y[i] = old_y[ min_index ];
      
      else                                   // sum up counts from part of first
      {                                      // bin, all of the complete bins
                                             // in the middle and part of last
                                             // bin
        first_fraction = ( old_x[ min_index + 1 ] - min_tof ) /
                         ( old_x[ min_index + 1 ] - old_x[ min_index ] ) ;
        total = first_fraction * old_y[ min_index ];

        for ( int j = min_index + 1; j < max_index; j++ )
          total += old_y[j];

        last_fraction = ( max_tof            - old_x[ max_index ] ) /
                        ( old_x[ max_index + 1 ] - old_x[ max_index ] ) ;
        total += last_fraction * old_y[ max_index ];
                                               // divide by the number of bins 
                                               // summed.  For non-uniform time
                                               // scales this is NOT correct.
        new_y[i] = total / 
                 (first_fraction + max_index - min_index - 1 + last_fraction);
      }
    }
    else
      new_y[i] = 0;
  }

  Data new_data = new Data( new_tof_scale, new_y, new_group_ID );
  new_data.setAttribute( new FloatAttribute( Attribute.INITIAL_PATH, 
                                             initial_path )); 
  DetectorPosition det_pos = new DetectorPosition();
  det_pos.setSphericalCoords( new_path_length-initial_path, 
                             (float)(theta * Math.PI / 180.0), 
                             (float)Math.PI/2  );
  new_data.setAttribute( new DetPosAttribute( Attribute.DETECTOR_POS,
                                              det_pos ) ); 
//  System.out.println("radius = " + (new_path_length-initial_path) );
//  System.out.println("theta  = " + theta );
//  System.out.println("phi    = " + Math.PI/2  );
//  System.out.println( "Detector Postion = " + det_pos );
//  float junk[] = det_pos.getSphericalCoords();
//  System.out.println( "Sphereical coords = " + junk[0] + 
//                                        ", " + junk[1] + 
//                                        ", " + junk[2]  );
  return new_data;
}
 

/* -------------------------------------------------------------------------
 *
 *  main program for test purposes only
 *
 */
  public static void main( String[] args )
  {
    float x[]          = new float[1000];
    float eff_arr[]    = new float[1000];
    float fpcorr_arr[] = new float[1000];

    float final_speed,
          e_meV,
          eff,
          fpcorr;
    float result[];
    for ( int i = 0; i < 1000; i++ )
    {
      final_speed = 0.001f + i * 0.00001f;
      e_meV       = tof_calc.EnergyFromVelocity(final_speed);
      result      = getEfficiencyFactor( final_speed, 1 );
      eff         = result[0];
      fpcorr      = result[1];
      System.out.print("speed(m/us) = " + Format.real(final_speed,8,5) + "  " );
      System.out.print("E(meV) = " + Format.real( e_meV, 8, 2 ));
      System.out.print(" EFF = " + Format.real( eff, 10, 7 ) );
      System.out.println(" FPCORR = "  + Format.real( fpcorr, 10, 7) );
      x[i]          = final_speed;
      eff_arr[i]    = eff;
      fpcorr_arr[i] = fpcorr;
    }

    final_speed = 0.001f; 
    for ( int i = 0; i < 10; i++ )
    {
      final_speed = final_speed + 0.001011f; 
      eff    = arrayUtil.interpolate( final_speed, x, eff_arr );
      fpcorr = arrayUtil.interpolate( final_speed, x, fpcorr_arr );
      System.out.println("INTERPOLATED VALUES ......" );
      System.out.print("speed(m/us) = " + Format.real(final_speed,8,5) + "  " );
      System.out.print(" EFF = " + Format.real( eff, 10, 7 ) );
      System.out.println(" FPCORR = "  + Format.real( fpcorr, 10, 7) );

      result      = getEfficiencyFactor( final_speed, 1 );
      eff         = result[0];
      fpcorr      = result[1];
      System.out.println("CALCULATED VALUES ......" );
      System.out.print("speed(m/us) = " + Format.real(final_speed,8,5) + "  " );
      System.out.print(" EFF = " + Format.real( eff, 10, 7 ) );
      System.out.println(" FPCORR = "  + Format.real( fpcorr, 10, 7) );
    }

    for ( int i = 0; i <= 100; i++ )
    {
      e_meV = i * 20;
      final_speed = tof_calc.VelocityFromEnergy( e_meV );
      System.out.print("speed(m/us) = " + Format.real(final_speed,8,5) + "  ");
      System.out.println("E(meV) = " + Format.real( e_meV, 8, 2 ));
    }

  }

  

}

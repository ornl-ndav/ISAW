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
 *  Revision 1.16  2002/11/27 23:15:47  pfpeterson
 *  standardized header
 *
 *  Revision 1.15  2002/07/10 20:23:05  dennis
 *  Removed debug print.
 *
 *  Revision 1.14  2002/07/10 20:10:30  dennis
 *  NewEnergyInData() method that adjusts time scales for new Ein value
 *  for DG_Spectrometers now maintains a UniformXScale if the original Data
 *  block has a UniformXScale.
 *
 *  Revision 1.13  2002/07/10 15:57:21  pfpeterson
 *  Contains time focusing code.
 *
 *  Revision 1.12  2002/07/08 15:43:52  pfpeterson
 *  Added simple time focusing method.
 *
 *  Revision 1.11  2002/06/03 22:27:13  dennis
 *  Added method: NewEnergyInData( data, new_e_in ) to adjust a spectrum to
 *  to a new incident energy.  The time bin boundaries, as well as the
 *  incident energy and source to sample attributes are adjusted.  The ratio
 *  of the focused position, L2', to the physical position, L2, is used in
 *  making the adjustment to the times.  Specifically, the adjusted times
 *  t2'_corr are calculated from the focused times t2' and the source to sample
 *  times for the two energies using: t1(E1) and t1(E2):
 *  t2'_corr = t2' + ( t1(E1) - t1(E2) )*(L2'/L2)
 *
 *  Revision 1.10  2002/05/02 19:04:12  pfpeterson
 *  Only integrate one sigma to calculate the first moment.
 *
 *  Revision 1.9  2002/04/04 19:17:20  dennis
 *  Removed some debug print statements.
 *
 *  Revision 1.8  2002/03/13 16:22:12  dennis
 *  Converted to new abstract Data class.
 *
 */

package DataSetTools.math;

import DataSetTools.peak.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.instruments.*;

/**
 *  Basic time-of-flight calculations on DataSets and Data blocks
 */
public final class tof_data_calc
{

public static final float  MONITOR_PEAK_EXTENT_FACTOR = 1.0f;
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
       centroid[i] = peak.Moment(0, 1) / area;

       DetectorPosition position = (DetectorPosition)
                         mon[i].getAttributeValue(Attribute.DETECTOR_POS);
       float coords[] = position.getCartesianCoords();
       x[i] = coords[0];
     }

     System.out.println("EnergyFromMonitorData ");
     System.out.println("Monitor 0 peak = " + x[0] + 
                        " centroid = " + centroid[0] );
     System.out.println("Monitor 1 peak = " + x[1] + 
                        " centroid = " + centroid[1] );

     float energy = tof_calc.Energy( x[1]-x[0], centroid[1]-centroid[0] );

     System.out.println("Energy = " + energy +" meV");
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
 *  Adjust the time bin boundaries for a direct geometry neutron time-of-flight
 *  spectrometer to values that correspond to a different incident energy.
 *  The data is assumed to be already time focussed to an effective secondary
 *  flight path, L2_focused, given by the "Effective Position" attribute.  The
 *  physical secondary flight path, L2, is assumed to be given by the 
 *  "Det Info List" attribute.  If either of these attributes are not present,
 *  a warning message is printed and the original Data block is returned.
 *
 *  @param  data      Data block containing the original focused spectrum to be 
 *                    adjusted.
 *
 *  @param  new_e_in The new incident energy for which the spectrum time
 *                    boundaries are to be adjusted.
 * 
 *  @return   A new Data block with it's time-of-flight "x" scale adjusted
 *            to correspond to the new incident energy.  If the needed 
 *            attributes are not present the original Data block is returned.
 */
public static Data NewEnergyInData( TabulatedData  data, 
                                    float          new_e_in )
{
  Float Float_e = (Float)data.getAttribute(Attribute.ENERGY_IN).getValue();
  Float Float_l = (Float)data.getAttribute(Attribute.INITIAL_PATH).getValue();
  DetectorPosition position = (DetectorPosition)
                     data.getAttribute(Attribute.DETECTOR_POS).getValue();

  DetectorInfo det_info[] =(DetectorInfo[])
                     data.getAttribute(Attribute.DETECTOR_INFO_LIST).getValue();
 
  if ( Float_e   == null || 
       Float_l   == null || 
       position  == null ||
       det_info  == null )
  {
    System.out.println("ERROR: missing attribute in " +
                              "tof_data_calc.SetNewEnergyIn");
    return data;
  }

  float focused_L2 = position.getDistance();
  float physical_L2 = 0;                        // use average of physical dists
  for ( int i = 0; i < det_info.length; i++ )
    physical_L2 += det_info[i].getPosition().getDistance();  
  physical_L2 /= det_info.length; 
     
  float old_e_in     = Float_e.floatValue();
  float initial_path = Float_l.floatValue();

  float t_old = tof_calc.TOFofEnergy( initial_path, old_e_in );
  float t_new = tof_calc.TOFofEnergy( initial_path, new_e_in );

  float delta_t = (t_new - t_old)*( focused_L2 / physical_L2 );
/*
  System.out.println("foc L2, phys L2, ratio = " + focused_L2  + ", " + 
                                                   physical_L2 + ", " +
                                                   focused_L2 / physical_L2 );
*/

  XScale x_scale;
  if ( data.getX_scale() instanceof UniformXScale )
  {
    float start_x = data.getX_scale().getStart_x() - delta_t;
    float end_x   = data.getX_scale().getEnd_x()   - delta_t;
    int   num_x   = data.getX_scale().getNum_x();
    x_scale = new UniformXScale( start_x, end_x, num_x );
  }
  else
  {
    float x[] = data.getX_scale().getXs();
    for ( int k = 0; k < x.length; k++ )
      x[k] -= delta_t;
    x_scale = new VariableXScale( x );
  }
                                          // make a new Data block with the new
                                          // x values and same group ID, y
                                          // values and attributes.
  float y[] = data.getY_values();
  TabulatedData new_d = (TabulatedData)
                         Data.getInstance( x_scale, y, data.getGroup_ID() );
  new_d.setAttributeList( data.getAttributeList() );
  new_d.setErrors( data.getCopyOfErrors() );
  new_d.setAttribute( new FloatAttribute(Attribute.ENERGY_IN, new_e_in) );
  new_d.setAttribute( new FloatAttribute(Attribute.SOURCE_TO_SAMPLE_TOF,t_new));

  return new_d;
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

  Data new_data = Data.getInstance( new_tof_scale, new_y, new_group_ID );
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
 
/**
 * Performs timefocusing on the provided XScale using the specified
 * positions.
 *
 * @param scale       The Xscale to focus.
 * @param path_length The total path length, in meters, the data was
 *                    taken at.
 * @param theta_rad   The angle, in radians, that the data was taken at.

 * @param focus_path_length The total path length, in meters, the data
 *                    was taken at.
 * @param focus_theta_rad The angle, in radians, that the data was
 *                    taken at.
 */
public static XScale DiffractometerFocus(XScale scale,
                                         float path_length,
                                         float theta_rad,
                                         float focus_path_length,
                                         float focus_theta_rad){
    XScale new_scale=null;
    float x_vals[]=scale.getXs();

    float scalar=1f;
    if(focus_path_length>0){
        scalar*=focus_path_length/path_length;
    }
    scalar*=Math.sin(focus_theta_rad)/Math.sin(theta_rad);

    if(scalar!=1.f){
        for( int i=0 ; i<x_vals.length ; i++ ){
            x_vals[i]*=scalar;
        }
        new_scale=new VariableXScale(x_vals);
    }
    return new_scale;
}

/**
 * Performs timefocusing on the provided XScale using the specified
 * DIFCs.
 *
 * @param scale       The XScale to focus.
 * @param dif_c       DIFC that the data block is currently at.
 * @param focus_dif_c DIFC that the data will be focused to.
 */
public static XScale DiffractometerFocus(XScale scale,
                                         float dif_c, float focus_dif_c){
    XScale new_scale=null;
    float x_vals[]=scale.getXs();
    float scalar=1f;

    if(focus_dif_c>0f)
        scalar=scalar*focus_dif_c;

    if(dif_c>0f)
        scalar=scalar/dif_c;

    if(scalar!=1f){
        for( int i=0 ; i<x_vals.length ; i++ ){
            x_vals[i]*=scalar;
        }
        new_scale=new VariableXScale(x_vals);
    }

    return new_scale;
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

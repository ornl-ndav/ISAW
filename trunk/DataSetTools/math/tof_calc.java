/*
 * File:  tof_calc.java 
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
 *  Revision 1.13  2001/07/12 16:32:42  dennis
 *  Modified calculation of "omega".
 *
 *  Revision 1.12  2001/07/10 20:23:24  dennis
 *  Added method to calculate Omega()
 *
 *  Revision 1.11  2001/04/25 20:56:54  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.10  2001/01/29 21:05:58  dennis
 *  Now uses CVS revision numbers.
 *
 *  Revision 1.9  2000/08/01 20:46:29  dennis
 *  Removed "sampling" related methods such as ReBin and CLSmooth
 *
 *  Revision 1.5  2000/07/26 20:47:00  dennis
 *  Now allow zero parameter in velocity<->energy conversions
 *
 *  Revision 1.4  2000/07/17 19:07:29  dennis
 *  Added methods to convert between energy and wavelength
 *
 *  Revision 1.3  2000/07/14 19:10:23  dennis
 *  Added methods to convert between velocity and energy and between
 *  velocity and wavelength.  Also added documentation to clarify the
 *  units used for parameters.
 *
 *  Revision 1.2  2000/07/10 22:25:15  dennis
 *  Now Using CVS 
 *
 *  Revision 1.5  2000/07/06 21:19:59  dennis
 *  added some constants for Planck's constant and added VelocityOfEnergy()
 *  function.
 *
 *  Revision 1.3  2000/05/11 16:08:13  dennis
 *  Added RCS logging
 *
 */

package DataSetTools.math;

/**
 *  This contains basic conversions for time-of-flight neutron scattering
 *  experiments. ( Ported to Java from tof_vis_calc.c )
 */
public final class tof_calc
{

/* --------------------------------------------------------------------------

   CONSTANTS

*/

                                                         // mass of neutron(kg)
public static final float  MN_KG        = 1.67492716e-27f;

public static final float  JOULES_PER_meV=  1.602176462e-22f;

                                                      //h in Joule seconds
public static final float  H_JS          =  6.62606876e-34f;

                                                      // h in erg seconds
public static final float  H_ES          =  6.62606876e-27f;

                                                      // h_bar in Joule seconds
public static final float  H_BAR_JS      =  1.05457160e-34f;

                                                      // h in erg seconds
public static final float  H_BAR_ES      =  1.05457160e-27f;   


public static final float  meV_per_mm_per_us_2 = 5.227037f;    // meV/(mm/us)^2 

public static final float  ANGST_PER_US_PER_M  = 3.956058e-3f;

public static final float  ANGST_PER_US_PER_MM = 3.956058f;

public static final float  RADIANS_PER_DEGREE  = 0.01745332925f;
 
  /**
   * Don't let anyone instantiate this class.
   */
  private tof_calc() {}


/* --------------------------- Energy -------------------------------- */
/**
 *   Calculate the energy of a neutron based on the time it takes to travel
 *   a specified distance.
 *
 *   @param path_len_m  The distance traveled in meters.
 *   @param time_us     The time in microseconds for the neutron to travel 
 *                      the distance.
 *
 *   @return The energy of the neutron in meV
 */
public static float Energy( float path_len_m, float time_us )
{
  float   v;
  float   energy;

  if ( time_us <= 0.0f )                     /* NOT MEANINGFUL */
    return( Float.NaN );

  v = (path_len_m * 1000.0f)/ time_us;        /*   velocity in mm/us    */
  energy = meV_per_mm_per_us_2 * v * v; 
  return( energy );  
}


/* ----------------------------- TOFofEnergy ---------------------------- */
/**
 *   Calculate the time it takes a neutron of a specified energy to travel
 *   a specified distance.
 *
 *   @param path_len_m  The distance traveled in meters.
 *   @param e_meV       The energy of the neutron in meV.
 *
 *   @return  The time in microseconds for the neutron to travel the distance.
 */


public static float  TOFofEnergy( float path_len_m, float e_meV )
{
  return (float)( path_len_m * 1000.0/Math.sqrt( e_meV/meV_per_mm_per_us_2 ));
}


/* ------------------------- VelocityFromEnergy ---------------------------- */
/**
 *   Calculate the velocity of a neutron based on it's energy.
 *
 *   @param e_meV       The energy of the neutron in meV.
 *
 *   @return The velocity of a neutron in meters per microsecond. 
 */

public static float VelocityFromEnergy( float e_meV )
{

  if ( e_meV < 0.0f )                        /* NOT MEANINGFUL */
    return( Float.NaN );

  float   v_m_per_us = (float)Math.sqrt( e_meV / meV_per_mm_per_us_2 )/1000;

  return( v_m_per_us );
}


/* ------------------------- EnergyFromVelocity ---------------------------- */
/**
 *   Calculate the energy of a neutron based on it's velocity
 *
 *   @param v_m_per_us  The velocity of a neutron in meters per microsecond. 
 *
 *   @return The energy of the neutron in meV
 */
public static float EnergyFromVelocity( float v_m_per_us )

{
  if ( v_m_per_us < 0.0f )                     /* NOT MEANINGFUL */
    return( Float.NaN );

  float   e_meV = v_m_per_us * v_m_per_us * 1000000 * meV_per_mm_per_us_2;

  return( e_meV );
}


/* ------------------------ EnergyFromWavelength --------------------------- */
/**
 *  Calculate the energy of a neutron based on it's wavelength 
 *
 *   @param wavelength_A  The wavelength of the neutron in Angstroms. 
 *
 *   @return The energy of the neutron in meV
 */
public static float EnergyFromWavelength( float wavelength_A )
{

  if ( wavelength_A <= 0.0f )                     /* NOT MEANINGFUL */
    return( Float.NaN );

  float  v_m_per_us = ANGST_PER_US_PER_M / wavelength_A;
  float  e_meV      = v_m_per_us * v_m_per_us * 1000000 * meV_per_mm_per_us_2;

  return( e_meV );
}



/* ------------------------ WavelengthFromEnergy --------------------------- */
/**
 *  Calculate the wavelength of a neutron based on it's energy
 *
 *   @param e_meV       The energy of the neutron in meV.
 *
 *   @return The wavelength of the neutron in Angstroms. 
 */
public static float WavelengthFromEnergy( float e_meV )
{

  if ( e_meV <= 0.0f )                     /* NOT MEANINGFUL */
    return( Float.NaN );

  float   v_m_per_us = (float)Math.sqrt( e_meV / meV_per_mm_per_us_2 )/1000;
  float   wavelength_A = ANGST_PER_US_PER_M / v_m_per_us;

  return( wavelength_A );
}



/* ------------------------ WavelengthFromVelocity ------------------------- */
/**
 *   Calculate the wavelength of a neutron based on it's velocity.
 *
 *   @param v_m_per_us  The velocity of a neutron in meters per microsecond.
 *   
 *   @return The wavelength of the neutron in Angstroms. 
 */
public static float WavelengthFromVelocity( float v_m_per_us )

{
  if ( v_m_per_us <= 0.0f )                     /* NOT MEANINGFUL */
    return( Float.NaN );

  float  wavelength_A = ANGST_PER_US_PER_M / v_m_per_us;

  return( wavelength_A );
}


/* ------------------------ VelocityFromWavelength ------------------------- */
/**
 *   Calculate the velocity of a neutron based on it's wavelength.
 *   
 *   @param wavelength_A  The wavelength of the neutron in Angstroms. 
 *   
 *   @return The velocity of a neutron in meters per microsecond.
 */

public static float VelocityFromWavelength( float wavelength_A )
{
  if ( wavelength_A <= 0.0f )                     /* NOT MEANINGFUL */
    return( Float.NaN );

  float  v_m_per_us = ANGST_PER_US_PER_M / wavelength_A;

  return( v_m_per_us );
}



/* ----------------------------- Wavelength -------------------------- */
/**
 *   Calculate the wavelength of a neutron based on a distance traveled 
 *   and the time it took to travel that distance.
 *
 *   @param path_len_m  The distance traveled in meters.
 *   @param time_us     The time in microseconds for the neutron to travel 
 *                      the distance.
 *   
 *   @return The wavelength of the neutron in Angstroms.
 */

public static float Wavelength( float path_len_m, float time_us )
{
                                 /* convert time in microseconds to time    */
                                 /* in seconds.  Calculate the wavelength   */
                                 /* in meters and then convert to Angstroms */
  return( ANGST_PER_US_PER_M * time_us / path_len_m );
}


/* ---------------------------- TOFofWavelength ------------------------- */
/**
 *   Calculate the time it takes a neutron of a specified wavelength to travel
 *   a specified distance.
 *
 *   @param path_len_m    The distance traveled in meters.
 *   @param wavelength_A  The wavelength of the neutron in Angstroms. 
 *
 *   @return  The time in microseconds for the neutron to travel the distance.
 */


public static float TOFofWavelength( float path_len_m, float wavelength_A )
{
  return( wavelength_A * path_len_m / ANGST_PER_US_PER_M );
}


/* -------------------------------- DSpacing ----------------------------- */
/**
 *   Calculate a "D" value based on the scattering angle, total flight path 
 *   length and time of flight for a neutron that was scattered by a sample.
 *
 *   @param angle_radians   The angle between the neutron beam and the line 
 *                          from the sample to the detector, in radians.
 *   @param path_len_m      The distance from the moderator to the detector
 *                          in meters.
 *   @param time_us         The time in microseconds for the neutron to travel 
 *                          the distance from the moderator to the detector.
 *   
 *   @return The corresponding "D" value in Angstroms.
 *
 */
public static float  DSpacing( float angle_radians, 
                               float path_len_m, 
                               float time_us )
{
  float wavelength;
  float theta_radians;

  wavelength    = ANGST_PER_US_PER_M * time_us / path_len_m;
  theta_radians = Math.abs( angle_radians / 2.0f );

  return (float)( wavelength / (2.0 * Math.sin( theta_radians ) ) ); 
}


/* ---------------------------- TOFofDSpacing ---------------------------- */
/**
 *   Calculate the time-of-flight for a neutron based on the scattering angle, 
 *   total flight path length and a "D" value for sample that scattered 
 *   the neutron beam.
 *
 *   @param angle_radians   The angle between the neutron beam and the line 
 *                          from the sample to the detector, in radians.
 *   @param path_len_m      The distance from the moderator to the detector
 *                          in meters.
 *   @param d_A             The "D" value in Angstroms.
 *                          the distance.
 *   
 *   @return The time in microseconds for the neutron to travel the distance 
 *           from the moderator to the detector.
 *
 */
public static float  TOFofDSpacing( float angle_radians, 
                                    float path_len_m, 
                                    float d_A          )
{
  float  wavelength;
  float  theta_radians;

  theta_radians = Math.abs( angle_radians / 2.0f );
  wavelength    = (float)(2.0 * Math.sin( theta_radians ) * d_A);

  return( wavelength * path_len_m / ANGST_PER_US_PER_M );
}


/* --------------------------- DiffractometerQ --------------------------- */
/**
 *   Calculate a "Q" value based on the scattering angle, total flight path 
 *   length and time of flight for a neutron that was scattered by a sample.
 *
 *   @param angle_radians   The angle between the neutron beam and the line
 *                          from the sample to the detector, in radians.
 *   @param path_len_m      The distance from the moderator to the detector
 *                          in meters.
 *   @param time_us         The time in microseconds for the neutron to travel
 *                          the distance from the moderator to the detector.
 *   
 *   @return The magnitude of "Q" in inverse Angstroms.
 *
 */

public static float  DiffractometerQ( float angle_radians, 
                                      float path_len_m, 
                                      float time_us    )
{
  float  wavelength;
  float  theta_radians;

  wavelength    = ANGST_PER_US_PER_M * time_us / path_len_m;
  theta_radians = Math.abs( angle_radians / 2.0f );

  return (float)( 4.0 * Math.PI * Math.sin( theta_radians ) / wavelength );
}

/* ------------------------ TOFofDiffractometerQ -------------------------- */
/**
 *   Calculate the time of flight for a neutron based on the scattering angle, i
 *   total flight path length and a "Q" value for a sample that scattered
 *   the neutron beam.
 *
 *   @param angle_radians   The angle between the neutron beam and the line
 *                          from the sample to the detector, in radians.
 *   @param path_len_m      The distance from the moderator to the detector
 *                          in meters.
 *   @param Q_invA          Q in inverse Angstroms.
 *  
 *   @return The time in microseconds for the neutron to travel the distance
 *           from the moderator to the detector.
 */

public static float  TOFofDiffractomerQ( float angle_radians, 
                                         float path_len_m, 
                                         float Q_invA     )

{
  float  wavelength;
  float  theta_radians;

  theta_radians = Math.abs( angle_radians / 2.0f );
  wavelength    = (float)( 4.0 * Math.PI * Math.sin( theta_radians ) / Q_invA );

  return( wavelength * path_len_m / ANGST_PER_US_PER_M );
}

/* --------------------------- SpectrometerQ ---------------------------- */
/**
 *   Calculate a "Q" value for a Spectrometer based on the initial energy,
 *   final energy and scattering angle.
 *
 *   @param e_in_meV        The initial energy of a neutron before being  
 *                          scattered by the sample.
 *   @param e_out_meV       The final energy of a neutron after being  
 *                          scattered by the sample.
 *   @param angle_radians   The angle between the neutron beam and the line
 *                          from the sample to the detector, in radians.
 *
 *   @return The magnitude of "Q" in inverse Angstroms.
 *
 */

public static float SpectrometerQ( float e_in_meV, 
                                   float e_out_meV, 
                                   float angle_radians )
{
  float  temp;
  float  two_theta_radians;

  two_theta_radians = Math.abs( angle_radians );
  temp = (float) (e_in_meV + e_out_meV
     - 2.0 * Math.sqrt( e_in_meV * e_out_meV ) * Math.cos( two_theta_radians ));

  if ( temp < 0.0f )
    {
      System.out.println("ERROR in ChopQ ... sqrt of negative number");
      return( -1.0f );
    }
  return (float)( Math.sqrt( temp / 2.0721 ) );
}


/* ------------------------------- Omega -------------------------------- */
/**
 *  Calculates "Omega"
 */
public static float Omega( float two_theta )
{
  float alpha = (two_theta/2 + 135);
  
  if ( alpha < 135 )
    return alpha;
  else
    return (alpha - 180);
}

}

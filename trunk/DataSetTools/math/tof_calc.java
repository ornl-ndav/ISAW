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
 *  Revision 1.23  2003/07/22 15:09:58  dennis
 *  Made all physical constants double precison.  Now calculates h/mn
 *  (approximately 3.9560) in terms of 'h' and the mass of the
 *  neutron, rather than using a numeric constant.
 *
 *  Revision 1.22  2003/07/11 18:46:28  dennis
 *  Added versions of makeEulerRotation() and makeEulerRotationInverse()
 *  that produce the rotations as 3x3 arrays of doubles.
 *
 *  Revision 1.21  2003/05/24 21:50:39  dennis
 *  Fixed spelling of method name TOFofDiffractometerQ().
 *
 *  Revision 1.20  2003/02/18 15:48:33  dennis
 *  Changed documentation for Euler rotation methods to explicitly
 *  state that angles are measured in degrees and are about the
 *  positive axes.
 *
 *  Revision 1.19  2003/01/07 22:30:41  dennis
 *  Added methods makeEulerRotation() and makeEulerRotationInverse() to
 *  calculate matrices representing rotation (and inverse rotations) by Euler
 *  angles phi, chi and omega about the z, x and z axes in that order.
 *
 *  Revision 1.18  2002/11/27 23:15:47  pfpeterson
 *  standardized header
 *
 *  Revision 1.17  2002/07/26 20:25:57  dennis
 *  Added method DiffractometerVecQ() to calculate the Q vector
 *  for elastic scattering.
 *
 *  Revision 1.16  2002/07/10 15:57:44  pfpeterson
 *  Added gsas tof->d and tof->Q calculations.
 *
 *  Revision 1.15  2002/07/02 19:09:24  pfpeterson
 *  Added a method to do Q->wavelength calculations.
 *
 *  Revision 1.14  2002/06/19 21:52:14  pfpeterson
 *  Added more conversions between wl, d, and Q.
 *
 */

package DataSetTools.math;

import DataSetTools.util.*;

/**
 *  This contains basic conversions for time-of-flight neutron scattering
 *  experiments. ( Ported to Java from tof_vis_calc.c )
 */
public final class tof_calc
{

/* --------------------------------------------------------------------------

   CONSTANTS

*/
//
//   "Accepted" values of physical constants and conversion factors 
//   available from:
//   http://physics.nist.gov/cuu/Constants/index.html?/codata86.html
//
                                                         // mass of neutron(kg)
public static final double  MN_KG          = 1.67492716e-27;

public static final double  JOULES_PER_meV = 1.602176462e-22;

                                                          //h in Joule seconds
public static final double  H_JS          =  6.62606876e-34;

                                                           // h in erg seconds
public static final double  H_ES          =  H_JS * 1.0e7; // 6.62606876e-27;

                                                      // h_bar in Joule seconds
                                                      // 1.05457160e-34;
public static final double  H_BAR_JS      =  H_JS/(2*Math.PI);

                                                      // h_bar in erg seconds
                                                      // 1.05457160e-27;
public static final double  H_BAR_ES      =  H_ES/(2*Math.PI);

                                                      //5.227037; meV/(mm/us)^2
public static final double  meV_PER_MM_PER_US_2 = (MN_KG/2)/(JOULES_PER_meV)
                                                 * 1.0e6;

public static final double  ANGST_PER_US_PER_MM = H_ES/MN_KG;  // 3.956058;

public static final double  ANGST_PER_US_PER_M  = ANGST_PER_US_PER_MM/1000;
                                                              //  3.956058e-3;
//public static final float  RADIANS_PER_DEGREE  = 0.01745332925;
 
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
  energy = (float)(meV_PER_MM_PER_US_2 * v * v); 
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
  return (float)( path_len_m * 1000.0/Math.sqrt( e_meV/meV_PER_MM_PER_US_2 ));
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

  float   v_m_per_us = (float)Math.sqrt( e_meV / meV_PER_MM_PER_US_2 )/1000;

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

  float   e_meV = (float)
                  (v_m_per_us * v_m_per_us * 1000000 * meV_PER_MM_PER_US_2);

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

  float  v_m_per_us = (float)(ANGST_PER_US_PER_M / wavelength_A);
  float  e_meV      = (float)
                      (v_m_per_us * v_m_per_us * 1000000 * meV_PER_MM_PER_US_2);

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

  float   v_m_per_us = (float)Math.sqrt( e_meV / meV_PER_MM_PER_US_2 )/1000;
  float   wavelength_A = (float)(ANGST_PER_US_PER_M / v_m_per_us);

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

  float  wavelength_A = (float)(ANGST_PER_US_PER_M / v_m_per_us);

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

  float  v_m_per_us = (float)(ANGST_PER_US_PER_M / wavelength_A);

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
  return (float)(ANGST_PER_US_PER_M * time_us / path_len_m );
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
  return (float)( wavelength_A * path_len_m / ANGST_PER_US_PER_M );
}

/**
 * Calculate the wavelength of a given Q.
 *
 * @param angle_radians The scattering angle.
 * @param diffractometerQ The Q value to be converted
 *
 * @return The wavelength related to the Q given.
 */
public static float WavelengthofDiffractometerQ( float angle_radians, float Q){
    float theta_radians=Math.abs(angle_radians/2.0f);
    return (float)( 4.0 * Math.PI * Math.sin(theta_radians) / Q );
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

  wavelength    = (float)(ANGST_PER_US_PER_M * time_us / path_len_m);
  theta_radians = Math.abs( angle_radians / 2.0f );

  return (float)( wavelength / (2.0 * Math.sin( theta_radians ) ) ); 
}


/* ------------------------------ DSpacing ------------------------------- */
/**
 * Calculate a "D" value based on the empirical relation used by gsas.
 *
 * @param  dif_c  The geometrical portion of the conversion.
 * @param  dif_a  The quadratic portion of the conversion.
 * @param  t_zero The constant offset.
 *
 * @return The magnitude of "D" in Angstroms.
 */
public static float DSpacing( float dif_c, float dif_a,
                              float t_zero, float time_us ){
    // shouldn't solve quadratic if unnecessary
    if(dif_a==0) return (time_us-t_zero)/dif_c;

    // imaginary numbers if t_zero>time_us
    if(t_zero>time_us)return Float.NaN;

    // otherwise do the calculation (only positive root has meaning)
    double num=-1.*dif_c+Math.sqrt(dif_c*dif_c+4.*dif_a*(time_us-t_zero));
    double den=2.*(double)dif_a;
    return (float)(num/den);
}


/* --------------------- DSpacingofDiffractometerQ ----------------------- */
/**
 *   Calculate a d-spacing based on the Q "value"
 *
 *   @param Q               The Q "value" of the neutron or x-ray (doesn't
 *                          matter which).
 *   
 *   @return The magnitude of d in Angstroms.
 *
 */
public static float DSpacingofDiffractometerQ( float Q ){
   return (float)( 2.0 * Math.PI / Q );
}

/* ------------------------ DSpacingofWavelength ------------------------- */
/**
 *   Calculate a d-spacing based on the scattering angle and wavelength.
 *
 *   @param angle_radians   The angle between the neutron beam and the line
 *                          from the sample to the detector, in radians. The
 *                          angle is commonly refered to as the Bragg angle
 *                          2_theta.
 *   @param wavelength      The wavelength of the neutron or x-ray (doesn't
 *                          matter which).
 *   
 *   @return The magnitude of d-spacing in Angstroms.
 *
 */
public static float DSpacingofWavelength(float angle_radians,float wavelength){
    float  theta_radians = Math.abs( angle_radians / 2.0f );
    return (float)( wavelength/(2.0 * Math.sin( theta_radians )) );
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

  return (float)( wavelength * path_len_m / ANGST_PER_US_PER_M );
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

  wavelength    = (float)(ANGST_PER_US_PER_M * time_us / path_len_m);
  theta_radians = Math.abs( angle_radians / 2.0f );

  return (float)( 4.0 * Math.PI * Math.sin( theta_radians ) / wavelength );
}


/* ------------------------- DiffractometerVecQ ------------------------- */
/**
 *   Calculate a "Q" vector based on the detector position, total flight path
 *   length and time of flight for a neutron that was scattered by a sample.
 *
 *   @param det_pos         The position of the detector, relative to the
 *                          sample.
 *   @param initial_path_m  The distance from the moderator to the sample 
 *                          in meters.
 *   @param time_us         The time in microseconds for the neutron to travel
 *                          the distance from the moderator to the detector.
 *
 *   @return A vector position, "Q" with components in inverse Angstroms.
 *
 */

public static Position3D DiffractometerVecQ( DetectorPosition  det_pos, 
                                             float             initial_path_m,
                                             float             time_us    )
{
  float       distance;
  float       path_len_m;
  float       angle_radians;
  float       magnitude_Q;
  Position3D  vector_Q = new Position3D();
  float       xyz[];
  float       magnitude_xyz;
  float       scale;

  distance      = det_pos.getDistance();
  path_len_m    = distance + initial_path_m;
  angle_radians = det_pos.getScatteringAngle();
  magnitude_Q   = DiffractometerQ( angle_radians, path_len_m, time_us ); 

  xyz = det_pos.getCartesianCoords();        // this is vector K'
  xyz[0] -= distance;                        // now it's in the direction of
                                             // vector Q = K' - K
  magnitude_xyz = (float)Math.sqrt(xyz[0]*xyz[0]+xyz[1]*xyz[1]+xyz[2]*xyz[2]);
  scale = magnitude_Q/magnitude_xyz;
  xyz[0] *= scale;
  xyz[1] *= scale;
  xyz[2] *= scale;                           // now it's really Q

  vector_Q.setCartesianCoords( xyz[0], xyz[1], xyz[2] );
  return vector_Q;
}



/* ------------------------- DiffractometerQ --------------------------- */
/**
 * Calculate a "Q" value based on the empirical relation used by gsas.
 *
 * @param  dif_c  The geometrical portion of the conversion.
 * @param  dif_a  The quadratic portion of the conversion.
 * @param  t_zero The constant offset.
 *
 * @return The magnitude of "Q" in inverse Angstroms.
 */
public static float DiffractometerQ( float dif_c, float dif_a,
                                     float t_zero, float time_us){
    // shouldn't solve quadratic if unnecessary
    if(dif_a==0) return 2f*(float)Math.PI*dif_c/(time_us-t_zero);

    // imaginary numbers if t_zero>time_us
    if(t_zero>time_us)return Float.NaN;

    // otherwise do the calculation (only positive root has meaning)
    double num=Math.PI*4.*(double)dif_a;
    double den=-1.*dif_c+Math.sqrt(dif_c*dif_c+4.*dif_a*(time_us-t_zero));
    return (float)(num/den);
}

/* -------------------- DiffractometerQofWavelength ---------------------- */
/**
 *   Calculate a "Q" value based on the scattering angle and wavelength.
 *
 *   @param angle_radians   The angle between the neutron beam and the line
 *                          from the sample to the detector, in radians. The
 *                          angle is commonly refered to as the Bragg angle
 *                          2_theta.
 *   @param wavelength      The wavelength of the neutron or x-ray (doesn't
 *                          matter which).
 *   
 *   @return The magnitude of "Q" in inverse Angstroms.
 *
 */
public static float DiffractometerQofWavelength(float angle_radians, 
                                                float wavelength ){
   float  theta_radians = Math.abs( angle_radians / 2.0f );
   return (float)( 4.0 * Math.PI * Math.sin( theta_radians ) / wavelength );
}

/* --------------------- DiffractometerQofDSpacing ----------------------- */
/**
 *   Calculate a "Q" value based on the d-spacing
 *
 *   @param d_space         The d-spacing of the neutron or x-ray (doesn't
 *                          matter which).
 *   
 *   @return The magnitude of "Q" in inverse Angstroms.
 *
 */
public static float DiffractometerQofDSpacing( float d_space ){
   return (float)( 2.0 * Math.PI / d_space );
}

/* ------------------------ TOFofDiffractometerQ -------------------------- */
/**
 *   Calculate the time of flight for a neutron based on the
 *   scattering angle, i total flight path length and a "Q" value for
 *   a sample that scattered the neutron beam.
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

public static float  TOFofDiffractometerQ( float angle_radians, 
                                           float path_len_m, 
                                           float Q_invA     )
{
  float  wavelength;
  float  theta_radians;

  theta_radians = Math.abs( angle_radians / 2.0f );
  wavelength    = (float)( 4.0 * Math.PI * Math.sin( theta_radians ) / Q_invA );

  return  (float)( wavelength * path_len_m / ANGST_PER_US_PER_M );
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
      System.out.println("ERROR in SpectrometerQ ... sqrt of negative number");
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


/* ------------------------ makeEulerRotation ------------------------ */
/**
 *  Make the cumulative rotation matrix representing rotation by Euler angles
 *  chi, phi and omega.  This produces a matrix representing the following 
 *  sequence of rotations, applied in the order listed:
 *
 *  1. Rotate about the positive z-axis by phi 
 *  2. Rotate about the positive x-axis by chi 
 *  3. Rotate about the positive z-axis by omega
 *
 *  NOTE: All rotations follow a right hand rule and the coordinate system is
 *  assumed to be right handed.  Rotation angles are specified in degrees.
 *
 *  @param phi    Angle to rotate about the +z-axis
 *  @param chi    Angle to rotate (the rotated system) about the +x-axis
 *  @param omega  Angle to rotate (the rotated system) about the +z-axis
 *  
 *  @return The cumulative rotation matrix omegaRz * chiRx * phiRz, 
 *          which carries out these rotations, starting with rotation by
 *          phi about the z-axis.
 */
public static Tran3D makeEulerRotation( float phi, float chi, float omega )
{
   Tran3D omegaR = new Tran3D();
   Tran3D phiR   = new Tran3D();
   Tran3D chiR   = new Tran3D();

   Vector3D i_vec = new Vector3D( 1, 0, 0 );
   Vector3D k_vec = new Vector3D( 0, 0, 1 );

   phiR.setRotation( phi, k_vec );
   chiR.setRotation( chi, i_vec );
   omegaR.setRotation( omega, k_vec );  

   // build the matrix product (omegaR * chiR * phiR).  When applied to a
   // vector v as in  (omegaR * chiR * phiR)v, this has the effect of doing
   // the rotation phiR first!.

   omegaR.multiply_by( chiR );
   omegaR.multiply_by( phiR );
   return omegaR;
}

/* ------------------------ makeEulerRotation ------------------------ */
/**
 *  Make the double precision rotation matrix representing rotation by Euler
 *  angles chi, phi and omega.  This produces a matrix representing the
 *  following sequence of rotations, applied in the order listed:
 *
 *  1. Rotate about the positive z-axis by phi
 *  2. Rotate about the positive x-axis by chi
 *  3. Rotate about the positive z-axis by omega
 *
 *  NOTE: All rotations follow a right hand rule and the coordinate system is
 *  assumed to be right handed.  Rotation angles are specified in degrees.
 *
 *  @param phi    Angle to rotate about the +z-axis
 *  @param chi    Angle to rotate (the rotated system) about the +x-axis
 *  @param omega  Angle to rotate (the rotated system) about the +z-axis
 *
 *  @return The cumulative rotation matrix omegaRz * chiRx * phiRz,
 *          which carries out these rotations, starting with rotation by
 *          phi about the z-axis.
 */
public static double[][] makeEulerRotation( double phi, 
                                            double chi, 
                                            double omega )
{
   phi   *= Math.PI/180;
   chi   *= Math.PI/180;
   omega *= Math.PI/180;

   double sin_phi = Math.sin( phi );
   double cos_phi = Math.cos( phi );

   double sin_chi = Math.sin( chi );
   double cos_chi = Math.cos( chi );

   double sin_omega = Math.sin( omega );
   double cos_omega = Math.cos( omega );

   double phiR[][] = {  { cos_phi, -sin_phi, 0 },
                        { sin_phi,  cos_phi, 0 },
                        {    0,        0,    1 } };

   double chiR[][] = {  { 1,    0,        0    },
                        { 0, cos_chi, -sin_chi },
                        { 0, sin_chi,  cos_chi } };

   double omegaR[][] = {  { cos_omega, -sin_omega, 0 },
                          { sin_omega,  cos_omega, 0 },
                          {    0,          0,      1 } };

   // build the matrix product (omegaR * chiR * phiR).  When applied to a
   // vector v as in  (omegaR * chiR * phiR)v, this has the effect of doing
   // the rotation phiR first!.

   double result[][] = LinearAlgebra.mult( omegaR, chiR );
   result = LinearAlgebra.mult( result, phiR );
   return result;
}


/* ---------------------- makeEulerRotationInverse -------------------- */
/*
 *  Make the cumulative rotation matrix that reverses rotation by Euler angles
 *  chi, phi and omega.  This produces a matrix representing the following   
 *  sequence of rotations, applied in the order listed:
 *
 *  1. Rotate about the positive z-axis by minus omega
 *  2. Rotate about the positive x-axis by minus chi
 *  3. Rotate about the positive z-axis by minus phi
 *
 *  NOTE: All rotations follow a right hand rule and the coordinate system is
 *  assumed to be right handed.  Rotation angles are specified in degrees.
 *  To use this to "unwind" the goniometer rotations on the SCD at IPNS with 
 *  the values of phi, chi and omega stored in the IPNS runfiles, use:
 *
 *     makeEulerRotationInverse(phi, chi, -omega)
 *
 *  @param phi    phi angle that the goniometer was rotated by about +z axis
 *  @param chi    chi angle that the goniometer was rotated by about +x axis
 *  @param omega  omega angle that the goniometer was rotated by about +z axis
 *  
 *  @return The cumulative rotation matrix that reversed the rotations by
 *          phi, chi and omega.  This returns the matrix product:
 *
 *          phiRz_inverse * chiRx_inverse * omegaRz_inverse
 *    
 *          which which carries out these rotations, starting with rotation by
 *          minus omega about the z-axis.
 */
public static Tran3D makeEulerRotationInverse(float phi, float chi, float omega)
{
  
  Tran3D inverse = makeEulerRotation( phi, chi, omega );

  inverse.transpose();  // NOTE: A Rotation matrix is an orthogonal
                        //       transformation and so it's transpose is
                        //       it's inverse.
  return inverse;
}

/* ---------------------- makeEulerRotationInverse -------------------- */
/*
 *  Make the double precision rotation matrix that reverses rotation by Euler 
 *  angles chi, phi and omega.  This produces a matrix representing the
 *  following sequence of rotations, applied in the order listed:
 *
 *  1. Rotate about the positive z-axis by minus omega
 *  2. Rotate about the positive x-axis by minus chi
 *  3. Rotate about the positive z-axis by minus phi
 *
 *  NOTE: All rotations follow a right hand rule and the coordinate system is
 *  assumed to be right handed.  Rotation angles are specified in degrees.
 *  To use this to "unwind" the goniometer rotations on the SCD at IPNS with
 *  the values of phi, chi and omega stored in the IPNS runfiles, use:
 *
 *     makeEulerRotationInverse(phi, chi, -omega)
 *
 *  @param phi    phi angle that the goniometer was rotated by about +z axis
 *  @param chi    chi angle that the goniometer was rotated by about +x axis
 *  @param omega  omega angle that the goniometer was rotated by about +z axis
 *
 *  @return The cumulative rotation matrix that reversed the rotations by
 *          phi, chi and omega.  This returns the matrix product:
 *
 *          phiRz_inverse * chiRx_inverse * omegaRz_inverse
 *
 *          which which carries out these rotations, starting with rotation by
 *          minus omega about the z-axis.
 */
public static double[][] makeEulerRotationInverse( double phi, 
                                                   double chi, 
                                                   double omega )
{
  double inverse[][] = makeEulerRotation( phi, chi, omega );

                           // NOTE: A Rotation matrix is an orthogonal
                           //       transformation and so it's transpose is
                           //       it's inverse.
  return LinearAlgebra.getTranspose( inverse );
}


/* --------------------------------- main -------------------------------- */
/**
 *  main program for test purposes only
 */

public static void main( String args[] )
{
  int n_times = 100000;
  ElapsedTime timer = new ElapsedTime();

  timer.reset();
  double d_rot[][] = null;
  for ( int i = 0; i < n_times; i++ )
    d_rot = makeEulerRotation( 25.0, 35.0, 45.0 );
  System.out.println("Time to make double precision form: " + 
                      timer.elapsed() / n_times ); 
  System.out.println("Double precision Euler rotation = " );
  LinearAlgebra.print( d_rot );

  Tran3D s_rot = null;
  timer.reset();
  for ( int i = 0; i < n_times; i++ )
    s_rot = makeEulerRotation( 25.0f, 35.0f, 45.0f );
  System.out.println("Time to make single precision form: " + 
                      timer.elapsed() / n_times ); 
  System.out.println("Single precision Euler rotation (Tran3d) = ");
  System.out.println("" + s_rot);

  System.out.println("Double precision inverse Euler rotation = " );
  d_rot = makeEulerRotationInverse( 25.0, 35.0, 45.0 );
  LinearAlgebra.print( d_rot );

  s_rot = makeEulerRotationInverse( 25.0f, 35.0f, 45.0f );
  System.out.println("Tran3D = ");
  System.out.println("" + s_rot);


  DetectorPosition det_pos = new DetectorPosition();
  det_pos.setCartesianCoords( 0, 0.32f, 0 ); 

  Position3D temp_pos = DiffractometerVecQ( det_pos, 9.459f, 3000 );
  DetectorPosition pos = new DetectorPosition( temp_pos );
  System.out.println( "Detector at = " + det_pos );
  System.out.println( "Scatt. ang. = " + det_pos.getScatteringAngle() );
  System.out.println( "Q at        = " + pos );
  System.out.println( "magnitude_Q = " + pos.getDistance() );
  System.out.println( "QScatt. ang.= " + pos.getScatteringAngle() );

  det_pos.setCylindricalCoords( 0.349f, (float)(113.45*Math.PI/180), .121f );
  temp_pos = DiffractometerVecQ( det_pos, 9.459f, 6000 );
  pos = new DetectorPosition( temp_pos );
  System.out.println( "Detector at = " + det_pos );
  System.out.println( "Scatt. ang. = " + det_pos.getScatteringAngle() );
  System.out.println( "Q at        = " + pos );
  System.out.println( "magnitude_Q = " + pos.getDistance() );
  System.out.println( "QScatt. ang.= " + pos.getScatteringAngle() );

  System.out.println( "MN_KG               " + MN_KG );
  System.out.println( "JOULES_PER_meV      " + JOULES_PER_meV   );
  System.out.println( "H_JS                " + H_JS  );
  System.out.println( "H_ES                " + H_ES  );
  System.out.println( "H_BAR_JS            " + H_BAR_JS  );
  System.out.println( "H_BAR_ES            " + H_BAR_ES  );
  System.out.println( "meV_PER_MM_PER_US_2 " + meV_PER_MM_PER_US_2   );
  System.out.println( "ANGST_PER_US_PER_MM " + ANGST_PER_US_PER_MM  );
  System.out.println( "ANGST_PER_US_PER_M  " + ANGST_PER_US_PER_M  );
}

}

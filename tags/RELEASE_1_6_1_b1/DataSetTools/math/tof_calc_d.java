/*
 * File:  tof_calc_d.java 
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * 
 *  $Log$
 *  Revision 1.5  2003/12/06 21:39:48  dennis
 *  Added some additional tests for Euler angles.
 *  Changed threshold for special cases in Euler
 *  calculation to 1E-15 from 1E-10 based on tests
 *  with randomly generated angles.
 *
 *  Revision 1.4  2003/12/01 17:12:16  dennis
 *  Added method getEulerAngles(u,v) to calculate Euler angles phi, chi, omega
 *  for rotations about the z axis, x axis and z axis again, given the ortho-
 *  normal vectors u,v that the x axis and y axis map to.  The third ortho-
 *  normal vector, n, is generated as u X v, by this routine.
 *
 *  Revision 1.3  2003/10/15 23:59:02  dennis
 *  Fixed javadocs to build cleanly with jdk 1.4.2
 *
 *  Revision 1.2  2003/07/22 15:10:59  dennis
 *  Now refers to double precision physical constants defined in
 *  tof_calc, rather than re-defining them.
 *
 *  Revision 1.1  2003/07/14 22:23:01  dennis
 *  Double precision version, ported from original
 *  single precision version.
 */

package DataSetTools.math;

import DataSetTools.util.*;

/**
 *  This contains basic conversions for time-of-flight neutron scattering
 *  experiments. ( Ported to Java from tof_vis_calc.c )
 */
public final class tof_calc_d
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
public static final double  MN_KG          = tof_calc.MN_KG; 

public static final double  JOULES_PER_meV = tof_calc.JOULES_PER_meV;

                                                          //h in Joule seconds
public static final double  H_JS          =  tof_calc.H_JS;

                                                           // h in erg seconds
public static final double  H_ES          =  tof_calc.H_ES;

                                                      // h_bar in Joule seconds
public static final double  H_BAR_JS      =  tof_calc.H_BAR_JS;

                                                      // h_bar in erg seconds
public static final double  H_BAR_ES      =  tof_calc.H_BAR_ES;

public static final double  meV_PER_MM_PER_US_2 = tof_calc.meV_PER_MM_PER_US_2;

public static final double  ANGST_PER_US_PER_MM = tof_calc.ANGST_PER_US_PER_MM;

public static final double  ANGST_PER_US_PER_M  = tof_calc.ANGST_PER_US_PER_M;

  /**
   * Don't let anyone instantiate this class.
   */
  private tof_calc_d() {}


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
public static double Energy( double path_len_m, double time_us )
{
  double   v;
  double   energy;

  if ( time_us <= 0.0 )                     /* NOT MEANINGFUL */
    return( Double.NaN );

  v = (path_len_m * 1000.0f)/ time_us;        /*   velocity in mm/us    */
  energy = meV_PER_MM_PER_US_2 * v * v; 
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


public static double  TOFofEnergy( double path_len_m, double e_meV )
{
  return  path_len_m * 1000.0/Math.sqrt( e_meV/meV_PER_MM_PER_US_2 );
}


/* ------------------------- VelocityFromEnergy ---------------------------- */
/**
 *   Calculate the velocity of a neutron based on it's energy.
 *
 *   @param e_meV       The energy of the neutron in meV.
 *
 *   @return The velocity of a neutron in meters per microsecond. 
 */

public static double VelocityFromEnergy( double e_meV )
{

  if ( e_meV < 0.0f )                        /* NOT MEANINGFUL */
    return( Double.NaN );

  double   v_m_per_us = Math.sqrt( e_meV / meV_PER_MM_PER_US_2 )/1000;

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
public static double EnergyFromVelocity( double v_m_per_us )

{
  if ( v_m_per_us < 0.0f )                     /* NOT MEANINGFUL */
    return( Double.NaN );

  double   e_meV = v_m_per_us * v_m_per_us * 1000000 * meV_PER_MM_PER_US_2;

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
public static double EnergyFromWavelength( double wavelength_A )
{

  if ( wavelength_A <= 0.0f )                     /* NOT MEANINGFUL */
    return( Double.NaN );

  double  v_m_per_us = ANGST_PER_US_PER_M / wavelength_A;
  double  e_meV      = v_m_per_us * v_m_per_us * 1000000 * meV_PER_MM_PER_US_2;

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
public static double WavelengthFromEnergy( double e_meV )
{

  if ( e_meV <= 0.0f )                     /* NOT MEANINGFUL */
    return( Double.NaN );

  double   v_m_per_us = Math.sqrt( e_meV / meV_PER_MM_PER_US_2 )/1000;
  double   wavelength_A = ANGST_PER_US_PER_M / v_m_per_us;

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
public static double WavelengthFromVelocity( double v_m_per_us )

{
  if ( v_m_per_us <= 0.0f )                     /* NOT MEANINGFUL */
    return( Double.NaN );

  double  wavelength_A = ANGST_PER_US_PER_M / v_m_per_us;

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

public static double VelocityFromWavelength( double wavelength_A )
{
  if ( wavelength_A <= 0.0f )                     /* NOT MEANINGFUL */
    return( Double.NaN );

  double  v_m_per_us = ANGST_PER_US_PER_M / wavelength_A;

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

public static double Wavelength( double path_len_m, double time_us )
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
public static double TOFofWavelength( double path_len_m, double wavelength_A )
{
  return( wavelength_A * path_len_m / ANGST_PER_US_PER_M );
}


/* -------------------- WavelengthofDiffractometerQ ------------------- */
/**
 * Calculate the wavelength of a given Q.
 *
 * @param angle_radians The scattering angle.
 *
 * @param Q             The Q value to be converted to wavelength
 *
 * @return The wavelength related to the Q given.
 */
public static double WavelengthofDiffractometerQ(double angle_radians, double Q)
{
    double theta_radians = Math.abs(angle_radians/2.0f);
    return  4.0 * Math.PI * Math.sin(theta_radians) / Q; 
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
public static double  DSpacing( double angle_radians, 
                               double path_len_m, 
                               double time_us )
{
  double wavelength;
  double theta_radians;

  wavelength    = ANGST_PER_US_PER_M * time_us / path_len_m;
  theta_radians = Math.abs( angle_radians / 2.0f );

  return  wavelength / (2.0 * Math.sin( theta_radians ) ); 
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
public static double DSpacing( double dif_c, double dif_a,
                              double t_zero, double time_us ){
    // shouldn't solve quadratic if unnecessary
    if(dif_a==0) return (time_us-t_zero)/dif_c;

    // imaginary numbers if t_zero>time_us
    if(t_zero>time_us)return Double.NaN;

    // otherwise do the calculation (only positive root has meaning)
    double num=-1.*dif_c+Math.sqrt(dif_c*dif_c+4.*dif_a*(time_us-t_zero));
    double den=2.*dif_a;
    return num/den;
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
public static double DSpacingofDiffractometerQ( double Q ){
   return 2.0 * Math.PI / Q;
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
public static double DSpacingofWavelength(double angle_radians,double wavelength){
    double  theta_radians = Math.abs( angle_radians / 2.0f );
    return wavelength/(2.0 * Math.sin( theta_radians ));
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
public static double  TOFofDSpacing( double angle_radians, 
                                    double path_len_m, 
                                    double d_A          )
{
  double  wavelength;
  double  theta_radians;

  theta_radians = Math.abs( angle_radians / 2.0f );
  wavelength    = 2.0 * Math.sin( theta_radians ) * d_A;

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

public static double  DiffractometerQ( double angle_radians, 
                                      double path_len_m, 
                                      double time_us    )
{
  double  wavelength;
  double  theta_radians;

  wavelength    = ANGST_PER_US_PER_M * time_us / path_len_m;
  theta_radians = Math.abs( angle_radians / 2.0f );

  return 4.0 * Math.PI * Math.sin( theta_radians ) / wavelength;
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

public static Position3D_d DiffractometerVecQ( 
                                             DetectorPosition_d  det_pos, 
                                             double             initial_path_m,
                                             double             time_us    )
{
  double        distance;
  double        path_len_m;
  double        angle_radians;
  double        magnitude_Q;
  Position3D_d  vector_Q = new Position3D_d();
  double        xyz[];
  double        magnitude_xyz;
  double        scale;

  distance      = det_pos.getDistance();
  path_len_m    = distance + initial_path_m;
  angle_radians = det_pos.getScatteringAngle();
  magnitude_Q   = DiffractometerQ( angle_radians, path_len_m, time_us ); 

  xyz = det_pos.getCartesianCoords();        // this is vector K'
  xyz[0] -= distance;                        // now it's in the direction of
                                             // vector Q = K' - K
  magnitude_xyz = Math.sqrt(xyz[0]*xyz[0]+xyz[1]*xyz[1]+xyz[2]*xyz[2]);
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
public static double DiffractometerQ( double dif_c, double dif_a,
                                     double t_zero, double time_us){
    // shouldn't solve quadratic if unnecessary
    if(dif_a==0) return 2*Math.PI*dif_c/(time_us-t_zero);

    // imaginary numbers if t_zero>time_us
    if(t_zero>time_us)return Double.NaN;

    // otherwise do the calculation (only positive root has meaning)
    double num=Math.PI*4.*dif_a;
    double den=-1.*dif_c+Math.sqrt(dif_c*dif_c+4.*dif_a*(time_us-t_zero));
    return num/den;
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
public static double DiffractometerQofWavelength(double angle_radians, 
                                                double wavelength ){
   double  theta_radians = Math.abs( angle_radians / 2.0f );
   return  4.0 * Math.PI * Math.sin( theta_radians ) / wavelength;
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
public static double DiffractometerQofDSpacing( double d_space ){
   return 2.0 * Math.PI / d_space;
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

public static double  TOFofDiffractometerQ( double angle_radians, 
                                           double path_len_m, 
                                           double Q_invA     )
{
  double  wavelength;
  double  theta_radians;

  theta_radians = Math.abs( angle_radians / 2.0f );
  wavelength    = 4.0 * Math.PI * Math.sin( theta_radians ) / Q_invA;

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

public static double SpectrometerQ( double e_in_meV, 
                                   double e_out_meV, 
                                   double angle_radians )
{
  double  temp;
  double  two_theta_radians;

  two_theta_radians = Math.abs( angle_radians );
  temp = e_in_meV + e_out_meV
     - 2.0 * Math.sqrt( e_in_meV * e_out_meV ) * Math.cos( two_theta_radians );

  if ( temp < 0.0f )
    {
      System.out.println("ERROR in SpectrometerQ ... sqrt of negative number");
      return( -1.0f );
    }
  return Math.sqrt( temp / 2.0721 );
}


/* ------------------------------- Omega -------------------------------- */
/**
 *  Calculates "Omega"
 */
public static double Omega( double two_theta )
{
  double alpha = (two_theta/2 + 135);
  
  if ( alpha < 135 )
    return alpha;
  else
    return (alpha - 180);
}


/* ------------------------ getEulerAngles --------------------------- */
/**
 *  Return an array containing a set of "Euler angles" representing a
 *  rotation that takes the orthonormal basis vectors i,j,k into another 
 *  set of orthonormal basis vectors u,v,n.   Only the basis vectors u,v 
 *  are taken as arguments since the "n" vector is calculated internally 
 *  as u X v.  "v" is then recalculated as n X u, in case v was not originally
 *  perpendicular to "u".  All vectors will be normalized internally.
 *  The angles of rotation are to be applied as follows:
 *
 *  1. Rotate about the positive z-axis by phi
 *  2. Rotate about the positive x-axis by chi
 *  3. Rotate about the positive z-axis by omega
 *
 *  NOTE: All rotations follow a right hand rule and the coordinate system is
 *  assumed to be right handed.  Rotation angles are returned in degrees.
 *
 *  @param u      Vector giving the direction that the basis vector "i" 
 *                maps to.
 *  @param v      Vector giving the direction that the basis  vector "j"
 *                maps to.  In principle, this should be perpendicular to u, 
 *                it at least must not be collinear with u.
 *
 *  @return The Euler angles are returned in an array euler[], with
 *          euler[0]=phi, euler[1]=chi, euler[2]=omega.  If u==0, v==0 or
 *          u and v are collinear, this just returns an array of zeros.
 */

public static double[] getEulerAngles( Vector3D_d u, Vector3D_d v )
{
  double euler[] = {0,0,0};
                                        // first make sure we have valid u,v,n
  if ( u.length() == 0 || v.length() == 0 )
  {
    System.out.println("Error: zero length u or v in getEulerAngles()" );
    System.out.println("u = " + u );
    System.out.println("v = " + v );
    return euler;
  }

  u.normalize();
  v.normalize();

  Vector3D_d n = new Vector3D_d();
  n.cross( u, v );
  if ( n.length() == 0 )
  {
    System.out.println("Error: u and v are collinear in getEulerAngles()" );
    System.out.println("u = " + u );
    System.out.println("v = " + v );
    return euler;
  }

  n.normalize();
  v.cross( n, u );
                          // Now that there are valid u,v,n find the Euler 
                          // angles.  A special case  occurs if the "n" vector 
                          // is collinear with "k". 

  double one = 1 - 1.0E-15;    // We'll consider anything within 1E-15 of 1 to
                               // be 1, to deal with special cases chi == 0
                               // and chi == 180 degrees.

  if ( n.get()[2] >= one )     // chi rotation is 0, just rotate about z-axis  
  {
    euler[0] = Math.atan2( u.get()[1], u.get()[0] );        // phi
    euler[1] = 0;                                           // chi
    euler[2] = 0;                                           // omega 
  }
  else if ( n.get()[2] <= -one ) // chi rotation is 180 degrees 
  {
    euler[0] = -Math.atan2( u.get()[1], u.get()[0] );       // phi
    euler[1] = Math.PI;                                     // chi
    euler[2] = 0;                                           // omega 
  }
  else
  {
    euler[0] = Math.atan2( u.get()[2], v.get()[2] );        // phi
    euler[1] = Math.acos( n.get()[2] );                     // chi
    euler[2] = Math.atan2( n.get()[0], -n.get()[1] );       // omega 
  }

  for ( int i = 0; i < euler.length; i++ )              // convert to degrees
    euler[i] *= 180.0 / Math.PI;

  return euler;
}


/* ------------------------ makeEulerRotation ------------------------ */
/**
 *  Make the cumulative rotation matrix representing rotation by Euler angles
 *  phi, chi and omega.  This produces a matrix representing the following 
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
public static Tran3D_d makeEulerRotation( double phi, double chi, double omega )
{
   Tran3D_d omegaR = new Tran3D_d();
   Tran3D_d phiR   = new Tran3D_d();
   Tran3D_d chiR   = new Tran3D_d();

   Vector3D_d i_vec = new Vector3D_d( 1, 0, 0 );
   Vector3D_d k_vec = new Vector3D_d( 0, 0, 1 );

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


/* ---------------------- makeEulerRotationInverse -------------------- */
/*
 *  Make the cumulative rotation matrix that reverses rotation by Euler angles
 *  phi, chi and omega.  This produces a matrix representing the following   
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
public static Tran3D_d makeEulerRotationInverse(double phi, 
                                                double chi, 
                                                double omega)
{
  
  Tran3D_d inverse = makeEulerRotation( phi, chi, omega );

  inverse.transpose();  // NOTE: A Rotation matrix is an orthogonal
                        //       transformation and so it's transpose is
                        //       it's inverse.
  return inverse;
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
    d_rot = tof_calc.makeEulerRotation( 25.0, 35.0, 45.0 );
  System.out.println("Time to make double precision form: " + 
                      timer.elapsed() / n_times ); 
  System.out.println("Double precision Euler rotation = " );
  LinearAlgebra.print( d_rot );

  Tran3D_d s_rot = null;
  timer.reset();
  for ( int i = 0; i < n_times; i++ )
    s_rot = makeEulerRotation( 25.0, 35.0, 45.0 );
  System.out.println("Time to make single precision form: " + 
                      timer.elapsed() / n_times ); 
  System.out.println("Single precision Euler rotation (Tran3d) = ");
  System.out.println("" + s_rot);

  System.out.println("Double precision inverse Euler rotation = " );
  d_rot = tof_calc.makeEulerRotationInverse( 25.0, 35.0, 45.0 );
  LinearAlgebra.print( d_rot );

  s_rot = makeEulerRotationInverse( 25.0, 35.0, 45.0 );
  System.out.println("Tran3D = ");
  System.out.println("" + s_rot);

  Tran3D_d test_rot  = makeEulerRotation(  25.0,  35.0,  45.0 );
  Tran3D_d test_rot2 = makeEulerRotation( -25.0, -35.0, -45.0 );
  System.out.println( "-----------Rotation with + = \n" + test_rot );
  System.out.println( "-----------Rotation with - = \n" + test_rot2 );

  // Check Euler angle calculation for special cases with n along z-axis
  Vector3D_d u = new Vector3D_d(  Math.sqrt(2)/2, Math.sqrt(2)/2, 0 );
  Vector3D_d v = new Vector3D_d( -Math.sqrt(2)/2, Math.sqrt(2)/2, 0 );
  double euler[] = tof_calc_d.getEulerAngles( u, v );
  s_rot = tof_calc_d.makeEulerRotation( euler[0], euler[1], euler[2] );
  System.out.println("Rotation to +45 degrees and +135 degrees ");
  System.out.println("" + s_rot );

  u = new Vector3D_d( -Math.sqrt(2)/2, Math.sqrt(2)/2, 0 );
  v = new Vector3D_d(  Math.sqrt(2)/2, Math.sqrt(2)/2, 0 );
  euler = tof_calc_d.getEulerAngles( u, v );
  s_rot = tof_calc_d.makeEulerRotation( euler[0], euler[1], euler[2] );
  System.out.println("Rotation to +45 degrees and +135 degrees and -z axis");
  System.out.println("" + s_rot );


  // Check Euler angle calculation for randomly distributed rotations
  double error = 0;
  double max_error = 0;
  int N_TRIES = 10000;
  for ( int i = 0; i < N_TRIES; i++ )
  {
    double phi   = 360 * Math.random();

    double chi   = .001 * Math.random() - 180.0005;// try angles near 180 
//    double chi   = .001 * Math.random() - .0005;   // try angles near zero
//    double chi   = 360 * Math.random();

    double omega = 360 * Math.random();

    d_rot = tof_calc.makeEulerRotation( phi, chi, omega );
    u = new Vector3D_d( d_rot[0][0], d_rot[1][0], d_rot[2][0] );
    v = new Vector3D_d( d_rot[0][1], d_rot[1][1], d_rot[2][1] );
    euler = getEulerAngles( u, v );
/*
    System.out.println("Euler Angles = " + euler[0] + 
                                    ", " + euler[1] + 
                                    ", " + euler[2] );
*/
    double current_error = 0;
    double d_rot2[][] = tof_calc.makeEulerRotation(euler[0],euler[1],euler[2]);
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
        current_error += Math.abs( d_rot[row][col] - d_rot2[row][col]);

    if ( current_error > 5.0E-10 )
      System.out.println( " i = "   + i + 
                          " ang = " + euler[0] +
                               ", " + euler[1] +
                               ", " + euler[2] + 
                           " ==== " + current_error );
    error += current_error;
    if ( current_error > max_error )
      max_error = current_error;
  }
  System.out.println( "Total Error = " + error + " in " + N_TRIES );
  System.out.println( "Max   Error = " + max_error + " in " + N_TRIES );

  DetectorPosition_d det_pos = new DetectorPosition_d();
  det_pos.setCartesianCoords( 0, 0.32, 0 ); 

  Position3D_d temp_pos = DiffractometerVecQ( det_pos, 9.459, 3000 );
  DetectorPosition_d pos = new DetectorPosition_d( temp_pos );
  System.out.println( "Detector at = " + det_pos );
  System.out.println( "Scatt. ang. = " + det_pos.getScatteringAngle() );
  System.out.println( "Q at        = " + pos );
  System.out.println( "magnitude_Q = " + pos.getDistance() );
  System.out.println( "QScatt. ang.= " + pos.getScatteringAngle() );

  det_pos.setCylindricalCoords( 0.349, 113.45*Math.PI/180, .121 );
  temp_pos = DiffractometerVecQ( det_pos, 9.459, 6000 );
  pos = new DetectorPosition_d( temp_pos );
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

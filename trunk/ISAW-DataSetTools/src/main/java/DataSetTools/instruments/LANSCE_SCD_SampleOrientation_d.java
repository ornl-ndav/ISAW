/*
 * File:  LANSCE_SCD_SampleOrientation.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 *  Revision 1.2  2006/02/13 00:09:06  dennis
 *  Reorganized to calculate and store the goniometer rotation
 *  matrix, and it's inverse when the object is constructed, to
 *  allow getting these rotations more efficiently.
 *  Removed methods to set chi, phi, omega individually.  If these
 *  are changed, a new SampleOrientation object should be constructed.
 *
 *  Revision 1.1  2006/01/16 02:03:14  dennis
 *  Initial commit of double precision version of the the LANSCE
 *  SCD sample orientation, needed for the calibration process.
 *
 */

package  DataSetTools.instruments;

import DataSetTools.math.*;

/**
 *  This class stores the phi, chi and omega values and computes the 
 *  corresponding rotation matrices using the conventions employed by the
 *  SCD instrument at LANSCE.  NOTE: The zero point of the Omega axis of 
 *  the LANSCE goniometer is offset by slightly more than 90 degrees from
 *  our "standard" convention in ISAW.  This class will accept the chi, phi
 *  and omega values as used on the LANSCE SCD goniometer, and will 
 *  provide the shift, internally, so that the methods to get the 
 *  goniometer transformations can be used polymorphically.  
 *
 *  A default offset of 91.5 degrees is provided.  There is also a 
 *  method to specify this offset, if the instrument is recalibrated.
 */  

public class LANSCE_SCD_SampleOrientation_d extends SampleOrientation_d
{
  private static    double omega_offset = 91.5;    // default offset


  /**
   *  Construct a SampleOrientation object with the values specified in
   *  degrees following the sign conventions and omega offset as needed
   *  for the SCD instrument at LANSCE.
   *
   *  @param  phi    This is the angle (in degrees) to rotate by first 
   *                 about the positive y-axis.  The positive y-axis points
   *                 up.
   *  @param  chi    This is the angle (in degrees) to rotate by second 
   *                 about the positive x-axis.  The positive x-axis points 
   *                 in the direction the beam of neutrons travel.
   *                 As the goniometer is currently set up at the LANSCE SCD,
   *                 chi is fixed at 120 degrees.
   *  @param  omega  This is the angle (in degrees) to rotate by third 
   *                 about the positive y-axis.  The values read from the
   *                 goniometer are offset by approximately 91.5 degrees
   *                 from the zero position for omega at IPNS.
   */
  public LANSCE_SCD_SampleOrientation_d( double phi, double chi, double omega  )
  {
    super( phi, chi, omega );
  }


  /** 
   *  Get the current value of the offset applied to the omega angle by ALL
   *  instances of this class.
   *
   *  @return the current value of the offset applied to omega.  
   */
  public double getOmegaOffset()
  {
    return omega_offset;
  } 


  /** 
   *  Set a new value for the offset that is applied to the omega angle by ALL
   *  instances of this class.
   *
   *  @param  offset  The new value for the offset to be applied to omega.  
   */
  public void setOmegaOffset( double offset )
  {
    omega_offset = offset;
    build_transforms();
  }


  /*
   *  Construct the transforms corresponding to the LANSCE SCD, by adding the
   *  omega offset of about 91.5 degrees to omega..
   */
  protected void build_transforms()
  {
    goniometer_rotation =
         tof_calc_d.makeEulerRotation( phi, chi, omega + omega_offset );
    goniometer_rotation_inverse =
         tof_calc_d.makeEulerRotationInverse( phi, chi, omega + omega_offset );
  }


  /* ------------------------------------------------------------------- */
  /**
   *  Main program for testing purposes.
   */
  public static void main( String args[] )
  {
    double phi   =   90;
    double chi   =  120;
    double omega =  290;

    LANSCE_SCD_SampleOrientation_d test = 
                         new LANSCE_SCD_SampleOrientation_d( phi, chi, omega );

    System.out.println( "phi = " + phi + 
                        ", chi = " + chi + 
                        ", omega = " + omega );
    System.out.println( "Goniometer rotation: .........................");    
    System.out.println( test.getGoniometerRotation().toString() );
    System.out.println( "Goniometer rotation inverse: .................");    
    System.out.println( test.getGoniometerRotationInverse().toString() );
  }

} 

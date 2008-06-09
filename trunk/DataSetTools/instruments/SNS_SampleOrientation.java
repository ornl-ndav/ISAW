/*
 * File:  SNS_SampleOrientation.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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
 * For further information, see <http://ftp.sns.gov/ISAW>
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package  DataSetTools.instruments;

import DataSetTools.math.tof_calc;

/**
 *  This class stores the phi, chi and omega values and computes the 
 *  corresponding rotation matrices using the conventions employed by the
 *  SCD instruments at the SNS.
 */  

      
public class SNS_SampleOrientation extends SampleOrientation
{
  /**
   *  Construct a SampleOrientation object using the phi, chi and omega
   *  (in degrees) following the sign conventions for the SCD instruments
   *  the SNS.  A sample orientation angle is specified as a signed angle
   *  of rotation about an axis, using the right hand rule.  The 
   *  rotations are used to specify the orientation of the crystal 
   *  relative to some fixed initial orientation.  Starting from that 
   *  initial reference orientation, the new orientation can be arrived 
   *  at by applying three rotations to the crystal, about fixed 
   *  coordinate axes at the sample center position, in order  The order
   *  in which the rotations are applied is important.  Specifically, the
   *  crystal is assumed to be rotated from its initial reference  
   *  orientation by first rotating about a vertical axis through angle 
   *  phi, then rotating about an axis in the beam direction by angle chi
   *  and finally rotating about the vertical axis again through angle 
   *  omega.  The angles are described in detail below:
   *
   *  @param  phi    This is the angle (in degrees) of the first rotation. 
   *                 The rotation is about about the vertical axis
   *                 (positive axis direction upward).  This is a right
   *                 hand rule rotation about the positive NeXus/SNS y-axis 
   *                 (the ISAW/IPNS z-axis).
   *  @param  chi    This is the angle (in degrees) of the second rotation.
   *                 The rotation is about an axis pointing along the
   *                 beam direction, with the positive direction being in
   *                 the direction of neutron travel along the beam.
   *                 This is a right hand rule rotation about the positive
   *                 NeXus/SNS z-axis (the ISAW/IPNS x axis).
   *  @param  omega  This is the angle (in degrees) of the third rotation.
   *                 The rotation is again about the vertical axis, following
   *                 the same conventions as for the phi angle.
   *              
   */
  public SNS_SampleOrientation( float phi, float chi, float omega  )
  {
    super( phi, chi, omega );
  }


  /*
   *  Construct the transforms corresponding to the SNS conventions.
   */
  protected void build_transforms()
  {
    goniometer_rotation = tof_calc.makeEulerRotation( phi, chi, omega );

    goniometer_rotation_inverse = 
                   tof_calc.makeEulerRotationInverse( phi, chi, omega );
  }


  /* ------------------------------------------------------------------- */
  /**
   *  Main program for testing purposes.
   */
  public static void main( String args[] )
  {
    float phi   = 0;
    float chi   = 0;
    float omega = 45;

    SNS_SampleOrientation test = 
                             new SNS_SampleOrientation( phi, chi, omega );

    System.out.println( "phi = " + phi + 
                        ", chi = " + chi + 
                        ", omega = " + omega );
    System.out.println( "Goniometer rotation: .........................");    
    System.out.println( test.getGoniometerRotation().toString() );
    System.out.println( "Goniometer rotation inverse: .................");    
    System.out.println( test.getGoniometerRotationInverse().toString() );
  }

} 

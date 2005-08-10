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
 *  Revision 1.1  2005/08/10 14:43:53  dennis
 *  Initial version of SampleOrientation class for the LANSCE SCD.
 *  This is intended to encapsulate the change of coordinates from
 *  LANSCE/NeXus to IPNS/ISAW coordinates, as well as any changes
 *  regarding direction of rotations.
 *
 */

package  DataSetTools.instruments;

import gov.anl.ipns.MathTools.Geometry.*;

import DataSetTools.math.*;

/**
 *  This class stores the phi, chi and omega values and computes the 
 *  corresponding rotation matrices using the conventions employed by the
 *  SCD instrument at LANSCE.  NOTE: The goniometer at the LANSCE SCD
 *  is defined in terms of coordinate axes similar to MCSTAS and NeXus,
 *  while internally, the ISAW coordinate system uses the conventions
 *  at IPNS.  Specifically, for the LANSCE SCD, the positive z-axis 
 *  points in the direction that the beam is traveling, the x-axis is
 *  horizontal and the y-axis points upward, in such a way as to form a 
 *  right handed coordinate system.  For the IPNS SCD, the positive x-axis
 *  is in the direction that the beam is traveling, the z-axis points upward
 *  and the y-axis is horizontal, in such a way as to form a right
 *  handed coordinate system.   
 *
 *  NOTE: Since this is intended to be used for the LANSCE SCD, the 
 *  documentation for the methods in this subclass is written in terms
 *  of the LANSCE SCD/NeXus coordinates. 
 */  

public class LANSCE_SCD_SampleOrientation extends SampleOrientation
{
  /**
   *  Construct a SampleOrientation object with the values specified in
   *  degrees following the sign conventions for the SCD instrument at LANSCE.
   *
   *  @param  phi    This is the angle (in degrees) to rotate by first 
   *                 about the positive y-axis, in NeXus coordinates. 
   *  @param  chi    This is the angle (in degrees) to rotate by second 
   *                 about the positive z-axis, in NeXus coordinates.
   *                 As the goniometer is currently set up at the LANSCE SCD,
   *                 chi is fixed at -135 degrees.
   *  @param  omega  This is the angle (in degrees) to rotate by third 
   *                 about the positive y-axis.
   *              
   */
  public LANSCE_SCD_SampleOrientation( float phi, float chi, float omega  )
  {
    super( phi, chi, omega );
  }

  /**
   *  Get the rotation matrix representing the rotation of the sample in
   *  the Goniometer by the current phi, chi and omega values.  The sign
   *  convention used is the convention used on the single crystal
   *  diffractometer at LANSCE. 
   *
   *  @return the rotation matrix for the angles phi, chi and omega.
   */
  public Tran3D getGoniometerRotation()
  {
    return tof_calc.makeEulerRotation( phi, chi, omega );
  }

  /**
   *  Get the inverse of the rotation matrix representing the rotation 
   *  of the sample in the Goniometer by the current phi, chi and omega 
   *  values.  The matrix returned is the matrix required to "unwind" the
   *  rotation and put measured "Q" values in the same coordinate system,
   *  relative to the crystal.  The sign convention used is the convention 
   *  used on the single crystal diffractometer at LANSCE.
   *
   *  @return the matrix that reverses the goniometer rotations to put
   *          measured "Q" values in a coordinate system relative to the
   *          crystal.
   */
  public Tran3D getGoniometerRotationInverse()
  {
    return tof_calc.makeEulerRotationInverse( phi, chi, omega );
  }


  /* ------------------------------------------------------------------- */
  /**
   *  Main program for testing purposes.
   */
  public static void main( String args[] )
  {
    float phi   =   90;
    float chi   = -135;
    float omega =  290;

    LANSCE_SCD_SampleOrientation test = 
                           new LANSCE_SCD_SampleOrientation( phi, chi, omega );

    System.out.println( "phi = " + phi + 
                        ", chi = " + chi + 
                        ", omega = " + omega );
    System.out.println( "Goniometer rotation: .........................");    
    System.out.println( test.getGoniometerRotation().toString() );
    System.out.println( "Goniometer rotation inverse: .................");    
    System.out.println( test.getGoniometerRotationInverse().toString() );
  }

} 

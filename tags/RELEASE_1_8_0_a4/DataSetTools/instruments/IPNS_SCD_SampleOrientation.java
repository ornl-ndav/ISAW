/*
 * File:  IPNS_SCD_SampleOrientation.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 *  Revision 1.4  2006/02/13 00:09:06  dennis
 *  Reorganized to calculate and store the goniometer rotation
 *  matrix, and it's inverse when the object is constructed, to
 *  allow getting these rotations more efficiently.
 *  Removed methods to set chi, phi, omega individually.  If these
 *  are changed, a new SampleOrientation object should be constructed.
 *
 *  Revision 1.3  2004/03/15 06:10:40  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.2  2004/03/15 03:28:15  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.1  2003/02/18 18:59:51  dennis
 *  Initial version.
 *
 */

package  DataSetTools.instruments;

import DataSetTools.math.*;

/**
 *  This class stores the phi, chi and omega values and computes the 
 *  corresponding rotation matrices using the conventions employed by the
 *  SCD instrument at IPNS, for which the direction of the omega angle
 *  is reversed.
 */  


public class IPNS_SCD_SampleOrientation extends SampleOrientation
{
  /**
   *  Construct a SampleOrientation object with the values specified in
   *  degrees following the sign conventions for the SCD instrument at IPNS.
   *
   *  @param  phi    This is the angle (in degrees) to rotate by first 
   *                 about the positive z-axis. 
   *  @param  chi    This is the angle (in degrees) to rotate by second 
   *                 about the positive x-axis.
   *  @param  omega  This is the angle (in degrees) to rotate by third 
   *                 about the NEGATIVE z-axis.
   *              
   */
  public IPNS_SCD_SampleOrientation( float phi, float chi, float omega  )
  {
    super( phi, chi, omega );
  }


  /*
   *  Construct the transforms corresponding to the IPNS SCD, where the
   *  direction of the omega angle is reversed relative to the ISAW
   *  conventions.
   */
  protected void build_transforms()
  {
    goniometer_rotation = tof_calc.makeEulerRotation( phi, chi, -omega );

    goniometer_rotation_inverse = 
                   tof_calc.makeEulerRotationInverse( phi, chi, -omega );
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

    IPNS_SCD_SampleOrientation test = 
                             new IPNS_SCD_SampleOrientation( phi, chi, omega );

    System.out.println( "phi = " + phi + 
                        ", chi = " + chi + 
                        ", omega = " + omega );
    System.out.println( "Goniometer rotation: .........................");    
    System.out.println( test.getGoniometerRotation().toString() );
    System.out.println( "Goniometer rotation inverse: .................");    
    System.out.println( test.getGoniometerRotationInverse().toString() );
  }

} 

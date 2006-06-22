/*
 * File:  SampleOrientation.java
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
 *  Revision 1.5  2006/02/13 00:09:06  dennis
 *  Reorganized to calculate and store the goniometer rotation
 *  matrix, and it's inverse when the object is constructed, to
 *  allow getting these rotations more efficiently.
 *  Removed methods to set chi, phi, omega individually.  If these
 *  are changed, a new SampleOrientation object should be constructed.
 *
 *  Revision 1.4  2004/03/15 06:10:40  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.3  2004/03/15 03:28:15  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.2  2003/02/20 20:04:03  dennis
 *  Now implements Serializable
 *
 *  Revision 1.1  2003/02/18 18:59:32  dennis
 *  Initial version.
 *
 */

package  DataSetTools.instruments;

import gov.anl.ipns.MathTools.Geometry.*;

import java.io.*;

/**
 *  This class is an abstract base class for objects that store the 
 *  goniometer angles phi, chi and omega and provide the sample rotation matrix
 *  and it inverse.  Derived classes may use different sign conventions for
 *  the angles or may use radians or degrees for the angles.  However, each
 *  derived class must provide the corresponding rotation matrices relative 
 *  to a right hand coordinate system, centered on the sample, with the 
 *  positive x-axis pointing in the direction the beam is traveling and 
 *  the z-axis vertical.
 */  

abstract public class SampleOrientation implements Serializable
{
  // NOTE: any field that is static or transient is NOT serialized.
  //
  // CHANGE THE "serialVersionUID" IF THE SERIALIZATION IS INCOMPATIBLE WITH
  // PREVIOUS VERSIONS, IN WAYS THAT CAN NOT BE FIXED BY THE readObject()
  // METHOD.  SEE "IsawSerialVersion" COMMENTS BELOW.  CHANGING THIS CAUSES
  // JAVA TO REFUSE TO READ DIFFERENT VERSIONS.
  //
  public  static final long serialVersionUID = 1L;

  // NOTE: The following fields are serialized.  If new fields are added that
  //       are not static, reasonable default values should be assigned in the
  //       readObject() method for compatibility with old servers, until the
  //       servers can be updated.
  private int IsawSerialVersion = 1;         // CHANGE THIS WHEN ADDING OR
                                             // REMOVING FIELDS, IF
                                             // readObject() CAN FIX ANY
                                             // COMPATIBILITY PROBLEMS
  protected float   phi;
  protected float   chi;
  protected float   omega;
  protected Tran3D  goniometer_rotation;
  protected Tran3D  goniometer_rotation_inverse;


  /**
   *  Construct a SampleOrientation object from the values of phi, chi and
   *  omega,specified in degrees, following the conventions of the particular
   *  goniometer.
   *
   *  @param  phi    This is the angle to rotate by first about the z-axis. 
   *  @param  chi    This is the angle to rotate by second about the x-axis.
   *  @param  omega  This is the angle to rotate by third about the z-axis.
   *              
   */
  public SampleOrientation( float phi, float chi, float omega  )
  {
    this.phi   = phi;  
    this.chi   = chi;
    this.omega = omega;

    build_transforms();
  }
 

  /**
   *  Get the value of the "phi" goniometer angle, for this sample orientation.
   *
   *  @return the value of "phi", as a float.
   */
  public float getPhi()
  {
    return phi;
  }


  /**
   *  Get the value of the "chi" goniometer angle, for this sample orientation.
   *
   *  @return the value of "chi" as a float.
   */
  public float getChi()
  {
    return chi;
  }


  /**
   *  Get the value of the "omega" goniometer angle for this sample orientation.
   *
   *  @return the value of "omega" as a float.
   */
  public float getOmega()
  {
    return omega;
  }


  /**
   *  Get the rotation matrix representing the rotation of the sample in
   *  the Goniometer by the current phi, chi and omega values.  The 
   *  conventions for the interpretation of phi, chi and omega, are 
   *  determined by the particular derived class.
   *
   *  @return the rotation matrix representing the rotation applied to 
   *          the sample using the current values of the angles phi, chi 
   *          and omega.
   */
  public Tran3D getGoniometerRotation()
  {
    return new Tran3D( goniometer_rotation );  // NOTE: If Tran3D wasn't
                                               // mutable, we could have 
                                               // returned a referenc
  }


  /**
   *  Get the inverse of the rotation matrix representing the rotation 
   *  of the sample in the Goniometer by the current phi, chi and omega 
   *  values.  The matrix returned is the matrix required to "unwind" the
   *  rotation and put measured "Q" values in the same coordinate system,
   *  relative to the crystal.  The conventions for the interpretation of
   *  of phi, chi and omega, are determined by the particular derived class.
   *
   *  @return the matrix that reverses the goniometer rotations to put
   *          measured "Q" values in a coordinate system relative to the
   *          crystal.
   */
  public Tran3D getGoniometerRotationInverse()
  {
    return new Tran3D( goniometer_rotation_inverse );
                                               // NOTE: If Tran3D wasn't
                                               // mutable, we could have 
                                               // returned a referenc
  }


  /**
   *  Construct the transform representing the rotation and inverse rotation
   *  of the goniometer, following the conventions of the specific type
   *  of goniometer corresponding to the concrete derived class.
   */
  abstract protected void build_transforms();


/* -----------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

/* ---------------------------- readObject ------------------------------- */
/**
 *  The readObject method is called when objects are read from a serialized
 *  ojbect stream, such as a file or network stream.  The non-transient and
 *  non-static fields that are common to the serialized class and the
 *  current class are read by the defaultReadObject() method.  The current
 *  readObject() method MUST include code to fill out any transient fields
 *  and new fields that are required in the current version but are not
 *  present in the serialized version being read.
 */

  private void readObject( ObjectInputStream s ) throws IOException,
                                                        ClassNotFoundException
  {
    s.defaultReadObject();               // read basic information

    if ( IsawSerialVersion != 1 )
      System.out.println("Warning:SampleOrientation IsawSerialVersion != 1");
  }

} 

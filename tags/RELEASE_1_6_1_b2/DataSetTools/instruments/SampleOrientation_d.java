/*
 * File:  SampleOrientation_d.java
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
 *  Revision 1.1  2003/07/17 21:46:15  dennis
 *  Double precision version, ported from single precision version.
 *
 */

package  DataSetTools.instruments;

import java.io.*;
import DataSetTools.math.*;
import java.util.*;

/**
 *  This class is an abstract base class for objects that store double 
 *  precision goniometer angles phi, chi and omega and provide the sample 
 *  rotation matrix and its inverse.  
 *  Derived classes may use different sign conventions for
 *  the angles or may use radians or degrees for the angles.  However, each
 *  derived class must provide the corresponding rotation matrices relative 
 *  to a right hand coordinate system, centered on the sample, with the 
 *  positive x-axis pointing in the direction the beam is traveling and 
 *  the z-axis vertical.
 */  

abstract public class SampleOrientation_d implements Serializable
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
  protected double phi;
  protected double chi;
  protected double omega;

  /**
   *  Construct a SampleOrientation_d object with the values specified in
   *  degrees.
   *
   *  @param  phi    This is the angle to rotate by first about the z-axis. 
   *  @param  chi    This is the angle to rotate by second about the x-axis.
   *  @param  omega  This is the angle to rotate by third about the z-axis.
   *              
   */
  public SampleOrientation_d( double phi, double chi, double omega  )
  {
    this.phi   = phi;
    this.chi   = chi;
    this.omega = omega;
  }
 
  /**
   *  Set the value of the "phi" goniometer angle, for this
   *  sample orientation.
   *
   *  @param  phi    The phi angle
   */
  public void setPhi( double phi )
  {
    this.phi = phi;
  }

  /**
   *  Get the value of the "phi" goniometer angle, for this sample orientation.
   *
   *  @return the value of "phi", as a double.
   */
  public double getPhi()
  {
    return phi;
  }

  /**
   *  Set the value of the "chi" goniometer angle, for this sample orientation.
   *
   *  @param  chi    The chi angle
   */
  public void setChi( double chi )
  {
    this.chi = chi;
  }

  /**
   *  Get the value of the "chi" goniometer angle, for this sample orientation.
   *
   *  @return the value of "chi" as a double.
   */
  public double getChi()
  {
    return chi;
  }

  /**
   *  Set the value of the "omega" goniometer angle for this sample orientation.
   *
   *  @param  omega    The omega angle
   */
  public void setOmega( double omega )
  {
    this.omega = omega;
  }

  /**
   *  Get the value of the "omega" goniometer angle for this sample orientation.
   *
   *  @return the value of "omega" as a double.
   */
  public double getOmega()
  {
    return omega;
  }

  /**
   *  Get the rotation matrix representing the rotation of the sample in
   *  the Goniometer by the current phi, chi and omega values.  The sign
   *  conventions used are determined by the particular derived class.
   *
   *  @return the rotation matrix representing the rotation applied to 
   *          the sample using the current values of the angles phi, chi 
   *          and omega.
   */
  abstract public Tran3D_d getGoniometerRotation();

  /**
   *  Get the inverse of the rotation matrix representing the rotation 
   *  of the sample in the Goniometer by the current phi, chi and omega 
   *  values.  The matrix returned is the matrix required to "unwind" the
   *  rotation and put measured "Q" values in the same coordinate system,
   *  relative to the crystal.
   *
   *  @return the matrix that reverses the goniometer rotations to put
   *          measured "Q" values in a coordinate system relative to the
   *          crystal.
   */
  abstract public Tran3D_d getGoniometerRotationInverse();

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
      System.out.println("Warning:SampleOrientation_d IsawSerialVersion != 1");
  }

} 

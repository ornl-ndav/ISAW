/*
 * File:  Rotate.java
 *
 * Copyright (C) 2004, Dennis Mikkelson
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
 * Modified:
 *
 *  $Log: Rotate.java,v $
 *  Revision 1.5  2006/07/20 15:23:53  dennis
 *  Updated from CVS repository at isaw.mscs.uwstout.edu.
 *  This added a method apply_to() to apply the transformation
 *  to a particular vector.
 *  Also added get methods for the basic information that
 *  can be set.
 *
 *  Revision 1.5  2005/11/23 00:17:08  dennis
 *  Added public method, apply_to(), that will apply the transformation
 *  to a specified vector.
 *  Added main program to provide basic test of apply_to() method.
 *  Made minor revisions to some javadoc comments.
 *
 *  Revision 1.4  2005/10/15 19:48:23  dennis
 *  Minor fixes to java docs.
 *
 *  Revision 1.3  2004/12/13 05:02:27  dennis
 *  Minor fix to documentation
 *
 *  Revision 1.2  2004/11/22 18:45:21  dennis
 *  Documented empty body of default constructor.
 *
 *  Revision 1.1  2004/10/25 21:59:50  dennis
 *  Added to SSG_Tools CVS repository
 */

package SSG_Tools.SSG_Nodes.Groups.Transforms;

import net.java.games.jogl.*;
import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  Instances of this class are Group nodes that transform their children using
 *  rotation by a specified angle around a specified 3D axis.
 */
public class Rotate extends TransformGroup
{
  float  ax = 0;                             // components of unit vector 
  float  ay = 0;                             // giving axis of rotation
  float  az = 1;
  float  angle_deg = 0;                      // rotation angle in degrees


  /* ---------------------- default constructor -------------------------- */
  /**
   *  Construct a Rotate node that will rotate its child nodes by a 
   *  specified angle about a specified axis.  The default rotation  
   *  is the identity transformation, that is, it rotates by 0 radians
   *  about the z axis.
   */
  public Rotate()
  {
    // Default constructor, makes identity transform
  }

  
  /* --------------------------- constructor ----------------------------- */
  /**
   *  Construct a Rotate node that will rotate its child nodes about the
   *  specified axis by the specifed angle (in radians).
   *
   *  @param  angle The angle (in radians) to rotate about the specified axis.
   *  @param  axis  Vector containing the axis direction for this rotation 
   */
  public Rotate( float angle, Vector3D axis )
  {
    set( angle, axis );
  }


  /* ------------------------------- set --------------------------------- */
  /**
   *  Set a new angle and axis of rotation for this transform.
   *
   *  @param  angle_rad   The angle (in radians) to rotate about the 
   *                      specified axis.
   *  @param  axis        Vector containing the axis direction for this 
   *                      rotation 
   */
  public void set( float angle_rad, Vector3D axis )
  {
    String status = testAndSetAxis( axis );
    if ( status != null )
    {
      System.out.println("Error: " + status + " in Rotate.setRotation");
      return;
    }

    angle_deg = (float)( angle_rad * 180.0 / Math.PI );
  }


  /* ---------------------------- setAngle ------------------------------- */
  /**
   *  Set the current angle of rotation for this rotation in radians 
   *
   *  @param  angle_rad   The angle (in radians) to rotate about the 
   *                      specified axis.
   */
  public void setAngle( float angle_rad )
  {
    angle_deg = (float)( angle_rad * 180.0 / Math.PI );
  }


  /* ---------------------------- setAxis ------------------------------- */
  /**
   *  Set a new axis of rotation this rotation.
   *
   *  @param  axis        Vector containing the new axis direction for this 
   *                      rotation 
   */
  public void setAxis( Vector3D axis )
  {
    String status = testAndSetAxis( axis );
    if ( status != null )
    {
      System.out.println("Error: " + status + " in Rotate.setAxis");
      return;
    }
  }


  /* ---------------------------- getAngle ------------------------------- */
  /**
   *  Get the current angle of rotation for this rotation in radians 
   *
   *  @return  the angle of rotation
   */
  public float getAngle()
  {
    return (float)( angle_deg * Math.PI / 180.0 );
  }


  /* ---------------------------- getAxis ------------------------------- */
  /**
   *  Get the vector containing the current axis of rotation for this
   *  rotation.
   *
   *  @return the unit vector containing the current axis of rotation.
   */
  public Vector3D getAxis()
  {
    return new Vector3D( ax, ay, az );
  }


  /* -------------------------- applyTransform ---------------------------- */
  /**
   *  Call glRotate to actually apply the rotation specified by the 
   *  current axis and angle. 
   */
  protected void applyTransform( GL gl )
  {
    gl.glRotatef( angle_deg, ax, ay, az );
  }


  /* --------------------------- apply_to --------------------------- */
  /**
   *  Rotate the specified Vector3D object by this rotation transform.   
   *  This method allows an application to track the current position of 
   *  an object.
   *
   *  @param  in_vec   The vector that the transform is applied to.
   *  @param  out_vec  The value of this vector is set to the result 
   *                   of applying the transform to in_vec.
   */
  public void apply_to( Vector3D in_vec, Vector3D out_vec )
  {
     if ( in_vec == null || out_vec == null )
       throw new IllegalArgumentException("null vector in Rotate.apply_to()");

     Tran3D rotation = new Tran3D();
     rotation.setRotation( angle_deg, new Vector3D( ax, ay, az ) );
     rotation.apply_to( in_vec, out_vec );
  }


  /* -----------------------------------------------------------------------
   *
   *  PRIVATE METHODS
   *
   */

  /* ------------------------ testAndSetAxis ----------------------------- */
  /*
   *  This method does "sanity checks" on the new axis vector and records 
   *  the new axis vector if it passes the checks.
   *
   *  @return  an error string if something went wrong, or null if the axis
   *           vector was valid.
   */
  private String testAndSetAxis( Vector3D axis )
  {
    if ( axis == null )
      return "null axis vector";
    else if ( axis.length() == 0 )
      return "zero length axis vector";

    Vector3D new_axis =  new Vector3D( axis );
    new_axis.normalize();
    float a[] = new_axis.get();
    ax = a[0];
    ay = a[1];
    az = a[2];
    return null;
  }


  /* ----------------------------- main -------------------------------- */
  /**
   *  Main program to test basic functionality of this class.
   */
  public static void main( String args[] )
  {
    Vector3D axis = new Vector3D( 1, 1, 1 );

    Rotate tran = new Rotate( 3.14159265f/1.5f, axis );

    Vector3D temp = new Vector3D( 0, 1, 0 );
    Vector3D temp2 = new Vector3D();

    tran.apply_to( temp, temp2 );
    System.out.println("Transformed vector is " + temp2 );
    System.out.println("Should be essentially 0, 0, 1");
  }

}

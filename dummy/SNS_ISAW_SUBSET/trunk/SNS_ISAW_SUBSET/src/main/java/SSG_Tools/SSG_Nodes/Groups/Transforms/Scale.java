/*
 * File:  Scale.java
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
 *  $Log$
 *  Revision 1.7  2007/08/14 00:03:31  dennis
 *  Major update to JSR231 based version from UW-Stout repository.
 *
 *  Revision 1.7  2006/09/10 19:33:32  dennis
 *  Switched to use new Vector3D representation with separate fields
 *  for x,y,z and w.  Since the Vector3D.get() method now just returns
 *  a copy of the values in an array, the values in the Vector3D object
 *  can no longer be altered by altering the values in the array.
 *
 *  Revision 1.6  2006/08/04 02:16:21  dennis
 *  Updated to work with JSR-231, 1.0 beta 5,
 *  instead of jogl 1.1.1.
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
 *  Revision 1.2  2004/11/22 18:44:42  dennis
 *  Documented empty body of default constructor.
 *
 *  Revision 1.1  2004/10/25 21:59:50  dennis
 *  Added to SSG_Tools CVS repository
 */

package SSG_Tools.SSG_Nodes.Groups.Transforms;

import javax.media.opengl.*;
import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  Instances of this class are Group nodes that transform their children using
 *  scaling by specified scale factors in the x, y and z directions.
 */
public class Scale extends TransformGroup
{
  float sx = 1;
  float sy = 1;
  float sz = 1;


  /* ---------------------- default constructor -------------------------- */
  /**
   *  Construct a scale transform node that will scale its child nodes
   *  by specified scale factors.  The default scale transform is the identity
   *  transformation, that is, it scales by 1 in all directions. 
   */
  public Scale()
  {
    // Default constructor, uses identity transform.
  }

  
  /* --------------------------- constructor ----------------------------- */
  /**
   *  Construct a scale transform node that will scale its child nodes
   *  by the specified scale factors. 
   *
   *  @param  scale_factors  Vector containing the scale factors to use 
   *                         for this scale transform 
   */
  public Scale( Vector3D scale_factors )
  {
    set( scale_factors );
  }


  /* -------------------------------- set -------------------------------- */
  /**
   *  Set a new set of scale factors for this scale transform.
   *
   *  @param  scale_factors  Vector containing the scale factors to use 
   *                         for this scale transform 
   */
  public void set( Vector3D scale_factors ) 
  {
    if ( scale_factors == null )
    {
      System.out.println("Error: null scale vector in Scale.setScale");
      return;
    }

    float s[] = scale_factors.get();
    sx = s[0];
    sy = s[1];
    sz = s[2];
  }


  /* ------------------------------- get --------------------------------- */
  /**
   *  Get the vector containing the current scale factors for this scale
   *  transform.
   *
   *  @return  the vector of scale factors for this transform.
   */
  public Vector3D get()
  {
    return new Vector3D( sx, sy, sz );
  }


  /* -------------------------- applyTransform ---------------------------- */
  /**
   *  Call glScale to actually apply the requested scale factors.
   */
  protected void applyTransform( GL gl )
  {
    gl.glScalef( sx, sy, sz );
  }


  /* --------------------------- apply_to --------------------------- */
  /**
   *  Scale the specified Vector3D object by this scaling transform.   
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
       throw new IllegalArgumentException("null vector in Scale.apply_to()");

     float coords[] = in_vec.get();
     coords[0] *= sx;
     coords[1] *= sy;
     coords[2] *= sz;
     out_vec.set( coords );
  }


  /* ----------------------------- main -------------------------------- */
  /**
   *  Main program to test basic functionality of this class.
   */
  public static void main( String args[] )
  {
    Scale tran = new Scale( new Vector3D( 2, 3, 4 ) );

    Vector3D temp = new Vector3D( 10, 20, 30 );
    Vector3D temp2 = new Vector3D();

    tran.apply_to( temp, temp2 );
    System.out.println("Transformed vector is " + temp2 );
    System.out.println("Should be 20, 60, 120");
  }

}

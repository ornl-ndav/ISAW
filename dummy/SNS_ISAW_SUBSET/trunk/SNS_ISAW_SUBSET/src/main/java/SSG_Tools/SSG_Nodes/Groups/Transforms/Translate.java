/*
 * File:  Translate.java
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
 *  $Log: Translate.java,v $
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
 *  Revision 1.2  2004/11/22 18:44:57  dennis
 *  Documented empty body of default constructor.
 *
 *  Revision 1.1  2004/10/25 21:59:50  dennis
 *  Added to SSG_Tools CVS repository
 */

package SSG_Tools.SSG_Nodes.Groups.Transforms;

import javax.media.opengl.*;
import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This class implements Group nodes that transform their children using
 *  translation by a specified 3D vector.
 */
public class Translate extends TransformGroup
{
  float tx = 0;
  float ty = 0;
  float tz = 0;


  /* ---------------------- default constructor -------------------------- */
  /**
   *  Construct a Translate transform node that will translate its child nodes
   *  by a specified vector.  The default translation is the identity
   *  transformation, that is, it translates by the zero vector. 
   */
  public Translate()
  {
    // Default constructor, uses identity transform
  }

  
  /* --------------------------- constructor ----------------------------- */
  /**
   *  Construct a Translate tranform node that will translate its child nodes
   *  by the specified vector. 
   *
   *  @param  translation  The translation vector to use for this transform
   */
  public Translate( Vector3D translation )
  {
    set( translation );
  }


  /* ------------------------------- set --------------------------------- */
  /**
   *  Set a new translation vector for this transform.
   *
   *  @param  translation  The new translation vector to use for this transform
   */
  public void set( Vector3D translation )
  {
    if ( translation == null )
    {
      System.out.println("Error: null translation in Translate.set");
      return;
    }

    float t[] = translation.get();
    tx = t[0];
    ty = t[1];
    tz = t[2];
  }


  /* ------------------------------- get --------------------------------- */
  /**
   *  Get the current translation vector for this transform.
   *
   *  @return  the translation vector for this transform.
   */
  public Vector3D get()
  {
    return new Vector3D( tx, ty, tz );
  }


  /* -------------------------- applyTransform --------------------------- */
  /**
   *  Call glTranslate to actually apply the requested translation. 
   */
  protected void applyTransform( GL gl )
  {
    gl.glTranslatef( tx, ty, tz );
  }


  /* ------------------------------- apply_to ---------------------------- */
  /**
   *  Translate the specified Vector3D object by this translation.   
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
       throw new IllegalArgumentException(
                                "null vector in Translate.apply_to()");
     float coords[] = in_vec.get(); 
     coords[0] += tx;
     coords[1] += ty;
     coords[2] += tz;
     out_vec.set( coords );
  }


  /* ----------------------------- main -------------------------------- */
  /**
   *  Main program to test basic functionality of this class.
   */
  public static void main( String args[] )
  {
    Translate tran = new Translate( new Vector3D( 1, 2, 3 ) );

    Vector3D temp = new Vector3D( 4, 5, 6 );  
    Vector3D temp2 = new Vector3D();

    tran.apply_to( temp, temp2 );
    System.out.println("Transformed vector is " + temp2 );
    System.out.println("Should be 5, 7, 9 "); 
  }
  
}

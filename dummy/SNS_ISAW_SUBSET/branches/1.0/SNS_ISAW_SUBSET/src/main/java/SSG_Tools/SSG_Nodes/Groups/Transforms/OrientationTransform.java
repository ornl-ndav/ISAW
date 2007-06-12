/*
 * File:  OrientationTransform.java
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
 *  $Log: OrientationTransform.java,v $
 *  Revision 1.4  2006/07/20 15:23:52  dennis
 *  Updated from CVS repository at isaw.mscs.uwstout.edu.
 *  This added a method apply_to() to apply the transformation
 *  to a particular vector.
 *  Also added get methods for the basic information that
 *  can be set.
 *
 *  Revision 1.3  2005/10/15 19:47:22  dennis
 *  Added methods to allow getting and setting of base, up and
 *  translation vectors separately.
 *
 *  Revision 1.2  2004/12/13 05:02:27  dennis
 *  Minor fix to documentation
 *
 *  Revision 1.1  2004/11/22 19:45:27  dennis
 *  Initial version of transformation that both orients and translates
 *  an object.
 */

package SSG_Tools.SSG_Nodes.Groups.Transforms;

import gov.anl.ipns.MathTools.Geometry.*;


/**
 *  This class represents transformation nodes that both "rotate" an object
 *  to align it's x and y axes to specified "base" and "up" directions AND
 *  translates the "center" of the object to a specified position.
 */
public class OrientationTransform extends MatrixTransform 
{
                               // keep local copies of transform info, to
                               // allow separately changing the orientation
                               // and position.
  private Vector3D base  = new Vector3D( 1, 0, 0 );
  private Vector3D up    = new Vector3D( 0, 1, 0 );
  private Vector3D place = new Vector3D( 0, 0, 0 );


  /* ------------------------ default constructor ------------------------- */
  /**
   *  Construct an orientation transformation node that uses defaults 
   *  of translation by (0,0,0) and no rotation. 
   */
  public OrientationTransform()
  {
    // Use identity matrix by default
  }


  /* --------------------------- constructor ----------------------------- */
  /**
   *  Construct an orientation transformation node using the specified 
   *  position, base and up vectors.
   *
   *  @param base_vec  Vector that the local x-axis of the object should be
   *                   mapped to.
   *  @param up_vec    Vector that the local y-axis of the object should be
   *                   mapped to.
   *  @param position  The location that the center of the object should be
   *                   mapped to.
   */
  public OrientationTransform( Vector3D  base_vec, 
                               Vector3D  up_vec, 
                               Vector3D  position )
  {
    base  = new Vector3D( base_vec );
    up    = new Vector3D( up_vec );
    place = new Vector3D( place );
    setMatrix( base_vec, up_vec, position );
  }


  /* ----------------------------- setBaseVec ----------------------------- */
  /**
   *  Set a new base vector for this transform.
   *
   *  @param base_vec  Vector that the local x-axis of the object should be
   *                   mapped to.
   */
  public void setBaseVec( Vector3D base_vec )
  {
    setMatrix( base_vec, up, place );
  }


  /* ------------------------------ setUpVec ------------------------------ */
  /**
   *  Set a new up vector for this transform.
   *
   *  @param up_vec    Vector that the local y-axis of the object should be
   *                   mapped to.
   */
  public void setUpVec( Vector3D up_vec )
  {
    setMatrix( base, up_vec, place );
  }


  /* --------------------------- setOrientation --------------------------- */
  /**
   *  Set all of the orientation information for this transform. 
   *
   *  @param base_vec  Vector that the local x-axis of the object should be
   *                   mapped to.
   *  @param up_vec    Vector that the local y-axis of the object should be
   *                   mapped to.
   */
  public void setOrientation( Vector3D  base_vec, Vector3D  up_vec )
  {
    setMatrix( base_vec, up_vec, place );
  }


  /* ------------------------------ setPosition --------------------------- */
  /**
   *  Set a new translation vector for this transform.
   *
   *  @param  position  The new position vector to use for this transform
   */
  public void setPosition( Vector3D position )
  {
    setMatrix( base, up, position );
  }


  /* ----------------------------- getBaseVec ----------------------------- */
  /**
   *  Get the current base vector for this transform.
   *
   *  @return  The base vector used for this transform
   */
  public Vector3D getBaseVec()
  {
    return new Vector3D( base );
  }


  /* ------------------------------ getUpVec ------------------------------ */
  /**
   *  Get the current up vector for this transform.
   *
   *  @return  The up vector used for this transform
   */
  public Vector3D getUpVec()
  {
    return new Vector3D( up );
  }


  /* ----------------------------- getPosition ---------------------------- */
  /**
   *  Get the current translation vector for this transform.
   *
   *  @return  The translation vector used for this transform
   */
  public Vector3D getPosition()
  {
    return new Vector3D( place );
  }


  /* ------------------------------- setMatrix ----------------------------- */
  /**
   *  Set this transform to be the transform representing a "rotation" to
   *  align the x,y axes with the specified base vector and up vector,
   *  AND translate to origin to the specified position.
   *
   *  @param base_vec  Vector that the local x-axis of the object should be
   *                   mapped to.
   *  @param up_vec    Vector that the local y-axis of the object should be
   *                   mapped to.
   *  @param position  The location that the center of the object should be
   *                   mapped to.
   */
  public void setMatrix( Vector3D  base_vec, 
                         Vector3D  up_vec, 
                         Vector3D  position )
  {
    if ( base_vec == null || base_vec.length() == 0 )
    {
      System.out.println("base vector invalid in " +
                         "OrientationTransform.SetMatrix() " + base_vec );
      return;
    }

    if ( up_vec == null || up_vec.length() == 0 )
    {
      System.out.println("up vector invalid in " +
                         "OrientationTransform.SetMatrix() " + up_vec );
      return;
    }

    if ( position == null )
    {
      System.out.println("Null position vector in " +
                         "OrientationTransform.SetMatrix() " + up_vec );
      return;
    }

    base  = new Vector3D( base_vec );
    up    = new Vector3D( up_vec );
    place = new Vector3D( position );

    Tran3D tran = new Tran3D();
    tran.setOrientation( base_vec, up_vec, position );
    super.setMatrix( tran.get() );
  }

}

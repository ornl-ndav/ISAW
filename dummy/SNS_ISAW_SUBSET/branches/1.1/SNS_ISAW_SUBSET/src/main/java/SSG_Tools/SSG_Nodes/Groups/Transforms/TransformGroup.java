/*
 * File:  TransformGroup.java
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
 *  $Log: TransformGroup.java,v $
 *  Revision 1.3  2006/07/20 15:23:53  dennis
 *  Updated from CVS repository at isaw.mscs.uwstout.edu.
 *  This added a method apply_to() to apply the transformation
 *  to a particular vector.
 *  Also added get methods for the basic information that
 *  can be set.
 *
 *  Revision 1.3  2005/11/23 00:16:09  dennis
 *  Added public method, apply_to(), that will apply the transformation
 *  to a specified vector.
 *
 *  Revision 1.2  2004/12/13 05:02:27  dennis
 *  Minor fix to documentation
 *
 *  Revision 1.1  2004/10/25 21:59:50  dennis
 *  Added to SSG_Tools CVS repository
 */

package SSG_Tools.SSG_Nodes.Groups.Transforms;

import net.java.games.jogl.*;
import SSG_Tools.SSG_Nodes.*;
import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This is the abstract base class for group nodes that apply a transformation
 *  BEFORE rendering their child nodes.  That is, the transformation is applied
 *  to all children of the group.  Furthermore, the matrix stack is pushed
 *  before applying the transformation and rendering the children, and the
 *  matrix stack is popped afterwards.  Consequently the transformation ONLY
 *  is applied to the decendendants of this node, not to any ancestors or
 *  siblings.
 */
abstract public class TransformGroup extends Group
{

  /* ------------------------------ Render ------------------------------ */
  /**
   *  Rendering this node will push the GL matrix stack, apply the  
   *  transformation then render the children and finally pop the matrix stack.
   *
   *  @param  drawable  The OpenGL drawable on which the transform should be
   *                    applied.
   */ 
  public void Render( GLDrawable drawable )
  {
    GL gl = drawable.getGL();
                             // to render a TransformGroup, we apply the 
                             // transform and render the children inside of
                             // a push/pop pair.
    gl.glPushMatrix();
      applyTransform( gl );
      super.Render(drawable);
    gl.glPopMatrix();
  }


  /* --------------------------- applyTransform --------------------------- */
  /**
   *  Derived classes will implement this method to issue the OpenGL 
   *  commands needed to multiply the matrix on the top of the OpenGL
   *  matrix stack times this transformation.
   *
   *  @param  gl  The GL object for the OpenGL commands.
   */
  abstract protected void applyTransform( GL gl );


  /* --------------------------- apply_to --------------------------- */
  /**
   *  Derived classes will implement this method to transform the 
   *  specified Vector3D object times this transformation.   This method
   *  allows an application to track the current position of an object.
   *
   *  @param  in_vec   The vector that the transform is applied to.
   *  @param  out_vec  The value of this vector is set to the result 
   *                   of applying the transform to in_vec.
   */
  abstract public void apply_to( Vector3D in_vec, Vector3D out_vec );

}

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
 *  Last Modified:
 * 
 *  $Author: eu7 $
 *  $Date: 2008-08-21 14:00:12 -0500 (Thu, 21 Aug 2008) $            
 *  $Revision: 298 $
 *
 *  $Log: TransformGroup.java,v $
 *
 *  Updated 2008/08/21 from UW-Stout repository.
 *
 *  Revision 1.5  2007/10/23 19:05:53  dennis
 *  Added method getCummulativeTransform() that calculates the resulting
 *  position and orientation for a node in a scene graph.
 *
 *  Revision 1.4  2006/08/04 02:16:21  dennis
 *  Updated to work with JSR-231, 1.0 beta 5,
 *  instead of jogl 1.1.1.
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

import java.util.Vector;

import javax.media.opengl.*;
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
  public void Render( GLAutoDrawable drawable )
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

  
  /* ----------------------- getCummulativeTransform --------------------- */
  /**
   *  Get an OrientationTransform that represents the cummulative effect
   *  of all transforms applied to the specified node.  This method will
   *  repeatedly use getParent() to find all ancestors of this node. 
   *  References to ancestors that are TransformGroups are saved and then
   *  applied in sequence to the vectors (0,0,0), (1,0,0) and (0,1,0) to 
   *  calculate the final position and orientation of an object transformed
   *  by the ancestor transformations.  This will work correctly provided 
   *  two conditions are met.  
   *    First, all of the transforms applied to the node must rigid motions
   *  or uniform scaling operations.  If some non-uniform scaling or other
   *  arbitrary matrix transforms have been used, this will probably not
   *  work correctly, since the orthonormal basis vectors i,j,k may be 
   *  transformed to a non-orthogonal set of vectors.
   *    Second, the node and each of it's ancestors must have at most one
   *  parent. 
   *  
   *  @param node   The node for which the cummulative transformation is
   *                calculated.  The specified node can be any node in a
   *                scene graph.  If the specified node is a transformation
   *                node, then the effect of that transformation node is
   *                also included in the cummulative transformation.
   *                
   *  @return Any OrientationTransform constructed from the final position
   *          and orientation of the origin and basis vectors, as transformed
   *          by this node and any ancestor nodes.
   */
  public static OrientationTransform getCummulativeTransform( Node node )
  {
    Vector<TransformGroup> transforms = new Vector<TransformGroup>();

    while ( node != null )                     // build up list of transforms 
    {                                          // that were applied in reverse
      if ( node instanceof TransformGroup )    // order
        transforms.add( (TransformGroup)node );       
      node = node.getParent();     
    }
                                                // now find where origin and
                                                // i & j basis vectors move to
    Vector3D origin   = new Vector3D( 0, 0, 0 );
    Vector3D base_vec = new Vector3D( 1, 0, 0 );
    Vector3D up_vec   = new Vector3D( 0, 1, 0 );
    for ( int k = transforms.size()-1; k >= 0; k-- )  // apply transforms 
    {                                                 // in order
      transforms.elementAt(k).apply_to(origin,origin);
      transforms.elementAt(k).apply_to( base_vec, base_vec );
      transforms.elementAt(k).apply_to( up_vec, up_vec );
    }
    base_vec.subtract( origin );
    up_vec.subtract( origin );
    
    return new OrientationTransform( base_vec, up_vec, origin );
  }
  
}

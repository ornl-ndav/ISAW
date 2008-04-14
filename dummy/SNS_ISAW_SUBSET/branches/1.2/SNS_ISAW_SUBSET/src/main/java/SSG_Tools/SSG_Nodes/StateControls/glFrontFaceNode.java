/*
 * File:glFrontFaceNode.java
 *
 * Copyright (C) 2006 Dennis Mikkelson
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
 *  $Log: glFrontFaceNode.java,v $
 *  Revision 1.1  2007/08/14 00:31:16  dennis
 *  Nodes to control the OpenGL state added from SSG_Tools
 *  at UW-Stout.
 *
 *  Revision 1.2  2006/12/10 06:14:53  dennis
 *  Minor fixes to javadocs.
 *
 *  Revision 1.1  2006/12/09 21:27:19  dennis
 *  Initial version of node to specify which winding order
 *  (clockwize or counterclockwize) is used for front faces.
 *
 */
package SSG_Tools.SSG_Nodes.StateControls;

import javax.media.opengl.*;
 
import SSG_Tools.SSG_Nodes.*;


/**
 *  This node calls glFrontFace with the specified parameter when it is 
 *  rendered.  This allows modifying the way that OpenGL determines which 
 *  faces are front faces when OpenGL renders later nodes.  In many cases, 
 *  if a glFrontFaceNode is used to modify the behavior of OpenGL during 
 *  scene graph traversal, the scene graph should also have a second
 *  glFrontFaceNode, to reset the previous behavior.  In addition to
 *  specifying how the front face is defined, a glEnableNode must also be
 *  used with the parameter GL.GL_CULL_FACE to actually turn on backface
 *  culling.  
 *     This capability may be needed if backface culling is turned on and
 *  if a model is used with faces that are specified with a clockwise vertex 
 *  ordering, instead of the default counter clockwise vertex ordering.
 *
 *    NOTE: glFrontFaceNodes should be used sparingly, since by design they
 *  have side effects and may interfere with the normal functioning of
 *  other nodes in SSG_Tools.
 */

public class glFrontFaceNode extends Node
{
  private int winding_order;               // GL.GL_CW or GL.GL_CCW


/* ------------------------------ constructor --------------------------- */
/**
 *  Construct a node to set the front face vertex ordering convention for
 *  subsequent primitives to "clockwise", GL.GL_CW or "counterclockwise",
 *  GL.GL_CCW.
 *
 *  @param  winding_order  The winding order for front faces must be either
 *                         GL.GL_CW or GL.GL_CCW. The default is GL.GL_CCW.
 */ 
  public glFrontFaceNode( int winding_order )
  {
    this.winding_order = winding_order;
  }


/* ------------------------------ Render --------------------------- */
/**
 *  Call glFrontFace to use the specified vertex ordering to determine
 *  which faces are front faces.
 *
 *  @param  drawable  The drawable for which glFrontFace is called.
 */ 
  public void Render( GLAutoDrawable drawable )
  {
     GL gl = drawable.getGL();

     gl.glFrontFace( winding_order );
  }

}

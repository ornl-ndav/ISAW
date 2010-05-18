/*
 * File:glDisableNode.java
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
 *  $Log$
 *  Revision 1.1  2007/08/14 00:31:16  dennis
 *  Nodes to control the OpenGL state added from SSG_Tools
 *  at UW-Stout.
 *
 *  Revision 1.1  2006/11/27 00:10:02  dennis
 *  Initial version added to CVS repository
 *
 *
 */
package SSG_Tools.SSG_Nodes.StateControls;

import javax.media.opengl.*;
 
import SSG_Tools.SSG_Nodes.*;


/**
 *  This node calls glDisable with a specified parameter when
 *  it is rendered.  This allows modifying the way that OpenGL renders
 *  later nodes.  In many cases, if a glEnable (or glDisable) Node is
 *  used to modify the behavir of OpenGL during scene graph traversal, 
 *  the scene graph should also have a corresponding glDisable (or glEnable)
 *  Node to restore the previous behaviour.  For example, if lighting
 *  is temporarily disabled for rendering a coordinate Axis, lighting
 *  would usually be enabled after the Axis is rendered.
 *
 *    NOTE: glDisableNodes should be used sparingly, since by design they
 *  have side effects and may interfere with the normal functioning of
 *  other nodes in SSG_Tools.
 */

public class glDisableNode extends Node
{
  private int capability;               // the capability to be disabled


/* ------------------------------ constructor --------------------------- */
/**
 *  Construct a node to disable a specified OpenGL capability when the
 *  node is rendered.
 *
 *  @param  capability  The OpenGL capability that will be disabled,
 *                      such as GL.GL_Lighting, GL.GL_CULL_FACE, or
 *                      GL.GL_NORMALIZE 
 */ 
  public glDisableNode( int capability )
  {
    this.capability = capability;
  }


/* ------------------------------ Render --------------------------- */
/**
 *  Call glDisable to disable the capability specified when this node
 *  was constructed. 
 *
 *  @param  drawable  The drawable for which the glDisable method will
 *                    be called.
 */ 
  public void Render( GLAutoDrawable drawable )
  {
     GL gl = drawable.getGL();

     gl.glDisable( capability );
  }

}

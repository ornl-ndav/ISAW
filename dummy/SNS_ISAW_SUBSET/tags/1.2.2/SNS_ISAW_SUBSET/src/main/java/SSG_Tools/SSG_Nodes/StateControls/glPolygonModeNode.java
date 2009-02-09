/*
 * File:glPolygonModeNode.java
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
 *  Revision 1.1  2006/12/10 04:06:08  dennis
 *  Initial version of node to switch drawing mode to GL_FILL,
 *  GL_LINE or GL_POINT
 *
 */
package SSG_Tools.SSG_Nodes.StateControls;

import javax.media.opengl.*;
 
import SSG_Tools.SSG_Nodes.*;


/**
 *  This node calls glPolygonMode with the specified parameter when it is 
 *  rendered.  This allows modifying the way that OpenGL draws filled  
 *  primitives, such as polygons and triangles, so that such primitives
 *  are drawn just using lines or points.  In many cases, if a 
 *  glPolygonModeNode is used to modify the behavir of OpenGL during scene 
 *  graph traversal, the scene graph should also have a second
 *  glPolygonModeNode, to reset the previous behavior.
 *
 *    NOTE: glPolygonModeNodes should be used sparingly, since by design they
 *  have side effects and may interfere with the normal functioning of
 *  other nodes in SSG_Tools.
 */

public class glPolygonModeNode extends Node
{
  private int face;               // GL.GL_FRONT, GL.GL_BACK or
                                  // GL.GL_FRONT_AND_BACK  
  private int mode;               // GL.GL_POINT, GL.GL_LINE or GL.GL_FILL


/* ------------------------------ constructor --------------------------- */
/**
 *  Construct a node to set drawing mode of polygons to GL.GL_POINT, 
 *  GL.GL_LINE or GL.GL_FILL
 *
 *  @param  face    The face(s) whose drawing mode is being changed,
 *                  GL.GL_FRONT, GL.GL_BACK or GL.GL_FRONT_AND_BACK
 *  @param  mode    The new drawing mode, GL.GL_POINT, GL.GL_LINE or 
 *                  GL.GL_FILL
 */ 
  public glPolygonModeNode( int face, int mode )
  {
    this.face = face;
    this.mode = mode;
  }


/* ------------------------------ Render --------------------------- */
/**
 *  Call glPolygon mode to set the drawing mode for subsequent filled
 *  primitives.
 *
 *  @param  drawable  The drawable for which glPolygonMode is called.
 */ 
  public void Render( GLAutoDrawable drawable )
  {
     GL gl = drawable.getGL();

     gl.glPolygonMode( face, mode );
  }

}

/*
 * File:glColorNode.java
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
 *  Revision 1.1  2006/12/10 23:34:04  dennis
 *  Initial version of node that calls glColor to set the
 *  current color for subsequent shapes that don't set their own
 *  color.
 *
 */
package SSG_Tools.SSG_Nodes.StateControls;

import java.awt.Color;
import javax.media.opengl.*;
import java.nio.FloatBuffer; 
import SSG_Tools.SSG_Nodes.*;


/**
 *  This node calls glColor4f with the specified color and alpha parameters
 *  when it is rendered.  This allows setting the current color before drawing
 *  a collection of shapes that do not define their own colors.
 *
 *    NOTE: glColorNodes should be used sparingly, since by design they
 *  have side effects and may interfere with the normal functioning of
 *  other nodes in SSG_Tools.
 */

public class glColorNode extends Node
{
  private float[] colors;       // array containing the colors and alpha value


/* ------------------------------ constructor --------------------------- */
/**
 *  Construct a node to set the current color and alpha value.
 *
 *  @param  color   The color to be used.
 *  @param  alpha   The alpha value to be used, normally 1.
 */ 
  public glColorNode( Color color, float alpha )
  {
    colors = new float[4];
    colors[0] = color.getRed()  /255f; 
    colors[1] = color.getGreen()/255f; 
    colors[2] = color.getBlue() /255f; 

    if ( alpha > 1f ) 
      colors[3] = 1f;
    else if ( alpha < 0f )
      colors[3] = 0f;
    else
      colors[3] = alpha;
  }


/* ------------------------------ Render --------------------------- */
/**
 *  Call glColor4 to set the current color and alpha value to the 
 *  values specified when this node was constructed.
 *
 *  @param  drawable  The drawable for which the color will be set.
 */ 
  public void Render( GLAutoDrawable drawable )
  {
     GL gl = drawable.getGL();

     gl.glColor4fv( FloatBuffer.wrap(colors) );
  }

}

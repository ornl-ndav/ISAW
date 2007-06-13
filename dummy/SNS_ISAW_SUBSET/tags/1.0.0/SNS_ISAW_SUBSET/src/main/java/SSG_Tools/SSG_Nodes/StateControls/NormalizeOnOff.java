/*
 * File:  NormalizeOnOff.java
 *
 * Copyright (C) 2005 Dennis Mikkelson
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
 *  $Log:$
 *
 */
package SSG_Tools.SSG_Nodes.StateControls;

import net.java.games.jogl.*;
 
import SSG_Tools.SSG_Nodes.*;


/**
 *  This node allows turning automatic normalization of surface normals
 *  on and off in the course of rendering a scene graph.
 */

public class NormalizeOnOff extends Node
{
  private boolean on;


/* ------------------------------ constructor --------------------------- */
/**
 *  Construct a node to turn automatic normalization on or off when it 
 *  is rendered.
 *
 *  @param on_off  boolean flag indicating whether automatic normalization
 *                 should be turned on or off.
 */ 
  public NormalizeOnOff( boolean on_off )
  {
    on = on_off;
  }


/* ------------------------------ Render --------------------------- */
/**
 *  Enable or disable automatic normaliation of surface normals,
 *  when Render method is called. 
 *
 *  @param  drawable  The drawable for which normalization will be enabled 
 *                    or disabled.
 */ 
  public void Render( GLDrawable drawable )
  {
     GL gl = drawable.getGL();

     if ( on )
       gl.glEnable( GL.GL_NORMALIZE );
     else
       gl.glDisable( GL.GL_NORMALIZE );
  }

}

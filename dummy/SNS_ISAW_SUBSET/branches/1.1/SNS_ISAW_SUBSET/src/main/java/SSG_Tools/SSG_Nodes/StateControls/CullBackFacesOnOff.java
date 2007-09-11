/*
 * File:  CullBackFacesOnOff.java
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
 *  This node allows turning back face culling on and off in the course of
 * rendering a scene graph.
 */

public class CullBackFacesOnOff extends Node
{
  private boolean on;


/* ------------------------------ constructor --------------------------- */
/**
 *  Construct a node to turn back face culling  on or off when it is rendered.
 *
 *  @param on_off  boolean flag indicating whether back face culling should be
 *                 turned on or off.
 */ 
  public CullBackFacesOnOff( boolean on_off )
  {
    on = on_off;
  }


/* ------------------------------ Render --------------------------- */
/**
 *  Enable or disable back face culling when Render method is called. 
 *
 *  @param  drawable  The drawable for which back face culling will be enabled 
 *                    or disabled.
 */ 
  public void Render( GLDrawable drawable )
  {
     GL gl = drawable.getGL();

     if ( on )
       gl.glEnable( GL.GL_CULL_FACE );
     else
       gl.glDisable( GL.GL_CULL_FACE );
  }

}

/*
 * File:  IGL_Renderable.java
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
 *  $Log: IGL_Renderable.java,v $
 *  Revision 1.1  2004/10/25 21:48:01  dennis
 *  Added to SSG_Tools CVS repository
 *
 */

package SSG_Tools;

import net.java.games.jogl.*;

/** 
 *  This interface is the interface that classes must implement in order to
 *  be "rendered" by the JoglDriverProgram object.  The action taken to render
 *  the object may be different for different types of objects, such as
 *  Shapes, Lights, Transforms and Groups.
 */

public interface IGL_Renderable
{
  /**
   *  Render this object using the specified drawable.
   *
   *  @param  drawable  The drawable on which the object is to be rendered.
   */
  public void Render( GLDrawable drawable );

}


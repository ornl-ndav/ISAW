/*
 * File:  IGetGLU.java
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
 * $Log: IGetGLU.java,v $
 * Revision 1.1  2007/08/14 00:42:14  dennis
 * Added files allowing use of GLCanvas or GLJPanel polymorphically.
 * Files copied from SSG_Tools at UW-Stout.
 *
 * Revision 1.1  2006/12/10 05:09:25  dennis
 * Classes to bundle one instance of a GLU object with a
 * GLAutoDrawable.  This will allow constructing ONE GLU object
 * for each JoglPanel, and provide easy access to that GLU object
 * from nodes in the scene graph.
 *
 */
package SSG_Tools.Viewers;

import javax.media.opengl.glu.*;

/**
 *  Classes that implement this interface have a method, getGLU(), to 
 *  obtain a reference to an appropriate GLU object.
 */

public interface IGetGLU
{
  /**
   *  Get a reference to an appropriate GLU object.
   *
   *  @return a reference to a GLU object.
   */
  public GLU getGLU();
 
}

/*
 * File:  SSG_Canvas.java
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
 * $Log: SSG_Canvas.java,v $
 * Revision 1.2  2007/08/26 23:23:21  dennis
 * Updated to latest version from UW-Stout repository.
 *
 * Revision 1.2  2007/08/26 21:07:21  dennis
 * Added serialVersionUID = 1
 *
 * Revision 1.1  2006/12/10 05:09:25  dennis
 * Classes to bundle one instance of a GLU object with a
 * GLAutoDrawable.  This will allow constructing ONE GLU object
 * for each JoglPanel, and provide easy access to that GLU object
 * from nodes in the scene graph.
 *
 */
package SSG_Tools.Viewers;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

/**
 *  This class extends a GLCanvas to include an instance of a jogl GLU
 *  object, so that we can use one instance of a GLU object per "window"
 *  and get immediate access to a GLU object that should be safe to use
 *  on the current thread.
 */

public class SSG_Canvas extends GLCanvas implements IGetGLU
{
  private static final long serialVersionUID = 1;

  GLU glu;
 
  public SSG_Canvas( GLCapabilities capabilities )
  {
    super( capabilities );
    glu = new GLU();
  }

  /**
   *  Get a reference to the GLU object associated with this GLCanvas.
   *
   *  @return a reference to a GLU object that was constructed for use
   *          with this SSG_Canvas object.
   */
  public GLU getGLU()
  {
    return glu;
  } 
 
}

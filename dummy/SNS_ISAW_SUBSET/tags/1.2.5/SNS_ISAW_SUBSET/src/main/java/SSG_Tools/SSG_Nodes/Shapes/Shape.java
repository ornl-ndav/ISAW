/*
 * File:  Shape.java
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
 * $Log$
 * Revision 1.7  2007/08/14 00:03:32  dennis
 * Major update to JSR231 based version from UW-Stout repository.
 *
 * Revision 1.8  2006/10/16 04:06:40  dennis
 * Moved automatic texture coordinate generation from the Shape
 * class to the Geometry class.
 *
 * Revision 1.7  2006/10/11 02:33:00  dennis
 * Removed unused variable "center"
 *
 * Revision 1.6  2006/08/04 02:16:21  dennis
 * Updated to work with JSR-231, 1.0 beta 5,
 * instead of jogl 1.1.1.
 *
 * Revision 1.5  2005/10/25 02:40:58  dennis
 * Further additions to javadoc.
 * Now allows the user to specify GL_EYE_LINEAR.
 *
 * Revision 1.4  2004/12/13 04:51:44  dennis
 * Changed default texture coordinate generation mode to GL_NONE,
 * instead of GL_OBJECT_LINEAR.
 *
 * Revision 1.3  2004/11/15 17:27:19  dennis
 *   Replaced Render() method with a PreRender() method, to take care of
 * texture coordinate generation, if texture coordinates are not specified
 * explicitly in the actual shape.  Also added method PostRender().
 * The Render() method of derived classes should now call preRender() before
 * drawing and postRender() after drawing.
 *   Added methods setTexCoordGenMode() and setTexPlane() to control how texture
 * coordinates are actually generated.
 *
 */

package SSG_Tools.SSG_Nodes.Shapes;

import javax.media.opengl.*;
import SSG_Tools.SSG_Nodes.*;
import SSG_Tools.Appearance.*;

/** 
 *  This is the abstract base class for scene graph nodes that represent
 *  actual objects that are drawn.  Material properties and texture mapping
 *  is also supported by objects derived form this class.  Derived classes 
 *  may define their own geometry, and implement the Render() method to 
 *  to actually draw the object.  This base class provides a preRender() and
 *  postRender() method that takes care of things that are common to all 
 *  shapes.  In particular, the preRender() and postRender() MUST be called
 *  at the start and at the end, respectively, of the Render() method 
 *  of derived classes.
 */

abstract public class Shape extends Node 
{
  private Appearance appearance = null;


  /* --------------------------- setAppearance ------------------------- */
  /**
   *  Record a reference to the appearance properties to use for this shape.
   *  Appearance objects may be shared by several different shapes.
   *
   *  @param  appearance  The new appearance properties to use for this shape. 
   */
   public void setAppearance( Appearance appearance )
   {
     this.appearance = appearance;
   }


  /* -------------------------- getAppearance ------------------------- */
  /**
   *  Get a reference to the appearance for this shape.
   *
   *  @return a reference to the appearance object for this shape. 
   */
   public Appearance getAppearance()
   {
     return appearance;
   }


  /* ------------------------------ preRender --------------------------- */
  /**
   *  Set up the appearance of this shape, before actually rendering the
   *  shape.
   *
   *  @param  drawable  The drawable on which the shape is to be drawn.
   */
  public void preRender( GLAutoDrawable drawable )
  {
    super.preRender(drawable);           // do any initial steps needed by 
                                         // the base classes.

    if ( appearance != null )            // set up the appearance for this 
      appearance.preRender( drawable );  // shape.
  }


  /* ------------------------------ postRender --------------------------- */
  /**
   *  Reset the appearance attributes, after rendering the shape.
   *
   *  @param  drawable  The drawable on which the shape is to be drawn.
   */
  public void postRender( GLAutoDrawable drawable )
  {
    if ( appearance != null )             // set up the appearance for this 
      appearance.postRender( drawable );  // shape.

    super.postRender(drawable);           // reset any initial steps taken by 
                                          // the base classes.
  }

}

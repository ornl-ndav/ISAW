/*
 * File:  SimpleShape.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * Revision 1.2  2007/08/14 00:03:33  dennis
 * Major update to JSR231 based version from UW-Stout repository.
 *
 * Revision 1.3  2006/11/26 01:43:02  dennis
 * Changed to allow a null color.  If color is null, the last color
 * that was set will be used.
 *
 * Revision 1.2  2006/09/24 05:33:21  dennis
 * Fixed getColor() method.  The color components had to be cast
 * to int, or else a constructor for Color that took floats was
 * used and the values 0..255 covered too large of a range.
 *
 * Revision 1.1  2005/10/14 04:04:11  dennis
 * Copied into local CVS repository from CVS repository at IPNS.
 *
 * Revision 1.1  2005/07/25 15:45:24  dennis
 * Abstract base class for "simple" shapes that don't have all material
 * properties specified and don't use texture mapping.
 *
 *
 */

package SSG_Tools.SSG_Nodes.SimpleShapes;

import java.awt.*;
import SSG_Tools.SSG_Nodes.*;

/**
 *  Abstract base class for simple 3D objects that are not textured, and 
 *  whose color is set by the current color, rather than with all of the
 *  possible material properties.  If the color for a simple shape is set
 *  to null, then the shape will be drawn using whatever color was last
 *  specified to OpenGL.  This allows drawing many simple shapes with the
 *  same color, without having to store or set colors for each shape.
 */
abstract public class SimpleShape extends Node
{
  protected float color[] = null;

  /** 
   *  Construct a 3D object of the given color, or just use the color
   *  that was last used by OpenGL, if the color is specified as null. 
   *
   *  @param  new_color  The color to be used for this object, or null
   *                     to just use the current color from OpenGL.
   */
  public SimpleShape( Color new_color )
  {
    setColor( new_color );
  }

  /**
   *  Set the color to be used when this object is redrawn.  If null is
   *  specified, then the color that was last used by OpenGL is used for
   *  this object.
   *
   *  @param new_color Specifies the color to be used when drawing this object.
   */
  public void setColor( Color new_color )
  {
    if ( new_color == null )          // set specified color back to null
    {
       color = null;
       return;
    }

    if ( color == null )              // new_color != null, so store the new
      color = new float[3];           // color.

    color[0] = new_color.getRed()/255f;
    color[1] = new_color.getGreen()/255f;
    color[2] = new_color.getBlue()/255f;
  }


  /**
   *  Get specified color of this object, or null if no color has
   *  been specified.
   *
   *  @return The color used when drawing this object, or null if
   *          no color was specified.
   */
  public Color getColor()
  {
    if ( color == null )
      return null;

    Color cur_color = new Color( (int)(color[0]*255), 
                                 (int)(color[1]*255), 
                                 (int)(color[2]*255) );
    return cur_color;
  }

}

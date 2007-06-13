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
 * $Log: SimpleShape.java,v $
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
 *  possible material properties.
 */
abstract public class SimpleShape extends Node
{
  protected float color[] = new float[3];

  /** 
   *  Construct a 3D object of the given color. 
   *
   *  @param  new_color  The color to be used for this object.
   */
  public SimpleShape( Color new_color )
  {
    setColor( new_color );
  }

  /**
   *  Set the color to be used when this object is redrawn.
   *
   *  @param new_color Specifies the color to be used when drawing this object.
   */
  public void setColor( Color new_color )
  {
    color[0] = new_color.getRed()/255f;
    color[1] = new_color.getGreen()/255f;
    color[2] = new_color.getBlue()/255f;
  }


  /**
   *  Get the color used when this object is redrawn.
   *
   *  @return The color used when drawing this object.
   */
  public Color getColor()
  {
    Color cur_color = new Color( color[0]*255, color[0]*255, color[0]*255 );
    return cur_color;
  }

}

/*
 * File:  PixelBox.java
 *
 * Copyright (C) 2005, Chad Jones
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
 * Primary   Chad Jones <cjones@cs.utk.edu>
 * Contact:  Student Developer, University of Tennessee
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * Modified:
 *
 * $Log: PixelBox.java,v $
 * Revision 1.3  2006/07/04 00:35:55  dennis
 * Removed unused imports.
 *
 * Revision 1.2  2005/07/25 21:24:32  cjones
 * PixelBox can now hold the value of the pixel.
 *
 * Revision 1.1  2005/07/19 16:01:34  cjones
 * Added group and shape for Detectors and Pixels that have user IDs.
 *
 *
 */

package SSG_Tools.SSG_Nodes.Shapes;


/** 
 *  This class draws a solid box of the specified width, height and depth
 *  centered at the origin. It also holds an associated user defined integer
 *  id and float value for the pixel.
 */

public class PixelBox extends SolidBox
{
  private int PixelID; 
  private float Value = 0;

  /* -------------------------- constructor ------------------------ */
  /**
   *  Construct a box that represents a pixel.
   *
   *  @param id User specified integer to associated with pixel.
   *  @param  width    The total width  of the box in the "x" direction.
   *  @param  height   The total height of the box in the "y" direction.
   *  @param  depth    The total depth  of the box in the "z" direction.
   */
  public PixelBox(int id, float width, float height, float depth)
  {
    super(width, height, depth);
    PixelID = id;
  }

  /**
   * Return pixel's id.
   *
   *    @return Pixel id
   */
  public int getPixelID()
  {
    return PixelID;
  }
  
  /**
   * Set the current value of the pixel box.
   * 
   * @param value Value of box.
   */
  public void setValue(float value)
  {
    Value = value;
  }
  
  /**
   * Return pixel's value.
   *
   * @return value of box
   */
  public float getValue()
  {
    return Value;
  }
}

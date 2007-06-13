/*
 * File:  Light.java
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
 * $Log: Light.java,v $
 * Revision 1.2  2004/12/13 05:02:27  dennis
 * Minor fix to documentation
 *
 * Revision 1.1  2004/10/25 21:59:54  dennis
 * Added to SSG_Tools CVS repository
 *
 */

package SSG_Tools.SSG_Nodes.Lights;

import java.awt.*;
import net.java.games.jogl.*;

import SSG_Tools.SSG_Nodes.*;

/**
 *  This class is an abstract base class for nodes of a scene graph that
 *  represent lights.  The color of the light is used for both the specular 
 *  and diffuse color for the light.  By default, the ambient color is not 
 *  used, unless a value has been set for the how much the light contributes
 *  to the ambient light using the setAmbientFactor() method.
 */

abstract public class Light extends Node
{
  public static final int MIN_LIGHT_NUM = GL.GL_LIGHT1;
  public static final int MAX_LIGHT_NUM = GL.GL_LIGHT7;

  protected int     light_num;
  private   float   ambient_factor = 0;
  private   Color   color;
  protected boolean on;

  /* -------------------------- constructor ------------------------ */
  /**
   *  Construct a new light node, using the specified OpenGL light and
   *  color.  
   *
   *  @param  light_num  The number from GL_LIGHT1 to GL_LIGHT7 to use for 
   *                     this light.  The light number is clamped to be between
   *                     GL_LIGHT1 and GL_LIGHT7.  If a value smaller than
   *                     GL_LIGHT1 is passed in, GL_LIGHT1 will be used.  If
   *                     a value larger than GL_LIGHT7 is passed in, GL_LIGHT7
   *                     will be used.  Applications should use the symbolic
   *                     names GL.GL_LIGHT1 through GL.GL_LIGHT7, since these
   *                     values are NOT typically mapped to 1..7.
   *
   *  @param  color      The color to use for this light.
   */
  public Light( int light_num, Color color )
  {
    if ( light_num <= GL.GL_LIGHT1 )
      light_num = GL.GL_LIGHT1;
 
    if ( light_num >= GL.GL_LIGHT7 )
      light_num = GL.GL_LIGHT7;

    this.light_num = light_num;
    this.color = color;
    on = true;
  }

  /* -------------------------- constructor ------------------------ */
  /**
   *  Construct a new light node, using the specified OpenGL light and
   *  a default color of GRAY.
   *
   *  @param  light_num  The number from GL_LIGHT1 to GL_LIGHT7 to use for 
   *                     this light.
   */
  public Light( int light_num )
  {
    this( light_num, Color.GRAY );
  }

  /* -------------------------- setEnable ---------------------------- */
  /**
   *  Set flag indicating whether or not this light is turned on.
   *
   *  @param  on_off  flag indicating whether to turn this ligth on or off.
   */
  public void setEnable( boolean on_off )
  {
    on = on_off;
  }

  /* --------------------------- setColor ---------------------------- */
  /**
   *  Set the color of this light.  If color is null this method has
   *  no effect.
   *
   *  @param color  The new color to use for this light.
   */
  public void setColor( Color color )
  {
    if ( color != null )
      this.color = color;
  }

  /* ------------------------- setAmbientFactor ----------------------- */
  /**
   *  Set the fraction of this light that will be used for ambient 
   *  light calculations.  If the fraction is zero, the ambient 
   *  components of this light will be set to zero so there is no 
   *  contribution to ambient light.  The fraction used is clamped between
   *  0 and 1. 
   *
   *  @param factor  The fraction of this light (between 0 and 1) that
   *                  will be used for the ambient light.
   */
  public void setAmbientFactor( float factor )
  {
    if ( factor < 0 )
      factor = 0;
    else if ( factor > 1 )
      factor = 1;

    this.ambient_factor = factor;
  }


  /* ---------------------------- Render ------------------------------ */
  /**
   *  Use OpenGL calls to use the specified light, if it is enabled.
   *
   *  @param  drawable  The drawable on which the light is to be used.
   */
  public void Render( GLDrawable drawable )
  {
    GL gl = drawable.getGL();
    gl.glEnable( light_num );
                                                // use same color for diffuse 
    float color_arr[] = new float[4];           // and specular components
    color_arr[0] = color.getRed()/255f;
    color_arr[1] = color.getGreen()/255f;
    color_arr[2] = color.getBlue()/255f;
    color_arr[3] = 1;
    gl.glLightfv( light_num, GL.GL_SPECULAR, color_arr );
    gl.glLightfv( light_num, GL.GL_DIFFUSE, color_arr );

                                                // use a fraction of the color
    for ( int i = 0; i < 3; i++ )               // for the ambient light
      color_arr[i] *= ambient_factor;
    gl.glLightfv( light_num, GL.GL_AMBIENT, color_arr );
  }

}

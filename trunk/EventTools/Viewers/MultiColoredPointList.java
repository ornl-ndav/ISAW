/* 
 * File: MultiColoredPointList.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.Viewers;

import java.awt.*;
import javax.media.opengl.*;

import SSG_Tools.SSG_Nodes.SimpleShapes.*;

/**
 *  This class represents a list of colored points of a specified
 *  size, drawn using GL_POINTS.
 */

public class MultiColoredPointList extends SimpleShape 
{
  private float[]  x = null;
  private float[]  y = null;
  private float[]  z = null;
  private float    size  = 1;
  private int[]    index = null;
  private float[]  red;
  private float[]  green;
  private float[]  blue;

  private float    alpha;


  /* --------------------------- Constructor --------------------------- */
  /**
   *  Construct a PointList from the specified arrays.
   *
   *  @param  x_vals  The list of x-coordinates. 
   *  @param  y_vals  The list of y-coordinates. 
   *  @param  z_vals  The list of z-coordinates. 
   *  @param  size    The size to use for the points specified in pixel 
   *                  units.
   *  @param  new_color The color of the LineStrip.
   *
   */
  public MultiColoredPointList( float[]  x_vals,
                                float[]  y_vals,
                                float[]  z_vals,
                                int[]    color_index,
                                Color[]  color_scale, 
                                float    size,
                                float    alpha  )
  {
    super(Color.WHITE);
    this.alpha = alpha;
    x = x_vals;
    y = y_vals;
    z = z_vals;
    index = color_index;

    this.size = size;

    red   = new float[color_scale.length];
    green = new float[color_scale.length];
    blue  = new float[color_scale.length];
    for ( int i = 0; i < color_scale.length; i++ )
    {
       red[i]   = color_scale[i].getRed()/255.0f;
       green[i] = color_scale[i].getGreen()/255.0f;
       blue[i]  = color_scale[i].getBlue()/255.0f;
    }
  }

 
  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this LineStrip to the specified drawable.  
   *
   *  @param  drawable  The drawable on which the LineStrip is to be drawn.
   */
  public void Render( GLAutoDrawable drawable )
  {
    GL gl = drawable.getGL();

    super.preRender( drawable );

    gl.glDisable( GL.GL_LIGHTING ); 
    gl.glEnable( GL.GL_BLEND );
    gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
//    gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE );

    int pos;
    gl.glPointSize( size );
    gl.glBegin( GL.GL_POINTS );
    for ( int i = 0; i < x.length; i++ )
    {
      pos = index[i]; 
      gl.glColor4f( red[pos], green[pos], blue[pos], alpha );
      gl.glVertex3f( x[i], y[i], z[i] );
    }
    gl.glEnd();

//    gl.glDisable( GL.GL_BLEND );
    super.postRender( drawable );
  }
}

/*
 * File:  TransparentMaterial.java
 *
 * Copyright (C) 2006, Dennis Mikkelson
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
 *  Last Modified:
 * 
 *  $Author: eu7 $
 *  $Date: 2008-08-21 20:32:14 -0500 (Thu, 21 Aug 2008) $            
 *  $Revision: 314 $
 *           
 *  $Log: TransparentMaterial.java,v $
 *
 *  Revision 1.3  2007/08/24 20:55:08  dennis
 *  Added default constructor.
 *
 *  Revision 1.2  2007/08/24 20:24:31  dennis
 *  Fixed typo in parameter name.
 *
 *  Revision 1.1  2007/08/24 20:19:45  dennis
 *  First pass at implementing transparency in SSG_Tools.
 *
 */
package SSG_Tools.Appearance;

import java.awt.*;
import javax.media.opengl.*;

/**
 *  This class extends the Material class to allow representing transparent
 *  objects.  It adds a method setAlpha(), which must be called to set the
 *  alpha value to the desired value between 0 and 1.  The default value for
 *  alpha is 0.5.  Besides using this TransparentMaterial for the Material 
 *  of a shape, the application must also include a glEnableNode to enable
 *  GL.GL_BLEND, before this node and the shape in the scene graph.  For 
 *  efficiency, the application should also include a glDisableNode to disable
 *  GL.GL_BLEND, after the shape is drawn in the scene graph.  This node
 *  only uses the diffuse and specular colors.
 *  NOTE: Transparent objects must be drawn last, so they should be the last
 *  nodes in a scene graph.
 */
public class TransparentMaterial extends Material 
{
  private float alpha = 0.5f;

  /* ---------------------- default constructor --------------------- */
  /**
   *  Construct a new gray material, with alpha = 0.5 by default.
   */
  public TransparentMaterial()
  {
    super( Color.GRAY );
  }


  /* -------------------------- constructor ------------------------ */
  /**
   *  Construct a new transparent material object, using the specified
   *  color for the ambient and diffuse colors, WHITE for the specular 
   *  color, and the specified alpha value to control the transparency.
   *  If the alpha value is not in the interval [0,1] then the default
   *  alpha value of 0.5 will be used.
   *
   *  @param  color  the color for this object
   *  @param  alpha  The alpha value to use for this object.  Values outside
   *                 of the interval [0,1] are ignored.
   */
  public TransparentMaterial( Color color, float alpha )
  {
    super( color );
    if ( alpha >= 0 && alpha <= 1 )
      this.alpha = alpha;
  }


  /* ------------------------- setAlpha --------------------------- */
  /**
   *  Set the alpha value to a value between 0 (invisible) and 1 (opaque).
   *  The default alpha value is 0.5, which means that this surface will
   *  be blended 50-50 with objects that have previously been drawn behind
   *  it.
   *
   *  @param alpha  The alpha value to use for this object.  Values outside
   *                of the interval [0,1] are ignored.
   */
  public void setAlpha( float alpha )
  {
    if ( alpha >= 0 && alpha <= 1 )
      this.alpha = alpha;
  }


  /* ------------------------- getAlpha --------------------------- */
  /**
   *  Set the current alpha value for this object.
   *
   *  @return the currently set alpha value.
   */
  public float getAlpha()
  {
    return alpha;
  }


  /* ---------------------------- Render ------------------------------ */
  /**
   *  Use OpenGL calls to set the material properties.
   *
   *  @param  drawable  The drawable for which the material properties are set. 
   */
  public void Render( GLAutoDrawable drawable )
  {
    GL gl = drawable.getGL();

    float mat[] = new float[4];
    mat[3] = alpha; 
                              // NOTE: The JoglPanel enables ColorMaterial with
                              //       the material diffuse color tracking
                              //       the current color set by glColor4f()
                              //       so here we can just call glColor4f()
    Color diffuse = getDiffuse();
    if ( diffuse != null )
    {
      mat[0] = diffuse.getRed()/255f;
      mat[1] = diffuse.getGreen()/255f;
      mat[2] = diffuse.getBlue()/255f;
      gl.glColor4fv( mat, 0 ); 
    }

    Color specular = getSpecular();
    if ( specular != null )
    {
      mat[0] = specular.getRed()/255f;
      mat[1] = specular.getGreen()/255f;
      mat[2] = specular.getBlue()/255f;
      gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, mat, 0 );
    }

    float shininess = getShininess();
    gl.glMaterialf( GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess );
  }
  
}

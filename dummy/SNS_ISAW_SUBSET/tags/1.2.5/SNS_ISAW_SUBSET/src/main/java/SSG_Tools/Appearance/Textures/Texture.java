/*
 * File:  Texture.java
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2007/08/14 00:03:27  dennis
 * Major update to JSR231 based version from UW-Stout repository.
 *
 * Revision 1.6  2006/12/11 19:04:13  dennis
 * Fixed spelling in javadoc comment
 *
 * Revision 1.5  2006/12/11 17:57:18  dennis
 * Fixed error in javadoc comment.
 *
 * Revision 1.4  2006/12/10 06:10:14  dennis
 * Added methods to get and set the min and mag filters separately.
 * This was needed since we now build MipMaps automatically for the
 * textures, and so the additional MIPMAP options had to be made
 * available for the min filter.
 * Also, the GLAutoDrawable passed in must now be either an SSG_Drawable
 * or an SSG_JPanel, so that it implements the IGetGLU interface.  This
 * will always be the case when a scene graph is used from a JoglPanel.
 *
 * Revision 1.3  2006/08/04 02:16:21  dennis
 * Updated to work with JSR-231, 1.0 beta 5,
 * instead of jogl 1.1.1.
 *
 * Revision 1.2  2005/11/01 20:31:18  dennis
 * Added more explanatory javadoc comments at the start of the class.
 * Made the methods setImage() and getTexture_name() protected, since
 * they should only be used by derived classes.  Added javadoc comments
 * suggesting that the setMode() and setFilter() methods should not
 * be used after the texture has been rendered.
 *
 * Revision 1.1  2004/11/15 18:54:19  dennis
 * Initial version.
 */

package SSG_Tools.Appearance.Textures; 

import javax.media.opengl.*;


/**
 *  This class is the abstract base class for texture objects.  It handles
 *  those parts of texture mapping that are the same for 1, 2 and 3D textures.
 *  To use texture mapping in the SSG_Tools system, a concrete texture object
 *  must be constructed and set into an appearance node.  In addition, the
 *  Shape to be textured must also define texture coordinates, or have 
 *  automatic texture coordinate generation enabled.   See Shape.java.  
 *  To improve efficiency, methods that change the texture map or the
 *  way the texture map is interpreted (setMode(), setMinFilter(), 
 *  setMagFilter()) are included.  These should generally be called 
 *  immediately after constructing the texture object, before it is rendered.
 *  NOTE: MipMaps will be automatically generated for the texture objects.
 */

abstract public class Texture 
{
  public static int DEFAULT_TEXTURE_NAME = 0;
  private int texture_name = DEFAULT_TEXTURE_NAME;

  private  byte image[] = null;
  private  float mag_filter = GL.GL_LINEAR;
  private  float min_filter = GL.GL_LINEAR;
  private  float mode       = GL.GL_MODULATE;

  protected boolean rebuild_texture = true;


 /* --------------------------- mustRebuild ----------------------------- */
 /**
  *  Check whether or not the texture was changed and must be rebuilt
  *  if it was used in a display list.
  *
  *  @return true if the texture was changed.
  */
  public boolean mustRebuild()
  {
    return rebuild_texture;
  }


  /* ---------------------------- activate ---------------------------- */
  /**
   *  Activate this texture object, so that texture colors will be obtained
   *  from this texture object.  This method is called by an appearance 
   *  object, as the scene graph is being rendered.  Applications should
   *  not call this directly.
   *
   *  @param drawable  The OpenGL drawable for which the texture object 
   *                   is used.
   */
  abstract public void activate( GLAutoDrawable drawable );


  /* ---------------------------- deactivate ---------------------------- */
  /**
   *  Deactivate this texture object, so that texture colors will no longer
   *  be obtained from this texture object.  This method is called by an 
   *  appearance object, as the scene graph is being rendered.  Applications
   *  should not call this directly.
   *
   *  @param drawable  The OpenGL drawable for which the texture object 
   *                   is used.
   */
  abstract public void deactivate( GLAutoDrawable drawable );


  /* ---------------------------- setImage ---------------------------- */
  /**
   * Set the texture image that will be used for this texture.  This method
   * is only intended to be used by the constructors of concrete derived
   * classes. 
   *
   * @param rgb_array  Array of rgb byte triples that list the colors for
   *                   the texture.   The texels are grouped with the
   *                   the first 3 bytes represent the RGB values for the
   *                   first texel, etc.
   * @param n_texels   The number of rgb byte triples to use to form the
   *                   texture.  
   */
  protected void setImage( byte[] rgb_array, int n_texels )
  {
    if ( rgb_array.length < 3 * n_texels )
    {
      System.out.println("ERROR not enough texels in rgb_array " + 
                         rgb_array.length + " not " + 3*n_texels );
      return;
    }

    if ( image == null || image.length != rgb_array.length )
      image = new byte[ 3 * n_texels ];

    System.arraycopy( rgb_array, 0, image, 0, 3 * n_texels );
    rebuild_texture = true;
  }


  /* ---------------------------- getImage -------------------------- */
  /**
   * Get a reference to the array of texels used for the current texture.
   * This is only intended to be used by derived classes, when they
   * implement the abstract methods activate() and deactivate().
   *
   * @return A reference to the array of bytes used for this texture
   */
  protected byte[] getImage()
  {
    return image;
  }


  /* ------------------------ getTexture_name ----------------------- */
  /**
   * Get the integer "texture name" for this texture object.  This is only
   * intended to be used by derived classes.
   *
   * @return The integer code for the OpenGL texture object,
   *         as obtained from glGenTextures.
   */
  protected int getTexture_name( GL gl )
  {
    if ( texture_name == DEFAULT_TEXTURE_NAME )
    {
      int list[] = new int[1];
      gl.glGenTextures( 1, list, 0 );
      texture_name = list[0];
    }
    return texture_name;
  }


  /* -------------------------- setFilter ---------------------------- */
  /**
   *  Set the filter type to be used for both image minification and
   *  image magnification.   This should not be called after the texture
   *  map has been rendered.
   *
   * @param filter_code  One of the values GL.GL_LINEAR or GL.GL_NEAREST
   */
  public void setFilter( float filter_code )
  {
    min_filter = filter_code;
    mag_filter = filter_code;
    rebuild_texture = true;
  }


  /* -------------------------- setMinFilter -------------------------- */
  /**
   *  Set the filter type to be used for image minification, when one
   *  on-screen pixel being rendered maps to an area greater than one
   *  texture element. This should generally not be called after the 
   *  texture map has been rendered.
   *
   * @param filter_code  One of the values GL.GL_LINEAR, GL.GL_NEAREST
   *                     GL_NEAREST_MIPMAP_NEAREST, GL_LINEAR_MIPMAP_NEAREST,
   *                     GL_NEAREST_MIPMAP_LINEAR or GL_LINEAR_MIPMAP_LINEAR
   */
  public void setMinFilter( float filter_code )
  {
    min_filter = filter_code;
    rebuild_texture = true;
  }


  /* -------------------------- setMagFilter -------------------------- */
  /**
   *  Set the filter type to be used for image magnification, when one
   *  on-screen pixel being rendered maps to an area less than or equal
   *  to one texture element. This should generally not be called after 
   *  the texture map has been rendered.
   *
   * @param filter_code  One of the values GL.GL_LINEAR or GL.GL_NEAREST
   */
  public void setMagFilter( float filter_code )
  {
    mag_filter = filter_code;
    rebuild_texture = true;
  }


  /* -------------------------- getMinFilter --------------------------- */
  /**
   *  Get the minification filter type that has been specified for 
   *  this texture.
   *
   *  @return The code for the current minification texture filter, 
   *          GL.GL_LINEAR, GL.GL_NEAREST, GL_NEAREST_MIPMAP_NEAREST, 
   *          GL_LINEAR_MIPMAP_NEAREST, GL_NEAREST_MIPMAP_LINEAR or 
   *          GL_LINEAR_MIPMAP_LINEAR
   */
  public float getMinFilter() 
  {
    return min_filter;
  }


  /* -------------------------- getMagFilter --------------------------- */
  /**
   *  Get the magnification filter type that has been specified for
   *  this texture.
   *
   *  @return The code for the current magnification texture filter,
   *          GL.GL_LINEAR or GL.GL_NEAREST.
   */
  public float getMagFilter()
  {
    return mag_filter;
  }


  /* -------------------------- setMode ---------------------------- */
  /**
   * Set the mode that controls how this texture is applied to
   * the shape.  This is typically GL.GL_MODULATE or GL.GL_DECAL
   * This should not be called after the texture map has been rendered.
   *
   * @param mode_code  The GL constant that controls the mode,
   *                   such as GL.GL_MODULATE or GL.GL_DECAL.
   */
  public void setMode( float mode_code )
  {
    mode = mode_code;
    rebuild_texture = true;
  }


  /* -------------------------- getMode ---------------------------- */
  /**
   * Get the current mode code for this texture object.
   *
   * @return The code for the current texture mode, such as
   *         GL.GL_MODULATE, GL.GL_DECAL
   */
  public float getMode() 
  {
    return mode;
  }

}

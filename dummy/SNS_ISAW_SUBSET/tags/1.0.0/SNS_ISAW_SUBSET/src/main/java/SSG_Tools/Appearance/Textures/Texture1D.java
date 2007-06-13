/*
 * File:  Texture1D.java
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
 * $Log: Texture1D.java,v $
 * Revision 1.2  2006/07/20 18:14:22  dennis
 * Updated from CVS repository on isaw.mscs.uwstout.edu
 * Basically, improved documentation.
 *
 * Revision 1.2  2005/11/01 20:52:33  dennis
 * Removed setImage() method.  The image and texture properties should
 * be set at construction time.
 * Added more explanatory javadocs at the start of the class, and
 * slightly expanded other javadocs.
 *
 * Revision 1.1  2004/11/15 18:54:19  dennis
 * Initial version.
 *
 */

package SSG_Tools.Appearance.Textures;

import net.java.games.jogl.*;


/**
 *  This class creates a 1-dimensional OpenGL Texture object.  After creating
 *  a Texture1D object, the texture object should be passed to the Appearance
 *  node of a Shape.   The properties of this texture object can be set at 
 *  the time the object is constructed, but should not be changed after 
 *  the texture object has been used.  
 */

public class Texture1D extends Texture
{
  private int n_cols = 0;
  private float wrap_s = GL.GL_REPEAT;


  /* --------------------------- constructor -------------------------- */
  /**
   *  Construct a 1D texture object, using the specified array of colors
   *  and the specified number of colors. 
   *
   * @param rgb_array  Array of rgb byte triples that list the colors for
   *                   the texture.
   * @param n_texels   The number of rgb byte triples to use to form the
   *                   texture.
   */
  public Texture1D( byte rgb_array[], int n_texels )
  {
    validateImage( rgb_array, n_texels );
    rebuild_texture = true;
  }


  /* ---------------------------- validateImage ------------------------ */
  /**
   * Validate and set the texture image that will be used for this texture.
   *
   * @param rgb_array  Array of rgb byte triples that list the colors for
   *                   the texture.
   * @param n_texels   The number of rgb byte triples to use to form the
   *                   texture.
   */
  private void validateImage( byte rgb_array[], int n_texels )
  {
    if ( rgb_array == null )
    {
       System.out.println("ERROR: null rgb_array in Texture2D.setImage ");
       return;
    }
    if ( rgb_array.length < 3 * n_texels )
    {
       System.out.println("ERROR: not enough colors in Texture2D.setImage "+
                           rgb_array.length );
       return;
    }
    this.n_cols = n_texels;

    super.setImage( rgb_array, n_cols );
    rebuild_texture = true;
  }


  /* ---------------------------- activate ---------------------------- */
  /**
   *  Activate this texture object, so that texture colors will be obtained
   *  from this texture object.  This method is called by an appearance 
   *  object, as the scene graph is being rendered.  Applications should
   *  not call this directly.
   *
   *  @param gl   The OpenGL context for which the texture object is used.
   */
  public void activate( GL gl )
  {                                      // always do the things that get 
                                         // stored in the display list
    gl.glEnable( GL.GL_TEXTURE_1D );
    int tex_name = getTexture_name( gl );
    gl.glBindTexture( GL.GL_TEXTURE_1D, tex_name );
    gl.glTexEnvf( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, getMode() );

    if ( rebuild_texture && getImage() != null )     // do the things that are
    {                                                // stored in the current
                                                     // 2D texture object
      gl.glPixelStorei( GL.GL_UNPACK_ALIGNMENT, 1 );
      gl.glTexImage1D( GL.GL_TEXTURE_1D, 0, 3,
                       n_cols, 
                       0, 
                       GL.GL_RGB,
                       GL.GL_UNSIGNED_BYTE,
                       getImage() );

      gl.glTexParameterf( GL.GL_TEXTURE_1D, GL.GL_TEXTURE_WRAP_S, wrap_s );
      gl.glTexParameterf( GL.GL_TEXTURE_1D, GL.GL_TEXTURE_MAG_FILTER, 
                          getFilter() );
      gl.glTexParameterf( GL.GL_TEXTURE_1D, GL.GL_TEXTURE_MIN_FILTER, 
                          getFilter() );
      rebuild_texture = false;
    }
  }


  /* ---------------------------- deactivate ---------------------------- */
  /**
   *  Deactivate this texture object, so that texture colors will no longer
   *  be obtained from this texture object.  This method is called by an 
   *  appearance object, as the scene graph is being rendered.  Applications
   *  should not call this directly.
   *
   *  @param gl   The OpenGL context for which the texture object is used.
   */
  public void deactivate( GL gl )
  {
     gl.glBindTexture( GL.GL_TEXTURE_1D, 0 );
     gl.glDisable( GL.GL_TEXTURE_1D );
  }


  /* ---------------------------- setWrap_s--------------------------- */
  /**
   *  Set the wrap mode for the s coordinate to one of GL.GL_REPEAT or
   *  GL.GL_CLAMP.  This method should not be called after the 
   *  texture object has been rendered.
   *
   * @param wrap_code 
   */
  public void setWrap_s( float wrap_code )
  {
    wrap_s = wrap_code;
    rebuild_texture = true;
  }


  /* ---------------------------- getWrap_s--------------------------- */
  /**
   * Get the wrap mode for the s coordinate.
   *
   * @return The current wrap mode for the first texture parameter, s,
   *         as GL.GL_REPEAT or GL.GL_CLAMP
   */
  public float getWrap_s()
  {
    return wrap_s;
  }

}

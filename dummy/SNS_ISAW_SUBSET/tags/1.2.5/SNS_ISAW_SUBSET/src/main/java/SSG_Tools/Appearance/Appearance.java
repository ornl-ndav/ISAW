/*
 * File:  Appearance.java
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
 *  Last Modified:
 * 
 *  $Author: eu7 $
 *  $Date: 2009-02-02 09:59:11 -0600 (Mon, 02 Feb 2009) $            
 *  $Revision: 316 $
 *           
 *  $Log: TransparentMaterial.java,v $
 *
 *  get/setMaterial object no longer makes a copy of the object, but 
 *  returns or stores a reference to the material object.  This was 
 *  more convenient when dealing with TransparentMaterial objects, since
 *  juat making a copy of a Material object lost the transparency.  
 *           
 *  Revision 1.7  2007/08/14 00:03:26  dennis
 *  Major update to JSR231 based version from UW-Stout repository.
 *
 *  Revision 1.10  2006/12/10 06:16:27  dennis
 *  Updated to use new interface for Texture objects.  That is, now
 *  pass in a GLAutoDrawable object rather than just the GL object.
 *
 *  Revision 1.9  2006/11/27 02:14:20  dennis
 *  Now pushes the polygon attributes as well as the lighting
 *  attributes.  Also, now pops the attribute stack in postrender
 *  precisely when the attribute stack was pushed in prerender,
 *  without re-checking a logical condition for pushing the stack.
 *
 *  Revision 1.8  2006/10/10 02:49:26  dennis
 *  Fixed bug with setPolygonMode(), polygon mode was not being set
 *  properly.
 *
 *  Revision 1.7  2006/08/04 02:16:21  dennis
 *  Updated to work with JSR-231, 1.0 beta 5,
 *  instead of jogl 1.1.1.
 *
 *  Revision 1.6  2005/10/14 03:46:47  dennis
 *  Updated from current version kept in CVS at IPNS.
 *
 *  Revision 1.6  2005/07/25 14:01:16  dennis
 *  If a material object has been set for this appearance, the current
 *  lighting/material state is now pushed on the attribute stack in the
 *  preRender() method, and the attribute stack is popped in the method
 *  postRender().
 *
 *  Revision 1.5  2004/12/13 05:02:27  dennis
 *  Minor fix to documentation
 *
 *  Revision 1.4  2004/11/22 18:48:12  dennis
 *  Removed redundant else clause.
 *  Documented empty body of default constructor.
 *
 *  Revision 1.3  2004/11/15 17:59:24  dennis
 *    Appearance no longer implements IGL_Renderable().
 *  Replaced the Render() method with preRender(), that is done before
 *  a Shape is rendered, and postRender(), that is done after rendering.
 *    Added methods to get/set the polygon mode, so that polygons can be easily
 *  rendered as POINTS, LINES or FILLed.
 *    Now supports textures as part of the appearance of an object.  Added
 *  methods to get/set the texture object.
 *
 *  Revision 1.2  2004/10/25 21:59:13  dennis
 *  Added to SSG_Tools CVS repository
 *
 */

package SSG_Tools.Appearance;

import javax.media.opengl.*;
import SSG_Tools.Appearance.Textures.*;

public class Appearance 
{
  private Material material     = null; 
  private Texture  texture      = null;
  private int      shade_model  = GL.GL_NONE;
  private int      polygon_mode = GL.GL_NONE;
  private boolean  pushed_stack = false;


  /* -------------------------- constructor ------------------------ */
  /**
   *  Construct a new empty appearance object.
   */
  public Appearance()
  { 
  	// default constructor.
  }


  /* -------------------------- constructor ------------------------ */
  /**
   *  Construct a new appearance object with the material specified.
   *
   *  @param material  the material to use for this appearance.
   */
  public Appearance( Material material )
  {
    this.material = material;
  }


  /* ------------------------- copy constructor ---------------------- */
  /**
   *  Construct a new Appearance object containing the same data as the
   *  specified Appearance object.
   */
  public Appearance( Appearance appearance )
  {
    material = appearance.getMaterial();    
    texture  = appearance.getTexture();
    this.shade_model  = appearance.getShadeModel();
    this.polygon_mode = appearance.getPolygonMode();
  }


  /* -------------------------- setMaterial -------------------------- */
  /**
   *  Record a reference to the specified material object, in this Appearance
   *  object.
   *
   *  @param new_material  The new material to use for this Appearance object.
   */
  public void setMaterial( Material new_material )
  {
    this.material = new_material;
  }


  /* ------------------------- getMaterial ---------------------------- */
  /**
   *  Get a reference to the current Material object for this Appearance.
   *
   *  @return  a reference to the Material object, or null if none has been 
   *           set.
   */
  public Material getMaterial()
  {
    return material;
  }


  /* -------------------------- setTexture -------------------------- */
  /**
   *  Record a reference to the specified texture object, in this Appearance
   *  object.
   *
   *  @param texture  The new texture to use for this Appearance object.
   */
  public void setTexture( Texture texture )
  {
    this.texture = texture;
  }


  /* ------------------------- getTexture ---------------------------- */
  /**
   *  Get a reference to the current texture object for this Appearance.
   *
   *  @return  a reference to the Texture object, or null if none has been 
   *           set.
   */
  public Texture getTexture()
  {
    return texture;
  }


  /* -------------------------- setShadeModel -------------------------- */
  /**
   *  Set the shade model to be GL_SMOOTH, GL_FLAT or GL_NONE to just use the
   *  shade model currently set in OpenGL.  Invalid values are ignored.
   *
   *  @param shade_model  Specify the shade model to use for this appearance
   *                      as GL.GL_SMOOTH or GL.GL_FLAT, GL.GL_NONE. 
   */
  public void setShadeModel( int shade_model )
  {
    if ( shade_model == GL.GL_SMOOTH || 
         shade_model == GL.GL_FLAT   ||
         shade_model == GL.GL_NONE   )
      this.shade_model = shade_model;
  }


  /* ------------------------- getShadeModel --------------------------- */
  /**
   *  Get a the current shade model set for this appearance object.
   *
   *  @return  one of GL.GL_SMOOTH, GL.GL_FLAT or GL.GL_NONE.
   */
  public int getShadeModel()
  {
    return shade_model;
  }


  /* ------------------------ setPolygonMode -------------------------- */
  /**
   *  Set the mode for surfaces to be GL_POINT, GL_LINE, GL_FILL or GL_NONE.
   *  If GL_NONE is specified, the polygon mode will NOT be changed from
   *  the previous polygon mode.
   *
   *  @param polygon_mode  Specify the polygon_mode to use when drawing 
   *                       as GL.GL_POINT or GL.GL_LINE, GL.GL_FILL or
   *                       GL.GL_NONE. 
   */
  public void setPolygonMode( int polygon_mode )
  {
    if ( polygon_mode == GL.GL_POINT ||
         polygon_mode == GL.GL_LINE  ||
         polygon_mode == GL.GL_FILL  ||
         polygon_mode == GL.GL_NONE   )
      this.polygon_mode = polygon_mode;
  }


  /* ------------------------- getPolygonMode --------------------------- */
  /**
   *  Get the current current polygon mode for this appearance object.
   *
   *  @return  one of GL.GL_POINT, GL.GL_LINE, GL.GL_FILL, or GL.GL_NONE.
   */
  public int getPolygonMode()
  {
    return polygon_mode;
  }


  /* ---------------------------- preRender ------------------------------ */
  /**
   *  Use OpenGL calls to set up GL to use the different parts of this 
   *  appearance object. 
   *
   *  @param  drawable  The drawable for which the material properties are set. 
   */
  public void preRender( GLAutoDrawable drawable )
  {
    GL gl = drawable.getGL();

    if ( shade_model  != GL.GL_NONE ||     // if we are going to change one
         polygon_mode != GL.GL_NONE ||     // of these, push/pop the attribute
         material     != null          )   // stack to isolate the changes
    {
      gl.glPushAttrib( GL.GL_LIGHTING_BIT | GL.GL_POLYGON_BIT );
      pushed_stack = true;
    }
    else
      pushed_stack = false;

    if ( shade_model != GL.GL_NONE )
      gl.glShadeModel( shade_model );

    if ( polygon_mode != GL.GL_NONE )
      gl.glPolygonMode( GL.GL_FRONT_AND_BACK, polygon_mode );

    if ( material != null )
      material.Render( drawable );

    if ( texture != null )
      texture.activate( drawable );
  }


  /* ---------------------------- postRender ----------------------------- */
  /**
   *  Use OpenGL calls to set up GL to reset the texture state 
   *
   *  @param  drawable  The drawable for which the material properties are set. 
   */
  public void postRender( GLAutoDrawable drawable )
  {
    if ( texture != null )
      texture.deactivate( drawable );

    if ( pushed_stack )
    {
      GL gl = drawable.getGL();
      gl.glPopAttrib();
    }
  }
  
}

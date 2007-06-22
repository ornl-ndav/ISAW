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
 * $Log: Shape.java,v $
 * Revision 1.6  2006/07/20 15:44:52  dennis
 * Updated from CVS repository at isaw.mscs.uwstout.edu.
 * Now allows user to specify GL_EYE_LINEAR for texture
 * coordinate generation.
 * Expanded javadocs.
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
 *   Added methods setTexCoordGenMode() and setTexPlane() to control how 
 * texture coordinates are actually generated.
 *
 */

package SSG_Tools.SSG_Nodes.Shapes;

import net.java.games.jogl.*;

import gov.anl.ipns.MathTools.Geometry.*;

import SSG_Tools.SSG_Nodes.*;
import SSG_Tools.Appearance.*;

/** 
 *  This is the abstract base class for scene graph nodes that represent
 *  actual objects that are drawn.  Material properties and texture mapping
 *  is also supported by objects derived form this class.  Derived classes 
 *  define their own geometry, and must implement the Render() method to 
 *  to actually draw the object.  This base class provides a preRender() and
 *  postRender() method that takes care of things that are common to all 
 *  shapes.  In particular, the preRender() and postRender() MUST be called
 *  at the start and at the end, respectively, of the Render() method 
 *  of derived classes
 */

abstract public class Shape extends Node 
{
  private Appearance appearance = null;
  private int        tex_coord_mode = GL.GL_NONE;
  private Vector3D   center = new Vector3D( 0, 0, 0 );
  private Vector3D   s_vec = new Vector3D( 1, 0, 0 );
  private Vector3D   t_vec = new Vector3D( 0, 1, 0 );
  private float      s_shift = 0;
  private float      t_shift = 0;
 

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

  /* ----------------------- setTexCoordGenMode ------------------------- */
  /**
   *  Specify how texture coordinates are to be generated,
   *  IF they are to be generated by OpenGL.  If the requested mode is
   *  GL.GL_SPHERE_MAP, the texture coordinates will be generated for 
   *  environmental mapping.  If the requested mode is GL.GL_OBJECT_LINEAR,
   *  the texture coordinates will be generated by projecting the object
   *  onto the s,t plane.   If the requested mode is GL.GL_EYE_LINEAR,
   *  this will occur in the eye coordinate system, rather than in the
   *  modeling coordinate system.  If the requested mode is GL_NONE then 
   *  automatic texture coordinate generation will be turned off for this 
   *  node.  If the requested mode is GL_NOOP, then the texture generation
   *  mode will not be set by this node.  NOTE: if object or eye linear
   *  are set, valid values must also be set for the vectors s_vector and
   *  t_vector that determine the plane of projection, by calling
   *  setTexPlane().
   *
   *  @param  mode  The requested mode.  This should be one of 
   *                GL.GL_NOOP
   *                GL.GL_NONE
   *                GL.GL_SPHERE_MAP
   *                GL.GL_OBJECT_LINEAR
   *                GL.GL_EYE_LINEAR
   */
  public void setTexCoordGenMode( int mode )
  {
    if ( mode == GL.GL_NOOP          ||
         mode == GL.GL_NONE          ||
         mode == GL.GL_SPHERE_MAP    ||
         mode == GL.GL_OBJECT_LINEAR || 
         mode == GL.GL_EYE_LINEAR )
      tex_coord_mode = mode;
    else
      System.out.println("Invalid mode " + mode + " in setTexCoordMode()" );

  }

  /* --------------------------- setTexPlane ---------------------------- */
  /**
   *  Set the orientation of the texture image that will be "projected"
   *  onto the object, if the texture coordinate mode is GL_OBJECT_LINEAR.
   * 
   *  @param s_vector  3-dimensional vector specifying the direction and
   *                   length of the segment [0,1] in the s-direction of 
   *                   the s,t plane. 
   *  @param t_vector  3-dimensional vector specifying the direction and
   *                   length of the segment [0,1] in the t-direction of 
   *                   the s,t plane. 
   *  @param s_shift   Shift in s, to control centering the
   *                   projected texture image.
   *  @param t_shift   Shift in t, to control centering the
   *                   projected texture image.
   */
  public void setTexPlane( Vector3D s_vector, 
                           Vector3D t_vector,
                           float    s_shift,
                           float    t_shift  )
  {
    this.center = new Vector3D( center );
    s_vec = new Vector3D( s_vector );
    t_vec = new Vector3D( t_vector );

    float s_length = s_vec.length();
    float t_length = t_vec.length();
    
    s_vec.multiply( 1/(s_length*s_length) );
    t_vec.multiply( 1/(t_length*t_length) );
 
    this.s_shift = s_shift; 
    this.t_shift = t_shift; 
  }

  /* ------------------------------ preRender --------------------------- */
  /**
   *  Set up the appearance of this shape, before actually rendering the
   *  shape.
   *
   *  @param  drawable  The drawable on which the shape is to be drawn.
   */
  public void preRender( GLDrawable drawable )
  {
    super.preRender(drawable);           // do any initial steps needed by 
                                         // the base classes.

    if ( appearance != null )            // set up the appearance for this 
      appearance.preRender( drawable );  // shape.

    GL gl = drawable.getGL();

    if ( tex_coord_mode == GL.GL_NONE ) 
    {
      gl.glDisable( GL.GL_TEXTURE_GEN_S );
      gl.glDisable( GL.GL_TEXTURE_GEN_T );
    } 
    else if ( tex_coord_mode == GL.GL_SPHERE_MAP )
    { 
      gl.glTexGeni( GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP );
      gl.glTexGeni( GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP );
      gl.glEnable( GL.GL_TEXTURE_GEN_S );
      gl.glEnable( GL.GL_TEXTURE_GEN_T );
    }
    else if ( tex_coord_mode == GL.GL_OBJECT_LINEAR && 
              s_vec != null                         &&
              t_vec != null )
    {
      gl.glTexGeni( GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR );
      gl.glTexGeni( GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR );
      float s_params[] = s_vec.get();
      float t_params[] = t_vec.get();
      s_params[3] = s_shift;
      t_params[3] = t_shift;
      gl.glTexGenfv( GL.GL_S, GL.GL_OBJECT_PLANE, s_params );
      gl.glTexGenfv( GL.GL_T, GL.GL_OBJECT_PLANE, t_params );
      gl.glEnable( GL.GL_TEXTURE_GEN_S );
      gl.glEnable( GL.GL_TEXTURE_GEN_T );
    }
    else if ( tex_coord_mode == GL.GL_EYE_LINEAR  && 
              s_vec != null                       &&
              t_vec != null )
    {
      gl.glTexGeni( GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_EYE_LINEAR );
      gl.glTexGeni( GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_EYE_LINEAR );
      float s_params[] = s_vec.get();
      float t_params[] = t_vec.get();
      gl.glTexGenfv( GL.GL_S, GL.GL_EYE_PLANE, s_params );
      gl.glTexGenfv( GL.GL_T, GL.GL_EYE_PLANE, t_params );
      gl.glEnable( GL.GL_TEXTURE_GEN_S );
      gl.glEnable( GL.GL_TEXTURE_GEN_T );
    }
  }


  /* ------------------------------ postRender --------------------------- */
  /**
   *  Reset the appearance attributes, after rendering the shape.
   *
   *  @param  drawable  The drawable on which the shape is to be drawn.
   */
  public void postRender( GLDrawable drawable )
  {
    if ( appearance != null )             // set up the appearance for this 
      appearance.postRender( drawable );  // shape.

    super.postRender(drawable);           // reset any initial steps taken by 
                                          // the base classes.
  }

}
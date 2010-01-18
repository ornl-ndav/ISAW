/*
 * File:  Geometry.java
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
 * Modified:
 *           
 *  $Log$
 *  Revision 1.1  2007/08/14 00:26:50  dennis
 *  New geometry classes from updated SSG_Tools at UW-Stout.
 *
 *  Revision 1.2  2006/10/16 04:00:50  dennis
 *  Now includes automatic texture coordinate generation.  The
 *  capability to generate texture coordinates was moved from the
 *  Shape class to the Geometry class, since Geometry objects are
 *  also used to specify texture coordinates that are associated
 *  with vertices.
 *
 *  Revision 1.1  2006/10/15 01:15:26  dennis
 *  Intial version of base class for Geometry objects that factor
 *  out geometric elements from shapes.
 *
 *
 */

package SSG_Tools.Geometry;

import javax.media.opengl.*;

import SSG_Tools.*;
import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  A Geometry object is responsible for drawing a shape and specifying the
 *  vertices, normal vectors and texture coordinates.  Geometry objects
 *  are typically used by Shape objects to do the actual drawing.
 */

public abstract class Geometry implements IGL_Renderable
{
  private int        tex_coord_mode = GL.GL_NONE;
  private Vector3D   s_vec = new Vector3D( 1, 0, 0 );
  private Vector3D   t_vec = new Vector3D( 0, 1, 0 );
  private float      s_shift = 0;
  private float      t_shift = 0;

  /* ----------------------- setTexCoordGenMode ------------------------- */
  /**
   *  Specify how texture coordinates are to be generated, IF they are
   *  to be automatically generated by OpenGL.  If the requested mode is
   *  GL.GL_SPHERE_MAP, the texture coordinates will be generated for 
   *  environmental mapping.  If the requested mode is GL.GL_OBJECT_LINEAR,
   *  the texture coordinates will be generated by projecting the object
   *  onto the s,t plane.   If the requested mode is GL.GL_EYE_LINEAR,
   *  this will occur in the eye coordinate system, rather than in the
   *  modeling coordinate system.  If the requested mode is GL_NONE then 
   *  automatic texture coordinate generation will be turned off and any
   *  texture coordinates specifed by the specific geometry object will
   *  be used.
   *
   *  NOTE: if object or eye linear are set, valid values must also be set 
   *  for the vectors s_vector and t_vector that determine the plane of 
   *  projection, by calling setTexPlane().
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
    s_vec = new Vector3D( s_vector );
    t_vec = new Vector3D( t_vector );

    float s_length = s_vec.length();
    float t_length = t_vec.length();

    s_vec.multiply( 1/(s_length*s_length) );
    t_vec.multiply( 1/(t_length*t_length) );

    this.s_shift = s_shift;
    this.t_shift = t_shift;
  }


  /**
   *  Render this object using the specified drawable.
   *
   *  @param  drawable  The drawable on which the object is to be rendered.
   */
  public void Render( GLAutoDrawable drawable )
  {
    GL gl = drawable.getGL();
                                             // If we need to enable texture
    if ( tex_coord_mode == GL.GL_NONE )      // coord generation, do it before
    {                                        // we do the drawing.
      gl.glDisable( GL.GL_TEXTURE_GEN_S );
      gl.glDisable( GL.GL_TEXTURE_GEN_T );
    }
    if ( tex_coord_mode == GL.GL_SPHERE_MAP )
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
      gl.glTexGenfv( GL.GL_S, GL.GL_OBJECT_PLANE, s_params, 0 );
      gl.glTexGenfv( GL.GL_T, GL.GL_OBJECT_PLANE, t_params, 0 );
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
      gl.glTexGenfv( GL.GL_S, GL.GL_EYE_PLANE, s_params, 0 );
      gl.glTexGenfv( GL.GL_T, GL.GL_EYE_PLANE, t_params, 0 );
      gl.glEnable( GL.GL_TEXTURE_GEN_S );
      gl.glEnable( GL.GL_TEXTURE_GEN_T );
    }

    DrawGeometry( gl );

    if ( tex_coord_mode != GL.GL_NONE )      // if we enabled texture coord
    {                                        // generation, turn it off when
      gl.glDisable( GL.GL_TEXTURE_GEN_S );   // we're done with drawin
      gl.glDisable( GL.GL_TEXTURE_GEN_T );
    }
  }


  /**
   *  This method issues the calls to OpenGL that actually draws the object
   *  and defines surface normals and texture coordinates.
   *
   *  @param gl  The GL object on which the geometry will be drawn.
   */
  protected abstract void DrawGeometry( GL gl );

}
/* 
 * File: TextureMappedPlane.java
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

import SSG_Tools.Appearance.Appearance;
import SSG_Tools.Appearance.TransparentMaterial;
import SSG_Tools.Appearance.Textures.Texture2D;

import SSG_Tools.SSG_Nodes.Shapes.Shape;

import EventTools.Histogram.*;

import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This class represents a texture mapped planar paralleogram that 
 *  may be transparent. 
 */

public class TextureMappedPlane extends Shape 
{
  private Vector3D  ll_vec;     // lower left  corner position
  private Vector3D  lr_vec;     // lower right corner position
  private Vector3D  ur_vec;     // upper right corner position
  private Vector3D  ul_vec;     // upper left  corner position

  private Vector3D  normal;     // plane normal

  private Appearance           appearance;
  private TransparentMaterial  material;

  /* --------------------------- Constructor --------------------------- */
  /**
   *  Construct a texture mapped plane using the specified image, over
   *  the specified parallelogram.  The lower-left corner of 
   *  image[0][0] is at the specified ll_corner.  The "bottom" edge of 
   *  row zero is along the specified base vector.  The "left" edge of 
   *  column zero is along the specified up vector.  The texture mapped
   *  plane will be partially transparent if alpha is less than 1.
   *
   *  @param  image       2-D array of floats.  The dimensions should be
   *                      a power of two.  The lower-left corner of 
   *                      image[0][0] is at the specified ll_corner.  
   *                      The "bottom" edge of row zero is along the 
   *                      specified base vector.  The "left" edge of 
   *                      column zero is along the specified up vector.
   *  @param  max         Max of image intensity scale.
   *  @param  color_tran  Array listing correspondence between pixel 
   *                      intensity values and colors in the color_scale.
   *                      Specifically, image values from 0 to max are
   *                      first scaled by value*(n_transf_vals-1)/max, where
   *                      n_transf_vals is the number of entries in the 
   *                      color_tran.  The integer part of this scaled 
   *                      value provides an index into color_tran array.
   *                      The value in the color_tran array is in turn used
   *                      as an index in the color_scale, to obtain the RGB
   *                      components of the color to be displayed. 
   *  @param  color_scale List of Color objects, used to map image intensity
   *                      values to Color objects.
   *  @param  ll_corner   Vector specifying the lower-left corner of the
   *                      image.
   *  @param  base_vec    Vector along the base of the image.  
   *  @param  up_vec      Vector along the left edge of the image, if the 
   *                      paralleologram is viewed from the side of the
   *                      plane where base_vec CROSS up_vec, is positive.
   *                      The up vector must NOT be parallel to the base
   *                      vector.
   *  @param  alpha       The alpha value for the plane.
   */
  public TextureMappedPlane( float[][]  image,
                             float      min,
                             float      max, 
                             int[]      color_tran,
                             Color[]    color_scale, 
                             Vector3D   ll_corner,
                             Vector3D   base_vec,
                             Vector3D   up_vec,
                             float      alpha  )
  {
    ll_vec = new Vector3D( ll_corner );

    lr_vec = new Vector3D( ll_corner );
    lr_vec.add( base_vec );

    ur_vec = new Vector3D( lr_vec );
    ur_vec.add( up_vec );

    ul_vec = new Vector3D( ll_corner );
    ul_vec.add( up_vec );

    normal = new Vector3D();
    normal.cross( base_vec, up_vec );

    material = new TransparentMaterial();
    material.setColor( Color.WHITE );
    material.setAlpha( alpha );

    int n_rows = image.length;
    int n_cols = image[0].length;
    byte[] rgb_image = new byte[3*n_rows*n_cols];

    UniformEventBinner color_binner =
                       new UniformEventBinner( min, max, color_tran.length ); 
    Color color;
    float image_val;
    int   color_index;
    int   index = 0; 
    for ( int row = 0; row < n_rows; row++ )
      for ( int col = 0; col < n_cols; col++ )
      {
        image_val = image[row][col];

        if ( image_val <= min )
          color_index = 0;

        else if ( image_val >= max )
          color_index = color_tran.length - 1; 

        else
          color_index = color_binner.index( image_val ); 

        color_index = color_tran[ color_index ];

        if ( color_index > color_scale.length - 1 )
          color_index = color_scale.length - 1;

        color = color_scale[color_index];    

        rgb_image[3*index  ] = (byte)color.getRed(); 
        rgb_image[3*index+1] = (byte)color.getGreen();
        rgb_image[3*index+2] = (byte)color.getBlue();
        index++;
      }

    Texture2D texture = new Texture2D( rgb_image, n_rows, n_cols );

    appearance = new Appearance();
    appearance.setMaterial( material );
    appearance.setTexture( texture );

    setAppearance( appearance );
  }

 
  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this textured plane to the specified drawable.  
   *
   *  @param  drawable  The drawable on which the LineStrip is to be drawn.
   */
  public void Render( GLAutoDrawable drawable )
  {
    preRender( drawable );

    GL gl = drawable.getGL();

    float[] xyz = normal.get();
    gl.glNormal3f( xyz[0], xyz[1], xyz[2] );
    
    gl.glBegin( GL.GL_QUADS );    // draw the quadrilateral with texture coords

      gl.glTexCoord2f( 0, 0 );                 // lower left corner
      xyz = ll_vec.get();
      gl.glVertex3d( xyz[0], xyz[1], xyz[2] );

      gl.glTexCoord2f( 1, 0 );                 // lower right corner
      xyz = lr_vec.get();
      gl.glVertex3d( xyz[0], xyz[1], xyz[2] );

      gl.glTexCoord2f( 1, 1 );                 // upper right corner
      xyz = ur_vec.get();
      gl.glVertex3d( xyz[0], xyz[1], xyz[2] );

      gl.glTexCoord2f( 0, 1 );                 // upper left corner
      xyz = ul_vec.get();
      gl.glVertex3d( xyz[0], xyz[1], xyz[2] );

    gl.glEnd();

    postRender( drawable );
  }
}

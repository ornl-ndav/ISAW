/* 
 * File: MultiColoredPointList_2.java
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

import java.nio.*;
import javax.media.opengl.*;
import com.sun.opengl.util.*;

import SSG_Tools.SSG_Nodes.SimpleShapes.*;

import EventTools.Histogram.*;

/**
 *  This class represents a list of colored points of a specified
 *  size, drawn using GL_POINTS.
 */

public class MultiColoredPointList_2 extends SimpleShape 
{
  private int      num_points;   // Total number of points available.

  private int[]    code;         // list of codes for each of the points

  private float    min;          // codes with this value or lower will map
                                 // to the first color
  private float    max;          // codes with this value or lower will map
                                 // to the last color
  private int[]    color_table;  // color look up table, as produced by 
                                 // the color control

  private Color[]  color_scale;  // list of RGB values describing the color
                                 // scale.  This must have at most 127 entries
  private float    size;
  private float    alpha;

  private FloatBuffer vertices_buffer;
  private ByteBuffer  color_buffer;
  private IntBuffer   index_buffer;
  private int         element_count;  // counts the number of points that
                                      // are actually drawn

  private boolean  filter_below_min = true;
  private boolean  filter_above_max = true;
  private boolean  use_alpha        = true;

  /* --------------------------- Constructor --------------------------- */
  /**
   *  Construct a PointList, where each point can have its own color.
   *  NOTE: For efficiency, this class keeps a reference to the list
   *        of coordinates and color indexes.  The calling program should
   *        generally not use those arrays after they have been passed in
   *        to this constructor.
   *
   *  @param  x_vals       The list of x-coordinates. 
   *  @param  y_vals       The list of y-coordinates. 
   *  @param  z_vals       The list of z-coordinates. 
   *  @param  color_index  List of indices into the specified color scale
   *                       The ith point will be drawn using the color
   *                       color_scale[color_index[i]].
   *  @param  color_scale  List of colors making up the color map for
   *                       this list of points.
   *  @param  size         The size to use for the points specified in pixel 
   *                       units.
   *  @param  alpha        The alpha value for this list of points.
   */
  public MultiColoredPointList_2( float[]  x_vals,
                                  float[]  y_vals,
                                  float[]  z_vals,
                                  int[]    color_index,
                                  Color[]  color_scale,
                                  float    size,
                                  float    alpha  )
  {
    super(Color.WHITE);

    this.num_points = x_vals.length;
    this.code = color_index;
    this.min  = 0;
    this.max  = color_scale.length-1;

    this.color_table = new int[ color_scale.length ];
    for ( int i = 0; i < color_table.length; i++ )
      color_table[i] = i;

    this.color_scale = color_scale;
    this.size  = size;
    this.alpha = alpha;

    setPointsBuffer( x_vals, y_vals, z_vals );
    setPointsColorBuffer();
  }


  /* --------------------------- Constructor --------------------------- */
  /**
   *  Construct a PointList, where each point can have its own color.
   *  NOTE: This class keeps a reference to the list of codes, the 
   *        color_table and color scale.  The calling program should
   *        generally not use those arrays after they have been passed in
   *        to this constructor.
   *
   *  @param  x_vals       The list of x-coordinates. 
   *  @param  y_vals       The list of y-coordinates. 
   *  @param  z_vals       The list of z-coordinates. 
   *  @param  code         The list of integer codes for the points.
   *  @param  min          The value that maps to the first color.
   *  @param  max          The value that maps to the last color.
   *  @param  color_table  This is a list of indices into the specified 
   *                       color scale that describe a possibly non-linear
   *                       mapping between numbers between the current
   *                       min and max and positions in the specified color
   *                       scale.  This must have values between 0 and
   *                       the length of the color scale - 1.
   *  @param  color_scale  List of colors making up the color scale used for
   *                       this list of points.
   *  @param  size         The size to use for the points specified in pixel 
   *                       units.
   *  @param  alpha        The alpha value for this list of points.
   */
  public MultiColoredPointList_2( float[]  x_vals,
                                  float[]  y_vals,
                                  float[]  z_vals,
                                  int[]    code,
                                  float    min,
                                  float    max,
                                  int[]    color_table,
                                  Color[]  color_scale, 
                                  float    size,
                                  float    alpha  )
  {
    super(Color.WHITE);

    System.out.println("IN MultiColoredPointList_2, min, max = "
                        + min + ", " + max );
    this.num_points  = x_vals.length;
    this.code        = code;
    this.min         = 0;
    this.max         = color_scale.length-1;
    this.color_table = color_table;
    this.color_scale = color_scale;
    this.size  = size;
    this.alpha = alpha;

    setPointsBuffer( x_vals, y_vals, z_vals );
    setPointsColorBuffer();
  }


  /* --------------------------- Constructor --------------------------- */
  /**
   *  Construct a PointList, where each point can have its own color.
   *  NOTE: This class keeps a reference to the list of codes, the 
   *        color_table and color scale.  The calling program should
   *        generally not use those arrays after they have been passed in
   *        to this constructor.
   *
   *  @param  xyz          The list of interleaved xyz-coordinates. 
   *  @param  min          The value that maps to the first color.
   *  @param  max          The value that maps to the last color.
   *  @param  color_table  This is a list of indices into the specified 
   *                       color scale that describe a possibly non-linear
   *                       mapping between numbers between the current
   *                       min and max and positions in the specified color
   *                       scale.  This must have values between 0 and
   *                       the length of the color scale - 1.
   *  @param  color_scale  List of colors making up the color scale used for
   *                       this list of points.
   *  @param  size         The size to use for the points specified in pixel 
   *                       units.
   *  @param  alpha        The alpha value for this list of points.
   */
  public MultiColoredPointList_2( float[]  xyz,
                                  int[]    code,
                                  float    min,
                                  float    max,
                                  int[]    color_table,
                                  Color[]  color_scale,
                                  float    size,
                                  float    alpha  )
  {
    super(Color.WHITE);

//    System.out.println("MCPL Constructor, min, max = " + min + ", " + max );
//    System.out.println("MCPL Constructor, color_scale length = " + 
//                        color_scale.length );
//    System.out.println("MCPL Constructor, color_table length = " + 
//                        color_table.length );

    this.num_points  = xyz.length / 3;
    this.code        = code;
    this.min         = min;
    this.max         = max;
    this.color_table = color_table;
    this.color_scale = color_scale;
    this.size  = size;
    this.alpha = alpha;

    setPointsBuffer( xyz );
    setPointsColorBuffer();
  }


  /**
   *  Set the color lookup table information for this list of points.
   *
   *  @param  min          The value that maps to the first color.
   *  @param  max          The value that maps to the last color.
   *  @param  color_table  This is a list of indices into the specified 
   *                       color scale that describe a possibly non-linear
   *                       mapping between numbers between the current
   *                       min and max and positions in the specified color
   *                       scale.  This must have values between 0 and
   *                       the length of the color scale - 1.
   *  @param  color_scale  List of colors making up the color scale used for
   *                       this list of points.
   */
  public void setColorInfo( float   min, 
                            float   max, 
                            int[]   color_table, 
                            Color[] color_scale )
  {
    this.min         = min;
    this.max         = max;
    this.color_table = color_table;
    this.color_scale = color_scale;
    setPointsColorBuffer();
  }


  /**
   *  Set options on how the points should drawn.
   *
   *  @param filter_above_max  Set true if events with a code above the
   *                           current max value should NOT be drawn.
   *  @param filter_below_min  Set true if events with a code below the
   *                           current min value should NOT be drawn.
   *  @param use_alpha         Set true if alpha blending should be used.
   *  @param alpha             Alpha value (between 0 and 1) to use for
   *                           the event points.
   */
  public void setDrawOptions( boolean filter_above_max,
                              boolean filter_below_min, 
                              boolean use_alpha,
                              float   alpha )
  {
    this.filter_above_max = filter_above_max;
    this.filter_below_min = filter_below_min;
    this.use_alpha        = use_alpha;
    this.alpha            = alpha;
  }


  /**
   *  Build the color_buffer for the current list of points, using the
   *  current code and color information.
   */
  private void setPointsColorBuffer()
  {
//    System.out.println("Color index table length = " + color_table.length );
//    System.out.println("min, max = " + min + ", " + max );
/*
    int[] counters = new int[20];
    for ( int i = 0; i < num_points; i++ )
    {
      if ( code[i] < counters.length )
        counters[ code[i] ]++;
    }

    for ( int i = 0; i < counters.length; i++ )
      System.out.printf( "i = %6d    count = %6d \n", i, counters[i] );
*/

    UniformEventBinner color_binner = 
                       new UniformEventBinner( min, max, color_table.length );
    int place = 0;
    int index;
    int color_index;
    int point_code;
    
    int[] element_index = new int[num_points];
    element_count = 0;

    byte[] rgb = new byte[ 4*num_points ];   // four byte color for each point
    
    boolean draw;
    for ( int i = 0; i < num_points; i++ )
    {
      draw = true;
      point_code = code[i];
      index = color_binner.index( point_code ); 
      if ( point_code < min )  
      {
        index = 0;
        if ( filter_below_min )
          draw = false;
      }
      else if ( index >= color_table.length )
      {
        index = color_table.length - 1;
        if ( filter_above_max )
          draw = false;
      }  

      if ( draw )
      {
        element_index[ element_count++ ] = i;

        color_index = color_table[ index ];

        if ( color_index >= 0 && color_index < color_scale.length )
        {     
          rgb[place++] = (byte)(color_scale[color_index].getRed());
          rgb[place++] = (byte)(color_scale[color_index].getGreen());
          rgb[place++] = (byte)(color_scale[color_index].getBlue());
          if ( use_alpha )
//            rgb[place++] = (byte)(alpha*255); 
            rgb[place++] = (byte)(alpha*index); 
          else
            rgb[place++] = (byte)(255);          
        }
        else
         place += 4;
      }
      else
        place += 4;                             // advance to next position
                                                // even if not drawn
    }

    color_buffer = BufferUtil.newByteBuffer( rgb.length );
    color_buffer.put( rgb );
    color_buffer.rewind();

    index_buffer = BufferUtil.newIntBuffer( element_count );
    index_buffer.put( element_index, 0, element_count );
    index_buffer.rewind();
  }

 
  /**
   *  Pack the three arrays of x,y,z coordinates into an interleaved array
   *  and wrap that array with an NIO FloatBuffer, so it can be used with
   *  glDrawArrays for faster drawing.
   *
   *  @param  x_vals       The list of x-coordinates. 
   *  @param  y_vals       The list of y-coordinates. 
   *  @param  z_vals       The list of z-coordinates. 
   */
  private void setPointsBuffer(float[] x_vals, float[] y_vals, float[] z_vals)
  { 
    float[] xyz = new float[3*x_vals.length];

    int place = 0;
    for ( int i = 0; i < x_vals.length; i++ )
    {
      xyz[place++] = x_vals[i];
      xyz[place++] = y_vals[i];
      xyz[place++] = z_vals[i];
    }

    vertices_buffer = BufferUtil.newFloatBuffer( xyz.length );
    vertices_buffer.put( xyz );
    vertices_buffer.rewind();
  }


  /**
   *  Set up an NIO FloatBuffer, so it can be used with
   *  glDrawArrays for faster drawing.
   *
   *  @param  xyz   An interleaved list of xyz-coordinates for the points.
   */
  private void setPointsBuffer( float[] xyz )
  {
    vertices_buffer = BufferUtil.newFloatBuffer( xyz.length );
    vertices_buffer.put( xyz );
    vertices_buffer.rewind();
  }


  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this list of points to the specified drawable.  
   *
   *  @param  drawable  The drawable on which the list of points will be drawn.
   */
  public void Render( GLAutoDrawable drawable )
  {
//    long start = System.nanoTime();

    GL gl = drawable.getGL();

    super.preRender( drawable );

    gl.glPointSize( size );

    gl.glEnableClientState( GL.GL_VERTEX_ARRAY );
    gl.glVertexPointer( 3, GL.GL_FLOAT, 12, vertices_buffer );

    gl.glEnableClientState( GL.GL_COLOR_ARRAY );
    gl.glColorPointer( 4, GL.GL_UNSIGNED_BYTE, 0, color_buffer );

    gl.glDrawElements( GL.GL_POINTS,  
                       element_count, 
                       GL.GL_UNSIGNED_INT, 
                       index_buffer);

    gl.glDisableClientState( GL.GL_COLOR_ARRAY );
    gl.glDisableClientState( GL.GL_VERTEX_ARRAY );

    super.postRender( drawable );

//    long time = System.nanoTime() - start;
//    System.out.printf("Drew %6d Points in %4.1f ms\n",
//                       element_count, time/1.0E6 );
  }

}

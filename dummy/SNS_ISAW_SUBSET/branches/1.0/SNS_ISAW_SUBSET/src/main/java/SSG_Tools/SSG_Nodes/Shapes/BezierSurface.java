/*
 * File:  BezierSurface.java
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
 * $Log: BezierSurface.java,v $
 * Revision 1.7  2006/07/20 15:35:51  dennis
 * Fixed import & use of deprecated method.
 *
 * Revision 1.6  2006/07/20 15:30:09  dennis
 * Updated from CVS repository on isaw.mscs.uwstout.edu.
 * Added code to check the maximum size of the arrays of control
 * points.  If the array size is too large, the array will be
 * restricted, and only some of the control points will be used.
 * The maximum supported size is implementation dependent, but is
 * at least 8
 * Added methods to get and set the number of steps at which
 * the figure is evaluated.
 * Added methods to get/set the range of texture coordinates
 * used.
 * Added more explanatory javadocs.
 *
 * Revision 1.5  2005/12/13 20:45:48  dennis
 * Added code to check the maximum size of the arrays of control
 * points.  If the array size is too large, the array will be
 * restricted, and only some of the control points will be used.
 * The maximum supported size is implementation dependent, but is
 * at least 8.
 *
 * Revision 1.4  2005/11/11 04:25:28  dennis
 * Added methods to get/set max_s and max_t values.
 * Added methods to get/set the number of steps in u and v.
 * Added more explanatory javadocs.
 *
 * Revision 1.3  2004/12/13 04:55:21  dennis
 * Now also generates texture coordinates when the surface is drawn.
 * The texture coordinates range from 0 to 1 in s along with the
 * surface parameters, u, v.
 *
 * Revision 1.2  2004/12/09 22:18:10  dennis
 * Added constructor that allows setting the number of evaluation points.
 * Changed test programs control points and include test of number of
 * evaluation points.
 * Constructors now throw an IllegalArgumentException if there is
 * something wrong with the array of control points.
 *
 * Revision 1.1  2004/11/15 18:56:34  dennis
 * Initial version of shapes that use OpenGL to generate Bezier curves
 * and surfaces.
 *
 */

package SSG_Tools.SSG_Nodes.Shapes;

import net.java.games.jogl.*;

import javax.swing.*;

import SSG_Tools.Viewers.*;
import SSG_Tools.Appearance.*;
import gov.anl.ipns.MathTools.Geometry.*;

/** 
 *   This class draws a Bezier surface patch.  The shape of the Bezier 
 * surface is determined by forming weighted averages of the points in the 
 * two dimensional array of control points.  If the two dimensional array of
 * control points is viewed with the column numbers increasing to the right
 * and the row numbers increasing in the upward direction, then the front 
 * face of the surface should be visible.  When viewed this way, the texture 
 * coordinates of the surface will have the "s" coordinate increasing to the
 * right and the "t" coordinate increasing in the upward direction.  The
 * default range for texture coordinates is from (0,0) in the lower left
 * hand corner, to (1,1) in the upper right hand corner.  The maximum values
 * to use for the "s" and "t" coordinates can be specified by calling the
 * setMax_s() and setMax_t() methods, before rendering the surface.  
 *
 *   By default, the surface will be evaluated using 10 intermediate points
 * in the "u" direction (increasing row number) and "v" direction (increasing
 * column number).  The number of points to evaluate in the "u" and "v" 
 * directions can be altered by calling the setNum_u() and setNum_v() 
 * methods. 
 *
 *   NOTE: The number of rows and columns in the array of control points
 * must not exceed the value specified by the OpenGL constant 
 * GL_MAX_EVAL_ORDER.  The maximum number of rows and columns allowed
 * depends on the OpenGL implementation, but must be at least 8.  This is
 * checked when the surface is rendered, and a warning message will be
 * printed if the maximum is exceeded.
 */

public class BezierSurface extends Shape
{
  private static boolean first_time = true;
  private static int     max_size   = 8;     // maximum size will be at least 8

  private Vector3D control_points[][];
  private int   n_u_vals = 10;
  private int   n_v_vals = 10;
  private float max_s    = 1;
  private float max_t    = 1;


  /* --------------------------- constructor --------------------------- */
  /**
   *  Construct a BezierSurface object with the specified control points.
   *
   *  @param  points   A two dimensional "rectangular" array of control
   *                   points.  There must be at least one row and at least
   *                   one column.  Also, each row must have the same number
   *                   of points.
   */
  public BezierSurface( Vector3D points[][] )
  {
    IllegalArgumentException ex = checkControlPoints( points );
    if ( ex != null )
      throw ex;

    control_points = new Vector3D[ points.length ][ points[0].length ];

    for ( int row = 0; row < points.length; row++ )
      for ( int col = 0; col < points[0].length; col++ )
        control_points[ row ][ col ] = new Vector3D( points[ row ][ col ] );
  }


  /* --------------------------- constructor --------------------------- */
  /**
   *  Construct a BezierSurface object with the specified control points.
   *
   *  @param  num_us   The number of u values used when evaluating the surface.
   *                   The value of u varies from 0 to 1 in the "direction"
   *                   of increasing row number. 
   *  @param  num_vs   The number of v values used when evaluating the surface
   *                   The value of v varies from 0 to 1 in the "direction"
   *                   of increasing column number. 
   *  @param  points   A two dimensional "rectangular" array of control
   *                   points.  There must be at least one row and at least
   *                   one column.  Also, each row must have the same number
   *                   of points.
   */
  public BezierSurface( Vector3D points[][], int num_us, int num_vs )
  {
    IllegalArgumentException ex = checkControlPoints( points );
    if ( ex != null )
      throw ex;

    control_points = new Vector3D[ points.length ][ points[0].length ];

    if ( num_us > 1 )
      n_u_vals = num_us - 1;

    if ( num_vs > 1 )
      n_v_vals = num_vs - 1;

    for ( int row = 0; row < points.length; row++ )
      for ( int col = 0; col < points[0].length; col++ )
        control_points[ row ][ col ] = new Vector3D( points[ row ][ col ] );
  }


  /* ---------------------------- setMax_s ------------------------------ */
  /**
   *  Set the maximum "s" texture coordinate value to use, if the explicitly
   *  generated texture coordinates are not overridden by texture coordinates
   *  generated by OpenGL due to calling Shape.setTexCoordGenMode().
   *
   *  @param  max_s  the maximum texture coordinate "s" that is generated.
   */
  public void setMax_s( float max_s )
  {
    this.max_s = max_s;
  }


  /* ---------------------------- getMax_s ------------------------------ */
  /**
   *  Get the currently set value for the maximum "s" texture coordinate. 
   *
   *  @return  max_s  the maximum texture coordinate "s" that is generated.
   */
  public float getMax_s()
  {
    return max_s;
  }


  /* ---------------------------- setMax_t ------------------------------ */
  /**
   *  Set the maximum "t" texture coordinate value to use, if the explicitly
   *  generated texture coordinates are not overridden by texture coordinates
   *  generated by OpenGL due to calling Shape.setTexCoordGenMode().
   *
   *  @param  max_t  the maximum texture coordinate "t" that is generated.
   */
  public void setMax_t( float max_t )
  {
    this.max_t = max_t;
  }


  /* ---------------------------- getMax_t ------------------------------ */
  /**
   *  Get the currently set value for the maximum "t" texture coordinate. 
   *
   *  @return  max_t  the maximum texture coordinate "t" that is generated.
   */
  public float getMax_t()
  {
    return max_t;
  }


  /* ---------------------------- setNum_u ------------------------------ */
  /**
   *  Set the number of points at which the surface is evaluated in the
   *  "u" direction (direction of increasing row number).  If the specified 
   *  number is less than 1, the default value of 10 will be used.
   *
   *  @param  num_u  the number of point to use in the "u" direction.
   *                 This must be at least 1, or the default value of 10
   *                 will be used.
   */
  public void setNum_u( int num_u )
  {
    if ( num_u > 0 )
      n_u_vals = num_u;
    else
      n_u_vals = 10;
  }


  /* ---------------------------- getNum_u ------------------------------ */
  /**
   *  Get the currently set value for number of points to evaluate in the
   *  "u" direction.
   *
   *  @return  num_u  the number of points evaluated in the "u" direction.
   */
  public int getNum_u()
  {
    return n_u_vals;
  }


  /* ---------------------------- setNum_v ------------------------------ */
  /**
   *  Set the number of points at which the surface is evaluated in the
   *  "v" direction (direction of increasing column number).  If the 
   *  specified number is less than 1, the default value of 10 will be used.
   *
   *  @param  num_v  the number of point to use in the "v" direction.
   *                 This must be at least 1, or the default value of 10
   *                 will be used.
   */
  public void setNum_v( int num_v )
  {
    if ( num_v > 0 )
      n_v_vals = num_v;
    else
      n_v_vals = 10;
  }


  /* ---------------------------- getNum_v ------------------------------ */
  /**
   *  Get the currently set value for number of points to evaluate in the
   *  "v" direction.
   *
   *  @return  num_v  the number of points evaluated in the "v" direction.
   */
  public int getNum_v()
  {
    return n_v_vals;
  }


  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this Bezier surface to the specified drawable using OpenGL.
   *
   *  @param  drawable  The drawable on which the plane is to be drawn.
   */
  public void Render( GLDrawable drawable )
  {
    preRender( drawable );                // handle name stack, appearance, etc

    GL gl = drawable.getGL();

    if ( first_time )                     // get the max size for array of 
    {                                     // control points the first time
      int order[] = new int[1];
      gl.glGetIntegerv( GL.GL_MAX_EVAL_ORDER, order );
      max_size = order[0];      
    }

    int    n_rows = control_points.length;
    int    n_cols = control_points[0].length;
                                                    // Check for common error
    if ( n_rows > max_size || n_cols > max_size )   // condition, array to big
    {
      System.out.println("ERROR in BezierSurface.render().  Max number " );
      System.out.println("rows or columns must not exceed " + max_size );
      System.out.println("Number of rows of control points = " + n_rows );
      System.out.println("Number of cols of control points = " + n_cols );
      System.out.println("Rendering surface using a restricted set ");
      System.out.println("of the control points.");

      if ( n_rows > max_size )
        n_rows = max_size;

      if ( n_cols > max_size )
        n_cols = max_size;
    }

    float  float_pts[] = new float[ 3* n_rows * n_cols ];
    int    offset;
    float  coords[];
                            // copy control points to simple array for OpenGL
    for ( int row = 0; row < n_rows; row++ )
      for ( int col = 0; col < n_cols; col++ )
      {
        offset = (n_rows - 1 - row) * n_cols + col;
        coords = control_points[ row ][ col ].get();
        float_pts[ offset * 3     ] = coords[0];
        float_pts[ offset * 3 + 1 ] = coords[1];
        float_pts[ offset * 3 + 2 ] = coords[2];
      }

    gl.glMap2f( GL.GL_MAP2_VERTEX_3, 
                0, 1, 3 * n_cols, n_rows,
                0, 1, 3,          n_cols, float_pts );

    float tex_coords[] = { 0, max_t,   max_s, max_t,   0, 0,   max_s, 0 };
    gl.glMap2f( GL.GL_MAP2_TEXTURE_COORD_2, 
                0, 1, 2 * 2, 2,
                0, 1, 2,     2, tex_coords );

    gl.glEnable( GL.GL_MAP2_VERTEX_3 );
    gl.glEnable( GL.GL_AUTO_NORMAL );
    gl.glEnable( GL.GL_MAP2_TEXTURE_COORD_2 );
    gl.glMapGrid2f( n_u_vals, 0, 1,
                    n_v_vals, 0, 1 );

    int polygon_mode = GL.GL_NONE;
    Appearance appearance = getAppearance();
    if ( appearance != null )
      polygon_mode = appearance.getPolygonMode();

    if ( polygon_mode == GL.GL_NONE )              // default behavior for our
      polygon_mode = GL.GL_FILL;                   // BezierSurface is to FILL
    
    gl.glEvalMesh2( polygon_mode, 0, n_u_vals, 0, n_v_vals );
    gl.glDisable( GL.GL_MAP2_VERTEX_3 );
    gl.glDisable( GL.GL_AUTO_NORMAL );
    gl.glDisable( GL.GL_MAP2_TEXTURE_COORD_2 );

    postRender( drawable );                // clean up after drawing
  }


  /* ------------------------- checkControlPoints ---------------------- */
  /**
   *  Check whether or not the parameter is a valid two dimensional
   *  array of Vectors.
   */
  private IllegalArgumentException checkControlPoints( Vector3D points[][] )
  {
    IllegalArgumentException ex = null;

    if ( points == null )
     ex = new IllegalArgumentException("null array of points in BezierSurface");

    if ( points.length < 2 )
     ex = new IllegalArgumentException("<2 rows of points in BezierSurface");

    for ( int i = 0; i < points.length; i++ )
     if ( points[i] == null )
       ex = new IllegalArgumentException("null row of points in BezierSurface");

    for ( int i = 1; i < points.length; i++ )
      if ( points[i].length != points[0].length )
      ex = new IllegalArgumentException("unequal row lengths in BezierSurface");

    for ( int i = 0; i < points.length; i++ )
      for ( int j = 0; j < points[i].length; j++ )
        if ( points[i][j] == null )
          ex = new IllegalArgumentException("null Vector3D in BezierSurface");

    return ex;
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
    Vector3D c_points[][] = new Vector3D[4][3];
    c_points[3][0] = new Vector3D(  0,  0,  3 );
    c_points[3][1] = new Vector3D(  3,  0,  3 );
    c_points[3][2] = new Vector3D(  3,  0,  0 );

    c_points[2][0] = new Vector3D(  0,  0,  1 );
    c_points[2][1] = new Vector3D(  1,  0,  1 );
    c_points[2][2] = new Vector3D(  1,  0,  0 );

    c_points[1][0] = new Vector3D(  0,  1,  1 );
    c_points[1][1] = new Vector3D(  1,  1,  1 );
    c_points[1][2] = new Vector3D(  1,  1,  0 );

    c_points[0][0] = new Vector3D(  0,  2,  1 );
    c_points[0][1] = new Vector3D(  1,  2,  1 );
    c_points[0][2] = new Vector3D(  1,  2,  0 );

    JoglPanel demo = new JoglPanel( new BezierSurface( c_points, 20, 4 ) );

    JFrame frame = new JFrame( "BezierSurface Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

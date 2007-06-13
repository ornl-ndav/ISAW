/*
 * File:  BezierCurve.java
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
 * $Log: BezierCurve.java,v $
 * Revision 1.5  2006/07/20 15:35:51  dennis
 * Fixed import & use of deprecated method.
 *
 * Revision 1.4  2006/07/20 15:30:09  dennis
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
 * Revision 1.3  2005/12/13 21:32:54  dennis
 * Fixed error message that is displayed if too many control points
 * are specified.
 *
 * Revision 1.2  2005/12/13 20:47:22  dennis
 * Added code to check the maximum size of the array of control
 * points.  If the array size is too large, the array will be
 * restricted, and only some of the control points will be used.
 * The maximum supported size is implementation dependent, but is
 * at least 8.
 * Added methods to get/set the number of points at which the curve
 * is evaluated.
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
import SSG_Tools.Viewers.Controls.*;
import SSG_Tools.Appearance.*;
import gov.anl.ipns.MathTools.Geometry.*;

/** 
 *   This class draws a Bezier curve using the specified control points.  The
 * shape of the Bezier curve is determined by forming weighted averages of
 * the points in the array of control points.
 *
 *   By default, the curve will be evaluated using 10 intermediate points. The
 * number of points to use to evaluate the curve can be set by calling the
 * setNum_steps() method.
 *
 *   NOTE: The number of control points must not exceed the value specified 
 * by the OpenGL constant GL_MAX_EVAL_ORDER.  The maximum number points allowed
 * depends on the OpenGL implementation, but must be at least 8.  This is
 * checked when the is rendered, and a warning message will be
 * printed if the maximum is exceeded.
 */

public class BezierCurve extends Shape
{
  private static boolean first_time = true;
  private static int     max_size   = 8;     // maximum size will be at least 8

  private Vector3D control_points[];
  private int n_steps = 10;


  /* --------------------------- constructor --------------------------- */
  /**
   *  Construct a BezierCurve object with the specified control points.
   *
   *  @param  points   A one dimensional list of control points.  There 
   *                   must be at least one element in the array. 
   */
  public BezierCurve( Vector3D points[] )
  {
    // #### Should first check for valid paramter here
    control_points = new Vector3D[ points.length ];

    for ( int i = 0; i < points.length; i++ )
      control_points[ i ] = new Vector3D( points[ i ] );
  }


  /* -------------------------- setNum_points ---------------------------- */
  /**
   *  Set the number of points at which the curve is evaluated.
   *
   *  @param  num_points  the number of point to use when evaluating the 
   *                      curve.  If the specified number is less than 1,
   *                      the default value of 10 will be used.
   */
  public void setNum_points( int num_points )
  {
    if ( num_points > 0 )
      n_steps = num_points;
    else
      n_steps = 10;
  }


  /* -------------------------- getNum_points ---------------------------- */
  /**
   *  Get the currently set value for number of points to evaluate along 
   *  the curve.
   *
   *  @return  the number of points evaluated along the curve.
   */
  public int getNum_points()
  {
    return n_steps;
  }


  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this Bezier curve to the specified drawable using OpenGL.
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

    int    n_points = control_points.length;
                                                    // Check for common error
    if ( n_points > max_size )                      // condition, array to big
    {
      System.out.println("ERROR in BezierCurve.render().  Number " );
      System.out.println("of points must not exceed " + max_size );
      System.out.println("Number of control points = " + n_points );
      System.out.println("Rendering curve using a restricted set ");
      System.out.println("of the control points.");

      n_points = max_size;
    }

    float  float_pts[] = new float[ 3 * n_points ];
    int    offset;
    float  coords[];

                            // copy control points to simple array for OpenGL
    for ( int i = 0; i < n_points; i++ )
    {
      coords = control_points[ i ].get();
      offset = i * 3;
      float_pts[ offset     ] = coords[0];
      float_pts[ offset + 1 ] = coords[1];
      float_pts[ offset + 2 ] = coords[2];
    }

    gl.glMap1f( GL.GL_MAP1_VERTEX_3, 0, 1, 3, n_points, float_pts );

    gl.glEnable( GL.GL_MAP1_VERTEX_3 );
    gl.glEnable( GL.GL_AUTO_NORMAL );
    gl.glMapGrid1f( n_steps, 0, 1 );

    int polygon_mode = GL.GL_NONE;
    Appearance appearance = getAppearance();
    if ( appearance != null )
      polygon_mode = appearance.getPolygonMode();

    if ( polygon_mode != GL.GL_POINT )             // default behavior for our
      polygon_mode = GL.GL_LINE;                   // BezierCurve is LINE 

    gl.glEvalMesh1( polygon_mode, 0, n_steps );
    gl.glDisable( GL.GL_MAP1_VERTEX_3 );
    gl.glDisable( GL.GL_AUTO_NORMAL );

    postRender( drawable );                // clean up after drawing
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
    Vector3D c_points[] = new Vector3D[9];
    c_points[0] = new Vector3D( 0, 0, 0 );
    c_points[1] = new Vector3D( -3, 3, 0 );
    c_points[2] = new Vector3D(  3, 3, 0 );
    c_points[3] = new Vector3D(  0,  3,  0 );
    c_points[4] = new Vector3D(  0,  3,  3 );
    c_points[5] = new Vector3D(  3,  3,  3 );
    c_points[6] = new Vector3D(  1,  3,  0 );
    c_points[7] = new Vector3D(  1,  1,  0 );
    c_points[8] = new Vector3D(  0,  1,  0 );

    BezierCurve curve = new BezierCurve( c_points );
    curve.setNum_points( 50 );

    JoglPanel demo = new JoglPanel( curve, true, JoglPanel.DEBUG_MODE );
    new MouseArcBall( demo );

    JFrame frame = new JFrame( "BezierCurve Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

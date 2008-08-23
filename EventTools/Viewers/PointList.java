/* 
 * File: PointList.java
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
import java.util.Random;

import javax.swing.*;
import javax.media.opengl.*;

import gov.anl.ipns.MathTools.Geometry.*;
import SSG_Tools.SSG_Nodes.SimpleShapes.*;
import SSG_Tools.SSG_Nodes.Group;
import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;


/**
 *  This class represents a list of simply colored points of a specified
 *  size, drawn using GL_POINTS.
 */

public class PointList extends SimpleShape 
{
  private float    size;
  private float[]  x = null;
  private float[]  y = null;
  private float[]  z = null;
  private float    alpha;


  /* --------------------------- Constructor --------------------------- */
  /**
   *  Construct a PointList from the specified arrays.
   *
   *  @param  x_vals    The list of x-coordinates. 
   *  @param  y_vals    The list of y-coordinates. 
   *  @param  z_vals    The list of z-coordinates. 
   *  @param  size      The size to use for the points specified in pixel 
   *                    units.
   *  @param  new_color The color of the points.
   *
   */
  public PointList( float[]  x_vals,
                    float[]  y_vals,
                    float[]  z_vals,
                    float    size,
                    Color    new_color,
                    float    alpha )
  {
    super(new_color);
    this.size  = size;
    this.alpha = alpha;
    x = x_vals;
    y = y_vals;
    z = z_vals;
  }

 
  /* --------------------------- Constructor --------------------------- */
  /**
   *  Construct a PointList from the specified list of Vector3D objects.
   *
   *  @param  verts     List of Vector3D objects specifying point positions.
   *  @param  size      The size to use for the points specified in pixel 
   *                    units.
   *  @param  new_color The color of the points.
   *
   */
  public PointList( Vector3D verts[],
                    float    size,
                    Color    new_color,
                    float    alpha )
  {
    super(new_color);
    this.size = size;
    this.alpha = alpha;

    x = new float[ verts.length ];
    y = new float[ verts.length ]; 
    z = new float[ verts.length ];
    float[] coords;
    for ( int i = 0; i < verts.length; i++ )
    {
      coords = verts[i].get();
      x[i] = coords[0];
      y[i] = coords[1];
      z[i] = coords[2];
    }
  }


  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this list of points to the specified drawable.  
   *
   *  @param  drawable  The drawable on which the points will be drawn.
   */
  public void Render( GLAutoDrawable drawable )
  {
    GL gl = drawable.getGL();

    super.preRender( drawable );

    if ( color != null )
      gl.glColor4f( color[0], color[1], color[2], alpha );

    gl.glPointSize( size );
    gl.glBegin( GL.GL_POINTS );
      for ( int i = 0; i < x.length; i++ )
        gl.glVertex3f( x[i], y[i], z[i] );
    gl.glEnd();

    super.postRender( drawable );
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that constructs an instance of a list of points and 
   *  displays them in 3D for testing purposes.  
   */
  public static void main( String args[] )
  {
    float size = 1;
    
    int NUM_POINTS       = 100000;
    Vector3D[] pts_r     = new Vector3D[NUM_POINTS];
    Vector3D[] pts_g     = new Vector3D[NUM_POINTS];
    float x1, 
          y1, 
          z1;
    
    Random ran_gen = new Random();
    for ( int i = 0; i < NUM_POINTS; i++ )
    {
      x1 = 10*ran_gen.nextFloat() - 5;
      y1 = 10*ran_gen.nextFloat() - 5;
      z1 = 10*ran_gen.nextFloat() - 5;
      pts_r[i] = new Vector3D( x1, y1, z1 );
      
      x1 = 10*ran_gen.nextFloat() - 5;
      y1 = 10*ran_gen.nextFloat() - 5;
      z1 = 10*ran_gen.nextFloat() - 5;
      pts_g[i] = new Vector3D( x1, y1, z1 );
    }
    
    Color blue = new Color( 50, 50, 150 );
    SimpleShape shape_r = new PointList( pts_r, size, Color.RED, 1.0f );
    
    SimpleShape shape_g = new PointList( pts_g, size, blue, 0.5f );
   
    Group group = new Group();
      group.addChild( shape_r );
      group.addChild( shape_g );
    JoglPanel demo = new JoglPanel( group, true );
    new MouseArcBall( demo );

    JFrame frame = new JFrame( "PointList Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

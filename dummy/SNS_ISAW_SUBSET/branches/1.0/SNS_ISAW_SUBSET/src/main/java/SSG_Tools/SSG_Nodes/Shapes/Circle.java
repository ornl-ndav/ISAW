/*
 * File:  Circle.java
 *
 * Copyright (C) 2005, Chad Jones
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
 * Primary   Chad Jones <cjones@cs.utk.edu>
 * Contact:  Student Developer, University of Tennessee
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * Modified:
 *
 * $Log: Circle.java,v $
 * Revision 1.2  2006/07/04 00:38:15  dennis
 * Replace call to deprecated method JFrame.show() with call to
 * setVisible( true )
 *
 * Revision 1.1  2005/07/22 19:32:05  cjones
 * Added 2D Shapes: Line and Circle
 *
 *
 */

package SSG_Tools.SSG_Nodes.Shapes;

import net.java.games.jogl.*;
import java.awt.*;
import javax.swing.*;

import gov.anl.ipns.MathTools.Geometry.*;
import SSG_Tools.Viewers.*;
import SSG_Tools.Appearance.*;

/** 
 *  This class draws a 2d circle of the specified radius centered at the 
 *  origin.
 */

public class Circle extends Shape
{
  public static final float DEFAULT_RADIUS = 1;
  public static final int   DEFAULT_NUM_LINES = 10;
 
  private float radius   = DEFAULT_RADIUS;
  private float n_lines = DEFAULT_NUM_LINES;

  /* ------------------------ default constructor ------------------------ */
  /**
   *  Construct a default circle object with the default radius, number
   *  of lines.
   */
  public Circle()
  {
  	// Default constructor
  }

  /* --------------------------- constructor --------------------------- */
  /**
   *  Construct a circle object with the specified radius using the specified
   *  number of subdivisions. 
   *
   *  @param  radius     The radius of the circle.
   *  @param  num_lines  The number of lines to divide circle into.
   */
  public Circle( float radius, int num_lines )
  {
    this();
    this.radius = radius;

    if ( num_lines > 0 )
      n_lines = num_lines;
  }

  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this circle to the specified drawable.  
   *
   *  @param  drawable  The drawable on which the sphere is to be drawn.
   */
  public void Render( GLDrawable drawable )
  {
    GL gl = drawable.getGL();

    double angle;
 
    preRender( drawable );     // handle name stack and appearance

    gl.glBegin(GL.GL_LINE_LOOP);
      
    for(int i =0; i < n_lines; i++) {
      angle = i*2*Math.PI/n_lines;

      gl.glVertex2f(radius*(float)Math.cos(angle), radius*(float)Math.sin(angle));
    }
      
    gl.glEnd();
      
    postRender( drawable );                      // clean up, after rendering  
  }

  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
  	Circle circle = new Circle( 1, 35 );
  	Material material = new Material();
  	material.setColor( Color.RED );
  	Appearance appearance = new Appearance();
  	appearance.setMaterial( material );
  	circle.setAppearance( appearance );
  	
    JoglPanel demo = new JoglPanel( circle );
    
    demo.getCamera().setVUV( new Vector3D( 0, 1, 0 ) );
    demo.getCamera().setCOP( new Vector3D( 0, 0, 5 ) );

    JFrame frame = new JFrame( "Sphere Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible(true);
  }

}

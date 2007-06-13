/*
 * File:  Line.java
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
 * $Log: Line.java,v $
 * Revision 1.2  2006/07/04 00:38:15  dennis
 * Replace call to deprecated method JFrame.show() with call to
 * setVisible( true )
 *
 * Revision 1.1  2005/07/22 19:32:06  cjones
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
 *  This class draws a 2d line from one point to another.
 */

public class Line extends Shape
{
 
  private float[] start_point;
  private float[] end_point;

  /* --------------------------- constructor --------------------------- */
  /**
   *  Construct a line object between given points.
   *
   *  @param  point1  First point.
   *  @param  point2  Second point.
   */
  public Line( Vector3D point1, Vector3D point2 )
  {
  	start_point = point1.get();
  	end_point = point2.get();    
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

    preRender( drawable );     // handle name stack and appearance

    gl.glBegin(GL.GL_LINES);
     	
      gl.glVertex3f( start_point[0], start_point[1], start_point[2] );
      gl.glVertex3f( end_point[0], end_point[1], end_point[2]);
      
    gl.glEnd();
      
    postRender( drawable );                      // clean up, after rendering  
  }

  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
  	Line line = new Line( new Vector3D(-1.f, -1.f, -1.f), new Vector3D(1.f, 1.f, 1.f) );
  	Material material = new Material();
  	material.setColor( Color.RED );
  	Appearance appearance = new Appearance();
  	appearance.setMaterial( material );
  	line.setAppearance( appearance );
  	
    JoglPanel demo = new JoglPanel( line );
    
    demo.getCamera().setVUV( new Vector3D( 0, 1, 0 ) );
    demo.getCamera().setCOP( new Vector3D( 0, 0, 5 ) );

    JFrame frame = new JFrame( "Sphere Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible(true);
  }

}

/*
 * File:  Parallelogram.java
 *
 * Copyright (C) 2005 Dennis Mikkelson
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
 *  $Log: Parallelogram.java,v $
 *  Revision 1.2  2006/07/04 00:40:47  dennis
 *  Replaced call to deprecated method JFrame.show(), with call
 *  to setVisible(true).
 *
 *  Revision 1.1  2005/07/25 15:46:02  dennis
 *  Initial version of simple shape.
 *
 *
 */
package SSG_Tools.SSG_Nodes.SimpleShapes;

import java.awt.*;
import javax.swing.*;
import net.java.games.jogl.*;

import gov.anl.ipns.MathTools.Geometry.*;
import SSG_Tools.Viewers.*;

import SSG_Tools.Cameras.*;

/**
 *  This Node does a basic draw of a parallelogram facet.
 */

public class Parallelogram extends SimpleShape 
{
   float p1[] = { -1, -1, 0 };
   float p2[] = {  1, -1, 0 };
   float p3[] = {  1,  1, 0 };
   float p4[] = { -1,  1, 0 };
   float normal[] = { 0, 0, 1 };


  /* --------------------------- Constructor --------------------------- */
  /**
   *  Construct a flat 3D parallelogram centered at the specified point with
   *  base edge and side edges lengths and directions specified by the 
   *  "base" and "up" vectors.   
   *
   *  @param  center_vec   The position of the center of the parallelogram.
   *  @param  base_vec     The length and direction of the base.
   *  @param  up_vec       The length and direction of the side.
   *  @param  new_color    The color of the parallelogram.
   *
   */
  public Parallelogram( Vector3D center_vec, 
                        Vector3D base_vec, 
                        Vector3D up_vec, 
                        Color    new_color )
  {
    super(new_color);

    float center[] = center_vec.get(); 
    float base[]   = base_vec.get(); 
    float up[]     = up_vec.get(); 

    for ( int i = 0; i < 3; i++ )
    {
      p1[i] = center[i] - base[i]/2 - up[i]/2;
      p2[i] = center[i] + base[i]/2 - up[i]/2;
      p3[i] = center[i] + base[i]/2 + up[i]/2;
      p4[i] = center[i] - base[i]/2 + up[i]/2;
    }

    Vector3D normal_vec = new Vector3D();
    normal_vec.cross( base_vec, up_vec );
    normal_vec.normalize();
    normal = normal_vec.get();
    
    color[0] = new_color.getRed()/255f;
    color[1] = new_color.getGreen()/255f;
    color[2] = new_color.getBlue()/255f;
  }


  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this Parallelogram to the specified drawable.  
   *
   *  @param  drawable  The drawable on which the Parallelogram is to be drawn.
   */
  public void Render( GLDrawable drawable )
  {
    GL gl = drawable.getGL();

    super.preRender( drawable );
    gl.glBegin( GL.GL_POLYGON );
      gl.glNormal3fv( normal );
      gl.glColor3fv( color );
      gl.glVertex3fv( p1 );
      gl.glVertex3fv( p2 );
      gl.glVertex3fv( p3 );
      gl.glVertex3fv( p4 );
    gl.glEnd();
    super.postRender( drawable );
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that constructs an instance of Parallelogram and displays 
   *  it in 3D for testing purposes.  
   */
  public static void main( String args[] )
  {
    Parallelogram par = new Parallelogram( new Vector3D( 0,0,0 ),
                                            new Vector3D( 1,0,0 ),
                                            new Vector3D( 0,1,0 ),
                                            Color.RED );

    JoglPanel demo = new JoglPanel( par );
    demo.enableHeadlight( true );

    Camera camera = demo.getCamera();
    camera.setVRP( new Vector3D( 0,0, 0 ) );
    camera.setCOP( new Vector3D( 0, 0, 4 ) );
    camera.SetViewVolume( 1, 10, 40 );
    demo.setCamera( camera );
  
    JFrame frame = new JFrame( "Sphere Scene" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible(true);
  }

}

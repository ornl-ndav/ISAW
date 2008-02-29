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
 *  Revision 1.3  2007/08/14 00:03:33  dennis
 *  Major update to JSR231 based version from UW-Stout repository.
 *
 *  Revision 1.5  2006/11/26 01:43:02  dennis
 *  Changed to allow a null color.  If color is null, the last color
 *  that was set will be used.
 *
 *  Revision 1.4  2006/09/23 05:05:32  dennis
 *  Added MouseArcBall to control the view.
 *
 *  Revision 1.3  2006/08/04 02:16:21  dennis
 *  Updated to work with JSR-231, 1.0 beta 5,
 *  instead of jogl 1.1.1.
 *
 *  Revision 1.2  2006/07/20 19:59:01  dennis
 *  Replaced deprecated method frame.show() with setVisible(true)
 *
 *  Revision 1.1  2005/10/14 04:04:11  dennis
 *  Copied into local CVS repository from CVS repository at IPNS.
 */

package SSG_Tools.SSG_Nodes.SimpleShapes;

import java.awt.*;
import javax.swing.*;
import javax.media.opengl.*;

import gov.anl.ipns.MathTools.Geometry.*;
import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;

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
  }


  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this Parallelogram to the specified drawable.  
   *
   *  @param  drawable  The drawable on which the Parallelogram is to be drawn.
   */
  public void Render( GLAutoDrawable drawable )
  {
    GL gl = drawable.getGL();

    super.preRender( drawable );

    if ( color != null )
      gl.glColor3fv( color, 0 );

    gl.glBegin( GL.GL_POLYGON );
      gl.glNormal3fv( normal, 0 );
      gl.glVertex3fv( p1, 0 );
      gl.glVertex3fv( p2, 0 );
      gl.glVertex3fv( p3, 0 );
      gl.glVertex3fv( p4, 0 );
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
    new MouseArcBall( demo );

    Camera camera = demo.getCamera();
    camera.setVRP( new Vector3D( 0,0, 0 ) );
    camera.setCOP( new Vector3D( 0, 0, 4 ) );
    camera.SetViewVolume( 1, 10, 40 );
    demo.setCamera( camera );
  
    JFrame frame = new JFrame( "Parallelogram" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

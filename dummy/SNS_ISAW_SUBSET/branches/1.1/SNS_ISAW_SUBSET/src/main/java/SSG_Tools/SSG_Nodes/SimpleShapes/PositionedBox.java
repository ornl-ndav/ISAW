/*
 * File:  PositionedBox.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * $Log: PositionedBox.java,v $
 * Revision 1.3  2006/07/25 04:38:43  dennis
 * Added mouse arc ball to main test program.
 * Now includes parameter to control wheter a lightweight or
 * heavyweight component is used.
 *
 * Revision 1.2  2006/07/04 00:40:47  dennis
 * Replaced call to deprecated method JFrame.show(), with call
 * to setVisible(true).
 *
 * Revision 1.1  2005/07/25 15:46:03  dennis
 * Initial version of simple shape.
 *
 *
 */

package SSG_Tools.SSG_Nodes.SimpleShapes;

import java.awt.*;
import javax.swing.*;
import net.java.games.jogl.*;

import gov.anl.ipns.MathTools.Geometry.*;
import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;
import SSG_Tools.Cameras.*;
import SSG_Tools.SSG_Nodes.*;

/** 
 *  This class draws a solid box of the specified width, height and depth
 *  in the specified orientation at the specified point.
 */

public class PositionedBox extends SimpleShape
{
  Vector3D  center;
  Vector3D  base;
  Vector3D  up;
  Vector3D  extents;

  /* --------------------------- constructor --------------------------- */
  /**
   */
  public PositionedBox( Vector3D center, Vector3D base, Vector3D up,
                        Vector3D extents,
                        Color    new_color)
  {
    super( new_color );
    this.center  = center;
    this.base    = base;
    this.up      = up;
    this.extents = extents; 
    color[0] = new_color.getRed()/255f;
    color[1] = new_color.getGreen()/255f;
    color[2] = new_color.getBlue()/255f;
  }

  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this box to the specified drawable.
   *
   *  @param  drawable  The drawable on which the box is to be drawn.
   */
  public void Render( GLDrawable drawable )
  {
    preRender( drawable );                 // handle name stack 

    GL gl = drawable.getGL();
 
    Vector3D v[][][] = new Vector3D[2][2][2];
    for ( int page = 0; page < 2; page++ )
      for ( int row = 0; row < 2; row++ )
        for ( int col = 0; col < 2; col++ )
          v[row][col][page] = new Vector3D( center );

    Vector3D dx = new Vector3D( base );
    Vector3D dy = new Vector3D( up );
    Vector3D dz = new Vector3D();
    dz.cross( base, up );
    dx.normalize();
    dy.normalize();
    dz.normalize();

    Vector3D front_norm  = new Vector3D( dz );
    Vector3D right_norm  = new Vector3D( dx );
    Vector3D top_norm    = new Vector3D( dy );
    Vector3D back_norm   = new Vector3D( dz );
    Vector3D left_norm   = new Vector3D( dx );
    Vector3D bottom_norm = new Vector3D( dy );
    back_norm.multiply(-1);
    left_norm.multiply(-1);
    bottom_norm.multiply(-1);
   
    float sizes[] = extents.get();
    dx.multiply( sizes[0]/2 );
    dy.multiply( sizes[1]/2 );
    dz.multiply( sizes[2]/2 );

    for ( int row = 0; row < 2; row++ )
      for ( int col = 0; col < 2; col++ )
      {
        v[col][row][0].subtract(dz);
        v[col][row][1].add(dz);
      }

    for ( int row = 0; row < 2; row++ )
      for ( int page = 0; page < 2; page++ )
      {
        v[0][row][page].subtract(dx);
        v[1][row][page].add(dx);
      }

    for ( int col = 0; col < 2; col++ )
      for ( int page = 0; page < 2; page++ )
      {
        v[col][0][page].subtract(dy);
        v[col][1][page].add(dy);
      }

    gl.glBegin( GL.GL_QUADS );
      gl.glColor3fv( color );
      gl.glNormal3fv( front_norm.get() );          // front face
      gl.glVertex3fv( v[0][0][1].get() );
      gl.glVertex3fv( v[1][0][1].get() );
      gl.glVertex3fv( v[1][1][1].get() );
      gl.glVertex3fv( v[0][1][1].get() );

      gl.glNormal3fv( right_norm.get() );           // right face
      gl.glVertex3fv( v[1][0][1].get() );
      gl.glVertex3fv( v[1][0][0].get() );
      gl.glVertex3fv( v[1][1][0].get() );
      gl.glVertex3fv( v[1][1][1].get() );

      gl.glNormal3fv( back_norm.get()  );           // back face
      gl.glVertex3fv( v[0][1][0].get() );
      gl.glVertex3fv( v[1][1][0].get() );
      gl.glVertex3fv( v[1][0][0].get() );
      gl.glVertex3fv( v[0][0][0].get() );

      gl.glNormal3fv( left_norm.get()  );          // left face
      gl.glVertex3fv( v[0][1][1].get() );
      gl.glVertex3fv( v[0][1][0].get() );
      gl.glVertex3fv( v[0][0][0].get() );
      gl.glVertex3fv( v[0][0][1].get() );

      gl.glNormal3fv( top_norm.get()   );           // top face
      gl.glVertex3fv( v[0][1][1].get() );
      gl.glVertex3fv( v[1][1][1].get() );
      gl.glVertex3fv( v[1][1][0].get() );
      gl.glVertex3fv( v[0][1][0].get() );

      gl.glNormal3fv( bottom_norm.get());        // bottom face
      gl.glVertex3fv( v[0][0][0].get() );
      gl.glVertex3fv( v[1][0][0].get() );
      gl.glVertex3fv( v[1][0][1].get() );
      gl.glVertex3fv( v[0][0][1].get() );

    gl.glEnd();

    postRender( drawable );              // clean up after drawing
  }

  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
    Vector3D center = new Vector3D( 0, 0, 0 );
    Vector3D base   = new Vector3D( 1, 0, 0 ); 
    Vector3D up     = new Vector3D( 0, 1, 0 ); 
    Vector3D extent = new Vector3D( 1, 2, 3 ); 

    Node box = new PositionedBox( center, base, up, extent, Color.BLUE ); 
    JoglPanel demo = new JoglPanel( box, false );
    
    Camera camera = demo.getCamera();
    camera.setVRP( new Vector3D(0,0,0) );
    camera.setCOP( new Vector3D(0,0,5) );
    new MouseArcBall( demo );

    JFrame frame = new JFrame( "SolidBox Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible(true);
  }

}

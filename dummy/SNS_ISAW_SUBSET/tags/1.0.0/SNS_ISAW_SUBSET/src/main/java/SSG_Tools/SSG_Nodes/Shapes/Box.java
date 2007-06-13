/*
 * File:  Box.java
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
 * $Log: Box.java,v $
 * Revision 1.5  2006/07/20 16:00:42  dennis
 * Updated from CVS repository at isaw.mscs.uwstout.edu
 * Added methods to get the dimensions of the box.
 *
 * Revision 1.4  2005/10/25 03:15:26  dennis
 * Added "getter" methods to get the width, height and depth of the box.
 *
 * Revision 1.3  2004/11/15 19:18:35  dennis
 * Added tag for logging.
 *
 */

package SSG_Tools.SSG_Nodes.Shapes;

import net.java.games.jogl.*;
import javax.swing.*;

import SSG_Tools.Viewers.*;

/** 
 *  This class draws a 3D wire frame box of the specified width, height and
 *  depth centered at the origin.
 */

public class Box extends Shape
{
  float width;                // The dimensions of this box in x,y,z directions
  float height;
  float depth;

  /* --------------------------- constructor --------------------------- */
  /**
   *  Construct a box object with the specified width, height and depth
   *
   *  @param  width    The total width  of the box in the "x" direction.
   *  @param  height   The total height of the box in the "y" direction.
   *  @param  depth    The total depth  of the box in the "z" direction.
   */
  public Box( float width, float height, float depth )
  {
    this.width  = width;
    this.height = height;
    this.depth  = depth;
  }


  /* ---------------------------- getWidth ---------------------------- */
  /**
   *  Get the width of this box
   *
   *  @return the width of the box that was set when the box was constructed
   */
  public float getWidth()
  {
    return width;
  }


  /* ---------------------------- getHeight ---------------------------- */
  /**
   *  Get the height of this box
   *
   *  @return the height of the box that was set when the box was constructed
   */
  public float getHeight()
  {
    return height;
  }


  /* ---------------------------- getDepth ---------------------------- */
  /**
   *  Get the depth of this box
   *
   *  @return the depth of the box that was set when the box was constructed
   */
  public float getDepth()
  {
    return depth;
  }


  /* ------------------------------ Render ----------------------------- */
  /**
   *  Render this box to the specified drawable.
   *
   *  @param  drawable  The drawable on which the box is to be drawn.
   */
  public void Render( GLDrawable drawable )
  {
    preRender( drawable );                // handle name stack and appearance
  	
    GL gl = drawable.getGL();

    gl.glBegin( GL.GL_LINE_LOOP );                           // back face
      gl.glVertex3f( -width/2, -height/2, -depth/2 ); 
      gl.glVertex3f( -width/2,  height/2, -depth/2 ); 
      gl.glVertex3f(  width/2,  height/2, -depth/2 ); 
      gl.glVertex3f(  width/2, -height/2, -depth/2 ); 
    gl.glEnd();
                                                           
    gl.glBegin( GL.GL_LINE_LOOP );                           // front face
      gl.glVertex3f( -width/2, -height/2,  depth/2 ); 
      gl.glVertex3f( -width/2,  height/2,  depth/2 ); 
      gl.glVertex3f(  width/2,  height/2,  depth/2 ); 
      gl.glVertex3f(  width/2, -height/2,  depth/2 ); 
    gl.glEnd();
                                                             // front to back
    gl.glBegin( GL.GL_LINES );                               // edges
      gl.glVertex3f( -width/2, -height/2, -depth/2 ); 
      gl.glVertex3f( -width/2, -height/2,  depth/2 ); 

      gl.glVertex3f( -width/2,  height/2, -depth/2 ); 
      gl.glVertex3f( -width/2,  height/2,  depth/2 ); 

      gl.glVertex3f(  width/2,  height/2, -depth/2 ); 
      gl.glVertex3f(  width/2,  height/2,  depth/2 ); 

      gl.glVertex3f(  width/2, -height/2, -depth/2 ); 
      gl.glVertex3f(  width/2, -height/2,  depth/2 ); 
    gl.glEnd();
    
    postRender( drawable );             // clean up, after rendering
  }

  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
    JoglPanel demo = new JoglPanel( new Box( 2, 2, 4 ) );

    JFrame frame = new JFrame( "Box Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

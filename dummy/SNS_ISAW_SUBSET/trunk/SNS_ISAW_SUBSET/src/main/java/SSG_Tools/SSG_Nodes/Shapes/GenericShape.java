/*
 * File:  GenericShape.java
 *
 * Copyright (C) 2006, Dennis Mikkelson
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
 * $Log: GenericShape.java,v $
 * Revision 1.1  2007/08/14 00:37:32  dennis
 * Added files from SSG_Tools at UW-Stout
 *
 * Revision 1.2  2006/10/16 04:05:28  dennis
 * Simplified main test program.
 *
 * Revision 1.1  2006/10/15 05:04:12  dennis
 * Initial version of generic shape object that uses a separate
 * Geometry object to define the shape, normals and texture coordinates.
 *
 */

package SSG_Tools.SSG_Nodes.Shapes;

import javax.media.opengl.*;
import java.awt.Color;
import javax.swing.*;

import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;
import SSG_Tools.Appearance.*;
import SSG_Tools.Geometry.*;

/** 
 *  A GenericShape object is a Shape object that delegates the work of
 *  drawing the object with normals and texture coordinates to a Geometry
 *  object.  
 */

public class GenericShape extends Shape
{
  private Geometry geometry;


  /* --------------------------- constructor --------------------------- */
  /**
   *  Construct a GenericShape object using the specified Geometry and 
   *  Appearance.  If no special appearance is required, appearance can
   *  be passed in as null.
   *
   *  @param  geometry    The geometry object for this Shape. This must
   *                      not be null, or no object will be drawn.
   *  @param  appearance  The appearance object for this shape.  This may
   *                      be null and the default object appearance will
   *                      used.
   */
  public GenericShape( Geometry geometry, Appearance appearance )
  {
    this.geometry = geometry;
    setAppearance( appearance );
  }


  /* ---------------------------- getGeometry -------------------------- */
  /**
   *  Get a reference to the Geometry object for this shape.
   *
   *  @return a reference to this shape's Geometry object.
   */
  public Geometry getGeometry()
  {
    return geometry;
  }


  /* ------------------------------ Render ----------------------------- */
  /**
   *  Draw this Shape on the specified drawable.
   *
   *  @param  drawable  The drawable where the Shape should be drawn. 
   */
  public void Render( GLAutoDrawable drawable )
  {
    if ( geometry != null )
    {
      preRender( drawable );                // handle name stack and appearance
      geometry.Render( drawable );
      postRender( drawable );               // clean up, after rendering
    }
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
    Geometry sphere_geometry = new SphereGeometry( 1, 20, 20 );

    Material material = new Material( Color.WHITE );
    Appearance appearance = new Appearance( material );

    GenericShape sphere = new GenericShape( sphere_geometry, appearance );

    JoglPanel demo = new JoglPanel( sphere );
    new MouseArcBall( demo );
    
    JFrame frame = new JFrame( "GenericShape Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

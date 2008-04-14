/*
 * File:  StateTests.java
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
 *  $Log: StateTests.java,v $
 *  Revision 1.2  2007/08/26 23:23:20  dennis
 *  Updated to latest version from UW-Stout repository.
 *
 *  Revision 1.5  2007/08/25 03:53:27  dennis
 *  Uncommented some test code.
 *
 *  Revision 1.4  2007/08/24 17:28:35  dennis
 *  Removed unused imports.
 *
 *  Revision 1.3  2006/12/11 19:51:54  dennis
 *  Cleaned up the set of basic tests a bit, so it is now easy to
 *  enable different nodes by removing some comment symbols.
 *
 *  Revision 1.2  2006/12/11 02:19:47  dennis
 *  Added a few more crude tests for state controls, such as
 *  glColorNode.
 *
 *  Revision 1.1  2006/11/27 00:10:02  dennis
 *  Initial version added to CVS repository
 */

package SSG_Tools.SSG_Nodes.StateControls;

import java.awt.*;
import javax.swing.*;
import javax.media.opengl.*;

import SSG_Tools.SSG_Nodes.SimpleShapes.*;
import SSG_Tools.SSG_Nodes.Shapes.*;
import SSG_Tools.SSG_Nodes.*;
import SSG_Tools.SSG_Nodes.Groups.Transforms.*;
import gov.anl.ipns.MathTools.Geometry.*;
import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;

import SSG_Tools.Cameras.*;
import SSG_Tools.Geometry.*;

/**
 *  This class includes some basic tests of the state setting nodes.
 */
public class StateTests
{

  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that constructs an instance of Parallelogram and displays
   *  it in 3D for testing purposes.
   */
  public static void main( String args[] )
  {
    Group scene = new Group(); 
                                            // Make some examples of state
                                            // control nodes to place in the
                                            // scene graph. 
                                            // Node to set default color for
                                            // later shapes.
    glColorNode yellow = new glColorNode( Color.YELLOW, 1 );
    glColorNode green  = new glColorNode( Color.GREEN, 1 );

                                            // Node to switch to drawing 
                                            // polygons using only lines
    glPolygonModeNode lines_only = new glPolygonModeNode( GL.GL_FRONT_AND_BACK,
                                                          GL.GL_LINE );

                                            // nodes to control which face is
                                            // front face. CCW is default
    glFrontFaceNode clockwise         = new glFrontFaceNode( GL.GL_CW );
//  glFrontFaceNode counter_clockwise = new glFrontFaceNode( GL.GL_CCW );

                                            // nodes to turn lighting on/off
    glDisableNode light_off = new glDisableNode( GL.GL_LIGHTING );
    glEnableNode  light_on  = new glEnableNode( GL.GL_LIGHTING );

                                            // nodes to turn back face culling
                                            // on/off
    glEnableNode  cull_on  = new glEnableNode( GL.GL_CULL_FACE );
    glDisableNode cull_off = new glDisableNode( GL.GL_CULL_FACE );

                                            // Shapes for the scene graph.

                                            // First make some coordinate axes
                                            // These are simple shapes that 
                                            // should not be lighted!
    Vector3D xmin = new Vector3D( 0, 0, 0 );
    Vector3D xmax = new Vector3D( 6, 0, 0 );
    Vector3D ymin = new Vector3D( 0, 0, 0 );
    Vector3D ymax = new Vector3D( 0, 6, 0 );
    Vector3D zmin = new Vector3D( 0, 0, 0 );
    Vector3D zmax = new Vector3D( 0, 0, 6 );

    Axis x_axis = Axis.getInstance( xmin, xmax, "X-Axis", Color.RED );
    Axis y_axis = Axis.getInstance( ymin, ymax, "Y-Axis", Color.GREEN );
    Axis z_axis = Axis.getInstance( zmin, zmax, "Z-Axis", Color.BLUE );

                                            // Next make a few Parallelograms 
                                            // that don't specify their color.
    Parallelogram par_1 = new Parallelogram( new Vector3D( 0,0,0 ),
                                             new Vector3D( 1,0,0 ),
                                             new Vector3D( 0,1,0 ),
                                             null );

    Parallelogram par_2 = new Parallelogram( new Vector3D( 0,0,0 ),
                                             new Vector3D( 1,0,0 ),
                                             new Vector3D( 0,1,0 ),
                                             null );

                                            // Finally, make a sphere that 
                                            // doesn't specifiy its color
    SphereGeometry sphere_geometry   = new SphereGeometry( 0.5f, 15, 15 );
    GenericShape   sphere = new GenericShape( sphere_geometry, null );

                                            // Translations for the second
                                            // parallelogram and sphere
    Translate tran_1 = new Translate( new Vector3D( 1.5f, 0, 0 ) );
    Translate tran_2 = new Translate( new Vector3D( 3f, 0, 0 ) );

                                          // now link the scene graph together
    scene.addChild( light_off );          // turn of lighting for axes
      scene.addChild( x_axis );
      scene.addChild( y_axis );
      scene.addChild( z_axis );
    scene.addChild( light_on );

    scene.addChild( lines_only );       // control drawing of polygons
    scene.addChild( clockwise );        // change front face to clockwise
    scene.addChild( cull_on );          // turn culling off

    scene.addChild( par_1 );

    scene.addChild( cull_off );           // optionally turn off culling for
                                          // the next parallelogram 

    scene.addChild( green );              // make parallelogram 2 yellow
    scene.addChild( tran_1 );
      tran_1.addChild( par_2 );

    scene.addChild( yellow );             // make sphere yellow
    scene.addChild( tran_2 );
      tran_2.addChild( sphere );

    JoglPanel demo = new JoglPanel( scene );
    demo.enableHeadlight( true );
    new MouseArcBall( demo );

    Camera camera = demo.getCamera();
    camera.setVRP( new Vector3D( 0,0, 0 ) );
    camera.setCOP( new Vector3D( 0, 0, 10 ) );
    camera.SetViewVolume( 1, 20, 40 );
    demo.setCamera( camera );

    JFrame frame = new JFrame( "Parallelogram" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}

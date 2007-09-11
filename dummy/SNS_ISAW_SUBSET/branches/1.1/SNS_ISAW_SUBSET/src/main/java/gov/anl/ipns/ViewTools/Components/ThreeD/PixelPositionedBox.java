/*
 * File:  PixelPositionedBox.java
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
 * This work was supported by the University of Tennessee Knoxville and 
 * the Spallation Neutron Source at Oak Ridge National Laboratory under: 
 *   Support of HFIR/SNS Analysis Software Development 
 *   UT-Battelle contract #:   4000036212
 *   Date:   Oct. 1, 2004 - Sept. 30, 2006
 *
 * Modified:
 *
 * $Log: PixelPositionedBox.java,v $
 * Revision 1.3  2006/08/10 15:01:28  dennis
 * Added basic test in main().
 *
 * Revision 1.2  2005/08/04 22:36:46  cjones
 * Updated documentation and comment header.
 *
 * Revision 1.1  2005/07/27 20:36:42  cjones
 * Added menu item that allows the user to choose between different shapes
 * for the pixels. Also, in frames view, user can change the time between
 * frame steps.
 *
 *
 */

package gov.anl.ipns.ViewTools.Components.ThreeD;

import javax.swing.*;
import java.awt.Color;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import SSG_Tools.SSG_Nodes.SimpleShapes.*;
import SSG_Tools.SSG_Nodes.*;
import SSG_Tools.Cameras.*;
import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;


/** 
 *  This class draws a solid box of the specified width, height and depth
 *  in the specified orientation at the specified point.
 * It holds an id and value for the pixel it represents.
 */

public class PixelPositionedBox extends PositionedBox 
                                implements IPixelShape
{
  private int PixelID = -1;
  private float PixelVal = 0;

  /* --------------------------- constructor --------------------------- */
  
  /**
   * This creates a positioned, colored box for for the pixel.
   * 
   *   @param id        The id of the pixel.
   *   @param base      The base vector of the box.
   *   @param up        The up vector of the box.
   *   @param extents   The width of each dimension of the box.
   *   @param new_color The color of the box.
   */
  public PixelPositionedBox( int id, Vector3D center, 
  		                     Vector3D base, Vector3D up,
                             Vector3D extents,
                             Color    new_color)
  {
    super( center, base, up, extents, new_color );

    PixelID = id;
  }

  /**
   * Return pixel's id.
   *
   *    @return Pixel id
   */
  public int getPixelID()
  {
  	return PixelID;
  }
  
  /**
   * Set the current value of the pixel.
   * 
   * @param value Value of pixel.
   */
  public void setValue(float value)
  {
  	PixelVal = value;
  }
  
  /**
   * Return pixel's value.
   *
   * @return value of pixel
   */
  public float getValue()
  {
  	return PixelVal;
  }

  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
    int id = 1;
    Vector3D center = new Vector3D( 0, 0, 0 );
    Vector3D base   = new Vector3D( 1, 0, 0 );
    Vector3D up     = new Vector3D( 0, 1, 0 );
    Vector3D extent = new Vector3D( 1, 2, 3 );

    Node box = new PixelPositionedBox( id, 
                                       center, base, up, extent, 
                                       Color.RED );
    JoglPanel demo = new JoglPanel( box, false );

    Camera camera = demo.getCamera();
    camera.setVRP( new Vector3D(0,0,0) );
    camera.setCOP( new Vector3D(0,0,5) );
    new MouseArcBall( demo );

    JFrame frame = new JFrame( "PixelPositionedBox Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible(true);
  }

}

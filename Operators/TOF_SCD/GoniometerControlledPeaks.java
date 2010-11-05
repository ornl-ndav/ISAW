/* 
 * File: GoniometerControlledPeaks.java
 *
 * Copyright (C) 2010, Dennis Mikkelson
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

package Operators.TOF_SCD;

import javax.media.opengl.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

import MessageTools.*;

import SSG_Tools.MathTools.*;

import SSG_Tools.Viewers.*;
import SSG_Tools.Viewers.Controls.*;

import SSG_Tools.Geometry.*;
import SSG_Tools.SSG_Nodes.*;
import SSG_Tools.SSG_Nodes.StateControls.*;
import SSG_Tools.SSG_Nodes.Shapes.*;
import SSG_Tools.SSG_Nodes.SimpleShapes.*;
import SSG_Tools.SSG_Nodes.Groups.Transforms.*;
import SSG_Tools.Cameras.*;

import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This class plots the peak positions from two peaks files in 3D and
 *  provides controls, allowing the user to specify goniomater rotations
 *  phi, chi and omega.  Changing the phi, chi and omega values will rotate
 *  the peaks by the INVERSE of the rotation specified.  If the data and
 *  and goniometer values correspond properly, the peaks from both files
 *  will line up.
 */
public class GoniometerControlledPeaks extends Group
{
  private ControlledPeaks  peaks_1 = null;
  private ControlledPeaks  peaks_2 = null;

  private MessageCenter message_center;

  /* --------------------------- constructor ------------------------------- */
  /**
   *  Build the scene graph with the peaks set(s), a message center
   *  and goniometer controls.
   */
  public GoniometerControlledPeaks( String file_1, String file_2 ) 
         throws IOException
  {

    Vector3D origin = new Vector3D( 0, 0, 0 );
    Vector3D x_dir  = new Vector3D( 1, 0, 0 );
    Vector3D y_dir  = new Vector3D( 0, 1, 0 );
    String   x_name = "Qx(beam)";
    String   y_name = "Qy";
    String   z_name = "Qz(vertical)";
    float    x_min  = -20;
    float    x_max  = +20;
    float    y_min  = -20;
    float    y_max  = +20;
    float    z_min  = -20;
    float    z_max  = +20;
    Color    x_color = Color.RED;
    Color    y_color = Color.GREEN;
    Color    z_color = Color.BLUE;
    CalibratedAxes axes = new CalibratedAxes( origin, x_dir, y_dir,
                                 x_name, x_min, x_max, x_color,
                                 y_name, y_min, y_max, y_color,
                                 z_name, z_min, z_max, z_color );

    addChild( axes );

    message_center = new MessageCenter( "GoniometerMessages" );

    addChild( new glDisableNode( GL.GL_LIGHTING ) );
    peaks_1 = new ControlledPeaks( file_1, Color.RED );
    peaks_1.connectTo( message_center );
    System.out.println("Called connectTo for peaks_1");

    addChild( peaks_1 );

    GoniometerControls controls1 
                           = new GoniometerControls( message_center, file_1 );

    GoniometerControls controls2 = null;
    if ( file_2 != null && file_2.length() > 0 )
    {
      peaks_2 = new ControlledPeaks( file_2, Color.GREEN );
      peaks_2.connectTo( message_center );

      addChild( peaks_2 );
      controls2 = new GoniometerControls( message_center, file_2 );
    }

    JFrame frame = new JFrame( "Specify Goniometer Angles to INVERT Rotation" );
    frame.setSize(300,500);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().setLayout( new GridLayout( 2, 1 ) );
    frame.getContentPane().add( controls1 );
    if ( controls2 != null )
      frame.getContentPane().add( controls2 );
    frame.setVisible( true );
  }

  /**
   *  This method creates an instance of the class GoniometerControlledPeaks
   *  so that the user can view the two peaks files and verify that specified
   *  goniometer angles are correct.
   *
   *  @param file1   Fully qualified name of the first peaks file.
   *  @param file2   Fully qualified name of the second peaks file.
   *
   */
  public static void ShowRotatedPeaks( String file1, String file2 )
                     throws IOException
  {
    int FRAME_TIME = 30;

    GoniometerControlledPeaks scene=new GoniometerControlledPeaks(file1,file2);
    JoglPanel demo = new JoglPanel( scene );

    Camera camera = new OrthographicCamera();
    camera.SetView( new Vector3D(-20,0,5),
                    new Vector3D(0,0,0),
                    new Vector3D(0,0,1) );
    demo.setCamera( camera );

    new MouseArcBall( demo );



    JFrame frame = new JFrame( "Goniometer Controlled Peaks" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );

    IUpdate updateables[] = { demo };
    new UpdateManager( scene.message_center, updateables, FRAME_TIME );
  }


  /* ----------------------------- main --------------------------------- */
  /**
   *  Test code for the ShowRotatedPeaks method. 
   */
  public static void main( String args[] ) throws IOException
  {
    String file1 = "/usr2/TOPAZ_17/TOP_1612_ev.peaks";
    String file2 = "/usr2/TOPAZ_17/TOP_1613_ev.peaks";

    ShowRotatedPeaks( file1, file2 );
  }

}


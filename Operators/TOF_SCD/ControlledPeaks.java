/*
 * File ControlledPeaks.java
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
 *
 */

package Operators.TOF_SCD;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.io.*;

import MessageTools.*;
import SSG_Tools.Viewers.*;

import SSG_Tools.Viewers.Controls.*;
import SSG_Tools.SSG_Nodes.*;
import SSG_Tools.SSG_Nodes.Groups.Transforms.*;
import gov.anl.ipns.MathTools.Geometry.*;
import SSG_Tools.SSG_Nodes.Shapes.Box;
import SSG_Tools.SSG_Nodes.SimpleShapes.*;

import DataSetTools.operator.Generic.TOF_SCD.*;

/**
 *  This class forms a small scene graph to display a list of SCD peaks 
 *  in 3D.  The inverse goniometer rotation of the peaks is set based
 *  on messages received specifying goniometer rotations.
 */
public class ControlledPeaks extends    Group
                             implements IReceiveMessage
{
  /**
   *  String names for control commands are composed from a prefix 
   *  the peak set name, followed by one of PHI, CHI and OMEGA to
   *  specify which angle is being set. 
   */
  public static final String PHI   = "PHI";
  public static final String CHI   = "CHI";
  public static final String OMEGA = "OMEGA";

  private String name;
  private Rotate first_rotation;
  private Rotate middle_rotation;
  private Rotate last_rotation;

  private MessageCenter message_center = null;

  /* --------------------------- constructor ------------------------------- */
  /**
   *  Build the sub-scene graph with the peaks set and trasforms, that
   *  will listen to the specified message center.
   */
  public ControlledPeaks( String file, Color color ) throws IOException
  {
    System.out.println("ControlledPeaks called for " + file );
    name = file;
                             // This is using IPNS coordinates, where the Z axis
                             // points vertically upward and the X axis points
                             // in the direction of neutron travel in the beam.
    first_rotation  = new Rotate( 0, new Vector3D(0,0,1) );
    middle_rotation = new Rotate( 0, new Vector3D(1,0,0) );
    last_rotation   = new Rotate( 0, new Vector3D(0,0,1) );

                              // NOTE: The scene graph will cause OpenGL to
                              //       form the matrix product LMF, where
                              //       L, M and F are the last, middle and
                              //       first rotations.  The effect of the
                              //       product of these rotation on a peak, v,
                              //       is LMFv = L(M(Fv)), so conceptually,
                              //       The F rotation is done first, followed
                              //       by M and L in that order.
    addChild( last_rotation );
      last_rotation.addChild( middle_rotation );
        middle_rotation.addChild( first_rotation );

    if ( file == null )
      first_rotation.addChild( new Box(1,2,3) );
    else
    {
      Vector<Peak_new>peaks = Peak_new_IO.ReadPeaks_new( file );
      Vector3D[] verts = new Vector3D[ peaks.size() ];
      for ( int i = 0; i < peaks.size(); i++ )
      {
         float[] qxyz = peaks.elementAt(i).getQ();
         for ( int j = 0; j < 3; j++ )
            qxyz[j] *= (float)Math.PI*2;
         verts[i] = new Vector3D(qxyz); 
      }
      Polymarker peak_points = new Polymarker(verts, 3, Polymarker.DOT, color);
      first_rotation.addChild( peak_points );
    }
  }


  /* -------------------------- connectTo --------------------------------- */
  /**
   *  Add this robot as a receiver for messages from all of the relevant 
   *  queues on the specified message center.
   *
   *  @param message_center  The message center the robot is to receive
   *                         messages from.
   */
  public void connectTo( MessageCenter message_center )
  {
    System.out.println( "connectTo called" );
    String queues[] = MakeListOfQueues();
    for ( int i = 0; i < queues.length; i++ )
    {
      System.out.println("Connecting to " + queues[i]);
      message_center.addReceiver( this, queues[i] );
    }
  }


  /* ----------------------- disconnectFrom ------------------------------ */
  /**
   *  Remove this robot as a receiver for messages from all of the relevant 
   *  queues on the specified message center.
   *
   *  @param message_center  The message center the robot is to no longer
   *                         receive messages from.
   */
  public void disconnectFrom( MessageCenter message_center )
  {
    String queues[] = MakeListOfQueues();
    for ( int i = 0; i < queues.length; i++ )
      message_center.removeReceiver( this, queues[i] );
  }


  /* ----------------------- MakeListOfQueues ---------------------------- */
  /**
   *  Convenience method to construct the list of queues that this robot
   *  should listen to.
   *
   *  @return  an array of Strings containing the queue names that this
   *           robot receives messages from.
   */
  private String[] MakeListOfQueues()
  {
    String list[] = new String[3];

    list[0] = name + " " + PHI;
    list[1] = name + " " + CHI;
    list[2] = name + " " + OMEGA;

    return list;
  }


  /* --------------------------- receive ------------------------------- */
  /**
   *  Accept and process a command message for this robot.
   *
   *  @param message  The command message to be processed.
   *
   *  @return  Return true if the message was processed and false if there
   *           was something wrong with the message, that prevented it from
   *           being processed.
   */
  public boolean receive( Message message )
  {
    String command = (String)message.getName();

    float angle = (Float)message.getValue();

    angle = (float)( angle * Math.PI/180 );

           // NOTE: If the goniometer rotation of the crystal (and diffraction
           //       pattern) is represented by the product of rotation matrices
           //       RoRcRp, then the inverse rotation obtained by the product
           //       of rotations by the NEGATIVE angles, in the reverse order.
           //       That is, the role of the omega and phi rotations are 
           //       reversed AND all angles are negated !         
    boolean do_inverse_rotation = true;
    if ( do_inverse_rotation )
    {
      if ( command.endsWith(PHI) )
        last_rotation.setAngle( -angle );

      else if ( command.endsWith(CHI) )
        middle_rotation.setAngle( -angle );

      else if ( command.endsWith(OMEGA) )
        first_rotation.setAngle( -angle );
    }
    else
    {
      if ( command.endsWith(PHI) )
        first_rotation.setAngle( angle );

      else if ( command.endsWith(CHI) )
        middle_rotation.setAngle( angle );

      else if ( command.endsWith(OMEGA) )
        last_rotation.setAngle( angle );
    }
    return true;
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] ) throws IOException
  {   
    String name = "/usr2/TOPAZ_15/ORTHO_1507.peaks";
    ControlledPeaks test_peaks = new ControlledPeaks( name, Color.RED );

    Vector3D origin = new Vector3D( 0, 0, 0 );
    Vector3D x_dir  = new Vector3D( 1, 0, 0 );
    Vector3D y_dir  = new Vector3D( 0, 1, 0 );
    String   x_name = "Qx";
    String   y_name = "Qy";
    String   z_name = "Qz";
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
    test_peaks.addChild( axes );

    JoglPanel demo = new JoglPanel( test_peaks );
    new MouseArcBall( demo );

    MessageCenter message_center = new MessageCenter("Test Center");
    message_center.addReceiver( test_peaks, name + " PHI" );
    message_center.addReceiver( test_peaks, name + " CHI" );
    message_center.addReceiver( test_peaks, name + " OMEGA" );

    JFrame frame = new JFrame( "Controlled Peaks" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );

    GoniometerControls controls = 
                           new GoniometerControls( message_center, name );
    
    JFrame control_frame = new JFrame( "Controls" );
    control_frame.setSize(300,300);
    control_frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    control_frame.getContentPane().add( controls );
    control_frame.setVisible( true );

    int FRAME_TIME = 30;
    IUpdate[] updateables = { demo };
    new UpdateManager( message_center, updateables, FRAME_TIME );
/*
    TestReceiver test_rec = new TestReceiver( "Test Receiver" ); 
    message_center.addReceiver( test_rec, 
                        message_center.getProcessCompleteQueueName() );
    message_center.addReceiver( test_rec, name + " PHI" );
*/
  }
}



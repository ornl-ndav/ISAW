/*
 * File:  MouseArcBall.java
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
 *  $Log: MouseArcBall.java,v $
 *  Revision 1.4  2006/07/25 13:01:05  dennis
 *  Replaced call to deprecated method show() with
 *  call to setVisible(true).
 *
 *  Revision 1.3  2006/07/20 14:28:58  dennis
 *  Added small main program for testing.
 *
 *  Revision 1.2  2005/08/02 14:46:01  dennis
 *  Removed unused imports.
 *
 *  Revision 1.1  2005/07/25 15:33:22  dennis
 *  This class uses mouse events to control the view displayed in a
 *  JoglPanel.  If dragged with button 1 down, an "arc ball" behaviour
 *  is executed.  If dragged with button 2 down, the virtual camera will
 *  dolly in or out.  If dragged with button 3 down, the virtual camera
 *  will pan.
 *
 */

package SSG_Tools.Viewers.Controls;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import gov.anl.ipns.MathTools.Geometry.*;

import SSG_Tools.Viewers.*;
import SSG_Tools.SSG_Nodes.*;
import SSG_Tools.Cameras.*;
import SSG_Tools.SSG_Nodes.SimpleShapes.*;


/**
 *  This class listens for mouse press and drag events and calls methods
 *  from ArcBallCalc to update the camera for a JoglPanel.
 */
public class MouseArcBall extends    MouseAdapter
                          implements MouseMotionListener
{
   JoglPanel my_panel;

   boolean   last_point_valid = false;
   int       last_x = 0;   
   int       last_y = 0;   
   Vector3D  wc_point = null;

   boolean   enabled = true;


   /**
    *  Make a MouseArcBall that listens to mouse events from the specified 
    *  JoglPanel, and controls the camera from that panel.
    */
   public MouseArcBall( JoglPanel panel )
   {
      my_panel = panel;
      Component comp = panel.getDisplayComponent();
      comp.addMouseListener( this );
      comp.addMouseMotionListener( this );
   }


  /**
   *  Enable or disable this MouseArcBall.  If it is disabled, no events
   *  will be processed.
   *
   *  @param  on_off  Flag controling whether or not the MosueArcBall is
   *                  enabled to handle events.
   */
   public void setEnabled( boolean on_off )
   {
     enabled = on_off;
   }

   /**
    *  If enabled, record the coordinates from the mouse event as the
    *  "last point" and mark the last point values as valid.
    *
    *  @param  e   The mouse event
    */
   public void mousePressed (MouseEvent e)
   {
     if ( !enabled )
       return;

     last_x = e.getX();
     last_y = e.getY();

     wc_point = my_panel.pickedPoint( last_x, last_y );
     last_point_valid = true;
   }


   /**
    *  Call methods to move the camera for the JoglPanel and redraw the
    *  scene from a new point of view if the last point is valid and 
    *  different from the  current point.
    *  If BUTTON 1 is down, do the arc ball operation,
    *  if BUTTON 2 is down, do the dolly in/out operation,
    *  if BUTTON 3 is down, do the pan operation.
    *
    *  @param  e   The mouse event
    */
   public void mouseDragged (MouseEvent e)
   {
     if ( !enabled )
       return;

     if ( last_point_valid )
     {
       int x = e.getX();
       int y = e.getY();

       if ( x == last_x && y == last_y )         // nothing to do
         return;

       Component window = e.getComponent();      // ignore point outside window
       Dimension size = window.getSize();
       if ( x < 0 || x >= size.width ||
            y < 0 || y >= size.height )
         return;

       int button1 = e.getModifiers() & InputEvent.BUTTON1_MASK;
       int button2 = e.getModifiers() & InputEvent.BUTTON2_MASK;
       int button3 = e.getModifiers() & InputEvent.BUTTON3_MASK;

       if ( button1 == InputEvent.BUTTON1_MASK )
         ArcBallCalc.ArcBall( x, y, last_x, last_y, wc_point, my_panel );

       else if ( button2 == InputEvent.BUTTON2_MASK )
         ArcBallCalc.DollyInOut( x, y, last_x, last_y, my_panel );

       else if ( button3 == InputEvent.BUTTON3_MASK )
         ArcBallCalc.Pan( x, y, last_x, last_y, my_panel );

       last_x = x;
       last_y = y;
       last_point_valid = true;
     }
   }


   /**
    *  Mark the last point values as no longer valid.
    *
    *  @param  e   The mouse event
    */
   public void mouseReleased (MouseEvent e)
   {
     last_point_valid = false;
   }


   /**
    *  This is needed for the MouseLisetener interface, but has no
    *  effect here.
    *
    *  @param  e   The mouse event
    */
   public void mouseMoved (MouseEvent e)
   {
      // we ignore mouse moved without a button down 
   }

  /** ----------------------------------------------------------------------
   *
   *   Main program for testing purposes only
   *
   */
  public static void main( String args[] )
  {
    Vector3D center = new Vector3D( 0, 0, 0 );
    Vector3D base   = new Vector3D( 1, 0, 0 );
    Vector3D up     = new Vector3D( 0, 1, 0 );
    Vector3D extent = new Vector3D( 1, 2, 3 );

    Node box = new PositionedBox( center, base, up, extent, Color.BLUE );
    JoglPanel demo = new JoglPanel( box );

    Camera camera = demo.getCamera();
    camera.setVRP( new Vector3D(0,0,0) );
    camera.setCOP( new Vector3D(3,4,5) );

    new MouseArcBall( demo );

    JFrame frame = new JFrame( "SolidBox Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }


}

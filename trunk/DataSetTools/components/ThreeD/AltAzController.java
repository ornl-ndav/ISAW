/*
 * File:  AltAzController.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2001/05/23 17:36:35  dennis
 * Control view matrix using sliders that adjust the
 * altitude angle, azimuthal angle and distance from the
 * VRP to the COP.
 *
 *
 */

package DataSetTools.components.ThreeD;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import DataSetTools.math.*;

/**
 *  A ViewController object controls the ViewTransform for one or more
 *  ThreeD_JPanel objects.  Derived classes implement various interfaces to
 *  allow the user to modify the transform.
 */ 

public class AltAzController extends    ViewController
                             implements Serializable
{
  public static final int MAX_ALT_ANGLE = 89;

  JSlider  azimuth_slider;
  JSlider  altitude_slider;
  JSlider  distance_slider;


  public AltAzController()
  {
    this( 45, 45, 1, 20, 10 );
  }


  public AltAzController( float altitude, 
                          float azimuth, 
                          float min_distance,
                          float max_distance,
                          float distance  )
  {
    setLayout( new GridLayout(3,1) );

    altitude_slider = new JSlider( JSlider.HORIZONTAL, 
                                  -MAX_ALT_ANGLE, 
                                   MAX_ALT_ANGLE, 
                                   0 );
    altitude_slider.addChangeListener( new SliderChanged() );
    add( altitude_slider ); 

    azimuth_slider = new JSlider( JSlider.HORIZONTAL, -180, 180, 0 );
    azimuth_slider.addChangeListener( new SliderChanged() );
    add( azimuth_slider ); 

    distance_slider = new JSlider( JSlider.HORIZONTAL, 1, 20, 10 );
    distance_slider.addChangeListener( new SliderChanged() );
    add( distance_slider ); 

    setDistanceRange( min_distance, max_distance );
    setDistance( distance );
    setAltitudeAngle( altitude );
    setAzimuthAngle( azimuth );

    setView();
  }

  public void setDistanceRange( float min_distance, float max_distance )
  {
    if ( min_distance > max_distance )
    {
      float temp   = min_distance;
      min_distance = max_distance;
      max_distance = temp;
    }

    if ( min_distance == max_distance )
      max_distance = min_distance + 1;

    distance_slider.setMinimum( (int)(10*min_distance) );
    distance_slider.setMaximum( (int)(10*max_distance) );
  }


  public void setDistance( float distance )
  {
    distance = Math.abs(distance);
    if ( distance == 0 )
      distance = 1;
    distance_slider.setValue( (int)(10*distance) );
  }

  public void setAzimuthAngle( float degrees )
  {
    if ( degrees > 180 )
      degrees = 180;
    else if ( degrees < -180 )
      degrees = -180;

    azimuth_slider.setValue( (int)degrees );
  }

  public void setAltitudeAngle( float degrees )
  {
    if ( degrees > MAX_ALT_ANGLE )
       degrees = MAX_ALT_ANGLE;
    else if ( degrees < -MAX_ALT_ANGLE )
       degrees = -MAX_ALT_ANGLE;

    altitude_slider.setValue( (int)degrees );
  }

/* -------------------------------------------------------------------------
 *
 *  PRIVATE METHODS 
 *
 */

 private void setView()
 {
   float azimuth  = azimuth_slider.getValue();
   float altitude = altitude_slider.getValue();
   float distance = distance_slider.getValue()/10.0f;

   float r = (float)(distance * Math.cos( altitude * Math.PI/180.0 ));

   float x = (float)(r * Math.cos( azimuth * Math.PI/180.0 ));
   float y = (float)(r * Math.sin( azimuth * Math.PI/180.0 ));
   float z = (float)(distance * Math.sin( altitude * Math.PI/180.0 ));

   float vrp[] = getVRP().get();
 
   setCOP( new Vector3D( x-vrp[0], y-vrp[0], z-vrp[0] ) );

   apply();
 }
 

/* --------------------------------------------------------------------------
 *
 *  INTERNAL CLASSES 
 * 
 */
  private class SliderChanged    implements ChangeListener,
                                            Serializable
  {
     public void stateChanged( ChangeEvent e )
     {
       JSlider slider = (JSlider)e.getSource();

//     if ( !slider.getValueIsAdjusting() )
         setView();
     }
  }


/** -------------------------------------------------------------------------
 *
 *   Main program for testing purposes only
 *
 */ 
  public static void main( String args[] )
  {
    JFrame f = new JFrame("Test for AltAzController");
    f.setBounds(0,0,200,200);
    AltAzController controller = new AltAzController();
//    controller.setVirtualScreenSize( 2, 2 );
    f.getContentPane().add( controller );
    f.setVisible( true );


    JFrame window = new JFrame("Test for AltAzController");
    window.setBounds(20,20,500,500);
    ThreeD_JPanel test = new ThreeD_JPanel();
    window.getContentPane().add( test );
    window.setVisible( true );

    controller.addControlledPanel( test );
    IThreeD_Object objs[] = new IThreeD_Object[1];
    Vector3D       pts[]  = new Vector3D[4];
    pts[0] = new Vector3D( -1,  1, 0 );
    pts[1] = new Vector3D(  1,  1, 0 );
    pts[2] = new Vector3D(  1, -1, 0 );
    pts[3] = new Vector3D( -1, -1, 0 );
    objs[0] = new Polyline( pts, Color.green );
    test.setObjects( objs );
    controller.apply();
  }
 
}

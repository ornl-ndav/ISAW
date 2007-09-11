/*
 * File:  AltAzController.java
 *
 * Copyright (C) 2001-2004, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882 and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log: AltAzController.java,v $
 * Revision 1.2  2006/07/25 13:01:05  dennis
 * Replaced call to deprecated method show() with
 * call to setVisible(true).
 *
 * Revision 1.1  2005/07/25 15:35:45  dennis
 * Simple altitude, azimuth and distance control for the camera associated
 * with a panel.  The altitude and azimuth angles are treated as angles
 * of rotation of the viewer around the VRP.  The VUV is fixed to the
 * initial value when the controller is constructed.
 *
 *
 */

package SSG_Tools.Viewers.Controls;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.ViewTools.UI.*;

import SSG_Tools.Cameras.*;
import SSG_Tools.Viewers.*;
import SSG_Tools.SSG_Nodes.*;
import SSG_Tools.SSG_Nodes.SimpleShapes.*;

/**
 *  An AltAzController object controls the Observers position for one or more
 *  3D panel objects by allowing the user to specify their position 
 *  relative to the view reference point.  The position is specified by
 *  sliders controlling the altitude angle, azimuth angle and distance to
 *  the view reference point.
 */ 

public class AltAzController extends JPanel
{
  public static final float MAX_ALT_ANGLE         = 89.9f;
  public static final float MAX_AZI_ANGLE         = 180.0f; 
  public static final float ANGLE_SCALE_FACTOR    = 10.0f;
  public static final float DISTANCE_SCALE_FACTOR = 10.0f;
  
  private JSlider    azimuth_slider;
  private JSlider    altitude_slider;
  private JSlider    distance_slider;
  private JCheckBox  ortho_checkbox;

  private Vector3D   fixed_VUV;      //  we keep the view up vector fixed
  private Vector3D   fixed_VRP;      //  we keep the view up vector fixed

  private Vector3D   fixed_U;        //  orthonormal vectors, with, V parallel 
  private Vector3D   fixed_V;        //  to VUV, N from "towards" initial COP
  private Vector3D   fixed_N;           

  private Camera     camera;
  private JoglPanel  panel;


/* --------------------------- Default Constructor --------------------- */
/**
 *  Construct a controller with default values for the viewer position 
 *  and distance range, for the specified panel.
 *
 *  @param  my_panel      The JoglPanel that this controller will handle
 *
 */
  public AltAzController( JoglPanel my_panel )
  {
     this( my_panel, 45, 45, 1, 100, 10 );
  }


/* ------------------------------ Constructor -------------------------- */
/**
 *  Construct a controller with specified values for the viewer position.
 *
 *  @param  my_panel      The JoglPanel that this controller will handle
 *
 *  @param  altitude      The initial altitude angle from the VRP to the COP
 *                        in degrees.
 *  @param  azimuth       The initial azimuth angle from the VRP to the COP
 *                        in degrees.
 *  @param  min_distance  The minimum distance on the distace slider.
 *  @param  max_distance  The maximum distance on the distace slider.
 *  @param  distance      The initial distance from the VRP to the COP.
 *
 */
  public AltAzController( JoglPanel my_panel,
                          float     altitude, 
                          float     azimuth, 
                          float     min_distance,
                          float     max_distance,
                          float     distance  )
  {
    panel  = my_panel;
    camera = my_panel.getCamera();

    fixed_VUV = camera.getVUV();
    fixed_VRP = camera.getVRP();

    fixed_V = new Vector3D( fixed_VUV );
    fixed_V.normalize();

    fixed_N = camera.getN();
    fixed_U = new Vector3D();
    fixed_U.cross( fixed_V, fixed_N );
    fixed_U.normalize();

    fixed_N.cross( fixed_U, fixed_V );
    fixed_N.normalize();

    setLayout( new GridLayout(4,1) );
    TitledBorder border = new TitledBorder(
                             LineBorder.createBlackLineBorder(),"View Control");
    border.setTitleFont( FontUtil.BORDER_FONT );
    setBorder( border );

    ortho_checkbox = new JCheckBox( "Orthographic" );
    SliderChanged slider_listener = new SliderChanged();
    altitude_slider = new JSlider( JSlider.HORIZONTAL, 
                                  -(int)(ANGLE_SCALE_FACTOR*MAX_ALT_ANGLE), 
                                   (int)(ANGLE_SCALE_FACTOR*MAX_ALT_ANGLE), 
                                   0 );
    altitude_slider.addChangeListener( slider_listener );
    border = new TitledBorder( LineBorder.createBlackLineBorder(),"Altitude");
    border.setTitleFont( FontUtil.BORDER_FONT );
    altitude_slider.setBorder( border );
    add( altitude_slider ); 

    azimuth_slider = new JSlider( JSlider.HORIZONTAL, 
                                 -(int)(ANGLE_SCALE_FACTOR*MAX_AZI_ANGLE), 
                                  (int)(ANGLE_SCALE_FACTOR*MAX_AZI_ANGLE), 
                                  0 );
    azimuth_slider.addChangeListener( slider_listener );
    border = new TitledBorder( LineBorder.createBlackLineBorder(),"Azimuth");
    border.setTitleFont( FontUtil.BORDER_FONT );
    azimuth_slider.setBorder( border );
    add( azimuth_slider ); 

    distance_slider = new JSlider( JSlider.HORIZONTAL, 1, 20, 10 );
    distance_slider.addChangeListener( slider_listener );
    border = new TitledBorder( LineBorder.createBlackLineBorder(),"Distance");
    border.setTitleFont( FontUtil.BORDER_FONT );
    distance_slider.setBorder( border );
    add( distance_slider ); 

    setDistanceRange( min_distance, max_distance );
    setDistance( distance );
    setAltitudeAngle( altitude );
    setAzimuthAngle( azimuth );

    ortho_checkbox.setFont(  FontUtil.BORDER_FONT );
    ortho_checkbox.addActionListener( new ProjectionTypeListener() );
    if ( camera instanceof OrthographicCamera )
      ortho_checkbox.setSelected( true );
    else
      ortho_checkbox.setSelected( false );

    JPanel panel = new JPanel();
    panel.setLayout( new GridLayout(1,1) );
    panel.add( ortho_checkbox );
    border = new TitledBorder( LineBorder.createBlackLineBorder(),"Projection");
    border.setTitleFont( FontUtil.BORDER_FONT );
    panel.setBorder( border );
    add( panel );

    setView();    
  }


 /* --------------------------- setDistanceRange ------------------------ */
 /**
  *  Set a new distance range for the distance slider.
  *
  *  @param  min_distance  The minimum distance on the distace slider.
  *  @param  max_distance  The maximum distance on the distace slider.
  */
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

    distance_slider.setMinimum( (int)(DISTANCE_SCALE_FACTOR * min_distance) );
    distance_slider.setMaximum( (int)(DISTANCE_SCALE_FACTOR * max_distance) );
  }


 /* ------------------------------ setDistance -------------------------- */
 /**
  *  Set a new distance from the VRP to the COP for the distance slider.
  *
  *  @param  distance      The initial distance from the VRP to the COP.
  */
  public void setDistance( float distance )
  {
    distance = Math.abs(distance);
    if ( distance == 0 )
      distance = 1;
    distance_slider.setValue( (int)(DISTANCE_SCALE_FACTOR * distance) );
  }


 /* ------------------------------ getDistance -------------------------- */
 /**
  *  Get the distance from the VRP to the COP from the distance slider.
  *
  *  @return  The current distance from the VRP to the COP.
  */
  public float getDistance()
  {
    return distance_slider.getValue() / DISTANCE_SCALE_FACTOR;
  }


 /* ---------------------------- setAzimuthAngle ------------------------ */
 /**
  *  Set a new azimuth angle from the VRP to the COP for the altitude slider.
  *
  *  @param  degrees  The azimuth angle from the VRP to the COP in degrees.
  *          
  */
  public void setAzimuthAngle( float degrees )
  {
    if ( degrees > MAX_AZI_ANGLE )
      degrees = MAX_AZI_ANGLE;
    else if ( degrees < -MAX_AZI_ANGLE )
      degrees = -MAX_AZI_ANGLE;

    azimuth_slider.setValue( (int)(ANGLE_SCALE_FACTOR * degrees) );
  }


 /* ---------------------------- getAzimuthAngle ------------------------ */
 /** 
  *  Get the azimuth angle from the VRP to the COP from the altitude slider.
  *  
  *  @return The azimuth angle from the VRP to the COP in degrees.
  * 
  */
  public float getAzimuthAngle()
  {
    return azimuth_slider.getValue() / ANGLE_SCALE_FACTOR;
  }



 /* ---------------------------- setAltitudeAngle ------------------------ */
 /**
  *  Set a new altitude angle from the VRP to the COP for the altitude slider.
  *
  *  @param  degrees  The altitude angle from the VRP to the COP in degrees.
  *          
  */
  public void setAltitudeAngle( float degrees )
  {
    if ( degrees > MAX_ALT_ANGLE )
       degrees = MAX_ALT_ANGLE;
    else if ( degrees < -MAX_ALT_ANGLE )
       degrees = -MAX_ALT_ANGLE;

    altitude_slider.setValue( (int)(ANGLE_SCALE_FACTOR * degrees) );
  }


 /* ---------------------------- getAltitudeAngle ------------------------ */
 /**
  *  Get the altitude angle from the VRP to the COP from the altitude slider.
  *
  *  @return The altitude angle from the VRP to the COP in degrees.
  *
  */
  public float getAltitudeAngle()
  {
    return altitude_slider.getValue() / ANGLE_SCALE_FACTOR;
  }


/* -------------------------------- setCOP ------------------------------- */
/**
 *   Set the observer's position (i.e. the Center of Projection )
 *   for this view.  The view reference point will be adjusted as well to
 *   keep the same view direction.
 *
 *   @param  cop  The new vector to use for the observer's position.  
 */
 public void setCOP( Vector3D cop )
 {
   Vector3D old_cop = camera.getCOP();     // calculate how COP changed
   Vector3D diff = new Vector3D( cop );
   diff.subtract( old_cop );
                                           // and shift the VRP the same way
   Vector3D vrp = camera.getVRP();
   vrp.add( diff );
   camera.setVRP( vrp );

   setView();                              // new COP is calcuated in setView()
 }


/* -------------------------------- setVRP ------------------------------- */
/**
 *   Set the point the observer is looking at (i.e. the View Reference Point )
 *   for this view.  The center of projection will be adjusted as well to
 *   keep the same view direction.
 *
 *   @param  vrp  The new vector to use for the point the observer is looking
 *                at.  
 */
  public void setVRP( Vector3D vrp )
  {
    camera.setVRP( vrp );
    setView();                             // new COP is calcuated in setView()
  }


/* ---------------------------- setPerspective --------------------------- */
/**
 *  Set the projection type for this view.  
 *
 *  @param  perspective_flag  If true, make this a perspective projection,
 *                            otherwise make it an orthographic projection.
 */
  public void setPerspective( boolean perspective_flag )
  {
    ortho_checkbox.setSelected( !perspective_flag );

    if ( ortho_checkbox.isSelected() )
      camera = new OrthographicCamera( camera );
    else
      camera = new PerspectiveCamera( camera );

    panel.setCamera( camera );

    setView();
  }

/* -------------------------------------------------------------------------
 *
 *  PRIVATE METHODS 
 *
 */

/* --------------------------------- setView ---------------------------- */
/**
 *  Calculate the new COP and send a message to listeners that the
 *  observers position has been changed.
 */

 private void setView() 
 {
   float azimuth  = getAzimuthAngle();
   float altitude = getAltitudeAngle();
   float distance = getDistance();

   float r = (float)(distance * Math.cos( altitude * Math.PI/180.0 ));

   float u = (float)(r * Math.cos( azimuth * Math.PI/180.0 ));
   float n = (float)(r * Math.sin( azimuth * Math.PI/180.0 ));
   float v = (float)(distance * Math.sin( altitude * Math.PI/180.0 ));

   Vector3D du = new Vector3D( fixed_U );
   Vector3D dv = new Vector3D( fixed_V );
   Vector3D dn = new Vector3D( fixed_N );
   du.multiply( u );
   dv.multiply( v );
   dn.multiply( n );

   Vector3D cop = new Vector3D( fixed_VRP );
   cop.add( du );
   cop.add( dv ); 
   cop.add( dn );

   camera.setCOP( cop );

   panel.Draw();
 }
 

/* --------------------------------------------------------------------------
 *
 *  INTERNAL CLASSES 
 * 
 */
 
/* ---------------------------- SliderChanged ---------------------------- */

  private class SliderChanged    implements ChangeListener,
                                            Serializable
  {
     public void stateChanged( ChangeEvent e )
     {
       JSlider slider = (JSlider)e.getSource();

       TitledBorder border = (TitledBorder)slider.getBorder();
       if ( slider.equals( azimuth_slider ))
         border.setTitle( "Azimuth \u2220 " + getAzimuthAngle() + "\u00B0" );
       else if ( slider.equals( altitude_slider ))
         border.setTitle( "Altitude \u2220 " + getAltitudeAngle() + "\u00B0" );
       else if ( slider.equals( distance_slider ) )
         border.setTitle( "Distance " + getDistance() );

       setView();
     }
  }

/* ------------------------- ProjectionTypeListener ---------------------- */

  private class ProjectionTypeListener implements ActionListener, Serializable
  {
     public void actionPerformed( ActionEvent e )
     {
        setPerspective( !ortho_checkbox.isSelected() );
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
    Vector3D center = new Vector3D( 0, 0, 0 );
    Vector3D base   = new Vector3D( 1, 0, 0 );
    Vector3D up     = new Vector3D( 0, 1, 0 );
    Vector3D extent = new Vector3D( 1, 2, 3 );

    Node box = new PositionedBox( center, base, up, extent, Color.BLUE );
    JoglPanel demo = new JoglPanel( box );
    Camera camera = demo.getCamera();
    camera.setVRP( new Vector3D(0,0,0) );
    camera.setCOP( new Vector3D(0,0,5) );

    JFrame frame = new JFrame( "SolidBox Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );

    JFrame f = new JFrame("Test for AltAzController");
    f.setBounds(0,0,200,200);
    AltAzController controller = new AltAzController( demo );
    f.getContentPane().add( controller );
    f.setVisible( true );
  }
 
}

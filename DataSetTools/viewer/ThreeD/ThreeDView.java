/*
 * File:  ThreeDView.java
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.44  2008/02/29 03:56:58  dennis
 * Now checks for detector pixel or group positions that have
 * NaN coordinates, so that depth sorting is not "broken" due
 * to comparison with NaN values.  The number of NaN pixels
 * and groups are displayed for information purposes.  This
 * fixes a drawing order problem for some SNS NeXus files that
 * currently have some NaN values.
 *
 * Revision 1.43  2008/01/11 22:48:25  amoe
 * The tooltip is now turned off for the ThreeDView.
 *
 * Revision 1.42  2007/07/27 01:35:11  dennis
 * Removed redundant calculation of scale_factor from method that
 * gets the data range.
 * Now recalculates the scale_factor, when data is rebinned.
 *
 * Revision 1.41  2007/07/13 16:52:46  dennis
 * Added getDisplayComponent() method to return just the data display
 * panel without any controls.
 *
 * Revision 1.40  2007/07/13 15:51:06  dennis
 * Finished change over to using Rebinner class.  Calculation of
 * Data min/max is now handled by rebinner.  The rebinner also now
 * maintains the current XScale(s).
 *
 * Revision 1.39  2007/07/05 16:20:14  dennis
 * Split some functionality out of MakeColorList() to make new
 * methods setDataScaleFactor() and findMaxAbsDataValue().
 * NOTE: The selection of a new X-Scale from this viewer is
 *       not working in this version.
 *
 * Revision 1.38  2007/06/13 17:00:00  dennis
 * No longer precomputes all colors for all data in advance.  This trade
 * off of space for time is no longer needed since cpu speeds have
 * increased since this was first designed.  This change saves the space
 * that was allocated to the precomputed color table, and will make the
 * change to displaying attribute informaton easier.
 * Now uses Integer objects instead of String objects to identify different
 * 3D objects that are displayed.
 *
 *
 * Revision 1.37  2007/04/29 18:27:58  dennis
 * Minor formatting fix.
 * Added one explanatory comment.
 *
 * Revision 1.36  2004/07/16 18:48:06  dennis
 * Fixed improper comparison with Float.NaN
 *
 * Revision 1.35  2004/05/03 16:25:20  dennis
 * Removed unused local variables.
 *
 * Revision 1.34  2004/03/15 06:10:56  dennis
 * Removed unused import statements.
 *
 * Revision 1.33  2004/03/15 03:29:03  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.32  2003/12/15 20:19:58  dennis
 * Removed unused imports.
 *
 * Revision 1.31  2003/08/11 19:44:44  dennis
 * Detector segment orientation is now calculated from the DataGrid,
 * (if possible), rather than making individual pixels so that their
 * normal vector points towards the sample
 *
 * Revision 1.30  2003/07/09 15:08:51  dennis
 * Uses new form of DataSetXConversionsTable.showConversions()
 * method, which rebins to a specified XScale.  The conversions
 * table now shows the correct counts at a pixel, when the
 * viewer has constructed a rebinned data set.
 *
 * Revision 1.29  2003/07/05 19:17:31  dennis
 * Adapted to the new form of the setNamedColorModel() method from the
 * ImageJPanel class.
 *
 * Revision 1.28  2003/02/12 19:44:07  dennis
 * Switched to use PixelInfoList instead of SegmentInfoList
 *
 * Revision 1.27  2003/01/15 20:54:31  dennis
 * Changed to use SegmentInfo, SegInfoListAttribute, etc.
 *
 * Revision 1.26  2002/11/27 23:25:50  pfpeterson
 * standardized header
 *
 * Revision 1.25  2002/08/02 15:34:00  dennis
 * improved handling of POINTED_AT_CHANGED messages
 *
 * Revision 1.24  2002/07/24 23:20:36  dennis
 * Now updates X-Conversions table when POINTED_AT_CHANGED
 * message is received.
 * Now set n_bins = 0 in x_scale_ui, to default to no
 * rebinning.
 *
 * Revision 1.23  2002/07/23 18:24:37  dennis
 * Now sends/processes POINTED_AT_CHANGED messages when the
 * pointed at "X" value is changed as well as when the
 * pointed at Data block is changed.
 *
 * Revision 1.22  2002/07/12 18:49:38  dennis
 * Now traps for invalid (null) XScale from the x_scale_ui
 * and uses the XScale from the first Data block of the DataSet as
 * the default XScale.
 *
 * Revision 1.21  2002/05/30 22:57:06  chatterjee
 * Added print feature
 *
 * Revision 1.20  2002/03/18 21:14:29  dennis
 * Now calculates max_radius as 0 if there are no detector groups
 * or segments.
 *
 * Revision 1.19  2002/03/13 16:11:08  dennis
 * Converted to new abstract Data class.
 *
 */

package DataSetTools.viewer.ThreeD;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.viewer.util.*;
import DataSetTools.components.ui.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.ViewTools.Panels.Image.*;
import gov.anl.ipns.ViewTools.Panels.ThreeD.*;
import gov.anl.ipns.ViewTools.UI.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * This class provides a view of the data mapped onto the detector positions
 * in 3D.  The Data Blocks in the DataSet must have "Detector Position"
 * and/or PixelInfoList attributes for this viewer to work.
 *
 * @see DataSetTools.dataset.DataSet
 * @see DataSetTools.viewer.DataSetViewer
 *
 */

public class ThreeDView extends DataSetViewer
{                         
  private final int LOG_TABLE_SIZE      = 60000;
  private final int NUM_POSITIVE_COLORS = 127;
  private final int ZERO_COLOR_INDEX    = NUM_POSITIVE_COLORS;

  private final String GROUP_MARKER_SMALL  = "Small";
  private final String GROUP_MARKER_MEDIUM = "Medium";
  private final String GROUP_MARKER_LARGE  = "Large";
  private final String GROUP_MARKER_NONE   = "NOT DRAWN";

  private final int SMALL_MARKER_SIZE  = 2;
  private final int MEDIUM_MARKER_SIZE = 4;
  private final int LARGE_MARKER_SIZE  = 7;

  private final String DETECTOR_FILLED = "Filled";
  private final String DETECTOR_HOLLOW = "Hollow";
  private final String DETECTOR_MARKER = "Marker";
  private final String DETECTOR_NONE   = "NOT DRAWN";

  private String  group_draw_mode     = GROUP_MARKER_SMALL;
  private String  detector_draw_mode  = DETECTOR_NONE;
  private float   last_pointed_at_x   = Float.NaN;
  private float   last_pointed_at_index = -1;
  private boolean redraw_cursor       = true;
  private boolean notify_ds_observers = true;

  private ThreeD_JPanel            threeD_panel      = null; 

  private Box                      control_panel     = null; 
  private XScaleChooserUI          x_scale_ui        = null;
  private ImageJPanel              color_scale_image = null;
  private JSlider                  log_scale_slider  = new JSlider();
  private AltAzController          view_control      = null;
  private AnimationController      frame_control     = null;
  private SplitPaneWithState       split_pane        = null;
  private volatile Color           color_table[]     = null;
  private float                    log_scale[]       = null;
  private DataSetXConversionsTable conv_table        = null;
  private float                    scale_factor      =    1;
  private float                    max_abs           =    0;
  private Rebinner                 rebinner;

  private final Integer  GROUP_PREFIX    = 0;
  private final Integer  DETECTOR_PREFIX = 100000000;
  private final Integer  AXES            = 200000000;
  private final Integer  BEAM            = 300000000;
  private final Integer  INSTRUMENT      = 400000000;

  private int pixel_NaN_count = 0;
  private int group_NaN_count = 0;

  private boolean debug = false;


  int last_index = IThreeD_Object.INVALID_PICK_ID;//last picked object

/* --------------------------------------------------------------------------
 *
 * CONSTRUCTORS
 */

/* ------------------------------------------------------------------------ */

public ThreeDView( DataSet data_set, ViewerState state ) 
{
  super(data_set, state);  // Records the data_set and current ViewerState
                           // object in the parent class and then
                           // sets up the menu bar with items handled by the
                           // parent class.

  color_table = IndexColorMaker.getDualColorTable( 
                          getState().get_String( ViewerState.COLOR_SCALE ),
                          NUM_POSITIVE_COLORS );

  group_draw_mode    = getState().get_String( ViewerState.V_GROUPS );
  detector_draw_mode = getState().get_String( ViewerState.V_DETECTORS );

  setLogScale( getState().get_int( ViewerState.BRIGHTNESS ));

  if ( !validDataSet() )
    return;
  JMenuBar jmb= getMenuBar();
  gov.anl.ipns.Util.Sys.PrintComponentActionListener.setUpMenuItem( jmb, this);
  init();

  AddOptionsToMenu();
}

/* -----------------------------------------------------------------------
 *
 *  PUBLIC METHODS
 *
 */

/* ----------------------------- redraw --------------------------------- */
/**
 * Redraw all or part of the image. The amount that needs to be redrawn is
 * determined by the "reason" parameter.
 *
 * @param  reason  The reason the redraw is needed.
 *
 */
public void redraw( String reason )
{
    // This will be called by the "outside world" if the contents of the
    // DataSet are changed and it is necesary to redraw the graphs using the
    // current DataSet.
  
   if ( !validDataSet() )
     return;

   if ( reason.equals(IObserver.SELECTION_CHANGED))  // no selection display yet
     return;

   else if ( reason.equals(IObserver.POINTED_AT_CHANGED) )
   { 
      if ( threeD_panel.isDoingBox() )           // don't interrupt the zoom in 
        return;                                  // process 
     
      DataSet ds = getDataSet();
      boolean changed = false;

      if ( debug )
      {
        System.out.println("ThreeDView.redraw(), reason = " + reason );
        System.out.println("PAX  = " + ds.getPointedAtX() );
        System.out.println("last = " + last_pointed_at_x );
      }

      int index = ds.getPointedAtIndex();
     // if( Math.abs( index - last_pointed_at_index) <2 && Math.abs( last_pointed_at_x -
     //       ds.getPointedAtX( )) <.2)
    //  {
         
    //     return;
    //  }
      
      if (index != DataSet.INVALID_INDEX && ! Float.isNaN( last_pointed_at_x ))
      {
        
        float y_val = rebinner.getY_valueAtX( index, last_pointed_at_x );
        conv_table.showConversions( last_pointed_at_x, y_val, index );
      }

      if ( !Float.isNaN( ds.getPointedAtX() )   &&
           ds.getPointedAtX() != last_pointed_at_x )
      {
        
        last_pointed_at_x = ds.getPointedAtX();
        frame_control.setFrameValue( last_pointed_at_x );
        redraw_cursor = false;
      }

    //  if( !changed )
    //     return;
     // redraw_cursor = changed;
      Vector3D detector_location = group_location( ds.getPointedAtIndex() );
      Point   pixel_point;
      if ( detector_location != null && redraw_cursor )
      {
        pixel_point = threeD_panel.project( detector_location );
        threeD_panel.set_crosshair( pixel_point );
        //SwingUtilities.invokeLater(
        //      new ThreeDsetCrossHair(threeD_panel,pixel_point) );
      }

      notify_ds_observers = false;            // since we are setting the
                                              // frame controller by request,
                                              // don't notify ds observers

      redraw_cursor = true;          // just skip drawing for one notification
   }
   else if ( reason.equals( XScaleChooserUI.N_STEPS_CHANGED ) ||
             reason.equals( XScaleChooserUI.X_RANGE_CHANGED )   )
   {
     initFrameValues();
     findMaxAbsDataValue();
     setDataScaleFactor();
     set_colors( frame_control.getFrameValue() );
   }

   else                                       // really regenerate everything
   {
     initFrameValues();
     setDataScaleFactor();
     MakeThreeD_Scene();
   }
}


/* ----------------------------- setDataSet ------------------------------- */
/**
 * Specify a different DataSet to be shown by this viewer.
 *
 * @param  ds   The new DataSet to view
 *
 */
public void setDataSet( DataSet ds )
{   
  // This will be called by the "outside world" if the viewer is to replace 
  // its reference to a DataSet by a reference to a new DataSet, ds, and
  // rebuild the entire display, titles, borders, etc.

  setVisible( false );
  super.setDataSet( ds );

  if ( !validDataSet() )
    return;

  init();
  setVisible( true );
}


/* ------------------------- getDisplayComponent -------------------------- */
 /**
  *  Get the JComponent that contains the 3D view of the data, without
  *  any associated controls or auxillary displays.
  */
  public JComponent getDisplayComponent()
  {
    return threeD_panel;
  }


/* ------------------------ getXConversionScale -------------------------- */
 /**
  *  Return an XScale specified by the user to be used to control X-Axis 
  *  conversions.
  *
  *  @return  The current values from the number of bins control and the
  *           x range control form the Xscale that is returned, if no XScale
  *           is defined, a default XScale is used from a Data block in the
  *           DataSet.
  *
  */
 public XScale getXConversionScale()
  {
    XScale x_scale = x_scale_ui.getXScale();
    if ( x_scale == null )                        // no XScale specified, so use
    {                                             // the XScale from first data
      DataSet ds = getDataSet();                  // block.
      Data    d  = ds.getData_entry(0);
      x_scale = d.getX_scale();
    }
    return x_scale;
  }


/* -------------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

/* ------------------------- AddOptionsToMenu --------------------------- */

private void AddOptionsToMenu()
{
  OptionMenuHandler option_menu_handler = new OptionMenuHandler();
  JMenu option_menu = menu_bar.getMenu( OPTION_MENU_ID );

                                                     // color options
  JMenu color_menu = new ColorScaleMenu( option_menu_handler );
  option_menu.add( color_menu );

                                                     // group draw options
  JMenu group_menu = new JMenu( "Draw Groups" );
  option_menu.add( group_menu );

  GroupDrawMenuHandler group_draw_handler = new GroupDrawMenuHandler();
  ButtonGroup button_group = new ButtonGroup();
  JRadioButtonMenuItem r_button = new JRadioButtonMenuItem( GROUP_MARKER_SMALL);
  if ( group_draw_mode.equalsIgnoreCase( GROUP_MARKER_SMALL ))
    r_button.setSelected(true);
  r_button.addActionListener( group_draw_handler );
  group_menu.add( r_button );
  button_group.add( r_button );

  r_button = new JRadioButtonMenuItem( GROUP_MARKER_MEDIUM );
  if ( group_draw_mode.equalsIgnoreCase( GROUP_MARKER_MEDIUM ))
    r_button.setSelected(true);
  r_button.addActionListener( group_draw_handler );
  group_menu.add( r_button );
  button_group.add( r_button );

  r_button = new JRadioButtonMenuItem( GROUP_MARKER_LARGE );
  if ( group_draw_mode.equalsIgnoreCase( GROUP_MARKER_LARGE ))
    r_button.setSelected(true);
  r_button.addActionListener( group_draw_handler );
  group_menu.add( r_button );
  button_group.add( r_button );

  r_button = new JRadioButtonMenuItem( GROUP_MARKER_NONE );
  if ( group_draw_mode.equalsIgnoreCase( GROUP_MARKER_NONE ))
    r_button.setSelected(true);
  r_button.addActionListener( group_draw_handler );
  group_menu.add( r_button );
  button_group.add( r_button );

                                                       // detector draw options
  JMenu detector_menu = new JMenu( "Draw Detector Segments" );
  option_menu.add( detector_menu );

  DetectorDrawMenuHandler detector_draw_handler = new DetectorDrawMenuHandler();
  button_group = new ButtonGroup();
  r_button = new JRadioButtonMenuItem( DETECTOR_FILLED );
  if ( detector_draw_mode.equalsIgnoreCase( DETECTOR_FILLED ))
    r_button.setSelected(true);
  r_button.addActionListener( detector_draw_handler );
  detector_menu.add( r_button );
  button_group.add( r_button );

  r_button = new JRadioButtonMenuItem( DETECTOR_HOLLOW );
  if ( detector_draw_mode.equalsIgnoreCase( DETECTOR_HOLLOW ))
    r_button.setSelected(true);
  r_button.addActionListener( detector_draw_handler );
  detector_menu.add( r_button );
  button_group.add( r_button );

  r_button = new JRadioButtonMenuItem( DETECTOR_MARKER );
  if ( detector_draw_mode.equalsIgnoreCase( DETECTOR_MARKER ))
    r_button.setSelected(true);
  r_button.addActionListener( detector_draw_handler );
  detector_menu.add( r_button );
  button_group.add( r_button );

  r_button = new JRadioButtonMenuItem( DETECTOR_NONE );
  if ( detector_draw_mode.equalsIgnoreCase( DETECTOR_NONE ))
    r_button.setSelected(true);
  r_button.addActionListener( detector_draw_handler );
  detector_menu.add( r_button );
  button_group.add( r_button );
}


/* ---------------------------- group_location --------------------------- */

private Vector3D group_location( int index )
{
  DataSet ds = getDataSet();
  int     n_data = ds.getNum_entries();

  if ( index < 0 || index >= n_data )
    return null;

  Data d = ds.getData_entry(index);
 
  Position3D position= (Position3D)d.getAttributeValue( Attribute.DETECTOR_POS);
 
  if ( position == null )
    return null;

  float coords[] = position.getCartesianCoords();
  for ( int i = 0; i < 3; i++ )
    if ( Float.isNaN( coords[i] ) )
    {
      group_NaN_count++;
      return null;
    }

  Vector3D pt_3D = new Vector3D( coords[0], coords[1], coords[2] );

  return pt_3D;
}


/* --------------------------- MakeThreeD_Scene --------------------------- */

private void MakeThreeD_Scene()
{
  group_NaN_count = 0;
  pixel_NaN_count = 0;

  float group_radius = 0;
  float detector_radius = 0;

  if ( !group_draw_mode.equalsIgnoreCase( GROUP_MARKER_NONE ))
    group_radius = draw_groups();

  if ( !detector_draw_mode.equalsIgnoreCase( DETECTOR_NONE ))
    detector_radius = draw_detectors();

  float radius = Math.max( group_radius, detector_radius );  
  if ( radius <= 0 )                                      // need valid radius 
  {
    for ( int i = 0; i < getDataSet().getNum_entries(); i++ )
    {
      Vector3D point = group_location( i );
      if ( point != null )
      {
        float temp = point.length();
        if ( temp > radius )
          radius = temp;
      }
    }
  }
  if ( radius <= 0 )
    radius = 10;
  
  float altitude = getState().get_float( ViewerState.V_ALTITUDE );
  float azimuth  = getState().get_float( ViewerState.V_AZIMUTH );
  float distance = getState().get_float( ViewerState.V_DISTANCE );
  if ( distance <= 0 )
    distance = 3f*radius;
  view_control.setViewAngle( 45 );
  view_control.setAltitudeAngle( altitude );
  view_control.setAzimuthAngle( azimuth );
  view_control.setDistanceRange( 0.5f*radius, 5*radius );
  view_control.setDistance( distance );
  view_control.apply( true );

  draw_axes( radius/5 );
  draw_beam( radius );
  draw_instrument( radius );

  set_colors( frame_control.getFrameValue() );

  if ( pixel_NaN_count != 0 )
    SharedData.addmsg("NaN pixel count = " + pixel_NaN_count );

  if ( group_NaN_count != 0 )
    SharedData.addmsg("NaN group count = " + group_NaN_count );
}


/* ------------------------------ set_colors ---------------------------- */
/**
 * Set the color of all of the 3D group and/or detector objects to the
 * color corresponding to the data at the specified x_val.
 *
 * @param x_val  The x_value at which the data is to be evaluated.
 *
 */
private void set_colors( float x_val )
{
  int num_data = getDataSet().getNum_entries();

  float[] y_vals = rebinner.getY_valuesAtX( x_val );
  float val;
  int   index;
  for ( int i = 0; i < num_data; i++ )
  {
    val = y_vals[i] * scale_factor;
    if ( val >= 0 )
      index = (int)( ZERO_COLOR_INDEX + log_scale[(int)val] );
    else
      index = (int)( ZERO_COLOR_INDEX - log_scale[(int)(-val)] );

    if ( index < 0 )
      index += 256;

    if ( !group_draw_mode.equalsIgnoreCase( GROUP_MARKER_NONE ))
      threeD_panel.setColors( GROUP_PREFIX+i, color_table[index] );

    if ( !detector_draw_mode.equalsIgnoreCase( DETECTOR_NONE ))
      threeD_panel.setColors( DETECTOR_PREFIX+i, color_table[index] );
  }

  threeD_panel.repaint( );
}


/* ---------------------------- initFrameValues -------------------------- */

private void initFrameValues()
{
  if ( !validDataSet() )
    return;

  DataSet ds = getDataSet();

  int  num_rows = ds.getNum_entries();
  if ( num_rows == 0 ) 
    return;

  XScale x_scale = rebinner.getXScale();
  int    num_x    = x_scale.getNum_x();
  int    num_frames;

  if ( num_x == 1 )                         // single point, get one frame by
    num_frames = 1;                         // sampling.
  else
    num_frames = num_x - 1;

  float x_vals[]     = x_scale.getXs();
  float frame_vals[] = new float[ num_frames ];

  if ( ds.getData_entry(0).isHistogram() )
    for ( int i = 0; i < num_frames; i++ ) 
      frame_vals[i] = ( x_vals[i] + x_vals[i+1] )/ 2 ;   // bin center for hist
  else
    for ( int i = 0; i < num_frames; i++ ) 
      frame_vals[i] = x_vals[i];                         // point for function 
 
  frame_control.setFrame_values( frame_vals );
}


/* -------------------------- findMaxAbsDataValue ----------------------- */
/**
 *  Find the overall maximum absolute value of the data values in the
 *  DataSet, and record it in the field max_abs.
 */

private void findMaxAbsDataValue()
{
  float[] min_max = rebinner.getDataRange();

  float max_data = min_max[0];
  float min_data = min_max[1];

  if ( Math.abs( max_data ) > Math.abs( min_data ) )
    max_abs = Math.abs( max_data );
  else
    max_abs = Math.abs( min_data );
}


/* -------------------------- setDataScaleFactor ----------------------- */
/**
 *  Find the overall min and max data values in the DataSet, and set
 *  the scale factor used to map the data values to the interval
 *  from -(LOG_TABLE_SIZE-1) to +(LOG_TABLE_SIZE-1).
 */

private void setDataScaleFactor()
{
  findMaxAbsDataValue();

  if ( max_abs > 0 )
    scale_factor = (LOG_TABLE_SIZE - 1) / max_abs;
  else
    scale_factor = 0;
}


/* ----------------------------- setLogScale -------------------------- */

  private void setLogScale( double s )
  {
    if ( s > 100 )                                // clamp s to [0,100]
      s = 100;
    if ( s < 0 )
      s = 0;

    s = Math.exp(20 * s / 100.0) + 0.1; // map [0,100] exponentially to get
                                        // scale change that appears more linear
    double scale = NUM_POSITIVE_COLORS / Math.log(s);

    log_scale = new float[LOG_TABLE_SIZE];

    for ( int i = 0; i < LOG_TABLE_SIZE; i++ )
      log_scale[i] = (byte)
                     (scale * Math.log(1.0+((s-1.0)*i)/LOG_TABLE_SIZE));
  }


/* ------------------------------ draw_groups -------------------------- */

private float draw_groups()
{
//  System.out.println("Start draw_groups");
  DataSet ds     = getDataSet();
  int     n_data = ds.getNum_entries();

  float   max_radius = 0;
  float   radius = 0;

  Vector3D  points[] = new Vector3D[1];
  Vector3D  point;
  points[0] = new Vector3D();

  int marker_size = SMALL_MARKER_SIZE;
  if ( group_draw_mode.equalsIgnoreCase( GROUP_MARKER_SMALL ) )
    marker_size = SMALL_MARKER_SIZE;
  else if ( group_draw_mode.equalsIgnoreCase( GROUP_MARKER_MEDIUM ) )
    marker_size = MEDIUM_MARKER_SIZE;
  else if ( group_draw_mode.equalsIgnoreCase( GROUP_MARKER_LARGE ) )
    marker_size = LARGE_MARKER_SIZE;

  for ( int i = 0; i < n_data; i++ )
  {
    point = group_location( i );
    if ( point != null )
    {
      IThreeD_Object objects[] = new IThreeD_Object[1];
      radius = point.length();
      if ( radius > max_radius )
        max_radius = radius;

      points[0]= point;
      objects[0] = new Polymarker( points, Color.red );
      ((Polymarker)(objects[0])).setType( Polymarker.STAR );
      ((Polymarker)(objects[0])).setSize( marker_size );
      objects[0].setPickID( i );
      threeD_panel.setObjects( GROUP_PREFIX+i, objects );
    }
  }

//  System.out.println("End draw_groups");
  return max_radius;
}


/* ------------------------------ remove_groups -------------------------- */
/**
 *  Remove the 3D objects representing average detector group positions 
 *  from the 3D display.
 */
private void  remove_groups()
{
  DataSet ds     = getDataSet();
  int     n_data = ds.getNum_entries();

  for ( int i = 0; i < n_data; i++ )
    threeD_panel.removeObjects( GROUP_PREFIX+i );
}


/* ------------------------------ draw_detectors -------------------------- */

private float draw_detectors()
{
//  System.out.println("Start draw_detectors");

  DataSet ds     = getDataSet();
  int     n_data = ds.getNum_entries();

  float   max_radius = 0;
  float   radius = 0;

  Vector3D  points[] = new Vector3D[1];
  Vector3D  point,
            base,
            up;
  Vector3D  pixel_defs[] = new Vector3D[3]; 

  points[0] = new Vector3D();
  IThreeD_Object objects[];

  for ( int i = 0; i < n_data; i++ )
  {
    Data d = ds.getData_entry(i);
    PixelInfoList pil = (PixelInfoList)
                          d.getAttributeValue( Attribute.PIXEL_INFO_LIST );

    if ( pil != null )
    {
      int n_pixels = pil.num_pixels();
      objects = new IThreeD_Object[ n_pixels ];
      for ( int k = 0; k < n_pixels; k++ )
      {
        point = pil.pixel(k).position();
        base  = pil.pixel(k).x_vec();
        up    = pil.pixel(k).y_vec();

        pixel_defs[0] = point;
        pixel_defs[1] = base;
        pixel_defs[2] = up;

        if ( isNullOrNaN( pixel_defs ) )
        {
          objects[k] = new ThreeD_Non_Object();
          pixel_NaN_count++;
        }

        else
        {
          radius = point.length();
          if ( radius > max_radius )
            max_radius = radius;

          objects[k] = make_detector( point, 
                                      base,
                                      up,
                                      pil.pixel(k).width(), 
                                      pil.pixel(k).height(),
                                      i );
        }
      }
      threeD_panel.setObjects( DETECTOR_PREFIX+i, objects );
    }
  }

//  System.out.println("End draw_detectors");
  return max_radius;
}


/**
 *  Check for any null vector, or vector with a NaN component in the
 *  list.  
 *  @param list   Array of vectors to check
 *  @return true if any vector is null or has a NaN entry, and returns
 *          false if they are all OK.
 */
private boolean isNullOrNaN( Vector3D[] list )
{
  if ( list == null || list.length <= 0 )
    return true;

  float[] coords;
  for ( int i = 0; i < list.length; i++ )
  {
    if ( list[i] == null )
      return true;

    coords = list[i].get();
    for ( int k = 0; k < coords.length; k++ )
      if ( Float.isNaN( coords[k] ) )
        return true;
  }

  return false;
}


/* ------------------------------ remove_detectors -------------------------- */
/**
 *  Remove the 3D objects representing detector pixel positions 
 *  from the 3D display.
 */
private void  remove_detectors()
{
  DataSet ds     = getDataSet();
  int     n_data = ds.getNum_entries();

  for ( int i = 0; i < n_data; i++ )
    threeD_panel.removeObjects( DETECTOR_PREFIX+i );
}


/* ------------------------------ draw_axes ----------------------------- */

private void draw_axes( float length  )
{
  IThreeD_Object objects[] = new IThreeD_Object[ 4 ];
  Vector3D points[] = new Vector3D[2];

  points[0] = new Vector3D( 0, 0, 0 );                    // y_axis
  points[1] = new Vector3D( 0, length, 0 );
  objects[0] = new Polyline( points, Color.green );
                                                          // z_axis
  points[1] = new Vector3D( 0, 0, length );
  objects[1] = new Polyline( points, Color.blue );

  points[1] = new Vector3D( length, 0, 0 );               // +x-axis
  objects[2] = new Polyline( points, Color.red );

  points[1] = new Vector3D( -length/3, 0, 0 );            // -x-axis
  objects[3] = new Polyline( points, Color.red );

  threeD_panel.setObjects( AXES, objects );
}


/* --------------------------- draw_beam --------------------------- */

private void draw_beam( float radius  )
{
  IThreeD_Object objects[] = new IThreeD_Object[ 15 ];
  Vector3D points[] = new Vector3D[1];

  points[0] = new Vector3D( 0, 0, 0 );                    // sample
  Polymarker sample = new Polymarker( points, Color.red );
  sample.setType( Polymarker.STAR );
  sample.setSize( 10 );
  objects[0] = sample; 
  
  for ( int i = 1; i < 15; i++ )                          // beam is segmented
  {                                                       // for depth sorting
    points = new Vector3D[2];
    points[0] = new Vector3D( -(i-1)*radius/3, 0, 0 );    // beam in 
    points[1] = new Vector3D( -i*radius/3, 0, 0 );        // -x-axis direction
    objects[i] = new Polyline( points, Color.red );
  }

  threeD_panel.setObjects( BEAM, objects );
}


/* --------------------------- draw_instrument --------------------------- */

private void draw_instrument( float radius  )
{
  final int N_PIECES = 180;
  IThreeD_Object objects[] = new IThreeD_Object[ N_PIECES ];
  Vector3D points[];
                                                          // draw circle for
                                                          // instrument 
  float delta_angle = 2 * 3.14159265f / N_PIECES;
  float angle = 0;
  float x,
        y;
  for ( int i = 0; i < N_PIECES; i++ )                    // circle is segmented
  {                                                       // for depth sorting
    points = new Vector3D[2];
    x = radius * (float)Math.cos( angle );
    y = radius * (float)Math.sin( angle );
    points[0] = new Vector3D( x, y, 0 );  

    angle += delta_angle;
    x = radius * (float)Math.cos( angle );
    y = radius * (float)Math.sin( angle );
    points[1] = new Vector3D( x, y, 0 );  
    objects[i] = new Polyline( points, Color.black );
  }

  threeD_panel.setObjects( INSTRUMENT, objects );
}


/* ---------------------------- make_detector --------------------------- */
/*
 *  Draw a detector object that is a simple rectangle, polyline, or marker. 
 */
private IThreeD_Object make_detector( Vector3D point,
                                      Vector3D base,
                                      Vector3D up,
                                      float    width,
                                      float    length,
                                      int      pick_id )
{
  IThreeD_Object detector;

  if ( detector_draw_mode.equalsIgnoreCase( DETECTOR_MARKER ))
  {
    Vector3D verts[] = new Vector3D[1];
    verts[0] = point;
    Polymarker object = new Polymarker( verts, Color.red );
    object.setType( Polymarker.BOX );
    object.setSize( 2 );

    detector = object;
  }
  else
  {
    Vector3D verts[] = new Vector3D[4];

    verts[0] = new Vector3D( -width/2,  length/2, 0 );
    verts[1] = new Vector3D( -width/2, -length/2, 0 );
    verts[2] = new Vector3D(  width/2, -length/2, 0 );
    verts[3] = new Vector3D(  width/2,  length/2, 0 );

    if ( base == null || up == null )
    {
      float coords[] = point.get();
      base = new Vector3D ( coords[1], -coords[0], 0 );
      base.normalize();

      Vector3D n  = new Vector3D( point );
      up = new Vector3D();
      up.cross( n, base );
      up.normalize();
    }

    Tran3D orient = new Tran3D();
    orient.setOrientation( base, up, point );
    orient.apply_to( verts, verts );

    gov.anl.ipns.ViewTools.Panels.ThreeD.Polygon object = 
       new gov.anl.ipns.ViewTools.Panels.ThreeD.Polygon( verts, Color.red );
    if ( detector_draw_mode.equalsIgnoreCase( DETECTOR_HOLLOW ))
      object.setType( gov.anl.ipns.ViewTools.Panels.ThreeD.Polygon.HOLLOW );

    detector = object;
  }

  detector.setPickID( pick_id );
  return detector;
}


/* ----------------------------- main ------------------------------------ */
/*
 *  For testing purposes only
 */
public static void main(String[] args)
{
/*
  DataSet   data_set   = new DataSet("Sample DataSet", "Sample log-info");
  data_set.setX_units( "Test X Units" );
  data_set.setX_label("Text X Label" );
  data_set.setY_units( "Test Y Units" );
  data_set.setY_label("Text Y Label" );

  Data          spectrum;     // data block that will hold a "spectrum"
  float[]       y_values;     // array to hold the "counts" for the spectrum
  UniformXScale x_scale;      // "time channels" for the spectrum

  for ( int id = 1; id < 10; id++ )            // for each id
  {
    x_scale = new UniformXScale( 1, 5, 50 );   // build list of time channels

    y_values = new float[50];                       // build list of counts
    for ( int channel = 0; channel < 50; channel++ )
      y_values[ channel ] = (float)Math.sin( id * channel / 10.0 );

    spectrum = new Data( x_scale, y_values, id ); 
    float x = 0;
    float y = 0;
    x = (float)( 2*Math.cos( id*Math.PI/10 ) );
    y = (float)( 2*Math.sin( id*Math.PI/10 ) );
    DetectorPosition pos = new DetectorPosition();
    pos.setCartesianCoords( x, y, 0 );
    DetPosAttribute  att = new DetPosAttribute( Attribute.DETECTOR_POS, pos );
    spectrum.setAttribute( att );
    data_set.addData_entry( spectrum );    
  }
*/
//  String file_name = "/usr/home/dennis/ARGONNE_DATA/glad0816.run";
//  String file_name = "/usr/home/dennis/ARGONNE_DATA/GLAD4696.RUN";
//  String file_name = "/usr/home/dennis/ARGONNE_DATA/hrcs2936.run";
  String file_name = "/usr/home/dennis/ARGONNE_DATA/GPPD12358.RUN";

  RunfileRetriever rr = new RunfileRetriever( file_name ); 
  DataSet data_set = rr.getDataSet( 1 );
  ThreeDView view = new ThreeDView( data_set, null );
  JFrame f = new JFrame("Test for ThreeDView");
  f.setBounds(0,0,600,400);
  f.setJMenuBar( view.getMenuBar() );
  f.getContentPane().add( view );
  f.setVisible( true );
}
   

/* -----------------------------------------------------------------------
 *
 * PRIVATE METHODS
 *
 */ 

private void init()
{
  rebinner = new Rebinner( getDataSet() );

  if ( threeD_panel != null )          // get rid of old components first 
  {
    threeD_panel.removeAll();
    split_pane.removeAll();
    control_panel.removeAll();
    removeAll();
  }

  threeD_panel = new ThreeD_JPanel();
  threeD_panel.setBackground( new Color( 90, 90, 90 ) );
  
  //setting coord info source to null because it doesn't really apply to 
  // a 3d environment
  threeD_panel.setCoordInfoSource(null);

  control_panel = new Box( BoxLayout.Y_AXIS );

  String label = getDataSet().getX_units();
  UniformXScale x_scale  = getDataSet().getXRange();
  float x_min  = x_scale.getStart_x();
  float x_max  = x_scale.getEnd_x();
                                // set n_steps to 0 to default to NO REBINNING
  x_scale_ui = new XScaleChooserUI( "X Scale", label, x_min, x_max, 0 );

  x_scale_ui.addActionListener( new XScaleListener() );
  control_panel.add( x_scale_ui );

  color_scale_image = new ColorScaleImage();
  color_scale_image.setNamedColorModel( 
              getState().get_String( ViewerState.COLOR_SCALE), true, true );
  control_panel.add( color_scale_image );

  log_scale_slider.setPreferredSize( new Dimension(120,50) );
  log_scale_slider.setValue( getState().get_int( ViewerState.BRIGHTNESS) );
  log_scale_slider.setMajorTickSpacing(20);
  log_scale_slider.setMinorTickSpacing(5);
  log_scale_slider.setPaintTicks(true);

  TitledBorder border = new TitledBorder(
                             LineBorder.createBlackLineBorder(),"Brightness");
  border.setTitleFont( FontUtil.BORDER_FONT );
  log_scale_slider.setBorder( border );
  log_scale_slider.addChangeListener( new LogScaleEventHandler() );

  control_panel.add( log_scale_slider );

  view_control = new AltAzController();
  view_control.addActionListener( new AltAzControlListener() );
  view_control.addControlledPanel( threeD_panel );
  control_panel.add( view_control );

  frame_control = new AnimationController();
  frame_control.setBorderTitle( getDataSet().getX_label() );
  frame_control.setTextLabel( getDataSet().getX_units() );
  control_panel.add( frame_control );

  conv_table = new DataSetXConversionsTable( getDataSet() );
  JPanel conv_panel = new JPanel();
  conv_panel.setLayout( new GridLayout(1,1) );
  conv_panel.add( conv_table.getTable() );
  border = new TitledBorder( LineBorder.createBlackLineBorder(), "Pixel Data");
  border.setTitleFont( FontUtil.BORDER_FONT );
  conv_panel.setBorder( border );
  control_panel.add( conv_panel );

  JPanel filler = new JPanel();
  filler.setPreferredSize( new Dimension( 120, 2000 ) );
  control_panel.add( filler );
                                        // make a titled border around the
                                        // whole viewer, using an appropriate
                                        // title from the DataSet. 
  String title = getDataSet().toString();
  AttributeList attr_list = getDataSet().getAttributeList();
  Attribute     attr      = attr_list.getAttribute(Attribute.RUN_TITLE);
  if ( attr != null )
   title = attr.getStringValue();

  border = new TitledBorder( LineBorder.createBlackLineBorder(), title);
  border.setTitleFont( FontUtil.BORDER_FONT );
  setBorder( border );
                                     // Place the graph area inside of a
                                     // JPanel and make a titled border around
                                     // the JPanel graph area using the last 
                                     // message, if available
  JPanel graph_container = new JPanel();
  OperationLog op_log = getDataSet().getOp_log();
  if ( op_log.numEntries() <= 0 )
    title = "Graph Display Area";
  else
    title = op_log.getEntryAt( op_log.numEntries() - 1 );

  border = new TitledBorder( LineBorder.createBlackLineBorder(), title );
  border.setTitleFont( FontUtil.BORDER_FONT );
  graph_container.setBorder( border );
  graph_container.setLayout( new GridLayout(1,1) );
  graph_container.add( threeD_panel );
  
  split_pane = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
                                       graph_container,
                                       control_panel,
                                       0.7f );

                                        // Add the control area and graph
                                        // container to the main viewer
                                        // panel and draw.
  setLayout( new GridLayout(1,1) );
  add ( split_pane ); 

  redraw( NEW_DATA_SET );

  threeD_panel.addMouseMotionListener( new ViewMouseMotionAdapter() );
  frame_control.addActionListener( new FrameControlListener() );
  threeD_panel.addMouseListener( new ViewMouseAdapter() );
}


   private void processMouseClickEvent(MouseEvent e)
   {
      int index = threeD_panel.pickID( e.getX( ) , e.getY( ) , 15 );
      
      if ( index != last_index )
      {
         last_index = index;
         DataSet ds = getDataSet( );
         if ( index >= 0 && index < ds.getNum_entries( ) )
         {
            ds.setPointedAtIndex( index );
            redraw_cursor = false;
            ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
            float frame_val = frame_control.getFrameValue( );
            if ( !Float.isNaN( frame_val ) )
            {              
               float y_val = rebinner.getY_valueAtX( index , frame_val );
               conv_table.showConversions( frame_val , y_val , index );
            }
         }
      }
   }

/* -------------------------------------------------------------------------
 *
 *  INTERNAL CLASSES
 *
 */
private class ViewMouseAdapter extends MouseAdapter
{
   @Override
   public void mouseClicked(MouseEvent e)
   {
      processMouseClickEvent( e );
     /* int index = threeD_panel.pickID( e.getX(), e.getY(), 15 );
      if ( index != last_index )
      {
        last_index = index;
        DataSet ds = getDataSet();
        if ( index >= 0 && index < ds.getNum_entries() ) 
        {
          ds.setPointedAtIndex( index );
          redraw_cursor = false;
          ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
         float frame_val = frame_control.getFrameValue();
          if ( !Float.isNaN( frame_val ) )
          {
            float y_val = rebinner.getY_valueAtX( index, frame_val );
            conv_table.showConversions( frame_val, y_val, index );
          }
          
        }
      }
     */ 
   }
}


/* ------------------------- ViewMouseMotionAdapter ----------------------- */
/**
 *  Listen for mouse motion events and just print out the pixel coordinates
 *  to demonstrate how to handle such events.
 */
private class ViewMouseMotionAdapter extends MouseMotionAdapter
{
   public void mouseDragged( MouseEvent e )
   {
      processMouseClickEvent( e );
     //System.out.println("Mouse moved at: " + e.getPoint() );
     /*int index = threeD_panel.pickID( e.getX(), e.getY(), 15 );
     if ( index != last_index )
     {
       last_index = index;
       DataSet ds = getDataSet();
       if ( index >= 0 && index < ds.getNum_entries() ) 
       {
         ds.setPointedAtIndex( index );
         redraw_cursor = false;
         ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
        float frame_val = frame_control.getFrameValue();
         if ( !Float.isNaN( frame_val ) )
         {
           float y_val = rebinner.getY_valueAtX( index, frame_val );
           conv_table.showConversions( frame_val, y_val, index );
         }
       }
     }
     */
   }
}


/* -------------------------- OptionMenuHandler --------------------------- */
/**
 *  Listen for Option menu selections and just print out the selected option.
 *  It may be most convenient to have a separate listener for each menu.
 */
private class OptionMenuHandler implements ActionListener
{
  public void actionPerformed( ActionEvent e )
  {
    String action = e.getActionCommand();
    color_table = IndexColorMaker.getDualColorTable( action,
                                                     NUM_POSITIVE_COLORS );
    set_colors( frame_control.getFrameValue() );
    color_scale_image.setNamedColorModel( action, true, true );
    getState().set_String( ViewerState.COLOR_SCALE, action );
  }
}


/* ---------------------------- LogScaleEventHandler ----------------------- */
/**
 *  Listen for changes to the log scale slider and build a new color scale
 *  when it changes.
 */
private class LogScaleEventHandler implements ChangeListener,
                                              Serializable
{
  public void stateChanged(ChangeEvent e)
  {
    JSlider slider = (JSlider)e.getSource();

                              // set new log scale when slider stops moving
    if ( !slider.getValueIsAdjusting() )
    {
      int value = slider.getValue();
      setLogScale( value );
      getState().set_int( ViewerState.BRIGHTNESS, value );
      set_colors( frame_control.getFrameValue() );
    }
  }
}


/* ---------------------------- FrameControlListener ----------------------- */
/**
 *  Step to new frames based on commands from the AnimationController.
 */
private class FrameControlListener implements ActionListener
{
  public void actionPerformed( ActionEvent e )
  {
    float frame_val = frame_control.getFrameValue();
    set_colors( frame_val );

    if ( !Float.isNaN( frame_val ) && notify_ds_observers )
    {
      getDataSet().setPointedAtX( frame_val );
      getState().set_float( ViewerState.POINTED_AT_X, frame_val );
      if ( debug )
        System.out.println("ThreeD viewer CALLING NOTIFY: " + frame_val);
      redraw_cursor = false;
      getDataSet().notifyIObservers( IObserver.POINTED_AT_CHANGED );
    }
    notify_ds_observers = true;  // only skip one message from frame control
  }
}


/* ----------------------- GroupDrawMenuHandler ------------------------ */

  private class GroupDrawMenuHandler implements ActionListener,
                                                Serializable
  {
    public void actionPerformed( ActionEvent e )
    {
      String action  = e.getActionCommand();
                                                 // drawing mode change
      if ( action != group_draw_mode )
      {
        JRadioButtonMenuItem button = (JRadioButtonMenuItem)e.getSource();
        button.setSelected(true);
        group_draw_mode = action;
        getState().set_String( ViewerState.V_GROUPS, group_draw_mode );

        if ( action.equalsIgnoreCase( GROUP_MARKER_NONE ) )
          remove_groups();
        else
          draw_groups();

        set_colors( frame_control.getFrameValue() );
      }
    }
  }


/* ----------------------- DetectorDrawMenuHandler ------------------------ */

  private class DetectorDrawMenuHandler implements ActionListener,
                                                   Serializable
  {
    public void actionPerformed( ActionEvent e )
    {
      String action  = e.getActionCommand();
                                                 // drawing mode change
      if ( action != detector_draw_mode )
      {
        JRadioButtonMenuItem button = (JRadioButtonMenuItem)e.getSource();
        button.setSelected(true);
        detector_draw_mode = action;
        getState().set_String( ViewerState.V_DETECTORS, detector_draw_mode );

        if ( action.equalsIgnoreCase( DETECTOR_NONE ) )
          remove_detectors();
        else
          draw_detectors();

        set_colors( frame_control.getFrameValue() );
      }
    }
  }


/* ------------------------ XScaleListener ----------------------------- */

  private class XScaleListener implements ActionListener,
                                          Serializable
  {
     public void actionPerformed(ActionEvent e)
     {
       String action  = e.getActionCommand();
       XScale x_scale = x_scale_ui.getXScale();

       if ( x_scale != null && x_scale.getNum_x() > 0 )
       {
         if ( x_scale.getNum_x() == 1 )
         {
           float min = x_scale.getStart_x();
           float max = x_scale.getEnd_x();
           x_scale = new UniformXScale( min, max, 2 );
           System.out.println("Special XScale = " + x_scale );
         }
         rebinner.setXScale( x_scale );
       }
       else
         rebinner.reset();

       setDataScaleFactor();
       getDataSet().notifyIObservers( action );
     }
  }


/* ------------------------ AltAzControlListener ------------------------- */

  private class AltAzControlListener implements ActionListener,
                                                Serializable
  {
     public void actionPerformed(ActionEvent e)
     {                                             // just update the values
                                                   // in the state object
       float altitude = view_control.getAltitudeAngle(); 
       float azimuth  = view_control.getAzimuthAngle(); 
       float distance = view_control.getDistance(); 
       
       getState().set_float( ViewerState.V_AZIMUTH,  azimuth );
       getState().set_float( ViewerState.V_ALTITUDE, altitude );
       getState().set_float( ViewerState.V_DISTANCE, distance );
       }
  }

   class ThreeDsetCrossHair extends Thread
   {
      Object        synchrObject = new Object();
      ThreeD_JPanel View3D;
      Point         pt;
      Point         done;

      public ThreeDsetCrossHair(ThreeD_JPanel View3D, Point pt)
      {
         this.View3D = View3D;
         this.pt = pt;
         done = null;
      }
      
      public void setPoint( Point pt)
      {
         synchronized(synchrObject)
         {
         this.pt = pt;
         }
      }
      
      public Point getPoint()
      {
         return done;
      }

      @Override
      public void run()
      {
        synchronized(synchrObject)
        {
         View3D.set_crosshair( pt );
         done = pt;
         pt = null;
        }
      }
   }

}

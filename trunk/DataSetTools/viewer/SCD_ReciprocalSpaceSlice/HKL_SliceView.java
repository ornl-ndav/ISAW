/*
 * File:  HKL_SliceView.java
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.6  2004/03/06 21:59:22  dennis
 * Changed to work with corrected calculation of inverse matrix.
 *
 * Revision 1.5  2004/03/04 20:59:39  dennis
 * Now allows selecting the slice plane in either HKL or Qxyz
 * and independently allows displaying the plane in either
 * HKL or Qxyz.
 *
 * Revision 1.4  2004/03/03 23:22:28  dennis
 * Implemented option to select slice in HKL but display it in Q.
 *
 * Revision 1.3  2004/03/03 16:05:33  dennis
 * Added table of conversions (not active yet).
 * Placed slice selector, image controls, and table of conversions
 * in three tabbed panes.
 * Added cursor x,y readout.
 * Fixed bug with calculation of number of rows & columns to use
 * when extracting slice.  (col & row were transposed)
 * Now preserves the aspect ratio.
 *
 * Revision 1.2  2004/02/02 23:55:23  dennis
 * Added additional Axes labels for special case of
 * constant h,k or l planes.  Added code to select
 * mode based on whether or not an orientation matrix
 * was loaded, and to set the SliceSelectorUI to the
 * corresponding mode.
 *
 * Revision 1.1  2004/01/28 23:58:26  dennis
 * Initial Version of viewer for slices in HKL.
 *
 */

package DataSetTools.viewer.SCD_ReciprocalSpaceSlice;

import DataSetTools.components.View.TwoD.*;
import DataSetTools.components.View.ViewControls.*;
import DataSetTools.components.ui.*;
import DataSetTools.components.containers.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Attribute.*;

import DataSetTools.trial.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.math.*;
import DataSetTools.instruments.*;
import DataSetTools.retriever.*;
import DataSetTools.components.View.*;
import DataSetTools.viewer.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.JComponent.*;

/**
 * Provides a mechanism for selecting and viewing slices through reciprocal
 * space for an SCD DataSet.  If the DataSet has an orientation matrix as
 * an attribute, the slices may specified in terms of Miller indices, otherwise
 * the slices are specified in terms of Q.
 *
 * @see DataSetTools.dataset.DataSet
 * @see DataSetTools.viewer.DataSetViewer
 */

public class HKL_SliceView extends DataSetViewer
{                         
  public static final int MIN_STEPS = 3;
  public static final int MAX_STEPS = 10001;
  
  // the split pane, image_container, control panels and slice_selector 
  // are made once in the constructor

  private SplitPaneWithState split_pane;
  private JPanel             image_container; 
  private JPanel             ivc_controls; 
  private JPanel             control_panel; 
  private SliceSelectorUI    slice_selector;

  // the ivc must be reconstructed whenever the rows and colums change.

  private ImageViewComponent ivc = null;
  private DataSetXConversionsTable image_table = 
                                   new DataSetXConversionsTable( getDataSet() );

  // the Q_SliceExtractor, orientation matrix and valid flag must be 
  // reconstructed whenever the DataSet is changed

  private Q_SliceExtractor  extractor = null;
  private Tran3D   orientation_matrix = null;
  
  private boolean valid_ds = false; 

  private boolean debug = false;

  private CursorOutputControl cursor_output = null;

/* --------------------------------------------------------------------------
 *
 * CONSTRUCTORS
 *
 */

  /* -------------------------------------------------------------------- */

  public HKL_SliceView( DataSet data_set, ViewerState state ) 
  {
    super(data_set, state);  // Records the data_set and current ViewerState
                             // object in the parent class and then
                             // sets up the menu bar with items handled by the
                             // parent class.

    slice_selector  = new SliceSelectorUI( SliceSelectorUI.HKL_MODE );
    slice_selector.addActionListener( new SliceListener() );

    image_container = new JPanel();
    image_container.setLayout( new GridLayout(1,1) );
    image_container.setPreferredSize( new Dimension(5000,0) );

    control_panel = new JPanel(); 
    control_panel.setMinimumSize( new Dimension(220,0) );
    control_panel.setLayout( new BorderLayout() );

    JPanel common_controls = new JPanel();
    common_controls.setPreferredSize( new Dimension(220,85) );
    common_controls.setLayout( new GridLayout(2,1) );

    ivc_controls  = new JPanel();
    ivc_controls.setLayout( new GridLayout(1,1) );

    split_pane    = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
                                            image_container,
                                            control_panel,
                                           .75f );
    setLayout( new GridLayout(1,1) );
    add ( split_pane ); 

    // make a titled border around the control area, since that doesn't change

    TitledBorder border = new TitledBorder( LineBorder.createBlackLineBorder(),
                                           "Controls" );
    border.setTitleFont( FontUtil.BORDER_FONT );
    control_panel.setBorder( border );

    image_table = new DataSetXConversionsTable( getDataSet() );
    JPanel table_panel = new JPanel();
    table_panel.setLayout( new BorderLayout() );
    JPanel filler = new JPanel();
    table_panel.add( image_table.getTable(), BorderLayout.NORTH );
    table_panel.add( filler, BorderLayout.CENTER );

    // put ivc_controls and slice_selector in a tabbed pane
    JTabbedPane tabbed_pane = new JTabbedPane();
    tabbed_pane.setFont( FontUtil.LABEL_FONT );
    tabbed_pane.addTab( "View", ivc_controls );
    tabbed_pane.addTab( "Slice", slice_selector );
    tabbed_pane.addTab( "Conversions", table_panel );
    tabbed_pane.setSelectedIndex(1);
    control_panel.add( tabbed_pane, BorderLayout.CENTER ); 
    control_panel.add( common_controls, BorderLayout.NORTH );

    ivc = new ImageViewComponent( new VirtualArray2D( 1, 1 ) );
    ivc.preserveAspectRatio( true );
    ivc.addActionListener( new ImageListener() );

    image_container.removeAll();
    image_container.add( ivc.getDisplayPanel() );

    String cursor_labels[] = {"X","Y"};
    cursor_output = new CursorOutputControl( cursor_labels );
    cursor_output.setTitle("Current Position");
    setCurrentPoint( new floatPoint2D(0,0) );

    Box controls = new Box(BoxLayout.Y_AXIS);
    JComponent[] ctrl = ivc.getSharedControls();
    for( int i = 0; i < ctrl.length; i++ )
    {
      if ( ctrl[i] instanceof ControlCheckboxButton )
        ((ControlCheckboxButton)ctrl[i]).setButtonFont( FontUtil.LABEL_FONT );

      if ( ctrl[i] instanceof ControlSlider )
        common_controls.add(ctrl[i]);
      else
        controls.add(ctrl[i]);
    }

    ivc_controls.removeAll();
    ivc_controls.add( controls );

    common_controls.add( cursor_output );
   
    split_pane.validate();

    setDataSet( data_set );
  }

  /* -----------------------------------------------------------------------
   *
   *  PUBLIC METHODS
   *
   */

  public void redraw( String reason )
  {
    if ( !valid_ds )
    {
      image_container.removeAll();
      image_container.add( 
             new JLabel("ERROR:SCD DataSet with sample orientation " +  
                        "and orientation matrix required") );
      return;
    }

    if ( extractor == null || ivc == null )
      return;

    if ( image_container.getComponent(0) instanceof JLabel )
    {
      image_container.removeAll();
      image_container.add( ivc.getDisplayPanel() );
    }

    IVirtualArray2D va2D = extractSlice();
    if ( va2D != null )
      ivc.dataChanged( va2D );
    else
      System.out.println("ERROR: null slice in HKL_SliceView.java");
  }

  /* ---------------------------- setDataSet ----------------------------- */
  
  public void setDataSet( DataSet ds )
  {   
    // This will be called by the "outside world" if the viewer is to replace 
    // its reference to a DataSet by a reference to a new DataSet, ds, and
    // rebuild the entire display, titles, borders, etc.

    setVisible( false );
    super.setDataSet( ds );

    resetBorderTitles();

    valid_ds = isValid_SCD_DataSet();
    orientation_matrix = getOrientationMatrix();

    if ( orientation_matrix == null )
    {
      slice_selector.setSliceMode( SliceSelectorUI.QXYZ_MODE, false ); 
      slice_selector.setDisplayMode( SliceSelectorUI.QXYZ_MODE, false ); 
    }
    else 
    {
      slice_selector.setSliceMode( SliceSelectorUI.HKL_MODE, true ); 
      slice_selector.setDisplayMode( SliceSelectorUI.HKL_MODE, true ); 
    }

    DataSet ds_list[] = new DataSet[1];
    ds_list[0] = ds;
    extractor = new Q_SliceExtractor( ds_list );

    redraw( NEW_DATA_SET );
    setVisible( true );
  }


  /* -----------------------------------------------------------------------
   *
   * PRIVATE METHODS
   *
   */ 

  /* ------------------------- resetBorderTitles ------------------------ */
  /*
   * make a titled border around the whole viewer and a border around the
   * image_container, using values from the current DataSet.
   */
  private void resetBorderTitles()
  { 
    String title = getDataSet().toString();
    AttributeList attr_list = getDataSet().getAttributeList();
    Attribute     attr      = attr_list.getAttribute(Attribute.RUN_TITLE);
    if ( attr != null )
     title = attr.getStringValue();

    TitledBorder border = new TitledBorder(
                                    LineBorder.createBlackLineBorder(), title);
    border.setTitleFont( FontUtil.BORDER_FONT );
    setBorder( border );

    OperationLog op_log = getDataSet().getOp_log();
    if ( op_log.numEntries() <= 0 )
      title = "Slice Display Area";
    else
      title = op_log.getEntryAt( op_log.numEntries() - 1 );

    border = new TitledBorder( LineBorder.createBlackLineBorder(), title );
    border.setTitleFont( FontUtil.BORDER_FONT );
    image_container.setBorder( border );
  }

 
  /* ------------------------ isValid_SCD_DataSet -------------------------*/
  /*
   *  Verify that the DataSet is an SCD DataSet and that it has an
   *  orientation matrix attribute.
   */
  private boolean isValid_SCD_DataSet()
  {
    Attribute attr = getDataSet().getAttribute( Attribute.INST_TYPE );
    if ( attr == null )
      return false;

    double type = attr.getNumericValue();
    if ( type != InstrumentType.TOF_SCD )
      return false;

    return true; 
  }

  /* ----------------------- setAxesAndTitle ----------------------------- */
  /*
   *  Determine appropriate axes and title for the slice 
   */
  private void setAxesAndTitle( IVirtualArray2D va2D )
  {
    SlicePlane3D plane = slice_selector.getPlane();
 
    Vector3D origin = plane.getOrigin();
    Vector3D normal = plane.getNormal();
    Vector3D u      = plane.getU();
    Vector3D v      = plane.getV();
    float width  = slice_selector.getSliceWidth();
    float height = slice_selector.getSliceHeight();

    if ( slice_selector.getDisplayMode() == SliceSelectorUI.HKL_MODE )
    {
      setHKL_Title( origin, normal, va2D );
      setHKL_Axis( AxisInfo.X_AXIS, -width/2.0f, width/2.0f, origin, u, va2D );
      setHKL_Axis( AxisInfo.Y_AXIS, -height/2.0f, height/2.0f, origin, v, va2D);
    }
    else
    {
      setGeneralTitle( origin, normal, va2D );
      setGeneralAxis( AxisInfo.X_AXIS, -width/2.0f, width/2.0f, u, va2D );
      setGeneralAxis( AxisInfo.Y_AXIS, -height/2.0f, height/2.0f, v, va2D );
    }
  }


  /* --------------------------- setGeneralAxis -------------------------- */
  /*
   *  Set the axis information to cover the specified interval, listing
   *  the specified vector as the direction, for the case of an arbitrary
   *  plane.
   */
  private void setGeneralAxis( int              axis_num, 
                               float            min,
                               float            max,
                               Vector3D         vector,
                               IVirtualArray2D  va2D )
  {
     String units;
     if ( slice_selector.getDisplayMode() == SliceSelectorUI.HKL_MODE )
       units = "HKL units";
     else
       units = "Inverse Angstroms";

     va2D.setAxisInfo( axis_num, min, max,
                      "Direction:(" + Format.real( vector.get()[0], 5 ) +
                                "," + Format.real( vector.get()[1], 5 ) +
                                "," + Format.real( vector.get()[2], 5 ) + ")",
                       units,
                       true );
  } 


  /* --------------------------- setHKL_Axis -------------------------- */
  /*
   *  Set the axis information to cover the specified interval, listing
   *  the specified vector as the direction, for the constant H, K or L case.
   */
  private void setHKL_Axis( int              axis_num,
                            float            min,
                            float            max,
                            Vector3D         origin,
                            Vector3D         vector,
                            IVirtualArray2D  va2D )
  {
    String title = null;

    if ( vector.dot( new Vector3D( 1, 0, 0 ) ) >= 0.99999 )
    {
      title = "H Index";
      min += origin.get()[0];
      max += origin.get()[0];
    }

    else if ( vector.dot( new Vector3D( 0, 1, 0 ) ) >= 0.99999 )
    {
      title = "K Index";
      min += origin.get()[1];
      max += origin.get()[1];
    }

    else if ( vector.dot( new Vector3D( 0, 0, 1 ) ) >= 0.99999 )
    {
      title = "L Index";
      min += origin.get()[2];
      max += origin.get()[2];
    }

    if ( title != null )
      va2D.setAxisInfo( axis_num, min, max, "", title, true );
    else 
      setGeneralAxis( axis_num, min, max, vector, va2D );
  }


  /* --------------------------- setGeneralTitle -------------------------- */
  /*
   *  Set the title information for the case of an arbitrary plane.
   */
  private void setGeneralTitle( Vector3D        origin, 
                                Vector3D        normal,
                                IVirtualArray2D va2D )
  {
    va2D.setTitle( "Center:(" + Format.real( origin.get()[0], 5 ) +
                          "," + Format.real( origin.get()[1], 5 ) +
                          "," + Format.real( origin.get()[2], 5 ) + "), " +
                   "Normal:(" + Format.real( normal.get()[0], 5 ) +
                          "," + Format.real( normal.get()[1], 5 ) +
                          "," + Format.real( normal.get()[2], 5 ) + ")" );
  }


  /* --------------------------- setHKL_Title -------------------------- */
  /*
   *  Set the title information for the constant H, K or L case.
   */
  private void setHKL_Title( Vector3D        origin,
                             Vector3D        normal,
                             IVirtualArray2D va2D )
  {
    String title = null;

    if ( normal.dot( new Vector3D( 1, 0, 0 ) ) >= 0.99999 )
      title = "H = " + origin.get()[0]; 

    else if ( normal.dot( new Vector3D( 0, 1, 0 ) ) >= 0.99999 ) 
      title = "K = " + origin.get()[1]; 

    else if ( normal.dot( new Vector3D( 0, 0, 1 ) ) >= 0.99999 )
      title = "L = " + origin.get()[2]; 

    if ( title != null )
      va2D.setTitle( title );
    else 
      setGeneralTitle( origin, normal, va2D );
  }


  /* ------------------------ getOrientationMatrix ----------------------- */
  /*
   *  Get the orientation matrix from the DataSet, return null if not
   *  present.
   */
   private Tran3D getOrientationMatrix()
   {
      Attribute attr = getDataSet().getAttribute( Attribute.ORIENT_MATRIX );
      if ( attr == null || !(attr instanceof Float2DAttribute) )
        return null;

      float matrix[][] = ((Float2DAttribute)attr).getFloatValue();

      if ( matrix.length    != 3  ||
           matrix[0].length != 3  ||
           matrix[1].length != 3  ||
           matrix[2].length != 3  )
      {
        System.out.println("ERROR: Invalid orientation matrix in " +
                           " getOrientationMatrix()" );
        return null;
      }

      LinearAlgebra.print( matrix ); 
      for ( int i = 0; i < 3; i++ )
        for ( int j = 0; j < 3; j++ )
          matrix[i][j] *= ((float)Math.PI * 2);

      return new Tran3D( matrix ); 
   }


  /* --------------------------- oddNumSteps ------------------------------ */
  /*
   *  Calculate an odd number of steps that covers the requested distance 
   *  using approximately the requested step size.  The result is clamped
   *  to be between MIN_STEPS and MAX_STEPS;
   */
  private int oddNumSteps( float distance, float step_size )
  {
    if ( step_size == 0 )                  // fix the parameters, if needed
      step_size = 1;

    if ( step_size < 0 )
      step_size = -step_size;

    if ( distance < 0 )
      distance = -distance;

    float float_steps = distance / step_size;

    if ( float_steps > MAX_STEPS )
      float_steps = MAX_STEPS;

    if ( float_steps < MIN_STEPS )
      float_steps = MIN_STEPS;

    int odd_steps = 2 * (int)(float_steps/2) + 1;

    return odd_steps;
  }



  /* ------------------------- extractSlice -------------------------- */
  /*
   *  Get a slice in the appropriate space, determined by the slice selector.
   *
   */
  private VirtualArray2D extractSlice()
  {
    VirtualArray2D va2D = null;

    float width     = slice_selector.getSliceWidth();
    float height    = slice_selector.getSliceHeight();
    float thickness = slice_selector.getSliceThickness();
    float step      = slice_selector.getStepSize();
    SlicePlane3D plane = slice_selector.getPlane();

    float slice[][] = null;

    if ( slice_selector.getDisplayMode() == SliceSelectorUI.HKL_MODE )
    {                                           
                                                    // Display in HKL space
      if ( slice_selector.getSliceMode() == SliceSelectorUI.QXYZ_MODE )
      {
        SlicePlane3D new_plane = MapPlaneToHKL( plane );
        float scale = 1/ScaleFactor( plane );

        width     *= scale;
        height    *= scale;
        thickness *= scale;
        step      *= scale;
        plane      = new_plane;

        slice = extractHKL_Slice( plane, width, height, thickness, step );
      }
      else
        slice = extractHKL_Slice( plane, width, height, thickness, step );

      va2D = new VirtualArray2D( slice );

      if ( isAligned( plane ) )
      {
        setHKL_Title( plane.getOrigin(), plane.getNormal(), va2D );
        setHKL_Axis( AxisInfo.X_AXIS, -width/2.0f, width/2.0f, 
                     plane.getOrigin(), plane.getU(), va2D );
        setHKL_Axis( AxisInfo.Y_AXIS, -height/2.0f, height/2.0f, 
                     plane.getOrigin(), plane.getV(), va2D );
      }
      else
      {
        setGeneralTitle( plane.getOrigin(), plane.getNormal(), va2D );
        setGeneralAxis( AxisInfo.X_AXIS,
                        -width/2.0f, width/2.0f, plane.getU(), va2D );
        setGeneralAxis( AxisInfo.Y_AXIS,
                       -height/2.0f, height/2.0f, plane.getV(), va2D );
      }
    }

    else                                            // Display in Qxyz space
    {
      if ( slice_selector.getSliceMode() == SliceSelectorUI.HKL_MODE )
      {
        SlicePlane3D new_plane = MapPlaneToQ( plane );
        float scale = ScaleFactor( plane );

        width     *= scale;
        height    *= scale;
        thickness *= scale;
        step      *= scale;
        plane      = new_plane;

        slice = extractQ_Slice( plane, width, height, thickness, step );
      }
      else
        slice = extractQ_Slice( plane, width, height, thickness, step );

      va2D = new VirtualArray2D( slice );
      setGeneralTitle( plane.getOrigin(), plane.getNormal(), va2D );
      setGeneralAxis( AxisInfo.X_AXIS, 
                      -width/2.0f, width/2.0f, plane.getU(), va2D );
      setGeneralAxis( AxisInfo.Y_AXIS, 
                      -height/2.0f, height/2.0f, plane.getV(), va2D );
    }
 
    if ( slice == null )
      return null;

    return va2D;
  }


  /* ----------------------- extractHKL_Slice -------------------------- */
  /*
   *  Actually get the slice in the appropriate plane in HKL space with 
   *  the required width, height, thickness and resolution.
   *
   */
  private float[][] extractHKL_Slice( SlicePlane3D plane, 
                                      float        width, 
                                      float        height,
                                      float        thickness,
                                      float        step  )
  {
    Vector3D origin = plane.getOrigin();
    Vector3D u      = plane.getU();
    Vector3D v      = plane.getV();
    u.multiply( width/2 );
    v.multiply( height/2 );
    int n_rows = (oddNumSteps( height, step ) - 1) / 2;
    int n_cols = (oddNumSteps( width,  step ) - 1) / 2;
    return extractor.HKL_Slice( orientation_matrix, 
                                origin, u, v, 
                                n_cols, n_rows );
  }



  /* ----------------------- extractQ_Slice -------------------------- */
  /*
   *  Actually get the slice in the appropriate plane in Q space with 
   *  the required width, height, thickness and resolution.
   *
   */
  private float[][] extractQ_Slice( SlicePlane3D plane,
                                    float        width,
                                    float        height,
                                    float        thickness,
                                    float        step  )
  {
    Vector3D origin = plane.getOrigin();
    Vector3D u      = plane.getU();
    Vector3D v      = plane.getV();
    u.multiply( width/2 );
    v.multiply( height/2 );
    int n_rows = (oddNumSteps( height, step ) - 1) / 2;
    int n_cols = (oddNumSteps( width,  step ) - 1) / 2;
    return extractor.Q_Slice( origin, u, v, n_cols, n_rows );
  }


  /* -------------------------- setCurrentPoint ------------------------- */
  /*
   * This method will set the current world coord point, displayed by the
   * cursor readout.
   */
  private void setCurrentPoint( floatPoint2D current_pt )
  {
    cursor_output.setValue( 0, current_pt.x );
    cursor_output.setValue( 1, current_pt.y );
  }



  /* ----------------------- MapPlaneToQ -------------------------------- */
  /*
   *  Map the specified plane from HKL to Q, as nearly as possible.
   */
  private SlicePlane3D MapPlaneToQ( SlicePlane3D plane )
  {
    Vector3D origin = new Vector3D( plane.getOrigin() );
    Vector3D u      = new Vector3D( plane.getU() );
    Vector3D v      = new Vector3D( plane.getV() );

    orientation_matrix.apply_to( origin, origin );
    orientation_matrix.apply_to( u, u );
    orientation_matrix.apply_to( v, v );

    return new SlicePlane3D( origin, u, v );  // Note: constructing the new
                                              // plane from these vectors WILL
                                              // build new orthonormal vectors
                                              // in the plane object
  }


  /* ----------------------- MapPlaneToHKL ------------------------------ */
  /*
   *  Map the specified plane from Q to HKL, as nearly as possible.
   */
  private SlicePlane3D MapPlaneToHKL( SlicePlane3D plane )
  {
    Vector3D origin = new Vector3D( plane.getOrigin() );
    Vector3D u      = new Vector3D( plane.getU() );
    Vector3D v      = new Vector3D( plane.getV() );

    Tran3D inverse = new Tran3D( orientation_matrix );
    inverse.invert();

    inverse.apply_to( origin, origin );
    inverse.apply_to( u, u );
    inverse.apply_to( v, v );

    return new SlicePlane3D( origin, u, v );  // Note: constructing the new
                                              // plane from these vectors WILL
                                              // build new orthonormal vectors
                                              // in the plane object
  }


  /* ------------------------- isAligned ------------------------------- */
  /*
   *  Check whether or not the specified plane is aligned with the coordinate
   *  axes.
   */
  private boolean isAligned( SlicePlane3D plane )
  {
    final double THRESHOLD = 0.9999;   // if dot products are this close to
                                       //  +- 1 then we assume the plane is
                                       // aligned with the coordinate axes.

    Vector3D i_vec = new Vector3D( 1, 0, 0 );
    Vector3D j_vec = new Vector3D( 0, 1, 0 );
    Vector3D k_vec = new Vector3D( 0, 0, 1 );

    Vector3D u = plane.getU();
    if (  Math.abs( u.dot( i_vec ) ) < THRESHOLD  &&
          Math.abs( u.dot( j_vec ) ) < THRESHOLD  &&
          Math.abs( u.dot( k_vec ) ) < THRESHOLD   )
      return false; 
    
    Vector3D v = plane.getV();
    if (  Math.abs( v.dot( i_vec ) ) < THRESHOLD  &&
          Math.abs( v.dot( j_vec ) ) < THRESHOLD  &&
          Math.abs( v.dot( k_vec ) ) < THRESHOLD   )
      return false;   

                   // since the planes u,v,n vectors are orthonormal, we don't
                   // need to check the plane normal.
    return true;
  }


  /* ------------------------- ScaleFactor ----------------------------- */
  /*
   *  Calculate the scale factor to use when converting from HKL to Q, for
   *  a particular slice plane.  The scale factor is taken to be the ratio
   *  of the lengths of "u" in the original plane in HKL to the length of
   *  A*u in Q, where A is the "orientation matrix".
   */
  private float ScaleFactor( SlicePlane3D plane )
  {
    Vector3D u     = new Vector3D( plane.getU() ); 
    Vector3D new_u = new Vector3D();

    orientation_matrix.apply_to( u, new_u );

    return new_u.length()/u.length();
  }


  /* -------------------------------------------------------------------------
   *
   *  INTERNAL CLASSES
   *
   */

  /* --------------------------- SliceListener -------------------------- */
  /*
   *  Listen for new slice selection
   */
  private class SliceListener implements ActionListener
  {
    public void actionPerformed( ActionEvent e )
    {
      String message = e.getActionCommand();

      if ( message.equals( ISlicePlaneSelector.SLICE_MODE_CHANGED ) )
      {
        // transform values in the selector to the new space
        SlicePlane3D plane = slice_selector.getPlane();

        if ( slice_selector.getSliceMode() == SliceSelectorUI.QXYZ_MODE )
          plane = MapPlaneToQ( plane );
        else
          plane = MapPlaneToHKL( plane );

        slice_selector.setPlane( plane );
        redraw( "Slice Changed" );
      }
      else
        redraw( "Slice Changed" );
    }
  }


  /* -------------------------- ImageListener -------------------------- */
  /*
   *  Listen for messages from the ImageViewComponent movement 
   */
  private class ImageListener implements ActionListener
  {
    public void actionPerformed( ActionEvent e )
    {
      String message = e.getActionCommand();
      if ( message.equals( IViewComponent.POINTED_AT_CHANGED ) )
      {
        setCurrentPoint( ivc.getPointedAt() );
      }
    }
  }


  /* ----------------------------- main ------------------------------------ */
  /*
   *  For testing purposes only
   */
  public static void main(String[] args)
  {
    String dir_name = "/usr2/ARGONNE_DATA/";
    String file_name = dir_name + "SCD_QUARTZ_2_DET/scd08336.run";
    RunfileRetriever rr = new RunfileRetriever( file_name );
    DataSet ds = rr.getDataSet(2);
    rr = null;

    String orientation_file = dir_name + "SCD_QUARTZ_2_DET/junk.mat";
    Operator op = new LoadOrientation( ds, orientation_file );
    op.getResult();

    String calib_file_name = dir_name + "SCD_QUARTZ_2_DET/instprm.dat";
    LoadSCDCalib load_cal = new LoadSCDCalib( ds, calib_file_name, -1, null );
    load_cal.getResult();

    HKL_SliceView view = new HKL_SliceView( ds, null );
    JFrame f = new JFrame("Test for HKL_SliceView");
    f.setBounds(0,0,600,400);
    f.setJMenuBar( view.getMenuBar() );
    f.getContentPane().add( view );
    f.setVisible( true );
  }

}

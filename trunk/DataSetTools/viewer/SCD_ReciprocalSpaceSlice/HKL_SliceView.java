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
  
  private boolean hkl_mode = true;
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
    tabbed_pane.addTab( "Conv", table_panel );
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
    System.out.println( "redraw of viewer called ..........." + reason );
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

    float slice[][] = extractSlice();

    System.out.println("Recalculated slice....." );
    if ( slice != null )
    {
      IVirtualArray2D va2D = new VirtualArray2D( slice );
      setAxesAndTitle( va2D );
      ivc.dataChanged( va2D );
    }
    else
      System.out.println("ERROR: null slice in HKL_SliceView.java");

    if ( debug )
    {      
      System.out.println("IN redraw");
      System.out.println("Array Size = " +slice.length+ " X "+slice[0].length);
      float min = slice[0][0];
      float max = slice[0][0];
      for ( int row = 0; row < slice.length; row++ )
        for ( int col = 0; col < slice[0].length; col++ )
          if ( slice[row][col] < max )
            max = slice[row][col];
          else if ( slice[row][col] > min )
            min = slice[row][col];
      System.out.println("min, max = " + min + ", " + max );
    }
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

    if ( orientation_matrix == null )         // ##### in the long run this
    {
      slice_selector.setMode( SliceSelectorUI.QXYZ_MODE ); 
      hkl_mode = false;                       // should just disable/enable
    }
    else                                      // selection of HKL mode
    {
      slice_selector.setMode( SliceSelectorUI.HKL_MODE ); 
      hkl_mode = true;
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
    System.out.println("setAxesAndTitle called .....................");
    SlicePlane3D plane = slice_selector.getPlane();
 
    Vector3D origin = plane.getOrigin();
    Vector3D normal = plane.getNormal();
    Vector3D u      = plane.getU();
    Vector3D v      = plane.getV();
    float width  = slice_selector.getSliceWidth();
    float height = slice_selector.getSliceHeight();

    if ( hkl_mode && orientation_matrix != null )
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
     if ( hkl_mode )
       units = "HKL units";
     else
       units = "Inverse Angstroms";

     va2D.setAxisInfo( axis_num, min, max,
                      "Direction:(" + Format.real( vector.get()[0], 5 ) +
                                "," + Format.real( vector.get()[1], 5 ) +
                                "," + Format.real( vector.get()[2], 5 ) + ")",
                       units,
                       true );
     System.out.println("setGeneralAxis : " + va2D.getAxisInfo( 0 ) );
     System.out.println("setGeneralAxis : " + va2D.getAxisInfo( 1 ) );
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

     System.out.println("setHKLAxis : " + va2D.getAxisInfo( 0 ) );
     System.out.println("setHKLAxis : " + va2D.getAxisInfo( 1 ) );
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

  /* --------------------------- oddNumRows ------------------------------ */
  /*
   *  Calculate an odd number of rows that covers the requested height 
   *  using approximately the requested step size.  The result is clamped
   *  to be between MIN_STEPS and MAX_STEPS; 
   */
  private int oddNumRows()
  {
    float step_size = slice_selector.getStepSize();
    float height    = slice_selector.getSliceHeight();

    float float_rows = height / step_size; 

    if ( float_rows > MAX_STEPS )
      float_rows = MAX_STEPS;

    if ( float_rows < MIN_STEPS )
      float_rows = MIN_STEPS; 

    int odd_rows = 2 * (int)(float_rows/2) + 1;

    return odd_rows;
  }


  /* --------------------------- oddNumCols ------------------------------ */
  /*
   *  Calculate an odd number of columns that covers the requested width 
   *  using approximately the requested step size.  The result is clamped
   *  to be between MIN_STEPS and MAX_STEPS;
   */
  private int oddNumCols()
  {
    float step_size = slice_selector.getStepSize();
    float width     = slice_selector.getSliceWidth();

    float float_cols = width / step_size;

    if ( float_cols > MAX_STEPS )
      float_cols = MAX_STEPS;

    if ( float_cols < MIN_STEPS )
      float_cols = MIN_STEPS;

    int odd_cols = 2 * (int)(float_cols/2) + 1;

    return odd_cols;
  }


  /* ------------------------- extractSlice -------------------------- */
  /*
   *  Actually get the slice with the appropriate center, width, height
   *  and thicknes.
   *
   */
  private float[][] extractSlice()
  {
    int n_rows = ( oddNumRows() - 1 ) / 2;
    int n_cols = ( oddNumCols() - 1 ) / 2;
    SlicePlane3D plane = slice_selector.getPlane();
    Vector3D origin = plane.getOrigin();
    Vector3D u      = plane.getU();
    Vector3D v      = plane.getV();
    u.multiply( slice_selector.getSliceWidth()/2 );
    v.multiply( slice_selector.getSliceHeight()/2 );

    float slice[][];
    if ( orientation_matrix != null )
      slice = extractor.HKL_Slice( orientation_matrix,
                                   origin, u, v,
                                   n_cols, n_rows );
    else
      slice = extractor.Q_Slice( origin, u, v, n_cols, n_rows );
 
    return slice;
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
      System.out.println( "Slice changed ....................... ");
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
      //System.out.println( "cursor moved " + e );
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

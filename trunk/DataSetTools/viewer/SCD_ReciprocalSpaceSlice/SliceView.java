/*
 * File:  SliceView.java
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
 * Revision 1.1  2004/01/27 23:23:41  dennis
 * SCD HKL SliceViewer
 *
 */

package DataSetTools.viewer.SCD_ReciprocalSpaceSlice;

import DataSetTools.components.View.TwoD.*;
import DataSetTools.components.ui.*;
import DataSetTools.components.containers.*;

import DataSetTools.trial.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.retriever.*;
import DataSetTools.components.View.*;
import DataSetTools.viewer.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * Provides a mechanism for selecting and viewing slices through reciprocal
 * space for an SCD DataSet.  If the DataSet has an orientation matrix as
 * an attribute, the slices may specified in terms of Miller indices, otherwise
 * the slices are specified in terms of Q.
 *
 * @see DataSetTools.dataset.DataSet
 * @see DataSetTools.viewer.DataSetViewer
 */

public class SliceView extends DataSetViewer
{                         
  public static final int MIN_STEPS = 3;
  public static final int MAX_STEPS = 10001;
  
  // the split pane, image_container, control_panel and slice_selector 
  // are made once in the constructor

  private SplitPaneWithState split_pane;
  private JPanel             image_container; 
  private JPanel             control_panel; 
  private SliceSelectorUI    slice_selector;

  // the ivc must be reconstructed whenever the rows and colums change.

  private ImageViewComponent ivc = null;

  // the Q_SliceExtractor must be reconstructed whenever the DataSet is
  // changed

  private Q_SliceExtractor extractor = null;

  private boolean valid_ds = false; 


/* --------------------------------------------------------------------------
 *
 * CONSTRUCTORS
 *
 */

  /* -------------------------------------------------------------------- */

  public SliceView( DataSet data_set, ViewerState state ) 
  {
    super(data_set, state);  // Records the data_set and current ViewerState
                             // object in the parent class and then
                             // sets up the menu bar with items handled by the
                             // parent class.

    slice_selector  = new SliceSelectorUI( "Select Slice" );

    image_container = new JPanel();
    image_container.setLayout( new GridLayout(1,1) );

    control_panel   = new JPanel();
    split_pane      = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
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

    init();
  }

  /* -----------------------------------------------------------------------
   *
   *  PUBLIC METHODS
   *
   */


  public void redraw( String reason )
  {
    if ( ! valid_ds )
    {
      image_container.removeAll();
      image_container.add( 
             new JLabel("ERROR:SCD DataSet with orientation matrix required") );
      return;
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

    init();

    redraw( NEW_DATA_SET );
    setVisible( true );
  }


  /* -----------------------------------------------------------------------
   *
   * PRIVATE METHODS
   *
   */ 
  private void init()
  {
    ivc = new ImageViewComponent( new VirtualArray2D(200,200) );

    resetBorderTitles();
    image_container.removeAll();
    image_container.add( ivc.getDisplayPanel() );

    redraw( NEW_DATA_SET );
  }


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
    Attribute attr;
    return true; 
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


  /* -------------------------------------------------------------------------
   *
   *  INTERNAL CLASSES
   *
   */

  /**
   *  Listen for resize events on the and just print out the new size to
   *  to demonstrate how to handle such events.
   */
  class ViewComponentAdapter extends ComponentAdapter
  {
    public void componentResized( ComponentEvent c )
    {
      System.out.println("View Area resized: " + c.getComponent().getSize() );
    }
  }


  /* ----------------------------- main ------------------------------------ */
  /*
   *  For testing purposes only
   */
  public static void main(String[] args)
  {
    String file_name = "/home/dennis/ARGONNE_DATA/gppd6100.run";
    RunfileRetriever rr = new RunfileRetriever( file_name );
    DataSet data_set = rr.getDataSet(1);
    rr = null;

    SliceView view = new SliceView( data_set, null );
    JFrame f = new JFrame("Test for SliceView");
    f.setBounds(0,0,600,400);
    f.setJMenuBar( view.getMenuBar() );
    f.getContentPane().add( view );
    f.setVisible( true );
  }

}

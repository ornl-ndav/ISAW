/*
 * File:  SANDWedgeViewer.java
 *
 * Copyright (C) 2003-2004, Mike Miller
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
 * Primary   Mike Miller <millermi@uwstout.edu>
 * Contact:  Student Developer, University of Wisconsin-Stout
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
 * Revision 1.41  2004/10/21 01:35:01  millermi
 * - Project Directory restored immediately so Load S(Qx,Qy) will
 *   begin in the last directory saved if SandProps.isv exists.
 * - Corrected title of graph when radial line integration is selected.
 *
 * Revision 1.40  2004/09/15 22:00:24  millermi
 * - Updated LINEAR, TRU_LOG, and PSEUDO_LOG setting for AxisInfo class.
 *   Adding a second log required the boolean parameter to be changed
 *   to an int. These changes may affect any ObjectState saved configurations
 *   made prior to this version.
 *
 * Revision 1.39  2004/07/10 06:38:31  millermi
 * - Added functionality for all selections to be written to
 *   file. Previously, only the last selection was written to file.
 *
 * Revision 1.38  2004/07/01 18:58:25  millermi
 * - Added comments to loadData(), specifying the format of header
 *   information.
 *
 * Revision 1.37  2004/06/01 21:56:41  millermi
 * - Switched the label and unit values.
 * - Relabeled the integration control.
 * - Initialize the center of the selection to (0,0).
 *
 * Revision 1.36  2004/05/11 02:01:53  millermi
 * - Changed integrate label from "Integrate by:" to "Integrate with:".
 *
 * Revision 1.35  2004/05/04 01:50:36  millermi
 * - Default close operation now set to DISPOSE_ON_CLOSE.
 * - Overloaded dispose() method so subcomponent windows are also closed.
 * - main() now has a window listener to exit when the viewer is used
 *   as a stand-alone tool.
 * - Controls now placed in tabbed pane instead of having a separate
 *   editor for the controls.
 * - Added integration over an angle, now users can choose between
 *   integration methods. This capability forces all selections to
 *   be of one integration method.
 *
 * Revision 1.34  2004/04/16 18:58:58  millermi
 * - Removed parenthesis from units passed to the ViewManager.
 *
 * Revision 1.33  2004/04/12 03:44:44  millermi
 * - Removed parenthesis from x and y units.
 * - Changed initial size to 700 x 525, which expanded the
 *   height of the viewer to compensate for the new marker
 *   control added to the ImageViewComponent.
 *
 * Revision 1.32  2004/03/30 03:21:02  millermi
 * - Added flexible file reading, now size of array and other
 *   attributes can be specified within the data file being read.
 *   The attributes must have form:
 *   # AttributeName: Attribute
 *
 * Revision 1.31  2004/03/15 19:33:49  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.30  2004/03/15 03:27:28  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.29  2004/03/10 23:37:27  millermi
 * - Changed IViewComponent interface, no longer
 *   distinguish between private and shared controls/
 *   menu items.
 * - Combined private and shared controls/menu items.
 *
 * Revision 1.28  2004/03/10 21:08:20  millermi
 * - Added menu item for saving image as a JPEG.
 *
 * Revision 1.27  2004/03/10 17:39:42  millermi
 * - Cursor readout control from ImageViewComponent is
 *   filtered out of the controls.
 *
 * Revision 1.26  2004/03/10 16:30:23  millermi
 * - Added filename to header of Results Files.
 * - Focus now returned to the ImageViewComponent after
 *   the ViewManager is no longer visible.
 * - Bin IDs now correspond to where the selection is stored
 *   in the Vector of selections.
 * - Added File|Exit and File|Print Image to menus.
 * - Fixed bug that did not allow a runfile to be loaded
 *   after an invalid runfile was unsuccessfully loaded.
 * - Rearranged menu items into more logical categories.
 *
 * Revision 1.25  2004/03/05 04:46:29  millermi
 * - Removed unnecessary variables from binarySearch().
 * - Added comments to code for better readability.
 *
 * Revision 1.24  2004/02/28 00:32:59  millermi
 * - Added ivc.preserveAspectRatio(true) to prevent image distortion.
 *
 * Revision 1.23  2004/02/16 05:44:44  millermi
 * - Now uses error functionality provided with theIVirtualArray2D
 *   class.
 * - made integrate() method more efficient by getting the array
 *   out of the virtual array and replacing getDataValue(row,col)
 *   with data_array[row][col].
 *
 * Revision 1.22  2004/02/14 03:39:08  millermi
 * - Updated changes made to ImageViewComponent.
 * - changed "Load/Save Project Settings" to "Load/Save Project"
 * - Added PROJECT_FILE key. This will allow the data file to
 *   be loaded automatically when "Load Project" is selected.
 * - setView() in ImageListener is now only called if a region is
 *   added or removed. This was causing other operations to slow
 *   because every message sent by IVC triggered this method.
 *
 * Revision 1.21  2004/02/11 22:48:37  millermi
 * - ViewManager is now updated when selections are removed
 *   from the image.
 * - Commented out Q range debug statement in integrate()
 * - Revised setData(), no longer concerned with size of the
 *   2-d virtual array.
 *
 * Revision 1.20  2004/01/29 08:23:06  millermi
 * - Updated the getObjectState() to include parameter for specifying
 *   default state.
 * - Added static variables DEFAULT and PROJECT to IPreserveState for
 *   use by getObjectState()
 * - Added ability to save user preferences. See Help|SANDWedgeViewer
 *   for more details.
 *
 * Revision 1.19  2004/01/24 03:06:37  millermi
 * - Minor change to help()
 *
 * Revision 1.18  2004/01/24 02:55:26  millermi
 * - Added File|Save Results menu item. This allows the
 *   results from the SWV to be neatly written out to file.
 * - Results window is now has toFront() called on it when
 *   a new selection is added.
 * - Moved field labels out of SANDEditor and into whole
 *   class. This allows them to be used when writing out
 *   the file.
 *
 * Revision 1.17  2004/01/23 22:59:12  millermi
 * - Results window now automatically popped up when a selection
 *   is made. The menu item previously used to show the results
 *   now will read "Hide Results Window" when the window is
 *   visible and "Show Results Window" when the window is not.
 * - Changed how the distance was calculated. Now goes from center of
 *   image, not center of Region.
 * - Removed all references to the FunctionViewComponent, now housed
 *   in the ViewManager.
 * - Changed label from "Width of Angle" to "Interior Angle" for
 *   Wedge and DoubleWedge field labels (in SANDEditor).
 *
 * Revision 1.16  2004/01/20 00:59:59  millermi
 * - Since Save is not yet implemented, added information in
 *   the help menu to help users find the save option.
 *
 * Revision 1.15  2004/01/19 23:38:43  millermi
 * - Fixed bug that caused file to be read into the array incorrectly.
 *   Now the first element is read into array[NUM_ROWS-1][0].
 * - Now checks if data exceeds array bounds, this prevents
 *   ARRAY_OUT_OF_BOUNDS exception.
 *
 * Revision 1.14  2004/01/09 21:08:17  dennis
 * Added cacluation of error estimates.
 *
 * Revision 1.13  2004/01/08 22:26:03  millermi
 * - Separated off the FunctionViewComponent. Now use "View Results"
 *   under the options menu to view a ViewManager with all of the
 *   old views, including the graph and table view.
 * - The ViewManager is always initialized with the graph view
 *   displaying the results.
 * - More testing needs to be done on this version...Please test and
 *   give feedback.
 *
 * Revision 1.12  2004/01/08 21:07:33  millermi
 * - Fixed bug introduced when world coord conversion took place.
 *
 * Revision 1.11  2004/01/08 20:14:46  millermi
 * - Made viewing selection info available.
 * - Selection defining points stored in attributes now
 *   converted to world coords.
 * - Expanded bounds of the SAND Editor.
 * - Known bug: Graphs corresponding to selections no longer
 *   disappear when the selection is removed.
 *
 * Revision 1.10  2004/01/07 06:47:39  millermi
 * - New float Region parameters have been updated.
 *
 * Revision 1.9  2004/01/06 20:28:16  dennis
 * Fixed some problems with labels.
 * Now displays intensity vs Q graph.
 * Some problems remain with the graph region and log axes.
 *
 * Revision 1.8  2004/01/03 04:40:23  millermi
 * - help() now uses html toolkit
 * - replaced setVisible(true) with WindowShower.
 * - Added code for world-to-image transform, however transform
 *   is not currently being used.
 *
 * Revision 1.7  2003/12/30 00:39:37  millermi
 * - Added Annular selection capabilities.
 * - Changed SelectionJPanel.CIRCLE to SelectionJPanel.ELLIPSE
 *
 * Revision 1.6  2003/12/29 07:54:31  millermi
 * - Added editor which enables user to make a selection
 *   by entering defining characteristics.
 * - ***Editing still unavailable*** This editor will
 *   only work for creating new selections. The next
 *   version should contain the ability to edit selections.
 *
 * Revision 1.5  2003/12/23 02:21:36  millermi
 * - Added methods and functionality to allow enabling/disabling
 *   of selections.
 * - Fixed interface package changes where applicable.
 *
 * Revision 1.4  2003/12/20 22:18:49  millermi
 * - Orphaned windows are now disposed when setData() is called.
 * - Added simple help() for new users. More detail needed.
 *
 * Revision 1.3  2003/12/20 11:11:52  millermi
 * - Introduced DataSet concept. Coordinated the
 *   ImageViewComponent with the FunctionViewComponent.
 *   THE VIEWER IS NOW FUNCTIONABLE!!!
 * - Known bug: PanViewControl causes layout issues because
 *   it calculated preferredSize on the fly.
 *
 * Revision 1.2  2003/12/20 03:55:43  millermi
 * - Fixed javadocs error.
 *
 * Revision 1.1  2003/12/19 02:20:09  millermi
 * - Initial Version - Adds specialized functionality to the
 *   IVCTester class. This class allows users to specifically
 *   view SAND data files.
 * - Currently, the FunctionViewComponent is unavailable.
 *   Once this is added, calculations on selected regions will
 *   be possible.
 *
 */

package DataSetTools.components.View;

import gov.anl.ipns.Util.File.RobustFileFilter;
import gov.anl.ipns.Util.File.TextFileReader;
import gov.anl.ipns.Util.Messaging.IObserver;
import gov.anl.ipns.Util.Numeric.Format;
import gov.anl.ipns.Util.Numeric.floatPoint2D;
import gov.anl.ipns.Util.Sys.PrintComponentActionListener;
import gov.anl.ipns.Util.Sys.SaveImageActionListener;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.Cursor.SelectionJPanel;
import gov.anl.ipns.ViewTools.Components.Menu.MenuItemMaker;
import gov.anl.ipns.ViewTools.Components.Menu.ViewMenuItem;
import gov.anl.ipns.ViewTools.Components.Region.*;
import gov.anl.ipns.ViewTools.Components.Transparency.SelectionOverlay;
import gov.anl.ipns.ViewTools.Components.TwoD.ImageViewComponent;
import gov.anl.ipns.ViewTools.Components.ViewControls.CursorOutputControl;
import gov.anl.ipns.ViewTools.Components.ViewControls.FieldEntryControl;
import gov.anl.ipns.ViewTools.Components.ViewControls.ViewControl;
import gov.anl.ipns.ViewTools.Panels.Transforms.*;
import gov.anl.ipns.ViewTools.UI.FontUtil;
import gov.anl.ipns.ViewTools.UI.SplitPaneWithState;

import javax.swing.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.io.IOException;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.html.HTMLEditorKit;

import DataSetTools.util.SharedData;
import DataSetTools.viewer.IViewManager;
import DataSetTools.viewer.ViewManager;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.FunctionTable;
import DataSetTools.dataset.UniformXScale;
import DataSetTools.dataset.Float1DAttribute;

/**
 * Simple class to display an image, specified by an IVirtualArray2D or a 
 * 2D array of floats, in a frame. This class adds further implementation to
 * the ImageFrame2.java class for thorough testing of the ImageViewComponent.
 */
public class SANDWedgeViewer extends JFrame implements IPreserveState,
                                                       Serializable
{
 /**
  * "ImageViewComponent" - This constant String is a key for referencing
  * the state information about the ImageViewComponent. Since the
  * ImageViewComponent has its own state, this value is of type ObjectState,
  * and contains the state of the ImageViewComponent. 
  */
  public static final String IMAGE_VIEW_COMPONENT    = "ImageViewComponent";
  
 /**
  * "ViewerSize" - This constant String is a key for referencing
  * the state information about the size of the viewer at the time
  * the state was saved. The value this key references is of type
  * Dimension.
  */
  public static final String VIEWER_SIZE           = "Viewer Size";
  
 /**
  * "Data Directory" - This constant String is a key for referencing
  * the state information about the location of the data files being
  * loaded by this viewer. The value this key references is of type
  * String.
  */
  public static final String DATA_DIRECTORY        = "Data Directory";
  
 /**
  * "Project File" - This constant String is a key for referencing
  * the state information about the current data file being viewed with
  * this viewer. The value this key references is of type String.
  */
  public static final String PROJECT_FILE        = "Project File";
  
  private static JFrame helper = null;
  
  // complete viewer, includes controls and ijp
  private transient SplitPaneWithState pane;
  private transient ImageViewComponent ivc;
  private transient IVirtualArray2D data;
  private transient JMenuBar menu_bar;
  private transient DataSet data_set;
  private String projectsDirectory = SharedData.getProperty("Data_Directory");
  private transient SANDControlPanel sandcontrolpane;
  private transient CoordTransform image_to_world_tran = new CoordTransform();
  private transient ViewManager oldview;
  private transient SANDWedgeViewer this_viewer;
  private String[] ellipselabels;
  private String[] wedgelabels;
  private String[] ringlabels;
  private String   datafile;
  private boolean os_region_added = false;
  private boolean integrate_by_ring = true; // If false, integrate wrt radii,
                                            // summing over an angle.

 /**
  * Construct a frame with no data to start with. This constructor will be
  * used when data is being loaded in.
  */
  public SANDWedgeViewer()
  {
    init(null);
  }

 /**
  * Construct a frame with the specified image and title
  *  
  *  @param  iva
  */
  public SANDWedgeViewer( IVirtualArray2D iva )
  {
    init(iva);
  }

 /**
  * Construct a frame with the specified image and title
  *  
  *  @param  array
  *  @param  xinfo
  *  @param  yinfo
  *  @param  title
  */  
  public SANDWedgeViewer( float[][] array, 
                          float[][] err_array,
                          AxisInfo xinfo,
		          AxisInfo yinfo,
		          String title )
  {
    VirtualArray2D temp = new VirtualArray2D( array, err_array );
    temp.setAxisInfo( AxisInfo.X_AXIS, xinfo.copy() );
    temp.setAxisInfo( AxisInfo.Y_AXIS, yinfo.copy() );
    temp.setTitle(title);
    
    init(temp);
  }
 
 /**
  * This method sets the ObjectState of this viewer to a previously saved
  * state.
  *
  *  @param  new_state The previously saved state that this viewer will be
  *                    set to.
  */ 
  public void setObjectState( ObjectState new_state )
  {
    boolean redraw = false;  // if any values are changed, repaint overlay.
    Object temp = new_state.get(PROJECT_FILE);
    if( temp != null )
    {
      datafile = (String)temp;
      loadData(datafile);
      redraw = true;  
    } 
    
    temp = new_state.get(IMAGE_VIEW_COMPONENT);
    if( temp != null )
    {
      Object os = ((ObjectState)temp).get(ImageViewComponent.SELECTION_OVERLAY);
      if( os != null )
      {
        os = ((ObjectState)os).get(SelectionOverlay.SELECTED_REGIONS);
	if( os != null )
          os_region_added = true;
      }
      if( ivc != null )
        ivc.setObjectState( (ObjectState)temp );
      redraw = true;  
    } 
    
    temp = new_state.get(VIEWER_SIZE); 
    if( temp != null )
    {
      setSize( (Dimension)temp );
      redraw = true;  
    } 
    
    temp = new_state.get(DATA_DIRECTORY); 
    if( temp != null )
    {
      projectsDirectory = (String)temp;
    } 
    
    if( redraw )
      repaint();
  }
 
 /**
  * This method will get the current values of the state variables for this
  * object. These variables will be wrapped in an ObjectState.
  *
  *  @param  isDefault Should selective state be returned, that used to store
  *                    user preferences common from project to project?
  *  @return if true, the default state containing user preferences,
  *          if false, the entire state, suitable for project specific saves.
  */ 
  public ObjectState getObjectState( boolean isDefault )
  {
    ObjectState state = new ObjectState();
    state.insert( IMAGE_VIEW_COMPONENT, ivc.getObjectState(isDefault) );
    state.insert( VIEWER_SIZE, getSize() );
    state.insert( DATA_DIRECTORY, new String(projectsDirectory) );
    // if they save the project, this file will automatically be loaded.
    if( !isDefault )
      state.insert( PROJECT_FILE, new String(datafile) );
    
    return state;
  }

 /**
  * Contains/Displays control information about this viewer.
  */
  public static void help()
  {
    helper = new JFrame("Introduction to the SAND Wedge Viewer");
    helper.setBounds(0,0,600,400);
    JEditorPane textpane = new JEditorPane();
    textpane.setEditable(false);
    textpane.setEditorKit( new HTMLEditorKit() );
    String text = "<H1>Description:</H1> <P>" + 
                "The SAND Wedge Viewer (SWV) in an interactive analysis tool." +
        	" SWV features the ability to make four selections: Wedge, " +
    		"Double Wedge, Annular, and Ellipse. Once a " +
    		"selection is made on the image, the graph will display " +
		"the intensity values per hit as a function of distance " +
		"in Q. Selections can be made in two ways: <BR>" +
		"1. Graphically using the SelectionOverlay<BR>" +
		"2. Entering defining information by pressing the Manual " +
		"Selection button.</P>" + 
		"<H2>Commands for SWV</H2>" +
                "<P> SAVING USER PREFERENCES: Click on <B>File|Save User " +
		"Settings</B>. Your preferences will automatically be saved " +
		"in SandProps.isv in your home directory. <I>This option " +
		"will not save project specific information, such as " +
		"selections or annotations. Use <B>Options|Save Project " +
		"Settings</B> to save project specific details.</I><BR><BR>" +
		"<I>ATTENTION: Selections must be made before using the " +
		"viewing or saving results to file. </I><BR><BR> " +
		"VIEW RESULTS: The Results window will automatically appear " +
		"after a selection has been made. <B>Options|Hide Results " +
		"Window</B> will hide the window. If the window is not " +
		"visible, <B>Options|Show Results Window</B> will cause the " +
		"results window to appear.<BR>"+
		"SAVE RESULTS TO FILE: Go to <B>File|Save Results</B> " +
		"in the SWV. The new file has 3 columns: Q, Intensity, " +
		"and Error Bounds. Information about the region is listed " +
		"at the top of the file, prefixed by a pound symbol(#). " +
		"<I>If multiple selections are made, only the last " +
		"selection can be written to file.</I><BR>" +
		"<H2>TROUBLESHOOTING</H2>" +
		"<P><I>Why doesn't the cursor readout work?</I> Go to the " +
		"<B>Image</B> tab, make sure the Annotation and Selection " +
		"controls are unchecked.<br><br>" +
		"<I>How do I add annotations to the image?</I> " +
		"Detailed commands for each overlay can be found under " +
		"<B>Help|Overlays</B> after a data file has been loaded.<br>" +
		"<br><I>Why can I no longer change between integration " +
		"techniques?</I> All selections must be of the same " +
		"integration technique. To switch techniques, remove all " +
		"existing selections (Hold A and single click on the image)."+
		"</P>";
    textpane.setText(text);
    JScrollPane scroll = new JScrollPane(textpane);
    scroll.setVerticalScrollBarPolicy(
        			    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    helper.getContentPane().add(scroll);
    WindowShower shower = new WindowShower(helper);
    java.awt.EventQueue.invokeLater(shower);
    shower = null;
  }
 
 /**
  * This method loads a data array from the file specified. If no header
  * information is provided, the data is assumed to be a 200 x 200 array.
  * <BR><BR>
  * Header information must be listed at the top of the data file, and follow
  * the format below:
  * <BR><BR>
  * # Value Key: Value
  * <BR><BR>
  * Example: <B>
  * <BR>   # Row: 20
  * <BR>   # Column: 25
  * <BR>   # X Label: X_Label
  * <BR>   # X Units: SomeUnits </B>
  * <BR> This specifies there is a 20 x 25 array of data. This also specifies
  * the labels and units for X axis to be X_Label and SomeUnits, repectively.
  * <BR><BR>
  * <I>Possible Value Keys (not case sensitive):</I> 
  * <BR> row, column, x label, y label,
  * z label, x units, y units, z units.
  *
  *  @param  filename Filename of the data file being loaded.
  *  @return true if load was successful.
  */ 
  public boolean loadData( String filename )
  {
    datafile = filename;
    // this assumes the data is from a 200x200 array.
    int NUM_ROWS = 200;
    int NUM_COLS = 200;
    float[][] array = null;
    float[][] err_array = null;
    float qxmin = 0;
    float qymin = 0;
    float qxmax = 0;
    float qymax = 0;
    // these are default values, taylored toward SAND data
    String xlabel = "Qx";
    String xunit  = "Inverse Angstroms";
    String ylabel = "Qy";
    String yunit  = "Inverse Angstroms";
    String zlabel = "";
    String zunit  = "Intensity";
    TextFileReader reader = null;
    // try to open the file
    try
    {
      // file arranged in 4 columns: Qx, Qy, Value, Error
      // Since we only care about the min and max of Qx, Qy, only the first
      // and last values in those columns are saved. The Value and Error columns
      // are each stored in a separate 2-D array.
      reader = new TextFileReader( filename );
      String header_line = removeLeadingSpaces(reader.read_line());
      String key = "";
      String info = null;
      int colon_index = -1;
      // while a line starts with #, treat it as special information.
      while( header_line.startsWith("#") )
      {
        //System.out.println("Line: " + header_line + "***" + 
        //                 header_line.startsWith("#") );
	// remove # character from substring
	header_line = header_line.substring(1);
	// remove any spaces
	header_line = removeLeadingSpaces(header_line);
	colon_index = header_line.indexOf(":");
	// require that a colon precedes the info
	if( colon_index >= 0 )
	{
	  info = header_line.substring(colon_index + 1);
	  // remove any leading spaces
	  info = removeLeadingSpaces(info);
	  // Get everything from header line except the "#" symbol and
	  // leading spaces.
	  header_line = header_line.substring(0,colon_index).toLowerCase();
	  // Find and set the corresponding information
	  // the number of rows and columns should be specified.
	  if( header_line.indexOf("row") >= 0 )
	  {
	    NUM_ROWS = (new Integer(info)).intValue();
	  }
	  else if( header_line.indexOf("column") >= 0 )
	  {
	    NUM_COLS = (new Integer(info)).intValue();
	  }
	  // X LABEL
	  else if( header_line.indexOf("x lab") >= 0 ||
	      header_line.indexOf("xlab") >= 0 )
	  {
	    xlabel = info;
	  }
	  // X UNITS
	  else if( header_line.indexOf("x unit") >= 0 ||
	      header_line.indexOf("xunit") >= 0 )
	  {
	    xunit = info;
	  }
	  // Y LABEL
	  else if( header_line.indexOf("y lab") >= 0 ||
	      header_line.indexOf("ylab") >= 0 )
	  {
	    ylabel = info;
	  }
	  // Y UNITS
	  else if( header_line.indexOf("y unit") >= 0 ||
	      header_line.indexOf("yunit") >= 0 )
	  {
	    yunit = info;
	  }
	  // Z LABEL
	  else if( header_line.indexOf("z lab") >= 0 ||
	      header_line.indexOf("zlab") >= 0 )
	  {
	    zlabel = info;
	  }
	  // Z UNITS
	  else if( header_line.indexOf("z unit") >= 0 ||
	      header_line.indexOf("zunit") >= 0 )
	  {
	    zunit = info;
	  }
	}
        // read the next line to see if it is also header info, making sure
	// the line does not start with spaces
	header_line = removeLeadingSpaces(reader.read_line());
      }
      // get rid of any empty lines between the metadata and the data.
      while( header_line.equals("") )
        header_line = removeLeadingSpaces(reader.read_line());
      // No # sign, then data was read in. Replace it and read again later.
      // If last line did not contain any useful data, do not replace it.
      if( !(header_line.equals("") || header_line == null) )
        reader.unread();
      
      //System.out.println("Num Rows/Columns: " + NUM_ROWS + "/" + NUM_COLS);
      // set sizes of the array
      array     = new float[NUM_ROWS][NUM_COLS];
      err_array = new float[NUM_ROWS][NUM_COLS];
      int row = NUM_ROWS - 1;
      int col = 0;
      // read in first line of data, this will set the Qx/Qy min and read in
      // the first value.
      StringTokenizer datarow = new StringTokenizer(reader.read_line());
      qxmin = (new Float( datarow.nextToken() )).floatValue();
      qymin = (new Float( datarow.nextToken() )).floatValue();
      array[row][col]     = (new Float( datarow.nextToken() )).floatValue();
      err_array[row][col] = (new Float( datarow.nextToken() )).floatValue();
    
      row--;  // decrement row since first element was read in.
      // let the exception EOF end the loop.
      while( true )
      {
	// now read in the data one row at a time, each row contains a
	// Qx, Qy, Value, and Error value. Store the Values in the array
	// by column instead of row, starting at lower left-hand corner of
	// array, and ending in upper right-hand corner.
        datarow = new StringTokenizer(reader.read_line());
        qxmax = (new Float( datarow.nextToken() )).floatValue();
        qymax = (new Float( datarow.nextToken() )).floatValue();
        array[row][col] = (new Float( datarow.nextToken() )).floatValue();
        err_array[row][col] = (new Float( datarow.nextToken() )).floatValue();
	//System.out.println("Row/Col: (" + row + "," + col + ")" );
	
	// increment column if at last row, reset row to start.
	// this will cause the numbers to be read in by column
	if( row == 0 )
        {
          row = NUM_ROWS - 1;
	  col++;
        }
	// increment rows so data is read in by column
        else
          row--;
	// if file is too large for array, artificially throw the EOF exception
        if( col == NUM_COLS )
	  throw new IOException("End of file");
      } // end while
    }
    // either end of file or no file found
    catch( IOException e1 )
    {
      // done reading file
      if( e1.getMessage().equals("End of file") )
      {
	// set the source of the image_to_world transform
        // y min/max are swapped since IVC swaps them.
        image_to_world_tran.setDestination( qxmin, qymax, qxmax, qymin );
	image_to_world_tran.setSource( 0.001f, 0.001f, array[0].length-0.001f,
				       array.length-0.001f );
        VirtualArray2D va2D = new VirtualArray2D( array, err_array );
        va2D.setAxisInfo( AxisInfo.X_AXIS, qxmin, qxmax, 
    		            xlabel, xunit, AxisInfo.LINEAR );
        va2D.setAxisInfo( AxisInfo.Y_AXIS, qymin, qymax, 
    			    ylabel, yunit, AxisInfo.LINEAR );
        // since datamin/max are gotten from the image,
	// the min/max are dummy values.
	va2D.setAxisInfo( AxisInfo.Z_AXIS, 0, 1, 
    			    zlabel, zunit, AxisInfo.LINEAR );
        int separator_index = filename.lastIndexOf(
        	                    System.getProperty("file.separator") );
	va2D.setTitle(filename.substring(separator_index + 1));
	setData( va2D );
      }
      // no file to be read, display file not found on empty jpanel.
      else
      {
        VirtualArray2D nullarray = null;
        this.setData(nullarray);
        ((JComponent)pane.getLeftComponent()).add( 
	                                       new JLabel("File Not Found") );
        validate();
        repaint();
	return false;
      }
    }
    
    // close the text file reader
    try
    {
      // Make sure to close file input stream.
      if( reader != null )
        reader.close();
    }
    catch( IOException e )
    {
      SharedData.addmsg("SANDWedgeViewer unable to close file in loadData()");
    }
    return true;
  }
  
 /*
  * Helper method for the loadData() method. This method will remove all
  * leading spaces from the String passed in.
  */
  private String removeLeadingSpaces( String string )
  {
    while( string.indexOf(" ") == 0 )
      string = string.substring(1);
    return string;
  }
  
 /**
  * This method takes in a virtual array and updates the image. If the array
  * is the same size as the previous data array, the image is just redrawn.
  * If the size is different, the frame is disposed and a new view component
  * is constructed.
  *
  *  @param  values
  */ 
  public void setData( IVirtualArray2D values )
  {
    // make sure an ivc exists, the values passed in are not null, and the
    // previous data was not null.
    if( ivc != null && values != null && data != null )
    {
      data = values;
      ivc.kill();  // since data is changing, kill all windows created by ivc.
      ivc.dataChanged(data);  // if ivc exists, update the image.
    }  
    // if data == null or values = null, remove everything and build again.
    // if ivc == null, build for the first time.
    else
    { 
      data = values;
      getContentPane().removeAll();
      buildMenubar();
      buildPane();
      getContentPane().add(pane);
      validate();
      repaint();
    }
  }
  
 /**
  * This method takes in a 2D array and updates the image. If the array
  * is the same size as the previous data array, the image is just redrawn.
  * If the size is different, the frame is disposed and a new view component
  * is constructed.
  *
  *  @param  array
  */ 
  public void setData( float[][] array, float[][] err_array )
  {
    setData( new VirtualArray2D(array, err_array) );
  }
 
 /**
  * This method sets the directory where data files can be found.
  *
  *  @param  path Path to directory where data is located.
  */ 
  public void setDataDirectory( String path )
  {
    projectsDirectory = path;
  }
  
 /**
  * This method overrides the dispose() method of the JFrame. This method will
  * dispose of all subcomponents' windows before calling dispose() method of
  * the JFrame.
  */
  public void dispose()
  {
    // Close the windows of all subcomponents
    if( oldview != null )
      oldview.dispose();
    if( helper != null )
      helper.dispose();
    if( ivc != null )
      ivc.kill();
    // Do the normal dispose process on this viewer.
    super.dispose();
  }
  
 /*
  * The init function gathers the common functionality between the constructors
  * so that the code does not have to exist in 3 spots. This will build the
  * niceties of the viewer.
  */ 
  private void init( IVirtualArray2D iva )
  {
    this_viewer = this;
    setTitle("SAND Wedge Viewer");
    ellipselabels = new String[]{"X Center", "Y Center",
                                 "X Radius", "Y Radius"};
    wedgelabels = new String[]{"X Center", "Y Center", "Radius",
                   "Wedge Axis Angle", "Interior Angle"};
    ringlabels = new String[]{"X Center", "Y Center",
                             "Inner Radius", "Outer Radius"};
    if( iva != null )
    {
      AxisInfo xinfo = iva.getAxisInfo( AxisInfo.X_AXIS );
      AxisInfo yinfo = iva.getAxisInfo( AxisInfo.Y_AXIS );
      // y min/max are swapped since IVC swaps them.
      image_to_world_tran.setDestination( new CoordBounds( xinfo.getMin(),
						           yinfo.getMax(),      
						           xinfo.getMax(),
						           yinfo.getMin() ) );
      image_to_world_tran.setSource( 0.001f, 0.001f,
                                     iva.getNumColumns()-0.001f,
				     iva.getNumRows()-0.001f );
    }
    data = null;
    datafile = "";
    buildMenubar();
    
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setBounds(0,0,700,525);
    data_set   = new DataSet("Intensity vs Q in Region", 
                             "Calculated Intensity vs Q in Region");

    data_set.setX_units("Inverse Angstroms" );
    data_set.setX_label("Q" );

    data_set.setY_units("" );
    data_set.setY_label("Relative Intensity" );
    if( data_set.getNum_entries() > 0 )
      data_set.setSelectFlag( data_set.getNum_entries() - 1, true );
    setData(iva);
    sandcontrolpane = new SANDControlPanel();
    
    // if SandProps.isv exists, load only the project directory since the
    // SandProps.isv is not loaded until after this information is needed.
    // This code will restore the user's data directory.
    String props = System.getProperty("user.home") + 
    		   System.getProperty("file.separator") +
    		   "SandProps.isv";
    ObjectState project_state = new ObjectState();
    project_state.silentFileChooser(props,false);
    Object temp = null;
    temp = project_state.get(DATA_DIRECTORY);    
    if( temp != null )
    {
      projectsDirectory = (String)temp;
    }
  }

 /*
  * This method builds the content pane of the frame.
  */
  private void buildPane()
  { 
    if( data != null )
    {
      ivc = new ImageViewComponent( data );
      // since box, point, and line selections don't apply, disable them.
      String[] disSelect = { SelectionJPanel.BOX,
                             SelectionJPanel.POINT,
			     SelectionJPanel.LINE };
      ivc.disableSelection( disSelect );
      ivc.setColorControlEast(true);
      ivc.preserveAspectRatio(true);
      ivc.addActionListener( new ImageListener() );    
      Box componentholder = new Box(BoxLayout.Y_AXIS);
      componentholder.add( ivc.getDisplayPanel() );
      pane = new SplitPaneWithState(JSplitPane.HORIZONTAL_SPLIT,
    	  			    componentholder,
        			    buildControls(), .75f );
      // get menu items from view component and place it in a menu
      ViewMenuItem[] menus = ivc.getMenuItems();
      for( int i = 0; i < menus.length; i++ )
      {
        if( ViewMenuItem.PUT_IN_FILE.equalsIgnoreCase(
    	    menus[i].getPath()) )
    	{
	  menu_bar.getMenu(0).add( menus[i].getItem() ); 
        }
	else if( ViewMenuItem.PUT_IN_OPTIONS.equalsIgnoreCase(
    	         menus[i].getPath()) )
    	{
	  menu_bar.getMenu(1).add( menus[i].getItem() );	   
        }
	else if( ViewMenuItem.PUT_IN_HELP.equalsIgnoreCase(
    	         menus[i].getPath()) )
        {
	  menu_bar.getMenu(2).add( menus[i].getItem() );
        }
      }
      // if SandProps.isv exists, load it into the ObjectState automatically.
      // This code will save user settings.
      String props = System.getProperty("user.home") + 
        	     System.getProperty("file.separator") +
        	     "SandProps.isv";
      ObjectState temp = getObjectState(IPreserveState.DEFAULT);
      temp.silentFileChooser(props,false);
      setObjectState(temp);
      // enable Print Image and Make Image menu items
      menu_bar.getMenu(0).getItem(3).setEnabled(true); // print image
      menu_bar.getMenu(0).getItem(4).setEnabled(true); // make image
    }
    // no data, build an empty split pane.
    else
    {
      pane = new SplitPaneWithState(JSplitPane.HORIZONTAL_SPLIT,
                                    new JPanel(), new JPanel(), .75f );
      // disable Print Image and Make Image menu items
      menu_bar.getMenu(0).getItem(3).setEnabled(false); // print image
      menu_bar.getMenu(0).getItem(4).setEnabled(false); // make image
    }   
  }
 
 /*
  * This private method will (re)build the menubar. This is necessary since
  * the ImageViewComponent could add menu items to the Menubar.
  * If the file being loaded is not found, those menu items
  * must be removed. To do so, rebuild the Menubar.
  */ 
  private void buildMenubar()
  {    
    setJMenuBar(null);
    menu_bar = new JMenuBar();
    setJMenuBar(menu_bar);
    
    Vector file              = new Vector();
    Vector options           = new Vector();
    Vector help              = new Vector();
    Vector save_results_menu = new Vector();
    Vector view_man  	     = new Vector();
    Vector save_menu 	     = new Vector();
    Vector load_menu 	     = new Vector();
    Vector save_default      = new Vector();
    Vector load_data         = new Vector();
    Vector swv_help          = new Vector();
    Vector print             = new Vector();
    Vector save_image        = new Vector();
    Vector exit              = new Vector();
    Vector file_listeners    = new Vector();
    Vector option_listeners  = new Vector();
    Vector help_listeners    = new Vector();
    
    // build file menu
    file.add("File");
    file_listeners.add( new WVListener() ); // listener for file
    file.add(load_data);
      load_data.add("Load S(Qx,Qy)");
      file_listeners.add( new WVListener() ); // listener for load data
    file.add(load_menu);
      load_menu.add("Open Project");
      file_listeners.add( new WVListener() ); // listener for load project
    file.add(save_menu);
      save_menu.add("Save Project");
      file_listeners.add( new WVListener() ); // listener for save project
    file.add(print);
      print.add("Print Image");
      file_listeners.add( new WVListener() ); // listener for printing IVC
    file.add(save_image);
      save_image.add("Make Image (JPEG)");
      file_listeners.add( new WVListener() ); // listener for saving IVC as jpg
    file.add(exit);
      exit.add("Exit");
      file_listeners.add( new WVListener() ); // listener for exiting SWViewer
    
    // build options menu
    options.add("Options");
    option_listeners.add( new WVListener() ); // listener for options
    options.add(view_man);
      view_man.add("Hide Results Window");
      option_listeners.add( new WVListener() ); // listener for view results
    options.add(save_results_menu);
      save_results_menu.add("Save Results to File");
      option_listeners.add( new WVListener() ); // listener for saving results
    options.add(save_default);
      save_default.add("Save User Settings");
      option_listeners.add( new WVListener() ); // listener for saving results
    
    // build help menu
    help.add("Help");
    help_listeners.add( new WVListener() );
    help.add( swv_help );
      swv_help.add("SAND Wedge Viewer");
      help_listeners.add( new WVListener() );  // listener for SAND helper
    
    // add menus to the menu bar.
    menu_bar.add( MenuItemMaker.makeMenuItem(file,file_listeners) ); 
    menu_bar.add( MenuItemMaker.makeMenuItem(options,option_listeners) );
    menu_bar.add( MenuItemMaker.makeMenuItem(help,help_listeners) );
    
    // since the IVC is not created unless data is available,
    // do not load state unless data is available.
    if( data == null )
    {
      JMenu file_menu = menu_bar.getMenu(0);
      file_menu.getItem(2).setEnabled(false);   // disable Save Project Settings
      file_menu.getItem(3).setEnabled(false);   // disable Print Image
      file_menu.getItem(4).setEnabled(false);   // disable Make Image
      JMenu option_menu = menu_bar.getMenu(1);
      option_menu.getItem(0).setEnabled(false); // disable Hide Results Window
      option_menu.getItem(1).setEnabled(false); // disable Save Results
      option_menu.getItem(2).setEnabled(false); // disable Save User Settings
    }
    
    // if ViewManager not visible, disable "Hide Results Window" button
    if( oldview == null || !oldview.isVisible() )
    {
      menu_bar.getMenu(1).getItem(0).setEnabled(false);
    }
  }
  
 /*
  * build controls for image view component.
  */ 
  private Box buildControls()
  {
    // box that contains all of the controls.
    Box controls = new Box(BoxLayout.Y_AXIS);

    // add imageviewcomponent controls
    Box ivc_controls = new Box(BoxLayout.Y_AXIS);
    TitledBorder ivc_border = 
    		     new TitledBorder(LineBorder.createBlackLineBorder(),
        			      "Image Controls");
    ivc_border.setTitleFont( FontUtil.BORDER_FONT ); 
    ivc_controls.setBorder( ivc_border );
    ViewControl[] ivc_ctrl = ivc.getControls();
    for( int i = 0; i < ivc_ctrl.length; i++ )
    {
      // don't add cursor read out.
      if( ivc_ctrl[i].getTitle().equals(
             ImageViewComponent.CURSOR_READOUT_NAME) || 
	  ivc_ctrl[i].getTitle().equals(
	           ImageViewComponent.INTENSITY_SLIDER_NAME) )
        controls.add(ivc_ctrl[i]);
      else
        ivc_controls.add(ivc_ctrl[i]);
    }
    // if resized, adjust container size for the pan view control.
    //ivc_controls.addComponentListener( new ResizedControlListener() );
    JTabbedPane sand_and_image_controls = new JTabbedPane();
    sand_and_image_controls.add( "SAND", sandcontrolpane );
    if( ivc_ctrl.length != 0 )
    {
      sand_and_image_controls.add("Image", ivc_controls);
    }
    controls.add(sand_and_image_controls);
    ivc_controls.addComponentListener( new ResizedControlListener() );
    
    // add spacer between ivc controls
    JPanel spacer = new JPanel();
    spacer.setPreferredSize( new Dimension(0, 10000) );
    controls.add(spacer);
    return controls;
  }

 /*
  *                 **************Integrate*******************
  * This method does the calculations that produce the graph.
  */
  private void integrate( Region region, int ID )
  {
    // Set the units of the dataset
    if( integrate_by_ring )
    {
      data_set.setX_units("Inverse Angstroms" );
      data_set.setX_label("Q" );
    }
    else
    {
      data_set.setTitle("Intensity vs Degrees in Region");
      data_set.setX_units("Degrees" );
      data_set.setX_label("Angle" );
    }
    
    float start_x = 0;
    float end_x   = 0;
    // theta is used only if integrating by angle
    float theta   = 0;
    float pix_avg_radius = 0; // This value is the average radius of the region.
    floatPoint2D center = new floatPoint2D(); 
    floatPoint2D  start_point = null,
                  end_point   = null;
    int n_xvals;
    String attribute_name = "";
    float[] attributes;
    // Attributes for wedge/double wedge.
    // 0. Type of selection (Static ints at top of this file)
    // 1. X axis Center position, in world coordinates
    // 2. Y axis Center position, in world coordinates
    // 3. Radius of wedge, in world coordinates
    // 4. Angle of the wedge axis, in degrees.
    // 5. Width of wedge, in degrees.
    // *** for Ellipse, Att. 3 is x radius, Att. 4 is y radius.***
    if( region == null )
    {
      return;
    }
    floatPoint2D[] def_pts = region.getDefiningPoints(Region.IMAGE);
    if( region instanceof WedgeRegion )
    {
     /* def_pts[0]   = center pt of circle that arc is taken from
      * def_pts[1]   = last mouse point/point at intersection of line and arc
      * def_pts[2]   = reflection of p[1]
      * def_pts[3]   = top left corner of bounding box around arc's total circle
      * def_pts[4]   = bottom right corner of bounding box around arc's circle
      * def_pts[5].x = startangle, the directional vector in degrees
      * def_pts[5].y = degrees covered by arc.
      */
      attributes = new float[5];
      center = new floatPoint2D( def_pts[0] );
      // build attributes list
      attribute_name = SelectionJPanel.WEDGE;
      float axisangle = def_pts[5].x + def_pts[5].y/2f;
      if( axisangle >= 360 )
        axisangle -= 360;
      // tranform from image to world coords
      floatPoint2D wc_center = image_to_world_tran.MapTo(center);
      float radius = image_to_world_tran.MapXTo(def_pts[4].x) - wc_center.x;
      // keep radius positive.
      radius = Math.abs(radius);
      // round number to 5 digits.
      attributes[0] = (float)Format.round((double)wc_center.x, 5);
      attributes[1] = (float)Format.round((double)wc_center.y, 5);
      attributes[2] = (float)Format.round((double)radius, 5);
      attributes[3] = axisangle;
      attributes[4] = def_pts[5].y;
      // Depending on the method of integration, start_x and end_x will change.
      if( integrate_by_ring )
        end_x = def_pts[4].x - center.x;
      else
      {
        start_x = def_pts[5].x;
	end_x = start_x + def_pts[5].y;
	theta = def_pts[5].y;
	// The average radius in pixel coords.
	pix_avg_radius = Math.abs((def_pts[4].x - center.x)/2f);
      }
      end_point = new floatPoint2D( def_pts[1].x, def_pts[1].y );
    }
    else if( region instanceof DoubleWedgeRegion )
    {
     /* def_pts[0]   = center pt of circle that arc is taken from
      * def_pts[1]   = last mouse point/point at intersection of line and arc
      * def_pts[2]   = reflection of p[1]
      * def_pts[3]   = top left corner of bounding box around arc's total circle
      * def_pts[4]   = bottom right corner of bounding box around arc's circle
      * def_pts[5].x = startangle, the directional vector in degrees
      * def_pts[5].y = degrees covered by arc.
      */
      attributes = new float[5];
      center = new floatPoint2D( def_pts[0] );
      // build attributes list
      attribute_name = SelectionJPanel.DOUBLE_WEDGE;
      float axisangle = def_pts[5].x + def_pts[5].y/2f;
      if( axisangle >= 360 )
        axisangle -= 360;
      // tranform from image to world coords
      floatPoint2D wc_center = image_to_world_tran.MapTo(center);
      float radius = image_to_world_tran.MapXTo(def_pts[4].x) - wc_center.x;
      // keep radius positive.
      radius = Math.abs(radius);
      // round number to 5 digits.
      attributes[0] = (float)Format.round((double)wc_center.x, 5);
      attributes[1] = (float)Format.round((double)wc_center.y, 5);
      attributes[2] = (float)Format.round((double)radius, 5);
      attributes[3] = axisangle;
      attributes[4] = def_pts[5].y;
      // Depending on the method of integration, start_x and end_x will change.
      if( integrate_by_ring )
        end_x = def_pts[4].x - center.x;
      else
      {
        start_x = def_pts[5].x;
	end_x = start_x + def_pts[5].y;
	theta = def_pts[5].y;
	// The average radius in pixel coords.
	pix_avg_radius = Math.abs((def_pts[4].x - center.x)/2f);
      }
      end_point = new floatPoint2D( def_pts[1].x, def_pts[1].y );
    }
    else if( region instanceof EllipseRegion )
    {
     /* def_pts[0]   = top left corner of bounding box around ellipse
      * def_pts[1]   = bottom right corner of bounding box around ellipse
      * def_pts[2]   = center pt of ellipse
      */
      attributes = new float[4];
      center = new floatPoint2D( def_pts[2] );
      // build attributes list
      attribute_name = SelectionJPanel.ELLIPSE;
      // tranform from image to world coords
      floatPoint2D wc_center = image_to_world_tran.MapTo(center);
      float major_radius = image_to_world_tran.MapXTo(def_pts[1].x) - 
                           wc_center.x;
      float minor_radius = image_to_world_tran.MapYTo(def_pts[1].y) - 
                           wc_center.y;
      // keep radii positive.
      major_radius = Math.abs(major_radius);
      minor_radius = Math.abs(minor_radius);
      // round number to 5 digits.
      attributes[0] = (float)Format.round((double)wc_center.x, 5);
      attributes[1] = (float)Format.round((double)wc_center.y, 5);
      attributes[2] = (float)Format.round((double)major_radius, 5);
      attributes[3] = (float)Format.round((double)minor_radius, 5);
      // Depending on the method of integration, start_x and end_x will change.
      if( integrate_by_ring )
        end_x = def_pts[1].x - center.x;
      else
      {
        // Cover entire circle
        start_x = 0;
	end_x = 360;
	theta = 360;
	// The average radius in pixel coords.
	pix_avg_radius = Math.abs((def_pts[1].x - center.x)/2f);
      }
      float image_radius = def_pts[1].x - center.x;
                                      // The following end_point calculation
                                      // may need to be changed ###############
      end_point = new floatPoint2D( center.x + image_radius, center.y );
    }
    else if( region instanceof AnnularRegion )
    {
     /*
      * def_pts[0]   = center pt of circle that arc is taken from
      * def_pts[1]   = top left corner of bounding box of inner circle
      * def_pts[2]   = bottom right corner of bounding box of inner circle
      * def_pts[3]   = top left corner of bounding box of outer circle
      * def_pts[4]   = bottom right corner of bounding box of outer circle
      */
      attributes = new float[4];
      center = new floatPoint2D( def_pts[0] );
      // build attributes list
      attribute_name = SelectionJPanel.RING;
      // tranform from image to world coords
      floatPoint2D wc_center = image_to_world_tran.MapTo(center);
      float inner_radius = image_to_world_tran.MapXTo(def_pts[2].x) -
                           wc_center.x;
      float outer_radius = image_to_world_tran.MapYTo(def_pts[4].x) -
                           wc_center.x;
      // keep radii positive.
      inner_radius = Math.abs(inner_radius);
      outer_radius = Math.abs(outer_radius);
      // round number to 5 digits.
      attributes[0] = (float)Format.round((double)wc_center.x, 5);
      attributes[1] = (float)Format.round((double)wc_center.y, 5);
      attributes[2] = (float)Format.round((double)inner_radius, 5);
      attributes[3] = (float)Format.round((double)outer_radius, 5);
      // Depending on the method of integration, start_x and end_x will change.
      if( integrate_by_ring )
        end_x = def_pts[4].x - center.x;
      else
      {
        // Cover entire circle
        start_x = 0;
	end_x = 360;
	theta = 360;
	// The average radius in pixel coords.
	pix_avg_radius = Math.abs( ( (def_pts[4].x - center.x) + 
	                             (def_pts[2].x - center.x) )/2f);
      }
      float image_radius = def_pts[4].x - center.x;
                                      // The following end_point calculation
                                      // needs to be changed ###############
      end_point = new floatPoint2D( center.x + image_radius, center.y );
    }
    // should never get to this else, we have an invalid Region.
    else
    {
      return;
    }

    UniformXScale x_scale;      // "time channels" for the spectrum
    if( integrate_by_ring )
    {
      // build list of Q bin centers, with one bin for each pixel in the radius
      n_xvals = Math.round(end_x);
    }
    else
    {
      // This uses eqn:
      // max steps = (theta in radians) * (average pixel radius) * 2/3
      n_xvals = Math.round( (float)(theta*Math.PI/180)*pix_avg_radius*2f/3f );
    }
    x_scale = new UniformXScale( start_x, end_x, n_xvals );

    int hit_count[]  = new int[n_xvals];
    float y_vals[]   = new float[n_xvals];
    float err_vals[] = new float[n_xvals];
    float err;
    float x_vals[] = x_scale.getXs();
    Point[] selected_pts = region.getSelectedPoints();
    // this loop will sum up values at the same distance and count the number
    // of hits at each distance.
    // NOTE: The distance calculation is done in image row/col values, not
    // in world coord values.
    // Map world coord origin to image origin for magnitude calculation
    floatPoint2D image_origin = 
                        image_to_world_tran.MapFrom( new floatPoint2D(0,0) );
    float x = 0;
    float y = 0;
    float dist = 0;
    int index = 0;
    // get arrays now since the "for" loop below will make several calls
    // to the array. It is a lot faster to access the data through 
    // the array than through the virtual array.
    float[][] data_array = data.getRegionValues( 0,data.getNumRows()-1,
                                                 0,data.getNumColumns()-1 );
    float[][] err_array = data.getErrors();
    
    // Bins are radial distances, summing over a ring.
    if( integrate_by_ring )
    {
      for ( int i = 0; i < selected_pts.length; i++ )
      {
    	x = Math.abs( selected_pts[i].x - image_origin.x );
    	y = Math.abs( selected_pts[i].y - image_origin.y );
    	dist = (float)Math.sqrt( x*x + y*y );
    	index = binarySearch( x_vals, dist );
    	y_vals[index] += data_array[ selected_pts[i].y ][ selected_pts[i].x ];
    	err = err_array[ selected_pts[i].y ][ selected_pts[i].x ];
    	err_vals[index] += err*err; 
    	hit_count[index]++;
      }
    }
    // Bins are angular distance, summing over an angle.
    else
    {
      // This variable is used to place the angle in the correct quadrant.
      float angle_modifier = 0;
      // System.out.println("X scale: " + x_scale );
      for ( int i = 0; i < selected_pts.length; i++ )
      {
    	x = selected_pts[i].x - image_origin.x;
    	y = selected_pts[i].y - image_origin.y;
	if( x < 0 )
	{
	  // quadrant III
	  if( y < 0 )
	    angle_modifier = 180f;
	  // quadrant II
    	  else
	    angle_modifier = -180f;
	}
	else
	{
	  // quadrant IV
	  if( y < 0 )
	    angle_modifier = -360f;
	  // quadrant I
    	  else
	    angle_modifier = 0;
	}
	// dist is the angle, this will always be a positive angle
	dist = (float)Math.atan( (double)Math.abs(y/x) );
	// convert from radians to degrees
	dist = dist * (float)(180d/Math.PI);
	// Add the modifier
	dist = Math.abs( dist + angle_modifier );
	// Make sure value is between the min and max theta
	if( dist < start_x )
	  dist += 360;
	if( dist > end_x )
	  dist -= 360;
    	index = binarySearch( x_vals, dist );
    	y_vals[index] += data_array[ selected_pts[i].y ][ selected_pts[i].x ];
    	err = err_array[ selected_pts[i].y ][ selected_pts[i].x ];
    	err_vals[index] += err*err; 
    	hit_count[index]++;
      }
    }
    
    // find average value per hit.
    for( int bindex = 0; bindex < n_xvals; bindex++ )
    {
      if( hit_count[bindex] != 0 )
      {
        y_vals[bindex] = y_vals[bindex]/(float)hit_count[bindex];
        err_vals[bindex] = (float)Math.sqrt(err_vals[bindex]) /
                                  (float)hit_count[bindex];
      }
    }

    // Bins are radial distances, summing over a ring.
    if( integrate_by_ring )
    {
      // Convert the spectrum into a spectrum relative to "Q".  Also, discard
      // the first bin, at the central vertex of the wedge, since the counts
      // there are usually 0.  ( 0 causes problems with the log-log display.)
      //
      // NOTE: The calculation in "pixel space" added the intensity to y_vals[k]
      //       provided the point's distance from the center was closest to
      //       x_vals[k], so the x_vals[] are actually bin centers.
     
      
      start_point = new floatPoint2D( center.x, center.y );
      start_point = image_to_world_tran.MapTo( start_point ); 
      end_point   = image_to_world_tran.MapTo( end_point ); 

      float new_start_x = start_point.magnitude(); 
      float new_end_x	= end_point.magnitude(); 
      // increment to avoid first bin.
      if ( n_xvals > 1 )
      {
     	float step = (new_end_x - new_start_x) / (n_xvals-1);
     	new_start_x += step;
      }

      //System.out.println("Using Q between " + new_start_x + 
      //			  " and " + new_end_x );
      // first bin gone, so n_xvals-1 values
      UniformXScale new_x_scale = new UniformXScale( new_start_x, 
     						     new_end_x, 
     						     n_xvals-1 );
      float new_y_vals[]   = new float[ y_vals.length - 1 ];
      float new_err_vals[] = new float[ y_vals.length - 1 ];
      for ( int i = 0; i < new_y_vals.length; i++ )
      {
     	new_y_vals[i]	= y_vals[i+1];
     	new_err_vals[i] = err_vals[i+1];
      }
    
      // put it into a "Data" object and then add it to the dataset
      Data new_spectrum = new FunctionTable( new_x_scale, 
     					     new_y_vals, 
     					     new_err_vals,  
     					     ID );
      new_spectrum.setAttribute( new Float1DAttribute( attribute_name, 
     				 attributes ) );

      data_set.addData_entry( new_spectrum ); 
      data_set.setSelectFlag( data_set.getNum_entries() - 1, true );
    }
    else
    {
      // put it into a "Data" object and then add it to the dataset
      Data new_spectrum = new FunctionTable( x_scale, 
        				     y_vals, 
        				     err_vals,  
        				     ID );
      new_spectrum.setAttribute( new Float1DAttribute( attribute_name, 
        			 attributes ) );

      data_set.addData_entry( new_spectrum ); 
      data_set.setSelectFlag( data_set.getNum_entries() - 1, true );
    }
  }
 
 /*
  * This method efficiently finds what bin to put data in.
  */ 
  private int binarySearch(  float[] x_values, float dist )
  { 
    int bin_low = 0;
    int bin_high = x_values.length - 1;
    int bin = Math.round( (float)(bin_high-bin_low)/2f );
    // half of the step from one x_value to the next.
    float half_increment = (x_values[1] - x_values[0])/2f;
    // if dist is within half a step of the x_value, or if the bin is an
    // extreme, return that bin index.
    while( !( dist <= (x_values[bin] + half_increment) &&
              dist > (x_values[bin] - half_increment) ) &&
	    !( bin == bin_low || bin == bin_high ) )
    {
      //System.out.println("Dist/X_val/bin: " + dist + "/" + x_values[bin] +
      //                   "/" + bin );
      if( dist < (x_values[bin] - half_increment) )
      {
        bin_high = bin;
      }
      else
      {
        bin_low = bin;
      }
      // move bin to midpoint of bin_low and bin_high
      bin = bin_low + Math.round((float)(bin_high-bin_low)/2f);
    }
    return bin;
  }
  
 /*
  * This class is required to handle all messages within the SANDWedgeViewer.
  */
  private class WVListener implements ActionListener
  {
    public void actionPerformed( ActionEvent ae )
    {
      if( ae.getActionCommand().equals("Load S(Qx,Qy)") )
      {
        JFileChooser fc = new JFileChooser(projectsDirectory);
        fc.setFileFilter( new DataFileFilter() );
        int result = fc.showDialog(new JFrame(),"Load S(Qx,Qy)");
     
        if( result == JFileChooser.APPROVE_OPTION )
        {
          String filename = fc.getSelectedFile().toString();
	  projectsDirectory = fc.getCurrentDirectory().toString();
          loadData(filename);
	  // Remove all currently selected regions.
	  if( data_set.getNum_entries() > 0 )
	  {
	    ivc.setSelectedRegions(null);
	  }
        }
      } // end else if load data
      else if( ae.getActionCommand().equals("Save Results to File") )
      {
        JFileChooser fc = new JFileChooser(projectsDirectory);
        fc.setFileFilter( new DataFileFilter() );
        int result = fc.showDialog(new JFrame(),"Save Results");
     
        if( result == JFileChooser.APPROVE_OPTION )
        {
          String filename = fc.getSelectedFile().toString();
	  filename = new DataFileFilter().appendExtension(filename);
	  projectsDirectory = fc.getCurrentDirectory().toString();
	  String[] descriptors;  // labels displayed in the SANDControlPanel.
          Data tempdata;
          Float1DAttribute fat;
	  // This portion will put the data attributes at the top of the
	  // file, each line preceeded with a pound (#) symbol.
	  StringBuffer header = new StringBuffer("# File: ");
	  header.append(datafile).append('\n');
	  SANDFileWriter.FileInfo[] info = 
	                new SANDFileWriter.FileInfo[data_set.getNum_entries()];
	  for( int entry = 0; entry < data_set.getNum_entries(); entry++ )
	  {
	    tempdata = data_set.getData_entry(entry);
	    // figure out which type of region was selected.
	    if( tempdata.getAttribute(SelectionJPanel.WEDGE) != null )
	    {
	      fat = (Float1DAttribute)
            		 tempdata.getAttribute(SelectionJPanel.WEDGE);
	      descriptors = wedgelabels;
	    }
	    else if( tempdata.getAttribute(SelectionJPanel.DOUBLE_WEDGE) != null )
	    {
	      fat = (Float1DAttribute)
            		 tempdata.getAttribute(SelectionJPanel.DOUBLE_WEDGE);
	      descriptors = wedgelabels;
	    }
	    else if( tempdata.getAttribute(SelectionJPanel.ELLIPSE) != null )
	    {
	      fat = (Float1DAttribute)
            		 tempdata.getAttribute(SelectionJPanel.ELLIPSE);
	      descriptors = ellipselabels;
	    }
	    else  // ring selection
	    {
	      fat = (Float1DAttribute)
            		 tempdata.getAttribute(SelectionJPanel.RING);
	      descriptors = ringlabels;
	    }
	    
	    float[] vals = fat.getFloatValue();
	    String at_name = fat.getName();
	    header.append("# Selection Type: ").append(at_name).append('\n');
	    // make sure there are the same number of values as descriptors.
	    int length = vals.length;
	    if( length > descriptors.length )
	      length = descriptors.length;
	    for( int i = 0; i < length; i++ )
	    {
	      header.append("# ").append(descriptors[i]);
	      header.append(": ").append(vals[i]).append('\n');
	    }
	    // Add the selection to a list, so all can be written out
	    // to one file.
            info[entry] = new SANDFileWriter.FileInfo( header.toString(),
	                       tempdata.getX_scale().getXs(),
                               tempdata.getY_values(), tempdata.getErrors() );
	    // clear the header String after it is saved.
	    header = header.delete(0,header.length());
	  }
	  SANDFileWriter.makeFile(filename,info);
        }
      } // end else if save results
      else if( ae.getActionCommand().equals("Hide Results Window") )
      {
        oldview.setVisible(false);
      }
      else if( ae.getActionCommand().equals("Show Results Window") )
      {
	if( !oldview.isVisible() )
	{
          WindowShower shower = new WindowShower(oldview);
          java.awt.EventQueue.invokeLater(shower);
          shower = null;
	}
	else
	  oldview.toFront();
      }
      // Save user preferences, this is a selective save.
      else if( ae.getActionCommand().equals("Save User Settings") )
      {
        String props = System.getProperty("user.home") + 
	                System.getProperty("file.separator") +
			"SandProps.isv";
	getObjectState(IPreserveState.DEFAULT).silentFileChooser(props,true);
      }
      else if( ae.getActionCommand().equals("Save Project") )
      {
	getObjectState(IPreserveState.PROJECT).openFileChooser(true);
      }
      else if( ae.getActionCommand().equals("Open Project") )
      {
        ObjectState state = new ObjectState();
	if( state.openFileChooser(false) )
	  setObjectState(state);
      }
      else if( ae.getActionCommand().equals("Print Image") )
      {
        // since the left component may change from data to data, only
	// get the component when printing has been asked for.
        JMenuItem silent_menu = PrintComponentActionListener.getActiveMenuItem(
	                                "not visible",
	                                pane.getLeftComponent() );
	silent_menu.doClick();
      }
      else if( ae.getActionCommand().equals("Make Image (JPEG)") )
      {
        // since the left component may change from data to data, only
	// get the component when saving has been asked for.
        JMenuItem silent_menu = SaveImageActionListener.getActiveMenuItem(
	                                "not visible",
	                                pane.getLeftComponent() );
	silent_menu.doClick();
      }
      else if( ae.getActionCommand().equals("SAND Wedge Viewer") )
      {
        help();
      }
      else if( ae.getActionCommand().equals("Exit") )
      {
        if( oldview != null )
	{
	  oldview.dispose();
	  oldview = null;
	}
	this_viewer.dispose();
	System.gc();
	//System.exit(0);
      }
    }
  }
  
 /*
  * This class listeners for all messages sent by the ImageViewComponent,
  * including selections made by the selection overlay.
  */ 
  private class ImageListener implements ActionListener
  {
    public void actionPerformed( ActionEvent ae )
    {
      String message = ae.getActionCommand();
      // get all of the selections, we only care about the last one.
      Region[] selectedregions = ivc.getSelectedRegions();
        
      if( message.equals(SelectionOverlay.REGION_ADDED) )
      {	
        // REGION_ADDED message coming from setObjectState(), need to look
	// at all of the selections, not just the last one.
        if( os_region_added )
	{
	  for( int i = 0; i < selectedregions.length; i++ )
            integrate( selectedregions[i], i );
	  os_region_added = false;
	}
        // region added graphically or manually, only need to get points
	// from the last selected region.
	else
          integrate( selectedregions[selectedregions.length-1],
	             selectedregions.length-1 );
	
	// if a viewmanager has not been created yet, make one.
	if( oldview == null )
	{
          oldview = new ViewManager( data_set, IViewManager.SELECTED_GRAPHS );
          oldview.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
	  oldview.addComponentListener( new VisibleListener() );
	  oldview.addWindowListener( new ClosingListener() );
	  oldview.setDataSet(data_set);
	}
	else
	{
	  // the update revises the tempDataSet, and the notify causes
	  // the graphs to be updated.
	  oldview.update(data_set, IObserver.DATA_CHANGED);
	  data_set.notifyIObservers(IObserver.SELECTION_CHANGED);
	  if( !oldview.isVisible() )
	  {
            WindowShower shower = new WindowShower(oldview);
            java.awt.EventQueue.invokeLater(shower);
            shower = null;
	  }
	  else
	    oldview.toFront();
	}
	sandcontrolpane.selectionChanged();
	sandcontrolpane.setIntegrateRadioEnabled(false);
      }
      else if( message.equals(SelectionOverlay.REGION_REMOVED) )
      {
        // set the selected flag of last entry to false
        data_set.setSelectFlag( data_set.getNum_entries() - 1, false );
        // since we will be removing the last entry, this serves as
	// both an index of the old last entry and a new number of entries.
        int num_entries = data_set.getNum_entries() - 1;
        data_set.removeData_entry( num_entries );
	// if no entries in dataset, do not make ViewManager viewable.
	if( num_entries == 0 )
	{
	  oldview.setVisible(false);
	  sandcontrolpane.setIntegrateRadioEnabled(true);
	}
	// the update revises the tempDataSet, and the notify causes
	// the graphs to be updated.
	oldview.update(data_set, IObserver.DATA_CHANGED);
	data_set.notifyIObservers( IObserver.SELECTION_CHANGED );
	sandcontrolpane.selectionChanged();
      }
      else if( message.equals(SelectionOverlay.ALL_REGIONS_REMOVED) )
      {
	// if no data, don't allow view manager to be viewed.
	oldview.setVisible(false);
	// unselect all data so they will not be displayed.
        data_set.clearSelections();
        data_set.removeAll_data_entries();
	// the update revises the tempDataSet, and the notify causes
	// the graphs to be updated.
	oldview.update(data_set, IObserver.DATA_CHANGED);
	data_set.notifyIObservers( IObserver.SELECTION_CHANGED );
	sandcontrolpane.selectionChanged();
	sandcontrolpane.setIntegrateRadioEnabled(true);
      }
      else if( message.equals(IViewComponent.POINTED_AT_CHANGED) )
      {
	data_set.notifyIObservers( IObserver.POINTED_AT_CHANGED );
	//System.out.println("Pointed At Changed " + 
	//                   ivc.getPointedAt().toString() );
      }
      // enable "Save Results" under File menu and
      // "Hide/Show Results Window" under Options menu if selections exist.
      if( data_set.getNum_entries() > 0 )
      {
        menu_bar.getMenu(1).getItem(0).setEnabled(true); // show projects window
        menu_bar.getMenu(1).getItem(1).setEnabled(true); // save results
      }
      // disable "Save Results" under File menu and
      // "Hide/Show Results Window" under Options menu if no selections exist.
      else
      {
        menu_bar.getMenu(1).getItem(0).setEnabled(false); // show proj. window
        menu_bar.getMenu(1).getItem(1).setEnabled(false); // save results
      }
    }
  }
  
 /*
  * If ViewManager is visible, make button read "Hide Results Window",
  * otherwise have it read "Show Results Window"
  */
  private class VisibleListener extends ComponentAdapter
  {
    public void componentHidden( ComponentEvent e )
    {
      menu_bar.getMenu(1).getItem(0).setText("Show Results Window");
      menu_bar.validate();
      // if ViewManager is hidden, SANDWedgeViewer should take focus.
      ivc.returnFocus();
    }  
    
    public void componentShown( ComponentEvent e )
    {
      // since menu item starts out disabled, make sure it is enabled.
      // This will change the text, since ViewManager is displayed.
      if( !menu_bar.getMenu(1).getItem(0).isEnabled() )
        menu_bar.getMenu(1).getItem(0).setEnabled(true);
      menu_bar.getMenu(1).getItem(0).setText("Hide Results Window");
    }
  } 
 
 /*
  * This class is needed if to check of the user hides the ViewManager by
  * the close button on the frame. This was needed to extend the functionality
  * of the VisibleListener class.
  */ 
  private class ClosingListener extends WindowAdapter
  {
    public void windowClosing( WindowEvent we )
    {
      menu_bar.getMenu(1).getItem(0).setText("Show Results Window");
      menu_bar.validate();
      // if ViewManager is closing, SANDWedgeViewer should take focus.
      ivc.returnFocus();
    }
  }
  
 /*
  * This class is needed to reajust the size of the PanViewControl. Since
  * the PanViewControl needs to resize itself once the width is known,
  * the initial bounding box restricts the size of the control.
  */
  private class ResizedControlListener extends ComponentAdapter
  {
    public void componentResized( ComponentEvent e )
    {
      JComponent control_box = (JComponent)e.getComponent();
      JComponent tabbed_pane = (JComponent)control_box.getParent();
      
      int height = 0;
      int width = control_box.getWidth();
      Component[] controls = control_box.getComponents();
      for( int ctrl = 0; ctrl < controls.length; ctrl++ )
      {
        height += ((JComponent)controls[ctrl]).getHeight();
      }
      height += 45; // this is to adjust for spaces in between components.
      // Adjust the size of the tabbed pane
      if( tabbed_pane.getHeight() < height )
      {
        tabbed_pane.setSize( new Dimension( width, height ) );
        tabbed_pane.validate();
      }
    }  
  }
 
 /*
  * File filter for .dat files being loaded for data analysis.
  */ 
  private class DataFileFilter extends RobustFileFilter
  {
   /*
    *  Default constructor.  Calls the super constructor,
    *  sets the description, and sets the file extensions.
    */
    public DataFileFilter()
    {
      super();
      super.setDescription("Data File (*.dat)");
      super.addExtension(".dat");
    } 
  }

 /*
  * This class constructs the controls for the SANDWedgeViewer.
  */
  private class SANDControlPanel extends Box
  {
    private FieldEntryControl radiofec = new FieldEntryControl(5);
    private JComboBox selectlist;
    private JRadioButton ring_option;
    private JRadioButton angle_option;
    private SANDControlPanel this_editor;
    
    protected SANDControlPanel()
    {
      super( BoxLayout.Y_AXIS );
      this_editor = this;
      
      ButtonGroup integrate_group = new ButtonGroup();
      ring_option = new JRadioButton("Rings");
      ring_option.addActionListener( new IntegrateOption() );
      angle_option = new JRadioButton("Radial Lines");
      angle_option.addActionListener( new IntegrateOption() );
      integrate_group.add(ring_option);
      integrate_group.add(angle_option);
      integrate_group.setSelected(ring_option.getModel(),true);
      JPanel radios = new JPanel( new BorderLayout() );
      radios.add(ring_option, BorderLayout.NORTH);
      radios.add(angle_option, BorderLayout.SOUTH);
      JPanel integrate_control = new JPanel( new GridLayout(1,1) );
      integrate_control.add(radios);
      TitledBorder radio_border = 
    		     new TitledBorder(LineBorder.createBlackLineBorder(),
        			      "Sum Along:");
      radio_border.setTitleFont( FontUtil.BORDER_FONT ); 
      integrate_control.setBorder(radio_border);
      
      this_editor.add(integrate_control);
            
      radiofec.setLabelWidth(9);
      radiofec.setFieldWidth(6);
      radiofec.setValue(0,0);
      radiofec.setValue(1,0);
      radiofec.addRadioChoice(SelectionJPanel.ELLIPSE,ellipselabels);
      radiofec.addRadioChoice(SelectionJPanel.WEDGE,wedgelabels);
      radiofec.addRadioChoice(SelectionJPanel.DOUBLE_WEDGE,
                              SelectionJPanel.WEDGE); // use wedge labels
      radiofec.addRadioChoice(SelectionJPanel.RING,ringlabels);
      radiofec.setButtonText("Enter Values");
      radiofec.addActionListener( new EditorListener() );
      
      selectlist = new JComboBox();
      selectlist.addActionListener( new EditorListener() );
      
      buildComboBox();
      this_editor.add( selectlist );
      this_editor.add(radiofec);
    }
    
   /*
    * called when a selection is added/removed
    */ 
    protected void selectionChanged()
    {
      radiofec.clearAllValues();
      // Initialize the center to (0,0)
      radiofec.setValue(0,0);
      radiofec.setValue(1,0);
      buildComboBox();
      this_editor.validate();
      this_editor.repaint();
    }
    
   /*
    * This method is called to disable switching between integration methods
    * once an integration is made. This is done because the two integration
    * methods do not have the same x-axis units, thus they cannot appear
    * on the same graph.
    */ 
    protected void setIntegrateRadioEnabled( boolean enable )
    {
      if( enable )
      {
        ring_option.setEnabled(true);
	angle_option.setEnabled(true);
      }
      else
      {
        ring_option.setEnabled(false);
	angle_option.setEnabled(false);
      }
    }
   
   /*
    * This will rebuild the combobox each time a selection is added/removed
    */ 
    private void buildComboBox()
    {  
      if( ivc != null )
      {
        Region[] regions = ivc.getSelectedRegions();
        String[] sel_names = new String[regions.length + 1];
        String temp = "";
	String temp_name = "";
	selectlist.removeAllItems();
	for( int reg = 0; reg < regions.length; reg++ )
        {
	  if( regions[reg] instanceof EllipseRegion )
	    temp = SelectionJPanel.ELLIPSE;
	  else if( regions[reg] instanceof WedgeRegion )
	    temp = SelectionJPanel.WEDGE;
	  else if( regions[reg] instanceof DoubleWedgeRegion )
	    temp = SelectionJPanel.DOUBLE_WEDGE;
	  else if( regions[reg] instanceof AnnularRegion )
	    temp = SelectionJPanel.RING;
          
	  temp_name = (reg+1) + " - " + temp;
          selectlist.addItem(temp_name);
	}
	selectlist.addItem("New Selection");
	// set selected region to be the last region added.
	if( regions.length > 0 )
	  selectlist.setSelectedIndex( regions.length - 1 );
      }
      else
      {
	selectlist.addItem("New Selection");
      }
    }
  
    private class IntegrateOption implements ActionListener
    {
      public void actionPerformed( ActionEvent ae )
      {
  	String message = ae.getActionCommand();
  	if( message.equals("Rings") )
  	{
  	  integrate_by_ring = true;
  	}
  	else if( message.equals("Radial Lines") )
  	{
  	  integrate_by_ring = false;
  	}
	// give focus back to ImageViewComponent
	if( ivc != null )
	  ivc.returnFocus();
      }
    }
  
   /*
    * This class listeners for messages send by the editor.
    */ 
    private class EditorListener implements ActionListener
    {
      public void actionPerformed( ActionEvent ae )
      {
        String message = ae.getActionCommand();
        
        if( message.equals(FieldEntryControl.BUTTON_PRESSED) )
        {
	  float[] values = radiofec.getAllFloatValues();
	  // make sure no values are invalid
	  for( int i = 0; i < values.length; i++ )
	  {
	    if( Float.isNaN(values[i]) )
	      return;
	  }
     /* ***********************Defining Points for Ellipse**********************
      * def_pts[0]   = top left corner of bounding box around arc's total circle
      * def_pts[1]   = bottom right corner of bounding box around arc's circle
      * def_pts[2]   = center pt of circle that arc is taken from
      **************************************************************************
      */
	  if( radiofec.getSelected().equals(SelectionJPanel.ELLIPSE) )
	  {
	    // since ivc y values are swapped, y values for topleft and
	    // bottomright are also swapped.
	    floatPoint2D[] wc_pts = new floatPoint2D[3];
	    wc_pts[0] = new floatPoint2D( (values[0] - values[2]),
	                                  (values[1] + values[3]) );
	    wc_pts[1] = new floatPoint2D( (values[0] + values[2]),
	                                  (values[1] - values[3]) );
	    wc_pts[2] = new floatPoint2D( values[0], values[1] );
	    ivc.addSelectedRegion( new EllipseRegion( wc_pts ) );
	  }
	  
     /* ***********************Defining Points for Wedge************************
      * def_pts[0]   = center pt of circle that arc is taken from
      * def_pts[1]   = last mouse point/point at intersection of line and arc
      * def_pts[2]   = reflection of p[1]
      * def_pts[3]   = top left corner of bounding box around arc's total circle
      * def_pts[4]   = bottom right corner of bounding box around arc's circle
      * def_pts[5].x = startangle, the directional vector in degrees
      * def_pts[5].y = degrees covered by arc.
      **************************************************************************
      */
	  else if( radiofec.getSelected().equals(SelectionJPanel.WEDGE) )
	  {
	    // since ivc y values are swapped, y values for topleft and
	    // bottomright are also swapped.
	    floatPoint2D[] wc_pts = new floatPoint2D[6];
	    wc_pts[0] = new floatPoint2D( values[0], values[1] );
	    wc_pts[3] = new floatPoint2D( (values[0] - values[2]),
	                                  (values[1] + values[2]) );
	    wc_pts[4] = new floatPoint2D( (values[0] + values[2]),
	                                  (values[1] - values[2]) );
	    // use trig to find p1 (see WedgeCursor)
	    double theta_p1 = (double)(values[3] - values[4]/2);
	    // convert theta from degrees to radians
	    theta_p1 = theta_p1 * Math.PI / 180;
	    wc_pts[1] = new floatPoint2D( values[0] +
	                                  values[2]*(float)Math.cos(theta_p1),
					  values[1] +
	                                  values[2]*(float)Math.sin(theta_p1));
	    // use trig to find rp1 (see WedgeCursor)
	    double theta_rp1 = (double)(values[3] + values[4]/2);
	    // convert theta from degrees to radians
	    theta_rp1 = theta_rp1 * Math.PI / 180;
	    wc_pts[2] = new floatPoint2D( values[0] +
	                                  values[2]*(float)Math.cos(theta_rp1),
					  values[1] +
	                                  values[2]*(float)Math.sin(theta_rp1));
	    // define angles so WedgeRegion can use them.
	    float arcangle = values[4];
	    // make sure values are on interval [0,360)
	    while( arcangle < 0 )
	      arcangle = -arcangle;
	    while( arcangle >= 360 )
	      arcangle -= 360;
	    
	    float startangle = values[3] - arcangle/2;
	    while( startangle >= 360 )
	      startangle -= 360;
	    while( startangle < 0 )
	      startangle += 360;
	    wc_pts[5] = new floatPoint2D( startangle, arcangle );
	    ivc.addSelectedRegion( new WedgeRegion( wc_pts ) );
	  }
	  
     /* *******************Defining Points for Double Wedge*********************
      * def_pts[0]   = center pt of circle that arc is taken from
      * def_pts[1]   = last mouse point/point at intersection of line and arc
      * def_pts[2]   = reflection of p[1]
      * def_pts[3]   = top left corner of bounding box around arc's total circle
      * def_pts[4]   = bottom right corner of bounding box around arc's circle
      * def_pts[5].x = startangle, the directional vector in degrees
      * def_pts[5].y = degrees covered by arc.
      **************************************************************************
      */
	  else if( radiofec.getSelected().equals(SelectionJPanel.DOUBLE_WEDGE) )
	  {
	    // since ivc y values are swapped, y values for topleft and
	    // bottomright are also swapped.
	    floatPoint2D[] wc_pts = new floatPoint2D[6];
	    wc_pts[0] = new floatPoint2D( values[0], values[1] );
	    wc_pts[3] = new floatPoint2D( (values[0] - values[2]),
	                                  (values[1] + values[2]) );
	    wc_pts[4] = new floatPoint2D( (values[0] + values[2]),
	                                  (values[1] - values[2]) );
	    // use trig to find p1 (see WedgeCursor)
	    double theta_p1 = (double)(values[3] - values[4]/2);
	    // convert theta from degrees to radians
	    theta_p1 = theta_p1 * Math.PI / 180;
	    wc_pts[1] = new floatPoint2D( values[0] +
	                                  values[2]*(float)Math.cos(theta_p1),
					  values[1] +
	                                  values[2]*(float)Math.sin(theta_p1));
	    // use trig to find rp1 (see WedgeCursor)
	    double theta_rp1 = (double)(values[3] + values[4]/2);
	    // convert theta from degrees to radians
	    theta_rp1 = theta_rp1 * Math.PI / 180;
	    wc_pts[2] = new floatPoint2D( values[0] +
	                                  values[2]*(float)Math.cos(theta_rp1),
					  values[1] +
	                                  values[2]*(float)Math.sin(theta_rp1));
	    // define angles so WedgeRegion can use them.
	    float arcangle = values[4];
	    // make sure values are on interval [0,360)
	    while( arcangle < 0 )
	      arcangle = -arcangle;
	    while( arcangle > 180 )
	      arcangle = 360 - arcangle;
	    
	    float startangle = values[3] - arcangle/2;
	    while( startangle >= 360 )
	      startangle -= 360;
	    while( startangle < 0 )
	      startangle += 360;
	    wc_pts[5] = new floatPoint2D( startangle, arcangle );
	    ivc.addSelectedRegion( new DoubleWedgeRegion( wc_pts ) );
	  }
	  
     /* ***********************Defining Points for Ring*************************
      * def_pts[0]   = center pt of circle that arc is taken from
      * def_pts[1]   = top left corner of bounding box of inner circle
      * def_pts[2]   = bottom right corner of bounding box of inner circle
      * def_pts[3]   = top left corner of bounding box of outer circle
      * def_pts[4]   = bottom right corner of bounding box of outer circle
      **************************************************************************
      */
	  else if( radiofec.getSelected().equals(SelectionJPanel.RING) )
	  {
	    // if inner radius larger than outer radius, swap them
	    if( values[2] > values[3] )
	    {
	      float temp = values[2];
	      values[2] = values[3];
	      values[3] = temp;
	    }
	    // since ivc y values are swapped, y values for topleft and
	    // bottomright are also swapped.
	    floatPoint2D[] wc_pts = new floatPoint2D[5];
	    wc_pts[0] = new floatPoint2D( values[0], values[1] );
	    // inner topleft and bottomright
	    wc_pts[1] = new floatPoint2D( (values[0] - values[2]),
	                                  (values[1] + values[2]) );
	    wc_pts[2] = new floatPoint2D( (values[0] + values[2]),
	                                  (values[1] - values[2]) );
	    // outer topleft and bottomright
	    wc_pts[3] = new floatPoint2D( (values[0] - values[3]),
	                                  (values[1] + values[3]) );
	    wc_pts[4] = new floatPoint2D( (values[0] + values[3]),
	                                  (values[1] - values[3]) );
	    ivc.addSelectedRegion( new AnnularRegion( wc_pts ) );
	  }	  
        } // end if (BUTTON_PRESSED)
        else if( message.equals("Close") )
        {
	  this_editor.setVisible(false);
        }
	else if( message.equals("comboBoxChanged") )
	{
	  int index = selectlist.getSelectedIndex();
	  if( selectlist.getSelectedItem() != null &&
	      !((String)selectlist.getSelectedItem()).equals("New Selection") )
	  {
	    Data data = data_set.getData_entry(index);
	    
	    if( ((String)selectlist.getSelectedItem()).indexOf(
	                                        SelectionJPanel.ELLIPSE) >= 0 )
	    {
	      radiofec.setSelected(SelectionJPanel.ELLIPSE);
	      Float1DAttribute att =
	           (Float1DAttribute)data.getAttribute(SelectionJPanel.ELLIPSE);
	      float[] attlist = att.getFloatValue();
	      for( int i = 0; i < attlist.length; i++ )
	        radiofec.setValue( i,attlist[i] );
	    }
	    else if( ((String)selectlist.getSelectedItem()).indexOf(
	                                   SelectionJPanel.DOUBLE_WEDGE) >= 0 )
	    {
	      radiofec.setSelected(SelectionJPanel.DOUBLE_WEDGE);
	      Float1DAttribute att = (Float1DAttribute)
	            data.getAttribute(SelectionJPanel.DOUBLE_WEDGE);
	      float[] attlist = att.getFloatValue();
	      for( int i = 0; i < attlist.length; i++ )
	        radiofec.setValue( i,attlist[i] );
	    }
	    else if( ((String)selectlist.getSelectedItem()).indexOf(
	                                   SelectionJPanel.WEDGE) >= 0 )
	    {
	      radiofec.setSelected(SelectionJPanel.WEDGE);
	      Float1DAttribute att =
	             (Float1DAttribute)data.getAttribute(SelectionJPanel.WEDGE);
	      float[] attlist = att.getFloatValue();
	      for( int i = 0; i < attlist.length; i++ )
	        radiofec.setValue( i,attlist[i] );
	    }
	    else if( ((String)selectlist.getSelectedItem()).indexOf(
	                                   SelectionJPanel.RING) >= 0 )
	    {
	      radiofec.setSelected(SelectionJPanel.RING);
	      Float1DAttribute att =
	             (Float1DAttribute)data.getAttribute(SelectionJPanel.RING);
	      float[] attlist = att.getFloatValue();
	      for( int i = 0; i < attlist.length; i++ )
	        radiofec.setValue( i,attlist[i] );
	    }
	  }
	  this_editor.validate();
	  this_editor.repaint();
	  // this will repaint the image when a selection is made.
	  if( this_viewer != null )
	  {
	    this_viewer.validate();
	    this_viewer.repaint();
	  }
	}
	// give focus back to ImageViewComponent
	if( ivc != null )
          ivc.returnFocus();
      }
    }
  
  } // end of SANDControlPanel
  
 /**
  * Main program - Running this viewer as a stand-alone application will
  * use this method.
  *
  *  @param  args If an argument exists, it is assumed to be a filename.
  *               The main() will automatically call loadData() if an
  *               argument is passed in. Make sure the filename includes
  *               the path, but not any spaces.
  */
  public static void main( String args[] )
  {
    SANDWedgeViewer wedgeviewer = new SANDWedgeViewer();
    // Since dispose does not return focus to the terminal, use exit to
    // terminate the viewer when used in a stand-alone application.
    wedgeviewer.addWindowListener( new WindowAdapter()
      {
        public void windowClosing( WindowEvent we )
	{
	  System.exit(0);
	}
      } );
    if( args.length > 0 )
      wedgeviewer.loadData( args[0] );
    WindowShower shower = new WindowShower(wedgeviewer);
    java.awt.EventQueue.invokeLater(shower);
    shower = null;
  }

}

/*
 * File: SelectionOverlay.java
 *
 * Copyright (C) 2003, Mike Miller
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log: SelectionOverlay.java,v $
 *  Revision 1.40  2005/05/25 20:28:33  dennis
 *  Now calls convenience method WindowShower.show() to show
 *  the window, instead of instantiating a WindowShower object
 *  and adding it to the event queue.
 *
 *  Revision 1.39  2005/01/20 23:05:52  millermi
 *  - Added super.paint(g) to paint method.
 *
 *  Revision 1.38  2004/05/18 19:40:17  millermi
 *  - Changed layout of editor to BoxLayout.
 *  - Make use of set...() methods to set private variables.
 *
 *  Revision 1.37  2004/04/02 20:58:33  millermi
 *  - Fixed javadoc errors
 *
 *  Revision 1.36  2004/03/15 23:53:53  dennis
 *  Removed unused imports, after factoring out the View components,
 *  Math and other utils.
 *
 *  Revision 1.35  2004/03/12 03:14:15  serumb
 *  Change package and imports.
 *
 *  Revision 1.34  2004/02/14 03:37:23  millermi
 *  - Replaced all WCRegion code with Region class.
 *  - if regions are in setObjectState(), REGION_ADDED message
 *    now sent to listeners.
 *  - Removed "Selected" from method names.
 *
 *  Revision 1.33  2004/02/06 23:23:43  millermi
 *  - Changed how editor bounds were stored in the ObjectState,
 *    removed check if visible.
 *
 *  Revision 1.32  2004/01/30 22:16:13  millermi
 *  - Changed references of messaging Strings from the IViewControl
 *    to the respective control that sent the message.
 *
 *  Revision 1.31  2004/01/29 08:16:28  millermi
 *  - Updated the getObjectState() to include parameter for specifying
 *    default state.
 *  - Added static variables DEFAULT and PROJECT to IPreserveState for
 *    use by getObjectState()
 *
 *  Revision 1.30  2004/01/07 17:54:33  millermi
 *  - Fixed javadoc errors
 *
 *  Revision 1.29  2004/01/03 04:36:13  millermi
 *  - help() now uses html tool kit to display text.
 *  - Replaced all setVisible(true) with WindowShower.
 *
 *  Revision 1.28  2003/12/30 00:39:37  millermi
 *  - Added Annular selection capabilities.
 *  - Changed SelectionJPanel.CIRCLE to SelectionJPanel.ELLIPSE
 *
 *  Revision 1.27  2003/12/29 06:35:04  millermi
 *  - Added addSelectedRegion() so regions could be added
 *    through commands and not just through the GUI.
 *  - Made paint() more robust, if region has null defining
 *    point, the region is deleted from the list of regions.
 *
 *  Revision 1.26  2003/12/23 02:21:36  millermi
 *  - Added methods and functionality to allow enabling/disabling
 *    of selections.
 *  - Fixed interface package changes where applicable.
 *
 *  Revision 1.25  2003/12/20 21:37:29  millermi
 *  - implemented kill() so editor and help windows are now
 *    disposed when the kill() is called.
 *
 *  Revision 1.24  2003/12/20 20:07:39  millermi
 *  - Added clearSelectedRegions() so selections can be cleared by
 *    method call.
 *
 *  Revision 1.23  2003/12/18 22:54:37  millermi
 *  - 3 defining points are now passed to the EllipseRegion.
 *    The new point, the center is used in case the selection
 *    is made at the edge of the image.
 *
 *  Revision 1.22  2003/11/21 02:59:55  millermi
 *  - Now saves editor bounds before dispose() is called on
 *    the editor.
 *
 *  Revision 1.21  2003/11/18 01:00:17  millermi
 *  - Made non-save dependent private variables transient.
 *
 *  Revision 1.20  2003/10/20 22:46:53  millermi
 *  - Added private class NotVisibleListener to listen
 *    when the overlay is no longer visible. When not
 *    visible, any editor that is visible will be made
 *    invisible too. This will not dispose the editor,
 *    just setVisible(false).
 *
 *  Revision 1.19  2003/10/16 05:00:09  millermi
 *  - Fixed java docs errors.
 *
 *  Revision 1.18  2003/10/02 23:04:42  millermi
 *  - Added java docs to all public static variables.
 *  - Added constructor to take in ObjectState information.
 *
 *  Revision 1.17  2003/09/24 01:33:41  millermi
 *  - Added static variables to be used as keys by ObjectState
 *  - Added methods setObjectState() and getObjectState() to adjust to
 *    changes made in OverlayJPanel.
 *  - Added componentResized() listener to set editor bounds
 *    when the editor is resized.
 *
 *  Revision 1.16  2003/08/26 03:41:05  millermi
 *  - Added functionality and help() comments for double wedge selection.
 *
 *  Revision 1.15  2003/08/21 18:18:17  millermi
 *  - Updated help() to reflect new controls in editor.
 *  - Added capabilities for wedge selection
 *
 *  Revision 1.14  2003/08/18 20:52:40  millermi
 *  - Added "Add Selection" controls to SelectionEditor so user no longer
 *    needs to know the keyboard events to make selections.
 *  - Added javadoc comments.
 *
 *  Revision 1.13  2003/08/14 22:57:44  millermi
 *  - ControlSlider for editor now ranging from 0-100 which
 *    decreased the number of increments in the slider.
 *
 *  Revision 1.12  2003/08/14 21:48:11  millermi
 *  - Added toFront() to SelectionEditor to display it over the viewer.
 *
 *  Revision 1.11  2003/08/14 17:11:57  millermi
 *  - Added SelectionEditor class
 *  - Now capable of changing the selection color and also the opacity
 *    of the selections.
 *  - Edited help() to provide more description.
 *
 *  Revision 1.10  2003/08/11 23:45:23  millermi
 *  - grouped multiple sendMessage() statements into one statement
 *    after all if statements.
 *  - Added static variable ALL_REGIONS_REMOVED for messaging.
 *
 *  Revision 1.9  2003/08/08 15:54:24  millermi
 *  - Edited Revision 1.8 so it did not exceed 80 characters per line.
 *  - Now uses method getWorldCoordPoints() to access WCRegion class.
 *    This method is more efficient because it returns the whole
 *    array of points, as opposed to getWorldCoordPointAt() which
 *    requires multiple method calls.
 *
 *  Revision 1.8  2003/08/07 22:47:55  millermi
 *  - Added line selection capabilities
 *  - Changed Help menu for REMOVE ALL SELECTIONS from "Double" to "Single" 
 *    click
 *  - Usage of Region class changed to WCRegion class and adapted for generic
 *    number of points
 *
 *  Revision 1.7  2003/08/07 17:57:41  millermi
 *  - Added line selection capabilities
 *  - Changed Help menu for REMOVE ALL SELECTIONS from "Double" to "Single"
 *    click
 *
 *  Revision 1.6  2003/08/06 13:56:45  dennis
 *  - Added sjp.setOpaque(false) to constructor. Fixes bug when
 *    Axis Overlay is turned off and Selection Overlay is on.
 *
 *  Revision 1.5  2003/07/25 14:39:34  dennis
 *  - Constructor now takes component of type IZoomAddible instead of
 *    IAxisAddible2D
 *  - Private class Region now moved to an independent file to allow for use by
 *    components using the selection overlay.
 *  - Added public methods addActionListener(), removeActionListener(),
 *    removeAllActionListeners(), and private method sendMessage() to allow
 *    listeners for when a selection occurs.
 *  - Added getSelectedRegion()
 *    (Mike Miller)
 *
 *  Revision 1.4  2003/06/17 13:21:37  dennis
 *  (Mike Miller)
 *  - Made selections zoomable. clipRect() method was added to paint
 *    to restrict the painted area to only that directly above
 *    the center panel.
 *
 *  Revision 1.3  2003/06/09 14:47:19  dennis
 *  Added static method help() to display commands via the HelpMenu.
 *  (Mike Miller)
 *
 *  Revision 1.2  2003/06/05 22:07:21  dennis
 *     (Mike Miller)
 *   - Added resize capability
 *   - Corrected duplication error
 *   - Added private class Region which includes the current_bounds for each
 *     region in the regions vector.
 *   - Added getFocus() method to fix keylistener problems
 *
 *  Revision 1.1  2003/05/29 14:29:20  dennis
 *  Initial version, current functionality does not support
 *  annotation editing or annotation deletion. (Mike Miller)
 * 
 */
 
/* *************************************************************
 * *********Basic controls for the Selection Overlay************
 * *************************************************************
 * Keyboard Event    * Mouse Event       * Action	       *
 ***************************************************************
 * press B	     * Press/Drag mouse  * box selection       *
 * press C	     * Press/Drag mouse  * circle selection    *
 * press L           * Press/Drag mouse  * line selection      *
 * press P	     * Press/Drag mouse  * point selection     * 
 * none  	     * Double click      * clear last selected *
 * press A (all)     * Single click      * clear all selected  *
 ***************************************************************
 * Important: 
 * All keyboard events must be done prior to mouse events.
 */ 
 
package gov.anl.ipns.ViewTools.Components.Transparency;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit; 
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Vector; 
import java.lang.Math;

import gov.anl.ipns.ViewTools.Panels.Cursors.*;
import gov.anl.ipns.ViewTools.Components.ObjectState;
import gov.anl.ipns.ViewTools.Components.Region.*;
import gov.anl.ipns.ViewTools.Components.Cursor.*; 
import gov.anl.ipns.ViewTools.Components.ViewControls.ControlSlider;
import gov.anl.ipns.Util.Numeric.floatPoint2D; 
import gov.anl.ipns.Util.Sys.ColorSelector;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Panels.Transforms.*;

/**
 * This class allows users to select a region for calculation purposes.
 * Three types of regions may currently be selected: point, box, and circle.
 * The selected region will initially show up in white.  Since this class
 * extends an OverlayJPanel, which extends a JPanel, this class is
 * already serializable.
 */
public class SelectionOverlay extends OverlayJPanel
{
 /**
  * "REGION_ADDED" - This constant String is an Action Listener message
  * sent out when a new region has been selected.
  */
  public static final String REGION_ADDED   = "REGION_ADDED";
  
 /**
  * "REGION_REMOVED" - This constant String is an Action Listener message
  * sent out when a region has been deselected/removed.
  */
  public static final String REGION_REMOVED = "REGION_REMOVED";
  
 /**
  * "ALL_REGIONS_REMOVED" - This constant String is an Action Listener message
  * sent out when all regions have been deselected/removed.
  */
  public static final String ALL_REGIONS_REMOVED = "ALL_REGIONS_REMOVED";
  
  // these variables are used to preserve the Selection state.
 /**
  * "Selected Regions" - This constant String is a key for referencing the
  * state information about which regions have been selected.
  * The value that this key references is a Vector of Region instances.
  */
  public static final String SELECTED_REGIONS = "Selected Regions";
  
 /**
  * "Selection Color" - This constant String is a key for referencing the
  * state information about the color of the selection outlines.
  * The value that this key references is of type Color.
  */
  public static final String SELECTION_COLOR  = "Selection Color";
  
 /**
  * "Opacity" - This constant String is a key for referencing the
  * state information about the invisibility of the selection outline.
  * The value that this key references is a primative float on the range
  * [0,1], with 0 = transparent, 1 = opaque.
  */
  public static final String OPACITY	      = "Opacity";
    
 /**
  * "Editor Bounds" - This constant String is a key for referencing the state
  * information about the size and bounds of the Selection Editor window. 
  * The value that this key references is a Rectangle. The Rectangle contains
  * the dimensions for the editor.
  */
  public static final String EDITOR_BOUNDS    = "Editor Bounds";
    
  private static JFrame helper = null;
  
  private transient SelectionJPanel sjp; // panel overlaying the center jpanel
  private transient IZoomAddible component;	 // component being passed
  private Vector regions;		 // all selected regions
  // used for repaint by SelectListener 
  private transient SelectionOverlay this_panel;
  private Color reg_color;
  private transient Rectangle current_bounds;
  private transient CoordTransform pixel_local;  
  private transient Vector Listeners = null;  
  private float opacity = 1.0f; 	 // value [0,1] where 0 is clear, 
					 // and 1 is solid.
  private transient SelectionEditor editor;
  // buttons for making selections, used by editor.
  private JButton[] sjpbuttons;
  private Rectangle editor_bounds = new Rectangle(0,0,430,290);
 
 /**
  * Constructor creates an overlay with a SelectionJPanel that shadows the
  * center panel of the IZoomAddible component.
  *
  *  @param  iza - IZoomAddible component
  */ 
  public SelectionOverlay(IZoomAddible iza)
  {
    super();
    this.setLayout( new GridLayout(1,1) );
    sjp = new SelectionJPanel();
    sjp.setOpaque(false);
    sjpbuttons = sjp.getControls();
    editor = new SelectionEditor();
    addComponentListener( new NotVisibleListener() );
    component = iza;
    regions = new Vector();	  
    this_panel = this;
    reg_color = Color.white;
     
    this.add(sjp);
    sjp.addActionListener( new SelectListener() ); 
    current_bounds = component.getRegionInfo();
    //this_panel.setBounds( current_bounds );
    CoordBounds pixel_map = 
        	 new CoordBounds( (float)current_bounds.getX(), 
        			  (float)current_bounds.getY(),
        			  (float)(current_bounds.getX() + 
    					  current_bounds.getWidth()),
    				  (float)(current_bounds.getY() + 
    					  current_bounds.getHeight() ) );
    pixel_local = new CoordTransform( pixel_map, 
        			      component.getLocalCoordBounds() );
    
    Listeners = new Vector();
    sjp.requestFocus(); 	      
  }
 
 /**
  * Constructor creates an SelectionOverlay with previous state information.
  *
  *  @param  iza - IZoomAddible component
  *  @param  state - ObjectState of this overlay
  */ 
  public SelectionOverlay(IZoomAddible iza, ObjectState state)
  {
    this(iza);
    setObjectState(state);
  }

 /**
  * Contains/Displays control information about this overlay.
  */
  public static void help()
  {
    helper = new JFrame("Help for Selection Overlay");
    helper.setBounds(0,0,600,400);
    JEditorPane textpane = new JEditorPane();
    textpane.setEditable(false);
    textpane.setEditorKit( new HTMLEditorKit() );
    String text = "<H1>Description:</H1>" +
                  "<P>The Selection Overlay is used to selection regions of " +
        	  "data for analysis. The selected region will initially be " +
        	  "outlined in white, unless otherwise specified.</P>" +
                  "<H2>Commands for Selection Overlay</H2>" +
                  "<P>Note:<BR>" +
        	  "- These commands will NOT work if the Annotation " +
        	  "Overlay checkbox IS checked or if the Selection " + 
    		  "Overlay IS NOT checked.<BR>" +
    		  "- Zooming on the image is only allowed if this overlay " +
    		  "is turned off.</P>"  +
                  "<H2>Image Commands:</H2>" +
                  "<P>Click/Drag/Release Mouse w/B_Key pressed>" + 
        	  "ADD BOX SELECTION<BR>" +
                  "Click/Drag/Release Mouse w/C_Key pressed>" + 
        	  "ADD ELLIPSE SELECTION<BR>" +
                  "Click/Drag/Release Mouse w/D_Key pressed>" + 
        	  "ADD DOUBLE WEDGE SELECTION<BR>" +
                  "Click/Drag/Release Mouse w/L_Key pressed>" + 
        	  "ADD LINE SELECTION<BR>" +
                  "Click/Drag/Release Mouse w/P_Key pressed>" + 
        	  "ADD POINT SELECTION<BR>" +
                  "Click/Drag/Release Mouse w/R_Key pressed>" + 
        	  "ADD RING SELECTION<BR>" +
                  "Click/Drag/Release Mouse w/W_Key pressed>" + 
        	  "ADD WEDGE SELECTION<BR>" +
                  "Double Click Mouse>REMOVE LAST SELECTION<BR>" +
                  "Single Click Mouse w/A_Key>REMOVE ALL SELECTIONS</P>" +
                  "<H2>Selection Editor Commands <BR>" +
		  "(Edit button under Selection Overlay Control)</H2><P>" +
                  "Click on button corresponding to region type in editor, " +
        	  "then on image Click/Drag/Release mouse to ADD SELECTION" +
                  "<BR>Move slider to CHANGE OPACITY OF SELECTION. If highly " +
        	  "opaque, lines show bright. Low opacity makes selections " +
    		  "clear or transparent.<BR>" +
                  "Click on \"Change Color\" to CHANGE COLOR OF SELECTION.</P>";
    
    textpane.setText(text);
    JScrollPane scroll = new JScrollPane(textpane);
    scroll.setVerticalScrollBarPolicy(
 				    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    helper.getContentPane().add(scroll);
    WindowShower.show(helper);
  }
  
 /**
  * This method will set the current state variables of the object to state
  * variables wrapped in the ObjectState passed in.
  *
  *  @param new_state
  */
  public void setObjectState( ObjectState new_state )
  {	       
    boolean redraw = false;  // if any values are changed, repaint overlay.
    Object temp = new_state.get(SELECTED_REGIONS);
    if( temp != null )
    {
      regions = ((Vector)temp);
      redraw = true;
      // only send message if region was added.
      if( regions.size() > 0 )
        sendMessage(REGION_ADDED); 
    }
    
    temp = new_state.get(SELECTION_COLOR);
    if( temp != null )
    {
      setRegionColor( (Color)temp );
      redraw = true;  
    }
    
    temp = new_state.get(OPACITY);
    if( temp != null )
    {
      setOpacity( ((Float)temp).floatValue() ); 
      redraw = true;  
    }  
     
    temp = new_state.get(EDITOR_BOUNDS);
    if( temp != null )
    {
      editor_bounds = (Rectangle)temp;
      editor.setBounds( editor_bounds );  
    }
    
    if( redraw )
      this_panel.repaint(); 
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
    state.insert( SELECTION_COLOR, reg_color );
    state.insert( OPACITY, new Float(opacity) );
    state.insert( EDITOR_BOUNDS, editor_bounds );
    
    // load these for project specific instances.
    if( !isDefault )
    {
      state.insert( SELECTED_REGIONS, regions );
    }
    
    return state;
  }
 
 /**
  * This method sets the opaqueness of the selection. Values will fall in the
  * interval [0,1], with 1 being opaque, and 0 being transparent. 
  *
  *  @param  value - on interval [0,1]
  */ 
  public void setOpacity( float value )
  {
    if( value > 1 )
      opacity = 1.0f;
    else if( value < 0 )
      opacity = 0;
    else
      opacity = value;
  }
  
 /**
  * This method is used to view an instance of the Selection Editor.
  */ 
  public void editSelection()
  {
    if( editor.isVisible() )
    {
      editor.toFront();
      editor.requestFocus();
    }
    else
    {
      editor_bounds = editor.getBounds();
      editor.dispose();
      editor = new SelectionEditor();
      WindowShower.show(editor);
      editor.toFront();
    }
  }
  
 /**
  * Method to add a listener to this overlay.
  *
  *  @param act_listener
  */
  public void addActionListener( ActionListener act_listener )
  {	     
    for ( int i = 0; i < Listeners.size(); i++ )    // don't add it if it's
      if ( Listeners.elementAt(i).equals( act_listener ) ) // already there
        return;

    Listeners.add( act_listener ); //Otherwise add act_listener
  }
 
 /**
  * Method to remove a listener from this component.
  *
  *  @param act_listener
  */ 
  public void removeActionListener( ActionListener act_listener )
  {
    Listeners.remove( act_listener );
  }
 
 /**
  * Method to remove all listeners from this component.
  */ 
  public void removeAllActionListeners()
  {
    Listeners.removeAllElements();
  }
  
 /**
  * This method gets the vector containing all of the selected regions. All
  * regions in the vector are in a Region wrapper.
  *
  *  @return region vector
  */ 
  public Vector getRegions()
  {
    return regions;
  }
  
 /**
  * Remove all selections from the overlay.
  */
  public void clearRegions()
  {
    if( regions.size() > 0 )
    {
      regions.clear(); 
      sendMessage(ALL_REGIONS_REMOVED);
    }
  }
  
 /**
  * This method allows a user to add a region with a method instead of by
  * using the GUI.
  *
  *  @param  reg The array of Regions to be added.
  */
  public void addRegions( Region[] reg )
  {
    // ignore if null
    if( reg == null || reg.length == 0 )
      return;
    // add all regions in the array.
    for( int i = 0; i < reg.length; i++ )
      regions.add(reg[i]);
    // send message that region was added.
    sendMessage(REGION_ADDED);
  } 
  
 /**
  * This method sets all the colors for the selected regions. Initially set
  * to white.
  *
  *  @param  color
  */
  public void setRegionColor( Color color )
  {
    reg_color = color;
    this_panel.repaint();
  }
  
 /**
  * This method gives focus to the SelectionJPanel, which is overlayed on the
  * center of the IZoomAddible component.
  */
  public void getFocus()
  {
    sjp.requestFocus();
  }
     
 /**
  * This method is called by to inform the overlay that it is no
  * longer needed. In turn, the overlay closes all windows created
  * by it before closing.
  */ 
  public void kill()
  {
    editor.dispose();
    if( helper != null )
      helper.dispose();
  }
 
 /**
  * This method will disable the selections and cursors included in the names
  * list. Names are defined by static Strings in the SelectionJPanel class.
  *
  *  @param  select_names List of selection names defined by
  *                       SelectionJPanel class.
  *  @see gov.anl.ipns.ViewTools.Components.Cursor.SelectionJPanel
  */ 
  public void disableSelection( String[] select_names )
  {
    sjp.disableCursor( select_names );
    sjpbuttons = sjp.getControls();
    
    if( editor.isVisible() )
    {
      editor.dispose();
      editSelection();
    }
  }
  
 /**
  * This method will enable the selections and cursors included in the names
  * list. Names are defined by static Strings in the SelectionJPanel class.
  *
  *  @param  select_names List of selection names defined by
  *                       SelectionJPanel class.
  *  @see gov.anl.ipns.ViewTools.Components.Cursor.SelectionJPanel
  */ 
  public void enableSelection( String[] select_names )
  {
    sjp.enableCursor( select_names );
    sjpbuttons = sjp.getControls();
    
    if( editor.isVisible() )
    {
      editor.dispose();
      editSelection();
    }
  }

 /**
  * Overrides paint method. This method will paint the selected regions.
  *
  *  @param  g - graphics object
  */
  public void paint(Graphics g) 
  { 
    super.paint(g);
    Graphics2D g2d = (Graphics2D)g; 
    // Change the opaqueness of the selections.
    AlphaComposite ac = AlphaComposite.getInstance( AlphaComposite.SRC_OVER,
        					    opacity );
    g2d.setComposite(ac);
    
    current_bounds = component.getRegionInfo();  // current size of center
    sjp.setBounds( current_bounds );
    // this limits the paint window to the size of the background image.
    g2d.clipRect( (int)current_bounds.getX(),
        	  (int)current_bounds.getY(),
    		  (int)current_bounds.getWidth(),
    		  (int)current_bounds.getHeight() );
    // the current pixel coordinates
    CoordBounds pixel_map = 
            new CoordBounds( (float)current_bounds.getX(), 
        		     (float)current_bounds.getY(),
        		     (float)(current_bounds.getX() + 
    				     current_bounds.getWidth()),
    			     (float)(current_bounds.getY() + 
    				     current_bounds.getHeight() ) );
    pixel_local.setSource( pixel_map );
    pixel_local.setDestination( component.getLocalCoordBounds() );
    // color of all of the selections.
    g2d.setColor(reg_color);

    Region region;    
    floatPoint2D[] fp;
    Point[] p;
    boolean nullfound = false;
    for( int num_reg = 0; num_reg < regions.size(); num_reg++ )
    {
      region = (Region)regions.elementAt(num_reg);
      fp = region.getDefiningPoints(Region.WORLD);
      p = new Point[fp.length];
      for( int i = 0; i < fp.length; i++ )
      {
        if( fp[i] == null )
	{
	  regions.remove(num_reg);
	  i = fp.length;
	  nullfound = true;
	}
	else
    	  p[i] = convertToPixelPoint( fp[i] );
      }
      if( !nullfound )
      {
        //System.out.println("Point: " + p[0].x + "," + p[0].y );   
        if( region instanceof EllipseRegion )
        {
          g2d.drawOval( p[0].x, p[0].y, p[1].x - p[0].x, p[1].y - p[0].y );
        }
        else if( region instanceof BoxRegion )
        {
          g2d.drawRect( p[0].x, p[0].y, p[1].x - p[0].x, p[1].y - p[0].y );
        }
        else if( region instanceof LineRegion )
        {
          g2d.drawLine( p[0].x, p[0].y, p[1].x, p[1].y );	   
        }
        else if( region instanceof PointRegion )
        {
          //System.out.println("Drawing instance of point at " + 
          //		     ((Point)region).x + "/" + ((Point)region).y );
          g2d.drawLine( p[0].x - 5, p[0].y, p[0].x + 5, p[0].y );	  
          g2d.drawLine( p[0].x, p[0].y - 5, p[0].x, p[0].y + 5 );
        }
        else if( region instanceof WedgeRegion )
        {
         /* p[0]   = center pt of circle that arc is taken from
          * p[1]   = last mouse point/point at intersection of line and arc
          * p[2]   = reflection of p[1]
          * p[3]   = top left corner of bounding box around arc's total circle
          * p[4]   = bottom right corner of bounding box around arc's circle
          * p[5].x = startangle, the directional vector in degrees
          * p[5].y = degrees covered by arc.
          */
          // Since p[5] is not a point, but angular measures, they are a direct
          // cast from float to int, no convertion needed.
          p[p.length - 1].x = (int)fp[p.length - 1].x;
          p[p.length - 1].y = (int)fp[p.length - 1].y;
          g2d.drawLine( p[0].x, p[0].y, p[1].x, p[1].y );
          g2d.drawLine( p[0].x, p[0].y, p[2].x, p[2].y );	    
          
          g2d.drawArc(p[3].x, p[3].y, p[4].x - p[3].x,
        	      p[4].y - p[3].y, p[5].x, p[5].y);
        }
        else if( region instanceof DoubleWedgeRegion )
        {
         /* p[0]   = center pt of circle that arc is taken from
          * p[1]   = last mouse point/point at intersection of line and arc
          * p[2]   = reflection of p[1]
          * p[3]   = top left corner of bounding box around arc's total circle
          * p[4]   = bottom right corner of bounding box around arc's circle
          * p[5].x = startangle, the directional vector in degrees
          * p[5].y = degrees covered by arc.
          */
          // Since p[5] is not a point, but angular measures, they are a direct
          // cast from float to int, no convertion needed.
          p[p.length - 1].x = (int)fp[p.length - 1].x;
          p[p.length - 1].y = (int)fp[p.length - 1].y;
          g2d.drawLine( 2*p[0].x - p[1].x, 2*p[0].y - p[1].y, p[1].x, p[1].y );
          g2d.drawLine( 2*p[0].x - p[2].x, 2*p[0].y - p[2].y, p[2].x, p[2].y );
          
          g2d.drawArc(p[3].x, p[3].y, p[4].x - p[3].x,
        	      p[4].y - p[3].y, p[5].x, p[5].y);
          g2d.drawArc(p[3].x, p[3].y, p[4].x - p[3].x,
        	      p[4].y - p[3].y, p[5].x + 180, p[5].y);
        }
        else if( region instanceof AnnularRegion )
        {
         /* p[0]   = center pt of circle
          * p[1]   = top left corner of bounding box of inner circle
          * p[2]   = bottom right corner of bounding box of inner circle
          * p[3]   = top left corner of bounding box of outer circle
          * p[4]   = bottom right corner of bounding box of outer circle
          */
          g2d.drawOval( p[1].x, p[1].y, p[2].x - p[1].x, p[2].y - p[1].y );
          g2d.drawOval( p[3].x, p[3].y, p[4].x - p[3].x, p[4].y - p[3].y );
        }
      }
    }
  } // end of paint()

 /*
  * Converts from world coordinates to a pixel point
  */
  private Point convertToPixelPoint( floatPoint2D fp )
  {
    floatPoint2D fp2d = pixel_local.MapFrom( fp );
    return new Point( (int)fp2d.x, (int)fp2d.y );
  }
 
 /*
  * Converts from pixel coordinates to world coordinates.
  */
  private floatPoint2D convertToWorldPoint( Point p )
  {
    return pixel_local.MapTo( new floatPoint2D((float)p.x, (float)p.y) );
  }
  
 /*
  * Tells all listeners about a new action.
  *
  *  @param  message
  */  
  private void sendMessage( String message )
  {
    for ( int i = 0; i < Listeners.size(); i++ )
    {
      ActionListener listener = (ActionListener)Listeners.elementAt(i);
      listener.actionPerformed( new ActionEvent( this, 0, message ) );
    }
  }

 /*
  * SelectListener listens for messages being passed from the SelectionJPanel.
  */
  private class SelectListener implements ActionListener
  {
    public void actionPerformed( ActionEvent ae )
    {
      String message = ae.getActionCommand(); 
      // clear all selections from the vector
      if( message.equals( SelectionJPanel.RESET_SELECTED ) )
      { 
	if( regions.size() > 0 )
	{
	  regions.clear(); 
	  sendMessage(ALL_REGIONS_REMOVED);
	}	  
      }
      // remove the last selection from the vector
      else if( message.equals( SelectionJPanel.RESET_LAST_SELECTED ) )
      {
	if( regions.size() > 0 )
	{
	  regions.removeElementAt(regions.size() - 1);  
	  sendMessage(REGION_REMOVED);
	}		  
      }
      // region is specified by REGION_SELECTED>BOX >ELLIPSE >POINT   
      // if REGION_SELECTED is in the string, find which region 
      else if( message.indexOf( SelectionJPanel.REGION_SELECTED ) > -1 )
      {
	boolean regionadded = true;
	if( message.indexOf( SelectionJPanel.BOX ) > -1 )
	{
	  Rectangle box = ((BoxCursor)sjp.getCursor( 
				 SelectionJPanel.BOX )).region();
	  Point p1 = new Point( box.getLocation() );
	  p1.x += (int)current_bounds.getX();
	  p1.y += (int)current_bounds.getY();
	  Point p2 = new Point( p1 );
	  p2.x += (int)box.getWidth();
	  p2.y += (int)box.getHeight();
	  floatPoint2D[] tempwcp = new floatPoint2D[2];
	  tempwcp[0] = convertToWorldPoint( p1 );
	  tempwcp[1] = convertToWorldPoint( p2 );
			 
	  regions.add( new BoxRegion(tempwcp) );
	  //System.out.println("Drawing box region" );
	}
	else if( message.indexOf( SelectionJPanel.ELLIPSE ) > -1 )
	{
	  Circle circle = ((CircleCursor)sjp.getCursor( 
				 SelectionJPanel.ELLIPSE )).region();
	  // top-left corner
	  Point p1 = new Point( circle.getDrawPoint() );
	  p1.x += (int)current_bounds.getX();
	  p1.y += (int)current_bounds.getY();
	  // bottom-right corner
	  Point p2 = new Point( circle.getCenter() );
	  p2.x += circle.getRadius() + (int)current_bounds.getX();
	  p2.y += circle.getRadius() + (int)current_bounds.getY();
	  // center of circle
	  Point p3 = new Point( circle.getCenter() );
	  p3.x += (int)current_bounds.getX();
	  p3.y += (int)current_bounds.getY();
	  floatPoint2D[] tempwcp = new floatPoint2D[3];
	  tempwcp[0] = convertToWorldPoint( p1 );
	  tempwcp[1] = convertToWorldPoint( p2 );
	  tempwcp[2] = convertToWorldPoint( p3 );
				
	  regions.add( new EllipseRegion(tempwcp) );
	}	
	else if( message.indexOf( SelectionJPanel.LINE ) > -1 )
	{
	  Line line = ((LineCursor)sjp.getCursor( 
				  SelectionJPanel.LINE )).region();
	  Point p1 = new Point( line.getP1() );
	  p1.x += (int)current_bounds.getX();
	  p1.y += (int)current_bounds.getY();
	  Point p2 = new Point( line.getP2() );
	  p2.x += (int)current_bounds.getX();
	  p2.y += (int)current_bounds.getY();
	  floatPoint2D[] tempwcp = new floatPoint2D[2];
	  tempwcp[0] = convertToWorldPoint( p1 );
	  tempwcp[1] = convertToWorldPoint( p2 );
	
	  regions.add( new LineRegion(tempwcp) );
	}	
	else if( message.indexOf( SelectionJPanel.POINT ) > -1 )
	{ 
	  //System.out.println("Drawing point region" );
	  // create new point, otherwise regions would be shared.
	  Point np = new Point( ((PointCursor)
		  sjp.getCursor( SelectionJPanel.POINT )).region() );
	  np.x += (int)current_bounds.getX();
	  np.y += (int)current_bounds.getY();
	  floatPoint2D[] tempwcp = new floatPoint2D[1];
	  tempwcp[0] = convertToWorldPoint( np );
	  regions.add( new PointRegion(tempwcp) );
	}    
	else if( message.indexOf( SelectionJPanel.WEDGE ) > -1 &&
        	 message.indexOf( SelectionJPanel.DOUBLE_WEDGE ) == -1 )
	{ 
	  //System.out.println("Drawing wedge region" );
	  // create new point, otherwise regions would be shared.
	  Point[] p_array = ( ((WedgeCursor)
		  sjp.get3ptCursor( SelectionJPanel.WEDGE )).region() );
	  floatPoint2D[] tempwcp = new floatPoint2D[p_array.length];
	  for( int i = 0; i < p_array.length - 1; i++ )
          {
            p_array[i].x += (int)current_bounds.getX();
	    p_array[i].y += (int)current_bounds.getY();
	    tempwcp[i] = convertToWorldPoint( p_array[i] );
          }
          // Since these are angles, they do not need transforming
          if( p_array.length > 0 )
          {
            tempwcp[p_array.length - 1] = new floatPoint2D( 
        			       (float)p_array[p_array.length - 1].x,
        			       (float)p_array[p_array.length - 1].y );
          }
          
	  regions.add( new WedgeRegion(tempwcp) );
	}
	else if( message.indexOf( SelectionJPanel.DOUBLE_WEDGE ) > -1 )
	{ 
	  // create new point, otherwise regions would be shared.
	  Point[] p_array = ( ((DoubleWedgeCursor)
		  sjp.get3ptCursor( SelectionJPanel.DOUBLE_WEDGE )).region() );
	  floatPoint2D[] tempwcp = new floatPoint2D[p_array.length];
	  for( int i = 0; i < p_array.length - 1; i++ )
          {
            p_array[i].x += (int)current_bounds.getX();
	    p_array[i].y += (int)current_bounds.getY();
	    tempwcp[i] = convertToWorldPoint( p_array[i] );
          }
          // Since these are angles, they do not need transforming
          if( p_array.length > 0 )
          {
            tempwcp[p_array.length - 1] = new floatPoint2D( 
        			       (float)p_array[p_array.length - 1].x,
        			       (float)p_array[p_array.length - 1].y );
          }
	  regions.add( new DoubleWedgeRegion(tempwcp) );
	} 
	else if( message.indexOf( SelectionJPanel.RING ) > -1 )
	{ 
	  // create new point, otherwise regions would be shared.
	  Point[] p_array = ( ((AnnularCursor)
		  sjp.get3ptCursor( SelectionJPanel.RING )).region() );
	  // center of ring
	  Point p1 = new Point( p_array[0] );
	  p1.x += (int)current_bounds.getX();
	  p1.y += (int)current_bounds.getY();
	  
	  // inner top-left corner
	  Point p2 = new Point( p1 );
	  p2.x -= p_array[1].x;
	  p2.y -= p_array[1].x;
	  // inner bottom-right corner
	  Point p3 = new Point( p1 );
	  p3.x += p_array[1].x;
	  p3.y += p_array[1].x;
	  
	  // outer top-left corner
	  Point p4 = new Point( p1 );
	  p4.x -= p_array[1].y;
	  p4.y -= p_array[1].y;
	  // outer bottom-right corner
	  Point p5 = new Point( p1 );
	  p5.x += p_array[1].y;
	  p5.y += p_array[1].y;
	  
	  floatPoint2D[] tempwcp = new floatPoint2D[5];
	  tempwcp[0] = convertToWorldPoint( p1 );
	  tempwcp[1] = convertToWorldPoint( p2 );
	  tempwcp[2] = convertToWorldPoint( p3 );
	  tempwcp[3] = convertToWorldPoint( p4 );
	  tempwcp[4] = convertToWorldPoint( p5 );
	  
	  regions.add( new AnnularRegion(tempwcp) );
	}
        else  // no recognized region was added
          regionadded = false;
	
        if( regionadded )
          sendMessage(REGION_ADDED);
      }
      this_panel.repaint();  // Without this, the newly drawn regions would
			     // not appear.
    }  // end actionPerformed()   
  } // end SelectListener
  
 /*
  * This class is the editor for the Selection Overlay. This is used to 
  * create a selection, change opacity of a selection, and change selection
  * color.
  */ 
  private class SelectionEditor extends JFrame
  {
    private JPanel pane;
    private SelectionEditor this_editor;
    
    public SelectionEditor()
    {
      super("SelectionEditor");
      this.setBounds(editor_bounds);
      this_editor = this;
      pane = new JPanel();
      new BoxLayout( pane, BoxLayout.Y_AXIS );
      // Number of grid rows needed for the selection type buttons,
      // and add one in for the JLabel.
      int gridrows = (int)Math.ceil( (double)(sjpbuttons.length + 1)/3 );
      // If number of rows are specified, the number of columns doesn't matter.
      JPanel sjpcontrols = new JPanel( new GridLayout( gridrows, 1 ) );
      sjpcontrols.add( new JLabel("Add Selection") );
      for( int i = 0; i < sjpbuttons.length; i++ )
	sjpcontrols.add( sjpbuttons[i] );
     
      pane.add( sjpcontrols );
      
      ColorSelector color_chooser = new ColorSelector(ColorSelector.SWATCH);
      color_chooser.addActionListener( new ControlListener() );
      
      pane.add(color_chooser);
      
      // Slider that controls the opaqueness of the selections.
      ControlSlider opacityscale = 
			   new ControlSlider("Selection Opacity Scale");
      opacityscale.setStep(.01f);
      opacityscale.setRange(0f,1f);
      opacityscale.setMajorTickSpace(.2f);
      opacityscale.setMinorTickSpace(.05f);
      opacityscale.setValue(opacity);
      opacityscale.addActionListener( new ControlListener() );
      
      JButton closebutton = new JButton("Close");
      closebutton.addActionListener( new ControlListener() );
      JPanel spacer = new JPanel();
      spacer.setPreferredSize( new Dimension(editor_bounds.width/4,0) );
      
      JPanel slider_and_close = new JPanel( new BorderLayout() );
      slider_and_close.add(opacityscale, BorderLayout.WEST );
      slider_and_close.add(spacer, BorderLayout.CENTER );
      slider_and_close.add(closebutton, BorderLayout.EAST );
      
      pane.add(slider_and_close);
      
      this.getContentPane().add(pane);
      this_editor.addComponentListener( new EditorListener() );
    }
    
   /*
    * Private listener for the SelectionEditor. This class listens to all
    * of the controls on the editor.
    */ 
    class ControlListener implements ActionListener
    {
      public void actionPerformed( ActionEvent ae )
      {
        String message = ae.getActionCommand();
	if( message.equals( ColorSelector.COLOR_CHANGED ) )
        {
	  setRegionColor( ((ColorSelector)ae.getSource()).getSelectedColor() );
	}
        else if( message.equals( ControlSlider.SLIDER_CHANGED ) )
        {
          setOpacity( ((ControlSlider)ae.getSource()).getValue() );
        }
        else if( message.equals("Close") )
        {  
	  editor_bounds = this_editor.getBounds(); 
          this_editor.dispose();
        }
        this_panel.repaint();
      }
    }
     
    class EditorListener extends ComponentAdapter
    {
      public void componentResized( ComponentEvent we )
      {
    	editor_bounds = editor.getBounds();
      }
    }	     
  }
  
 /*
  * This class will hide the SelectionEditor if the editor is visible but
  * the overlay is not.
  */
  private class NotVisibleListener extends ComponentAdapter
  {
    public void componentHidden( ComponentEvent ce )
    {
      editor.setVisible(false);
    }
  }
}
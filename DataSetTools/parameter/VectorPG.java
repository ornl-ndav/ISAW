/*
 * File:  VectorPG.java
 *
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.34  2003/09/03 23:35:15  bouzekc
 * Changed direct this.value call to getValue().
 *
 * Revision 1.33  2003/09/03 14:54:04  bouzekc
 * Removed inner ActionListener class (as it is now in ArrayEntryJFrame).
 *
 * Revision 1.32  2003/08/30 20:00:02  bouzekc
 * Added javadoc comments to initGUi to signal that derived classes should
 * use this method in general.
 *
 * Revision 1.31  2003/08/30 19:47:12  bouzekc
 * Now uses ArrayEntryJFrame.
 *
 * Revision 1.30  2003/08/28 03:36:53  bouzekc
 * Made innerParameter private and added a method to set the parameter so that
 * the type can be set more accurately in the derived classes.
 *
 * Revision 1.29  2003/08/28 02:50:53  bouzekc
 * Changed call from addPCLtoVector to super.addPropertyChangeListener().
 *
 * Revision 1.28  2003/08/28 02:31:22  bouzekc
 * Now makes use of ParameterGUI's constructors.  Removed setEnabled() and
 * getType() methods.  Changed instance variable param to innerParam and
 * made it protected.
 *
 * Revision 1.27  2003/08/26 18:26:34  bouzekc
 * Made the internal ArrayEntryJPanel protected so that subclasses can use
 * it.  Removed layout setup for entrywidget.
 *
 * Revision 1.26  2003/08/22 20:12:06  bouzekc
 * Modified to work with EntryWidget.
 *
 * Revision 1.25  2003/08/22 01:25:45  bouzekc
 * Removed erroneous getEntryWidget() method.
 *
 * Revision 1.24  2003/08/19 21:01:54  bouzekc
 * Removed old entrywidget reference from initGUI().
 *
 * Revision 1.23  2003/08/19 18:49:56  rmikk
 * Arrays retain their initial values.
 * An empty vector is returned instead of null
 *
 * Revision 1.22  2003/08/16 02:09:54  bouzekc
 * Now properly adds PropertyChangeListeners which exist before the GUI is
 * created.
 *
 * Revision 1.21  2003/08/15 23:56:21  bouzekc
 * Modified to work with new IParameterGUI and ParameterGUI.
 *
 * Revision 1.20  2003/08/15 03:59:22  bouzekc
 * Removed init() method.  Now relies on init(Vector).
 *
 * Revision 1.19  2003/07/10 18:29:42  bouzekc
 * Replaced deprecated show() with setVisible(boolean).
 *
 * Revision 1.18  2003/07/07 22:40:05  bouzekc
 * Now uses a class constant for the "Data Changed" event,
 * and checks to be sure that the event is correct in
 * propertyChange().
 *
 * Revision 1.17  2003/07/07 21:51:30  bouzekc
 * Reorganized methods according to access privilege.
 *
 * Revision 1.16  2003/07/07 21:42:18  bouzekc
 * Fixed bug where the JDialog wasn't fully listening to
 * window closing events and removed extraneous comments.
 *
 * Revision 1.15  2003/07/01 14:44:15  bouzekc
 * Uses a JDialog again.
 *
 * Revision 1.14  2003/06/30 22:26:33  bouzekc
 * Fixed comment to reflect change in ArrayEntryJPanel.
 *
 * Revision 1.13  2003/06/24 20:23:11  bouzekc
 * Cleaned up import statements.
 *
 * Now implements PropertyChanger.
 *
 * Fixed comments running over 80 columns.
 *
 * Added method comments.
 *
 * Removed inner MJPanel class, now
 * uses ArrayEntryJPanel.
 *
 * Fixed PropertyChangeListener methods to add the listener
 * parameter rather than "this".
 *
 * Split showGUI method into showEntryPanel and makeentryPanel
 * methods.
 *
 * Added hook in actionPerformed to get the ActionEvent name.
 *
 * Renamed MyWindowListener inner class to
 * VectorPGWindowListener and changed access to private.
 *
 * Now hides the data entry window when the "OK" button is clicked.
 *
 * Closing the window now triggers a property change event.
 *
 * Revision 1.12  2003/06/23 22:30:20  bouzekc
 * Reorganization of methods and inner classes into blocks
 * in order to separate functionality out.
 *
 * Revision 1.11  2003/06/23 15:20:56  bouzekc
 * Renamed the buttons to slightly less offensive names.
 *
 * Revision 1.10  2003/06/23 15:00:32  bouzekc
 * Removed testbed and inner ActionListener class.
 *
 * Revision 1.9  2003/06/23 13:53:35  bouzekc
 * Reformatted for consistent indenting.
 *
 * Revision 1.8  2003/06/18 20:36:41  pfpeterson
 * Changed calls for NxNodeUtils.Showw(Object) to
 * DataSetTools.util.StringUtil.toString(Object)
 *
 * Revision 1.7  2003/06/10 14:54:07  pfpeterson
 * Commented out a call that was meant to be a comment.
 *
 * Revision 1.6  2003/06/10 14:42:21  rmikk
 * Now implements ParamUsesString
 * All GUI elements are now created first in init()
 *
 * Revision 1.5  2003/06/09 22:35:52  rmikk
 * Changed JFrames to JDialog's in a JFrame so they work with
 *    the JParametersDialog system
 * The entry widget that gets one element of a Vector is now
 *    in the same window as the JList of values.
 * Reduced the need to click as many buttons to get a list in
 *
 * Revision 1.4  2003/06/06 18:49:44  pfpeterson
 * Made abstract and removed clone method.
 *
 * Revision 1.3  2003/05/25 19:09:16  rmikk
 * -Added more documentation
 * -Revised the Property Change handling
 * -Fixed details to get VectorPG of VectorPG to work
 *
 * Revision 1.2  2003/05/21 20:10:30  pfpeterson
 * Turned MyActionList into a private class so there is not conflicts
 * when compiling the whole package.
 *
 * Revision 1.1  2003/05/21 17:33:39  rmikk
 * -Initial Checkin.  Base for other intermediate length array entries
 *
 */
package DataSetTools.parameter;

import DataSetTools.components.ParametersGUI.ArrayEntryJFrame;
import DataSetTools.components.ParametersGUI.EntryWidget;

import DataSetTools.util.PropertyChanger;
import DataSetTools.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.*;

import java.util.*;

import javax.swing.*;


/**
 * This parameterGUI is the parent class of other parameterGUI's whose values
 * are Vectors with a common Object data type for each elements.  This GUI is
 * best for a medium sized list.  The list appears in a list box where the
 * values can be edited deleted, and/or rearranged. A vector of choicelist
 * should go through this constructor
 */
public abstract class VectorPG extends ParameterGUI
  implements PropertyChangeListener, ParamUsesString {
  //~ Static fields/initializers ***********************************************

  public static final String DATA_CHANGED = "Data Changed";

  //~ Instance fields **********************************************************

  private ParameterGUI innerParam;
  private ArrayEntryJFrame GUI            = null;
  private JButton vectorButton            = null;
  private Vector listeners                = new Vector(  );
  private JDialog entryDialog;
  private JFrame entryFrame;
  private final String TYPE               = "Array";

  //~ Constructors *************************************************************

  /**
   * Constructor
   *
   * @param name the prompt string that appears on the  GUI( a button) and the
   *        resultant JFrame when the button is pressed The ParameterGUI is
   *        just a button in a JPanel.  When the button is pressed a more
   *        complicated JFrame is created with the list box and editing
   *        buttons.
   * @param val The initial value to set this VectorPG to.
   */
  public VectorPG( String name, Object val ) {
    super( name, val );
    this.type = TYPE;
  }

  /**
   * Constructor
   *
   * @param name the prompt string that appears on the  GUI( a button) and the
   *        resultant JFrame when the button is pressed The ParameterGUI is
   *        just a button in a JPanel.  When the button is pressed a more
   *        complicated JFrame is created with the list box and editing
   *        buttons.
   * @param val The initial value to set this VectorPG to.
   * @param valid Whether this VectorPG should be valid or not (initially).
   */
  public VectorPG( String name, Object val, boolean valid ) {
    super( name, val, valid );
    this.type = TYPE;
  }

  //~ Methods ******************************************************************

  /**
   * Sets the value and displays these values in the associated JList.
   *
   * @param value The new value to set the VectorPG to.
   */
  public void setStringValue( String value ) {
    this.value = ArrayPG.StringtoArray( value );
  }

  /**
   * Accessor method for the String value of this VectorPG.
   *
   * @return This VectorPG's String value.
   */
  public String getStringValue(  ) {
    return ArrayPG.ArraytoString( ( Vector )value );
  }

  /**
   * Sets the value and displays these values in the associated JList.
   *
   * @param newVal The new value to set the VectorPG to.
   */
  public void setValue( Object newVal ) {
    if( newVal instanceof Vector ) {
      value = newVal;
    } else if( newVal instanceof String ) {
      setStringValue( ( String )newVal );
    } else {
      value = null;
    }

    if( GUI != null ) {
      GUI.setValue( value );
    }
  }

  /**
   * Gets the value of the Vector
   */
  public Object getValue(  ) {
    if( value == null ) {
      return new Vector(  );
    }

    return value;
  }

  /**
   * Adds a property change listener to listen for new Vector values
   *
   * @param listener The listener to add.
   */
  public void addPropertyChangeListener( PropertyChangeListener listener ) {
    super.addPropertyChangeListener( listener );

    if( initialized ) {
      GUI.addPropertyChangeListener( listener );
    }
  }

  /**
   * Adds a property change listener to listen for new Vector values
   *
   * @param property The property to listen for.
   * @param listener The listener to add.
   */
  public void addPropertyChangeListener( 
    String property, PropertyChangeListener listener ) {
    super.addPropertyChangeListener( property, listener );

    if( initialized ) {
      GUI.addPropertyChangeListener( property, listener );
    }
  }

  /**
   * Initializes this VectorPG.  In general, this method should work well as a
   * GUI initializer for VectorPG's derived classes.  However, if you need to
   * overwrite it for any reason, you may wish to call this method, since the
   * button that displays the ArrayEntryJFrame cannot be set without it (since
   * the button name is based on the parameter "name" passed into the
   * constructor).
   *
   * @param V The Vector to use when initializing this VectorPG.
   */
  public void initGUI( Vector V ) {
    if( this.initialized ) {
      return;
    }

    if( V != null ) {  // Usually is null so use the previous value
      setValue( V );
    }

    GUI = new ArrayEntryJFrame( innerParam );
    GUI.addPropertyChangeListener( DATA_CHANGED, this );
    GUI.setValue( getValue(  ) );
    vectorButton   = new JButton( innerParam.getName(  ) );
    entrywidget    = new EntryWidget(  );

    entrywidget.add( vectorButton );
    vectorButton.addActionListener( GUI );

    super.initGUI(  );
  }

  /**
   * Listens for events from the internal ArrayEntryJFrame and sets the value
   * if it has changed.
   */
  public void propertyChange( PropertyChangeEvent pce ) {
    if( pce.getPropertyName(  ) == DATA_CHANGED ) {
      setValue( pce.getNewValue(  ) );
    }

    super.propertyChange( pce );
  }

  /**
   * Removes a property change listener.
   *
   * @param listener The listener to remove
   */
  public void removePropertyChangeListener( PropertyChangeListener listener ) {
    super.removePropertyChangeListener( listener );

    if( initialized ) {
      GUI.removePropertyChangeListener( listener );
    }
  }

  /**
   * Method to set the inner ArrayEntryJFrame.
   *
   * @param frame The ArrayEntryJFrame to use.
   */
  protected void setEntryFrame( ArrayEntryJFrame frame ) {
    this.GUI = frame;
  }

  /**
   * Method to get the inner ArrayEntryJFrame.
   *
   * @return frame The internal ArrayEntryJFrame.
   */
  protected ArrayEntryJFrame getEntryFrame(  ) {
    return GUI;
  }

  /**
   * Accessor method for subclasses to get the inner ParameterGUI.
   *
   * @return The inner ParameterGUI.
   */
  protected final ParameterGUI getInnerParam(  ) {
    return innerParam;
  }

  /**
   * Sets this VectorPG's parameter.  Also resets the type to more accurately
   * show what this VectorPG is an array of.
   */
  protected final void setParam( ParameterGUI param ) {
    innerParam   = param;
    this.type    = param.getType(  ) + " " + TYPE;
  }
}

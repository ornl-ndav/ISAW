/*
 * File:  ParameterGUI.java
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.27  2003/09/16 22:43:30  bouzekc
 *  Clarified documentation for class users and subclass writers.
 *
 *  Revision 1.26  2003/09/16 22:31:41  bouzekc
 *  Added documentation indicating the convention of using
 *  PropertyChangeListeners for listening to events.
 *
 *  Revision 1.25  2003/09/13 22:45:30  bouzekc
 *  Removed direct call on entrywidget.setEnabled() to instance method call.
 *
 *  Revision 1.24  2003/09/13 22:13:39  bouzekc
 *  Now uses a Hashtable rather than parallel Vectors to store the list of
 *  PropertyChangeListeners.
 *
 *  Revision 1.23  2003/08/30 17:36:52  bouzekc
 *  Added documentation to reflect the need to set the type in the constructor.
 *
 *  Revision 1.22  2003/08/28 15:45:13  bouzekc
 *  Instantiated internal PropertyChangeSupport.  This removes several
 *  chances at NullPointerExceptions.
 *
 *  Revision 1.21  2003/08/28 04:56:12  bouzekc
 *  The internal PropertyChangeSupport now fires an event when propertyChange()
 *  is invoked.
 *
 *  Revision 1.20  2003/08/28 03:00:15  bouzekc
 *  Made setDrawValid(), getDrawValid(), getEnabled(), getEntryWidget(),
 *  getGUIPanel(), setIgnorePropertyChange(), getIgnorePropertyChange(),
 *  getLabel(), setName(), getName(), getType(), setValid(), and
 *  getValid() final.  Made, addPCLToVector() and removePCLFromVector()
 *  private.
 *
 *  Revision 1.19  2003/08/28 02:25:08  bouzekc
 *  Made setEnabled final.  This is to avoid recoding, since the EntryWidget
 *  has a recursive setEnabled method.
 *
 *  Revision 1.18  2003/08/28 01:30:39  bouzekc
 *  Added constructors to do initialization for all ParameterGUIs.  Added code
 *  in initGUI() to add this as a PropertyChangeListener to the EntryWidget
 *  and to properly set the enabled state.
 *
 *  Revision 1.17  2003/08/28 00:52:40  bouzekc
 *  Fixed potential bug in setEnabled.
 *
 *  Revision 1.16  2003/08/28 00:47:55  bouzekc
 *  Moved setEnabled() into this class because EntryWidget can recursively enable
 *  and disable its constituent components.
 *
 *  Revision 1.15  2003/08/22 20:12:05  bouzekc
 *  Modified to work with EntryWidget.
 *
 *  Revision 1.14  2003/08/16 02:22:29  bouzekc
 *  Added a top level PropertyChangeSupport so that child classes don't need to
 *  rewrite the property change listener methods just to have property change
 *  support.
 *
 *  Revision 1.13  2003/08/16 02:05:33  bouzekc
 *  Fixed NullPointerException when adding PropertyChangeListeners to the
 *  entrywidget in addPCLToWidget().
 *
 *  Revision 1.12  2003/08/15 23:21:15  bouzekc
 *  Removed init() method.  Added documentation to help make it clearer what
 *  to do to create a ParameterGUI.
 *
 *  Revision 1.11  2003/08/15 03:51:04  bouzekc
 *  Made init() final.  Added code to keep track of internal Vector of
 *  PropertyChangeListeners.  Should now properly add PropertyChangeListeners
 *  to the entrywidget.
 *
 *  Revision 1.10  2003/08/15 00:06:19  bouzekc
 *  Made entrywidget protected again.
 *
 *  Revision 1.9  2003/08/15 00:05:01  bouzekc
 *  Filled in javadoc comments.
 *
 *  Revision 1.8  2003/08/14 23:48:44  bouzekc
 *  Reformatted code.
 *
 *  Revision 1.7  2003/08/14 18:45:19  bouzekc
 *  Now implements Serializable.
 *
 *  Revision 1.6  2003/06/20 16:30:25  bouzekc
 *  Removed non-instantiated methods.  Added methods to get and
 *  set the ignore property change value.
 *
 *  Revision 1.5  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.4  2002/11/27 23:22:43  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/09/19 16:07:24  pfpeterson
 *  Changed to work with new system where operators get IParameters in stead of Parameters. Now support clone method.
 *
 *  Revision 1.2  2002/07/15 21:27:07  pfpeterson
 *  Factored out parts of the GUI.
 *
 *  Revision 1.1  2002/06/06 16:14:36  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.parameter;

import DataSetTools.components.ParametersGUI.*;

import DataSetTools.util.PropertyChanger;

import java.awt.*;

import java.beans.*;

import java.util.Vector;

import javax.swing.*;


/**
 * This is a superclass to take care of many of the common details of
 * ParameterGUIs.  DO NOT instantiate initGUI( Vector ) from the interface
 * IParameterGUI in this class.  It is meant to be instantiated in the child
 * class, and the child class should call super.initGUI() to create the full
 * GUI.  In addition, to set the type of this ParameterGUI, the constructor
 * MUST perform a this.type = "SOMETYPE" assignment. <br>
 * <br>
 * NOTE FOR USERS OF THIS CLASS HIERARCHY:<br>
 * <br>
 * The convention for listening to GUI events (mouse clicks, etc.) is through
 * PropertyChangeListeners and PropertyChangeEvents.  The EntryWidget class
 * takes care of firing off PropertyChangeEvents for GUI components, so if all
 * your external class cares about is whether or not the value changed, adding
 * the class as a PropertyChangeListener will work fine. This works because
 * the EntryWidget, by default, has "this" added as a  PropertyChangeListener
 * along with the property name IParameter.VALUE.  You may, however, wish to
 * remove this listener and/or add other PropertyChangeListeners.  If you do
 * so, you should specify the property name  in order to filter the events.   <br>
 * <br>
 * NOTE FOR THOSE SUBCLASSING THIS CLASS: If you want to retrieve the old and
 * new values correctly all the time, the  subclassed ParameterGUI may need to
 * override propertyChange().  If it does  so, use the protected topPCS
 * PropertyChangeSupport to propagate events upward,  in addition to the code
 * needed for acquiring old and new values.
 */
public abstract class ParameterGUI implements IParameterGUI, PropertyChanger,
  PropertyChangeListener, java.io.Serializable {
  //~ Instance fields **********************************************************

  // instance variables for IParameter
  protected String name;
  protected Object value;
  protected boolean valid;
  protected String type;

  // instance variables for IParameterGUI
  protected JLabel label;
  protected EntryWidget entrywidget;
  protected JPanel guipanel;
  protected boolean enabled;
  protected boolean drawvalid;
  protected JCheckBox validcheck;

  // extra instance variables
  protected boolean initialized;
  protected boolean ignore_prop_change;
  protected PropertyChangeSupport topPCS;

  //these are PARALLEL Vectors.  They must be added to and removed from
  //simultaneously.
  private Vector propListeners = new Vector(  );
  private Vector nameList      = new Vector(  );

  //~ Constructors *************************************************************

  /**
   * Constructor
   *
   * @param name The name of this ParameterGUI.
   * @param val The initial value to set this ParameterGUI to.
   */
  public ParameterGUI( String name, Object val ) {
    this( name, val, false );
    setDrawValid( false );
  }

  /**
   * Constructor
   *
   * @param name The name of this ParameterGUI.
   * @param val The initial value to set this ParameterGUI to.
   * @param valid Whether this VectorPG should be valid or not (initially).
   */
  public ParameterGUI( String name, Object val, boolean valid ) {
    setName( name );
    setValue( val );
    setEnabled( true );
    setValid( valid );
    setDrawValid( true );
    initialized          = false;
    ignore_prop_change   = false;
    topPCS               = new PropertyChangeSupport( this );
  }

  //~ Methods ******************************************************************

  /**
   * Specify if the valid checkbox will be drawn.
   *
   * @param draw boolean indicating whether or not to draw the checkbox.
   */
  public final void setDrawValid( boolean draw ) {
    this.drawvalid = draw;
    this.updateDrawValid(  );
  }

  /**
   * Determine if the 'valid' checkbox will be drawn.
   *
   * @return boolean indicating whether or not the checkbox will be drawn.
   */
  public final boolean getDrawValid(  ) {
    return drawvalid;
  }

  /**
   * Done up here because the new EntryWidget will recursively enable/disable
   * the inner components it has.
   *
   * @param enable Whether to enable the EntryWidget associated with this
   *        ParamterGUI or not.
   */
  public final void setEnabled( boolean enable ) {
    this.enabled = enable;

    if( entrywidget != null ) {
      entrywidget.setEnabled( enable );
    }
  }

  /**
   * Determine if the entry widget is enabled.
   *
   * @return boolean indicating whether or not the entrywidget is enabled.
   */
  public final boolean getEnabled(  ) {
    return enabled;
  }

  /**
   * @return The entrywidget associated with this ParameterGUI.
   */
  public final EntryWidget getEntryWidget(  ) {
    return entrywidget;
  }

  /**
   * @return The GUI panel upon which the entrywidget is drawn.
   */
  public final JPanel getGUIPanel(  ) {
    return guipanel;
  }

  /**
   * Method to set the ignore_prop_change variable.  Useful for changing the
   * value and validity within code.
   *
   * @param ignore boolean indicating whether to ignore property changes or
   *        not.
   */
  public final void setIgnorePropertyChange( boolean ignore ) {
    ignore_prop_change = ignore;
  }

  /**
   * Accessor method to get the ignore_prop_change variable.
   *
   * @return boolean indicating whether or not this ParameterGUI will ignore
   *         property changes.
   */
  public final boolean getIgnorePropertyChange(  ) {
    return ignore_prop_change;
  }

  /**
   * @return The label of this ParameterGUI.
   */
  public final JLabel getLabel(  ) {
    return label;
  }

  /**
   * Set the name of the parameter.
   *
   * @param The new name.
   */
  public final void setName( String name ) {
    this.name = name;

    if( !this.initialized ) {
      return;
    }

    if( this.label == null ) {
      label = new JLabel(  );
    }
    label.setText( "  " + this.getName(  ) );
  }

  /**
   * @return The name of the parameter. This is normally used as the title of
   *         the parameter.
   */
  public final String getName(  ) {
    return this.name;
  }

  /**
   * @return The string used in scripts to denote the particular parameter.
   */
  public final String getType(  ) {
    return this.type;
  }

  /**
   * Set the valid state of the parameter.
   *
   * @param boolean indicating whether or not this ParameterGUI should be
   *        considered valid.
   */
  public final void setValid( boolean valid ) {
    this.valid = valid;
    this.updateDrawValid(  );
  }

  /**
   * @return Whether or not this ParameterGUI is valid.
   */
  public final boolean getValid(  ) {
    return this.valid;
  }

  /**
   * Adds the specified property change listener to the inner Vector of
   * listeners.   If this ParameterGUI has been initialized, the
   * PropertyChangeListener is added to the entrywidget as well.
   *
   * @param pcl The property change listener to be added.
   */
  public void addPropertyChangeListener( PropertyChangeListener pcl ) {
    addPCLToVector( pcl );
    topPCS.addPropertyChangeListener( pcl );

    if( this.initialized ) {
      entrywidget.addPropertyChangeListener( pcl );
    }
  }

  /**
   * Adds the specified property change listener to the inner Vector of
   * listeners, and to the entrywidget if this ParameterGUI has been
   * initialized. If this ParameterGUI has been initialized, the
   * PropertyChangeListener is added to the entrywidget as well.
   *
   * @param prop The property to listen for.
   * @param pcl The property change listener to be added.
   */
  public void addPropertyChangeListener( 
    String prop, PropertyChangeListener pcl ) {
    addPCLToVector( prop, pcl );
    topPCS.addPropertyChangeListener( prop, pcl );

    if( this.initialized ) {
      entrywidget.addPropertyChangeListener( prop, pcl );
    }
  }

  /**
   * Clones this ParameterGUI.
   */
  public Object clone(  ) {
    return this.clone(  );
    /*ParameterGUI pg=new ParameterGUI(this.name,this.value,this.valid);
       pg.setDrawValid(this.getDrawValid());
       pg.initialized=false;
       return pg;*/
  }

  /**
   * Called when this ParameterGUIs property changes.  Sets this ParameterGUI
   * invalid if it is listening to property changes; does nothing otherwise.
   *
   * @param ev The triggering PropertyChangeEvent.
   */
  public void propertyChange( PropertyChangeEvent ev ) {
    if( this.ignore_prop_change ) {
      return;
    }
    this.setValid( false );
    topPCS.firePropertyChange( ev );
  }

  /**
   * Removes a property change listener from this ParameterGUIs inner Vector of
   * listeners, and from the entrywidget if this ParameterGUI has been
   * initialized. If this ParameterGUI has been initialized, the
   * PropertyChangeListener is removed from the entrywidget as well.
   *
   * @param pcl The property change listener to be removed.
   */
  public void removePropertyChangeListener( PropertyChangeListener pcl ) {
    removePCLFromVector( pcl );
    topPCS.removePropertyChangeListener( pcl );

    if( this.initialized ) {
      entrywidget.removePropertyChangeListener( pcl );
    }
  }

  /**
   * @return A String representation of this ParameterGUI consisting of its
   *         type, name, valid, and validity.
   */
  public String toString(  ) {
    String rs = this.getType(  ) + ": \"" + this.getName(  ) + "\" " +
      this.getValue(  ) + " " + this.getValid(  );

    return rs;
  }

  /**
   * Initializes the GUI for this ParameterGUI.  This calls addPCLtoWidget to
   * add any pre-existing PropertyChangeListeners to the (now) existing
   * entrywidget.  This also sets initialized to true.  Child classes MUST
   * call  this unless they plan on building the entire GUI from scratch (this
   * is not  recommended - if you have a complex entrywidget, such as
   * BrowsePG's entrywidget, put it inside a JPanel.  Calling initGUI() will
   * then create the entire GUI panel correctly).
   */
  protected final void initGUI(  ) {
    this.initialized = true;

    // create the label
    if( this.label == null ) {
      this.label = new JLabel(  );
    }
    label.setText( "  " + this.getName(  ) );

    // create the checkbox
    if( this.validcheck == null ) {
      this.validcheck = new JCheckBox( "" );
    }
    this.validcheck.setSelected( this.getValid(  ) );
    this.validcheck.setEnabled( false );
    this.validcheck.setVisible( this.getDrawValid(  ) );

    // put the gui together
    this.packupGUI(  );
    addPCLtoWidget(  );
    entrywidget.addPropertyChangeListener( IParameter.VALUE, this );
    setEnabled( this.getEnabled(  ) );
  }

  /**
   * Method to pack up everything in the frame.
   */
  protected void packupGUI(  ) {
    if( 
      ( this.getLabel(  ) != null ) && ( this.getEntryWidget(  ) != null ) &&
        ( this.validcheck != null ) ) {
      this.guipanel = new JPanel(  );
      this.guipanel.setLayout( new BorderLayout(  ) );

      JPanel innerpanel = new JPanel( new GridLayout( 1, 2 ) );
      innerpanel.add( this.getLabel(  ) );
      innerpanel.add( this.getEntryWidget(  ) );

      JPanel checkpanel = new JPanel( new GridLayout( 1, 1 ) );
      checkpanel.add( this.validcheck );
      this.guipanel.add( innerpanel, BorderLayout.CENTER );
      this.guipanel.add( checkpanel, BorderLayout.EAST );
    } else {
      System.err.println( 
        "cannot construct GUI component of " + this.getType(  ) + " " +
        this.getName(  ) );
    }
  }

  /**
   * Shows the GUI.  If this inner GUI panel does not exist, this creates a
   * test JFrame.  Otherwise it does nothing.
   */
  protected void showGUIPanel(  ) {
    this.showGUIPanel( 0, 0 );
  }

  /**
   * Shows the GUI.  If this inner GUI panel does not exist, this creates a
   * test JFrame.  Otherwise it does nothing.
   *
   * @param x X location of the JFrame.
   * @param y Y location of the JFrame.
   */
  protected void showGUIPanel( int x, int y ) {
    if( this.getGUIPanel(  ) != null ) {
      JFrame mw = new JFrame( "Test Display of " + this.getType(  ) );
      mw.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      mw.getContentPane(  )
        .add( this.getGUIPanel(  ) );
      mw.pack(  );

      Rectangle pos = mw.getBounds(  );
      pos.setLocation( x, y );
      mw.setBounds( pos );
      mw.show(  );
    }
  }

  /**
   * Adds a PropertyChangeListener to the internal Vectors.
   *
   * @param pcl The PropertyChangeListener to remove.
   */
  private void addPCLToVector( PropertyChangeListener pcl ) {
    propListeners.addElement( pcl );
    nameList.addElement( null );
  }

  /**
   * Adds a PropertyChangeListener to the internal Vectors.
   *
   * @param prop The property to listen for.
   * @param pcl The PropertyChangeListener to remove.
   */
  private void addPCLToVector( String prop, PropertyChangeListener pcl ) {
    propListeners.addElement( pcl );
    nameList.addElement( prop );
  }

  /**
   * When this is called, all of the internal PropertyChangeListeners will be
   * added to the entrywidget.
   */
  private void addPCLtoWidget(  ) {
    String temp;
    PropertyChangeListener pcl;

    //add the property change listeners
    for( int i = 0; i < propListeners.size(  ); i++ ) {
      if( nameList.elementAt( i ) instanceof String ) {
        temp = ( String )nameList.elementAt( i );
      } else {
        temp = null;
      }
      pcl = ( PropertyChangeListener )propListeners.elementAt( i );

      if( temp != null ) {
        entrywidget.addPropertyChangeListener( temp, pcl );
      } else {
        entrywidget.addPropertyChangeListener( pcl );
      }
    }
  }

  /**
   * Removes a PropertyChangeListener from the internal Vectors.
   *
   * @param pcl The PropertyChangeListener to remove.
   */
  private void removePCLFromVector( PropertyChangeListener pcl ) {
    int nameIndex = propListeners.indexOf( pcl );
    propListeners.remove( pcl );

    if( nameIndex >= 0 ) {
      nameList.removeElementAt( nameIndex );
    }
  }

  /**
   * Utility method to centralize dealing with the checkbox.
   */
  private void updateDrawValid(  ) {
    if( !this.initialized ) {
      return;
    }

    if( this.validcheck == null ) {  // make the checkbox if it dne
      this.validcheck = new JCheckBox( "" );
    }
    this.validcheck.setSelected( this.getValid(  ) );
    this.validcheck.setEnabled( false );
    this.validcheck.setVisible( this.getDrawValid(  ) );
    this.setName( this.getName(  ) );
  }
}

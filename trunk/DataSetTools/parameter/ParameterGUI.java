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
 *  Revision 1.34  2003/11/23 01:18:02  bouzekc
 *  Refactored getPropertyChangeSupport() and setPropertyChangeSupport() into
 *  firePropertyChange(), as this is the functionality that derived classes
 *  really need.
 *
 *  Revision 1.33  2003/11/20 01:43:50  bouzekc
 *  Removed final keyword from propertyChange().
 *
 *  Revision 1.32  2003/11/20 01:21:51  bouzekc
 *  Made several methods final.
 *
 *  Revision 1.31  2003/11/19 04:05:27  bouzekc
 *  Added accessor and mutator methods and made all fields private.  This class
 *  is now a JavaBean.  Added code to clone() to copy all PropertyChangeListeners.
 *
 *  Revision 1.30  2003/11/18 03:11:54  bouzekc
 *  Now really does use Hashtable for internal PropertyChangeListeners.
 *
 *  Revision 1.29  2003/10/17 02:22:11  bouzekc
 *  Fixed javadoc errors.
 *
 *  Revision 1.28  2003/10/11 18:54:56  bouzekc
 *  Implemented clone() using reflection.
 *
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.*;

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
 * override propertyChange().  If it does  so, use the inner
 * PropertyChangeSupport to propagate events upward,  in addition to the code
 * needed for acquiring old and new values.
 */
public abstract class ParameterGUI implements IParameterGUI, PropertyChanger,
  PropertyChangeListener, java.io.Serializable {
  //~ Static fields/initializers ***********************************************

  private static String UNKNOWN_PROPERTY = "UNKNOWN_PROPERTY";

  //~ Instance fields **********************************************************

  // instance variables for IParameter
  private String name;
  private Object value;
  private boolean valid;
  private String type;

  // instance variables for IParameterGUI
  private JLabel label;
  private EntryWidget entryWidget;
  private JPanel gUIPanel;
  private boolean enabled;
  private boolean drawValid;
  private JCheckBox validCheck;

  // extra instance variables
  private boolean initialized;
  private boolean ignorePropertyChange;
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( 
      this );
  private Hashtable propListeners        = new Hashtable(  );

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
    setInitialized( false );
    setIgnorePropertyChange( false );
  }

  //~ Methods ******************************************************************

  /**
   * Specify if the valid checkbox will be drawn.
   *
   * @param draw boolean indicating whether or not to draw the checkbox.
   */
  public final void setDrawValid( boolean draw ) {
    this.drawValid = draw;
    this.updateDrawValid(  );
  }

  /**
   * Determine if the 'valid' checkbox will be drawn.
   *
   * @return boolean indicating whether or not the checkbox will be drawn.
   */
  public final boolean getDrawValid(  ) {
    return drawValid;
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

    if( getEntryWidget(  ) != null ) {
      getEntryWidget(  )
        .setEnabled( getEnabled(  ) );
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
    return entryWidget;
  }

  /**
   * @return The GUI panel upon which the entrywidget is drawn.
   */
  public final JPanel getGUIPanel(  ) {
    return gUIPanel;
  }

  /**
   * Mutator method to set whether this ParameterGUI will ignore
   * PropertyChanges.
   *
   * @param ignore boolean indicating whether to ignore property changes or
   *        not.
   */
  public final void setIgnorePropertyChange( boolean ignore ) {
    ignorePropertyChange = ignore;
  }

  /**
   * Accessor method to get whether this ParameterGUI will ignore
   * PropertyChanges.
   *
   * @return boolean indicating whether or not this ParameterGUI will ignore
   *         property changes.
   */
  public final boolean getIgnorePropertyChange(  ) {
    return ignorePropertyChange;
  }

  /**
   * Mutator method for setting the label of this ParameterGUI.
   *
   * @param label The new label to use.
   */
  public final void setLabel( JLabel label ) {
    this.label = label;
  }

  /**
   * Mutator method for setting the label of this ParameterGUI.
   *
   * @param text The new label text to use.
   */
  public final void setLabel( String text ) {
    setLabel( new JLabel( text ) );
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
   * @param name The new name.
   */
  public final void setName( String name ) {
    this.name = name;

    if( !getInitialized(  ) ) {
      return;
    }

    if( getLabel(  ) == null ) {
      setLabel( new JLabel(  ) );
    }
    getLabel(  )
      .setText( "  " + this.getName(  ) );
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
   * @param valid boolean indicating whether or not this ParameterGUI should be
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
   * Mutator method that directly sets the internal value variable to the
   * parameter passed in.  Derived classes should override this.
   *
   * @param val The new value to set.
   */
  public void setValue( Object val ) {
    value = val;
  }

  /**
   * Accessor method to get the raw internal value.  Derived classes should
   * override this.
   *
   * @return The raw internal value.
   */
  public Object getValue(  ) {
    return value;
  }

  /**
   * Adds the specified property change listener to the inner Vector of
   * listeners.   If this ParameterGUI has been initialized, the
   * PropertyChangeListener is added to the entrywidget as well.
   *
   * @param pcl The property change listener to be added.
   */
  public final void addPropertyChangeListener( PropertyChangeListener pcl ) {
    propListeners.put( pcl, UNKNOWN_PROPERTY );
    propertyChangeSupport.addPropertyChangeListener( pcl );

    if( getInitialized(  ) ) {
      getEntryWidget(  )
        .addPropertyChangeListener( pcl );
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
  public final void addPropertyChangeListener( 
    String prop, PropertyChangeListener pcl ) {
    propListeners.put( pcl, prop );
    propertyChangeSupport.addPropertyChangeListener( prop, pcl );

    if( getInitialized(  ) ) {
      getEntryWidget(  )
        .addPropertyChangeListener( prop, pcl );
    }
  }

  /**
   * Definition of the clone method.  Uses reflection so that derived classes
   * don't need to write their own clone methods, although they can if they
   * need to.
   */
  public Object clone(  ) {
    try {
      Class klass           = this.getClass(  );
      Constructor construct = klass.getConstructor( 
          new Class[]{ String.class, Object.class } );
      ParameterGUI pg       = ( ParameterGUI )construct.newInstance( 
          new Object[]{ null, null } );
      pg.setName( new String( this.getName(  ) ) );
      pg.setValue( this.getValue(  ) );
      pg.setDrawValid( this.getDrawValid(  ) );
      pg.setValid( this.getValid(  ) );

      if( this.getInitialized(  ) ) {
        pg.initGUI( null );
      }

      if( propListeners != null ) {
        Enumeration e              = propListeners.keys(  );
        PropertyChangeListener pcl = null;
        String propertyName        = null;

        while( e.hasMoreElements(  ) ) {
          pcl            = ( PropertyChangeListener )e.nextElement(  );
          propertyName   = ( String )propListeners.get( pcl );
          pg.addPropertyChangeListener( propertyName, pcl );
        }
      }

      return pg;
    } catch( InstantiationException e ) {
      throw new InstantiationError( e.getMessage(  ) );
    } catch( IllegalAccessException e ) {
      throw new IllegalAccessError( e.getMessage(  ) );
    } catch( NoSuchMethodException e ) {
      throw new NoSuchMethodError( e.getMessage(  ) );
    } catch( InvocationTargetException e ) {
      throw new RuntimeException( e.getTargetException(  ).getMessage(  ) );
    }
  }

  /**
   * Called when this ParameterGUIs property changes.  Sets this ParameterGUI
   * invalid if it is listening to property changes; does nothing otherwise.
   *
   * @param ev The triggering PropertyChangeEvent.
   */
  public void propertyChange( PropertyChangeEvent ev ) {
    if( getIgnorePropertyChange(  ) ) {
      return;
    }
    this.setValid( false );
    propertyChangeSupport.firePropertyChange( ev );
  }

  /**
   * Removes a property change listener from this ParameterGUIs inner Vector of
   * listeners, and from the entrywidget if this ParameterGUI has been
   * initialized. If this ParameterGUI has been initialized, the
   * PropertyChangeListener is removed from the entrywidget as well.
   *
   * @param pcl The property change listener to be removed.
   */
  public final void removePropertyChangeListener( PropertyChangeListener pcl ) {
    propListeners.remove( pcl );
    propertyChangeSupport.removePropertyChangeListener( pcl );

    if( getInitialized(  ) ) {
      getEntryWidget(  )
        .removePropertyChangeListener( pcl );
    }
  }

  /**
   * @return A String representation of this ParameterGUI consisting of its
   *         type, name, valid, and validity.
   */
  public final String toString(  ) {
    String rs = this.getType(  ) + ": \"" + this.getName(  ) + "\" " +
      this.getValue(  ) + " " + this.getValid(  );

    return rs;
  }

  /**
   * Accessor method for child classes to set the EntryWidget.
   *
   * @param wijit The new EntryWidget to use.
   */
  protected final void setEntryWidget( EntryWidget wijit ) {
    entryWidget = wijit;
  }

  /**
   * Accessor method for child classes to set the GUI panel.
   *
   * @param gPanel The new GUI panel to use.
   */
  protected final void setGUIPanel( JPanel gPanel ) {
    this.gUIPanel = gPanel;
  }

  /**
   * Mutator method for child classes to set the initialized state.
   *
   * @param init True if this ParameterGUI has been initialized.
   */
  protected final void setInitialized( boolean init ) {
    this.initialized = init;
  }

  /**
   * Accessor method for child classes to get the initialized state.
   *
   * @return True if this ParameterGUI has been initialized.
   */
  protected final boolean getInitialized(  ) {
    return this.initialized;
  }

  /**
   * Mutator method for child classes to set the propertyListeners..
   *
   * @param pclTable The new PropertyChangeListeners Hashtable.
   */
  protected final void setPropListeners( Hashtable pclTable ) {
    this.propListeners = pclTable;
  }

  /**
   * Accessor method for child classes to get the propListeners.
   *
   * @return The PropertyChangeListeners Hashtable.
   */
  protected final Hashtable getPropListeners(  ) {
    return this.propListeners;
  }

  /**
   * Mutator method for derived classes to set the type.
   *
   * @param type The new type.
   */
  protected final void setType( String type ) {
    this.type = type;
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
    setInitialized( true );

    // create the label
    if( getLabel(  ) == null ) {
      setLabel( new JLabel(  ) );
    }
    getLabel(  )
      .setText( "  " + this.getName(  ) );

    // create the checkbox
    if( this.validCheck == null ) {
      this.validCheck = new JCheckBox( "" );
    }
    this.validCheck.setSelected( this.getValid(  ) );
    this.validCheck.setEnabled( false );
    this.validCheck.setVisible( this.getDrawValid(  ) );

    // put the gui together
    this.packupGUI(  );
    addPCLtoWidget(  );
    getEntryWidget(  )
      .addPropertyChangeListener( IParameter.VALUE, this );
    setEnabled( this.getEnabled(  ) );
  }

  /**
   * Method to pack up everything in the frame.
   */
  protected final void packupGUI(  ) {
    if( 
      ( this.getLabel(  ) != null ) && ( this.getEntryWidget(  ) != null ) &&
        ( this.validCheck != null ) ) {
      setGUIPanel( new JPanel(  ) );

      JPanel gPanel = getGUIPanel(  );
      gPanel.setLayout( new BorderLayout(  ) );

      JPanel innerpanel = new JPanel( new GridLayout( 1, 2 ) );
      innerpanel.add( this.getLabel(  ) );
      innerpanel.add( this.getEntryWidget(  ) );

      JPanel checkpanel = new JPanel( new GridLayout( 1, 1 ) );
      checkpanel.add( this.validCheck );
      gPanel.add( innerpanel, BorderLayout.CENTER );
      gPanel.add( checkpanel, BorderLayout.EAST );
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
  protected final void showGUIPanel(  ) {
    this.showGUIPanel( 0, 0 );
  }

  /**
   * Shows the GUI.  If this inner GUI panel does not exist, this creates a
   * test JFrame.  Otherwise it does nothing.
   *
   * @param x X location of the JFrame.
   * @param y Y location of the JFrame.
   */
  protected final void showGUIPanel( int x, int y ) {
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
   * Method to return an Enumeration of the PropertyChangeListeners for this
   * ParameterGUI.
   *
   * @return The Enumeration of PropertyChangeListeners.
   */
  protected Enumeration getPropertyChangeListeners(  ) {
    return propListeners.keys(  );
  }

  /**
   * Utility method to fire PropertyChangeEvents.
   *
   * @param event The event to fire.
   */
  protected void firePropertyChange( PropertyChangeEvent event ) {
    propertyChangeSupport.firePropertyChange( event );
  }

  /**
   * When this is called, all of the internal PropertyChangeListeners will be
   * added to the entrywidget.
   */
  private void addPCLtoWidget(  ) {
    String temp;
    PropertyChangeListener pcl;
    String propertyName;

    //add the property change listeners
    Enumeration e = propListeners.keys(  );

    while( e.hasMoreElements(  ) ) {
      pcl            = ( PropertyChangeListener )e.nextElement(  );
      propertyName   = ( String )propListeners.get( pcl );

      if( propertyName == UNKNOWN_PROPERTY ) {
        getEntryWidget(  )
          .addPropertyChangeListener( pcl );
      } else {
        getEntryWidget(  )
          .addPropertyChangeListener( propertyName, pcl );
      }
    }
  }

  /**
   * Utility method to centralize dealing with the checkbox.
   */
  private void updateDrawValid(  ) {
    if( !getInitialized(  ) ) {
      return;
    }

    if( this.validCheck == null ) {  // make the checkbox if it dne
      this.validCheck = new JCheckBox( "" );
    }
    this.validCheck.setSelected( this.getValid(  ) );
    this.validCheck.setEnabled( false );
    this.validCheck.setVisible( this.getDrawValid(  ) );
    this.setName( this.getName(  ) );
  }
}

/*
 * File:  EntryWidget.java
 *
 * Copyright (C) 2003, Chris M. Bouzek
 *
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
 *           Chris M. Bouzek <coldfusion78@yahoo.com>
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * $Log$
 * Revision 1.7  2003/10/20 22:43:08  bouzekc
 * Now propagates the pressed key out as a PropertyChangeEvent new value
 * when a key is pressed.
 *
 * Revision 1.6  2003/10/06 23:57:55  bouzekc
 * Now sends out the value of AbstractButton's ActionEvents.  In particular,
 * if a JCheckBox is clicked, its value is propagated upwards as a
 * PropertyChangeEvent.
 *
 * Revision 1.5  2003/08/26 18:23:36  bouzekc
 * Default layout is now X-axis BoxLayout.
 *
 * Revision 1.4  2003/08/26 17:58:14  bouzekc
 * Added call to this() in Components[] constructor.  Moved call to super()
 * into default constructor.
 *
 * Revision 1.3  2003/08/22 19:29:30  bouzekc
 * Added default constructor.
 *
 * Revision 1.2  2003/08/22 18:57:32  bouzekc
 * Added method to recursively enable/disable all Components contained within
 * this EntryWidget.  Modified ActionListener and KeyListener methods to more
 * accurately trigger PropertyChangeEvents.  Commented out main() method.
 *
 * Revision 1.1  2003/08/22 05:01:16  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.components.ParametersGUI;

import DataSetTools.parameter.IParameter;

import DataSetTools.util.PropertyChanger;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import java.awt.event.*;

import java.beans.*;

import java.util.*;

import javax.swing.*;


/**
 * This class defines a JPanel-derived GUI "widget" originally meant for use in
 * ParameterGUIs.  It has the ability to add PropertyChangeListeners (the
 * standard way to indicate a change in a ParameterGUIs property), although in
 * order to accomodate all the possible ways of changing values, it uses
 * KeyListeners, ActionListeners, and PropertyChangeListeners.
 */
public class EntryWidget extends JPanel implements PropertyChanger,
  PropertyChangeListener, ActionListener, KeyListener {
  //~ Static fields/initializers ***********************************************

  public static final String NO_NAME = "UNKNOWN";

  //~ Instance fields **********************************************************

  private Vector innerComponents         = new Vector(  );
  private Hashtable listeners            = new Hashtable(  );
  private PropertyChangeSupport propBind = new PropertyChangeSupport( this );

  //~ Constructors *************************************************************

  /**
   * Default constructor.  Simply calls super(  ), and is here to facilitate
   * easier layout of complex EntryWidgets by allowing you to set Layout
   * Managers and the like before adding components.
   */
  public EntryWidget(  ) {
    super(  );
    super.setLayout( new BoxLayout( this, BoxLayout.X_AXIS  ) );
  }

  /**
   * Creates an EntryWidget that consists of a single Component (such as a
   * JTextField).
   *
   * @param comp The Component to use for this entrywidget.
   */
  public EntryWidget( Component comp ) {
    this(  );
    this.add( comp );
  }

  /**
   * Constructor for creating more complex EntryWidgets.   The passed-in
   * components will be added in the order that they exist in  the array.
   *
   * @param comps The components to add.
   */
  public EntryWidget( Component[] comps ) {
    this(  );

    for( int i = 0; i < comps.length; i++ ) {
      innerComponents.add( comps[i] );
      this.add( comps[i] );
    }
  }

  /**
   * Constructor meant for adding PropertyChangeListeners at creation time.
   * This constructor is meant for a single Component (i.e. a JTextField).
   *
   * @param comp The Component to use.
   * @param pcls The Hashtable of PropertyChangeListeners to add.  The keys for
   *        this Hashtable are the listeners themselves, and the values are
   *        the property names.  If you do not wish to use a property name
   *        when constructing the Hashtable, use EntryWidget.NO_NAME.
   */
  public EntryWidget( Component comp, Hashtable pcls ) {
    this( comp );
    listeners = pcls;
    addPreviousListeners( comp );
  }

  /**
   * Constructor meant for adding PropertyChangeListeners at creation time.
   * This constructor is meant for complex EntryWidgets utilizing several
   * Components. The passed-in components will be added in the order that they
   * exist in  the array.
   *
   * @param comps The components to add.
   * @param pcls The Hashtable of PropertyChangeListeners to add.  The keys for
   *        this Hashtable are the listeners themselves, and the values are
   *        the property names.  If you do not wish to use a property name
   *        when constructing the Hashtable, use EntryWidget.NO_NAME.
   */
  public EntryWidget( Component[] comps, Hashtable pcls ) {
    this( comps );
    listeners = pcls;

    for( int i = 0; i < comps.length; i++ ) {
      addPreviousListeners( ( Component )comps[i] );
    }
  }

  //~ Methods ******************************************************************

  /**
   * Overridden to enable/disable all the components within the EntryWidget.
   * Uses recursion.
   */
  public void setEnabled( boolean enable ) {
    recursivelySetEnabled( this, enable );
  }

  /**
   * ActionListener implementation.
   *
   * @param evt The triggering ActionEvent.
   */
  public void actionPerformed( ActionEvent evt ) {
    Object source = evt.getSource(  );
    Object oldVal = "old";
    Object newVal = "new";

    if( source instanceof AbstractButton ) {
      if( source instanceof JCheckBox ) {
        newVal = new Boolean( ( ( JCheckBox )source ).isSelected(  ) );
        oldVal = new Boolean( !( ( JCheckBox )source ).isSelected(  ) );
      } else {
        newVal = ( ( AbstractButton )source ).getText(  );
      }
    }

    propertyChange( 
      new PropertyChangeEvent( this, IParameter.VALUE, oldVal, newVal ) );
  }

  /**
   * Overridden from JPanel to take care of adding the PropertyChangeListeners.
   *
   * @param comp The Component to add.
   */
  public Component add( Component comp ) {
    addListenersAndComps( comp );

    return super.add( comp );
  }

  /**
   * Overridden from JPanel to take care of adding the PropertyChangeListeners.
   *
   * @param name The Component name.
   * @param comp The Component to add.
   */
  public Component add( String name, Component comp ) {
    addListenersAndComps( comp );

    return super.add( name, comp );
  }

  /**
   * Overridden from JPanel to take care of adding the PropertyChangeListeners.
   *
   * @param comp The Component to add.
   * @param index The position at which to insert the component.
   */
  public Component add( Component comp, int index ) {
    addListenersAndComps( comp );

    return super.add( comp, index );
  }

  /**
   * Overridden from JPanel to take care of adding the PropertyChangeListeners.
   *
   * @param comp The Component to add.
   * @param constraints Object expressing layout constraints for this
   *        Component.
   */
  public void add( Component comp, Object constraints ) {
    addListenersAndComps( comp );
    super.add( comp, constraints );
  }

  /**
   * Overridden from JPanel to take care of adding the PropertyChangeListeners.
   *
   * @param comp The Component to add.
   * @param constraints Object expressing layout constraints for this
   *        Component.
   * @param index The position at which to insert the component.
   */
  public void add( Component comp, Object constraints, int index ) {
    addListenersAndComps( comp );
    super.add( comp, constraints, index );
  }

  /**
   * Used to add PropertyChangeListeners to this EntryWidget.  Adds the given
   * listener to all Components in this EntryWidget.
   *
   * @param pcl The PropertyChangeListener to add.
   */
  public void addPropertyChangeListener( PropertyChangeListener pcl ) {
    Component temp = null;

    for( int i = 0; i < innerComponents.size(  ); i++ ) {
      temp = ( Component )innerComponents.get( i );

      recursivelyAddListeners( pcl, temp, NO_NAME );
    }

    if( propBind != null ) {
      propBind.addPropertyChangeListener( pcl );
    }

    listeners.put( pcl, NO_NAME );
  }

  /**
   * Used to add PropertyChangeListeners to this EntryWidget.  Adds the given
   * listener to all Components in this EntryWidget.
   *
   * @param name The name of the Property to add.
   * @param pcl The PropertyChangeListener to add.
   */
  public void addPropertyChangeListener( 
    String name, PropertyChangeListener pcl ) {
    Component temp = null;

    for( int i = 0; i < innerComponents.size(  ); i++ ) {
      temp = ( Component )innerComponents.get( i );

      recursivelyAddListeners( pcl, temp, name );
    }

    if( propBind != null ) {
      propBind.addPropertyChangeListener( name, pcl );
    }

    listeners.put( pcl, name );
  }

  /**
   * Testbed.
   */

  /*public static void main( String[] args ) {
     EntryWidget ew = new EntryWidget( new JTextField( "TEST" ) );
     //ew.addPropertyChangeListener(
     //new Operators.TOF_SCD.MyPropertyChangeListener(  ) );
     JFrame frame = new JFrame( "TEST" );
     frame.getContentPane(  )
          .add( ew );
     frame.setSize( new Dimension( 640, 480 ) );
     frame.setVisible( true );
     }*/

  /**
   * Triggered when a key is pressed.
   *
   * @param e The triggering key event.
   */
  public void keyPressed( KeyEvent e ) {}

  /**
   * Triggered when a key is release.
   *
   * @param e The triggering key event.
   */
  public void keyReleased( KeyEvent e ) {}

  /**
   * Triggered when a key is typed.
   *
   * @param e The triggering key event.
   */
  public void keyTyped( KeyEvent e ) {
    propertyChange( 
      new PropertyChangeEvent( this, IParameter.VALUE, "old",  
                               String.valueOf( e.getKeyChar(  ) ) ) );
  }

  /**
   * Fires off property change events whenever one of the internal Components
   * changes.
   *
   * @param e The triggering PropertyChangeEvent.
   */
  public void propertyChange( PropertyChangeEvent e ) {
    propBind.firePropertyChange( e );
  }

  /**
   * Used to remove PropertyChangeListeners from this EntryWidget.  Removes the
   * given listener from all JComponents in this EntryWidget.
   *
   * @param pcl The PropertyChangeListener to remove.
   */
  public void removePropertyChangeListener( PropertyChangeListener pcl ) {
    for( int i = 0; i < innerComponents.size(  ); i++ ) {
      ( ( Component )innerComponents.get( i ) ).removePropertyChangeListener( 
        pcl );
    }

    if( propBind != null ) {
      propBind.addPropertyChangeListener( pcl );
    }

    listeners.remove( pcl );
  }

  /**
   * Adds the Eventlistener to the specified component.  Handles KeyListeners,
   * ActionListeners, and PropertyChangeListeners.  By doing this, we hope to
   * snare all the possible property changing events.
   *
   * @param evl The EventListener to use.
   * @param comp The component to register this class as a listener for.
   * @param name (Optional) The name of the property to listen for in the case
   *        of a PropertyChangeListener.  If no property name exists, or evl
   *        is not a PropertyChangeListener, this should be set to NO_NAME.
   */
  private void addListener( EventListener evl, Component comp, String name ) {
    if( evl instanceof ActionListener ) {
      if( comp instanceof JTextField ) {
        ( ( JTextField )comp ).addActionListener( ( ActionListener )evl );
      } else if( comp instanceof JComboBox ) {
        ( ( JComboBox )comp ).addActionListener( ( ActionListener )evl );
      } else if( comp instanceof AbstractButton ) {
        ( ( AbstractButton )comp ).addActionListener( ( ActionListener )evl );
      } else if( comp instanceof JFileChooser ) {
        ( ( JFileChooser )comp ).addActionListener( ( ActionListener )evl );
      }
    }

    if( evl instanceof KeyListener ) {
      //add a keylistener on
      if( comp instanceof JTextField || comp instanceof JTextArea ) {
        comp.addKeyListener( ( KeyListener )evl );
      }
    }

    if( evl instanceof PropertyChangeListener ) {
      if( name == NO_NAME ) {
        comp.addPropertyChangeListener( ( PropertyChangeListener )evl );
      } else {
        comp.addPropertyChangeListener( name, ( PropertyChangeListener )evl );
      }
    }
  }

  /**
   * Utility method to facilitate overriding the numerous add(...) methods in
   * JPanel.
   *
   * @param comp The component to use.
   */
  private void addListenersAndComps( Component comp ) {
    if( innerComponents == null ) {
      innerComponents = new Vector(  );
    }

    innerComponents.add( comp );

    recursivelyAddListeners( this, comp, IParameter.VALUE );
    addPreviousListeners( comp );
  }

  /**
   * Adds any previously existing PropertyChangeListeners to the Component.
   *
   * @param comp The Component to add the previously existing Listeners to.
   */
  private void addPreviousListeners( Component comp ) {
    String val                 = null;
    PropertyChangeListener key = null;

    Enumeration keys = listeners.keys(  );

    //add any previously existing PropertyChangeListeners
    while( keys.hasMoreElements(  ) ) {
      key   = ( PropertyChangeListener )keys.nextElement(  );
      val   = listeners.get( key )
                       .toString(  );
      recursivelyAddListeners( key, comp, val );
    }
  }

  /**
   * Adds the listener to all Components inside the Component passed to it.
   * This method recurses through the Containers within the Component.
   *
   * @param evl The EventListener to add.
   * @param comp The Component to add the EventListener to.
   * @param name (Optional) The name of the property to listen for in the case
   *        of a PropertyChangeListener.  If no property name exists, or evl
   *        is not a PropertyChangeListener, this should be set to NO_NAME.
   */
  private void recursivelyAddListeners( 
    EventListener evl, Component comp, String name ) {
    if( !( comp instanceof Container ) ) {
      //base case
      addListener( evl, comp, name );

      return;
    }

    //add listeners to the container as well
    addListener( evl, comp, name );

    for( int i = 0; i < ( ( Container )comp ).getComponents(  ).length; i++ ) {
      recursivelyAddListeners( 
        evl, ( ( Container )comp ).getComponents(  )[i], name );
    }
  }

  /**
   * Used by setEnabled( boolean ) to enable/disable all components within this
   * EntryWidget.  Recursive method.
   *
   * @param comp The Component to enable/disable Components in.
   * @param enable Whether to enable/disable.
   */
  private void recursivelySetEnabled( Component comp, boolean enable ) {
    if( !( comp instanceof Container ) ) {
      //base case
      comp.setEnabled( enable );

      return;
    }

    //enable/disable the Container as well.  To avoid infinite recursion, we
    //don't want to call this setEnabled on the EntryWidget again.
    if( !( comp instanceof EntryWidget ) ) {
      comp.setEnabled( enable );
    } else {
      super.setEnabled( enable );
    }

    for( int i = 0; i < ( ( Container )comp ).getComponents(  ).length; i++ ) {
      recursivelySetEnabled( 
        ( ( Container )comp ).getComponents(  )[i], enable );
    }
  }
}

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

import DataSetTools.components.ParametersGUI.ArrayEntryJPanel;

import DataSetTools.util.PropertyChanger;
import DataSetTools.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.*;

import javax.swing.*;


/**
 *   This parameterGUI is the parent class of other parameterGUI's whose values
 *   are Vectors with a common Object data type for each elements.  This GUI is
 *   best for a medium sized list.  The list appears in a list box where the
 *   values can be edited deleted, and/or rearranged.
 *
 *   A vector of choicelist should go through this constructor
 */
public abstract class VectorPG extends ParameterGUI
  implements PropertyChangeListener, ActionListener, ParamUsesString,
    PropertyChanger {
  private String typeName;
  private ParameterGUI param;
  private PropertyChangeSupport pcs;
  private ArrayEntryJPanel GUI;
  private JButton vectorButton;
  private JPanel buttonHolder;
  private Vector listeners    = new Vector(  );
  private JDialog entryDialog;
  private JFrame entryFrame;

  /**
   *  Constructor
   *  @param   param   A ParameterGUI that determines the data type of the
   *                   elements of the resultant Vector.
   *  @param  Prompt   the prompt string that appears on the  GUI( a button)
   *                   and the resultant JFrame when the button is pressed
   *
   *  The ParameterGUI is just a button in a JPanel.  When the button is
   *  pressed a more complicated JFrame is created with the list box and
   *  editing buttons.
   */
  public VectorPG( ParameterGUI param, String Prompt ) {
    super(  );
    typeName     = param.getType(  ) + "Array";
    this.param   = param;
    setName( Prompt );
    pcs            = new PropertyChangeSupport( this );
    GUI            = null;
    vectorButton   = null;
    buttonHolder   = null;
  }

  /**
   *  Initializes this VectorPG.
   */
  public void init(  ) {
    GUI = new ArrayEntryJPanel( param );
    GUI.addPropertyChangeListener( this );
    entrywidget = vectorButton;
    GUI.setValue( value );
    vectorButton   = new JButton( param.getName(  ) );
    buttonHolder   = new JPanel( new GridLayout( 1, 1 ) );
    buttonHolder.add( vectorButton );
    vectorButton.addActionListener( this );
  }

  /**
   *
   * @return  The entrywidget associated with this ParameterGUI.
   */
  public JComponent getEntryWidget(  ) {
    return vectorButton;
  }

  /**
   *  Adds an ActionListener to the Vector of ActionListeners held by this
   *  ParameterGUI.
   *
   *  @param  listener         The ActionListener to add.
   */
  public void addActionListener( ActionListener listener ) {
    listeners.addElement( listener );
  }

  /**
   *  Removes an ActionListener from the Vector of ActionListeners held
   *  by this ParameterGUI.
   *
   *  @param  listener         The ActionListener to remove.
   */
  public void removeActionListener( ActionListener listener ) {
    listeners.remove( listener );
  }

  /**
   *  Fires ActionEvents to the ActionListeners in this ParameterGUIs Vector of
   *  ActionListeners.
   */
  public void notifyActionListeners( String command ) {
    for( int i = 0; i < listeners.size(  ); i++ ) {
      ( ( ActionListener )listeners.elementAt( i ) ).actionPerformed( 
        new ActionEvent( this, ActionEvent.ACTION_PERFORMED, command ) );
    }
  }

  //*********** PropertyChanger methods *********************************

  /**
   *    Adds property change listeners to listen for new Vector values
   */
  public void addPropertyChangeListener( PropertyChangeListener listener ) {
    pcs.addPropertyChangeListener( listener );
    GUI.addPropertyChangeListener( listener );
  }

  public void addPropertyChangeListener( 
    String property, PropertyChangeListener listener ) {
    pcs.addPropertyChangeListener( property, listener );
    GUI.addPropertyChangeListener( property, listener );
  }

  /**
   *    Removes the property change listener
   */
  public void removePropertyChangeListener( PropertyChangeListener listener ) {
    pcs.removePropertyChangeListener( listener );
    GUI.removePropertyChangeListener( listener );
  }

  //*********** end PropertyChanger methods *********************************
  //*********** ParamUsesString methods *********************************
  public String getStringValue(  ) {
    return ArrayPG.ArraytoString( ( Vector )value );
  }

  /**
   *   Sets the value and displays these values in the associated JList.
   *
   *  @param  value               The new value to set the VectorPG to.
   */
  public void setStringValue( String value ) {
    this.value = ArrayPG.StringtoArray( value );
    checkValue(  );
  }

  protected void checkValue(  ) {}

  /**
   *    Gets the value of the Vector
   */
  public Object getValue(  ) {
    return value;
  }

  /**
   *  Accessor method to set the enable state of this parameter.
   *
   *  @param  enable             Whether to set the parameter enabled or not.
   */
  public void setEnabled( boolean enable ) {
    enabled = enable;
  }

  /**
   *   Sets the value and displays these values in the associated JList.
   *
   *  @param  newVal               The new value to set the VectorPG to.
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
   *   Displays the JFrame with the list box containing the elements of the
   *   Vector.  If it is already being shown, this does nothing.
   */
  protected void showEntryPanel(  ) {
    if( entryFrame == null ) {
      this.makeEntryPanel(  );
    }

    if( !entryFrame.isShowing(  ) ) {
      //there must be a way to show this without remaking the GUI -
      //setVisible(true) does NOT work
      this.makeEntryPanel(  );
      entryFrame.show(  );
    } else {
      return;
    }
  }

  /**
   *  Creates the entry panel for this VectorPG.
   */
  protected void makeEntryPanel(  ) {
    entryFrame = new JFrame( param.getName(  ) + " List" );

    entryFrame.setSize( 500, 300 );

    //leave this commented code in here.  It is possible that ScriptOperator
    //needs it yet, and VectorPG has undergone major surgery between CVS
    //commits.  06/24/2003 - CMB
    //entryDialog = new JDialog( entryFrame, param.getName(  ), true );
    //entryDialog.setSize( 500, 300 );
    entryFrame.setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
    entryFrame.addWindowListener( new VectorPGWindowListener(  ) );

    //entryDialog.addWindowListener( new VectorPGWindowListener(  ) );
    //entryDialog.getContentPane(  ).setLayout( new GridLayout( 1, 1 ) );
    //entryDialog.getContentPane(  ).add( GUI );
    entryFrame.getContentPane(  ).add( GUI );
    GUI.addPropertyChangeListener( this );
  }

  //*********** ActionListener methods *********************************

  /**
   *  Called when the original button is pressed. It creates the JFrame that
   *  stores the list box and editing buttons, etc.
   */
  public void actionPerformed( ActionEvent evt ) {
    String command = evt.getActionCommand(  );

    if( command.equals( param.getName(  ) ) ) {
      ;
    }

    showEntryPanel(  );
  }

  //*********** end ActionListener methods *********************************

  /**
   *  The type name is the param's type name with the letters "Array" affixed to the end
   */
  public String getType(  ) {
    return typeName;
  }

  /**
   *  Returns a JPanel that holds a button.  When the button is pressed, a new
   *  JFrame with more options appears.
   */
  public JPanel getGUIPanel(  ) {
    return buttonHolder;
  }

  /**
   *  Initializes this VectorPG.
   *
   *  @param  V         The Vector to use when initializing this VectorPG.
   */
  public void init( Vector V ) {
    value = ( V );
    init(  );
  }

  /**
   *  Triggered when the "Done" button in the ArrayEntryJPanel is clicked.
   */
  public void propertyChange( PropertyChangeEvent evt ) {
    value = ( GUI.getValues(  ) );
    setValid( true );
    pcs.firePropertyChange( evt );
    entryFrame.setVisible( false );
  }

  /**
   *  Triggers a property change event when the window is closed, and gives us
   *  the values from the GUI.
   */
  private class VectorPGWindowListener extends WindowAdapter {
    //~ Methods ****************************************************************

    public void windowClosing( WindowEvent e ) {
      propertyChange( 
        new PropertyChangeEvent( 
          this, "Data Changed", value, GUI.getValues(  ) ) );
    }
  }
}

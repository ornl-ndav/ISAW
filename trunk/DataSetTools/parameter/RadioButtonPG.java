/*
 * File:  RadioButtonPG.java
 *
 * Copyright (C) 2003, Chris M. Bouzek
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
 * number DMR-0218882 and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.16  2003/08/28 02:28:11  bouzekc
 *  Removed setEnabled() method.
 *
 *  Revision 1.15  2003/08/28 02:01:58  bouzekc
 *  Modified to work with new ParameterGUI.
 *
 *  Revision 1.14  2003/08/25 19:39:43  bouzekc
 *  Fixed bug where getValue() did not return the value.
 *
 *  Revision 1.13  2003/08/22 20:31:39  bouzekc
 *  Modified to work with EntryWidget.
 *
 *  Revision 1.12  2003/08/15 23:50:05  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI
 *  classes.  Commented out testbed main().
 *
 *  Revision 1.11  2003/08/15 03:57:37  bouzekc
 *  Should now properly add PropertyChangeListeners before GUI is initialized.
 *
 *  Revision 1.10  2003/07/29 08:06:17  bouzekc
 *  Now fires off propertyChangeEvents when the radio buttons are
 *  clicked.  Now implements PropertyChanger.  Fixed bug in
 *  getStringValue().
 *
 *  Revision 1.9  2003/07/29 07:32:02  bouzekc
 *  Fixed bug in init().  Should now successfully show the choices,
 *  regardless of when the GUI is made.
 *
 *  Revision 1.8  2003/07/29 06:48:27  bouzekc
 *  getStringValue() now returns proper case.
 *
 *  Revision 1.7  2003/07/17 20:57:04  bouzekc
 *  setValue(int) now works if the GUI has not been created.
 *
 *  Revision 1.6  2003/07/17 20:37:04  bouzekc
 *  Now properly updates internal value.  The value can now be
 *  set by using the internal index.
 *
 *  Revision 1.5  2003/07/17 20:25:04  bouzekc
 *  Separated adding values to the inner choice list from
 *  adding values to the actual buttons.  Now requires a call
 *  to init() to actually create the GUI.  Added constructor to
 *  comply with ParameterClassLists's getInstance() method.
 *
 *  Revision 1.4  2003/07/17 18:55:59  bouzekc
 *  Single parameter constructor now no longer draws a "valid"
 *  checkbox.  Added a method to add a list of items.
 *
 *  Revision 1.3  2003/07/17 18:46:06  bouzekc
 *  Removed main()'s inner class.
 *
 *  Revision 1.2  2003/07/17 18:27:42  bouzekc
 *  No longer implements PropertyChanger, now correctly
 *  invalidates itself when a radio button is clicked, fixed
 *  bug in addItem().
 *
 *  Revision 1.1  2003/07/17 16:56:05  bouzekc
 *  Added to CVS.
 *
 *
 */
package DataSetTools.parameter;

import DataSetTools.components.ParametersGUI.*;

import DataSetTools.util.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.lang.Float;

import java.util.*;

import javax.swing.*;


/**
 * This is class is to deal with radio-button style (i.e. only one valid
 * choice) parameters.  It contains an inner Vector of JRadioButtons, as well
 * as a logical ButtonGroup to link them together.
 */
public class RadioButtonPG extends ParameterGUI implements ParamUsesString {
  //~ Static fields/initializers ***********************************************

  private static final String TYPE = "RadioButton";

  //~ Instance fields **********************************************************

  //the radioButtons and radioChoices are parallel Vectors...they must be added
  //to simultaneously!!
  private Vector radioButtons;
  private Vector radioChoices;
  private Vector extListeners      = null;
  private ButtonGroup radioGroup;

  //~ Constructors *************************************************************

  /**
   * Creates a new RadioButtonPG object without a drawn "valid" checkbox and an
   * initial state of valid = false.
   *
   * @param name The name of this ParameterGUI
   * @param Object val
   */
  public RadioButtonPG( String name, Object val ) {
    super( name, val );
    addItem( val.toString(  ) );
    setValue( val.toString(  ) );
    this.type = TYPE;
  }

  /**
   * Creates a new RadioButtonPG object.
   *
   * @param PGname The name of this parameterGUI.
   * @param valid Whether this parameterGUI should be initially valid or not.
   */
  public RadioButtonPG( String PGname, boolean valid ) {
    this( PGname, null, valid );
  }

  /**
   * Creates a new RadioButtonPG object.
   *
   * @param PGname The name of this parameterGUI.
   * @param val Either the Vector of (String) values that this RadioButtonPG
   *        should have, or a single choice to add.
   * @param valid Whether this parameterGUI should be initially valid or not.
   */
  public RadioButtonPG( String PGname, Object val, boolean valid ) {
    super( PGname, val, valid );

    if( val instanceof Vector ) {
      radioChoices = ( Vector )val;
    } else {
      radioChoices = new Vector(  );
      addItem( val.toString(  ) );
      setValue( val.toString(  ) );
    }

    this.type = TYPE;
  }

  //~ Methods ******************************************************************

  /**
   * Sets the value by using a String corresponding to a JRadioButtons label.
   *
   * @param newVal JRadioButton label to set the value to.
   */
  public void setStringValue( String newVal ) {
    this.setValue( newVal );
  }

  /**
   * Fast-access method to return the string value of this class.
   *
   * @return The String value associated with the selected radio button.
   */
  public String getStringValue(  ) {
    if( getValue(  ) == null ) {
      return null;
    }

    String tempVal = getValue(  )
                       .toString(  );

    if( tempVal == null ) {
      return null;
    }

    return tempVal;
  }

  /**
   * Overrides the default version of setValue to properly deal with radio
   * buttons.
   *
   * @param value The value to set the radio button to.  This must be a String
   *        name.  If the value does not exist, it will not be added.
   */
  public void setValue( Object sVal ) {
    String valName = sVal.toString(  );

    int radioIndex = getButtonIndex( valName );

    if( radioIndex < 0 ) {
      return;
    }

    if( this.initialized ) {
      setValue( radioIndex );
    } else {
      this.value = sVal;
    }
  }

  /**
   * Utility to set the value of this RadioButtonPG to a particular index.
   *
   * @param index The index to set the value to.
   */
  public void setValue( int index ) {
    if( initialized ) {
      JRadioButton selectedButton = ( ( JRadioButton )radioButtons.elementAt( 
          index ) );

      selectedButton.setSelected( true );
      value = selectedButton.getText(  );
    } else if( ( radioChoices != null ) && ( radioChoices.size(  ) > 0 ) ) {
      value = radioChoices.elementAt( index )
                          .toString(  );
    }
  }

  /**
   * Returns the text associated with the selected radio button.
   *
   * @return String label of the selected radio button.
   */
  public Object getValue(  ) {
    if( !initialized ) {
      return value;
    } else if( radioButtons != null ) {
      JRadioButton button;

      for( int i = 0; i < radioButtons.size(  ); i++ ) {
        button = ( JRadioButton )radioButtons.elementAt( i );

        if( button.isSelected(  ) ) {
          return button.getActionCommand(  );
        }
      }

      //couldn't find anything
      return null;
    } else {
      //couldn't get anything
      return null;
    }
  }

  /*
   * Main method for testing purposes.
   *
   * @param args Unused.
   */
  /*public static void main( String[] args ) {
     JFrame mainWindow = new JFrame(  );
     RadioButtonPG rpg = new RadioButtonPG( "Tester", true );
     rpg.addItem( "Choice 1" );
     rpg.addItem( "Choice 2" );
     rpg.addItem( "Choice 3" );
     rpg.addItem( "Choice 3" );
     //rpg.setValue( 2 );
     System.out.println( rpg.getValue(  ) );
     rpg.initGUI( null );
     //rpg.setValue( "Choice 5" );
     //rpg.setValue( "Choice 1" );
     System.out.println( rpg.getValue(  ) );
     rpg.showGUIPanel(  );
     }*/

  /**
   * Utility method to allow adding external action listeners to this PG's list
   * of ActionListeners.  This will not do much unless the PG's GUI has been
   * initialized.
   *
   * @param al The ActionListener to add.
   */
  public void addActionListener( ActionListener al ) {
    if( extListeners == null ) {
      extListeners = new Vector( 5, 2 );
    }

    extListeners.add( al );
  }

  /**
   * Adds a radio button to the list if and only if it is not already in the
   * list.  If this RadioButtonPG has been initialized (i.e. the GUI has been
   * created) then it is also added to the GUI.
   *
   * @param buttonName The name of the value to add.
   */
  public void addItem( String buttonName ) {
    if( radioChoices == null ) {
      radioChoices = new Vector(  );
    }

    //only add if the button does not exist
    int foundIndex = getButtonIndex( buttonName );

    if( foundIndex < 0 ) {
      //negative number, so it doesn't exist in the list
      radioChoices.add( buttonName );

      if( initialized ) {
        createGUIButton( buttonName );
      }
    }
  }

  /**
   * Method to add a list of items to this RadioButtonPG.
   *
   * @param itemList The Vector of items (Strings) to add.
   */
  public void addItems( Vector itemList ) {
    for( int i = 0; i < itemList.size(  ); i++ ) {
      addItem( itemList.elementAt( i ).toString(  ) );
    }
  }

  /**
   * Definition of the clone method.
   */
  public Object clone(  ) {
    RadioButtonPG pg = new RadioButtonPG( this.name, this.valid );

    pg.setDrawValid( this.getDrawValid(  ) );
    pg.initialized = false;

    return pg;
  }

  /**
   * Allows for initialization of the GUI after instantiation.
   *
   * @param init_values The values to initialize the GUI to.
   */
  public void initGUI( Vector init_values ) {
    if( this.initialized ) {
      return;  // don't initialize more than once
    }

    //one column grid.  Also, if we create the GUI stuff before we add items,
    //we can let addItem take care of adding it to the button group, entry
    //widget, and Vector of buttons.
    entrywidget = new EntryWidget(  );
    entrywidget.setLayout( new GridLayout( 0, 1 ) );
    radioButtons   = new Vector(  );
    radioGroup     = new ButtonGroup(  );

    if( init_values != null ) {
      for( int i = 0; i < init_values.size(  ); i++ ) {
        addItem( init_values.elementAt( i ).toString(  ) );
      }
    } else if( radioChoices != null ) {
      for( int k = 0; k < radioChoices.size(  ); k++ ) {
        createGUIButton( radioChoices.elementAt( k ).toString(  ) );
      }
    }

    entrywidget.addPropertyChangeListener( IParameter.VALUE, this );

    this.setEnabled( this.getEnabled(  ) );
    super.initGUI(  );
  }

  /**
   * Utility to get the button index.
   *
   * @param buttonName The label of the JRadioButton you wish to have the index
   *        for.
   * @param int corresponding to the array index of the button.  Returns -1 if
   *        it is not found.
   */
  private int getButtonIndex( String buttonName ) {
    if( radioChoices == null ) {
      return -1;
    }

    boolean found  = false;
    int foundIndex = -1;

    for( int i = 0; i < radioChoices.size(  ); i++ ) {
      if( radioChoices.elementAt( i )
                        .toString(  )
                        .equals( buttonName ) ) {
        found        = true;
        foundIndex   = i;

        break;
      }
    }

    return foundIndex;
  }

  /**
   * Used by addItem() and init() to create the GUI buttons when this PG is
   * shown.
   *
   * @param buttonName The name of the Button to add.
   */
  private void createGUIButton( String buttonName ) {
    JRadioButton tempButton = new JRadioButton( buttonName );

    radioButtons.add( tempButton );
    radioGroup.add( tempButton );
    entrywidget.add( tempButton );

    //add the external listeners
    if( extListeners != null ) {
      for( int i = 0; i < extListeners.size(  ); i++ ) {
        tempButton.addActionListener( 
          ( ( ActionListener )extListeners.elementAt( i ) ) );
      }
    }
  }
}

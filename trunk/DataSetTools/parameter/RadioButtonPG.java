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
public class RadioButtonPG extends ParameterGUI implements ParamUsesString,
  ActionListener {
  //~ Static fields/initializers ***********************************************

  private static final String TYPE = "RadioButton";

  //~ Instance fields **********************************************************

  //the radioButtons and radioChoices are parallel Vectors...they must be added
  //to simultaneously!!
  private Vector radioButtons;
  private Vector radioChoices;
  private ButtonGroup radioGroup;

  //~ Constructors *************************************************************

  /**
   * Creates a new RadioButtonPG object without a drawn "valid" checkbox and an
   * initial state of valid = false.
   *
   * @param PGname The name of this ParameterGUI
   */
  public RadioButtonPG( String PGname, Object newVal ) {
    this( PGname, newVal, false );
    this.setDrawValid( false );
  }

  /**
   * Creates a new RadioButtonPG object.
   *
   * @param PGname The name of this parameterGUI.
   * @param valid Whether this parameterGUI should be initially valid or not.
   */
  public RadioButtonPG( String PGname, boolean valid ) {
    this.setName( PGname );
    this.setEnabled( true );
    this.setValid( valid );
    this.setDrawValid( true );
    this.type                 = TYPE;
    this.initialized          = false;
    this.ignore_prop_change   = false;
  }

  /**
   * Creates a new RadioButtonPG object.
   *
   * @param PGname The name of this parameterGUI.
   * @param choices The Vector of (String) values that this RadioButtonPG
   *        should have.
   * @param valid Whether this parameterGUI should be initially valid or not.
   */
  public RadioButtonPG( String PGname, Object choices, boolean valid ) {
    this( PGname, valid );

    if( !( choices instanceof Vector ) ) {
      radioChoices = new Vector(  );
    } else {
      radioChoices = ( Vector )choices;
    }
  }

  //~ Methods ******************************************************************

  /**
   * Set the enabled state of the JRadioButtons.
   *
   * @param enableMe Whether to set this RadioButtonPG enabled.
   */
  public void setEnabled( boolean enableMe ) {
    enabled = enableMe;

    if( getEntryWidget(  ) != null ) {
      entrywidget.setEnabled( enabled );
    }
  }

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
    String tempVal = getValue(  )
                       .toString(  );

    if( tempVal == null ) {
      return null;
    }

    return tempVal.toLowerCase(  );
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
   * Utility to set the value of this RadioButtonPG to a particular index.  If
   * the GUI has not been initialized, this does nothing.
   *
   * @param index The index to set the value to.
   */
  public void setValue( int index ) {
    if( !initialized ) {
      return;
    }

    JRadioButton selectedButton = ( ( JRadioButton )radioButtons.elementAt( 
        index ) );

    selectedButton.setSelected( true );
    value = selectedButton.getText(  );
  }

  /**
   * Returns the text associated with the selected radio button.
   *
   * @return String label of the selected radio button.
   */
  public Object getValue(  ) {
    return value;
  }

  /**
   * This sets the inner state of the RadioButtonPG, so that getValue(  ) works
   * correctly.
   *
   * @param e The triggering ActionEvent.
   */
  public void actionPerformed( ActionEvent e ) {
    //System.out.println( "Previous value: " + getValue(  ) );
    this.value = e.getActionCommand(  );

    //System.out.println( "New value: " + getValue(  ) );
    this.setValid( false );
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
      radioChoices.add( buttonName );

      //negative number, so it doesn't exist in the list
      if( initialized ) {
        JRadioButton tempButton = new JRadioButton( buttonName );

        radioButtons.add( tempButton );
        radioGroup.add( tempButton );
        entrywidget.add( tempButton );
        tempButton.addActionListener( this );
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
   * Allows for initialization of the GUI after instantiation.
   *
   * @param init_values The values to initialize the GUI to.
   */
  public void init( Vector init_values ) {
    if( this.initialized ) {
      return;  // don't initialize more than once
    }

    //one column grid.  Also, if we create the GUI stuff before we add items,
    //we can let addItem take care of adding it to the button group, entry
    //widget, and Vector of buttons.
    entrywidget    = new JPanel( new GridLayout( 0, 1 ) );
    radioButtons   = new Vector(  );
    radioGroup     = new ButtonGroup(  );

    if( init_values != null ) {
      for( int i = 0; i < init_values.size(  ); i++ ) {
        addItem( init_values.elementAt( i ).toString(  ) );
      }
    }

    this.setEnabled( this.getEnabled(  ) );
    super.initGUI(  );
    this.initialized = true;
  }

  /**
   * Main method for testing purposes.
   *
   * @param args Unused.
   */
  public static void main( String[] args ) {
    JFrame mainWindow = new JFrame(  );
    RadioButtonPG rpg = new RadioButtonPG( "Tester", false );

    rpg.init( null );
    rpg.addItem( "Choice 1" );
    rpg.addItem( "Choice 2" );
    rpg.addItem( "Choice 3" );
    rpg.addItem( "Choice 3" );
    rpg.setValue( 2 );
    System.out.println( rpg.getValue(  ) );

    //rpg.setValue( "Choice 5" );
    rpg.setValue( "Choice 1" );
    System.out.println( rpg.getValue(  ) );
    rpg.showGUIPanel(  );
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
   * Utility to get the button index.
   *
   * @param buttonName The label of the JRadioButton you wish to have the index
   *        for.
   * @param int corresponding to the array index of the button.  Returns -1 if
   *        it is not found.
   */
  private int getButtonIndex( String buttonName ) {
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
}

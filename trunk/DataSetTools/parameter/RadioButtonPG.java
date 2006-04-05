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
 *  Revision 1.34  2006/04/05 18:05:45  hammonds
 *  Modified so that the RadioButtons will appear in the order in which they are given in the input array.  This has also been modified so that a default value can be specified by adding one item to the end of the input array that starts with $ and then repeats the specified default value.  For instance if the buttons were specified by ["Input 1", "Output 3", "Test 4"], and you want "Output 3" to be the default change the input array to ["Input 1", "Output 3", "Test 4", "$Output 3"]
 *
 *  Revision 1.33  2005/06/15 12:49:55  dennis
 *  getValue() method now checks for null before calling .toString()
 *
 *  Revision 1.32  2004/03/12 21:13:49  bouzekc
 *  Added clear() method.
 *
 *  Revision 1.31  2004/02/14 05:07:16  bouzekc
 *  Made private members default access to avoid a performance hit with
 *  synthetic accessor methods.
 *
 *  Revision 1.30  2003/12/15 02:44:08  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.29  2003/12/02 00:25:41  bouzekc
 *  Fixed bug that invalidated this ParameterGUI when initGUI() was called.
 *
 *  Revision 1.28  2003/12/01 19:03:19  bouzekc
 *  Fixed bug that discarded a set value if the GUI had not been created.
 *
 *  Revision 1.27  2003/11/25 03:02:32  bouzekc
 *  Now only tries to clone the Label if it has been initialized.
 *
 *  Revision 1.26  2003/11/23 02:12:18  bouzekc
 *  Now properly clones the label.
 *
 *  Revision 1.25  2003/11/23 01:18:46  bouzekc
 *  Now calls firePropertyChange() directly.
 *
 *  Revision 1.24  2003/11/19 04:06:53  bouzekc
 *  This class is now a JavaBean.  Added code to clone() to copy all
 *  PropertyChangeListeners.
 *
 *  Revision 1.23  2003/10/17 02:25:12  bouzekc
 *  Fixed javadoc errors.
 *
 *  Revision 1.22  2003/10/11 19:04:24  bouzekc
 *  Now implements clone() using reflection.
 *
 *  Revision 1.21  2003/09/16 22:50:08  bouzekc
 *  Now uses an internal ActionListener class to retrieve and set the value.
 *  This fixes a subtle bug related to ActionEvents being implicitly
 *  converted to PropertyChangeEvents by the getEntryWidget().  Removed the
 *  addActionListener() method and overrode propertyChange to achieve the same
 *  effect.  This ParameterGUI is now in compliance with the convention of
 *  using PropertyChangeListeners as the only external event interface.
 *
 *  Revision 1.20  2003/09/13 23:06:56  bouzekc
 *  Fixed bug where initGUI() did not set the button corresponding to
 *  getValue()'s value.
 *
 *  Revision 1.19  2003/09/13 22:11:10  bouzekc
 *  Now uses a Hashtable rather than parallel Vectors to store the JRadioButton
 *  names and buttons.  Removed a non-standard constructor.  setValue() now
 *  calls validateSelf().  Modified main method for updated testing.  Changed
 *  signature on addItem() to take an Object rather than a String.  Updated
 *  clone() to return the correct Object.  Removed setValue(int) and private
 *  getButtonIndex() method.
 *
 *  Revision 1.18  2003/09/09 23:06:30  bouzekc
 *  Implemented validateSelf().
 *
 *  Revision 1.17  2003/09/02 21:20:35  bouzekc
 *  Now handles null values in the constructor and setValue() better.
 *
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
 *  Modified to work with getEntryWidget().
 *
 *  Revision 1.12  2003/08/15 23:50:05  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI
 *  classes.  Commented out testbed main().
 *
 *  Revision 1.11  2003/08/15 03:57:37  bouzekc
 *  Should now properly add PropertyChangeListeners before GUI is getInitialized().
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
 */
package DataSetTools.parameter;

import DataSetTools.components.ParametersGUI.EntryWidget;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;


/**
 * Class is to deal with radio-button style parameters.  These are similar to
 * regular JRadioButtons (i.e. only one valid choice). It contains an inner
 * Vector of JRadioButtons, as well as a logical ButtonGroup to link them
 * together.
 */
public class RadioButtonPG extends ParameterGUI implements ParamUsesString {
  //~ Static fields/initializers ***********************************************

  private static final String TYPE = "RadioButton";

  //~ Instance fields **********************************************************

  String oldValue                           = null;
  private RadioButtonPGListener rpgListener = new RadioButtonPGListener(  );

  //the Hashtables keys are the radio button names, its values are the radio
  //buttons themselves.  If this RadioButtonPG is not getInitialized(), both the
  //keys and values are the button name, and when initGUI is called, new
  //RadioButtons are created.
  private Hashtable radioChoices = new Hashtable(  );
  private Vector ButtonNames = new Vector();
  private ButtonGroup radioGroup;
  private String defInitVal = new String();
  
  //~ Constructors *************************************************************

  /**
   * Creates a new RadioButtonPG object without a drawn "valid" checkbox and an
   * initial state of valid = false.  Note that any value sent in will be
   * coerced to a String and added to the list of choices.
   *
   * @param name The name of this ParameterGUI
   * @param val The new value to give this RadioButtonPG.
   */
  public RadioButtonPG( String name, Object val ) {
    super( name, val );
    addItem( val );
    setValue( val );
    this.setType( TYPE );
  }

  /**
   * Creates a new RadioButtonPG object.  Note that any value sent in will be
   * coerced to a String and added to the list of choices.
   *
   * @param PGname The name of this parameterGUI.
   * @param val Either the Vector of (String) values that this RadioButtonPG
   *        should have, or a single choice to add.
   * @param valid Whether this parameterGUI should be initially valid or not.
   */
  public RadioButtonPG( String PGname, Object val, boolean valid ) {
    super( PGname, val, valid );

    if( val instanceof Vector ) {
      Vector tempVec = ( Vector )val;
 //     ButtonNames = (Vector)((Vector)val).clone();
      if ( tempVec.elementAt(tempVec.size()-1).toString().startsWith("$")) {
    	  defInitVal = tempVec.elementAt(tempVec.size()-1).toString().substring(1);
      }
      else {
    	  defInitVal = tempVec.elementAt(0).toString();
      }
      for ( int ii = 0; ii< ((Vector)val).size(); ii++) {

    	  ButtonNames.add( ((Vector)val).elementAt(ii).toString()); 
      }

      addItems( tempVec );
      setValue( defInitVal );
    } else {
      addItem( val );
      setValue( val );
    }

    this.setType( TYPE );
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
   * @return The String value associated with the selected radio button.  If
   *         there is no selected button, this returns a blank String.
   */
  public String getStringValue(  ) {
    if( getValue(  ) == null ) {
      return null;
    }

    Object val = getValue(  );

    if( val != null ) {
      String tempVal = val.toString(  );

      if( tempVal == null ) {
        return null;
      }

      return tempVal;
    }

    //nothing to return, so send a blank String.
    return "";
  }

  /**
   * Overrides the default version of setValue to properly deal with radio
   * buttons.  If a null is sent in, this does nothing.
   *
   * @param val The value to set the radio button to.  This will be converted
   *        to a String if it is not one already by way of an Object's
   *        toString() method.  If the value does not exist, it will not be
   *        added.
   */
  public void setValue( Object val ) {
    String sVal = "";

    if( ( radioChoices != null ) && ( val != null ) ) {
      String valName = val.toString(  );

      if( radioChoices.containsKey( valName ) ) {
        //set the internal temporary value
        sVal = valName;

        //update the GUI if it exists
        if( this.getInitialized(  ) ) {
          JRadioButton selectedButton = ( JRadioButton )radioChoices.get(sVal);

          if( !selectedButton.isSelected(  ) ) {
            selectedButton.doClick(  );
          }

          sVal = selectedButton.getText(  );
        }
      }
    }

    super.setValue( sVal );
  }

  /**
   * Returns the text associated with the selected radio button.
   *
   * @return String label of the selected radio button.
   */
  public Object getValue(  ) {

    Object obj = super.getValue();

    if ( obj == null )
      return "";

    return obj.toString();
  }

  /*
   * Main method for testing purposes.
   *
   * @param args Unused.
   */
  public static void main( String[] args ) {
    RadioButtonPG rpg = new RadioButtonPG( "Tester", null, true );

    rpg.addItem( "Choice 1" );
    rpg.addItem( "Choice 2" );
    rpg.addItem( "Choice 3" );
    rpg.addItem( "Choice 3" );
    rpg.setValue( "Choice 3" );
    System.out.println( "MAIN " + rpg.getValue(  ) );
    rpg.initGUI( null );
    rpg.setValue( "Choice 5" );
    rpg.setValue( "Choice 2" );
    System.out.println( "MAIN INITGUI: " + rpg.getValue(  ) );
    rpg.showGUIPanel(  );
    rpg.clear(  );
    rpg.showGUIPanel(  );
  }

  /**
   * Adds a radio button to the list if and only if it is not already in the
   * list.  If this RadioButtonPG has been getInitialized() (i.e. the GUI has
   * been created) then it is also added to the GUI.  Note that the sent in
   * value is coerced to a String.
   *
   * @param val The name of the value to add.  If this is null, nothing is
   *        done.
   */
  public void addItem( Object val ) {
    if( val == null ) {
      return;
    }

    String buttonName = val.toString(  );

    //only add if the button does not exist
    boolean found = radioChoices.containsKey( buttonName );

    if( !found ) {
      //negative number, so it doesn't exist in the list
      ButtonNames.add(buttonName);
      radioChoices.put( buttonName, buttonName );
      if( getInitialized(  ) ) {
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
   * Used to clear out the PG.  This resets the GUI and clears the values.
   */
  public void clear(  ) {
    radioChoices = new Hashtable(  );
    setInitialized( false );
    getGUIPanel(  ).invalidate(  );
    initGUI( new Vector(  ) );
  }

  /**
   * Clones this RadioButtonPG.  Overwritten so that the choices are preserved.
   */
  public Object clone(  ) {
    try {
      Class klass           = this.getClass(  );
      Constructor construct = klass.getConstructor( 
          new Class[]{ String.class, Object.class } );
      RadioButtonPG pg      = ( RadioButtonPG )construct.newInstance( 
          new Object[]{ null, null } );

      pg.setName( new String( this.getName(  ) ) );
      pg.setValue( this.getValue(  ) );
      pg.setDrawValid( this.getDrawValid(  ) );
      pg.setValid( this.getValid(  ) );
      pg.defInitVal = new String(this.defInitVal);
      if( radioChoices != null ) {
        pg.radioChoices = ( Hashtable )radioChoices.clone(  );
      }

      if (ButtonNames != null ){
    	  pg.ButtonNames = (Vector)ButtonNames.clone();
      }
    	  
      if( this.getInitialized(  ) ) {
        pg.initGUI( null );
        pg.setLabel( new String( this.getLabel(  ).getText(  ) ) );
      }

      if( getPropListeners(  ) != null ) {
        java.util.Enumeration e    = getPropListeners(  ).keys(  );
        PropertyChangeListener pcl = null;
        String propertyName        = null;

        while( e.hasMoreElements(  ) ) {
          pcl            = ( PropertyChangeListener )e.nextElement(  );
          propertyName   = ( String )getPropListeners(  ).get( pcl );
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
   * Allows for initialization of the GUI after instantiation.
   *
   * @param init_values The values to initialize the GUI to.
   */
  public void initGUI( Vector init_values ) {
    if( this.getInitialized(  ) ) {
      return;  // don't initialize more than once
    }

    //one column grid.  Also, if we create the GUI stuff before we add items,
    //we can let addItem take care of adding it to the button group, entry
    //widget, and Vector of buttons.
    setEntryWidget( new EntryWidget(  ) );
    getEntryWidget(  ).setLayout( new GridLayout( 0, 1 ) );
    radioGroup = new ButtonGroup(  );

    //we will either discard all the old values and set to the new ones, or
    //create buttons for the new ones.
    if( init_values != null ) {
        if ( init_values.elementAt(init_values.size()-1).toString().startsWith("$")) {
      	  defInitVal = init_values.elementAt(init_values.size()-1).toString().substring(1);
        }
        else {
      	  defInitVal = init_values.elementAt(0).toString();
        }
      for( int i = 0; i < init_values.size(  ); i++ ) {
        addItem( init_values.elementAt( i ).toString(  ) );
      }
    } else if( radioChoices != null ) {
      Enumeration names = radioChoices.keys(  );
      if ( ButtonNames.elementAt(ButtonNames.size()-1).toString().startsWith("$")) {
      	  defInitVal = ButtonNames.elementAt(ButtonNames.size()-1).toString().substring(1);
      	  ButtonNames.remove(ButtonNames.size()-1);
      }
        else {
      	  defInitVal = ButtonNames.elementAt(0).toString();
        }

      
      for (int ii=0; ii<ButtonNames.size(); ii++){
      //while( names.hasMoreElements(  ) ) {
        createGUIButton( ButtonNames.elementAt(ii).toString() );
      }
    }
    setValue(defInitVal);
    super.initGUI(  );

    //ignore prop changes because we are about to change the value
    boolean ignore = getIgnorePropertyChange(  );

    setIgnorePropertyChange( true );
    setValue( defInitVal );
    setIgnorePropertyChange( ignore );
  }

  /**
   * Method to fire off the correct values when the property changes.
   *
   * @param pce The triggering PropertyChangeEvent.
   */
  public void propertyChange( PropertyChangeEvent pce ) {
    if( this.getIgnorePropertyChange(  ) ) {
      return;
    }

    this.setValid( false );

    String propName            = pce.getPropertyName(  );
    PropertyChangeEvent newPCE = new PropertyChangeEvent( 
        this, propName, oldValue, getValue(  ) );

    firePropertyChange( newPCE );
  }

  /**
   * Validates this RadioButtonPG.  A RadioButtonPG is considered valid if the
   * value returned from getValue() is not null and is a String.
   */
  public void validateSelf(  ) {
    Object val = getValue(  );

    if( ( val != null ) && val instanceof String ) {
      setValid( true );
    } else {
      setValid( false );
    }
  }

  /**
   * Used by addItem() and init() to create the GUI buttons when this PG is
   * shown.
   *
   * @param buttonName The name of the Button to add.
   */
  private void createGUIButton( String buttonName ) {
    JRadioButton tempButton = new JRadioButton( buttonName );

    radioChoices.remove( buttonName );
    radioChoices.put( buttonName, tempButton );
    radioGroup.add( tempButton );
    getEntryWidget(  ).add( tempButton );
    tempButton.addActionListener( rpgListener );
  }

  //~ Inner Classes ************************************************************

  /**
   * Class for listening to clicks on the JRadioButtons.
   */
  private class RadioButtonPGListener implements ActionListener {
    //~ Methods ****************************************************************

    /**
     * Sets the value of the RadioButtonPG based on the clicked button and
     * stores the previous value so that PropertyChangeEvents can be correctly
     * fired.
     *
     * @param event The ActionEvent triggered by the JRadioButton click.
     */
    public void actionPerformed( ActionEvent ae ) {
      oldValue = getValue(  ).toString(  );
      setValue( ae.getActionCommand(  ) );
    }
  }
}

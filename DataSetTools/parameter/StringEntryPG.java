/*
 * File:  StringEntryPG.java
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
 *  Revision 1.9  2003/08/22 20:12:07  bouzekc
 *  Modified to work with EntryWidget.
 *
 *  Revision 1.8  2003/08/21 21:45:18  bouzekc
 *  Added javadoc comments.
 *
 *  Revision 1.7  2003/08/15 23:56:21  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI.
 *
 *  Revision 1.6  2003/06/30 16:00:44  bouzekc
 *  Now returns the StringFilterer FILTER associated with this
 *  class, rather than the entrywidget's filter.  This is to
 *  aid in using the StringFilterer in noGUI situations.
 *
 *  Revision 1.5  2003/06/30 15:58:33  bouzekc
 *  Reformatted for consistency.
 *
 *  Revision 1.4  2003/06/18 22:48:38  bouzekc
 *  Added method to return StringFilterer associated with the
 *  entrywidget.
 *
 *  Revision 1.3  2003/06/10 13:48:32  bouzekc
 *  Fixed NullPointerException in init().
 *
 *  Revision 1.2  2003/06/09 20:30:21  pfpeterson
 *  Fixed problem with null values in the GUI.
 *
 *  Revision 1.1  2003/06/06 18:48:49  pfpeterson
 *  Added to CVS.
 *
 */
package DataSetTools.parameter;

import DataSetTools.components.ParametersGUI.*;

import DataSetTools.util.StringFilterer;

import java.beans.*;

import java.lang.String;

import java.util.Vector;

import javax.swing.*;


/**
 * This is a superclass to take care of many of the common details of
 * StringEntryPGs.
 */
public abstract class StringEntryPG extends ParameterGUI {
  protected static final int DEF_COLS = 20;
  protected StringFilterer FILTER     = null;

  // ********** Constructors **********
  /**
   * Creates a StringEntryPG with the specified name and value.  Does NOT draw
   * the "valid" checkbox.
   *
   * @param name The name of this StringEntryPG.
   * @param value The value of this StringEntryPG.
   */
  public StringEntryPG( String name, Object value ) {
    this( name, value, false );
    this.setDrawValid( false );
  }

  /**
   * Creates a StringEntryPG with the specified name, value and validity.
   *
   * @param name The name of this StringEntryPG.
   * @param value The value of this StringEntryPG.
   * @param valid Whether or not this StringEntryPG should be valid.
   */
  public StringEntryPG( String name, Object value, boolean valid ) {
    this.setName( name );
    this.setValue( value );
    this.setEnabled( true );
    this.setValid( valid );
    this.setDrawValid( true );
    this.type                 = "UNKNOWN";
    this.initialized          = false;
    this.ignore_prop_change   = false;
  }

  // ********** IParameter requirements **********

  /**
   * Returns the value of the parameter. While this is a generic
   * object specific parameters will return appropriate
   * objects. There can also be a 'fast access' method which returns
   * a specific object (such as String or DataSet) without casting.
   *
   * @return The value of this ParameterGUI.
   */
  public Object getValue(  ) {
    Object value = null;

    if( this.initialized ) {
      value = ( ( JTextField )( entrywidget.getComponent( 0 ) ) ).getText(  );
    } else {
      value = this.value;
    }

    return value;
  }

  /**
   * Sets the value of the parameter.
   *
   * @param value The new value.
   */
  protected void setEntryValue( Object value ) {
    if( this.initialized ) {
      if( value == null ) {
        ( ( JTextField )( entrywidget.getComponent( 0 ) ) ).setText( "" );
      } else {
        if( value instanceof String ) {
          ( ( JTextField )( entrywidget.getComponent( 0 ) ) ).setText( ( String )value );
        } else {
          ( ( JTextField )( entrywidget.getComponent( 0 ) ) ).setText( value.toString(  ) );
        }
      }
    } else {
      return;
    }

    this.setValid( true );
  }

  /**
   *  Accessor method to allow access to the StringFilter so that the outside
   *  world can pre-check any values that it wants to send in.
   *
   *  @return                    The StringFilterer (interface implemented by
   *                             StringFilter) that this PG uses.
   */
  public StringFilterer getStringFilter(  ) {
    return FILTER;
  }

  // ********** IParameterGUI requirements **********

  /**
   * Allows for initialization of the GUI after instantiation.
   * 
   * @param init_values The initial values to use.
   */
  public void initGUI( Vector init_values ) {
    if( this.initialized ) {
      return;  // don't initialize more than once
    }

    if( init_values != null ) {
      if( init_values.size(  ) == 1 ) {
        // the init_values is what to set as the value of the parameter
        this.setValue( init_values.elementAt( 0 ) );
      } else {
        // something is not right, should throw an exception
      }
    }

    if( this.value != null ) {
      entrywidget = new EntryWidget( 
                      new StringEntry( this.value.toString(  ), DEF_COLS, FILTER ) );
    } else {
      entrywidget = new EntryWidget( 
                      new StringEntry( "", DEF_COLS, FILTER ) );
    }

    entrywidget.addPropertyChangeListener( IParameter.VALUE, this );
    this.setEnabled( this.getEnabled(  ) );
    super.initGUI(  );
  }

  /**
   * Set the enabled state of the EntryWidget. This produces a more
   * pleasant effect than the default setEnabled of the widget.
   *
   * @param enabled Whether or not this StringEntryPG should be enabled.
   */
  public void setEnabled( boolean enabled ) {
    this.enabled = enabled;

    if( this.getEntryWidget(  ) != null ) {
      ( ( JTextField )( entrywidget.getComponent( 0 ) ) ).setEditable( enabled );
    }
  }
}

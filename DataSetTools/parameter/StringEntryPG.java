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
  public StringEntryPG( String name, Object value ) {
    this( name, value, false );
    this.setDrawValid( false );
  }

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
   */
  public Object getValue(  ) {
    Object value = null;

    if( this.initialized ) {
      value = ( ( JTextField )this.entrywidget ).getText(  );
    } else {
      value = this.value;
    }

    return value;
  }

  /**
   * Sets the value of the parameter.
   */
  protected void setEntryValue( Object value ) {
    if( this.initialized ) {
      if( value == null ) {
        ( ( JTextField )this.entrywidget ).setText( "" );
      } else {
        if( value instanceof String ) {
          ( ( JTextField )this.entrywidget ).setText( ( String )value );
        } else {
          ( ( JTextField )this.entrywidget ).setText( value.toString(  ) );
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
    return ( ( StringEntry )this.entrywidget ).getStringFilter(  );
  }

  // ********** IParameterGUI requirements **********

  /**
   * Allows for initialization of the GUI after instantiation.
   */
  public void init( Vector init_values ) {
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
      entrywidget = new StringEntry( this.value.toString(  ), DEF_COLS, FILTER );
    } else {
      entrywidget = new StringEntry( "", DEF_COLS, FILTER );
    }

    entrywidget.addPropertyChangeListener( IParameter.VALUE, this );
    this.setEnabled( this.getEnabled(  ) );
    super.initGUI(  );
  }

  /**
   * Set the enabled state of the EntryWidget. This produces a more
   * pleasant effect than the default setEnabled of the widget.
   */
  public void setEnabled( boolean enabled ) {
    this.enabled = enabled;

    if( this.getEntryWidget(  ) != null ) {
      ( ( JTextField )this.entrywidget ).setEditable( enabled );
    }
  }
}

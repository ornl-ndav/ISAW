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
 *  Revision 1.23  2003/11/25 03:02:32  bouzekc
 *  Now only tries to clone the Label if it has been initialized.
 *
 *  Revision 1.22  2003/11/23 02:12:18  bouzekc
 *  Now properly clones the label.
 *
 *  Revision 1.21  2003/11/19 04:06:53  bouzekc
 *  This class is now a JavaBean.  Added code to clone() to copy all
 *  PropertyChangeListeners.
 *
 *  Revision 1.20  2003/11/05 04:35:06  bouzekc
 *  validateSelf now handles the case where the StringFilterer is null.
 *
 *  Revision 1.19  2003/10/17 02:28:17  bouzekc
 *  Fixed javadoc errors and updated method javadocs.
 *
 *  Revision 1.18  2003/10/11 19:12:46  bouzekc
 *  Now implements clone() using reflection.  Now implements ParamUsesString.
 *
 *  Revision 1.16  2003/10/07 18:32:58  bouzekc
 *  Now implements ParamUsesString.
 *
 *  Revision 1.15  2003/09/16 22:46:55  bouzekc
 *  Removed addition of this as a PropertyChangeListener.  This is already done
 *  in ParameterGUI.  This should fix the excessive events being fired.
 *
 *  Revision 1.14  2003/09/13 23:35:58  bouzekc
 *  Fixed bug in validateSelf().
 *
 *  Revision 1.13  2003/09/13 23:16:40  bouzekc
 *  Removed calls to setEnabled in initGUI(Vector), since ParameterGUI.init()
 *  already calls this.
 *
 *  Revision 1.12  2003/09/09 23:06:31  bouzekc
 *  Implemented validateSelf().
 *
 *  Revision 1.11  2003/08/28 02:28:12  bouzekc
 *  Removed setEnabled() method.
 *
 *  Revision 1.10  2003/08/28 01:47:12  bouzekc
 *  Modified to work with new ParameterGUI.
 *
 *  Revision 1.9  2003/08/22 20:12:07  bouzekc
 *  Modified to work with getEntryWidget().
 *
 *  Revision 1.8  2003/08/21 21:45:18  bouzekc
 *  Added javadoc comments.
 *
 *  Revision 1.7  2003/08/15 23:56:21  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI.
 *
 *  Revision 1.6  2003/06/30 16:00:44  bouzekc
 *  Now returns the StringFilterer FILTER associated with this
 *  class, rather than the getEntryWidget()'s filter.  This is to
 *  aid in using the StringFilterer in noGUI situations.
 *
 *  Revision 1.5  2003/06/30 15:58:33  bouzekc
 *  Reformatted for consistency.
 *
 *  Revision 1.4  2003/06/18 22:48:38  bouzekc
 *  Added method to return StringFilterer associated with the
 *  getEntryWidget().
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Vector;

import javax.swing.*;


/**
 * This is a superclass to take care of many of the common details of
 * StringEntryPGs.
 */
public abstract class StringEntryPG extends ParameterGUI
  implements ParamUsesString {
  //~ Static fields/initializers ***********************************************

  protected static final int DEF_COLS = 20;

  //~ Instance fields **********************************************************

  protected StringFilterer FILTER = null;

  //~ Constructors *************************************************************

  // ********** Constructors **********

  /**
   * Creates a StringEntryPG with the specified name and value.  Does NOT draw
   * the "valid" checkbox.
   *
   * @param name The name of this StringEntryPG.
   * @param val The value of this StringEntryPG.
   */
  public StringEntryPG( String name, Object val ) {
    super( name, val );
  }

  /**
   * Creates a StringEntryPG with the specified name, value and validity.
   *
   * @param name The name of this StringEntryPG.
   * @param val The value of this StringEntryPG.
   * @param valid Whether or not this StringEntryPG should be valid.
   */
  public StringEntryPG( String name, Object val, boolean valid ) {
    super( name, val, valid );
    this.setType( "UNKNOWN" );
  }

  //~ Methods ******************************************************************

  /**
   * Accessor method to allow access to the StringFilter so that the outside
   * world can pre-check any values that it wants to send in.
   *
   * @return The StringFilterer (interface implemented by StringFilter) that
   *         this PG uses.
   */
  public StringFilterer getStringFilter(  ) {
    return FILTER;
  }

  /**
   * Accessor method for the value of this StringEntryPG.
   *
   * @return The value in the text field of this StringEntryPG.  If no GUI is
   *         present, returns the internal value.
   */
  public Object getValue(  ) {
    Object value = super.getValue(  );

    if( this.getInitialized() ) {
      value = ( ( JTextField )( getEntryWidget().getComponent( 0 ) ) ).getText(  );
    } 

    return value;
  }

  /**
   * Override of clone() to preserve the filter on the string entry GUI.
   *
   * @return A clone of this StringEntryPG.
   */
  public Object clone(  ) {
    try {
      Class klass           = this.getClass(  );
      Constructor construct = klass.getConstructor( 
          new Class[]{ String.class, Object.class } );
      StringEntryPG pg      = ( StringEntryPG )construct.newInstance( 
          new Object[]{ null, null } );
      pg.setName( new String( this.getName(  ) ) );
      pg.setValue( this.getValue(  ) );
      pg.setDrawValid( this.getDrawValid(  ) );
      pg.setValid( this.getValid(  ) );

      StringFilterer newFilter = this.getStringFilter(  );

      if( newFilter != null ) {
        pg.FILTER = newFilter;
      }

      if( this.getInitialized() ) {
        pg.initGUI( null );
        pg.setLabel( new String( this.getLabel(  ).getText(  ) ) );
      }

      if( getPropListeners(  ) != null ) {
        java.util.Enumeration e = getPropListeners(  ).keys(  );
        PropertyChangeListener pcl = null;
        String propertyName = null;

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
   * @param init_values The initial values to use.
   */
  public void initGUI( Vector init_values ) {
    if( this.getInitialized() ) {
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

    if( getValue(  ) != null ) {
      setEntryWidget( new EntryWidget( 
          new StringEntry( getValue(  ).toString(  ), DEF_COLS, FILTER ) ) );
    } else {
      setEntryWidget( new EntryWidget( 
            new StringEntry( "", DEF_COLS, FILTER ) ) );
    }
    super.initGUI(  );
  }

  /**
   * Validates this StringEntryPG.  In general, a valid FloatPG is one that
   * contains text that can go through its filter.  This is only valid for
   * derived classes that use a StringFilterer.  Those that do not must
   * implement their own validate() method.
   */
  public void validateSelf(  ) {
    StringFilterer sf = getStringFilter(  );

    if( sf == null ) {
      setValid( false );
    } else {
      setValid( sf.isOkay( 0, getValue(  ).toString(  ), "" ) );
    }
  }

  /**
   * Sets the value of the parameter.
   *
   * @param value The new value.
   */
  protected void setEntryValue( Object value ) {
    if( this.getInitialized() ) {
      if( value == null ) {
        ( ( JTextField )( getEntryWidget().getComponent( 0 ) ) ).setText( "" );
      } else {
        if( value instanceof String ) {
          ( ( JTextField )( getEntryWidget().getComponent( 0 ) ) ).setText( 
            ( String )value );
        } else {
          ( ( JTextField )( getEntryWidget().getComponent( 0 ) ) ).setText( 
            value.toString(  ) );
        }
      }
    } else {
      return;
    }
    this.setValid( true );
  }
}

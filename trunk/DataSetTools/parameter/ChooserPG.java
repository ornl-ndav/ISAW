/*
 * File:  ChooserPG.java
 *
 * Copyright (C) 2003, Peter F. Peterson
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
 *  $Log$
 *  Revision 1.27  2004/01/14 19:26:59  bouzekc
 *  Now checks the Vector of values in addItems() to be sure it is not null.
 *  initGUI() now directly calls addItems().
 *
 *  Revision 1.26  2004/01/14 19:20:23  bouzekc
 *  Added several more checks for null values to head potential bugs off.
 *
 *  Revision 1.25  2004/01/14 19:00:11  bouzekc
 *  Formatted code in preparation for bug hunting.
 *
 *  Revision 1.24  2004/01/14 18:41:12  bouzekc
 *  Fixed a bug that occurred when calling setValue() when the internal
 *  list was empty.
 *
 *  Revision 1.23  2004/01/09 00:18:35  bouzekc
 *  setValue() now adds the item if it is not in the list already.
 *
 *  Revision 1.22  2003/12/02 00:24:42  bouzekc
 *  Fixed bug that invalidated this ParameterGUI when initGUI was called.
 *  Fixed bug that prevented previous values from being saved when initGUI()
 *  was called.
 *
 *  Revision 1.21  2003/11/25 03:02:32  bouzekc
 *  Now only tries to clone the Label if it has been initialized.
 *
 *  Revision 1.20  2003/11/23 02:12:17  bouzekc
 *  Now properly clones the label.
 *
 *  Revision 1.19  2003/11/19 04:06:53  bouzekc
 *  This class is now a JavaBean.  Added code to clone() to copy all
 *  PropertyChangeListeners.
 *
 *  Revision 1.18  2003/10/11 19:04:23  bouzekc
 *  Now implements clone() using reflection.
 *
 *  Revision 1.17  2003/09/16 22:46:53  bouzekc
 *  Removed addition of this as a PropertyChangeListener.  This is already done
 *  in ParameterGUI.  This should fix the excessive events being fired.
 *
 *  Revision 1.16  2003/09/15 18:15:25  dennis
 *  Moved addItem() call from constructors to initGUI(). (Ruth)
 *
 *  Revision 1.15  2003/09/13 23:29:46  bouzekc
 *  Moved calls from setValid(true) to validateSelf().
 *
 *  Revision 1.14  2003/09/13 23:16:40  bouzekc
 *  Removed calls to setEnabled in initGUI(Vector), since ParameterGUI.init()
 *  already calls this.
 *
 *  Revision 1.13  2003/09/12 20:22:01  rmikk
 *  AddItem(one item only) now adds the item to the getEntryWidget() too. If the
 *    ParameterGUI has been getInitialized(), new entries can be
 *    added afterwards.
 *
 *  Revision 1.12  2003/09/09 23:06:28  bouzekc
 *  Implemented validateSelf().
 *
 *  Revision 1.11  2003/08/28 02:28:11  bouzekc
 *  Removed setEnabled() method.
 *
 *  Revision 1.10  2003/08/28 01:40:28  bouzekc
 *  Fixed bug in constructor where the passed in value was not added.
 *
 *  Revision 1.9  2003/08/28 01:36:56  bouzekc
 *  Modified to work with new ParameterGUI.
 *
 *  Revision 1.8  2003/08/22 20:12:07  bouzekc
 *  Modified to work with getEntryWidget().
 *
 *  Revision 1.7  2003/08/15 23:56:23  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI.
 *
 *  Revision 1.6  2003/08/15 03:55:34  bouzekc
 *  Removed unnecessary getInitialized()=true statement.
 *
 *  Revision 1.5  2003/08/02 04:52:23  bouzekc
 *  Fixed bug in init() which caused a reinitialization every time getEntryWidget()
 *  was shown.  Now properly updates the GUI when init() is called.
 *
 *  Revision 1.4  2003/06/05 22:34:34  bouzekc
 *  Added method to retrieve the index of a given item.
 *
 *  Revision 1.3  2003/03/25 19:39:57  pfpeterson
 *  Fixed bug with updating the DataSets listed in the combo box by
 *  allowing multiple calls to init.
 *
 *  Revision 1.2  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.1  2003/02/24 20:58:31  pfpeterson
 *  Added to CVS.
 *
 */
package DataSetTools.parameter;

import DataSetTools.components.ParametersGUI.EntryWidget;
import DataSetTools.components.ParametersGUI.HashEntry;

import DataSetTools.dataset.DataSet;

import java.beans.PropertyChangeListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Vector;


/**
 * This is a superclass to take care of many of the common details of Parameter
 * GUIs that use a combobox.
 */
public abstract class ChooserPG extends ParameterGUI {
  //~ Static fields/initializers ***********************************************

  // static variables
  private static String TYPE    = "Chooser";
  protected static int DEF_COLS = 20;

  //~ Instance fields **********************************************************

  // instance variables
  protected Vector vals = null;

  //~ Constructors *************************************************************

  // ********** Constructors **********
  public ChooserPG( String name, Object val ) {
    super( name, val );
    setValue( val );
    this.setType( TYPE );
  }

  /**
   * Creates a new ChooserPG object.
   *
   * @param name The name of this ChooserPG.
   * @param val The value of this ChooserPG.
   * @param valid If this ChooserPG should be considered initially valid.
   */
  public ChooserPG( String name, Object val, boolean valid ) {
    super( name, val, valid );
    setValue( val );
    this.setType( TYPE );
  }

  //~ Methods ******************************************************************

  /**
   * @return The index of an item.  If the internal list does not exist, this
   *         returns -1.  If the item is not found, this will return a
   *         negative number.
   */
  public int getIndex( Object val ) {
    if( vals == null ) {
      return -1;
    }

    return vals.indexOf( val );
  }

  /**
   * Sets the value of the parameter.  This will add an item to the list if it
   * is not in there already.  This will simply return if the new value is
   * null.
   *
   * @param val The new value to be set.
   */
  public void setValue( Object val ) {
    if( val != null ) {
      if( vals == null ) {
        vals = new Vector(  );
      }

      //item is not in the list, so add it
      if( vals.indexOf( val ) < 0 ) {
        addItem( val );
      }

      //update the GUI part
      if( this.getInitialized(  ) ) {
        ( ( HashEntry )( getEntryWidget(  ).getComponent( 0 ) ) ).setSelectedItem( 
          val );
      }

      //always update the internal value
      super.setValue( val );
    }
  }

  /**
   * Returns the value of the selected item if this ParameterGUI has been
   * getInitialized().  Otherwise, it returns the internal value.
   */
  public Object getValue(  ) {
    Object val = super.getValue(  );

    if( this.getInitialized(  ) ) {
      val = ( ( HashEntry )( getEntryWidget(  ).getComponent( 0 ) ) ).getSelectedItem(  );
    }

    return val;
  }

  // ********** Methods to deal with the hash **********

  /**
   * Add a single item to the vector of choices.  If the new value is null,
   * this does nothing.
   *
   * @param val The item to add.
   */
  public void addItem( Object val ) {
    if( val != null ) {
      if( vals == null ) {
        // initialize if necessary
        vals = new Vector(  );
      }

      //add it if it is not in the list
      if( vals.indexOf( val ) < 0 ) {
        vals.add( val );

        if( getInitialized(  ) ) {
          ( ( HashEntry )( getEntryWidget(  ).getComponent( 0 ) ) ).addItem( 
            val );
        }
      }
    }
  }

  /**
   * Add a set of items to the vector of choices at once.  This also sets the
   * value of this ChooserPG to the first item in the list of values.  If the
   * parameter is null, this does  nothing.
   *
   * @param values The Vector of values to add.
   */
  public void addItems( Vector values ) {
    if( values != null ) {
      for( int i = 0; i < values.size(  ); i++ ) {
        addItem( values.elementAt( i ) );
      }
    }
  }

  /**
   * Definition of the clone method.  Overridden to provide for cloning the
   * internal Vector of values.
   */
  public Object clone(  ) {
    try {
      Class klass           = this.getClass(  );
      Constructor construct = klass.getConstructor( 
          new Class[]{ String.class, Object.class } );
      ChooserPG pg          = ( ChooserPG )construct.newInstance( 
          new Object[]{ null, null } );
      pg.setName( new String( this.getName(  ) ) );
      pg.setValue( this.getValue(  ) );
      pg.setDrawValid( this.getDrawValid(  ) );
      pg.setValid( this.getValid(  ) );

      if( ( this.vals ) == null ) {
        pg.vals = null;
      } else {
        pg.vals = ( Vector )this.vals.clone(  );
      }

      if( this.getInitialized(  ) ) {
        pg.initGUI( new Vector(  ) );
        pg.setLabel( new String( this.getLabel(  ).getText(  ) ) );
      }

      if( this.getPropListeners(  ) != null ) {
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
      e.printStackTrace(  );
      throw new RuntimeException( e.getTargetException(  ).getMessage(  ) );
    }
  }

  // ********** IParameterGUI requirements **********

  /**
   * Allows for initialization of the GUI after instantiation.
   *
   * @param init_values The new values to add to the currently existing ones.
   */
  public void initGUI( Vector init_values ) {
    if( this.getInitialized(  ) ) {
      return;
    }

    //make sure that the superclass value is in the list
    Object initVal = getValue(  );

    if( ( initVal != null ) && ( initVal != DataSet.EMPTY_DATA_SET ) ) {
      this.addItem( initVal );
    }

    //addItems will check to see if the Vector is null and set the 
    //value to the first element
    addItem( init_values );

    // set up the combobox
    setEntryWidget( new EntryWidget( new HashEntry( this.vals ) ) );
    super.initGUI(  );

    //ignore prop changes because we are about to change the value
    boolean ignore = getIgnorePropertyChange(  );
    setIgnorePropertyChange( true );

    //GUI won't properly update without this
    setValue( super.getValue(  ) );
    setIgnorePropertyChange( ignore );
  }

  /**
   * Allows initialization using an array.
   *
   * @param init_values The array of Objects to add to the list of values.
   */
  public void initGUI( Object[] init_values ) {
    Vector init_vec;

    if( init_values != null ) {
      init_vec = new Vector( init_values.length );

      for( int i = 0; i < init_values.length; i++ ) {
        init_vec.add( init_values[i] );
      }
    } else {
      init_vec = new Vector( 1, 1 );
    }
    initGUI( init_vec );
  }

  /**
   * Remove an item from the internal list.  If the item to remove is null,
   * this does nothing.
   *
   * @param val The item to remove.
   */
  public void removeItem( Object val ) {
    if( ( val != null ) && ( vals != null ) ) {
      int index = vals.indexOf( val );

      if( index >= 0 ) {
        vals.remove( index );
      }
    }
  }

  /**
   * Validates this ChooserPG.  This just checks to be sure that getValue()
   * does not return null.  A derived class may want to do more stringent
   * checks.
   */
  public void validateSelf(  ) {
    setValid( ( getValue(  ) != null ) );
  }
}

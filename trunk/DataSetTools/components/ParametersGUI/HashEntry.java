/*
 * File: HashEntry.java
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
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.6  2004/05/09 18:17:18  bouzekc
 *  Reformatted and added comments.
 *
 *  Revision 1.5  2004/03/15 19:33:48  dennis
 *  Removed unused imports after factoring out view components,
 *  math and utilities.
 *
 *  Revision 1.4  2004/03/15 03:27:21  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.3  2003/12/14 19:20:41  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.2  2002/11/27 23:12:34  pfpeterson
 *  standardized header
 *
 *  Revision 1.1  2002/08/05 13:56:54  pfpeterson
 *  Added to CVS.
 *
 *  Revision 1.1  2002/06/06 16:09:25  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.components.ParametersGUI;

import DataSetTools.parameter.*;

import gov.anl.ipns.Util.Messaging.*;

import java.awt.event.*;

import java.beans.*;

import java.util.Vector;

import javax.swing.*;


/**
 * This class is intended to be used as a replacement for JTextField whan a
 * integer value is to be entered. The major difference is an overridden
 * insertString method which beeps when something that isn't found in an
 * integer is entered.
 */
public class HashEntry extends JComboBox implements PropertyChanger {
  //~ Static fields/initializers ***********************************************

  private static boolean DEBUG = false;

  //~ Instance fields **********************************************************

  private PropertyChangeSupport propBind = new PropertyChangeSupport( this );
  private Object lastSelected;

  //~ Constructors *************************************************************

  /**
   * Constructs a HashEntry with the choices specified.
   */
  public HashEntry( Vector v ) {
    super(  );

    if( v == null ) {
      return;
    }

    for( int i = 0; i < v.size(  ); i++ ) {
      this.addItem( v.get( i ) );
    }

    this.lastSelected = this.getSelectedItem(  );
    this.addActionListener( 
      new ActionListener(  ) {
        public void actionPerformed( ActionEvent e ) {
          HashEntry cb       = ( HashEntry )e.getSource(  );
          Object newSelected = cb.getSelectedItem(  );

          if( !cb.lastSelected.equals( newSelected ) ) {
            cb.fireValueChange( cb.lastSelected, newSelected );
            cb.lastSelected = newSelected;
          }
        }
      } );
  }

  /**
   * Constructs a HashEntry with the choices specified.
   */
  public HashEntry( Object selected, Vector v ) {
    this( v );
    this.setSelectedItem( selected );
  }

  //~ Methods ******************************************************************

  /*public void setEnabled(boolean enabled){
     super.setEnabled(enabled);
     }*/

  /**
   * This method overrides the default version in the superclass. The version
   * checks that the item to be added is not a duplicate first.
   */
  public void addItem( Object item ) {
    // check that we aren't double adding an item
    for( int i = 0; i < this.getItemCount(  ); i++ ) {
      if( item.equals( this.getItemAt( i ) ) ) {
        if( DEBUG ) {
          System.out.println( "WARNING: trying to add " + item + " again" );
        }

        return;
      }
    }

    // must be unique, add it
    super.addItem( item );
  }

  /**
   * Adds a PropertyChangeListener.
   *
   * @param prop The property to listen for.
   * @param pcl The PropertyChangeListener.
   */
  public void addPropertyChangeListener( 
    String prop, PropertyChangeListener pcl ) {
    super.addPropertyChangeListener( prop, pcl );

    if( propBind != null ) {
      propBind.addPropertyChangeListener( prop, pcl );
    }
  }

  /**
   * Adds a PropertyChangeListener.
   *
   * @param pcl The PropertyChangeListener to add.
   */
  public void addPropertyChangeListener( PropertyChangeListener pcl ) {
    super.addPropertyChangeListener( pcl );

    if( propBind != null ) {
      propBind.addPropertyChangeListener( pcl );
    }
  }

  /**
   * Removes a PropertyChangeListener.
   *
   * @param pcl The PropertyChangeListener to remove.
   */
  public void removePropertyChangeListener( PropertyChangeListener pcl ) {
    super.removePropertyChangeListener( pcl );

    if( propBind != null ) {
      propBind.removePropertyChangeListener( pcl );
    }
  }

  /**
   * This takes care of firing the property change event out to all that might
   * be listening.
   */
  private void fireValueChange( Object oldValue, Object newValue ) {
    if( 
      ( propBind != null ) && ( newValue != null ) &&
        !oldValue.equals( newValue ) ) {
      this.propBind.firePropertyChange( IParameter.VALUE, oldValue, newValue );
    }
  }
}

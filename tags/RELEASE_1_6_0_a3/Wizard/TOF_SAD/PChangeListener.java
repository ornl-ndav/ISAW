

/*
 * File:  PChangeListener.java 
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
 * Revision 1.1  2003/11/11 20:46:42  rmikk
 * Initial Checkin
 *
 */
package Wizard.TOF_SAD;
import java.beans.*;
import DataSetTools.parameter.*;
import java.util.*;


/**
 *    This class is a special PropertyChangeListener that changes the
 *    Enabled status of a set of ParameterGUI's based on the new value
 *    of a PropertyChangeEvent
*/
public class PChangeListener implements PropertyChangeListener{
  Vector V ;
  Object TrueValue;
  public PChangeListener( ParameterGUI listener){
     V = new Vector();
     V.addElement( listener);
     TrueValue = new Boolean(true);

  }

  /**
   *   Constructor.
   *   @param  listeners  the set of ParameterGUI's that will be enabled
   *           when the new value of a PropertyChangeEvent is true. Otherwise the
   *           the ParameterGUI's will be disabled. 
   */
  public PChangeListener( ParameterGUI[] listeners){
    V = new Vector();
    if( listeners == null)
      return;
    for( int i=0; i< listeners.length; i++)
       V.addElement(listeners[i]);
     TrueValue = new Boolean(true);

   }

  /**
   *    Constructor
   *   @param  listeners  the set of ParameterGUI's that will be enabled
   *           when the new value of a PropertyChangeEvent matches the 
   *           TrueValue
   *   @param  TrueValue   The value the the new value  must match so the 
   *                       listeners will be enabled. Otherwise the 
   *                       ParameterGUI's will be disabled
   */
  public PChangeListener(ParameterGUI[] listeners, Object TrueValue){
    V = new Vector();
    if( listeners == null)
      return;
    for( int i=0; i< listeners.length; i++)
       V.addElement( listeners[i]);
    this.TrueValue = TrueValue;

  }

  /**
   *    Constructor
   *    @param  listeners  One Parameter GUI that will be enabled when a 
   *                   PropertyChangeEvent's new Value matches the TrueValue
   *   @param  TrueValue   The value the the new value  must match so the 
   *                       listeners will be enabled. Otherwise the 
   *                        ParameterGUI will be disabled
   */
  public PChangeListener(ParameterGUI listeners, Object TrueValue){
     V = new Vector();
     if( listeners == null)
        return;
     V.addElement( listeners);
     this.TrueValue = TrueValue;
  }


  /**
   *   The method that changes the listeners to enabled if evt's new value 
   *   matches TrueValue.  Otherwise the listeners are disabled.
   *   @param  evt  the PropertyChangeEvent whose NewValue determines whether 
   *              the ParameterGui(s) are enabled or disabled
   */
  public void propertyChange(PropertyChangeEvent evt){
     Object value = evt.getNewValue();
     if( value == null)
        return;
     boolean set = value.equals( TrueValue);
     for( int i=0; i< V.size(); i++){
        ParameterGUI PG = (ParameterGUI)(V.elementAt(i));
        PG.setEnabled( set);  
     }

  } 
}




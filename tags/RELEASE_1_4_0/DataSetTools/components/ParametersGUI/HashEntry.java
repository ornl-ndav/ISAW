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
 *           9700 S. Cass Avenue, Bldg 360
 *           Argonne, IL 60440
 *           USA
 *
 * For further information, see http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2002/08/05 13:56:54  pfpeterson
 *  Added to CVS.
 *
 *  Revision 1.1  2002/06/06 16:09:25  pfpeterson
 *  Added to CVS.
 *
 *
 */
 
package DataSetTools.components.ParametersGUI;

import javax.swing.*; 
import javax.swing.text.*; 
import java.awt.event.*;
import java.awt.*;
import java.util.Locale;
import java.util.Vector;
import java.beans.*;
import DataSetTools.parameter.*;
import DataSetTools.util.*;
import DataSetTools.util.StringFilterer;

/**
 * This class is intended to be used as a replacement for JTextField
 * whan a integer value is to be entered. The major difference is an
 * overridden insertString method which beeps when something that
 * isn't found in an integer is entered.
 */
public class HashEntry extends JComboBox implements PropertyChanger{
    private static boolean DEBUG=false;

    private PropertyChangeSupport propBind=new PropertyChangeSupport(this);
    private Object lastSelected;

    /**
     * Constructs a HashEntry with the choices specified.
     */
    public HashEntry(Vector v){
        super();
        if(v==null)return;
        for( int i=0 ; i<v.size() ; i++ ){
            this.addItem(v.get(i));
        }
        this.lastSelected=this.getSelectedItem();
        this.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    HashEntry cb = (HashEntry)e.getSource();
                    Object newSelected=cb.getSelectedItem();
                    if(!cb.lastSelected.equals(newSelected)){
                        cb.fireValueChange(cb.lastSelected,newSelected);
                        cb.lastSelected=newSelected;
                    }
                }
            });
    }

    /**
     * Constructs a HashEntry with the choices specified.
     */
    public HashEntry(Object selected, Vector v){
        this(v);
        this.setSelectedItem(selected);
    }

    /*public void setEnabled(boolean enabled){
      super.setEnabled(enabled);
      }*/

    /**
     * This method overrides the default version in the
     * superclass. The version checks that the item to be added is not
     * a duplicate first.
     */
    public void addItem(Object item){
        // check that we aren't double adding an item
        for( int i=0 ; i<this.getItemCount() ; i++ ){
            if(item.equals(this.getItemAt(i))){
                if(DEBUG)System.out.println("WARNING: trying to add "+item
                                            +" again");
                return;
            }
        }
        // must be unique, add it
        super.addItem(item);
    }

    /**
     * This takes care of firing the property change event out to all
     * that might be listening.
     */
    private void fireValueChange(Object oldValue, Object newValue){
        if(propBind!=null && newValue!=null && !oldValue.equals(newValue)){
            this.propBind.firePropertyChange(IParameter.VALUE,
                                             oldValue,newValue);
        }
    }

    // ********** Methods for the PropertyChanger interface
    public void addPropertyChangeListener(String prop,
                                          PropertyChangeListener pcl){
        super.addPropertyChangeListener(prop,pcl);
        if(propBind!=null) propBind.addPropertyChangeListener(prop,pcl);
    }
    public void addPropertyChangeListener(PropertyChangeListener pcl){
        super.addPropertyChangeListener(pcl);
        if(propBind!=null) propBind.addPropertyChangeListener(pcl);
    }
    public void removePropertyChangeListener(PropertyChangeListener pcl){
        super.removePropertyChangeListener(pcl);
        if(propBind!=null) propBind.removePropertyChangeListener(pcl);
    }
}

/*
 * File:  ArrayPG.java 
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
 *           Intense Pulse Neutron Source Division
 *           Argonne National Laboratory
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
 *  Revision 1.2  2002/09/19 16:07:20  pfpeterson
 *  Changed to work with new system where operators get IParameters in stead of Parameters. Now support clone method.
 *
 *  Revision 1.1  2002/08/01 18:40:01  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;

import java.util.Vector;
import DataSetTools.components.ParametersGUI.HashEntry;

/**
 * This is a superclass to take care of many of the common details of
 * Array Parameter GUIs.
 */
public class ArrayPG extends ParameterGUI{
    // static variables
    private   static String TYPE     = "Array";
    protected static int    DEF_COLS = 20;

    // instance variables
    protected   Vector vals;

    // ********** Constructors **********
    public ArrayPG(String name, Object value){
        this(name,value,false);
        this.setDrawValid(false);
        this.type=TYPE;
    }

    public ArrayPG(String name, Object value, boolean valid){
        this.addItem(value);
        this.setName(name);
        this.setValue(value);
        this.setEnabled(true);
        this.setValid(valid);
        this.setDrawValid(true);
        this.type=TYPE;
        this.initialized=false;
        this.ignore_prop_change=false;
    }

    // ********** Methods to deal with the hash **********

    /**
     * Add a single item to the vector of choices.
     */
    public void addItem( Object val){
        if(this.vals==null) this.vals=new Vector(); // initialize if necessary
        if(val==null) return; // don't add null to the vector
        if(this.vals.indexOf(val)<0) this.vals.add(val); // add if unique
    }

    /**
     * Add a set of items to the vector of choices at once.
     */
    public void addItems( Vector vals){
        for( int i=0 ; i<vals.size() ; i++ ){
            addItem(vals.elementAt(i));
        }
    }

    /**
     * Remove an item from the hash based on its key.
     */
    public void removeItem( Object val ){
        int index=vals.indexOf(val);
        if(index>=0) vals.remove(index);
    }

    // ********** IParameter requirements **********

    /**
     * Returns the value of the parameter. While this is a generic
     * object specific parameters will return appropriate
     * objects. There can also be a 'fast access' method which returns
     * a specific object (such as String or DataSet) without casting.
     */
    public Object getValue(){
        Object value=null;
        if(this.initialized){
            value=((HashEntry)this.entrywidget).getSelectedItem();
        }else{
            value=this.value;
        }
        return value;
    }

    /**
     * Sets the value of the parameter.
     */
    public void setValue(Object value){
        this.addItem(value);
        if(this.initialized){
            if(value==null){
                // do nothing
            }else{
                ((HashEntry)this.entrywidget).setSelectedItem(value);
            }
        }else{
            this.value=value;
        }
        this.setValid(true);
    }

    // ********** IParameterGUI requirements **********
    /**
     * Allows for initialization of the GUI after instantiation.
     */
    public void init(Vector init_values){
        if(this.initialized) return; // don't initialize more than once
        if(init_values!=null){
            if(init_values.size()==1){
                this.setValue(init_values.elementAt(0));
            }else if(init_values.size()>1){
                for( int i=0 ; i<init_values.size() ; i++ ){
                    this.addItem(init_values.elementAt(i));
                }
            }else{
                // something is not right, should throw an exception
            }
        }

        // set up the combobox
        this.entrywidget=new HashEntry(this.vals);
        this.entrywidget.setEnabled(this.enabled);
        this.entrywidget.addPropertyChangeListener(IParameter.VALUE, this);
        this.packupGUI();
        this.initialized=true;
    }

    /**
     * Since this is an array parameter, better allow an array to
     * initialize the GUI.
     */
    public void init(Object init_values[]){
        Vector init_vec=new Vector();
        for( int i=0 ; i<init_values.length ; i++ ){
            init_vec.add(init_values[i]);
        }
        this.init(init_vec);
    }

    /**
     * Set the enabled state of the EntryWidget. This produces a more
     * pleasant effect that the default setEnabled of the widget.
     */
    public void setEnabled(boolean enabled){
        this.enabled=enabled;
        if(this.entrywidget!=null) this.entrywidget.setEnabled(this.enabled);
    }

    /**
     * Main method for testing purposes.
     */
    static void main(String args[]){
        ArrayPG fpg;
        int y=0, dy=70;

        Vector vals=new Vector();
        vals.add("bob");
        vals.add("bob");
        vals.add("doug");

        fpg=new ArrayPG("a","1f");
        System.out.println(fpg);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new ArrayPG("b","10f");
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new ArrayPG("c","1000f",false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new ArrayPG("d","100f",true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.init(vals);
        fpg.showGUIPanel(0,y);
        y+=dy;

    }

    /**
     * Definition of the clone method.
     */
    public Object clone(){
        ArrayPG apg=new ArrayPG(this.name,this.value,this.valid);
        apg.setDrawValid(this.getDrawValid());
        apg.initialized=false;
        return apg;
    }
}

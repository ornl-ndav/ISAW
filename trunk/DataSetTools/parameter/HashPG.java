/*
 * File:  HashPG.java 
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
 *  Revision 1.13  2003/09/16 22:46:54  bouzekc
 *  Removed addition of this as a PropertyChangeListener.  This is already done
 *  in ParameterGUI.  This should fix the excessive events being fired.
 *
 *  Revision 1.12  2003/09/13 23:29:47  bouzekc
 *  Moved calls from setValid(true) to validateSelf().
 *
 *  Revision 1.11  2003/09/13 23:16:40  bouzekc
 *  Removed calls to setEnabled in initGUI(Vector), since ParameterGUI.init()
 *  already calls this.
 *
 *  Revision 1.10  2003/08/28 02:28:11  bouzekc
 *  Removed setEnabled() method.
 *
 *  Revision 1.9  2003/08/28 01:54:05  bouzekc
 *  Modified to work with new ParameterGUI.
 *
 *  Revision 1.8  2003/08/22 20:12:07  bouzekc
 *  Modified to work with EntryWidget.
 *
 *  Revision 1.7  2003/08/15 23:56:20  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI.
 *
 *  Revision 1.6  2003/06/06 18:49:44  pfpeterson
 *  Made abstract and removed clone method.
 *
 *  Revision 1.5  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.4  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/10/10 22:11:51  pfpeterson
 *  Fixed a bug with the clone method not getting the choices copied over.
 *
 *  Revision 1.2  2002/09/19 16:07:22  pfpeterson
 *  Changed to work with new system where operators get IParameters in stead of Parameters. Now support clone method.
 *
 *  Revision 1.1  2002/08/01 18:40:04  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;

import java.util.Vector;
import DataSetTools.components.ParametersGUI.HashEntry;
import DataSetTools.components.ParametersGUI.EntryWidget;

/**
 * This is a superclass to take care of many of the common details of
 * Hash Parameter GUIs.
 */
abstract public class HashPG extends ParameterGUI{
    // static variables
    private   static String TYPE     = "Hash";
    protected static int    DEF_COLS = 20;

    // instance variables
    private   Vector keys;
    private   Vector vals;

    // ********** Constructors **********
    public HashPG(String name, Object val){
        super( name, val );
        addItem( val.toString(  ), val );
        setValue( val );
    }

    public HashPG(String name, Object val, boolean valid){
        super( name, val, valid );
        addItem( val.toString(  ), val );
        setValue( val );
    }

    public HashPG(String name, Object val, String key){
        super(name,val);
        this.type=TYPE;
        addItem( key, val );
        setValue( val );
    }

    public HashPG(String name, Object val, boolean valid, String key){
        super( name, val, valid );
        this.type=TYPE;
        addItem(key,value);
        setValue( val );
    }

    // ********** Methods to deal with the hash **********

    /**
     * Add a single item to the hash.
     */
    public void addItem( String key, Object val){
        if( (this.keys==null) || (this.vals==null) ){
            this.keys=new Vector();
            this.vals=new Vector();
        }
        int index=this.keys.indexOf(key);
        if(index<0){              // do not already have the key
            this.keys.add(key);        // so add it
            this.vals.add(val);
        }else{                    // clobber the last version
            this.vals.set(index,val);
        }
    }

    /**
     * Add a set of items to the hash at once.
     */
    public void addItems( Vector keys, Vector vals){
        if( keys.size()!=vals.size() ) return;
        for( int i=0 ; i<keys.size() ; i++ ){
            addItem((String)keys.elementAt(i),vals.elementAt(i)); 
        }
    }

    /**
     * Remove an item from the hash based on its key.
     */
    public void removeItem( String key ){
        int index=keys.indexOf(key);
        if(index>=0){
            keys.remove(index);
            vals.remove(index);
        }
    }

    /**
     * Private method for resolving the actual value of a given key.
     */
    private Object getValue(String key){
        int index=keys.indexOf(key);
        if(index>=0){
            return vals.elementAt(index);
        }else{
            return null;
        }
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
            value=((HashEntry)(entrywidget.getComponent(0))).getSelectedItem().toString();
            return this.getValue((String)value);
        }else{
            value=this.value;
        }
        return value;
    }

    /**
     * Sets the value of the parameter.
     */
    public void setValue(Object value){
        if(this.initialized){
            if(value==null){
                // do nothing
            }else{
                int index=keys.indexOf(value);
                if(index<=0){
                    ((HashEntry)(entrywidget.getComponent(0)))
                        .setSelectedItem(this.getValue((String)value));
                }else{
                    ((HashEntry)(entrywidget.getComponent(0)))
                        .setSelectedItem(value);
                }
            }
        }else{
            this.value=value;
        }
        validateSelf();
    }

    // ********** IParameterGUI requirements **********
    /**
     * Allows for initialization of the GUI after instantiation.
     */
    public void initGUI(Vector init_values){
        if(this.initialized) return; // don't initialize more than once
        if(init_values!=null){
            if(init_values.size()==1){
                // the init_values is what to set as the value of the parameter
                this.setValue(init_values.elementAt(0));
            }else if(init_values.size()==2){
                Vector keys=(Vector)init_values.elementAt(0);
                Vector vals=(Vector)init_values.elementAt(0);
                if(keys.size()==vals.size()){
                    for( int i=0 ; i<keys.size() ; i++ ){
                        if(keys.elementAt(i)instanceof String){
                            this.addItem((String)keys.elementAt(i),
                                         vals.elementAt(i));
                        }
                    }
                }
            }else{
                // something is not right, should throw an exception
            }
        }

        // set up the combobox
        entrywidget=new EntryWidget(new HashEntry(keys));
        super.initGUI();
    }

    /**
     * An easier method for adding the hash of keys and values.
     */
    public void initGUI(Vector keys, Vector vals){
        if(keys.size()==vals.size()){
            Vector init_vals=new Vector();
            init_vals.addElement(keys);
            init_vals.addElement(vals);
            this.initGUI(init_vals);
        }else{
            this.initGUI(null);
        }
    }
}

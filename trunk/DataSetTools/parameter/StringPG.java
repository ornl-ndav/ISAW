/*
 * File:  StringPG.java 
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
 *  Revision 1.3  2002/07/15 21:27:33  pfpeterson
 *  Factored out parts of the GUI.
 *
 *  Revision 1.2  2002/06/14 14:21:14  pfpeterson
 *  Added more checks to setValue() and getValue().
 *
 *  Revision 1.1  2002/06/06 16:14:37  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;
import javax.swing.*;
import java.util.Vector;
import java.lang.String;
import java.beans.*;
import DataSetTools.components.ParametersGUI.*;

/**
 * This is a superclass to take care of many of the common details of
 * StringPGs.
 */
public class StringPG extends ParameterGUI{
    private   static String TYPE     = "String";
    protected static int    DEF_COLS = 20;

    // ********** Constructors **********
    public StringPG(String name, Object value){
        this(name,value,false);
        this.setDrawValid(false);
        this.type=TYPE;
    }

    public StringPG(String name, Object value, boolean valid){
        this.setName(name);
        this.setValue(value);
        this.setEnabled(true);
        this.setValid(valid);
        this.setDrawValid(true);
        this.type=TYPE;
        this.initialized=false;
        this.ignore_prop_change=false;
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
            value=((JTextField)this.entrywidget).getText();
        }else{
            value=this.value;
        }
        return value;
    }

    public String getStringValue(){
        Object ob=this.getValue();
        if(ob==null){
            return null;
        }else{
            return this.getValue().toString();
        }
    }

    /**
     * Sets the value of the parameter.
     */
    public void setValue(Object value){
        if(this.initialized){
            if(value==null){
                ((JTextField)this.entrywidget).setText("");
            }else{
                if(value instanceof String){
                    ((JTextField)this.entrywidget).setText((String)value);
                }else{
                    ((JTextField)this.entrywidget).setText(value.toString());
                }
            }
        }else{
            this.value=value;
        }
        this.setValid(true);
    }

    /**
     * Returns the string used in scripts to denote the particular
     * parameter.
     */
    /*public String getType(){
      return this.type;
      }*/
    
    // ********** IParameterGUI requirements **********
    /**
     * Allows for initialization of the GUI after instantiation.
     */
    public void init(Vector init_values){
        if(this.initialized) return; // don't initialize more than once
        if(init_values!=null){
            if(init_values.size()==1){
                // the init_values is what to set as the value of the parameter
                this.setValue(init_values.elementAt(0));
            }else{
                // something is not right, should throw an exception
            }
        }
        //entrywidget=new StringField((String)this.getValue(),20);
        entrywidget=new StringEntry(this.getStringValue(),DEF_COLS);
        entrywidget.addPropertyChangeListener(IParameter.VALUE, this);
        this.setEnabled(this.getEnabled());
        this.packupGUI();
        this.initialized=true;
    }

    /**
     * Set the enabled state of the EntryWidget. This produces a more
     * pleasant effect that the default setEnabled of the widget.
     */
    public void setEnabled(boolean enabled){
        this.enabled=enabled;
        if(this.getEntryWidget()!=null){
            ((JTextField)this.entrywidget).setEditable(enabled);
        }
        // need to add stuff for actually enabling
    }

    static void main(String args[]){
        StringPG fpg;

        fpg=new StringPG("a","1f");
        System.out.println(fpg);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new StringPG("b","10f");
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new StringPG("c","1000f",false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new StringPG("d","100f",true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.init();
        fpg.showGUIPanel();

    }
}

/*
 * File:  BrowsePG.java 
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
 *  Revision 1.1  2002/07/15 21:26:06  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;
import javax.swing.*;
import java.util.Vector;
import java.lang.String;
import java.beans.*;
import java.io.File;
import DataSetTools.components.ParametersGUI.*;

/**
 * This is a superclass to take care of many of the common details of
 * BrowsePGs.
 */
public class BrowsePG extends ParameterGUI{
    private static String TYPE     = "Browse";

    protected static int VIS_COLS  = 12;
    protected static int HIDE_COLS = StringPG.DEF_COLS;

    protected StringEntry innerEntry = null;
    protected JButton     browse     = null;

    // ********** Constructors **********
    public BrowsePG(String name, Object value){
        this(name,value,false);
        this.setDrawValid(false);
        this.type=TYPE;
    }

    public BrowsePG(String name, Object value, boolean valid){
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
        String value=null;
        if(this.initialized){
            value=((JTextField)this.innerEntry).getText();
        }else{
            value=(String)this.value;
        }
        return value;
    }

    public String getStringValue(){
        return (String)this.getValue();
    }

    /**
     * Sets the value of the parameter.
     */
    public void setValue(Object value){
        if(this.initialized){
            if(value==null){
                ((JTextField)this.innerEntry).setText("");
            }else{
                if(value instanceof String){
                    ((JTextField)this.innerEntry).setText((String)value);
                }else{
                    ((JTextField)this.innerEntry).setText(value.toString());
                }
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
                // the init_values is what to set as the value of the parameter
                this.setValue(init_values.elementAt(0));
            }else{
                // something is not right, should throw an exception
            }
        }
        innerEntry=new StringEntry(this.getStringValue(),StringPG.DEF_COLS);
        innerEntry.addPropertyChangeListener(IParameter.VALUE, this);
        browse=new JButton("Browse");
        browse.addActionListener(new BrowseButtonListener(innerEntry,
                                              BrowseButtonListener.DIR_ONLY));
        entrywidget=new JPanel();
        entrywidget.add(innerEntry);
        entrywidget.add(browse);
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
        if(this.innerEntry!=null){
            this.innerEntry.setEditable(enabled);
            if(enabled){
                this.innerEntry.setColumns(VIS_COLS);
            }else{
                this.innerEntry.setColumns(HIDE_COLS);
            }
        }
        if(this.browse!=null){
            this.browse.setVisible(enabled);
        }
    }

    static void main(String args[]){
        BrowsePG fpg;
        int y=0, dy=70;
        String defString="/IPNShome/pfpeterson/IsawProps.dat";

        fpg=new BrowsePG("a",defString);
        System.out.println(fpg);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new BrowsePG("b",defString);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new BrowsePG("c",defString,false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new BrowsePG("d",defString,true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.init();
        fpg.showGUIPanel(0,y);

    }
}

/*
 * File:  IntegerPG.java 
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
 *  Revision 1.4  2002/10/07 15:27:41  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.3  2002/09/30 15:20:51  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.2  2002/06/12 14:20:20  pfpeterson
 *  Added two convenience constructors to create the parameter
 *  with an int.
 *
 *  Revision 1.1  2002/06/06 16:14:34  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;
import javax.swing.*;
import java.util.Vector;
import java.lang.Integer;
import java.beans.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.util.*;

/**
 * This is a superclass to take care of many of the common details of
 * IntegerPGs.
 */
public class IntegerPG extends StringPG{
    private static String TYPE="Integer";

    // ********** Constructors **********
    public IntegerPG(String name, Object value){
        super(name,value);
        this.type=TYPE;
    }

    public IntegerPG(String name, Object value, boolean valid){
        super(name,value,valid);
        this.type=TYPE;
    }

    public IntegerPG(String name, int value){
        this(name, new Integer(value));
    }

    public IntegerPG(String name, int value, boolean valid){
        this(name, new Integer(value), valid);
    }

    // ********** IParameter requirements **********

    /** 
     * Override the default method.
     */
    public Object getValue(){
        Object val=super.getValue();
        if(val instanceof String){
            return new Integer((String)val);
        }else if(val instanceof Integer){
            return (Integer)val;
        }else{
            return null;
            // throw an exception
        }
    }

    /**
     * Returns a primitive integer version of the value.
     */
    public int getintValue(){
        return ((Integer)this.getValue()).intValue();
    }

    /**
     * Overrides the default behavior.
     */
    public void setValue(Object value){
        super.setValue(value);
        if(!this.initialized){
            if(value instanceof Integer){
                this.value=value;
            }else if(value instanceof String){
                this.value=new Integer((String)value);
            }else{
                // should throw an exception
            }
        }
    }

    /**
     * Convenience method to allow for passing a primitive int.
     */
    public void setintValue(int value){
        this.setValue(new Integer(value));
    }

    // ********** IParameterGUI requirements **********
    /**
     * Allows for initialization of the GUI after instantiation.
     */
    public void init(Vector init_values){
        if(init_values!=null){
            if(init_values.size()==1){
                // the init_values is what to set as the value of the parameter
                this.setValue(init_values.elementAt(0));
            }else{
                // something is not right, should throw an exception
            }
        }
        //entrywidget=new IntegerField(this.getintValue(),20);
        entrywidget=new StringEntry(this.getStringValue(),20,
                                    new IntegerFilter());
        entrywidget.addPropertyChangeListener(IParameter.VALUE, this);
        this.setEnabled(this.getEnabled());
        this.packupGUI();
        this.initialized=true;
    }

    /**
     * Set the enabled state of the EntryWidget. This produces a more
     * pleasant effect that the default setEnabled of the widget.
     */
    /*public void setEnabled(boolean enabled){
      this.enabled=enabled;
      if(this.getEntryWidget()!=null){
      ((JTextField)this.entrywidget).setEditable(enabled);
      }
      // need to add stuff for actually enabling
      }*/

    static void main(String args[]){
        IntegerPG fpg;

        fpg=new IntegerPG("a",new Integer(1));
        System.out.println(fpg);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new IntegerPG("b",new Integer(10));
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new IntegerPG("c",new Integer(100),false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new IntegerPG("d",new Integer(1000),true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.init();
        fpg.showGUIPanel();

    }

    /**
     * Definition of the clone method.
     */
    public Object clone(){
        IntegerPG pg=new IntegerPG(this.name,this.value,this.valid);
        pg.setDrawValid(this.getDrawValid());
        pg.initialized=false;
        return pg;
    }
}

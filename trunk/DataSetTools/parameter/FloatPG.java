/*
 * File:  FloatPG.java 
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
 *  Revision 1.1  2002/06/06 16:14:30  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;
import javax.swing.*;
import java.util.Vector;
import java.lang.Float;
import java.beans.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.util.*;

/**
 * This is class is to deal with float parameters.
 */
public class FloatPG extends StringPG{
    private static String TYPE="Float";

    // ********** Constructors **********
    public FloatPG(String name, Object value){
        super(name,value);
        this.type=TYPE;
    }
    
    public FloatPG(String name, Object value, boolean valid){
        super(name,value,valid);
        this.type=TYPE;
    }
    
    /**
     * Override the default method.
     */
    public Object getValue(){
        Object val=super.getValue();
        if(val instanceof String){
            return new Float((String)val);
        }else if(val instanceof Float){
            return (Float)val;
        }else{
            return null;
            // throw an exception
        }
    }

    /**
     * Convenience method to get the proper type value right away.
     */
    public float getfloatValue(){
        return ((Float)this.getValue()).floatValue();
    }

    /**
     * Overrides the default version of setValue to properly deal with
     * floats.
     */
    public void setValue(Object value){
        super.setValue(value);
        if(!this.initialized){
            if(value instanceof Float){
                this.value=value;
            }else if(value instanceof String){
                this.value=new Float((String)value);
            }else{
                // should throw an exception
            }
        }
    }

    /**
     * Convenience method to set the proper type value right away.
     */
    public void setfloatValue(float value){
        this.setValue(new Float(value));
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
        entrywidget=new StringEntry(this.getStringValue(),20,
                                    new FloatFilter());
        entrywidget.addPropertyChangeListener(IParameter.VALUE, this);
        this.setEnabled(this.getEnabled());
        this.packupGUI();
        this.initialized=true;
    }

    static void main(String args[]){
        FloatPG fpg;

        fpg=new FloatPG("a",new Float(1f));
        System.out.println(fpg);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new FloatPG("b",new Float(10f));
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new FloatPG("c",new Float(1000f),false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new FloatPG("d",new Float(100f),true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.init();
        fpg.showGUIPanel();

    }
}

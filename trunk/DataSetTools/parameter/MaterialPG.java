/*
 * File:  MaterialPG.java 
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
 *  Revision 1.2  2002/09/30 15:20:53  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.1  2002/06/06 16:14:35  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;
import java.util.Vector;
import java.beans.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.util.*;

/**
 * This is class is to deal with float parameters.
 */
public class MaterialPG extends StringPG{
    private static String TYPE="Material";

    // ********** Constructors **********
    public MaterialPG(String name, Object value){
        super(name,value);
        this.type=TYPE;
    }
    
    public MaterialPG(String name, Object value, boolean valid){
        super(name,value,valid);
        this.type=TYPE;
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
        entrywidget=new StringEntry(this.getStringValue(),20);
        entrywidget.addPropertyChangeListener(IParameter.VALUE, this);
        this.setEnabled(this.getEnabled());
        this.packupGUI();
        this.initialized=true;
    }

    static void main(String args[]){
        MaterialPG fpg;

        fpg=new MaterialPG("a","1f");
        System.out.println(fpg);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new MaterialPG("b","10f");
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new MaterialPG("c","100f",false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new MaterialPG("d","1000f",true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.init();
        fpg.showGUIPanel();

    }

    /**
     * Definition of the clone method.
     */
    public Object clone(){
        return (MaterialPG)super.clone();
    }
}

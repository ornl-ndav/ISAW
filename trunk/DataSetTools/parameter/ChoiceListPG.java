/*
 * File:  ChoiceListPG.java 
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
 *  Revision 1.3  2002/10/07 15:27:34  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.2  2002/09/30 15:20:44  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.1  2002/08/01 18:40:02  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;

import DataSetTools.dataset.*;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.util.SharedData;

/**
 * This is a superclass to take care of many of the common details of
 * Array Parameter GUIs.
 */
public class ChoiceListPG extends ArrayPG{
    // static variables
    private   static String TYPE     = "ChoiceList";
    protected static int    DEF_COLS = ArrayPG.DEF_COLS;

    // ********** Constructors **********
    public ChoiceListPG(String name, Object value){
        super(name,value);
        this.type=TYPE;
        if(!(value instanceof String))
            SharedData.addmsg("WARN: Non-String"
                              +" in ChoiceListPG constructor");
    }

    public ChoiceListPG(String name, Object value, boolean valid){
        super(name,value,valid);
        this.type=TYPE;
        if(!(value instanceof String))
            SharedData.addmsg("WARN: Non-String"
                              +" in ChoiceListPG constructor");
    }

    // ********** Methods to deal with the hash **********

    /**
     * Add a single DataSet to the vector of choices. This calls the
     * superclass's method once it confirms the value to be added is a
     * DataSet.
     */
    public void addItem( Object val){
        if(val instanceof String) super.addItem(val);
    }

    /**
     * Main method for testing purposes.
     */
    static void main(String args[]){
        ChoiceListPG fpg;
        int y=0, dy=70;

        String[] choices=new String[5];
        choices[0]="a";
        choices[1]="b";
        choices[2]="c";
        choices[3]="d";
        choices[4]="e";

        fpg=new ChoiceListPG("a",choices[0]);
        System.out.println(fpg);
        fpg.init(choices);
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new ChoiceListPG("b",choices[0]);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init(choices);
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new ChoiceListPG("c",choices[0],false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init(choices);
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new ChoiceListPG("d","q",true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.init(choices);
        fpg.showGUIPanel(0,y);
        y+=dy;

    }

    /**
     * Definition of the clone method.
     */
    public Object clone(){
        ChoiceListPG pg=new ChoiceListPG(this.name,this.value,this.valid);
        pg.setDrawValid(this.getDrawValid());
        pg.initialized=false;
        return pg;
    }
}

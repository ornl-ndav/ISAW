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
 *  Revision 1.8  2003/06/03 22:00:28  rmikk
 *  Checked for a null initial value before reporting the
 *     incorrect data type warning
 *
 *  Revision 1.7  2003/06/02 22:09:27  bouzekc
 *  Modified clone() to work with a Vector of FileFilters.
 *
 *  Revision 1.6  2003/02/24 20:59:14  pfpeterson
 *  Now extends ChooserPG rather than ArrayPG.
 *
 *  Revision 1.5  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/10/10 22:12:56  pfpeterson
 *  Fixed a bug with the clone method not getting the choices copied
 *  over. Also simplified a constructor and implemented AddItems
 *  rather than depending on inheritence.
 *
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
import java.util.Vector;

/**
 * This class represents a parameter where there is a list of Strings
 * to choose from.
 */
public class ChoiceListPG extends ChooserPG{
    // static variables
    private   static String TYPE     = "ChoiceList";
    protected static int    DEF_COLS = ChooserPG.DEF_COLS;

    // ********** Constructors **********
    public ChoiceListPG(String name, Object value){
        this(name,value,false);
        this.setDrawValid(false);
        this.type=TYPE;
    }

    public ChoiceListPG(String name, Object value, boolean valid){
        super(name,value,valid);
        this.type=TYPE;
        if( value != null)
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

    public void addItems( Vector values){
        Object obj;
        for( int i=0 ; i<values.size() ; i++ ){
            obj=values.elementAt(i);
            if(obj!=null) this.addItem(obj);
        }
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
        if((this.vals) == null)
          pg.vals = null;
        else
          pg.vals=(Vector)this.vals.clone();
        pg.setDrawValid(this.getDrawValid());
        pg.initialized=false;
        return pg;
    }
}

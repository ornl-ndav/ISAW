/*
 * File:  DataDirPG.java 
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
 *  Revision 1.4  2002/10/10 19:15:13  pfpeterson
 *  Fixed a bug where the Data_Directory was not used if a value
 *  was not specified.
 *
 *  Revision 1.3  2002/10/07 15:27:35  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.2  2002/09/30 15:20:45  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.1  2002/07/15 21:26:07  pfpeterson
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
import DataSetTools.util.SharedData;

/**
 * This is a particular case of the BrowsePG used for loading a single
 * file. The value is a string.
 */
public class DataDirPG extends BrowsePG{
    private static String TYPE     = "DataDir";

    // ********** Constructors **********
    public DataDirPG(String name, Object value){
        this(name,value,false);
        this.setDrawValid(false);
    }
    
    public DataDirPG(String name, Object value, boolean valid){
        super(name,value,valid);
        if(value!=null && ((String)value).length()>0){
            File file=new File((String)value);
            if(file.exists()){
                if(file.isFile()){
                    this.setValue(file.getParent());
                }
            }
        }else{
            this.setValue(SharedData.getProperty("Data_Directory"));
        }
        this.type=TYPE;
    }

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
        innerEntry=new StringEntry(this.getStringValue(),BrowsePG.VIS_COLS);
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

    static void main(String args[]){
        DataDirPG fpg;
        int y=0, dy=70;
        String defString="/IPNShome/pfpeterson/IsawProps.dat";

        fpg=new DataDirPG("a",defString);
        System.out.println(fpg);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new DataDirPG("b",defString);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new DataDirPG("c",defString,false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new DataDirPG("d",defString,true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.init();
        fpg.showGUIPanel(0,y);

    }

    /**
     * Definition of the clone method.
     */
    public Object clone(){
        DataDirPG pg=new DataDirPG(this.name,this.value,this.valid);
        pg.setDrawValid(this.getDrawValid());
        pg.initialized=false;
        return pg;
    }
}

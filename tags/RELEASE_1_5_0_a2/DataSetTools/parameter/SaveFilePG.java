/*
 * File:  SaveFilePG.java 
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
 *  Revision 1.6  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.5  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/10/23 18:50:45  pfpeterson
 *  Now supports a javax.swing.filechooser.FileFilter to be specified
 *  for browsing options. Also fixed bug where it did not automatically
 *  switch to the data directory if no value was specified.
 *
 *  Revision 1.3  2002/10/07 15:27:47  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.2  2002/09/30 15:20:57  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.1  2002/07/15 21:26:09  pfpeterson
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
 * This is a particular case of the BrowsePG used for loading a single
 * file. The value is a string.
 */
public class SaveFilePG extends BrowsePG{
    private static String TYPE     = "SaveFile";

    // ********** Constructors **********
    public SaveFilePG(String name, Object value){
        this(name,value,false);
        this.setDrawValid(false);
    }
    
    public SaveFilePG(String name, Object value, boolean valid){
        super(name,value,valid);
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
                                  BrowseButtonListener.SAVE_FILE,this.filter));
        entrywidget=new JPanel();
        entrywidget.add(innerEntry);
        entrywidget.add(browse);
        this.setEnabled(this.getEnabled());
        super.initGUI();
    }

    static void main(String args[]){
        SaveFilePG fpg;
        int y=0, dy=70;
        String defString="/IPNShome/pfpeterson/IsawProps.dat";

        fpg=new SaveFilePG("a",defString);
        System.out.println(fpg);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new SaveFilePG("b",defString);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new SaveFilePG("c",defString,false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new SaveFilePG("d",defString,true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.init();
        fpg.showGUIPanel(0,y);

    }

    /**
     * Definition of the clone method.
     */
    public Object clone(){
        SaveFilePG pg=new SaveFilePG(this.name,this.value,this.valid);
        pg.setDrawValid(this.getDrawValid());
        pg.initialized=false;
        pg.filter=this.filter;
        return pg;
    }
}

/*
 * File: StringEntry.java
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
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.3  2003/06/10 14:49:15  pfpeterson
 *  Fix a null pointer exception in setStringFilter.
 *
 *  Revision 1.2  2002/11/27 23:12:35  pfpeterson
 *  standardized header
 *
 *  Revision 1.1  2002/06/06 16:09:25  pfpeterson
 *  Added to CVS.
 *
 *
 */
 
package DataSetTools.components.ParametersGUI;

import javax.swing.*; 
import javax.swing.text.*; 
//import java.awt.Toolkit;
import java.util.Locale;
import java.beans.*;
import DataSetTools.parameter.*;
import DataSetTools.util.*;
import DataSetTools.util.StringFilterer;

/**
 * This class is intended to be used as a replacement for JTextField
 * whan a integer value is to be entered. The major difference is an
 * overridden insertString method which beeps when something that
 * isn't found in an integer is entered.
 */
public class StringEntry extends JTextField implements //StringFilterer, 
                                                       PropertyChanger{
    // private Toolkit toolkit;
    private PropertyChangeSupport propBind=new PropertyChangeSupport(this);
    private StringFilterer filter=new StringFilter();
    private FilteredDocument filterdoc;

    /**
     * Constructs an StringEntry with the appropriate number of
     * columns and the default value of zero.
     */
    public StringEntry(int columns){
        this("",columns);
        //System.out.println("StringEntry(int)");
    }

    /**
     * Constructs an StringEntry with a specified default value and
     * number of columns.
     */
    public StringEntry(String value, int columns) {
        this(value,columns,new StringFilter());
        //System.out.println("StringEntry(String, int)");
    }

    /**
     * Full constructor for the StringEntry widget.
     */
    public StringEntry(String value, int columns, StringFilterer sf){
        super(columns);
        //System.out.println("a");
        //this.toolkit=Toolkit.getDefaultToolkit();
        this.setStringFilter(sf);
        //System.out.println("b");
        this.filterdoc.setPropBind(this.propBind);
        //System.out.println("c");
        super.setText(value);
        //System.out.println("d");
        //System.out.println("StringEntry(String, int, StringFilterer)");
        //System.out.println("e");
    }

    /**
     * Mutator method to change the filter from the default value
     * after instantiation.
     */
    public void setStringFilter(StringFilterer sf){
        if(sf==null)
          sf=new StringFilter();
        this.filter=sf;
        this.filterdoc.setFilter(sf);
    }

    /**
     * Acessor method to get the filter being used.
     */
    public StringFilterer getStringFilter(){
        return this.filter;
    }

    /** 
     * A hook to override the insertString method.
     */
    protected Document createDefaultModel() {
        //System.out.println("createDefaultModel");
        this.filterdoc=new FilteredDocument(this,this.filter,this.propBind);
        
        return this.filterdoc; //new FilteredDocument(this,this.filter,this.propBind);
    }

    // ********** Methods for the PropertyChanger interface
    public void addPropertyChangeListener(String prop,
                                          PropertyChangeListener pcl){
        super.addPropertyChangeListener(prop,pcl);
        if(propBind!=null) propBind.addPropertyChangeListener(prop,pcl);
    }
    public void addPropertyChangeListener(PropertyChangeListener pcl){
        super.addPropertyChangeListener(pcl);
        if(propBind!=null) propBind.addPropertyChangeListener(pcl);
    }
    public void removePropertyChangeListener(PropertyChangeListener pcl){
        super.removePropertyChangeListener(pcl);
        if(propBind!=null) propBind.removePropertyChangeListener(pcl);
    }
}

/*
 * File: FilteredDocument.java
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
 *           9700 S. Cass Avenue, Bldg 360
 *           Argonne, IL 60440
 *           USA
 *
 * For further information, see http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2002/06/06 16:09:24  pfpeterson
 *  Added to CVS.
 *
 *
 *
 */
 
package DataSetTools.components.ParametersGUI;

import javax.swing.*; 
import javax.swing.text.*; 
import java.awt.Toolkit;
import java.util.Locale;
import java.beans.*;
import DataSetTools.parameter.*;
import DataSetTools.util.*;

/**
 * Internal class to do all of the formatting checks and pass out
 * PropertChange events to listeners. Should only be used from within
 * the package.
 */
class FilteredDocument extends PlainDocument {
    private StringEntry           textBox;
    private Toolkit               toolkit;
    private PropertyChangeSupport propBind;
    private StringFilterer        filter;
    
    /**
     * The constructor allows for connecting back to the original
     * JTextBox.
     */
    public FilteredDocument(StringEntry T, StringFilterer sf, 
                            PropertyChangeSupport pb){
        super();
        this.textBox  = T;
        this.toolkit  = Toolkit.getDefaultToolkit();
        this.setPropBind(pb);
        this.filter   = sf; //setFilter(sf);
    }
    
    /**
     * Overrides the default insertString method. Insert if okay,
     * beep if not.
     */
    public void insertString(int offs, String str, AttributeSet a) 
        throws BadLocationException {
        //System.out.println("insertString("+offs+", "+str+"): "+filter);

        if(filter==null){
            super.insertString(offs,str,a);
            return;
        }
        String oldText=textBox.getText();
        str=this.filter.modifyString(offs,str,oldText);
        if(this.filter.isOkay(offs,str,textBox.getText())){
            super.insertString(offs,str,a);
            if(propBind!=null)
                this.fireValueChange(oldText,textBox.getText());
            /*propBind.firePropertyChange(IParameter.VALUE,
              oldText,textBox.getText());*/
        }else{
            toolkit.beep();
        }
    }
    
    /** 
     * Overrides the default remove method.
     */
    public void remove(int offs, int len) throws BadLocationException{
        String oldText=textBox.getText();
        super.remove(offs,len);
        //System.out.println("value:"+oldText+"->"+textBox.getText());
        if(propBind!=null)
            this.fireValueChange(oldText,textBox.getText());
        /*propBind.firePropertyChange(IParameter.VALUE,
          oldText,textBox.getText());*/
    }

    private void fireValueChange(String oldValue, String newValue){
        if(!oldValue.equals(newValue)){
            propBind.firePropertyChange(IParameter.VALUE,oldValue,newValue);
        }
    }

    /**
     * Set the StringFilter.
     */
    void setFilter(StringFilterer sf){
        this.filter=sf;
    }
    /**
     * Set what fires off the PropertyChangedEvents.
     */
    void setPropBind(PropertyChangeSupport pb){
        this.propBind=pb;
    }
}

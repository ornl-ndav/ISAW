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
 *  Revision 1.5  2003/08/14 19:11:48  bouzekc
 *  Made inner Toolkit transient.
 *
 *  Revision 1.4  2002/11/27 23:12:34  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/06/20 15:26:43  pfpeterson
 *  Modified to be used from JTextComponents rather than StringEntries.
 *
 *  Revision 1.2  2002/06/14 14:17:58  pfpeterson
 *  Added more checks before firing a PropertyChangeEvent.
 *
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
    private JTextComponent        textBox;
    private transient Toolkit     toolkit;
    private PropertyChangeSupport propBind;
    private StringFilterer        filter;
    
    /**
     * The constructor allows for connecting back to the original
     * JTextComponent.
     */
    public FilteredDocument(JTextComponent T, StringFilterer sf, 
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
        if(propBind!=null)
            this.fireValueChange(oldText,textBox.getText());
    }

    private void fireValueChange(String oldValue, String newValue){
        if(propBind!=null && newValue!=null && newValue.length()>0 
           && !oldValue.equals(newValue) ){
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

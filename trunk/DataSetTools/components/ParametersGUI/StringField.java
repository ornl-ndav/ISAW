/*
 * File: StringField.java
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
 *  Revision 1.1  2002/05/31 19:32:10  pfpeterson
 *  Added to CVS.
 *
 *  Revision 1.1  2002/03/08 16:19:49  pfpeterson
 *  Added to CVS.
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

/**
 * This class is intended to be used as a replacement for JTextField
 * whan a integer value is to be entered. The major difference is an
 * overridden insertString method which beeps when something that
 * isn't found in an integer is entered.
 */
public class StringField extends JTextField {
    private Toolkit toolkit;
    private PropertyChangeSupport propBind=new PropertyChangeSupport(this);

    /**
     * Constructs an StringField with the appropriate number of
     * columns and the default value of zero.
     */
    public StringField(int columns){
        this("",columns);
    }

    /**
     * Constructs an StringField with a specified default value and
     * number of columns.
     */
    public StringField(String value, int columns) {
        super(value,columns);
        toolkit = Toolkit.getDefaultToolkit();
    }

    /** 
     * A hook to override the insertString method.
     */
    protected Document createDefaultModel() {
        return new StringDocument(this);
    }

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

    /**
     * Internal class to do all of the formatting checks.
     */
    protected class StringDocument extends PlainDocument {
        private StringField textBox;
        public StringDocument(StringField T){
            super();
            textBox=T;
        }

        /**
         * Overrids the default insertString method. Insert if okay,
         * beep if not.
         */
        public void insertString(int offs, String str, AttributeSet a) 
            throws BadLocationException {
            
            String oldText=textBox.getText();
            if(isOkay(offs,str,textBox.getText())){
                super.insertString(offs,str,a);
                if(propBind!=null)
                    propBind.firePropertyChange(IParameter.VALUE,
                                                oldText,textBox.getText());
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
                propBind.firePropertyChange(IParameter.VALUE,
                                            oldText,textBox.getText());
        }

        /**
         * Internal method to confirm that the text can be added.
         */
        private boolean isOkay(int offs, String inString, String curString){
            return true;
        }
    }
}

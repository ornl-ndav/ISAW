/*
 * File: FloatField.java
 *
 * Copyright (C) 2002, Peter Peterson
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
 *  Revision 1.7  2004/01/22 01:35:12  bouzekc
 *  Removed and/or commented out unused variables.
 *
 *  Revision 1.6  2003/12/14 19:20:41  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.5  2003/08/14 19:11:48  bouzekc
 *  Made inner Toolkit transient.
 *
 *  Revision 1.4  2002/11/27 23:12:35  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/06/06 16:06:49  pfpeterson
 *  Reorganized some of the code and class hierarchy.
 *
 *  Revision 1.2  2002/05/31 19:32:45  pfpeterson
 *  Now fire PropertyChangeEvent when the value in widget is changed.
 *
 *  Revision 1.1  2002/03/08 16:19:47  pfpeterson
 *  Added to CVS.
 *
 *
 */
 
package DataSetTools.components.ParametersGUI;

import javax.swing.*; 
import javax.swing.text.*; 
import java.awt.Toolkit;
import java.beans.*;
import DataSetTools.parameter.*;

/**
 * This class is intended to be used as a replacement for JTextField
 * when a float value is to be entered. The major difference is an
 * overridden insertString method which beeps when something that
 * isn't found in a float is entered.
 */
public class FloatField extends JTextField {
    private transient Toolkit toolkit;
    private PropertyChangeSupport propBind=new PropertyChangeSupport(this);

    private static Character MINUS =new Character((new String("-")).charAt(0));
    //private static Character PLUS  =new Character((new String("+")).charAt(0));
    private static Character DEC   =new Character((new String(".")).charAt(0));
    private static Character E     =new Character((new String("E")).charAt(0));

    /**
     * Constructs a FloatField with the appropriate number of columns
     * and the default value of zero.
     */
    public FloatField(int columns){
        this(0f,columns);
    }

    /**
     * Constructs a FloatField with a specified default value and
     * number of columns.
     */
    public FloatField(float value, int columns) {
        super((new Float(value)).toString(),columns);
        toolkit = Toolkit.getDefaultToolkit();
    }

    /**
     * A hook to override the insertString method.
     */
    protected Document createDefaultModel() {
        return new FloatDocument(this);
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
     * Internal method to confirm that the text can be added.
     */
    private boolean isOkay(int offs, String inString, String curString){
        char[] source = inString.toCharArray();
        for( int i=0 ; i < source.length ; i++ ){
            if(Character.isDigit(source[i])){
                // do nothing
            }else if(DEC.compareTo(new Character(source[i]))==0){
                if(curString.indexOf(DEC.toString())>=0){
                    return false;
                }else{
                    int index=curString.indexOf(E.toString());
                    if(index>=0){
                        if(offs+i>index){
                            return false;
                        }else{
                            // do nothing
                        }
                    }else{
                        // do nothing
                    }
                }
                /* }else if(PLUS.compareTo(new Character(source[i]))==0){
                   int pi=curString.indexOf(PLUS.toString());
                   int ei=curString.indexOf(E.toString());
                   if(pi>=0){
                   return false;
                   }else{
                   if(ei>=0){
                   if(offs+i==ei+1){
                   // do nothing
                   }else{
                   return false;
                   }
                   }else{
                   return false;
                   }
                   }*/
            }else if(MINUS.compareTo(new Character(source[i]))==0){
                int mi=curString.indexOf(MINUS.toString());
                int ei=curString.indexOf(E.toString());
                if(ei>=0){ // allow two minuses
                    if(offs+i==0){
                        if(offs+i==mi){
                            return false;
                        }else{
                            // do nothing
                        }
                    }else if(offs+i==ei+1){
                        if(mi==0){
                            mi=curString.indexOf(MINUS.toString(),mi+1);
                        }
                        if(offs+i==mi){
                            return false;
                        }else{
                            
                        }
                    }else{
                        return false;
                    }
                }else{     // allow only one minus
                    if(offs+i==0 && mi<0){
                        // do nothing
                    }else{
                        return false;
                    }
                }
                // do nothing
            }else if(E.compareTo(new Character(source[i]))==0){
                if(curString.indexOf(E.toString())>=0){
                    return false;
                }else if( offs==0 && i==0 ){
                    return false;
                }else{
                    if(offs+i<=curString.indexOf(DEC.toString())){
                        return false;
                    }else{
                        // do nothing
                    }
                }
            }else{
                return false;
            }
        }
        
        return true;
    }

    /**
     * Internal class to do all of the formatting checks.
     */
    protected class FloatDocument extends PlainDocument {
        private FloatField textBox;
        public FloatDocument(FloatField T){
            super();
            textBox=T;
        }

        /**
         * Overrides the default insertString method. Insert if okay,
         * beep if not.
         */
        public void insertString(int offs, String str, AttributeSet a) 
            throws BadLocationException {

            String oldText=textBox.getText();
            str=str.toUpperCase();
            if(textBox.isOkay(offs,str,textBox.getText())){
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

    }
}

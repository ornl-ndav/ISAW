/*
 * File: IntegerField.java
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

/**
 * This class is intended to be used as a replacement for JTextField
 * whan a integer value is to be entered. The major difference is an
 * overridden insertString method which beeps when something that
 * isn't found in an integer is entered.
 */
public class IntegerField extends JTextField {
    private Toolkit toolkit;

    private static Character MINUS =new Character((new String("-")).charAt(0));
    private static Character ZERO  =new Character((new String("0")).charAt(0));

    /**
     * Constructs an IntegerField with the appropriate number of
     * columns and the default value of zero.
     */
    public IntegerField(int columns){
        this(0,columns);
    }

    /**
     * Constructs an IntegerField with a specified default value and
     * number of columns.
     */
    public IntegerField(int value, int columns) {
        super((new Integer(value)).toString(),columns);
        toolkit = Toolkit.getDefaultToolkit();
    }

    /** 
     * A hook to override the insertString method.
     */
    protected Document createDefaultModel() {
        return new WholeNumberDocument(this);
    }

    /**
     * Internal class to do all of the formatting checks.
     */
    protected class WholeNumberDocument extends PlainDocument {
        private IntegerField textBox;
        public WholeNumberDocument(IntegerField T){
            super();
            textBox=T;
        }

        /**
         * Overrids the default insertString method. Insert if okay,
         * beep if not.
         */
        public void insertString(int offs, String str, AttributeSet a) 
            throws BadLocationException {
            
            if(isOkay(offs,str,textBox.getText())){
                super.insertString(offs,str,a);
            }else{
                toolkit.beep();
            }
        }

        private boolean isOkay(int offs, String inString, String curString){
            char[]    source  = inString.toCharArray();

            for( int i=0 ; i < source.length ; i++ ){
                if(Character.isDigit(source[i])){
                    if(ZERO.compareTo(new Character(source[i]))==0){
                        if( offs+i==0 && curString.length()>0 ){
                            return false;
                        }else if(curString.startsWith(MINUS.toString())){
                            if( offs+i==1 ){
                                return false;
                            }else{
                                // do nothing
                            }
                        }else{
                            // do nothing
                        }
                    }else{
                        // do nothing
                    }
                }else if(MINUS.compareTo(new Character(source[i]))==0){
                    if(offs+i==0){
                        if(curString.startsWith(MINUS.toString())){
                            return false;
                        }else{
                            // do nothing
                        }
                    }else{
                        return false;
                    }
                }else{
                    return false;
                }
            }

            return true;
        }
    }
}

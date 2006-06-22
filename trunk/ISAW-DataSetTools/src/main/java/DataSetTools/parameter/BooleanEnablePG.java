
/* File:  BooleanEnablePG.java 
 *
 * Copyright (C) 2006, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <Mikkelsonr@uwstout.edu>
 *           University of Wisconsin-Stout
 *           Menomonie, Wisconsin 54751
 *           USA
 *
  * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
*
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.1  2006/03/16 22:48:40  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.parameter;
import java.util.*;
import Command.JavaCC.*;
import java.beans.*;


/**
 * This ParameterGUI influences the enabled status of several of the
 * ParameterGUI's following this one in the parameterlist of an operator
 * or in a Vector of parameters.
 * 
 * It has one parameter consisting of a vector of 3 values:
 *    1.  This initial truth value 
 *    2.  The number of parameterGUI's following this one in a list that
 *        are set to true if this GUI's value is true and false if this GUI's
 *        value is false
 * 
 *     
 *    3.  The number of parameterGUI's after the previous ones in a list that
 *        are set to false if this GUI's value is true and true if this GUI's
 *        value is false
 * @author MikkelsonR
 *
 */
public class BooleanEnablePG extends BooleanPG {
    
    int nSetIfTrue = 0;
    int nSetIfFalse = 0;
    boolean value = false;
    private static final String type = "BooleanEnable";
    /**
     * This constructor is a BooleanPG with the added Information to
     * determine which other parameterGUI's are to be enabled if true and 
     * disabledif false
     * @param name   The prompt
     * @param value  Should be a Vector containing the intial value( True or 
     *               False), an Integer for the number of Parameters following
     *               this one that will be enabled if true(disabled if false)
     *               then another integer indicating the number of parameters 
     *               following the first batch that are disabled when the value
     *               is true and enabled when the value is false
     *               
     *               If it is not a Vector with three elements, this is just a
     *               regular BooleanPG
     */
    public BooleanEnablePG(String name, Object value) {
        this(name, value, true);
        
    }

    /**
     * Constructor 
     * @param name   The prompt used to get the value
     * @param value   It should be a Vector[ init value,nSetIfTrue,nSetIfFalse]
     * @param valid   The initial valid state
     * @see  #Constructor(String,Object) other constructor
     */
    public BooleanEnablePG(String name, Object value, boolean valid) {
        super(name, null, valid);
        this.setType( type);
        this.setValue( value);        
          
    }
    
   
    
    
    /**
     * 
     * @return the number of parameters immediately after this 
     *         parameter that are enabled if the value is
     *         True, otherwise it is disabled
     */
    public int getNSetIfTrue(){
        return nSetIfTrue;
    }
    
 
    
    /**
     * 
     * @return the number of parameters immediately after this 
     *         parameter and the ones that are set true that are 
     *         disabled if the value is True, otherwise it is enabled
     */
 
    public int getNSetIfFalse(){
        return nSetIfFalse;
    }
    
   /**
    *  Converts the value to boolean and sets the value both
    *  in the value variable in a superclass along with the
    *  value showing in the associated GUI, if initialized
    *  
    *  @param value   the value to be set or false if it cannot 
    *                 be converted to a boolean
    */
    public void setValue(Object value) {
        if (value == null)
            return;
        if (value instanceof String) {
            try {
                value = (Object) ParameterGUIParser.parseText((String) value);
            } catch (ParseException s) {
                value = null;
                super.setValue(new Boolean(false));
                return;
            }
        }

        if (!(value instanceof Vector)) {
            super.setValue(value);
            return;
        }
        Vector V = (Vector) value;
        if (V.size() > 0)
            super.setValue(V.elementAt(0));
        if (V.size() > 1)
            try {
                nSetIfTrue = ((Integer) (V.elementAt(1))).intValue();
                nSetIfFalse = ((Integer) (V.elementAt(2))).intValue();
            } catch (Exception ss) {

            }

    }
   
   
    public Object clone() {
        Vector V = new Vector();
        V.addElement(getValue());
        V.addElement(new Integer(nSetIfTrue));
        V.addElement(new Integer(nSetIfFalse));
        return new BooleanEnablePG(getName(), V);

    }

    public void fire() {
        firePropertyChange(new PropertyChangeEvent(this, "NONAME", new Boolean(
                false), new Boolean(true)));
    }
}

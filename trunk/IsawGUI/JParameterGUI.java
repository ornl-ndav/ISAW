/*
 * @(#)Isaw.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 *
 */

package IsawGUI;

import javax.swing.*;

import DataSetTools.*;
import DataSetTools.operator.*;

abstract public class JParameterGUI
{
    Parameter parameter;
    public JParameterGUI(Parameter parameter)
    { 
       this.parameter = parameter;
    }
    
    public Parameter getParameter()
    {
        return parameter;
    }
    
    abstract public JPanel getGUISegment();
}

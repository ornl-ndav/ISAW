/*
 * @(#)JFloatParameterGUI.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package IsawGUI;

import javax.swing.*;
//import javax.swing.*;
import DataSetTools.*;
import DataSetTools.operator.*;
import java.awt.*;
import java.util.zip.*;
import java.io.Serializable;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JFloatParameterGUI extends JParameterGUI implements Serializable
{
    private JPanel segment;
    private JTextArea floatText;
    
    public JFloatParameterGUI(Parameter parameter)
    { 
       super(parameter);
       
       String value = ((Float)parameter.getValue()).toString();
       floatText = new JTextArea();
       floatText.setText(value);
       segment = new JPanel();
       segment.setLayout(new GridLayout(1,2));
       segment.add(new JLabel(parameter.getName()));
       segment.add(floatText);
   
    }
    
    
    
    public JPanel getGUISegment()
    {
        return segment;
        
    }


    public Parameter getParameter()
    {
        String s = floatText.getText();
        Float value = new Float(s);
        parameter.setValue(value);
        return parameter;
    }

}

/*
 * @(#)JStringParameterGUI.java     1.0  99/09/02  Alok Chatterjee
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
import java.io.Serializable;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JStringParameterGUI extends JParameterGUI implements Serializable
{
    private JPanel segment;
    private JTextArea stringText;
    
    public JStringParameterGUI(Parameter parameter)
    { 
       super(parameter);
       
       String value = ((String)parameter.getValue()).toString();
       stringText = new JTextArea();
       stringText.setText(value);
       segment = new JPanel();
       segment.setLayout(new GridLayout(1,2));
       segment.add(new JLabel(parameter.getName()));
       segment.add(stringText);
   
    }
    
    
    
    public JPanel getGUISegment()
    {
        return segment;
        
    }


    public Parameter getParameter()
    {
        String s = stringText.getText();
        String value = new String(s);
        parameter.setValue(value);
        return parameter;
    }

}

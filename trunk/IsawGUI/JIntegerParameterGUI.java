/*
 * @(#)JIntParameterGUI.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package IsawGUI;

import javax.swing.*;
import javax.swing.border.*;
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

public class JIntegerParameterGUI extends JParameterGUI implements Serializable
{
    private JPanel segment;
    private JTextField intText;
    
    public JIntegerParameterGUI(Parameter parameter)
    { 
       super(parameter);
       
       String value = ((Integer)parameter.getValue()).toString();
       JLabel label = new JLabel(parameter.getName());
       label.setPreferredSize(new Dimension(170,25));
       intText = new JTextField(20);
       intText.setText(value);
       segment = new JPanel();
       segment.setLayout(new FlowLayout(FlowLayout.CENTER, 70, 5));
       segment.add(label);
       segment.add(intText);
   
    }
    
    
    
    public JPanel getGUISegment()
    {
        return segment;
        
    }


    public Parameter getParameter()
    {
        String s = intText.getText();
        Integer value = new Integer(s);
        parameter.setValue(value);
        return parameter;
    }

}

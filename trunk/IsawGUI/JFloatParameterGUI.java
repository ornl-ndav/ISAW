/*
 * @(#)JFloatParameterGUI.java     1.0  99/09/02  Alok Chatterjee
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
import java.util.zip.*;
import java.io.Serializable;

/**
 * Class used to create a Float parameter type
 *
 * @version 1.0  
 */



public class JFloatParameterGUI extends JParameterGUI implements Serializable
{
    private JPanel segment;
    private JTextField floatText;
    
    public JFloatParameterGUI(Parameter parameter)
    { 
       super(parameter);
       
       String value = ((Float)parameter.getValue()).toString();
       JLabel label = new JLabel(parameter.getName());
       label.setPreferredSize(new Dimension(170,25));
       floatText = new JTextField(20);
       floatText.setText(value);
       segment = new JPanel();
       segment.setLayout(new FlowLayout(FlowLayout.CENTER, 70, 5)); 
       segment.add(label);
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

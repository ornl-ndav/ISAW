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
import java.awt.*;
import java.io.Serializable;

public class JIntegerParameterGUI extends JParameterGUI implements Serializable
{
    private JPanel segment;
    private JTextArea intText;
    
    public JIntegerParameterGUI(Parameter parameter)
    { 
       super(parameter);
       
       String value = ((Integer)parameter.getValue()).toString();
       intText = new JTextArea();
       intText.setText(value);
       segment = new JPanel();
       segment.setLayout(new GridLayout(1,2));
       segment.add(new JLabel(parameter.getName()));
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

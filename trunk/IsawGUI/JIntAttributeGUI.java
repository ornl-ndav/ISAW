/*
 * @(#)JIntAttributeGUI.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package IsawGUI;

import javax.swing.*;
//import javax.swing.*;
import DataSetTools.*;
import DataSetTools.dataset.*;
import java.awt.*;
import java.util.zip.*;
import java.io.Serializable;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JIntAttributeGUI extends JAttributeGUI implements Serializable
{
    private JPanel segment;
    private JTextArea integerText;
    public JIntAttributeGUI(IntAttribute attr)
    { 
       super(attr);
       
       String value = ((Integer)attr.getValue()).toString();
       integerText = new JTextArea();
       integerText.setText(value);
       segment = new JPanel();
       segment.setLayout(new GridLayout(1,2));
       segment.add(new JLabel(attr.getName()));
       segment.add(integerText);
   
    }
    
    
    
    public JPanel getGUISegment()
    {
        return segment;
        
    }


    public Attribute getAttribute()
    {
        
        String s = integerText.getText();
        if(s.equalsIgnoreCase("NaN"))
        attr = new IntAttribute(attr.getName(),(int)attr.getNumericValue());
        else{
        int value = Integer.valueOf(s).intValue();
        ((IntAttribute)attr).setIntValue(value);
        }
        return attr;
    }

}

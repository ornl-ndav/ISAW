/*
 * @(#)JFloatAttributeGUI.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package IsawGUI;

import javax.swing.*;
import javax.swing.border.*;
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

public class JFloatAttributeGUI extends JAttributeGUI implements Serializable
{
    private JPanel segment;
    private JTextField floatText;
    public JFloatAttributeGUI(FloatAttribute attr)
    { 
       super(attr);
       
       String value = ((Float)attr.getValue()).toString();
       JLabel label = new JLabel(attr.getName() );
       label.setPreferredSize(new Dimension(150,25));
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


    public Attribute getAttribute()
    {
        
        String s = floatText.getText();
        if(s.equalsIgnoreCase("NaN"))
        attr = new FloatAttribute(attr.getName(),Float.NaN);
        else{
        float value = Float.valueOf(s).floatValue();
        ((FloatAttribute)attr).setFloatValue(value);
        }
        return attr;
    }

}

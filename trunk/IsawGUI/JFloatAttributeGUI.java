/*
 * @(#)Isaw.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 *
 */

package IsawGUI;

import javax.swing.*;
import DataSetTools.*;
import DataSetTools.dataset.*;
import java.awt.*;
import java.util.zip.*;
import java.io.Serializable;

public class JFloatAttributeGUI extends JAttributeGUI implements Serializable
{
    private JPanel segment;
    private JTextArea floatText;
    public JFloatAttributeGUI(FloatAttribute attr)
    { 
       super(attr);
       
       String value = ((Float)attr.getValue()).toString();
       floatText = new JTextArea();
       floatText.setText(value);
       segment = new JPanel();
       segment.setLayout(new GridLayout(1,2));
       segment.add(new JLabel(attr.getName()));
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

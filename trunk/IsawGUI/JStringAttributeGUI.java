/*
 * @(#)JStringAttributeGUI.java     1.0  99/09/02  Alok Chatterjee
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

public class JStringAttributeGUI extends JAttributeGUI implements Serializable
{
    private JPanel segment;
    private JTextField stringText;
    public JStringAttributeGUI(StringAttribute attr)
    { 
       super(attr);
       
       String value = ((String)attr.getValue()).toString();
       JLabel label = new JLabel(attr.getName());
       label.setPreferredSize(new Dimension(170,25));
       stringText = new JTextField(20);
       stringText.setText(value);
       segment = new JPanel();
       segment.setLayout(new FlowLayout(FlowLayout.CENTER, 70, 5)); 
       segment.add(label);
       segment.add(stringText);
   
    }
    
    
    
    public JPanel getGUISegment()
    {
        return segment;
        
    }


    public Attribute getAttribute()
    {
        
        String s = stringText.getText();
        if(s.equalsIgnoreCase("NaN"))
        attr = new StringAttribute(attr.getName(),(String)attr.getStringValue());
        else{
        //String value = String.valueOf(s).S
        ((StringAttribute)attr).setStringValue(s);
        }
        return attr;
    }

}
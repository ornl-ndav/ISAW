/*
 * @(#)JIntAttributeGUI.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package IsawGUI;

import javax.swing.*;
import javax.swing.border.*;
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
    private JTextField integerText;
    public JIntAttributeGUI(IntAttribute attr)
    { 
       super(attr);
       
       String value = ((Integer)attr.getValue()).toString();
       JLabel label = new JLabel(attr.getName() );
       label.setPreferredSize(new Dimension(150,25));
       integerText = new JTextField(20);
       integerText.setText(value);
       segment = new JPanel();
       segment.setLayout(new FlowLayout(FlowLayout.CENTER, 70, 5));
       segment.add(label);
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
        attr = new IntAttribute(attr.getName(),value);
        }
        return attr;
    }

}

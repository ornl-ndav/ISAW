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

public class JStringAttributeGUI extends JAttributeGUI implements Serializable
{
    private JPanel segment;
    private JTextArea stringText;
    public JStringAttributeGUI(StringAttribute attr)
    { 
       super(attr);
       
       String value = ((String)attr.getValue()).toString();
       stringText = new JTextArea();
       stringText.setText(value);
       segment = new JPanel();
       segment.setLayout(new GridLayout(1,2));
       segment.add(new JLabel(attr.getName()));
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

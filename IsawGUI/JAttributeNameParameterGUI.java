/*
 * @(#)JAttributeNameParameterGUI.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
package IsawGUI;

import javax.swing.*;
//import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.tree.*;
import javax.swing.JTree.*;
import DataSetTools.util.*;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JAttributeNameParameterGUI extends JParameterGUI
{
    private JPanel segment;
    private JTextField dsText;
    private JComboBox combobox;
    public JAttributeNameParameterGUI(Parameter parameter, AttributeList attr_list)
    { 
        super(parameter);
        combobox = new JComboBox();
        combobox.setEditable(true);
        //combobox.addItem(ds);
        JLabel label = new JLabel(parameter.getName());
       label.setPreferredSize(new Dimension(150,25));

        for(int i = 0; i<attr_list.getNum_attributes(); i++)
        {
            Attribute attr = attr_list.getAttribute(i);
            combobox.addItem(attr.getName());
        }
        
        segment = new JPanel();
        segment.setLayout(new FlowLayout(FlowLayout.CENTER, 70, 5)); 
       
        segment.add(label);
        segment.add(combobox);
        
       
    }

    public JPanel getGUISegment()
    {
        return segment;
        
    }


	 public Parameter getParameter()
    {

        Class C = parameter.getValue().getClass();
        try{
           SpecialString X = (SpecialString)(C.newInstance());
           X.setString ((String)(combobox.getSelectedItem()));
           parameter.setValue(X );
           }
        catch( Exception s)
         {}
        return parameter;
    }



  }

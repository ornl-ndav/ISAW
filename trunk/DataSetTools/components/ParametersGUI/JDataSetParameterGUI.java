/*
 * @(#)JDataSetParameterGUI.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package DataSetTools.components.ParametersGUI;

import javax.swing.*;
//import javax.swing.*;
import DataSetTools.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.tree.*;
import javax.swing.JTree.*;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JDataSetParameterGUI extends JParameterGUI
{
    private JPanel    segment;
    private JTextArea dsText;
    private JComboBox combobox;
    private DataSet   DS[];

    public JDataSetParameterGUI(Parameter parameter, DataSet new_ds[] )
    { 
       super(parameter);
       
        combobox = new JComboBox();
        //combobox.addItem(ds);
        JLabel label = new JLabel(parameter.getName());
       label.setPreferredSize(new Dimension(150,25));

        DS = new_ds;
        if( DS == null)
           return;

        for( int i = 0; i < DS.length ; i++)
          { combobox.addItem( DS[i]);
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
       if( DS == null )
         {return parameter;
           }
       DataSet ds = (DataSet)combobox.getSelectedItem();
        parameter.setValue(ds);
        return parameter;
    }

}


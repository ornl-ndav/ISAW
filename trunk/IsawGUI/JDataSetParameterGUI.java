/*
 * @(#)JDataSetParameterGUI.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package IsawGUI;

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
    private JPanel segment;
    private JTextArea dsText;
    private JComboBox combobox;
    public JDataSetParameterGUI(Parameter parameter, JTreeUI jtui)
    { 
       super(parameter);
       
        combobox = new JComboBox();
        //combobox.addItem(ds);
        
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)jtui.getTree().getModel().getRoot();
        int n_containers = root.getChildCount();
  
        for(int i = 0; i<n_containers; i++)
        {
            DefaultMutableTreeNode  container =(DefaultMutableTreeNode)root.getChildAt(i);
            //DefaultMutableTreeNode  container =(DefaultMutableTreeNode)jtui.getSelectedNode();
            int n_children = container.getChildCount();
            for(int k = 0; k < n_children; k++)
            {
                DefaultMutableTreeNode  child =(DefaultMutableTreeNode)container.getChildAt(k);
                DataSet ds = (DataSet)child.getUserObject();
                combobox.addItem(ds);
            }
        }
        
        segment = new JPanel();
        segment.setLayout(new GridLayout(1,2));
       
        segment.add(new JLabel(parameter.getName()));
        segment.add(combobox);
        
       
    }

    public JPanel getGUISegment()
    {
        return segment;
        
    }

    public Parameter getParameter()
    {
       
       DataSet ds = (DataSet)combobox.getSelectedItem();
        parameter.setValue(ds);
        return parameter;
    }

}

/*
 * @(#)JlocDataSetParameterGUI.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package Command;

import javax.swing.*;
//import javax.swing.*;
//import DataSetTools.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.tree.*;
import javax.swing.JTree.*;
import Command.*;
import IsawGUI.*;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JlocDataSetParameterGUI extends JParameterGUI
{
    private JPanel segment;
    private JTextArea dsText;
    private JComboBox combobox;
    public JlocDataSetParameterGUI(Parameter parameter , DataSet ds[])
    { 
       super(parameter);
       
        combobox = new JComboBox();
    
        //combobox.addItem(ds);
        
       
  
        if( ds == null)
	  {
           System.out.println("Global data set is null");
          }          
        else 
          for( int i=0; i< ds.length; i++)
	    {              
               combobox.addItem(ds[i]);
                     
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
    { Parameter P;
       
       DataSet ds = (DataSet)combobox.getSelectedItem();
       
	  
       P = super.getParameter();
       
          P.setValue(ds);
          return P;
	  
       
      
    }

}

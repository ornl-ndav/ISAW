/*
 * @(#)JAttributesDialog.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package IsawGUI;

import javax.swing.*;
import DataSetTools.dataset.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.File;
import java.io.*;
import javax.swing.JDialog.*;
import java.util.zip.*;
import java.io.Serializable.* ;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JAttributesDialog implements Serializable
{

    Vector vparamGUI = new Vector();
    JDialog opDialog;
    JLabel resultsLabel = new JLabel("Result");
    DataSet ds;
    Attribute attr;
    JAttributeGUI attrGUI;
    AttributeList attr_list;
    
    public JAttributesDialog(AttributeList attr_list, String nam)
    {
        this.attr_list = attr_list;
        opDialog = new JDialog(new JFrame(), nam ,true);
        opDialog.setSize(620,400);
        opDialog.getContentPane().add(new JLabel(nam +" to be performed on Selected Node"));
       
        //Center the opdialog frame 
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    Dimension size = opDialog.getSize();
	    screenSize.height = screenSize.height/2;
	    screenSize.width = screenSize.width/2;
	    size.height = size.height/2;
	    size.width = size.width/2;
	    int y = screenSize.height - size.height;
	    int x = screenSize.width - size.width;
	    opDialog.setLocation(x, y);
  
        int num_attributes = attr_list.getNum_attributes();
        opDialog.getContentPane().setLayout(new GridLayout(num_attributes+4,1));
      
        for (int i = 0; i<num_attributes; i++)
        {  
       //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%     
            
            attr = attr_list.getAttribute(i);
           System.out.println("The attributes in attr_list are " +attr);
           if (attr instanceof FloatAttribute)
           {     attrGUI = new JFloatAttributeGUI((FloatAttribute)attr);
           // else //if(param.getValue() instanceof DataSet)
           //     paramGUI = new JDataSetParameterGUI(param, jtui);
            //Add other kinds of parameter types here.
            
            opDialog.getContentPane().add(attrGUI.getGUISegment());
            vparamGUI.addElement(attrGUI);
           }
           
           if (attr instanceof IntAttribute)
           {     attrGUI = new JIntAttributeGUI((IntAttribute)attr);
            opDialog.getContentPane().add(attrGUI.getGUISegment());
            vparamGUI.addElement(attrGUI);
           }
           
           if (attr instanceof StringAttribute)
           {     attrGUI = new JStringAttributeGUI((StringAttribute)attr);
            opDialog.getContentPane().add(attrGUI.getGUISegment());
            vparamGUI.addElement(attrGUI);
           }
                    
         //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 
            
        }
                 opDialog.getContentPane().add(resultsLabel);
                 JPanel buttonpanel = new JPanel();
                 buttonpanel.setLayout(new FlowLayout());
                 JButton apply = new JButton("Apply");
                 JButton exit = new JButton("Exit");
                 buttonpanel.add(apply);
                 apply.addActionListener(new ApplyButtonHandler());
                 buttonpanel.add(exit);
                 exit.addActionListener(new ExitButtonHandler());
                 opDialog.getContentPane().add(buttonpanel);
                 opDialog.setVisible(true);
      }
      
       public AttributeList getAttributeList()
       {
        return attr_list;
       }
     
      public class ApplyButtonHandler implements ActionListener
      {
         public void actionPerformed(ActionEvent ev) 
         {
             JAttributeGUI aGUI;
             System.out.println(" vparam size is  " +vparamGUI.size());
             for(int i = 0; i<vparamGUI.size(); i++)
             {
                aGUI = (JAttributeGUI)vparamGUI.elementAt(i);
               Attribute attr = (Attribute)aGUI.getAttribute();
               attr_list.setAttribute(attr);
               
             }            
            resultsLabel.setText("Result: Operation completed");
         }
      } 
    
    public class ExitButtonHandler implements ActionListener
    {
         public void actionPerformed(ActionEvent ev) {
              opDialog.dispose();     
    } 
    }
}

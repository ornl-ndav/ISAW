/*
 * @(#)Isaw.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 *
 */

package IsawGUI;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.*;

public class JEditAttributes implements Serializable
{
     JTable table;
     JDialog opDialog;
     
	 public JEditAttributes()
	 {
        DefaultTableModel dtm = new DefaultTableModel();
        table = new JTable(dtm);
        opDialog = new JDialog(new JFrame(), "Attributes can be edited here",true);
        opDialog.setSize(200,250);
        opDialog.getContentPane().add(new JLabel("Please Edit Operations Here"));
        //opDialog.getContentPane().setLayout(new BorderLayout());

     
        Vector col = new Vector();
	    col.addElement("Attribute"); col.addElement("Value");
	    Vector data = new Vector();
	    dtm = new DefaultTableModel(data, col);
	    table.setModel(dtm);
	    
	             JPanel buttonpanel = new JPanel();
                 buttonpanel.setLayout(new FlowLayout());
                 JButton apply = new JButton("Apply");
                 JButton exit = new JButton("Exit");
                 buttonpanel.add(apply);
                 apply.addActionListener(new ApplyButtonHandler());
                 buttonpanel.add(exit);
                 exit.addActionListener(new ExitButtonHandler());
                 opDialog.getContentPane().add(buttonpanel);

        
	 }  //end of constructor
	 
	 
	 public void editAttributes(AttributeList attr_list)
	 {
	    Vector heading = new Vector();
	    heading.addElement("Attribute"); 
	    heading.addElement("Value");
	    Vector data = new Vector();
	    for (int i=0; i<attr_list.getNum_attributes(); i++)
	    {
	        Attribute attr = attr_list.getAttribute(i);
	       
	        Vector oo = new Vector();
	        oo.addElement(attr.getName()); 
	        oo.addElement(attr.getStringValue());
	      
	        data.addElement(oo);
	    }
	    	 DefaultTableModel dtm = new DefaultTableModel(data, heading);
	         table.setModel(dtm);
	         	    opDialog.getContentPane().add(table);
	                opDialog.show();
	 }
	 
	  public class ApplyButtonHandler implements ActionListener
      {
         public void actionPerformed(ActionEvent ev) {
              //  optionPane.setValue(btnString1);
             // System.out.println("buttonpressed" +ev);
        /*     JParameterGUI pGUI;
             for(int i = 0; i<op.getNum_parameters(); i++)
             {
                pGUI = (JParameterGUI)vparamGUI.elementAt(i);
                op.setParameter(pGUI.getParameter(), i);
             
             }
             
             opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             DataSet result = (DataSet)(op.getResult());
             opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
           if (result == null)
           {System.out.println("Result was null  :");
            
                resultsLabel.setText("Operation ' " +op.getTitle()+ " ' failed");
                //resultsLabel.setBackground(new java.awt.Color(225, 225, 0));
          //      resultsLabel.setBackground(new java.awt.Color(100,10,10));
          }
          else 
            {
                DataSet[] dss = new DataSet[1];
                dss[0] = (DataSet)result;
        
            //**
             
        //  System.out.println("The new Title is before   :" +result.getTitle());
                    //treeUI.addDataSets(dss, "Operation Result");
                    jtui.addDataSet(result);
            //**
                 
                System.out.println("The new Title is    :" +result.getTitle());
                resultsLabel.setText("Operation ' " +op.getTitle()+ " '  completed");
                //resultsLabel.setBackground(new java.awt.Color(225, 225, 0));
              //  resultsLabel.setBackground(new java.awt.Color(100,10,10));
            }*/
            }
      } 
    
    public class ExitButtonHandler implements ActionListener
    {
         public void actionPerformed(ActionEvent ev) {
              opDialog.dispose();     
    } 
    }
	 
}


/*
 * File: JEditAttributes.java
 *
 * Copyright (C) 1999, Alok Chatterjee
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.4  2002/11/27 23:27:07  pfpeterson
 * standardized header
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

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

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


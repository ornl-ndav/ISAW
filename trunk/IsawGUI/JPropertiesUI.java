/*
 * @(#)JPropertiesGUI.java     1.0  99/09/02  Alok Chatterjee
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

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.*;
import java.util.zip.*; 
import java.io.Serializable;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JPropertiesUI extends JPanel implements Serializable
{
     JTable table;

	 public JPropertiesUI()
	 {
           DefaultTableModel dtm = new DefaultTableModel();
           table = new JTable(dtm);
          // table.setPreferredSize(new Dimension(50,50));
          //  table.setMinimumSize(new Dimension(50,50));
           setLayout(new GridLayout(1,1) );
           JScrollPane scrollPane = new JScrollPane(table);
           add( scrollPane );   
           showAttributes( new AttributeList() );
	 }  
	 
	 
	 public void showAttributes(AttributeList attr_list)
	 {
	    Vector heading = new Vector();
	    heading.addElement("Attribute" ); 
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
             table.setSize( 30, 30 );     // the numbers used don't seem to
                                          // be important, but setting the 
                                          // size get's the table to fill out
                                          // the available space.
	 }
}


/*
 * File:  JAttributesDialog.java
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
 * Contact : Alok Chatterjee achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 S. Cass Avenue, Bldg 360
 *           Argonne, IL 60440
 *           USA
 *
 * 
 *
 * For further information, see http://www.pns.anl.gov/ISAW/>
 * 
 * Modified:
 *
 *  $Log$
 *  Revision 1.6  2002/04/05 17:57:21  pfpeterson
 *  Fixed the layout of the Change Attributes dialog and GUIs it depends on.
 *
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
 * This is the class for a dialog to edit attributes for a DataSet or
 * DataBlock.
 */

public class JAttributesDialog implements Serializable{

    Vector vparamGUI = new Vector();
    JDialog opDialog;
    JLabel resultsLabel = new JLabel(" Result:");
    DataSet ds;
    Attribute attr;
    JAttributeGUI attrGUI;
    AttributeList attr_list;
    
    /**
     * Builds and displays the JAttributesDialog GUI.
     *
     * @param attr_list Attribute list to be modified.
     * @param nam       Title of the dialog box.
     */
    public JAttributesDialog(AttributeList attr_list, String nam){
        this.attr_list = attr_list;
        opDialog = new JDialog(new JFrame(), nam ,true);
        opDialog.setSize(650,450);
        //Container contents=opDialog.getContentPane();
        JPanel contents=new JPanel();
        contents.setLayout(new GridBagLayout());
        opDialog.getContentPane().add(contents);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.weightx   = 1.0;
        gbc.weighty   = 2.0;
        gbc.anchor    = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        //contents.setBackground(Color.red);

        contents.add(new JLabel(" "+nam 
                                +" to be performed on Selected Node"),gbc);
        
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
        
        // get the number of attributes to loop through
        int num_attributes = attr_list.getNum_attributes();


        // create a frame to insert the attribute stuff in.
        JPanel atts=new JPanel();
        atts.setLayout(new BoxLayout(atts,BoxLayout.Y_AXIS));
        JScrollPane scrollpane=new JScrollPane(atts,
                           ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weighty = 30.0;
        contents.add(scrollpane,gbc);


        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%     
        for (int i = 0; i<num_attributes; i++){  
            attr = attr_list.getAttribute(i);
            //System.out.println("The attributes in attr_list are " +attr);
            if (attr instanceof FloatAttribute){
                attrGUI = new JFloatAttributeGUI((FloatAttribute)attr);
                atts.add(attrGUI.getGUISegment());
                vparamGUI.addElement(attrGUI);
            }else if (attr instanceof IntAttribute){
                attrGUI = new JIntAttributeGUI((IntAttribute)attr);
                atts.add(attrGUI.getGUISegment());
                vparamGUI.addElement(attrGUI);
            }else if (attr instanceof StringAttribute){
                attrGUI = new JStringAttributeGUI((StringAttribute)attr);
                atts.add(attrGUI.getGUISegment());
                vparamGUI.addElement(attrGUI);
            }
            //Add other kinds of parameter types here.
        }
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 

        // put the result label in
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.SOUTHWEST;
        gbc.weighty = 1.0;
        contents.add(resultsLabel,gbc);
        
        // build the set of buttons at the bottom of the dialog
        JPanel buttonpanel = new JPanel();
        buttonpanel.setLayout(new FlowLayout());
        JButton apply = new JButton("Apply");
        JButton exit = new JButton("Exit");
        buttonpanel.add(apply);
        apply.addActionListener(new ApplyButtonHandler());
        buttonpanel.add(exit);
        exit.addActionListener(new ExitButtonHandler());
        gbc.weighty = 1.0;
        contents.add(buttonpanel,gbc);

        opDialog.setVisible(true);
    } // end of JAttributesDialog(AttributeList, String)
    
    /**
     * Accessor method to get the current list of Attributes.
     */
    public AttributeList getAttributeList(){
        return attr_list;
    }
     
    /**
     * Internal class to set all of the attributes in the
     * AttributeList once the 'Set' button is pressed.
     */
    public class ApplyButtonHandler implements ActionListener{
        public void actionPerformed(ActionEvent ev){
            JAttributeGUI aGUI;
            //System.out.println(" vparam size is  " +vparamGUI.size());
            for(int i = 0; i<vparamGUI.size(); i++){
                aGUI = (JAttributeGUI)vparamGUI.elementAt(i);
                Attribute attr = (Attribute)aGUI.getAttribute();
                attr_list.setAttribute(attr);
                
            }            
            resultsLabel.setText(" Result: Operation completed");
        }
    } 
    
    /**
     * Internal class to destroy the dialog.
     */
    public class ExitButtonHandler implements ActionListener{
        public void actionPerformed(ActionEvent ev) {
            opDialog.dispose();     
        } 
    }
}

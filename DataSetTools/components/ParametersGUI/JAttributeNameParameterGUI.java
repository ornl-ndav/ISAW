/*
 * File: JAttributeNameParameterGUI.java 
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
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.8  2002/11/27 23:12:34  pfpeterson
 *  standardized header
 *
 *  Revision 1.7  2002/03/08 16:20:39  pfpeterson
 *  Added method to disable the GUIs. This is to help out wizards.
 *
 */
package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import java.awt.*;
import java.awt.event.*;
import DataSetTools.util.*;

public class JAttributeNameParameterGUI extends JParameterGUI
{
    private JPanel     segment;
    private JTextField dsText;
    private JComboBox  combobox;


    public JAttributeNameParameterGUI( Parameter     parameter, 
                                       AttributeList attr_list )
    { 
       super(parameter);
       combobox = new JComboBox();
       combobox.setEditable(true);
       JLabel label = new JLabel("  "+parameter.getName());
       //label.setPreferredSize(new Dimension(150,25));

       for(int i = 0; i<attr_list.getNum_attributes(); i++)
       {
          Attribute attr = attr_list.getAttribute(i);
          combobox.addItem(attr.getName());
       }
       combobox.setSelectedItem( parameter.getValue().toString() );
       segment = new JPanel();
       segment.setLayout(new GridLayout( 1,2)); 
       
       segment.add(label);
       segment.add(combobox);
    }


    public JPanel getGUISegment()
    {
        return segment;
    }


    public void setEnabled(boolean en){
        this.combobox.setEnabled(en);
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

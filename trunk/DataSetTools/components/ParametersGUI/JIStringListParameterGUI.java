/*
 * File: JAttributeNameParameterGUI.java 
 *
 * Copyright (C) 2001, Ruth Mikkelson,
 *                     Alok Chatterjee
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
 * For further information, see http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.4  2001/08/16 01:03:46  rmikk
 *  Set the Selected member of the Combobox to be the
 *  parameters value .toString() so that it is a string
 *
 *  Revision 1.3  2001/08/15 02:08:30  rmikk
 *  Set the selected item of the combo box to the value of
 *  the parameter
 *
 *  Revision 1.2  2001/08/07 20:58:36  rmikk
 *  Eliminated setPreferred size and set segment layout to a
 *  grid layout
 *
 *  Revision 1.1  2001/08/06 22:16:59  rmikk
 *  New File: Handles all the IStringList parameter Values
 *
 * 
 *
 */
package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import java.awt.*;
import java.awt.event.*;
import DataSetTools.util.*;

public class JIStringListParameterGUI extends JParameterGUI
{
    private JPanel     segment;
    private JTextField dsText;
    private JComboBox  combobox;


    public JIStringListParameterGUI( Parameter     parameter, 
                                     IStringList   str_list )
    { 
       super(parameter);
       combobox = new JComboBox();
       combobox.setEditable(true);
       JLabel label = new JLabel("  "+parameter.getName());
       //label.setPreferredSize(new Dimension(150,25));

       for(int i = 0; i< str_list.num_strings(); i++)                 
          combobox.addItem(str_list.getString( i ));     
       //Note: cannot set Selected using parameter's value
       combobox.setSelectedItem( parameter.getValue().toString());
       segment = new JPanel();
       segment.setLayout(new GridLayout( 1, 2)); 
       
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
           if( ! X.getClass().equals( C ))
	       System.out.println("Class Mismatch in JIStringListParameter" );
           }
        catch( Exception s)
         { System.out.println("Exception occurred "+ s);}
        return parameter;
    }
}

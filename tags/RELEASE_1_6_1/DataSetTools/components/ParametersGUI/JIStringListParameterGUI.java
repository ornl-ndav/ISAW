/*
 * File: JAttributeNameParameterGUI.java 
 *
 * Copyright (C) 2001, Ruth Mikkelson, Alok Chatterjee
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
 *  Revision 1.9  2004/01/22 01:41:27  bouzekc
 *  Removed unused variables and unused imports.
 *
 *  Revision 1.8  2002/11/27 23:12:35  pfpeterson
 *  standardized header
 *
 *  Revision 1.7  2002/03/08 16:20:43  pfpeterson
 *  Added method to disable the GUIs. This is to help out wizards.
 *
 *  Revision 1.6  2002/02/11 21:34:20  rmikk
 *  Fixed a bug that occured with the new StringChoiceList Parameters.
 *  This was a major change in the algorithm
 *
 *  Revision 1.5  2001/11/27 18:39:07  dennis
 *  Set Editable to false. Only members of the list can be selected.(Ruth)
 *
 */
package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import DataSetTools.operator.*;
import java.awt.*;
import DataSetTools.util.*;

public class JIStringListParameterGUI extends JParameterGUI
{
    private JPanel     segment;
    private JComboBox  combobox;


    public JIStringListParameterGUI( Parameter     parameter, 
                                     IStringList   str_list )
    { 
       super(parameter);
       combobox = new JComboBox();
       combobox.setEditable(false);
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


    public void setEnabled(boolean en){
        this.combobox.setEnabled(en);
    }

    public Parameter getParameter()
    {  
        /*Class C = parameter.getValue().getClass();
        
        try{
           SpecialString X = (SpecialString)(C.newInstance());            
           X.setString ((String)(combobox.getSelectedItem()));          
           parameter.setValue(X );           
           if( ! X.getClass().equals( C ))
	       System.out.println("Class Mismatch in JIStringListParameter" );
           }
        catch( Exception s)
         { System.out.println("Exception occurred "+ s);}
        */
        String s = (String)(combobox.getSelectedItem());
        ((SpecialString)(parameter.getValue())).setString(s);
        return parameter;
    }
}

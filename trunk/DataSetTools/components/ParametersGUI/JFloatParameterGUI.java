/*
 * File: JFloatParameterGUI.java
 *
 * Copyright (C) 1999, Alok Chatterjee, Dennis Mikkelson
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
 *  Revision 1.3  2001/06/26 18:37:31  dennis
 *  Added Copyright and GPL license.
 *  Removed un-needed imports and improved
 *  code format.
 *
 */
 
package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import DataSetTools.operator.*;
import java.awt.*;
import java.io.*;


public class JFloatParameterGUI extends    JParameterGUI 
                                implements Serializable
{
    private JPanel segment;
    private JTextField floatText;
    
    public JFloatParameterGUI(Parameter parameter)
    { 
       super(parameter);
       
       String value = ((Float)parameter.getValue()).toString();
       JLabel label = new JLabel(parameter.getName());
       label.setPreferredSize(new Dimension(170,25));
       floatText = new JTextField(20);
       floatText.setText(value);
       segment = new JPanel();
       segment.setLayout(new FlowLayout(FlowLayout.CENTER, 70, 5)); 
       segment.add(label);
       segment.add(floatText);
    }
    
    public JPanel getGUISegment()
    {
        return segment;   
    }


    public Parameter getParameter()
    {
        String s = floatText.getText();
        Float value = new Float(s);
        parameter.setValue(value);
        return parameter;
    }
}

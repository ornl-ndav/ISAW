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
 *  Revision 1.6  2002/11/27 23:12:35  pfpeterson
 *  standardized header
 *
 *  Revision 1.5  2002/03/08 16:20:42  pfpeterson
 *  Added method to disable the GUIs. This is to help out wizards.
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
    private FloatField floatText;
    
    public JFloatParameterGUI(Parameter parameter)
    { 
       super(parameter);
       
       String value = ((Float)parameter.getValue()).toString();
       JLabel label = new JLabel("  "+parameter.getName());
       //label.setPreferredSize(new Dimension(170,25));
       floatText = new FloatField(20);
       floatText.setText(value);
       segment = new JPanel();
       segment.setLayout(new GridLayout( 1, 2 )); 
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

    /**
     * Enable the parameter GUI.
     */
    public void setEnabled(boolean en){
        this.floatText.setEditable(en);
    }
}

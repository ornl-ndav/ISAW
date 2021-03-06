/*
 * File: JBooleanParameterGUI.java
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
 *  Revision 1.6  2004/01/22 01:41:27  bouzekc
 *  Removed unused variables and unused imports.
 *
 *  Revision 1.5  2002/11/27 23:12:34  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/03/08 16:20:40  pfpeterson
 *  Added method to disable the GUIs. This is to help out wizards.
 *
 */
 
package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import DataSetTools.operator.*;
import java.awt.*;
import java.io.*;


public class JBooleanParameterGUI extends    JParameterGUI 
                                  implements Serializable
{
    private JPanel segment;
    private JCheckBox jcb;

    public JBooleanParameterGUI(Parameter parameter)
    { 
       super(parameter);
       boolean value = ((Boolean)parameter.getValue()).booleanValue();
       segment = new JPanel();
       segment.setLayout(new GridLayout(1,2));
       segment.add(new JLabel(""));
       jcb = new JCheckBox(parameter.getName(),value);
       segment.add(jcb);
    }

    public JPanel getGUISegment()
    {
        return segment;
    }

    public void setEnabled(boolean en){
        this.jcb.setEnabled(en);
    }

    public Parameter getParameter()
    {
        boolean val = jcb.isSelected();
        Boolean value = new Boolean(val);
        parameter.setValue(value);
        return parameter;
    }
}

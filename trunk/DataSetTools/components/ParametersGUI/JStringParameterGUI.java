/*
 * File: JStringParameterGUI.java
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
 *  Revision 1.5  2001/08/06 22:15:52  rmikk
 *  Expanded to take care of the Special Strings that were not
 *  IStringLists
 *
 *  Revision 1.4  2001/08/06 20:16:59  rmikk
 *  Adjusted this to take care of the IntListString parameter
 *
 *  Revision 1.3  2001/06/26 18:37:43  dennis
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
import DataSetTools.util.*;

public class JStringParameterGUI extends JParameterGUI implements Serializable
{
    private JPanel segment;
    private JTextField stringText;
    
    public JStringParameterGUI(Parameter parameter)
    { 
       super(parameter);
       String value;
       
           value = parameter.getValue().toString();
      
     
       JLabel label = new JLabel(parameter.getName());
       label.setPreferredSize(new Dimension(170,25));
       stringText = new JTextField(20);
       stringText.setText(value);
       segment = new JPanel();
       segment.setLayout(new FlowLayout(FlowLayout.CENTER, 70, 5)); 
       segment.add(label);
       segment.add(stringText);
    }
    
    
    public JPanel getGUISegment()
    {
        return segment;
    }


    public Parameter getParameter()
    {  String s = stringText.getText();
       if( parameter.getValue() instanceof String)
          {          
           String value = new String(s);        
           parameter.setValue(value);
          
           return parameter;
          } 

        Class C = parameter.getValue().getClass();
        try{
           SpecialString X = (SpecialString)(C.newInstance());
           X.setString ( s );
           parameter.setValue(X );
           if( ! X.getClass().equals( C ))
	       System.out.println("Class Mismatch in JIStringListParameter" );
          
           }
        catch( Exception e )
         {}
        return parameter;
    }
}







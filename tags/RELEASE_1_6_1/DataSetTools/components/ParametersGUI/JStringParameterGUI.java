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
 *  Revision 1.9  2002/11/27 23:12:35  pfpeterson
 *  standardized header
 *
 *  Revision 1.8  2002/04/18 22:16:19  pfpeterson
 *  Changed the setEnabled() method to disable more appropriately.
 *
 *  Revision 1.7  2002/03/08 16:20:49  pfpeterson
 *  Added method to disable the GUIs. This is to help out wizards.
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
       String value = parameter.getValue().toString();
  
       JLabel label = new JLabel("  "+parameter.getName());      
       stringText = new JTextField(20);
       stringText.setText(value);
       segment = new JPanel();
       segment.setLayout(new GridLayout( 1, 2 )); 
       segment.add(label);
       segment.add(stringText);
    }
    
    
    public JPanel getGUISegment()
    {
        return segment;
    }


    public void setEnabled(boolean en){
        this.stringText.setEditable(en);
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







/*
 * File: JStringAttributeGUI.java
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
 * $Log$
 * Revision 1.7  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 */
 
package IsawGUI;

import javax.swing.*;
//import javax.swing.*;
import DataSetTools.dataset.*;
import java.awt.*;
import java.util.zip.*;
import java.io.Serializable;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JStringAttributeGUI extends JAttributeGUI implements Serializable
{
    private JPanel segment;
    private JTextField stringText;
    public JStringAttributeGUI(StringAttribute attr)
    { 
       super(attr);
       
       String value = ((String)attr.getValue()).toString();
       JLabel label = new JLabel(attr.getName());
       label.setPreferredSize(new Dimension(150,25));
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


    public Attribute getAttribute()
    {
        
      String s = stringText.getText();
      if(s.equalsIgnoreCase("NaN"))
      attr = new StringAttribute(attr.getName(),(String)attr.getStringValue());
      else{
      //String value = String.valueOf(s).S
      attr = new StringAttribute(attr.getName(),s);
      }
      return attr;
    }

}

/*
 * File: JDataSetParameterGUI.java
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
 *  Revision 1.5  2001/08/15 02:08:38  rmikk
 *  Set the selected item of the combo box to the value of
 *  the parameter
 *
 *  Revision 1.4  2001/08/07 20:57:13  rmikk
 *  Eliminated setPreferred Size and changed segments layout
 *  to a Grid Layout
 *
 *  Revision 1.3  2001/06/26 18:37:28  dennis
 *  Added Copyright and GPL license.
 *  Removed un-needed imports and improved
 *  code format.
 *
 */
 
package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import java.awt.*;

public class JDataSetParameterGUI extends JParameterGUI
{
    private JPanel    segment;
    private JTextArea dsText;
    private JComboBox combobox;
    private DataSet   DS[];

    public JDataSetParameterGUI(Parameter parameter, DataSet new_ds[] )
    { 
       super(parameter);
       
       combobox = new JComboBox();
       JLabel label = new JLabel("  "+parameter.getName());
       //label.setPreferredSize(new Dimension(150,25));

       DS = new_ds;
       if( DS == null)
          return;

       for( int i = 0; i < DS.length ; i++)
       { 
         combobox.addItem( DS[i]);
       }
       combobox.setSelectedItem( parameter.getValue() );
       segment = new JPanel();
       segment.setLayout(new GridLayout( 1, 2 )); 
      
       segment.add(label);
       segment.add(combobox);
    }

    public JPanel getGUISegment()
    {
        return segment;
    }

    public Parameter getParameter()
    {
      if( DS == null )
      {
        return parameter;
      }
      DataSet ds = (DataSet)combobox.getSelectedItem();
      parameter.setValue(ds);
      return parameter;
    }
}

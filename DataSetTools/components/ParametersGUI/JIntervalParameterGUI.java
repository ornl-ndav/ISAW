/*
 * File: JIntervalParameterGUI.java
 *
 * Copyright (C) 2001 Kevin Neff
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *

 *
 * $Log$
 * Revision 1.4  2002/11/27 23:12:34  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/03/08 16:20:45  pfpeterson
 * Added method to disable the GUIs. This is to help out wizards.
 *
 */

package DataSetTools.components.ParametersGUI;


import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JTextField;
import javax.swing.JPanel;
import DataSetTools.operator.Parameter;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;
import DataSetTools.util.SpecialString;
import javax.swing.JComboBox;

public class JIntervalParameterGUI
  extends JParameterGUI
{
  private JPanel     segment;
  private JTextField text;
  private JComboBox  combobox;
  private static final String SEPERATOR = "";

  public JIntervalParameterGUI( Parameter     parameter, 
                                AttributeList attr_list )
  { 
    super( parameter );

                                       //create a combo box with the
                                       //attributes passed in as parameters
    combobox = new JComboBox();
    combobox.setEditable( false );
    for(int i = 0; i<attr_list.getNum_attributes(); i++)
      combobox.addItem(  attr_list.getAttribute(i).getName()  );

                                      //create and size the text box
                                      //used for entering intervals
    text = new JTextField();
    text.setPreferredSize(  new Dimension( 100, 10 )  ); 

                                      //set up JPanel to be returned
                                      //to the JParametersDialog
    segment = new JPanel();
    segment.setLayout(  new GridLayout( 1, 2 )  ); 
    segment.add( combobox );
    segment.add( text );
  }


  public JPanel getGUISegment()
  {
    return segment;
  }


  public void setEnabled(boolean en){
      this.text.setEnabled(en);
  }

  /**
   * get the value that the user entered.
   */
  public Parameter getParameter()
  {
    try
    {
      String str = (String)( combobox.getSelectedItem() );
      str += SEPERATOR + text.getText();
      parameter.setValue( str );
    }
    catch( Exception s)
    {
      System.out.println( s );
    }

    return parameter;
  }
}

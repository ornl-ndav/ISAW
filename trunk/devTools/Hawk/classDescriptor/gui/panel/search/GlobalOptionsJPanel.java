/*
 * File:  GlobalOptionsJPanel.java
 *
 * Copyright (C) 2004 Dominic Kramer
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Dominic Kramer <kramerd@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA and by
 * the National Science Foundation under grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2004/03/11 18:40:11  bouzekc
 * Documented file using javadoc statements.
 * Removed the main method.
 *
 * Revision 1.1  2004/02/07 05:15:50  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.panel.search;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Panel that is used to get input from the user to basically specify the size of a vector.  The panel contains a 
 * JComboBox to allow the user to specify that the entry must contain "Greater than or equal to", "Less than or equal to", or "Exactly" 
 * a certain number of elements.  There is also a JSpinner that allows the user to specify the number of elements.  When read from 
 * left to right for instance the panel by default would read that the entry must contain "Greater than or equal to" "0" elements.  This class 
 * is primarily used by the VectorOptionsJPanel class to specify information about the Vector.  This class contains a main method to allow 
 * you to view the panel.
 * @author Dominic Kramer
 */
public class GlobalOptionsJPanel extends JPanel implements ActionListener
{
	/**
	 * Integer specifying that the number of elements in the Vector has to be greater than or equal 
	 * to the value specified by the JSpinner on the panel.
	 */
	public static final int GREATER_THAN_OR_EQUAL_TO = 0;
	/**
	 * Integer specifying that the number of elements in the Vector has to be less than or equal 
	 * to the value specified by the JSpinner on the panel.
	 */
	public static final int LESS_THAN_OR_EQUAL_TO = 1;
	/**
	 * Integer specifying that the number of elements in the Vector has to be equal 
	 * to the value specified by the JSpinner on the panel.
	 */
	public static final int EXACTLY = 2;
	
	/**
	 * The combo box specifying the relation between the number of elements in the Vector 
	 * with the JSpinner on the panel.
	 */
	protected JComboBox globalBox;
	/**
	 * The spinner specifying the relation to the number of elements that are supposed to be in the Vector.
	 */
	protected JSpinner globalSpinner;
	/** The model used to specify the numbers in the JSpinner. */
	protected SpinnerNumberModel model;
	/** The first label on the panel (from left to right). */
	protected JLabel label1;
	/** The second label on the panel (from left to right). */
	protected JLabel label2;
	
	/**
	 * Create a new GlobalOptionsJPanel.
	 * @param str1 The String to be placed on the first label on the panel (from left to right).
	 * @param str2 The String to be placed on the second label on the panel (from left to right).
	 */
	public GlobalOptionsJPanel(String str1, String str2)
	{
		setLayout(new FlowLayout(FlowLayout.LEFT));
		label1 = new JLabel(str1);
		label2 = new JLabel(str2);
		String[] stringArr = new String[3];
			stringArr[0]="Greater than or equal to";
			stringArr[1]="Less than or equal to";
			stringArr[2]="Exactly";
		globalBox = new JComboBox(stringArr);
		model = new SpinnerNumberModel();
		model.setMinimum(new Integer(0));
		model.setValue(new Integer(0));
		globalSpinner = new JSpinner(model);
		
		add(label1);
		add(globalBox);
		add(globalSpinner);
		add(label2);
	}
	
	/**
	 * Specify the text in the JComobBox globalBox.
	 * @param num Either GREATER_THAN_OR_EQUAL_TO, LESS_THAN_OR_EQUAL_TO, or EXACTLY.
	 */
	public void setGlobalBoxText(int num)
	{
		if (num==GREATER_THAN_OR_EQUAL_TO || num==LESS_THAN_OR_EQUAL_TO || num==EXACTLY)
			globalBox.setSelectedIndex(num);
	}
	
	/**
	 * Get the text that is currently displayed on the JComboBox globalBox.
	 * @return The text in globalBox.
	 */
	public String getGlobalBoxText()
	{
		return (String)globalBox.getSelectedItem();
	}
	
	/**
	 * Get the selected index from the JComboBox globalBox.
	 * @return Either GREATER_THAN_OR_EQUAL_TO, LESS_THAN_OR_EQUAL_TO, or EXACTLY.
	 */	
	public int getGlobalBoxSelectedIndex()
	{
		return globalBox.getSelectedIndex();
	}
	
	/**
	 * Set the value of the JSpinner.
	 * @param num The value of the spinner.
	 */
	public void setSpinnerValue(int num)
	{
		globalSpinner.setValue(new Integer(num));
	}
	
	/**
	 * Get the JSpinner's value.
	 * @return The JSpinner's value.
	 */	
	public int getSpinnerValue()
	{
		return ((Integer)globalSpinner.getValue()).intValue();
	}
	
	/** Handles action events. */
	public void actionPerformed(ActionEvent e)
	{
	}
	
	/**
	 * Determines if "num" is a valid number as specified from the components on the panel.  For 
	 * instance if the panel reads "Greater than" "9" elements, 10,11,12, . . . . will return true and 8,7,6,5, . . . . 
	 * will return false.
	 * @param num The number to compare.
	 * @return True if the number is valid and false otherwise.
	 */	
	public boolean isAValidNumberOfItems(int num)
	{
		boolean answer = false;
		int index = globalBox.getSelectedIndex();
		if (index == GREATER_THAN_OR_EQUAL_TO)
			answer = (num>=getSpinnerValue());
		else if (index == LESS_THAN_OR_EQUAL_TO)
			answer = (num<=getSpinnerValue());
		else if (index == EXACTLY)
			answer = (num==getSpinnerValue());
			
		return answer;
	}
}

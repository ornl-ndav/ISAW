/*
 * File:  AdvancedOptionsJPanel.java
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
 * Revision 1.2  2004/03/11 18:21:32  bouzekc
 * Documented file using javadoc statements.
 * Removed the main method.
 *
 * Revision 1.1  2004/02/07 05:15:49  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.panel.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *This class is a special BasicOptionsJPanel which has the correct options when searching for a group of terms (such as parameters).
 *This includes an extra JComboBox and JSpinner combination that allows the user to specify where each term should be
 *(either anywhere, as last element, or at a specific element number) in the Vector.
 *@author Dominic Kramer
 */
public class AdvancedOptionsJPanel extends BasicOptionsJPanel implements ActionListener
{
	/**
	 * Integer specifying that the String has to be anywhere in the Vector.
	 */
	public static int ANYWHERE = 0;
	/**
	 * Integer specifying that the String has to be at the last element ing the Vector.
	 */
	public static int AT_LAST_ELEMENT = 1;
	/**
	 * Integer specifying the String has to be at a specific element.
	 */
	public static int AT_ELEMENT = 2;
	/** The label on the panel. */
	protected JLabel locationLabel;
	/** The combo box specifying where the String should be in the Vector. */
	protected JComboBox locationBox;
	/** The JSpinner associated with the panel. */
	protected JSpinner numberSpinner;
	/** The model describing the elements in the list. */
	protected SpinnerNumberModel model;
	/** The frame onto which this panel is added to. */
	protected JFrame frame;
	
	/**
	 * Create a new AdvancedOptionsJPanel.
	 * @param textTitle The title of the panel.
	 * @param FRAME The frame onto which this panel is placed.
	 */
	public AdvancedOptionsJPanel(String textTitle, JFrame FRAME)
	{
		super(textTitle);
		frame = FRAME;
		locationLabel = new JLabel("Located");
		String[] locationArr = new String[3];
			locationArr[0]="Anywhere";
			locationArr[1]="At last element";
			locationArr[2]="At element";
		locationBox = new JComboBox(locationArr);
		locationBox.addActionListener(this);
		locationBox.setActionCommand("location.box");
		
		model = new SpinnerNumberModel();
		model.setMinimum(new Integer(1));
		model.setValue(new Integer(1));
		numberSpinner = new JSpinner(model);
		numberSpinner.setVisible(false);
		
		add(locationLabel);
		add(locationBox);
		add(numberSpinner);
	}
	
	/**
	 * Get the frame associated with this panel.
	 */
	public JFrame getJFrame()
	{
		return frame;
	}
	
	/**
	 * Set the frame associated with this panel.
	 * @param FRAME The frame to associate with this panel.
	 */
	public void setJFrame(JFrame FRAME)
	{
		frame = FRAME;
	}

	/**
	 * Set the JComboBox specifying where the String is supposed to be.
	 * @param num Either ANYWHERE, AT_LAST_ELEMENT, or AT_ELEMENT.
	 */
	public void setLocationBoxSelectedBox(int num)
	{
		if (num==ANYWHERE || num==AT_LAST_ELEMENT || num==AT_ELEMENT)
			locationBox.setSelectedIndex(num);
	}
	
	/**
	 * Get the text of locationBox.
	 * @return Either "Anywhere", "At last element", or "At element".
	 */
	public String getLocationBoxSelectedText()
	{
		return (String)locationBox.getItemAt(locationBox.getSelectedIndex());
	}
	
	/**
	 * Get the value of locationBox.  This method is more useful for checking 
	 * conditions.
	 * @return Either ANYWHERE, AT_LAST_ELEMENT, or AT_ELEMENT.
	 */
	public int getLocationBoxSelectedValue()
	{
		return locationBox.getSelectedIndex();
	}
	
	/**
	 * Set the JSpinner's value.
	 * @param num The value to set.
	 */
	public void setNumberSpinnerValue(int num)
	{
		if (num>0)
			model.setValue(new Integer(num));
	}
	
	/**
	 * Get the JSpinner's value.
	 * @return The JSpinner's value.
	 */
	public int getNumberSpinnerValue()
	{
		return ((Integer)model.getValue()).intValue();
	}
	
	/**
	 * Handles action events.
	 */	
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("location.box"))
		{
			if ( ((String)locationBox.getItemAt(locationBox.getSelectedIndex())).equals("At element"))
				numberSpinner.setVisible(true);
			else
				numberSpinner.setVisible(false);
			
			frame.pack();
		}
		
		super.actionPerformed(event);
	}
}
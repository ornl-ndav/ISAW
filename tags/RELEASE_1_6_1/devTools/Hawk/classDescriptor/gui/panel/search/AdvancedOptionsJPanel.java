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
 * Revision 1.1  2004/02/07 05:15:49  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.panel.search;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * @author kramer
 *
 *This class is a special BasicOptionsJPanel which has the correct options when searching for a group of terms (such as parameters).
 *This includes an extra JComboBox and JSpinner combination that allows the user to specify where each term should be
 *(either anywhere, as last element, or at a specific element number).
 */
public class AdvancedOptionsJPanel extends BasicOptionsJPanel implements ActionListener
{
	public static int ANYWHERE = 0;
	public static int AT_LAST_ELEMENT = 1;
	public static int AT_ELEMENT = 2;
	protected JLabel locationLabel;
	protected JComboBox locationBox;
	protected JSpinner numberSpinner;
	protected SpinnerNumberModel model;
	protected JFrame frame;
	
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
	
	public JFrame getJFrame()
	{
		return frame;
	}
	
	public void setJFrame(JFrame FRAME)
	{
		frame = FRAME;
	}
	
	public void setLocationBoxSelectedBox(int num)
	{
		if (num==ANYWHERE || num==AT_LAST_ELEMENT || num==AT_ELEMENT)
			locationBox.setSelectedIndex(num);
	}
	
	public String getLocationBoxSelectedText()
	{
		return (String)locationBox.getItemAt(locationBox.getSelectedIndex());
	}
	
	public int getLocationBoxSelectedValue()
	{
		return locationBox.getSelectedIndex();
	}
	
	public void setNumberSpinnerValue(int num)
	{
		if (num>0)
			model.setValue(new Integer(num));
	}

	public int getNumberSpinnerValue()
	{
		return ((Integer)model.getValue()).intValue();
	}
	
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

	//this is to test how the JPanel looks
	public static void main(String args[])
	{
		JFrame frame = new JFrame();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		AdvancedOptionsJPanel panel = new AdvancedOptionsJPanel("Class Name:", frame);
		mainPanel.add(panel, BorderLayout.CENTER);
		frame.getContentPane().add(mainPanel);
		frame.setVisible(true);
		frame.pack();
	}
}
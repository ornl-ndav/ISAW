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
 * Revision 1.1  2004/02/07 05:15:50  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.panel.search;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class GlobalOptionsJPanel extends JPanel implements ActionListener
{
	public static final int GREATER_THAN_OR_EQUAL_TO = 0;
	public static final int LESS_THAN_OR_EQUAL_TO = 1;
	public static final int EXACTLY = 2;
	
	protected JComboBox globalBox;
	protected JSpinner globalSpinner;
	protected SpinnerNumberModel model;
	protected JLabel label1;
	protected JLabel label2;
	
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
	
	public void setGlobalBoxText(int num)
	{
		if (num==GREATER_THAN_OR_EQUAL_TO || num==LESS_THAN_OR_EQUAL_TO || num==EXACTLY)
			globalBox.setSelectedIndex(num);
	}
	
	public String getGlobalBoxText()
	{
		return (String)globalBox.getSelectedItem();
	}
	
	public int getGlobalBoxSelectedIndex()
	{
		return globalBox.getSelectedIndex();
	}
	
	public void setSpinnerValue(int num)
	{
		globalSpinner.setValue(new Integer(num));
	}
	
	public int getSpinnerValue()
	{
		return ((Integer)globalSpinner.getValue()).intValue();
	}
	
	public void actionPerformed(ActionEvent e)
	{
	}
	
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

	//this is to test how the JPanel looks
	public static void main(String args[])
	{
		JFrame frame = new JFrame();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		GlobalOptionsJPanel panel = new GlobalOptionsJPanel("Result must contain ","parameters");
		mainPanel.add(panel, BorderLayout.CENTER);
		frame.getContentPane().add(mainPanel);
		frame.setVisible(true);
		frame.pack();
	}
}

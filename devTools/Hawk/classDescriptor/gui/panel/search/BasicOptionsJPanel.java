/*
 * File:  BasicOptionsJPanel.java
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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author kramer
 *
 *This class is a special type of JPanel which has the options to search for one word.  This panel has a place to enter the text and specify
 *if you want to match the entire word and/or if you want the search to be case sensitive.
 */
public class BasicOptionsJPanel extends JPanel implements ActionListener
{
	public static final int MATCH_MUST_CONTAIN = 0;
	public static final int MATCH_MUST_NOT_CONTAIN = 1;
	
	protected JLabel label;
	protected JTextField textField;
	protected JComboBox comboBox;
	protected JCheckBox entireWordBox;
	protected JCheckBox caseSensitiveBox;
	
	public BasicOptionsJPanel(String textTitle)
	{
		label = new JLabel(textTitle);
		textField = new JTextField(10);
		String[] strArr = new String[2];
			strArr[0]="Match Must Contain Word";
			strArr[1]="Match Must Not Contain Word";
		comboBox = new JComboBox(strArr);
		entireWordBox = new JCheckBox("Match Entire Word");
			entireWordBox.addActionListener(this);
			entireWordBox.setActionCommand("entire.word");
		caseSensitiveBox = new JCheckBox("Case Sensitive");
			caseSensitiveBox.addActionListener(this);
			caseSensitiveBox.setActionCommand("case.sensitive");
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(label);
		add(textField);
		add(comboBox);
		add(entireWordBox);
		add(caseSensitiveBox);
	}
	
	public String getLabelText()
	{
		return label.getText();
	}
	
	public void setLabelText(String str)
	{
		label.setText(str);
	}
	
	public String getTextFieldText()
	{
		return textField.getText();
	}
	
	public void setTextFieldText(String str)
	{
		textField.setText(str);
	}

	public void setComboBoxSelectedText(int num)
	{
		if ( (num==MATCH_MUST_CONTAIN) || (num==MATCH_MUST_NOT_CONTAIN))
			comboBox.setSelectedIndex(num);
	}
	
	public String getComboBoxSelectedText()
	{
		return (String)comboBox.getItemAt(comboBox.getSelectedIndex());
	}
	
	public int getComboBoxSelectedValue()
	{
		return comboBox.getSelectedIndex();
	}
	
	public boolean mustMatchContainWord()
	{
		boolean answer = false;
		int num = comboBox.getSelectedIndex();
		if (num == MATCH_MUST_CONTAIN)
			answer = true;
			
		return answer;
	}
		
	public void setMatchEntireWord(boolean bol)
	{
		entireWordBox.setSelected(bol);
	}
	
	public boolean matchEntireWord()
	{
		return entireWordBox.isSelected();
	}
	
	public void setMatchCaseSensitive(boolean bol)
	{
		caseSensitiveBox.setSelected(bol);
	}
	
	public boolean matchCaseSensitive()
	{
		return caseSensitiveBox.isSelected();
	}
	
	public void actionPerformed(ActionEvent event)
	{
	}
	
	//this is to test how the JPanel looks
	public static void main(String args[])
	{
		JFrame frame = new JFrame();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		BasicOptionsJPanel panel = new BasicOptionsJPanel("Class Name:");
		mainPanel.add(panel, BorderLayout.CENTER);
		frame.getContentPane().add(mainPanel);
		frame.setVisible(true);
		frame.pack();
	}
}

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
 * Revision 1.2  2004/03/11 18:29:35  bouzekc
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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *This class is a special type of JPanel which has the options to search for one word.  This panel has a place to enter the text and specify
 *if you want to match the entire word and/or if you want the search to be case sensitive.  This class contains a main method to allow you 
 *to view the panel.
 *
 *@author Dominic Kramer
 */
public class BasicOptionsJPanel extends JPanel implements ActionListener
{
	/**
	 * Constant used to specify that the search must match the word.
	 */
	public static final int MATCH_MUST_CONTAIN = 0;
	/**
	 * Constant used to specify that the search must not match the word.  For 
	 * instance if "class" is the word to look for.  Then the search results will be 
	 * everything that does not contain the word "class".
	 */
	public static final int MATCH_MUST_NOT_CONTAIN = 1;
	/**
	 * The JPanel's label.
	 */
	protected JLabel label;
	/**
	 * The text field used to enter the string to search for.
	 */
	protected JTextField textField;
	/**
	 * This combo box holds the options if the user wants to have 
	 * the search match the entered word or not.
	 */
	protected JComboBox comboBox;
	/**
	 * Used to allow the user to specify if the entire text from the 
	 * JTextField should be matched.  For example, if the user entered 
	 * "lass" and the user wants to match the entire word, then "class" 
	 * would not be a match.
	 */
	protected JCheckBox entireWordBox;
	/**
	 * Used to allow the user to specify if the entered text should be 
	 * searched in a case sensitive context.
	 */
	protected JCheckBox caseSensitiveBox;
	
	/**
	 * Creates a new BasicOptionsJPanel.
	 * @param textTitle The title of the panel's JLabel.
	 */
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
	
	/**
	 * Get the JLabel's text.
	 * @return The JLabel's text.
	 */
	public String getLabelText()
	{
		return label.getText();
	}
	
	/**
	 * Set the JLabel's text.
	 * @param str The JLabel's text.
	 */
	public void setLabelText(String str)
	{
		label.setText(str);
	}
	
	/**
	 * Get the JTextField's text.
	 * @return The JTextField's text.
	 */
	public String getTextFieldText()
	{
		return textField.getText();
	}
	
	/**
	 * Set the JTextField's text.
	 * @param str The JTextField's text.
	 */
	public void setTextFieldText(String str)
	{
		textField.setText(str);
	}
	
	/**
	 * Sets the option selected in the JComboBox that displays either "Match must contain word" 
	 * and "Match must not contain word".
	 * @param num Either MATCH_MUST_CONTAIN or MATCH_MUST_NOT_CONTAIN
	 */
	public void setComboBoxSelectedText(int num)
	{
		if ( (num==MATCH_MUST_CONTAIN) || (num==MATCH_MUST_NOT_CONTAIN))
			comboBox.setSelectedIndex(num);
	}
	
	/**
	 * Returns the text from the JComboBox that displays either "Match must contain word" and 
	 * "Match must not contain word".
	 * @return Either "Match must contain word" or "Match must not contain word"
	 */
	public String getComboBoxSelectedText()
	{
		return (String)comboBox.getItemAt(comboBox.getSelectedIndex());
	}

	/**
	 * Returns the value specifying the item selected from the JComboBox that displays 
	 * either "Match must contain word" and "Match must not contain word".
	 * @return Either MATCH_MUST_CONTAIN or MATCH_MUST_NOT_CONTAIN
	 */	
	public int getComboBoxSelectedValue()
	{
		return comboBox.getSelectedIndex();
	}
	
	/**
	 * Describes if the user wants to match word or if the user wants to 
	 * find matches that do not contain the word.
	 * @return True if the word must be matched.
	 */
	public boolean mustMatchContainWord()
	{
		boolean answer = false;
		int num = comboBox.getSelectedIndex();
		if (num == MATCH_MUST_CONTAIN)
			answer = true;
			
		return answer;
	}
	
	/**
	 * Set the option to match the entire word.  This changes the 
	 * selection of the JCheckBox on the panel.
	 * @param bol True if the entire word is to be matched.
	 */
	public void setMatchEntireWord(boolean bol)
	{
		entireWordBox.setSelected(bol);
	}
	
	/**
	 * Describes if the user wants to match the entire word.
	 * @return True if the user wants to match the entire word.
	 */
	public boolean matchEntireWord()
	{
		return entireWordBox.isSelected();
	}
	
	/**
	 * Sets the option that match should be case sensitive.  
	 * This method changes the JCheckBox on the panel.
	 * @param bol
	 */
	public void setMatchCaseSensitive(boolean bol)
	{
		caseSensitiveBox.setSelected(bol);
	}
	
	/**
	 * Describes if the user wants the match to be case 
	 * sensitive.
	 * @return True if the match is to be case sensitive.
	 */
	public boolean matchCaseSensitive()
	{
		return caseSensitiveBox.isSelected();
	}
	
	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
	}
}

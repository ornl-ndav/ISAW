/*
 * File:  VectorOptionsJPanel.java
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
 * Revision 1.3  2004/03/12 19:46:18  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:15:51  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.panel.search;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *This class is a special JPanel which has a place to enter a combination of Strings.  It also has a JButton.  When the
 *user presses this button a window pops up to allow the user to specify more specific options for each String entered.  
 *The class is used to allow the user to specify a combination of Strings each of which may correspond to an element 
 *from a Vector from an Interface object.
 *@author Dominic Kramer
 */
public class VectorOptionsJPanel extends JPanel implements ActionListener
{
	/** The panel's secondary label. */
	protected String subTitle;
	/** The panel's label. */
	protected JLabel label;
	/** Allows the user to enter the text to search for. */
	protected JTextField textField;
	/** The button that opens the window to edit advanced options. */
	protected JButton advancedButton;
	/**
	 *  A Vector of AdvancedOptionsJPanel objects (One for each token entered where a token is 
	 * a section of the text entered into the JTextField as separated by spaces).
	 */
	protected Vector optionsVec; //a vector of AdvancedOptionsJPanel objects
	/** Used to specify how many elements the Vector must contain. */
	protected GlobalOptionsJPanel globalOptionsPanel;
	/** Used to determine if the user has changed the String entered. */
	private String previousText;
	
	/**
	 * Create a new VectorOptionsJPanel.
	 * @param textTitle The text that is on the first label on the panel (from left to right).
	 * @param SUBTITLE The text that is on the second label on the panel (from left to right).
	 * @param pluralFormOfType The plural form of the type that is to be entered on this panel.  For 
	 * example "Parameters" or "Characteristcs"
	 */
	public VectorOptionsJPanel(String textTitle, String SUBTITLE, String pluralFormOfType)
	{
		subTitle = SUBTITLE;
		label = new JLabel(textTitle);
		textField = new JTextField(10);
		advancedButton = new JButton("Advanced Options");
			advancedButton.addActionListener(this);
			advancedButton.setActionCommand("advanced.options");
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(label);
		add(textField);
		add(advancedButton);
		optionsVec = new Vector();
		previousText = textField.getText();
		
		globalOptionsPanel = new GlobalOptionsJPanel("Match must contain",pluralFormOfType);
	}
	
	/**
	 * Get optionsVec.
	 */	
	public Vector getOptionsVec()
	{
		return optionsVec;
	}
	
	/**
	 * Get label's text.
	 */
	public String getLabelText()
	{
		return label.getText();
	}
	
	/**
	 * Set label's text.
	 * @param str The new label's text.
	 */	
	public void setLabelText(String str)
	{
		label.setText(str);
	}
	
	/**
	 * Get the text from textField.
	 * @return The text from textField.
	 */
	public String getTextFieldText()
	{
		return textField.getText();
	}
	
	/**
	 * Set the text for textField.
	 * @param str The new text.
	 */	
	public void setTextFieldText(String str)
	{
		textField.setText(str);
	}
	
	/**
	 * Get globalOptionsPanel.
	 */	
	public GlobalOptionsJPanel getGlobalOptionsJPanel()
	{
		return globalOptionsPanel;
	}
	
	/** Handles ActionEvents. */
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("advanced.options"))
		{
			String search = textField.getText();
			if (!search.equals(previousText))
			{
				optionsVec.removeAllElements();

				if (!search.trim().equals(""))
				{
					StringTokenizer tokenizer = new StringTokenizer(search);
					while (tokenizer.hasMoreTokens())
					{
						String token = tokenizer.nextToken();
						AdvancedOptionsJPanel optionsPanel = new AdvancedOptionsJPanel(subTitle, new JFrame());
						optionsPanel.setTextFieldText(token);
						optionsVec.add(optionsPanel);
					}
					AdvancedOptionsGUI gui = new AdvancedOptionsGUI(optionsVec);
					gui.getMainPanel().add(globalOptionsPanel, BorderLayout.NORTH);
					for (int i=0; i<optionsVec.size(); i++)
						((AdvancedOptionsJPanel)(optionsVec.elementAt(i))).setJFrame(gui);
					gui.setVisible(true);
					gui.pack();
				}
				else
				{
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,
						"Enter a search string to modify its advanced options",
						"Note",
						JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else
			{
				if (!search.trim().equals(""))
				{
					AdvancedOptionsGUI gui = new AdvancedOptionsGUI(optionsVec);
					for (int i=0; i<optionsVec.size(); i++)
						((AdvancedOptionsJPanel)(optionsVec.elementAt(i))).setJFrame(gui);
					gui.setVisible(true);
				}
				else
				{
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,
						"Enter a search string to modify its advanced options",
						"Note",
						JOptionPane.INFORMATION_MESSAGE);
				}
			}
			previousText = search;
		}
	}
	
	/**
	 * This class takes the strings entered in the JTextField of the outer class and tokenizes them by using a space 
	 * as a deliminator.  This window then allow the user to specify options for each string.
	 * @author Dominic Kramer
	 */
	class AdvancedOptionsGUI extends JFrame implements ActionListener
	{	
		/**
		 * The panel on which all the elements are added.
		 */
		protected JPanel mainPanel;
		
		/**
		 * Create a new AdvancedOptionsGUI.
		 * @param vec The Vector of Strings whose options are to be specified by the user.
		 */	
		public AdvancedOptionsGUI(Vector vec)
		{
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());

			JPanel panelsPanel = new JPanel();
			if (vec.size() > 0)
			{
				panelsPanel.setLayout(new GridLayout(vec.size(), 0));
				for (int i=0; i<vec.size(); i++)
					panelsPanel.add((AdvancedOptionsJPanel)(vec.elementAt(i)));				
			}
			
			 JPanel buttonPanel = new JPanel();
			 buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			 JButton okButton = new JButton("Ok");
				okButton.addActionListener(this);
				okButton.setActionCommand("ok");
			 buttonPanel.add(okButton);
		 
			 mainPanel.add(panelsPanel, BorderLayout.CENTER);
			 mainPanel.add(buttonPanel, BorderLayout.SOUTH);
			 getContentPane().add(mainPanel);
			 pack();
		}
		
		/** Get the main panel on which all of the components are placed. */
		public JPanel getMainPanel()
		{
			return mainPanel;
		}
		
		/** Handles ActionEvents. */
		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals("ok"))
				dispose();
		}
	}
}
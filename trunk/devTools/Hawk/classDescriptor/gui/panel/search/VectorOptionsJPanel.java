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
 * @author kramer
 *
 *This class is a special JPanel which has a place to enter a combination of Strings.  It also has a JButton.  When the
 *user presses this button a window pops up to allow the user to specify more specific options.
 */
public class VectorOptionsJPanel extends JPanel implements ActionListener
{
	protected String subTitle;
	protected JLabel label;
	protected JTextField textField;
	protected JButton advancedButton;
	protected Vector optionsVec; //a vector of AdvanceOptionsJPanel objects
	protected GlobalOptionsJPanel globalOptionsPanel;
	private String previousText;
	
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
	
	public Vector getOptionsVec()
	{
		return optionsVec;
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
	
	public GlobalOptionsJPanel getGlobalOptionsJPanel()
	{
		return globalOptionsPanel;
	}
	
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
	
	class AdvancedOptionsGUI extends JFrame implements ActionListener
	{	
		protected JPanel mainPanel;
			
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
		
		public JPanel getMainPanel()
		{
			return mainPanel;
		}
		
		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals("ok"))
				dispose();
		}
	}
	
	//this is to test how the JPanel looks
	public static void main(String args[])
	{
		JFrame frame = new JFrame();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		VectorOptionsJPanel panel = new VectorOptionsJPanel("Parameters:  ", "Parameter:  ","parameters");
		mainPanel.add(panel, BorderLayout.CENTER);
		frame.getContentPane().add(mainPanel);
		frame.setVisible(true);
		frame.pack();
	}
}
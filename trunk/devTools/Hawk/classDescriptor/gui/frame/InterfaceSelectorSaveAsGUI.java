/*
 * File:  InterfaceSelectorSaveAsGUI.java
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
 * Revision 1.2  2004/03/11 18:45:59  bouzekc
 * Documented file using javadoc statements.
 *
 * Revision 1.1  2004/02/07 05:08:50  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import devTools.Hawk.classDescriptor.modeledObjects.Project;

/**
 * This is a class that allows the user to save the project from an InterfaceSelectorJPanel.  
 * Currently, this class is not needed, may be removed, and is still under construction.
 * @author Dominic Kramer
 */
public class InterfaceSelectorSaveAsGUI extends JFrame implements ActionListener
{
	private JTextField nameField;
	private Project selectedProject;
	private JTextField fileField;
	
	public InterfaceSelectorSaveAsGUI(Project pro)
	{
		selectedProject = pro;
		
		setTitle("Save as a Project");
		setSize(200,200);
		
		Container pane = getContentPane();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(4,0));
		
		JPanel midPanel = new JPanel();
		midPanel.setLayout(new GridLayout(2,0));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		//this is the font that could be used
			//Font monoSpaced = new Font("Monospaced", Font.PLAIN, 12);
		
		//now to make the text panel
			JLabel label1 = new JLabel("The data will be saved as a project."); 
				//label1.setFont(monoSpaced);
			JLabel label2 = new JLabel("Choose a name for the project and");
				//label2.setFont(monoSpaced);
			JLabel label3 = new JLabel("select a filename to save to.");
				//label3.setFont(monoSpaced);
			
			textPanel.add(label1);
			textPanel.add(label2);
			textPanel.add(label3);
			textPanel.add(new JSeparator());
			mainPanel.add(textPanel, BorderLayout.NORTH);
			
		//now to make the middle panel
			JPanel panel1 = new JPanel();
			panel1.setLayout(new BorderLayout());
			
			JPanel panel2 = new JPanel();
			panel2.setLayout(new BorderLayout());			
			
			JLabel nameLabel = new JLabel("Project Name:  ");
				//nameLabel.setFont(monoSpaced);
			panel1.add(nameLabel, BorderLayout.WEST);
				nameField = new JTextField("",15);
			panel1.add(nameField, BorderLayout.EAST);
			midPanel.add(panel1);
			
			JLabel fileLabel = new JLabel("Filename:      ");
				//fileLabel.setFont(monoSpaced);
			panel2.add(fileLabel, BorderLayout.WEST);
				fileField = new JTextField("",15);
			panel2.add(fileField, BorderLayout.EAST);
			midPanel.add(panel2);
			
			mainPanel.add(midPanel, BorderLayout.CENTER);
			
		//now to make the button panel
			JButton findFileButton = new JButton("Select File");
			findFileButton.addActionListener(this);
			buttonPanel.add(findFileButton);
			
			JButton saveButton = new JButton("Save");
			saveButton.addActionListener(this);
			buttonPanel.add(saveButton);
			
			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(this);
			buttonPanel.add(closeButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);	
			
			pane.add(mainPanel);
			pack();
	}
	
	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Save"))
		{
			boolean end = true;
			if (!(nameField.getText().trim().equals("")  || fileField.getText().trim().equals("")))
			{
				selectedProject.setProjectName(nameField.getText());
				selectedProject.getData().setFileName(fileField.getText());
				selectedProject.writeNativeHawkFile();
			}
			else
			{
				JOptionPane opPane = new JOptionPane();
				String warning = "";
				if (nameField.getText().trim().equals(""))	
					warning = warning + "Please enter a name for the project";
				if (fileField.getText().trim().equals(""))
				{
					if (nameField.getText().trim().equals(""))
						warning = warning + ".  Also, p";
					else
						warning = warning + "P";
						
					warning = warning + "lease enter a filename\n";
				}	
					JOptionPane.showMessageDialog(opPane,warning,"Error"
					,JOptionPane.ERROR_MESSAGE);
				end = false;
			}
			
			if (end)
				dispose();
		}
		else if (event.getActionCommand().equals("Select File"))
		{
			JFrame frame = new JFrame();
			frame.setSize(500,400);
			Container framePane = frame.getContentPane();
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			
			JFileChooser chooser = new JFileChooser();
			int returnVal = chooser.showDialog(frame, "Select");
						
			mainPanel.add(chooser, BorderLayout.CENTER);
				
			framePane.add(mainPanel);
			framePane.setVisible(true);
			
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				fileField.setText(chooser.getSelectedFile().getAbsoluteFile().toString());
			}
		}
		else if (event.getActionCommand().equals("Close"))
		{
			dispose();
		}
	}

}

/*
 * File:  CreateNewProjectGUI.java
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
 * Revision 1.1  2004/02/07 05:08:49  bouzekc
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
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import devTools.Hawk.classDescriptor.gui.panel.ProjectSelectorJPanel;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.threads.FindClassThread;
import devTools.Hawk.classDescriptor.threads.LoadClassThread;
import devTools.Hawk.classDescriptor.tools.FileReflector;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * @author kramer
 *
 */
public class CreateNewProjectGUI extends JFrame implements ActionListener, ListSelectionListener
{
	protected FileReflector fileReflector;
	protected UnableToLoadClassGUI gui;

	protected Project createdProject;
	
	protected JList list;
	protected DefaultListModel model;
	protected JTextField nameField;
	protected ProjectSelectorJPanel projectSelector;
	
	protected boolean createNewProject;
	
	protected CreateNewProjectGUI() {}
	
	public CreateNewProjectGUI(ProjectSelectorJPanel psjp, UnableToLoadClassGUI GUI)
	{
		createNewProject = true;
		
		gui = GUI;
		fileReflector = new FileReflector(gui);
		projectSelector = psjp;
		
		createdProject = new Project();
		
		setTitle("Create New Project");
		setSize(300,100);
		
		Container pane = getContentPane();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(3,0));
			JPanel panel1 = new JPanel();
			panel1.setLayout(new FlowLayout(FlowLayout.LEFT));
		
			JPanel panel2 = new JPanel();
			panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
					
			JPanel panel3 = new JPanel();
			panel3.setLayout(new FlowLayout(FlowLayout.LEFT));
					
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		//now to make the JTextfield
			nameField = new JTextField("New Project", 20);
		
		//now to make the top panel
			panel1.add(new JLabel("Enter the project's name"));
			panel2.add(new JLabel("Project name:  "));
			panel2.add(nameField);
			panel3.add(new JLabel("Choose files and directories to add to the project"));
		textPanel.add(panel1);
		textPanel.add(panel2);
		textPanel.add(panel3);
		mainPanel.add(textPanel, BorderLayout.NORTH);		
		
		//now to make the JList
			model = new DefaultListModel();
			list = new JList(model);
			list.addListSelectionListener(this);
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			JScrollPane listScrollPane = new JScrollPane(list);
			mainPanel.add(listScrollPane, BorderLayout.CENTER);
			
		//now to make the button panel
			JButton addFileButton = new JButton("Add file");
			addFileButton.setActionCommand("add.file");
			addFileButton.addActionListener(this);
			buttonPanel.add(addFileButton);
			
			JButton removeFileButton = new JButton("Remove selected files");
			removeFileButton.setActionCommand("remove.selected.files");
			removeFileButton.addActionListener(this);
			buttonPanel.add(removeFileButton);
			
			JButton closeButton = new JButton("Close");
			closeButton.setActionCommand("close");
			closeButton.addActionListener(this);
			buttonPanel.add(closeButton);
			
			JButton createProjectButton = new JButton("Create Project");
			createProjectButton.setActionCommand("create.project");
			createProjectButton.addActionListener(this);
			buttonPanel.add(createProjectButton);
			
			mainPanel.add(buttonPanel, BorderLayout.SOUTH);
			
			pane.add(mainPanel);
			pack();
	}
	
	public Project getCreatedProject()
	{
		return createdProject;
	}
	
	public boolean createNewProject()
	{
		return createNewProject;
	}
	
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("create.project"))
		{			
			LoadClassThread thread = new LoadClassThread(this);
			thread.start();
		}
		else if (event.getActionCommand().equals("close"))
		{
			dispose();
		}
		else if (event.getActionCommand().equals("add.file"))
		{
			FindClassThread thread = new FindClassThread(this);
			thread.start();
		}
		else if (event.getActionCommand().equals("remove.selected.files"))
		{
			int[] selectedIndices = list.getSelectedIndices();
			
			for (int i = selectedIndices.length-1; i>=0; i--)
			{
				model.remove(selectedIndices[i]);
			}
		}
	}
		
	public void disposeAndShowErrorBox()
	{
		if (gui.showErrorBox())
			gui.showGUI();
		dispose();
	}
	
	public FileReflector getFileReflector()
	{
		return fileReflector;
	}
	
	public DefaultListModel getModel()
	{
		return model;
	}
	
	private int getNumberOfClassesInJarFile(String fileName)
	{
		int answer = 0;
		JarFile jarFile = null;
		Enumeration en = null;
		ZipEntry entry = null;
		try
		{
			jarFile = new JarFile(fileName);
			en = jarFile.entries();
			while (en.hasMoreElements())
			{
				entry = (ZipEntry)en.nextElement();
				if (entry.toString().endsWith(".class"))
					answer++;
				else if (entry.toString().endsWith(".jar"))
					answer += getNumberOfClassesInJarFile(entry.toString());
			}
		}
		catch (IOException e)
		{
			SystemsManager.printStackTrace(e);
		}
		return answer;
	}
		
	public ProjectSelectorJPanel getProjectSelectorJPanel()
	{
		return projectSelector;
	}
	
	public JTextField getTextField()
	{
		return nameField;
	}
	
	public Vector getVectorOfInterfacesCreated()
	{
		return createdProject.getInterfaceVec();
	}
	
	public int numberOfClassesToLoad()
	{
		int answer = 0;
		String filename = "";
		for (int i=0; i<model.size(); i++)
		{
			filename = (String)model.elementAt(i);
			if (filename.endsWith(".jar"))
				answer += getNumberOfClassesInJarFile(filename);
			else
				answer++;
		}
		
		return answer;
	}
	
	public void setVectorOfInterfaces(Vector vec)
	{
		createdProject.setInterfaceVec(vec);
	}

	public void valueChanged(ListSelectionEvent e)
	{

	}

}

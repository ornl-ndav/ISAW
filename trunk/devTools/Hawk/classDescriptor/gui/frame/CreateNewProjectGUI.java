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
 * Revision 1.2  2004/03/11 18:33:12  bouzekc
 * Documented file using javadoc statesments.
 * Added tooltips to the buttons on the GUI.
 *
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
 * This class creates a window which allows the user to create a new project.
 * @author Dominic Kramer
 */
public class CreateNewProjectGUI extends JFrame implements ActionListener, ListSelectionListener
{
	/**
	 * Used to create Interface objects from .class files.
	 */
	protected FileReflector fileReflector;
	/**
	 * Window used to display any errors that occured when trying to make the project.
	 */
	protected UnableToLoadClassGUI gui;
	
	/**
	 * The Project that will be returned as being created.
	 */
	protected Project createdProject;
	/**
	 * The JList containing all of the files to used to load classes.
	 */
	protected JList list;
	/**
	 * The model for the JList.
	 */
	protected DefaultListModel model;
	/**
	 * The JTextField which used to hold the project's name.
	 */
	protected JTextField nameField;
	/**
	 * The ProjectSelectorJPanel from which the project is to be added to.
	 */
	protected ProjectSelectorJPanel projectSelector;
	/**
	 * This is the button which causes the project to be created.
	 */
	protected JButton createProjectButton;
	
	/**
	 * Creats the window with everything on it.
	 * @param psjp The ProjectSelectorJPanel to which the the project is added.
	 * @param GUI The window onto which any problems are displayed when trying to make the project.
	 */
	public CreateNewProjectGUI(ProjectSelectorJPanel psjp, UnableToLoadClassGUI GUI)
	{
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
			list.setToolTipText("This is the list of files from which class information will be obtained");
			list.addListSelectionListener(this);
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			JScrollPane listScrollPane = new JScrollPane(list);
			mainPanel.add(listScrollPane, BorderLayout.CENTER);
			
		//now to make the button panel
			JButton addFileButton = new JButton("Add file");
			addFileButton.setActionCommand("add.file");
			addFileButton.setToolTipText("Add files to the list");
			addFileButton.addActionListener(this);
			buttonPanel.add(addFileButton);
			
			JButton removeFileButton = new JButton("Remove selected files");
			removeFileButton.setToolTipText("Remove the selected files from the list");
			removeFileButton.setActionCommand("remove.selected.files");
			removeFileButton.addActionListener(this);
			buttonPanel.add(removeFileButton);
			
			JButton closeButton = new JButton("Close");
			closeButton.setActionCommand("close");
			closeButton.setToolTipText("Close this window without making a project");
			closeButton.addActionListener(this);
			buttonPanel.add(closeButton);
			
			createProjectButton = new JButton("Create Project");
			createProjectButton.setActionCommand("create.project");
			createProjectButton.setToolTipText("Load the files in the list to make the project");
			createProjectButton.addActionListener(this);
			buttonPanel.add(createProjectButton);
			
			mainPanel.add(buttonPanel, BorderLayout.SOUTH);
			
			pane.add(mainPanel);
			pack();
	}
	
	/**
	 * Returns the created project in its current state.
	 * @return The current project
	 */
	public Project getCreatedProject()
	{
		return createdProject;
	}
	
	/**
	 * Returns the flag that describes if the class should make a new project.
	 * @return True if a new project should be created and false otherwise.
	 */
	public boolean createNewProject()
	{
		return true;
	}
	
	/**
	 * Returns the JButton corresponding to the the button which allows the user to 
	 * create a project.
	 * @return createProjectButton
	 */
	public JButton getCreateProjectButton()
	{
		return createProjectButton;
	}
	
	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("create.project"))
		{
			LoadClassThread thread = new LoadClassThread(createdProject,this);
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
	
	/**
	 * This disposes the window and brings up the window that displays any errors 
	 * that occured while trying to make the project.
	 */	
	public void disposeAndShowErrorBox()
	{
		if (gui.showErrorBox())
			gui.showGUI();
		dispose();
	}
	
	/**
	 * Get the FileReflector object associated with this window.  The FileReflector object is 
	 * used to create Interface objects from .class files.
	 * @return fileReflector
	 */
	public FileReflector getFileReflector()
	{
		return fileReflector;
	}
	
	/**
	 * Returns the list's model.
	 * @return The list's model.
	 */
	public DefaultListModel getModel()
	{
		return model;
	}
	
	/**
	 * Returns the number of .class files in a .jar file
	 * @param fileName The full filename for the .jar file
	 * @return The number of .class files.
	 */
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
	
	/**
	 * Returns the ProjectSelectorJPanel from which the project was selected.
	 * @return projectSelector
	 */
	public ProjectSelectorJPanel getProjectSelectorJPanel()
	{
		return projectSelector;
	}
	
	/**
	 * Returns the JTextField which holds the project's name.
	 * @return nameField
	 */
	public JTextField getTextField()
	{
		return nameField;
	}
	
	/**
	 * Returns a Vector containing all of the Interface objects created.
	 * @return A Vector of Interface objects
	 */
	public Vector getVectorOfInterfacesCreated()
	{
		return createdProject.getInterfaceVec();
	}
	
	/**
	 * Reads all of the files from the list and determines the total number of classes that 
	 * would be loaded.
	 * @return The total number of classes to load.
	 */
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
	
	/**
	 * This sets the Interfaces that will be returned as being created.
	 * @param vec A Vector of Interface objects.
	 */
	public void setVectorOfInterfaces(Vector vec)
	{
		createdProject.setInterfaceVec(vec);
	}
	
	/**
	 * This responds to a change in the JList.  Currently, this does nothing but has to be 
	 * in this class because it implements ListSelectionListener.
	 */
	public void valueChanged(ListSelectionEvent e)
	{

	}
}

/*
 * File:  ProjectSelectorJPanel.java
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
 * Revision 1.1  2004/02/07 05:09:43  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.panel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import DataSetTools.util.RobustFileFilter;
import devTools.Hawk.classDescriptor.gui.frame.AboutGUI;
import devTools.Hawk.classDescriptor.gui.frame.AddInterfaceGUI;
import devTools.Hawk.classDescriptor.gui.frame.CreateNewProjectGUI;
import devTools.Hawk.classDescriptor.gui.frame.FileAssociationGUI;
import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.gui.frame.PrintGUI;
import devTools.Hawk.classDescriptor.gui.frame.SearchGUI;
import devTools.Hawk.classDescriptor.gui.frame.StatisticsGUI;
import devTools.Hawk.classDescriptor.gui.frame.UnableToLoadClassGUI;
import devTools.Hawk.classDescriptor.gui.internalFrame.AlphabeticalListGUI;
import devTools.Hawk.classDescriptor.gui.internalFrame.PackageTreeGUI;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.FileAssociationManager;
import devTools.Hawk.classDescriptor.tools.dataFileUtilities;

public class ProjectSelectorJPanel extends JPanel implements ActionListener, ListSelectionListener, MenuListener
{
	//these are declare here to allow the actionPerformed() method to find them
	//and are components found on the GUI	
	/**
	* this is the JList of names for all of the projects currently open
	* and is placed on the GUI
	*/
		protected JList list;
	/** the model describing list */
		protected DefaultListModel model;
	/** the popup window */
		protected JPopupMenu popup;
		protected HawkDesktop desktop;
	
	//these attributes are used to describe the ProjectSelectorJPanel
	/** 
	* this is a Vector of Project objects, and each 
	* time a Project object is made (new) or opened from a file
	* it is placed at the end of this Vector.
	*/
		protected Vector ProjectVec;
	/** 
	* set this to true to have a new AlphabeticalListGUI 
	* pop up when the user selects an item in the list
	*/
		protected boolean openNewAlphaWindowOnSelect;
	
	protected JMenuItem copyToPreviousTabItem;
	protected JMenuItem previousTabItem;
	
	public ProjectSelectorJPanel(Vector PROVEC, HawkDesktop desk)
	{
		desktop = desk;
		
		//now to instantiate the Continer and main panel everything is added on
			setLayout(new BorderLayout());
		
		//this initially has a new window pop up if the list is selected
			openNewAlphaWindowOnSelect = true;
		
		//now to make the list for the gui		
			//this model allows you to modify the list
			model = new DefaultListModel();
			
			if (PROVEC != null)
			{
				ProjectVec = PROVEC;
				for (int i=0; i<ProjectVec.size(); i++)
					model.addElement( ((Project)ProjectVec.elementAt(i)).getProjectName());
			}
			else
			{
				ProjectVec = new Vector();
				ProjectVec.add(new Project());
				model.addElement("No projects listed");
			}

			list = new JList(model);
			list.addListSelectionListener(this);
			list.setVisibleRowCount(-1);
			//the following only allows one item to be selected at a time
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
				
		//now to make the JScrolPane to put the JList on		
			JScrollPane listPane = new JScrollPane(list);
		
			add(listPane, BorderLayout.CENTER);
					
		//now to make the popup menu
		popup = new JPopupMenu();
			//now to add components to the popup menu
			popup.add(new JLabel("View Interfaces"));
			popup.add(new JSeparator());
			
			JMenuItem alphaWindow = new JMenuItem("Alphabetically");
			alphaWindow.addActionListener(this);
			alphaWindow.setActionCommand("alphaWindow.popup");
			popup.add(alphaWindow);
			
			JMenuItem extendsWindow = new JMenuItem("Organized in Packages");
			extendsWindow.addActionListener(this);
			extendsWindow.setActionCommand("packageWindow.popup");
			popup.add(extendsWindow);
			
		//now to add listeners to the components that con bring up the popup menu
		MouseListener popupListener = new PopupListener();
		list.addMouseListener(popupListener);
	}
	
	protected ProjectSelectorJPanel() {}
	
	public JPanel getProjectSelectorJPanel()
	{
		return this;
	}
	
	public DefaultListModel getModel()
	{
		return model;
	}
	
	/**
	 * This returns the JMenuBar that will be attached to a HawkDesktop gui.
	 * @return The JMenuBar
	 */
	public JMenuBar getProjectJMenuBar()
	{
		//	now to make the JMenuBar for this part of the GUI
			JMenuBar projectMenuBar = new JMenuBar();
				 JMenu projectFileMenu = new JMenu("File");
					 JMenuItem newProjectItem = new JMenuItem("New Project");
					 newProjectItem.addActionListener(this);
					 newProjectItem.setActionCommand("project.new");
					 projectFileMenu.add(newProjectItem);
			
					 JMenuItem projectOpenItem = new JMenuItem("Open");
					 projectOpenItem.addActionListener(this);
					 projectOpenItem.setActionCommand("project.open");
					 projectFileMenu.add(projectOpenItem);
					 
					 JMenu openInMenu = new JMenu("Open In");
						JMenuItem openInNewWindowItem = new JMenuItem("New Window");
						openInNewWindowItem.addActionListener(this);
						openInNewWindowItem.setActionCommand("project.openIn.newWindow");
						openInMenu.add(openInNewWindowItem);
					 	
						JMenuItem openInNewTabItem = new JMenuItem("New Tab");
						openInNewTabItem.addActionListener(this);
						openInNewTabItem.setActionCommand("project.openIn.newTab");
						openInMenu.add(openInNewTabItem);
					//projectFileMenu.add(openInMenu);
					
					 projectFileMenu.add(new JSeparator());
						
					 JMenuItem projectSaveItem = new JMenuItem("Save");
					 projectSaveItem.addActionListener(this);
					 projectSaveItem.setActionCommand("project.save");
					 //projectFileMenu.add(projectSaveItem);
			
					 JMenuItem projectSaveAsItem = new JMenuItem("Save As");
					 projectSaveAsItem.addActionListener(this);
					 projectSaveAsItem.setActionCommand("project.saveAs");
					 projectFileMenu.add(projectSaveAsItem);
					 
					 projectFileMenu.add(new JSeparator());
					 
					 JMenuItem projectPrintItem = new JMenuItem("Print Project");
					 projectPrintItem.addActionListener(this);
					 projectPrintItem.setActionCommand("project.print");
					 projectFileMenu.add(projectPrintItem);
				     
					 projectFileMenu.add(new JSeparator());
					
					 JMenuItem exitItem = new JMenuItem("Exit");
						exitItem.addActionListener(this);
						exitItem.setActionCommand("exit");
						projectFileMenu.add(exitItem);					 				     
				
				JMenu projectEditMenu = new JMenu("Edit");
					JMenuItem associateSourceItem = new JMenuItem("Associate Source Files");
					associateSourceItem.addActionListener(this);
					associateSourceItem.setActionCommand("project.associate.source");
					projectEditMenu.add(associateSourceItem);
					
					JMenuItem associateJavadocsItem = new JMenuItem("Associate Javadoc Files");
					associateJavadocsItem.addActionListener(this);
					associateJavadocsItem.setActionCommand("project.associate.javadocs");
					projectEditMenu.add(associateJavadocsItem);
					
					projectEditMenu.add(new JSeparator());
					
					JMenuItem addIntItem = new JMenuItem("Add Interface");
					addIntItem.addActionListener(this);
					addIntItem.setActionCommand("project.addInterface");
					projectEditMenu.add(addIntItem);
					
					JMenuItem remIntItem = new JMenuItem("Remove Interface");
					remIntItem.addActionListener(this);
					remIntItem.setActionCommand("project.removeInterface");
					//projectEditMenu.add(remIntItem);

					projectEditMenu.add(new JSeparator());
					
					JMenuItem changeProjectNameItem = new JMenuItem("Change Project Name");
					changeProjectNameItem.addActionListener(this);
					changeProjectNameItem.setActionCommand("project.edit.projectName");
					projectEditMenu.add(changeProjectNameItem);

					projectEditMenu.add(new JSeparator());

					JMenuItem removeProjectItem = new JMenuItem("Remove From List");
					removeProjectItem.addActionListener(this);
					removeProjectItem.setActionCommand("project.remove");
					projectEditMenu.add(removeProjectItem);
			
				JMenu projectViewMenu = new JMenu("View");
					JMenu interfacesMenu = new JMenu("Interfaces");				
						JMenuItem alphaItem = new JMenuItem("Alphabetically");
						alphaItem.addActionListener(this);
						alphaItem.setActionCommand("alphaWindow.popup");
						interfacesMenu.add(alphaItem);
						
						JMenuItem inherItem = new JMenuItem("Organized in Packages");
						inherItem.addActionListener(this);
						inherItem.setActionCommand("packageWindow.popup");
						interfacesMenu.add(inherItem);
					
					JMenuItem statisticsMenuItem = new JMenuItem("Statistics");
					statisticsMenuItem.addActionListener(this);
					statisticsMenuItem.setActionCommand("project.statistics");

					JMenuItem searchItem = new JMenuItem("Search");
					searchItem.addActionListener(this);
					searchItem.setActionCommand("project.search");
					
					projectViewMenu.add(interfacesMenu);
					projectViewMenu.add(statisticsMenuItem);
					projectViewMenu.add(new JSeparator());
					projectViewMenu.add(searchItem);
				
				JMenu windowMenu = new JMenu("Window");
					windowMenu.addMenuListener(this);
					windowMenu.setActionCommand("windowMenuSelected");
				
					JMenuItem renameItem = new JMenuItem("Rename Tab");
					renameItem.addActionListener(this);
					renameItem.setActionCommand("rename.tab");
					windowMenu.add(renameItem);
					
					JMenuItem removeItem = new JMenuItem("Remove Tab");
					removeItem.addActionListener(this);
					removeItem.setActionCommand("remove.tab");
					windowMenu.add(removeItem);					
						
					windowMenu.add(new JSeparator());
											
					JMenuItem openNewWindow = new JMenuItem("New Window");
						openNewWindow.addActionListener(this);
						openNewWindow.setActionCommand("open.newWindow");
						windowMenu.add(openNewWindow);
					
					JMenuItem openNewTab = new JMenuItem("New Tab");
						openNewTab.addActionListener(this);
						openNewTab.setActionCommand("open.newTab");
						windowMenu.add(openNewTab);
					
				JMenu helpMenu = new JMenu("Help");
					JMenuItem aboutItem = new JMenuItem("About");
					aboutItem.addActionListener(this);
					aboutItem.setActionCommand("about");
					helpMenu.add(aboutItem);
				
			projectMenuBar.add(projectFileMenu);
			projectMenuBar.add(projectEditMenu);
			projectMenuBar.add(projectViewMenu);
			projectMenuBar.add(windowMenu);
			projectMenuBar.add(helpMenu);
			
		 return projectMenuBar;
	}
	
	public JList getList()
	{
		return list;
	}
		
	public void setHawkDesktop(HawkDesktop desk)
	{
		desktop = desk;
	}
	
	public HawkDesktop getHawkDesktop()
	{
		return desktop;
	}
	
	/**
	* This sets ProjectVec
	* @param vec The vector to set equal to ProjectVec
	*/
		public void setProjectVec(Vector vec)
		{
			ProjectVec = vec;
		}
	
	/**
	* This returns the Vector of Project objects that the user has opened
	* @return ProjectVec
	*/
		public Vector getProjectVec()
		{
			return ProjectVec;
		}
	
	/**
	* This returns the currently selected Project from the Jlist in the GUI
	* @return The Project object currently selected
	*/
		public Project getSelectedProject()
		{
			int selectedIndex = list.getSelectedIndex();
		
			if (selectedIndex < 0)
				return null;
			else
				return (Project)(ProjectVec.elementAt(selectedIndex));
		}
		
		public void setSelectedProjectsName(String name)
		{
			int selectedIndex = list.getSelectedIndex();
			if (selectedIndex >= 0)
				model.set(selectedIndex, name);
			if (getSelectedProject() != null)
				getSelectedProject().setProjectName(name);
		}
	
	/**
	* This sets openNewAlphaWindowOnSelect to either true or false.  Set it to true 
	* if you want a new AlphabeticalListGUI to appear
	* when the user selects a new Project from the Jlist.  Set to false if you
	* want the new Projects information to appear in the AlphabeticalListGUI
	* already open.  Note:  In AlphabeticalListGUI.java when the user closes the
	* GUI, the setOpenNewAlphaWindowOnSelect(true) is automatically selected to
	* have a new GUI window pop up.
	* @param bol True for a new window to pop up, and false otherwise.
	*/	
		public void setOpenNewAlphaWindowOnSelect(boolean bol)
		{
			openNewAlphaWindowOnSelect = bol;
		}
	
	/**
	* This is used in if statements to decide whether or not to open a new 
	* AlphabeticalListGUI if the current class trying to obtain the information
	* can't due to encapsulation.
	* @return The value of openNewAlphaWindowOnSelect
	*/
		public boolean getOpenNewAlphaWindowOnSelect()
		{
			return openNewAlphaWindowOnSelect;
		}
		
		/**
		 * This method processes an ActionEvent.  If a gui has a ProjectSelectorJPanel in it and has buttons or other components to throw ActionEvents.
		 * Then, that gui can call this method in its actionPerformed(ActionEvent) method to reduce code.  That way if the user selects to view the AlphabeticalListGUI
		 * from the ProjectSelectorJPanel or from a button the same gui pops up.
		 * @param event
		 */
		public void processSentEvent(ActionEvent event)
		{
			System.out.println("command="+event.getActionCommand());
			
			if (event.getActionCommand().equals("exit"))
			{
				desktop.dispose();
			}
			else if (event.getActionCommand().trim().equals("project.new"))
			{
				CreateNewProjectGUI cnpg = new CreateNewProjectGUI(this, new UnableToLoadClassGUI());
				cnpg.setVisible(true);
			}
			else if (event.getActionCommand().equals("project.edit.projectName"))
			{
				int index = list.getSelectedIndex();
				boolean con = true;
			
				if (index < 0)
					con = false;
				else if ( ((String)(model.getElementAt(index))).equals("No projects listed") )
					con = false;
			
				if (con)
				{	
					ChangeProjectNameGUI gui = new ChangeProjectNameGUI();
					gui.setVisible(true);
				}
				else
				{
					//custom title, informational icon
					if ( (index == 0) && ((String)(model.getElementAt(index))).equals("No projects listed") )
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu to edit its name"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"You need to select a project to edit its name"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
			else if (event.getActionCommand().equals("project.search"))
			{
				int index = list.getSelectedIndex();
				boolean con = true;

				if (index < 0)
					con = false;
				else if ( ((String)(model.getElementAt(index))).equals("No projects listed") )
					con = false;

				if (con)
				{	
					SearchGUI gui = new SearchGUI(getSelectedProject().getInterfaceVec());
					gui.setVisible(true);
				}
				else
				{
					//custom title, informational icon
					if ( (index == 0) && ((String)(model.getElementAt(index))).equals("No projects listed") )
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu to\n" +
																					   "search through its classes and interfaces"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"You need to select a project to search\n" +
																					   "through its classes and interfaces"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				}			
			}
			else if (event.getActionCommand().equals("project.open"))
			{
				JFrame frame = new JFrame();
				frame.setSize(500,400);
				Container framePane = frame.getContentPane();
			
				JPanel mainPanel = new JPanel();
				mainPanel.setLayout(new BorderLayout());
			
				JFileChooser chooser = new JFileChooser();
					RobustFileFilter jdfFilter = new RobustFileFilter();
					jdfFilter.addExtension("jdf");
					chooser.setFileFilter(jdfFilter);			
				int returnVal = chooser.showOpenDialog(frame);
			
				mainPanel.add(chooser, BorderLayout.CENTER);
						
				framePane.add(mainPanel);
				framePane.setVisible(true);
			
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					String fileName = chooser.getSelectedFile().getAbsoluteFile().toString();
								
					String project_Name = "";
					try
					{
						dataFileUtilities data = new dataFileUtilities(fileName, true);
											
						//this gets the projects name
						project_Name = Project.getProjectName(fileName);
					
						//this will remove the first element which states "No projects listed"
						//if this is the first time the user has opened a new project
						if ( ((String)(model.elementAt(0))).equals("No projects listed") )
						{
							ProjectVec.remove(0);
							model.remove(0);
						}
					
						//now to add the project to the list
						//this constructor makes a Project object with a new Interface object in interfaceVec
						Project newProject = new Project(data, project_Name);
										
						ProjectVec.add(newProject);
					
						model.addElement(project_Name);
					
					}
					catch(FileNotFoundException e)
					{
						//custom title, error icon
							JOptionPane opPane = new JOptionPane();
							JOptionPane.showMessageDialog(opPane,
								"The file you selected is either not of the correct type or has been corrupted.\nPlease choose a file that ends in .jdf",
								"File Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else if (event.getActionCommand().equals("project.save"))
			{
					if (getSelectedProject() != null)
					{
					
					}
			}
			else if (event.getActionCommand().equals("project.saveAs"))
			{
				int index = list.getSelectedIndex();
				boolean con = true;
			
				if (index < 0)
					con = false;
				else if ( ((String)(model.getElementAt(index))).equals("No projects listed") )
					con = false;
			
				if (con)
				{	
					JFrame frame = new JFrame();
					frame.setSize(500,400);
					Container framePane = frame.getContentPane();
			
					JPanel mainPanel = new JPanel();
					mainPanel.setLayout(new BorderLayout());
			
					JFileChooser chooser = new JFileChooser();
					int returnVal = chooser.showSaveDialog(frame);
			
					mainPanel.add(chooser, BorderLayout.CENTER);
						
					framePane.add(mainPanel);
					framePane.setVisible(true);
			
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						String fileName = chooser.getSelectedFile().getAbsoluteFile().toString();
						dataFileUtilities.writeJDFFile(getSelectedProject(), fileName);
					}
				}
				else
				{
					//custom title, informational icon
					if ( (index == 0) && ((String)(model.getElementAt(index))).equals("No projects listed") )
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu to save\nits contents"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"You need to select a project to save\nits contents"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
			else if (event.getActionCommand().equals("project.remove"))
			{
				int selectedIndex = list.getSelectedIndex();
			
				if (selectedIndex >= 0)
				{	
					ProjectVec.remove(selectedIndex);
					model.remove(selectedIndex);
				}
			
				if (model.isEmpty())
				{
					ProjectVec.add(new Project());
					model.addElement("No projects listed");
				}
			
			}
			else if (event.getActionCommand().equals("alphaWindow.popup"))
			{
				int index = list.getSelectedIndex();
				boolean con = true;
			
				if (index < 0)
					con = false;
				else if ( ((String)(model.getElementAt(index))).equals("No projects listed") )
					con = false;
			
				if (con)
				{
					AlphabeticalListGUI alphaGUI3 = new AlphabeticalListGUI(getSelectedProject(), this, getSelectedProject().getProjectName(), false, false,desktop);
					alphaGUI3.setVisible(true);
					if (desktop.getSelectedDesktop() != null)
						desktop.getSelectedDesktop().add(alphaGUI3);
					else
					{
						JOptionPane opPane = new JOptionPane();
							JOptionPane.showMessageDialog(opPane,"You need to select a tab to view the\nproject's interfaces alphabetically"
								,"Note"
								,JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else
				{
					//custom title, informational icon
					if ( (index == 0) && ((String)(model.getElementAt(index))).equals("No projects listed") )
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu to view\nits interfaces alphabetically"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"You need to select a project to view\nits interfaces alphabetically"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
			else if (event.getActionCommand().equals("project.print"))
			{
				PrintGUI pg = new PrintGUI(ProjectVec, "Current Projects");
				pg.setVisible(true);
			}
			else if (event.getActionCommand().equals("packageWindow.popup"))
			{
				int index = list.getSelectedIndex();
				boolean con = true;
			
				if (index < 0)
					con = false;
				else if ( ((String)(model.getElementAt(index))).equals("No projects listed") )
					con = false;
			
				if (con)
				{
					PackageTreeGUI packageGUI = new PackageTreeGUI(getSelectedProject(), false, false, true, true,desktop);
					packageGUI.setVisible(true);
					if (desktop.getSelectedDesktop() != null)
						desktop.getSelectedDesktop().add(packageGUI);
					else
					{
						JOptionPane opPane = new JOptionPane();
							JOptionPane.showMessageDialog(opPane,"You need to select a tab to view the\nproject's interfaces by package name"
								,"Note"
								,JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else
				{
					//custom title, informational icon
					if ( (index == 0) && ((String)(model.getElementAt(index))).equals("No projects listed") )
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu to view\nits interfaces organized in packages"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"You need to select a project to view\nits interfaces organized in packages"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
			else if (event.getActionCommand().equals("project.associate.source"))
			{
				int index = list.getSelectedIndex();
				boolean con = true;
			
				if (index < 0)
					con = false;
				else if ( ((String)(model.getElementAt(index))).equals("No projects listed") )
					con = false;			
			
				if (con)
				{
					FileAssociationGUI sourceGUI = new FileAssociationGUI(getSelectedProject(), FileAssociationManager.JAVASOURCE);
					sourceGUI.setVisible(true);
				}
				else
				{
					//custom title, informational icon
					if ( (index == 0) && ((String)(model.getElementAt(index))).equals("No projects listed") )
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu and select it\nto associate source code files with it"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane
							,"You need to select a project to associate\nsource code files with it"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
			else if (event.getActionCommand().equals("project.associate.javadocs"))
			{
				int index = list.getSelectedIndex();
				boolean con = true;
			
				if (index < 0)
					con = false;
				else if ( ((String)(model.getElementAt(index))).equals("No projects listed") )
					con = false;			
			
				if (con)
				{
					FileAssociationGUI javadocsGUI = new FileAssociationGUI(getSelectedProject(), FileAssociationManager.JAVADOCS);
					javadocsGUI.setVisible(true);
				}
				else
				{
					//custom title, informational icon
					if ( (index == 0) && ((String)(model.getElementAt(index))).equals("No projects listed") )
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu and select it\nto associate javadoc files with it"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane
							,"You need to select a project to associate\njavadoc files with it"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
			else if (event.getActionCommand().equals("project.statistics"))
			{
				int index = list.getSelectedIndex();
				boolean con = true;
			
				if (index < 0)
					con = false;
				else if ( ((String)(model.getElementAt(index))).equals("No projects listed") )
					con = false;
			
				if (con)
				{	
					StatisticsGUI statsGUI = new StatisticsGUI(getSelectedProject());
					statsGUI.setVisible(true);
				}
				else
				{
					//custom title, informational icon
					if ( (index == 0) && ((String)(model.getElementAt(index))).equals("No projects listed") )
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu\nand select it to view its statistics"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"You need to select a project to view its statistics"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
			else if (event.getActionCommand().equals("project.addInterface"))
			{
				int index = list.getSelectedIndex();
				boolean con = true;
			
				if (index < 0)
					con = false;
				else if ( ((String)(model.getElementAt(index))).equals("No projects listed") )
					con = false;
			
				if (con)
				{
					AddInterfaceGUI gui = new AddInterfaceGUI(this, new UnableToLoadClassGUI(), getSelectedProject());
					gui.setVisible(true);
				}
				else
				{
					//custom title, informational icon
					if ( (index == 0) && ((String)(model.getElementAt(index))).equals("No projects listed") )
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu\nand select it to add classes and interfaces to it"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"You need to select a project to add classes and interfaces to it"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}				
			else if (event.getActionCommand().equals("about"))
			{
				AboutGUI aboutGUI = new AboutGUI();
				aboutGUI.setVisible(true);
			}
			else if (event.getActionCommand().equals("open.newWindow"))
			{
				HawkDesktop newHawkDesktop = new HawkDesktop(ProjectVec);
				newHawkDesktop.setVisible(true);
			}
			else if (event.getActionCommand().equals("open.newTab"))
			{
				int num = desktop.getTabbedPane().getTabCount() + 1;
				desktop.getTabbedPane().add(new JDesktopPane());
				desktop.getTabbedPane().setTitleAt(num-1,"Tab"+num);
			}
			else if (event.getActionCommand().equals("rename.tab"))
			{	
				int index = desktop.getTabbedPane().getSelectedIndex();
				
				if (index == -1)
				{
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane
						,"You need to select a tab to change its name"
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					ChangeTabNameGUI gui = new ChangeTabNameGUI();
					gui.setVisible(true);
				}
			}
			else if (event.getActionCommand().equals("remove.tab"))
			{
				int index = desktop.getTabbedPane().getSelectedIndex();
				desktop.getTabbedPane().remove(index);				
			}
		}
	
	/**
	* Handles the action events that happen in the GUI such as button clicks and
	* menu selections etc.
	* @param event The ActionEvent that occured
	*/
	public void actionPerformed( ActionEvent event)
	{
		processSentEvent(event);
	}
	
	//this is for the menu listener's methods
	public void menuSelected(MenuEvent event)
	{
	}
	
	public void menuDeselected(MenuEvent event)
	{
	}
	
	public void menuCanceled(MenuEvent event)
	{
	}
	
	public void valueChanged(ListSelectionEvent e)
	{
		//list.getSelectedIndex() returns the current selected index with -1 if nothing is selected
		//the nth selected index refers to the nth Project object in ProjectVec
		
		if (e.getValueIsAdjusting() == false)
		{
		}
	}
	
	/**
	* This class handles what to do when the user wants to open
	* a popup menu.
	*/
	class PopupListener extends MouseAdapter 
	{
        	public void mousePressed(MouseEvent e)
		{
			maybeShowPopup(e);
        	}

		public void mouseReleased(MouseEvent e)
		{
			maybeShowPopup(e);
        	}

		private void maybeShowPopup(MouseEvent e)
		{
			if (e.isPopupTrigger())
			{
				popup.show(e.getComponent(),
					e.getX(), e.getY());
			}
		}
	}
	
	private class ChangeProjectNameGUI extends JFrame implements ActionListener
	{
		private JTextField textField;
		
		public ChangeProjectNameGUI()
		{
			setTitle("Editing "+getSelectedProject().getProjectName()+"'s Name");
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(this);
				buttonPanel.add(cancelButton);
				
				JButton okButton = new JButton("Ok");
				okButton.addActionListener(this);
				buttonPanel.add(okButton);
			
			JPanel textPanel = new JPanel();
			textPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				textField = new JTextField(getSelectedProject().getProjectName(), 15);
				JLabel label = new JLabel("New Name=");
				textPanel.add(label);
				textPanel.add(textField);
			
			mainPanel.add(new JLabel("Enter the project's new name below."), BorderLayout.NORTH);
			mainPanel.add(textPanel, BorderLayout.CENTER);
			mainPanel.add(buttonPanel, BorderLayout.SOUTH);
			getContentPane().add(mainPanel);

			pack();
			setVisible(true);
		}
		
		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals("Ok"))
			{
				getSelectedProject().setProjectName(textField.getText());
				int selectedIndex = list.getSelectedIndex();
				model.setElementAt(getSelectedProject().getProjectName(), selectedIndex);
			}

				dispose();
		}
	}
	
	private class ChangeTabNameGUI extends JFrame implements ActionListener
	{
		private JTextField textField;
		
		public ChangeTabNameGUI()
		{
			setTitle("Editing the Selected Tab's Name");
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(this);
				buttonPanel.add(cancelButton);
				
				JButton okButton = new JButton("Ok");
				okButton.addActionListener(this);
				buttonPanel.add(okButton);
			
			JPanel textPanel = new JPanel();
			textPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				textField = new JTextField("", 15);
				JLabel label = new JLabel("New Tab Name=");
				textPanel.add(label);
				textPanel.add(textField);
			
			mainPanel.add(new JLabel("Enter the tab's new name below."), BorderLayout.NORTH);
			mainPanel.add(textPanel, BorderLayout.CENTER);
			mainPanel.add(buttonPanel, BorderLayout.SOUTH);
			getContentPane().add(mainPanel);

			pack();
			setVisible(true);
		}
		
		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals("Ok"))
			{
				int index = desktop.getTabbedPane().getSelectedIndex();
				desktop.getTabbedPane().setTitleAt(index, textField.getText());
			}

				dispose();
		}
	}
}

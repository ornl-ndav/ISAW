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
 * Revision 1.4  2004/03/12 19:46:17  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.2  2004/02/07 05:31:17  bouzekc
 * Commented out debugging println.
 *
 * Revision 1.1  2004/02/07 05:09:43  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.panel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import devTools.Hawk.classDescriptor.gui.frame.RemoveInterfaceGUI;
import devTools.Hawk.classDescriptor.gui.frame.SearchGUI;
import devTools.Hawk.classDescriptor.gui.frame.StatisticsGUI;
import devTools.Hawk.classDescriptor.gui.frame.UnableToLoadClassGUI;
import devTools.Hawk.classDescriptor.gui.internalFrame.AlphabeticalListGUI;
import devTools.Hawk.classDescriptor.gui.internalFrame.PackageTreeGUI;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.FileAssociationManager;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * This is a special type of JPanel that contains a list of all of the currently opened projects.  This class 
 * handles any events that are thrown when the user selects a project from the list.
 * @author Dominic Kramer
 */
public class ProjectSelectorJPanel extends JPanel implements ActionListener, ListSelectionListener, MenuListener
{
	//these are declared here to allow the actionPerformed() method to find them
	//and are components found on the GUI	
	/**
	* This is the JList of names for all of the projects currently open
	* and is placed on the GUI
	*/
	protected JList list;
	/** The model that handles modifying the list.*/
	protected DefaultListModel model;
	/** the popup window */
	protected JPopupMenu popup;
	/** The HawkDesktop on which this panel is placed (after being placed on a JInternalFrame). */
	protected HawkDesktop desktop;
	
	//these attributes are used to describe the ProjectSelectorJPanel
	/** 
	* This is a Vector of Project objects, and each 
	* time a Project object is made (new) or opened from a file
	* it is placed at the end of this Vector.
	*/
//	protected Vector ProjectVec;
	/** 
	* Set this to true to have a new AlphabeticalListGUI 
	* pop up when the user selects an item in the list.  This is no 
	* used an will be removed.
	*/
//	protected boolean openNewAlphaWindowOnSelect;
	
	protected boolean noProjectsListed;
	
//	protected JMenuItem copyToPreviousTabItem;
//	protected JMenuItem previousTabItem;
	
	/**
	 * Create a new ProjectSelectorJPanel.
	 * @param PROVEC The Vector of Project objects to add to this panel's list.
	 * @param desk The HawkDesktop on which this panel is located.
	 */
	public ProjectSelectorJPanel(Vector PROVEC, HawkDesktop desk)
	{
		noProjectsListed = true;
		desktop = desk;
		
		//now to instantiate the Continer and main panel everything is added on
			setLayout(new BorderLayout());
		
		//this initially has a new window pop up if the list is selected
//			openNewAlphaWindowOnSelect = true;
		
		//now to make the list for the gui		
			//this model allows you to modify the list
			model = new DefaultListModel();
			
			if (PROVEC != null)
			{
			//	ProjectVec = PROVEC;
				for (int i=0; i<PROVEC.size(); i++)
					model.addElement( (Project)PROVEC.elementAt(i));
				
				noProjectsListed = false;
			}
			else
			{
//				ProjectVec = new Vector();
//				ProjectVec.add(new Project());
				Project tempProject = new Project();
				tempProject.setProjectName("No projects listed");
				model.addElement(tempProject);
			}

			list = new JList(model);
			list.addListSelectionListener(this);
			list.setVisibleRowCount(-1);
			//the following only allows one item to be selected at a time
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);		
				
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
	
	/**
	 * Used in subclasses of this class.
	 */
	protected ProjectSelectorJPanel() {}
	
	/**
	 * Get this panel.
	 * @return this
	 */
	public JPanel getProjectSelectorJPanel()
	{
		return this;
	}
	
	/**
	 * Get the model that is used to modify the panel's Jlist.
	 * @return The panel's list's model.
	 */
	public DefaultListModel getModel()
	{
		return model;
	}
	
	/**
	 * Returns a Vector of Project s that are in this panel's list's model.
	 */
	public Vector getProjectVec()
	{
		Vector proVec = new Vector();
		for (int i=0; i<model.size(); i++)
			proVec.addElement((Project)model.elementAt(i));
			
		return proVec;
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
					 projectFileMenu.add(projectSaveItem);
			
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
					projectEditMenu.add(remIntItem);

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
	
	/**
	 * Get this panel's list.
	 * @return The panel's list.
	 */
	public JList getList()
	{
		return list;
	}
	
	/**
	 * Set the HawkDesktop on which this panel is placed.
	 * @param desk The HawkDesktop.
	 */
	public void setHawkDesktop(HawkDesktop desk)
	{
		desktop = desk;
	}
	
	/**
	 * Get the HawkDesktop on which this panel is placed.
	 * @return The HawkDesktop.
	 */
	public HawkDesktop getHawkDesktop()
	{
		return desktop;
	}
	
	/**
	 * Returns true if no projects are listed and false if there are projects listed.
	 */
	public boolean noProjectsListed()
	{
		return noProjectsListed;
	}
	
	/**
	 * Sets the flag that describes if there are projects currently listed.
	 * @param bol True if no projects are listed and false if there are projects currently listed.
	 */
	public void setNoProjectsListed(boolean bol)
	{
		noProjectsListed = bol;
	}
		
	/**
	* This returns the currently selected Project from the Jlist in the GUI
	* @return The Project object currently selected
	*/
	public Project[] getSelectedProjects()
	{
		int[] selectedIndexArray = list.getSelectedIndices();
		Vector proVec = new Vector();
		
		for (int i=0; i<selectedIndexArray.length; i++)
		{
			if (selectedIndexArray[i]>=0)
				proVec.add((Project)model.elementAt(selectedIndexArray[i]));
		}
		Project[] proArr = new Project[proVec.size()];
		for (int i=0; i<proVec.size(); i++)
			proArr[i] = (Project)proVec.elementAt(i);
			
		return proArr;
	}
	
	/**
	 * Sets the names of the currently selected projects if one of the elements from 
	 * the list is selected.  This method updates the name on the list and sets the 
	 * project's name.
	 * @param name A Vector of Strings each of which is the name of one of the selected projects.  
	 * The number elements in the Vector name has to be greater than or equal to number of selected 
	 * elements from the list.
	 */	
	public void setSelectedProjectsNames(Vector name)
	{
		int[] selectedIndex = list.getSelectedIndices();
		Project pro = new Project();
		for (int i=0; i<selectedIndex.length; i++)
		{
			if (selectedIndex[i]>=0)
			{
				pro = (Project)model.elementAt(selectedIndex[i]);
				pro.setProjectName((String)name.elementAt(i));
				model.set(selectedIndex[i],pro);
			}
		}
	}		
		/**
		 * This method is used exclusively for the processSentEvent(ActionEvent) method to 
		 * handle saving the project to a file.  This method opens a window asking the user for 
		 * the filename to save the project to.
		 */
		private void promptForFileToSaveProject(Project pro)
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
				pro.getData().setFileName(fileName);
				pro.writeNativeHawkFile();
			}
		}
		
		
		/**
		 * This method processes an ActionEvent.  If a gui has a ProjectSelectorJPanel in it and has buttons or other components to throw ActionEvents.
		 * Then, that gui can call this method in its actionPerformed(ActionEvent) method to reduce code.  That way if the user selects to view the AlphabeticalListGUI
		 * from the ProjectSelectorJPanel or from a button the same gui pops up.
		 * @param event
		 */
		public void processSentEvent(ActionEvent event)
		{
			if (event.getActionCommand().equals("exit"))
			{
				desktop.dispose();
				if (desktop.isFirstWindowOpen())
					System.exit(0);
			}
			else if (event.getActionCommand().trim().equals("project.new"))
			{
				CreateNewProjectGUI cnpg = new CreateNewProjectGUI(this, new UnableToLoadClassGUI());
				cnpg.setVisible(true);
			}
			else if (event.getActionCommand().equals("project.edit.projectName"))
			{
				if (!noProjectsListed)
				{
					Project[] proArr = getSelectedProjects();
				
					if (proArr.length == 0)
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu to edit its name"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						ChangeProjectNameGUI gui = new ChangeProjectNameGUI();
						gui.setVisible(true);
					}
				}
				else
				{
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,"You need to select a project to edit its name"
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (event.getActionCommand().equals("project.search"))
			{
				if (!noProjectsListed)
				{
					Project[] proArr = getSelectedProjects();
				
					if (proArr.length == 0)
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"You need to select a project to search\n" +
																					   "through its classes and interfaces"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					Vector interfacesVec = new Vector();
					Vector intfVec = new Vector();
					String title = "Search ";
					for (int i=0; i<proArr.length; i++)
					{
						if (i == proArr.length-1)
							title += proArr[i].getProjectName();
						else if (i == proArr.length-2)
							title += proArr[i].getProjectName()+", and ";
						else
							title += proArr[i].getProjectName()+", ";
						intfVec = proArr[i].getInterfaceVec();
						for (int j=0; j<intfVec.size(); j++)
							interfacesVec.add((Interface)intfVec.elementAt(j));
					}
					
					SearchGUI gui = new SearchGUI(interfacesVec,desktop);
					gui.setVisible(true);
					gui.setTitle(title);
				}
				else
				{
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,"Open a project from the file menu to\n" +
																				   "search through its classes and interfaces"
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
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
					jdfFilter.addExtension(SystemsManager.getHawkFileExtensionWithoutPeriod());
					chooser.setFileFilter(jdfFilter);			
				int returnVal = chooser.showOpenDialog(frame);
			
				mainPanel.add(chooser, BorderLayout.CENTER);
						
				framePane.add(mainPanel);
				framePane.setVisible(true);
			
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					String fileName = chooser.getSelectedFile().getAbsoluteFile().toString();
						//this will remove the first element which states "No projects listed"
						//if this is the first time the user has opened a new project
						if (noProjectsListed)
						{
							model.remove(0);
						}
						
						//now to add the project to the list
						//this constructor makes a Project object with a new Interface object in interfaceVec
						Project newProject = new Project(fileName, true);					
						model.addElement(newProject);
						noProjectsListed = false;
				}
			}
			else if (event.getActionCommand().equals("project.save"))
			{
				if (!noProjectsListed)
				{	
					Project[] proArr = getSelectedProjects();
					if (proArr.length == 0)
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"You need to select a project to save its contents."
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						for (int i=0; i<proArr.length; i++)
						{
							if (proArr[i].getData().isAlreadySaved())
								proArr[i].writeNativeHawkFileWithoutPrompting();
							else
								promptForFileToSaveProject(proArr[i]);
						}
					}
				}
				else
				{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu to save\nits contents"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (event.getActionCommand().equals("project.saveAs"))
			{
				if (!noProjectsListed)
				{
					Project[] proArr = getSelectedProjects();
				
					if (proArr.length == 0)
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"You need to select a project to save\nits contents"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						for (int i=0; i<proArr.length; i++)
							promptForFileToSaveProject(proArr[i]);
					}
				}
				else
				{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu to save\nits contents"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (event.getActionCommand().equals("project.remove"))
			{
				int[] selectedIndex = list.getSelectedIndices();
			
				for (int i=selectedIndex.length-1; i>=0; i--)
				{
					if (selectedIndex[i] >= 0)
					{	
						model.remove(selectedIndex[i]);
					}
				}
				
				if (model.isEmpty())
				{
					Project tempProject = new Project();
					tempProject.setProjectName("No projects listed");
					model.addElement(tempProject);
					noProjectsListed = true;
				}
			
			}
			else if (event.getActionCommand().equals("alphaWindow.popup"))
			{
				if (!noProjectsListed)
				{
					Project[] proArr = getSelectedProjects();
				
					if (proArr.length == 0)
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"You need to select a project to view\nits interfaces alphabetically"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				
					for (int i=0; i<proArr.length; i++)
					{
						AlphabeticalListGUI alphaGUI3 = new AlphabeticalListGUI(proArr[i], proArr[i].getProjectName() +" (Alphabetical Listing)", false, false,desktop);
						alphaGUI3.setVisible(true);
						if (desktop.getSelectedDesktop() == null)
							createNewTab();
							
							desktop.getSelectedDesktop().add(alphaGUI3);
							alphaGUI3.setAsSelected(true);
					}
				}
				else
				{
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,"Open a project from the file menu to view\nits interfaces alphabetically"
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (event.getActionCommand().equals("project.print"))
			{
				Vector proVec = new Vector();
				for (int i=0; i<model.size(); i++)
					proVec.add((Project)model.elementAt(i));
				
				PrintGUI pg = new PrintGUI(proVec, "Current Projects");
				pg.setVisible(true);
			}
			else if (event.getActionCommand().equals("packageWindow.popup"))
			{
				if (!noProjectsListed)
				{
					Project[] proArr = getSelectedProjects();
				
					if (proArr.length == 0)
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"You need to select a project to view\nits interfaces organized in packages"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				
					for (int i=0; i<proArr.length; i++)
					{
						PackageTreeGUI packageGUI = new PackageTreeGUI(proArr[i],proArr[i].getProjectName()+" (Package Listing)", false, false, true, true,desktop);
						packageGUI.setVisible(true);
						if (desktop.getSelectedDesktop() == null)
							createNewTab();
							
						desktop.getSelectedDesktop().add(packageGUI);
						packageGUI.setAsSelected(true);
					}
				}
				else
				{
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,"Open a project from the file menu to view\nits interfaces organized in packages"
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (event.getActionCommand().equals("project.associate.source"))
			{
				if (!noProjectsListed)
				{
					Project[] proArr = getSelectedProjects();
				
					if (proArr.length == 0)
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane
							,"You need to select a project to associate\nsource code files with it"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				
					for (int i=0; i<proArr.length; i++)
					{
						FileAssociationGUI sourceGUI = new FileAssociationGUI(proArr[i].getInterfaceVec(), FileAssociationManager.JAVASOURCE,"Assigning Java Source Files for the Project "+proArr[i].getProjectName());
						sourceGUI.setVisible(true);
					}
				}
				else
				{
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,"Open a project from the file menu and select it\nto associate source code files with it"
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (event.getActionCommand().equals("project.associate.javadocs"))
			{
				if (!noProjectsListed)
				{
					Project[] proArr = getSelectedProjects();
				
					if (proArr.length == 0)
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane
							,"You need to select a project to associate\njavadoc files with it"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				
					for (int i=0; i<proArr.length; i++)
					{
						FileAssociationGUI sourceGUI = new FileAssociationGUI(proArr[i].getInterfaceVec(), FileAssociationManager.JAVADOCS,"Assigning Javadoc Files for the Project "+proArr[i].getProjectName());
						sourceGUI.setVisible(true);
					}
				}
				else
				{
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,"Open a project from the file menu and select it\nto associate javadoc files with it"
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (event.getActionCommand().equals("project.statistics"))
			{
				if (!noProjectsListed)
				{
					Project[] proArr = getSelectedProjects();
				
					if (proArr.length == 0)
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu\nand select it to view its statistics"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
				
					for (int i=0; i<proArr.length; i++)
					{
						StatisticsGUI statsGUI = new StatisticsGUI(proArr[i]);
						statsGUI.setVisible(true);
					}
				}
				else
				{
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,"You need to select a project to view its statistics"
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (event.getActionCommand().equals("project.addInterface"))
			{
				if (!noProjectsListed)
				{
					Project[] proArr = getSelectedProjects();
				
					if (proArr.length == 0)
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu\nand select it to add classes and interfaces to it"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					
					for (int i=0; i<proArr.length; i++)
					{
						AddInterfaceGUI gui = new AddInterfaceGUI(this, new UnableToLoadClassGUI(), proArr[i]);
						gui.setVisible(true);
					}
				}
				else
				{
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,"You need to select a project to add classes and interfaces to it"
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (event.getActionCommand().equals("project.removeInterface"))
			{
				if (!noProjectsListed)
				{
					Project[] proArr = getSelectedProjects();
				
					if (proArr.length == 0)
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu\nand select it to remove classes and interfaces from it"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					
					for (int i=0; i<proArr.length; i++)
					{
						RemoveInterfaceGUI gui = new RemoveInterfaceGUI(proArr[i],false,false,true,true);
						gui.setVisible(true);
					}
				}
				else
				{
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,"You need to select a project to add classes and interfaces to it"
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
				}
			}				
			else if (event.getActionCommand().equals("about"))
			{
				AboutGUI aboutGUI = new AboutGUI();
				aboutGUI.setVisible(true);
			}
			else if (event.getActionCommand().equals("open.newWindow"))
			{
				Vector proVec = new Vector();
				for (int i=0; i<model.size(); i++)
					proVec.add((Project)model.elementAt(i));
				
				HawkDesktop newHawkDesktop = new HawkDesktop(proVec,false);
				newHawkDesktop.setHawkDesktopTitle("Secondary Hawk");
				newHawkDesktop.setVisible(true);
			}
			else if (event.getActionCommand().equals("open.newTab"))
			{
				createNewTab();
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
				if (index >= 0)
					desktop.getTabbedPane().remove(index);				
			}
		}
	
	/**
	 * Used to create a new tab on the HawkDesktop this panel is placed onto.
	 * The tab is also given the name "Tab (n+1)" where n is the number of tabs 
	 * currently displayed.
	 */
	private void createNewTab()
	{
		int num = desktop.getTabbedPane().getTabCount() + 1;
		desktop.getTabbedPane().add(new JDesktopPane());
		desktop.getTabbedPane().setTitleAt(num-1,"Tab"+num);
	}
		
	/**
	* Handles the action events that happen in the GUI such as button clicks and
	* menu selections etc.
	* @param event The ActionEvent that occured
	*/
	public void actionPerformed( ActionEvent event)
	{
		ActionPerformedThread thread = new ActionPerformedThread(event,this);
		thread.start();
	}
	
	//this is for the menu listener's methods
	/**
	 * Handles the menus being selected.
	 */
	public void menuSelected(MenuEvent event)
	{
	}
	
	/**
	 * Handles the menus being deselected.
	 */
	public void menuDeselected(MenuEvent event)
	{
	}
	
	/**
	 * Handles the menu being cancelled.
	 */
	public void menuCanceled(MenuEvent event)
	{
	}
	
	/**
	 * Handles the list changing.
	 */
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
	* @author Dominic Kramer
	*/
	class PopupListener extends MouseAdapter 
	{
		/** Handles the mouse being pressed. */
        public void mousePressed(MouseEvent e)
		{
			maybeShowPopup(e);
        }
        
        /** Handles the mouse button being released. */
		public void mouseReleased(MouseEvent e)
		{
			maybeShowPopup(e);
        }
        
        /** Handles showing the popup menu. */
		private void maybeShowPopup(MouseEvent e)
		{
			if (e.isPopupTrigger())
			{
				popup.show(e.getComponent(),
					e.getX(), e.getY());
			}
		}
	}
	
	/**
	 * A window that allows the user to change the currently selected project's name.
	 * @author Dominic Kramer
	 */
	private class ChangeProjectNameGUI extends JFrame implements ActionListener
	{
		/** The field on which the user enters the new name. */
		private JTextField[] textfieldArr;
		private Project[] proArr;
		private int[] selectedIndicesArr;
		
		/**
		 * Create a new ChangeProjectNameGUI.
		 */
		public ChangeProjectNameGUI()
		{			
			setTitle("Editing the project(s) Name(s)");
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
			
			selectedIndicesArr = list.getSelectedIndices();	
			proArr = getSelectedProjects();
			textfieldArr = new JTextField[proArr.length];
			
			JPanel panelsPanel = new JPanel();
			panelsPanel.setLayout(new GridLayout(proArr.length,0));
			
			for (int i=0; i<proArr.length; i++)
			{
				JPanel textPanel = new JPanel();
				textPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
					JTextField textField = new JTextField(proArr[i].getProjectName(), 15);
					JLabel label = new JLabel("Old Name = "+proArr[i].getProjectName()+" and New Name=");
					textPanel.add(label);
					textPanel.add(textField);
					
					textfieldArr[i] = textField;
					panelsPanel.add(textPanel);
			}
			
			mainPanel.add(new JLabel("Enter the projects' new names below."), BorderLayout.NORTH);
			mainPanel.add(panelsPanel, BorderLayout.CENTER);
			mainPanel.add(buttonPanel, BorderLayout.SOUTH);
			getContentPane().add(mainPanel);

			pack();
			setVisible(true);
		}
		
		/**
		 * Handles ActionEvents.
		 */
		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals("Ok"))
			{
				for (int i=0; i<proArr.length; i++)
				{
					proArr[i].setProjectName(textfieldArr[i].getText());
					model.setElementAt(proArr[i], selectedIndicesArr[i]);
				}
			}

			dispose();
		}
	}
	
	/**
	 * A window that allows the user to change the currently selected tab's name.
	 * @author Dominic Kramer
	 */
	private class ChangeTabNameGUI extends JFrame implements ActionListener
	{
		/** The field where the user enters the new name */
		private JTextField textField;
		
		/**
		 * Create a new ChangeTabNameGUI.
		 */
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
		
		/**
		 * Handles ActionEvents.
		 */
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
	
	/**
	 * Class which handles ActionEvents the same way as processSentEvent(ActionEvent) method would, 
	 * except in a separate thread.
	 * @author Dominic Kramer
	 */
	class ActionPerformedThread extends Thread
	{
		/** The ActionEvent to handle. */
		private ActionEvent e;
		/** The panel from which the user selects to perform an action. */
		private JPanel panel;
		/** Make an ActionPerformedThread object. */
		public ActionPerformedThread(ActionEvent ev, JPanel pan)
		{
			e = ev;
			panel = pan;
		}
		/** Define what the thread should do. */
		public void run()
		{
			panel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			processSentEvent(e);
			panel.setCursor(Cursor.getDefaultCursor());
		}
	}
}
/*
 * File:  HawkDesktop.java
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
 * Revision 1.4  2004/05/26 19:36:10  kramer
 * Added a popup listener.
 * Added inner classes:
 *   For creating the HawkDesktop's menu bar
 *   For changing a project's name
 *   For changing a tab's name
 *
 * Revision 1.3  2004/03/12 19:46:14  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:08:50  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import gov.anl.ipns.Util.File.RobustFileFilter;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.plaf.metal.MetalBorders;

import devTools.Hawk.classDescriptor.gui.HawkFileChooser;
import devTools.Hawk.classDescriptor.gui.internalFrame.AlphabeticalListGUI;
import devTools.Hawk.classDescriptor.gui.internalFrame.PackageTreeGUI;
import devTools.Hawk.classDescriptor.gui.panel.ProjectSelectorJPanel;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.FileAssociationManager;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * This is Hawk's main window from which the user does everything.
 * @author Dominic Kramer
 */
public class HawkDesktop extends JFrame implements ActionListener
{
	/**
	 * This is basically a modified JPanel which contains a JList of the currently opened Projects.  It 
	 * also has support for a popup menu and has a method to obain a JMenuBar.  The ProjectSelectorJPanel 
	 * handles ActionEvents from selecting an item from a meu or popup menu.
	 */
	protected ProjectSelectorJPanel proPanel;
	/**
	 * This is the JTabbedPane.  Each tab in the JTabbedPane 
	 * holds a JDesktop each of which hold JInternalFrames.
	 */
	protected JTabbedPane tabbedPane;
	/**
	 * The menu bar associated with this HawkDesktop.
	 */
	protected HawkDesktopMenuBar menuBar;
	/**
	 * This is set to true if this HawkDesktop is the first HawkDesktop opened.
	 */
	protected boolean firstWindowOpened;
	
	/**
	 * This is the popup menu for this HawkDesktop.
	 */
	protected JPopupMenu popup;
	
	/**
	 * Creates a HawkDesktop.
	 * @param vec The Vector of Projects to add to the ProjectSelectorJPanel in the HawkDesktop.
	 * @param firstOpened True if this is the first HawkDesktop window opened.
	 */
	public HawkDesktop(Vector vec, boolean firstOpened)
	{
		firstWindowOpened = firstOpened;
		
		setTitle("Hawk");
		Dimension dim = getToolkit().getScreenSize();
		setSize((int)(dim.getWidth()*0.8),(int)(dim.getHeight()*0.8));
		setLocation((int)(dim.getWidth()*0.1),(int)(dim.getHeight()*0.1));
		addWindowListener(new WindowDestroyer());
		
		JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			
		JPanel westSuperPanel = new JPanel();
			westSuperPanel.setLayout(new BorderLayout());
			JPanel westPanel = new JPanel();
				westPanel.setLayout(new BorderLayout());
				proPanel = new ProjectSelectorJPanel(vec,this);
					westPanel.add(new JLabel("Current Projects"), BorderLayout.NORTH);
					westPanel.add(proPanel, BorderLayout.CENTER);
						westSuperPanel.add(westPanel,BorderLayout.CENTER);
						westSuperPanel.add(new JLabel(" "), BorderLayout.EAST);
						westSuperPanel.add(new JLabel(" "), BorderLayout.WEST);
						westSuperPanel.add(new JLabel(" "), BorderLayout.SOUTH);

		JPanel topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout());
			//JPanel buttonPanel = new JPanel();
				//buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			JToolBar buttonPanel = new JToolBar();
//			buttonPanel.setRollover(true);
			buttonPanel.setFloatable(false);
				
				JButton newButton = getIconedJButton("new.png","New");
				newButton.setToolTipText("Create a new project");
				newButton.setActionCommand("project.new");
				newButton.addActionListener(this);
				buttonPanel.add(newButton);
				
				JButton openButton = getIconedJButton("stock-open.png","Open");
				openButton.setToolTipText("Open a previously saved project");
				openButton.setActionCommand("project.open");
				openButton.addActionListener(this);
				buttonPanel.add(openButton);
				
				JButton saveButton = getIconedJButton("filesave.png","Save");
				saveButton.setToolTipText("Save a project");
				saveButton.setActionCommand("project.save");
				saveButton.addActionListener(this);
				buttonPanel.add(saveButton);
				
				buttonPanel.addSeparator();
				
				JButton viewAlphaButton = getIconedJButton("view_alpha.png","View Alphabetically");
				viewAlphaButton.setToolTipText("View the project's classes and interfaces in alphabetical order");
				viewAlphaButton.setActionCommand("alphaWindow.popup");
				viewAlphaButton.addActionListener(this);
				buttonPanel.add(viewAlphaButton);
				
				JButton viewPackagesButton = getIconedJButton("view_packages.png","View By Package");
				viewPackagesButton.setToolTipText("View the project's classes and interfaces categorized by the package they're in");
				viewPackagesButton.setActionCommand("packageWindow.popup");
				viewPackagesButton.addActionListener(this);
				buttonPanel.add(viewPackagesButton);
				
				buttonPanel.addSeparator();
				
				JButton printButton = getIconedJButton("fileprint.png","Print");
				printButton.setToolTipText("Print class and interface information");
				printButton.setActionCommand("project.print");
				printButton.addActionListener(this);
				buttonPanel.add(printButton);
				
				JButton searchButton = getIconedJButton("stock-find.png","Search");
				searchButton.setToolTipText("Search for a class or interface");
				searchButton.setActionCommand("project.search");
				searchButton.addActionListener(this);
				buttonPanel.add(searchButton);
				
				topPanel.add(buttonPanel, BorderLayout.CENTER);
				topPanel.add(new JSeparator(), BorderLayout.SOUTH);				
						
		tabbedPane = new JTabbedPane();
			JDesktopPane dTop = new JDesktopPane();
			tabbedPane.add(dTop);
			tabbedPane.setTitleAt(0,"Tab 1");
		
		popup = proPanel.getWindowMenuAsPopupMenu();
		PopupListener popupListener = new PopupListener();
		tabbedPane.addMouseListener(popupListener);
		
		mainPanel.add(topPanel, BorderLayout.NORTH);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,westSuperPanel,tabbedPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);

		mainPanel.add(splitPane, BorderLayout.CENTER);
		
		getContentPane().add(mainPanel);
		menuBar = new HawkDesktopMenuBar(proPanel,this);
		setJMenuBar(menuBar);
	}
	
	/**
	 * This returns a JButton with the image with the filename "name" from the pixmap directory specified by
	 * SystemsManager.getPixmapDirectory().  If the image can not be found, a JButton with the text, "title" on 
	 * it is returned instead.  This method also gives the button a MetalBorders.ButtonBorder to make the 
	 * buttons appear in a more compact format.
	 * @param location The location to an image to place on the JButton
	 * @param title The text to put on the JButton if the image could not be found
	 * @return A JButton with an image or text on it
	 */
	public JButton getIconedJButton(String name, String title)
	{
		JButton button = null;
		ImageIcon icon = SystemsManager.getImageIconOrNull(name);
		if (icon != null)
			button = new JButton(icon);
		else
			button = new JButton(title);
		
		button.setBorder(new MetalBorders.ButtonBorder());
					
		return button;
	}
	
	/**
	 * Returns true if this is the first HawkDesktop window open and false otherwise.
	 */
	public boolean isFirstWindowOpen()
	{
		return firstWindowOpened;
	}
	
	/**
	 * Sets or unsets this window as the first window open.
	 * @param bol True if the window is to be set as the first window open and false otherwise.
	 */
	public void setFirstWindowOpen(boolean bol)
	{
		firstWindowOpened = bol;
	}
	
	/**
	 * Sets the title.
	 * @param title The title.
	 */
	public void setHawkDesktopTitle(String title)
	{
		setTitle(title);
	}
	
	/**
	 * Get the names of the tabs.
	 * @return An array of Strings each of which is the name of a tab.  The element at index 
	 * "n" is the tab at index "n".
	 */
	public String[] getTabNames()
	{
		int num = tabbedPane.getTabCount();
		String[] arr = new String[num];
		
		for (int i=0; i<num; i++)
			arr[i] = tabbedPane.getTitleAt(i);
		
		return arr;
	}
	
	/**
	 * This gets the JTabbedPane in the HawkDesktop.
	 * @return The JTabbedPane.
	 */
	public JTabbedPane getTabbedPane()
	{
		return tabbedPane;
	}
	
	/**
	 * This gets the JDesktop pane from the tabbed pane that is currently visible.
	 * @return The currently visible JDesktop pane.
	 */
	public JDesktopPane getSelectedDesktop()
	{
		int num = tabbedPane.getSelectedIndex();
		if (num != -1)
			return (JDesktopPane)tabbedPane.getComponentAt(num);
		else
			return null;
	}
	
	/**
	 * Sets the selected JDesktopPane.  This basically sets selected tab 
	 * from the JTabbedPane.
	 * @param index The index of the selected tab in the JTabbedPane.
	 */
	public void setSelectedDesktop(int index)
	{
		tabbedPane.setSelectedIndex(index);
	}
	
	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
		menuBar.actionPerformed(event);
	}
	
	/**
	 * Handles closing the window.  If the window is the first window opened and the user 
	 * selects to close the window, the program is shut down.  If the window is not the first 
	 * window opened, and the user selects to close the window, the window is just closed.
	 * @author Dominic Kramer
	 */
	class WindowDestroyer extends WindowAdapter
	{
		/**
		 * Handles closing the window.
		 */
		public void windowClosing(WindowEvent event)
		{
			dispose();
			if (firstWindowOpened)
				System.exit(0);
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
	
	public class HawkDesktopMenuBar extends JMenuBar implements ActionListener
	{
		protected ProjectSelectorJPanel psjp;
		protected HawkDesktop desktop;
		
		/**
		 * Create the JMenuBar that will be attached to a HawkDesktop gui.
		 * @return The JMenuBar
		 */
		public HawkDesktopMenuBar(ProjectSelectorJPanel panel, HawkDesktop desk)
		{
			psjp = panel;
			desktop = desk;
				 JMenu projectFileMenu = new JMenu("File");
					 JMenuItem newProjectItem = new JMenuItem("New Project", SystemsManager.getImageIconOrBlankIcon("new.png"));
					 newProjectItem.addActionListener(this);
					 newProjectItem.setActionCommand("project.new");
					 projectFileMenu.add(newProjectItem);
		
					 JMenuItem projectOpenItem = new JMenuItem("Open", SystemsManager.getImageIconOrBlankIcon("stock-open.png"));
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
					
					 JMenuItem projectSaveItem = new JMenuItem("Save", SystemsManager.getImageIconOrBlankIcon("filesave.png"));
					 projectSaveItem.addActionListener(this);
					 projectSaveItem.setActionCommand("project.save");
					 projectFileMenu.add(projectSaveItem);
		
					 JMenuItem projectSaveAsItem = new JMenuItem("Save As", SystemsManager.getImageIconOrBlankIcon("saveas.png"));
					 projectSaveAsItem.addActionListener(this);
					 projectSaveAsItem.setActionCommand("project.saveAs");
					 projectFileMenu.add(projectSaveAsItem);
				 
					 projectFileMenu.add(new JSeparator());
				 
					 JMenuItem projectPrintItem = new JMenuItem("Print Project", SystemsManager.getImageIconOrBlankIcon("fileprint.png"));
					 projectPrintItem.addActionListener(this);
					 projectPrintItem.setActionCommand("project.print");
					 projectFileMenu.add(projectPrintItem);
			     
					 projectFileMenu.add(new JSeparator());
				
					 JMenuItem exitItem = new JMenuItem("Exit", SystemsManager.getImageIconOrBlankIcon("exit.png"));
						exitItem.addActionListener(this);
						exitItem.setActionCommand("exit");
						projectFileMenu.add(exitItem);					 				     
			
				JMenu projectEditMenu = new JMenu("Edit");
					JMenuItem associateSourceItem = new JMenuItem("Associate Source Files", SystemsManager.getImageIconOrBlankIcon("source.png"));
					associateSourceItem.addActionListener(this);
					associateSourceItem.setActionCommand("project.associate.source");
					projectEditMenu.add(associateSourceItem);
				
					JMenuItem associateJavadocsItem = new JMenuItem("Associate Javadoc Files", SystemsManager.getImageIconOrBlankIcon("javadocs.png"));
					associateJavadocsItem.addActionListener(this);
					associateJavadocsItem.setActionCommand("project.associate.javadocs");
					projectEditMenu.add(associateJavadocsItem);
				
					projectEditMenu.add(new JSeparator());
				
					JMenuItem addIntItem = new JMenuItem("Add Interface", SystemsManager.getImageIconOrBlankIcon("add.png"));
					addIntItem.addActionListener(this);
					addIntItem.setActionCommand("project.addInterface");
					projectEditMenu.add(addIntItem);
				
					/*
					JMenuItem remIntItem = new JMenuItem("Remove Interface", SystemsManager.getImageIconOrBlankIcon("subtract.png"));
					remIntItem.addActionListener(this);
					remIntItem.setActionCommand("project.removeInterface");
					projectEditMenu.add(remIntItem);
					*/

					projectEditMenu.add(new JSeparator());
				
					JMenuItem changeProjectNameItem = new JMenuItem("Change Project Name", SystemsManager.getImageIconOrBlankIcon("edit.png"));
					changeProjectNameItem.addActionListener(this);
					changeProjectNameItem.setActionCommand("project.edit.projectName");
					projectEditMenu.add(changeProjectNameItem);

					projectEditMenu.add(new JSeparator());

					JMenuItem removeProjectItem = new JMenuItem("Remove From List", SystemsManager.getImageIconOrBlankIcon("remove_from_list.png"));
					removeProjectItem.addActionListener(this);
					removeProjectItem.setActionCommand("project.remove");
					projectEditMenu.add(removeProjectItem);
		
				JMenu projectViewMenu = new JMenu("View");
					JMenu interfacesMenu = new JMenu("Interfaces");				
						JMenuItem alphaItem = new JMenuItem("Alphabetically",SystemsManager.getImageIconOrBlankIcon("view_alpha.png"));
						alphaItem.addActionListener(this);
						alphaItem.setActionCommand("alphaWindow.popup");
						interfacesMenu.add(alphaItem);
					
						JMenuItem inherItem = new JMenuItem("Organized in Packages",SystemsManager.getImageIconOrBlankIcon("view_packages.png"));
						inherItem.addActionListener(this);
						inherItem.setActionCommand("packageWindow.popup");
						interfacesMenu.add(inherItem);
				
					JMenuItem statisticsMenuItem = new JMenuItem("Statistics", SystemsManager.getImageIconOrBlankIcon("stats.png"));
					statisticsMenuItem.addActionListener(this);
					statisticsMenuItem.setActionCommand("project.statistics");

					JMenuItem searchItem = new JMenuItem("Search", SystemsManager.getImageIconOrBlankIcon("stock-find.png"));
					searchItem.addActionListener(this);
					searchItem.setActionCommand("project.search");
				
					projectViewMenu.add(interfacesMenu);
					projectViewMenu.add(statisticsMenuItem);
					projectViewMenu.add(new JSeparator());
					projectViewMenu.add(searchItem);
			
				JMenu windowMenu = new JMenu("Window");
					windowMenu.setActionCommand("windowMenuSelected");
			
					JMenuItem renameItem = new JMenuItem("Rename Tab", SystemsManager.getImageIconOrBlankIcon("tab_rename.png"));
					renameItem.addActionListener(this);
					renameItem.setActionCommand("rename.tab");
					windowMenu.add(renameItem);
				
					JMenuItem removeItem = new JMenuItem("Remove Tab", SystemsManager.getImageIconOrBlankIcon("tab_remove.png"));
					removeItem.addActionListener(this);
					removeItem.setActionCommand("remove.tab");
					windowMenu.add(removeItem);					
					
					windowMenu.add(new JSeparator());
										
					JMenuItem openNewWindow = new JMenuItem("New Window", SystemsManager.getImageIconOrBlankIcon("window_new.png"));
						openNewWindow.addActionListener(this);
						openNewWindow.setActionCommand("open.newWindow");
						windowMenu.add(openNewWindow);
				
					JMenuItem openNewTab = new JMenuItem("New Tab",SystemsManager.getImageIconOrBlankIcon("new_tab.png"));
						openNewTab.addActionListener(this);
						openNewTab.setActionCommand("open.newTab");
						windowMenu.add(openNewTab);
				
				JMenu helpMenu = new JMenu("Help");
					JMenuItem aboutItem = new JMenuItem("About",SystemsManager.getImageIconOrBlankIcon("about.png"));
					aboutItem.addActionListener(this);
					aboutItem.setActionCommand("about");
					helpMenu.add(aboutItem);
			
			this.add(projectFileMenu);
			this.add(projectEditMenu);
			this.add(projectViewMenu);
			this.add(windowMenu);
			this.add(helpMenu);
		}
				
		/**
		* Handles the action events that happen in the GUI such as button clicks and
		* menu selections etc.
		* @param event The ActionEvent that occured
		*/
		public void actionPerformed( ActionEvent event)
		{
			ActionPerformedThread thread = new ActionPerformedThread(event,proPanel);
			thread.start();
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
				CreateNewProjectGUI cnpg = new CreateNewProjectGUI(psjp, new UnableToLoadClassGUI());
				cnpg.setVisible(true);
			}
			else if (event.getActionCommand().equals("project.edit.projectName"))
			{
				if (psjp.areProjectListed())
				{
					Project[] proArr = psjp.getSelectedProjects();
				
					if (proArr.length == 0)
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu to edit its name"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						ChangeProjectNameGUI gui = new ChangeProjectNameGUI(psjp);
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
				if (psjp.areProjectListed())
				{
					Project[] proArr = psjp.getSelectedProjects();
				
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
			
				HawkFileChooser chooser = new HawkFileChooser();
					RobustFileFilter jdfFilter = new RobustFileFilter();
					jdfFilter.addExtension(SystemsManager.getHawkFileExtensionWithoutPeriod());
					jdfFilter.setDescription(SystemsManager.getHawkFileExtension()+" (Hawk Java Project Files)");
					chooser.setFileFilter(jdfFilter);
					chooser.setMultiSelectionEnabled(true);
				int returnVal = chooser.showOpenDialog(frame);
			
				mainPanel.add(chooser, BorderLayout.CENTER);
						
				framePane.add(mainPanel);
				framePane.setVisible(true);
			
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					File[] fileNameArr = chooser.getSelectedFiles();
						//this will remove the first element which states "No projects listed"
						//if this is the first time the user has opened a new project
						if (psjp.getNoProjectsListed())
						{
							psjp.getModel().remove(0);
						}
						
						//now to add the projects to the list
						//this constructor makes a Project object with a new Interface object in interfaceVec
						for (int i=0; i<fileNameArr.length; i++)
						{
							Project newProject = new Project(fileNameArr[i].getAbsolutePath(), true);					
							psjp.getModel().addElement(newProject);
							psjp.setNoProjectsListed(false);						}
				}
			}
			else if (event.getActionCommand().equals("project.save"))
			{
				if (!psjp.getNoProjectsListed())
				{	
					Project[] proArr = psjp.getSelectedProjects();
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
				if (!psjp.getNoProjectsListed())
				{
					Project[] proArr = psjp.getSelectedProjects();
				
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
				int[] selectedIndex = psjp.getList().getSelectedIndices();
			
				for (int i=selectedIndex.length-1; i>=0; i--)
				{
					if (selectedIndex[i] >= 0)
					{	
						psjp.getModel().remove(selectedIndex[i]);
					}
				}
				
				if (psjp.getModel().isEmpty())
				{
					Project tempProject = new Project();
					tempProject.setProjectName("No projects listed");
					psjp.getModel().addElement(tempProject);
					psjp.setNoProjectsListed(true);
				}
			
			}
			else if (event.getActionCommand().equals("alphaWindow.popup"))
			{
				if (!psjp.getNoProjectsListed())
				{
					Project[] proArr = psjp.getSelectedProjects();
				
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
				for (int i=0; i<psjp.getModel().size(); i++)
					proVec.add((Project)psjp.getModel().elementAt(i));
				
				PrintGUI pg = new PrintGUI(proVec, "Current Projects");
				pg.setVisible(true);
			}
			else if (event.getActionCommand().equals("packageWindow.popup"))
			{
				if (!psjp.getNoProjectsListed())
				{
					Project[] proArr = psjp.getSelectedProjects();
				
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
				if (!psjp.getNoProjectsListed())
				{
					Project[] proArr = psjp.getSelectedProjects();
				
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
				if (!psjp.getNoProjectsListed())
				{
					Project[] proArr = psjp.getSelectedProjects();
				
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
				if (!psjp.getNoProjectsListed())
				{
					Project[] proArr = psjp.getSelectedProjects();
				
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
				if (!psjp.getNoProjectsListed())
				{
					Project[] proArr = psjp.getSelectedProjects();
				
					if (proArr.length == 0)
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,"Open a project from the file menu\nand select it to add classes and interfaces to it"
							,"Note"
							,JOptionPane.INFORMATION_MESSAGE);
					}
					
					for (int i=0; i<proArr.length; i++)
					{
						AddInterfaceGUI gui = new AddInterfaceGUI(psjp, new UnableToLoadClassGUI(), proArr[i]);
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
				if (!psjp.getNoProjectsListed())
				{
					Project[] proArr = psjp.getSelectedProjects();
				
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
				for (int i=0; i<psjp.getModel().size(); i++)
					proVec.add((Project)psjp.getModel().elementAt(i));
				
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
					ChangeTabNameGUI gui = new ChangeTabNameGUI(desktop);
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
		
			HawkFileChooser chooser = new HawkFileChooser();
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
	
	/**
	 * A window that allows the user to change the currently selected project's name.
	 * @author Dominic Kramer
	 */
	protected class ChangeProjectNameGUI extends JFrame implements ActionListener
	{
		/** The field on which the user enters the new name. */
		private JTextField[] textfieldArr;
		private Project[] proArr;
		private int[] selectedIndicesArr;
		private ProjectSelectorJPanel psjp;
		
		/**
		 * Create a new ChangeProjectNameGUI.
		 */
		public ChangeProjectNameGUI(ProjectSelectorJPanel panel)
		{
			psjp = panel;
			
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
			
			selectedIndicesArr = psjp.getList().getSelectedIndices();	
			proArr = psjp.getSelectedProjects();
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
					psjp.getModel().setElementAt(proArr[i], selectedIndicesArr[i]);
				}
			}

			dispose();
		}
	}
	
	/**
	 * A window that allows the user to change the currently selected tab's name.
	 * @author Dominic Kramer
	 */
	protected class ChangeTabNameGUI extends JFrame implements ActionListener
	{
		/** The field where the user enters the new name */
		private JTextField textField;
		private HawkDesktop desktop;
		
		/**
		 * Create a new ChangeTabNameGUI.
		 */
		public ChangeTabNameGUI(HawkDesktop desk)
		{
			desktop = desk;
			
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
					int index = desktop.getTabbedPane().getSelectedIndex();
					if (index >= 0)
						textField.setText(desktop.getTabbedPane().getTitleAt(index));
					textField.selectAll();
				JLabel label = new JLabel("Name=");
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
}

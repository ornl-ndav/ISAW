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
 * Revision 1.6  2004/05/26 20:17:27  kramer
 * Removed the following methods:
 *   private void promptForFileToSaveProject(Project pro)
 *   public void processSentEvent(ActionEvent event)
 *   private void createNewTab()
 * Removed the following classes:
 *   ChangeProjectNameGUI
 *   ChangeTabNameGUI
 *   ActionPerformedThread
 * Added the method
 *   public Component[] determineWaitingComponents()
 *
 * Revision 1.5  2004/03/15 20:30:15  dennis
 * Changed to use RobustFileFilter from new package,
 * gov.anl.ipns.Util.File
 *
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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import devTools.Hawk.classDescriptor.gui.MouseNotifiable;
import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * This is a special type of JPanel that contains a list of all of the currently opened projects.  This class 
 * handles any events that are thrown when the user selects a project from the list.
 * @author Dominic Kramer
 */
public class ProjectSelectorJPanel extends JPanel implements ActionListener, ListSelectionListener, MouseNotifiable
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
	/** The popup window */
	protected JPopupMenu popup;
	/** the HawkDesktop that this panel is on. */
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
		desktop = desk;
		noProjectsListed = true;
		
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
			
			JMenuItem alphaWindow = new JMenuItem("Alphabetically", SystemsManager.getImageIconOrBlankIcon("view_alpha.png"));
			alphaWindow.addActionListener(this);
			alphaWindow.setActionCommand("alphaWindow.popup");
			popup.add(alphaWindow);
			
			JMenuItem extendsWindow = new JMenuItem("Organized in Packages", SystemsManager.getImageIconOrBlankIcon("view_packages.png"));
			extendsWindow.addActionListener(this);
			extendsWindow.setActionCommand("packageWindow.popup");
			popup.add(extendsWindow);
			
		//now to add listeners to the components that con bring up the popup menu
		MouseListener popupListener = new PopupListener(popup);
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
	 * Determines whether or not there are any projects listed.
	 * @return True if there are projects listed and false otherwise.
	 */
	public boolean areProjectListed()
	{
		return !noProjectsListed;
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
	 * Get the window menu used to create new tabs, remove tabs, open new windows, etc. in a HawkDesktop as a popup menu.
	 * @return The window menu from the JMenuBar obtained from getProjectJMenuBar().
	 */
	public JPopupMenu getWindowMenuAsPopupMenu()
	{
		JPopupMenu windowMenu = new JPopupMenu("Window");
			windowMenu.add(new JLabel("Window"));
			windowMenu.add(new JSeparator());			
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
						
			JMenuItem openNewTab = new JMenuItem("New Tab", SystemsManager.getImageIconOrBlankIcon("new_tab.png"));
				openNewTab.addActionListener(this);
				openNewTab.setActionCommand("open.newTab");
				windowMenu.add(openNewTab);
			
			return windowMenu;
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
	 * Returns true if no projects are listed and false if there are projects listed.
	 */
	public boolean getNoProjectsListed()
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
	 * Handles action events.
	 */
	public void actionPerformed(ActionEvent event)
	{
		desktop.actionPerformed(event);
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
	protected static class PopupListener extends MouseAdapter 
	{
		protected JPopupMenu popup;
		
		public PopupListener(JPopupMenu pop)
		{
			popup = pop;
		}
		
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
	 * The Components in the array returned from this method are the Components that should have the 
	 * mouse use the waiting animation when an operation is in progress.
	 */
	public Component[] determineWaitingComponents()
	{
		Component[] compArr = new Component[4];
		compArr[0] = list;
		compArr[1] = popup;
		compArr[2] = desktop;
		compArr[3] = this;
		return compArr;
	}
}

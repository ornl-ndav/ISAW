/*
 * File:  InterfaceSelectorJPanel.java
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
 * Revision 1.3  2004/03/12 19:46:17  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:09:42  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.panel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import DataSetTools.util.RobustFileFilter;
import devTools.Hawk.classDescriptor.gui.frame.InterfaceSelectorSaveAsGUI;
import devTools.Hawk.classDescriptor.gui.internalFrame.JavadocsGUI;
import devTools.Hawk.classDescriptor.gui.internalFrame.ShortenedSourceGUI;
import devTools.Hawk.classDescriptor.gui.internalFrame.SingleUMLGUI;
import devTools.Hawk.classDescriptor.gui.internalFrame.SourceCodeGUI;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.SystemsManager;
import devTools.Hawk.classDescriptor.tools.dataFileUtilities;

/**
 * This is a special type of panel that contains a JList.  The panel contains information about a specific project and contains 
 * object to view information about this project.  The list contains all of the projects interfaces.  The panel can be used to 
 * create a project.  Currently, this class is not really needed any more, may be removed, and is still under construction.
 * @author Dominic Kramer
 */
public class InterfaceSelectorJPanel extends ProjectSelectorJPanel implements ActionListener, ListSelectionListener
{
	//This JPanel is a lot like ProjectSelectorJPanel, except
	//1.  projectVec is really just a Vector of one Project object.  This Project object has no name
	//2.  The contents of list are Strings which represent the elements in Project.interfaceVec 
	
	//INTFVEC is a Vector of Interface objects
	protected Vector ProjectVec;
	
	public InterfaceSelectorJPanel(Vector INTFVEC)
	{
		//now to instantiate the Vector of Project objects
			ProjectVec = new Vector();
			ProjectVec.setSize(1);
		
		//now to instantiate the Continer and main panel everything is added on
			setLayout(new BorderLayout());
				
		//now to make the list for the gui		
			//this model allows you to modify the list
			model = new DefaultListModel();
			//the following will fill in the list with some blank data			
			
			ProjectVec.setElementAt(new Project(), 0);
			model.addElement("Current Classes");
			
			if (INTFVEC != null)
			{
				for (int i=0; i<INTFVEC.size(); i++)
				{
					((Project)ProjectVec.elementAt(0)).getInterfaceVec().add((Interface)INTFVEC.elementAt(i));
					model.addElement( "    " + ((Interface)INTFVEC.elementAt(i)).getPgmDefn().getInterface_name() );
				}
			}

			list = new JList(model);
			list.addListSelectionListener(this);
			//the following only allows one item to be selected at a time
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
				
		//now to make the JScrolPane to put the JList on		
			JScrollPane listPane = new JScrollPane(list);
			
			add(listPane, BorderLayout.CENTER);
					
		//now to make the popup menu
			popup = new JPopupMenu();
			//now to add components to the popup menu
			popup.add(new JLabel("View"));
			popup.add(new JSeparator());
						
			JMenuItem singleUMLItem = new JMenuItem("Single UML");
			singleUMLItem.addActionListener(this);
			singleUMLItem.setActionCommand("popup.singleUML");
			popup.add(singleUMLItem);
			
			JMenuItem shortenedSourceItem = new JMenuItem("Shortened Source Code");
			shortenedSourceItem.addActionListener(this);
			shortenedSourceItem.setActionCommand("popup.shortenedSource");
			popup.add(shortenedSourceItem);
			
			JMenuItem sourceItem = new JMenuItem("Source Code");
			sourceItem.addActionListener(this);
			sourceItem.setActionCommand("popup.sourceCode");
			popup.add(sourceItem);
			
			JMenuItem javadocsItem = new JMenuItem("Javadocs");
			javadocsItem.addActionListener(this);
			javadocsItem.setActionCommand("popup.javadocs");
			popup.add(javadocsItem);
		
			JMenuItem horizontalItem = new JMenuItem("Horizontally");
			horizontalItem.addActionListener(this);
			horizontalItem.setActionCommand("popup.horizontal");
			popup.add(horizontalItem);
			
			JMenuItem verticalItem = new JMenuItem("Vertically");
			verticalItem.addActionListener(this);
			verticalItem.setActionCommand("popup.vertical");
			popup.add(verticalItem);
			
		//now to add listeners to the components that con bring up the popup menu
		MouseListener popupListener = new PopupListener();
		list.addMouseListener(popupListener);
	}
	
	public JPanel getInterfaceSelectorJPanel()
	{
		return this;
	}
	
	public JMenuBar getInterfaceJMenuBar()
	{
		JMenuBar intMenuBar = new JMenuBar();
			JMenu fileMenu = new JMenu("File");
				JMenuItem saveItem = new JMenuItem("Save");
				saveItem.addActionListener(this);
				fileMenu.add(saveItem);
				
				JMenuItem saveAsItem = new JMenuItem("Save As");
				saveAsItem.addActionListener(this);
				fileMenu.add(saveAsItem);
				
				fileMenu.add(new JSeparator());
				
				JMenuItem printItem = new JMenuItem("Print");
				printItem.addActionListener(this);
				fileMenu.add(printItem);
				
			JMenu editMenu = new JMenu("Edit");
				JMenu importSubMenu = new JMenu("Import");
					JMenuItem importInterfaceItem = new JMenuItem("Interface");
					importInterfaceItem.addActionListener(this);
					importSubMenu.add(importInterfaceItem);
					
					JMenuItem importProjectItem = new JMenuItem("Java Project");
					importProjectItem.addActionListener(this);
					importSubMenu.add(importProjectItem);
				editMenu.add(importSubMenu);
				
				JMenu removeSubMenu = new JMenu("Remove");
					JMenuItem removeInterfaceItem = new JMenuItem("Interface");
					removeInterfaceItem.addActionListener(this);
					removeInterfaceItem.setActionCommand("remove.Interface");
					removeSubMenu.add(removeInterfaceItem);
					
					JMenuItem removeAllItem = new JMenuItem("All");
					removeAllItem.addActionListener(this);
					removeSubMenu.add(removeAllItem);
				editMenu.add(removeSubMenu);
				
				JMenuItem alphaItem = new JMenuItem("Alphabatize List");
				alphaItem.addActionListener(this);
				editMenu.add(alphaItem);
				
				JMenuItem searchItem = new JMenuItem("Search");
				searchItem.addActionListener(this);
				editMenu.add(searchItem);
			
			JMenu viewMenu = new JMenu("View");
				JMenu allSubMenu = new JMenu("All");
					JMenuItem allAlphaItem = new JMenuItem("Alphabetically");
					allAlphaItem.addActionListener(this);
					allSubMenu.add(allAlphaItem);
					
					JMenuItem allByInHItem = new JMenuItem("By Inheritance");
					allByInHItem.addActionListener(this);
					allSubMenu.add(allByInHItem);
				viewMenu.add(allSubMenu);
				
				JMenuItem singleUMLItem = new JMenuItem("Single UML");
				singleUMLItem.addActionListener(this);
				viewMenu.add(singleUMLItem);
				
				JMenuItem shortenedSourceItem = new JMenuItem("Shortened Source");
				shortenedSourceItem.addActionListener(this);
				viewMenu.add(shortenedSourceItem);
				
				JMenuItem javadocsItem = new JMenuItem("Javadocs");
				javadocsItem.addActionListener(this);
				viewMenu.add(javadocsItem);
				
				JMenuItem sourceItem = new JMenuItem("Source Code");
				sourceItem.addActionListener(this);
				viewMenu.add(sourceItem);
				
				JMenuItem horizontalItem = new JMenuItem("Horizontally");
				horizontalItem.addActionListener(this);
				viewMenu.add(horizontalItem);
				
				JMenuItem verticalItem = new JMenuItem("Vertically");
				verticalItem.addActionListener(this);
				viewMenu.add(verticalItem);
			
			intMenuBar.add(fileMenu);
			intMenuBar.add(editMenu);
			intMenuBar.add(viewMenu);
											
		return intMenuBar;
	}
	
	/**
	* This returns the currently selected Project from the Jlist in the GUI
	* @return The Project object currently selected
	*/
		public Interface getSelectedInterface() throws NotAnInterfaceException
		{
			int selectedIndex = list.getSelectedIndex();
			Interface intF;
			
			if (selectedIndex == 0)
			{
				throw new NotAnInterfaceException();
			}
			else
			{
				if (selectedIndex < 0)
					selectedIndex = 0;
				selectedIndex = selectedIndex - 1;
				intF = (Interface)( ((Project)(ProjectVec.elementAt(0))).getInterfaceVec().elementAt(selectedIndex) );
			}
				
			return intF;
		}
		
	/**
	* This returns the currently selected Project from the Jlist in the GUI.  This method is overriden from ProjectSelectorJPanel
	* to always return the same Project because when the user uses the InterfaceSelectorJPanel he/she is really only working with
	* one project.
	* @return The Project object currently selected
	*/
		public Project getSelectedProject()
		{
				return (Project)(ProjectVec.elementAt(0));
		}
	
	/**
	* Handles the action events that happen in the GUI such as button clicks and
	* menu selections etc.
	* @param event The ActionEvent that occured
	*/
	public void actionPerformed( ActionEvent event)
	{
		if (event.getActionCommand().equals("Interface"))
		{
/*
					Vector fileVec = new Vector();
					FileReflector.getVectorOfInterfacesGUI(fileVec);
					Vector vec = new Vector();
					LoadClassThread thread = new LoadClassThread(fileVec,vec,new FileReflector());
					thread.start();
					
					for (int i = 0; i < vec.size(); i++)
					{
						if (vec.elementAt(i) != null)
						{
							String name = ((Interface)vec.elementAt(i)).getPgmDefn().getInterface_name();
							StringTokenizer tokenizer = new StringTokenizer(name,".");
							String lastToken = "";
							
							while (tokenizer.hasMoreTokens())
							{
								lastToken = tokenizer.nextToken();
							}
							
							model.addElement( "    "+lastToken);
							((Project)ProjectVec.elementAt(0)).getInterfaceVec().add(((Interface)vec.elementAt(i)));
						}
					}
*/
			}
			else if (event.getActionCommand().equals("Java Project"))
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
				int returnVal = chooser.showDialog(frame, "Select");
			
				mainPanel.add(chooser, BorderLayout.CENTER);
						
				framePane.add(mainPanel);
				framePane.setVisible(true);
			
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					String fileName = chooser.getSelectedFile().getAbsoluteFile().toString();
					String abstractFileName = chooser.getSelectedFile().getName();
				
					String project_Name = "";
						dataFileUtilities data = new dataFileUtilities(fileName, true);
						if (data != null)
						{
							//this only works if there are no "." in the file's name except at the end with .jdf
							StringTokenizer tokenizer = new StringTokenizer(abstractFileName,".");
							
							//this gets the projects name
							project_Name = tokenizer.nextToken();
											
							//now to add the project to the list
							//this constructor makes a Project object with a new Interface object in interfaceVec
							Project newProject = new Project(data, project_Name);
											
							for (int i=0; i<newProject.getInterfaceVec().size(); i++)
							{
								((Project)ProjectVec.elementAt(0)).getInterfaceVec().add( (Interface)(newProject.getInterfaceVec().elementAt(i)) );
								model.addElement( "    "+((Interface)(newProject.getInterfaceVec().elementAt(i))).toString() );
							}
						}
				}				
			}
			else if (event.getActionCommand().equals("remove.Interface"))
			{
				int selectedIndex = list.getSelectedIndex();
				if (selectedIndex > 0)
				{
					((Project)ProjectVec.elementAt(0)).getInterfaceVec().remove(selectedIndex-1);
					model.remove(selectedIndex);
				}
			}
			else if (event.getActionCommand().equals("All"))
			{
				System.out.println("Removing All");
				int vecSize = ((Project)ProjectVec.elementAt(0)).getInterfaceVec().size();
				for (int i=1; i<=vecSize; i++)
				{
					((Project)ProjectVec.elementAt(0)).getInterfaceVec().remove(vecSize-i);
				}
				int modelSize = model.size();
				for (int i=1; i<modelSize; i++)
				{
					model.remove(modelSize-i);
				}
			}
			else if (event.getActionCommand().equals("popup.singleUML"))
			{
				SingleUMLGUI singleUML = null;
				try
				{
					singleUML = new SingleUMLGUI(getSelectedInterface(), getSelectedInterface().getPgmDefn().getInterface_name(), false, false,desktop);
				}
				catch(NotAnInterfaceException e)
				{
					System.out.println("A NotAnInterfaceException was thrown in actionPerformed(ActionEvent) in InterfaceSelectorJPanel.java");
				}
				
				if (singleUML != null)
					singleUML.setVisible(true);
			}
			else if (event.getActionCommand().equals("popup.shortenedSource"))
			{
					try
					{
						ShortenedSourceGUI popupSsg = new ShortenedSourceGUI(getSelectedInterface(), getSelectedInterface().getPgmDefn().getInterface_name(), false, false,desktop);
						popupSsg.setVisible(true);
						popupSsg.fillInTextArea(getSelectedInterface(), false, false);
					}
					catch(NotAnInterfaceException e)
					{
					}
			}
			else if (event.getActionCommand().equals("popup.sourceCode"))
			{
				try
				{
					SourceCodeGUI popupSource = new SourceCodeGUI(getSelectedInterface(), ((Project)ProjectVec.elementAt(0)).getProjectName(),desktop);
					popupSource.setVisible(true);
				}
				catch(NotAnInterfaceException e)
				{
						System.out.println("A NotAnInterfaceException was thrown in InterfaceSelectorJPanel.java where event.getActionCommand() = "+event.getActionCommand());
				}
			}
			else if (event.getActionCommand().equals("popup.javadocs"))
			{
				try
				{	
					JavadocsGUI javagui = new JavadocsGUI(getSelectedInterface(), ((Project)ProjectVec.elementAt(0)).getProjectName(),desktop);
					javagui.setVisible(true);
				}
				catch(NotAnInterfaceException e)
				{
					System.out.println("A NotAnInterfaceException was thrown in InterfaceSelectorJPanel.java where event.getActionCommand() = "+event.getActionCommand());
				}
			}
			else if (event.getActionCommand().equals("Save"))
			{
			}
			else if (event.getActionCommand().equals("Save As"))
			{
				InterfaceSelectorSaveAsGUI intSelSavGUI = new InterfaceSelectorSaveAsGUI(getSelectedProject());
				intSelSavGUI.setVisible(true);				
			}
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

	public class NotAnInterfaceException extends Throwable
	{
	
	}
	
}

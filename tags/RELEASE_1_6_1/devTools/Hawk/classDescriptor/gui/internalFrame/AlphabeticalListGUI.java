/*
 * File:  AlphabeticalListGUI.java
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
 * Revision 1.1  2004/02/07 05:09:14  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.internalFrame;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
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
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import devTools.Hawk.classDescriptor.gui.frame.FileAssociationGUI;
import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.gui.frame.HorizontalInterfacePanelGUI;
import devTools.Hawk.classDescriptor.gui.frame.VerticalInterfacePanelGUI;
import devTools.Hawk.classDescriptor.gui.panel.ProjectSelectorJPanel;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.FileAssociationManager;
import devTools.Hawk.classDescriptor.tools.InterfaceUtilities;

public class AlphabeticalListGUI extends DesktopInternalFrame implements ActionListener, ListSelectionListener
{
	//the following are declared here to allow actionPerformed() to find them
	//they are components placed on the GUI
		/** The Jlist that contains all of the interfacees and/or classes
		* in a project in alphabetical order
		*/
		protected JList alphaList;
		
		/**
		* The mode that describes list
		*/
		protected DefaultListModel alphaModel;
		/**
		* The popup menu that appears when the user right clicks on the JList
		*/
		protected JPopupMenu popup;
		
	//the following are attributes that describe the class
		/**
		* This contains a reference to the ProjectGUI which had an event performed
		* on it to cause this GUI to be opened
		*/
		protected Project project;
		/**
		* This contains a reference to the ProjectGUI from which this GUI was
		* created from.  AlphabeticalListGUI needs this reference because when
		* the AlphabeticalListGUI is closed, it needs the reference to set 
		* openNewAlphaWindowOnSelect to true so that a new AlphabeticalListGUI 
		* is open if the user selects a new Project in ProjectGUI.
		*/
		protected ProjectSelectorJPanel proGUI;
		protected HawkDesktop desktop;
		
		protected JCheckBox shortenJavaBox;
		protected JCheckBox shortenOtherBox;

	public AlphabeticalListGUI(Project PRO, ProjectSelectorJPanel PROGUI, String title, boolean shortJava, boolean shortOther, HawkDesktop desk)
	{
		super(desk);
		
		//Now to define the Project that the information is obtained from
		//to fill in data in the GUI
			project = PRO;
			proGUI = PROGUI;
			desktop = desk;
		
		//Now to set some characteristics about the main window
			setTitle(title);
			setLocation(0,0);
			setSize(200,200);
			setClosable(true);
			setIconifiable(true);
			setMaximizable(true);
			setResizable(true);
			
			Container pane = getContentPane();
			
		//Now to make the JPanels
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			
		//Now to make the JList
			//now to make the list for the gui		
			//this model allows you to modify the list
			alphaModel = new DefaultListModel();
			alphaList = new JList(alphaModel);
			alphaList.addListSelectionListener(this);
			//the following only allows one item to be selected at a time
			alphaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		
		//now to fill the list
			fillList(shortJava, shortOther);
				
		//now to make the JScrolPane to put the JList on		
			JScrollPane listPane = new JScrollPane(alphaList);
			
		//now to add the components onto each other
			mainPanel.add(listPane, BorderLayout.CENTER);
			pane.add(mainPanel);
			
		//now to make the popup menu
			popup = new JPopupMenu();
			//now to add components to the popup menu
			popup.add(new JLabel("View"));
			popup.add(new JSeparator());

			JMenuItem singleUMLPopupItem = new JMenuItem("Single UML");
			singleUMLPopupItem.addActionListener(this);
			singleUMLPopupItem.setActionCommand("popup.singleUML");
			popup.add(singleUMLPopupItem);

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
			//popup.add(horizontalItem);
			
			JMenuItem verticalItem = new JMenuItem("Vertically");
			verticalItem.addActionListener(this);
			verticalItem.setActionCommand("popup.vertical");
			//popup.add(verticalItem);
		
		//now to make the JMenuBar (this is basically just menubar version of the popup menu)
			JMenuBar alphaMenuBar = new JMenuBar();
				JMenu fileMenu = new JMenu("File");
					JMenuItem saveButton = new JMenuItem("Save");
					saveButton.addActionListener(this);
					fileMenu.add(saveButton);
			
					JMenuItem closeButton = new JMenuItem("Close");
					closeButton.addActionListener(this);
					fileMenu.add(closeButton);
				
				JMenu editMenu = new JMenu("Edit");
					JMenuItem associateSourceCodeItem = new JMenuItem("Associate Source Code");
					associateSourceCodeItem.addActionListener(this);
					associateSourceCodeItem.setActionCommand("associate.source.code");
					editMenu.add(associateSourceCodeItem);

					JMenuItem associateJavadocsItem = new JMenuItem("Associate Javadocs");
					associateJavadocsItem.addActionListener(this);
					associateJavadocsItem.setActionCommand("associate.javadocs");
					editMenu.add(associateJavadocsItem);
				
				JMenu viewMenu = new JMenu("View");
					JMenuItem singleUMLItem = new JMenuItem("Single UML");
					singleUMLItem.addActionListener(this);
					singleUMLItem.setActionCommand("popup.singleUML");
					viewMenu.add(singleUMLItem);					
					
					JMenuItem shortenedSourceItem2 = new JMenuItem("Shortened Source Code");
					shortenedSourceItem2.addActionListener(this);
					shortenedSourceItem2.setActionCommand("popup.shortenedSource");
					viewMenu.add(shortenedSourceItem2);
			
					JMenuItem sourceItem2 = new JMenuItem("Source Code");
					sourceItem2.addActionListener(this);
					sourceItem2.setActionCommand("popup.sourceCode");
					viewMenu.add(sourceItem2);
			
					JMenuItem javadocsItem2 = new JMenuItem("Javadocs");
					javadocsItem2.addActionListener(this);
					javadocsItem2.setActionCommand("popup.javadocs");
					viewMenu.add(javadocsItem2);
		
					JMenuItem horizontalItem2 = new JMenuItem("Horizontally");
					horizontalItem2.addActionListener(this);
					horizontalItem2.setActionCommand("popup.horizontal");
					//viewMenu.add(horizontalItem2);
			
					JMenuItem verticalItem2 = new JMenuItem("Vertically");
					verticalItem2.addActionListener(this);
					verticalItem2.setActionCommand("popup.vertical");
					//viewMenu.add(verticalItem2);
				
				JMenu propertiesMenu = new JMenu("Properties");
					shortenJavaBox = new JCheckBox("Shorten Java Names");
					shortenJavaBox.addActionListener(this);
					shortenJavaBox.setActionCommand("properties.shorten");
					shortenJavaBox.setSelected(shortJava);
					propertiesMenu.add(shortenJavaBox);
					
					shortenOtherBox = new JCheckBox("Shorten Non-Java Names");
					shortenOtherBox.addActionListener(this);
					shortenOtherBox.setActionCommand("properties.shorten");
					shortenOtherBox.setSelected(shortOther);
					propertiesMenu.add(shortenOtherBox);
				
				alphaMenuBar.add(fileMenu);
				alphaMenuBar.add(editMenu);
				alphaMenuBar.add(viewMenu);
				alphaMenuBar.add(propertiesMenu);
				refreshMoveAndCopyMenu();
				windowMenu.addMenuListener(new WindowMenuListener(this,menuBar,windowMenu));
				alphaMenuBar.add(windowMenu);
				menuBar = alphaMenuBar;
			setJMenuBar(alphaMenuBar);	
					
		//now to add listeners to the components that will pop up the popup menu
			MouseListener popupListener = new PopupListener();
			alphaList.addMouseListener(popupListener);
	}
	
	public DesktopInternalFrame getCopy()
	{
		return new AlphabeticalListGUI(project, proGUI, getTitle(), shortenJavaBox.isSelected(), shortenOtherBox.isSelected(),desktop);
	}

	public void setProject(Project PRO)
	{
		project = PRO;
	}
	
	public Project getProject()
	{
		return project;
	}

	public Interface getSelectedInterface()
	{
		int index = alphaList.getSelectedIndex();
		
		if (index < 0)
			index = 0;
		return (Interface)(project.getInterfaceVec().elementAt(index));
	}

	public void fillList(boolean shortJava, boolean shortOther)
	{
		alphaModel.removeAllElements();
		//first check if the Vector of Interfaces needs to be alphabatized
			InterfaceUtilities.alphabatizeVector(project.getInterfaceVec(), shortJava, shortOther);
		
		for (int i = 0; i < (project.getInterfaceVec()).size(); i++)
		{
			alphaModel.addElement((((((Interface)((project.getInterfaceVec()).elementAt(i))).getPgmDefn()).getInterface_name(shortJava, shortOther))) );
		}
   	}
	
	public void fillList(Project pro, boolean shortJava, boolean shortOther)
	{
		alphaModel.removeAllElements();
		//first check if the Vector of Interfaces needs to be alphabatized
			InterfaceUtilities.alphabatizeVector(pro.getInterfaceVec(), shortJava, shortOther);
		for (int i = 0; i < (pro.getInterfaceVec()).size(); i++)
		{
			alphaModel.addElement( (((((Interface)((pro.getInterfaceVec()).elementAt(i))).getPgmDefn()).getInterface_name(shortJava, shortOther))) );
		}
	}
		
	public void actionPerformed( ActionEvent event)
	{		
		if (event.getActionCommand().equals("Save"))
		{
			
		}
		else if (event.getActionCommand().equals("Close"))
		{
			dispose();
			proGUI.setOpenNewAlphaWindowOnSelect(true);
		}
		else if (event.getActionCommand().equals("popup.singleUML"))
		{
			SingleUMLGUI singleUML = null;
			singleUML = new SingleUMLGUI(getSelectedInterface(), getSelectedInterface().getPgmDefn().getInterface_name(), true, false, desktop);
				
			if (singleUML != null)
			{
				singleUML.setVisible(true);
				desktop.getSelectedDesktop().add(singleUML);
			}
		}
		else if (event.getActionCommand().equals("popup.shortenedSource"))
		{
			ShortenedSourceGUI popupSsg = new ShortenedSourceGUI(getSelectedInterface(), project.getProjectName(), true, false,desktop);
			popupSsg.setVisible(true);
			desktop.getSelectedDesktop().add(popupSsg);
			
		}
		else if (event.getActionCommand().equals("popup.sourceCode"))
		{
			SourceCodeGUI popupSource = new SourceCodeGUI(getSelectedInterface(), project.getProjectName(), desktop);
			popupSource.setVisible(true);
			desktop.getSelectedDesktop().add(popupSource);
		}
		else if (event.getActionCommand().equals("popup.javadocs"))
		{
			JavadocsGUI javagui = new JavadocsGUI(getSelectedInterface(), project.getProjectName(), desktop);
			javagui.setVisible(true);
			desktop.getSelectedDesktop().add(javagui);
		}
		else if (event.getActionCommand().equals("popup.horizontal"))
		{
			HorizontalInterfacePanelGUI hPanel = new HorizontalInterfacePanelGUI(getSelectedInterface(), project.getProjectName());
			hPanel.setVisible(true);
		}
		else if (event.getActionCommand().equals("popup.vertical"))
		{
			VerticalInterfacePanelGUI vPanel = new VerticalInterfacePanelGUI(getSelectedInterface(), project.getProjectName());
			vPanel.setVisible(true);
		}
		else if (event.getActionCommand().equals("properties.shorten"))
		{
			fillList(shortenJavaBox.isSelected(), shortenOtherBox.isSelected());
		}
		else if (event.getActionCommand().equals("associate.source.code"))
		{
			Interface intF = getSelectedInterface();
			if (intF != null)
			{
				FileAssociationGUI sourceGUI = new FileAssociationGUI(intF, FileAssociationManager.JAVASOURCE);
				sourceGUI.setVisible(true);
			}
			else
			{
				//custom title, error icon
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,
						"You need to select an interface to associate a source code file with it",
						"Note",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else if (event.getActionCommand().equals("associate.javadocs"))
		{
			Interface intF = getSelectedInterface();
			if (intF != null)
			{
				FileAssociationGUI javadocsGUI = new FileAssociationGUI(intF, FileAssociationManager.JAVADOCS);
				javadocsGUI.setVisible(true);
			}
			else
			{
				//custom title, error icon
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,
						"You need to select an interface to associate a javadocs file with it",
						"Note",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else
		{
			AlphabeticalListGUI copy = (AlphabeticalListGUI)getCopy();
			copy.setVisible(true);
			processWindowChange(event,copy,this);
		}
	}
	
	public void valueChanged(ListSelectionEvent e)
	{
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
}

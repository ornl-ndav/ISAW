/*
 * File:  ExtendsListGUI.java
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
 * Revision 1.2  2004/03/11 18:35:12  bouzekc
 * Documented file using javadoc statements.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import devTools.Hawk.classDescriptor.gui.panel.ProjectSelectorJPanel;
import devTools.Hawk.classDescriptor.modeledObjects.Project;

/**
 * This class is under construction and is incomplete.  It could be used to 
 * display the class and interfaces in a project in a JTree based on what classes or 
 * interfaces it extends.
 * @author Dominic Kramer
 */
public class ExtendsListGUI extends JFrame implements ActionListener
{
	protected JPopupMenu popup;
	
	protected Project project;
	protected ProjectSelectorJPanel projectGUI;
	
	public ExtendsListGUI(Project PRO, ProjectSelectorJPanel PROGUI, String title)
	{
		//now to define some of the main varaibles
			project = PRO;
			projectGUI = PROGUI;
			
		//now to define the characteristics of the main window
			setTitle(title);
			setSize(175,400);
			addWindowListener(new WindowDestroyer());
			Container pane = getContentPane();
		
		//now to make the JScrollPane for the JTree
			JScrollPane scrollPane = new JScrollPane();
		
		//now to make the main JPanel
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			
		//now to make the buttons and button panel
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			
			JButton saveButton = new JButton("Save");
			saveButton.addActionListener(this);
			buttonPanel.add(saveButton);
			
			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(this);
			buttonPanel.add(closeButton);
			
		//now to add the components to the main panel
			mainPanel.add(buttonPanel, BorderLayout.SOUTH);
			mainPanel.add(scrollPane, BorderLayout.CENTER);
		//now to add the main panel to the pane
			pane.add(mainPanel);
			
		//now to make the popup menu
			popup = new JPopupMenu();
			//now to add components to the popup menu
			JMenuItem shortenedSourceItem = new JMenuItem("View Shortened Source Code");
			shortenedSourceItem.addActionListener(this);
			shortenedSourceItem.setActionCommand("popup.shortenedSource");
			popup.add(shortenedSourceItem);
			
			JMenuItem sourceItem = new JMenuItem("View Source Code");
			sourceItem.addActionListener(this);
			sourceItem.setActionCommand("popup.sourceCode");
			popup.add(sourceItem);
			
			JMenuItem javadocsItem = new JMenuItem("View Javadocs");
			javadocsItem.addActionListener(this);
			javadocsItem.setActionCommand("popup.javadocs");
			popup.add(javadocsItem);
		
			JMenuItem horizontalItem = new JMenuItem("View Horizontally");
			horizontalItem.addActionListener(this);
			horizontalItem.setActionCommand("popup.horizontal");
			popup.add(horizontalItem);
			
			JMenuItem verticalItem = new JMenuItem("View Vertically");
			verticalItem.addActionListener(this);
			verticalItem.setActionCommand("popup.vertical");
			popup.add(verticalItem);
			
		//now to add listeners to the components that will pop up the popup menu
			//MouseListener popupListener = new PopupListener();
			//use the add() method here to add a listener to the JTree
	}
	
	public void actionPerformed( ActionEvent event)
	{
		if (event.getActionCommand().equals("Save"))
		{
		
		}
		else if (event.getActionCommand().equals("Close"))
		{
			dispose();
		}
	}
	
	public class WindowDestroyer extends WindowAdapter
	{
		public WindowDestroyer() {}
	
		public void windowClosing(WindowEvent e)
		{
			dispose();
		}
	}
}

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
 * Revision 1.4  2004/05/27 13:40:39  kramer
 * Added method
 *   public Component[] determineWaitingComponents()
 * Added toolbars to the gui that allows the user to search and filter the list
 * of classes displayed.
 *
 * Revision 1.3  2004/03/12 19:46:16  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:09:14  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.internalFrame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.gui.panel.AlphabeticalListJPanel;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.Project;

/**
 * This is a special type of JInternalFrame that displays all of the classes and interfaces in a list 
 * in alphabetical order.
 * @author Dominic Kramer
 */
public class AlphabeticalListGUI extends DesktopInternalFrame implements ActionListener
{
	/**
	 * The specialized JPanel which holds the Interfaces in alphabetical order.
	 */
	protected AlphabeticalListJPanel listPanel;
	
	/**
	 * Create a new AlphabeticalListGUI window.
	 * @param pro The Project whose Interfaces are to be displayed.
	 * @param title The title of the window.
	 * @param shortJava True if java names are to be shortened by default.
	 * @param shortOther True if non-java names are to be shortened by default.
	 * @param desk The HawkDesktop onto which this window is placed.
	 */
	public AlphabeticalListGUI(Project pro, String title, boolean shortJava, boolean shortOther, HawkDesktop desk)
	{
		super(desk,desk.getSelectedDesktop(),new Interface(),false,false,false,false);
		
		//Now to set some characteristics about the main window
			setTitle(title);
			setLocation(0,0);
			setSize(300,400);
			setClosable(true);
			setIconifiable(true);
			setMaximizable(true);
			setResizable(true);
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			
			listPanel = new AlphabeticalListJPanel(pro,shortJava,shortOther,desktop,this);
			
			JPanel topPanel = new JPanel(new GridLayout(2,0));
			topPanel.add(listPanel.createToolBar());
			topPanel.add(listPanel.getSearchPanel());
			
			JPanel labelPanel = new JPanel(new GridLayout(1,1));
			labelPanel.add(listPanel.getLabel());
			
			mainPanel.add(topPanel, BorderLayout.NORTH);
			mainPanel.add(listPanel, BorderLayout.CENTER);
			mainPanel.add(labelPanel, BorderLayout.SOUTH);
			
			JMenuBar menuBar = new JMenuBar();
			JMenu fileMenu = new JMenu("File");
				JMenuItem closeItem = listPanel.getCloseMenuItem();
				closeItem.addActionListener(this);
				fileMenu.add(closeItem);
			menuBar.add(fileMenu);
			menuBar.add(listPanel.getEditMenu());
			menuBar.add(listPanel.getViewMenu());
			menuBar.add(listPanel.getPropertiesMenu(shortJava,shortOther));			
			menuBar.add(windowMenu);
			
			setJMenuBar(menuBar);
			getContentPane().add(mainPanel);
			
			pack();
	}
	
	/**
	 * Gets a copy of this window.
	 * @return A copy of this window.
	 */
	public AttachableDetachableFrame getCopy()
	{
		return new AlphabeticalListGUI(listPanel.getProject(), getTitle(), listPanel.getShortenJavaCheckBox().isSelected(), listPanel.getShortenOtherCheckBox().isSelected(),desktop);
	}
	
	/**
	 * Handles action events.
	 */
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Close"))
		{
			dispose();
		}
		else
		{
			listPanel.actionPerformed(event);
			super.actionPerformed(event);
		}
	}
	
	/**
	 * The Components in the array returned from this method are the Components that should have the 
	 * mouse use the waiting animation when an operation is in progress.
	 */
	public Component[] determineWaitingComponents()
	{
		return listPanel.determineWaitingComponents();
	}
}

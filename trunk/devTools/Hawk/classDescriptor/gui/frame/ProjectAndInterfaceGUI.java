/*
 * File:  ProjectAndInterfaceGUI.java
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
 * Revision 1.1  2004/02/07 05:08:51  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import devTools.Hawk.classDescriptor.gui.panel.InterfaceSelectorJPanel;
import devTools.Hawk.classDescriptor.gui.panel.ProjectSelectorJPanel;

/*
 * Created on Nov 18, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ProjectAndInterfaceGUI extends JFrame implements ActionListener
{
	//PROVEC is a Vector of Projects
	public ProjectAndInterfaceGUI(Vector PROVEC)
	{
		//now to define some of the charateristics about the window
			setTitle("");
			setSize(212,456);
			addWindowListener(new WindowDestroyer());
				
		//now to make the JMenuBar that will be at the top of the JFrame
			JMenuBar thisMenuBar = new JMenuBar();
				JMenu thisFileMenu = new JMenu("File");
					JMenuItem thisPrintItem = new JMenuItem("Print All");
					thisPrintItem.addActionListener(this);
					thisPrintItem.setActionCommand("this.print");
					thisFileMenu.add(thisPrintItem);
					
					JMenuItem thisSearchItem = new JMenuItem("Search All");
					thisSearchItem.addActionListener(this);
					thisSearchItem.setActionCommand("this.search");
					thisFileMenu.add(thisSearchItem);
					
					JMenuItem thisExitItem = new JMenuItem("Exit");
					thisExitItem.addActionListener(this);
					thisExitItem.setActionCommand("this.exit");
					thisFileMenu.add(thisExitItem);
				thisMenuBar.add(thisFileMenu);
				
				JMenu aboutMenu = new JMenu("Help");
					JMenuItem aboutItem = new JMenuItem("About Hawk");
					aboutItem.addActionListener(this);
					aboutItem.setActionCommand("this.about");
					aboutMenu.add(aboutItem);
				thisMenuBar.add(aboutMenu);
					
			setJMenuBar(thisMenuBar);
		
		//the following will add a JList to the panel that the user can right click on and get a popup menu
		//the JList contains all of the currently opened projects
			JInternalFrame projectFrame = new JInternalFrame();
			Container projectFramePane = projectFrame.getContentPane();
			
			JPanel projectPanel = new JPanel();
			projectPanel.setLayout(new BorderLayout());
			
			ProjectSelectorJPanel psjp = new ProjectSelectorJPanel(PROVEC, null);
			
			projectFrame.setJMenuBar(psjp.getProjectJMenuBar());
			projectPanel.add(psjp.getProjectSelectorJPanel(), BorderLayout.CENTER);			
			projectFramePane.add(projectPanel);
			
			//now to set some of the properties about projectFrame
				projectFrame.setTitle("Projects");
				projectFrame.setLocation(0,0);
				projectFrame.setResizable(true);
				projectFrame.setSize(200,200);
				projectFrame.setVisible(true);
				projectFrame.setIconifiable(true);
				projectFrame.setMaximizable(true);
			
		//the following will add a JList to the panel that the user can right click on and a popup menu	
		//the JList contains currently opened Interfaces that haven't been assigned to a Project yet
			JInternalFrame interfaceFrame = new JInternalFrame();
			Container interfaceFramePane = interfaceFrame.getContentPane();
			
			JPanel interfacePanel = new JPanel();
			interfacePanel.setLayout(new BorderLayout());
			
			InterfaceSelectorJPanel isjp = new InterfaceSelectorJPanel(null);
			
			interfaceFrame.setJMenuBar(isjp.getInterfaceJMenuBar());
			interfacePanel.add(isjp.getInterfaceSelectorJPanel(), BorderLayout.CENTER);
			
			interfaceFramePane.add(interfacePanel, null);
			
			//now to set some of the characteristics interfaceFramePane
				interfaceFrame.setTitle("Interfaces");
				interfaceFrame.setLocation(0,200);
				interfaceFrame.setResizable(true);
				interfaceFrame.setSize(200,200);
				interfaceFrame.setVisible(true);
				interfaceFrame.setIconifiable(true);
				interfaceFrame.setMaximizable(true);
			
		//now to add the internal frames to the pane
		JDesktopPane desktop = new JDesktopPane();	
			desktop.add(projectFrame);
			desktop.add(interfaceFrame);
			
		setContentPane(desktop);
	}
	
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("this.print"))
		{
			
		}
		else if (event.getActionCommand().equals("this.search"))
		{
		}
		else if (event.getActionCommand().equals("this.about"))
		{
			AboutGUI aboutGUI = new AboutGUI();
			aboutGUI.setVisible(true);
		}
		else if (event.getActionCommand().equals("this.exit"))
		{
			System.exit(0);
		}
	}
	
	public class WindowDestroyer extends WindowAdapter
	{
		public WindowDestroyer() {}
		
		public void windowClosing(WindowEvent event)
		{
			System.exit(0);
		}
	}
}

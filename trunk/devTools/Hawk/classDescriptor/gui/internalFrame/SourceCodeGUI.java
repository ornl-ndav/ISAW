/*
 * File:  SourceCodeGUI.java
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
 * Revision 1.1  2004/02/07 05:09:16  bouzekc
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;

public class SourceCodeGUI extends ColorfulTextGUI implements ActionListener
{
	protected JTextPane sourcePane;
	protected Interface selectedInterface;
	protected StyledDocument document;
	
	public SourceCodeGUI(Interface INT, String title, HawkDesktop desk)
	{
		super(desk);
		
		//now to instantiate selectedInterface
			selectedInterface = INT;
		//now to set some of the characteristics of the window
			setTitle(title);
			setLocation(0,0);
			setSize(175,400);
			setClosable(true);
			setIconifiable(true);
			setMaximizable(true);
			setResizable(true);
		
		//now to make the main areas of the frame
			Container pane = getContentPane();
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			
		//now to create the area for placing the javadocs in html format
			sourcePane = new JTextPane();
			document = sourcePane.getStyledDocument();
			addStylesToDocument(document);
			styleDocument(INT.getSourceCodeAsString(),document);
						
		//now to create the JScrollPane to put the JEditorPane on
			JScrollPane scrollPane = new JScrollPane(sourcePane);
		
		//now to add the components to the main panel
			mainPanel.add(scrollPane, BorderLayout.CENTER);
			
		pane.add(mainPanel);
		
		//Now to make the JMenuBar
			JMenuBar sourceMenuBar = new JMenuBar();
				JMenu fileMenu = new JMenu("File");
					JMenuItem closeItem = new JMenuItem("Close");
					closeItem.setActionCommand("Close");
					closeItem.addActionListener(this);
				fileMenu.add(closeItem);
			sourceMenuBar.add(fileMenu);
			refreshMoveAndCopyMenu();
			windowMenu.addMenuListener(new WindowMenuListener(this,menuBar,windowMenu));
			sourceMenuBar.add(windowMenu);
		menuBar = sourceMenuBar;
		setJMenuBar(sourceMenuBar);
	}
	
	public DesktopInternalFrame getCopy()
	{
		return new SourceCodeGUI(selectedInterface,getTitle(),desktop);
	}
		
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Close"))
		{
			dispose();
		}
		else
		{
			SourceCodeGUI copy = (SourceCodeGUI)getCopy();
			copy.setVisible(true);
			processWindowChange(event,copy,this);
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

/*
 * File:  HorizontalInterfacePanelGUI.java
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
 * Revision 1.3  2004/03/12 19:46:15  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:08:50  bouzekc
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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import devTools.Hawk.classDescriptor.modeledObjects.Interface;

/**
 * This class is a window displaying information about the supplied Interface by using JPanels 
 * (containing specific information about the Interface) placed horizontally across the window.  
 * Currently, this class is incomplete and will probably be removed.
 * @author Dominic Kramer
 * @deprecated
 */
public class HorizontalInterfacePanelGUI extends JFrame implements ActionListener
{
	protected Interface selectedInterface;
	
	public HorizontalInterfacePanelGUI(Interface INTF, String title)
	{
		//now to define selectedInterface
			selectedInterface = INTF;
			
		//now to define the characteristics of the main window
			setTitle(title);
			setSize(800,200);
			addWindowListener(new WindowDestroyer());
			
		//now to get the main parts of the JFrame
			Container pane = getContentPane();
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
		
		//the following panel is just made to be placed in the location
		//BorderLayout.SOUTH and is only there to make the window
		//look slightly more symmetric
			JPanel blankPanel = new JPanel();
			blankPanel.add(new JLabel());
		
		//the following panel is there to label the sections of the gui
			//JPanel labelPanel = new JPanel();
			//labelPanel.setLayout(new GridLayout(0,4));
			//labelPanel.add(new JLabel("General"));
			//labelPanel.add(new JLabel("Attributes"));
			//labelPanel.add(new JLabel("Constructors"));
			//labelPanel.add(new JLabel("Methods"));
		
		//mainPanel.add(labelPanel, BorderLayout.NORTH);							
		mainPanel.add(INTF.getHorizontalInterfaceJPanel(), BorderLayout.CENTER);
		mainPanel.add(blankPanel, BorderLayout.SOUTH);
		
		pane.add(mainPanel);
		
		//Now to make the JMenuBar
			JMenuBar horizontalIntfMenuBar = new JMenuBar();
				JMenu file = new JMenu("File");
					JMenuItem close = new JMenuItem("Close");
					close.setActionCommand("Close");
					close.addActionListener(this);
				file.add(close);
			horizontalIntfMenuBar.add(file);
		setJMenuBar(horizontalIntfMenuBar);
	}
	
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Close"))
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
